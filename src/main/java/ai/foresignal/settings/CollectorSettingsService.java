package ai.foresignal.settings;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link CollectorSettings} ({@code GET/PUT /v1/settings/collector}, prompt 150). A
 * PUT replaces the whole value; a {@code maxConcurrentPulls} outside {@value CollectorSettings#MIN_PULLS} to
 * {@value CollectorSettings#MAX_PULLS} is clamped, anything that is not an integer is a 400
 * {@code VALIDATION_FAILED}. The pull governor resizes on the {@link AppSettingChangedEvent} that the store publishes
 * after commit.
 */
@Service
public class CollectorSettingsService {

    private final AppSettingService settings;

    /** Creates the service. */
    public CollectorSettingsService(AppSettingService settings) {
        this.settings = settings;
    }

    /** The stored settings, or the defaults. */
    public CollectorSettings get() {
        return settings.getOrDefault(SettingKeys.COLLECTOR, CollectorSettings.class, CollectorSettings::defaults);
    }

    /**
     * Validates and stores {@code body}, replacing the settings.
     *
     * @throws ValidationException listing every problem
     */
    public CollectorSettings update(JsonNode body) {
        CollectorSettings value = parse(body);
        settings.put(SettingKeys.COLLECTOR, value);
        return value;
    }

    /**
     * Parses a {@code CollectorSettings} body: {@code maxConcurrentPulls} an integer (clamped), absent meaning the
     * default; no other members.
     *
     * @throws ValidationException listing every problem
     */
    public static CollectorSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a CollectorSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!field.equals("maxConcurrentPulls")) {
                errors.add(new FieldError(field, "is not a CollectorSettings member"));
            }
        }
        int pulls = CollectorSettings.DEFAULT_PULLS;
        JsonNode node = body.get("maxConcurrentPulls");
        if (node != null && !node.isNull()) {
            if (!node.isIntegralNumber() || !node.canConvertToInt()) {
                errors.add(new FieldError("maxConcurrentPulls", "must be an integer from "
                        + CollectorSettings.MIN_PULLS + " to " + CollectorSettings.MAX_PULLS));
            } else {
                pulls = node.intValue();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The collector settings are not valid.", errors);
        }
        return new CollectorSettings(pulls);
    }
}
