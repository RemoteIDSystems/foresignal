package ai.foresignal.news;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The {@code news_translation} table (prompt NP-002, migration V27): machine translations of news articles and stories,
 * one row per (object, target language), stored beside the untouched originals. Only {@code ok} rows are served; the
 * other statuses record why an object is untranslated so the job does not retry it on every pass.
 */
@Component
public class NewsTranslationStore {

    /** {@code object_type} of an article. */
    public static final String ARTICLE = "article";

    /** {@code object_type} of a story. */
    public static final String STORY = "story";

    /** A translation that is served. */
    public static final String OK = "ok";

    /** Not attempted: budget exhausted, no chat provider, or an uncertain source language. */
    public static final String SKIPPED = "skipped";

    /** Attempted and failed: a provider error or a rejected output. */
    public static final String FAILED = "failed";

    private static final String COLUMNS = "object_type, object_id, target_language, source_language, source_hash,"
            + " status, translated_title, translated_excerpt, engine, model, reason, cost_usd, translated_at";

    private static final String WRITE_COLUMNS = COLUMNS + ", checked_at";

    /**
     * The retry clause of the pick queries: a failed row, or one skipped for the budget or a missing provider, last
     * checked before the retry time. Rows skipped for the same or an uncertain language wait for new content.
     */
    private static final String RETRY = " OR (t.status <> 'ok' AND COALESCE(t.reason, '') NOT IN ('same-language',"
            + " 'low-confidence') AND t.checked_at < ?)";

    private final JdbcTemplate jdbc;

