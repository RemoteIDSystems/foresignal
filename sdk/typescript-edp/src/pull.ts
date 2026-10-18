/**
 * Pull-server helper: a framework-neutral handler for `GET {itemsUrl}?cursor=&limit=` and the optional
 * `GET {searchUrl}?q=&since=&until=&bbox=&lang=&limit=` (EDP §4.2, §4.3).
 *
 * The handler takes a request description and returns a {@link HttpResponse}, so it fits any
 * framework; {@link toNodeListener} adapts it to `node:http` (and Express or Connect), and
 * {@link toFetchHandler} to `(Request) => Response` runtimes (Deno, Bun, Hono, workers).
 *
 * **Cursor semantics.** Foresignal follows `nextCursor` until it is `null`, storing the cursor after
 * each page, and its next run starts again from the last cursor it sent. So a cursor must stay valid
 * and replayable. Positions are whatever your {@link ItemSource} understands; a {@link CursorStore}
 * turns them into opaque strings and back. {@link OpaqueCursorStore} (the default) is stateless and
 * survives restarts; {@link MemoryCursorStore} keeps short random tokens in memory only.
 *
 * **Pages** never exceed 5 MB: when a page would, the handler asks the source again with half the
 * limit. Changed health on a {@link StatusBoard} rides at the front of the next page.
 */
import { createHmac, randomBytes, timingSafeEqual } from "node:crypto";
import type { IncomingMessage, ServerResponse } from "node:http";
import { CursorError } from "./errors.ts";
import type { StatusBoard } from "./health.ts";
import { batch as makeBatch, newSnapshotId } from "./items.ts";
import type { JsonObject, Snapshot, StatusItem } from "./items.ts";
import { dumps } from "./ndjson.ts";
import {
  DEFAULT_PULL_LIMIT, DEFAULT_SEARCH_LIMIT, JSON_MEDIA_TYPE, MAX_BATCH_BYTES, MAX_ITEMS, PROTOCOL_VERSION,
  WELL_KNOWN_MANIFEST_PATH,
} from "./protocol.ts";
import { parseRfc3339 } from "./times.ts";

/** A framework-neutral HTTP response. */
export interface HttpResponse {
  /** The status code. */
  status: number;
  /** Response headers. */
  headers: Record<string, string>;
  /** The body. */
  body: Uint8Array;
}

/** Query values: a string, a list (first value used) or absent. */
export type Query = Record<string, string | readonly string[] | undefined> | URLSearchParams;
/** Request headers (any case). */
export type HeaderMap = Record<string, string | readonly string[] | undefined>;
/** Checks request headers. */
export type Authorizer = (headers: HeaderMap) => boolean;

/** What an {@link ItemSource} returns. */
export interface Page {
  /** The items. */
  items: JsonObject[];
  /** The position of the next page; null or undefined when caught up (`nextCursor` null). */
  nextPosition?: unknown;
  /** An optional batch `snapshot` object. */
  snapshot?: Snapshot | null;
}

/** Produces pages of items from a position (`null` for the beginning). */
export interface ItemSource {
  /**
   * Returns at most `limit` items starting at `position`. Throw {@link CursorError} for a position it
   * cannot serve. Must be repeatable: the same position may be asked for again.
   */
  fetch(position: unknown, limit: number): Page | Promise<Page>;
}

/** Turns source positions into opaque cursor strings and back. */
export interface CursorStore {
  /** Returns the cursor of `position` (JSON-serializable). */
  encode(position: unknown): string | Promise<string>;
  /** Returns the position of `cursor`; throws {@link CursorError} when unknown or invalid. */
  decode(cursor: string): unknown;
}

function b64url(data: Buffer | Uint8Array): string {
  return Buffer.from(data).toString("base64url");
}

