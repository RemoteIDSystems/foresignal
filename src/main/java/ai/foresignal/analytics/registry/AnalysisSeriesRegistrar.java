package ai.foresignal.analytics.registry;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.stats.RegionRollupComputer;
import ai.foresignal.stats.RegionRollupResolution;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Nightly auto-registration of analysis series (prompt 119, Intelligence-Design §5.6; 02:00 UTC from the
 * {@code AnalyticsScheduler}).
 *
 * <p>Candidates: cross-source region-day rollups ({@code rollup:day/&#42;/{kind}/{region}}, canonical events counted once)
 * with at least {@value #MIN_NONZERO_DAYS} non-zero days in the last 90; every daily, weekly or hourly
 * {@code series-point} series of 093 and 117; the PortWatch chokepoints of 088; and the 100 anomaly rule groups with
 * at least {@value #MIN_RULE_WEEKS} weeks of samples. Series named by the default watch pairs
 * ({@code intel/watch-pairs.json}) come first, then the highest volume; the automatic rows are capped at
 * {@code analysis.series.max} (2,000) minus the operator rows. Operator rows are never touched; automatic rows that
 * fall out are removed, their findings kept.
 */
@Component
public class AnalysisSeriesRegistrar {

    private static final Logger log = LoggerFactory.getLogger(AnalysisSeriesRegistrar.class);

    /** The bundled default watch pairs. */
    public static final String WATCH_PAIRS_RESOURCE = "intel/watch-pairs.json";

    /** The default cap. */
    public static final int DEFAULT_MAX = 2_000;

    /** Non-zero days a region-day rollup needs in the last 90. */
    public static final int MIN_NONZERO_DAYS = 30;

    /** Weeks of samples an anomaly rule group needs. */
    public static final int MIN_RULE_WEEKS = 8;

    /** The {@code forecast} detector value (prompt 126). */
    public static final String FORECAST = "forecast";

    /** Days of history a region-day rollup needs for the {@code forecast} detector by default (prompt 126). */
    public static final int FORECAST_MIN_DAYS = 365;

    /** Sources whose series get the {@code forecast} detector by default (117 markets and attention, PortWatch). */
    public static final Set<String> FORECAST_SOURCES = Set.of("cftc-cot", "fred-series", "wikimedia-pageviews",
            "gdelt-timelines", SeriesReader.PORTWATCH);

    /** The look-back of the volume and threshold checks. */
    static final Duration LOOKBACK = Duration.ofDays(90);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * A candidate row.
     *
     * @param ref the reference
     * @param frequency the registry frequency
     * @param family the signal family, or null
     * @param regionCode the region, or null
     * @param volume the ranking volume
     */
    public record Candidate(String ref, String frequency, String family, String regionCode, double volume) {}

    /**
     * What one run did.
     *
     * @param candidates candidates found
     * @param kept automatic rows kept or created
     * @param removed automatic rows removed
     * @param skippedDefaults default watch-pair series that do not exist (skipped with a warning)
     */
    public record Result(int candidates, int kept, int removed, List<String> skippedDefaults) {}

    private final NamedParameterJdbcTemplate jdbc;
    private final AnalysisSeriesRepository rows;
    private final SeriesReader reader;
    private final SignalFamilyRegistry families;
    private final AppSettingService settings;
    private final ObjectProvider<AttentionTopicRepository> topics;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the registrar. */
    public AnalysisSeriesRegistrar(NamedParameterJdbcTemplate jdbc, AnalysisSeriesRepository rows, SeriesReader reader,
            SignalFamilyRegistry families, AppSettingService settings,
            ObjectProvider<AttentionTopicRepository> topics, JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.rows = rows;
        this.reader = reader;
        this.families = families;
        this.settings = settings;
        this.topics = topics;
        this.json = json;
        this.clock = clock;
    }

    /** The cap in force ({@code analysis.series.max}). */
    public int max() {
        try {
            return Math.max(0, settings.get(SettingKeys.ANALYSIS_SERIES_MAX, Integer.class).orElse(DEFAULT_MAX));
        } catch (RuntimeException e) {
            return DEFAULT_MAX;
        }
    }

    /** Runs the auto-registration at {@code now}. */
    @Transactional
    public Result run(Instant now) {
        List<Candidate> candidates = new ArrayList<>();
        candidates.addAll(regionDayRollups(now));
        candidates.addAll(seriesPoints(now));
        candidates.addAll(portWatch(now));
        candidates.addAll(ruleGroups(now));
        List<String> skipped = new ArrayList<>();
        Map<String, List<Map<String, Object>>> defaults = defaultWatchPairs(candidates, skipped);
        Map<String, AnalysisSeriesEntity> existing = new HashMap<>();
        for (AnalysisSeriesEntity row : rows.findAll()) {
            existing.put(row.getRef(), row);
        }
        int room = (int) Math.max(0, max() - rows.countByOrigin("operator"));
        candidates.sort(Comparator.comparing((Candidate c) -> !defaults.containsKey(c.ref()))
                .thenComparing(Comparator.comparingDouble(Candidate::volume).reversed())
                .thenComparing(Candidate::ref));
        Set<String> kept = new HashSet<>();
        Set<String> forecastDefaults = forecastDefaults(candidates, now);
        for (Candidate c : candidates) {
            if (kept.size() >= room) {
                break;
            }
            AnalysisSeriesEntity row = existing.get(c.ref());
            if (row != null && "operator".equals(row.getOrigin())) {
                continue;
            }
            if (!kept.add(c.ref())) {
                continue;
            }
            if (row == null) {
                row = new AnalysisSeriesEntity(c.ref(), "auto", now);
                row.setDetectors(AnalysisSeriesService.DEFAULT_DETECTORS);
                row.setEnabled(true);
            }
            if (forecastDefaults.contains(c.ref()) && !row.getDetectors().contains(FORECAST)) {
                List<String> detectors = new ArrayList<>(row.getDetectors());
                detectors.add(FORECAST);
                row.setDetectors(detectors);
            }
            row.setFrequency(c.frequency());
            row.setFamily(c.family());
            row.setRegionCode(c.regionCode());
            row.setVolume(c.volume());
            row.setWatchPairs(defaults.getOrDefault(c.ref(), List.of()));
            row.setUpdatedAt(now);
            rows.save(row);
        }
        int removed = 0;
        for (AnalysisSeriesEntity row : existing.values()) {
            if ("auto".equals(row.getOrigin()) && !kept.contains(row.getRef())) {
                rows.delete(row);
                removed++;
            }
        }
        if (!skipped.isEmpty()) {
            log.warn("Default watch-pair series not found and skipped: {}", skipped);
        }
        return new Result(candidates.size(), kept.size(), removed, List.copyOf(skipped));
    }

    /**
     * The candidates whose automatic rows get the {@code forecast} detector by default (prompt 126): region-day
     * rollups with at least {@value #FORECAST_MIN_DAYS} days of history, the 117 market and attention series
     * ({@code cftc-cot}, {@code fred-series}, {@code wikimedia-pageviews}, {@code gdelt-timelines}, and series of a
     * market family), and the PortWatch chokepoints whose forecasts are ledger questions by default.
     */
    Set<String> forecastDefaults(List<Candidate> candidates, Instant now) {
        Set<String> out = new HashSet<>();
        Set<String> longRollups = new HashSet<>();
        jdbc.query("SELECT kind, region_code FROM activity_rollup WHERE resolution = :resolution AND source_id = :all"
                        + " AND region_code IS NOT NULL GROUP BY kind, region_code HAVING min(bucket_start) <= :since",
                new MapSqlParameterSource().addValue("resolution", RegionRollupResolution.DAY.code())
                        .addValue("all", RegionRollupComputer.ALL_SOURCES)
                        .addValue("since", Timestamp.from(now.minus(Duration.ofDays(FORECAST_MIN_DAYS)))),
                rs -> {
                    longRollups.add(SeriesRef.rollup("day", SeriesRef.ALL_SOURCES, rs.getString("kind"),
                            rs.getString("region_code")).format());
                });
        for (Candidate c : candidates) {
            if (longRollups.contains(c.ref())) {
                out.add(c.ref());
                continue;
            }
            if (!c.ref().startsWith("series:")) {
                continue;
            }
            try {
                SeriesRef ref = SeriesRef.parse(c.ref());
                if (FORECAST_SOURCES.contains(ref.sourceId())) {
                    out.add(c.ref());
                    continue;
                }
                SeriesReader.Meta meta = reader.meta(ref);
                if (ai.foresignal.analytics.AnalyticsSupport.MARKET_FAMILIES.contains(meta.seriesFamily())
                        || "markets".equals(meta.signalFamily()) || "attention".equals(meta.signalFamily())) {
                    out.add(c.ref());
                }
            } catch (RuntimeException e) {
                log.debug("No forecast default for {}: {}", c.ref(), e.toString());
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- candidates

    List<Candidate> regionDayRollups(Instant now) {
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT kind, region_code, count(*) FILTER (WHERE entities > 0) AS nonzero, sum(entities) AS total"
                        + " FROM activity_rollup WHERE resolution = :resolution AND source_id = :all AND bucket_start"
                        + " >= :since GROUP BY kind, region_code HAVING count(*) FILTER (WHERE entities > 0) >= :min",
                new MapSqlParameterSource().addValue("resolution", RegionRollupResolution.DAY.code())
                        .addValue("all", RegionRollupComputer.ALL_SOURCES)
                        .addValue("since", Timestamp.from(now.minus(LOOKBACK)))
                        .addValue("min", MIN_NONZERO_DAYS),
                rs -> {
                    String kind = rs.getString("kind");
                    String region = rs.getString("region_code");
                    if (region == null) {
                        return;
                    }
                    SeriesRef ref = SeriesRef.rollup("day", SeriesRef.ALL_SOURCES, kind, region);
                    out.add(new Candidate(ref.format(), "day", families.familyOfRecord(null, kind).orElse(null),
                            region, rs.getDouble("total")));
                });
        return out;
    }

    List<Candidate> seriesPoints(Instant now) {
        Map<String, Double> volume = new HashMap<>();
        jdbc.query("SELECT source_id, payload->>'series' AS series, count(*) AS n FROM record_head WHERE kind ="
                        + " 'series-point' AND tombstone = false AND event_time >= :since GROUP BY source_id,"
                        + " payload->>'series'",
                new MapSqlParameterSource("since", Timestamp.from(now.minus(LOOKBACK))),
                rs -> {
                    volume.put(rs.getString("source_id") + "/" + rs.getString("series"), rs.getDouble("n"));
                });
        List<Candidate> out = new ArrayList<>();
        List<Map<String, Object>> latest = jdbc.queryForList("SELECT source_id, payload->>'series' AS series,"
                + " payload->>'frequency' AS frequency FROM record_head WHERE kind = 'series-latest' AND tombstone ="
                + " false ORDER BY source_id, external_id", new MapSqlParameterSource());
        for (Map<String, Object> row : latest) {
            String source = String.valueOf(row.get("source_id"));
            Object series = row.get("series");
            String frequency = registryFrequency(row.get("frequency") == null ? null : row.get("frequency").toString());
            if (series == null || frequency == null) {
                continue;
            }
            SeriesRef ref;
            try {
                ref = SeriesRef.series(source, series.toString());
                SeriesReader.Meta meta = reader.meta(ref);
                out.add(new Candidate(ref.format(), frequency, meta.signalFamily(), meta.regionCode(),
                        volume.getOrDefault(source + "/" + series, 0.0)));
            } catch (RuntimeException e) {
                log.debug("Skipping series {}/{}: {}", source, series, e.toString());
            }
        }
        return out;
    }

    List<Candidate> portWatch(Instant now) {
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT split_part(external_id, ':', 1) AS port, count(*) AS n FROM record_head WHERE source_id ="
                        + " :source AND kind = 'chokepoint-transit' AND tombstone = false GROUP BY 1",
                new MapSqlParameterSource("source", SeriesReader.PORTWATCH),
                rs -> {
                    SeriesRef ref = SeriesRef.series(SeriesReader.PORTWATCH, rs.getString("port"));
                    out.add(new Candidate(ref.format(), "day",
                            families.familyOfRecord(SeriesReader.PORTWATCH, "chokepoint-transit").orElse(null), null,
                            rs.getDouble("n")));
                });
        return out;
    }

    List<Candidate> ruleGroups(Instant now) {
        List<Candidate> out = new ArrayList<>();
        jdbc.query("SELECT s.rule_id, s.group_key, sum(s.count) AS total FROM activity_sample s JOIN trigger_rule r ON"
                        + " r.id = s.rule_id WHERE r.type = 'anomaly' GROUP BY s.rule_id, s.group_key HAVING"
                        + " max(s.bucket_start) - min(s.bucket_start) >= make_interval(weeks => :weeks)",
                new MapSqlParameterSource("weeks", MIN_RULE_WEEKS),
                rs -> {
                    SeriesRef ref = SeriesRef.rule(rs.getObject("rule_id", UUID.class), rs.getString("group_key"));
                    String family = null;
                    try {
                        family = reader.meta(ref).signalFamily();
                    } catch (RuntimeException e) {
                        // The rule disappeared meanwhile.
                    }
                    out.add(new Candidate(ref.format(), "day", family, ref.regionCode(), rs.getDouble("total")));
                });
        return out;
    }

    /** The registry frequency of a 093 period: {@code day}, {@code week}, {@code day} for hourly, else null. */
    static String registryFrequency(String iso) {
        if (iso == null) {
            return null;
        }
        return switch (iso) {
            case "P1D", "PT1H" -> "day";
            case "P1W" -> "week";
            default -> null;
        };
    }

    // ------------------------------------------------------------------------- default watch pairs

    /**
     * The default watch pairs per candidate ref, from {@code intel/watch-pairs.json}; named refs that are not
     * candidates are added to {@code skipped}.
     */
    Map<String, List<Map<String, Object>>> defaultWatchPairs(List<Candidate> candidates, List<String> skipped) {
        Map<String, Object> file = load();
        Set<String> known = new HashSet<>();
        for (Candidate c : candidates) {
            known.add(c.ref());
        }
        Map<String, String> topicOfSeries = topicOfSeries();
        Map<String, List<String>> topicRegions = topicRegions();
        Map<String, List<Map<String, Object>>> out = new LinkedHashMap<>();
        Object entries = file.get("entries");
        if (!(entries instanceof List<?> list)) {
            return out;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> entry)) {
                continue;
            }
            List<Map<String, Object>> pairs = pairs(entry.get("watchPairs"));
            if (pairs.isEmpty()) {
                continue;
            }
            Set<String> refs = new java.util.LinkedHashSet<>();
            for (String ref : strings(entry.get("refs"))) {
                if (known.contains(ref)) {
                    refs.add(ref);
                } else {
                    skipped.add(ref);
                }
            }
            List<String> topicIds = strings(entry.get("topics"));
            List<String> sources = strings(entry.get("sources"));
            for (String ref : known) {
                String topic = topicOfSeries.get(ref);
                if (topic != null && topicIds.contains(topic)) {
                    refs.add(ref);
                }
                for (String source : sources) {
                    if (ref.startsWith("series:" + source + "/")) {
                        refs.add(ref);
                    }
                }
            }
            for (String ref : refs) {
                out.computeIfAbsent(ref, k -> new ArrayList<>()).addAll(pairs);
            }
            if (Boolean.TRUE.equals(entry.get("gdeltVolumeByTopicRegion"))) {
                for (String ref : known) {
                    String topic = topicOfSeries.get(ref);
                    if (topic == null || !ref.startsWith("series:gdelt-timelines/") || !ref.endsWith(".vol")) {
                        continue;
                    }
                    List<String> regions = topicRegions.getOrDefault(topic, List.of());
                    if (regions.isEmpty()) {
                        continue;
                    }
                    for (Map<String, Object> pair : pairs) {
                        Map<String, Object> scoped = new LinkedHashMap<>(pair);
                        scoped.put("regionCodes", regions);
                        out.computeIfAbsent(ref, k -> new ArrayList<>()).add(scoped);
                    }
                }
            }
        }
        return out;
    }

    private Map<String, String> topicOfSeries() {
        Map<String, String> out = new HashMap<>();
        jdbc.query("SELECT source_id, payload->>'series' AS series, payload->>'topicId' AS topic FROM record_head WHERE"
                // jsonb_exists, not the ? operator, which the driver would read as a bind placeholder.
                + " kind = 'series-latest' AND tombstone = false AND jsonb_exists(payload, 'topicId')",
                new MapSqlParameterSource(),
                rs -> {
                    if (rs.getString("series") != null && rs.getString("topic") != null) {
                        out.put(SeriesRef.series(rs.getString("source_id"), rs.getString("series")).format(),
                                rs.getString("topic"));
                    }
                });
        return out;
    }

    private Map<String, List<String>> topicRegions() {
        Map<String, List<String>> out = new HashMap<>();
        AttentionTopicRepository repository = topics.getIfAvailable();
        if (repository != null) {
            for (AttentionTopicEntity topic : repository.findAll()) {
                out.put(topic.getId(), topic.getRegionCodes() == null ? List.of() : topic.getRegionCodes());
            }
        }
        return out;
    }

    private List<Map<String, Object>> pairs(Object value) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    WatchPair pair = WatchPair.fromMap((Map<String, Object>) m);
                    if (pair != null && families.canonical(pair.eventFamily()).isPresent()) {
                        out.add(pair.toMap());
                    }
                }
            }
        }
        return out;
    }

    private static List<String> strings(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof String s) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private Map<String, Object> load() {
        try (InputStream in = new ClassPathResource(WATCH_PAIRS_RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException | RuntimeException e) {
            log.warn("Cannot read {}: {}", WATCH_PAIRS_RESOURCE, e.toString());
            return Map.of();
        }
    }

    /** The clock (for the scheduler). */
    Instant now() {
        return clock.instant();
    }
}
