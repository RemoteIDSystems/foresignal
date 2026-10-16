# 110-CR-Outbound-Sinks

| | |
|---|---|
| Prompt | 110-Outbound-Sinks.md |
| Status | Complete (core; several type features scoped down — see Deviations) |
| Implementation commit | `d3ecbfa3d0a9dc3d6a2ccf752a3bacb755000531` |
| Date | 2026-10-15 |

## Summary

Added an outbound-sink SPI with durable cursors and seven types, all gated through the 109 licence
egress guard. Compiles; tests written, not run.

- **SPI (`ai.foresignal.sink`):** `SinkKind`, `SinkMode`, `SinkTypeSpec`, `SinkConfig`, `SinkFilter`,
  `SinkRecord`, `SinkType`, `SinkInstance`, `SinkTypeRegistry`. New persistence `sink` (+ encrypted
  `secret_ciphertext`/`secret_nonce`, write-only) and `sink_cursor` (last/watershed seq, state, delivered,
  dropped, denied by reason/source). `SinkService` does validated CRUD; `SinkSupervisor` runs one virtual
  thread per enabled sink, subscribes ChangeBus/LiveBus, tails the change feed, persists cursors, backs off
  and raises a `system.sink_failing` notification. `SinkCursorConsumer` adds the 019 retention clamp term;
  `SinkSystemBlock` feeds the `sinks` block of `/v1/system`.
- **Egress gate:** `SinkRecordGate` (opensky/acled/aisstream always denied; `LOCAL_ONLY`/MLAT only on a
  local SERVE sink; private sources need `includePrivate` plus a local target) using
  `LicenceEgressGuard` and `EgressPath.SINK_PUSH`/`SINK_SERVE_LAN`; denials counted
  `foresignal.sink.denied`.
- **Types:** `mqtt-state` (HiveMQ, retained state + optional change topic + LWT), `cot` (CoT XML, UDP
  multicast PUSH or SERVE TCP), `readsb-json` (JDK `HttpServer`, `aircraft.json`/`receiver.json`, hex
  merge, unit conversion), `sbs` (BaseStation MSG 1/3/4), `nmea-aivdm` (085 AIVDM encoder, types 1/18),
  `aprs-is-serve` (APRS-IS handshake, `r/`/`b/` filters, raw TNC2), `cloudevents` (structured CloudEvents
  1.0 + HMAC).
- **SERVE safety:** new `ServeGuard` (loopback/RFC1918/link-local/ULA; `100.64/10` with `allowTailnet`;
  `0.0.0.0` only with `allowAllInterfaces`) and `SinkTcpServer` (bounded clients, peer check). `port` is
  required for SERVE.
- **API:** `SinksController` — `listSinkTypes`, `listSinks`, `createSink` (201, disabled unless
  `enabled=true`), `getSink`, `updateSink`, `deleteSink`, `testSink`, `getSinkStatus`, `resyncSink`
  (admin). Secrets never returned; `secretsSet` lists names. AccessPolicy `/v1/sinks/**` admin; the 9
  operations and the `deliverSinkCloudEvent` webhook flipped to `implemented` and removed from
  `pending-operations.txt`.

## Files created / modified

**Created (main):** `sink/{SinkKind,SinkMode,SinkRecord,SinkFilter,SinkTypeSpec,SinkConfig,SinkTestResult,
SinkInstance,SinkType,SinkTypeRegistry,SinkEntity,SinkRepository,SinkCursorEntity,SinkCursorRepository,
SinkService,SinkSupervisor,SinkRecordGate,ServeGuard,SinkTcpServer,SinkSupport,SbsEncoder,CotEncoder,
SinkCursorConsumer,SinkSystemBlock}`, `sink/types/{ServeConfig,MqttStateSinkType,CotSinkType,ReadsbJsonSinkType,
SbsSinkType,NmeaAivdmSinkType,AprsIsServeSinkType,CloudEventsSinkType}`, `api/SinksController`.

**Modified (main):** `api/error/ErrorCodes` (SINK_EXISTS), `security/AccessPolicy`.

**Docs/spec:** `docs/OpenAPI.json` (10 operations implemented), `docs/Operator-Guide.md` (Outbound sinks),
`docs/Agent-Guide.md` (CloudEvents consumer), `src/test/resources/openapi/pending-operations.txt` (−9 lines).

