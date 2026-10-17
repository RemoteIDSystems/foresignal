package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.backtest.BacktestViews;
import ai.foresignal.baserate.BaseRateDefinition;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.BaseRateViews;
import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.baserate.reference.ReferenceImportService;
import ai.foresignal.baserate.reference.ReferenceImportViews;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base rates, reference-history imports and analogs (prompt 121; OpenAPI tag {@code Analysis}). Rates, ad-hoc queries
 * and analogs are reads; storing, deleting and refreshing rates are {@code agent}; imports are {@code admin}
 * ({@code AccessPolicy}). Rates are derived statistics: no record content, and the per-source counts of a
 * {@code LOCAL_ONLY} source only where {@code EgressPolicy} allows them.
 */
@RestController
@Tag(name = "Analysis")
public class BaseRateController {

    private final BaseRateService baseRates;
    private final ReferenceImportService imports;
    private final AnalogService analogs;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the controller. */
    public BaseRateController(BaseRateService baseRates, ReferenceImportService imports, AnalogService analogs,
            IntelVisibility visibility, JsonMapper json) {
        this.baseRates = baseRates;
        this.imports = imports;
        this.analogs = analogs;
        this.visibility = visibility;
        this.json = json;
    }

    // ------------------------------------------------------------------------- reference imports

