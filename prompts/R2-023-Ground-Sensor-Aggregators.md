# R2-023 — First-party ground-sensor aggregators (the `ground-sensors` domain)

**Context.** v2.0 (R2) backend, Part C. Implements the **`ground-sensors`** domain of the aggregator surface
built in **R2-019**. **Adds no new operations** — it registers a `DomainAggregator` for `ground-sensors` whose
`config` selects which local sensor feeds are active. Permission `network.manage`. Depends on R2-019 and R2-014.

**Goal.** Promote the local ground-sensor receivers to first-party ingestion under one domain: Raspberry Shake
**seismic**, **SAME/EAS weather-radio** alerts, **radiosondes**, **local air quality**, **personal weather
stations** (Davis/Ecowitt), and **Meshtastic/MeshCore mesh**. Keep the superseded public mirrors in the catalog
but **default them OFF** once the matching local feed is live. (Official authorities — USGS ShakeMap, NWS — are
not mirrors and stay on.)

## Read first

- `source.aggregator.DomainAggregator` SPI + surface (R2-019).
- **Seismic:** `contrib/rsudp/` (the Raspberry Shake rsudp bridge — `foresignal_alarm.py`), and the inbound
  path it posts to; `source/adapters/usgs/UsgsShakeMapAdapter.java` (authority, keep).
- **Weather-radio:** `contrib/same/` (`receive-same.sh`, `foresignal-post`) — SAME/EAS reception.
- **Radiosondes:** `source/formats/sonde/RadiosondeRecords.java`; `radiosonde_auto_rx` is the new local decoder
  (the superseded public aggregator is sondehub/radiosondy).
- **Air quality:** `source/adapters/env/airq/AirqLocalAdapter.java` + `AirqLocalFamily.java` (local) vs. the
  public `env` mirrors (`openaq`, `sensorcommunity`).
- **PWS:** `source/adapters/local/DavisWllAdapter.java` + `EcowittAdapter.java` (prompt 069).
- **Mesh:** `source/adapters/mesh/MeshtasticLocalAdapter.java`, `MeshCoreLocalAdapter.java`,
  `MeshCoreBridgeAdapter.java`, `MeshMqttSubscriber.java`, `MeshtasticMqttAdapter.java` and
  `contrib/meshcore-bridge/` (the public Meshtastic MQTT is the mirror to default off).
- `source/SourceService.java` (`autoEnable`/`seedDefaults`), `settings/AppSettingService.java`.

## Build

1. **`ground-sensors` `DomainAggregator`**. Config under `app_setting` key `aggregator.ground-sensors`:
   `enabled` (default **false**), `mode`, and a domain `config` with a per-sensor sub-section — `seismic`,
   `same`, `radiosonde`, `airq`, `pws`, `mesh` — each with its own enable + local endpoint (the rsudp bridge,
   the SAME post target, the radiosonde_auto_rx feed, the local air-quality adapter, the Davis/Ecowitt station,
   the local Meshtastic/MeshCore bridge). When a sub-section is enabled, route that sensor's ingestion through
   the matching local adapter listed above.

2. **`test()`** probes each enabled sub-sensor's local endpoint and reports overall reachability + a
   `sampleCount` summed across them. `status`/`lastSeenAt` from the adapters' health; raise
   `system.aggregator_down` via R2-014 when a configured sub-sensor is down.

3. **Supersede, don't remove.** `supersededSourceIds()` = the public mirror per active sub-sensor: the public
   radiosonde aggregator (sondehub/radiosondy), the public air-quality mirrors (`openaq`, `sensorcommunity`),
   the public Meshtastic MQTT source, and any public PWS-network mirror. When the matching local sub-sensor is
   live and `disableSuperseded` is set, flip those source rows disabled and mark their descriptors
   `autoEnable=false`. They remain a zero-setup opt-in fallback. **Do not** disable USGS ShakeMap or NWS.

**Migration.** Config in `app_setting`; reuse existing per-sensor persistence. **No new table** expected; only
add a Flyway migration (next free `V{n}`, read the directory; R2-001…R2-013 claim V14 upward) if you introduce one.

## Contract

No new operations — the `ground-sensors` domain is served through R2-019's authored surface ops (status stays
`planned` until R2-027). Extend the `AggregatorConfig` description if the per-sensor `config` needs documenting
and regenerate Postman (`python3 scripts/generate-postman.py`); **do not flip `x-foresignal-status` and do not
regenerate the SDKs**. `docs/OpenAPI.json` is the only contract.

## Docs

Add the ground-sensors section to `docs/Network-Sourcing.md` (per-sensor local receivers + their contrib
bridges, superseded public mirrors default-off, authorities kept). Update `docs/Source-Catalog.md` where
default-enable changes.

## Tests to write (do not run)

- `GroundSensorAggregatorTest` — enabling a sub-sensor routes that sensor's ingestion to its local adapter;
  `test()` sums sample counts across enabled sub-sensors.
- `GroundSensorSupersededIT` — with a sub-sensor live, its public mirror seeds/flips disabled while USGS
  ShakeMap / NWS stay enabled; all remain in the catalog.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

The `ground-sensors` domain is live through the aggregator surface: seismic, SAME/EAS, radiosonde, air quality,
PWS and mesh read from local receivers/bridges; superseded public mirrors default off (kept as fallback);
authorities untouched; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-023: First-party ground-sensor aggregators`, then
`completion-reports/R2-023-CR-Ground-Sensor-Aggregators.md` with the 40-character implementation hash.
