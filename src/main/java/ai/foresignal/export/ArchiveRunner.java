package ai.foresignal.export;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionCapPruner;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionPlanner;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.system.OperationalAlertService;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

/**
 * Cold-storage archive runs (prompt R2-016): history older than a cutoff leaves the hot database for an
 * {@link ArchiveTier}, and only then is it purged.
 *
 * <p>A run, whether asked for ({@code runArchive}), scheduled ({@code archive.schedule}) or started by the retention
 * size cap (prompt R2-015, through {@link ArchiveCapPruner}):
 *
 * <ol>
 *   <li><b>Plans</b> a retention pass and does not run it yet. With a cutoff, the STANDARD windows of the archived
 *       sources are capped to {@code now − cutoff} for this pass only ({@link RetentionRun#capSources}); no other
 *       source's window moves. {@link RetentionPlanner} keeps the plan on the run.
 *   <li><b>Exports</b> exactly the rows that plan would remove from the archived sources ({@link ArchiveStep#archived}
 *       decides which): the STANDARD scopes below their cutoffs and the rows in each TRACK partition about to be
 *       dropped, one gzip NDJSON file per source ({@link ArchiveSegmentFiles}).
 *   <li><b>Offloads and verifies</b> every file on the tier: the external disk reads the SHA-256 back, object storage
 *       checks the ETag and size. Any failure marks the segment {@code failed}, raises {@code system.archive_failed}
 *       (prompt R2-014) and stops: nothing is purged.
 *   <li><b>Records</b> the manifest ({@code complete}).
 *   <li><b>Purges</b> by running every retention step on the same planned run, so the deletes and partition drops
 *       are exactly the planned ones and go through the existing paths (StandardHistoryStep, TrackPartitionStep,
 *       HeadPurgeStep: I6 and I7 hold). It then counts what is still there: none left is {@code purged}; rows a step
 *       kept back (the consumer clamp, a hold, a skipped source) leave the segment {@code complete} with a note.
 * </ol>
 *
 * <p>A manual or scheduled run holds the retention job's single-run guard ({@link RetentionJob#lease}) from the
 * request to the end of the purge, so it never overlaps a retention run or another archive run
 * ({@code 409 ALREADY_RUNNING}). A size-cap run happens inside the retention run that started it.
 *
 * <p>{@link #restore} brings a segment back: each file is pulled from its tier (a Glacier object is thawed first, which
 * takes hours; the tick asks again each minute), its SHA-256 checked, and its rows loaded into the restore area
 * {@code record_version_restored}. A restore is admin-gated and may be large and slow.
 */
@Component
public class ArchiveRunner {

    private static final Logger log = LoggerFactory.getLogger(ArchiveRunner.class);

    /** The operational alert key of archive runs and restores. */
    static final String ALERT = "archive";

    /** {@code 409}: the archive is off or its tier is not usable. */
    public static final String NOT_CONFIGURED = "ARCHIVE_NOT_CONFIGURED";

    /** {@code 404}: no such segment. */
    public static final String UNKNOWN_ARCHIVE = "UNKNOWN_ARCHIVE";

    /** {@code 409}: the segment was restored already. */
    public static final String ALREADY_RESTORED = "ALREADY_RESTORED";

    /** {@code 409}: the segment failed, so it holds nothing to restore. */
    public static final String NOT_RESTORABLE = "ARCHIVE_NOT_RESTORABLE";

    /** A run asked for through {@code runArchive}. */
    public static final String MANUAL = "manual";

    /** A run started by {@code archive.schedule}. */
    public static final String SCHEDULED = "scheduled";

    /** A run started by the retention size cap. */
    public static final String CAP = "cap";