function stableStringify(value: unknown): string {
  if (Array.isArray(value)) return `[${value.map(stableStringify).join(",")}]`;
  if (value !== null && typeof value === "object") {
    const entries = Object.entries(value).sort(([a], [b]) => (a < b ? -1 : a > b ? 1 : 0));
    return `{${entries.map(([k, v]) => `${JSON.stringify(k)}:${stableStringify(v)}`).join(",")}}`;
  }
  return JSON.stringify(value) ?? "null";
}

/**
 * A stateless store: the cursor is the position as base64url JSON, signed with HMAC-SHA256 when a
 * `secret` is given (so a caller cannot forge positions). Survives restarts.
 */
export class OpaqueCursorStore implements CursorStore {
  private readonly secret: Buffer | null;

  /** Creates the store; `secret` enables signing. */
  constructor(secret?: string | Uint8Array) {
    this.secret = secret === undefined ? null : Buffer.from(secret);
  }

  private sign(payload: string): string {
    return b64url(createHmac("sha256", this.secret as Buffer).update(payload).digest());
  }

  /** Encodes `position`. */
  encode(position: unknown): string {
    const payload = b64url(Buffer.from(stableStringify(position), "utf8"));
    return this.secret ? `${payload}.${this.sign(payload)}` : payload;
  }

  /** Decodes `cursor`; throws {@link CursorError} when malformed or wrongly signed. */
  decode(cursor: string): unknown {
    let payload = cursor;
    if (this.secret) {
      const dot = cursor.indexOf(".");
      const signature = dot < 0 ? "" : cursor.slice(dot + 1);
      payload = dot < 0 ? cursor : cursor.slice(0, dot);
      const expected = Buffer.from(this.sign(payload));
      const given = Buffer.from(signature);
      if (given.length !== expected.length || !timingSafeEqual(given, expected)) {
        throw new CursorError("The cursor's signature does not match");
      }
    }
    if (!/^[A-Za-z0-9_-]+$/.test(payload)) throw new CursorError("The cursor is not valid");
    try {
      return JSON.parse(Buffer.from(payload, "base64url").toString("utf8")) as unknown;
    } catch {
      throw new CursorError("The cursor is not valid");
    }
  }
}

/**
 * Random tokens kept in memory; the same position always gets the same token. Holds at most
 * `maxEntries` positions, forgetting the least recently used. Tokens do not survive a restart.
 */
export class MemoryCursorStore implements CursorStore {
  private readonly byToken = new Map<string, string>();
  private readonly byPosition = new Map<string, string>();
  private readonly maxEntries: number;

  /** Creates the store. */
  constructor(maxEntries = 100_000) {
    this.maxEntries = maxEntries;
  }

  /** Returns the token of `position`, creating one when needed. */
  encode(position: unknown): string {
    const key = stableStringify(position);
    let token = this.byPosition.get(key);
    if (token === undefined) {
      token = b64url(randomBytes(15));
      this.byPosition.set(key, token);
    }
    this.byToken.delete(token);
    this.byToken.set(token, key);
    while (this.byToken.size > this.maxEntries) {
      const [oldest, oldKey] = this.byToken.entries().next().value as [string, string];
      this.byToken.delete(oldest);
      this.byPosition.delete(oldKey);
    }
    return token;
  }

  /** Returns the position of `cursor`; throws {@link CursorError} when unknown. */
  decode(cursor: string): unknown {
    const key = this.byToken.get(cursor);
    if (key === undefined) throw new CursorError("The cursor is unknown or has expired");
    this.byToken.delete(cursor);
    this.byToken.set(cursor, key);
    return JSON.parse(key) as unknown;
  }
}

/**
 * Serves a sequence by offset, like the reference `csv_provider.py`: a full page gets the next offset
 * as its cursor, a short page `null`; asking again for the last cursor returns that page's items
 * plus anything appended since.
 */
export class SequenceSource implements ItemSource {
  private readonly load: () => readonly JsonObject[] | Promise<readonly JsonObject[]>;

