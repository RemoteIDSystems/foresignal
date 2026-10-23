package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.collect.CollectorStatus;
import ai.foresignal.collect.PullGovernor;
import ai.foresignal.settings.CollectorSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code getCollectorStatus} (prompt 160): the pull governor's live state, with no database. */
class CollectorSettingsControllerTest {

    @Test
    @DisplayName("the status reports the governor's cap, and a held permit shows in inUse until it is released")
    void statusReflectsTheGovernor() throws InterruptedException {
        PullGovernor governor = new PullGovernor(3);
        CollectorSettingsController controller =
                new CollectorSettingsController(mock(CollectorSettingsService.class), governor);

        assertThat(controller.getCollectorStatus()).isEqualTo(new CollectorStatus(3, 0, 0));

        try (PullGovernor.Permit permit = governor.acquire()) {
            assertThat(controller.getCollectorStatus()).isEqualTo(new CollectorStatus(3, 1, 0));
        }

        assertThat(controller.getCollectorStatus()).isEqualTo(new CollectorStatus(3, 0, 0));
    }

    @Test
    @DisplayName("a resize shows at once in the reported cap")
    void statusFollowsAResize() {
        PullGovernor governor = new PullGovernor(2);
        CollectorSettingsController controller =
                new CollectorSettingsController(mock(CollectorSettingsService.class), governor);

        governor.resize(5);

        assertThat(controller.getCollectorStatus().cap()).isEqualTo(5);
    }
}
