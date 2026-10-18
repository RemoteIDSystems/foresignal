/** Manifest builder and validator. */
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import {
  ManifestBuilder, ManifestInvalidError, StreamBuilder, asManifestCheck, bboxPolygon, isDuration, isHttpUrl,
  isManifestCheck, validateManifest,
} from "../src/index.ts";
import { CONFORMANCE_DIR, HAS_CONFORMANCE, loadJson, minimalManifest } from "./helpers.ts";

function hormuzBuilder(): ManifestBuilder {
  return new ManifestBuilder("hormuz-transits", "Strait of Hormuz transits")
    .description("Daily tanker and cargo transits counted from public dashboards")
    .version("0.3.1")
    .publisher("Example OSINT Lab", { url: "https://example.org", contact: "ops@example.org" })
    .homepage("https://github.com/example/hormuz-edp")
    .license({ id: "CC-BY-4.0", url: "https://creativecommons.org/licenses/by/4.0/", attribution: "Example OSINT Lab",
      redistribution: "allowed" })
    .terms("Derived from a public dashboard; upstream terms unclear", null)
    .access("scrape", "high")
    .originator("example-osint-lab", "aggregator", [])
    .markPrivate(false)
    .push()
    .pull("https://hormuz.example.org/edp/items", { intervalSeconds: 3600, auth: "bearer" })
    .stream(new StreamBuilder("transits", { mode: "atomic", itemType: "observation", kind: "transit-count" })
      .title("Daily transits by vessel class").unit("vessels/day").incremental()
      .payloadSchema({ type: "object", properties: { vesselClass: { type: "string" } } })
      .suggestBinding("series"))
    .stream(new StreamBuilder("incidents", { mode: "layer", itemType: "event", kind: "maritime-incident" })
      .title("Reported incidents").geometry("Point").incremental()
      .payloadSchema({ type: "object", required: ["category"],
        properties: { category: { type: "string" }, vessel: { type: ["string", "null"] } } })
      .fields({ title: "category", significant: ["category"], volatile: [] })
      .retentionClass("event")
      .presentation({ color: "#d9480f", icon: "ship", labelField: "vessel", minZoom: 3, tileProperties: ["category"] })
      .coverage({ area: bboxPolygon(54.5, 25.0, 58.0, 27.5), family: "maritime-incident", pDetect: 0.6 })
      .suggestBinding("canonical-family", { family: "maritime-incident" }));
}

