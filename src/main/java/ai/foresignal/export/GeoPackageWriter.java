package ai.foresignal.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An OGC GeoPackage 1.4 writer (prompt 111; {@code application_id 0x47504B47}, {@code user_version
 * 10400}): the required {@code gpkg_spatial_ref_sys}/{@code gpkg_contents}/{@code gpkg_geometry_columns}
 * rows, one feature table per kind with the flattened columns and {@code payload} TEXT, geometries as
 * GeoPackageBinary (envelope type 1, srs 4326), the RTree spatial index, and the licence in
 * {@code gpkg_metadata}.
 */
public final class GeoPackageWriter implements ExportWriter {

    private static final int APP_ID = 0x47504B47;
    private static final int USER_VERSION = 10400;

    private Connection connection;
    private ExportSchema schema;
    private final Map<String, PreparedStatement> inserts = new LinkedHashMap<>();
    private final Map<String, double[]> extents = new LinkedHashMap<>();
    private long rows;

    @Override
    public void open(Path file, ExportSchema schema, Map<String, String> metadata) throws IOException {
        this.schema = schema;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA application_id = " + APP_ID);
                st.execute("PRAGMA user_version = " + USER_VERSION);
                st.execute("CREATE TABLE gpkg_spatial_ref_sys (srs_name TEXT NOT NULL, srs_id INTEGER PRIMARY KEY,"
                        + " organization TEXT NOT NULL, organization_coordsys_id INTEGER NOT NULL,"
                        + " definition TEXT NOT NULL, description TEXT)");
                st.execute("CREATE TABLE gpkg_contents (table_name TEXT NOT NULL PRIMARY KEY,"
                        + " data_type TEXT NOT NULL, identifier TEXT UNIQUE, description TEXT DEFAULT '',"
                        + " last_change DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),"
                        + " min_x DOUBLE, min_y DOUBLE, max_x DOUBLE, max_y DOUBLE, srs_id INTEGER)");
                st.execute("CREATE TABLE gpkg_geometry_columns (table_name TEXT NOT NULL, column_name TEXT NOT NULL,"
                        + " geometry_type_name TEXT NOT NULL, srs_id INTEGER NOT NULL, z TINYINT NOT NULL,"
                        + " m TINYINT NOT NULL, CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name))");
                st.execute("CREATE TABLE gpkg_extensions (table_name TEXT, column_name TEXT, extension_name TEXT NOT"
                        + " NULL, definition TEXT NOT NULL, scope TEXT NOT NULL, CONSTRAINT ge_tce UNIQUE"
                        + " (table_name, column_name, extension_name))");
                st.execute("CREATE TABLE gpkg_metadata (id INTEGER PRIMARY KEY AUTOINCREMENT, md_scope TEXT NOT NULL"
                        + " DEFAULT 'dataset', md_standard_uri TEXT NOT NULL, mime_type TEXT NOT NULL DEFAULT"
                        + " 'text/xml', metadata TEXT NOT NULL)");
                st.execute("CREATE TABLE gpkg_metadata_reference (reference_scope TEXT NOT NULL, table_name TEXT,"
                        + " column_name TEXT, row_id_value INTEGER, timestamp DATETIME NOT NULL DEFAULT"
                        + " (strftime('%Y-%m-%dT%H:%M:%fZ','now')), md_file_id INTEGER NOT NULL, md_parent_id"
                        + " INTEGER, CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id))");
                st.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('Undefined Cartesian SRS', -1, 'NONE', -1,"
                        + " 'undefined', NULL)");
                st.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('Undefined geographic SRS', 0, 'NONE', 0,"
                        + " 'undefined', NULL)");
                st.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('WGS 84 geodetic', 4326, 'EPSG', 4326,"
                        + " 'GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563]],"
                        + "PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]]', NULL)");
            }
            String licence = metadata.getOrDefault("licence", "unknown");
            try (PreparedStatement md = connection.prepareStatement(
                    "INSERT INTO gpkg_metadata (md_scope, md_standard_uri, mime_type, metadata) VALUES ('dataset',"
                            + " 'http://foresignal.ai', 'text/plain', ?)")) {
                md.setString(1, licence);
                md.executeUpdate();
            }
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IOException("Could not create the GeoPackage", e);
        }
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        String kind = String.valueOf(row.getOrDefault("kind", "feature"));
        try {
            ensureTable(kind);
            PreparedStatement insert = inserts.get(kind);
            int index = 1;
            for (ExportSchema.Column column : schema.columns()) {
                setValue(insert, index++, column.type(), row.get(column.name()));
            }
            byte[] geom = row.get("geometry") instanceof byte[] b ? b : null;
            double[] bbox = row.get("bbox") instanceof double[] d ? d : null;
            if (schema.geometry()) {
                insert.setBytes(index++, geom == null ? null : geoPackageBinary(geom, bbox));
                if (bbox != null) {
                    merge(kind, bbox);
                }
            }
            insert.setString(index, row.get("payload") == null ? null : String.valueOf(row.get("payload")));
            insert.addBatch();
            if (++rows % 1000 == 0) {
                insert.executeBatch();
            }
        } catch (SQLException e) {
            throw new IOException("Could not write a GeoPackage row", e);
        }
    }

    private void ensureTable(String kind) throws SQLException {
        if (inserts.containsKey(kind)) {
            return;
        }
        String table = "fs_" + kind.replaceAll("[^A-Za-z0-9_]", "_").toLowerCase(Locale.ROOT);
        StringBuilder create = new StringBuilder("CREATE TABLE ").append(table).append(" (fid INTEGER PRIMARY KEY"
                + " AUTOINCREMENT, geom BLOB");
        for (ExportSchema.Column column : schema.columns()) {
            if (column.name().equals("payload")) {
                continue;
            }
            create.append(", ").append(column.name()).append(' ').append(sqlType(column.type()));
        }
        create.append(", payload TEXT)");
        try (Statement st = connection.createStatement()) {
            st.execute(create.toString());
            st.execute("INSERT INTO gpkg_contents (table_name, data_type, identifier, srs_id) VALUES ('" + table
                    + "', 'features', '" + table + "', 4326)");
            st.execute("INSERT INTO gpkg_geometry_columns VALUES ('" + table + "', 'geom', 'GEOMETRY', 4326, 0, 0)");
            st.execute("INSERT INTO gpkg_extensions VALUES ('" + table + "', 'geom', 'gpkg_rtree_index',"
                    + " 'http://www.geopackage.org/spec120/#extension_rtree', 'write-only')");
            st.execute("CREATE VIRTUAL TABLE rtree_" + table + "_geom USING rtree(id, minx, maxx, miny, maxy)");
            st.execute("CREATE TRIGGER rtree_" + table + "_geom_insert AFTER INSERT ON " + table
                    + " WHEN (new.geom NOT NULL) BEGIN INSERT OR REPLACE INTO rtree_" + table
                    + "_geom VALUES (new.fid, ST_MinX(new.geom), ST_MaxX(new.geom), ST_MinY(new.geom),"
                    + " ST_MaxY(new.geom)); END");
        }
        List<String> columns = new ArrayList<>();
        for (ExportSchema.Column column : schema.columns()) {
            columns.add(column.name());
        }
        columns.add("geom");
        columns.add("payload");
        String placeholders = String.join(",", columns.stream().map(c -> "?").toList());
        inserts.put(kind, connection.prepareStatement("INSERT INTO " + table + " ("
                + String.join(",", columns) + ") VALUES (" + placeholders + ")"));
    }

    private static String sqlType(ExportSchema.Type type) {
        return switch (type) {
            case INT64 -> "INTEGER";
            case INT32 -> "INTEGER";
            case DOUBLE -> "REAL";
            default -> "TEXT";
        };
    }

    private static void setValue(PreparedStatement ps, int index, ExportSchema.Type type, Object value)
            throws SQLException {
        if (value == null) {
            ps.setObject(index, null);
            return;
        }
        switch (type) {
            case INT64, INT32 -> ps.setLong(index, ((Number) value).longValue());
            case DOUBLE -> ps.setDouble(index, ((Number) value).doubleValue());
            case TIMESTAMP_MICROS -> ps.setString(index,
                    Instant.ofEpochSecond(((Number) value).longValue() / 1_000_000L,
                            (((Number) value).longValue() % 1_000_000L) * 1_000L).toString());
            default -> ps.setString(index, String.valueOf(value));
        }
    }

    private void merge(String kind, double[] b) {
        double[] current = extents.computeIfAbsent(kind, k -> new double[] {b[0], b[1], b[2], b[3]});
        current[0] = Math.min(current[0], b[0]);
        current[1] = Math.min(current[1], b[1]);
        current[2] = Math.max(current[2], b[2]);
        current[3] = Math.max(current[3], b[3]);
    }

    /** The GeoPackageBinary header (GP, version 0, little-endian, envelope type 1) plus the WKB. */
    static byte[] geoPackageBinary(byte[] wkb, double[] bbox) {
        double[] box = bbox == null ? new double[] {0, 0, 0, 0} : bbox;
        ByteBuffer buffer = ByteBuffer.allocate(8 + 32 + wkb.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 'G').put((byte) 'P').put((byte) 0).put((byte) 1); // envelope type 1, little-endian
        buffer.putInt(4326);
        buffer.putDouble(box[0]).putDouble(box[2]).putDouble(box[1]).putDouble(box[3]);
        buffer.put(wkb);
        return buffer.array();
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        try {
            for (PreparedStatement insert : inserts.values()) {
                insert.executeBatch();
                insert.close();
            }
            for (Map.Entry<String, double[]> e : extents.entrySet()) {
                String table = "fs_" + e.getKey().replaceAll("[^A-Za-z0-9_]", "_").toLowerCase(Locale.ROOT);
                try (PreparedStatement ps = connection.prepareStatement("UPDATE gpkg_contents SET min_x=?, min_y=?,"
                        + " max_x=?, max_y=? WHERE table_name=?")) {
                    ps.setDouble(1, e.getValue()[0]);
                    ps.setDouble(2, e.getValue()[1]);
                    ps.setDouble(3, e.getValue()[2]);
                    ps.setDouble(4, e.getValue()[3]);
                    ps.setString(5, table);
                    ps.executeUpdate();
                }
            }
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            throw new IOException("Could not close the GeoPackage", e);
        }
    }
}
