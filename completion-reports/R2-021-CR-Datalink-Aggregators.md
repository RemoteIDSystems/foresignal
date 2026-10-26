# R2-021-CR-Datalink-Aggregators

| | |
|---|---|
| Prompt | R2-021-Datalink-Aggregators.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `4bc1e8317290e9503cf92a1d5c590dac982e6559` |
| Date | 2026-10-26 |

## What was added

- **`source/aggregator/DatalinkAggregator`:** the `datalink` `DomainAggregator` bean.
  - `endpoint` is the decoder host, `tcp://host`, with no port, path or credentials.
  - `config` selects the decoders:
    - `acars`, `vdlm2`, `hfdl` (acars_router's TCP servers) and `aprs` (the igate's KISS TCP port);
    - each is a port on the endpoint host or a whole `tcp://host:port`;
    - also `stationId`, `stationCallsign` and `includeMessages`.
  - Enabling requires at least one decoder.
  - `apply` writes the endpoints into two reads through `SourceService.update`, so `LAN_URL` pinning and the
    adapters' own `checkParams` apply. Each read is turned on while the aggregator is live with one of its decoders
    selected; a read with nothing selected is only turned off and keeps its parameters.
  - `test` probes every selected read at once (virtual threads, 10 s, `ProbeService.probe`) and sums the records.
  - `supersededSourceIds()` is `aprs-is`.
- **New sources:**
  - `local-acars-aggregator` (`adapters/acars/LocalAcarsAggregatorAdapter`):
    - It is a `LocalAcarsAdapter` with its own descriptor, through a new protected constructor and a shared
      `describe(id, name, description, budget)`.
    - It is metadata only, private by descriptor, and `LOCAL_ONLY`, not overridable, exactly like the slots.
  - `local-aprs-aggregator` (`adapters/radio/LocalAprsAggregatorAdapter`):
    - It is a `LocalAprsAdapter` with its own descriptor, through a new protected constructor and
      `describe(id, name, description)`.
    - It is receive-only and registers its packets for `aprs-is` dedup. It is `LOCAL_ONLY` and overridable.
    - The `local-aprs-` prefix means `AprsIsServeSinkType` serves it like the slots.
- **`AggregatorService` (R2-019 code, generalized because `datalink` reads through two sources):**
  - `status` follows the first *enabled* source in `sourceIds()` (the new `statusSource`), not always the first one.
  - `system.aggregator_down` is raised when any of the domain's sources fails.
  - `system.aggregator_recovered` is raised when a source recovers and no other enabled source of the domain is
    failing.
  - For `adsb` and `ais`, which have one source each, behavior is unchanged.
  - The `DomainAggregator.sourceIds()` javadoc states the rule.
- **Tests (written, not run):**
  - `DatalinkAggregatorTest`: an IT with loopback acars_router and Direwolf stand-ins, fed from the existing fixtures
    `local-rf/acarsdec.json` and `local-aprs/direwolf-kiss.hex`.
  - `DatalinkSupersededIT`.
  - The shared helper `LoopbackFeed`.
  - `AggregatorItSupport` turns the two new reads off.
  - `AdapterInventoryTest.SHARED_CAPTURE` lists `local-acars-aggregator` and `local-aprs-aggregator`.
- **Docs:**
  - `docs/Network-Sourcing.md` replaced whole, with a new "C2c" section, the status rule, the C3 rows marked built,
    and EGR-6.
  - `docs/Source-Catalog.md`: rows for both reads, and a superseded note on `aprs-is`.
  - `docs/Operator-Guide.md`: a "Local datalink (R2-021)" paragraph.
  - `docs/Invariants.md`: EGR-6 and EGR-7 name the new tests.
- **No migration.**

## Contract

- **`AggregatorConfig`:** the descriptions of `endpoint` and `config` gained the datalink rules.
- **Operation descriptions:** `listAggregators`, `updateAggregator` and `testAggregator` gained the datalink notes.
- **Postman:** regenerated (936 requests). No operations were added, statuses stay `planned`, and the SDKs were not
  regenerated.

## Decisions and disagreements for the owner

1. **airframes.io is not superseded, because there is no such source.**
   - `grep -rln airframes src/main/java` returns nothing.
   - So nothing is turned off for ACARS, VDL Mode 2 and HFDL.
   - The docs say so instead of naming a source id that does not exist.
2. **Superseding is conditional, as in R2-019.** `aprs-is`'s descriptor was not changed.
   - It already requires a narrow `filter` to be enabled, so a fresh install cannot seed it on.
   - While the aggregator is live, it is held off and the bulk enables skip it.
3. **`endpoint` is a host, and the decoders are in `config`.**
   - The surface has one `endpoint` string, but this domain reads up to four TCP servers.
   - So `endpoint` is the decoder host, and each decoder is a port on it or a whole URL.
   - The defaults are not assumed: acars_router's 15550/15555/15556 and Direwolf's 8001 appear in the docs as
     examples only, matching the slots' rule.
4. **The `AggregatorService` status and alert change touches R2-019 code** (see "What was added"). Without it,
   `status` would read `down` whenever only APRS is selected, because `local-acars-aggregator` would be first and
   disabled.
5. **The ACARS read stays private and its egress is not overridable.** The prompt does not ask for anything else, and
   the slots' interception-disclosure reasoning applies unchanged.
6. **The test's `reachable` field is per domain.** It is false only when no selected read connected. A partial
   failure is `ok: false`, with each failing decoder named in `message`, because `AggregatorTestResult` has no
   per-decoder field.
7. **`DatalinkSupersededIT` resets `aprs-is` in SQL** (`params = '{}'`). A required `filter` cannot be cleared through
   `SourceService`, and `RadioIngestIT` relies on `aprs-is` refusing to enable without one.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guards this touches were reviewed by reading them, not run: `AdapterInventoryTest`, `SourceCatalogParityIT`,
    `RegisteredSourcesHaveLicenceTest`, `InvariantRegistryTest`, `UpstreamClientIsTheOnlyHttpClientTest` and
    `RadioIngestIT`. The `local-aprs-*` assertions in `RadioIngestIT` (disabled, `LOCAL_ONLY`) hold for the new read.

## Notes for the next prompt

- A domain may now drive several sources. List them in order of preference in `sourceIds()`; status and alerts
  follow the enabled ones.
- `LoopbackFeed` (test package `source.aggregator`) is a reusable loopback decoder stand-in.
- `AggregatorSurfaceIT.pendingDomain` uses `ground-sensors` until R2-023.
- The next Flyway migration is still **V24**.