describe("ManifestBuilder", () => {
  it("reproduces the specification example", () => {
    const built = hormuzBuilder().build();
    const source = built["source"] as Record<string, unknown>;
    expect((source["licence"] as Record<string, unknown>)["redistribution"]).toBe("allowed");
    expect(built["transport"]).toEqual({ push: true, pull: { itemsUrl: "https://hormuz.example.org/edp/items",
      intervalSeconds: 3600, auth: "bearer" } });
    const streams = built["streams"] as Record<string, unknown>[];
    expect((streams[1]?.["coverage"] as Record<string, unknown>)["pDetect"]).toBe(0.6);
    if (HAS_CONFORMANCE) {
      const expected = loadJson(join(CONFORMANCE_DIR, "manifests", "valid", "hormuz-example.json"));
      expect(source).toEqual(expected["source"]);
      const expectedStreams = expected["streams"] as Record<string, unknown>[];
      expect(streams[0]).toEqual(expectedStreams[0]);
      expect(streams[1]?.["presentation"]).toEqual(expectedStreams[1]?.["presentation"]);
      expect(streams[1]?.["coverage"]).toEqual(expectedStreams[1]?.["coverage"]);
    }
  });

  it("writes snapshot, complete and document members", () => {
    const manifest = new ManifestBuilder("harbor-cams", "Harbor cameras")
      .publisher("Example Port Watch").originator("example-port-watch", "operator").access("official", "low").push()
      .stream(new StreamBuilder("cameras", { mode: "layer", itemType: "feature", kind: "harbor-camera" })
        .geometry("Point").snapshot("region").ttl("PT1H"))
      .stream(new StreamBuilder("notes", { mode: "atomic", itemType: "document", kind: "harbor-note" })
        .documentType("note").complete().fields({ observedAtMode: "received" }))
      .build();
    const [cameras, notes] = manifest["streams"] as Record<string, unknown>[];
    expect(cameras).toMatchObject({ update: "snapshot", snapshotScope: "region", ttl: "PT1H" });
    expect(notes).toMatchObject({ complete: true, documentType: "note", fields: { observedAtMode: "received" } });
    const check = validateManifest(manifest);
    expect(check.valid).toBe(true);
    expect(check.streams.get("cameras")?.update).toBe("snapshot");
    expect(check.streams.get("notes")?.observedAtReceived).toBe(true);
  });

  it("keeps a null scope on a whole-stream snapshot and drops it when incremental", () => {
    const whole = new StreamBuilder("a", { mode: "layer", itemType: "feature", kind: "k" }).snapshot().build();
    expect("snapshotScope" in whole && whole["snapshotScope"] === null).toBe(true);
    const back = new StreamBuilder("a", { mode: "layer", itemType: "feature", kind: "k" }).snapshot("r").incremental().build();
    expect("snapshotScope" in back).toBe(false);
  });

  it("validates on build and reports every problem", () => {
    const builder = new ManifestBuilder("Bad Id", "Bad").publisher("X").originator("x", "operator");
    let error: unknown;
    try {
      builder.build();
    } catch (e) {
      error = e;
    }
    expect(error).toBeInstanceOf(ManifestInvalidError);
    const paths = (error as ManifestInvalidError).errors.map((e) => e.path);
    expect(paths).toContain("/source/id");
    expect(paths).toContain("/transport");
    expect(paths).toContain("/streams");
    expect((builder.build({ validate: false })["source"] as Record<string, unknown>)["id"]).toBe("Bad Id");
  });

  it("sets forward-compatible members and exposes the registered id", () => {
    const manifest = new ManifestBuilder("a", "A").publisher("X").originator("x", "operator").push()
      .stream(new StreamBuilder("s", { mode: "atomic", itemType: "observation", kind: "k" }).set("x-future", 1))
      .set("x-top", true).build();
    expect(manifest["x-top"]).toBe(true);
    expect((manifest["streams"] as Record<string, unknown>[])[0]?.["x-future"]).toBe(1);
    expect(new ManifestBuilder("abc", "A").registeredId).toBe("ext-abc");
  });
});

type Mutation = (m: Record<string, any>) => void; // eslint-disable-line @typescript-eslint/no-explicit-any

