# 091-CR-Sanctions-And-Filings

| | |
|---|---|
| Prompt | 091-Sanctions-And-Filings.md |
| Status | Complete (post-review fixes applied 2026-10-16; the `allowShrink` override needs one framework line, see Issues §1) |
| Implementation commit | ee79d62e79e840f31899b9f657a281447e266452 |
| Post-review fix commit | 4b78dc2e1a66449570889f98fc5ef42d11e97920 |
| Date | 2026-10-14 (revised 2026-10-16) |

## Summary

The three official sanctions lists and the SEC current-filings feed land as `sanction` and `filing` records
with no geometry. Vessel IMO/MMSI, aircraft tail numbers and UN references become entity keys for the entity
registry (098); a delisting is a tombstone, guarded against an implausible list shrink.

**Cross-cutting additions.**
- `SourceHttp.getNoFollow(URI)` (interface default + `BoundSourceHttp`): a budgeted GET that returns the 3xx,
  so a source can change-check a pre-signed redirect target cheaply.
- `SourceDescriptor.userAgentContact` (+ `UpstreamClient.userAgent`): a contact appended to Foresignal's own
  User-Agent as `; contact=<value>`. Since the post-review fix, a source that declares a credential named
  `contactEmail` (`SourceHttp.USER_AGENT_CONTACT_CREDENTIAL`) has that credential's value appended instead.
- Post-review: `UrlRedactor` masks the whole query of an AWS pre-signed or Azure SAS URL wherever a URL is stored
  or logged; `FetchContext.allowShrink()` (default false; `DefaultFetchContext` carries it) exposes the operator's
  one-off shrink-guard release to adapters.

**Shared sanctions model** (`ai.foresignal.source.adapters.sanctions`).
- `Sanctions`: the `sanction` kind, party-type mapping, the IMO check-digit and nine-digit-MMSI validators, the
  alias/identifier/address/remarks caps, and (post-review) `deleteQuietly` and `putIfPresent`.
- `Xml`: secure StAX (DTDs and external entities refused, so a DOCTYPE/XXE document throws) plus a small
  per-entry tree (`readElement`), so the large lists are never materialized whole.
- `DelistingGuard`: the 90 % shrink rule (`LIST_SHRINK_GUARD`) and the gone-id computation.
- `ListReconciler` (post-review): the delisting step all three lists share: tombstones for ids that disappeared;
  on an implausible shrink no tombstones, the **previous id set kept**, a degraded reason starting
  `LIST_SHRINK_GUARD`, and a `system.sanctions_list_shrink` notification; `ctx.allowShrink()` accepts the shrink.
  The id set is stored gzip-compressed and Base64-encoded behind `gz:`; the earlier plain-text value still reads.

**`ofac-sdn`.** The legacy-schema exports `SDN.XML` and, for the Consolidated (Non-SDN) list, `CONSOLIDATED.XML`
(simpler than `SDN_ADVANCED.XML`, same entries). Each export 302s to a pre-signed S3 object; the publication path
(query stripped) is stored and compared, so an unchanged publication costs one request, and a run whose every
list is unchanged returns `FetchResult.unchanged()`. A downloaded file whose SHA-256 matches the last one is not
re-ingested. Only `*.s3.us-gov-west-1.amazonaws.com` (or the configured base host) is accepted as a redirect
target. Downloaded with a 200 MB cap, parsed from disk, then deleted. `observedAt` = `Publish_Date`; ids
`SDN:uid`/`OFAC-CONS:uid`; entity keys `imo`, `mmsi` and `registration` (from `aircraftTail`; the key was renamed
from `aircraft-tail` by 098). The tail is the `Aircraft Tail Number` id, else an aircraft entry's
registration-shaped name (`EP-GOL`).

