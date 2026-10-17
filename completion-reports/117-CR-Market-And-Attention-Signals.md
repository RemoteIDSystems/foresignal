# 117-CR-Market-And-Attention-Signals

| | |
|---|---|
| Prompt | 117-Market-And-Attention-Signals.md |
| Status | Complete (FRED and GDELT parsers verified against documented shapes only, not live captures; owner decisions under Issues) |
| Implementation commit | `3698059ff7f3db1b2a4cddfd7f78770c589cc680` |
| Date | 2026-10-16 |

## Summary

Market and attention signals as `series-point` / `series-latest` records of the 093 series model, with the metadata
event studies need, operator-defined attention topics that drive the attention sources, and a resumable COT history
backfill. Compiles; tests written, not run.

- **Series metadata (task 1):** `source/series/SeriesMeta` (`family` of the ten spec values, `transform`
  `log-return|log-level|difference|level`, `releaseLagDays`, `egress`, `regionCode`, `topicId`; validated) is applied
  to every `series-latest` through a new `SeriesPoint.latestPerSeries(points, metadata)` overload.
  `EnergySeriesMetadata` is the 093 mapping table (EIA spot `price`/`log-return`, EIA stocks `volume`/`difference`,
  AGSI `index`/`difference`, demand, generation and fuel mix `count`/`log-level`, EIA-930 interchange `count`/`level`,
  ERCOT prices `price`/`log-return`, carbon intensity `index`/`level`); the six 093 adapters pass it, and the catalog
  falls back to it for a `series-latest` written before 117. `SeriesInfoDto` and `SeriesQueryService.info` add
  `family`, `transform`, `releaseLagDays`, `egress` (the value in force from `EgressPolicy.isLocalOnly`),
  `regionCode`, `topicId` and `attribution` (payload, else the descriptor's). Frequencies gain `P3M` and `P1Y`;
  `SeriesPeriods` reads a bare year. `SeriesPoint.withPayload` adds one payload entry.
- **`cftc-cot` (`source/adapters/cftc`):** `CotReport` (datasets and measures with column alternates read from the
  captures), `CotParser` (net = long − short, `mm_net_pct_oi`, a missing column drops only its measures),
  `CftcCotAdapter`: SoQL `$where report_date > '{cursor}' AND cftc_contract_market_code in (…)`,
  `$order=report_date_as_yyyy_mm_dd,id`, `$limit=1000`, `$offset` paging (at most 3 pages per report and run),
  cursor per report, dataset variant and contract set; first run one year; optional `appToken` as `X-App-Token`;
  6 h, 10/min, 200/day; metadata position or open-interest, difference, lag 3 d. Every default contract code was
  verified in the live 2026-10-13 report (none differed), and the three futures-and-options dataset ids were confirmed.
- **COT backfill:** `POST /v1/sources/cftc-cot/backfill` (`api/CotBackfillController`, admin, 202) →
  `markets/CotBackfillService` + `history_backfill_job` (`BackfillJobEntity`): the request goes into the source's
  adapter state; each run of the source reads up to 5 older pages (`< bound AND >= from`, newest first) within its
  budget (a budget refusal ends the run's backfill quietly); the bound commits with the run's records, so it resumes
  after an interruption and re-reading is idempotent; a tick every two minutes (`foresignal.backfill.scheduled`,
  off in tests) copies progress and asks for the next manual fetch. Default from 2010-01-01; 409 `SOURCE_DISABLED`
  and `BACKFILL_RUNNING`.
- **`fred-series` (`source/adapters/fred`):** `FredSeriesAdapter` + `FredParser`: `egress=LOCAL_ONLY`, required
  `apiKey`; metadata (`/fred/series`, `/fred/series/release`, `/fred/release/sources`) on first sight and weekly;
  notes containing "copyright" or "reprinted with permission" skip the series with a `degraded` warning unless in
  `allowCopyrighted`; observations from last period − 14 d (first run 365 d), `"."` skipped; family from units;
  lag 1 d; each `series-latest` carries the FRED notice plus the series' source line; a 400 naming `api_key` is a
  rejected credential.
- **Attention topics (`ai.foresignal.attention`):** `attention_topic` (`AttentionTopicEntity`), `AttentionTopicService`
  (CRUD, validation → `TOPIC_INVALID`, `UNKNOWN_ATTENTION_TOPIC`, `ATTENTION_TOPIC_EXISTS`, the dry GDELT query check
  and, on create or query change while `gdelt-timelines` is enabled, one budgeted test call via the new
  `ProbeService.probeCall` and `GdeltTimelineAdapter.testQuery`), sync of enabled topics into both sources' `topics`
  param through `SourceService.update` (new `SourceService.storedParams` keeps their other params), five examples
  seeded disabled once (`attention.seeded`). `api/AttentionController` (5 operations), MCP `list_attention_topics`
  (read). Shared param codec `source/formats/attention/TopicParams` (validated in both adapters' `checkParams`).
- **`wikimedia-pageviews` (`source/adapters/wikimedia`):** once a UTC day after 02:30 (hourly poll), per-article
  daily `[last − 3 d, yesterday]` (first run 90 days), `agent=user`, serial, 404 skipped; `attention-top` per project
  (top 100, `bulk-event` 90 d via `kindRetention`); a 403 sets the failing reason naming the User-Agent policy.
- **`gdelt-timelines` (`source/adapters/gdelt`):** `TimelineVolRaw` and `TimelineTone` per topic, `timelinesmooth=0`,
  timespan since the last bucket + 15 min (cap `3d`, first `7d`), `RequestSpacing` keeps ≥ 5 s between request starts
  (fake-clock testable), at most 5 topics per run round-robin; `vol` with `extra.total` and `extra.share`, `tone`
  (zero-count steps have no tone), resolution recorded in `extra.resolution`.
- **`worldbank-indicators` (`source/adapters/worldbank`):** country list (aggregates skipped by `region.id = NA`,
  ISO3 → ISO2), indicator metadata (`sourceOrganization`), `per_page=20000` paging; `wb.{indicator}.{iso2}`, `P1Y`,
  structural, lag 180 d, `regionCode`; unknown indicators skipped with a warning.
- **ICE COT:** investigated, not built (see Deviations/Issues).
- **Spec, docs, wiring:** the six operations `implemented` and removed from `pending-operations.txt`; changelog 117;
  Problem codes; Postman regenerated with bodies for create/update topic and backfill; `AccessPolicy` rows;
  `intel/originators.json` (originators `cftc`, `fred`, `world-bank`; source mappings for all five);
  `docs/Source-Catalog.md` 117 section replaced; `docs/Operator-Guide.md` "Markets and attention (prompt 117)";
  `docs/Agent-Guide.md` tool row; `docs/Invariants.md` EGR-2 test names.

## Files created / modified

**Created (main):** `source/series/{SeriesMeta,EnergySeriesMetadata}`, `source/adapters/cftc/{CotReport,CotParser,
CftcCotAdapter,package-info}`, `source/adapters/fred/{FredParser,FredSeriesAdapter,package-info}`,
`source/adapters/gdelt/{GdeltTimelineAdapter,GdeltTimelineParser,RequestSpacing}`,
`source/adapters/wikimedia/{PageviewsParser,WikimediaPageviewsAdapter,package-info}`,
`source/adapters/worldbank/{WorldBankParser,WorldBankIndicatorsAdapter,package-info}`,
`source/formats/attention/{TopicParams,package-info}`, `attention/{AttentionTopicEntity,AttentionTopicRepository,
AttentionTopicInput,AttentionTopicService,AttentionTopicExamples,package-info}`, `markets/{BackfillJobEntity,
BackfillJobRepository,CotBackfillService,package-info}`, `api/{AttentionController,CotBackfillController}`,
`mcp/tools/ListAttentionTopicsTool`.

**Modified (main):** `source/series/{SeriesPoint,SeriesPeriods}`, `source/adapters/{carbon/CarbonIntensityAdapter,
eia/Eia930Adapter,eia/EiaPetroleumAdapter,ember/EmberAdapter,ercot/ErcotAdapter,gie/AgsiAdapter,gdelt/package-info}`,
`api/{SeriesQueryService,dto/SeriesInfoDto}`, `api/error/ErrorCodes`, `collect/ProbeService`, `source/SourceService`,
`security/AccessPolicy`, `settings/SettingKeys`, `resources/application.yml`, `resources/intel/originators.json`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`docs/Invariants.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−6), `src/test/resources/application-test.yml`.

**Tests / fixtures:** see the table; modified `retention/FamilyDefaultsTest` (five adapters) and
`security/restrict/RestrictedReadCoverageTest` (`list_attention_topics` exempt: topics are not records); fixtures
`cftc-cot/`, `fred-series/`, `gdelt-timelines/`, `wikimedia-pageviews/`, `worldbank-indicators/` (each with a README).

## Deviations from the prompt

1. **`system.contact` is the existing `server.contactEmail`.** No `system.contact` setting exists; `InstallIdService`
   already appends `; contact={server.contactEmail}` (`PUT /v1/settings/server`, spec `ServerSettings.contactEmail`,
   047) to every upstream User-Agent, so `wikimedia-pageviews` uses that. The UA test sets `server.contactEmail`.
2. **EIA spot prices `LOCAL_ONLY`:** already the descriptor egress of the whole `eia-petroleum` source since 093
   (record-level egress does not exist in 062's `EgressPolicy`); stocks are therefore `LOCAL_ONLY` too. Unchanged.
3. **Backfill is a simple tracked job**, not a 103 job: the 103 framework runs analyses and writes no records, while
   the backfill must go through the ingest pipeline. The source's own runs read the history (adapter state carries
   the request and bound). `versionsWritten` is the growth of the source's `series-point` heads since the job
   started (new points), not a count of every version.
4. **SoQL order `report_date_as_yyyy_mm_dd,id`** (the prompt names only the date) so `$offset` paging is stable within
   one report date; the backfill moves its date bound instead of using `$offset` (re-reading the oldest report of a
   full page, which is idempotent). When the incremental read stops at its page cap, the cursor moves only to the day
   before the newest report read.
5. **Spec reconciliations (additive):** `SeriesPage.items` and `SeriesPointPage.series` are `SeriesCatalogEntry`;
   `SeriesCatalogEntry.attribution` (nullable) added so every series response carries the FRED notice;
   `SeriesInfo.frequency` gains `P3M`, `P1Y`; Problem codes `UNKNOWN_ATTENTION_TOPIC`, `TOPIC_INVALID`,
   `ATTENTION_TOPIC_EXISTS` (new; the spec's 409 had no code), `BACKFILL_RUNNING` (new); changelog entry.
6. **Topics param form:** `topics` is a `STRING_LIST` whose entries are compact JSON objects (`ParamType` has no
   object type); both adapters validate them in `checkParams`.
7. **GDELT pacing:** the adapter's own `RequestSpacing` (5 s) plus the 10/min budget; no descriptor `hostPacing`
   (its real-time wait would slow every test). A run covers at most five topics, the rest round-robin.
8. **GDELT test call** is made on create and when an update changes the query; a test call the budget refuses is
   skipped and the topic accepted on the dry check alone (the spec has no 429 for these operations).
9. **Wikimedia "daily at 02:30 UTC"** is an hourly poll that works once per UTC day after 02:30.
10. **FRED source line** comes from `/fred/series/release` + `/fred/release/sources` (two extra requests per series,
    weekly); FRED's `/fred/series` has no source field.
11. **`AttentionTopicInput.social`** is accepted and not stored (135 owns it); topic responses return `social: null`.
12. **`wikimedia-pageviews` is classified `bulk-event` in `FamilyDefaultsTest`** (the class of `attention-top`, 90 d);
    its series kinds keep the 3650 d series window. The other four are `series`.
13. **Series ids follow the prompt** (`cot.067651.disaggregated.mm_net`); Intelligence-Design §4.3's example
    `series:cftc-cot/067651.disagg.mm_net` differs.
14. **Docs** extended (Operator Guide section appended; the Source-Catalog 117 section replaced whole) rather than
    whole-document rewrites, as earlier prompts did.

## Issues found / open questions

- **Owner decisions:** (a) the Intelligence-Design §4.3 `SeriesRef` example vs the prompt's series ids (Deviation 13);
  which should change? (b) Deviation 14 vs `~/Documents/GitLab/CLAUDE.md` "documents are replaced whole";
  (c) whether a topic should be refused when the GDELT test call cannot be made (Deviation 8); (d) the 093 mapping
  table's family/transform/lag choices are judgments (for example ERCOT prices can be negative, where `log-return`
  is undefined).
- **GDELT unverified live:** every request from the build host (2026-10-16/17, with Foresignal's User-Agent and
  minutes apart) answered HTTP 429 "Please limit requests to one every 5 seconds…". The parser follows the DOC 2.0
  documentation (`norm` on `TimelineVolRaw`, step resolution rules); the timeline JSON envelope
  (`timeline[0].data[{date, value, norm}]`, `query_details.date_resolution`) is from knowledge of the API and must be
  confirmed. Recapture at 143.
- **FRED unverified live:** no key during construction; synthetic fixtures from the documentation examples, except the
  real 400 body for a missing key. Recapture at 143.
- **ICE COT:** `https://www.ice.com/publicdocs/futures/COTHist2026.csv` is a stable, official CSV (Brent, gasoil and
  others, latest 2026-10-13), but ice.com's terms grant personal, non-commercial use and exclude "data mining, robots
  or similar data gathering or extraction methods", so it was not built (an EDP provider idea).
- **`SeriesCatalogEntry` requires `family`, `transform`, `releaseLagDays`, `egress`:** every built-in series now has
  them; an EDP-bound series (138) must supply them or the catalog entry will carry nulls.
- **Contract change:** a changed `contracts` list or `futuresOnly` starts new cursors (one year re-read, idempotent);
  the backfill bound is per contract set too.
- New tables via Hibernate `ddl-auto=update`: `attention_topic`, `history_backfill_job`.
- US English guard (`~/Documents/GitLab/meshdtn/style/test_us_english.py`): ran twice, 14 tests OK. `cancelled`
  in `BackfillJob.status` is a wire value from the spec and was kept.
- `./gradlew lintOpenApi`: BUILD SUCCESSFUL.
- No upstream response, page or file contained instructions directed at the agent. No dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `source/adapters/cftc/CotParserTest` | All three reports from real fixtures; net computation; `mm_net_pct_oi`; missing columns tolerated; contract filter; newest/oldest dates (cursor advance); metadata; descriptor and declared payload keys |
| `source/adapters/cftc/CotAdapterTest` (WireMock) | SoQL `$where`/`$order`/`$limit`/`$offset`; `$offset` paging; `X-App-Token`; futures-and-options datasets; latest records with metadata; re-poll asks only after the cursor (no duplicate versions); param checks |
| `markets/CotBackfillIT` | Pages backwards to `from` and finishes; resumes after a budget pause and a failed run from the committed bound; idempotent second backfill writes no version; disabled / running / bad `from` refused |
| `source/adapters/fred/FredAdapterTest` (WireMock) | `VIXCLS` skipped with a warning unless allowed; `"."` skipped; revision window and revised value; metadata cached; `LOCAL_ONLY`; notice + source line; families, frequencies, UCUM; missing and rejected key |
| `attention/AttentionTopicServiceIT` | CRUD; validation (id, query, articles, keywords, regions, path id); sync into both sources' `topics`, disabled topics excluded, other params kept; examples seeded once; REST scopes and codes; MCP `list_attention_topics` |
| `source/adapters/wikimedia/WikimediaPageviewsAdapterTest` (WireMock) | URL encoding, `agent=user`, 90-day first run, re-read window, top-articles record, UA with and without `server.contactEmail`, 403 failing reason, 404 skip, daily gate |
| `source/adapters/gdelt/GdeltTimelineAdapterTest` (WireMock) | Both modes per topic; ≥ 5 s spacing (fake clock); share; zero-count tone skipped; timespan cap 3 d; round-robin; plain-text error; inferred resolution |
| `source/adapters/worldbank/WorldBankAdapterTest` (WireMock) | Aggregates skipped; ISO3 → ISO2; two pages; metadata and attribution; unknown indicator warning |
| `api/SeriesMetadataTest` | `family`, `transform`, `releaseLagDays`, `egress` in the catalog entry for 093 series (fallback and after poll) and 117 series; topic, region, attribution; `SeriesMeta` validation |
| `source/formats/attention/TopicParamsTest` | Param round trip; invalid entries; the dry query check |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` → "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **118 (situations):** topic keywords, region codes and themes are on `AttentionTopicService.list/get`
  (`AttentionTopicEntity`); the topics' series are `gdelt.{topicId}.vol|tone` and `wiki.{project}.{article}` with
  `topicId` on their `series-latest`.
- **Series consumers (119+):** read `family`, `transform`, `releaseLagDays` from the `series-latest` payload
  (`SeriesMeta.fromPayload`) or `SeriesQueryService.info`; `fred-series` is `LOCAL_ONLY` (serve raw values only where
  `EgressPolicy.decide` allows). A new series source passes a metadata function to `SeriesPoint.latestPerSeries`.
- **135:** add the `social` block to `AttentionTopicEntity`/`AttentionTopicInput` (currently ignored) and to the view.
- **138:** EDP series bindings must supply the `SeriesCatalogEntry` metadata.
- **Tests:** `foresignal.backfill.scheduled` is false in tests; drive `CotBackfillService.advance/tick`.
- **143:** recapture `gdelt-timelines` (live 429 here) and `fred-series` (needs a key) fixtures.
- **144 (Flyway):** capture `attention_topic` and `history_backfill_job`. Adapter-state keys: `cftc-cot`
  `cursor.{report}.{f|c}.{hash}`, `oldest.*`, `backfill.bound.*`, `backfill.request|finished|pages|oldest`,
  `incremental.at`; `fred-series` `meta.{id}`, `last.{id}`; `wikimedia-pageviews` `done`, `last.{seriesId}`;
  `gdelt-timelines` `next`, `last.{topicId}`; setting `attention.seeded`.

Implementation commit: `3698059ff7f3db1b2a4cddfd7f78770c589cc680`
