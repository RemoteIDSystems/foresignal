package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.FakePollingAdapter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchDispatcherTest {

    private final VirtualThreadFetchDispatcher dispatcher = new VirtualThreadFetchDispatcher();
    private final SourceDescriptor descriptor = FakePollingAdapter.defaultDescriptor("dispatch-test").build();

    @AfterEach
    void close() {
        dispatcher.destroy();
    }

    @Test
    @DisplayName("returns the task's result, run on a virtual thread")
    void result() throws Exception {
        assertThat(dispatcher.call(descriptor, () -> Thread.currentThread().isVirtual(), Duration.ofSeconds(5)))
                .isTrue();
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
