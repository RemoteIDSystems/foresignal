package ai.foresignal.raster.decode;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.nio.file.Path;

/**
 * Decodes one upstream file into a {@link DecodedGrid} (prompt 046, the decoder SPI of
 * Weather-Imagery-Design §0.1).
 *
 * <p>Implementations: {@link Grib2Decoder} and {@link NetcdfDecoder}, both on NetCDF-Java and both
 * opening files only through {@link SafeNetcdf}. Decodes are CPU- and memory-heavy and run on the
 * bounded raster decode pool (the adapter hands a decoder to {@code ctx.rasters().commit} as the
 * frame's {@code ai.foresignal.source.spi.RasterDecoder}); a header-only decode is cheap and may run
 * on the fetch thread.
 *
 * <p>A gzip input ({@code .gz}) is inflated by streaming into a sibling temporary file, which is
 * deleted before {@code decode} returns.
 */
public interface RasterDecoder {

    /** True when this decoder reads requests of {@code r}'s format. */
    boolean supports(DecodeRequest r);

    /**
     * Decodes {@code file}.
     *
     * @param file a regular file under {@code rasters/tmp} or {@code rasters/selftest}
     * @param r what to read
     * @return the decoded field (samples omitted when {@code r.headerOnly()})
     * @throws RasterFormatException when the file is unreadable or not of the expected format
     * @throws SilentFormatChangeException when the field or variable the request names is missing
     */
    DecodedGrid decode(Path file, DecodeRequest r) throws RasterFormatException, SilentFormatChangeException;
}
