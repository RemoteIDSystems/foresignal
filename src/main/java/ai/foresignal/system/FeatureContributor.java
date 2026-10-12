package ai.foresignal.system;

import java.util.Map;

/**
 * Contributes capability flags to {@code /v1/system.features} (prompt 019), so clients can adapt to
 * what this installation offers. Later prompts declare a bean each ({@code llm}, {@code search},
 * {@code push}, {@code mcp}, {@code weather} …); a key reported by several contributors is true when
 * any of them says so.
 */
public interface FeatureContributor {

    /** Feature name to whether it is available now. */
    Map<String, Boolean> features();
}
