package ai.foresignal.system;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The capabilities every installation of this version has (prompt 019): notifications (018),
 * retention, the watchdog and the ingest canary.
 */
@Component
public class CoreFeatureContributor implements FeatureContributor {

    @Override
    public Map<String, Boolean> features() {
        Map<String, Boolean> out = new LinkedHashMap<>();
        out.put("notifications", true);
        out.put("retention", true);
        out.put("watchdog", true);
        out.put("canary", true);
        return out;
    }
}
