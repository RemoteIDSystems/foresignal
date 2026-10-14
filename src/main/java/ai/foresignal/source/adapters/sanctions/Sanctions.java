package ai.foresignal.source.adapters.sanctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared pieces of the sanctions parsers (prompt 091): the record shape, party types and identifier validation. */
public final class Sanctions {

    /** The record kind of every sanctions source. */
    public static final String KIND = "sanction";

    /** Aliases are capped at this many. */
    public static final int MAX_ALIASES = 50;

    /** A single alias is capped at this many characters. */
    public static final int MAX_ALIAS_CHARS = 200;

    /** Identifiers are capped at this many. */
    public static final int MAX_IDENTIFIERS = 30;

    /** Addresses are capped at this many. */
    public static final int MAX_ADDRESSES = 10;

    /** Remarks are capped at this many characters. */
    public static final int MAX_REMARKS = 2048;

    private Sanctions() {}

    /** {@code individual}, {@code entity}, {@code vessel} or {@code aircraft} from an upstream type string. */
    public static String partyType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.strip().toLowerCase(Locale.ROOT)) {
            case "individual", "person" -> "individual";
            case "entity", "organisation", "organization" -> "entity";
            case "vessel", "ship" -> "vessel";
            case "aircraft", "plane" -> "aircraft";
            default -> null;
        };
    }

    /** The digits of {@code value}, or null when it holds none. */
    public static String digits(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                out.append(value.charAt(i));
            }
        }
        return out.isEmpty() ? null : out.toString();
    }

    /** True when {@code value} is a seven-digit IMO number with a valid check digit. */
    public static boolean isImo(String value) {
        String digits = digits(value);
        if (digits == null || digits.length() != 7) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += (digits.charAt(i) - '0') * (7 - i);
        }
        return sum % 10 == digits.charAt(6) - '0';
    }

    /** True when {@code value} is a nine-digit MMSI. */
    public static boolean isMmsi(String value) {
        String digits = digits(value);
        return digits != null && digits.length() == 9;
    }

    /** The first {@code max} strings, each capped at {@code maxChars} characters. */
    public static List<String> cap(List<String> values, int max, int maxChars) {
        List<String> out = new ArrayList<>();
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String trimmed = value.strip();
            if (maxChars > 0 && trimmed.length() > maxChars) {
                trimmed = trimmed.substring(0, maxChars - 1) + "…";
            }
            out.add(trimmed);
            if (out.size() >= max) {
                break;
            }
        }
        return out;
    }

    /** {@code value} capped at {@code max} characters, or null. */
    public static String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max - 1) + "…";
    }

    /** An identifier map {@code {type, value, country}}, dropping null members. */
    public static java.util.Map<String, Object> identifier(String type, String value, String country) {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("type", truncate(type, 200));
        out.put("value", truncate(value, 500));
        if (country != null && !country.isBlank()) {
            out.put("country", country.strip());
        }
        return out;
    }
}
