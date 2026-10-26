package ai.foresignal.egress;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * {@code getEgressSettings} and {@code updateEgressSettings} (prompt R2-018) over the settings store, key
 * {@code egress}. A PUT replaces the policy; every problem is one 400.
 */
@Service
public class EgressSettingsService {

    private final AppSettingService settings;
    private final EgressRouteStore routes;

    /** Creates the service. */
    public EgressSettingsService(AppSettingService settings, EgressRouteStore routes) {
        this.settings = settings;
        this.routes = routes;
    }

    /** The policy, or the shipped default ({@code direct}). */
    public EgressSettings get() {
        return settings.getOrDefault(SettingKeys.EGRESS, EgressSettings.class, EgressSettings::defaults);
    }

    /**
     * Replaces the policy with an {@code EgressSettings} body.
     *
     * @throws ValidationException listing every bad member; {@code defaultRoute} must name an existing route
     */
    public EgressSettings update(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an EgressSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!name.equals("rotation") && !name.equals("defaultRoute")) {
                errors.add(new FieldError(name, "is not a member of EgressSettings"));
            }
        }
        String rotation = null;
        JsonNode rotationNode = body.get("rotation");
        if (rotationNode == null || rotationNode.isNull()) {
            errors.add(new FieldError("rotation", "is required"));
        } else if (!rotationNode.isString() || !EgressSettings.ROTATIONS.contains(rotationNode.asString())) {
            errors.add(new FieldError("rotation", "must be one of " + String.join(", ", EgressSettings.ROTATIONS)));
        } else {
            rotation = rotationNode.asString();
        }
        UUID defaultRoute = null;
        JsonNode routeNode = body.get("defaultRoute");
        if (routeNode != null && !routeNode.isNull()) {
            try {
                defaultRoute = UUID.fromString(routeNode.asString());
                if (routes.find(defaultRoute).isEmpty()) {
                    errors.add(new FieldError("defaultRoute", "names no egress route"));
                }
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                errors.add(new FieldError("defaultRoute", "must be an egress route id (a UUID)"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The egress settings are not valid.", errors);
        }
        EgressSettings updated = new EgressSettings(rotation, defaultRoute);
        settings.put(SettingKeys.EGRESS, updated);
        return updated;
    }

    /** Clears {@code defaultRoute} when it names {@code routeId} (the route was deleted). */
    public void forget(UUID routeId) {
        EgressSettings current = get();
        if (routeId.equals(current.defaultRoute())) {
            settings.put(SettingKeys.EGRESS, new EgressSettings(current.rotation(), null));
        }
    }

    /** The API view ({@code EgressSettings}). */
    public static Map<String, Object> view(EgressSettings value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("rotation", value.rotation());
        out.put("defaultRoute", value.defaultRoute());
        return out;
    }
}
