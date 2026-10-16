package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.SourceHttp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The staged adapter state of one executed fetch (F1/F2 of the 2026-10 review). */
class StagedAdapterStateStoreTest {

    /** An in-memory persistent store. */
    private static final class MemoryStore implements AdapterStateStore {
        final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public Optional<String> get(String key) {
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public void put(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }

    @Test
    void writesAreInvisibleToTheDelegateUntilCommitted() {
        MemoryStore persistent = new MemoryStore();
        persistent.put("cursor", "1");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.put("cursor", "2");
        staged.put("other", "x");

        assertThat(staged.get("cursor")).contains("2");
        assertThat(persistent.get("cursor")).contains("1");
        assertThat(persistent.get("other")).isEmpty();

        staged.commit();

        assertThat(persistent.values).containsEntry("cursor", "2").containsEntry("other", "x");
        assertThat(staged.isClosed()).isTrue();
    }

    @Test
    void discardKeepsThePersistentValues() {
        MemoryStore persistent = new MemoryStore();
        persistent.put("cursor", "1");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.put("cursor", "2");
        staged.remove("cursor");
        assertThat(staged.get("cursor")).isEmpty();
        staged.discard();

        assertThat(persistent.get("cursor")).contains("1");
        assertThat(staged.staged()).isEmpty();
    }

    @Test
    void removalsAreStagedAndCommitted() {
        MemoryStore persistent = new MemoryStore();
        persistent.put(SourceHttp.ETAG_STATE_PREFIX + "k", "\"v1\"");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        SourceHttp.forgetValidators(staged, "k");
        assertThat(staged.get(SourceHttp.ETAG_STATE_PREFIX + "k")).isEmpty();
        assertThat(persistent.get(SourceHttp.ETAG_STATE_PREFIX + "k")).contains("\"v1\"");

        staged.commit();
        assertThat(persistent.get(SourceHttp.ETAG_STATE_PREFIX + "k")).isEmpty();
    }

    @Test
    void putNullStagesARemoval() {
        MemoryStore persistent = new MemoryStore();
        persistent.put("cursor", "1");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.put("cursor", null);
        staged.commit();

        assertThat(persistent.get("cursor")).isEmpty();
    }

    @Test
    void healthKeysWriteThroughAtOnce() {
        MemoryStore persistent = new MemoryStore();
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.degrade("layer missing");
        assertThat(persistent.get(AdapterStateStore.DEGRADED_REASON_KEY)).contains("layer missing");

        staged.discard();
        assertThat(persistent.get(AdapterStateStore.DEGRADED_REASON_KEY)).contains("layer missing");

        StagedAdapterStateStore next = new StagedAdapterStateStore(persistent);
        next.clearDegraded();
        assertThat(persistent.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    void putDurablyWritesThroughAndReplacesAStagedValue() {
        MemoryStore persistent = new MemoryStore();
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.put("credits", "10");
        staged.putDurably("credits", "9");
        assertThat(persistent.get("credits")).contains("9");
        assertThat(staged.get("credits")).contains("9");

        staged.discard();
        assertThat(persistent.get("credits")).contains("9");
    }

    @Test
    void putDurablyNullRemovesAtOnce() {
        MemoryStore persistent = new MemoryStore();
        persistent.put("credits", "9");
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);

        staged.putDurably("credits", null);

        assertThat(persistent.get("credits")).isEmpty();
    }

    @Test
    void writesAfterCloseAreDropped() {
        MemoryStore persistent = new MemoryStore();
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);
        staged.discard();

        staged.put("cursor", "late");
        staged.remove("other");
        staged.commit();

        assertThat(persistent.values).isEmpty();
        assertThat(staged.staged()).isEmpty();
    }

    @Test
    void commitIsIdempotent() {
        MemoryStore persistent = new MemoryStore();
        StagedAdapterStateStore staged = new StagedAdapterStateStore(persistent);
        staged.put("cursor", "2");
        staged.commit();
        persistent.put("cursor", "3");

        staged.commit();

        assertThat(persistent.get("cursor")).contains("3");
    }

    @Test
    void defaultPutDurablyIsPut() {
        MemoryStore persistent = new MemoryStore();
        persistent.putDurably("k", "v");
        assertThat(persistent.get("k")).contains("v");
    }
}
