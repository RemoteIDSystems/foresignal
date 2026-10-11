/**
 * Single-writer ingest: the pure core (this prompt) and, from prompt 009, the writer that applies
 * it to SQL.
 *
 * <p><strong>This package's core never touches the database.</strong> {@link
 * ai.foresignal.ingest.Normalizer}, {@link ai.foresignal.ingest.ContentHasher}, {@link
 * ai.foresignal.ingest.IngestPolicyEngine}, {@link ai.foresignal.ingest.EntityState} and the value
 * types are deterministic code with no Spring, JDBC or JPA dependencies, so they can be tested
 * exhaustively. The only Spring beans are {@link ai.foresignal.ingest.SourceIngestProfiles} and the
 * {@link ai.foresignal.ingest.PersistGate} implementations.
 *
 * <h2>Time contract (Core-Data-Model §2)</h2>
 *
 * <ul>
 *   <li>{@code event_time}: when a discrete event happened; hashed.
 *   <li>{@code observed_at}: when upstream last observed the state. Fixed time base per source
 *       ({@code observedAtMode}); a future value is always clamped to {@code received_at}; drives
 *       the LATE check, SAMPLED {@code Δt} and OBSERVED-anchor TTL and keyframes. Not hashed.
 *   <li>{@code received_at}: when Foresignal received it; drives the clamp, RECEIVED-anchor TTL and
 *       keyframes, the retention keyframe and the touch throttle. Not hashed.
 *   <li>{@code ingested_at}: the writer's monotonic batch time (prompt 009). Not hashed.
 * </ul>
 *
 * <p>Per entity, accepted {@code observed_at} never decreases (invariant I4): the normalizer sorts
 * by {@code (externalId, observedAt, arrival)} and the decision table drops anything older than the
 * live state as LATE, with no side effects.
 *
 * <h2>Decision table (Core-Data-Model §5.2)</h2>
 *
 * <p>First match wins: 1 LATE · 2 SKIP · 3 TOMBSTONE · 4 FIRST · 5 REAPPEAR · 6 SIGNIFICANT · 7
 * CHANGE (ALL) · 8 HEARTBEAT / KEYFRAME · 9 TRIGGER_EDGE · 10 SAMPLE (SAMPLED) · 11 TOUCH /
 * TOUCH_THROTTLED · 12 LIVE. See {@link ai.foresignal.ingest.IngestPolicyEngine} for the exact
 * conditions.
 */
package ai.foresignal.ingest;
