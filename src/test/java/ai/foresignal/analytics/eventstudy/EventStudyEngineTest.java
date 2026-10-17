package ai.foresignal.analytics.eventstudy;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.series.SeriesFrequency;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The event-study engine (prompt 119): a planted pre-event abnormality is found with a low placebo p, a later point
 * changes nothing in a live pre-event check (no look-ahead), weekly windows apply, overlapping events are clustered,
 * and short estimation windows are excluded with FEW_EVENTS.
 */
class EventStudyEngineTest {

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");

    private static Instant[] starts(SeriesFrequency f, int n) {
        Instant[] out = new Instant[n];
        Instant t = f.floor(T0);
        for (int i = 0; i < n; i++) {
            out[i] = t;
            t = f.next(t);
        }
        return out;
    }

    /** A random-walk price (log returns N(0, 0.01)). */
    private static double[] prices(long seed, int n) {
        Random random = new Random(seed);
        double[] v = new double[n];
        v[0] = 70;
        for (int i = 1; i < n; i++) {
            v[i] = v[i - 1] * Math.exp(0.01 * random.nextGaussian());
        }
        return v;
    }

    @Test
    @DisplayName("a planted pre-event run-up across events is found; the placebo p and standardized p are low")
    void plantedPreEvent() {
        int n = 1200;
        double[] v = prices(1, n);
        Instant[] s = starts(SeriesFrequency.DAY, n);
        List<EventStudyEngine.Event> events = new ArrayList<>();
        for (int e = 300; e < n - 10; e += 100) {
            for (int off = -5; off <= -1; off++) {
                double bump = Math.exp(0.02);
                for (int j = e + off; j < n; j++) {
                    v[j] *= bump;
                }
            }
            events.add(new EventStudyEngine.Event(s[e].plusSeconds(3600), "intel-event:e" + e));
        }
        EventStudyEngine.Result r = EventStudyEngine.run(new EventStudyEngine.Input(s, v, SeriesFrequency.DAY,
                EventStudyEngine.Transform.LOG_RETURN, EventStudyEngine.Windows.DAILY, events, List.of(), 42, 500,
                true));
        assertThat(r.used()).isEqualTo(events.size());
        assertThat(r.clusters()).isEqualTo(events.size());
        assertThat(r.pre().caar()).isGreaterThan(5);
        assertThat(r.pre().p()).isLessThan(0.001);
        assertThat(r.pre().placeboP()).isLessThan(0.01);
        assertThat(r.pre().corradoP()).isLessThan(0.01);
        assertThat(r.event().p()).as("nothing was planted in the event window").isGreaterThan(0.001);
        assertThat(r.placeboDates()).isPositive();
        EventStudyEngine.Result again = EventStudyEngine.run(new EventStudyEngine.Input(s, v.clone(),
                SeriesFrequency.DAY, EventStudyEngine.Transform.LOG_RETURN, EventStudyEngine.Windows.DAILY, events,
                List.of(), 42, 500, true));
        assertThat(again.pre().placeboP()).as("deterministic for one seed").isEqualTo(r.pre().placeboP());
    }

    @Test
    @DisplayName("no look-ahead: a value after the pre-event window changes nothing in a live pre-event check")
    void noLookAhead() {
        int n = 400;
        double[] v = prices(2, n);
        Instant[] s = starts(SeriesFrequency.DAY, n);
        int event = 380;
        List<EventStudyEngine.Event> events = List.of(new EventStudyEngine.Event(s[event], "situation:s1"));
        EventStudyEngine.Result before = EventStudyEngine.run(new EventStudyEngine.Input(s, v, SeriesFrequency.DAY,
                EventStudyEngine.Transform.LOG_RETURN, EventStudyEngine.Windows.DAILY, events, List.of(), 7, 200,
                false));
        double[] later = v.clone();
        for (int i = event; i < n; i++) {
            later[i] = later[i] * 5;
        }
        EventStudyEngine.Result after = EventStudyEngine.run(new EventStudyEngine.Input(s, later, SeriesFrequency.DAY,
                EventStudyEngine.Transform.LOG_RETURN, EventStudyEngine.Windows.DAILY, events, List.of(), 7, 200,
                false));
        assertThat(after.events().get(0).carPre()).isEqualTo(before.events().get(0).carPre());
        assertThat(after.pre().caar()).isEqualTo(before.pre().caar());
    }

