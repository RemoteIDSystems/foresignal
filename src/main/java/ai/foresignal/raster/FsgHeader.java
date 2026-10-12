package ai.foresignal.raster;

import ai.foresignal.source.spi.RasterDtype;
import java.util.Objects;

/**
 * The 64-byte header of a {@code .fsg} frame file, minus the fields {@link ElgCodec} computes
 * (magic, version, payload length, CRC32C and the reserved words).
 *
 * @param dtype the sample type
 * @param codec {@value #CODEC_NONE} (none) or {@value #CODEC_DEFLATE} (deflate)
 * @param nx columns
 * @param ny rows
 * @param bands bands, at least 1
 * @param partial flags bit 0: the frame is incomplete
 * @param scale code-to-physical scale
 * @param offset code-to-physical offset
 * @param nodata the raw code of a missing sample; {@code NaN} when there is none
 */
public record FsgHeader(RasterDtype dtype, int codec, int nx, int ny, int bands, boolean partial, double scale,
        double offset, double nodata) {

    /** No compression. */
    public static final int CODEC_NONE = 0;

    /** Deflate (zlib) at level 1. */
    public static final int CODEC_DEFLATE = 1;

    /** Validates the dimensions and the codec. */
    public FsgHeader {
        Objects.requireNonNull(dtype, "dtype");
        if (codec != CODEC_NONE && codec != CODEC_DEFLATE) {
            throw new IllegalArgumentException("Unknown codec " + codec);
        }
        if (nx <= 0 || ny <= 0 || bands <= 0 || bands > 0xFFFF) {
            throw new IllegalArgumentException("Bad frame dimensions " + nx + "x" + ny + "x" + bands);
        }
    }

    /** The decoded payload length in bytes: {@code nx × ny × bands × bytesPerSample}. */
    public long decodedLength() {
        return (long) nx * ny * bands * dtype.bytesPerSample();
    }

    /** This header with another codec. */
    public FsgHeader withCodec(int newCodec) {
        return new FsgHeader(dtype, newCodec, nx, ny, bands, partial, scale, offset, nodata);
    }
}
