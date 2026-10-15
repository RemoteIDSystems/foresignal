package ai.foresignal.trigger.window;

import java.time.Duration;
import java.util.Locale;

/** Parses the duration shorthand the window spec accepts: ISO-8601 ({@code PT30M}) or {@code 30m}. */
public final class Durations {

    private Durations() {}

    /**
     * Parses {@code text} into a duration.
     *
     * @throws IllegalArgumentException when it is neither an ISO-8601 duration nor a {@code 30m}/{@code 6h}/{@code 7d} shorthand
     */
    public static Duration parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A duration is required");
        }
        String value = text.trim();
        if (value.regionMatches(true, 0, "P", 0, 1)) {
            return Duration.parse(value.toUpperCase(Locale.ROOT));
        }
        char unit = value.charAt(value.length() - 1);
        long amount;
        try {
            amount = Long.parseLong(value.substring(0, value.length() - 1).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unknown duration '" + text + "'");
        }
        return switch (Character.toLowerCase(unit)) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Unknown duration unit in '" + text + "'");
        };
    }
}
