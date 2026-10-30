# Foresignal client SDKs

Client libraries for the Foresignal HTTP API (prompt 141), generated from the authored contract
[`docs/OpenAPI.json`](../docs/OpenAPI.json) plus a small hand-written layer that every SDK implements with the
same semantics:

| SDK | Generated client | Hand-written layer | Tests |
|---|---|---|---|
| Dart | [`dart/foresignal_api`](dart/foresignal_api) (openapi-generator `dart-dio`, `json_serializable`) | [`dart/foresignal_sdk`](dart/foresignal_sdk) (on `dio`) | `dart test` |
| TypeScript | [`typescript/src/generated/schema.ts`](typescript/src/generated) (openapi-typescript, used by openapi-fetch) | [`typescript/src`](typescript/src) `realtime/`, `paging/`, `contract/` (on `fetch`) | `vitest` |
| Python | [`python/foresignal_client`](python/foresignal_client) (openapi-python-client) | [`python/foresignal_sdk`](python/foresignal_sdk) (on `httpx`) | `pytest` |

The EDP provider libraries (`python-edp`, `typescript-edp`) are separate packages with their own READMEs.

## The hand-written layer

It depends only on the HTTP client (dio, `fetch`, httpx), never on the generated types, so it keeps working
when the generated code is regenerated or not used at all.

- **`ChangeStream`** reads `GET /v1/changes/stream` or `GET /v1/events/stream` (docs/Core-Data-Model.md §10.2):
  - resumes with `Last-Event-ID`, or with `after=` / `lastEventId=` and a stream ticket from
    `POST /v1/stream-tickets` when the client cannot send headers (`useTicket`); a ticket refused with 401 is
    minted again once;
  - tracks the last `change` id (the composite cursor on `/v1/events/stream`);
  - on `reset` it emits the reset; for `resync_required` it pages the snapshot (`/v1/records?order=id`, keeping
    `watermarkSeq`) and reopens at the watermark; for `buffer_overflow` and `replay_cap` it reopens at
    `resumeAfter` (or the last id);
  - on `resync` it pages the current state again and keeps the stream open;
  - applies `live` and `touch` events only when their `observedAt` is not older than the stored value for the
    entity;
  - reconnects when nothing (event or heartbeat) arrives within 2.5 × the heartbeat interval (default 15 s,
    the server's `stream.limits.heartbeatSeconds`), with jittered exponential backoff from 1 s to 60 s that
    never goes below the server's `retry:` field or `Retry-After` header.
- **Cursor iterators** over every paged operation (`cursor` in, `nextCursor` out; async iterables in
  TypeScript, `Stream`s in Dart, generators in Python), and a seq iterator over `GET /v1/changes` whose
  `410 RESYNC_REQUIRED` surfaces as a typed `ResyncRequired` error carrying `purgeHorizon` and `watermarkSeq`.
- **Client contract** (docs/Client-Contract.md): `ObjectRef` parse and format with the server's encoding rules,
  the object envelope type, `resolve(refs)` in batches of at most 500, workspace context validation and JSON
  merge patches, deep link encode and decode (`#fs=` payloads, version check, 2 KB limit), and `WorkspaceSync`
  (follows the `workspace` topic, applies remote patches in version order, re-reads on a gap or reconnect,
  sends local patches with `If-Match` and retries a `412` by re-reading and re-applying the intent, ignores
  events carrying its own `originClientId`, exposes presence).

The three SDKs run the same contract fixtures, [`contract-fixtures/`](contract-fixtures): `refs.json` (valid and
invalid references, record parts, URL path segments) and `contexts.json` (contexts and their exact deep link
payloads, invalid payloads, merge patches).

## Generating

```sh
./gradlew generateSdks          # all three generated clients (not part of build or check)
./gradlew checkSdkGeneration    # preprocess the spec for Dart and dry-run dart-dio (optional smoke test)
```

`generateSdks` overwrites only the generated directories: `dart/foresignal_api` (except the files listed in its
`.openapi-generator-ignore`), `typescript/src/generated/schema.ts` and `python/foresignal_client`. It needs:

- openapi-generator: a Java runtime (11 or later; the pinned jar is downloaded from Maven Central into
  `build/sdk/tools` and checked against its SHA-256) or Docker;
- the Dart SDK (`dart pub get` and `dart run build_runner build` write the `json_serializable` parts);
- Node.js 20.19 or later with `npx` (openapi-typescript);
- `pipx` or `uv` with Python 3.11 or later (openapi-python-client).

A missing tool fails the task with install instructions.

### Dart and OpenAPI 3.1

dart-dio handles OpenAPI 3.1 nullable unions badly, so the Dart client is generated from an OpenAPI 3.0.3 copy
written by [`scripts/sdk/preprocess-dart.py`](../scripts/sdk/preprocess-dart.py) into `build/sdk/openapi-dart.json`:
`type: [T, "null"]` becomes `type: T` with `nullable: true`, `oneOf: [{$ref}, {type: "null"}]` becomes
`allOf: [{$ref}]` with `nullable: true`, `const` becomes a one-value `enum`, `examples` becomes `example`, and
`webhooks`, `$schema` and `jsonSchemaDialect` are dropped; `x-foresignal-*` extensions are kept. A few constructs
the generator turns into code that does not compile (unions of primitives, constraint-only `anyOf`, enum values
with an apostrophe, defaults on enums and lists) are moved to `x-foresignal-oas31-*` extensions; the script's
docstring lists them. Its tests are in `scripts/sdk/tests` (`python3 -m pytest scripts/sdk/tests`, with
openapi-spec-validator installed for the whole-spec check).

## Versions

Every tool and runtime dependency is pinned in [`versions.properties`](versions.properties), which the Gradle
tasks read; the SDK manifests (`pubspec.yaml`, `package.json`, `pyproject.toml`) use the same versions. Each SDK is
versioned with the API's `info.version` (currently `2.1.1`): regenerate after a spec change and bump the
manifests to the new `info.version`.

## Publishing

Not done here. The packages are marked unpublishable (`publish_to: none`, `"private": true`) and are installed
from a local path or from git (each README shows how). Publishing to pub.dev, npm or PyPI is a separate release
step.
