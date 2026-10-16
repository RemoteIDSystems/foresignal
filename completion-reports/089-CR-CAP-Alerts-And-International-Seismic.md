# 089-CR-CAP-Alerts-And-International-Seismic

| | |
|---|---|
| Prompt | 089-CAP-Alerts-And-International-Seismic.md |
| Status | Complete |
| Implementation commit | 8710233301f1e60901c1c1793bf565378aa75749 |
| Date | 2026-10-14 |

## Summary

Alerting extends beyond the US and beyond the USGS earthquake feed:

- a reviewed CAP feed family (`cap-*`, 13 sources since the post-review fixes; 14 as first built) seeded offline from the WMO Register of Alerting Authorities;
- the MeteoAlarm family (`meteoalarm-*`, 38 sources) reading the JSON API;
- the WPC Excessive Rainfall Outlook (`wpc-ero-d1` … `wpc-ero-d5`) as five feature-kit specs;
- `emsc-seismic`, a WebSocket stream with an FDSN gap-fill, and the `fdsn-*` family (GEOFON, INGV);
- `usgs-shakemap`, the ShakeMap and loss-PAGER impact products.

Every CAP-shaped source uses prompt 067's `CapParser`. Both new CAP families use the shared update chain
(`CapThreadResolver`) and a common mapper (`CapAlertRecords`) in which an `Update` versions the same record and a
`Cancel` is a tombstone of the thread.

**Before this prompt, the application context did not start.** Two startup defects were found during the
assessment and fixed in the two commits immediately preceding the implementation:

- `e0207ce` — six `@Component` beans from 070–073 (`TransitFamily`, `GbfsFamily`, `CctvCatalogFamily`,
  `FeatureKitFamily`, `FeedKitFamily`, `KitSpecs`) had two public constructors and no `@Autowired`, so Spring fell
  back to a non-existent no-arg constructor; the context failed with `NoSuchMethodException` and every one of the
  252 `@IntegrationTest` classes was blocked.
- `1d92993` — `celestrak-socrates` had `recordTtl` equal to its 12 h interval, so no keyframe could satisfy
  `keyframe <= ttl − interval − 30s` and `SourceRegistry` rejected the descriptor; the TTL is now 24 h (owner
  decision).

`ForesignalApplicationTests` is green again. The `FamilyDefaultsTest` classification gap from 062–064 (065
Issue 4) was **not** fixed here: it needs retention-class decisions for the space sources and is a checkpoint-097
item; it is unchanged and still red.

**1. CAP feed family (`cap-*`).**

- **`scripts/build-cap-register.py` (new).** Reads the WMO Atom index and each authority record's "CAP Feed URL(s)"
  section and writes `build/cap-candidates.json` (304 authorities, 160 with feeds). Stdlib only; the register is
  never read at runtime.
- **`src/main/resources/sources/cap-feeds.json` (new).** A human-reviewed subset: 13 feeds across WMO regions
  (DE, FI, NO, IS, AU, HK, SG, KR, IN, BR, NZ, KE, NG), ids `cap-<iso2>-<authority>` (for example `cap-de-dwd`).
  As first built there were 14, with language-suffixed ids and DWD registered twice (`cap-de-dwd-de`,
  `cap-de-dwd-en`); see the post-review fixes. Every feed is keyless, HTTPS, documented, and carries a
  licence, a terms URL and a review date. US NWS is skipped (047) and European MeteoAlarm feeds are skipped (the
  `meteoalarm-*` family). `CapFeeds` loads and validates the list.
