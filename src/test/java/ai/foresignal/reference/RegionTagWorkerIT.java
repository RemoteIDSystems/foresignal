package ai.foresignal.reference;

import static ai.foresignal.testsupport.reference.RegionTestSources.LAZY;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINTS;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link RegionTagWorker} (prompt 040): eager sources are tagged as versions persist, movers are tagged
 * again only past the retag distance, lazy sources are tagged on demand and off sources never, a
 * tombstone deletes the row, and the durable cursor carries the worker across a restart.
 */
@IntegrationTest
@Import(RegionTestSources.class)
class RegionTagWorkerIT {

    static final Instant T = Instant.parse("2026-06-01T12:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(15);

    @Autowired RegionTagWorker worker;
    @Autowired RegionTagger tagger;
    @Autowired RegionTaggingService modes;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired AppSettingService settings;
    @Autowired ApplicationContext context;

    Instant now = T;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        ReferenceFixtures.loadAll(context);
        await().atMost(WAIT).until(() -> !worker.retagging());
    }

    @AfterEach
    void tearDown() {
        worker.start();
        settings.invalidateCache();
    }

    void at(String source, String id, double lon, double lat) {
        now = now.plusSeconds(30);
        clock.setInstant(now);
        records.ingest(source, NormalizedRecord.builder(id, POINT_KIND).observedAt(now).point(lon, lat)
                .put("name", "Point " + id).build());
    }

    void settle() {
        await().atMost(WAIT).until(() -> worker.cursor() >= changeBus.lastPublishedSeq());
    }

    Map<String, Object> row(String source, String id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM record_region WHERE source_id = ? AND"
                + " external_id = ?", source, id);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    @Test
    @DisplayName("eager: a persisted version of a STANDARD source is tagged by the worker")
    void eager() {
        assertThat(modes.mode(POINTS)).isEqualTo(RegionTagMode.EAGER);
        at(POINTS, "e1", -97.74, 30.27);
        await().atMost(WAIT).until(() -> row(POINTS, "e1") != null);
        Map<String, Object> row = row(POINTS, "e1");
        assertThat(row).containsEntry("iso2", "US").containsEntry("admin1", "US-TX").containsEntry("admin2", "48453")
                .containsEntry("near_name", "Austin");
        assertThat((Double) row.get("tagged_lon")).isEqualTo(-97.74);
    }

    @Test
    @DisplayName("movers are tagged again only past the retag distance (2 km for STANDARD)")
    void retagThreshold() {
        at(POINTS, "m1", -97.74, 30.27);
        await().atMost(WAIT).until(() -> row(POINTS, "m1") != null);
        Object firstAt = row(POINTS, "m1").get("tagged_at");

        at(POINTS, "m1", -97.735, 30.275);
        settle();
        assertThat((Double) row(POINTS, "m1").get("tagged_lon")).as("700 m: not tagged again").isEqualTo(-97.74);
        assertThat(row(POINTS, "m1").get("tagged_at")).isEqualTo(firstAt);

        at(POINTS, "m1", -95.36, 29.76);
        await().atMost(WAIT).until(() -> (Double) row(POINTS, "m1").get("tagged_lon") == -95.36);
        assertThat(row(POINTS, "m1")).containsEntry("admin2", "48201").containsEntry("near_name", "Houston");
        assertThat(tagger.retagKm(POINTS)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("lazy: nothing is tagged until asked, then the result is stored; off is never tagged")
    void lazyAndOff() {
        modes.update(LAZY, "lazy");
        assertThat(modes.mode(LAZY)).isEqualTo(RegionTagMode.LAZY);
        at(LAZY, "l1", -97.74, 30.27);
        settle();
        assertThat(row(LAZY, "l1")).as("not tagged eagerly").isNull();

        RegionTags tags = tagger.tagRecord(LAZY, "l1");
        assertThat(tags.admin2()).isEqualTo("48453");
        assertThat(row(LAZY, "l1")).containsEntry("admin2", "48453");
        assertThat(tagger.stored(LAZY, "l1")).map(RegionTags::admin1).contains("US-TX");

        modes.update(LAZY, "off");
        at(LAZY, "o1", -97.74, 30.27);
        settle();
        assertThat(tagger.tagRecord(LAZY, "o1")).isNull();
        assertThat(row(LAZY, "o1")).isNull();
        modes.update(LAZY, "eager");
    }

    @Test
    @DisplayName("a tombstone deletes the row")
    void tombstone() {
        at(POINTS, "t1", -97.74, 30.27);
        await().atMost(WAIT).until(() -> row(POINTS, "t1") != null);
        now = now.plusSeconds(30);
        clock.setInstant(now);
        records.ingest(POINTS, NormalizedRecord.builder("t1", POINT_KIND).observedAt(now).tombstone(true).build());
        await().atMost(WAIT).until(() -> row(POINTS, "t1") == null);
    }

    @Test
    @DisplayName("the durable cursor survives a restart: versions persisted while stopped are tagged on start")
    void cursorSurvivesRestart() {
        at(POINTS, "c1", -97.74, 30.27);
        settle();
        long before = worker.cursor();
        worker.stop();
        assertThat(worker.running()).isFalse();
        assertThat(jdbc.queryForObject("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.REGION_CURSOR).strip()).isEqualTo(Long.toString(before));

        at(POINTS, "c2", -95.36, 29.76);
        assertThat(row(POINTS, "c2")).as("stopped: not tagged").isNull();

        worker.start();
        await().atMost(WAIT).until(() -> row(POINTS, "c2") != null);
        assertThat(row(POINTS, "c2")).containsEntry("admin2", "48201");
        // The catch-up runs on its own thread; the cursor advances just after the row is written.
        await().atMost(WAIT).until(() -> worker.cursor() > before);
    }

    @Test
    @DisplayName("after a reference load every current record of an eager source is tagged again")
    void retagAll() {
        jdbc.update("TRUNCATE ref_admin2");
        tagger.resetState();
        at(POINTS, "r1", -97.74, 30.27);
        await().atMost(WAIT).until(() -> row(POINTS, "r1") != null);
        assertThat(row(POINTS, "r1").get("admin2")).isNull();

        ReferenceFixtures.load(context, List.of("ne-admin2-us"));
        await().atMost(WAIT).until(() -> "48453".equals(row(POINTS, "r1").get("admin2")));
        assertThat(worker.retagAll()).isPositive();
    }
}
