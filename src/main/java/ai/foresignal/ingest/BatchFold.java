package ai.foresignal.ingest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;

/**
 * Folds one batch's observations through the decision table against the loaded entity states
 * (Core-Data-Model §6.2 step 6). Pure: no database, no clock.
 *
 * <p>Observations arrive stable-sorted by {@code (externalId, observedAt, arrival)}. For each one
 * the fold runs {@link IngestPolicyEngine#prepare}, {@link IngestPolicyEngine#gateFlip},
 * {@link IngestPolicyEngine#decide} and {@link EntityState#apply}, so every observation sees the
 * state the previous one of its entity produced.
 *
 * <p>Seqs are not known yet (they are allocated after the fold, once the number of versions is
 * known), so the k-th persisted version gets the <em>provisional</em> seq {@code −(k + 1)}. A
 * version's {@code prev_seq} and a head's seq may therefore be provisional;
 * {@link Result#resolve} maps them to the allocated seqs.
 */
final class BatchFold {

    private BatchFold() {}

    /**
     * One version to insert.
     *
     * @param index its position among the batch's versions (fold order)
     * @param entity the entity's plan
     * @param prepared the observation, after carry-forward and hashing
     * @param reason why it is persisted
     * @param prevSeq the entity's previous seq (possibly provisional), or null for its first
     * @param version the per-entity version number
     * @param prevPersistedBbox the persisted geometry's box before this version
     * @param prevLiveBbox the live geometry's box before this version
     * @param validUntil the effective expiry
     */
    record PlannedVersion(
            int index,
            EntityPlan entity,
            PreparedObservation prepared,
            PersistReason reason,
            Long prevSeq,
            int version,
            Envelope prevPersistedBbox,
            Envelope prevLiveBbox,
            Instant validUntil) {

        Observation observation() {
            return prepared.observation();
        }
    }

    /** Everything the fold decided about one entity. */
    static final class EntityPlan {

        final String externalId;
        final EntityState initial;
        EntityState state;
        boolean persisted;
        boolean live;
        int touches;
        /** True when the last PERSIST or LIVE outcome was LIVE (the live row ends dirty). */
        boolean endsLive;
        /** The last observation with a PERSIST or LIVE outcome. */
        PreparedObservation lastContent;
        /** The last persisted observation. */
        PreparedObservation lastPersisted;
        /** The latest {@code received_at} among the entity's non-LATE observations. */
        Instant maxReceivedNonLate;

        EntityPlan(String externalId, EntityState initial) {
            this.externalId = externalId;
            this.initial = initial;
            this.state = initial;
        }

        /** True when the entity needs a live upsert (step 11). */
        boolean needsLiveUpsert() {
            return persisted || live;
        }

        /** True when the entity's only writes are touches (step 12). */
        boolean touchOnly() {
            return touches > 0 && !needsLiveUpsert();
        }
    }

    /** The outcome of a fold. */
    static final class Result {

        final List<PlannedVersion> versions;
        final Map<String, EntityPlan> entities;
        final Map<PersistReason, Integer> byReason;
        final int live;
        final int touches;
        final int throttled;
        final int late;
        final int skipped;

        Result(
                List<PlannedVersion> versions,
                Map<String, EntityPlan> entities,
                Map<PersistReason, Integer> byReason,
                int live,
                int touches,
                int throttled,
                int late,
                int skipped) {
            this.versions = Collections.unmodifiableList(versions);
            this.entities = Collections.unmodifiableMap(entities);
            this.byReason = Collections.unmodifiableMap(byReason);
            this.live = live;
            this.touches = touches;
            this.throttled = throttled;
            this.late = late;
            this.skipped = skipped;
        }

        /** The number of versions, {@code n} of §6.2 step 7. */
        int persisted() {
            return versions.size();
        }

        /**
         * Maps a possibly provisional seq to the allocated one.
         *
         * @param seq a real seq (positive) or a provisional one ({@code −(k + 1)})
         * @param allocated the allocated seqs, ascending, one per version
         */
        static long resolve(long seq, long[] allocated) {
            return seq < 0 ? allocated[(int) (-seq - 1)] : seq;
        }
    }

