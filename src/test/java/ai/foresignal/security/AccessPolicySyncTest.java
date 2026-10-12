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
 * {@link AccessPolicy} against {@code x-foresignal-scope} of every implemented route in
 * {@code docs/OpenAPI.json} (prompt 020, invariant PRC-1).
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
    @DisplayName("webhooks are outbound and are never part of the inbound policy check")
    void webhooksAreExcluded() {
        assertThat(OpenApiSpec.webhooks()).isNotEmpty().allSatisfy(op -> assertThat(op.isRoute()).isFalse());
        assertThat(OpenApiSpec.routes()).allSatisfy(op -> assertThat(op.isRoute()).isTrue());
    }

    /** The requirement {@code op}'s annotations call for. */
    private static AccessRequirement expected(SpecOperation op) {
        if ("open".equals(op.scope()) && acceptsIngestKey(op.node())) {
            return AccessRequirement.INGEST_KEY_OR_ADMIN;
        }
        return switch (op.scope()) {
            case "open" -> AccessRequirement.OPEN;
            case "read" -> AccessRequirement.READ;
            case "agent" -> AccessRequirement.AGENT;
            case "admin" -> AccessRequirement.ADMIN;
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
