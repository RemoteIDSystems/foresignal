/**
 * `ChangeStream`: a resuming reader of `GET /v1/changes/stream` and `GET /v1/events/stream` that implements the
 * client contract of docs/Core-Data-Model.md §10.2 (prompts 015 and 023):
 *
 * - resumes with `Last-Event-ID` (or `after=` / `lastEventId=` when a stream ticket replaces the header) and
 *   tracks the last `change` id;
 * - on `reset`: emits the reset; when the reason is `resync_required` it pages the snapshot
 *   (`/v1/records?order=id`, keeping `watermarkSeq`) and reopens at the watermark, otherwise it reopens at
 *   `resumeAfter` (or the last id);
 * - on `resync`: pages the current state again and keeps the stream open;
 * - applies `live` and `touch` events only when their `observedAt` is not older than the stored one;
 * - reconnects when no event or heartbeat arrives within 2.5 × the heartbeat interval, with jittered exponential
 *   backoff (1 s → 60 s) that honors `retry:` and `Retry-After`.
 *
 * It uses `fetch` streaming only, so it runs in browsers and Node.js alike and does not need the generated types.
 */
import { ApiError, HeartbeatTimeoutError, apiErrorFrom } from "../errors.js";
import { HttpTransport, type Query, type TransportOptions } from "../http.js";
import { Backoff, type BackoffOptions } from "./backoff.js";
import { SseParser } from "./sse.js";

/** Which endpoint to read. */
export type StreamEndpoint = "changes" | "events";

/** Payload of a `change` event (the `Change` schema; only the members the stream logic reads are typed). */
export interface ChangeEventData {
  seq: number;
  source: string;
  externalId: string;
  kind?: string;
  version?: number;
  observedAt?: string;
  tombstone?: boolean;
  leftFilter?: boolean;
  [member: string]: unknown;
}

/** Payload of a `live` event. */
export interface LiveEventData {
  sourceId: string;
  externalId: string;
  observedAt: string;
  leftFilter?: boolean;
  [member: string]: unknown;
}

/** Payload of a `touch` event. */
export interface TouchEventData {
  sourceId: string;
  externalId: string;
  observedAt: string;
  [member: string]: unknown;
}

/** Payload of a `resync` event. */
export interface ResyncEventData {
  reason: "gap" | "live_cap" | "live_dropped" | string;
  watermarkSeq: number;
}

/** Payload of a `reset` event (the server closes the stream after it). */
export interface ResetEventData {
  reason: "buffer_overflow" | "replay_cap" | "resync_required" | string;
  resumeAfter?: number | null;
  watermarkSeq?: number | null;
}

/** Payload of the `hello` event that opens `/v1/events/stream`. */
export interface HelloEventData {
  streamId: string;
  topics: string[];
  watermarkSeq: number;
  filterVersion: number;
  streamKey?: string | null;
}

/** A current record from the snapshot (`Record` schema; only the members the stream logic reads are typed). */
export interface SnapshotRecord {
  source: string;
  externalId: string;
  observedAt?: string;
  [member: string]: unknown;
}

/** What the stream yields. */
export type StreamSignal =
  | { type: "open"; url: string }
  | { type: "hello"; data: HelloEventData }
  | { type: "change"; data: ChangeEventData; id: string | undefined }
  | { type: "live"; data: LiveEventData }
  | { type: "touch"; data: TouchEventData }
  | { type: "resync"; data: ResyncEventData }
  | { type: "reset"; data: ResetEventData }
  | { type: "snapshot"; cause: "reset" | "resync"; items: SnapshotRecord[]; watermarkSeq: number | null; done: boolean }
  | { type: "heartbeat" }
  | { type: "event"; event: string; data: unknown; id: string | undefined }
  | { type: "reconnecting"; attempt: number; delayMs: number; reason: string };

