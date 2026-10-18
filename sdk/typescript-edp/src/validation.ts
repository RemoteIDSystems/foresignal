/**
 * Item and batch validation in the server's order (EDP §3.1), with the server's rejection codes.
 *
 * Per item: a known `type`; the stream exists (a source-level `status` may omit it); the type
 * matches the stream's `itemType` (`delete` and `status` fit any stream); the item schema; string and
 * media limits; times (RFC 3339 with an offset, `observedAt` at most 5 minutes after receipt and
 * present unless the stream declares `observedAtMode: received`); `properties` against the stream's
 * `payloadSchema`; geometry (§3.3); the observation period and one granularity per series; forecast
 * values. Invalid items are rejected individually.
 */
import { rejection } from "./errors.ts";
import type { ItemRejection, SchemaError } from "./errors.ts";
import { validateGeometry } from "./geometry.ts";
import { asManifestCheck } from "./manifest.ts";
import type { JsonObject, ManifestCheck, StreamInfo, ValidateManifestOptions } from "./manifest.ts";
import { dumps } from "./ndjson.ts";
import { parsePeriod } from "./periods.ts";
import {
  ITEM_TYPES, MAX_BATCH_BYTES, MAX_FUTURE_MS, MAX_ITEMS, MAX_MEDIA, MAX_STRING_LENGTH, MAX_TEXT_LENGTH, QUANTILES,
  REQUIRED_QUANTILES, protocolMinor, registeredSourceId,
} from "./protocol.ts";
import type { ItemType } from "./protocol.ts";
import { shapeErrors } from "./schemas.ts";
import { parseRfc3339 } from "./times.ts";

const TIMES = ["observedAt", "eventTime", "validUntil", "knownAt", "publishedAt", "madeAt"];
const NEEDS_OBSERVED_AT = new Set(["feature", "event", "observation", "document"]);

/** An accepted item: its index, the item, its type and its stream (null for a source-level status). */
export interface ValidItem {
  /** The item's position. */
  index: number;
  /** The item. */
  item: JsonObject;
  /** Its type. */
  type: ItemType;
  /** Its stream, or null for a source-level `status`. */
  stream: StreamInfo | null;
}

/** The result of validating items: accepted and refused, each in order. */
export interface ItemCheck {
  /** The accepted items. */
  valid: ValidItem[];
  /** The refused items. */
  rejected: ItemRejection[];
  /** True when no item was refused. */
  ok: boolean;
}

/**
 * The result of validating a batch. `code` is the server's batch-level refusal (`VALIDATION_FAILED`,
 * `PROTOCOL_UNSUPPORTED`, `SOURCE_MISMATCH` or `PAYLOAD_TOO_LARGE`) with its `errors`; when the
 * header is acceptable `items` holds the per-item result.
 */
export interface BatchCheck {
  /** The batch-level refusal, or null. */
  code: string | null;
  /** The batch-level problems. */
  errors: SchemaError[];
  /** The per-item result, when the header was acceptable. */
  items: ItemCheck | null;
  /** True when the batch is accepted and every item is valid. */
  ok: boolean;
}

/** Options of the item and batch validators. */
export interface ValidateItemsOptions extends ValidateManifestOptions {
  /** The receipt time the 5-minute `observedAt` rule compares to (default now). */
  receivedAt?: Date;
}

