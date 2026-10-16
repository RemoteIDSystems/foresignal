# 107-CR-SatDump-Extras

| | |
|---|---|
| Prompt | 107-SatDump-Extras.md |
| Status | Implemented, with open items: `EmwinFallbackIT` and `CelestrakCompatIT` are not written, and the DCS/EMWIN fixtures are synthetic (see "Issues found") |
| Implementation commit | `fa464906b62f698ee5540ec6c8ccbcc2ba8815a3` |
| Post-review fix commit | the commit `107: fix post-review defects` (2026-10-16) |
| Date | 2026-10-15 (post-review fixes 2026-10-16) |

## Summary

Finished the ground-station integration, all on the far side of the GPL boundary (Foresignal reads files
and HTTP only). Compiles; tests written, not run.

- **DCS (task 1):** the `satdump-{station}` source gains `dcsEnabled`/`dcsGlob`/`dcsBbox`/`dcsAddresses`/
  `dcsAgencies`/`dcsMaxPerScan`. `DcsJsonParser` reads SatDump's `parse_dcs` JSON leniently; `DcpLocationStore`
  loads `dcs/dcp_locations.csv` into the `dcp_location` table (re-read on change). Messages are filtered by
  bbox, allowlist and agency, dropped and counted when they have no location or fall outside
  (`foresignal.satdump.dcs.dropped{reason}`), and the per-scan cap keeps the newest per address and degrades
  health. `dcp-observation` records, 3 d retention. **DCS needs an effective bbox**: `dcsBbox`, else the
  places-AOI envelope while places exist. The default AOI box in force without places does not count
  (`AreaOfInterest.isDefault`). Without a bbox the source is refused at configuration
  (`SatDumpAdapter.checkParams`), and if places are later removed, nothing is processed and health says so.
- **EMWIN (task 2):** `EmwinTextParser` reads the WMO heading (with an optional BBB suffix), AWIPS id, P-VTEC,
  `LAT...LON` (latitude first; west longitudes negated; a five-digit longitude is ≥ 100°) and
  `TIME...MOT...LOC`, with ZIP guards (≤ 16 entries, ≤ 2 MB, no path traversal) and text caps.
  - `emwin-product`: allowed AWIPS categories; externalId `{wmoHeading}:{awipsId}:{issue}`; `observedAt` and
    `issuedAt` are the heading's issue time; embedded text; 3 d.
  - `emwin-warning`: externalId `{office}.{phen}.{sig}.{etn}`; CAN/EXP tombstone; AOI-filtered;
    `fallback:true`; `authoritativeSource: nws-alerts`; `duplicateOf` only while `nws-alerts` is available
    and holds the same VTEC.
  - The Operator Guide carries the fallback recipe.
- **DCS and EMWIN are processed incrementally.** On every inbox scan the station walks `inboxPath` for files
  matching `dcsGlob`/`emwinGlob` (relative to `inboxPath`, skipping `IMAGES` trees) and processes each file
  once it has settled (`ExtrasTracker`: unchanged size and mtime since the previous scan, processed once per
  `size|mtime`, behind a watermark). This does not depend on pass-folder readiness. EMWIN products are
  applied in issue-time order, and an older product never overrides a newer one of the same warning. A DCS
  message no newer than the last one emitted for its address is skipped. The tracker is persisted in
  `adapter_state` (`extras.files`). A remote pass upload (054) processes its DCS/EMWIN files with the pass.
- **`glm-local-{station}` (task 3):** a POLLING family that reads SatDump GRB GLM L2 LCFA NetCDF from the
  inbox, decodes it with the 048 `GlmParser` and feeds the shared AOI ring (`origin: local`).
  - De-duplication against the internet `goes-glm` flashes (time ± 1 s, position ± 0.05°) happens in
    `FlashStore`'s one append path, in both directions. The ring tracks each flash's origin, and
    same-origin flashes never de-duplicate each other.
  - A file is read once it has been unmodified for 10 s. A young file that fails to parse is retried.
  - The local clusterer runs only while `goes-glm` is unavailable, and its open clusters are tombstoned when
    `goes-glm` recovers.
  - Health degrades when no file arrives for 5 min during a pass.
