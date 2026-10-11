# 091 — Sanctions And Filings

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/091-CR-Sanctions-And-Filings.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/090-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `ofac-sdn`, `sec-8k` (World Monitor survey) and `uk-ofsi-sanctions` / `un-sanctions` (Velocity survey)
- `docs/Product-Charter.md` FR-15 (delisting writes a tombstone)
- `docs/Core-Data-Model.md` §2 ("Catalogue and reference" row: the descriptor picks the publication date or RECEIVED, never a per-record fallback), §3 (payload limits: 64 KB, 512 keys), §4 (hashing)
- Completion Reports of 005 (`download` with byte cap, redirect policy), 008 (`entityKeys`, `titleField`). The entity registry (098) comes later and links sanctioned vessels to AIS through the entity keys chosen here.
- Upstream: https://ofac.treasury.gov/sanctions-list-service, https://www.gov.uk/government/publications/the-uk-sanctions-list, https://www.gov.uk/guidance/moving-to-a-single-list-for-uk-sanctions-designations-28-january-2026, https://main.un.org/securitycouncil/en/content/un-sc-consolidated-list, https://www.sec.gov/os/accessing-edgar-data

## Objective

Add the three official sanctions lists (US OFAC, UK, UN) and the SEC 8-K current-filings feed. Sanctions entries have no geometry but matter for correlation: vessel IMO/MMSI and aircraft identifiers become entity keys, and a delisting is a tombstone. The lists are large, so they are downloaded with a cap, change-checked cheaply and stream-parsed.

## Tasks

### 1. Shared sanctions model (`ai.foresignal.source.adapters.sanctions`)

- Kind `sanction`, no geometry. externalId `{list}:{upstream id}`.
- Payload (allowlisted, no nulls): `list` (`OFAC-SDN`, `OFAC-CONS`, `UK`, `UN`), `name` (primary, full), `partyType` (`individual`, `entity`, `vessel`, `aircraft`), `programs` (regimes), `aliases` (≤ 50, strings ≤ 200 chars), `nationalities`, `dateOfBirth` (string as published), `identifiers` (≤ 30 of `{type, value, country}`), `addresses` (≤ 10 of `{city, country}` only), `remarks` (≤ 2 KB), `listedOn`, `lastUpdated`, `crossRefs` (e.g. UN reference on UK entries), `imo`, `mmsi`, `callSign`, `vesselFlag`, `aircraftTail`.
- `entityKeys`: `imo`, `mmsi`, `aircraftTail`, `unReference`. Extract IMO/MMSI from identifier types ("IMO", "Vessel Registration Identification", "MMSI") and from vessel blocks; validate the IMO check digit and 9-digit MMSI.
- Time base: `observedAtMode` UPSTREAM with `observedAt` = **the list's publication date for every record** (one fixed rule per source); `eventTime` = `listedOn` when known. `lastUpdated` is significant (a real amendment produces a version).
- titleField `name`; searchFields `name, aliases, identifiers, programs`; tileProperties none; `embedFields` none (names are matched lexically, not semantically); STANDARD, ALL, no TTL.
- **Delisting safety:** each run is `complete=true` and emits explicit tombstones for ids that disappeared, **unless** the new entry count is below 90 % of the previous run's, in which case no tombstones are written, the run is marked degraded with reason `LIST_SHRINK_GUARD`, and a system event is raised. Keep the previous id set in adapter state (compressed) or derive it from current heads.
- XML parsing: StAX with `SUPPORT_DTD=false`, external entities off, coalescing on; download to a temp file through `ctx.http().download(uri, maxBytes)` (005) and parse from disk.

### 2. `ofac-sdn` (POLLING)

