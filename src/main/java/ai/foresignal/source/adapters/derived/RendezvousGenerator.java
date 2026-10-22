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
 * {@code derived-rendezvous} (prompt 099): two vessels within 0.5 nm, both below 3 knots, for at
 * least 15 minutes, at least 5 nm from any World Port Index port, excluding tugs, pilots, SAR craft
 * and port tenders (AIS ship types 31, 32, 50–53, 55). Neighbors are found through about-1-nm grid
 * buckets, so the search never runs over the whole fleet.
 *
 * <p>{@code externalId} is {@code {a}:{b}} with the two vessel keys sorted. The record is tombstoned
 * once the pair separates beyond 1 nm for 10 minutes.
 */
@Component
public class RendezvousGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-rendezvous";

    /** The record kind. */
    public static final String KIND = "rendezvous";

    /** The meeting radius, km (0.5 nm). */
    public static final double MEET_KM = 0.926;

    /** The separation radius, km (1 nm). */
    public static final double SEPARATE_KM = 1.852;

    /** The port exclusion radius, km (5 nm). */
    public static final double PORT_EXCLUSION_KM = 9.26;

    /** The shortest meeting. */
    public static final Duration MIN_DURATION = Duration.ofMinutes(15);

    /** How long a separated pair is kept before tombstoning. */
    public static final Duration SEPARATION = Duration.ofMinutes(10);

    /** The grid bucket size, km (about 1 nm). */
    public static final double CELL_KM = 1.852;

    /** AIS ship types never considered (tug, pilot, SAR, port tender). */
    static final Set<Integer> EXCLUDED = Set.of(31, 32, 50, 51, 52, 53, 55);

    private final SourceDescriptor descriptor = layer(ID, "Vessel rendezvous (derived)",
            "Two vessels within 0.5 nautical miles, both slow, away from any port, for 15 minutes."
                    + " Automated, not an accusation.",
            "derived (input licences; ODbL for keyless AIS)", KIND).build();

    private final ReferenceData reference;
    private final Map<String, Pair> pairs = new LinkedHashMap<>();

    /** Creates the generator. */
    public RendezvousGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** A pair's meeting interval. */
    static final class Pair {

        Instant firstSeen;
        Instant lastMet;
        Instant separatedAt;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return DerivedSources.VESSEL_PREFIXES;
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.VESSEL);
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        pairs.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return pairs.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(Duration.ofMinutes(30)), cap);
        List<ReferenceData.Port> ports = reference.ports();
        List<Observation> eligible = new ArrayList<>();
        for (Observation observation : observations) {
            Double sog = LayerSupport.sog(observation);
            Integer type = LayerSupport.shipType(observation);
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (sog == null || sog >= 3 || lon == null || lat == null) {
                continue;
            }
            if (type != null && EXCLUDED.contains(type)) {
                continue;
            }
            if (nearPort(ports, lon, lat)) {
                continue;
            }
            eligible.add(observation);
        }
        LayerStatus coverage = status(ctx, now.minus(Duration.ofMinutes(30)));
        Map<String, List<Observation>> grid = bucket(eligible);
        Set<String> met = new java.util.HashSet<>();
        for (Observation a : eligible) {
            for (Observation b : neighbors(grid, a)) {
                if (a.externalId().compareTo(b.externalId()) >= 0) {
                    continue;
                }
                Double aLon = a.longitude();
                Double aLat = a.latitude();
                Double bLon = b.longitude();
                Double bLat = b.latitude();
                double distance = Geo.haversineKm(aLat, aLon, bLat, bLon);
                String key = a.externalId() + ":" + b.externalId();
                if (distance <= MEET_KM) {
                    met.add(key);
                    record(ctx, now, coverage, key, a, b, distance);
                }
            }
        }
        for (Map.Entry<String, Pair> entry : new ArrayList<>(pairs.entrySet())) {
            Pair pair = entry.getValue();
            if (met.contains(entry.getKey())) {
                pair.separatedAt = null;
                continue;
            }
            if (pair.separatedAt == null) {
                pair.separatedAt = now;
            } else if (Duration.between(pair.separatedAt, now).compareTo(SEPARATION) >= 0) {
                pairs.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
            }
        }
    }

    private void record(DerivedContext ctx, Instant now, LayerStatus coverage, String key, Observation a, Observation b,
            double distance) {
        Pair pair = pairs.computeIfAbsent(key, k -> {
            Pair p = new Pair();
            p.firstSeen = now;
            return p;
        });
        if (Duration.between(pair.firstSeen, now).compareTo(MIN_DURATION) < 0) {
            return;
        }
        if (coverage != LayerStatus.OK) {
            return;
        }
        pair.lastMet = now;
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.6, List.of());
        payload.put("vessels", List.of(a.externalId(), b.externalId()));
        payload.put("distanceNm", LayerPayload.ratio1(distance / 1.852));
        payload.put("durationMin", LayerPayload.minutes5(Duration.between(pair.firstSeen, now).toMinutes()));
        double lon = (a.longitude() + b.longitude()) / 2;
        double lat = (a.latitude() + b.latitude()) / 2;
        ctx.emit(NormalizedRecord.builder(key, KIND).observedAt(now).point(lon, lat).putAll(payload).build());
    }

    private static boolean nearPort(List<ReferenceData.Port> ports, double lon, double lat) {
        for (ReferenceData.Port port : ports) {
            if (Geo.haversineKm(lat, lon, port.lat(), port.lon()) <= PORT_EXCLUSION_KM) {
                return true;
            }
        }
        return false;
    }

    /** Buckets observations into about-1-nm cells. */
    static Map<String, List<Observation>> bucket(List<Observation> observations) {
        Map<String, List<Observation>> grid = new LinkedHashMap<>();
        for (Observation observation : observations) {
            grid.computeIfAbsent(cell(observation.longitude(), observation.latitude()), k -> new ArrayList<>()).add(observation);
        }
        return grid;
    }

    /** The observations in the eight cells around {@code a}. */
    static List<Observation> neighbors(Map<String, List<Observation>> grid, Observation a) {
        List<Observation> out = new ArrayList<>();
        int cx = (int) Math.floor(a.longitude() / CELL_KM);
        int cy = (int) Math.floor(a.latitude() / CELL_KM);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                out.addAll(grid.getOrDefault((cx + dx) + ":" + (cy + dy), List.of()));
            }
        }
        return out;
    }

    private static String cell(double lon, double lat) {
        return (int) Math.floor(lon / CELL_KM) + ":" + (int) Math.floor(lat / CELL_KM);
    }
}
