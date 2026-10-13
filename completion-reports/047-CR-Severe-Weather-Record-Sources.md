# 047-CR-Severe-Weather-Record-Sources

| | |
|---|---|
| Prompt | 047-Severe-Weather-Record-Sources.md |
| Status | Complete |
| Implementation commit | c67e4599db0a4d4bc8becebaa21ad76cdc6c4e22 |
| Date | 2026-10-12 |

## Summary

Four record sources now turn official severe-weather products into alertable records:
- `nws-alerts`: NWS active alerts;
- `mrms-probsevere`: MRMS ProbSevere v3 storm objects inside the places AOI;
- `spc-outlooks`: SPC day 1–3 convective outlooks;
- `awc-metars`: AWC METARs.

Every tombstone comes from a complete, successfully processed upstream response. A failed, truncated,
unparseable or older response throws, or returns unchanged, before anything is tombstoned. All seven
tasks are done. No operations were added.

**1. Hooks.** Checked against the code:
- Already present:
  - 036's `PlacesAoiService` (`aoi()`, `envelope()`, the default Texas box with a health warning,
    `PlacesAoiChangedEvent`);
  - `cooldownScope: record|rule` and `near` (017/036);
  - per-kind retention (019);
  - `validFrom`, `titleField` and `fieldSpecs` (008);
  - `ref_admin2` (040).
- Added:
  - **SPI `AreaOfInterest`** (`envelope()`, `version()`, `registerConsumer`) and **SPI
    `CountyGeometries`**, so adapters keep the Architecture §3 dependency rule. `PlacesAoiService`
    implements the first. The new `reference.CountyUnionService` implements the second: PostGIS
    `ST_Union` + `ST_SimplifyPreserveTopology(…, 0.005)`, cached per county set, cleared on reference
    reload.
  - **SPI `LiveIdSet`**: remembers the ids of the last complete snapshot per scope in adapter state and
    returns the ids that went absent. A just-gone id is reported once more on the next run, so a
    tombstone lost to a failed ingest is retried.
  - **`AdapterStateStore.degrade/clearDegraded`** and the new **`collect.AdapterStateHealthRule`**: an
    adapter can mark its source `degraded` with a reason while the fetch still succeeds.
  - **`AdapterException.httpStatus()`** (overridden by `UpstreamHttpException`), so an adapter can treat
    a 404 without importing the collector's exception.
  - **`SourceHttp.forgetValidators`**: used when a 200 body could not be used.
  - **User-Agent contact.** `InstallIdService.userAgent()` only carried the project URL. It now appends
    `; contact=<email>` from the new optional `ServerSettings.contactEmail` (validated in
    `ServerSettingsService`, in the spec). It never imitates a browser.

**2. `nws-alerts`** (`source.adapters.nws`).
- Request: `GET /alerts/active?status=actual[&area=…]` with `Accept: application/geo+json`, gzip
  through the client.
- Descriptor as specified: 60 s (min 30 s), 2/min and 1,600/day, STANDARD, ALL, UPSTREAM, RECEIVED,
  TTL null, `US-PD`, the terms URL, titleField `headline`, the embed and search fields,
  `zeroResultsNormal`. Retention is the exception: Deviation 1.
- Params: `area` (normalized to sorted upper-case codes; anything else is refused) and `minSeverity`.
- Mapping as specified, except `validUntil` (Deviation 2).
- Payload: the full allowlist, no nulls. `eventCode` is `{same, nws}`. Every string is capped at
  8 KB (UTF-8) with `…`.
- Geometry: polygon, else county union, else none, recorded in `geometrySource`.
- Lifecycle:
  - an id absent from a complete response is tombstoned, scoped per `area`;
  - an alert named in an `Update`/`Cancel` `references[].identifier` is dropped from the response, and
    so tombstoned at the superseding message's `sent`.
- The Source Catalog notes that 067 must not re-implement this source.

**3. `mrms-probsevere`** (`source.adapters.mrms`).
- Fetching: `listS3` of today's prefix with `start-after` the last processed key, plus yesterday's
  prefix until a key of today has been processed. Only the newest file is downloaded.
