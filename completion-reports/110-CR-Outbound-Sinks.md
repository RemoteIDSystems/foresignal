# 110-CR-Outbound-Sinks

| | |
|---|---|
| Prompt | 110-Outbound-Sinks.md |
| Status | Complete (core; some type features scoped down — see Deviations). Post-review fixes 2026-10-16. |
| Implementation commit | `d3ecbfa3d0a9dc3d6a2ccf752a3bacb755000531` |
| Date | 2026-10-15 (post-review fixes 2026-10-16) |

## Summary

Added an outbound-sink SPI with durable cursors and seven sink types. Every record passes the one
`EgressPolicy` before a type sees it (after the post-review fixes; see below). Compiles; tests written, not run.

- **SPI (`ai.foresignal.sink`):** `SinkKind`, `SinkMode`, `SinkTypeSpec`, `SinkConfig`, `SinkFilter`,
  `SinkRecord`, `SinkType`, `SinkInstance`, `SinkTypeRegistry`. Persistence `sink` (+ encrypted
  `secret_ciphertext`/`secret_nonce`, write-only) and `sink_cursor` (last/watermark seq, state, delivered,
  dropped, denied by reason and source). `SinkService` does validated CRUD; `SinkSupervisor` runs one virtual
  thread per enabled sink. `SinkCursorConsumer` adds the 019 retention clamp term; `SinkSystemBlock` feeds the
  `sinks` block of `/v1/system`.
- **Egress gate:** `SinkRecordGate` maps the mode to `EgressPath.SINK_SERVE_LAN` (SERVE) or `SINK_PUSH`
  (PUSH) and asks `EgressPolicy.decide(path, sourceId, recordTerms, payload, EgressContext.sink(includePrivate,
  localTarget))`. Denials are counted `foresignal.sink.denied{sink,reason}` and per source.
- **Types:** `mqtt-state` (060 HiveMQ client, retained state + optional change topic + LWT), `cot` (CoT XML,
  UDP multicast PUSH or SERVE TCP), `readsb-json` (JDK `HttpServer`, `aircraft.json`/`receiver.json`, hex
  merge, unit conversion), `sbs` (BaseStation MSG 1/3/4), `nmea-aivdm` (085 AIVDM encoder, types 1/18),
  `aprs-is-serve` (APRS-IS handshake, `r/`/`b/` filters, raw TNC2, 20 s keepalive), `cloudevents` (structured
  or batched CloudEvents 1.0 + HMAC).
- **SERVE safety:** `ServeGuard` (loopback/RFC 1918/link-local/ULA; `100.64/10` with `allowTailnet`; `0.0.0.0`
  only with `allowAllInterfaces`) and `SinkTcpServer` (peer check, client cap, per-client write queues). `port`
  is required for SERVE.
- **API:** `SinksController` — `listSinkTypes`, `listSinks`, `createSink` (201, disabled unless
  `enabled=true`), `getSink`, `updateSink`, `deleteSink`, `testSink`, `getSinkStatus`, `resyncSink` (admin).
  Secrets never returned; `secretsSet` lists names. AccessPolicy `/v1/sinks/**` admin; the 9 operations and
  the `deliverSinkCloudEvent` webhook flipped to `implemented` and removed from `pending-operations.txt`.

## Post-review fixes (2026-10-16)

Each finding was re-verified against the code before fixing.

**HIGH**

