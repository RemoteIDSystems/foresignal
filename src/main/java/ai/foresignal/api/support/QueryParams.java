package ai.foresignal.api.support;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Parsers for scalar and multi-value query parameters.
 *
 * <p>Controllers take raw strings and parse them here, so every malformed value becomes the same
 * {@code 400 VALIDATION_FAILED} with an {@code errors[].field}, instead of the framework's generic
 * type-mismatch response.
 */
public final class QueryParams {

    /** The most values a multi-value filter such as {@code externalId} accepts. */
    public static final int MAX_VALUES = 100;

    private QueryParams() {}

    /**
     * Joins repeated and comma-separated values ({@code ?kind=a&kind=b,c}), trimmed, deduplicated
     * and in first-seen order.
     *
     * @param name the parameter name, for errors
     * @param raw the values as bound, or null
     * @param max the most distinct values allowed
     * @return the values; empty when none were given
     */
    public static List<String> multi(String name, List<String> raw, int max) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        Set<String> values = new LinkedHashSet<>();
        for (String entry : raw) {
            if (entry == null) {
                continue;
            }
            for (String part : entry.split(",")) {
                String value = part.strip();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        }
        if (values.size() > max) {
            throw ValidationException.forField(name, "at most " + max + " values are allowed, got " + values.size());
        }
        return List.copyOf(values);
    }

    /** {@link #multi(String, List, int)} with {@link #MAX_VALUES}. */
    public static List<String> multi(String name, List<String> raw) {
        return multi(name, raw, MAX_VALUES);
    }

    /**
     * An integer in {@code [min, max]}, or {@code defaultValue} when absent.
     *
     * @throws ValidationException when malformed or out of range
     */
    public static int intInRange(String name, String raw, int min, int max, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        int value;
        try {
            value = Integer.parseInt(raw.strip());
        } catch (NumberFormatException e) {
            throw ValidationException.forField(name, "'" + raw.strip() + "' is not an integer");
        }
        if (value < min || value > max) {
            throw ValidationException.forField(name, "must be between " + min + " and " + max);
        }
        return value;
    }

    /**
     * A non-negative long, or null when absent.
     *
     * @throws ValidationException when malformed or negative
     */
    public static Long nonNegativeLong(String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        long value;
        try {
            value = Long.parseLong(raw.strip());
        } catch (NumberFormatException e) {
            throw ValidationException.forField(name, "'" + raw.strip() + "' is not an integer");
        }
        if (value < 0) {
            throw ValidationException.forField(name, "must not be negative");
        }
        return value;
    }

    /**
     * A finite number in {@code [min, max]}, or null when absent.
     *
     * @throws ValidationException when malformed or out of range
     */
    public static Double doubleInRange(String name, String raw, double min, double max) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        double value;
        try {
            value = Double.parseDouble(raw.strip());
        } catch (NumberFormatException e) {
            throw ValidationException.forField(name, "'" + raw.strip() + "' is not a number");
        }
        if (!Double.isFinite(value) || value < min || value > max) {
            throw ValidationException.forField(name, "must be between " + format(min) + " and " + format(max));
        }
        return value;
    }

    /**
     * {@code true}/{@code false} (case-insensitive), or {@code defaultValue} when absent.
     *
     * @throws ValidationException for any other value
     */
    public static boolean bool(String name, String raw, boolean defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return switch (raw.strip().toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw ValidationException.forField(name, "must be true or false");
        };
    }

    /**
     * An RFC 3339 instant ({@code 2026-10-01T12:00:00Z} or with an offset), or null when absent.
     *
     * @throws ValidationException when malformed
     */
    public static Instant instant(String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.strip();
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(text).toInstant();
            } catch (DateTimeParseException e2) {
                throw ValidationException.forField(name, "'" + text + "' is not an RFC 3339 date-time");
            }
        }
    }

    /**
     * One value of {@code allowed} (case-insensitive), or {@code defaultValue} when absent.
     *
     * @return the matching allowed value, in its canonical spelling
     */
    public static String oneOf(String name, String raw, List<String> allowed, String defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        String value = raw.strip();
        for (String candidate : allowed) {
            if (candidate.equalsIgnoreCase(value)) {
                return candidate;
            }
        }
        throw ValidationException.forField(name, "must be one of " + String.join(", ", allowed));
    }

    /**
     * Values from {@code allowed} (repeatable or comma-separated); any other value is rejected.
     */
    public static Set<String> enumSet(String name, List<String> raw, List<String> allowed) {
        List<String> values = multi(name, raw, allowed.size() + MAX_VALUES);
        Set<String> result = new LinkedHashSet<>();
        List<String> unknown = new ArrayList<>();
        for (String value : values) {
            String match = null;
            for (String candidate : allowed) {
                if (candidate.equalsIgnoreCase(value)) {
                    match = candidate;
                }
            }
            if (match == null) {
                unknown.add(value);
            } else {
                result.add(match);
            }
        }
        if (!unknown.isEmpty()) {
            throw ValidationException.forField(
                    name, "unknown value(s) " + String.join(", ", unknown) + "; allowed: " + String.join(", ", allowed));
        }
        return Set.copyOf(result);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }
}
