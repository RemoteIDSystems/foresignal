package ai.foresignal.export;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.ReadQueryRunner;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes retention scopes out to the cold archive before retention removes them (prompt 111).
 *
 * <p>Layout (hive-partitioned, readable by DuckDB with {@code hive_partitioning=true}):
 * {@code record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd of ingested_at, UTC}/part-{fromSeq}-{toSeq}.parquet},
 * one GeoParquet file per date of a scope's rows, named after the first and last seq it holds, plus
 * one {@code _manifest.json} per partition listing its files (rows, bytes, sha256, upload state) and
 * the license summary.
 *
 * <p>Rows are read in seq order with keyset pages; because {@code ingested_at} never decreases with
 * seq (I3), each date is one contiguous run and a file is closed when the date changes. Every file is
 * written under a temporary name, fsynced and verified by reading its row count back from the Parquet
 * footer before it is renamed into place; only then does the scope's {@link ArchiveWatermarkEntity}
 * advance. The watermark makes the archive exactly-once: the next run starts at it, so rows whose
 * delete failed after a verified write are never written again.
 *
 * <p>When the target is S3, each verified file is uploaded on a single background thread
 * ({@link S3Uploader}); the manifest records the key and upload time, and the local copy is deleted
 * only when {@code keepLocal=false} and the upload's checksum matched.
 */
