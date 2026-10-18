/**
 * Batched envelope resolution (`POST /v1/objects/resolve`, at most 500 refs per call; docs/Client-Contract.md §8).
 * Missing and invisible references come back in `missing`, not as errors.
 */
import { HttpTransport, type TransportOptions } from "../http.js";
import type { ObjectEnvelope, ResolveResult } from "./envelope.js";
import { parseObjectRef } from "./object-ref.js";

/** Most references per resolve call. */
export const MAX_RESOLVE_BATCH = 500;

/** Options of {@link resolveObjects}. */
export interface ResolveOptions {
  /** Transaction-time replay (records and situations honor it). */
  asOf?: string | null;
  /** References per call (1 to 500, default 500). */
  batchSize?: number;
  /** Check every reference locally first, so one bad ref does not fail a whole batch (default true). */
  validate?: boolean;
  signal?: AbortSignal;
}

/**
 * Resolves any number of references in batches of at most 500, keeping the first occurrence of duplicates and
 * the input order.
 *
 * @throws ObjectRefError for a malformed reference (when `validate` is on)
 * @throws ApiError when a call fails
 */
export async function resolveObjects(
  target: HttpTransport | TransportOptions,
  refs: Iterable<string>,
  options: ResolveOptions = {},
): Promise<ResolveResult> {
  const transport = target instanceof HttpTransport ? target : new HttpTransport(target);
  const batchSize = Math.min(MAX_RESOLVE_BATCH, Math.max(1, options.batchSize ?? MAX_RESOLVE_BATCH));
  const unique: string[] = [];
  const seen = new Set<string>();
  for (const ref of refs) {
    if (options.validate ?? true) {
      parseObjectRef(ref);
    }
    if (!seen.has(ref)) {
      seen.add(ref);
      unique.push(ref);
    }
  }
  const items: ObjectEnvelope[] = [];
  const missing: string[] = [];
  let asOf: string | null | undefined = options.asOf;
  for (let i = 0; i < unique.length; i += batchSize) {
    const body: Record<string, unknown> = { refs: unique.slice(i, i + batchSize) };
    if (options.asOf !== undefined && options.asOf !== null) {
      body["asOf"] = options.asOf;
    }
    const { data } = await transport.request<ResolveResult>("POST", "/v1/objects/resolve",
      options.signal ? { body, signal: options.signal } : { body });
    items.push(...(data.items ?? []));
    missing.push(...(data.missing ?? []));
    asOf = data.asOf ?? asOf;
  }
  return asOf === undefined ? { items, missing } : { items, missing, asOf };
}

/** Splits references into batches of at most `size` (default 500). */
export function batchRefs(refs: readonly string[], size = MAX_RESOLVE_BATCH): string[][] {
  const out: string[][] = [];
  for (let i = 0; i < refs.length; i += size) {
    out.push(refs.slice(i, i + size));
  }
  return out;
}
