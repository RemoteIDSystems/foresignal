package ai.foresignal.security.ratelimit;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code getRateLimits} and {@code updateRateLimits} (prompt 035): the {@link RateLimitSettings}
 * under {@code app_setting['ratelimits']}.
 *
 * <p>The settings are read on every request by {@link ClientRateLimitFilter}, so this service keeps
 * its own copy and drops it on the {@link AppSettingChangedEvent} a write (or a cache invalidation)
 * publishes; a change applies to the next request without a restart.
 */
@Service
public class RateLimitSettingsService {

    static final Set<String> MEMBERS =
            Set.of("read", "tiles", "stream", "expensive", "write", "anonymous", "metricsPerMinute");

    static final Set<String> STREAM_MEMBERS = Set.of("opensPerMinute", "concurrent");

    private final AppSettingService settings;
    private volatile RateLimitSettings cached;

    /** Creates the service. */
    public RateLimitSettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The settings in force: the stored value, or the defaults when none was ever stored. */
    public RateLimitSettings get() {
        RateLimitSettings value = cached;
        if (value == null) {
            value = settings.getOrDefault(SettingKeys.RATELIMITS, RateLimitSettings.class,
                    RateLimitSettings::defaults);
            cached = value;
        }
        return value;
    }

    /**
     * Validates {@code body} and replaces the stored settings; members left out take their defaults.
     *
     * @return the settings now in force
     * @throws ValidationException {@code 400 VALIDATION_FAILED} listing every field error
     */
    public RateLimitSettings replace(JsonNode body) {
        RateLimitSettings value = parse(body);
        replace(value);
        return value;
    }

    /** Stores already valid settings. */
    public void replace(RateLimitSettings value) {
        settings.put(SettingKeys.RATELIMITS, value);
        cached = null;
    }

    /** Drops the cached copy when the {@code ratelimits} setting changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.RATELIMITS.equals(event.key())) {
            cached = null;
        }
    }

    /**
     * Parses and validates a {@code RateLimitSettings} body without storing it. Every limit is an
     * integer of at least 1; {@code metricsPerMinute} may also be null (no metrics limit); unknown
     * members are refused.
     *
     * @throws ValidationException listing every problem
     */
    public static RateLimitSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED,
                    "The request body must be a RateLimitSettings object.",
                    List.of(new FieldError("body", "must be a JSON object")));
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a RateLimitSettings member"));
            }
        }
        int read = limit(body, "read", "read", errors);
        int tiles = limit(body, "tiles", "tiles", errors);
        int expensive = limit(body, "expensive", "expensive", errors);
        int write = limit(body, "write", "write", errors);
        int anonymous = limit(body, "anonymous", "anonymous", errors);
        Integer metrics = null;
        JsonNode metricsNode = body.get("metricsPerMinute");
        if (metricsNode != null && !metricsNode.isNull()) {
            int value = limit(body, "metricsPerMinute", "metricsPerMinute", errors);
            metrics = value > 0 ? value : null;
        }
        RateLimitSettings.StreamRates stream = null;
        JsonNode streamNode = body.get("stream");
        if (streamNode != null && !streamNode.isNull()) {
            if (!streamNode.isObject()) {
                errors.add(new FieldError("stream", "must be an object"));
            } else {
                for (String member : streamNode.propertyNames()) {
                    if (!STREAM_MEMBERS.contains(member)) {
                        errors.add(new FieldError("stream." + member, "is not a stream limit member"));
                    }
                }
                stream = new RateLimitSettings.StreamRates(
                        limit(streamNode, "opensPerMinute", "stream.opensPerMinute", errors),
                        limit(streamNode, "concurrent", "stream.concurrent", errors));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The rate-limit settings are not valid.",
                    errors);
        }
        return new RateLimitSettings(read, tiles, stream, expensive, write, anonymous, metrics);
    }

    /**
     * The integer member {@code name} of {@code node}: 0 (the default) when absent or null,
     * otherwise an integer of at least 1, or a field error.
     */
    private static int limit(JsonNode node, String name, String field, List<FieldError> errors) {
        JsonNode value = node.get(name);
        if (value == null || value.isNull()) {
            return 0;
        }
        if (!value.isIntegralNumber() || !value.canConvertToInt()) {
            errors.add(new FieldError(field, "must be an integer"));
            return 0;
        }
        int number = value.asInt();
        if (number < 1) {
            errors.add(new FieldError(field, "must be at least 1"));
            return 0;
        }
        return number;
    }
}
