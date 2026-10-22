package ai.foresignal.api;

import ai.foresignal.api.dto.SeriesInfoDto;
import ai.foresignal.api.dto.SeriesPageDto;
import ai.foresignal.api.dto.SeriesPointDto;
import ai.foresignal.api.dto.SeriesPointPageDto;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.source.series.EnergySeriesMetadata;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.Egress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * The native SQL behind {@code GET /v1/series} and {@code GET /v1/series/{sourceId}/{seriesId}} (prompt 093).
 *
 * <p>The catalog reads {@code series-latest} heads; the points read {@code series-point} heads in
 * {@code event_time} order, which is served by the partial expression index
 * {@code record_head (source_id, (payload->>'series'), event_time) WHERE kind='series-point'}.
 *
 * <p><strong>Every read is filtered by the caller's {@link Visibility}</strong> (prompt 035 / EGR-1): the sources
 * it may not see (private sources without the grant, those outside its restriction, and on the Ask path those an
 * LLM destination may not receive) are excluded, and its {@link ReadRestriction} is applied through
 * {@link RestrictionClamp}. Series records carry no geometry, so a spatially restricted caller sees no series
 * (fail closed, as for any geometry-less row).
 */
@Component
public class SeriesQueryService {

    /** The point kind. */
    static final String POINT_KIND = SeriesPoint.KIND;

    /** The catalog kind. */
    static final String LATEST_KIND = SeriesPoint.LATEST_KIND;

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;

    /** The egress in force per source (prompt 117), or null in a hand-built service. */
    private ai.foresignal.security.EgressPolicy egressPolicy;

    /** The descriptors, for the attribution fallback (prompt 117), or null in a hand-built service. */
    private ai.foresignal.source.SourceRegistry registry;

    /** Creates the service. */
    public SeriesQueryService(NamedParameterJdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /**
     * Installs the egress policy whose {@code isLocalOnly} gives each catalog entry its {@code egress} (prompt 117).
     * The label is informational; whether a caller may read a source's values is still decided by
     * {@link SeriesEgress} through {@code EgressPolicy.decide}.
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setEgressPolicy(ai.foresignal.security.EgressPolicy egressPolicy) {
        this.egressPolicy = egressPolicy;
    }

    /** Installs the registry whose descriptors supply the attribution fallback and egress label (prompt 117). */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setRegistry(ai.foresignal.source.SourceRegistry registry) {
        this.registry = registry;
    }

    /** A catalog page. */
    public record Catalog(List<SeriesInfoDto> items, String nextCursor) {}

    /**
     * What the caller may read.
     *
     * @param excluded source ids the caller may not see (never null)
     * @param restriction the caller's read restriction ({@link ReadRestriction#NONE} when none)
     */
    public record Visibility(Set<String> excluded, ReadRestriction restriction) {

        /** Everything: for trusted internal callers only. */
        public static final Visibility ALL = new Visibility(Set.of(), ReadRestriction.NONE);

        /** Copies the excluded set; a null restriction becomes {@link ReadRestriction#NONE}. */
        public Visibility {
            excluded = excluded == null ? Set.of() : Set.copyOf(excluded);
            restriction = restriction == null ? ReadRestriction.NONE : restriction;
        }

        /** True when {@code sourceId} may be read at all (not excluded and allowed by the restriction). */
        public boolean allows(String sourceId) {
            return !excluded.contains(sourceId) && restriction.allowsSource(sourceId);
        }
    }

    /** A page of points. */
    public record Points(SeriesPointPageDto page) {}

    private record InfoRow(String sourceId, String externalId, SeriesInfoDto dto) {}

    private record PointRow(String externalId, Instant eventTime, SeriesPointDto dto) {}

    /**
     * The series catalog.
     *
     * @param sources the source ids to include; empty means every source
     * @param q a case-insensitive match on series id, seriesName and area, or null
     * @param cursor the cursor from the previous page, or null
     * @param limit the page size
     * @param visibility what the caller may read
     */
    public Catalog list(List<String> sources, String q, String cursor, int limit, Visibility visibility) {
        Cursor decoded = Cursor.decode(cursor);
        List<String> clamped = RestrictionClamp.clampSources(sources, visibility.restriction());
        if (clamped != null && clamped.isEmpty()) {
            // The request and the restriction are disjoint: nothing matches (intersect, never union).
            return new Catalog(List.of(), null);
        }
        StringBuilder sql = new StringBuilder("SELECT source_id, external_id, payload FROM record_head"
                + " WHERE kind = '" + LATEST_KIND + "' AND tombstone = false");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (sources != null && !sources.isEmpty()) {
            sql.append(" AND source_id IN (:sources)");
            params.addValue("sources", sources);
        }
        appendVisibility(sql, params, visibility);
        if (q != null && !q.isBlank()) {
            sql.append(" AND (lower(payload->>'series') LIKE :q ESCAPE '\\'"
                    + " OR lower(coalesce(payload->>'seriesName','')) LIKE :q ESCAPE '\\'"
                    + " OR lower(coalesce(payload->>'area','')) LIKE :q ESCAPE '\\')");
            params.addValue("q", "%" + q.strip().toLowerCase() + "%");
        }
        if (decoded != null && decoded.kind == Cursor.Kind.CATALOG) {
            sql.append(" AND (source_id, external_id) > (:cursorSource, :cursorExternal)");
            params.addValue("cursorSource", decoded.sourceId);
            params.addValue("cursorExternal", decoded.externalId);
        }
        sql.append(" ORDER BY source_id, external_id LIMIT :limit");
        params.addValue("limit", limit + 1);
        List<InfoRow> rows = jdbc.query(sql.toString(), params, infoRowMapper());
        boolean more = rows.size() > limit;
        List<SeriesInfoDto> items = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            items.add(rows.get(i).dto());
        }
        String next = null;
        if (more && !items.isEmpty()) {
            InfoRow last = rows.get(limit - 1);
            next = new Cursor(Cursor.Kind.CATALOG, last.sourceId(), last.externalId(), null).encode();
        }
        return new Catalog(List.copyOf(items), next);
    }