- Budget 4/min, 1,600/day and 300 MB/day. Retention 3 d.
- Parsing:
  - every property is a string and is converted to a number;
  - the payload follows the prompt, with `motion{speedKmh (5), bearingDeg (10, toward)}`;
  - every payload has `automated` and `notAWarning`;
  - volatile fields as specified.
- AOI filter: the polygon's bounding box must intersect the AOI envelope. The AOI version is stored
  with the cursor. When it changes and no newer file exists, the last file is processed again, so
  objects now outside are tombstoned.
- A file older than the last processed one, a failed listing or download, or a truncated body →
  no tombstones.

**4. `spc-outlooks`** (`source.adapters.spc`).
- 17 conditional GETs. Descriptor 15 min, 20/min and 2,000/day. Retention 14 d.
- `externalId` is `day{d}:{type}:{LABEL}`. ISSUE, VALID and EXPIRE map to `observedAt`, `eventTime`
  and `validUntil`. Payload `day`, `type`, `label`, `label2`, `dn`, `issue`, `valid`, `expire`,
  `title`.
- Per-file completeness:
  - a 200 tombstones the labels missing for that day and type only;
  - a 304 changes nothing;
  - a 404 degrades with "SPC layer {file} missing" and tombstones nothing;
  - any other single-layer failure is reported the same way;
  - the run fails only when every layer failed.

**5. `awc-metars`** (`source.adapters.awc`).
- A conditional GET of the cache file. The body is gzip without `Content-Encoding`, so the adapter
  inflates it.
- Descriptor: 5 min (min 1 min), 2/min, 400/day and 100 MB/day, ttlAnchor OBSERVED, TTL 3 h,
  retention 7 d. AWC's request limits are in the descriptor description (Deviation 4).
- Params: `scope` (`aoi`, `conus`, `global`) or a `stations` allowlist.
- Parsing:
  - the preamble is skipped until the header;
  - the sky pairs are read by position;
  - the newest report per station is kept;
  - the payload follows the prompt, with empty values omitted.
- Nothing is tombstoned; old reports leave through the TTL.

**6. Rule recipes.** `docs/Agent-Guide.md` has a new "Weather alerts" section with four ready-to-POST
trigger bodies:
- the tornado warning at home;
- ProbTor ≥ 30 in a county, with `geofenceRef` and `cooldownScope: rule`;
- SPC moderate risk or higher today;
- IFR/LIFR at an airport.

It states that ProbSevere is automated guidance and NWS alerts are authoritative.

**7. Spec, Postman, docs.**
- `docs/OpenAPI.json`:
  - `ServerSettings.contactEmail`;
  - `Record.x-foresignal-kinds` entries for `nws-alert`, `probsevere-object`, `spc-outlook` and `metar`,
    with payload fields, notes and examples (Deviation 5);
  - a changelog entry.
- Postman regenerated (unchanged: no operation changed).
- `docs/Source-Catalog.md`: a "Severe weather record sources" table with every verified fact. The
  `nws-alerts` row moved out of the core/hazard group. `awc-sigmets` now points at `awc-metars`.
- `docs/Adapter-Guide.md` §7: "Tombstone only from a complete, successful response", with the four
  sources as examples.
- `docs/Operator-Guide.md`: "Severe weather records" and `contactEmail`.
- `docs/Architecture.md` §3: the two new SPI types.

**Also fixed: probes moved adapter state.**
- `FetchContextFactory` gives a probe a `ProbeStateOverlay`: reads fall through, writes stay in memory.
- `BoundSourceHttp.getConditional` stores no validators while probing.
- Before this, a probe of `nws-alerts` would have consumed the live set, so the next real fetch would
  not tombstone what had gone. A probe of a conditional source would have made the next real fetch a
  304, so it never ingested what only the probe saw.

**Verified outside the tests** with scratch programs against the compiled classes (JDK 21):
- `DescriptorValidator.problems` is empty for all four descriptors.
- Every fixture parses, and every payload key is declared.
- Counts:

  | Input | Result |
  |---|---|
  | NWS national | 217 alerts, 16 polygons |
  | ProbSevere in the Texas box | 10 of 86 objects |
  | SPC | 12 risk areas; the stale day-3 `sigprob` polygon skipped as expired |
  | METARs, `aoi` | 291 stations |
  | METARs, `conus` | 2,582 stations |
  | METARs, `global` | 5,022 stations |

