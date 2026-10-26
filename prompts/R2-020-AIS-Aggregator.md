# R2-020 — First-party AIS aggregator (the `ais` domain)

**Context.** v2.0 (R2) backend, Part C. Implements the **`ais`** domain of the aggregator surface built in
**R2-019**. **Adds no new operations** — it registers a `DomainAggregator` for `ais` so the existing
`listAggregators`/`getAggregator`/`updateAggregator`/`testAggregator` ops serve it. Permission `network.manage`.
Depends on R2-019 (the surface + SPI) and R2-014 (aggregator-down alerts).

**Goal.** AIS (ships) is the ADS-B twin — a distributed RF network the public sites merely aggregate. Build
first-party AIS ingestion: an **own RF receiver** (RTL-SDR + AIS) and/or **feed aggregation**, read locally.
Keep the public AIS mirror sources in the catalog but **default them OFF** once the aggregator is live.

## Read first

- `source.aggregator.DomainAggregator` SPI and the surface (R2-019) — implement `domain()="ais"`, `describe`,
  `apply`, `test`, `supersededSourceIds`.
- `source/adapters/ais/LocalAisAdapter.java` + `LocalAisFamily.java` (local AIS reception — the thing to
  promote), `source/adapters/ais/AisMqttSubscriber.java` (NMEA/JSON over MQTT from a local AIS-catcher/rtl-ais),
  `source/adapters/ais/DigitrafficAisParser.java` + `KystverketAdapter.java` (official regional feeds — keep),
  `source/adapters/aisstream/AisStreamSession.java` (the public `aisstream` feed — a mirror to default off),
  `source/adapters/maritime/` (keep).
- `source/SourceService.java` (`seedDefaults`/`defaultRow`/`autoEnable`), `source/SourceDescriptor.java`,
  `settings/AppSettingService.java`.

## Build

1. **`ais` `DomainAggregator`** (`ai.foresignal.source.aggregator` or `source/adapters/ais`). Config under
   `app_setting` key `aggregator.ais`: `enabled` (default **false**), `mode` (`receiver`/`feed`/`hybrid`),
   `endpoint` (the local AIS source — an AIS-catcher/rtl-ais NMEA/JSON TCP/UDP stream, or the MQTT topic the
   `AisMqttSubscriber` reads), domain `config` (receiver device / feed URL / decoder). When enabled, route the
   AIS reception through `LocalAisAdapter`/`AisMqttSubscriber` reading the local endpoint — the full local feed,
   no public endpoint hit.

2. **`test()`** reads a sample from the local endpoint and reports reachability + `sampleCount`
   (`AggregatorTestResult`). `status`/`lastSeenAt` from the adapter health; raise `system.aggregator_down` via
   R2-014 when configured-but-down.

3. **Supersede, don't remove.** `supersededSourceIds()` = the public AIS mirror source(s) (`aisstream`, and any
   other public global-AIS aggregator in the catalog). When the aggregator is live and `disableSuperseded` is
   set, flip them disabled via `SourceService` and mark their descriptors `autoEnable=false`. They stay as a
   zero-setup opt-in fallback. **Do not** disable the official regional feeds (Digitraffic, Kystverket) — those
   are first-party authorities, not mirrors.

**Migration.** Config in `app_setting` — **no new table** unless you persist AIS station stats (an entity may
already exist — `source/adapters/ais/AisStationStats.java`; reuse it). Only add a Flyway migration if you
introduce a new table, using the next free `V{n}` (read the directory; R2-001…R2-013 claim V14 upward).

## Contract

No new operations — the `ais` domain is served through R2-019's authored surface ops (status stays `planned`
until R2-027). If a domain `config` detail needs documenting, extend the `AggregatorConfig` description and
regenerate Postman (`python3 scripts/generate-postman.py`); **do not flip `x-foresignal-status` and do not
regenerate the SDKs**. `docs/OpenAPI.json` is the only contract.

## Docs

Add the AIS section to `docs/Network-Sourcing.md` (receiver vs. feed, the local endpoint, superseded mirrors
default-off). Update `docs/Source-Catalog.md` where the AIS mirrors' default-enable changes.

## Tests to write (do not run)

- `AisAggregatorTest` — enabling it routes reception to the local endpoint; `test()` reports a sample count;
  disabling restores prior behavior.
- `AisSupersededIT` — with the aggregator live, `aisstream` (and peers) seed/flip disabled but stay in the
  catalog; Digitraffic/Kystverket are untouched.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

The `ais` domain is live through the aggregator surface: an own RF receiver and/or feed aggregation read
locally, public AIS mirrors default off (kept as fallback), official regional feeds untouched; compile passes;
`checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-020: First-party AIS aggregator`, then
`completion-reports/R2-020-CR-AIS-Aggregator.md` with the 40-character implementation hash.
