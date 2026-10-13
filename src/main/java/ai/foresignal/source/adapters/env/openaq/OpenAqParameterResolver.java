package ai.foresignal.source.adapters.env.openaq;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Resolves OpenAQ parameter names ({@code pm25}, {@code o3}, …) to the v3 parameter ids the {@code latest}
 * endpoint is keyed by (prompt 078). Ids are never hard-coded: the list comes from {@code GET /v3/parameters}
 * at most once per {@link #REFRESH} and is cached in the adapter state.
 *
 * <p>v3 lists some names more than once with different units (verified in the v3 OpenAPI document: {@code
 * Parameter{id, name, units, displayName}}), so a name resolves to every id that carries it.
 *
 * <p>State keys: {@value #CACHE_KEY} (the list as JSON) and {@value #CACHED_AT_KEY} (epoch seconds of the last
 * successful refresh).
 */
public final class OpenAqParameterResolver {

    /** The state key of the cached list. */
    public static final String CACHE_KEY = "openaq:parameters";

    /** The state key of the time the list was last read, epoch seconds. */
    public static final String CACHED_AT_KEY = "openaq:parametersAt";

    /** How long a cached list is used before it is read again. */
    public static final Duration REFRESH = Duration.ofHours(24);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One v3 parameter.
     *
     * @param id the parameter id
     * @param name the parameter name, lower case ({@code pm25})
     * @param units the unit as published ({@code µg/m³}, {@code ppm})
     */
    public record Parameter(long id, String name, String units) {}

    private OpenAqParameterResolver() {}

    /**
     * Parses a {@code /v3/parameters} answer ({@code {meta, results: [{id, name, units, …}]}}).
     *
     * @throws UpstreamFormatException when the body is not that shape
     */
    public static List<Parameter> parse(byte[] body) {
        JsonNode root;
        try {
            root = body == null || body.length == 0 ? null : JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("OpenAQ parameters answer is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode results = root == null ? null : root.get("results");
        if (results == null || !results.isArray()) {
            throw new UpstreamFormatException("OpenAQ parameters answer has no results array", "");
        }
        List<Parameter> out = new ArrayList<>();
        for (JsonNode p : results) {
            Long id = JsonProps.integer(p, "id");
            String name = JsonProps.text(p, "name");
            if (id != null && name != null) {
                out.add(new Parameter(id, name.toLowerCase(Locale.ROOT), JsonProps.text(p, "units")));
            }
        }
        if (out.isEmpty()) {
            throw new UpstreamFormatException("OpenAQ parameters answer lists no parameters", "");
        }
        return List.copyOf(out);
    }

    /** True when there is no cached list or it is older than {@link #REFRESH} at {@code now}. */
    public static boolean due(AdapterStateStore state, Instant now) {
        Optional<String> at = state.get(CACHED_AT_KEY);
        if (at.isEmpty() || state.get(CACHE_KEY).isEmpty()) {
            return true;
        }
        try {
            return !Instant.ofEpochSecond(Long.parseLong(at.get().strip())).plus(REFRESH).isAfter(now);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /** Stores {@code parameters} as the cached list read at {@code now}. */
    public static void store(AdapterStateStore state, List<Parameter> parameters, Instant now) {
        ArrayNode array = JSON.createArrayNode();
        for (Parameter p : parameters) {
            ObjectNode o = array.addObject();
            o.put("id", p.id());
            o.put("name", p.name());
            if (p.units() != null) {
                o.put("units", p.units());
            }
        }
        state.put(CACHE_KEY, array.toString());
        state.put(CACHED_AT_KEY, Long.toString(now.getEpochSecond()));
    }

    /** The cached list, or an empty list when there is none or it does not parse. */
    public static List<Parameter> cached(AdapterStateStore state) {
        Optional<String> text = state.get(CACHE_KEY);
        if (text.isEmpty()) {
            return List.of();
        }
        try {
            JsonNode array = JSON.readTree(text.get());
            List<Parameter> out = new ArrayList<>();
            if (array != null && array.isArray()) {
                for (JsonNode p : array) {
                    Long id = JsonProps.integer(p, "id");
                    String name = JsonProps.text(p, "name");
                    if (id != null && name != null) {
                        out.add(new Parameter(id, name, JsonProps.text(p, "units")));
                    }
                }
            }
            return List.copyOf(out);
        } catch (JacksonException e) {
            return List.of();
        }
    }

    /**
     * Every parameter of {@code parameters} carrying each of {@code names} (case-insensitive), in the order of
     * {@code names}; a name with no parameter maps to an empty list.
     */
    public static Map<String, List<Parameter>> byName(List<Parameter> parameters, List<String> names) {
        Map<String, List<Parameter>> out = new LinkedHashMap<>();
        for (String name : names) {
            String wanted = name.strip().toLowerCase(Locale.ROOT);
            List<Parameter> matches = new ArrayList<>();
            for (Parameter p : parameters) {
                if (p.name().equals(wanted)) {
                    matches.add(p);
                }
            }
            out.put(wanted, List.copyOf(matches));
        }
        return out;
    }
}
