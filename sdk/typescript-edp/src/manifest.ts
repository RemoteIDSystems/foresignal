/**
 * Manifest builder and validator (EDP §2, §3.1, §7).
 *
 * {@link ManifestBuilder} and {@link StreamBuilder} write a manifest with TypeScript names; the wire
 * keys stay as the protocol spells them (`licence` included). {@link validateManifest} performs the
 * checks Foresignal performs at registration: the exported manifest schema, the protocol minor, at
 * least one delivery mechanism, unique stream ids, layer streams carrying `feature` or `event` items
 * with a geometry, slugs, pull and search endpoints, field roles, presentation hints, TTLs and every
 * `payloadSchema` (vetted and compiled). Every problem is reported, not just the first.
 */
import { ManifestInvalidError, PayloadSchemaError } from "./errors.ts";
import type { SchemaError } from "./errors.ts";
import type { Geometry } from "./geometry.ts";
import { compilePayloadSchema } from "./payloadSchema.ts";
import type { PayloadValidator } from "./payloadSchema.ts";
import {
  COLOR, FIELD_NAME, MAX_MINOR, MAX_ZOOM, MIN_PULL_INTERVAL_SECONDS, PROTOCOL_VERSION, SLUG, protocolMinor,
  registeredSourceId,
} from "./protocol.ts";
import type {
  AccessRoute, AuthStyle, BindingTarget, DocumentType, OriginatorClass, Redistribution, RetentionClass,
  StreamGeometry, StreamItemType, StreamMode,
} from "./protocol.ts";
import { shapeErrors } from "./schemas.ts";

/** A JSON object. */
export type JsonObject = Record<string, unknown>;

const ISO_DURATION = /^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)(?:[.,]\d{0,9})?S)?)?$/i;
const SHORTHAND_DURATION = /^(\d+)\s*([smhdw])$/i;

/**
 * True for a non-negative ISO-8601 duration (`PT1H`, `P7D`; days, hours, minutes and seconds, as
 * `java.time.Duration` reads it) or the shorthand `30s`, `15m`, `12h`, `7d`, `1w`.
 */
export function isDuration(text: string): boolean {
  const trimmed = text.trim();
  if (SHORTHAND_DURATION.test(trimmed)) return true;
  const m = ISO_DURATION.exec(trimmed);
  if (!m || ![1, 2, 3, 4].some((i) => m[i] !== undefined)) return false;
  return !trimmed.toUpperCase().includes("T") || [2, 3, 4].some((i) => m[i] !== undefined);
}