  /** `load` returns the current items, in a stable order where new items are appended. */
  constructor(load: () => readonly JsonObject[] | Promise<readonly JsonObject[]>) {
    this.load = load;
  }

  /** Returns the items at offset `position` (0 when null). */
  async fetch(position: unknown, limit: number): Promise<Page> {
    const offset = position === null || position === undefined ? 0 : position;
    if (typeof offset !== "number" || !Number.isInteger(offset) || offset < 0) {
      throw new CursorError("The cursor does not name an offset");
    }
    const rows = await this.load();
    const page = rows.slice(offset, offset + limit).map((r) => ({ ...r }));
    return { items: page, nextPosition: page.length === limit ? offset + page.length : null };
  }
}

/** Options of {@link SnapshotSource}. */
export interface SnapshotSourceOptions {
  /** The scope value this snapshot covers (null: the whole stream). */
  scope?: string | null;
  /** A snapshot older than this restarts (default 25 minutes, under Foresignal's 30-minute window). */
  windowMs?: number;
  /** The clock in epoch milliseconds. */
  now?: () => number;
  /** Creates snapshot ids. */
  idFactory?: () => string;
  /** How many finished snapshot ids to remember (default 1,024). */
  remember?: number;
}

interface SnapshotPosition { s: string; p: number; a: string | null; t: number }

function isSnapshotPosition(value: unknown): value is SnapshotPosition {
  if (value === null || typeof value !== "object") return false;
  const v = value as Record<string, unknown>;
  return typeof v["s"] === "string" && typeof v["t"] === "number" && typeof v["p"] === "number";
}

/**
 * Pages a full snapshot of one scope of a snapshot stream (EDP §3.2). A run without a cursor (or with
 * the cursor of a finished snapshot) starts a new snapshot with a fresh id; pages are ordered by item
 * id with keyset paging, share the id, count `page` from 1, and the last has `final: true` and a null
 * `nextCursor`. A cursor of a snapshot older than `windowMs` starts a new one.
 */
export class SnapshotSource implements ItemSource {
  /** The snapshot stream. */
  readonly stream: string;
  /** The scope value, or null. */
  readonly scope: string | null;
  private readonly windowMs: number;
  private readonly now: () => number;
  private readonly idFactory: () => string;
  private readonly remember: number;
  private readonly finished = new Set<string>();
  private readonly listItems: () => Iterable<JsonObject> | Promise<Iterable<JsonObject>>;

  /** `listItems` returns the scope's complete current set of items of `stream`. */
  constructor(stream: string, listItems: () => Iterable<JsonObject> | Promise<Iterable<JsonObject>>,
    options: SnapshotSourceOptions = {}) {
    this.stream = stream;
    this.listItems = listItems;
    this.scope = options.scope ?? null;
    this.windowMs = options.windowMs ?? 25 * 60 * 1000;
    this.now = options.now ?? Date.now;
    this.idFactory = options.idFactory ?? newSnapshotId;
    this.remember = options.remember ?? 1024;
  }

  private finish(id: string): void {
    this.finished.add(id);
    while (this.finished.size > this.remember) {
      const oldest = this.finished.values().next().value as string;
      this.finished.delete(oldest);
    }
  }

  /** Returns the next page of the current snapshot, or the first page of a new one. */
  async fetch(position: unknown, limit: number): Promise<Page> {
    const now = this.now();
    const fresh = !isSnapshotPosition(position) || this.finished.has(position.s) || now - position.t > this.windowMs;
    const state: SnapshotPosition = fresh || !isSnapshotPosition(position)
      ? { s: this.idFactory(), p: 1, a: null, t: now }
      : position;
    const items: JsonObject[] = [];
    for (const item of await this.listItems()) {
      if (item["stream"] !== this.stream) {
        throw new RangeError(`SnapshotSource(${this.stream}) got an item of stream ${String(item["stream"])}`);
      }
      items.push({ ...item });
    }
    const key = (i: JsonObject): string => String(i["id"]);
    items.sort((x, y) => (key(x) < key(y) ? -1 : key(x) > key(y) ? 1 : 0));
    const remaining = state.a === null ? items : items.filter((i) => key(i) > (state.a as string));
    const chunk = remaining.slice(0, limit);
    const final = remaining.length <= limit;
    const snapshot: Snapshot = { id: state.s, stream: this.stream, scope: this.scope, page: state.p, final };
    if (final) {
      this.finish(state.s);
      return { items: chunk, nextPosition: null, snapshot };
    }
    const last = chunk[chunk.length - 1] as JsonObject;
    return { items: chunk, nextPosition: { s: state.s, p: state.p + 1, a: key(last), t: state.t }, snapshot };
  }
}

