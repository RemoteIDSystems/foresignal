package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The ECMWF range planner (prompt 105): explicit lengths, coalescing, the 8 MB message cap. */
class EcmwfRunPlannerTest {

    private static EcmwfIndex.Entry entry(int position, String param, long offset, long length) {
        return new EcmwfIndex.Entry(position, param, "sfc", null, offset, length);
    }

    @Test
    @DisplayName("a message spans its own offset and length; no object size is needed")
    void spansAreFromLengths() {
        EcmwfRunPlanner.Plan plan = EcmwfRunPlanner.plan(List.of(entry(4, "tp", 1000, 500)));
        assertThat(plan.ranges()).hasSize(1);
        assertThat(plan.ranges().get(0).start()).isEqualTo(1000);
        assertThat(plan.ranges().get(0).end()).isEqualTo(1499);
        assertThat(plan.chargeBytes()).isEqualTo(500);
    }

    @Test
    @DisplayName("adjacent messages coalesce into one ranged GET")
    void coalescesAdjacent() {
        List<EcmwfIndex.Entry> wanted = List.of(entry(1, "10u", 100, 200), entry(2, "10v", 300, 200));
        EcmwfRunPlanner.Plan plan = EcmwfRunPlanner.plan(wanted);
        assertThat(plan.ranges()).hasSize(1);
        assertThat(plan.ranges().get(0).length()).isEqualTo(400);
        assertThat(plan.ranges().get(0).spans()).hasSize(2);
    }

    @Test
    @DisplayName("non-adjacent messages become separate GETs")
    void separatesNonAdjacent() {
        List<EcmwfIndex.Entry> wanted = List.of(entry(1, "10u", 100, 200), entry(5, "10v", 90_000, 200));
        assertThat(EcmwfRunPlanner.plan(wanted).requests()).isEqualTo(2);
    }

    @Test
    @DisplayName("a message over 8 MB is refused")
    void refusesTooLarge() {
        assertThatThrownBy(() -> EcmwfRunPlanner.plan(List.of(entry(1, "big", 0, 9L * 1024 * 1024))))
                .isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("over the");
    }

    @Test
    @DisplayName("duplicates are read once")
    void dedupes() {
        EcmwfIndex.Entry e = entry(1, "10u", 100, 200);
        assertThat(EcmwfRunPlanner.plan(List.of(e, e)).ranges()).hasSize(1);
    }
}
