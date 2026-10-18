package ai.foresignal.social;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The hand-off between 137 cues and the search-capable platform adapters (prompt 137): the cue service submits exact
 * queries (or posts an off-platform backend already found) per adapter and runs a manual fetch of that adapter; the
 * adapter drains its requests at the start of the fetch, searches through its own budgets, egress policy and
 * User-Agent, stores what it finds as posts with {@code collection = cue} and the cue id (a targeted sample: counted
 * as {@code sampled}, never baselined, SOC-3), and reports what each request gave.
 */
@Component
public class CueRequests {

    /**
     * One exact query of a cue on one adapter.
     *
     * @param cueId the cue
     * @param sourceId the adapter
     * @param query the exact query string
     * @param language the language the query was built for, or null
     * @param from the window start
     * @param to the window end
     * @param maxResults the most posts
     * @param maxCostUsd the money this request may spend (paid adapters)
     * @param posts posts already found by another backend (stored without a request), or empty
     */
    public record Request(String cueId, String sourceId, String query, String language, Instant from, Instant to,
            int maxResults, BigDecimal maxCostUsd, List<SocialPost> posts) {

        /** Copies the list. */
        public Request {
            posts = posts == null ? List.of() : List.copyOf(posts);
        }
    }

    /**
     * What one request gave.
     *
     * @param cueId the cue
     * @param sourceId the adapter
     * @param query the query
     * @param posts posts found
     * @param costUsd money spent
     * @param error why it failed ({@code BUDGET_EXCEEDED}, an HTTP error…), or null
     */
    public record Result(String cueId, String sourceId, String query, int posts, BigDecimal costUsd, String error) {}

    private final Map<String, List<Request>> pending = new ConcurrentHashMap<>();
    private final Map<String, List<Result>> results = new ConcurrentHashMap<>();

    /** Submits a request for its adapter's next fetch. */
    public void submit(Request request) {
        pending.computeIfAbsent(request.sourceId(), k -> new ArrayList<>());
        synchronized (pending.get(request.sourceId())) {
            pending.get(request.sourceId()).add(request);
        }
    }

    /** Takes every request waiting for an adapter. */
    public List<Request> drain(String sourceId) {
        List<Request> list = pending.get(sourceId);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            List<Request> out = new ArrayList<>(list);
            list.clear();
            return out;
        }
    }

    /** Records what a request gave. */
    public void report(Result result) {
        results.computeIfAbsent(result.cueId(), k -> java.util.Collections.synchronizedList(new ArrayList<>()))
                .add(result);
    }

    /** Takes the results of a cue. */
    public List<Result> take(String cueId) {
        List<Result> list = results.remove(cueId);
        return list == null ? List.of() : new ArrayList<>(list);
    }

    /** True when requests wait for an adapter. */
    public boolean waiting(String sourceId) {
        List<Request> list = pending.get(sourceId);
        return list != null && !list.isEmpty();
    }
}
