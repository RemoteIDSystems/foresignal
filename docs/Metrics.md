# Foresignal metrics reference

> Every Micrometer meter the broker registers under the `foresignal.` prefix, as scraped from
> `/actuator/prometheus` (admin token). Prompt 143 created this table from the meter names in
> `src/main/java`. `MetricNamesDocumentedTest` fails when a `foresignal.*` meter is registered at startup
> and is missing here, and when a row names a meter that no longer appears in the source.
> `scripts/verify/metric-drift.sh` makes the same comparison against a live scrape and the Grafana
> dashboards in `integrations/grafana/dashboards/`.

## How names appear in Prometheus

Micrometer's Prometheus registry turns each dotted name into the exposition name by replacing `.` and `-`
with `_`, and then adds a suffix that depends on the meter type:

| Meter type | Exposition names for `foresignal.a.b` |
|---|---|
| Counter | `foresignal_a_b_total` |
| Gauge | `foresignal_a_b` (plus the base unit when one is set) |
| Timer | `foresignal_a_b_seconds_count`, `_seconds_sum`, `_seconds_max` (and `_seconds_bucket` with histograms) |
| Distribution summary | `foresignal_a_b_count`, `_sum`, `_max` |

**Many meters are registered lazily**, the first time their event happens (a dropped frame, a rejected
request, a skipped row), or only while the matching source or feature is enabled. A meter missing from a
scrape is not drift. A scraped `foresignal_*` series that matches no row below is.

The `/v1/metrics` data projections (`foresignal_data_<name>`) come from a separate registry and never appear
on `/actuator/prometheus`. They are described in the Operator Guide, "Metric projections", and are not listed
here.

**Names that cross the wire are not respelled.** `foresignal.licence.egress.denied` keeps its spelling
because dashboards and alert rules scrape it by name.

## Meters

