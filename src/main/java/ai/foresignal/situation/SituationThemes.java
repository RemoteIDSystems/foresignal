package ai.foresignal.situation;

import java.util.List;

/**
 * The fixed list of situation themes (prompt 118) and their compatibility rule: a theme is compatible with itself
 * and with {@code mixed}, and an input whose theme is unknown is compatible with every situation.
 */
public final class SituationThemes {

    /** Shipping, ports, chokepoints and navigation. */
    public static final String MARITIME = "maritime";

    /** Aircraft, airspace and airports. */
    public static final String AVIATION = "aviation";

    /** Armed conflict and unrest. */
    public static final String CONFLICT = "conflict";

    /** Power, telecoms, internet and other infrastructure. */
    public static final String INFRASTRUCTURE = "infrastructure";

    /** Weather and natural hazards. */
    public static final String WEATHER = "weather";

    /** Cyber security. */
    public static final String CYBER = "cyber";

    /** Markets and aggregate positioning. */
    public static final String MARKETS = "markets";

    /** Public health. */
    public static final String HEALTH = "health";

    /** Space and space weather. */
    public static final String SPACE = "space";

    /** More than one theme; compatible with every theme. */
    public static final String MIXED = "mixed";

    /** Every theme, in the spec's order. */
    public static final List<String> ALL = List.of(MARITIME, AVIATION, CONFLICT, INFRASTRUCTURE, WEATHER, CYBER,
            MARKETS, HEALTH, SPACE, MIXED);

    private SituationThemes() {}

    /** True when {@code theme} is one of {@link #ALL}. */
    public static boolean valid(String theme) {
        return theme != null && ALL.contains(theme);
    }

    /**
     * True when an input of {@code inputTheme} may join a situation of {@code situationTheme} without the theme
     * penalty: equal themes, either one {@code mixed}, or an unknown input theme.
     */
    public static boolean compatible(String inputTheme, String situationTheme) {
        if (inputTheme == null || situationTheme == null) {
            return true;
        }
        return inputTheme.equals(situationTheme) || MIXED.equals(inputTheme) || MIXED.equals(situationTheme);
    }
}
