/**
 * Push client: `POST /v1/external/sources/{sourceId}/items` with the source's ingest key (EDP §4.1).
 *
 * - **Batching:** at most 10,000 items and 5 MB per batch (both configurable downward), counted on
 *   the exact bytes sent.
 * - **NDJSON:** with `ndjson: true` each batch is a header line plus one line per item.
 * - **Retries:** `429` and `503` (and network failures) are retried with exponential backoff and
 *   jitter (1 s doubling to 60 s by default), honoring `Retry-After` (seconds or an HTTP date).
 * - **Idempotent re-sends:** every batch is serialized once with a fixed `sentAt`, and every retry
 *   sends the same bytes. Foresignal hashes item content, so a repeated item becomes a touch, never a
 *   duplicate. A failed push throws {@link PushError} carrying the batches not yet accepted;
 *   {@link PushClient.sendAll} re-sends them unchanged later.
 * - **Snapshots:** {@link PushClient.pushSnapshot} pages a full snapshot of one scope (EDP §3.2).
 * - **Status:** {@link PushClient.reportStatus} sends source or stream health.
 */
import { EdpError, ItemTooLargeError } from "./errors.ts";
import type { ItemRejection } from "./errors.ts";
import { newSnapshotId, status as statusItem } from "./items.ts";
import { dumps } from "./ndjson.ts";
import {
  INGEST_KEY_HEADER, JSON_MEDIA_TYPE, MAX_BATCH_BYTES, MAX_ITEMS, NDJSON_MEDIA_TYPE, PROTOCOL_VERSION, VERSION,
} from "./protocol.ts";
import type { Health } from "./protocol.ts";
import { formatTime } from "./times.ts";
import type { TimeLike } from "./times.ts";

/** The `User-Agent` of pushes. */
export const USER_AGENT = `foresignal-edp-typescript/${VERSION}`;

const encoder = new TextEncoder();

/** A fetch-compatible function (the global `fetch` by default). */
export type FetchLike = (input: string, init: { method: string; headers: Record<string, string>;
  body?: Uint8Array | string; signal?: AbortSignal }) => Promise<{ status: number; headers: { get(name: string): string | null };
  arrayBuffer(): Promise<ArrayBuffer> }>;

/** One serialized batch, ready to send and re-send byte for byte. */
export interface PreparedBatch {
  /** The exact body. */
  body: Uint8Array;
  /** `application/json` or `application/x-ndjson`. */
  contentType: string;
  /** Items in the batch. */
  itemCount: number;
  /** Position of its first item in the push call (maps rejection indexes back). */
  firstIndex: number;
  /** The snapshot page object, when it is one. */
  snapshot: Record<string, unknown> | null;
}

/** The merged outcome of one or more batches. */
export interface PushResult {
  /** Accepted items. */
  accepted: number;
  /** Refused items, indexes into the pushed sequence. */
  rejected: ItemRejection[];
  /** Batches sent. */
  batches: number;
  /** Each batch's parsed response body (`EdpIngestResult`). */
  responses: Record<string, unknown>[];
  /** True when no item was refused. */
  ok: boolean;
}

/** Creates an empty {@link PushResult}. */
export function emptyResult(): PushResult {
  return { accepted: 0, rejected: [], batches: 0, responses: [], ok: true };
}

function addResponse(result: PushResult, response: Record<string, unknown>, firstIndex: number): void {
  result.batches++;
  result.responses.push(response);
  const accepted = response["accepted"];
  if (typeof accepted === "number") result.accepted += accepted;
  const rejected = response["rejected"];
  if (Array.isArray(rejected)) {
    for (const r of rejected) {
      if (r === null || typeof r !== "object") continue;
      const entry = r as Record<string, unknown>;
      result.rejected.push({
        index: Number(entry["index"] ?? 0) + firstIndex,
        stream: (entry["stream"] as string | null | undefined) ?? null,
        id: (entry["id"] as string | null | undefined) ?? null,
        reason: String(entry["reason"] ?? ""),
        schemaErrors: Array.isArray(entry["schemaErrors"])
          ? (entry["schemaErrors"] as Record<string, unknown>[]).map((e) => ({ path: String(e["path"] ?? ""),
            message: String(e["message"] ?? "") }))
          : [],
      });
    }
  }
  result.ok = result.rejected.length === 0;
}

