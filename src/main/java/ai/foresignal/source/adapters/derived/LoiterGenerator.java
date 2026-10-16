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
 * {@code derived-loiter} (prompt 099): a vessel that stays within 0.5 nm of its own centroid for at
 * least 30 minutes over four or more fixes, outside port buffers and not moored. It ends when a fix
 * lies more than 0.5 nm from the centroid. {@code externalId} is the vessel's key.
 */
@Component
public class LoiterGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-loiter";

    /** The record kind. */
    public static final String KIND = "loiter";

    /** The loiter radius, km (0.5 nautical miles). */
    public static final double RADIUS_KM = 0.926;

    /** The shortest span. */
    public static final Duration SPAN = Duration.ofMinutes(30);

    /** The fewest fixes. */
    public static final int MIN_FIXES = 4;

    private final SourceDescriptor descriptor = layer(ID, "Vessel loitering (derived)",
            "A vessel holding station within 0.5 nautical miles for at least 30 minutes, outside ports."
                    + " Automated, not an accusation.",
            "derived (input licences; ODbL for keyless AIS)", KIND).build();

    private final ReferenceData reference;
    private final Map<String, Vessel> vessels = new LinkedHashMap<>();

    /** Creates the generator. */
    public LoiterGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** A vessel's recent fixes. */
    static final class Vessel {

        final Deque<Fix> fixes = new ArrayDeque<>();
        boolean reported;
    }

    /** One fix. */
    record Fix(Instant at, double lon, double lat) {}

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
        vessels.clear();
        reference.refresh();
    }

    /** A live loiter record re-enters the state as reported, so the next tick confirms or ends it. */
    @Override
    protected void adopt(ai.foresignal.derive.HeadSnapshot head, DerivedContext ctx) {
        if (KIND.equals(head.kind())) {
            vessels.computeIfAbsent(head.externalId(), k -> new Vessel()).reported = true;
        }
    }

    @Override
    public int stateSize() {
        int size = 0;
        for (Vessel vessel : vessels.values()) {
            size += vessel.fixes.size();
        }
        return size;
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(SPAN.plus(SPAN)), cap);
        List<ReferenceData.Port> ports = reference.ports();
        Set<String> present = new java.util.HashSet<>();
        for (Observation observation : observations) {
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            Instant at = observation.observedAt();
            if (lon == null || lat == null || at == null) {
                continue;
            }
            String key = observation.externalId();
            present.add(key);
            Vessel vessel = vessels.computeIfAbsent(key, k -> new Vessel());
            if (vessel.fixes.isEmpty() || at.isAfter(vessel.fixes.peekLast().at())) {
                vessel.fixes.addLast(new Fix(at, lon, lat));
            }
            // Keep the shortest tail that still spans SPAN: drop the oldest fix only while the next one is
            // itself at least SPAN old, so "spanning ≥ 30 min" can hold for fixes at any spacing.
            while (vessel.fixes.size() > 1 && Duration.between(second(vessel.fixes), at).compareTo(SPAN) >= 0) {
                vessel.fixes.removeFirst();
            }
            evaluate(ctx, now, ports, key, vessel, observation);
        }
        for (String key : new ArrayList<>(vessels.keySet())) {
            if (!present.contains(key)) {
                // A reported loiter whose vessel is no longer seen cannot be supported any more: end it
                // rather than leave the record behind with no state that could ever end it.
                Vessel gone = vessels.remove(key);
                if (gone.reported) {
                    ctx.tombstone(KIND, key);
                }
            }
        }
    }

    private void evaluate(DerivedContext ctx, Instant now, List<ReferenceData.Port> ports, String key, Vessel vessel,
            Observation observation) {
        if (vessel.fixes.size() < MIN_FIXES) {
            return;
        }
        Fix first = vessel.fixes.peekFirst();
        Fix last = vessel.fixes.peekLast();
        if (Duration.between(first.at(), last.at()).compareTo(SPAN) < 0) {
            return;
        }
        double lon = 0;
        double lat = 0;
        for (Fix fix : vessel.fixes) {
            lon += fix.lon();
            lat += fix.lat();
        }
        lon /= vessel.fixes.size();
        lat /= vessel.fixes.size();
        if (!withinRadius(vessel.fixes, lon, lat, RADIUS_KM)) {
            if (vessel.reported) {
                vessels.remove(key);
                ctx.tombstone(KIND, key);
            }
            return;
        }
        if (LayerSupport.mooredOrAnchored(observation)) {
            return;
        }
        for (ReferenceData.Port port : ports) {
            if (Geo.haversineKm(lat, lon, port.lat(), port.lon()) <= port.radiusKm()) {
                return;
            }
        }
        LayerStatus coverage = status(ctx, now.minus(SPAN));
        if (coverage != LayerStatus.OK) {
            return;
        }
        vessel.reported = true;
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.6, List.of());
        payload.put("fixes", vessel.fixes.size());
        payload.put("spanMinutes", LayerPayload.minutes5(Duration.between(first.at(), last.at()).toMinutes()));
        payload.put("radiusKm", LayerPayload.km05(maxRadius(vessel, lon, lat)));
        payload.put("centroid", List.of(LayerPayload.ratio1(lon), LayerPayload.ratio1(lat)));
        ctx.emit(NormalizedRecord.builder(key, KIND).observedAt(last.at())
                .geometry(Geo.factory().createPoint(new Coordinate(lon, lat))).putAll(payload).build());
    }

    private static double maxRadius(Vessel vessel, double lon, double lat) {
        double max = 0;
        for (Fix fix : vessel.fixes) {
            max = Math.max(max, Geo.haversineKm(lat, lon, fix.lat(), fix.lon()));
        }
        return max;
    }

    /** The time of the second-oldest fix (the deque holds at least two). */
    private static Instant second(Deque<Fix> fixes) {
        java.util.Iterator<Fix> it = fixes.iterator();
        it.next();
        return it.next().at();
    }

    /** True when every fix is within {@code radiusKm} of the centroid's position. */
    static boolean withinRadius(java.util.Collection<Fix> fixes, double lon, double lat, double radiusKm) {
        for (Fix fix : fixes) {
            if (Geo.haversineKm(lat, lon, fix.lat(), fix.lon()) > radiusKm) {
                return false;
            }
        }
        return true;
    }
}
