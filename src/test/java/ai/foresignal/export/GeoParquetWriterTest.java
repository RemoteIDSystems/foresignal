package ai.foresignal.export;

import static ai.foresignal.export.ExportRowsFixture.T0;
import static ai.foresignal.export.ExportRowsFixture.line;
import static ai.foresignal.export.ExportRowsFixture.point;
import static ai.foresignal.export.ExportRowsFixture.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.format.FileMetaData;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKBReader;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The GeoParquet 1.1 writer (prompt 111): a round trip through parquet-java's reader, the {@code geo}
 * metadata, UTC microsecond timestamps, observed geometry types, row groups of 65,536 rows and the
 * ZSTD codec, all without Hadoop on the runtime classpath.
 */
class GeoParquetWriterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @TempDir Path dir;

    private Path write(List<Map<String, Object>> rows, List<String> flattened) throws Exception {
        Path file = dir.resolve("out.parquet");
        try (GeoParquetWriter writer = new GeoParquetWriter()) {
            writer.open(file, ExportSchema.standard(flattened, true), Map.of("licence", "{\"licenses\":[]}"));
            for (Map<String, Object> row : rows) {
                writer.write(row);
            }
            assertThat(writer.rows()).isEqualTo(rows.size());
        }
        return file;
    }

    @Test
    @DisplayName("rows round-trip through the parquet reader with WKB geometry, bbox and every column")
    void roundTrip() throws Exception {
        Map<String, Object> a = row("src", "a", "aircraft", 1, point(10.5, 50.25));
        a.put("order", "x1");
        Map<String, Object> b = row("src", "b", "aircraft", 2, null);
        Path file = write(List.of(a, b), List.of("order"));

        List<Group> rows = GeoParquetTestReader.rows(file);
        assertThat(rows).hasSize(2);
        Group first = rows.getFirst();
        assertThat(first.getString("source_id", 0)).isEqualTo("src");
        assertThat(first.getString("external_id", 0)).isEqualTo("a");
        assertThat(first.getLong("seq", 0)).isEqualTo(1L);
        assertThat(first.getInteger("version", 0)).isEqualTo(1);
        assertThat(first.getString("title", 0)).isEqualTo("Title <a> & co");
        assertThat(first.getString("order", 0)).isEqualTo("x1");
        assertThat(first.getString("payload", 0)).contains("\"v\":1");
        byte[] wkb = first.getBinary("geometry", 0).getBytes();
        assertThat(new WKBReader().read(wkb).getCoordinate()).isEqualTo(new Coordinate(10.5, 50.25));
        Group bbox = first.getGroup("bbox", 0);
        assertThat(bbox.getDouble("xmin", 0)).isEqualTo(10.5);
        assertThat(bbox.getDouble("ymax", 0)).isEqualTo(50.25);
        Group second = rows.get(1);
        assertThat(second.getFieldRepetitionCount("geometry")).isZero();
        assertThat(second.getFieldRepetitionCount("bbox")).isZero();
        assertThat(GeoParquetFiles.rowCount(file)).isEqualTo(2);
    }

    @Test
    @DisplayName("timestamps are INT64 TIMESTAMP(MICROS, UTC) holding microseconds since the epoch")
    void timestampsAreUtcMicros() throws Exception {
        Path file = write(List.of(row("src", "a", "k", 7, point(1, 2))), List.of());
        ParquetMetadata footer = GeoParquetTestReader.footer(file);
        MessageType schema = footer.getFileMetaData().getSchema();
        PrimitiveType observed = schema.getType("observed_at").asPrimitiveType();
        assertThat(observed.getPrimitiveTypeName()).isEqualTo(PrimitiveType.PrimitiveTypeName.INT64);
        LogicalTypeAnnotation.TimestampLogicalTypeAnnotation ts =
                (LogicalTypeAnnotation.TimestampLogicalTypeAnnotation) observed.getLogicalTypeAnnotation();
        assertThat(ts.isAdjustedToUTC()).isTrue();
        assertThat(ts.getUnit()).isEqualTo(LogicalTypeAnnotation.TimeUnit.MICROS);
        long micros = GeoParquetTestReader.rows(file).getFirst().getLong("observed_at", 0);
        assertThat(micros).isEqualTo(T0.plusSeconds(7).getEpochSecond() * 1_000_000L + 123_456L);
        assertThat(schema.getType("source_id").getLogicalTypeAnnotation()).isEqualTo(LogicalTypeAnnotation.stringType());
        assertThat(schema.getType("geometry").asPrimitiveType().getPrimitiveTypeName())
                .isEqualTo(PrimitiveType.PrimitiveTypeName.BINARY);
    }

    @Test
    @DisplayName("the geo metadata is GeoParquet 1.1.0 with the covering, the observed types and the overall bbox")
    void geoMetadata() throws Exception {
        Path file = write(List.of(row("s", "a", "k", 1, point(10, 50)), row("s", "b", "k", 2, line(-5, 40))),
                List.of());
        JsonNode geo = JSON.readTree(GeoParquetFiles.keyValueMetadata(file).get(GeoParquetWriter.GEO_KEY));
        assertThat(geo.get("version").asString()).isEqualTo("1.1.0");
        assertThat(geo.get("primary_column").asString()).isEqualTo("geometry");
        JsonNode column = geo.get("columns").get("geometry");
        assertThat(column.get("encoding").asString()).isEqualTo("WKB");
        assertThat(column.get("geometry_types").toString()).isEqualTo("[\"Point\",\"LineString\"]");
        assertThat(column.get("bbox").toString()).isEqualTo("[-5.0,40.0,10.0,50.0]");
        JsonNode covering = column.get("covering").get("bbox");
        assertThat(covering.get("xmin").toString()).isEqualTo("[\"bbox\",\"xmin\"]");
        assertThat(covering.get("ymin").toString()).isEqualTo("[\"bbox\",\"ymin\"]");
        assertThat(covering.get("xmax").toString()).isEqualTo("[\"bbox\",\"xmax\"]");
        assertThat(covering.get("ymax").toString()).isEqualTo("[\"bbox\",\"ymax\"]");
        assertThat(column.has("crs")).as("no crs: OGC:CRS84").isFalse();
        assertThat(GeoParquetFiles.keyValueMetadata(file)).containsEntry("foresignal.licence", "{\"licenses\":[]}")
                .containsEntry("foresignal.row_count", "2");
    }

    @Test
    @DisplayName("with no geometry at all, geometry_types is empty (unknown) and no bbox is claimed")
    void noGeometry() throws Exception {
        Path file = write(List.of(row("s", "a", "k", 1, null)), List.of());
        JsonNode column = JSON.readTree(GeoParquetFiles.keyValueMetadata(file).get("geo")).get("columns").get("geometry");
        assertThat(column.get("geometry_types").size()).isZero();
        assertThat(column.has("bbox")).isFalse();
    }

    @Test
    @DisplayName("row groups close every 65,536 rows and pages are ZSTD-compressed")
    void rowGroupsAndCodec() throws Exception {
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        for (int i = 0; i < GeoParquetWriter.ROW_GROUP + 10; i++) {
            rows.add(row("s", "e" + (i % 7), "k", i, point(i % 90, 0)));
        }
        Path file = write(rows, List.of());
        FileMetaData footer = GeoParquetFiles.footer(file);
        assertThat(footer.getRow_groups()).hasSize(2);
        assertThat(footer.getRow_groups().get(0).getNum_rows()).isEqualTo(GeoParquetWriter.ROW_GROUP);
        assertThat(footer.getRow_groups().get(1).getNum_rows()).isEqualTo(10);
        ParquetMetadata metadata = GeoParquetTestReader.footer(file);
        assertThat(metadata.getBlocks().getFirst().getColumns().getFirst().getCodec()).isEqualTo(CompressionCodecName.ZSTD);
        assertThat(GeoParquetTestReader.rows(file)).hasSize(GeoParquetWriter.ROW_GROUP + 10);
    }

    @Test
    @DisplayName("Z geometries are named with the GeoParquet suffix")
    void zTypes() throws Exception {
        org.locationtech.jts.geom.Point z = ExportRowsFixture.WGS84.createPoint(new Coordinate(1, 2, 3));
        Map<String, Object> r = row("s", "a", "k", 1, null);
        r.put(ExportWriter.GEOMETRY, new org.locationtech.jts.io.WKBWriter(3).write(z));
        r.put(ExportWriter.BBOX, new double[] {1, 2, 1, 2});
        Path file = write(List.of(r), List.of());
        JsonNode column = JSON.readTree(GeoParquetFiles.keyValueMetadata(file).get("geo")).get("columns").get("geometry");
        assertThat(column.get("geometry_types").toString()).isEqualTo("[\"Point Z\"]");
    }

    @Test
    @DisplayName("a required column left null fails the write; a closed or never-opened writer closes quietly")
    void requiredColumnAndClose() throws Exception {
        Map<String, Object> r = row("s", "a", "k", 1, null);
        r.remove("source_id");
        Path file = dir.resolve("bad.parquet");
        GeoParquetWriter writer = new GeoParquetWriter();
        writer.open(file, ExportSchema.standard(List.of(), true), null);
        try {
            assertThatThrownBy(() -> writer.write(r)).isInstanceOf(IllegalArgumentException.class);
        } finally {
            try {
                writer.close();
            } catch (Exception ignored) {
                // The half-written record leaves the writer unusable; only the refusal matters here.
            }
        }
        new GeoParquetWriter().close();
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    @DisplayName("a file that is not Parquet is refused by the footer reader")
    void notParquet() throws Exception {
        Path file = dir.resolve("plain.txt");
        Files.writeString(file, "definitely not a parquet file");
        assertThatThrownBy(() -> GeoParquetFiles.rowCount(file)).isInstanceOf(java.io.IOException.class);
        Files.write(file, new byte[4]);
        assertThatThrownBy(() -> GeoParquetFiles.footer(file)).isInstanceOf(java.io.IOException.class);
    }

    @Test
    @DisplayName("micros accepts an instant or a number of microseconds")
    void micros() {
        assertThat(GeoParquetWriter.micros(java.time.Instant.ofEpochSecond(1, 2_000))).isEqualTo(1_000_002L);
        assertThat(GeoParquetWriter.micros(42L)).isEqualTo(42L);
    }
}
