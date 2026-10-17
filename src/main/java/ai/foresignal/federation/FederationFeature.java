package ai.foresignal.federation;

import ai.foresignal.config.EdgeMode;
import ai.foresignal.system.FeatureContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * The federation capability flags (prompt 113): {@code federation} (peers, edge-relay ingest and compact bundles,
 * in every mode) and {@code federationServing} (the change feed, stream and snapshot served to peers; off in edge
 * mode).
 */
@Component
public class FederationFeature implements FeatureContributor {

    private final Environment environment;

    /** Creates the contributor. */
    public FederationFeature(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Map<String, Boolean> features() {
        Map<String, Boolean> out = new LinkedHashMap<>();
        out.put("federation", true);
        out.put("federationServing", !EdgeMode.edge(environment));
        return out;
    }
}
