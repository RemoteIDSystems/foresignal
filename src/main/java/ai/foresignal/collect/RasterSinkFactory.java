package ai.foresignal.collect;

import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;

/**
 * Supplies the {@link RasterSink} behind {@code FetchContext.rasters()} (prompt 042).
 *
 * <p>Declared here and implemented by the raster pipeline ({@code ai.foresignal.raster}), so the
 * collector hands adapters a sink without depending on the raster package.
 */
public interface RasterSinkFactory {

    /**
     * The sink for one fetch, probe or stream of {@code descriptor}'s source.
     *
     * @param probing true for a probe: the sink writes nothing and returns previews
     */
    RasterSink forSource(SourceDescriptor descriptor, boolean probing);
}