/**
 * A push failed. `status` is the HTTP status (null for a network failure), `code` the ProblemDetail
 * `code`, `retryable` whether retrying later may succeed, `result` what was accepted before the
 * failure, and `remaining` the prepared batches not accepted (the failed one first).
 */
export class PushError extends EdpError {
  /** The HTTP status, or null for a network failure. */
  readonly status: number | null;
  /** The ProblemDetail `code`, when sent. */
  readonly code: string | null;
  /** The ProblemDetail body, when sent. */
  readonly problem: Record<string, unknown> | null;
  /** True when retrying later may succeed. */
  readonly retryable: boolean;
  /** What was accepted before the failure. */
  result: PushResult = emptyResult();
  /** The batches not accepted, the failed one first. */
  remaining: PreparedBatch[] = [];

  /** Creates the error. */
  constructor(message: string, options: { status?: number | null; code?: string | null;
    problem?: Record<string, unknown> | null; retryable?: boolean; cause?: unknown } = {}) {
    super(message);
    this.status = options.status ?? null;
    this.code = options.code ?? null;
    this.problem = options.problem ?? null;
    this.retryable = options.retryable ?? false;
    if (options.cause !== undefined) (this as { cause?: unknown }).cause = options.cause;
  }
}

/** Reads `Retry-After`: delta seconds or an HTTP date. Returns seconds (never negative) or null. */
export function parseRetryAfter(value: string | null | undefined, now: number = Date.now()): number | null {
  if (value === null || value === undefined || value.trim() === "") return null;
  const text = value.trim();
  if (/^-?\d+(\.\d+)?$/.test(text)) return Math.max(0, Number(text));
  const when = Date.parse(text);
  if (Number.isNaN(when)) return null;
  return Math.max(0, (when - now) / 1000);
}

function parseObject(body: Uint8Array): Record<string, unknown> | null {
  if (body.length === 0) return null;
  try {
    const value: unknown = JSON.parse(new TextDecoder().decode(body));
    return value !== null && typeof value === "object" && !Array.isArray(value) ? value as Record<string, unknown> : null;
  } catch {
    return null;
  }
}

function concat(parts: Uint8Array[]): Uint8Array {
  const total = parts.reduce((n, p) => n + p.length, 0);
  const out = new Uint8Array(total);
  let offset = 0;
  for (const part of parts) {
    out.set(part, offset);
    offset += part.length;
  }
  return out;
}

/** Options of {@link PushClient}. */
export interface PushClientOptions {
  /** The Foresignal instance, e.g. `https://foresignal.example`. */
  baseUrl: string;
  /** The registered source id, `ext-{source.id}`. */
  sourceId: string;
  /** The source's ingest key (`eik_…`), sent as `X-Foresignal-Ingest-Key`. */
  ingestKey: string;
  /** Send `application/x-ndjson` instead of one JSON object. */
  ndjson?: boolean;
  /** Items per batch (at most 10,000). */
  maxItems?: number;
  /** Bytes per batch (at most 5 MB). */
  maxBytes?: number;
  /** Retries of one batch after a 429, a 503 or a network failure (default 5). */
  maxRetries?: number;
  /** The first backoff in milliseconds (default 1,000; doubles per retry). */
  initialBackoffMs?: number;
  /** The backoff ceiling in milliseconds (default 60,000). */
  maxBackoffMs?: number;
  /** The longest `Retry-After` honored, in seconds (default 900); a longer one fails as retryable. */
  maxRetryAfterSeconds?: number;
  /** The per-request timeout in milliseconds (default 30,000). */
  timeoutMs?: number;
  /** Write `sourceId` into each batch (default true). */
  includeSourceId?: boolean;
  /** The protocol string written (default `foresignal-edp/1.0`). */
  protocol?: string;
  /** Statuses retried (default 429 and 503). */
  retryStatuses?: number[];
  /** The fetch implementation (default the global `fetch`). */
  fetch?: FetchLike;
  /** The sleep function (injectable for tests). */
  sleep?: (ms: number) => Promise<void>;
  /** A `[0, 1)` random source for jitter (injectable for tests). */
  random?: () => number;
  /** The clock for `sentAt` (injectable for tests). */
  now?: () => Date;
}

