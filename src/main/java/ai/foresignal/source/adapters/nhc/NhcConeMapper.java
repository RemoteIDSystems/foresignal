// The storm-id derivation from idp_source follows God's Eye View, server/providers/cyclones.js (the
// ^((?:al|ep|cp)\d{6})-… pattern of parseCycloneForecast), MIT; see THIRD_PARTY_NOTICES.md.
package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Features of the NHC tropical weather summary MapServer as records (prompt 075). Pure.
 *
 * <ul>
 *   <li><strong>Storm id:</strong> lower-case basin, two-digit {@code stormnum} and year, for example
 *       {@code ep202026}, exactly the {@code externalId} of prompt 067's {@code nhc-cyclones}
 *       ({@link NhcCurrentStormsParser#ID}). It is read from the {@code idp_source} prefix ({@code
 *       ep202026-025A_5day_pts}) when that agrees with {@code basin} and {@code stormnum}; otherwise it is built
 *       from {@code basin}, {@code stormnum} and the year of {@code advdate} (else of {@code idp_filedate}, UTC). A
 *       feature whose id cannot be built or does not match the 067 pattern is skipped
 *       ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Ids per kind:</strong> {@code cyclone-cone} {@code stormId}; {@code cyclone-track}
 *       {@code stormId:track} (a record is keyed by source and {@code externalId} alone, Core-Data-Model §9, so the
 *       track cannot share the cone's id); {@code cyclone-warning} {@code stormId:<tcww>:<n>} (n counts the storm's
 *       segments of that type in the layer's object-id order, from 1); {@code cyclone-forecast-point}
 *       {@code stormId:tau<tau>}. The payload's {@code stormId} is always the bare 067 id.
 *   <li><strong>Time:</strong> {@code observedAt = idp_filedate} (the advisory product time, for every kind; a
 *       feature without it is skipped, {@value MappedRecord#MISSING_OBSERVED_AT}); {@code eventTime =
 *       validtime} for points only ({@code DD/HHMM} UTC, its month and year taken from the product time).
 *   <li><strong>Payload:</strong> {@code stormId, stormName, stormType, basin, advisoryNumber, advisoryDate,
 *       forecastPeriod}; warnings add {@code warningType} (the {@code tcww} code); points add {@code tau, validTime,
 *       maxWindKt, gustKt, mslpHpa, development, label, motionDirDeg, motionSpeedKt, saffirSimpson}. NHC's 9999
 *       placeholder (no pressure or motion past the first point) is left out. {@code objectid}, the {@code idp_*}
 *       fields, {@code st_area}, {@code st_perimeter}, {@code st_length}, {@code binnumber} and {@code shape} are
 *       never copied, nor any other field outside the list.
 * </ul>
 */
public final class NhcConeMapper {

    /** The kind of a forecast cone. */
    public static final String CONE_KIND = "cyclone-cone";

    /** The kind of a forecast track. */
    public static final String TRACK_KIND = "cyclone-track";

    /** The kind of a watch or warning segment. */
    public static final String WARNING_KIND = "cyclone-warning";

    /** The kind of a forecast point. */
    public static final String POINT_KIND = "cyclone-forecast-point";

    /** The suffix of a track's {@code externalId}. */
    public static final String TRACK_SUFFIX = ":track";

    /** NHC's placeholder for a missing value. */
    static final double MISSING = 9999;

    static final Pattern SOURCE_ID = Pattern.compile("^((?:al|ep|cp)(\\d{2})(\\d{4}))-", Pattern.CASE_INSENSITIVE);
    static final Pattern YEAR = Pattern.compile("(\\d{4})\\s*$");
    static final Pattern VALID_TIME = Pattern.compile("^(\\d{1,2})/(\\d{2})(\\d{2})$");

    private NhcConeMapper() {}

    /**
     * Maps one layer's features, in layer order.
     *
     * @param layer which layer they came from
     * @param features the GeoJSON features
     */
    public static List<MappedRecord> map(NhcLayerResolver.Layer layer, List<JsonNode> features) {
        List<MappedRecord> out = new ArrayList<>(features.size());
        Map<String, Integer> ordinals = new HashMap<>();
        for (JsonNode feature : features) {
            out.add(map(layer, feature, ordinals));
        }
        return out;
    }

    private static MappedRecord map(NhcLayerResolver.Layer layer, JsonNode feature, Map<String, Integer> ordinals) {
        JsonNode p = feature == null ? null : feature.get("properties");
        Instant filed = JsonProps.epochMillis(p, "idp_filedate");
        if (filed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        String stormId = stormId(p, filed);
        if (stormId == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (UpstreamFormatException e) {
            return MappedRecord.skipped(MappedRecord.BAD_GEOMETRY);
        }
        String id;
        Instant eventTime = null;
        String warningType = null;
        Long tau = null;
        switch (layer) {
            case POINTS -> {
                tau = JsonProps.integer(p, "tau");
                if (tau == null) {
                    return MappedRecord.skipped(MappedRecord.MISSING_ID);
                }
                id = stormId + ":tau" + tau;
                eventTime = validTime(JsonProps.text(p, "validtime"), filed);
            }
            case WARNING -> {
                warningType = JsonProps.text(p, "tcww");
                if (warningType == null) {
                    return MappedRecord.skipped(MappedRecord.MISSING_ID);
                }
                String group = stormId + ":" + warningType;
                int n = ordinals.merge(group, 1, Integer::sum);
                id = group + ":" + n;
            }
            case TRACK -> id = stormId + TRACK_SUFFIX;
            default -> id = stormId;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, layer.kind())
                .observedAt(filed)
                .eventTime(eventTime)
                .geometry(geometry.geometry())
                .put("stormId", stormId)
                .put("stormName", JsonProps.text(p, "stormname"))
                .put("stormType", JsonProps.text(p, "stormtype"))
                .put("basin", upper(JsonProps.text(p, "basin")))
                .put("advisoryNumber", JsonProps.text(p, "advisnum"))
                .put("advisoryDate", JsonProps.text(p, "advdate"))
                .put("forecastPeriod", present(JsonProps.integer(p, "fcstprd")))
                .put("warningType", warningType);
        if (layer == NhcLayerResolver.Layer.POINTS) {
            b.put("tau", tau)
                    .put("validTime", eventTime == null ? null : eventTime.toString())
                    .put("maxWindKt", present(JsonProps.integer(p, "maxwind")))
                    .put("gustKt", present(JsonProps.integer(p, "gust")))
                    .put("mslpHpa", present(JsonProps.integer(p, "mslp")))
                    .put("development", JsonProps.text(p, "tcdvlp"))
                    .put("label", JsonProps.text(p, "dvlbl"))
                    .put("motionDirDeg", present(JsonProps.integer(p, "tcdir")))
                    .put("motionSpeedKt", present(JsonProps.integer(p, "tcspd")))
                    .put("saffirSimpson", present(JsonProps.integer(p, "ssnum")));
        }
        if (geometry.simplified()) {
            b.put(FeatureGeometry.SIMPLIFIED_KEY, true).put(FeatureGeometry.TOLERANCE_KEY, geometry.toleranceDeg());
        }
        return MappedRecord.of(b.build());
    }

    /**
     * The storm id of a feature (see the class note), or null.
     *
     * @param p the feature properties
     * @param filed the product time, for the year fallback
     */
    static String stormId(JsonNode p, Instant filed) {
        String basin = JsonProps.text(p, "basin");
        Long number = JsonProps.integer(p, "stormnum");
        String source = JsonProps.text(p, "idp_source");
        if (source != null) {
            Matcher m = SOURCE_ID.matcher(source);
            if (m.find() && (basin == null || m.group(1).regionMatches(true, 0, basin, 0, 2))
                    && (number == null || Integer.parseInt(m.group(2)) == number)) {
                return valid(m.group(1).toLowerCase(Locale.ROOT));
            }
        }
        if (basin == null || number == null || number < 0 || number > 99) {
            return null;
        }
        Integer year = null;
        String advdate = JsonProps.text(p, "advdate");
        if (advdate != null) {
            Matcher m = YEAR.matcher(advdate);
            if (m.find()) {
                year = Integer.parseInt(m.group(1));
            }
        }
        if (year == null) {
            year = filed.atZone(ZoneOffset.UTC).getYear();
        }
        return valid(basin.toLowerCase(Locale.ROOT) + String.format(Locale.ROOT, "%02d%04d", number, year));
    }

    private static String valid(String id) {
        return NhcCurrentStormsParser.ID.matcher(id).matches() ? id : null;
    }

    /**
     * {@code DD/HHMM} (UTC) as an instant in the month of {@code filed}, or the adjacent month when the day is more
     * than 15 days away (a forecast across a month end); null when unreadable.
     *
     * @param text the {@code validtime} value
     * @param filed the product time
     */
    static Instant validTime(String text, Instant filed) {
        if (text == null) {
            return null;
        }
        Matcher m = VALID_TIME.matcher(text.strip());
        if (!m.matches()) {
            return null;
        }
        int day = Integer.parseInt(m.group(1));
        int hour = Integer.parseInt(m.group(2));
        int minute = Integer.parseInt(m.group(3));
        if (day < 1 || day > 31 || hour > 23 || minute > 59) {
            return null;
        }
        ZonedDateTime base = filed.atZone(ZoneOffset.UTC);
        YearMonth month = YearMonth.from(base);
        if (day < base.getDayOfMonth() - 15) {
            month = month.plusMonths(1);
        } else if (day > base.getDayOfMonth() + 15) {
            month = month.minusMonths(1);
        }
        if (day > month.lengthOfMonth()) {
            return null;
        }
        return LocalDateTime.of(month.getYear(), month.getMonth(), day, hour, minute).toInstant(ZoneOffset.UTC);
    }

    /** {@code value}, or null for NHC's 9999 placeholder. */
    private static Long present(Long value) {
        return value == null || value == MISSING ? null : value;
    }

    private static String upper(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }
}
