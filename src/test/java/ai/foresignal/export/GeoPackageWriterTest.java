package ai.foresignal.export;

import static ai.foresignal.export.ExportRowsFixture.point;
import static ai.foresignal.export.ExportRowsFixture.row;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKBReader;

/**
 * The GeoPackage 1.4 writer (prompt 111), opened afterwards with plain SQLite (sqlite-jdbc): the
 * application id and version, the required tables and rows, one feature table per kind, the
 * GeoPackageBinary header, the populated RTree, its triggers and the license metadata.
 */
class GeoPackageWriterTest {

    @TempDir Path dir;

    private Path write(List<Map<String, Object>> rows) throws Exception {
        Path file = dir.resolve("out.gpkg");
        try (GeoPackageWriter writer = new GeoPackageWriter()) {
            writer.open(file, ExportSchema.standard(List.of("order"), true),
                    Map.of("licence", "{\"licenses\":[{\"terms\":{\"id\":\"CC-BY-4.0\"}}]}"));
            for (Map<String, Object> row : rows) {
                writer.write(row);
            }
            assertThat(writer.rows()).isEqualTo(rows.size());
        }
        return file;
    }

    private static List<Map<String, Object>> rows() {
        Map<String, Object> a = row("src", "a", "aircraft", 1, point(10, 50));
        a.put("order", "first");
        Map<String, Object> b = row("src", "b", "aircraft", 2, point(11, 51));
        Map<String, Object> c = row("src", "c", "select", 3, point(-5, -6));
        Map<String, Object> d = row("src", "d", "select", 4, null);
        return List.of(a, b, c, d);
    }

