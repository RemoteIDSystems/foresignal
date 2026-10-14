# 089-CR-CAP-Alerts-And-International-Seismic

| | |
|---|---|
| Prompt | 089-CAP-Alerts-And-International-Seismic.md |
| Status | Complete |
| Implementation commit | 8710233301f1e60901c1c1793bf565378aa75749 |
| Date | 2026-10-14 |

## Summary

Alerting extends beyond the US and beyond the USGS earthquake feed:

- a reviewed CAP feed family (`cap-*`, 14 sources) seeded offline from the WMO Register of Alerting Authorities;
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
- **`src/main/resources/sources/cap-feeds.json` (new).** A human-reviewed subset: 14 feeds across WMO regions
  (DE ×2, FI, NO, IS, AU, HK, SG, KR, IN, BR, NZ, KE, NG). Every feed is keyless, HTTPS, documented, and carries a
  licence, a terms URL and a review date. US NWS is skipped (047) and European MeteoAlarm feeds are skipped (the
  `meteoalarm-*` family). `CapFeeds` loads and validates the list.
- **`CapFeedAdapter`.** A conditional GET of the Atom/RSS index (reusing 073's `FeedParser`), then a serial fetch
  of only the CAP documents whose link is new (≤ 50 a run, a bounded per-source seen-set in adapter state). Each
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
state (`emsc:lastUpdate`, `updatedafter = cursor − 5 min`, `limit=500`) and merges them. Dedup is by **version**
(`unid@lastupdate`), so a re-delivered version is dropped but a later `update` is a new record. Reconnect 15 s →
10 min; a connection silent for 10 min is recycled; `zeroResultsNormal`. `FdsnEventFamily` + `FdsnTextParser` read
GEOFON and INGV `format=text` (columns by header name; `Depth/km` vs `Depth/Km`, optional `EventType`),
`updatedafter` where supported. Reviewed list `src/main/resources/sources/fdsn-event-services.json`; IRIS is
excluded (event service retired, 404).

**5. `usgs-shakemap`.** The FDSN index (`producttype=shakemap&updatedafter=…&minmagnitude=…`) is polled; the
detail document is fetched only when an event's `updated` changes. It writes `earthquake-impact` (externalId event
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
- fixtures `cap-de-dwd-de/`, `meteoalarm-greece/`, `fdsn-geofon/`, `fdsn-ingv/`, `emsc-seismic/`,
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
9. **`FeatureSpecValidator.ID_PATTERN`** gains `wpc-ero`, as 088 added `imf-portwatch` / 087 added `faa-uas`.

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
7. **Instructions in data:** none. The WMO register, the feeds, the FDSN responses and the MeteoAlarm terms pages
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
5. **144 (Flyway):** no schema change. New adapter state: `cap:index`, `cap:seen`, `emsc:lastUpdate`,
   `fdsn:lastUpdate`, `shakemap:last`, `shakemap:seen:{id}`, `shakemap:mmi:{id}`.
6. **Owner decisions pending:** the questions above, plus those carried from 012–088.

Implementation commit: 8710233301f1e60901c1c1793bf565378aa75749
