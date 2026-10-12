package ai.foresignal.store.purge;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.ingest.QaFlags;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.store.NotificationEntity;
import ai.foresignal.store.NotificationRepository;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * Entity purge end to end (prompt 039): the writer's {@code EntityPurge} command on both tiers, the
 * tombstone change with {@code purged}, the later {@code HeadPurge}, and the fan-out (alerts,
 * notifications, embeddings, region rows) with the report counts. Records are seeded only through
 * {@code TestRecords}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class EntityPurgeIT extends RetentionItSupport {

    @Autowired EntityPurgeService purges;
    @Autowired PurgeFanout fanout;
    @Autowired AlertRepository alerts;
    @Autowired NotificationRepository notifications;
    @Autowired RecordQueryService queries;
    @Autowired RecordMapper mapper;

    static NormalizedRecord standard(String id, Instant at, int value) {
        return NormalizedRecord.builder(id, STANDARD_KIND).observedAt(at).point(10.0 + value, 50.0)
                .put("v", value).build();
    }

    static NormalizedRecord aircraft(String id, Instant at, double lon) {
        return NormalizedRecord.builder(id, TRACK_KIND).observedAt(at).point(lon, 50.0)
                .put("callsign", id.toUpperCase()).put("alt", 30000).put("gs", 400).put("track", 90).build();
    }

    private void seedStandard(String id, int count) {
        for (int i = 0; i < count; i++) {
            Instant at = T0.plus(Duration.ofMinutes(i));
            ingestAt(STANDARD, at, standard(id, at, i));
        }
    }

    private AlertEntity alert(String sourceId, String externalId) {
        AlertEntity a = new AlertEntity(UUID.randomUUID(), 1L, sourceId, externalId, T0);
        a.setSeverity("warning");
        a.setMessage("Entity " + externalId + " entered the area");
        a.setRecordSnapshot(Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(10.0, 50.0))));
        return alerts.saveAndFlush(a);
    }

    private NotificationEntity notification(long alertId) {
        NotificationEntity n = new NotificationEntity();
        n.setEvent("alert");
        n.setSubtype("alert.fired");
        n.setSeverity("warning");
        n.setTitle("Entity entered the area");
        n.setBodyMarkdown("At 50.0, 10.0");
        n.setData(Map.of("externalId", "secret"));
        n.setAlertId(alertId);
        n.setCreatedAt(T0);
        return notifications.saveAndFlush(n);
    }

    @Test
    @DisplayName("a STANDARD purge writes a PURGED tombstone, deletes every older version and tombstones head and live")
    void standardPurge() {
        seedStandard("p-std", 3);
        seedStandard("p-keep", 2);
        long before = versions(STANDARD, "p-std");
        assertThat(before).isEqualTo(3);

        clock.setInstant(T0.plus(Duration.ofHours(1)));
        PurgeReport report = purges.purge(STANDARD, "p-std", false);

        assertThat(report.versionsDeleted()).isEqualTo(3);
        assertThat(report.tombstoneSeq()).isPositive();
        assertThat(versions(STANDARD, "p-std")).isEqualTo(1);
        Map<String, Object> tomb = jdbc.queryForMap("SELECT seq, persist_reason, qa_flags, tombstone, payload::text AS p,"
                + " geom IS NULL AS no_geom FROM record_version WHERE source_id = ? AND external_id = ?", STANDARD, "p-std");
        assertThat(((Number) tomb.get("seq")).longValue()).isEqualTo(report.tombstoneSeq());
        assertThat(((Number) tomb.get("persist_reason")).intValue()).isEqualTo(PersistReason.TOMBSTONE.code());
        assertThat(((Number) tomb.get("qa_flags")).intValue()).isEqualTo(QaFlags.PURGED);
        assertThat(tomb.get("tombstone")).isEqualTo(true);
        assertThat(tomb.get("p")).isEqualTo("{}");
        assertThat(tomb.get("no_geom")).isEqualTo(true);
        // No version of the entity predates its tombstone.
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?"
                + " AND seq < ?", Long.class, STANDARD, "p-std", report.tombstoneSeq())).isZero();

        Map<String, Object> head = jdbc.queryForMap("SELECT seq, tombstone, payload::text AS p, persisted_geom IS NULL"
                + " AS no_geom, title FROM record_head WHERE source_id = ? AND external_id = ?", STANDARD, "p-std");
        assertThat(((Number) head.get("seq")).longValue()).isEqualTo(report.tombstoneSeq());
        assertThat(head.get("tombstone")).isEqualTo(true);
        assertThat(head.get("p")).isEqualTo("{}");
        assertThat(head.get("no_geom")).isEqualTo(true);
        assertThat(head.get("title")).isNull();
        assertThat(jdbc.queryForObject("SELECT geom IS NULL FROM record_live WHERE source_id = ? AND external_id = ?",
                Boolean.class, STANDARD, "p-std")).isTrue();
        // Another entity is untouched.
        assertThat(versions(STANDARD, "p-keep")).isEqualTo(2);
    }

    @Test
    @DisplayName("a TRACK purge deletes versions on the TRACK tier too")
    void trackPurge() {
        for (int i = 0; i < 4; i++) {
            Instant at = T0.plus(Duration.ofSeconds(30L * i));
            ingestAt(TRACK, at, aircraft("p-trk", at, 1.0 + i));
        }
        long before = versions(TRACK, "p-trk");
        assertThat(before).isPositive();
        clock.setInstant(T0.plus(Duration.ofMinutes(10)));

        PurgeReport report = purges.purge(TRACK, "p-trk", false);

        assertThat(report.versionsDeleted()).isEqualTo(before);
        assertThat(versions(TRACK, "p-trk")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT history_tier FROM record_version WHERE source_id = ? AND external_id = ?",
                Integer.class, TRACK, "p-trk")).isEqualTo(1);
    }

    @Test
    @DisplayName("the tombstone is published with purged: true and /v1/changes renders purged")
    void tombstoneChangeCarriesPurged() {
        seedStandard("p-chg", 2);
        List<ChangeSummary> seen = new CopyOnWriteArrayList<>();
        var subscription = changeBus.subscribe("entity-purge-it", new ChangeListener() {
            @Override
            public void onBatch(ChangeBatch batch) {
                seen.addAll(batch.changes());
            }

            @Override
            public void onGap(long lastDeliveredSeq) {
                // not expected
            }
        }, 100);
        try {
            clock.setInstant(T0.plus(Duration.ofHours(1)));
            PurgeReport report = purges.purge(STANDARD, "p-chg", false);
            await().atMost(Duration.ofSeconds(10)).until(() -> seen.stream()
                    .anyMatch(c -> c.seq() == report.tombstoneSeq()));
            ChangeSummary change = seen.stream().filter(c -> c.seq() == report.tombstoneSeq()).findFirst().orElseThrow();
            assertThat(change.purged()).isTrue();
            assertThat(change.tombstone()).isTrue();
            assertThat(change.persistReason()).isEqualTo(PersistReason.TOMBSTONE);
            assertThat(change.prevPersistedBbox()).as("so area-filtered clients still get it").isNotNull();
            assertThat(mapper.streamChange(change, false)).containsEntry("purged", true);

            RecordRow row = queries.changesBySeq(List.of(report.tombstoneSeq())).get(report.tombstoneSeq());
            assertThat(mapper.change(row, FieldSelection.ALL)).containsEntry("purged", true);
        } finally {
            subscription.close();
        }
    }

    @Test
    @DisplayName("ordinary tombstones never carry purged")
    void ordinaryChangesHaveNoPurged() {
        seedStandard("p-plain", 1);
        long seq = jdbc.queryForObject("SELECT max(seq) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, STANDARD, "p-plain");
        RecordRow row = queries.changesBySeq(List.of(seq)).get(seq);
        assertThat(mapper.change(row, FieldSelection.ALL)).doesNotContainKey("purged");
    }

    @Test
    @DisplayName("a later HeadPurge removes the tombstoned head once retention removed the tombstone (I7)")
    void headRemovedByLaterHeadPurge() {
        seedStandard("p-head", 2);
        Instant purgeAt = T0.plus(Duration.ofHours(1));
        clock.setInstant(purgeAt);
        purges.purge(STANDARD, "p-head", false);
        assertThat(head(STANDARD, "p-head")).isTrue();

        runAt(purgeAt.plus(Duration.ofDays(400)));

        assertThat(versions(STANDARD, "p-head")).isZero();
        assertThat(head(STANDARD, "p-head")).isFalse();
        assertThat(live(STANDARD, "p-head")).isFalse();
    }

    @Test
    @DisplayName("alerts are redacted by default and deleted with purgeAlerts; notifications are redacted")
    void alertsRedactedOrDeleted() {
        seedStandard("p-alert", 1);
        seedStandard("p-alert2", 1);
        AlertEntity redactMe = alert(STANDARD, "p-alert");
        NotificationEntity note = notification(redactMe.getId());
        AlertEntity deleteMe = alert(STANDARD, "p-alert2");
        AlertEntity other = alert(STANDARD, "p-other");
        clock.setInstant(T0.plus(Duration.ofHours(1)));

        PurgeReport redacted = purges.purge(STANDARD, "p-alert", false);
        assertThat(redacted.alertsRedacted()).isEqualTo(1);
        assertThat(redacted.alertsDeleted()).isZero();
        assertThat(redacted.notificationsRedacted()).isEqualTo(1);
        Map<String, Object> row = jdbc.queryForMap("SELECT record_snapshot::text AS s, message FROM alert WHERE id = ?",
                redactMe.getId());
        assertThat(row.get("s").toString()).contains("\"redacted\"").doesNotContain("coordinates");
        assertThat(row.get("message")).isEqualTo(PurgeFanout.REDACTED);
        Map<String, Object> n = jdbc.queryForMap("SELECT title, body_markdown, data::text AS d FROM notification"
                + " WHERE id = ?", note.getId());
        assertThat(n.get("body_markdown")).isNull();
        assertThat(n.get("d").toString()).contains("redacted").doesNotContain("secret");

        PurgeReport deleted = purges.purge(STANDARD, "p-alert2", true);
        assertThat(deleted.alertsDeleted()).isEqualTo(1);
        assertThat(deleted.alertsRedacted()).isZero();
        assertThat(alerts.findById(deleteMe.getId())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT message FROM alert WHERE id = ?", String.class, other.getId()))
                .startsWith("Entity p-other");
    }

    @Test
    @DisplayName("embedding rows of the record and its alerts, and record_region rows, are deleted")
    void embeddingsAndRegionRowsGone() {
        seedStandard("p-emb", 1);
        AlertEntity a = alert(STANDARD, "p-emb");
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('record', ?, ?, 'text', 1, now())", STANDARD + ":p-emb", STANDARD);
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('alert', ?, ?, 'summary', 2, now())", Long.toString(a.getId()), STANDARD);
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, source_id, text, text_hash, updated_at)"
                + " VALUES ('record', ?, ?, 'kept', 3, now())", STANDARD + ":p-emb-other", STANDARD);
        jdbc.execute("CREATE TABLE IF NOT EXISTS record_region (source_id text, external_id text, region text)");
        try {
            jdbc.update("INSERT INTO record_region VALUES (?, 'p-emb', 'r1'), (?, 'p-emb-other', 'r1')",
                    STANDARD, STANDARD);
            clock.setInstant(T0.plus(Duration.ofHours(1)));

            PurgeReport report = purges.purge(STANDARD, "p-emb", false);

            assertThat(report.embeddingsDeleted()).isEqualTo(2);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM embedding_document WHERE doc_id = ?", Long.class,
                    STANDARD + ":p-emb-other")).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM record_region WHERE external_id = 'p-emb'",
                    Long.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM record_region WHERE external_id = 'p-emb-other'",
                    Long.class)).isEqualTo(1);
        } finally {
            jdbc.execute("DROP TABLE IF EXISTS record_region");
        }
        assertThat(fanout.deleteRegionRows(STANDARD, "p-emb")).as("no table, no-op").isZero();
    }

    @Test
    @DisplayName("the report counts citing documents and an unknown entity is 404")
    void reportAndNotFound() {
        seedStandard("p-cite", 1);
        clock.setInstant(T0.plus(Duration.ofHours(1)));
        PurgeReport report = purges.purge(STANDARD, "p-cite", false);
        assertThat(report.citingDocuments()).isEqualTo(new PurgeReport.CitingDocuments(0, 0));
        assertThatThrownBy(() -> purges.purge(STANDARD, "never-seen", false)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> purges.purge("no-such-source", "x", false)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("a purge is ordered with concurrent batches on the writer")
    void orderingWithConcurrentBatches() {
        seedStandard("p-order", 1);
        Instant t1 = T0.plus(Duration.ofMinutes(5));
        clock.setInstant(t1);
        CompletableFuture<IngestOutcome> first = records.submitAt(STANDARD, t1, List.of(standard("p-order", t1, 7)));
        PurgeReport report = purges.purge(STANDARD, "p-order", false);
        Instant t2 = T0.plus(Duration.ofHours(2));
        clock.setInstant(t2);
        IngestOutcome after = records.ingestAt(STANDARD, t2, standard("p-order", t2, 8));

        IngestOutcome firstOutcome = ai.foresignal.testsupport.ingest.TestRecords.await(first);
        assertThat(firstOutcome.toSeq()).isLessThan(report.tombstoneSeq());
        assertThat(after.fromSeq()).isGreaterThan(report.tombstoneSeq());
        assertThat(report.versionsDeleted()).as("the first batch's version was deleted too").isEqualTo(2);
        List<Long> seqs = jdbc.queryForList("SELECT seq FROM record_version WHERE source_id = ? AND external_id = ?"
                + " ORDER BY seq", Long.class, STANDARD, "p-order");
        assertThat(seqs).containsExactly(report.tombstoneSeq(), after.fromSeq());
        assertThat(jdbc.queryForObject("SELECT tombstone FROM record_head WHERE source_id = ? AND external_id = ?",
                Boolean.class, STANDARD, "p-order")).isFalse();
    }
}