    @Test
    @DisplayName("weekly series use the [−52, −3], [−2, −1], [0, +1] windows")
    void weekly() {
        int n = 120;
        Random random = new Random(3);
        double[] v = new double[n];
        for (int i = 0; i < n; i++) {
            v[i] = 1000 * i + 500 * random.nextGaussian();
        }
        Instant[] s = starts(SeriesFrequency.WEEK, n);
        v[98] += 20_000;
        v[99] += 20_000;
        EventStudyEngine.Result r = EventStudyEngine.run(new EventStudyEngine.Input(s, v, SeriesFrequency.WEEK,
                EventStudyEngine.Transform.DIFFERENCE, EventStudyEngine.Windows.of(SeriesFrequency.WEEK),
                List.of(new EventStudyEngine.Event(s[100].plus(Duration.ofDays(2)), null)), List.of(), 5, 500, false));
        EventStudyEngine.EventOutcome o = r.events().get(0);
        assertThat(o.excluded()).isNull();
        assertThat(o.index()).isEqualTo(100);
        assertThat(o.abnormal()).hasSize(4);
        assertThat(o.carPre()).isGreaterThan(10);
        assertThat(EventStudyEngine.Windows.WEEKLY.toMap()).containsEntry("estimation", List.of(-52, -3));
    }

    @Test
    @DisplayName("overlapping events form one cluster; short estimation windows are excluded with FEW_EVENTS")
    void clustering() {
        int n = 600;
        double[] v = prices(4, n);
        Instant[] s = starts(SeriesFrequency.DAY, n);
        List<EventStudyEngine.Event> events = List.of(new EventStudyEngine.Event(s[400], "a"),
                new EventStudyEngine.Event(s[403], "b"), new EventStudyEngine.Event(s[500], "c"),
                new EventStudyEngine.Event(s[30], "too-early"));
        EventStudyEngine.Result r = EventStudyEngine.run(new EventStudyEngine.Input(s, v, SeriesFrequency.DAY,
                EventStudyEngine.Transform.LOG_RETURN, EventStudyEngine.Windows.DAILY, events, List.of(), 1, 100,
                true));
        assertThat(r.clusters()).isEqualTo(2);
        assertThat(r.used()).isEqualTo(3);
        assertThat(r.excluded()).isEqualTo(1);
        EventStudyEngine.EventOutcome early = r.events().stream().filter(e -> "too-early".equals(e.ref()))
                .findFirst().orElseThrow();
        assertThat(early.excluded()).startsWith("FEW_EVENTS");
        assertThat(r.events().stream().filter(e -> e.excluded() == null).mapToInt(e -> e.clusterId()).distinct()
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("transforms follow the series family and hint")
    void transforms() {
        assertThat(EventStudyEngine.Transform.of(true, "log-return", "price"))
                .isEqualTo(EventStudyEngine.Transform.ANSCOMBE);
        assertThat(EventStudyEngine.Transform.of(false, null, "price")).isEqualTo(EventStudyEngine.Transform.LOG_RETURN);
        assertThat(EventStudyEngine.Transform.of(false, "log-level", "attention"))
                .isEqualTo(EventStudyEngine.Transform.LOG_LEVEL);
        assertThat(EventStudyEngine.Transform.of(false, "difference", "position"))
                .isEqualTo(EventStudyEngine.Transform.DIFFERENCE);
        assertThat(EventStudyEngine.transform(new double[] {100, 110}, EventStudyEngine.Transform.LOG_RETURN)[1])
                .isEqualTo(Math.log(1.1));
    }
}
