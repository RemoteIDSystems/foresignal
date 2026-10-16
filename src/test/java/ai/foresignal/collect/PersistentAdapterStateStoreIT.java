package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** {@link PersistentAdapterStateStore#apply}: a fetch's staged writes land in one atomic merge (F1). */
@IntegrationTest
class PersistentAdapterStateStoreIT {

    private static final String SOURCE = "state-apply-test";

    @Autowired UpstreamClient upstream;

    @Test
    @DisplayName("apply removes and sets keys and keeps the keys it does not name")
    void applyMerges() {
        PersistentAdapterStateStore state = upstream.stateStore(SOURCE);
        state.put("keep", "k");
        state.put("drop", "d");
        state.put("cursor", "1");

        state.apply(Map.of("cursor", "2", "fresh", "f"), Set.of("drop"));

        assertThat(state.all()).containsEntry("keep", "k").containsEntry("cursor", "2").containsEntry("fresh", "f")
                .doesNotContainKey("drop");
    }

    @Test
    @DisplayName("apply creates the row when the source has none, and an empty apply writes nothing")
    void applyCreates() {
        PersistentAdapterStateStore state = upstream.stateStore(SOURCE + "-new");
        state.apply(Map.of(), Set.of());
        assertThat(state.all()).isEmpty();

        state.apply(Map.of("cursor", "1"), Set.of("absent"));

        assertThat(state.get("cursor")).contains("1");
    }

    @Test
    @DisplayName("a staged store commits through apply")
    void stagedCommit() {
        PersistentAdapterStateStore state = upstream.stateStore(SOURCE + "-staged");
        state.put("old", "o");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(state);
        staged.put("cursor", "9");
        staged.remove("old");
        assertThat(state.get("cursor")).isEmpty();

        staged.commit();

        assertThat(state.all()).containsEntry("cursor", "9").doesNotContainKey("old");
    }
}
