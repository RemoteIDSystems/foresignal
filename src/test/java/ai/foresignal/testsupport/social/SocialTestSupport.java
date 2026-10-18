package ai.foresignal.testsupport.social;

import ai.foresignal.social.AuthorKeys;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialLanguages;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.social.StreamLedger;
import ai.foresignal.social.TermIndex;
import ai.foresignal.social.TermIndexService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Builders for the social tests (prompt 135): term indexes from topic blocks, a collector with fixed author keys and
 * no database, a runtime around it, and a cleaner of the social tables for the integration tests.
 */
public final class SocialTestSupport {

    /** A fixed 32-byte salt. */
    public static final byte[] SALT = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    private SocialTestSupport() {}

    /** A social block with English terms (and optional hashtags). */
    public static Map<String, Object> block(List<String> terms, List<String> hashtags) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("terms", Map.of("en", terms));
        m.put("hashtags", hashtags);
        return m;
    }

    /** An index of topics: topic id to its social block map. */
    public static TermIndex index(Map<String, Map<String, Object>> topics) {
        List<TermIndex.TopicEntry> entries = new ArrayList<>();
        topics.forEach((id, block) -> entries.add(new TermIndex.TopicEntry(id, SocialTopicBlock.parse(block, null),
                Map.of(), Map.of(), Map.of())));
        return new TermIndex(entries);
    }

    /** A term-index holder with an installed index. */
    public static TermIndexService terms(TermIndex index) {
        TermIndexService service = new TermIndexService(null, null);
        service.install(index);
        return service;
    }

    /** A collector over an index, English and French, fixed author keys. */
    public static SocialCollector collector(TermIndex index, StoredPosts stored, Clock clock) {
        return new SocialCollector(terms(index), new SocialLanguages(List.of("en", "fr")), new AuthorKeys(SALT),
                stored, clock);
    }

    /** A runtime around a collector, a ledger and a store; every other member is absent. */
    public static SocialRuntime runtime(SocialCollector collector, StreamLedger ledger, StoredPosts stored) {
        return new SocialRuntime(collector, null, null, null, ledger, null, stored);
    }

    /** Empties the social tables (integration tests). */
    public static void clean(JdbcTemplate jdbc) {
        for (String table : List.of("social_volume_hourly", "social_denominator_hourly", "social_coverage",
                "social_stream_gap", "social_spend", "social_quota_use", "social_event_log", "social_platform")) {
            jdbc.update("DELETE FROM " + table);
        }
    }
}