    /**
     * One series' catalog entry, or empty when no such series exists or the caller may not read it (the two are
     * answered alike, so existence does not leak).
     */
    public Optional<SeriesInfoDto> info(String sourceId, String seriesId, Visibility visibility) {
        if (!visibility.allows(sourceId)) {
            return Optional.empty();
        }
        for (String kind : List.of(LATEST_KIND, POINT_KIND)) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("source", sourceId).addValue("series", seriesId);
            StringBuilder sql = new StringBuilder("SELECT source_id, external_id, payload FROM record_head"
                    + " WHERE kind = '" + kind + "' AND tombstone = false AND source_id = :source"
                    + " AND payload->>'series' = :series");
            appendVisibility(sql, params, visibility);
            sql.append(" LIMIT 1");
            List<InfoRow> rows = jdbc.query(sql.toString(), params, infoRowMapper());
            if (!rows.isEmpty()) {
                return Optional.of(rows.get(0).dto());
            }
        }
        return Optional.empty();
    }

    /**
     * The points of one series in {@code periodStart} order.
     *
     * @param from periodStart at or after this, or null
     * @param to periodStart before this, or null
     * @param cursor the cursor from the previous page, or null
     * @param visibility what the caller may read
     */
    public Points points(String sourceId, SeriesInfoDto info, Instant from, Instant to, String cursor, int limit,
            Visibility visibility) {
        if (!visibility.allows(sourceId)) {
            return new Points(new SeriesPointPageDto(info, List.of(), null, false));
        }
        Cursor decoded = Cursor.decode(cursor);
        StringBuilder sql = new StringBuilder("SELECT external_id, event_time, version, persisted_observed_at,"
                + " payload FROM record_head WHERE kind = '" + POINT_KIND + "' AND tombstone = false"
                + " AND source_id = :source AND payload->>'series' = :series");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("source", sourceId).addValue("series", info.series());
        appendVisibility(sql, params, visibility);
        if (from != null) {
            sql.append(" AND event_time >= :from");
            params.addValue("from", OffsetDateTime.ofInstant(from, ZoneOffset.UTC));
        }
        if (to != null) {
            sql.append(" AND event_time < :to");
            params.addValue("to", OffsetDateTime.ofInstant(to, ZoneOffset.UTC));
        }
        if (decoded != null && decoded.kind == Cursor.Kind.POINTS) {
            sql.append(" AND (event_time, external_id) > (:cursorTime, :cursorExternal)");
            params.addValue("cursorTime", OffsetDateTime.ofInstant(decoded.eventTime, ZoneOffset.UTC));
            params.addValue("cursorExternal", decoded.externalId);
        }
        sql.append(" ORDER BY event_time, external_id LIMIT :limit");
        params.addValue("limit", limit + 1);
        List<PointRow> rows = jdbc.query(sql.toString(), params, pointRowMapper());
        boolean more = rows.size() > limit;
        List<SeriesPointDto> items = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            items.add(rows.get(i).dto());
        }
        String next = null;
        if (more && !items.isEmpty()) {
            PointRow last = rows.get(limit - 1);
            next = new Cursor(Cursor.Kind.POINTS, null, last.externalId(), last.eventTime()).encode();
        }
        return new Points(new SeriesPointPageDto(info, List.copyOf(items), next, more));
    }

    /** Appends the caller's exclusions and restriction to a {@code record_head} query. */
    private static void appendVisibility(StringBuilder sql, MapSqlParameterSource params, Visibility visibility) {
        if (!visibility.excluded().isEmpty()) {
            sql.append(" AND source_id NOT IN (:excludedSources)");
            params.addValue("excludedSources", List.copyOf(visibility.excluded()));
        }
        RestrictionClamp.sqlPredicate("source_id", "kind", null, visibility.restriction(), params, "rc")
                .ifPresent(predicate -> sql.append(" AND ").append(predicate));
    }

    // ---------------------------------------------------------------- mapping

    private RowMapper<InfoRow> infoRowMapper() {
        return (rs, rowNum) -> {
            Map<String, Object> payload = parse(rs.getString("payload"));
            String sourceId = rs.getString("source_id");
            return new InfoRow(sourceId, rs.getString("external_id"), info(sourceId, payload));
        };
    }

    /**
     * The catalog entry of a {@code series-latest} (or {@code series-point}) payload, with the prompt 117 metadata:
     * the payload's own {@link SeriesMeta}, else the 093 mapping table ({@link EnergySeriesMetadata}) for a record
     * written before prompt 117; the egress in force; and the payload's attribution, else the descriptor's.
     */
    SeriesInfoDto info(String sourceId, Map<String, Object> payload) {
        String series = str(payload, "series");
        SeriesMeta meta = SeriesMeta.fromPayload(payload);
        if (meta == null) {
            meta = EnergySeriesMetadata.lookup(sourceId, series, str(payload, "dataset"));
        }
        String attribution = str(payload, "attribution");
        if (attribution == null && registry != null && registry.contains(sourceId)) {
            attribution = registry.descriptor(sourceId).attribution();
        }
        return new SeriesInfoDto(
                sourceId,
                series,
                str(payload, "seriesName"),
                str(payload, "dataset"),
                str(payload, "unit"),
                str(payload, "frequency"),
                str(payload, "area"),
                str(payload, "period"),
                SeriesPoint.number(payload.get("value")),
                str(payload, "status"),
                meta == null ? null : meta.family(),
                meta == null ? null : meta.transform(),
                meta == null ? null : meta.releaseLagDays(),
                egressOf(sourceId, meta),
                meta == null ? null : meta.regionCode(),
                meta == null ? null : meta.topicId(),
                attribution);
    }

    /** The egress label: the policy's value in force, else the payload's, else the descriptor's, else OPEN. */
    private String egressOf(String sourceId, SeriesMeta meta) {
        if (egressPolicy != null) {
            return egressPolicy.isLocalOnly(sourceId) ? Egress.LOCAL_ONLY.name() : Egress.OPEN.name();
        }
        if (meta != null && meta.egress() != null) {
            return meta.egress();
        }
        if (registry != null && registry.contains(sourceId)) {
            return registry.descriptor(sourceId).egress().name();
        }
        return Egress.OPEN.name();
    }

    private RowMapper<PointRow> pointRowMapper() {
        return (rs, rowNum) -> {
            Map<String, Object> payload = parse(rs.getString("payload"));
            Instant eventTime = instant(rs.getObject("event_time", OffsetDateTime.class));
            Instant periodStart = parseInstant(str(payload, "periodStart"));
            Instant periodEnd = parseInstant(str(payload, "periodEnd"));
            @SuppressWarnings("unchecked")
            Map<String, Object> extra = payload.get("extra") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : null;
            SeriesPointDto dto = new SeriesPointDto(
                    str(payload, "period"),
                    periodStart != null ? periodStart : eventTime,
                    periodEnd,
                    SeriesPoint.number(payload.get("value")),
                    str(payload, "unit"),
                    str(payload, "status"),
                    extra,
                    rs.getInt("version"),
                    instant(rs.getObject("persisted_observed_at", OffsetDateTime.class)),
                    null,
                    null);
            return new PointRow(rs.getString("external_id"), eventTime, dto);
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String text) {
        try {
            return text == null ? Map.of() : json.readValue(text, Map.class);
        } catch (JacksonException e) {
            return Map.of();
        }
    }

    private static String str(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Instant parseInstant(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    /**
     * An opaque keyset cursor. Catalog pages key on {@code (source_id, external_id)}, point pages on
     * {@code (event_time, external_id)}.
     */
    record Cursor(Kind kind, String sourceId, String externalId, Instant eventTime) {

        enum Kind {
            CATALOG,
            POINTS
        }

        static Cursor decode(String cursor) {
            if (cursor == null || cursor.isBlank()) {
                return null;
            }
            try {
                String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
                        .split("\u0000", -1);
                if (parts.length == 4 && "p".equals(parts[0])) {
                    return new Cursor(Kind.POINTS, null, parts[1], Instant.parse(parts[2]));
                }
                if (parts.length == 4 && "c".equals(parts[0])) {
                    return new Cursor(Kind.CATALOG, parts[1], parts[2], null);
                }
            } catch (RuntimeException e) {
                return null;
            }
            return null;
        }

        String encode() {
            String payload = kind == Kind.POINTS
                    ? "p\u0000" + externalId + "\u0000" + eventTime + "\u0000"
                    : "c\u0000" + sourceId + "\u0000" + externalId + "\u0000";
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        }
    }
}
