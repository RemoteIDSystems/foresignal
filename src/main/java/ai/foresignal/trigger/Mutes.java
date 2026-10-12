package ai.foresignal.trigger;

import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

/**
 * Reads a {@code TriggerMuteInput} (prompt 033): exactly one of {@code until} or {@code for}, from
 * one hour to seven days ahead. A mute always has an expiry.
 */
public final class Mutes {

    /** The longest mute. */
    public static final Duration MAX = Duration.ofDays(7);

    /** The shortest mute. */
    public static final Duration MIN = Duration.ofHours(1);

    private Mutes() {}

    /**
     * The expiry {@code body} asks for.
     *
     * @throws ValidationException {@code 400} when it is not a valid mute
     */
    public static Instant expiry(JsonNode body, Instant now) {
        Instant until = Snoozes.expiry(body, now, MAX);
        if (until.isBefore(now.plus(MIN))) {
            throw ValidationException.forField(JsonTrees.present(body, "until") ? "until" : "for",
                    "must be at least 1h ahead");
        }
        return until;
    }
}
