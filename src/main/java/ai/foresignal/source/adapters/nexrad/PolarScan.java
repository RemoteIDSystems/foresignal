package ai.foresignal.source.adapters.nexrad;

import java.time.Instant;

/**
 * One radial scan of a NEXRAD Level III product (prompt 106), as {@link NidsRadialReader} reads it.
 *
 * @param variable the data variable ({@code BaseReflectivityDR}, {@code BaseVelocityDV})
 * @param units its units ({@code dBz}, {@code m/s}), or null
 * @param radarLat the radar latitude, degrees
 * @param radarLon the radar longitude, degrees
 * @param azimuths the azimuth of each radial, degrees clockwise from true north, in file order
 * @param gateStarts the distance to the start of each gate, meters, ascending
 * @param values the samples, row-major {@code azimuth × gate}; NaN for no data
 * @param time the scan time, or null when the file does not say
 */
public record PolarScan(String variable, String units, double radarLat, double radarLon, float[] azimuths,
        float[] gateStarts, float[] values, Instant time) {

    /** Checks the shapes. */
    public PolarScan {
        if (azimuths.length == 0 || gateStarts.length == 0 || values.length != azimuths.length * gateStarts.length) {
            throw new IllegalArgumentException("A polar scan needs azimuths x gates samples");
        }
    }

    /** The gate spacing, meters (250 m for the super-resolution products). */
    public double gateWidth() {
        return gateStarts.length > 1 ? gateStarts[1] - gateStarts[0] : 250.0;
    }

    /** The sample of radial {@code ray} at gate {@code gate}. */
    public float value(int ray, int gate) {
        return values[ray * gateStarts.length + gate];
    }
}
