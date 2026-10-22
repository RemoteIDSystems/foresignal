package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import ai.foresignal.trigger.OperatorRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shared-attribute join (prompt 157): a candidate joins a cluster when its payload value at a dot-path equals
 * a seed member's — the join a fraud/relationship recipe needs, matching on the attribute rather than geography.
 */
class SharedAttributeJoinTest {

    private CorrelationEngine.MemberRow row(String externalId, Map<String, Object> payload) {
        return new CorrelationEngine.MemberRow("companies-house", externalId, "company", 1, 1L,
                Instant.parse("2026-06-01T00:00:00Z"), null, payload);
    }

    @Test
    @DisplayName("a candidate sharing the attribute value with the cluster matches")
    void matchesOnSharedValue() {
        CorrelationEngine.MemberRow seed = row("A", Map.of("registeredAddress", "1 High St"));
        CorrelationEngine.MemberRow candidate = row("B", Map.of("registeredAddress", "1 High St"));

        Object matched = CorrelationEngine.sharedAttributeMatch(candidate, List.of(seed), "registeredAddress");

        assertThat(matched).isEqualTo("1 High St");
    }

    @Test
    @DisplayName("a candidate with a different or absent attribute does not match")
    void noMatchWhenDifferentOrAbsent() {
        CorrelationEngine.MemberRow seed = row("A", Map.of("registeredAddress", "1 High St"));
        CorrelationEngine.MemberRow different = row("B", Map.of("registeredAddress", "9 Low Rd"));
        CorrelationEngine.MemberRow absent = row("C", Map.of("name", "Acme"));

        assertThat(CorrelationEngine.sharedAttributeMatch(different, List.of(seed), "registeredAddress")).isNull();
        assertThat(CorrelationEngine.sharedAttributeMatch(absent, List.of(seed), "registeredAddress")).isNull();
    }

    @Test
    @DisplayName("a nested dot-path is read from the payload")
    void matchesNestedPath() {
        CorrelationEngine.MemberRow seed = row("A", Map.of("officer", Map.of("id", "P-1")));
        CorrelationEngine.MemberRow candidate = row("B", Map.of("officer", Map.of("id", "P-1")));

        assertThat(CorrelationEngine.sharedAttributeMatch(candidate, List.of(seed), "officer.id")).isEqualTo("P-1");
    }

    @Test
    @DisplayName("the join predicate round-trips through the recipe definition")
    void parsesSharedAttributeJoin() {
        OperatorRegistry operators = mock(OperatorRegistry.class);
        Map<String, Object> definition = Map.of(
                "type", "maritime-disruption",
                "seed", Map.of("kinds", List.of("company")),
                "joins", List.of(Map.of("kinds", List.of("company"), "sharedAttribute", "registeredAddress")));

        RecipeDefinition parsed = RecipeDefinition.of("shell-companies", 1, definition, operators);

        assertThat(parsed.joins()).hasSize(1);
        assertThat(parsed.joins().get(0).sharedAttribute()).isEqualTo("registeredAddress");
    }
}
