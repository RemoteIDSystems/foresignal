package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;

/**
 * The policy table, asserted operation by operation.
 *
 * <p>The expected values come from the {@code x-foresignal-scope} extension on each operation in
 * {@code docs/OpenAPI.json}, so this test and {@link AccessPolicy} are two independent
 * transcriptions of the same contract; a mistake in either shows up here.
 */
class AccessPolicyTest {

    private final AccessPolicy policy = new AccessPolicy();

    private AccessRequirement requirement(String method, String path) {
        return policy.requirementFor(HttpMethod.valueOf(method), path);
    }

    private void assertScope(String method, String path, Scope expected) {
        assertThat(requirement(method, path))
                .as("%s %s", method, path)
                .isEqualTo(new AccessRequirement.RequiresScope(expected));
    }

    private void assertOpen(String method, String path) {
        assertThat(requirement(method, path))
                .as("%s %s", method, path)
                .isInstanceOf(AccessRequirement.Open.class);
    }

    // ---------- read ----------

    @ParameterizedTest(name = "READ: GET {0}")
    @ValueSource(
            strings = {
                "/v1/records",
                "/v1/records/usgs-earthquakes/us7000abcd",
                "/v1/records/usgs-earthquakes/us7000abcd/versions",
                "/v1/kinds",
                "/v1/kinds/earthquake/schema",
                "/v1/history",
                "/v1/changes",
                "/v1/changes/stream",
                "/v1/events/stream",
                "/v1/sources",
                "/v1/sources/health",
                "/v1/sources/usgs-earthquakes",
                "/v1/sources/usgs-earthquakes/health",
                "/v1/sources/usgs-earthquakes/usage",
                "/v1/tiles/records/3/4/5.mvt",
                "/v1/tiles.json",
                "/v1/tiles/earthquake.json",
                "/v1/search",
                "/v1/search/status",
                "/v1/alerts",
                "/v1/alerts/stream",
                "/v1/alerts/42",
                "/v1/triggers",
                "/v1/triggers/9f1e",
                "/v1/briefings",
                "/v1/briefings/9f1e",
                "/v1/briefings/9f1e/runs",
                "/v1/briefings/9f1e/runs/7",
                "/v1/ask/threads",
                "/v1/ask/threads/9f1e",
            })
    void dataGetsRequireRead(String path) {
        assertScope("GET", path, Scope.READ);
    }

    @ParameterizedTest(name = "READ: {0} {1}")
    @CsvSource({
        "POST, /v1/ask",
        "POST, /v1/ask/stream",
        "POST, /v1/push/subscriptions",
        "DELETE, /v1/push/subscriptions/abc123",
        "PATCH, /v1/events/stream/AbCdEfGhIjKlMnOpQrStUv/filter",
    })
    @DisplayName("a few writes need only read, because they act on the caller's own behalf")
    void someWritesRequireOnlyRead(String method, String path) {
        assertScope(method, path, Scope.READ);
    }

    @ParameterizedTest(name = "ADMIN by default: {0} {1}")
    @CsvSource({
        "GET, /v1/kinds/earthquake",
        "GET, /v1/kinds/earthquake/schema/extra",
        "PUT, /v1/kinds/earthquake/schema",
        "PATCH, /v1/events/stream",
        "POST, /v1/events/stream/abc/filter",
        "PATCH, /v1/events/stream/abc/filter/extra",
        "POST, /v1/tiles.json",
        "GET, /v1/tiles.jsonx",
    })
    @DisplayName("only GET /v1/kinds, GET /v1/kinds/{kind}/schema and the event-stream pair are reads; neighbors fail closed (014, 015, 016)")
    void kindSchemaRuleIsExact(String method, String path) {
        assertScope(method, path, Scope.ADMIN);
    }

    // ---------- agent ----------

