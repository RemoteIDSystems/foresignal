package ai.foresignal.raster;

import java.time.Instant;
import java.util.Map;

/**
 * One {@code raster_frame} row as the raster code reads it.
 *
 * @param productId the product
 * @param frameId the immutable frame id
 * @param validTime the valid time
 * @param referenceTime the forecast run, or null
 * @param forecastStep the forecast step in hours, or null
 * @param obsStart start of the observation period, or null
 * @param obsEnd end of the observation period, or null
 * @param gridId the grid
 * @param path the file path relative to {@code rasters/}
 * @param bytes the file's size
 * @param sha256 hex SHA-256 of the decoded content
 * @param dtype the stored sample type
 * @param codec the stored codec
 * @param stats statistics in physical units, or null
 * @param upstreamKey the upstream object key, or null
 * @param status {@value #PARTIAL} or {@value #COMPLETE}
 * @param thinned true when kept by ladder thinning
 * @param runComplete true when the frame's forecast run is complete
 * @param ingestedAt when the frame was stored
 * @param expiresAt when retention removes the frame, or null
 */
public record RasterFrameRow(
        String productId,
        String frameId,
        Instant validTime,
        Instant referenceTime,
        Integer forecastStep,
        Instant obsStart,
        Instant obsEnd,
        String gridId,
        String path,
        long bytes,
        String sha256,
        String dtype,
        int codec,
        Map<String, Object> stats,
        String upstreamKey,
        String status,
        boolean thinned,
        boolean runComplete,
        Instant ingestedAt,
        Instant expiresAt) {

    /** A frame still filling. */
    public static final String PARTIAL = "partial";

    /** A finished frame. */
    public static final String COMPLETE = "complete";

    /** True for a complete frame. */
    public boolean complete() {
        return COMPLETE.equals(status);
    }

    /** True for a forecast step. */
    public boolean forecast() {
        return referenceTime != null;
    }

    /** The path form of the valid time. */
    public String timeKey() {
        return FrameKeys.timeKey(validTime);
    }

    /** {@code {timeKey}~{sha8}}. */
    public String frameKey() {
        return FrameKeys.frameKey(timeKey(), sha256);
    }

    /** The first 8 hex characters of the content hash. */
    public String sha8() {
        return FrameKeys.sha8(sha256);
    }
}
