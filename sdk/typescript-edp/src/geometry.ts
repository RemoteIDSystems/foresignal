/**
 * GeoJSON geometry builders and the EDP §3.3 geometry check: Point, MultiPoint, LineString,
 * MultiLineString, Polygon, MultiPolygon (no GeometryCollection); WGS84 longitude -180..180 and
 * latitude -90..90; two or three finite ordinates (the third is altitude in meters); closed linear
 * rings of at least four positions; at most 10,000 vertices; a type compatible with the stream's
 * declared `geometry`.
 */
import type { SchemaError } from "./errors.ts";
import { GEOMETRY_TYPES, MAX_VERTICES } from "./protocol.ts";
import type { GeometryType } from "./protocol.ts";

/** A position: longitude, latitude and optional altitude. */
export type Position = readonly number[];

/** A GeoJSON geometry object. */
export interface Geometry {
  /** The geometry type. */
  type: GeometryType;
  /** The coordinates, nested by type. */
  coordinates: unknown[];
}

const ALL: ReadonlySet<string> = new Set(GEOMETRY_TYPES);
const COMPATIBLE: Record<string, ReadonlySet<string>> = {
  Point: new Set(["Point", "MultiPoint"]),
  LineString: new Set(["LineString", "MultiLineString"]),
  Polygon: new Set(["Polygon", "MultiPolygon"]),
  MultiPolygon: new Set(["Polygon", "MultiPolygon"]),
  any: ALL,
  none: new Set<string>(),
};

/** A `Point` at `lon`, `lat` (and `alt` meters when given). */
export function point(lon: number, lat: number, alt?: number): Geometry {
  return { type: "Point", coordinates: alt === undefined ? [lon, lat] : [lon, lat, alt] };
}

/** A `MultiPoint`. */
export function multiPoint(positions: Iterable<Position>): Geometry {
  return { type: "MultiPoint", coordinates: [...positions].map((p) => [...p]) };
}

/** A `LineString` (at least two positions). */
export function lineString(positions: Iterable<Position>): Geometry {
  return { type: "LineString", coordinates: [...positions].map((p) => [...p]) };
}

/** A `MultiLineString`. */
export function multiLineString(lines: Iterable<Iterable<Position>>): Geometry {
  return { type: "MultiLineString", coordinates: [...lines].map((l) => [...l].map((p) => [...p])) };
}

function ring(positions: Iterable<Position>, close: boolean): number[][] {
  const out = [...positions].map((p) => [...p]);
  const first = out[0];
  const last = out[out.length - 1];
  if (close && first && last && (first[0] !== last[0] || first[1] !== last[1])) out.push([...first]);
  return out;
}

/** A `Polygon`; each ring is closed by repeating its first position unless `close` is false. */
export function polygon(rings: Iterable<Iterable<Position>>, close = true): Geometry {
  return { type: "Polygon", coordinates: [...rings].map((r) => ring(r, close)) };
}

/** A `MultiPolygon`; rings are closed as in {@link polygon}. */
export function multiPolygon(polygons: Iterable<Iterable<Iterable<Position>>>, close = true): Geometry {
  return { type: "MultiPolygon", coordinates: [...polygons].map((p) => [...p].map((r) => ring(r, close))) };
}

/** A rectangular `Polygon` (counterclockwise exterior ring), handy for `coverage.area`. */
export function bboxPolygon(minLon: number, minLat: number, maxLon: number, maxLat: number): Geometry {
  return polygon([[[minLon, minLat], [maxLon, minLat], [maxLon, maxLat], [minLon, maxLat]]]);
}

interface Counter {
  vertices: number;
}

function isNumber(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value);
}

function checkPosition(position: unknown, path: string, errors: SchemaError[], counter: Counter): void {
  counter.vertices++;
  if (!Array.isArray(position) || position.length < 2 || position.length > 3) {
    errors.push({ path, message: "a position has two or three numbers" });
    return;
  }
  if (!position.every(isNumber)) {
    errors.push({ path, message: "ordinates must be finite numbers" });
    return;
  }
  const [lon, lat] = position as number[];
  if (lon === undefined || lon < -180 || lon > 180) {
    errors.push({ path: `${path}/0`, message: "longitude must be between -180 and 180" });
  }
  if (lat === undefined || lat < -90 || lat > 90) {
    errors.push({ path: `${path}/1`, message: "latitude must be between -90 and 90" });
  }
}

