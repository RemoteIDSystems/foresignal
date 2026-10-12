package ai.foresignal.raster.render;

import ai.foresignal.source.spi.ProductSpec;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Product legends (prompt 045; Weather-Imagery-Design §6.3): the JSON document native clients draw
 * from, and a PNG drawn with Java2D in the JDK's logical {@code SansSerif} font (no external fonts),
 * horizontal or vertical, on a dark or light background.
 */
@Service
public class LegendService {

    /** Default PNG width, pixels. */
    public static final int DEFAULT_WIDTH = 256;

    /** Height of a horizontal PNG, pixels. */
    public static final int HORIZONTAL_HEIGHT = 48;

    /** Height of a vertical PNG, pixels. */
    public static final int VERTICAL_HEIGHT = 256;

    /** Ticks along a continuous ramp. */
    static final int CONTINUOUS_TICKS = 5;

    /** One tick: a value in display units and its label. */
    record Tick(double value, String label) {}

    /**
     * The JSON legend ({@code Legend}).
     *
     * @param imperial {@code units=imperial}
     */
    public Map<String, Object> legend(ProductSpec spec, StyleDefinition style, boolean imperial) {
        UnitConversion conv = UnitConversion.of(style.units() != null ? style.units() : spec.units(), imperial);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("productId", spec.id());
        out.put("styleId", style.id());
        out.put("styleVersion", style.version());
        out.put("type", style.type());
        out.put("units", conv.units());
        out.put("unitsLabel", conv.label());
        List<Map<String, Object>> stops = new ArrayList<>();
        for (StyleDefinition.Stop stop : style.stops()) {
            Map<String, Object> m = new LinkedHashMap<>();
            double v = conv.apply(stop.value());
            m.put("value", round(v));
            m.put("color", stop.color());
            m.put("label", conv.converts() || stop.label() == null ? UnitConversion.format(v) : stop.label());
            stops.add(m);
        }
        out.put("stops", stops);
        List<Map<String, Object>> ticks = new ArrayList<>();
        for (Tick t : ticks(style, conv)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("value", round(t.value()));
            m.put("label", t.label());
            ticks.add(m);
        }
        out.put("ticks", ticks);
        Map<String, Object> styleMap = style.toMap(true);
        out.put("categories", styleMap.get("categories"));
        out.put("specials", styleMap.get("specials"));
        List<Map<String, Object>> meaning = new ArrayList<>();
        for (StyleDefinition.Meaning mn : style.meaning()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("min", mn.min() == null ? null : round(conv.apply(mn.min())));
            m.put("max", mn.max() == null ? null : round(conv.apply(mn.max())));
            m.put("text", mn.text());
            meaning.add(m);
        }
        out.put("meaning", meaning);
        return out;
    }

    /** The ticks: every stop of a discrete style, evenly spaced values along a continuous one. */
    static List<Tick> ticks(StyleDefinition style, UnitConversion conv) {
        List<Tick> out = new ArrayList<>();
        List<StyleDefinition.Stop> stops = style.stops();
        if (StyleDefinition.CATEGORICAL.equals(style.type())) {
            for (StyleDefinition.Category c : style.categories()) {
                out.add(new Tick(c.code(), c.label()));
            }
            return out;
        }
        if (stops.isEmpty()) {
            return out;
        }
        if (StyleDefinition.DISCRETE.equals(style.type()) || stops.size() == 1) {
            for (StyleDefinition.Stop s : stops) {
                double v = conv.apply(s.value());
                out.add(new Tick(v, UnitConversion.format(v)));
            }
            return out;
        }
        double min = stops.get(0).value();
        double max = stops.get(stops.size() - 1).value();
        for (int k = 0; k < CONTINUOUS_TICKS; k++) {
            double v = conv.apply(min + (max - min) * k / (CONTINUOUS_TICKS - 1));
            out.add(new Tick(v, UnitConversion.format(v)));
        }
        return out;
    }

