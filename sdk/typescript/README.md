# @foresignal/sdk (TypeScript)

The TypeScript SDK for the Foresignal API:

- **Generated:** `src/generated/schema.ts` — openapi-typescript types for every operation in
  [`docs/OpenAPI.json`](../../docs/OpenAPI.json), used by the openapi-fetch client from
  `createForesignalClient`. Regenerate with `./gradlew generateSdks` (or `npm run generate`); never edit it.
- **Hand-written** (on `fetch` only, independent of the generated types), with the same semantics as the Dart
  and Python SDKs:
  - `src/realtime` — `ChangeStream`, an async iterable over `/v1/changes/stream` or `/v1/events/stream`:
    `Last-Event-ID` resume (or stream tickets with `useTicket: true` for clients that cannot set headers),
    snapshot paging on `reset {resync_required}` and `resync`, the `observedAt` guard for `live`/`touch`, a
    heartbeat watchdog (2.5 × the interval) and jittered backoff (1 s → 60 s) honoring `retry:` and
    `Retry-After`;
  - `src/paging` — `paginate`, `paginatePages`, `paginateWith` (async iterables over `nextCursor`) and
    `iterateChanges`, which throws `ResyncRequiredError` on `410 RESYNC_REQUIRED`;
  - `src/contract` — `parseObjectRef`, `recordRef`, `objectRefPathSegment`, the `ObjectEnvelope` type,
    `resolveObjects` (batches of at most 500), `contextProblems`, `applyMergePatch`, `encodeDeepLink`,
    `decodeDeepLink` and `WorkspaceSync`.

## Install

From a local checkout (build first; the package entry point is `dist/`):

```sh
(cd foresignal/sdk/typescript && npm install && npm run build)
npm install ./foresignal/sdk/typescript
```

From git, clone `https://gitlab.com/adam.allard/foresignal.git` and install from the local path as above (npm
cannot install a subdirectory of a git repository directly).

## Use

```ts
import { ChangeStream, WorkspaceSync, createForesignalClient, paginate } from "@foresignal/sdk";

const client = createForesignalClient({ baseUrl: "https://foresignal.example.org", token: "fs_…" });
const { data } = await client.GET("/v1/records", { params: { query: { kind: ["aircraft"], limit: 100 } } });

for await (const alert of paginate({ baseUrl, token }, "/v1/alerts", { status: "firing" })) { … }

const stream = new ChangeStream({ baseUrl, token, filters: { kind: "aircraft", touches: true } });
for await (const signal of stream) {
  if (signal.type === "change" || signal.type === "live" || signal.type === "touch") apply(signal.data);
  else if (signal.type === "reset") clearLocalState();
  else if (signal.type === "snapshot") signal.items.forEach(applyRecord);
}

const sync = new WorkspaceSync({ baseUrl, token, workspaceId: "01JAB…", clientKind: "globe" });
sync.onContext((change) => render(change.context));
await sync.start();
await sync.update((ctx) => ({ focusRef: "situation:01JA8…" }));
```

## Scripts

`npm run typecheck` (`tsc --noEmit`), `npm run build`, `npm test` (vitest; an in-process `node:http` server plays
the SSE server, and the contract tests read [`sdk/contract-fixtures`](../contract-fixtures)).

## Versioning and publishing

The package version is the API's `info.version` (`2.1.1`); tool and dependency versions are pinned in
[`sdk/versions.properties`](../versions.properties). The package is `"private": true`; publishing to npm is a
separate release step.
