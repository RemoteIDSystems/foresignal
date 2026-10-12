package ai.foresignal.inbound;

import ai.foresignal.inbound.signature.VerifierConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code listCustomSources}, {@code createCustomSource}, {@code getCustomSource},
 * {@code updateCustomSource}, {@code deleteCustomSource} and {@code rotateIngestKey} (admin, prompt
 * 024). Keys are shown once, in the create and rotate responses; verifier secrets are write-only and
 * reported as {@code secretSet}.
 */
@RestController
@Tag(name = "Ingest")
public class CustomSourcesController {

    private final CustomSourceService service;
    private final IngestRecordLimiter recordLimiter;
    private final JsonMapper json;

    /** Creates the controller. */
    public CustomSourcesController(CustomSourceService service, IngestRecordLimiter recordLimiter, JsonMapper json) {
        this.service = service;
        this.recordLimiter = recordLimiter;
        this.json = json;
    }

    /** {@code listCustomSources}. */
    @GetMapping("/v1/custom-sources")
    @Operation(operationId = "listCustomSources", tags = "Ingest")
    public List<Map<String, Object>> listCustomSources() {
        return service.list().stream().map(this::view).toList();
    }

    /** {@code createCustomSource}: 201 with the ingest key, shown once. */
    @PostMapping("/v1/custom-sources")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createCustomSource", tags = "Ingest")
    public Map<String, Object> createCustomSource(@RequestBody JsonNode body) {
        CustomSourceService.Created created = service.create(body);
        Map<String, Object> out = view(created.source());
        out.put("ingestKey", created.ingestKey());
        return out;
    }

    /** {@code getCustomSource}. */
    @GetMapping("/v1/custom-sources/{sourceId}")
    @Operation(operationId = "getCustomSource", tags = "Ingest")
    public Map<String, Object> getCustomSource(@PathVariable String sourceId) {
        return view(service.get(sourceId));
    }

    /** {@code updateCustomSource}. */
    @PutMapping("/v1/custom-sources/{sourceId}")
    @Operation(operationId = "updateCustomSource", tags = "Ingest")
    public Map<String, Object> updateCustomSource(@PathVariable String sourceId, @RequestBody JsonNode body) {
        return view(service.update(sourceId, body));
    }

    /** {@code deleteCustomSource}: 204. */
    @DeleteMapping("/v1/custom-sources/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteCustomSource", tags = "Ingest")
    public void deleteCustomSource(@PathVariable String sourceId) {
        service.get(sourceId);
        service.delete(sourceId);
        recordLimiter.forget(sourceId);
    }

    /** {@code rotateIngestKey}: the new key, shown once; the old key stops working at once. */
    @PostMapping("/v1/custom-sources/{sourceId}/ingest-key/rotate")
    @Operation(operationId = "rotateIngestKey", tags = "Ingest")
    public Map<String, Object> rotateIngestKey(@PathVariable String sourceId) {
        service.get(sourceId);
        return Map.of("ingestKey", service.rotateKey(sourceId));
    }

    /** The {@code CustomSource} view of {@code row}. */
    Map<String, Object> view(CustomSourceEntity row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.getSourceId());
        out.put("name", row.getName());
        if (row.getDescription() != null) {
            out.put("description", row.getDescription());
        }
        out.put("kinds", row.getKinds());
        if (row.getAttribution() != null) {
            out.put("attribution", row.getAttribution());
        }
        out.put("recordTtl", row.getRecordTtl());
        out.put("tileProperties", row.getTileProperties());
        if (row.getSearchFields() != null) {
            out.put("searchFields", row.getSearchFields());
        }
        out.put("embedFields", row.getEmbedFields());
        out.put("rankField", row.getRankField());
        out.put("rateLimitPerMinute", row.getRateLimitPerMinute());
        out.put("observedAtMode", row.getObservedAtMode());
        out.put("ttlAnchor", row.getTtlAnchor());
        out.put("volatileFields", row.getVolatileFields());
        out.put("significantFields", row.getSignificantFields());
        out.put("titleField", row.getTitleField());
        out.put("fieldSpecs", row.getFieldSpecs() == null ? Map.of() : json.readTree(row.getFieldSpecs()));
        out.put("private", row.isPrivateSource());
        out.put("licence", row.getLicence());
        out.put("recordsPerMinute", row.getRecordsPerMinute());
        out.put("formats", row.getFormats() == null ? Map.of() : json.readTree(row.getFormats()));
        Map<String, VerifierConfig> configs = service.verifierConfigs(row);
        Set<String> secrets = service.secretsSet(row);
        Map<String, Object> verifiers = new LinkedHashMap<>();
        configs.forEach((format, config) -> {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("scheme", config.scheme());
            v.put("header", config.header());
            v.put("secretSet", secrets.contains(format));
            v.put("keyOptional", config.keyOptional());
            verifiers.put(format, v);
        });
        out.put("verifiers", verifiers);
        out.put("createdAt", row.getCreatedAt());
        out.put("lastIngestAt", row.getLastIngestAt());
        out.put("ingestKeyPrefix", row.getIngestKeyPrefix());
        return out;
    }
}
