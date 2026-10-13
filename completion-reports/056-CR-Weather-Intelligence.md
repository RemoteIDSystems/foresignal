# 056-CR-Weather-Intelligence

| | |
|---|---|
| Prompt | 056-Weather-Intelligence.md |
| Status | Complete |
| Implementation commit | 4536a83f3fdfa8904c59aa5ad545971904c9768c |
| Date | 2026-10-13 |

## Summary

Alerts, briefings and Ask now all use the same deterministic weather facts, and each fact carries a status:
- every geolocated alert gets a computed `context.weather`;
- briefings and `/v1/digest` gain "Weather at places";
- Ask cites weather values and gets a deterministic guard against answers that ignore a non-ok status.

None of these paths fetches anything upstream. A section that is `stale`, `no-coverage` or `unavailable` is
worded as unknown, never as clear. `getWeatherSettings` and `updateWeatherSettings` are `implemented` and are off
`pending-operations.txt`.

**1. `WeatherContextService`** (new package `ai.foresignal.weather`).
- **Signature:** `compute(Geometry, Instant)` returns `WeatherContext {status, at, computedAt, sections, summary}`.
- **Inputs:** stored frames through `RasterReader` (`SampleService`), the GLM ring (`FlashSink`), and current heads
  through the new `WeatherRecords` / `JdbcWeatherRecords` (`record_head ⋈ record_live`, invariant HIST-2).
- **Budget:** the six sections run in parallel on virtual threads within 500 ms. A section that misses the budget
  is `unavailable` with the text "not computed within the 500 ms budget; this is not evidence of clear weather".
- **Status rules** follow 052:
  - an unknown, external, disabled or failing product is `unavailable`, and so is a product whose source is
    disabled (a new check; `SampleService.unavailableReason` does not cover a disabled source);
  - an observation frame further from the alert time than the product's `maxGap` is `stale`, with `asOf`;
  - a point or area with no coverage (fewer than half of the cells hold a value) is `no-coverage`;
  - a non-ok section carries `statusText` and no values.

