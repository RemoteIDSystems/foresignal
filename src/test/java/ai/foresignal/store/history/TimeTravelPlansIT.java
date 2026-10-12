package ai.foresignal.store.history;

import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Prompt 037 adds no index on {@code record_version}: its readers are served by the three
 * partitioned indexes of Core-Data-Model §8. With sequential scans disabled, each query shape must
 * plan onto an index; the plans are logged so checkpoint 041 can record them.
 *
 * <ul>
 *   <li>the track scan: {@code (source_id, external_id, seq)};
 *   <li>the timeline on the observed axis: {@code (source_id, observed_at)};
 *   <li>the asOf tile candidates: the GiST {@code (geom)} index (or the seq primary key);
 *   <li>the asOf tile pick: {@code (source_id, external_id, seq)}.
 * </ul>
 */
@IntegrationTest
@Import(TimeTravelTestSources.class)
class TimeTravelPlansIT {

    private static final Logger LOG = LoggerFactory.getLogger(TimeTravelPlansIT.class);
    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @BeforeEach
    void seed() {
        clock.setInstant(T);
        for (int i = 0; i < 20; i++) {
            clock.setInstant(T.plusSeconds(i * 30L));
            records.ingest(MOVER, NormalizedRecord.builder("m" + (i % 4), VEHICLE).observedAt(T.plusSeconds(i * 30L))
                    .point(10 + i * 0.01, 50).put("callsign", "CS").build());
        }
        jdbc.execute("ANALYZE record_version");
    }

    private String plan(String sql) {
        String text = new TransactionTemplate(transactions).execute(status -> {
            jdbc.execute("SET LOCAL enable_seqscan = off");
            return String.join("\n", jdbc.queryForList("EXPLAIN " + sql, String.class));
        });
        LOG.info("plan of {}:\n{}", sql, text);
        return text;
    }

    @Test
    @DisplayName("record_version keeps exactly its primary key and the three partitioned indexes")
    void noNewIndex() {
        Set<String> indexes = new TreeSet<>(jdbc.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'record_version'", String.class));
        assertThat(indexes).containsExactlyInAnyOrder("record_version_pkey", "record_version_source_external_seq_idx",
                "record_version_source_observed_idx", "record_version_geom_gist");
    }

    @Test
    @DisplayName("the track scan uses (source_id, external_id, seq)")
    void trackPlan() {
        String text = plan("SELECT v.seq FROM record_version v WHERE v.history_tier = CAST(1 AS smallint)"
                + " AND v.source_id = '" + MOVER + "' AND v.external_id = 'm1'"
                + " AND v.observed_at BETWEEN TIMESTAMPTZ '2026-03-01T00:00:00Z' AND TIMESTAMPTZ '2026-03-01T06:00:00Z'"
                + " ORDER BY v.seq LIMIT 200001");
        assertThat(text).contains("source_id_external_id_seq");
    }

    @Test
    @DisplayName("the timeline on the observed axis uses (source_id, observed_at)")
    void timelinePlan() {
        String text = plan("SELECT date_bin(INTERVAL '1 minute', v.observed_at, TIMESTAMPTZ '2026-03-01T00:00:00Z'),"
                + " count(*) FROM record_version v WHERE v.source_id = ANY(ARRAY['" + MOVER + "'])"
                + " AND v.observed_at >= TIMESTAMPTZ '2026-03-01T00:00:00Z'"
                + " AND v.observed_at < TIMESTAMPTZ '2026-03-01T01:00:00Z' GROUP BY 1");
        assertThat(text).contains("source_id_observed_at");
    }

    @Test
    @DisplayName("the asOf tile candidates and pick plan onto existing indexes")
    void asOfTilePlan() {
        String text = plan("WITH cand AS (SELECT DISTINCT v.source_id, v.external_id FROM record_version v"
                + " WHERE v.history_tier = CAST(1 AS smallint) AND v.seq BETWEEN 0 AND 1000000"
                + " AND v.source_id = ANY(ARRAY['" + MOVER + "'])"
                + " AND v.geom && ST_MakeEnvelope(9, 49, 11, 51, 4326))"
                + " SELECT p.* FROM cand c CROSS JOIN LATERAL (SELECT * FROM record_version v"
                + " WHERE v.history_tier = CAST(1 AS smallint) AND v.source_id = c.source_id"
                + " AND v.external_id = c.external_id AND v.seq BETWEEN 0 AND 1000000 ORDER BY v.seq DESC LIMIT 1) p");
        assertThat(text).doesNotContain("Seq Scan");
        assertThat(text).contains("source_id_external_id_seq");
        List<String> scans = text.lines().filter(l -> l.contains("Scan")).toList();
        assertThat(scans).isNotEmpty();
    }
}