/** A parsed search request. */
export interface SearchQuery {
  /** The query text. */
  q: string;
  /** Start time (epoch ms), when given. */
  since: number | null;
  /** End time (epoch ms), when given. */
  until: number | null;
  /** minLon, minLat, maxLon, maxLat, when given. */
  bbox: [number, number, number, number] | null;
  /** BCP 47 language tags. */
  lang: string[];
  /** The maximum number of items. */
  limit: number;
}

/** Answers a search. */
export type SearchFunction = (query: SearchQuery) => readonly JsonObject[] | Page | Promise<readonly JsonObject[] | Page>;

function header(headers: HeaderMap | undefined, name: string): string {
  if (!headers) return "";
  const wanted = name.toLowerCase();
  for (const [key, value] of Object.entries(headers)) {
    if (key.toLowerCase() === wanted) return (Array.isArray(value) ? value[0] : value) ?? "";
  }
  return "";
}

function safeEqual(a: string, b: string): boolean {
  const x = Buffer.from(a);
  const y = Buffer.from(b);
  return x.length === y.length && timingSafeEqual(x, y);
}

/** An authorizer for `auth: bearer`: `Authorization: Bearer {token}`. */
export function bearerAuth(token: string): Authorizer {
  return (headers) => {
    const value = header(headers, "authorization");
    const space = value.indexOf(" ");
    return space > 0 && value.slice(0, space).toLowerCase() === "bearer" && safeEqual(value.slice(space + 1).trim(), token);
  };
}

/** An authorizer for `auth: header`: header `name` equal to `value`. */
export function headerAuth(name: string, value: string): Authorizer {
  return (headers) => safeEqual(header(headers, name), value);
}

/** An authorizer for `auth: basic`. */
export function basicAuth(username: string, password: string): Authorizer {
  return (headers) => {
    const value = header(headers, "authorization");
    const space = value.indexOf(" ");
    if (space <= 0 || value.slice(0, space).toLowerCase() !== "basic") return false;
    const credential = value.slice(space + 1).trim();
    if (!/^[A-Za-z0-9+/]+={0,2}$/.test(credential)) return false;
    return safeEqual(Buffer.from(credential, "base64").toString("utf8"), `${username}:${password}`);
  };
}

function first(query: Query | undefined, name: string): string | null {
  if (!query) return null;
  if (query instanceof URLSearchParams) return query.get(name);
  const value = query[name];
  if (value === undefined) return null;
  return typeof value === "string" ? value : value[0] ?? null;
}

const STATUS_TEXT: Record<number, string> = { 200: "OK", 400: "Bad Request", 401: "Unauthorized", 404: "Not Found",
  405: "Method Not Allowed", 500: "Internal Server Error" };

function problem(status: number, code: string, detail: string, extra: Record<string, string> = {}): HttpResponse {
  return { status, headers: { "Content-Type": "application/problem+json", "Cache-Control": "no-store", ...extra },
    body: dumps({ type: "about:blank", title: STATUS_TEXT[status] ?? "Error", status, code, detail }) };
}

function json(body: Uint8Array): HttpResponse {
  return { status: 200, headers: { "Content-Type": JSON_MEDIA_TYPE, "Cache-Control": "no-store" }, body };
}