1. **The gate bypassed `EgressPolicy`.** `SinkRecordGate` called `LicenceEgressGuard` and `SourceVisibility`
   directly, so descriptor `egress: LOCAL_ONLY` and operator overrides were ignored (`eia-petroleum`,
   `LOCAL_ONLY` with a US-PD licence, would have been pushed off the network, breaking EGR-2). The original
   report did not disclose this. The gate now holds no policy: it calls `EgressPolicy.decide` with the record's
   resolved per-record terms (null when the resolver fell back to the descriptor, so the policy applies the
   source's current terms with overrides), the payload (MLAT rule) and the sink context. `SinkRecordGateTest`
   now builds a real `EgressPolicy` + `LicenceEgressGuard` + `SourceLicences` over descriptors and resolves
   every record's terms from its free-text licence, `egress` and private flag (it no longer hands terms in).
2. **One denied record stalled a CHANGE_FEED sink** and its cursor held back retention. Refused records are now
   skipped and counted, and the cursor moves past them; an unknown licence still counts as `LOCAL_ONLY` (the
   109 egress-safe default) and is skipped the same way.
3. **`readsb-json` with `allowAllInterfaces` served any peer.** The HTTP handlers now run behind a guard that
   drops a peer `ServeGuard.allowedPeer` refuses (no response) and caps concurrent requests at `maxClients`
   (`503`); both count as `rejected`. Status reports recent peers.

**MEDIUM**

4. **Supervisor rework** (`SinkSupervisor`, new `MirrorState`):
   - The `resync` flag is now read: `POST /v1/sinks/{id}/resync` re-snapshots a mirror and replays current
     state as `SNAPSHOT` events on a CHANGE_FEED (a stopped sink resyncs on its next start).
   - Below the purge horizon a CHANGE_FEED now snapshots and then tails (it previously jumped the cursor without
     a snapshot); a brand-new cursor and a cursor left `RESYNCING` by a crash do the same, so a new sink no
     longer replays all of history. Snapshots read every page (the original read one page).
   - Last-writer-wins by `observedAt` for every STATE_MIRROR type (previously only readsb merged by time);
     `validUntil` expiry removal is implemented on the run thread; a record that leaves the filter or stops
     passing the gate is removed, and a removal is only sent for a record that was mirrored.
   - STATE_MIRROR now also tails persisted changes from the snapshot watermark (it previously applied only
     tombstones from the ChangeBus, so new event records after the snapshot never reached a mirror).
   - Bus listeners only queue work; the CHANGE_FEED drain no longer runs on the ChangeBus thread, denial and
     delivery counters are kept in memory and written to the cursor every 5 s by the run thread.
   - `SinkTcpServer`: per-client bounded queues and writer threads (a slow client is disconnected instead of
     blocking delivery), `onConnect` (the SBS/CoT snapshot) runs on the client's thread, received lines are
     capped at 1 KiB, the injected `ServeGuard` is used for the peer check.
   - `cloudevents`: the URL is re-checked against the outbound URL policy on every delivery and the request is
     pinned to the checked address (`PinnedHttp`), closing DNS rebinding; non-2xx now raises
     `SinkDeliveryException` (429/5xx/408 retryable with `Retry-After`, other 4xx permanent), which the
     supervisor turns into backoff without advancing the cursor; `batchSize > 1` sends
     `application/cloudevents-batch+json`.
   - `filter.placeId` and `filter.watchlistId` are applied (`SinkScope`: place intersection; watchlist identity
     through the source's `entityKeys`; both fail closed); a non-UUID `watchlistId` is refused at save.
   - CoT speed is converted from knots to m/s (aircraft `gs`, vessels `sog`; course from `track`/`cog`);
     records without a point are skipped by CoT and produce no SBS position line; the CoT multicast group must be
     LAN-scoped (`239/8`, `224.0.0/24`, IPv6 link/site/org scope) and is validated at save.
   - `mqtt-state` waits for the first connect before the snapshot (the 060 client refuses to publish while
     disconnected, so the snapshot was silently lost) and republishes its retained state after reconnects.

**LOW**

5. The failing notification is raised once per failing episode (it repeated every backoff and was suppressed
   after any resync); SBS free-text fields are CSV-safe (commas and line breaks become spaces); CoT strips
   characters XML 1.0 forbids; APRS raw packets have line breaks flattened; a partial secrets update merges
   (omitted fields kept, `null` removes one) instead of dropping omitted secrets; the Docker NAT limitation of the
   per-peer check is documented in the Operator Guide.

**Undisclosed / docs**

- **Audit events for sink CRUD:** not a defect. `TokenFilter.auditIfNeeded` audits every authenticated write
  (`POST`/`PUT`/`DELETE` on `/v1/sinks/**`) with `targetType=sinks` and `targetId` = the sink id; no
  sink-specific code is needed.
- **Postman:** the original report said Postman was regenerated; it was not (the collection was last changed by
  107 and the sink requests had empty bodies). `scripts/generate-postman.py` now has BODIES for `createSink`,
  `updateSink` (a disabled `mqtt-state` example with a partial secrets update) and `testSink`; regeneration is
  left to the coordinator.
- **OpenAPI:** `SinkUpdate.secrets` documents the merge and accepts `null` values; `SinkStatus.clients.rejected`
  also counts cap refusals; changelog entry added. Status now reports the real `lastPublishedSeq`, `max`,
  `rejected` and `peers` (previously hard-coded 0/8/0/[]), and the sink `state` comes from the cursor.
- **Operator Guide "Outbound sinks"** rewritten: egress through `EgressPolicy`, what never leaves (including
  descriptor `LOCAL_ONLY`), denied-record handling, filters, delivery and resync semantics, SERVE safety, Docker
  NAT, secrets.

## Files created / modified

**Created (main, original):** `sink/{SinkKind,SinkMode,SinkRecord,SinkFilter,SinkTypeSpec,SinkConfig,
SinkTestResult,SinkInstance,SinkType,SinkTypeRegistry,SinkEntity,SinkRepository,SinkCursorEntity,
SinkCursorRepository,SinkService,SinkSupervisor,SinkRecordGate,ServeGuard,SinkTcpServer,SinkSupport,SbsEncoder,
CotEncoder,SinkCursorConsumer,SinkSystemBlock}`, `sink/types/{ServeConfig,MqttStateSinkType,CotSinkType,
ReadsbJsonSinkType,SbsSinkType,NmeaAivdmSinkType,AprsIsServeSinkType,CloudEventsSinkType}`, `api/SinksController`.

**Created (post-review):** `sink/{MirrorState,SinkScope,SinkDeliveryException}`.

**Modified (post-review):** `sink/{SinkRecordGate,SinkSupervisor,SinkTcpServer,SinkInstance,SinkRecord,
SinkFilter,SinkService,ServeGuard,CotEncoder,SbsEncoder,SinkSupport}`, `sink/types/{CotSinkType,SbsSinkType,
ReadsbJsonSinkType,CloudEventsSinkType,AprsIsServeSinkType,MqttStateSinkType}`, `api/SinksController`,
`docs/OpenAPI.json`, `docs/Operator-Guide.md`, `scripts/generate-postman.py`.

## Deviations from the prompt

1. **No server-side socket transport existed**, so `SinkTcpServer` and `ServeGuard` were added (plain TCP; the
   JDK `HttpServer` for `readsb-json`). **CoT TLS (client PKCS#12) and a TAK-server TCP client are not
   implemented** — `cot` supports UDP multicast PUSH and SERVE TCP only.
2. **`readsb-json` merges from the sink's own mirror** (freshest `observedAt` per hex); the prompt's "read only
   `derived-aircraft` when enabled" is achieved by pointing `filter.sources` at it. Ties are not broken by the
   ADS-B > MLAT > TIS-B, local > network rule.
3. **`nmea-aivdm` encodes position types 1 and 18 only**; static types 5/24A/24B, the 6-minute resend and the
   optional UDP unicast are not implemented.
4. **`aprs-is-serve`** implements the handshake, `r/lat/lon/km` and `b/CALL*` filters, raw streaming and the
   20 s keepalive; the `t/` filter is minimal (it does not filter by packet type). It never connects to APRS-IS.
5. **Rate control (`maxPerSecond`, conflation) is not implemented**; `dropped` counts only updates ignored as
   older than the mirrored state. `foresignal.sink.lag_seq`/`clients` are exposed through the status endpoint,
   not as Micrometer gauges.
6. **MCP `list_sinks`/`sink_status` are not added**; the REST API is complete.
7. **The CHANGE_FEED cursor is persisted after every acknowledged batch** (batch size 1 by default), which
   satisfies "persist `last_seq` in the same step as the ack" per batch.
8. **No new dependencies.** MQTT reuses the 060 HiveMQ client, HTTP reuses the JDK client through `PinnedHttp`,
   crypto reuses `CredentialCipher`. `MqttStateSinkIT` uses the shared Mosquitto container of the 060 tests
   (HiveMQ CE Testcontainers are not used anywhere in the tests).
9. **The private-record sink exception is implemented in `EgressPolicy`** (109 owns it): a private record may
   reach a sink only with `filter.includePrivate=true` and a local target, the conditional exception 110
   defines to 109's "private excluded on every path" rule.

## Issues found / open questions

- **Batch-snapshotted licence vs. operator override:** a stored version's licence comes from its batch's
  `fetch_log.licence`; when that resolves (not the descriptor fallback) the gate hands it to `EgressPolicy` as
  the record's terms, so an operator's `OPEN` egress override of a local receiver does not lift the
  `LOCAL_ONLY` licence snapshotted on rows written before the override. Live updates and descriptor-fallback
  rows use the source terms with the override. Whether the override should also apply to batch-licensed rows is
  an `EgressPolicy` (109) decision.
- **Docker NAT:** behind Docker's bridge network the per-peer check sees the bridge gateway for every client;
  documented, not solvable inside the container.
- **`sink`/`sink_cursor`** arrive via Hibernate `ddl-auto=update`; capture them in 144.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `sink/SinkRecordGateTest` | through a real `EgressPolicy`: OpenSky/ACLED/aisstream always denied; descriptor `LOCAL_ONLY` (eia-petroleum) never pushed; local receivers and MLAT only on a local SERVE sink; unknown licence local-only; private needs opt-in plus local target; unregistered refused; batch terms honored |
| `sink/ChangeFeedCursorIT` | new cursor: snapshot then tail in seq order; denied record skipped, counted, cursor advances, clamp not held back; no advance without ack; crash mid-page resumes without loss; purge horizon resync; clamp includes the cursor |
| `sink/SinkSupervisorIT` | enable/disable/delete lifecycle (no cursor resurrection); backoff while the target is down; one notification per failing episode; mirror snapshot, tail, refusal and resync |
| `sink/StateMirrorTest` | last writer wins by `observedAt`; expiry at `validUntil` unless replaced; tombstone/refusal removes only mirrored records; resync clears |
| `sink/SinkTcpServerTest` | disallowed peer closed and counted; `maxClients`; snapshot on connect; line limit; keepalive; non-blocking queueing |
| `sink/ServeGuardTest` | public bind refused; `0.0.0.0` needs the flag; tailnet/peer rules; LAN-scoped multicast |
| `sink/SinkScopeTest` | `placeId` and `watchlistId` matching, fail-closed cases, UUID validation |
| `sink/SinkSupportUnitsTest` | secrets merge; denial counter merge; which licence reaches the gate; local-target classification |
| `sink/SinkApiIT` | CRUD, secrets never returned and merged, admin only, test endpoint, status, SERVE/multicast validation |
| `sink/types/CloudEventsSinkIT` | WireMock over TLS: structured and batch bodies, attributes, HMAC, 503 + `Retry-After`, 400 permanent, per-delivery URL policy (rebinding) |
| `sink/types/CotSinkIT` | TCP client gets snapshot and well-formed events (m/s, control characters stripped), no-point records skipped, removal `stale = now`; multicast validation |
| `sink/types/ReadsbJsonSinkIT` | fields and units, `ground`, hex merge, `ETag`/304, `receiver.json`, refused peer, `maxClients` |
| `sink/types/AprsIsServeIT` | handshake, `r/` filter, uploads discarded, line breaks flattened, keepalive |
| `sink/types/MqttStateSinkIT` | Mosquitto: retained state, change topic, empty retained on removal, LWT `offline` on a cut |
| `sink/CotEncoderTest`, `sink/SbsEncoderTest`, `sink/AivdmRoundTripTest` | encoders (plus XML stripping, knots, CSV-safe fields) |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL` (post-review). `docs/OpenAPI.json` parses. Postman
not regenerated in this pass (the coordinator regenerates from the updated BODIES).

## Notes for the next prompt

- **111/112/113** call `EgressPolicy.decide(...)` (or its `allows` overloads) for their paths; never
  `LicenceEgressGuard` directly. `EgressPath.PUBLIC_SHARE`, `EXPORT`, `ARCHIVE` and `FEDERATION` are ready.
- **113** can reuse the sink framework pattern for `federation-push` (already in the `SinkType` enum in the spec).
- Consider finishing the scoped-down type features (CoT TCP/TLS client, AIVDM static types, the readsb tie-break,
  rate control) at a checkpoint.
- **144 (Flyway):** capture `sink` and `sink_cursor`.

Implementation commit: `d3ecbfa3d0a9dc3d6a2ccf752a3bacb755000531`
