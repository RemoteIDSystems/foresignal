package ai.foresignal.collect;

import ai.foresignal.source.spi.RasterSink;

/** Fallbacks for a {@link DefaultFetchContext} built without optional collaborators. */
final class FetchContextDefaults {

    private FetchContextDefaults() {}

    /**
     * Throws: there is no raster pipeline to hand out.
     *
     * @throws UnsupportedOperationException always
     */
    static RasterSink noRasters(String sourceId) {
        throw new UnsupportedOperationException("No raster pipeline is available to " + sourceId + ".");
    }
}
