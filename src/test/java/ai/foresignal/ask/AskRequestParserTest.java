package ai.foresignal.ask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@link AskRequestParser}: members, lengths, scope and every error reported at once. */
class AskRequestParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    SourceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = mock(SourceRegistry.class);
        when(registry.contains(anyString())).thenAnswer(i -> Set.of("pub", "priv").contains(i.<String>getArgument(0)));
    }

    AskInput parse(String json) {
        return AskRequestParser.parse(JSON.readTree(json), registry, Set.of("priv"));
    }

    @Test
    @DisplayName("a full request parses, trimming the question")
    void parses() {
        AskInput input = parse("""
                {"question":"  Quakes?  ","threadId":"0b8f8d0e-3c3e-4f1e-9b7a-1d2c3b4a5f60","allowActions":true,
                 "scope":{"sources":["pub"],"kinds":["quake"],"bbox":"1,2,3,4","since":"2026-10-01T00:00:00Z",
                          "until":"2026-10-02T00:00:00+02:00"}}""");

        assertThat(input.question()).isEqualTo("Quakes?");
        assertThat(input.threadId()).isNotNull();
        assertThat(input.allowActions()).isTrue();
        assertThat(input.scope().sources()).containsExactly("pub");
        assertThat(input.scope().bbox()).isEqualTo("1,2,3,4");
        assertThat(input.scope().until().toString()).isEqualTo("2026-10-01T22:00:00Z");
        assertThat(parse("{\"question\":\"x\"}").scope()).isEqualTo(AskScope.NONE);
    }

    @Test
    @DisplayName("every problem is reported at once; a hidden source looks unknown")
    void errors() {
        assertThatThrownBy(() -> parse("""
                {"allowAction":true,"threadId":"nope","allowActions":"yes",
                 "scope":{"sources":["priv","none"],"bbox":"1,2","since":"2026-10-02T00:00:00Z","until":"2026-10-01T00:00:00Z",
                          "extra":1}}"""))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                        .extracting(err -> err.field())
                        .contains("allowAction", "question", "threadId", "allowActions", "scope.sources", "scope.bbox",
                                "scope.until", "scope.extra"));
        assertThatThrownBy(() -> parse("{\"question\":\"" + "x".repeat(4001) + "\"}"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> AskRequestParser.parse(JSON.readTree("[1]"), registry, Set.of()))
                .isInstanceOf(ValidationException.class);
    }
}
