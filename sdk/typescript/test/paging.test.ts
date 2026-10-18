/** Cursor iterators across pages, the changes feed, and the typed 410 RESYNC_REQUIRED error. */
import { afterEach, describe, expect, it } from "vitest";
import { ApiError, ResyncRequiredError } from "../src/errors.js";
import { HttpTransport } from "../src/http.js";
import { iterateChanges, nextCursorOf, paginate, paginatePages, paginateWith } from "../src/paging/paginate.js";
import { json, startServer, type MockServer } from "./mock-server.js";

let server: MockServer | undefined;

afterEach(async () => {
  await server?.close();
  server = undefined;
});

async function all<T>(iterable: AsyncIterable<T>): Promise<T[]> {
  const out: T[] = [];
  for await (const item of iterable) {
    out.push(item);
  }
  return out;
}

describe("paginate", () => {
  it("follows nextCursor across three pages", async () => {
    server = await startServer((req, res) => {
      const cursor = req.query.get("cursor");
      if (!cursor) {
        return json(res, 200, { items: [{ id: 1 }, { id: 2 }], nextCursor: "c2" });
      }
      if (cursor === "c2") {
        return json(res, 200, { items: [{ id: 3 }], nextCursor: "c3" });
      }
      return json(res, 200, { items: [{ id: 4 }], nextCursor: null });
    });
    const items = await all(paginate<{ id: number }>({ baseUrl: server.url, token: "fs_t" }, "/v1/alerts",
      { status: "firing" }, { pageSize: 2 }));
    expect(items.map((i) => i.id)).toEqual([1, 2, 3, 4]);
    const requests = server.requestsTo("/v1/alerts");
    expect(requests.map((r) => r.query.get("cursor"))).toEqual([null, "c2", "c3"]);
    expect(requests.every((r) => r.query.get("status") === "firing" && r.query.get("limit") === "2")).toBe(true);
    expect(requests[0]!.headers["authorization"]).toBe("Bearer fs_t");
  });

  it("yields whole pages and stops at maxPages", async () => {
    server = await startServer((req, res) => json(res, 200, { items: [req.query.get("cursor") ?? "first"], nextCursor: "again" }));
    const pages = await all(paginatePages<string>(new HttpTransport({ baseUrl: server.url }), "/v1/places", {}, { maxPages: 3 }));
    expect(pages.map((p) => p.items[0])).toEqual(["first", "again", "again"]);
  });

  it("accepts the older next spelling and works with any page fetcher", async () => {
    const pages: Record<string, { items: number[]; next?: string | null }> = {
      start: { items: [1], next: "b" },
      b: { items: [2], next: "c" },
      c: { items: [3], next: null },
    };
    const seen: (string | undefined)[] = [];
    const items = await all(paginateWith<number>(async (cursor) => {
      seen.push(cursor);
      return pages[cursor ?? "start"]!;
    }));
    expect(items).toEqual([1, 2, 3]);
    expect(seen).toEqual([undefined, "b", "c"]);
    expect(nextCursorOf({ nextCursor: "", next: "x" })).toBeUndefined();
    expect(nextCursorOf({ next: "x" })).toBe("x");
  });

  it("surfaces API errors", async () => {
    server = await startServer((req, res) => json(res, 400, { status: 400, code: "INVALID_CURSOR", detail: "bad cursor" }));
    const error = await all(paginate({ baseUrl: server.url }, "/v1/records")).catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).code).toBe("INVALID_CURSOR");
    expect((error as ApiError).message).toBe("bad cursor");
  });
});

describe("iterateChanges", () => {
  it("pages by seq until hasMore is false and returns the last seq", async () => {
    server = await startServer((req, res) => {
      const after = Number(req.query.get("after"));
      if (after === 0) {
        return json(res, 200, { items: [{ seq: 1 }, { seq: 2 }], lastSeq: 2, hasMore: true, watermarkSeq: 5 });
      }
      if (after === 2) {
        return json(res, 200, { items: [{ seq: 4 }], lastSeq: 4, hasMore: true, watermarkSeq: 5 });
      }
      return json(res, 200, { items: [], lastSeq: 5, hasMore: false, watermarkSeq: 5 });
    });
    const lastSeqs: number[] = [];
    const iterator = iterateChanges<{ seq: number }>({ baseUrl: server.url }, {
      filters: { source: ["adsb", "ais"] }, limit: 2, onPage: (p) => lastSeqs.push(p.lastSeq),
    });
    const seqs: number[] = [];
    let result = await iterator.next();
    while (!result.done) {
      seqs.push(result.value.seq);
      result = await iterator.next();
    }
    expect(seqs).toEqual([1, 2, 4]);
    expect(result.value).toBe(5);
    expect(lastSeqs).toEqual([2, 4, 5]);
    const requests = server.requestsTo("/v1/changes");
    expect(requests.map((r) => r.query.get("after"))).toEqual(["0", "2", "4"]);
    expect(requests[0]!.query.getAll("source")).toEqual(["adsb", "ais"]);
    expect(requests[0]!.query.get("limit")).toBe("2");
  });

  it("throws ResyncRequiredError on 410 RESYNC_REQUIRED", async () => {
    server = await startServer((req, res) => json(res, 410, {
      type: "https://foresignal.ai/problems/resync-required", title: "Resync required", status: 410,
      code: "RESYNC_REQUIRED", purgeHorizon: 1200, watermarkSeq: 5000,
    }));
    const error = await all(iterateChanges({ baseUrl: server.url }, { after: 10 })).catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ResyncRequiredError);
    expect(error).toBeInstanceOf(ApiError);
    const resync = error as ResyncRequiredError;
    expect(resync.status).toBe(410);
    expect(resync.code).toBe("RESYNC_REQUIRED");
    expect(resync.purgeHorizon).toBe(1200);
    expect(resync.watermarkSeq).toBe(5000);
  });

  it("keeps a plain ApiError for another 410", async () => {
    server = await startServer((req, res) => json(res, 410, { status: 410, code: "GONE" }));
    const error = await all(iterateChanges({ baseUrl: server.url })).catch((e: unknown) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect(error).not.toBeInstanceOf(ResyncRequiredError);
  });
});
