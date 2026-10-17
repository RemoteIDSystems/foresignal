package ai.foresignal.manifest;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.JobRunner;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Manifest replay (prompt 116, {@code POST /v1/manifests/{id}/replay}): queues a 103 job of type
 * {@value BacktestJobService#MANIFEST_REPLAY} that re-runs a deterministic component at the manifest's
 * {@code asOfSeq} with its recorded configuration and inputs and reports {@code identical}, {@code differs} (with
 * a diff summary) or {@code not-replayable} ({@code inputs-purged}, {@code component-removed} or
 * {@code llm-output}). An LLM manifest reports "inputs recorded; model output not reproducible". A manifest
 * whose component no replayer knows is refused up front with {@code 409 MANIFEST_NOT_REPLAYABLE}.
 */
@Service
public class ManifestReplayService implements JobRunner {

    private final ManifestService manifests;
    private final ObjectProvider<BacktestJobService> jobs;
    private final ObjectProvider<ManifestReplayer> replayers;
    private final Clock clock;

    /** Creates the service. */
    public ManifestReplayService(ManifestService manifests, ObjectProvider<BacktestJobService> jobs,
            ObjectProvider<ManifestReplayer> replayers, Clock clock) {
        this.manifests = manifests;
        this.jobs = jobs;
        this.replayers = replayers;
        this.clock = clock;
    }

    @Override
    public String type() {
        return BacktestJobService.MANIFEST_REPLAY;
    }

    /**
     * Queues a replay.
     *
     * @param tokenId the caller's token id (the one-job-per-token rule), or null
     * @return the {@code ReplayResult} of the queued job
     * @throws ai.foresignal.api.error.NotFoundException 404 {@code UNKNOWN_MANIFEST}
     * @throws ConflictException 409 {@code MANIFEST_NOT_REPLAYABLE} when no replayer knows the component
     */
    public Map<String, Object> submit(String manifestId, String tokenId) {
        ManifestService.Manifest manifest = manifests.get(manifestId);
        if (manifest.model() == null && replayer(manifest.component()).isEmpty()) {
            throw new ConflictException(ErrorCodes.MANIFEST_NOT_REPLAYABLE, "The component " + manifest.component()
                    + " of manifest " + manifestId + " cannot be replayed (" + ReplayOutcome.COMPONENT_REMOVED + ").");
        }
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("manifestId", manifestId);
        definition.put("component", manifest.component());
        definition.put("componentVersion", manifest.componentVersion());
        BacktestJobEntity job = jobs.getObject().submitRunnerJob(type(), manifestId, definition, tokenId);
        return view(job.getId(), manifestId, "queued", null, null, null);
    }

    /** Runs a claimed replay job. */
    @Override
    public Map<String, Object> run(BacktestJobEntity job, JobMonitor monitor) {
        java.time.Instant started = clock.instant();
        ReplayOutcome outcome = replay(job.getTargetId());
        Map<String, Object> out = view(job.getId(), job.getTargetId(), "done", outcome, started, clock.instant());
        return out;
    }

    /** Replays a manifest synchronously (the job body; tests). */
    public ReplayOutcome replay(String manifestId) {
        Optional<ManifestService.Manifest> found = manifests.find(manifestId);
        if (found.isEmpty()) {
            return ReplayOutcome.notReplayable(ReplayOutcome.COMPONENT_REMOVED, "The manifest no longer exists.");
        }
        ManifestService.Manifest manifest = found.get();
        if (manifest.model() != null) {
            return ReplayOutcome.notReplayable(ReplayOutcome.LLM_OUTPUT, ReplayOutcome.LLM_MESSAGE);
        }
        Optional<ManifestReplayer> replayer = replayer(manifest.component());
        if (replayer.isEmpty()) {
            return ReplayOutcome.notReplayable(ReplayOutcome.COMPONENT_REMOVED,
                    "No replayer exists for component " + manifest.component() + ".");
        }
        try {
            return replayer.get().replay(manifest);
        } catch (RuntimeException e) {
            return ReplayOutcome.notReplayable(null, "The replay failed: " + e.getMessage());
        }
    }

    private Optional<ManifestReplayer> replayer(String component) {
        return replayers.orderedStream().filter(r -> r.supports(component)).findFirst();
    }

    /** The {@code ReplayResult} shape. */
    static Map<String, Object> view(String jobId, String manifestId, String status, ReplayOutcome outcome,
            java.time.Instant startedAt, java.time.Instant finishedAt) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("jobId", jobId);
        out.put("manifestId", manifestId);
        out.put("status", status);
        out.put("outcome", outcome == null ? null : outcome.outcome());
        out.put("reason", outcome == null ? null : outcome.reason());
        out.put("diffSummary", outcome == null ? null : outcome.diffSummary());
        out.put("message", outcome == null ? null : outcome.message());
        out.put("startedAt", startedAt);
        out.put("finishedAt", finishedAt);
        return out;
    }

    /** The components with a registered replayer, for docs and tests. */
    public List<String> replayable(List<String> components) {
        return components.stream().filter(c -> replayer(c).isPresent()).toList();
    }
}