    private final ArchiveSettingsService settingsService;
    private final ArchiveStep archiveStep;
    private final RetentionJob job;
    private final RetentionResolver resolver;
    private final RetentionPlanner planner;
    private final ArchiveSegmentFiles files;
    private final ArchiveTiers tiers;
    private final ArchiveManifestStore manifests;
    private final ArchiveService archive;
    private final ExportStore store;
    private final ObjectProvider<OperationalAlertService> alerts;
    private final Clock clock;
    private final boolean scheduled;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            Thread.ofVirtual().name("archive-run").factory());
    private final AtomicBoolean restoring = new AtomicBoolean();
    private volatile Instant scheduleCheckedAt;

    /** Creates the runner. */
    public ArchiveRunner(ArchiveSettingsService settingsService, ArchiveStep archiveStep, RetentionJob job,
            RetentionResolver resolver, RetentionPlanner planner, ArchiveSegmentFiles files, ArchiveTiers tiers,
            ArchiveManifestStore manifests, ArchiveService archive, ExportStore store,
            ObjectProvider<OperationalAlertService> alerts,
            Clock clock, @Value("${foresignal.archive.scheduled:true}") boolean scheduled) {
        this.settingsService = settingsService;
        this.archiveStep = archiveStep;
        this.job = job;
        this.resolver = resolver;
        this.planner = planner;
        this.files = files;
        this.tiers = tiers;
        this.manifests = manifests;
        this.archive = archive;
        this.store = store;
        this.alerts = alerts;
        this.clock = clock;
        this.scheduled = scheduled;
        this.scheduleCheckedAt = clock.instant();
    }

    /**
     * A {@code runArchive} body.
     *
     * @param cutoff archive history older than this instead of {@code archive.cutoff}, or null
     * @param sources only these sources (each must be archived), or empty for every archived source
     * @param dryRun count what would be archived; export and purge nothing
     */
    public record Request(Instant cutoff, List<String> sources, boolean dryRun) {

        /** Fills defaults. */
        public Request {
            sources = sources == null ? List.of() : List.copyOf(sources);
        }
    }

    // =====================================================================================
    // Runs
    // =====================================================================================

    /**
     * Starts a run on the worker and returns its segment ({@code pending}); a dry run returns its counts at once and
     * stores nothing.
     *
     * @throws ConflictException {@code ARCHIVE_NOT_CONFIGURED}, or {@code ALREADY_RUNNING} while retention or another
     *     archive run is active
     * @throws ValidationException for a cutoff not in the past or a source the archive does not cover
     */
    public ArchiveSegment start(Request request, String trigger) {
        ArchiveSettings settings = requireConfigured();
        Instant now = clock.instant();
        Instant cutoff = cutoff(request, settings, now);
        Set<String> sources = sources(request, settings);
        if (request.dryRun()) {
            return dryRun(settings, cutoff, sources, now);
        }
        RetentionJob.Lease lease = job.lease();
        try {
            ArchiveSegment segment = newSegment(trigger, settings, cutoff, now);
            manifests.save(segment);
            worker.submit(() -> {
                try (lease) {
                    RetentionRun run = lease.plan(clock.instant(), resolver.settings(),
                            caps(settings, sources, cutoff, now));
                    execute(segment, settings, sources, run, lease::execute, false);
                } catch (RuntimeException e) {
                    log.error("Archive run {} failed", segment.id(), e);
                    fail(manifests.find(segment.id()).orElse(segment), "The run failed: " + e.getMessage());
                }
            });
            return segment;
        } catch (RuntimeException e) {
            lease.close();
            throw e;
        }
    }

    /**
     * The retention size cap's archive-then-purge (prompt R2-015's {@link RetentionCapPruner} seam): runs inside the
     * retention run that holds the guard.
     *
     * @param tightened the cap's tightened settings
     * @param passes the retention run's passes
     * @return the purge pass, or null when the archive failed and nothing was purged
     */
    public RetentionRun archiveThenPurge(RetentionSettings tightened, RetentionCapPruner.Passes passes) {
        ArchiveSettings settings = settingsService.get();
        Set<String> sources = sources(new Request(null, List.of(), false), settings);
        ArchiveSegment segment = newSegment(CAP, settings, null, clock.instant());
        manifests.save(segment);
        RetentionRun run = passes.plan(tightened);
        return execute(segment, settings, sources, run, passes::execute, true);
    }

    /**
     * True when an archive tier is configured, so the size cap should archive before it purges.
     *
     * @return whether {@link #archiveThenPurge} applies
     */
    public boolean configured() {
        return usable(settingsService.get()) == null;
    }

    private ArchiveSegment dryRun(ArchiveSettings settings, Instant cutoff, Set<String> sources, Instant now) {
        RetentionRun run = job.scratch(now, resolver.settings());
        run.capSources(caps(settings, sources, cutoff, now));
        long rows = 0;
        Set<String> touched = new LinkedHashSet<>();
        for (Selection selection : select(run, settings, sources)) {
            long n = files.count(selection.tier(), selection.sourceId(), selection.ranges());
            if (n > 0) {
                rows += n;
                touched.add(selection.sourceId());
            }
        }
        ArchiveSegment base = newSegment(MANUAL, settings, cutoff, now);
        return new ArchiveSegment(base.id(), ArchiveSegment.PENDING, MANUAL, base.domain(), base.target(),
                base.location(), cutoff, List.copyOf(touched), null, null, null, null, null, rows, null, List.of(),
                false, "Dry run: nothing was exported or purged, and this segment was not stored.", now, now, null,
                null, null);
    }

    /**
     * Steps 2–5 of a run.
     *
     * @param alwaysPurge purge even when nothing was archived (the size cap must still prune the other sources)
     * @return the purge pass, or null when nothing was purged
     */
    private RetentionRun execute(ArchiveSegment segment, ArchiveSettings settings, Set<String> sources,
            RetentionRun run, UnaryOperator<RetentionRun> purge, boolean alwaysPurge) {
        ArchiveSegment current = save(segment.withStatus(ArchiveSegment.RUNNING, null, clock.instant()));
        Path staging = store.resolve("archive-staging/" + segment.id());
        List<Exported> exported = new ArrayList<>();
        try {
            for (Selection selection : select(run, settings, sources)) {
                String name = selection.tier().wireValue() + "-" + selection.sourceId() + ".ndjson.gz";
                ArchiveSegmentFiles.Written written = files.write(staging.resolve(name), selection.tier(),
                        selection.sourceId(), selection.ranges());
                if (written != null) {
                    exported.add(new Exported(selection, name, written));
                }
            }
        } catch (RuntimeException e) {
            log.warn("Archive run {} could not export; nothing is purged", segment.id(), e);
            deleteQuietly(staging);
            fail(current, "The export failed: " + e.getMessage());
            return null;
        }
        if (exported.isEmpty()) {
            deleteQuietly(staging);
            current = save(new ArchiveSegment(current.id(), ArchiveSegment.COMPLETE, current.trigger(),
                    current.domain(), current.target(), current.location(), current.cutoff(), List.of(), null, null,
                    null, null, 0L, 0L, checksum(List.of()), List.of(), false,
                    "Nothing in the archived sources was older than the cutoff.", current.createdAt(), clock.instant(),
                    clock.instant(), null, null));
            recovered();
            return alwaysPurge ? purge.apply(run) : null;
        }
        current = save(current.withStatus(ArchiveSegment.VERIFYING, null, clock.instant()));
        ArchiveTier tier = tiers.forTarget(settings.target());
        List<ArchiveSegment.SegmentFile> stored = new ArrayList<>();
        try {
            for (Exported file : exported) {
                String key = "segments/" + segment.id() + "/" + file.name();
                ArchiveRef ref = tier.offload(settings, new ArchiveSpec(key, file.written().file(),
                        file.written().sha256(), file.written().bytes()));
                if (!tier.verify(settings, ref)) {
                    throw new IllegalStateException("The stored copy of " + key + " does not verify");
                }
                stored.add(new ArchiveSegment.SegmentFile(file.name(), key, file.selection().sourceId(),
                        file.selection().tier().wireValue(), file.written().fromSeq(), file.written().toSeq(),
                        file.written().fromTime(), file.written().toTime(), file.written().rows(),
                        file.written().bytes(), file.written().sha256(), ref.location(), ref.etag()));
            }
        } catch (RuntimeException e) {
            log.warn("Archive run {} could not store a verified copy; nothing is purged", segment.id(), e);
            fail(current, "Verification failed: " + e.getMessage());
            return null;
        } finally {
            deleteQuietly(staging);
        }
        Instant completed = clock.instant();
        current = save(new ArchiveSegment(current.id(), ArchiveSegment.COMPLETE, current.trigger(), current.domain(),
                current.target(), current.location(), current.cutoff(),
                stored.stream().map(ArchiveSegment.SegmentFile::sourceId).distinct().toList(),
                stored.stream().mapToLong(ArchiveSegment.SegmentFile::fromSeq).min().orElseThrow(),
                stored.stream().mapToLong(ArchiveSegment.SegmentFile::toSeq).max().orElseThrow(),
                stored.stream().map(ArchiveSegment.SegmentFile::fromTime).min(Comparator.naturalOrder()).orElse(null),
                stored.stream().map(ArchiveSegment.SegmentFile::toTime).max(Comparator.naturalOrder()).orElse(null),
                stored.stream().mapToLong(ArchiveSegment.SegmentFile::bytes).sum(),
                stored.stream().mapToLong(ArchiveSegment.SegmentFile::rows).sum(), checksum(stored), stored, false,
                null, current.createdAt(), completed, completed, null, null));
        recovered();
        RetentionRun purged = purge.apply(run);
        long left = 0;
        for (Exported file : exported) {
            left += files.count(file.selection().tier(), file.selection().sourceId(), file.selection().ranges());
        }
        Instant now = clock.instant();
        save(new ArchiveSegment(current.id(), left == 0 ? ArchiveSegment.PURGED : ArchiveSegment.COMPLETE,
                current.trigger(), current.domain(), current.target(), current.location(), current.cutoff(),
                current.sources(), current.fromSeq(), current.toSeq(), current.fromTime(), current.toTime(),
                current.sizeBytes(), current.itemCount(), current.checksum(), current.files(), left == 0,
                left == 0 ? null : left + " archived rows are still in the hot store: the purge pass kept them back"
                        + " (the consumer clamp, a hold or a skipped source). They stay until a later run removes"
                        + " them.", current.createdAt(), now, current.completedAt(), now, null));
        return purged;
    }

    /** One source's rows the planned run removes. */
    private record Selection(HistoryTier tier, String sourceId, List<ArchiveSegmentFiles.Range> ranges) {}

    /** One written file. */
    private record Exported(Selection selection, String name, ArchiveSegmentFiles.Written written) {}

    /** What {@code run}'s plan removes from the archived {@code sources}. */
    private List<Selection> select(RetentionRun run, ArchiveSettings settings, Set<String> sources) {
        List<Selection> out = new ArrayList<>();
        Map<String, List<RetentionPlanner.StandardScope>> plan = planner.standard(run).scopes();
        for (SourceDescriptor descriptor : archiveStep.archived(settings, HistoryTier.STANDARD)) {
            List<RetentionPlanner.StandardScope> scopes = plan.get(descriptor.id());
            if (!sources.contains(descriptor.id()) || scopes == null || run.skipped(descriptor.id())) {
                continue;
            }
            List<ArchiveSegmentFiles.Range> ranges = new ArrayList<>();
            for (RetentionPlanner.StandardScope scope : scopes) {
                if (scope.cutoff() > 0) {
                    ranges.add(new ArchiveSegmentFiles.Range(scope.kindClause("v"), scope.kindParams(), 0L,
                            scope.cutoff()));
                }
            }
            if (!ranges.isEmpty()) {
                out.add(new Selection(HistoryTier.STANDARD, descriptor.id(), ranges));
            }
        }
        List<LeafInfo> leaves = new ArrayList<>();
        for (LeafInfo leaf : planner.track(run).droppable()) {
            if (run.leafBlocked(leaf.name())) {
                break;
            }
            leaves.add(leaf);
        }
        if (!leaves.isEmpty()) {
            for (SourceDescriptor descriptor : archiveStep.archived(settings, HistoryTier.TRACK)) {
                if (!sources.contains(descriptor.id())) {
                    continue;
                }
                List<ArchiveSegmentFiles.Range> ranges = leaves.stream()
                        .map(leaf -> new ArchiveSegmentFiles.Range("", new org.springframework.jdbc.core.namedparam
                                .MapSqlParameterSource(), leaf.fromSeq(), leaf.toSeq()))
                        .toList();
                out.add(new Selection(HistoryTier.TRACK, descriptor.id(), ranges));
            }
        }
        return out;
    }

    /** The run-only STANDARD caps: the archived sources' windows capped to {@code now − cutoff}. */
    private Map<String, Duration> caps(ArchiveSettings settings, Set<String> sources, Instant cutoff, Instant now) {
        if (cutoff == null) {
            return Map.of();
        }
        Duration window = Duration.between(cutoff, now);
        Map<String, Duration> caps = new LinkedHashMap<>();
        for (SourceDescriptor descriptor : archiveStep.archived(settings, HistoryTier.STANDARD)) {
            if (sources.contains(descriptor.id())) {
                caps.put(descriptor.id(), window);
            }
        }
        return caps;
    }

    // =====================================================================================
    // Restore
    // =====================================================================================

    /**
     * Starts restoring segment {@code id} on the worker.
     *
     * @return the segment, now {@code restoring}
     * @throws NotFoundException {@code UNKNOWN_ARCHIVE}
     * @throws ConflictException {@code ALREADY_RESTORED}, {@code ALREADY_RUNNING} while it or another restore is
     *     being worked on, {@code ARCHIVE_NOT_RESTORABLE} for a failed segment, {@code ARCHIVE_NOT_CONFIGURED}
     */
    public ArchiveSegment restore(String id) {
        ArchiveSegment segment = manifests.find(id)
                .orElseThrow(() -> new NotFoundException(UNKNOWN_ARCHIVE, "No archive segment has the id " + id + "."));
        if (ArchiveSegment.RESTORED.equals(segment.status())) {
            throw new ConflictException(ALREADY_RESTORED, "Archive segment " + id + " was restored at "
                    + segment.restoredAt() + ".");
        }
        if (segment.busy()) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "Archive segment " + id + " is "
                    + segment.status() + ".");
        }
        if (ArchiveSegment.FAILED.equals(segment.status())) {
            throw new ConflictException(NOT_RESTORABLE, "Archive segment " + id + " failed, so it holds nothing to"
                    + " restore.");
        }
        ArchiveSettings settings = settingsService.get();
        if (segment.files().isEmpty()) {
            return save(segment.withStatus(ArchiveSegment.RESTORED, null, clock.instant()));
        }
        if (segment.target().equals(ArchiveSettings.S3_TARGET) || segment.target().equals(ArchiveSettings.GLACIER)) {
            if (settings.s3() == null || !settings.hasSecrets()) {
                throw new ConflictException(NOT_CONFIGURED, "Restoring from " + segment.target() + " needs the s3"
                        + " settings and secrets.");
            }
        }
        if (!restoring.compareAndSet(false, true)) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "Another restore is running.");
        }
        try {
            ArchiveSegment started = save(segment.withStatus(ArchiveSegment.RESTORING, null, clock.instant()));
            worker.submit(() -> {
                try {
                    continueRestore(started, settings);
                } finally {
                    restoring.set(false);
                }
            });
            return started;
        } catch (RuntimeException e) {
            restoring.set(false);
            throw e;
        }
    }

    /** Pulls every file back; loads them once all are ready, else leaves the segment {@code restoring}. */
    void continueRestore(ArchiveSegment segment, ArchiveSettings settings) {
        ArchiveTier tier = tiers.forTarget(segment.target());
        Path dir = store.resolve("archive-restore/" + segment.id());
        try {
            List<Path> ready = new ArrayList<>();
            List<String> waiting = new ArrayList<>();
            for (ArchiveSegment.SegmentFile file : segment.files()) {
                ArchiveTier.Restored restored = tier.restore(settings, file.ref(segment.target()),
                        dir.resolve(file.name()));
                if (restored.ready()) {
                    ready.add(restored.file());
                } else {
                    waiting.add(restored.detail());
                }
            }
            if (!waiting.isEmpty()) {
                save(segment.withStatus(ArchiveSegment.RESTORING, String.join("; ", waiting), clock.instant()));
                return;
            }
            long rows = 0;
            for (int k = 0; k < segment.files().size(); k++) {
                ArchiveSegment.SegmentFile file = segment.files().get(k);
                HistoryTier historyTier = HistoryTier.valueOf(file.tier().toUpperCase(java.util.Locale.ROOT));
                rows += files.load(segment.id(), file.sourceId(), historyTier, ready.get(k));
            }
            Instant now = clock.instant();
            save(new ArchiveSegment(segment.id(), ArchiveSegment.RESTORED, segment.trigger(), segment.domain(),
                    segment.target(), segment.location(), segment.cutoff(), segment.sources(), segment.fromSeq(),
                    segment.toSeq(), segment.fromTime(), segment.toTime(), segment.sizeBytes(), segment.itemCount(),
                    segment.checksum(), segment.files(), segment.purgedFromHot(),
                    rows + " rows are in the restore area (record_version_restored).", segment.createdAt(), now,
                    segment.completedAt(), segment.purgedAt(), now));
            deleteQuietly(dir);
            recovered();
        } catch (RuntimeException e) {
            log.warn("The restore of archive segment {} failed", segment.id(), e);
            String back = segment.purgedFromHot() ? ArchiveSegment.PURGED : ArchiveSegment.COMPLETE;
            ArchiveSegment reverted = save(segment.withStatus(back, "The restore failed: " + e.getMessage(),
                    clock.instant()));
            alarm(reverted, "The restore of archive segment " + segment.id() + " failed: " + e.getMessage());
        }
    }

    // =====================================================================================
    // Schedule
    // =====================================================================================

    /**
     * Each minute: starts a scheduled run when {@code archive.schedule} has fired since the last check, and asks
     * again for segments still {@code restoring} (a Glacier thaw). Off when {@code foresignal.archive.scheduled} is
     * false.
     */
    @Scheduled(fixedDelayString = "${foresignal.archive.tick:60s}", initialDelayString = "${foresignal.archive.tick:60s}")
    public void tick() {
        if (!scheduled) {
            return;
        }
        try {
            runSchedule();
            resumeRestores();
        } catch (RuntimeException e) {
            log.warn("The archive tick failed", e);
        }
    }

    /** Starts a scheduled run when one is due; a run blocked by retention is tried again on the next tick. */
    void runSchedule() {
        ArchiveSettings settings = settingsService.get();
        Instant now = clock.instant();
        if (!settings.enabled() || settings.schedule() == null) {
            scheduleCheckedAt = now;
            return;
        }
        ZonedDateTime next = CronExpression.parse(settings.schedule())
                .next(ZonedDateTime.ofInstant(scheduleCheckedAt, ZoneOffset.UTC));
        if (next == null || next.toInstant().isAfter(now)) {
            return;
        }
        try {
            start(new Request(null, List.of(), false), SCHEDULED);
            scheduleCheckedAt = now;
        } catch (ConflictException e) {
            log.info("The scheduled archive run waits for the next tick: {}", e.getMessage());
        }
    }

    /** Asks again for every segment still {@code restoring}, one at a time. */
    void resumeRestores() {
        for (ArchiveSegment segment : manifests.withStatus(ArchiveSegment.RESTORING)) {
            if (!restoring.compareAndSet(false, true)) {
                return;
            }
            try {
                continueRestore(segment, settingsService.get());
            } finally {
                restoring.set(false);
            }
        }
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================

    private ArchiveSettings requireConfigured() {
        ArchiveSettings settings = settingsService.get();
        String why = usable(settings);
        if (why != null) {
            throw new ConflictException(NOT_CONFIGURED, why);
        }
        return settings;
    }

    /** Why the archive cannot run, or null when it can. */
    private static String usable(ArchiveSettings settings) {
        if (!settings.enabled()) {
            return "The archive is not enabled (PUT /v1/settings/archive with enabled: true).";
        }
        if (settings.objectStorage() && (settings.s3() == null || !settings.hasSecrets())) {
            return "The " + settings.target() + " target needs its s3 settings and secrets.";
        }
        return null;
    }

    private static Instant cutoff(Request request, ArchiveSettings settings, Instant now) {
        Instant cutoff = request.cutoff();
        if (cutoff == null && settings.cutoff() != null) {
            cutoff = now.minus(Durations.parse(settings.cutoff()));
        }
        if (cutoff != null && !cutoff.isBefore(now)) {
            throw ValidationException.forField("cutoff", "must be in the past");
        }
        return cutoff;
    }

    /** The requested sources, each of which the archive must cover; every archived source when none is named. */
    private Set<String> sources(Request request, ArchiveSettings settings) {
        Set<String> archived = new LinkedHashSet<>();
        archiveStep.archived(settings, HistoryTier.STANDARD).forEach(d -> archived.add(d.id()));
        archiveStep.archived(settings, HistoryTier.TRACK).forEach(d -> archived.add(d.id()));
        if (request.sources().isEmpty()) {
            return archived;
        }
        List<FieldError> errors = new ArrayList<>();
        for (String source : request.sources()) {
            if (!archived.contains(source)) {
                errors.add(new FieldError("sources", source + " is not a source the archive covers"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The archive run request is not valid.", errors);
        }
        return new LinkedHashSet<>(request.sources());
    }

    private ArchiveSegment newSegment(String trigger, ArchiveSettings settings, Instant cutoff, Instant now) {
        String id = Ulids.next(now);
        return new ArchiveSegment(id, ArchiveSegment.PENDING, trigger, "record_version", settings.target(),
                location(settings, id), cutoff, List.of(), null, null, null, null, null, null, null, List.of(), false,
                null, now, now, null, null, null);
    }

    private String location(ArchiveSettings settings, String id) {
        if (settings.objectStorage() && settings.s3() != null) {
            String prefix = settings.s3().prefix() == null || settings.s3().prefix().isBlank() ? ""
                    : settings.s3().prefix().replaceAll("^/+|/+$", "") + "/";
            return "s3://" + settings.s3().bucket() + "/" + prefix + "segments/" + id;
        }
        return archive.root(settings).resolve("segments").resolve(id).toString();
    }

    /** SHA-256 over each file's {@code key:sha256} line, in key order. */
    static String checksum(List<ArchiveSegment.SegmentFile> stored) {
        StringBuilder lines = new StringBuilder();
        stored.stream().sorted(Comparator.comparing(ArchiveSegment.SegmentFile::key))
                .forEach(f -> lines.append(f.key()).append(':').append(f.sha256()).append('\n'));
        return ExportManifests.sha256(lines.toString().getBytes(StandardCharsets.UTF_8));
    }

    private ArchiveSegment save(ArchiveSegment segment) {
        manifests.save(segment);
        return segment;
    }

    private void fail(ArchiveSegment segment, String why) {
        ArchiveSegment failed = save(segment.withStatus(ArchiveSegment.FAILED, why, clock.instant()));
        alarm(failed, "Archive segment " + segment.id() + " failed: " + why + " Nothing was purged.");
    }

    private void alarm(ArchiveSegment segment, String body) {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        path.report(ALERT, "warn", (level, previous) -> new OperationalAlertService.Alert("system.archive_failed",
                "warning", "The cold-storage archive failed", body, "/v1/archives", data(segment)));
    }

    private void recovered() {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        path.report(ALERT, "ok", (level, previous) -> new OperationalAlertService.Alert("system.archive_recovered",
                "info", "The cold-storage archive is working again", "The last archive run or restore succeeded.",
                "/v1/archives", Map.of()));
    }

    private static Map<String, Object> data(ArchiveSegment segment) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("archiveId", segment.id());
        data.put("status", segment.status());
        data.put("target", segment.target());
        data.put("trigger", segment.trigger());
        return data;
    }

    private static void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (var walk = Files.walk(dir)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Could not delete {}", dir, e);
        }
    }

    /** Stops the worker. */
    @PreDestroy
    public void shutdown() {
        worker.shutdown();
    }
}
