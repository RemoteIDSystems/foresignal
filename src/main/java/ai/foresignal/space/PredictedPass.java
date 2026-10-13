package ai.foresignal.space;

import java.time.Instant;
import java.util.List;

/**
 * One predicted pass of a satellite over an {@link Observer} (prompt 064), above the minimum elevation the
 * prediction was asked for.
 *
 * @param aos acquisition: the elevation rises through the minimum
 * @param tca the time of maximum elevation
 * @param los loss: the elevation falls through the minimum
 * @param maxElevationDeg the maximum elevation, degrees
 * @param aosAzimuthDeg the azimuth at AOS, degrees clockwise from north
 * @param losAzimuthDeg the azimuth at LOS
 * @param groundTrack at most {@value PassPredictor#MAX_TRACK_POINTS} sub-satellite points
 *     {@code {lon, lat}} from AOS to LOS, both included
 */
public record PredictedPass(Instant aos, Instant tca, Instant los, double maxElevationDeg, double aosAzimuthDeg,
        double losAzimuthDeg, List<double[]> groundTrack) {

    /** Copies the track. */
    public PredictedPass {
        groundTrack = groundTrack == null ? List.of() : List.copyOf(groundTrack);
    }
}
