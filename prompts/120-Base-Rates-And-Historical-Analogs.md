# 120 — Base Rates And Historical Analogs

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/120-CR-Base-Rates-And-Historical-Analogs.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/119-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles 3, 6, 13), §7 (base rates, reference imports, analogs), §14.3
- Completion Reports of 027 (embeddings, `embedding_document`), 040 (regions, region classes), 081 (ACLED and UCDP adapters and their terms), 089 (international seismic), 067 (GDACS), 103 (job framework, Wilson interval), 116 (COT backfill), 117 (situations, snapshots), 118 (signal families, region rollups, findings), 119 (reference families in candidate generation)
- Upstreams (verify formats and current URLs): UCDP GED downloads `https://ucdp.uu.se/downloads/` (CC BY 4.0) and the UCDP API; ACLED API (operator credentials, terms forbid redistribution); USGS FDSN event service `https://earthquake.usgs.gov/fdsnws/event/1/` (public domain); GDACS event list API (attribution)

## Objective

Give every probability Elaro produces an **outside view**. Build a library of base rates computed from Elaro's own history and from long **reference-history imports** (decades of conflict, seismic and disaster data in compact weekly form), so base rates exist on day one. Add **historical analogs**: past situations that looked like the current one, with what happened next.

## Principles (binding)

- Base rates state their reference class, period, sample size, interval and sources. Thin samples are labelled, never hidden.
- Reference imports are stored as counts only (no raw rows), keep their licence, and follow their egress terms (`LOCAL_ONLY` for ACLED).
- Analogs never overlap the current situation in time and always report how many were found.

## Tasks

### 1. Reference history (`ai.elaro.baserate.reference`)

- `reference_history`: `family` (118 signal family plus a subtype, e.g. `conflict:battles`, `conflict:explosions-remote`, `conflict:violence-civilians`, `conflict:protests`, `seismic:m5`, `disaster:gdacs-orange+`), `region_code` (040), `week_start` (ISO week, Monday UTC), `count`, `fatalities` (nullable), `source`, `licence`, `egress`, `import_id`. PK `(family, region_code, week_start, source)`.
- `reference_import`: `id`, `dataset`, `params`, `file_sha256` (file imports), `rows_read`, `rows_written`, `period_from`, `period_to`, `status`, `started_at`, `finished_at`, `error`, `created_by`.
- Importers (admin; each a resumable 103-framework job, idempotent per `(dataset, period)`):
  - **UCDP GED:** from an operator-downloaded CSV under `${ELARO_DATA_DIR}/imports` (validated like `INBOX_PATH`), or the UCDP API when it is reachable within budget. Map `type_of_violence` 1/2/3 to families; region from `country_id`/ISO code (and `adm_1` when it matches a 040 admin1). Licence `CC-BY-4.0`.
  - **ACLED history:** through the 081 ACLED client with the operator's credentials, by country and year range, within the 081 budget; event types mapped to the families above. `egress=LOCAL_ONLY`; attribution per ACLED terms. Skipped with a clear status if credentials are absent.
  - **USGS ComCat:** FDSN `query?format=csv&minmagnitude=4.5&starttime&endtime` per year, paginated by time; families `seismic:m4.5`, `seismic:m5`, `seismic:m6`; region from 040 point-in-polygon (offshore → nearest country within 200 km, else `ZZ`).
  - **GDACS:** the GDACS event list API by year, alert levels orange and red per hazard type.
  - **Own history:** a nightly job folds Elaro's own region-day rollups (118) for the same families into `reference_history` with `source=elaro`, so the reference set keeps growing.
- `POST /v1/reference-imports {dataset, params}` (admin, 202), `GET /v1/reference-imports` and `/{id}` (admin), `DELETE /v1/reference-imports/{id}` (admin; removes its rows).

### 2. Base-rate library (`ai.elaro.baserate`)

