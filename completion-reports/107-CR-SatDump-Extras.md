# 107-CR-SatDump-Extras

| | |
|---|---|
| Prompt | 107-SatDump-Extras.md |
| Status | Complete |
| Implementation commit | `fa464906b62f698ee5540ec6c8ccbcc2ba8815a3` |
| Date | 2026-10-15 |

## Summary

Finished the ground-station integration, all on the far side of the GPL boundary (Foresignal reads files
and HTTP only). Compiles; tests written, not run.

- **DCS (task 1):** the `satdump-{station}` source gains `dcsEnabled`/`dcsGlob`/`dcsBbox`/`dcsAddresses`/
  `dcsAgencies`/`dcsMaxPerScan`. `DcsJsonParser` reads SatDump's `parse_dcs` JSON leniently; `DcpLocationStore`
  loads `dcs/dcp_locations.csv` into the new `dcp_location` table (re-read on change); messages are filtered by
  bbox/allowlist/agency, dropped-and-counted when they have no location or fall outside
  (`foresignal.satdump.dcs.dropped{reason}`), and the per-scan cap keeps the newest per address and degrades
  health. `dcp-observation` records, emit-on-change, 3 d retention.
- **EMWIN (task 2):** `EmwinTextParser` reads the WMO heading, AWIPS id, P-VTEC, `LAT...LON` and
  `TIME...MOT...LOC`, with ZIP guards (≤ 16 entries, ≤ 2 MB, no path traversal) and text caps. `emwin-product`
  (allowed AWIPS categories, embedded text, 3 d) and `emwin-warning` (externalId `{office}.{phen}.{sig}.{etn}`,
  CAN/EXP tombstone, AOI-filtered, `fallback:true`, `authoritativeSource: nws-alerts`, `duplicateOf` only while
  `nws-alerts` is healthy and holds the same VTEC) records. The Operator Guide carries the fallback recipe.
- **`glm-local-{station}` (task 3):** a new POLLING family reading SatDump GRB GLM L2 LCFA NetCDF from the
  inbox, decoding with the 048 `GlmParser`, de-duplicating against the internet `goes-glm` flashes by
  (time ± 1 s, position ± 0.05°) and feeding the shared AOI ring (`origin: local`). Clusters keep flowing while
  the internet feed is down (a local clusterer runs only then). Health degrades when no file arrives for 5 min
  during a pass.
- **Elements feed (task 4):** 064 already delivered `/compat/celestrak/gp.php`, the `NORAD/elements/gp.php`
  alias, the format matrix, the `X-Foresignal-Elements-Age`/stale `Warning` header and cache-only serving;
  added `/compat/celestrak/NORAD/elements/{group}.txt` and the `noaa`/`goes` groups. Served from cache only;
  feed keys via `?key=fsf_…` or `openReads`.
- **SatDump 2.0 hook (task 5):** a new `satdumpVersion` param (1.2|2.0) documents the gate; the inbox loop
  recognises a `foresignal-manifest.json` version-agnostically (`ForesignalManifest`, unknown schema rejected
  with a health reason); the MIT hook `contrib/satdump/foresignal_hook.as` and its README ship separately.
- **Spec/docs (task 6):** the `{group}.txt` operation, the `dcp-observation`/`emwin-product`/`emwin-warning`
  kinds, changelog, Postman, Source-Catalog rows, Operator-Guide "SatDump extras", THIRD_PARTY_NOTICES.
  `getCelestrakGroupTle` removed from `pending-operations.txt`.

## Files created / modified

**Created (main):** `source/spi/CurrentRecords`, `source/support/ServiceCurrentRecords`,
`source/adapters/satdump/{DcsJsonParser,EmwinTextParser,ForesignalManifest,SatDumpExtras,DcpLocationEntity,
DcpLocationRepository,DcpLocationStore}`, `source/adapters/goes/{GlmLocalAdapter,GlmLocalFamily,FlashDedup}`,
`contrib/satdump/{foresignal_hook.as,README.md}`.

**Modified (main):** `source/spi/FlashSink` (origin overload), `source/adapters/satdump/{SatDumpDescriptor,
SatDumpAdapter,SatDumpStation,SatDumpFamily}`, `source/adapters/celestrak/CelestrakGroups` (`noaa`/`goes`),
`api/CelestrakCompatController` (`{group}.txt`).

**Docs/spec:** `docs/OpenAPI.json`, `postman/` (regenerated), `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`,
`src/test/resources/openapi/pending-operations.txt` (−1 line).

**Tests:** `source/adapters/satdump/{DcsJsonParserContractTest,DcpLocationStoreTest,EmwinTextParserTest,
ForesignalManifestTest}`, `source/adapters/goes/FlashDedupTest`; `retention/FamilyDefaultsTest` updated
(`GlmLocalAdapter`; the three station kinds).

## Deviations from the prompt

