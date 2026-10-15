package ai.foresignal.intel;

import java.util.Set;

/** The intelligence event types (prompt 101). */
public final class EventTypes {

    public static final String MARITIME_DISRUPTION = "maritime-disruption";
    public static final String AVIATION_ANOMALY = "aviation-anomaly";
    public static final String INFRASTRUCTURE_DISRUPTION = "infrastructure-disruption";
    public static final String WEATHER_IMPACT = "weather-impact";
    public static final String SEISMIC = "seismic";
    public static final String CUSTOM = "custom";

    /** Every type. */
    public static final Set<String> ALL = Set.of(MARITIME_DISRUPTION, AVIATION_ANOMALY, INFRASTRUCTURE_DISRUPTION,
            WEATHER_IMPACT, SEISMIC, CUSTOM);

    private EventTypes() {}
}
