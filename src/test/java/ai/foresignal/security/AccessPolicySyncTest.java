package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.JsonNode;

/**
 * {@link AccessPolicy} against {@code x-foresignal-scope} and
 * {@code x-foresignal-always-authenticated} of every implemented route in {@code docs/OpenAPI.json}
 * (prompt 020, invariant PRC-1; prompt 023 adds the flag, invariants SEC-4 and SEC-5).
 *
 * <p>Prompt 023 names this check {@code AccessPolicySpecSyncTest}; it extends this existing class
 * rather than duplicating it.
 *
 * <p>Only implemented routes are checked: the policy is written ahead for many planned paths, and
 * each prompt that implements an operation (setting its status to {@code implemented}) is then held
 * to it here. Webhooks are excluded: they are requests Foresignal sends, so no inbound scope
 * applies. {@code peer} routes (federation, 113) are excluded until the policy has a peer
 * requirement.
 */
class AccessPolicySyncTest {

    private final AccessPolicy policy = new AccessPolicy();

    @Test
    @DisplayName("every implemented route requires exactly what its x-foresignal-scope says")
    void implementedRoutesMatchTheirScope() {
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (SpecOperation op : OpenApiSpec.routes()) {
            if (!op.implemented() || "peer".equals(op.scope())) {
                continue;
            }
            checked++;
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            AccessRequirement actual = policy.requirementFor(HttpMethod.valueOf(op.method().toUpperCase()), path);
            AccessRequirement expected = expected(op);
            if (!expected.equals(actual)) {
                problems.add(op.label() + ": spec " + op.scope() + " (" + expected + "), AccessPolicy " + actual);
            }
        }
        assertThat(checked).isPositive();
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("a planned read route the policy already grants read carries the spec's always-authenticated flag")
    void plannedReadRoutesCarryTheFlag() {
        List<String> problems = new ArrayList<>();
        for (SpecOperation op : OpenApiSpec.routes()) {
            if (op.implemented() || !"read".equals(op.scope())) {
                continue;
            }
            boolean specFlag = op.node().path("x-foresignal-always-authenticated").asBoolean(false);
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            AccessRequirement actual = policy.requirementFor(HttpMethod.valueOf(op.method().toUpperCase()), path);
            // Only where the flag changes behavior: the policy already grants read (planned routes
            // without a rule fall to admin, which needs a token anyway).
            if (actual instanceof AccessRequirement.RequiresScope r && r.scope() == Scope.READ
                    && specFlag != r.alwaysAuthenticated()) {
                problems.add(op.label() + " (planned): spec always-authenticated " + specFlag + ", AccessPolicy " + actual);
            }
        }
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("webhooks are outbound and are never part of the inbound policy check")
    void webhooksAreExcluded() {
        assertThat(OpenApiSpec.webhooks()).isNotEmpty().allSatisfy(op -> assertThat(op.isRoute()).isFalse());
        assertThat(OpenApiSpec.routes()).allSatisfy(op -> assertThat(op.isRoute()).isTrue());
    }

    /** The requirement {@code op}'s annotations call for, including the always-authenticated flag. */
    private static AccessRequirement expected(SpecOperation op) {
        if ("open".equals(op.scope()) && acceptsIngestKey(op.node())) {
            return AccessRequirement.INGEST_KEY_OR_ADMIN;
        }
        boolean always = op.node().path("x-foresignal-always-authenticated").asBoolean(false);
        return switch (op.scope()) {
            case "open" -> AccessRequirement.OPEN;
            case "read" -> new AccessRequirement.RequiresScope(Scope.READ, always);
            case "agent" -> new AccessRequirement.RequiresScope(Scope.AGENT, always);
            case "admin" -> new AccessRequirement.RequiresScope(Scope.ADMIN, always);
            default -> throw new AssertionError(op.label() + ": unknown scope " + op.scope());
        };
    }

    /** True when the operation's security alternatives include an ingest key scheme. */
    private static boolean acceptsIngestKey(JsonNode operation) {
        JsonNode security = operation.get("security");
        if (security == null) {
            return false;
        }
        for (JsonNode alternative : security) {
            for (String scheme : alternative.propertyNames()) {
                if (scheme.startsWith("ingestKey")) {
                    return true;
                }
            }
        }
        return false;
    }
}
