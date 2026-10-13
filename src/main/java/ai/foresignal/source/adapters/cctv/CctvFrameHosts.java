package ai.foresignal.source.adapters.cctv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The frame-access hints of {@code classpath:cctv/hosts.json} (prompt 072): frame host → how a client can load a
 * frame from it.
 *
 * <ul>
 *   <li>{@code direct}: an {@code image/*} (or HLS) answer to any client;
 *   <li>{@code browser}: non-browser clients get an HTML page or a 403, so the frame loads only from a real browser
 *       {@code <img>};
 *   <li>{@code key}: the host needs a key.
 * </ul>
 *
 * <p>The table is filled in from a probe of one frame per pack with Foresignal's User-Agent and no {@code Referer};
 * {@code Referer} or {@code Origin} requirements are recorded only where the upstream documents them. Foresignal
 * itself never requests a frame.
 */
public final class CctvFrameHosts {

    /** The classpath resource. */
    public static final String RESOURCE = "/cctv/hosts.json";

    /** The access values. */
    public static final Set<String> ACCESS_VALUES = Set.of("direct", "browser", "key");

    /**
     * One host's hint.
     *
     * @param access {@code direct}, {@code browser} or {@code key}
     * @param cors true when the host sends {@code Access-Control-Allow-Origin}
     * @param notes what the probe saw
     */
    public record Hint(String access, boolean cors, String notes) {}

    private static final Map<String, Hint> HOSTS = load();

    private CctvFrameHosts() {}

    /** Every host's hint, keyed by lowercase host. */
    public static Map<String, Hint> all() {
        return HOSTS;
    }

    /**
     * The access hint of {@code host}.
     *
     * @return {@code direct}, {@code browser} or {@code key}; null for a host the table does not list
     */
    public static String access(String host) {
        if (host == null) {
            return null;
        }
        Hint hint = HOSTS.get(host.toLowerCase(Locale.ROOT));
        return hint == null ? null : hint.access();
    }

    private static Map<String, Hint> load() {
        try (InputStream in = CctvFrameHosts.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource " + RESOURCE);
            }
            JsonNode root = JsonMapper.builder().build().readTree(in);
            Map<String, Hint> out = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> e : root.path("hosts").properties()) {
                JsonNode h = e.getValue();
                out.put(e.getKey().toLowerCase(Locale.ROOT), new Hint(h.path("access").asString(),
                        h.path("cors").asBoolean(false), h.path("notes").asString("")));
            }
            return Map.copyOf(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + RESOURCE, e);
        }
    }
}
