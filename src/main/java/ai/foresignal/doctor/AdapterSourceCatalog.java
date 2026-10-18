package ai.foresignal.doctor;

import ai.foresignal.source.SourceRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The adapters' own Java sources packaged into the jar (prompt 140, task 1):
 * {@code META-INF/foresignal/adapter-src/{package}/…java} with {@code adapter-src-index.json} (adapter package →
 * files), written by the Gradle task {@code packageDoctorResources}. Foresignal is MIT, so it may ship its own sources;
 * files ported from third-party code and anything resembling key material are left out at build time.
 *
 * <p>A source id resolves to its files by the package-per-adapter convention: the package of the source's adapter
 * class below {@code ai.foresignal.source.adapters}. Absent-safe: no index or no adapter gives no files.
 */
@Component
public class AdapterSourceCatalog {

    private static final Logger log = LoggerFactory.getLogger(AdapterSourceCatalog.class);

    /** The packaged sources' classpath folder. */
    public static final String ROOT = "META-INF/foresignal/adapter-src/";

    /** The index file's name. */
    public static final String INDEX = "adapter-src-index.json";

    /** The adapters' base package. */
    public static final String BASE_PACKAGE = "ai.foresignal.source.adapters";

    /** The repository path of the adapters' base package, accepted as a prefix of a patch's file name. */
    public static final String REPOSITORY_PREFIX = "src/main/java/ai/foresignal/source/adapters/";

    /**
     * One packaged source file.
     *
     * @param path the path below the adapters' base package, e.g. {@code usgs/UsgsGeoJsonParser.java}
     * @param text the file's text
     */
    public record SourceFile(String path, String text) {}

    private final String root;
    private final JsonMapper json;
    private Function<String, Optional<String>> packageResolver;
    private volatile Map<String, List<String>> index;

    /** The catalog of the packaged sources; source ids resolve through the registry. */
    public AdapterSourceCatalog() {
        this(ROOT, JsonMapper.builder().build(), id -> Optional.empty());
    }

    /**
     * A catalog reading another classpath folder (tests).
     *
     * @param packageResolver source id → adapter package below the base package (for example {@code usgs})
     */
    public AdapterSourceCatalog(String root, JsonMapper json, Function<String, Optional<String>> packageResolver) {
        this.root = root.endsWith("/") ? root : root + "/";
        this.json = json;
        this.packageResolver = packageResolver;
    }

    /** Resolves source ids through the registry's adapter classes. */
    @Autowired
    void useRegistry(ObjectProvider<SourceRegistry> registry) {
        this.packageResolver = sourceId -> {
            SourceRegistry r = registry.getIfAvailable();
            if (r == null) {
                return Optional.empty();
            }
            return r.find(sourceId).flatMap(SourceRegistry.Registration::adapterIfPresent)
                    .map(adapter -> packageOf(adapter.getClass()));
        };
    }

    /** The package of {@code type} below the adapters' base package with slashes, or null when outside it. */
    static String packageOf(Class<?> type) {
        String name = type.getPackageName();
        if (name.equals(BASE_PACKAGE)) {
            return "";
        }
        if (!name.startsWith(BASE_PACKAGE + ".")) {
            return null;
        }
        return name.substring(BASE_PACKAGE.length() + 1).replace('.', '/');
    }

    /** The adapter package of {@code sourceId}, if its adapter lives below the base package. */
    public Optional<String> packageFor(String sourceId) {
        try {
            return packageResolver.apply(sourceId);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** The packaged files of {@code sourceId}'s adapter package; empty when none. */
    public List<SourceFile> files(String sourceId) {
        Optional<String> pkg = packageFor(sourceId);
        if (pkg.isEmpty()) {
            return List.of();
        }
        List<SourceFile> out = new ArrayList<>();
        for (String path : index().getOrDefault(pkg.get(), List.of())) {
            if (path.endsWith("package-info.java")) {
                continue;
            }
            read(path).ifPresent(text -> out.add(new SourceFile(path, text)));
        }
        return out;
    }

    /** True when {@code path} (relative, or with the repository prefix) is a packaged file. */
    public boolean contains(String path) {
        String p = normalize(path);
        if (p == null) {
            return false;
        }
        for (List<String> files : index().values()) {
            if (files.contains(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The path below the base package of a file name a patch may use: relative already, with the repository prefix,
     * or with {@code a/} and {@code b/} diff prefixes. Null for a name that tries to leave the folder.
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        String p = path.strip().replace('\\', '/');
        if (p.startsWith("a/") || p.startsWith("b/")) {
            p = p.substring(2);
        }
        int at = p.indexOf(REPOSITORY_PREFIX);
        if (at >= 0) {
            p = p.substring(at + REPOSITORY_PREFIX.length());
        }
        if (p.startsWith("/") || p.contains("..") || p.isEmpty()) {
            return null;
        }
        return p;
    }

    /** The text of one packaged file, if present. */
    public Optional<String> read(String path) {
        String p = normalize(path);
        if (p == null) {
            return Optional.empty();
        }
        ClassPathResource resource = new ClassPathResource(root + p);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream in = resource.getInputStream()) {
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.warn("Cannot read adapter source {}: {}", p, e.toString());
            return Optional.empty();
        }
    }

    private Map<String, List<String>> index() {
        Map<String, List<String>> current = index;
        if (current != null) {
            return current;
        }
        Map<String, List<String>> out = new LinkedHashMap<>();
        ClassPathResource resource = new ClassPathResource(root + INDEX);
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                JsonNode packages = json.readTree(in).path("packages");
                for (Map.Entry<String, JsonNode> e : packages.properties()) {
                    List<String> files = new ArrayList<>();
                    for (JsonNode f : e.getValue()) {
                        files.add(f.asString());
                    }
                    out.put(e.getKey(), List.copyOf(files));
                }
            } catch (IOException | JacksonException e) {
                log.warn("Cannot read the packaged adapter source index {}: {}", root + INDEX, e.toString());
            }
        } else {
            log.info("No packaged adapter source index at {}; the adapter doctor will work without parser sources",
                    root + INDEX);
        }
        index = out;
        return out;
    }
}
