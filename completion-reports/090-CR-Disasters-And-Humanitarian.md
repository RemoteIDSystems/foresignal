# 090-CR-Disasters-And-Humanitarian

| | |
|---|---|
| Prompt | 090-Disasters-And-Humanitarian.md |
| Status | Complete |
| Implementation commit | a058c21a0f498a78809f210f31da7883346901ba |
| Date | 2026-10-14 |

## Summary

The humanitarian and disaster layer lands: multi-hazard natural events, humanitarian disasters, displacement
statistics, WHO outbreak news, Copernicus emergency activations, US and New Zealand volcano alert levels, US
disaster declarations and US travel advisories. Most records are country-level, so geometry comes from prompt
040's gazetteer (label point or polygon) and every placed record carries `geometryApproximate=true`.

A cross-cutting addition is the descriptor field **`llmNotice`** (documented in the OpenAPI API-first but not yet
in Java): a per-source notice appended whenever that source's records reach an LLM context. `OpenFEMA`'s
eligibility notice is set on `openfema-declarations`, and `McpOutputShaper` (the single choke point used by
prompts 028 Ask, 029 briefings and 030 enrichment) appends it to every record item.

**1. Shared helpers** (`ai.foresignal.source.adapters.humanitarian`).
- `HtmlText.strip(html, maxChars)`: jsoup's allowlist cleaner (no tags kept, entities decoded), whitespace
  collapsed, capped.
- `Instants.parse(value)`: tolerant timestamps — ISO offset, zoned instant, offset-less ISO as UTC, and the
  space-separated `yyyy-MM-dd HH:mm:ss` HANS form as UTC. The live captures needed this (HANS `sent_utc`,
  Copernicus `eventTime`/`lastUpdate` without offsets).
- `BundledCountryPolygons` (040) gains `polygon(String iso2)` for the country-polygon lookups.

**2. `nasa-eonet`.** `GET /api/v3/events?status=open&days={days}`. JSON is detected by the first non-space byte,
not the content type (the response is labelled `application/rss+xml`). The latest `geometry[]` entry is the
record geometry (Point or Polygon); `eventTime` = the first date, `observedAt` = the latest; `validUntil =
closed`. Payload `title, description, categories, sources, magnitude, magnitudeUnit, trackPoints`. 30 min ·
2/min, 200/day; public domain.

**3. `reliefweb-disasters`.** `POST /v2/disasters?appname={appname}` with the documented JSON query. A 403 naming
the appname is `CredentialsRejectedException` (`APPNAME_NOT_APPROVED`), not retried. `eventTime = date.event`,
`observedAt = date.changed`, Point from `primary_country.location`. 6 h · 2/min, 50/day.

**4. `unhcr-population`.** One record per country of origin and per country of asylum per year (latest two
years). `eventTime` = 1 January; `observedAtMode` RECEIVED; numeric payload; string numbers and the `-` sentinel
handled; label-point geometry. 24 h · 5/min, 100/day; CC BY 4.0; retention 3650 d (`series`).

**5. `who-don`.** OData Disease Outbreak News. The title splits on the last dash into disease and country
(leading "the " stripped); a multi-country title is a MultiPoint. `OverrideTitle`/`UseOverrideTitle` handled.
6 h · 2/min, 50/day; CC BY-NC-SA 3.0 IGO.

**6. `copernicus-ems`.** The dashboard list is paged until an activation's `lastUpdate` is no older than the
watermark; the detail (`reason`, `subCategory`, `reportLink`) is fetched only when `lastUpdate` changed. Point
from the WKT centroid; `entityKeys gdacsId`; `embedFields name, reason`. 1 h · 10/min, 300/day.

**7. Volcano alert levels.** `usgs-hans-volcanoes` (alert levels and aviation colours, coordinates cached
weekly from `getUSVolcanoes`) and `geonet-volcanoes` (GeoJSON, levels 0–5, RECEIVED time base). Both 10 min;
retention 365 d (`state`); significant `alertLevel`/`aviationColor` and `nzLevel`/`aviationColor`.

**8. `openfema-declarations`.** One record per designated area; geometry is a 040 county polygon (`000` county
code → state polygon; tribal/unknown → null). The descriptor carries the `llmNotice`. 6 h · 10/min, 200/day.

**9. `travel-advisory-us`.** Conditional GET of the State advisories; `Category` is a FIPS 10-4 code (sent as a
string or an array), resolved through `CountryCentroids.byFips`; the level comes from the title. Geometry is
040's country polygon, else the label point. Significant `level`. 6 h · 2/min, 20/day.

**10. Not built.** `travel-advisory-uk` and the `hdx-hapi-*` family are marked optional and disabled in the
prompt; both are recorded as not built (the HAPI identifier and the FCDO per-country mapping were not
implemented).

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
- `source/adapters/humanitarian/`: `package-info`, `HtmlText`, `Instants`, `EonetAdapter`,
  `ReliefWebDisastersAdapter`, `UnhcrPopulationAdapter`, `WhoDonAdapter`, `CopernicusEmsAdapter`,
  `UsgsHansVolcanoAdapter`, `GeoNetVolcanoAdapter`, `OpenFemaDeclarationsAdapter`, `UsTravelAdvisoryAdapter`.

**Modified, main**
- `source/spi/SourceDescriptor.java` (record component, builder, constructor: `llmNotice`)
- `api/dto/SourceDescriptorDto.java`, `api/mapper/SourceMapper.java` (`llmNotice`)
- `mcp/McpOutputShaper.java` (`llmNotice` appended to record items)
- `reference/BundledCountryPolygons.java` (`polygon(iso2)`)

