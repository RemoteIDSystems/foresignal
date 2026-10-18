package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Chronicle capture (prompt 131, CHR-1, CHR-2): matching versions are copied into the chronicle's own leaf; global
 * retention deleting the source rows leaves the chronicle intact; a cursor restart captures each version exactly once;
 * ingest never waits for capture (a burst is captured afterwards from {@code record_version}); layers apply from their
 * edit's seq onward; removing a layer keeps its rows unless purged.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleCaptureIT extends ChronicleItSupport {

    static final String AREA = "{'geometry':{'type':'Polygon','coordinates':[[[40,10],[45,10],[45,15],[40,15],[40,10]]]}}";

    @Test
    @DisplayName("matching versions inside the area are captured into the leaf, with first appearances in the thread")
    void matchingVersionsCaptured() {
        Person jo = person("jo-capture", Scope.AGENT);
        ChronicleEntity c = create(jo, "{'name':'Gulf watch','area':" + AREA + ",'layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}");
        assertThat(c.getStatus()).isEqualTo(ChronicleVocabulary.PROPOSED);
        assertThat(storage.leafExists(c.getId())).isFalse();
        activate(jo, c);
        assertThat(storage.leafExists(c.getId())).isTrue();

        standard("inside", T.plusSeconds(10), 42, 12, 1);
        standard("inside", T.plusSeconds(20), 42.5, 12.5, 2);
        standard("outside", T.plusSeconds(30), 10, 10, 1);
        ChronicleCaptureService.PassResult pass = capture.catchUp();

        assertThat(pass.versions()).isEqualTo(2);
        assertThat(capturedSeqs(c.getId(), "inside")).hasSize(2);
        assertThat(capturedSeqs(c.getId(), "outside")).isEmpty();
        ChronicleEntity after = chronicleRows.findById(c.getId()).orElseThrow();
        assertThat(after.getBytesUsed()).isPositive();
        assertThat(after.getRowsCaptured()).isEqualTo(2);
        assertThat(after.getCursorSeq()).isEqualTo(changeBus.lastPublishedSeq());
        assertThat(firstLayer(c.getId()).getRowsCaptured()).isEqualTo(2);
        assertThat(threadTypes(c.getId())).contains(ChronicleVocabulary.ITEM_STATUS_CHANGE,
                ChronicleVocabulary.ITEM_FIRST_APPEARANCE);
        assertThat(eventRows.findByChronicleIdAndTypeOrderByIdAsc(c.getId(), ChronicleVocabulary.ITEM_FIRST_APPEARANCE))
                .singleElement().satisfies(item -> assertThat(item.getTitle()).contains("Entity inside"));
    }

    @Test
    @DisplayName("deleting the global rows (as retention does) leaves the chronicle's copy and its replay intact")
    void globalRetentionLeavesTheChronicleIntact() {
        Person jo = person("jo-retention", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Keep','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        standard("e1", T.plusSeconds(10), 42, 12, 1);
        standard("e1", T.plusSeconds(20), 43, 13, 2);
        capture.catchUp();
        long kept = captured(c.getId());
        assertThat(kept).isEqualTo(2);

        jdbc.update("DELETE FROM record_version WHERE source_id = ?", TestIngestSources.STANDARD);
        assertThat(globalVersions(TestIngestSources.STANDARD, "e1")).isZero();

        assertThat(captured(c.getId())).isEqualTo(kept);
        Map<String, Object> state = replay.state(chronicleRows.findById(c.getId()).orElseThrow(),
                T.plusSeconds(25), null, null, null, 100, new ChronicleReplayService.Reader(java.util.Set.of(), null));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> layers = (List<Map<String, Object>>) state.get("layers");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) layers.getFirst().get("items");
        assertThat(items).singleElement().satisfies(i -> {
            assertThat(i.get("externalId")).isEqualTo("e1");
            assertThat(((Map<?, ?>) i.get("payload")).get("v")).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("re-reading after a cursor restart captures nothing twice")
    void cursorRestartExactlyOnce() {
        Person jo = person("jo-restart", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Once','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        for (int i = 0; i < 5; i++) {
            standard("e" + i, T.plusSeconds(10 + i), 42, 12, i);
        }
        capture.catchUp();
        ChronicleEntity first = chronicleRows.findById(c.getId()).orElseThrow();
        long rows = captured(c.getId());
        assertThat(rows).isEqualTo(5);

        jdbc.update("UPDATE chronicle SET cursor_seq = capture_from_seq WHERE id = ?", c.getId());
        capture.resetState();
        capture.catchUp();

        ChronicleEntity second = chronicleRows.findById(c.getId()).orElseThrow();
        assertThat(captured(c.getId())).isEqualTo(rows);
        assertThat(second.getBytesUsed()).isEqualTo(first.getBytesUsed());
        assertThat(second.getRowsCaptured()).isEqualTo(first.getRowsCaptured());
        assertThat(second.getCursorSeq()).isEqualTo(first.getCursorSeq());
    }

    @Test
    @DisplayName("a burst is ingested without waiting for capture, then captured from record_version")
    void ingestNeverWaitsForCapture() {
        Person jo = person("jo-burst", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Burst','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        long before = changeBus.lastPublishedSeq();
        long started = System.nanoTime();
        for (int i = 0; i < 200; i++) {
            standard("b" + i, T.plusSeconds(i), 42, 12, i);
            capture.onBatch(null);
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - started);

        assertThat(changeBus.lastPublishedSeq()).isGreaterThanOrEqualTo(before + 200);
        assertThat(chronicleRows.findById(c.getId()).orElseThrow().getCursorSeq())
                .as("nothing was captured while ingesting").isEqualTo(before);
        assertThat(elapsed).isLessThan(Duration.ofSeconds(60));

        capture.catchUp();
        assertThat(captured(c.getId())).isEqualTo(200);
    }

    @Test
    @DisplayName("a layer added later applies from its edit's seq; removing keeps its rows unless purged")
    void layerEditsApplyFromTheEditSeq() {
        Person jo = person("jo-layers", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Layers','layers':[{'name':'Big values','selector':"
                + "{'type':'records','sources':['" + TestIngestSources.STANDARD + "'],'conditions':"
                + "[{'path':'v','op':'>=','value':10}]},'capture':{'mode':'full'}}]}"));
        standard("small", T.plusSeconds(10), 42, 12, 5);
        standard("big", T.plusSeconds(11), 42, 12, 50);
        ChronicleLayerEntity all = chronicles.addLayer(c.getId(), node(standardLayer("All", "{'mode':'full'}")),
                jo.caller());
        assertThat(all.getAddedAtSeq()).isEqualTo(changeBus.lastPublishedSeq() + 1);
        standard("later", T.plusSeconds(12), 42, 12, 1);
        capture.catchUp();

        String bigLayer = chronicles.liveLayers(c.getId()).stream().filter(l -> l.getName().equals("Big values"))
                .findFirst().orElseThrow().getId();
        assertThat(captured(c.getId(), bigLayer)).isEqualTo(1);
        assertThat(captured(c.getId(), all.getId())).as("only the version after the edit").isEqualTo(1);
        assertThat(capturedSeqs(c.getId(), "small")).isEmpty();
        assertThat(capturedSeqs(c.getId(), "later")).hasSize(1);
        assertThat(threadTypes(c.getId())).contains(ChronicleVocabulary.ITEM_LAYER_EDIT);

        chronicles.removeLayer(c.getId(), all.getId(), false, jo.caller());
        assertThat(captured(c.getId(), all.getId())).isEqualTo(1);
        standard("after-removal", T.plusSeconds(20), 42, 12, 99);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "after-removal")).as("only the remaining layer").hasSize(1);
        assertThat(captured(c.getId(), all.getId())).isEqualTo(1);

        long bytesBefore = chronicleRows.findById(c.getId()).orElseThrow().getBytesUsed();
        chronicles.removeLayer(c.getId(), bigLayer, true, jo.caller());
        assertThat(captured(c.getId(), bigLayer)).isZero();
        assertThat(chronicleRows.findById(c.getId()).orElseThrow().getBytesUsed()).isLessThan(bytesBefore);
        assertThat(chronicles.liveLayers(c.getId())).isEmpty();
    }

    @Test
    @DisplayName("events-only captures exactly the event versions; a paused chronicle captures nothing")
    void eventsOnlyAndPause() {
        Person jo = person("jo-events", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Events','layers':["
                + standardLayer("Events", "{'mode':'events-only'}") + "]}"));
        Instant t = T.plusSeconds(10);
        standard("e", t, 42, 12, 1);
        standard("e", t.plusSeconds(1), 42, 12, 2);
        standardGone("e", t.plusSeconds(2));
        capture.catchUp();
        Long events = jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = 'e'"
                + " AND persist_reason IN (1, 2, 3, 4, 9)", Long.class, TestIngestSources.STANDARD);
        assertThat((long) capturedSeqs(c.getId(), "e").size()).isEqualTo(events);

        chronicles.pause(c.getId(), jo.caller());
        standard("paused", t.plusSeconds(5), 42, 12, 1);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "paused")).isEmpty();
        chronicles.resume(c.getId(), jo.caller());
        standard("resumed", t.plusSeconds(6), 42, 12, 1);
        capture.catchUp();
        assertThat(capturedSeqs(c.getId(), "paused")).as("the paused interval is not captured").isEmpty();
        assertThat(capturedSeqs(c.getId(), "resumed")).hasSize(1);
    }
}
