package ai.foresignal.baserate.analog;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.search.SearchIndexState;
import ai.foresignal.search.SearchIndexStateStore;
import ai.foresignal.search.VectorSlot;
import ai.foresignal.situation.SituationRecordPublisher;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Situation signatures and outcomes (prompt 121).
 *
 * <ul>
 *   <li><strong>Signatures</strong> ({@link SignatureBuilder}) are computed when a situation becomes active and on
 *       each day of its first {@value #SIGNATURE_DAYS} days ({@link #computeDue}, every few minutes), from its members
 *       (per 119 signal family, over the first N = min(7, day) days), the families' global weekly standard deviation
 *       (weekly member counts over the last year), its theme, the 040 continent and 117 income band of its first
 *       country, its 098 entity classes and its snapshot severities. Situations without any signature (created before
 *       this prompt, or never activated) are backfilled at their last day, {@value #BACKFILL} per run.
 *   <li><strong>Outcomes</strong> are computed for closed or dormant situations and any older than
 *       {@value #SIGNATURE_DAYS} days: duration, peak severity, and the families active in their regions within 7 and
 *       30 days of the reference time (day 7, or the end when shorter), read from the 119 region-day rollups and
 *       {@code reference_history}. An outcome is final once the situation is closed and its 30-day window has passed.
 *       {@code hypothesisOutcome} is the newest resolved hypothesis set linked to the situation (prompt 125:
 *       {@code {setId, outcome}}), or null.
 * </ul>
 */
@Service
public class SignatureService {

    /** The days a situation gets a daily signature. */
    public static final int SIGNATURE_DAYS = 30;

    /** Situations backfilled per run. */
    public static final int BACKFILL = 200;

    /** Outcomes computed per run. */
    public static final int OUTCOMES_PER_RUN = 200;

    private static final Logger log = LoggerFactory.getLogger(SignatureService.class);

    /**
     * A situation's facts used for signatures, outcomes and analogs.
     *
     * @param id the id
     * @param title the title
     * @param theme the theme
     * @param status the status
     * @param severity the current severity
     * @param regionCodes the 040 regions
     * @param startedAt when it started
     * @param lastActivityAt its last activity
     * @param closedAt when it closed, or null
     * @param mergedInto the situation it was merged into, or null
     * @param privateSituation true when private
     * @param entityIds its 098 entities
     */
    public record SituationFacts(String id, String title, String theme, String status, String severity,
            List<String> regionCodes, Instant startedAt, Instant lastActivityAt, Instant closedAt, String mergedInto,
            boolean privateSituation, List<String> entityIds) {

        /** The end used for overlap: closed, else last activity for dormant or closed, else null (open). */
        public Instant end() {
            if (closedAt != null) {
                return closedAt;
            }
            return "dormant".equals(status) ? lastActivityAt : null;
        }
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SituationSignatureRepository signatures;
    private final SituationOutcomeRepository outcomes;
    private final SignalFamilyRegistry families;
    private final RegionClasses regionClasses;
    private final ObjectProvider<SearchIndexStateStore> searchIndex;
    private final Clock clock;
    private volatile Map<String, Double> familySd;
    private volatile Instant familySdAt;

    /** Creates the service. */
    public SignatureService(NamedParameterJdbcTemplate jdbc, SituationSignatureRepository signatures,
            SituationOutcomeRepository outcomes, SignalFamilyRegistry families, RegionClasses regionClasses,
            ObjectProvider<SearchIndexStateStore> searchIndex, Clock clock) {
        this.jdbc = jdbc;
        this.signatures = signatures;
        this.outcomes = outcomes;
        this.families = families;
        this.regionClasses = regionClasses;
        this.searchIndex = searchIndex;
        this.clock = clock;
    }

    // ------------------------------------------------------------------------- facts

    /** A situation's facts, if it exists. */
    public Optional<SituationFacts> facts(String id) {
        List<SituationFacts> out = jdbc.query("SELECT id, title, theme, status, severity, array_to_string(region_codes,"
                + " ',') AS regions, started_at, last_activity_at, closed_at, merged_into, private,"
                + " array_to_string(entity_ids, ',') AS entities FROM situation WHERE id = :id",
                new MapSqlParameterSource("id", id), (rs, n) -> new SituationFacts(rs.getString("id"),
                        rs.getString("title"), rs.getString("theme"), rs.getString("status"),
                        rs.getString("severity"), csv(rs.getString("regions")), ts(rs.getTimestamp("started_at")),
                        ts(rs.getTimestamp("last_activity_at")), ts(rs.getTimestamp("closed_at")),
                        rs.getString("merged_into"), rs.getBoolean("private"), csv(rs.getString("entities"))));
        return out.stream().findFirst();
    }

    /** The day of a situation's life at {@code now}: 1 to {@value #SIGNATURE_DAYS}. */
    public static int dayN(Instant startedAt, Instant now) {
        long hours = Math.max(0, Duration.between(startedAt, now).toHours());
        return (int) Math.max(1, Math.min(SIGNATURE_DAYS, Math.ceil(hours / 24.0)));
    }

    // ------------------------------------------------------------------------- signatures

    /**
     * Computes and stores a situation's signature for one day.
     *
     * @param id the situation
     * @param dayN the day of its life
     * @return the stored signature, or empty when the situation does not exist
     */
    public Optional<SituationSignatureEntity> compute(String id, int dayN) {
        Optional<SituationFacts> facts = facts(id);
        if (facts.isEmpty()) {
            return Optional.empty();
        }
        SituationFacts s = facts.get();
        SignatureBuilder.Signature signature = signature(s, dayN);
        SituationSignatureEntity entity = new SituationSignatureEntity(id, dayN, signature, embeddingRef(id),
                clock.instant());
        return Optional.of(signatures.save(entity));
    }

    /** A situation's signature on a day, computed (not stored). */
    public SignatureBuilder.Signature signature(SituationFacts s, int dayN) {
        int n = Math.min(SignatureBuilder.MAX_DAYS, Math.max(1, dayN));
        Instant from = s.startedAt();
        Instant to = from.plus(Duration.ofDays(n));
        Instant now = clock.instant();
        if (to.isAfter(now)) {
            to = now;
        }
        Map<String, Long> familyCounts = new TreeMap<>();
        long[] daily = new long[n];
        Instant windowStart = from;
        jdbc.query("SELECT signal_family, coalesce(observed_at, added_at) AS at FROM situation_member WHERE"
                + " situation_id = :id AND coalesce(observed_at, added_at) >= :from AND coalesce(observed_at,"
                + " added_at) < :to", new MapSqlParameterSource().addValue("id", s.id())
                        .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to)), rs -> {
                    String family = rs.getString("signal_family");
                    String canonical = family == null ? "unknown" : families.canonical(family).orElse(family);
                    familyCounts.merge(canonical, 1L, Long::sum);
                    Instant at = rs.getTimestamp("at").toInstant();
                    int day = (int) Math.min(n - 1, Math.max(0, Duration.between(windowStart, at).toDays()));
                    daily[day]++;
                });
        String country = s.regionCodes().stream().filter(c -> c.length() >= 2 && !"ZZ".equals(c))
                .map(c -> c.substring(0, 2)).findFirst().orElse(null);
        RegionClasses.Country covariates = country == null ? null : regionClasses.country(country);
        Map<String, Long> entities = new TreeMap<>();
        if (!s.entityIds().isEmpty()) {
            jdbc.query("SELECT type, count(*) AS n FROM entity WHERE id IN (:ids) GROUP BY type",
                    new MapSqlParameterSource("ids", s.entityIds()),
                    rs -> {
                        entities.put(rs.getString("type"), rs.getLong("n"));
                    });
        }
        List<String> severities = new ArrayList<>(jdbc.queryForList("SELECT severity FROM situation_snapshot WHERE"
                + " situation_id = :id AND day < :until ORDER BY day", new MapSqlParameterSource().addValue("id", s.id())
                        .addValue("until", Date.valueOf(LocalDate.ofInstant(from, ZoneOffset.UTC).plusDays(dayN))),
                String.class));
        if (severities.isEmpty() && s.severity() != null) {
            severities.add(s.severity());
        }
        return SignatureBuilder.build(new SignatureBuilder.Inputs(familyCounts, familySd(), daily, s.theme(),
                covariates == null ? null : covariates.continent(), covariates == null ? null
                        : covariates.incomeBand(), entities, severities));
    }

    /** Each family's standard deviation of weekly member counts across all situations over the last year. */
    public Map<String, Double> familySd() {
        Map<String, Double> current = familySd;
        Instant now = clock.instant();
        if (current != null && familySdAt != null && familySdAt.plus(Duration.ofDays(1)).isAfter(now)) {
            return current;
        }
        Map<String, Map<LocalDate, Long>> weekly = new HashMap<>();
        jdbc.query("SELECT signal_family, CAST(date_trunc('week', coalesce(observed_at, added_at) AT TIME ZONE 'UTC')"
                + " AS date) AS week, count(*) AS n FROM situation_member WHERE coalesce(observed_at, added_at) >= :from"
                + " GROUP BY 1, 2", new MapSqlParameterSource("from", Timestamp.from(now.minus(Duration.ofDays(364)))),
                rs -> {
                    String family = rs.getString("signal_family");
                    String canonical = family == null ? "unknown" : families.canonical(family).orElse(family);
                    weekly.computeIfAbsent(canonical, k -> new HashMap<>()).merge(rs.getDate("week").toLocalDate(),
                            rs.getLong("n"), Long::sum);
                });
        Map<String, Double> out = new TreeMap<>();
        weekly.forEach((family, weeks) -> {
            double[] values = new double[52];
            int i = 0;
            for (Long n : weeks.values()) {
                if (i < values.length) {
                    values[i++] = n;
                }
            }
            out.put(family, sd(values));
        });
        familySd = out;
        familySdAt = now;
        return out;
    }

    /** Forgets the cached family standard deviations (tests, and after a registry change). */
    public void clearCache() {
        familySd = null;
        familySdAt = null;
    }

    /** The population standard deviation. */
    static double sd(double[] values) {
        if (values.length == 0) {
            return 0;
        }
        double mean = Arrays.stream(values).average().orElse(0);
        double sum = 0;
        for (double v : values) {
            sum += (v - mean) * (v - mean);
        }
        return Math.sqrt(sum / values.length);
    }

    /** The situation's summary embedding document id when one exists in the active slot, else null. */
    public String embeddingRef(String situationId) {
        SearchIndexStateStore store = searchIndex.getIfAvailable();
        if (store == null) {
            return null;
        }
        try {
            SearchIndexState state = store.get();
            if (!state.hasActive()) {
                return null;
            }
            String docId = SituationRecordPublisher.SOURCE_ID + ":" + situationId;
            VectorSlot slot = state.activeSlot();
            List<String> found = jdbc.queryForList("SELECT doc_id FROM embedding_document WHERE doc_type = 'record' AND"
                    + " doc_id = :id AND " + slot.modelColumn() + " = :model AND " + slot.embeddingColumn()
                    + " IS NOT NULL", new MapSqlParameterSource().addValue("id", docId)
                            .addValue("model", state.activeModelId()), String.class);
            return found.isEmpty() ? null : docId;
        } catch (DataAccessException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * The summary-embedding cosine of the {@value AnalogSearch#TOP} nearest situations to a target, by situation id,
     * or null when the target has no embedding or no index is active.
     */
    public Map<String, Double> embeddingSimilarities(String situationId) {
        SearchIndexStateStore store = searchIndex.getIfAvailable();
        if (store == null) {
            return null;
        }
        try {
            SearchIndexState state = store.get();
            if (!state.hasActive()) {
                return null;
            }
            VectorSlot slot = state.activeSlot();
            String col = slot.embeddingColumn();
            String model = slot.modelColumn();
            String target = SituationRecordPublisher.SOURCE_ID + ":" + situationId;
            MapSqlParameterSource p = new MapSqlParameterSource().addValue("target", target)
                    .addValue("model", state.activeModelId()).addValue("prefix", SituationRecordPublisher.SOURCE_ID
                            + ":").addValue("limit", AnalogSearch.TOP * 4);
            Integer has = jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_type = 'record' AND"
                    + " doc_id = :target AND " + model + " = :model AND " + col + " IS NOT NULL", p, Integer.class);
            if (has == null || has == 0) {
                return null;
            }
            Map<String, Double> out = new LinkedHashMap<>();
            jdbc.query("SELECT d.doc_id, 1 - (d." + col + " <=> t.v) AS sim FROM embedding_document d, (SELECT " + col
                    + " AS v FROM embedding_document WHERE doc_type = 'record' AND doc_id = :target) t WHERE"
                    + " d.doc_type = 'record' AND d.kind = '" + SituationRecordPublisher.KIND + "' AND d.doc_id LIKE"
                    + " :prefix || '%' AND d.doc_id <> :target AND d." + model + " = :model AND d." + col
                    + " IS NOT NULL ORDER BY d." + col + " <=> t.v LIMIT :limit", p, rs -> {
                        String docId = rs.getString("doc_id");
                        out.put(docId.substring(docId.indexOf(':') + 1), rs.getDouble("sim"));
                    });
            return out;
        } catch (DataAccessException | IllegalArgumentException e) {
            log.debug("Embedding similarity unavailable for {}: {}", situationId, e.getMessage());
            return null;
        }
    }

    /**
     * Computes what is due: today's signature of every activated situation in its first {@value #SIGNATURE_DAYS}
     * days, a backfill of situations with none, and outcomes.
     *
     * @return the signatures and outcomes written
     */
    public int computeDue() {
        Instant now = clock.instant();
        int written = 0;
        List<Map<String, Object>> active = jdbc.queryForList("SELECT s.id, s.started_at FROM situation s WHERE"
                + " s.merged_into IS NULL AND s.started_at >= :from AND EXISTS (SELECT 1 FROM situation_log l WHERE"
                + " l.situation_id = s.id AND l.event = 'situation.activated')", new MapSqlParameterSource("from",
                        Timestamp.from(now.minus(Duration.ofDays(SIGNATURE_DAYS)))));
        for (Map<String, Object> row : active) {
            String id = String.valueOf(row.get("id"));
            int day = dayN(((Timestamp) row.get("started_at")).toInstant(), now);
            if (!signatures.existsById(new SituationSignatureEntity.Key(id, day))) {
                compute(id, day);
                written++;
            }
        }
        List<Map<String, Object>> missing = jdbc.queryForList("SELECT s.id, s.started_at FROM situation s WHERE"
                + " s.merged_into IS NULL AND NOT EXISTS (SELECT 1 FROM situation_signature g WHERE g.situation_id ="
                + " s.id) ORDER BY s.started_at LIMIT :limit", new MapSqlParameterSource("limit", BACKFILL));
        for (Map<String, Object> row : missing) {
            String id = String.valueOf(row.get("id"));
            compute(id, dayN(((Timestamp) row.get("started_at")).toInstant(), now));
            written++;
        }
        List<String> due = jdbc.queryForList("SELECT s.id FROM situation s LEFT JOIN situation_outcome o ON"
                + " o.situation_id = s.id WHERE s.merged_into IS NULL AND (s.status IN ('closed', 'dormant') OR"
                + " s.started_at < :old) AND (o.situation_id IS NULL OR (o.is_final = false AND o.computed_at <"
                + " :stale)) ORDER BY s.started_at LIMIT :limit", new MapSqlParameterSource()
                        .addValue("old", Timestamp.from(now.minus(Duration.ofDays(SIGNATURE_DAYS))))
                        .addValue("stale", Timestamp.from(now.minus(Duration.ofDays(1))))
                        .addValue("limit", OUTCOMES_PER_RUN), String.class);
        for (String id : due) {
            outcome(id);
            written++;
        }
        return written;
    }

    // ------------------------------------------------------------------------- outcomes

    /** The reference time of a situation's outcomes: day 7 of its life, or its end when that comes first. */
    public static Instant referenceAt(SituationFacts s) {
        Instant day7 = s.startedAt().plus(Duration.ofDays(SignatureBuilder.MAX_DAYS));
        Instant end = s.closedAt() != null ? s.closedAt() : s.lastActivityAt();
        return end != null && end.isBefore(day7) ? end : day7;
    }

    /**
     * Computes and stores a situation's outcome.
     *
     * @return the outcome, or empty when the situation does not exist
     */
    public Optional<SituationOutcomeEntity> outcome(String id) {
        Optional<SituationFacts> facts = facts(id);
        if (facts.isEmpty()) {
            return Optional.empty();
        }
        SituationFacts s = facts.get();
        Instant now = clock.instant();
        SituationOutcomeEntity o = outcomes.findById(id).orElseGet(() -> new SituationOutcomeEntity(id));
        Instant end = s.closedAt() != null ? s.closedAt() : s.lastActivityAt();
        o.setDurationDays(end == null ? null : Duration.between(s.startedAt(), end).toMinutes() / 1440.0);
        String peak = s.severity();
        for (String sev : jdbc.queryForList("SELECT DISTINCT severity FROM situation_snapshot WHERE situation_id ="
                + " :id", new MapSqlParameterSource("id", id), String.class)) {
            if (SignatureBuilder.SEVERITIES.indexOf(sev) > SignatureBuilder.SEVERITIES.indexOf(peak)) {
                peak = sev;
            }
        }
        o.setPeakSeverity(peak);
        Instant reference = referenceAt(s);
        o.setReferenceAt(reference);
        o.setFollowOn7d(List.copyOf(familiesActive(s.regionCodes(), reference, 7)));
        o.setFollowOn30d(List.copyOf(familiesActive(s.regionCodes(), reference, 30)));
        o.setHypothesisOutcome(hypothesisOutcome(id));
        o.setFinalOutcome("closed".equals(s.status()) && now.isAfter(reference.plus(Duration.ofDays(30))));
        o.setComputedAt(now);
        return Optional.of(outcomes.save(o));
    }

    /**
     * The newest resolved hypothesis set linked to a situation, as {@code {setId, outcome}} (prompt 125), or null. The
     * hypothesis tables are read through SQL so this package stays independent of the hypothesis layer.
     */
    Map<String, Object> hypothesisOutcome(String situationId) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, resolved_outcome FROM hypothesis_set WHERE"
                    + " situation_id = :id AND status = 'resolved' ORDER BY updated_at DESC LIMIT 1",
                    new MapSqlParameterSource("id", situationId));
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("setId", rows.get(0).get("id"));
            out.put("outcome", rows.get(0).get("resolved_outcome"));
            return out;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * The families active in some regions in {@code (from, from + days]}: the 119 region-day rollups (cross-source
     * rows, by kind) and {@code reference_history} weeks starting in the window.
     */
    public TreeSet<String> familiesActive(List<String> regions, Instant from, int days) {
        TreeSet<String> out = new TreeSet<>();
        List<String> codes = regions == null ? List.of() : regions.stream().filter(c -> !"ZZ".equals(c)).toList();
        if (codes.isEmpty()) {
            return out;
        }
        Instant to = from.plus(Duration.ofDays(days));
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("regions", codes)
                .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to))
                .addValue("fromDay", Date.valueOf(LocalDate.ofInstant(from, ZoneOffset.UTC)))
                .addValue("toDay", Date.valueOf(LocalDate.ofInstant(to, ZoneOffset.UTC)));
        try {
            for (String kind : jdbc.queryForList("SELECT DISTINCT kind FROM activity_rollup WHERE resolution = 3 AND"
                    + " source_id = '*' AND region_code IN (:regions) AND bucket_start > :from AND bucket_start <= :to"
                    + " AND entities > 0", p, String.class)) {
                families.familyOfRecord("*", kind).ifPresent(out::add);
            }
        } catch (DataAccessException e) {
            log.debug("Rollup follow-on families unavailable: {}", e.getMessage());
        }
        out.addAll(jdbc.queryForList("SELECT DISTINCT family FROM reference_history WHERE region_code IN (:regions) AND"
                + " week_start >= :fromDay AND week_start < :toDay AND count > 0", p, String.class));
        return out;
    }

    private static List<String> csv(String text) {
        return text == null || text.isBlank() ? List.of() : List.of(text.split(","));
    }

    private static Instant ts(Timestamp t) {
        return t == null ? null : t.toInstant();
    }
}
