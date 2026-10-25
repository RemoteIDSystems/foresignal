package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.store.query.RecordQueryService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link IntelEventService#rescore} reads each source's health once, not once per member: a large event's thousands
 * of members come from a handful of sources, and each health view runs the health rules, one of which reads the
 * database (the correlation loop's CPU after the 2026-10-25 restart).
 */
class RescoreHealthLookupTest {

    @Test
    @DisplayName("rescoring 300 members from 3 sources reads health 3 times, and scores as before")
    void healthOncePerSource() {
        IntelEventMemberRepository members = mock(IntelEventMemberRepository.class);
        HealthService health = mock(HealthService.class);
        SourceHealthView healthy = mock(SourceHealthView.class);
        when(healthy.status()).thenReturn(HealthStatus.HEALTHY);
        when(health.health(anyString())).thenReturn(healthy);
        List<IntelEventMemberEntity> all = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            IntelEventMemberEntity member = new IntelEventMemberEntity("e1", "record", "source-" + (i % 3), "x" + i, i);
            member.setSignalFamily("family-" + (i % 3));
            member.setWeight(0.6);
            all.add(member);
        }
        when(members.readByEventIdOrderByIdAsc("e1")).thenReturn(all);
        IntelEventService service = new IntelEventService(mock(IntelligenceEventRepository.class), members,
                mock(IntelEventLogRepository.class), mock(RecordQueryService.class), health, mock(IntelEventBus.class),
                mock(RecipeRegistry.class), mock(IntelVisibility.class), JsonMapper.builder().build(),
                Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC));
        IntelligenceEventEntity event = new IntelligenceEventEntity("e1", "custom", "Test", Instant.EPOCH);

        service.rescore(event);

        verify(health, times(1)).health("source-0");
        verify(health, times(1)).health("source-1");
        verify(health, times(1)).health("source-2");
        assertThat(event.getMemberCount()).isEqualTo(300);
        assertThat(event.getConfidence()).isNotNull();
    }
}
