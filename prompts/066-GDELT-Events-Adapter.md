# 066 — GDELT Events Adapter

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/066-CR-GDELT-Events-Adapter.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/065-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (`eventTime` for discrete events), §3 (rules 8 and 10), §5.3 ("Everything else")
- `docs/Source-Catalog.md` row `gdelt-events`, and "Excluded by charter" (article-title scraping of GDELT URLs)
- Completion Reports of 027 (`embedFields`, `text_hash`), 028 (untrusted upstream text in LLM context) and 040 (gazetteer, FIPS→ISO crosswalk if present)
- ShadowBroker `services/geopolitics.py` (AGPL, **reference only**: read for quirks, copy nothing)
- Upstream: GDELT 2.0 Event Codebook `http://data.gdeltproject.org/documentation/GDELT-Event_Codebook-V2.0.pdf`, CAMEO codebook `http://data.gdeltproject.org/documentation/CAMEO.Manual.1.1b3.pdf`, terms `https://www.gdeltproject.org/about.html`

## Objective

Ingest GDELT 2.0 events as `conflict-event` point records. Each run reads the newest 15-minute export file, keeps CAMEO root codes 13–20 by default, and produces a deterministic text summary for semantic search, without ever fetching the linked articles.

## Tasks

### 1. Descriptor (`ai.foresignal.source.adapters.gdelt.GdeltEventsAdapter`)

| Field | Value |
|---|---|
| id / kinds | `gdelt-events` / `{conflict-event}` |
| params | `rootCodes` (string-list of two-digit codes `01`–`20`, default `13,14,15,16,17,18,19,20`); `minMentions` (integer ≥ 1, default 1); `requireGeo` (boolean, default true); `countries` (string-list of 2-letter FIPS 10-4 codes, optional) |
| interval / budget | 15 min (min 15 min); 4/min, 400/day (2 requests per run) |
| tier / policy | STANDARD, ALL, `recordTtl` 24 h (anchor RECEIVED). Events are never re-observed, so no heartbeats are written. |
| time | UPSTREAM; `observedAt = DATEADDED` (`yyyyMMddHHmmss` UTC); `eventTime = Day` (`yyyyMMdd`, start of the UTC day) |
| text | `embedFields = [summary]`; `titleField summary`; `searchFields summary, actionGeoName, actor1Name, actor2Name`; `rankField numArticles` |
| tiles | `eventRootCode, quadClass, goldstein, numArticles, geoPrecision` |
| licence | "GDELT: unrestricted use and redistribution with citation and a link" (the catalogue said CC BY; corrected, verified 2026-10), `termsUrl` `https://www.gdeltproject.org/about.html`, attribution "The GDELT Project (https://www.gdeltproject.org)" |
| `zeroResultsNormal` | false (every 15-min file contains root 13–20 events) |

### 2. Fetch

1. `getConditional("https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt")`.
   - Always use the `storage.googleapis.com/data.gdeltproject.org/...` form over HTTPS: the `data.gdeltproject.org` CNAME's certificate does not cover the hostname.
   - Verified 2026-10: both hosts serve the same three lines of `<size> <md5> <url>`, for `export`, `mentions` and `gkg`.
2. Take the `export` line. Extract **only the file name** from its URL, which points at `http://data.gdeltproject.org/…` even when read from the mirror.
   - It must match `^\d{14}\.export\.CSV\.zip$`.
   - Rebuild the URL as `https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/{file}`. Never request a URL taken verbatim from the response.
   - Reject a declared size above 50 MB.
3. If `file` equals adapter state `lastFile`, return `FetchResult.unchanged()`.
4. GET the ZIP.
   - The byte length must equal the declared size, and the MD5 must equal the declared hash; otherwise throw `UpstreamFormatException` and leave `lastFile` unchanged.
   - Unzip with `ZipInputStream`: exactly one entry named `{ts}.export.CSV`, uncompressed size capped at 200 MB (counted while streaming, to defeat zip bombs), no directory entries.
5. **Newest file only.**
   - If the file's timestamp is more than 15 min after `lastFile`'s, count the skipped files (`foresignal.gdelt.files_skipped`) and set `Coverage(expected = files due, actual = 1)` so 006's coverage rule sees the gaps.
   - Store `lastFile` after a successful hand-off.

### 3. Parser (`GdeltExportParser`, pure)