- **Elements feed (task 4):** 064 already delivered `/compat/celestrak/gp.php`, the `NORAD/elements/gp.php`
  alias, the format matrix, the `X-Foresignal-Elements-Age` header, the stale `Warning` header and cache-only
  serving.
  - Added `/compat/celestrak/NORAD/elements/{group}.txt`.
  - Added the default member groups `noaa`, `goes` and `resource`. `active` is the every-run group.
  - Served from the cache only. Feed keys work via `?key=fsf_…` or `openReads`.
- **SatDump 2.0 hook (task 5):** a `satdumpVersion` param (1.2|2.0) documents the gate.
  - The inbox loop reads a pass folder's `foresignal-manifest.json` version-agnostically
    (`ForesignalManifest`; an unknown schema is rejected with a health reason) and commits its products
    (`PolarOverlays.processManifest`). A listed GeoTIFF that `polarGlob` did not match goes through the
    overlay pipeline. A listed PNG goes through the image guard and is georeferenced from its
    `bounds [w,s,e,n]` (`equirec`) as an EPSG:4326 affine frame.
  - The MIT hook `contrib/satdump/foresignal_hook.as` and its README ship separately.
- **Spec/docs (task 6):** the `{group}.txt` operation, the `dcp-observation`/`emwin-product`/`emwin-warning`
  kinds, the changelog, Postman, Source-Catalog rows, the Operator Guide "SatDump extras" section and
  THIRD_PARTY_NOTICES. `getCelestrakGroupTle` was removed from `pending-operations.txt`.

## Files created / modified

**Created (main):** `source/spi/CurrentRecords`, `source/support/ServiceCurrentRecords`,
`source/adapters/satdump/{DcsJsonParser,EmwinTextParser,ForesignalManifest,SatDumpExtras,ExtrasTracker,
DcpLocationEntity,DcpLocationRepository,DcpLocationStore}`, `source/adapters/goes/{GlmLocalAdapter,GlmLocalFamily}`,
`contrib/satdump/{foresignal_hook.as,README.md}`. (`goes/FlashDedup`, created in the implementation commit,
was removed by the post-review fixes; de-duplication lives in `FlashStore`.)

**Modified (main):**
- `source/spi/FlashSink` (origin overload) and `source/spi/AreaOfInterest` (`isDefault`)
- `place/PlacesAoiService` (`isDefault`)
- `raster/lightning/{FlashStore,FlashRing}` (cross-origin de-duplication)
- `source/adapters/satdump/{SatDumpDescriptor,SatDumpAdapter,SatDumpStation,SatDumpFamily,StationStore,
  PolarOverlays}`
- `source/adapters/celestrak/CelestrakGroups` (`noaa`/`goes`/`resource`)
- `api/CelestrakCompatController` (`{group}.txt`)

**Docs/spec:** `docs/OpenAPI.json`, `postman/` (regenerated), `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`,
`src/test/resources/openapi/pending-operations.txt` (−1 line).

**Tests:**
- `source/adapters/satdump/{DcsJsonParserContractTest,DcpLocationStoreTest,EmwinTextParserTest,
  ForesignalManifestTest,ExtrasTrackerTest,SatDumpExtrasTest}`
- `source/adapters/goes/GlmLocalAdapterTest` and `raster/lightning/FlashStoreDedupTest`
- `SatDumpIngestIT` gains the 107 ingest cases
- `retention/FamilyDefaultsTest` updated
- `testsupport/raster/RasterTestSources` wires the real extras into `satdump-itest`

## Deviations from the prompt

1. **DCS/EMWIN are helpers called by `SatDumpStation`**, not a rewrite of the streaming class. `SatDumpExtras`
   is a Spring bean injected through `SatDumpFamily`/`SatDumpAdapter`. Each station holds its own
   `ExtrasTracker`. New reasons surface under the `extras` health key; manifest and upload reasons surface
   under `polar`.
2. **The DCS bbox rule is enforced in `SatDumpAdapter.checkParams`**, which can see the places AOI;
   `SatDumpDescriptor.check` cannot. It is a source-parameter problem (`PUT /v1/sources/{id}/params`), not an
   HTTP 400 on a request, because sources are configured, not requested. At run time a missing bbox stops
   DCS processing.
3. **`parse_dcs` JSON and EMWIN output layouts are read leniently.** The exact SatDump 1.2.2/2.0 keys and
   folder layout were not captured from the owner's station. `DcsJsonParser` accepts several spellings and
   both object/array and byte-array/hex data. The globs are matched relative to `inboxPath`. A fixture per
   pinned version should replace the synthetic cases.
