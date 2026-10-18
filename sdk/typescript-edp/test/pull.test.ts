/** Pull-server helper: cursor paging, cursor stores, snapshot paging, search, auth, status and adapters. */
import { createServer } from "node:http";
import type { AddressInfo } from "node:net";
import { describe, expect, it } from "vitest";
import {
  CursorError, MemoryCursorStore, OpaqueCursorStore, PullHandler, SequenceSource, SnapshotSource, StatusBoard, basicAuth,
  bearerAuth, feature, headerAuth, observation, point, toFetchHandler, toNodeListener, validateBatch,
} from "../src/index.ts";
import type { HttpResponse, ItemSource, JsonObject, PullHandlerOptions, SearchQuery } from "../src/index.ts";
import { minimalManifest } from "./helpers.ts";

const NOW = new Date("2026-10-11T18:00:00Z");

function rows(n: number): JsonObject[] {
  return Array.from({ length: n }, (_, i) => observation("s", { id: `r-${i}`, seriesKey: "k",
    t: `2026-01-${String((i % 28) + 1).padStart(2, "0")}`, value: i, observedAt: NOW }));
}

function handlerOver(data: JsonObject[], options: PullHandlerOptions = {}): PullHandler {
  return new PullHandler(new SequenceSource(() => data), { sourceId: "test-source", now: () => NOW, onError: () => {}, ...options });
}

function parse(response: HttpResponse): Record<string, any> { // eslint-disable-line @typescript-eslint/no-explicit-any
  return JSON.parse(new TextDecoder().decode(response.body));
}

async function get(handler: PullHandler, query: Record<string, string | number> = {}): Promise<Record<string, any>> { // eslint-disable-line @typescript-eslint/no-explicit-any
  const response = await handler.handleItems(Object.fromEntries(Object.entries(query).map(([k, v]) => [k, String(v)])));
  expect(response.status, new TextDecoder().decode(response.body)).toBe(200);
  expect(response.headers["Content-Type"]).toBe("application/json");
  return parse(response);
}

const ids = (page: Record<string, any>): string[] => (page["items"] as { id: string }[]).map((i) => i.id); // eslint-disable-line @typescript-eslint/no-explicit-any

