# 089 — CAP Alerts And International Seismic

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/089-CR-CAP-Alerts-And-International-Seismic.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/088-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `meteoalarm`, `emsc-seismic` (Velocity survey), `usgs-earthquakes`
- `docs/design-history/Core-Data-Model.md` §2 ("Revised events" row: `event_time` = origin, `observed_at` = upstream `updated`), §3 (limits: 10,000 vertices), §4 (volatile fields), §5.3
- `docs/design-history/Gap-Analysis.md` §4b rows "CAP alert family", "SPC Day 1–3 outlooks, WPC ERO", "ShakeMap/PAGER, EMSC WebSocket + FDSN family", §8.C item 48 ("Extend 051" = this prompt; `spc-outlooks` already shipped in 047)
- Completion Reports of 008 (`validFrom`, `entityKeys`), 011 (USGS payload and Adapter Guide altitude convention), 040 (gazetteer), 067 (shared CAP 1.2 parser), 073 (ArcGIS kit)
- Upstream: OASIS CAP 1.2, https://alertingauthority.wmo.int (Register of Alerting Authorities; `atom.xml`), https://feeds.meteoalarm.org, https://www.seismicportal.eu/webservices.html and `/realtime.html`, FDSN Web Service Specifications 1.2 (event), https://earthquake.usgs.gov/fdsnws/event/1/

## Objective

Extend alerting beyond the US: a reviewed family of national CAP feeds seeded from the WMO register, MeteoAlarm for Europe, WPC's Excessive Rainfall Outlook, EMSC's real-time seismic stream with an FDSN family behind it, and USGS ShakeMap/PAGER impact products. Everything CAP-shaped goes through 067's parser so updates and cancels behave the same in every source.

## Tasks

### 1. CAP feed family (`cap-*`)