- `base_rate`: `id`, `definition` jsonb (`{family, regionCodes | regionClass, window: P7D|P28D|P90D, condition?: {family, withinDays}}`), `rate_per_window`, `p_at_least_one` (fraction of windows with ≥ 1 event), Jeffreys 95% interval, `n_windows`, `n_events`, `period_from`, `period_to`, `source_mix` jsonb (`{elaro: n, ucdp: n, …}`), `caveats` (`SMALL_SAMPLE` when n_windows < 30, `LOW_REPORTING_DENSITY` when the region's overall reporting density is in the bottom decile, `MIXED_SOURCES`), `computer` (`baserate` 1.0.0), `computed_at`, `origin` (auto | operator | agent).
- **Computation:** from `reference_history` (and own rollups through the fold), choosing per family the source with the longest consistent coverage and never double-counting the same events across sources (priority: ACLED > UCDP for conflict where both exist for a region and period; USGS for seismic). Windows are non-overlapping and aligned to weeks.
- **Conditional rates:** `condition {family, withinDays}` gives `P(consequent within window | antecedent family occurred)`, computed over antecedent occurrences, with the unconditional rate alongside (this is the precursor-test logic of 103 applied to reference data).
- **Reporting density:** per region, the ratio of its total weekly event count across all text-based sources to the median of countries with similar population (World Bank, 116). Stored on `region_profile(region_code, reporting_density, density_decile, computed_at)` and used for `LOW_REPORTING_DENSITY`.
- **Auto rates:** monthly (125's monthly cycle, or `POST /v1/base-rates/refresh`), for every family × region with ≥ 52 weeks of coverage, windows P7D and P28D.
- API: `GET /v1/base-rates?family&region&window&cursor` (read), `GET /v1/base-rates/{id}` (read), `POST /v1/base-rates/query` (read; compute an ad-hoc definition without storing, cached 24 h), `POST /v1/base-rates` (agent; store a custom definition), `DELETE /v1/base-rates/{id}` (agent; custom only).
- MCP: `get_base_rate` (read; takes a definition, returns the stored or computed rate with interval, n and caveats). Add to the read catalogue.

### 3. Analogs (`ai.elaro.baserate.analog`)

- **Signature** (`situation-signature` 1.0.0), computed when a situation becomes active and on each daily snapshot for its first 30 days: counts per signal family over the first N days (N = min(7, age)), standardised by the family's global weekly SD; escalation slope (members per day, log-linear fit); theme one-hot; region class (040 continent and income group from 116); entity class counts (098); severity path. Stored in `situation_signature(situation_id, day_n, features float[], embedding_ref, computed_at)`.
- **Outcomes** for closed or dormant situations (and any situation older than 30 days): duration, peak severity, follow-on families within 7 and 30 days (from 118 region rollups), whether a related hypothesis set resolved and how. Stored on `situation_outcome`.
- **Search:** feature cosine (top 50) and embedding cosine of the deterministic summary (027, top 50), fused by reciprocal rank; exclude situations overlapping in time and the same situation's merges; return top k (10) with similarity, outcomes, and the count found. Without embeddings, feature similarity only, labelled.
- **Region-period analogs** (no situation needed): signatures over a region's last 14 days of `reference_history` and rollups, searched against the same region class's history, for questions that are not tied to a situation.
- **Analog outcome distribution:** for a target outcome (a family within W days, or a situation reaching critical), the frequency among analogs, shrunk toward the base rate by `k ÷ (k + 10)`, with the analog count and Jeffreys interval.
- API: `GET /v1/situations/{id}/analogs?k&outcome` (read), `POST /v1/analogs/query {regionCode, from, to, k, outcome}` (read).
- MCP: `find_analogs` (read; read catalogue).

### 4. Spec and docs

- `docs/OpenAPI.json`: `ReferenceImport`, `ReferenceImportRequest`, `BaseRate`, `BaseRateDefinition`, `RegionProfile`, `Analog`, `AnalogResult`; tag `Analysis`; Postman BODIES for an import, a base-rate query and an analog query. AccessPolicy rows. Problem codes: `UNKNOWN_BASE_RATE`, `UNKNOWN_IMPORT` (404), `IMPORT_DATASET_UNAVAILABLE` (409), `BASE_RATE_INVALID` (400).
- `docs/Operator-Guide.md` "Base rates and analogs": why the outside view matters, which datasets to import first (UCDP GED, USGS), download steps and licences, ACLED terms, reading intervals and caveats.
- `docs/Source-Catalog.md`: the reference datasets section with licences and egress.

## Tests to write (do not run)

- `UcdpImporterTest` (small CSV fixture): family mapping, week bucketing, region mapping, idempotent re-import, licence recorded.
- `UsgsComCatImporterTest` (WireMock): yearly paging, magnitude families, offshore region rule.
- `AcledHistoryImporterTest`: skipped without credentials; `LOCAL_ONLY` rows.
- `BaseRateCalculatorTest`: rate, `p_at_least_one`, Jeffreys interval against reference values; no double counting across sources; `SMALL_SAMPLE`; conditional rate on a planted dependency.
- `ReportingDensityTest`: decile assignment and the caveat.
- `SignatureAndAnalogIT`: signatures computed on activation and daily; analogs exclude time overlap; fusion ranking; outcome distribution shrinkage; feature-only fallback without embeddings.
- `BaseRateControllersIT`: endpoints, scopes, caching of ad-hoc queries, MCP shapes.

## Acceptance criteria

- After importing UCDP GED and USGS ComCat, base rates for conflict and seismic families exist per region with intervals and caveats, before Elaro has much history of its own.
- Every base rate states its reference class, period, n, interval and sources.
- Analog search returns past situations with outcomes and an outcome distribution shrunk toward the base rate.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `120: Base Rates And Historical Analogs`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/120-CR-Base-Rates-And-Historical-Analogs.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `120: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
