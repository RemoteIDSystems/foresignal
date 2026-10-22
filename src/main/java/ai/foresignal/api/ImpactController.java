package ai.foresignal.api;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.impact.ImpactPairs;
import ai.foresignal.impact.ImpactQueryService;
import ai.foresignal.impact.ImpactViews;
import ai.foresignal.impact.hazard.HazardForecast;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The impact projection endpoints (prompt 127): {@code listProjectedImpacts}, {@code getProjectedImpact} and
 * {@code listImpactRates}, all {@code read}. Every projection carries the label "modeled; heuristic links may be
 * wrong; not an official warning".
 */
@RestController
@Tag(name = "Analytics")
public class ImpactController {

    private final ImpactQueryService impacts;
    private final ImpactPairs pairs;
    private final IntelVisibility visibility;

    /** Creates the controller. */
    public ImpactController(ImpactQueryService impacts, ImpactPairs pairs, IntelVisibility visibility) {
        this.impacts = impacts;
        this.pairs = pairs;
        this.visibility = visibility;
    }

    /** {@code listProjectedImpacts}: current projections, highest probability first within each hazard. */
    @GetMapping("/v1/analytics/impacts")
    @RestrictedRead("projections are derived statistics with no record content; private ones need private access")
    @Operation(operationId = "listProjectedImpacts", tags = "Analytics")
    public Map<String, Object> listProjectedImpacts(@RequestParam(required = false) String hazardType,
            @RequestParam(required = false) String region, @RequestParam(required = false) String since,
            @RequestParam(required = false) String minProbability, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        String type = hazardType == null || hazardType.isBlank() ? null
                : QueryParams.oneOf("hazardType", hazardType.strip(), HazardForecast.TYPES, null);
        ImpactQueryService.Page<AnalyticFindingEntity> page = impacts.list(new ImpactQueryService.Query(type,
                region(region), QueryParams.instant("since", since),
                QueryParams.doubleInRange("minProbability", minProbability, 0, 1),
                cursor, QueryParams.intInRange("limit", limit, 1, 500, 100)), visibility.seesPrivate(request));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items().stream().map(f -> ImpactViews.projected(f, false)).toList());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code getProjectedImpact}: one projection with its inputs, paths, labels and question. */
    @GetMapping("/v1/analytics/impacts/{findingId}")
    @RestrictedRead("projections are derived statistics with no record content; private ones need private access")
    @Operation(operationId = "getProjectedImpact", tags = "Analytics")
    @ApiResponse(responseCode = "404", description = "UNKNOWN_IMPACT")
    public Map<String, Object> getProjectedImpact(@PathVariable String findingId, HttpServletRequest request) {
        return ImpactViews.projected(impacts.get(findingId, visibility.seesPrivate(request)), true);
    }

    /** {@code listImpactRates}: the built-in pairs' conditional rates. */
    @GetMapping("/v1/analytics/impact-rates")
    @RestrictedRead("rates are aggregate statistics with no record content")
    @Operation(operationId = "listImpactRates", tags = "Analytics")
    public Map<String, Object> listImpactRates(@RequestParam(required = false) String hazardClass,
            @RequestParam(required = false) String impactFamily, @RequestParam(required = false) String regionClass,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        String h = blank(hazardClass);
        if (h != null && pairs.hazardClass(h) == null) {
            throw ValidationException.forField("hazardClass", "is not a hazard class of the built-in pairs");
        }
        String f = blank(impactFamily);
        if (f != null && pairs.family(f) == null) {
            throw ValidationException.forField("impactFamily", "is not an impact family of the built-in pairs");
        }
        String r = blank(regionClass);
        if (r != null && !RegionClasses.valid(r)) {
            throw ValidationException.forField("regionClass", "must be continent:XX or income:band");
        }
        ImpactQueryService.Page<Map<String, Object>> page = impacts.rates(h, f, r, blank(cursor),
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** A validated region filter, or null. */
    static String region(String raw) {
        String value = blank(raw);
        if (value != null && !ImpactQueryService.REGION.matcher(value).matches()) {
            throw ValidationException.forField("region", "'" + value + "' is not a country or admin1 code");
        }
        return value;
    }

    private static String blank(String raw) {
        return raw == null || raw.isBlank() ? null : raw.strip();
    }
}
