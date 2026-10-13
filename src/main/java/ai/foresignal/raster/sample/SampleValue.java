package ai.foresignal.raster.sample;

import ai.foresignal.raster.render.UnitConversion;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One product value at a point (prompt 052; OpenAPI {@code SampleValue}).
 *
 * @param productId the product
 * @param value the physical value in the requested units; null for u/v products and non-ok statuses
 * @param u the u component of a u/v field, else null
 * @param v the v component of a u/v field, else null
 * @param derived {@code {speedMs, fromDeg, compass}} of a u/v field, else null
 * @param units the units of {@code value}
 * @param label a human label, such as {@code 52.5 dBZ} or {@code no echo}; null when not ok
 * @param frameKey the frame read, or null
 * @param validTime the frame's valid time, or null
 * @param localTime the valid time in the place's zone (else UTC), ISO-8601 with offset, or null
 * @param ageMinutes minutes from the valid time to now (negative for a future step), or null
 * @param run the forecast run, or null
 * @param step the forecast step in hours, or null
 * @param method {@code nearest} or {@code bilinear}
 * @param coverage the product's coverage ({@code CONUS}, {@code global}, …)
 * @param status the status
 * @param statusText why, for a non-ok status
 * @param attribution the structured attribution
 */
public record SampleValue(String productId, Double value, Double u, Double v, Map<String, Object> derived,
        String units, String label, String frameKey, Instant validTime, String localTime, Double ageMinutes,
        String run, Integer step, String method, String coverage, SampleStatus status, String statusText,
        Map<String, Object> attribution) {

    /** A value with no frame behind it. */
    public static SampleValue unknown(ProductSpec spec, SampleStatus status, String text, boolean imperial) {
        UnitConversion conversion = UnitConversion.of(spec.units(), imperial);
        return new SampleValue(spec.id(), null, null, null, null, conversion.units(), null, null, null, null, null,
                null, null, spec.bilinear() ? ProductSpec.RESAMPLE_BILINEAR : ProductSpec.RESAMPLE_NEAREST,
                spec.spatial() == null ? null : spec.spatial().coverage(), status,
                text == null ? status.defaultText() : text, SampleService.attribution(spec));
    }

    /** The {@code SampleValue} JSON object. */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", productId);
        m.put("value", value);
        m.put("u", u);
        m.put("v", v);
        m.put("derived", derived);
        m.put("units", units);
        m.put("label", label);
        m.put("frameKey", frameKey);
        m.put("validTime", validTime == null ? null : validTime.toString());
        m.put("localTime", localTime);
        m.put("ageMinutes", ageMinutes);
        m.put("run", run);
        m.put("step", step);
        m.put("method", method);
        if (coverage != null) {
            m.put("coverage", coverage);
        }
        m.put("status", status.wireValue());
        m.put("statusText", statusText);
        m.put("attribution", attribution);
        return m;
    }
}
