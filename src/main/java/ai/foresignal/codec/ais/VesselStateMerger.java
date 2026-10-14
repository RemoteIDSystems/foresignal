package ai.foresignal.codec.ais;

// Origin: the merge of static data into per-MMSI vessel state follows prompt 063's VesselStateStore, which is ported
// from God's Eye View (MIT, Copyright (c) 2026 Bilawal Sidhu): server/providers/vessels/ais-store.js
// (ingestAisStreamEnvelope, mergeAisStaticIntoLiveVessel, pruneAisStreamCache). See THIRD_PARTY_NOTICES.md.

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Per-MMSI vessel state merged into complete snapshots (prompt 085; adapter rule 7), shared by every keyless vessel
 * source so derived layers (099) and the NMEA sink (110) treat all vessel sources alike.
 *
 * <p><strong>Payload contract:</strong> exactly prompt 063's {@code aisstream} keys and units: {@code mmsi, name,
 * callsign, imo, shipType} (int), {@code class} ({@code A|B}), {@code destination, navStatus, draught} (m),
 * {@code lengthM} (A + B), {@code beamM} (C + D), {@code eta} (ISO-8601 UTC at the next occurrence of the reported
 * month, day, hour and minute; omitted when any part is unavailable), {@code sog, cog, heading}; each omitted when
 * unknown. The rate of turn and the position-accuracy flag go to {@code quality.extra} ({@value #ROT_KEY},
 * {@value #POS_ACC_KEY}), never the payload.
 *
 * <ul>
 *   <li><b>Snapshots:</b> every merged update returns the vessel's complete snapshot once the vessel has had a usable
 *       position; static data for an MMSI with no position yet is held, not emitted (063's rule).
 *   <li><b>Time:</b> {@code observedAt} is the newest merged update's time when the source's time base is upstream;
 *       a RECEIVED source gets no {@code observedAt} (the framework stamps the receive time). A position older than
 *       the newest position merged does not move the vessel, and static data older than the field's newest value does
 *       not replace it.
 *   <li><b>Geometry:</b> the newest position report's position, null when that report's position was a sentinel (the
 *       TRACK carry-forward then keeps the last one with {@code POSITION_STALE}).
 *   <li><b>Quality:</b> {@code positionSource} REPORTED (AIS is GNSS reported by the vessel); {@code accuracyMeters}
 *       10 when the accuracy flag is high, 100 otherwise, 185 for a type 27 long-range report.
 *   <li><b>Observers:</b> the observer of the newest update, when the source names one (Kystverket's base station,
 *       the operator's AIS-catcher).
 *   <li><b>Static cache:</b> bounded at {@link #DEFAULT_MAX_ENTRIES} MMSIs, least recently updated evicted first, and
 *       an MMSI not updated for {@link #DEFAULT_TTL} is forgotten, position and static data alike.
 *   <li><b>MMSI:</b> 100000000 to 999999999 only; anything else (0, base stations' {@code 00…}) is refused and
 *       counted.
 * </ul>
 *
 * <p>Thread-safe; time comes in with each call ({@code now}), never from a clock read here.
 */
public final class VesselStateMerger {

    /** The record kind of every vessel source. */
    public static final String KIND = "vessel";

    /** The most MMSIs held. */
    public static final int DEFAULT_MAX_ENTRIES = 100_000;

    /** An MMSI not updated this long is forgotten. */
    public static final Duration DEFAULT_TTL = Duration.ofHours(6);

    /** The {@code quality.extra} key of the raw rate of turn. */
    public static final String ROT_KEY = "ais.rot";

    /** The {@code quality.extra} key of the position accuracy flag. */
    public static final String POS_ACC_KEY = "ais.pos_acc";

    /** Accuracy in meters with the accuracy flag high (better than 10 m). */
    public static final double ACCURACY_HIGH_M = 10.0;

    /** Accuracy in meters with the flag low. */
    public static final double ACCURACY_LOW_M = 100.0;

    /** Accuracy in meters of a type 27 report (1/10 minute of latitude). */
    public static final double ACCURACY_LONG_RANGE_M = 185.0;

    /** The lowest valid MMSI. */
    public static final int MIN_MMSI = 100_000_000;

    /** The highest valid MMSI. */
    public static final int MAX_MMSI = 999_999_999;

    private final boolean upstreamTime;
    private final int maxEntries;
    private final Duration ttl;
    private final LinkedHashMap<String, State> states = new LinkedHashMap<>(1024, 0.75f, true);
    private long rejectedMmsi;

    /**
     * A merger with the default bounds.
     *
     * @param upstreamTime true when update times are upstream times and become {@code observedAt}; false for a
     *     RECEIVED source
     */
    public VesselStateMerger(boolean upstreamTime) {
        this(upstreamTime, DEFAULT_MAX_ENTRIES, DEFAULT_TTL);
    }

    /**
     * A merger with explicit bounds (tests).
     *
     * @param upstreamTime whether update times become {@code observedAt}
     * @param maxEntries the most MMSIs held
     * @param ttl how long an MMSI may go without an update
     */
    public VesselStateMerger(boolean upstreamTime, int maxEntries, Duration ttl) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.upstreamTime = upstreamTime;
        this.maxEntries = maxEntries;
        this.ttl = ttl;
    }

    /** True when {@code mmsi} is a nine-digit vessel MMSI in 100000000–999999999. */
    public static boolean validMmsi(long mmsi) {
        return mmsi >= MIN_MMSI && mmsi <= MAX_MMSI;
    }

    /** {@code mmsi} as a nine-digit string, or null when it is not valid. */
    public static String mmsiText(long mmsi) {
        return validMmsi(mmsi) ? Long.toString(mmsi) : null;
    }

    /**
     * Merges one decoded AIVDM message.
     *
     * @param message the message
     * @param time the message time (TAG {@code c}) or the receive time
     * @param observer the receiver that heard it, or null
     * @return the snapshot, or empty for a message that carries no vessel state (types 4, 21, unsupported), an invalid
     *     MMSI, or a vessel without a position yet
     */
    public Optional<NormalizedRecord> merge(AisMessage message, Instant time, Observer observer) {
        List<VesselUpdate> updates = updates(message, time);
        if (updates.isEmpty()) {
            if (carriesVessel(message) && !validMmsi(message.mmsi())) {
                synchronized (this) {
                    rejectedMmsi++;
                }
            }
            return Optional.empty();
        }
        return merge(updates, observer, time);
    }

    /**
     * Merges one update.
     *
     * @param update the update
     * @param observer the receiver, or null
     * @param now the current time, for the cache's TTL
     * @return the snapshot, or empty for an invalid MMSI or a vessel without a position yet
     */
    public Optional<NormalizedRecord> merge(VesselUpdate update, Observer observer, Instant now) {
        return merge(List.of(update), observer, now);
    }

    /**
     * Stores static data without emitting anything (Digitraffic's metadata bootstrap seeds the cache).
     *
     * @return false when the MMSI is not valid
     */
    public synchronized boolean seed(VesselUpdate.Static update, Instant now) {
        State state = state(update.mmsi(), now);
        if (state == null) {
            return false;
        }
        state.apply(update);
        return true;
    }

    /** The MMSIs held. */
    public synchronized int size() {
        return states.size();
    }

    /** Forgets every vessel. */
    public synchronized void clear() {
        states.clear();
    }

    /** Updates refused for an invalid MMSI so far. */
    public synchronized long rejectedMmsi() {
        return rejectedMmsi;
    }

    /** True when {@code mmsi} is held. */
    public synchronized boolean contains(String mmsi) {
        return states.containsKey(mmsi);
    }

    private synchronized Optional<NormalizedRecord> merge(List<VesselUpdate> updates, Observer observer,
            Instant now) {
        State state = state(updates.getFirst().mmsi(), now);
        if (state == null) {
            return Optional.empty();
        }
        for (VesselUpdate update : updates) {
            state.apply(update);
        }
        if (observer != null) {
            state.observer = observer;
        }
        return state.snapshot(upstreamTime);
    }

    private State state(String mmsi, Instant now) {
        long number;
        try {
            number = mmsi == null ? 0 : Long.parseLong(mmsi);
        } catch (NumberFormatException e) {
            number = 0;
        }
        if (!validMmsi(number)) {
            rejectedMmsi++;
            return null;
        }
        evictIdle(now);
        String key = Long.toString(number);
        State state = states.get(key);
        if (state == null) {
            state = new State(key);
            states.put(key, state);
            while (states.size() > maxEntries) {
                Iterator<String> eldest = states.keySet().iterator();
                eldest.next();
                eldest.remove();
            }
        }
        state.lastSeen = now;
        return state;
    }

    private void evictIdle(Instant now) {
        Instant cutoff = now.minus(ttl);
        Iterator<Map.Entry<String, State>> it = states.entrySet().iterator();
        while (it.hasNext()) {
            State eldest = it.next().getValue();
            if (eldest.lastSeen == null || !eldest.lastSeen.isBefore(cutoff)) {
                return;
            }
            it.remove();
        }
    }

    private static boolean carriesVessel(AisMessage message) {
        return !(message instanceof AisMessage.BaseStationReport || message instanceof AisMessage.AidToNavigation
                || message instanceof AisMessage.Unsupported);
    }

    /** The source-neutral updates of one AIVDM message; empty when it carries no vessel state or an invalid MMSI. */
    static List<VesselUpdate> updates(AisMessage message, Instant time) {
        String mmsi = mmsiText(message.mmsi());
        if (mmsi == null) {
            return List.of();
        }
        return switch (message) {
            case AisMessage.ClassAPosition m -> List.of(new VesselUpdate.Position(mmsi, time, "A", m.latitude(),
                    m.longitude(), m.sog(), m.cog(), m.heading(), m.navStatus(), m.rot(), m.accuracy(), false));
            case AisMessage.ClassBPosition m -> List.of(new VesselUpdate.Position(mmsi, time, "B", m.latitude(),
                    m.longitude(), m.sog(), m.cog(), m.heading(), null, null, m.accuracy(), false));
            case AisMessage.ClassBExtended m -> List.of(
                    new VesselUpdate.Static(mmsi, time, "B", false, m.name(), null, null, m.shipType(), m.dimension(),
                            null, null, null),
                    new VesselUpdate.Position(mmsi, time, "B", m.latitude(), m.longitude(), m.sog(), m.cog(),
                            m.heading(), null, null, m.accuracy(), false));
            case AisMessage.LongRangePosition m -> List.of(new VesselUpdate.Position(mmsi, time, null, m.latitude(),
                    m.longitude(), m.sog(), m.cog(), null, m.navStatus(), null, m.accuracy(), true));
            case AisMessage.StaticVoyage m -> List.of(new VesselUpdate.Static(mmsi, time, "A", true, m.name(),
                    m.callsign(), m.imo(), m.shipType(), m.dimension(), m.eta(), m.draft(), m.destination()));
            case AisMessage.StaticDataA m -> List.of(new VesselUpdate.Static(mmsi, time, "B", false, m.name(), null,
                    null, null, null, null, null, null));
            case AisMessage.StaticDataB m -> List.of(new VesselUpdate.Static(mmsi, time, "B", false, null,
                    m.callsign(), null, m.shipType(), m.dimension(), null, null, null));
            case AisMessage.BaseStationReport m -> List.of();
            case AisMessage.AidToNavigation m -> List.of();
            case AisMessage.Unsupported m -> List.of();
        };
    }

    /**
     * The next occurrence of an AIS ETA at or after {@code reference}, in UTC; null when the date does not exist
     * (30 February) within a year.
     */
    public static Instant nextEta(AisMessage.Eta eta, Instant reference) {
        int year = reference.atOffset(ZoneOffset.UTC).getYear();
        for (int y = year; y <= year + 1; y++) {
            try {
                Instant candidate = LocalDateTime.of(y, eta.month(), eta.day(), eta.hour(), eta.minute())
                        .toInstant(ZoneOffset.UTC);
                if (!candidate.isBefore(reference)) {
                    return candidate;
                }
            } catch (DateTimeException e) {
                // 29 February in a common year, or 31 April: try the next year
            }
        }
        return null;
    }

    /** One vessel. */
    private static final class State {

        final String mmsi;
        Instant lastSeen;
        Instant newestAt;
        Observer observer;

        // position group
        Instant positionAt;
        String positionClass;
        Double latitude;
        Double longitude;
        boolean everFixed;
        Double sog;
        Double cog;
        Integer heading;
        Integer navStatus;
        Integer rot;
        Boolean positionAccuracy;
        boolean longRange;

        // static group: each field keeps its own newest time, so a part B does not erase a part A's name
        String staticClass;
        final Field<String> name = new Field<>();
        final Field<String> callsign = new Field<>();
        final Field<Integer> imo = new Field<>();
        final Field<Integer> shipType = new Field<>();
        final Field<AisMessage.Dimension> dimension = new Field<>();
        final Field<Instant> eta = new Field<>();
        final Field<Double> draft = new Field<>();
        final Field<String> destination = new Field<>();

        State(String mmsi) {
            this.mmsi = mmsi;
        }

        void apply(VesselUpdate update) {
            Instant t = update.time();
            if (t == null) {
                return;
            }
            switch (update) {
                case VesselUpdate.Position p -> applyPosition(p, t);
                case VesselUpdate.Static s -> applyStatic(s, t);
            }
        }

        private void applyPosition(VesselUpdate.Position p, Instant t) {
            if (positionAt != null && t.isBefore(positionAt)) {
                return;
            }
            touch(t);
            positionAt = t;
            if (p.aisClass() != null) {
                positionClass = p.aisClass();
            }
            latitude = p.latitude();
            longitude = p.longitude();
            if (p.hasPosition()) {
                everFixed = true;
            }
            sog = p.sog();
            cog = p.cog();
            heading = p.heading();
            navStatus = p.navStatus();
            rot = p.rot();
            positionAccuracy = p.positionAccuracy();
            longRange = p.longRange();
        }

        private void applyStatic(VesselUpdate.Static s, Instant t) {
            touch(t);
            if (s.aisClass() != null) {
                staticClass = s.aisClass();
            }
            if (s.voyage()) {
                // The whole voyage block: an absent value there means cleared.
                destination.set(s.destination(), t, true);
                eta.set(s.eta() == null ? null : nextEta(s.eta(), t), t, true);
                draft.set(s.draft(), t, true);
                imo.set(s.imo(), t, true);
            }
            // Identity fields come from several message types; an absent value means "not carried".
            name.set(s.name(), t, false);
            callsign.set(s.callsign(), t, false);
            shipType.set(s.shipType(), t, false);
            dimension.set(s.dimension(), t, false);
        }

        private void touch(Instant t) {
            if (newestAt == null || t.isAfter(newestAt)) {
                newestAt = t;
            }
        }

        Optional<NormalizedRecord> snapshot(boolean upstreamTime) {
            if (!everFixed || newestAt == null) {
                return Optional.empty();
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(mmsi, KIND);
            if (upstreamTime) {
                b.observedAt(newestAt);
            }
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
            double accuracy = longRange ? ACCURACY_LONG_RANGE_M
                    : Boolean.TRUE.equals(positionAccuracy) ? ACCURACY_HIGH_M : ACCURACY_LOW_M;
            Map<String, Object> extra = new LinkedHashMap<>();
            if (rot != null) {
                extra.put(ROT_KEY, rot);
            }
            if (positionAccuracy != null) {
                extra.put(POS_ACC_KEY, positionAccuracy);
            }
            b.quality(new Quality(accuracy, null, PositionSource.REPORTED, extra));
            if (observer != null) {
                b.observers(List.of(observer));
            }
            return Optional.of(b.build());
        }
    }

    /** One static field with the time of the update that last set it. */
    private static final class Field<T> {

        T value;
        Instant at;

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