- **`CapFeedAdapter`.** A conditional GET of the Atom/RSS index (reusing 073's `FeedParser`), then a serial fetch
  of only the CAP documents whose link is new and on the feed's own host (or reviewed `documentHosts`), at most 5 a
  run to fit the 6-a-minute budget with the index (the first build said ≤ 50, which the budget could never allow);
  a bounded per-source seen-set lives in adapter state. Each
  document is parsed with `CapParser`, threaded with `CapThreadResolver`, and mapped with `CapAlertRecords`; a
  `Cancel` is a tombstone. Kind `cap-alert`; 5 min · 6/min, 1,000/day; retention 1825 d (`event`);
  `entityKeys cap-identifier`; `includeTests` param.
- **`CapFeedAdapterFamily`.** One source per reviewed feed, validated at startup.

**2. `meteoalarm-*`.** `MeteoAlarmCountries` reviews the 38 members (slugs verified live 2026-10-14;
`republic-of-north-macedonia` and `czechia` are the correct slugs for the two that 404 under other names).
`MeteoAlarmJsonAdapter` converts the JSON alert to the CAP model; `MeteoAlarmAdapter` reads
`feeds-<slug>` and reuses the same thread/mapping path. 15 min · 2/min, 200/day, `maxResponseBytes` 32 MB.
Licence "MeteoAlarm / EUMETNET free re-use with attribution", terms
<https://www.meteoalarm.org/en/live/page/terms-of-use>.

**3. WPC ERO.** `src/main/resources/kits/features/wpc-ero.json` (five `arcgis` specs). `dn → category`
(MRGL/SLGT/MDT/HIGH); `observedAt = issueTime`, `validFrom = startTime`, `validUntil = endTime`.
`FeatureSpecValidator.ID_PATTERN` gains the reviewed `wpc-ero-` prefix (as 088 added `imf-portwatch`).

**4. `emsc-seismic` + `fdsn-*`.** `EmscSeismicAdapter` (STREAMING) opens the standing_order WebSocket; on every
(re)connect `EmscSession.gapFill()` queries the FDSN event service for events since a cursor stored in adapter
state (`emsc:lastUpdate`, `updatedafter = cursor − 5 min`) and merges them. Since the post-review fixes the gap-fill
runs right after the socket opens, pages (`orderby=time-asc`, `limit=500`, `offset`) until complete, and the cursor
does not move until it has completed; as first built it was one unpaged request whose failure only logged while the
stream advanced the cursor past the gap, so a reconnect could lose events. Dedup is by **version**
(`unid@lastupdate`), so a re-delivered version is dropped but a later `update` is a new record. Reconnect 15 s →
10 min; a connection silent for 10 min is recycled; `zeroResultsNormal`. `FdsnEventFamily` + `FdsnTextParser` read
GEOFON and INGV `format=text` (columns by header name; `Depth/km` vs `Depth/Km`, optional `EventType`),
`updatedafter` where supported, paged with `offset` until a short page, HTTP 204 as no matches. Reviewed list `src/main/resources/sources/fdsn-event-services.json`; IRIS is
excluded (event service retired, 404).

**5. `usgs-shakemap`.** The FDSN index (`producttype=shakemap&updatedafter=…&minmagnitude=…`) is polled; the
detail document is fetched only when an event's `updated` changes, oldest change first, at most 9 events a run;
an event is marked seen only once finished (as first built, a failed or budget-refused request still marked it seen). It writes `earthquake-impact` (externalId event
id, PAGER levels, maxMMI, version) and `shakemap-mmi` (`{eventId}:mmi{level}`, a MultiLineString contour
simplified with `TopologyPreservingSimplifier` to ≤ 10,000 vertices). A level that disappears in a newer version is
tombstoned. `minMagnitude` param (default 4.5); 10 min · 20/min, 1,500/day.

**6. Fixtures.** Live captures 2026-10-14 for DWD (index + two CAP documents), MeteoAlarm Greece (20 warnings),
GEOFON, INGV, EMSC gap-fill, all five ERO layers, and the ShakeMap index/detail/contours. The CAP update chain
(`chain-alert/update/cancel/exercise.xml`) is hand-made and marked so in its README.

**7. Spec, docs.** No new operations. The four kinds (`cap-alert`, `rainfall-outlook`, `earthquake-impact`,
`shakemap-mmi`) were already enumerated API-first; their payloads and notes are filled in and a changelog entry
added. `docs/Source-Catalog.md` gains "International alerts and seismic (prompt 089)" (the CAP table with accepted
and rejected feeds, MeteoAlarm, ERO, EMSC + FDSN, ShakeMap). `docs/Adapter-Guide.md` §18 gains "CAP update chains
(prompt 089)". `docs/Invariants.md` SRC-1 gains `EmscStreamTest` and `CapFeedAdapterFamilyTest`.

## Files created / modified

**Created, main**
- `scripts/build-cap-register.py`
- `src/main/resources/sources/cap-feeds.json`, `src/main/resources/sources/fdsn-event-services.json`
- `src/main/resources/kits/features/wpc-ero.json`
- `source/adapters/cap/` (`package-info`, `CapFeed`, `CapFeeds`, `CapAlertRecords`, `CapFeedAdapter`,
  `CapFeedAdapterFamily`)
- `source/adapters/meteoalarm/` (`package-info`, `MeteoAlarmCountries`, `MeteoAlarmJsonAdapter`,
  `MeteoAlarmAdapter`, `MeteoAlarmFamily`)
- `source/adapters/seismic/` (`package-info`, `SeismicRecords`, `FdsnService`, `FdsnServices`, `FdsnTextParser`,
  `FdsnEventAdapter`, `FdsnEventFamily`, `EmscSeismicAdapter`, `EmscSession`)
- `source/adapters/usgs/UsgsShakeMapAdapter.java`

**Modified, main**
- `source/kit/feature/FeatureSpecValidator.java` (ID_PATTERN gains `wpc-ero`)

**Created, test**
- `cap/{CapFeedsResourceTest, CapUpdateChainTest, CapFeedAdapterFamilyTest, InternationalAlertsIngestIT}`
- `meteoalarm/MeteoAlarmJsonAdapterTest`, `seismic/{FdsnTextParserTest, EmscStreamTest, SeismicTestContext}`,
  `usgs/ShakeMapAdapterTest`, `kit/feature/WpcEroKitSpecTest`
- fixtures `cap-de-dwd/` (named `cap-de-dwd-de/` until the post-review fixes), `meteoalarm-greece/`, `fdsn-geofon/`, `fdsn-ingv/`, `emsc-seismic/`,
  `usgs-shakemap/`, `wpc-ero-d1..d5/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (five 089 adapters classified, `event`)
- `source/kit/feature/FeatureSpecValidatorTest.java` (`shipped` expects the five `wpc-ero-*` ids)
- `testsupport/weather/WeatherIngestSupport.java` (089 base-URL properties and SOURCES)

**Docs**
- `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Invariants.md`

## Deviations from the prompt

1. **CAP `externalId` is the update-chain thread, not the bare root identifier.** The prompt says
   `externalId = rootIdentifier`. The shared `CapThreadResolver` (067) returns `{sender}:{identifier}` of the root
   thread, which is collision-safe and is the "shared update-chain rule" the acceptance criteria require. The
   record's own `identifier` stays in the payload.
2. **`cap-inline` is supported but no reviewed feed uses it.** Every committed feed is an `atom-index` or
   `rss-index` whose items link CAP documents. `cap-inline` (the URL is one CAP document) is implemented for a
   future feed; it is not in the reviewed list.
3. **MeteoAlarm EMMA_ID boundaries are not bundled.** A geocode-only MeteoAlarm area gets null geometry with
   `geometryFromGeocode=false` (no remote lookup), because whether MeteoAlarm's boundary data may be
   redistributed is unresolved (owner question).
4. **WPC ERO `idField` is `category`, not `dn`.** The feature-kit validator requires `idField` to name a payload
   **target**; `dn` is the upstream attribute, mapped to the target `category` via a value map. The id is therefore
   the category, which is the prompt's intent ("one polygon per category per day").
5. **ERO time fields are `auto` + UTC.** The layer's `issue_time`/`start_time`/`end_time` are
   `esriFieldTypeString` in `yyyy-MM-dd HH:mm:ss` (UTC), not epoch numbers, so the kit reads them with
   `format: auto`, `zone: UTC`; the payload targets are `issueTime`, `startTime`, `endTime`.
6. **EMSC dedup is by version (`unid@lastupdate`), not by `unid`.** Deduping by `unid` alone would drop a
   legitimate `update`; the version key drops a re-delivered message but lets a newer `lastupdate` through.
7. **ShakeMap contour simplification grows the tolerance** from 0.001°, doubling until the geometry is under
   10,000 vertices (or 10° is reached), rather than a fixed sequence.
8. **ERO retention is 90 d** (the prompt names none); `event`-class windows are longer but this is not an event.
   Owner question.
. **CAP `documentHosts`** (post-review): an optional, reviewed list in `cap-feeds.json` of further hosts an index
    may link documents on. The prompt has no such field; it exists so a feed whose documents live elsewhere can be
    admitted by review rather than by following whatever the index says. No committed feed uses it.
11. **FDSN cursor without an update time** (post-review): the prompt's review asked for "the max updated time seen",
    but `format=text` carries none, so the cursor moves to the start of the completed pass minus 5 minutes.
12. **Undisclosed in the first report:** the CAP ids carried a language suffix and DWD was registered twice, and
    `docs/Source-Catalog.md` recorded the same. The prompt's `cap-<iso2>-<authority>` is now followed.

## Issues found / open questions

1. **The application context was broken at 088** (the two constructor/TTL defects above). Fixed before 089.
   `FamilyDefaultsTest` (the 062–064 classification gap) is still red and remains a 097 item.
2. **MeteoAlarm EMMA_ID boundary licence** (Deviation 3): should Foresignal bundle the EMMA_ID boundaries?
3. **WPC ERO retention class** (Deviation 8): 90 d was chosen; the prompt names none.
4. **`cap-identifier` entity key** (Deviation 1 context): is the entity-key type `cap-identifier` right, or
   should CAP alerts be deduped by `identifier` across sources (099)?
5. **`usgs-shakemap` minMagnitude** default 4.5 (the prompt says default 4.5; some ShakeMap events below it are
   skipped).
6. **Unverified (143):** the EC/IRIS FDSN `updatedafter` support; the EMSC WebSocket live shape (fixtures
   synthesized — the stream is continuous and was not recorded); the MeteoAlarm large-country volume
   (de/greece/it were read).
7. **Framework (post-review):** `FdsnEventAdapter`, `UsgsShakeMapAdapter` and `CapFeedAdapter` write their
   cursors and seen-sets to adapter state during `fetch`; until the fetch framework stages state writes until the
   records are written (being fixed centrally), a failed write can still skip what that run returned.
8. **Renamed source ids** (pre-release): `cap-*-en`, `cap-kr-kma-ko`, `cap-br-inmet-pt` and `cap-de-dwd-de` lost
   their suffix and `cap-de-dwd-en` is gone. Persisted rows or state under the old ids (any dev database) are
   orphaned; nothing in code, tests or OpenAPI refers to the old ids any more (searched the repository).
9. **Instructions in data:** none. The WMO register, the feeds, the FDSN responses and the MeteoAlarm terms pages
   were read; nothing addressed an agent.

## Tests written (not run)

- **`CapFeedsResourceTest`:** the committed list loads, validates, has ≥ 10 entries, covers several regions, and
  every entry has HTTPS + licence + terms URL + review date; a bad entry is refused.
- **`CapUpdateChainTest`:** Alert → Update → Cancel version one record and the Cancel tombstones it; an unknown
  reference starts a root; a non-`Actual` status is skipped unless `includeTests`; an inverted range is handled.
- **`CapFeedAdapterFamilyTest`** (`@IntegrationTest`, WireMock): the index is fetched, only new documents are
  fetched (a seen document is not refetched), non-`Actual` is skipped unless `includeTests`, and the reviewed
  family's descriptors validate.
- **`MeteoAlarmJsonAdapterTest`:** the real Greece fixture converts with fidelity and maps to a `cap-alert`
  record; a bad node / missing `sent` is refused.
- **`WpcEroKitSpecTest`:** the five specs load and validate, the descriptors validate, and the real day-1 fixture
  maps category and validity.
- **`FdsnTextParserTest`:** GEOFON and INGV text (both depth spellings), the RECEIVED time base, empty fields and
  a header-only/unusable body.
- **`EmscStreamTest`:** create and update emit, a duplicate version is dropped, the gap-fill merges once, and one
  socket is opened.
- **`ShakeMapAdapterTest`** (`@IntegrationTest`, WireMock): one impact and its contours, PAGER mapping, the detail
  fetched only once, and simplification under the vertex cap.
- **`InternationalAlertsIngestIT`** (`@IntegrationTest`, `WeatherIngestSupport`): a CAP Alert/Update/Cancel from
  one feed becomes three versions of one record through the real executor and writer, the last a tombstone.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi`: "Your API description is valid" (warnings only).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "835 requests across 80 tag folders … covers all
  ops: True" (no change to the collection).
- `ForesignalApplicationTests` (run during the startup fix): green.

## Notes for the next prompt

1. **090 onward:** `CapParser` + `CapThreadResolver` + `CapAlertRecords` are the CAP path; the reviewed-list
   pattern (`CapFeeds`/`FdsnServices` + `FamilyDefaultsTest`) is the pattern for a static source family.
2. **097 checkpoint:**
   - first run of every 089 test;
   - `FamilyDefaultsTest` still red on the 062–064 adapters (unchanged from 065);
   - `WeatherIngestSupport` gained the 089 base-URL properties; `InternationalAlertsIngestIT` shares that context.
3. **099 (derived layers):** cross-source earthquake dedup is 099's job; `emsc-seismic` and the `fdsn-*` sources
   keep `entityKeys` empty as instructed.
4. **143:** recapture each 089 fixture per its README; record a live EMSC session; verify the MeteoAlarm large
   countries and the FDSN `updatedafter` support.
5. **144 (Flyway):** no schema change. New adapter state: `cap:index`, `cap:seen`, `cap:skippedTests`,
   `cap:includeTests`, `emsc:lastUpdate`, `emsc:gap`, `fdsn:lastUpdate`, `fdsn:pass`, `shakemap:last`,
   `shakemap:seen:{id}`, `shakemap:fail:{id}`, `shakemap:mmi:{id}`.
6. **Owner decisions pending:** the questions above, plus those carried from 012–088.

## Post-review fixes (2026-10-16)

Fixes commit: 0be8b999dd7a25d6b05067cb37fbeb58b819f8c8 (`089: fix post-review defects`). Tests written, not run;
`./gradlew compileJava compileTestJava` succeeds; the workspace US English guard ran 14 tests, OK.

1. **HIGH, FDSN 204:** `FdsnEventAdapter` threw on any non-200, but FDSN answers 204 for no matches (verified live
   2026-10-16 on GEOFON and EMSC). 204 is now an empty, successful run; `nodata` is not sent, so 404 still means a
   broken endpoint. Test: `FdsnEventAdapterTest.noContent`.
2. **HIGH, ShakeMap seen-marking:** failed or budget-refused detail/contour requests were swallowed and the event
   marked seen for good. Now an event is marked seen only when finished (or when the detail is 404/410, or after 3
   failed attempts, `shakemap:fail:{id}`); a budget refusal stops the run; events go oldest change first, at most 9 a
   run (two requests each within 20/min), which also sizes the first run's 7-day window to the budget; the cursor
   stops before the first unfinished change. Tests: `budgetRefusalRetries`, `failedDetailRetried`.
3. **MEDIUM, EMSC gap-fill:** it was one unpaged request, its failure only logged, and the stream then moved the
   cursor past the gap. Now the socket opens first, then the gap-fill pages (`orderby=time-asc`, 1-based `offset`,
   verified live) until a short page (204 empty), at most 10 pages per attempt; progress is kept in `emsc:gap` and
   retried every 2 min and on the next connection; the cursor moves only after completion. Tests:
   `EmscStreamTest.reconnectGapFillWithoutDuplicates`, `failedGapFillHoldsCursor`, `gapFillPages`,
   `gapFillNoContent`. The acceptance criterion "EMSC reconnects never lose events" holds only from this fix on.
4. **MEDIUM, FDSN paging:** pages with `limit=500&offset` until a short page; the cursor moves only when a pass
   completes, to its start − 5 min (text has no update time, Deviation 11); a budget refusal after page 1 stores the
   pass (`fdsn:pass`). Tests: `paging`, `budgetStopsCleanly`, `otherStatusFails`.
5. **MEDIUM, MeteoAlarm `sent`:** a malformed warning is skipped and counted instead of failing the country. Test:
   `MeteoAlarmAdapterTest`.
6. **MEDIUM, unchecked follow-up hosts:** CAP document links must be on the feed's host and port (or reviewed
   `documentHosts`, Deviation 10) with the feed's scheme and no user info; others are refused, marked seen and
   counted. ShakeMap detail and contour links must share the index's origin (`KitUrls.sameOrigin`). No descriptor
   field exists for HTTP hosts (`allowedHosts` is raw TCP only). On 2026-10-16 every committed feed with items
   linked its documents on its own host; HK, SG, BR and NZ had no items to check. Tests: `offHostRefused`,
   `offOriginRefused`.
