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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code derived-dark-vessel-match} (prompt 099): joins an {@code ais-gap} record with an unmatched
 * SAR vessel detection inside the vessel's reachable area (last fix plus max speed × time, capped at
 * 200 km) within the gap window ± 6 h. Labelled a candidate: SAR detections are not identifications.
 *
 * <p>{@code externalId} is {@code {mmsi}:{gapStartEpoch}:{detectionId}}. The SAR source
 * ({@code gfw-sar-detections}) is optional and was not built by prompt 088, so with no extraction the
 * generator is idle and records a health note rather than emitting anything.
 */
@Component
public class DarkVesselMatchGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-dark-vessel-match";

    /** The record kind. */
    public static final String KIND = "dark-vessel-candidate";

    /** The assumed maximum vessel speed, knots. */
    public static final double MAX_SPEED_KN = 25;

    /** The reachable cap, km. */
    public static final double REACH_KM = 200;

    /** The time window around the gap. */
    public static final Duration WINDOW = Duration.ofHours(6);

    /** How long a gap stays matchable. */
    public static final Duration LOOKBACK = Duration.ofDays(7);

    private final SourceDescriptor descriptor = layer(ID, "Dark-vessel candidates (derived)",
            "AIS gaps matched to unmatched SAR vessel detections. Candidates only; SAR detections are not"
                    + " identifications.",
            "derived (input licences)", KIND).build();

    /** Creates the generator. */
    public DarkVesselMatchGenerator(ObjectProvider<SourceRegistry> registry) {
        super(registry);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    protected Set<String> inputPrefixes() {
        return Set.of();
    }

    @Override
    protected Set<String> explicitInputSources() {
        return Set.of(DerivedSources.DERIVED_AIS_GAP, DerivedSources.SAR_DETECTIONS);
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of("ais-gap", "sar-vessel-detection");
    }

    @Override
    protected Duration tick() {
        return Duration.ofMinutes(10);
    }

    @Override
    protected void clearState() {
        // Stateless.
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> detections =
                ctx.observations().recent(Set.of(DerivedSources.SAR_DETECTIONS), Set.of("sar-vessel-detection"),
                        now.minus(LOOKBACK), cap);
        if (detections.isEmpty()) {
            ctx.state().degrade("gfw-sar-detections provides no input; the dark-vessel match is idle");
            return;
        }
        ctx.state().clearDegraded();
        List<Observation> gaps =
                ctx.observations().recent(Set.of(DerivedSources.DERIVED_AIS_GAP), Set.of("ais-gap"), now.minus(LOOKBACK), cap);
        LayerStatus coverage = status(ctx, now.minus(LOOKBACK));
        for (Observation gap : gaps) {
            Double gLon = gap.longitude();
            Double gLat = gap.latitude();
            String gapStartText = gap.text("gapStart");
            Instant gapStart = parse(gapStartText);
            if (gLon == null || gLat == null || gapStart == null) {
                continue;
            }
            double reachable = reachableKm(gapStart, now);
            String mmsi = mmsi(gap.externalId());
            for (Observation detection : detections) {
                Double dLon = detection.longitude();
                Double dLat = detection.latitude();
                if (dLon == null || dLat == null || detection.observedAt() == null) {
                    continue;
                }
                double distance = Geo.haversineKm(gLat, gLon, dLat, dLon);
                if (distance > reachable) {
                    continue;
                }
                double offsetHours = Duration.between(gapStart, detection.observedAt()).toMinutes() / 60.0;
                if (Math.abs(offsetHours) > WINDOW.toHours()) {
                    continue;
                }
                emit(ctx, now, coverage, mmsi, gap, detection, distance, offsetHours);
            }
        }
    }

    private void emit(DerivedContext ctx, Instant now, LayerStatus coverage, String mmsi, Observation gap,
            Observation detection, double distance, double offsetHours) {
        String id = mmsi + ":" + gap.text("gapStart") + ":" + detection.externalId();
        double score = Math.max(0, 1 - distance / REACH_KM) * Math.max(0, 1 - Math.abs(offsetHours) / WINDOW.toHours());
        Map<String, Object> payload = LayerPayload.base(ID, coverage, score, List.of());
        payload.put("distanceKm", LayerPayload.km05(distance));
        payload.put("timeOffsetH", LayerPayload.ratio1(offsetHours));
        payload.put("matchScore", LayerPayload.band01(score));
        payload.put("candidate", true);
        payload.put("note", "candidate; SAR detections are not identifications");
        ctx.emit(NormalizedRecord.builder(id, KIND).observedAt(now).putAll(payload).build());
    }

    /** The reachable radius (km) from a gap start to now, capped at {@link #REACH_KM}. */
    static double reachableKm(Instant gapStart, Instant now) {
        long hours = Math.max(1, Duration.between(gapStart, now).toHours());
        return Math.min(REACH_KM, MAX_SPEED_KN * 1.852 * hours);
    }

    private static String mmsi(String externalId) {        int colon = externalId.indexOf(':');
        return colon > 0 ? externalId.substring(0, colon) : externalId;
    }

    private static Instant parse(String text) {
        try {
            return text == null ? null : Instant.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