/** Settings of a {@link ChangeStream}. */
export interface ChangeStreamOptions extends TransportOptions {
  /** `changes` (`/v1/changes/stream`, default) or `events` (`/v1/events/stream`). */
  endpoint?: StreamEndpoint;
  /**
   * Stream query parameters: for `changes` the filters (`source`, `kind`, `bbox`, `live`, `touches`, …), for
   * `events` `topics` and the prefixed topic filters (`changes.source`, `workspace.workspaceId`, …).
   */
  filters?: Query;
  /** Start after this change seq (the watermark of a snapshot you already hold). */
  after?: number;
  /** Start from this event id (`/v1/events/stream` composite cursor, e.g. `c=1234;a=56`). */
  lastEventId?: string;
  /** The server's heartbeat interval (`stream.limits.heartbeatSeconds`, default 15 s). */
  heartbeatIntervalMs?: number;
  /** Reconnect backoff (default 1 s → 60 s). */
  backoff?: BackoffOptions;
  /**
   * Authenticate the stream with a ticket from `POST /v1/stream-tickets` instead of the `Authorization` header,
   * and send the cursor as a query parameter: for clients that cannot set headers on the stream request.
   */
  useTicket?: boolean;
  /** Page `/v1/records?order=id` on `reset {resync_required}` and on `resync` (default true). */
  snapshot?: boolean;
  /** Snapshot page size (default 1000, at most 5000). */
  snapshotPageSize?: number;
  /** Snapshot query; defaults to the stream's record filters. */
  snapshotQuery?: Query;
  /** A transport to reuse instead of building one from the connection settings. */
  transport?: HttpTransport;
  /** Sleep used between reconnects (tests replace it). */
  sleep?: (ms: number, signal: AbortSignal) => Promise<void>;
}

/** Filter names a records snapshot understands. */
const SNAPSHOT_KEYS = new Set([
  "source", "kind", "bbox", "externalId", "near", "radiusKm", "place", "view", "inWatchlist", "licencePolicy",
]);

/** Event names handled by the stream itself; everything else is yielded as `{type: "event"}`. */
const CORE_EVENTS = new Set(["hello", "change", "live", "touch", "resync", "reset", "heartbeat"]);

/** Returns the value of `key` in a composite cursor (`c=1234;a=56`), or undefined. */
export function compositeCursorValue(cursor: string | undefined, key: string): number | undefined {
  if (!cursor) {
    return undefined;
  }
  for (const part of cursor.split(";")) {
    const eq = part.indexOf("=");
    if (eq > 0 && part.slice(0, eq) === key) {
      const n = Number(part.slice(eq + 1));
      return Number.isFinite(n) ? n : undefined;
    }
  }
  return undefined;
}

/** Returns `cursor` with `key` set to `value` (added when absent). */
export function withCompositeCursorValue(cursor: string | undefined, key: string, value: number): string {
  const parts = (cursor ? cursor.split(";") : []).filter((p) => p !== "" && !p.startsWith(key + "="));
  parts.unshift(`${key}=${value}`);
  return parts.join(";");
}

/** Remembers the newest `observedAt` per entity and refuses older updates. */
export class ObservedAtGuard {
  private readonly latest = new Map<string, number>();

  /** The key of an entity. */
  static key(sourceId: string, externalId: string): string {
    return `${sourceId}\u0000${externalId}`;
  }

  /** True (and remembered) when `observedAt` is not older than the stored value; unparseable times pass. */
  accept(key: string, observedAt: string | undefined): boolean {
    const t = observedAt ? Date.parse(observedAt) : Number.NaN;
    if (Number.isNaN(t)) {
      return true;
    }
    const stored = this.latest.get(key);
    if (stored !== undefined && t < stored) {
      return false;
    }
    this.latest.set(key, t);
    return true;
  }

  /** Remembers `observedAt` when it is newer than the stored value (changes and snapshot rows). */
  observe(key: string, observedAt: string | undefined): void {
    this.accept(key, observedAt);
  }

  /** The stored `observedAt` in epoch milliseconds, or undefined. */
  get(key: string): number | undefined {
    return this.latest.get(key);
  }

  /** Number of entities tracked. */
  get size(): number {
    return this.latest.size;
  }
}

/** The default sleep: resolves after `ms`, or early when `signal` aborts. */
function defaultSleep(ms: number, signal: AbortSignal): Promise<void> {
  return new Promise((resolve) => {
    if (signal.aborted || ms <= 0) {
      resolve();
      return;
    }
    const timer = setTimeout(done, ms);
    function done(): void {
      clearTimeout(timer);
      signal.removeEventListener("abort", done);
      resolve();
    }
    signal.addEventListener("abort", done);
  });
}

/** Parses an event's data as JSON, or returns the raw text. */
function parseData(data: string): unknown {
  try {
    return JSON.parse(data);
  } catch {
    return data;
  }
}

