package ai.foresignal.store.history;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Every statement that defines the partitioned {@code record_version}, kept in one place so prompt
 * 144 can move them into the Flyway baseline unchanged (Core-Data-Model §9).
 *
 * <p>Column order and types match {@link ai.foresignal.store.RecordVersionEntity}; {@code
 * SchemaParityIT} checks the two agree. There is deliberately no unique constraint, no
 * {@code ingested_at} index, no content hash and never a DEFAULT partition.
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

    /** Step 1: the allocator. */
    public static final String CREATE_SEQUENCE =
            "CREATE SEQUENCE IF NOT EXISTS " + SEQUENCE + " INCREMENT BY 1";

    /** The relkind of {@code record_version} in the current schema, or no row when absent. */
    public static final String SELECT_RELKIND =
            """
            SELECT c.relkind::text FROM pg_class c
              JOIN pg_namespace n ON n.oid = c.relnamespace
             WHERE n.nspname = current_schema() AND c.relname = 'record_version'
            """;

    /** Whether the prompt-002 plain {@code record_version} is empty. */
    public static final String LEGACY_VERSION_EMPTY = "SELECT NOT EXISTS (SELECT 1 FROM record_version)";

    /**
     * Whether {@code record_head} exists. Checked separately because a query naming a missing
     * relation fails at parse time, however it is guarded.
     */
    public static final String HEAD_EXISTS = "SELECT to_regclass('record_head') IS NOT NULL";

    /** Whether {@code record_head} is empty; only run when it exists. */
    public static final String HEAD_EMPTY = "SELECT NOT EXISTS (SELECT 1 FROM record_head)";

    /** Drops the prompt-002 tables, so Hibernate recreates the head without its old columns. */
    public static final String DROP_LEGACY = "DROP TABLE IF EXISTS record_version, record_head CASCADE";

    /** The fail-fast message for a populated prompt-002 table. */
    public static final String LEGACY_DATA_MESSAGE =
            "record_version holds pre-v1.2 data; run docker compose down -v and restart";

    /** Step 3: the parent, partitioned by tier. */
    public static final String CREATE_PARENT =
            "CREATE TABLE IF NOT EXISTS "
                    + TABLE
                    + " ("
                    + COLUMNS.stream().map(Column::definition).collect(Collectors.joining(", "))
                    + ", PRIMARY KEY (seq, history_tier)) PARTITION BY LIST (history_tier)";

    /** Step 3: the STANDARD parent, partitioned by seq. */
    public static final String CREATE_STANDARD_PARENT =
            "CREATE TABLE IF NOT EXISTS "
                    + STANDARD_PARENT
                    + " PARTITION OF "
                    + TABLE
                    + " FOR VALUES IN (0) PARTITION BY RANGE (seq)";

    /** Step 3: the TRACK parent, partitioned by seq. */
    public static final String CREATE_TRACK_PARENT =
            "CREATE TABLE IF NOT EXISTS "
                    + TRACK_PARENT
                    + " PARTITION OF "
                    + TABLE
                    + " FOR VALUES IN (1) PARTITION BY RANGE (seq)";

    /** Step 3: applies a later column addition in place; a no-op when every column exists. */
    public static final String ADD_MISSING_COLUMNS =
            "ALTER TABLE "
                    + TABLE
                    + " "
                    + COLUMNS.stream()
                            .map(c -> "ADD COLUMN IF NOT EXISTS " + c.definition())
                            .collect(Collectors.joining(", "));

    /** Step 4: the three partitioned indexes. */
    public static final List<String> CREATE_INDEXES = List.of(
            "CREATE INDEX IF NOT EXISTS record_version_source_external_seq_idx ON "
                    + TABLE
                    + " (source_id, external_id, seq)",
            "CREATE INDEX IF NOT EXISTS record_version_source_observed_idx ON "
                    + TABLE
                    + " (source_id, observed_at)",
            "CREATE INDEX IF NOT EXISTS record_version_geom_gist ON " + TABLE + " USING GIST (geom)");

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
