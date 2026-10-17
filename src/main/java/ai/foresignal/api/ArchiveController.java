package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.export.ArchiveCatalogService;
import ai.foresignal.export.ArchiveSettings;
import ai.foresignal.export.ArchiveSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The cold archive (OpenAPI tag {@code Archive}, prompt 111; admin): the {@code archive} settings
 * (secrets write-only) and the catalog of archived partitions and files.
 *
 * <p>Left out in edge mode (prompt 113: exports (archive)).
 */
@RestController
@Tag(name = "Archive")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class ArchiveController {

    private final ArchiveSettingsService settings;
    private final ArchiveCatalogService catalog;

    /** Creates the controller. */
    public ArchiveController(ArchiveSettingsService settings, ArchiveCatalogService catalog) {
        this.settings = settings;
        this.catalog = catalog;
    }

    /** {@code getArchiveSettings}: the settings, without secrets. */
    @GetMapping("/v1/settings/archive")
    @Operation(operationId = "getArchiveSettings", tags = "Archive")
    public Map<String, Object> getArchiveSettings() {
        return settings.view(settings.get());
    }

    /** {@code updateArchiveSettings}: update the settings; omitted members and secrets keep their values. */
    @PutMapping("/v1/settings/archive")
    @Operation(operationId = "updateArchiveSettings", tags = "Archive")
    public Map<String, Object> updateArchiveSettings(@RequestBody JsonNode body) {
        ArchiveSettings updated = settings.update(body);
        return settings.view(updated);
    }

    /** {@code getArchiveCatalog}: partitions and files, filtered by source and date. */
    @GetMapping("/v1/archive/catalog")
    @Operation(operationId = "getArchiveCatalog", tags = "Archive")
    public Map<String, Object> getArchiveCatalog(@RequestParam(required = false) String source,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String cursor, @RequestParam(required = false) String limit) {
        ArchiveCatalogService.Page page = catalog.list(source, date("from", from), date("to", to), cursor,
                BulkExportsController.parseLimit(limit));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.items());
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    private static LocalDate date(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.strip());
        } catch (RuntimeException e) {
            throw ValidationException.forField(field, "must be a date (yyyy-mm-dd)");
        }
    }
}
