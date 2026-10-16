package ai.foresignal.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An OGC GeoPackage 1.4 writer (prompt 111) on {@code org.xerial:sqlite-jdbc}.
 *
 * <ul>
 *   <li>{@code PRAGMA application_id = 0x47504B47} ("GPKG") and {@code user_version = 10400};
 *   <li>the required {@code gpkg_spatial_ref_sys} rows (4326, −1 and 0), {@code gpkg_contents} and
 *       {@code gpkg_geometry_columns};
 *   <li>one feature table per record kind: {@code fid}, a {@code geom} GEOMETRY column, the
 *       {@link ExportSchema} columns (text, integers, ISO 8601 {@code DATETIME}) and {@code payload};
 *   <li>geometries as GeoPackageBinary: {@code "GP"}, version 0, flags {@code 0x03} (little-endian,
 *       envelope type 1 {@code [minx, maxx, miny, maxy]}), {@code srs_id} 4326, the envelope, then the
 *       WKB;
 *   <li>the RTree spatial index extension ({@code gpkg_rtree_index}): the virtual table is filled from
 *       Java as rows are written, and the spec's maintenance triggers are created at close, after
 *       every row, because they call {@code ST_*} functions that plain SQLite lacks and that GeoPackage
 *       readers (GDAL, QGIS) provide;
 *   <li>the license summary in {@code gpkg_metadata} with a geopackage-scope
 *       {@code gpkg_metadata_reference} row, registered as the {@code gpkg_metadata} extension.
 * </ul>
 *
 * <p>Every identifier is double-quoted, so kinds and flattened field names may be SQL keywords.
 * Rows are committed in batches of {@value #BATCH}.
 */
public final class GeoPackageWriter implements ExportWriter {

    /** {@code "GPKG"} as the SQLite application id. */
    public static final int APPLICATION_ID = 0x47504B47;

    /** GeoPackage 1.4.0. */
    public static final int USER_VERSION = 10400;

    /** The GeoPackageBinary flags byte: little-endian (bit 0) and envelope type 1 (bits 1–3). */
    public static final byte FLAGS = 0x03;

    /** The flags byte of an empty geometry: {@link #FLAGS} plus the empty bit (bit 4). */
    public static final byte FLAGS_EMPTY = 0x13;

    /** The geometry column of every feature table. */
    public static final String GEOMETRY_COLUMN = "geom";

    /** The rtree extension's definition URL. */
    static final String RTREE_DEFINITION = "http://www.geopackage.org/spec/#extension_rtree";

    /** The metadata extension's definition URL. */
    static final String METADATA_DEFINITION = "http://www.geopackage.org/spec/#extension_metadata";

    private static final int BATCH = 10_000;
    private static final DateTimeFormatter ISO_MILLIS = DateTimeFormatter.ISO_INSTANT;

    private Connection connection;
    private ExportSchema schema;
    private Map<String, String> metadata;
    private final Map<String, FeatureTable> tables = new LinkedHashMap<>();
    private long rows;
    private int pending;
    private boolean closed;

    /** One feature table and its running extent. */
    private static final class FeatureTable {
        final String name;
        final PreparedStatement insert;
        final PreparedStatement rtree;
        double minX = Double.NaN;
        double minY = Double.NaN;
        double maxX = Double.NaN;
        double maxY = Double.NaN;

        FeatureTable(String name, PreparedStatement insert, PreparedStatement rtree) {
            this.name = name;
            this.insert = insert;
            this.rtree = rtree;
        }

        void expand(double[] b) {
            minX = Double.isNaN(minX) ? b[0] : Math.min(minX, b[0]);
            minY = Double.isNaN(minY) ? b[1] : Math.min(minY, b[1]);
            maxX = Double.isNaN(maxX) ? b[2] : Math.max(maxX, b[2]);
            maxY = Double.isNaN(maxY) ? b[3] : Math.max(maxY, b[3]);
        }
    }

    @Override
    public void open(Path file, ExportSchema exportSchema, Map<String, String> meta) throws IOException {
        this.schema = exportSchema;
        this.metadata = meta == null ? Map.of() : meta;
        Files.deleteIfExists(file);
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA application_id = " + APPLICATION_ID);
                statement.execute("PRAGMA user_version = " + USER_VERSION);
                statement.execute("PRAGMA journal_mode = OFF");
                statement.execute("PRAGMA synchronous = OFF");
                createCoreTables(statement);
            }
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IOException("Could not create the GeoPackage " + file, e);
        }
    }

    private static void createCoreTables(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE gpkg_spatial_ref_sys (srs_name TEXT NOT NULL, srs_id INTEGER PRIMARY KEY,"
                + " organization TEXT NOT NULL, organization_coordsys_id INTEGER NOT NULL, definition TEXT NOT NULL,"
                + " description TEXT)");
        statement.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('WGS 84 geodetic', 4326, 'EPSG', 4326,"
                + " 'GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,"
                + "AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,"
                + "AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],"
                + "AXIS[\"Latitude\",NORTH],AXIS[\"Longitude\",EAST],AUTHORITY[\"EPSG\",\"4326\"]]',"
                + " 'longitude/latitude coordinates in decimal degrees on the WGS 84 spheroid')");
        statement.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('Undefined cartesian SRS', -1, 'NONE', -1,"
                + " 'undefined', 'undefined cartesian coordinate reference system')");
        statement.execute("INSERT INTO gpkg_spatial_ref_sys VALUES ('Undefined geographic SRS', 0, 'NONE', 0,"
                + " 'undefined', 'undefined geographic coordinate reference system')");
        statement.execute("CREATE TABLE gpkg_contents (table_name TEXT NOT NULL PRIMARY KEY, data_type TEXT NOT NULL,"
                + " identifier TEXT UNIQUE, description TEXT DEFAULT '', last_change DATETIME NOT NULL DEFAULT"
                + " (strftime('%Y-%m-%dT%H:%M:%fZ','now')), min_x DOUBLE, min_y DOUBLE, max_x DOUBLE, max_y DOUBLE,"
                + " srs_id INTEGER, CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES"
                + " gpkg_spatial_ref_sys(srs_id))");
        statement.execute("CREATE TABLE gpkg_geometry_columns (table_name TEXT NOT NULL, column_name TEXT NOT NULL,"
                + " geometry_type_name TEXT NOT NULL, srs_id INTEGER NOT NULL, z TINYINT NOT NULL, m TINYINT NOT NULL,"
                + " CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name),"
                + " CONSTRAINT uk_gc_table_name UNIQUE (table_name),"
                + " CONSTRAINT fk_gc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name),"
                + " CONSTRAINT fk_gc_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id))");
        statement.execute("CREATE TABLE gpkg_extensions (table_name TEXT, column_name TEXT, extension_name TEXT NOT NULL,"
                + " definition TEXT NOT NULL, scope TEXT NOT NULL,"
                + " CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))");
        statement.execute("CREATE TABLE gpkg_metadata (id INTEGER CONSTRAINT m_pk PRIMARY KEY ASC NOT NULL,"
                + " md_scope TEXT NOT NULL DEFAULT 'dataset', md_standard_uri TEXT NOT NULL, mime_type TEXT NOT NULL"
                + " DEFAULT 'text/xml', metadata TEXT NOT NULL DEFAULT '')");
        statement.execute("CREATE TABLE gpkg_metadata_reference (reference_scope TEXT NOT NULL, table_name TEXT,"
                + " column_name TEXT, row_id_value INTEGER, timestamp DATETIME NOT NULL DEFAULT"
                + " (strftime('%Y-%m-%dT%H:%M:%fZ','now')), md_file_id INTEGER NOT NULL, md_parent_id INTEGER,"
                + " CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id),"
                + " CONSTRAINT crmr_mpi_fk FOREIGN KEY (md_parent_id) REFERENCES gpkg_metadata(id))");
    }

    @Override
    public void write(Map<String, Object> row) throws IOException {
        String kind = String.valueOf(row.getOrDefault("kind", "record"));
        try {
            FeatureTable table = tables.get(kind);
            if (table == null) {
                table = createFeatureTable(kind);
                tables.put(kind, table);
            }
            PreparedStatement insert = table.insert;
            int index = 1;
            byte[] wkb = row.get(GEOMETRY) instanceof byte[] w && w.length > 0 ? w : null;
            double[] box = row.get(BBOX) instanceof double[] b && b.length == 4 ? b : null;
            if (wkb != null && box != null) {
                insert.setBytes(index++, geoPackageBinary(wkb, box));
            } else {
                insert.setNull(index++, Types.BLOB);
            }
            for (ExportSchema.Column column : schema.columns()) {
                setValue(insert, index++, column.type(), row.get(column.name()));
            }
            insert.executeUpdate();
            if (wkb != null && box != null) {
                long fid;
                try (var keys = insert.getGeneratedKeys()) {
                    keys.next();
                    fid = keys.getLong(1);
                }
                table.rtree.setLong(1, fid);
                table.rtree.setDouble(2, box[0]);
                table.rtree.setDouble(3, box[2]);
                table.rtree.setDouble(4, box[1]);
                table.rtree.setDouble(5, box[3]);
                table.rtree.executeUpdate();
                table.expand(box);
            }
            rows++;
            if (++pending >= BATCH) {
                connection.commit();
                pending = 0;
            }
        } catch (SQLException e) {
            throw new IOException("Could not write a GeoPackage row", e);
        }
    }

    private FeatureTable createFeatureTable(String kind) throws SQLException {
        String table = quote(kind);
        StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(table)
                .append(" (\"fid\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, ")
                .append(quote(GEOMETRY_COLUMN)).append(" GEOMETRY");
        List<String> names = new ArrayList<>();
        names.add(quote(GEOMETRY_COLUMN));
        for (ExportSchema.Column column : schema.columns()) {
            ddl.append(", ").append(quote(column.name())).append(' ').append(sqlType(column.type()));
            names.add(quote(column.name()));
        }
        ddl.append(')');
        String rtreeName = "rtree_" + kind + "_" + GEOMETRY_COLUMN;
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl.toString());
            statement.execute("CREATE VIRTUAL TABLE " + quote(rtreeName) + " USING rtree(id, minx, maxx, miny, maxy)");
        }
        try (PreparedStatement contents = connection.prepareStatement("INSERT INTO gpkg_contents (table_name, data_type,"
                + " identifier, description, last_change, srs_id) VALUES (?, 'features', ?, ?, ?, 4326)")) {
            contents.setString(1, kind);
            contents.setString(2, kind);
            contents.setString(3, "Foresignal records of kind " + kind);
            contents.setString(4, ISO_MILLIS.format(Instant.now().truncatedTo(ChronoUnit.MILLIS)));
            contents.executeUpdate();
        }
        try (PreparedStatement columns = connection.prepareStatement(
                "INSERT INTO gpkg_geometry_columns VALUES (?, ?, 'GEOMETRY', 4326, 0, 0)")) {
            columns.setString(1, kind);
            columns.setString(2, GEOMETRY_COLUMN);
            columns.executeUpdate();
        }
        try (PreparedStatement extension = connection.prepareStatement(
                "INSERT INTO gpkg_extensions VALUES (?, ?, 'gpkg_rtree_index', ?, 'write-only')")) {
            extension.setString(1, kind);
            extension.setString(2, GEOMETRY_COLUMN);
            extension.setString(3, RTREE_DEFINITION);
            extension.executeUpdate();
        }
        String placeholders = String.join(", ", names.stream().map(n -> "?").toList());
        PreparedStatement insert = connection.prepareStatement("INSERT INTO " + table + " (" + String.join(", ", names)
                + ") VALUES (" + placeholders + ")", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement rtree = connection.prepareStatement("INSERT INTO " + quote(rtreeName) + " VALUES (?, ?, ?, ?, ?)");
        return new FeatureTable(kind, insert, rtree);
    }

    private static void setValue(PreparedStatement statement, int index, ExportSchema.Type type, Object value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, type == ExportSchema.Type.INT64 || type == ExportSchema.Type.INT32
                    ? Types.INTEGER : type == ExportSchema.Type.DOUBLE ? Types.DOUBLE : Types.VARCHAR);
            return;
        }
        switch (type) {
            case INT64, INT32 -> statement.setLong(index, ((Number) value).longValue());
            case DOUBLE -> statement.setDouble(index, ((Number) value).doubleValue());
            case TIMESTAMP_MICROS -> statement.setString(index, dateTime(value));
            case STRING -> statement.setString(index, String.valueOf(value));
        }
    }

    /** A GeoPackage {@code DATETIME}: ISO 8601 UTC with a {@code Z} suffix. */
    static String dateTime(Object value) {
        if (value instanceof Instant instant) {
            return ISO_MILLIS.format(instant.truncatedTo(ChronoUnit.MILLIS));
        }
        if (value instanceof Number micros) {
            long m = micros.longValue();
            return ISO_MILLIS.format(Instant.ofEpochSecond(Math.floorDiv(m, 1_000_000L),
                    Math.floorMod(m, 1_000_000L) * 1_000L).truncatedTo(ChronoUnit.MILLIS));
        }
        return String.valueOf(value);
    }

    private static String sqlType(ExportSchema.Type type) {
        return switch (type) {
            case STRING -> "TEXT";
            case INT64, INT32 -> "INTEGER";
            case DOUBLE -> "DOUBLE";
            case TIMESTAMP_MICROS -> "DATETIME";
        };
    }

    /**
     * The GeoPackageBinary blob of {@code wkb}: magic {@code GP}, version 0, flags {@link #FLAGS},
     * {@code srs_id} 4326 and the envelope {@code [minx, maxx, miny, maxy]} (little-endian), then the
     * WKB unchanged.
     *
     * @param box the envelope as {@code [xmin, ymin, xmax, ymax]}
     */
    public static byte[] geoPackageBinary(byte[] wkb, double[] box) {
        ByteBuffer out = ByteBuffer.allocate(8 + 32 + wkb.length).order(ByteOrder.LITTLE_ENDIAN);
        out.put((byte) 'G').put((byte) 'P').put((byte) 0).put(FLAGS);
        out.putInt(4326);
        out.putDouble(box[0]).putDouble(box[2]).putDouble(box[1]).putDouble(box[3]);
        out.put(wkb);
        return out.array();
    }

    /** Double-quotes an SQL identifier, doubling embedded quotes. */
    static String quote(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    @Override
    public long rows() {
        return rows;
    }

    @Override
    public void close() throws IOException {
        if (closed || connection == null) {
            closed = true;
            return;
        }
        closed = true;
        try {
            for (FeatureTable table : tables.values()) {
                table.insert.close();
                table.rtree.close();
                try (PreparedStatement extent = connection.prepareStatement(
                        "UPDATE gpkg_contents SET min_x = ?, min_y = ?, max_x = ?, max_y = ? WHERE table_name = ?")) {
                    setNullable(extent, 1, table.minX);
                    setNullable(extent, 2, table.minY);
                    setNullable(extent, 3, table.maxX);
                    setNullable(extent, 4, table.maxY);
                    extent.setString(5, table.name);
                    extent.executeUpdate();
                }
                try (Statement statement = connection.createStatement()) {
                    for (String trigger : rtreeTriggers(table.name, GEOMETRY_COLUMN)) {
                        statement.execute(trigger);
                    }
                }
            }
            writeMetadata();
            connection.commit();
        } catch (SQLException e) {
            throw new IOException("Could not finish the GeoPackage", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                // The file is complete or the error above is reported.
            }
        }
    }

    private void writeMetadata() throws SQLException {
        String license = metadata.get("licence");
        if (license == null) {
            return;
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO gpkg_metadata (id, md_scope,"
                + " md_standard_uri, mime_type, metadata) VALUES (1, 'dataset', 'https://foresignal.ai/license-summary',"
                + " 'application/json', ?)")) {
            insert.setString(1, license);
            insert.executeUpdate();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO gpkg_metadata_reference (reference_scope, table_name, column_name,"
                    + " row_id_value, md_file_id, md_parent_id) VALUES ('geopackage', NULL, NULL, NULL, 1, NULL)");
            statement.execute("INSERT INTO gpkg_extensions VALUES ('gpkg_metadata', NULL, 'gpkg_metadata', '"
                    + METADATA_DEFINITION + "', 'read-write')");
            statement.execute("INSERT INTO gpkg_extensions VALUES ('gpkg_metadata_reference', NULL, 'gpkg_metadata', '"
                    + METADATA_DEFINITION + "', 'read-write')");
        }
    }

    private static void setNullable(PreparedStatement statement, int index, double value) throws SQLException {
        if (Double.isNaN(value)) {
            statement.setNull(index, Types.DOUBLE);
        } else {
            statement.setDouble(index, value);
        }
    }

    /**
     * The GeoPackage RTree extension's maintenance triggers (GeoPackage 1.4, Annex F.3) for
     * {@code table}.{@code column}: insert, the four update cases and delete.
     */
    static List<String> rtreeTriggers(String table, String column) {
        String t = quote(table);
        String c = quote(column);
        String r = quote("rtree_" + table + "_" + column);
        String prefix = "rtree_" + table + "_" + column;
        List<String> out = new ArrayList<>();
        out.add("CREATE TRIGGER " + quote(prefix + "_insert") + " AFTER INSERT ON " + t
                + " WHEN (new." + c + " NOT NULL AND NOT ST_IsEmpty(NEW." + c + ")) BEGIN INSERT OR REPLACE INTO " + r
                + " VALUES (NEW.\"fid\", ST_MinX(NEW." + c + "), ST_MaxX(NEW." + c + "), ST_MinY(NEW." + c + "),"
                + " ST_MaxY(NEW." + c + ")); END");
        out.add("CREATE TRIGGER " + quote(prefix + "_update6") + " AFTER UPDATE OF " + c + " ON " + t
                + " WHEN OLD.\"fid\" = NEW.\"fid\" AND (NEW." + c + " NOTNULL AND NOT ST_IsEmpty(NEW." + c + "))"
                + " BEGIN UPDATE " + r + " SET minx = ST_MinX(NEW." + c + "), maxx = ST_MaxX(NEW." + c + "),"
                + " miny = ST_MinY(NEW." + c + "), maxy = ST_MaxY(NEW." + c + ") WHERE id = NEW.\"fid\";"
                + " INSERT OR IGNORE INTO " + r + " SELECT NEW.\"fid\", ST_MinX(NEW." + c + "), ST_MaxX(NEW." + c
                + "), ST_MinY(NEW." + c + "), ST_MaxY(NEW." + c + ") WHERE (SELECT changes() = 0); END");
        out.add("CREATE TRIGGER " + quote(prefix + "_update7") + " AFTER UPDATE OF " + c + " ON " + t
                + " WHEN OLD.\"fid\" = NEW.\"fid\" AND (NEW." + c + " ISNULL OR ST_IsEmpty(NEW." + c + "))"
                + " BEGIN DELETE FROM " + r + " WHERE id = OLD.\"fid\"; END");
        out.add("CREATE TRIGGER " + quote(prefix + "_update5") + " AFTER UPDATE ON " + t
                + " WHEN OLD.\"fid\" != NEW.\"fid\" AND (NEW." + c + " NOTNULL AND NOT ST_IsEmpty(NEW." + c + "))"
                + " BEGIN DELETE FROM " + r + " WHERE id = OLD.\"fid\"; INSERT OR REPLACE INTO " + r
                + " VALUES (NEW.\"fid\", ST_MinX(NEW." + c + "), ST_MaxX(NEW." + c + "), ST_MinY(NEW." + c + "),"
                + " ST_MaxY(NEW." + c + ")); END");
        out.add("CREATE TRIGGER " + quote(prefix + "_update4") + " AFTER UPDATE ON " + t
                + " WHEN OLD.\"fid\" != NEW.\"fid\" AND (NEW." + c + " ISNULL OR ST_IsEmpty(NEW." + c + "))"
                + " BEGIN DELETE FROM " + r + " WHERE id IN (OLD.\"fid\", NEW.\"fid\"); END");
        out.add("CREATE TRIGGER " + quote(prefix + "_delete") + " AFTER DELETE ON " + t
                + " WHEN old." + c + " NOT NULL BEGIN DELETE FROM " + r + " WHERE id = OLD.\"fid\"; END");
        return out;
    }
}