/** Options of {@link PullHandler}. */
export interface PullHandlerOptions {
  /** Turns positions into cursors (default: an unsigned {@link OpaqueCursorStore}). */
  cursorStore?: CursorStore;
  /** Written as the batches' `sourceId` when given (the provider's `source.id`). */
  sourceId?: string;
  /** The protocol string written. */
  protocol?: string;
  /** Page size when the request has no `limit` (Foresignal sends 500). */
  defaultLimit?: number;
  /** The largest page size honored (at most 10,000). */
  maxLimit?: number;
  /** The largest response body (at most 5 MB). */
  maxBytes?: number;
  /** Answers search requests; without it search answers 404. */
  search?: SearchFunction;
  /** Checks request headers; without it everyone is admitted. */
  authorize?: Authorizer;
  /** Health whose changes ride on the next page. */
  status?: StatusBoard;
  /** The manifest served at `manifestPath`. */
  manifest?: JsonObject;
  /** The items route (default `/edp/items`). */
  itemsPath?: string;
  /** The search route (default `/edp/search`). */
  searchPath?: string;
  /** The manifest route (default `/.well-known/foresignal-edp.json`). */
  manifestPath?: string;
  /** The clock for `sentAt`. */
  now?: () => Date;
  /** Receives source failures (default `console.error`). */
  onError?: (error: unknown) => void;
}

/** A request for {@link PullHandler.handle}. */
export interface PullRequest {
  /** The HTTP method. */
  method: string;
  /** The path, optionally with a query string. */
  path: string;
  /** Extra query values (merged over the path's). */
  query?: Query;
  /** Request headers. */
  headers?: HeaderMap;
}

/** Serves an EDP items endpoint (and optionally search and the manifest). */
export class PullHandler {
  /** The items source. */
  readonly source: ItemSource;
  /** The cursor store. */
  readonly cursorStore: CursorStore;
  private readonly options: PullHandlerOptions;
  private readonly defaultLimit: number;
  private readonly maxLimit: number;
  private readonly maxBytes: number;

  /** Creates the handler; throws `RangeError` for limits outside the protocol's. */
  constructor(source: ItemSource, options: PullHandlerOptions = {}) {
    const maxLimit = options.maxLimit ?? MAX_ITEMS;
    const maxBytes = options.maxBytes ?? MAX_BATCH_BYTES;
    if (!Number.isInteger(maxLimit) || maxLimit < 1 || maxLimit > MAX_ITEMS) throw new RangeError(`maxLimit must be 1 to ${MAX_ITEMS}`);
    if (!Number.isInteger(maxBytes) || maxBytes < 1 || maxBytes > MAX_BATCH_BYTES) {
      throw new RangeError(`maxBytes must be 1 to ${MAX_BATCH_BYTES}`);
    }
    this.source = source;
    this.cursorStore = options.cursorStore ?? new OpaqueCursorStore();
    this.options = options;
    this.maxLimit = maxLimit;
    this.maxBytes = maxBytes;
    this.defaultLimit = Math.min(options.defaultLimit ?? DEFAULT_PULL_LIMIT, maxLimit);
  }

  private limit(query: Query | undefined, fallback: number): number | HttpResponse {
    const raw = first(query, "limit");
    if (raw === null || raw === "") return fallback;
    if (!/^-?\d+$/.test(raw.trim())) return problem(400, "LIMIT_INVALID", "limit must be an integer");
    return Math.max(1, Math.min(Number(raw), this.maxLimit));
  }

  private unauthorized(headers: HeaderMap | undefined): HttpResponse | null {
    if (this.options.authorize && !this.options.authorize(headers ?? {})) {
      return problem(401, "UNAUTHORIZED", "missing or invalid credentials");
    }
    return null;
  }

