# 091-CR-Sanctions-And-Filings

| | |
|---|---|
| Prompt | 091-Sanctions-And-Filings.md |
| Status | Complete |
| Implementation commit | ee79d62e79e840f31899b9f657a281447e266452 |
| Date | 2026-10-14 |

## Summary

The three official sanctions lists and the SEC current-filings feed land as `sanction` and `filing` records
with no geometry. Vessel IMO/MMSI, aircraft tail numbers and UN references become entity keys for the entity
registry (098); a delisting is a tombstone, guarded against an implausible list shrink.

**Cross-cutting additions.**
- `SourceHttp.getNoFollow(URI)` (interface default + `BoundSourceHttp`): a budgeted GET that returns the 3xx,
  so a source can change-check a pre-signed redirect target cheaply.
- `SourceDescriptor.userAgentContact` (+ `UpstreamClient.userAgent`): a contact appended to Foresignal's own
  User-Agent as `; contact=<value>` (the SEC requires one). Not added to the OpenAPI schema (it is not a
  documented property there; the UA is Foresignal's own plus a suffix).

**Shared sanctions model** (`ai.foresignal.source.adapters.sanctions`).
- `Sanctions`: the `sanction` kind, party-type mapping, the IMO check-digit and nine-digit-MMSI validators, and
  the alias/identifier/address/remarks caps.
- `Xml`: secure StAX (DTDs and external entities refused, so a DOCTYPE/XXE document throws) plus a small
  per-entry tree (`readElement`) with attributes, so the large lists are never materialised whole.
- `DelistingGuard`: the 90 % shrink guard (`LIST_SHRINK_GUARD`) and the gone-id computation.

**`ofac-sdn`.** The legacy `SDN.XML`/`CONS_PRIM.XML` export (simpler than `SDN_ADVANCED.XML`, same entries). The
export 302s to a pre-signed S3 object; the publication path (query stripped) is stored and compared, so an
unchanged publication costs one request, and the pre-signed query is never stored or logged. The redirect host
is allowlisted (`*.s3.us-gov-west-1.amazonaws.com`). Downloaded with a 200 MB cap and parsed from disk.
`observedAt` = `Publish_Date`; ids `SDN:uid`/`OFAC-CONS:uid`; entity keys `imo`, `mmsi`, `aircraft-tail`.

**`uk-sanctions-list`.** The FCDO UK Sanctions List (the OFSI Consolidated List closed 28 January 2026),
change-checked with a conditional GET, downloaded with a cap and parsed from disk. `NameType = Primary Name` is
the record name; `UNReferenceNumber` is the `unReference` entity key and a cross-ref. Ids `uk:UniqueID`;
`observedAt` = `DateGenerated`; OGL v3.

**`un-sanctions`.** The consolidated XML URL 302s to an Azure Blob SAS URL; the blob host is allowlisted and the
SAS query is never logged. Individuals and entities; ids `un:REFERENCE_NUMBER`; `observedAt` = `dateGenerated`,
`eventTime` = `LISTED_ON`.

**`sec-8k`.** The EDGAR current-8-K Atom feed (ISO-8859-1). The `contactEmail` credential is appended to the
User-Agent. Item codes (e.g. `1.05`, `2.02`) come from the entry summary; accession number is the externalId;
up to three pages; rolling feed, `complete=false`.

**Delisting safety** (all three lists): the previous id set is kept in adapter state; a run tombstones the ids
that disappeared, unless the new count is below 90 % of the previous (then no tombstones, `state.degrade(
"LIST_SHRINK_GUARD")`).

**Fixtures.** Live captures 2026-10-14, trimmed to 50 entries with the XML structure intact and required cases
selected (a vessel with IMO, an aircraft, a multi-alias entry; a UK entry with a UN reference); hand-made XXE,
short-list and 8-K/A fixtures. Each with a README.

## Files created / modified

**Created, main**
- `source/adapters/sanctions/`: `package-info`, `Sanctions`, `Xml`, `DelistingGuard`, `OfacSdnAdapter`,
  `UkSanctionsAdapter`, `UnSanctionsAdapter`
- `source/adapters/sec/`: `package-info`, `Sec8kAdapter`

**Modified, main**
- `source/spi/SourceHttp.java` (`getNoFollow` default), `source/spi/SourceDescriptor.java` (`userAgentContact`),
  `collect/BoundSourceHttp.java` (`getNoFollow`), `collect/UpstreamClient.java` (UA contact suffix)

**Created, test**
- `sanctions/{OfacSdnParserTest, UkSanctionsParserTest, UnSanctionsParserTest, DelistingGuardTest,
  OfacChangeCheckTest, SanctionsIngestIT}`, `sec/{Sec8kParserTest, Sec8kAdapterTest}`
- fixtures `ofac-sdn/`, `uk-sanctions-list/`, `un-sanctions/`, `sec-8k/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (four 091 adapters classified, `event`)
- `testsupport/weather/WeatherIngestSupport.java` (four base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json` (sanction/filing notes, changelog), `docs/Source-Catalog.md` (new section),
  `docs/Operator-Guide.md` (shrink guard and credentials)

## Deviations from the prompt

1. **No descriptor `redirectHosts` field.** Cross-host redirects are restricted **in the adapter**: the
   `Location` host is checked against a reviewed allowlist before the redirect target is fetched. This keeps the
   change out of the shared HTTP client (which follows redirects with `Redirect.NORMAL`) while giving the same
   protection; the pre-signed query is never logged.
2. **`userAgentContact` is a descriptor field, not a schema property.** The OpenAPI `SourceDescriptor` has no UA
   field, so it was added to the Java descriptor only; the DTO/spec are unchanged.
3. **No explicit system event on shrink.** The prompt asks for a system event when the guard fires; adapters have
   no event publisher, so the run sets the degraded reason `LIST_SHRINK_GUARD` (visible in `/v1/sources/{id}`
   health). A richer notification would need a framework hook.
4. **OFAC legacy schema has no per-entry dates.** The legacy `SDN.XML` carries no per-entry `lastUpdated`/
   `listedOn`, so `observedAt` = `Publish_Date` (the one fixed rule) and `lastUpdated`/`listedOn` are null for
   OFAC; `significantFields lastUpdated` applies to the UK/UN lists, whose documents carry update dates.
5. **The UK/UN parsers traverse by local element name** (the exact schemas are large); the required fields
   (`UniqueID`, `UNReferenceNumber`, `Names/Name`, `REFERENCE_NUMBER`, `*_ALIAS`, `INDIVIDUAL_DOCUMENT`) are
   read explicitly.
6. **Old Source-Catalog survey rows** (`uk-ofsi-sanctions`, the older `ofac-sdn`/`sec-8k` entries) are superseded
   by the new "Sanctions and filings (prompt 091)" section rather than edited in place.

## Issues found / open questions

1. **UN URL made a HEAD 404 but a GET 302** (the earlier `curl -I` check); the documented GET URL is correct.
2. **List sizes:** OFAC SDN ≈ 29 MB (19,430 entries), UK ≈ 22 MB (2026-10-12), UN ≈ 2 MB. OFAC's
   `maxResponseBytes` is 200 MB, so a much larger future export still parses from disk.
3. **Unverified (143):** the exact UK/UN element coverage beyond the captured fixtures; the SEC feed's live
   item-summary HTML variants.
4. **Instructions in data:** none. The list exports and the SEC feed were read; nothing addressed an agent.

## Tests written (not run)

- `OfacSdnParserTest`: entries, entity keys (a validated IMO, an aircraft), cap checks, the DOCTYPE/XXE refusal,
  the IMO/MMSI validators.
- `UkSanctionsParserTest`: primary name, aliases, the UN cross-reference.
- `UnSanctionsParserTest`: individuals and entities, documents, reference ids.
- `DelistingGuardTest`: the 90 % shrink rule and the gone-id computation.
- `OfacChangeCheckTest` (WireMock): a new publication downloads once; the same publication is not downloaded again.
- `Sec8kParserTest`: accession id, item codes, 8-K/A with three items, the SEC size field.
- `Sec8kAdapterTest` (WireMock): the User-Agent carries Foresignal's identifier and the contact suffix; the feed
  is `complete=false`.
- `SanctionsIngestIT` (`@IntegrationTest`, `WeatherIngestSupport`): an unchanged UN list adds no versions; a
  delisted entry is tombstoned.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: valid (warnings only).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "835 requests across 80 tag folders … covers
  all ops: True".

## Notes for the next prompt

1. **092 onward:** `SourceHttp.getNoFollow` and `SourceDescriptor.userAgentContact` are available; the sanctions
   package shows the download-to-disk + secure-StAX + delisting-guard pattern.
2. **097 checkpoint:** first run of every 091 test; `WeatherIngestSupport` gained the 091 base-URL properties;
   `FamilyDefaultsTest` gained four entries; `SourceHttp` gained `getNoFollow` (test doubles use the default that
   refuses).
3. **098 (entity registry):** the sanctions entity keys are `imo`, `mmsi`, `aircraft-tail`, `un-reference`.
4. **143:** recapture each list fixture per its README.
5. **144 (Flyway):** no schema change. New adapter state: `ofac:path:*`, `ofac:ids:*`, `uk:list`, `uk:ids`,
   `un:path`, `un:ids`.
6. **Owner decisions pending:** whether to add a framework hook for the `LIST_SHRINK_GUARD` system event, plus
   those carried from 012–090.

Implementation commit: ee79d62e79e840f31899b9f657a281447e266452
