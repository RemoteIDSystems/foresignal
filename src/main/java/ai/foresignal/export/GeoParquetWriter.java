package ai.foresignal.export;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.parquet.bytes.HeapByteBufferAllocator;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.column.impl.ColumnWriteStoreV1;
import org.apache.parquet.column.page.PageWriteStore;
import org.apache.parquet.conf.PlainParquetConfiguration;
import org.apache.parquet.hadoop.CodecFactory;
import org.apache.parquet.hadoop.ColumnChunkPageWriteStore;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName;
import org.apache.parquet.schema.Types;

/**
 * A GeoParquet 1.1 writer (prompt 111). Uses the Hadoop-free parquet-java path: a local
 * {@link LocalOutputFile}, {@code PlainParquetConfiguration}, {@link ParquetFileWriter} and
 * {@link ColumnWriteStoreV1}. Compression is {@code UNCOMPRESSED} (no codec natives needed). The
 * {@code geo} file metadata (version 1.1.0, WKB, observed geometry types, bbox and {@code covering}) is
 * written at close.
 */
public final class GeoParquetWriter implements ExportWriter {

    private static final int PAGE_SIZE = 1 << 20;
    private static final int BLOCK_SIZE = 128 * 1024 * 1024;
    private static final int MAX_PADDING = 8 * 1024;
    private static final int ROW_GROUP = 65_536;

    private ExportSchema schema;
    private MessageType messageType;
    private ParquetFileWriter fileWriter;
    private CodecFactory codecFactory;
    private PageWriteStore pageStore;
    private ColumnWriteStoreV1 columnStore;
    private RecordConsumer consumer;
    private long rows;
    private int blockRows;
    private final Set<String> geometryTypes = new LinkedHashSet<>();
    private double[] bbox;

