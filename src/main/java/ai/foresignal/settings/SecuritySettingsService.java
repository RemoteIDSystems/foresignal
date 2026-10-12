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
 * settings import: {@code openReads} (required), {@code exposeObserver} (default false), and from
 * prompt 035 the {@code anonymous} restriction profile and {@code metricsOpen} (default true).
 *
 * <p>The token filter and the read endpoints drop their cached copy on the
 * {@link AppSettingChangedEvent} the write publishes. {@code exposeMlat} (069) is refused until then,
 * so a client cannot believe a protection is on that nothing enforces. The anonymous profile is
 * validated like a read token's restriction; {@code update} also checks that its sources and kinds
 * exist and its endpoint patterns match {@code read} operations.
 */
@Service
public class SecuritySettingsService {

    static final Set<String> MEMBERS = Set.of("openReads", "exposeObserver", "anonymous", "metricsOpen");

    static final Set<String> LATER_MEMBERS = Set.of("exposeMlat");

    private final AppSettingService settings;
    private final ai.foresignal.security.RestrictionValidator restrictions;

    /** Creates the service. */
    public SecuritySettingsService(AppSettingService settings,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.security.RestrictionValidator> restrictions) {
        this.settings = settings;
        this.restrictions = restrictions.getIfAvailable(ai.foresignal.security.RestrictionValidator::structural);
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
        SecuritySettings value = parse(body, restrictions);
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
        return parse(body, ai.foresignal.security.RestrictionValidator.structural());
    }

    /**
     * {@link #parse(JsonNode)} with {@code validator} checking the anonymous profile's sources,
     * kinds and endpoints.
     *
     * @throws ValidationException listing every problem
     */
    public static SecuritySettings parse(JsonNode body, ai.foresignal.security.RestrictionValidator validator) {
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
        JsonNode metricsOpen = body.get("metricsOpen");
        if (metricsOpen != null && !metricsOpen.isNull() && !metricsOpen.isBoolean()) {
            errors.add(new FieldError("metricsOpen", "must be a boolean"));
        }
        ai.foresignal.security.TokenRestrictions anonymous = validator.parse(body.get("anonymous"),
                ai.foresignal.security.Scope.READ, "anonymous", errors);
        if (anonymous.excludePrivate() != null && !anonymous.excludePrivate()) {
            errors.add(new FieldError("anonymous.excludePrivate", "private sources are never served anonymously"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The security settings are not valid.", errors);
        }
        boolean observe = exposeObserver != null && exposeObserver.isBoolean() && exposeObserver.asBoolean();
        Boolean metrics = metricsOpen == null || metricsOpen.isNull() ? null : metricsOpen.asBoolean();
        return new SecuritySettings(openReads.asBoolean(), observe, anonymous.orNull(), metrics);
    }
}
