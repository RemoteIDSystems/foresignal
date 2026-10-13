package ai.foresignal.source.adapters.satdump;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses SatDump's {@code /api} JSON (prompt 049), written from the observed output of the legacy CLI
 * web server; no SatDump code.
 *
 * <p>Two shapes exist (SatDump 2.0.0-alpha, verified in the local checkout):
 *
 * <ul>
 *   <li>{@code legacy live}: the module statistics map at top level, {@code {moduleId: {...}}};
 *   <li>{@code legacy autotrack}: {@code {live_pipeline: {moduleId: {...}}, object_tracker: {...},
 *       frequency, vfos?, recording?}}.
 * </ul>
 *
 * <p>Parsing is lenient: unknown keys are ignored, and a number may arrive as a string or an array (the
 * best element is used for SNR, the mean for BER and Reed-Solomon). What the parser checks is the
 * skeleton the pinned-version fixture has: in autotrack, an {@code object_tracker} object with
 * {@code object_name}, {@code sat_current_pos} and {@code next_aos_time}; in live, objects only. A
 * response that breaks it carries {@link #DRIFT_REASON}.
 */
public final class SatDumpApiParser {

    /** The health reason of a response that does not match the pinned shape. */
    public static final String DRIFT_REASON = "SatDump /api shape changed (version drift?)";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SatDumpApiParser() {}

    /**
     * Parses one response body.
     *
     * @return the snapshot; {@link ApiSnapshot#driftReason()} is set when the body is not JSON or does
     *     not match the pinned shape
     */
    public static ApiSnapshot parse(String body) {
        JsonNode root;
        try {
            root = JSON.readTree(body == null ? "" : body);
        } catch (JacksonException e) {
            return drift(ApiSnapshot.LIVE);
        }
        if (root == null || !root.isObject()) {
            return drift(ApiSnapshot.LIVE);
        }
        JsonNode tracker = root.get("object_tracker");
        if (tracker != null) {
            return autotrack(root, tracker);
        }
        return live(root);
    }

    private static ApiSnapshot live(JsonNode root) {
        Map<String, JsonNode> modules = new TreeMap<>();
        boolean shapeOk = true;
        for (Map.Entry<String, JsonNode> field : root.properties()) {
            if (field.getValue().isObject()) {
                modules.put(field.getKey(), field.getValue());
            } else {
                shapeOk = false;
            }
        }
        if (root.has("live_pipeline")) {
            // An autotrack body without its tracker.
            shapeOk = false;
        }
        return fold(ApiSnapshot.LIVE, null, null, null, null, null, null, null, modules,
                shapeOk ? null : DRIFT_REASON);
    }

    private static ApiSnapshot autotrack(JsonNode root, JsonNode tracker) {
        boolean shapeOk = tracker.isObject() && tracker.has("object_name") && tracker.path("sat_current_pos").isObject()
                && tracker.has("next_aos_time");
        Map<String, JsonNode> modules = new TreeMap<>();
        collectModules(root.get("live_pipeline"), "", modules);
        JsonNode vfos = root.get("vfos");
        if (vfos != null && vfos.isObject()) {
            for (Map.Entry<String, JsonNode> vfo : vfos.properties()) {
                collectModules(vfo.getValue().get("live_pipeline"), vfo.getKey() + "/", modules);
            }
        }
        String name = text(tracker.get("object_name"));
        String target = name == null || name.isBlank() || name.equalsIgnoreCase("None") ? null : name.trim();
        JsonNode pos = tracker.path("sat_current_pos");
        Double az = number(pos.get("az"), Agg.FIRST);
        Double el = number(pos.get("el"), Agg.FIRST);
        Double frequency = number(root.get("frequency"), Agg.FIRST);
        Double aos = number(tracker.get("next_aos_time"), Agg.FIRST);
        Double los = number(tracker.get("next_los_time"), Agg.FIRST);
        Boolean rotator = bool(tracker.get("rotator_engaged"));
        return fold(ApiSnapshot.AUTOTRACK, frequency == null ? null : Math.round(frequency), target, az, el,
                epoch(aos), epoch(los), rotator, modules, shapeOk ? null : DRIFT_REASON);
    }

    private static void collectModules(JsonNode pipeline, String prefix, Map<String, JsonNode> into) {
        if (pipeline == null || !pipeline.isObject()) {
            return;
        }
        for (Map.Entry<String, JsonNode> module : pipeline.properties()) {
            if (module.getValue().isObject()) {
                into.put(prefix + module.getKey(), module.getValue());
            }
        }
    }

    private static ApiSnapshot fold(String mode, Long frequency, String target, Double az, Double el, Instant aos,
            Instant los, Boolean rotator, Map<String, JsonNode> modules, String drift) {
        Double snr = null;
        Double peak = null;
        Double ber = null;
        String deframer = null;
        boolean locked = false;
        double rsSum = 0;
        int rsCount = 0;
        for (JsonNode stats : modules.values()) {
            snr = max(snr, number(stats.get("snr"), Agg.MAX));
            peak = max(peak, number(stats.get("peak_snr"), Agg.MAX));
            Double moduleBer = number(stats.get("viterbi_ber"), Agg.MEAN);
            if (moduleBer != null) {
                ber = ber == null ? moduleBer : Math.min(ber, moduleBer);
            }
            String state = text(stats.get("deframer_state"));
            if (state != null) {
                if (synced(state)) {
                    deframer = "SYNCED";
                } else if (deframer == null) {
                    deframer = state.trim().toUpperCase(Locale.ROOT);
                }
            }
            Double rs = number(stats.get("rs_avg"), Agg.MEAN);
            if (rs != null) {
                rsSum += rs;
                rsCount++;
            }
            locked |= (state != null && synced(state)) || truthy(stats.get("viterbi_lock"))
                    || truthy(stats.get("lock_state")) || truthy(stats.get("deframer_lock"));
        }
        return new ApiSnapshot(mode, frequency, target, az, el, aos, los, rotator, new ArrayList<>(modules.keySet()),
                snr, peak, ber, deframer, locked, rsCount == 0 ? null : rsSum / rsCount, drift);
    }

    private static ApiSnapshot drift(String mode) {
        return new ApiSnapshot(mode, null, null, null, null, null, null, null, List.of(), null, null, null, null, false,
                null, DRIFT_REASON);
    }

    /** How an array of numbers is reduced. */
    enum Agg { FIRST, MAX, MEAN }

    /** A number from a number, a numeric string or an array of them; null when there is none. */
    static Double number(JsonNode node, Agg agg) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            double v = node.asDouble();
            return Double.isFinite(v) ? v : null;
        }
        if (node.isString()) {
            try {
                double v = Double.parseDouble(node.asString().trim());
                return Double.isFinite(v) ? v : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (node.isArray()) {
            Double result = null;
            double sum = 0;
            int count = 0;
            for (JsonNode element : node) {
                Double v = number(element, Agg.FIRST);
                if (v == null) {
                    continue;
                }
                if (agg == Agg.FIRST && result == null) {
                    result = v;
                }
                if (agg == Agg.MAX) {
                    result = result == null ? v : Math.max(result, v);
                }
                sum += v;
                count++;
            }
            if (agg == Agg.MEAN) {
                return count == 0 ? null : sum / count;
            }
            return result;
        }
        return null;
    }

    private static boolean truthy(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isString()) {
            String s = node.asString().trim();
            return synced(s) || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("locked");
        }
        Double v = number(node, Agg.MAX);
        return v != null && v > 0;
    }

    private static Boolean bool(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return truthy(node);
    }

    private static boolean synced(String state) {
        return state.trim().equalsIgnoreCase("SYNCED");
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        return node.asString();
    }

    private static Instant epoch(Double seconds) {
        if (seconds == null || seconds <= 0) {
            return null;
        }
        return Instant.ofEpochMilli(Math.round(seconds * 1000));
    }

    private static Double max(Double a, Double b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : Math.max(a, b);
    }
}