/** True for an absolute `http` or `https` URL with a host. */
export function isHttpUrl(url: unknown): boolean {
  if (typeof url !== "string") return false;
  try {
    const parsed = new URL(url);
    return (parsed.protocol === "http:" || parsed.protocol === "https:") && parsed.hostname.length > 0
      && /^https?:\/\/[^/?#]/i.test(url);
  } catch {
    return false;
  }
}

/** The parts of a declared stream the item validator needs. */
export interface StreamInfo {
  /** The stream id. */
  id: string;
  /** `layer` or `atomic`. */
  mode: StreamMode;
  /** The item type the stream carries. */
  itemType: StreamItemType;
  /** The record kind. */
  kind: string;
  /** The declared geometry (`any` when absent). */
  geometry: StreamGeometry;
  /** `incremental` or `snapshot`. */
  update: "incremental" | "snapshot";
  /** The snapshot scope property, or null. */
  snapshotScope: string | null;
  /** True when the stream declares `observedAtMode: received`. */
  observedAtReceived: boolean;
}

/** The result of validating a manifest. */
export interface ManifestCheck {
  /** The manifest that was checked. */
  manifest: unknown;
  /** True when the manifest may be registered. */
  valid: boolean;
  /** Every problem; empty when valid. */
  errors: SchemaError[];
  /** True when the protocol minor is above the validator's `maxMinor` (`PROTOCOL_UNSUPPORTED`). */
  protocolUnsupported: boolean;
  /** The protocol minor (-1 when invalid). */
  minor: number;
  /** `source.id`, when present. */
  providerId: string | null;
  /** The declared streams by id (valid manifests only). */
  streams: Map<string, StreamInfo>;
  /** The compiled payload schemas by stream id (valid manifests only). */
  payloadValidators: Map<string, PayloadValidator>;
}

function isObject(value: unknown): value is JsonObject {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function text(node: unknown, name: string): string | null {
  if (!isObject(node)) return null;
  const value = node[name];
  if (value === null || value === undefined || typeof value === "object") return null;
  return typeof value === "string" ? value : JSON.stringify(value);
}

function strings(node: unknown): string[] {
  return Array.isArray(node) ? node.filter((v): v is string => typeof v === "string") : [];
}

function isInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isInteger(value);
}

function checkSource(source: unknown, errors: SchemaError[]): void {
  if (!isObject(source)) return;
  const id = text(source, "id");
  if (id !== null && (!SLUG.test(id) || id.length > 48)) {
    errors.push({ path: "/source/id", message: "must be a slug of at most 48 characters" });
  }
  const originator = text(source["originator"], "id");
  if (originator !== null && !SLUG.test(originator)) {
    errors.push({ path: "/source/originator/id", message: "must be a slug" });
  }
}

function checkEndpoint(endpoint: JsonObject, urlField: string, path: string, errors: SchemaError[]): void {
  if (!isHttpUrl(text(endpoint, urlField))) {
    errors.push({ path: `${path}/${urlField}`, message: "must be an absolute http or https URL" });
  }
  const auth = text(endpoint, "auth");
  if (auth !== null && !["none", "bearer", "header", "basic"].includes(auth)) {
    errors.push({ path: `${path}/auth`, message: "must be none, bearer, header or basic" });
  }
  if (auth === "header" && text(endpoint, "header") === null) {
    errors.push({ path: `${path}/header`, message: "is required when auth is header" });
  }
}

function checkTransport(transport: unknown, errors: SchemaError[]): void {
  if (!isObject(transport)) {
    errors.push({ path: "/transport", message: "is required" });
    return;
  }
  const push = transport["push"] === true;
  const pull = transport["pull"];
  if (!push && !isObject(pull)) {
    errors.push({ path: "/transport",
      message: "must declare at least one delivery mechanism: push: true or a pull endpoint" });
  }
  if (isObject(pull)) {
    checkEndpoint(pull, "itemsUrl", "/transport/pull", errors);
    const interval = pull["intervalSeconds"];
    if (interval !== undefined && interval !== null && (!isInteger(interval) || interval < MIN_PULL_INTERVAL_SECONDS)) {
      errors.push({ path: "/transport/pull/intervalSeconds", message: "must be an integer of at least 60" });
    }
  }
  const search = transport["search"];
  if (isObject(search)) checkEndpoint(search, "url", "/transport/search", errors);
}

function checkNames(names: string[], path: string, errors: SchemaError[]): void {
  names.forEach((name, i) => {
    if (!FIELD_NAME.test(name)) errors.push({ path: `${path}/${i}`, message: "must be a top-level payload name" });
  });
}

function checkFields(fields: unknown, path: string, errors: SchemaError[]): void {
  if (!isObject(fields)) return;
  const title = text(fields, "title");
  if (title !== null && !FIELD_NAME.test(title)) {
    errors.push({ path: `${path}/title`, message: "must be a top-level payload name" });
  }
  const significant = strings(fields["significant"]);
  const volatile = strings(fields["volatile"]);
  checkNames(significant, `${path}/significant`, errors);
  checkNames(volatile, `${path}/volatile`, errors);
  for (const name of significant) {
    if (volatile.includes(name)) errors.push({ path, message: `'${name}' is both significant and volatile` });
  }
}

function checkPresentation(presentation: unknown, path: string, errors: SchemaError[]): void {
  if (!isObject(presentation)) return;
  const color = text(presentation, "color");
  if (color !== null && !COLOR.test(color)) errors.push({ path: `${path}/color`, message: "must be #RRGGBB" });
  for (const zoom of ["minZoom", "maxZoom"]) {
    const value = presentation[zoom];
    if (value !== undefined && value !== null && (!isInteger(value) || value < 0 || value > MAX_ZOOM)) {
      errors.push({ path: `${path}/${zoom}`, message: `must be an integer from 0 to ${MAX_ZOOM}` });
    }
  }
  const label = text(presentation, "labelField");
  if (label !== null && !FIELD_NAME.test(label)) {
    errors.push({ path: `${path}/labelField`, message: "must be a top-level payload name" });
  }
  checkNames(strings(presentation["tileProperties"]), `${path}/tileProperties`, errors);
}

function checkStream(stream: unknown, path: string, ids: Set<string>, validators: Map<string, PayloadValidator>,
  errors: SchemaError[]): void {
  if (!isObject(stream)) return;
  const id = text(stream, "id");
  if (id !== null) {
    if (!SLUG.test(id)) errors.push({ path: `${path}/id`, message: "must be a slug" });
    if (ids.has(id)) errors.push({ path: `${path}/id`, message: `duplicate stream id '${id}'` });
    ids.add(id);
  }
  const kind = text(stream, "kind");
  if (kind !== null && !SLUG.test(kind)) errors.push({ path: `${path}/kind`, message: "must be a slug" });
  const mode = text(stream, "mode");
  const itemType = text(stream, "itemType");
  if (mode === "layer") {
    if (itemType !== null && itemType !== "feature" && itemType !== "event") {
      errors.push({ path: `${path}/itemType`, message: "a layer stream carries feature or event items" });
    }
    if (text(stream, "geometry") === "none") {
      errors.push({ path: `${path}/geometry`, message: "a layer stream must have a geometry" });
    }
  }
  const ttl = text(stream, "ttl");
  if (ttl !== null && !isDuration(ttl)) errors.push({ path: `${path}/ttl`, message: "must be an ISO-8601 duration" });
  if (text(stream, "snapshotScope") !== null && text(stream, "update") !== "snapshot") {
    errors.push({ path: `${path}/snapshotScope`, message: "only snapshot streams have a snapshot scope" });
  }
  checkFields(stream["fields"], `${path}/fields`, errors);
  checkPresentation(stream["presentation"], `${path}/presentation`, errors);
  const schema = stream["payloadSchema"];
  if (schema !== undefined && schema !== null) {
    try {
      const compiled = compilePayloadSchema(schema);
      if (id !== null) validators.set(id, compiled);
    } catch (e) {
      if (!(e instanceof PayloadSchemaError)) throw e;
      for (const error of e.errors) errors.push({ path: `${path}/payloadSchema${error.path}`, message: error.message });
    }
  }
}

function streamInfo(stream: JsonObject): StreamInfo {
  const fields = stream["fields"];
  return {
    id: stream["id"] as string,
    mode: stream["mode"] as StreamMode,
    itemType: stream["itemType"] as StreamItemType,
    kind: stream["kind"] as string,
    geometry: (typeof stream["geometry"] === "string" ? stream["geometry"] : "any") as StreamGeometry,
    update: stream["update"] === "snapshot" ? "snapshot" : "incremental",
    snapshotScope: typeof stream["snapshotScope"] === "string" ? stream["snapshotScope"] : null,
    observedAtReceived: isObject(fields) && fields["observedAtMode"] === "received",
  };
}

/** Options of {@link validateManifest}. */
export interface ValidateManifestOptions {
  /** The highest `foresignal-edp/1.N` minor to accept (an instance reports its `maxMinor`; 0 for EDP 1.0). */
  maxMinor?: number;
}

/** Validates `manifest`. Never throws for invalid input; the problems are in the result. */
export function validateManifest(manifest: unknown, options: ValidateManifestOptions = {}): ManifestCheck {
  const maxMinor = options.maxMinor ?? MAX_MINOR;
  const empty = { streams: new Map<string, StreamInfo>(), payloadValidators: new Map<string, PayloadValidator>() };
  if (!isObject(manifest)) {
    return { manifest, valid: false, errors: [{ path: "", message: "the manifest must be a JSON object" }],
      protocolUnsupported: false, minor: -1, providerId: null, ...empty };
  }
  const errors: SchemaError[] = [];
  let protocolUnsupported = false;
  const minor = protocolMinor(manifest["protocol"]);
  if (minor < 0) {
    errors.push({ path: "/protocol", message: "must be foresignal-edp/1.N" });
  } else if (minor > maxMinor) {
    protocolUnsupported = true;
    errors.push({ path: "/protocol",
      message: `foresignal-edp/1.${minor} is newer than this validator supports (highest minor ${maxMinor})` });
  }
  errors.push(...shapeErrors("manifest", manifest));
  checkTransport(manifest["transport"], errors);
  checkSource(manifest["source"], errors);
  const validators = new Map<string, PayloadValidator>();
  const ids = new Set<string>();
  const streams = manifest["streams"];
  if (Array.isArray(streams)) streams.forEach((s, i) => checkStream(s, `/streams/${i}`, ids, validators, errors));
  const unique = [...new Map(errors.map((e) => [`${e.path}\u0000${e.message}`, e])).values()];
  const providerId = text(manifest["source"], "id");
  if (unique.length > 0) {
    return { manifest, valid: false, errors: unique, protocolUnsupported, minor, providerId, ...empty };
  }
  const infos = new Map<string, StreamInfo>();
  for (const stream of streams as JsonObject[]) infos.set(stream["id"] as string, streamInfo(stream));
  return { manifest, valid: true, errors: [], protocolUnsupported, minor, providerId, streams: infos,
    payloadValidators: validators };
}

/** True when `value` is a {@link ManifestCheck}. */
export function isManifestCheck(value: unknown): value is ManifestCheck {
  return isObject(value) && value["streams"] instanceof Map && Array.isArray(value["errors"]) && "manifest" in value;
}

/**
 * Returns `manifest` when it is already a valid {@link ManifestCheck}, else validates it.
 *
 * @throws ManifestInvalidError when the manifest is invalid
 */
export function asManifestCheck(manifest: ManifestCheck | JsonObject, options: ValidateManifestOptions = {}): ManifestCheck {
  const check = isManifestCheck(manifest) ? manifest : validateManifest(manifest, options);
  if (!check.valid) throw new ManifestInvalidError(check.errors, check.protocolUnsupported);
  return check;
}

function dropUndefined<T extends JsonObject>(value: T): T {
  return Object.fromEntries(Object.entries(value).filter(([, v]) => v !== undefined)) as T;
}

/** Builds one `streams[]` entry. Every setter returns `this`. */
export class StreamBuilder {
  private readonly data: JsonObject;

  /** Starts a stream with its required members. */
  constructor(id: string, options: { mode: StreamMode; itemType: StreamItemType; kind: string }) {
    this.data = { id, mode: options.mode, itemType: options.itemType, kind: options.kind };
  }

  /** Sets `title`. */
  title(title: string | null): this { this.data["title"] = title; return this; }

  /** Sets `description`. */
  description(description: string | null): this { this.data["description"] = description; return this; }

  /** Sets `geometry`: `Point`, `LineString`, `Polygon`, `MultiPolygon`, `any` or `none`. */
  geometry(geometry: StreamGeometry): this { this.data["geometry"] = geometry; return this; }

  /** Sets `update: incremental` (items are changes; only `delete` items remove anything). */
  incremental(): this {
    this.data["update"] = "incremental";
    delete this.data["snapshotScope"];
    return this;
  }

  /** Sets `update: snapshot` and `snapshotScope` (null: the whole stream is one scope). */
  snapshot(scopeProperty: string | null = null): this {
    this.data["update"] = "snapshot";
    this.data["snapshotScope"] = scopeProperty;
    return this;
  }

  /** Sets `payloadSchema` (JSON Schema 2020-12 for the items' `properties`). */
  payloadSchema(schema: JsonObject | null): this {
    this.data["payloadSchema"] = schema === null ? null : structuredClone(schema);
    return this;
  }

  /** Sets `fields`: the title field, significant and volatile fields, and `observedAtMode`. */
  fields(fields: { title?: string; significant?: string[]; volatile?: string[];
    observedAtMode?: "upstream" | "received" }): this {
    this.data["fields"] = dropUndefined({ title: fields.title, significant: fields.significant ?? [],
      volatile: fields.volatile ?? [], observedAtMode: fields.observedAtMode });
    return this;
  }

  /** Sets the record `ttl` (ISO-8601 duration such as `PT1H`). */
  ttl(ttl: string | null): this { this.data["ttl"] = ttl; return this; }

  /** Sets `retentionClass`. */
  retentionClass(retentionClass: RetentionClass): this { this.data["retentionClass"] = retentionClass; return this; }

  /** Sets `unit` (observation streams). */
  unit(unit: string | null): this { this.data["unit"] = unit; return this; }

  /** Sets `documentType` (document streams). */
  documentType(documentType: DocumentType | null): this { this.data["documentType"] = documentType; return this; }

  /** Sets the layer `presentation` hints. */
  presentation(presentation: { color?: string; icon?: string; labelField?: string; minZoom?: number;
    maxZoom?: number; tileProperties?: string[] }): this {
    this.data["presentation"] = dropUndefined({ ...presentation });
    return this;
  }

  /** Sets the declared `coverage`: footprint geometry, canonical family and detection probability. */
  coverage(coverage: { area?: Geometry | null; family?: string | null; pDetect?: number | null }): this {
    this.data["coverage"] = { area: coverage.area ?? null, family: coverage.family ?? null,
      pDetect: coverage.pDetect ?? null };
    return this;
  }

  /** Sets `complete`: the stream is a complete record of its scope. */
  complete(complete = true): this { this.data["complete"] = complete; return this; }

  /** Appends a `suggestedBindings` entry. */
  suggestBinding(target: BindingTarget, config?: JsonObject): this {
    const list = (this.data["suggestedBindings"] as JsonObject[] | undefined) ?? [];
    list.push(config === undefined ? { target } : { target, config: { ...config } });
    this.data["suggestedBindings"] = list;
    return this;
  }

  /** Sets any other member by its wire name (for later 1.x additions). */
  set(name: string, value: unknown): this { this.data[name] = value; return this; }

  /** Returns the stream object, without members set to `null` except `snapshotScope` of a snapshot stream. */
  build(): JsonObject {
    const out: JsonObject = {};
    for (const [key, value] of Object.entries(this.data)) {
      if (value !== null && value !== undefined) out[key] = value;
      else if (key === "snapshotScope" && this.data["update"] === "snapshot") out[key] = null;
    }
    return structuredClone(out);
  }
}

/** Builds a manifest. Every setter returns `this`; {@link build} validates by default. */
export class ManifestBuilder {
  private readonly protocol: string;
  private readonly source: JsonObject;
  private readonly transport: JsonObject = {};
  private readonly streams: JsonObject[] = [];
  private readonly extra: JsonObject = {};

  /** Starts a manifest for provider `sourceId` (a slug; registered as `ext-{sourceId}`). */
  constructor(sourceId: string, name: string, protocol: string = PROTOCOL_VERSION) {
    this.protocol = protocol;
    this.source = { id: sourceId, name };
  }

  /** The id an instance registers this provider under (`ext-{source.id}`). */
  get registeredId(): string {
    return registeredSourceId(this.source["id"] as string);
  }

  /** Sets `source.description`. */
  description(description: string | null): this { this.source["description"] = description; return this; }

  /** Sets `source.version` (the provider's own version). */
  version(version: string | null): this { this.source["version"] = version; return this; }

  /** Sets `source.publisher`. */
  publisher(name: string, options: { url?: string; contact?: string } = {}): this {
    this.source["publisher"] = dropUndefined({ name, url: options.url, contact: options.contact });
    return this;
  }

  /** Sets `source.homepage`. */
  homepage(homepage: string | null): this { this.source["homepage"] = homepage; return this; }

  /** Sets the source license (wire key `source.licence`); `redistribution` sets the default egress. */
  license(license: { id?: string; url?: string; attribution?: string; redistribution?: Redistribution }): this {
    this.source["licence"] = dropUndefined({ id: license.id, url: license.url, attribution: license.attribution,
      redistribution: license.redistribution ?? "unknown" });
    return this;
  }

  /** Sets `source.terms`. */
  terms(summary: string | null, url: string | null = null): this {
    this.source["terms"] = { summary, url };
    return this;
  }

  /** Sets `source.access`: how the provider gets its data and how likely it is to break. */
  access(route: AccessRoute, fragility?: "low" | "medium" | "high"): this {
    this.source["access"] = dropUndefined({ route, fragility });
    return this;
  }

  /** Sets `source.originator` (who originally observed the data, for evidence independence). */
  originator(id: string, originatorClass: OriginatorClass, aggregates?: string[]): this {
    this.source["originator"] = aggregates === undefined ? { id, class: originatorClass }
      : { id, class: originatorClass, aggregates: [...aggregates] };
    return this;
  }

  /** Sets `source.private` (the data is personal). */
  markPrivate(isPrivate = true): this { this.source["private"] = isPrivate; return this; }

  /** Declares `transport.push`. */
  push(enabled = true): this { this.transport["push"] = enabled; return this; }

  /**
   * Declares `transport.pull`. The credential itself is entered by the operator in Foresignal, never
   * put in the manifest.
   */
  pull(itemsUrl: string, options: { intervalSeconds?: number; auth?: AuthStyle; header?: string } = {}): this {
    this.transport["pull"] = dropUndefined({ itemsUrl, intervalSeconds: options.intervalSeconds, auth: options.auth,
      header: options.header });
    return this;
  }

  /** Declares the optional `transport.search` endpoint. */
  search(url: string, options: { auth?: AuthStyle; header?: string } = {}): this {
    this.transport["search"] = dropUndefined({ url, auth: options.auth, header: options.header });
    return this;
  }

  /** Appends a stream (a {@link StreamBuilder} or a ready stream object). */
  stream(stream: StreamBuilder | JsonObject): this {
    this.streams.push(stream instanceof StreamBuilder ? stream.build() : structuredClone(stream));
    return this;
  }

  /** Sets any other top-level member by its wire name. */
  set(name: string, value: unknown): this { this.extra[name] = value; return this; }

  /** Returns the manifest without validating it. */
  toJSON(): JsonObject {
    const source = Object.fromEntries(Object.entries(this.source).filter(([, v]) => v !== null && v !== undefined));
    return structuredClone({ protocol: this.protocol, source, transport: this.transport, streams: this.streams,
      ...this.extra });
  }

  /** Validates the manifest built so far. */
  validate(options: ValidateManifestOptions = {}): ManifestCheck {
    return validateManifest(this.toJSON(), options);
  }

  /**
   * Returns the manifest.
   *
   * @throws ManifestInvalidError when `validate` is true (the default) and the manifest is invalid
   */
  build(options: ValidateManifestOptions & { validate?: boolean } = {}): JsonObject {
    const manifest = this.toJSON();
    if (options.validate !== false) asManifestCheck(manifest, options);
    return manifest;
  }
}
