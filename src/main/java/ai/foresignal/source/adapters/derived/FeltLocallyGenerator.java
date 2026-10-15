package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.derive.Observation;
import ai.foresignal.place.PlaceView;
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
 * {@code derived-felt-locally} (prompt 099): for each earthquake and each operator place within
 * 1,000 km, the estimated intensity at the place. When a USGS ShakeMap exists for the event the
 * nearest stored MMI contour is used ({@code method: shakemap}); otherwise the Atkinson &amp; Wald
 * (2007) intensity prediction equation is used ({@code method: ipe-aw2007}), with the California
 * coefficient set west of −105° and the central/eastern US set east of it. A record is emitted when
 * the intensity is at least 2.0; the payload carries {@code mmi} (0.5 bands), {@code distanceKm},
 * {@code feltLikely} (MMI ≥ 2.5) and {@code method}.
 *
 * <p>{@code externalId} is {@code {eventId}:{placeId}}.
 */
@Component
public class FeltLocallyGenerator extends LayerGenerator {

    /** The source id. */
    public static final String ID = "derived-felt-locally";

    /** The record kind. */
    public static final String KIND = "felt-report-estimate";

    /** The furthest place considered. */
    public static final double MAX_KM = 1000;

    /** The intensity below which nothing is emitted. */
    public static final double MIN_MMI = 2.0;

    /** The intensity at which shaking is likely felt. */
    public static final double FELT_MMI = 2.5;

    /** The longitude that splits the California and CEUS coefficient sets. */
    public static final double CALIFORNIA_LONGITUDE = -105;

    /**
     * Atkinson &amp; Wald (2007) intensity prediction equation coefficients: {@code MMI = c1 +
     * c2 (M − 6) + c3 (M − 6)^2 − c4 ln R − c5 R} with {@code R = sqrt(distance^2 + h^2)}.
     *
     * <p><strong>Verify against the paper before relying on the numbers:</strong> they are
     * transcribed for the California and central/eastern US regions and need a reread of Tables 4–5
     * of Atkinson, G. M. and Wald, D. J. (2007), "Did You Feel It? Intensity data reinterpreted",
     * <em>BSSA</em> 97(5), 1483–1495.
     */
    record Coefficients(double c1, double c2, double c3, double c4, double c5, double h) {}

    static final Coefficients CALIFORNIA = new Coefficients(4.63, 1.16, 0.44, 1.16, 0.0023, 6.0);
    static final Coefficients CEUS = new Coefficients(3.83, 1.12, 0.50, 1.18, 0.0025, 6.0);

    private final SourceDescriptor descriptor = layer(ID, "Felt-locally estimates (derived)",
            "Estimated shaking intensity at the operator's places from earthquakes, by ShakeMap or the"
                    + " Atkinson & Wald (2007) intensity prediction equation. Automated, not a warning.",
            "derived (input licences)", KIND).build();

    /** Creates the generator. */
    public FeltLocallyGenerator(ObjectProvider<SourceRegistry> registry) {
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
        return Set.of(DerivedSources.USGS, DerivedSources.EMSC, DerivedSources.FDSN);
    }

    @Override
    protected Set<String> inputKinds() {
        return Set.of("earthquake");
    }

    @Override
    protected boolean wantsPlaces() {
        return true;
    }

