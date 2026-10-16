package ai.foresignal.export;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The attribute columns of a bulk export (prompt 111): the fixed Foresignal columns, then the
 * optional flattened payload columns, then {@code payload}. The geometry is not a column here: each
 * writer adds it in its format's own way (GeoParquet {@code geometry} WKB plus the {@code bbox}
 * covering struct, a GeoPackage {@code geom} column, a FlatGeobuf feature geometry, GPX coordinates)
 * when {@link #geometry()} is true.
 *
 * @param columns the attribute columns, in order
 * @param geometry true when the writer adds its geometry
 */
public record ExportSchema(List<Column> columns, boolean geometry) {

    /** The column names the fixed layout reserves; a flattened field never takes one of them. */
    public static final Set<String> RESERVED = Set.of("source_id", "external_id", "kind", "seq", "version",
            "observed_at", "event_time", "valid_until", "ingested_at", "title", "payload", "geometry", "bbox", "geom",
            "fid");

    /** A column type. */
    public enum Type {
        /** UTF-8 text. */
        STRING,
        /** A signed 64-bit integer. */
        INT64,
        /** A signed 32-bit integer. */
        INT32,
        /** An instant, microseconds since the epoch, UTC. */
        TIMESTAMP_MICROS,
        /** A 64-bit float. */
        DOUBLE
    }

    /**
     * One column.
     *
     * @param name the column name
     * @param type the type
     * @param nullable whether a row may omit it
     */
    public record Column(String name, Type type, boolean nullable) {}

    /** Validates and copies. */
    public ExportSchema {
        columns = columns == null ? List.of() : List.copyOf(columns);
    }

    /**
     * The fixed columns of every export plus {@code flattened} payload fields as text columns.
     * Flattened names that collide with a fixed column, repeat, or are blank are dropped.
     */
    public static ExportSchema standard(List<String> flattened, boolean geometry) {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("source_id", Type.STRING, false));
        columns.add(new Column("external_id", Type.STRING, false));
        columns.add(new Column("kind", Type.STRING, false));
        columns.add(new Column("seq", Type.INT64, false));
        columns.add(new Column("version", Type.INT32, false));
        columns.add(new Column("observed_at", Type.TIMESTAMP_MICROS, true));
        columns.add(new Column("event_time", Type.TIMESTAMP_MICROS, true));
        columns.add(new Column("valid_until", Type.TIMESTAMP_MICROS, true));
        columns.add(new Column("ingested_at", Type.TIMESTAMP_MICROS, true));
        columns.add(new Column("title", Type.STRING, true));
        for (String field : flattenedNames(flattened)) {
            columns.add(new Column(field, Type.STRING, true));
        }
        columns.add(new Column("payload", Type.STRING, true));
        return new ExportSchema(columns, geometry);
    }

    /** The usable flattened field names of {@code flattened}, in order. */
    public static List<String> flattenedNames(List<String> flattened) {
        Set<String> out = new LinkedHashSet<>();
        for (String field : flattened == null ? List.<String>of() : flattened) {
            if (field != null && !field.isBlank() && !RESERVED.contains(field)) {
                out.add(field);
            }
        }
        return List.copyOf(out);
    }

    /** The column named {@code name}, or null. */
    public Column column(String name) {
        for (Column column : columns) {
            if (column.name().equals(name)) {
                return column;
            }
        }
        return null;
    }
}
