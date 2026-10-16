package ai.foresignal.backtest;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.history.HistoryFidelity;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.AnomalyDetector;
import ai.foresignal.trigger.window.RuleType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The backtest job framework (prompt 103 §1): submit, run, poll and cancel jobs of every type on one
 * table.
 *
 * <ul>
 *   <li><strong>Submit</strong> validates the target, resolves the requested range (time through
 *       {@code seq_clock} {@code S(T)}, or seqs), caps it at {@value #MAX_DAYS} days (400
 *       {@code BACKTEST_RANGE_INVALID} beyond), clamps the upper seq to the published watermark and
 *       the lower seq to the retained floor of the involved sources ({@code RANGE_CLAMPED_BY_RETENTION}),
 *       snapshots the definition, enforces one queued or running job per token (409
 *       {@code BACKTEST_IN_PROGRESS}) and hands the job to the worker pool <em>after the submitting
 *       transaction commits</em>, so the worker never looks for a row that is not yet visible.
 *   <li><strong>Run</strong> claims the job ({@code queued → running}) with a conditional update, runs
 *       it with a {@link JobMonitor} that records progress and checks for cancellation between chunks,
 *       and writes the terminal state with a conditional update that never overwrites
 *       {@code cancelled}. Any {@link Throwable}, a {@link StackOverflowError} included, ends the job
 *       {@code failed}.
 *   <li><strong>Cancel</strong> flips {@code queued|running → cancelled}; the run stops at its next
 *       checkpoint and its result is discarded.
 *   <li>Finished jobs are deleted after {@value #RETENTION_DAYS} days.
 * </ul>
 *
 * <p>The worker pool has {@code foresignal.backtest.concurrency} platform threads (default 1, at
 * most 2).
 */
@Service
public class BacktestJobService {

    /** The maximum range, days. */
    public static final long MAX_DAYS = 90;

    /** The days a finished job is kept. */
    public static final long RETENTION_DAYS = 30;

    /** The default range of a precursor job without one, days. */
    public static final long DEFAULT_PRECURSOR_DAYS = 30;

    /** The default range of any other job without one, days. */
    public static final long DEFAULT_DAYS = 1;

    /** The detector version recorded on rule jobs of record and windowed rules. */
    public static final String RULE_BACKTEST_VERSION = "rule-backtest/1.0.0";

    /** The detector version recorded on recipe jobs. */
    public static final String RECIPE_BACKTEST_VERSION = "recipe-backtest/1.0.0";

    /** The job types this service runs. */
    public static final List<String> TYPES = List.of("rule", "recipe", "precursor", "quality");

    private static final Logger log = LoggerFactory.getLogger(BacktestJobService.class);

    private final BacktestJobRepository jobs;
    private final RuleBacktest ruleBacktest;
    private final RecipeBacktest recipeBacktest;
    private final PrecursorTestService precursorTests;
    private final TriggerRuleService rules;
    private final ReadQueryRunner reads;
    private final SeqClockQuery seqClock;
    private final RetentionState retention;
    private final HistoryFidelity fidelity;
    private final ChangeBus changeBus;
    private final BacktestCoverage coverage;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final ExecutorService executor;

    /** Creates the service. */
    public BacktestJobService(BacktestJobRepository jobs, RuleBacktest ruleBacktest, RecipeBacktest recipeBacktest,
            PrecursorTestService precursorTests, TriggerRuleService rules, ReadQueryRunner reads, SeqClockQuery seqClock,
            RetentionState retention, HistoryFidelity fidelity, ChangeBus changeBus, BacktestCoverage coverage,
            JdbcTemplate jdbc, JsonMapper json, Clock clock,
            @Value("${foresignal.backtest.concurrency:1}") int concurrency) {
        this.jobs = jobs;
        this.ruleBacktest = ruleBacktest;
        this.recipeBacktest = recipeBacktest;
        this.precursorTests = precursorTests;
        this.rules = rules;
        this.reads = reads;
        this.seqClock = seqClock;
        this.retention = retention;
        this.fidelity = fidelity;
        this.changeBus = changeBus;
        this.coverage = coverage;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
        this.executor = Executors.newFixedThreadPool(Math.max(1, Math.min(2, concurrency)), r -> {
            Thread t = new Thread(r, "foresignal-backtest");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Submits a job.
     *
     * @param type {@code rule}, {@code recipe}, {@code precursor} or {@code quality} (null is rule)
     * @param targetId the rule, recipe or precursor test id
     * @param definition an inline definition (recorded in the snapshot; rule and precursor jobs need a target)
     * @param range an object with {@code from}/{@code to} (ISO-8601) or {@code fromSeq}/{@code toSeq}
     * @param variants up to five rule variants
     * @param tokenId the submitting token's id (the one-job-per-token rule), or null for system jobs
     * @throws ValidationException 400 {@code BACKTEST_RANGE_INVALID} for an empty, inverted, too long
     *     or wholly-below-retention range
     * @throws ConflictException 409 {@code BACKTEST_IN_PROGRESS}
     */
    @Transactional
    public BacktestJobEntity submit(String type, String targetId, JsonNode definition, JsonNode range,
            List<Map<String, Object>> variants, String tokenId) {
        String normalized = type == null ? "rule" : type;
        if (!TYPES.contains(normalized)) {
            throw ValidationException.forField("type", "must be rule, recipe, precursor or quality");
        }
        Map<String, Object> definitionMap = definition == null || definition.isNull() || !definition.isObject()
                ? new LinkedHashMap<>()
                : json.readValue(json.writeValueAsString(definition), new tools.jackson.core.type.TypeReference<>() {});
        Set<String> sources = new LinkedHashSet<>();
        String detectorVersion = null;
        switch (normalized) {
            case "rule" -> {
                UUID ruleId = ruleId(targetId);
                TriggerRuleEntity rule = rules.load(ruleId);
                sources.addAll(rule.getSources());
                definitionMap.put("ruleId", targetId);
                definitionMap.put("rule", ruleSnapshot(rule));
                detectorVersion = RuleType.fromWire(rule.getType()) == RuleType.ANOMALY
                        ? AnomalyDetector.NAME + "/" + AnomalyDetector.VERSION : RULE_BACKTEST_VERSION;
            }
            case "precursor" -> {
                PrecursorTestEntity test = precursorTests.get(targetId);
                sources.addAll(precursorTests.involvedSources(test));
                definitionMap.put("precursorTest", BacktestViews.precursorTest(test));
                detectorVersion = "precursor-tester/" + PrecursorTester.VERSION;
            }
            case "recipe" -> {
                if (targetId != null) {
                    definitionMap.put("recipeId", targetId);
                }
                detectorVersion = RECIPE_BACKTEST_VERSION;
            }
            default -> { }
        }
        if (variants != null && !variants.isEmpty()) {
            definitionMap.put("variants", new ArrayList<>(
                    variants.subList(0, Math.min(variants.size(), RuleBacktest.MAX_VARIANTS))));
        }
        if (tokenId != null) {
            List<BacktestJobEntity> inFlight = jobs.findByCreatedByAndStatusIn(tokenId, List.of("queued", "running"));
            if (!inFlight.isEmpty()) {
                throw new ConflictException(ErrorCodes.BACKTEST_IN_PROGRESS,
                        "You already have a queued or running job (" + inFlight.get(0).getId() + ").");
            }
        }
        ResolvedRange resolved = resolveRange(normalized, range, sources);
        Instant now = clock.instant();
        BacktestJobEntity job = new BacktestJobEntity(Ulids.next(now), normalized, now);
        job.setTargetId(targetId);
        job.setCreatedBy(tokenId);
        job.setDefinition(definitionMap);
        job.setFromSeq(resolved.fromSeq());
        job.setToSeq(resolved.toSeq());
        job.setRangeFrom(resolved.from());
        job.setRangeTo(resolved.to());
        job.setRangeClamped(resolved.clamped());
        job.setDetectorVersion(detectorVersion);
        job.setDefinitionHash(hash(json.writeValueAsString(definitionMap) + "|" + resolved.fromSeq() + "|"
                + resolved.toSeq() + "|" + resolved.from() + "|" + resolved.to()));
        job.setCaveats(CaveatGenerator.stored(CaveatGenerator.generate(new CaveatGenerator.Inputs(resolved.clamped(),
                false, false, false, false, false, false, Integer.MAX_VALUE))));
        jobs.save(job);
        String id = job.getId();
        afterCommit(() -> executor.submit(() -> runQuietly(id)));
        return job;
    }

    /** Runs {@code action} once the current transaction commits, or at once outside one. */
    static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private static UUID ruleId(String targetId) {
        if (targetId == null) {
            throw ValidationException.forField("targetId", "a rule backtest needs the rule's id");
        }
        try {
            return UUID.fromString(targetId);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("targetId", "must be a rule id (UUID)");
        }
    }

    /** The parts of a rule that decide what it fires, for the definition snapshot. */
    private static Map<String, Object> ruleSnapshot(TriggerRuleEntity rule) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", String.valueOf(rule.getId()));
        out.put("name", rule.getName());
        out.put("type", rule.getType());
        out.put("sources", rule.getSources());
        out.put("kinds", rule.getKinds());
        out.put("conditions", rule.getConditions() == null ? List.of() : rule.getConditions().stream()
                .map(c -> {
                    Map<String, Object> condition = new LinkedHashMap<>();
                    condition.put("path", c.path());
                    condition.put("op", c.op());
                    condition.put("value", c.value());
                    return condition;
                }).toList());
        out.put("mode", rule.getMode() == null ? null : rule.getMode().wireValue());
        out.put("cooldownSeconds", rule.getCooldownSeconds());
        out.put("cooldownScope", rule.getCooldownScope());
        out.put("maxAlertsPerHour", rule.getMaxAlertsPerHour());
        out.put("forecastHorizonSeconds", rule.getForecastHorizonSeconds());
        out.put("window", rule.getWindowSpec());
        out.put("updatedAt", rule.getUpdatedAt() == null ? null : rule.getUpdatedAt().toString());
        return out;
    }

    /**
     * A resolved range.
     *
     * @param fromSeq exclusive lower seq bound
     * @param toSeq inclusive upper seq bound
     * @param from the start in time
     * @param to the end in time
     * @param clamped true when the start was raised to the retained floor
     */
    record ResolvedRange(long fromSeq, long toSeq, Instant from, Instant to, boolean clamped) {}

    /** Resolves and validates the requested range (see the class comment). */
    ResolvedRange resolveRange(String type, JsonNode range, Set<String> sources) {
        JsonNode r = range == null || range.isNull() ? json.createObjectNode() : range;
        boolean timeRange = r.hasNonNull("from") || r.hasNonNull("to");
        boolean seqRange = r.hasNonNull("fromSeq") || r.hasNonNull("toSeq");
        if (timeRange && seqRange) {
            throw invalidRange("Use from/to or fromSeq/toSeq, not both.");
        }
        Instant now = clock.instant();
        long watermark = changeBus.lastPublishedSeq();
        Duration defaultSpan = Duration.ofDays("precursor".equals(type) ? DEFAULT_PRECURSOR_DAYS : DEFAULT_DAYS);
        Duration cap = Duration.ofDays(MAX_DAYS);
        long fromSeq;
        long toSeq;
        Instant from;
        Instant to;
        if (seqRange) {
            Long requestedFrom = seq(r, "fromSeq");
            Long requestedTo = seq(r, "toSeq");
            if (requestedFrom != null && requestedTo != null && requestedFrom >= requestedTo) {
                throw invalidRange("The range is empty or inverted.");
            }
            toSeq = requestedTo == null ? watermark : Math.min(requestedTo, watermark);
            Instant end = timeOfSeq(toSeq, now);
            to = end;
            if (requestedFrom != null) {
                fromSeq = requestedFrom;
                long capSeq = reads.read(t -> seqClock.seqAt(t, end.minus(cap)));
                if (fromSeq < capSeq) {
                    throw invalidRange("The range is longer than " + MAX_DAYS + " days.");
                }
            } else {
                fromSeq = Math.min(toSeq, reads.read(t -> seqClock.seqAt(t, end.minus(defaultSpan))));
            }
            Instant start = timeOfSeq(fromSeq, end.minus(defaultSpan));
            from = start.isAfter(end) ? end : start;
        } else {
            Instant requestedTo = instant(r, "to");
            Instant requestedFrom = instant(r, "from");
            Instant end = requestedTo == null || requestedTo.isAfter(now) ? now : requestedTo;
            Instant start = requestedFrom == null ? end.minus(defaultSpan) : requestedFrom;
            if (!start.isBefore(end)) {
                throw invalidRange("The range is empty or inverted.");
            }
            if (Duration.between(start, end).compareTo(cap) > 0) {
                throw invalidRange("The range is longer than " + MAX_DAYS + " days.");
            }
            from = start;
            to = end;
            fromSeq = reads.read(t -> seqClock.seqAt(t, start));
            toSeq = Math.min(watermark, reads.read(t -> seqClock.seqAt(t, end)));
        }
        boolean clamped = false;
        if ("rule".equals(type)) {
            long floor = retention.lowestRetainedSeq(sources);
            if (floor > 0 && toSeq <= floor) {
                throw invalidRange("The range is wholly below retention.");
            }
            if (floor > 0 && fromSeq < floor) {
                fromSeq = floor;
                from = timeOfSeq(floor, from);
                clamped = true;
            }
            if (fromSeq >= toSeq) {
                throw invalidRange("The range is empty: no history was stored in it.");
            }
        }
        return new ResolvedRange(fromSeq, toSeq, from, to, clamped);
    }

    private static ValidationException invalidRange(String detail) {
        return new ValidationException(ErrorCodes.BACKTEST_RANGE_INVALID, detail + " (at most " + MAX_DAYS
                + " days, inside retained history)", List.of());
    }

    private static Long seq(JsonNode range, String field) {
        JsonNode node = range.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.canConvertToLong() || node.asLong() < 0) {
            throw new ValidationException(ErrorCodes.BACKTEST_RANGE_INVALID, field + " must be a non-negative integer.",
                    List.of(new FieldError(field, "must be a non-negative integer")));
        }
        return node.asLong();
    }

    private static Instant instant(JsonNode range, String field) {
        JsonNode node = range.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(node.asString()).toInstant();
        } catch (DateTimeParseException e) {
            throw new ValidationException(ErrorCodes.BACKTEST_RANGE_INVALID, field + " must be an ISO-8601 instant.",
                    List.of(new FieldError(field, "must be an ISO-8601 instant")));
        }
    }

    /**
     * The minute in which {@code seq} was allocated ({@code seq_clock}, minute resolution), or
     * {@code fallback} when the clock has no minute that early.
     */
    private Instant timeOfSeq(long seq, Instant fallback) {
        List<OffsetDateTime> minutes = reads.read(t -> t.getJdbcTemplate().queryForList(
                "SELECT minute FROM seq_clock WHERE first_seq <= ? ORDER BY minute DESC LIMIT 1", OffsetDateTime.class,
                seq));
        return minutes.isEmpty() || minutes.getFirst() == null ? fallback : minutes.getFirst().toInstant();
    }

    private void runQuietly(String id) {
        try {
            run(id);
        } catch (Throwable e) {
            log.warn("backtest job {} failed outside its run: {}", id, e.toString());
            finish(id, "failed", Map.of("error", String.valueOf(e)), null);
        }
    }

    /**
     * Runs a queued job to completion. Does nothing when the job is no longer queued (another worker
     * claimed it, or it was canceled).
     */
    public void run(String id) {
        int claimed = jdbc.update("UPDATE backtest_job SET status = 'running', started_at = ? WHERE id = ?"
                + " AND status = 'queued'", Timestamp.from(clock.instant()), id);
        if (claimed == 0) {
            return;
        }
        BacktestJobEntity job = get(id);
        JobMonitor monitor = monitor(id);
        try {
            Map<String, Object> result = switch (job.getType()) {
                case "rule" -> ruleBacktest.run(UUID.fromString(job.getTargetId()), job.getFromSeq(), job.getToSeq(),
                        job.getRangeFrom(), job.getRangeTo(), variants(job), monitor);
                case "recipe" -> recipeBacktest.run(job.getTargetId(), job.getRangeFrom(), job.getRangeTo(), monitor);
                case "precursor" -> precursorTests.runJob(job, monitor);
                default -> new LinkedHashMap<>();
            };
            List<Map<String, Object>> caveats = caveats(job, result);
            if ("precursor".equals(job.getType())) {
                // PrecursorTestResult carries its caveats too (required by the spec).
                result = new LinkedHashMap<>(result);
                result.put("caveats", caveats);
            }
            finish(id, "done", truncate(result), caveats);
        } catch (JobMonitor.Canceled e) {
            // The cancel already wrote the terminal state.
        } catch (Throwable e) {
            log.warn("backtest job {} failed: {}", id, e.toString());
            finish(id, "failed", Map.of("error", String.valueOf(e.getMessage() == null ? e : e.getMessage())), null);
        }
    }

    /** Writes a terminal state unless the job already left {@code running} (a cancel). */
    private void finish(String id, String status, Map<String, Object> result, List<Map<String, Object>> caveats) {
        if (caveats == null) {
            jdbc.update("UPDATE backtest_job SET status = ?, finished_at = ?, result = CAST(? AS jsonb)"
                            + " WHERE id = ? AND status IN ('queued', 'running')",
                    status, Timestamp.from(clock.instant()), json.writeValueAsString(result), id);
        } else {
            jdbc.update("UPDATE backtest_job SET status = ?, progress = 1, finished_at = ?, result = CAST(? AS jsonb),"
                            + " caveats = CAST(? AS jsonb) WHERE id = ? AND status = 'running'",
                    status, Timestamp.from(clock.instant()), json.writeValueAsString(result),
                    json.writeValueAsString(caveats), id);
        }
    }

    /** A monitor that reads the {@code cancelled} flag and writes the progress of job {@code id}. */
    JobMonitor monitor(String id) {
        return new JobMonitor() {
            @Override
            public boolean canceled() {
                Boolean cancelled = jdbc.queryForObject("SELECT cancelled FROM backtest_job WHERE id = ?",
                        Boolean.class, id);
                return Boolean.TRUE.equals(cancelled);
            }

            @Override
            public void progress(double fraction) {
                jdbc.update("UPDATE backtest_job SET progress = ? WHERE id = ? AND status = 'running'", fraction, id);
            }
        };
    }

    /** The caveats of a finished job, computed from the range, the stored history and the result. */
    private List<Map<String, Object>> caveats(BacktestJobEntity job, Map<String, Object> result) {
        Set<String> sources = new LinkedHashSet<>();
        boolean windowApproximate = false;
        boolean definitionDiffers = false;
        if ("rule".equals(job.getType())) {
            try {
                TriggerRuleEntity rule = rules.load(UUID.fromString(job.getTargetId()));
                sources.addAll(rule.getSources());
                RuleType type = RuleType.fromWire(rule.getType());
                windowApproximate = type == RuleType.COUNT || type == RuleType.ABSENCE;
                // The actual alerts in range came from the definition in force then; a rule edited
                // after the range started may have fired them under a different definition.
                definitionDiffers = rule.getUpdatedAt() != null && job.getRangeFrom() != null
                        && rule.getUpdatedAt().isAfter(job.getRangeFrom());
            } catch (RuntimeException e) {
                definitionDiffers = true;
            }
        } else if ("precursor".equals(job.getType())) {
            try {
                PrecursorTestEntity test = precursorTests.get(job.getTargetId());
                sources.addAll(precursorTests.involvedSources(test));
                Object snapshot = job.getDefinition() == null ? null : job.getDefinition().get("precursorTest");
                definitionDiffers = snapshot != null && !json.writeValueAsString(snapshot)
                        .equals(json.writeValueAsString(BacktestViews.precursorTest(test)));
            } catch (RuntimeException e) {
                definitionDiffers = true;
            }
        }
        Instant from = job.getRangeFrom();
        Instant to = job.getRangeTo();
        boolean gaps = from != null && to != null && to.isAfter(from) && coverage.gapFraction(sources, from, to) > 0;
        boolean entityChanges = from != null && to != null && coverage.entityResolutionChanged(sources, from, to);
        return CaveatGenerator.stored(CaveatGenerator.generate(new CaveatGenerator.Inputs(
                Boolean.TRUE.equals(job.getRangeClamped()), downsampled(job), coverage.sampled(sources), gaps,
                definitionDiffers, entityChanges, windowApproximate, sampleSize(job.getType(), result))));
    }

    /** The sample size behind a result: matches, antecedents or events. */
    static int sampleSize(String type, Map<String, Object> result) {
        Object value = switch (type) {
            case "rule" -> result.get("wouldFire");
            case "precursor" -> result.get("nAntecedent");
            case "recipe" -> result.get("eventCount");
            default -> null;
        };
        return value instanceof Number n ? (int) Math.min(Integer.MAX_VALUE, n.longValue()) : Integer.MAX_VALUE;
    }

    private boolean downsampled(BacktestJobEntity job) {
        try {
            return fidelity != null && job.getToSeq() != null && job.getFromSeq() != null
                    && job.getToSeq() > job.getFromSeq()
                    && fidelity.downsampled(HistoryTier.TRACK, job.getFromSeq() + 1, job.getToSeq());
        } catch (RuntimeException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> variants(BacktestJobEntity job) {
        Object raw = job.getDefinition() == null ? null : job.getDefinition().get("variants");
        if (raw instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    out.add((Map<String, Object>) map);
                }
            }
            return out;
        }
        return List.of();
    }

    /** A bounded result (≤ 1 MB estimated by serialized length; larger is truncated). */
    Map<String, Object> truncate(Map<String, Object> result) {
        String serialized = json.writeValueAsString(result);
        if (serialized.length() <= 1_000_000) {
            return result;
        }
        Map<String, Object> truncated = new LinkedHashMap<>(result);
        truncated.put("truncated", true);
        truncated.remove("samples");
        truncated.remove("topEntities");
        truncated.remove("events");
        return truncated;
    }

    /** Lists jobs. */
    public List<BacktestJobEntity> list(String status, String type, int limit) {
        if (status != null && type != null) {
            return jobs.findByStatusAndTypeOrderByCreatedAtDescIdDesc(status, type, Limit.of(limit));
        }
        if (status != null) {
            return jobs.findByStatusOrderByCreatedAtDescIdDesc(status, Limit.of(limit));
        }
        if (type != null) {
            return jobs.findByTypeOrderByCreatedAtDescIdDesc(type, Limit.of(limit));
        }
        return jobs.findAllByOrderByCreatedAtDescIdDesc(Limit.of(limit));
    }

    /** One job, or 404. */
    public BacktestJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_BACKTEST,
                "No such job: " + id));
    }

    /**
     * Cancels a queued or running job; the cancellation sticks (the run never overwrites it).
     *
     * @throws NotFoundException when there is no such job
     */
    public BacktestJobEntity cancel(String id) {
        get(id);
        jdbc.update("UPDATE backtest_job SET cancelled = true, status = 'cancelled', finished_at = ?"
                + " WHERE id = ? AND status IN ('queued', 'running')", Timestamp.from(clock.instant()), id);
        return get(id);
    }

    /** Deletes jobs finished more than {@value #RETENTION_DAYS} days ago; returns how many. */
    @Scheduled(fixedDelayString = "${foresignal.backtest.purge-interval:1h}", initialDelayString = "5m")
    public int purgeFinished() {
        return jdbc.update("DELETE FROM backtest_job WHERE finished_at IS NOT NULL AND finished_at < ?",
                Timestamp.from(clock.instant().minus(Duration.ofDays(RETENTION_DAYS))));
    }

    /** The maximum seq of the retained history (for range caps). */
    public long watermark() {
        return changeBus.lastPublishedSeq();
    }

    private static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** Stops the worker pool. */
    @jakarta.annotation.PreDestroy
    void shutdown() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** The worker pool, for tests. */
    ExecutorService executor() {
        return executor;
    }
}
