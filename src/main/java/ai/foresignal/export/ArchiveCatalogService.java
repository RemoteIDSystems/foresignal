package ai.foresignal.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Lists the cold archive's partitions and files (prompt 111, {@code getArchiveCatalog}) from the hive
 * layout and each partition's {@code _manifest.json}, which records a file's seq bounds, rows,
 * SHA-256 and upload state even after its local copy was deleted. A part file without a manifest
 * entry is still listed, with its bounds parsed from its name and its rows read from its footer.
 */
@Service
public class ArchiveCatalogService {

    private static final Pattern PART = Pattern.compile("part-(\\d+)-(\\d+)\\.parquet");

    private final ArchiveService archive;
    private final ArchiveSettingsService settingsService;

    /** Creates the service. */
    public ArchiveCatalogService(ArchiveService archive, ArchiveSettingsService settingsService) {
        this.archive = archive;
        this.settingsService = settingsService;
    }

    /**
     * A page of partitions.
     *
     * @param items {@code ArchivePartition} maps
     * @param nextCursor the cursor of the next page, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {}

    /**
     * One page of partitions ordered by tier, source and date.
     *
     * @param source a source id, or null for all
     * @param from the first date (inclusive), or null
     * @param to the last date (inclusive), or null
     * @param cursor the {@code path} of the previous page's last partition, or null
     */
    public Page list(String source, LocalDate from, LocalDate to, String cursor, int limit) {
        int size = Math.max(1, Math.min(500, limit));
        Path recordVersion = archive.root(settingsService.get()).resolve("record_version");
        if (!Files.isDirectory(recordVersion)) {
            return new Page(List.of(), null);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        String next = null;
        for (Path tierDir : directories(recordVersion)) {
            String tier = strip(tierDir, "tier=");
            for (Path sourceDir : directories(tierDir)) {
                String sourceId = strip(sourceDir, "source_id=");
                if (source != null && !source.isBlank() && !source.equals(sourceId)) {
                    continue;
                }
                for (Path dateDir : directories(sourceDir)) {
                    String dateText = strip(dateDir, "date=");
                    LocalDate date;
                    try {
                        date = LocalDate.parse(dateText);
                    } catch (RuntimeException e) {
                        continue;
                    }
                    if ((from != null && date.isBefore(from)) || (to != null && date.isAfter(to))) {
                        continue;
                    }
                    String path = "record_version/tier=" + tier + "/source_id=" + sourceId + "/date=" + dateText;
                    if (cursor != null && path.compareTo(cursor) <= 0) {
                        continue;
                    }
                    if (out.size() == size) {
                        next = (String) out.getLast().get("path");
                        return new Page(out, next);
                    }
                    out.add(partition(tier, sourceId, dateText, path, dateDir));
                }
            }
        }
        return new Page(out, next);
    }

    private Map<String, Object> partition(String tier, String sourceId, String date, String path, Path dir) {
        Map<String, Object> manifest = archive.readManifest(dir);
        List<Map<String, Object>> files = new ArrayList<>();
        Set<String> listed = new HashSet<>();
        if (manifest != null && manifest.get("files") instanceof List<?> entries) {
            for (Object entry : entries) {
                if (entry instanceof Map<?, ?> map) {
                    Map<String, Object> file = new LinkedHashMap<>();
                    map.forEach((k, v) -> file.put(String.valueOf(k), v));
                    Object name = file.get("name");
                    if (name != null) {
                        listed.add(String.valueOf(name));
                        file.put("local", Files.isRegularFile(dir.resolve(String.valueOf(name))));
                    }
                    files.add(file);
                }
            }
        }
        try (Stream<Path> stream = Files.list(dir)) {
            for (Path file : stream.sorted().toList()) {
                String name = file.getFileName().toString();
                Matcher m = PART.matcher(name);
                if (!m.matches() || listed.contains(name)) {
                    continue;
                }
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", name);
                entry.put("fromSeq", Long.parseLong(m.group(1)));
                entry.put("toSeq", Long.parseLong(m.group(2)));
                entry.put("rows", GeoParquetFiles.rowCount(file));
                entry.put("bytes", Files.size(file));
                entry.put("sha256", ExportManifests.sha256(file));
                entry.put("local", true);
                entry.put("uploadedAt", null);
                entry.put("s3Key", null);
                files.add(entry);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the archive partition " + path, e);
        }
        files.sort(java.util.Comparator.comparingLong(f -> ((Number) f.get("fromSeq")).longValue()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("tier", tier);
        out.put("sourceId", sourceId);
        out.put("date", date);
        out.put("path", path);
        out.put("files", files);
        out.put("licence", manifest == null ? null : manifest.get("licence"));
        return out;
    }

    private static List<Path> directories(Path parent) {
        try (Stream<Path> stream = Files.list(parent)) {
            return stream.filter(Files::isDirectory).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list " + parent, e);
        }
    }

    private static String strip(Path dir, String prefix) {
        String name = dir.getFileName().toString();
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }
}