- `GET https://sanctionslistservice.ofac.treas.gov/api/PublicationPreview/exports/SDN.XML`. **Verified 2026-10:** the API answers 302 to a pre-signed S3 URL in `us-gov-west-1` whose path contains the publication id and date (`…/Published/<uuid>/2026-10-08/<uuid>/SDN.XML?X-Amz-…`).
- **Cheap change check:** 005's shared client follows redirects (`Redirect.NORMAL`), so add `SourceHttp.getNoFollow(URI)` (budgeted like `get`, returns the 3xx response) if it does not exist. Request with it, take `Location`, strip the query string, compare with the stored publication path; equal → `FetchResult.unchanged()`. Otherwise follow it. Add a descriptor `redirectHosts` allowlist (`*.s3.us-gov-west-1.amazonaws.com`) if 005 does not already restrict cross-host redirects per source; the pre-signed query string is never logged.
- Use the legacy `SDN.XML` schema (`publshInformation/Publish_Date` `MM/DD/YYYY`; `sdnEntry {uid, firstName, lastName, title, sdnType, remarks, programList, idList/id {idType, idNumber, idCountry}, akaList/aka {category, type, firstName, lastName}, addressList, nationalityList, dateOfBirthList, vesselInfo {callSign, vesselType, vesselFlag, vesselOwner}}`), which carries the same entries with a far simpler structure than `SDN_ADVANCED.XML`; note the choice in the adapter Javadoc. Param `lists` (string-list, default `sdn`; allowed `sdn`, `consolidated` → `CONS_PRIM.XML` the same way, as list `OFAC-CONS`).
- `maxResponseBytes` 200 MB (record the actual size in the fixture README). Interval 6 h, budget 4/min, 40/day. Public domain.

### 3. `uk-sanctions-list` (POLLING; replaces `uk-ofsi-sanctions`)

- **Verified 2026-10:** the OFSI Consolidated List closed on 28 January 2026 and the FCDO **UK Sanctions List** is now the single source of UK designations. Rename the catalogue row and use `GET https://sanctionslist.fcdo.gov.uk/docs/UK-Sanctions-List.xml` (about 22 MB, `ETag` and `Last-Modified` present: use `getConditional`).
- XML `Designations { DateGenerated (dd/MM/yyyy), Designation* { LastUpdated, DateDesignated, UniqueID, OFSIGroupID, UNReferenceNumber, Names/Name { Name1..Name6, NameType }, … } }`. Map regime, designation type, sanctions imposed, nationalities, addresses, identifiers and ship details by the element names in the fixture (the CSV header lists the same fields: `Regime Name, Designation Type, Designation source, Sanctions Imposed, Other Information, UK Statement of Reasons, Address…, D.O.B, Nationality(/ies), National Identifier number, …`). `NameType` `Primary Name` → `name`, others → `aliases`. `UNReferenceNumber` → `crossRefs` and entity key `unReference`.
- externalId `uk:{UniqueID}`, `observedAt` = `DateGenerated`. Licence OGL v3. Interval 6 h, budget 2/min, 20/day.
- Note in the catalogue that a separate list still applies to specific Russia financial and investment restrictions; it is not ingested here.

### 4. `un-sanctions` (POLLING)

- `GET https://scsanctions.un.org/resources/xml/en/consolidated.xml`. **Verified 2026-10:** 302 to an Azure Blob SAS URL (`unsolprodfiles.blob.core.windows.net/publiclegacyxmlfiles/EN/consolidatedLegacyByPRN.xml?sv=…`); allow that host in `redirectHosts` and never log the SAS query.
- XML `CONSOLIDATED_LIST[@dateGenerated] { INDIVIDUALS/INDIVIDUAL*, ENTITIES/ENTITY* }` with `DATAID, VERSIONNUM, FIRST_NAME … FOURTH_NAME, UN_LIST_TYPE, REFERENCE_NUMBER, LISTED_ON, COMMENTS1, NATIONALITY/VALUE, *_ALIAS {QUALITY, ALIAS_NAME}, INDIVIDUAL_DATE_OF_BIRTH, INDIVIDUAL_DOCUMENT {TYPE_OF_DOCUMENT, NUMBER, ISSUING_COUNTRY}, ENTITY_ADDRESS, LAST_DAY_UPDATED/VALUE`.
- externalId `un:{REFERENCE_NUMBER}` (e.g. `QDi.001`), `observedAt` = `dateGenerated`, `programs` = `UN_LIST_TYPE`. Interval 12 h, budget 2/min, 10/day. Licence: UN terms of use (public information; verify and record).

### 5. `sec-8k` (POLLING)