/** Pushes EDP items to one registered source. */
export class PushClient {
  /** The instance base URL without a trailing slash. */
  readonly baseUrl: string;
  /** The registered source id. */
  readonly sourceId: string;
  /** Whether batches are NDJSON. */
  readonly ndjson: boolean;
  /** Items per batch. */
  readonly maxItems: number;
  /** Bytes per batch. */
  readonly maxBytes: number;
  /** Retries per batch. */
  readonly maxRetries: number;
  /** The fetch implementation; replaceable (e.g. to re-send through another connection). */
  fetchImpl: FetchLike;
  private readonly ingestKey: string;
  private readonly initialBackoffMs: number;
  private readonly maxBackoffMs: number;
  private readonly maxRetryAfterSeconds: number;
  private readonly timeoutMs: number;
  private readonly includeSourceId: boolean;
  private readonly protocol: string;
  private readonly retryStatuses: Set<number>;
  private readonly sleep: (ms: number) => Promise<void>;
  private readonly random: () => number;
  private readonly now: () => Date;

  /** Creates the client; throws `RangeError` for limits outside the protocol's. */
  constructor(options: PushClientOptions) {
    const maxItems = options.maxItems ?? MAX_ITEMS;
    const maxBytes = options.maxBytes ?? MAX_BATCH_BYTES;
    if (!Number.isInteger(maxItems) || maxItems < 1 || maxItems > MAX_ITEMS) throw new RangeError(`maxItems must be 1 to ${MAX_ITEMS}`);
    if (!Number.isInteger(maxBytes) || maxBytes < 1 || maxBytes > MAX_BATCH_BYTES) {
      throw new RangeError(`maxBytes must be 1 to ${MAX_BATCH_BYTES}`);
    }
    if ((options.maxRetries ?? 5) < 0) throw new RangeError("maxRetries must not be negative");
    if (!options.sourceId) throw new RangeError("sourceId is required (the registered id, ext-{source.id})");
    this.baseUrl = options.baseUrl.replace(/\/+$/, "");
    this.sourceId = options.sourceId;
    this.ingestKey = options.ingestKey;
    this.ndjson = options.ndjson ?? false;
    this.maxItems = maxItems;
    this.maxBytes = maxBytes;
    this.maxRetries = options.maxRetries ?? 5;
    this.initialBackoffMs = options.initialBackoffMs ?? 1_000;
    this.maxBackoffMs = options.maxBackoffMs ?? 60_000;
    this.maxRetryAfterSeconds = options.maxRetryAfterSeconds ?? 900;
    this.timeoutMs = options.timeoutMs ?? 30_000;
    this.includeSourceId = options.includeSourceId ?? true;
    this.protocol = options.protocol ?? PROTOCOL_VERSION;
    this.retryStatuses = new Set(options.retryStatuses ?? [429, 503]);
    this.fetchImpl = options.fetch ?? (globalThis.fetch as unknown as FetchLike);
    this.sleep = options.sleep ?? ((ms) => new Promise((resolve) => setTimeout(resolve, ms)));
    this.random = options.random ?? Math.random;
    this.now = options.now ?? (() => new Date());
  }

  /** `{baseUrl}/v1/external/sources/{sourceId}/items`. */
  get itemsUrl(): string {
    return `${this.baseUrl}/v1/external/sources/${encodeURIComponent(this.sourceId)}/items`;
  }

  /** `{baseUrl}/v1/external/sources/{sourceId}/manifest`. */
  get manifestUrl(): string {
    return `${this.baseUrl}/v1/external/sources/${encodeURIComponent(this.sourceId)}/manifest`;
  }

