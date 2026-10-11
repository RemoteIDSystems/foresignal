# 073 — Generic Feature And Feed Kits

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/073-CR-Generic-Feature-And-Feed-Kits.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/072-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4c rows "Generic RSS/Atom/GeoRSS/JSON Feed family" and "OGC API / ArcGIS / WFS kits", §8.C item 34 (Gap label 039a = this prompt)
- `docs/Core-Data-Model.md` §2 (time contract), §3 (descriptor fields, adapter rules 7–13, limits incl. 10,000 vertices), §5.3 defaults
- `docs/Architecture.md` §4.2 (adapter families: static, code-reviewed lists; never caller-supplied)
- `docs/Adapter-Guide.md` (prompt 011); prompt 072's `AbsenceTombstoner` (`ai.foresignal.source.support`)
- Prompt 035 outbound URL policy (`OutboundUrlPolicy`), prompt 024 IngestFormat SPI (for the WebSub push path)
- Specs: ArcGIS REST "Query (Feature Service/Layer)"; OGC API – Features Part 1 (OGC 17-069r4); WFS 2.0 GetFeature (GeoServer `outputFormat=application/json`); RSS 2.0; Atom RFC 4287; GeoRSS Simple and GML; W3C Basic Geo; JSON Feed 1.1; W3C WebSub

## Objective

Build two reusable, protocol-level kits so most "it's just a feature service" or "it's just a feed" sources become a reviewed resource file instead of a hand-written adapter: a **feature kit** (ArcGIS FeatureServer/MapServer query, OGC API Features, WFS-JSON) and a **feed kit** (RSS, Atom, GeoRSS, JSON Feed) with a curated bulletin pack and opt-in WebSub. Later prompts (075 fire perimeters and cones, 079 hydrology, 088 PortWatch and maritime notices, 094 reference datasets) build their sources on these kits.

## Tasks

### 1. Feature kit clients (`ai.foresignal.source.kit.feature`)

All clients use `ctx.http()` only, are stateless, and return an iterator of GeoJSON feature trees with hard caps (`maxPages`, default 10; `maxFeatures`, default 50,000; overall bytes via `maxResponseBytes`).

- **`ArcGisQueryClient`**
  - Reads layer metadata (`{layerUrl}?f=json`) at most once per 24 h (cache in `AdapterStateStore`): `maxRecordCount`, `advancedQueryCapabilities.supportsPagination`, `editingInfo.lastEditDate` / `editingInfo.dataLastEditDate` when present, field list and types.
  - Query: `{layerUrl}/query?where=…&outFields=…&returnGeometry=true&outSR=4326&f=geojson&resultOffset=N&resultRecordCount=min(pageSize,maxRecordCount)&orderByFields=<objectIdField>` plus optional `maxAllowableOffset` / `geometryPrecision`.
  - Page while `exceededTransferLimit` is true (top level or under `properties`) and the page is non-empty.
  - Change short-circuit: when `lastEditDate` is unchanged since the last complete fetch, return `FetchResult.unchanged()` (no query sent). Many services (e.g. NOAA MapServers) expose no `editingInfo`; then always query.
  - Fall back to `f=json` (Esri JSON) only if a spec sets `format: esrijson`; convert rings/paths/points with a small converter (outer ring orientation per Esri: clockwise = outer).
  - Date fields arrive as epoch milliseconds.
- **`OgcFeaturesClient`**: `{base}/collections/{id}/items?f=json&limit=…[&bbox=…][&datetime=…][&<property filters>]`, follows `links[rel=next]` (any query shape, e.g. USGS uses `cursor=`), accepts `application/geo+json` or `application/json`, stops on a missing `next`. Ignores `numberMatched` for paging.
- **`WfsJsonClient`**: `GetFeature` with `service=WFS&version=2.0.0&typeNames=…&outputFormat=application/json&count=N&startIndex=K&srsName=urn:ogc:def:crs:EPSG::4326`, pages while `numberReturned == count`. **Axis order:** GeoServer's GeoJSON writer emits lon,lat even when the response `crs` says `EPSG::4326` (verified 2026-10 on CWFIS); a spec field `axisOrder: lonlat|latlon` (default `lonlat`) controls it, and a sanity check against the spec's `bbox` swaps and warns once if the first 20 points only fit when swapped.
- **`FeatureGeometry`**: GeoJSON → JTS via `Geo.fromGeoJson`; if the geometry exceeds 9,500 vertices, simplify with `TopologyPreservingSimplifier` at doubling tolerances starting at 0.0005° until it fits, and set payload `geometrySimplified=true` plus `simplifyToleranceDeg`.

