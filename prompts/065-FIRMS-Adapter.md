# 065 — FIRMS Adapter

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/065-CR-FIRMS-Adapter.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/064-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (row "FIRMS": `eventTime` = `observedAt` = acquisition time), §3 (rules 8 and 10), §5.3 ("Everything else")
- `docs/Source-Catalog.md` row `nasa-firms`
- `docs/Weather-Imagery-Design.md` §2.4 retention table (`fire-detection` 7 d) and the `goes-fdc` row (048 uses the same kind)
- GEV (MIT, portable): `server/providers/firms.js` (sequential sources, `world/2` plus a trailing-24 h clamp, keyless means no request), `src/data/firmsCsv.js` (header-driven columns, unpadded `acq_time`, `isLikelyCsv`, VIIRS vs MODIS confidence), `src/data/firmsAdapt.js`
- Upstream: `https://firms.modaps.eosdis.nasa.gov/api/area/`, attributes `https://www.earthdata.nasa.gov/data/tools/firms/active-fire-data-attributes-modis-viirs`

## Objective

Ingest NASA FIRMS active-fire detections from the VIIRS (NOAA-20, NOAA-21, Suomi-NPP) and MODIS near-real-time products as `fire-detection` point records. Fetch incrementally so a 30-minute poll does not re-process the whole day, and keep the MAP_KEY out of every log and trace.

## Tasks

### 1. Descriptor (`ai.foresignal.source.adapters.firms.FirmsAdapter`)

| Field | Value |
|---|---|
| id / kinds | `nasa-firms` / `{fire-detection}`. The catalogue said `fire`; use the kind shared with 048 `goes-fdc` so rules and retention cover both. |
| credentials | `mapKey` (required). Keyless means disabled: no request is ever made without it. |
| params | `sources` (string-list, allowed `VIIRS_NOAA20_NRT, VIIRS_NOAA21_NRT, VIIRS_SNPP_NRT, MODIS_NRT, LANDSAT_NRT`; default the first four). `area` (bbox, default world). `window` (duration, 1–48 h, default 24 h). |
| interval / budget | 30 min (min 10 min); 6/min, 600/day; optional `mbPerDay` 1500 (005) |
| tier / policy | STANDARD, ALL, `recordTtl` 24 h, `ttlAnchor` **OBSERVED**, so a detection stays current for 24 h after acquisition whatever the poll cadence. Default keyframe applies (`ttl − poll − 30 s`). |
| time | `observedAtMode` UPSTREAM; `observedAt = eventTime = acq_date + acq_time` (UTC; `acq_time` is unpadded `HHMM`, so `"45"` means 00:45) |
| tiles / rank / title | `frp, confidenceClass, satellite, daynight`; `rankField frp`; `titleField` none (the kind display title "Fire {frp} MW") |
| retention | `kindRetention fire-detection` 7 d (matches the design table) |
| licence | "NASA open data (cite NASA LANCE FIRMS)", `termsUrl` `https://www.earthdata.nasa.gov/data/tools/firms`, attribution "NASA FIRMS" |
| `zeroResultsNormal` | false for the world area; the `area` param description explains that a small bbox may need `true`, which the operator can set through settings if 013 exposes it (otherwise record this as an open item) |

### 2. Fetch (sequential, never parallel)

- For each configured source, in order: GET `https://firms.modaps.eosdis.nasa.gov/api/area/csv/{mapKey}/{SOURCE}/{area}/1`.
  - `area` is `world` or `west,south,east,north`. Verified 2026-10: DAY_RANGE accepts 1–5, and NOAA21 has no `_SP` product.
  - Between 00:00 and 06:00 UTC, also fetch the previous day: `/1/{yyyy-MM-dd}` for yesterday. Late NRT granules for yesterday arrive then, and day range 1 alone would miss them.
- **Incremental emit:** adapter state `hw:{SOURCE}` holds the newest acquisition instant already emitted.
  - Emit rows with acquisition ≥ `hw − 3 h` (the overlap absorbs out-of-order granules) and within `window` of now.
  - Advance `hw` after the batch is handed off.
  - Rows inside the overlap re-emit identical records. They are TOUCH or TOUCH_THROTTLED at worst, so there are no new versions.
