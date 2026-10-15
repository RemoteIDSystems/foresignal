package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-emergency-squawk} (prompt 099): an aircraft transmitting 7500 (unlawful
 * interference), 7600 (radio failure) or 7700 (emergency), or a readsb {@code emergency} other than
 * {@code none}, seen in at least two observations at least 10 seconds apart (or from two sources).
 *
 * <p>The payload states the code and its meaning and is marked {@code unverified}. The geometry is
 * the live position and is updated when it moves at least 10 km. The record is tombstoned once the
 * code has been absent for two minutes while the aircraft is still being observed; with no fresh
 * data it is kept with {@code status: insufficient-data}.
 */
@Component
public class EmergencySquawkGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-emergency-squawk";

    /** The record kind. */
    public static final String KIND = "emergency-squawk";

    /** The two observations must be this far apart. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(10);

    /** How long after the code is cleared the record is tombstoned, with fresh observations. */
    public static final Duration CLEARED_AFTER = Duration.ofMinutes(2);

    /** How much the position must move before the geometry is updated. */
    public static final double MOVE_KM = 10;

    /** The known squawk meanings. */
    static final Map<String, String> MEANINGS = Map.of(
            "7500", "unlawful interference",
            "7600", "radio failure",
            "7700", "emergency");

    private final SourceDescriptor descriptor = layer(ID, "Emergency squawks (derived)",
            "Aircraft transmitting an emergency squawk or reporting an emergency. Unverified; automated,"
                    + " not a warning.",
            "derived (input licences, e.g. ODbL for adsb.lol)", KIND).build();

    private final Map<String, Active> active = new LinkedHashMap<>();

    /** Creates the generator. */
    public EmergencySquawkGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** One aircraft's active emergency. */
    static final class Active {

        String code;
        String meaning;
        final List<Instant> observations = new ArrayList<>();
        final Set<String> sources = new java.util.HashSet<>();
        Geometry position;
        Instant lastQualifying;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return DerivedSources.AIRCRAFT_PREFIXES;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.AIRCRAFT);
    }

    @Override
    protected void clearState() {
        active.clear();
    }

    @Override
    public int stateSize() {
        return active.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.AIRCRAFT), now.minus(Duration.ofMinutes(10)), cap);
        Map<String, List<Observation>> byHex = new LinkedHashMap<>();
        for (Observation observation : observations) {
            byHex.computeIfAbsent(observation.externalId(), k -> new ArrayList<>()).add(observation);
        }
        for (Map.Entry<String, List<Observation>> entry : byHex.entrySet()) {
            process(ctx, now, entry.getKey(), entry.getValue());
        }
        // An aircraft with no fresh observations is not cleared: keep its record (unknown is not
        // clear). Forget it only after a long absence, and only to bound memory.
        Set<String> present = byHex.keySet();
        for (Map.Entry<String, Active> entry : new ArrayList<>(active.entrySet())) {
            Active state = entry.getValue();
            if (!present.contains(entry.getKey()) && state.lastQualifying != null
                    && Duration.between(state.lastQualifying, now).compareTo(Duration.ofHours(24)) > 0) {
                active.remove(entry.getKey());
            }
        }
    }

    private void process(DerivedContext ctx, Instant now, String hex, List<Observation> group) {
        String code = null;
        String meaning = null;
        Geometry position = null;
        for (Observation observation : group) {
            String squawk = observation.text("squawk");
            String emergency = observation.text("emergency");
            if (squawk != null && MEANINGS.containsKey(squawk)) {
                code = squawk;
                meaning = MEANINGS.get(squawk);
            } else if (emergency != null && !"none".equalsIgnoreCase(emergency)) {
                code = "7700";
                meaning = "emergency (" + emergency + ")";
            }
            if (observation.geometry() != null) {
                position = observation.geometry();
            }
        }
        Active state = active.get(hex);
        if (code == null) {
            if (state != null && state.lastQualifying != null
                    && Duration.between(state.lastQualifying, now).compareTo(CLEARED_AFTER) > 0) {
                active.remove(hex);
                ctx.tombstone(KIND, hex);
            }
            return;
        }
        if (state == null) {
            state = new Active();
            active.put(hex, state);
        }
        state.code = code;
        state.meaning = meaning;
        Instant at = newest(group);
        state.lastQualifying = at;
        if (at != null && (state.observations.isEmpty() || !state.observations.contains(at))) {
            state.observations.add(at);
        }
        for (Observation observation : group) {
            state.sources.add(observation.sourceId());
        }
        LayerStatus coverage = status(ctx, now.minus(CLEARED_AFTER));
        boolean confirmed = enoughEvidence(state);
        Geometry geometry = position != null ? position : state.position;
        if (coverage != LayerStatus.OK) {
            emit(ctx, now, hex, state, geometry, LayerStatus.INSUFFICIENT_DATA, false);
            return;
        }
        if (!confirmed) {
            emit(ctx, now, hex, state, geometry, LayerStatus.INSUFFICIENT_DATA, false);
            return;
        }
        if (state.position == null || position == null
                || Geo.haversineKm(state.position.getCoordinate().y, state.position.getCoordinate().x,
                        position.getCoordinate().y, position.getCoordinate().x) >= MOVE_KM) {
            state.position = position;
        }
        emit(ctx, now, hex, state, state.position, LayerStatus.OK, true);
    }

    static boolean enoughEvidence(Active state) {
        if (state.sources.size() >= 2) {
            return true;
        }
        if (state.observations.size() < 2) {
            return false;
        }
        Instant min = null;
        Instant max = null;
        for (Instant at : state.observations) {
            if (min == null || at.isBefore(min)) {
                min = at;
            }
            if (max == null || at.isAfter(max)) {
                max = at;
            }
        }
        return min != null && max != null && Duration.between(min, max).compareTo(MIN_INTERVAL) >= 0;
    }

    private void emit(DerivedContext ctx, Instant now, String hex, Active state, Geometry geometry, LayerStatus status,
            boolean confirmed) {
        Map<String, Object> payload = LayerPayload.base(ID, status, confirmed ? 0.8 : 0.3, List.of());
        payload.put("squawk", state.code);
        payload.put("meaning", state.meaning);
        payload.put("unverified", true);
        if (state.sources.size() > 1) {
            payload.put("sources", new ArrayList<>(state.sources));
        }
        NormalizedRecord.Builder builder = NormalizedRecord.builder(hex, KIND).observedAt(now);
        if (geometry != null) {
            builder.geometry(geometry);
        }
        ctx.emit(builder.putAll(payload).build());
    }

    private static Instant newest(List<Observation> group) {
        Instant newest = null;
        for (Observation observation : group) {
            if (observation.observedAt() != null && (newest == null || observation.observedAt().isAfter(newest))) {
                newest = observation.observedAt();
            }
        }
        return newest;
    }
}
