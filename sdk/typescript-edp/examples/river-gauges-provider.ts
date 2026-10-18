/**
 * Example EDP provider built on @foresignal/edp: river levels from a CSV, by push or pull.
 *
 * The CSV has the columns `gauge,period,level_m[,known_at]` (`river-levels.csv`); each row becomes an
 * `observation` of the atomic stream `levels` (kind `river-level`), series `{gauge}`.
 *
 *   push:     node examples/river-gauges-provider.ts push CSV BASE_URL INGEST_KEY
 *   serve:    node examples/river-gauges-provider.ts serve CSV PORT [PUBLIC_URL]
 *             (set RIVER_GAUGES_API_KEY to require the X-Api-Key header)
 *   manifest: node examples/river-gauges-provider.ts manifest [PUBLIC_URL]
 *
 * Node 22.18+ and 24 run TypeScript directly; in a provider project import from "@foresignal/edp"
 * instead of "../src/index.ts".
 */
import { readFileSync } from "node:fs";
import { createServer } from "node:http";
import { basename } from "node:path";
import { pathToFileURL } from "node:url";
import {
  ManifestBuilder, PullHandler, PushClient, SequenceSource, StatusBoard, StreamBuilder, headerAuth, observation,
  registeredSourceId, toNodeListener, validateItems,
} from "../src/index.ts";
import type { JsonObject, PushClientOptions } from "../src/index.ts";

/** The provider id; registered as `ext-river-gauges`. */
export const SOURCE_ID = "river-gauges";
/** The stream id. */
export const STREAM = "levels";
/** The pull credential header. */
export const API_KEY_HEADER = "X-Api-Key";

/** The provider's manifest: push and pull, one atomic observation stream suggesting a series binding. */
export function buildManifest(publicUrl = "http://localhost:8099", apiKeyRequired = false): JsonObject {
  return new ManifestBuilder(SOURCE_ID, "River gauges")
    .description("Daily river levels from a CSV export")
    .version("1.0.0")
    .publisher("Example Hydro")
    .license({ redistribution: "local-only" })
    .access("other", "low")
    .originator("example-hydro", "sensor-network")
    .push()
    .pull(`${publicUrl.replace(/\/+$/, "")}/edp/items`, apiKeyRequired
      ? { intervalSeconds: 300, auth: "header", header: API_KEY_HEADER } : { intervalSeconds: 300, auth: "none" })
    .stream(new StreamBuilder(STREAM, { mode: "atomic", itemType: "observation", kind: "river-level" })
      .title("Daily river level by gauge").unit("m").geometry("none").retentionClass("series")
      .payloadSchema({ type: "object", properties: { gauge: { type: "string" } } })
      .suggestBinding("series"))
    .build();
}

/** Parses the CSV (no quoted fields) into observations; an empty `level_m` is a null value. */
export function readItems(path: string, observedAt: Date = new Date()): JsonObject[] {
  const [head, ...lines] = readFileSync(path, "utf8").split(/\r?\n/).filter((l) => l.trim() !== "");
  const columns = (head ?? "").split(",");
  return lines.map((line) => {
    const cells = line.split(",");
    const row = Object.fromEntries(columns.map((c, i) => [c, cells[i] ?? ""]));
    return observation(STREAM, { seriesKey: row["gauge"] ?? "", t: row["period"] ?? "",
      value: row["level_m"] ? Number(row["level_m"]) : null, observedAt,
      ...(row["known_at"] ? { knownAt: row["known_at"] } : {}), properties: { gauge: row["gauge"] ?? "" } });
  });
}

/** A pull handler over the CSV; a missing file is reported as a `blocked` status. */
export function makeHandler(path: string, publicUrl = "http://localhost:8099", apiKey?: string,
  status: StatusBoard = new StatusBoard()): PullHandler {
  const load = (): JsonObject[] => {
    try {
      const rows = readItems(path);
      status.ok({ stream: STREAM });
      return rows;
    } catch (e) {
      if ((e as NodeJS.ErrnoException).code !== "ENOENT") throw e;
      status.report("blocked", `${basename(path)} is missing`, { stream: STREAM });
      return [];
    }
  };
  return new PullHandler(new SequenceSource(load), {
    sourceId: SOURCE_ID,
    manifest: buildManifest(publicUrl, apiKey !== undefined),
    status,
    ...(apiKey !== undefined ? { authorize: headerAuth(API_KEY_HEADER, apiKey) } : {}),
  });
}

/** Validates the rows locally, pushes them and prints the outcome; returns a process exit code. */
export async function push(path: string, baseUrl: string, ingestKey: string,
  clientOptions: Partial<PushClientOptions> = {}): Promise<number> {
  const client = new PushClient({ baseUrl, sourceId: registeredSourceId(SOURCE_ID), ingestKey, ...clientOptions });
  let rows: JsonObject[];
  try {
    rows = readItems(path);
  } catch {
    await client.reportStatus("blocked", `${basename(path)} is missing`, { stream: STREAM });
    console.error("CSV missing; reported status blocked");
    return 1;
  }
  const local = validateItems(rows, buildManifest());
  for (const r of local.rejected) console.error(`row ${r.index} invalid locally: ${r.reason}`);
  const result = await client.push(local.valid.map((v) => v.item));
  console.log(`accepted ${result.accepted} of ${rows.length} in ${result.batches} batch(es)`);
  for (const r of result.rejected) console.error(`rejected item ${r.index}: ${r.reason}`);
  return result.ok && local.ok ? 0 : 2;
}

/** Command-line entry point. */
export async function main(argv: string[]): Promise<number> {
  const [command, a, b, c] = argv;
  if (command === "push" && a && b && c) return push(a, b, c);
  if (command === "serve" && a && b) {
    const port = Number(b);
    const handler = makeHandler(a, c ?? `http://localhost:${port}`, process.env["RIVER_GAUGES_API_KEY"]);
    createServer(toNodeListener(handler)).listen(port, () => console.log(`Serving EDP provider on port ${port}`));
    return 0;
  }
  if (command === "manifest") {
    console.log(JSON.stringify(buildManifest(a ?? "http://localhost:8099"), null, 2));
    return 0;
  }
  console.error("usage: river-gauges-provider.ts push CSV BASE_URL INGEST_KEY | serve CSV PORT [PUBLIC_URL] | manifest [PUBLIC_URL]");
  return 64;
}

if (process.argv[1] !== undefined && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main(process.argv.slice(2)).then((code) => { if (code !== 0) process.exitCode = code; },
    (error: unknown) => { console.error(error); process.exitCode = 1; });
}
