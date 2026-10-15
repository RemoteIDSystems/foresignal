package ai.foresignal.intel;

import java.util.Set;

/**
 * The signal families a member contributes (prompt 101). Prompt 119's {@code
 * intel/signal-families.json} becomes the shared registry and reconciles these strings; the recipe
 * JSON uses these names today.
 */
public final class SignalFamilies {

    public static final String AIS_BEHAVIOUR = "ais-behaviour";
    public static final String NAV_WARNING = "nav-warning";
    public static final String AVIATION_BEHAVIOUR = "aviation-behaviour";
    public static final String AVIATION_RESTRICTION = "aviation-restriction";
    public static final String GNSS_INTERFERENCE = "gnss-interference";
    public static final String WEATHER_HAZARD = "weather-hazard";
    public static final String INTERNET_MEASUREMENT = "internet-measurement";
    public static final String GRID_FREQUENCY = "grid-frequency";
    public static final String SEISMIC = "seismic";
    public static final String FIRE = "fire";
    public static final String CONFLICT = "conflict";

    /** Every family name. */
    public static final Set<String> ALL = Set.of(AIS_BEHAVIOUR, NAV_WARNING, AVIATION_BEHAVIOUR,
            AVIATION_RESTRICTION, GNSS_INTERFERENCE, WEATHER_HAZARD, INTERNET_MEASUREMENT, GRID_FREQUENCY, SEISMIC, FIRE,
            CONFLICT);

    private SignalFamilies() {}
}
