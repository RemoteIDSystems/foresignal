package ai.foresignal.llm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.anthropic.core.ObjectMappers;
import com.anthropic.models.beta.messages.BetaMessageParam;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

/** {@link RawTurnCodec}: every turn kind round-trips, raw turns byte for byte. */
class RawTurnCodecTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    final RawTurnCodec codec = new RawTurnCodec(JSON);

    /** Encodes, passes through a JSON text (as a jsonb column would) and decodes. */
    List<ChatMessage> roundTrip(List<ChatMessage> messages, String providerId, String currentProvider) {
        ArrayNode encoded = codec.encodeAll(messages, providerId);
        JsonNode stored = JSON.readTree(encoded.toString());
        return codec.decodeAll(stored, currentProvider);
    }

    @Test
    @DisplayName("user, assistant and tool-result turns round-trip; JSON and string raw turns keep their exact text")
    void roundTrips() {
        JsonNode raw = JSON.readTree("{\"z\":1,\"a\":[3,2,1],\"content\":[{\"type\":\"thinking\",\"signature\":\"c2ln\"}]}");
        ToolCall call = new ToolCall("t1", "query_records", JSON.readTree("{\"limit\":5}"));
        List<ChatMessage> messages = List.of(
                new ChatMessage.User("Question?"),
                new ChatMessage.Assistant(raw, "Looking.", List.of(call)),
                new ChatMessage.ToolResults(List.of(ToolResult.ok(call, "{\"tool\":\"query_records\",\"data\":{}}"),
                        new ToolResult("t2", "get_record", "not found", true))),
                new ChatMessage.Assistant("opaque-raw", "Answer.", List.of()));

        List<ChatMessage> decoded = roundTrip(messages, "p", "p");

        assertThat(decoded).hasSize(4);
        assertThat(((ChatMessage.User) decoded.get(0)).text()).isEqualTo("Question?");
        ChatMessage.Assistant assistant = (ChatMessage.Assistant) decoded.get(1);
        assertThat(assistant.rawTurn().toString()).isEqualTo(raw.toString());
        assertThat(assistant.toolCalls().getFirst().input().get("limit").asInt()).isEqualTo(5);
        assertThat(((ChatMessage.ToolResults) decoded.get(2)).results())
                .isEqualTo(((ChatMessage.ToolResults) messages.get(2)).results());
        assertThat(((ChatMessage.Assistant) decoded.get(3)).rawTurn()).isEqualTo("opaque-raw");
    }

    @Test
    @DisplayName("an Anthropic message param round-trips to the same JSON, thinking signature included")
    void anthropic() throws Exception {
        String json = "{\"role\":\"assistant\",\"content\":[{\"type\":\"thinking\",\"thinking\":\"\","
                + "\"signature\":\"c2lnbmF0dXJlLTE=\"},{\"type\":\"text\",\"text\":\"Hi\"}]}";
        BetaMessageParam param = ObjectMappers.jsonMapper().readValue(json, BetaMessageParam.class);

        List<ChatMessage> decoded = roundTrip(List.of(new ChatMessage.Assistant(param, "Hi", List.of())), "claude",
                "claude");

        Object raw = ((ChatMessage.Assistant) decoded.getFirst()).rawTurn();
        assertThat(raw).isInstanceOf(BetaMessageParam.class);
        assertThat(codec.rawText(raw)).isEqualTo(codec.rawText(param));
        assertThat(codec.rawText(raw)).contains("c2lnbmF0dXJlLTE=");
    }

    @Test
    @DisplayName("a turn from another provider is replayed without its raw form; unknown raw types are dropped")
    void otherProvider() {
        List<ChatMessage> decoded = roundTrip(List.of(new ChatMessage.Assistant("raw", "Text", List.of())), "old", "new");
        assertThat(((ChatMessage.Assistant) decoded.getFirst()).rawTurn()).isNull();
        assertThat(((ChatMessage.Assistant) decoded.getFirst()).text()).isEqualTo("Text");

        JsonNode encoded = codec.encode(new ChatMessage.Assistant(new Object(), "T", List.of()), "p");
        assertThat(encoded.get("format").asString()).isEqualTo(RawTurnCodec.NONE);
        assertThat(encoded.get("raw").isNull()).isTrue();
    }

    @Test
    @DisplayName("a node that is not an encoded turn is refused")
    void refusesGarbage() {
        assertThatThrownBy(() -> codec.decode(JSON.readTree("{\"kind\":\"system\"}"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
