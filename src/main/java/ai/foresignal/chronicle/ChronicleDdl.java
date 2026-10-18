package ai.foresignal.chronicle;

import ai.foresignal.security.Ulids;
import ai.foresignal.store.history.HistoryDdl;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Every statement that defines chronicle storage, kept in one place so prompt 144 can move them into the Flyway
 * baseline unchanged (prompt 131; Core-Data-Model §9 assigns partitioned history to bootstrap SQL).
 *
 * <pre>
 * chronicle_version                    PARTITION BY LIST (chronicle_id)
 * └── chronicle_version_{id lower}     FOR VALUES IN ('{id}')   one leaf per chronicle: created on activation,
 *                                                                dropped on deletion, archive or own retention
 * </pre>
 *
 * <p>The columns mirror {@code record_version} ({@link HistoryDdl#COLUMNS}) after {@code chronicle_id} and
 * {@code layer_id}, plus {@code backfilled}, {@code bucket_start} (the {@code latest-per-interval} bucket) and
 * {@code captured_at}. The primary key is {@code (chronicle_id, layer_id, seq)}: a version is stored once per layer
 * that captured it, and re-capturing after a cursor restart is a no-op (exactly once). The four indexes are
 * partitioned, so every leaf gets {@code (seq)}, {@code (layer_id, observed_at)}, GiST {@code (geom)} and
 * {@code (source_id, external_id, seq)}. Never a DEFAULT partition.
 */
public final class ChronicleDdl {

    /** The partitioned parent. */
    public static final String TABLE = "chronicle_version";

    /** The leaf name prefix. */
    public static final String LEAF_PREFIX = "chronicle_version_";

    /** The columns after {@code record_version}'s. */
    public static final List<HistoryDdl.Column> EXTRA_COLUMNS = List.of(
            new HistoryDdl.Column("backfilled", "boolean", false),
            new HistoryDdl.Column("bucket_start", "timestamptz", false),
            new HistoryDdl.Column("captured_at", "timestamptz", false));

    /** Every column, in order. */
    public static final List<HistoryDdl.Column> COLUMNS = columns();

    /** Creates the parent. */
    public static final String CREATE_PARENT = "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
            + "chronicle_id varchar(26) NOT NULL, layer_id varchar(26) NOT NULL, "
            + HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::definition).collect(Collectors.joining(", "))
            + ", backfilled boolean NOT NULL DEFAULT false, bucket_start timestamptz,"
            + " captured_at timestamptz NOT NULL DEFAULT now(),"
            + " PRIMARY KEY (chronicle_id, layer_id, seq)) PARTITION BY LIST (chronicle_id)";

    /** Applies a later column addition in place; a no-op when every column exists. */
    public static final String ADD_MISSING_COLUMNS = "ALTER TABLE " + TABLE + " "
            + HistoryDdl.COLUMNS.stream().map(c -> "ADD COLUMN IF NOT EXISTS " + c.definition())
                    .collect(Collectors.joining(", "));

    /** The four partitioned indexes. */
    public static final List<String> CREATE_INDEXES = List.of(
            "CREATE INDEX IF NOT EXISTS chronicle_version_seq_idx ON " + TABLE + " (seq)",
            "CREATE INDEX IF NOT EXISTS chronicle_version_layer_observed_idx ON " + TABLE + " (layer_id, observed_at)",
            "CREATE INDEX IF NOT EXISTS chronicle_version_geom_gist ON " + TABLE + " USING GIST (geom)",
            "CREATE INDEX IF NOT EXISTS chronicle_version_source_external_seq_idx ON " + TABLE
                    + " (source_id, external_id, seq)");

    /** The attached leaves with the chronicle id each one holds. */
    public static final String SELECT_LEAVES = """
            SELECT c.relname, pg_get_expr(c.relpartbound, c.oid)
              FROM pg_inherits i
              JOIN pg_class c ON c.oid = i.inhrelid
              JOIN pg_class p ON p.oid = i.inhparent
              JOIN pg_namespace n ON n.oid = p.relnamespace
             WHERE n.nspname = current_schema() AND p.relname = 'chronicle_version'
            """;

    private ChronicleDdl() {}

    private static List<HistoryDdl.Column> columns() {
        List<HistoryDdl.Column> out = new ArrayList<>();
        out.add(new HistoryDdl.Column("chronicle_id", "varchar(26)", true));
        out.add(new HistoryDdl.Column("layer_id", "varchar(26)", true));
        out.addAll(HistoryDdl.COLUMNS);
        out.addAll(EXTRA_COLUMNS);
        return List.copyOf(out);
    }

    /**
     * The leaf of a chronicle.
     *
     * @throws IllegalArgumentException when {@code chronicleId} is not a ULID (so no other text reaches the DDL)
     */
    public static String leaf(String chronicleId) {
        requireUlid(chronicleId);
        return LEAF_PREFIX + chronicleId.toLowerCase(Locale.ROOT);
    }

    /** Creates the leaf of a chronicle (idempotent). */
    public static String createLeaf(String chronicleId) {
        return "CREATE TABLE IF NOT EXISTS " + leaf(chronicleId) + " PARTITION OF " + TABLE + " FOR VALUES IN ('"
                + chronicleId + "')";
    }

    /** Drops the leaf of a chronicle (idempotent). */
    public static String dropLeaf(String chronicleId) {
        return "DROP TABLE IF EXISTS " + leaf(chronicleId);
    }

    /** The chronicle id of a leaf name, or null when it is not a chronicle leaf. */
    public static String chronicleOf(String leafName) {
        if (leafName == null || !leafName.startsWith(LEAF_PREFIX)) {
            return null;
        }
        String id = leafName.substring(LEAF_PREFIX.length()).toUpperCase(Locale.ROOT);
        return Ulids.isUlid(id) ? id : null;
    }

    private static void requireUlid(String chronicleId) {
        if (!Ulids.isUlid(chronicleId)) {
            throw new IllegalArgumentException("not a chronicle id: " + chronicleId);
        }
    }
}
