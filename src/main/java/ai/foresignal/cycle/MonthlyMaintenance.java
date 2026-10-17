package ai.foresignal.cycle;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.Ulids;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The monthly maintenance (prompt 128 §5): the monthly cycle (or {@code POST /v1/analysis/maintenance/run}) triggers,
 * in order, 116 observability re-estimation; the 123 historical benchmark suite; the 121 base-rate refresh; the 120
 * precursor re-test; the 122 Platt refits, ensemble weight refresh and extremizing check; the 126 index weight fits
 * and series-method re-selection; archival proposals for hypothesis sets past their horizon; and, when the profile's
 * provider has a {@code knowledgeCutoff}, the 122 retrospective evaluation of the {@code llm:*} forecasters over the
 * past month. Every outcome is recorded on the {@code maintenance_run}; a failing step never stops the later ones.
 * One maintenance runs at a time ({@code MAINTENANCE_IN_PROGRESS}).
 */
@Service
public class MonthlyMaintenance {

    private static final Logger log = LoggerFactory.getLogger(MonthlyMaintenance.class);

    /** The steps, in order. */
    public static final List<String> STEPS = List.of("observability-reestimation", "benchmark-historical",
            "base-rate-refresh", "precursor-retest", "platt-refit", "ensemble-weights", "extremizing-check",
            "index-weight-fit", "series-method-reselection", "hypothesis-archival-proposals",
            "llm-retrospective-evaluation");

    /** How long a running maintenance blocks another. */
    static final Duration STALE = Duration.ofHours(6);

    private final MaintenanceRunRepository runs;
    private final MaintenanceActions actions;
    private final Clock clock;

    /** Creates the service. */
    public MonthlyMaintenance(MaintenanceRunRepository runs, MaintenanceActions actions, Clock clock) {
        this.runs = runs;
        this.actions = actions;
        this.clock = clock;
    }

    /**
     * Starts a maintenance run: the row is created with every step {@code pending}.
     *
     * @param trigger {@code monthly-cycle}, {@code manual} or {@code schedule}
     * @param cycleRunId the monthly cycle run, or null
     * @throws ConflictException 409 {@code MAINTENANCE_IN_PROGRESS}
     */
    public synchronized MaintenanceRunEntity start(String trigger, String cycleRunId) {
        Instant now = clock.instant();
        runs.findTop1ByStatusOrderByIdDesc("running").ifPresent(running -> {
            if (running.getStartedAt().isAfter(now.minus(STALE))) {
                throw new ConflictException(ErrorCodes.MAINTENANCE_IN_PROGRESS, "Maintenance " + running.getId()
                        + " is still running.");
            }
            running.setStatus("failed");
            running.setFinishedAt(now);
            runs.save(running);
        });
        MaintenanceRunEntity run = new MaintenanceRunEntity(Ulids.next(now), trigger, now);
        run.setCycleRunId(cycleRunId);
        List<Map<String, Object>> steps = new ArrayList<>();
        for (String step : STEPS) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("step", step);
            s.put("status", "pending");
            s.put("startedAt", null);
            s.put("finishedAt", null);
            s.put("jobId", null);
            s.put("result", null);
            s.put("error", null);
            steps.add(s);
        }
        run.setSteps(steps);
        return runs.save(run);
    }

    /**
     * Runs every step of a started maintenance, in order, recording each outcome.
     *
     * @param id the maintenance run
     * @param providerId the provider whose {@code knowledgeCutoff} gates the retrospective evaluation, or null
     * @return the finished run
     */
    public MaintenanceRunEntity execute(String id, String providerId) {
        MaintenanceRunEntity run = get(id);
        List<Map<String, Object>> steps = new ArrayList<>();
        for (Map<String, Object> s : run.getSteps()) {
            steps.add(new LinkedHashMap<>(s));
        }
        int failed = 0;
        int done = 0;
        for (Map<String, Object> step : steps) {
            String name = String.valueOf(step.get("step"));
            Instant started = clock.instant();
            step.put("status", "running");
            step.put("startedAt", started.toString());
            run.setSteps(steps);
            runs.save(run);
            try {
                Map<String, Object> result = action(name, providerId).apply(started);
                if (result == null) {
                    step.put("status", "skipped");
                    step.put("error", "the provider has no knowledgeCutoff, so the LLM forecasters cannot be"
                            + " evaluated without leakage");
                } else {
                    step.put("status", "done");
                    step.put("result", result);
                    Object job = result.get("jobId");
                    step.put("jobId", job == null ? null : String.valueOf(job));
                    done++;
                }
            } catch (RuntimeException e) {
                failed++;
                step.put("status", "failed");
                step.put("error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
                log.warn("Maintenance step {} failed: {}", name, e.toString());
            }
            step.put("finishedAt", clock.instant().toString());
        }
        run.setSteps(steps);
        run.setStatus(failed == 0 ? "done" : done == 0 ? "failed" : "partial");
        run.setFinishedAt(clock.instant());
        return runs.save(run);
    }

    private Function<Instant, Map<String, Object>> action(String step, String providerId) {
        return switch (step) {
            case "observability-reestimation" -> actions::observabilityReestimation;
            case "benchmark-historical" -> actions::benchmarkHistorical;
            case "base-rate-refresh" -> actions::baseRateRefresh;
            case "precursor-retest" -> actions::precursorRetest;
            case "platt-refit" -> actions::plattRefit;
            case "ensemble-weights" -> actions::ensembleWeights;
            case "extremizing-check" -> actions::extremizingCheck;
            case "index-weight-fit" -> actions::indexWeightFit;
            case "series-method-reselection" -> actions::seriesMethodReselection;
            case "hypothesis-archival-proposals" -> actions::hypothesisArchivalProposals;
            default -> now -> actions.retrospectiveEvaluation(now, providerId);
        };
    }

    /**
     * A maintenance run.
     *
     * @throws NotFoundException 404 when unknown
     */
    public MaintenanceRunEntity get(String id) {
        return runs.findById(id == null ? "" : id).orElseThrow(() -> new NotFoundException(
                "No maintenance run '" + id + "'."));
    }

    /** The {@code MaintenanceRun} view. */
    public static Map<String, Object> view(MaintenanceRunEntity run) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", run.getId());
        out.put("trigger", run.getTrigger());
        out.put("status", run.getStatus());
        out.put("startedAt", run.getStartedAt().toString());
        out.put("finishedAt", run.getFinishedAt() == null ? null : run.getFinishedAt().toString());
        out.put("steps", run.getSteps());
        return out;
    }
}
