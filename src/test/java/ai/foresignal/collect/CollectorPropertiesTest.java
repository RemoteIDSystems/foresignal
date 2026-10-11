package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CollectorPropertiesTest {

    @Test
    @DisplayName("the defaults are the documented ones")
    void defaults() {
        CollectorProperties properties = CollectorProperties.defaults();
        assertThat(properties.scheduled()).isTrue();
        assertThat(properties.reconcileInterval()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.shutdownWait()).isEqualTo(Duration.ofSeconds(30));
        CollectorProperties.Stream stream = properties.stream();
        assertThat(stream.flushInterval()).isEqualTo(Duration.ofSeconds(2));
        assertThat(stream.flushMaxEntries()).isEqualTo(2000);
        assertThat(stream.allBufferMax()).isEqualTo(50_000);
        assertThat(stream.acceptBlockTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(stream.ladderResetAfter()).isEqualTo(Duration.ofMinutes(10));
        assertThat(stream.credentialsRetry()).isEqualTo(Duration.ofHours(1));
        assertThat(stream.reconnectLadder()).containsExactly(
                Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(60), Duration.ofSeconds(300),
                Duration.ofSeconds(900));
    }

    @Test
    @DisplayName("nulls fall back to the defaults")
    void nullsFilled() {
        CollectorProperties properties = new CollectorProperties(false, null, null, null, null);
        assertThat(properties.reconcileInterval()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.ingestGrace()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.stream()).isEqualTo(CollectorProperties.Stream.defaults());
    }

    @Test
    @DisplayName("the last ladder step repeats")
    void ladder() {
        CollectorProperties.Stream stream = new CollectorProperties.Stream(
                null, 10, 10, null, null, List.of(Duration.ofSeconds(1), Duration.ofSeconds(2)), null, null);
        assertThat(stream.reconnectDelay(-1)).isEqualTo(Duration.ofSeconds(1));
        assertThat(stream.reconnectDelay(0)).isEqualTo(Duration.ofSeconds(1));
        assertThat(stream.reconnectDelay(1)).isEqualTo(Duration.ofSeconds(2));
        assertThat(stream.reconnectDelay(7)).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("sizes must be positive")
    void validation() {
        assertThatThrownBy(() -> new CollectorProperties.Stream(null, 0, 10, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CollectorProperties.Stream(null, 10, 0, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
