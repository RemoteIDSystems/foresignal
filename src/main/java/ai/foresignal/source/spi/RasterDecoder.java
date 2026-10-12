package ai.foresignal.source.spi;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Decodes a downloaded upstream file into band arrays (Weather-Imagery-Design §2.1, the decoder
 * handle an adapter hands to {@link RasterSink#commit}).
 *
 * <p>Runs on the bounded raster decode pool, never on a virtual thread. A decoder that finds an
 * expected field or variable missing throws {@link SilentFormatChangeException}: the pipeline then
 * degrades the source's health and writes no frame, rather than an empty one. Prompt 046 supplies
 * the GRIB2 and NetCDF decoders.
 */
@FunctionalInterface
public interface RasterDecoder {

    /**
     * Decodes {@code file}.
     *
     * @param file the downloaded file; the pipeline deletes it afterwards
     * @return the decoded bands
     * @throws IOException when the file cannot be read
     * @throws SilentFormatChangeException when an expected field or variable is missing
     */
    DecodedBands decode(Path file) throws IOException;
}
