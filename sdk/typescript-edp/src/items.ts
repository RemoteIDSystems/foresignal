/**
 * Typed builders for every EDP item type, the batch envelope and snapshot pages (EDP §3).
 *
 * Each builder returns a plain object in wire form, omitting optional members left `undefined`.
 * Times accept a `Date` or a ready RFC 3339 string. `extra` merges further members (for example a
 * later 1.x field) into the item.
 */
import type { Geometry } from "./geometry.ts";
import { HEALTH_VALUES, PROTOCOL_VERSION, REQUIRED_QUANTILES } from "./protocol.ts";
import type { DocumentType, Health, OriginatorClass } from "./protocol.ts";
import { formatDate, formatTime } from "./times.ts";
import type { TimeLike } from "./times.ts";
import { randomUUID } from "node:crypto";

/** A JSON object. */
export type JsonObject = Record<string, unknown>;

/** Item-level provenance (EDP §3.4). */
export interface Origin {
  /** The original observer's slug. */
  originator: string;
  /** Its class. */
  class?: OriginatorClass | null;
  /** The original publisher's id for the observation or report. */
  upstreamId?: string | null;
  /** Other items (`stream:id`) or upstream ids this item was derived from. */
  derivedFrom?: string[];
}

/** Members every item may carry. */
interface ItemBase {
  /** The stream (absent only on a source-level `status`). */
  stream?: string;
  /** The item type. */
  type: string;
  /** The item id, stable per stream. */
  id?: string;
  /** When the provider observed this state. */
  observedAt?: string;
  /** Validated against the stream's `payloadSchema`. */
  properties?: JsonObject;
  /** Item-level provenance. */
  origin?: Origin;
  /** Unknown members are ignored by the protocol (forward compatibility). */
  [extra: string]: unknown;
}

/** A `feature` item. */
export interface FeatureItem extends ItemBase { type: "feature"; stream: string; id: string; geometry?: Geometry;
  eventTime?: string; validUntil?: string }
/** An `event` item. */
export interface EventItem extends ItemBase { type: "event"; stream: string; id: string; eventTime: string;
  geometry?: Geometry; upstreamEventId?: string }
/** An `observation` item. */
export interface ObservationItem extends ItemBase { type: "observation"; stream: string; id: string; seriesKey: string;
  t: string; value: number | null; knownAt?: string; unit?: string }
/** A `document` item. */
export interface DocumentItem extends ItemBase { type: "document"; stream: string; id: string; documentType: DocumentType;
  text: string }
/** A `forecast` item. */
export interface ForecastItem extends ItemBase { type: "forecast"; stream: string; id: string; madeAt: string;
  probability?: number; quantiles?: Quantiles; question?: Question; questionRef?: string }
/** A `delete` item. */
export interface DeleteItem extends ItemBase { type: "delete"; stream: string; id: string }
/** A `status` item. */
export interface StatusItem extends ItemBase { type: "status"; health: Health; message?: string }
/** Any EDP item. */
export type Item = FeatureItem | EventItem | ObservationItem | DocumentItem | ForecastItem | DeleteItem | StatusItem;

/** Forecast quantiles; p05, p25, p50, p75 and p95 are required and values are non-decreasing. */
export interface Quantiles { p05: number; p10?: number; p25: number; p50: number; p75: number; p90?: number; p95: number }

/** A provider's own forecast question. */
export interface Question { externalId: string; title: string; closeTime?: string; resolutionCriteria?: string }

/** A batch's `snapshot` member (EDP §3.2). */
export interface Snapshot { id: string; stream: string; scope: string | null; page: number; final: boolean }

/** A batch envelope. */
export interface Batch {
  /** `foresignal-edp/1.N`. */
  protocol: string;
  /** The provider id (optional; must match the registration). */
  sourceId?: string;
  /** When the batch was sent. */
  sentAt?: string;
  /** Marks the batch as a snapshot page. */
  snapshot?: Snapshot | null;
  /** The items. */
  items: unknown[];
  /** Pull responses: the next cursor, null when caught up. */
  nextCursor?: string | null;
  /** Unknown members are ignored. */
  [extra: string]: unknown;
}

/** Options every builder accepts. */
export interface CommonOptions {
  /** When the provider observed this state. */
  observedAt?: TimeLike;
  /** Item properties (validated against the stream's `payloadSchema`). */
  properties?: JsonObject;
  /** Item-level provenance. */
  origin?: Origin;
  /** Further members merged into the item. */
  extra?: JsonObject;
}

