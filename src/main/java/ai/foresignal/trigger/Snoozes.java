package ai.foresignal.trigger;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * Reads a {@code SnoozeInput}: exactly one of {@code until} (an RFC 3339 time in the future) or
 * {@code for} (a positive duration). A snooze always has an expiry.
 */
public final class Snoozes {

    /** The longest rule snooze (prompt 017). */
    public static final Duration MAX_RULE_SNOOZE = Duration.ofDays(7);

    private Snoozes() {}

    /**
     * The expiry {@code body} asks for.
     *
     * @param body the request body
     * @param now the current time
     * @param max the longest snooze allowed, or null for no limit
     * @throws ValidationException {@code 400} when the body is not a valid snooze
     */
    public static Instant expiry(JsonNode body, Instant now, Duration max) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The snooze must be a JSON object with until or for.");
        }
        boolean hasUntil = JsonTrees.present(body, "until");
        boolean hasFor = JsonTrees.present(body, "for");
        if (hasUntil == hasFor) {
            throw ValidationException.forField("until", "give exactly one of until or for");
        }
        Instant expiry;
        if (hasUntil) {
            JsonNode until = body.get("until");
            if (!until.isString()) {
                throw ValidationException.forField("until", "must be an RFC 3339 date-time");
            }
            try {
                expiry = OffsetDateTime.parse(until.asString()).toInstant();
            } catch (DateTimeParseException e) {
                throw ValidationException.forField("until", "must be an RFC 3339 date-time");
            }
            if (!expiry.isAfter(now)) {
                throw ValidationException.forField("until", "must be in the future");
            }
        } else {
            JsonNode duration = body.get("for");
            if (!duration.isString()) {
                throw ValidationException.forField("for", "must be a duration such as 2h or PT2H");
            }
            try {
                expiry = now.plus(Durations.parsePositive(duration.asString()));
            } catch (InvalidInputException e) {
                throw ValidationException.forField("for", e.getMessage());
            }
        }
        if (max != null && expiry.isAfter(now.plus(max))) {
            throw ValidationException.forField(hasUntil ? "until" : "for",
                    "must be at most " + Durations.format(max) + " ahead");
        }
        return expiry;
    }
}
