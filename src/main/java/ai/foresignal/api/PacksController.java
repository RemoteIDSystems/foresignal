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
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RequestPrincipal;
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

/** {@code /v1/packs} (prompts 101 and 111): population packs and PMTiles offline packs. */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/packs")
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

    /** {@code createPack}: build a PMTiles offline pack. */
    @PostMapping
    @Operation(operationId = "createPack", tags = "Exports")
    public ResponseEntity<Map<String, Object>> createPack(@RequestBody JsonNode body, HttpServletRequest request) {
        PackSpec spec = PackSpec.parse(body);
        ReadCaller caller = callers.resolve(request);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        ExportJobService.Caller packCaller = new ExportJobService.Caller(tokenId(request), admin,
                caller.privateAccess(), caller.restriction());
        ExportJobEntity job = packs.create(spec, packCaller);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create("/v1/exports/" + job.getId()))
                .body(exports.view(job, urls.base(request)));
    }

    private static String tokenId(HttpServletRequest request) {
        return RequestPrincipal.of(request).map(p -> p.id() == null ? null : p.id().toString()).orElse(null);
    }
}