- **Credential safety:** the MAP_KEY is a path segment. Confirm 005's redaction replaces it in `FetchTrace`, logs, probe exchanges and exception messages. Add a test.
- **Validation (port `isLikelyCsv`):**
  - The first non-whitespace character must not be `<`, and the header must contain `latitude, longitude, acq_date, acq_time`.
  - A body containing `Invalid MAP_KEY` (case-insensitive) throws `CredentialsRejectedException`.
  - Any other non-CSV 200 throws `UpstreamFormatException` with a ≤ 500-character snippet, after redacting the key.
  - A header-only CSV is zero rows and valid.
- **Quota:** 5,000 transactions per 10 min per key. Verified 2026-10: larger requests may count as several transactions, and how a world request is counted is undocumented. The `mapkey_status` endpoint could not be verified (it returned 403 to a dummy key), so do not call it. Rely on the budget and on 429/`Retry-After` (005).
- Honour `fetchTimeout` 120 s and `maxResponseBytes` 64 MB (world VIIRS days can be large).

### 3. Parser (`FirmsCsvParser`, pure; port of `firmsCsv.js`)

- Columns are located **by header name**, never by position (the published order and the live order are not confirmed to match).
  - VIIRS: `bright_ti4, bright_ti5`, confidence `l|n|h`.
  - MODIS: `brightness, bright_t31`, confidence 0–100, optional `type`.
- `externalId` = `{SOURCE}:{acq_date}:{acq_time padded to 4}:{lat 4dp}:{lon 4dp}`. Geometry `Point(longitude, latitude)`; skip rows with invalid coordinates and count them.
- Payload:
  - `source`, `satellite`, `instrument` (derived from SOURCE: `VIIRS|MODIS|OLI`, since the attribute page lists no `instrument` column)
  - `frp` (MW), `brightK`, `bright2K`, `scanKm`, `trackKm`, `daynight`, `version`
  - `confidence` (raw: `l|n|h` or an integer)
  - `confidenceClass` (`low|nominal|high`: VIIRS letter map; MODIS < 30 low, 30–79 nominal, ≥ 80 high)
  - `type` (MODIS only, when present)
- No cross-satellite dedup: the satellites are independent observations. Clustering belongs to derived layers.

### 4. Fixtures

- Capture (README with the date and the command, key replaced by `KEY` in the README):
  - `curl -s "https://firms.modaps.eosdis.nasa.gov/api/area/csv/$KEY/VIIRS_NOAA20_NRT/-125,24,-66,50/1"` → `viirs.csv`, trimmed to 50 rows
  - the same for `MODIS_NRT` → `modis.csv`
- Hand-made: `invalid-key.txt`, `html-error.html`, `header-only.csv`, `unpadded-times.csv` (`acq_time` 5, 45, 1305), `bad-coords.csv`.

### 5. Catalogue and docs

- `docs/Source-Catalog.md` `nasa-firms` row:
  - kind `fire-detection`, STANDARD/ALL, TTL 24 h with anchor OBSERVED, time base, licence
  - incremental fetch and the yesterday catch-up
  - verified 2026-10: DAY_RANGE 1–5, source list, transaction counting undocumented
- Operator Guide: obtaining a MAP_KEY; setting `area` to reduce bytes.
- OpenAPI: no operation changes. Add a `fire-detection` payload example to the kind docs if 014's kind schema docs exist.
- `THIRD_PARTY_NOTICES.md`: GEV entries for `firmsCsv.js` and `firms.js` ports, with origin headers.

## Tests to write (do not run)

- `FirmsCsvParserTest`:
  - VIIRS and MODIS fixtures; columns found by name in a shuffled header
  - unpadded times; confidence classes at the boundaries (29/30/79/80)
  - `externalId` stability; header-only; bad coordinates skipped
- `FirmsAdapterTest` (WireMock):
  - sources fetched sequentially in configured order; area path segment
  - the yesterday URL only between 00:00 and 06:00 UTC (`MutableClock`)
  - incremental high-water with a 3 h overlap; `window` filter
  - `Invalid MAP_KEY` → `CredentialsRejectedException`; HTML → `UpstreamFormatException`
  - the key never appears in the `FetchTrace` URL, exception messages or logs
  - no request without a key (enable is refused)
- `FirmsIngestIT` (`@IntegrationTest`):
  - first fetch → FIRST versions
  - the overlap re-emit → no versions
  - a detection expires 24 h after acquisition (`valid_until = observed_at + 24h`)
  - `fire-detection` retention resolves to 7 d

## Acceptance criteria

- `nasa-firms` registers with valid defaults and cannot be enabled without `mapKey`.
- Per-source requests are strictly sequential, and the MAP_KEY is redacted everywhere.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `065: FIRMS Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/065-CR-FIRMS-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `065: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
