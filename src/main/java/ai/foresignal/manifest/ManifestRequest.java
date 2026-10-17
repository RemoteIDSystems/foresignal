package ai.foresignal.manifest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a component records about one output (prompt 116), built fluently and passed to
 * {@link ManifestService#record(ManifestRequest)}.
 *
 * @param component the component name, e.g. {@code poisson-tail}
 * @param componentVersion its semantic version
 * @param config parameters and thresholds
 * @param registries the registry files used ({@code originators.json}, …)
 * @param asOfSeq the {@code asOfSeq}, or null
 * @param inputRefs the ordered input refs, or null when only a query describes them
 * @param inputQuery the query definition of the inputs, or null
 * @param model LLM metadata ({@code provider, model, effort, promptHash, toolsHash, samples, templates}), or null
 */
public record ManifestRequest(String component, String componentVersion, Map<String, Object> config,
        List<String> registries, Long asOfSeq, List<String> inputRefs, Map<String, Object> inputQuery,
        Map<String, Object> model) {

    /** Copies the collections. */
    public ManifestRequest {
        config = config == null ? Map.of() : new LinkedHashMap<>(config);
        registries = registries == null ? List.of() : List.copyOf(registries);
        inputRefs = inputRefs == null ? null : new ArrayList<>(inputRefs);
        inputQuery = inputQuery == null ? null : new LinkedHashMap<>(inputQuery);
        model = model == null ? null : new LinkedHashMap<>(model);
    }

    /** A request for {@code component} at {@code version} with nothing else yet. */
    public static ManifestRequest of(String component, String version) {
        return new ManifestRequest(component, version, null, null, null, null, null, null);
    }

    /** With the configuration. */
    public ManifestRequest config(Map<String, Object> value) {
        return new ManifestRequest(component, componentVersion, value, registries, asOfSeq, inputRefs, inputQuery, model);
    }

    /** With the registries used. */
    public ManifestRequest registries(String... names) {
        return new ManifestRequest(component, componentVersion, config, List.of(names), asOfSeq, inputRefs, inputQuery,
                model);
    }

    /** With the {@code asOfSeq}. */
    public ManifestRequest asOf(Long seq) {
        return new ManifestRequest(component, componentVersion, config, registries, seq, inputRefs, inputQuery, model);
    }

    /** With explicit input refs. */
    public ManifestRequest inputs(List<String> refs) {
        return new ManifestRequest(component, componentVersion, config, registries, asOfSeq, refs, inputQuery, model);
    }

    /** With an input query definition. */
    public ManifestRequest query(Map<String, Object> query) {
        return new ManifestRequest(component, componentVersion, config, registries, asOfSeq, inputRefs, query, model);
    }

    /** With LLM metadata. */
    public ManifestRequest model(Map<String, Object> value) {
        return new ManifestRequest(component, componentVersion, config, registries, asOfSeq, inputRefs, inputQuery, value);
    }
}
