# R2-020-CR-AIS-Aggregator

| | |
|---|---|
| Prompt | R2-020-AIS-Aggregator.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `2910a9ed2740fbe652a48721d1dc543b6b4a9fce` |
| Date | 2026-10-26 |

## What was added

- **`source/aggregator/AisAggregator`:** the `ais` `DomainAggregator` bean. `AggregatorService` now serves it in place
  of the pending stand-in.
  - `sourceIds()` is `local-ais-aggregator`.
  - `supersededSourceIds()` is `aisstream`, when it is registered.
  - `validate`:
    - the endpoint is required to enable;
    - it must be `tcp://host:port` or `udp://bindHost:port`, with an explicit port;
    - `config` may hold only `allowSenders` (a list) and `stationName` (a string);
    - a `udp://` endpoint requires `config.allowSenders`.
  - `apply` writes `endpoint`, `allowSenders` and `stationName` into the read through `SourceService.update`, so
    `LAN_URL` pinning and the `allowSenders` private-space rule are applied by `ParamValidator`. Errors are remapped
    to `endpoint` / `config.*`. The read is turned on while the aggregator is live and off otherwise.
  - `test` listens through the existing `ProbeService.probe(local-ais-aggregator, null, 10)`, so it uses the source's
    own socket path, budget and pinning and opens no new socket. It reports the vessel records decoded (at most 100).
    A live UDP listener cannot be bound twice, so it is reported from the read's statistics instead (distinct MMSIs
    in 10 minutes).
- **New source `local-ais-aggregator` (`source/adapters/ais/LocalAisAggregatorAdapter`, `@Component`):**
  - It is a `LocalAisAdapter` with its own descriptor, through a new protected constructor
    `LocalAisAdapter(SourceDescriptor, AivdmSocketCodec)`.
  - It has the same `aivdm` decoding, station parameters and statistics, and is `LOCAL_ONLY`, overridable.
  - Its endpoint accepts `tcp` and `udp`. A `udp://` endpoint binds via `ctx.sockets().udp(…)` with `allowSenders`,
    and decodes every line of each datagram.
  - Budget is 4 per minute and 400 per day.
  - The `local-ais-` prefix means every generator reading `DerivedSources.VESSEL_PREFIXES` picks it up.
- **`AisSourcesHealth`:** contributes the read's health details (`messagesPerMin`, `distinctMmsi10m`,
  `lastMessageAt`, `vessels`).
- **Tests (written, not run):**
  - `AisAggregatorTest`: an IT with a loopback NMEA TCP server.
  - `AisSupersededIT`.
  - `LocalAisAggregatorAdapterTest`: a unit test; multi-line datagrams, CRLF, the allowlist, the descriptor.
- **Existing tests adjusted:**
  - `AggregatorItSupport` also turns `local-ais-aggregator` off.
  - `AggregatorSurfaceIT.pendingDomain` now uses `ground-sensors`, because `ais` is no longer pending.
  - `AdapterInventoryTest.SHARED_CAPTURE` gained `local-ais-aggregator` **and `local-adsb-aggregator`** (see
    decision 5).
- **Docs:**
  - `docs/Network-Sourcing.md` replaced whole, with a new section "C2b. Local AIS aggregator", the table rows, the C3
    AIS row marked built, and EGR-6 extended.
  - `docs/Source-Catalog.md`: a parity row for `local-ais-aggregator`, and a superseded note on the `aisstream` row.
  - `docs/Operator-Guide.md`: a "Local AIS (R2-020)" paragraph; the line saying only `adsb` is implemented is
    corrected.
  - `docs/Invariants.md`: EGR-6 and EGR-7 name the new tests.
- **No migration.** The configuration is the existing `aggregator.ais` setting. `AisStationStats` is reused in memory
  (it is not an entity), so nothing needs persisting.

## Contract

- **`AggregatorConfig`:** the descriptions of `endpoint` and `config` now state the ais rules (`tcp://` / `udp://`,
  `allowSenders`, `stationName`).
- **Operation descriptions:**
  - `listAggregators` says ais is implemented;
  - `updateAggregator` lists the ais endpoint 400s;
  - `testAggregator` gained an R2-020 paragraph.
- **Postman:** regenerated (936 requests). No operations were added, statuses stay `planned`, and the SDKs were not
  regenerated.

## Decisions and disagreements for the owner

1. **The MQTT option is not offered.** The prompt suggests the endpoint could be "the MQTT topic the
   `AisMqttSubscriber` reads". In the code, `AisMqttSubscriber` is Digitraffic's subscriber: hard-wired to Fintraffic's
   `wss://meri.digitraffic.fi` and its JSON topics. It cannot read a local AIS-catcher's MQTT. So the endpoint is the
   decoder's NMEA over TCP or UDP. A local MQTT reader would be a new adapter; say if you want one.
2. **Superseding is conditional, as in R2-019.** `aisstream`'s `autoEnable` was not changed: it already needs an API
   key and is not core, so a fresh install never seeds it on. While the aggregator is live, it is held off and the
   bulk enables skip it.
3. **Only `aisstream` is superseded.** The prompt also says "any other public global-AIS aggregator in the catalog".
   - `grep -rln VesselDescriptors` over `src/main/java` finds these vessel sources: `aisstream`, `ais-digitraffic`,
     `ais-kystverket`, `local-ais-*` and `gfw-events`.
   - `gfw-events` is Global Fishing Watch's event analysis, not a mirror of the AIS stream, so it stays on.
   - The 059 `aiscatcher` HTTP preset is an ingest device preset, not a registered source.
   - The network doc's C3 table said "aisstream, maritime mirrors". That row now says `aisstream` only.
4. **UDP was added beyond the slots.**
   - Verified: the AIS-catcher docs give `-u host port` for UDP and `-S port` for its TCP server. The rtl-ais README
     says it sends UDP to 127.0.0.1:10110 by default, and has `-T` for a TCP listener.
   - UDP is therefore the decoders' common output, so the read accepts both.
   - The `local-ais-{n}` slots are unchanged (TCP only).
5. **R2-019 left a test-guard gap, fixed here.** `AdapterInventoryTest` requires every non-derived, non-push source to
   have a fixture directory, or to be listed in `SHARED_CAPTURE`. `local-adsb-aggregator` (R2-019) had neither, so
   that guard would have failed once tests run. Both aggregator reads are now listed; they parse exactly as their
   slots do.
6. **The test window is fixed at 10 s** (`ProbeService.DEFAULT_SECONDS`).
   - A quiet coast may decode nothing in 10 s. The result is then `reachable: true, ok: false`, with a message saying
     so.
   - `sampleCount` is capped at 100 by the probe's preview cap.
7. **The budget is 4 per minute and 400 per day**, double the slots' 2 and 200. A UDP bind and a TCP connect each cost
   one unit.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.
  - The guards this touches were reviewed by reading them, not run: `AdapterInventoryTest`, `SourceCatalogParityIT`,
    `RegisteredSourcesHaveLicenceTest`, `InvariantRegistryTest` and `UpstreamClientIsTheOnlyHttpClientTest`. No new
    socket is opened in main code.

## Notes for the next prompt

- `AggregatorSurfaceIT.pendingDomain` now uses `ground-sensors` as the example of a domain without a backend. R2-023
  must move it again, or drop it once every domain is implemented.
- A new `local-*` read needs a `SHARED_CAPTURE` entry (or a fixture directory) in `AdapterInventoryTest`, and a
  Source-Catalog row.
- To test a streaming endpoint, `ProbeService.probe(sourceId, null, seconds)` reuses the source's own transport.
- The next Flyway migration is still **V24**.
