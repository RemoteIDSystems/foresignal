package ai.foresignal.stream.sse;

import static ai.foresignal.stream.sse.SseFixtures.T;
import static ai.foresignal.stream.sse.SseFixtures.box;
import static ai.foresignal.stream.sse.SseFixtures.boxFilter;
import static ai.foresignal.stream.sse.SseFixtures.change;
import static ai.foresignal.stream.sse.SseFixtures.live;
import static ai.foresignal.stream.sse.SseFixtures.touch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.support.NearFilter;
import ai.foresignal.stream.Bbox;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link ChangeFilter}'s three-box rule, {@link StreamFilter}, and the conflation drop on a swap. */
class StreamFilterTest {

    private static final String DALLAS = "-97.5,32.5,-96.5,33.2";

    @Test
    @DisplayName("bbox OR prevPersistedBbox OR prevLiveBbox matches; only a previous box gives leftFilter")
    void threeBoxMatching() {
        ChangeFilter f = boxFilter(DALLAS);
        Bbox in = box(-97.0, 32.8);
        Bbox out = box(-90.0, 40.0);
        assertThat(f.matchChange(change(1, "s", "k", in, null, null, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(2, "s", "k", in, out, out, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(3, "s", "k", out, in, null, false))).isEqualTo(FilterMatch.LEFT);
        assertThat(f.matchChange(change(4, "s", "k", out, null, in, false))).isEqualTo(FilterMatch.LEFT);
        assertThat(f.matchChange(change(5, "s", "k", out, out, out, false))).isEqualTo(FilterMatch.NONE);
        assertThat(f.matchChange(change(6, "s", "k", null, null, null, false))).isEqualTo(FilterMatch.NONE);
        // A tombstone carries the previous boxes and is delivered as LEFT.
        assertThat(f.matchChange(change(7, "s", "k", null, in, in, true))).isEqualTo(FilterMatch.LEFT);
    }

    @Test
    @DisplayName("without a spatial filter every change matches, boxes or not")
    void noSpatialFilter() {
        ChangeFilter f = ChangeFilter.all(Set.of());
        assertThat(f.spatial()).isFalse();
        assertThat(f.matchChange(change(1, "s", "k", null, null, null, false))).isEqualTo(FilterMatch.MATCH);
    }

    @Test
    @DisplayName("live events match on bbox or prevBbox with the same leftFilter rule")
    void liveMatching() {
        ChangeFilter f = boxFilter(DALLAS);
        assertThat(f.matchLive(live("a", T, box(-97, 33), null))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchLive(live("a", T, box(-90, 40), box(-97, 33)))).isEqualTo(FilterMatch.LEFT);
        assertThat(f.matchLive(live("a", T, box(-90, 40), box(-91, 40)))).isEqualTo(FilterMatch.NONE);
    }

    @Test
    @DisplayName("an antimeridian bbox matches on both sides and not in between")
    void antimeridian() {
        ChangeFilter f = boxFilter("170,-20,-170,20");
        assertThat(f.matchChange(change(1, "s", "k", box(175, 0), null, null, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(2, "s", "k", box(-175, 0), null, null, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(3, "s", "k", box(0, 0), null, null, false))).isEqualTo(FilterMatch.NONE);
        assertThat(f.matchChange(change(4, "s", "k", box(0, 0), box(179, 5), null, false))).isEqualTo(FilterMatch.LEFT);
    }

    @Test
    @DisplayName("near + radiusKm uses the nearest point of the box, across the antimeridian too")
    void nearRadius() {
        ChangeFilter f = new ChangeFilter(List.of(), List.of(), List.of(), null, new NearFilter(-96.80, 32.78, 50),
                false, true, false, Set.of());
        // Fort Worth (~48 km west) is inside, Austin (~300 km) is not.
        assertThat(f.matchChange(change(1, "s", "k", box(-97.33, 32.75), null, null, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(2, "s", "k", box(-97.74, 30.27), null, null, false))).isEqualTo(FilterMatch.NONE);
        // A large box whose edge passes near the center matches even though its corners are far.
        assertThat(f.matchChange(change(3, "s", "k", new Bbox(-96.7, 20, -80, 40), null, null, false)))
                .isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(4, "s", "k", box(-97.74, 30.27), box(-96.8, 32.8), null, false)))
                .isEqualTo(FilterMatch.LEFT);

        ChangeFilter dateLine = new ChangeFilter(List.of(), List.of(), List.of(), null, new NearFilter(179.9, 0, 50),
                false, true, false, Set.of());
        assertThat(dateLine.matchChange(change(5, "s", "k", box(-179.9, 0), null, null, false)))
                .isEqualTo(FilterMatch.MATCH);
        assertThat(ChangeFilter.haversineKm(0, 0, 0, 1)).isBetween(111.0, 111.4);
    }

    @Test
    @DisplayName("source, kind, externalId and excluded sources filter before any box")
    void entityFilters() {
        ChangeFilter f = new ChangeFilter(List.of("s"), List.of("k"), List.of("e1"), null, null, false, true, true,
                Set.of("private"));
        assertThat(f.matchChange(change(1, "s", "k", null, null, null, false))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchChange(change(2, "s", "k", null, null, null, false))).isEqualTo(FilterMatch.NONE);
        assertThat(f.matchChange(change(1, "other", "k", null, null, null, false))).isEqualTo(FilterMatch.NONE);
        assertThat(f.matchChange(change(1, "s", "other", null, null, null, false))).isEqualTo(FilterMatch.NONE);
        ChangeFilter excluding = ChangeFilter.all(Set.of("private"));
        assertThat(excluding.matchChange(change(1, "private", "k", null, null, null, false))).isEqualTo(FilterMatch.NONE);
    }

    @Test
    @DisplayName("a touch without a kind matches a kind filter through its source's declared kinds")
    void touchKinds() {
        ChangeFilter f = new ChangeFilter(List.of(), List.of("aircraft"), List.of(), null, null, false, true, true,
                Set.of());
        assertThat(f.matchTouch(touch("a", T, box(1, 1)), List.of("aircraft", "balloon"))).isEqualTo(FilterMatch.MATCH);
        assertThat(f.matchTouch(touch("a", T, box(1, 1)), List.of("vessel"))).isEqualTo(FilterMatch.NONE);
        assertThat(f.matchTouch(touch("a", T, box(1, 1)), null)).isEqualTo(FilterMatch.NONE);
    }

    @Test
    @DisplayName("StreamFilter needs a filter for every topic, starts at version 1 and counts versions")
    void streamFilterShape() {
        StreamFilter first = StreamFilter.changesOnly(ChangeFilter.all(Set.of()));
        assertThat(first.version()).isEqualTo(1);
        assertThat(first.subscribes(ChangesTopic.NAME)).isTrue();
        assertThat(first.changes()).isNotNull();
        assertThat(first.filterFor("alerts")).isNull();
        StreamFilter second = first.next(List.of(ChangesTopic.NAME), Map.of(ChangesTopic.NAME, boxFilter(DALLAS)));
        assertThat(second.version()).isEqualTo(2);
        assertThat(second.changes().spatial()).isTrue();
        assertThatThrownBy(() -> new StreamFilter(List.of("alerts"), Map.of(), 1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new StreamFilter(List.of(), Map.of(), 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a filter swap drops conflation entries that no longer match and emits filter-applied {atSeq}")
    void swapDropsNonMatchingConflationEntries() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.filter = StreamFilter.changesOnly(boxFilter("-180,-90,180,90"));
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> SseFixtures.fifo(7));
        client.pump();
        client.offerLive(live("dallas", T, box(-97, 33), null));
        client.offerLive(live("paris", T, box(2.35, 48.85), null));
        client.offerTouch(touch("tokyo", T, box(139.7, 35.7)), List.of("aircraft"));
        StreamFilter next = client.filter().next(List.of(ChangesTopic.NAME), Map.of(ChangesTopic.NAME, boxFilter(DALLAS)));
        client.applyFilter(next, Map.of());
        b.clock.advance(Duration.ofSeconds(1));
        client.pump();
        List<SseFrame> applied = b.sender.named("filter-applied");
        assertThat(applied).hasSize(1);
        assertThat(applied.getFirst().data()).isEqualTo("{\"atSeq\":7,\"filterVersion\":2}");
        assertThat(b.sender.named("touch")).isEmpty();
        assertThat(b.sender.named("live")).hasSize(1);
        assertThat(b.sender.named("live").getFirst().data()).contains("\"externalId\":\"dallas\"");
        assertThat(b.sender.events().indexOf("filter-applied")).isLessThan(b.sender.events().indexOf("live"));
        assertThat(client.filter().version()).isEqualTo(2);
    }

    @Test
    @DisplayName("events queued after a swap use the new filter")
    void eventsAfterSwapUseNewFilter() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        client.applyFilter(client.filter().next(List.of(ChangesTopic.NAME), Map.of(ChangesTopic.NAME, boxFilter(DALLAS))),
                Map.of());
        client.offerFifo(ChangesTopic.NAME, f -> ((ChangeFilter) f).spatial() ? SseFixtures.fifo(9) : null);
        client.pump();
        assertThat(b.sender.events()).containsExactly("filter-applied", "change");
    }
}
