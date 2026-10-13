package ai.foresignal.source.adapters.hydro.ea;

import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import tools.jackson.databind.JsonNode;

/**
 * The Environment Agency flood-monitoring API's current flood warnings as {@code flood-warning} records, and their
 * flood-area polygons (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13: {@code /flood-monitoring/id/floods} answers JSON-LD {@code {meta: {licence: OGL v3, …},
 * items: [...]}}; each item has {@code floodAreaID, severity} ({@code Flood alert}), {@code severityLevel} (1 severe
 * flood warning, 2 flood warning, 3 flood alert, 4 no longer in force), {@code description, eaAreaName, message,
 * isTidal, timeRaised, timeMessageChanged, timeSeverityChanged} (local UK times without an offset) and
 * {@code floodArea.polygon}, a separate GeoJSON FeatureCollection per area ({@code …/floodAreas/{id}/polygon}).
 *
 * <ul>
 *   <li>{@code externalId = floodAreaID}; level 4 is a tombstone;
 *   <li>payload {@code severity, severityLevel, description, eaAreaName, message} (at most {@value #MAX_MESSAGE_BYTES}
 *       bytes, untrusted text), {@code isTidal};
 *   <li>{@code observedAt = timeMessageChanged}, {@code eventTime = timeRaised}, both read in {@code Europe/London};
 *   <li>geometry: the area polygon when it has been fetched, simplified by {@link FeatureGeometry}; none before.
 * </ul>
 */
public final class EaFloodParser {

    /** The record kind. */
    public static final String KIND = "flood-warning";

    /** The severity level meaning "no longer in force". */
    public static final int NO_LONGER_IN_FORCE = 4;

    /** The longest message kept, UTF-8 bytes. */
    public static final int MAX_MESSAGE_BYTES = 4096;

    /** The zone of the API's local times. */
    public static final ZoneId LONDON = ZoneId.of("Europe/London");

    private EaFloodParser() {}

    /**
     * The items of a {@code floods} answer.
     *
     * @throws UpstreamFormatException when it is not JSON or has no {@code items} array
     */
    public static List<JsonNode> items(byte[] body) {
        JsonNode root = HydroSources.json(body, "EA floods");
        JsonNode items = root.get("items");
        if (items == null || !items.isArray()) {
            throw new UpstreamFormatException("EA floods has no items array", "");
        }
        List<JsonNode> out = new ArrayList<>();
        items.forEach(out::add);
        return out;
    }

    /** The flood area id of an item, or null. */
    public static String areaId(JsonNode item) {
        return item == null ? null : HydroSources.text(item.get("floodAreaID"));
    }

    /** The severity level of an item, or null. */
    public static Integer severityLevel(JsonNode item) {
        Double level = item == null ? null : HydroSources.number(item.get("severityLevel"));
        return level == null || level != Math.rint(level) ? null : level.intValue();
    }

    /**
     * Maps one item.
     *
     * @param item the warning
     * @param polygon the area polygon, or null when not fetched yet
     * @return a record, a tombstone for level 4, or the skip reason
     */
    public static MappedRecord item(JsonNode item, Geometry polygon) {
        String id = areaId(item);
        if (id == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant changed = local(HydroSources.text(item.get("timeMessageChanged")));
        if (changed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        Integer level = severityLevel(item);
        if (level != null && level == NO_LONGER_IN_FORCE) {
            return MappedRecord.of(NormalizedRecord.tombstone(id, KIND, changed));
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(changed)
                .eventTime(local(HydroSources.text(item.get("timeRaised"))))
                .geometry(polygon)
                .put("severity", HydroSources.text(item.get("severity")))
                .put("severityLevel", level == null ? null : level.longValue())
                .put("description", HydroSources.text(item.get("description")))
                .put("eaAreaName", HydroSources.text(item.get("eaAreaName")))
                .put("message", cap(HydroSources.text(item.get("message"))));
        JsonNode tidal = item.get("isTidal");
        if (tidal != null && tidal.isBoolean()) {
            b.put("isTidal", tidal.booleanValue());
        }
        return MappedRecord.of(b.build());
    }

    /** A local UK time ({@code 2026-10-13T15:41:00}) or an ISO instant, or null. */
    static Instant local(String text) {
        if (text == null) {
            return null;
        }
        Instant withOffset = HydroSources.instant(text);
        if (withOffset != null) {
            return withOffset;
        }
        try {
            return LocalDateTime.parse(text.strip()).atZone(LONDON).toInstant();
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** {@code text} cut to at most {@value #MAX_MESSAGE_BYTES} UTF-8 bytes, on a character boundary. */
    static String cap(String text) {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length <= MAX_MESSAGE_BYTES) {
            return text;
        }
        int bytes = 0;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes + size > MAX_MESSAGE_BYTES) {
                break;
            }
            out.appendCodePoint(cp);
            bytes += size;
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    /**
     * The area polygon of a {@code …/polygon} answer: its first feature's geometry, simplified to fit.
     *
     * @throws UpstreamFormatException when it is not a FeatureCollection with a geometry
     */
    public static FeatureGeometry.Result polygon(byte[] body) {
        JsonNode root = HydroSources.json(body, "EA flood area polygon");
        JsonNode features = root.get("features");
        JsonNode geometry = "Feature".equals(HydroSources.text(root.get("type"))) ? root.get("geometry")
                : features != null && features.isArray() && !features.isEmpty() ? features.get(0).get("geometry")
                : null;
        FeatureGeometry.Result result = FeatureGeometry.fromGeoJson(geometry);
        if (result.geometry() == null) {
            throw new UpstreamFormatException("EA flood area polygon has no geometry", "");
        }
        return result;
    }

    /** A geometry as base64 WKB, for the adapter state. */
    public static String encode(Geometry geometry) {
        return Base64.getEncoder().encodeToString(new WKBWriter().write(geometry));
    }

    /** A geometry written by {@link #encode}, SRID 4326; null for null or damaged text. */
    public static Geometry decode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry g = new WKBReader().read(Base64.getDecoder().decode(text.strip()));
            g.setSRID(Geo.SRID);
            return g;
        } catch (ParseException | IllegalArgumentException e) {
            return null;
        }
    }
}
