package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.ask.AskPrompts;
import ai.foresignal.briefing.BriefingPrompts;
import ai.foresignal.mapping.MappingSuggestPrompts;
import ai.foresignal.trigger.EnrichmentPrompts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The versioned prompt templates (prompt 116): every LLM role's system prompt is a bundled
 * {@code llm/templates/{id}@{version}.md} file and the code sends exactly that text.
 */
class PromptTemplatesTest {

    @Test
    @DisplayName("each role's system prompt is its template's text")
    void constantsAreTemplates() {
        assertThat(AskPrompts.SYSTEM).isEqualTo(PromptTemplates.text(PromptTemplates.ASK, PromptTemplates.V1));
        assertThat(AskPrompts.RETRIEVAL_SYSTEM)
                .isEqualTo(PromptTemplates.text(PromptTemplates.ASK_RETRIEVAL, PromptTemplates.V1));
        assertThat(EnrichmentPrompts.SYSTEM).isEqualTo(PromptTemplates.text(PromptTemplates.ENRICHMENT, PromptTemplates.V1));
        assertThat(BriefingPrompts.SYSTEM).isEqualTo(PromptTemplates.text(PromptTemplates.BRIEFING, PromptTemplates.V1));
        assertThat(MappingSuggestPrompts.SYSTEM)
                .isEqualTo(PromptTemplates.text(PromptTemplates.MAPPING_SUGGEST, PromptTemplates.V1));
        assertThat(AskPrompts.SYSTEM).startsWith("You are Ask Foresignal").contains("Rules:");
    }

    @Test
    @DisplayName("every role has a bundled 1.0.0 template, a role and a stable hash")
    void everyRoleBundled() {
        for (String id : PromptTemplates.ROLES.keySet()) {
            assertThat(PromptTemplates.versions(id)).as(id).contains(PromptTemplates.V1);
            assertThat(PromptTemplates.latest(id)).isPresent();
            assertThat(PromptTemplates.sha256(id, PromptTemplates.V1)).hasSize(64);
        }
        assertThat(PromptTemplates.reference(PromptTemplates.ANALYST, PromptTemplates.V1))
                .containsEntry("templateId", "analyst-system").containsEntry("version", "1.0.0");
    }

    @Test
    @DisplayName("unknown templates and malformed ids are not found")
    void unknown() {
        assertThat(PromptTemplates.find("nope", "1.0.0")).isEmpty();
        assertThat(PromptTemplates.find("../secret", "1.0.0")).isEmpty();
        assertThat(PromptTemplates.versions("nope")).isEmpty();
        assertThatThrownBy(() -> PromptTemplates.text("nope", "1.0.0")).isInstanceOf(IllegalStateException.class);
    }
}
