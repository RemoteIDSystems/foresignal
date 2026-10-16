package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The sanctions delisting step (prompt 091, FR-15): a removal is a tombstone; a 20 % shrink writes no tombstone,
 * keeps the previous id set, degrades the source and raises a system event; the operator's {@code allowShrink}
 * accepts it; the stored id set is compressed and an earlier plain-text value is still read.
 */
class ListReconcilerTest {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    @Test
    @DisplayName("an id that disappeared is tombstoned and the new set becomes the baseline")
    void removal() {
        Context ctx = new Context(false);
        ListReconciler reconciler = new ListReconciler(null);
        reconciler.reconcile(ctx, "ids", "UN", records(20));

        ListReconciler.Outcome outcome = reconciler.reconcile(ctx, "ids", "UN", records(19));

        assertThat(outcome.tripped()).isFalse();
        assertThat(outcome.records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).containsExactly("un:19");
        assertThat(ListReconciler.stored(ctx.state, "ids")).hasSize(19);
    }

    @Test
    @DisplayName("the real 20%-shorter SDN fixture writes no tombstones, keeps the baseline, degrades and notifies")
    void shrinkGuard() throws Exception {
        OfacSdnAdapter parser = new OfacSdnAdapter(null);
        List<NormalizedRecord> full = parser.parse(fixture("sdn.xml"), OfacSdnAdapter.SDN);
        List<NormalizedRecord> shorter = parser.parse(fixture("sdn-short.xml"), OfacSdnAdapter.SDN);
        assertThat(shorter.size()).isLessThan((int) (full.size() * DelistingGuard.MIN_RETAINED_RATIO));
        Context ctx = new Context(false);
        List<Object> events = new ArrayList<>();
        ListReconciler reconciler = new ListReconciler(events::add);
        reconciler.reconcile(ctx, "ofac:ids:SDN", "SDN", full);

        ListReconciler.Outcome outcome = reconciler.reconcile(ctx, "ofac:ids:SDN", "SDN", shorter);
        ListReconciler.applyHealth(ctx.state, List.of(outcome));

        assertThat(outcome.tripped()).isTrue();
        assertThat(outcome.records()).noneMatch(NormalizedRecord::tombstone).hasSize(shorter.size());
        assertThat(ListReconciler.stored(ctx.state, "ofac:ids:SDN")).as("the previous baseline is kept")
                .hasSize(full.size());
        assertThat(ctx.state.get(AdapterStateStore.DEGRADED_REASON_KEY).orElseThrow())
                .startsWith(DelistingGuard.SHRINK_REASON);
        assertThat(events).singleElement().isInstanceOfSatisfying(SystemEvent.class, event -> {
            assertThat(event.subtype()).isEqualTo(ListReconciler.SHRINK_EVENT);
            assertThat(event.data()).containsEntry("previousCount", full.size())
                    .containsEntry("currentCount", shorter.size());
        });

        // the next scheduled run still trips: running again does not silently accept the smaller list
        assertThat(reconciler.reconcile(ctx, "ofac:ids:SDN", "SDN", shorter).tripped()).isTrue();
    }

    @Test
    @DisplayName("allowShrink accepts a large delisting: tombstones, a new baseline, the degraded reason cleared")
    void allowShrink() {
        Context scheduled = new Context(false);
        ListReconciler reconciler = new ListReconciler(null);
        reconciler.reconcile(scheduled, "ids", "UK", records(100));
        ListReconciler.applyHealth(scheduled.state,
                List.of(reconciler.reconcile(scheduled, "ids", "UK", records(50))));
        assertThat(scheduled.state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isPresent();

        Context manual = new Context(true, scheduled.state);
        ListReconciler.Outcome outcome = reconciler.reconcile(manual, "ids", "UK", records(50));
        ListReconciler.applyHealth(manual.state, List.of(outcome));

        assertThat(outcome.tripped()).isFalse();
        assertThat(outcome.records()).filteredOn(NormalizedRecord::tombstone).hasSize(50);
        assertThat(ListReconciler.stored(manual.state, "ids")).hasSize(50);
        assertThat(manual.state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    @DisplayName("another component's degraded reason is not cleared by a clean run")
    void foreignReasonKept() {
        Context ctx = new Context(false);
        ctx.state.degrade("something else");
        ListReconciler reconciler = new ListReconciler(null);
        ListReconciler.applyHealth(ctx.state, List.of(reconciler.reconcile(ctx, "ids", "UN", records(3))));
        assertThat(ctx.state.get(AdapterStateStore.DEGRADED_REASON_KEY)).contains("something else");
    }

    @Test
    @DisplayName("the id set is stored compressed; a plain newline value of an earlier build still reads")
    void encoding() {
        String encoded = ListReconciler.encode(List.of("b", "a"));
        assertThat(encoded).startsWith(ListReconciler.GZIP_PREFIX).doesNotContain("\n");
        assertThat(ListReconciler.decode(encoded)).containsExactly("a", "b");
        assertThat(ListReconciler.decode("uk:1\nuk:2\n")).containsExactly("uk:1", "uk:2");
        assertThat(ListReconciler.decode(null)).isEmpty();
        assertThat(ListReconciler.decode(ListReconciler.GZIP_PREFIX + "not base64!")).isEmpty();
        assertThat(Set.copyOf(ListReconciler.decode(ListReconciler.encode(List.of())))).isEmpty();
    }

    private static Path fixture(String name) throws Exception {
        return Xml.temporary(AdapterFixtures.bytes("ofac-sdn", name));
    }

    private static List<NormalizedRecord> records(int n) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(NormalizedRecord.builder("un:" + i, Sanctions.KIND).observedAt(NOW).put("name", "N" + i).build());
        }
        return out;
    }

    /** An in-memory adapter state. */
    static final class MapState implements AdapterStateStore {
        final Map<String, String> values = new HashMap<>();

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

    /** A hand-built fetch context: state, clock and the {@code allowShrink} flag only. */
    static final class Context implements FetchContext {
        final MapState state;
        final boolean allowShrink;

        Context(boolean allowShrink) {
            this(allowShrink, new MapState());
        }

        Context(boolean allowShrink, MapState state) {
            this.allowShrink = allowShrink;
            this.state = state;
        }

        @Override
        public String sourceId() {
            return "test-sanctions";
        }

        @Override
        public SourceDescriptor descriptor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SourceHttp http() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ValidatedParams params() {
            return ValidatedParams.empty();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }

        @Override
        public AdapterStateStore state() {
            return state;
        }

        @Override
        public boolean allowShrink() {
            return allowShrink;
        }
    }
}
