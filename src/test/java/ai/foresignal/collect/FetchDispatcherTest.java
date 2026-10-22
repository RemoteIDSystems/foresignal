package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchDispatcherTest {

    private final SourceThreadFetchDispatcher dispatcher = new SourceThreadFetchDispatcher();
    private final SourceDescriptor descriptor = FakePollingAdapter.defaultDescriptor("dispatch-test").build();

    @AfterEach
    void close() {
        dispatcher.destroy();
    }

    @Test
    @DisplayName("returns the task's result, run on a platform thread of the source's own in the collect role")
    void result() throws Exception {
        String seen = dispatcher.call(
                descriptor,
                () -> Thread.currentThread().isVirtual() + " " + Thread.currentThread().getName() + " " + WorkRole.current(),
                Duration.ofSeconds(5));
        assertThat(seen).isEqualTo("false foresignal-fetch-dispatch-test COLLECT");
    }

    @Test
    @DisplayName("refuses new fetches once destroyed")
    void refusesAfterDestroy() {
        dispatcher.destroy();
        assertThatThrownBy(() -> dispatcher.dispatch(descriptor, () -> 1))
                .isInstanceOf(java.util.concurrent.RejectedExecutionException.class);
    }

    @Test
    @DisplayName("rethrows the task's own exception unwrapped")
    void unwraps() {
        assertThatThrownBy(() -> dispatcher.call(descriptor, () -> {
            throw new AdapterException("nope");
        }, Duration.ofSeconds(5))).isInstanceOf(AdapterException.class).hasMessage("nope");
        assertThatThrownBy(() -> dispatcher.call(descriptor, () -> {
            throw new StackOverflowError("deep");
        }, Duration.ofSeconds(5))).isInstanceOf(StackOverflowError.class);
    }

    @Test
    @DisplayName("interrupts the task and throws AdapterTimeoutException on timeout")
    void timeout() {
        AtomicBoolean interrupted = new AtomicBoolean();
        assertThatThrownBy(() -> dispatcher.call(descriptor, () -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
            return null;
        }, Duration.ofMillis(200)))
                .isInstanceOf(AdapterTimeoutException.class)
                .satisfies(e -> assertThat(((AdapterTimeoutException) e).getTimeout()).isEqualTo(Duration.ofMillis(200)));
        await().atMost(Duration.ofSeconds(5)).untilTrue(interrupted);
    }
}
