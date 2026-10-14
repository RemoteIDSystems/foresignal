package ai.foresignal.notify.mesh;

import java.util.Locale;

/**
 * The regulatory region of a mesh channel (prompt 084), which sets the duty-cycle ceiling the airtime governor never
 * exceeds, not even for critical alerts.
 *
 * <ul>
 *   <li>{@link #EU_868} and {@link #UK_868}: 10 %, the 869.40–869.65 MHz sub-band (ETSI EN 300 220 band P, 500 mW)
 *       that Meshtastic's {@code EU_868} region uses.
 *   <li>{@link #EU_433}: 10 %.
 *   <li>{@link #US}, {@link #ANZ} and {@link #OTHER}: no duty-cycle ceiling (US and ANZ rules limit dwell time, which
 *       the radio enforces); the governor then caps everything, critical alerts included, at the
 *       {@link #MAX_DUTY_CYCLE_PERCENT} maximum.
 * </ul>
 */
public enum MeshRegion {

    /** Europe 868 MHz, 10 %. */
    EU_868(10.0),
    /** Europe 433 MHz, 10 %. */
    EU_433(10.0),
    /** United Kingdom 868 MHz, 10 %. */
    UK_868(10.0),
    /** United States 915 MHz: no duty-cycle ceiling. */
    US(null),
    /** Australia and New Zealand 915 MHz: no duty-cycle ceiling. */
    ANZ(null),
    /** Anywhere else: no duty-cycle ceiling known to Foresignal. */
    OTHER(null);

    /** The largest {@code dutyCyclePercent} any channel may set, and the cap where a region has no ceiling. */
    public static final double MAX_DUTY_CYCLE_PERCENT = 10.0;

    private final Double ceilingPercent;

    MeshRegion(Double ceilingPercent) {
        this.ceilingPercent = ceilingPercent;
    }

    /** The regulatory duty-cycle ceiling in percent, or null when the region has none. */
    public Double ceilingPercent() {
        return ceilingPercent;
    }

    /** The ceiling the governor applies: the regulatory one, else {@link #MAX_DUTY_CYCLE_PERCENT}. */
    public double effectiveCeilingPercent() {
        return ceilingPercent == null ? MAX_DUTY_CYCLE_PERCENT : Math.min(ceilingPercent, MAX_DUTY_CYCLE_PERCENT);
    }

    /** The region named {@code value} (case-insensitive), or null when it is not one. */
    public static MeshRegion parse(Object value) {
        if (!(value instanceof String text)) {
            return null;
        }
        try {
            return valueOf(text.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
