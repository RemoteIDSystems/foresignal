package ai.foresignal.doctor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The recorded fixtures packaged into the jar (prompt 140, task 1): {@code META-INF/foresignal/fixtures/{sourceId}/…}
 * with {@code fixtures-index.json}, written by the Gradle task {@code packageDoctorResources} from
 * {@code src/test/resources/fixtures/**} with a per-file cap of 256 KB.
 *
 * <p>Absent-safe: a jar without the index, or a source without fixtures, gives empty results, and the doctor reports
 * "no fixture for this source".
 */
@Component
public class FixtureCatalog {

    private static final Logger log = LoggerFactory.getLogger(FixtureCatalog.class);

    /** The packaged fixtures' classpath folder. */
    public static final String ROOT = "META-INF/foresignal/fixtures/";

    /** The index file's name. */
    public static final String INDEX = "fixtures-index.json";

    /** File names that mark a fixture as an edge case or a deliberate break rather than a normal capture. */
    private static final List<String> NOT_NORMAL = List.of("edge", "error", "not-a", "invalid", "malformed", "empty",
            "broken", "bad", "e2e", "revised", "updated", "changed", "format-change", "truncated", "garbage", "html",
            "404", "500", "429", "403", "401");

    /**
     * One packaged fixture file.
     *
     * @param sourceId the source
     * @param file the file, relative to the source's folder
     * @param bytes the packaged size
     * @param originalBytes the size before truncation
     * @param sha256 the packaged bytes' SHA-256
     * @param truncated true when the file was cut to fit the cap
     * @param capturedAt the capture date the source's fixture README states, or null
     */
    public record FixtureFile(String sourceId, String file, long bytes, long originalBytes, String sha256,
            boolean truncated, String capturedAt) {}

    private final String root;
    private final JsonMapper json;
    private volatile Map<String, List<FixtureFile>> index;

    /** The catalog of the packaged fixtures. */
    public FixtureCatalog() {
        this(ROOT, JsonMapper.builder().build());
    }

    /** A catalog reading another classpath folder (tests). */
    public FixtureCatalog(String root, JsonMapper json) {
        this.root = root.endsWith("/") ? root : root + "/";
        this.json = json;
    }

    /** True when the index was packaged. */
    public boolean available() {
        return new ClassPathResource(root + INDEX).exists();
    }

    /** The fixture files of {@code sourceId}, in index order; empty when it has none. */
    public List<FixtureFile> files(String sourceId) {
        return index().getOrDefault(sourceId, List.of());
    }

    /** Every source with packaged fixtures. */
    public List<String> sources() {
        return List.copyOf(index().keySet());
    }

    /** The bytes of one packaged fixture file, if present. */
    public Optional<byte[]> read(String sourceId, String file) {
        if (sourceId == null || file == null || file.contains("..") || sourceId.contains("/")) {
            return Optional.empty();
        }
        ClassPathResource resource = new ClassPathResource(root + sourceId + "/" + file);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream in = resource.getInputStream()) {
            return Optional.of(in.readAllBytes());
        } catch (IOException e) {
            log.warn("Cannot read fixture {}/{}: {}", sourceId, file, e.toString());
            return Optional.empty();
        }
    }

    /**
     * The fixture to compare a probe with: a data file (not a README or a hand-made edge case), preferring one whose
     * extension fits {@code probeFormat}, then the largest.
     *
     * @param sourceId the source
     * @param probeFormat the probe body's format, or null
     */
    public Optional<FixtureFile> primary(String sourceId, StructureFormat probeFormat) {
        List<FixtureFile> candidates = new ArrayList<>();
        for (FixtureFile f : files(sourceId)) {
            String name = f.file().toLowerCase(Locale.ROOT);
            if (name.endsWith(".md") || name.endsWith(".py") || name.endsWith(".sha256") || name.endsWith(".idx")) {
                continue;
            }
            candidates.add(f);
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        Comparator<FixtureFile> order = Comparator
                .comparing((FixtureFile f) -> !isNormal(f.file()))
                .thenComparing(f -> !fits(f.file(), probeFormat))
                .thenComparing(Comparator.comparingLong(FixtureFile::originalBytes).reversed())
                .thenComparing(FixtureFile::file);
        candidates.sort(order);
        return Optional.of(candidates.get(0));
    }

    /** True when the name does not mark an edge case or a deliberate break. */
    static boolean isNormal(String file) {
        String name = file.toLowerCase(Locale.ROOT);
        for (String marker : NOT_NORMAL) {
            if (name.contains(marker)) {
                return false;
            }
        }
        return true;
    }

    /** True when the file's extension fits the format. */
    static boolean fits(String file, StructureFormat format) {
        if (format == null) {
            return true;
        }
        String name = file.toLowerCase(Locale.ROOT);
        return switch (format) {
            case JSON -> name.endsWith(".json") || name.endsWith(".geojson");
            case CSV -> name.endsWith(".csv") || name.endsWith(".txt");
            case TSV -> name.endsWith(".tsv") || name.endsWith(".txt");
            case XML -> name.endsWith(".xml") || name.endsWith(".kml") || name.endsWith(".rss")
                    || name.endsWith(".atom");
            case HTML -> name.endsWith(".html") || name.endsWith(".htm");
            case BINARY -> !(name.endsWith(".json") || name.endsWith(".csv") || name.endsWith(".xml")
                    || name.endsWith(".txt") || name.endsWith(".html"));
            case TEXT -> name.endsWith(".txt");
        };
    }

    private Map<String, List<FixtureFile>> index() {
        Map<String, List<FixtureFile>> current = index;
        if (current != null) {
            return current;
        }
        Map<String, List<FixtureFile>> out = new LinkedHashMap<>();
        ClassPathResource resource = new ClassPathResource(root + INDEX);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                JsonNode sources = json.readTree(in).path("sources");
                for (Map.Entry<String, JsonNode> e : sources.properties()) {
                    String capturedAt = text(e.getValue().get("capturedAt"));
                    List<FixtureFile> files = new ArrayList<>();
                    for (JsonNode f : e.getValue().path("files")) {
                        files.add(new FixtureFile(e.getKey(), f.path("file").asString(), f.path("bytes").asLong(),
                                f.path("originalBytes").asLong(), f.path("sha256").asString(),
                                f.path("truncated").asBoolean(false), capturedAt));
                    }
                    out.put(e.getKey(), List.copyOf(files));
                }
            } catch (IOException | JacksonException e) {
                log.warn("Cannot read the packaged fixture index {}: {}", root + INDEX, e.toString());
            }
        } else {
            log.info("No packaged fixture index at {}; the adapter doctor will report no fixtures", root + INDEX);
        }
        index = out;
        return out;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
