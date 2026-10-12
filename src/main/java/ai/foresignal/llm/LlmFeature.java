package ai.foresignal.llm;

import ai.foresignal.system.FeatureContributor;
import java.util.Map;
import org.springframework.stereotype.Component;

/** {@code features.llm} of {@code /v1/system}: true while a chat provider is assigned. */
@Component
public class LlmFeature implements FeatureContributor {

    private final LlmSettingsService settings;

    /** Creates the contributor. */
    public LlmFeature(LlmSettingsService settings) {
        this.settings = settings;
    }

    @Override
    public Map<String, Boolean> features() {
        return Map.of("llm", settings.get().chatProvider() != null);
    }
}
