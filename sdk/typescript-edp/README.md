# @foresignal/edp (TypeScript)

Provider library for the Foresignal **External Data Protocol (EDP) 1.0**
([docs/External-Data-Protocol.md](../../docs/External-Data-Protocol.md)). It is independent of the
Foresignal API client, so a provider project stays small: the only runtime dependency is `ajv`.
ES modules, Node 20 or newer (it uses `node:crypto` for cursor signing and snapshot ids, so Deno and
Bun work too).

| Export | Purpose |
|---|---|
| `ManifestBuilder`, `StreamBuilder` | Write a manifest, including `coverage`, `complete`, `suggestedBindings`, snapshot streams and field roles. The wire keys keep the protocol's spelling (`source.licence`, set with `.license({...})`). |
| `validateManifest` | The registration checks: the exported manifest schema (Ajv, JSON Schema 2020-12), protocol minor, delivery mechanism, unique stream ids, layer item types and geometry, slugs, pull and search URLs, field roles, presentation hints, TTLs and every `payloadSchema` (64 KB, local `$ref`s only, patterns of at most 256 characters a linear-time engine accepts). |
| `feature`, `event`, `observation`, `document`, `forecast`, `deleteItem` (also exported as `delete`), `status` | Typed item builders, plus `point`/`polygon`/... geometry helpers, `origin`, `question`, `quantiles`, `author`, `media`, `snapshot` and `batch`. |
| `validateItems`, `validateBatch` | Per-item validation in the server's order, with its rejection codes (`SCHEMA_INVALID`, `UNKNOWN_STREAM`, `PAYLOAD_INVALID`, `GEOMETRY_INVALID`, `PERIOD_INVALID`, `FORECAST_INVALID`, ...), and the batch-level refusals. |
| `PushClient` | `POST /v1/external/sources/{sourceId}/items` with `X-Foresignal-Ingest-Key`: batches of at most 10,000 items and 5 MB, optional NDJSON, retries with backoff on 429 and 503 honoring `Retry-After`, idempotent re-sends of byte-identical batches, snapshot paging, status reports, manifest updates. |
| `PullHandler` | A framework-neutral handler for `GET items?cursor=&limit=` and optional `search`, with a pluggable `CursorStore`, `SequenceSource` (offset paging) and `SnapshotSource` (snapshot paging), auth checks and a 5 MB page cap; `toNodeListener` (node:http, Express) and `toFetchHandler` (`Request` to `Response`). |
| `StatusBoard` | Health for pull providers; changes ride on the next page served. |

## Install

From a checkout of this repository (local path):

```sh
npm install ./sdk/typescript-edp     # then build once: (cd sdk/typescript-edp && npm install && npm run build)
```

From git, point a dependency at the repository and build the package directory (`npm run build`
emits `dist/`). Publishing to a registry is a separate release step and is not done here.

## Versioning

The major and minor version follow the EDP protocol version: `1.0.x` speaks `foresignal-edp/1.0`.
The patch number counts library releases for that protocol version. A later `1.N` protocol (always
additive) gets a `1.N.0` library; `validateManifest`'s `maxMinor` defaults to the minor it knows.

## Quick start

```ts
import { ManifestBuilder, StreamBuilder, PushClient, observation, validateItems } from "@foresignal/edp";

const manifest = new ManifestBuilder("river-gauges", "River gauges")
  .publisher("Example Hydro")
  .originator("example-hydro", "sensor-network")
  .access("unofficial-api", "medium")
  .license({ redistribution: "local-only" })
  .push()
  .stream(new StreamBuilder("levels", { mode: "atomic", itemType: "observation", kind: "river-level" })
    .geometry("none").unit("m").suggestBinding("series"))
  .build();                                   // throws ManifestInvalidError with every problem

const items = [observation("levels", { seriesKey: "kaub", t: "2026-10-15", value: 1.42, observedAt: "2026-10-15T06:00:00Z" })];
validateItems(items, manifest).ok;            // the same checks Foresignal runs

const client = new PushClient({ baseUrl: "https://foresignal.example", sourceId: "ext-river-gauges", ingestKey: "eik_..." });
const result = await client.push(items);      // { accepted, rejected, batches, responses, ok }
await client.reportStatus("ok", undefined, { stream: "levels" });
```

A failed push throws `PushError` with `status`, `code` (the ProblemDetail code), `retryable`,
`result` (what was accepted) and `remaining` (the prepared batches not accepted, the failed one
first). `client.sendAll(error.remaining)` re-sends them byte for byte later; Foresignal turns
repeated items into touches, so re-sending is always safe.

Snapshots (EDP §3.2): `client.pushSnapshot("cameras", allCurrentItems, { scope: "north" })` pages the
full current set with one snapshot id and `final: true` on the last page. Nothing is deleted unless
every page arrives.

### Serving a pull endpoint

```ts
import { createServer } from "node:http";
import { PullHandler, SequenceSource, OpaqueCursorStore, headerAuth, toNodeListener } from "@foresignal/edp";

const handler = new PullHandler(new SequenceSource(() => loadItemsInAppendOrder()), {
  cursorStore: new OpaqueCursorStore("change-me"),        // stateless, signed, survives restarts
  authorize: headerAuth("X-Api-Key", "the-key-the-operator-enters"),
  manifest,                                               // served at /.well-known/foresignal-edp.json
});
createServer(toNodeListener(handler)).listen(8099);
// or: export default { fetch: toFetchHandler(handler) }  // Deno, Bun, workers
```

Foresignal follows `nextCursor` until it is `null` and starts its next run from the last cursor it
sent, so cursors must stay valid and replayable. `SequenceSource` does this by offset;
`new SnapshotSource(stream, listItems, { scope })` serves full snapshots with keyset paging and
starts a fresh snapshot after a finished one; any object with `fetch(position, limit)` works.
`MemoryCursorStore` keeps short random tokens but forgets them on restart; implement
`encode`/`decode` against your database for a durable store.

## Example provider

[`examples/river-gauges-provider.ts`](examples/river-gauges-provider.ts) serves
[`examples/river-levels.csv`](examples/river-levels.csv) by push and pull, with an optional API key
and `blocked` status when the CSV is missing. Node 22.18+ and 24 run it directly:

```sh
npm install
node examples/river-gauges-provider.ts manifest https://hydro.example.org
node examples/river-gauges-provider.ts push examples/river-levels.csv https://foresignal.example eik_YOUR_KEY
RIVER_GAUGES_API_KEY=secret node examples/river-gauges-provider.ts serve examples/river-levels.csv 8099
```

## Schemas and conformance fixtures

- `src/generated/schemas.ts` embeds the exported schemas of `docs/protocol/edp-1.0/` (generated from
  `docs/OpenAPI.json`) as a module, so no JSON imports are needed in any runtime or bundler. Refresh it
  with `npm run sync-schemas`; `test/schemas-sync.test.ts` fails while it differs.
- The tests run the prompt-024 conformance fixtures **by relative path** from
  `../../src/test/resources/edp/conformance` (the server's own copy, so they cannot drift). Outside
  the repository, set `FORESIGNAL_EDP_CONFORMANCE_DIR`.
- `format` keywords are annotations, as on the server; times are checked by the item validator.
  Patterns are compiled as Ajv does after refusing the constructs RE2 rejects; the server (RE2)
  remains the final judge.

## Development

```sh
npm install
npm run typecheck     # tsc --noEmit
npm test              # vitest run
npm run build         # emits dist/
```
