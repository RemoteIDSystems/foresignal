package ai.foresignal.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link LlmProviderValidator} and {@link LlmSettings#parse}: defaults, baseUrl shape, options. */
class LlmProviderValidatorTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    final LlmProviderValidator validator = new LlmProviderValidator(
            ai.foresignal.testsupport.net.TestOutbound.offlinePolicy());

    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    static List<String> fields(Runnable action) {
        try {
            action.run();
        } catch (ValidationException e) {
            return e.getErrors().stream().map(FieldError::field).toList();
        }
        throw new AssertionError("expected a ValidationException");
    }

    @Test
    @DisplayName("anthropic defaults: base URL, claude-opus-5-5, effort medium, maxTokens 16000")
    void anthropicDefaults() {
        LlmProviderInput input = validator.parse(json("{'id':'claude','type':'anthropic','apiKey':'k'}"), null);

        assertThat(input.baseUrl()).isEqualTo("https://api.anthropic.com");
        assertThat(input.chatModel()).isEqualTo("claude-opus-5-5");
        assertThat(input.options()).containsEntry("effort", "medium").containsEntry("maxTokens", 16_000);
        assertThat(input.requestsPerMinute()).isEqualTo(30);
        assertThat(input.tokensPerDay()).isEqualTo(2_000_000L);
        assertThat(input.enabled()).isTrue();
        assertThat(input.apiKey()).isEqualTo("k");
        assertThat(input.toString()).doesNotContain("k]").contains("apiKeySet=true");
    }

    @Test
    @DisplayName("ollama defaults to http://ollama:11434; openai-compatible needs a baseUrl")
    void otherDefaults() {
        assertThat(validator.parse(json("{'id':'ollama','type':'ollama'}"), null).baseUrl())
                .isEqualTo("http://ollama:11434");
        assertThat(fields(() -> validator.parse(json("{'id':'oa','type':'openai-compatible'}"), null)))
                .containsExactly("baseUrl");
        assertThat(validator.parse(json("{'id':'oa','type':'openai-compatible','baseUrl':'http://192.168.1.5:8000/v1/'}"),
                null).baseUrl()).isEqualTo("http://192.168.1.5:8000/v1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://host", "http://user:pw@host", "http://host/#frag", "not a uri", "http:///nohost",
        "file:/etc/passwd"})
    @DisplayName("baseUrl: http/https, a host, no user info, no fragment")
    void badBaseUrl(String url) {
        List<FieldError> errors = new ArrayList<>();
        LlmProviderValidator.validateBaseUrl(url, errors);
        assertThat(errors).isNotEmpty().allSatisfy(e -> assertThat(e.field()).isEqualTo("baseUrl"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:11434", "http://10.0.0.2", "https://api.openai.com/v1",
        "http://ollama:11434"})
    @DisplayName("private and loopback addresses are allowed")
    void goodBaseUrl(String url) {
        List<FieldError> errors = new ArrayList<>();
        LlmProviderValidator.validateBaseUrl(url, errors);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("every member is checked and every error reported")
    void errors() {
        List<String> fields = fields(() -> validator.parse(json("""
                {'id':'Bad Id','type':'gpt','embeddingDimensions':4,'apiKey':'','options':{'effort':'huge',
                 'maxTokens':10,'temperature':5,'extraHeaders':{'Authorization':'x','Bad Name':'y'},'local':'yes','other':1},
                 'requestsPerMinute':0,'tokensPerDay':10,'enabled':'no','knowledgeCutoff':'2025-13-01','unknown':true}"""),
                null));

        assertThat(fields).contains("id", "type", "embeddingDimensions", "apiKey", "options.effort",
                "options.maxTokens", "options.temperature", "options.extraHeaders.Authorization",
                "options.extraHeaders.Bad Name", "options.local", "options.other", "requestsPerMinute", "tokensPerDay",
                "enabled", "knowledgeCutoff", "unknown");
    }

    @Test
    @DisplayName("anthropic refuses embeddings and temperature")
    void anthropicLimits() {
        assertThat(fields(() -> validator.parse(json(
                "{'id':'c','type':'anthropic','embeddingModel':'x','options':{'temperature':0.5}}"), null)))
                .containsExactly("embeddingModel", "options.temperature");
    }

    @Test
    @DisplayName("options, cutoff and output-only members round-trip; the path id must match a body id")
    void fullInput() {
        LlmProviderInput input = validator.parse(json("""
                {'id':'router','type':'openai-compatible','baseUrl':'https://openrouter.ai/api/v1','chatModel':'x/y',
                 'embeddingModel':'e','embeddingDimensions':1024,'options':{'effort':'HIGH','maxTokens':4096,
                 'temperature':0.2,'extraHeaders':{'HTTP-Referer':'https://foresignal.ai'},'local':true},
                 'requestsPerMinute':10,'tokensPerDay':5000,'enabled':false,'knowledgeCutoff':'2025-03-01',
                 'apiKeySet':true,'status':'healthy'}"""), "router");

        assertThat(input.options()).containsEntry("effort", "high").containsEntry("maxTokens", 4096)
                .containsEntry("temperature", 0.2).containsEntry("local", true)
                .containsEntry("extraHeaders", Map.of("HTTP-Referer", "https://foresignal.ai"));
        assertThat(input.knowledgeCutoff()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(input.enabled()).isFalse();
        assertThat(input.apiKey()).isNull();
        assertThat(fields(() -> validator.parse(json("{'id':'other','type':'ollama'}"), "router")))
                .containsExactly("id");
        assertThat(validator.parse(json("{'type':'ollama'}"), "router").id()).isEqualTo("router");
    }

    @Test
    @DisplayName("a non-object body is refused")
    void notAnObject() {
        assertThatThrownBy(() -> validator.parse(json("[]"), null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> validator.parse(null, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("LlmSettings.parse: defaults, types, askMaxTokens floor, allowPrivateLocal accepted (039)")
    void settings() {
        LlmSettings defaults = LlmSettings.parse(json("{}"));
        assertThat(defaults).isEqualTo(LlmSettings.DEFAULTS);
        assertThat(defaults.configured()).isFalse();

        LlmSettings set = LlmSettings.parse(json("""
                {'chatProvider':'claude','embeddingProvider':'ollama','askEnabled':false,'enrichmentEnabled':false,
                 'logContent':true,'askMaxTokens':5000,'allowPrivateLocal':false}"""));
        assertThat(set).isEqualTo(new LlmSettings("claude", "ollama", false, false, true, 5000, false));
        assertThat(set.configured()).isTrue();

        assertThat(fields(() -> LlmSettings.parse(json("""
                {'chatProvider':3,'askEnabled':'yes','askMaxTokens':10,'allowPrivateLocal':'yes','other':1}"""))))
                .containsExactlyInAnyOrder("chatProvider", "askEnabled", "askMaxTokens", "allowPrivateLocal", "other");
        assertThatThrownBy(() -> LlmSettings.parse(json("[]"))).isInstanceOf(ValidationException.class);
        assertThat(LlmSettings.parse(json("{'allowPrivateLocal':true}")).allowPrivateLocal()).isTrue();
    }
}
