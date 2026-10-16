package ai.foresignal.export;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code archive} block of {@code /v1/system} (prompt 111): whether the cold archive is on, its
 * target, the last run, its bytes and failures, and the totals since start.
 */
@Component
public class ArchiveSystemBlock implements SystemBlockContributor {

    private final ArchiveSettingsService settings;
    private final ArchiveStep.ArchiveState state;

    /** Creates the block. */
    public ArchiveSystemBlock(ArchiveSettingsService settings, ArchiveStep.ArchiveState state) {
        this.settings = settings;
        this.state = state;
    }

    @Override
    public String key() {
        return "archive";
    }

    @Override
    public Map<String, Object> block() {
        ArchiveSettings current = settings.get();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", current.enabled());
        out.put("target", current.target());
        out.put("lastRunAt", state.lastRunAt() == null ? null : state.lastRunAt().toString());
        out.put("lastRunBytes", state.lastRunBytes());
        out.put("lastRunFailures", state.lastRunFailures());
        out.put("lastFailureAt", state.lastFailureAt() == null ? null : state.lastFailureAt().toString());
        out.put("bytes", state.bytes());
        out.put("failures", state.failures());
        return out;
    }
}
