# 096 — Satellite Scenes STAC

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/096-CR-Satellite-Scenes-STAC.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/095-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4b row "Satellite scenes (STAC)": footprints and links only, Earth Search default
- `docs/Product-Charter.md` §5 (non-goal: hosting or proxying imagery) and `docs/Weather-Imagery-Design.md` §0.1 (Elaro hosts only products derived from data it ingests; third-party imagery stays client-direct)
- `docs/Core-Data-Model.md` §2 (revised events: `eventTime` = acquisition, `observedAt` = upstream `updated`), §3 (limits: ≤ 10,000 vertices, adapter rules 7–13)
- Completion Reports for 036 (places as AOIs), 073 (shared geometry helpers, antimeridian handling if added)
- Upstream: Earth Search `https://earth-search.aws.element84.com/v1` (landing page, `/collections`, `/search`), STAC API Item Search spec and the `query`, `sort` and `fields` extensions (`https://github.com/stac-api-extensions`), Copernicus Sentinel data legal notice, USGS Landsat data policy

## Objective

Record new Sentinel-2 and Landsat (optionally Sentinel-1) scenes over the operator's areas of interest as `satellite-scene` records: footprint geometry, acquisition time, cloud cover, AOI coverage, and links to the STAC item and any public thumbnail. Elaro never downloads or proxies imagery; clients follow the links. Rules can then alert on "a new cloud-free scene covers home".

## Tasks

### 1. `EarthSearchFamily` (`ai.elaro.source.adapters.stac`)

Static, code-reviewed feed list against `https://earth-search.aws.element84.com/v1` (v1 is current, STAC 1.0.0, items appear about 2 h after acquisition, no deprecation announced; verified 2026-10):

| Source | Collection | Cloud filter | Licence and attribution |
|---|---|---|---|
| `stac-sentinel2` | `sentinel-2-c1-l2a` (Collection 1, recommended; the legacy `sentinel-2-l2a` is still ingesting but is not used) | yes | "Copernicus Sentinel data terms (free, full and open)", "Contains modified Copernicus Sentinel data {year}" |
| `stac-landsat` | `landsat-c2-l2` | yes | "US-PD (USGS Landsat)", "Landsat imagery courtesy of the U.S. Geological Survey" |
| `stac-sentinel1` | `sentinel-1-grd` (disabled by default) | no | Copernicus, as above |

- Params: `aoi` (place id or bbox, **required**; enabling without it fails with 400 `VALIDATION_FAILED`), `maxCloudCover` (default 30; ignored for Sentinel-1), `lookbackDays` (default 3, first run only), `maxPages` (default 5).
- Descriptor: STANDARD, ALL, no TTL, `observedAtMode=UPSTREAM`, `zeroResultsNormal=true` (cloudy weeks return nothing), interval 1 h (min 15 min), budget 4/min and 300/day (no published limit and no rate-limit headers; requests are serial), `defaultRetention` 90 d, `maxResponseBytes` 8 MB.

### 2. Search

- `POST {base}/search` with a JSON body:
  - `collections: [<collection>]`;
  - `intersects`: the AOI polygon (or `bbox` for bbox AOIs); split AOIs that cross the antimeridian into two requests;
  - `datetime: "{from}/.."` where `from = lastAcquisition − 2 d` from adapter state (late processing), else `now − lookbackDays`;
  - `query: {"eo:cloud_cover": {"lt": maxCloudCover}}`. **Use the `query` extension; CQL2 `filter` is not supported and is silently ignored** (it returns every item, verified 2026-10). Add a parser-side guard: if any returned item violates the requested cloud or time bounds, throw `UpstreamFormatException("search filter ignored")` instead of ingesting;
  - `sortby: [{"field": "properties.datetime", "direction": "desc"}]`, `limit: 100`;
  - `fields: {"include": ["id", "collection", "geometry", "bbox", "properties.datetime", "properties.created", "properties.updated", "properties.eo:cloud_cover", "properties.platform", "properties.constellation", "properties.instruments", "properties.s2:mgrs_tile", "properties.grid:code", "properties.landsat:wrs_path", "properties.landsat:wrs_row", "properties.view:sun_elevation", "properties.sar:instrument_mode", "properties.sar:polarizations", "assets", "links"]}`.
- Pagination: follow the `rel=next` link (its body token or `next` parameter is opaque; send it back unchanged), at most `maxPages`. Stop early when items are older than `from`.
- Store `lastAcquisition` (newest `datetime` seen) in adapter state after a successful run.

### 3. Parser (`StacItemParser`, pure)