- With fake HTTP:
  - NWS tombstones the 9 vanished ids, re-reports them once, then nothing;
  - SPC degrades on a 404 with the exact reason and clears on the next good run;
  - SPC throws when all layers fail.

## Files created / modified

Production, created:
- `source/spi/`: `AreaOfInterest`, `CountyGeometries`, `LiveIdSet`;
- `source/adapters/nws/`: `package-info`, `NwsAlertsAdapter`, `NwsAlertsParser`;
- `source/adapters/mrms/`: `ProbSevereAdapter`, `ProbSevereParser`;
- `source/adapters/spc/`: `package-info`, `SpcOutlooksAdapter`, `SpcOutlookParser`;
- `source/adapters/awc/`: `package-info`, `AwcMetarsAdapter`, `MetarCsvParser`;
- `reference/CountyUnionService`;
- `collect/`: `AdapterStateHealthRule`, `ProbeStateOverlay`.

Production, modified:
- `source/spi/`: `AdapterException`, `AdapterStateStore`, `SourceHttp`;
- `source/adapters/mrms/package-info`;
- `collect/`: `BoundSourceHttp`, `FetchContextFactory`, `UpstreamHttpException`;
- `place/PlacesAoiService`;
- `security/InstallIdService`;
- `settings/`: `ServerSettings`, `ServerSettingsService`.

Docs and spec: `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`,
`docs/Agent-Guide.md`, `docs/Operator-Guide.md`, `docs/Architecture.md`.

Tests: listed below.
- Test support, created: `testsupport/weather/WeatherAdapterHarness`, `WeatherIngestSupport`.
- Fixtures, each directory with a README:
  - `fixtures/nws/`: `national.json`, `area-TX.json`, synthetic `edge-cases.json`;
  - `fixtures/probsevere/`: one real file and a trimmed listing;
  - `fixtures/spc/`: the 17 layers;
  - `fixtures/awc/`: the real `metars.cache.csv.gz` and synthetic `preamble.csv`.
- Modified: `FamilyDefaultsTest`, `InstallIdServiceIT`, `ServerSettingsServiceTest`.

## Deviations from the prompt

1. **`nws-alert` retention is 1825 d, not 30 d.** `docs/Conventions.md` "Analytic retention" names
   "weather and CAP alerts" in the `event` class and overrides a shorter adapter window for event kinds.
   Conventions wins over prompt text, and `FamilyDefaultsTest` enforces the class minimum.
2. **`validUntil` is the later of `expires` and `ends`, not `expires`.** Verified on the 2026-10-13
   national capture:
   - 46 of 217 active alerts had `expires` in the past while `ends` was ahead. They would have been
     stored already expired.
   - 40 had `onset` after `expires`. The normalizer would reject those as `VALID_RANGE_INVERTED`.

   When `onset` is still after the chosen expiry, `validFrom` is left out rather than losing the alert.
3. **Supersession tombstones only ids this source held.** Most `references` in the national feed (about
   90 ids) name alerts that left the feed long ago. Emitting a tombstone for each every minute would
   re-submit about 90 no-op tombstones per run. A referenced alert is dropped from the response instead,
   and is then tombstoned through the absent-id path at the superseding `sent`.
4. **"Descriptor notes" do not exist.** `SourceDescriptor` has no notes field, so AWC's limits are in
   the descriptor `description`.
5. **`x-foresignal-examples` → `x-foresignal-kinds`.** The spec's existing convention is
   `Record.x-foresignal-kinds.{kind}` with `payloadFields`, `note` and `example`. The four kinds were
   added there. The required `SourceHealth` reason field already existed (`statusReason`).
6. **Adapters reach the AOI and county outlines through new SPI types** (`AreaOfInterest`,
   `CountyGeometries`) rather than injecting `PlacesAoiService` or the reference package, to keep the
   adapter dependency rule. ProbSevere "refreshes on `PlacesAoiChangedEvent`" by comparing the stored AOI
   version on each run (re-processing the last file when it moved), not by an event listener in the
   adapter.