**`uk-sanctions-list`.** The FCDO UK Sanctions List (the OFSI Consolidated List closed 28 January 2026), fetched
with one conditional GET per run (capped at 100 MB) and stream-parsed from the response body. `NameType = Primary
Name` (ships: `Primary name`) is the record name; `UNReferenceNumber` is the `unReference` entity key and a
cross-ref. Mapped from the real schema: identifiers (`PassportNumber`, `NationalIdentifierNumber`,
`BusinessRegistrationNumber`, `IMONumber`), addresses (`AddressLine5`, else `AddressLine6`, as the city;
`AddressCountry`), `dateOfBirth` (first `DOB` as published), `designationSource` and `sanctionsImposed` (split on
`|`), ship `imo` and `vesselFlag`, `listedOn` (`DateDesignated` as an ISO date). Ids `uk:UniqueID`; `observedAt` =
`DateGenerated`; OGL v3.

**`un-sanctions`.** The consolidated XML URL 302s to an Azure Blob SAS URL with a fixed path and a changing SAS
query; only `unsolprodfiles.blob.core.windows.net` is accepted, and the blob is change-checked by its ETag with a
conditional GET (an unchanged list costs the redirect and a 304). Individuals and entities; ids
`un:REFERENCE_NUMBER`; `observedAt` = `dateGenerated`, `eventTime` = `LISTED_ON` (a bare date read as UTC
midnight); `listedOn`, `addresses` (`CITY`, `COUNTRY`), `dateOfBirth` (`DATE`, else `YEAR`, else
`FROM_YEAR-TO_YEAR`) and `lastUpdated` (the latest `LAST_DAY_UPDATED/VALUE`) mapped.

**`sec-8k`.** The EDGAR current-8-K Atom feed (ISO-8859-1). The `contactEmail` credential is appended to the
User-Agent by the shared client; without it (and without the optional
`foresignal.adapters.sec-8k.contact-email` fallback) the fetch is refused. Item codes (e.g. `1.05`, `2.02`) come
from the entry summary; accession number is the externalId; up to three pages, a further page only while the
previous one was full and entirely newer than the last run's newest entry; rolling feed, `complete=false`.

**Fixtures.** Live captures 2026-10-14, trimmed with the XML structure intact; hand-made XXE, short-list and 8-K/A
fixtures; each with a README. Post-review additions, all real data copied verbatim and noted in the READMEs: four
OFAC entries from the 2026-10-15 SDN.XML (a 95-alias entity, an individual with aliases and passports, a vessel
with IMO and MMSI, an aircraft with a tail-number id), `ofac-sdn/consolidated.xml` (30 entries of
`CONSOLIDATED.XML`), and the UK ship `DPR0075`.

## Files created / modified

**Created, main**
- `source/adapters/sanctions/`: `package-info`, `Sanctions`, `Xml`, `DelistingGuard`, `OfacSdnAdapter`,
  `UkSanctionsAdapter`, `UnSanctionsAdapter`; post-review: `ListReconciler`
- `source/adapters/sec/`: `package-info`, `Sec8kAdapter`

**Modified, main**
- `source/spi/SourceHttp.java` (`getNoFollow` default; post-review: `USER_AGENT_CONTACT_CREDENTIAL`),
  `source/spi/SourceDescriptor.java` (`userAgentContact`), `collect/BoundSourceHttp.java` (`getNoFollow`),
  `collect/UpstreamClient.java` (UA contact suffix; post-review: from the declared credential)
- post-review: `collect/UrlRedactor.java` (pre-signed query masking), `source/spi/FetchContext.java` and
  `collect/DefaultFetchContext.java` (`allowShrink`), `source/adapters/humanitarian/Instants.java` (bare dates)

**Created, test**
- `sanctions/{OfacSdnParserTest, UkSanctionsParserTest, UnSanctionsParserTest, DelistingGuardTest,
  OfacChangeCheckTest, SanctionsIngestIT}`, `sec/{Sec8kParserTest, Sec8kAdapterTest}`
- post-review: `sanctions/{ListReconcilerTest, SanctionsAdaptersTest}`
- fixtures `ofac-sdn/`, `uk-sanctions-list/`, `un-sanctions/`, `sec-8k/` (each with a README); post-review
  `ofac-sdn/consolidated.xml`

