package ai.foresignal.export;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Plans the state at a seq for snapshots and {@code atSeq} exports (prompt 111, Core-Data-Model §12).
 *
 * <p>The state at {@code S} is judged on the transaction axis at {@code T}, the {@code ingested_at}
 * of {@code S} (or the requested {@code asOf}). Per source:
 *
 * <ul>
 *   <li>when {@code T} is at or after the source's asOf horizon ({@code now − window + effective
 *       keyframe + 1 h}), {@code record_version} still holds what the state needs;
 *   <li>otherwise the needed versions start at {@code L} (TRACK {@code S(T − ttl − 2 min)}, STANDARD
 *       {@code S(T − effective keyframe − 1 h)}) and must come from {@code record_version_held}: some
 *       active hold must cover the source, the requested kinds and {@code [L, S]}, and must have been
 *       created while the source's floor was still at or below {@code L}
 *       ({@link HoldService#covering}); else {@code 400 SNAPSHOT_BEYOND_RETENTION}.
 * </ul>
 *
 * <p>The TRACK lower bound of the query is the smallest {@code L} over the TRACK sources; STANDARD
 * reads from 0. Validity widens over downsampled ranges as asOf reads do (prompt 044).
 */
@Component
public class PointInTimePlanner {

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final AsOfResolver asOf;
    private final RetentionState retention;
    private final SourceRegistry registry;
    private final HoldService holds;
    private final ChangeBus changeBus;
    private final Clock clock;

    /** Creates the planner. */
    public PointInTimePlanner(ReadQueryRunner runner, SeqClockQuery seqClock, AsOfResolver asOf,
            RetentionState retention, SourceRegistry registry, HoldService holds, ChangeBus changeBus, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.asOf = asOf;
        this.retention = retention;
        this.registry = registry;
        this.holds = holds;
        this.changeBus = changeBus;
        this.clock = clock;
    }

    /** {@code S(T)} for a requested {@code asOf}. */
    public long seqAt(Instant t) {
        return runner.read(jdbc -> seqClock.seqAt(jdbc, t));
    }

    /** The current watermark ({@code ChangeBus.lastPublishedSeq()}). */
    public long watermark() {
        return changeBus.lastPublishedSeq();
    }

    /**
     * Plans the state at {@code s} over {@code sources}.
     *
     * @param t the transaction time to judge validity at, or null for the {@code ingested_at} of {@code s}
     * @param kinds the requested kinds (empty for all), which a covering hold must include
     * @throws ValidationException {@code SNAPSHOT_BEYOND_RETENTION} when a source's needed versions are
     *     gone and no hold covers them, or {@code EXPORT_SPEC_INVALID} for a seq in the future
     */
    public PointInTime plan(long s, Instant t, Collection<String> sources, Collection<String> kinds) {
        if (s > watermark()) {
            throw new ValidationException(ErrorCodes.EXPORT_SPEC_INVALID,
                    "atSeq " + s + " is past the current watermark " + watermark() + ".", List.of());
        }
        return runner.read(jdbc -> plan(jdbc, s, t, sources, kinds));
    }

    private PointInTime plan(NamedParameterJdbcTemplate jdbc, long s, Instant requested, Collection<String> sources,
            Collection<String> kinds) {
        Instant t = requested != null ? requested : transactionTime(jdbc, s);
        Instant now = clock.instant();
        Map<HistoryTier, Long> lower = new EnumMap<>(HistoryTier.class);
        Map<String, Duration> validity = new LinkedHashMap<>();
        Map<String, List<String>> held = new LinkedHashMap<>();
        boolean approximate = false;
        for (String id : sources) {
            SourceDescriptor descriptor = registry.find(id).map(r -> r.descriptor()).orElse(null);
            if (descriptor == null) {
                continue;
            }
            HistoryTier tier = descriptor.historyTier();
            Duration ttl = descriptor.recordTtl();
            Instant horizon = asOf.horizon(id, now);
            long needed;
            if (tier == HistoryTier.TRACK) {
                needed = ttl == null ? 0L : seqClock.seqAt(jdbc, t.minus(ttl).minus(AsOfResolver.TRACK_LOWER_MARGIN));
                lower.merge(tier, needed, Math::min);
            } else {
                Duration margin = Duration.between(now.minus(retention.window(id)), horizon);
                needed = seqClock.seqAt(jdbc, t.minus(margin.isNegative() ? Duration.ZERO : margin));
                lower.putIfAbsent(tier, 0L);
            }
            if (t.isBefore(horizon)) {
                List<String> covering = new ArrayList<>();
                for (HoldEntity hold : holds.covering(id, kinds, needed, s)) {
                    covering.add(hold.getId());
                }
                if (covering.isEmpty()) {
                    throw new ValidationException(ErrorCodes.SNAPSHOT_BEYOND_RETENTION,
                            "The state at seq " + s + " (" + t + ") is before the retention horizon of " + id + " ("
                                    + horizon + ") and no hold covers it.", List.of())
                            .withProperty("sourceId", id).withProperty("horizon", horizon.toString());
                }
                held.put(id, covering);
            }
            boolean downsampled = asOf.fidelity().downsampled(tier, needed, s);
            Duration widening = downsampled ? asOf.fidelity().validityWidening(id) : Duration.ZERO;
            validity.put(id, ttl == null ? null : ttl.plus(widening == null ? Duration.ZERO : widening));
            approximate |= downsampled || asOf.sampled(id);
        }
        return new PointInTime(s, t, lower, validity, held, approximate);
    }

    /**
     * The {@code ingested_at} of {@code s}: of the newest version at or below it (in
     * {@code record_version} or a held copy), else the start of its {@code seq_clock} minute.
     *
     * @throws ValidationException {@code SNAPSHOT_BEYOND_RETENTION} when nothing records when {@code s} was ingested
     */
    Instant transactionTime(NamedParameterJdbcTemplate jdbc, long s) {
        MapSqlParameterSource params = new MapSqlParameterSource("s", s);
        List<OffsetDateTime> found = jdbc.queryForList("SELECT ingested_at FROM ("
                + " (SELECT seq, ingested_at FROM record_version WHERE seq <= :s ORDER BY seq DESC LIMIT 1)"
                + " UNION ALL (SELECT seq, ingested_at FROM " + HeldVersionsTable.NAME
                + " WHERE seq <= :s ORDER BY seq DESC LIMIT 1)) x ORDER BY seq DESC LIMIT 1", params, OffsetDateTime.class);
        if (!found.isEmpty() && found.getFirst() != null) {
            return found.getFirst().toInstant();
        }
        List<OffsetDateTime> minute = jdbc.queryForList(
                "SELECT minute FROM seq_clock WHERE first_seq <= :s ORDER BY minute DESC LIMIT 1", params,
                OffsetDateTime.class);
        if (!minute.isEmpty() && minute.getFirst() != null) {
            return minute.getFirst().toInstant();
        }
        throw new ValidationException(ErrorCodes.SNAPSHOT_BEYOND_RETENTION,
                "No version at or below seq " + s + " remains to date it.", List.of());
    }

    /** An asOf plan of {@link AsOfResolver} as a point in time (no held sources). */
    public static PointInTime fromAsOf(AsOfPlan plan) {
        Map<HistoryTier, Long> lower = new EnumMap<>(HistoryTier.class);
        for (HistoryTier tier : plan.sources().keySet()) {
            lower.put(tier, plan.lower(tier));
        }
        return new PointInTime(plan.s(), plan.t(), lower, plan.validity(), Map.of(), plan.approximate());
    }

    /** The stored form of a point in time (in a job's plan). */
    public static Map<String, Object> toMap(PointInTime pit) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("s", pit.s());
        out.put("t", pit.t().toString());
        Map<String, Object> lower = new LinkedHashMap<>();
        pit.lower().forEach((tier, value) -> lower.put(tier.name(), value));
        out.put("lower", lower);
        Map<String, Object> validity = new LinkedHashMap<>();
        pit.validity().forEach((source, value) -> validity.put(source, value == null ? null : value.toString()));
        out.put("validity", validity);
        out.put("held", pit.held());
        out.put("approximate", pit.approximate());
        return out;
    }

    /** A point in time back from {@link #toMap}. */
    public static PointInTime fromMap(Map<?, ?> map) {
        Map<HistoryTier, Long> lower = new EnumMap<>(HistoryTier.class);
        if (map.get("lower") instanceof Map<?, ?> raw) {
            raw.forEach((tier, value) -> lower.put(HistoryTier.valueOf(String.valueOf(tier)), ((Number) value).longValue()));
        }
        Map<String, Duration> validity = new LinkedHashMap<>();
        if (map.get("validity") instanceof Map<?, ?> raw) {
            raw.forEach((source, value) -> validity.put(String.valueOf(source),
                    value == null ? null : Duration.parse(String.valueOf(value))));
        }
        Map<String, List<String>> held = new LinkedHashMap<>();
        if (map.get("held") instanceof Map<?, ?> raw) {
            raw.forEach((source, value) -> {
                List<String> ids = new ArrayList<>();
                if (value instanceof List<?> list) {
                    list.forEach(item -> ids.add(String.valueOf(item)));
                }
                held.put(String.valueOf(source), ids);
            });
        }
        return new PointInTime(((Number) map.get("s")).longValue(), Instant.parse(String.valueOf(map.get("t"))), lower,
                validity, held, Boolean.TRUE.equals(map.get("approximate")));
    }
}
