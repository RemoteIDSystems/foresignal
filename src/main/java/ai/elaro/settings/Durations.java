package ai.elaro.settings;

import ai.elaro.api.error.InvalidInputException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Parses and formats the duration strings used across Elaro's API and settings.
 *
 * <p>Two spellings are accepted everywhere a duration is read: ISO-8601 ({@code PT15M},
 * {@code P7D}) and a shorthand ({@code 30s}, {@code 15m}, {@code 12h}, {@code 7d}, {@code 1w}).
 * Output always uses the shorthand, because that is what the OpenAPI examples show.
 */
public final class Durations {

    /** Shorthand: a non-negative integer followed by a unit suffix. */
    private static final Pattern SHORTHAND = Pattern.compile("^(\\d+)\\s*(s|m|h|d|w)$", Pattern.CASE_INSENSITIVE);

    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long SECONDS_PER_HOUR = 60L * 60L;
    private static final long SECONDS_PER_DAY = 24L * 60L * 60L;
    private static final long SECONDS_PER_WEEK = 7L * SECONDS_PER_DAY;

    private Durations() {}

    /**
     * Parses a duration in either spelling.
     *
     * <p>Zero is allowed here because some settings legitimately mean "no delay"; use
     * {@link #parsePositive(String)} where zero makes no sense.
     *
     * @throws InvalidInputException if the text is blank, negative or not a duration
     */
    public static Duration parse(String text) {
        if (text == null) {
            throw new InvalidInputException("Duration is required, but was null");
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidInputException("Duration is required, but was blank");
        }

        Duration parsed = parseShorthand(trimmed);
        if (parsed == null) {
            parsed = parseIso(trimmed);
        }
        if (parsed.isNegative()) {
            throw new InvalidInputException(
                    "Duration must not be negative, but was '" + trimmed + "'");
        }
        return parsed;
    }

    /**
     * Parses a duration that must be greater than zero.
     *
     * @throws InvalidInputException if the text is not a duration, or is zero or negative
     */
    public static Duration parsePositive(String text) {
        Duration parsed = parse(text);
        if (parsed.isZero()) {
            throw new InvalidInputException(
                    "Duration must be greater than zero, but was '" + text.trim() + "'");
        }
        return parsed;
    }

    private static Duration parseShorthand(String trimmed) {
        var matcher = SHORTHAND.matcher(trimmed);
        if (!matcher.matches()) {
            return null;
        }
        long amount;
        try {
            amount = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new InvalidInputException(
                    "Duration '" + trimmed + "' is too large to represent", e);
        }
        String unit = matcher.group(2).toLowerCase();
        long seconds =
                switch (unit) {
                    case "s" -> amount;
                    case "m" -> multiplyExact(amount, SECONDS_PER_MINUTE, trimmed);
                    case "h" -> multiplyExact(amount, SECONDS_PER_HOUR, trimmed);
                    case "d" -> multiplyExact(amount, SECONDS_PER_DAY, trimmed);
                    case "w" -> multiplyExact(amount, SECONDS_PER_WEEK, trimmed);
                    // The regex admits no other suffix.
                    default -> throw new InvalidInputException(
                            "Unsupported duration unit '" + unit + "' in '" + trimmed + "'");
                };
        return Duration.ofSeconds(seconds);
    }

    private static long multiplyExact(long amount, long factor, String trimmed) {
        try {
            return Math.multiplyExact(amount, factor);
        } catch (ArithmeticException e) {
            throw new InvalidInputException(
                    "Duration '" + trimmed + "' is too large to represent", e);
        }
    }

    private static Duration parseIso(String trimmed) {
        try {
            return Duration.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(
                    "Cannot parse duration '"
                            + trimmed
                            + "'. Expected ISO-8601 (PT15M, P7D) or shorthand (30s, 15m, 12h, 7d, 1w).",
                    e);
        }
    }

    /**
     * Formats a duration as the shortest shorthand that represents it exactly.
     *
     * <p>{@code 7d} rather than {@code 168h}, {@code 90m} rather than {@code 5400s}. Durations that
     * no whole unit divides exactly, and those with sub-second precision, fall back to ISO-8601 so
     * the value survives a round trip.
     *
     * <p>Days are the largest unit emitted. {@code 1w} parses, but seven days formats as
     * {@code 7d}, matching the retention examples in the OpenAPI document.
     *
     * @throws InvalidInputException if the duration is null or negative
     */
    public static String format(Duration duration) {
        if (duration == null) {
            throw new InvalidInputException("Duration is required, but was null");
        }
        if (duration.isNegative()) {
            throw new InvalidInputException("Duration must not be negative, but was " + duration);
        }
        if (duration.getNano() != 0) {
            // No shorthand unit is finer than a second.
            return duration.toString();
        }

        long seconds = duration.getSeconds();
        if (seconds == 0) {
            return "0s";
        }
        if (seconds % SECONDS_PER_DAY == 0) {
            return (seconds / SECONDS_PER_DAY) + "d";
        }
        if (seconds % SECONDS_PER_HOUR == 0) {
            return (seconds / SECONDS_PER_HOUR) + "h";
        }
        if (seconds % SECONDS_PER_MINUTE == 0) {
            return (seconds / SECONDS_PER_MINUTE) + "m";
        }
        return seconds + "s";
    }
}
