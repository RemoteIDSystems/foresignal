package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;

/**
 * Maps AWC {@code /api/data/pirep?format=geojson} features to {@code pirep} records (prompt 087). Pure.
 *
 * <p>Verified 2026-10-14 against a live CONUS response (the fixture, 88 reports): every feature is a Point; the
 * properties are {@code icaoId, rawOb, airepType} ({@code PIREP}, {@code Urgent PIREP}, {@code AIREP}), {@code acType,
 * fltlvlType, receiptTime, obsTime} (both ISO-8601 text, not the epoch number AWC's OpenAPI documents for
 * {@code obsTime}), {@code fltlvl} (hundreds of feet; absent for {@code FLUNKN}), {@code tbInt1, tbType1, tbFreq1,
 * icgInt1, icgType1, clouds} ({@code [{cover, base, top}]}), {@code temp, wdir, wspd} and the decoded aircraft name.
 * OpenAPI spellings ({@code fltLvl}, {@code pirepType}, {@code wxString}) are read too.
 *
 * <p>Identity {@code receiptTime:lat:lon:sha1(rawOb)[0..8]}: the receipt time as published, the point's coordinates
 * as published and the first 8 hex digits of the SHA-1 of the raw report, so a re-fetched report is the same record.
 * Payload {@code fltLvl, acType, turbulence, icing, skyCover, wx, raw, reportType}; {@code eventTime} =
 * {@code observedAt} = {@code obsTime}. {@code raw} is upstream text: untrusted, stored as data only.
 */
public final class PirepParser {

    /** The record kind. */
    public static final String KIND = "pirep";

    private PirepParser() {}

    /**
     * Maps the features; a feature without a point, an observation time, a receipt time or a raw report is skipped
     * and counted, and a repeated identity keeps its first feature.
     */
    public static AwcHazardParser.Parsed parse(List<JsonNode> features) {
        Map<String, NormalizedRecord> records = new LinkedHashMap<>();
        int skipped = 0;
        for (JsonNode feature : features) {
            JsonNode p = feature.get("properties");
            Geometry geometry = AwcSupport.geometry(feature);
            if (p == null || !(geometry instanceof Point point) || point.isEmpty()) {
                skipped++;
                continue;
            }
            String receipt = AwcSupport.text(p.get("receiptTime"));
            Instant observed = AwcSupport.instant(p.get("obsTime"));
            String raw = AwcSupport.text(p.get("rawOb"));
            if (receipt == null || observed == null || raw == null) {
                skipped++;
                continue;
            }
            JsonNode coordinates = feature.get("geometry").get("coordinates");
            String id = receipt + ":" + coordinate(coordinates.get(1)) + ":" + coordinate(coordinates.get(0)) + ":"
                    + sha1(raw).substring(0, 8);
            if (records.containsKey(id)) {
                skipped++;
                continue;
            }
            Long level = AwcSupport.integer(p.get("fltlvl"));
            if (level == null) {
                level = AwcSupport.integer(p.get("fltLvl"));
            }
            String type = AwcSupport.text(p.get("airepType"));
            if (type == null) {
                type = AwcSupport.text(p.get("pirepType"));
            }
            String wx = AwcSupport.text(p.get("wxString"));
            records.put(id, NormalizedRecord.builder(id, KIND)
                    .observedAt(observed)
                    .eventTime(observed)
                    .geometry(point)
                    .put("fltLvl", level)
                    .put("acType", AwcSupport.text(p.get("acType")))
                    .put("turbulence", join(AwcSupport.text(p.get("tbInt1")), AwcSupport.text(p.get("tbType1"))))
                    .put("icing", join(AwcSupport.text(p.get("icgInt1")), AwcSupport.text(p.get("icgType1"))))
                    .put("skyCover", skyCover(p.get("clouds")))
                    .put("wx", wx == null ? AwcSupport.text(p.get("wx")) : wx)
                    .put("raw", AwcHazardParser.raw(raw))
                    .put("reportType", type)
                    .build());
        }
        return new AwcHazardParser.Parsed(new ArrayList<>(records.values()), skipped, 0);
    }

    /** The cloud covers in order, space-separated ({@code BKN OVC}), or null when none. */
    static String skyCover(JsonNode clouds) {
        if (clouds == null || !clouds.isArray() || clouds.isEmpty()) {
            return null;
        }
        List<String> covers = new ArrayList<>();
        for (JsonNode cloud : clouds) {
            String cover = AwcSupport.text(cloud.get("cover"));
            if (cover != null) {
                covers.add(cover);
            }
        }
        return covers.isEmpty() ? null : String.join(" ", covers);
    }

    /** {@code intensity} and {@code type} joined by a space ({@code MOD CHOP}); null when both are absent. */
    static String join(String intensity, String type) {
        if (intensity == null) {
            return type;
        }
        return type == null ? intensity : intensity + " " + type;
    }

    /** A coordinate as published, without trailing zeros. */
    static String coordinate(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return AwcSupport.text(node);
        }
        return node.decimalValue().stripTrailingZeros().toPlainString();
    }

    /** The lower-case hex SHA-1 of {@code text} (UTF-8). */
    static String sha1(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is required by the Java platform", e);
        }
    }
}
