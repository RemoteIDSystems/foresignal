package ai.foresignal.raster.render;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Draws basemap-free static map PNGs with Java2D (prompt 055; Weather-Imagery-Design §6.6, §8): the
 * renderer behind {@code GET /v1/maps/static}, the MCP tool {@code get_map_image} and the map
 * attachments of geolocated alerts. Nothing here fetches third-party tiles: the land, lakes, coastline
 * and boundaries come from the bundled Natural Earth asset ({@link BasemapStore}), the rasters from
 * Foresignal's own stored frames, and only an operator's LAN tile server ({@link OperatorBasemap}) can
 * replace the Natural Earth fills.
 *
 * <p>Layout, top to bottom: a header strip naming each product with its <em>frame valid time</em> (UTC,
 * plus the place's local time), the map, a legend strip, the attribution line and the footer
 * "Automated — not an official warning". The map area is drawn in this order:
 *
 * <ol>
 *   <li>water, then land and lakes (or the operator basemap);
 *   <li>each product's frame, drawn through {@link RasterRenderService#drawDirect} over the map box and
 *       composited at the product's default opacity;
 *   <li>admin-1 lines, admin-0 boundaries, the coastline;
 *   <li>the alert or record geometry: stroke and translucent fill in the severity color;
 *   <li>the place marker and its range rings;
 *   <li>a scale bar, and a labeled note for every product whose frame is missing or stale ("MRMS
 *       reflectivity unavailable — not evidence of clear weather"): a missing layer is never silently
 *       omitted.
 * </ol>
 *
 * <p>The projection is WebMercator. The requested box is snapped outwards once to 0.01° (as
 * {@code /image} and {@code /loop} do) and then widened to the map area's aspect, so the map is never
 * distorted; the result is {@code Foresignal-Image-Bounds}. Text uses the JDK's logical {@code SansSerif}
 * font (no font downloads) with antialiasing on. The PNG is 8-bit indexed when the image has at most 256
 * colors, else 24-bit.
 *
 * <p>{@link #render} runs the drawing on the {@link RenderQueue} at {@code INTERACTIVE} priority with a
 * {@value #BUDGET_MILLIS} ms budget and answers {@code 503 RENDER_BUSY} under load; {@link #draw} draws on
 * the calling thread.
 */
@Service
public class MapRenderer {

    private static final Logger log = LoggerFactory.getLogger(MapRenderer.class);

    /** The render budget, milliseconds. */
    public static final long BUDGET_MILLIS = 2000;

    /** The footer every map carries. */
    public static final String FOOTER = "Automated — not an official warning";

    /** The note suffix of a missing or stale product. */
    public static final String NO_COVERAGE = "not evidence of clear weather";

    /** The basemap credit. */
    public static final String BASEMAP_CREDIT = "Basemap: Natural Earth";

    /** The opacity of a product without a declared default. */
    static final double DEFAULT_OPACITY = 0.8;

    static final DateTimeFormatter UTC_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'", Locale.US);
    static final DateTimeFormatter LOCAL_TIME = DateTimeFormatter.ofPattern("HH:mm zzz", Locale.US);

    /**
     * A rendered map.
     *
     * @param png the PNG bytes
     * @param bounds the map area's bounds {@code w,s,e,n}
     * @param width the image width
     * @param height the image height
     * @param indexed true when written as 8-bit indexed PNG
     */
    public record Rendered(byte[] png, double[] bounds, int width, int height, boolean indexed) {

        /** The {@code Foresignal-Image-Bounds} value, {@code w,s,e,n}. */
        public String boundsHeader() {
            return fmt(bounds[0]) + "," + fmt(bounds[1]) + "," + fmt(bounds[2]) + "," + fmt(bounds[3]);
        }

        private static String fmt(double v) {
            return BigDecimal.valueOf(v).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        }
    }

    /** The colors of a theme. */
    record Theme(Color water, Color land, Color coast, Color admin0, Color admin1, Color text, Color muted,
            Color strip, Color halo) {

        static final Theme LIGHT = new Theme(new Color(0xCFE0EE), new Color(0xF3F1EC), new Color(0x7D8F9E),
                new Color(0x8C8C8C), new Color(0xBDBDBD), new Color(0x1F2328), new Color(0x5A6270),
                new Color(0xFFFFFF), new Color(255, 255, 255, 210));

        static final Theme DARK = new Theme(new Color(0x0F1B26), new Color(0x1F242A), new Color(0x6E8293),
                new Color(0x6A737D), new Color(0x3F474F), new Color(0xE6EDF3), new Color(0x9AA4AE),
                new Color(0x111418), new Color(17, 20, 24, 210));
    }

    private final BasemapStore basemap;
    private final RasterRenderService rasters;
    private final OperatorBasemap operator;
    private final RenderQueue queue;

    /** Creates the renderer. */
    public MapRenderer(BasemapStore basemap, RasterRenderService rasters, OperatorBasemap operator, RenderQueue queue) {
        this.basemap = basemap;
        this.rasters = rasters;
        this.operator = operator;
        this.queue = queue;
    }

    /**
     * Renders on the render pool at {@code INTERACTIVE} priority within {@value #BUDGET_MILLIS} ms.
     *
     * @param key the single-flight key (the response ETag)
     * @param client the fairness key
     * @throws ai.foresignal.api.error.ForesignalException {@code 503 RENDER_BUSY} when the queue sheds it
     */
    public Rendered render(MapSpec spec, String key, String client) {
        Rendered[] out = new Rendered[1];
        Supplier<byte[]> work = () -> {
            out[0] = draw(spec);
            return out[0].png();
        };
        byte[] png = queue.render("map:" + key, RenderQueue.Priority.INTERACTIVE, client, work,
                Duration.ofMillis(BUDGET_MILLIS));
        if (out[0] != null && out[0].png() == png) {
            return out[0];
        }
        // Another caller's job rendered the same key (single-flight): its bounds are ours too.
        double[] bounds = layout(spec).bounds();
        return new Rendered(png, bounds, spec.width(), spec.height(), false);
    }

    /** The geometry of the image: strips and the fitted map box. */
    record Layout(int headerHeight, int mapTop, int mapHeight, int legendTop, int legendHeight, int footerTop,
            int fontSize, double[] bounds) {}

    /** Draws on the calling thread. */
    public Rendered draw(MapSpec spec) {
        Layout layout = layout(spec);
        int w = spec.width();
        int h = spec.height();
        Theme theme = spec.dark() ? Theme.DARK : Theme.LIGHT;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setColor(theme.strip());
            g.fillRect(0, 0, w, h);
            Projection proj = new Projection(layout.bounds(), w, layout.mapHeight(), layout.mapTop());
            Shape oldClip = g.getClip();
            g.setClip(0, layout.mapTop(), w, layout.mapHeight());
            boolean operatorDrawn = operator.configured() && operator.draw(g, layout.bounds()[0], layout.bounds()[1],
                    layout.bounds()[2], layout.bounds()[3], w, layout.mapHeight(), layout.mapTop());
            if (!operatorDrawn) {
                g.setColor(theme.water());
                g.fillRect(0, layout.mapTop(), w, layout.mapHeight());
                fill(g, proj, BasemapStore.LAND, theme.land());
                fill(g, proj, BasemapStore.LAKES, theme.water());
            }
            for (MapSpec.Layer layer : spec.layers()) {
                overlay(g, proj, layer, layout);
            }
            if (!operatorDrawn) {
                float s = Math.max(1f, w / 800f);
                stroke(g, proj, BasemapStore.ADMIN1, theme.admin1(), new BasicStroke(0.6f * s));
                stroke(g, proj, BasemapStore.ADMIN0, theme.admin0(), new BasicStroke(1.0f * s, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10f, new float[] {4f * s, 2f * s}, 0f));
                stroke(g, proj, BasemapStore.COASTLINE, theme.coast(), new BasicStroke(0.9f * s));
            }
            if (spec.alert() != null) {
                alert(g, proj, spec.alert(), w);
            }
            rings(g, proj, spec, layout, theme);
            scaleBar(g, proj, spec, layout, theme);
            notes(g, spec, layout, theme);
            g.setClip(oldClip);
            header(g, spec, layout, theme);
            legend(g, spec, layout, theme);
            footer(g, spec, layout, theme);
        } finally {
            g.dispose();
        }
        int[] argb = image.getRGB(0, 0, w, h, null, 0, w);
        byte[] indexed = indexedPng(argb, w, h);
        byte[] png = indexed != null ? indexed : ImageEncoding.png(image);
        return new Rendered(png, layout.bounds(), w, h, indexed != null);
    }

    // ------------------------------------------------------------------ layout and projection

    /** The strips and the map box fitted to the map area's aspect. */
    static Layout layout(MapSpec spec) {
        int w = spec.width();
        int h = spec.height();
        int font = Math.max(9, Math.min(16, Math.round(w / 64f)));
        int line = font + Math.max(3, font / 3);
        int lines = Math.max(1, spec.layers().size());
        int header = 6 + lines * line;
        boolean legend = spec.layers().stream().anyMatch(MapRenderer::hasLegend);
        int legendHeight = legend ? font * 2 + 14 : 0;
        int footer = 2 * line + 6;
        int mapHeight = h - header - legendHeight - footer;
        if (mapHeight < h / 3) {
            // A short image keeps the map and drops the legend strip.
            legendHeight = 0;
            mapHeight = Math.max(8, h - header - footer);
        }
        int mapTop = header;
        return new Layout(header, mapTop, mapHeight, mapTop + mapHeight, legendHeight, mapTop + mapHeight + legendHeight,
                font, fit(LoopService.snap(normalize(spec.bbox()), true), w, mapHeight));
    }

    private static double[] normalize(double[] box) {
        return new double[] {box[0], box[1], box[2], box[3]};
    }

    /**
     * Widens a box (in WebMercator) to the aspect of a {@code width × height} area, keeping its center; the
     * latitudes stay inside WebMercator's range.
     */
    static double[] fit(double[] box, int width, int height) {
        double east = box[2] < box[0] ? box[2] + 360 : box[2];
        double x0 = WebMercator.x(box[0]);
        double x1 = WebMercator.x(east);
        double y0 = WebMercator.y(box[1]);
        double y1 = WebMercator.y(box[3]);
        double area = (double) width / height;
        double spanX = x1 - x0;
        double spanY = y1 - y0;
        if (spanX / spanY < area) {
            double grow = (spanY * area - spanX) / 2;
            x0 -= grow;
            x1 += grow;
        } else {
            double grow = (spanX / area - spanY) / 2;
            y0 -= grow;
            y1 += grow;
        }
        double limit = WebMercator.y(WebMercator.MAX_LAT);
        if (y1 > limit) {
            y0 -= y1 - limit;
            y1 = limit;
        }
        if (y0 < -limit) {
            y1 = Math.min(limit, y1 + (-limit - y0));
            y0 = -limit;
        }
        double west = WebMercator.lon(x0);
        double eastOut = WebMercator.lon(x1);
        if (x1 - x0 >= 2 * WebMercator.HALF) {
            west = -180;
            eastOut = 180;
        } else {
            west = wrap(west);
            eastOut = wrap(eastOut);
        }
        return new double[] {round6(west), round6(WebMercator.lat(y0)), round6(eastOut), round6(WebMercator.lat(y1))};
    }

    private static double wrap(double lon) {
        double out = ((lon + 180) % 360 + 360) % 360 - 180;
        return out == -180 && lon > 0 ? 180 : out;
    }

    private static double round6(double v) {
        return Math.round(v * 1e6) / 1e6;
    }

    /** WGS84 to image pixels for the map area. */
    static final class Projection {
        final double west;
        final double x0;
        final double x1;
        final double y0;
        final double y1;
        final int width;
        final int height;
        final int top;
        final boolean crosses;
        final double[] bounds;

        Projection(double[] bounds, int width, int height, int top) {
            this.bounds = bounds;
            this.west = bounds[0];
            this.crosses = bounds[2] < bounds[0];
            double east = crosses ? bounds[2] + 360 : bounds[2];
            this.x0 = WebMercator.x(bounds[0]);
            this.x1 = WebMercator.x(east);
            this.y0 = WebMercator.y(bounds[1]);
            this.y1 = WebMercator.y(bounds[3]);
            this.width = width;
            this.height = height;
            this.top = top;
        }

        double px(double lon) {
            double l = crosses && lon < west ? lon + 360 : lon;
            return (WebMercator.x(l) - x0) / (x1 - x0) * width;
        }

        double py(double lat) {
            return top + (y1 - WebMercator.y(lat)) / (y1 - y0) * height;
        }

        /** Meters on the ground per pixel at the map's center latitude. */
        double groundMetersPerPixel() {
            double lat = WebMercator.lat((y0 + y1) / 2);
            return (x1 - x0) / width * Math.cos(Math.toRadians(lat));
        }

        double centerLon() {
            double lon = WebMercator.lon((x0 + x1) / 2);
            return lon > 180 ? lon - 360 : lon;
        }

        double centerLat() {
            return WebMercator.lat((y0 + y1) / 2);
        }
    }

    // ------------------------------------------------------------------ basemap

    private void fill(Graphics2D g, Projection proj, String layerName, Color color) {
        BasemapStore.Layer layer = basemap.layer(layerName);
        g.setColor(color);
        for (BasemapStore.Part part : layer.query(proj.bounds[0], proj.bounds[1], proj.bounds[2], proj.bounds[3])) {
            Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
            for (int[] ring : part.rings()) {
                append(path, proj, ring, true);
            }
            g.fill(path);
        }
    }

    private void stroke(Graphics2D g, Projection proj, String layerName, Color color, BasicStroke stroke) {
        BasemapStore.Layer layer = basemap.layer(layerName);
        g.setColor(color);
        g.setStroke(stroke);
        for (BasemapStore.Part part : layer.query(proj.bounds[0], proj.bounds[1], proj.bounds[2], proj.bounds[3])) {
            Path2D.Double path = new Path2D.Double();
            for (int[] ring : part.rings()) {
                append(path, proj, ring, false);
            }
            g.draw(path);
        }
    }

    private static void append(Path2D.Double path, Projection proj, int[] ring, boolean close) {
        int n = ring.length / 2;
        if (n < 2) {
            return;
        }
        path.moveTo(proj.px(ring[0] / 1e6), proj.py(ring[1] / 1e6));
        for (int i = 1; i < n; i++) {
            path.lineTo(proj.px(ring[i * 2] / 1e6), proj.py(ring[i * 2 + 1] / 1e6));
        }
        if (close) {
            path.closePath();
        }
    }

    // ------------------------------------------------------------------ rasters

    private void overlay(Graphics2D g, Projection proj, MapSpec.Layer layer, Layout layout) {
        if (layer.frame() == null) {
            return;
        }
        double[] b = layout.bounds();
        try {
            RasterRenderService.ImageRequest request = new RasterRenderService.ImageRequest(b[0], b[1], b[2], b[3],
                    proj.width, proj.height, true, false);
            TileRenderer.Drawn drawn = rasters.drawDirect(layer.spec(), layer.frame(), layer.style(), request);
            if (drawn.empty()) {
                return;
            }
            int[] argb = drawn.pixels() != null ? ImageEncoding.toArgb(drawn.pixels(), drawn.style().palette())
                    : drawn.argb();
            BufferedImage over = new BufferedImage(drawn.width(), drawn.height(), BufferedImage.TYPE_INT_ARGB);
            over.setRGB(0, 0, drawn.width(), drawn.height(), argb, 0, drawn.width());
            Composite old = g.getComposite();
            double opacity = layer.spec().defaultOpacity() == null ? DEFAULT_OPACITY : layer.spec().defaultOpacity();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) Math.max(0.05, Math.min(1.0, opacity))));
            g.drawImage(over, 0, layout.mapTop(), null);
            g.setComposite(old);
        } catch (RuntimeException e) {
            // A frame whose file vanished draws the note instead (see notes); never a broken image.
            log.warn("Static map overlay {} {} failed: {}", layer.spec().id(), layer.frame().frameKey(), e.getMessage());
        }
    }

    /** True when a layer draws a legend: a frame and a non-passthrough style. */
    static boolean hasLegend(MapSpec.Layer layer) {
        return !layer.style().style().isPassthrough();
    }

    // ------------------------------------------------------------------ alert, place, scale

    /** The severity color, as the channel types use it. */
    static Color severityColor(String severity) {
        return switch (severity == null ? "info" : severity) {
            case "critical" -> new Color(0xDC2626);
            case "warning" -> new Color(0xF59E0B);
            default -> new Color(0x3B82F6);
        };
    }

    private static void alert(Graphics2D g, Projection proj, MapSpec.Alert alert, int width) {
        Color color = severityColor(alert.severity());
        Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);
        float s = Math.max(1f, width / 800f);
        g.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawGeometry(g, proj, alert.geometry(), color, fill, s);
    }

    private static void drawGeometry(Graphics2D g, Projection proj, Geometry geometry, Color color, Color fill,
            float s) {
        for (int k = 0; k < geometry.getNumGeometries(); k++) {
            Geometry part = geometry.getGeometryN(k);
            if (part != geometry && part.getNumGeometries() > 1) {
                drawGeometry(g, proj, part, color, fill, s);
                continue;
            }
            if (part instanceof Polygon polygon) {
                Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
                ring(path, proj, polygon.getExteriorRing().getCoordinates(), true);
                for (int r = 0; r < polygon.getNumInteriorRing(); r++) {
                    ring(path, proj, polygon.getInteriorRingN(r).getCoordinates(), true);
                }
                g.setColor(fill);
                g.fill(path);
                g.setColor(color);
                g.draw(path);
            } else if (part instanceof LineString line) {
                Path2D.Double path = new Path2D.Double();
                ring(path, proj, line.getCoordinates(), false);
                g.setColor(color);
                g.draw(path);
            } else if (part instanceof Point point && !point.isEmpty()) {
                double x = proj.px(point.getX());
                double y = proj.py(point.getY());
                double r = 7 * s;
                g.setColor(fill);
                g.fill(new Ellipse2D.Double(x - r * 2, y - r * 2, r * 4, r * 4));
                g.setColor(color);
                g.fill(new Ellipse2D.Double(x - r / 2, y - r / 2, r, r));
                g.draw(new Ellipse2D.Double(x - r * 2, y - r * 2, r * 4, r * 4));
            }
        }
    }

    private static void ring(Path2D.Double path, Projection proj, Coordinate[] coords, boolean close) {
        if (coords.length == 0) {
            return;
        }
        path.moveTo(proj.px(coords[0].x), proj.py(coords[0].y));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(proj.px(coords[i].x), proj.py(coords[i].y));
        }
        if (close) {
            path.closePath();
        }
    }

    private static void rings(Graphics2D g, Projection proj, MapSpec spec, Layout layout, Theme theme) {
        double lon;
        double lat;
        if (spec.place() != null) {
            lon = spec.place().lon();
            lat = spec.place().lat();
        } else if (!spec.rangeRingsKm().isEmpty()) {
            lon = proj.centerLon();
            lat = proj.centerLat();
        } else {
            return;
        }
        float s = Math.max(1f, spec.width() / 800f);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(9, layout.fontSize() - 1)));
        for (double km : spec.rangeRingsKm()) {
            Path2D.Double path = new Path2D.Double();
            for (int deg = 0; deg <= 360; deg += 4) {
                double[] p = destination(lon, lat, deg, km);
                if (deg == 0) {
                    path.moveTo(proj.px(p[0]), proj.py(p[1]));
                } else {
                    path.lineTo(proj.px(p[0]), proj.py(p[1]));
                }
            }
            g.setColor(theme.text());
            g.setStroke(new BasicStroke(1.2f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f,
                    new float[] {5f * s, 4f * s}, 0f));
            g.draw(path);
            double[] top = destination(lon, lat, 0, km);
            label(g, distanceLabel(km, spec.imperial()), (int) proj.px(top[0]) + 3, (int) proj.py(top[1]) - 3, theme);
        }
        if (spec.place() != null) {
            double x = proj.px(lon);
            double y = proj.py(lat);
            double r = 5 * s;
            g.setStroke(new BasicStroke(2f * s));
            g.setColor(theme.strip());
            g.fill(new Ellipse2D.Double(x - r - 2 * s, y - r - 2 * s, 2 * r + 4 * s, 2 * r + 4 * s));
            g.setColor(new Color(0x7C3AED));
            g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            String name = spec.place().name() == null ? spec.place().id() : spec.place().name();
            if (name != null) {
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, layout.fontSize()));
                label(g, name, (int) (x + r + 4 * s), (int) (y + layout.fontSize() / 2.5), theme);
            }
        }
    }

    /** The point {@code km} from {@code (lon, lat)} on {@code bearing} degrees (spherical Earth). */
    static double[] destination(double lon, double lat, double bearing, double km) {
        double d = km / 6371.0088;
        double b = Math.toRadians(bearing);
        double p1 = Math.toRadians(lat);
        double l1 = Math.toRadians(lon);
        double p2 = Math.asin(Math.sin(p1) * Math.cos(d) + Math.cos(p1) * Math.sin(d) * Math.cos(b));
        double l2 = l1 + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(p1), Math.cos(d) - Math.sin(p1) * Math.sin(p2));
        double outLon = Math.toDegrees(l2);
        outLon = ((outLon + 540) % 360) - 180;
        return new double[] {outLon, Math.toDegrees(p2)};
    }

    /** {@code 25 km} or {@code 15.5 mi}. */
    static String distanceLabel(double km, boolean imperial) {
        double v = imperial ? km / 1.609344 : km;
        String unit = imperial ? "mi" : "km";
        return UnitConversion.formatReading(v) + " " + unit;
    }

    private static void scaleBar(Graphics2D g, Projection proj, MapSpec spec, Layout layout, Theme theme) {
        double metersPerPixel = proj.groundMetersPerPixel();
        if (!(metersPerPixel > 0)) {
            return;
        }
        double unitMeters = spec.imperial() ? 1609.344 : 1000;
        double target = metersPerPixel * spec.width() / 5 / unitMeters;
        double nice = nice(target);
        int barPx = (int) Math.round(nice * unitMeters / metersPerPixel);
        if (barPx < 8 || barPx > spec.width() / 2) {
            return;
        }
        float s = Math.max(1f, spec.width() / 800f);
        int x = (int) (10 * s);
        int y = layout.mapTop() + layout.mapHeight() - (int) (10 * s);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(9, layout.fontSize() - 1)));
        String label = distanceLabel(spec.imperial() ? nice * 1.609344 : nice, spec.imperial());
        int labelWidth = g.getFontMetrics().stringWidth(label);
        g.setColor(theme.halo());
        g.fillRect(x - 4, y - layout.fontSize() - 6, barPx + labelWidth + 16, layout.fontSize() + 12);
        g.setColor(theme.text());
        g.setStroke(new BasicStroke(2f * s));
        g.drawLine(x, y, x + barPx, y);
        g.drawLine(x, y - (int) (4 * s), x, y);
        g.drawLine(x + barPx, y - (int) (4 * s), x + barPx, y);
        g.drawString(label, x + barPx + 6, y);
    }

    /** The largest 1, 2 or 5 × 10ⁿ at most {@code v}. */
    static double nice(double v) {
        if (!(v > 0)) {
            return 1;
        }
        double p = Math.pow(10, Math.floor(Math.log10(v)));
        double m = v / p;
        double n = m >= 5 ? 5 : m >= 2 ? 2 : 1;
        return n * p;
    }

    private static void notes(Graphics2D g, MapSpec spec, Layout layout, Theme theme) {
        List<String> notes = notesOf(spec);
        if (notes.isEmpty()) {
            return;
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, layout.fontSize()));
        FontMetrics fm = g.getFontMetrics();
        int y = layout.mapTop() + 6;
        for (String note : notes) {
            String text = fitText(note, fm, spec.width() - 24);
            int tw = fm.stringWidth(text);
            g.setColor(theme.halo());
            g.fillRect(6, y, tw + 12, fm.getHeight() + 6);
            g.setColor(new Color(0xB45309));
            g.drawRect(6, y, tw + 12, fm.getHeight() + 6);
            g.setColor(theme.text());
            g.drawString(text, 12, y + 3 + fm.getAscent());
            y += fm.getHeight() + 10;
        }
    }

    /** The notes of missing and stale layers, in layer order. */
    static List<String> notesOf(MapSpec spec) {
        List<String> out = new ArrayList<>();
        for (MapSpec.Layer layer : spec.layers()) {
            if (layer.status() == MapSpec.FrameStatus.UNAVAILABLE) {
                out.add(layer.name() + " unavailable — " + NO_COVERAGE);
            } else if (layer.status() == MapSpec.FrameStatus.STALE) {
                out.add(layer.name() + " stale" + (layer.frame() == null ? "" : " (frame "
                        + UTC_TIME.format(layer.frame().validTime().atZone(ZoneOffset.UTC)) + ")") + " — " + NO_COVERAGE);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ strips

    private static void header(Graphics2D g, MapSpec spec, Layout layout, Theme theme) {
        g.setColor(theme.strip());
        g.fillRect(0, 0, spec.width(), layout.headerHeight());
        int font = layout.fontSize();
        int line = font + Math.max(3, font / 3);
        List<String> lines = headerLines(spec);
        int y = 3 + font;
        for (int k = 0; k < lines.size(); k++) {
            g.setFont(new Font(Font.SANS_SERIF, k == 0 ? Font.BOLD : Font.PLAIN, font));
            g.setColor(theme.text());
            g.drawString(fitText(lines.get(k), g.getFontMetrics(), spec.width() - 12), 6, y);
            y += line;
        }
    }

    /** One header line per product: its name and frame valid time (UTC and the place's local time). */
    static List<String> headerLines(MapSpec spec) {
        List<String> out = new ArrayList<>();
        for (MapSpec.Layer layer : spec.layers()) {
            out.add(layer.name() + " — " + frameTime(layer.frame(), spec.zone()));
        }
        if (out.isEmpty()) {
            out.add(spec.place() != null && spec.place().name() != null ? spec.place().name() : "Foresignal map");
        }
        return out;
    }

    /** {@code 2026-10-12 21:04 UTC (16:04 CDT)}, or {@code unavailable}. */
    static String frameTime(RasterFrameRow frame, ZoneId zone) {
        if (frame == null) {
            return "unavailable";
        }
        Instant t = frame.validTime();
        String utc = UTC_TIME.format(t.atZone(ZoneOffset.UTC));
        if (zone == null || zone.normalized().equals(ZoneOffset.UTC)) {
            return utc + (frame.referenceTime() == null ? "" : runSuffix(frame));
        }
        return utc + " (" + LOCAL_TIME.format(t.atZone(zone)) + ")" + (frame.referenceTime() == null ? "" : runSuffix(frame));
    }

    private static String runSuffix(RasterFrameRow frame) {
        return ", run " + UTC_TIME.format(frame.referenceTime().atZone(ZoneOffset.UTC));
    }

    private static void legend(Graphics2D g, MapSpec spec, Layout layout, Theme theme) {
        if (layout.legendHeight() == 0) {
            return;
        }
        List<MapSpec.Layer> layers = spec.layers().stream().filter(MapRenderer::hasLegend).toList();
        int n = Math.min(3, layers.size());
        int pad = 6;
        int slot = (spec.width() - pad * (n + 1)) / n;
        int font = Math.max(8, layout.fontSize() - 2);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, font));
        FontMetrics fm = g.getFontMetrics();
        int barTop = layout.legendTop() + 4;
        int barHeight = Math.max(6, font - 1);
        for (int k = 0; k < n; k++) {
            MapSpec.Layer layer = layers.get(k);
            StyleDefinition style = layer.style().style();
            ProductSpec product = layer.spec();
            UnitConversion conv = UnitConversion.of(style.units() != null ? style.units() : product.units(),
                    spec.imperial());
            int x = pad + k * (slot + pad);
            String units = conv.label() == null ? "" : conv.label();
            int unitsWidth = units.isEmpty() ? 0 : fm.stringWidth(units) + 6;
            int barWidth = Math.max(10, slot - unitsWidth);
            List<int[]> segments = LegendService.segments(style);
            for (int i = 0; i < segments.size(); i++) {
                int x0 = x + (int) Math.round((double) barWidth * i / segments.size());
                int x1 = x + (int) Math.round((double) barWidth * (i + 1) / segments.size());
                g.setColor(new Color(segments.get(i)[0], true));
                g.fillRect(x0, barTop, Math.max(1, x1 - x0), barHeight);
            }
            g.setColor(theme.muted());
            g.drawRect(x, barTop, barWidth, barHeight);
            g.setColor(theme.text());
            if (!units.isEmpty()) {
                g.drawString(units, x + barWidth + 6, barTop + barHeight);
            }
            List<LegendService.Tick> ticks = LegendService.ticks(style, conv);
            int lastEnd = Integer.MIN_VALUE;
            for (int i = 0; i < ticks.size(); i++) {
                String label = ticks.get(i).label();
                if (label == null) {
                    continue;
                }
                int tx = ticks.size() == 1 ? x + barWidth / 2
                        : x + (int) Math.round(barWidth * (i + 0.5) / ticks.size());
                int lw = fm.stringWidth(label);
                int left = Math.max(x, Math.min(x + barWidth - lw, tx - lw / 2));
                if (left <= lastEnd + 2) {
                    continue;
                }
                g.drawString(label, left, barTop + barHeight + fm.getAscent() + 1);
                lastEnd = left + lw;
            }
        }
    }

    private static void footer(Graphics2D g, MapSpec spec, Layout layout, Theme theme) {
        int font = Math.max(8, layout.fontSize() - 2);
        int line = layout.fontSize() + Math.max(3, layout.fontSize() / 3);
        g.setColor(theme.strip());
        g.fillRect(0, layout.footerTop(), spec.width(), spec.height() - layout.footerTop());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, font));
        g.setColor(theme.muted());
        int y = layout.footerTop() + 2 + font;
        g.drawString(fitText(attribution(spec), g.getFontMetrics(), spec.width() - 12), 6, y);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font));
        g.setColor(theme.text());
        g.drawString(FOOTER, 6, y + line);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, font));
        String brand = "Foresignal";
        g.drawString(brand, spec.width() - 6 - g.getFontMetrics().stringWidth(brand), y + line);
    }

    /** The attribution line: each product's structured attribution once, then the basemap credit. */
    static String attribution(MapSpec spec) {
        List<String> parts = new ArrayList<>();
        Map<String, Boolean> seen = new HashMap<>();
        for (MapSpec.Layer layer : spec.layers()) {
            ProductSpec.Attribution a = layer.spec().attribution();
            String text = a == null || a.text() == null || a.text().isBlank() ? layer.spec().id() : a.text().strip();
            if (seen.putIfAbsent(text, true) == null) {
                parts.add(text);
            }
        }
        parts.add(BASEMAP_CREDIT);
        return "Data: " + String.join(" · ", parts);
    }

    private static void label(Graphics2D g, String text, int x, int y, Theme theme) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(theme.halo());
        g.fillRect(x - 2, y - fm.getAscent(), fm.stringWidth(text) + 4, fm.getHeight());
        g.setColor(theme.text());
        g.drawString(text, x, y);
    }

    /** {@code text} cut with an ellipsis to fit {@code max} pixels. */
    static String fitText(String text, FontMetrics fm, int max) {
        if (fm.stringWidth(text) <= max) {
            return text;
        }
        String out = text;
        while (out.length() > 1 && fm.stringWidth(out + "…") > max) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "…";
    }

    // ------------------------------------------------------------------ encoding

    /** 8-bit indexed PNG when the image has at most 256 colors, else null. */
    static byte[] indexedPng(int[] argb, int width, int height) {
        Map<Integer, Integer> palette = new HashMap<>();
        byte[] pixels = new byte[argb.length];
        for (int i = 0; i < argb.length; i++) {
            int color = argb[i] | 0xFF000000;
            Integer index = palette.get(color);
            if (index == null) {
                if (palette.size() == 256) {
                    return null;
                }
                index = palette.size();
                palette.put(color, index);
            }
            pixels[i] = (byte) (int) index;
        }
        int size = Math.max(2, palette.size());
        byte[] r = new byte[size];
        byte[] gr = new byte[size];
        byte[] b = new byte[size];
        for (Map.Entry<Integer, Integer> e : palette.entrySet()) {
            int c = e.getKey();
            r[e.getValue()] = (byte) (c >> 16);
            gr[e.getValue()] = (byte) (c >> 8);
            b[e.getValue()] = (byte) c;
        }
        IndexColorModel model = new IndexColorModel(8, size, r, gr, b);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, model);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, target, 0, pixels.length);
        return ImageEncoding.png(image);
    }
}
