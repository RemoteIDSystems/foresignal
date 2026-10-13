/*
 * The transit modes and the per-operator route-mode rules (MBTA, Entur, HSL and Metro Transit route ids) are ported
 * from God's Eye View, src/data/transitFeeds.js (TRANSIT_MODES, mbtaRouteMode, enturRouteMode, hslRouteMode,
 * metroTransitRouteMode, transitModeFor), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; each rule is a UnaryOperator over the route id returning a mode or "unknown"; the fallback to the
 * feed's default mode is TransitFeed.modeFor.
 */
package ai.foresignal.source.adapters.transit;

import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * The transit modes a {@code transit-vehicle} record carries in {@code mode}, and the route-id rules of the reviewed
 * operators whose route ids name the mode.
 */
public final class TransitModes {

    /** A bus. */
    public static final String BUS = "bus";

    /** A tram or light rail vehicle. */
    public static final String TRAM = "tram";

    /** A metro or subway train. */
    public static final String SUBWAY = "subway";

    /** A heavy or commuter rail train. */
    public static final String RAIL = "rail";

    /** A ferry. */
    public static final String FERRY = "ferry";

    /** The route id does not tell, and the feed has no default. */
    public static final String UNKNOWN = "unknown";

    /** Every mode, in GEV's order. */
    public static final List<String> MODES = List.of(BUS, TRAM, SUBWAY, RAIL, FERRY, UNKNOWN);

    private static final Pattern MBTA_SUBWAY = Pattern.compile("^(Red|Orange|Blue)\\b.*");
    private static final Pattern MBTA_TRAM = Pattern.compile("^(Green|Mattapan).*");
    private static final Pattern HSL_SUBWAY = Pattern.compile("^31M.*");
    private static final Pattern HSL_TRAM = Pattern.compile("^10(0[1-9]|10|15).*");
    private static final Pattern HSL_FERRY = Pattern.compile("^1019.*");
    private static final Pattern HSL_RAIL = Pattern.compile("^300[0-9A-Z].*");
    private static final Set<String> ENTUR_RAIL_CODESPACES = Set.of("VYG", "GJB", "SJN", "FLT", "GOA", "NSB", "VYT",
            "FLB");

    private TransitModes() {}

    /**
     * MBTA route ids are mode-typed: rapid transit lines are colors, commuter rail is {@code CR-}, ferries
     * {@code Boat-}, everything else is a bus.
     */
    public static final UnaryOperator<String> MBTA = routeId -> {
        if (routeId == null) {
            return UNKNOWN;
        }
        if (MBTA_SUBWAY.matcher(routeId).matches()) {
            return SUBWAY;
        }
        if (MBTA_TRAM.matcher(routeId).matches()) {
            return TRAM;
        }
        if (routeId.startsWith("CR-")) {
            return RAIL;
        }
        if (routeId.startsWith("Boat-")) {
            return FERRY;
        }
        return BUS;
    };

    /** Entur route ids are {@code <codespace>:Line:<id>}; a few codespaces are rail-only operators. */
    public static final UnaryOperator<String> ENTUR = routeId -> {
        if (routeId == null) {
            return UNKNOWN;
        }
        String codespace = routeId.split(":", -1)[0];
        return ENTUR_RAIL_CODESPACES.contains(codespace) ? RAIL : BUS;
    };

    /** HSL route ids: {@code 31M…} metro, {@code 1001–1010, 1015} trams, {@code 1019} the ferry, {@code 300x} rail. */
    public static final UnaryOperator<String> HSL = routeId -> {
        if (routeId == null) {
            return UNKNOWN;
        }
        if (HSL_SUBWAY.matcher(routeId).matches()) {
            return SUBWAY;
        }
        if (HSL_TRAM.matcher(routeId).matches()) {
            return TRAM;
        }
        if (HSL_FERRY.matcher(routeId).matches()) {
            return FERRY;
        }
        if (HSL_RAIL.matcher(routeId).matches()) {
            return RAIL;
        }
        return BUS;
    };

    /** Metro Transit: routes 901 and 902 are light rail, 888 is Northstar commuter rail. */
    public static final UnaryOperator<String> METRO_TRANSIT = routeId -> {
        if (routeId == null) {
            return UNKNOWN;
        }
        if (routeId.equals("901") || routeId.equals("902")) {
            return TRAM;
        }
        return routeId.equals("888") ? RAIL : BUS;
    };
}