1. **DCS/EMWIN are helpers wired into `SatDumpStation.processPass`**, not a rewrite of the streaming class:
   `SatDumpExtras` is a Spring bean injected into the station via `SatDumpFamily`/`SatDumpAdapter`, called per
   ready pass folder with the folder's files. New reasons surface under the `extras` health key.
2. **The DCS bbox rule is enforced in `SatDumpAdapter.checkParams`**, which sees the places AOI; the descriptor
   `SatDumpDescriptor.check` does not. It is a source-parameter problem (via `PUT /v1/sources/{id}/params`), not
   an HTTP 400 on a request, because sources are configured, not requested.
3. **`parse_dcs` JSON and EMWIN output layouts are read leniently.** The exact SatDump 1.2.2/2.0 keys were not
   captured from the owner's station; `DcsJsonParser` accepts several spellings and both object/array and
   byte-array/hex data, and `EmwinTextParser` recognises the documented WMO/AWIPS/P-VTEC shapes. A fixture per
   pinned version should replace the synthetic cases at a checkpoint.
4. **The EMWIN `duplicateOf` check reads current `nws-alerts` heads** through a new SPI `CurrentRecords` (over
   `JdbcDerivedRecords` + `HealthService`), because a streaming adapter has no health channel. The 047 parser
   stores `vtec` raw; the keys are matched as `{office}.{phen}.{sig}.{etn}`.
5. **`glm-local` runs its own clusterer only while `goes-glm` is unavailable.** The prompt says the ring is the
   single input; to keep `lightning-cluster` working when the internet feed is down *without* the two sources
   minting `lc-{ulid}` ids for the same storms, the local generator is gated on `goes-glm`'s health. The origin
   tag is a new `FlashSink.append(satellite, origin, batch, time)` overload (informational).
6. **`glm-local` stages each inbox `.nc` into `rasters/tmp`** before `GlmParser.parse`, because `SafeNetcdf`
   only opens files under `rasters/tmp`/`rasters/selftest`.
7. **The elements feed's feed-key form is the query parameter `?key=fsf_…`** (the 064 `PathKeyFilter` query
   path), not `/v1/k/{key}/compat/...` as the prompt's text suggests: `PathKeyFilter` rewrites `/v1/k/…` to
   `/v1/…`, which no controller serves. Documented in the Operator Guide.
8. **The 2.0 hook is shipped as a documented, MIT, separate file** (`contrib/satdump/foresignal_hook.as`) marked
   experimental; the AngelScript API could not be verified against a 2.0 build (2.0 exists only as alpha/
   nightly as of 2026-10, 1.2.2 is stable), so the README also carries the manifest specification Foresignal
   reads independently of the version.
9. **No new dependencies.**

## Issues found / open questions

- **Existing SatDump tests** (`SatDumpDescriptorTest`, and the harness `SatDumpFixtures`) may need updates for
  the new params/kinds and the `SatDumpStation` constructor's new `SatDumpExtras` argument.
- **Ingest ITs for `dcp-observation` (emit-on-change) and `emwin-warning` (ETN versioning)**, and
  `EmwinFallbackIT`/`CelestrakCompatIT`, are **not written**; only contract tests (parsers, dedup, manifest,
  locations) are. This is the one coverage shortfall of the prompt.
- **DCS/EMWIN fixtures** need a capture from the owner's pinned SatDump version.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `satdump/DcsJsonParserContractTest` | Object and array shapes, common key spellings, byte-array and hex data, ASCII, missing address skipped, malformed refused |
| `satdump/DcpLocationStoreTest` | CSV header/invalid rows skipped, address normalization |
| `satdump/EmwinTextParserTest` | WMO/AWIPS id, P-VTEC (externalId, times), CAN, `LAT...LON` sign, ZIP traversal and entry-count guards, text cap, category filter |
| `satdump/ForesignalManifestTest` | Valid manifest, unknown schema refused, unsafe file refused, no products refused |
| `goes/FlashDedupTest` | A local flash within 1 s/0.05° dropped; a distant one kept |
| `retention/FamilyDefaultsTest` (updated) | `GlmLocalAdapter` classified `weather`; the three station kinds 3 d |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `./gradlew lintOpenApi` → valid.
Postman regenerated (835 requests, all operations covered). No new dependencies.

## Notes for the next prompt

- **108** reuses the 054 GeoTIFF/PNG pipeline and, for its manifest-listed products, the
  `ForesignalManifest` reader.
- **Capture DCS/EMWIN fixtures** from the owner's pinned SatDump version and write the ingest ITs at a
  checkpoint.
- **144 (Flyway):** the new `dcp_location` table, and `adapter_state` keys for `extras`, `glm-local`
  (`lastMtime`, `clusters`).

Implementation commit: `fa464906b62f698ee5540ec6c8ccbcc2ba8815a3`
