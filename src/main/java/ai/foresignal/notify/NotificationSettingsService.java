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
 * brought forward to the new end. {@code chatops.enabled} (prompt 061) is the global kill switch
 * of interactive chat channels (omitted: true). {@code imageLinks} (108) is accepted and not stored
 * until that prompt. {@code defaultLanguage} (prompt 141) is the BCP 47 language notifications are written in
 * (default {@code en}); channels with another {@code language} get translations.
 */
@Service
public class NotificationSettingsService {

    /** The members a {@code NotificationSettings} body may carry. */
    public static final Set<String> FIELDS = Set.of("pausedUntil", "pauseBypassSeverity", "chatops", "imageLinks",
            "defaultLanguage");

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
        if (value.imageLinksOn()) {
            ai.foresignal.settings.ServerSettings server = settings.getOrDefault(SettingKeys.SERVER,
                    ai.foresignal.settings.ServerSettings.class, ai.foresignal.settings.ServerSettings::defaults);
            String base = server.normalizedPublicBaseUrl();
            if (base == null || !base.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) {
                throw new ai.foresignal.api.error.ForesignalException(org.springframework.http.HttpStatus.BAD_REQUEST,
                        ai.foresignal.api.error.ErrorCodes.IMAGE_LINKS_REQUIRE_HTTPS,
                        "notifications.imageLinks needs a publicBaseUrl with https.");
            }
        }
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
        NotificationSettings.ChatOps chatops = chatops(body.get("chatops"), errors);
        boolean imageLinks = false;
        if (body.has("imageLinks")) {
            JsonNode node = body.get("imageLinks");
            if (node.isNull()) {
                imageLinks = false;
            } else if (!node.isBoolean()) {
                errors.add(new FieldError("imageLinks", "must be true or false"));
            } else {
                imageLinks = node.asBoolean();
            }
        }
        String defaultLanguage = NotificationSettings.DEFAULT_LANGUAGE;
        JsonNode languageNode = body.get("defaultLanguage");
        if (languageNode != null && !languageNode.isNull()) {
            String tag = languageNode.isString() ? NotificationTranslator.canonicalTag(languageNode.asString()) : null;
            if (tag == null) {
                errors.add(new FieldError("defaultLanguage", "must be a BCP 47 language tag, for example en or de"));
            } else {
                defaultLanguage = tag;
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The notification settings are not valid.", errors);
        }
        return new NotificationSettings(until, bypass, chatops, imageLinks, defaultLanguage);
    }

    /** Parses {@code chatops} (prompt 061): an object whose only member is the boolean {@code enabled}. */
    static NotificationSettings.ChatOps chatops(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return NotificationSettings.ChatOps.defaults();
        }
        if (!node.isObject()) {
            errors.add(new FieldError("chatops", "must be an object"));
            return NotificationSettings.ChatOps.defaults();
        }
        for (String field : node.propertyNames()) {
            if (!"enabled".equals(field)) {
                errors.add(new FieldError("chatops." + field, "is not a chatops member"));
            }
        }
        JsonNode enabled = node.get("enabled");
        if (enabled == null || enabled.isNull()) {
            return NotificationSettings.ChatOps.defaults();
        }
        if (!enabled.isBoolean()) {
            errors.add(new FieldError("chatops.enabled", "must be true or false"));
            return NotificationSettings.ChatOps.defaults();
        }
        return new NotificationSettings.ChatOps(enabled.asBoolean());
    }

    /** The API form of {@code value}. */
    public static Map<String, Object> body(NotificationSettings value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pausedUntil", value.pausedUntil() == null ? null : value.pausedUntil().toString());
        out.put("pauseBypassSeverity", value.pauseBypassSeverity());
        out.put("chatops", Map.of("enabled", value.chatops().on()));
        out.put("imageLinks", value.imageLinksOn());
        return out;
    }
}
