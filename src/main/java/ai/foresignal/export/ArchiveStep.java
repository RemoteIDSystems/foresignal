package ai.foresignal.export;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Retention step 0.5 (prompt 111): write records as GeoParquet to the cold archive before retention
 * deletes or drops them. Runs after {@link HoldCopyStep} and before the deletes
 * ({@code StandardHistoryStep}, {@code TrackPartitionStep}). A source whose write fails is skipped
 * this run and alarms {@code foresignal.archive.failed}.
 */
@Component
public class ArchiveStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(ArchiveStep.class);

    /** Counter of archive failures. */
    public static final String FAILED_METRIC = "foresignal.archive.failed";

    /** Gauge of archived bytes. */
    public static final String BYTES_METRIC = "foresignal.archive.bytes";

    private final ArchiveSettingsService settingsService;
    private final ArchiveService archive;
    private final SourceRegistry registry;
    private final LicenceCatalog licences;
    private final ArchiveState state;
    private final MeterRegistry meters;

    /** Creates the step. */
    public ArchiveStep(ArchiveSettingsService settingsService, ArchiveService archive, SourceRegistry registry,
            LicenceCatalog licences, ArchiveState state, MeterRegistry meters) {
        this.settingsService = settingsService;
        this.archive = archive;
        this.registry = registry;
        this.licences = licences;
        this.state = state;
        this.meters = meters;
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
        Duration standardWindow = run.largestStandardWindow();
        long standardCutoff = run.rawCutoff(standardWindow);
        long trackCutoff = run.rawCutoff(run.settings().trackWindow());
        List<SourceDescriptor> sources = allowed(settings);
        long bytes = 0;
        int failures = 0;
        for (SourceDescriptor descriptor : sources) {
            long cutoff = descriptor.isTrack() ? trackCutoff : standardCutoff;
            String licence = licences.forDescriptor(descriptor).id();
            try {
                List<ArchiveService.Partition> partitions =
                        archive.archive(descriptor.id(), descriptor.historyTier(), cutoff, settings, licence);
                for (ArchiveService.Partition partition : partitions) {
                    bytes += partition.files().stream().mapToLong(ArchiveService.ArchivedFile::bytes).sum();
                }
            } catch (RuntimeException e) {
                log.warn("Cold archive of {} failed; skipping its sources this run", descriptor.id(), e);
                run.skip(descriptor.id());
                run.error("cold archive " + descriptor.id() + ": " + e.getMessage());
                meters.counter(FAILED_METRIC).increment();
                failures++;
            }
        }
        state.record(run.now(), bytes, failures);
        state.addBytes(bytes);
        if (bytes > 0) {
            log.info("Cold archive wrote {} bytes for {} sources", bytes, sources.size());
        }
    }

    private List<SourceDescriptor> allowed(ArchiveSettings settings) {
        List<SourceDescriptor> out = new ArrayList<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.privateSource()) {
                continue;
            }
            if (!settings.sources().isEmpty() && !settings.sources().contains(descriptor.id())) {
                continue;
            }
            out.add(descriptor);
        }
        return out;
    }

    /** In-memory archive state for {@code /v1/system}. */
    @Component
    public static class ArchiveState {

        private volatile Instant lastRunAt;
        private volatile int failures;
        private final AtomicLong bytes = new AtomicLong();

        /** Records a run. */
        void record(Instant at, long runBytes, int runFailures) {
            this.lastRunAt = at;
            this.failures = runFailures;
        }

        /** Adds archived bytes. */
        void addBytes(long runBytes) {
            bytes.addAndGet(runBytes);
        }

        /** The last run time, or null. */
        public Instant lastRunAt() {
            return lastRunAt;
        }

        /** Failures of the last run. */
        public int failures() {
            return failures;
        }

        /** Total bytes written since start. */
        public long bytes() {
            return bytes.get();
        }
    }
}
