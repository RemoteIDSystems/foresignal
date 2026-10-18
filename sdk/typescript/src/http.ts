/**
 * The minimal HTTP layer the hand-written SDK code uses: `fetch`, a base URL, a bearer token and JSON. It does not
 * use the generated types, so the realtime, paging and contract helpers work with or without them.
 */
import { apiErrorFrom } from "./errors.js";

/** A `fetch`-compatible function (the global `fetch` by default). */
export type FetchLike = (input: string, init?: RequestInit) => Promise<Response>;

/** A query parameter value; arrays are sent as repeated parameters, `null`/`undefined` are left out. */
export type QueryValue = string | number | boolean | null | undefined | ReadonlyArray<string | number | boolean>;

/** Query parameters. */
export type Query = Record<string, QueryValue>;

/** Connection settings shared by every helper. */
export interface TransportOptions {
  /** The Foresignal base URL, e.g. `https://foresignal.example.org` (no `/v1`). */
  baseUrl: string;
  /** A Foresignal API token, sent as `Authorization: Bearer …`. */
  token?: string;
  /** Extra headers sent with every request. */
  headers?: Record<string, string>;
  /** The `fetch` to use (defaults to the global one). */
  fetch?: FetchLike;
}

/** Per-request options. */
export interface RequestOptions {
  query?: Query;
  /** A JSON body (serialized with `JSON.stringify`). */
  body?: unknown;
  /** Content type of the body (default `application/json`). */
  contentType?: string;
  headers?: Record<string, string>;
  /** Send the bearer token (default true). */
  auth?: boolean;
  signal?: AbortSignal;
}

/** A parsed JSON response with the raw response for its headers. */
export interface JsonResponse<T> {
  data: T;
  response: Response;
}

/** Serializes query parameters (repeated keys for arrays). */
export function encodeQuery(query: Query | undefined): string {
  if (!query) {
    return "";
  }
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value === null || value === undefined) {
      continue;
    }
    if (Array.isArray(value)) {
      for (const item of value) {
        params.append(key, String(item));
      }
    } else {
      params.append(key, String(value));
    }
  }
  const text = params.toString();
  return text ? `?${text}` : "";
}

/** Sends requests to one Foresignal instance. */
export class HttpTransport {
  /** The base URL without a trailing slash. */
  readonly baseUrl: string;
  private readonly token: string | undefined;
  private readonly extraHeaders: Record<string, string>;
  private readonly fetchFn: FetchLike;

  /** Creates a transport from connection settings. */
  constructor(options: TransportOptions) {
    this.baseUrl = options.baseUrl.replace(/\/+$/, "");
    this.token = options.token;
    this.extraHeaders = { ...(options.headers ?? {}) };
    const f = options.fetch ?? (globalThis.fetch as FetchLike | undefined);
    if (!f) {
      throw new Error("No fetch implementation: pass options.fetch");
    }
    this.fetchFn = f;
  }

  /** The absolute URL of `path` (starting with `/`) with `query`. */
  url(path: string, query?: Query): string {
    return this.baseUrl + path + encodeQuery(query);
  }

  /** The headers for a request: the extra headers, then the token (when `auth`), then `extra`. */
  headers(extra?: Record<string, string>, auth = true): Record<string, string> {
    const out: Record<string, string> = { ...this.extraHeaders };
    if (auth && this.token) {
      out["Authorization"] = `Bearer ${this.token}`;
    }
    return { ...out, ...(extra ?? {}) };
  }

  /** Sends a request and returns the raw response, whatever its status. */
  async fetch(method: string, path: string, options: RequestOptions = {}): Promise<Response> {
    const headers = this.headers(options.headers, options.auth ?? true);
    let body: string | undefined;
    if (options.body !== undefined) {
      body = JSON.stringify(options.body);
      headers["Content-Type"] = options.contentType ?? "application/json";
    }
    const init: RequestInit = { method, headers };
    if (body !== undefined) {
      init.body = body;
    }
    if (options.signal) {
      init.signal = options.signal;
    }
    return this.fetchFn(this.url(path, options.query), init);
  }

  /**
   * Sends a request and parses the JSON answer.
   *
   * @throws ApiError (or ResyncRequiredError) for a non-2xx status
   */
  async request<T>(method: string, path: string, options: RequestOptions = {}): Promise<JsonResponse<T>> {
    const response = await this.fetch(method, path, {
      ...options,
      headers: { Accept: "application/json", ...(options.headers ?? {}) },
    });
    if (!response.ok) {
      throw await apiErrorFrom(response);
    }
    if (response.status === 204) {
      return { data: undefined as T, response };
    }
    const text = await response.text();
    return { data: (text ? JSON.parse(text) : undefined) as T, response };
  }

  /** `GET` returning JSON. */
  async getJson<T>(path: string, query?: Query): Promise<JsonResponse<T>> {
    return this.request<T>("GET", path, query ? { query } : {});
  }

  /** `POST` with a JSON body returning JSON. */
  async postJson<T>(path: string, body: unknown, query?: Query): Promise<JsonResponse<T>> {
    return this.request<T>("POST", path, query ? { body, query } : { body });
  }
}
