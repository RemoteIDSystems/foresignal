# R2-023-CR-Ground-Sensor-Aggregators

| | |
|---|---|
| Prompt | R2-023-Ground-Sensor-Aggregators.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `3117a0e0d34261229ed556ee023c78c27a40d615` |
| Date | 2026-10-26 |

## What was added

- **`source/aggregator/GroundSensorAggregator`:** the `ground-sensors` `DomainAggregator` bean. With it, all five
  domains are implemented.
  - It has no `endpoint`; setting one is a 400. `config` holds one object per sensor, each with its own `enabled`:

    | Sensor | Members | Local read | Superseded |
    |---|---|---|---|
    | `seismic` | `source` (a registered PUSH source: the rsudp bridge's custom source) | that source | nothing |
    | `same` | `source` (the `contrib/same` custom source) | that source | nothing |
    | `radiosonde` | `endpoint`, `allowSenders`, `stationCallsign` | `local-sonde` | `sondehub` |
    | `airq` | `deviceType`, `endpoint`, `lat`, `lon`, `indoor`, `name` | `airq-local-aggregator` | `openaq`, `sensor-community` |
    | `pws` | `station` (`davis`/`ecowitt`), `endpoint`, `lat`, `lon` | `wxstation-davis-aggregator` / `wxstation-ecowitt-aggregator` | nothing |
    | `mesh` | `meshtastic` (+ `root`), `meshcore`, `bridge` | `meshtastic-local`, `meshcore-local`, `meshcore-bridge` | `meshtastic-mqtt`, `meshtastic-map`, only with `meshtastic` set |

  - It is live while enabled with at least one sensor enabled.
  - `apply` writes each enabled sensor's members into its read through `SourceService.update`, so `LAN_URL` pinning
    and each adapter's own `checkParams` apply. A source's own error is remapped to the member, for example
    `config.pws.lat`. The read is turned on while the aggregator is live.
  - Ownership: a read that the previous stored configuration drove and the new one does not is turned off. A read no
    configuration drove is left alone, which includes the operator's own slots.
  - `test` probes every enabled polling or streaming read at once (virtual threads, 10 s, `ProbeService.probe`) and
    sums the records. A push source, or a live `local-sonde` holding its UDP port, counts as reachable once its health
    has `lastDataAt`. A sensor whose source was deleted is reported as a failure.
  - `supersededSourceIds()` lists every registered candidate. `supersededSourceIds(settings)` lists only the mirrors
    of the enabled sensors.
- **SPI (R2-019 code):**
  - `DomainAggregator.live(settings)` defaults to `settings.live()`, and `notLiveReason()` was added.
    `AggregatorService` now asks the aggregator in every place it used `settings.live()`.
  - **`AggregatorService.update`, while the aggregator stays live:** a source in `disabledByAggregator` that the new
    configuration no longer supersedes is turned back on and dropped from the list. Before this change it stayed off
    until the whole aggregator stopped being live. This also corrects R2-022's GOES east → west switch.
- **New sources (`@Component`)**, each a slot adapter with its own descriptor, through a new protected constructor and
  a `protected static buildDescriptor(id, name)`:
  - `airq-local-aggregator` (`env/airq/AirqLocalAggregatorAdapter`);
  - `wxstation-davis-aggregator` (`local/DavisWllAggregatorAdapter`);
  - `wxstation-ecowitt-aggregator` (`local/EcowittAggregatorAdapter`).

  The slots are unchanged.
- **Tests (written, not run):**
  - `GroundSensorAggregatorTest`: an IT; WireMock serves `/json` from the existing fixture
    `airq-local/purpleair.json`. It covers routing, ownership, test, validation, error remapping and the superseded
    sets.
  - `GroundSensorSupersededIT`.
  - `PendingDomainAggregatorTest`: a unit test of the stand-in.
  - `AggregatorSurfaceIT.pendingDomain` was replaced by `everyDomainImplemented`.
  - `AggregatorItSupport` turns the three new reads off.
  - `AdapterInventoryTest.SHARED_CAPTURE` lists the two weather-station reads. `airq-local-aggregator` matches
    `fixtures/airq-local/` by prefix.
- **Docs:**
  - `docs/Network-Sourcing.md`: a new "C2e" section, the table row, the live and status rules, the C3 rows marked
    built, and EGR-6.
  - `docs/Source-Catalog.md`: three rows, plus superseded notes on `sondehub`, `openaq`, `sensor-community`,
    `meshtastic-mqtt` and `meshtastic-map`.
  - `docs/Operator-Guide.md`: a "Local ground sensors (R2-023)" paragraph.
  - `docs/Invariants.md`: EGR-6 and EGR-7 name the new tests.
- **No migration.**

## Contract

- **`AggregatorConfig`:** the descriptions of `endpoint` and `config` gained the ground-sensor rules.
- **Operation descriptions:** `listAggregators`, `updateAggregator` and `testAggregator` gained the R2-023 notes.
- **Postman:** regenerated (936 requests). No operations were added, statuses stay `planned`, and the SDKs were not
  regenerated.

## Decisions and disagreements for the owner

1. **`openaq` is superseded by a single local monitor. I disagree with this; please decide.**
   - The prompt lists "public AQ mirrors". `sensor-community` is a citizen-sensor network, so superseding it is a
     true mirror swap.
   - `openaq` aggregates *official* regulatory monitors. One PurpleAir does not replace them.
   - I followed the prompt's table. Dropping `OpenAqAdapter.ID` from `supersededSourceIds(settings)` is a one-line
     change.
   - `airnow` (official) is not superseded.
2. **Seismic and SAME supersede nothing.**
   - USGS (`usgs-earthquakes`, `usgs-shakemap`), `emsc-seismic` and the NWS sources are authorities, and the prompt's
     C3 table has "—" for both.
   - Their local "read" is the custom push source the existing `contrib/` bridge posts to. The aggregator only
     validates that the source is registered PUSH and turns it on or off.
3. **No public PWS source exists to supersede.** `grep -rln "wunderground\|api.ecowitt.net" src/main/java` returns
   nothing, so `pws` supersedes nothing.
4. **Mesh supersedes the public Meshtastic sources only while `meshtastic` is set.** A MeshCore companion does not
   replace Meshtastic's public map.
5. **`meshcore-bridge` is enabled by default.**
   - A `mesh.bridge: true` that is later removed turns it **off**, because it is a read the aggregator drove.
   - The alternative is never turning off a default-on source. Say if you prefer that.
   - The IT restores the bridge's original state so the shared database is not changed.
6. **No top-level `endpoint`.** The surface has one endpoint string, but this domain has up to seven addresses, so
   each sensor carries its own. The domain's `status` follows the first enabled source in `sourceIds()`, which lists
   the enabled sensors' sources.
7. **The `AggregatorService` restore change touches R2-019 code** (see "What was added"). It is needed for any domain
   whose superseded set depends on configuration. Without it, turning `radiosonde` off would have left `sondehub` off.
8. **The `same` sensor accepts any registered PUSH source.** This includes a non-custom one, such as
   `meshcore-bridge`. Only the source's mode is checked, not the preset it was created from.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guards this touches were reviewed by reading them, not run: `AdapterInventoryTest`, `SourceCatalogParityIT`
    (the new ids have their own rows) and `InvariantRegistryTest`.

## Notes for the next prompt

- The aggregator family is complete. Every domain reads `implemented: true`.
- `DomainAggregator.live(settings)` and `notLiveReason()` exist for a domain whose "live" is not "enabled with an
  endpoint".
- The next Flyway migration is still **V24**.