  /** Hides the ingest key from logs. */
  toJSON(): Record<string, string> {
    return { baseUrl: this.baseUrl, sourceId: this.sourceId, ingestKey: "***" };
  }

  private header(sentAt: string, snapshot: Record<string, unknown> | null): Record<string, unknown> {
    const header: Record<string, unknown> = { protocol: this.protocol };
    if (this.includeSourceId) header["sourceId"] = this.sourceId;
    header["sentAt"] = sentAt;
    if (snapshot !== null) header["snapshot"] = snapshot;
    return header;
  }

  private encode(header: Record<string, unknown>, items: Uint8Array[]): Uint8Array {
    const newline = encoder.encode("\n");
    if (this.ndjson) {
      const parts: Uint8Array[] = [dumps(header)];
      for (const item of items) parts.push(newline, item);
      parts.push(newline);
      return concat(parts);
    }
    const head = dumps({ ...header, items: [] });
    const parts: Uint8Array[] = [head.subarray(0, head.length - 2)];
    items.forEach((item, i) => {
      if (i > 0) parts.push(encoder.encode(","));
      parts.push(item);
    });
    parts.push(encoder.encode("]}"));
    return concat(parts);
  }

  /**
   * Splits `items` into serialized batches within the item and byte limits. `snapshot`
   * (`{id, stream, scope}`) makes them the pages of one snapshot: `page` from 1 and the last
   * `final: true`; an empty snapshot is one empty final page.
   *
   * @throws ItemTooLargeError when one item alone does not fit in `maxBytes`
   */
  prepare(items: readonly unknown[], options: { snapshot?: { id: string; stream: string; scope?: string | null };
    sentAt?: TimeLike } = {}): PreparedBatch[] {
    const stamp = formatTime(options.sentAt ?? this.now());
    const snap = options.snapshot;
    const placeholder = snap ? { id: snap.id, stream: snap.stream, scope: snap.scope ?? null, page: 1e9, final: false } : null;
    const overhead = this.encode(this.header(stamp, placeholder), []).length + (this.ndjson ? 0 : -1);
    const groups: { first: number; items: Uint8Array[] }[] = [];
    let current: Uint8Array[] = [];
    let currentSize = overhead;
    let start = 0;
    items.forEach((item, index) => {
      const encoded = dumps(item);
      const needed = encoded.length + 1;
      if (overhead + needed > this.maxBytes) throw new ItemTooLargeError(index, overhead + needed, this.maxBytes);
      if (current.length > 0 && (current.length >= this.maxItems || currentSize + needed > this.maxBytes)) {
        groups.push({ first: start, items: current });
        start = index;
        current = [];
        currentSize = overhead;
      }
      current.push(encoded);
      currentSize += needed;
    });
    if (current.length > 0 || groups.length === 0) groups.push({ first: start, items: current });
    if (!snap && groups.length === 1 && groups[0]?.items.length === 0) return [];
    const contentType = this.ndjson ? NDJSON_MEDIA_TYPE : JSON_MEDIA_TYPE;
    return groups.map((group, i) => {
      const page = snap ? { id: snap.id, stream: snap.stream, scope: snap.scope ?? null, page: i + 1,
        final: i === groups.length - 1 } : null;
      return { body: this.encode(this.header(stamp, page), group.items), contentType, itemCount: group.items.length,
        firstIndex: group.first, snapshot: page };
    });
  }

  /** The delay in ms before retry `attempt` (0-based) without `Retry-After`: half to all of the capped exponential. */
  backoffMs(attempt: number): number {
    const ceiling = Math.min(this.maxBackoffMs, this.initialBackoffMs * 2 ** attempt);
    return ceiling / 2 + this.random() * (ceiling / 2);
  }