- **Register seeding is offline and reviewed.** Write `scripts/build-cap-register.py` that reads `https://alertingauthority.wmo.int/atom.xml` (verified 2026-10: Atom entries per authority with country and a record link) and each authority record's "CAP Feed URL", and writes `build/cap-candidates.json`. A human-reviewed subset is committed as `src/main/resources/sources/cap-feeds.json`; the runtime never reads the register and never accepts feed URLs from API callers.
- Entry fields: `id` (`cap-<iso2>-<authority>`, e.g. `cap-de-dwd`), `country`, `authority`, `feedUrl`, `format` (`atom-index`, `rss-index` or `cap-inline`), `languages`, `licence`, `termsUrl`, `reviewedOn`, `notes`. Admit a feed only if it is keyless, documented, over HTTPS, has terms that permit re-use, and is not already covered by another source (skip US NWS: 047 has `nws-alerts`). Per Gap-Analysis, prefer DWD's CAP feed over BBK (BBK is JSON with unclear terms). Start with the feeds you can verify live (aim for 10–20, covering each WMO region); record rejected candidates and reasons in `docs/Source-Catalog.md`.
- **`CapFeedAdapterFamily`**: one source per entry. Fetch the index with conditional GET (parse Atom/RSS indexes with 073's feed-kit parser), then fetch only CAP documents whose link or identifier is new (≤ 50 per run, serial). Parse with 067's CAP parser.
- **Update chains:** keep `identifier → rootIdentifier` in adapter state (from `references`). externalId = `rootIdentifier`, so `Update` messages version the same record; `msgType=Cancel` → tombstone of the root; `Alert` with an unknown reference starts a new root.
- Record: kind `cap-alert`, `observedAt` = `sent`, `eventTime` = `onset` when present, `validFrom` = `onset` ?? `effective`, `validUntil` = `expires`. Pick the `info` block by the source's `languages` order; other languages' headlines go to payload `headlines` (map lang → text). Payload `identifier, sender, status, msgType, scope, category, event, urgency, severity, certainty, headline, description` (≤ 4 KB), `instruction` (≤ 2 KB), `areaDesc, geocodes, web`. Geometry: union of `polygon`/`circle` areas; geocode-only areas → null geometry with `geometryFromGeocode=false` (no remote boundary lookups). `entityKeys` `capIdentifier`. `embedFields` `headline, description` (untrusted). `status` other than `Actual` is skipped unless param `includeTests`.
- Interval 5 min, budget 6/min, 1,000/day per feed.

### 2. `meteoalarm-*` family

- **Verified 2026-10:** `https://feeds.meteoalarm.org/api/v1/warnings/feeds-{country}` returns JSON `{"warnings":[{"alert":{…CAP as JSON…},"uuid":…}]}` (Germany ≈ several MB, no ETag, `Cache-Control: must-revalidate`); areas carry `geocode` entries with `valueName` `EMMA_ID`. The legacy Atom feeds (`feeds/meteoalarm-legacy-atom-{country}`) are the fallback if the JSON API changes. Use the JSON API.
- Reviewed static list of country slugs (the 38 MeteoAlarm members; verify each slug once). Sources `meteoalarm-<slug>`, disabled by default.
- Convert the JSON alert to 067's CAP model with a small `MeteoAlarmJsonAdapter`, then apply the same update-chain logic as §1. Geometry: `polygon` when present; else an EMMA_ID boundary from a bundled file **only if** MeteoAlarm's boundary data licence allows redistribution (verify; otherwise null geometry and `areaDesc` only).
- Licence/terms: read MeteoAlarm's terms of use during implementation and record them exactly (attribution "MeteoAlarm / EUMETNET" at minimum). Interval 15 min, `maxResponseBytes` 32 MB, budget 2/min, 200/day per country.

### 3. `wpc-ero-d1` … `wpc-ero-d5` (073 feature-kit specs)

- **Verified 2026-10:** `https://mapservices.weather.noaa.gov/vector/rest/services/hazards/wpc_precip_hazards/MapServer/{0..4}` = Excessive Rainfall Day 1–5, polygons, fields `product, valid_time, outlook, issue_time, start_time, end_time, dn`, GeoJSON queries supported. Add `kits/features/wpc-ero.json` with one `FeatureSourceSpec` per day layer (`protocol: arcgis`, `where=1=1`, `outSR=4326`), so no adapter code is needed.
- Spec mapping: kind `rainfall-outlook`, `idField` `dn` (one polygon per category per day), `time {observedAt: issue_time, validFrom: start_time, validUntil: end_time}`, fields `dn → category` (1–4 = MRGL/SLGT/MDT/HIGH; if 073's mapper has no value maps, keep the number and document it), `outlook`, `issue_time`; `absenceTombstones: true` so a category that disappears is tombstoned. Day 1 issues at 01, 0830 and 15 UTC: interval 30 min, budget 5/min, 300/day.

### 4. `emsc-seismic` (STREAMING) and the `fdsn-*` family

- **WebSocket** `wss://www.seismicportal.eu/standing_order/websocket` via `ctx.http().openWebSocket` (verified 2026-10: upgrade-only endpoint). Messages `{"action":"create"|"update","data":<GeoJSON Feature>}` with properties `unid, time, lastupdate, mag, magtype, depth, lat, lon, auth, source_catalog, flynn_region, evtype`.
- **Gap fill:** on every (re)connect, query `https://www.seismicportal.eu/fdsnws/event/1/query?format=json&updatedafter={lastUpdate − 5 min}&limit=500` (verified: FeatureCollection, feature `id` = `unid`) and merge before resuming the stream.
- Record: kind `earthquake`, externalId `unid`, `eventTime` = `time`, `observedAt` = `lastupdate`, Point from lat/lon (2D; depth goes in payload `depthKm` per the Adapter Guide altitude convention), payload aligned with 011's USGS keys where they exist (`mag, magType, place` = `flynn_region`, `depthKm, auth, sourceCatalog, evtype`), `entityKeys` none (cross-source dedup is 099's job). Reconnect 15 s → 10 min, watchdog 10 min (quiet periods happen; `zeroResultsNormal`), budget 12 connects/hour.
- **`fdsn-*` family** (POLLING): reviewed `fdsn-event-services.json` with `id, baseUrl, supportsUpdatedAfter, licence`. Start with GEOFON (`https://geofon.gfz.de/fdsnws/event/1/query`) and INGV (`https://webservices.ingv.it/fdsnws/event/1/query`); verify each live; do not add IRIS (its event service was retired in favour of USGS; confirm). Use `format=text` (the FDSN pipe-delimited `#EventID|Time|Latitude|Longitude|Depth/km|Author|Catalog|Contributor|ContributorID|MagType|Magnitude|MagAuthor|EventLocationName`), which every FDSN event service supports, with `updatedafter` where supported, else `starttime` = now − 2 h. `observedAt`: services without an update time in text format use `observedAtMode` RECEIVED for that whole source. Interval 5 min, budget 2/min, 600/day each.

### 5. `usgs-shakemap` (POLLING)

- Index: `https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&producttype=shakemap&updatedafter={last}&minmagnitude={minMagnitude}&orderby=time` (verified 2026-10). Param `minMagnitude` (default 4.5).
- For each event whose `updated` changed, fetch `properties.detail` and read `products.shakemap[0]` (`properties.maxmmi`, `version`, `contents["download/cont_mmi.json"]`; confirm the contour file name at fixture capture) and `products.losspager[0].properties.alertlevel`, `alertfatality`, `alertecon`.
- Records: kind `earthquake-impact`, externalId event id, Point epicentre, `eventTime` = origin time, `observedAt` = event `updated`, payload `maxMmi, pagerAlert, pagerFatalityAlert, pagerEconomicAlert, shakemapVersion, mag, place`; and kind `shakemap-mmi`, externalId `{eventId}:mmi{level}`, MultiLineString contour per MMI level, simplified with `TopologyPreservingSimplifier` (tolerance 0.001°, then coarser until ≤ 10,000 vertices). Tombstone contour levels that disappear in a newer version.
- Interval 10 min, budget 20/min, 1,500/day (USGS asks for reasonable request rates; details only on change).

### 6. Fixtures

Capture: two CAP feeds' indexes and five CAP documents including an Update and a Cancel chain; one MeteoAlarm country JSON trimmed to 20 warnings; all five ERO layers; a recorded EMSC WebSocket session (≥ 10 messages including an update) plus the FDSN gap-fill response; GEOFON and INGV text responses; one ShakeMap event detail with contours and PAGER. README per directory with URL and date.

### 7. Spec, Postman, docs

- No new operations. Add kinds `cap-alert, rainfall-outlook, earthquake-impact, shakemap-mmi` where enumerated.
- `docs/Source-Catalog.md`: the CAP family table (accepted and rejected feeds), MeteoAlarm JSON API and terms, ERO, EMSC WebSocket + FDSN family, ShakeMap. `docs/Adapter-Guide.md`: "CAP update chains" (root identifier rule) for any future CAP source.

## Tests to write (do not run)

- `CapUpdateChainTest`: Alert → Update → Cancel versions one record then tombstones it; unknown reference starts a root; language selection.
- `CapFeedAdapterFamilyTest` (WireMock): only new documents fetched, 50-per-run cap, conditional index GET, non-`Actual` skipped.
- `CapFeedsResourceTest`: every committed entry has HTTPS, licence, termsUrl and `reviewedOn`; ids unique and valid.
- `MeteoAlarmJsonAdapterTest`: JSON → CAP model fidelity on the real fixture.
- `WpcEroKitSpecTest`: the five specs load through 073's family; the real fixture maps categories and validity fields; a vanished category is tombstoned.
- `EmscStreamTest`: create/update messages, reconnect gap-fill merge without duplicates; `FdsnTextParserTest` (header line, empty fields, both services).
- `ShakeMapAdapterTest`: detail fetched only on change, contour simplification under the vertex cap, PAGER mapping.
- `InternationalAlertsIngestIT`: CAP Update produces version 2 on the same record; ERO `validFrom` in the future is not yet active.

## Acceptance criteria

- Every CAP-shaped source uses 067's parser and the shared update-chain rule.
- The CAP feed list is a committed, reviewed resource; no runtime register fetch.
- EMSC reconnects never lose events (gap fill) and never duplicate them.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `089: CAP Alerts And International Seismic`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/089-CR-CAP-Alerts-And-International-Seismic.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `089: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
