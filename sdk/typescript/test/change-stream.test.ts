/**
 * ChangeStream against an in-process SSE server: resume with the last id, reset with a snapshot, heartbeat
 * watchdog, observedAt guard, stream tickets, retry hints and the multiplexed events endpoint.
 */
import { afterEach, describe, expect, it } from "vitest";
import { ApiError } from "../src/errors.js";
import {
  ChangeStream,
  ObservedAtGuard,
  compositeCursorValue,
  withCompositeCursorValue,
  type StreamSignal,
} from "../src/realtime/change-stream.js";
import { Backoff } from "../src/realtime/backoff.js";
import { collect, json, recordingSleep, sse, startServer, type MockServer } from "./mock-server.js";

let server: MockServer | undefined;

afterEach(async () => {
  await server?.close();
  server = undefined;
});

const change = (seq: number, externalId = "ae1234", observedAt = "2026-10-18T00:00:00Z") => ({
  seq, source: "adsb", externalId, kind: "aircraft", version: 1, observedAt,
});

describe("ChangeStream on /v1/changes/stream", () => {
  it("resumes after a disconnect with the last change id", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      if (req.path !== "/v1/changes/stream") {
        return json(res, 404, {});
      }
      connection++;
      if (connection === 1) {
        sse(res, [{ id: "5", event: "change", data: change(5) }, { id: "6", event: "change", data: change(6) }]);
      } else {
        sse(res, [{ id: "7", event: "change", data: change(7) }], true);
      }
    });
    const { sleep } = recordingSleep();
    const stream = new ChangeStream({ baseUrl: server.url, token: "fs_test", after: 4, sleep });
    const signals = await collect(stream, (s) => s.type === "change" && s.data.seq === 7);

    const streams = server.requestsTo("/v1/changes/stream");
    expect(streams).toHaveLength(2);
    expect(streams[0]!.headers["last-event-id"]).toBe("4");
    expect(streams[0]!.headers["authorization"]).toBe("Bearer fs_test");
    expect(streams[1]!.headers["last-event-id"]).toBe("6");
    expect(signals.filter((s) => s.type === "change").map((s) => (s as { id?: string }).id)).toEqual(["5", "6", "7"]);
    expect(signals.some((s) => s.type === "reconnecting")).toBe(true);
    expect(stream.lastSeq).toBe(7);
    expect(stream.lastEventId).toBe("7");
  });

  it("on reset resync_required pages the snapshot and reopens at the watermark", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      if (req.path === "/v1/records") {
        const cursor = req.query.get("cursor");
        if (!cursor) {
          return json(res, 200, { items: [{ source: "adsb", externalId: "a", observedAt: "2026-10-18T00:00:10Z" }],
            nextCursor: "p2", watermarkSeq: 500 });
        }
        return json(res, 200, { items: [{ source: "adsb", externalId: "b", observedAt: "2026-10-18T00:00:11Z" }],
          nextCursor: null, watermarkSeq: 500 });
      }
      connection++;
      if (connection === 1) {
        sse(res, [{ id: "10", event: "change", data: change(10) },
          { event: "reset", data: { reason: "resync_required", resumeAfter: null, watermarkSeq: null } }]);
      } else {
        sse(res, [{ id: "501", event: "change", data: change(501) }], true);
      }
    });
    const { sleep } = recordingSleep();
    const stream = new ChangeStream({
      baseUrl: server.url, after: 9, sleep, filters: { source: ["adsb"], kind: "aircraft", live: true, touches: true },
    });
    const signals = await collect(stream, (s) => s.type === "change" && s.data.seq === 501);

    const types = signals.map((s) => s.type);
    expect(types).toEqual(["open", "change", "reset", "snapshot", "snapshot", "reconnecting", "open", "change"]);
    const snapshots = signals.filter((s): s is Extract<StreamSignal, { type: "snapshot" }> => s.type === "snapshot");
    expect(snapshots.map((s) => s.done)).toEqual([false, true]);
    expect(snapshots.every((s) => s.cause === "reset" && s.watermarkSeq === 500)).toBe(true);
    expect(snapshots.flatMap((s) => s.items.map((i) => i.externalId))).toEqual(["a", "b"]);

    const records = server.requestsTo("/v1/records");
    expect(records).toHaveLength(2);
    expect(records[0]!.query.get("order")).toBe("id");
    expect(records[0]!.query.getAll("source")).toEqual(["adsb"]);
    expect(records[0]!.query.get("kind")).toBe("aircraft");
    expect(records[0]!.query.has("live")).toBe(false);
    expect(records[0]!.query.has("touches")).toBe(false);
    expect(records[1]!.query.get("cursor")).toBe("p2");

    const streams = server.requestsTo("/v1/changes/stream");
    expect(streams[1]!.headers["last-event-id"]).toBe("500");
    expect(stream.guard.get(ObservedAtGuard.key("adsb", "b"))).toBe(Date.parse("2026-10-18T00:00:11Z"));
  });

  it("on reset buffer_overflow reopens at resumeAfter without a snapshot", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      connection++;
      if (connection === 1) {
        sse(res, [{ id: "40", event: "change", data: change(40) },
          { event: "reset", data: { reason: "buffer_overflow", resumeAfter: 42, watermarkSeq: 90 } }]);
      } else {
        sse(res, [{ id: "43", event: "change", data: change(43) }], true);
      }
    });
    const stream = new ChangeStream({ baseUrl: server.url, sleep: recordingSleep().sleep });
    await collect(stream, (s) => s.type === "change" && s.data.seq === 43);
    expect(server.requestsTo("/v1/records")).toHaveLength(0);
    expect(server.requestsTo("/v1/changes/stream")[1]!.headers["last-event-id"]).toBe("42");
  });

  it("re-pages the live state on resync and keeps the stream open", async () => {
    server = await startServer((req, res) => {
      if (req.path === "/v1/records") {
        return json(res, 200, { items: [{ source: "adsb", externalId: "a" }], nextCursor: null, watermarkSeq: 77 });
      }
      sse(res, [{ event: "resync", data: { reason: "live_dropped", watermarkSeq: 77 } },
        { id: "78", event: "change", data: change(78) }], true);
    });
    const stream = new ChangeStream({ baseUrl: server.url, sleep: recordingSleep().sleep });
    const signals = await collect(stream, (s) => s.type === "change");
    expect(signals.map((s) => s.type)).toEqual(["open", "resync", "snapshot", "change"]);
    expect(server.requestsTo("/v1/changes/stream")).toHaveLength(1);
    const snapshot = signals[2] as Extract<StreamSignal, { type: "snapshot" }>;
    expect(snapshot.cause).toBe("resync");
    expect(snapshot.watermarkSeq).toBe(77);
  });

  it("reconnects when no event or heartbeat arrives within 2.5 x the heartbeat interval", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      connection++;
      if (connection === 1) {
        // One heartbeat, then silence with the connection left open.
        sse(res, [{ event: "heartbeat", data: {} }], true);
      } else {
        sse(res, [{ id: "1", event: "change", data: change(1) }], true);
      }
    });
    const { delays, sleep } = recordingSleep();
    const stream = new ChangeStream({ baseUrl: server.url, heartbeatIntervalMs: 40, sleep });
    expect(stream.watchdogTimeoutMs).toBe(100);
    const started = Date.now();
    const signals = await collect(stream, (s) => s.type === "change");
    const reconnect = signals.find((s) => s.type === "reconnecting") as Extract<StreamSignal, { type: "reconnecting" }>;
    expect(reconnect.reason).toContain("HeartbeatTimeoutError");
    expect(signals.filter((s) => s.type === "heartbeat")).toHaveLength(1);
    expect(Date.now() - started).toBeGreaterThanOrEqual(90);
    expect(delays[0]).toBeGreaterThanOrEqual(1000);
    expect(server.requestsTo("/v1/changes/stream")).toHaveLength(2);
  });

  it("applies live and touch events only when observedAt is not older than the stored one", async () => {
    server = await startServer((req, res) => {
      sse(res, [
        { event: "live", data: { sourceId: "adsb", externalId: "A", observedAt: "2026-10-18T00:00:02Z", version: 1, kind: "aircraft" } },
        { event: "live", data: { sourceId: "adsb", externalId: "A", observedAt: "2026-10-18T00:00:01Z", version: 1, kind: "aircraft" } },
        { event: "touch", data: { sourceId: "adsb", externalId: "A", observedAt: "2026-10-18T00:00:03Z" } },
        { event: "live", data: { sourceId: "adsb", externalId: "B", observedAt: "2026-10-18T00:00:01Z", version: 1, kind: "aircraft" } },
        { id: "9", event: "change", data: change(9, "A", "2026-10-18T00:00:05Z") },
        { event: "live", data: { sourceId: "adsb", externalId: "A", observedAt: "2026-10-18T00:00:04Z", version: 1, kind: "aircraft" } },
        { event: "touch", data: { sourceId: "adsb", externalId: "A", observedAt: "2026-10-18T00:00:05Z" } },
        { event: "heartbeat", data: {} },
      ], true);
    });
    const stream = new ChangeStream({ baseUrl: server.url, sleep: recordingSleep().sleep });
    const signals = await collect(stream, (s) => s.type === "heartbeat");
    const applied = signals
      .filter((s) => s.type === "live" || s.type === "touch")
      .map((s) => `${s.type}:${(s as { data: { externalId: string; observedAt: string } }).data.externalId}@${(s as { data: { observedAt: string } }).data.observedAt.slice(17, 19)}`);
    expect(applied).toEqual(["live:A@02", "touch:A@03", "live:B@01", "touch:A@05"]);
    expect(stream.staleDropped).toBe(2);
  });

  it("uses a stream ticket instead of the Authorization header and mints a new one on 401", async () => {
    let tickets = 0;
    let connection = 0;
    server = await startServer((req, res) => {
      if (req.path === "/v1/stream-tickets") {
        tickets++;
        return json(res, 201, { ticket: `fst_t${tickets}`, purpose: "stream", expiresAt: "2026-10-18T00:01:00Z" });
      }
      connection++;
      if (connection === 1) {
        sse(res, [{ id: "8", event: "change", data: change(8) }]);
      } else if (connection === 2) {
        json(res, 401, { status: 401, code: "UNAUTHORIZED" });
      } else {
        sse(res, [{ id: "9", event: "change", data: change(9) }], true);
      }
    });
    const stream = new ChangeStream({ baseUrl: server.url, token: "fs_secret", useTicket: true, after: 7,
      sleep: recordingSleep().sleep });
    await collect(stream, (s) => s.type === "change" && s.data.seq === 9);

    const mints = server.requestsTo("/v1/stream-tickets", "POST");
    expect(mints).toHaveLength(2);
    expect(mints[0]!.headers["authorization"]).toBe("Bearer fs_secret");
    expect(JSON.parse(mints[0]!.body)).toEqual({ purpose: "stream" });

    const streams = server.requestsTo("/v1/changes/stream");
    expect(streams).toHaveLength(3);
    for (const s of streams) {
      expect(s.headers["authorization"]).toBeUndefined();
      expect(s.headers["last-event-id"]).toBeUndefined();
    }
    expect(streams[0]!.query.get("ticket")).toBe("fst_t1");
    expect(streams[0]!.query.get("after")).toBe("7");
    expect(streams[1]!.query.get("ticket")).toBe("fst_t1");
    expect(streams[1]!.query.get("after")).toBe("8");
    expect(streams[2]!.query.get("ticket")).toBe("fst_t2");
    expect(streams[2]!.query.get("after")).toBe("8");
  });

  it("honors Retry-After and retry: in the reconnect delay", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      connection++;
      if (connection === 1) {
        json(res, 429, { status: 429, code: "STREAM_LIMIT" }, { "Retry-After": "3" });
      } else if (connection === 2) {
        sse(res, [{ retry: 5000, comment: "hint" }, { id: "1", event: "change", data: change(1) }]);
      } else {
        sse(res, [{ id: "2", event: "change", data: change(2) }], true);
      }
    });
    const { delays, sleep } = recordingSleep();
    const stream = new ChangeStream({ baseUrl: server.url, sleep, backoff: { random: () => 0.5 } });
    await collect(stream, (s) => s.type === "change" && s.data.seq === 2);
    expect(delays[0]).toBeGreaterThanOrEqual(3000);
    expect(delays[1]).toBeGreaterThanOrEqual(5000);
  });

  it("fails on a non-retryable status", async () => {
    server = await startServer((req, res) => json(res, 403, { status: 403, code: "INSUFFICIENT_SCOPE" }));
    const stream = new ChangeStream({ baseUrl: server.url, sleep: recordingSleep().sleep });
    await expect(collect(stream, () => false)).rejects.toBeInstanceOf(ApiError);
  });

  it("can be iterated only once and stops on close", async () => {
    server = await startServer((req, res) => sse(res, [{ event: "heartbeat", data: {} }], true));
    const stream = new ChangeStream({ baseUrl: server.url, sleep: recordingSleep().sleep });
    const signals = await collect(stream, (s) => s.type === "heartbeat");
    expect(signals.at(-1)?.type).toBe("heartbeat");
    expect(stream.closed).toBe(true);
    expect(() => stream[Symbol.asyncIterator]()).toThrow();
  });
});