describe("PullHandler items", () => {
  it("follows nextCursor until null and replays the last cursor with new items", async () => {
    const data = rows(7);
    const handler = handlerOver(data);
    const first = await get(handler, { limit: 3 });
    expect(first).toMatchObject({ protocol: "foresignal-edp/1.0", sourceId: "test-source", sentAt: "2026-10-11T18:00:00Z" });
    expect(ids(first)).toEqual(["r-0", "r-1", "r-2"]);
    const second = await get(handler, { limit: 3, cursor: first["nextCursor"] });
    expect(ids(second)).toEqual(["r-3", "r-4", "r-5"]);
    const third = await get(handler, { limit: 3, cursor: second["nextCursor"] });
    expect(ids(third)).toEqual(["r-6"]);
    expect(third["nextCursor"]).toBeNull();
    data.push(...rows(9).slice(7));
    const again = await get(handler, { limit: 3, cursor: second["nextCursor"] });
    expect(ids(again)).toEqual(["r-6", "r-7", "r-8"]);
    expect(again["nextCursor"]).not.toBeNull();
  });

  it("serves valid batches", async () => {
    expect(validateBatch(await get(handlerOver(rows(3))), minimalManifest(), { receivedAt: NOW }).ok).toBe(true);
  });

  it("defaults, clamps and validates limit", async () => {
    const handler = handlerOver(rows(700), { maxLimit: 600 });
    expect(ids(await get(handler))).toHaveLength(500);
    expect(ids(await get(handler, { limit: 10_000 }))).toHaveLength(600);
    expect(ids(await get(handler, { limit: 0 }))).toHaveLength(1);
    const bad = await handler.handleItems({ limit: "many" });
    expect(bad.status).toBe(400);
    expect(parse(bad)["code"]).toBe("LIMIT_INVALID");
    expect(bad.headers["Content-Type"]).toBe("application/problem+json");
  });

  it("accepts list values, URLSearchParams and an empty cursor", async () => {
    const handler = handlerOver(rows(2));
    expect(ids(parse(await handler.handleItems({ cursor: [""], limit: ["1"] })))).toEqual(["r-0"]);
    expect(ids(parse(await handler.handleItems(new URLSearchParams("limit=1"))))).toEqual(["r-0"]);
  });

  it("answers 400 for invalid cursors", async () => {
    const handler = handlerOver(rows(2));
    expect(parse(await handler.handleItems({ cursor: "%%%" }))["code"]).toBe("CURSOR_INVALID");
    expect(parse(await handler.handleItems({ cursor: new OpaqueCursorStore().encode(-1) }))["code"]).toBe("CURSOR_INVALID");
  });

  it("halves oversized pages until they fit", async () => {
    const data = rows(40).map((r) => ({ ...r, properties: { blob: "x".repeat(1000) } }));
    const handler = handlerOver(data, { maxBytes: 8000 });
    const first = await handler.handleItems({ limit: "40" });
    expect(first.status).toBe(200);
    expect(first.body.length).toBeLessThanOrEqual(8000);
    const page = parse(first);
    expect(ids(page).length).toBeGreaterThan(0);
    expect(ids(page).length).toBeLessThan(40);
    const following = await get(handler, { limit: 40, cursor: page["nextCursor"] });
    expect(ids(following)[0]).toBe(`r-${ids(page).length}`);
  });

  it("answers 500 when one item cannot fit", async () => {
    const response = await handlerOver([{ ...rows(1)[0], properties: { blob: "x".repeat(5000) } }], { maxBytes: 2000 }).handleItems();
    expect(response.status).toBe(500);
    expect(parse(response)["code"]).toBe("PAGE_TOO_LARGE");
  });

  it("answers 500 for source failures without leaking details, keeping status", async () => {
    const broken: ItemSource = { fetch: () => { throw new Error("database down"); } };
    const board = new StatusBoard();
    board.report("degraded", "slow upstream");
    const errors: unknown[] = [];
    const response = await new PullHandler(broken, { status: board, onError: (e) => errors.push(e) }).handleItems();
    expect(response.status).toBe(500);
    expect(parse(response)["code"]).toBe("PROVIDER_ERROR");
    expect(new TextDecoder().decode(response.body)).not.toContain("database down");
    expect(errors).toHaveLength(1);
    expect(board.drain()[0]?.health).toBe("degraded");
  });

  it("puts changed status at the front of the next page once", async () => {
    const board = new StatusBoard();
    const handler = handlerOver(rows(2), { status: board });
    board.report("layout-changed", "columns moved", { stream: "s" });
    const first = await get(handler);
    expect(first["items"][0]).toEqual({ stream: "s", type: "status", health: "layout-changed", message: "columns moved" });
    expect(first["items"]).toHaveLength(3);
    expect((await get(handler))["items"].some((i: { type: string }) => i.type === "status")).toBe(false);
  });
});

describe("cursor stores", () => {
  it("signs opaque cursors", () => {
    const store = new OpaqueCursorStore("secret");
    const cursor = store.encode({ offset: 5 });
    expect(store.decode(cursor)).toEqual({ offset: 5 });
    const signature = cursor.split(".")[1];
    const forged = `${Buffer.from('{"offset":0}').toString("base64url")}.${signature}`;
    expect(() => store.decode(forged)).toThrow(CursorError);
    expect(() => store.decode(cursor.split(".")[0] ?? "")).toThrow(CursorError);
    expect(new OpaqueCursorStore().decode(new OpaqueCursorStore().encode([1, "a"]))).toEqual([1, "a"]);
    expect(new OpaqueCursorStore().encode({ b: 1, a: 2 })).toBe(new OpaqueCursorStore().encode({ a: 2, b: 1 }));
  });

  it("keeps memory tokens stable and bounded", () => {
    const store = new MemoryCursorStore(2);
    const a = store.encode(10);
    expect(store.encode(10)).toBe(a);
    expect(store.decode(a)).toBe(10);
    const b = store.encode(20);
    store.decode(a);
    store.encode(30);
    expect(store.decode(a)).toBe(10);
    expect(() => store.decode(b)).toThrow(CursorError);
    expect(() => store.decode("unknown")).toThrow(CursorError);
  });

  it("uses a pluggable store", async () => {
    const encoded: unknown[] = [];
    const store = { encode: (p: unknown) => { encoded.push(p); return `pos-${String(p)}`; },
      decode: (c: string) => Number(c.replace("pos-", "")) };
    const handler = handlerOver(rows(5), { cursorStore: store });
    const first = await get(handler, { limit: 2 });
    expect(first["nextCursor"]).toBe("pos-2");
    expect(encoded).toEqual([2]);
    expect(ids(await get(handler, { limit: 2, cursor: "pos-2" }))).toEqual(["r-2", "r-3"]);
  });
});

