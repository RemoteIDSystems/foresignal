package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The chronicle lifecycle (prompt 131): proposed → active ⇄ paused → stopped → archived → stopped, with
 * {@code 409 CHRONICLE_STATE_INVALID} elsewhere; archiving writes a verified cold package and drops the leaf, restoring
 * brings every captured row back; deleting needs the display id and drops the leaf, the thread, the layers, the
 * workspace and the access list; the size cap warns at 90% and degrades full layers to events-only at 100%, visibly.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleLifecycleIT extends ChronicleItSupport {

    @Autowired ChronicleArchiver archiver;

    private static void conflict(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ForesignalException.class,
                e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.CHRONICLE_STATE_INVALID));
    }

    @Test
    @DisplayName("pause, resume and stop move through the lifecycle; invalid transitions are 409 CHRONICLE_STATE_INVALID")
    void transitions() {
        Person jo = person("jo-life", Scope.AGENT);
        ChronicleEntity c = create(jo, "{'name':'Life','layers':[" + standardLayer("S", "{'mode':'full'}") + "]}");
        conflict(() -> chronicles.pause(c.getId(), jo.caller()));
        conflict(() -> chronicles.stop(c.getId(), jo.caller()));
        activate(jo, c);
        conflict(() -> chronicles.activate(c.getId(), null, jo.caller(), false));
        conflict(() -> chronicles.resume(c.getId(), jo.caller()));
        conflict(() -> chronicles.archive(c.getId(), jo.caller()));
        assertThat(chronicles.pause(c.getId(), jo.caller()).getStatus()).isEqualTo(ChronicleVocabulary.PAUSED);
        assertThat(chronicles.resume(c.getId(), jo.caller()).getStatus()).isEqualTo(ChronicleVocabulary.ACTIVE);
        standard("x", T.plusSeconds(10), 42, 12, 1);
        capture.catchUp();
        ChronicleEntity stopped = chronicles.stop(c.getId(), jo.caller());
        assertThat(stopped.getStatus()).isEqualTo(ChronicleVocabulary.STOPPED);
        assertThat(stopped.getStoppedAt()).isNotNull();
        assertThat(stopped.getCaptureToSeq()).isEqualTo(changeBus.lastPublishedSeq());
        conflict(() -> chronicles.resume(c.getId(), jo.caller()));
        conflict(() -> chronicles.restore(c.getId(), jo.caller()));
        standard("after-stop", T.plusSeconds(20), 42, 12, 1);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "after-stop")).isEmpty();
        assertThat(capturedSeqs(c.getId(), "x")).hasSize(1);

        List<Map<String, Object>> statuses = new ArrayList<>();
        for (ChronicleEventEntity e : eventRows.findByChronicleIdAndTypeOrderByIdAsc(c.getId(),
                ChronicleVocabulary.ITEM_STATUS_CHANGE)) {
            statuses.add(e.getSnapshot());
        }
        assertThat(statuses).extracting(s -> s.get("status")).containsExactly("active", "paused", "active", "stopped");
        assertThat(statuses.get(1).get("previousStatus")).isEqualTo("active");
    }

    @Test
    @DisplayName("archive writes a verified cold package and drops the leaf; restore brings every row back as stopped")
    void archiveAndRestore() throws Exception {
        Person jo = person("jo-archive", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Archive','layers':["
                + standardLayer("S", "{'mode':'full'}") + "," + standardLayer("Counts", "{'mode':'rollup-only'}")
                + "]}"));
        for (int i = 0; i < 3; i++) {
            standard("a" + i, T.plusSeconds(10 + i), 42, 12, i);
        }
        capture.catchUp();
        long versions = captured(c.getId());
        Long rollups = jdbc.queryForObject("SELECT sum(versions) FROM chronicle_rollup WHERE chronicle_id = ?",
                Long.class, c.getId());
        assertThat(versions).isEqualTo(3);
        assertThat(rollups).isEqualTo(3);
        chronicles.stop(c.getId(), jo.caller());

        ChronicleEntity archived = chronicles.archive(c.getId(), jo.caller());
        assertThat(archived.getStatus()).isEqualTo(ChronicleVocabulary.ARCHIVED);
        assertThat(archived.getArchive()).containsEntry("state", "archived");
        assertThat(storage.leafExists(c.getId())).isFalse();
        assertThat(Files.isRegularFile(archiver.dir(c.getId()).resolve("manifest.json"))).isTrue();
        assertThat(Files.isRegularFile(archiver.dir(c.getId()).resolve("versions.jsonl.gz"))).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM chronicle_rollup WHERE chronicle_id = ?", Long.class,
                c.getId())).isZero();
        Map<String, Object> state = replay.state(archived, T.plusSeconds(60), null, null, null, 100,
                new ChronicleReplayService.Reader(Set.of(), null));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> caveats = (List<Map<String, Object>>) state.get("caveats");
        assertThat(caveats).extracting(cv -> cv.get("code")).contains((Object) "ARCHIVED");
        conflict(() -> chronicles.addLayer(c.getId(), node(standardLayer("Late", "{'mode':'full'}")), jo.caller()));

        ChronicleEntity restored = chronicles.restore(c.getId(), jo.caller());
        assertThat(restored.getStatus()).isEqualTo(ChronicleVocabulary.STOPPED);
        assertThat(storage.leafExists(c.getId())).isTrue();
        assertThat(captured(c.getId())).isEqualTo(versions);
        assertThat(jdbc.queryForObject("SELECT sum(versions) FROM chronicle_rollup WHERE chronicle_id = ?", Long.class,
                c.getId())).isEqualTo(rollups);
        assertThat(threadTypes(c.getId())).filteredOn(ChronicleVocabulary.ITEM_STATUS_CHANGE::equals).hasSize(4);
    }

    @Test
    @DisplayName("delete needs the display id, then drops the leaf, the thread, the layers, the workspace and the ACL")
    void deleteDropsEverything() {
        Person jo = person("jo-delete", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Delete','layers':["
                + standardLayer("S", "{'mode':'full'}") + "]}"));
        standard("d", T.plusSeconds(10), 42, 12, 1);
        capture.catchUp();
        String workspaceId = chronicleRows.findById(c.getId()).orElseThrow().getWorkspaceId();
        assertThat(workspaceId).isNotNull();

        assertThatThrownBy(() -> chronicles.delete(c.getId(), "nope", jo.caller()))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.CHRONICLE_CONFIRMATION_REQUIRED));
        Person other = person("jo-other", Scope.AGENT);
        assertThatThrownBy(() -> chronicles.delete(c.getId(), c.getDisplayId(), other.caller()))
                .isInstanceOfSatisfying(ForesignalException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ErrorCodes.UNKNOWN_CHRONICLE));

        chronicles.delete(c.getId(), c.getDisplayId(), jo.caller());

        assertThat(storage.leafExists(c.getId())).isFalse();
        assertThat(chronicleRows.findById(c.getId())).isEmpty();
        assertThat(eventRows.findByChronicleIdOrderByIdAsc(c.getId())).isEmpty();
        assertThat(layerRows.findByChronicleIdOrderByAddedAtAscIdAsc(c.getId())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM workspace WHERE id = ?", Long.class, workspaceId))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM shareable_object WHERE object_ref = ?", Long.class,
                "chronicle:" + c.getId())).isZero();
    }

    @Test
    @DisplayName("the cap warns at 90% and at 100% degrades full layers to events-only, in the thread and the inbox")
    void sizeCap() {
        Person jo = person("jo-cap", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Cap','layers':["
                + standardLayer("S", "{'mode':'full'}") + "]}"));
        standard("e00", T.plusSeconds(1), 42, 12, 100);
        capture.catchUp();
        long perRow = chronicleRows.findById(c.getId()).orElseThrow().getBytesUsed();
        assertThat(perRow).isPositive();
        chronicles.patch(c.getId(), node("{'sizeCapBytes':" + (perRow * 20) + "}"), null, jo.caller());

        List<String> states = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            standard(String.format("e%02d", i), T.plusSeconds(1 + i), 42, 12, 100 + i % 10);
            capture.catchUp();
            states.add(chronicleRows.findById(c.getId()).orElseThrow().getCapState());
        }
        assertThat(states).contains(ChronicleVocabulary.CAP_WARNING, ChronicleVocabulary.CAP_EXCEEDED);
        assertThat(states.indexOf(ChronicleVocabulary.CAP_WARNING))
                .isLessThan(states.indexOf(ChronicleVocabulary.CAP_EXCEEDED));
        assertThat(threadTypes(c.getId())).contains(ChronicleVocabulary.ITEM_CAP_WARNING,
                ChronicleVocabulary.ITEM_CAP_FALLBACK);
        assertThat(jdbc.queryForList("SELECT event FROM principal_inbox WHERE principal_id = ?", String.class,
                jo.id())).contains("chronicle.cap_warning");
        Map<String, Object> view = chronicles.view(chronicleRows.findById(c.getId()).orElseThrow(), jo.caller(), true);
        @SuppressWarnings("unchecked")
        Map<String, Object> layer = ((List<Map<String, Object>>) view.get("layers")).getFirst();
        assertThat(((Map<?, ?>) layer.get("effectiveCapture")).get("mode")).isEqualTo(CaptureMode.EVENTS_ONLY);

        standard("e00", T.plusSeconds(100), 42, 12, 999);
        capture.catchUp();
        Short reason = jdbc.queryForObject("SELECT persist_reason FROM record_version WHERE source_id = ? AND"
                + " external_id = 'e00' ORDER BY seq DESC LIMIT 1", Short.class, TestIngestSources.STANDARD);
        boolean event = ChronicleVocabulary.EVENT_REASONS.contains(PersistReason.fromCode(reason));
        assertThat(capturedSeqs(c.getId(), "e00")).hasSize(event ? 2 : 1);
        standard("brand-new", T.plusSeconds(101), 42, 12, 1);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "brand-new")).as("FIRST versions are events").hasSize(1);
    }
}