    @ParameterizedTest(name = "AGENT: {0} {1}")
    @CsvSource({
        "POST, /v1/triggers",
        "PUT, /v1/triggers/9f1e",
        "DELETE, /v1/triggers/9f1e",
        "POST, /v1/triggers/9f1e/test",
        "POST, /v1/alerts/42/ack",
        "POST, /v1/alerts/42/resolve",
        "POST, /v1/alerts/42/snooze",
        "DELETE, /v1/alerts/42/snooze",
        "PUT, /v1/alerts/42/feedback",
        "POST, /v1/triggers/9f1e/snooze",
        "DELETE, /v1/triggers/9f1e/snooze",
        "POST, /v1/briefings",
        "POST, /v1/briefings/preview",
        "PUT, /v1/briefings/9f1e",
        "DELETE, /v1/briefings/9f1e",
        "POST, /v1/briefings/9f1e/run",
        "POST, /v1/ask/actions/abc/confirm",
        "POST, /v1/ask/actions/abc/reject",
        "DELETE, /v1/ask/threads/9f1e",
    })
    void agentOperations(String method, String path) {
        assertScope(method, path, Scope.AGENT);
    }

    @ParameterizedTest(name = "ADMIN by default: {0} {1}")
    @CsvSource({
        "PATCH, /v1/alerts/42/resolve",
        "GET, /v1/alerts/42/resolve/extra",
        "POST, /v1/alerts/42/feedback",
        "PUT, /v1/alerts/42/snooze",
        "POST, /v1/alerts/42",
    })
    @DisplayName("the alert lifecycle rules are exact; neighbors fail closed or fall to the read rule (017)")
    void alertLifecycleRulesAreExact(String method, String path) {
        assertScope(method, path, "GET".equals(method) ? Scope.READ : Scope.ADMIN);
    }

    // ---------- admin ----------

    @ParameterizedTest(name = "ADMIN: {0} {1}")
    @CsvSource({
        "GET, /v1/settings/retention",
        "PUT, /v1/settings/retention",
        "POST, /v1/settings/retention/run",
        "GET, /v1/settings/cors",
        "PUT, /v1/settings/cors",
        "GET, /v1/settings/security",
        "PUT, /v1/settings/security",
        "GET, /v1/settings/server",
        "PUT, /v1/settings/server",
        "GET, /v1/settings/export",
        "PUT, /v1/settings/import",
        "POST, /v1/system/admin-token/rotate",
        "GET, /v1/channels",
        "POST, /v1/channels",
        "GET, /v1/channels/types",
        "GET, /v1/channels/abc",
        "PUT, /v1/channels/abc",
        "DELETE, /v1/channels/abc",
        "GET, /v1/channels/abc/deliveries",
        "POST, /v1/channels/abc/test",
        "GET, /v1/tokens",
        "POST, /v1/tokens",
        "DELETE, /v1/tokens/abc",
        "GET, /v1/llm/providers",
        "POST, /v1/llm/providers",
        "GET, /v1/llm/providers/anthropic",
        "PUT, /v1/llm/providers/anthropic",
        "DELETE, /v1/llm/providers/anthropic",
        "GET, /v1/llm/providers/anthropic/models",
        "POST, /v1/llm/providers/anthropic/test",
        "GET, /v1/llm/settings",
        "PUT, /v1/llm/settings",
        "GET, /v1/llm/usage",
        "GET, /v1/custom-sources",
        "POST, /v1/custom-sources",
        "GET, /v1/custom-sources/custom-sdr",
        "PUT, /v1/custom-sources/custom-sdr",
        "DELETE, /v1/custom-sources/custom-sdr",
        "POST, /v1/custom-sources/custom-sdr/ingest-key/rotate",
        "PUT, /v1/sources/usgs-earthquakes",
        "PUT, /v1/sources/usgs-earthquakes/credentials",
        "DELETE, /v1/sources/usgs-earthquakes/credentials",
        "POST, /v1/sources/usgs-earthquakes/fetch",
        "POST, /v1/sources/usgs-earthquakes/probe",
        "GET, /v1/sources/usgs-earthquakes/fetches",
        "POST, /v1/search/reindex",
        "GET, /v1/push/subscriptions",
    })
    void adminOperations(String method, String path) {
        assertScope(method, path, Scope.ADMIN);
    }

    // ---------- open ----------