**Modified, test**
- `retention/FamilyDefaultsTest.java` (four 091 adapters classified, `event`)
- `testsupport/weather/WeatherIngestSupport.java` (four base-URL properties and SOURCES)
- post-review: `collect/UrlRedactorTest.java`

**Docs**
- `docs/OpenAPI.json` (sanction/filing notes, changelog; post-review: payload fields `unReference`,
  `designationSource`, `sanctionsImposed`, the `registration` key and `allowShrink` in the note),
  `docs/Source-Catalog.md`, `docs/Operator-Guide.md` (shrink guard and credentials)

## Deviations from the prompt

1. **No descriptor `redirectHosts` field.** Cross-host redirects are restricted **in the adapter**: the
   `Location` host is checked against the reviewed host before the target is fetched (OFAC:
   `*.s3.us-gov-west-1.amazonaws.com`; UN: `unsolprodfiles.blob.core.windows.net`, both narrowed post-review).
2. **`userAgentContact` is a descriptor field, not a schema property.** The OpenAPI `SourceDescriptor` has no UA
   field; the DTO/spec are unchanged.
3. **OFAC legacy schema has no per-entry dates.** `observedAt` = `Publish_Date` (the one fixed rule) and
   `lastUpdated`/`listedOn` are absent for OFAC; `significantFields lastUpdated` applies to the UK/UN lists.
4. **The UK/UN parsers traverse by local element name**; the mapped elements are listed in the Summary.
5. **Old Source-Catalog survey rows** (`uk-ofsi-sanctions`, the older `ofac-sdn`/`sec-8k` entries) are superseded
   by the "Sanctions and filings (prompt 091)" section rather than edited in place.
6. **`CONSOLIDATED.XML`, not `CONS_PRIM.XML`** (post-review). Verified 2026-10-16: `CONS_PRIM.XML` answers 200 with
   an empty body; `CONSOLIDATED.XML` 302s like `SDN.XML` to the legacy-schema list (481 entries). The fixture is
   `consolidated.xml`.
7. **UK and UN are parsed from the conditional GET's body, not a temp file** (post-review). The prompt asks for
   `getConditional` (UK) and a download to disk; there is no conditional download, and the review required one
   request. The bodies (UK about 22 MB under its 100 MB cap, UN about 2.2 MB) are stream-parsed with StAX from
   memory. OFAC (29 MB, no validators) still downloads to disk.
8. **List id `SDN`, not `OFAC-SDN`.** The prompt names the list `OFAC-SDN`; the code and the OpenAPI note use
   `SDN` (externalIds `SDN:uid`). It is a persisted wire value, so it was not renamed (see Issues §3).
9. **Extra payload keys** (post-review): `unReference` (all lists), and for the UK `designationSource` and
   `sanctionsImposed`, which carry the prompt's "designation" and "sanctions imposed" fields that the allowlist did
   not name. Added to the OpenAPI payload fields.
10. **UK city** is `AddressLine5`, else `AddressLine6`: the schema has no city element; in the 2026-10-16 capture
    line 5 holds the town and line 6 the province or, without line 5, the town.

## Issues found / open questions

1. **`allowShrink` needs one framework line.** The sanctions adapters honor `ctx.allowShrink()`, and
   `DefaultFetchContext.allowShrink(boolean)` carries it, but `FetchExecutor.run(...)` does not yet set it. The
   required change, after `ctx.chunkSink(chunks::emitChunk);` (about line 275), is
   `ctx.allowShrink(allowShrink);`. Until then `POST /v1/sources/{id}/fetch?allowShrink=true` releases the 094
   reconciliation guard but not these three sources' guard; the Operator Guide says so.
2. **State written before the download (review finding 4).** The OFAC publication path, the SHA-256 and the
   conditional-GET validators are written to adapter state during the fetch; a failed parse after that point would
   leave them pointing at a publication that was never ingested. The framework change in progress (adapter state
   and validators committed only when the fetch succeeds) fixes this; no adapter-side workaround was added.
