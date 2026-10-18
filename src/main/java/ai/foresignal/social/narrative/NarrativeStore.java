package ai.foresignal.social.narrative;

import ai.foresignal.chronicle.network.MinHashSketch;
import ai.foresignal.news.Vectors;
import ai.foresignal.security.Ulids;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * SQL access to the narrative tables (prompt 136): {@code social_narrative}, {@code social_narrative_member},
 * {@code social_narrative_log}, {@code social_claim}, {@code media_fingerprint}, {@code media_use},
 * {@code social_media_queue} and {@code social_propagation_edge}. The entities only declare the schema.
 */
@Component
public class NarrativeStore {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};
    private static final TypeReference<List<Object>> LIST = new TypeReference<>() {};

    private static final String NARRATIVE_COLUMNS = "id, title, title_source, central_post, central_signature,"
            + " centroid, centroid_model, centroid_n, topic_ids, place_refs, entity_refs, region_codes, languages,"
            + " platforms::text AS platforms, first_observed_at, last_activity_at, post_count, unique_authors,"
            + " evidence_group_count, origin_concentration, velocity, media_fingerprints, news_story_ids, situation_ids,"
            + " status, state, coordination::text AS coordination, coordination_finding_id, merged_into, tlp,"
            + " manifest_id, dirty, llm_extracted_at, llm_post_count, created_at, updated_at";

    private static final String MEMBER_COLUMNS = "id, narrative_id, source_id, external_id, seq, link_id, platform,"
            + " post_key, url, author_key, originator_id, source_key, account_created_at, signature, language,"
            + " created_at,"
            + " link_reason, matched_key, similarity, repost_of, quote_of, reply_to, links, media_urls, fingerprints,"
            + " topic_ids, place_refs, entity_refs, region_codes, claims::text AS claims, evidence_group_key,"
            + " evidence_group_id, centrality, joined_at";

    private static final String CLAIM_COLUMNS = "id, narrative_id, claim_key, type, signal_family, polarity, hedged,"
            + " place::text AS place, place_lon, place_lat, region_code, entity_refs, event_time::text AS event_time,"
            + " actor, support_count, evidence_group_count, first_observed_at, extraction, manifest_id, confidence,"
            + " status, comparison_id, source_key, source_reliability, information_credibility, tlp, created_at,"
            + " updated_at";

    private static final String FINGERPRINT_COLUMNS = "id, pdq, quality, first_seen_at, first_post_ref,"
            + " first_post_key, use_count, platforms, narrative_ids, media_id, reuse_finding_ids, created_at, updated_at";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the store. */
    public NarrativeStore(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The JDBC template (tests and the views). */
    public JdbcTemplate jdbc() {
        return jdbc;
    }

    // ------------------------------------------------------------------------- records

    /**
     * A narrative row.
     *
     * @param id the id (the canonical event id)
     * @param title the deterministic title
     * @param titleSource {@code member} or {@code claims}
     * @param centralPost the most central member ({@code source/externalId}), or null
     * @param centralSignature the central member's text signature, or null
     * @param centroid the text-embedding centroid, or null
     * @param centroidModel its model, or null
     * @param centroidN the vectors in the centroid
     * @param topicIds the attention topics
     * @param placeRefs the 036 places
     * @param entityRefs the entities
     * @param regionCodes the 040 regions
     * @param languages the languages
     * @param platforms the {@code NarrativePlatformStats} list
     * @param firstObservedAt the earliest member
     * @param lastActivityAt the latest member
     * @param postCount the posts
     * @param uniqueAuthors the distinct authors
     * @param evidenceGroupCount the independent origins
     * @param originConcentration the share of posts in the three largest groups
     * @param velocity the hourly velocity ratio, or null
     * @param mediaFingerprints the fingerprint ids
     * @param newsStoryIds the linked 133 stories
     * @param situationIds the linked 118 situations
     * @param status the 137 status
     * @param state the state
     * @param coordination the {@code CoordinationIndicators}, or null
     * @param coordinationFindingId the {@code social-coordination} finding, or null
     * @param mergedInto the survivor when merged, or null
     * @param tlp the TLP label
     * @param manifestId the clusterer manifest, or null
     * @param dirty true when a full refresh is due
     * @param llmExtractedAt the last LLM extraction, or null
     * @param llmPostCount the posts at the last LLM extraction
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record Narrative(String id, String title, String titleSource, String centralPost,
            MinHashSketch centralSignature, float[] centroid, String centroidModel, int centroidN,
            List<String> topicIds, List<String> placeRefs, List<String> entityRefs, List<String> regionCodes,
            List<String> languages, List<Object> platforms, Instant firstObservedAt, Instant lastActivityAt,
            int postCount, int uniqueAuthors, int evidenceGroupCount, double originConcentration, Double velocity,
            List<String> mediaFingerprints, List<String> newsStoryIds, List<String> situationIds, String status,
            String state, Map<String, Object> coordination, String coordinationFindingId, String mergedInto,
            String tlp, String manifestId, boolean dirty, Instant llmExtractedAt, int llmPostCount,
            Instant createdAt, Instant updatedAt) {}

    /**
     * A member row.
     *
     * @param id {@code source/externalId}
     * @param narrativeId the narrative
     * @param sourceId the collecting source
     * @param externalId the record
     * @param seq the version seq
     * @param linkId the observation link, or null
     * @param platform the network
     * @param postKey the post's {@code {platform}:{postId}}
     * @param url the public URL, or null
     * @param authorKey the author key
     * @param originatorId the originator
     * @param sourceKey the reporting source for Admiralty grading ({@code channel:…} or {@code account:…})
     * @param accountCreatedAt when the account was created, or null
     * @param signature the text signature, or null
     * @param language the language, or null
     * @param createdAt the post time
     * @param linkReason why it joined
     * @param matchedKey the member or post it matched, or null
     * @param similarity the similarity it joined with, or null
     * @param repostOf the reposted post key, or null
     * @param quoteOf the quoted post key, or null
     * @param replyTo the post key replied to, or null
     * @param links normalized links
     * @param mediaUrls image and thumbnail URLs
     * @param fingerprints media fingerprint ids
     * @param topicIds attention topics
     * @param placeRefs 036 places
     * @param entityRefs entities
     * @param regionCodes 040 regions
     * @param claims the post's extracted claims
     * @param evidenceGroupKey the evidence-group root, or null
     * @param evidenceGroupId the evidence group, or null
     * @param centrality the similarity to the centroid, or null
     * @param joinedAt when it joined
     */
    public record Member(String id, String narrativeId, String sourceId, String externalId, long seq, String linkId,
            String platform, String postKey, String url, String authorKey, String originatorId, String sourceKey,
            Instant accountCreatedAt, MinHashSketch signature, String language, Instant createdAt, String linkReason,
            String matchedKey, Double similarity, String repostOf, String quoteOf, String replyTo, List<String> links,
            List<String> mediaUrls, List<String> fingerprints, List<String> topicIds, List<String> placeRefs,
            List<String> entityRefs, List<String> regionCodes, List<Object> claims, String evidenceGroupKey,
            String evidenceGroupId, Double centrality, Instant joinedAt) {

        /** The member's record ref without seq. */
        public String recordRef() {
            return ai.foresignal.evidence.RecordRef.format(sourceId, externalId);
        }
    }

    /**
     * A claim row.
     *
     * @param id the id
     * @param narrativeId the narrative
     * @param claimKey type, polarity and place
     * @param type the lexicon type
     * @param signalFamily the signal family
     * @param polarity the polarity
     * @param hedged hedged
     * @param place the {@code ClaimPlace}, or null
     * @param lon the place longitude, or null
     * @param lat the place latitude, or null
     * @param regionCode the place's region, or null
     * @param entityRefs the entities
     * @param eventTime the event time, or null
     * @param actor the actor, or null
     * @param supportCount supporting posts
     * @param evidenceGroupCount independent origins among them
     * @param firstObservedAt the earliest supporting post
     * @param extraction {@code deterministic} or {@code llm}
     * @param manifestId the manifest
     * @param confidence extraction confidence
     * @param status the 137 status
     * @param comparisonId the 137 comparison, or null
     * @param sourceKey the account or channel of the earliest supporting post
     * @param sourceReliability the Admiralty reliability
     * @param informationCredibility the Admiralty credibility
     * @param tlp the TLP label
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record Claim(String id, String narrativeId, String claimKey, String type, String signalFamily,
            String polarity, boolean hedged, Map<String, Object> place, Double lon, Double lat, String regionCode,
            List<String> entityRefs, Map<String, Object> eventTime, String actor, int supportCount,
            int evidenceGroupCount, Instant firstObservedAt, String extraction, String manifestId, double confidence,
            String status, String comparisonId, String sourceKey, String sourceReliability,
            int informationCredibility, String tlp, Instant createdAt, Instant updatedAt) {}

    /**
     * A fingerprint row.
     *
     * @param id the id
     * @param pdq the hash
     * @param quality the quality
     * @param firstSeenAt the first sighting
     * @param firstPostRef the first post's record ref
     * @param firstPostKey the first post's key
     * @param useCount the uses
     * @param platforms the networks
     * @param narrativeIds the narratives
     * @param mediaId the stored image, or null
     * @param reuseFindingIds the reuse findings
     * @param createdAt the creation time
     * @param updatedAt the last change
     */
    public record Fingerprint(String id, String pdq, int quality, Instant firstSeenAt, String firstPostRef,
            String firstPostKey, int useCount, List<String> platforms, List<String> narrativeIds, String mediaId,
            List<String> reuseFindingIds, Instant createdAt, Instant updatedAt) {}

    /**
     * A queued image.
     *
     * @param id the queue id
     * @param url the image URL
     * @param sourceId the post's source
     * @param externalId the post's record
     * @param postKey the post key
     * @param platform the network
     * @param narrativeId the post's narrative, or null
     * @param seenAt when the post was seen
     * @param attempts fetch attempts
     */
    public record QueueItem(String id, String url, String sourceId, String externalId, String postKey,
            String platform, String narrativeId, Instant seenAt, int attempts) {}

    /**
     * A propagation edge.
     *
     * @param from the from node
     * @param to the to node
     * @param type the edge type
     * @param deltaSeconds the time delta
     */
    public record Edge(String from, String to, String type, double deltaSeconds) {}

    // ------------------------------------------------------------------------- narratives

    /** One narrative. */
    public Optional<Narrative> narrative(String id) {
        return jdbc.query("SELECT " + NARRATIVE_COLUMNS + " FROM social_narrative WHERE id = ?",
                (rs, i) -> narrative(rs), id).stream().findFirst();
    }

    /** Narratives by id. */
    public Map<String, Narrative> narratives(Collection<String> ids) {
        Map<String, Narrative> out = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT " + NARRATIVE_COLUMNS + " FROM social_narrative WHERE id = ANY(?)", rs -> {
            Narrative n = narrative(rs);
            out.put(n.id(), n);
        }, (Object) ids.toArray(String[]::new));
        return out;
    }

    /**
     * Unmerged narratives active at or after {@code from} that share a topic, a 036 place, a region or an entity,
     * latest activity first.
     */
    public List<Narrative> candidates(Instant from, Collection<String> topics, Collection<String> places,
            Collection<String> regions, Collection<String> entities, int limit) {
        return jdbc.query("SELECT " + NARRATIVE_COLUMNS + " FROM social_narrative WHERE merged_into IS NULL AND"
                + " last_activity_at >= ? AND (topic_ids && ?::text[] OR place_refs && ?::text[] OR region_codes && ?::text[] OR entity_refs"
                + " && ?::text[]) ORDER BY last_activity_at DESC, id LIMIT ?", (rs, i) -> narrative(rs), Timestamp.from(from),
                array(topics), array(places), array(regions), array(entities), limit);
    }

    /**
     * A page of unmerged narratives, latest activity first.
     *
     * @param where extra SQL conditions (with {@code ?} placeholders), or empty
     * @param args their arguments
     * @param after the cursor position {@code [lastActivityAt epoch micros, id]}, or null
     * @param limit the page size (one more is read to know whether a next page exists)
     */
    public List<Narrative> page(String where, List<Object> args, Object[] after, int limit) {
        List<Object> a = new ArrayList<>(args);
        StringBuilder sql = new StringBuilder("SELECT " + NARRATIVE_COLUMNS
                + " FROM social_narrative WHERE merged_into IS NULL");
        if (where != null && !where.isEmpty()) {
            sql.append(" AND ").append(where);
        }
        if (after != null) {
            sql.append(" AND (last_activity_at < ? OR (last_activity_at = ? AND id < ?))");
            a.add(after[0]);
            a.add(after[0]);
            a.add(after[1]);
        }
        sql.append(" ORDER BY last_activity_at DESC, id DESC LIMIT ?");
        a.add(limit + 1);
        return jdbc.query(sql.toString(), (rs, i) -> narrative(rs), a.toArray());
    }

    /** Unmerged narratives active at or after {@code since}. */
    public List<Narrative> activeSince(Instant since, int limit) {
        return jdbc.query("SELECT " + NARRATIVE_COLUMNS + " FROM social_narrative WHERE merged_into IS NULL AND"
                + " last_activity_at >= ? ORDER BY last_activity_at DESC, id LIMIT ?", (rs, i) -> narrative(rs),
                Timestamp.from(since), limit);
    }

    /** Unmerged narratives that are not dormant (their state may need to move). */
    public List<Narrative> live(int limit) {
        return jdbc.query("SELECT " + NARRATIVE_COLUMNS + " FROM social_narrative WHERE merged_into IS NULL AND state"
                + " <> 'dormant' ORDER BY last_activity_at DESC, id LIMIT ?", (rs, i) -> narrative(rs), limit);
    }

    /** Narratives waiting for a full refresh, oldest change first. */
    public List<String> dirty(int limit) {
        return jdbc.queryForList("SELECT id FROM social_narrative WHERE dirty AND merged_into IS NULL ORDER BY"
                + " updated_at, id LIMIT ?", String.class, limit);
    }

    /** Inserts a new narrative seeded by one post. */
    public void insertNarrative(String id, String title, String centralPost, MinHashSketch signature,
            List<String> topics, List<String> places, List<String> entities, List<String> regions,
            List<String> languages, Instant firstObservedAt, String tlp, String manifestId, Instant now) {
        jdbc.update("INSERT INTO social_narrative (id, title, title_source, central_post, central_signature, centroid_n,"
                        + " topic_ids, place_refs, entity_refs, region_codes, languages, platforms, first_observed_at,"
                        + " last_activity_at, post_count, unique_authors, evidence_group_count, origin_concentration,"
                        + " media_fingerprints, news_story_ids, situation_ids, status, state, tlp, manifest_id, dirty,"
                        + " llm_post_count, created_at, updated_at) VALUES (?, ?, 'member', ?, ?, 0, ?, ?, ?, ?, ?,"
                        + " '[]'::jsonb, ?, ?, 0, 0, 0, 0, '{}', '{}', '{}', ?, ?, ?, ?, true, 0, ?, ?)"
                        + " ON CONFLICT (id) DO NOTHING",
                id, clip(title, 300), centralPost, signature == null ? null : signature.toBytes(), array(topics),
                array(places), array(entities), array(regions), array(languages), Timestamp.from(firstObservedAt),
                Timestamp.from(firstObservedAt), NarrativeVocabulary.UNASSESSED, NarrativeVocabulary.EMERGING, tlp,
                manifestId, Timestamp.from(now), Timestamp.from(now));
    }

    /**
     * Rewrites a narrative's derived columns after a full refresh.
     *
     * @param n the narrative with its new values
     */
    public void updateNarrative(Narrative n) {
        jdbc.update("UPDATE social_narrative SET title = ?, title_source = ?, central_post = ?, central_signature = ?,"
                        + " centroid = ?, centroid_model = ?, centroid_n = ?, topic_ids = ?, place_refs = ?,"
                        + " entity_refs = ?, region_codes = ?, languages = ?, platforms = CAST(? AS jsonb),"
                        + " first_observed_at = ?, last_activity_at = ?, post_count = ?, unique_authors = ?,"
                        + " evidence_group_count = ?, origin_concentration = ?, velocity = ?, media_fingerprints = ?,"
                        + " news_story_ids = ?, situation_ids = ?, state = ?, dirty = false, updated_at = ?"
                        + " WHERE id = ?",
                clip(n.title(), 300), n.titleSource(), n.centralPost(),
                n.centralSignature() == null ? null : n.centralSignature().toBytes(),
                n.centroid() == null ? null : Vectors.format(n.centroid()), n.centroidModel(), n.centroidN(),
                array(n.topicIds()), array(n.placeRefs()), array(n.entityRefs()), array(n.regionCodes()),
                array(n.languages()), json.writeValueAsString(n.platforms()), Timestamp.from(n.firstObservedAt()),
                Timestamp.from(n.lastActivityAt()), n.postCount(), n.uniqueAuthors(), n.evidenceGroupCount(),
                n.originConcentration(), n.velocity(), array(n.mediaFingerprints()), array(n.newsStoryIds()),
                array(n.situationIds()), n.state(), Timestamp.from(n.updatedAt()), n.id());
    }

    /** Counts a new member without a full refresh and marks the narrative dirty. */
    public void touch(String id, Instant activity, Instant now) {
        jdbc.update("UPDATE social_narrative SET post_count = post_count + 1, last_activity_at = GREATEST("
                + "last_activity_at, ?), dirty = true, updated_at = ? WHERE id = ?", Timestamp.from(activity),
                Timestamp.from(now), id);
    }

    /** Marks a narrative dirty. */
    public void markDirty(String id, Instant now) {
        jdbc.update("UPDATE social_narrative SET dirty = true, updated_at = ? WHERE id = ?", Timestamp.from(now), id);
    }

    /** Writes a new state (and velocity). */
    public void updateState(String id, String state, Double velocity, Instant now) {
        jdbc.update("UPDATE social_narrative SET state = ?, velocity = ?, updated_at = ? WHERE id = ?", state, velocity,
                Timestamp.from(now), id);
    }

    /** Writes a 137 status (used by 137 and tests). */
    public void updateStatus(String id, String status, Instant now) {
        jdbc.update("UPDATE social_narrative SET status = ?, updated_at = ? WHERE id = ?", status, Timestamp.from(now),
                id);
    }

    /** Marks a narrative merged into another. */
    public void setMerged(String loser, String survivor, Instant now) {
        jdbc.update("UPDATE social_narrative SET merged_into = ?, dirty = false, updated_at = ? WHERE id = ?", survivor,
                Timestamp.from(now), loser);
        jdbc.update("UPDATE social_narrative SET merged_into = ?, updated_at = ? WHERE merged_into = ?", survivor,
                Timestamp.from(now), loser);
    }

    /** Writes the coordination indicators and their finding. */
    public void updateCoordination(String id, Map<String, Object> coordination, String findingId, Instant now) {
        jdbc.update("UPDATE social_narrative SET coordination = CAST(? AS jsonb), coordination_finding_id = COALESCE(?,"
                + " coordination_finding_id), updated_at = ? WHERE id = ?", json.writeValueAsString(coordination),
                findingId, Timestamp.from(now), id);
    }

    /** Records an LLM extraction run. */
    public void markLlm(String id, int posts, Instant now) {
        jdbc.update("UPDATE social_narrative SET llm_extracted_at = ?, llm_post_count = ? WHERE id = ?",
                Timestamp.from(now), posts, id);
    }

    /** LLM extraction runs since {@code since}. */
    public int llmRunsSince(Instant since) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM social_narrative WHERE llm_extracted_at >= ?",
                Integer.class, Timestamp.from(since));
        return n == null ? 0 : n;
    }

    /** The narratives of posts, by post key (unmerged only). */
    public Map<String, List<String>> narrativesOfPosts(Collection<String> postKeys) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        if (postKeys == null || postKeys.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT m.post_key, m.narrative_id FROM social_narrative_member m JOIN social_narrative n ON n.id ="
                + " m.narrative_id WHERE m.post_key = ANY(?) AND n.merged_into IS NULL", rs -> {
                    out.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2));
                }, (Object) postKeys.toArray(String[]::new));
        return out;
    }

    // ------------------------------------------------------------------------- members

    /** The member row of a record, if any. */
    public Optional<Member> member(String sourceId, String externalId) {
        return jdbc.query("SELECT " + MEMBER_COLUMNS + " FROM social_narrative_member WHERE id = ?",
                (rs, i) -> member(rs), memberId(sourceId, externalId)).stream().findFirst();
    }

    /** The members of a narrative, earliest first. */
    public List<Member> members(String narrativeId) {
        return jdbc.query("SELECT " + MEMBER_COLUMNS + " FROM social_narrative_member WHERE narrative_id = ?"
                + " ORDER BY created_at, id", (rs, i) -> member(rs), narrativeId);
    }

    /** The latest members of a narrative, newest first. */
    public List<Member> latestMembers(String narrativeId, int limit) {
        return jdbc.query("SELECT " + MEMBER_COLUMNS + " FROM social_narrative_member WHERE narrative_id = ?"
                + " ORDER BY created_at DESC, id DESC LIMIT ?", (rs, i) -> member(rs), narrativeId, limit);
    }

    /** Members of a narrative created in {@code [from, to)}. */
    public List<Member> membersBetween(String narrativeId, Instant from, Instant to) {
        return jdbc.query("SELECT " + MEMBER_COLUMNS + " FROM social_narrative_member WHERE narrative_id = ? AND"
                + " created_at >= ? AND created_at < ? ORDER BY created_at, id", (rs, i) -> member(rs), narrativeId,
                Timestamp.from(from), Timestamp.from(to));
    }

    /**
     * A page of a narrative's members, newest first.
     *
     * @param after the cursor position {@code [createdAt, id]}, or null
     */
    public List<Member> memberPage(String narrativeId, String platform, String evidenceGroupId, Object[] after,
            int limit) {
        List<Object> args = new ArrayList<>(List.of(narrativeId));
        StringBuilder sql = new StringBuilder("SELECT " + MEMBER_COLUMNS
                + " FROM social_narrative_member WHERE narrative_id = ?");
        if (platform != null) {
            sql.append(" AND platform = ?");
            args.add(platform);
        }
        if (evidenceGroupId != null) {
            sql.append(" AND evidence_group_id = ?");
            args.add(evidenceGroupId);
        }
        if (after != null) {
            sql.append(" AND (created_at < ? OR (created_at = ? AND id < ?))");
            args.add(after[0]);
            args.add(after[0]);
            args.add(after[1]);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        return jdbc.query(sql.toString(), (rs, i) -> member(rs), args.toArray());
    }

    /** The unmerged narrative holding a post key, if any (the latest joined). */
    public Optional<Member> memberByPost(String postKey) {
        return jdbc.query("SELECT " + MEMBER_COLUMNS + " FROM social_narrative_member WHERE post_key = ? AND"
                + " narrative_id IN (SELECT id FROM social_narrative WHERE merged_into IS NULL) ORDER BY joined_at DESC"
                + " LIMIT 1", (rs, i) -> member(rs), postKey).stream().findFirst();
    }

    /**
     * Unmerged narratives active at or after {@code from} with a member sharing one of {@code links}; the member key
     * matched for each.
     */
    public Map<String, String> narrativesSharingLinks(Collection<String> links, Instant from, int limit) {
        Map<String, String> out = new LinkedHashMap<>();
        if (links == null || links.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT m.narrative_id, m.id FROM social_narrative_member m JOIN social_narrative n ON n.id ="
                + " m.narrative_id WHERE m.links && ?::text[] AND n.merged_into IS NULL AND n.last_activity_at >= ? ORDER BY"
                + " m.created_at LIMIT ?", rs -> {
                    out.putIfAbsent(rs.getString(1), rs.getString(2));
                }, array(links), Timestamp.from(from), limit);
        return out;
    }

    /**
     * Unmerged narratives active at or after {@code from} with a member using one of {@code mediaUrls} or one of the
     * fingerprints; the member key matched for each.
     */
    public Map<String, String> narrativesSharingMedia(Collection<String> mediaUrls, Collection<String> fingerprints,
            Instant from, int limit) {
        Map<String, String> out = new LinkedHashMap<>();
        if ((mediaUrls == null || mediaUrls.isEmpty()) && (fingerprints == null || fingerprints.isEmpty())) {
            return out;
        }
        jdbc.query("SELECT m.narrative_id, m.id FROM social_narrative_member m JOIN social_narrative n ON n.id ="
                + " m.narrative_id WHERE (m.media_urls && ?::text[] OR m.fingerprints && ?::text[]) AND n.merged_into IS NULL AND"
                + " n.last_activity_at >= ? ORDER BY m.created_at LIMIT ?", rs -> {
                    out.putIfAbsent(rs.getString(1), rs.getString(2));
                }, array(mediaUrls), array(fingerprints), Timestamp.from(from), limit);
        return out;
    }

    /** Inserts a member. */
    public void insertMember(Member m) {
        jdbc.update("INSERT INTO social_narrative_member (id, narrative_id, source_id, external_id, seq, link_id,"
                        + " platform, post_key, url, author_key, originator_id, source_key, account_created_at,"
                        + " signature, language, created_at, link_reason, matched_key, similarity, repost_of, quote_of, reply_to, links,"
                        + " media_urls, fingerprints, topic_ids, place_refs, entity_refs, region_codes, claims,"
                        + " evidence_group_key, evidence_group_id, centrality, joined_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?,"
                        + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?)"
                        + " ON CONFLICT (id) DO NOTHING",
                m.id(), m.narrativeId(), m.sourceId(), m.externalId(), m.seq(), m.linkId(), m.platform(), m.postKey(),
                clip(m.url(), 2000), m.authorKey(), m.originatorId(), clip(m.sourceKey(), 300),
                ts(m.accountCreatedAt()),
                m.signature() == null ? null : m.signature().toBytes(), m.language(), Timestamp.from(m.createdAt()),
                m.linkReason(), m.matchedKey(), m.similarity(), m.repostOf(), m.quoteOf(), m.replyTo(), array(m.links()),
                array(m.mediaUrls()), array(m.fingerprints()), array(m.topicIds()), array(m.placeRefs()),
                array(m.entityRefs()), array(m.regionCodes()), json.writeValueAsString(m.claims()),
                m.evidenceGroupKey(), m.evidenceGroupId(), m.centrality(), Timestamp.from(m.joinedAt()));
    }

    /** Refreshes a member to a newer version of its post (text, links, scopes, claims). */
    public void updateMember(Member m) {
        jdbc.update("UPDATE social_narrative_member SET seq = ?, signature = ?, links = ?, media_urls = ?,"
                        + " topic_ids = ?, place_refs = ?, entity_refs = ?, region_codes = ?, claims = CAST(? AS jsonb)"
                        + " WHERE id = ?", m.seq(), m.signature() == null ? null : m.signature().toBytes(),
                array(m.links()), array(m.mediaUrls()), array(m.topicIds()), array(m.placeRefs()),
                array(m.entityRefs()), array(m.regionCodes()), json.writeValueAsString(m.claims()), m.id());
    }

    /** Sets a member's link id. */
    public void setLinkId(String memberId, String linkId) {
        jdbc.update("UPDATE social_narrative_member SET link_id = ? WHERE id = ?", linkId, memberId);
    }

    /** Writes a member's evidence group and centrality. */
    public void updateGroup(String memberId, String groupKey, String groupId, Double centrality) {
        jdbc.update("UPDATE social_narrative_member SET evidence_group_key = ?, evidence_group_id = ?, centrality = ?"
                + " WHERE id = ?", clip(groupKey, 400), groupId, centrality, memberId);
    }

    /** Adds a fingerprint to the members holding a post key. */
    public void addFingerprint(String postKey, String fingerprintId) {
        jdbc.update("UPDATE social_narrative_member SET fingerprints = array_append(fingerprints, ?) WHERE post_key = ?"
                + " AND NOT (COALESCE(fingerprints, '{}') @> ARRAY[?]::text[])", fingerprintId, postKey, fingerprintId);
    }

    /** Moves members to another narrative. */
    public void moveMembers(Collection<String> memberIds, String narrativeId) {
        if (memberIds.isEmpty()) {
            return;
        }
        jdbc.update("UPDATE social_narrative_member SET narrative_id = ? WHERE id = ANY(?)", narrativeId,
                (Object) memberIds.toArray(String[]::new));
    }

    /** Moves every member of one narrative to another. */
    public void moveAllMembers(String from, String to) {
        jdbc.update("UPDATE social_narrative_member SET narrative_id = ? WHERE narrative_id = ?", to, from);
    }

    /** Moves observation links to another canonical event (a split), clearing their group. */
    public void moveLinks(Collection<String> linkIds, String canonicalEventId) {
        if (linkIds.isEmpty()) {
            return;
        }
        jdbc.update("UPDATE observation_link SET canonical_event_id = ?, evidence_group_id = NULL WHERE id = ANY(?)",
                canonicalEventId, (Object) linkIds.toArray(String[]::new));
    }

    // ------------------------------------------------------------------------- history

    /** Appends a history entry. */
    public void log(String narrativeId, String action, String actor, List<String> related, String from, String to,
            String reason, Instant at) {
        jdbc.update("INSERT INTO social_narrative_log (id, narrative_id, at, action, actor, related_ids, from_value,"
                        + " to_value, reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Ulids.next(at), narrativeId,
                Timestamp.from(at), action, actor == null ? "system" : actor, array(related), clip(from, 300),
                clip(to, 300), clip(reason, 2000));
    }

    /** A narrative's history, oldest first ({@code NarrativeLogEntry}). */
    public List<Map<String, Object>> history(String narrativeId) {
        return jdbc.query("SELECT at, action, actor, related_ids, from_value, to_value, reason FROM social_narrative_log"
                + " WHERE narrative_id = ? ORDER BY at, id", (rs, i) -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("at", instant(rs, "at").toString());
                    out.put("action", rs.getString("action"));
                    out.put("by", rs.getString("actor"));
                    out.put("relatedNarrativeIds", strings(rs.getArray("related_ids")));
                    out.put("from", rs.getString("from_value"));
                    out.put("to", rs.getString("to_value"));
                    out.put("reason", rs.getString("reason"));
                    return out;
                }, narrativeId);
    }

    // ------------------------------------------------------------------------- claims

    /** One claim. */
    public Optional<Claim> claim(String id) {
        return jdbc.query("SELECT " + CLAIM_COLUMNS + " FROM social_claim WHERE id = ?", (rs, i) -> claim(rs), id)
                .stream().findFirst();
    }

    /** The claims of a narrative, most supported first. */
    public List<Claim> claims(String narrativeId) {
        return jdbc.query("SELECT " + CLAIM_COLUMNS + " FROM social_claim WHERE narrative_id = ? ORDER BY support_count"
                + " DESC, first_observed_at, id", (rs, i) -> claim(rs), narrativeId);
    }

    /**
     * A page of claims, newest first.
     *
     * @param where extra SQL conditions, or empty
     * @param args their arguments
     * @param after the cursor position {@code [firstObservedAt, id]}, or null
     */
    public List<Claim> claimPage(String where, List<Object> args, Object[] after, int limit) {
        List<Object> a = new ArrayList<>(args);
        StringBuilder sql = new StringBuilder("SELECT " + CLAIM_COLUMNS + " FROM social_claim c WHERE true");
        if (where != null && !where.isEmpty()) {
            sql.append(" AND ").append(where);
        }
        if (after != null) {
            sql.append(" AND (first_observed_at < ? OR (first_observed_at = ? AND id < ?))");
            a.add(after[0]);
            a.add(after[0]);
            a.add(after[1]);
        }
        sql.append(" ORDER BY first_observed_at DESC, id DESC LIMIT ?");
        a.add(limit + 1);
        return jdbc.query(sql.toString(), (rs, i) -> claim(rs), a.toArray());
    }

    /**
     * Inserts or updates a claim by narrative, key and extraction; the id, status, comparison and creation time of an
     * existing claim are kept.
     *
     * @return the claim id and whether it is new
     */
    public Map.Entry<String, Boolean> upsertClaim(Claim c) {
        List<String> existing = jdbc.queryForList("SELECT id FROM social_claim WHERE narrative_id = ? AND claim_key = ?"
                + " AND extraction = ?", String.class, c.narrativeId(), clip(c.claimKey(), 600), c.extraction());
        if (!existing.isEmpty()) {
            jdbc.update("UPDATE social_claim SET type = ?, signal_family = ?, polarity = ?, hedged = ?,"
                            + " place = CAST(? AS jsonb), place_lon = ?, place_lat = ?, region_code = ?, entity_refs = ?,"
                            + " event_time = CAST(? AS jsonb), actor = ?, support_count = ?, evidence_group_count = ?,"
                            + " first_observed_at = ?, manifest_id = ?, confidence = ?, source_key = ?,"
                            + " source_reliability = ?, information_credibility = CASE status WHEN 'corroborated' THEN 2"
                            + " WHEN 'contradicted' THEN 4 WHEN 'unobserved' THEN 5 ELSE ? END, updated_at = ? WHERE id = ?",
                    c.type(), c.signalFamily(), c.polarity(), c.hedged(), jsonOrNull(c.place()), c.lon(), c.lat(),
                    c.regionCode(), array(c.entityRefs()), jsonOrNull(c.eventTime()), clip(c.actor(), 200),
                    c.supportCount(), c.evidenceGroupCount(), Timestamp.from(c.firstObservedAt()), c.manifestId(),
                    c.confidence(), clip(c.sourceKey(), 300), c.sourceReliability(), c.informationCredibility(),
                    Timestamp.from(c.updatedAt()), existing.get(0));
            return Map.entry(existing.get(0), false);
        }
        jdbc.update("INSERT INTO social_claim (id, narrative_id, claim_key, type, signal_family, polarity, hedged, place,"
                        + " place_lon, place_lat, region_code, entity_refs, event_time, actor, support_count,"
                        + " evidence_group_count, first_observed_at, extraction, manifest_id, confidence, status,"
                        + " comparison_id, source_key, source_reliability, information_credibility, tlp, created_at,"
                        + " updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, CAST(? AS jsonb), ?,"
                        + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.id(), c.narrativeId(), clip(c.claimKey(), 600), c.type(), c.signalFamily(), c.polarity(), c.hedged(),
                jsonOrNull(c.place()), c.lon(), c.lat(), c.regionCode(), array(c.entityRefs()), jsonOrNull(c.eventTime()),
                clip(c.actor(), 200), c.supportCount(), c.evidenceGroupCount(), Timestamp.from(c.firstObservedAt()),
                c.extraction(), c.manifestId(), c.confidence(), c.status(), c.comparisonId(), clip(c.sourceKey(), 300),
                c.sourceReliability(), c.informationCredibility(), c.tlp(), Timestamp.from(c.createdAt()),
                Timestamp.from(c.updatedAt()));
        return Map.entry(c.id(), true);
    }

    /**
     * Sets a claim's 137 status and comparison; its information credibility follows the status (prompt 137).
     *
     * @return true when a row changed
     */
    public boolean updateClaimStatus(String claimId, String status, String comparisonId, Instant now) {
        return jdbc.update("UPDATE social_claim SET status = ?, comparison_id = ?, information_credibility = ?,"
                + " updated_at = ? WHERE id = ? AND (status IS DISTINCT FROM ? OR comparison_id IS DISTINCT FROM ?)",
                status, comparisonId, SocialEvidenceGrades.credibility(status), Timestamp.from(now), claimId, status,
                comparisonId) > 0;
    }

    /**
     * Adds situations linked by place, family and time (prompt 137) to a narrative's {@code situation_ids}.
     *
     * @return true when a row changed
     */
    public boolean addSituations(String narrativeId, Collection<String> situationIds, Instant now) {
        if (situationIds == null || situationIds.isEmpty()) {
            return false;
        }
        return jdbc.update("UPDATE social_narrative SET situation_ids = (SELECT array_agg(DISTINCT x ORDER BY x) FROM"
                + " unnest(COALESCE(situation_ids, '{}'::text[]) || ?) AS x), updated_at = ? WHERE id = ? AND NOT"
                + " (COALESCE(situation_ids, '{}'::text[]) @> ?)", array(situationIds), Timestamp.from(now),
                narrativeId, array(situationIds)) > 0;
    }

    /** Deletes a narrative's claims of one extraction whose keys are no longer derived. */
    public void deleteClaimsExcept(String narrativeId, String extraction, Collection<String> keys) {
        jdbc.update("DELETE FROM social_claim WHERE narrative_id = ? AND extraction = ? AND NOT (claim_key = ANY(?))",
                narrativeId, extraction, array(keys));
    }

    /** Moves claims to another narrative (a merge); duplicates are resolved by the next refresh. */
    public void moveClaims(String from, String to) {
        jdbc.update("DELETE FROM social_claim c WHERE c.narrative_id = ? AND EXISTS (SELECT 1 FROM social_claim s WHERE"
                + " s.narrative_id = ? AND s.claim_key = c.claim_key AND s.extraction = c.extraction)", from, to);
        jdbc.update("UPDATE social_claim SET narrative_id = ? WHERE narrative_id = ?", to, from);
    }

    /**
     * The 137 track record of an account or channel: claims assessed (status other than {@code unassessed},
     * {@code pending} and {@code insufficient-coverage}) and those corroborated.
     *
     * @return {@code [assessed, corroborated]}
     */
    public int[] trackRecord(String sourceKey) {
        if (sourceKey == null) {
            return new int[] {0, 0};
        }
        return jdbc.query("SELECT count(*) FILTER (WHERE status IN ('corroborated', 'unobserved', 'contradicted')),"
                + " count(*) FILTER (WHERE status = 'corroborated') FROM social_claim WHERE source_key = ?",
                rs -> rs.next() ? new int[] {rs.getInt(1), rs.getInt(2)} : new int[] {0, 0}, sourceKey);
    }

    // ------------------------------------------------------------------------- media

    /** One fingerprint. */
    public Optional<Fingerprint> fingerprint(String id) {
        return jdbc.query("SELECT " + FINGERPRINT_COLUMNS + " FROM media_fingerprint WHERE id = ?",
                (rs, i) -> fingerprint(rs), id).stream().findFirst();
    }

    /** Fingerprints by id. */
    public Map<String, Fingerprint> fingerprints(Collection<String> ids) {
        Map<String, Fingerprint> out = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT " + FINGERPRINT_COLUMNS + " FROM media_fingerprint WHERE id = ANY(?)", rs -> {
            Fingerprint f = fingerprint(rs);
            out.put(f.id(), f);
        }, (Object) ids.toArray(String[]::new));
        return out;
    }

    /** The fingerprint with an exact hash, if any. */
    public Optional<Fingerprint> fingerprintByPdq(String pdq) {
        return jdbc.query("SELECT " + FINGERPRINT_COLUMNS + " FROM media_fingerprint WHERE pdq = ?",
                (rs, i) -> fingerprint(rs), pdq).stream().findFirst();
    }

    /** The fingerprint already computed for an image URL, if any. */
    public Optional<String> fingerprintOfUrl(String url) {
        return jdbc.queryForList("SELECT fingerprint_id FROM media_use WHERE url = ? LIMIT 1", String.class, url)
                .stream().findFirst();
    }

    /** Every matchable fingerprint (quality ≥ 50): id and hash. */
    public Map<String, String> matchable() {
        Map<String, String> out = new LinkedHashMap<>();
        jdbc.query("SELECT id, pdq FROM media_fingerprint WHERE quality >= ?", rs -> {
            out.put(rs.getString(1), rs.getString(2));
        }, NarrativeVocabulary.PDQ_MIN_QUALITY);
        return out;
    }

    /** The fingerprint store's start (its earliest first sighting), or null when empty. */
    public Instant storeStart() {
        return jdbc.query("SELECT min(first_seen_at) FROM media_fingerprint",
                rs -> rs.next() && rs.getTimestamp(1) != null ? rs.getTimestamp(1).toInstant() : null);
    }

    /** Inserts a fingerprint. */
    public void insertFingerprint(Fingerprint f) {
        jdbc.update("INSERT INTO media_fingerprint (id, pdq, quality, first_seen_at, first_post_ref, first_post_key,"
                        + " use_count, platforms, narrative_ids, media_id, reuse_finding_ids, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, '{}', ?, ?) ON CONFLICT (pdq) DO NOTHING",
                f.id(), f.pdq(), f.quality(), Timestamp.from(f.firstSeenAt()), clip(f.firstPostRef(), 500),
                f.firstPostKey(), array(f.platforms()), array(f.narrativeIds()), f.mediaId(),
                Timestamp.from(f.createdAt()), Timestamp.from(f.updatedAt()));
    }

    /**
     * Records one use of a fingerprint (once per post).
     *
     * @return true when the use is new
     */
    public boolean recordUse(String fingerprintId, String postRef, String postKey, String url, String platform,
            String narrativeId, Instant seenAt, Instant now) {
        int inserted = jdbc.update("INSERT INTO media_use (id, fingerprint_id, post_ref, post_key, url, platform,"
                + " seen_at) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING", clip(fingerprintId + "|" + postKey,
                300), fingerprintId, clip(postRef, 500), postKey, clip(url, 2000), platform, Timestamp.from(seenAt));
        if (inserted == 0) {
            return false;
        }
        jdbc.update("UPDATE media_fingerprint SET use_count = use_count + 1, platforms = CASE WHEN CAST(? AS text) IS NULL OR"
                + " platforms @> ARRAY[?]::text[] THEN platforms ELSE array_append(platforms, ?) END,"
                + " narrative_ids = CASE WHEN CAST(? AS text) IS NULL OR narrative_ids @> ARRAY[?]::text[] THEN narrative_ids ELSE"
                + " array_append(narrative_ids, ?) END, updated_at = ? WHERE id = ?", platform, platform, platform,
                narrativeId, narrativeId, narrativeId, Timestamp.from(now), fingerprintId);
        return true;
    }

    /** Adds a narrative to a fingerprint. */
    public void addNarrative(String fingerprintId, String narrativeId, Instant now) {
        jdbc.update("UPDATE media_fingerprint SET narrative_ids = array_append(narrative_ids, ?), updated_at = ? WHERE"
                + " id = ? AND NOT (narrative_ids @> ARRAY[?]::text[])", narrativeId, Timestamp.from(now),
                fingerprintId, narrativeId);
    }

    /** Replaces a merged narrative in every fingerprint. */
    public void renameNarrative(String from, String to) {
        jdbc.update("UPDATE media_fingerprint SET narrative_ids = array_append(array_remove(narrative_ids, ?), ?)"
                + " WHERE narrative_ids @> ARRAY[?]::text[] AND NOT (narrative_ids @> ARRAY[?]::text[])", from, to, from,
                to);
        jdbc.update("UPDATE media_fingerprint SET narrative_ids = array_remove(narrative_ids, ?) WHERE narrative_ids @>"
                + " ARRAY[?]::text[]", from, from);
    }

    /** Adds a reuse finding to a fingerprint. */
    public void addReuseFinding(String fingerprintId, String findingId, Instant now) {
        jdbc.update("UPDATE media_fingerprint SET reuse_finding_ids = array_append(reuse_finding_ids, ?), updated_at = ?"
                + " WHERE id = ? AND NOT (reuse_finding_ids @> ARRAY[?]::text[])", findingId, Timestamp.from(now),
                fingerprintId, findingId);
    }

    /** Sets the stored image of a fingerprint. */
    public void setMediaId(String fingerprintId, String mediaId) {
        jdbc.update("UPDATE media_fingerprint SET media_id = COALESCE(media_id, ?) WHERE id = ?", mediaId,
                fingerprintId);
    }

    /** The latest uses of a fingerprint ({@code postRef}, {@code seenAt}, {@code platform}). */
    public List<Map<String, Object>> uses(String fingerprintId, int limit) {
        return jdbc.query("SELECT post_ref, seen_at, platform FROM media_use WHERE fingerprint_id = ? ORDER BY seen_at"
                + " DESC, id LIMIT ?", (rs, i) -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("postRef", rs.getString(1));
                    out.put("seenAt", instant(rs, "seen_at").toString());
                    out.put("platform", rs.getString(3));
                    return out;
                }, fingerprintId, limit);
    }

    /** The fingerprint first-sighting posts of fingerprints: id to post key. */
    public Map<String, String> firstPosts(Collection<String> fingerprintIds) {
        Map<String, String> out = new LinkedHashMap<>();
        if (fingerprintIds == null || fingerprintIds.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT id, first_post_key FROM media_fingerprint WHERE id = ANY(?)", rs -> {
            out.put(rs.getString(1), rs.getString(2));
        }, (Object) fingerprintIds.toArray(String[]::new));
        return out;
    }

    // ------------------------------------------------------------------------- media queue

    /** Queues an image of a post (once per post and URL). */
    public void enqueue(String id, String url, String sourceId, String externalId, String postKey, String platform,
            String narrativeId, Instant seenAt, Instant now) {
        jdbc.update("INSERT INTO social_media_queue (id, url, source_id, external_id, post_key, platform, narrative_id,"
                        + " seen_at, attempts, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'pending', ?)"
                        + " ON CONFLICT (id) DO NOTHING", id, clip(url, 2000), sourceId, externalId, postKey, platform,
                narrativeId, Timestamp.from(seenAt), Timestamp.from(now));
    }

    /** The oldest pending queue items. */
    public List<QueueItem> pending(int limit) {
        return jdbc.query("SELECT id, url, source_id, external_id, post_key, platform, narrative_id, seen_at, attempts"
                + " FROM social_media_queue WHERE status = 'pending' ORDER BY created_at, id LIMIT ?",
                (rs, i) -> new QueueItem(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), instant(rs, "seen_at"), rs.getInt(9)),
                limit);
    }

    /** Finishes a queue item ({@code done}, {@code failed}, or back to {@code pending} with one more attempt). */
    public void finish(String id, String status) {
        if ("pending".equals(status)) {
            jdbc.update("UPDATE social_media_queue SET attempts = attempts + 1 WHERE id = ?", id);
        } else {
            jdbc.update("DELETE FROM social_media_queue WHERE id = ?", id);
        }
    }

    // ------------------------------------------------------------------------- propagation

    /** Replaces a narrative's edges. */
    public void replaceEdges(String narrativeId, List<Edge> edges, Instant now) {
        jdbc.update("DELETE FROM social_propagation_edge WHERE narrative_id = ?", narrativeId);
        List<Object[]> batch = new ArrayList<>();
        for (Edge e : edges) {
            batch.add(new Object[] {Ulids.next(now), narrativeId, clip(e.from(), 500), clip(e.to(), 500), e.type(),
                e.deltaSeconds(), Timestamp.from(now)});
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO social_propagation_edge (id, narrative_id, from_node, to_node, type,"
                    + " delta_seconds, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)", batch);
        }
    }

    /** A narrative's stored edges. */
    public List<Edge> edges(String narrativeId) {
        return jdbc.query("SELECT from_node, to_node, type, delta_seconds FROM social_propagation_edge WHERE"
                + " narrative_id = ? ORDER BY id", (rs, i) -> new Edge(rs.getString(1), rs.getString(2),
                rs.getString(3), rs.getDouble(4)), narrativeId);
    }

    /** Moves edges of a merged narrative to the survivor. */
    public void moveEdges(String from, String to) {
        jdbc.update("UPDATE social_propagation_edge SET narrative_id = ? WHERE narrative_id = ?", to, from);
    }

    // ------------------------------------------------------------------------- mapping

    /** The member id of a record. */
    public static String memberId(String sourceId, String externalId) {
        return sourceId + "/" + externalId;
    }

    private Narrative narrative(ResultSet rs) throws SQLException {
        byte[] sig = rs.getBytes("central_signature");
        return new Narrative(rs.getString("id"), rs.getString("title"), rs.getString("title_source"),
                rs.getString("central_post"), sig == null ? null : MinHashSketch.fromBytes(sig),
                Vectors.parse(rs.getString("centroid")), rs.getString("centroid_model"), rs.getInt("centroid_n"),
                strings(rs.getArray("topic_ids")), strings(rs.getArray("place_refs")),
                strings(rs.getArray("entity_refs")), strings(rs.getArray("region_codes")),
                strings(rs.getArray("languages")), list(rs.getString("platforms")), instant(rs, "first_observed_at"),
                instant(rs, "last_activity_at"), rs.getInt("post_count"), rs.getInt("unique_authors"),
                rs.getInt("evidence_group_count"), rs.getDouble("origin_concentration"),
                (Double) rs.getObject("velocity"), strings(rs.getArray("media_fingerprints")),
                strings(rs.getArray("news_story_ids")), strings(rs.getArray("situation_ids")), rs.getString("status"),
                rs.getString("state"), map(rs.getString("coordination")), rs.getString("coordination_finding_id"),
                rs.getString("merged_into"), rs.getString("tlp"), rs.getString("manifest_id"), rs.getBoolean("dirty"),
                instant(rs, "llm_extracted_at"), rs.getInt("llm_post_count"), instant(rs, "created_at"),
                instant(rs, "updated_at"));
    }

    private Member member(ResultSet rs) throws SQLException {
        byte[] sig = rs.getBytes("signature");
        return new Member(rs.getString("id"), rs.getString("narrative_id"), rs.getString("source_id"),
                rs.getString("external_id"), rs.getLong("seq"), rs.getString("link_id"), rs.getString("platform"),
                rs.getString("post_key"), rs.getString("url"), rs.getString("author_key"),
                rs.getString("originator_id"), rs.getString("source_key"), instant(rs, "account_created_at"),
                sig == null ? null : MinHashSketch.fromBytes(sig), rs.getString("language"), instant(rs, "created_at"),
                rs.getString("link_reason"), rs.getString("matched_key"), (Double) rs.getObject("similarity"),
                rs.getString("repost_of"), rs.getString("quote_of"), rs.getString("reply_to"),
                strings(rs.getArray("links")), strings(rs.getArray("media_urls")),
                strings(rs.getArray("fingerprints")), strings(rs.getArray("topic_ids")),
                strings(rs.getArray("place_refs")), strings(rs.getArray("entity_refs")),
                strings(rs.getArray("region_codes")), list(rs.getString("claims")),
                rs.getString("evidence_group_key"), rs.getString("evidence_group_id"),
                (Double) rs.getObject("centrality"), instant(rs, "joined_at"));
    }

    private Claim claim(ResultSet rs) throws SQLException {
        Map<String, Object> place = rs.getString("place") == null ? null : map(rs.getString("place"));
        Map<String, Object> time = rs.getString("event_time") == null ? null : map(rs.getString("event_time"));
        return new Claim(rs.getString("id"), rs.getString("narrative_id"), rs.getString("claim_key"),
                rs.getString("type"), rs.getString("signal_family"), rs.getString("polarity"), rs.getBoolean("hedged"),
                place, (Double) rs.getObject("place_lon"), (Double) rs.getObject("place_lat"),
                rs.getString("region_code"), strings(rs.getArray("entity_refs")), time, rs.getString("actor"),
                rs.getInt("support_count"), rs.getInt("evidence_group_count"), instant(rs, "first_observed_at"),
                rs.getString("extraction"), rs.getString("manifest_id"), rs.getDouble("confidence"),
                rs.getString("status"), rs.getString("comparison_id"), rs.getString("source_key"),
                rs.getString("source_reliability"), rs.getInt("information_credibility"), rs.getString("tlp"),
                instant(rs, "created_at"), instant(rs, "updated_at"));
    }

    private Fingerprint fingerprint(ResultSet rs) throws SQLException {
        return new Fingerprint(rs.getString("id"), rs.getString("pdq"), rs.getInt("quality"),
                instant(rs, "first_seen_at"), rs.getString("first_post_ref"), rs.getString("first_post_key"),
                rs.getInt("use_count"), strings(rs.getArray("platforms")), strings(rs.getArray("narrative_ids")),
                rs.getString("media_id"), strings(rs.getArray("reuse_finding_ids")), instant(rs, "created_at"),
                instant(rs, "updated_at"));
    }

    private Map<String, Object> map(String text) {
        return text == null ? null : json.readValue(text, MAP);
    }

    private List<Object> list(String text) {
        return text == null ? new ArrayList<>() : json.readValue(text, LIST);
    }

    private String jsonOrNull(Object value) {
        return value == null ? null : json.writeValueAsString(value);
    }

    static List<String> strings(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        Object[] values = (Object[]) array.getArray();
        List<String> out = new ArrayList<>(values.length);
        for (Object v : values) {
            if (v != null) {
                out.add(String.valueOf(v));
            }
        }
        return out;
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toInstant();
    }

    private static Timestamp ts(Instant at) {
        return at == null ? null : Timestamp.from(at);
    }

    static String[] array(Collection<String> values) {
        return values == null ? new String[0] : values.toArray(String[]::new);
    }

    static String clip(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        int end = max;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }
}
