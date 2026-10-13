# 059-CR-Mapping-Profiles-And-Device-Presets

| | |
|---|---|
| Prompt | 059-Mapping-Profiles-And-Device-Presets.md |
| Status | Complete |
| Implementation commit | 348b735c9f0a6bef3e8e33562a8e6ca40baf296e |
| Date | 2026-10-13 |

## Summary

Any JSON, form or query-string device can now be onboarded without code:
- a declarative `RecordMapping` turns its payload into records;
- a preview shows the result before saving;
- an LLM can draft a mapping, which is never stored or applied;
- eight device presets cover common homelab gear.

Mappings are data. There is no expression language and nothing is executed. All five planned operations are
`implemented` and off `pending-operations.txt`.

**1. RecordMapping** (`ai.foresignal.mapping`).
- **Schema** (`version: 1`). Every value is `{path}`, `{template}` (substitution only) or `{const}`.
  - Members: `select`, `externalId`, `kind`, `observedAt`, `eventTime`, `validUntil`, `geometry`, `tombstone`,
    `quality`, `fields[≤ 64]`, `pairs`, `drop` and `filter`.
  - Added beyond the prompt (Deviation 1): `spread`, `geometry.fallback` and `quality.extra`.
  - `$pairs.<key>` reads a flattened pair.
- **`MappingCompiler`** compiles a mapping into an immutable `CompiledMapping` and collects every error with its
  path (`mapping.fields[3].unit.from`; `400 MAPPING_INVALID`). It refuses:
  - unknown members;
  - deep scan (`..`), any function call, and regex filters (`=~`), with quoted names ignored;
  - field names outside `^[A-Za-z][A-Za-z0-9_]{0,63}$`, and fields listed in `drop`;
  - unit pairs outside the table, and units, `scale` or `offset` on non-numeric types;
  - an `observedAt` that disagrees with the source's `observedAtMode`. That is an error for saving and ingest,
    and a warning in a preview.
- **JSONPath:** Jayway `com.jayway.jsonpath:json-path` 3.0.0 (Apache-2.0, checked on Maven Central). It ships
  `Jackson3JsonNodeJsonProvider` and `Jackson3MappingProvider`, so no provider had to be written.
  - `MappingJsonPath` always passes its own configuration over Boot's `JsonMapper`, with exceptions suppressed.
  - `json-smart` is excluded from the application; the runtime classpath was checked with `dependencyInsight`.
  - The Boot BOM pins 2.10.0, so `build.gradle.kts` overrides its `json-path.version` property.
  - Behavior checked with a scratch program against the jar, with no json-smart on the classpath: definite
    paths, `[*]`, a quoted `['frm-payload']`, a filter, and a missing path reading as null.
- **`Units`:** the UCUM table of the prompt (ten conversions, exact factors) plus identity for any listed code.
- **`MappingEngine.apply(doc, mapping, receivedAt)`** returns a `MappingResult`: records with item indexes,
  rejections, warnings, the skipped count and the item count.
  - Per item, in order: pairs, filters (skipped and counted), external id, kind, times, tombstone, geometry,
    fields, spread, then `drop`, then quality.
  - Coercion follows `type`. Numeric strings are read with `BigDecimal`, so `NaN` and `Infinity` are rejected.
  - Units are applied first, then `scale`, then `offset`. `integer` rounds.
  - An uncoercible, NaN or infinite value leaves that field out with `FIELD_REJECTED`.
  - An out-of-range coordinate (AIS lat 91, lon 181) leaves the geometry out, falls back to `geometry.fallback`
    if set, and warns `GEOMETRY_OUT_OF_RANGE`.
  - A repeated field name keeps its first value.
- **`MappingInputs`:** form and query inputs become a flat object of strings (the first value wins; a form body
  also takes query parameters it does not carry). A request with no body is its query.
- **Storage:** `custom_source` gains `mapping` (jsonb), `default_format`, `preset` and `capture_samples_until`
  (Hibernate `ddl-auto`).
