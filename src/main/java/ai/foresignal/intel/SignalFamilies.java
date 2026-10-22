package ai.foresignal.intel;

import java.util.Set;

/**
 * The signal families a member contributes (prompt 101). Since prompt 119 the shared registry is
 * {@code intel/signal-families.json} ({@link ai.foresignal.analytics.family.SignalFamilyRegistry}); these stored
 * names resolve through it: {@code aviation-behaviour} and {@code aviation-restriction} are aliases of
 * {@code aviation-ops}, {@code weather-hazard} of {@code weather-severe} and {@code grid-frequency} of {@code outage};
 * the rest are registry families themselves. Stored rows and the recipe JSON keep these names.
 */
public final class SignalFamilies {

    public static final String AIS_BEHAVIOR = "ais-behaviour";
    public static final String NAV_WARNING = "nav-warning";
    public static final String AVIATION_BEHAVIOR = "aviation-behaviour";
    public static final String AVIATION_RESTRICTION = "aviation-restriction";
    public static final String GNSS_INTERFERENCE = "gnss-interference";
    public static final String WEATHER_HAZARD = "weather-hazard";
    public static final String INTERNET_MEASUREMENT = "internet-measurement";
    public static final String GRID_FREQUENCY = "grid-frequency";
    public static final String SEISMIC = "seismic";
    public static final String FIRE = "fire";
    public static final String CONFLICT = "conflict";

    /** Every family name. */
    public static final Set<String> ALL = Set.of(AIS_BEHAVIOR, NAV_WARNING, AVIATION_BEHAVIOR,
            AVIATION_RESTRICTION, GNSS_INTERFERENCE, WEATHER_HAZARD, INTERNET_MEASUREMENT, GRID_FREQUENCY, SEISMIC, FIRE,
            CONFLICT);

    /**
     * The family a record kind contributes when no recipe clause names one (prompt 101). The correlation
     * engine and manual/agent members both use this, so a member's family is always derived by the
     * server, never taken from a client or a model.
     */
    public static String forKind(String kind) {
        if (kind == null) {
            return CONFLICT;
        }
        return switch (kind) {
            case "ais-gap", "loiter", "rendezvous", "chokepoint-uturn", "chokepoint-activity" -> AIS_BEHAVIOR;
            case "emergency-squawk", "holding-pattern" -> AVIATION_BEHAVIOR;
            case "jamming-cell" -> GNSS_INTERFERENCE;
            case "outage", "internet-outage", "bgp-prefix-state" -> INTERNET_MEASUREMENT;
            case "grid-frequency" -> GRID_FREQUENCY;
            case "earthquake", "shakemap-mmi", "earthquake-impact" -> SEISMIC;
            case "fire-detection", "fire-cluster" -> FIRE;
            case "chokepoint-transit", "nav-warning" -> NAV_WARNING;
            case "sigmets", "tfr", "ground-stop", "delay" -> AVIATION_RESTRICTION;
            default -> CONFLICT;
        };
    }

    private SignalFamilies() {}
}
