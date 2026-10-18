package ai.foresignal.social;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and writes the social aggregate tables (prompt 135): {@code social_volume_hourly},
 * {@code social_denominator_hourly} and {@code social_coverage}. Writes add the deltas of a
 * {@link VolumeAccumulator.Drain}; closing an hour stamps its {@code complete} flags.
 */
@Component
public class SocialVolumeStore {

    /**
     * One stored volume hour.
     *
     * @param platform the network
     * @param language the language bucket
     * @param collectionType the collection type
     * @param hour the hour start
     * @param posts posts
     * @param uniqueAuthors distinct authors
     * @param reposts reposts
     * @param engagement engagement
     */
    public record VolumeRow(String platform, String language, String collectionType, Instant hour, long posts,
            long uniqueAuthors, long reposts, long engagement) {

        /** The value of a metric. */
        public long metric(String metric) {
            return switch (metric) {
                case SocialVocabulary.UNIQUE_AUTHORS -> uniqueAuthors;
                case SocialVocabulary.REPOSTS -> reposts;
                case SocialVocabulary.ENGAGEMENT -> engagement;
                default -> posts;
            };
        }
    }

    /**
     * One stored coverage hour.
     *
     * @param platform the network
     * @param regionCode the region
     * @param hour the hour start
     * @param postsSeen posts attributed to the region
     * @param denominator the platform's denominator, or null
     * @param complete whether complete-stream collection was healthy, or null while the hour is open
     */
    public record CoverageRow(String platform, String regionCode, Instant hour, long postsSeen, Long denominator,
            Boolean complete) {}

    /**
     * A series that has volume.
     *
     * @param scopeType the scope type
     * @param scopeId the scope id
     * @param platform the network
     * @param collectionType the collection type
     * @param hours hours with rows
     * @param posts total posts
     */
    public record SeriesSummary(String scopeType, String scopeId, String platform, String collectionType, long hours,
            long posts) {}

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the store. */
    public SocialVolumeStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Adds a drain's deltas. */
    @Transactional
    public void apply(VolumeAccumulator.Drain drain, Instant now) {
        Timestamp at = Timestamp.from(now);
        for (VolumeAccumulator.VolumeDelta d : drain.volumes()) {
            VolumeAccumulator.VolumeKey k = d.key();
            int colon = k.scope().indexOf(':');
            String scopeType = colon > 0 ? k.scope().substring(0, colon) : k.scope();
            String scopeId = colon > 0 ? k.scope().substring(colon + 1) : "";
            jdbc.update("INSERT INTO social_volume_hourly (id, scope_type, scope_id, platform, language, hour,"
                    + " collection_type, posts, unique_authors, reposts, engagement, updated_at) VALUES (:id, :st, :si,"
                    + " :p, :l, :h, :ct, :posts, :ua, :rp, :en, :at) ON CONFLICT (id) DO UPDATE SET posts ="
                    + " social_volume_hourly.posts + EXCLUDED.posts, unique_authors = social_volume_hourly.unique_authors"
                    + " + EXCLUDED.unique_authors, reposts = social_volume_hourly.reposts + EXCLUDED.reposts,"
                    + " engagement = social_volume_hourly.engagement + EXCLUDED.engagement, updated_at ="
                    + " EXCLUDED.updated_at", new MapSqlParameterSource()
                    .addValue("id", k.scope() + "|" + k.platform() + "|" + k.language() + "|"
                            + k.hour().getEpochSecond() + "|" + k.collectionType())
                    .addValue("st", scopeType).addValue("si", scopeId).addValue("p", k.platform())
                    .addValue("l", k.language()).addValue("h", Timestamp.from(k.hour()))
                    .addValue("ct", k.collectionType()).addValue("posts", d.posts())
                    .addValue("ua", d.uniqueAuthors()).addValue("rp", d.reposts()).addValue("en", d.engagement())
                    .addValue("at", at));
        }
        drain.denominators().forEach((k, n) -> jdbc.update("INSERT INTO social_denominator_hourly (id, platform,"
                + " language, hour, posts, updated_at) VALUES (:id, :p, :l, :h, :n, :at) ON CONFLICT (id) DO UPDATE SET"
                + " posts = social_denominator_hourly.posts + EXCLUDED.posts, updated_at = EXCLUDED.updated_at",
                new MapSqlParameterSource().addValue("id", k.platform() + "|" + k.language() + "|"
                        + k.hour().getEpochSecond()).addValue("p", k.platform()).addValue("l", k.language())
                        .addValue("h", Timestamp.from(k.hour())).addValue("n", n).addValue("at", at)));
        drain.coverage().forEach((k, n) -> jdbc.update("INSERT INTO social_coverage (id, platform, region_code, hour,"
                + " posts_seen, updated_at) VALUES (:id, :p, :r, :h, :n, :at) ON CONFLICT (id) DO UPDATE SET posts_seen"
                + " = social_coverage.posts_seen + EXCLUDED.posts_seen, updated_at = EXCLUDED.updated_at",
                new MapSqlParameterSource().addValue("id", k.platform() + "|" + k.regionCode() + "|"
                        + k.hour().getEpochSecond()).addValue("p", k.platform()).addValue("r", k.regionCode())
                        .addValue("h", Timestamp.from(k.hour())).addValue("n", n).addValue("at", at)));
    }

