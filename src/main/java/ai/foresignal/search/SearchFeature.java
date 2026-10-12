package ai.foresignal.search;

import ai.foresignal.system.FeatureContributor;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code features.search} and {@code features.semanticSearch} of {@code /v1/system} (prompt 027):
 * text search is always available; semantic search when pgvector, an embedding provider and an index
 * exist.
 */
@Component
public class SearchFeature implements FeatureContributor {

    private final SearchStatusService status;

    /** Creates the contributor. */
    public SearchFeature(SearchStatusService status) {
        this.status = status;
    }

    @Override
    public Map<String, Boolean> features() {
        return Map.of("search", true, "semanticSearch", status.availability().available());
    }
}
