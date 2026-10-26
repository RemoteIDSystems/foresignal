# R2-022-CR-Satellite-Reception

| | |
|---|---|
| Prompt | R2-022-Satellite-Reception.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `66babdde7c3fc16550314f31033d75470e0cc0f6` |
| Date | 2026-10-26 |

## What was added

- **`source/aggregator/SatelliteAggregator`:** the `satellite` `DomainAggregator` bean.
  - `endpoint` is SatDump's web server: an http(s) URL with no credentials, query or fragment. A base path is kept,
    because `SatDumpStation.resolve` appends `/api`.
  - `config` holds `location` (required with an endpoint), `inboxPath`, `satdumpVersion`, `predictNorad`, and
    `goes` (`east` or `west`).
  - `apply` writes `apiUrl`, `location`, `inboxPath`, `satdumpVersion` and `predictNorad` into the station through
    `SourceService.update`, so `LAN_URL` pinning, `INBOX_PATH` rules and `SatDumpDescriptor.check` apply. The station
    is turned on while the aggregator is live.
  - `test` runs the station for 10 s through `ProbeService.probe`: its `/api` read and inbox scan, with its budget and
    pinning. It counts the records produced: station state, passes and products.
  - `supersededSourceIds()` is `goes-east-abi` and `goes-west-abi`, the candidates.
    `supersededSourceIds(settings)` narrows that to the one named by `config.goes`, or none.
- **SPI extension (R2-019 code):**
  - `DomainAggregator.supersededSourceIds(AggregatorSettings)`, a default method returning `supersededSourceIds()`.
  - `AggregatorService` uses it with the proposed settings when superseding, and with the current settings in
    `isSuperseded` and the view.
  - For the other domains, behavior is unchanged.
- **New source `satdump-aggregator` (`adapters/satdump/SatDumpAggregatorAdapter`, `@Component`):**
  - It is a `SatDumpAdapter` for the reserved station name `aggregator`, registered whether or not
    `FORESIGNAL_SATDUMP_STATIONS` lists any station.
  - `SatDumpFamily.names` refuses `aggregator`, with a startup message, so the id cannot collide.
- **Tests (written, not run):**
  - `SatelliteAggregatorTest`: an IT; WireMock serves `/api` from the existing fixture
    `satdump/2.0.0-alpha/api/autotrack.json`.
  - `SatelliteSupersededIT`.
  - `SatDumpAggregatorAdapterTest`: a unit test; the descriptor equals a station's, and the name is reserved.
  - `AggregatorItSupport` turns the station off.
  - No `AdapterInventoryTest` change was needed: `fixtures/satdump/` matches by prefix.
- **Docs:**
  - `docs/Network-Sourcing.md` replaced whole, with a new "C2d" section, the table rows and the C3 row.
  - `docs/Source-Catalog.md`: a `satdump-aggregator` row, and superseded notes on `goes-east-abi` and
    `goes-west-abi`.
  - `docs/Operator-Guide.md`: a "Direct satellite reception (R2-022)" paragraph.
  - `docs/Invariants.md`: EGR-7 names `SatelliteSupersededIT`.
- **No migration.** The station's persistence (`StationStore`, `DcpLocationRepository`) is reused unchanged.

## Contract

- **`AggregatorConfig`:** the descriptions of `endpoint` and `config` gained the satellite rules.
- **`Aggregator.supersededSources`:** now described as the set under the current configuration.
- **Operation descriptions:** `listAggregators`, `updateAggregator` and `testAggregator` gained the satellite notes.
- **Postman:** regenerated (936 requests). No operations were added, statuses stay `planned`, and the SDKs were not
  regenerated.

## Decisions and disagreements for the owner

1. **Only the ABI imagery of the received GOES is superseded.**
   - The prompt says to supersede "the `goes` and any NOAA weather-sat mirror adapters". The `goes` package holds four
     sources:
     - `goes-east-abi` and `goes-west-abi` are imagery, which a dish's HRIT broadcast replaces.
     - `goes-glm` is lightning. It is in GRB, not HRIT, and `GlmLocalAdapter` ("de-duplicated against the internet
       goes-glm flashes") is built to run alongside it.
     - `goes-fdc` is a ground-processed fire product; no broadcast carries it.
   - So GLM and FDC stay on.
   - A dish sees one GOES, so `config.goes` names it, and only that satellite's pull is superseded. A polar-only
     station supersedes nothing.
   - No other NOAA weather-satellite imagery pull is registered. The packages `hms`, `sst` and `firms` are derived
     products, not imagery mirrors.
2. **The SPI gained `supersededSourceIds(settings)`** (see "What was added"). Without it, a GOES-West station would
   have turned off GOES-East's imagery as well.
3. **Superseding is conditional, as in R2-019.** No descriptor's `autoEnable` was changed.
4. **The `satdump-aggregator` station.**
   - Stations were property-only (`FORESIGNAL_SATDUMP_STATIONS`, empty by default), so the aggregator needed a
     source that always exists. It takes the reserved station name `aggregator`.
   - **An install that already lists a station named `aggregator` will now fail at startup** with a message saying so.
     I judged that better than a silent duplicate-id failure. Say if you prefer another reserved name.
5. **Egress.** A SatDump station is `LOCAL_ONLY` through its license (`operator-owned` → `local-only-operator`,
   redistribution `LOCAL_ONLY`), not through the descriptor's `egress`, which is the builder default `OPEN`.
   - That is true of every existing station, and I did not change the shared descriptor.
   - EGR-6 says "LOCAL_ONLY by descriptor". For SatDump it holds through the license. Should the station descriptor
     set `egress(LOCAL_ONLY)` explicitly?
6. **`config` has no dish or SDR fields.** SatDump owns its hardware configuration; Foresignal has no parameter that
   would use one, so the station's real parameters are exposed instead.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guards this touches were reviewed by reading them, not run: `AdapterInventoryTest`, `SourceCatalogParityIT`
    (the `satdump-{station}` placeholder row also matches the new id), `InvariantRegistryTest` and
    `SatDumpDescriptorTest`.

## Notes for the next prompt

- A domain whose superseded set depends on its configuration overrides `supersededSourceIds(settings)`.
- `AggregatorSurfaceIT.pendingDomain` uses `ground-sensors`. R2-023 must change it once `ground-sensors` is
  implemented, because no domain will be pending any more.
- The next Flyway migration is still **V24**.
