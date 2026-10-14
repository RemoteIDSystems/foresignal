package ai.foresignal.source.adapters.seismic;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * Maps an EMSC GeoJSON earthquake feature to a Foresignal {@code earthquake} record (prompt 089), shared by the
 * {@code emsc-seismic} stream and its FDSN gap-fill.
 *
 * <p>The payload keys match 011's USGS adapter where they exist, so one rule reads every earthquake:
 * {@code mag, magType, place, depthKm, auth, sourceCatalog, evtype}. Depth is in the payload, not the
 * geometry (the Adapter Guide altitude convention); the point is 2D.
 */
public final class SeismicRecords {

    /** The record kind. */
    public static final String KIND = "earthquake";

    private SeismicRecords() {}

    /**
     * The record of one EMSC/FDSN GeoJSON feature.
     *
     * @param feature the GeoJSON feature
     * @param fallbackObservedAt {@code lastupdate} is read from the properties; when absent this is used
     *     instead (the FDSN gap-fill uses the query time)
     * @return the record, or null when it has no {@code unid} or no origin time
     */
    public static NormalizedRecord fromFeature(JsonNode feature, Instant fallbackObservedAt) {
        if (feature == null || !feature.isObject()) {
            return null;
        }
        JsonNode props = feature.get("properties");
        if (props == null || !props.isObject()) {
            props = feature;
        }
        String unid = firstText(props, "unid", "id");
        if (unid == null) {
            unid = text(feature, "id");
        }
        if (unid == null || unid.isBlank()) {
            return null;
        }
        Instant time = time(props, "time");
        if (time == null) {
            return null;
        }
        Instant observedAt = time(props, "lastupdate");
        if (observedAt == null) {
            observedAt = fallbackObservedAt;
        }
        Double lat = number(props, "lat");
        Double lon = number(props, "lon");
        Double depth = number(props, "depth");
        JsonNode geometry = feature.get("geometry");
        if ((lat == null || lon == null) && geometry != null && geometry.get("coordinates") != null
                && geometry.get("coordinates").isArray() && geometry.get("coordinates").size() >= 2) {
            lon = geometry.get("coordinates").get(0).asDouble();
            lat = geometry.get("coordinates").get(1).asDouble();
            if (depth == null && geometry.get("coordinates").size() >= 3) {
                depth = geometry.get("coordinates").get(2).asDouble();
            }
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(unid, KIND)
                .observedAt(observedAt)
                .eventTime(time);
        if (lat != null && lon != null) {
            b.point(lon, lat);
        }
        b.put("mag", number(props, "mag"));
        b.put("magType", firstText(props, "magtype", "magType"));
        b.put("place", firstText(props, "flynn_region", "region"));
        b.put("depthKm", depth);
        b.put("auth", text(props, "auth"));
        b.put("sourceCatalog", firstText(props, "source_catalog", "sourceCatalog"));
        b.put("evtype", text(props, "evtype"));
        return b.build();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        try {
            return Double.parseDouble(value.asString().strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant time(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
