package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Daily situation snapshots and "what changed since" (prompt 118 §4): the snapshot of a day is written once per
 * non-closed situation, and {@code changes} diffs the snapshot of the requested day with the current state over two
 * days, without an LLM.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class SituationSnapshotIT {

    static final String STANDARD = TestIngestSources.STANDARD;
    static final Instant DAY1 = Instant.parse("2026-03-01T10:00:00Z");

    @Autowired SituationService service;
    @Autowired SituationSnapshots snapshots;
    @Autowired SituationSnapshotRepository repository;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    long record(String id, double lon, double lat, String severity) {
        Instant now = clock.instant();
        records.ingestAt(STANDARD, now, NormalizedRecord.builder(id, "test-record").observedAt(now).point(lon, lat)
                .put("title", "Thing " + id).put("severity", severity).build());
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, STANDARD, id);
    }

    String member(String id, long seq) {
        return "{\"memberType\":\"record\",\"ref\":\"" + STANDARD + "/" + id + "\",\"seq\":" + seq + "}";
    }

    @Test
    @DisplayName("a daily snapshot is written once, and /changes diffs two days")
    void snapshotAndChanges() {
        clock.setInstant(DAY1);
        long s1 = record("r1", 45.0, 12.0, "warning");
        SituationEntity situation = service.create(json.readTree("{\"title\":\"Gulf of Aden\",\"theme\":\"maritime\","
                + "\"members\":[" + member("r1", s1) + "]}"), "tester", "operator", false);
        assertThat(situation.getSeverity()).isEqualTo("warning");

        clock.setInstant(DAY1.plus(Duration.ofHours(14)).plusSeconds(600));
        LocalDate day1 = LocalDate.parse("2026-03-01");
        assertThat(snapshots.snapshot(day1)).isEqualTo(1);
        assertThat(snapshots.snapshot(day1)).isZero();
        SituationSnapshotEntity snapshot = repository.findBySituationIdAndDay(situation.getId(), day1).orElseThrow();
        assertThat(snapshot.getStatus()).isEqualTo(situation.getStatus());
        assertThat(snapshot.getSeverity()).isEqualTo("warning");
        assertThat(snapshot.getNewMembers()).isEqualTo(1);
        assertThat(snapshot.getMemberCounts()).containsKeys("byType", "byFamily");
        assertThat(snapshot.getKeyValues()).isEmpty();
        assertThat(snapshot.getAreaKm2()).isNotNull();

        // Day two: a critical member far enough away to grow the area.
        clock.setInstant(DAY1.plus(Duration.ofDays(1)));
        long s2 = record("r2", 46.5, 12.8, "critical");
        service.addMember(situation.getId(), json.readTree(member("r2", s2)), "tester", "operator", false);

        Map<String, Object> changes = snapshots.changes(service.get(situation.getId()), DAY1);
        assertThat(changes).containsEntry("baselineDay", "2026-03-01");
        @SuppressWarnings("unchecked")
        Map<String, Object> severity = (Map<String, Object>) changes.get("severity");
        assertThat(severity).containsEntry("before", "warning").containsEntry("after", "critical")
                .containsEntry("changed", true);
        @SuppressWarnings("unchecked")
        Map<String, Object> newMembers = (Map<String, Object>) changes.get("newMembers");
        assertThat(newMembers).containsEntry("total", 1);
        @SuppressWarnings("unchecked")
        Map<String, Object> area = (Map<String, Object>) changes.get("area");
        assertThat(((Number) area.get("deltaKm2")).doubleValue()).isPositive();

        // A situation that started after `since` has no baseline: everything is new.
        Map<String, Object> earlier = snapshots.changes(service.get(situation.getId()),
                DAY1.minus(Duration.ofDays(3)));
        assertThat(earlier.get("baselineDay")).isNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> all = (Map<String, Object>) earlier.get("newMembers");
        assertThat(all).containsEntry("total", 2);
    }

    @Test
    @DisplayName("closed situations get no snapshot")
    void closedSkipped() {
        clock.setInstant(DAY1);
        long s1 = record("r1", 45.0, 12.0, "info");
        SituationEntity situation = service.create(json.readTree("{\"title\":\"Closed one\",\"theme\":\"mixed\","
                + "\"members\":[" + member("r1", s1) + "]}"), "tester", "operator", false);
        service.patch(situation.getId(), json.readTree("{\"status\":\"closed\"}"), "tester", false);
        assertThat(snapshots.snapshot(LocalDate.parse("2026-03-01"))).isZero();
    }
}
