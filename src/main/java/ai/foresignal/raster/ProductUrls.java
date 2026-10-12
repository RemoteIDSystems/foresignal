package ai.foresignal.raster;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.ProductSpec;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Builds the fully expanded product URLs and templates (Weather-Imagery-Design §6.1): clients never
 * build time strings, they fill only {@code {z}/{x}/{y}} or {@code {bbox}/{width}/{height}}.
 *
 * <ul>
 *   <li>tiles: {@code {base}/v1/products/{id}/tiles/{frameKey}/{z}/{x}/{y}.png?style={name@v}} (plus
 *       {@code &run=} for a forecast frame, which pins the run);
 *   <li>image: {@code {base}/v1/products/{id}/image/{frameKey}.png?bbox={bbox}&width={width}&height={height}&style=…};
 *   <li>forecast run template: {@code …/tiles/{timeKey}/{z}/{x}/{y}.png?style=…&run={runIso}}.
 * </ul>
 *
 * <p>The base is {@code server.publicBaseUrl} when set. API responses otherwise use the request's
 * scheme, host and port; {@code raster-frame} record payloads, which are built with no request, are
 * then root-relative ({@code /v1/products/…}). The routes behind these URLs are served by prompt 045.
 */
@Component
public class ProductUrls {

    private final AppSettingService settings;

    /** Creates the builder. */
    public ProductUrls(AppSettingService settings) {
        this.settings = settings;
    }

    /** {@code server.publicBaseUrl} without a trailing slash, or null when unset. */
    public String configuredBase() {
        if (settings == null) {
            return null;
        }
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        return server.normalizedPublicBaseUrl();
    }

    /** The base for record payloads: the configured public base, or empty (root-relative URLs). */
    public String storedBase() {
        String configured = configuredBase();
        return configured == null ? "" : configured;
    }

    /** The base for an API response: the configured public base, else the request's origin. */
    public String base(HttpServletRequest request) {
        String configured = configuredBase();
        if (configured != null) {
            return configured;
        }
        return requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /** {@code scheme://host[:port]}, bracketing an IPv6 literal and omitting 80/443. */
    static String requestBase(String scheme, String host, int port) {
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        boolean defaultPort = port <= 0
                || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + h + (defaultPort ? "" : ":" + port);
    }

    /** {@code {base}/v1/products/{id}}. */
    public static String product(String base, String productId) {
        return base + "/v1/products/" + productId;
    }

    /** The frames list. */
    public static String frames(String base, String productId) {
        return product(base, productId) + "/frames";
    }

    /** The latest-frame alias. */
    public static String latest(String base, String productId) {
        return product(base, productId) + "/frames/latest";
    }

    /** The TileJSON document (045). */
    public static String tileJson(String base, String productId) {
        return product(base, productId) + "/tilejson";
    }

    /** The JSON legend (045). */
    public static String legend(String base, String productId) {
        return product(base, productId) + "/legend";
    }

    /** The PNG legend (045). */
    public static String legendPng(String base, String productId) {
        return product(base, productId) + "/legend.png";
    }

    /** The timeline (051) for one product. */
    public static String timeline(String base, String productId) {
        return base + "/v1/products/timeline?products=" + productId;
    }

    /** The content-addressed 256 px tile template of a frame. */
    public static String tiles(String base, ProductSpec spec, RasterFrameRow frame) {
        return tileBase(base, spec.id(), frame.frameKey()) + "/{z}/{x}/{y}.png" + query(spec, frame.referenceTime());
    }

    /** The {@code @2x} (512 px) tile template of a frame. */
    public static String tiles2x(String base, ProductSpec spec, RasterFrameRow frame) {
        return tileBase(base, spec.id(), frame.frameKey()) + "/{z}/{x}/{y}@2x.png" + query(spec, frame.referenceTime());
    }

    /** The image template of a frame. */
    public static String image(String base, ProductSpec spec, RasterFrameRow frame) {
        String style = styleParam(spec);
        String run = frame.referenceTime() == null ? "" : "&run=" + FrameKeys.runIso(frame.referenceTime());
        return product(base, spec.id()) + "/image/" + frame.frameKey() + ".png?bbox={bbox}&width={width}&height={height}"
                + (style == null ? "" : "&style=" + style) + run;
    }

    /** A forecast run's tile template, {@code {timeKey}} left for the step, the run pinned. */
    public static String runTilesTemplate(String base, ProductSpec spec, Instant run) {
        return tileBase(base, spec.id(), "{timeKey}") + "/{z}/{x}/{y}.png" + query(spec, run);
    }

    private static String tileBase(String base, String productId, String key) {
        return product(base, productId) + "/tiles/" + key;
    }

    private static String query(ProductSpec spec, Instant run) {
        String style = styleParam(spec);
        StringBuilder out = new StringBuilder();
        if (style != null) {
            out.append("?style=").append(style);
        }
        if (run != null) {
            out.append(out.isEmpty() ? '?' : '&').append("run=").append(FrameKeys.runIso(run));
        }
        return out.toString();
    }

    private static String styleParam(ProductSpec spec) {
        if (spec.defaultStyle() != null) {
            return spec.defaultStyle();
        }
        return spec.styles().isEmpty() ? null : spec.styles().get(0);
    }
}
