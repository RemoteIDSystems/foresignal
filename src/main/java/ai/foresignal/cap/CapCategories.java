package ai.foresignal.cap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CAP 1.2 {@code <category>} of a record kind (prompt 112), from {@code cap/categories.json}: an exact
 * kind entry, else the first {@code contains} entry whose text occurs in the kind, else the default
 * ({@code Other}).
 */
public final class CapCategories {

    /** The classpath resource. */
    public static final String RESOURCE = "/cap/categories.json";

    private final Map<String, String> exact;
    private final List<String[]> contains;
    private final String fallback;

    private CapCategories(Map<String, String> exact, List<String[]> contains, String fallback) {
        this.exact = exact;
        this.contains = contains;
        this.fallback = fallback;
    }

    /** Loads the shipped table. */
    public static CapCategories load() {
        try (InputStream in = CapCategories.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + RESOURCE);
            }
            JsonNode root = JsonMapper.builder().build().readTree(in);
            Map<String, String> exact = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> e : root.get("exact").properties()) {
                exact.put(e.getKey(), e.getValue().asString());
            }
            List<String[]> contains = new ArrayList<>();
            for (JsonNode pair : root.get("contains")) {
                contains.add(new String[] {pair.get(0).asString(), pair.get(1).asString()});
            }
            return new CapCategories(exact, contains, root.get("default").asString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The category of {@code kind}. */
    public String of(String kind) {
        if (kind == null) {
            return fallback;
        }
        String k = kind.toLowerCase(Locale.ROOT);
        String direct = exact.get(k);
        if (direct != null) {
            return direct;
        }
        for (String[] pair : contains) {
            if (k.contains(pair[0])) {
                return pair[1];
            }
        }
        return fallback;
    }
}
