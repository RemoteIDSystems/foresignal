package ai.foresignal.share;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The privacy mask and field allowlist of a share link (prompt 112).
 *
 * <ul>
 *   <li><em>Precision:</em> coordinates are snapped to a grid of {@code precisionM} meters (in degrees of
 *       latitude, {@code precisionM / 111,320}), which also coarsens lines and polygons.
 *   <li><em>Privacy mask</em> (the default for personal or private sources, 039): observer, device and
 *       address-like fields are removed and the payload is reduced to {@code title}, {@code kind} and
 *       {@code observedAt} unless the creator passed an explicit allowlist; precision is capped to
 *       {@value #PRIVATE_PRECISION_M} m.
 *   <li><em>Allowlist</em> ({@code field_mask}): only the listed payload keys are kept, applied after license
 *       filtering.
 * </ul>
 */
public final class ShareMask {

    /** The precision cap of the privacy mask, in meters. */
    public static final int PRIVATE_PRECISION_M = 1_000;

    /** Meters per degree of latitude. */
    static final double METERS_PER_DEGREE = 111_320.0;

    /** The payload keys a masked payload keeps when no allowlist is given. */
    public static final Set<String> MASKED_KEEP = Set.of("title", "kind", "observedAt");

    /** Keys that identify an observer, a device or an address; removed by the privacy mask. */
    static final Pattern PERSONAL_KEY = Pattern.compile(
            "(?i)^(observer.*|device.*|.*address.*|street.*|house.*|postcode|postal.*|zip.*|phone.*|e?mail.*"
                    + "|ip|ipv[46].*|mac|imei|imsi|serial.*|owner.*|operator(position|lat|lon).*|home.*)$");

    private ShareMask() {}

    /**
     * Applies the mask to a payload.
     *
     * @param payload the payload after license filtering
     * @param masked true for the privacy mask
     * @param allowlist the creator's payload allowlist, or null
     * @return a new map
     */
    public static Map<String, Object> payload(Map<String, Object> payload, boolean masked, List<String> allowlist) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (payload == null) {
            return out;
        }
        for (Map.Entry<String, Object> e : payload.entrySet()) {
            String key = e.getKey();
            if (masked && PERSONAL_KEY.matcher(key).matches()) {
                continue;
            }
            if (allowlist != null) {
                if (!allowlist.contains(key)) {
                    continue;
                }
            } else if (masked && !MASKED_KEEP.contains(key)) {
                continue;
            }
            out.put(key, e.getValue());
        }
        return out;
    }

    /**
     * Snaps every coordinate of a GeoJSON geometry (as a map) to {@code precisionM} meters; null precision returns
     * the geometry unchanged.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> geometry(Map<String, Object> geometry, Integer precisionM) {
        if (geometry == null || precisionM == null || precisionM <= 0) {
            return geometry;
        }
        Map<String, Object> out = new LinkedHashMap<>(geometry);
        if (geometry.get("coordinates") != null) {
            out.put("coordinates", coordinates(geometry.get("coordinates"), precisionM));
        }
        if (geometry.get("geometries") instanceof List<?> list) {
            List<Object> parts = new ArrayList<>();
            for (Object part : list) {
                parts.add(part instanceof Map<?, ?> m ? geometry((Map<String, Object>) m, precisionM) : part);
            }
            out.put("geometries", parts);
        }
        return out;
    }

    /** Snaps a coordinate array (any nesting) to the grid. */
    public static Object coordinates(Object value, int precisionM) {
        if (value instanceof List<?> list) {
            if (!list.isEmpty() && list.getFirst() instanceof Number) {
                List<Object> point = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    Object v = list.get(i);
                    point.add(i < 2 && v instanceof Number n ? snap(n.doubleValue(), precisionM) : v);
                }
                return point;
            }
            List<Object> out = new ArrayList<>();
            for (Object v : list) {
                out.add(coordinates(v, precisionM));
            }
            return out;
        }
        return value;
    }

    /** One coordinate snapped to a grid of {@code precisionM} meters. */
    public static double snap(double degrees, int precisionM) {
        double step = precisionM / METERS_PER_DEGREE;
        double snapped = Math.round(degrees / step) * step;
        return Math.round(snapped * 1e6) / 1e6;
    }

    /** True when {@code key} is observer-, device- or address-like. */
    public static boolean personal(String key) {
        return key != null && PERSONAL_KEY.matcher(key.toLowerCase(Locale.ROOT)).matches();
    }
}