- `GET https://www.sec.gov/cgi-bin/browse-edgar?action=getcurrent&type=8-K&output=atom&count=100` (verified 2026-10: Atom, ISO-8859-1; entry `title` "8-K - AUTOZONE INC (0000866787) (Filer)", `link` to the filing index, `summary` HTML with `Filed`, `AccNo`, `Size` and one `Item N.NN: title` line per item, `updated`, `category term` = form type, `id` = `urn:tag:sec.gov,2008:accession-number=…`).
- **User-Agent:** SEC requires a declared UA with contact details. Param `contactEmail` (required). If 005's client has no per-source UA contact suffix, add one: the UA stays `Foresignal/<version> (+https://foresignal.ai; install=<id>)` with `; contact=<email>` appended. Never use another product's UA. Respect SEC's 10 requests/second ceiling by a wide margin (budget 6/min).
- Parse with the XML declaration's encoding. Record: kind `filing`, externalId accession number, no geometry, `eventTime` = `Filed` date, `observedAt` = entry `updated`, payload `form` (`8-K`, `8-K/A`), `company, cik, items` (codes like `2.02`), `itemTitles, sizeKb, url`. `rankField` none; `embedFields` `company, itemTitles`; searchFields `company, items`. Item codes make rules such as "Item 1.05 (material cybersecurity incident)" a simple `items in […]` trigger (017).
- Paging: if all 100 entries are new, fetch `&start=100` and `&start=200` (max 3 pages). `complete=false` (rolling feed, never tombstone). Interval 10 min, budget 6/min, 600/day. Public domain.

### 6. Fixtures

Capture once (the large lists trimmed to 50 entries with the XML structure intact; keep the original size and publication date in the README): `SDN.XML`, `CONS_PRIM.XML`, `UK-Sanctions-List.xml`, the UN consolidated XML, and one 8-K Atom page. Include in the trimmed sets at least one vessel with an IMO number, one aircraft, one individual with several aliases and documents, and one UK entry with a UN reference. Hand-make: a DOCTYPE/XXE document (must throw), a list 20 % shorter than the previous (guard test), an 8-K/A entry with three items.

### 7. Spec, Postman, docs

- No new operations. Add kinds `sanction, filing` where enumerated. If `redirectHosts` or the UA contact suffix appear in `SourceDescriptor` in the spec, add them.
- `docs/Source-Catalog.md`: update `ofac-sdn` (SDN.XML, redirect check), replace `uk-ofsi-sanctions` with `uk-sanctions-list` (closure date and new URL), update `un-sanctions` (blob redirect) and `sec-8k` (contact UA). `docs/Operator-Guide.md`: the shrink guard and how to accept a genuinely large delisting (one-off `POST /v1/sources/{id}/fetch?force=true` if 013 offers it; otherwise document running the fetch twice after verifying upstream).

## Tests to write (do not run)

- `OfacSdnParserTest`: real fixture; vessel IMO/MMSI keys with check-digit validation; aliases capped at 50; DOCTYPE rejected.
- `OfacChangeCheckTest` (WireMock): identical publication path → notModified without following; new path → download; SAS/presigned query never logged (log capture).
- `UkSanctionsParserTest`: name types, UN cross-reference, ship details; `UnSanctionsParserTest`: individuals and entities, documents, reference ids.
- `DelistingGuardTest`: removal → tombstone; 20 % shrink → no tombstones, degraded, system event.
- `Sec8kParserTest`: ISO-8859-1, items parsed from the summary, accession id, 8-K/A.
- `Sec8kAdapterTest` (WireMock): UA carries the contact suffix and the Foresignal identifier; paging stops at 3 pages.
- `SanctionsIngestIT`: re-ingesting an unchanged list produces no versions; an amended `lastUpdated` produces one; a delisted entry is tombstoned (FR-15).

## Acceptance criteria

- The UK source uses the FCDO UK Sanctions List, not the closed OFSI list.
- Large lists are change-checked before download and stream-parsed from disk.
- No tombstones are written when a list shrinks implausibly.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `091: Sanctions And Filings`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/091-CR-Sanctions-And-Filings.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `091: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
