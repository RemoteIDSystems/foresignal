package ai.foresignal.api;

import ai.foresignal.weather.WeatherSettings;
import ai.foresignal.weather.WeatherSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * {@code getWeatherSettings} and {@code updateWeatherSettings} (prompt 056): alert weather context, the default
 * AOI and the AOI buffer. Admin only ({@code /v1/settings/**}). A PUT that changes {@code defaultAoi} or
 * {@code aoiBufferKm} recomputes the places AOI.
 */
@RestController
@Tag(name = "Settings")
public class WeatherSettingsController {

    private final WeatherSettingsService weather;

    /** Creates the controller. */
    public WeatherSettingsController(WeatherSettingsService weather) {
        this.weather = weather;
    }

    /** {@code getWeatherSettings}. */
    @GetMapping("/v1/settings/weather")
    @Operation(operationId = "getWeatherSettings", tags = "Settings")
    public WeatherSettings getWeatherSettings() {
        return weather.get();
    }

    /** {@code updateWeatherSettings}: replaces the settings. */
    @PutMapping("/v1/settings/weather")
    @Operation(operationId = "updateWeatherSettings", tags = "Settings")
    public WeatherSettings updateWeatherSettings(@RequestBody JsonNode body) {
        return weather.put(WeatherSettingsService.parse(body));
    }
}
