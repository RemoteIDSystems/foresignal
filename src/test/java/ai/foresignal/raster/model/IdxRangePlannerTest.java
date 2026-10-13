package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Byte ranges from {@code .idx} offsets (prompt 051). */
class IdxRangePlannerTest {

    static final GribIdx IDX = GribIdx.parse(String.join("\n",
            "1:0:d=2026091506:UGRD:10 m above ground:6 hour fcst:",
            "2:2:d=2026091506:VGRD:10 m above ground:6 hour fcst:",
            "3:4:d=2026091506:TMP:850 mb:6 hour fcst:",
            "4:6:d=2026091506:TMP:2 m above ground:6 hour fcst:",
            "5:8:d=2026091506:PRMSL:mean sea level:6 hour fcst:",
            "6:10:d=2026091506:HGT:surface:6 hour fcst:"));

    static GribIdx.Message m(String variable, String level) {
        return IDX.find(FieldSelector.of(variable, level)).orElseThrow();
    }

    @Test
    @DisplayName("a message spans its offset to the next message's offset − 1 (God's Eye View windMessageRanges)")
    void nextOffset() {
        IdxRangePlanner.Plan plan = IdxRangePlanner.plan(IDX, List.of(m("TMP", "2 m above ground")), null);
        assertThat(plan.ranges()).hasSize(1);
        assertThat(plan.ranges().get(0).start()).isEqualTo(6);
        assertThat(plan.ranges().get(0).end()).isEqualTo(7);
        assertThat(plan.ranges().get(0).spans().get(0).length()).isEqualTo(2);
    }

    @Test
    @DisplayName("the last message ends at the object size from the listing; without it the plan refuses")
    void lastMessage() {
        GribIdx.Message hgt = m("HGT", "surface");
        assertThat(IdxRangePlanner.needsObjectSize(IDX, List.of(hgt))).isTrue();
        assertThat(IdxRangePlanner.needsObjectSize(IDX, List.of(m("UGRD", "10 m above ground")))).isFalse();
        IdxRangePlanner.Plan plan = IdxRangePlanner.plan(IDX, List.of(hgt), 15L);
        assertThat(plan.ranges().get(0).start()).isEqualTo(10);
        assertThat(plan.ranges().get(0).end()).isEqualTo(14);
        assertThatThrownBy(() -> IdxRangePlanner.plan(IDX, List.of(hgt), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("object size");
        assertThatThrownBy(() -> IdxRangePlanner.plan(IDX, List.of(hgt), 10L))
                .isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("adjacent wanted messages are coalesced into one ranged GET; separate ones are not")
    void coalescing() {
        IdxRangePlanner.Plan plan = IdxRangePlanner.plan(IDX, List.of(m("VGRD", "10 m above ground"),
                m("UGRD", "10 m above ground"), m("TMP", "2 m above ground")), null);
        assertThat(plan.ranges()).hasSize(2);
        IdxRangePlanner.Range wind = plan.ranges().get(0);
        assertThat(wind.start()).isZero();
        assertThat(wind.end()).isEqualTo(3);
        assertThat(wind.spans()).extracting(s -> s.message().variable()).containsExactly("UGRD", "VGRD");
        assertThat(wind.spans().get(1).start()).isEqualTo(2);
        assertThat(plan.ranges().get(1).start()).isEqualTo(6);
        assertThat(plan.requests()).isEqualTo(2);
        IdxRangePlanner.Plan run = IdxRangePlanner.plan(IDX, List.of(m("TMP", "850 mb"), m("TMP", "2 m above ground"),
                m("PRMSL", "mean sea level")), null);
        assertThat(run.ranges()).hasSize(1);
        assertThat(run.ranges().get(0).length()).isEqualTo(6);
    }

    @Test
    @DisplayName("the advance mbPerDay charge is the sum of the planned range lengths")
    void advanceCharge() {
        IdxRangePlanner.Plan plan = IdxRangePlanner.plan(IDX, List.of(m("UGRD", "10 m above ground"),
                m("VGRD", "10 m above ground"), m("PRMSL", "mean sea level"), m("HGT", "surface")), 1_000L);
        // UGRD+VGRD bytes 0..3 (4), then PRMSL 8..9 (2) and HGT 10..999 (990) in one adjacent range.
        assertThat(plan.chargeBytes()).isEqualTo(4 + 2 + 990);
        assertThat(IdxRangePlanner.plan(IDX, List.of(), null).chargeBytes()).isZero();
    }

    @Test
    @DisplayName("duplicates are read once")
    void duplicates() {
        GribIdx.Message u = m("UGRD", "10 m above ground");
        assertThat(IdxRangePlanner.plan(IDX, List.of(u, u), null).ranges().get(0).spans()).hasSize(1);
    }

    @Test
    @DisplayName("a message over 8 MB, and non-increasing offsets, are refused")
    void limits() {
        GribIdx big = GribIdx.parse("1:0:d=x:A:b:c:\n2:9000000:d=x:B:b:c:\n");
        assertThatThrownBy(() -> IdxRangePlanner.plan(big, List.of(big.messages().get(0)), null))
                .isInstanceOf(RasterFormatException.class).hasMessageContaining("cap");
        GribIdx backwards = GribIdx.parse("1:50:d=x:A:b:c:\n2:10:d=x:B:b:c:\n");
        assertThatThrownBy(() -> IdxRangePlanner.plan(backwards, List.of(backwards.messages().get(0)), null))
                .isInstanceOf(RasterFormatException.class).hasMessageContaining("increase");
    }

    @Test
    @DisplayName("coalesced ranges stop at 16 MB")
    void rangeCap() {
        GribIdx idx = GribIdx.parse("1:0:d=x:A:l:t:\n2:7000000:d=x:B:l:t:\n3:14000000:d=x:C:l:t:\n"
                + "4:21000000:d=x:D:l:t:\n");
        IdxRangePlanner.Plan plan = IdxRangePlanner.plan(idx, idx.messages().subList(0, 3), null);
        assertThat(plan.ranges()).hasSize(2);
        assertThat(plan.ranges().get(0).spans()).hasSize(2);
        assertThat(plan.ranges().get(0).length()).isLessThanOrEqualTo(IdxRangePlanner.MAX_RANGE_BYTES);
    }
}