describe("ChangeStream on /v1/events/stream", () => {
  it("tracks the composite cursor, yields other topics and resumes with Last-Event-ID", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      connection++;
      if (connection === 1) {
        sse(res, [
          { event: "hello", data: { streamId: "AAAAAAAAAAAAAAAAAAAAAA", topics: ["changes", "alerts"], watermarkSeq: 100, filterVersion: 1 } },
          { id: "c=101;a=5", event: "change", data: change(101) },
          { id: "c=101;a=6", event: "alert", data: { id: 6, state: "firing" } },
        ]);
      } else {
        sse(res, [{ id: "c=102;a=6", event: "change", data: change(102) }], true);
      }
    });
    const stream = new ChangeStream({
      baseUrl: server.url, endpoint: "events", sleep: recordingSleep().sleep,
      filters: { topics: "changes,alerts", "changes.source": "adsb" },
    });
    const signals = await collect(stream, (s) => s.type === "change" && s.data.seq === 102);
    expect(stream.streamId).toBe("AAAAAAAAAAAAAAAAAAAAAA");
    const alert = signals.find((s) => s.type === "event") as Extract<StreamSignal, { type: "event" }>;
    expect(alert.event).toBe("alert");
    expect(alert.id).toBe("c=101;a=6");
    const streams = server.requestsTo("/v1/events/stream");
    expect(streams[0]!.query.get("topics")).toBe("changes,alerts");
    expect(streams[0]!.query.get("changes.source")).toBe("adsb");
    expect(streams[1]!.headers["last-event-id"]).toBe("c=101;a=6");
    expect(stream.lastSeq).toBe(102);
    expect(stream.lastEventId).toBe("c=102;a=6");
  });

  it("reopens at the watermark with the other topic cursors kept after a resync_required reset", async () => {
    let connection = 0;
    server = await startServer((req, res) => {
      if (req.path === "/v1/records") {
        expect(req.query.get("source")).toBe("adsb");
        return json(res, 200, { items: [], nextCursor: null, watermarkSeq: 900 });
      }
      connection++;
      if (connection === 1) {
        sse(res, [{ id: "c=10;a=3", event: "change", data: change(10) },
          { event: "reset", data: { reason: "resync_required" } }]);
      } else {
        sse(res, [{ id: "c=901;a=3", event: "change", data: change(901) }], true);
      }
    });
    const stream = new ChangeStream({ baseUrl: server.url, endpoint: "events", sleep: recordingSleep().sleep,
      filters: { "changes.source": "adsb", "changes.live": true } });
    await collect(stream, (s) => s.type === "change" && s.data.seq === 901);
    expect(server.requestsTo("/v1/events/stream")[1]!.headers["last-event-id"]).toBe("c=900;a=3");
  });

  it("sends the cursor as lastEventId when a ticket is used", async () => {
    server = await startServer((req, res) => {
      if (req.path === "/v1/stream-tickets") {
        return json(res, 201, { ticket: "fst_x", purpose: "stream", expiresAt: "2026-10-18T00:01:00Z" });
      }
      sse(res, [{ event: "heartbeat", data: {} }], true);
    });
    const stream = new ChangeStream({ baseUrl: server.url, endpoint: "events", lastEventId: "c=5;a=1", useTicket: true,
      sleep: recordingSleep().sleep });
    await collect(stream, (s) => s.type === "heartbeat");
    const req = server.requestsTo("/v1/events/stream")[0]!;
    expect(req.query.get("lastEventId")).toBe("c=5;a=1");
    expect(req.query.get("ticket")).toBe("fst_x");
    expect(req.headers["last-event-id"]).toBeUndefined();
  });
});

