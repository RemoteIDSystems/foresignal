package ai.foresignal.api;

import ai.foresignal.export.ArchiveCatalogService;
import ai.foresignal.export.ArchiveSettings;
import ai.foresignal.export.ArchiveSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/** The cold archive (OpenAPI tag {@code Archive}, prompt 111): settings and catalog. */
@RestController
@Tag(name = "Archive")
public class ArchiveController {

    private final ArchiveSettingsService settings;
    private final ArchiveCatalogService catalog;

    /** Creates the controller. */
    public ArchiveController(ArchiveSettingsService settings, ArchiveCatalogService catalog) {
        this.settings = settings;
        this.catalog = catalog;
    }

    /** {@code getArchiveSettings}: the archive settings. */
    @GetMapping("/v1/settings/archive")
    @Operation(operationId = "getArchiveSettings", tags = "Archive")
    public Map<String, Object> getArchiveSettings() {
        return settings.view(settings.get());
    }

    /** {@code updateArchiveSettings}: update the archive settings. */
    @PutMapping("/v1/settings/archive")
    @Operation(operationId = "updateArchiveSettings", tags = "Archive")
    public Map<String, Object> updateArchiveSettings(@RequestBody JsonNode body) {
        ArchiveSettings updated = settings.update(body);
        return settings.view(updated);
    }

    /** {@code getArchiveCatalog}: the archived partitions and files. */
    @GetMapping("/v1/archive/catalog")
    @Operation(operationId = "getArchiveCatalog", tags = "Archive")
    public Map<String, Object> getArchiveCatalog(@RequestParam(required = false) String source,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        List<Map<String, Object>> items = catalog.list(source, from, to);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }
}
