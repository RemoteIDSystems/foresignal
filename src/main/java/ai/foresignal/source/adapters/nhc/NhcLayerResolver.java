package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Finds the NHC summary MapServer's layer ids by layer name (prompt 075), so a renumbered service still works.
 *
 * <p>The service document ({@code {service}?f=json}) is read at most once per {@link #CACHE_TTL}; the ids are cached
 * in the adapter state ({@value #LAYERS_KEY}, {@value #LAYERS_AT_KEY}). A service whose document lacks one of the
 * {@link Layer} names fails with an {@link UpstreamFormatException}, and nothing is cached. Verified 2026-10-13:
 * 5 Forecast Points, 6 Forecast Track, 7 Forecast Cone, 8 Watch-Warning (children of 4 Forecast Information).
 */
public final class NhcLayerResolver {

    /** How long resolved ids are reused. */
    public static final Duration CACHE_TTL = Duration.ofHours(24);

    /** The state key of the resolved ids ({@code points=5,track=6,cone=7,warning=8}). */
    public static final String LAYERS_KEY = "nhc:layers";

    /** The state key of when they were resolved (ISO-8601). */
    public static final String LAYERS_AT_KEY = "nhc:layersAt";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The layers read, with their names in the service and the kind of their records. */
    public enum Layer {
        /** The forecast positions. */
        POINTS("points", "Forecast Points", NhcConeMapper.POINT_KIND),
        /** The forecast track line. */
        TRACK("track", "Forecast Track", NhcConeMapper.TRACK_KIND),
        /** The cone of uncertainty. */
        CONE("cone", "Forecast Cone", NhcConeMapper.CONE_KIND),
        /** The coastal watches and warnings. */
        WARNING("warning", "Watch-Warning", NhcConeMapper.WARNING_KIND);

        private final String key;
        private final String serviceName;
        private final String kind;

        Layer(String key, String serviceName, String kind) {
            this.key = key;
            this.serviceName = serviceName;
            this.kind = kind;
        }

        /** The short key used in state and scopes. */
        public String key() {
            return key;
        }

        /** The layer's name in the service document. */
        public String serviceName() {
            return serviceName;
        }

        /** The kind of the layer's records. */
        public String kind() {
            return kind;
        }
    }

    private NhcLayerResolver() {}

    /**
     * The layer ids, from the cache while fresh, else from the service document.
     *
     * @param ctx the fetch context
     * @param service the MapServer URL
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException when the document is not JSON or lacks a layer name
     */
    public static Map<Layer, Integer> resolve(FetchContext ctx, URI service) throws AdapterException {
        Map<Layer, Integer> cached = cached(ctx.state(), ctx.now());
        if (cached != null) {
            return cached;
        }
        UpstreamResponse response = ctx.http().get(URI.create(service.toString().replaceAll("/+$", "") + "?f=json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException("NHC service document answered HTTP " + response.status()
                    + (response.truncated() ? " (truncated)" : ""), "");
        }
        Map<Layer, Integer> ids = parse(response.body());
        ctx.state().put(LAYERS_KEY, encode(ids));
        ctx.state().put(LAYERS_AT_KEY, ctx.now().toString());
        return ids;
    }

    /**
     * The ids of the four layers in a service document.
     *
     * @param body the {@code ?f=json} document
     * @throws UpstreamFormatException when it is not JSON, has no {@code layers} array or lacks a name
     */
    public static Map<Layer, Integer> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NHC service document is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode layers = root == null ? null : root.get("layers");
        if (layers == null || !layers.isArray()) {
            throw new UpstreamFormatException("NHC service document has no layers array", "");
        }
        Map<Layer, Integer> ids = new LinkedHashMap<>();
        for (Layer layer : Layer.values()) {
            for (JsonNode l : layers) {
                if (l.has("name") && layer.serviceName().equals(l.get("name").asString().strip()) && l.has("id")
                        && l.get("id").isNumber()) {
                    ids.put(layer, l.get("id").intValue());
                    break;
                }
            }
            if (!ids.containsKey(layer)) {
                throw new UpstreamFormatException("NHC service has no layer named '" + layer.serviceName() + "'", "");
            }
        }
        return ids;
    }

    static Map<Layer, Integer> cached(AdapterStateStore state, Instant now) {
        String text = state.get(LAYERS_KEY).orElse(null);
        String at = state.get(LAYERS_AT_KEY).orElse(null);
        if (text == null || at == null) {
            return null;
        }
        try {
            if (Instant.parse(at).plus(CACHE_TTL).isBefore(now)) {
                return null;
            }
            Map<Layer, Integer> ids = new LinkedHashMap<>();
            for (String pair : text.split(",")) {
                String[] kv = pair.split("=", 2);
                for (Layer layer : Layer.values()) {
                    if (layer.key().equals(kv[0])) {
                        ids.put(layer, Integer.parseInt(kv[1]));
                    }
                }
            }
            return ids.size() == Layer.values().length ? ids : null;
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    static String encode(Map<Layer, Integer> ids) {
        StringBuilder sb = new StringBuilder();
        ids.forEach((layer, id) -> sb.append(sb.isEmpty() ? "" : ",").append(layer.key()).append('=').append(id));
        return sb.toString();
    }
}