describe("helpers", () => {
  it("reads and writes composite cursors", () => {
    expect(compositeCursorValue("c=1234;a=56;n=7", "a")).toBe(56);
    expect(compositeCursorValue("a=56", "c")).toBeUndefined();
    expect(compositeCursorValue(undefined, "c")).toBeUndefined();
    expect(withCompositeCursorValue("c=1;a=2", "c", 9)).toBe("c=9;a=2");
    expect(withCompositeCursorValue(undefined, "c", 9)).toBe("c=9");
    expect(withCompositeCursorValue("a=2", "c", 9)).toBe("c=9;a=2");
  });

  it("backs off exponentially from 1 s to 60 s with jitter and server floors", () => {
    const backoff = new Backoff({ random: () => 0.5 });
    const delays = Array.from({ length: 9 }, () => backoff.next());
    expect(delays).toEqual([1000, 2000, 4000, 8000, 16000, 32000, 60000, 60000, 60000]);
    backoff.reset();
    expect(backoff.next(90_000)).toBe(90_000);
    const low = new Backoff({ random: () => 0 });
    expect(low.next()).toBe(1000);
    expect(low.next()).toBe(1600);
    const high = new Backoff({ random: () => 0.999 });
    high.next();
    expect(high.next()).toBeLessThanOrEqual(2400);
  });

  it("guards observedAt per entity", () => {
    const guard = new ObservedAtGuard();
    const key = ObservedAtGuard.key("s", "e");
    expect(guard.accept(key, "2026-10-18T00:00:02Z")).toBe(true);
    expect(guard.accept(key, "2026-10-18T00:00:01Z")).toBe(false);
    expect(guard.accept(key, "2026-10-18T00:00:02Z")).toBe(true);
    expect(guard.accept(key, undefined)).toBe(true);
    expect(guard.size).toBe(1);
  });
});