- **Mapping format:** the 024 `mapping` format was **replaced**. It now delegates to `MappingEngine` with the
  stored mapping (`MappingCache`, recompiled when the mapping text, kinds or time base change).
  - `RecordMapping`, `RecordMappingEngine` and `RecordMappingEngineTest` of package `inbound.format` were deleted.
  - `formats.mapping` is refused with a field error that points to `mapping`.
- **Change event:** editing the mapping is a custom-source update, which re-registers the descriptor and
  publishes the change event. So 009's lazy recipe migration runs when hashed fields change.

**2. Ingest variants.**
- **Already in place from 039** (reused unchanged): the GET path for formats that declare it, the gzip reader
  with the 5 MB cap after decompression (413), Basic with the key as password, the key-in-path URL with
  `allowPathKey`, and the `ingestAllowCidrs` sender check (default private ranges; `403 INGEST_SOURCE_DENIED`).
- **Form:** `POST /v1/ingest/{sourceId}` uses the source's default format (a preset's format) when it has one,
  so a form POST from a device lands in its mapping. `IngestBodyReader` rebuilds a form body that the container
  already parsed into parameters.
- **GET:** the `wu-pws` and `gmc` formats declare GET.
- **Protocol-native keys:**
  - the WU `PASSWORD` query parameter on `/wu-pws` is an ingest-key credential (`ID` is ignored). A query-key
    push is subject to the sender allowlist, like a GET.
  - the key-in-path formats gain `ecowitt`, `gmc` and `mapping`.
- **Redaction:** `IngestUrls.SECRET_PARAMETERS` gains `password` and `passkey` (case-insensitive). This covers
  logs, the audit log and `fetch_log.url_redacted`. The Operator Guide has a Caddy `log` filter example, checked
  against the Caddy documentation's `filter`/`regexp` section.
- **Replies:** `ecowitt` an empty 200, `wu-pws` `200 text/plain success`, `gmc` `200 text/plain OK.`
  (unverified), the others the `IngestOutcome`.

**3. Preview, samples, test.**
- **`POST /v1/mappings/preview`** (`MappingPreviewService`) compiles leniently, maps, then normalizes with the
  standard `Normalizer` and the source's `SourceIngestProfile`. Limits, null island, the future clamp, the time
  base and the private precision cap show exactly as on ingest.
  - Without `sourceId` it uses a stand-in push source with the mapped kinds.
  - Normalizer rejections are mapped back to item indexes.
  - Warnings: `UNMAPPED_INPUT_KEY`, `FIELD_NOT_IN_FIELD_SPECS`, `SIGNIFICANT_FIELD_MISSING` (the effective
    policy, override first) and `TIME_BASE_MISMATCH`.
  - It writes nothing.
- **`SampleCapture`** (in memory) keeps the last 5 requests per source, newest first.
  - It keeps the method, format segment, `Content-Type`, `Content-Encoding`, the redacted query and the
    redacted, decompressed body (at most 64 KiB, with `truncated`). No other header is kept.
  - Credentials are masked by name in the query, in a form body and as JSON string members.
  - It captures after the caller is authorized and the body is read, before the signature check and parsing,
    so a payload the mapping cannot read yet is still captured.
  - `captureSamples: true` opens a 1-hour window (`capture_samples_until`). A PUT that keeps it on does not
    extend the window; turning it off drops the samples.
  - `GET /v1/custom-sources/{id}/samples` lists them.
- **`POST /v1/custom-sources/{id}/mapping/test`** runs the proposed or the stored mapping over each sample:
  `409 MAPPING_NOT_SET` without either, `409 NO_SAMPLES` without samples.

**4. LLM-suggested draft** (`MappingSuggestService`, `MappingSuggestPrompts`).
- **Request:** admin, always authenticated (`AccessPolicy`), purpose `mapping-suggest` (new
  `Purpose.MAPPING_SUGGEST`; the wire value now maps `_` to `-`), and the 026 budget through the governed client.
