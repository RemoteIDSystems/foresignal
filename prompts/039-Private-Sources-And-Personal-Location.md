# 039 — Private Sources And Personal Location

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/039-CR-Private-Sources-And-Personal-Location.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/038-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 7, §4c "Personal location" row, §8 item 24 ("028e" = this prompt)
- `docs/Core-Data-Model.md`:
  - §1: I1, I7, I9 (only the writer touches heads; heads are purged only when every version is gone)
  - §6: the `IngestCommand` sealed type, `HeadPurge`
  - §10: ChangeSummary and SSE `change` events
  - §13: retention
- Completion Reports of:
  - 008: `private` descriptor flag and the precision cap before hashing
  - 023: private visibility gate, audit log, device metadata
  - 024: the `IngestFormat` SPI and ingest key accepted as Bearer/Basic
  - 027, 028, 029, 030: embeddings, Ask, briefings, enrichment
  - 031–033: push payloads, inbox
  - 035: restricted tokens, anonymous profile, feed keys, local-vs-remote URL classification
  - 036: moving anchors
  - 037, 038: tracks, frames, stats, glance, metrics
- Upstream protocol docs, all verified 2026-10:
  - OwnTracks HTTP mode, https://owntracks.org/booklet/tech/http/ : Basic auth, `X-Limit-U`/`X-Limit-D`, reply `[]`
  - Traccar OsmAnd protocol, https://www.traccar.org/osmand/ : query or form parameters. The Traccar server's `OsmAndProtocolDecoder` also accepts `Content-Type: application/json` bodies `{device_id, location{timestamp, coords{latitude, longitude, speed, heading, accuracy, altitude}, battery{level, is_charging}, is_moving, event}}` from the current Traccar Client.
  - Overland, https://github.com/aaronpk/Overland-iOS : `{"locations":[GeoJSON Feature…]}`, `Authorization: Bearer`, must reply `{"result":"ok"}` or the app retries.
  - GPSLogger custom URL, https://gpslogger.app/ : user-templated method, body and headers, Basic auth supported.

## Objective

Make it safe to feed family phones and personal trackers into Foresignal.

- A `private` source is invisible on every read and egress path unless the caller is explicitly allowed.
- The four common tracker apps can push without custom glue.
- Devices get keys that can write only their own entity.
- Any single entity can be exported or erased. The erasure fans out a tombstone so offline clients and caches drop it too.

## Tasks

### 1. Visibility model (`ai.foresignal.security.visibility`)

- Effective privacy = descriptor `private` (008) **OR** `CustomSourceInput.private` **OR** `source_settings.private`. An admin can tighten a source this way but cannot make a descriptor-private source public.
- `SourceVisibility`:
  - `isPrivate(id)`, `privateSourceIds()`: cached, refreshed on settings and registry events
  - `canSeePrivate(principal)`: true for admin, and for read or agent tokens, feed keys and tile tickets created with the 023 private grant (check the CR for its exact name; add a `privateAccess` boolean to `api_token` and to the key tables if missing); always false for anonymous callers
- `PrivateFilter.sql(alias)` and `PrivateFilter.allows(sourceId)`: one helper used by every query builder below.
- A direct GET of a private record by an unauthorised caller returns **404** (not 403), so its existence does not leak.

### 2. Enforce on every path

Apply each item below and test it.

- **Records and history:** `/v1/records`, `/{s}/{id}`, `/versions`, `/track`, `/v1/history`, `/history/frames`, `/v1/timeline`, `/v1/changes`, asOf, `count=true`, and `/v1/kinds` counts.
- **Sources:** `/v1/sources` lists private sources only to allowed callers.
- **Tiles:** kind layers exclude private sources; a private source layer returns 404. TileJSON and 016 indexes omit them. Tiles that include private data are served with `private` cache headers only.
- **SSE:** change and live lanes and the multiplexed `/v1/events/stream` filter per client at the hub. A client whose grant is revoked loses the stream (`reset`).
- **Search and embeddings:** the `embed` setting is forced off for private sources, and existing embedding rows are deleted when a source becomes private. Text search excludes them for unauthorised callers.
- **LLM egress (Ask, briefings, enrichment, MCP-sampled context):** private records are never placed in a prompt for a provider that 035 classifies as remote. For local providers they are included only when `llm.allowPrivateLocal=true` (default false). Enrichment is off for private-source rules unless both conditions hold.
- **MCP:** every tool filters per token, using the same `PrivateFilter`.
- **Alerts:**
  - Alerts on private records (including `record_snapshot`) are visible only to allowed callers.
  - Notification payloads for them carry only the rule name, severity and a link, unless the channel has `privatePayload: true` (an admin opt-in per channel).
  - Web Push, APNs and FCM payloads (031–032) never carry private payloads.
- **Stats, glance, metrics (038):** keep the exclusions 038 added, and add a test for each.
- **Later egress hooks:** add an `EgressPolicy.allows(path, sourceId, principal)` interface, with paths EXPORT, SINK, FEDERATION, SHARE and FEED. Prompts 058, 110, 111, 112 and 113 must call it. Document this in Architecture.
- **Audit (023):** every private-data read by a non-admin principal, and every export or purge.

