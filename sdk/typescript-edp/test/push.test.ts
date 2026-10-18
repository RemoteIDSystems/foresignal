/**
 * Push client: batching limits, NDJSON, retries with backoff on 429/503 honoring Retry-After,
 * idempotent re-sends, snapshots, status and manifest updates.
 */
import { describe, expect, it } from "vitest";
import { ItemTooLargeError, PushClient, PushError, decodeNdjson, deleteItem, observation, parseRetryAfter } from "../src/index.ts";
import type { PushClientOptions } from "../src/index.ts";
import { bodyJson, fakeFetch, ok, problemStep } from "./helpers.ts";
import type { Step } from "./helpers.ts";

const NOW = new Date("2026-10-11T18:00:00Z");
const decoder = new TextDecoder();

function client(script: Step[] = [], options: Partial<PushClientOptions> = {}) {
  const fake = fakeFetch(script);
  const sleeps: number[] = [];
  const push = new PushClient({ baseUrl: "https://foresignal.example/", sourceId: "ext-river-gauges", ingestKey: "eik_secret",
    fetch: fake.fetch, sleep: async (ms) => { sleeps.push(ms); }, random: () => 0.5, now: () => NOW, ...options });
  return { push, requests: fake.requests, sleeps };
}

function items(n: number, blob = 0): Record<string, unknown>[] {
  return Array.from({ length: n }, (_, i) => observation("levels", { id: `i-${i}`, seriesKey: "kaub",
    t: `2026-${String((i % 12) + 1).padStart(2, "0")}`, value: i, observedAt: NOW,
    ...(blob > 0 ? { properties: { blob: "x".repeat(blob) } } : {}) }));
}

