package ai.foresignal.testsupport.weather;

import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RasterSink;
import java.time.Instant;
import java.util.List;

/**
 * A {@link RasterSink} for the prompt 048 adapter tests: no frames, only {@link #flashes()}, backed by a
 * real {@link FlashStore} (its probing view when the sink acts as a probe's).
 */
public final class FlashTestSink implements RasterSink {

    private final FlashStore store;
    private final boolean probing;

    /**
     * A sink over {@code store}.
     *
     * @param probing true to act as a probe's sink
     */
    public FlashTestSink(FlashStore store, boolean probing) {
        this.store = store;
        this.probing = probing;
    }

    /** The store behind the sink. */
    public FlashStore store() {
        return store;
    }

    @Override
    public NormalizedRecord commit(FrameInput frame) {
        throw new UnsupportedOperationException("The flash test sink stores no frames");
    }

    @Override
    public List<NormalizedRecord> completeRun(String productId, Instant referenceTime) {
        return List.of();
    }

    @Override
    public void reportFormatChange(String productId, String reason) {
        throw new UnsupportedOperationException("The GLM and FDC adapters report format changes through their state");
    }

    @Override
    public boolean probing() {
        return probing;
    }

    @Override
    public FlashSink flashes() {
        return probing ? store.probingView() : store;
    }
}
