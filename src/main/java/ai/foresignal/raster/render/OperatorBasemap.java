package ai.foresignal.raster.render;

import ai.foresignal.media.GuardedImages;
import ai.foresignal.media.ImageGuard;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The optional operator basemap of static maps (prompt 055; Weather-Imagery-Design §8 "No public
 * basemaps"): {@code foresignal.maps.basemap-url-template} ({@code FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE}),
 * an XYZ template such as {@code http://tiles.lan:8080/{z}/{x}/{y}.png} for a tile server on the
 * operator's own network. Off by default; then static maps draw Natural Earth only.
 *
 * <ul>
 *   <li>The template is accepted only when its host resolves to a loopback or private address:
 *       {@link OutboundUrlPolicy} profile {@code LAN_ONLY}, the outbound successor of the prompt 004
 *       {@code LocalAddresses} rule. A public host (for example {@code tile.openstreetmap.org}, whose
 *       usage policy forbids this kind of use) is refused and logged, and the map falls back to
 *       Natural Earth.
 *   <li>Tiles are fetched only inside the map request that needs them, through {@link PinnedHttp}
 *       (connected to the checked address, redirects off), at most {@value #MAX_TILES} tiles with a
 *       {@value #TIMEOUT_MILLIS} ms timeout each. Nothing is cached or proxied: this is never a tile
 *       proxy, and a client request never reaches a third-party server.
 *   <li>Any failure draws nothing and returns false, so the caller draws Natural Earth instead.
 * </ul>
 */
@Component
public class OperatorBasemap {

    private static final Logger log = LoggerFactory.getLogger(OperatorBasemap.class);

    /** The most tiles one map fetches. */
    static final int MAX_TILES = 16;

    /** The per-tile timeout, milliseconds. */
    static final int TIMEOUT_MILLIS = 800;

    /** The tile size the template serves, pixels. */
    static final int TILE = 256;

    /** The largest tile body accepted, bytes. */
    static final int MAX_TILE_BYTES = 2 * 1024 * 1024;

    private final String template;
    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;
    private final GuardedImages images;

    /**
     * Creates the basemap from {@code foresignal.maps.basemap-url-template} (blank: off).
     *
     * @param template the XYZ template, or blank for none
     * @param policy the outbound policy that keeps the host on the LAN
     * @param pinned the pinned client
     * @param images the guarded decoder every tile passes (prompt 043: nothing decodes pixels without
     *     {@code ImageGuard}; checkpoint 057 found this path calling {@code ImageIO.read} directly)
     */
    @Autowired
    public OperatorBasemap(@Value("${foresignal.maps.basemap-url-template:}") String template,
            OutboundUrlPolicy policy, PinnedHttp pinned, GuardedImages images) {
        this.template = template == null || template.isBlank() ? null : template.strip();
        this.policy = policy;
        this.pinned = pinned;
        this.images = images;
    }

    /**
     * Creates the basemap with a guard limited to the {@value #MAX_TILE_BYTES}-byte tile cap (tests).
     *
     * @param template the XYZ template, or blank for none
     * @param policy the outbound policy
     * @param pinned the pinned client
     */
    public OperatorBasemap(String template, OutboundUrlPolicy policy, PinnedHttp pinned) {
        this(template, policy, pinned, new GuardedImages(new ImageGuard(MAX_TILE_BYTES, null)));
    }

    /** True when a template is configured (it is still checked on every use). */
    public boolean configured() {
        return template != null;
    }

    /**
     * Why a template is refused, or empty when it is acceptable: it must contain {@code {z}}, {@code {x}}
     * and {@code {y}}, be http(s), and its host must resolve to a loopback or private address.
     */
    public Optional<String> problem(String candidate) {
        if (candidate == null || !candidate.contains("{z}") || !candidate.contains("{x}")
                || !candidate.contains("{y}")) {
            return Optional.of("the basemap template must contain {z}, {x} and {y}");
        }
        String sample = fill(candidate, 0, 0, 0);
        OutboundUrlPolicy.Result result = policy.check(sample, OutboundUrlPolicy.Profile.LAN_ONLY, true);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            return Optional.of("the basemap host is not on the local network: " + denied.reason()
                    + " (public tile servers such as tile.openstreetmap.org are never used)");
        }
        return Optional.empty();
    }

    /**
     * Draws the operator tiles covering a WebMercator box into {@code g} at {@code (0, top)}, scaled to
     * {@code width × height}.
     *
     * @return true when every tile was drawn; false (and nothing useful drawn) otherwise
     */
    public boolean draw(Graphics2D g, double west, double south, double east, double north, int width, int height,
            int top) {
        if (template == null) {
            return false;
        }
        Optional<String> refused = problem(template);
        if (refused.isPresent()) {
            log.warn("Static maps ignore foresignal.maps.basemap-url-template: {}", refused.get());
            return false;
        }
        if (west > east) {
            return false;
        }
        double x0 = WebMercator.x(west);
        double x1 = WebMercator.x(east);
        double y0 = WebMercator.y(south);
        double y1 = WebMercator.y(north);
        int z = zoom(x1 - x0, width);
        int[] range = WebMercator.tileRange(z, west, south, east, north);
        int count = (range[2] - range[0] + 1) * (range[3] - range[1] + 1);
        while (count > MAX_TILES && z > 0) {
            z--;
            range = WebMercator.tileRange(z, west, south, east, north);
            count = (range[2] - range[0] + 1) * (range[3] - range[1] + 1);
        }
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = canvas.createGraphics();
        try {
            cg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            for (int ty = range[1]; ty <= range[3]; ty++) {
                for (int tx = range[0]; tx <= range[2]; tx++) {
                    BufferedImage tile = fetch(fill(template, z, tx, ty));
                    if (tile == null) {
                        return false;
                    }
                    double[] m = WebMercator.tileMeters(z, tx, ty);
                    int px0 = (int) Math.floor((m[0] - x0) / (x1 - x0) * width);
                    int px1 = (int) Math.ceil((m[2] - x0) / (x1 - x0) * width);
                    int py0 = (int) Math.floor((y1 - m[3]) / (y1 - y0) * height);
                    int py1 = (int) Math.ceil((y1 - m[1]) / (y1 - y0) * height);
                    cg.drawImage(tile, px0, py0, px1 - px0, py1 - py0, null);
                }
            }
        } finally {
            cg.dispose();
        }
        g.drawImage(canvas, 0, top, null);
        return true;
    }

    /** The zoom whose tiles are closest to one tile pixel per map pixel. */
    static int zoom(double spanMeters, int width) {
        double metersPerPixel = spanMeters / Math.max(1, width);
        double z = Math.log(2 * WebMercator.HALF / (TILE * metersPerPixel)) / Math.log(2);
        return Math.max(0, Math.min(19, (int) Math.round(z)));
    }

    /** The template with {@code {z}}, {@code {x}} and {@code {y}} filled. */
    static String fill(String template, int z, int x, int y) {
        return template.replace("{z}", Integer.toString(z)).replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y));
    }

    private BufferedImage fetch(String url) {
        try {
            URI uri = URI.create(url);
            OutboundUrlPolicy.Result result = policy.check(uri, OutboundUrlPolicy.Profile.LAN_ONLY, true);
            if (!(result instanceof OutboundUrlPolicy.Allowed allowed)) {
                return null;
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(uri, allowed.pinned()))
                    .timeout(Duration.ofMillis(TIMEOUT_MILLIS)).GET();
            PinnedHttp.setHostHeader(builder, uri);
            HttpClient client = pinned.client(uri, new PinnedHttp.ClientSpec(Duration.ofMillis(TIMEOUT_MILLIS),
                    HttpClient.Version.HTTP_1_1));
            HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body().length > MAX_TILE_BYTES) {
                return null;
            }
            return images.decode(response.body(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | RuntimeException e) {
            log.debug("Operator basemap tile {} failed: {}", url, e.getMessage());
            return null;
        }
    }
}
