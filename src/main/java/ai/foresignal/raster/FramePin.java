package ai.foresignal.raster;

/**
 * Keeps raster frames that something else references out of the raster retention ladder and quota (prompt 131): a
 * chronicle captures frames by reference to their content-addressed frame key (042), so the frame must outlive the
 * product's own thinning. {@link RasterRetentionStep} skips a frame that any pin claims.
 */
public interface FramePin {

    /**
     * True when the frame must be kept.
     *
     * @param productId the product
     * @param frameKey the content-addressed frame key ({@code FrameKeys.frameKey(timeKey, sha256)})
     */
    boolean pinned(String productId, String frameKey);
}