7. **`spc-outlooks` skips features with an empty geometry or an `EXPIRE` not after the fetch.**
   Verified: empty layers carry an empty placeholder feature, and day-3 `sigprob` still held a March 2026
   polygon.
8. **AWC visibility `6+`.** The prompt names `10+`. The live file also has `6+` (9999 m abroad), so any
   trailing `+` sets `visibilityPlus`. A VRB wind is never `VRB` in `wind_dir_degrees` (it is `0`), so
   variability is read from the raw report.
9. **`spc-outlooks` removal from the "089 group".** The Source Catalog had no `spc-outlooks` row
   anywhere, so there was nothing to remove. Prompt 089 already says `spc-outlooks` shipped in 047.
10. **Fixture capture procedures** are in each fixture directory's `README.md` (the established USGS
    pattern), not in the Java test packages. `area=TX` and national are both committed.
11. **Two framework changes beyond the prompt:**
    - probes no longer write adapter state or validators (see Summary);
    - `SourceHttp.forgetValidators` exists for unusable 200 bodies (SPC per layer, AWC).
12. **Retention class.** `FamilyDefaultsTest` gains a provisional `weather` class: `probsevere-object`
    3 d, `spc-outlook` 14 d and `metar` 7 d, exactly Weather-Imagery-Design §2.4. The Conventions table
    names none of these kinds (Issue 2).

## Issues found / open questions

**Needs the owner:**

1. **`nws-alert` retention.** Conventions (1825 d) and Weather-Imagery-Design §2.4 (30 d) disagree.
   1825 d is implemented. **Owner:** which document should change?