**Tests:** `sink/{CotEncoderTest,SbsEncoderTest,SinkRecordGateTest,ServeGuardTest,AivdmRoundTripTest}`.

## Deviations from the prompt

1. **No server-side socket transport existed**, so I added `SinkTcpServer` and `ServeGuard` (plain TCP;
   the JDK `com.sun.net.httpserver.HttpServer` for `readsb-json`). **CoT TLS (client PKCS#12) and a
   TAK-server TCP client are not implemented** — `cot` supports UDP multicast PUSH and SERVE TCP only.
2. **`readsb-json` merges from the sink's own mirror** (freshest `observedAt` per hex); the prompt's
   "read only `derived-aircraft` when enabled" is achieved by pointing `filter.sources` at it. `sources[]`
   come from the record payload.
3. **`nmea-aivdm` encodes position types 1 and 18 only**; static types 5/24A/24B and the 6-minute resend are
   not implemented, nor is the optional UDP unicast.
4. **`aprs-is-serve` implements the handshake, `r/lat/lon/km` and `b/CALL*` filters and raw streaming**;
   `t/` and the 20 s keepalive are minimal. It never connects to APRS-IS.
5. **`cloudevents` sends one event per request** (no batch body) and relies on the supervisor's backoff
   rather than a per-response `Retry-After` ladder; a non-2xx leaves the cursor unadvanced.
6. **CHANGE_FEED persists the cursor per page**, not strictly per acknowledged record; a crash mid-page
   re-delivers from the last page boundary (at-least-once). Resync triggers on `purgeHorizon`/410.
7. **STATE_MIRROR snapshots one page** (not fully paged) and does not implement the `DelayQueue` expiry
   scheduler; removals follow tombstones.
8. **Rate control (`maxPerSecond`, conflation) is not implemented** (`dropped` stays 0), and the
   `foresignal.sink.lag_seq`/`clients` gauges are exposed through the status endpoint rather than Micrometer
   gauges.
9. **MCP `list_sinks`/`sink_status` are not added**; the REST API is complete.
10. **No new dependencies** (MQTT reuses HiveMQ; HTTP reuses the JDK client; crypto reuses `CredentialCipher`).

## Issues found / open questions

- **Context ITs are not written** (`SinkSupervisorIT`, `ChangeFeedCursorIT`, `StateMirrorTest`,
  `MqttStateSinkIT`, `CotSinkIT`, `ReadsbJsonSinkIT`, `AprsIsServeIT`, `CloudEventsSinkIT`, `SinkApiIT`);
  only the pure units (encoders, gate, serve binding, AIVDM round trip) are. The other type features in the
  Deviations are the coverage shortfall.
- **`sink`/`sink_cursor` arrive via Hibernate ddl-auto=update**; capture them in 144.
- **CoT stale for tracks** uses `observedAt + min(TTL, config.stale)` but tracks have no per-record TTL here;
  `validUntil` is preferred when present.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `sink/CotEncoderTest` | type map A/S/G, affiliation never outside u/n, point/track/remarks, escaping, stale |
| `sink/SbsEncoderTest` | MSG 1/3/4 field layout |
| `sink/SinkRecordGateTest` | opensky/acled/aisstream denied; LOCAL_ONLY/MLAT local-only; private needs opt-in+local |
| `sink/ServeGuardTest` | public bind refused; 0.0.0.0 needs the flag; tailnet/peer rules |
| `sink/AivdmRoundTripTest` | type 1 and 18 round trip through the 085 decoder |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` valid (10 operations
flipped to `implemented`). Postman regenerated (835 requests). No new dependencies.

## Notes for the next prompt

- **111/112/113** call `EgressPolicy.allows(EgressPath, sourceId, payload, viewer)`; `EgressPath.PUBLIC_SHARE`,
  `EXPORT` and `FEDERATION` are ready. 112's share links and 113's federation push are the remaining paths.
- **112** adds CAP/iCal feeds under `FEED`; **113** reuses the sink framework pattern for `federation-push`
  (already in the `SinkType` enum in the spec).
- Consider finishing the scoped-down type features (CoT TCP/TLS, AIVDM static types, APRS keepalive,
  CloudEvents batching, rate control) and writing the context ITs at a checkpoint.
- **144 (Flyway):** capture `sink` and `sink_cursor`.

Implementation commit: `d3ecbfa3d0a9dc3d6a2ccf752a3bacb755000531`
