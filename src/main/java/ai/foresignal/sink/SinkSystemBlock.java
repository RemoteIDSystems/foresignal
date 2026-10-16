package ai.foresignal.sink;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** The {@code sinks} block of {@code /v1/system} (prompt 110): how many are enabled and failing. */
@Component
public class SinkSystemBlock implements SystemBlockContributor {

    private final SinkRepository sinks;
    private final SinkSupervisor supervisor;

    /** Creates the block. */
    public SinkSystemBlock(SinkRepository sinks, SinkSupervisor supervisor) {
        this.sinks = sinks;
        this.supervisor = supervisor;
    }

    @Override
    public String key() {
        return "sinks";
    }

    @Override
    public Map<String, Object> block() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", sinks.findByEnabledTrueOrderByIdAsc().size());
        out.put("failing", supervisor.failing());
        out.put("running", supervisor.running());
        out.put("clients", supervisor.clients());
        return out;
    }
}
