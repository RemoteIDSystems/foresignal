package ai.foresignal.backtest;

import ai.foresignal.manifest.ManifestReplayCheck;
import ai.foresignal.manifest.ManifestReplayer;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.manifest.ReplayOutcome;
import ai.foresignal.retention.RetentionState;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Replays a backtest job from its manifest (prompt 116): the finished job that carries the manifest is re-run
 * with its stored definition and range (writing nothing) and the results are compared. A rule job whose range
 * starts below the retained history of its sources is {@code not-replayable} ({@code inputs-purged}).
 */
@Component
public class BacktestReplayer implements ManifestReplayer {

    private final ObjectProvider<BacktestJobService> jobs;
    private final ObjectProvider<RetentionState> retention;

    /** Creates the replayer. */
    public BacktestReplayer(ObjectProvider<BacktestJobService> jobs, ObjectProvider<RetentionState> retention) {
        this.jobs = jobs;
        this.retention = retention;
    }

    @Override
    public boolean supports(String component) {
        return component != null && component.startsWith(BacktestJobService.MANIFEST_COMPONENT_PREFIX)
                && !component.endsWith(BacktestJobService.MANIFEST_REPLAY);
    }

    @Override
    public ReplayOutcome replay(ManifestService.Manifest manifest) {
        BacktestJobService service = jobs.getObject();
        Optional<BacktestJobEntity> found = service.byManifest(manifest.id());
        if (found.isEmpty()) {
            return ReplayOutcome.notReplayable(null, "No backtest job carries this manifest.");
        }
        BacktestJobEntity job = found.get();
        if (!"done".equals(job.getStatus())) {
            return ReplayOutcome.notReplayable(null, "The job is " + job.getStatus() + ", so there is no output to compare.");
        }
        Set<String> sources = sources(job);
        RetentionState state = retention.getIfAvailable();
        if (state != null && !sources.isEmpty() && job.getFromSeq() != null
                && state.lowestRetainedSeq(sources) > job.getFromSeq() + 1) {
            return ReplayOutcome.notReplayable(ReplayOutcome.INPUTS_PURGED,
                    "Retention removed versions of " + sources + " inside the job's range.");
        }
        Map<String, Object> replayed = service.rerun(job, JobMonitor.NONE);
        return ManifestReplayCheck.compare(withoutVolatile(job.getResult()), withoutVolatile(replayed));
    }

    @SuppressWarnings("unchecked")
    private static Set<String> sources(BacktestJobEntity job) {
        Set<String> out = new LinkedHashSet<>();
        Object rule = job.getDefinition() == null ? null : job.getDefinition().get("rule");
        if (rule instanceof Map<?, ?> map && map.get("sources") instanceof Collection<?> list) {
            for (Object item : list) {
                out.add(String.valueOf(item));
            }
        }
        return out;
    }

    /** The result without the members added after the run ({@code caveats} for precursor jobs). */
    private static Map<String, Object> withoutVolatile(Map<String, Object> result) {
        Map<String, Object> out = result == null ? new LinkedHashMap<>() : new LinkedHashMap<>(result);
        out.remove("caveats");
        return out;
    }
}
