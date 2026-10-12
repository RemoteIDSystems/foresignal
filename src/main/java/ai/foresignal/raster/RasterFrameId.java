package ai.foresignal.raster;

import java.io.Serializable;
import java.util.Objects;

/**
 * The key of {@code raster_frame}: (product id, frame id).
 *
 * <p>Mutable fields and a no-argument constructor because JPA's {@code @IdClass} requires them.
 */
public class RasterFrameId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;
    private String frameId;

    /** For JPA. */
    public RasterFrameId() {}

    /** A key. */
    public RasterFrameId(String productId, String frameId) {
        this.productId = productId;
        this.frameId = frameId;
    }

    /** The product. */
    public String getProductId() {
        return productId;
    }

    /** The frame. */
    public String getFrameId() {
        return frameId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RasterFrameId id && Objects.equals(productId, id.productId)
                && Objects.equals(frameId, id.frameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, frameId);
    }
}
