# Foresignal External Data Protocol (EDP) 1.0

**Status:** design (2026-10-11). Built by prompts **024** (core: manifest, registration, push and pull, validation, layer streams) and **137** (bindings for atomic streams, provider kit). The machine-readable contract is `docs/OpenAPI.json` (schemas `Edp*`, `ExternalSource*`, `ExternalBinding*`; tag `External`); JSON Schema files are exported to `docs/protocol/edp-1.0/` and served at `/v1/external/protocol/schemas/{name}`.

EDP lets anyone write a data source for Foresignal **without changing Foresignal**. A provider is a separate program, written in any language and published anywhere, that describes itself with a **manifest** and delivers **items**. Foresignal does not care where the data comes from: an official API, a scraper, a sensor, a spreadsheet or another analyst's tool. As long as the output matches this contract, Foresignal stores it with full history and uses it like any built-in source.

This is how Foresignal handles fringe and specialist inputs (web maps, scrapers, Telegram channels, prediction markets, niche feeds): they live in their own projects as EDP providers, and the operator decides which ones to connect.

## 1. Concepts

- **Provider:** a program that produces data. It either **pushes** batches to Foresignal or exposes an **items endpoint** that Foresignal **pulls**.
- **External source:** the provider as registered in one Foresignal instance (`ext-{id}`). It is a custom source (024) whose descriptor is built from the manifest, so versioning, history, SSE, search, triggers, retention, licences and egress all apply.
- **Stream:** one typed flow of items inside a source. Each stream has a **mode**:
  - **`layer`:** geolocated features or events that form a map layer (current state, history, tiles, live updates, geofence rules). Works as soon as the source is approved.
  - **`atomic`:** discrete inputs with no predetermined use (a value, a document, a forecast, an event without a fixed role). They are stored and searchable, and the operator **binds** them to where they should be used: a series, hypothesis evidence, an indicator, a forecast question, a place, a situation or a chronicle (137).
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
    "search": null
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
| `protocol` | `foresignal-edp/1.0`. Minor versions are additive; Foresignal accepts any `1.x` and ignores unknown fields. |
| `source.id` | slug `^[a-z0-9]+(-[a-z0-9]+)*$`, ≤ 48 chars; the instance registers it as `ext-{id}`. |
| `source.licence.redistribution` | `allowed`, `local-only` or `unknown`; sets the default egress (`local-only` and `unknown` → `LOCAL_ONLY`); the operator can override. |
| `source.access` | how the provider gets its data (`official`, `unofficial-api`, `scrape`, `browser`, `other`) and how likely it is to break; shown to the operator at approval. |
| `source.originator` | who originally observed the data, for evidence independence (Intelligence-Design §4.5). `class`: `sensor-network`, `agency`, `aggregator`, `media`, `crowd`, `social`, `operator`, `derived`. `aggregates` lists originators it re-publishes, so their reports are not counted twice. |
| `source.private` | the data is personal (Foresignal's `private` rules apply). |
| `transport.push` | the provider will push to `POST /v1/external/sources/{id}/items`. |
| `transport.pull` | Foresignal polls `itemsUrl` (§4.2) every `intervalSeconds` (≥ 60) with the stated `auth` (`none`, `bearer`, `header` with `header` name, `basic`); the credential is entered by the operator, never put in the manifest. |
| `transport.search` | optional: Foresignal may call `GET {url}?q=&since=&until=&bbox=&lang=&limit=` (same auth styles as pull) and expects a batch; used for searches triggered by physical findings (136 cues). |
| `streams[].id` | slug, unique in the manifest. |
| `streams[].mode` | `layer` or `atomic`. |
| `streams[].itemType` | the item type the stream carries (one per stream). |
| `streams[].kind` | the Foresignal record kind (`^[a-z0-9]+(-[a-z0-9]+)*$`). Reusing a built-in kind (`vessel`, `aircraft`, `earthquake`, `air-raid-alert`, `social-post`…) makes the items join that kind's layers, rules and analysis; the payload should then follow the built-in kind's payload. |
| `streams[].payloadSchema` | JSON Schema 2020-12 for the item's `properties` (`feature`, `event`, `document`) or `properties` of an observation. Items that fail it are rejected individually. |
| `streams[].fields` | `title` (field used as the record title), `significant` (changes that create a new version), `volatile` (changes that only touch), as in custom sources (024). |
| `streams[].geometry` | `Point`, `LineString`, `Polygon`, `MultiPolygon`, `any` or `none`. |
| `streams[].update` | `incremental` (items are changes) or `snapshot` (each pull or push batch with `"complete": true` is the full current set; absent ids are tombstoned). |
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
  "complete": false,
  "items": [
    {"stream": "transits", "type": "observation", "id": "tanker:2026-10-10", "seriesKey": "tanker", "t": "2026-10-10", "value": 31, "knownAt": "2026-10-11T06:00:00Z", "properties": {"vesselClass": "tanker"}},
    {"stream": "incidents", "type": "event", "id": "inc-8812", "observedAt": "2026-10-11T17:41:00Z", "eventTime": "2026-10-11T16:55:00Z", "geometry": {"type": "Point", "coordinates": [56.4, 26.5]}, "properties": {"category": "approach-by-small-craft", "vessel": "MV Example"}},
    {"stream": "incidents", "type": "delete", "id": "inc-8790"},
    {"stream": "transits", "type": "status", "health": "degraded", "message": "dashboard layout changed; tanker counts missing for 2026-10-11"}
  ],
  "nextCursor": null
}
```

Common item fields: `stream` (required), `type` (required), `id` (required except for `status`; stable per stream; Foresignal's record `externalId` is `{stream}:{id}`), `observedAt` (when the provider observed this state; required unless the stream is declared `observedAtMode: received` in its `fields`), `properties` (validated against `payloadSchema`).

| Type | Extra fields | Becomes |
|---|---|---|
| `feature` | `geometry`, `eventTime?`, `validUntil?` | a record (current state of an entity: a vessel, a camera, a checkpoint) |
| `event` | `eventTime`, `geometry?`, `upstreamEventId?` | a record of a discrete occurrence; `upstreamEventId` lets evidence matching recognise the same event from other sources |
| `observation` | `seriesKey`, `t` (time or period: `2026-10-10`, `2026-W41`, `2026-10`, or a timestamp), `value` (number or null), `knownAt?` (when the value became knowable; defaults to `observedAt`), `unit?` | a series point of series `{kind}/{seriesKey}` |
| `document` | `documentType` (`social-post`, `article`, `report`, `transcript`, `note`), `text` (≤ 20,000 chars), `title?`, `url?`, `author?` (`{handle, name, accountId}`), `language?`, `publishedAt?`, `about?` (Foresignal object references or URLs), `media?` (`[{type, url, thumbUrl?}]`, links only), `geometry?`, `engagement?` | a document record; social posts and articles can be bound into the social (134–136) and news (132) pipelines |
| `forecast` | `question` (`{externalId, title, closeTime?, resolutionCriteria?}`) or `questionRef` (a Foresignal question id), and `probability` (0–1) or `quantiles`, `madeAt` | an external forecast; bound to ledger questions it is scored like any forecaster (122) |
| `delete` | — | a tombstone for `{stream}:{id}` |
| `status` | `health` (`ok`, `degraded`, `blocked`, `layout-changed`, `geo-restricted`), `message` | the source's health in Foresignal (no record) |

Rules:

- Items with the same `{stream, id}` and identical content are idempotent (Foresignal's hashing turns repeats into touches); a changed item is a new version.
- Text, titles and URLs are untrusted data. Foresignal never executes them, never follows `media` URLs except through its image guard when a binding asks for fingerprints, and never gives them to an LLM outside an untrusted-data block.
- Limits per batch: 5 MB, 10,000 items; per source: the operator's request and items-per-minute caps.

## 4. Transport

### 4.1 Push

`POST /v1/external/sources/{id}/items` with the batch as `application/json`, or as `application/x-ndjson` (one item per line; the first line may be a batch header without `items`). Authenticate with the source's ingest key (`X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…` or Basic), the same as custom sources (024). The response reports accepted items and per-item rejections with schema errors. While the source is awaiting approval, pushes get `409 SOURCE_PENDING_APPROVAL`.

### 4.2 Pull

The provider serves `GET {itemsUrl}?cursor={cursor}&limit={n}` returning a batch with `nextCursor` (null when caught up). Foresignal stores the cursor, retries with backoff, counts requests against the source's budget and reports provider errors as health. For `snapshot` streams the provider returns `"complete": true` on the last page of a full set.

### 4.3 Search (optional)

Providers that can search on demand declare `transport.search`. Foresignal calls `GET {url}?q={query}&since=&until=&bbox=&lang=&limit=` when a physical finding triggers a cue (136), within the cue's budget, and stores the returned items like any other (marked as cue results, so they never feed volume baselines).

### 4.4 Registration

Three ways:

1. **Operator registers** (`POST /v1/external/sources`, admin) with the manifest or its URL. Foresignal validates it, shows the access block, licence and streams, and creates the source `pending` (or `active` with `approve: true`). The response carries the ingest key once.
2. **Provider self-registers** with a one-time **registration token** (`fsx_…`) the operator created (`POST /v1/external/registration-tokens`): `POST /v1/external/register` with the manifest. The source is created `pending`; the operator approves it.
3. **Manifest updates:** the provider (with its ingest key) or the operator can `PUT /v1/external/sources/{id}/manifest`. Additive changes (new optional fields, presentation, descriptions) apply at once; changes to streams, kinds or schemas return the source to `pending` until approved.

`POST /v1/external/validate` checks a manifest and sample items without storing anything, for provider developers and CI.

## 5. Using atomic streams (bindings, prompt 137)

A binding connects an atomic stream to a destination:

| Target | Effect |
|---|---|
| `series` | observations become analysis series (`series:ext-{id}/{kind}/{seriesKey}`) for change points, event studies, precursor tests, outlooks and charts |
| `canonical-family` | events join a canonical-event family, so they corroborate (or fail to corroborate) other sources' events, with the manifest's originator counted once |
| `social` | `social-post` documents join the social pipeline (narratives, claims, comparisons) |
| `news` | `article` documents join news stories and coverage analysis |
| `evidence` | matching items become graded evidence items on a hypothesis set |
| `indicator` | a condition on items marks a hypothesis indicator `observed` (physical data) or `reported` (documents) |
| `forecast` | forecast items become external forecasts on linked ledger questions (question links map the provider's question ids to Foresignal questions) |
| `place`, `situation`, `chronicle` | attach the stream to a place, a situation, or a chronicle layer |

Every stream, layer or atomic, is also a record kind, so trigger rules, search, briefings and Ask can use it without a binding.

## 6. Writing a provider

- Start from the JSON Schemas and the conformance fixtures (`src/test/resources/edp/conformance/`), and validate with `POST /v1/external/validate` or `foresignal edp validate` (CLI, prompt 140).
- Provider libraries for Python and TypeScript (prompt 140) handle batching, NDJSON, cursors, retries and the pull endpoint.
- Keep each provider in its own repository. Foresignal's catalogue lists provider ideas (`docs/Source-Catalog.md` "External provider ideas") but ships none of them.
- Report breakage with `status` items rather than sending empty data.