| Metric | Emitted by | What it measures |
|---|---|---|
| `foresignal.ais.dropped` | `AivdmSocketCodec` | AIVDM sentences dropped by the local AIS socket codec |
| `foresignal.ais.fragments.completed` | `AivdmSocketCodec` | Multi-sentence AIVDM messages reassembled |
| `foresignal.ais.fragments.expired` | `AivdmSocketCodec` | Incomplete AIVDM fragment groups that expired |
| `foresignal.ais.messages` | `AivdmSocketCodec` | AIVDM messages decoded from local receivers |
| `foresignal.aisstream.dropped` | `AisStreamMetrics` | aisstream messages dropped, by reason |
| `foresignal.aisstream.messages` | `AisStreamMetrics` | aisstream messages received, by type |
| `foresignal.aisstream.vessels` | `AisStreamMetrics` | Vessels held in the aisstream merged state |
| `foresignal.alerts.active` | `OperatorGauges` | Active alerts, by severity |
| `foresignal.alerts.bus.dropped` | `AlertBus` | Alert events dropped by a slow in-process subscriber |
| `foresignal.alerts.open` | `OperatorGauges` | Open alerts, by severity |
| `foresignal.analytics.bus.dropped` | `FindingEventBus` | Analytics finding events dropped by a slow subscriber |
| `foresignal.aprs.dedup` | `AprsIsAdapter` | APRS-IS packets discarded as duplicates |
| `foresignal.aprs.local.non_ax25` | `LocalAprsAdapter` | Local APRS frames that were not AX.25 |
| `foresignal.aprs.unsupported` | `AprsIsAdapter` | APRS packets of a type the adapter does not decode |
| `foresignal.archive.bytes` | `ArchiveStep` | Bytes written by the history archive step |
| `foresignal.archive.failed` | `ArchiveStep` | Archive step failures |
| `foresignal.audit.dropped` | `AuditService` | Audit events dropped because the audit queue was full |
| `foresignal.auth.backoff.active` | `AuthFailureBackoff` | Clients currently in authentication-failure backoff |
| `foresignal.auth.failures` | `AuthFailureBackoff` | Failed authentication attempts |
| `foresignal.canary.latency` | `CanaryService` | Ingest canary round trip |
| `foresignal.cctv.skipped` | `CctvPackAdapter` | CCTV pack rows skipped, by reason |
| `foresignal.channel.mesh.airtime_seconds` | `AirtimeGovernors` | Airtime used by mesh notification channels |
| `foresignal.channel.mesh.deferred` | `AirtimeGovernors` | Mesh notifications deferred by the airtime governor |
| `foresignal.channel.mesh.dropped` | `AirtimeGovernors` | Mesh notifications dropped by the airtime governor |
| `foresignal.channel.mesh.folded` | `AirtimeGovernors` | Mesh notifications folded into a later message |
| `foresignal.chatops.rejected` | `CommandRouter` | ChatOps commands rejected |
| `foresignal.derive.eval.duration` | `DerivedMetrics` | Derived generator evaluation time, by generator |
| `foresignal.derive.feed.truncated` | `DefaultObservationFeed` | Derived observation feeds truncated at their limit |
| `foresignal.derive.state.size` | `DerivedMetrics` | Derived generator state size, by generator |
| `foresignal.ecowitt.unknown_ids` | `EcowittAdapter` | Ecowitt payload fields with unknown ids |
| `foresignal.enrichment.dropped` | `EnrichmentWorker` | Enrichment jobs dropped because the queue was full |
| `foresignal.enrichment.duration` | `EnrichmentService` | LLM enrichment time per alert |
| `foresignal.enrichment.failed` | `EnrichmentService` | Enrichment attempts that failed |
| `foresignal.enrichment.skipped` | `EnrichmentService` | Enrichment attempts skipped |
| `foresignal.enrichment.succeeded` | `EnrichmentService` | Enrichment attempts that succeeded |
| `foresignal.entity.conflicts` | `EntityResolver` | Entity resolution conflicts |
| `foresignal.entity.invalid_keys` | `EntityResolver` | Entity keys rejected as invalid, by type |
| `foresignal.entity.reassigned` | `EntityResolver` | Weak entity keys reassigned, by type |
| `foresignal.env.skipped` | `EnvSources` | Environmental source rows skipped, by reason |
| `foresignal.export.duration` | `ExportJobService` | Export job run time |
| `foresignal.export.rows` | `ExportJobService` | Rows written by export jobs |
| `foresignal.federation.compact.queued` | `CompactOutboxService` | Items queued in the compact federation outbox |
| `foresignal.federation.denied` | `FederationFeedService` | Federation feed items withheld from a peer |
| `foresignal.federation.dropped` | `FederationIngestor` | Federation items dropped on ingest |
| `foresignal.federation.items` | `FederationIngestor` | Federation items ingested |
| `foresignal.federation.lag_seq` | `PeerRuntime` | Federation peer lag, in seq |
| `foresignal.fetch.duration` | `FetchExecutor` | Upstream fetch time, by source |
| `foresignal.fetch.skipped` | `FetchExecutor` | Fetches skipped, by reason |
| `foresignal.fire.skipped` | `FireFetch` | Fire source rows skipped, by reason |
| `foresignal.gbfs.unmatched` | `GbfsAdapter` | GBFS status rows with no matching station |
| `foresignal.gbfs.untimed` | `GbfsAdapter` | GBFS rows with no usable timestamp |
| `foresignal.gdelt.files_skipped` | `GdeltEventsAdapter` | GDELT export files skipped |
| `foresignal.history.downsample.duration` | `HistoryDownsampler` | History downsampling run time |
| `foresignal.history.downsample.failures` | `HistoryDownsampler` | History downsampling failures |
| `foresignal.history.downsample.rows_after` | `HistoryDownsampler` | Rows kept by downsampling |
| `foresignal.history.downsample.rows_before` | `HistoryDownsampler` | Rows considered by downsampling |
| `foresignal.history.partition.headroom` | `PartitionManager` | Headroom of the history partitions created ahead, by tier |
| `foresignal.history.rows` | `OperatorGauges` | Rows in the history tiers, by tier |
| `foresignal.humanitarian.country.unresolved` | `UnresolvedCountries` | Humanitarian rows whose country could not be resolved |
| `foresignal.hydro.skipped` | `HydroSources` | Hydrology source rows skipped, by reason |
| `foresignal.ingest.batch.duration` | `IngestWriter` | Ingest writer batch time, by phase |
| `foresignal.ingest.batch.retries` | `IngestWriter` | Ingest writer batch retries |
| `foresignal.ingest.clock.clamped` | `IngestWriter` | Clamps applied by the ingest clock guard |
| `foresignal.ingest.clock.jump` | `IngestWriter` | Wall-clock jumps seen by the clock guard, by direction |
| `foresignal.ingest.persist` | `IngestWriter` | Versions persisted, by source and persist reason |
| `foresignal.ingest.purge.requeued` | `IngestWriter` | Purges requeued by the writer |
| `foresignal.ingest.queue.depth` | `IngestQueue` | Observations waiting in the ingest queue |
| `foresignal.ingest.records.clamped` | `IngestWriter` | Records whose time was clamped, by source |
| `foresignal.ingest.records.late` | `IngestWriter` | Records that arrived late, by source |
| `foresignal.ingest.records.live` | `IngestWriter` | Live-state updates, by source |
| `foresignal.ingest.records.persisted` | `IngestWriter` | Records persisted as versions, by source |
| `foresignal.ingest.records.rejected` | `IngestWriter` | Records rejected, by source |
| `foresignal.ingest.records.seen` | `IngestWriter` | Observations seen by the writer, by source |
| `foresignal.ingest.records.skipped` | `IngestWriter` | Records skipped, by source |
| `foresignal.ingest.records.touched` | `IngestWriter` | Records touched without a new version, by source |
| `foresignal.ingest.records.touches_throttled` | `IngestWriter` | Touches throttled, by source |
| `foresignal.ingest.records.valid_until_ignored` | `IngestWriter` | Upstream `valid_until` values ignored, by source |
| `foresignal.ingest.repair.live_missing` | `IngestWriter` | Missing live rows repaired by the writer |
| `foresignal.ingest.writer.lock_lost` | `IngestWriter` | Times the writer lost its advisory lock |
| `foresignal.ingest.writer.state` | `IngestWriter` | Writer state code |
| `foresignal.intel.bus.dropped` | `IntelEventBus` | Intelligence events dropped by a slow subscriber |
| `foresignal.ioda.region_unmatched` | `IodaGeometry` | IODA regions with no matching geometry |
| `foresignal.kit.features.rejected` | `KitFeatureAdapter` | Kit feature rows rejected, by reason |
| `foresignal.kit.feed.skipped` | `KitFeedAdapter` | Kit feed items skipped, by reason |
| `foresignal.kiwisdr.skipped` | `KiwiSdrAdapter` | KiwiSDR rows skipped, by reason |
| `foresignal.licence.egress.denied` | `LicenceEgressGuard` | Egress refused by the license guard, by path |
| `foresignal.media.guard.rejected` | `ImageGuard` | Images rejected by the media guard, by reason |
| `foresignal.media.orphans` | `MediaRetentionStep` | Orphaned media files removed, by type |
| `foresignal.mesh.dropped` | `MeshtasticStream` | Meshtastic packets dropped, by reason |
| `foresignal.mesh.unknown_types` | `MeshtasticStream` | Meshtastic packets of an unknown type |
| `foresignal.mqtt.connected` | `MqttConnections` | Whether each MQTT connection is up |
| `foresignal.nhc.cones.skipped` | `NhcCycloneConesAdapter` | NHC cone features skipped, by reason |
| `foresignal.nhc.windprobs.skipped` | `NhcWindProbsAdapter` | NHC wind probability features skipped, by reason |
| `foresignal.plugin.rejected` | `WasmFeedAdapter` | Plugin feed records rejected, by reason |
| `foresignal.projection.coercion_failures` | `ProjectionService` | Non-numeric values skipped by `/v1/metrics` projections |
| `foresignal.pskreporter.skipped` | `PskReporterAdapter` | PSK Reporter rows skipped, by reason |
| `foresignal.radio.skipped` | `RadioBrowserAdapter` | Radio Browser rows skipped, by reason |
| `foresignal.ratelimit.rejected` | `ClientRateLimiter` | Requests rejected by the client rate limiter |
| `foresignal.records.current` | `OperatorGauges` | Current records in live state |
| `foresignal.remoteid.dropped` | `LocalRemoteIdAdapter` | Remote ID frames dropped, by reason |
| `foresignal.retention.consumer_lag` | `RetentionJob` | Retention consumer clamp lag |
| `foresignal.retention.deleted` | `RetentionJob` | Rows deleted by retention |
| `foresignal.retention.duration` | `RetentionJob` | Retention run time |
| `foresignal.roads.skipped` | `RoadFetch` | Road source rows skipped, by reason |
| `foresignal.satdump.dcs.dropped` | `SatDumpExtras` | SatDump DCS messages dropped, by reason |
| `foresignal.sink.delivered` | `SinkSupervisor` | Items delivered by outbound sinks, by sink and type |
| `foresignal.sink.denied` | `SinkSupervisor` | Items an outbound sink was not allowed to send, by reason |
| `foresignal.sink.failures` | `SinkSupervisor` | Outbound sink delivery failures, by sink |
| `foresignal.situations.bus.dropped` | `SituationEventBus` | Situation events dropped by a slow subscriber |
| `foresignal.socket.connects` | `SocketMetrics` | Socket collector connections |
| `foresignal.socket.frames` | `SocketMetrics` | Frames read by socket collectors |
| `foresignal.socket.frames.dropped` | `SocketMetrics` | Frames dropped by socket collectors |
| `foresignal.socket.udp.rejected` | `SocketMetrics` | UDP datagrams rejected by socket collectors |
| `foresignal.source.consecutive_failures` | `HealthService` | Consecutive fetch failures, by source |
| `foresignal.source.data_age_seconds` | `OperatorGauges` | Age of each source's newest data |
| `foresignal.source.status` | `HealthService` | Health status code, by source |
| `foresignal.sse.clients` | `OperatorGauges` | Connected SSE clients, by stream |
| `foresignal.stream.change.dropped` | `ChangeBus` | Change events dropped by a slow subscriber |
| `foresignal.stream.live.dropped` | `LiveBus` | Live events dropped by a slow subscriber |
| `foresignal.stream.records.dropped` | `PolicyAwareRecordSink` | Records dropped before the ingest queue |
| `foresignal.transit.untimed` | `TransitVehicleAdapter` | Transit vehicle positions with no usable timestamp |
| `foresignal.watchdog.pings` | `WatchdogService` | Watchdog pings, by kind and result |
