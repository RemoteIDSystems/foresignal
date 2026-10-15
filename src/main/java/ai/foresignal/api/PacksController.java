package ai.foresignal.api;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.PopulationPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** {@code POST /v1/packs/population/import} (prompt 101): imports a GHSL GHS-POP GeoTIFF pack. */
@RestController
@Tag(name = "Intelligence")
@RequestMapping("/v1/packs")
public class PacksController {

    private final PopulationPackService population;

    /** Creates the controller. */
    public PacksController(PopulationPackService population) {
        this.population = population;
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
}
