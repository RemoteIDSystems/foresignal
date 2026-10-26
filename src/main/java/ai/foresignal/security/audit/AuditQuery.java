package ai.foresignal.security.audit;

import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.QueryParams;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the audit log for {@code GET /v1/audit} (prompt 023): newest first, keyset paging on
 * {@code id}, filtered by time, token, operation and status, and (prompt R2-013) actor kind, user and target.
 */
@Component
public class AuditQuery {

    /** Default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** Largest page size. */
    public static final int MAX_LIMIT = 500;

    /**
     * A page.
     *
     * @param items the entries, newest first
     * @param nextCursor the cursor of the next page, or null at the end
     */
    public record Page(List<AuditEventView> items, String nextCursor) {}

    /**
     * The filters.
     *
     * @param since earliest {@code at}, inclusive, or null
     * @param until latest {@code at}, exclusive, or null
     * @param tokenId only this token, or null
     * @param operationId only this operation, or null
     * @param status only this status, or null
     * @param actorKind only this actor kind, or null (prompt R2-013)
     * @param userId only entries attributed to this user, or null
     * @param targetType only this target type, or null
     * @param targetId only this target id, or null
     */
    public record Filter(Instant since, Instant until, UUID tokenId, String operationId, Integer status,
            String actorKind, UUID userId, String targetType, String targetId) {

        /** The filters before prompt R2-013. */
        public Filter(Instant since, Instant until, UUID tokenId, String operationId, Integer status) {
            this(since, until, tokenId, operationId, status, null, null, null, null);
        }

        String canonical() {
            return "audit|" + since + "|" + until + "|" + tokenId + "|" + operationId + "|" + status + "|" + actorKind
                    + "|" + userId + "|" + targetType + "|" + targetId;
        }
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** Creates the query. */
    public AuditQuery(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * One page.
     *
     * @param filter the filters
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     */
    public Page page(Filter filter, String cursor, int limit) {
        String fingerprint = CursorCodec.fingerprint(filter.canonical());
        Cursor.Seq after = CursorCodec.decode(cursor, Cursor.Seq.class, fingerprint);
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_event WHERE true");
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (filter.since() != null) {
            sql.append(" AND at >= :since");
            p.addValue("since", Timestamp.from(filter.since()));
        }
        if (filter.until() != null) {
            sql.append(" AND at < :until");
            p.addValue("until", Timestamp.from(filter.until()));
        }
        if (filter.tokenId() != null) {
            sql.append(" AND token_id = :tokenId");
            p.addValue("tokenId", filter.tokenId());
        }
        if (filter.operationId() != null) {
            sql.append(" AND operation_id = :operationId");
            p.addValue("operationId", filter.operationId());
        }
        if (filter.status() != null) {
            sql.append(" AND status = :status");
            p.addValue("status", filter.status());
        }
        if (filter.actorKind() != null) {
            sql.append(" AND actor_kind = :actorKind");
            p.addValue("actorKind", filter.actorKind());
        }
        if (filter.userId() != null) {
            sql.append(" AND user_id = :userId");
            p.addValue("userId", filter.userId());
        }
        if (filter.targetType() != null) {
            sql.append(" AND target_type = :targetType");
            p.addValue("targetType", filter.targetType());
        }
        if (filter.targetId() != null) {
            sql.append(" AND target_id = :targetId");
            p.addValue("targetId", filter.targetId());
        }
        if (after != null) {
            sql.append(" AND id < :after");
            p.addValue("after", after.q());
        }
        sql.append(" ORDER BY id DESC LIMIT :limit");
        p.addValue("limit", limit + 1);
        List<AuditEventView> rows = new ArrayList<>(jdbc.query(sql.toString(), p, this::row));
        String next = null;
        if (rows.size() > limit) {
            rows = new ArrayList<>(rows.subList(0, limit));
            next = CursorCodec.encode(new Cursor.Seq(rows.get(rows.size() - 1).id(), null), fingerprint);
        }
        return new Page(rows, next);
    }

    /** Parses {@code limit} (1–500, default 100). */
    public static int limit(String raw) {
        return QueryParams.intInRange("limit", raw, 1, MAX_LIMIT, DEFAULT_LIMIT);
    }

    @SuppressWarnings("unchecked")
    private AuditEventView row(ResultSet rs, int n) throws SQLException {
        String detail = rs.getString("detail");
        Map<String, Object> parsed = detail == null ? Map.of() : json.readValue(detail, Map.class);
        Object token = rs.getObject("token_id");
        Timestamp at = rs.getTimestamp("at");
        Integer status = (Integer) rs.getObject("status");
        return new AuditEventView(
                rs.getLong("id"),
                at == null ? null : at.toInstant(),
                rs.getString("actor_kind"),
                uuid(token),
                rs.getString("token_name"),
                rs.getString("principal_id"),
                rs.getString("scope"),
                rs.getString("method"),
                rs.getString("path"),
                rs.getString("operation_id"),
                status,
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("remote_ip"),
                parsed,
                uuid(rs.getObject("user_id")),
                rs.getString("user_email"));
    }

    private static UUID uuid(Object value) {
        return value instanceof UUID uuid ? uuid : value == null ? null : UUID.fromString(value.toString());
    }
}
