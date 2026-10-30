package ai.foresignal.news;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The news of a user's edition (prompt NP-003): the period's top stories, limited to the owner's news subscriptions
 * ({@link NewsSubscriptionService}) and to sources the owner may see, each titled in the edition's language when NP-002
 * has translated it, so a foreign story reads natively.
 *
 * <p>A story qualifies when at least one of its articles seen in the period passes the filters: a visible source, one of
 * the subscribed outlets (when any), and one of the subscribed languages (when any; the article's language or an
 * {@code ok} translation into it). Stories rank by distinct outlets, then articles, then recency, the coverage the
 * story resolver already measures.
 */
@Component
public class EditionNews {

    /** The most stories an edition lists. */
    public static final int MAX_STORIES = 10;

    private final JdbcTemplate jdbc;
    private final NewsSubscriptionService subscriptions;
    private final NewsTranslationStore translations;

    /** Creates the component. */
    public EditionNews(JdbcTemplate jdbc, NewsSubscriptionService subscriptions, NewsTranslationStore translations) {
        this.jdbc = jdbc;
        this.subscriptions = subscriptions;
        this.translations = translations;
    }

    /**
     * One listed story.
     *
     * @param storyId the story
     * @param title the title shown: translated into the edition's language when available, else the original
     * @param originalTitle the untouched original title
     * @param translated true when {@code title} is a translation
     * @param outletCount distinct outlets covering it
     * @param articleCount its articles
     * @param lastSeenAt its latest article
     */
    public record Story(String storyId, String title, String originalTitle, boolean translated, int outletCount,
            int articleCount, Instant lastSeenAt) {

        /** The story as section data. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("storyId", storyId);
            out.put("ref", "news-story:" + storyId);
            out.put("title", title);
            if (translated) {
                out.put("originalTitle", originalTitle);
            }
            out.put("outletCount", outletCount);
            out.put("articleCount", articleCount);
            out.put("lastSeenAt", lastSeenAt == null ? null : lastSeenAt.toString());
            return out;
        }
    }

    /**
     * The top stories of an edition.
     *
     * @param from the period start
     * @param to the period end
     * @param ownerUserId the owner, whose subscriptions apply; null for none
     * @param language the edition's language, or null to keep the original titles
     * @param hiddenSources sources whose articles do not count
     * @param limit the most stories
     */
    public List<Story> topStories(Instant from, Instant to, UUID ownerUserId, String language,
            Set<String> hiddenSources, int limit) {
        NewsSubscriptionService.Filter filter = subscriptions.filter(ownerUserId);
        StringBuilder sql = new StringBuilder("SELECT s.id, s.title, s.outlet_count, s.article_count, s.last_seen_at"
                + " FROM news_story s WHERE s.article_count > 0 AND s.last_seen_at >= ? AND s.first_seen_at <= ?"
                + " AND EXISTS (SELECT 1 FROM news_article a WHERE a.story_id = s.id AND a.seen_at >= ?"
                + " AND a.seen_at <= ?");
        List<Object> args = new ArrayList<>(List.of(Timestamp.from(from), Timestamp.from(to), Timestamp.from(from),
                Timestamp.from(to)));
        if (hiddenSources != null && !hiddenSources.isEmpty()) {
            sql.append(" AND NOT (a.source_id = ANY(?))");
            args.add(hiddenSources.toArray(String[]::new));
        }
        if (!filter.outletIds().isEmpty()) {
            sql.append(" AND a.outlet_id = ANY(?)");
            args.add(filter.outletIds().toArray(String[]::new));
        }
        if (!filter.languages().isEmpty()) {
            Set<String> primary = new LinkedHashSet<>();
            for (String tag : filter.languages()) {
                primary.add(primary(tag));
            }
            sql.append(" AND (lower(split_part(a.language, '-', 1)) = ANY(?) OR EXISTS (SELECT 1 FROM news_translation t"
                    + " WHERE t.object_type = 'article' AND t.object_id = a.id AND t.status = 'ok'"
                    + " AND t.target_language = ANY(?)))");
            args.add(primary.toArray(String[]::new));
            args.add(filter.languages().toArray(String[]::new));
        }
        sql.append(") ORDER BY s.outlet_count DESC, s.article_count DESC, s.last_seen_at DESC, s.id DESC LIMIT ?");
        args.add(Math.max(1, limit));
        List<Story> rows = jdbc.query(sql.toString(), (rs, i) -> new Story(rs.getString(1), rs.getString(2),
                rs.getString(2), false, rs.getInt(3), rs.getInt(4), rs.getTimestamp(5).toInstant()), args.toArray());
        if (language == null || rows.isEmpty()) {
            return rows;
        }
        List<String> ids = rows.stream().map(Story::storyId).toList();
        Map<String, NewsTranslationStore.Translation> served = translations.served(NewsTranslationStore.STORY, ids,
                List.of(language));
        List<Story> out = new ArrayList<>();
        for (Story s : rows) {
            NewsTranslationStore.Translation t = served.get(s.storyId());
            if (t != null && t.translatedTitle() != null) {
                out.add(new Story(s.storyId(), t.translatedTitle(), s.originalTitle(), true, s.outletCount(),
                        s.articleCount(), s.lastSeenAt()));
            } else {
                out.add(s);
            }
        }
        return out;
    }

    /** The primary language subtag, lower case ({@code pt-BR} is {@code pt}). */
    static String primary(String tag) {
        int dash = tag.indexOf('-');
        return (dash < 0 ? tag : tag.substring(0, dash)).toLowerCase(Locale.ROOT);
    }
}
