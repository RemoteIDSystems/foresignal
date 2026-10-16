package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

/**
 * {@code foresignal-intel} (prompt 101): private events are never published as public records and a
 * published one is tombstoned when it turns private (EGR-1); every event is covered by paging, never a
 * fixed window; the published set is rebuilt from the source's own heads; incremental passes re-emit only
 * events updated since the previous pass (the context's own emit-on-change then skips unchanged payloads).
 */
class ForesignalIntelGeneratorTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    IntelligenceEventRepository events;
    IntelEventMemberRepository members;
    DerivedContext ctx;
    List<String> emitted;
    List<String> tombstoned;
    ForesignalIntelGenerator generator;

    @BeforeEach
    void setUp() {
        events = mock(IntelligenceEventRepository.class);
        members = mock(IntelEventMemberRepository.class);
        when(members.findByEventIdOrderByIdAsc(anyString())).thenReturn(List.of());
        ctx = mock(DerivedContext.class);
        emitted = new ArrayList<>();
        tombstoned = new ArrayList<>();
        Mockito.doAnswer(i -> emitted.add(((NormalizedRecord) i.getArgument(0)).externalId())).when(ctx).emit(any());
        Mockito.doAnswer(i -> tombstoned.add(i.getArgument(1))).when(ctx).tombstone(anyString(), anyString());
        generator = new ForesignalIntelGenerator(events, members);
    }

    private static IntelligenceEventEntity event(String id, boolean privateEvent) {
        IntelligenceEventEntity event = new IntelligenceEventEntity(id, "custom", "Event " + id, NOW);
        event.setStatus("active");
        event.setPrivateEvent(privateEvent);
        event.setUpdatedAt(NOW);
        return event;
    }

    private void all(List<IntelligenceEventEntity> rows) {
        when(events.findAllByOrderByIdAsc(any(Pageable.class))).thenAnswer(i -> page(rows, i.getArgument(0)));
    }

    private static SliceImpl<IntelligenceEventEntity> page(List<IntelligenceEventEntity> rows, Pageable pageable) {
        int from = (int) pageable.getOffset();
        int to = Math.min(rows.size(), from + pageable.getPageSize());
        List<IntelligenceEventEntity> content = from >= rows.size() ? List.of() : rows.subList(from, to);
        return new SliceImpl<>(content, pageable, to < rows.size());
    }

    @Test
    @DisplayName("a private event is never emitted; a published event that turns private is tombstoned")
    void privateNeverPublished() {
        IntelligenceEventEntity open = event("01A", false);
        IntelligenceEventEntity secret = event("01B", true);
        all(List.of(open, secret));
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW, ctx);
        assertThat(emitted).containsExactly("01A");

        open.setPrivateEvent(true);
        open.setUpdatedAt(NOW.plusSeconds(30));
        when(events.findByUpdatedAtGreaterThanEqualOrderByIdAsc(any(), any(Pageable.class)))
                .thenAnswer(i -> page(List.of(open), i.getArgument(1)));
        generator.onTick(NOW.plusSeconds(30), ctx);
        assertThat(tombstoned).containsExactly("01A");
        assertThat(generator.published("01A")).isFalse();
    }

    @Test
    @DisplayName("a full pass pages through every event, however many there are")
    void pagesThroughAll() {
        List<IntelligenceEventEntity> rows = new ArrayList<>();
        for (int i = 0; i < ForesignalIntelGenerator.PAGE * 2 + 7; i++) {
            rows.add(event(String.format("01E%06d", i), false));
        }
        all(rows);
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW, ctx);
        assertThat(emitted).hasSize(rows.size());
        assertThat(tombstoned).isEmpty();
    }

    @Test
    @DisplayName("the published set comes from the source's own heads; an id whose event is gone is tombstoned")
    void reconcileFromHeads() {
        all(List.of(event("01A", false)));
        generator.reconcile(List.of(
                new HeadSnapshot(ForesignalIntelGenerator.ID, "01A", ForesignalIntelGenerator.KIND, 1, 1, NOW, false,
                        Map.of(), null),
                new HeadSnapshot(ForesignalIntelGenerator.ID, "01GONE", ForesignalIntelGenerator.KIND, 2, 1, NOW,
                        false, Map.of(), null)), ctx);
        generator.onTick(NOW, ctx);
        assertThat(tombstoned).containsExactly("01GONE");
        assertThat(generator.published("01A")).isTrue();
    }

    @Test
    @DisplayName("an incremental pass reads only events updated since the previous pass, with an overlap")
    void incremental() {
        all(List.of(event("01A", false), event("01B", false)));
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW, ctx);
        emitted.clear();

        IntelligenceEventEntity changed = event("01B", false);
        changed.setSeverity("critical");
        Instant[] since = new Instant[1];
        when(events.findByUpdatedAtGreaterThanEqualOrderByIdAsc(any(), any(Pageable.class))).thenAnswer(i -> {
            since[0] = i.getArgument(0);
            return page(List.of(changed), i.getArgument(1));
        });
        generator.onTick(NOW.plusSeconds(30), ctx);
        assertThat(emitted).containsExactly("01B");
        assertThat(since[0]).isEqualTo(NOW.minus(Duration.ofMinutes(ForesignalIntelGenerator.OVERLAP_MINUTES)));
    }

    @Test
    @DisplayName("a dismissed or merged event that was published is tombstoned")
    void dismissedAndMerged() {
        IntelligenceEventEntity dismissed = event("01A", false);
        IntelligenceEventEntity merged = event("01B", false);
        all(List.of(dismissed, merged));
        generator.reconcile(List.of(), ctx);
        generator.onTick(NOW, ctx);
        dismissed.setStatus("dismissed");
        merged.setMergedInto("01C");
        when(events.findByUpdatedAtGreaterThanEqualOrderByIdAsc(any(), any(Pageable.class)))
                .thenAnswer(i -> page(List.of(dismissed, merged), i.getArgument(1)));
        generator.onTick(NOW.plusSeconds(30), ctx);
        assertThat(tombstoned).containsExactly("01A", "01B");
    }

    @Test
    @DisplayName("the descriptor keeps intel records for the 1825-day analytic retention class")
    void retention() {
        assertThat(ForesignalIntelGenerator.buildDescriptor().defaultRetention()).isEqualTo(Duration.ofDays(1825));
        assertThat(PageRequest.of(0, ForesignalIntelGenerator.PAGE).getPageSize()).isEqualTo(500);
    }
}