/** A resuming change stream. Iterate it with `for await`; call {@link close} to stop. */
export class ChangeStream implements AsyncIterable<StreamSignal> {
  /** The endpoint read. */
  readonly endpoint: StreamEndpoint;
  /** The `observedAt` guard (exposed for inspection). */
  readonly guard = new ObservedAtGuard();
  /** Live and touch events dropped by the guard. */
  staleDropped = 0;
  /** The `streamId` from the latest `hello` (`/v1/events/stream`). */
  streamId: string | undefined;

  private readonly transport: HttpTransport;
  private readonly filters: Query;
  private readonly watchdogMs: number;
  private readonly backoff: Backoff;
  private readonly useTicket: boolean;
  private readonly snapshotEnabled: boolean;
  private readonly snapshotPageSize: number;
  private readonly snapshotQueryOverride: Query | undefined;
  private readonly sleepFn: (ms: number, signal: AbortSignal) => Promise<void>;
  private readonly stopController = new AbortController();
  private connection: AbortController | undefined;
  private ticket: string | undefined;
  private seq: number | undefined;
  private eventId: string | undefined;
  private serverRetryMs: number | undefined;
  private started = false;

  /** Creates a stream; nothing is sent until it is iterated. */
  constructor(options: ChangeStreamOptions) {
    this.endpoint = options.endpoint ?? "changes";
    this.transport = options.transport ?? new HttpTransport(options);
    this.filters = { ...(options.filters ?? {}) };
    this.watchdogMs = Math.round(2.5 * (options.heartbeatIntervalMs ?? 15_000));
    this.backoff = new Backoff(options.backoff);
    this.useTicket = options.useTicket ?? false;
    this.snapshotEnabled = options.snapshot ?? true;
    this.snapshotPageSize = Math.min(5000, Math.max(1, options.snapshotPageSize ?? 1000));
    this.snapshotQueryOverride = options.snapshotQuery;
    this.sleepFn = options.sleep ?? defaultSleep;
    this.seq = options.after ?? compositeCursorValue(options.lastEventId, "c");
    this.eventId = options.lastEventId ?? (options.after !== undefined && this.endpoint === "events"
      ? `c=${options.after}` : undefined);
  }

  /** The last change seq received (or the watermark the stream reopened at). */
  get lastSeq(): number | undefined {
    return this.seq;
  }

  /** The last event id received (`/v1/events/stream` composite cursor; the seq on `/v1/changes/stream`). */
  get lastEventId(): string | undefined {
    return this.endpoint === "events" ? this.eventId : this.seq?.toString();
  }

  /** The watchdog timeout in milliseconds (2.5 × the heartbeat interval). */
  get watchdogTimeoutMs(): number {
    return this.watchdogMs;
  }

  /** Whether {@link close} was called. */
  get closed(): boolean {
    return this.stopController.signal.aborted;
  }

  /** Stops the stream: the current read is aborted and the iteration ends. */
  close(): void {
    this.stopController.abort();
    this.connection?.abort();
  }

  /** Iterates the stream (once). */
  [Symbol.asyncIterator](): AsyncIterator<StreamSignal> {
    if (this.started) {
      throw new Error("A ChangeStream can be iterated only once");
    }
    this.started = true;
    return this.run();
  }

  /** The reconnect loop. */
  private async *run(): AsyncGenerator<StreamSignal, void, undefined> {
    while (!this.closed) {
      let response: Response;
      let url: string;
      try {
        ({ response, url } = await this.connect());
      } catch (error) {
        if (this.closed) {
          return;
        }
        if (!this.retryable(error)) {
          throw error;
        }
        const floor = Math.max(this.serverRetryMs ?? 0, error instanceof ApiError ? error.retryAfterMs ?? 0 : 0);
        yield* this.pause(this.backoff.next(floor), describe(error));
        continue;
      }
      this.backoff.reset();
      yield { type: "open", url };

      const outcome: { reset?: ResetEventData; error?: unknown } = {};
      const reader = response.body!.getReader();
      const decoder = new TextDecoder();
      const parser = new SseParser();
      try {
        reading: for (;;) {
          const { value, done } = await this.read(reader);
          if (done) {
            break;
          }
          const messages = parser.feed(decoder.decode(value, { stream: true }));
          if (parser.retryMs !== undefined) {
            this.serverRetryMs = parser.retryMs;
          }
          for (const message of messages) {
            if (message.event === "reset") {
              outcome.reset = parseData(message.data) as ResetEventData;
              yield { type: "reset", data: outcome.reset };
              break reading;
            }
            yield* this.handle(message.event, message.data, message.id);
          }
        }
      } catch (error) {
        outcome.error = error;
      } finally {
        this.connection?.abort();
        try {
          reader.releaseLock();
        } catch {
          // A read may still be pending on an aborted body; the lock goes with the connection.
        }
      }
      if (this.closed) {
        return;
      }

      if (outcome.reset) {
        try {
          yield* this.afterReset(outcome.reset);
        } catch (error) {
          if (this.closed) {
            return;
          }
          if (!this.retryable(error)) {
            throw error;
          }
          // The cursor is unchanged, so the next connection is refused again and the snapshot is retried.
          yield* this.pause(this.backoff.next(this.serverRetryMs ?? 0), describe(error));
          continue;
        }
        if (this.serverRetryMs) {
          yield* this.pause(this.serverRetryMs, `reset: ${outcome.reset.reason}`);
        } else {
          yield { type: "reconnecting", attempt: 0, delayMs: 0, reason: `reset: ${outcome.reset.reason}` };
        }
      } else {
        const reason = outcome.error ? describe(outcome.error) : "stream closed by the server";
        yield* this.pause(this.backoff.next(this.serverRetryMs ?? 0), reason);
      }
    }
  }

