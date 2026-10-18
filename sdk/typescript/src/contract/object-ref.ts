/**
 * Object references `{type}:{id}` (docs/Client-Contract.md §2), parsed and formatted with the same rules as the
 * server's `ObjectRef`: record ids are `{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded
 * (`/` → `%2F`, `@` → `%40`, `%` → `%25`, whitespace and control characters as their UTF-8 bytes), sensor ids
 * are `{kind}:{id}`, remote ids are `{instanceId}/{type}:{id}`. Ids are otherwise opaque and references compare
 * as strings. Types are not checked against a fixed list (the contract grows additively); pass `knownTypes`
 * (from `GET /v1/contract`) to {@link parseObjectRef} to reject types the server does not know.
 */
import { ObjectRefError } from "../errors.js";

/** Longest accepted reference, in characters. */
export const MAX_OBJECT_REF_LENGTH = 1024;

/** Sensor kinds accepted in `sensor:{kind}:{id}`. */
export const SENSOR_KINDS: readonly string[] = ["source", "receiver", "station", "satellite", "scene", "raster-product"];

const TYPE_PATTERN = /^[a-z][a-z-]*$/;

/** The record parts of a `record:` reference. */
export interface RecordRefParts {
  sourceId: string;
  /** The decoded external id. */
  externalId: string;
  /** The pinned version, or null for the current one. */
  seq: number | null;
}

/** A parsed reference. */
export interface ObjectRef {
  /** The canonical text, `{type}:{id}`. */
  readonly ref: string;
  readonly type: string;
  /** The id exactly as it appears after the first colon. */
  readonly id: string;
  /** Present for `record:` references. */
  readonly record?: RecordRefParts;
  /** Present for `sensor:` references. */
  readonly sensor?: { kind: string; id: string };
  /** Present for `remote:` references. */
  readonly remote?: { instanceId: string; ref: string };
}

/** True for whitespace as Java's `Character.isWhitespace` defines it (the server's rule). */
function isJavaWhitespace(cp: number): boolean {
  return (cp >= 0x09 && cp <= 0x0d) || (cp >= 0x1c && cp <= 0x20) || cp === 0x1680
    || (cp >= 0x2000 && cp <= 0x2006) || (cp >= 0x2008 && cp <= 0x200a)
    || cp === 0x2028 || cp === 0x2029 || cp === 0x205f || cp === 0x3000;
}

/** True for ISO control characters (U+0000–U+001F, U+007F–U+009F). */
function isControl(cp: number): boolean {
  return cp <= 0x1f || (cp >= 0x7f && cp <= 0x9f);
}

/** Percent-encodes `%`, `/`, `@`, whitespace and control characters of an external id. */
export function encodeExternalId(externalId: string): string {
  let out = "";
  for (const ch of externalId) {
    const cp = ch.codePointAt(0)!;
    if (ch === "%" || ch === "/" || ch === "@" || isJavaWhitespace(cp) || isControl(cp)) {
      for (const b of new TextEncoder().encode(ch)) {
        out += "%" + b.toString(16).toUpperCase().padStart(2, "0");
      }
    } else {
      out += ch;
    }
  }
  return out;
}

/**
 * Decodes a percent-encoded external id (UTF-8).
 *
 * @throws Error on a truncated or non-hex escape
 */
export function decodeExternalId(encoded: string): string {
  if (!encoded.includes("%")) {
    return encoded;
  }
  const bytes: number[] = [];
  const encoder = new TextEncoder();
  for (let i = 0; i < encoded.length; i++) {
    const c = encoded[i]!;
    if (c === "%") {
      const hex = encoded.slice(i + 1, i + 3);
      if (hex.length < 2 || !/^[0-9A-Fa-f]{2}$/.test(hex)) {
        throw new Error(hex.length < 2 ? "truncated escape" : "bad escape");
      }
      bytes.push(parseInt(hex, 16));
      i += 2;
    } else {
      const cp = encoded.codePointAt(i)!;
      const ch = String.fromCodePoint(cp);
      bytes.push(...encoder.encode(ch));
      i += ch.length - 1;
    }
  }
  return new TextDecoder("utf-8").decode(new Uint8Array(bytes));
}

/**
 * Parses a reference.
 *
 * @param text the reference
 * @param knownTypes optional list of types in force (`GET /v1/contract`); other types are rejected
 * @throws ObjectRefError when it is malformed
 */
