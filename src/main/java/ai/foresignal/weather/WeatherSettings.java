package ai.foresignal.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The instance weather settings (prompt 056; OpenAPI {@code WeatherSettings}), stored under
 * {@link ai.foresignal.settings.SettingKeys#WEATHER} and served by {@code GET/PUT /v1/settings/weather}.
 *
 * @param alertContext {@value #AUTO} (compute {@code context.weather} for geolocated alerts) or {@value #OFF}
 * @param defaultAoi the area of interest as {@code minLon,minLat,maxLon,maxLat} while no places exist (047)
 * @param aoiBufferKm the minimum buffer around each place in the places AOI (036), 0 to 1000 km
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherSettings(String alertContext, String defaultAoi, Double aoiBufferKm) {

    /** {@code auto}. */
    public static final String AUTO = "auto";

    /** {@code off}. */
    public static final String OFF = "off";

    /** The shipped default AOI (the Texas box). */
    public static final String DEFAULT_AOI = "-106.65,25.84,-93.51,36.50";

    /** The shipped buffer. */
    public static final double DEFAULT_BUFFER_KM = 150;

    /** The largest buffer. */
    public static final double MAX_BUFFER_KM = 1000;

    /** Fills missing members with the shipped defaults. */
    public WeatherSettings {
        alertContext = alertContext == null || alertContext.isBlank() ? AUTO : alertContext;
        defaultAoi = defaultAoi == null || defaultAoi.isBlank() ? DEFAULT_AOI : defaultAoi.strip();
        aoiBufferKm = aoiBufferKm == null ? DEFAULT_BUFFER_KM : aoiBufferKm;
    }

    /** The shipped defaults. */
    public static WeatherSettings defaults() {
        return new WeatherSettings(null, null, null);
    }

    /** True when alert weather context is on. */
    public boolean alertContextOn() {
        return !OFF.equals(alertContext);
    }

    /** The default AOI as {@code [minLon, minLat, maxLon, maxLat]}. */
    public double[] defaultBox() {
        String[] parts = defaultAoi.split(",");
        double[] out = new double[4];
        for (int i = 0; i < 4; i++) {
            out[i] = Double.parseDouble(parts[i].strip());
        }
        return out;
    }
}
