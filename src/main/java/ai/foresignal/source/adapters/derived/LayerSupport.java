package ai.foresignal.source.adapters.derived;

import ai.foresignal.derive.Observation;

/** Small shared tests over vessel and aircraft observations (prompt 099). */
public final class LayerSupport {

    private LayerSupport() {}

    /** An integer payload field, from a number or a numeric string, or null. */
    public static Integer intField(Observation observation, String key) {
        Object value = observation.payload().get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** True when the vessel is moored or at anchor (AIS navigational status 1 or 5). */
    public static boolean mooredOrAnchored(Observation observation) {
        Integer status = intField(observation, "navStatus");
        return status != null && (status == 1 || status == 5);
    }

    /** The vessel's speed over ground in knots, or null. */
    public static Double sog(Observation observation) {
        return observation.number("sog");
    }

    /** The vessel's course over ground in degrees, or null. */
    public static Double cog(Observation observation) {
        return observation.number("cog");
    }

    /** The vessel's AIS ship type, or null. */
    public static Integer shipType(Observation observation) {
        return intField(observation, "shipType");
    }

    /** True when the aircraft reports being off the ground. */
    public static boolean airborne(Observation observation) {
        Object onGround = observation.payload().get("onGround");
        return !(onGround instanceof Boolean b && b);
    }

    /** True when the aircraft's position is ADS-B (not MLAT or TIS-B). */
    public static boolean adsbPosition(Observation observation) {
        return observation.positionSource() != ai.foresignal.source.spi.PositionSource.CALCULATED;
    }
}
