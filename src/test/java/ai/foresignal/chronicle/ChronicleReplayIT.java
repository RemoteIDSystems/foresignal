package ai.foresignal.chronicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.Scope;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.track.TrackResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Replay from chronicle storage only (prompt 131): the state at T matches what was recorded by then (latest version
 * per entity observed and known at T, tombstones and expiry respected, nothing ingested after T), tiles and tracks are
 * drawn from captured versions even after the global rows are gone, and the frame index, series and summary read the
 * chronicle's own tables.
 */
@IntegrationTest
@Import(AskTestSources.class)
class ChronicleReplayIT extends ChronicleItSupport {

    static final ChronicleReplayService.Reader ANYONE = new ChronicleReplayService.Reader(Set.of(), null);

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> items(Map<String, Object> state) {
        List<Map<String, Object>> layers = (List<Map<String, Object>>) state.get("layers");
        return (List<Map<String, Object>>) layers.getFirst().get("items");
    }

    private ChronicleEntity recorded(Person jo) {
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Replay','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        standard("mover", T.plusSeconds(60), 42, 12, 1);
        standard("mover", T.plusSeconds(120), 43, 13, 2);
        standard("mover", T.plusSeconds(180), 44, 12, 3);
        standardGone("mover", T.plusSeconds(240));
        clock.setInstant(T.plusSeconds(300));
        records.ingestAt(TestIngestSources.STANDARD, T.plusSeconds(300), NormalizedRecord.builder("late",
                TestIngestSources.STANDARD_KIND).observedAt(T.plusSeconds(90)).put("v", 9).point(42, 12).build());
        capture.catchUp();
        return chronicleRows.findById(c.getId()).orElseThrow();
    }

    @Test
    @DisplayName("state at several instants is what was recorded then, with no look-ahead")
    void stateMatchesTheRecordedTruth() {
        ChronicleEntity c = recorded(person("jo-state", Scope.AGENT));

        assertThat(items(replay.state(c, T.plusSeconds(30), null, null, null, 100, ANYONE))).isEmpty();
        assertThat(items(replay.state(c, T.plusSeconds(100), null, null, null, 100, ANYONE)))
                .as("'late' was observed at +90 s but only known at +300 s").singleElement()
                .satisfies(i -> assertThat(((Map<?, ?>) i.get("payload")).get("v")).isEqualTo(1));
        List<Map<String, Object>> at150 = items(replay.state(c, T.plusSeconds(150), null, null, null, 100, ANYONE));
        assertThat(at150).singleElement().satisfies(i -> {
            assertThat(i.get("externalId")).isEqualTo("mover");
            assertThat(((Map<?, ?>) i.get("payload")).get("v")).isEqualTo(2);
            assertThat(i.get("ref").toString()).startsWith("record:" + TestIngestSources.STANDARD + "/mover@");
        });
        List<Map<String, Object>> at260 = items(replay.state(c, T.plusSeconds(260), null, null, null, 100, ANYONE));
        assertThat(at260).as("the tombstone ends the mover").isEmpty();
        List<Map<String, Object>> at400 = items(replay.state(c, T.plusSeconds(400), null, null, null, 100, ANYONE));
        assertThat(at400).extracting(i -> i.get("externalId")).containsExactly("late");

        Map<String, Object> boxed = replay.state(c, T.plusSeconds(150), null,
                List.of(new org.locationtech.jts.geom.Envelope(0, 10, 0, 10)), null, 100, ANYONE);
        assertThat(items(boxed)).isEmpty();
        assertThat((List<?>) replay.state(c, T.minusSeconds(3600), null, null, null, 100, ANYONE).get("caveats"))
                .isNotEmpty();
    }

    @Test
    @DisplayName("state pages through one layer with its cursor")
    void statePages() {
        Person jo = person("jo-pages", Scope.AGENT);
        ChronicleEntity c = activate(jo, create(jo, "{'name':'Pages','layers':["
                + standardLayer("Standard", "{'mode':'full'}") + "]}"));
        for (int i = 0; i < 5; i++) {
            standard("p" + i, T.plusSeconds(10 + i), 42, 12, i);
        }
        capture.catchUp();
        String layer = firstLayer(c.getId()).getId();
        Map<String, Object> first = replay.state(c, T.plusSeconds(60), layer, null, null, 2, ANYONE);
        @SuppressWarnings("unchecked")
        Map<String, Object> page = ((List<Map<String, Object>>) first.get("layers")).getFirst();
        assertThat(page.get("count")).isEqualTo(5L);
        assertThat((List<?>) page.get("items")).hasSize(2);
        String cursor = (String) page.get("nextCursor");
        assertThat(cursor).isNotNull();
        Map<String, Object> second = replay.state(c, T.plusSeconds(60), null, null, cursor, 2, ANYONE);
        assertThat(items(second)).extracting(i -> i.get("externalId")).containsExactly("p2", "p3");
    }

