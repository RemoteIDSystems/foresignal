package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.infra.InfraBuildEntity;
import ai.foresignal.infra.InfraGraphBuilder;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.infra.InfraRebuildScheduler;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.source.spi.Geo;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The infrastructure controller's request handling that needs no database (prompt 104). */
class InfraControllerTest {

    private static final Instant T0 = Instant.parse("2026-10-16T00:00:00Z");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("rebuild while a build is running answers 409 INFRA_REBUILD_RUNNING, as the spec declares")
    void rebuildWhileRunning() {
        InfraRebuildScheduler rebuilds = mock(InfraRebuildScheduler.class);
        when(rebuilds.startRebuild()).thenReturn(new InfraGraphBuilder.Started(new InfraBuildEntity(T0, 1), false));
        InfraController controller = new InfraController(null, null, rebuilds, null, null, null, null);
        assertThatThrownBy(controller::rebuildInfraGraph).isInstanceOf(ConflictException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.INFRA_REBUILD_RUNNING);
    }

    @Test
    @DisplayName("rebuild answers the new build when it started one")
    void rebuildStarts() {
        InfraRebuildScheduler rebuilds = mock(InfraRebuildScheduler.class);
        when(rebuilds.startRebuild()).thenReturn(new InfraGraphBuilder.Started(new InfraBuildEntity(T0, 1), true));
        Map<String, Object> build = new InfraController(null, null, rebuilds, null, null, null, null).rebuildInfraGraph();
        assertThat(build).containsEntry("startedAt", T0).containsEntry("rulesVersion", "1");
    }

    @Test
    @DisplayName("an eventId seed goes through the shared intel visibility: a hidden event is 404 UNKNOWN_EVENT")
    void hiddenEventIsUnknown() {
        InfraImpactService impact = mock(InfraImpactService.class);
        IntelEventService events = mock(IntelEventService.class);
        when(events.visible("E1", false)).thenThrow(new NotFoundException(ErrorCodes.UNKNOWN_EVENT, "No such event"));
        assertThatThrownBy(() -> InfraController.seed(impact, JSON.readTree("{\"eventId\":\"E1\"}"), events, false))
                .isInstanceOf(NotFoundException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.UNKNOWN_EVENT);
        verify(impact, never()).eventSeed(any());
    }

    @Test
    @DisplayName("a visible event seeds its area, with the caller's private visibility passed to the shared rule")
    void visibleEventSeeds() {
        InfraImpactService impact = mock(InfraImpactService.class);
        IntelEventService events = mock(IntelEventService.class);
        IntelligenceEventEntity event = new IntelligenceEventEntity("E2", "infrastructure-disruption", "Outage", T0);
        event.setGeom(Geo.fromGeoJson("{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}"));
        when(events.visible("E2", true)).thenReturn(event);
        InfraImpactService.Seed seed = new InfraImpactService.Seed(event.getGeom(), false, "event");
        when(impact.eventSeed(event)).thenReturn(seed);
        assertThat(InfraController.seed(impact, JSON.readTree("{\"eventId\":\"E2\"}"), events, true)).isSameAs(seed);
        verify(events).visible("E2", true);
    }
}