3. **`SDN` vs `OFAC-SDN`** (Deviation 8): the prompt and the code/spec disagree on a persisted value. Which one
   should change?
4. **First run after the fix re-versions every entry once:** the newly mapped payload keys (`listedOn`,
   `addresses`, `dateOfBirth`, `identifiers`, `unReference`, …) change each record's content.
5. **List sizes:** OFAC SDN 29,231,187 bytes (19,457 entries, 2026-10-15), UK 22,051,514 bytes (2026-10-16), UN
   2,186,276 bytes (2026-10-16).
6. **Unverified (143):** the SEC feed's live item-summary HTML variants.
7. **Instructions in data:** none.

## Tests written (not run)

- `OfacSdnParserTest`: entries, entity keys, the 95-alias real entity capped at 50, the real vessel's IMO and
  MMSI, an individual's aliases and documents, the tail from the `Aircraft Tail Number` id and from a
  registration-shaped name, `consolidated.xml` as `OFAC-CONS`, the DOCTYPE/XXE refusal, the IMO/MMSI validators.
- `UkSanctionsParserTest`: primary name and aliases, the UN cross-reference, the real ship (IMO, flag,
  identifiers, sanctions imposed, designation source, `listedOn`), entity addresses, an individual's DOB and
  passports.
- `UnSanctionsParserTest`: individuals and entities, `LISTED_ON` as `eventTime`, the latest of three
  `LAST_DAY_UPDATED` values, DOB, documents and addresses.
- `DelistingGuardTest`: the 90 % rule and the gone-id computation.
- `ListReconcilerTest`: removal → tombstone; the real `sdn-short.xml` against `sdn.xml` → no tombstones, the
  baseline kept, degraded, one `system.sanctions_list_shrink` event, and still tripping on the next run;
  `allowShrink` → tombstones, a new baseline, the reason cleared; a foreign degraded reason kept; compressed and
  legacy id-set encodings.
- `OfacChangeCheckTest` (WireMock): a new publication downloads once and the same path is `notModified()`; a new
  path with identical bytes is `notModified()`; the pre-signed signature and credential never reach traces, the
  log output (captured) or adapter state; an unapproved host is refused; the shrink guard and the `allowShrink`
  override at adapter level.
- `SanctionsAdaptersTest` (WireMock): UK is one conditional GET and a 304 is `notModified()`; the UN blob at a
  fixed path with a new SAS query is a 304 and the signature is never traced; another blob account is refused.
- `UrlRedactorTest`: AWS and Azure pre-signed queries masked with and without stored secrets; ordinary queries and
  look-alike parameter names untouched.
- `Sec8kParserTest`: accession id, item codes, 8-K/A with three items, the SEC size field.
- `Sec8kAdapterTest` (WireMock): the `contactEmail` credential reaches the User-Agent with the Foresignal
  identifier and wins over the fallback property; no contact → refused with no request; paging stops at three
  pages; a full first page that is not all new does not page on.
- `SanctionsIngestIT` (`@IntegrationTest`, `WeatherIngestSupport`): against a fixed blob path with a changing SAS
  query and ETag validation, an unchanged list adds no versions (304); an amended `LAST_DAY_UPDATED` produces a
  SIGNIFICANT version; a delisted entry is tombstoned; no SAS signature in `fetch_log`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (original run and after the post-review fixes).

## Notes for the next prompt

1. **092 onward:** `SourceHttp.getNoFollow`, `SourceDescriptor.userAgentContact`, the `contactEmail` credential
   convention and `FetchContext.allowShrink()` are available; `ListReconciler` is the delisting pattern.
2. **097 checkpoint:** first run of every 091 test, including the post-review additions.
3. **098 (entity registry):** the sanctions entity keys are `imo`, `mmsi`, `registration`, `un-reference`.
4. **143:** recapture each list fixture per its README.
5. **144 (Flyway):** no schema change. Adapter state: `ofac:path:*`, `ofac:sha:*`, `ofac:ids:*` (now `gz:`-encoded),
   `uk:list` validators, `uk:ids`, `un:blob` validators, `un:ids`, `sec:newest`; `un:path` is no longer written.
