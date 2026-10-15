package ai.foresignal.derive;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * The DERIVED layers' metrics (prompt 099):
 * {@code foresignal.derive.eval.duration{generator}} per invocation and
 * {@code foresignal.derive.state.size{generator}} per generator state.
 */
@Component
public class DerivedMetrics {

    /** The per-invocation evaluation timer. */
    public static final String EVAL_METRIC = "foresignal.derive.eval.duration";

    /** The per-generator state-size gauge. */
    public static final String STATE_METRIC = "foresignal.derive.state.size";

    private final MeterRegistry meters;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> states = new ConcurrentHashMap<>();

    /** Creates the metrics. */
    public DerivedMetrics(MeterRegistry meters) {
        this.meters = meters;
    }

    /** Records one invocation's wall time. */
    public void eval(String generator, Duration duration) {
        if (generator == null || duration == null) {
            return;
        }
        timers.computeIfAbsent(generator,
                id -> Timer.builder(EVAL_METRIC).tag("generator", id).register(meters)).record(duration);
    }

    /** Publishes a generator's state size. */
    public void stateSize(String generator, int size) {
        if (generator == null) {
            return;
        }
        state(generator).set(Math.max(0, size));
    }

    private AtomicInteger state(String generator) {
        return states.computeIfAbsent(generator, id -> {
            AtomicInteger value = new AtomicInteger();
            Gauge.builder(STATE_METRIC, value, AtomicInteger::get).tag("generator", id).register(meters);
            return value;
        });
    }
}