### 2. Feature source specs and family

- Resource files `src/main/resources/kits/features/*.json`, each an array of `FeatureSourceSpec`:
  `id, name, description, kind, protocol (arcgis|ogc|wfs), url, layer|collection|typeName, query {where, filters, bbox, outFields}, idField, titleField, fields {source → target, with type, UCUM unit and optional `valueMap` (e.g. `{"1": "low", "2": "high"}`)}, constants {target → fixed value added to every record, e.g. `restrictionType`}, time {observedAt: field|RECEIVED, eventTime, validFrom, validUntil, format}, recordTtl, ttlAnchor, interval, minInterval, budget {perMinute, perDay, mbPerDay}, paging {pageSize, maxPages}, geometry {maxAllowableOffset, geometryPrecision, axisOrder}, absenceTombstones (bool), zeroResultsNormal, significantFields, volatileFields, searchFields, tileProperties, licence, termsUrl, attribution, homepage, headers (allowlist: Accept, Digitraffic-User), credential (optional: name + placement query|header)`.
- `FeatureSpecValidator` (startup, fail fast, all errors listed): https only; no userinfo or credential-looking query values in `url`; id pattern and uniqueness against the registry; fields referenced by `idField`, `titleField`, time and `significantFields` are declared targets; time base fixed per source (Core-Data-Model §2: never a per-record fallback between fields); `recordTtl` vs interval satisfies the §5.1 keyframe constraint.
- `FeatureKitFamily implements SourceAdapterFamily` creates a `KitFeatureAdapter` per spec. The adapter maps each feature through `FeatureMapper` (pure): allowlisted fields only, renamed, value-mapped, spec constants added, typed (number coercion from strings, trimmed strings, epoch-ms/ISO-8601/`yyyy-MM-dd HH:mm:ss` with the spec's zone), nulls and sentinels (`-999`, `-9999`, `0001-01-01…`) removed, `externalId` from `idField` (string), rejected when missing. Uses `AbsenceTombstoner` when `absenceTombstones=true` and the fetch was complete (not truncated, `maxPages` not hit).
- Descriptor from spec: `historyTier=STANDARD`, policy ALL, plus the spec fields; every kit source ships **disabled**.
- Starter spec (proves the ArcGIS path in production): `feature-wfigs-incidents`: NIFC WFIGS Incident Locations Current (`services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/WFIGS_Incident_Locations_Current/FeatureServer/0`), kind `fire-incident`, id `IrwinID`, observedAt `ModifiedOnDateTime_dt`, eventTime `FireDiscoveryDateTime`, absence tombstones on, 10 min, public domain. Verified 2026-10: `maxRecordCount` 2000, `editingInfo.lastEditDate` present (so the short-circuit applies), fields `IrwinID`, `IncidentName`, `IncidentSize`, `PercentContained`, `POOState`, `UniqueFireIdentifier`, `FireDiscoveryDateTime`, `ModifiedOnDateTime_dt`. OGC and WFS starters arrive with prompts 075 and 079.
- **Operator overlay (off by default):** when `foresignal.kits.operator-specs.enabled=true`, also load `${FORESIGNAL_DATA_DIR}/kits/{features,feeds}/*.json` at startup. Every URL must pass `OutboundUrlPolicy` (prompt 035); if that class is missing, refuse the overlay and log why. Overlay ids must start with `op-`. Never accept specs over the API.

### 3. Feed kit (`ai.foresignal.source.kit.feed`)

- **`FeedParser`** (pure): `byte[] → FeedDocument` for RSS 2.0, RSS 1.0 (RDF), Atom 1.0 and JSON Feed 1.0/1.1. Detect by content type, then by root element / `version` key.
  - XML through a hardened StAX factory (`SUPPORT_DTD=false`, external entities off, coalescing on, 8 MB cap). Handle `CDATA`, `content:encoded`, Atom `type=html|xhtml|text`.
  - `FeedItem`: `id` (guid / Atom id / JSON `id`; else SHA-256 of `link + title`, first 16 hex), `title`, `summary` (HTML → plain text with jsoup `Safelist.none()`, whitespace collapsed, ≤ 4 KB), `link` (absolute, http/https only), `published`, `updated`, `authors`, `categories` (≤ 20), `enclosures` (url, type, length; ≤ 5), `language`, `geometry`.
  - Geometry: GeoRSS Simple (`georss:point|line|polygon|box`, "lat lon" order), GeoRSS GML (`gml:Point/gml:pos`, `gml:LineString/gml:posList`, `gml:Polygon/gml:exterior/gml:LinearRing/gml:posList`, lat-lon order unless `srsName` says CRS84), W3C Basic Geo (`geo:lat`/`geo:long`, or `geo:Point`), and JSON Feed `_geo` extension if present.
  - Dates: RFC 822 (including two-digit years and named zones like `EDT`), RFC 3339, ISO-8601 without zone → UTC with a parser warning.
- **`FeedSourceSpec`** resource files `src/main/resources/kits/feeds/*.json`: `id, name, url, kind (default bulletin), language, categories filter (include/exclude), maxItems (default 200), recordTtl (default 30 d, ttlAnchor RECEIVED), observedAt (updated|published|RECEIVED), interval (default 15 min, min 5 min), budget, licence, termsUrl, attribution, itemMapper (optional named mapper class for source-specific parsing, e.g. prompt 075 InciWeb)`.
- `FeedKitFamily` → `KitFeedAdapter`: conditional GET; one record per item: `kind` from spec, `externalId = item.id`, geometry from the item (often null), payload `title, summary, link, published, updated, categories, authors, enclosures, feedTitle`; `eventTime = published`; `observedAt` per spec; `embedFields = title, summary`; `titleField = title`; `searchFields = title, summary, categories`. Feeds are windows, so **no absence tombstones**; expiry is by `recordTtl`.
- **Untrusted text:** item text is upstream-controlled. Under prompt 028's rules all record text reaching an LLM is untrusted data, never instructions; keep summaries plain text and never render them as HTML.

### 4. Curated bulletin pack (`kits/feeds/bulletins.json`, all disabled)

Verified reachable with Foresignal's UA on 2026-10-10:

| Source id | URL | Licence |
|---|---|---|
| `feed-cisa-advisories` | `https://www.cisa.gov/cybersecurity-advisories/all.xml` (1.2 MB: interval 60 min) | US public domain |
| `feed-ncsc-uk` | `https://www.ncsc.gov.uk/api/1/services/v1/all-rss-feed.xml` | OGL v3 |
| `feed-nhc-atlantic` | `https://www.nhc.noaa.gov/index-at.xml` | US public domain |
| `feed-nhc-eastpacific` | `https://www.nhc.noaa.gov/index-ep.xml` | US public domain |
| `feed-ecdc-threats` | `https://www.ecdc.europa.eu/en/taxonomy/term/1307/feed` | ECDC reuse policy (attribution) |
| `feed-who-news` | `https://www.who.int/rss-feeds/news-english.xml` | WHO terms (attribution, non-commercial reuse) |

Smithsonian GVP's weekly RSS returned 403 to Foresignal's UA (2026-10) and is not included.

### 5. WebSub (off by default)

- Setting `foresignal.kits.websub.enabled=false` (bootstrap property). Only when true, and only for feed sources with spec `websub: true`, and only when `publicBaseUrl` is an `https` URL: if the feed advertises `<link rel="hub">` and `<link rel="self">` (or JSON Feed `hubs`), subscribe: `POST hub` with `hub.mode=subscribe`, `hub.topic=<self>`, `hub.callback=${publicBaseUrl}/v1/websub/{sourceId}/{subscriptionToken}`, `hub.secret` (32 random bytes, stored encrypted with `CredentialCipher`), `hub.lease_seconds=86400`. Hub URLs must pass `OutboundUrlPolicy`.
- New table `websub_subscription(source_id PK, hub, topic, token, secret_enc, state, lease_expires_at, last_verified_at, last_push_at)`.
- Endpoints:
  - `GET /v1/websub/{sourceId}/{token}`: intent verification; echo `hub.challenge` only when mode, topic and token match a pending subscription; else 404.
  - `POST /v1/websub/{sourceId}/{token}`: verify `X-Hub-Signature` (`sha256=` preferred, `sha1=` accepted) over the raw body with constant-time compare; 202 on success; parse with `FeedParser` and hand records to the source's `RecordSink` with `receivedAt` stamped by the sink; 403 on a bad signature; body cap 2 MB; per-source rate cap 60/min.
- While a subscription is active, the poll interval stretches to 6 h as a safety net. Renew at 80% of the lease; on failure fall back to polling and set health reason "WebSub lease lost".

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: the two `/v1/websub/{sourceId}/{token}` operations with `"x-foresignal-scope": "open"` and `security: []`, query parameters `hub.mode`, `hub.topic`, `hub.challenge`, `hub.lease_seconds`; 202/403/404 responses (ProblemDetail). Add a Postman example body (an Atom snippet) in the generator's BODIES map and regenerate with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `AccessPolicy`: permit anonymous `GET|POST /v1/websub/*/*` only; everything else unchanged (fail closed).
- `docs/Adapter-Guide.md`: "Kit sources" section: writing a feature or feed spec, validator rules, the time-base rule, simplification, operator overlay, WebSub.
- `docs/Source-Catalog.md`: kit rows (`feature-wfigs-incidents`, `feed-*`) and a note that 075/079/088/094 sources are kit-based.

## Tests to write (do not run)

- `ArcGisQueryClientTest` (WireMock): pagination until `exceededTransferLimit=false`, `maxPages` cap marks the result incomplete, `lastEditDate` short-circuit, metadata cache reuse, `f=json` conversion including a hole.
- `OgcFeaturesClientTest`: `next` links with cursor and offset styles, content types, stop conditions.
- `WfsJsonClientTest`: `startIndex` paging, axis-order swap warning against bbox.
- `FeatureGeometryTest`: vertex-cap simplification keeps validity and sets the flags.
- `FeatureSpecValidatorTest`: each rule fails with a field path; all errors aggregated.
- `FeatureMapperTest`: typing, value maps, constants, sentinels removed, date formats, missing id rejected, `SnapshotContract.assertStableStaticHash`.
- `FeedParserTest`: fixtures for RSS 2.0, RSS 1.0, Atom, JSON Feed 1.1, GeoRSS simple and GML, Basic Geo, XXE payload rejected, HTML stripped, odd dates.
- `KitFeedAdapterTest` (WireMock): conditional GET, item cap, category filter.
- `WebSubControllerIT` (`@IntegrationTest`): disabled by default (404); verification echo; signature good/bad; topic mismatch 404; records reach the sink; lease renewal scheduling with `MutableClock`.
- `KitIngestIT`: `feature-wfigs-incidents` from a fixture ingests N records and tombstones after two misses; a feed source ingests items and leaves absent items alone.

## Acceptance criteria

- Both families register their specs at startup; all kit sources are disabled by default.
- With `foresignal.kits.websub.enabled=false`, no subscription is ever attempted and the callback returns 404.
- Spec validation failures stop startup with every problem listed.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `073: Generic Feature And Feed Kits`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/073-CR-Generic-Feature-And-Feed-Kits.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `073: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