describe("validateManifest semantic checks", () => {
  const cases: [string, Mutation, string, string][] = [
    ["protocol", (m) => { m["protocol"] = "edp/1.0"; }, "/protocol", "foresignal-edp/1.N"],
    ["pull scheme", (m) => { m["transport"].pull = { itemsUrl: "ftp://x/items" }; }, "/transport/pull/itemsUrl", "absolute http or https URL"],
    ["pull interval", (m) => { m["transport"].pull = { itemsUrl: "https://x/items", intervalSeconds: 30 }; },
      "/transport/pull/intervalSeconds", "at least 60"],
    ["header auth", (m) => { m["transport"].pull = { itemsUrl: "https://x/items", auth: "header" }; },
      "/transport/pull/header", "required when auth is header"],
    ["search url", (m) => { m["transport"].search = { url: "not a url" }; }, "/transport/search/url", "absolute http or https URL"],
    ["originator", (m) => { m["source"].originator.id = "Not Slug"; }, "/source/originator/id", "slug"],
    ["kind", (m) => { m["streams"][0].kind = "Bad Kind"; }, "/streams/0/kind", "slug"],
    ["ttl", (m) => { m["streams"][0].ttl = "one hour"; }, "/streams/0/ttl", "ISO-8601"],
    ["snapshotScope", (m) => { m["streams"][0].snapshotScope = "region"; }, "/streams/0/snapshotScope", "only snapshot streams"],
    ["field title", (m) => { m["streams"][0].fields = { title: "a.b" }; }, "/streams/0/fields/title", "top-level payload name"],
    ["field roles", (m) => { m["streams"][0].fields = { significant: ["x"], volatile: ["x"] }; }, "/streams/0/fields",
      "both significant and volatile"],
    ["color", (m) => { m["streams"][0].presentation = { color: "red" }; }, "/streams/0/presentation/color", "#RRGGBB"],
    ["zoom", (m) => { m["streams"][0].presentation = { minZoom: 23 }; }, "/streams/0/presentation/minZoom", "0 to 22"],
    ["label", (m) => { m["streams"][0].presentation = { labelField: "a b" }; }, "/streams/0/presentation/labelField",
      "top-level payload name"],
    ["tile properties", (m) => { m["streams"][0].presentation = { tileProperties: ["ok", "not ok"] }; },
      "/streams/0/presentation/tileProperties/1", "top-level payload name"],
    ["dynamicRef", (m) => { m["streams"][0].payloadSchema = { $dynamicRef: "#x" }; }, "/streams/0/payloadSchema/$dynamicRef",
      "not allowed"],
    ["dialect", (m) => { m["streams"][0].payloadSchema = { $schema: "http://json-schema.org/draft-07/schema#" }; },
      "/streams/0/payloadSchema/$schema", "must be absent"],
    ["look-around", (m) => { m["streams"][0].payloadSchema = { patternProperties: { "(?=x)": {} } }; },
      "/streams/0/payloadSchema/patternProperties/(?=x)", "linear-time"],
    ["metaschema", (m) => { m["streams"][0].payloadSchema = { type: "no-such-type" }; }, "/streams/0/payloadSchema/type", ""],
    ["layer geometry", (m) => { Object.assign(m["streams"][0], { mode: "layer", itemType: "feature", geometry: "none" }); },
      "/streams/0/geometry", "must have a geometry"],
  ];

  it.each(cases)("%s", (_name, mutate, path, text) => {
    const manifest = minimalManifest() as Record<string, any>; // eslint-disable-line @typescript-eslint/no-explicit-any
    mutate(manifest);
    const check = validateManifest(manifest);
    expect(check.valid).toBe(false);
    expect(check.errors.some((e) => e.path === path && e.message.includes(text)), JSON.stringify(check.errors)).toBe(true);
  });

  it("refuses an oversized payload schema", () => {
    const check = validateManifest(minimalManifest({ payloadSchema: { type: "object", description: "x".repeat(64 * 1024) } }));
    expect(check.errors.some((e) => e.path === "/streams/0/payloadSchema" && e.message.includes("maximum is 65536"))).toBe(true);
  });

  it("handles non-objects and exposes helpers", () => {
    const bad = validateManifest([1, 2]);
    expect(bad.valid).toBe(false);
    expect(bad.errors[0]?.message).toBe("the manifest must be a JSON object");
    expect(bad.minor).toBe(-1);
    const check = validateManifest(minimalManifest());
    expect(check.valid && check.minor === 0 && check.providerId === "test-source").toBe(true);
    expect(isManifestCheck(check)).toBe(true);
    expect(asManifestCheck(check)).toBe(check);
    const duplicate = minimalManifest() as Record<string, any>; // eslint-disable-line @typescript-eslint/no-explicit-any
    duplicate["streams"].push({ ...duplicate["streams"][0] });
    expect(() => asManifestCheck(duplicate)).toThrow(/duplicate stream id/);
  });

  it("recognizes durations and URLs", () => {
    for (const ok of ["PT1H", "P7D", "15m", "pt1.5s"]) expect(isDuration(ok)).toBe(true);
    for (const bad of ["P", "PT", "-PT1H", "P1Y"]) expect(isDuration(bad)).toBe(false);
    expect(isHttpUrl("https://a.example/x")).toBe(true);
    expect(isHttpUrl("http://localhost:8099/edp/items")).toBe(true);
    expect(isHttpUrl("https:///x")).toBe(false);
    expect(isHttpUrl("mailto:a@b")).toBe(false);
    expect(isHttpUrl(null)).toBe(false);
  });
});
