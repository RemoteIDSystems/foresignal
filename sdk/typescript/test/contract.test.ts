/**
 * Client-contract round trips on the fixtures shared by the three SDKs (sdk/contract-fixtures), plus resolve
 * batching against an in-process server.
 */
import { readFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it } from "vitest";
import { applyMergePatch, contextProblems, stripNullMembers, validateContext } from "../src/contract/context.js";
import { decodeDeepLink, encodeDeepLink } from "../src/contract/deep-link.js";
import { contractProblems } from "../src/contract/envelope.js";
import {
  decodeExternalId,
  encodeExternalId,
  formatObjectRef,
  isValidObjectRef,
  objectRefPathSegment,
  parseObjectRef,
  recordRef,
  remoteRef,
} from "../src/contract/object-ref.js";
import { batchRefs, resolveObjects } from "../src/contract/resolve.js";
import { ContextValidationError, DeepLinkError, ObjectRefError } from "../src/errors.js";
import { json, startServer, type MockServer } from "./mock-server.js";

const fixtures = resolve(dirname(fileURLToPath(import.meta.url)), "../../contract-fixtures");
const refs = JSON.parse(readFileSync(resolve(fixtures, "refs.json"), "utf8")) as {
  valid: {
    ref: string; type: string; id: string; pathSegment: string;
    record?: { sourceId: string; externalId: string; seq: number | null };
    sensor?: { kind: string; id: string };
    remote?: { instanceId: string; ref: string };
  }[];
  invalid: { ref: string; reason: string }[];
};
const contexts = JSON.parse(readFileSync(resolve(fixtures, "contexts.json"), "utf8")) as {
  deepLinkVersion: number;
  contexts: { name: string; workspaceId: string | null; context: Record<string, unknown>;
    decodedContext: Record<string, unknown>; payload: string; fragment: string; bytes: number }[];
  invalidPayloads: { name: string; payload: string; error: string }[];
  tooLarge: { name: string; workspaceId: string | null; context: Record<string, unknown>; error: string };
  mergePatches: { name: string; target: unknown; patch: unknown; result: unknown }[];
};

let server: MockServer | undefined;

afterEach(async () => {
  await server?.close();
  server = undefined;
});

describe("ObjectRef fixtures", () => {
  it.each(refs.valid)("parses and formats $ref", (c) => {
    const parsed = parseObjectRef(c.ref);
    expect(parsed.ref).toBe(c.ref);
    expect(parsed.type).toBe(c.type);
    expect(parsed.id).toBe(c.id);
    expect(formatObjectRef(parsed.type, parsed.id)).toBe(c.ref);
    expect(objectRefPathSegment(c.ref)).toBe(c.pathSegment);
    expect(parsed.record).toEqual(c.record);
    expect(parsed.sensor).toEqual(c.sensor);
    expect(parsed.remote).toEqual(c.remote);
    if (c.record) {
      expect(recordRef(c.record.sourceId, c.record.externalId, c.record.seq)).toBe(c.ref);
      expect(decodeExternalId(encodeExternalId(c.record.externalId))).toBe(c.record.externalId);
    }
    if (c.remote) {
      expect(remoteRef(c.remote.instanceId, c.remote.ref)).toBe(c.ref);
    }
    expect(isValidObjectRef(c.ref)).toBe(true);
  });

  it.each(refs.invalid)("rejects $reason", (c) => {
    expect(() => parseObjectRef(c.ref)).toThrow(ObjectRefError);
    expect(isValidObjectRef(c.ref)).toBe(false);
  });

  it("rejects types outside the contract's list when one is given", () => {
    expect(() => parseObjectRef("future-type:x", ["situation", "record"])).toThrow(ObjectRefError);
    expect(parseObjectRef("situation:x", ["situation"]).type).toBe("situation");
  });
});

