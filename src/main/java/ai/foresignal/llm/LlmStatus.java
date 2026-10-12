package ai.foresignal.llm;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code llm} block of the full {@code /v1/system} document (OpenAPI {@code SystemInfo.llm}):
 * {@code configured}, the provider of each role and the tokens used today over every provider.
 */
@Component
public class LlmStatus implements SystemBlockContributor {

    private final LlmSettingsService settings;
    private final LlmUsageMeter usage;

    /** Creates the contributor. */
    public LlmStatus(LlmSettingsService settings, LlmUsageMeter usage) {
        this.settings = settings;
        this.usage = usage;
    }

    @Override
    public String key() {
        return "llm";
    }

    @Override
    public Map<String, Object> block() {
        LlmSettings current = settings.get();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("configured", current.chatProvider() != null || current.embeddingProvider() != null);
        out.put("chatProvider", current.chatProvider());
        out.put("embeddingProvider", current.embeddingProvider());
        out.put("tokensToday", usage.tokensToday(null));
        return out;
    }
}
