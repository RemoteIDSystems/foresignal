package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.system.OperationalAlertService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The size-cap ratchet of {@link RetentionDbCap} (prompt R2-015) against a scripted size probe: step down only while
 * over and growing, stop when a step does not shrink, hold, relax, floor, disabled, and the tightened settings.
 */
class RetentionDbCapTest {

    static final long GB = 1024L * 1024L * 1024L;

    final Map<String, Object> store = new HashMap<>();
    final Deque<Long> sizes = new ArrayDeque<>();
    final List<Integer> pruned = new ArrayList<>();
    final RetentionResolver resolver = mock(RetentionResolver.class);
    RetentionDbCap cap;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.RETENTION_DB_CAP), eq(RetentionDbCap.State.class), any()))
                .thenAnswer(i -> store.containsKey(SettingKeys.RETENTION_DB_CAP)
                        ? store.get(SettingKeys.RETENTION_DB_CAP)
                        : ((Supplier<RetentionDbCap.State>) i.getArgument(2)).get());
        doAnswer(i -> store.put(i.getArgument(0), i.getArgument(1))).when(settings).put(anyString(), any());
        doAnswer(i -> store.remove(i.getArgument(0))).when(settings).remove(anyString());
        when(resolver.largestWindow()).thenReturn(Duration.ofDays(100));
        ObjectProvider<OperationalAlertService> alerts = mock(ObjectProvider.class);
        cap = new RetentionDbCap(settings, resolver, alerts, () -> sizes.isEmpty() ? 0L : sizes.size() == 1
                ? sizes.peek() : sizes.poll(), Clock.fixed(Instant.parse("2026-10-26T00:00:00Z"), ZoneOffset.UTC));
    }

    /** Passes the scripted pruner never runs. */
    static final RetentionCapPruner.Passes NO_PASSES = new RetentionCapPruner.Passes() {
        @Override
        public RetentionRun plan(RetentionSettings settings) {
            return null;
        }

        @Override
        public RetentionRun execute(RetentionRun run) {
            return run;
        }
    };

    static RetentionSettings settings(Integer maxDays, Double maxDbGb, Boolean overwriteOldest) {
        return RetentionPreviewIT.capped(maxDays, maxDbGb, overwriteOldest);
    }

    private RetentionDbCap.Outcome enforce(RetentionSettings configured) {
        return cap.enforce(configured, (tightened, steps) -> {
            pruned.add(tightened.maxDays());
            return null;
        }, NO_PASSES);
    }

    private void sizes(long... values) {
        sizes.clear();
        for (long v : values) {
            sizes.add(v);
        }
    }

    @Test
    @DisplayName("off: no outcome, and tighten clears any working state")
    void off() {
        store.put(SettingKeys.RETENTION_DB_CAP, new RetentionDbCap.State(10, 5L, null));
        RetentionSettings plain = RetentionSettings.defaults();
        assertThat(enforce(plain)).isNull();
        assertThat(cap.tighten(plain)).isSameAs(plain);
        assertThat(store).doesNotContainKey(SettingKeys.RETENTION_DB_CAP);
    }

    @Test
    @DisplayName("under the cap with no state: nothing is pruned")
    void under() {
        sizes(GB);
        RetentionDbCap.Outcome o = enforce(settings(null, 2.0, true));
        assertThat(o.action()).isEqualTo("under");
        assertThat(o.steps()).isZero();
        assertThat(o.windowDays()).isNull();
        assertThat(pruned).isEmpty();
    }

    @Test
    @DisplayName("over and growing: halves from the largest window, stops when a step does not shrink")
    void stepsUntilNoShrink() {
        sizes(3 * GB, (long) (2.5 * GB), (long) (2.5 * GB));
        RetentionDbCap.Outcome o = enforce(settings(null, 2.0, true));
        assertThat(pruned).containsExactly(50, 25);
        assertThat(o.action()).isEqualTo("stepped");
        assertThat(o.windowDays()).isEqualTo(25);
        assertThat(o.bytesReclaimed()).isEqualTo((long) (0.5 * GB));
        assertThat(cap.state().windowDays()).isEqualTo(25);
        assertThat(cap.tighten(settings(null, 2.0, true)).maxDays()).isEqualTo(25);
    }

    @Test
    @DisplayName("over and growing: stops at the cap")
    void stopsAtTheCap() {
        sizes(3 * GB, GB);
        RetentionDbCap.Outcome o = enforce(settings(40, 2.0, true));
        assertThat(pruned).containsExactly(20);
        assertThat(o.met()).isTrue();
        assertThat(o.windowDays()).isEqualTo(20);
    }

    @Test
    @DisplayName("over but not growing since the last run: holds the window")
    void holds() {
        store.put(SettingKeys.RETENTION_DB_CAP, new RetentionDbCap.State(25, 3 * GB, null));
        sizes(3 * GB);
        RetentionDbCap.Outcome o = enforce(settings(null, 2.0, true));
        assertThat(o.action()).isEqualTo("holding");
        assertThat(o.windowDays()).isEqualTo(25);
        assertThat(pruned).isEmpty();
    }

    @Test
    @DisplayName("well under the cap: the window doubles back, and clears once it reaches the configured one")
    void relaxes() {
        store.put(SettingKeys.RETENTION_DB_CAP, new RetentionDbCap.State(25, 3 * GB, null));
        sizes(GB);
        assertThat(enforce(settings(null, 2.0, true)).windowDays()).isEqualTo(50);
        sizes(GB);
        assertThat(enforce(settings(null, 2.0, true)).windowDays()).isNull();
        assertThat(cap.tighten(settings(null, 2.0, true)).maxDays()).isNull();
    }

    @Test
    @DisplayName("at the floor and still over: floor, nothing pruned")
    void floor() {
        sizes(3 * GB);
        RetentionDbCap.Outcome o = enforce(settings(1, 2.0, true));
        assertThat(o.action()).isEqualTo("floor");
        assertThat(o.steps()).isZero();
        assertThat(pruned).isEmpty();
    }

    @Test
    @DisplayName("overwriteOldest false: disabled, nothing pruned")
    void disabled() {
        sizes(3 * GB);
        RetentionDbCap.Outcome o = enforce(settings(null, 2.0, false));
        assertThat(o.action()).isEqualTo("disabled");
        assertThat(pruned).isEmpty();
    }

    @Test
    @DisplayName("the base window is maxDays when set, else the largest window rounded up to whole days")
    void baseDays() {
        assertThat(cap.baseDays(settings(30, 2.0, true))).isEqualTo(30);
        when(resolver.largestWindow()).thenReturn(Duration.ofHours(25));
        assertThat(cap.baseDays(settings(null, 2.0, true))).isEqualTo(2);
    }
}