describe("SnapshotSource", () => {
  it("pages one snapshot and starts a new one after the final page", async () => {
    let clock = 1000;
    const snapshotIds = ["snap-1", "snap-2", "snap-3"];
    const entities = [3, 1, 2, 5, 4].map((i) => feature("cameras", `cam-${i}`, { geometry: point(0, 0), observedAt: NOW }) as JsonObject);
    const handler = new PullHandler(new SnapshotSource("cameras", () => entities, { scope: "north", now: () => clock,
      idFactory: () => snapshotIds.shift() as string }));
    const p1 = await get(handler, { limit: 2 });
    expect(ids(p1)).toEqual(["cam-1", "cam-2"]);
    expect(p1["snapshot"]).toEqual({ id: "snap-1", stream: "cameras", scope: "north", page: 1, final: false });
    entities.push(feature("cameras", "cam-0", { geometry: point(0, 0), observedAt: NOW }));
    const p2 = await get(handler, { limit: 2, cursor: p1["nextCursor"] });
    expect(ids(p2)).toEqual(["cam-3", "cam-4"]);
    expect(p2["snapshot"]["page"]).toBe(2);
    const p3 = await get(handler, { limit: 2, cursor: p2["nextCursor"] });
    expect(ids(p3)).toEqual(["cam-5"]);
    expect(p3["snapshot"]).toEqual({ id: "snap-1", stream: "cameras", scope: "north", page: 3, final: true });
    expect(p3["nextCursor"]).toBeNull();
    clock += 60_000;
    const restart = await get(handler, { limit: 10, cursor: p2["nextCursor"] });
    expect(restart["snapshot"]).toMatchObject({ id: "snap-2", page: 1, final: true });
    expect(ids(restart)).toHaveLength(6);
  });

  it("restarts stale snapshots and refuses items of other streams", async () => {
    let clock = 0;
    const snapshotIds = ["a", "b"];
    const entities = Array.from({ length: 4 }, (_, i) => feature("cameras", `c-${i}`, { observedAt: NOW }) as JsonObject);
    const source = new SnapshotSource("cameras", () => entities, { now: () => clock, windowMs: 60_000,
      idFactory: () => snapshotIds.shift() as string });
    const first = await source.fetch(null, 2);
    clock = 61_000;
    const stale = await source.fetch(first.nextPosition, 2);
    expect(stale.snapshot).toMatchObject({ id: "b", page: 1 });
    const wrong = new SnapshotSource("cameras", () => [feature("other", "x") as JsonObject]);
    await expect(wrong.fetch(null, 10)).rejects.toThrow(RangeError);
  });
});

describe("PullHandler search", () => {
  it("parses the query and answers a batch", async () => {
    const seen: SearchQuery[] = [];
    const handler = handlerOver([], { search: (q) => { seen.push(q); return rows(5); } });
    const response = await handler.handleSearch({ q: " flood ", since: "2026-10-01T00:00:00Z", until: "2026-10-11T00:00:00Z",
      bbox: "5,45,15,55", lang: "de, en", limit: "3" });
    expect(response.status).toBe(200);
    const body = parse(response);
    expect(body["items"]).toHaveLength(3);
    expect(body["nextCursor"]).toBeNull();
    expect(seen[0]).toEqual({ q: "flood", since: Date.parse("2026-10-01T00:00:00Z"), until: Date.parse("2026-10-11T00:00:00Z"),
      bbox: [5, 45, 15, 55], lang: ["de", "en"], limit: 3 });
    expect((await handler.handleSearch({ q: "x" })).status).toBe(200);
    expect(seen.at(-1)?.limit).toBe(100);
  });

  it.each([
    [{}, "QUERY_REQUIRED"],
    [{ q: "x", since: "yesterday" }, "TIME_INVALID"],
    [{ q: "x", bbox: "1,2,3" }, "BBOX_INVALID"],
    [{ q: "x", bbox: "10,0,5,1" }, "BBOX_INVALID"],
    [{ q: "x", bbox: "a,b,c,d" }, "BBOX_INVALID"],
    [{ q: "x", limit: "x" }, "LIMIT_INVALID"],
  ])("refuses %j", async (query, code) => {
    const response = await handlerOver([], { search: () => [] }).handleSearch(query);
    expect(response.status).toBe(400);
    expect(parse(response)["code"]).toBe(code);
  });

  it("answers 404 without search, 500 on failure, and accepts pages", async () => {
    expect((await handlerOver([]).handleSearch({ q: "x" })).status).toBe(404);
    const failing = handlerOver([], { search: () => { throw new Error("boom"); } });
    expect(parse(await failing.handleSearch({ q: "x" }))["code"]).toBe("PROVIDER_ERROR");
    const paged = handlerOver([], { search: async () => ({ items: rows(2) }) });
    expect(parse(await paged.handleSearch({ q: "x" }))["items"]).toHaveLength(2);
  });
});

