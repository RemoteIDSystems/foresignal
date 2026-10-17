package ai.foresignal.exercise;

import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The one place that decides how an exercise is marked (prompt 115). Every renderer calls it, so a rehearsal
 * can never be mistaken for a real alert:
 *
 * <ul>
 *   <li>{@link NotificationMessage}: title prefix {@value #TITLE_PREFIX} (on the title and the short title) and
 *       the body footer "This is an exercise replay of data from {from}–{to}. No action required.";
 *   <li>webhooks: header {@value #WEBHOOK_HEADER}{@code : true} and payload field {@code exercise: true};
 *   <li>push payloads (031/032): {@code exercise: true}, urgency forced to {@code normal} (never a time-sensitive
 *       or critical interruption level);
 *   <li>CAP (112): {@code status=Exercise};
 *   <li>MCP and SSE alert events: {@code exercise: true}.
 * </ul>
 *
 * <p>The exercise context of an alert ({@link #CONTEXT_FLAG}, {@link #CONTEXT_RUN}, {@link #CONTEXT_WINDOW},
 * {@link #CONTEXT_CHANNELS}, {@link #CONTEXT_ENRICH}) is stored in the alert's {@code context}, so the
 * notification router, the enrichment worker and CAP read it without depending on this package's tables.
 */
public final class ExerciseMarking {

    /** The title prefix of every exercise message. */
    public static final String TITLE_PREFIX = "[EXERCISE] ";

    /** The webhook header of an exercise delivery. */
    public static final String WEBHOOK_HEADER = "X-Foresignal-Exercise";

    /** The payload and context member that marks an exercise. */
    public static final String CONTEXT_FLAG = "exercise";

    /** The context member naming the exercise run. */
    public static final String CONTEXT_RUN = "exerciseRunId";

    /** The context member holding the replayed window {@code {from, to}}. */
    public static final String CONTEXT_WINDOW = "exerciseWindow";

    /** The context member listing the channels named in the run. */
    public static final String CONTEXT_CHANNELS = "exerciseChannelIds";

    /** The context member saying whether 030 enrichment may run for the alert. */
    public static final String CONTEXT_ENRICH = "exerciseEnrich";

    /** The {@code data} member of an exercise notification carrying the window. */
    public static final String DATA_MEMBER = "exercise";

    /** The CAP status of an exercise. */
    public static final String CAP_EXERCISE = "Exercise";

    /** The CAP status of a real alert. */
    public static final String CAP_ACTUAL = "Actual";

    private static final DateTimeFormatter WINDOW_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC);

    private ExerciseMarking() {}

    /** {@code title} with the exercise prefix, added once. */
    public static String title(String title) {
        String base = title == null ? "" : title;
        return base.startsWith(TITLE_PREFIX) ? base : TITLE_PREFIX + base;
    }

    /** The footer naming the replayed window; an unknown bound reads "unknown". */
    public static String footer(Instant from, Instant to) {
        return "This is an exercise replay of data from " + format(from) + "–" + format(to) + ". No action required.";
    }

    /** {@code body} with the footer appended once (a blank body becomes the footer alone). */
    public static String body(String body, Instant from, Instant to) {
        String footer = footer(from, to);
        if (body == null || body.isBlank()) {
            return footer;
        }
        return body.contains(footer) ? body : body.strip() + "\n\n" + footer;
    }

    /**
     * {@code message} marked as an exercise when its {@code exercise} flag is set: prefixed title and short title,
     * footer appended. Idempotent; a message that is not an exercise is returned unchanged.
     */
    public static NotificationMessage mark(NotificationMessage message) {
        if (message == null || !message.exercise()) {
            return message;
        }
        Instant[] window = window(message.data());
        String shortTitle = message.shortTitle() == null ? null : title(message.shortTitle());
        return new NotificationMessage(message.id(), message.event(), message.subtype(), title(message.title()),
                shortTitle, body(message.bodyMarkdown(), window[0], window[1]), message.severity(), message.url(),
                message.links(), message.data(), message.tags(), message.attachments(), message.updateOf(),
                message.origin(), message.createdAt(), message.alertId(), message.ruleId(), message.briefingRunId(),
                true);
    }

    /**
     * The replayed window {@code [from, to]} of a notification's {@code data}: {@code data.exercise.from/to};
     * either element may be null.
     */
    public static Instant[] window(Map<String, Object> data) {
        Instant[] out = new Instant[2];
        if (data != null && data.get(DATA_MEMBER) instanceof Map<?, ?> exercise) {
            out[0] = instant(exercise.get("from"));
            out[1] = instant(exercise.get("to"));
        }
        return out;
    }

    /** True when {@code alert} is an exercise: its column, or the 112 context flag {@code "exercise": true}. */
    public static boolean isExercise(AlertView alert, JsonMapper json) {
        if (alert == null) {
            return false;
        }
        if (alert.exercise()) {
            return true;
        }
        JsonNode context = context(alert, json);
        return context != null && context.path(CONTEXT_FLAG).asBoolean(false);
    }

    /** The CAP status of {@code alert}: {@value #CAP_EXERCISE} for an exercise, else {@value #CAP_ACTUAL}. */
    public static String capStatus(AlertView alert, JsonMapper json) {
        return isExercise(alert, json) ? CAP_EXERCISE : CAP_ACTUAL;
    }

    /** True when the exercise alert's run allowed 030 enrichment ({@code enrich=true}). */
    public static boolean enrichAllowed(AlertView alert, JsonMapper json) {
        JsonNode context = context(alert, json);
        return context != null && context.path(CONTEXT_ENRICH).asBoolean(false);
    }

    /** The parsed context of {@code alert}, or null. */
    public static JsonNode context(AlertView alert, JsonMapper json) {
        if (alert == null || alert.context() == null || alert.context().isBlank()) {
            return null;
        }
        try {
            return json.readTree(alert.context());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * True when an exercise alert may be routed to {@code channel}: the channel opted in with
     * {@code acceptExercise}, or the run named it. The channel's own filters ({@code accepts}) still apply.
     *
     * @param channel the channel
     * @param namedChannelIds the channel ids named in the run
     */
    public static boolean routes(ai.foresignal.notify.NotificationChannel channel,
            java.util.Collection<String> namedChannelIds) {
        return channel.acceptExercise()
                || (namedChannelIds != null && namedChannelIds.contains(channel.id().toString()));
    }

    /** The channel ids named in an exercise alert's context; empty when none. */
    public static java.util.List<String> namedChannels(AlertView alert, JsonMapper json) {
        JsonNode context = context(alert, json);
        java.util.List<String> out = new java.util.ArrayList<>();
        if (context != null && context.path(CONTEXT_CHANNELS).isArray()) {
            for (JsonNode id : context.path(CONTEXT_CHANNELS)) {
                out.add(id.asString());
            }
        }
        return out;
    }

    /** The {@code data.exercise} member of an exercise alert's notification: {@code {runId, from, to}}. */
    public static Map<String, Object> dataMember(AlertView alert, JsonMapper json) {
        JsonNode context = context(alert, json);
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("runId", alert.exerciseRunId() != null ? alert.exerciseRunId()
                : context == null ? null : context.path(CONTEXT_RUN).asString(null));
        JsonNode window = context == null ? null : context.path(CONTEXT_WINDOW);
        out.put("from", window == null || window.isMissingNode() ? null : window.path("from").asString(null));
        out.put("to", window == null || window.isMissingNode() ? null : window.path("to").asString(null));
        return out;
    }

    private static String format(Instant instant) {
        return instant == null ? "unknown" : WINDOW_FORMAT.format(instant);
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
