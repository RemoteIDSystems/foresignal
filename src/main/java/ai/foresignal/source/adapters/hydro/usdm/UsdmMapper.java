package ai.foresignal.source.adapters.hydro.usdm;

import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import tools.jackson.databind.JsonNode;

/**
 * The U.S. Drought Monitor's current map as {@code drought-area} records, one per category (prompt 079). Pure.
 *
 * <p>Verified 2026-10-13: {@code usdm_current.json} is a FeatureCollection of five MultiPolygons with only
 * {@code OBJECTID, DM} (0–4), {@code Shape_Length, Shape_Area}; the 2026-10-06 map was 23.8 MB with 138,210 vertices
 * in D0 down to 598 in D4, so every category but D4 exceeds the 10,000-vertex limit.
 *
 * <ul>
 *   <li>{@code externalId = "D" + DM}; geometry simplified by {@link FeatureGeometry} to at most
 *       {@value FeatureGeometry#MAX_VERTICES} vertices;
 *   <li>payload {@code category} ({@code D0}…{@code D4}), {@code label} ({@link #LABELS}), {@code mapDate}
 *       ({@code yyyy-MM-dd}), {@code geometrySimplified} and, when simplified, {@code simplifyToleranceDeg};
 *   <li>{@code observedAt = eventTime} = the map date at 00:00 UTC. The map is valid for the Tuesday before its
 *       Thursday release: {@link #mapDate} is the latest Tuesday on or before the file's {@code Last-Modified} date.
 * </ul>
 */
public final class UsdmMapper {

    /** The record kind. */
    public static final String KIND = "drought-area";

    /** The category labels, D0 to D4. */
    public static final List<String> LABELS = List.of("Abnormally Dry", "Moderate Drought", "Severe Drought",
            "Extreme Drought", "Exceptional Drought");

    /** Every category id. */
    public static final List<String> CATEGORIES = List.of("D0", "D1", "D2", "D3", "D4");

    private UsdmMapper() {}

    /**
     * The map's valid date: the latest Tuesday on or before {@code lastModified}'s UTC date.
     *
     * @param lastModified the file's {@code Last-Modified}
     */
    public static LocalDate mapDate(Instant lastModified) {
        return LocalDate.ofInstant(lastModified, ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY));
    }

    /** An HTTP date ({@code Thu, 08 Oct 2026 10:16:12 GMT}), or null when blank or unreadable. */
    public static Instant httpDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(text.strip(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Maps the collection.
     *
     * @param root the FeatureCollection
     * @param mapDate the map's valid date
     * @return the records by category, sorted
     * @throws UpstreamFormatException when it has no features array, a feature cannot be read, or no category is
     *     usable
     */
    public static List<NormalizedRecord> map(JsonNode root, LocalDate mapDate) {
        JsonNode features = root == null ? null : root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("USDM map has no features array", "");
        }
        Map<String, NormalizedRecord> byId = new TreeMap<>();
        for (JsonNode feature : features) {
            MappedRecord mapped = feature(feature, mapDate);
            if (mapped.accepted()) {
                byId.putIfAbsent(mapped.record().externalId(), mapped.record());
            }
        }
        if (byId.isEmpty() && !features.isEmpty()) {
            throw new UpstreamFormatException("USDM map has no usable drought category", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(byId.values());
        out.sort(Comparator.comparing(NormalizedRecord::externalId));
        return out;
    }

    /**
     * One category feature.
     *
     * @throws UpstreamFormatException when its geometry is malformed or cannot be simplified
     */
    static MappedRecord feature(JsonNode feature, LocalDate mapDate) {
        JsonNode p = feature == null ? null : feature.get("properties");
        Double dm = p == null ? null : HydroSources.number(p.get("DM"));
        if (dm == null || dm != Math.rint(dm) || dm < 0 || dm > 4) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        int level = dm.intValue();
        FeatureGeometry.Result geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        if (geometry.geometry() == null) {
            return MappedRecord.skipped(MappedRecord.BAD_GEOMETRY);
        }
        Instant at = mapDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        NormalizedRecord.Builder b = NormalizedRecord.builder(CATEGORIES.get(level), KIND)
                .observedAt(at).eventTime(at).geometry(geometry.geometry())
                .put("category", CATEGORIES.get(level))
                .put("label", LABELS.get(level))
                .put("mapDate", mapDate.toString())
                .put(FeatureGeometry.SIMPLIFIED_KEY, geometry.simplified())
                .put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        return MappedRecord.of(b.build());
    }

    /**
     * Tombstones for the categories of the previous map that this map lacks (for example no D4).
     *
     * @param previous the previous map's categories
     * @param current this map's categories
     * @param at the tombstones' time (this map's date)
     */
    public static List<NormalizedRecord> tombstones(Collection<String> previous, Collection<String> current,
            Instant at) {
        Set<String> gone = new TreeSet<>(previous);
        gone.removeAll(current);
        List<NormalizedRecord> out = new ArrayList<>();
        for (String id : gone) {
            out.add(NormalizedRecord.tombstone(id, KIND, at));
        }
        return out;
    }
}
