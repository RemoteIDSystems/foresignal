package ai.foresignal.backup;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.export.ArchiveRef;
import ai.foresignal.export.ArchiveSettings;
import ai.foresignal.export.ArchiveSpec;
import ai.foresignal.export.ArchiveTier;
import ai.foresignal.export.ArchiveTiers;
import ai.foresignal.export.ExportManifests;
import ai.foresignal.export.ExportStore;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingsTransferService;
import ai.foresignal.system.OperationalAlertService;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Database backups, config bundles and their restore (prompt R2-017).
 *
 * <p><strong>A backup</strong> ({@code runBackup}, or {@code backup.schedule}) is recorded as a {@code pending}
 * {@code backup_run} row and written on a single worker thread:
 * <ol>
 *   <li>{@code database}: {@code pg_dump --format=custom} of the application database, using the datasource's host,
 *       port, database and user. The password goes to the subprocess as {@code PGPASSWORD} and is never logged or
 *       put on its command line;
 *   <li>{@code config}: the portable settings bundle of {@code exportSettings}
 *       ({@link SettingsTransferService#export()}), which carries no credential or secret;
 *   <li>each file is placed on the target through the cold-archive tiers of prompt R2-016 ({@code local} and
 *       {@code external}: the external-disk tier, copied, fsynced and its SHA-256 read back; {@code s3}: uploaded and
 *       checked by ETag and size) under {@code backups/{id}/};
 *   <li>the row is marked {@code complete} with its files, size and checksum, and complete backups of the same type
 *       beyond {@code keepBackups} are deleted with their files.
 * </ol>
 * Any failure marks the row {@code failed} with the reason and raises the operational alert
 * {@code system.backup_failed} (prompt R2-014); the next success raises {@code system.backup_recovered}. When
 * {@code pg_dump} is not on the {@code PATH}, a database backup fails with that reason and the application carries on.
 *
 * <p><strong>A restore</strong> needs {@code confirm: true}. A {@code config} backup is imported at once through
 * {@link SettingsTransferService#importBundle} (atomic: every error, nothing applied). A {@code database} or
 * {@code full} backup is restored with {@code pg_restore --clean --if-exists --single-transaction} into the live
 * database on the worker, under the retention job's single-run guard so no retention or archive pass runs meanwhile.
 * It replaces every table, this history included; afterwards Flyway brings the schema forward, the settings cache is
 * dropped, and the backup's own row is written back as {@code restored}. Writes made while it runs (ingest, API calls)
 * wait on its locks or are lost with the data they changed, so restore with collection paused and restart afterwards.
 *
 * <p>One backup or restore at a time: a second one is {@code 409 ALREADY_RUNNING}.
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    /** The key of the backup condition in {@link OperationalAlertService}. */
    static final String ALERT = "backup";

    /** {@code runBackup} while backups are off. */
    public static final String NOT_CONFIGURED = "BACKUP_NOT_CONFIGURED";

    /** {@code restoreBackup} of an unknown id. */
    public static final String UNKNOWN_BACKUP = "UNKNOWN_BACKUP";

    /** {@code restoreBackup} of a backup that has no stored files. */
    public static final String NOT_RESTORABLE = "BACKUP_NOT_RESTORABLE";

    /** The most process output kept for an error message. */
    static final int OUTPUT_TAIL = 2_000;

    private final BackupSettingsService settingsService;
    private final BackupRunStore runs;
    private final ArchiveTiers tiers;
    private final ExportStore store;
    private final SettingsTransferService transfer;
    private final RetentionJob job;
    private final AppSettingService appSettings;
    private final ObjectProvider<JdbcConnectionDetails> connection;
    private final ObjectProvider<Flyway> flyway;
    private final ObjectProvider<OperationalAlertService> alerts;
    private final JsonMapper json;
    private final Clock clock;
    private final boolean scheduled;
    private final Duration timeout;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(
            Thread.ofVirtual().name("backup-run").factory());
    private final AtomicBoolean busy = new AtomicBoolean();
    private volatile String pgDump;
    private volatile String pgRestore;
    private volatile Instant scheduleCheckedAt;

    /** Creates the service. */
    public BackupService(BackupSettingsService settingsService, BackupRunStore runs, ArchiveTiers tiers,
            ExportStore store, SettingsTransferService transfer, RetentionJob job, AppSettingService appSettings,
            ObjectProvider<JdbcConnectionDetails> connection, ObjectProvider<Flyway> flyway,
            ObjectProvider<OperationalAlertService> alerts, JsonMapper json, Clock clock,
            @Value("${foresignal.backup.scheduled:true}") boolean scheduled,
            @Value("${foresignal.backup.pg-dump:pg_dump}") String pgDump,
            @Value("${foresignal.backup.pg-restore:pg_restore}") String pgRestore,
            @Value("${foresignal.backup.timeout:6h}") Duration timeout) {
        this.settingsService = settingsService;
        this.runs = runs;
        this.tiers = tiers;
        this.store = store;
        this.transfer = transfer;
        this.job = job;
        this.appSettings = appSettings;
        this.connection = connection;
        this.flyway = flyway;
        this.alerts = alerts;
        this.json = json;
        this.clock = clock;
        this.scheduled = scheduled;
        this.pgDump = pgDump;
        this.pgRestore = pgRestore;
        this.timeout = timeout;
        this.scheduleCheckedAt = clock.instant();
    }

    /**
     * The outcome of {@link #restore}.
     *
     * @param run the backup, {@code restoring} (a database restore under way) or {@code restored} (a config import)
     * @param importReport what a config import applied, or null
     */
    public record Restore(BackupRun run, SettingsTransferService.ImportReport importReport) {}

    // =====================================================================================
    // Backup
    // =====================================================================================

    /**
     * Starts a backup on the worker.
     *
     * @param type {@code database}, {@code config} or {@code full}; null means {@code full} when
     *     {@code includeConfig} is on and {@code database} otherwise
     * @param label a label, or null
     * @param trigger {@link BackupRun#MANUAL} or {@link BackupRun#SCHEDULED}
     * @return the {@code pending} row
     * @throws ConflictException {@code BACKUP_NOT_CONFIGURED} while backups are off or the target lacks its settings,
     *     {@code ALREADY_RUNNING} while another backup or restore runs
     */
    public BackupRun start(String type, String label, String trigger) {
        BackupSettings settings = settingsService.get();
        requireConfigured(settings);
        String kind = type != null ? type : settings.includeConfig() ? BackupRun.FULL : BackupRun.DATABASE;
        if (!busy.compareAndSet(false, true)) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "A backup or restore is already running.");
        }
        try {
            Instant now = clock.instant();
            BackupRun run = new BackupRun(Ulids.next(now), kind, BackupRun.PENDING, trigger, label, settings.target(),
                    settings.disk() ? diskRoot(settings).toString() : null, null, null, null, List.of(), null, now, now,
                    null, null, null);
            runs.save(run);
            worker.execute(() -> {
                try {
                    write(run, settings);
                } finally {
                    busy.set(false);
                }
            });
            return run;
        } catch (RuntimeException e) {
            busy.set(false);
            throw e;
        }
    }

    /** True while a backup or restore runs. */
    public boolean running() {
        return busy.get();
    }

    private void write(BackupRun pending, BackupSettings settings) {
        BackupRun run = save(pending.started(clock.instant()));
        Path staging = store.resolve("backup-staging/" + run.id());
        try {
            Files.createDirectories(staging);
            List<Path> files = new ArrayList<>();
            if (run.hasDatabase()) {
                files.add(dump(staging.resolve(BackupRun.DATABASE_FILE)));
            }
            if (run.hasConfig()) {
                Path bundle = staging.resolve(BackupRun.CONFIG_FILE);
                Files.write(bundle, json.writerWithDefaultPrettyPrinter().writeValueAsBytes(transfer.export()));
                files.add(bundle);
            }
            ArchiveSettings tierSettings = settings.tierSettings(run.root());
            ArchiveTier tier = tiers.forTarget(tierSettings.target());
            List<BackupRun.BackupFile> stored = new ArrayList<>();
            long size = 0;
            for (Path file : files) {
                String name = file.getFileName().toString();
                String key = "backups/" + run.id() + "/" + name;
                long bytes = Files.size(file);
                ArchiveRef ref = tier.offload(tierSettings, new ArchiveSpec(key, file, ExportManifests.sha256(file),
                        bytes));
                stored.add(new BackupRun.BackupFile(name, key, ref.location(), ref.sha256(), ref.bytes(), ref.etag()));
                size += bytes;
            }
            String location = stored.isEmpty() ? null : parent(stored.getFirst().location());
            run = save(run.completed(location, size, checksum(stored), stored, clock.instant()));
            log.info("Backup {} ({}) is complete: {} bytes at {}", run.id(), run.type(), size, location);
            recovered();
            prune(run.type(), settings);
        } catch (Exception e) {
            String why = reason(e);
            log.warn("Backup {} failed: {}", run.id(), why);
            BackupRun failed = save(run.failed(why, clock.instant()));
            alarm(failed, "Backup " + run.id() + " (" + run.type() + ") failed: " + why);
        } finally {
            ExportStore.deleteTree(staging);
        }
    }

    /** {@code pg_dump --format=custom} of the application database into {@code into}. */
    private Path dump(Path into) throws IOException, InterruptedException {
        Path command = onPath(pgDump);
        if (command == null) {
            throw new IllegalStateException(pgDump + " was not found on the PATH; install the PostgreSQL client"
                    + " (the same major version as the server or newer) to take database backups");
        }
        Connection db = connection();
        List<String> args = new ArrayList<>(List.of(command.toString(), "--format=custom", "--no-owner",
                "--no-privileges", "--file", into.toString()));
        args.addAll(db.args());
        exec(args, db.password(), "pg_dump");
        if (!Files.isRegularFile(into) || Files.size(into) == 0) {
            throw new IllegalStateException("pg_dump exited cleanly but wrote no dump");
        }
        return into;
    }

    /** Deletes complete backups of {@code type} beyond {@code keepBackups}, and failed ones older than those kept. */
    void prune(String type, BackupSettings settings) {
        Integer keep = settings.keepBackups();
        if (keep == null) {
            return;
        }
        List<BackupRun> all = runs.ofType(type);
        List<BackupRun> stored = all.stream().filter(r -> BackupRun.STORED.contains(r.status())).toList();
        if (stored.size() <= keep) {
            return;
        }
        Instant oldestKept = stored.get(keep - 1).createdAt();
        for (BackupRun run : stored.subList(keep, stored.size())) {
            try {
                deleteFiles(run, settings);
                runs.delete(run.id());
                log.info("Backup {} pruned (keepBackups {})", run.id(), keep);
            } catch (RuntimeException e) {
                log.warn("Could not prune backup {}; it is kept: {}", run.id(), reason(e));
            }
        }
        all.stream().filter(r -> BackupRun.FAILED.equals(r.status()) && r.createdAt().isBefore(oldestKept))
                .forEach(r -> runs.delete(r.id()));
    }

    private void deleteFiles(BackupRun run, BackupSettings settings) {
        ArchiveSettings tierSettings = tierSettings(run, settings);
        ArchiveTier tier = tiers.forTarget(tierSettings.target());
        for (BackupRun.BackupFile file : run.files()) {
            tier.delete(tierSettings, file.ref(tierSettings.target()));
        }
        if (run.root() != null) {
            try {
                Files.deleteIfExists(Path.of(run.root()).resolve("backups").resolve(run.id()));
            } catch (IOException e) {
                log.debug("Backup directory of {} not removed: {}", run.id(), e.getMessage());
            }
        }
    }

    // =====================================================================================
    // Restore
    // =====================================================================================

    /**
     * Restores a backup. A {@code config} backup is imported now; a {@code database} or {@code full} backup is
     * restored into the live database on the worker.
     *
     * @throws NotFoundException {@code UNKNOWN_BACKUP}
     * @throws ConflictException {@code ALREADY_RUNNING} while a backup, restore, retention or archive pass runs;
     *     {@code BACKUP_NOT_RESTORABLE} for a backup without stored files, or a config bundle whose stored copy is
     *     missing or does not match its checksum
     */
    public Restore restore(String id) {
        BackupRun run = runs.find(id)
                .orElseThrow(() -> new NotFoundException(UNKNOWN_BACKUP, "No backup has the id " + id + "."));
        if (!BackupRun.STORED.contains(run.status())) {
            if (run.busy()) {
                throw new ConflictException(ConflictException.ALREADY_RUNNING, "Backup " + id + " is " + run.status()
                        + ".");
            }
            throw new ConflictException(NOT_RESTORABLE, "Backup " + id + " failed and has nothing to restore.");
        }
        BackupSettings settings = settingsService.get();
        if (BackupSettings.S3_TARGET.equals(run.target()) && (settings.s3() == null || !settings.hasSecrets())) {
            throw new ConflictException(NOT_CONFIGURED, "Backup " + id + " is on S3, and the backup settings have no"
                    + " bucket or secrets to read it with.");
        }
        if (!busy.compareAndSet(false, true)) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "A backup or restore is already running.");
        }
        try {
            if (BackupRun.CONFIG.equals(run.type())) {
                try {
                    return importConfig(run, settings);
                } finally {
                    busy.set(false);
                }
            }
            RetentionJob.Lease lease = job.lease();
            BackupRun restoring;
            try {
                restoring = save(run.withStatus(BackupRun.RESTORING, null, clock.instant()));
                worker.execute(() -> {
                    try (lease) {
                        restoreDatabase(restoring, run.status(), settings);
                    } finally {
                        busy.set(false);
                    }
                });
            } catch (RuntimeException e) {
                lease.close();
                throw e;
            }
            return new Restore(restoring, null);
        } catch (RuntimeException e) {
            if (!BackupRun.CONFIG.equals(run.type())) {
                busy.set(false);
            }
            throw e;
        }
    }

    private Restore importConfig(BackupRun run, BackupSettings settings) {
        Path into = store.resolve("backup-restore/" + run.id());
        try {
            JsonNode node;
            try {
                node = json.readTree(Files.readAllBytes(fetch(run, BackupRun.CONFIG_FILE, settings, into)));
            } catch (IOException | RuntimeException e) {
                String why = reason(e);
                BackupRun noted = save(run.withStatus(run.status(), "The restore failed: " + why, clock.instant()));
                alarm(noted, "The restore of backup " + run.id() + " failed: " + why);
                throw new ConflictException(NOT_RESTORABLE, "Backup " + run.id() + " cannot be read: " + why);
            }
            SettingsTransferService.ImportReport report = transfer.importBundle(node);
            BackupRun restored = save(run.restored(clock.instant()));
            log.info("Config backup {} imported", run.id());
            return new Restore(restored, report);
        } finally {
            ExportStore.deleteTree(into);
        }
    }

    private void restoreDatabase(BackupRun run, String previous, BackupSettings settings) {
        Path into = store.resolve("backup-restore/" + run.id());
        try {
            Path dump = fetch(run, BackupRun.DATABASE_FILE, settings, into);
            Path command = onPath(pgRestore);
            if (command == null) {
                throw new IllegalStateException(pgRestore + " was not found on the PATH; install the PostgreSQL client"
                        + " to restore database backups");
            }
            Connection db = connection();
            List<String> args = new ArrayList<>(List.of(command.toString(), "--clean", "--if-exists", "--no-owner",
                    "--no-privileges", "--single-transaction", "--exit-on-error"));
            args.addAll(db.args());
            args.add(dump.toString());
            exec(args, db.password(), "pg_restore");
            Flyway migrations = flyway.getIfAvailable();
            if (migrations != null) {
                migrations.migrate();
            }
            appSettings.invalidateCache();
            // The restore replaced backup_run with the dump's copy, in which this backup was still running.
            save(run.restored(clock.instant()));
            log.warn("The database was restored from backup {}; restart the server so every cache is rebuilt",
                    run.id());
            recovered();
        } catch (Exception e) {
            String why = reason(e);
            log.warn("The restore of backup {} failed: {}", run.id(), why);
            BackupRun reverted = save(run.withStatus(previous, "The restore failed: " + why, clock.instant()));
            alarm(reverted, "The restore of backup " + run.id() + " failed: " + why);
        } finally {
            ExportStore.deleteTree(into);
        }
    }

    /** Brings file {@code name} of {@code run} back from its target into {@code dir}, checking its SHA-256. */
    private Path fetch(BackupRun run, String name, BackupSettings settings, Path dir) throws IOException {
        BackupRun.BackupFile file = run.file(name);
        if (file == null) {
            throw new IllegalStateException("Backup " + run.id() + " has no " + name);
        }
        ArchiveSettings tierSettings = tierSettings(run, settings);
        Files.createDirectories(dir);
        ArchiveTier.Restored restored = tiers.forTarget(tierSettings.target())
                .restore(tierSettings, file.ref(tierSettings.target()), dir.resolve(name));
        if (!restored.ready()) {
            throw new IllegalStateException("Backup file " + file.key() + " is not readable yet: " + restored.detail());
        }
        return restored.file();
    }

    // =====================================================================================
    // Schedule
    // =====================================================================================

    /** Each minute: starts a scheduled backup when {@code backup.schedule} has fired since the last check. */
    @Scheduled(fixedDelayString = "${foresignal.backup.tick:60s}", initialDelayString = "${foresignal.backup.tick:60s}")
    public void tick() {
        if (!scheduled) {
            return;
        }
        try {
            runSchedule();
        } catch (RuntimeException e) {
            log.warn("The backup tick failed", e);
        }
    }

    /** Starts a scheduled backup when one is due; one blocked by a running backup is tried on the next tick. */
    void runSchedule() {
        BackupSettings settings = settingsService.get();
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
            start(null, null, BackupRun.SCHEDULED);
            scheduleCheckedAt = now;
        } catch (ConflictException e) {
            if (NOT_CONFIGURED.equals(e.getCode())) {
                scheduleCheckedAt = now;
                alarm(null, "The scheduled backup did not run: " + e.getMessage());
            } else {
                log.info("The scheduled backup waits for the next tick: {}", e.getMessage());
            }
        }
    }

    /** Stops the worker. */
    @PreDestroy
    public void shutdown() {
        worker.shutdown();
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================

    /**
     * Throws unless backups can run with {@code settings}.
     *
     * @throws ConflictException {@code BACKUP_NOT_CONFIGURED}
     */
    void requireConfigured(BackupSettings settings) {
        if (!settings.enabled()) {
            throw new ConflictException(NOT_CONFIGURED, "Backups are off; enable them with updateBackupSettings.");
        }
        if (BackupSettings.S3_TARGET.equals(settings.target())
                && (settings.s3() == null || settings.s3().bucket() == null || !settings.hasSecrets())) {
            throw new ConflictException(NOT_CONFIGURED, "The s3 backup target needs a bucket, an access key and a"
                    + " secret key.");
        }
        if (BackupSettings.EXTERNAL.equals(settings.target()) && settings.root().isEmpty()) {
            throw new ConflictException(NOT_CONFIGURED, "The external backup target needs a root.");
        }
    }

    /** The absolute directory disk backups of {@code settings} are written under. */
    Path diskRoot(BackupSettings settings) {
        if (settings.root().isEmpty()) {
            return store.dataDir().toAbsolutePath().normalize();
        }
        Path root = Path.of(settings.root());
        return root.isAbsolute() ? root.normalize() : store.resolve(settings.root()).toAbsolutePath().normalize();
    }

    /** The tier settings that reach {@code run}'s files: its own root on disk, the current bucket on S3. */
    private ArchiveSettings tierSettings(BackupRun run, BackupSettings settings) {
        BackupSettings at = new BackupSettings(true, null, run.target(), run.root(), settings.s3(), null, false,
                settings.accessKeyCipher(), settings.accessKeyNonce(), settings.secretKeyCipher(),
                settings.secretKeyNonce());
        return at.tierSettings(run.root());
    }

    /**
     * The checksum of a backup: the one file's SHA-256, or the SHA-256 of the {@code key:sha256} lines sorted by key.
     */
    static String checksum(List<BackupRun.BackupFile> files) {
        if (files.isEmpty()) {
            return null;
        }
        if (files.size() == 1) {
            return files.getFirst().sha256();
        }
        StringBuilder lines = new StringBuilder();
        files.stream().sorted(Comparator.comparing(BackupRun.BackupFile::key))
                .forEach(f -> lines.append(f.key()).append(':').append(f.sha256()).append('\n'));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(lines.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The directory or prefix of a file location. */
    static String parent(String location) {
        int slash = location.lastIndexOf('/');
        return slash > 0 ? location.substring(0, slash) : location;
    }

    /** {@code command} resolved on the {@code PATH} (or as a path), or null when it is not there. */
    static Path onPath(String command) {
        if (command == null || command.isBlank()) {
            return null;
        }
        if (command.contains("/")) {
            Path path = Path.of(command);
            return Files.isRegularFile(path) && Files.isExecutable(path) ? path : null;
        }
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        for (String dir : path.split(File.pathSeparator)) {
            if (dir.isBlank()) {
                continue;
            }
            Path candidate = Path.of(dir, command);
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * The libpq connection of the datasource.
     *
     * @param host the host
     * @param port the port
     * @param database the database name
     * @param user the user
     * @param password the password, or null
     */
    record Connection(String host, int port, String database, String user, String password) {

        /** The connection options of {@code pg_dump} and {@code pg_restore}; never the password. */
        List<String> args() {
            List<String> out = new ArrayList<>(List.of("--host", host, "--port", Integer.toString(port), "--dbname",
                    database));
            if (user != null && !user.isBlank()) {
                out.add("--username");
                out.add(user);
            }
            return out;
        }

        /** Parses a {@code jdbc:postgresql://host[:port]/database[?…]} URL. */
        static Connection parse(String jdbcUrl, String user, String password) {
            if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql://")) {
                throw new IllegalStateException("The datasource is not a jdbc:postgresql:// URL");
            }
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (uri.getHost() == null || path.length() < 2) {
                throw new IllegalStateException("The datasource URL names no host or database");
            }
            return new Connection(uri.getHost(), uri.getPort() < 0 ? 5432 : uri.getPort(), path.substring(1), user,
                    password);
        }
    }

    private Connection connection() {
        JdbcConnectionDetails details = connection.getIfAvailable();
        if (details == null) {
            throw new IllegalStateException("No JdbcConnectionDetails bean: the database connection is unknown");
        }
        return Connection.parse(details.getJdbcUrl(), details.getUsername(), details.getPassword());
    }

    /** Runs {@code args} with {@code PGPASSWORD}; throws with the tail of its output when it fails. */
    private void exec(List<String> args, String password, String name) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(args).redirectErrorStream(true);
        if (password != null) {
            builder.environment().put("PGPASSWORD", password);
        }
        Process process = builder.start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thread reader = Thread.ofVirtual().start(() -> {
            try (InputStream in = process.getInputStream()) {
                in.transferTo(output);
            } catch (IOException e) {
                log.debug("{} output not read: {}", name, e.getMessage());
            }
        });
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(name + " did not finish within " + timeout);
        }
        reader.join(Duration.ofSeconds(5));
        if (process.exitValue() != 0) {
            String text = output.toString(StandardCharsets.UTF_8).strip();
            if (text.length() > OUTPUT_TAIL) {
                text = "…" + text.substring(text.length() - OUTPUT_TAIL);
            }
            throw new IllegalStateException(name + " exited with " + process.exitValue()
                    + (text.isEmpty() ? "" : ": " + text));
        }
    }

    private BackupRun save(BackupRun run) {
        runs.save(run);
        return run;
    }

    private static String reason(Throwable e) {
        Throwable cause = e;
        while (cause.getMessage() == null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    private void alarm(BackupRun run, String body) {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        path.report(ALERT, "warn", (level, previous) -> new OperationalAlertService.Alert("system.backup_failed",
                "warning", "A backup failed", body, "/v1/backups", data(run)));
    }

    private void recovered() {
        OperationalAlertService path = alerts.getIfAvailable();
        if (path == null) {
            return;
        }
        path.report(ALERT, "ok", (level, previous) -> new OperationalAlertService.Alert("system.backup_recovered",
                "info", "Backups are working again", "The last backup or restore succeeded.", "/v1/backups",
                Map.of()));
    }

    private static Map<String, Object> data(BackupRun run) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (run != null) {
            data.put("backupId", run.id());
            data.put("type", run.type());
            data.put("status", run.status());
            data.put("target", run.target());
            data.put("error", run.error());
        }
        return data;
    }

    /** Points the service at other {@code pg_dump} and {@code pg_restore} programs (tests). */
    void commands(String dump, String restore) {
        this.pgDump = dump;
        this.pgRestore = restore;
    }
}
