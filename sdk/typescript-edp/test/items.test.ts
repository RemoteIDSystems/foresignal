/** Typed item builders: every item type validates against its exported schema and a manifest. */
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import {
  author, batch, deleteItem, document, event, feature, forecast, lineString, media, multiLineString, multiPoint,
  multiPolygon, observation, origin, point, polygon, quantiles, question, shapeErrors, snapshot, status, validateItems,
} from "../src/index.ts";
import * as edp from "../src/index.ts";
import { CONFORMANCE_DIR, CONFORMANCE_RECEIVED_AT, HAS_CONFORMANCE, loadJson } from "./helpers.ts";

const T = new Date("2026-10-11T17:00:00Z");

describe("item builders", () => {
  it("builds a feature", () => {
    const item = feature("cameras", "cam-1", { geometry: point(-71.05, 42.36), observedAt: T,
      properties: { name: "Pier 4", region: "north" }, validUntil: new Date("2026-10-11T18:00:00Z"),
      origin: origin("example-port-watch", "operator", { upstreamId: "u-1" }) });
    expect(item).toEqual({ stream: "cameras", type: "feature", id: "cam-1", observedAt: "2026-10-11T17:00:00Z",
      properties: { name: "Pier 4", region: "north" },
      origin: { originator: "example-port-watch", class: "operator", upstreamId: "u-1" },
      geometry: { type: "Point", coordinates: [-71.05, 42.36] }, validUntil: "2026-10-11T18:00:00Z" });
    expect(shapeErrors("feature", item)).toEqual([]);
  });

  it("builds an event", () => {
    const item = event("incidents", "inc-1", { eventTime: "2026-10-11T16:55:00Z", observedAt: T, geometry: point(56.4, 26.5),
      properties: { category: "x" }, upstreamEventId: "up-9" });
    expect(item.eventTime).toBe("2026-10-11T16:55:00Z");
    expect(item.upstreamEventId).toBe("up-9");
    expect(shapeErrors("event", item)).toEqual([]);
  });

  it("builds observations with default ids and formatted periods", () => {
    const day = observation("transits", { seriesKey: "tanker", t: new Date("2026-10-10T00:00:00Z"), dateOnly: true, value: 31,
      observedAt: T, knownAt: T, unit: "vessels/day" });
    expect(day.id).toBe("tanker:2026-10-10");
    expect(day.t).toBe("2026-10-10");
    const nullValue = observation("transits", { id: "cargo:w41", seriesKey: "cargo", t: "2026-W41", value: null, observedAt: T });
    expect(nullValue.value).toBeNull();
    const instant = observation("transits", { seriesKey: "k", t: T, value: 1.5, observedAt: T });
    expect(instant.t).toBe("2026-10-11T17:00:00Z");
    for (const item of [day, nullValue, instant]) expect(shapeErrors("observation", item)).toEqual([]);
    expect(() => observation("s", { seriesKey: "k", t: "2026", value: true as unknown as number })).toThrow(TypeError);
  });

  it("builds a document", () => {
    const item = document("notes", "n-1", { documentType: "note", text: "Crane 3 out of service", observedAt: T, title: "Crane",
      url: "https://example.org/n-1", author: author("@port", "Port", "42"), language: "en", publishedAt: T,
      about: ["place:01J"], media: [media("https://e.org/c.jpg", "image")], geometry: polygon([[[0, 0], [1, 0], [1, 1]]]),
      engagement: { likes: 3, shares: null } });
    expect(item["author"]).toEqual({ handle: "@port", name: "Port", accountId: "42" });
    expect(item["media"]).toEqual([{ url: "https://e.org/c.jpg", type: "image" }]);
    expect((item["geometry"] as { coordinates: number[][][] }).coordinates[0]?.at(-1)).toEqual([0, 0]);
    expect(shapeErrors("document", item)).toEqual([]);
  });

  it("builds forecasts and refuses incomplete ones", () => {
    const byProbability = forecast("odds", "f-1", { madeAt: T,
      question: question("q-1", "Will it close?", { closeTime: T }), probability: 0.2 });
    const byQuantiles = forecast("odds", "f-2", { madeAt: T, questionRef: "01J9", quantiles: quantiles(1, 2, 3, 4, 5, { p10: 1.5 }) });
    expect(byProbability.question?.closeTime).toBe("2026-10-11T17:00:00Z");
    expect(byQuantiles.quantiles).toEqual({ p05: 1, p10: 1.5, p25: 2, p50: 3, p75: 4, p95: 5 });
    expect(shapeErrors("forecast", byProbability)).toEqual([]);
    expect(shapeErrors("forecast", byQuantiles)).toEqual([]);
    expect(() => forecast("odds", "f", { madeAt: T, probability: 0.5 })).toThrow(/question/);
    expect(() => forecast("odds", "f", { madeAt: T, questionRef: "q" })).toThrow(/probability or quantiles/);
    expect(() => forecast("odds", "f", { madeAt: T, questionRef: "q", probability: 1.5 })).toThrow(/between 0 and 1/);
    expect(() => forecast("odds", "f", { madeAt: T, questionRef: "q", quantiles: { p05: 1 } as never })).toThrow(/p05, p25/);
    expect(() => quantiles(5, 4, 3, 2, 1)).toThrow(/non-decreasing/);
  });

  it("builds delete and status items", () => {
    expect(deleteItem("incidents", "inc-8790")).toEqual({ stream: "incidents", type: "delete", id: "inc-8790" });
    expect(edp.delete("s", "1")).toEqual({ stream: "s", type: "delete", id: "1" });
    expect(status("degraded", "layout changed")).toEqual({ type: "status", health: "degraded", message: "layout changed" });
    expect(status("ok", undefined, { stream: "transits" })).toEqual({ stream: "transits", type: "status", health: "ok" });
    expect(shapeErrors("delete", deleteItem("s", "x", { observedAt: T, properties: { a: 1 } }))).toEqual([]);
    expect(shapeErrors("status", status("blocked"))).toEqual([]);
    expect(() => status("on-fire" as never)).toThrow(RangeError);
  });

  it("merges extra members and builds geometries", () => {
    expect(feature("s", "1", { extra: { "x-new": 1 } })["x-new"]).toBe(1);
    expect(point(1, 2, 3)).toEqual({ type: "Point", coordinates: [1, 2, 3] });
    expect(multiPoint([[1, 2], [3, 4]]).coordinates).toEqual([[1, 2], [3, 4]]);
    expect(lineString([[0, 0], [1, 1]]).type).toBe("LineString");
    expect(multiLineString([[[0, 0], [1, 1]]]).coordinates).toEqual([[[0, 0], [1, 1]]]);
    expect((polygon([[[0, 0], [1, 0], [1, 1], [0, 0]]]).coordinates[0] as unknown[]).length).toBe(4);
    expect((polygon([[[0, 0], [1, 0], [1, 1]]], false).coordinates[0] as unknown[]).length).toBe(3);
    expect(((multiPolygon([[[[0, 0], [1, 0], [1, 1]]]]).coordinates[0] as unknown[][])[0] as unknown[]).at(-1)).toEqual([0, 0]);
  });

  it("builds batch and snapshot envelopes", () => {
    const page = snapshot("snap-1", "cameras", 1, true, "north");
    expect(batch([deleteItem("s", "1")], { sourceId: "harbor-cams", sentAt: T, snapshot: page })).toEqual({
      protocol: "foresignal-edp/1.0", sourceId: "harbor-cams", sentAt: "2026-10-11T17:00:00Z",
      snapshot: { id: "snap-1", stream: "cameras", scope: "north", page: 1, final: true },
      items: [{ stream: "s", type: "delete", id: "1" }] });
    const pulled = batch([], { includeNextCursor: true });
    expect(pulled.nextCursor).toBeNull();
    expect(pulled.sentAt?.endsWith("Z")).toBe(true);
    expect(() => snapshot("x", "s", 0, false)).toThrow(RangeError);
  });

  it.skipIf(!HAS_CONFORMANCE)("produces items the full validator accepts", () => {
    const manifest = loadJson(join(CONFORMANCE_DIR, "manifests", "valid", "push-layer.json"));
    const items = [
      feature("cameras", "cam-9", { geometry: point(-71.0, 42.3), observedAt: T, properties: { name: "Dock 9" } }),
      document("notes", "n-9", { documentType: "note", text: "ok", observedAt: T }),
      forecast("odds", "f-9", { madeAt: T, questionRef: "q", probability: 0.4 }),
      deleteItem("cameras", "cam-1"),
      status("ok", undefined, { stream: "cameras" }),
      status("ok"),
    ];
    const check = validateItems(items, manifest, { receivedAt: CONFORMANCE_RECEIVED_AT });
    expect(check.rejected).toEqual([]);
    expect(check.valid.map((v) => v.type)).toEqual(["feature", "document", "forecast", "delete", "status", "status"]);
    expect(check.valid.at(-1)?.stream).toBeNull();
  });
});
