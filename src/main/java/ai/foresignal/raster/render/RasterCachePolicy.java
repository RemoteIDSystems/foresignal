package ai.foresignal.raster.render;

import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.source.spi.ProductSpec;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * The one cache rule for tiles, images, legends, fields and media (prompt 045;
 * Weather-Imagery-Design §0.1, §6.1), used by every raster controller.
 *
 * <ul>
 *   <li>A content-addressed URL (frame token {@code {timeKey}~{sha8}}, versioned style
 *       {@code name@v}, a pinned {@code run} for forecasts, and a complete frame) gets
 *       {@code {public|private}, max-age=31536000, immutable} and a strong ETag.
 *   <li>Anything else (an alias, a bare {@code timeKey}, an unversioned style, a partial frame) gets
 *       {@code {public|private}, max-age=min(cadence/2, 60s)}, an ETag, and
 *       {@code Foresignal-Frame-Time}; the ETag is checked ({@link #notModified}) <em>before</em>
 *       anything is rendered.
 *   <li>{@code public} only when {@code security.openReads} covers the path for this caller and the
 *       source is not private; otherwise {@code private}.
 * </ul>
 */
public final class RasterCachePolicy {

    /** One year, the immutable lifetime. */
    public static final long IMMUTABLE_SECONDS = 31_536_000L;

    /** The valid time of the frame a non-content-addressed URL resolved to. */
    public static final String FRAME_TIME_HEADER = "Foresignal-Frame-Time";

    /** Set when {@code onExpired=transparent} served the transparent tile for an expired frame. */
    public static final String FRAME_EXPIRED_HEADER = "Foresignal-Frame-Expired";

    /** The snapped bounds of an image. */
    public static final String IMAGE_BOUNDS_HEADER = "Foresignal-Image-Bounds";

    /** {@code Vary}: responses depend on the caller and the encoding. */
    public static final String VARY = "Accept-Encoding, Authorization";

    private static final HexFormat HEX = HexFormat.of();

    private RasterCachePolicy() {}

    /**
     * A cache decision.
     *
     * @param immutable true for a content-addressed URL
     * @param publicCache {@code public} rather than {@code private}
     * @param maxAge the lifetime
     */
    public record Decision(boolean immutable, boolean publicCache, Duration maxAge) {

        /** The {@code Cache-Control} value. */
        public String cacheControl() {
            String scope = publicCache ? "public" : "private";
            return immutable ? scope + ", max-age=" + IMMUTABLE_SECONDS + ", immutable"
                    : scope + ", max-age=" + maxAge.toSeconds();
        }
    }

    /**
     * The decision for a response.
     *
     * @param contentAddressed the URL names the content (see the class comment)
     * @param publicCache {@code openReads} covers the path for this caller and the source is public
     * @param spec the product, whose cadence sets the short lifetime (null: 60 s)
     */
    public static Decision decide(boolean contentAddressed, boolean publicCache, ProductSpec spec) {
        Duration shortAge = spec == null ? ProductCatalogService.MAX_ALIAS_AGE : ProductCatalogService.aliasMaxAge(spec);
        return new Decision(contentAddressed, publicCache,
                contentAddressed ? Duration.ofSeconds(IMMUTABLE_SECONDS) : shortAge);
    }

    /**
     * The cache headers of a response.
     *
     * @param etag the strong ETag
     * @param frameTime the resolved frame's valid time, sent on non-immutable responses; or null
     */
    public static HttpHeaders headers(Decision decision, String etag, Instant frameTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(decision.cacheControl());
        if (etag != null) {
            headers.setETag(etag);
        }
        headers.set(HttpHeaders.VARY, VARY);
        if (!decision.immutable() && frameTime != null) {
            headers.set(FRAME_TIME_HEADER, frameTime.toString());
        }
        return headers;
    }

    /**
     * True when the request's {@code If-None-Match} matches {@code etag}: answer 304 without
     * rendering ({@link org.springframework.web.context.request.WebRequest#checkNotModified}).
     */
    public static boolean notModified(HttpServletRequest request, String etag) {
        return etag != null && new ServletWebRequest(request).checkNotModified(etag);
    }

    /** A strong ETag over {@code parts}: {@code "r-" + the first 16 hex of their SHA-256}. */
    public static String etag(String... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                digest.update(String.valueOf(part).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return "\"r-" + HEX.formatHex(digest.digest()).substring(0, 16) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** A strong ETag over a body. */
    public static String etagOf(byte[] body) {
        try {
            return "\"r-" + HEX.formatHex(MessageDigest.getInstance("SHA-256").digest(body)).substring(0, 16) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
