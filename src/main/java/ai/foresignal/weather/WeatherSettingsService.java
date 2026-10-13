package ai.foresignal.weather;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.place.WeatherAoiProperties;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link WeatherSettings} (prompt 056; {@code GET/PUT /v1/settings/weather}, admin).
 *
 * <p>Until a value is stored, {@code defaultAoi} and {@code aoiBufferKm} come from the configuration properties
 * {@code foresignal.weather.default-aoi} and {@code foresignal.weather.aoi-buffer-km} ({@link WeatherAoiProperties})
 * and {@code alertContext} is {@code auto}. A PUT replaces the whole value (missing members take the shipped
 * defaults), reports every problem in one 400, and publishes {@link WeatherSettingsChangedEvent}, on which the places
 * AOI recomputes when its inputs changed.
 */
@Service
public class WeatherSettingsService {

    static final Set<String> FIELDS = Set.of("alertContext", "defaultAoi", "aoiBufferKm");

    private final AppSettingService settings;
    private final WeatherAoiProperties properties;
    private final ApplicationEventPublisher events;

    /** Creates the service. */
    public WeatherSettingsService(AppSettingService settings, WeatherAoiProperties properties,
            ApplicationEventPublisher events) {
        this.settings = settings;
        this.properties = properties == null ? WeatherAoiProperties.defaults() : properties;
        this.events = events;
    }

    /** The stored settings, or the configured defaults. */
    public WeatherSettings get() {
        Optional<WeatherSettings> stored;
        try {
            stored = settings.get(SettingKeys.WEATHER, WeatherSettings.class);
        } catch (RuntimeException e) {
            stored = Optional.empty();
        }
        return stored.orElseGet(() -> new WeatherSettings(WeatherSettings.AUTO, properties.defaultAoi(),
                properties.aoiBufferKm()));
    }

    /** Stores {@code value} (already validated by {@link #parse}) and publishes the change. */
    public WeatherSettings put(WeatherSettings value) {
        WeatherSettings previous = get();
        settings.put(SettingKeys.WEATHER, value);
        events.publishEvent(new WeatherSettingsChangedEvent(previous, value));
        return value;
    }

    /**
     * Parses a {@code WeatherSettings} body.
     *
     * @throws ValidationException listing every problem
     */
    public static WeatherSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a WeatherSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a WeatherSettings member"));
            }
        }
        String alertContext = null;
        JsonNode mode = body.get("alertContext");
        if (mode != null && !mode.isNull()) {
            if (!mode.isString() || !(WeatherSettings.AUTO.equals(mode.asString())
                    || WeatherSettings.OFF.equals(mode.asString()))) {
                errors.add(new FieldError("alertContext", "must be auto or off"));
            } else {
                alertContext = mode.asString();
            }
        }
        String aoi = null;
        JsonNode aoiNode = body.get("defaultAoi");
        if (aoiNode != null && !aoiNode.isNull()) {
            String problem = aoiNode.isString() ? boxProblem(aoiNode.asString()) : "must be minLon,minLat,maxLon,maxLat";
            if (problem != null) {
                errors.add(new FieldError("defaultAoi", problem));
            } else {
                aoi = aoiNode.asString().strip();
            }
        }
        Double buffer = null;
        JsonNode bufferNode = body.get("aoiBufferKm");
        if (bufferNode != null && !bufferNode.isNull()) {
            if (!bufferNode.isNumber() || bufferNode.asDouble() < 0 || bufferNode.asDouble() > WeatherSettings.MAX_BUFFER_KM
                    || Double.isNaN(bufferNode.asDouble())) {
                errors.add(new FieldError("aoiBufferKm", "must be a number from 0 to 1000"));
            } else {
                buffer = bufferNode.asDouble();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The weather settings are not valid.", errors);
        }
        return new WeatherSettings(alertContext, aoi, buffer);
    }

    /** The problem with a {@code minLon,minLat,maxLon,maxLat} box, or null when it is valid. */
    static String boxProblem(String text) {
        String[] parts = text.split(",");
        if (parts.length != 4) {
            return "must be four numbers: minLon,minLat,maxLon,maxLat";
        }
        double[] v = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                v[i] = Double.parseDouble(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            return "must be four numbers: minLon,minLat,maxLon,maxLat";
        }
        if (v[0] < -180 || v[2] > 180 || v[1] < -90 || v[3] > 90) {
            return "must lie within -180..180 and -90..90";
        }
        if (v[0] >= v[2] || v[1] >= v[3]) {
            return "minLon must be less than maxLon and minLat less than maxLat";
        }
        return null;
    }
}
