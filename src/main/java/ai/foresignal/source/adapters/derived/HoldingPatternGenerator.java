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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-holding} (prompt 099): an airborne aircraft below 300 knots holding its altitude
 * within ±1,000 ft while turning through more than 300° within 10 minutes. Geometry is the convex
 * hull of the fixes; the payload carries the turn, the duration, the altitude and the nearest
 * OurAirports airport within 50 km. It ends five minutes after the last turn.
 *
 * <p>{@code externalId} is {@code {hex}:{startEpoch}}.
 */
@Component
public class HoldingPatternGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-holding";

    /** The record kind. */
    public static final String KIND = "holding-pattern";

    /** The window. */
    public static final Duration WINDOW = Duration.ofMinutes(10);

    /** The cumulative turn that marks a hold. */
    public static final double TURN_DEG = 300;

    /** The altitude band, feet. */
    public static final double ALTITUDE_FT = 1000;

    /** The speed ceiling, knots. */
    public static final double MAX_GS = 300;

    /** How long after the last turn the pattern ends. */
    public static final Duration END = Duration.ofMinutes(5);

    private final SourceDescriptor descriptor = layer(ID, "Aircraft holding patterns (derived)",
            "An aircraft circling below 300 knots within a 1,000 ft altitude band. Automated, not a warning.",
            "derived (input licences, e.g. ODbL for adsb.lol)", KIND).build();

    private final ReferenceData reference;
    private final Map<String, Deque<Point>> tracks = new LinkedHashMap<>();

    /** Creates the generator. */
    public HoldingPatternGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** One fix. */
    record Point(Instant at, double lon, double lat, double altitudeFt, double track) {}

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
        tracks.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return tracks.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.AIRCRAFT), now.minus(WINDOW.plus(WINDOW)), cap);
        LayerStatus coverage = status(ctx, now.minus(WINDOW));
        Set<String> present = new java.util.HashSet<>();
        for (Observation observation : observations) {
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            Double gs = observation.number("gs");
            Double altM = firstNumber(observation, "alt_geom", "alt_baro");
            Double track = observation.number("track");
            Instant at = observation.observedAt();
            if (lon == null || lat == null || gs == null || altM == null || track == null || at == null) {
                continue;
            }
            if (!LayerSupport.airborne(observation) || gs >= MAX_GS) {
                continue;
            }
            String key = observation.externalId();
            present.add(key);
            Deque<Point> trackPoints = tracks.computeIfAbsent(key, k -> new ArrayDeque<>());
            if (trackPoints.isEmpty() || at.isAfter(trackPoints.peekLast().at())) {
                trackPoints.addLast(new Point(at, lon, lat, altM * 3.28084, track));
            }
            while (trackPoints.size() > 1 && Duration.between(trackPoints.peekFirst().at(), at).compareTo(WINDOW) > 0) {
                trackPoints.removeFirst();
            }
            evaluate(ctx, now, coverage, key, trackPoints);
        }
        for (String key : new ArrayList<>(tracks.keySet())) {
            if (!present.contains(key)) {
                tracks.remove(key);
            }
        }
    }

    private void evaluate(DerivedContext ctx, Instant now, LayerStatus coverage, String key, Deque<Point> points) {
        if (points.size() < 4) {
            return;
        }
        List<Point> list = new ArrayList<>(points);
        Point first = list.get(0);
        Point last = list.get(list.size() - 1);
        double turn = 0;
        double lastTurnAt = 0;
        boolean turning = false;
        for (int i = 1; i < list.size(); i++) {
            double delta = wrap(list.get(i).track() - list.get(i - 1).track());
            turn += delta;
            if (Math.abs(delta) > 1) {
                lastTurnAt = i;
                turning = true;
            }
        }
        boolean altitudeBand = true;
        for (Point point : list) {
            if (Math.abs(point.altitudeFt() - first.altitudeFt()) > ALTITUDE_FT) {
                altitudeBand = false;
                break;
            }
        }
        boolean holds = Math.abs(turn) > TURN_DEG && altitudeBand && Duration.between(first.at(), last.at()).compareTo(WINDOW) >= 0;
        if (!holds || !turning || coverage != LayerStatus.OK) {
            if (points.size() >= 4 && Duration.between(points.peekLast().at(), now).compareTo(END) > 0) {
                tracks.remove(key);
                ctx.tombstone(KIND, key);
            }
            return;
        }
        org.locationtech.jts.geom.MultiPoint multi = Geo.factory()
                .createMultiPointFromCoords(list.stream().map(p -> new Coordinate(p.lon(), p.lat())).toArray(Coordinate[]::new));
        org.locationtech.jts.geom.Geometry hull = multi.convexHull();
        String id = key + ":" + first.at().getEpochSecond();
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.7, List.of());
        payload.put("turnsDeg", Math.round(Math.abs(turn)));
        payload.put("durationMin", LayerPayload.minutes5(Duration.between(first.at(), last.at()).toMinutes()));
        payload.put("altitudeFt", Math.round(first.altitudeFt() / 100.0) * 100);
        String nearest = nearestAirport(first.lon(), first.lat());
        if (nearest != null) {
            payload.put("nearestAirport", nearest);
        }
        ctx.emit(NormalizedRecord.builder(id, KIND).observedAt(last.at()).geometry(hull).putAll(payload).build());
    }

    private String nearestAirport(double lon, double lat) {
        String nearest = null;
        double best = 50;
        for (ReferenceData.Airport airport : reference.airports()) {
            double distance = Geo.haversineKm(lat, lon, airport.lat(), airport.lon());
            if (distance <= best) {
                best = distance;
                nearest = airport.ident();
            }
        }
        return nearest;
    }

    /** The cumulative signed track change across a sequence of fixes. */
    static double cumulativeTurn(List<Point> points) {
        double turn = 0;
        for (int i = 1; i < points.size(); i++) {
            turn += wrap(points.get(i).track() - points.get(i - 1).track());
        }
        return turn;
    }

    static double wrap(double degrees) {        double value = degrees % 360;
        if (value > 180) {
            value -= 360;
        }
        if (value < -180) {
            value += 360;
        }
        return value;
    }

    private static Double firstNumber(Observation observation, String... keys) {
        for (String key : keys) {
            Double value = observation.number(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
