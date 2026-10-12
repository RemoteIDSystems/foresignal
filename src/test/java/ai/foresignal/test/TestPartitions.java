package ai.foresignal.test;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Inspects and resets {@code record_version}'s partitions from tests.
 *
 * <p>A test that runs its own {@link PartitionManager} with tiny blocks calls
 * {@link #dropAllLeaves(JdbcTemplate)} first, so the default-sized leaves do not overlap its own,
 * and puts the defaults back in {@code @AfterEach} with {@code dropAllLeaves} followed by
 * {@link #restore(PartitionManager)} on the application's manager.
 */
public final class TestPartitions {

    private static final Pattern RANGE_BOUND =
            Pattern.compile("FROM \\('?(-?\\d+)'?\\) TO \\('?(-?\\d+)'?\\)");

    private TestPartitions() {}

    /** The leaves currently attached under {@code tier}'s parent, read from the catalog. */
    public static List<LeafInfo> leaves(JdbcTemplate jdbc, HistoryTier tier) {
        return jdbc.query(
                """
                select c.relname, pg_get_expr(c.relpartbound, c.oid), i.inhdetachpending
                  from pg_inherits i
                  join pg_class c on c.oid = i.inhrelid
                  join pg_class p on p.oid = i.inhparent
                 where p.relname = ?
                """,
                (rs, n) -> {
                    Matcher m = RANGE_BOUND.matcher(rs.getString(2));
                    if (!m.find()) {
                        throw new IllegalStateException("Unexpected bound " + rs.getString(2));
                    }
                    return new LeafInfo(
                            rs.getString(1),
                            tier,
                            Long.parseLong(m.group(1)),
                            Long.parseLong(m.group(2)),
                            rs.getBoolean(3));
                },
                PartitionManager.parentOf(tier))
                .stream()
                .sorted((a, b) -> Long.compare(a.fromSeq(), b.fromSeq()))
                .toList();
    }

    /**
     * Detaches (plainly, not concurrently) and drops every leaf of both tiers, plus any leaf-named
     * table left unattached by an interrupted create or downsampling copy ({@code rv_t_<start>_ds},
     * prompt 044). Meant for empty test tables; the tier parents stay.
     */
    public static void dropAllLeaves(JdbcTemplate jdbc) {
        for (HistoryTier tier : HistoryTier.values()) {
            for (LeafInfo leaf : leaves(jdbc, tier)) {
                if (leaf.detachPending()) {
                    jdbc.execute("ALTER TABLE " + PartitionManager.parentOf(tier)
                            + " DETACH PARTITION " + leaf.name() + " FINALIZE");
                } else {
                    jdbc.execute("ALTER TABLE " + PartitionManager.parentOf(tier)
                            + " DETACH PARTITION " + leaf.name());
                }
                jdbc.execute("DROP TABLE " + leaf.name());
            }
        }
        List<String> unattached = jdbc.queryForList(
                """
                select c.relname from pg_class c
                  join pg_namespace n on n.oid = c.relnamespace
                 where n.nspname = current_schema() and c.relkind = 'r'
                   and c.relname ~ '^rv_[st]_[0-9]+(_ds)?$'
                   and not exists (select 1 from pg_inherits i where i.inhrelid = c.oid)
                """,
                String.class);
        unattached.forEach(name -> jdbc.execute("DROP TABLE " + name));
    }

    /** Re-reads the catalog into {@code partitionManager} and puts its coverage back ahead. */
    public static void restore(PartitionManager partitionManager) {
        partitionManager.refresh();
        partitionManager.ensureAhead();
    }
}