export function parseObjectRef(text: string, knownTypes?: readonly string[]): ObjectRef {
  if (typeof text !== "string" || text.trim() === "") {
    throw new ObjectRefError(String(text ?? ""), "is required");
  }
  if (text.length > MAX_OBJECT_REF_LENGTH) {
    throw new ObjectRefError(text, `is longer than ${MAX_OBJECT_REF_LENGTH} characters`);
  }
  const colon = text.indexOf(":");
  if (colon <= 0 || colon === text.length - 1) {
    throw new ObjectRefError(text, "must be {type}:{id}");
  }
  const type = text.slice(0, colon);
  const id = text.slice(colon + 1);
  if (!TYPE_PATTERN.test(type)) {
    throw new ObjectRefError(text, `has an invalid object type '${type}'`);
  }
  if (knownTypes && !knownTypes.includes(type)) {
    throw new ObjectRefError(text, `has unknown object type '${type}'`);
  }
  for (const ch of id) {
    const cp = ch.codePointAt(0)!;
    if (isJavaWhitespace(cp) || isControl(cp)) {
      throw new ObjectRefError(text, "must not contain whitespace or control characters");
    }
  }
  if (type === "record") {
    return { ref: text, type, id, record: parseRecordId(text, id) };
  }
  if (type === "sensor") {
    const c = id.indexOf(":");
    if (c <= 0 || c === id.length - 1 || !SENSOR_KINDS.includes(id.slice(0, c))) {
      throw new ObjectRefError(text, `sensor refs are sensor:{kind}:{id} with kind one of ${SENSOR_KINDS.join(", ")}`);
    }
    return { ref: text, type, id, sensor: { kind: id.slice(0, c), id: id.slice(c + 1) } };
  }
  if (type === "remote") {
    const slash = id.indexOf("/");
    const inner = slash > 0 ? id.slice(slash + 1) : "";
    const innerColon = inner.indexOf(":");
    if (slash <= 0 || inner === "" || innerColon <= 0 || innerColon === inner.length - 1) {
      throw new ObjectRefError(text, "remote refs are remote:{instanceId}/{type}:{id}");
    }
    const innerType = inner.slice(0, innerColon);
    if (!TYPE_PATTERN.test(innerType) || innerType === "remote") {
      throw new ObjectRefError(text, "a remote ref names an object type other than remote");
    }
    return { ref: text, type, id, remote: { instanceId: id.slice(0, slash), ref: inner } };
  }
  return { ref: text, type, id };
}

/** Validates and splits a record id. */
function parseRecordId(text: string, id: string): RecordRefParts {
  const slash = id.indexOf("/");
  if (slash <= 0 || slash === id.length - 1) {
    throw new ObjectRefError(text, "record refs are record:{sourceId}/{externalId}[@{seq}]");
  }
  const rest = id.slice(slash + 1);
  const at = rest.indexOf("@");
  if (at === 0 || (at > 0 && rest.indexOf("@", at + 1) >= 0)) {
    throw new ObjectRefError(text, "a record ref has at most one '@' (encode '@' in the external id as %40)");
  }
  let seq: number | null = null;
  if (at > 0) {
    const digits = rest.slice(at + 1);
    if (digits === "" || digits.length > 19 || !/^\d+$/.test(digits)) {
      throw new ObjectRefError(text, "the version after '@' must be a non-negative integer seq");
    }
    seq = Number(digits);
  }
  let externalId: string;
  try {
    externalId = decodeExternalId(at < 0 ? rest : rest.slice(0, at));
  } catch {
    throw new ObjectRefError(text, "has a malformed percent-encoding in the external id");
  }
  return { sourceId: id.slice(0, slash), externalId, seq };
}

/** True when `text` parses. */
export function isValidObjectRef(text: string, knownTypes?: readonly string[]): boolean {
  try {
    parseObjectRef(text, knownTypes);
    return true;
  } catch {
    return false;
  }
}

/** Formats `{type}:{id}` (validated). */
export function formatObjectRef(type: string, id: string): string {
  return parseObjectRef(`${type}:${id}`).ref;
}

/** Formats a record reference from its parts, encoding the external id. */
export function recordRef(sourceId: string, externalId: string, seq?: number | null): string {
  const id = `${sourceId}/${encodeExternalId(externalId)}${seq === null || seq === undefined ? "" : `@${seq}`}`;
  return parseObjectRef(`record:${id}`).ref;
}

/** Formats a remote reference `remote:{instanceId}/{ref}`. */
export function remoteRef(instanceId: string, ref: string): string {
  return parseObjectRef(`remote:${instanceId}/${ref}`).ref;
}

/**
 * Encodes a reference as one URL path segment (`/v1/objects/{ref}`): `%` → `%25`, `/` → `%2F`, `#` → `%23`,
 * `?` → `%3F`; everything else stays as written.
 */
export function objectRefPathSegment(ref: string): string {
  return ref.replace(/%/g, "%25").replace(/\//g, "%2F").replace(/#/g, "%23").replace(/\?/g, "%3F");
}