    /**
     * The PNG legend.
     *
     * @param width pixels (64–2048)
     * @param vertical vertical layout (else horizontal)
     * @param dark dark theme (else light)
     */
    public byte[] png(ProductSpec spec, StyleDefinition style, boolean imperial, int width, boolean vertical,
            boolean dark) {
        UnitConversion conv = UnitConversion.of(style.units() != null ? style.units() : spec.units(), imperial);
        int height = vertical ? VERTICAL_HEIGHT : HORIZONTAL_HEIGHT;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(dark ? new Color(0x1E1E1E) : Color.WHITE);
            g.fillRect(0, 0, width, height);
            Color text = dark ? Color.WHITE : Color.BLACK;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            FontMetrics fm = g.getFontMetrics();
            List<int[]> segments = segments(style);
            List<Tick> ticks = ticks(style, conv);
            if (vertical) {
                int top = 16;
                int bottom = height - 8;
                drawBar(g, segments, 8, top, 16, bottom - top, true);
                g.setColor(text);
                if (conv.label() != null) {
                    g.drawString(conv.label(), 8, 12);
                }
                for (int k = 0; k < ticks.size(); k++) {
                    int y = ticks.size() == 1 ? (top + bottom) / 2
                            : bottom - (int) Math.round((bottom - top) * (k + 0.5) / ticks.size());
                    g.drawString(ticks.get(k).label(), 30, y + fm.getAscent() / 2);
                }
            } else {
                int left = 4;
                int right = width - 4;
                drawBar(g, segments, left, 6, right - left, 16, false);
                g.setColor(text);
                for (int k = 0; k < ticks.size(); k++) {
                    String label = ticks.get(k).label();
                    int x = ticks.size() == 1 ? (left + right) / 2
                            : left + (int) Math.round((right - left) * (k + 0.5) / ticks.size());
                    int w = fm.stringWidth(label);
                    g.drawString(label, Math.max(0, Math.min(width - w, x - w / 2)), 36);
                }
                if (conv.label() != null) {
                    int w = fm.stringWidth(conv.label());
                    g.drawString(conv.label(), Math.max(0, width - w - 4), height - 2);
                }
            }
        } finally {
            g.dispose();
        }
        return ImageEncoding.png(image);
    }

    /** The bar's colors, one ARGB per segment. */
    static List<int[]> segments(StyleDefinition style) {
        List<int[]> out = new ArrayList<>();
        if (StyleDefinition.CATEGORICAL.equals(style.type())) {
            for (StyleDefinition.Category c : style.categories()) {
                out.add(new int[] {StyleDefinition.argb(c.color())});
            }
        } else if (StyleDefinition.DISCRETE.equals(style.type())) {
            for (StyleDefinition.Stop s : style.stops()) {
                out.add(new int[] {StyleDefinition.argb(s.color())});
            }
        } else if (!style.stops().isEmpty()) {
            double min = style.stops().get(0).value();
            double max = style.stops().get(style.stops().size() - 1).value();
            int n = 64;
            for (int k = 0; k < n; k++) {
                out.add(new int[] {CompiledStyle.interpolate(style.stops(), min + (max - min) * k / (n - 1))});
            }
        }
        return out;
    }

    private static void drawBar(Graphics2D g, List<int[]> segments, int x, int y, int w, int h, boolean vertical) {
        if (segments.isEmpty()) {
            return;
        }
        int n = segments.size();
        for (int k = 0; k < n; k++) {
            g.setColor(new Color(segments.get(k)[0], true));
            if (vertical) {
                int y0 = y + h - (int) Math.round((double) h * (k + 1) / n);
                int y1 = y + h - (int) Math.round((double) h * k / n);
                g.fillRect(x, y0, w, Math.max(1, y1 - y0));
            } else {
                int x0 = x + (int) Math.round((double) w * k / n);
                int x1 = x + (int) Math.round((double) w * (k + 1) / n);
                g.fillRect(x0, y, Math.max(1, x1 - x0), h);
            }
        }
    }

    private static double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }
}