    private static long count(Statement statement, String sql) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    @DisplayName("application_id is GPKG, user_version 10400, and the required tables and rows exist")
    void requiredTables() throws Exception {
        Path file = write(rows());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
                Statement statement = connection.createStatement()) {
            assertThat(count(statement, "PRAGMA application_id")).isEqualTo(0x47504B47L);
            assertThat(count(statement, "PRAGMA user_version")).isEqualTo(10400L);
            List<Long> srs = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("SELECT srs_id FROM gpkg_spatial_ref_sys ORDER BY srs_id")) {
                while (rs.next()) {
                    srs.add(rs.getLong(1));
                }
            }
            assertThat(srs).containsExactly(-1L, 0L, 4326L);
            try (ResultSet rs = statement.executeQuery("SELECT table_name, data_type, srs_id, min_x, min_y, max_x, max_y"
                    + " FROM gpkg_contents ORDER BY table_name")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("aircraft");
                assertThat(rs.getString(2)).isEqualTo("features");
                assertThat(rs.getLong(3)).isEqualTo(4326L);
                assertThat(rs.getDouble(4)).isEqualTo(10.0);
                assertThat(rs.getDouble(7)).isEqualTo(51.0);
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("select");
            }
            assertThat(count(statement, "SELECT count(*) FROM gpkg_geometry_columns WHERE column_name = 'geom'"
                    + " AND geometry_type_name = 'GEOMETRY' AND srs_id = 4326")).isEqualTo(2);
            try (ResultSet rs = statement.executeQuery("PRAGMA integrity_check")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("ok");
            }
        }
    }

    @Test
    @DisplayName("feature tables hold the columns, ISO timestamps and payload; keyword names are quoted")
    void featureTables() throws Exception {
        Path file = write(rows());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
                Statement statement = connection.createStatement()) {
            assertThat(count(statement, "SELECT count(*) FROM \"aircraft\"")).isEqualTo(2);
            assertThat(count(statement, "SELECT count(*) FROM \"select\"")).isEqualTo(2);
            try (ResultSet rs = statement.executeQuery("SELECT external_id, seq, observed_at, \"order\", payload,"
                    + " title FROM \"aircraft\" ORDER BY fid")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("a");
                assertThat(rs.getLong(2)).isEqualTo(1L);
                assertThat(rs.getString(3)).isEqualTo("2026-03-01T12:00:01.123Z");
                assertThat(rs.getString(4)).isEqualTo("first");
                assertThat(rs.getString(5)).contains("\"v\":1");
                assertThat(rs.getString(6)).isEqualTo("Title <a> & co");
            }
            assertThat(count(statement, "SELECT count(*) FROM \"select\" WHERE geom IS NULL")).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("geometries are GeoPackageBinary: GP, version 0, flags 0x03, srs 4326, envelope, then the WKB")
    void geoPackageBinary() throws Exception {
        Path file = write(rows());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT geom FROM \"aircraft\" ORDER BY fid LIMIT 1")) {
            assertThat(rs.next()).isTrue();
            byte[] blob = rs.getBytes(1);
            assertThat(blob[0]).isEqualTo((byte) 'G');
            assertThat(blob[1]).isEqualTo((byte) 'P');
            assertThat(blob[2]).isEqualTo((byte) 0);
            assertThat(blob[3]).isEqualTo((byte) 0x03);
            ByteBuffer header = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN);
            assertThat(header.getInt(4)).isEqualTo(4326);
            assertThat(header.getDouble(8)).isEqualTo(10.0);
            assertThat(header.getDouble(16)).isEqualTo(10.0);
            assertThat(header.getDouble(24)).isEqualTo(50.0);
            assertThat(header.getDouble(32)).isEqualTo(50.0);
            byte[] wkb = java.util.Arrays.copyOfRange(blob, 40, blob.length);
            assertThat(new WKBReader().read(wkb).getCoordinate()).isEqualTo(new Coordinate(10, 50));
        }
    }

    @Test
    @DisplayName("the RTree is populated from Java and its extension row and triggers are present")
    void rtree() throws Exception {
        Path file = write(rows());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
                Statement statement = connection.createStatement()) {
            assertThat(count(statement, "SELECT count(*) FROM \"rtree_aircraft_geom\"")).isEqualTo(2);
            assertThat(count(statement, "SELECT count(*) FROM \"rtree_select_geom\"")).isEqualTo(1);
            assertThat(count(statement, "SELECT count(*) FROM \"rtree_aircraft_geom\" WHERE minx >= 10.5")).isEqualTo(1);
            assertThat(count(statement, "SELECT count(*) FROM gpkg_extensions WHERE extension_name = 'gpkg_rtree_index'"
                    + " AND column_name = 'geom' AND scope = 'write-only'")).isEqualTo(2);
            assertThat(count(statement, "SELECT count(*) FROM sqlite_master WHERE type = 'trigger'"
                    + " AND name LIKE 'rtree_aircraft_geom_%'")).isEqualTo(6);
        }
    }

    @Test
    @DisplayName("the license summary is in gpkg_metadata with a geopackage-scope reference and its extension rows")
    void metadata() throws Exception {
        Path file = write(rows());
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file);
                Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT md_scope, mime_type, metadata FROM gpkg_metadata")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("dataset");
                assertThat(rs.getString(2)).isEqualTo("application/json");
                assertThat(rs.getString(3)).contains("CC-BY-4.0");
            }
            assertThat(count(statement, "SELECT count(*) FROM gpkg_metadata_reference WHERE reference_scope ="
                    + " 'geopackage' AND md_file_id = 1")).isEqualTo(1);
            assertThat(count(statement, "SELECT count(*) FROM gpkg_extensions WHERE extension_name = 'gpkg_metadata'"))
                    .isEqualTo(2);
        }
    }

    @Test
    @DisplayName("helpers: identifier quoting, DATETIME text, the binary header and the trigger set")
    void helpers() {
        assertThat(GeoPackageWriter.quote("a\"b")).isEqualTo("\"a\"\"b\"");
        assertThat(GeoPackageWriter.dateTime(1_000_000L)).isEqualTo("1970-01-01T00:00:01Z");
        assertThat(GeoPackageWriter.dateTime("as-is")).isEqualTo("as-is");
        byte[] blob = GeoPackageWriter.geoPackageBinary(new byte[] {1, 2}, new double[] {1, 2, 3, 4});
        assertThat(blob).hasSize(42);
        assertThat(GeoPackageWriter.rtreeTriggers("t", "geom")).hasSize(6)
                .allMatch(sql -> sql.startsWith("CREATE TRIGGER \"rtree_t_geom_"));
    }
}
