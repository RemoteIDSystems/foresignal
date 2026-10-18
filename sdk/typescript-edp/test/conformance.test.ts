/**
 * The 024 EDP 1.0 conformance fixtures (`src/test/resources/edp/conformance`, by relative path):
 * valid manifests validate, invalid ones are refused for the reason their name gives, every item of
 * every valid batch (JSON and NDJSON) is accepted, and every invalid batch's one item is rejected
 * with the reason in `x-expect`.
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { decodeNdjson, validateBatch, validateItems, validateManifest } from "../src/index.ts";
import { CONFORMANCE_DIR, CONFORMANCE_RECEIVED_AT, HAS_CONFORMANCE, fixtureFiles, loadJson } from "./helpers.ts";

const INVALID_MANIFESTS: Record<string, [string, string]> = {
  "protocol-too-new.json": ["/protocol", "newer"],
  "empty-transport.json": ["/transport", "delivery mechanism"],
  "duplicate-stream-ids.json": ["/streams/1/id", "duplicate stream id"],
  "layer-observation.json": ["/streams/0/itemType", "layer stream carries feature or event"],
  "remote-ref.json": ["/streams/0/payloadSchema/$ref", "only local references"],
  "long-pattern.json": ["/streams/0/payloadSchema/properties/code/pattern", "maximum is 256"],
};

const manifestOf = (name: string): Record<string, unknown> => loadJson(join(CONFORMANCE_DIR, "manifests", "valid", name));

describe.skipIf(!HAS_CONFORMANCE)("EDP 1.0 conformance fixtures", () => {
  it("has every expected file", () => {
    expect(fixtureFiles("manifests/invalid")).toEqual(Object.keys(INVALID_MANIFESTS).sort());
    expect(fixtureFiles("manifests/valid")).toEqual(["hormuz-example.json", "pull-atomic.json", "push-layer.json"]);
    const validTypes = fixtureFiles("batches/valid").map((f) => f.replace(/\.json$/, ""));
    for (const type of ["feature", "event", "observation", "document", "forecast", "delete", "status"]) {
      expect(validTypes).toContain(type);
    }
    expect(fixtureFiles("batches/valid", ".ndjson")).toEqual(["event-and-delete.ndjson"]);
    expect(fixtureFiles("batches/invalid")).toHaveLength(9);
  });

  it.each(fixtureFiles("manifests/valid"))("valid manifest %s validates", (file) => {
    const check = validateManifest(manifestOf(file));
    expect(check.errors).toEqual([]);
    expect(check.valid).toBe(true);
    expect(check.protocolUnsupported).toBe(false);
    expect(check.streams.size).toBeGreaterThan(0);
  });

  it.each(fixtureFiles("manifests/invalid"))("invalid manifest %s is refused for its reason", (file) => {
    const check = validateManifest(loadJson(join(CONFORMANCE_DIR, "manifests", "invalid", file)));
    expect(check.valid).toBe(false);
    const [path, text] = INVALID_MANIFESTS[file] as [string, string];
    expect(check.errors.some((e) => e.path === path && e.message.includes(text)), JSON.stringify(check.errors)).toBe(true);
    expect(check.protocolUnsupported).toBe(file === "protocol-too-new.json");
    expect(check.streams.size).toBe(0);
  });

  it("accepts protocol 1.9 when the validator knows minor 9", () => {
    const manifest = loadJson(join(CONFORMANCE_DIR, "manifests", "invalid", "protocol-too-new.json"));
    expect(validateManifest(manifest, { maxMinor: 9 }).valid).toBe(true);
  });

  it.each(fixtureFiles("batches/valid"))("valid batch %s accepts every item", (file) => {
    const batch = loadJson(join(CONFORMANCE_DIR, "batches", "valid", file));
    const check = validateBatch(batch, manifestOf(batch["x-manifest"] as string), { receivedAt: CONFORMANCE_RECEIVED_AT });
    expect(check.code).toBeNull();
    expect(check.items?.rejected).toEqual([]);
    expect(check.items?.valid).toHaveLength((batch["items"] as unknown[]).length);
    expect(check.ok).toBe(true);
  });

  it("valid NDJSON batch accepts every item", () => {
    const batch = decodeNdjson(readFileSync(join(CONFORMANCE_DIR, "batches", "valid", "event-and-delete.ndjson")));
    expect(batch["sourceId"]).toBe("hormuz-transits");
    expect((batch.items as { type: string }[]).map((i) => i.type)).toEqual(["event", "delete"]);
    const check = validateBatch(batch, manifestOf("hormuz-example.json"), { receivedAt: CONFORMANCE_RECEIVED_AT });
    expect(check.ok).toBe(true);
  });

  it.each(fixtureFiles("batches/invalid"))("invalid batch %s rejects its one item with x-expect", (file) => {
    const batch = loadJson(join(CONFORMANCE_DIR, "batches", "invalid", file));
    const items = batch["items"] as unknown[];
    expect(items).toHaveLength(1);
    const check = validateItems(items, manifestOf(batch["x-manifest"] as string), { receivedAt: CONFORMANCE_RECEIVED_AT });
    expect(check.valid).toEqual([]);
    expect(check.rejected).toHaveLength(1);
    expect(check.rejected[0]?.reason).toBe(batch["x-expect"]);
    expect(check.rejected[0]?.index).toBe(0);
    expect(check.rejected[0]?.schemaErrors.length).toBeGreaterThan(0);
  });
});
