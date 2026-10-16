package ai.foresignal.export;

import static ai.foresignal.export.ExportRowsFixture.line;
import static ai.foresignal.export.ExportRowsFixture.point;
import static ai.foresignal.export.ExportRowsFixture.row;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;
import org.wololo.flatgeobuf.generated.Header;

/**
 * The FlatGeobuf writer (prompt 111), read back with the same library: the header, the columns, the
 * geometry type ({@code Unknown} when mixed), the packed Hilbert R-tree and the features in index
 * order, and the index-less file when a record has no geometry.
 */
class FlatGeobufWriterTest {

    @TempDir Path dir;

    private ByteBuffer write(List<Map<String, Object>> rows) throws Exception {
        Path file = dir.resolve("out.fgb");
        try (FlatGeobufWriter writer = new FlatGeobufWriter()) {
            writer.open(file, ExportSchema.standard(List.of(), true),
                    Map.of("name", "test", "licence", "{\"licenses\":[]}", "attribution", "Test data"));
            for (Map<String, Object> row : rows) {
                writer.write(row);
            }
            assertThat(writer.rows()).isEqualTo(rows.size());
        }
        try (var listing = Files.list(dir)) {
            assertThat(listing.map(p -> p.getFileName().toString())).containsExactly("out.fgb");
        }
        return ByteBuffer.wrap(Files.readAllBytes(file)).order(ByteOrder.LITTLE_ENDIAN);
    }

    /** The features of a file in file order, decoded: geometry and properties by column name. */
    private static List<Map<String, Object>> features(ByteBuffer bb, HeaderMeta header) {
        long treeSize = header.indexNodeSize > 0 ? PackedRTree.calcSize((int) header.featuresCount, header.indexNodeSize) : 0;
        int position = header.offset + (int) treeSize;
        List<Map<String, Object>> out = new ArrayList<>();
        for (int k = 0; k < header.featuresCount; k++) {
            int length = bb.getInt(position);
            ByteBuffer slice = bb.duplicate().position(position + 4).slice().order(ByteOrder.LITTLE_ENDIAN);
            out.add(decode(Feature.getRootAsFeature(slice), header));
            position += 4 + length;
        }
        return out;
    }

