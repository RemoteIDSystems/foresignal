/** Constants of the Foresignal External Data Protocol 1.x (docs/External-Data-Protocol.md). */

/** The library version. Major and minor follow the EDP protocol version; the patch counts library releases. */
export const VERSION = "1.0.0";

/** The protocol version this library writes. */
export const PROTOCOL_VERSION = "foresignal-edp/1.0";

/** The highest `1.N` minor this library understands; a higher minor is refused as unsupported. */
export const MAX_MINOR = 0;

/** Every item type, in the order the specification lists them. */
export const ITEM_TYPES = ["feature", "event", "observation", "document", "forecast", "delete", "status"] as const;
/** An item type. */
export type ItemType = (typeof ITEM_TYPES)[number];

/** The item types a stream may declare as its `itemType`. */
export const STREAM_ITEM_TYPES = ["feature", "event", "observation", "document", "forecast"] as const;
/** A stream item type. */
export type StreamItemType = (typeof STREAM_ITEM_TYPES)[number];

/** Stream modes. */
export const MODES = ["layer", "atomic"] as const;
/** A stream mode. */
export type StreamMode = (typeof MODES)[number];

/** Declared stream geometries. */
export const STREAM_GEOMETRIES = ["Point", "LineString", "Polygon", "MultiPolygon", "any", "none"] as const;
/** A declared stream geometry. */
export type StreamGeometry = (typeof STREAM_GEOMETRIES)[number];

/** GeoJSON geometry types accepted on items (no GeometryCollection). */
export const GEOMETRY_TYPES = ["Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon"] as const;
/** A GeoJSON geometry type. */
export type GeometryType = (typeof GEOMETRY_TYPES)[number];

/** Analytic retention classes. */
export const RETENTION_CLASSES = ["event", "bulk-event", "series", "state", "track", "reference", "social"] as const;
/** A retention class. */
export type RetentionClass = (typeof RETENTION_CLASSES)[number];

/** Document types. */
export const DOCUMENT_TYPES = ["social-post", "article", "report", "transcript", "note"] as const;
/** A document type. */
export type DocumentType = (typeof DOCUMENT_TYPES)[number];

/** Health values of `status` items. */
export const HEALTH_VALUES = ["ok", "degraded", "blocked", "layout-changed", "geo-restricted"] as const;
/** A health value. */
export type Health = (typeof HEALTH_VALUES)[number];

/** Targets of suggested bindings (prompt 138). */
export const BINDING_TARGETS = [
  "series", "canonical-family", "social", "news", "evidence", "indicator", "forecast", "place", "situation", "chronicle",
] as const;
/** A binding target. */
export type BindingTarget = (typeof BINDING_TARGETS)[number];

/** Originator classes (evidence independence). */
export const ORIGINATOR_CLASSES = [
  "sensor-network", "agency", "aggregator", "media", "crowd", "social", "operator", "derived",
] as const;
/** An originator class. */
export type OriginatorClass = (typeof ORIGINATOR_CLASSES)[number];

/** How a provider gets its data. */
export const ACCESS_ROUTES = ["official", "unofficial-api", "scrape", "browser", "other"] as const;
/** An access route. */
export type AccessRoute = (typeof ACCESS_ROUTES)[number];

/** Values of `source.licence.redistribution` (the wire key keeps its spelling). */
export const REDISTRIBUTION_VALUES = ["allowed", "local-only", "unknown"] as const;
/** A redistribution value. */
export type Redistribution = (typeof REDISTRIBUTION_VALUES)[number];

/** Authentication styles of the pull and search endpoints. */
export const AUTH_STYLES = ["none", "bearer", "header", "basic"] as const;
/** An authentication style. */
export type AuthStyle = (typeof AUTH_STYLES)[number];

/** Forecast quantile names in order. */
export const QUANTILES = ["p05", "p10", "p25", "p50", "p75", "p90", "p95"] as const;
/** The required forecast quantiles. */
export const REQUIRED_QUANTILES = ["p05", "p25", "p50", "p75", "p95"] as const;

/** The names of the exported JSON Schemas. */
export const SCHEMA_NAMES = [
  "manifest", "batch", "item", "feature", "event", "observation", "document", "forecast", "delete", "status",
] as const;
/** An exported schema name. */
export type SchemaName = (typeof SCHEMA_NAMES)[number];

/** Per batch: 5 MB. */
export const MAX_BATCH_BYTES = 5 * 1024 * 1024;
/** Per batch: 10,000 items. */
export const MAX_ITEMS = 10_000;
/** Per batch: JSON nesting depth 32. */
export const MAX_DEPTH = 32;
/** A document's `text`: 20,000 characters. */
export const MAX_TEXT_LENGTH = 20_000;
/** Any other string: 2,048 characters. */
export const MAX_STRING_LENGTH = 2_048;
/** Per item: 20 media entries. */
export const MAX_MEDIA = 20;
/** Per item: 10,000 geometry vertices. */
export const MAX_VERTICES = 10_000;
/** A payload schema: 64 KB. */
export const MAX_SCHEMA_BYTES = 64 * 1024;
/** A `pattern` or `patternProperties` key: 256 characters. */
export const MAX_PATTERN_LENGTH = 256;
/** A pull `intervalSeconds` is at least 60. */
export const MIN_PULL_INTERVAL_SECONDS = 60;
/** How far ahead of receipt an `observedAt` may be, in milliseconds. */
export const MAX_FUTURE_MS = 5 * 60 * 1000;
/** The highest map zoom a presentation hint may name. */
export const MAX_ZOOM = 22;

/** Registered external sources are `ext-{source.id}`. */
export const SOURCE_PREFIX = "ext-";
/** The ingest-key header of pushes. */
export const INGEST_KEY_HEADER = "X-Foresignal-Ingest-Key";
/** The NDJSON media type. */
export const NDJSON_MEDIA_TYPE = "application/x-ndjson";
/** The JSON media type. */
export const JSON_MEDIA_TYPE = "application/json";
/** Where a provider conventionally publishes its manifest. */
export const WELL_KNOWN_MANIFEST_PATH = "/.well-known/foresignal-edp.json";
/** The page size Foresignal asks a pull provider for. */
export const DEFAULT_PULL_LIMIT = 500;
/** The default page size of a search. */
export const DEFAULT_SEARCH_LIMIT = 100;

/** A slug: `^[a-z0-9]+(-[a-z0-9]+)*$`. */
export const SLUG = /^[a-z0-9]+(-[a-z0-9]+)*$/;
/** A top-level payload field name. */
export const FIELD_NAME = /^[A-Za-z0-9_]{1,64}$/;
/** A `#RRGGBB` color. */
export const COLOR = /^#[0-9A-Fa-f]{6}$/;
const PROTOCOL = /^foresignal-edp\/1\.(0|[1-9][0-9]{0,8})$/;

/** Returns N of a `foresignal-edp/1.N` string, or -1 when it is not a valid 1.x protocol string. */
export function protocolMinor(protocol: unknown): number {
  if (typeof protocol !== "string") return -1;
  const match = PROTOCOL.exec(protocol);
  return match?.[1] !== undefined ? Number(match[1]) : -1;
}

/** Returns the id a Foresignal instance registers a provider under: `ext-{source.id}`. */
export function registeredSourceId(providerId: string): string {
  return SOURCE_PREFIX + providerId;
}
