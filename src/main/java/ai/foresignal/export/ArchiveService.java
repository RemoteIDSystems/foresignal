package ai.foresignal.export;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.SqlParams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes records out to the cold-archive hive layout as GeoParquet before retention deletes them
 * (prompt 111): {@code record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd}/part-…parquet}
 * plus a {@code _manifest.json}. Each file's row count is verified against the database before
 * retention proceeds. S3 upload runs asynchronously after the local write.
 */
@Service
public class ArchiveService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    private final ReadQueryRunner runner;
    private final ExportStore store;
    private final ArchiveSettingsService settingsService;
    private final S3Uploader s3;
    private final JsonMapper jsonMapper;
    private final ExecutorService uploads = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public ArchiveService(ReadQueryRunner runner, ExportStore store, ArchiveSettingsService settingsService,
            S3Uploader s3, JsonMapper jsonMapper) {
        this.runner = runner;
        this.store = store;
        this.settingsService = settingsService;
        this.s3 = s3;
        this.jsonMapper = jsonMapper;
    }

    /** One archived file. */
    public record ArchivedFile(String name, long fromSeq, long toSeq, long rows, long bytes, String sha256,
            boolean local, String s3Key) {}

    /** One archived partition (a source, tier and date). */
    public record Partition(String tier, String sourceId, String date, String path, List<ArchivedFile> files,
            String licence) {}

    /** Archives every row of {@code sourceId} in {@code tier} with {@code seq < cutoff}. */
    public List<Partition> archive(String sourceId, HistoryTier tier, long cutoff, ArchiveSettings settings,
            String licence) {
        if (cutoff <= 0) {
            return List.of();
        }
        List<String> dates = runner.read(jdbc -> {
            SqlParams p = new SqlParams();
            return jdbc.query("SELECT to_char(ingested_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS d, count(*) AS n"
                            + " FROM record_version WHERE source_id = " + p.text(sourceId) + " AND history_tier = "
                            + p.add(tier.code()) + " AND seq < " + p.bigint(cutoff) + " GROUP BY 1 ORDER BY 1",
                    p.source(), (rs, i) -> rs.getString("d"));
        });
        List<Partition> partitions = new ArrayList<>();
        for (String date : dates) {
            partitions.add(archiveDate(sourceId, tier, date, cutoff, settings, licence));
        }
        return partitions;
    }

    private Partition archiveDate(String sourceId, HistoryTier tier, String date, long cutoff, ArchiveSettings settings,
            String licence) {
        Path dir = root(settings).resolve("record_version").resolve("tier=" + tier.wireValue())
                .resolve("source_id=" + sourceId).resolve("date=" + date);
        String name = "part-0-" + cutoff + ".parquet";
        Path file = dir.resolve(name);
        String mediaType = "application/vnd.apache.parquet";
        try {
            Files.createDirectories(dir);
            long written;
            try (ExportWriter writer = ExportWriterFactory.create(ExportFormat.GEOPARQUET)) {
                writer.open(file, ExportSchema.standard(List.of(), true), Map.of("licence", licence == null ? "unknown"
                        : licence));
                written = runner.stream(jdbc -> {
                    SqlParams p = new SqlParams();
                    String sql = "SELECT v.source_id, v.external_id, v.kind, v.seq, v.version, v.observed_at,"
                            + " v.event_time, v.valid_until, v.ingested_at, NULL::varchar AS title,"
                            + " v.payload::text AS payload, ST_AsBinary(v.geom) AS geom,"
                            + " ST_XMin(v.geom) AS xmin, ST_YMin(v.geom) AS ymin, ST_XMax(v.geom) AS xmax,"
                            + " ST_YMax(v.geom) AS ymax FROM record_version v"
                            + " WHERE v.source_id = " + p.text(sourceId) + " AND v.history_tier = " + p.add(tier.code())
                            + " AND v.seq < " + p.bigint(cutoff)
                            + " AND v.ingested_at >= CAST(" + p.instant(startOf(date)) + " AS timestamptz)"
                            + " AND v.ingested_at < CAST(" + p.instant(endOf(date)) + " AS timestamptz)";
                    jdbc.query(sql, p.source(), (RowCallbackHandler) rs -> {
                        try {
                            writer.write(map(rs));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    return writer.rows();
                });
            }
            long expected = runner.read(jdbc -> {
                SqlParams p = new SqlParams();
                Long n = jdbc.queryForObject("SELECT count(*) FROM record_version v WHERE v.source_id = "
                        + p.text(sourceId) + " AND v.history_tier = " + p.add(tier.code()) + " AND v.seq < "
                        + p.bigint(cutoff) + " AND v.ingested_at >= CAST(" + p.instant(startOf(date))
                        + " AS timestamptz) AND v.ingested_at < CAST(" + p.instant(endOf(date)) + " AS timestamptz)",
                        p.source(), Long.class);
                return n == null ? 0L : n;
            });
            if (written != expected) {
                Files.deleteIfExists(file);
                throw new IllegalStateException("Archived " + written + " rows but expected " + expected
                        + " for " + sourceId + "/" + date);
            }
            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            long bytes = Files.size(file);
            String sha256 = ExportManifests.sha256(file);
            ArchivedFile archived = new ArchivedFile(name, 0L, cutoff, written, bytes, sha256, true, null);
            Partition partition = new Partition(tier.wireValue(), sourceId, date,
                    "record_version/tier=" + tier.wireValue() + "/source_id=" + sourceId + "/date=" + date,
                    List.of(archived), licence);
            writeManifest(dir, partition);
            uploadIfNeeded(settings, partition, file);
            return partition;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not archive " + sourceId + " " + date, e);
        }
    }

    private void writeManifest(Path dir, Partition partition) {
        try {
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("files", partition.files());
            manifest.put("licence", partition.licence());
            manifest.put("rows", partition.files().stream().mapToLong(ArchivedFile::rows).sum());
            Files.write(dir.resolve("_manifest.json"), jsonMapper.writeValueAsBytes(manifest));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void uploadIfNeeded(ArchiveSettings settings, Partition partition, Path file) {
        if (!"s3".equals(settings.target()) || settings.s3() == null) {
            return;
        }
        Map<String, String> secrets = settingsService.secrets(settings);
        String key = partition.path() + "/" + file.getFileName();
        boolean keepLocal = settings.keepLocal();
        uploads.submit(() -> {
            try {
                s3.upload(settings.s3(), secrets, key, file);
                if (!keepLocal) {
                    Files.deleteIfExists(file);
                }
            } catch (RuntimeException | IOException e) {
                log.warn("Could not upload {} to S3", key, e);
            }
        });
    }

    private Path root(ArchiveSettings settings) {
        if (settings.root() == null || settings.root().isBlank()) {
            return store.archiveRoot();
        }
        return store.resolve(settings.root());
    }

    private static java.time.Instant startOf(String date) {
        return java.time.LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
    }

    private static java.time.Instant endOf(String date) {
        return java.time.LocalDate.parse(date).plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
    }

    private static Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source_id", rs.getString("source_id"));
        row.put("external_id", rs.getString("external_id"));
        row.put("kind", rs.getString("kind"));
        row.put("seq", rs.getLong("seq"));
        row.put("version", rs.getInt("version"));
        row.put("observed_at", instant(rs.getObject("observed_at")));
        row.put("event_time", instant(rs.getObject("event_time")));
        row.put("valid_until", instant(rs.getObject("valid_until")));
        row.put("ingested_at", instant(rs.getObject("ingested_at")));
        row.put("title", rs.getString("title"));
        row.put("payload", rs.getString("payload"));
        row.put("geometry", rs.getBytes("geom"));
        Object xmin = rs.getObject("xmin");
        if (xmin instanceof Number) {
            row.put("bbox", new double[] {((Number) xmin).doubleValue(), ((Number) rs.getObject("ymin")).doubleValue(),
                ((Number) rs.getObject("xmax")).doubleValue(), ((Number) rs.getObject("ymax")).doubleValue()});
        }
        return row;
    }

    private static java.time.Instant instant(Object value) {
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        return null;
    }
}