    /**
     * Folds {@code observations}.
     *
     * @param profile the source's profile
     * @param snapshot the gate snapshot, read once for the batch
     * @param observations sorted observations
     * @param loaded the loaded state per external id; entities without an entry have no head
     */
    static Result fold(
            SourceIngestProfile profile,
            GateSnapshot snapshot,
            List<Observation> observations,
            Map<String, EntityState> loaded) {
        return fold(profile, snapshot, observations, loaded, false);
    }

    /**
     * Folds {@code observations}.
     *
     * @param forceTouch true for a complete-set reference run (prompt 094): an unchanged record the throttle
     *     would suppress is written as a TOUCH instead, so its {@code last_received_at} always advances and
     *     the complete-set reconciliation never mistakes it for a record that disappeared
     */
    static Result fold(
            SourceIngestProfile profile,
            GateSnapshot snapshot,
            List<Observation> observations,
            Map<String, EntityState> loaded,
            boolean forceTouch) {
        Map<String, EntityPlan> entities = new LinkedHashMap<>();
        List<PlannedVersion> versions = new ArrayList<>();
        Map<PersistReason, Integer> byReason = new EnumMap<>(PersistReason.class);
        int live = 0;
        int touches = 0;
        int throttled = 0;
        int late = 0;
        int skipped = 0;
        for (Observation observation : observations) {
            EntityPlan plan =
                    entities.computeIfAbsent(
                            observation.externalId(),
                            id -> new EntityPlan(id, loaded.getOrDefault(id, EntityState.absent())));
            EntityState state = plan.state;
            PreparedObservation prepared = IngestPolicyEngine.prepare(observation, state, profile);
            boolean flip = IngestPolicyEngine.gateFlip(prepared, state, profile, snapshot);
            Decision decision = IngestPolicyEngine.decide(prepared, state, profile, flip);
            if (decision.outcome() != Outcome.LATE) {
                Instant received = prepared.observation().receivedAt();
                if (plan.maxReceivedNonLate == null || received.isAfter(plan.maxReceivedNonLate)) {
                    plan.maxReceivedNonLate = received;
                }
            }
            switch (decision.outcome()) {
                case PERSIST -> {
                    int index = versions.size();
                    long provisional = -(index + 1L);
                    EntityState.Head head = state.head();
                    versions.add(
                            new PlannedVersion(
                                    index,
                                    plan,
                                    prepared,
                                    decision.reason(),
                                    head.exists() ? head.seq() : null,
                                    head.version() + 1,
                                    head.persistedBbox(),
                                    state.live() == null ? null : state.live().bbox(),
                                    prepared.observation().validUntil()));
                    byReason.merge(decision.reason(), 1, Integer::sum);
                    plan.state = state.apply(decision, prepared, profile, provisional);
                    plan.persisted = true;
                    plan.endsLive = false;
                    plan.lastContent = prepared;
                    plan.lastPersisted = prepared;
                }
                case LIVE -> {
                    live++;
                    plan.state = state.apply(decision, prepared, profile, 0);
                    plan.live = true;
                    plan.endsLive = true;
                    plan.lastContent = prepared;
                }
                case TOUCH -> {
                    touches++;
                    plan.state = state.apply(decision, prepared, profile, 0);
                    plan.touches++;
                }
                case TOUCH_THROTTLED -> {
                    if (forceTouch) {
                        // Prompt 094: a complete-set run always advances last_received_at, so the
                        // reconciliation can trust "not seen this run".
                        touches++;
                        plan.state = state.apply(Decision.of(Outcome.TOUCH), prepared, profile, 0);
                        plan.touches++;
                    } else {
                        throttled++;
                    }
                }
                case LATE -> late++;
                case SKIP -> skipped++;
            }
        }
        return new Result(versions, entities, byReason, live, touches, throttled, late, skipped);
    }
}
