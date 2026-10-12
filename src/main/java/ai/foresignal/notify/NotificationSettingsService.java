package ai.foresignal.notify;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.NotificationSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.trigger.Severity;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * Parses and applies the global notification settings ({@code app_setting['notifications']},
 * prompt 018), shared by {@code updateNotificationSettings} and the settings import (prompt 019).
 *
 * <p>An omitted {@code pauseBypassSeverity} is {@code critical} and an explicit null means nothing
 * bypasses. When the pause ends earlier than before (or is lifted), deliveries it deferred are
 * brought forward to the new end. {@code chatops} (061) and {@code imageLinks} (108) are accepted
 * and not stored until those prompts.
 */
@Service
public class NotificationSettingsService {

    /** The members a {@code NotificationSettings} body may carry. */
    public static final Set<String> FIELDS = Set.of("pausedUntil", "pauseBypassSeverity", "chatops", "imageLinks");

    private final AppSettingService settings;
    private final NotificationService notifications;
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public NotificationSettingsService(AppSettingService settings, NotificationService notifications,
            NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.settings = settings;
        this.notifications = notifications;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The settings in force. */
    public NotificationSettings get() {
        return notifications.pause();
    }

    /** Stores {@code value} and releases deliveries the pause no longer defers. */
    @Transactional
    public NotificationSettings apply(NotificationSettings value) {
        settings.put(SettingKeys.NOTIFICATIONS, value);
        Instant now = clock.instant();
        Instant release = value.pausedAt(now) ? value.pausedUntil() : now;
        jdbc.update("""
                update notification_delivery set next_attempt_at = :release
                 where status = 'PENDING' and deferred_reason = 'paused' and next_attempt_at > :release
                """, new MapSqlParameterSource("release", Timestamp.from(release)));
        return value;
    }

    /**
     * Parses a {@code NotificationSettings} body.
     *
     * @throws ValidationException listing every problem
     */
    public static NotificationSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a NotificationSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a NotificationSettings member"));
            }
        }
        Instant until = null;
        JsonNode pausedUntil = body.get("pausedUntil");
        if (pausedUntil != null && !pausedUntil.isNull()) {
            try {
                until = Instant.parse(pausedUntil.asString());
            } catch (DateTimeParseException | IllegalArgumentException e) {
                errors.add(new FieldError("pausedUntil", "must be an ISO-8601 date-time or null"));
            }
        }
        String bypass = NotificationSettings.DEFAULT_BYPASS;
        if (body.has("pauseBypassSeverity")) {
            JsonNode node = body.get("pauseBypassSeverity");
            if (node.isNull()) {
                bypass = null;
            } else if (!node.isString() || !Severity.WIRE_VALUES.contains(node.asString())) {
                errors.add(new FieldError("pauseBypassSeverity", "must be info, warning, critical or null"));
            } else {
                bypass = node.asString();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The notification settings are not valid.", errors);
        }
        return new NotificationSettings(until, bypass);
    }

    /** The API form of {@code value}. */
    public static Map<String, Object> body(NotificationSettings value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pausedUntil", value.pausedUntil() == null ? null : value.pausedUntil().toString());
        out.put("pauseBypassSeverity", value.pauseBypassSeverity());
        return out;
    }
}
