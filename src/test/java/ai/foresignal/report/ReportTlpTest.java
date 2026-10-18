package ai.foresignal.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.exchange.ExchangeTlp;
import ai.foresignal.security.Tlp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * TLP on reports and packages (prompt 132, COL-2): a report inherits the strictest label of what it cites, may not be
 * labeled below it, and every send, link and package is checked per recipient, refused with {@code TLP_VIOLATION}
 * naming the rule.
 */
class ReportTlpTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static String rule(Runnable r) {
        try {
            r.run();
        } catch (ForesignalException e) {
            assertThat(e.getCode()).isEqualTo(ErrorCodes.TLP_VIOLATION);
            return String.valueOf(e.getProperties().get("rule"));
        }
        throw new AssertionError("expected TLP_VIOLATION");
    }

    @Test
    @DisplayName("a report inherits the strictest label of the envelopes it cites")
    void inheritsStrictest() {
        ReportSnapshots.Collected collected = new ReportSnapshots.Collected();
        collected.envelope("evidence-item:01JA8Q3V7S2K9W4T6Y1B5N0M3C", Map.of("ref", "evidence-item:x", "tlp", "GREEN"));
        collected.envelope("investigation:01JA8Q3V7S2K9W4T6Y1B5N0M3D", Map.of("ref", "investigation:y", "tlp", "RED"));
        collected.envelope("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3E", Map.of("ref", "situation:z", "private", true,
                "provenance", Map.of("sourceIds", List.of("src-a", "src-b"))));
        assertThat(Tlp.strictest(collected.tlps, Tlp.AMBER)).isEqualTo(Tlp.RED);
        assertThat(collected.anyPrivate).isTrue();
        assertThat(collected.sourceIds).containsExactly("src-a", "src-b");
        assertThat(ReportSnapshots.tlp(Map.of("tlp", "AMBER+STRICT"))).isEqualTo(Tlp.AMBER_STRICT);
        assertThat(ReportSnapshots.tlp(Map.of())).isNull();
        assertThat(ReportSnapshots.tlp(Map.of("tlp", "nonsense"))).isNull();
    }

    @Test
    @DisplayName("labeling below the strictest input is refused with the inheritance rule")
    void floor() {
        ForesignalException e = ReportService.tlpFloor(Tlp.RED);
        assertThat(e.getCode()).isEqualTo(ErrorCodes.TLP_VIOLATION);
        assertThat(String.valueOf(e.getProperties().get("rule"))).contains("RED").contains("strictest");
    }

    @Test
    @DisplayName("public report links need CLEAR, or GREEN when exchange.allowGreenLinks is on")
    void links() {
        assertThatCode(() -> ReportService.checkPublicLink(Tlp.CLEAR, false)).doesNotThrowAnyException();
        assertThat(rule(() -> ReportService.checkPublicLink(Tlp.GREEN, false))).contains("CLEAR");
        assertThatCode(() -> ReportService.checkPublicLink(Tlp.GREEN, true)).doesNotThrowAnyException();
        assertThat(rule(() -> ReportService.checkPublicLink(Tlp.AMBER, true))).contains("AMBER");
        assertThat(rule(() -> ReportService.checkPublicLink(Tlp.RED, true))).contains("RED");
    }

    @Test
    @DisplayName("packages: * needs GREEN or CLEAR; named remote recipients follow the 130 rules")
    void packages() {
        ExchangeTlp.Recipients everyone = ExchangeTlp.parse(JSON.readTree("\"*\""));
        assertThat(everyone.everyone()).isTrue();
        assertThatCode(() -> ExchangeTlp.enforce(Tlp.GREEN, everyone, i -> false)).doesNotThrowAnyException();
        assertThat(rule(() -> ExchangeTlp.enforce(Tlp.AMBER, everyone, i -> false))).contains("AMBER");

        ExchangeTlp.Recipients named = ExchangeTlp.parse(JSON.readTree(
                "[{\"instanceId\":\"fs-eu-1\",\"principals\":[\"jsmith\",\"JSmith\"]}]"));
        assertThat(named.byInstance().get("fs-eu-1")).containsExactly("jsmith");
        assertThatCode(() -> ExchangeTlp.enforce(Tlp.RED, named, i -> false)).doesNotThrowAnyException();
        assertThatCode(() -> ExchangeTlp.enforce(Tlp.AMBER, named, i -> false)).doesNotThrowAnyException();
        assertThat(rule(() -> ExchangeTlp.enforce(Tlp.AMBER_STRICT, named, i -> false))).contains("AMBER+STRICT");
        assertThatCode(() -> ExchangeTlp.enforce(Tlp.AMBER_STRICT, named, i -> true)).doesNotThrowAnyException();

        ExchangeTlp.Recipients wildcardThere = ExchangeTlp.parse(JSON.readTree(
                "[{\"instanceId\":\"fs-eu-1\",\"principals\":[\"*\"]}]"));
        assertThat(rule(() -> ExchangeTlp.enforce(Tlp.RED, wildcardThere, i -> false))).contains("RED");
        assertThatCode(() -> ExchangeTlp.enforce(Tlp.CLEAR, wildcardThere, i -> false)).doesNotThrowAnyException();

        assertThatThrownBy(() -> ExchangeTlp.parse(JSON.readTree("[]"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ExchangeTlp.parse(JSON.readTree(
                "[{\"instanceId\":\"fs-eu-1\",\"principals\":[\"not a handle\"]}]")))
                .isInstanceOf(ValidationException.class);
        assertThat(named.toManifest()).isEqualTo(List.of(Map.of("instanceId", "fs-eu-1", "principals",
                List.of("jsmith"))));
        assertThat(everyone.toManifest()).isEqualTo("*");
    }

    @Test
    @DisplayName("a probability-bearing key judgment must cite a question or a hypothesis set; its ICD term is derived")
    void judgments() {
        assertThatThrownBy(() -> ReportBlocks.parse(JSON.readTree("{\"type\":\"bluf\",\"judgments\":[{\"text\":"
                + "\"Likely escalation\",\"probability\":0.7}]}"), "block", ReportVocabulary.AUTHOR))
                .isInstanceOf(ValidationException.class);
        Map<String, Object> block = ReportBlocks.parse(JSON.readTree("{\"type\":\"bluf\",\"judgments\":[{\"text\":"
                + "\"Escalation\",\"probability\":0.7,\"questionRef\":\"question:01JA8Q3V7S2K9W4T6Y1B5N0M3C\"}]}"),
                "block", ReportVocabulary.AUTHOR);
        @SuppressWarnings("unchecked")
        Map<String, Object> j = ((List<Map<String, Object>>) block.get("judgments")).get(0);
        assertThat(j.get("icdTerm")).isEqualTo("likely");
        assertThat(ReportBlocks.citedRefs(block)).containsExactly("question:01JA8Q3V7S2K9W4T6Y1B5N0M3C");
    }
}
