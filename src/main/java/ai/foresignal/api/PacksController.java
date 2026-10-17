package ai.foresignal.api;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.export.PackService;
import ai.foresignal.export.PackSpec;
import ai.foresignal.intel.PopulationPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** {@code /v1/packs} (prompts 101 and 111): population packs and PMTiles offline packs. *
 * <p>Left out in edge mode (prompt 113: exports (offline packs)).
 */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/packs")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class PacksController {

    private final PopulationPackService population;
    private final PackService packs;
    private final ExportJobService exports;
    private final ReadCallerResolver callers;
    private final ai.foresignal.raster.ProductUrls urls;

    /** Creates the controller. */
    public PacksController(PopulationPackService population, PackService packs, ExportJobService exports,
            ReadCallerResolver callers, ai.foresignal.raster.ProductUrls urls) {
        this.population = population;
        this.packs = packs;
        this.exports = exports;
        this.callers = callers;
        this.urls = urls;
    }

    /** {@code importPopulationPack}. */
    @PostMapping("/population/import")
    @Operation(operationId = "importPopulationPack", tags = "Intelligence")
    public Map<String, Object> importPopulationPack(@RequestBody JsonNode body) {
        String path = body.path("path").asString(null);
        if (path == null || path.isBlank()) {
            throw ValidationException.forField("path", "is required");
        }
        short epoch = body.hasNonNull("epoch") ? (short) body.get("epoch").asInt() : (short) 2025;
        try {
            Map<String, Object> record = population.importPack(path, epoch);
            return new LinkedHashMap<>(record);
        } catch (IOException e) {
            throw new ConflictException(ErrorCodes.PACK_NOT_INSTALLED, e.getMessage());
        }
    }

    /**
     * {@code createPack}: queue a PMTiles offline pack (prompt 111), 202 with the {@code ExportJob} of
     * kind {@code PACK}; downloaded through {@code /v1/exports/{id}/download}.
     */
    @PostMapping
    @Operation(operationId = "createPack", tags = "Exports")
    public ResponseEntity<Map<String, Object>> createPack(@RequestBody JsonNode body, HttpServletRequest request) {
        PackSpec spec = PackSpec.parse(body);
        ReadCaller caller = callers.resolve(request);
        ExportJobEntity job = packs.create(spec, ExportCallers.of(request, callers), callers.excludedSources(caller));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create("/v1/exports/" + job.getId()))
                .body(exports.view(job, urls.base(request)));
    }

}
