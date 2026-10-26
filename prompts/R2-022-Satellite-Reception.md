# R2-022 — Direct satellite reception (the `satellite` domain)

**Context.** v2.0 (R2) backend, Part C. Implements the **`satellite`** domain of the aggregator surface built in
**R2-019**. **Adds no new operations** — it registers a `DomainAggregator` for `satellite`. Permission
`network.manage`. Depends on R2-019 and R2-014.

**Goal.** Receive GOES / weather satellites **off a dish** via SatDump instead of pulling NOAA mirrors. The
SatDump station plumbing already exists (prompts 049/054); promote it to a first-party aggregator. Keep the
superseded NOAA imagery pulls in the catalog but **default them OFF** once direct reception is live.

## Read first

- `source.aggregator.DomainAggregator` SPI + surface (R2-019).
- `source/adapters/satdump/SatDumpAdapter.java`, `SatDumpStation.java`, `SatDumpFamily.java`,
  `SatDumpApiParser.java`, `RfSession.java`, `StationStore.java`, `PolarPassProcessor.java`,
  `XritProducts.java` (the existing SatDump station — the thing to promote; it reads a local SatDump instance).
- `contrib/satdump/` (the local SatDump setup/reference — GPL, reference only, do not copy code).
- `source/adapters/goes/` and any NOAA/GOES imagery pull adapters (the mirrors to default off),
  `source/SourceService.java` (`autoEnable`/`seedDefaults`), `settings/AppSettingService.java`.

## Build

1. **`satellite` `DomainAggregator`**. Config under `app_setting` key `aggregator.satellite`: `enabled`
   (default **false**), `mode` (`receiver` is the norm here), `endpoint` (the local SatDump instance the
   `SatDumpAdapter` reads), domain `config` (station location, dish/SDR, satellites/products). When enabled,
   route satellite imagery ingestion through `SatDumpAdapter`/`SatDumpStation` reading the local SatDump — a
   direct dish receive, no NOAA mirror hit.

2. **`test()`** probes the local SatDump endpoint and reports reachability + `sampleCount` (passes/products
   seen). `status`/`lastSeenAt` from `SatDumpStation` health; `system.aggregator_down` via R2-014 when
   configured-but-down.

3. **Supersede, don't remove.** `supersededSourceIds()` = the NOAA/GOES imagery pull sources that direct
   reception replaces (the `goes` and any NOAA weather-sat mirror adapters). When the aggregator is live and
   `disableSuperseded` is set, flip them disabled and mark their descriptors `autoEnable=false`. They remain a
   zero-setup opt-in fallback for stations without a dish.

**Migration.** Config in `app_setting`; reuse the existing SatDump station/station-location persistence
(`StationStore`, `DcpLocationRepository`). **No new table** expected; only add a Flyway migration (next free
`V{n}`, read the directory; R2-001…R2-013 claim V14 upward) if you introduce one.

## Contract

No new operations — the `satellite` domain is served through R2-019's authored surface ops (status stays
`planned` until R2-027). Extend the `AggregatorConfig` description if a `config` detail needs it and regenerate
Postman (`python3 scripts/generate-postman.py`); **do not flip `x-foresignal-status` and do not regenerate the
SDKs**. `docs/OpenAPI.json` is the only contract.

## Docs

Add the satellite section to `docs/Network-Sourcing.md` (dish/SDR + SatDump station, superseded NOAA pulls
default-off). Update `docs/Source-Catalog.md` where default-enable changes.

## Tests to write (do not run)

- `SatelliteAggregatorTest` — enabling routes imagery ingestion to the local SatDump; `test()` reports a sample
  count; disabling restores prior behavior.
- `SatelliteSupersededIT` — with the aggregator live, the NOAA/GOES mirror pulls seed/flip disabled but remain
  in the catalog.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

The `satellite` domain is live through the aggregator surface: GOES/weather sats received off a dish via a local
SatDump, superseded NOAA pulls default off (kept as fallback); compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. Do not copy SatDump (GPL) code — reference only. **Stage only this
prompt's files.**

## Commit

`R2-022: Direct satellite reception`, then
`completion-reports/R2-022-CR-Satellite-Reception.md` with the 40-character implementation hash.
