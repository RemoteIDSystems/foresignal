/*
 * The direction words and abbreviations are ported from God's Eye View, src/data/directionText.js
 * (directionToHeading) and server/providers/cctv/sources.js (DRIVEBC_ORIENTATION_HEADINGS),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; whole-field matching only (no free-text inference from names), 16-point abbreviations added, and
 * no fallback heading: text that is not a compass direction yields none.
 */
package ai.foresignal.source.adapters.cctv;

import java.util.Locale;
import java.util.Map;

/**
 * Turns a published compass direction into degrees (prompt 072).
 *
 * <p>Used only on fields an upstream documents as the camera's direction (TfL {@code view}, DriveBC
 * {@code orientation}, NSW {@code direction}, Ontario 511 {@code Direction}). The <strong>whole</strong> field must
 * be a direction: {@code "N"}, {@code "N-E"}, {@code "Northeast"}, {@code "Westbound"}, {@code "WB"}. Anything else,
 * including street names that merely contain a direction word and "Both Directions", yields null. There is no
 * fallback: God's Eye View's {@code fallbackHeadingFromId} is deliberately not ported.
 */
public final class Compass {

    private static final Map<String, Double> HEADINGS = Map.ofEntries(
            Map.entry("N", 0.0), Map.entry("NNE", 22.5), Map.entry("NE", 45.0), Map.entry("ENE", 67.5),
            Map.entry("E", 90.0), Map.entry("ESE", 112.5), Map.entry("SE", 135.0), Map.entry("SSE", 157.5),
            Map.entry("S", 180.0), Map.entry("SSW", 202.5), Map.entry("SW", 225.0), Map.entry("WSW", 247.5),
            Map.entry("W", 270.0), Map.entry("WNW", 292.5), Map.entry("NW", 315.0), Map.entry("NNW", 337.5),
            Map.entry("NORTH", 0.0), Map.entry("NORTHEAST", 45.0), Map.entry("EAST", 90.0),
            Map.entry("SOUTHEAST", 135.0), Map.entry("SOUTH", 180.0), Map.entry("SOUTHWEST", 225.0),
            Map.entry("WEST", 270.0), Map.entry("NORTHWEST", 315.0),
            Map.entry("NB", 0.0), Map.entry("EB", 90.0), Map.entry("SB", 180.0), Map.entry("WB", 270.0),
            Map.entry("NORTHBOUND", 0.0), Map.entry("EASTBOUND", 90.0), Map.entry("SOUTHBOUND", 180.0),
            Map.entry("WESTBOUND", 270.0));

    private Compass() {}

    /**
     * The heading of a whole-field compass direction.
     *
     * @param text the published direction, for example {@code "N-E"} or {@code "West"}
     * @return degrees in [0, 360), or null when {@code text} is not a compass direction
     */
    public static Double heading(String text) {
        if (text == null) {
            return null;
        }
        String key = text.strip().toUpperCase(Locale.ROOT).replace("-", "").replace(" ", "").replace("_", "");
        return key.isEmpty() ? null : HEADINGS.get(key);
    }

    /**
     * {@code degrees} folded into {@code [0, 360)}, or null when null or not finite. A value already in
     * range is returned unchanged (plain {@code (x % 360 + 360) % 360} perturbs it by an ulp).
     */
    public static Double normalize(Double degrees) {
        if (degrees == null || !Double.isFinite(degrees)) {
            return null;
        }
        double value = degrees % 360;
        return value < 0 ? value + 360 : value;
    }
}
