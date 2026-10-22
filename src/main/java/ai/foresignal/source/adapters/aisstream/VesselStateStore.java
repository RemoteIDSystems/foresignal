package ai.foresignal.source.adapters.aisstream;

// Origin: the merge of static data into per-MMSI vessel state is ported from God's Eye View (MIT, Copyright
// (c) 2026 Bilawal Sidhu): server/providers/vessels/ais-store.js (ingestAisStreamEnvelope,
// mergeAisStaticIntoLiveVessel, pruneAisStreamCache). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Per-MMSI merged vessel state (prompt 063; adapter rule 7: the payload is a complete snapshot).
 *
 * <p>AIS splits a vessel over position reports and static messages. Every recognized message is merged into
 * the vessel's state, and the store returns <strong>one complete snapshot</strong> of the vessel:
 *
 * <ul>
 *   <li>{@code externalId} is the MMSI; {@code observedAt} is the time of the newest message merged, never
 *       a receive time;
 *   <li>geometry is the newest position report's position, null when that report's position was a sentinel
 *       (the TRACK carry-forward then keeps the last one with {@code POSITION_STALE});
 *   <li>{@code quality} is REPORTED, with {@code extra["ais.position_age_s"]}, the age of the last usable
 *       position at {@code observedAt};
 *   <li>the payload holds {@code mmsi, name, callsign, imo, shipType, class, destination, navStatus,
 *       draught, lengthM, beamM, eta, sog, cog, heading}, each omitted when unknown.
 * </ul>
 *
 * <p>Static data for an MMSI that has never had a usable position is held, not emitted. A position report
 * older than the newest position merged does not move the vessel, and static data older than the newest
 * static data merged does not replace it, so late messages cannot roll the state back.
 *
 * <p>The store is bounded: at most {@link #DEFAULT_MAX_ENTRIES} vessels, and a vessel unseen for
 * {@link #DEFAULT_IDLE_EVICTION} (by the injected clock) is evicted. Thread-safe.
 */
public final class VesselStateStore {

    /** The bound on vessels held. */
    public static final int DEFAULT_MAX_ENTRIES = 200_000;

    /** A vessel unseen this long is evicted. */
    public static final Duration DEFAULT_IDLE_EVICTION = Duration.ofHours(6);

    /** The {@code quality.extra} key of the merged position's age, in seconds. */
    public static final String POSITION_AGE_KEY = "ais.position_age_s";

    private final Clock clock;
    private final int maxEntries;
    private final Duration idleEviction;

    /** Access-ordered by last update: the eldest entry is the one unseen the longest. */
    private final LinkedHashMap<String, State> states = new LinkedHashMap<>(1024, 0.75f, true);

    /** A store with the default bounds. */
    public VesselStateStore(Clock clock) {
        this(clock, DEFAULT_MAX_ENTRIES, DEFAULT_IDLE_EVICTION);
    }

    /**
     * A store with explicit bounds (tests).
     *
     * @param maxEntries the most vessels held
     * @param idleEviction how long a vessel may go unseen before it is evicted
     */
    public VesselStateStore(Clock clock, int maxEntries, Duration idleEviction) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.clock = clock;
        this.maxEntries = maxEntries;
        this.idleEviction = idleEviction;
    }

    /**
     * Merges one message and returns the vessel's snapshot.
     *
     * @return the complete snapshot, or empty while the vessel has never had a usable position
     */
    public synchronized Optional<NormalizedRecord> merge(AisMessage message) {
        Instant now = clock.instant();
        evictIdle(now);
        State state = states.get(message.mmsi());
        if (state == null) {
            state = new State(message.mmsi());
            states.put(message.mmsi(), state);
            while (states.size() > maxEntries) {
                Iterator<String> eldest = states.keySet().iterator();
                eldest.next();
                eldest.remove();
            }
        }
        state.lastSeen = now;
        state.apply(message);
        return state.snapshot();
    }

    /** How many vessels are held. */
    public synchronized int size() {
        return states.size();
    }

    /** Forgets every vessel. */
    public synchronized void clear() {
        states.clear();
    }

    /** True when {@code mmsi} is held (tests). */
    synchronized boolean contains(String mmsi) {
        return states.containsKey(mmsi);
    }

    private void evictIdle(Instant now) {
        Instant cutoff = now.minus(idleEviction);
        Iterator<Map.Entry<String, State>> it = states.entrySet().iterator();
        while (it.hasNext()) {
            State eldest = it.next().getValue();
            if (!eldest.lastSeen.isBefore(cutoff)) {
                return;
            }
            it.remove();
        }
    }

    /**
     * The next occurrence of an AIS ETA at or after {@code reference}, in UTC; null when the date does not
     * exist (30 February).
     */
    static Instant nextEta(AisMessage.Eta eta, Instant reference) {
        int year = reference.atOffset(ZoneOffset.UTC).getYear();
        for (int y = year; y <= year + 1; y++) {
            try {
                Instant candidate = LocalDateTime.of(y, eta.month(), eta.day(), eta.hour(), eta.minute())
                        .toInstant(ZoneOffset.UTC);
                if (!candidate.isBefore(reference)) {
                    return candidate;
                }
            } catch (DateTimeException e) {
                // 29 February in a common year: try the next year
            }
        }
        // 29 February with no leap year in reach of the next year.
        return null;
    }

    /** One vessel. */
    private static final class State {

        final String mmsi;
        Instant lastSeen;
        Instant newestAt;

        // position group
        Instant positionAt;
        String positionClass;
        Double latitude;
        Double longitude;
        Instant lastFixAt;
        Double sog;
        Double cog;
        Integer heading;
        Integer navStatus;

        // static group: each field keeps its own newest time, so a part B does not erase a part A's name
        String staticClass;
        Field<String> name = new Field<>();
        Field<String> callsign = new Field<>();
        Field<Integer> imo = new Field<>();
        Field<Integer> shipType = new Field<>();
        Field<AisMessage.Dimension> dimension = new Field<>();
        Field<Instant> eta = new Field<>();
        Field<Double> draft = new Field<>();
        Field<String> destination = new Field<>();

        State(String mmsi) {
            this.mmsi = mmsi;
        }

        void apply(AisMessage m) {
            Instant t = m.time();
            if (newestAt == null || t.isAfter(newestAt)) {
                newestAt = t;
            }
            if (m.position() && (positionAt == null || !t.isBefore(positionAt))) {
                positionAt = t;
                positionClass = m.aisClass();
                latitude = m.latitude();
                longitude = m.longitude();
                if (m.hasPosition()) {
                    lastFixAt = t;
                }
                sog = m.sog();
                cog = m.cog();
                heading = m.heading();
                navStatus = m.navStatus();
            }
            if (!m.position() && staticClass == null) {
                staticClass = m.aisClass();
            }
            if (isShipStatic(m)) {
                // ShipStaticData carries the whole voyage block: an absent value there means cleared.
                destination.set(m.destination(), t, true);
                eta.set(m.eta() == null ? null : nextEta(m.eta(), t), t, true);
                draft.set(m.draft(), t, true);
                imo.set(m.imo(), t, true);
            }
            // The identity fields come from several message types; an absent value means "not carried".
            name.set(m.name(), t, false);
            callsign.set(m.callsign(), t, false);
            shipType.set(m.shipType(), t, false);
            dimension.set(m.dimension(), t, false);
        }

        private static boolean isShipStatic(AisMessage m) {
            return AisStreamMessageParser.SHIP_STATIC_DATA.equals(m.messageType());
        }

        Optional<NormalizedRecord> snapshot() {
            if (lastFixAt == null) {
                return Optional.empty();
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(mmsi, AisStreamAdapter.KIND).observedAt(newestAt);
            if (latitude != null && longitude != null) {
                b.point(longitude, latitude);
            }
            AisMessage.Dimension dims = dimension.value;
            b.put("mmsi", mmsi)
                    .put("name", name.value)
                    .put("callsign", callsign.value)
                    .put("imo", imo.value)
                    .put("shipType", shipType.value)
                    .put("class", positionClass != null ? positionClass : staticClass)
                    .put("destination", destination.value)
                    .put("navStatus", navStatus)
                    .put("draught", draft.value)
                    .put("lengthM", dims == null ? null : dims.lengthM())
                    .put("beamM", dims == null ? null : dims.beamM())
                    .put("eta", eta.value == null ? null : eta.value.toString())
                    .put("sog", sog)
                    .put("cog", cog)
                    .put("heading", heading);
            long ageSeconds = Math.max(0, Duration.between(lastFixAt, newestAt).toSeconds());
            b.quality(Quality.positionSource(PositionSource.REPORTED).withExtra(POSITION_AGE_KEY, ageSeconds));
            return Optional.of(b.build());
        }
    }

    /** One static field with the time of the message that last set it. */
    private static final class Field<T> {

        T value;
        Instant at;

        /**
         * Applies {@code candidate} from a message at {@code t}: a newer or equal message wins.
         *
         * @param clearing true when an absent value in that message means "cleared", false when it means
         *     "this message does not carry the field"
         */
        void set(T candidate, Instant t, boolean clearing) {
            if (candidate == null && !clearing) {
                return;
            }
            if (at == null || !t.isBefore(at)) {
                value = candidate;
                at = t;
            }
        }
    }
}