  private async request(method: string, url: string, body: Uint8Array, contentType: string): Promise<Record<string, unknown>> {
    const headers = { "Content-Type": contentType, Accept: "application/json, application/problem+json",
      [INGEST_KEY_HEADER]: this.ingestKey, "User-Agent": USER_AGENT };
    for (let attempt = 0; ; attempt++) {
      let delayMs: number;
      let response: Awaited<ReturnType<FetchLike>> | null = null;
      try {
        response = await this.fetchImpl(url, { method, headers, body, signal: AbortSignal.timeout(this.timeoutMs) });
      } catch (e) {
        if (attempt >= this.maxRetries) {
          throw new PushError(`Network failure after ${attempt + 1} attempts: ${(e as Error).message}`,
            { retryable: true, cause: e });
        }
        delayMs = this.backoffMs(attempt);
      }
      if (response !== null) {
        const bytes = new Uint8Array(await response.arrayBuffer());
        if (response.status >= 200 && response.status < 300) return parseObject(bytes) ?? {};
        const problem = parseObject(bytes);
        const code = typeof problem?.["code"] === "string" ? problem["code"] : null;
        const retryable = this.retryStatuses.has(response.status);
        if (!retryable || attempt >= this.maxRetries) {
          const detail = String(problem?.["detail"] ?? problem?.["title"] ?? "");
          throw new PushError(`${method} ${url} answered ${response.status}${code ? ` ${code}` : ""}${detail ? `: ${detail}` : ""}`,
            { status: response.status, code, problem, retryable });
        }
        const retryAfter = parseRetryAfter(response.headers.get("retry-after"));
        if (retryAfter !== null && retryAfter > this.maxRetryAfterSeconds) {
          throw new PushError(`${method} ${url} answered ${response.status} with Retry-After ${Math.round(retryAfter)} s, `
            + "longer than maxRetryAfterSeconds", { status: response.status, code, problem, retryable: true });
        }
        delayMs = retryAfter !== null ? retryAfter * 1000 : this.backoffMs(attempt);
      }
      await this.sleep(delayMs!);
    }
  }

  /** Sends one prepared batch (retrying as configured) and returns the parsed response. */
  send(prepared: PreparedBatch): Promise<Record<string, unknown>> {
    return this.request("POST", this.itemsUrl, prepared.body, prepared.contentType);
  }

  /** Sends prepared batches in order; use it to re-send `PushError.remaining` unchanged. */
  async sendAll(prepared: readonly PreparedBatch[]): Promise<PushResult> {
    const result = emptyResult();
    for (let i = 0; i < prepared.length; i++) {
      const batch = prepared[i] as PreparedBatch;
      let response: Record<string, unknown>;
      try {
        response = await this.send(batch);
      } catch (e) {
        if (e instanceof PushError) {
          e.result = result;
          e.remaining = prepared.slice(i);
        }
        throw e;
      }
      addResponse(result, response, batch.firstIndex);
    }
    return result;
  }

  /** Batches and pushes `items`; rejection indexes refer to positions in `items`. */
  push(items: readonly unknown[]): Promise<PushResult> {
    return this.sendAll(this.prepare(items));
  }

  /**
   * Pushes the full current set of one scope of a snapshot stream as the pages of one snapshot. If a
   * page fails, Foresignal discards the snapshot and deletes nothing, so it can be pushed again.
   */
  pushSnapshot(stream: string, items: readonly unknown[], options: { scope?: string | null; snapshotId?: string } = {}): Promise<PushResult> {
    return this.sendAll(this.prepare(items, { snapshot: { id: options.snapshotId ?? newSnapshotId(), stream,
      scope: options.scope ?? null } }));
  }

  /** Reports the source's health (or one stream's) with a `status` item. */
  reportStatus(health: Health, message?: string, options: { stream?: string } = {}): Promise<PushResult> {
    return this.push([statusItem(health, message, options)]);
  }

  /** `PUT /v1/external/sources/{id}/manifest` with the ingest key. */
  updateManifest(manifest: Record<string, unknown>): Promise<Record<string, unknown>> {
    return this.request("PUT", this.manifestUrl, dumps(manifest), JSON_MEDIA_TYPE);
  }
}
