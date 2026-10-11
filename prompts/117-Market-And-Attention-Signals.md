# 117 — Market And Attention Signals

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/117-CR-Market-And-Attention-Signals.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/116-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §0, §2 (principles 9 and 13), §4.1 (retention classes), §4.3 (series and `knownAt`), §5.3 (event studies), §14.5 (data sources and their terms)
- `docs/Source-Catalog.md` (series conventions and the excluded list); `docs/Conventions.md` "Analytic retention" and "No credentials during construction"
- Completion Reports of 004/008 (descriptor fields, `egress`), 005 (budgets), 024 (custom sources), 062 (`egress=LOCAL_ONLY` semantics in `EgressPolicy`), 066 (GDELT), 073 (feed kits), 093 (series model: `series-point`, `series-latest`, `/v1/series`), 095 (Wikipedia edit surge)
- Upstreams (verify each endpoint and field name against a captured response before writing the parser):
  - CFTC Public Reporting (Socrata): `https://publicreporting.cftc.gov/resource/{datasetId}.json`, datasets legacy futures `6dca-aqww`, disaggregated futures `72hh-3qpy`, TFF futures `gpe5-46if` (futures-and-options variants `jun7-fc8e`, `kh3c-gbw2`, `yw9f-hn96`); optional Socrata app token header `X-App-Token`
  - FRED API: `https://fred.stlouisfed.org/docs/api/fred/` (`/fred/series`, `/fred/series/observations`) and its terms of use (`https://fred.stlouisfed.org/docs/api/terms_of_use.html`)
  - Wikimedia Pageviews REST: `https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/{project}/all-access/user/{article}/daily/{start}/{end}` and `.../pageviews/top/{project}/all-access/{yyyy}/{mm}/{dd}`; User-Agent policy `https://meta.wikimedia.org/wiki/User-Agent_policy`
  - GDELT DOC 2.0 API: `https://api.gdeltproject.org/api/v2/doc/doc` with `mode=TimelineVolRaw` and `mode=TimelineTone`, `format=json` (limit: one request per 5 s)
  - World Bank Indicators API v2: `https://api.worldbank.org/v2/country/all/indicator/{id}?format=json`

## Objective

Add the market and attention signals the intelligence layer correlates with events: futures positioning from the CFTC Commitments of Traders, rates and prices from FRED, public attention from Wikipedia pageviews, news volume and tone from GDELT timelines, and slow structural covariates from the World Bank. All of them are `series-point` records in the 093 series model, carry the metadata event studies need (family, release lag), respect each provider's terms, and can backfill history. Operator-defined **attention topics** drive the attention sources.

## Principles (binding)

- **Official APIs and their terms only.** CME and ICE exchange data, prediction markets, Metaculus and UKMTO are excluded (Intelligence-Design §16). No scraping.
- **Licensed data stays local.** The whole `fred-series` source is `egress=LOCAL_ONLY`; FRED series whose notes mark them as copyrighted additionally need explicit opt-in.
- **Release lags are data.** Every series states `releaseLagDays`, and the version's ingest time is its `knownAt` (Intelligence-Design §4.3). Never back-date `observedAt` to the period.

## Tasks

### 1. Series metadata (extends 093)

- Extend the `series-latest` payload (and the `/v1/series` catalogue response) with: `family` (price | volume | open-interest | position | rate | attention | tone | count | index | structural), `transform` hint (log-return | log-level | difference | level), `releaseLagDays`, `egress` (from the descriptor), `regionCode` (nullable, 040 code), and `topicId` (nullable).
- Backfill these fields for 093's existing series in a small mapping table in 093's adapters (EIA prices → `price`, `log-return`; AGSI → `index`, `difference`; demand → `count`, `log-level`). EIA spot prices are sourced from LSEG: set those series' `egress` to `LOCAL_ONLY` (record-level `egress` per 062 if available, otherwise the descriptor flag, and note it in the Completion Report).

### 2. `cftc-cot` (POLLING, keyless; optional credential `appToken`)

- Params: `reports` (subset of `disaggregated`, `legacy`, `tff`; default all three), `futuresOnly` (default true), `contracts` (list of CFTC contract market codes; defaults below, each verified against the captured response and corrected in the CR if a code differs):
  WTI crude NYMEX `067651`; natural gas NYMEX `023651`; RBOB `111659`; ULSD `022651`; gold COMEX `088691`; copper COMEX `085692`; wheat CBOT `001602`; corn CBOT `002602`; E-mini S&P 500 `13874A`; 10-year T-note `043602`; US dollar index `098662`; euro FX `099741`.