    @ParameterizedTest(name = "OPEN: GET {0}")
    @ValueSource(
            strings = {
                "/v1/system",
                "/v1/push/vapid-public-key",
                "/v1/openapi.json",
                "/v1/openapi-generated.json",
                "/v1/openapi-generated.json/swagger-config",
                "/docs",
                "/docs/index.html",
                "/actuator/health",
                "/actuator/health/readiness",
                "/actuator/info",
                "/actuator/prometheus",
                "/",
                "/favicon.ico",
            })
    void openPaths(String path) {
        assertOpen("GET", path);
    }

    // ---------- ingest ----------

    @Test
    @DisplayName("ingest accepts an ingest key or an admin token")
    void ingestIsSpecialCased() {
        assertThat(requirement("POST", "/v1/ingest/custom-sdr"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
    }

    // ---------- defaults and edges ----------

    @Test
    @DisplayName("an unknown /v1 path requires admin, so a new endpoint is never open by accident")
    void unknownV1PathsDenyByDefault() {
        assertScope("GET", "/v1/something-new", Scope.ADMIN);
        assertScope("POST", "/v1/something-new", Scope.ADMIN);
        assertScope("DELETE", "/v1/records/usgs/abc", Scope.ADMIN);
    }

    @Test
    @DisplayName("a trailing slash does not change the decision")
    void ignoresTrailingSlash() {
        assertScope("GET", "/v1/records/", Scope.READ);
        assertScope("PUT", "/v1/settings/cors/", Scope.ADMIN);
    }

    @Test
    @DisplayName("a query string does not change the decision")
    void ignoresQueryString() {
        assertScope("GET", "/v1/records?kind=earthquake&limit=10", Scope.READ);
    }

    @Test
    @DisplayName("specific source rules beat the general source read rule")
    void specificRulesWin() {
        assertScope("GET", "/v1/sources/usgs-earthquakes", Scope.READ);
        assertScope("PUT", "/v1/sources/usgs-earthquakes", Scope.ADMIN);
        assertScope("PUT", "/v1/sources/usgs-earthquakes/credentials", Scope.ADMIN);
    }

    @Test
    @DisplayName("the fetch log is admin even though it is a GET under /v1/sources (013)")
    void fetchLogIsAdmin() {
        assertScope("GET", "/v1/sources/usgs-earthquakes/fetches", Scope.ADMIN);
        assertScope("GET", "/v1/sources/usgs-earthquakes/fetches?limit=10&status=OK", Scope.ADMIN);
        assertScope("GET", "/v1/sources/usgs-earthquakes/fetches/", Scope.ADMIN);
        // The neighboring reads stay read.
        assertScope("GET", "/v1/sources/usgs-earthquakes/health", Scope.READ);
        assertScope("GET", "/v1/sources/usgs-earthquakes/usage", Scope.READ);
        assertScope("GET", "/v1/sources/health", Scope.READ);
    }

    @Test
    @DisplayName("reading triggers is a read, changing them is not")
    void triggerReadsAndWritesDiffer() {
        assertScope("GET", "/v1/triggers/9f1e", Scope.READ);
        assertScope("PUT", "/v1/triggers/9f1e", Scope.AGENT);
    }

    @Test
    @DisplayName("listing push subscriptions is admin even though creating one is read")
    void pushSubscriptionsSplitByMethod() {
        assertScope("GET", "/v1/push/subscriptions", Scope.ADMIN);
        assertScope("POST", "/v1/push/subscriptions", Scope.READ);
    }

    @Test
    @DisplayName("a null or empty path is handled without blowing up")
    void toleratesOddPaths() {
        assertOpen("GET", "/");
        assertThat(policy.requirementFor(HttpMethod.GET, null))
                .isInstanceOf(AccessRequirement.Open.class);
        assertThat(policy.requirementFor(HttpMethod.GET, ""))
                .isInstanceOf(AccessRequirement.Open.class);
    }

    @Test
    @DisplayName("OPTIONS on a protected path still resolves through the table")
    void optionsResolves() {
        assertThat(requirement("OPTIONS", "/v1/settings/cors")).isNotNull();
    }
}
