package ai.foresignal.store.history;

import java.util.List;

/**
 * The shape of the partitioned {@code record_version} and the catalog queries the {@link PartitionManager} uses
 * (Architecture §9, §25).
 *
 * <p>Since prompt 144 the Flyway baseline ({@code db/migration/V1__baseline.sql}) creates {@code record_seq}, the
 * parent, both tier parents and the three partitioned indexes; only leaves are created at runtime, by the
 * {@link PartitionManager}. {@link #COLUMNS} is the column list the baseline declares, in order, and it matches
 * {@link ai.foresignal.store.RecordVersionEntity}; {@code SchemaParityIT} checks the entity, this list and the migrated
 * table agree. There is deliberately no unique constraint, no {@code ingested_at} index, no content hash and never a
 * DEFAULT partition.
 */
public final class HistoryDdl {

    /** The partitioned parent. */
    public static final String TABLE = "record_version";

    /** The STANDARD tier parent ({@code history_tier = 0}). */
    public static final String STANDARD_PARENT = "record_version_std";

    /** The TRACK tier parent ({@code history_tier = 1}). */
    public static final String TRACK_PARENT = "record_version_trk";

    /** The global seq allocator. */
    public static final String SEQUENCE = "record_seq";

    /**
     * One column of {@code record_version}.
     *
     * @param name the column name
     * @param type the SQL type
     * @param notNull whether the column is NOT NULL
     */
    public record Column(String name, String type, boolean notNull) {

        /** The column as it appears in a column list. */
        public String definition() {
            return name + " " + type + (notNull ? " NOT NULL" : "");
        }
    }

    /** The columns, in order. {@code valid_from} is appended for Gap-Analysis §8.A item 2. */
    public static final List<Column> COLUMNS = List.of(
            new Column("seq", "bigint", true),
            new Column("ingested_at", "timestamptz", true),
            new Column("observed_at", "timestamptz", true),
            new Column("received_at", "timestamptz", true),
            new Column("event_time", "timestamptz", false),
            new Column("valid_until", "timestamptz", false),
            new Column("valid_until_explicit", "timestamptz", false),
            new Column("prev_seq", "bigint", false),
            new Column("batch_id", "bigint", false),
            new Column("version", "integer", true),
            new Column("history_tier", "smallint", true),
            new Column("persist_reason", "smallint", true),
            new Column("qa_flags", "smallint", true),
            new Column("tombstone", "boolean", true),
            new Column("source_id", "varchar(64)", true),
            new Column("external_id", "varchar(256)", true),
            new Column("kind", "varchar(64)", true),
            new Column("geom", "geometry(Geometry,4326)", false),
            new Column("payload", "jsonb", true),
            new Column("quality", "jsonb", false),
            new Column("observer", "jsonb", false),
            new Column("valid_from", "timestamptz", false));

    /** The attached leaves of both tier parents, with their bounds and detach state. */
    public static final String SELECT_LEAVES =
            """
            SELECT c.relname, p.relname, pg_get_expr(c.relpartbound, c.oid), i.inhdetachpending
              FROM pg_inherits i
              JOIN pg_class c ON c.oid = i.inhrelid
              JOIN pg_class p ON p.oid = i.inhparent
              JOIN pg_namespace n ON n.oid = p.relnamespace
             WHERE n.nspname = current_schema()
               AND p.relname IN ('record_version_std', 'record_version_trk')
            """;

    /** The allocator's position. */
    public static final String SELECT_LAST_VALUE = "SELECT last_value FROM " + SEQUENCE;

    /** Refreshes planner statistics across every leaf. */
    public static final String ANALYZE = "ANALYZE " + TABLE;

    private HistoryDdl() {}
}
