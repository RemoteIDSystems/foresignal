package ai.foresignal.export;

import java.util.ArrayList;
import java.util.List;

/**
 * The column layout of a bulk export (prompt 111): the fixed Foresignal columns plus optional flattened
 * payload columns. The geometry column (WKB) and its {@code bbox} struct are appended when present.
 *
 * @param columns the columns
 * @param geometry true when a {@code geometry} WKB column and a {@code bbox} struct are included
 */
public record ExportSchema(List<Column> columns, boolean geometry) {

    /** A column type. */
    public enum Type {
        STRING, INT64, INT32, TIMESTAMP_MICROS, DOUBLE, WKB
    }

    /** One column. */
    public record Column(String name, Type type, boolean nullable) {}

    /** Validates and copies. */
    public ExportSchema {
        columns = columns == null ? List.of() : List.copyOf(columns);
    }

    /** The fixed columns of every export. */
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
        for (String field : flattened == null ? List.<String>of() : flattened) {
            columns.add(new Column(field, Type.STRING, true));
        }
        columns.add(new Column("payload", Type.STRING, true));
        if (geometry) {
            columns.add(new Column("geometry", Type.WKB, true));
        }
        return new ExportSchema(columns, geometry);
    }
}