    private static Map<String, Object> decode(Feature feature, HeaderMeta header) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (feature.geometry() != null) {
            int type = header.geometryType == GeometryType.Unknown ? feature.geometry().type() : header.geometryType;
            out.put("geometry", GeometryConversions.deserialize(feature.geometry(), type));
        }
        ByteBuffer properties = feature.propertiesAsByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
        while (properties.remaining() > 0) {
            int index = Short.toUnsignedInt(properties.getShort());
            ColumnMeta column = header.columns.get(index);
            switch (column.type) {
                case ColumnType.Long -> out.put(column.name, properties.getLong());
                case ColumnType.Int -> out.put(column.name, properties.getInt());
                case ColumnType.Double -> out.put(column.name, properties.getDouble());
                default -> {
                    byte[] bytes = new byte[properties.getInt()];
                    properties.get(bytes);
                    out.put(column.name, new String(bytes, StandardCharsets.UTF_8));
                }
            }
        }
        return out;
    }

    @Test
    @DisplayName("a mixed file declares Unknown, carries the columns and an index that finds the features")
    void mixedWithIndex() throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            rows.add(row("src", "e" + i, "k", i, i % 4 == 0 ? line(i, i / 2.0) : point(i, i / 2.0)));
        }
        ByteBuffer bb = write(rows);
        HeaderMeta header = HeaderMeta.read(bb.duplicate().order(ByteOrder.LITTLE_ENDIAN));
        assertThat(header.featuresCount).isEqualTo(40);
        assertThat(header.geometryType).isEqualTo((byte) GeometryType.Unknown);
        assertThat(header.indexNodeSize).isEqualTo(FlatGeobufWriter.NODE_SIZE);
        assertThat(header.srid).isEqualTo(4326);
        assertThat(header.envelope).isEqualTo(new Envelope(0, 39, 0, 19.5));
        assertThat(header.columns).extracting(c -> c.name).containsExactly("source_id", "external_id", "kind", "seq",
                "version", "observed_at", "event_time", "valid_until", "ingested_at", "title", "payload");
        assertThat(header.columns.get(3).type).isEqualTo((byte) ColumnType.Long);
        assertThat(header.columns.get(5).type).isEqualTo((byte) ColumnType.DateTime);
        assertThat(header.columns.get(10).type).isEqualTo((byte) ColumnType.Json);

        ByteBuffer headerBuffer = bb.duplicate().position(12).slice().order(ByteOrder.LITTLE_ENDIAN);
        Header raw = Header.getRootAsHeader(headerBuffer);
        assertThat(raw.description()).isEqualTo("Test data");
        assertThat(raw.metadata()).isEqualTo("{\"licenses\":[]}");

        List<Map<String, Object>> features = features(bb, header);
        assertThat(features).hasSize(40);
        assertThat(features).extracting(f -> f.get("external_id")).doesNotHaveDuplicates();
        Map<String, Object> e8 = features.stream().filter(f -> "e8".equals(f.get("external_id"))).findFirst().orElseThrow();
        assertThat(((Geometry) e8.get("geometry")).getGeometryType()).isEqualTo("LineString");
        assertThat(e8.get("seq")).isEqualTo(8L);
        assertThat(e8.get("version")).isEqualTo(1);
        assertThat((String) e8.get("observed_at")).startsWith("2026-03-01T12:00:08");

        var hits = PackedRTree.search(bb, header.offset, (int) header.featuresCount, header.indexNodeSize,
                new Envelope(9.5, 12.5, 4.5, 6.5));
        long treeSize = PackedRTree.calcSize((int) header.featuresCount, header.indexNodeSize);
        List<Object> found = new ArrayList<>();
        for (var hit : hits) {
            int position = header.offset + (int) treeSize + (int) hit.offset;
            ByteBuffer slice = bb.duplicate().position(position + 4).slice().order(ByteOrder.LITTLE_ENDIAN);
            found.add(decode(Feature.getRootAsFeature(slice), header).get("external_id"));
        }
        assertThat(found).contains("e10", "e11", "e12");
    }

    @Test
    @DisplayName("one geometry type throughout is declared in the header")
    void singleType() throws Exception {
        ByteBuffer bb = write(List.of(row("s", "a", "k", 1, point(1, 1)), row("s", "b", "k", 2, point(2, 2))));
        HeaderMeta header = HeaderMeta.read(bb.duplicate().order(ByteOrder.LITTLE_ENDIAN));
        assertThat(header.geometryType).isEqualTo((byte) GeometryType.Point);
        assertThat(features(bb, header)).extracting(f -> ((Geometry) f.get("geometry")).getGeometryType())
                .containsOnly("Point");
    }

    @Test
    @DisplayName("a record without a geometry keeps every row and leaves the index out")
    void nullGeometryNoIndex() throws Exception {
        ByteBuffer bb = write(List.of(row("s", "a", "k", 1, point(1, 1)), row("s", "b", "k", 2, null)));
        HeaderMeta header = HeaderMeta.read(bb.duplicate().order(ByteOrder.LITTLE_ENDIAN));
        assertThat(header.indexNodeSize).isZero();
        List<Map<String, Object>> features = features(bb, header);
        assertThat(features).extracting(f -> f.get("external_id")).containsExactly("a", "b");
        assertThat(features.get(1)).doesNotContainKey("geometry");
    }

    @Test
    @DisplayName("level bounds: leaves last, root first, one root over a single feature")
    void levelBounds() {
        long[][] one = FlatGeobufWriter.levelBounds(1, 16);
        assertThat(one).hasDimensions(2, 2);
        assertThat(one[0]).containsExactly(1, 2);
        assertThat(one[1]).containsExactly(0, 1);
        long[][] many = FlatGeobufWriter.levelBounds(40, 16);
        assertThat(many[0]).containsExactly(4, 44);
        assertThat(many[1]).containsExactly(1, 4);
        assertThat(many[2]).containsExactly(0, 1);
        assertThat(PackedRTree.calcSize(40, 16)).isEqualTo(44L * 40);
        assertThat(FlatGeobufWriter.hilbert(0, 0)).isZero();
        assertThat(FlatGeobufWriter.hilbert(0xFFFF, 0)).isNotEqualTo(FlatGeobufWriter.hilbert(0, 0xFFFF));
    }

    @Test
    @DisplayName("an empty export is a valid file with no features")
    void empty() throws Exception {
        ByteBuffer bb = write(List.of());
        HeaderMeta header = HeaderMeta.read(bb.duplicate().order(ByteOrder.LITTLE_ENDIAN));
        assertThat(header.featuresCount).isZero();
        assertThat(header.indexNodeSize).isZero();
    }
}