function common(stream: string | undefined, type: string, id: string | undefined, options: CommonOptions): JsonObject {
  const item: JsonObject = {};
  if (stream !== undefined) item["stream"] = stream;
  item["type"] = type;
  if (id !== undefined) item["id"] = id;
  if (options.observedAt !== undefined) item["observedAt"] = formatTime(options.observedAt);
  if (options.properties !== undefined) item["properties"] = { ...options.properties };
  if (options.origin !== undefined) item["origin"] = { ...options.origin };
  return item;
}

function finish<T>(item: JsonObject, optional: JsonObject, extra: JsonObject | undefined): T {
  for (const [key, value] of Object.entries(optional)) if (value !== undefined) item[key] = value;
  if (extra) Object.assign(item, extra);
  return item as T;
}

/** Builds an `origin` object. */
export function origin(originator: string, originatorClass?: OriginatorClass,
  options: { upstreamId?: string; derivedFrom?: string[] } = {}): Origin {
  const out: Origin = { originator };
  if (originatorClass !== undefined) out.class = originatorClass;
  if (options.upstreamId !== undefined) out.upstreamId = options.upstreamId;
  if (options.derivedFrom !== undefined) out.derivedFrom = [...options.derivedFrom];
  return out;
}

/** A `feature` item: the current state of an entity (layer streams). */
export function feature(stream: string, id: string, options: CommonOptions & { geometry?: Geometry;
  eventTime?: TimeLike; validUntil?: TimeLike } = {}): FeatureItem {
  return finish(common(stream, "feature", id, options), { geometry: options.geometry,
    eventTime: formatTime(options.eventTime), validUntil: formatTime(options.validUntil) }, options.extra);
}

/** An `event` item: a discrete occurrence; `upstreamEventId` lets evidence matching recognize it elsewhere. */
export function event(stream: string, id: string, options: CommonOptions & { eventTime: TimeLike; geometry?: Geometry;
  upstreamEventId?: string }): EventItem {
  return finish(common(stream, "event", id, options), { eventTime: formatTime(options.eventTime),
    geometry: options.geometry, upstreamEventId: options.upstreamEventId }, options.extra);
}

/**
 * An `observation` item: a value (or `null`) of series `{kind}/{seriesKey}` at period `t`. The id
 * defaults to `{seriesKey}:{t}`, which keeps re-sends of the same point idempotent. `t` is a period
 * string or a `Date` (formatted as `YYYY-MM-DD` when `dateOnly` is true, else as an instant).
 */
export function observation(stream: string, options: CommonOptions & { id?: string; seriesKey: string;
  t: string | Date; value: number | null; knownAt?: TimeLike; unit?: string; dateOnly?: boolean }): ObservationItem {
  if (typeof options.value === "boolean") throw new TypeError("An observation value is a number or null");
  const t = typeof options.t === "string" ? options.t : options.dateOnly ? formatDate(options.t) : formatTime(options.t);
  const item = common(stream, "observation", options.id ?? `${options.seriesKey}:${t}`, options);
  item["seriesKey"] = options.seriesKey;
  item["t"] = t;
  item["value"] = options.value;
  return finish(item, { knownAt: formatTime(options.knownAt), unit: options.unit }, options.extra);
}

/** A document `author` object. */
export function author(handle?: string, name?: string, accountId?: string): JsonObject {
  return Object.fromEntries(Object.entries({ handle, name, accountId }).filter(([, v]) => v !== undefined));
}

/** A document `media` entry (links only). */
export function media(url: string, type?: string, thumbUrl?: string): JsonObject {
  const out: JsonObject = { url };
  if (type !== undefined) out["type"] = type;
  if (thumbUrl !== undefined) out["thumbUrl"] = thumbUrl;
  return out;
}

/** A `document` item (`text` at most 20,000 characters). */
export function document(stream: string, id: string, options: CommonOptions & { documentType: DocumentType; text: string;
  title?: string; url?: string; author?: JsonObject; language?: string; publishedAt?: TimeLike; about?: string[];
  media?: JsonObject[]; geometry?: Geometry; engagement?: Record<string, number | null> }): DocumentItem {
  const item = common(stream, "document", id, options);
  item["documentType"] = options.documentType;
  item["text"] = options.text;
  return finish(item, { title: options.title, url: options.url, author: options.author, language: options.language,
    publishedAt: formatTime(options.publishedAt), about: options.about, media: options.media,
    geometry: options.geometry, engagement: options.engagement }, options.extra);
}

/** A forecast `question` object (the provider's own question). */
export function question(externalId: string, title: string,
  options: { closeTime?: TimeLike; resolutionCriteria?: string } = {}): Question {
  const out: Question = { externalId, title };
  if (options.closeTime !== undefined) out.closeTime = formatTime(options.closeTime);
  if (options.resolutionCriteria !== undefined) out.resolutionCriteria = options.resolutionCriteria;
  return out;
}

