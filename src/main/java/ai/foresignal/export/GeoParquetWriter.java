package ai.foresignal.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.parquet.bytes.HeapByteBufferAllocator;
import org.apache.parquet.column.ColumnWriteStore;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.ColumnChunkPageWriteStore;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.LocalOutputFile;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Types;
import tools.jackson.databind.json.JsonMapper;

/**
 * A GeoParquet 1.1 writer (prompt 111) on the Hadoop-free parquet-java path: parquet's own
 * {@link LocalOutputFile}, {@link ParquetFileWriter}, a {@link ColumnChunkPageWriteStore} with the
 * zstd-jni compressor of {@link ParquetCodecs}, and a column store from
 * {@link ParquetProperties#newColumnWriteStore}.
 *
 * <p>Layout: the {@link ExportSchema} columns ({@code seq} int64, {@code version} int32, timestamps as
 * {@code TIMESTAMP(MICROS, isAdjustedToUTC=true)}, text as {@code STRING}), then {@code geometry}
 * (WKB in a {@code BYTE_ARRAY}) and the {@code bbox} covering struct {@code {xmin, ymin, xmax, ymax}}
 * of doubles. Rows are written in the order they arrive (the export queries sort by
 * {@code (kind, source_id, external_id, seq)}); a row group is closed every {@value #ROW_GROUP} rows.
 *
 * <p>The file metadata key {@code geo} is written at close: version {@code 1.1.0}, primary column
 * {@code geometry}, encoding {@code WKB}, the geometry types actually seen ({@code []}, meaning
 * unknown, when no row had a geometry), the overall {@code bbox} (omitted when no row had one) and the
 * {@code covering.bbox} paths. No {@code crs} member, so the CRS is OGC:CRS84.
 */
public final class GeoParquetWriter implements ExportWriter {

    /** Rows per row group. */
    public static final int ROW_GROUP = 65_536;

    /** The file metadata key of the GeoParquet metadata. */
    public static final String GEO_KEY = "geo";

    /** The GeoParquet version written. */
    public static final String GEOPARQUET_VERSION = "1.1.0";

    private static final int PAGE_SIZE = 1 << 20;
    private static final long ROW_GROUP_BYTES = 128L * 1024 * 1024;
    private static final int COLUMN_INDEX_TRUNCATE = 64;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ExportSchema schema;
    private MessageType messageType;
    private ParquetProperties properties;
    private ParquetFileWriter fileWriter;
    private ColumnChunkPageWriteStore pageStore;
    private ColumnWriteStore columnStore;
    private RecordConsumer consumer;
    private Map<String, String> extraMetadata;
    private long rows;
    private int blockRows;
    private final Set<String> geometryTypes = new LinkedHashSet<>();
    private double[] bbox;
    private boolean closed;