4. **The EMWIN `duplicateOf` check reads current `nws-alerts` heads** through the `CurrentRecords` SPI
   (`JdbcDerivedRecords` + `HealthService`), because a streaming adapter has no health channel. "Available"
   means neither failing nor disabled. Keys are matched as `{office}.{phen}.{sig}.{etn}` for any
   significance.
5. **`glm-local` runs its own clusterer only while `goes-glm` is unavailable.** This keeps
   `lightning-cluster` working when the internet feed is down, without both sources minting `lc-{ulid}` ids
   for the same storms. On recovery the local clusters are ended. The origin tag is the
   `FlashSink.append(satellite, origin, batch, time)` overload, and local flashes are archived under
   `rasters/glm/local/`.
6. **`glm-local` stages each inbox `.nc` into `rasters/tmp`** before `GlmParser.parse`, because `SafeNetcdf`
   only opens files under `rasters/tmp`/`rasters/selftest`.
7. **The elements feed's feed-key form is the query parameter `?key=fsf_…`** (the 064 `PathKeyFilter` query
   path), not `/v1/k/{key}/compat/...` as the prompt suggests. `PathKeyFilter` rewrites `/v1/k/…` to
   `/v1/…`, which no controller serves. Documented in the Operator Guide.
8. **The 2.0 hook is shipped as a documented, MIT, separate file** marked experimental. The AngelScript API
   could not be verified against a 2.0 build (2.0 is alpha/nightly only as of 2026-10; 1.2.2 is stable), so
   the README also carries the manifest specification Foresignal reads.
9. **The manifest is read only in a pass folder that also holds `dataset.json`** (the 049 readiness marker).
   A folder holding only `foresignal-manifest.json` is not found by the scanner.
10. **No new dependencies.**

## Issues found / open questions

- **Not written:** `EmwinFallbackIT` (that the "Warnings via satellite" rule fires while `nws-alerts` is
  failing) and `CelestrakCompatIT` (aliases, format matrix, stale header, feed-key access). The
  `duplicateOf` behavior is covered at unit level (`SatDumpExtrasTest.duplicateOf`), and CAN → tombstone
  plus ETN versioning by `SatDumpIngestIT`.
- **`GlmLocalAdapterTest` uses no GRB fixture:** it covers settle, retry, skip and cluster handover with
  unreadable files. Decoding is the 048 `GlmParser`, covered by `GlmParserTest`.
- **DCS/EMWIN fixtures** need a capture from the owner's pinned SatDump version.
- **Cross-origin de-duplication covers the ring's scope only.** The ring is the only in-memory index, so a
  flash outside the AOI/CONUS scope is archived by both origins (the archive is per-directory; `local` and
  `G19` are separate).
- **`emwin-warning.etn`** is emitted as the four-digit string (`"0042"`), while `docs/OpenAPI.json` types it
  as an integer. This is a wire value, so it was left unchanged. Which one should change?
- **Existing SatDump tests** (`SatDumpDescriptorTest`, `SatDumpFixtures` users) may need updates for the new
  params and kinds.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `satdump/DcsJsonParserContractTest` | Object and array shapes, common key spellings, byte-array and hex data, ASCII, missing address skipped, malformed refused |