describe("auth, routing and adapters", () => {
  it.each([
    ["bearer", bearerAuth("t0k"), { Authorization: "Bearer t0k" }, { Authorization: "Bearer nope" }],
    ["header", headerAuth("X-Api-Key", "k1"), { "x-api-key": "k1" }, { "X-Api-Key": "k2" }],
    ["basic", basicAuth("u", "p"), { Authorization: `Basic ${Buffer.from("u:p").toString("base64")}` }, { Authorization: "Basic !!!" }],
  ])("%s auth", async (_name, authorize, good, bad) => {
    const handler = handlerOver(rows(1), { authorize, search: () => [] });
    expect((await handler.handleItems({}, good)).status).toBe(200);
    expect((await handler.handleItems({}, bad)).status).toBe(401);
    expect((await handler.handleItems({}, {})).status).toBe(401);
    expect((await handler.handleSearch({ q: "x" }, bad)).status).toBe(401);
  });

  it("routes manifest, items, HEAD and methods", async () => {
    const manifest = minimalManifest();
    const handler = handlerOver(rows(3), { manifest });
    expect(parse(await handler.handle({ method: "GET", path: "/.well-known/foresignal-edp.json" }))).toEqual(manifest);
    expect(parse(await handler.handle({ method: "GET", path: "/edp/items?limit=2" }))["items"]).toHaveLength(2);
    const head = await handler.handle({ method: "HEAD", path: "/edp/items" });
    expect(head.status).toBe(200);
    expect(head.body.length).toBe(0);
    const post = await handler.handle({ method: "POST", path: "/edp/items" });
    expect(post.status).toBe(405);
    expect(post.headers["Allow"]).toBe("GET, HEAD");
    expect((await handler.handle({ method: "GET", path: "/elsewhere" })).status).toBe(404);
    expect((await handlerOver([]).handle({ method: "GET", path: "/.well-known/foresignal-edp.json" })).status).toBe(404);
  });

  it("adapts to fetch-style handlers", async () => {
    const fetchHandler = toFetchHandler(handlerOver(rows(3), { authorize: bearerAuth("t") }));
    const response = await fetchHandler(new Request("http://provider.example/edp/items?limit=2",
      { headers: { Authorization: "Bearer t" } }));
    expect(response.status).toBe(200);
    expect(((await response.json()) as { items: unknown[] }).items).toHaveLength(2);
    expect((await fetchHandler(new Request("http://provider.example/edp/items"))).status).toBe(401);
  });

  it("adapts to node:http", async () => {
    const server = createServer(toNodeListener(handlerOver(rows(3), { authorize: headerAuth("X-Api-Key", "k") })));
    await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
    try {
      const { port } = server.address() as AddressInfo;
      const response = await fetch(`http://127.0.0.1:${port}/edp/items?limit=2`, { headers: { "X-Api-Key": "k" } });
      expect(response.status).toBe(200);
      expect(response.headers.get("content-length")).not.toBeNull();
      expect(((await response.json()) as { items: unknown[] }).items).toHaveLength(2);
      expect((await fetch(`http://127.0.0.1:${port}/edp/items`)).status).toBe(401);
    } finally {
      await new Promise<void>((resolve) => server.close(() => resolve()));
    }
  });

  it("validates its options", () => {
    expect(() => new PullHandler(new SequenceSource(() => []), { maxLimit: 10_001 })).toThrow(RangeError);
    expect(() => new PullHandler(new SequenceSource(() => []), { maxBytes: 0 })).toThrow(RangeError);
  });
});