    /**
     * Replaces one hour's post count of a scope with an absolute count the platform reported itself (X
     * {@code counts/recent}); distinct authors, reposts and engagement stay as they are.
     *
     * @param scope the scope key
     * @param platform the network
     * @param language the language bucket
     * @param hour the hour start
     * @param collectionType the collection type
     * @param posts the count
     * @param now the current time
     */
    @Transactional
    public void putCount(String scope, String platform, String language, Instant hour, String collectionType,
            long posts, Instant now) {
        int colon = scope.indexOf(':');
        jdbc.update("INSERT INTO social_volume_hourly (id, scope_type, scope_id, platform, language, hour,"
                + " collection_type, posts, unique_authors, reposts, engagement, updated_at) VALUES (:id, :st, :si, :p,"
                + " :l, :h, :ct, :posts, 0, 0, 0, :at) ON CONFLICT (id) DO UPDATE SET posts = EXCLUDED.posts,"
                + " updated_at = EXCLUDED.updated_at", new MapSqlParameterSource()
                .addValue("id", scope + "|" + platform + "|" + language + "|" + hour.getEpochSecond() + "|"
                        + collectionType)
                .addValue("st", colon > 0 ? scope.substring(0, colon) : scope)
                .addValue("si", colon > 0 ? scope.substring(colon + 1) : "").addValue("p", platform)
                .addValue("l", language).addValue("h", Timestamp.from(hour)).addValue("ct", collectionType)
                .addValue("posts", posts).addValue("at", Timestamp.from(now)));
    }