| Section | Computation |
|---|---|
| `radar` | `mrms-reflectivity` maximum within 10 km: `PlaceGridIndex` for a point, `AreaScan` over a 10-km buffer for lines and polygons. `nearestCell` is the nearest `wx-cells` `radar-cell` within 100 km (`id`, `distanceKm`, `motion`, `meshMaxMm`), or null |
| `lightning` | GLM flashes within 20 km over the 15 min before the time (point: the ring's `flashesWithin`, counted up to the time; area: `countWithin` over a 20-km buffer). It is `stale` when the newest GLM file ended more than 2 min before the time, and `unavailable` when the window is older than the 60-min ring, GLM is disabled or failing, or there is no flash store |
| `precip` | `mrms-precip-rate` at the representative point, plus `qpe1hMm` when `mrms-qpe-1h` is installed and current |
| `warnings` | Active `nws-alert` records intersecting the geometry (not expired at the time), most severe first, at most 5: `{id, event, severity, expires, untrusted: true}`. The source is `unavailable` when disabled, `stale` when failing |
| `wind` | The newest complete run of HRRR (else GFS). The step chosen is the one nearest the time. Fields: `productId`, `speedMs`, `fromDeg`, `gustMs` (same model's gust product), `run`, `validTime`, `step`. HRRR without coverage falls back to GFS. It is `stale` when the run is more than 12 h old or the step is beyond the product's gap |
| `smoke` | `hrrr-smoke` at the nearest step: `pm25UgM3`, `run`, `validTime`, `step` |

- **Summary:** `WeatherSummary` builds one deterministic sentence in a fixed order, ending "Automated — not an
  official warning.". Non-ok sections read "lightning data stale since 14:30 UTC", "radar data has no coverage here
  (not evidence of clear weather)" or "smoke data unavailable". NWS event names are cleaned and cut to 60
  characters.

**2. Alert context.**
- **Column:** `alert.context` already existed (036).
- **When it is computed:** `AlertWeatherContext` runs in `TriggerEngine`, for both chunk evaluation and anchor
  evaluation. It computes before the insert transaction, so the context is stored with the alert before any event
  is published, and the rule template can use it. Each alert gets its own budget, the alerts run in parallel, and a
  chunk waits at most 750 ms (the budget plus slack).
- **Conditions:**
  - the record version has a geometry;
  - the alert is not suppressed;
  - the global `weather.alertContext` is `auto`;
  - the rule's `weatherContext` is not `off`;
  - `anyInputEnabled()` holds.
- **Failures** are logged and store `{status: unavailable, …, summary}`; they never block delivery.
- **Rule override:** `trigger_rule.weather_context` (`auto` | `off`; Hibernate column, null reads as auto),
  validated by `RuleValidator` and returned by `TriggerMapper`.
- **Global setting:** `weather` in `app_setting` (`SettingKeys.WEATHER`), through `WeatherSettingsService` and
  `GET/PUT /v1/settings/weather`. Missing members take the shipped defaults; every problem is reported in one 400.
- **AOI:** `PlacesAoiService` now reads `defaultAoi` and `aoiBufferKm` from the stored settings, else the
  properties. It recomputes and publishes `PlacesAoiChangedEvent` on `WeatherSettingsChangedEvent` when either
  changed.
- **REST:**
  - `GET /v1/alerts/{id}` returns the whole `context`;
  - `GET /v1/alerts` items carry `context = {"weather": {"summary": …}}` (or null) unless `include=context`
    (`AlertMapper.listItem`);
  - SSE and MCP keep the whole context.
- **Templates:** `WeatherTemplateVars` flattens every scalar leaf, for example `{{context.weather.summary}}`,
  `{{context.weather.radar.maxDbz}}` or `{{context.weather.warnings.items.0.event}}`. These variables work in rule
  templates and channel templates; `AlertTemplates` reads them from the notification's `data.context`.
- **Default bodies:** `DEFAULT_BODY_WITH_WEATHER` appends the summary line for `discord`, `telegram`, `email`,
  `ntfy`, `pushover` and `slack` (`DeliveryWorker`), and the stored notification keeps it with the message
  (`NotificationRouter`). Other types and custom templates are unchanged.
- **Notification data:** `NotificationRouter.data` now includes `context`, so webhook bodies carry
  `data.context.weather`.
- **Enrichment:** `WeatherEnrichmentContributor` (030 SPI, key `alert.context.weather`) adds the stored block
  inside the untrusted data envelope. `EnrichmentPrompts.SYSTEM` gains rule 6 ("Weather sections with status other
  than ok are unknown, not clear; …").

**3. Briefings: "Weather at places"** (`WeatherAtPlacesSection`, 029 SPI).
- **Scope:**
  - `BriefingScope` gains `places` and `weatherProducts`;
  - `BriefingValidator` returns 400 for an unknown place or product (the "until prompt 056" refusal is gone);
  - `/v1/digest?places=` returns 400 for an unknown place and 401 without a token, includes the places in the ETag,
    and returns the section as top-level `weatherAtPlaces` as well as under `sections.weatherAtPlaces`.
- **Per place, in the place's timezone:**
  - **now:** radar and lightning from the `place-weather` head, and active NWS alerts intersecting the place area
    with `alertsStatus`;
  - **period:** maximum `radar.maxDbzInRadius` and `hail.meshMaxMmInRadius` over the ok `place-weather` versions in
    `(fromSeq, toSeq]`, plus the version in force at the start. `partial` is true when `fromSeq` is below the
    `wx-places` retention floor. The GLM flash total within the radius comes from the 14-day archive, in one pass
    per satellite and day, taking the largest count over satellites; it is partial before the archive. The hazards
    that entered the area are `nws-alert`, `probsevere-object`, `lightning-cluster` and `fire-detection`; an entry is
    a first version intersecting the area whose previous version did not. The period also carries `notes`;
  - **next:** the `nws-forecast` head summary (stale after 6 h), and the GFS and HRRR maxima over 72 h from
    `ForecastSeriesService` (`gustMaxMs` with its `validTime`, `precipMm`, `smokeMaxUgM3`, `run`).
- **Markdown:** a compact table per place (an HTML table in renditions). Every non-ok part is labeled, for example
  "Lightning: data stale since 06:30"; the footer reads "Automated — not an official warning".
- **LLM mode:** gets `{instruction, places}` as the section's data block. `BriefingPrompts.SYSTEM` gains rule 7.
  The fallback is unchanged.

**4. Ask.**
- **Catalog:** the four weather tools were already in the shared `ToolCatalog` (they are read-only beans), so Ask and
  MCP stay identical.
- **Citation ids:** each weather value now carries `citationId`:
  - `WeatherToolSupport.value` gives `wx:{productId}@{frameKey}`, or `wx:{productId}@{run}/{step}` for a forecast;
  - `area_weather_stats` gives the same forms;
  - each `weather_forecast` step gives `wx:{provider}@{run}/{step}`; NWS uses `wx:nws@{updatedAt}/{hours}`;
  - each `get_map_image` product carries one too.
- **Verification:**
  - `WeatherCitationExtractor` records the weather values and the non-ok statuses in `CitationSet`;
  - `CitationVerifier` accepts `[wx:…]`;
  - `AskCitation` gains type `weather` with `productId`, `frameKey`, `run`, `step`, `validTime` and `ageMinutes`.
- **Prompt:** `AskPrompts` gains rule 7 (cite `[wx:…]` with run, validTime and age; non-ok means unknown; automated,
  not official).
- **Guard:** `WeatherAnswerGuard` runs after verification. It appends the standard caveat and the warning
  `weather caveat appended: …` when the answer states neither the `statusText` nor an equivalent phrase. It is
  skipped for refusals and streamed as a final delta.
- **Images:** `get_map_image` images reach Anthropic providers inside the tool result (new
  `llm.client.ToolResult.images`, `ToolLoop.RichToolExecutor`, `BetaToolResultBlockParam.contentOfBlocks` with a
  base64 image block, verified in anthropic-java-core 2.71.0). Other providers get the caption only; it is added to
  the result data as `caption`.

**5. Docs.**
- **Agent Guide:** a new "Weather (prompt 056)" section (conventions; sampling, forecast, area stats and map recipes;
  Home Assistant and Node-RED consumers of `context.weather.summary`), the wx citation and caveat warning in Ask,
  and `weatherAtPlaces` in the digest.
- **Operator Guide:** a new "Alert weather context" section (sections, statuses, the setting and the rule override,
  template variables, enrichment, "Weather at places"), with updates to Triggers, Notification channels and
  Briefings.
- **Architecture:** §3 and the AOI paragraph.
- **Invariants:** new tests listed under SRC-6, EGR-1 and EGR-3.

**6. Spec and Postman.**
- **Spec:** the settings operations are implemented. `extended-by` 056 is added on getAlert, listAlerts, getDigest,
  createTrigger, updateTrigger, createBriefing and updateBriefing. The descriptions of getDigest and listAlerts
  `include` are updated, and so are those of `WeatherContext`, `AlertContext`, `NotificationMessage.data` (alert
  `context`) and `AskCitation`. `PlaceWeatherDigest` gains `now.alertsStatus` and `period.notes`. There is a
  changelog entry.
- **Postman:** regenerated with new trigger, briefing and weather-settings `BODIES` examples.

## Files created / modified

Production, created:
- `weather/`: `package-info`, `WeatherContextService`, `WeatherContext`, `WeatherSections`, `WeatherSummary`,
  `WeatherRecords`, `JdbcWeatherRecords`, `WeatherTemplateVars`, `AlertWeatherContext`, `WeatherSettings`,
  `WeatherSettingsService`, `WeatherSettingsChangedEvent`, `WeatherEnrichmentContributor`, `WeatherAtPlacesSection`,
  `WeatherCitationExtractor`, `WeatherCitations`, `WeatherAnswerGuard`;
- `api/WeatherSettingsController`.

Production, modified:
- `trigger/`: `TriggerEngine`, `RuleValidator`, `ValidatedRule`, `EnrichmentPrompts`;
- `store/TriggerRuleEntity`;
- `api/`: `AlertsController`, `mapper/AlertMapper`, `mapper/TriggerMapper`;
- `notify/`: `AlertTemplates`, `NotificationRouter`, `DeliveryWorker`;
- `briefing/`: `BriefingScope`, `BriefingValidator`, `BriefingSectionContributor`, `DigestStatsService`,
  `DigestService`, `BriefingPrompts`;
- `ask/`: `AskService`, `AskPrompts`, `AskCitation`, `CitationSet`, `CitationVerifier`;
- `llm/client/`: `ToolResult`, `ToolLoop`, `AnthropicLlmClient`;
- `mcp/tools/`: `WeatherToolSupport`, `AreaWeatherStatsTool`, `WeatherForecastTool`, `GetMapImageTool`;
- `place/PlacesAoiService`;
- `settings/SettingKeys`.

Docs, spec and tooling: `docs/OpenAPI.json`, `docs/Agent-Guide.md`, `docs/Operator-Guide.md`,
`docs/Architecture.md`, `docs/Invariants.md`, `scripts/generate-postman.py`,
`postman/Foresignal.postman_collection.json`, `src/test/resources/openapi/pending-operations.txt`.

Tests, created (`src/test/java/ai/foresignal/weather/`): `WeatherContextServiceTest`,
`TemplateWeatherVariablesTest`, `WeatherSettingsServiceTest`, `AlertWeatherContextIT`, `EnrichmentWeatherInputIT`,
`BriefingWeatherSectionIT`, `AskWeatherToolsIT`.

## Deviations from the prompt

1. **The package is `ai.foresignal.weather`**, one of the two the prompt allows.
2. **Tool registration needed no change.** The four tools were already in the shared catalog, because the catalog
   lists every read-only `ForesignalMcpTool` bean. The work was their `citationId`s, the extractor, the verifier and
   the guard.
3. **Non-ok sections carry no values.** A stale section has `asOf` and `statusText` and no metrics, so a stale
   number can never be read as current. The `place-weather` payloads keep their last values; the alert context does
   not.
4. **`no-data` maps to `unavailable`** in the alert context. `WeatherSectionStatus` has no `no-data`, so a
   sample-level `no-data` is reported as `unavailable` with the `no-data` text.
5. **"Nearest valid time".** Wind and smoke use the step nearest the time in the newest complete run; observation
   frames use the frame at or before the time, or the newest frame when it is just after and nearer.
6. **Lightning for an old time.** The ring holds 60 minutes, so a time whose 15-minute window starts before the ring
   is `unavailable` (catch-up after downtime).
7. **Spec additions.**
   - **`PlaceWeatherDigest.now.alertsStatus`** is needed because an empty NWS alert list must not read as quiet
     while the source is disabled, failing or not visible.
   - **`PlaceWeatherDigest.period.notes`** explains unknown or partial values.
8. **Lists drop `near` and `anchor`.** Following the spec, `listAlerts` items no longer carry
   `context.near`/`context.anchor` without `include=context`. Single reads, SSE, webhooks and MCP keep them.
9. **Forecast citation form.** The citation uses the run's ISO time as tools return it, for example
   `wx:gfs@2026-10-12T06:00:00Z/15`. The weather_forecast "productId" is the provider (`gfs`, `hrrr`, `nws`); `gfs`
   and `hrrr` are also those models' wind product ids.
10. **Images and providers.**
    - Images go to Anthropic only. OpenAI-compatible tool messages take text only.
    - Ollama tool messages are left caption-only, because image support in a `tool` role message could not be
      verified.
    - Images are not persisted with a thread's raw turns, so a replayed turn carries the caption only.
11. **The `get_digest` MCP tool** has no `places` argument. The prompt names only `GET /v1/digest`, and the spec
    defines no MCP schema for it.
12. **Default `weatherProducts`** are `mrms-reflectivity`, `mrms-mesh-60m`, `mrms-precip-rate`, `hrrr-smoke`,
    `gfs-gust` and `gfs-apcp` (the `wx-places` inputs). They choose the radar and MESH values and the GFS and HRRR
    rows. Lightning, NWS alerts and the NWS forecast are always shown, because GLM and NWS are not raster products.

## Issues found / open questions

**Needs the owner (non-blocking):**

1. **Lists and `context`.** Should `listAlerts` keep `context.near` and `context.anchor` without
   `include=context`? The spec says only the weather summary (Deviation 8). A client that read `near` from lists
   now needs `include=context`.
2. **Ollama images.** Should Ollama providers receive map images? This would need a verified `images` field on
   `tool` messages (Deviation 10).
3. **Product Charter.** No change was made: the Charter is the owner's document, per this run's instructions. The
   alert weather context and "Weather at places" are derived products computed from ingested data, which the
   existing non-goals already allow as far as this prompt needed.

**Other findings:**

4. **Unchanged chunk timing without weather inputs.** When no weather input is enabled, `anyInputEnabled()` is false
   and no context is computed. In the test contexts every source is disabled, so the existing ITs see no
   `context.weather`.
5. **Test contributor key.** `EnrichmentTestSources.TestWeatherContributor` uses the same key
   (`alert.context.weather`) as the real contributor. It runs later (order 10 against -100), so while it is switched
   on its section replaces the real one in the test context only.
6. **Digest ETag.** `/v1/digest`'s ETag covers the places. Weather that changes without a new seq (a forecast frame
   with no `place-weather` change) can be cached until the next record.
7. **`labelled`.** The `PlaceWeatherDigest` description (a 056 schema) said "labelled"; it now says "labeled".
   Nine other descriptions owned by other prompts (the 098–132 block and 055's `getStaticMap`) still say
   "labelled" or "unlabelled". They were left for their prompts or the owner.
8. **No instructions directed at an agent** appeared in any file or tool output read.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `weather/WeatherContextServiceTest` | Every section from synthetic frames (`SampleFixtures`), flashes and records. Polygon geometry (10/20-km buffers). Statuses: no-coverage MRMS, stale GLM with `asOf`, disabled HRRR as unavailable, a stale frame. HRRR→GFS fallback; disabled and failing NWS; warning order and the expiry and five-item caps. A budget overrun is unavailable with the budget text, within 2 s. A missing geometry; `anyInputEnabled`. Summary golden strings with ok and non-ok wording, and `clean`. No outbound HTTP (constructors take stored-data readers only). `bufferKm` |
| `weather/TemplateWeatherVariablesTest` | `{{context.weather.summary}}`, nested and list fields with canonical numbers. Missing context, section or field renders empty. Channel templates read `data.context`. Default bodies: the six types append the summary, webhook does not, nothing without a context |
| `weather/WeatherSettingsServiceTest` | Defaults from the properties, parsing and shipped defaults, every problem in one 400, the box checks, `put` storing and publishing, `aoiChanged` |
| `weather/AlertWeatherContextIT` | (`@MockitoBean WeatherContextService`) The fired event already carries `context.weather`, and the rule template renders the summary. Rule `off`, global `off` and an alert without geometry skip it. A failing computation stores `unavailable` and is still dispatched |
| `weather/EnrichmentWeatherInputIT` | The `alert.context.weather` block reaches `FakeLlmClient` inside `<data>`. The system prompt has rule 6. Injected NWS text stays inside the data block and changes nothing; no tools are offered. No block without a context |
| `weather/BriefingWeatherSectionIT` | The section on in-memory records for two places (America/Chicago, Asia/Tokyo): now, period and next; stale and unavailable labels; partial period; golden markdown; `llmContext`. No places means no section. On the briefing context: the digest briefing renders it, the LLM briefing receives the block and instruction, `/v1/digest` places include `weatherAtPlaces` (401 anonymous, 400 unknown), and unknown `scope.places` and `weatherProducts` are refused |
| `weather/AskWeatherToolsIT` | (`@MockitoSpyBean SampleService`) The weather tools are offered and the system prompt has rule 7. A `wx:` citation is verified with productId, frameKey, validTime and age; an invented one is dropped. "Skies are clear" with radar no-coverage gets the caveat and a warning; a stated status gets none. The private gate's excluded sources reach `sample_weather`. A guard and verifier unit check |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered.
- No new dependency.

## Notes for the next prompt

1. **057 checkpoint:**
   - New contexts: `AlertWeatherContextIT` (`@MockitoBean WeatherContextService` on the trigger context) and
     `AskWeatherToolsIT` (`@MockitoSpyBean SampleService` on the Ask context). `BriefingWeatherSectionIT` uses the
     briefing context, and `EnrichmentWeatherInputIT` the enrichment context.
   - The golden markdown in `BriefingWeatherSectionIT` depends on `zzz` printing `CDT` and `JST` (Locale.US).
   - Check that `TriggerEngine` still meets the latency budget of the trigger ITs. No context is computed while
     weather inputs are disabled.
   - `WeatherContextServiceTest.budget` sleeps a virtual thread for up to 3 s (it is interrupted on cancel).
   - The `JdbcWeatherRecords` SQL (geography `ST_DWithin`, the `prev_seq` self-join in `entered`) runs for the
     first time in `BriefingWeatherSectionIT.digestBriefing` and `llmBriefing`.
2. **058 (map style):** no interaction.
3. **143:** measure:
   - the alert weather context's time per alert with real MRMS, GLM and HRRR data, against the 500 ms budget;
   - chunk latency during alert bursts;
   - the GLM archive pass of "Weather at places" for a 7-day lookback.
4. **144 (Flyway):**
   - `trigger_rule` gains `weather_context varchar(8) default 'auto'` (null reads as auto);
   - `app_setting` key `weather` (`WeatherSettings`);
   - `briefing.scope` JSON gains `places` and `weatherProducts`;
   - `alert.context` gains `weather`.
5. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–055.
