/**
 * Cursor iterators over Foresignal's paged operations (async iterables). Every listing that takes `cursor`
 * answers `{items, nextCursor}` (two older ones say `next`); `GET /v1/changes` pages by seq (`after`, `lastSeq`,
 * `hasMore`) and refuses a cursor below the purge horizon with `410 RESYNC_REQUIRED`, surfaced as
 * {@link ResyncRequiredError}.
 */
import { HttpTransport, type Query, type TransportOptions } from "../http.js";

export { ResyncRequiredError } from "../errors.js";

/** One page of a cursor-paged listing. */
export interface CursorPage<T> {
  items: T[];
  /** The cursor of the next page; absent or null on the last page. */
  nextCursor?: string | null;
  /** The older spelling of {@link nextCursor}. */
  next?: string | null;
  [member: string]: unknown;
}

/** Options of the cursor iterators. */
export interface PaginateOptions {
  /** Page size, sent as `limit` (the operation's default when absent). */
  pageSize?: number;
  /** Stop after this many pages. */
  maxPages?: number;
  signal?: AbortSignal;
}

/** The next cursor of a page (`nextCursor`, else `next`). */
export function nextCursorOf(page: { nextCursor?: string | null; next?: string | null }): string | undefined {
  const cursor = page.nextCursor ?? page.next;
  return cursor === null || cursor === undefined || cursor === "" ? undefined : cursor;
}

/**
 * Iterates pages from any page fetcher, following the cursor until there is none. Use it with the generated
 * clients, e.g. `paginatePagesWith((cursor) => client.GET("/v1/alerts", {params: {query: {cursor}}}).then(unwrap))`.
 */
export async function* paginatePagesWith<P extends { nextCursor?: string | null; next?: string | null }>(
  fetchPage: (cursor: string | undefined) => Promise<P>,
  options: PaginateOptions = {},
): AsyncGenerator<P, void, undefined> {
  let cursor: string | undefined;
  let pages = 0;
  do {
    if (options.signal?.aborted) {
      return;
    }
    const page = await fetchPage(cursor);
    pages++;
    yield page;
    cursor = nextCursorOf(page);
  } while (cursor !== undefined && (options.maxPages === undefined || pages < options.maxPages));
}

/** Iterates the items of every page from any page fetcher. */
export async function* paginateWith<T>(
  fetchPage: (cursor: string | undefined) => Promise<CursorPage<T>>,
  options: PaginateOptions = {},
): AsyncGenerator<T, void, undefined> {
  for await (const page of paginatePagesWith(fetchPage, options)) {
    yield* page.items ?? [];
  }
}

/** Accepts a transport or the settings to build one. */
function transportOf(target: HttpTransport | TransportOptions): HttpTransport {
  return target instanceof HttpTransport ? target : new HttpTransport(target);
}

/** Iterates the pages of a cursor-paged `GET` operation, e.g. `paginatePages(t, "/v1/alerts", {status: "firing"})`. */
export function paginatePages<T>(
  target: HttpTransport | TransportOptions,
  path: string,
  query: Query = {},
  options: PaginateOptions = {},
): AsyncGenerator<CursorPage<T>, void, undefined> {
  const transport = transportOf(target);
  const base: Query = options.pageSize !== undefined ? { ...query, limit: options.pageSize } : { ...query };
  return paginatePagesWith(async (cursor) => {
    const requestOptions = { query: cursor ? { ...base, cursor } : base, ...(options.signal ? { signal: options.signal } : {}) };
    const { data } = await transport.request<CursorPage<T>>("GET", path, requestOptions);
    return data;
  }, options);
}

/** Iterates the items of a cursor-paged `GET` operation. */
export async function* paginate<T>(
  target: HttpTransport | TransportOptions,
  path: string,
  query: Query = {},
  options: PaginateOptions = {},
): AsyncGenerator<T, void, undefined> {
  for await (const page of paginatePages<T>(target, path, query, options)) {
    yield* page.items ?? [];
  }
}

/** One page of `GET /v1/changes`. */
export interface ChangePage<T> {
  items: T[];
  lastSeq: number;
  hasMore: boolean;
  watermarkSeq?: number;
  purgeHorizon?: number;
  [member: string]: unknown;
}

/** Options of {@link iterateChanges}. */
export interface ChangesOptions {
  /** Start after this seq (default 0). */
  after?: number;
  /** Filters: `source`, `kind`, `bbox`, `externalId`, `payload`, … */
  filters?: Query;
  /** Page size (1 to 10,000; the server default is 1,000). */
  limit?: number;
  signal?: AbortSignal;
  /** Called after each page with its `lastSeq`, to persist the cursor. */
  onPage?: (page: ChangePage<unknown>) => void;
}

/**
 * Iterates `GET /v1/changes` from `after` until `hasMore` is false.
 *
 * @throws ResyncRequiredError when `after` is below the purge horizon (410 RESYNC_REQUIRED)
 */
export async function* iterateChanges<T = Record<string, unknown>>(
  target: HttpTransport | TransportOptions,
  options: ChangesOptions = {},
): AsyncGenerator<T, number, undefined> {
  const transport = transportOf(target);
  let after = options.after ?? 0;
  for (;;) {
    const query: Query = { ...(options.filters ?? {}), after };
    if (options.limit !== undefined) {
      query["limit"] = options.limit;
    }
    const { data } = await transport.request<ChangePage<T>>("GET", "/v1/changes",
      options.signal ? { query, signal: options.signal } : { query });
    options.onPage?.(data as ChangePage<unknown>);
    yield* data.items ?? [];
    after = data.lastSeq ?? after;
    if (!data.hasMore || options.signal?.aborted) {
      return after;
    }
  }
}
