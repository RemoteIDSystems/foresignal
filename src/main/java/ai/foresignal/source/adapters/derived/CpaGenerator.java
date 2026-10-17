package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.kinematics.CpaCalculator;
import ai.foresignal.kinematics.GeoMath;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-cpa} (prompt 099): closest point of approach for vessel pairs in an area of
 * interest, both above 3 knots. Positions, SOG and COG are turned into a local equirectangular
 * frame; a pair is flagged when its CPA is under 0.5 nm with a time to CPA of at most 20 minutes.
 * {@code externalId} is {@code {a}:{b}}; the record is tombstoned once the CPA time has passed or
 * the pair diverges.
 */
@Component
public class CpaGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-cpa";

    /** The record kind. */
    public static final String KIND = "cpa";

    /** The closest approach that matters, nm. */
    public static final double CPA_NM = 0.5;

    /** The furthest time to CPA, hours. */
    public static final double TCPA_HOURS = 1.0 / 3.0;

    /** The lowest speed a pair needs, knots. */
    public static final double MIN_SPEED_KN = 3;

    /** The area parameter. */
    public static final String AOI_PARAM = "aoi";

    private final SourceDescriptor descriptor = layer(ID, "Vessel close-quarters (derived)",
            "Vessel pairs whose closest point of approach is under 0.5 nautical miles within 20 minutes,"
                    + " inside an area. Automated, not a warning.",
            "derived (input licences; ODbL for keyless AIS)", KIND)
            .param(ParamSpec.required(AOI_PARAM, ParamType.BBOX, "The area of interest as minLon,minLat,maxLon,maxLat"))
            .build();

    private final Map<String, Instant> lastRisk = new LinkedHashMap<>();

    /** Creates the generator. */
    public CpaGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
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
        return Duration.ofMinutes(1);
    }

    @Override
    protected void clearState() {
        lastRisk.clear();
    }

    @Override
    public int stateSize() {
        return lastRisk.size();
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        Envelope aoi = ctx.params().bbox(AOI_PARAM);
        int cap = maxEntities(ctx.params());
        List<Observation> observations =
                ctx.observations().recent(inputSources(), Set.of(DerivedSources.VESSEL), now.minus(Duration.ofMinutes(10)), cap);
        List<Observation> moving = new ArrayList<>();
        for (Observation observation : observations) {
            Double sog = LayerSupport.sog(observation);
            Double lon = observation.longitude();
            Double lat = observation.latitude();
            if (sog == null || sog <= MIN_SPEED_KN || lon == null || lat == null) {
                continue;
            }
            if (aoi != null && !aoi.isNull() && !aoi.contains(lon, lat)) {
                continue;
            }
            moving.add(observation);
        }
        LayerStatus coverage = status(ctx, now.minus(Duration.ofMinutes(10)));
        Set<String> risky = new java.util.HashSet<>();
        for (int i = 0; i < moving.size(); i++) {
            for (int j = i + 1; j < moving.size(); j++) {
                Observation a = moving.get(i);
                Observation b = moving.get(j);
                double[] result = approach(a, b);
                if (result == null) {
                    continue;
                }
                double cpaKm = result[0];
                double tcpaHours = result[1];
                boolean flag = cpaKm <= CPA_NM * 1.852 && tcpaHours > 0 && tcpaHours <= TCPA_HOURS;
                String key = pairKey(a.externalId(), b.externalId());
                if (!flag) {
                    continue;
                }
                risky.add(key);
                if (coverage != LayerStatus.OK) {
                    continue;
                }
                lastRisk.put(key, now);
                emit(ctx, now, key, a, b, cpaKm, tcpaHours);
            }
        }
        for (Map.Entry<String, Instant> entry : new ArrayList<>(lastRisk.entrySet())) {
            if (!risky.contains(entry.getKey()) && Duration.between(entry.getValue(), now).compareTo(Duration.ofMinutes(30)) > 0) {
                lastRisk.remove(entry.getKey());
                ctx.tombstone(KIND, entry.getKey());
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, String key, Observation a, Observation b, double cpaKm,
            double tcpaHours) {
        Map<String, Object> payload = LayerPayload.base(ID, LayerStatus.OK, 0.7, List.of());
        payload.put("vessels", List.of(a.externalId(), b.externalId()));
        payload.put("cpaNm", LayerPayload.ratio1(cpaKm / 1.852));
        payload.put("tcpaMin", LayerPayload.minutes5(tcpaHours * 60));
        double lon = (a.longitude() + b.longitude()) / 2;
        double lat = (a.latitude() + b.latitude()) / 2;
        ctx.emit(NormalizedRecord.builder(key, KIND).observedAt(now).point(lon, lat).putAll(payload).build());
    }

    /**
     * The CPA distance (km) and time to CPA (hours) of {@code b} relative to {@code a}, or null when
     * the closing speed is negligible. The maths is the shared {@link CpaCalculator#closest} (prompt 114);
     * only the vessel banding stays here. The time is unclamped: a negative TCPA means the pair diverges.
     */
    static double[] approach(Observation a, Observation b) {
        double[] d = GeoMath.offsetMeters(a.longitude(), a.latitude(), b.longitude(), b.latitude());
        double[] va = velocity(a);
        double[] vb = velocity(b);
        CpaCalculator.Raw raw = CpaCalculator.closest(d[0] / 1000.0, d[1] / 1000.0, vb[0] - va[0], vb[1] - va[1], 1e-6);
        return raw == null ? null : new double[] {raw.dcpa(), raw.tcpa()};
    }

    /** The vessel's velocity {@code {east, north}} in km/h from SOG (knots) and COG. */
    private static double[] velocity(Observation observation) {
        Double sog = LayerSupport.sog(observation);
        Double cog = LayerSupport.cog(observation);
        return GeoMath.velocity(sog == null ? 0 : sog * 1.852, cog == null ? 0 : cog);
    }

    static String pairKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + ":" + b : b + ":" + a;
    }
}
