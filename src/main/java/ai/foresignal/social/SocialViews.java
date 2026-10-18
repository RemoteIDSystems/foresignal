package ai.foresignal.social;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.observability.AbsenceCheck;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The read side of the social API and MCP tools (prompt 135): stored posts ({@code SocialPost}), mention volume with
 * baselines ({@code SocialVolume}) and collection coverage ({@code SocialCoverage}). Visibility filtering (sources
 * outside a caller's restriction) is applied here from the excluded-source set the caller resolves.
 */
@Component
public class SocialViews {

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 100;

    /** The largest page size. */
    public static final int MAX_LIMIT = 500;

    /** The longest volume window. */
    public static final Duration MAX_VOLUME_WINDOW = Duration.ofDays(400);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final SocialVolumeStore store;
    private final SocialObservability observability;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the views. */
    public SocialViews(NamedParameterJdbcTemplate jdbc, SocialVolumeStore store, SocialObservability observability,
            ObjectProvider<AttentionTopicRepository> topics, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.store = store;
        this.observability = observability;
        this.topics = topics;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A page of results.
     *
     * @param items the items
     * @param nextCursor the next page's cursor, or null
     */
    public record Page(List<Map<String, Object>> items, String nextCursor) {

        /** The JSON form. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("items", items);
            out.put("nextCursor", nextCursor);
            return out;
        }
    }

    /**
     * Which collecting sources a caller may read posts of (prompt 035 restrictions): a spatial restriction or one
     * that leaves out the {@code social-post} kind sees none (posts carry geometry only when the platform gave it).
     *
     * @param allowed the allowed sources, or null for every source
     * @param excluded the hidden sources
     * @param none true when nothing is visible
     */
    public record PostFilter(Set<String> allowed, Set<String> excluded, boolean none) {

        /** No restriction. */
        public static final PostFilter ALL = new PostFilter(null, Set.of(), false);

        /** The filter of a caller's restriction and hidden sources. */
        public static PostFilter of(ai.foresignal.security.restrict.ReadRestriction r, Set<String> excluded) {
            Set<String> hidden = excluded == null ? Set.of() : Set.copyOf(excluded);
            if (r == null || r.isUnrestricted()) {
                return new PostFilter(null, hidden, false);
            }
            boolean spatial = r.bbox() != null || r.area() != null;
            boolean kind = r.allowsKind(SocialVocabulary.KIND);
            return new PostFilter(r.sources() == null || r.sources().isEmpty() ? null : Set.copyOf(r.sources()),
                    hidden, spatial || !kind);
        }
    }

    /** Validates a network name; {@code null} passes. */
    public static String network(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.strip();
        if (!SocialVocabulary.NETWORK.matcher(v).matches()) {
            throw ValidationException.forField(field, "'" + value + "' is not a network name");
        }
        return v;
    }

    // ------------------------------------------------------------------------- posts

    /**
     * Stored posts newest first ({@code listSocialPosts}).
     *
     * @param topic an attention topic id, or null
     * @param platform a network, or null
     * @param q a text filter, or null
     * @param cueId a 137 cue, or null
     * @param since {@code createdAt ≥ since}, or null
     * @param until {@code createdAt < until}, or null
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     * @param filter the sources the caller may read
     */
    public Page posts(String topic, String platform, String q, String cueId, Instant since, Instant until,
            String cursor, int limit, PostFilter filter) {
        if (filter != null && filter.none()) {
            return new Page(List.of(), null);
        }
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("k", SocialVocabulary.KIND)
                .addValue("n", limit + 1);
        StringBuilder sql = new StringBuilder("SELECT source_id, external_id, seq, version, event_time,"
                + " payload::text AS payload FROM record_head WHERE kind = :k AND NOT tombstone");
        if (topic != null) {
            sql.append(" AND payload->'topicIds' @> jsonb_build_array(CAST(:topic AS text))");
            p.addValue("topic", topic);
        }
        if (platform != null) {
            sql.append(" AND payload->>'platform' = :platform");
            p.addValue("platform", platform);
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND payload->>'text' ILIKE :q ESCAPE '\\'");
            p.addValue("q", "%" + q.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        if (cueId != null) {
            sql.append(" AND payload->>'cueId' = :cue");
            p.addValue("cue", cueId);
        }
        if (since != null) {
            sql.append(" AND event_time >= :since");
            p.addValue("since", Timestamp.from(since));
        }
        if (until != null) {
            sql.append(" AND event_time < :until");
            p.addValue("until", Timestamp.from(until));
        }
        appendFilter(sql, p, filter);
        long[] after = decodePostCursor(cursor);
        if (after != null) {
            sql.append(" AND (event_time < :at OR (event_time = :at AND seq < :seq))");
            p.addValue("at", Timestamp.from(Instant.EPOCH.plus(after[0], ChronoUnit.MICROS))).addValue("seq", after[1]);
        }
        sql.append(" ORDER BY event_time DESC, seq DESC LIMIT :n");
        List<Map<String, Object>> items = new ArrayList<>();
        List<long[]> keys = new ArrayList<>();
        jdbc.query(sql.toString(), p, rs -> {
            Instant eventTime = rs.getTimestamp("event_time") == null ? Instant.EPOCH
                    : rs.getTimestamp("event_time").toInstant();
            items.add(postView(rs.getString("source_id"), rs.getString("external_id"), rs.getLong("seq"),
                    rs.getInt("version"), json.readValue(rs.getString("payload"), MAP)));
            keys.add(new long[] {ChronoUnit.MICROS.between(Instant.EPOCH, eventTime), rs.getLong("seq")});
        });
        String next = null;
        if (items.size() > limit) {
            items.remove(limit);
            long[] last = keys.get(limit - 1);
            next = encode(last[0] + "|" + last[1]);
        }
        fillNarratives(items);
        return new Page(items, next);
    }

    /**
     * One post by network and post id ({@code getSocialPost}): the newest head of any collecting source.
     *
     * @throws NotFoundException 404 {@code NOT_FOUND}
     */
    public Map<String, Object> post(String platform, String postId, PostFilter filter) {
        if (filter != null && filter.none()) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "No stored post " + platform + " " + postId + ".");
        }
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("k", SocialVocabulary.KIND)
                .addValue("e", SocialVocabulary.externalId(platform, postId));
        StringBuilder sql = new StringBuilder("SELECT source_id, external_id, seq, version, payload::text AS payload"
                + " FROM record_head WHERE kind = :k AND external_id = :e AND NOT tombstone");
        appendFilter(sql, p, filter);
        List<Map<String, Object>> rows = jdbc.query(sql + " ORDER BY seq DESC LIMIT 1", p,
                (rs, n) -> postView(rs.getString("source_id"), rs.getString("external_id"), rs.getLong("seq"),
                        rs.getInt("version"), json.readValue(rs.getString("payload"), MAP)));
        if (rows.isEmpty()) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "No stored post " + platform + " " + postId + ".");
        }
        fillNarratives(rows);
        return rows.get(0);
    }

    /** Fills {@code narrativeIds} (prompt 136) from the narrative members of the posts' records. */
    private void fillNarratives(List<Map<String, Object>> views) {
        if (views.isEmpty()) {
            return;
        }
        Map<String, Map<String, Object>> byMember = new LinkedHashMap<>();
        for (Map<String, Object> v : views) {
            byMember.put(v.get("sourceId") + "/" + v.get("externalId"), v);
        }
        try {
            jdbc.query("SELECT id, narrative_id FROM social_narrative_member WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", byMember.keySet()), rs -> {
                        Map<String, Object> v = byMember.get(rs.getString("id"));
                        if (v != null) {
                            v.put("narrativeIds", List.of(rs.getString("narrative_id")));
                        }
                    });
        } catch (RuntimeException e) {
            // narratives are optional for post views
        }
    }

    private static void appendFilter(StringBuilder sql, MapSqlParameterSource p, PostFilter filter) {
        if (filter == null) {
            return;
        }
        if (!filter.excluded().isEmpty()) {
            sql.append(" AND source_id NOT IN (:excluded)");
            p.addValue("excluded", filter.excluded());
        }
        if (filter.allowed() != null) {
            sql.append(" AND source_id IN (:allowed)");
            p.addValue("allowed", filter.allowed());
        }
    }

    /** The {@code SocialPost} view of a stored head. */
    public static Map<String, Object> postView(String sourceId, String externalId, long seq, int version,
            Map<String, Object> payload) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : List.of("platform", "postId", "url", "author", "authorKey", "text", "language", "createdAt",
                "observedAt", "replyTo", "quoteOf", "repostOf")) {
            out.put(key, payload.get(key));
        }
        out.put("links", payload.getOrDefault("links", List.of()));
        out.put("hashtags", payload.getOrDefault("hashtags", List.of()));
        out.put("mentions", payload.getOrDefault("mentions", List.of()));
        out.put("media", payload.getOrDefault("media", List.of()));
        out.put("engagement", payload.get("engagement"));
        out.put("geo", payload.get("geo"));
        out.put("channel", payload.get("channel"));
        String collection = payload.get("collection") == null ? SocialVocabulary.COLLECTION_STREAM
                : payload.get("collection").toString();
        out.put("collection", collection);
        out.put("topicIds", payload.getOrDefault("topicIds", List.of()));
        out.put("cueId", payload.get("cueId"));
        out.put("deleted", Boolean.TRUE.equals(payload.get("deleted")));
        out.put("ref", RecordRef.format(sourceId, externalId));
        out.put("sourceId", sourceId);
        out.put("externalId", externalId);
        out.put("seq", seq);
        out.put("version", version);
        out.put("collectionType", SocialVocabulary.collectionTypeOf(collection));
        out.put("narrativeIds", List.of());
        return out;
    }

    // ------------------------------------------------------------------------- volume

    /**
     * Mention volume with baselines ({@code getSocialVolume}).
     *
     * @param scopeType {@code topic}, {@code place} or {@code entity}
     * @param scopeId the id
     * @param platform a network, or null for every network with series
     * @param language a language bucket, or null to sum languages
     * @param metric the metric
     * @param interval {@code PT1H} or {@code P1D}
     * @param since the window start
     * @param until the window end
     * @throws NotFoundException 404 {@code UNKNOWN_PLATFORM} or {@code NOT_FOUND}
     */
    public Map<String, Object> volume(String scopeType, String scopeId, String platform, String language,
            String metric, String interval, Instant since, Instant until) {
        if (!until.isAfter(since)) {
            throw ValidationException.forField("until", "must be after since");
        }
        if (Duration.between(since, until).compareTo(MAX_VOLUME_WINDOW) > 0) {
            throw ValidationException.forField("since", "the window is longer than 400 days");
        }
        List<String> platforms = platform != null ? List.of(platform) : store.platformsOf(scopeType, scopeId);
        if (platform != null && !SocialVocabulary.NETWORKS.contains(platform)
                && !store.exists(scopeType, scopeId, platform) && !store.hasDenominators(platform)) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PLATFORM, "No network '" + platform + "' has social data.");
        }
        if (!scopeKnown(scopeType, scopeId) && !store.exists(scopeType, scopeId, null)) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "No " + scopeType + " '" + scopeId + "' has social"
                    + " volume.");
        }
        Instant from = since.truncatedTo(ChronoUnit.HOURS);
        Instant historyFrom = from.minus(Duration.ofDays(7L * SocialBaseline.WEEKS));
        boolean daily = "P1D".equals(interval);
        List<Map<String, Object>> series = new ArrayList<>();
        boolean anyUnscaled = false;
        for (String net : platforms) {
            Map<Instant, Long> denominators = store.denominators(net, language, historyFrom, until);
            if (!SocialBaseline.hasDenominators(denominators)) {
                denominators = Map.of();
            }
            Map<Instant, Boolean> complete = store.completeHours(net, from, until);
            if (SocialVocabulary.DENOMINATOR.equals(metric)) {
                if (denominators.isEmpty()) {
                    continue;
                }
                series.add(seriesView(net, language, SocialVocabulary.SCOPE_ALL, metric, SocialVocabulary.COMPLETE,
                        new TreeMap<>(denominators), Map.of(), complete, from, until, daily, false));
                continue;
            }
            Map<String, TreeMap<Instant, Long>> byType = new LinkedHashMap<>();
            for (SocialVolumeStore.VolumeRow r : store.volume(scopeType, scopeId, net, language, historyFrom,
                    until)) {
                String group = SocialVocabulary.feedsBaselines(r.collectionType()) ? "eligible"
                        : SocialVocabulary.SAMPLED;
                byType.computeIfAbsent(group, k -> new TreeMap<>()).merge(r.hour(), r.metric(metric), Long::sum);
            }
            for (Map.Entry<String, TreeMap<Instant, Long>> e : byType.entrySet()) {
                boolean eligible = "eligible".equals(e.getKey());
                String type = eligible ? (denominators.isEmpty() ? SocialVocabulary.PAGED : SocialVocabulary.COMPLETE)
                        : SocialVocabulary.SAMPLED;
                if (eligible && denominators.isEmpty()) {
                    anyUnscaled = true;
                }
                series.add(seriesView(net, language, scopeType + ":" + scopeId, metric, type, e.getValue(),
                        denominators, complete, from, until, daily, eligible));
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scope", Map.of("type", scopeType, "id", scopeId));
        out.put("metric", metric);
        out.put("interval", interval);
        out.put("since", from.toString());
        out.put("until", until.toString());
        out.put("series", series);
        out.put("baseline", SocialBaseline.detector());
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", "SOCIAL_IS_REPORTING", "text",
                "Social volume measures what people are saying, not what happened."));
        if (anyUnscaled) {
            caveats.add(Map.of("code", "NO_DENOMINATOR", "text",
                    "A search platform has no complete-stream denominator; its baseline is not scaled for platform"
                            + " growth or outages."));
        }
        out.put("caveats", caveats);
        return out;
    }

    private boolean scopeKnown(String scopeType, String scopeId) {
        try {
            return switch (scopeType) {
                case SocialVocabulary.SCOPE_TOPIC -> {
                    AttentionTopicRepository repository = topics.getIfAvailable();
                    yield repository != null && repository.existsById(scopeId);
                }
                case SocialVocabulary.SCOPE_PLACE -> exists("SELECT count(*) FROM place WHERE id = :id", scopeId);
                case SocialVocabulary.SCOPE_ENTITY -> exists("SELECT count(*) FROM entity WHERE id = :id", scopeId);
                default -> false;
            };
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean exists(String sql, String id) {
        Integer n = jdbc.queryForObject(sql, new MapSqlParameterSource().addValue("id", id), Integer.class);
        return n != null && n > 0;
    }

    private Map<String, Object> seriesView(String platform, String language, String scope, String metric,
            String collectionType, TreeMap<Instant, Long> values, Map<Instant, Long> denominators,
            Map<Instant, Boolean> complete, Instant from, Instant until, boolean daily, boolean eligible) {
        List<Map<String, Object>> points = new ArrayList<>();
        Instant now = clock.instant();
        Instant end = until.isAfter(now) ? now : until;
        Map<Instant, Map<String, Object>> days = new TreeMap<>();
        Set<Instant> collected = null;
        if (eligible && denominators.isEmpty() && !values.isEmpty()) {
            collected = new TreeSet<>();
            for (Instant h = values.firstKey(); h.isBefore(end); h = h.plus(Duration.ofHours(1))) {
                collected.add(h);
            }
        }
        for (Instant h = from; h.isBefore(end); h = h.plus(Duration.ofHours(1))) {
            double observed = values.getOrDefault(h, 0L);
            SocialBaseline.Point p = eligible ? SocialBaseline.at(h, values, denominators, collected) : null;
            Boolean ok = complete.get(h);
            boolean isComplete = complete.isEmpty() ? eligible && SocialVocabulary.PAGED.equals(collectionType)
                    : ok != null && ok;
            if (!daily) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("t", h.toString());
                point.put("observed", observed);
                point.put("baseline", p == null ? null : p.baseline());
                point.put("mad", p == null ? null : p.mad());
                point.put("velocityRatio", p == null ? null : p.velocityRatio());
                point.put("zRobust", p == null ? null : p.zRobust());
                point.put("complete", isComplete);
                points.add(point);
            } else {
                Instant day = h.truncatedTo(ChronoUnit.DAYS);
                Map<String, Object> d = days.computeIfAbsent(day, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("t", k.toString());
                    m.put("observed", 0.0);
                    m.put("baseline", eligible ? 0.0 : null);
                    m.put("complete", true);
                    return m;
                });
                d.put("observed", (Double) d.get("observed") + observed);
                if (d.get("baseline") != null) {
                    d.put("baseline", p == null || p.baseline() == null ? null : (Double) d.get("baseline")
                            + p.baseline());
                }
                d.put("complete", Boolean.TRUE.equals(d.get("complete")) && isComplete);
            }
        }
        if (daily) {
            for (Map<String, Object> d : days.values()) {
                Double baseline = (Double) d.get("baseline");
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("t", d.get("t"));
                point.put("observed", d.get("observed"));
                point.put("baseline", baseline);
                point.put("mad", null);
                point.put("velocityRatio", baseline == null ? null : (Double) d.get("observed")
                        / Math.max(baseline, 1.0));
                point.put("zRobust", null);
                point.put("complete", d.get("complete"));
                points.add(point);
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("platform", platform);
        out.put("language", language);
        out.put("seriesRef", "social:" + scope + "/" + platform + "/" + metric + (language == null ? "" : "/"
                + language));
        out.put("collectionType", collectionType);
        out.put("baselineEligible", eligible);
        out.put("points", points);
        return out;
    }

    // ------------------------------------------------------------------------- coverage

    /**
     * Collection coverage ({@code getSocialCoverage}).
     *
     * @param platform a network, or null for every network
     * @param regionCode a 040 region, or null for every region with coverage
     * @param since the window start
     * @param until the window end
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     */
    public Page coverage(String platform, String regionCode, Instant since, Instant until, String cursor, int limit) {
        if (!until.isAfter(since)) {
            throw ValidationException.forField("until", "must be after since");
        }
        if (Duration.between(since, until).compareTo(Duration.ofDays(31)) > 0) {
            throw ValidationException.forField("since", "the window is longer than 31 days");
        }
        if (platform != null && !SocialVocabulary.NETWORKS.contains(platform) && !store.hasDenominators(platform)
                && store.coverageRegions(platform, since, until).isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_PLATFORM, "No network '" + platform + "' has social data.");
        }
        Instant from = since.truncatedTo(ChronoUnit.HOURS);
        List<String[]> pairs = new ArrayList<>();
        Set<String> networks = new LinkedHashSet<>();
        if (platform != null) {
            networks.add(platform);
        } else {
            for (SocialPlatformCatalog.Definition d : SocialObservability.streamPlatforms()) {
                networks.add(d.network());
            }
            for (SocialVolumeStore.CoverageRow r : store.coverage(null, regionCode, from, until)) {
                networks.add(r.platform());
            }
        }
        List<String> regions = regionCode != null ? List.of(regionCode) : store.coverageRegions(platform, from,
                until);
        for (String region : regions) {
            for (String net : networks) {
                pairs.add(new String[] {region, net});
            }
        }
        int offset = decodeOffset(cursor);
        List<Map<String, Object>> items = new ArrayList<>();
        AbsenceCheck absence = null;
        for (int i = offset; i < Math.min(pairs.size(), offset + limit); i++) {
            String region = pairs.get(i)[0];
            String net = pairs.get(i)[1];
            if (absence == null || !region.equals(pairs.get(Math.max(offset, i - 1))[0])) {
                absence = observability.canSeeAbsence(region, from, until);
            }
            items.add(coverageItem(net, region, from, until, absence));
        }
        String next = pairs.size() > offset + limit ? encode(Integer.toString(offset + limit)) : null;
        return new Page(items, next);
    }

    private Map<String, Object> coverageItem(String platform, String region, Instant from, Instant until,
            AbsenceCheck absence) {
        Map<Instant, SocialVolumeStore.CoverageRow> rows = new TreeMap<>();
        for (SocialVolumeStore.CoverageRow r : store.coverage(platform, region, from, until)) {
            rows.put(r.hour(), r);
        }
        Map<Instant, Long> denominators = store.denominators(platform, null, from, until);
        Map<Instant, Boolean> complete = store.completeHours(platform, from, until);
        List<Map<String, Object>> buckets = new ArrayList<>();
        Instant now = clock.instant();
        int hours = 0;
        int completeHours = 0;
        for (Instant h = from; h.isBefore(until) && h.isBefore(now); h = h.plus(Duration.ofHours(1))) {
            SocialVolumeStore.CoverageRow r = rows.get(h);
            boolean ok = Boolean.TRUE.equals(complete.get(h));
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("hour", h.toString());
            b.put("postsSeen", r == null ? 0 : r.postsSeen());
            b.put("denominator", denominators.get(h));
            b.put("complete", ok);
            buckets.add(b);
            hours++;
            if (ok) {
                completeHours++;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("platform", platform);
        out.put("regionCode", region);
        out.put("since", from.toString());
        out.put("until", until.toString());
        out.put("buckets", buckets);
        out.put("completeShare", hours == 0 ? 0.0 : completeHours / (double) hours);
        out.put("baselinePostsPerHour", observability.baseline(platform, region, from));
        out.put("absenceOk", absence != null && absence.ok());
        out.put("reason", absence == null ? SocialVocabulary.LOW_SOCIAL_COVERAGE : absence.reason());
        return out;
    }

    // ------------------------------------------------------------------------- cursors

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String cursor) {
        try {
            return new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw invalidCursor();
        }
    }

    private static long[] decodePostCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        String[] parts = decode(cursor).split("\\|");
        if (parts.length != 2) {
            throw invalidCursor();
        }
        try {
            return new long[] {Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        } catch (NumberFormatException e) {
            throw invalidCursor();
        }
    }

    private static int decodeOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            int offset = Integer.parseInt(decode(cursor));
            if (offset < 0) {
                throw invalidCursor();
            }
            return offset;
        } catch (NumberFormatException e) {
            throw invalidCursor();
        }
    }

    private static ValidationException invalidCursor() {
        return new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not one this endpoint issued.",
                List.of(new FieldError("cursor", "is not a cursor from this endpoint")));
    }
}
