package ai.foresignal.inbound.formats.location;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One position fix read from a tracker app, before validation (prompt 039). Mutable and confined to
 * one parse call; {@link LocationRecords#add} validates it and turns it into a {@code tracker} record.
 *
 * <p>Units are already normalized: speed in m/s, course in degrees, altitude and accuracy in meters,
 * battery in percent.
 */
final class LocationFix {

    /** The entity id, or null when none could be determined. */
    String externalId;

    /** When the device took the fix, or null. */
    Instant observedAt;

    /** Latitude in degrees, or null. */
    Double lat;

    /** Longitude in degrees, or null. */
    Double lon;

    /** Horizontal accuracy in meters, or null. */
    Double accuracyMeters;

    /** Altitude in meters, or null. */
    Double altitudeM;

    /** Speed in m/s, or null. */
    Double speedMs;

    /** Course over ground in degrees, or null. */
    Double courseDeg;

    /** Battery level in percent, or null. */
    Double battery;

    /** True while charging (or full on power), or null when unknown. */
    Boolean charging;

    /** Motion activities (for example {@code walking}, {@code stationary}), or null. */
    List<String> motion;

    /** Further payload members kept as sent (connectivity, tracker id, event), never null. */
    final Map<String, Object> extra = new LinkedHashMap<>();
}
