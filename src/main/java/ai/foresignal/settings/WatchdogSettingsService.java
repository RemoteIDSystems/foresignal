package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link WatchdogSettings} ({@code GET/PUT /v1/settings/watchdog} and the
 * settings import, prompt 019). A PUT replaces the whole value; every problem is reported in one 400.
 */
@Service
public class WatchdogSettingsService {

    static final Set<String> FIELDS = Set.of("heartbeatUrl", "failUrl", "interval", "method", "canary");
    static final Set<String> CANARY_FIELDS = Set.of("enabled", "interval");

    private final AppSettingService settings;

    /** Creates the service. */
    public WatchdogSettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The stored settings, or the defaults. */
    public WatchdogSettings get() {
        return settings.getOrDefault(SettingKeys.WATCHDOG, WatchdogSettings.class, WatchdogSettings::defaults);
    }

    /** Stores {@code value} (already validated by {@link #parse}). */
    public WatchdogSettings put(WatchdogSettings value) {
        settings.put(SettingKeys.WATCHDOG, value);
        return value;
    }

    /**
     * Parses a {@code WatchdogSettings} body.
     *
     * @throws ValidationException listing every problem
     */
    public static WatchdogSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a WatchdogSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a WatchdogSettings member"));
            }
        }
        String heartbeat = url(body, "heartbeatUrl", errors);
        String fail = url(body, "failUrl", errors);
        Duration interval = duration(body.get("interval"), "interval", WatchdogSettings.DEFAULT_INTERVAL, errors);
        if (interval != null && interval.compareTo(WatchdogSettings.MIN_INTERVAL) < 0) {
            errors.add(new FieldError("interval", "must be at least 30s"));
        }
        String method = "GET";
        JsonNode methodNode = body.get("method");
        if (methodNode != null && !methodNode.isNull()) {
            String value = methodNode.isString() ? methodNode.asString().toUpperCase(Locale.ROOT) : "";
            if (!value.equals("GET") && !value.equals("POST")) {
                errors.add(new FieldError("method", "must be GET or POST"));
            } else {
                method = value;
            }
        }
        WatchdogSettings.Canary canary = WatchdogSettings.Canary.defaults();
        JsonNode canaryNode = body.get("canary");
        if (canaryNode != null && !canaryNode.isNull()) {
            if (!canaryNode.isObject()) {
                errors.add(new FieldError("canary", "must be an object"));
            } else {
                for (String field : canaryNode.propertyNames()) {
                    if (!CANARY_FIELDS.contains(field)) {
                        errors.add(new FieldError("canary." + field, "is not a canary member"));
                    }
                }
                boolean enabled = false;
                JsonNode enabledNode = canaryNode.get("enabled");
                if (enabledNode != null && !enabledNode.isNull()) {
                    if (!enabledNode.isBoolean()) {
                        errors.add(new FieldError("canary.enabled", "must be a boolean"));
                    } else {
                        enabled = enabledNode.asBoolean();
                    }
                }
                Duration canaryInterval = duration(canaryNode.get("interval"), "canary.interval",
                        WatchdogSettings.DEFAULT_CANARY_INTERVAL, errors);
                if (canaryInterval != null && canaryInterval.compareTo(WatchdogSettings.MIN_INTERVAL) < 0) {
                    errors.add(new FieldError("canary.interval", "must be at least 30s"));
                }
                canary = new WatchdogSettings.Canary(enabled, canaryInterval);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The watchdog settings are not valid.", errors);
        }
        return new WatchdogSettings(heartbeat, fail, interval, method, canary);
    }

    private static String url(JsonNode body, String field, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(field, "must be an http or https URL or null"));
            return null;
        }
        String value = node.asString().strip();
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null) {
                errors.add(new FieldError(field, "must be an absolute http or https URL"));
                return null;
            }
        } catch (URISyntaxException e) {
            errors.add(new FieldError(field, "is not a valid URL"));
            return null;
        }
        return value;
    }

    private static Duration duration(JsonNode node, String field, Duration fallback, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a duration such as 60s or PT1M"));
            return null;
        }
        try {
            return Durations.parse(node.asString());
        } catch (InvalidInputException e) {
            errors.add(new FieldError(field, e.getMessage()));
            return null;
        }
    }
}
