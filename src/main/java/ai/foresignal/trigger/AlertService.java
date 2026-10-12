package ai.foresignal.trigger;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * The alert lifecycle (prompt 017): firing → acknowledged → resolved, with feedback, notes and
 * snoozes. Resolved is terminal: the next enter edge fires a new alert, and cooldown still counts
 * from the resolved alert's {@code observed_at}.
 *
 * <p>Every change bumps {@code rev} and publishes an {@link AlertEvent} through
 * {@link AlertStore#write}. {@code actor} is the caller as {@code admin} or {@code token:<name>} (the
 * single-operator model has no user directory). Plain values in and out, so prompt 025's
 * {@code set_alert_status} tool can call it.
 */
@Service
public class AlertService {

    /** The longest note. */
    public static final int MAX_NOTE = 2_000;

    static final String ACK =
            "UPDATE alert SET state = 'acknowledged', acknowledged_at = COALESCE(acknowledged_at, :now),"
                    + " acknowledged_by = COALESCE(acknowledged_by, :actor), note = COALESCE(CAST(:note AS text), note),"
                    + " feedback = COALESCE(CAST(:feedback AS text), feedback),"
                    + " feedback_at = CASE WHEN CAST(:feedback AS text) IS NULL THEN feedback_at ELSE :now END,"
                    + " rev = nextval('alert_rev')"
                    + " WHERE id = :id AND state <> 'resolved'"
                    + " AND (state <> 'acknowledged' OR CAST(:note AS text) IS NOT NULL OR CAST(:feedback AS text) IS NOT NULL)"
                    + " RETURNING id";

    static final String RESOLVE =
            "UPDATE alert SET state = 'resolved', resolved_at = :now, resolved_by = :actor,"
                    + " note = COALESCE(CAST(:note AS text), note), feedback = COALESCE(CAST(:feedback AS text), feedback),"
                    + " feedback_at = CASE WHEN CAST(:feedback AS text) IS NULL THEN feedback_at ELSE :now END,"
                    + " rev = nextval('alert_rev') WHERE id = :id AND state <> 'resolved' RETURNING id";

    static final String SNOOZE =
            "UPDATE alert SET snoozed_until = :until, rev = nextval('alert_rev') WHERE id = :id RETURNING id";

    static final String UNSNOOZE =
            "UPDATE alert SET snoozed_until = NULL, rev = nextval('alert_rev')"
                    + " WHERE id = :id AND snoozed_until IS NOT NULL RETURNING id";

    static final String FEEDBACK =
            "UPDATE alert SET feedback = :feedback, feedback_at = :now, note = COALESCE(CAST(:note AS text), note),"
                    + " rev = nextval('alert_rev') WHERE id = :id RETURNING id";

    private final AlertStore store;
    private final Clock clock;