/**
 * A `quantiles` object.
 *
 * @throws RangeError when the values decrease in quantile order
 */
export function quantiles(p05: number, p25: number, p50: number, p75: number, p95: number,
  options: { p10?: number; p90?: number } = {}): Quantiles {
  const out: Quantiles = { p05, p25, p50, p75, p95 };
  if (options.p10 !== undefined) out.p10 = options.p10;
  if (options.p90 !== undefined) out.p90 = options.p90;
  const ordered = [out.p05, out.p10, out.p25, out.p50, out.p75, out.p90, out.p95].filter((v): v is number => v !== undefined);
  for (let i = 1; i < ordered.length; i++) {
    if ((ordered[i] as number) < (ordered[i - 1] as number)) {
      throw new RangeError("Quantiles must be non-decreasing (p05 <= p10 <= p25 <= p50 <= p75 <= p90 <= p95)");
    }
  }
  return out;
}

/**
 * A `forecast` item: a `probability` (0-1) and/or `quantiles` for a `question` (the provider's) or a
 * `questionRef` (a Foresignal question id).
 *
 * @throws RangeError without a question or a value, with a probability outside 0-1, or with quantiles
 *     missing a required name
 */
export function forecast(stream: string, id: string, options: CommonOptions & { madeAt: TimeLike; question?: Question;
  questionRef?: string; probability?: number; quantiles?: Quantiles }): ForecastItem {
  if (options.question === undefined && !options.questionRef) throw new RangeError("A forecast needs a question or a questionRef");
  if (options.probability === undefined && options.quantiles === undefined) {
    throw new RangeError("A forecast needs a probability or quantiles");
  }
  if (options.probability !== undefined && !(options.probability >= 0 && options.probability <= 1)) {
    throw new RangeError("probability must be between 0 and 1");
  }
  const q = options.quantiles as Record<string, unknown> | undefined;
  if (q !== undefined && REQUIRED_QUANTILES.some((name) => typeof q[name] !== "number")) {
    throw new RangeError("quantiles need p05, p25, p50, p75 and p95");
  }
  const item = common(stream, "forecast", id, options);
  item["madeAt"] = formatTime(options.madeAt);
  return finish(item, { question: options.question, questionRef: options.questionRef,
    probability: options.probability, quantiles: options.quantiles }, options.extra);
}

/** A `delete` item: a tombstone for `{stream}:{id}`. */
export function deleteItem(stream: string, id: string, options: Omit<CommonOptions, "origin"> = {}): DeleteItem {
  return finish(common(stream, "delete", id, options), {}, options.extra);
}

/**
 * A `status` item: the source's health, or a stream's when `stream` is given (no record).
 *
 * @throws RangeError for an unknown health value
 */
export function status(health: Health, message?: string, options: { stream?: string; extra?: JsonObject } = {}): StatusItem {
  if (!(HEALTH_VALUES as readonly string[]).includes(health)) {
    throw new RangeError(`health must be one of ${HEALTH_VALUES.join(", ")}`);
  }
  const item = common(options.stream, "status", undefined, {});
  item["health"] = health;
  return finish(item, { message }, options.extra);
}

/** A fresh snapshot id. */
export function newSnapshotId(): string {
  return randomUUID().replace(/-/g, "");
}

/** A batch `snapshot` object; pages share the id, `page` counts from 1, the last page has `final: true`. */
export function snapshot(id: string, stream: string, page: number, final: boolean, scope: string | null = null): Snapshot {
  if (!Number.isInteger(page) || page < 1) throw new RangeError("Snapshot pages count from 1");
  return { id, stream, scope, page, final };
}

/**
 * A batch envelope. `sentAt` defaults to now; `nextCursor` is written when `includeNextCursor` is true
 * or a cursor is given (pull responses always carry it, null when caught up).
 */
export function batch(items: readonly unknown[], options: { protocol?: string; sourceId?: string; sentAt?: TimeLike;
  snapshot?: Snapshot | null; nextCursor?: string | null; includeNextCursor?: boolean } = {}): Batch {
  const out: Batch = { protocol: options.protocol ?? PROTOCOL_VERSION, items: [] };
  if (options.sourceId !== undefined) out.sourceId = options.sourceId;
  out.sentAt = formatTime(options.sentAt ?? new Date());
  if (options.snapshot) out.snapshot = { ...options.snapshot };
  out.items = [...items];
  if (options.includeNextCursor || (options.nextCursor !== undefined && options.nextCursor !== null)) {
    out.nextCursor = options.nextCursor ?? null;
  }
  return out;
}