describe("PushClient", () => {
  it("sends the batch with the ingest key and merges the result", async () => {
    const { push, requests } = client([ok(2)]);
    const result = await push.push(items(2));
    expect(result).toMatchObject({ accepted: 2, ok: true, batches: 1 });
    const request = requests[0];
    expect(request?.method).toBe("POST");
    expect(request?.url).toBe("https://foresignal.example/v1/external/sources/ext-river-gauges/items");
    expect(request?.headers["X-Foresignal-Ingest-Key"]).toBe("eik_secret");
    expect(request?.headers["Content-Type"]).toBe("application/json");
    expect(request?.headers["User-Agent"]).toMatch(/^foresignal-edp-typescript\//);
    const body = bodyJson(request);
    expect(body).toMatchObject({ protocol: "foresignal-edp/1.0", sourceId: "ext-river-gauges", sentAt: "2026-10-11T18:00:00Z" });
    expect((body["items"] as { id: string }[]).map((i) => i.id)).toEqual(["i-0", "i-1"]);
    expect(JSON.stringify(push)).not.toContain("eik_secret");
  });

  it("splits batches at the item limit", () => {
    const prepared = client().push.prepare(items(25_001));
    expect(prepared.map((p) => p.itemCount)).toEqual([10_000, 10_000, 5_001]);
    expect(prepared.map((p) => p.firstIndex)).toEqual([0, 10_000, 20_000]);
    expect(client([], { maxItems: 3 }).push.prepare(items(7)).map((p) => p.itemCount)).toEqual([3, 3, 1]);
  });

  it("splits batches at the byte limit and never exceeds it", () => {
    const prepared = client().push.prepare(items(30, 400_000));
    expect(prepared.length).toBeGreaterThanOrEqual(3);
    expect(prepared.every((p) => p.body.length <= 5 * 1024 * 1024)).toBe(true);
    expect(prepared.reduce((n, p) => n + p.itemCount, 0)).toBe(30);
    for (const p of prepared) expect((JSON.parse(decoder.decode(p.body)).items as unknown[]).length).toBe(p.itemCount);
    const tight = client([], { maxBytes: 4096 }).push.prepare(items(40));
    expect(tight.every((p) => p.body.length <= 4096)).toBe(true);
  });

  it("counts bytes exactly at the boundary", () => {
    const two = client().push.prepare(items(2))[0]!;
    expect(client([], { maxBytes: two.body.length }).push.prepare(items(2))).toHaveLength(1);
    expect(client([], { maxBytes: two.body.length - 1 }).push.prepare(items(2)).map((p) => p.itemCount)).toEqual([1, 1]);
  });

  it("refuses an item that cannot fit", () => {
    expect(() => client([], { maxBytes: 1024 }).push.prepare([...items(1), ...items(1, 2000)])).toThrow(ItemTooLargeError);
  });

  it("sends NDJSON with a header line", async () => {
    const { push, requests } = client([ok(3)], { ndjson: true, maxItems: 3 });
    await push.push(items(3));
    expect(requests[0]?.headers["Content-Type"]).toBe("application/x-ndjson");
    const lines = decoder.decode(requests[0]?.body).trimEnd().split("\n");
    expect(lines).toHaveLength(4);
    const header = JSON.parse(lines[0] ?? "{}");
    expect(header.type).toBeUndefined();
    expect(header.items).toBeUndefined();
    expect((decodeNdjson(requests[0]!.body).items as { id: string }[]).map((i) => i.id)).toEqual(["i-0", "i-1", "i-2"]);
    expect(client([], { ndjson: true, maxBytes: 2048 }).push.prepare(items(50)).every((p) => p.body.length <= 2048)).toBe(true);
  });

  it("sends nothing for no items", async () => {
    const { push, requests } = client();
    expect((await push.push([])).batches).toBe(0);
    expect(requests).toHaveLength(0);
  });

  it("retries 429 and 503 honoring Retry-After with byte-identical bodies", async () => {
    const { push, requests, sleeps } = client([problemStep(429, "RATE_LIMITED", "7"), problemStep(503, "INGEST_BUSY", "30"), ok(2)]);
    expect((await push.push(items(2))).accepted).toBe(2);
    expect(sleeps).toEqual([7000, 30000]);
    expect(requests).toHaveLength(3);
    expect(new Set(requests.map((r) => decoder.decode(r.body))).size).toBe(1);
  });

  it("honors Retry-After as an HTTP date", async () => {
    const later = new Date(Date.now() + 120_000).toUTCString();
    const { push, sleeps } = client([problemStep(503, "INGEST_BUSY", later), ok(1)]);
    await push.push(items(1));
    expect(sleeps[0]).toBeGreaterThan(100_000);
    expect(sleeps[0]).toBeLessThanOrEqual(121_000);
  });

  it("backs off exponentially with jitter up to the ceiling", async () => {
    const script = [...Array.from({ length: 5 }, () => problemStep(503, "INGEST_BUSY")), ok(1)];
    const { push, sleeps } = client(script, { initialBackoffMs: 1000, maxBackoffMs: 8000, maxRetries: 5 });
    await push.push(items(1));
    expect(sleeps).toEqual([750, 1500, 3000, 6000, 6000]);
  });

  it("retries network failures", async () => {
    const { push, requests, sleeps } = client([new Error("reset"), new Error("reset"), ok(1)]);
    expect((await push.push(items(1))).accepted).toBe(1);
    expect(sleeps).toHaveLength(2);
    expect(new Set(requests.map((r) => decoder.decode(r.body))).size).toBe(1);
  });

  it("bounds retries", async () => {
    const { push, requests } = client(Array.from({ length: 10 }, () => problemStep(429, "RATE_LIMITED", "1")), { maxRetries: 2 });
    const error = await push.push(items(1)).catch((e: unknown) => e);
    expect(error).toBeInstanceOf(PushError);
    expect(error).toMatchObject({ status: 429, code: "RATE_LIMITED", retryable: true });
    expect(requests).toHaveLength(3);
    const network = client(Array.from({ length: 5 }, () => new Error("down")), { maxRetries: 1 });
    expect(await network.push.push(items(1)).catch((e: unknown) => e)).toMatchObject({ status: null, retryable: true });
  });

  it("fails instead of waiting for an excessive Retry-After", async () => {
    const { push, requests } = client([problemStep(429, "RATE_LIMITED", "3600")], { maxRetryAfterSeconds: 60 });
    expect(await push.push(items(1)).catch((e: unknown) => e)).toMatchObject({ retryable: true });
    expect(requests).toHaveLength(1);
  });

  it.each([[400, "SOURCE_MISMATCH"], [401, "UNAUTHORIZED"], [409, "SOURCE_PENDING_APPROVAL"], [413, "PAYLOAD_TOO_LARGE"]])(
    "does not retry %i %s", async (status, code) => {
      const { push, requests } = client([problemStep(status, code)]);
      const error = await push.push(items(1)).catch((e: unknown) => e) as PushError;
      expect(error).toMatchObject({ status, code, retryable: false });
      expect(error.problem).not.toBeNull();
      expect(error.message).toContain(code);
      expect(requests).toHaveLength(1);
    });

  it("keeps the remaining batches for an identical re-send", async () => {
    const { push, requests } = client([ok(2), problemStep(409, "SOURCE_SUSPENDED")], { maxItems: 2 });
    const error = await push.push(items(5)).catch((e: unknown) => e) as PushError;
    expect(error.result.accepted).toBe(2);
    expect(error.remaining.map((b) => b.itemCount)).toEqual([2, 1]);
    const failedBody = decoder.decode(requests[1]?.body);
    const resend = fakeFetch([ok(2), ok(1)]);
    push.fetchImpl = resend.fetch;
    expect((await push.sendAll(error.remaining)).accepted).toBe(3);
    expect(decoder.decode(resend.requests[0]?.body)).toBe(failedBody);
  });

  it("maps rejection indexes to push positions", async () => {
    const rejected = { index: 1, stream: "levels", id: "i-3", reason: "PERIOD_INVALID", schemaErrors: [{ path: "", message: "bad" }] };
    const { push } = client([ok(2), ok(1, [rejected])], { maxItems: 2 });
    const result = await push.push(items(4));
    expect(result.accepted).toBe(3);
    expect(result.ok).toBe(false);
    expect(result.rejected[0]).toEqual({ index: 3, stream: "levels", id: "i-3", reason: "PERIOD_INVALID",
      schemaErrors: [{ path: "", message: "bad" }] });
  });

  it("pages a snapshot", async () => {
    const { push, requests } = client([], { maxItems: 2 });
    await push.pushSnapshot("cameras", Array.from({ length: 5 }, (_, i) => deleteItem("cameras", String(i))),
      { scope: "north", snapshotId: "snap" });
    expect(requests.map((r) => bodyJson(r)["snapshot"])).toEqual([
      { id: "snap", stream: "cameras", scope: "north", page: 1, final: false },
      { id: "snap", stream: "cameras", scope: "north", page: 2, final: false },
      { id: "snap", stream: "cameras", scope: "north", page: 3, final: true },
    ]);
  });

  it("sends an empty snapshot as one final page, in the NDJSON header", async () => {
    const { push, requests } = client([], { ndjson: true });
    await push.pushSnapshot("cameras", []);
    expect(requests).toHaveLength(1);
    const header = JSON.parse(decoder.decode(requests[0]?.body).split("\n")[0] ?? "{}");
    expect(header.snapshot).toMatchObject({ page: 1, final: true });
    expect(header.snapshot.id).toBeTruthy();
  });

  it("reports status and updates the manifest", async () => {
    const { push, requests } = client([ok(1), { status: 200, body: { state: "active" } }]);
    await push.reportStatus("layout-changed", "the dashboard moved", { stream: "levels" });
    expect(bodyJson(requests[0])["items"]).toEqual([
      { stream: "levels", type: "status", health: "layout-changed", message: "the dashboard moved" }]);
    expect(await push.updateManifest({ protocol: "foresignal-edp/1.0" })).toEqual({ state: "active" });
    expect(requests[1]?.method).toBe("PUT");
    expect(requests[1]?.url).toMatch(/\/v1\/external\/sources\/ext-river-gauges\/manifest$/);
  });

  it("can omit sourceId and tolerates empty success bodies", async () => {
    const { push, requests } = client([{ status: 204 }], { includeSourceId: false });
    const result = await push.push(items(1));
    expect(bodyJson(requests[0])["sourceId"]).toBeUndefined();
    expect(result).toMatchObject({ batches: 1, accepted: 0 });
  });

  it("validates its options", () => {
    const base = { baseUrl: "https://x", sourceId: "ext-a", ingestKey: "k" };
    expect(() => new PushClient({ ...base, maxItems: 10_001 })).toThrow(RangeError);
    expect(() => new PushClient({ ...base, maxBytes: 6 * 1024 * 1024 })).toThrow(RangeError);
    expect(() => new PushClient({ ...base, maxRetries: -1 })).toThrow(RangeError);
    expect(() => new PushClient({ ...base, sourceId: "" })).toThrow(RangeError);
  });
});

describe("parseRetryAfter", () => {
  it("reads seconds and HTTP dates", () => {
    expect(parseRetryAfter("12")).toBe(12);
    expect(parseRetryAfter("-5")).toBe(0);
    expect(parseRetryAfter(null)).toBeNull();
    expect(parseRetryAfter(" ")).toBeNull();
    expect(parseRetryAfter("soon")).toBeNull();
    expect(parseRetryAfter("Sun, 11 Oct 2026 18:01:00 GMT", NOW.getTime())).toBe(60);
  });
});