### 3. Personal-location ingest formats (024 `IngestFormat` SPI, `ai.foresignal.inbound.formats.location`)

Each format owns its auth, its reply shape and its field mapping. Every one of them maps to kind `tracker`.

| Format | Auth | Reply | Mapping |
|---|---|---|---|
| `owntracks` (POST JSON) | Basic; the password is the ingest or device key (§4) | `200 application/json []` | `_type=location`: `lat, lon`, `tst` (epoch s) → observedAt, `acc` → quality.accuracyMeters, `alt` → altitudeM, `vel` km/h → speedMs, `cog` → courseDeg, `batt`, `bs` → charging, `conn`, `tid`. externalId = `X-Limit-U/X-Limit-D`, else `tid`. `_type=transition` → kind `tracker-transition` record (`event`, `desc`, `wtst`). `waypoint`, `lwt`, `card`, `status` → `[]` and ignored. `_type=encrypted` → `[]`, dropped, counted, and the health warning "OwnTracks payload encryption is not supported; disable it". |
| `osmand` (Traccar Client, OsmAnd; GET or POST, query/form or JSON) | No native auth: key in path `/v1/ingest/{source}/k/{key}/osmand` (024's path-key form if it exists, else add it here) or a device key; `id`/`deviceid`/`device_id` must match the key's bound device | `200` empty | Query: `lat, lon` or `location=lat,lon`; `timestamp` (epoch s, epoch ms, ISO, or `yyyy-MM-dd HH:mm:ss` UTC); `speed` (knots); `bearing`/`heading`; `altitude`; `accuracy`; `batt`; `charge`; `valid=false` → rejected. JSON: as in Read first; speed is m/s. |
| `overland` (POST JSON) | `Authorization: Bearer <key>` | `200 {"result":"ok"}`, even when some points are rejected (rejections are counted, not retried) | Each `locations[]` Feature: geometry; `properties.timestamp`, `horizontal_accuracy`, `altitude`, `speed` (m/s), `course`, `motion[]`, `battery_level`, `battery_state`, `wifi` (dropped unless `keepWifi`), `device_id` → externalId. Ignore `trip` and `current`. Up to 1,000 points per request. |
| `gpslogger` (GET or POST form/JSON) | Basic or Bearer | `200` | Documented template `lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER`. Accept these names case-insensitively. Speed is m/s. Confirm the placeholder list in the app's Parameters screen and record it in the CR. |

- **Accuracy filter:** drop fixes with accuracy > `maxAccuracyMeters` (a source param, default 200), reason `LOW_ACCURACY`. Drop `valid=false` and (0,0).
- **Logging:** never log query strings or paths containing keys. Add a redaction filter for `/k/{key}/` and `key=`, and document reverse-proxy log rules.
- **Profile `personal-location`:** `POST /v1/custom-sources` with `profile: "personal-location"` applies these defaults:
  - `private=true` (cannot be unset), STANDARD tier, kind `tracker`
  - `observedAtMode=UPSTREAM`, `ttlAnchor=OBSERVED`, `recordTtl=24h`, default retention 7d
  - policy SAMPLED (minInterval 30 s, keyframe 10 min, minDeviation 50 m, `significantFields=[motion, charging]`)
  - `embed=false`, enrichment off
  - precision cap off by default (the owner's own data; 008's cap is available)

  STANDARD is required so per-entity purge can use indexed deletes (§5). 059 later generalises profiles into presets.
- **fieldSpecs:** `speedMs` (m/s), `courseDeg` (deg), `altitudeM` (m), `battery` (%), `charging`, `motion`.

### 4. Device-scoped ingest keys

- Table `ingest_device`: `id`, `source_id`, `external_id`, `name`, `key_hash`, `key_prefix` (`eid_`), `created_at`, `last_seen_at`, `revoked_at`. A device key authenticates **only** for its source and **forces** externalId = its bound id. A mismatched id in the payload → 403 `DEVICE_ENTITY_MISMATCH`.
- Endpoints (admin):
  - `GET/POST /v1/custom-sources/{id}/devices`
  - `DELETE /v1/custom-sources/{id}/devices/{deviceId}`
  - `POST …/{deviceId}/key/rotate`

  Creation returns the key once, plus app setup data:
  - an OwnTracks `.otrc` JSON (mode 3 HTTP, url, auth, username, deviceId)
  - the Overland endpoint and token
  - the Traccar server URL with the key path
  - the GPSLogger URL template

  Base all URLs on `publicBaseUrl`.
- Revoking a key stops ingest immediately. The device's records stay until retention removes them or the operator purges them.

### 5. Per-entity export and purge

- `GET /v1/records/{sourceId}/{externalId}/export?format=geojson|ndjson|gpx` (admin) streams:
  - the current state and every retained version (with observers)
  - alerts referencing the entity
  - a manifest with counts and SHA-256
  - GPX `trk` for `tracker`
- `POST /v1/records/{sourceId}/{externalId}/purge {purgeAlerts?: false}` (admin) submits a new writer command `EntityPurge(sourceId, externalId, future)`. Extend the sealed `IngestCommand`, record a Core-Data-Model amendment in the CR, and keep I1 and I9. On the writer thread, in one transaction:
  1. Write a tombstone version (`persist_reason` TOMBSTONE, new qa flag 32 `PURGED`). Its `ChangeSummary` and SSE `change` carry `purged: true`, so SSE clients, `/v1/changes` readers and offline caches delete the entity.
  2. `DELETE FROM record_version WHERE source_id=? AND external_id=? AND seq < :tombstoneSeq`. This uses the `(source_id, external_id, seq)` index on both tiers; it is a bounded single-entity delete, not a retention scan.
  3. Set the head and live rows to the tombstone state. A later `HeadPurge` removes them under I7.
- After the writer commits, a `PurgeFanout` listener:
  - deletes `embedding_document` rows
  - redacts `alert.record_snapshot` and `message` (`{"redacted":true}`), or deletes the alerts when `purgeAlerts`
  - redacts persisted notification bodies (018/033)
  - deletes 040 `record_region` rows
  - drops metric series
  - counts (but does not edit) Ask messages and briefing runs whose citations reference the entity
- Response `PurgeReport{tombstoneSeq, versionsDeleted, alertsRedacted, alertsDeleted, embeddingsDeleted, notificationsRedacted, citingDocuments}`.
- The docs state plainly that messages already delivered to external channels cannot be recalled.
- Also `POST /v1/custom-sources/{id}/devices/{deviceId}/purge`: purge the device's entity and revoke its key.
- MCP: `export_entity` and `purge_entity` are admin-scope tools and are **never** offered to Ask's tool loop.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - `private` on `CustomSourceInput`, `SourceSettings`, `SourceDescriptor`; `privateAccess` on token and key schemas; `privatePayload` in the channel config docs
  - `profile` on `CustomSourceInput`
  - operations for the device CRUD and rotate, `exportRecord`, `purgeRecord` and `purgeDevice` (admin), and the ingest format paths (`/v1/ingest/{sourceId}/{format}` plus the `k/{key}` variants and GET for `osmand`/`gpslogger`)
  - `purged` on `Change`; schemas `IngestDevice`, `IngestDeviceCreated`, `PurgeReport`
  - error codes `DEVICE_ENTITY_MISMATCH`, `LOW_ACCURACY` (rejection reason)
- Each operation gets `x-foresignal-scope`. Add `BODIES` entries for the device create and purge, then regenerate Postman.
- `AccessPolicy`: `GET` and `POST /v1/ingest/**` → `INGEST_KEY_OR_ADMIN` (including device keys and path keys); admin for the export and purge paths, which must sit before the `/v1/records/**` READ rule.
- `docs/Operator-Guide.md` "Personal location and privacy":
  - setup per app
  - what `private` hides, on which paths
  - the LLM egress rules
  - export and purge
  - a note on the GDPR household exemption, without legal advice
- Architecture: a visibility section plus the `EntityPurge` command. Product Charter: an FR for private sources, the next free FR number.

## Tests to write (do not run)

- `PrivateEnforcementIT`: a parameterised matrix of {anonymous, read, read + privateAccess, agent, admin} × every path in §2. Hidden means absent from lists and 404 on direct reads; visible means present.
- `SseHubPrivateIT`: filter at the hub; reset on revocation.
- `LlmEgressPrivateIT` (`FakeLlmClient` flagged remote or local): no private text reaches a remote provider in Ask, briefings or enrichment.
- `OwnTracksFormatTest`, `OsmAndFormatTest` (query, form and JSON fixtures; timestamp formats; knots), `OverlandFormatTest`, `GpsLoggerFormatTest`: field mapping, reply shapes, encrypted, invalid and low-accuracy fixes.
- `PersonalLocationIngestIT`: each app end to end with keys; device mismatch 403; revoked key 401; keys redacted in logs.
- `IngestDeviceServiceIT`: key once, rotate, setup bundle URLs.
- `EntityPurgeIT`:
  - versions gone on both tiers
  - tombstone change with `purged`
  - head removed by a later HeadPurge
  - alerts redacted vs deleted
  - embeddings and region rows gone
  - report counts
  - concurrent ingest batch ordering on the writer
- `EntityExportIT`: GeoJSON, ndjson and GPX; manifest hash.

## Acceptance criteria

- With `openReads=true`, an anonymous client cannot learn the position, or the existence, of a private-source entity through any REST, tile, SSE, search, MCP, glance, stats, metrics or notification path.
- OwnTracks, Traccar Client, Overland and GPSLogger each ingest with their native settings and get the reply they expect.
- After a purge, no `record_version` row for the entity predates its tombstone, and SSE clients receive the tombstone.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `039: Private Sources And Personal Location`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/039-CR-Private-Sources-And-Personal-Location.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `039: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
