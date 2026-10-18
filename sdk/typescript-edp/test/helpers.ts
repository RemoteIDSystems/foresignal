/**
 * Shared test helpers.
 *
 * The EDP 1.0 conformance fixtures of prompt 024 are **referenced by relative path**, not copied:
 * `../../src/test/resources/edp/conformance` from this library's root (the repository's single copy,
 * also used by the server's `EdpManifestServiceTest` and `EdpValidationTest`). Set
 * `FORESIGNAL_EDP_CONFORMANCE_DIR` to run the suite outside the repository.
 */
import { existsSync, readFileSync, readdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import type { FetchLike } from "../src/index.ts";

/** This library's root directory. */
export const LIBRARY_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "..");
/** The repository root. */
export const REPOSITORY_ROOT = resolve(LIBRARY_ROOT, "..", "..");
/** The conformance fixture directory. */
export const CONFORMANCE_DIR = process.env["FORESIGNAL_EDP_CONFORMANCE_DIR"]
  ?? join(REPOSITORY_ROOT, "src", "test", "resources", "edp", "conformance");
/** The exported schema directory. */
export const EXPORTED_SCHEMAS_DIR = join(REPOSITORY_ROOT, "docs", "protocol", "edp-1.0");
/** True when the conformance fixtures are available. */
export const HAS_CONFORMANCE = existsSync(CONFORMANCE_DIR);
/** The receipt time the conformance batches are written against (their sentAt). */
export const CONFORMANCE_RECEIVED_AT = new Date("2026-10-11T18:00:00Z");

/** Reads a JSON file. */
export function loadJson<T = Record<string, unknown>>(path: string): T {
  return JSON.parse(readFileSync(path, "utf8")) as T;
}

/** Lists the files of a conformance subdirectory with an extension. */
export function fixtureFiles(sub: string, extension = ".json"): string[] {
  const directory = join(CONFORMANCE_DIR, sub);
  return existsSync(directory) ? readdirSync(directory).filter((f) => f.endsWith(extension)).sort() : [];
}

/** A small valid push manifest with one atomic observation stream (`s`). */
export function minimalManifest(streamOverrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    protocol: "foresignal-edp/1.0",
    source: { id: "test-source", name: "Test", publisher: { name: "X" }, originator: { id: "x", class: "operator" } },
    transport: { push: true },
    streams: [{ id: "s", mode: "atomic", itemType: "observation", kind: "k", geometry: "none", ...streamOverrides }],
  };
}

/** One request the fake fetch received. */
export interface RecordedRequest {
  url: string;
  method: string;
  headers: Record<string, string>;
  body: Uint8Array;
}

/** A scripted response, an error to throw, or a function of the request. */
export type Step = { status: number; headers?: Record<string, string>; body?: unknown } | Error
  | ((request: RecordedRequest) => { status: number; headers?: Record<string, string>; body?: unknown });

/** A scripted fetch: each call consumes one step; once the script runs out every call gets 200 `{accepted: 0}`. */
export function fakeFetch(script: Step[] = []): { fetch: FetchLike; requests: RecordedRequest[] } {
  const requests: RecordedRequest[] = [];
  const fetch: FetchLike = async (url, init) => {
    const body = typeof init.body === "string" ? new TextEncoder().encode(init.body) : init.body ?? new Uint8Array();
    const request = { url, method: init.method, headers: { ...init.headers }, body: new Uint8Array(body) };
    requests.push(request);
    const step = script.shift() ?? { status: 200, body: { accepted: 0, rejected: [] } };
    if (step instanceof Error) throw step;
    const response = typeof step === "function" ? step(request) : step;
    const headers = new Map(Object.entries(response.headers ?? {}).map(([k, v]) => [k.toLowerCase(), v]));
    const payload = response.body === undefined ? new Uint8Array()
      : new TextEncoder().encode(typeof response.body === "string" ? response.body : JSON.stringify(response.body));
    return {
      status: response.status,
      headers: { get: (name: string) => headers.get(name.toLowerCase()) ?? null },
      arrayBuffer: async () => payload.buffer.slice(payload.byteOffset, payload.byteOffset + payload.byteLength) as ArrayBuffer,
    };
  };
  return { fetch, requests };
}

/** A 200 `EdpIngestResult`. */
export function ok(accepted: number, rejected: unknown[] = []): Step {
  return { status: 200, body: { accepted, rejected } };
}

/** A ProblemDetail response, optionally with `Retry-After`. */
export function problemStep(status: number, code: string, retryAfter?: string): Step {
  return { status, headers: retryAfter === undefined ? {} : { "Retry-After": retryAfter },
    body: { status, code, title: "x", detail: `${code} detail` } };
}

/** Decodes a request body as JSON. */
export function bodyJson(request: RecordedRequest | undefined): Record<string, unknown> {
  if (!request) throw new Error("no request");
  return JSON.parse(new TextDecoder().decode(request.body)) as Record<string, unknown>;
}
