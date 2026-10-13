package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link LiveIdSet}, the degraded-reason helpers of {@link AdapterStateStore} and {@link SourceHttp#forgetValidators} (prompt 047). */
class LiveIdSetTest {

    @Test
    @DisplayName("the first snapshot tombstones nothing; an id that disappears is reported, then retried once")
    void absentAndRetried() {
        var state = new WeatherAdapterHarness.MapState();

        assertThat(LiveIdSet.absentSince(state, "x", List.of("a", "b", "c"))).isEmpty();
        assertThat(LiveIdSet.read(state, "live:x")).containsExactly("a", "b", "c");

        assertThat(LiveIdSet.absentSince(state, "x", List.of("a", "c"))).containsExactly("b");
        // Reported once more in case the tombstone of the last run was lost.
        assertThat(LiveIdSet.absentSince(state, "x", List.of("a", "c"))).containsExactly("b");
        assertThat(LiveIdSet.absentSince(state, "x", List.of("a", "c"))).isEmpty();
    }

    @Test
    @DisplayName("an id that comes back is never reported; scopes are independent")
    void reappearAndScopes() {
        var state = new WeatherAdapterHarness.MapState();
        LiveIdSet.absentSince(state, "x", List.of("a", "b"));
        assertThat(LiveIdSet.absentSince(state, "x", List.of("a"))).containsExactly("b");
        assertThat(LiveIdSet.absentSince(state, "x", List.of("a", "b"))).isEmpty();

        assertThat(LiveIdSet.absentSince(state, "y", List.of())).isEmpty();
        assertThat(LiveIdSet.absentSince(state, null, List.of("q"))).isEmpty();
        assertThat(state.asMap()).containsKeys("live:x", "live:default");
    }

    @Test
    @DisplayName("an empty snapshot tombstones everything that was live and clears the key")
    void emptySnapshot() {
        var state = new WeatherAdapterHarness.MapState();
        LiveIdSet.absentSince(state, "x", List.of("a", "b"));
        assertThat(LiveIdSet.absentSince(state, "x", List.of())).containsExactly("a", "b");
        assertThat(state.get("live:x")).isEmpty();
    }

    @Test
    @DisplayName("degrade stores a reason, clearDegraded removes it")
    void degraded() {
        var state = new WeatherAdapterHarness.MapState();
        state.degrade("SPC layer x missing");
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).contains("SPC layer x missing");
        state.degrade(" ");
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
        state.degrade("again");
        state.clearDegraded();
        assertThat(state.asMap()).isEmpty();
    }

    @Test
    @DisplayName("forgetValidators removes the stored ETag and Last-Modified")
    void forgetValidators() {
        var state = new WeatherAdapterHarness.MapState();
        state.put("etag:k", "\"1\"");
        state.put("lastmod:k", "Mon");
        state.put("etag:other", "\"2\"");
        SourceHttp.forgetValidators(state, "k");
        assertThat(state.asMap()).containsOnlyKeys("etag:other");
    }

    @Test
    @DisplayName("a plain AdapterException carries no HTTP status")
    void noStatus() {
        assertThat(new AdapterException("x").httpStatus()).isEqualTo(OptionalInt.empty());
    }

    @Test
    @DisplayName("AreaOfInterest box tests include the edges")
    void aoiHelpers() {
        double[] box = {-100, 30, -90, 40};
        assertThat(AreaOfInterest.contains(box, -100, 30)).isTrue();
        assertThat(AreaOfInterest.contains(box, -89.9, 35)).isFalse();
        assertThat(AreaOfInterest.intersects(box, -90, 40, -80, 50)).isTrue();
        assertThat(AreaOfInterest.intersects(box, -89, 41, -80, 50)).isFalse();
        assertThat(CountyGeometries.none().available()).isFalse();
        assertThat(CountyGeometries.none().union(List.of("48453"))).isEmpty();
    }
}
