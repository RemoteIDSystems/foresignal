/** WorkspaceSync: remote patches in version order, own-origin events ignored, gap and reconnect re-reads, presence, 412 retry. */
import { afterEach, describe, expect, it } from "vitest";
import { ApiError } from "../src/errors.js";
import { WorkspaceSync, type ContextChange, type PresenceEvent } from "../src/contract/workspace-sync.js";
import { json, recordingSleep, sse, startServer, type MockServer } from "./mock-server.js";

const W = "01JAB0000000000000000000WS";
let server: MockServer | undefined;
let sync: WorkspaceSync | undefined;

afterEach(async () => {
  await sync?.stop();
  sync = undefined;
  await server?.close();
  server = undefined;
});

async function waitFor(predicate: () => boolean, timeoutMs = 5000): Promise<void> {
  const until = Date.now() + timeoutMs;
  while (!predicate()) {
    if (Date.now() > until) {
      throw new Error("timed out waiting for the condition");
    }
    await new Promise((r) => setTimeout(r, 10));
  }
}

const ctxEvent = (version: number, patch: Record<string, unknown>, originClientId: string | null) => ({
  event: "workspace.context",
  data: { workspaceId: W, version, patch, originClientId, principal: "p1", at: "2026-10-18T00:00:00Z" },
});