  private encodeBatch(items: readonly unknown[], nextCursor: string | null, snapshot: Snapshot | null | undefined): Uint8Array {
    const opts: Parameters<typeof makeBatch>[1] = { protocol: this.options.protocol ?? PROTOCOL_VERSION,
      sentAt: (this.options.now ?? (() => new Date()))(), snapshot: snapshot ?? null, nextCursor, includeNextCursor: true };
    if (this.options.sourceId !== undefined) opts.sourceId = this.options.sourceId;
    return dumps(makeBatch(items, opts));
  }

  private requeue(statuses: StatusItem[] | null): void {
    if (this.options.status && statuses && statuses.length > 0) this.options.status.requeue(statuses);
  }

  /** Answers `GET {itemsUrl}?cursor=&limit=` with a batch carrying `nextCursor`. */
  async handleItems(query?: Query, headers?: HeaderMap): Promise<HttpResponse> {
    const denied = this.unauthorized(headers);
    if (denied) return denied;
    let limit = this.limit(query, this.defaultLimit);
    if (typeof limit !== "number") return limit;
    const cursor = first(query, "cursor") || null;
    let position: unknown = null;
    try {
      if (cursor !== null) position = await this.cursorStore.decode(cursor);
    } catch (e) {
      return problem(400, "CURSOR_INVALID", (e as Error).message);
    }
    let statuses: StatusItem[] | null = null;
    try {
      for (;;) {
        const page = await this.source.fetch(position, limit);
        // Drained after the first fetch, so health the source reports while loading rides along.
        statuses ??= this.options.status ? this.options.status.drain() : [];
        const next = page.nextPosition === null || page.nextPosition === undefined
          ? null : await this.cursorStore.encode(page.nextPosition);
        const body = this.encodeBatch([...statuses, ...page.items], next, page.snapshot);
        if (body.length <= this.maxBytes) return json(body);
        if (limit === 1) break;
        limit = Math.max(1, Math.floor(limit / 2));
      }
    } catch (e) {
      this.requeue(statuses);
      if (e instanceof CursorError) return problem(400, "CURSOR_INVALID", e.message);
      (this.options.onError ?? console.error)(e);
      return problem(500, "PROVIDER_ERROR", `the item source failed: ${(e as Error)?.name ?? "Error"}`);
    }
    this.requeue(statuses);
    return problem(500, "PAGE_TOO_LARGE", `a single item does not fit in ${this.maxBytes} bytes`);
  }

  /** Answers `GET {searchUrl}?q=&since=&until=&bbox=&lang=&limit=` with a batch (`nextCursor` null). */
  async handleSearch(query?: Query, headers?: HeaderMap): Promise<HttpResponse> {
    const search = this.options.search;
    if (!search) return problem(404, "NOT_FOUND", "this provider has no search endpoint");
    const denied = this.unauthorized(headers);
    if (denied) return denied;
    const q = (first(query, "q") ?? "").trim();
    if (q === "") return problem(400, "QUERY_REQUIRED", "q is required");
    const times: Record<string, number | null> = {};
    for (const name of ["since", "until"]) {
      const raw = first(query, name);
      times[name] = raw ? parseRfc3339(raw) : null;
      if (raw && times[name] === null) return problem(400, "TIME_INVALID", `${name} must be an RFC 3339 date-time with an offset`);
    }
    let bbox: [number, number, number, number] | null = null;
    const rawBbox = first(query, "bbox");
    if (rawBbox) {
      const values = rawBbox.split(",").map((v) => (v.trim() === "" ? Number.NaN : Number(v)));
      const [minLon, minLat, maxLon, maxLat] = values as [number, number, number, number];
      if (values.length !== 4 || values.some((v) => !Number.isFinite(v)) || !(-180 <= minLon && minLon <= maxLon && maxLon <= 180)
        || !(-90 <= minLat && minLat <= maxLat && maxLat <= 90)) {
        return problem(400, "BBOX_INVALID", "bbox must be minLon,minLat,maxLon,maxLat in WGS84");
      }
      bbox = [minLon, minLat, maxLon, maxLat];
    }
    const lang = (first(query, "lang") ?? "").split(",").map((v) => v.trim()).filter((v) => v !== "");
    const limit = this.limit(query, Math.min(DEFAULT_SEARCH_LIMIT, this.maxLimit));
    if (typeof limit !== "number") return limit;
    let found: readonly JsonObject[] | Page;
    try {
      found = await search({ q, since: times["since"] ?? null, until: times["until"] ?? null, bbox, lang, limit });
    } catch (e) {
      (this.options.onError ?? console.error)(e);
      return problem(500, "PROVIDER_ERROR", `the search failed: ${(e as Error)?.name ?? "Error"}`);
    }
    let items = (Array.isArray(found) ? found : (found as Page).items).slice(0, limit);
    for (;;) {
      const body = this.encodeBatch(items, null, null);
      if (body.length <= this.maxBytes || items.length === 0) return json(body);
      items = items.slice(0, Math.floor(items.length / 2));
    }
  }

