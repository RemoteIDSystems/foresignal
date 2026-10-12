package ai.foresignal.system;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.health.contributor.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The readiness verdict outside the actuator (prompt 019), for the watchdog: the members of the
 * {@code readiness} health group evaluated directly. Ready means the application accepts traffic,
 * the database answers, the ingest writer is {@code RUNNING} and no tier is out of partition
 * headroom.
 */
@Component
public class ReadinessCheck {

    private final ApplicationAvailability availability;
    private final JdbcTemplate jdbc;
    private final IngestWriterHealthIndicator writer;
    private final PartitionsHealthIndicator partitions;

    /** Creates the check. */
    public ReadinessCheck(ApplicationAvailability availability, JdbcTemplate jdbc, IngestWriterHealthIndicator writer,
            PartitionsHealthIndicator partitions) {
        this.availability = availability;
        this.jdbc = jdbc;
        this.writer = writer;
        this.partitions = partitions;
    }

    /** True when every readiness member is up. */
    public boolean ready() {
        return availability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC
                && databaseUp()
                && Status.UP.equals(writer.health().getStatus())
                && !Status.DOWN.equals(partitions.health().getStatus());
    }

    /** True when {@code SELECT 1} succeeds. */
    public boolean databaseUp() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            return one != null && one == 1;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
