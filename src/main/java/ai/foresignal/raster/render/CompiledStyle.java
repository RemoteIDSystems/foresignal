package ai.foresignal.raster.render;

import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A style compiled for one product (prompt 045): a palette of at most 256 colors whose entry 0 is
 * always fully transparent, and the product's raw-code → palette-index table.
 *
 * <ul>
 *   <li>u8: a 256-entry table; u16 and i16: a 65,536-entry table (i16 offset by 32,768);
 *   <li>f32: no table, the index is computed per pixel from the value ({@link #indexOfValue});
 *   <li>rgba8 with a passthrough style: no palette, the frame's own colors.
 * </ul>
 * A raw code is first matched against the product's special codes (its {@code codes} named
 * {@code noCoverage}, {@code noEcho} or {@code noData}, and its {@code nodata} code, which is
 * {@code noData}), then, for a categorical style, against the categories; anything else is the
 * physical value {@code code × scale + offset} through the value ramp.
 */
public final class CompiledStyle {

    /** Colors sampled along a continuous ramp. */
    static final int CONTINUOUS_ENTRIES = 250;

    private final StyleDefinition style;
    private final RasterDtype dtype;
    private final int[] palette;
    private final byte[] table;
    private final int rampStart;
    private final int rampSize;
    private final double rampMin;
    private final double rampMax;
    private final Map<Integer, Integer> categoryIndex = new HashMap<>();
    private final Map<String, Integer> specialIndex = new HashMap<>();
    private final ProductSpec spec;

    private CompiledStyle(StyleDefinition style, ProductSpec spec) {
        this.style = style;
        this.spec = spec;
        this.dtype = spec.dtype();
        List<Integer> colors = new ArrayList<>();
        colors.add(0);
        int start = 1;
        double min = 0;
        double max = 0;
        int size = 0;
        if (!style.isPassthrough()) {
            switch (style.type()) {
                case StyleDefinition.CONTINUOUS -> {
                    List<StyleDefinition.Stop> stops = style.stops();
                    min = stops.get(0).value();
                    max = stops.get(stops.size() - 1).value();
                    size = stops.size() == 1 ? 1 : CONTINUOUS_ENTRIES;
                    for (int k = 0; k < size; k++) {
                        double v = size == 1 ? min : min + (max - min) * k / (size - 1);
                        colors.add(interpolate(stops, v));
                    }
                }
                case StyleDefinition.DISCRETE -> {
                    size = style.stops().size();
                    for (StyleDefinition.Stop stop : style.stops()) {
                        colors.add(StyleDefinition.argb(stop.color()));
                    }
                }
                default -> {
                    for (StyleDefinition.Category category : style.categories()) {
                        categoryIndex.put(category.code(), colors.size());
                        colors.add(StyleDefinition.argb(category.color()));
                    }
                }
            }
            for (StyleDefinition.Special special : style.specials()) {
                int argb = StyleDefinition.argb(special.color());
                if (argb >>> 24 == 0) {
                    specialIndex.put(special.kind(), 0);
                } else {
                    specialIndex.put(special.kind(), colors.size());
                    colors.add(argb);
                }
            }
        }
        if (colors.size() > 256) {
            throw new IllegalStateException("Style " + style.ref() + " needs " + colors.size() + " colors; at most 256");
        }
        this.palette = colors.stream().mapToInt(Integer::intValue).toArray();
        this.rampStart = start;
        this.rampSize = size;
        this.rampMin = min;
        this.rampMax = max;
        this.table = buildTable();
    }

    /**
     * Compiles {@code style} for {@code spec}.
     *
     * @throws IllegalStateException when the palette would exceed 256 colors
     */
    public static CompiledStyle compile(StyleDefinition style, ProductSpec spec) {
        return new CompiledStyle(style, spec);
    }

    private byte[] buildTable() {
        if (dtype == null || style.isPassthrough()) {
            return null;
        }
        return switch (dtype) {
            case U8 -> fill(256, 0);
            case U16 -> fill(65_536, 0);
            case I16 -> fill(65_536, -32_768);
            default -> null;
        };
    }

    private byte[] fill(int n, int base) {
        byte[] t = new byte[n];
        for (int k = 0; k < n; k++) {
            t[k] = (byte) indexOfCode(k + base);
        }
        return t;
    }

    /** The palette index of raw code {@code code}. */
    public int indexOfCode(double code) {
        String kind = specialKind(code);
        if (kind != null) {
            return specialIndex.getOrDefault(kind, 0);
        }
        if (StyleDefinition.CATEGORICAL.equals(style.type())) {
            return Double.isNaN(code) ? 0 : categoryIndex.getOrDefault((int) code, 0);
        }
        return indexOfValue(spec.physical(code));
    }

    /** The special kind of raw code {@code code}, or null. */
    String specialKind(double code) {
        if (Double.isNaN(code)) {
            return "noData";
        }
        if (spec.nodata() != null && code == spec.nodata()) {
            return "noData";
        }
        if (code == Math.rint(code) && Math.abs(code) < Integer.MAX_VALUE) {
            String meaning = spec.codes().get((int) code);
            if (meaning != null && StyleDefinition.SPECIAL_KINDS.contains(meaning)) {
                return meaning;
            }
        }
        return null;
    }

    /** True when raw code {@code code} is a special code (bilinear sampling leaves those alone). */
    public boolean isSpecial(double code) {
        return specialKind(code) != null;
    }

    /** The palette index of physical value {@code v} through the value ramp (0 for NaN). */
    public int indexOfValue(double v) {
        if (Double.isNaN(v)) {
            return specialIndex.getOrDefault("noData", 0);
        }
        switch (style.type()) {
            case StyleDefinition.CONTINUOUS -> {
                if (rampSize == 1 || rampMax == rampMin) {
                    return rampStart;
                }
                double t = (v - rampMin) / (rampMax - rampMin);
                t = Math.max(0, Math.min(1, t));
                return rampStart + (int) Math.round(t * (rampSize - 1));
            }
            case StyleDefinition.DISCRETE -> {
                List<StyleDefinition.Stop> stops = style.stops();
                if (v < stops.get(0).value()) {
                    return 0;
                }
                int found = 0;
                for (int k = 0; k < stops.size(); k++) {
                    if (stops.get(k).value() <= v) {
                        found = k;
                    }
                }
                return rampStart + found;
            }
            default -> {
                return categoryIndex.getOrDefault((int) Math.rint(v), 0);
            }
        }
    }

    /** The code → index table (u8, u16, i16), or null. */
    public byte[] table() {
        return table;
    }

    /** The palette, ARGB; entry 0 is transparent. */
    public int[] palette() {
        return palette.clone();
    }

    /** The color of palette index {@code index}, ARGB. */
    public int color(int index) {
        return palette[index];
    }

    /** True when the frame's own rgba8 colors are drawn. */
    public boolean passthrough() {
        return style.isPassthrough() || dtype == RasterDtype.RGBA8;
    }

    /** The style. */
    public StyleDefinition style() {
        return style;
    }

    /** The product. */
    public ProductSpec spec() {
        return spec;
    }

    /** An 8-bit {@link IndexColorModel} over the palette, with alpha (written as PNG tRNS). */
    public IndexColorModel colorModel() {
        int n = Math.max(2, palette.length);
        byte[] r = new byte[n];
        byte[] g = new byte[n];
        byte[] b = new byte[n];
        byte[] a = new byte[n];
        for (int k = 0; k < palette.length; k++) {
            int c = palette[k];
            a[k] = (byte) (c >>> 24);
            r[k] = (byte) (c >>> 16);
            g[k] = (byte) (c >>> 8);
            b[k] = (byte) c;
        }
        return new IndexColorModel(8, n, r, g, b, a);
    }

    /** The color at {@code v} along continuous stops (RGBA interpolated linearly). */
    static int interpolate(List<StyleDefinition.Stop> stops, double v) {
        if (v <= stops.get(0).value()) {
            return StyleDefinition.argb(stops.get(0).color());
        }
        for (int k = 1; k < stops.size(); k++) {
            StyleDefinition.Stop hi = stops.get(k);
            if (v <= hi.value()) {
                StyleDefinition.Stop lo = stops.get(k - 1);
                double t = hi.value() == lo.value() ? 1 : (v - lo.value()) / (hi.value() - lo.value());
                return mix(StyleDefinition.argb(lo.color()), StyleDefinition.argb(hi.color()), t);
            }
        }
        return StyleDefinition.argb(stops.get(stops.size() - 1).color());
    }

    private static int mix(int c0, int c1, double t) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = c0 >>> shift & 0xFF;
            int b = c1 >>> shift & 0xFF;
            out |= (int) Math.round(a + (b - a) * t) << shift;
        }
        return out;
    }
}
