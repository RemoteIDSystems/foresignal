package ai.foresignal.testsupport.news;

import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.StoryFamilyHandler;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsArticles;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Helpers of the prompt 133 news tests: articles as the story resolver receives them (no ingest needed), the
 * {@code news-story} rule, and a clean slate of the news and outlet tables.
 */
public final class NewsTestSupport {

    private NewsTestSupport() {}

    /** Empties the news, outlet and rating tables and the {@code news-story} canonical events. */
    public static void clean(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM news_article");
        jdbc.update("DELETE FROM news_story");
        jdbc.update("DELETE FROM news_coverage_daily");
        jdbc.update("DELETE FROM evidence_group WHERE canonical_event_id IN (SELECT id FROM canonical_event"
                + " WHERE family = 'news-story')");
        jdbc.update("DELETE FROM observation_link WHERE canonical_event_id IN (SELECT id FROM canonical_event"
                + " WHERE family = 'news-story')");
        jdbc.update("DELETE FROM canonical_event WHERE family = 'news-story'");
        jdbc.update("DELETE FROM outlet_consensus");
        jdbc.update("DELETE FROM media_rating");
        jdbc.update("DELETE FROM media_outlet");
        jdbc.update("DELETE FROM media_provider");
    }

    /** The {@code news-story} family rule of the bundled rules. */
    public static CanonicalRules.FamilyRule rule(CanonicalRules rules) {
        return rules.family("news-story");
    }

    /**
     * One article version.
     *
     * @param seq the seq
     * @param url the article URL
     * @param title the headline
     * @param topics the attention topics
     * @param seenAt when it was seen
     */
    public static StoryFamilyHandler.Observation article(long seq, String url, String title, List<String> topics,
            Instant seenAt) {
        return article(NewsArticles.GDELT_SOURCE, seq, url, title, topics, seenAt);
    }

    /** One article version of {@code sourceId}. */
    public static StoryFamilyHandler.Observation article(String sourceId, long seq, String url, String title,
            List<String> topics, Instant seenAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("url", ArticleUrls.canonical(url));
        payload.put("domain", ArticleUrls.domain(url));
        payload.put("language", "en");
        payload.put("sourceCountry", "US");
        payload.put("seenAt", seenAt.toString());
        payload.put("topicIds", topics);
        return new StoryFamilyHandler.Observation(seq, sourceId, ArticleUrls.externalId(url), NewsArticles.KIND, 1,
                seenAt, seenAt, payload);
    }
}
