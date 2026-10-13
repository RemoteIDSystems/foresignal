/*
 * Unit conversions ported in part from God's Eye View, src/data/adsbLolFallback.js (FOOT_TO_M,
 * KNOT_TO_MPS, FPM_TO_MPS, emitterCategory), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; meters rounded to 1 m, speeds and angles to 0.1; the emitter category table runs
 * from OpenSky's integer to the ADS-B code (the reverse of GEV's) and adds C1–C5.
 */
package ai.foresignal.source.formats.readsb;

import java.util.Map;

/**
 * The payload keys every aircraft source emits (readsb, adsb.lol, OpenSky; prompt 062) and the unit
 * conversions that put them in one unit system.
 *
 * <p>Vertical values are in meters and never in a geometry's Z (Adapter Guide §5); speeds are knots
 * ({@code gs}, the aviation unit the motion spec declares) and vertical rates are m/s. Values are rounded
 * here so an unchanged reading hashes the same however the upstream formatted it: altitudes to 1 m,
 * speeds, tracks and rates to 0.1.
 */
public final class AircraftFields {

    /** The record kind of every aircraft source. */
    public static final String KIND = "aircraft";

    /** ICAO 24-bit address, lower case; a leading {@code ~} marks a non-ICAO address (readsb). */
    public static final String HEX = "hex";
    /** Callsign, trimmed. */
    public static final String FLIGHT = "flight";
    /** Registration. */
    public static final String REGISTRATION = "r";
    /** ICAO type designator. */
    public static final String TYPE = "t";
    /** Type description. */
    public static final String DESCRIPTION = "desc";
    /** Mode A code. */
    public static final String SQUAWK = "squawk";
    /** ADS-B emergency status; omitted when {@code none}. */
    public static final String EMERGENCY = "emergency";
    /** ADS-B emitter category ({@code A1}…{@code C5}). */
    public static final String CATEGORY = "category";
    /** True on the ground. */
    public static final String ON_GROUND = "onGround";
    /** Barometric (pressure) altitude in meters; omitted on the ground. */
    public static final String ALT_BARO = "alt_baro";
    /** Geometric (WGS84 ellipsoid) altitude in meters. */
    public static final String ALT_GEOM = "alt_geom";
    /** Ground speed in knots. */
    public static final String GROUND_SPEED = "gs";
    /** True track over ground in degrees. */
    public static final String TRACK = "track";
    /** Barometric vertical rate in m/s. */
    public static final String BARO_RATE = "baro_rate";
    /** How the position was obtained ({@code adsb|adsr|mlat|tisb|modes|other}, or OpenSky's {@code adsb|asterix|mlat|flarm}). */
    public static final String POS_SOURCE = "posSource";
    /** Navigation accuracy category for position. */
    public static final String NAC_P = "nac_p";
    /** Navigation integrity category. */
    public static final String NIC = "nic";
    /** True when {@code nac_p < 8 || nic < 7} (adapter rule 12: a quality transition as a significant field). */
    public static final String NAV_DEGRADED = "navDegraded";
    /** Military, from readsb {@code dbFlags} bit 1. */
    public static final String MILITARY = "mil";
    /** Privacy ICAO address, from {@code dbFlags} bit 4. */
    public static final String PIA = "pia";
    /** FAA LADD list, from {@code dbFlags} bit 8. */
    public static final String LADD = "ladd";
    /** OpenSky only: the country of registration inferred from the address. */
    public static final String ORIGIN_COUNTRY = "originCountry";

    /** Feet to meters, exact. */
    public static final double FOOT_TO_METER = 0.3048;
    /** Feet per minute to meters per second. */
    public static final double FPM_TO_MPS = 0.00508;
    /** Knots to meters per second. */
    public static final double KNOT_TO_MPS = 0.514444;

    /** The {@code nac_p} below which navigation is degraded. */
    public static final int NAC_P_OK = 8;
    /** The {@code nic} below which navigation is degraded. */
    public static final int NIC_OK = 7;

    /** OpenSky's integer category to the ADS-B emitter category; 0, 1 and 13 have none. */
    static final Map<Integer, String> OPENSKY_CATEGORIES = Map.ofEntries(
            Map.entry(2, "A1"), Map.entry(3, "A2"), Map.entry(4, "A3"), Map.entry(5, "A4"),
            Map.entry(6, "A5"), Map.entry(7, "A6"), Map.entry(8, "A7"),
            Map.entry(9, "B1"), Map.entry(10, "B2"), Map.entry(11, "B3"), Map.entry(12, "B4"),
            Map.entry(14, "B6"), Map.entry(15, "B7"),
            Map.entry(16, "C1"), Map.entry(17, "C2"), Map.entry(18, "C3"), Map.entry(19, "C4"), Map.entry(20, "C5"));

    private AircraftFields() {}

    /** Feet to whole meters. */
    public static int feetToMeters(double feet) {
        return (int) Math.round(feet * FOOT_TO_METER);
    }

    /** Meters rounded to whole meters. */
    public static int wholeMeters(double meters) {
        return (int) Math.round(meters);
    }

    /** Feet per minute to m/s, rounded to 0.1. */
    public static double fpmToMps(double feetPerMinute) {
        return tenths(feetPerMinute * FPM_TO_MPS);
    }

    /** m/s to knots, rounded to 0.1. */
    public static double mpsToKnots(double metersPerSecond) {
        return tenths(metersPerSecond / KNOT_TO_MPS);
    }

    /** {@code value} rounded to one decimal. */
    public static double tenths(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        // Avoid -0.0, which hashes differently from 0.0.
        return rounded == 0.0 ? 0.0 : rounded;
    }

    /** The ADS-B emitter category of an OpenSky category integer, or null for 0, 1, 13 and unknown values. */
    public static String openSkyCategory(int value) {
        return OPENSKY_CATEGORIES.get(value);
    }

    /**
     * {@link #NAV_DEGRADED}: true when either value is below its threshold; null when both are absent.
     *
     * @param nacP {@code nac_p}, or null
     * @param nic {@code nic}, or null
     */
    public static Boolean navDegraded(Integer nacP, Integer nic) {
        if (nacP == null && nic == null) {
            return null;
        }
        return (nacP != null && nacP < NAC_P_OK) || (nic != null && nic < NIC_OK);
    }
}