2. **Retention class for ProbSevere objects, SPC outlooks and METARs.** Provisional `weather` class with
   the §2.4 windows (like 046's provisional `raster`). **Owner:** keep these short windows, or give them
   a Conventions class (`state` would mean 365 d)?
3. **`validFrom` for NWS.** This prompt says `validFrom = onset`. 067 says `onset ?? effective`. Onset is
   implemented. **Owner:** which one should 067 enforce?
4. **`validUntil = max(expires, ends)`** departs from Core-Data-Model §2's NWS row (`validUntil = expires`)
   on the verified data. **Owner:** confirm, or amend §2.

**Other findings:**
- `GET /alerts/active` returned 0.94 MB (217 alerts) on a quiet evening, so the design's 2.6 MB is a busy
  day. The default 32 MB response cap covers both.
- The ingest ITs share one cached context (`WeatherIngestSupport`, one static WireMock server for all four
  base URLs and the S3 endpoint), so they add **one** Spring context in total.
- Instructions directed at an agent: none appeared in any upstream response or file. That covers the NWS
  descriptions and instructions, SPC layers, ProbSevere, AWC and the S3 listings.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `nws/NwsAlertsParserTest` | `fieldSpecs` contract on all three fixtures; the national capture (217 alerts, 16 polygons, the time base, payload); polygon vs county union (SAME → FIPS `48199`, `48241`) vs none; without `ref_admin2`; onset → eventTime/validFrom, effective fallback; max(expires, ends) and the inverted range; Update/Cancel references and dropping; 8 KB truncation (multi-byte safe); storm parameters; `minSeverity`; county UGC detection; format errors |
| `mrms/ProbSevereParserTest` | validTime → observedAt, eventTime null; string → number and quantization; motion derivation (four headings, still); label; AOI filter (10 of 86); `fieldSpecs`; format errors; `SnapshotContract` with volatile-only drift |
| `spc/SpcOutlookParserTest` | ids; ISSUE/VALID/EXPIRE; probability layers and titles; empty placeholders; the expired March polygon; the 17-layer set (day 3 = cat, prob, sigprob); `fieldSpecs` on every layer; duplicate labels merged; format error |
| `awc/MetarCsvParserTest` | the live gzip file (5,022 stations, KAUS mapped with four sky layers); VRB from the raw report; the synthetic preamble, positional sky pairs, newest per station, `null` category; AOI and allowlist filters; `fieldSpecs`; CSV quoting and format errors |
| `nws/NwsAlertsAdapterTest` (WireMock) | Accept, gzip and the UA contact; the `area` parameter and refusal; absent → tombstone; supersession tombstone at `sent`; truncated body → throws, state untouched; 503 + Retry-After → backoff, state untouched; format error; descriptor |
| `mrms/ProbSevereAdapterTest` (WireMock S3) | cold start, newest only, `start-after`; absent → tombstone at the file time; failed download → no tombstones, cursor kept; older file ignored; AOI move → re-filter and tombstones; day rollover; descriptor |
| `spc/SpcOutlooksAdapterTest` (WireMock) | 17 requests then all 304; per-file tombstones; 404 → degraded with the exact reason, no tombstones, cleared afterwards; unreadable layer forgets validators; all failed → throws; descriptor |
| `awc/AwcMetarsAdapterTest` (WireMock) | conditional GET (200 then 304, both validators sent); scopes and allowlist; garbled body forgets validators; perMinute budget refuses the third request; descriptor |
| `nws/NwsAlertsIngestIT` | 9 FIRST versions; event_time, observed_at, valid_until, valid_from, title; an expired alert stored but not tombstoned; absent → TOMBSTONE version; identical re-fetch → touches only |
| `mrms/ProbSevereIngestIT` | default AOI → 10 FIRST; a place shrinks the AOI → the same file re-filtered, the outside objects tombstoned, the inside ones not |
| `spc/SpcOutlookIngestIT` | 12 FIRST; the time base and title; re-fetch → NOT_MODIFIED |
| `awc/MetarIngestIT` | new report → version; same → touch; `valid_until` = observation + 3 h; an old report expires |
| `weather/StaleNeverClearsIT` | NWS 500 and a cut-off body, ProbSevere download 500: no TOMBSTONE versions, no exit alerts (`TriggerEngine.catchUp`), health `degraded` |
| `reference/CountyUnionServiceIT` | union by FIPS from the reference fixture, SRID, unknown and malformed codes, cache |
| `collect/AdapterStateHealthRuleIT` | degrade → `degraded` with the reason; cleared → no verdict |
| `collect/ProbeStateOverlayTest`, `source/spi/LiveIdSetTest` | the overlay never writes through; the live set, its retry, scopes, the empty snapshot; degrade helpers; `forgetValidators`; AOI box helpers |
| `InstallIdServiceIT` (added), `ServerSettingsServiceTest` (added), `FamilyDefaultsTest` (extended) | the UA contact; `contactEmail` validation; the four adapters classified |

## Compile result

- `./gradlew compileJava compileTestJava`: `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 829 requests, all operations covered
  (collection unchanged).
- No new dependencies.

## Notes for the next prompt

1. **048 (GLM, FDC, HMS):**
   - use `AreaOfInterest` for AOI filtering, comparing the stored `version()` to re-filter;
   - use `LiveIdSet.absentSince` for "tombstoned after N missing scans" style lifecycles;
   - `goes-fdc`'s 3-missing-scans rule needs a counter, not `LiveIdSet` alone.
2. **067 (Hazard-Adapters):**
   - do not re-implement `nws-alerts`;
   - its `validFrom` is `onset` (Issue 3).
3. **087 (Aviation-Safety):**
   - reuse `awc-metars`;
   - the cache-file fetch, inflate and CSV parsing live in `MetarCsvParser` (`inflate`, `split`, `Columns`)
     and can move to an `AwcSupport` as 087 says.
4. **089:** `spc-outlooks` is done.
5. **Any adapter with a conditional GET:** call `SourceHttp.forgetValidators` when a 200 body is unusable.
6. **Any adapter with a non-fatal problem:** report it with `ctx.state().degrade(reason)` and clear it on
   success.
7. **057 checkpoint:**
   - run the 047 tests;
   - the four new sources exist (disabled) in every context;
   - `WeatherIngestSupport` starts one WireMock server for the JVM and one extra cached context;
   - `CountyUnionServiceIT` loads the reference fixtures;
   - the probe overlay changes probe behavior for every source (no validator or state writes).
8. **144 (Flyway):** no new tables or columns. `adapter_state` gains the keys `live:*`, `gone:*` and
   `health.degraded`.
9. **Owner decisions pending:** Issues 1–4 above, plus those carried from 012–046.
