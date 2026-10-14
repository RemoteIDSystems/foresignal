package ai.foresignal.source.adapters.radio;

import java.util.Locale;

/**
 * Maidenhead grid locators (prompt 082): a 4-, 6- or 8-character locator ({@code FN31}, {@code FN31pr},
 * {@code FN31pr21}; case-insensitive) to its cell's center point and envelope. Pure.
 *
 * <p>Pairs: field (A–R, 20° × 10°), square (0–9, 2° × 1°), subsquare (A–X, 5′ × 2.5′), extended square (0–9,
 * 30″ × 15″). Anything else, including an odd length or a character out of range, is malformed and yields null.
 */
public final class Maidenhead {

    private Maidenhead() {}

    /**
     * A locator's cell.
     *
     * @param locator the locator, normalized (field and subsquare letters as written by convention: {@code FN31pr})
     * @param west the cell's west edge in degrees
     * @param south the cell's south edge in degrees
     * @param widthDeg the cell's width in degrees of longitude
     * @param heightDeg the cell's height in degrees of latitude
     */
    public record Cell(String locator, double west, double south, double widthDeg, double heightDeg) {

        /** The center longitude. */
        public double centerLon() {
            return west + widthDeg / 2.0;
        }

        /** The center latitude. */
        public double centerLat() {
            return south + heightDeg / 2.0;
        }

        /** The east edge. */
        public double east() {
            return west + widthDeg;
        }

        /** The north edge. */
        public double north() {
            return south + heightDeg;
        }

        /**
         * Half the cell's diagonal in meters (spherical earth, 6,371 km), measured at the center latitude: the
         * {@code quality.accuracyMeters} of a position known only to this cell.
         */
        public double halfDiagonalMeters() {
            double r = 6_371_000.0;
            double dy = Math.toRadians(heightDeg) * r;
            double dx = Math.toRadians(widthDeg) * r * Math.cos(Math.toRadians(centerLat()));
            return Math.sqrt(dx * dx + dy * dy) / 2.0;
        }
    }

    /**
     * Decodes a locator.
     *
     * @param locator 4, 6 or 8 characters, case-insensitive; surrounding whitespace is ignored
     * @return the cell, or null when malformed
     */
    public static Cell decode(String locator) {
        if (locator == null) {
            return null;
        }
        String text = locator.strip().toUpperCase(Locale.ROOT);
        int n = text.length();
        if (n != 4 && n != 6 && n != 8) {
            return null;
        }
        char f1 = text.charAt(0);
        char f2 = text.charAt(1);
        char s1 = text.charAt(2);
        char s2 = text.charAt(3);
        if (f1 < 'A' || f1 > 'R' || f2 < 'A' || f2 > 'R' || !digit(s1) || !digit(s2)) {
            return null;
        }
        double west = (f1 - 'A') * 20.0 - 180.0 + (s1 - '0') * 2.0;
        double south = (f2 - 'A') * 10.0 - 90.0 + (s2 - '0') * 1.0;
        double width = 2.0;
        double height = 1.0;
        if (n >= 6) {
            char u1 = text.charAt(4);
            char u2 = text.charAt(5);
            if (u1 < 'A' || u1 > 'X' || u2 < 'A' || u2 > 'X') {
                return null;
            }
            width = 2.0 / 24.0;
            height = 1.0 / 24.0;
            west += (u1 - 'A') * width;
            south += (u2 - 'A') * height;
        }
        if (n == 8) {
            char e1 = text.charAt(6);
            char e2 = text.charAt(7);
            if (!digit(e1) || !digit(e2)) {
                return null;
            }
            width /= 10.0;
            height /= 10.0;
            west += (e1 - '0') * width;
            south += (e2 - '0') * height;
        }
        String normalized = text.substring(0, 4) + (n >= 6 ? text.substring(4, 6).toLowerCase(Locale.ROOT) : "")
                + (n == 8 ? text.substring(6, 8) : "");
        return new Cell(normalized, west, south, width, height);
    }

    /**
     * Decodes the longest valid prefix of 8, 6 or 4 characters (PSK Reporter sends 10-character locators such as
     * {@code CM88qg60fk}, whose fifth pair is not part of the 8-character system), or null when not even the first 4
     * characters are valid.
     */
    public static Cell decodeLenient(String locator) {
        if (locator == null) {
            return null;
        }
        String text = locator.strip();
        for (int length : new int[] {8, 6, 4}) {
            if (text.length() >= length) {
                Cell cell = decode(text.substring(0, length));
                if (cell != null) {
                    return cell;
                }
            }
        }
        return null;
    }

    private static boolean digit(char c) {
        return c >= '0' && c <= '9';
    }
}
