package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.PartitionManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** The {@code ingestWriter} and {@code partitions} readiness members (prompt 019). */
class HealthIndicatorsTest {

    @ParameterizedTest
    @EnumSource(WriterState.class)
    @DisplayName("ingestWriter is UP only while RUNNING (state loaded, lock held, clock guard clear)")
    void writer(WriterState state) {
        IngestWriter writer = mock(IngestWriter.class);
        when(writer.status()).thenReturn(new WriterStatus(state, List.of(), 0, null, 0));
        String code = new IngestWriterHealthIndicator(writer).health().getStatus().getCode();
        assertThat(code).isEqualTo(state == WriterState.RUNNING ? "UP" : "DOWN");
    }

    @Test
    @DisplayName("partitions: UP with a block of headroom per tier, DOWN below it, UNKNOWN before the first read")
    void partitions() {
        PartitionManager manager = mock(PartitionManager.class);
        PartitionsHealthIndicator indicator = new PartitionsHealthIndicator(manager);
        for (HistoryTier tier : HistoryTier.values()) {
            when(manager.block(tier)).thenReturn(100L);
            when(manager.headroom(tier)).thenReturn(150.0);
        }
        assertThat(indicator.healthy()).isTrue();

        when(manager.headroom(HistoryTier.STANDARD)).thenReturn(99.0);
        assertThat(indicator.health().getStatus().getCode()).isEqualTo("DOWN");

        when(manager.headroom(HistoryTier.STANDARD)).thenReturn(Double.NaN);
        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UNKNOWN");
    }
}
