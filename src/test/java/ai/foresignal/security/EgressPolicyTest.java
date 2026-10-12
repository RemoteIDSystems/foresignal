package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.query.RecordRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EgressPolicy}'s LLM-context path (prompt 026): private sources stay out of remote prompts. */
class EgressPolicyTest {

    static final EgressPolicy.LlmDestination ANTHROPIC = new EgressPolicy.LlmDestination("claude", "anthropic", false);
    static final EgressPolicy.LlmDestination OPENAI = new EgressPolicy.LlmDestination("router", "openai-compatible", false);
    static final EgressPolicy.LlmDestination OLLAMA = new EgressPolicy.LlmDestination("ollama", "ollama", true);
    static final EgressPolicy.LlmDestination LOCAL_VLLM =
            new EgressPolicy.LlmDestination("vllm", "openai-compatible", true);

    EgressPolicy policy;

    @BeforeEach
    void setUp() {
        SourceRegistry registry = mock(SourceRegistry.class);
        SourceDescriptor open = SourceDescriptor.builder("quakes").name("Quakes").kind("earthquake").mode(Mode.PUSH)
                .build();
        SourceDescriptor family = SourceDescriptor.builder("family").name("Family").kind("position")
                .mode(Mode.PUSH).privateSource(true).build();
        when(registry.find("quakes")).thenReturn(Optional.of(new SourceRegistry.Registration(open, null, false)));
        when(registry.find("family")).thenReturn(Optional.of(new SourceRegistry.Registration(family, null, false)));
        when(registry.find("gone")).thenReturn(Optional.empty());
        policy = new EgressPolicy(registry);
    }

    static RecordRow row(String sourceId, String externalId) {
        return new RecordRow(sourceId, externalId, "k", 1, 1L, null, null, null, null, null, false, null, null, null,
                null, null, null, 0, null, null, null, null, null, null, null, null, null, null);
    }

    static final List<RecordRow> ROWS = List.of(row("quakes", "q1"), row("family", "phone"), row("quakes", "q2"),
            row("gone", "x"));

    @Test
    @DisplayName("private-source records are dropped for anthropic and openai-compatible providers")
    void remoteDropsPrivate() {
        assertThat(policy.filter(ROWS, ANTHROPIC)).extracting(RecordRow::externalId).containsExactly("q1", "q2");
        assertThat(policy.filter(ROWS, OPENAI)).extracting(RecordRow::externalId).containsExactly("q1", "q2");
    }

    @Test
    @DisplayName("private-source records are kept for ollama and for providers flagged local")
    void localKeepsPrivate() {
        assertThat(policy.filter(ROWS, OLLAMA)).extracting(RecordRow::externalId)
                .containsExactly("q1", "phone", "q2");
        assertThat(policy.filter(ROWS, LOCAL_VLLM)).extracting(RecordRow::externalId)
                .containsExactly("q1", "phone", "q2");
    }

    @Test
    @DisplayName("records of unregistered sources (or without a source) are dropped everywhere: fail closed")
    void unknownSourceDropped() {
        assertThat(policy.allowsLlmContext("gone", OLLAMA)).isFalse();
        assertThat(policy.allowsLlmContext(null, OLLAMA)).isFalse();
    }

    @Test
    @DisplayName("the generic filter works on any item type and keeps order")
    void generic() {
        List<Map<String, String>> items = List.of(Map.of("source", "family"), Map.of("source", "quakes"));

        assertThat(policy.filter(items, m -> m.get("source"), ANTHROPIC)).containsExactly(Map.of("source", "quakes"));
        assertThat(policy.filter(items, m -> m.get("source"), OLLAMA)).hasSize(2);
    }
}