**Created, test**
- `humanitarian/{EonetParserTest, ReliefWebParserTest, UnhcrParserTest, WhoDonParserTest, CopernicusEmsParserTest,
  HansParserTest, GeoNetParserTest, OpenFemaParserTest, UsTravelAdvisoryParserTest, HumanitarianAdaptersTest,
  HumanitarianIngestIT}`
- `reference/CountryCentroidsFipsTest`
- fixtures `nasa-eonet/`, `reliefweb-disasters/`, `unhcr-population/`, `who-don/`, `copernicus-ems/`,
  `usgs-hans-volcanoes/`, `geonet-volcanoes/`, `openfema-declarations/`, `travel-advisory-us/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (nine 090 adapters classified)
- `testsupport/weather/WeatherIngestSupport.java` (nine base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`

## Deviations from the prompt

1. **`travel-advisory-uk` and the `hdx-hapi-*` family are not built.** Both are marked optional and disabled in
   the prompt; implementing them would need the FCDO field mapping and a HAPI identifier that were not
   available. They are recorded as not built.
2. **`travel-advisory-us` `Category` is an array.** The prompt says it "holds a FIPS 10-4 code"; the live API
   sends an array (`["SA"]`). The first element is used.
3. **`openfema-declarations` `noticeUrl`/`elevationM`.** HANS exposes `notice_url`, so it is stored; GeoNet
   publishes no notice id. `usgs-hans-volcanoes` carries `noticeUrl` from `notice_url`.
4. **EONET `description` added to the payload** (the prompt's `embedFields title, description` needs a payload
   key; the kind's payload list gained `description`).
5. **Copernicus detail shape.** The live `public-activations/?code=` response is `{"results":[{…}]}`, not
   `{"result":{…}}`; the parser reads the first `results` element.
6. **WHO "the " prefix.** Titles such as "– the Russian Federation" strip a leading "the " before `byName`.
7. **UNHCR `-` sentinel.** A `coo_iso`/`coa_iso` of `-` is treated as absent and the row skipped.
8. **Retention classes** (the prompt names none): `event` (1825 d) for EONET, ReliefWeb, WHO, Copernicus and
   OpenFEMA; `series` (3650 d) for UNHCR; `state` (365 d) for the volcano levels and travel advisories.

## Issues found / open questions

1. **`llmNotice` reaches LLM contexts only through `McpOutputShaper`.** The shaper is the choke point for 028,
   029 and 030, so the notice rides on every record item. A context-root, deduplicated notice was considered
   but the per-record field is what the Ask tool result exposes directly (and what the test asserts).
2. **HDX HAPI and UK travel advisory** are not built (Deviation 1).
3. **OpenFEMA geometry** needs `ref_admin2` loaded (`CountyGeometries.available()`); before it is, area geometry
   is null (the records are still stored).
4. **Unverified (143):** the ReliefWeb body with a real approved appname; the HANS coordinate join for volcanoes
   outside the captured first page; Copernicus detail for activations other than EMSR934.
5. **Instructions in data:** none. The endpoints, the FCDO/HAPI docs and the WHO/ReliefWeb terms pages were read;
   nothing addressed an agent.

## Tests written (not run)

- Parser tests on the captured fixtures: `EonetParserTest` (first-byte JSON sniffing, latest polygon),
  `ReliefWebParserTest`, `UnhcrParserTest` (string numbers, `-`), `WhoDonParserTest` (title split, multi-country
  MultiPoint, override title), `CopernicusEmsParserTest` (WKT centroid, offset-less times), `HansParserTest`
  (space `sent_utc`, cached coords), `GeoNetParserTest`, `OpenFemaParserTest`, `UsTravelAdvisoryParserTest`
  (level regex, FIPS→ISO2, no-level).
- `HumanitarianAdaptersTest` (`@IntegrationTest`, WireMock): ReliefWeb 403 → `APPNAME_NOT_APPROVED` without
  retry; GeoNet `Accept` header; Copernicus detail fetched only on change; OpenFEMA watermark in the next query.
- `CountryCentroidsFipsTest`: every FIPS code in the State fixture resolves (and YM → YE).
- `HumanitarianIngestIT` (`@IntegrationTest`, `WeatherIngestSupport`): OpenFEMA records ingest and the descriptor
  carries the eligibility `llmNotice`; a travel-advisory level change is persisted SIGNIFICANT.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: "Your API description is valid" (warnings only).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "835 requests across 80 tag folders … covers
  all ops: True" (no change).

## Notes for the next prompt

1. **091 onward:** the reviewed-list + base-URL-property + `FamilyDefaultsTest` classification pattern is the one
   to follow; `CountryCentroids`/`BundledCountryPolygons`/`CountyGeometries`/`Admin1Centroids` are constructor
   injected.
2. **Descriptor `llmNotice`** exists now; use it for any flag-limited source.
3. **097 checkpoint:** first run of every 090 test; `WeatherIngestSupport` gained the nine 090 base-URL
   properties; `FamilyDefaultsTest` gained nine entries; `SourceMapperTest`/`McpOutputShaperTest` may need the
   new `llmNotice` component.
4. **143:** recapture each fixture per its README; verify ReliefWeb with an approved appname and HDX with an
   identifier.
5. **144 (Flyway):** no schema change. New adapter state: `copernicus:lastUpdate`, `fema:lastRefresh`,
   `hans:coords`, `hans:coordsAt`, `us-advisory:index`.
6. **Owner decisions pending:** HDX HAPI and UK travel advisory (build or drop?), plus those carried from
   012–089.

Implementation commit: a058c21a0f498a78809f210f31da7883346901ba