  /** Answers the manifest URL (404 when no manifest was given). */
  handleManifest(): HttpResponse {
    if (!this.options.manifest) return problem(404, "NOT_FOUND", "no manifest is published here");
    return json(dumps(this.options.manifest));
  }

  /** Routes a request by path (a query string in `path` is merged under `query`). */
  async handle(request: PullRequest): Promise<HttpResponse> {
    const url = new URL(request.path, "http://provider.invalid");
    const merged: Record<string, string> = Object.fromEntries(url.searchParams.entries());
    if (request.query instanceof URLSearchParams) {
      for (const [k, v] of request.query.entries()) merged[k] = v;
    } else if (request.query) {
      for (const [k, v] of Object.entries(request.query)) {
        const value = typeof v === "string" ? v : v?.[0];
        if (value !== undefined) merged[k] = value;
      }
    }
    const route = url.pathname;
    const itemsPath = this.options.itemsPath ?? "/edp/items";
    const searchPath = this.options.searchPath ?? "/edp/search";
    const manifestPath = this.options.manifestPath ?? WELL_KNOWN_MANIFEST_PATH;
    if (![itemsPath, searchPath, manifestPath].includes(route)) return problem(404, "NOT_FOUND", `no route ${route}`);
    const method = request.method.toUpperCase();
    if (method !== "GET" && method !== "HEAD") {
      return problem(405, "METHOD_NOT_ALLOWED", "only GET is supported", { Allow: "GET, HEAD" });
    }
    const response = route === itemsPath ? await this.handleItems(merged, request.headers)
      : route === searchPath ? await this.handleSearch(merged, request.headers)
        : this.handleManifest();
    return method === "HEAD" ? { ...response, body: new Uint8Array() } : response;
  }
}

/** Adapts a {@link PullHandler} to a `node:http` request listener (also usable as Express middleware). */
export function toNodeListener(handler: PullHandler): (req: IncomingMessage, res: ServerResponse) => void {
  return (req, res) => {
    handler.handle({ method: req.method ?? "GET", path: req.url ?? "/", headers: req.headers as HeaderMap })
      .then((response) => {
        res.writeHead(response.status, { ...response.headers, "Content-Length": String(response.body.length) });
        res.end(response.body);
      })
      .catch((error: unknown) => {
        res.writeHead(500, { "Content-Type": "text/plain" });
        res.end(String(error));
      });
  };
}

/** Adapts a {@link PullHandler} to a `(Request) => Promise<Response>` handler (Deno, Bun, Hono, workers). */
export function toFetchHandler(handler: PullHandler): (request: Request) => Promise<Response> {
  return async (request) => {
    const url = new URL(request.url);
    const headers: HeaderMap = {};
    request.headers.forEach((value, key) => { headers[key] = value; });
    const response = await handler.handle({ method: request.method, path: `${url.pathname}${url.search}`, headers });
    return new Response(response.body.length > 0 ? response.body : null, { status: response.status, headers: response.headers });
  };
}
