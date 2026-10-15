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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-deception} (prompt 099): position-integrity checks that never accuse. It records
 * which check matched, not intent: a duplicate MMSI at two positions more than 50 km apart within
 * 120 seconds (across any sources), a vessel implied speed over 80 knots between fixes at least 10
 * seconds apart (skipping the 102.3-knot sentinel, high-speed craft 40–49 and WIG 20–29), and an
 * aircraft implied ground speed over 1,400 knots between ADS-B fixes (never MLAT or TIS-B).
 *
 * <p>{@code externalId} is the entity; the payload lists {@code checks[]} with counts, and the
 * record is tombstoned after an hour without recurrence.
 */
@Component
public class DeceptionGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-deception";

    /** The record kind. */
    public static final String KIND = "deception";

    /** The duplicate-MMSI separation. */
    public static final double DUPLICATE_KM = 50;

    /** The duplicate-MMSI window. */
    public static final Duration DUPLICATE_WINDOW = Duration.ofSeconds(120);

    /** The shortest interval between two fixes for an implied-speed check. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(10);

    /** The vessel implied-speed threshold. */
    public static final double VESSEL_KN = 80;

    /** The aircraft implied-speed threshold. */
    public static final double AIRCRAFT_KN = 1400;

    /** How long a check lives without recurrence. */
    public static final Duration STALE = Duration.ofHours(1);

    private final SourceDescriptor descriptor = layer(ID, "Position-integrity checks (derived)",
            "Automated position-integrity checks (duplicate MMSI, impossible speed). States the check that"
                    + " matched, never intent.",
            "derived (input licences)", KIND).build();

    private final Map<String, Observed> last = new LinkedHashMap<>();
    private final Map<String, Active> active = new LinkedHashMap<>();

    /** Creates the generator. */
    public DeceptionGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    /** The last fix seen for an entity of a source. */
    static final class Observed {

        Instant at;
        double lon;
        double lat;
    }

    /** One entity's active checks. */
    static final class Active {

        final Map<String, Integer> checks = new LinkedHashMap<>();
        Instant lastSeen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        Set<String> all = new java.util.TreeSet<>(DerivedSources.AIRCRAFT_PREFIXES);
        all.addAll(DerivedSources.VESSEL_PREFIXES);
        return all;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.AIRCRAFT, DerivedSources.VESSEL);
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        last.clear();
        active.clear();
    }

    @Override
    public int stateSize() {
        return last.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        LayerStatus coverage = status(ctx, now.minus(Duration.ofMinutes(10)));
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), inputKinds(), now.minus(Duration.ofMinutes(30)), cap);
        Map<String, List<Observation>> vesselsByMmsi = new LinkedHashMap<>();
        for (Observation observation : observations) {
            if (DerivedSources.VESSEL.equals(observation.kind())) {
                vesselsByMmsi.computeIfAbsent(observation.externalId(), k -> new ArrayList<>()).add(observation);
            }
            impliedSpeed(observation);
        }
        if (coverage == LayerStatus.OK) {
            duplicateMmsi(vesselsByMmsi);
        }
        for (Map.Entry<String, Active> entry : new ArrayList<>(active.entrySet())) {
            Active state = entry.getValue();
            if (state.checks.isEmpty()) {
                continue;
            }
            if (state.lastSeen != null && Duration.between(state.lastSeen, now).compareTo(STALE) > 0) {
                active.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
                continue;
            }
            emit(ctx, now, entry.getKey(), state);
        }
    }

    private void impliedSpeed(Observation observation) {
        Double lon = observation.longitude();
        Double lat = observation.latitude();
        Instant at = observation.observedAt();
        if (lon == null || lat == null || at == null) {
            return;
        }
        String key = observation.sourceId() + "|" + observation.externalId();
        Observed previous = last.get(key);
        last.put(key, fix(at, lon, lat));
        if (previous == null || Duration.between(previous.at, at).compareTo(MIN_INTERVAL) < 0) {
            return;
        }
        double knots = impliedKnots(previous.lat, previous.lon, lat, lon, Duration.between(previous.at, at).toSeconds());
        boolean aircraft = DerivedSources.AIRCRAFT.equals(observation.kind());
        if (aircraft) {
            if (knots > AIRCRAFT_KN && LayerSupport.adsbPosition(observation)) {
                mark(observation.externalId(), "implied-speed", at);
            }
            return;
        }
        Double sog = LayerSupport.sog(observation);
        Integer type = LayerSupport.shipType(observation);
        boolean sentinel = sog != null && sog == 102.3;
        boolean fast = type != null && ((type >= 40 && type <= 49) || (type >= 20 && type <= 29));
        if (knots > VESSEL_KN && !sentinel && !fast) {
            mark(observation.externalId(), "implied-speed", at);
        }
    }

    private void duplicateMmsi(Map<String, List<Observation>> vesselsByMmsi) {
        for (Map.Entry<String, List<Observation>> entry : vesselsByMmsi.entrySet()) {
            List<Observation> group = entry.getValue();
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    Observation a = group.get(i);
                    Observation b = group.get(j);
                    if (a.longitude() == null || b.longitude() == null || a.observedAt() == null
                            || b.observedAt() == null) {
                        continue;
                    }
                    if (Duration.between(a.observedAt(), b.observedAt()).abs().compareTo(DUPLICATE_WINDOW) > 0) {
                        continue;
                    }
                    if (Geo.haversineKm(a.latitude(), a.longitude(), b.latitude(), b.longitude()) > DUPLICATE_KM) {
                        mark(entry.getKey(), "duplicate-mmsi", a.observedAt());
                    }
                }
            }
        }
    }

    private void mark(String entity, String check, Instant at) {
        Active state = active.computeIfAbsent(entity, k -> new Active());
        state.checks.merge(check, 1, Integer::sum);
        state.lastSeen = at;
    }

    private void emit(DerivedContext ctx, Instant now, String entity, Active state) {
        List<Map<String, Object>> checks = new ArrayList<>();
        for (Map.Entry<String, Integer> check : state.checks.entrySet()) {
            checks.add(Map.of("check", check.getKey(), "count", check.getValue()));
        }
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.6, List.of());
        payload.put("checks", checks);
        ctx.emit(NormalizedRecord.builder(entity, KIND).observedAt(now).putAll(payload).build());
    }

    /** The implied speed in knots between two positions over {@code seconds}. */
    static double impliedKnots(double lat1, double lon1, double lat2, double lon2, long seconds) {
        if (seconds <= 0) {
            return 0;
        }
        double hours = seconds / 3600.0;
        return Geo.haversineKm(lat1, lon1, lat2, lon2) / 1.852 / hours;
    }

    private static Observed fix(Instant at, double lon, double lat) {        Observed observed = new Observed();
        observed.at = at;
        observed.lon = lon;
        observed.lat = lat;
        return observed;
    }
}
