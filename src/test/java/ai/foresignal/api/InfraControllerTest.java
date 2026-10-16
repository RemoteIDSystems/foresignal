package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.infra.InfraBuildEntity;
import ai.foresignal.infra.InfraGraphBuilder;
import ai.foresignal.infra.InfraRebuildScheduler;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The infrastructure controller's request handling that needs no database (prompt 104). */
class InfraControllerTest {

    private static final Instant T0 = Instant.parse("2026-10-16T00:00:00Z");

    @Test
    @DisplayName("rebuild while a build is running answers 409 INFRA_REBUILD_RUNNING, as the spec declares")
    void rebuildWhileRunning() {
        InfraRebuildScheduler rebuilds = mock(InfraRebuildScheduler.class);
        when(rebuilds.startRebuild()).thenReturn(new InfraGraphBuilder.Started(new InfraBuildEntity(T0, 1), false));
        InfraController controller = new InfraController(null, null, rebuilds, null, null);
        assertThatThrownBy(controller::rebuildInfraGraph).isInstanceOf(ConflictException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.INFRA_REBUILD_RUNNING);
    }

    @Test
    @DisplayName("rebuild answers the new build when it started one")
    void rebuildStarts() {
        InfraRebuildScheduler rebuilds = mock(InfraRebuildScheduler.class);
        when(rebuilds.startRebuild()).thenReturn(new InfraGraphBuilder.Started(new InfraBuildEntity(T0, 1), true));
        Map<String, Object> build = new InfraController(null, null, rebuilds, null, null).rebuildInfraGraph();
        assertThat(build).containsEntry("startedAt", T0).containsEntry("rulesVersion", "1");
    }
}