describe("deep link fixtures", () => {
  it.each(contexts.contexts)("encodes $name exactly", (c) => {
    const link = encodeDeepLink(c.workspaceId, c.context);
    expect(link.payload).toBe(c.payload);
    expect(link.fragment).toBe(c.fragment);
    expect(link.bytes).toBe(c.bytes);
    expect(link.version).toBe(contexts.deepLinkVersion);
  });

  it.each(contexts.contexts)("decodes $name from the payload, the fragment and a URL", (c) => {
    for (const input of [c.payload, c.fragment, `https://globe.example.org/app${c.fragment}`]) {
      const decoded = decodeDeepLink(input);
      expect(decoded.version).toBe(1);
      expect(decoded.workspaceId).toBe(c.workspaceId);
      expect(decoded.context).toEqual(c.decodedContext);
    }
    expect(stripNullMembers(c.context)).toEqual(c.decodedContext);
  });

  it.each(contexts.invalidPayloads)("refuses $name", (c) => {
    try {
      decodeDeepLink(c.payload);
      expect.unreachable();
    } catch (error) {
      expect(error).toBeInstanceOf(DeepLinkError);
      expect((error as DeepLinkError).kind).toBe(c.error);
    }
  });

  it("refuses to encode a context over 2 KB", () => {
    try {
      encodeDeepLink(contexts.tooLarge.workspaceId, contexts.tooLarge.context);
      expect.unreachable();
    } catch (error) {
      expect((error as DeepLinkError).kind).toBe(contexts.tooLarge.error);
    }
  });
});

describe("context helpers", () => {
  it.each(contexts.mergePatches)("merge patch: $name", (c) => {
    const target = JSON.parse(JSON.stringify(c.target)) as unknown;
    expect(applyMergePatch(target, c.patch)).toEqual(c.result);
    expect(target).toEqual(c.target);
  });

  it("validates contexts like the server", () => {
    expect(contextProblems(contexts.contexts[0]!.context)).toEqual([]);
    const errors = contextProblems({
      camera: {}, timeRange: { end: "2026-10-01T00:00:00Z" }, playback: { state: "rewinding" },
      viewport: { bbox: [0, 0, 1, 1], center: [0, 0] }, layers: [""], activeSituationId: "nope",
      selectedRefs: ["situation:ok", "bad ref"],
    });
    expect(errors.map((e) => e.field).sort()).toEqual([
      "context.activeSituationId", "context.camera", "context.layers", "context.playback.state",
      "context.selectedRefs[1]", "context.timeRange.start", "context.viewport",
    ].sort());
    expect(() => validateContext({ timeRange: { start: "2026-10-02T00:00:00Z", end: "2026-10-01T00:00:00Z" } }))
      .toThrow(ContextValidationError);
    expect(contextProblems({ viewport: { center: [10, 10], radiusKm: 25_000 } })[0]?.field).toBe("context.viewport.radiusKm");
  });

  it("checks contract versions", () => {
    const ok = { contractVersion: "1.5", objectTypes: [], relationTypes: [], contextSchemaVersion: 1, deepLinkVersion: 1 };
    expect(contractProblems(ok)).toEqual([]);
    expect(contractProblems({ ...ok, contractVersion: "2.0", deepLinkVersion: 2 })).toHaveLength(2);
  });
});

describe("resolveObjects", () => {
  it("resolves in batches of at most 500, keeping order and dropping duplicates", async () => {
    server = await startServer((req, res) => {
      const body = JSON.parse(req.body) as { refs: string[]; asOf?: string };
      json(res, 200, {
        items: body.refs.filter((r) => !r.endsWith("7")).map((r) => ({ ref: r, type: "situation", title: r })),
        missing: body.refs.filter((r) => r.endsWith("7")),
        asOf: body.asOf ?? null,
      });
    });
    const input = Array.from({ length: 1200 }, (_, i) => `situation:S${i}`);
    const result = await resolveObjects({ baseUrl: server.url, token: "fs_t" }, [...input, "situation:S0"],
      { asOf: "2026-10-01T00:00:00Z" });
    const calls = server.requestsTo("/v1/objects/resolve", "POST");
    expect(calls.map((c) => (JSON.parse(c.body) as { refs: string[] }).refs.length)).toEqual([500, 500, 200]);
    expect(JSON.parse(calls[0]!.body).asOf).toBe("2026-10-01T00:00:00Z");
    expect(calls[0]!.headers["authorization"]).toBe("Bearer fs_t");
    expect(result.items.length + result.missing.length).toBe(1200);
    expect(result.missing).toContain("situation:S7");
    expect(result.items[0]!.ref).toBe("situation:S0");
    expect(result.asOf).toBe("2026-10-01T00:00:00Z");
  });

  it("validates references before sending and splits batches", async () => {
    await expect(resolveObjects({ baseUrl: "http://127.0.0.1:9" }, ["situation:ok", "nope"])).rejects.toThrow(ObjectRefError);
    expect(batchRefs(["a:1", "b:2", "c:3"], 2)).toEqual([["a:1", "b:2"], ["c:3"]]);
  });
});
