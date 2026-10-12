package ai.foresignal.raster;

import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON form of a {@link ProductSpec} stored in {@code raster_product.spec}: durations as
 * shorthand ({@code 2m}, {@code 6h}), codes keyed by their decimal string, nulls left out.
 */
public final class ProductSpecJson {

    private ProductSpecJson() {}

    /** The spec as a JSON-ready map. */
    public static Map<String, Object> toMap(ProductSpec spec) {
        Map<String, Object> out = new LinkedHashMap<>();
        put(out, "id", spec.id());
        put(out, "title", spec.title());
        put(out, "kind", spec.kind());
        put(out, "category", spec.category());
        put(out, "units", spec.units());
        put(out, "dtype", spec.dtype() == null ? null : spec.dtype().wireValue());
        out.put("scale", spec.scale());
        out.put("offset", spec.offset());
        put(out, "nodata", spec.nodata());
        if (!spec.codes().isEmpty()) {
            Map<String, Object> codes = new LinkedHashMap<>();
            spec.codes().forEach((code, meaning) -> codes.put(Integer.toString(code), meaning));
            out.put("codes", codes);
        }
        if (!spec.bands().isEmpty()) {
            out.put("bands", spec.bands());
        }
        if (!spec.styles().isEmpty()) {
            out.put("styles", spec.styles());
        }
        put(out, "defaultStyle", spec.defaultStyle());
        put(out, "defaultOpacity", spec.defaultOpacity());
        if (spec.temporal() != null) {
            Map<String, Object> t = new LinkedHashMap<>();
            put(t, "type", spec.temporal().type());
            put(t, "cadence", format(spec.temporal().cadence()));
            put(t, "maxGap", format(spec.temporal().maxGap()));
            out.put("temporal", t);
        }
        if (spec.spatial() != null) {
            Map<String, Object> s = new LinkedHashMap<>();
            put(s, "coverage", spec.spatial().coverage());
            put(s, "nativeMaxZoom", spec.spatial().nativeMaxZoom());
            put(s, "minZoom", spec.spatial().minZoom());
            put(s, "maxZoomForAnimation", spec.spatial().maxZoomForAnimation());
            put(s, "bounds", spec.spatial().bounds());
            out.put("spatial", s);
        }
        if (spec.retention() != null) {
            out.put("retention", retention(spec.retention()));
        }
        if (spec.attribution() != null) {
            out.put("attribution", attribution(spec.attribution()));
        }
        put(out, "coverageMeaning", spec.coverageMeaning());
        put(out, "legendSummary", spec.legendSummary());
        put(out, "resample", spec.resample());
        return out;
    }

    /** The ladder as the API's {@code temporal.retention} object. */
    public static Map<String, Object> retention(ProductSpec.RetentionLadder ladder) {
        Map<String, Object> r = new LinkedHashMap<>();
        List<Map<String, Object>> rungs = new ArrayList<>();
        for (ProductSpec.RetentionLadder.Rung rung : ladder.rungs()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("olderThan", format(rung.olderThan()));
            m.put("keepEvery", format(rung.keepEvery()));
            rungs.add(m);
        }
        r.put("ladder", rungs);
        put(r, "maxAge", format(ladder.maxAge()));
        put(r, "runs", ladder.runs());
        r.put("summary", ladder.summary());
        return r;
    }

    /** The structured attribution as the API's {@code Attribution} object. */
    public static Map<String, Object> attribution(ProductSpec.Attribution a) {
        Map<String, Object> m = new LinkedHashMap<>();
        put(m, "text", a.text());
        put(m, "html", a.html());
        put(m, "url", a.url());
        put(m, "licence", a.licence());
        put(m, "licenceUrl", a.licenceUrl());
        m.put("requiredDisplay", a.requiredDisplay());
        return m;
    }

    /** A duration in shorthand, or null. */
    public static String format(Duration d) {
        return d == null ? null : Durations.format(d);
    }

    private static void put(Map<String, Object> out, String key, Object value) {
        if (value != null) {
            out.put(key, value);
        }
    }
}
