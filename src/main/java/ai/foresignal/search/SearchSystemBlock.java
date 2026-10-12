package ai.foresignal.search;

import ai.foresignal.system.SystemBlockContributor;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code search} block of the full {@code /v1/system} document ({@code SystemInfo.search},
 * prompt 027): whether semantic search is on (and why not), the active model, the document count,
 * the backlog and the reindex state.
 */
@Component
public class SearchSystemBlock implements SystemBlockContributor {

    private final SearchStatusService status;

    /** Creates the contributor. */
    public SearchSystemBlock(SearchStatusService status) {
        this.status = status;
    }

    @Override
    public String key() {
        return "search";
    }

    @Override
    public Map<String, Object> block() {
        return status.systemBlock();
    }
}