  /** Handles one non-reset event. */
  private async *handle(event: string, raw: string, id: string | undefined): AsyncGenerator<StreamSignal, void, undefined> {
    if (id !== undefined) {
      if (this.endpoint === "events") {
        this.eventId = id;
        const c = compositeCursorValue(id, "c");
        if (c !== undefined) {
          this.seq = c;
        }
      } else if (event === "change") {
        const n = Number(id);
        if (Number.isFinite(n)) {
          this.seq = n;
        }
      }
    }
    const data = parseData(raw);
    switch (event) {
      case "hello": {
        const hello = data as HelloEventData;
        this.streamId = hello.streamId;
        yield { type: "hello", data: hello };
        return;
      }
      case "change": {
        const change = data as ChangeEventData;
        this.guard.observe(ObservedAtGuard.key(change.source, change.externalId), change.observedAt);
        yield { type: "change", data: change, id };
        return;
      }
      case "live":
      case "touch": {
        const update = data as LiveEventData;
        if (!this.guard.accept(ObservedAtGuard.key(update.sourceId, update.externalId), update.observedAt)) {
          this.staleDropped++;
          return;
        }
        yield event === "live" ? { type: "live", data: update } : { type: "touch", data: update as TouchEventData };
        return;
      }
      case "resync": {
        const resync = data as ResyncEventData;
        yield { type: "resync", data: resync };
        if (this.snapshotEnabled) {
          yield* this.snapshot("resync");
        }
        return;
      }
      case "heartbeat":
        yield { type: "heartbeat" };
        return;
      default:
        if (!CORE_EVENTS.has(event)) {
          yield { type: "event", event, data, id };
        }
    }
  }

  /** Decides where to reopen after a `reset`, paging the snapshot when the reason is `resync_required`. */
  private async *afterReset(reset: ResetEventData): AsyncGenerator<StreamSignal, void, undefined> {
    if (reset.reason === "resync_required") {
      let watermark: number | null | undefined = reset.watermarkSeq;
      if (this.snapshotEnabled) {
        watermark = yield* this.snapshot("reset");
      }
      this.moveCursor(watermark ?? undefined);
      return;
    }
    if (reset.resumeAfter !== null && reset.resumeAfter !== undefined) {
      this.moveCursor(reset.resumeAfter);
    }
  }

  /** Moves the resume point to `seq` (both endpoints); undefined means "live from now". */
  private moveCursor(seq: number | undefined): void {
    this.seq = seq;
    if (this.endpoint === "events") {
      this.eventId = seq === undefined ? undefined : withCompositeCursorValue(this.eventId, "c", seq);
    }
  }

  /**
   * Pages `/v1/records?order=id` with the stream's record filters, yielding each page, and returns the
   * snapshot's `watermarkSeq`.
   */
  private async *snapshot(cause: "reset" | "resync"): AsyncGenerator<StreamSignal, number | null, undefined> {
    const query: Query = { ...this.snapshotQuery(), order: "id", limit: this.snapshotPageSize };
    let cursor: string | undefined;
    let watermark: number | null = null;
    do {
      const page = await this.transport.request<{
        items?: SnapshotRecord[]; nextCursor?: string | null; watermarkSeq?: number; asOfSeq?: number | null;
      }>("GET", "/v1/records", { query: cursor ? { ...query, cursor } : query, signal: this.stopController.signal });
      if (watermark === null) {
        const header = page.response.headers.get("Foresignal-Watermark-Seq");
        watermark = page.data.watermarkSeq ?? page.data.asOfSeq ?? (header ? Number(header) : null);
      }
      const items = page.data.items ?? [];
      for (const item of items) {
        this.guard.observe(ObservedAtGuard.key(item.source, item.externalId), item.observedAt);
      }
      cursor = page.data.nextCursor ?? undefined;
      yield { type: "snapshot", cause, items, watermarkSeq: watermark, done: !cursor };
    } while (cursor && !this.closed);
    return watermark;
  }

