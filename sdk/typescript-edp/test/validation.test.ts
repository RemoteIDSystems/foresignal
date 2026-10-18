/** Item and batch validation beyond the conformance fixtures: every rejection code and batch-level refusal. */
import { describe, expect, it } from "vitest";
import { ManifestInvalidError, forecastProblem, observation, validateBatch, validateItems, validateSnapshotHeader } from "../src/index.ts";
import { minimalManifest } from "./helpers.ts";

const NOW = new Date("2026-10-11T18:00:00Z");

function obs(overrides: Record<string, unknown> = {}, remove: string[] = []): Record<string, unknown> {
  const item: Record<string, unknown> = { stream: "s", type: "observation", id: "a", observedAt: "2026-10-11T17:00:00Z",
    seriesKey: "k", t: "2026-10-10", value: 1, ...overrides };
  for (const key of remove) delete item[key];
  return item;
}

function reasons(items: unknown[], manifest: Record<string, unknown> = minimalManifest()): string[] {
  return validateItems(items, manifest, { receivedAt: NOW }).rejected.map((r) => r.reason);
}

describe("validateItems", () => {
  it("refuses non-objects and unknown types", () => {
    expect(reasons(["text", obs({ type: "widget" })])).toEqual(["SCHEMA_INVALID", "SCHEMA_INVALID"]);
  });

  it("refuses an item type the stream does not carry", () => {
    expect(reasons([{ stream: "s", type: "event", id: "e", observedAt: "2026-10-11T17:00:00Z",
      eventTime: "2026-10-11T17:00:00Z" }])).toEqual(["ITEM_TYPE_MISMATCH"]);
  });

  it("treats a stream-less status as source health and refuses unknown streams", () => {
    const check = validateItems([{ type: "status", health: "ok" }, { type: "status", health: "ok", stream: "x" }],
      minimalManifest(), { receivedAt: NOW });
    expect(check.valid).toHaveLength(1);
    expect(check.valid[0]?.stream).toBeNull();
    expect(check.rejected.map((r) => r.reason)).toEqual(["UNKNOWN_STREAM"]);
  });

  it("enforces string, text and media limits", () => {
    expect(reasons([obs({ properties: { note: "x".repeat(2049) } }), obs({ properties: { ["k".repeat(2049)]: 1 } })]))
      .toEqual(["LIMIT_EXCEEDED", "LIMIT_EXCEEDED"]);
    const manifest = minimalManifest({ itemType: "document" });
    const base = { stream: "s", type: "document", observedAt: "2026-10-11T17:00:00Z", documentType: "note" };
    expect(reasons([
      { ...base, id: "long", text: "x".repeat(20_001) },
      { ...base, id: "media", text: "ok", media: Array.from({ length: 21 }, (_, i) => ({ url: `https://e.org/${i}` })) },
      { ...base, id: "limit", text: "x".repeat(20_000) },
    ], manifest)).toEqual(["SCHEMA_INVALID", "LIMIT_EXCEEDED"]);
  });

  it("checks times", () => {
    expect(reasons([obs({ observedAt: "2026-10-11 17:00" })])).toEqual(["SCHEMA_INVALID"]);
    expect(reasons([obs({ knownAt: "yesterday" })])).toEqual(["SCHEMA_INVALID"]);
    expect(reasons([obs({ observedAt: "2026-10-11T18:05:00Z" })])).toEqual([]);
    expect(reasons([obs({ observedAt: "2026-10-11T18:05:01Z" })])).toEqual(["OBSERVED_AT_IN_FUTURE"]);
    expect(reasons([obs({ observedAt: "2026-10-11T20:00:00+02:00" })])).toEqual([]);
    expect(reasons([obs({}, ["observedAt"])])).toEqual(["MISSING_OBSERVED_AT"]);
    expect(reasons([obs({}, ["observedAt"])], minimalManifest({ fields: { observedAtMode: "received" } }))).toEqual([]);
  });

  it("keeps one granularity per series", () => {
    expect(reasons([obs({ id: "a" }), obs({ id: "b", t: "2026-10" }), obs({ id: "c", seriesKey: "other", t: "2026" })]))
      .toEqual(["GRANULARITY_MIXED"]);
  });

  it("checks periods", () => {
    expect(reasons([obs({ t: "2025-W53" })])).toEqual(["PERIOD_INVALID"]);
    expect(reasons([obs({ t: "2026-W53" })])).toEqual([]);
    expect(reasons([obs({ t: "2024-02-29" })])).toEqual([]);
    expect(reasons([obs({ t: "2023-02-29" })])).toEqual(["PERIOD_INVALID"]);
    expect(reasons([obs({ t: "2026-Q5" })])).toEqual(["SCHEMA_INVALID"]);
  });

  it("checks payloads and geometry", () => {
    const manifest = minimalManifest({ mode: "layer", itemType: "feature", geometry: "LineString",
      payloadSchema: { type: "object", required: ["name"], properties: { name: { type: "string" } } } });
    const base = { stream: "s", type: "feature", id: "f", observedAt: "2026-10-11T17:00:00Z", properties: { name: "a" } };
    const check = validateItems([
      { ...base, geometry: { type: "LineString", coordinates: [[0, 0], [1, 1]] } },
      { ...base, properties: {} },
      { ...base, geometry: { type: "LineString", coordinates: [[0, 0]] } },
      { ...base, geometry: { type: "Point", coordinates: [0, 0] } },
      { ...base },
    ], manifest, { receivedAt: NOW });
    expect(check.rejected.map((r) => r.reason)).toEqual(["PAYLOAD_INVALID", "GEOMETRY_INVALID", "GEOMETRY_INVALID"]);
    expect(check.rejected[0]?.schemaErrors[0]?.path).toBe("/properties");
    expect(check.valid.map((v) => v.index)).toEqual([0, 4]);
  });

  it("explains forecast problems", () => {
    expect(forecastProblem({ questionRef: " " })).toBe("a forecast needs a question or a questionRef");
    expect(forecastProblem({ questionRef: "q" })).toBe("a forecast needs a probability or quantiles");
    expect(forecastProblem({ questionRef: "q", probability: 2 })).toBe("probability must be between 0 and 1");
    expect(forecastProblem({ questionRef: "q", quantiles: { p05: 1 } })).toBe("quantiles need p05, p25, p50, p75 and p95");
    expect(forecastProblem({ questionRef: "q", quantiles: { p05: 2, p10: 1, p25: 3, p50: 4, p75: 5, p95: 6 } }))
      .toContain("p10 < p05");
    expect(forecastProblem({ question: { externalId: "a", title: "b" }, probability: 0.5 })).toBeNull();
  });

  it("throws for an invalid manifest", () => {
    const manifest = minimalManifest();
    manifest["transport"] = {};
    expect(() => validateItems([], manifest)).toThrow(ManifestInvalidError);
  });

  it("accepts builder output", () => {
    expect(reasons([observation("s", { seriesKey: "k", t: "2026-10", value: 3, observedAt: NOW })])).toEqual([]);
  });
});

