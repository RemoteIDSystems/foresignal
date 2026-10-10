/**
 * The contract every source adapter implements.
 *
 * <p>An adapter's whole job is to turn one upstream's responses into {@link
 * ai.elaro.source.spi.NormalizedRecord}s. It never touches the store, the scheduler or another
 * adapter (Architecture §3), which is what lets a parser be tested against a recorded fixture with
 * no Spring context and no network.
 *
 * <h2>Adapter rules</h2>
 *
 * <p>Rules 1–6 are in Architecture §4.2: one self-contained package per adapter; no direct {@code
 * HttpClient}; pure parser functions; a fixture plus a parser contract test plus a WireMock test;
 * adapters are {@code @Component}s discovered by type, with families for multi-feed parsers; ported
 * God's Eye View code carries its origin header.
 *
 * <p>Rules 7–13 are from Core-Data-Model §3 and govern what an adapter is allowed to emit:
 *
 * <ol start="7">
 *   <li><strong>The payload is a complete snapshot.</strong> Protocols that split an entity's state
 *       across messages — AIS, MQTT — merge per entity before emitting. Hashing and the ingest
 *       policies both compare whole payloads, so a partial payload reads as "everything else was
 *       removed". Verify with {@code SnapshotContract.assertStableStaticHash}.
 *   <li><strong>Slim, allowlisted payloads with no nulls.</strong> Copy the fields that are worth
 *       querying, not everything the upstream sent. Leave out anything relative to the query that
 *       produced it (distance from a search centre, index in a page), because the same entity
 *       fetched differently would then look changed. Raw counters are left out or declared
 *       volatile.
 *   <li><strong>{@code validUntil} is only an upstream-asserted expiry.</strong> Never pre-compute
 *       the descriptor TTL into it — the framework does that from the {@linkplain
 *       ai.elaro.source.spi.TtlAnchor anchor}. TRACK sources must leave it null.
 *   <li><strong>{@code eventTime} only for discrete events and measurement periods.</strong>
 *       Continuous state has no event time; using it for "when this position was valid" would make
 *       {@code effective_time} meaningless.
 *   <li><strong>Scrub sentinels.</strong> AIS latitude 91, longitude 181, speed 102.3, heading 511
 *       and their equivalents mean "unknown", not a location in the Arctic at impossible speed.
 *   <li><strong>Quality transitions that matter become significant fields.</strong> {@link
 *       ai.elaro.source.spi.Quality} is never hashed, so an adapter that needs, say, a navigation
 *       downgrade to produce a version computes an explicit payload field such as {@code
 *       navDegraded}.
 *   <li><strong>The time base is fixed per source.</strong> Pick the timestamp field in the
 *       descriptor and use it for every record. Switching fields from record to record would break
 *       the late-observation check, which assumes a single monotonic series per entity.
 * </ol>
 *
 * <h2>Blocking</h2>
 *
 * <p>{@link ai.elaro.source.spi.RecordSink#accept} may block when the ingest queue is full. That
 * back-pressure is intentional, and a streaming adapter must not call it while holding a lock its
 * read loop needs.
 */
package ai.elaro.source.spi;
