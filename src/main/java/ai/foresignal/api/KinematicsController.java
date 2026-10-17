package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.kinematics.KinematicService;
import ai.foresignal.kinematics.KinematicService.RecordKey;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Kinematic CPA and ETA (prompt 114, task 3): reads under the caller's excluded sources and restriction
 * ({@link RestrictedRead}), so a record the caller cannot read is "not found". Errors: {@code 400 CPA_RANGE},
 * {@code 422 STATE_TOO_OLD}.
 */
@RestController
@Tag(name = "Kinematics")
@RequestMapping("/v1/kinematics")
public class KinematicsController {

    private final KinematicService kinematics;
    private final ReadCallerResolver callers;

    /** Creates the controller. */
    public KinematicsController(KinematicService kinematics, ReadCallerResolver callers) {
        this.kinematics = kinematics;
        this.callers = callers;
    }

    /** {@code getCpa}: the closest point of approach of two records. */
    @GetMapping("/cpa")
    @RestrictedRead
    @Operation(operationId = "getCpa", tags = "Kinematics")
    public Map<String, Object> getCpa(@RequestParam(required = false) String a, @RequestParam(required = false) String b,
            @RequestParam(required = false) String horizon, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        return kinematics.cpa(RecordKey.parse(a, "a"), RecordKey.parse(b, "b"), horizon(horizon, "horizon"),
                callers.excludedSources(caller), caller.restriction());
    }

    /** {@code getEta}: the ETA of a record to a place or a point. */
    @GetMapping("/eta")
    @RestrictedRead
    @Operation(operationId = "getEta", tags = "Kinematics")
    public Map<String, Object> getEta(@RequestParam(required = false) String record,
            @RequestParam(required = false) String to, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        boolean mayReadPlaces = caller.scope() != null && caller.scope().includes(Scope.AGENT);
        return kinematics.eta(RecordKey.parse(record, "record"), to, mayReadPlaces, callers.excludedSources(caller),
                caller.restriction());
    }

    /** {@code cpaBatch}: one subject against up to 500 records from a filter, top N by dcpa. */
    @PostMapping("/cpa/batch")
    @RestrictedRead
    @Operation(operationId = "cpaBatch", tags = "Kinematics")
    public Map<String, Object> cpaBatch(@RequestBody JsonNode body, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        BatchRequest b = parseBatch(body);
        return kinematics.batch(b.subject(), b.sources(), b.kinds(), b.bbox(), b.horizon(), b.maxDcpaM(), b.top(),
                callers.excludedSources(caller), caller.restriction());
    }

    /**
     * A parsed {@code CpaBatchRequest}.
     *
     * @param subject the subject record
     * @param sources the candidate sources (empty for any)
     * @param kinds the candidate kinds (empty for any)
     * @param bbox the candidate box {@code [minLon, minLat, maxLon, maxLat]}, or null
     * @param horizon the CPA horizon
     * @param maxDcpaM the largest dcpa kept, or null
     * @param top the number of results
     */
    public record BatchRequest(RecordKey subject, List<String> sources, List<String> kinds, double[] bbox,
            Duration horizon, Double maxDcpaM, int top) {}

    /**
     * Parses a {@code CpaBatchRequest}: a subject, a filter with at least one of kinds, sources or bbox, an optional
     * horizon (at most 24 h), {@code maxDcpaM} ≥ 0 and {@code top} 1 to 500.
     *
     * @throws ValidationException when it is not valid
     */
    public static BatchRequest parseBatch(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw ValidationException.forField("body", "must be a CpaBatchRequest object");
        }
        RecordKey subject = RecordKey.parse(text(body.get("subject")), "subject");
        JsonNode filter = body.get("filter");
        if (filter == null || !filter.isObject()) {
            throw ValidationException.forField("filter", "is required: an object with kinds, sources or bbox");
        }
        List<String> kinds = strings(filter.get("kinds"), "filter.kinds");
        List<String> sources = strings(filter.get("sources"), "filter.sources");
        double[] bbox = bbox(filter.get("bbox"));
        if (kinds.isEmpty() && sources.isEmpty() && bbox == null) {
            throw ValidationException.forField("filter", "needs at least one of kinds, sources or bbox");
        }
        Duration horizon = horizon(text(body.get("horizon")), "horizon");
        Double maxDcpa = null;
        JsonNode max = body.get("maxDcpaM");
        if (max != null && !max.isNull()) {
            if (!max.isNumber() || max.doubleValue() < 0) {
                throw ValidationException.forField("maxDcpaM", "must be a number ≥ 0");
            }
            maxDcpa = max.doubleValue();
        }
        int top = KinematicService.DEFAULT_TOP;
        JsonNode topNode = body.get("top");
        if (topNode != null && !topNode.isNull()) {
            if (!topNode.isIntegralNumber() || topNode.intValue() < 1 || topNode.intValue() > KinematicService.MAX_BATCH) {
                throw ValidationException.forField("top", "must be an integer from 1 to 500");
            }
            top = topNode.intValue();
        }
        return new BatchRequest(subject, sources, kinds, bbox, horizon, maxDcpa, top);
    }

    /** A positive horizon of at most 24 h; the default 2 h when absent. */
    static Duration horizon(String value, String field) {
        return KinematicService.parseHorizon(value, field);
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.isString() ? node.asString() : node.toString();
    }

    private static List<String> strings(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw ValidationException.forField(field, "must be an array of strings");
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(field, "must be an array of strings");
            }
            out.add(item.asString().strip());
        }
        return out;
    }

    private static double[] bbox(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            throw ValidationException.forField("filter.bbox", "must be [minLon, minLat, maxLon, maxLat]");
        }
        double[] out = new double[4];
        for (int i = 0; i < 4; i++) {
            if (!node.get(i).isNumber()) {
                throw ValidationException.forField("filter.bbox", "must be [minLon, minLat, maxLon, maxLat]");
            }
            out[i] = node.get(i).doubleValue();
        }
        if (out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90 || out[0] > out[2] || out[1] > out[3]) {
            throw ValidationException.forField("filter.bbox", "must be a valid WGS84 box with min ≤ max");
        }
        return out;
    }
}
