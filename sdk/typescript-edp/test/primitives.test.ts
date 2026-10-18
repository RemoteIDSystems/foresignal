/** Times, periods, geometry, payload-schema vetting, NDJSON and schema loading. */
import { describe, expect, it } from "vitest";
import {
  BatchFormatError, PayloadSchemaError, compilePayloadSchema, decodeBatch, decodeJson, decodeNdjson, encodeJson,
  encodeNdjson, formatTime, isoWeek, loadSchema, month, parsePeriod, parseRfc3339, protocolMinor, quarter,
  registeredSourceId, validateGeometry, vetPayloadSchema,
} from "../src/index.ts";

const ms = (iso: string): number => new Date(iso).getTime();
const text = (bytes: Uint8Array): string => new TextDecoder().decode(bytes);

describe("times", () => {
  it("parses RFC 3339 with an offset", () => {
    expect(parseRfc3339("2026-10-11T18:00:00Z")).toBe(ms("2026-10-11T18:00:00Z"));
    expect(parseRfc3339("2026-10-11T20:00+02:00")).toBe(ms("2026-10-11T18:00:00Z"));
    expect(parseRfc3339("2026-10-11t18:00:00.123456789z")).toBe(ms("2026-10-11T18:00:00.123Z"));
    for (const bad of ["2026-10-11", "2026-02-30T00:00:00Z", "2026-10-11T24:00:00Z", "2026-10-11T18:00:60Z",
      "2026-10-11T18:00:00", "2026-10-11T18:00:00+19:00", null, 5]) {
      expect(parseRfc3339(bad)).toBeNull();
    }
  });

  it("formats times", () => {
    expect(formatTime(new Date("2026-10-11T18:00:00Z"))).toBe("2026-10-11T18:00:00Z");
    expect(formatTime(new Date("2026-10-11T18:00:00.250Z"))).toBe("2026-10-11T18:00:00.250Z");
    expect(formatTime("as-is")).toBe("as-is");
    expect(formatTime(undefined)).toBeUndefined();
    expect(() => formatTime(new Date(Number.NaN))).toThrow(TypeError);
  });
});

describe("periods", () => {
  it.each([
    ["2026", "year", "2026-01-01", "2027-01-01"],
    ["2026-Q4", "quarter", "2026-10-01", "2027-01-01"],
    ["2026-Q1", "quarter", "2026-01-01", "2026-04-01"],
    ["2026-12", "month", "2026-12-01", "2027-01-01"],
    ["2026-W01", "week", "2025-12-29", "2026-01-05"],
    ["2026-10-11", "day", "2026-10-11", "2026-10-12"],
  ])("parses %s", (t, granularity, start, end) => {
    expect(parsePeriod(t)).toEqual({ granularity, start: ms(`${start}T00:00:00Z`), end: ms(`${end}T00:00:00Z`) });
  });

  it("parses instants and refuses invalid periods", () => {
    const instant = parsePeriod("2026-10-11T18:00:00+00:00");
    expect(instant?.granularity).toBe("instant");
    expect(instant?.start).toBe(instant?.end);
    for (const bad of ["2026-13", "2026-W00", "2025-W53", "2026-02-29", "26", "2026-10-11T18:00", null]) {
      expect(parsePeriod(bad)).toBeNull();
    }
  });

  it("formats periods", () => {
    expect(quarter(2026, 4)).toBe("2026-Q4");
    expect(month(2026, 3)).toBe("2026-03");
    expect(isoWeek(2026, 53)).toBe("2026-W53");
    expect(() => quarter(2026, 5)).toThrow(RangeError);
    expect(() => month(2026, 0)).toThrow(RangeError);
    expect(() => isoWeek(2025, 53)).toThrow(RangeError);
  });
});

describe("geometry", () => {
  const ring = [[0, 0], [1, 0], [1, 1], [0, 0]];

  it("accepts every type", () => {
    for (const geometry of [
      { type: "Point", coordinates: [10, 20, 5] }, { type: "MultiPoint", coordinates: [[0, 0], [1, 1]] },
      { type: "LineString", coordinates: [[0, 0], [1, 1]] }, { type: "MultiLineString", coordinates: [[[0, 0], [1, 1]]] },
      { type: "Polygon", coordinates: [ring] }, { type: "MultiPolygon", coordinates: [[ring]] },
    ]) {
      expect(validateGeometry(geometry)).toEqual([]);
    }
    expect(validateGeometry(null, "none")).toEqual([]);
    expect(validateGeometry({ type: "MultiPolygon", coordinates: [[ring]] }, "Polygon")).toEqual([]);
  });

  it.each([
    ["x", null, "/geometry", "GeoJSON geometry object"],
    [{ type: "GeometryCollection", geometries: [] }, null, "/geometry/type", "must be Point"],
    [{ type: "Polygon", coordinates: [[[0, 0], [1, 0], [1, 1], [0, 0]]] }, "Point", "/geometry/type", "does not match"],
    [{ type: "Point", coordinates: [0, 0] }, "none", "/geometry/type", "does not match"],
    [{ type: "Point", coordinates: "x" }, null, "/geometry/coordinates", "must be an array"],
    [{ type: "Point", coordinates: [0] }, null, "/geometry/coordinates", "two or three numbers"],
    [{ type: "Point", coordinates: [0, "a"] }, null, "/geometry/coordinates", "finite numbers"],
    [{ type: "Point", coordinates: [0, 91] }, null, "/geometry/coordinates/1", "latitude"],
    [{ type: "Point", coordinates: [-181, 0] }, null, "/geometry/coordinates/0", "longitude"],
    [{ type: "MultiPoint", coordinates: [] }, null, "/geometry/coordinates", "must not be empty"],
    [{ type: "MultiPoint", coordinates: [5] }, null, "/geometry/coordinates/0", "must be an array"],
    [{ type: "Polygon", coordinates: [] }, null, "/geometry/coordinates", "at least one ring"],
    [{ type: "Polygon", coordinates: [[[0, 0], [1, 0], [0, 0]]] }, null, "/geometry/coordinates/0", "at least four"],
    [{ type: "Polygon", coordinates: [[[0, 0], [1, 0], [1, 1], [0, 1]]] }, null, "/geometry/coordinates/0", "must be closed"],
  ])("reports %j", (geometry, declared, path, message) => {
    const errors = validateGeometry(geometry, declared);
    expect(errors.some((e) => e.path === path && e.message.includes(message)), JSON.stringify(errors)).toBe(true);
  });

  it("caps vertices at 10,000", () => {
    const line = Array.from({ length: 10_001 }, (_, i) => [0, i * 0.0001]);
    expect(validateGeometry({ type: "LineString", coordinates: line }).some((e) => e.message.includes("maximum is 10000"))).toBe(true);
    expect(validateGeometry({ type: "LineString", coordinates: line.slice(0, 10_000) })).toEqual([]);
  });
});

