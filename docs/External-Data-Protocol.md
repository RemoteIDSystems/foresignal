# Foresignal External Data Protocol (EDP) 1.0

**Status:** design (2026-10-11). Built by prompts **024** (core: manifest, registration, push and pull, validation, layer streams), **138** (bindings for atomic streams, provider kit) and **155** (on-demand analysis-request round-trip, first-class external forecasters). The machine-readable contract is `docs/OpenAPI.json` (schemas `Edp*`, `ExternalSource*`, `ExternalBinding*`; tag `External`); JSON Schema files are exported to `docs/protocol/edp-1.0/` and served at `/v1/external/protocol/schemas/{name}`.

EDP lets anyone write a data source for Foresignal **without changing Foresignal**. A provider is a separate program, written in any language and published anywhere, that describes itself with a **manifest** and delivers **items**. Foresignal does not care where the data comes from: an official API, a scraper, a sensor, a spreadsheet or another analyst's tool. As long as the output matches this contract, Foresignal stores it with full history and uses it like any built-in source.

This is how Foresignal handles fringe and specialist inputs (web maps, scrapers, Telegram channels, prediction markets, niche feeds): they live in their own projects as EDP providers, and the operator decides which ones to connect.

## 1. Concepts

- **Provider:** a program that produces data. It either **pushes** batches to Foresignal or exposes an **items endpoint** that Foresignal **pulls**.
- **External source:** the provider as registered in one Foresignal instance (`ext-{id}`). It is a custom source (024) whose descriptor is built from the manifest, so versioning, history, SSE, search, triggers, retention, licenses and egress all apply.
- **Stream:** one typed flow of items inside a source. Each stream has a **mode**:
  - **`layer`:** geolocated features or events that form a map layer (current state, history, tiles, live updates, geofence rules). Works as soon as the source is approved.
  - **`atomic`:** discrete inputs with no predetermined use (a value, a document, a forecast, an event without a fixed role). They are stored and searchable, and the operator **binds** them to where they should be used: a series, hypothesis evidence, an indicator, a forecast question, a place, a situation or a chronicle (138).
- **Item:** one unit of data in a stream: `feature`, `event`, `observation`, `document`, `forecast`, `delete` or `status`.

## 2. Manifest

The manifest is a JSON document that describes the provider. It is submitted at registration, or published by the provider at a URL (conventionally `/.well-known/foresignal-edp.json`) that the operator gives Foresignal.

```json
{
  "protocol": "foresignal-edp/1.0",
  "source": {
    "id": "hormuz-transits",
    "name": "Strait of Hormuz transits",
    "description": "Daily tanker and cargo transits counted from public dashboards",
    "version": "0.3.1",
    "publisher": {"name": "Example OSINT Lab", "url": "https://example.org", "contact": "ops@example.org"},
    "homepage": "https://github.com/example/hormuz-edp",
    "licence": {"id": "CC-BY-4.0", "url": "https://creativecommons.org/licenses/by/4.0/", "attribution": "Example OSINT Lab", "redistribution": "allowed"},
    "terms": {"summary": "Derived from a public dashboard; upstream terms unclear", "url": null},
    "access": {"route": "scrape", "fragility": "high"},
    "originator": {"id": "example-osint-lab", "class": "aggregator", "aggregates": []},
    "private": false
  },
  "transport": {
    "push": true,
    "pull": {"itemsUrl": "https://hormuz.example.org/edp/items", "intervalSeconds": 3600, "auth": "bearer"},
    "search": null,
    "analysisRequest": null
  },
  "streams": [
    {
      "id": "transits",
      "mode": "atomic",
      "itemType": "observation",
      "kind": "transit-count",
      "title": "Daily transits by vessel class",
      "unit": "vessels/day",
      "update": "incremental",
      "payloadSchema": {"type": "object", "properties": {"vesselClass": {"type": "string"}}},
      "suggestedBindings": [{"target": "series"}]
    },
    {
      "id": "incidents",
      "mode": "layer",
      "itemType": "event",
      "kind": "maritime-incident",
      "title": "Reported incidents",
      "geometry": "Point",
      "update": "incremental",
      "payloadSchema": {"type": "object", "required": ["category"], "properties": {"category": {"type": "string"}, "vessel": {"type": ["string", "null"]}}},
      "fields": {"title": "category", "significant": ["category"], "volatile": []},
      "ttl": null,
      "retentionClass": "event",
      "presentation": {"color": "#d9480f", "icon": "ship", "labelField": "vessel", "minZoom": 3, "tileProperties": ["category"]},
      "coverage": {"area": {"type": "Polygon", "coordinates": [[[54.5, 25.0], [58.0, 25.0], [58.0, 27.5], [54.5, 27.5], [54.5, 25.0]]]}, "family": "maritime-incident", "pDetect": 0.6},
      "suggestedBindings": [{"target": "canonical-family", "config": {"family": "maritime-incident"}}]
    }
  ]
}
```

