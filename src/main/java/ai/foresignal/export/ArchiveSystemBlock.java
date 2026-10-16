package ai.foresignal.export;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** The {@code archive} block of {@code /v1/system} (prompt 111): last run, bytes and failures. */
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
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", settings.get().enabled());
        out.put("lastRunAt", state.lastRunAt() == null ? null : state.lastRunAt().toString());
        out.put("bytes", state.bytes());
        out.put("failures", state.failures());
        return out;
    }
}
