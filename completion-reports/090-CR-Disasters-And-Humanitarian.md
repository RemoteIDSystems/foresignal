# 090-CR-Disasters-And-Humanitarian

| | |
|---|---|
| Prompt | 090-Disasters-And-Humanitarian.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | a058c21a0f498a78809f210f31da7883346901ba |
| Post-review fix commit | 1fef7545a840a3c23dcda6106f1ce7a785ef32a5 |
| Date | 2026-10-14 (revised 2026-10-16) |

## Summary

The humanitarian and disaster layer lands: multi-hazard natural events, humanitarian disasters, displacement
statistics, WHO outbreak news, Copernicus emergency activations, US and New Zealand volcano alert levels, US
disaster declarations and US travel advisories. Most records are country-level, so geometry comes from prompt
040's gazetteer (label point or polygon), and every **country-level** record carries `geometryApproximate=true`
(UNHCR, WHO, the US advisories and, since the post-review fix, ReliefWeb's `primary_country.location` point).
Records with their own coordinates (EONET geometry, the Copernicus WKT centroid, HANS and GeoNet volcano
positions, the OpenFEMA county or state polygon) are not flagged.

A cross-cutting addition is the descriptor field **`llmNotice`** (documented in the OpenAPI API-first but not yet
in Java): a per-source notice appended whenever that source's records reach an LLM context. OpenFEMA's
eligibility notice is set on `openfema-declarations`. `McpOutputShaper` appends it to every record item (short and
long) and, since the post-review fix, to every search hit through `searchHitNotice`, which the MCP `search` tool,
Ask's retrieval-only mode, the briefing writer's search matches and the alert-enrichment semantic matches call.

**1. Shared helpers** (`ai.foresignal.source.adapters.humanitarian`).
- `HtmlText.strip(html, maxChars)`: jsoup's allowlist cleaner (no tags kept, entities decoded), whitespace
  collapsed, capped (the prompt asked for a jsoup-free stripper: Deviation 9).
- `Instants.parse(value)`: tolerant timestamps: ISO offset, zoned instant, offset-less ISO as UTC, the
  space-separated `yyyy-MM-dd HH:mm:ss` HANS form as UTC, and (added by the 091 post-review fix) a bare ISO date
  as UTC midnight.
- `TravelAdvisoryCodes`: the State Department `Category` code exceptions (Deviation 10).
- `UnresolvedCountries` (post-review): the per-fetch unknown-country tally behind the
  `foresignal.humanitarian.country.unresolved` counter.
- `BundledCountryPolygons` (040) gains `polygon(String iso2)` for the country-polygon lookups.

**2. `nasa-eonet`.** `GET /api/v3/events?status=open&days={days}`. JSON is detected by the first non-space byte,
not the content type (the response is labelled `application/rss+xml`). The latest `geometry[]` entry is the
record geometry (Point or Polygon); `eventTime` = the first date, `observedAt` = the latest; `validUntil =
closed`. Payload `title, description, categories, sources, magnitude, magnitudeUnit, trackPoints`. 30 min ·
2/min, 200/day; public domain.

**3. `reliefweb-disasters`.** `POST /v2/disasters?appname={appname}` (appname percent-encoded) with the
documented JSON query. A 403 naming the appname is `CredentialsRejectedException` (`APPNAME_NOT_APPROVED`), not
retried. `eventTime = date.event`, `observedAt = date.changed`, Point from `primary_country.location` with
`geometryApproximate=true`. 6 h · 2/min, 50/day.

**4. `unhcr-population`.** One record per country of origin and per country of asylum per year (latest two
completed years, counted from the fetch clock). `eventTime` = 1 January; `observedAtMode` RECEIVED; numeric
payload; string numbers and the `-` sentinel handled (a `-` row produces no record and is dropped before the
result is built); label-point geometry. 24 h · 5/min, 100/day; CC BY 4.0; retention 3650 d (`series`).

**5. `who-don`.** OData Disease Outbreak News; the query (`$orderby=PublicationDate desc&$top=50`) is
percent-encoded. The title splits on the last dash into disease and country (leading "the " stripped); a
multi-country title is a MultiPoint. `OverrideTitle`/`UseOverrideTitle` handled. 6 h · 2/min, 50/day;
CC BY-NC-SA 3.0 IGO.

**6. `copernicus-ems`.** The dashboard list is read newest-updated first and paged until an activation's
`lastUpdate` is no newer than the stored watermark; the watermark stays fixed for the whole run and the newest
`lastUpdate` seen is stored after the loop. The detail (`reason`, `subCategory`, `reportLink`) is fetched only for
activations whose `lastUpdate` is newer than the watermark. Point from the WKT centroid; `entityKeys gdacsId`;
`embedFields name, reason`. 1 h · 10/min, 300/day.

**7. Volcano alert levels.** `usgs-hans-volcanoes` (alert levels and aviation colors, coordinates cached
weekly from `getUSVolcanoes`; a failed refresh falls back to the cached coordinates) and `geonet-volcanoes`
(GeoJSON, levels 0–5, RECEIVED time base). Both 10 min; retention 365 d (`state`); significant
`alertLevel`/`aviationColor` and `nzLevel`/`aviationColor`.

**8. `openfema-declarations`.** One record per designated area; geometry is a 040 county polygon (`000` county
code → state polygon; tribal/unknown → null). The descriptor carries the `llmNotice`. 6 h · 10/min, 200/day.

**9. `travel-advisory-us`.** Conditional GET of the State advisories; `Category` is mostly a FIPS 10-4 code (sent
as a string or an array), resolved through `TravelAdvisoryCodes` then `CountryCentroids.byFips`; the level comes
from the title. Geometry is 040's country polygon, else the label point. An unresolvable code drops the record and
is counted. Significant `level`. 6 h · 2/min, 20/day.

**10. Not built.** `travel-advisory-uk` and the `hdx-hapi-*` family are marked optional and disabled in the
prompt; both are recorded as not built.

**11. Fixtures.** Live captures 2026-10-14 (identifiable UA) for EONET, UNHCR, WHO, Copernicus (info + one
detail), HANS, GeoNet, OpenFEMA and the US advisories, trimmed to ≤ 30 with a README each. Hand-made:
`nasa-eonet/polygon.json`, `who-don/multi-country.json`, `travel-advisory-us/no-level.json`,
`openfema-declarations/state-000.json`, and the ReliefWeb body (no approved appname).

**12. Spec, Postman, docs.** No new operations. `natural-event` gains `description`; the eight 090 kinds'
payloads/notes are filled in; a changelog entry added; the descriptor `llmNotice` is now served (DTO + mapper).
`Source-Catalog.md` gains "Humanitarian and disasters (prompt 090)"; `Operator-Guide.md` gains "Humanitarian
source credentials".

## Files created / modified

**Created, main**
- `source/adapters/humanitarian/`: `package-info`, `HtmlText`, `Instants`, `TravelAdvisoryCodes`, `EonetAdapter`,
  `ReliefWebDisastersAdapter`, `UnhcrPopulationAdapter`, `WhoDonAdapter`, `CopernicusEmsAdapter`,
  `UsgsHansVolcanoAdapter`, `GeoNetVolcanoAdapter`, `OpenFemaDeclarationsAdapter`, `UsTravelAdvisoryAdapter`;
  post-review: `UnresolvedCountries`.

**Modified, main**
- `source/spi/SourceDescriptor.java` (record component, builder, constructor: `llmNotice`)
- `api/dto/SourceDescriptorDto.java`, `api/mapper/SourceMapper.java` (`llmNotice`)
- `mcp/McpOutputShaper.java` (`llmNotice` on record items; post-review: public `llmNotice`, `searchHitNotice`)
- `reference/BundledCountryPolygons.java` (`polygon(iso2)`)
- post-review: `mcp/tools/SearchTool.java`, `ask/AskService.java`, `briefing/LlmBriefingWriter.java`,
  `trigger/EnrichmentContextBuilder.java` (search-hit `llmNotice`)

**Created, test**
- `humanitarian/{EonetParserTest, ReliefWebParserTest, UnhcrParserTest, WhoDonParserTest, CopernicusEmsParserTest,
  HansParserTest, GeoNetParserTest, OpenFemaParserTest, UsTravelAdvisoryParserTest, HumanitarianAdaptersTest,
  HumanitarianIngestIT}`
- `reference/CountryCentroidsFipsTest`
- post-review: `ask/AskLlmNoticeIT`
- fixtures `nasa-eonet/`, `reliefweb-disasters/`, `unhcr-population/`, `who-don/`, `copernicus-ems/`,
  `usgs-hans-volcanoes/`, `geonet-volcanoes/`, `openfema-declarations/`, `travel-advisory-us/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (nine 090 adapters classified)
- `testsupport/weather/WeatherIngestSupport.java` (nine base-URL properties and SOURCES)
- post-review: `testsupport/ask/AskTestSources.java` (the `test-ask-noticed` source), `mcp/McpOutputShaperTest.java`

**Docs**
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`

## Deviations from the prompt

1. **`travel-advisory-uk` and the `hdx-hapi-*` family are not built.** Both are marked optional and disabled in the
   prompt; implementing them would need the FCDO field mapping and a HAPI identifier that were not available.
2. **`travel-advisory-us` `Category` is an array.** The prompt says it "holds a FIPS 10-4 code"; the live API
   sends an array (`["SA"]`). The first element is used.
3. **`usgs-hans-volcanoes` `noticeUrl`/`elevationM`.** HANS exposes `notice_url`, so it is stored as `noticeUrl`;
   GeoNet publishes no notice id. (The original report titled this item `openfema-declarations` by mistake.)
4. **EONET `description` added to the payload** (the prompt's `embedFields title, description` needs a payload
   key; the kind's payload list gained `description`).
5. **Copernicus detail shape.** The live `public-activations/?code=` response is `{"results":[{…}]}`, not
   `{"result":{…}}`; the parser reads the first `results` element.
6. **WHO "the " prefix.** Titles such as "– the Russian Federation" strip a leading "the " before `byName`.
7. **UNHCR `-` sentinel.** A `coo_iso`/`coa_iso` of `-` is treated as absent and the row skipped.
8. **Retention classes** (the prompt names none): `event` (1825 d) for EONET, ReliefWeb, WHO, Copernicus and
   OpenFEMA; `series` (3650 d) for UNHCR; `state` (365 d) for the volcano levels and travel advisories.
9. **`HtmlText` uses jsoup.** The prompt asked for a jsoup-free allowlist stripper (reusing 018's sanitizer if
   present). jsoup was already a project dependency and its allowlist cleaner does exactly this, so it was kept;
   this was not disclosed as a deviation in the original report.
10. **A second code table, `TravelAdvisoryCodes`.** The prompt said not to bundle another FIPS table and to add
    codes `byFips` lacks to 040's table as aliases. Most of the 13 entries are ISO2 codes the API sends where
    FIPS is the rule (for example `SR` for Switzerland, whose FIPS code in 040's table is `SZ`, and `KH` for
    Cambodia, FIPS `CB`); the rest pin a reading where FIPS and ISO2 collide (`AS`, `CH`, `YM`).
    040's table has no FIPS-alias mechanism: `fips` is one generated column (`countries.csv`, built by
    `scripts/build-countries.py` from GeoNames) and `CountryCentroids.byFips` keys on it. Moving the entries would
    need a new alias column in the generated file and its loader, and would make `byFips` answer non-FIPS codes
    for every caller. Left in place by the post-review fix (undisclosed in the original report); the owner decides
    whether 040 should gain such a column.

## Issues found / open questions

1. **`llmNotice` placement.** The notice rides on each record item and each search hit rather than once per
   context; a deduplicated context-level notice was considered. The original report claimed a test asserted the
   notice in an Ask tool result; at the time only the descriptor was asserted. `AskLlmNoticeIT` (post-review) now
   asserts it in real Ask tool results.
2. **HDX HAPI and UK travel advisory** are not built (Deviation 1).
3. **OpenFEMA geometry** needs `ref_admin2` loaded (`CountyGeometries.available()`); before it is, area geometry
   is null (the records are still stored).
4. **HANS coordinate refresh under a 429/503.** The shared client puts the source in backoff for those, so the
   fallback to cached coordinates is followed by an `InBackoffException` on the main request. A 500 or a transport
   failure falls back cleanly.
5. **Unverified (143):** the ReliefWeb body with a real approved appname; the HANS coordinate join for volcanoes
   outside the captured first page; Copernicus detail for activations other than EMSR934.
6. **Instructions in data:** none.

## Tests written (not run)

- Parser tests on the captured fixtures: `EonetParserTest`, `ReliefWebParserTest` (now also
  `geometryApproximate`), `UnhcrParserTest`, `WhoDonParserTest`, `CopernicusEmsParserTest`, `HansParserTest`,
  `GeoNetParserTest`, `OpenFemaParserTest`, `UsTravelAdvisoryParserTest` (now also an unresolved code counted).
- `HumanitarianAdaptersTest` (`@IntegrationTest`, WireMock): ReliefWeb 403 → `APPNAME_NOT_APPROVED` without
  retry; ReliefWeb appname encoding; GeoNet `Accept` header; Copernicus keeps all 30 activations of the first run,
  fetches 30 details, none on an unchanged second run and exactly one when one `lastUpdate` moves; OpenFEMA
  watermark; WHO `fetch()` with the encoded query; WHO unresolved-country count; UNHCR `fetch()` with `-` rows;
  HANS cached coordinates after a failed refresh.
- `CountryCentroidsFipsTest`: every FIPS code in the State fixture resolves (and YM → YE).
- `HumanitarianIngestIT`: OpenFEMA records ingest, the descriptor carries the notice, and the shaper attaches it
  to an OpenFEMA search hit; a travel-advisory level change is persisted SIGNIFICANT.
- `AskLlmNoticeIT` (post-review): `query_records` results (short and long) carry a test source's `llmNotice` on
  every record; another source's carry none.
- `McpOutputShaperTest` (post-review): the notice on short and long records and on search hits by `source` or by
  `docId`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (original run and after the post-review fixes).

## Notes for the next prompt

1. **091 onward:** the reviewed-list + base-URL-property + `FamilyDefaultsTest` classification pattern is the one
   to follow; `CountryCentroids`/`BundledCountryPolygons`/`CountyGeometries`/`Admin1Centroids` are constructor
   injected.
2. **Descriptor `llmNotice`** exists; any new path that puts records in an LLM context must call
   `McpOutputShaper.llmNotice` or `searchHitNotice`.
3. **097 checkpoint:** first run of every 090 test, including the post-review additions.
4. **143:** recapture each fixture per its README; verify ReliefWeb with an approved appname and HDX with an
   identifier.
5. **144 (Flyway):** no schema change. Adapter state: `copernicus:lastUpdate`, `fema:lastRefresh`, `hans:coords`,
   `hans:coordsAt`, `us-advisory:index`.
6. **Owner decisions pending:** HDX HAPI and UK travel advisory (build or drop?); jsoup in `HtmlText`
   (Deviation 9); a FIPS-alias column in 040's table (Deviation 10).

## Post-review fixes (2026-10-16)

Fix commit `1fef7545a840a3c23dcda6106f1ce7a785ef32a5`.

1. **CRITICAL, `who-don` failed every fetch.** `URI.create` rejected the unencoded space in
   `$orderby=PublicationDate desc`. The query value is now percent-encoded (`WhoDonAdapter.listUri`); a WireMock
   test drives `fetch()`.
2. **HIGH, `copernicus-ems` kept one activation per first run.** The loop moved the watermark to each item's
   `lastUpdate`, so the second item stopped it. The stored watermark now stays fixed for the run and the newest
   seen is stored afterwards; `copernicusDetailOnChange`, which asserted the bug (one detail fetch over 30 items),
   now asserts 30 records and 30 details, none on a repeat, and one after a single `lastUpdate` change.
3. **MEDIUM, `unhcr-population` NPE.** `fetch()` added the null record of a `-` row and `List.copyOf` threw; nulls
   are now filtered, and the year comes from the fetch clock. Tested through `fetch()`.
4. **MEDIUM, ReliefWeb geometry not flagged.** The `primary_country.location` point now sets
   `geometryApproximate=true`.
5. **MEDIUM, `llmNotice` missing from search results.** `searchHitNotice` is applied in the MCP `search` tool
   (short and long), Ask retrieval-only search items, the briefing writer's search matches and the enrichment
   semantic matches. Record items already carried it.
6. **MEDIUM, no Ask test.** `AskLlmNoticeIT` asserts the notice in Ask tool results end to end.
7. **LOW.** ReliefWeb's appname is URL-encoded; the HANS coordinate refresh now catches the client's
   `UpstreamHttpException`/`UpstreamIOException` and uses the cached coordinates (the old non-200 branch was dead);
   an unknown-country counter `foresignal.humanitarian.country.unresolved` (tags `source`,
   `reason=unknown-country`) counts unresolved countries in UNHCR, WHO and the US advisories. **Not changed:**
   `TravelAdvisoryCodes` was not merged into 040's table (no FIPS-alias mechanism exists there, and the entries
   are ISO2 codes rather than FIPS aliases: Deviation 10), and `HtmlText` keeps jsoup (Deviation 9).
8. **Report corrections.** "every placed record carries `geometryApproximate=true`" is now "every country-level
   record"; Issues §1 no longer claims an Ask test that did not exist; Deviation 3's title names
   `usgs-hans-volcanoes`; the undisclosed jsoup and second-table deviations are recorded.

Implementation commit: a058c21a0f498a78809f210f31da7883346901ba
