package ai.foresignal.backtest;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.HistoryFidelity;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The backtest job framework (prompt 103): submit, run, poll and cancel jobs of every type on one
 * table. A worker pool (default one thread) replays the range in chunks with a per-token
 * one-job rule; ranges are resolved through {@code seq_clock} and clamped to the retained floor.
 */
@Service
public class BacktestJobService {

    /** The maximum range, days. */
    public static final long MAX_DAYS = 90;

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
    private final JsonMapper json;
    private final Clock clock;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "foresignal-backtest");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean active = new AtomicBoolean();

    /** Creates the service. */
    public BacktestJobService(BacktestJobRepository jobs, RuleBacktest ruleBacktest, RecipeBacktest recipeBacktest,
            PrecursorTestService precursorTests, TriggerRuleService rules, ReadQueryRunner reads, SeqClockQuery seqClock,
            RetentionState retention, HistoryFidelity fidelity, ChangeBus changeBus, JsonMapper json, Clock clock) {
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
        this.json = json;
        this.clock = clock;
    }

    /** Submits a job. */
    @Transactional
    public BacktestJobEntity submit(String type, String targetId, JsonNode definition, JsonNode range,
            List<Map<String, Object>> variants, String tokenId) {
        JsonNode effectiveRange = range == null || range.isNull() ? json.createObjectNode() : range;
        String normalized = type == null ? "rule" : type;
        if (!List.of("rule", "recipe", "precursor", "quality").contains(normalized)) {
            throw ValidationException.forField("type", "must be rule, recipe, precursor or quality");
        }
        if (tokenId != null) {
            List<BacktestJobEntity> inFlight = jobs.findByCreatedByAndStatusIn(tokenId, List.of("queued", "running"));
            if (!inFlight.isEmpty()) {
                throw new ConflictException(ErrorCodes.BACKTEST_IN_PROGRESS,
                        "You already have a queued or running job (" + inFlight.get(0).getId() + ").");
            }
        }
        Instant now = clock.instant();
        BacktestJobEntity job = new BacktestJobEntity(Ulids.next(now), normalized, now);
        job.setTargetId(targetId);
        job.setCreatedBy(tokenId);
        Map<String, Object> definitionMap = definition == null || definition.isNull() ? new LinkedHashMap<>()
                : json.readValue(json.writeValueAsString(definition), new tools.jackson.core.type.TypeReference<>() {});
        if (normalized.equals("rule") && targetId != null) {
            definitionMap.put("ruleId", targetId);
        }
        job.setDefinition(definitionMap);
        job.setDefinitionHash(hash(definitionMap, effectiveRange));
        Long fromSeq = effectiveRange.path("fromSeq").isNumber() ? effectiveRange.get("fromSeq").asLong() : null;
        Long toSeq = effectiveRange.path("toSeq").isNumber() ? effectiveRange.get("toSeq").asLong() : null;
        job.setFromSeq(fromSeq);
        job.setToSeq(toSeq);
        if (definitionMap.get("variants") instanceof List<?> ignored) {
            // variants were supplied in the definition
        }
        if (variants != null && !variants.isEmpty()) {
            definitionMap.put("variants", variants.subList(0, Math.min(variants.size(), RuleBacktest.MAX_VARIANTS)));
        }
        jobs.save(job);
        executor.submit(() -> runQuietly(job.getId()));
        return job;
    }

    private void runQuietly(String id) {
        try {
            run(id);
        } catch (RuntimeException e) {
            jobs.findById(id).ifPresent(job -> {
                job.setStatus("failed");
                job.setFinishedAt(clock.instant());
                jobs.save(job);
            });
        }
    }

    /** Runs a job to completion. */
    public void run(String id) {
        BacktestJobEntity job = get(id);
        job.setStatus("running");
        job.setStartedAt(clock.instant());
        jobs.save(job);
        try {
            resolveRange(job);
            Map<String, Object> result = switch (job.getType()) {
                case "rule" -> ruleBacktest.run(UUID.fromString(job.getTargetId()), job.getFromSeq(), job.getToSeq(),
                        variants(job));
                case "recipe" -> recipeBacktest.run(job.getFromSeq(), job.getToSeq());
                case "precursor" -> precursorTests.runJob(job);
                default -> Map.of();
            };
            job.setResult(truncate(result));
            job.setStatus("done");
            job.setProgress(1.0);
        } catch (RuntimeException e) {
            job.setStatus("failed");
            job.setResult(Map.of("error", String.valueOf(e.getMessage())));
        }
        job.setFinishedAt(clock.instant());
        jobs.save(job);
    }

    private void resolveRange(BacktestJobEntity job) {
        Instant now = clock.instant();
        long to;
        long from;
        if (job.getToSeq() != null) {
            to = job.getToSeq();
            from = job.getFromSeq() != null ? job.getFromSeq()
                    : Math.min(to, reads.read(t -> seqClock.seqAt(t, now.minus(Duration.ofDays(1)))));
        } else {
            Instant until = job.getRangeTo() != null ? job.getRangeTo() : now;
            Instant since = job.getRangeFrom() != null ? job.getRangeFrom() : until.minus(Duration.ofDays(1));
            long floor = retention.lowestRetainedSeq(java.util.Set.of());
            from = reads.read(t -> seqClock.seqAt(t, since));
            to = reads.read(t -> seqClock.seqAt(t, until));
            if (floor > 0 && from < floor) {
                from = floor;
            }
        }
        if (from >= to) {
            throw new ValidationException(ErrorCodes.BACKTEST_RANGE_INVALID,
                    "The range is empty, inverted or wholly below retention.", List.of());
        }
        job.setFromSeq(from);
        job.setToSeq(to);
        job.setCaveats(CaveatGenerator.stored(CaveatGenerator.generate(new CaveatGenerator.Inputs(
                job.getRangeFrom() != null && job.getFromSeq() != null, downsampled(job), false, false, false, false,
                job.getType().equals("rule"), 100))));
    }

    private boolean downsampled(BacktestJobEntity job) {
        try {
            return fidelity != null && job.getToSeq() > job.getFromSeq()
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
        return jobs.findAllByOrderByCreatedAtDescIdDesc(Limit.of(limit));
    }

    /** One job, or 404. */
    public BacktestJobEntity get(String id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException(ErrorCodes.UNKNOWN_BACKTEST,
                "No such job: " + id));
    }

    /** Cancels a job. */
    @Transactional
    public BacktestJobEntity cancel(String id) {
        BacktestJobEntity job = get(id);
        if (List.of("queued", "running").contains(job.getStatus())) {
            job.setCancelled(true);
            job.setStatus("cancelled");
            job.setFinishedAt(clock.instant());
            jobs.save(job);
        }
        return job;
    }

    /** The maximum seq of the retained history (for range caps). */
    public long watermark() {
        return changeBus.lastPublishedSeq();
    }

    private static String hash(Map<String, Object> definition, JsonNode range) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest((String.valueOf(definition) + range).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** The worker pool, for tests. */
    ExecutorService executor() {
        return executor;
    }
}