- Fetch: SoQL `$where=report_date_as_yyyy_mm_dd > '{cursor}' AND cftc_contract_market_code in (…)`, `$order=report_date_as_yyyy_mm_dd`, `$limit=1000`, paging with `$offset`. Cursor per report in `adapter_state`. Interval 6 h (the weekly release is Friday 15:30 ET); budget `perMinute 10`, `perDay 200`.
- Records: one `series-point` per (contract, report, measure, report date). `seriesId = cot.{code}.{report}.{measure}`, `externalId = {seriesId}:{reportDate}`, `eventTime` = report date (the Tuesday "as of" date), `observedAt` = fetch time. Measures (read the column names from the fixture):
  - disaggregated: `oi`, `mm_long`, `mm_short`, `mm_net`, `mm_net_pct_oi`, `pm_net` (producer/merchant), `sd_net` (swap dealers), `or_net` (other reportables), `nr_net` (non-reportables);
  - legacy: `oi`, `nc_net` (non-commercial), `comm_net`, `nr_net`;
  - TFF: `oi`, `dealer_net`, `am_net` (asset manager), `lev_net` (leveraged funds), `or_net`.
  Net = long − short. Also emit `series-latest` per series. Metadata: `family` position (oi: open-interest), `transform` difference, `releaseLagDays` 3.
