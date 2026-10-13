package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Reads the flashes of one GOES-R GLM L2 LCFA granule (prompt 048; GOES-R PUG Vol. 5, GLM L2+ Lightning
 * Detection).
 *
 * <p>Variables (read in enhanced mode through {@link NetcdfDecoder#open}, so scale, offset and
 * {@code _Unsigned} are applied): {@code flash_lat}, {@code flash_lon} (degrees),
 * {@code flash_time_offset_of_first_event} (seconds relative to {@code product_time}),
 * {@code flash_energy} (J), {@code flash_quality_flag}, and the scalar {@code product_time} (seconds since
 * 2000-01-01 12:00:00 UTC). A missing variable is a {@link SilentFormatChangeException}
 * ("variable flash_lat missing").
 *
 * <p>Only flashes with {@code flash_quality_flag == 0} are kept. A flash's time is
 * {@code product_time + flash_time_offset_of_first_event}; the offset can be slightly negative (the
 * first event fell in the previous granule). Energy is converted to femtojoules (× 10¹⁵).
 *
 * <p>Verified on 2026-10-12 granules: an active 20-s file held 450 flashes (420 with flag 0, 30 with
 * flag 3), offsets from −0.96 s to 19.4 s, energies 1.3 fJ to 5,249 fJ; the offsets' {@code units}
 * attribute names the same instant as {@code product_time}.
 */
public final class GlmParser {

    /** The variables read. */
    static final String LAT = "flash_lat";
    static final String LON = "flash_lon";
    static final String OFFSET = "flash_time_offset_of_first_event";
    static final String ENERGY = "flash_energy";
    static final String QUALITY = "flash_quality_flag";
    static final String PRODUCT_TIME = "product_time";

    /** 2000-01-01T12:00:00Z, the GOES-R epoch of {@code product_time}. */
    static final Instant J2000 = Instant.parse("2000-01-01T12:00:00Z");

    /** Joules to femtojoules. */
    static final double FJ_PER_J = 1e15;

    private GlmParser() {}

    /**
     * One parsed granule.
     *
     * @param flashes the quality-0 flashes
     * @param total every flash in the file
     * @param productTime the file's {@code product_time}
     */
    public record Result(FlashBatch flashes, int total, Instant productTime) {

        /** Flashes dropped for their quality flag (or an unusable position). */
        public int dropped() {
            return total - flashes.size();
        }
    }

    /**
     * Parses {@code file}.
     *
     * @param decoder the NetCDF decoder
     * @param file the downloaded granule, under {@code rasters/tmp}
     * @param sourceId the source, named in a {@link SilentFormatChangeException}
     * @throws SilentFormatChangeException when a variable is missing
     */
    public static Result parse(NetcdfDecoder decoder, Path file, String sourceId) {
        try (NetcdfDecoder.Reader r = decoder.open(file, sourceId)) {
            float[] lat = r.floats(LAT);
            float[] lon = r.floats(LON);
            float[] offset = r.floats(OFFSET);
            float[] energy = r.floats(ENERGY);
            float[] quality = r.floats(QUALITY);
            double productTime = r.scalar(PRODUCT_TIME);
            return assemble(productTime, lat, lon, offset, energy, quality);
        }
    }

    /**
     * Builds the batch from the decoded variables (pure, for tests).
     *
     * @param productTime seconds since {@link #J2000}
     * @throws IllegalArgumentException when the arrays differ in length
     */
    public static Result assemble(double productTime, float[] lat, float[] lon, float[] offset, float[] energyJ,
            float[] quality) {
        int n = lat.length;
        if (lon.length != n || offset.length != n || energyJ.length != n || quality.length != n) {
            throw new IllegalArgumentException("GLM flash variables differ in length: lat " + n + ", lon " + lon.length
                    + ", offset " + offset.length + ", energy " + energyJ.length + ", quality " + quality.length);
        }
        long baseMs = J2000.toEpochMilli() + Math.round(productTime * 1000.0);
        int kept = 0;
        long[] t = new long[n];
        float[] la = new float[n];
        float[] lo = new float[n];
        float[] e = new float[n];
        for (int i = 0; i < n; i++) {
            if (quality[i] != 0f || !Float.isFinite(lat[i]) || !Float.isFinite(lon[i]) || !Float.isFinite(offset[i])
                    || Math.abs(lat[i]) > 90f || Math.abs(lon[i]) > 180f) {
                continue;
            }
            t[kept] = baseMs + Math.round(offset[i] * 1000.0);
            la[kept] = lat[i];
            lo[kept] = lon[i];
            e[kept] = Float.isFinite(energyJ[i]) ? (float) (energyJ[i] * FJ_PER_J) : 0f;
            kept++;
        }
        FlashBatch batch = kept == 0 ? FlashBatch.EMPTY
                : new FlashBatch(java.util.Arrays.copyOf(t, kept), java.util.Arrays.copyOf(la, kept),
                        java.util.Arrays.copyOf(lo, kept), java.util.Arrays.copyOf(e, kept));
        return new Result(batch, n, Instant.ofEpochMilli(baseMs));
    }
}