function envelope(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return { protocol: "foresignal-edp/1.0", sentAt: "2026-10-11T18:00:00Z", items: [obs()], ...overrides };
}

describe("validateBatch", () => {
  it.each([
    ["minor too new", envelope({ protocol: "foresignal-edp/1.1" }), "PROTOCOL_UNSUPPORTED"],
    ["bad protocol", envelope({ protocol: "nope" }), "VALIDATION_FAILED"],
    ["other source", envelope({ sourceId: "someone-else" }), "SOURCE_MISMATCH"],
    ["items not an array", envelope({ items: { a: 1 } }), "VALIDATION_FAILED"],
    ["too many items", envelope({ items: Array.from({ length: 10_001 }, (_, i) => obs({ id: String(i) })) }), "VALIDATION_FAILED"],
    ["bad snapshot", envelope({ snapshot: { id: "x", stream: "s", page: 0, final: true } }), "VALIDATION_FAILED"],
    ["over 5 MB", envelope({ items: Array.from({ length: 2700 }, (_, i) => obs({ properties: { blob: "x".repeat(2000), i } })) }),
      "PAYLOAD_TOO_LARGE"],
    ["not an object", [1], "VALIDATION_FAILED"],
  ])("refuses %s", (_name, batch, code) => {
    const check = validateBatch(batch, minimalManifest(), { receivedAt: NOW });
    expect(check.code).toBe(code);
    expect(check.errors.length).toBeGreaterThan(0);
    expect(check.ok).toBe(false);
  });

  it("accepts the provider id or the registered id as sourceId", () => {
    for (const sourceId of ["test-source", "ext-test-source"]) {
      expect(validateBatch(envelope({ sourceId }), minimalManifest(), { receivedAt: NOW }).ok).toBe(true);
    }
    expect(validateBatch(envelope({ sourceId: "custom" }), minimalManifest(), { receivedAt: NOW, registeredId: "custom" }).ok)
      .toBe(true);
  });

  it("reports per-item rejections under a valid header", () => {
    const check = validateBatch(envelope({ snapshot: { id: "x", stream: "s", scope: null, page: 1, final: true },
      items: [obs(), obs({ id: "b", t: "bad" })] }), minimalManifest(), { receivedAt: NOW });
    expect(check.code).toBeNull();
    expect(check.items?.rejected.map((r) => r.reason)).toEqual(["SCHEMA_INVALID"]);
    expect(check.ok).toBe(false);
  });

  it("checks snapshot headers", () => {
    expect(validateSnapshotHeader(null)).toBeNull();
    expect(validateSnapshotHeader("x")).toBe("snapshot must be an object");
    expect(validateSnapshotHeader({ id: "x".repeat(129), stream: "s", page: 1, final: true })).not.toBeNull();
    expect(validateSnapshotHeader({ id: "x", stream: "s", page: 1.5, final: true })).not.toBeNull();
    expect(validateSnapshotHeader({ id: "x", stream: "s", page: 2, final: false })).toBeNull();
  });
});