    @Test
    @DisplayName("tiles and tracks are drawn from captured versions after the global rows are gone")
    void tilesAndTracksFromChronicleStorage() {
        ChronicleEntity c = recorded(person("jo-tiles", Scope.AGENT));
        jdbc.update("DELETE FROM record_version WHERE source_id = ?", TestIngestSources.STANDARD);

        ChronicleReplayService.TileBytes tile = replay.tile(c, new TileCoordinates(4, 9, 7), T.plusSeconds(150),
                List.of(), ANYONE);
        assertThat(tile.bytes()).isNotEmpty();
        assertThat(tile.truncated()).isFalse();
        ChronicleReplayService.TileBytes empty = replay.tile(c, new TileCoordinates(4, 9, 7), T.plusSeconds(30),
                List.of(), ANYONE);
        assertThat(empty.bytes()).isEmpty();

        TrackResult track = replay.track(c, TestIngestSources.STANDARD, "mover", null, null, 2_000, ANYONE);
        assertThat(track.pointsScanned()).isEqualTo(4);
        assertThat(track.coordinates()).singleElement().satisfies(segment -> assertThat(segment).hasSize(3));
        assertThat(track.times().getFirst().getFirst()).isEqualTo(T.plusSeconds(60).toEpochMilli());
        assertThatThrownBy(() -> replay.track(c, TestIngestSources.STANDARD, "nobody", null, null, 2_000, ANYONE))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> replay.track(c, TestIngestSources.STANDARD, "mover", null, null, 2_000,
                new ChronicleReplayService.Reader(Set.of(TestIngestSources.STANDARD), null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("the frame index counts captured versions per step and lists the changed entities")
    void frameIndex() {
        ChronicleEntity c = recorded(person("jo-frames", Scope.AGENT));
        Map<String, Object> index = replay.frames(c, T, T.plusSeconds(300), Duration.ofMinutes(1), List.of(), ANYONE);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> frames = (List<Map<String, Object>>) index.get("frames");
        assertThat(frames).hasSize(5);
        assertThat(index.get("truncated")).isEqualTo(false);
        @SuppressWarnings("unchecked")
        Map<String, Object> minuteOne = (Map<String, Object>) frames.get(1).get("counts");
        assertThat(List.copyOf(minuteOne.values())).containsExactly((Object) 2L);
        assertThat(frames.get(1).get("changedTotal")).isEqualTo(2L);
        assertThat((List<?>) frames.get(1).get("changed")).hasSize(2);
        Map<String, Object> tooMany = replay.frames(c, T, T.plus(Duration.ofDays(30)), Duration.ofSeconds(1),
                List.of(), ANYONE);
        assertThat(tooMany.get("truncated")).isEqualTo(true);
        assertThat((List<?>) tooMany.get("frames")).hasSize(ChronicleReplayService.MAX_FRAMES);
    }

    @Test
    @DisplayName("series points and the summary come from the chronicle's own tables")
    void seriesAndSummary() {
        ChronicleEntity c = recorded(person("jo-series", Scope.AGENT));
        String layer = firstLayer(c.getId()).getId();
        Instant day = Instant.parse("2026-10-01T00:00:00Z");
        for (int i = 0; i < 3; i++) {
            storage.upsertSeriesPoint(c.getId(), new ChronicleStorage.SeriesPointWrite(layer,
                    "series:eia-steo/BREPUUS", "2026-10-0" + (i + 1), day.plus(Duration.ofDays(i)),
                    day.plus(Duration.ofDays(i + 1)), 80.0 + i, "USD/bbl", "final", 1, day.plus(Duration.ofDays(i + 2)),
                    true, null, false), T);
        }
        storage.upsertSeriesPoint(c.getId(), new ChronicleStorage.SeriesPointWrite(layer, "series:eia-steo/BREPUUS",
                "2026-10-01", day, day.plus(Duration.ofDays(1)), 79.5, "USD/bbl", "final", 2,
                day.plus(Duration.ofDays(5)), true, null, false), T);
        Map<String, Object> series = replay.series(c, "series:eia-steo/BREPUUS", null, null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> points = (List<Map<String, Object>>) series.get("points");
        assertThat(points).hasSize(3);
        assertThat(points.getFirst().get("value")).as("the revision replaced the first point").isEqualTo(79.5);
        assertThat((List<?>) replay.series(c, "series:eia-steo/BREPUUS", day.plus(Duration.ofDays(1)), null)
                .get("points")).hasSize(2);

        Map<String, Object> summary = replay.summary(c, ai.foresignal.object.ObjectCaller.system(), ANYONE);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> top = (List<Map<String, Object>>) summary.get("topEntities");
        assertThat(top.getFirst().get("count")).isEqualTo(4L);
        assertThat(((Map<?, ?>) summary.get("storage")).get("bytesUsed")).isNotNull();
        assertThat((List<?>) summary.get("gaps")).isEmpty();
    }
}
