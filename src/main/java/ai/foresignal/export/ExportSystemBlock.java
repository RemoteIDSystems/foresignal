package ai.foresignal.export;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code exports} block of {@code /v1/system} (prompt 111): jobs queued and running, and the
 * bytes the export and snapshot directories hold against {@code exports.diskQuotaGb}.
 */
@Component
public class ExportSystemBlock implements SystemBlockContributor {

    private final BulkExportJobRepository jobs;
    private final ExportStore store;
    private final ExportProperties properties;

    /** Creates the block. */
    public ExportSystemBlock(BulkExportJobRepository jobs, ExportStore store, ExportProperties properties) {
        this.jobs = jobs;
        this.store = store;
        this.properties = properties;
    }

    @Override
    public String key() {
        return "exports";
    }

    @Override
    public Map<String, Object> block() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("queued", jobs.countByStatus(ExportJobEntity.QUEUED));
        out.put("running", jobs.countByStatus(ExportJobEntity.RUNNING));
        out.put("bytes", ExportStore.bytesUnder(store.exportsRoot()) + ExportStore.bytesUnder(store.snapshotsRoot()));
        out.put("quotaBytes", properties.diskQuotaGb() * 1024L * 1024L * 1024L);
        out.put("maxConcurrent", properties.maxConcurrent());
        return out;
    }
}
