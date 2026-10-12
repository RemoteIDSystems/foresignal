package ai.foresignal.system;

import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.ingest.WriterState;
import ai.foresignal.ingest.WriterStatus;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health contributor {@code ingestWriter}, part of the {@code readiness} group (prompt 019): UP only
 * while the writer is {@code RUNNING}, which means its state is loaded, it holds the single-writer
 * lock when {@code exclusive}, and the clock guard is not holding batches. {@code CLOCK_BEHIND},
 * {@code LOCK_LOST} and {@code STOPPED} are DOWN. Details are hidden by
 * {@code show-details: never}; {@code /v1/system.ingestWriter} carries them.
 */
@Component
public class IngestWriterHealthIndicator implements HealthIndicator {

    private final IngestWriter writer;

    /** Creates the indicator. */
    public IngestWriterHealthIndicator(IngestWriter writer) {
        this.writer = writer;
    }

    @Override
    public Health health() {
        WriterStatus status = writer.status();
        Health.Builder builder = status.state() == WriterState.RUNNING ? Health.up() : Health.down();
        return builder.withDetail("state", status.state().name()).build();
    }
}
