/**
 * `WorkspaceSync` keeps a local copy of a workspace context in step with Foresignal (docs/Client-Contract.md §5):
 *
 * - it reads the workspace, then follows the `workspace` topic of `/v1/events/stream` with this client's id and
 *   kind;
 * - it applies remote `workspace.context` patches in version order, and re-reads the workspace on a version gap
 *   or a reconnect (workspace events are id-less and not replayed);
 * - it ignores the content of events carrying its own `originClientId` (the PATCH response already gave it the
 *   new state), using them only to advance the version;
 * - it sends local changes as JSON merge patches with `If-Match: <contextVersion>` and `Foresignal-Client-Id`,
 *   and on `412 WORKSPACE_CONTEXT_STALE` re-reads the workspace, re-applies the intent and retries;
 * - it exposes presence from `workspace.presence` events.
 */
import { ApiError, apiErrorFrom } from "../errors.js";
import { HttpTransport, type TransportOptions } from "../http.js";
import type { BackoffOptions } from "../realtime/backoff.js";
import { ChangeStream } from "../realtime/change-stream.js";
import { applyMergePatch, cloneJson, type WorkspaceContext } from "./context.js";

/** Client kinds accepted by the `workspace` topic. */
export type ClientKind = "globe" | "map" | "analyst" | "satellite" | "rf" | "mobile" | "agent" | "other";

/** A connected client (`workspace.presence`). */
export interface PresenceClient {
  clientId: string;
  clientKind: ClientKind | string;
  principal?: string | null;
  connectedAt: string;
}

/** A `workspace.presence` event. */
export interface PresenceEvent {
  workspaceId: string;
  clients: PresenceClient[];
  change?: { type: "joined" | "left"; clientId: string } | null;
  at: string;
}

/** A `workspace.context` event. */
export interface ContextPatchEvent {
  workspaceId: string;
  version: number;
  patch: Record<string, unknown>;
  originClientId?: string | null;
  principal?: string | null;
  at: string;
}

/** The server's answer to a context PATCH (`WorkspaceContextState`). */
export interface WorkspaceContextState {
  workspaceId: string;
  contextVersion: number;
  context: WorkspaceContext;
}

/** Where a context change came from. */
export type ContextOrigin = "remote" | "local" | "refresh";

/** A change of the local context copy. */
export interface ContextChange {
  context: WorkspaceContext;
  version: number;
  origin: ContextOrigin;
  /** The remote event, for `origin: "remote"`. */
  event?: ContextPatchEvent;
}

/** A local change: a merge patch, or a function computing one from the current context (null for no change). */
export type ContextIntent = Record<string, unknown> | ((context: WorkspaceContext) => Record<string, unknown> | null);

/** Settings of a {@link WorkspaceSync}. */
export interface WorkspaceSyncOptions extends TransportOptions {
  workspaceId: string;
  /** This client's id (generated when absent): presence and `originClientId`. */
  clientId?: string;
  /** This client's kind (default `other`). */
  clientKind?: ClientKind;
  /** Attempts per local change when the version is stale (default 5). */
  maxAttempts?: number;
  /** The server's heartbeat interval (default 15 s). */
  heartbeatIntervalMs?: number;
  backoff?: BackoffOptions;
  /** Use a stream ticket for the event stream (clients that cannot set headers). */
  useTicket?: boolean;
  transport?: HttpTransport;
  /** Sleep used between stream reconnects (tests replace it). */
  sleep?: (ms: number, signal: AbortSignal) => Promise<void>;
}

type Listener<T> = (value: T) => void;