    @Override
    protected void clearState() {
        // Stateless.
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        int cap = maxEntities(ctx.params());
        List<Observation> events =
                ctx.observations().recent(explicitInputSources(), Set.of("earthquake"), now.minus(Duration.ofHours(24)), cap);
        if (events.isEmpty() || ctx.places().all().isEmpty()) {
            return;
        }
        Map<String, List<Observation>> contours = shakemap(ctx);
        LayerStatus coverage = status(ctx, now.minus(Duration.ofHours(24)));
        for (Observation event : events) {
            Double eLon = event.longitude();
            Double eLat = event.latitude();
            if (eLon == null || eLat == null) {
                continue;
            }
            Double magnitude = magnitude(event);
            if (magnitude == null) {
                continue;
            }
            Double depth = depth(event);
            for (PlaceView place : ctx.places().all()) {
                Geometry placeGeometry = place.geometry();
                if (placeGeometry == null || placeGeometry.isEmpty()) {
                    continue;
                }
                double pLon = placeGeometry.getCentroid().getX();
                double pLat = placeGeometry.getCentroid().getY();
                double distance = Geo.haversineKm(eLat, eLon, pLat, pLon);
                if (distance > MAX_KM) {
                    continue;
                }
                Estimate estimate = estimate(event, contours.get(event.externalId()), pLon, pLat, magnitude, depth);
                if (estimate.mmi < MIN_MMI) {
                    continue;
                }
                Map<String, Object> payload = LayerPayload.base(ID, coverage, 0.5, List.of());
                payload.put("mmi", band(estimate.mmi));
                payload.put("distanceKm", LayerPayload.km05(distance));
                payload.put("feltLikely", estimate.mmi >= FELT_MMI);
                payload.put("method", estimate.method);
                ctx.emit(NormalizedRecord.builder(event.externalId() + ":" + place.id(), KIND).observedAt(now)
                        .point(pLon, pLat).putAll(payload).build());
            }
        }
    }

    /** An intensity estimate and how it was obtained. */
    record Estimate(double mmi, String method) {}

    /** The ShakeMap contours keyed by event id. */
    private Map<String, List<Observation>> shakemap(DerivedContext ctx) {
        Map<String, List<Observation>> byEvent = new LinkedHashMap<>();
        List<Observation> contours =
                ctx.observations().recent(Set.of("usgs-shakemap"), Set.of("shakemap-mmi"), Instant.EPOCH, 100_000);
        for (Observation contour : contours) {
            String id = contour.externalId();
            int colon = id.indexOf(':');
            String eventId = colon > 0 ? id.substring(0, colon) : id;
            byEvent.computeIfAbsent(eventId, k -> new ArrayList<>()).add(contour);
        }
        return byEvent;
    }

    /** The intensity at a place: the nearest ShakeMap contour, else the IPE. */
    static Estimate estimate(Observation event, List<Observation> contours, double pLon, double pLat, double magnitude,
            Double depth) {
        if (contours != null && !contours.isEmpty()) {
            double best = Double.MAX_VALUE;
            double mmi = Double.NaN;
            for (Observation contour : contours) {
                Double mmiValue = contour.number("mmi");
                Geometry geometry = contour.geometry();
                if (mmiValue == null || geometry == null) {
                    continue;
                }
                double distance = Geo.haversineKm(pLat, pLon, geometry.getCentroid().getY(), geometry.getCentroid().getX());
                if (distance < best) {
                    best = distance;
                    mmi = mmiValue;
                }
            }
            if (!Double.isNaN(mmi)) {
                return new Estimate(mmi, "shakemap");
            }
        }
        double h = depth == null || depth <= 0 ? 10 : depth;
        double r = Math.sqrt(1.0 + h * h);
        Coefficients coefficients = pLon < CALIFORNIA_LONGITUDE ? CALIFORNIA : CEUS;
        double m = magnitude;
        double mmi = coefficients.c1() + coefficients.c2() * (m - 6) + coefficients.c3() * (m - 6) * (m - 6)
                - coefficients.c4() * Math.log(r) - coefficients.c5() * r;
        return new Estimate(mmi, "ipe-aw2007");
    }

    /** MMI rounded to the lower 0.5 band. */
    static double band(double mmi) {
        return Math.floor(mmi * 2.0) / 2.0;
    }

    private static Double magnitude(Observation event) {
        Double value = event.number("mag");
        if (value == null) {
            value = event.number("magnitude");
        }
        return value;
    }

    private static Double depth(Observation event) {
        Double value = event.number("depthKm");
        if (value == null) {
            value = event.number("depth");
        }
        return value;
    }
}