- **History backfill:** `POST /v1/sources/cftc-cot/backfill {from}` (admin, 202, a job on the 103 framework or a simple tracked job if 103's framework does not fit): pages backwards from the oldest stored report to `from` (default 2010-01-01), budget-limited, resumable, idempotent (existing versions are not duplicated).
- Licence: US government work, public domain. Analytic class `series`.

### 3. `fred-series` (POLLING; required credential `apiKey`; `egress=LOCAL_ONLY`)

- Params: `series` (list of FRED series ids; default `DGS10`, `DGS2`, `T10Y2Y`, `DFF`, `DTWEXBGS`, `DCOILWTICO`, `DHHNGSP`), `allowCopyrighted` (list of ids the operator explicitly accepts for personal use; default empty).
- On enable and weekly, fetch `/fred/series?series_id=` metadata (title, units, frequency, notes, last_updated). A series whose `notes` contain "copyright" or "reprinted with permission" (case-insensitive) is **skipped** with a health warning unless it is in `allowCopyrighted`. Examples known to be copyrighted: `VIXCLS`, `OVXCLS` (Cboe), `DCOILBRENTEU`.
- Observations: `/fred/series/observations?series_id=…&observation_start={last period − 14 d}&file_type=json`, so revisions become new versions. The value `"."` means missing: skip it (no record). Interval 6 h; budget `perMinute 30`, `perDay 1500`.
- `seriesId = fred.{id}`; family from units (percent → rate; dollars per barrel or per MMBtu → price; index → index); `releaseLagDays` 1.
- Attribution on every response that includes these series, and in the Operator Guide: "This product uses the FRED® API but is not endorsed or certified by the Federal Reserve Bank of St. Louis." plus the series' own source line.

### 4. Attention topics (`ai.foresignal.attention`)

- `attention_topic`: `id` (slug `^[a-z0-9-]{3,48}$`), `name`, `description`, `gdelt_query` (≤ 250 chars; validated by a dry character check and, on create, one budgeted test call when `gdelt-timelines` is enabled), `wiki_articles` (≤ 20 `{project, article}`; `project` like `en.wikipedia`), `keywords` (≤ 30, used by 118's linker), `region_codes` (040 codes), `themes`, `enabled`, `created_by`, timestamps.
- Adapters never read the store (Architecture §3), so `AttentionTopicService` **syncs** enabled topics into the `topics` param of `gdelt-timelines` and `wikimedia-pageviews` through `SourceService` (validated by their param schemas) whenever topics change.
- Ship five example topics, disabled: `red-sea-shipping`, `strait-of-hormuz`, `gulf-energy-infrastructure`, `taiwan-strait`, `ukraine-front`.
- API: `GET /v1/attention/topics` (read), `GET /v1/attention/topics/{id}` (read), `POST`, `PUT /{id}`, `DELETE /{id}` (agent). MCP: `list_attention_topics` (read). Problem codes: `UNKNOWN_TOPIC` (404), `TOPIC_INVALID` (400).

### 5. `wikimedia-pageviews` (POLLING, keyless)

- Daily at 02:30 UTC (pageviews for day D are published on D+1). For each topic article: per-article daily for `[lastStored − 3 d, yesterday]` (first run: 90 days), `agent=user`. Requests are serialised; budget `perMinute 60`.
- `seriesId = wiki.{project}.{article}` (article URL-encoded as in the API path), value = views, `family` attention, `transform` log-level, `releaseLagDays` 1, `topicId` set.
- **Top articles:** for each project used by any topic, `pageviews/top/{project}/all-access/{yyyy}/{mm}/{dd}` for yesterday → one record `kind=attention-top`, `externalId = {project}:{date}`, payload: the top 100 `{article, views, rank}`. Analytic class `bulk-event`.
- User-Agent: the standard Foresignal UA (it carries the project URL as contact); if the setting `system.contact` is set, append `; contact={value}`. A 403 with a UA complaint marks the source failing with a clear reason.
- Licence: CC0.

### 6. `gdelt-timelines` (POLLING, keyless)

- For each enabled topic, two requests: `mode=TimelineVolRaw` and `mode=TimelineTone`, `query={gdelt_query}`, `format=json`, `timespan` = since the last stored bucket (max `3d`; first run `7d`), `timelinesmooth=0`. **Space requests at least 5 s apart** (budget `perMinute 10` plus a per-source minimum interval in the adapter); interval 30 min.
- Records: `seriesId = gdelt.{topicId}.vol` (value = matching article count per bucket; `extra.total` = all articles in that bucket; `extra.share` = count ÷ total), `gdelt.{topicId}.tone` (average tone). Bucket = the API's timeline resolution (record it; typically 15 min for short spans). `family` attention or tone; `releaseLagDays` 0.
- Licence: GDELT open terms with citation and link; add the attribution string.

### 7. `worldbank-indicators` (POLLING, keyless)

- Params: `indicators` (default `SP.POP.TOTL`, `NY.GDP.PCAP.KD`, `SP.DYN.IMRT.IN`, `SP.URB.TOTL.IN.ZS`, `NE.TRD.GNFS.ZS`, `EG.ELC.ACCS.ZS`), `fromYear` (2000).
- Weekly; `per_page=20000`, paging per indicator. Skip aggregates (regions and income groups) by the `region.id` metadata; map ISO3 to the 040 ISO2 region code.
- `seriesId = wb.{indicator}.{iso2}`, annual periods, `family` structural, `regionCode` set, `releaseLagDays` 180. Licence per indicator source (most CC BY 4.0; record `sourceOrganization`).

### 8. Optional `ice-cot`

Investigate whether ICE Futures Europe publishes its weekly Commitments of Traders (Brent, Gasoil) as a stable, official, machine-readable file whose terms allow personal use. If yes, implement it like `cftc-cot` (`seriesId = icecot.{contract}.{measure}`). If not, do not implement it; record the finding in the CR and in `docs/Source-Catalog.md`'s excluded list. This is not a reason for `Partial`.

### 9. Descriptors, retention, docs

- All series sources: analytic class `series` (Conventions), STANDARD tier, `embedFields` none, `kindDisplay` for `series-point` reused from 093.
- `docs/Source-Catalog.md`: rows for `cftc-cot`, `fred-series`, `wikimedia-pageviews`, `gdelt-timelines`, `worldbank-indicators` (and `ice-cot` or its exclusion), with licence, egress, cadence and release lag; add CME, ICE end-of-day, UKMTO and Metaculus to the excluded list with reasons.
- `docs/OpenAPI.json`: `AttentionTopic`, `AttentionTopicInput`; the `SeriesCatalogEntry` additions; the backfill operation; tag `Attention`; `x-foresignal-scope` on every operation. Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`) with BODIES for create topic and backfill.
- AccessPolicy rows as listed.
- `docs/Operator-Guide.md` "Markets and attention": what each source gives and its lag, the FRED key and notice, copyrighted series and `LOCAL_ONLY`, topics and how to write a GDELT query, the backfill command, and the explicit limit "daily and weekly data cannot show intraday trading".

## Tests to write (do not run)

- `CotParserTest` (fixtures for each report): net computation, `mm_net_pct_oi`, missing columns tolerated, contract filter, cursor advance.
- `CotAdapterTest` (WireMock): SoQL query built correctly, paging with `$offset`, app-token header when set, no duplicate versions on re-poll.
- `CotBackfillIT`: pages backwards to `from`, resumes after interruption, idempotent.
- `FredAdapterTest` (WireMock): metadata copyright detection skips `VIXCLS` unless allowed; `"."` skipped; revision window creates a new version for a revised value; source is `LOCAL_ONLY`; attribution present.
- `AttentionTopicServiceIT`: CRUD, validation, sync into both sources' `topics` params, disabled topics excluded.
- `WikimediaPageviewsAdapterTest` (WireMock): per-article URL encoding, `agent=user`, first-run 90 days, top-articles record, UA with and without `system.contact`, 403 handling.
- `GdeltTimelineAdapterTest` (WireMock): both modes per topic, ≥ 5 s spacing enforced (fake clock), share computed, timespan capped at 3 d.
- `WorldBankAdapterTest`: aggregates skipped, ISO3 → ISO2, paging.
- `SeriesMetadataTest`: `family`, `transform`, `releaseLagDays`, `egress` present in `/v1/series` for 093 and 117 series.

## Acceptance criteria

- COT, FRED (with a key), pageviews, GDELT timelines and World Bank indicators produce `series-point` and `series-latest` records with family, transform, release lag and egress metadata.
- FRED raw values never leave through non-local egress paths; copyrighted series need explicit opt-in.
- Attention topics drive the attention sources without adapters touching the store.
- COT history can be backfilled, resumably and idempotently.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `117: Market And Attention Signals`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/117-CR-Market-And-Attention-Signals.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `117: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
