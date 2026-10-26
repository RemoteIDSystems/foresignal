package ai.foresignal.export;

import ai.foresignal.source.spi.HistoryTier;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The file format of archive segments (prompt R2-016): gzip-compressed NDJSON, one {@code record_version} row per
 * line with every column under its own name. {@code geom} is hex EWKB, the JSON columns are their text, timestamps are
 * ISO-8601 instants, so a row loads back unchanged ({@link #load}). The format needs nothing but gzip and a JSON
 * reader to inspect, and reads back at run time, which the prompt 111 GeoParquet files cannot: reading them needs the
 * Hadoop classes that are deliberately not on the runtime classpath ({@link GeoParquetFiles}).
 */
@Component
public class ArchiveSegmentFiles {

    /** The columns of {@code record_version}, in table order. */
    static final List<String> COLUMNS = List.of("seq", "ingested_at", "observed_at", "received_at", "event_time",
            "valid_until", "valid_until_explicit", "prev_seq", "batch_id", "version", "history_tier", "persist_reason",
            "qa_flags", "tombstone", "source_id", "external_id", "kind", "geom", "payload", "quality", "observer",
            "valid_from");

    private static final List<String> TIMESTAMPS = List.of("ingested_at", "observed_at", "received_at", "event_time",
            "valid_until", "valid_until_explicit", "valid_from");

    private static final String SELECT = "SELECT v.seq, v.ingested_at, v.observed_at, v.received_at, v.event_time,"
            + " v.valid_until, v.valid_until_explicit, v.prev_seq, v.batch_id, v.version, v.history_tier,"
            + " v.persist_reason, v.qa_flags, v.tombstone, v.source_id, v.external_id, v.kind,"
            + " encode(ST_AsEWKB(v.geom), 'hex') AS geom, v.payload::text AS payload, v.quality::text AS quality,"
            + " v.observer::text AS observer, v.valid_from FROM record_version v";

    private static final String INSERT = "INSERT INTO record_version_restored (archive_id, seq, ingested_at,"
            + " observed_at, received_at, event_time, valid_until, valid_until_explicit, prev_seq, batch_id, version,"
            + " history_tier, persist_reason, qa_flags, tombstone, source_id, external_id, kind, geom, payload, quality,"
            + " observer, valid_from) VALUES (:archive_id, :seq, :ingested_at, :observed_at, :received_at,"
            + " :event_time, :valid_until, :valid_until_explicit, :prev_seq, :batch_id, :version, :history_tier,"
            + " :persist_reason, :qa_flags, :tombstone, :source_id, :external_id, :kind,"
            + " ST_GeomFromEWKB(decode(CAST(:geom AS text), 'hex')), CAST(:payload AS jsonb),"
            + " CAST(:quality AS jsonb), CAST(:observer AS jsonb), :valid_from)";

    private static final int BATCH = 500;

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final ExportProperties properties;
    private final TransactionTemplate tx;

    /** Creates the format. */
    public ArchiveSegmentFiles(NamedParameterJdbcTemplate jdbc, JsonMapper json, ExportProperties properties,
            PlatformTransactionManager transactions) {
        this.jdbc = jdbc;
        this.json = json;
        this.properties = properties;
        this.tx = new TransactionTemplate(transactions);
    }

    /**
     * One seq range of one source to write.
     *
     * @param kindClause a kind restriction over alias {@code v} (empty for none)
     * @param kindParams its parameters
     * @param fromSeq the first seq (inclusive)
     * @param toSeq the end seq (exclusive)
     */
    public record Range(String kindClause, MapSqlParameterSource kindParams, long fromSeq, long toSeq) {}

    /**
     * What {@link #write} wrote.
     *
     * @param file the file
     * @param rows the rows
     * @param bytes its size
     * @param sha256 its SHA-256
     * @param fromSeq the lowest seq
     * @param toSeq the highest seq
     * @param fromTime the earliest {@code ingested_at}
     * @param toTime the latest {@code ingested_at}
     */
    public record Written(Path file, long rows, long bytes, String sha256, long fromSeq, long toSeq, Instant fromTime,
            Instant toTime) {}

    /**
     * Writes every row of {@code sourceId} in {@code tier} within {@code ranges} to {@code file} (replaced), fsyncs
     * it and reads its SHA-256.
     *
     * @return what was written, or null when no row matched (no file is left)
     */
    public Written write(Path file, HistoryTier tier, String sourceId, List<Range> ranges) {
        long[] stats = {0, Long.MAX_VALUE, Long.MIN_VALUE};
        Instant[] times = {null, null};
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                GZIPOutputStream gzip = new GZIPOutputStream(Channels.newOutputStream(channel), 1 << 16);
                Writer out = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8), 1 << 16);
                for (Range range : ranges) {
                    long after = range.fromSeq();
                    while (true) {
                        MapSqlParameterSource p = new MapSqlParameterSource(range.kindParams().getValues())
                                .addValue("tier", tier.code()).addValue("source", sourceId)
                                .addValue("after", after).addValue("to", range.toSeq())
                                .addValue("limit", properties.pageSize());
                        List<Map<String, Object>> page = jdbc.query(SELECT
                                + " WHERE v.history_tier = :tier AND v.source_id = :source AND v.seq >= :after"
                                + " AND v.seq < :to" + range.kindClause() + " ORDER BY v.seq LIMIT :limit", p,
                                (rs, i) -> row(rs));
                        for (Map<String, Object> row : page) {
                            out.write(new String(json.writeValueAsBytes(row), StandardCharsets.UTF_8));
                            out.write('\n');
                            long seq = (Long) row.get("seq");
                            Instant ingested = Instant.parse((String) row.get("ingested_at"));
                            stats[0]++;
                            stats[1] = Math.min(stats[1], seq);
                            stats[2] = Math.max(stats[2], seq);
                            times[0] = times[0] == null || ingested.isBefore(times[0]) ? ingested : times[0];
                            times[1] = times[1] == null || ingested.isAfter(times[1]) ? ingested : times[1];
                        }
                        if (page.size() < properties.pageSize()) {
                            break;
                        }
                        after = (Long) page.getLast().get("seq") + 1;
                    }
                }
                out.flush();
                gzip.finish();
                gzip.flush();
                channel.force(true);
            }
            if (stats[0] == 0) {
                Files.deleteIfExists(file);
                return null;
            }
            return new Written(file, stats[0], Files.size(file), ExportManifests.sha256(file), stats[1], stats[2],
                    times[0], times[1]);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the archive segment file " + file, e);
        }
    }

    /**
     * The rows of {@code sourceId} in {@code tier} within {@code ranges}, as {@link #write} would select them.
     *
     * @return the count
     */
    public long count(HistoryTier tier, String sourceId, List<Range> ranges) {
        long total = 0;
        for (Range range : ranges) {
            MapSqlParameterSource p = new MapSqlParameterSource(range.kindParams().getValues())
                    .addValue("tier", tier.code()).addValue("source", sourceId)
                    .addValue("from", range.fromSeq()).addValue("to", range.toSeq());
            Long n = jdbc.queryForObject("SELECT count(*) FROM record_version v WHERE v.history_tier = :tier"
                    + " AND v.source_id = :source AND v.seq >= :from AND v.seq < :to" + range.kindClause(), p,
                    Long.class);
            total += n == null ? 0 : n;
        }
        return total;
    }

    /**
     * Loads {@code file} into the restore area ({@code record_version_restored}) under {@code archiveId}, in one
     * transaction, replacing what an earlier restore of the same file loaded.
     *
     * @return the rows loaded
     */
    public long load(String archiveId, String sourceId, HistoryTier tier, Path file) {
        Long loaded = tx.execute(status -> {
            jdbc.update("DELETE FROM record_version_restored WHERE archive_id = :id AND source_id = :source"
                    + " AND history_tier = :tier", new MapSqlParameterSource("id", archiveId)
                    .addValue("source", sourceId).addValue("tier", tier.code()));
            long rows = 0;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(Files.newInputStream(file), 1 << 16), StandardCharsets.UTF_8), 1 << 16)) {
                List<MapSqlParameterSource> batch = new ArrayList<>();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    Map<String, Object> row = json.readValue(line, new TypeReference<LinkedHashMap<String, Object>>() {});
                    batch.add(params(archiveId, row));
                    if (batch.size() == BATCH) {
                        rows += flush(batch);
                    }
                }
                rows += flush(batch);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read the archive segment file " + file, e);
            }
            return rows;
        });
        return loaded == null ? 0 : loaded;
    }

    private long flush(List<MapSqlParameterSource> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        jdbc.batchUpdate(INSERT, batch.toArray(new MapSqlParameterSource[0]));
        long n = batch.size();
        batch.clear();
        return n;
    }

    private static MapSqlParameterSource params(String archiveId, Map<String, Object> row) {
        MapSqlParameterSource p = new MapSqlParameterSource("archive_id", archiveId);
        for (String column : COLUMNS) {
            Object value = row.get(column);
            if (TIMESTAMPS.contains(column)) {
                value = value == null ? null : Timestamp.from(Instant.parse(String.valueOf(value)));
            } else if (value instanceof Number number && !"geom".equals(column)) {
                value = switch (column) {
                    case "seq", "prev_seq", "batch_id" -> number.longValue();
                    default -> number.intValue();
                };
            }
            p.addValue(column, value);
        }
        return p;
    }

    private static Map<String, Object> row(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String column : COLUMNS) {
            Object value;
            if (TIMESTAMPS.contains(column)) {
                Timestamp ts = rs.getTimestamp(column);
                value = ts == null ? null : ts.toInstant().toString();
            } else if ("seq".equals(column) || "prev_seq".equals(column) || "batch_id".equals(column)) {
                long n = rs.getLong(column);
                value = rs.wasNull() ? null : n;
            } else if ("tombstone".equals(column)) {
                value = rs.getBoolean(column);
            } else if ("version".equals(column) || "history_tier".equals(column) || "persist_reason".equals(column)
                    || "qa_flags".equals(column)) {
                value = rs.getInt(column);
            } else {
                value = rs.getString(column);
            }
            row.put(column, value);
        }
        return row;
    }
}
