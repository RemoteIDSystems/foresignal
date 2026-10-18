/** The example provider builds a valid manifest and serves and pushes valid batches. */
import { join } from "node:path";
import { describe, expect, it, vi } from "vitest";
import { buildManifest, main, makeHandler, push, readItems } from "../examples/river-gauges-provider.ts";
import { validateBatch, validateManifest } from "../src/index.ts";
import { LIBRARY_ROOT, bodyJson, fakeFetch, ok } from "./helpers.ts";

const CSV = join(LIBRARY_ROOT, "examples", "river-levels.csv");
const NOW = new Date("2026-10-17T12:00:00Z");

describe("river gauges example", () => {
  it("builds a valid manifest with and without an API key", () => {
    const open = buildManifest("https://hydro.example.org");
    expect(validateManifest(open).valid).toBe(true);
    expect((open["transport"] as Record<string, unknown>)["pull"]).toEqual({ itemsUrl: "https://hydro.example.org/edp/items",
      intervalSeconds: 300, auth: "none" });
    const keyed = buildManifest(undefined, true);
    expect((keyed["transport"] as Record<string, Record<string, unknown>>)["pull"]).toMatchObject({ auth: "header",
      header: "X-Api-Key" });
    expect(validateManifest(keyed).valid).toBe(true);
  });

  it("turns rows into observations", () => {
    const items = readItems(CSV, NOW);
    expect(items.map((i) => i["id"])).toEqual(["kaub:2026-10-15", "kaub:2026-10-16", "maxau:2026-10-15", "maxau:2026-10-16"]);
    expect(items[0]?.["knownAt"]).toBe("2026-10-15T06:00:00Z");
    expect(items[2]?.["knownAt"]).toBeUndefined();
    expect(items[3]?.["value"]).toBeNull();
  });

  it("serves a valid batch with status", async () => {
    const handler = makeHandler(CSV, undefined, "k");
    expect((await handler.handle({ method: "GET", path: "/edp/items" })).status).toBe(401);
    const response = await handler.handle({ method: "GET", path: "/edp/items", headers: { "X-Api-Key": "k" } });
    const body = JSON.parse(new TextDecoder().decode(response.body)) as Record<string, unknown>;
    const items = body["items"] as Record<string, unknown>[];
    expect(items[0]).toEqual({ stream: "levels", type: "status", health: "ok" });
    expect(items).toHaveLength(5);
    expect(body["nextCursor"]).toBeNull();
    const manifest = JSON.parse(new TextDecoder().decode(handler.handleManifest().body)) as Record<string, unknown>;
    expect(validateBatch(body, manifest).ok).toBe(true);
  });

  it("reports a missing CSV as blocked", async () => {
    const response = await makeHandler(join(LIBRARY_ROOT, "examples", "missing.csv")).handle({ method: "GET", path: "/edp/items" });
    const body = JSON.parse(new TextDecoder().decode(response.body)) as Record<string, unknown>;
    expect(body["items"]).toEqual([{ stream: "levels", type: "status", health: "blocked", message: "missing.csv is missing" }]);
  });

  it("pushes the rows", async () => {
    const fake = fakeFetch([ok(4)]);
    const log = vi.spyOn(console, "log").mockImplementation(() => {});
    try {
      expect(await push(CSV, "https://fs.example", "eik_k", { fetch: fake.fetch, sleep: async () => {} })).toBe(0);
      expect(fake.requests[0]?.url).toBe("https://fs.example/v1/external/sources/ext-river-gauges/items");
      expect(bodyJson(fake.requests[0])["items"]).toHaveLength(4);
      expect(log).toHaveBeenCalledWith("accepted 4 of 4 in 1 batch(es)");
    } finally {
      log.mockRestore();
    }
  });

  it("prints usage for unknown commands", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      expect(await main([])).toBe(64);
    } finally {
      error.mockRestore();
    }
  });
});
