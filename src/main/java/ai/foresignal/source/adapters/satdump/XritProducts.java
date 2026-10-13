package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/**
 * The dynamic products of SatDump geostationary frames (prompt 049, Weather-Imagery-Design §4.5 item 1):
 * one per satellite, region and channel, id {@code satdump-{station}-{satKey}-{region}-{channel}}
 * (Conventions "SatDump product ids").
 *
 * <p>Samples: 8-bit channels are stored {@code u8}, 16-bit grayscale {@code u16} and color composites
 * {@code rgba8}. Gray codes keep the image's counts (a 16-bit image whose values exceed
 * {@code 2^bit_depth − 1} is scaled down to that range), code 0 is space (no data), and the physical
 * value is mapped onto the {@code ir-gray@1} ramp, {@code 330 − 150 × code / max}, so a bright pixel
 * draws white; it is a display value, not a temperature. Color composites draw their own colors
 * ({@code rgb@1}); black is transparent so space does not cover the map.
 */
public final class XritProducts {

    /** The ladder of SatDump geostationary products (Weather-Imagery-Design §2.4). */
    public static final Duration LADDER = Duration.ofHours(72);

    /** The cadence assumed until two frames have been seen. */
    public static final Duration DEFAULT_CADENCE = Duration.ofMinutes(15);

    /** The region used when the folder has none. */
    public static final String FULL_DISK = "fd";

    private static final double RAMP_TOP = 330;
    private static final double RAMP_SPAN = 150;

    private XritProducts() {}

    /**
     * The product id, kebab-case and at most 64 characters; a longer one keeps its first 57 characters
     * and appends a 6-hex hash of the whole.
     */
    public static String productId(String station, String satKey, String region, String channel) {
        String r = region == null || region.isBlank() ? FULL_DISK : SatNames.slug(region);
        String id = "satdump-" + SatNames.slug(station) + "-" + SatNames.slug(satKey) + "-" + (r.isEmpty() ? FULL_DISK : r)
                + "-" + (SatNames.slug(channel).isEmpty() ? "ch" : SatNames.slug(channel));
        return bounded(id);
    }

    /**
     * {@code id} with repeated hyphens collapsed, at most 64 characters: a longer one keeps its first 57
     * characters and appends a 6-hex hash of the whole (prompt 054 shares it for polar overlays).
     */
    public static String bounded(String id) {
        id = id.replaceAll("-+", "-");
        if (id.length() <= 64) {
            return id;
        }
        String head = id.substring(0, 57).replaceAll("-+$", "");
        return head + "-" + sha6(id);
    }

    /** The stored sample type of a decoded image. */
    public static RasterDtype dtype(BufferedImage image) {
        ColorModel model = image.getColorModel();
        if (model.getNumColorComponents() >= 3) {
            return RasterDtype.RGBA8;
        }
        return image.getRaster().getSampleModel().getSampleSize(0) > 8 ? RasterDtype.U16 : RasterDtype.U8;
    }

    /** The largest gray code of {@code dtype} at {@code bitDepth}. */
    public static int maxCode(RasterDtype dtype, Integer bitDepth) {
        if (dtype == RasterDtype.U8) {
            return 255;
        }
        int bits = bitDepth == null || bitDepth < 1 || bitDepth > 16 ? 16 : bitDepth;
        return (1 << bits) - 1;
    }

    /**
     * The spec of one channel.
     *
     * @param id the product id
     * @param title the title
     * @param dtype the sample type
     * @param bitDepth the channel's bit depth, or null
     * @param cadence the observed median interval between frames
     * @param satellite the satellite's display name
     */
    public static ProductSpec spec(String id, String title, RasterDtype dtype, Integer bitDepth, Duration cadence,
            String satellite) {
        Duration c = cadence == null || !cadence.isPositive() ? DEFAULT_CADENCE : cadence;
        ProductSpec.Builder builder = ProductSpec.builder(id)
                .title(title)
                .category("imagery")
                .dtype(dtype)
                .temporal(ProductSpec.Temporal.observation(c, c.multipliedBy(3)))
                .spatial(new ProductSpec.Spatial("station-disc", 6, 0))
                .retention(ProductSpec.RetentionLadder.maxAge(LADDER))
                .attribution(new ProductSpec.Attribution(satellite + " imagery received by an operator-owned SatDump"
                        + " ground station", null, null, SatDumpDescriptor.LICENSE, null, false))
                .coverageMeaning("The disc the satellite sees from its longitude; transparent in space");
        if (dtype == RasterDtype.RGBA8) {
            builder.defaultStyle("rgb@1").legendSummary("A SatDump color composite drawn in its own colors");
        } else {
            int max = maxCode(dtype, bitDepth);
            builder.scaleOffset(-RAMP_SPAN / max, RAMP_TOP)
                    .nodata(0.0)
                    .defaultStyle("ir-gray@1")
                    .legendSummary("Relative brightness on the ir-gray ramp (bright is white); a display value, not"
                            + " a temperature");
        }
        return builder.build();
    }

    /**
     * The decoded bands of an image, rows north to south.
     *
     * @param image the image
     * @param dtype its sample type from {@link #dtype}
     * @param bitDepth the channel's bit depth, or null
     */
    public static DecodedBands bands(BufferedImage image, RasterDtype dtype, Integer bitDepth) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] codes = new int[w * h];
        if (dtype == RasterDtype.RGBA8) {
            boolean alpha = image.getColorModel().hasAlpha();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = image.getRGB(x, y);
                    int r = (argb >>> 16) & 0xFF;
                    int g = (argb >>> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int a = alpha ? (argb >>> 24) & 0xFF : ((r | g | b) == 0 ? 0 : 255);
                    codes[y * w + x] = (r << 24) | (g << 16) | (b << 8) | a;
                }
            }
            return DecodedBands.codes(codes);
        }
        Raster raster = image.getRaster();
        int max = maxCode(dtype, bitDepth);
        int observedMax = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = raster.getSample(x, y, 0);
                codes[y * w + x] = v;
                observedMax = Math.max(observedMax, v);
            }
        }
        if (dtype == RasterDtype.U16 && observedMax > max) {
            // Counts stretched to the full 16-bit range: bring them back to the channel's bit depth.
            for (int i = 0; i < codes.length; i++) {
                codes[i] = (int) Math.round(codes[i] * (double) max / 65_535);
            }
        }
        return DecodedBands.codes(codes);
    }

    /** The median of the intervals between {@code times} (epoch seconds, any order), or null. */
    public static Duration medianInterval(List<Long> times) {
        List<Long> sorted = times.stream().distinct().sorted().toList();
        if (sorted.size() < 2) {
            return null;
        }
        long[] gaps = new long[sorted.size() - 1];
        for (int i = 1; i < sorted.size(); i++) {
            gaps[i - 1] = sorted.get(i) - sorted.get(i - 1);
        }
        java.util.Arrays.sort(gaps);
        long median = gaps.length % 2 == 1 ? gaps[gaps.length / 2]
                : (gaps[gaps.length / 2 - 1] + gaps[gaps.length / 2]) / 2;
        return median > 0 ? Duration.ofSeconds(median) : null;
    }

    private static String sha6(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 6);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
