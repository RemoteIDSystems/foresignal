package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Features of NHC's cumulative wind-speed probability layers as records (prompt 127). Pure.
 *
 * <p>The {@code NHC_tropical_weather_summary} MapServer publishes three polygon layers, "Probabilistic Winds 34 kts",
 * "50 kts" and "64 kts" (ids 30, 31 and 32 on 2026-10-17), one polygon per probability band in the field
 * {@code percentage}: {@code <5%}, {@code 5-10%}, {@code 10-20%} … {@code 80-90%}, {@code >90%}. The bands are the
 * cumulative probability that winds of at least that speed occur at a point within the 120 hours after the product
 * time ({@code idp_filedate}), for every active storm together; they carry no storm id.
 *
 * <ul>
 *   <li><strong>Ids:</strong> {@code {kt}kt:{low}-{high}} in percent, for example {@code 34kt:40-50}; {@code <5%} is
 *       {@code 34kt:0-5} and {@code >90%} is {@code 34kt:90-100}. A band that cannot be read is skipped
 *       ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Time:</strong> {@code observedAt = validFrom = idp_filedate}; {@code validUntil} 120 hours later.
 *   <li><strong>Payload:</strong> {@code thresholdKt}, {@code band} (as published), {@code pMin}, {@code pMax} (the
 *       band's bounds as probabilities; {@code >90%} has {@code pMax = 1}), {@code issuedAt},
 *       {@code validUntil} and {@code horizonHours} (120).
 * </ul>
 */
public final class NhcWindProbMapper {

    /** The kind of a wind-speed probability band. */
    public static final String KIND = "wind-probability";

    /** The cumulative horizon NHC's probabilities cover. */
    public static final Duration HORIZON = Duration.ofHours(120);

    static final Pattern RANGE = Pattern.compile("^(\\d{1,3})\\s*-\\s*(\\d{1,3})\\s*%?$");
    static final Pattern BELOW = Pattern.compile("^<\\s*(\\d{1,3})\\s*%?$");
    static final Pattern ABOVE = Pattern.compile("^>\\s*(\\d{1,3})\\s*%?$");

    private NhcWindProbMapper() {}

    /**
     * Maps one layer's features, in layer order.
     *
     * @param thresholdKt the wind threshold of the layer (34, 50 or 64)
     * @param features the GeoJSON features
     */
    public static List<MappedRecord> map(int thresholdKt, List<JsonNode> features) {
        List<MappedRecord> out = new ArrayList<>(features.size());
        for (JsonNode feature : features) {
            out.add(map(thresholdKt, feature));
        }
        return out;
    }

    private static MappedRecord map(int thresholdKt, JsonNode feature) {
        JsonNode p = feature == null ? null : feature.get("properties");
        Instant filed = JsonProps.epochMillis(p, "idp_filedate");
        if (filed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        String band = JsonProps.text(p, "percentage");
        int[] bounds = bounds(band);
        if (bounds == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            return MappedRecord.skipped(MappedRecord.BAD_GEOMETRY);
        }
        Instant until = filed.plus(HORIZON);
        NormalizedRecord.Builder b = NormalizedRecord.builder(externalId(thresholdKt, bounds), KIND)
                .observedAt(filed)
                .validFrom(filed)
                .validUntil(until)
                .geometry(geometry.geometry())
                .put("thresholdKt", thresholdKt)
                .put("band", band.strip())
                .put("pMin", bounds[0] / 100.0)
                .put("pMax", bounds[1] / 100.0)
                .put("issuedAt", filed.toString())
                .put("validUntil", until.toString())
                .put("horizonHours", HORIZON.toHours());
        if (geometry.simplified()) {
            b.put(FeatureGeometry.SIMPLIFIED_KEY, true).put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        }
        return MappedRecord.of(b.build());
    }

    /** The external id of a band. */
    static String externalId(int thresholdKt, int[] bounds) {
        return String.format(Locale.ROOT, "%dkt:%d-%d", thresholdKt, bounds[0], bounds[1]);
    }

    /**
     * The bounds of a published band in percent ({@code <5%} → 0–5, {@code >90%} → 90–100), or null when the text
     * is not a band or its bounds are not ordered within 0–100.
     *
     * @param band the {@code percentage} value
     */
    static int[] bounds(String band) {
        if (band == null) {
            return null;
        }
        String text = band.strip();
        int[] out = null;
        Matcher m = RANGE.matcher(text);
        if (m.matches()) {
            out = new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        } else if ((m = BELOW.matcher(text)).matches()) {
            out = new int[] {0, Integer.parseInt(m.group(1))};
        } else if ((m = ABOVE.matcher(text)).matches()) {
            out = new int[] {Integer.parseInt(m.group(1)), 100};
        }
        if (out == null || out[0] < 0 || out[1] > 100 || out[0] >= out[1]) {
            return null;
        }
        return out;
    }
}
