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
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-cable-risk} (prompt 099): a seabed-infrastructure guard. A vessel that is not a
 * cable ship, at anchor or under sustained slow speed (0.3–3 knots), within 2 km of a submarine
 * cable and outside every port or anchorage buffer for at least 30 minutes is a proximity risk;
 * moving 0.5–3 knots while anchored within 1 km for 15 minutes is flagged as a possible anchor drag.
 * Severity rises with duration, the number of cables within 2 km and the drag flag.
 *
 * <p>{@code externalId} is {@code {mmsi}:{cableId}:{startEpoch}}.
 */
@Component
public class CableRiskGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-cable-risk";

    /** The record kind. */
    public static final String KIND = "cable-proximity-risk";

    /** The cable proximity, km. */
    public static final double PROXIMITY_KM = 2;

    /** The drag proximity, km. */
    public static final double DRAG_KM = 1;

    /** The port exclusion around a World Port Index point, km. */
    public static final double PORT_KM = 5;

    /** The shortest slow stay. */
    public static final Duration MIN_STAY = Duration.ofMinutes(30);

    /** The shortest drag. */
    public static final Duration DRAG_DURATION = Duration.ofMinutes(15);

    /** Service vessel types that are not cable-risk candidates. */
    static final Set<Integer> EXCLUDED = Set.of(31, 32, 52, 53, 55);

    private final SourceDescriptor descriptor = layer(ID, "Submarine cable risk (derived)",
            "Slow or anchored vessels near submarine cables outside ports, with a possible anchor-drag"
                    + " flag. Automated, not an accusation.",
            "derived (input licences; see inputs)", KIND).build();

    private final ReferenceData reference;
    private final Map<String, Risk> risks = new LinkedHashMap<>();

    /** Creates the generator. */
    public CableRiskGenerator(ObjectProvider<SourceRegistry> registry, ReferenceData reference) {
        super(registry);
        this.reference = reference;
    }

    /** A vessel's risk state against one cable. */
    static final class Risk {

        String externalId;
        String cableId;
        Instant start;
        Instant lastSeen;
        Instant dragStart;
        boolean drag;
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
    protected Set<String> explicitInputSources() {
        return Set.of(DerivedSources.SUBMARINE_CABLES);
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of(DerivedSources.VESSEL, "cable");
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(5);
    }

    @Override
    protected void clearState() {
        risks.clear();
        reference.refresh();
    }

    @Override
    public int stateSize() {
        return risks.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(Duration.ofHours(2)), cap);
        List<ReferenceData.Cable> cables = reference.cables();
        List<ReferenceData.Port> ports = reference.ports();
        LayerStatus coverage = status(ctx, now.minus(MIN_STAY));
        Set<String> active = new java.util.HashSet<>();
        for (Observation observation : observations) {
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (lon == null || lat == null) {
                continue;
            }
            Double sog = LayerSupport.sog(observation);
            boolean anchored = LayerSupport.mooredOrAnchored(observation) && isAnchored(observation);
            boolean slow = sog != null && sog >= 0.3 && sog <= 3;
            if (!anchored && !slow) {
                continue;
            }
            Integer type = LayerSupport.shipType(observation);
            if (type != null && EXCLUDED.contains(type)) {
                continue;
            }
            if (withinPort(ports, lon, lat)) {
                continue;
            }
            int nearby = 0;
            for (ReferenceData.Cable cable : cables) {
                double km = pointDistanceKm(cable, lon, lat);
                if (km > PROXIMITY_KM) {
                    continue;
                }
                nearby++;
                String key = observation.externalId() + ":" + cable.id();
                active.add(key);
                if (risks.size() >= cap && !risks.containsKey(key)) {
                    continue;
                }
                Risk risk = risks.computeIfAbsent(key, k -> {
                    Risk fresh = new Risk();
                    fresh.cableId = cable.id();
                    fresh.start = observation.observedAt();
                    fresh.externalId = observation.externalId() + ":" + cable.id() + ":"
                            + (observation.observedAt() == null ? now.getEpochSecond() : observation.observedAt().getEpochSecond());
                    return fresh;
                });
                risk.lastSeen = observation.observedAt();
                if (anchored && sog != null && sog >= 0.5 && sog <= 3 && km <= DRAG_KM) {
                    if (risk.dragStart == null) {
                        risk.dragStart = observation.observedAt();
                    } else if (Duration.between(risk.dragStart, observation.observedAt()).compareTo(DRAG_DURATION) >= 0) {
                        risk.drag = true;
                    }
                } else {
                    risk.dragStart = null;
                }
                if (risk.start != null && observation.observedAt() != null
                        && Duration.between(risk.start, observation.observedAt()).compareTo(MIN_STAY) >= 0) {
                    emit(ctx, now, coverage, observation, risk, nearby);
                }
            }
        }
        for (Map.Entry<String, Risk> entry : new ArrayList<>(risks.entrySet())) {
            Risk risk = entry.getValue();
            if (!active.contains(entry.getKey()) && risk.lastSeen != null
                    && Duration.between(risk.lastSeen, now).compareTo(Duration.ofHours(6)) > 0) {
                risks.remove(entry.getKey());
                ctx.tombstone(KIND, risk.externalId);
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, LayerStatus coverage, Observation observation, Risk risk,
            int cablesNearby) {
        double hours = risk.start == null ? 0 : Duration.between(risk.start, now).toMinutes() / 60.0;
        String severity = risk.drag || (hours >= 6 && cablesNearby >= 2) ? "high" : hours >= 2 ? "medium" : "low";
        Map<String, Object> payload = LayerPayload.base(ID, coverage, 0.6, List.of());
        payload.put("cableId", risk.cableId);
        payload.put("durationH", LayerPayload.ratio1(hours));
        payload.put("cablesNearby", cablesNearby);
        payload.put("possibleAnchorDrag", risk.drag);
        payload.put("severity", severity);
        ctx.emit(NormalizedRecord.builder(risk.externalId, KIND).observedAt(now)
                .point(observation.longitude(), observation.latitude()).putAll(payload).build());
    }

    private static boolean isAnchored(Observation observation) {
        Integer status = LayerSupport.intField(observation, "navStatus");
        return status != null && status == 1;
    }

    private static boolean withinPort(List<ReferenceData.Port> ports, double lon, double lat) {
        for (ReferenceData.Port port : ports) {
            if (Geo.haversineKm(lat, lon, port.lat(), port.lon()) <= port.radiusKm() + PORT_KM) {
                return true;
            }
        }
        return false;
    }

    /** The approximate distance in km from a point to a cable route. */
    static double pointDistanceKm(ReferenceData.Cable cable, double lon, double lat) {
        Point point = Geo.factory().createPoint(new org.locationtech.jts.geom.Coordinate(lon, lat));
        return cable.route().distance(point) * 111.0;
    }
}