/** Generates a client id (`c-` and 16 random hex characters). */
function generateClientId(): string {
  const bytes = new Uint8Array(8);
  globalThis.crypto.getRandomValues(bytes);
  return "c-" + Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

/** Keeps one workspace context synchronized. */
export class WorkspaceSync {
  /** The workspace followed. */
  readonly workspaceId: string;
  /** This client's id. */
  readonly clientId: string;
  /** This client's kind. */
  readonly clientKind: ClientKind;

  private readonly transport: HttpTransport;
  private readonly options: WorkspaceSyncOptions;
  private readonly maxAttempts: number;
  private stream: ChangeStream | undefined;
  private loop: Promise<void> | undefined;
  private state: { context: WorkspaceContext; version: number } | undefined;
  private readonly pending = new Map<number, ContextPatchEvent>();
  private presenceClients: PresenceClient[] = [];
  private readonly contextListeners = new Set<Listener<ContextChange>>();
  private readonly presenceListeners = new Set<Listener<PresenceEvent>>();
  private readonly errorListeners = new Set<Listener<unknown>>();
  private refreshing: Promise<void> | undefined;

  /** Creates the helper; call {@link start}. */
  constructor(options: WorkspaceSyncOptions) {
    this.options = options;
    this.workspaceId = options.workspaceId;
    this.clientId = options.clientId ?? generateClientId();
    this.clientKind = options.clientKind ?? "other";
    this.maxAttempts = Math.max(1, options.maxAttempts ?? 5);
    this.transport = options.transport ?? new HttpTransport(options);
  }

  /** The current context (a copy). */
  get context(): WorkspaceContext | undefined {
    return this.state ? cloneJson(this.state.context) : undefined;
  }

  /** The current context version. */
  get version(): number | undefined {
    return this.state?.version;
  }

  /** The clients connected to the workspace, from the latest presence event. */
  get presence(): PresenceClient[] {
    return [...this.presenceClients];
  }

  /** Subscribes to context changes; returns the unsubscribe function. */
  onContext(listener: Listener<ContextChange>): () => void {
    this.contextListeners.add(listener);
    return () => this.contextListeners.delete(listener);
  }

  /** Subscribes to presence events; returns the unsubscribe function. */
  onPresence(listener: Listener<PresenceEvent>): () => void {
    this.presenceListeners.add(listener);
    return () => this.presenceListeners.delete(listener);
  }

  /** Subscribes to background errors (stream failures, failed re-reads); returns the unsubscribe function. */
  onError(listener: Listener<unknown>): () => void {
    this.errorListeners.add(listener);
    return () => this.errorListeners.delete(listener);
  }

  /** Reads the workspace and starts following its events. Resolves once the first read is done. */
  async start(): Promise<void> {
    if (this.stream) {
      return;
    }
    await this.refresh();
    const stream = new ChangeStream({
      ...this.options,
      transport: this.transport,
      endpoint: "events",
      filters: {
        topics: "workspace",
        "workspace.workspaceId": this.workspaceId,
        "workspace.clientId": this.clientId,
        "workspace.clientKind": this.clientKind,
      },
      snapshot: false,
    });
    this.stream = stream;
    this.loop = this.follow(stream);
  }

  /** Stops following the workspace. */
  async stop(): Promise<void> {
    this.stream?.close();
    await this.loop?.catch(() => undefined);
    this.stream = undefined;
    this.loop = undefined;
  }

  /** Re-reads the workspace (`GET /v1/workspaces/{id}`) and replaces the local copy. */
  async refresh(): Promise<void> {
    if (!this.refreshing) {
      this.refreshing = (async () => {
        try {
          const { data } = await this.transport.getJson<{ context?: WorkspaceContext; contextVersion: number }>(
            `/v1/workspaces/${encodeURIComponent(this.workspaceId)}`);
          this.accept(data.context ?? {}, data.contextVersion, "refresh");
        } finally {
          this.refreshing = undefined;
        }
      })();
    }
    return this.refreshing;
  }

  /**
   * Sends a local change. A function intent is re-evaluated against the fresh context after a `412`.
   *
   * @returns the new state, or the current one when the intent produced no patch
   * @throws ApiError when the server refuses the change, or still answers 412 after `maxAttempts`
   */
  async update(intent: ContextIntent): Promise<WorkspaceContextState> {
    if (!this.state) {
      await this.refresh();
    }
    let lastError: unknown;
    for (let attempt = 0; attempt < this.maxAttempts; attempt++) {
      const current = this.state!;
      const patch = typeof intent === "function" ? intent(cloneJson(current.context)) : intent;
      if (patch === null) {
        return { workspaceId: this.workspaceId, contextVersion: current.version, context: cloneJson(current.context) };
      }
      const response = await this.transport.fetch("PATCH", `/v1/workspaces/${encodeURIComponent(this.workspaceId)}/context`, {
        body: patch,
        contentType: "application/merge-patch+json",
        headers: {
          Accept: "application/json",
          "If-Match": String(current.version),
          "Foresignal-Client-Id": this.clientId,
        },
      });
      if (response.ok) {
        const state = (await response.json()) as WorkspaceContextState;
        this.accept(state.context ?? {}, state.contextVersion, "local");
        return state;
      }
      const error = await apiErrorFrom(response);
      if (error.status !== 412) {
        throw error;
      }
      lastError = error;
      await this.refresh();
    }
    throw lastError instanceof ApiError ? lastError : new Error("The workspace context stayed stale");
  }

  /** Reads stream signals until the stream is closed. */
  private async follow(stream: ChangeStream): Promise<void> {
    let opened = 0;
    try {
      for await (const signal of stream) {
        if (signal.type === "open") {
          opened++;
          if (opened > 1) {
            // Workspace events are not replayed: anything sent while disconnected is only in a fresh read.
            await this.safely(() => this.refresh());
          }
        } else if (signal.type === "event") {
          if (signal.event === "workspace.context") {
            await this.safely(() => this.onContextEvent(signal.data as ContextPatchEvent));
          } else if (signal.event === "workspace.presence") {
            this.onPresenceEvent(signal.data as PresenceEvent);
          }
        }
      }
    } catch (error) {
      if (!stream.closed) {
        this.emitError(error);
      }
    }
  }

  /** Handles a `workspace.context` event. */
  async onContextEvent(event: ContextPatchEvent): Promise<void> {
    if (!event || event.workspaceId !== this.workspaceId || !this.state) {
      return;
    }
    if (event.version <= this.state.version) {
      return;
    }
    this.pending.set(event.version, event);
    this.drain();
    if (this.pending.size > 0) {
      // A version is missing between the local copy and the buffered events: re-read, then apply the rest.
      await this.refresh();
    }
  }

  /** Applies buffered events that continue the local version. */
  private drain(): void {
    if (!this.state) {
      return;
    }
    for (const version of [...this.pending.keys()]) {
      if (version <= this.state.version) {
        this.pending.delete(version);
      }
    }
    for (let next = this.pending.get(this.state.version + 1); next; next = this.pending.get(this.state.version + 1)) {
      this.pending.delete(next.version);
      if (next.originClientId === this.clientId) {
        // Our own change: the PATCH response carries the state; only the version moves on here.
        this.state = { context: this.state.context, version: next.version };
        continue;
      }
      this.state = { context: applyMergePatch<WorkspaceContext>(this.state.context, next.patch), version: next.version };
      this.emitContext({ context: cloneJson(this.state.context), version: next.version, origin: "remote", event: next });
    }
  }

  /** Replaces the local copy with a server state that is not older than it. */
  private accept(context: WorkspaceContext, version: number, origin: ContextOrigin): void {
    if (this.state && version < this.state.version) {
      return;
    }
    this.state = { context: cloneJson(context), version };
    this.drain();
    this.emitContext({ context: cloneJson(this.state.context), version: this.state.version, origin });
  }

  /** Handles a `workspace.presence` event. */
  private onPresenceEvent(event: PresenceEvent): void {
    if (!event || event.workspaceId !== this.workspaceId) {
      return;
    }
    this.presenceClients = [...(event.clients ?? [])];
    for (const listener of this.presenceListeners) {
      listener(event);
    }
  }

  private emitContext(change: ContextChange): void {
    for (const listener of this.contextListeners) {
      listener(change);
    }
  }

  private emitError(error: unknown): void {
    for (const listener of this.errorListeners) {
      listener(error);
    }
  }

  private async safely(action: () => Promise<void>): Promise<void> {
    try {
      await action();
    } catch (error) {
      this.emitError(error);
    }
  }
}
