package ai.foresignal.source.formats.social;

import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.TermIndex;
import ai.foresignal.source.spi.AdapterStateStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The standing searches of a search-based social adapter (prompt 135): one query per term (topic terms plus the names
 * of its places, regions and entities), hashtag and followed account of every enabled, standing topic that applies to
 * the network, deduplicated, and a round-robin window over them so a run stays within its budget.
 */
public final class SocialQueries {

    /** A term query. */
    public static final String TERM = "term";

    /** A hashtag query. */
    public static final String HASHTAG = "hashtag";

    /** A followed account. */
    public static final String ACCOUNT = "account";

    /**
     * One standing query.
     *
     * @param kind {@value #TERM}, {@value #HASHTAG} or {@value #ACCOUNT}
     * @param text the term, the hashtag without {@code #}, or the handle
     * @param topicIds the topics that asked for it
     */
    public record Query(String kind, String text, List<String> topicIds) {

        /** The state key of this query's last run. */
        public String stateKey() {
            return "q." + kind + "." + Integer.toHexString(text.toLowerCase(java.util.Locale.ROOT).hashCode());
        }
    }

    private SocialQueries() {}

    /** The standing queries of a network, in a stable order. */
    public static List<Query> forNetwork(TermIndex index, String network) {
        Map<String, Query> out = new LinkedHashMap<>();
        if (index == null) {
            return List.of();
        }
        for (TermIndex.TopicEntry e : index.topics()) {
            SocialTopicBlock b = e.block();
            if (b.cueOnly() || (!b.platforms().isEmpty() && !b.platforms().contains(network))) {
                continue;
            }
            for (String term : TermIndex.queryTerms(e)) {
                add(out, TERM, term, e.topicId());
            }
            for (String tag : b.hashtags()) {
                add(out, HASHTAG, tag.startsWith("#") ? tag.substring(1) : tag, e.topicId());
            }
            for (SocialTopicBlock.Account a : b.accounts()) {
                if (a.platform().equals(network)) {
                    add(out, ACCOUNT, a.handle(), e.topicId());
                }
            }
        }
        return List.copyOf(out.values());
    }

    private static void add(Map<String, Query> out, String kind, String text, String topicId) {
        String key = kind + "|" + text.toLowerCase(java.util.Locale.ROOT);
        Query q = out.get(key);
        if (q == null) {
            out.put(key, new Query(kind, text, List.of(topicId)));
        } else if (!q.topicIds().contains(topicId)) {
            List<String> topics = new ArrayList<>(q.topicIds());
            topics.add(topicId);
            out.put(key, new Query(kind, text, topics));
        }
    }

    /**
     * The next window of queries (round robin), advancing the stored index.
     *
     * @param queries every query
     * @param state the adapter state
     * @param max the most queries this run
     */
    public static List<Query> window(List<Query> queries, AdapterStateStore state, int max) {
        if (queries.isEmpty()) {
            return List.of();
        }
        int next = 0;
        try {
            next = Integer.parseInt(state.get("next").orElse("0"));
        } catch (NumberFormatException e) {
            next = 0;
        }
        next = Math.floorMod(next, queries.size());
        int count = Math.min(max, queries.size());
        List<Query> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add(queries.get((next + i) % queries.size()));
        }
        state.put("next", Integer.toString((next + count) % queries.size()));
        return out;
    }
}
