package ai.foresignal.raster.sample;

import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * A sample time expression (prompt 052; Weather-Imagery-Design §6.5): {@code latest}, {@code now}, an
 * ISO-8601 instant, or an offset from now such as {@code -PT1H} or {@code +PT6H}.
 *
 * <p>Past and present times choose observation frames; a time after now chooses the newest complete
 * forecast run. {@code latest} is the newest frame whatever its age (then judged stale against now);
 * {@code now} is the frame valid at or before now.
 *
 * @param kind which form
 * @param at the absolute time for {@link Kind#AT}, else null
 * @param offset the offset for {@link Kind#OFFSET}, else null
 * @param text the expression as given
 */
public record TimeSpec(Kind kind, Instant at, Duration offset, String text) {

    /** The forms. */
    public enum Kind {
        /** The newest frame. */
        LATEST,
        /** The frame valid at or before now. */
        NOW,
        /** An absolute time. */
        AT,
        /** Now plus an offset. */
        OFFSET
    }

    /** {@code latest}. */
    public static final TimeSpec LATEST = new TimeSpec(Kind.LATEST, null, null, "latest");

    /** {@code now}. */
    public static final TimeSpec NOW = new TimeSpec(Kind.NOW, null, null, "now");

    /** The largest offset accepted either way. */
    public static final Duration MAX_OFFSET = Duration.ofDays(16);

    /**
     * Parses an expression; blank is {@code latest}.
     *
     * @param field the parameter name, for the 400
     * @throws ValidationException when it is none of the forms
     */
    public static TimeSpec parse(String field, String text) {
        if (text == null || text.isBlank() || "latest".equalsIgnoreCase(text.strip())) {
            return LATEST;
        }
        String s = text.strip();
        if ("now".equalsIgnoreCase(s)) {
            return NOW;
        }
        if (s.startsWith("+") || s.startsWith("-") || s.startsWith("P") || s.startsWith("p")) {
            try {
                String body = s.startsWith("+") ? s.substring(1) : s;
                Duration d = Duration.parse(body);
                if (d.abs().compareTo(MAX_OFFSET) > 0) {
                    throw ValidationException.forField(field, "an offset must be within 16 days of now");
                }
                return new TimeSpec(Kind.OFFSET, null, d, s);
            } catch (DateTimeParseException e) {
                throw ValidationException.forField(field,
                        "must be latest, now, an ISO-8601 time or an offset such as -PT1H or +PT6H");
            }
        }
        try {
            return new TimeSpec(Kind.AT, Instant.parse(s), null, s);
        } catch (DateTimeParseException e) {
            try {
                return new TimeSpec(Kind.AT, OffsetDateTime.parse(s).toInstant(), null, s);
            } catch (DateTimeParseException e2) {
                throw ValidationException.forField(field,
                        "must be latest, now, an ISO-8601 time or an offset such as -PT1H or +PT6H");
            }
        }
    }

    /** The requested instant: now for {@code latest} and {@code now}. */
    public Instant target(Instant now) {
        return switch (kind) {
            case LATEST, NOW -> now;
            case AT -> at;
            case OFFSET -> now.plus(offset);
        };
    }

    /** True when the requested instant lies after now (a forecast time). */
    public boolean future(Instant now) {
        return target(now).isAfter(now);
    }
}
