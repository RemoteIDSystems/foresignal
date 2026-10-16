package ai.foresignal.export;

import ai.foresignal.retention.RetentionPlanner;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.LeafInfo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * The cold archive's retention step (prompt 111): after {@link HoldCopyStep} and before the deletes
 * and drops of Core-Data-Model §13 steps 1–2, it writes exactly the rows this run is about to remove
 * ({@link RetentionPlanner}) as GeoParquet ({@link ArchiveService}): for each TRACK leaf about to be
 * dropped, the rows of each archived TRACK source in it; for each STANDARD scope about to be
 * deleted, its rows below the cutoff.
 *
 * <p>Every file is verified by reading its row count back before the delete or drop proceeds. A
 * failure keeps the rows: a STANDARD source is skipped this run, a TRACK leaf (and so every later
 * leaf) is kept; it alarms {@code foresignal.archive.failed}. Archived bytes count in
 * {@code foresignal.archive.bytes}.
 *
 * <p>Archived sources: the {@code archive.sources} allowlist, or every non-private registered source
 * when it is empty.
 */
@Component
public class ArchiveStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(ArchiveStep.class);

    /** Counter of archive failures. */
    public static final String FAILED_METRIC = "foresignal.archive.failed";

    /** Counter of archived bytes. */
    public static final String BYTES_METRIC = "foresignal.archive.bytes";

    private final ArchiveSettingsService settingsService;
    private final ArchiveService archive;
    private final RetentionPlanner planner;
    private final SourceRegistry registry;
    private final EgressPolicy egress;
    private final ExportLicenceService licenses;
    private final ArchiveState state;
    private final Counter failed;
    private final Counter bytesCounter;

    /** Creates the step. */
    public ArchiveStep(ArchiveSettingsService settingsService, ArchiveService archive, RetentionPlanner planner,
            SourceRegistry registry, EgressPolicy egress, ExportLicenceService licenses, ArchiveState state,
            MeterRegistry meters) {
        this.settingsService = settingsService;
        this.archive = archive;
        this.planner = planner;
        this.registry = registry;
        this.egress = egress;
        this.licenses = licenses;
        this.state = state;
        this.failed = Counter.builder(FAILED_METRIC).description("Cold archive writes that failed").register(meters);
        this.bytesCounter = Counter.builder(BYTES_METRIC).baseUnit("bytes")
                .description("Bytes written to the cold archive").register(meters);
    }

    @Override
    public String name() {
        return "cold-archive";
    }

    @Override
    public int order() {
        return 95;
    }

    @Override
    public void run(RetentionRun run) {
        ArchiveSettings settings = settingsService.get();
        if (!settings.enabled()) {
            return;
        }
        long[] totals = new long[2];
        archiveTrack(run, settings, totals);
        archiveStandard(run, settings, totals);
        state.record(run.now(), totals[0], (int) totals[1]);
        if (totals[0] > 0) {
            bytesCounter.increment(totals[0]);
            log.info("Cold archive wrote {} bytes", totals[0]);
        }
    }

    private void archiveTrack(RetentionRun run, ArchiveSettings settings, long[] totals) {
        List<SourceDescriptor> sources = archived(settings, HistoryTier.TRACK);
        if (sources.isEmpty()) {
            return;
        }
        for (LeafInfo leaf : planner.track(run).droppable()) {
            if (run.leafBlocked(leaf.name())) {
                return;
            }
            for (SourceDescriptor descriptor : sources) {
                try {
                    ArchiveService.Range range = new ArchiveService.Range(HistoryTier.TRACK, descriptor.id(), "*", "",
                            new MapSqlParameterSource(), leaf.fromSeq(), leaf.toSeq());
                    ArchiveService.Written written = archive.archive(settings, range,
                            licenses.archiveSummary(descriptor, 0));
                    totals[0] += written.bytes();
                } catch (RuntimeException e) {
                    log.warn("Cold archive of {} in track partition {} failed; keeping the partition this run",
                            descriptor.id(), leaf.name(), e);
                    run.blockLeaf(leaf.name());
                    run.error("cold archive " + descriptor.id() + " in " + leaf.name() + ": " + e.getMessage());
                    failed.increment();
                    totals[1]++;
                    return;
                }
            }
        }
    }

    private void archiveStandard(RetentionRun run, ArchiveSettings settings, long[] totals) {
        Map<String, List<RetentionPlanner.StandardScope>> plan = planner.standard(run).scopes();
        for (SourceDescriptor descriptor : archived(settings, HistoryTier.STANDARD)) {
            List<RetentionPlanner.StandardScope> scopes = plan.get(descriptor.id());
            if (scopes == null || run.skipped(descriptor.id())) {
                // Nothing planned, or an earlier step keeps the source's rows this run: writing them
                // now would only write them again once they are really deleted.
                continue;
            }
            try {
                for (RetentionPlanner.StandardScope scope : scopes) {
                    if (scope.cutoff() <= 0) {
                        continue;
                    }
                    ArchiveService.Range range = new ArchiveService.Range(HistoryTier.STANDARD, descriptor.id(),
                            scope.scopeKey(), scope.kindClause("v"), scope.kindParams(), 0L, scope.cutoff());
                    ArchiveService.Written written = archive.archive(settings, range,
                            licenses.archiveSummary(descriptor, 0));
                    totals[0] += written.bytes();
                }
            } catch (RuntimeException e) {
                log.warn("Cold archive of {} failed; keeping its versions this run", descriptor.id(), e);
                run.skip(descriptor.id());
                run.error("cold archive " + descriptor.id() + ": " + e.getMessage());
                failed.increment();
                totals[1]++;
            }
        }
    }

    /** The registered sources of {@code tier} the archive covers. */
    List<SourceDescriptor> archived(ArchiveSettings settings, HistoryTier tier) {
        List<SourceDescriptor> out = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.historyTier() != tier) {
                continue;
            }
            boolean listed = settings.sources().contains(descriptor.id());
            if (settings.sources().isEmpty() ? egress.isPrivate(descriptor.id()) : !listed) {
                continue;
            }
            out.add(descriptor);
        }
        return out;
    }

    /** The archive's run state, for the {@code archive} block of {@code /v1/system}. */
    @Component
    public static class ArchiveState {

        private volatile Instant lastRunAt;
        private volatile long lastRunBytes;
        private volatile int lastRunFailures;
        private volatile Instant lastFailureAt;
        private final AtomicLong bytes = new AtomicLong();
        private final AtomicLong failures = new AtomicLong();

        /** Records a run. */
        public void record(Instant at, long runBytes, int runFailures) {
            lastRunAt = at;
            lastRunBytes = runBytes;
            lastRunFailures = runFailures;
            bytes.addAndGet(runBytes);
            failures.addAndGet(runFailures);
            if (runFailures > 0) {
                lastFailureAt = at;
            }
        }

        /** The last run, or null before the first. */
        public Instant lastRunAt() {
            return lastRunAt;
        }

        /** Bytes written by the last run. */
        public long lastRunBytes() {
            return lastRunBytes;
        }

        /** Failures of the last run. */
        public int lastRunFailures() {
            return lastRunFailures;
        }

        /** The last run with a failure, or null. */
        public Instant lastFailureAt() {
            return lastFailureAt;
        }

        /** Bytes written since start. */
        public long bytes() {
            return bytes.get();
        }

        /** Failures since start. */
        public long failures() {
            return failures.get();
        }
    }
}
