package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.user.Permissions;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
 * <p>Prompt R2-004 extends the check to every route, implemented or planned, and to the
 * {@code x-foresignal-permission} of each ({@link AccessPolicy#permissionFor}): the scope and the permission must both
 * agree with the contract, so a planned operation cannot reach its controller with a looser rule than it was
 * authored with. Webhooks are excluded: they are requests Foresignal sends, so no inbound scope
 * applies. {@code peer} routes (federation, 113) require a peer ingest key
 * ({@link AccessRequirement#PEER}).
 *
 * <p>NP-001 adds a named check for the Automated Newspaper operations (API 2.1.0): each is in the contract and the
 * policy grants exactly what it was authored with, so the six cannot fall to the admin default unnoticed.
 */
class AccessPolicySyncTest {

    private final AccessPolicy policy = new AccessPolicy();

    @Test
    @DisplayName("every route, implemented or planned, requires exactly what its x-foresignal-scope says")
    void everyRouteMatchesItsScope() {
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (SpecOperation op : OpenApiSpec.routes()) {
            checked++;
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            AccessRequirement actual = policy.requirementFor(HttpMethod.valueOf(op.method().toUpperCase()), path);
            AccessRequirement expected = expected(op);
            if (!expected.equals(actual)) {
                problems.add(op.label() + (op.implemented() ? "" : " (planned)") + ": spec " + op.scope() + " ("
                        + expected + "), AccessPolicy " + actual);
            }
        }
        assertThat(checked).isPositive();
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("every route resolves to exactly its x-foresignal-permission, or to none when it carries none")
    void everyRouteMatchesItsPermission() {
        List<String> problems = new ArrayList<>();
        int withPermission = 0;
        for (SpecOperation op : OpenApiSpec.routes()) {
            JsonNode node = op.node().get("x-foresignal-permission");
            Optional<String> expected = node == null ? Optional.empty() : Optional.of(node.asString());
            expected.ifPresent(key -> assertThat(Permissions.byKey(key))
                    .as(op.label() + " names a permission in the catalog").isPresent());
            if (expected.isPresent()) {
                withPermission++;
            }
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            Optional<String> actual = policy.permissionFor(HttpMethod.valueOf(op.method().toUpperCase()), path);
            if (!expected.equals(actual)) {
                problems.add(op.label() + ": spec " + expected.orElse("none") + ", AccessPolicy "
                        + actual.orElse("none"));
            }
        }
        assertThat(withPermission).isPositive();
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("every route accepts exactly its permission and its x-foresignal-permission-alternatives (R2-009)")
    void everyRouteAcceptsItsAlternatives() {
        List<String> problems = new ArrayList<>();
        int withAlternatives = 0;
        for (SpecOperation op : OpenApiSpec.routes()) {
            JsonNode node = op.node().get("x-foresignal-permission");
            List<String> expected = new ArrayList<>();
            if (node != null) {
                expected.add(node.asString());
            }
            JsonNode alternatives = op.node().get("x-foresignal-permission-alternatives");
            if (alternatives != null) {
                assertThat(node).as(op.label() + " has alternatives only next to a permission").isNotNull();
                withAlternatives++;
                alternatives.forEach(key -> {
                    assertThat(Permissions.byKey(key.asString()))
                            .as(op.label() + " names alternatives in the catalog").isPresent();
                    expected.add(key.asString());
                });
            }
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            List<String> actual = policy.acceptedPermissions(HttpMethod.valueOf(op.method().toUpperCase()), path);
            if (!expected.equals(actual)) {
                problems.add(op.label() + ": spec " + expected + ", AccessPolicy " + actual);
            }
        }
        assertThat(withAlternatives).isPositive();
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("the generated permission table has one row per route")
    void permissionTableCoversEveryRoute() {
        assertThat(AccessPermissions.RULES).hasSize(OpenApiSpec.routes().size());
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
    @DisplayName("each Automated Newspaper operation (NP-001) is a route the policy grants exactly as authored")
    void newspaperOperationsHavePolicyEntries() {
        List<String> ids = List.of("getTranslationSettings", "updateTranslationSettings", "getMyNewsSubscriptions",
                "updateMyNewsSubscriptions", "listMyEditions", "getEditionSite");
        for (String id : ids) {
            SpecOperation op = OpenApiSpec.routes().stream()
                    .filter(candidate -> id.equals(candidate.operationId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(id + " is not a route of docs/OpenAPI.json"));
            HttpMethod method = HttpMethod.valueOf(op.method().toUpperCase());
            String path = op.path().replaceAll("\\{[^/}]+}", "sample");
            assertThat(policy.requirementFor(method, path)).as(op.label()).isEqualTo(expected(op));
            JsonNode node = op.node().get("x-foresignal-permission");
            assertThat(policy.permissionFor(method, path)).as(op.label())
                    .isEqualTo(node == null ? Optional.empty() : Optional.of(node.asString()));
        }
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/editions/sample/site")).isEqualTo(AccessRequirement.READ);
        assertThat(policy.requirementFor(HttpMethod.GET, "/v1/me/editions"))
                .isEqualTo(AccessRequirement.READ_ALWAYS_AUTHENTICATED);
        assertThat(policy.requirementFor(HttpMethod.PUT, "/v1/settings/translation")).isEqualTo(AccessRequirement.ADMIN);
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
            case "peer" -> AccessRequirement.PEER;
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