    @Override
    public void open(Path file, ExportSchema schema, Map<String, String> metadata) throws IOException {
        this.schema = schema;
        this.messageType = messageType(schema);
        this.codecFactory = new CodecFactory(new PlainParquetConfiguration(), PAGE_SIZE);
        this.fileWriter = new ParquetFileWriter(new LocalOutputFile(file), messageType,
                ParquetFileWriter.Mode.CREATE, BLOCK_SIZE, MAX_PADDING);
        fileWriter.start();
        startBlock();
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        consumer.startMessage();
        List<ExportSchema.Column> columns = schema.columns();
        int index = 0;
        for (ExportSchema.Column column : columns) {
            Object value = row.get(column.name());
            if (value != null) {
                consumer.startField(column.name(), index);
                writeValue(column.type(), value);
                consumer.endField(column.name(), index);
            }
            index++;
        }
        if (schema.geometry()) {
            Object wkb = row.get("geometry");
            if (wkb instanceof byte[] bytes && bytes.length > 0) {
                consumer.startField("geometry", index);
                consumer.addBinary(Binary.fromConstantByteArray(bytes));
                consumer.endField("geometry", index);
                geometryTypes.add(geometryType(bytes));
            }
            index++;
            Object box = row.get("bbox");
            if (box instanceof double[] b && b.length == 4) {
                consumer.startField("bbox", index);
                consumer.startGroup();
                consumer.startField("xmin", 0);
                consumer.addDouble(b[0]);
                consumer.endField("xmin", 0);
                consumer.startField("ymin", 1);
                consumer.addDouble(b[1]);
                consumer.endField("ymin", 1);
                consumer.startField("xmax", 2);
                consumer.addDouble(b[2]);
                consumer.endField("xmax", 2);
                consumer.startField("ymax", 3);
                consumer.addDouble(b[3]);
                consumer.endField("ymax", 3);
                consumer.endGroup();
                consumer.endField("bbox", index);
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
            case STRING, WKB -> consumer.addBinary(Binary.fromString(String.valueOf(value)));
            case INT64 -> consumer.addLong(((Number) value).longValue());
            case INT32 -> consumer.addInteger(((Number) value).intValue());
            case DOUBLE -> consumer.addDouble(((Number) value).doubleValue());
            case TIMESTAMP_MICROS -> consumer.addLong(micros(value));
        }
    }

    private static long micros(Object value) {
        if (value instanceof Instant instant) {
            return instant.getEpochSecond() * 1_000_000L + instant.getNano() / 1_000L;
        }
        return ((Number) value).longValue();
    }

    private void startBlock() {
        pageStore = new ColumnChunkPageWriteStore(
                codecFactory.getCompressor(CompressionCodecName.UNCOMPRESSED), messageType,
                new HeapByteBufferAllocator(), PAGE_SIZE);
        ParquetProperties props = ParquetProperties.builder().build();
        columnStore = new ColumnWriteStoreV1(messageType, pageStore, props);
        MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(messageType);
        consumer = columnIO.getRecordWriter(columnStore);
        blockRows = 0;
    }

    private void endBlock() throws IOException {
        columnStore.flush();
        fileWriter.startBlock(blockRows);
        ((ColumnChunkPageWriteStore) pageStore).flushToFileWriter(fileWriter);
        fileWriter.endBlock();
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        if (blockRows > 0) {
            endBlock();
        }
        java.util.Map<String, String> extra = new java.util.LinkedHashMap<>();
        extra.put("geo", geoMetadata());
        extra.put("foresignal.row_count", Long.toString(rows));
        fileWriter.end(extra);
        fileWriter.close();
        codecFactory.release();
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

    private String geoMetadata() {
        List<String> types = geometryTypes.isEmpty() ? List.of("Point") : new ArrayList<>(geometryTypes);
        String box = bbox == null ? "[-180.0,-90.0,180.0,90.0]" : "[" + bbox[0] + "," + bbox[1] + "," + bbox[2] + ","
                + bbox[3] + "]";
        return "{\"version\":\"1.1.0\",\"primary_column\":\"geometry\",\"columns\":{\"geometry\":{"
                + "\"encoding\":\"WKB\",\"geometry_types\":" + jsonArray(types) + ",\"bbox\":" + box + ","
                + "\"covering\":{\"bbox\":{\"xmin\":[\"bbox\",\"xmin\"],\"ymin\":[\"bbox\",\"ymin\"],"
                + "\"xmax\":[\"bbox\",\"xmax\"],\"ymax\":[\"bbox\",\"ymax\"]}}}}}";
    }

    private static String jsonArray(List<String> values) {
        StringBuilder out = new StringBuilder("[");
        for (int k = 0; k < values.size(); k++) {
            if (k > 0) {
                out.append(',');
            }
            out.append('"').append(values.get(k)).append('"');
        }
        return out.append(']').toString();
    }

    /** The WKB geometry type name (first little-endian byte). */
    static String geometryType(byte[] wkb) {
        if (wkb.length < 5) {
            return "Point";
        }
        int type = (wkb[0] & 0xFF);
        return switch (type) {
            case 1 -> "Point";
            case 2 -> "LineString";
            case 3 -> "Polygon";
            case 4 -> "MultiPoint";
            case 5 -> "MultiLineString";
            case 6 -> "MultiPolygon";
            case 7 -> "GeometryCollection";
            default -> "Point";
        };
    }

    private static MessageType messageType(ExportSchema schema) {
        Types.MessageTypeBuilder builder = Types.buildMessage();
        for (ExportSchema.Column column : schema.columns()) {
            Types.Builder<?, ?> field = column.nullable() ? builder.optional(primitive(column.type()))
                    : builder.required(primitive(column.type()));
            if (column.type() == ExportSchema.Type.STRING) {
                field = field.as(LogicalTypeAnnotation.stringType());
            } else if (column.type() == ExportSchema.Type.TIMESTAMP_MICROS) {
                field = field.as(LogicalTypeAnnotation.timestampType(true,
                        LogicalTypeAnnotation.TimeUnit.MICROS));
            }
            field.named(column.name());
        }
        if (schema.geometry()) {
            builder.optional(PrimitiveTypeName.BINARY).named("geometry");
            builder.optionalGroup()
                    .optional(PrimitiveTypeName.DOUBLE).named("xmin")
                    .optional(PrimitiveTypeName.DOUBLE).named("ymin")
                    .optional(PrimitiveTypeName.DOUBLE).named("xmax")
                    .optional(PrimitiveTypeName.DOUBLE).named("ymax")
                    .named("bbox");
        }
        return builder.named("foresignal");
    }

    private static PrimitiveTypeName primitive(ExportSchema.Type type) {
        return switch (type) {
            case STRING, WKB -> PrimitiveTypeName.BINARY;
            case INT64, TIMESTAMP_MICROS -> PrimitiveTypeName.INT64;
            case INT32 -> PrimitiveTypeName.INT32;
            case DOUBLE -> PrimitiveTypeName.DOUBLE;
        };
    }
}