| `satdump/DcpLocationStoreTest` | CSV header/invalid rows skipped, address normalization |
| `satdump/EmwinTextParserTest` | WMO/AWIPS id, P-VTEC (externalId, times), CAN, `LAT...LON` latitude-first order with four- and five-digit longitudes and an out-of-range latitude, `TIME...MOT...LOC` anywhere, the heading's issue time (month rollover, BBB), ZIP traversal and entry-count guards, text cap, category filter |
| `satdump/ForesignalManifestTest` | Valid manifest, unknown schema refused, unsafe file refused, no products refused |
| `satdump/ExtrasTrackerTest` | Settle after one unchanged scan, processed once, rewritten file reprocessed, watermark never passing a waiting file, restore, newest-wins per warning, DCS newest-only, pruning |
| `satdump/SatDumpExtrasTest` | EMWIN processed with no pass folder once settled and only once; issue-order application with no resurrection by a late `NEW`; `duplicateOf`; DCS refused without a bbox (default AOI not counted) and `effectiveBbox`; DCS newest-per-address once; stable product id; a synthetic manifest pass folder whose PNG becomes an EPSG:4326 frame from its bounds; bounds validation and antimeridian unwrapping |
| `goes/GlmLocalAdapterTest` | Settle and retry rules; an unsettled file does not move the watermark; a young unreadable file is retried, an old one skipped; local clusters tombstoned when `goes-glm` recovers, kept while it is down |
| `lightning/FlashStoreDedupTest` | Internet-first and local-first duplicates dropped from ring and archive; same-origin and out-of-tolerance flashes kept; origin restored by a rebuild |
| `satdump/SatDumpIngestIT` (+2) | `emwin-warning` NEW → CON versions one record by ETN and CAN tombstones it, with no pass folder; `dcp-observation` one version per new message, none for a rescan or a platform outside the bbox |
| `retention/FamilyDefaultsTest` (updated) | `GlmLocalAdapter` classified `weather`; the three station kinds 3 d |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL` (after the post-review fixes as well).
`./gradlew lintOpenApi` → valid at the implementation commit. The spec was not changed by the fixes.
No new dependencies.

## Post-review fixes (2026-10-16)

| # | Finding | Result |
|---|---|---|
| 1 (CRITICAL) | `EmwinTextParser.polygon` read longitude first | **Fixed.** Pairs are latitude first. Longitudes are negated, and a five-digit longitude (`10123`) is 101.23° W. A latitude over 90° refuses the polygon. The existing test's `x=-97.30, y=32.50` now holds; new cases were added. The `TIME...MOT...LOC` regex never matched (`matches()` on the whole text, no `Z`/`DEG`/`KT`); fixed too. |
| 2 (HIGH) | EMWIN/DCS only from ready polar pass folders; reprocessing in folder order could resurrect a canceled warning | **Fixed.** Incremental per-file processing on every scan (`ExtrasTracker`, persisted), independent of pass readiness. EMWIN is applied in issue-time order with newest-wins per warning; DCS is newest-only per address. |
| 3 (HIGH) | Flash de-duplication was one-directional | **Fixed.** It now lives in `FlashStore.append` (the shared path) and works across origins in both directions. `FlashDedup` was removed. |
| 4 (MEDIUM) | DCS ran unfiltered without a bbox; a hard-coded Texas envelope meant "no AOI" | **Fixed.** `AreaOfInterest.isDefault()` replaces the envelope literal. DCS is not processed at all without an effective bbox (health reason). |
| 5 (MEDIUM) | Manifest products parsed then discarded; bounds unused | **Fixed.** `PolarOverlays.processManifest` commits listed GeoTIFFs and PNGs. PNGs are georeferenced from `bounds` (`equirec`). |
| 6 (MEDIUM) | `glm-local`: no settle check (a partial file was lost); local clusters never ended after `goes-glm` recovered | **Fixed.** 10 s settle, retry of young unreadable files, and the watermark never passes an unsettled file. Open clusters are tombstoned on recovery. |
| 7 (LOW) | `emwin-product` id from `hashCode`, `observedAt = now`, no `issuedAt`; `resource`/`active` missing from the Celestrak defaults | **Fixed.** The id is `{wmoHeading}:{awipsId}:{issue}` (SHA-256 of the text when the heading has no time). `observedAt`/`issuedAt` are the issue time (the spec already required `issuedAt`). `resource` was added. `active` needs no member entry because it is the every-run group (`CelestrakGpAdapter` default `group`). |
| — | Tests missing | **Partly fixed.** Added ingest ITs, a synthetic manifest pass folder, tracker, store and adapter tests. `EmwinFallbackIT` and `CelestrakCompatIT` are still not written (see above). |
| — | CR status "Complete" contradicted | **Fixed.** The status now names the open items. |

Also fixed while there: the descriptor's `significantFields("address", "data")` named a key the payload does
not have. It is now `address`, `dataAscii`, `dataHex` and `failureCode`, per the prompt's "address, data,
failure code".

## Notes for the next prompt

- **108** reuses the 054 GeoTIFF/PNG pipeline and, for manifest-listed products, `ForesignalManifest` with
  `PolarOverlays.processManifest`.
- **Capture DCS/EMWIN fixtures** from the owner's pinned SatDump version, and write `EmwinFallbackIT` and
  `CelestrakCompatIT` at a checkpoint.
- **144 (Flyway):** the `dcp_location` table, and the `adapter_state` keys `extras.files` (station) and
  `lastMtime`/`clusters` (`glm-local`).

Implementation commit: `fa464906b62f698ee5540ec6c8ccbcc2ba8815a3`