| Field | Meaning |
|---|---|
| `protocol` | `foresignal-edp/1.N`. Minor versions are additive. An instance accepts any minor up to the highest it supports (`maxMinor` in `GET /v1/external/protocol`); a higher minor is rejected at registration with `PROTOCOL_UNSUPPORTED`, so a provider can never register successfully and then fail at ingestion. |
| `source.id` | slug `^[a-z0-9]+(-[a-z0-9]+)*$`, ≤ 48 chars; the instance registers it as `ext-{id}`. |
| `source.licence.redistribution` | `allowed`, `local-only` or `unknown`; sets the default egress (`local-only` and `unknown` → `LOCAL_ONLY`); the operator can override. |
| `source.access` | how the provider gets its data (`official`, `unofficial-api`, `scrape`, `browser`, `other`) and how likely it is to break; shown to the operator at approval. |
| `source.originator` | who originally observed the data, for evidence independence (Intelligence-Design §4.5). `class`: `sensor-network`, `agency`, `aggregator`, `media`, `crowd`, `social`, `operator`, `derived`. `aggregates` lists originators it re-publishes, so their reports are not counted twice. |
| `source.private` | the data is personal (Foresignal's `private` rules apply). |
| `transport.push` | `true` when the provider pushes to `POST /v1/external/sources/{id}/items`. A manifest must declare at least one delivery mechanism: `push: true` or a `pull` endpoint (there is no default). |
| `transport.pull` | Foresignal polls `itemsUrl` (§4.2) every `intervalSeconds` (≥ 60) with the stated `auth` (`none`, `bearer`, `header` with `header` name, `basic`); the credential is entered by the operator, never put in the manifest. |
| `transport.search` | optional: Foresignal may call `GET {url}?q=&since=&until=&bbox=&lang=&limit=` (same auth styles as pull) and expects a batch; used for searches triggered by physical findings (137 cues). |
| `transport.analysisRequest` | optional: Foresignal may `POST {url}` a request to forecast a specific ledger question now (`{questionId, horizon, correlationId, callbackUrl}`, same auth styles as pull), used for on-demand and backtest forecasts from first-class external forecasters (§4.4, 155). |
| `streams[].id` | slug, unique in the manifest (checked at registration). |
| `streams[].mode` | `layer` or `atomic`. Layer streams carry `feature` or `event` items and must not declare `geometry: none`. |
| `streams[].itemType` | the item type the stream carries (one per stream). |
| `streams[].kind` | the Foresignal record kind (`^[a-z0-9]+(-[a-z0-9]+)*$`). Reusing a built-in kind (`vessel`, `aircraft`, `earthquake`, `air-raid-alert`, `social-post`…) makes the items join that kind's layers, rules and analysis; the payload should then follow the built-in kind's payload. |
| `streams[].payloadSchema` | JSON Schema 2020-12 for the item's `properties` (`feature`, `event`, `document`) or `properties` of an observation. Items that fail it are rejected individually. |
| `streams[].fields` | `title` (field used as the record title), `significant` (changes that create a new version), `volatile` (changes that only touch), as in custom sources (024). |
| `streams[].geometry` | `Point`, `LineString`, `Polygon`, `MultiPolygon`, `any` or `none`. |
| `streams[].update` | `incremental` (items are changes; only `delete` items remove anything) or `snapshot` (the provider periodically sends the full current set of a scope; see §3.2). |
| `streams[].snapshotScope` | for snapshot streams: null when the whole stream is one scope, or the name of the item property that names a scope (a region code, a feed id), so a snapshot of one scope never removes items of another. |
| `streams[].ttl`, `retentionClass` | record TTL (ISO-8601 duration) and the analytic retention class (`event`, `bulk-event`, `series`, `state`, `track`, `reference`, `social`). |
| `streams[].presentation` | layer hints for clients and Foresignal's styles: `color`, `icon`, `labelField`, `minZoom`, `maxZoom`, `tileProperties`. Hints, never required. |
| `streams[].coverage` | optional footprint and detection probability, used by the observability model as a **declared** estimate (low confidence) until Foresignal can measure it. |
| `streams[].unit`, `documentType`, `questionSet` | type-specific details (§3). |
| `streams[].suggestedBindings` | bindings the provider recommends for atomic streams; the operator can accept them in one step. |

## 3. Items

Items travel in a **batch**:

```json
{
  "protocol": "foresignal-edp/1.0",
  "sourceId": "hormuz-transits",
  "sentAt": "2026-10-11T18:00:00Z",
  "snapshot": null,
  "items": [
    {"stream": "transits", "type": "observation", "id": "tanker:2026-10-10", "seriesKey": "tanker", "t": "2026-10-10", "value": 31, "knownAt": "2026-10-11T06:00:00Z", "properties": {"vesselClass": "tanker"}},
    {"stream": "incidents", "type": "event", "id": "inc-8812", "observedAt": "2026-10-11T17:41:00Z", "eventTime": "2026-10-11T16:55:00Z", "geometry": {"type": "Point", "coordinates": [56.4, 26.5]}, "properties": {"category": "approach-by-small-craft", "vessel": "MV Example"}},
    {"stream": "incidents", "type": "delete", "id": "inc-8790"},
    {"stream": "transits", "type": "status", "health": "degraded", "message": "dashboard layout changed; tanker counts missing for 2026-10-11"}
  ],
  "nextCursor": null
}
```

Common item fields: `stream` (required except for source-level `status`), `type` (required), `id` (required except for `status`; stable per stream; Foresignal's record `externalId` is `{stream}:{id}`), `observedAt` (when the provider observed this state; required unless the stream is declared `observedAtMode: received` in its `fields`), `properties` (validated against `payloadSchema`), `origin` (optional item-level provenance, §3.4).

Batch fields: `protocol` (`foresignal-edp/1.N`, not above the manifest's minor), `sourceId` (optional; the push URL and key, or the pull registration, identify the source, and a supplied value must match it, else `400 SOURCE_MISMATCH`), `sentAt`, `items`, `nextCursor` (pull), `snapshot` (§3.2).

| Type | Extra fields | Becomes |
|---|---|---|
| `feature` | `geometry` (§3.3), `eventTime?`, `validUntil?` | a record (current state of an entity: a vessel, a camera, a checkpoint) |
| `event` | `eventTime`, `geometry?`, `upstreamEventId?` | a record of a discrete occurrence; `upstreamEventId` lets evidence matching recognize the same event from other sources |
| `observation` | `seriesKey`, `t` (one of `YYYY`, `YYYY-Qn`, `YYYY-MM`, `YYYY-Www` (ISO week), `YYYY-MM-DD`, or an RFC 3339 timestamp with offset; periods are UTC intervals `[start, end)`, and one series keeps one granularity), `value` (number or null), `knownAt?` (when the value became knowable; defaults to `observedAt`; see §3.5), `unit?` | a series point of series `{kind}/{seriesKey}` |
| `document` | `documentType` (`social-post`, `article`, `report`, `transcript`, `note`), `text` (≤ 20,000 chars), `title?`, `url?`, `author?` (`{handle, name, accountId}`), `language?`, `publishedAt?`, `about?` (Foresignal object references or URLs), `media?` (`[{type, url, thumbUrl?}]`, links only), `geometry?`, `engagement?` | a document record; social posts and articles can be bound into the social (135–137) and news (133) pipelines |
| `forecast` | `question` (`{externalId, title, closeTime?, resolutionCriteria?}`) or `questionRef` (a Foresignal question id) — one is required; `probability` (0–1) or `quantiles` (`p05`, `p25`, `p50`, `p75`, `p95` required, `p10`, `p90` optional; non-decreasing) — at least one is required; `madeAt`. A provider that emits only deciles (`p10`…`p90`, as TimesFM does) is accepted: the binding maps them onto the required quantiles by monotone piecewise-linear interpolation (§5), so the ledger's required set is never loosened | an external forecast; bound to ledger questions it is scored like any forecaster (122) |
| `delete` | — (`observedAt` and `properties` optional, kept for audit) | a tombstone for `{stream}:{id}` |
| `status` | `health` (`ok`, `degraded`, `blocked`, `layout-changed`, `geo-restricted`), `message`; without `stream` it is the source's health, with `stream` that stream's | health in Foresignal (no record) |

Rules:

- Items with the same `{stream, id}` and identical content are idempotent (Foresignal's hashing turns repeats into touches); a changed item is a new version.
- **Forward compatibility:** unknown fields are ignored (not stored) at every level, so a provider written for a later `1.x` degrades gracefully. Item `properties` are governed by the stream's `payloadSchema`, which decides whether extra keys are allowed.
- Text, titles and URLs are untrusted data. Foresignal never executes them and never gives them to an LLM outside an untrusted-data block (§7).
- Limits per batch: 5 MB, 10,000 items, JSON nesting depth 32, `text` ≤ 20,000 chars and other strings ≤ 2,048; per item ≤ 20 `media` entries and ≤ 10,000 geometry vertices; per source: the operator's request and items-per-minute caps.

### 3.1 Validation order

Registration checks the manifest schema, unique stream ids, layer streams' item types, each `payloadSchema` (§7) and the protocol minor. Ingestion checks the batch, then each item: `type` matches the stream's `itemType`, the item schema, `properties` against `payloadSchema`, geometry (§3.3), period format, forecast values (non-decreasing quantiles). Invalid items are rejected individually with their errors; valid items in the same batch are accepted.

### 3.2 Snapshots and deletions

Only two things remove an entity: an explicit `delete` item, or its **absence from a completed snapshot of its scope**. Nothing else (a short page, a failed pull, a stale or unhealthy provider, partial geographic coverage) ever deletes.

- A snapshot is sent as one or more batches carrying `snapshot: {id, stream, scope, page, final}`; pages share the `id`, `page` counts from 1, and the last has `final: true`. Pull providers return the same object on each page; the snapshot ends with the page whose `nextCursor` is null.
- Foresignal stages the ids it receives and applies absence tombstones (reason `ABSENT_FROM_SNAPSHOT`) only when: the final page arrived; every page 1..n arrived and validated (no whole-page rejection); the snapshot completed within `snapshotWindow` (default 30 min) of its first page; the provider reported no `status` other than `ok` for that stream meanwhile; and the absent share is at most `maxAbsentRatio` (default 0.5; above it the deletion is held, the source turns `degraded`, and the operator can allow it once). Otherwise the snapshot is discarded and nothing is removed.
- Scope: with `snapshotScope` null the snapshot covers the whole stream; otherwise only items whose `snapshotScope` property equals `snapshot.scope`.
- An absence tombstone ends an entity's state; it is never evidence that something did not happen. Absence conclusions in the intelligence layer still require detectability (Intelligence-Design §4.6).

### 3.3 Geometry

GeoJSON geometries (RFC 7946): `Point`, `MultiPoint`, `LineString`, `MultiLineString`, `Polygon`, `MultiPolygon` (no `GeometryCollection`), WGS84 longitude −180…180 and latitude −90…90, two or three coordinates (the third is altitude in meters), closed linear rings with at least four positions, and a type compatible with the stream's declared `geometry`. Invalid geometry rejects the item.

### 3.4 Origin and independence

The **provider** (the EDP source that delivered an item) is never evidence of independence. Independence is counted by **originator**: the item's `origin.originator` when given, else the manifest's `source.originator`; `aggregates` and `origin.derivedFrom` mark re-publication, and `origin.upstreamId` (or `upstreamEventId` on events) lets Foresignal recognize the same underlying observation arriving through several providers or built-in sources, so it forms one evidence group (Intelligence-Design §4.5). Five providers relaying one report count once; one provider relaying five independent sensors counts five times only if it labels each item's origin.

### 3.5 Time

Provider-supplied times (`observedAt`, `eventTime`, `knownAt`, `publishedAt`, `madeAt`) are stored as claims. Foresignal's own receipt (ingest sequence and receive time) is the transaction-time boundary: as-of replays, look-ahead guards and the ledger use `max(provider time, receipt time)` for availability (`knownAt`, `publishedAt`, `madeAt`), so a provider can never make information appear available before Foresignal had it. `observedAt` more than 5 minutes in the future is rejected (clock-skew rules of the core apply).

## 4. Transport

### 4.1 Push

`POST /v1/external/sources/{id}/items` with the batch as `application/json`, or as `application/x-ndjson` (one item per line; the first line may be a batch header without `items`). Authenticate with the source's ingest key (`X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…` or Basic), the same as custom sources (024). The response reports accepted items and per-item rejections with schema errors. While the source is awaiting approval, pushes get `409 SOURCE_PENDING_APPROVAL`.

### 4.2 Pull

The provider serves `GET {itemsUrl}?cursor={cursor}&limit={n}` returning a batch with `nextCursor` (null when caught up). Foresignal stores the cursor, retries with backoff, counts requests against the source's budget and reports provider errors as health. Snapshot streams follow §3.2.

### 4.3 Search (optional)

Providers that can search on demand declare `transport.search`. Foresignal calls `GET {url}?q={query}&since=&until=&bbox=&lang=&limit=` when a physical finding triggers a cue (137), within the cue's budget, and stores the returned items like any other (marked as cue results, so they never feed volume baselines).

### 4.4 Analysis request (optional, on-demand, 155)

A continuous forecaster pushes or is pulled like any stream, and a pushed `forecast` item is scored with no extra transport. A **first-class external forecaster** (a model like TimesFM that answers a specific question on demand) additionally declares `transport.analysisRequest`: an endpoint Foresignal calls to ask *forecast this question now*.

- Foresignal `POST {url}`s `{questionId, horizon, correlationId, callbackUrl}` (`asOf` instead of `correlationId`/`callbackUrl` for a backtest, §below), with the auth style the manifest declares, within the source's budget and under the same URL vetting as pull (§7).
- The provider answers either way, or both:
  - **Synchronously** — the forecast (`probability` and/or `quantiles`, deciles accepted) in the HTTP response body. Used for backtests and recorded at once.
  - **Asynchronously** — a later `POST {callbackUrl}` (`/v1/external/analysis/callback`) carrying the `correlationId` and the forecast. Foresignal resolves the correlation to its question and external forecaster and records the forecast through the usual binding path.
- The returned forecast is recorded as the external forecaster `edp:{sourceId}:{streamId}`, scored like any forecaster (122). Decile output is mapped to the ledger's required quantiles (§5).
- **Backtesting:** because the request can be answered synchronously, Foresignal can backtest an external forecaster by issuing an **as-of** analysis request per question in an evaluation window (`createForecastEvaluation`); a provider that answers only asynchronously cannot be backtested and those questions are skipped.
- **First-class config:** an operator can pre-register, name and configure an external forecaster (display name; a per-forecaster `externalMinResolved`) independent of the auto-derived `edp:{…}` id (`POST /v1/forecasts/forecasters`, `PUT /v1/forecasts/forecasters/{id}`).

The question, horizon and forecaster are correlated by the `correlationId` Foresignal persists before it calls out, so an asynchronous answer always lands on the right question. TimesFM and every other model stay external services; Foresignal adds only the transport and the correlation.

### 4.5 Registration

Three ways:

1. **Operator registers** (`POST /v1/external/sources`, admin) with the manifest or its URL. Foresignal validates it, shows the access block, license and streams, and creates the source `pending` (or `active` with `approve: true`). The response carries the ingest key once.
2. **Provider self-registers** with a one-time **registration token** (`fsx_…`) the operator created (`POST /v1/external/registration-tokens`): `POST /v1/external/register` with the manifest. The source is created `pending`; the operator approves it.
3. **Manifest updates:** the provider (with its ingest key) or the operator can `PUT /v1/external/sources/{id}/manifest`. Additive changes (new optional fields, presentation, descriptions) apply at once; changes to streams, kinds or schemas return the source to `pending` until approved.

`POST /v1/external/validate` checks a manifest and sample items without storing anything, for provider developers and CI.

## 5. Using atomic streams (bindings, prompt 138)

A binding connects an atomic stream to a destination:

| Target | Effect |
|---|---|
| `series` | observations become analysis series (`series:ext-{id}/{kind}/{seriesKey}`) for change points, event studies, precursor tests, outlooks and charts |
| `canonical-family` | events join a canonical-event family, so they corroborate (or fail to corroborate) other sources' events, with the manifest's originator counted once |
| `social` | `social-post` documents join the social pipeline (narratives, claims, comparisons) |
| `news` | `article` documents join news stories and coverage analysis |
| `evidence` | matching items become graded evidence items on a hypothesis set |
| `indicator` | a condition on items marks a hypothesis indicator `observed` (physical data) or `reported` (documents) |
| `forecast` | forecast items become external forecasts on linked ledger questions (question links map the provider's question ids to Foresignal questions); a forecaster that declares `transport.analysisRequest` can also be asked on demand and backtested (§4.4) |
| `place`, `situation`, `chronicle` | attach the stream to a place, a situation, or a chronicle layer |

Every stream, layer or atomic, is also a record kind, so trigger rules, search, briefings and Ask can use it without a binding.

**Decile mapping.** A forecast carrying only deciles (`p10`, `p20`…`p90`) — the native output of models like TimesFM — is mapped to the ledger's required quantiles (`p05`, `p25`, `p50`, `p75`, `p95`) binding-side: each required level is read from the provided points by monotone piecewise-linear interpolation (and clamped to stay non-decreasing), extrapolating the tails from the two nearest points. A forecast that already carries every required level is passed through unchanged. This keeps the ledger's required set fixed while accepting a decile provider.

## 6. Writing a provider

- Start from the JSON Schemas and the conformance fixtures (`src/test/resources/edp/conformance/`), and validate with `POST /v1/external/validate` or `foresignal edp validate` (CLI, prompt 141).
- Provider libraries for Python and TypeScript (prompt 141) handle batching, NDJSON, cursors, retries and the pull endpoint.
- Keep each provider in its own repository. Foresignal's catalog lists provider ideas (`docs/Source-Catalog.md` "External provider ideas") but ships none of them.
- Report breakage with `status` items rather than sending empty data.

## 7. Security and trust

Providers are an untrusted boundary.

- **Provider URLs** (`manifestUrl`, `pull.itemsUrl`, `search.url`, `analysisRequest.url`, media): HTTPS by default; DNS is resolved once per request and the connection pinned to the vetted address (no DNS rebinding); loopback, link-local, private, carrier-grade NAT and cloud-metadata ranges are refused unless the operator allows private networks **for that source** (a LAN provider on a homelab is normal); redirects are followed only to the same host (≤ 3, each re-vetted); connect and read timeouts, response size caps (5 MB) and the same budgets as other sources apply.
- **Payload schemas:** ≤ 64 KB each, local `$ref`s only (no remote or `$dynamicRef` resolution), `pattern` and `patternProperties` ≤ 256 characters evaluated with a linear-time regex engine, and a validation time budget per batch (2 s); a schema that breaks these rules fails registration.
- **Media:** links only; Foresignal fetches media solely through its image guard (size, type and address checks) when a binding needs fingerprints.
- **Private and licensed data:** `source.private` and the license's redistribution setting travel with every record and every derived product (embeddings, caches, tiles, findings, situations, exports, reports, MCP output, notifications) under Foresignal's egress and TLP rules; a derived product inherits the strictest input, so summarizing private data never makes it public.
