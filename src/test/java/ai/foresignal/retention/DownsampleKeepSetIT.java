package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.DownsampleItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The keep set of history downsampling (prompt 044, Core-Data-Model §13), seeded only through
 * {@code TestRecords}: the kept reasons (FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE, TRIGGER_EDGE), the
 * newest seq per entity in the leaf, head- and alert-referenced seqs, one ordinary sample per entity per
 * interval, and a per-source interval override.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class DownsampleKeepSetIT extends DownsampleItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String FENCE = "{'type':'Polygon','coordinates':[[[20,49.5],[21,49.5],[21,50.5],[20,50.5],[20,49.5]]]}";

    @Autowired TriggerRuleService rules;
    @Autowired AlertRepository alerts;

    /** The seqs of {@code all} at the given minute indexes. */
    static List<Long> at(List<Long> all, int... indexes) {
        return IntStream.of(indexes).mapToObj(all::get).toList();
    }

    @Test
    @DisplayName("ordinary samples are thinned to the lowest seq per entity per 5-minute bucket; FIRST and the newest stay")
    void oneSamplePerInterval() {
        flyEveryMinute("a", 1.0, 30);
        List<Long> before = seqs(TRACK, "a");
        assertThat(before).hasSize(31);
        pushPastRawWindow();

        runDue();

        assertThat(bookkeeping(LEAF)).containsEntry("status", "DONE");
        // FIRST (minute 0); the lowest remaining seq of each bucket (1, 5, 10, 15, 20, 25); the newest (30).
        assertThat(seqs(TRACK, "a")).containsExactlyElementsOf(at(before, 0, 1, 5, 10, 15, 20, 25, 30));
        long head = jdbc.queryForObject("SELECT seq FROM record_head WHERE source_id = ? AND external_id = 'a'",
                Long.class, TRACK);
        assertThat(seqs(TRACK, "a")).as("the head's version still resolves").contains(head);
    }

    @Test
    @DisplayName("FIRST, SIGNIFICANT, TOMBSTONE and REAPPEAR are kept whatever their bucket")
    void keptReasons() {
        ingestAt(TRACK, minute(0), aircraft("b", minute(0), 10.0, "B1"));
        ingestAt(TRACK, minute(1), aircraft("b", minute(1), 10.0, "B2"));
        ingestAt(TRACK, minute(2), aircraft("b", minute(2), 10.05, "B2"));
        ingestAt(TRACK, minute(3), aircraft("b", minute(3), 10.10, "B2"));
        ingestAt(TRACK, minute(4), NormalizedRecord.tombstone("b", TRACK_KIND, minute(4)));
        ingestAt(TRACK, minute(5), aircraft("b", minute(5), 10.15, "B2"));
        ingestAt(TRACK, minute(6), aircraft("b", minute(6), 10.20, "B2"));
        ingestAt(TRACK, minute(7), aircraft("b", minute(7), 10.25, "B2"));
        List<Long> before = seqs(TRACK, "b");
        assertThat(reasons(TRACK, "b")).containsExactly(
                (int) PersistReason.FIRST.code(), (int) PersistReason.SIGNIFICANT.code(),
                (int) PersistReason.SAMPLE.code(), (int) PersistReason.SAMPLE.code(),
                (int) PersistReason.TOMBSTONE.code(), (int) PersistReason.REAPPEAR.code(),
                (int) PersistReason.SAMPLE.code(), (int) PersistReason.SAMPLE.code());
        pushPastRawWindow();

        runDue();

        // Bucket 0 (minutes 0-4): FIRST, SIGNIFICANT and TOMBSTONE are pinned; of the samples 2 and 3 only 2
        // stays. Bucket 1 (5-9): REAPPEAR and the newest (7) are pinned; sample 6 is the bucket's only other row.
        assertThat(seqs(TRACK, "b")).containsExactlyElementsOf(at(before, 0, 1, 2, 4, 5, 6, 7));
    }

    @Test
    @DisplayName("TRIGGER_EDGE versions and the seqs alerts reference are kept")
    void edgesAndAlerts() {
        UUID fence = rules.create(JSON.readTree(("{'name':'Fence','sources':['" + TRACK + "'],'geofence':" + FENCE
                + ",'mode':'enter'}").replace('\'', '"'))).rule().getId();
        try {
            ingestAt(TRACK, minute(0), aircraft("c", minute(0), 19.5, "C"));
            ingestAt(TRACK, minute(1), aircraft("c", minute(1), 19.6, "C"));
            ingestAt(TRACK, minute(2), aircraft("c", minute(2), 20.5, "C"));
            ingestAt(TRACK, minute(3), aircraft("c", minute(3), 20.6, "C"));
            ingestAt(TRACK, minute(4), aircraft("c", minute(4), 20.7, "C"));
            flyEveryMinute("d", 30.0, 4);
            List<Long> c = seqs(TRACK, "c");
            List<Long> d = seqs(TRACK, "d");
            assertThat(reasons(TRACK, "c").get(2)).isEqualTo((int) PersistReason.TRIGGER_EDGE.code());
            awaitTriggerCursor();
            // An alert on an ordinary sample of "d" that the interval rule alone would drop.
            alerts.save(new AlertEntity(UUID.randomUUID(), d.get(3), TRACK, "d", minute(3)));
            pushPastRawWindow();

            runDue();

            // c: FIRST 0, sample 1 (lowest remaining), TRIGGER_EDGE 2 (also its alert), newest 4; sample 3 goes.
            assertThat(seqs(TRACK, "c")).containsExactlyElementsOf(at(c, 0, 1, 2, 4));
            // d: FIRST 0, sample 1, alert-referenced 3, newest 4; sample 2 goes.
            assertThat(seqs(TRACK, "d")).containsExactlyElementsOf(at(d, 0, 1, 3, 4));
            List<Long> alertSeqs = jdbc.queryForList("SELECT seq FROM alert", Long.class);
            assertThat(alertSeqs).isNotEmpty();
            for (Long seq : alertSeqs) {
                assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE seq = ?", Long.class, seq))
                        .as("alert seq %d still resolves", seq).isEqualTo(1L);
            }
        } finally {
            rules.delete(fence);
        }
    }

    @Test
    @DisplayName("a per-source override of 10 minutes keeps one sample per 10-minute bucket")
    void perSourceOverride() {
        sources.update(TRACK, SourceSettingsUpdate.downsampleInterval(Duration.ofMinutes(10)));
        try {
            flyEveryMinute("a", 1.0, 30);
            List<Long> before = seqs(TRACK, "a");
            pushPastRawWindow();

            runDue();

            assertThat(seqs(TRACK, "a")).containsExactlyElementsOf(at(before, 0, 1, 10, 20, 30));
            assertThat(bookkeeping(LEAF)).containsEntry("default_interval_s", 300);
        } finally {
            sources.update(TRACK, SourceSettingsUpdate.clearingDownsampleInterval());
        }
    }

    @Test
    @DisplayName("the marker in the next leaf is untouched")
    void otherLeavesUntouched() {
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();
        long marker = seqs(TRACK, "marker").getFirst();

        runDue();

        assertThat(seqs(TRACK, "marker")).containsExactly(marker);
        assertThat(bookkeeping("rv_t_" + BLOCK)).isEmpty();
    }
}
