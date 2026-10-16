package ai.foresignal.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lists the cold archive's partitions and files (prompt 111), reading the hive layout and each
 * partition's {@code _manifest.json}.
 */
@Service
public class ArchiveCatalogService {

    private final ExportStore store;
    private final ArchiveSettingsService settingsService;
    private final JsonMapper jsonMapper;

    /** Creates the service. */
    public ArchiveCatalogService(ExportStore store, ArchiveSettingsService settingsService, JsonMapper jsonMapper) {
        this.store = store;
        this.settingsService = settingsService;
        this.jsonMapper = jsonMapper;
    }

    /** Lists partitions, filtered by source and by {@code from}/{@code to} (ISO dates). */
    public List<Map<String, Object>> list(String source, String from, String to) {
        ArchiveSettings settings = settingsService.get();
        Path root = settings.root() == null || settings.root().isBlank() ? store.archiveRoot()
                : store.resolve(settings.root());
        Path recordVersion = root.resolve("record_version");
        if (!Files.isDirectory(recordVersion)) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        try (var tiers = Files.list(recordVersion)) {
            for (Path tierDir : tiers.filter(Files::isDirectory).sorted().toList()) {
                String tier = name(tierDir.getFileName().toString(), "tier=");
                try (var sources = Files.list(tierDir)) {
                    for (Path sourceDir : sources.filter(Files::isDirectory).sorted().toList()) {
                        String sourceId = name(sourceDir.getFileName().toString(), "source_id=");
                        if (source != null && !source.isBlank() && !source.equals(sourceId)) {
                            continue;
                        }
                        try (var dates = Files.list(sourceDir)) {
                            for (Path dateDir : dates.filter(Files::isDirectory).sorted().toList()) {
                                String date = name(dateDir.getFileName().toString(), "date=");
                                if (from != null && date.compareTo(from) < 0) {
                                    continue;
                                }
                                if (to != null && date.compareTo(to) > 0) {
                                    continue;
                                }
                                out.add(partition(tier, sourceId, date, dateDir));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the archive", e);
        }
        out.sort(Comparator.comparing(m -> String.valueOf(m.get("sourceId"))));
        return out;
    }

    private Map<String, Object> partition(String tier, String sourceId, String date, Path dateDir) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tier", tier);
        out.put("sourceId", sourceId);
        out.put("date", date);
        out.put("path", "record_version/tier=" + tier + "/source_id=" + sourceId + "/date=" + date);
        out.put("licence", licence(dateDir));
        out.put("files", files(dateDir));
        return out;
    }

    private List<Map<String, Object>> files(Path dateDir) {
        List<Map<String, Object>> out = new ArrayList<>();
        try (var stream = Files.list(dateDir)) {
            for (Path file : stream.filter(p -> p.getFileName().toString().endsWith(".parquet")).sorted().toList()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", file.getFileName().toString());
                entry.put("bytes", Files.size(file));
                entry.put("local", true);
                try {
                    entry.put("sha256", ExportManifests.sha256(file));
                } catch (IOException e) {
                    entry.put("sha256", null);
                }
                out.add(entry);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    private String licence(Path dateDir) {
        Path manifest = dateDir.resolve("_manifest.json");
        if (!Files.isRegularFile(manifest)) {
            return null;
        }
        try {
            JsonNode node = jsonMapper.readTree(Files.readAllBytes(manifest));
            JsonNode licence = node.get("licence");
            return licence == null || licence.isNull() ? null : licence.asString();
        } catch (IOException e) {
            return null;
        }
    }

    private static String name(String value, String prefix) {
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }
}
