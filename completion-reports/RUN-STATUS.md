# Run status: checkpoint 097 not green

| | |
|---|---|
| Run | 097 — Checkpoint Sources |
| Status | Partial |
| Implementation commit | 7428f36fdd98cc75d177984a60973e24b85bbdff |
| Date | 2026-10-14 |
| Last full `./gradlew test` | 8081 tests, 54 failed, 0 errors, 0 skipped |

## Why this file exists

The checkpoint requires `./gradlew test` to pass. After fixing every fault this run could determine to be a
defect (see `097-CR-Checkpoint-Sources.md`), 54 tests still fail. They are **not** the shared adapter
infrastructure this checkpoint owns — the context now starts, `FieldSpec` deserializes, family defaults and
retention are classified, the CAP/SEC/sanctions/readsb/GBFS/Open511 parsers work, and the ingest seq
accounting is correct. The residue is per-adapter fixture/parser ambiguity, contract drift from 062–096, and
order-dependent integration tests. They were not weakened to force green.

## Sequence

1. **Run 1** — `./gradlew test`: 5304 tests, 1859 failed, `OutOfMemoryError` (2 GB). Root causes: a Spring
   circular dependency (`SourceRegistry ↔ RegionTaggingService`) and a Jackson 3 `FieldSpec` creator bug
   (absent `noLink`). Both made the application context fail to load.
2. **Fix** — `@Lazy` the cycle; remove stray `@Component` from two family adapters; raise the test heap to
   4 GB; rewrite `FieldSpec`'s JSON creator; fix the `EnvTestSupport.later` helper.
3. **Run 2** — 5702 tests, 116 failed, OOM at the end.
4. **Run 3** — 8081 tests, 120 failed, no OOM.
5. **Fix** — Open511 `getOrDefault(null)`, `ConflictEgressTest` mock, the `SourceService` param-validation
   approach (kept strict; fixed the two IT teardowns), `FamilyDefaultsTest`, the schema pins, the sanctions
   package.
6. **Run 4** — 8081 tests, 78 failed.
7. **Fix** — User-Agent regex, `FetchExecutor` seq accounting + mock arity, `MqttEndpoint` TLS, AWC dedup,
   GBFS seconds, `HealthDetails` order, `CapFeed` state, `MeshCoreJsonFormat`/`MeshShortTextFormatter`, XML
   entity replacement + SEC regexes, sanctions download path/redirects.
8. **Run 5 (final full)** — **8081 tests, 54 failed, 0 errors, 0 skipped**.
9. **Fix after run 5** (targeted, not re-run as a full suite): `AwdbParserTest` expectation,
   `NuclearReactorsUsAdapterTest` → weekly, reactor interval reverted to `ReferenceSupport` default.

## Residual failures (from the final full run) with analysis

### Settings export/import (4) — order/state dependent
`LlmControllersIT`, `ServerSettingsIT`, `SettingsExportImportIT`, `BriefingsControllerIT`
("settings bundle is not valid", 400). These pass when run in isolation (verified) but fail in the full
suite, which points at shared application-settings/context state leaking between ITs rather than a per-test
defect. Needs isolation work (distinct setting keys or a reset hook).

### OpenAPI / Postman contract drift (3)
`OpenApiContractIT` (2xx status codes missing from the generated spec for
`POST /v1/channels/{id}/mqtt/...`), `PostmanCollectionContractTest` (createChannel example body; mapping
operations need validators). 062–096 added operations/fields without reconciling `docs/OpenAPI.json` and
`postman/`. A dedicated reconciliation pass is needed; out of this checkpoint's fix scope.

### Per-adapter parser/fixture ambiguities (~30)
- `CountryCentroidsFipsTest`, `UsTravelAdvisoryParserTest`: the travel-advisory `Category` mixes FIPS 10-4
  and ISO 3166-1 alpha-2 (`YM` vs `KH`/`AS`/`CH`), with collisions (`AS` = Australia in FIPS, American Samoa
  in ISO2). Not guessed.
- `TsunamiAdapterTest`, `GdacsEventParserTest`, `HansParserTest`, `AgsiAdapterTest`, `GppdParserTest`,
  `GcatParserTest`, `FdsnTextParserTest`, `CctvPackParsersTest`, `AtlasDumpParserTest`,
  `AutoRxPayloadCodecTest`, `OurAirportsParserTest`/`AdapterTest`, `ReferenceDescriptorsTest`: payload or
  record-count assertions that need a per-adapter diff against the intent.
- `SatellitePassesGeneratorTest`/`SatellitePassesDerivedIT`: rounding to 10 s and one extra predicted pass.
- `Wikistream` (`RecentChangeEventTest`, `SurgeDetectorTest`): filter/band boundary expectations.
- `NifcPerimeterMapperTest`: vertex-simplification flag.
- `AgsiAdapterTest`, `AwdbParserTest` (fixed after run 5).

### Streaming / socket (5)
- `AisStreamAdapterTest`: the Java-WebSocket library logs the subscription frame (with the API key) at DEBUG;
  the test expects redaction at every level.
- `VesselIngestIT`, `AisStreamIngestIT`, `MqttChannelIT`: awaitility timeouts (streaming flush/health).
- `WebSocketOptionsIT`: compressed-bytes wire count; `UdpTransportIT`: datagram delivery.

### HTTP retry / auth (3)
- `ErcotAdapterTest`, `Eia SeriesIngestIT`: a 401 token re-fetch/retry path.
- `HumanitarianAdaptersTest`: ReliefWeb 403 → the wrong exception type; Copernicus budget coupling.

### Other (a handful)
`PresetCustomSourceIT` (expected error code), `TransitFamilyTest` (problem count), `TransitCatalogControllerIT`
(catalog filter), `SkillFrontmatterTest`/`McpServerIT` (tool set), `MeshReportIngestIT` (rate-limit/ERR),
`ShakeMapAdapterTest` (unmatched WireMock request), `ConflictAdaptersTest` (Last-Modified round trip),
`EarthSearchAdapterTest` (WireMock event ordering; state timestamp format), `InatObservationsAdapterTest`
(query-parameter order).

## Next steps

1. Reconcile `docs/OpenAPI.json` + `postman/` with the 062–096 operations.
2. Fix the IT isolation causing the settings export/import failures.
3. Work the per-adapter list above one adapter at a time (probe → fixture diff → parser).
4. Build `AdapterInventoryTest` (focus area 1) once the per-adapter fixtures/contracts are consistent.
5. Complete the `docs/Invariants.md` sweep for owner prompts ≤ 096.