@Service
public class ArchiveService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    /** The manifest file of each partition. */
    public static final String MANIFEST = "_manifest.json";

    /** The parquet media type. */
    static final String MEDIA_TYPE = "application/vnd.apache.parquet";

    private final ReadQueryRunner runner;
    private final ExportStore store;
    private final ArchiveSettingsService settingsService;
    private final ArchiveWatermarkRepository watermarks;
    private final S3Uploader s3;
    private final ExportProperties properties;
    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final ExecutorService uploads = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("archive-upload").factory());
    private final Object manifestLock = new Object();

    /** Creates the service. */
    public ArchiveService(ReadQueryRunner runner, ExportStore store, ArchiveSettingsService settingsService,
            ArchiveWatermarkRepository watermarks, S3Uploader s3, ExportProperties properties, JsonMapper jsonMapper,
            Clock clock) {
        this.runner = runner;
        this.store = store;
        this.settingsService = settingsService;
        this.watermarks = watermarks;
        this.s3 = s3;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    /**
     * One scope to archive: the rows of {@code sourceId} in {@code tier} with a seq in
     * {@code [fromSeq, toSeq)}, restricted by {@code kindClause} (over alias {@code v}).
     *
     * @param scopeKey the scope's watermark key ({@code *}, a kind, or {@code !k1,k2})
     */
    public record Range(HistoryTier tier, String sourceId, String scopeKey, String kindClause,
            MapSqlParameterSource kindParams, long fromSeq, long toSeq) {}

    /**
     * One archived file.
     *
     * @param name {@code part-{fromSeq}-{toSeq}.parquet}
     * @param fromSeq the first seq in the file
     * @param toSeq the last seq in the file
     * @param rows the rows, as read back from the footer
     * @param bytes the file size
     * @param sha256 the file's SHA-256
     * @param local whether the local copy exists
     * @param uploadedAt when it was uploaded, or null
     * @param s3Key its object key, or null
     */
    public record ArchivedFile(String name, long fromSeq, long toSeq, long rows, long bytes, String sha256,
            boolean local, String uploadedAt, String s3Key) {}

    /**
     * The outcome of archiving one range.
     *
     * @param files the files written, in seq order
     * @param throughSeq the scope's watermark afterwards
     */
    public record Written(List<ArchivedFile> files, long throughSeq) {

        /** Bytes written. */
        public long bytes() {
            return files.stream().mapToLong(ArchivedFile::bytes).sum();
        }

        /** Rows written. */
        public long rows() {
            return files.stream().mapToLong(ArchivedFile::rows).sum();
        }
    }

    /**
     * Archives {@code range}, starting at the scope's watermark.
     *
     * @param license the {@code ExportLicenceSummary} of the source, recorded in each manifest and file
     * @throws UncheckedIOException or another runtime exception when a write or verification fails;
     *     the files verified before the failure stay and the watermark covers them
     */
    public Written archive(ArchiveSettings settings, Range range, Map<String, Object> license) {
        String watermarkId = ArchiveWatermarkEntity.id(range.tier().wireValue(), range.sourceId(), range.scopeKey());
        long start = Math.max(range.fromSeq(), watermarks.findById(watermarkId)
                .map(ArchiveWatermarkEntity::getThroughSeq).orElse(0L));
        List<ArchivedFile> files = new ArrayList<>();
        if (start >= range.toSeq()) {
            return new Written(files, start);
        }
        Path root = root(settings);
        String licenseJson = new String(jsonMapper.writeValueAsBytes(license), StandardCharsets.UTF_8);
        OpenFile open = null;
        long after = start;
        try {
            while (true) {
                List<Map<String, Object>> page = page(range, after);
                for (Map<String, Object> row : page) {
                    long seq = (Long) row.get("seq");
                    Instant ingestedAt = (Instant) row.get("ingested_at");
                    LocalDate date = LocalDate.ofInstant(ingestedAt, ZoneOffset.UTC);
                    if (open != null && !open.date.equals(date)) {
                        files.add(finish(open, settings, license));
                        advance(watermarkId, open.lastSeq + 1);
                        open = null;
                    }
                    if (open == null) {
                        open = open(root, range, date, seq, licenseJson);
                    }
                    open.writer.write(row);
                    open.lastSeq = seq;
                }
                if (page.size() < properties.pageSize()) {
                    break;
                }
                after = (Long) page.getLast().get("seq") + 1;
            }
            if (open != null) {
                files.add(finish(open, settings, license));
                open = null;
            }
            advance(watermarkId, range.toSeq());
            return new Written(files, range.toSeq());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not archive " + range.sourceId() + " " + range.scopeKey(), e);
        } finally {
            if (open != null) {
                abandon(open);
            }
        }
    }

    private List<Map<String, Object>> page(Range range, long after) {
        return runner.read(properties.queryTimeout(), jdbc -> {
            MapSqlParameterSource params = new MapSqlParameterSource(range.kindParams().getValues())
                    .addValue("tier", range.tier().code()).addValue("source", range.sourceId())
                    .addValue("after", after).addValue("to", range.toSeq()).addValue("limit", properties.pageSize());
            String sql = "SELECT " + ExportRows.versionColumns("v") + " FROM record_version v"
                    + " WHERE v.history_tier = :tier AND v.source_id = :source AND v.seq >= :after AND v.seq < :to"
                    + range.kindClause() + " ORDER BY v.seq LIMIT :limit";
            return jdbc.query(sql, params, (rs, i) -> ExportRows.map(rs));
        });
    }

    /** A part file being written. */
    private static final class OpenFile {
        final Path dir;
        final Path tmp;
        final LocalDate date;
        final long firstSeq;
        final ExportWriter writer;
        final String partitionPath;
        long lastSeq;

        OpenFile(Path dir, Path tmp, LocalDate date, long firstSeq, ExportWriter writer, String partitionPath) {
            this.dir = dir;
            this.tmp = tmp;
            this.date = date;
            this.firstSeq = firstSeq;
            this.writer = writer;
            this.partitionPath = partitionPath;
        }
    }

    private OpenFile open(Path root, Range range, LocalDate date, long firstSeq, String licenseJson) throws IOException {
        String partitionPath = partitionPath(range.tier(), range.sourceId(), date);
        Path dir = root.resolve(partitionPath);
        Files.createDirectories(dir);
        Path tmp = dir.resolve(".part-" + firstSeq + "-" + System.nanoTime() + ".tmp");
        ExportWriter writer = new GeoParquetWriter();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("licence", licenseJson);
        metadata.put("name", partitionPath);
        writer.open(tmp, ExportSchema.standard(List.of(), true), metadata);
        return new OpenFile(dir, tmp, date, firstSeq, writer, partitionPath);
    }

    /** Closes, fsyncs and verifies a part file, then moves it into place and records it. */
    private ArchivedFile finish(OpenFile file, ArchiveSettings settings, Map<String, Object> license)
            throws IOException {
        file.writer.close();
        try (FileChannel channel = FileChannel.open(file.tmp, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
        long readBack = GeoParquetFiles.rowCount(file.tmp);
        if (readBack != file.writer.rows()) {
            Files.deleteIfExists(file.tmp);
            throw new IOException("Archive file " + file.tmp + " reads back " + readBack + " rows; "
                    + file.writer.rows() + " were written");
        }
        String name = "part-" + file.firstSeq + "-" + file.lastSeq + ".parquet";
        Path target = file.dir.resolve(name);
        try {
            Files.move(file.tmp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(file.tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        ArchivedFile archived = new ArchivedFile(name, file.firstSeq, file.lastSeq, readBack, Files.size(target),
                ExportManifests.sha256(target), true, null, null);
        recordFile(file.dir, archived, license);
        if ("s3".equals(settings.target()) && settings.s3() != null) {
            scheduleUpload(settings, file.dir, file.partitionPath, target, archived);
        }
        return archived;
    }

    private void abandon(OpenFile file) {
        try {
            file.writer.close();
        } catch (IOException | RuntimeException e) {
            log.debug("Could not close an abandoned archive file", e);
        }
        try {
            Files.deleteIfExists(file.tmp);
        } catch (IOException e) {
            log.warn("Could not delete the abandoned archive file {}", file.tmp, e);
        }
    }

    private void advance(String id, long throughSeq) {
        Instant now = clock.instant();
        ArchiveWatermarkEntity watermark = watermarks.findById(id)
                .orElseGet(() -> new ArchiveWatermarkEntity(id, 0L, now));
        watermark.advance(throughSeq, now);
        watermarks.save(watermark);
    }

    /** {@code record_version/tier={std|trk}/source_id={id}/date={date}}. */
    public static String partitionPath(HistoryTier tier, String sourceId, LocalDate date) {
        return "record_version/tier=" + tierSegment(tier) + "/source_id=" + sourceId + "/date=" + date;
    }

    /**
     * The hive {@code tier} value of {@code tier}: {@code std} or {@code trk} (prompt 111, {@code ArchivePartition.tier}),
     * not the {@link HistoryTier#wireValue()} {@code standard}/{@code track} (checkpoint 124).
     */
    public static String tierSegment(HistoryTier tier) {
        return tier == HistoryTier.TRACK ? "trk" : "std";
    }

    /** The archive root of {@code settings}: an absolute path, a path under the data directory, or the default. */
    public Path root(ArchiveSettings settings) {
        if (settings.root() == null || settings.root().isBlank()) {
            return store.archiveRoot();
        }
        Path configured = Path.of(settings.root());
        return configured.isAbsolute() ? configured.normalize() : store.resolve(settings.root());
    }

    // =====================================================================================
    // Manifests
    // =====================================================================================

    /** The manifest of a partition directory: {@code files}, {@code rows}, {@code license}. */
    public Map<String, Object> readManifest(Path partitionDir) {
        Path manifest = partitionDir.resolve(MANIFEST);
        if (!Files.isRegularFile(manifest)) {
            return null;
        }
        try {
            return jsonMapper.readValue(Files.readAllBytes(manifest), new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (IOException | RuntimeException e) {
            log.warn("Could not read the archive manifest {}", manifest, e);
            return null;
        }
    }

    private void recordFile(Path partitionDir, ArchivedFile file, Map<String, Object> license) throws IOException {
        synchronized (manifestLock) {
            Map<String, Object> manifest = readManifest(partitionDir);
            List<Map<String, Object>> files = new ArrayList<>();
            if (manifest != null && manifest.get("files") instanceof List<?> existing) {
                for (Object entry : existing) {
                    if (entry instanceof Map<?, ?> map && !file.name().equals(map.get("name"))) {
                        files.add(stringKeys(map));
                    }
                }
            }
            files.add(fileMap(file));
            writeManifest(partitionDir, files, license);
        }
    }

    private void updateFile(Path partitionDir, String name, java.util.function.Consumer<Map<String, Object>> change)
            throws IOException {
        synchronized (manifestLock) {
            Map<String, Object> manifest = readManifest(partitionDir);
            if (manifest == null || !(manifest.get("files") instanceof List<?> existing)) {
                return;
            }
            List<Map<String, Object>> files = new ArrayList<>();
            for (Object entry : existing) {
                if (entry instanceof Map<?, ?> map) {
                    Map<String, Object> copy = stringKeys(map);
                    if (name.equals(copy.get("name"))) {
                        change.accept(copy);
                    }
                    files.add(copy);
                }
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> license = manifest.get("licence") instanceof Map<?, ?> l ? (Map<String, Object>) l : null;
            writeManifest(partitionDir, files, license);
        }
    }

    private void writeManifest(Path partitionDir, List<Map<String, Object>> files, Map<String, Object> license)
            throws IOException {
        files.sort(java.util.Comparator.comparingLong(f -> ((Number) f.get("fromSeq")).longValue()));
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("rows", files.stream().mapToLong(f -> ((Number) f.get("rows")).longValue()).sum());
        manifest.put("files", files);
        manifest.put("licence", license);
        Path tmp = partitionDir.resolve(MANIFEST + ".tmp");
        Files.write(tmp, jsonMapper.writeValueAsBytes(manifest));
        try {
            Files.move(tmp, partitionDir.resolve(MANIFEST), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, partitionDir.resolve(MANIFEST), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Map<String, Object> fileMap(ArchivedFile file) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", file.name());
        out.put("fromSeq", file.fromSeq());
        out.put("toSeq", file.toSeq());
        out.put("rows", file.rows());
        out.put("bytes", file.bytes());
        out.put("sha256", file.sha256());
        out.put("local", file.local());
        out.put("uploadedAt", file.uploadedAt());
        out.put("s3Key", file.s3Key());
        return out;
    }

    private static Map<String, Object> stringKeys(Map<?, ?> map) {
        Map<String, Object> out = new LinkedHashMap<>();
        map.forEach((k, v) -> out.put(String.valueOf(k), v));
        return out;
    }

    // =====================================================================================
    // Upload
    // =====================================================================================

    private void scheduleUpload(ArchiveSettings settings, Path partitionDir, String partitionPath, Path file,
            ArchivedFile archived) {
        Map<String, String> secrets = settingsService.secrets(settings);
        String key = partitionPath + "/" + archived.name();
        uploads.submit(() -> upload(settings, secrets, partitionDir, key, file, archived));
    }

    /** Uploads one archived file and records the result; runs on the upload thread. */
    void upload(ArchiveSettings settings, Map<String, String> secrets, Path partitionDir, String key, Path file,
            ArchivedFile archived) {
        try {
            S3Uploader.Result result = s3.upload(settings.s3(), secrets, key, file);
            boolean deleteLocal = !settings.keepLocal() && result.verified();
            String uploadedAt = clock.instant().toString();
            if (deleteLocal) {
                Files.deleteIfExists(file);
            }
            updateFile(partitionDir, archived.name(), entry -> {
                entry.put("uploadedAt", uploadedAt);
                entry.put("s3Key", result.key());
                entry.put("local", !deleteLocal);
            });
        } catch (IOException | RuntimeException e) {
            log.warn("Could not upload the archive file {} to {}", file, key, e);
        }
    }

    /** Stops the upload thread. */
    @PreDestroy
    public void shutdown() {
        uploads.shutdown();
    }
}
