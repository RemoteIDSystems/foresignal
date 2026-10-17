package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.llm.LlmService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.testsupport.llm.FakeLlmClient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * The situation theme table (prompt 118 §3): bundled rows for event types, finding types, tags and kinds, operator
 * rows merged over them, keyword rules for untyped text, and the optional LLM classification, off by default and
 * cached by the text's hash when on.
 */
class ThemeTableTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    AppSettingService settings;
    ThemeClassificationRepository cache;
    LlmService llm;
    FakeLlmClient client;
    ThemeTable table;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        settings = mock(AppSettingService.class);
        when(settings.get(any(), any())).thenReturn(Optional.empty());
        cache = mock(ThemeClassificationRepository.class);
        when(cache.findById(any())).thenReturn(Optional.empty());
        llm = mock(LlmService.class);
        client = new FakeLlmClient("chat");
        when(llm.chatConfigured()).thenReturn(true);
        when(llm.chatClient()).thenReturn(Optional.of(client));
        ObjectProvider<LlmService> llmProvider = mock(ObjectProvider.class);
        when(llmProvider.getIfAvailable()).thenReturn(llm);
        ObjectProvider<EgressPolicy> egress = mock(ObjectProvider.class);
        table = new ThemeTable(JSON, settings, cache, llmProvider, egress,
                Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("bundled rows map event types, finding types, tags and kinds; unknown keys are null")
    void bundledRows() {
        assertThat(table.forEventType("maritime-disruption")).isEqualTo("maritime");
        assertThat(table.forEventType("aviation-anomaly")).isEqualTo("aviation");
        assertThat(table.forFindingType("pre-event-signal")).isEqualTo("markets");
        assertThat(table.forTags(List.of("unrelated", "Shipping"))).isEqualTo("maritime");
        assertThat(table.forKind("internet-outage")).isEqualTo("infrastructure");
        assertThat(table.forKind("no-such-kind")).isNull();
        assertThat(table.forEventType(null)).isNull();
        assertThat(table.version()).isEqualTo("1.0.0");
        for (String theme : List.of("maritime", "aviation", "conflict", "infrastructure", "weather", "cyber",
                "markets", "health", "space")) {
            assertThat(SituationThemes.valid(theme)).isTrue();
        }
    }

    @Test
    @DisplayName("operator rows from intel.themes win over the bundled ones, and their keyword rules come first")
    void operatorRows() {
        when(settings.get(eq(SettingKeys.INTEL_THEMES), eq(Map.class))).thenReturn(Optional.of(Map.of(
                "kinds", Map.of("internet-outage", "cyber", "my-kind", "health"),
                "keywords", List.of(Map.of("theme", "space", "words", List.of("storm"))))));
        assertThat(table.forKind("internet-outage")).isEqualTo("cyber");
        assertThat(table.forKind("my-kind")).isEqualTo("health");
        assertThat(table.forKeywords("A storm hit the coast")).isEqualTo("space");
    }

    @Test
    @DisplayName("keyword rules classify untyped text; a theme outside the fixed list is ignored")
    void keywords() {
        assertThat(table.forKeywords("Cholera outbreak reported in the camp")).isEqualTo("health");
        assertThat(table.forKeywords("Tanker diverted around the strait")).isEqualTo("maritime");
        assertThat(table.forKeywords("Nothing relevant here")).isNull();
        ThemeTable.Theme byKeyword = table.forText("bulletin", "Ransomware hits the port authority", "feeds");
        assertThat(byKeyword.source()).isEqualTo("keyword");
        assertThat(table.forText("internet-outage", "anything", "src")).isEqualTo(new ThemeTable.Theme(
                "infrastructure", "table"));
    }

    @Test
    @DisplayName("the LLM classification is off by default: no call, theme unknown")
    void llmOffByDefault() {
        assertThat(table.llmClassifyEnabled()).isFalse();
        assertThat(table.forText("bulletin", "Nothing any rule knows", "feeds")).isEqualTo(ThemeTable.Theme.UNKNOWN);
        assertThat(client.requests()).isEmpty();
        verify(cache, never()).save(any());
    }

    @Test
    @DisplayName("when on, the LLM picks one theme from the fixed list, labeled llm, and the answer is cached by hash")
    void llmOnAndCached() {
        when(settings.get(eq(SettingKeys.INTEL_THEMES_LLM_CLASSIFY), eq(Boolean.class))).thenReturn(Optional.of(true));
        client.then(FakeLlmClient.text("Conflict."));
        ThemeTable.Theme theme = table.forText("bulletin", "Nothing any rule knows", "feeds");
        assertThat(theme).isEqualTo(new ThemeTable.Theme("conflict", "llm"));
        assertThat(client.requests()).hasSize(1);
        assertThat(client.requests().get(0).system()).contains("exactly one theme");
        String hash = ThemeTable.sha256(ThemeTable.normalize("Nothing any rule knows"));
        verify(cache).save(org.mockito.ArgumentMatchers.argThat(e -> e.getTextHash().equals(hash)
                && e.getTheme().equals("conflict")));
        // A cached text is not sent again.
        when(cache.findById(hash)).thenReturn(Optional.of(new ThemeClassificationEntity(hash, "conflict", "m",
                Instant.parse("2026-03-01T12:00:00Z"))));
        assertThat(table.forText("bulletin", "  Nothing   any rule knows ", "feeds").theme()).isEqualTo("conflict");
        assertThat(client.requests()).hasSize(1);
    }

    @Test
    @DisplayName("an answer that is not a theme leaves the theme unknown and is not cached")
    void llmInvalidAnswer() {
        when(settings.get(eq(SettingKeys.INTEL_THEMES_LLM_CLASSIFY), eq(Boolean.class))).thenReturn(Optional.of(true));
        client.then(FakeLlmClient.text("geopolitics"));
        assertThat(table.classify("Something new", "feeds")).isEmpty();
        verify(cache, never()).save(any());
    }
}
