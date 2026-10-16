package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Investigations (prompt 101): items keep a snapshot taken when added (so evidence survives
 * retention), and the markdown and GeoJSON exports carry the banner and citations.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InvestigationIT {

    @Autowired InvestigationService investigations;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("a record item keeps its snapshot and the exports cite it")
    void snapshotAndExport() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("e1", "thing").observedAt(now).point(-95.0, 32.0).put("name", "x").build());
        Long seq = jdbc.queryForObject(
                "select max(seq) from record_version where source_id = ? and external_id = ?", Long.class,
                TestIngestSources.STANDARD, "e1");

        var investigation = investigations.create(json.readTree("{\"title\":\"Case\"}"), "tester");
        assertThat(investigation.getDisplayId()).startsWith("INV-");
        var item = investigations.addItem(investigation.getId(),
                json.readTree("{\"itemType\":\"record\",\"ref\":\"test-standard:e1\",\"seq\":" + seq + "}"), "tester", true);
        assertThat(item.getSnapshot()).containsKey("record");

        String markdown = investigations.export(investigation.getId(), "md", true);
        assertThat(markdown).contains(InvestigationService.BANNER).contains("[test-standard/e1@v");
        String geojson = investigations.export(investigation.getId(), "geojson", true);
        assertThat(geojson).contains("FeatureCollection").contains("foresignal:header");
    }

    @Test
    @DisplayName("an item keeps its snapshot after the referenced version is purged")
    void survivesPurge() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("p1", "thing").observedAt(now).point(-95.0, 32.0).put("name", "x").build());
        Long seq = jdbc.queryForObject(
                "select max(seq) from record_version where source_id = ? and external_id = ?", Long.class,
                TestIngestSources.STANDARD, "p1");
        var investigation = investigations.create(json.readTree("{\"title\":\"Purge\"}"), "tester");
        investigations.addItem(investigation.getId(),
                json.readTree("{\"itemType\":\"record\",\"ref\":\"test-standard/p1\",\"seq\":" + seq + "}"), "tester",
                true);

        jdbc.update("delete from record_version where seq = ?", seq);

        String markdown = investigations.export(investigation.getId(), "md", true);
        assertThat(markdown).contains("[test-standard/p1@v");
        assertThat(investigations.items(investigation.getId(), true).get(0).getSnapshot()).containsKey("record");
    }

    @Test
    @DisplayName("an oversized record snapshot is capped at 64 KB and says so")
    void snapshotCapped() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        records.ingestAt(TestIngestSources.STANDARD, now, NormalizedRecord.builder("big", "thing").observedAt(now)
                .point(-95.0, 32.0).put("blob", "x".repeat(80 * 1024)).build());
        Long seq = jdbc.queryForObject(
                "select max(seq) from record_version where source_id = ? and external_id = ?", Long.class,
                TestIngestSources.STANDARD, "big");
        var investigation = investigations.create(json.readTree("{\"title\":\"Big\"}"), "tester");
        var item = investigations.addItem(investigation.getId(),
                json.readTree("{\"itemType\":\"record\",\"ref\":\"test-standard/big\",\"seq\":" + seq + "}"), "tester",
                true);

        assertThat(SnapshotCaps.size(item.getSnapshot(), json)).isLessThanOrEqualTo(SnapshotCaps.ITEM_MAX_BYTES);
        assertThat(item.getSnapshot()).containsEntry("truncated", true);
    }

    @Test
    @DisplayName("display ids are sequential per year and unique")
    void displayIds() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        var first = investigations.create(json.readTree("{\"title\":\"One\"}"), "tester");
        var second = investigations.create(json.readTree("{\"title\":\"Two\"}"), "tester");
        assertThat(first.getDisplayId()).isEqualTo("INV-2026-0001");
        assertThat(second.getDisplayId()).isEqualTo("INV-2026-0002");
    }
}
