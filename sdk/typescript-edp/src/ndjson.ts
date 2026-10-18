/**
 * Batch encoding: one JSON object, or NDJSON (one item per line, optionally after a header line).
 * The NDJSON header is the batch without `items`: an object with `protocol` and no `type` (EDP §4.1).
 */
import { BatchFormatError } from "./errors.ts";
import type { Batch } from "./items.ts";
import { MAX_DEPTH, MAX_ITEMS, NDJSON_MEDIA_TYPE } from "./protocol.ts";

const encoder = new TextEncoder();
const decoder = new TextDecoder("utf-8", { fatal: true });

/** Compact UTF-8 JSON bytes. */
export function dumps(value: unknown): Uint8Array {
  return encoder.encode(JSON.stringify(value));
}

/** Encodes a batch as one JSON object. */
export function encodeJson(value: Batch | Record<string, unknown>): Uint8Array {
  return dumps(value);
}

/** Encodes a header line (without `items`; omitted when `header` is null) and one line per item. */
export function encodeNdjson(header: Record<string, unknown> | null, items: Iterable<unknown>): Uint8Array {
  const lines: string[] = [];
  if (header !== null) {
    const { items: _ignored, ...rest } = header;
    lines.push(JSON.stringify(rest));
  }
  for (const item of items) lines.push(JSON.stringify(item));
  return encoder.encode(`${lines.join("\n")}\n`);
}

function depth(value: unknown, level = 1): number {
  if (Array.isArray(value)) return value.reduce<number>((max, v) => Math.max(max, depth(v, level + 1)), level);
  if (value !== null && typeof value === "object") {
    return Object.values(value).reduce<number>((max, v) => Math.max(max, depth(v, level + 1)), level);
  }
  return level - 1;
}

function toText(body: Uint8Array | string): string {
  if (typeof body === "string") return body;
  try {
    return decoder.decode(body);
  } catch {
    throw new BatchFormatError("The body is not valid UTF-8");
  }
}

function parse(text: string, what: string): unknown {
  let value: unknown;
  try {
    value = JSON.parse(text);
  } catch (e) {
    throw new BatchFormatError(`${what} is not valid JSON: ${(e as Error).message}`);
  }
  if (depth(value) > MAX_DEPTH) throw new BatchFormatError(`${what} nests deeper than ${MAX_DEPTH} levels`);
  return value;
}

function isObject(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

/**
 * Decodes a JSON batch; `items` defaults to an empty array.
 *
 * @throws BatchFormatError for malformed JSON, nesting deeper than 32, a non-object, a non-array
 *     `items` or more than 10,000 items
 */
export function decodeJson(body: Uint8Array | string): Batch {
  const root = parse(toText(body), "The batch");
  if (!isObject(root)) throw new BatchFormatError("The batch must be a JSON object");
  const items = root["items"];
  if (items !== undefined && items !== null && !Array.isArray(items)) throw new BatchFormatError("The batch's items must be an array");
  const list = (items as unknown[] | null | undefined) ?? [];
  if (list.length > MAX_ITEMS) throw new BatchFormatError(`The batch has ${list.length} items; the maximum is ${MAX_ITEMS}`);
  return { ...root, items: list } as Batch;
}

/**
 * Decodes NDJSON into a batch: the first line is the header when it has `protocol` and no `type`.
 *
 * @throws BatchFormatError as {@link decodeJson}, per line
 */
export function decodeNdjson(body: Uint8Array | string): Batch {
  let header: Record<string, unknown> = {};
  const items: unknown[] = [];
  let first = true;
  toText(body).split(/\r?\n/).forEach((line, index) => {
    if (line.trim() === "") return;
    const node = parse(line, `Line ${index + 1}`);
    if (!isObject(node)) throw new BatchFormatError(`Line ${index + 1} must be a JSON object`);
    if (first && "protocol" in node && !("type" in node)) {
      const { items: _ignored, ...rest } = node;
      header = rest;
    } else {
      items.push(node);
      if (items.length > MAX_ITEMS) throw new BatchFormatError(`The batch has more than ${MAX_ITEMS} items`);
    }
    first = false;
  });
  return { ...header, items } as Batch;
}

/** Decodes a batch, as NDJSON when `contentType` is `application/x-ndjson`, else as JSON. */
export function decodeBatch(body: Uint8Array | string, contentType?: string | null): Batch {
  const media = contentType?.split(";")[0]?.trim().toLowerCase();
  return media === NDJSON_MEDIA_TYPE ? decodeNdjson(body) : decodeJson(body);
}