describe("WorkspaceSync", () => {
  it("applies remote patches in version order, ignores its own origin, re-reads on a gap and exposes presence", async () => {
    let gets = 0;
    server = await startServer((req, res) => {
      if (req.path === `/v1/workspaces/${W}` && req.method === "GET") {
        gets++;
        if (gets === 1) {
          return json(res, 200, { id: W, name: "w", context: { layers: ["a"] }, contextVersion: 3 }, { ETag: '"3"' });
        }
        return json(res, 200, { id: W, name: "w", context: { layers: ["refreshed"] }, contextVersion: 9 }, { ETag: '"9"' });
      }
      if (req.path === "/v1/events/stream") {
        return sse(res, [
          { event: "hello", data: { streamId: "AAAAAAAAAAAAAAAAAAAAAA", topics: ["workspace"], watermarkSeq: 0, filterVersion: 1 } },
          { event: "workspace.presence", data: { workspaceId: W, at: "2026-10-18T00:00:00Z",
            clients: [{ clientId: "me", clientKind: "map", connectedAt: "2026-10-18T00:00:00Z" },
              { clientId: "other", clientKind: "globe", principal: "p1", connectedAt: "2026-10-18T00:00:00Z" }],
            change: { type: "joined", clientId: "other" } } },
          ctxEvent(4, { layers: ["a", "b"] }, "other"),
          ctxEvent(5, { focusRef: "situation:own" }, "me"),
          ctxEvent(6, { timeRange: { start: "2026-10-01T00:00:00Z" } }, "mcp:session-1"),
          ctxEvent(6, { layers: ["duplicate"] }, "other"),
          ctxEvent(9, { layers: ["after-gap"] }, "other"),
        ], true);
      }
      json(res, 404, {});
    });
    const changes: ContextChange[] = [];
    const presence: PresenceEvent[] = [];
    sync = new WorkspaceSync({ baseUrl: server.url, token: "fs_t", workspaceId: W, clientId: "me", clientKind: "map",
      sleep: recordingSleep().sleep });
    sync.onContext((c) => changes.push(c));
    sync.onPresence((p) => presence.push(p));
    await sync.start();
    await waitFor(() => changes.some((c) => c.version === 9));

    expect(changes.map((c) => `${c.origin}:${c.version}`)).toEqual(["refresh:3", "remote:4", "remote:6", "refresh:9"]);
    const afterSix = changes[2]!.context;
    expect(afterSix.layers).toEqual(["a", "b"]);
    expect(afterSix.timeRange).toEqual({ start: "2026-10-01T00:00:00Z" });
    expect(afterSix.focusRef).toBeUndefined();
    expect(sync.version).toBe(9);
    expect(sync.context).toEqual({ layers: ["refreshed"] });
    expect(presence).toHaveLength(1);
    expect(sync.presence.map((c) => c.clientId)).toEqual(["me", "other"]);

    const stream = server.requestsTo("/v1/events/stream")[0]!;
    expect(stream.query.get("topics")).toBe("workspace");
    expect(stream.query.get("workspace.workspaceId")).toBe(W);
    expect(stream.query.get("workspace.clientId")).toBe("me");
    expect(stream.query.get("workspace.clientKind")).toBe("map");
    expect(stream.headers["authorization"]).toBe("Bearer fs_t");
  });

  it("re-reads the workspace after a reconnect", async () => {
    let streams = 0;
    let gets = 0;
    server = await startServer((req, res) => {
      if (req.path === `/v1/workspaces/${W}`) {
        gets++;
        return json(res, 200, { id: W, context: {}, contextVersion: gets });
      }
      streams++;
      if (streams === 1) {
        return sse(res, [{ event: "heartbeat", data: {} }]);
      }
      return sse(res, [{ event: "heartbeat", data: {} }], true);
    });
    sync = new WorkspaceSync({ baseUrl: server.url, workspaceId: W, sleep: recordingSleep().sleep });
    await sync.start();
    await waitFor(() => gets >= 2);
    expect(sync.clientId).toMatch(/^c-[0-9a-f]{16}$/);
    await waitFor(() => sync!.version === 2);
  });

  it("retries a stale PATCH with If-Match after re-reading and re-applying the intent", async () => {
    let gets = 0;
    let patches = 0;
    server = await startServer((req, res) => {
      if (req.method === "GET") {
        gets++;
        return gets === 1
          ? json(res, 200, { id: W, context: { layers: ["a"] }, contextVersion: 3 })
          : json(res, 200, { id: W, context: { layers: ["a", "x"] }, contextVersion: 4 });
      }
      patches++;
      if (patches === 1) {
        return json(res, 412, { status: 412, code: "WORKSPACE_CONTEXT_STALE", contextVersion: 4 });
      }
      const patch = JSON.parse(req.body) as { layers: string[] };
      return json(res, 200, { workspaceId: W, contextVersion: 5, context: { layers: patch.layers } }, { ETag: '"5"' });
    });
    sync = new WorkspaceSync({ baseUrl: server.url, token: "fs_t", workspaceId: W, clientId: "me" });
    const state = await sync.update((ctx) => ({ layers: [...(ctx.layers ?? []), "b"] }));

    expect(state.contextVersion).toBe(5);
    expect(sync.version).toBe(5);
    expect(sync.context).toEqual({ layers: ["a", "x", "b"] });
    const sent = server.requestsTo(`/v1/workspaces/${W}/context`, "PATCH");
    expect(sent.map((r) => r.headers["if-match"])).toEqual(["3", "4"]);
    expect(sent.map((r) => JSON.parse(r.body))).toEqual([{ layers: ["a", "b"] }, { layers: ["a", "x", "b"] }]);
    expect(sent.every((r) => r.headers["content-type"] === "application/merge-patch+json")).toBe(true);
    expect(sent.every((r) => r.headers["foresignal-client-id"] === "me")).toBe(true);
    expect(gets).toBe(2);
  });

  it("gives up after maxAttempts and passes other errors through", async () => {
    server = await startServer((req, res) => {
      if (req.method === "GET") {
        return json(res, 200, { id: W, context: {}, contextVersion: 1 });
      }
      return req.body.includes("forbidden")
        ? json(res, 403, { status: 403, code: "WORKSPACE_FORBIDDEN" })
        : json(res, 412, { status: 412, code: "WORKSPACE_CONTEXT_STALE" });
    });
    sync = new WorkspaceSync({ baseUrl: server.url, workspaceId: W, maxAttempts: 2 });
    const stale = await sync.update({ layers: ["x"] }).catch((e: unknown) => e);
    expect((stale as ApiError).status).toBe(412);
    expect(server.requestsTo(`/v1/workspaces/${W}/context`, "PATCH")).toHaveLength(2);
    const forbidden = await sync.update({ layers: ["forbidden"] }).catch((e: unknown) => e);
    expect((forbidden as ApiError).code).toBe("WORKSPACE_FORBIDDEN");
    const none = await sync.update(() => null);
    expect(none.contextVersion).toBe(1);
  });
});