  /** The snapshot query: the override, or the record filters of the stream. */
  private snapshotQuery(): Query {
    if (this.snapshotQueryOverride) {
      return { ...this.snapshotQueryOverride };
    }
    const out: Query = {};
    for (const [key, value] of Object.entries(this.filters)) {
      const name = this.endpoint === "events" ? (key.startsWith("changes.") ? key.slice(8) : undefined) : key;
      if (name !== undefined && SNAPSHOT_KEYS.has(name)) {
        out[name] = value;
      }
    }
    return out;
  }

  /** Opens one connection (minting or reusing a ticket when configured). */
  private async connect(): Promise<{ response: Response; url: string }> {
    for (let ticketAttempt = 0; ; ticketAttempt++) {
      const query: Query = { ...this.filters };
      const headers: Record<string, string> = { Accept: "text/event-stream", "Cache-Control": "no-cache" };
      const path = this.endpoint === "events" ? "/v1/events/stream" : "/v1/changes/stream";
      if (this.endpoint === "events") {
        if (this.eventId) {
          if (this.useTicket) {
            query["lastEventId"] = this.eventId;
          } else {
            headers["Last-Event-ID"] = this.eventId;
          }
        }
      } else if (this.seq !== undefined) {
        if (this.useTicket) {
          query["after"] = this.seq;
        } else {
          headers["Last-Event-ID"] = String(this.seq);
        }
      }
      if (this.useTicket) {
        query["ticket"] = await this.ensureTicket();
      }
      this.connection = new AbortController();
      const response = await this.transport.fetch("GET", path, {
        query, headers, auth: !this.useTicket, signal: this.connection.signal,
      });
      if (response.ok && response.body) {
        return { response, url: this.transport.url(path, { ...query, ticket: undefined }) };
      }
      if (response.status === 401 && this.useTicket && ticketAttempt === 0) {
        // The ticket expired (5 min after the last disconnect, 24 h in total): mint a new one once.
        await response.body?.cancel();
        this.ticket = undefined;
        continue;
      }
      throw await apiErrorFrom(response);
    }
  }

  /** The current stream ticket, minted on first use. */
  private async ensureTicket(): Promise<string> {
    if (!this.ticket) {
      const { data } = await this.transport.postJson<{ ticket: string }>("/v1/stream-tickets", { purpose: "stream" });
      this.ticket = data.ticket;
    }
    return this.ticket;
  }

  /** Reads one chunk, failing with {@link HeartbeatTimeoutError} after the watchdog timeout. */
  private read(reader: ReadableStreamDefaultReader<Uint8Array>): Promise<ReadableStreamReadResult<Uint8Array>> {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        reject(new HeartbeatTimeoutError(this.watchdogMs));
        this.connection?.abort();
      }, this.watchdogMs);
      reader.read().then(
        (result) => {
          clearTimeout(timer);
          resolve(result);
        },
        (error: unknown) => {
          clearTimeout(timer);
          reject(error);
        },
      );
    });
  }

  /** True for failures worth a reconnect: network errors, 408, 425, 429 and 5xx. */
  private retryable(error: unknown): boolean {
    if (error instanceof ApiError) {
      return error.status === 408 || error.status === 425 || error.status === 429 || error.status >= 500;
    }
    return true;
  }

  /** Yields a `reconnecting` signal and sleeps. */
  private async *pause(delayMs: number, reason: string): AsyncGenerator<StreamSignal, void, undefined> {
    yield { type: "reconnecting", attempt: this.backoff.attempt, delayMs, reason };
    await this.sleepFn(delayMs, this.stopController.signal);
  }
}

/** A short description of a failure for `reconnecting` signals. */
function describe(error: unknown): string {
  if (error instanceof Error) {
    return `${error.name}: ${error.message}`;
  }
  return String(error);
}
