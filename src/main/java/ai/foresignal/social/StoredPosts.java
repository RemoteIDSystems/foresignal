package ai.foresignal.social;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads stored {@code social-post} heads (prompt 135) for deletion sync, originator inheritance and the API: the
 * current payload of a post, the ids a deletion check should look at, and the external ids a source holds.
 */
@Component
public class StoredPosts {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * A post deletion sync re-checks.
     *
     * @param externalId the record's external id
     * @param postId the platform post id
     */
    public record Candidate(String externalId, String postId) {}

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the reader. */
    public StoredPosts(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** The current payload of a stored post, or null. */
    public Map<String, Object> payload(String sourceId, String externalId) {
        List<String> rows = jdbc.queryForList("SELECT payload::text FROM record_head WHERE source_id = :s AND"
                + " external_id = :e AND kind = :k AND NOT tombstone", new MapSqlParameterSource().addValue("s",
                sourceId).addValue("e", externalId).addValue("k", SocialVocabulary.KIND), String.class);
        return rows.isEmpty() || rows.get(0) == null ? null : json.readValue(rows.get(0), MAP);
    }

    /** The current payload of a post stored by any source (the newest head), or null. */
    public Map<String, Object> anyPayload(String externalId) {
        List<String> rows = jdbc.queryForList("SELECT payload::text FROM record_head WHERE external_id = :e AND kind ="
                + " :k AND NOT tombstone ORDER BY seq DESC LIMIT 1", new MapSqlParameterSource().addValue("e",
                externalId).addValue("k", SocialVocabulary.KIND), String.class);
        return rows.isEmpty() || rows.get(0) == null ? null : json.readValue(rows.get(0), MAP);
    }

    /** The external ids of the posts a source holds (live, not deleted), at most {@code limit}. */
    public List<String> externalIds(String sourceId, int limit) {
        return jdbc.queryForList("SELECT external_id FROM record_head WHERE source_id = :s AND kind = :k AND NOT"
                + " tombstone ORDER BY seq DESC LIMIT :n", new MapSqlParameterSource().addValue("s", sourceId)
                .addValue("k", SocialVocabulary.KIND).addValue("n", limit), String.class);
    }

    /**
     * The posts deletion sync should re-check: stored by the source, created since {@code since}, not yet deleted,
     * after {@code afterExternalId} in external-id order.
     */
    public List<Candidate> deletionCandidates(String sourceId, Instant since, String afterExternalId, int limit) {
        return jdbc.query("SELECT external_id, payload->>'postId' AS post_id FROM record_head WHERE source_id = :s AND"
                + " kind = :k AND NOT tombstone AND coalesce((payload->>'deleted')::boolean, false) = false AND"
                + " event_time >= :since AND external_id > :after ORDER BY external_id LIMIT :n",
                new MapSqlParameterSource().addValue("s", sourceId).addValue("k", SocialVocabulary.KIND)
                        .addValue("since", Timestamp.from(since)).addValue("after", afterExternalId == null ? ""
                                : afterExternalId).addValue("n", limit),
                (rs, n) -> new Candidate(rs.getString("external_id"), rs.getString("post_id")));
    }

    /** The posts deleted by deletion sync since a time (for the platform view). */
    public long tombstonedSince(String sourceId, Instant since) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = :s AND kind = :k AND"
                + " coalesce((payload->>'deleted')::boolean, false) AND persisted_received_at >= :since",
                new MapSqlParameterSource().addValue("s", sourceId).addValue("k", SocialVocabulary.KIND)
                        .addValue("since", Timestamp.from(since)), Long.class);
        return n == null ? 0 : n;
    }
}