7. **LOW, CAP per-run cap:** 5 a run (6/min less the index), not 50; a budget refusal stops cleanly; a backlog
   forgets the index validators so a 304 cannot strand the remaining documents. Tests: `perRunCap`,
   `conditionalIndex`, `budgetStopsCleanly`.
8. **LOW, non-`Actual` CAP documents:** kept in `cap:skippedTests` instead of the seen-set, and a change of
   `includeTests` forces an unconditional index read, so turning it on later reads them. Test: `nonActualSkipped`.
9. **LOW, MeteoAlarm `references`:** every space-separated triplet is parsed. Test: `references`.
10. **CAP ids and DWD:** ids are now `cap-<iso2>-<authority>`; DWD is one source (`cap-de-dwd`, German feed; the
    English feed duplicates its warnings under `.ENG` identifiers). `CapFeeds.problems` checks the country prefix
    and one source per authority. Fixture folder renamed `cap-de-dwd/`; `WeatherIngestSupport`,
    `InternationalAlertsIngestIT`, `CapFeedAdapterFamilyTest`, `CapUpdateChainTest` and the Source Catalog updated
    (Issue 8). `wpc-ero.json` `zeroResultsNormal` is now true (a day with no risk area is normal).
11. **Test gaps filled:** `CapUpdateChainTest.languageSelection`, `WpcEroKitSpecTest.vanishedCategoryTombstoned`,
    `CapFeedsResourceTest.idsAndAuthorities`, plus those above.

This report was corrected for the above: the "≤ 50 a run" claim, the gap-fill description, the 14 feeds and their
ids, the fixture folder and the adapter-state keys.

Implementation commit: 8710233301f1e60901c1c1793bf565378aa75749
