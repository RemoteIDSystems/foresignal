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

    /** Asserts the scope only; {@link #alwaysAuthenticatedRules} checks the flag (prompt 023). */
    private void assertScope(String method, String path, Scope expected) {
        assertThat(requirement(method, path))
                .as("%s %s", method, path)
                .isInstanceOfSatisfying(AccessRequirement.RequiresScope.class,
                        r -> assertThat(r.scope()).as("%s %s", method, path).isEqualTo(expected));
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
                "/v1/history/frames",
                "/v1/timeline",
                "/v1/stats/timeseries",
                "/v1/stats/grid",
                "/v1/stats/top",
                "/v1/glance",
                "/v1/metrics",
                "/v1/records/usgs-earthquakes/us7000abcd/track",
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
                "/v1/briefings/9f1e/runs/latest",
                "/v1/digest",
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
        "POST, /v1/timeline",
        "GET, /v1/timelines",
        "POST, /v1/stats/rollup/rebuild",
        "POST, /v1/stats/timeseries",
        "POST, /v1/glance",
        "GET, /v1/glances",
        "PUT, /v1/metrics",
        "GET, /v1/metricsx",
        "GET, /v1/settings/metrics",
        "PUT, /v1/settings/metrics",
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

    @Test
    @DisplayName("057: HEAD takes the GET rule of its path; a HEAD on a path with no GET rule still needs admin")
    void headFollowsGet() {
        assertThat(requirement("HEAD", "/v1/tiles/quakes/3/2/1.mvt"))
                .isEqualTo(requirement("GET", "/v1/tiles/quakes/3/2/1.mvt"));
        assertThat(requirement("HEAD", "/v1/products/mrms-reflectivity/tiles/latest/3/2/1.png"))
                .isEqualTo(requirement("GET", "/v1/products/mrms-reflectivity/tiles/latest/3/2/1.png"));
        assertThat(requirement("HEAD", "/v1/system")).isEqualTo(requirement("GET", "/v1/system"));
        assertScope("HEAD", "/v1/no-such-path", Scope.ADMIN);
    }

    // ---------- ingest ----------

    @Test
    @DisplayName("ingest accepts an ingest key or an admin token")
    void ingestIsSpecialCased() {
        assertThat(requirement("POST", "/v1/ingest/custom-sdr"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
        assertThat(requirement("POST", "/v1/ingest/custom-sdr/alertmanager"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
        assertThat(requirement("OPTIONS", "/v1/ingest/custom-sdr/cloudevents"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
        assertScope("GET", "/v1/ingest/formats", Scope.ADMIN);
    }

    @Test
    @DisplayName("054: the remote SatDump pushes (media, rasters) take an ingest key or an admin token, like every push")
    void remoteSatDumpPushes() {
        assertThat(requirement("POST", "/v1/ingest/satdump-home/media"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
        assertThat(requirement("POST", "/v1/ingest/satdump-home/rasters"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
        assertThat(requirement("POST", "/v1/ingest/satdump-pi-at-the-antenna/rasters"))
                .isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
    }

    @Test
    @DisplayName("073: only GET and POST /v1/websub/*/* are open; other methods and depths stay admin (fail closed)")
    void webSubCallbacks() {
        assertOpen("GET", "/v1/websub/feed-nhc-atlantic/abcdefghijklmnop");
        assertOpen("POST", "/v1/websub/feed-nhc-atlantic/abcdefghijklmnop");
        assertScope("PUT", "/v1/websub/feed-nhc-atlantic/abcdefghijklmnop", Scope.ADMIN);
        assertScope("DELETE", "/v1/websub/feed-nhc-atlantic/abcdefghijklmnop", Scope.ADMIN);
        assertScope("GET", "/v1/websub/feed-nhc-atlantic", Scope.ADMIN);
        assertScope("GET", "/v1/websub/feed-nhc-atlantic/abcdefghijklmnop/extra", Scope.ADMIN);
    }

    @Test
    @DisplayName("055: GET /v1/maps/static is read (a place needs a token, checked by the controller); POST is admin")
    void staticMaps() {
        assertThat(requirement("GET", "/v1/maps/static")).isEqualTo(AccessRequirement.READ);
        assertScope("POST", "/v1/maps/static", Scope.ADMIN);
    }

    @Test
    @DisplayName("141: /fdsnws/** is read, /taxii2/** and /v1/export/stix are agent; other methods are admin, never open")
    void exchangeFormats() {
        assertThat(requirement("GET", "/fdsnws/event/1/query")).isEqualTo(AccessRequirement.READ);
        assertThat(requirement("GET", "/fdsnws/event/1/version")).isEqualTo(AccessRequirement.READ);
        assertThat(requirement("GET", "/fdsnws/event/1/application.wadl")).isEqualTo(AccessRequirement.READ);
        assertThat(requirement("HEAD", "/fdsnws/event/1/query")).isEqualTo(AccessRequirement.READ);
        assertScope("POST", "/fdsnws/event/1/query", Scope.ADMIN);
        assertScope("PUT", "/fdsnws/anything", Scope.ADMIN);

        assertThat(requirement("GET", "/taxii2/")).isEqualTo(AccessRequirement.AGENT);
        assertThat(requirement("GET", "/taxii2")).isEqualTo(AccessRequirement.AGENT);
        assertThat(requirement("GET", "/taxii2/foresignal/")).isEqualTo(AccessRequirement.AGENT);
        assertThat(requirement("GET", "/taxii2/foresignal/collections/")).isEqualTo(AccessRequirement.AGENT);
        assertThat(requirement("GET", "/taxii2/foresignal/collections/abc/objects/")).isEqualTo(AccessRequirement.AGENT);
        assertScope("POST", "/taxii2/foresignal/collections/abc/objects/", Scope.ADMIN);
        assertScope("DELETE", "/taxii2", Scope.ADMIN);

        assertThat(requirement("GET", "/v1/export/stix")).isEqualTo(AccessRequirement.AGENT);
        assertScope("POST", "/v1/export/stix", Scope.ADMIN);
    }

    @Test
    @DisplayName("the CelesTrak-compatible output outside /v1 is read, never open; other methods are admin (064)")
    void celestrakCompat() {
        assertThat(requirement("GET", "/compat/celestrak/gp.php")).isEqualTo(AccessRequirement.READ);
        assertThat(requirement("GET", "/compat/celestrak/NORAD/elements/gp.php")).isEqualTo(AccessRequirement.READ);
        assertThat(requirement("HEAD", "/compat/celestrak/gp.php")).isEqualTo(AccessRequirement.READ);
        assertScope("POST", "/compat/celestrak/gp.php", Scope.ADMIN);
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /compat/ntfy/custom-alerts",
        "PUT, /compat/ntfy/custom-alerts",
        "GET, /compat/ntfy/custom-alerts/publish",
        "POST, /compat/ntfy/custom-alerts/send",
        "POST, /compat/ntfy",
        "POST, /compat/gotify/message",
        "POST, /v1/external/sources/ext-hormuz/items",
        "PUT, /v1/external/sources/ext-hormuz/manifest"
    })
    @DisplayName("the compatibility paths outside /v1 and the EDP push paths take an ingest key (024)")
    void compatAndEdpPushTakeIngestKeys(String method, String path) {
        assertThat(requirement(method, path)).isInstanceOf(AccessRequirement.IngestKeyOrAdmin.class);
    }

    @Test
    @DisplayName("EDP: the protocol and self-registration are open, validation is agent, the rest admin (024)")
    void externalDataProtocol() {
        assertOpen("GET", "/v1/external/protocol");
        assertOpen("GET", "/v1/external/protocol/schemas/manifest");
        assertOpen("POST", "/v1/external/register");
        assertScope("POST", "/v1/external/validate", Scope.AGENT);
        assertScope("GET", "/v1/external/sources", Scope.ADMIN);
        assertScope("POST", "/v1/external/sources", Scope.ADMIN);
        assertScope("GET", "/v1/external/sources/ext-hormuz", Scope.ADMIN);
        assertScope("DELETE", "/v1/external/sources/ext-hormuz", Scope.ADMIN);
        assertScope("POST", "/v1/external/sources/ext-hormuz/approve", Scope.ADMIN);
        assertScope("POST", "/v1/external/sources/ext-hormuz/suspend", Scope.ADMIN);
        assertScope("POST", "/v1/external/sources/ext-hormuz/snapshots/s1/allow", Scope.ADMIN);
        assertScope("GET", "/v1/external/registration-tokens", Scope.ADMIN);
        assertScope("POST", "/v1/external/registration-tokens", Scope.ADMIN);
        assertScope("DELETE", "/v1/external/registration-tokens/01J0000000000000000000000A", Scope.ADMIN);
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

    // ---------- prompt 023 ----------

    @ParameterizedTest(name = "023: {0} {1} -> {2}")
    @CsvSource({
        "PUT, /v1/tokens/current/device, READ",
        "POST, /v1/stream-tickets, READ",
        "GET, /v1/tokens, ADMIN",
        "POST, /v1/tokens, ADMIN",
        "DELETE, /v1/tokens/9f1e, ADMIN",
        "PUT, /v1/tokens/9f1e/device, ADMIN",
        "GET, /v1/pairing-codes, ADMIN",
        "POST, /v1/pairing-codes, ADMIN",
        "DELETE, /v1/pairing-codes/9f1e, ADMIN",
        "GET, /v1/pairing-codes/9f1e/qr.svg, ADMIN",
        "GET, /v1/audit, ADMIN",
        "GET, /v1/settings/security, ADMIN",
        "PUT, /v1/settings/server, ADMIN",
    })
    @DisplayName("token, ticket, pairing and audit paths; the device rule precedes /v1/tokens/**")
    void promptTwentyThreeRules(String method, String path, Scope scope) {
        assertScope(method, path, scope);
    }

    @Test
    @DisplayName("claiming a pairing code is open: the code is the credential")
    void pairingClaimIsOpen() {
        assertOpen("POST", "/v1/pairing/claim");
        assertScope("GET", "/v1/pairing/claim", Scope.ADMIN);
    }

    @ParameterizedTest(name = "always authenticated: {0} {1}")
    @CsvSource({
        "POST, /v1/ask",
        "POST, /v1/ask/stream",
        "GET, /v1/ask/threads",
        "GET, /v1/ask/threads/9f1e",
        "GET, /v1/search",
        "POST, /v1/push/subscriptions",
        "DELETE, /v1/push/subscriptions/abc",
        "POST, /v1/stream-tickets",
        "PUT, /v1/tokens/current/device",
        "POST, /v1/search/reindex",
        "POST, /v1/briefings/preview",
        "POST, /v1/briefings/9f1e/run",
        "POST, /v1/llm/providers/ollama/test",
    })
    @DisplayName("cost-bearing operations need a token even under openReads (SEC-5)")
    void alwaysAuthenticatedRules(String method, String path) {
        assertThat(requirement(method, path).alwaysAuthenticated()).as("%s %s", method, path).isTrue();
    }

    @ParameterizedTest(name = "not always authenticated: {0} {1}")
    @CsvSource({
        "GET, /v1/records",
        "GET, /v1/search/status",
        "GET, /v1/changes/stream",
        "GET, /v1/tiles/earthquake/1/2/3.mvt",
        "GET, /v1/system",
    })
    void ordinaryReadsFollowOpenReads(String method, String path) {
        assertThat(requirement(method, path).alwaysAuthenticated()).as("%s %s", method, path).isFalse();
    }

    @Test
    @DisplayName("alwaysAuthenticated() lists exactly the flagged rules, for 025's MCP tools")
    void alwaysAuthenticatedListing() {
        assertThat(policy.alwaysAuthenticated())
                .contains(new AccessPolicy.EndpointRule(HttpMethod.POST, "/v1/ask"),
                        new AccessPolicy.EndpointRule(HttpMethod.GET, "/v1/search"),
                        new AccessPolicy.EndpointRule(HttpMethod.POST, "/v1/stream-tickets"))
                .doesNotContain(new AccessPolicy.EndpointRule(HttpMethod.GET, "/v1/records/**"));
        assertThat(policy.alwaysAuthenticated()).allSatisfy(rule -> assertThat(
                policy.requirementFor(rule.method() == null ? HttpMethod.GET : rule.method(),
                        rule.pattern().replace("**", "x").replace("*", "x")).alwaysAuthenticated()).isTrue());
    }

    @ParameterizedTest
    @CsvSource({
        "POST, /mcp",
        "GET, /mcp",
        "DELETE, /mcp",
        "GET, /mcp/sse",
        "POST, /mcp/message",
    })
    @DisplayName("the MCP server paths need read, with per-tool scope checks inside (025)")
    void mcpPathsNeedRead(String method, String path) {
        assertScope(method, path, Scope.READ);
        assertThat(requirement(method, path).alwaysAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("TokenFilter recognizes the MCP paths whose calls are audited per tool (025)")
    void mcpPathsRecognized() {
        assertThat(TokenFilter.isMcp("/mcp")).isTrue();
        assertThat(TokenFilter.isMcp("/mcp/message")).isTrue();
        assertThat(TokenFilter.isMcp("/mcpx")).isFalse();
        assertThat(TokenFilter.isMcp("/v1/records")).isFalse();
    }

    @Test
    @DisplayName("the requirement records and their flag compare by value")
    void requirementEquality() {
        assertThat(new AccessRequirement.RequiresScope(Scope.READ)).isEqualTo(AccessRequirement.READ);
        assertThat(AccessRequirement.READ_ALWAYS_AUTHENTICATED).isNotEqualTo(AccessRequirement.READ);
        assertThat(AccessRequirement.OPEN.alwaysAuthenticated()).isFalse();
        assertThat(AccessRequirement.INGEST_KEY_OR_ADMIN.alwaysAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("039: GET pushes and key-in-path ingest take an ingest key; export, purge and devices are admin")
    void privateSourcesAndPersonalLocation() {
        assertThat(requirement("GET", "/v1/ingest/custom-x/osmand")).isEqualTo(AccessRequirement.INGEST_KEY_OR_ADMIN);
        assertThat(requirement("GET", "/v1/ingest/custom-x/k/eid_abc/osmand"))
                .isEqualTo(AccessRequirement.INGEST_KEY_OR_ADMIN);
        assertThat(requirement("POST", "/v1/ingest/custom-x/k/eid_abc/osmand"))
                .isEqualTo(AccessRequirement.INGEST_KEY_OR_ADMIN);
        assertScope("GET", "/v1/ingest/formats", Scope.ADMIN);
        assertScope("GET", "/v1/records/s/e/export", Scope.ADMIN);
        assertScope("POST", "/v1/records/s/e/purge", Scope.ADMIN);
        assertScope("GET", "/v1/records/s/e", Scope.READ);
        assertScope("GET", "/v1/custom-sources/custom-x/devices", Scope.ADMIN);
        assertScope("POST", "/v1/custom-sources/custom-x/devices", Scope.ADMIN);
        assertScope("DELETE", "/v1/custom-sources/custom-x/devices/0b6f2c1e-0000-4000-8000-000000000001", Scope.ADMIN);
        assertScope("POST", "/v1/custom-sources/custom-x/devices/0b6f2c1e-0000-4000-8000-000000000001/key/rotate",
                Scope.ADMIN);
        assertScope("POST", "/v1/custom-sources/custom-x/devices/0b6f2c1e-0000-4000-8000-000000000001/purge",
                Scope.ADMIN);
    }

    @Test
    @DisplayName("141: the RFC 9728 metadata is open, the mcp.auth settings are admin, /mcp stays read")
    void mcpRemoteAuthorization() {
        assertOpen("GET", "/.well-known/oauth-protected-resource");
        assertOpen("GET", "/.well-known/oauth-protected-resource/mcp");
        assertOpen("HEAD", "/.well-known/oauth-protected-resource/mcp");
        assertScope("GET", "/v1/settings/mcp-auth", Scope.ADMIN);
        assertScope("PUT", "/v1/settings/mcp-auth", Scope.ADMIN);
        assertScope("POST", "/mcp", Scope.READ);
    }
}
