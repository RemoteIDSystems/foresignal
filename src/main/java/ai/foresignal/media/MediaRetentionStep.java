package ai.foresignal.media;

import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Media retention (prompt 043, Weather-Imagery-Design §2.4 "Media: follows the owning record; map
 * attachments 7 d"), run by the hourly {@code RetentionJob} through prompt 019's {@link RetentionStep}
 * extension point, at order {@value #ORDER}: after the head purges, and before the raster step, whose
 * quota counts media bytes. Deliberately not {@code @Transactional}: a row is deleted first and its
 * file unlinked afterwards, only when no remaining row references the file's SHA-256.
 *
 * <ol>
 *   <li><b>Expired:</b> rows whose {@code expires_at} has passed (map attachments are stored with
 *       {@code now + 7d} by prompt 055).
 *   <li><b>Record-following</b> ({@code expires_at} null): an original is deleted when its
 *       {@code created_at} is older than the effective window of its owning record's (source, kind),
 *       resolved by 019's per-kind order ({@link RetentionResolver#window}); or when the owning
 *       record's head no longer exists and the row is older than
 *       {@code foresignal.media.head-missing-grace} (24 h).
 *   <li><b>Unowned</b> ({@code expires_at} null and no record): follows the producing source's window,
 *       or {@code retention.default} without a source.
 *   <li><b>Thumbnails</b> go with their original ({@code variant_of}); a variant whose original is gone
 *       is deleted.
 *   <li><b>Orphan sweep</b> (every run, so hourly): files with no row and rows with no file, both only
 *       once older than {@code foresignal.media.orphan-grace}, and stale staging files; counted in
 *       {@code foresignal.media.orphans{type=file|row}}.
 *   <li>Tombstones older than {@code foresignal.media.tombstone-retention} are pruned.
 * </ol>
 */
@Component
public class MediaRetentionStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(MediaRetentionStep.class);

    /** The table name reported in {@code foresignal.retention.deleted{table}}. */
    public static final String TABLE = "media";

    /** The orphan counter. */
    public static final String ORPHANS_METRIC = "foresignal.media.orphans";

    /** The step's position: after the head purges (400) and region sweep (460), before rasters (800). */
    public static final int ORDER = 780;

    /** Ids selected per delete round trip. */
    static final int CHUNK = 500;

    private static final Pattern FILE_NAME = Pattern.compile("[0-9a-f]{64}\\.[a-z]{3,4}");

    private final MediaQueries queries;
    private final MediaPaths paths;
    private final MediaLocks locks;
    private final RetentionResolver resolver;
    private final MediaProperties properties;
    private final MeterRegistry meters;

    /** Creates the step. */
    @org.springframework.beans.factory.annotation.Autowired
    public MediaRetentionStep(MediaQueries queries, MediaPaths paths, MediaLocks locks, RetentionResolver resolver,
            MediaProperties properties, ObjectProvider<MeterRegistry> meters) {
        this(queries, paths, locks, resolver, properties, meters.getIfAvailable());
    }

    /** Creates the step with an explicit registry (null for none). */
    public MediaRetentionStep(MediaQueries queries, MediaPaths paths, MediaLocks locks, RetentionResolver resolver,
            MediaProperties properties, MeterRegistry meters) {
        this.queries = queries;
        this.paths = paths;
        this.locks = locks;
        this.resolver = resolver;
        this.properties = properties;
        this.meters = meters;
    }

    @Override
    public String name() {
        return "media";
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void run(RetentionRun run) {
        Result result = apply(run.now());
        run.deleted(TABLE, result.rowsDeleted() + result.orphanRows());
        if (result.rowsDeleted() > 0 || result.orphanRows() > 0 || result.orphanFiles() > 0) {
            log.info("Media retention: {} rows deleted, {} files unlinked, {} orphan rows and {} orphan files removed",
                    result.rowsDeleted(), result.filesUnlinked(), result.orphanRows(), result.orphanFiles());
        }
    }

    /**
     * What one run did.
     *
     * @param rowsDeleted rows deleted by the expiry, record-following, unowned and variant rules
     * @param filesUnlinked files unlinked because no row references them any more
     * @param orphanRows rows deleted because their file was missing
     * @param orphanFiles files and stale staging files removed because no row references them
     */
    public record Result(long rowsDeleted, long filesUnlinked, long orphanRows, long orphanFiles) {}

    /** Applies every rule at {@code now}. */
    public Result apply(Instant now) {
        Counts counts = new Counts();
        deleteAll(() -> queries.expiredIds(now, CHUNK), now, counts);
        for (MediaQueries.RecordGroup group : queries.recordGroups()) {
            Duration window = resolver.window(group.sourceId(), group.kind());
            Instant cutoff = now.minus(window);
            deleteAll(() -> queries.recordFollowingIds(group, cutoff, CHUNK), now, counts);
        }
        Instant headCutoff = now.minus(properties.headMissingGrace());
        deleteAll(() -> queries.headMissingIds(headCutoff, CHUNK), now, counts);
        for (String sourceId : queries.unownedSources()) {
            Duration window = sourceId == null ? resolver.settings().defaultRetention() : resolver.window(sourceId, null);
            Instant cutoff = now.minus(window);
            deleteAll(() -> queries.unownedIds(sourceId, cutoff, CHUNK), now, counts);
        }
        deleteAll(() -> queries.orphanVariantIds(CHUNK), now, counts);
        long rowsDeleted = counts.rows;
        Instant graceCutoff = now.minus(properties.orphanGrace());
        long orphanRows = sweepRows(graceCutoff, now, counts);
        long orphanFiles = sweepFiles(graceCutoff);
        queries.pruneTombstones(now.minus(properties.tombstoneRetention()));
        return new Result(rowsDeleted, counts.unlinked, orphanRows, orphanFiles);
    }

    /** Running totals of one run. */
    private static final class Counts {
        long rows;
        long unlinked;
    }

    /** Deletes in chunks until {@code ids} returns fewer than a chunk. */
    private void deleteAll(Supplier<List<String>> ids, Instant now, Counts counts) {
        List<String> batch;
        do {
            batch = ids.get();
            if (batch.isEmpty()) {
                return;
            }
            List<MediaQueries.Deleted> deleted = queries.deleteWithVariants(batch, now);
            counts.rows += deleted.size();
            counts.unlinked += unlinkUnreferenced(deleted);
            if (deleted.isEmpty()) {
                return;
            }
        } while (batch.size() == CHUNK);
    }

    /** Unlinks each deleted row's file once no remaining row references its hash; returns how many. */
    long unlinkUnreferenced(List<MediaQueries.Deleted> deleted) {
        Map<String, String> files = new LinkedHashMap<>();
        deleted.forEach(d -> files.putIfAbsent(d.sha256(), d.path()));
        long unlinked = 0;
        for (Map.Entry<String, String> file : files.entrySet()) {
            boolean removed = locks.withLock(file.getKey(), () -> {
                if (queries.referenced(file.getKey())) {
                    return false;
                }
                try {
                    return Files.deleteIfExists(paths.resolve(file.getValue()));
                } catch (IOException | RuntimeException e) {
                    log.warn("Could not unlink media file {}; the orphan sweep retries", file.getValue(), e);
                    return false;
                }
            });
            if (removed) {
                unlinked++;
            }
        }
        return unlinked;
    }

    /** Deletes rows whose file is missing and that are older than the grace; returns how many. */
    long sweepRows(Instant graceCutoff, Instant now, Counts counts) {
        long removed = 0;
        String after = null;
        List<MediaRow> page;
        do {
            page = queries.page(after, 1000);
            List<String> missing = new ArrayList<>();
            for (MediaRow row : page) {
                after = row.id();
                if (row.createdAt().isBefore(graceCutoff) && !Files.exists(paths.resolve(row.path()))) {
                    missing.add(row.id());
                }
            }
            if (!missing.isEmpty()) {
                List<MediaQueries.Deleted> deleted = queries.deleteWithVariants(missing, now);
                counts.unlinked += unlinkUnreferenced(deleted);
                removed += deleted.size();
            }
        } while (page.size() == 1000);
        orphans("row", removed);
        return removed;
    }

    /** Removes content files no row references, and stale staging files; returns how many. */
    long sweepFiles(Instant graceCutoff) {
        long removed = 0;
        Path tmp = paths.tmp();
        if (Files.isDirectory(tmp)) {
            try (Stream<Path> staged = Files.list(tmp)) {
                for (Path file : staged.toList()) {
                    if (olderThan(file, graceCutoff) && Files.deleteIfExists(file)) {
                        removed++;
                    }
                }
            } catch (IOException e) {
                log.warn("Could not sweep {}", tmp, e);
            }
        }
        Path root = paths.root();
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path dir : dirs.filter(Files::isDirectory).toList()) {
                String prefix = dir.getFileName().toString();
                if (!prefix.matches("[0-9a-f]{2}")) {
                    continue;
                }
                Set<String> known = new HashSet<>(queries.hashesWithPrefix(prefix));
                try (Stream<Path> files = Files.list(dir)) {
                    for (Path file : files.filter(Files::isRegularFile).toList()) {
                        String name = file.getFileName().toString();
                        if (!FILE_NAME.matcher(name).matches()) {
                            continue;
                        }
                        String sha = name.substring(0, 64);
                        if (known.contains(sha) || !olderThan(file, graceCutoff)) {
                            continue;
                        }
                        boolean deleted = locks.withLock(sha, () -> {
                            if (queries.referenced(sha)) {
                                return false;
                            }
                            try {
                                return Files.deleteIfExists(file);
                            } catch (IOException e) {
                                log.warn("Could not remove orphan media file {}", file, e);
                                return false;
                            }
                        });
                        if (deleted) {
                            removed++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Could not sweep {}", root, e);
        }
        orphans("file", removed);
        return removed;
    }

    private void orphans(String type, long count) {
        if (meters != null && count > 0) {
            Counter.builder(ORPHANS_METRIC).tag("type", type).register(meters).increment(count);
        }
    }

    private static boolean olderThan(Path file, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(file).toInstant().isBefore(cutoff);
        } catch (IOException e) {
            return false;
        }
    }
}