    /**
     * Closes an hour of one platform: stamps {@code complete} on its denominator and coverage rows and copies the
     * platform's total denominator into the coverage rows. A complete platform with no denominator row yet gets a
     * zero row, so a silent but healthy hour is recorded as such.
     *
     * @param platform the network
     * @param hour the hour start
     * @param complete whether complete-stream collection was healthy for the whole hour
     * @param now the current time
     */
    @Transactional
    public void closeHour(String platform, Instant hour, boolean complete, Instant now) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("p", platform)
                .addValue("h", Timestamp.from(hour)).addValue("c", complete).addValue("at", Timestamp.from(now));
        jdbc.update("UPDATE social_denominator_hourly SET complete = :c, updated_at = :at WHERE platform = :p AND hour"
                + " = :h", p);
        if (complete) {
            jdbc.update("INSERT INTO social_denominator_hourly (id, platform, language, hour, posts, complete,"
                    + " updated_at) VALUES (:id, :p, :l, :h, 0, true, :at) ON CONFLICT (id) DO NOTHING",
                    new MapSqlParameterSource(p.getValues()).addValue("id", platform + "|"
                            + SocialVocabulary.OTHER_LANGUAGE + "|" + hour.getEpochSecond())
                            .addValue("l", SocialVocabulary.OTHER_LANGUAGE));
        }
        jdbc.update("UPDATE social_coverage SET complete = :c, updated_at = :at, denominator = (SELECT sum(d.posts) FROM"
                + " social_denominator_hourly d WHERE d.platform = :p AND d.hour = :h) WHERE platform = :p AND hour ="
                + " :h", p);
    }

    /**
     * The volume rows of one scope.
     *
     * @param scopeType the scope type
     * @param scopeId the scope id
     * @param platform the network, or null for all
     * @param language the language bucket, or null for all
     * @param from the first hour (inclusive)
     * @param to the end (exclusive)
     */
    public List<VolumeRow> volume(String scopeType, String scopeId, String platform, String language, Instant from,
            Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("st", scopeType).addValue("si", scopeId)
                .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to));
        StringBuilder sql = new StringBuilder("SELECT platform, language, collection_type, hour, posts,"
                + " unique_authors, reposts, engagement FROM social_volume_hourly WHERE scope_type = :st AND scope_id ="
                + " :si AND hour >= :from AND hour < :to");
        if (platform != null) {
            sql.append(" AND platform = :p");
            p.addValue("p", platform);
        }
        if (language != null) {
            sql.append(" AND language = :l");
            p.addValue("l", language);
        }
        sql.append(" ORDER BY platform, language, collection_type, hour");
        return jdbc.query(sql.toString(), p, (rs, n) -> new VolumeRow(rs.getString("platform"),
                rs.getString("language"), rs.getString("collection_type"), rs.getTimestamp("hour").toInstant(),
                rs.getLong("posts"), rs.getLong("unique_authors"), rs.getLong("reposts"), rs.getLong("engagement")));
    }

    /**
     * The hourly denominators of a platform.
     *
     * @param platform the network
     * @param language the language bucket, or null to sum every language
     * @param from the first hour (inclusive)
     * @param to the end (exclusive)
     * @return hour to posts, in time order
     */
    public Map<Instant, Long> denominators(String platform, String language, Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("p", platform)
                .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to));
        String sql = "SELECT hour, sum(posts) AS posts FROM social_denominator_hourly WHERE platform = :p AND hour >="
                + " :from AND hour < :to";
        if (language != null) {
            sql += " AND language = :l";
            p.addValue("l", language);
        }
        sql += " GROUP BY hour ORDER BY hour";
        Map<Instant, Long> out = new TreeMap<>();
        jdbc.query(sql, p, rs -> {
            out.put(rs.getTimestamp("hour").toInstant(), rs.getLong("posts"));
        });
        return out;
    }

    /**
     * The closed hours of a platform and whether each was complete.
     *
     * @return hour to complete, in time order (open hours left out)
     */
    public Map<Instant, Boolean> completeHours(String platform, Instant from, Instant to) {
        Map<Instant, Boolean> out = new TreeMap<>();
        jdbc.query("SELECT hour, bool_and(complete) AS complete FROM social_denominator_hourly WHERE platform = :p AND"
                + " hour >= :from AND hour < :to AND complete IS NOT NULL GROUP BY hour ORDER BY hour",
                new MapSqlParameterSource().addValue("p", platform).addValue("from", Timestamp.from(from))
                        .addValue("to", Timestamp.from(to)), rs -> {
                    out.put(rs.getTimestamp("hour").toInstant(), rs.getBoolean("complete"));
                });
        return out;
    }

    /**
     * Coverage rows.
     *
     * @param platform the network, or null for all
     * @param regionCode the region, or null for all
     * @param from the first hour (inclusive)
     * @param to the end (exclusive)
     */
    public List<CoverageRow> coverage(String platform, String regionCode, Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        StringBuilder sql = new StringBuilder("SELECT platform, region_code, hour, posts_seen, denominator, complete"
                + " FROM social_coverage WHERE hour >= :from AND hour < :to");
        if (platform != null) {
            sql.append(" AND platform = :p");
            p.addValue("p", platform);
        }
        if (regionCode != null) {
            sql.append(" AND region_code = :r");
            p.addValue("r", regionCode);
        }
        sql.append(" ORDER BY region_code, platform, hour");
        return jdbc.query(sql.toString(), p, (rs, n) -> new CoverageRow(rs.getString("platform"),
                rs.getString("region_code"), rs.getTimestamp("hour").toInstant(), rs.getLong("posts_seen"),
                (Long) rs.getObject("denominator", Long.class), (Boolean) rs.getObject("complete", Boolean.class)));
    }

    /**
     * The mean posts per hour attributed to a region on a platform over complete hours of a window (missing complete
     * hours count as zero).
     *
     * @return the mean, or null when the platform had no complete hour in the window
     */
    public Double regionMeanPerHour(String platform, String regionCode, Instant from, Instant to) {
        Map<Instant, Boolean> hours = completeHours(platform, from, to);
        long complete = hours.values().stream().filter(Boolean::booleanValue).count();
        if (complete == 0) {
            return null;
        }
        Long total = jdbc.queryForObject("SELECT coalesce(sum(c.posts_seen), 0) FROM social_coverage c WHERE"
                + " c.platform = :p AND c.region_code = :r AND c.hour >= :from AND c.hour < :to AND EXISTS (SELECT 1"
                + " FROM social_denominator_hourly d WHERE d.platform = c.platform AND d.hour = c.hour AND d.complete)",
                new MapSqlParameterSource().addValue("p", platform).addValue("r", regionCode)
                        .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to)), Long.class);
        return (total == null ? 0 : total) / (double) complete;
    }

    /** The regions with coverage rows in a window, sorted. */
    public List<String> coverageRegions(String platform, Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        String sql = "SELECT DISTINCT region_code FROM social_coverage WHERE hour >= :from AND hour < :to";
        if (platform != null) {
            sql += " AND platform = :p";
            p.addValue("p", platform);
        }
        return jdbc.queryForList(sql + " ORDER BY region_code", p, String.class);
    }

    /** The platforms with rows (volume, denominators or coverage) for a scope, sorted. */
    public List<String> platformsOf(String scopeType, String scopeId) {
        return jdbc.queryForList("SELECT DISTINCT platform FROM social_volume_hourly WHERE scope_type = :st AND scope_id"
                + " = :si ORDER BY platform", new MapSqlParameterSource().addValue("st", scopeType)
                .addValue("si", scopeId), String.class);
    }

    /** The platforms with denominators, sorted. */
    public List<String> denominatorPlatforms() {
        return jdbc.queryForList("SELECT DISTINCT platform FROM social_denominator_hourly ORDER BY platform",
                new MapSqlParameterSource(), String.class);
    }

    /**
     * The volume rows of one hour (every scope), for the velocity detector.
     *
     * @return scope key to its rows
     */
    public Map<String, List<VolumeRow>> hour(Instant hour) {
        Map<String, List<VolumeRow>> out = new LinkedHashMap<>();
        jdbc.query("SELECT scope_type, scope_id, platform, language, collection_type, hour, posts, unique_authors,"
                + " reposts, engagement FROM social_volume_hourly WHERE hour = :h ORDER BY scope_type, scope_id,"
                + " platform, language", new MapSqlParameterSource().addValue("h", Timestamp.from(hour)), rs -> {
                    out.computeIfAbsent(rs.getString("scope_type") + ":" + rs.getString("scope_id"),
                            k -> new ArrayList<>()).add(new VolumeRow(rs.getString("platform"),
                            rs.getString("language"), rs.getString("collection_type"),
                            rs.getTimestamp("hour").toInstant(), rs.getLong("posts"), rs.getLong("unique_authors"),
                            rs.getLong("reposts"), rs.getLong("engagement")));
                });
        return out;
    }

    /**
     * The scope-platform series with volume since a time, for the 119 registrar.
     *
     * @param since the start
     * @param minHours the hours with rows a series needs
     */
    public List<SeriesSummary> series(Instant since, int minHours) {
        return jdbc.query("SELECT scope_type, scope_id, platform, min(collection_type) AS collection_type,"
                + " count(DISTINCT hour) AS hours, sum(posts) AS posts FROM social_volume_hourly WHERE hour >= :since"
                + " AND collection_type <> :sampled GROUP BY scope_type, scope_id, platform HAVING count(DISTINCT hour)"
                + " >= :min", new MapSqlParameterSource().addValue("since", Timestamp.from(since))
                .addValue("sampled", SocialVocabulary.SAMPLED).addValue("min", minHours),
                (rs, n) -> new SeriesSummary(rs.getString("scope_type"), rs.getString("scope_id"),
                        rs.getString("platform"), rs.getString("collection_type"), rs.getLong("hours"),
                        rs.getLong("posts")));
    }

    /** True when a scope has any volume row on a platform (or on any platform when null). */
    public boolean exists(String scopeType, String scopeId, String platform) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("st", scopeType).addValue("si", scopeId);
        String sql = "SELECT count(*) FROM (SELECT 1 FROM social_volume_hourly WHERE scope_type = :st AND scope_id ="
                + " :si";
        if (platform != null) {
            sql += " AND platform = :p";
            p.addValue("p", platform);
        }
        Integer n = jdbc.queryForObject(sql + " LIMIT 1) x", p, Integer.class);
        return n != null && n > 0;
    }

    /** True when a platform has a denominator row with posts (a whole-platform stream counts it). */
    public boolean hasDenominators(String platform) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM (SELECT 1 FROM social_denominator_hourly WHERE platform ="
                + " :p AND posts > 0 LIMIT 1) x", new MapSqlParameterSource().addValue("p", platform), Integer.class);
        return n != null && n > 0;
    }
}
