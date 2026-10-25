package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.reference.ReferenceAreas;
import ai.foresignal.reference.ReferenceDataLoader;
import ai.foresignal.reference.RegionTagger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Reference data (prompt 040; OpenAPI tag {@code Reference}): {@code getReference},
 * {@code loadReference}, {@code listReferenceCountries} and {@code reverseGeocode}; prompt 161 adds
 * {@code searchReferenceAreas} and {@code getReferenceArea}, the named areas a rule can be scoped to.
 *
 * <p>Reads need the {@code read} scope (open while {@code security.openReads} is on); loading is
 * {@code admin} and asynchronous (202). Loading accepts only dataset names from the bundled manifest,
 * never URLs. None of these return records.
 */
@RestController
@Tag(name = "Reference")
public class ReferenceController {

    private final ReferenceDataLoader loader;
    private final CountryCentroids countries;
    private final RegionTagger tagger;
    private final ReferenceAreas areas;

    /** Creates the controller. */
    public ReferenceController(ReferenceDataLoader loader, CountryCentroids countries, RegionTagger tagger,
            ReferenceAreas areas) {
        this.loader = loader;
        this.countries = countries;
        this.tagger = tagger;
        this.areas = areas;
    }

    /** {@code getReference}: datasets, editions, row counts, attributions and load state. */
    @GetMapping("/v1/reference")
    @Operation(operationId = "getReference", tags = "Reference")
    public Map<String, Object> getReference() {
        return loader.status();
    }

    /**
     * {@code loadReference}: queues the named datasets (from the bundled manifest) for download, checksum
     * verification and an atomic swap; 202 with the status.
     */
    @PostMapping("/v1/reference/load")
    @Operation(operationId = "loadReference", tags = "Reference")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> loadReference(@RequestBody JsonNode body) {
        loader.loadAsync(datasets(body));
        return loader.status();
    }

    /** {@code listReferenceCountries}: the bundled country table. */
    @GetMapping("/v1/reference/countries")
    @Operation(operationId = "listReferenceCountries", tags = "Reference")
    public List<Map<String, Object>> listReferenceCountries() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CountryRef country : countries.all()) {
            out.add(country.toMap());
        }
        return out;
    }

    /** {@code reverseGeocode}: the {@code RegionTags} of a point; never fails for missing reference data. */
    @GetMapping("/v1/reference/reverse")
    @Operation(operationId = "reverseGeocode", tags = "Reference")
    public Map<String, Object> reverseGeocode(@RequestParam(required = false) String lon,
            @RequestParam(required = false) String lat) {
        double x = coordinate("lon", lon, 180);
        double y = coordinate("lat", lat, 90);
        return tagger.tag(x, y).toMap();
    }

    /**
     * {@code searchReferenceAreas} (prompt 161): the countries, admin-1 regions and populated places whose names
     * match {@code q}, best first.
     */
    @GetMapping("/v1/reference/areas")
    @Operation(operationId = "searchReferenceAreas", tags = "Reference")
    public List<Map<String, Object>> searchReferenceAreas(@RequestParam(required = false) String q,
            @RequestParam(required = false) String limit) {
        int max = QueryParams.intInRange("limit", limit, 1, ReferenceAreas.MAX_LIMIT, ReferenceAreas.DEFAULT_LIMIT);
        return areas.search(q, max);
    }

    /** {@code getReferenceArea} (prompt 161): the simplified outline of a country or admin-1 region. */
    @GetMapping("/v1/reference/areas/{code}")
    @Operation(operationId = "getReferenceArea", tags = "Reference")
    public Map<String, Object> getReferenceArea(@PathVariable String code) {
        return areas.outline(code);
    }

    /**
     * The {@code datasets} of a {@code ReferenceLoadRequest}.
     *
     * @throws ValidationException when the body is not an object with a non-empty array of names
     */
    public static List<String> datasets(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a ReferenceLoadRequest object.");
        }
        JsonNode node = body.get("datasets");
        if (node == null || !node.isArray() || node.isEmpty()) {
            throw ValidationException.forField("datasets", "must be a non-empty array of dataset names");
        }
        List<String> names = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField("datasets", "every item must be a dataset name");
            }
            names.add(item.asString().strip());
        }
        return names;
    }

    private static double coordinate(String name, String raw, double limit) {
        if (raw == null || raw.isBlank()) {
            throw ValidationException.forField(name, "is required");
        }
        return QueryParams.doubleInRange(name, raw, -limit, limit);
    }
}
