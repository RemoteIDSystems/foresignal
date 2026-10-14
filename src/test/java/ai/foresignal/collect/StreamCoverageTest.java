package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The flush coverage of a {@code coverageFromObservers} stream (prompt 083): the distinct observer receiver ids of the
 * batch as {@code receivers}, the batch size as {@code actual}.
 */
class StreamCoverageTest {

    private static IngestRecord record(String id, String... receivers) {
        List<Observer> observers = java.util.Arrays.stream(receivers).map(Observer::of).toList();
        return new IngestRecord(NormalizedRecord.builder(id, "mesh-node").observers(observers).build(),
                Instant.parse("2026-10-14T00:00:00Z"), 1);
    }

    @Test
    @DisplayName("distinct gateways in the flush window become Coverage.receivers")
    void distinctReceivers() {
        Coverage coverage = PolicyAwareRecordSink.coverage(List.of(record("!1", "!gw1"), record("!2", "!gw1"),
                record("!3", "!gw2"), record("!4")));
        assertThat(coverage.receivers()).isEqualTo(2);
        assertThat(coverage.actual()).isEqualTo(4);
        assertThat(coverage.expected()).isNull();
    }

    @Test
    @DisplayName("no observers: receivers is unset")
    void noObservers() {
        Coverage coverage = PolicyAwareRecordSink.coverage(List.of(record("!1")));
        assertThat(coverage.receivers()).isNull();
        assertThat(coverage.actual()).isEqualTo(1);
    }
}