6. **Owner decisions pending:** Issues §1 (the `FetchExecutor` line) and §3 (`SDN` vs `OFAC-SDN`), plus those
   carried from 012–090.

## Post-review fixes (2026-10-16)

Fix commit `4b78dc2e1a66449570889f98fc5ef42d11e97920`.

1. **HIGH, UN never updated after the first ingest.** The redirect path is fixed and only the SAS query changes,
   so comparing the query-stripped path always said "unchanged". The blob is now fetched with a conditional GET on
   its ETag (verified live: 304). `SanctionsIngestIT` now uses a fixed path with a changing query.
2. **HIGH, genuine large delistings were never tombstoned.** All three lists stored the new id set even when the
   guard tripped. `ListReconciler` keeps the previous set, raises `system.sanctions_list_shrink`, and accepts the
   shrink only on `ctx.allowShrink()` (094's override). The Operator Guide's "run the fetch twice" advice, which
   could never work, is replaced. The override still needs Issues §1.
3. **HIGH, `sec-8k` contact from an undocumented property.** The declared `contactEmail` credential now reaches
   the User-Agent through `UpstreamClient.userAgent`; the property is only a fallback; a fetch with neither is
   refused. Tested through the credential.
4. **HIGH, state saved before download.** Left to the framework change (Issues §2).
5. **MEDIUM, OFAC unchanged returned an empty complete result.** It now returns `FetchResult.unchanged()` when every
   list is unchanged.
6. **MEDIUM, pre-signed query in `fetch_log`.** No adapter-side API could keep the query out of the client's
   recorded URL, so `UrlRedactor.redact` now masks the whole query of any URL carrying `X-Amz-Signature`,
   `X-Amz-Credential`, `X-Amz-Security-Token` or `sig`. Log-capture, trace and `fetch_log` tests added.
7. **MEDIUM, UN `eventTime` always null.** `Instants.parse` reads a bare ISO date as UTC midnight.
8. **MEDIUM, UN `lastUpdated` took the first value.** It takes the latest `LAST_DAY_UPDATED/VALUE`.
9. **MEDIUM, UK downloaded twice.** One conditional GET per run (Deviation 7).
10. **MEDIUM, vessel/aircraft keys unverified.** Checked against the live files: UK ships carry
    `ShipDetails/Ship/IMONumbers/IMONumber` (the code read `ShipIMO`/`IMO`, which do not exist); OFAC uses the
    exact id type `Aircraft Tail Number` (the old match also caught `Previous Aircraft Tail Number`) and most
    aircraft without it are named by their registration. Real fixture entries added for each case.
11. **LOW.** The OFAC temp file is deleted after parsing (UK/UN no longer use one); redirect allowlists narrowed to
    the prompt's hosts. Also: `listedOn` set (UK, UN); the id set stored compressed with backward-compatible read;
    UK addresses, identifiers, DOB, designation source, sanctions imposed and ship details and UN addresses and DOB
    mapped; `CONS_PRIM.XML` (empty upstream) replaced by `CONSOLIDATED.XML`; SEC paging now requires an
    all-new full page.
12. **Report corrections.** The `contactEmail` claim (it was a Spring property), "the pre-signed query is never
    stored or logged" (it reached `fetch_log`), the implied `unchanged()` behavior (OFAC returned an empty complete
    run), the fixture claims (no UK ship, no OFAC individual, no `CONS_PRIM.XML`, no aircraft tail-number id), the
    aircraft entity key (`registration` since 098, not `aircraft-tail`), and the `OfacChangeCheckTest`
    description (it did not test logging) are corrected above; Deviation 3 of the original report ("no system
    event") is resolved.

Implementation commit: ee79d62e79e840f31899b9f657a281447e266452
