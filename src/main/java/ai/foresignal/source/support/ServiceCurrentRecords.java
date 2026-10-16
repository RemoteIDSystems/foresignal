package ai.foresignal.source.support;

import ai.foresignal.collect.HealthService;
import ai.foresignal.derive.DerivedRecords;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.CurrentRecords;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@link CurrentRecords} over the derived-record reader and the health service (prompt 107). Reads
 * stored {@code record_head ⋈ record_live} rows and the source-health table only.
 */
@Component
public class ServiceCurrentRecords implements CurrentRecords {

    private final DerivedRecords records;
    private final ObjectProvider<HealthService> health;

    /** Creates the service. */
    public ServiceCurrentRecords(DerivedRecords records, ObjectProvider<HealthService> health) {
        this.records = records;
        this.health = health;
    }

    @Override
    public List<Row> current(List<String> sources, List<String> kinds, int limit) {
        List<Row> out = new ArrayList<>();
        for (HeadSnapshot head : records.current(sources, kinds, limit)) {
            if (head.tombstone()) {
                continue;
            }
            out.add(new Row(head.sourceId(), head.externalId(), head.kind(), head.observedAt(), head.payload()));
        }
        return out;
    }

    @Override
    public String health(String sourceId) {
        HealthService service = health == null ? null : health.getIfAvailable();
        if (service == null) {
            return "IDLE";
        }
        try {
            return service.health(sourceId).status().name();
        } catch (RuntimeException e) {
            return "IDLE";
        }
    }
}