- **Order of checks:**
  1. no provider: `409 LLM_NOT_CONFIGURED`;
  2. a private source's sample and a remote provider: `403`, even with confirmation;
  3. a remote provider (`EgressPolicy.isLocal`, 035's classification) without `confirmEgress: true`:
     `409 EGRESS_CONFIRMATION_REQUIRED`.
- **Prompts:**
  - the system prompt is stable, describes the schema, rules and units, says to output only JSON, and declares
    the sample untrusted;
  - the sample goes between `<sample>` delimiters, and a `</sample>` inside it is defused;
  - no tools are offered.
- **Validation:** the output is parsed (fenced or bare JSON), then compiled. Up to 2 repair rounds send the
  errors back; after that the answer is `422 MAPPING_INVALID` with `errors`.
- **Result:** a valid draft is previewed over the sample and returned with the warnings, `repairRounds`,
  provider and model. Nothing is stored.

**5. Preset catalog** (`src/main/resources/mapping/presets/*.json`, `PresetCatalog`, `DeviceMappingFormat`).
- **The eight presets:** `ecowitt`, `wu-pws`, `aiscatcher`, `chirpstack-v4`, `tts-v3`, `alertmanager`,
  `airrohr` and `gmc`, with the field sets, units, policies and privacy of the prompt's table.
- **Each preset file holds:**
  - the spec's `MappingPreset` members;
  - the setup with the exact URL template and markdown instructions (`{host}`, `{port}`, `{path}` and `{url}`
    are rendered from `publicBaseUrl`, else the request base);
  - internal descriptor defaults: thresholds, motion, retention, `mergeCurrent` and `locationMode`.
- **Create from a preset:** `POST /v1/custom-sources {preset, presetParams}` fills every member the input leaves
  out. Parameters are checked; `lon`/`lat` are rounded to 0.01° unless `exactLocation`, and `tombstoneOnResolve`
  applies to alertmanager. The response returns `setup` with the concrete URL, shown once like the key.
  `preset` cannot change on update.
- **Descriptor defaults:** `CustomSourceDescriptors` applies the preset's ingest policy, retention, thresholds
  and motion.
- **Formats:** one `DeviceMappingFormat` bean per device preset (`DeviceFormatsConfiguration`). Code covers what
  a mapping cannot:
  - **AIS merging:** each record is laid over the earlier records of the request, else over the current head
    (`DerivedRecords`), so the payload stays a complete snapshot (Core-Data-Model §3 rule 7);
  - **ChirpStack events:** `location` moves the device and keeps its payload; other events are ignored; Protobuf
    is `415`;
  - **TTS location:** without an `frm-payload` location, `locations.user` is used and flagged
    `lora.locationSource=static`.
- **Alertmanager:** `AlertmanagerFormat` gains `receiverPrefix`, `resolvedValidFor` and `station`. It now copies
  `severity` and `alertname` to the payload, for every source, so they can be significant.
- **`GET /v1/mappings/presets`** lists the catalog with setup rendered for this server.
- **Fixtures:** `src/test/resources/fixtures/mapping/{id}/`: `sample.json` (the `CapturedSample` shape),
  `expected.json` (golden records, secrets, never-stored keys) and a README marked `synthetic: true`.

**6. Spec, Postman, docs.**
- **`docs/OpenAPI.json`:**
  - the 5 operations are `implemented`;
  - RecordMapping gains `spread`, `geometry.fallback` and `quality.extra`, and sets `additionalProperties: false`;
  - CustomSourceFormats changes: `mapping` is now `null` only, `IngestRecordMapping` is removed, and
    `alertmanager` gains its new options;
  - CustomSourceInput descriptions for `mapping`, `preset` and `captureSamples`;
  - the path-key format enum gains `ecowitt`, `gmc` and `mapping`;
  - the format, GET, POST and native-endpoint descriptions, with the WU `PASSWORD` documented;
  - Problem error codes `MAPPING_INVALID`, `EGRESS_CONFIRMATION_REQUIRED`, `MAPPING_NOT_SET` and `NO_SAMPLES`;
  - the 409 and 403 descriptions, and a changelog entry.
- **Postman:** `BODIES` for `previewMapping`, `suggestMapping` and `testCustomSourceMapping`; a `createCustomSource`
  variant "from the ecowitt preset"; the mapping variant now uses a RecordMapping. Regenerated.
- **`AccessPolicy`:** `POST /v1/mappings/suggest` `ADMIN_ALWAYS_AUTHENTICATED`, `/v1/mappings/**` `ADMIN`.
  `GET /v1/ingest/**` was already `INGEST_KEY_OR_ADMIN`.
- **Docs:**
  - Operator Guide: "Devices and mappings", with a section per preset (exact device settings), the cleartext and
    LAN caveat, mapping authoring, preview, samples and test, and suggest. Also the formats table, the custom
    source notes, and Caddy in "Logs and reverse proxies".
  - Architecture: §16.1 RecordMapping and device presets, and the package layout.
  - Invariants: SRC-5, SEC-5, EGR-1 and EGR-3 gain the 059 tests.
  - `THIRD_PARTY_NOTICES.md`: Jayway JsonPath.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/mapping/`:
  - `package-info`, `MappingJsonPath`, `ValueSpec`, `TimeSpec`, `GeometrySpec`, `MappingScope`, `CompiledMapping`;
  - `MappingContext`, `MappingCompiler`, `MappingInvalidException`, `Units`, `MappingEngine`, `MappingResult`;
  - `MappingWarning`, `MappingInputs`, `MappingCache`, `SampleCapture`, `MappingPreviewService`;
  - `MappingSuggestService`, `MappingSuggestPrompts`, `MappingsController`;
  - `preset/MappingPreset`, `preset/PresetCatalog`;
  - `format/DeviceMappingFormat`, `format/DeviceFormatsConfiguration`.
- `src/main/resources/mapping/presets/`: `ecowitt`, `wu-pws`, `aiscatcher`, `chirpstack-v4`, `tts-v3`,
  `alertmanager`, `airrohr`, `gmc` (`.json`)
- `src/test/resources/fixtures/mapping/{8 presets}/`: `sample.json`, `expected.json`, `README.md`
- Tests:
  - `mapping/`: `MappingCompilerTest`, `MappingEngineTest`, `UnitTableTest`, `SampleCaptureTest`,
    `MappingSuggestServiceTest`;
  - `mapping/`: `IngestVariantsIT`, `MappingPreviewIT`, `SampleCaptureIT`, `MappingSuggestIT`,
    `PresetCustomSourceIT`;
  - `mapping/preset/`: `PresetContract`, `PresetCatalogTest` and the eight contract tests
    `EcowittPresetContractTest`, `WuPwsPresetContractTest`, `AiscatcherPresetContractTest`,
    `ChirpstackV4PresetContractTest`, `TtsV3PresetContractTest`, `AlertmanagerPresetContractTest`,
    `AirrohrPresetContractTest` and `GmcPresetContractTest`.

**Modified**
- `inbound/`:
  - `CustomSourceEntity`, `CustomSourceDefinition`, `CustomSourceInputReader`, `CustomSourceService`;
  - `CustomSourcesController`, `CustomSourceDescriptors`;
  - `InboundRequests`, `IngestAuthenticator`, `IngestBodyReader`, `IngestController`, `IngestUrls`;
  - `format/AlertmanagerFormat`, `format/MappingFormat`, `format/IngestFormatRegistry`.
- `llm/client/Purpose`, `api/error/ErrorCodes`, `security/AccessPolicy`
- `build.gradle.kts`, `gradle/libs.versions.toml`, `THIRD_PARTY_NOTICES.md`
- `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
  `src/test/resources/openapi/pending-operations.txt`
- Test support: `testsupport/inbound/InboundBodies`, `inbound/CustomSourceProfileTest` (new constructor argument)

**Deleted**
- `inbound/format/RecordMapping`, `inbound/format/RecordMappingEngine`, `inbound/format/RecordMappingEngineTest`
  (024's interim mapping)

## Deviations from the prompt

1. **RecordMapping members added (spec amended).**
   - `spread` copies an object's scalar members, at most 32. ChirpStack's "`object.*` numeric top-level keys
     (≤ 32)" and TTS `decoded_payload.*` cannot be listed field by field.
   - `geometry.fallback` holds the preset's position for LoRaWAN devices without a location.
   - `quality.extra` holds `lora.rssi`/`lora.snr` and `wifi.rssi`, which the presets require; the spec's
     `quality` had only `accuracyMeters`.
   - A repeated field name keeps its first value, for airrohr's SDS/PMS/SPS30 alternatives.
   - Recorded in the changelog.
2. **024's `formats.mapping` was replaced, not kept beside the new mapping.** The JSON-Pointer shape and the
   `IngestRecordMapping` schema were removed, and `formats.mapping` is now a 400 pointing to the top-level
   `mapping`. There are no users and no compatibility debt.
3. **WU time base.** `wu-pws` uses `observedAtMode: RECEIVED` with `observedAt: {received: true}` for every
   observation, not "`dateutc=now` → received time, else `dateutc`". A per-record switch between the receive time
   and a device time is what Core-Data-Model §2 forbids. A station that sends real `dateutc` values also gets
   receive times.
4. **Significant fields the table does not name.** LATEST needs significant fields:
   - `wu-pws` uses `[model]` from `softwaretype`;
   - `airrohr` uses `[firmware]` from `software_version`;
   - `gmc` uses `[geigerId]` from `GID`.

   Each is an identity field, so history is the keyframe stream plus device changes, like Ecowitt's `[model]`.
5. **AIS field names.** The table's `speed`, `course`, `shipname`, `shiptype` and `status` are stored as `sog`,
   `cog`, `name`, `shipType` and `navStatus`, so the vessel significant fields of Core-Data-Model §5.3 (`name`,
   `callsign`, `shipType`, `destination`, `navStatus`) and the thresholds (`cog` 15°, `sog` 3 kn) apply.
   - Sentinels: speed 102.3, course 360, heading 511, `shipType` 0 and `navStatus` 15 are dropped; lat 91 and
     lon 181 are out of range.
   - Station position: the root `station_lat`/`station_lon` are never mapped. This is "opt-in" in the sense
     that only a hand-written mapping can store them.
6. **AIS merging and ChirpStack location events** read the current head (`DerivedRecords`) inside the format.
   The prompt says only "merge" and "location updates geometry", and without the head both would break the
   complete-snapshot rule.
7. **One external id per weather station.** `ecowitt` and `wu-pws` use `const "station"`, because `PASSKEY` and
   `ID` must not be stored. One source per station; the Operator Guide says so.
8. **Fixtures are synthetic.** No device was available. Each fixture is built from the protocol's documented
   fields and marked `synthetic: true` with the recapture procedure. For AIS-catcher, the `jsonaiscatcher`
   member names (`protocol`, `stationid`, `station_lat`, `msgs`) were checked in its source (GPL-3.0, read for
   facts only, nothing copied). Its documentation site answered 403.
9. **Error codes added:** `MAPPING_NOT_SET` and `NO_SAMPLES` for the two 409s of `testCustomSourceMapping`. The
   spec named the condition but no code.
10. **`mapping` on key-in-path URLs.** `mapping` joined `ecowitt` and `gmc` in the key-in-path enum, so a
    hand-mapped device that can only set a path can push too.
11. **Golden comparison** is field by field with a relative tolerance of 1e-9 (`PresetContract.assertGolden`).
    It is not a byte comparison, because the expected values were computed outside Java and floating-point
    rounding order differs.

## Issues found / open questions

1. **GMC is unverified** (as the prompt says). The preset, its `OK.` reply and its parameter names need a
   capture from a real counter before anyone relies on them.
2. **Ecowitt `dateutc`.** The console's clock is the observation time (UPSTREAM). A console with a wrong clock
   produces LATE drops or clamps. Should `ecowitt` also be RECEIVED, like `wu-pws`? Left UPSTREAM as the prompt
   specifies.
3. **`AlertmanagerFormat` now always adds `severity` and `alertname`** to the payload, also for non-preset
   sources. This is harmless pre-release; mentioned because existing alertmanager sources' hashes change once.
4. **Preview of a private source** shows coordinates reduced to `privatePrecisionDecimals`, exactly as ingest
   would write them. Raw sample coordinates are visible only in the admin's own request.
5. Data contained no instructions directed at the agent.

## Tests written (not run)

- `MappingCompilerTest`:
  - every validation path: shape, version, unknown members, required members and kind rules, value-spec
    exclusivity;
  - rejected paths (deep scan, `length()`, a function in a filter, `=~`, missing `$`, malformed, empty), and
    quoted `..`/`(` allowed;
  - time formats, patterns and zones;
  - the time base against `observedAtMode`, strict and lenient;
  - field rules (name, type, unknown unit, pair outside the table, scale or offset on a string, default,
    sentinels, drop, the 64-field cap);
  - geometry variants, tombstone, quality, pairs, spread, drop and filter errors;
  - collect-all, and `firstKeyOf`.
- `MappingEngineTest`:
  - select arrays and edge cases, templates, pairs with `$pairs` in fields and quality;
  - every unit row with known values, scale and offset, and integer rounding;
  - sentinels and defaults, coercion including NaN, infinity and an overflowing scale;
  - filters (`==`, `!=`, `exists`, `in`, numeric strings), drop through spread, tombstone by `equals` and `in`;
  - geometry (lon/lat, strings, sentinel with fallback, no fallback, missing, GeoJSON, constant, invalid GeoJSON);
  - time formats and the absent `observedAt`, unknown kinds and big numeric ids, quality;
  - form, query and sample inputs, and equality.
- `UnitTableTest`: every conversion row with known values, identities, unknown pairs, the table size.
- `SampleCaptureTest`: query, form and JSON redaction (case-insensitive), truncation, the ring of five.
- `PresetCatalogTest`: all 8 load, and every preset mapping compiles strictly against its own kinds and time
  base; descriptor defaults; apply (input wins, fallback, alertmanager options, bad parameters, rounding); setup
  rendering and the listing view.
- `MappingSuggestServiceTest`: draft parsing (fenced, bare, invalid, empty), hint lines, defused delimiters,
  repair text, truncation.
- `{Preset}PresetContractTest` × 8: each fixture against its golden records, with secrets and never-stored keys
  absent from the payload. Extras:
  - Ecowitt: location rounding and the PASSKEY drop;
  - WU: the receive time;
  - AIS: merging and sentinels;
  - ChirpStack: join ignored, Protobuf 415, a location event, private by default;
  - TTS: `frm-payload` wins, a non-uplink message is skipped;
  - Alertmanager: `tombstoneOnResolve` and the station;
  - airrohr: alternative sensors;
  - GMC: GET, path key and `OK.`.
- `IngestVariantsIT` (the `CollectorTestSources` context):
  - an Ecowitt form with a path key (empty 200, PASSKEY never stored) and the default-format bare endpoint;
  - WU GET with `PASSWORD` (`success`, a wrong or missing key 401, a public address 403);
  - GET on airrohr 405;
  - AIS-catcher gzip with Basic (merged), and the decompression cap 413;
  - airrohr Basic, and ChirpStack Bearer (join ignored, octet-stream 415);
  - path key off 404, on 200 (GMC `OK.`), an unknown key 401, a non-path-key format 404;
  - CIDR deny 403 and allow;
  - keys, `PASSWORD` and `PASSKEY` kept out of the fetch log, the audit log and the output.
- `MappingPreviewIT`:
  - a form preview with a GeoJSON point, the unmapped-key warning and nothing persisted;
  - preset-source warnings (time base, fieldSpecs, significant field) and the RECEIVED time;
  - normalizer behavior (null island, clamp, `MISSING_OBSERVED_AT` mapped to the item index);
  - `MAPPING_INVALID` paths, a bad sample 400, an unknown source 404, no token 401.
- `SampleCaptureIT`:
  - redaction, no headers, newest first, at most 5, the default endpoint;
  - auto-off after an hour, and turning off drops the samples;
  - the stored and proposed mapping test, an invalid proposal 400;
  - `MAPPING_NOT_SET`, `NO_SAMPLES` and 404s.
- `MappingSuggestIT` (the Ask context, `FakeLlmClient`):
  - a valid draft (purpose, no tools, preview, nothing stored);
  - two repair rounds with the errors sent back, 422 after them;
  - injection text only inside the delimiters, with an unchanged system prompt;
  - a remote provider without confirmation 409 and no call;
  - a private source refused for a remote provider and allowed for a local one;
  - `LLM_NOT_CONFIGURED`, and read, agent and anonymous callers.
- `PresetCustomSourceIT`:
  - the key and concrete setup URL once, and reads without them;
  - every preset accepted by the policy validator, with its defaults (AIS SAMPLED, 3 d, thresholds, motion;
    ChirpStack private);
  - emptied significant fields `INGEST_POLICY_INVALID`;
  - an unknown preset, bad parameters, `presetParams` without a preset, the input winning;
  - the preset immutable on update;
  - the listing.

The ITs reuse existing contexts (`CollectorTestSources`, `AskTestSources`). The unit and contract tests need no
Spring context.

## Compile result

- `./gradlew compileJava compileTestJava` succeeded with no errors.
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "830 requests across 80 tag folders …
  covers all ops: True".
- `./gradlew dependencyInsight`: `json-path:3.0.0` on both classpaths, no `json-smart` at runtime.

## Notes for the next prompt

1. **060:** no interaction. A new push device belongs in a preset file plus a `DeviceFormatsConfiguration` bean
   (only when it needs its own reply or code), a fixture and a contract test. `PresetCatalog` loads every file
   under `mapping/presets/`.
2. **086 (`rsudp-alarm`, named in the spec's listMappingPresets):** add it as a preset file. If it needs a
   format of its own, add a bean.
3. **097 checkpoint:**
   - The contract tests read fixtures from the classpath and stub `CustomSourceRepository` and `DerivedRecords`
     with Mockito.
   - `IngestVariantsIT`, `MappingPreviewIT`, `SampleCaptureIT` and `PresetCustomSourceIT` share
     `CollectorTestSources`; `MappingSuggestIT` shares `AskTestSources` and extends `AskItSupport`.
   - The ITs set the clock to `2026-10-13T15:05:00Z`. `SampleCaptureIT.autoOff` moves it 61 minutes and back.
   - First run of: the `custom_source` columns `mapping`, `default_format`, `preset` and
     `capture_samples_until`; the form-body path through MockMvc; and Jayway JSONPath inside the full context.
   - Possible fragility: `IngestVariantsIT.redaction` expects `PASSWORD=***` in a fetch-log URL, and
     `PresetCustomSourceIT.listing` expects the request base `http://…` when `publicBaseUrl` is unset.
4. **143:** point a real Ecowitt console, a WU-protocol station, AIS-catcher, ChirpStack, TTS, Alertmanager,
   airrohr and a GMC counter at a deployment with `captureSamples` on. Replace the synthetic fixtures with the
   captures, and settle Issue 1.
5. **144 (Flyway):**
   - `custom_source` gains `mapping jsonb`, `default_format varchar(32)`, `preset varchar(32)` and
     `capture_samples_until timestamptz`;
   - `formats.mapping` is no longer valid in stored rows (there are none).
6. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–058.