    /** Creates the store. */
    public NewsTranslationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * One row.
     *
     * @param objectType {@value #ARTICLE} or {@value #STORY}
     * @param objectId the article id ({@code {sourceId}/{externalId}}) or the story id
     * @param targetLanguage the BCP 47 target language
     * @param sourceLanguage the detected source language, or null
     * @param sourceHash the SHA-256 of the source text
     * @param status {@value #OK}, {@value #SKIPPED} or {@value #FAILED}
     * @param translatedTitle the translated title, or null
     * @param translatedExcerpt the translated excerpt, or null
     * @param engine the engine ({@code llm})
     * @param model the model that answered, or null
     * @param reason why the row is not {@value #OK}, or null
     * @param costUsd what the call cost
     * @param translatedAt when the row was written
     */
    public record Translation(String objectType, String objectId, String targetLanguage, String sourceLanguage,
            String sourceHash, String status, String translatedTitle, String translatedExcerpt, String engine,
            String model, String reason, BigDecimal costUsd, Instant translatedAt) {

        /** True when the row is served. */
        public boolean ok() {
            return OK.equals(status);
        }

        /**
         * The wire fields of {@code NewsArticle}/{@code NewsStory} (NP-001): {@code targetLanguage},
         * {@code translatedTitle}, {@code translatedExcerpt} and {@code translationProvenance}.
         */
        public Map<String, Object> wire() {
            Map<String, Object> provenance = new LinkedHashMap<>();
            provenance.put("engine", engine);
            provenance.put("model", model);
            provenance.put("at", translatedAt == null ? null : translatedAt.toString());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("targetLanguage", targetLanguage);
            out.put("translatedTitle", translatedTitle);
            out.put("translatedExcerpt", translatedExcerpt);
            out.put("translationProvenance", provenance);
            return out;
        }
    }

    /** The row of {@code objectType}/{@code objectId} in {@code targetLanguage}. */
    public Optional<Translation> find(String objectType, String objectId, String targetLanguage) {
        return jdbc.query("SELECT " + COLUMNS + " FROM news_translation WHERE object_type = ? AND object_id = ?"
                + " AND target_language = ?", NewsTranslationStore::row, objectType, objectId, targetLanguage)
                .stream().findFirst();
    }

    /**
     * The served ({@value #OK}) translation of each of {@code objectIds}, in the first of {@code languages} that has
     * one.
     *
     * @return object id to its translation; an object without one is absent
     */
    public Map<String, Translation> served(String objectType, Collection<String> objectIds, List<String> languages) {
        Map<String, Translation> out = new LinkedHashMap<>();
        if (objectIds == null || objectIds.isEmpty() || languages == null || languages.isEmpty()) {
            return out;
        }
        List<Translation> rows = jdbc.query("SELECT " + COLUMNS + " FROM news_translation WHERE object_type = ?"
                + " AND object_id = ANY(?) AND target_language = ANY(?) AND status = 'ok'", NewsTranslationStore::row,
                objectType, objectIds.toArray(String[]::new), languages.toArray(String[]::new));
        for (String language : languages) {
            for (Translation t : rows) {
                if (language.equals(t.targetLanguage())) {
                    out.putIfAbsent(t.objectId(), t);
                }
            }
        }
        return out;
    }

    /** Every served translation of one object, keyed by target language. */
    public Map<String, Translation> servedAll(String objectType, String objectId) {
        Map<String, Translation> out = new LinkedHashMap<>();
        for (Translation t : jdbc.query("SELECT " + COLUMNS + " FROM news_translation WHERE object_type = ?"
                + " AND object_id = ? AND status = 'ok' ORDER BY target_language", NewsTranslationStore::row,
                objectType, objectId)) {
            out.put(t.targetLanguage(), t);
        }
        return out;
    }

    /** Inserts or replaces a row. */
    public void upsert(Translation t) {
        jdbc.update("INSERT INTO news_translation (" + WRITE_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                        + " ?)"
                        + " ON CONFLICT (object_type, object_id, target_language) DO UPDATE SET"
                        + " source_language = EXCLUDED.source_language, source_hash = EXCLUDED.source_hash,"
                        + " status = EXCLUDED.status, translated_title = EXCLUDED.translated_title,"
                        + " translated_excerpt = EXCLUDED.translated_excerpt, engine = EXCLUDED.engine,"
                        + " model = EXCLUDED.model, reason = EXCLUDED.reason, cost_usd = EXCLUDED.cost_usd,"
                        + " translated_at = EXCLUDED.translated_at, checked_at = EXCLUDED.checked_at",
                t.objectType(), t.objectId(), t.targetLanguage(), t.sourceLanguage(), t.sourceHash(), t.status(),
                clip(t.translatedTitle(), 1000), clip(t.translatedExcerpt(), 1000), t.engine(), clip(t.model(), 200),
                clip(t.reason(), 200), t.costUsd() == null ? BigDecimal.ZERO : t.costUsd(),
                Timestamp.from(t.translatedAt()), Timestamp.from(t.translatedAt()));
    }

    /** Records that a row still matches its object's current content, so the job does not pick it again. */
    public void markChecked(String objectType, String objectId, String targetLanguage, Instant now) {
        jdbc.update("UPDATE news_translation SET checked_at = ? WHERE object_type = ? AND object_id = ?"
                + " AND target_language = ?", Timestamp.from(now), objectType, objectId, targetLanguage);
    }

    /** The USD translation has spent since {@code since}. */
    public BigDecimal spentSince(Instant since) {
        BigDecimal v = jdbc.queryForObject("SELECT COALESCE(SUM(cost_usd), 0) FROM news_translation"
                + " WHERE translated_at >= ?", BigDecimal.class, Timestamp.from(since));
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * The ids of articles seen since {@code since} that have no row in {@code language} for their current content, or
     * a {@value #FAILED} or budget/provider {@value #SKIPPED} row older than {@code retryBefore}; newest first. A row
     * skipped for the same language or an uncertain source language is retried only when the content changes.
     *
     * <p>"Current content" is judged in the caller by hash; this query only narrows to rows that are missing, stale by
     * time ({@code updated_at} after the translation) or due for a retry.
     */
    public List<String> articlesToTranslate(String language, Instant since, Instant retryBefore, int limit) {
        return jdbc.queryForList("SELECT a.id FROM news_article a LEFT JOIN news_translation t"
                + " ON t.object_type = 'article' AND t.object_id = a.id AND t.target_language = ?"
                + " WHERE a.seen_at >= ? AND (t.object_id IS NULL OR t.checked_at < a.updated_at" + RETRY + ")"
                + " ORDER BY a.seen_at DESC, a.id DESC LIMIT ?", String.class, language, Timestamp.from(since),
                Timestamp.from(retryBefore), limit);
    }

    /** As {@link #articlesToTranslate} for stories active since {@code since}. */
    public List<String> storiesToTranslate(String language, Instant since, Instant retryBefore, int limit) {
        return jdbc.queryForList("SELECT s.id FROM news_story s LEFT JOIN news_translation t"
                + " ON t.object_type = 'story' AND t.object_id = s.id AND t.target_language = ?"
                + " WHERE s.article_count > 0 AND s.last_seen_at >= ? AND (t.object_id IS NULL"
                + " OR t.checked_at < s.updated_at" + RETRY + ")"
                + " ORDER BY s.last_seen_at DESC, s.id DESC LIMIT ?", String.class, language, Timestamp.from(since),
                Timestamp.from(retryBefore), limit);
    }

    /** Marks a story changed so the record publisher re-publishes it with its new translation. */
    public void touchStory(String storyId, Instant now) {
        jdbc.update("UPDATE news_story SET updated_at = ? WHERE id = ? AND updated_at < ?", Timestamp.from(now),
                storyId, Timestamp.from(now));
    }

    /** Deletes the rows of articles that no longer exist (they follow the article's retention); returns how many. */
    public int deleteOrphanedArticles() {
        return jdbc.update("DELETE FROM news_translation t WHERE t.object_type = 'article'"
                + " AND NOT EXISTS (SELECT 1 FROM news_article a WHERE a.id = t.object_id)");
    }

    private static Translation row(ResultSet rs, int i) throws SQLException {
        Timestamp at = rs.getTimestamp("translated_at");
        return new Translation(rs.getString("object_type"), rs.getString("object_id"),
                rs.getString("target_language"), rs.getString("source_language"), rs.getString("source_hash"),
                rs.getString("status"), rs.getString("translated_title"), rs.getString("translated_excerpt"),
                rs.getString("engine"), rs.getString("model"), rs.getString("reason"), rs.getBigDecimal("cost_usd"),
                at == null ? null : at.toInstant());
    }

    private static String clip(String text, int max) {
        return text == null ? null : text.length() <= max ? text : text.substring(0, max);
    }
}