describe("payload schemas", () => {
  it("compiles and validates an acceptable schema", () => {
    const validator = compilePayloadSchema({ $schema: "https://json-schema.org/draft/2020-12/schema", type: "object",
      $defs: { name: { type: "string", pattern: "^[A-Z]" } }, properties: { name: { $ref: "#/$defs/name" } }, required: ["name"] });
    expect(validator.validate({ name: "Kaub" })).toEqual([]);
    expect(validator.validate({ name: "kaub" })[0]?.path).toBe("/name");
    expect(validator.validate(undefined).length).toBeGreaterThan(0);
  });

  it.each([
    [[], ""],
    [{ $ref: "other.json#/x" }, "/$ref"],
    [{ properties: { a: { $recursiveRef: "#" } } }, "/properties/a/$recursiveRef"],
    [{ properties: { a: { pattern: "(a)\\1" } } }, "/properties/a/pattern"],
    [{ properties: { a: { pattern: "(?<!x)y" } } }, "/properties/a/pattern"],
    [{ properties: { a: { pattern: "a++" } } }, "/properties/a/pattern"],
    [{ properties: { a: { pattern: "(" } } }, "/properties/a/pattern"],
    [{ patternProperties: { "x/y": {}, "(?!z)": {} } }, "/patternProperties/(?!z)"],
  ])("vets %j", (schema, path) => {
    expect(vetPayloadSchema(schema).some((p) => p.path === path)).toBe(true);
    expect(() => compilePayloadSchema(schema)).toThrow(PayloadSchemaError);
  });

  it("reports metaschema errors and unresolvable local references", () => {
    expect(() => compilePayloadSchema({ type: 5 })).toThrow(PayloadSchemaError);
    expect(() => compilePayloadSchema({ $ref: "#/$defs/missing" })).toThrow(PayloadSchemaError);
  });
});

describe("NDJSON", () => {
  it("round-trips a header and items", () => {
    const items = [{ stream: "s", type: "delete", id: "1" }, { type: "status", health: "ok" }];
    const body = encodeNdjson({ protocol: "foresignal-edp/1.0", sourceId: "a", items: ["dropped"] }, items);
    const lines = text(body).split("\n");
    expect(lines).toHaveLength(4);
    expect(lines[3]).toBe("");
    expect(lines[0]).not.toContain("items");
    expect(decodeNdjson(body)).toEqual({ protocol: "foresignal-edp/1.0", sourceId: "a", items });
    expect(decodeBatch(body, "application/x-ndjson; charset=utf-8")).toEqual(decodeNdjson(body));
  });

  it("decodes header-less bodies with blank lines", () => {
    expect(decodeNdjson('{"stream":"s","type":"delete","id":"1"}\n\n{"stream":"s","type":"delete","id":"2"}\r\n'))
      .toEqual({ items: [{ stream: "s", type: "delete", id: "1" }, { stream: "s", type: "delete", id: "2" }] });
    expect(text(encodeNdjson(null, [{ a: 1 }]))).toBe('{"a":1}\n');
  });

  it("refuses unreadable bodies", () => {
    expect(() => decodeNdjson('{"protocol":"foresignal-edp/1.0"}\n[1]')).toThrow(/Line 2/);
    expect(() => decodeNdjson("{nope")).toThrow(BatchFormatError);
    expect(() => decodeJson(`{"items": ${"[".repeat(40)}${"]".repeat(40)}}`)).toThrow(/deeper/);
    expect(() => decodeJson("[]")).toThrow(BatchFormatError);
    expect(() => decodeJson('{"items": {}}')).toThrow(BatchFormatError);
    expect(() => decodeJson(new Uint8Array([0xff, 0xfe]))).toThrow(BatchFormatError);
    expect(decodeJson('{"protocol":"foresignal-edp/1.0"}').items).toEqual([]);
    expect(decodeBatch(encodeJson({ protocol: "p", items: [] }))).toEqual({ protocol: "p", items: [] });
  });
});

describe("protocol helpers", () => {
  it("loads schemas and reads protocol strings", () => {
    expect(String(loadSchema("manifest")["$id"])).toMatch(/manifest\.schema\.json$/);
    expect(() => loadSchema("nope" as never)).toThrow(RangeError);
    expect(protocolMinor("foresignal-edp/1.12")).toBe(12);
    expect(protocolMinor("foresignal-edp/2.0")).toBe(-1);
    expect(protocolMinor("foresignal-edp/1.01")).toBe(-1);
    expect(protocolMinor(undefined)).toBe(-1);
    expect(registeredSourceId("abc")).toBe("ext-abc");
  });
});