- Kind `satellite-scene`, `externalId = {collection}:{id}`, `eventTime = properties.datetime`, `observedAt = properties.updated` (fallback `created`, fixed per collection and decided by the fixture, never per item).
- Geometry: the item footprint via `Geo.fromGeoJson`; simplify with JTS `TopologyPreservingSimplifier` to ≤ 10,000 vertices; split antimeridian-crossing footprints into a MultiPolygon.
- Payload:
  - `collection, platform` (now includes `sentinel-2c` and `sentinel-1c`), `constellation, instruments`;
  - `cloudCover` (1 decimal; absent for SAR), `mgrsTile` (`s2:mgrs_tile`) or `gridCode` (`grid:code`), `wrsPath`/`wrsRow` (Landsat), `sunElevation`, `sarMode`, `polarizations` (SAR);
  - `aoiCoverPct`: area of footprint ∩ AOI divided by AOI area, computed in a local equal-area projection centred on the AOI (0–100, integer);
  - `stacItemUrl` (the item `self` link), `thumbnailUrl` **only** when `assets.thumbnail.href` is `https://` (Sentinel-2). Landsat and Sentinel-1 thumbnails are `s3://` hrefs in requester-pays buckets (`usgs-landsat`, `sentinel-s1-l1c`): omit them, never rewrite or sign them;
  - `assetKeys` (sorted list of asset names, ≤ 40).
- `volatileFields = []`, `significantFields` empty (all keys significant). An upstream reprocessing that changes `updated` but nothing else hashes equal and becomes a touch.
- Never fetch any asset. Assert this in the WireMock test by failing on any request outside `/v1/search`.
- `titleField` built as `{platform} {mgrsTile|wrsPath/wrsRow} {cloudCover}%`; `searchFields = [platform, mgrsTile, gridCode]`; `tileProperties = [platform, cloudCover, aoiCoverPct, eventTime]`.

### 4. Alternatives (not implemented)

Microsoft Planetary Computer STAC (`https://planetarycomputer.microsoft.com/api/stac/v1/search`) supports CQL2 `filter`, but its asset links need SAS signing. Note it in the Source Catalog as an alternative; Earth Search stays the default (Gap-Analysis §4b).

### 5. Spec, Postman, docs

- No new operations. Add kind `satellite-scene` through `fieldSpecs` (units: `cloudCover` `%`, `sunElevation` `deg`); regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman` if examples change.
- `docs/Source-Catalog.md` rows for the three sources (tier, policy, `observedAtMode`, retention, licence) with the verified 2026-10 notes: `sentinel-2-c1-l2a`, `query` not `filter`, requester-pays thumbnails, Planetary Computer alternative.
- `docs/Operator-Guide.md` "Satellite scenes": AOI setup, the "links only, no imagery" rule, and rule recipes: `kind=satellite-scene`, `cloudCover < 20 AND aoiCoverPct >= 80`, `match` with `only_new`; and a Sentinel-1 recipe for cloudy-season flood watching.
- `docs/Agent-Guide.md`: agents find scenes with `query_records kind=satellite-scene` and follow `stacItemUrl`; Elaro never fetches imagery.

## Tests to write (do not run)

- `StacItemParserTest` with real trimmed fixtures (one Sentinel-2 C1, one Landsat, one Sentinel-1 search page; README with request body and capture date): field mapping, `aoiCoverPct` for full, partial and no overlap, `https` thumbnail kept and `s3://` dropped, antimeridian split, simplification limit, a filter-ignored response raises `UpstreamFormatException`.
- `EarthSearchAdapterTest` (WireMock): POST body contains `collections`, `intersects`, `datetime`, the `query` cloud clause and `sortby`; `next` token followed verbatim and capped at `maxPages`; early stop on old items; adapter state advances only after success; no request outside `/v1/search`; 429 with `Retry-After` honoured.
- `StacDescriptorsTest`: AOI required, Sentinel-1 disabled by default, licences set, defaults pass `IngestPolicyValidator`.
- `StacIngestIT` (`@IntegrationTest`, WireMock): first run ingests N scenes; the same page again writes no versions; a reprocessed item with a changed `cloudCover` becomes version 2; an `only_new` rule on `aoiCoverPct >= 80` fires once.

## Acceptance criteria

- `stac-sentinel2` and `stac-landsat` register as family members; enabling requires an AOI.
- No code path downloads, proxies or signs an imagery asset.
- Scenes carry footprints, acquisition time, cloud cover, AOI coverage and item links, with the correct licence per collection.
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
2. **Commit** all changes with the message `096: Satellite Scenes STAC`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/096-CR-Satellite-Scenes-STAC.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `096: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
