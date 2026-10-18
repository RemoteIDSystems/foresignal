package ai.foresignal.news;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SQL of the news tables (prompt 133): {@code news_story} and {@code news_article}. The story resolver and the
 * coverage job write through it; the API and the analyses read.
 */
@Component
public class NewsStoryStore {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private static final String STORY_COLUMNS = "id, title, title_tokens, central_article, centroid, centroid_model,"
            + " centroid_n, first_seen_at, last_seen_at, article_count, outlet_count, evidence_group_count, topic_ids,"
            + " region_codes, coverage::text AS coverage, blindspot, skewed, n_rated, state_affiliated_share,"
            + " corroborated, corroborated_by, situation_ids, blindspot_finding_id, created_at, updated_at";

    private static final String ARTICLE_COLUMNS = "id, source_id, external_id, seq, story_id, link_id,"
            + " evidence_group_key, evidence_group_id, outlet_id, domain, title, title_tokens, url, language,"
            + " source_country, seen_at, published_at, topic_ids, region_codes, summary, image_url, syndicated,"
            + " wire_originator, similarity, match_method, updated_at";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public NewsStoryStore(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * A story row.
     *
     * @param id the id (the canonical event id)
     * @param title the deterministic title
     * @param titleTokens its tokens
     * @param centralArticle the article the title comes from ({@code source/externalId}), or null
     * @param centroid the title-embedding centroid, or null
     * @param centroidModel the embedding model of the centroid, or null
     * @param centroidN the vectors in the centroid
     * @param firstSeenAt the earliest article
     * @param lastSeenAt the latest article
     * @param articleCount the articles
     * @param outletCount the distinct outlets
     * @param evidenceGroupCount the evidence groups
     * @param topicIds the attention topics
     * @param regionCodes the region codes
     * @param coverage the {@code CoverageStats}
     * @param blindspot {@code left}, {@code right} or null
     * @param skewed the skew judgment
     * @param nRated the rated evidence groups
     * @param stateAffiliatedShare the state-affiliated share
     * @param corroborated whether non-news evidence corroborates it
     * @param corroboratedBy the corroborating canonical events
     * @param situationIds the situations it is a member of
     * @param blindspotFindingId the {@code coverage-blindspot} finding, or null
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record Story(String id, String title, List<String> titleTokens, String centralArticle, float[] centroid,
            String centroidModel, int centroidN, Instant firstSeenAt, Instant lastSeenAt, int articleCount,
            int outletCount, int evidenceGroupCount, List<String> topicIds, List<String> regionCodes,
            Map<String, Object> coverage, String blindspot, boolean skewed, int nRated, double stateAffiliatedShare,
            boolean corroborated, List<String> corroboratedBy, List<String> situationIds, String blindspotFindingId,
            Instant createdAt, Instant updatedAt) {}

    /**
     * An article row.
     *
     * @param id {@code source/externalId}
     * @param sourceId the source
     * @param externalId the record
     * @param seq the version seq
     * @param storyId the story, or null
     * @param linkId the observation link, or null
     * @param evidenceGroupKey the evidence group root, or null
     * @param evidenceGroupId the evidence group id, or null
     * @param outletId the outlet, or null
     * @param domain the domain
     * @param title the title
     * @param titleTokens its tokens
     * @param url the canonical URL
     * @param language the language, or null
     * @param sourceCountry the source country, or null
     * @param seenAt when it was seen
     * @param publishedAt when it was published, or null
     * @param topicIds its topics
     * @param regionCodes its region codes (from the topics)
     * @param summary the feed summary, or null
     * @param imageUrl the image link, or null
     * @param syndicated true for a reprint
     * @param wireOriginator the wire of its group, or null
     * @param similarity the similarity it joined its story with, or null
     * @param matchMethod {@code embedding}, {@code tokens} or {@code first}
     * @param updatedAt the last change
     */
    public record Article(String id, String sourceId, String externalId, long seq, String storyId, String linkId,
            String evidenceGroupKey, String evidenceGroupId, String outletId, String domain, String title,
            List<String> titleTokens, String url, String language, String sourceCountry, Instant seenAt,
            Instant publishedAt, List<String> topicIds, List<String> regionCodes, String summary, String imageUrl,
            boolean syndicated, String wireOriginator, Double similarity, String matchMethod, Instant updatedAt) {

        /** The record ref without seq, {@code record:{source}/{externalId}}. */
        public String recordKey() {
            return sourceId + "/" + externalId;
        }
    }

    /** The article id of a record. */
    public static String articleId(String sourceId, String externalId) {
        return sourceId + "/" + externalId;
    }

    // ------------------------------------------------------------------------- stories

    /** One story. */
    public Optional<Story> story(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return jdbc.query("SELECT " + STORY_COLUMNS + " FROM news_story WHERE id = ?", (rs, i) -> story(rs), id)
                .stream().findFirst();
    }

    /** Stories by id (missing ones left out). */
    public Map<String, Story> stories(Collection<String> ids) {
        Map<String, Story> out = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        for (Story s : jdbc.query("SELECT " + STORY_COLUMNS + " FROM news_story WHERE id = ANY(?)",
                (rs, i) -> story(rs), (Object) new LinkedHashSet<>(ids).toArray(String[]::new))) {
            out.put(s.id(), s);
        }
        return out;
    }

    /**
     * Candidate stories for an article: last seen at or after {@code from}, first seen at or before {@code to}, and
     * sharing a topic or a region (any story when the article has neither), newest first.
     */
    public List<Story> candidates(Instant from, Instant to, Collection<String> topics, Collection<String> regions,
            int limit) {
        boolean any = (topics == null || topics.isEmpty()) && (regions == null || regions.isEmpty());
        String filter = any ? "" : " AND (topic_ids && ?::text[] OR region_codes && ?::text[])";
        List<Object> args = new ArrayList<>(List.of(Timestamp.from(from), Timestamp.from(to)));
        if (!any) {
            args.add(array(topics));
            args.add(array(regions));
        }
        args.add(limit);
        return jdbc.query("SELECT " + STORY_COLUMNS + " FROM news_story WHERE last_seen_at >= ? AND first_seen_at <= ?"
                + filter + " ORDER BY last_seen_at DESC, id LIMIT ?", (rs, i) -> story(rs), args.toArray());
    }

    /** Stories updated at or after {@code since}, oldest change first. */
    public List<Story> updatedSince(Instant since, int limit) {
        return jdbc.query("SELECT " + STORY_COLUMNS + " FROM news_story WHERE updated_at >= ? ORDER BY updated_at, id"
                + " LIMIT ?", (rs, i) -> story(rs), Timestamp.from(since), limit);
    }

    /** Stories last seen at or after {@code since}. */
    public List<Story> activeSince(Instant since, int limit) {
        return jdbc.query("SELECT " + STORY_COLUMNS + " FROM news_story WHERE last_seen_at >= ? ORDER BY last_seen_at"
                + " DESC, id LIMIT ?", (rs, i) -> story(rs), Timestamp.from(since), limit);
    }

    /** Inserts a new story. */
    public void insertStory(String id, String title, List<String> tokens, String centralArticle, Instant seen,
            List<String> topics, List<String> regions, Instant now) {
        jdbc.update("INSERT INTO news_story (id, title, title_tokens, central_article, centroid_n, first_seen_at,"
                        + " last_seen_at, article_count, outlet_count, evidence_group_count, topic_ids, region_codes,"
                        + " coverage, skewed, n_rated, state_affiliated_share, corroborated, corroborated_by,"
                        + " situation_ids, created_at, updated_at) VALUES (?, ?, ?, ?, 0, ?, ?, 0, 0, 0, ?, ?,"
                        + " '{}'::jsonb, false, 0, 0, false, '{}', '{}', ?, ?) ON CONFLICT (id) DO NOTHING",
                id, clip(title, 500), array(tokens), centralArticle, Timestamp.from(seen), Timestamp.from(seen),
                array(topics), array(regions), Timestamp.from(now), Timestamp.from(now));
    }

    /** Rewrites a story's summary columns. */
    public void updateStory(String id, String title, List<String> tokens, String centralArticle, float[] centroid,
            String centroidModel, int centroidN, Instant firstSeen, Instant lastSeen, int articles, int outlets,
            int groups, List<String> topics, List<String> regions, Instant now) {
        jdbc.update("UPDATE news_story SET title = ?, title_tokens = ?, central_article = ?, centroid = ?,"
                        + " centroid_model = ?, centroid_n = ?, first_seen_at = ?, last_seen_at = ?, article_count = ?,"
                        + " outlet_count = ?, evidence_group_count = ?, topic_ids = ?, region_codes = ?, updated_at = ?"
                        + " WHERE id = ?", clip(title, 500), array(tokens), centralArticle,
                centroid == null ? null : Vectors.format(centroid), centroidModel, centroidN, Timestamp.from(firstSeen),
                Timestamp.from(lastSeen), articles, outlets, groups, array(topics), array(regions), Timestamp.from(now),
                id);
    }

    /** Stores a story's coverage statistics. */
    public void updateCoverage(String id, Map<String, Object> coverage, String blindspot, boolean skewed, int nRated,
            double stateShare, Instant now) {
        jdbc.update("UPDATE news_story SET coverage = CAST(? AS jsonb), blindspot = ?, skewed = ?, n_rated = ?,"
                        + " state_affiliated_share = ?, updated_at = ? WHERE id = ?", json.writeValueAsString(coverage),
                blindspot, skewed, nRated, stateShare, Timestamp.from(now), id);
    }

    /** Stores a story's corroboration; true when it changed. */
    public boolean updateCorroboration(String id, boolean corroborated, List<String> by, Instant now) {
        return jdbc.update("UPDATE news_story SET corroborated = ?, corroborated_by = ?, updated_at = ? WHERE id = ?"
                + " AND (corroborated IS DISTINCT FROM ? OR corroborated_by IS DISTINCT FROM ?::text[])", corroborated,
                array(by), Timestamp.from(now), id, corroborated, array(by)) > 0;
    }

    /** Stores a story's situations; true when they changed. */
    public boolean updateSituations(String id, List<String> situationIds, Instant now) {
        return jdbc.update("UPDATE news_story SET situation_ids = ?, updated_at = ? WHERE id = ? AND situation_ids IS"
                + " DISTINCT FROM ?::text[]", array(situationIds), Timestamp.from(now), id, array(situationIds)) > 0;
    }

    /** Records the blindspot finding of a story. */
    public void setBlindspotFinding(String id, String findingId) {
        jdbc.update("UPDATE news_story SET blindspot_finding_id = ? WHERE id = ?", findingId, id);
    }

    // ------------------------------------------------------------------------- articles

    /** The article row of a record, or empty. */
    public Optional<Article> article(String sourceId, String externalId) {
        return jdbc.query("SELECT " + ARTICLE_COLUMNS + " FROM news_article WHERE id = ?", (rs, i) -> article(rs),
                articleId(sourceId, externalId)).stream().findFirst();
    }

    /** The articles of a story, by seq. */
    public List<Article> articles(String storyId) {
        return jdbc.query("SELECT " + ARTICLE_COLUMNS + " FROM news_article WHERE story_id = ? ORDER BY seq, id",
                (rs, i) -> article(rs), storyId);
    }

    /** The articles of several stories, by story then seq. */
    public Map<String, List<Article>> articles(Collection<String> storyIds) {
        Map<String, List<Article>> out = new LinkedHashMap<>();
        if (storyIds == null || storyIds.isEmpty()) {
            return out;
        }
        for (Article a : jdbc.query("SELECT " + ARTICLE_COLUMNS + " FROM news_article WHERE story_id = ANY(?)"
                + " ORDER BY story_id, seq, id", (rs, i) -> article(rs),
                (Object) new LinkedHashSet<>(storyIds).toArray(String[]::new))) {
            out.computeIfAbsent(a.storyId(), k -> new ArrayList<>()).add(a);
        }
        return out;
    }

    /** Articles by record key ({@code source/externalId}); missing ones left out. */
    public Map<String, Article> articlesByKey(Collection<String> keys) {
        Map<String, Article> out = new LinkedHashMap<>();
        if (keys == null || keys.isEmpty()) {
            return out;
        }
        for (Article a : jdbc.query("SELECT " + ARTICLE_COLUMNS + " FROM news_article WHERE id = ANY(?)",
                (rs, i) -> article(rs), (Object) new LinkedHashSet<>(keys).toArray(String[]::new))) {
            out.put(a.id(), a);
        }
        return out;
    }

    /** Inserts or replaces an article row. */
    public void upsertArticle(Article a) {
        jdbc.update("INSERT INTO news_article (" + ARTICLE_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                        + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET seq = EXCLUDED.seq,"
                        + " story_id = EXCLUDED.story_id, link_id = EXCLUDED.link_id, outlet_id = EXCLUDED.outlet_id,"
                        + " domain = EXCLUDED.domain, title = EXCLUDED.title, title_tokens = EXCLUDED.title_tokens,"
                        + " url = EXCLUDED.url, language = EXCLUDED.language, source_country = EXCLUDED.source_country,"
                        + " seen_at = EXCLUDED.seen_at, published_at = EXCLUDED.published_at,"
                        + " topic_ids = EXCLUDED.topic_ids, region_codes = EXCLUDED.region_codes,"
                        + " summary = EXCLUDED.summary, image_url = EXCLUDED.image_url,"
                        + " similarity = EXCLUDED.similarity, match_method = EXCLUDED.match_method,"
                        + " updated_at = EXCLUDED.updated_at",
                a.id(), a.sourceId(), a.externalId(), a.seq(), a.storyId(), a.linkId(), a.evidenceGroupKey(),
                a.evidenceGroupId(), a.outletId(), a.domain(), clip(a.title(), 500), array(a.titleTokens()),
                clip(a.url(), 4000), a.language(), a.sourceCountry(), Timestamp.from(a.seenAt()), ts(a.publishedAt()),
                array(a.topicIds()), array(a.regionCodes()), clip(a.summary(), 500), clip(a.imageUrl(), 4000),
                a.syndicated(), a.wireOriginator(), a.similarity(), a.matchMethod(), Timestamp.from(a.updatedAt()));
    }

    /** Records an article's evidence group and reprint flag. */
    public void updateGroup(String articleId, String groupKey, String groupId, boolean syndicated, String wire) {
        jdbc.update("UPDATE news_article SET evidence_group_key = ?, evidence_group_id = ?, syndicated = ?,"
                + " wire_originator = ? WHERE id = ?", groupKey, groupId, syndicated, wire, articleId);
    }

    /** Deletes article rows seen before {@code before}; returns how many. */
    public int deleteArticlesBefore(Instant before) {
        return jdbc.update("DELETE FROM news_article WHERE seen_at < ?", Timestamp.from(before));
    }

    // ------------------------------------------------------------------------- mapping

    private Story story(ResultSet rs) throws SQLException {
        String coverage = rs.getString("coverage");
        return new Story(rs.getString("id"), rs.getString("title"), strings(rs.getArray("title_tokens")),
                rs.getString("central_article"), Vectors.parse(rs.getString("centroid")),
                rs.getString("centroid_model"), rs.getInt("centroid_n"), instant(rs, "first_seen_at"),
                instant(rs, "last_seen_at"), rs.getInt("article_count"), rs.getInt("outlet_count"),
                rs.getInt("evidence_group_count"), strings(rs.getArray("topic_ids")),
                strings(rs.getArray("region_codes")), coverage == null ? new LinkedHashMap<>()
                        : json.readValue(coverage, MAP), rs.getString("blindspot"), rs.getBoolean("skewed"),
                rs.getInt("n_rated"), rs.getDouble("state_affiliated_share"), rs.getBoolean("corroborated"),
                strings(rs.getArray("corroborated_by")), strings(rs.getArray("situation_ids")),
                rs.getString("blindspot_finding_id"), instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private static Article article(ResultSet rs) throws SQLException {
        return new Article(rs.getString("id"), rs.getString("source_id"), rs.getString("external_id"),
                rs.getLong("seq"), rs.getString("story_id"), rs.getString("link_id"),
                rs.getString("evidence_group_key"), rs.getString("evidence_group_id"), rs.getString("outlet_id"),
                rs.getString("domain"), rs.getString("title"), strings(rs.getArray("title_tokens")),
                rs.getString("url"), rs.getString("language"), rs.getString("source_country"),
                instant(rs, "seen_at"), instant(rs, "published_at"), strings(rs.getArray("topic_ids")),
                strings(rs.getArray("region_codes")), rs.getString("summary"), rs.getString("image_url"),
                rs.getBoolean("syndicated"), rs.getString("wire_originator"), (Double) rs.getObject("similarity"),
                rs.getString("match_method"), instant(rs, "updated_at"));
    }

    static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : (Object[]) array.getArray()) {
            if (o != null) {
                out.add(String.valueOf(o));
            }
        }
        return out;
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime v = rs.getObject(column, OffsetDateTime.class);
        return v == null ? null : v.toInstant();
    }

    private static String[] array(Collection<String> values) {
        return values == null ? new String[0] : values.toArray(String[]::new);
    }

    private static Timestamp ts(Instant i) {
        return i == null ? null : Timestamp.from(i);
    }

    private static String clip(String text, int max) {
        return text == null ? null : text.length() <= max ? text : text.substring(0, max);
    }
}