    @Override
    public void open(Path file, ExportSchema exportSchema, Map<String, String> metadata) throws IOException {
        this.schema = exportSchema;
        this.messageType = messageType(exportSchema);
        this.properties = ParquetProperties.builder()
                .withPageSize(PAGE_SIZE)
                .withDictionaryPageSize(PAGE_SIZE)
                .withAllocator(new HeapByteBufferAllocator())
                .build();
        this.extraMetadata = new LinkedHashMap<>();
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (value != null) {
                    extraMetadata.put("foresignal." + key, value);
                }
            });
        }
        this.fileWriter = new ParquetFileWriter(new LocalOutputFile(file), messageType,
                ParquetFileWriter.Mode.OVERWRITE, ROW_GROUP_BYTES, 0, null, properties);
        fileWriter.start();
        startBlock();
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        consumer.startMessage();
        int index = 0;
        for (ExportSchema.Column column : schema.columns()) {
            Object value = row.get(column.name());
            if (value != null) {
                consumer.startField(column.name(), index);
                writeValue(column.type(), value);
                consumer.endField(column.name(), index);
            } else if (!column.nullable()) {
                throw new IllegalArgumentException("The export column " + column.name() + " is required");
            }
            index++;
        }
        if (schema.geometry()) {
            if (row.get(GEOMETRY) instanceof byte[] wkb && wkb.length > 0) {
                consumer.startField(GEOMETRY, index);
                consumer.addBinary(Binary.fromConstantByteArray(wkb));
                consumer.endField(GEOMETRY, index);
                String type = Wkb.geoParquetType(wkb);
                if (type != null) {
                    geometryTypes.add(type);
                }
            }
            index++;
            if (row.get(BBOX) instanceof double[] b && b.length == 4) {
                consumer.startField(BBOX, index);
                consumer.startGroup();
                String[] names = {"xmin", "ymin", "xmax", "ymax"};
                for (int k = 0; k < 4; k++) {
                    consumer.startField(names[k], k);
                    consumer.addDouble(b[k]);
                    consumer.endField(names[k], k);
                }
                consumer.endGroup();
                consumer.endField(BBOX, index);
                merge(b);
            }
        }
        consumer.endMessage();
        rows++;
        if (++blockRows >= ROW_GROUP) {
            endBlock();
            startBlock();
        }
    }

    private void writeValue(ExportSchema.Type type, Object value) {
        switch (type) {
            case STRING -> consumer.addBinary(Binary.fromString(String.valueOf(value)));
            case INT64 -> consumer.addLong(((Number) value).longValue());
            case INT32 -> consumer.addInteger(((Number) value).intValue());
            case DOUBLE -> consumer.addDouble(((Number) value).doubleValue());
            case TIMESTAMP_MICROS -> consumer.addLong(micros(value));
        }
    }

    /** Microseconds since the epoch of an {@link Instant} (or a number taken as microseconds). */
    static long micros(Object value) {
        if (value instanceof Instant instant) {
            return Math.addExact(Math.multiplyExact(instant.getEpochSecond(), 1_000_000L), instant.getNano() / 1_000L);
        }
        return ((Number) value).longValue();
    }

    private void startBlock() {
        pageStore = new ColumnChunkPageWriteStore(ParquetCodecs.INSTANCE.getCompressor(ParquetCodecs.DEFAULT_CODEC),
                messageType, new HeapByteBufferAllocator(), COLUMN_INDEX_TRUNCATE, false);
        columnStore = properties.newColumnWriteStore(messageType, pageStore, pageStore);
        MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(messageType);
        consumer = columnIO.getRecordWriter(columnStore);
        blockRows = 0;
    }

    private void endBlock() throws IOException {
        if (blockRows == 0) {
            return;
        }
        // The record writer caches the nulls of absent optional fields; they reach the column writers only on
        // flush, and a column flushed without them has no values ("writing empty page").
        consumer.flush();
        columnStore.flush();
        fileWriter.startBlock(blockRows);
        pageStore.flushToFileWriter(fileWriter);
        fileWriter.endBlock();
        columnStore.close();
        pageStore.close();
        blockRows = 0;
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        if (closed || fileWriter == null) {
            closed = true;
            return;
        }
        closed = true;
        endBlock();
        Map<String, String> extra = new LinkedHashMap<>(extraMetadata);
        extra.put(GEO_KEY, geoMetadata());
        extra.put("foresignal.row_count", Long.toString(rows));
        fileWriter.end(extra);
        fileWriter.close();
    }

    private void merge(double[] b) {
        if (bbox == null) {
            bbox = b.clone();
            return;
        }
        bbox[0] = Math.min(bbox[0], b[0]);
        bbox[1] = Math.min(bbox[1], b[1]);
        bbox[2] = Math.max(bbox[2], b[2]);
        bbox[3] = Math.max(bbox[3], b[3]);
    }

    /** The {@code geo} metadata JSON (GeoParquet 1.1.0). */
    String geoMetadata() {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("encoding", "WKB");
        column.put("geometry_types", new ArrayList<>(geometryTypes));
        if (bbox != null) {
            column.put("bbox", List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        }
        Map<String, Object> covering = new LinkedHashMap<>();
        covering.put("xmin", List.of(BBOX, "xmin"));
        covering.put("ymin", List.of(BBOX, "ymin"));
        covering.put("xmax", List.of(BBOX, "xmax"));
        covering.put("ymax", List.of(BBOX, "ymax"));
        column.put("covering", Map.of(BBOX, covering));
        Map<String, Object> geo = new LinkedHashMap<>();
        geo.put("version", GEOPARQUET_VERSION);
        geo.put("primary_column", GEOMETRY);
        geo.put("columns", Map.of(GEOMETRY, column));
        return new String(JSON.writeValueAsBytes(geo), StandardCharsets.UTF_8);
    }

    /** The Parquet schema of {@code schema}: its columns, then {@code geometry} and {@code bbox}. */
    static MessageType messageType(ExportSchema schema) {
        Types.MessageTypeBuilder builder = Types.buildMessage();
        for (ExportSchema.Column column : schema.columns()) {
            Types.PrimitiveBuilder<Types.GroupBuilder<MessageType>> field;
            if (column.nullable()) {
                field = builder.optional(primitive(column.type()));
            } else {
                field = builder.required(primitive(column.type()));
            }
            if (column.type() == ExportSchema.Type.STRING) {
                field = field.as(LogicalTypeAnnotation.stringType());
            } else if (column.type() == ExportSchema.Type.TIMESTAMP_MICROS) {
                field = field.as(LogicalTypeAnnotation.timestampType(true, LogicalTypeAnnotation.TimeUnit.MICROS));
            } else if (column.type() == ExportSchema.Type.INT32) {
                field = field.as(LogicalTypeAnnotation.intType(32, true));
            } else if (column.type() == ExportSchema.Type.INT64) {
                field = field.as(LogicalTypeAnnotation.intType(64, true));
            }
            field.named(column.name());
        }
        if (schema.geometry()) {
            builder.optional(PrimitiveTypeName.BINARY).named(GEOMETRY);
            builder.optionalGroup()
                    .required(PrimitiveTypeName.DOUBLE).named("xmin")
                    .required(PrimitiveTypeName.DOUBLE).named("ymin")
                    .required(PrimitiveTypeName.DOUBLE).named("xmax")
                    .required(PrimitiveTypeName.DOUBLE).named("ymax")
                    .named(BBOX);
        }
        return builder.named("foresignal");
    }

    private static PrimitiveTypeName primitive(ExportSchema.Type type) {
        return switch (type) {
            case STRING -> PrimitiveTypeName.BINARY;
            case INT64, TIMESTAMP_MICROS -> PrimitiveTypeName.INT64;
            case INT32 -> PrimitiveTypeName.INT32;
            case DOUBLE -> PrimitiveTypeName.DOUBLE;
        };
    }
}
