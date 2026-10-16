package ai.foresignal.export;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** The {@code exports} block of {@code /v1/system} (prompt 111): queued, running and bytes. */
@Component
public class ExportSystemBlock implements SystemBlockContributor {

    private final ExportJobRepository jobs;

    /** Creates the block. */
    public ExportSystemBlock(ExportJobRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    public String key() {
        return "exports";
    }

    @Override
    public Map<String, Object> block() {
        List<ExportJobEntity> all = jobs.findAll();
        long queued = all.stream().filter(j -> ExportJobEntity.QUEUED.equals(j.getStatus())).count();
        long running = all.stream().filter(j -> ExportJobEntity.RUNNING.equals(j.getStatus())).count();
        long bytes = all.stream().filter(j -> j.getBytes() != null).mapToLong(ExportJobEntity::getBytes).sum();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("queued", queued);
        out.put("running", running);
        out.put("bytes", bytes);
        return out;
    }
}