function checkPositions(positions: unknown[], path: string, errors: SchemaError[], counter: Counter): void {
  for (let i = 0; i < positions.length; i++) {
    checkPosition(positions[i], `${path}/${i}`, errors, counter);
    if (counter.vertices > MAX_VERTICES) return;
  }
}

function checkLine(line: unknown[], path: string, errors: SchemaError[], counter: Counter): void {
  if (line.length < 2) errors.push({ path, message: "a line needs at least two positions" });
  checkPositions(line, path, errors, counter);
}

function samePosition(a: unknown, b: unknown): boolean {
  return Array.isArray(a) && Array.isArray(b) && a.length >= 2 && b.length >= 2 && a[0] === b[0] && a[1] === b[1];
}

function checkPolygon(rings: unknown[], path: string, errors: SchemaError[], counter: Counter): void {
  if (rings.length === 0) errors.push({ path, message: "a polygon needs at least one ring" });
  rings.forEach((r, i) => {
    const ringPath = `${path}/${i}`;
    if (!Array.isArray(r)) {
      errors.push({ path: ringPath, message: "must be an array" });
      return;
    }
    if (r.length < 4) errors.push({ path: ringPath, message: "a linear ring needs at least four positions" });
    const before = errors.length;
    checkPositions(r, ringPath, errors, counter);
    if (errors.length === before && r.length >= 2 && !samePosition(r[0], r[r.length - 1])) {
      errors.push({ path: ringPath, message: "a linear ring must be closed (first and last positions equal)" });
    }
  });
}

function each(array: unknown[], path: string, errors: SchemaError[],
  part: (child: unknown[], childPath: string) => void): void {
  if (array.length === 0) errors.push({ path, message: "must not be empty" });
  array.forEach((child, i) => {
    if (!Array.isArray(child)) {
      errors.push({ path: `${path}/${i}`, message: "must be an array" });
      return;
    }
    part(child, `${path}/${i}`);
  });
}

/**
 * Returns the problems of `geometry` for a stream declaring `declared` (`any` when absent). `null`
 * and `undefined` mean "no geometry" and are always acceptable here.
 */
export function validateGeometry(geometry: unknown, declared?: string | null, path = "/geometry"): SchemaError[] {
  const errors: SchemaError[] = [];
  if (geometry === null || geometry === undefined) return errors;
  const allowedKey = declared ?? "any";
  const allowed = COMPATIBLE[allowedKey] ?? ALL;
  if (typeof geometry !== "object" || Array.isArray(geometry)) {
    errors.push({ path, message: "must be a GeoJSON geometry object" });
    return errors;
  }
  const { type, coordinates } = geometry as { type?: unknown; coordinates?: unknown };
  if (typeof type !== "string" || !ALL.has(type)) {
    errors.push({ path: `${path}/type`,
      message: "must be Point, MultiPoint, LineString, MultiLineString, Polygon or MultiPolygon" });
    return errors;
  }
  if (!allowed.has(type)) {
    errors.push({ path: `${path}/type`, message: `${type} does not match the stream's declared geometry ${allowedKey}` });
    return errors;
  }
  const coords = `${path}/coordinates`;
  if (!Array.isArray(coordinates)) {
    errors.push({ path: coords, message: "must be an array" });
    return errors;
  }
  const counter: Counter = { vertices: 0 };
  switch (type) {
    case "Point":
      checkPosition(coordinates, coords, errors, counter);
      break;
    case "MultiPoint":
      each(coordinates, coords, errors, (c, p) => checkPosition(c, p, errors, counter));
      break;
    case "LineString":
      checkLine(coordinates, coords, errors, counter);
      break;
    case "MultiLineString":
      each(coordinates, coords, errors, (c, p) => checkLine(c, p, errors, counter));
      break;
    case "Polygon":
      checkPolygon(coordinates, coords, errors, counter);
      break;
    default:
      each(coordinates, coords, errors, (c, p) => checkPolygon(c, p, errors, counter));
  }
  if (counter.vertices > MAX_VERTICES) {
    errors.push({ path: coords, message: `has ${counter.vertices} vertices; the maximum is ${MAX_VERTICES}` });
  }
  return errors;
}
