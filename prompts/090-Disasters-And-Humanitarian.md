# 090 — Disasters And Humanitarian

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/090-CR-Disasters-And-Humanitarian.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/089-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `nasa-eonet`, `reliefweb-disasters`, `unhcr-population`, `who-don` (World Monitor survey)
- `docs/design-history/Core-Data-Model.md` §2 (catalogue and measurement rows; a source's time base is fixed), §3 (descriptor, `embedFields`, payload limits), §5.3
- `docs/design-history/Gap-Analysis.md` §4b rows "Government travel advisories" and "Volcano levels (HANS, GeoNet), Copernicus EMS, OpenFEMA", §8.C item 49 ("Extend 052" = this prompt)
- Completion Reports of 008 (`titleField`, `validFrom`), 026–030 (how source text reaches LLM prompts), 040 (country/admin centroids and polygons, US counties, `{{near.*}}`)
- Upstream: https://eonet.gsfc.nasa.gov/docs/v3, https://apidoc.reliefweb.int, https://api.unhcr.org/docs/refugee-statistics.html, https://www.who.int/api/emergencies/diseaseoutbreaknews, https://rapidmapping.emergency.copernicus.eu, https://volcanoes.usgs.gov/hans-public/api/, https://api.geonet.org.nz, https://www.fema.gov/about/openfema/terms-conditions, https://cadataapi.state.gov/api/TravelAdvisories, https://hapi.humdata.org/docs

## Objective

Add the humanitarian and disaster layer: multi-hazard natural events, humanitarian disasters, displacement statistics, WHO outbreak news, Copernicus emergency activations, volcano alert levels (US and New Zealand), US disaster declarations and government travel advisories, with an optional HDX HAPI family. Most of these are country-level, so geometry comes from 040's gazetteer and is flagged approximate.

## Tasks

### 1. Shared helpers (`ai.foresignal.source.adapters.humanitarian`)

- Use 040's `CountryCentroids` (`byIso2`, `byIso3`, `byFips`, `byName`; label points that always fall inside the country) for every country-level record, and set payload `geometryApproximate=true` (Adapter Guide rule from 040). Use 040's country polygons where a polygon is wanted. Unknown country → null geometry, counted. Do not bundle another centroid or FIPS table; if the State Department fixture contains a FIPS code `byFips` lacks, add it to 040's table as an alias.
- `HtmlText.strip(html, maxChars)`: jsoup-free, allowlist-based tag stripper with entity decoding (reuse 018's sanitizer if present).
- All free text from these sources is untrusted for LLM use: quote as data, never as instructions (028 rules).

### 2. `nasa-eonet` (POLLING)

- `GET https://eonet.gsfc.nasa.gov/api/v3/events?status=open&days={days}` (param `days`, default 30). **Verified 2026-10:** the body is JSON but the response is labelled `application/rss+xml`; detect JSON by its first non-space byte instead of trusting the content type.
- Record: kind `natural-event`, externalId `id`, geometry = the latest `geometry[]` entry (Point or Polygon), `eventTime` = first geometry `date`, `observedAt` = latest geometry `date`, `validUntil` = `closed` when set. Payload `title, categories` (ids), `sources` (ids and URLs), `magnitude`, `magnitudeUnit`, `trackPoints` (count). Storm tracks over 2,000 points keep only the latest geometry. titleField `title`, `embedFields` `title, description`.
- Interval 30 min, budget 2/min, 200/day. Public domain (NASA).

### 3. `reliefweb-disasters` (POLLING)

- Credential `appname` (required). **Verified 2026-10:** ReliefWeb now rejects unapproved appnames with HTTP 403 ("You are not using an approved appname"); the operator must request one at https://apidoc.reliefweb.int/parameters#appname. Map that 403 to a clear health error (`APPNAME_NOT_APPROVED`) and do not retry until credentials change.
- `POST https://api.reliefweb.int/v2/disasters?appname={appname}` with a JSON query: filter `status` ∈ (`alert`, `current`), `sort` `date.changed:desc`, `limit` 100, fields `id, name, status, glide, type.name, country.iso3, primary_country.iso3, primary_country.location, date.event, date.changed, url, description` (verify field names against the API docs).
- Record: kind `disaster`, externalId `rw:{id}`, `eventTime` = `date.event`, `observedAt` = `date.changed`, Point from `primary_country.location` (approximate), payload `name, status, glide, types, countries, url, description` (≤ 4 KB). `embedFields` `name, description`.
- Interval 6 h (catalogue: daily is enough), budget 2/min, 50/day (ReliefWeb allows 1,000 calls/day per appname). Licence: ReliefWeb terms (attribution "ReliefWeb / UN OCHA"; content rights vary by source).

### 4. `unhcr-population` (POLLING)

- `GET https://api.unhcr.org/population/v1/population/?year={y}&coo_all=true&limit=1000&page={n}` and the same with `coa_all=true` (verified 2026-10: `{page, maxPages, items[]}` with fields `year, coo_iso, coo_name, coa_iso, coa_name, refugees, asylum_seekers, returned_refugees, idps, returned_idps, stateless, ooc, oip, hst`; numbers sometimes arrive as strings, `-` means not applicable).
- Records: kind `displacement`, externalId `coo:{iso3}:{year}` or `coa:{iso3}:{year}`, `eventTime` = 1 January of `year` (measurement period), `observedAtMode` RECEIVED (no upstream update time), country centroid geometry, numeric payload with absent keys for `-`. Fetch the latest two years (`yearsBack` param, default 2).
- Interval 24 h, budget 5/min, 100/day. Licence CC BY 4.0 (UNHCR).

### 5. `who-don` (POLLING)

- `GET https://www.who.int/api/emergencies/diseaseoutbreaknews?$orderby=PublicationDate desc&$top=50` (OData; verified fields `Id, DonId, Title, OverrideTitle, UseOverrideTitle, PublicationDate, LastModified, UrlName, Summary, Overview, Epidemiology, Assessment, Advice, Response, FurtherInformation`).
- Title is `<disease> – <country>`: split on the last ` - `/` – `; resolve the country through 040 names; multi-country titles produce a MultiPoint of centroids.
- Record: kind `disease-outbreak`, externalId `Id`, `eventTime` = `PublicationDate`, `observedAt` = `LastModified`, payload `donId, title` (override title when `UseOverrideTitle`), `disease, countries, url` (`https://www.who.int/emergencies/disease-outbreak-news/item/{UrlName}`), `summary` (stripped, ≤ 4 KB). `embedFields` `title, summary`.
- Interval 6 h, budget 2/min, 50/day. Licence: record WHO's terms exactly after checking them (WHO content is generally CC BY-NC-SA 3.0 IGO).

### 6. `copernicus-ems` (POLLING)

- `GET https://rapidmapping.emergency.copernicus.eu/backend/dashboard-api/public-activations-info/?limit=50&offset={n}` (verified 2026-10: `{count, next, results[{code, countries, eventTime, name, centroid (WKT POINT), activationTime, category, lastUpdate, closed, gdacsId, n_aois, n_products}]}`). Page until an item's `lastUpdate` is older than the stored watermark.
- On a changed `lastUpdate`, fetch `public-activations/?code={code}` for `reason`, `subCategory` and `reportLink` (verified).
- Record: kind `emergency-activation`, externalId `code`, Point from the WKT centroid, `eventTime` = `eventTime`, `observedAt` = `lastUpdate`, payload `name, category, subCategory, countries, activationTime, closed, gdacsId, nAois, nProducts, reason` (≤ 4 KB), `reportLink`. `entityKeys` `gdacsId` (links to 067's `gdacs`). `embedFields` `name, reason`.
- Interval 1 h, budget 10/min, 300/day. Attribution "© European Union, Copernicus Emergency Management Service" (verify the current wording).

### 7. Volcano alert levels

- **`usgs-hans-volcanoes`**: `GET https://volcanoes.usgs.gov/hans-public/api/volcano/getMonitoredVolcanoes` (verified: every monitored volcano with `vnum, volcano_name, alert_level, color_code, sent_utc, notice_identifier, obs_abbr, notice_type_cd`). Coordinates come from `getUSVolcanoes` (verified `latitude, longitude, elevation_meters, region`), cached in adapter state and refreshed weekly. Record kind `volcano-alert`, externalId `usgs:{vnum}`, `observedAt` = `sent_utc` (UTC), payload `name, alertLevel` (NORMAL/ADVISORY/WATCH/WARNING), `aviationColor` (GREEN/YELLOW/ORANGE/RED), `observatory, noticeId, noticeUrl, elevationM`. Significant: `alertLevel, aviationColor`. Interval 10 min, budget 4/min, 400/day.
- **`geonet-volcanoes`**: `GET https://api.geonet.org.nz/volcano/val` with `Accept: application/vnd.geo+json;version=2` (verified GeoJSON: `volcanoID, volcanoTitle, level` 0–5, `acc` aviation colour, `activity, hazards`). Record kind `volcano-alert`, externalId `geonet:{volcanoID}`, `observedAtMode` RECEIVED, payload `name, nzLevel, aviationColor, activity, hazards`. Interval 10 min, budget 2/min, 300/day. Licence CC BY 4.0 (GeoNet; verify).

### 8. `openfema-declarations` (POLLING)

- `GET https://www.fema.gov/api/open/v2/DisasterDeclarationsSummaries?$filter=lastRefresh gt '{watermark}'&$orderby=lastRefresh&$top=1000&$skip={n}` (verified endpoint). First run: `$filter=declarationDate ge '{now − backfillDays}'` (default 365).
- Record per designated area: kind `disaster-declaration`, externalId `id`, geometry = 040 county polygon from `fipsStateCode`+`fipsCountyCode` (`000` → state polygon; tribal or other areas → null), `eventTime` = `incidentBeginDate`, `observedAt` = `lastRefresh`, payload `femaDeclarationString, disasterNumber, declarationType, declarationDate, incidentType, declarationTitle, state, designatedArea, ihProgramDeclared, iaProgramDeclared, paProgramDeclared, hmProgramDeclared, incidentEndDate, closeoutDate`.
- **Terms:** OpenFEMA's terms forbid using the data to determine individual eligibility or implying FEMA endorsement. Add a nullable descriptor field `llmNotice` (if not present) that 028/029/030 append whenever records of that source are placed in an LLM context; set it to "OpenFEMA data must not be used to determine eligibility for assistance; direct people to DisasterAssistance.gov." Add an Ask test that the notice is present in the tool result.
- Interval 6 h, budget 10/min, 200/day. Public domain.

### 9. Travel advisories

- **`travel-advisory-us`**: `GET https://cadataapi.state.gov/api/TravelAdvisories` (verified 2026-10: array of `{Title, Link, Category, Summary, id, Published, Updated}`, 223 entries, `Category` holds a **FIPS 10-4** code, e.g. Yemen = `YM`, resolved with `CountryCentroids.byFips`). Level from `Title` with `Level (\d):\s*(.+)$` (missing → level absent, counted). Record kind `travel-advisory`, externalId `us:{iso2}`, geometry = 040 country polygon (else centroid), `observedAt` = `Updated`, payload `issuer` (`US`), `level, levelText, title, summary` (stripped, ≤ 4 KB), `url`. Significant: `level`. `embedFields` `summary`. Interval 6 h, conditional GET, budget 2/min, 20/day.
- **`travel-advisory-uk`** (optional, disabled): index `https://www.gov.uk/api/content/foreign-travel-advice`, then per-country content only when `public_updated_at` changed; map FCDO alert status to `ukAlertStatus` (verify field names; FCDO has no numeric levels, so `level` stays absent). OGL v3. Budget 10/min, 300/day.

### 10. `hdx-hapi-*` family (optional, disabled)

- Credentials `appName`, `email`; send `app_identifier = base64("{appName}:{email}")` (verified 2026-10: requests without it get 403 "Invalid app identifier").
- Reviewed resource `hdx-hapi-endpoints.json`; start with internally displaced persons and food security at admin-1 (confirm exact `/api/v2/...` paths from the HAPI docs). Records without native coordinates use the admin-1 or country centroid from 040 with `geometryApproximate`. Store the dataset's licence string in payload `licence` until 109 handles per-record licences. Interval 24 h, budget 10/min, 200/day per endpoint.
- **CDC NWSS is not part of this prompt**: the roadmap assigns it to 095. Do not add it here.

### 11. Fixtures

Capture every endpoint above with an identifiable UA (ReliefWeb and HAPI with the operator's own approved credentials from the environment, never committed); trim lists to ≤ 30 items and note dates in a README. Hand-make: a WHO title with two countries, a State advisory title without a level, an EONET event with a polygon, an OpenFEMA `000` county.

### 12. Spec, Postman, docs

- If `SourceDescriptor` in `docs/OpenAPI.json` is extended with `llmNotice`, add it (string, nullable). Add kinds `natural-event, displacement, disease-outbreak, emergency-activation, volcano-alert, disaster-declaration, travel-advisory` where enumerated. Regenerate Postman if the spec changed.
- `docs/Source-Catalog.md`: update the four rows (ReliefWeb approved-appname requirement, EONET content type), add the new ones. `docs/Operator-Guide.md`: obtaining a ReliefWeb appname and a HAPI identifier.

## Tests to write (do not run)

- Parser tests per source on real fixtures: `EonetParserTest` (content-type sniffing), `ReliefWebParserTest`, `UnhcrParserTest` (string numbers, `-`), `WhoDonParserTest` (country split, multi-country), `CopernicusEmsParserTest` (WKT centroid), `HansParserTest`, `GeoNetParserTest`, `OpenFemaParserTest`, `UsTravelAdvisoryParserTest` (level regex, FIPS→ISO for `YM`→`YE`).
- Adapter tests (WireMock): ReliefWeb 403 → `APPNAME_NOT_APPROVED` without retry; OpenFEMA watermark paging; Copernicus detail fetched only on change; HAPI `app_identifier` header; GeoNet `Accept` header.
- `CountryCentroidsFipsTest` (extends 040's tests): every FIPS code in the State fixture resolves.
- `HumanitarianIngestIT`: OpenFEMA records reach an Ask tool result with the `llmNotice`; travel advisory level change persists SIGNIFICANT.

## Acceptance criteria

- Every source registered with correct licence, time base and budget; ReliefWeb and HAPI disabled until credentials are set.
- Country-level records carry `geometryApproximate=true`.
- OpenFEMA's eligibility notice reaches every LLM context that includes its records.
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
2. **Commit** all changes with the message `090: Disasters And Humanitarian`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/090-CR-Disasters-And-Humanitarian.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `090: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
