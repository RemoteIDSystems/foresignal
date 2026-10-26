package ai.foresignal.source.aggregator;

import ai.foresignal.api.error.FieldError;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A domain whose backend this server does not have yet (prompt R2-019): its configuration can be read and stored
 * while disabled, but it cannot be enabled, it supersedes nothing, and its status is {@code unconfigured}.
 *
 * @param domain the domain
 * @param name its display name
 */
record PendingDomainAggregator(String domain, String name) implements DomainAggregator {

    @Override
    public boolean implemented() {
        return false;
    }

    @Override
    public List<String> sourceIds() {
        return List.of();
    }

    @Override
    public List<String> supersededSourceIds() {
        return List.of();
    }

    @Override
    public void validate(AggregatorSettings proposed, List<FieldError> errors) {
        if (proposed.enabled()) {
            errors.add(new FieldError("enabled", "this server has no " + name + " backend yet, so it cannot be"
                    + " enabled"));
        }
    }

    @Override
    public void apply(AggregatorSettings settings) {
        // Nothing to configure.
    }

    @Override
    public Map<String, Object> test(AggregatorSettings settings) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", false);
        out.put("reachable", false);
        out.put("sampleCount", null);
        out.put("message", "This server has no " + name + " backend yet.");
        return out;
    }
}