    /** Creates the service. */
    public AlertService(AlertStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    /**
     * An optional note and feedback ({@code AlertTransitionInput}, {@code AlertFeedbackInput}).
     *
     * @param note the note, or null to leave it
     * @param feedback the feedback wire value, or null to leave it
     */
    public record Transition(String note, String feedback) {

        /** Nothing to record. */
        public static final Transition NONE = new Transition(null, null);
    }

    /**
     * One alert.
     *
     * @throws NotFoundException when it does not exist
     */
    public AlertView get(long id) {
        return store.find(id).orElseThrow(() -> NotFoundException.of("Alert", id));
    }

    /** One page, newest first; see {@link AlertStore#list}. */
    public List<AlertView> list(AlertQuery query, Long beforeId, int limit) {
        return store.list(query, beforeId, limit);
    }

    /**
     * Acknowledges an alert: idempotent, and a no-op on a resolved alert. A note or feedback given
     * with a repeated acknowledge is still recorded.
     */
    public AlertView acknowledge(long id, Transition transition, String actor) {
        get(id);
        Instant now = clock.instant();
        return store.write(w -> w.update(AlertEvent.ACKNOWLEDGED, ACK, params(id, now)
                        .addValue("actor", actor)
                        .addValue("note", transition.note())
                        .addValue("feedback", transition.feedback())))
                .orElseGet(() -> get(id));
    }

    /** Resolves an alert ({@code resolved_by = actor}); idempotent on a resolved alert. */
    public AlertView resolve(long id, Transition transition, String actor) {
        get(id);
        Instant now = clock.instant();
        return store.write(w -> w.update(AlertEvent.RESOLVED, RESOLVE, params(id, now)
                        .addValue("actor", actor)
                        .addValue("note", transition.note())
                        .addValue("feedback", transition.feedback())))
                .orElseGet(() -> get(id));
    }

    /**
     * Snoozes an alert until {@code until}: new matches of the same rule and entity are recorded
     * suppressed ({@code alert_snoozed}) while it is open.
     */
    public AlertView snooze(long id, Instant until) {
        get(id);
        Instant now = clock.instant();
        return store.write(w -> w.update(AlertEvent.SNOOZED, SNOOZE, params(id, now).addValue("until", AlertStore.utc(until))))
                .orElseGet(() -> get(id));
    }

    /** Snoozes an alert as a {@code SnoozeInput} body asks. */
    public AlertView snooze(long id, JsonNode body) {
        get(id);
        return snooze(id, Snoozes.expiry(body, clock.instant(), null));
    }

    /** Clears an alert's snooze; idempotent. */
    public AlertView unsnooze(long id) {
        get(id);
        Instant now = clock.instant();
        return store.write(w -> w.update(AlertEvent.UPDATED, UNSNOOZE, params(id, now))).orElseGet(() -> get(id));
    }

    /** Sets feedback (required) and optionally a note, in any state. */
    public AlertView setFeedback(long id, Transition transition) {
        if (transition.feedback() == null) {
            throw ValidationException.forField("feedback", "is required");
        }
        get(id);
        Instant now = clock.instant();
        return store.write(w -> w.update(AlertEvent.UPDATED, FEEDBACK, params(id, now)
                        .addValue("feedback", transition.feedback())
                        .addValue("note", transition.note())))
                .orElseGet(() -> get(id));
    }

    /**
     * Reads an {@code AlertTransitionInput} or {@code AlertFeedbackInput} body.
     *
     * @param body the body, or null for none
     * @param feedbackRequired true for {@code setAlertFeedback}
     * @throws ValidationException when a field is not valid
     */
    public static Transition transition(JsonNode body, boolean feedbackRequired) {
        if (body == null || body.isNull() || body.isMissingNode()) {
            if (feedbackRequired) {
                throw ValidationException.forField("feedback", "is required");
            }
            return Transition.NONE;
        }
        if (!body.isObject()) {
            throw new ValidationException("The body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String note = null;
        JsonNode noteNode = body.get("note");
        if (noteNode != null && !noteNode.isNull()) {
            if (!noteNode.isString()) {
                errors.add(new FieldError("note", "must be a string"));
            } else if (noteNode.asString().length() > MAX_NOTE) {
                errors.add(new FieldError("note", "must be at most " + MAX_NOTE + " characters"));
            } else {
                note = noteNode.asString();
            }
        }
        String feedback = null;
        JsonNode feedbackNode = body.get("feedback");
        if (feedbackNode != null && !feedbackNode.isNull()) {
            try {
                feedback = Feedback.fromWire(feedbackNode.isString() ? feedbackNode.asString() : "").wire();
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("feedback", e.getMessage()));
            }
        } else if (feedbackRequired) {
            errors.add(new FieldError("feedback", "is required"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The request is not valid.", errors);
        }
        return new Transition(note, feedback);
    }

    private static MapSqlParameterSource params(long id, Instant now) {
        return new MapSqlParameterSource("id", id).addValue("now", AlertStore.utc(now));
    }
}