- **Tab-delimited** despite the `.CSV` name, with 61 columns per Codebook V2.0. Indices verified 2026-10:
  - `0 GlobalEventID`, `1 Day`, `6 Actor1Name`, `7 Actor1CountryCode`, `16 Actor2Name`, `17 Actor2CountryCode`
  - `25 IsRootEvent`, `26 EventCode`, `27 EventBaseCode`, `28 EventRootCode`, `29 QuadClass`
  - `30 GoldsteinScale`, `31 NumMentions`, `32 NumSources`, `33 NumArticles`, `34 AvgTone`
  - `51 ActionGeo_Type`, `52 ActionGeo_Fullname`, `53 ActionGeo_CountryCode` (FIPS), `56 ActionGeo_Lat`, `57 ActionGeo_Long`, `58 ActionGeo_FeatureID`
  - `59 DATEADDED`, `60 SOURCEURL`
- A row with a column count other than 61 is skipped and counted. More than 5% bad rows throws `UpstreamFormatException` (a format change).
- **Event codes stay strings** (leading zeros).
- **Filters:** root code in `rootCodes`; `NumMentions ≥ minMentions`; when `requireGeo`, a numeric lat/lon and `ActionGeo_Type ≥ 1`; optional `countries`.
- **Record:**
  - `externalId = GlobalEventID`
  - geometry `Point(ActionGeo_Long, ActionGeo_Lat)`, or null when `requireGeo=false` and absent
  - `geoPrecision` from `ActionGeo_Type`: 1 → `country`, 2 and 5 → `adm1`, 3 and 4 → `city`
  - `geometryApproximate: true` for `country` and `adm1` (centroids)
- **Payload:**
  - `eventCode, eventBaseCode, eventRootCode, cameoRoot` (label from a bundled 20-row CAMEO root table, e.g. 18 "Assault", 19 "Fight", 20 "Use unconventional mass violence")
  - `quadClass, goldstein, numMentions, numSources, numArticles, avgTone` (2 dp)
  - `actor1Name, actor1Country, actor2Name, actor2Country, actionGeoName, actionGeoCountry`
  - `actionGeoCountryIso`: ISO 3166-1 alpha-2 via 040's FIPS→ISO crosswalk when present; omit otherwise and note it in the Completion Report
  - `geoPrecision, geometryApproximate, isRoot, sourceUrl`
  - omit empty strings
- **`summary`** is built only from codes and names, never from the article:
  - `"{actor1Name|Unknown actor} — {cameoRoot} — {actor2Name|} in {actionGeoName}"`, collapsed and trimmed to ≤ 200 characters.
  - It is the embedded text (`embedFields`). `sourceUrl` is never fetched, unfurled or scraped by Foresignal (charter). It is passed to LLM features only as an untrusted data field, under 028's rules.

### 4. Fixtures

- Capture (README with the date and file name):
  - `curl -s https://storage.googleapis.com/data.gdeltproject.org/gdeltv2/lastupdate.txt > lastupdate.txt`
  - download the export ZIP it names
  - keep the ZIP for the adapter test, and a TSV trimmed to 40 rows (mixed root codes, one country-level geo, one row with no geo)
- Hand-made:
  - `lastupdate-foreign-host.txt` (the export URL points elsewhere; the parser must use only the validated file name)
  - `bad-md5.txt`
  - `zip-two-entries.zip`
  - `zip-bomb.zip` (a highly compressible 300 MB entry built in the test, not committed)
  - `short-rows.tsv`

### 5. Catalogue and docs

- `docs/Source-Catalog.md` `gdelt-events` row: STANDARD/ALL, TTL 24 h, time base, filters, `embedFields`, the corrected licence, "tab-delimited", newest-file-only with coverage.
- Agent Guide: GDELT events are machine-coded from news, may be duplicated across reports, and do not confirm that an event happened. Prefer corroboration (101).
- OpenAPI: no operation changes.

## Tests to write (do not run)

- `GdeltExportParserTest`:
  - fixture row count after the default filter
  - every mapped index
  - leading-zero codes preserved
  - geoPrecision and approximate flag
  - summary determinism and length cap
  - malformed row counting and the 5% threshold
- `LastUpdateParserTest`: three-line parse, file-name regex, foreign host ignored, oversize declared size rejected.
- `GdeltEventsAdapterTest` (WireMock):
  - same `lastFile` → notModified with no ZIP request
  - MD5 or size mismatch → error, `lastFile` unchanged
  - two-entry ZIP rejected; zip-bomb cap enforced
  - skipped-file coverage computed
  - all requests go to `storage.googleapis.com`
- `GdeltIngestIT` (`@IntegrationTest`): first file → FIRST versions with `eventTime` at the start of `Day`; the same file again → no request beyond `lastupdate.txt`, no versions; records expire after 24 h.
- `GdeltEmbeddingTest` (if 027 is present): only `summary` is queued for embedding.

## Acceptance criteria

- `gdelt-events` registers with valid defaults and never requests a host other than `storage.googleapis.com`.
- No code path fetches `SOURCEURL`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `066: GDELT Events Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/066-CR-GDELT-Events-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `066: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
