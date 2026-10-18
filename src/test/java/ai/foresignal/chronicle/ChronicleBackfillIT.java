package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.retention.DownsampleRanges;
import ai.foresignal.retention.RetentionStateStore;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Backfill on activation (prompt 131): history still retained globally is copied and marked {@code backfilled};
 * caveats say where global history was already deleted or downsampled; capture after activation is not marked.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleBackfillIT extends ChronicleItSupport {

    @Autowired RetentionStateStore retentionState;
    @Autowired DownsampleRanges downsampleRanges;
    @Autowired ChronicleBackfill backfill;

    @AfterEach
    void reloadRetentionState() {
        retentionState.reload();
        downsampleRanges.refresh();
    }

    @Test
    @DisplayName("retained history from the backfill start is copied, marked backfilled, and reported in the thread")
    void retainedHistoryIsCopied() throws Exception {
        Person jo = person("jo-backfill", Scope.AGENT);
        standard("early", T.minusSeconds(3600), 42, 12, 0);
        standard("e1", T.plusSeconds(10), 42, 12, 1);
        standard("e1", T.plusSeconds(20), 43, 13, 2);
        ChronicleEntity c = create(jo, "{'name':'Backfilled','layers':[" + standardLayer("Standard", "{'mode':'full'}")
                + "]}");
        chronicles.activate(c.getId(), node("{'backfill':{'from':'" + T + "'}}"), jo.caller(), false);

        List<Map<String, Object>> rows = jdbc.queryForList("SELECT external_id, backfilled FROM chronicle_version"
                + " WHERE chronicle_id = ? ORDER BY seq", c.getId());
        assertThat(rows).extracting(r -> r.get("external_id")).containsExactly("e1", "e1");
        assertThat(rows).allSatisfy(r -> assertThat(r.get("backfilled")).isEqualTo(true));
        ChronicleEventEntity report = eventRows.findByChronicleIdAndTypeOrderByIdAsc(c.getId(),
                ChronicleVocabulary.ITEM_BACKFILL).getFirst();
        assertThat(report.isBackfilled()).isTrue();
        assertThat(report.getSnapshot().get("versions")).isEqualTo(2);
        assertThat((List<?>) report.getSnapshot().get("caveats")).isEmpty();

        standard("e1", T.plusSeconds(60), 44, 14, 3);
        capture.catchUp();
        Boolean live = jdbc.queryForObject("SELECT backfilled FROM chronicle_version WHERE chronicle_id = ?"
                + " ORDER BY seq DESC LIMIT 1", Boolean.class, c.getId());
        assertThat(live).isFalse();
        assertThat(chronicleRows.findById(c.getId()).orElseThrow().getRowsCaptured()).isEqualTo(3);
    }

    @Test
    @DisplayName("caveats name history already deleted by retention and history thinned by downsampling")
    void caveatsWhereHistoryIsThinnedOrGone() {
        Person jo = person("jo-caveats", Scope.AGENT);
        standard("old", T.plusSeconds(1), 42, 12, 1);
        standard("kept", T.plusSeconds(2), 42, 12, 2);
        long oldSeq = jdbc.queryForObject("SELECT seq FROM record_version WHERE external_id = 'old'", Long.class);
        long keptSeq = jdbc.queryForObject("SELECT seq FROM record_version WHERE external_id = 'kept'", Long.class);
        jdbc.update("DELETE FROM record_version WHERE seq = ?", oldSeq);
        jdbc.update("INSERT INTO retention_state (source_id, kind, floor_seq, purge_seq) VALUES (?, '*', ?, 0)",
                TestIngestSources.STANDARD, keptSeq);
        retentionState.reload();
        jdbc.update("INSERT INTO history_downsample (leaf_name, history_tier, from_seq, to_seq, default_interval_s,"
                + " started_at, status) VALUES ('rv_t_test', 1, 0, ?, 60, ?, 'DONE')", keptSeq + 10,
                Timestamp.from(T));
        downsampleRanges.refresh();

        ChronicleEntity c = create(jo, "{'name':'Caveats','layers':[" + standardLayer("Standard", "{'mode':'full'}")
                + "]}");
        activate(jo, c);
        ChronicleBackfill.Result result = backfill.run(c.getId(), T);

        assertThat(result.versions()).isEqualTo(1);
        assertThat(result.caveats()).extracting(cv -> cv.get("code")).containsExactlyInAnyOrder("HISTORY_GONE",
                "HISTORY_THINNED");
        assertThat(capturedSeqs(c.getId(), "kept")).containsExactly(keptSeq);
        assertThat(capturedSeqs(c.getId(), "old")).isEmpty();
    }

    @Test
    @DisplayName("a rollup-only layer counts backfilled versions per region and hour")
    void rollupBackfill() {
        Person jo = person("jo-rollup", Scope.AGENT);
        for (int i = 0; i < 4; i++) {
            standard("r" + i, T.plusSeconds(i), 42, 12, i);
        }
        ChronicleEntity c = create(jo, "{'name':'Counts','layers':[" + standardLayer("Counts",
                "{'mode':'rollup-only'}") + "]}");
        chronicles.activate(c.getId(), node("{'backfill':{'from':'" + T.minusSeconds(60) + "'}}"), jo.caller(),
                false);
        Long counted = jdbc.queryForObject("SELECT sum(versions) FROM chronicle_rollup WHERE chronicle_id = ?",
                Long.class, c.getId());
        assertThat(counted).isEqualTo(4);
        assertThat(captured(c.getId())).isZero();
        Boolean flagged = jdbc.queryForObject("SELECT bool_and(backfilled) FROM chronicle_rollup WHERE chronicle_id = ?",
                Boolean.class, c.getId());
        assertThat(flagged).isTrue();
    }
}