function isObject(value: unknown): value is JsonObject {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function text(node: unknown, name: string): string | null {
  if (!isObject(node)) return null;
  const value = node[name];
  if (value === null || value === undefined || typeof value === "object") return null;
  return String(value);
}

function isNumber(value: unknown): value is number {
  return typeof value === "number";
}

/** Collects strings over the limits: `/text` 20,000 characters, everything else 2,048. */
export function checkStrings(node: unknown, path: string, errors: SchemaError[]): void {
  if (typeof node === "string") {
    const limit = path === "/text" ? MAX_TEXT_LENGTH : MAX_STRING_LENGTH;
    if (node.length > limit) errors.push({ path, message: `is ${node.length} characters; the maximum is ${limit}` });
  } else if (Array.isArray(node)) {
    node.forEach((child, i) => checkStrings(child, `${path}/${i}`, errors));
  } else if (isObject(node)) {
    for (const [key, value] of Object.entries(node)) {
      if (key.length > MAX_STRING_LENGTH) {
        errors.push({ path, message: `has a member name longer than ${MAX_STRING_LENGTH} characters` });
      }
      checkStrings(value, `${path}/${key.replace(/~/g, "~0").replace(/\//g, "~1")}`, errors);
    }
  }
}

/** The problem with a forecast's question and values, or null. */
export function forecastProblem(item: JsonObject): string | null {
  const question = item["question"];
  const questionRef = item["questionRef"];
  if (!isObject(question) && !(typeof questionRef === "string" && questionRef.trim() !== "")) {
    return "a forecast needs a question or a questionRef";
  }
  const probability = item["probability"];
  const quantiles = item["quantiles"];
  const hasProbability = isNumber(probability);
  const hasQuantiles = isObject(quantiles);
  if (!hasProbability && !hasQuantiles) return "a forecast needs a probability or quantiles";
  if (hasProbability && (probability < 0 || probability > 1)) return "probability must be between 0 and 1";
  if (hasQuantiles) {
    if (REQUIRED_QUANTILES.some((name) => !isNumber(quantiles[name]))) return "quantiles need p05, p25, p50, p75 and p95";
    let last: number | null = null;
    let lastName = "";
    for (const name of QUANTILES) {
      const value = quantiles[name];
      if (value === undefined || value === null) continue;
      if (!isNumber(value) || !Number.isFinite(value)) return `quantile ${name} must be a finite number`;
      if (last !== null && value < last) return `quantiles must be non-decreasing, but ${name} < ${lastName}`;
      last = value;
      lastName = name;
    }
  }
  return null;
}

function check(index: number, item: unknown, manifest: ManifestCheck, receivedAt: number,
  granularity: Map<string, string>): ValidItem | ItemRejection {
  const streamId = text(item, "stream");
  const id = text(item, "id");
  if (!isObject(item)) return rejection(index, null, null, "SCHEMA_INVALID", "an item must be a JSON object");
  const type = text(item, "type");
  if (type === null || !(ITEM_TYPES as readonly string[]).includes(type)) {
    return rejection(index, streamId, id, "SCHEMA_INVALID", `type must be one of [${ITEM_TYPES.join(", ")}]`);
  }
  const itemType = type as ItemType;
  let stream: StreamInfo | null = null;
  if (streamId !== null || itemType !== "status") {
    stream = streamId === null ? null : manifest.streams.get(streamId) ?? null;
    if (stream === null) {
      return rejection(index, streamId, id, "UNKNOWN_STREAM", `the manifest declares no stream '${streamId}'`);
    }
    if (itemType !== "delete" && itemType !== "status" && itemType !== stream.itemType) {
      return rejection(index, streamId, id, "ITEM_TYPE_MISMATCH",
        `stream '${streamId}' carries ${stream.itemType} items, not ${itemType}`);
    }
  }
  const shape = shapeErrors(itemType, item);
  if (shape.length > 0) return { index, stream: streamId, id, reason: "SCHEMA_INVALID", schemaErrors: shape };
  const limits: SchemaError[] = [];
  checkStrings(item, "", limits);
  const media = item["media"];
  if (Array.isArray(media) && media.length > MAX_MEDIA) {
    limits.push({ path: "/media", message: `has ${media.length} entries; the maximum is ${MAX_MEDIA}` });
  }
  if (limits.length > 0) return { index, stream: streamId, id, reason: "LIMIT_EXCEEDED", schemaErrors: limits };
  for (const name of TIMES) {
    const value = item[name];
    if (value !== undefined && value !== null && parseRfc3339(value) === null) {
      return { index, stream: streamId, id, reason: "SCHEMA_INVALID",
        schemaErrors: [{ path: `/${name}`, message: "must be an RFC 3339 date-time with an offset" }] };
    }
  }
  const observedAt = parseRfc3339(item["observedAt"]);
  if (observedAt !== null && observedAt > receivedAt + MAX_FUTURE_MS) {
    return rejection(index, streamId, id, "OBSERVED_AT_IN_FUTURE",
      `observedAt ${String(item["observedAt"])} is more than 5 minutes after receipt`);
  }
  if (NEEDS_OBSERVED_AT.has(itemType) && observedAt === null && !(stream?.observedAtReceived ?? false)) {
    return rejection(index, streamId, id, "MISSING_OBSERVED_AT",
      "observedAt is required unless the stream declares observedAtMode: received");
  }
  if (stream !== null && itemType !== "status" && itemType !== "delete") {
    const validator = manifest.payloadValidators.get(stream.id);
    if (validator !== undefined) {
      const payload = validator.validate(item["properties"]);
      if (payload.length > 0) {
        return { index, stream: streamId, id, reason: "PAYLOAD_INVALID",
          schemaErrors: payload.map((e) => ({ path: `/properties${e.path}`, message: e.message })) };
      }
    }
    const geometry = validateGeometry(item["geometry"], stream.geometry, "/geometry");
    if (geometry.length > 0) return { index, stream: streamId, id, reason: "GEOMETRY_INVALID", schemaErrors: geometry };
  }
  if (itemType === "observation") {
    const t = text(item, "t");
    const period = parsePeriod(t);
    if (period === null) {
      return rejection(index, streamId, id, "PERIOD_INVALID",
        `t '${t}' is not YYYY, YYYY-Qn, YYYY-MM, YYYY-Www, YYYY-MM-DD or an RFC 3339 timestamp`);
    }
    const seriesKey = text(item, "seriesKey");
    const key = `${streamId}\u0000${seriesKey}`;
    const previous = granularity.get(key);
    if (previous === undefined) granularity.set(key, period.granularity);
    else if (previous !== period.granularity) {
      return rejection(index, streamId, id, "GRANULARITY_MIXED",
        `series '${seriesKey}' already uses ${previous} periods, not ${period.granularity}`);
    }
  }
  if (itemType === "forecast") {
    const problem = forecastProblem(item);
    if (problem !== null) return rejection(index, streamId, id, "FORECAST_INVALID", problem);
  }
  return { index, item, type: itemType, stream };
}

/**
 * Validates `items` against a manifest (an object or a {@link ManifestCheck}).
 *
 * @throws ManifestInvalidError when the manifest itself is invalid
 */
export function validateItems(items: readonly unknown[], manifest: ManifestCheck | JsonObject,
  options: ValidateItemsOptions = {}): ItemCheck {
  const checked = asManifestCheck(manifest, options);
  const receivedAt = (options.receivedAt ?? new Date()).getTime();
  const valid: ValidItem[] = [];
  const rejected: ItemRejection[] = [];
  const granularity = new Map<string, string>();
  items.forEach((item, index) => {
    const outcome = check(index, item, checked, receivedAt, granularity);
    if ("reason" in outcome) rejected.push(outcome);
    else valid.push(outcome);
  });
  return { valid, rejected, ok: rejected.length === 0 };
}

/** The problem with a batch's `snapshot` member, or null (absent or null is fine). */
export function validateSnapshotHeader(snapshot: unknown): string | null {
  if (snapshot === undefined || snapshot === null) return null;
  if (!isObject(snapshot)) return "snapshot must be an object";
  const id = text(snapshot, "id");
  const stream = text(snapshot, "stream");
  const page = snapshot["page"];
  if (id === null || id.length > 128 || stream === null || typeof page !== "number" || !Number.isInteger(page)
    || page < 1 || typeof snapshot["final"] !== "boolean") {
    return "snapshot needs id (at most 128 characters), stream, page >= 1 and final";
  }
  return null;
}

function refused(code: string, path: string, message: string): BatchCheck {
  return { code, errors: [{ path, message }], items: null, ok: false };
}

/**
 * Validates a whole batch: size, `protocol` not above the manifest's minor, `sourceId` equal to the
 * provider id or its registered `ext-` id, `items` an array of at most 10,000, a well-formed
 * `snapshot`, then every item.
 *
 * @throws ManifestInvalidError when the manifest itself is invalid
 */
export function validateBatch(batch: unknown, manifest: ManifestCheck | JsonObject,
  options: ValidateItemsOptions & { registeredId?: string } = {}): BatchCheck {
  const checked = asManifestCheck(manifest, options);
  if (!isObject(batch)) return refused("VALIDATION_FAILED", "", "the batch must be a JSON object");
  let size: number;
  try {
    size = dumps(batch).length;
  } catch (e) {
    return refused("VALIDATION_FAILED", "", `the batch is not JSON: ${(e as Error).message}`);
  }
  if (size > MAX_BATCH_BYTES) {
    return refused("PAYLOAD_TOO_LARGE", "", `the batch is ${size} bytes; the maximum is ${MAX_BATCH_BYTES}`);
  }
  const minor = protocolMinor(batch["protocol"]);
  if (minor < 0) return refused("VALIDATION_FAILED", "/protocol", "must be foresignal-edp/1.N");
  if (minor > checked.minor) {
    return refused("PROTOCOL_UNSUPPORTED", "/protocol",
      `foresignal-edp/1.${minor} is newer than the manifest's 1.${checked.minor}`);
  }
  const sourceId = batch["sourceId"];
  if (sourceId !== undefined && sourceId !== null) {
    const value = String(sourceId);
    const provider = checked.providerId ?? "";
    const allowed = new Set([provider, registeredSourceId(provider)]);
    if (options.registeredId) allowed.add(options.registeredId);
    if (!allowed.has(value)) {
      return refused("SOURCE_MISMATCH", "/sourceId", `names source '${value}', not '${registeredSourceId(provider)}'`);
    }
  }
  const items = batch["items"];
  if (items !== undefined && items !== null && !Array.isArray(items)) {
    return refused("VALIDATION_FAILED", "/items", "must be an array");
  }
  const list = (items as unknown[] | null | undefined) ?? [];
  if (list.length > MAX_ITEMS) {
    return refused("VALIDATION_FAILED", "/items", `has ${list.length} items; the maximum is ${MAX_ITEMS}`);
  }
  const problem = validateSnapshotHeader(batch["snapshot"]);
  if (problem !== null) return refused("VALIDATION_FAILED", "/snapshot", problem);
  const itemCheck = validateItems(list, checked, options);
  return { code: null, errors: [], items: itemCheck, ok: itemCheck.ok };
}
