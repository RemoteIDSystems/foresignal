package ai.foresignal.export;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The shared SELECT list and row mapping of the export, snapshot and archive queries (prompt 111):
 * every query selects the same aliases, and {@link #map} turns a result row into the map the
 * {@link ExportWriter}s take.
 */
public final class ExportRows {

    private ExportRows() {}

    /**
     * The select list over a {@code record_version}-shaped relation aliased {@code alias}: the
     * export columns, the payload as text, the geometry as WKB ({@code ST_AsBinary}) and its envelope.
     */
    public static String versionColumns(String alias) {
        String a = alias + ".";
        return a + "source_id, " + a + "external_id, " + a + "kind, " + a + "seq, " + a + "version, "
                + a + "observed_at, " + a + "event_time, " + a + "valid_until, " + a + "ingested_at,"
                + " NULL::varchar AS title, " + a + "payload::text AS payload, ST_AsBinary(" + a + "geom) AS geom_wkb,"
                + " ST_XMin(" + a + "geom) AS xmin, ST_YMin(" + a + "geom) AS ymin, ST_XMax(" + a + "geom) AS xmax,"
                + " ST_YMax(" + a + "geom) AS ymax";
    }

    /** Maps the current row of {@code rs} (selected with the aliases of {@link #versionColumns}). */
    public static Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source_id", rs.getString("source_id"));
        row.put("external_id", rs.getString("external_id"));
        row.put("kind", rs.getString("kind"));
        row.put("seq", rs.getLong("seq"));
        row.put("version", rs.getInt("version"));
        row.put("observed_at", instant(rs.getObject("observed_at")));
        row.put("event_time", instant(rs.getObject("event_time")));
        row.put("valid_until", instant(rs.getObject("valid_until")));
        row.put("ingested_at", instant(rs.getObject("ingested_at")));
        row.put("title", rs.getString("title"));
        row.put("payload", rs.getString("payload"));
        byte[] wkb = rs.getBytes("geom_wkb");
        if (wkb != null && wkb.length > 0) {
            row.put(ExportWriter.GEOMETRY, wkb);
            Double xmin = number(rs.getObject("xmin"));
            if (xmin != null) {
                row.put(ExportWriter.BBOX, new double[] {xmin, number(rs.getObject("ymin")), number(rs.getObject("xmax")),
                    number(rs.getObject("ymax"))});
            }
        }
        return row;
    }

    /** An instant from a JDBC timestamp value, or null. */
    public static Instant instant(Object value) {
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof Instant i) {
            return i;
        }
        return null;
    }

    private static Double number(Object value) {
        return value instanceof Number n ? n.doubleValue() : null;
    }
}
