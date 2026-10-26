package ai.foresignal.backup;

import ai.foresignal.export.ArchiveRef;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * One row of {@code backup_run} (prompt R2-017): a backup, its files on the target, and what became of it.
 *
 * @param id the ULID
 * @param type {@code database}, {@code config} or {@code full}
 * @param status {@code pending}, {@code running}, {@code complete}, {@code failed}, {@code restoring} or
 *     {@code restored}
 * @param trigger {@code manual} ({@code runBackup}) or {@code scheduled}
 * @param label the caller's label, or null
 * @param target {@code local}, {@code external} or {@code s3}
 * @param root the directory a disk backup was written under, or null for {@code s3}
 * @param location the directory or {@code s3://} prefix holding the files, or null before they are stored
 * @param sizeBytes the files' total size, or null before they are stored
 * @param checksum the SHA-256 of the one file, or of the sorted {@code key:sha256} lines when there are two
 * @param files the stored files
 * @param error why the backup or the last restore failed, or null
 * @param createdAt when it was asked for
 * @param updatedAt when the row last changed
 * @param startedAt when the worker began it
 * @param finishedAt when it completed or failed
 * @param restoredAt when it was last restored
 */
public record BackupRun(String id, String type, String status, String trigger, String label, String target,
        String root, String location, Long sizeBytes, String checksum, List<BackupFile> files, String error,
        Instant createdAt, Instant updatedAt, Instant startedAt, Instant finishedAt, Instant restoredAt) {

    /** A pg_dump of the database. */
    public static final String DATABASE = "database";

    /** The portable config bundle ({@code exportSettings}). */
    public static final String CONFIG = "config";

    /** Both. */
    public static final String FULL = "full";

    /** The types. */
    public static final List<String> TYPES = List.of(DATABASE, CONFIG, FULL);

    /** Asked for, not started. */
    public static final String PENDING = "pending";

    /** Being written. */
    public static final String RUNNING = "running";

    /** Stored and verified on the target. */
    public static final String COMPLETE = "complete";

    /** Not stored. */
    public static final String FAILED = "failed";

    /** Being restored. */
    public static final String RESTORING = "restoring";

    /** Restored at least once. */
    public static final String RESTORED = "restored";

    /** The statuses. */
    public static final List<String> STATUSES = List.of(PENDING, RUNNING, COMPLETE, FAILED, RESTORING, RESTORED);

    /** The statuses of a backup whose files are stored and can be restored. */
    public static final Set<String> STORED = Set.of(COMPLETE, RESTORED);

    /** A {@code runBackup}. */
    public static final String MANUAL = "manual";

    /** A run started by {@code backup.schedule}. */
    public static final String SCHEDULED = "scheduled";

    /** The file name of the database dump. */
    public static final String DATABASE_FILE = "database.dump";

    /** The file name of the config bundle. */
    public static final String CONFIG_FILE = "config.json";

    /** Fills defaults. */
    public BackupRun {
        files = files == null ? List.of() : List.copyOf(files);
    }

    /**
     * One stored file.
     *
     * @param name {@value #DATABASE_FILE} or {@value #CONFIG_FILE}
     * @param key its key on the target ({@code backups/{id}/{name}})
     * @param location its absolute path or {@code s3://} URL
     * @param sha256 its SHA-256, hex
     * @param bytes its size
     * @param etag its ETag on {@code s3}, or null
     */
    public record BackupFile(String name, String key, String location, String sha256, long bytes, String etag) {

        /** The tier reference of this file on the archive tier {@code tierTarget}. */
        public ArchiveRef ref(String tierTarget) {
            return new ArchiveRef(tierTarget, key, location, sha256, bytes, etag);
        }
    }

    /** True while a worker holds it. */
    public boolean busy() {
        return PENDING.equals(status) || RUNNING.equals(status) || RESTORING.equals(status);
    }

    /** True when {@link #DATABASE_FILE} is among the files to write. */
    public boolean hasDatabase() {
        return DATABASE.equals(type) || FULL.equals(type);
    }

    /** True when {@link #CONFIG_FILE} is among the files to write. */
    public boolean hasConfig() {
        return CONFIG.equals(type) || FULL.equals(type);
    }

    /** The file named {@code name}, or null. */
    public BackupFile file(String name) {
        return files.stream().filter(f -> f.name().equals(name)).findFirst().orElse(null);
    }

    /** This run with {@code status}, {@code error} and {@code updatedAt}. */
    public BackupRun withStatus(String newStatus, String newError, Instant at) {
        return new BackupRun(id, type, newStatus, trigger, label, target, root, location, sizeBytes, checksum, files,
                newError, createdAt, at, startedAt, finishedAt, restoredAt);
    }

    /** This run started at {@code at}. */
    public BackupRun started(Instant at) {
        return new BackupRun(id, type, RUNNING, trigger, label, target, root, location, sizeBytes, checksum, files,
                null, createdAt, at, at, finishedAt, restoredAt);
    }

    /** This run complete with its stored files. */
    public BackupRun completed(String newLocation, long size, String sum, List<BackupFile> stored, Instant at) {
        return new BackupRun(id, type, COMPLETE, trigger, label, target, root, newLocation, size, sum, stored, null,
                createdAt, at, startedAt, at, restoredAt);
    }

    /** This run failed at {@code at}. */
    public BackupRun failed(String why, Instant at) {
        return new BackupRun(id, type, FAILED, trigger, label, target, root, location, sizeBytes, checksum, files,
                why, createdAt, at, startedAt, at, restoredAt);
    }

    /** This run restored at {@code at}. */
    public BackupRun restored(Instant at) {
        return new BackupRun(id, type, RESTORED, trigger, label, target, root, location, sizeBytes, checksum, files,
                null, createdAt, at, startedAt, finishedAt, at);
    }
}
