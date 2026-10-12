package ai.foresignal.settings;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code getSecuritySettings} and {@code updateSecuritySettings} (prompt 023), shared with the
 * settings import: {@code openReads} (required) and {@code exposeObserver} (default false).
 *
 * <p>The token filter and the read endpoints drop their cached copy on the
 * {@link AppSettingChangedEvent} the write publishes. Members later prompts own
 * ({@code anonymous}, {@code metricsOpen} 035, {@code exposeMlat} 069) are refused until then, so a
 * client cannot believe a protection is on that nothing enforces.
 */
@Service
public class SecuritySettingsService {

    static final Set<String> MEMBERS = Set.of("openReads", "exposeObserver");

    static final Set<String> LATER_MEMBERS = Set.of("anonymous", "metricsOpen", "exposeMlat");

    private final AppSettingService settings;

    /** Creates the service. */
    public SecuritySettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The settings in force. */
    public SecuritySettings get() {
        return settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
    }

    /**
     * Validates and stores {@code body}.
     *
     * @throws ValidationException listing every problem
     */
    public SecuritySettings update(JsonNode body) {
        SecuritySettings value = parse(body);
        settings.put(SettingKeys.SECURITY, value);
        return value;
    }

    /** Stores already validated settings (settings import). */
    public void apply(SecuritySettings value) {
        settings.put(SettingKeys.SECURITY, value);
    }

    /**
     * Parses and validates a {@code SecuritySettings} body without storing it.
     *
     * @throws ValidationException listing every problem
     */
    public static SecuritySettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a SecuritySettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String member : body.propertyNames()) {
            if (LATER_MEMBERS.contains(member)) {
                if (!body.get(member).isNull()) {
                    errors.add(new FieldError(member, "is not supported by this version yet"));
                }
            } else if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(member, "is not a SecuritySettings member"));
            }
        }
        JsonNode openReads = body.get("openReads");
        if (openReads == null || !openReads.isBoolean()) {
            errors.add(new FieldError("openReads", "is required and must be a boolean"));
        }
        JsonNode exposeObserver = body.get("exposeObserver");
        if (exposeObserver != null && !exposeObserver.isNull() && !exposeObserver.isBoolean()) {
            errors.add(new FieldError("exposeObserver", "must be a boolean"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The security settings are not valid.", errors);
        }
        boolean observe = exposeObserver != null && exposeObserver.isBoolean() && exposeObserver.asBoolean();
        return new SecuritySettings(openReads.asBoolean(), observe);
    }
}
