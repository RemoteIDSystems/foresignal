# R2-021 — First-party datalink aggregators (the `datalink` domain)

**Context.** v2.0 (R2) backend, Part C. Implements the **`datalink`** domain of the aggregator surface built in
**R2-019**. **Adds no new operations** — it registers a `DomainAggregator` for `datalink`. Permission
`network.manage`. Depends on R2-019 and R2-014.

**Goal.** Aircraft datalink — **ACARS/VDL/HFDL** — and **APRS** both ride distributed RF networks whose public
sites are aggregators. Promote the local decoders to first-party ingestion read from `localhost`; keep
airframes.io and APRS-IS in the catalog but **default them OFF** once local reception is live.

## Read first

- `source.aggregator.DomainAggregator` SPI + surface (R2-019).
- `source/adapters/acars/LocalAcarsAdapter.java` + `LocalAcarsFamily.java` (local ACARS/VDL/HFDL reception —
  acarsdec/dumpvdl2/dumphfdl feeding a local endpoint; the thing to promote).
- `source/formats/aprs/AprsParser.java` + `AprsPacket.java` + `AprsRecords.java` + `AprsPacketDedup.java`
  (APRS packet parse), `source/adapters/radio/LocalAprsAdapter.java` + `LocalAprsFamily.java` (own igate),
  `source/adapters/radio/AprsIsAdapter.java` + `AprsIsFilter.java` (APRS-IS — the public network to default off).
- `source/SourceService.java` (`seedDefaults`/`autoEnable`), `source/SourceDescriptor.java`,
  `settings/AppSettingService.java`.

## Build

1. **`datalink` `DomainAggregator`**. Config under `app_setting` key `aggregator.datalink`: `enabled`
   (default **false**), `mode`, and a domain `config` selecting the active local decoders — ACARS/VDL/HFDL (via
   `LocalAcarsAdapter`, reading the local decoder endpoint) and APRS (via `LocalAprsAdapter`, an own igate
   reading `formats/aprs`). `endpoint` is the local decoder/igate URL. When enabled, route datalink reception
   through the local adapters.

2. **`test()`** samples the configured local endpoint(s) and reports reachability + `sampleCount`.
   `status`/`lastSeenAt` from adapter health; `system.aggregator_down` via R2-014 when configured-but-down.

3. **Supersede, don't remove.** `supersededSourceIds()` = the public aggregators: **airframes.io**
   (ACARS/VDL/HFDL) and the **APRS-IS** source (`AprsIsAdapter`). When the aggregator is live and
   `disableSuperseded` is set, flip those source rows disabled and mark their descriptors `autoEnable=false`.
   They stay as a zero-setup opt-in fallback — nothing is removed.

**Migration.** Config in `app_setting` — **no new table** expected (reuse existing APRS/ACARS persistence). Only
add a Flyway migration if you introduce a new table, using the next free `V{n}` (read the directory;
R2-001…R2-013 claim V14 upward).

## Contract

No new operations — the `datalink` domain is served through R2-019's authored surface ops (status stays
`planned` until R2-027). Extend the `AggregatorConfig` description if a `config` detail needs it and regenerate
Postman (`python3 scripts/generate-postman.py`); **do not flip `x-foresignal-status` and do not regenerate the
SDKs**. `docs/OpenAPI.json` is the only contract.

## Docs

Add the datalink section to `docs/Network-Sourcing.md` (ACARS/VDL/HFDL decoders + APRS igate, superseded public
aggregators default-off). Update `docs/Source-Catalog.md` where default-enable changes.

## Tests to write (do not run)

- `DatalinkAggregatorTest` — enabling routes ACARS/VDL/HFDL and APRS reception to the local decoders/igate;
  `test()` reports a sample count.
- `DatalinkSupersededIT` — with the aggregator live, airframes.io and APRS-IS seed/flip disabled but remain in
  the catalog.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

The `datalink` domain is live through the aggregator surface: local ACARS/VDL/HFDL decoders + own APRS igate
read locally, airframes.io and APRS-IS default off (kept as fallback); compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-021: First-party datalink aggregators (ACARS/VDL/HFDL + APRS)`, then
`completion-reports/R2-021-CR-Datalink-Aggregators.md` with the 40-character implementation hash.
