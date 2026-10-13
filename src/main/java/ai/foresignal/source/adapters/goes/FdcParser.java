package ai.foresignal.source.adapters.goes;

import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the fire pixels of one GOES-R ABI L2 Fire/Hot Spot Characterization CONUS file (FDCC; prompt
 * 048, GOES-R PUG Vol. 5 "Fire/Hot Spot Characterization").
 *
 * <p>Variables, read in enhanced mode through {@link NetcdfDecoder#open} ({@code NaN} for fill):
 * {@code Mask} (fire category), {@code Power} (fire radiative power, MW), {@code Temp} (K),
 * {@code Area} (m²), {@code DQF}, and the {@code goes_imager_projection} fixed grid with its
 * {@code x}/{@code y} scan angles, for pixel centers. A missing variable is a
 * {@link SilentFormatChangeException}.
 *
 * <p>A pixel is a detection when its mask is in {@code 10–13} or {@code 30–33}: processed (good),
 * saturated, cloud-contaminated and high-probability fire pixels, and their temporally filtered
 * variants. Medium and low probability ({@code 14, 15, 34, 35}) are not detections. Only the
 * candidates' values of the other variables are kept, so one full variable is held at a time.
 *
 * <p>Verified on the 2026-10-12 21:56 UTC CONUS file: 1500 × 2500 pixels, 25 detections (mask 10 × 2,
 * 12 × 1, 30 × 19, 32 × 3), FRP 4.6–271 MW, {@code Area} in m² (4,000–214,625), {@code Mask}
 * {@code _FillValue} −99.
 */
public final class FdcParser {

    /** The record kind. */
    public static final String KIND = "fire-detection";

    /** The variables read. */
    static final String MASK = "Mask";
    static final String POWER = "Power";
    static final String TEMP = "Temp";
    static final String AREA = "Area";
    static final String DQF = "DQF";

    /** The satellite in payloads and ids. */
    static final String SATELLITE = "G19";

    private FdcParser() {}

    /**
     * One fire pixel.
     *
     * @param col the column (x index, file order)
     * @param row the row (y index, file order, north to south)
     * @param lon the pixel center longitude
     * @param lat the pixel center latitude
     * @param maskCode the fire mask category
     * @param frpMw fire radiative power, MW, or NaN
     * @param tempK fire temperature, K, or NaN
     * @param areaM2 fire area, m², or NaN
     * @param dqf the data quality flag, or −1 for fill
     */
    public record Detection(int col, int row, double lon, double lat, int maskCode, float frpMw, float tempK,
            float areaM2, int dqf) {

        /** {@code g19:{col}:{row}}. */
        public String externalId() {
            return "g19:" + col + ":" + row;
        }

        /** {@code {col}:{row}}, the key cluster membership uses. */
        public String pixel() {
            return col + ":" + row;
        }
    }

    /**
     * One parsed file.
     *
     * @param detections the fire pixels inside the envelope, in file order
     * @param nx columns
     * @param ny rows
     * @param fireCandidates fire pixels before the envelope filter
     */
    public record Result(List<Detection> detections, int nx, int ny, int fireCandidates) {}

    /** True for a detection mask: 10–13 or 30–33. */
    public static boolean isDetection(int mask) {
        return (mask >= 10 && mask <= 13) || (mask >= 30 && mask <= 33);
    }

    /**
     * Parses {@code file}.
     *
     * @param envelope {@code [minLon, minLat, maxLon, maxLat]} to keep, or null for every pixel (CONUS)
     * @throws SilentFormatChangeException when a variable or the projection is missing
     */
    public static Result parse(NetcdfDecoder decoder, Path file, String sourceId, double[] envelope) {
        try (NetcdfDecoder.Reader r = decoder.open(file, sourceId)) {
            float[] mask = r.floats(MASK);
            NetcdfDecoder.FixedGrid grid = r.fixedGrid(MASK);
            List<Integer> candidates = new ArrayList<>();
            for (int i = 0; i < mask.length; i++) {
                float m = mask[i];
                if (!Float.isNaN(m) && isDetection(Math.round(m))) {
                    candidates.add(i);
                }
            }
            int[] fireIdx = candidates.stream().mapToInt(Integer::intValue).toArray();
            List<double[]> positions = new ArrayList<>();
            List<Integer> kept = new ArrayList<>();
            for (int k = 0; k < fireIdx.length; k++) {
                double[] lonLat = grid.lonLat(fireIdx[k]);
                if (Double.isNaN(lonLat[0]) || Double.isNaN(lonLat[1])) {
                    continue;
                }
                if (envelope != null && !(lonLat[0] >= envelope[0] && lonLat[0] <= envelope[2]
                        && lonLat[1] >= envelope[1] && lonLat[1] <= envelope[3])) {
                    continue;
                }
                kept.add(k);
                positions.add(lonLat);
            }
            int[] idx = kept.stream().mapToInt(k -> fireIdx[k]).toArray();
            float[] power = r.gather(POWER, idx);
            float[] temp = r.gather(TEMP, idx);
            float[] area = r.gather(AREA, idx);
            float[] dqf = r.gather(DQF, idx);
            List<Detection> out = new ArrayList<>(idx.length);
            for (int k = 0; k < idx.length; k++) {
                int i = idx[k];
                double[] lonLat = positions.get(k);
                out.add(new Detection(i % grid.nx(), i / grid.nx(), lonLat[0], lonLat[1], Math.round(mask[i]), power[k],
                        temp[k], area[k], Float.isNaN(dqf[k]) ? -1 : Math.round(dqf[k])));
            }
            return new Result(out, grid.nx(), grid.ny(), fireIdx.length);
        }
    }

    /** The confidence of a detection mask: processed, saturated, cloudy or high. */
    public static String confidence(int mask) {
        return switch (mask % 10) {
            case 0 -> "processed";
            case 1 -> "saturated";
            case 2 -> "cloudy";
            case 3 -> "high";
            default -> throw new IllegalArgumentException("Not a detection mask: " + mask);
        };
    }

    /** True for the temporally filtered masks (30–33). */
    public static boolean temporallyFiltered(int mask) {
        return mask >= 30;
    }

    /** {@code v} to 2 significant figures, or null for NaN or a negative value. */
    public static Double sig2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v) || v < 0) {
            return null;
        }
        if (v == 0) {
            return 0.0;
        }
        return new BigDecimal(v).round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue();
    }

    /** {@code v} rounded to 10, or null for NaN. */
    public static Long round10(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return null;
        }
        return Math.round(v / 10.0) * 10;
    }

    /** {@code v} rounded to 4 decimals (pixel centers, so float noise never makes a version). */
    static double round4(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }

    /**
     * The {@code fire-detection} record of {@code d}.
     *
     * @param scanStart the scan's {@code s} time ({@code observedAt})
     * @param firstSeen when the pixel was first detected ({@code eventTime}, stable across scans)
     */
    public static NormalizedRecord record(Detection d, Instant scanStart, Instant firstSeen) {
        Double frp = sig2(d.frpMw());
        Double areaKm2 = Float.isNaN(d.areaM2()) ? null : sig2(d.areaM2() / 1_000_000.0);
        String confidence = confidence(d.maskCode());
        return NormalizedRecord.builder(d.externalId(), KIND)
                .observedAt(scanStart)
                .eventTime(firstSeen)
                .point(round4(d.lon()), round4(d.lat()))
                .put("maskCode", d.maskCode())
                .put("confidence", confidence)
                .put("temporallyFiltered", temporallyFiltered(d.maskCode()))
                .put("frpMw", frp)
                .put("tempK", round10(d.tempK()))
                .put("areaKm2", areaKm2)
                .put("satellite", SATELLITE)
                .put("method", "FDC")
                .put("label", label(confidence, frp))
                .build();
    }

    /** "Fire detection (GOES-19, high), 45 MW". */
    static String label(String confidence, Double frpMw) {
        String base = "Fire detection (GOES-19, " + confidence + ")";
        if (frpMw == null) {
            return base;
        }
        return base + ", " + formatMw(frpMw) + " MW";
    }

    /** A 2-significant-figure power without a trailing ".0". */
    static String formatMw(double mw) {
        return mw == Math.rint(mw) && Math.abs(mw) < 1e15 ? Long.toString((long) mw)
                : BigDecimal.valueOf(mw).stripTrailingZeros().toPlainString();
    }
}
