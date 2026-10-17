package ai.foresignal.object;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@link ContextLinks} and {@link WorkspaceContextValidator}: Client-Contract §5–§6 (prompt 129). */
class ContextLinkTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static JsonNode tree(String json) {
        return JSON.readTree(json.replace('\'', '"'));
    }

    @Test
    @DisplayName("encode then decode returns the workspace and the context, without null members")
    void roundTrip() {
        JsonNode ctx = tree("{'selectedRefs':['record:adsblol-mil/ae1234','situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C'],"
                + "'focusRef':'record:adsblol-mil/ae1234','timeRange':{'start':'2026-10-10T00:00:00Z','end':null},"
                + "'asOf':null,'playback':{'state':'paused','rate':60},'viewport':{'center':[-96.55,33.75],"
                + "'radiusKm':150},'filters':{'kinds':['aircraft','vessel'],'minSeverity':'warning'},"
                + "'layers':['aircraft','observability:aircraft']}");
        Map<String, Object> link = ContextLinks.encode("01JAB8Q3V7S2K9W4T6Y1B5N0M3", ctx);
        assertThat(link.get("version")).isEqualTo(1);
        assertThat((String) link.get("fragment")).startsWith("#fs=");
        assertThat((Integer) link.get("bytes")).isLessThanOrEqualTo(ContextLinks.MAX_BYTES);
        ContextLinks.Decoded decoded = ContextLinks.decode((String) link.get("fragment"));
        assertThat(decoded.version()).isEqualTo(1);
        assertThat(decoded.workspaceId()).isEqualTo("01JAB8Q3V7S2K9W4T6Y1B5N0M3");
        assertThat(decoded.context().get("focusRef").asString()).isEqualTo("record:adsblol-mil/ae1234");
        assertThat(decoded.context().has("asOf")).isFalse();
        assertThat(ContextLinks.decode((String) link.get("payload")).context()).isEqualTo(decoded.context());
    }

    @Test
    @DisplayName("an ad-hoc context has no workspace")
    void adHoc() {
        Map<String, Object> link = ContextLinks.encode(null, tree("{'focusRef':'situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C'}"));
        assertThat(link.get("workspaceId")).isNull();
        assertThat(ContextLinks.decode((String) link.get("payload")).workspaceId()).isNull();
    }

    @Test
    @DisplayName("a payload over 2 KB is 413 PAYLOAD_TOO_LARGE")
    void sizeCap() {
        StringBuilder refs = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            refs.append(i == 0 ? "" : ",").append("'record:adsblol-mil/ae").append(1000 + i).append("'");
        }
        JsonNode ctx = tree("{'selectedRefs':[" + refs + "]}");
        assertThatThrownBy(() -> ContextLinks.encode(null, ctx)).isInstanceOf(ForesignalException.class)
                .satisfies(e -> {
                    assertThat(((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.PAYLOAD_TOO_LARGE);
                    assertThat(((ForesignalException) e).getStatus().value()).isEqualTo(413);
                });
    }

    @Test
    @DisplayName("schema validation: refs, ordered time range, viewport shape, simple filters, 200 selected refs")
    void schemaValidation() {
        assertThatThrownBy(() -> ContextLinks.encode(null, tree("{'selectedRefs':['nope']}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.OBJECT_REF_INVALID));
        for (String bad : new String[] {
            "{'timeRange':{'start':'2026-10-11T00:00:00Z','end':'2026-10-10T00:00:00Z'}}",
            "{'timeRange':{'end':'2026-10-10T00:00:00Z'}}",
            "{'viewport':{'bbox':[0,0,1,1],'center':[0,0],'radiusKm':5}}",
            "{'viewport':{'bbox':[0,10,1,5]}}",
            "{'viewport':{'center':[0,0]}}",
            "{'viewport':{'bbox':[0,0,1,1],'zoom':4}}",
            "{'playback':{'state':'rewinding'}}",
            "{'filters':{'nested':{'a':1}}}",
            "{'activeSituationId':'not-a-ulid'}",
            "{'cameraHeading':12}"}) {
            assertThatThrownBy(() -> WorkspaceContextValidator.validate(tree(bad), "context"))
                    .as(bad).isInstanceOf(ValidationException.class);
        }
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            many.append(i == 0 ? "" : ",").append("'situation:S").append(i).append("'");
        }
        assertThatThrownBy(() -> WorkspaceContextValidator.validate(tree("{'selectedRefs':[" + many + "]}"),
                "context")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("malformed payloads are rejected on decode")
    void malformedPayloads() {
        assertThatThrownBy(() -> ContextLinks.decode("#fs=***")).isInstanceOf(ValidationException.class);
        String v2 = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"v\":2,\"ws\":null,\"ctx\":{}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThatThrownBy(() -> ContextLinks.decode(v2)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a JSON merge patch merges objects and removes null members")
    void mergePatch() {
        JsonNode merged = JsonMergePatch.apply(tree("{'a':1,'b':{'c':2,'d':3},'e':[1]}"),
                tree("{'a':null,'b':{'c':null,'x':4},'e':[2,3]}"));
        assertThat(merged).isEqualTo(tree("{'b':{'d':3,'x':4},'e':[2,3]}"));
    }
}