    /** {@code createReferenceImport}: 202 with the import. */
    @PostMapping("/v1/reference-imports")
    @Operation(operationId = "createReferenceImport", tags = "Analysis")
    public ResponseEntity<Map<String, Object>> createReferenceImport(@RequestBody JsonNode body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ReferenceImportViews.view(imports.create(body,
                actor(request))));
    }

    /** {@code listReferenceImports}: newest first. */
    @GetMapping("/v1/reference-imports")
    @Operation(operationId = "listReferenceImports", tags = "Analysis")
    public Map<String, Object> listReferenceImports(@RequestParam(required = false) String dataset,
            @RequestParam(required = false) String status, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        String datasetFilter = dataset == null || dataset.isBlank() ? null
                : QueryParams.oneOf("dataset", dataset, ai.foresignal.baserate.reference.ReferenceFamilies.DATASETS,
                        null);
        String statusFilter = status == null || status.isBlank() ? null
                : QueryParams.oneOf("status", status, ReferenceImportService.STATUSES, null);
        ReferenceImportService.Page page = imports.list(datasetFilter, statusFilter, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        return ReferenceImportViews.page(page.items(), page.nextCursor());
    }

    /** {@code getReferenceImport}. */
    @GetMapping("/v1/reference-imports/{importId}")
    @Operation(operationId = "getReferenceImport", tags = "Analysis")
    public Map<String, Object> getReferenceImport(@PathVariable String importId) {
        return ReferenceImportViews.view(imports.get(importId));
    }

    /** {@code deleteReferenceImport}: 204; its rows are removed. */
    @DeleteMapping("/v1/reference-imports/{importId}")
    @Operation(operationId = "deleteReferenceImport", tags = "Analysis")
    public ResponseEntity<Void> deleteReferenceImport(@PathVariable String importId) {
        imports.delete(importId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- base rates

    /** {@code listBaseRates}. */
    @GetMapping("/v1/base-rates")
    @RestrictedRead("base rates are derived statistics, no record content")
    @Operation(operationId = "listBaseRates", tags = "Analysis")
    public Map<String, Object> listBaseRates(@RequestParam(required = false) List<String> family,
            @RequestParam(required = false) List<String> region, @RequestParam(required = false) String window,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        List<String> regions = QueryParams.multi("region", region);
        for (String code : regions) {
            if (!code.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw ValidationException.forField("region", "'" + code + "' is not a 040 region code");
            }
        }
        String windowFilter = window == null || window.isBlank() ? null
                : QueryParams.oneOf("window", window, BaseRateDefinition.WINDOWS, null);
        BaseRateService.Page page = baseRates.list(QueryParams.multi("family", family), regions, windowFilter, cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
        return BaseRateViews.page(page, baseRates, EgressPath.API_READ);
    }

    /** {@code createBaseRate}: 201; origin {@code operator} for admin tokens, else {@code agent}. */
    @PostMapping("/v1/base-rates")
    @Operation(operationId = "createBaseRate", tags = "Analysis")
    public ResponseEntity<Map<String, Object>> createBaseRate(@RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a BaseRateInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!field.equals("name") && !field.equals("definition")) {
                throw ValidationException.forField(field, "is not a BaseRateInput member");
            }
        }
        String name = body.hasNonNull("name") ? body.get("name").asString() : null;
        boolean admin = CurrentToken.of(request).map(t -> t.scope() == Scope.ADMIN).orElse(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseRateViews.view(baseRates.create(name,
                map(body.get("definition")), admin ? "operator" : "agent", actor(request)), baseRates,
                EgressPath.API_READ));
    }

    /** {@code queryBaseRate}: a stored rate with this definition, else computed and cached for 24 h. */
    @PostMapping("/v1/base-rates/query")
    @RestrictedRead("base rates are derived statistics, no record content")
    @Operation(operationId = "queryBaseRate", tags = "Analysis")
    public Map<String, Object> queryBaseRate(@RequestBody JsonNode body) {
        return BaseRateViews.view(baseRates.query(map(body)), baseRates, EgressPath.API_READ);
    }

    /** {@code refreshBaseRates}: 202 with the 103 job. */
    @PostMapping("/v1/base-rates/refresh")
    @Operation(operationId = "refreshBaseRates", tags = "Analysis")
    public ResponseEntity<Map<String, Object>> refreshBaseRates(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(BacktestViews.job(baseRates.submitRefresh("manual",
                tokenId(request))));
    }

    /** {@code getBaseRate}. */
    @GetMapping("/v1/base-rates/{baseRateId}")
    @RestrictedRead("base rates are derived statistics, no record content")
    @Operation(operationId = "getBaseRate", tags = "Analysis")
    public Map<String, Object> getBaseRate(@PathVariable String baseRateId) {
        return BaseRateViews.view(baseRates.get(baseRateId), baseRates, EgressPath.API_READ);
    }

    /** {@code deleteBaseRate}: 204; automatic rates are refused with 409. */
    @DeleteMapping("/v1/base-rates/{baseRateId}")
    @Operation(operationId = "deleteBaseRate", tags = "Analysis")
    public ResponseEntity<Void> deleteBaseRate(@PathVariable String baseRateId) {
        baseRates.delete(baseRateId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------- analogs

    /** {@code getSituationAnalogs}. */
    @GetMapping("/v1/situations/{situationId}/analogs")
    @RestrictedRead("analogs are situation summaries and statistics; private situations need private access")
    @Operation(operationId = "getSituationAnalogs", tags = "Analysis")
    public Map<String, Object> getSituationAnalogs(@PathVariable String situationId,
            @RequestParam(required = false) String k, @RequestParam(required = false) String outcome,
            HttpServletRequest request) {
        return analogs.forSituation(situationId, QueryParams.intInRange("k", k, 1, 50, AnalogService.DEFAULT_K),
                outcome, visibility.seesPrivate(request));
    }

    /** {@code queryAnalogs}: region-period analogs. */
    @PostMapping("/v1/analogs/query")
    @RestrictedRead("region-period analogs are reference-history statistics, no record content")
    @Operation(operationId = "queryAnalogs", tags = "Analysis")
    public Map<String, Object> queryAnalogs(@RequestBody JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an AnalogQuery object.");
        }
        return analogs.forRegion(map(body));
    }

    private Map<String, Object> map(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            throw ValidationException.forField("definition", "must be an object");
        }
        return json.readValue(json.writeValueAsString(node), new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private static String tokenId(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse(null);
    }

    private static String actor(HttpServletRequest request) {
        return CurrentToken.of(request).map(t -> t.id().toString()).orElse("operator");
    }
}
