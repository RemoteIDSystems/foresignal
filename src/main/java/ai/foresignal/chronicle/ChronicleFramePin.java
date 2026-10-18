package ai.foresignal.chronicle;

import ai.foresignal.raster.FramePin;
import org.springframework.stereotype.Component;

/**
 * Keeps the raster frames chronicles captured out of raster retention (prompt 131): a frame referenced by a
 * {@code chronicle_frame} row is never thinned or evicted by quota. Captured frames are referenced by their
 * content-addressed frame key (042); this pin plays the part of the 111 retention hold, which covers record versions
 * only.
 */
@Component
public class ChronicleFramePin implements FramePin {

    private final ChronicleStorage storage;

    /** Creates the pin. */
    public ChronicleFramePin(ChronicleStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean pinned(String productId, String frameKey) {
        return storage.framePinned(productId, frameKey);
    }
}
