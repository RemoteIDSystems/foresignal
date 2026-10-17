package ai.foresignal.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * SHA-256 hashes of the bundled registry files a component used (prompt 116), such as
 * {@code originators.json}, {@code canonical-rules.json}, {@code observability.json} or
 * {@code signal-families.json}, read once from {@code src/main/resources/intel/}.
 */
@Component
public class RegistryHashes {

    /** The resource folder registry names are resolved in. */
    public static final String FOLDER = "intel/";

    /** The hash reported for a registry that is not bundled. */
    public static final String MISSING = "missing";

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** The hash of registry {@code name} ({@code intel/{name}}, or a path containing {@code /}). */
    public String hash(String name) {
        return cache.computeIfAbsent(name, RegistryHashes::read);
    }

    /** {@code name → sha256} for each name, sorted by name. */
    public Map<String, String> hashes(Iterable<String> names) {
        Map<String, String> out = new TreeMap<>();
        for (String name : names) {
            out.put(name, hash(name));
        }
        return out;
    }

    private static String read(String name) {
        String path = name.contains("/") ? name : FOLDER + name;
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return MISSING;
        }
        try (InputStream in = resource.getInputStream()) {
            return CanonicalJson.sha256(in.readAllBytes());
        } catch (IOException e) {
            return MISSING;
        }
    }
}
