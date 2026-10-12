package ai.foresignal.watch;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * What a watchlist identifies (prompt 036): the entity key it matches, its default normalization and
 * the validation of a value.
 *
 * <ul>
 *   <li>{@code icao24}: six hex digits (normalized upper case);
 *   <li>{@code registration}: a tail number, letters, digits and dashes up to 10 (upper);
 *   <li>{@code callsign}: letters and digits up to 8 (upper);
 *   <li>{@code mmsi}: nine digits (digits only);
 *   <li>{@code imo}: seven digits with a valid check digit (digits only, so "IMO 9074729" is accepted);
 *   <li>{@code custom}: any non-empty value up to 256 characters; rules must give a path.
 * </ul>
 *
 * <p>Identifier quality differs: ICAO 24-bit addresses and IMO numbers are stable; MMSIs are reused
 * and spoofed (Operator Guide, "Watchlists").
 */
public enum Identity {
    ICAO24("icao24", Normalize.UPPER, Pattern.compile("^[0-9A-Fa-f]{6}$")),
    REGISTRATION("registration", Normalize.UPPER, Pattern.compile("^[A-Za-z0-9-]{1,10}$")),
    CALLSIGN("callsign", Normalize.UPPER, Pattern.compile("^[A-Za-z0-9]{1,8}$")),
    MMSI("mmsi", Normalize.DIGITS, Pattern.compile("^[0-9]{9}$")),
    IMO("imo", Normalize.DIGITS, Pattern.compile("^[0-9]{7}$")),
    CUSTOM("custom", Normalize.NONE, Pattern.compile("^.{1,256}$", Pattern.DOTALL));

    private final String wire;
    private final Normalize defaultNormalize;
    private final Pattern pattern;

    Identity(String wire, Normalize defaultNormalize, Pattern pattern) {
        this.wire = wire;
        this.defaultNormalize = defaultNormalize;
        this.pattern = pattern;
    }

    /** The wire value, which is also the descriptor {@code entityKeys} type. */
    public String wire() {
        return wire;
    }

    /** The normalization a list gets when it names none. */
    public Normalize defaultNormalize() {
        return defaultNormalize;
    }

    /** The identity of a wire value. */
    public static Identity fromWire(String value) {
        for (Identity identity : values()) {
            if (identity.wire.equals(value)) {
                return identity;
            }
        }
        throw new IllegalArgumentException("must be icao24, registration, callsign, mmsi, imo or custom");
    }

    /**
     * Why a normalized value is not a valid identifier of this kind, or null when it is.
     *
     * @param normalized the value after the list's normalization
     */
    public String invalid(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return "is empty after normalization";
        }
        if (!pattern.matcher(normalized).matches()) {
            return switch (this) {
                case ICAO24 -> "must be six hex digits";
                case REGISTRATION -> "must be letters, digits and dashes (at most 10)";
                case CALLSIGN -> "must be letters and digits (at most 8)";
                case MMSI -> "must be nine digits";
                case IMO -> "must be seven digits";
                case CUSTOM -> "must be at most 256 characters";
            };
        }
        if (this == IMO && !imoCheckDigit(normalized)) {
            return "has a wrong IMO check digit";
        }
        return null;
    }

    /** True when the seventh digit is the sum of the first six times 7…2, modulo 10. */
    static boolean imoCheckDigit(String digits) {
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += (digits.charAt(i) - '0') * (7 - i);
        }
        return sum % 10 == digits.charAt(6) - '0';
    }

    /** How values are normalized before they are stored or compared. */
    public enum Normalize {
        UPPER("upper"),
        LOWER("lower"),
        DIGITS("digits"),
        NONE("none");

        private final String wire;

        Normalize(String wire) {
            this.wire = wire;
        }

        /** The wire value. */
        public String wire() {
            return wire;
        }

        /** The normalization of a wire value. */
        public static Normalize fromWire(String value) {
            for (Normalize n : values()) {
                if (n.wire.equals(value)) {
                    return n;
                }
            }
            throw new IllegalArgumentException("must be upper, lower, digits or none");
        }

        /**
         * {@code value} normalized in Java, exactly as {@link #sql} normalizes in SQL: spaces trimmed,
         * then upper or lower case, or every non-digit removed.
         */
        public String apply(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = trimSpaces(value);
            return switch (this) {
                case UPPER -> trimmed.toUpperCase(Locale.ROOT);
                case LOWER -> trimmed.toLowerCase(Locale.ROOT);
                case DIGITS -> trimmed.replaceAll("[^0-9]", "");
                case NONE -> trimmed;
            };
        }

        /** The SQL form of {@link #apply} over a text expression. */
        public String sql(String text) {
            return switch (this) {
                case UPPER -> "upper(btrim(" + text + "))";
                case LOWER -> "lower(btrim(" + text + "))";
                case DIGITS -> "regexp_replace(btrim(" + text + "), '[^0-9]', '', 'g')";
                case NONE -> "btrim(" + text + ")";
            };
        }

        /** Leading and trailing spaces removed (as PostgreSQL's {@code btrim} does by default). */
        static String trimSpaces(String value) {
            int start = 0;
            int end = value.length();
            while (start < end && value.charAt(start) == ' ') {
                start++;
            }
            while (end > start && value.charAt(end - 1) == ' ') {
                end--;
            }
            return value.substring(start, end);
        }
    }
}
