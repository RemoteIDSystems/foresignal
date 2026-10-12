package ai.foresignal.reference;

import static ai.foresignal.testsupport.reference.RegionTestSources.LAZY;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINTS;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Region tags live outside the hash (prompt 040, Core-Data-Model §4 and §1 I1): tagging, retagging after a
 * reference load and lazy tagging never change {@code static_hash} or {@code full_hash}, never write a
 * version and never advance the change watermark.
 */
@IntegrationTest
@Import(RegionTestSources.class)
class RegionHashInvarianceIT {

    static final Instant T = Instant.parse("2026-06-02T12:00:00Z");

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired RegionTagWorker worker;
    @Autowired RegionTagger tagger;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationContext context;

    Map<String, Object> state(String source, String id) {
        return jdbc.queryForMap("SELECT h.static_hash, l.full_hash, h.seq, h.version,"
                + " (SELECT count(*) FROM record_version v WHERE v.source_id = h.source_id"
                + " AND v.external_id = h.external_id) AS versions"
                + " FROM record_head h JOIN record_live l ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE h.source_id = ? AND h.external_id = ?", source, id);
    }

    @Test
    @DisplayName("tagging, retagging and lazy tagging change no hash, write no version and publish no change")
    void tagsNeverVersion() {
        clock.setInstant(T);
        records.ingest(POINTS, NormalizedRecord.builder("h1", POINT_KIND).observedAt(T).point(-97.74, 30.27)
                .put("name", "Hash point").build());
        records.ingest(LAZY, NormalizedRecord.builder("h2", POINT_KIND).observedAt(T).point(-95.36, 29.76)
                .put("name", "Lazy point").build());
        await().atMost(Duration.ofSeconds(15)).until(() -> worker.cursor() >= changeBus.lastPublishedSeq());
        Map<String, Object> eager = state(POINTS, "h1");
        Map<String, Object> lazy = state(LAZY, "h2");
        long watermark = changeBus.lastPublishedSeq();
        long versions = jdbc.queryForObject("SELECT count(*) FROM record_version", Long.class);

        ReferenceFixtures.loadAll(context);
        await().atMost(Duration.ofSeconds(15)).until(() -> !worker.retagging());
        worker.retagAll();
        await().atMost(Duration.ofSeconds(15)).until(() -> jdbc.queryForObject(
                "SELECT admin2 FROM record_region WHERE source_id = ? AND external_id = 'h1'", String.class, POINTS)
                != null);
        assertThat(tagger.tagRecord(LAZY, "h2")).isNotNull();

        assertThat(state(POINTS, "h1")).isEqualTo(eager);
        assertThat(state(LAZY, "h2")).isEqualTo(lazy);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version", Long.class)).isEqualTo(versions);
        assertThat(changeBus.lastPublishedSeq()).isEqualTo(watermark);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_region", Long.class)).isEqualTo(2);
    }
}
