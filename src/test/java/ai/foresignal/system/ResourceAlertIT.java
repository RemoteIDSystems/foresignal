package ai.foresignal.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.notify.SystemEvent;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Host-resource alerting end to end (prompt R2-014): the resources and thresholds endpoints, a breach shown in
 * {@code /v1/system.warnings}, and one edge-triggered alert and one recovery through the channel system.
 *
 * <p>A real disk cannot be filled in a test, so the breach is made by the threshold: {@code criticalFreePercent: 100}
 * is critical for any volume that is not completely empty.
 */
@IntegrationTest
@RecordApplicationEvents
class ResourceAlertIT {

    static final Instant T0 = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private TokenService tokens;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private AppSettingService settings;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ResourceMonitor monitor;
    @Autowired private ResourceAlertEvaluator evaluator;
    @Autowired private OperationalAlertService alerts;
    @Autowired private ApplicationEvents events;

    private String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        alerts.reset();
        monitor.refresh();
        admin = TestTokens.create(tokens, "resources-it", Scope.ADMIN).plaintext();
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private MockHttpServletResponse thresholds(String body) throws Exception {
        return call(jsonBody(put("/v1/settings/thresholds"), body), admin);
    }

    private List<SystemEvent> published(String subtype) {
        return events.stream(SystemEvent.class).filter(e -> e.subtype().equals(subtype)).toList();
    }

    private String createChannel(String name, String bearer, String extra) throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/channels"), "{'name':'" + name + "','type':'slack',"
                + "'secrets':{'webhookUrl':'https://hooks.example.invalid/services/T/B/X'}" + extra + "}"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return body(r).get("id").asString();
    }

    // ------------------------------------------------------------------------- endpoints

    @Test
    @DisplayName("GET /v1/system/resources returns live disk, memory and database gauges with a status")
    void status() throws Exception {
        MockHttpServletResponse r = call(get("/v1/system/resources"), admin);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode status = body(r);
        assertThat(status.get("status").asString()).isIn("ok", "warn", "critical");
        assertThat(status.get("disk").get("totalBytes").asLong()).isPositive();
        assertThat(status.get("disk").get("freeBytes").asLong()).isNotNegative();
        assertThat(status.get("memory").get("totalBytes").asLong()).isPositive();
        assertThat(status.get("database").get("sizeBytes").asLong()).isPositive();
        assertThat(status.get("checkedAt").asString()).isNotBlank();
    }

    @Test
    @DisplayName("the defaults are disk 15 %/5 % and memory off; a PUT replaces them; a Viewer is refused")
    void thresholdsRoundTrip() throws Exception {
        JsonNode defaults = body(call(get("/v1/settings/thresholds"), admin));
        assertThat(defaults.get("disk").get("warnFreePercent").asDouble()).isEqualTo(15.0);
        assertThat(defaults.get("disk").get("criticalFreePercent").asDouble()).isEqualTo(5.0);
        assertThat(defaults.get("memory").get("warnFreePercent").isNull()).isTrue();
        assertThat(defaults.get("sourceStorm").get("failedSources").asInt()).isEqualTo(5);

        MockHttpServletResponse r = thresholds("{'disk':{'warnFreePercent':20,'criticalFreeBytes':1000},"
                + "'memory':{'warnFreePercent':10},'sourceStorm':null}");
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode stored = body(call(get("/v1/settings/thresholds"), admin));
        assertThat(stored.get("disk").get("warnFreePercent").asDouble()).isEqualTo(20.0);
        assertThat(stored.get("disk").get("criticalFreePercent").isNull()).isTrue();
        assertThat(stored.get("disk").get("criticalFreeBytes").asLong()).isEqualTo(1000);
        assertThat(stored.get("memory").get("warnFreePercent").asDouble()).isEqualTo(10.0);
        assertThat(stored.get("sourceStorm").get("failedSources").isNull()).isTrue();
        assertThat(stored.get("collectorBacklog").get("triggerLagSeq").asLong()).isEqualTo(10_000);

        assertThat(thresholds("{'disk':{'warnFreePercent':5,'criticalFreePercent':10}}").getStatus()).isEqualTo(400);
        MockHttpServletResponse unknown = thresholds("{'alertChannels':['" + UUID.randomUUID() + "','"
                + UUID.randomUUID() + "']}");
        assertThat(unknown.getStatus()).isEqualTo(400);
        assertThat(body(unknown).get("errors").size()).as("one 400 listing the ids").isEqualTo(1);

        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        MockHttpServletResponse login = mvc.perform(jsonBody(post("/v1/auth/login"),
                TestUsers.loginBody("viewer@example.com"))).andReturn().getResponse();
        String viewer = body(login).get("token").asString();
        MockHttpServletResponse refused = call(get("/v1/system/resources"), viewer);
        assertThat(refused.getStatus()).isEqualTo(403);
        assertThat(body(refused).get("requiredPermission").asString()).isEqualTo("storage.manage");
    }

    // ------------------------------------------------------------------------- breach and recovery

    @Test
    @DisplayName("entering critical fires one alert and one warning; no re-fire while breached; one recovery")
    void breachFiresOnce() throws Exception {
        assertThat(thresholds("{'disk':{'criticalFreePercent':100}}").getStatus()).isEqualTo(200);

        ResourceStatus breached = evaluator.evaluate();
        assertThat(breached.disk().status()).isEqualTo("critical");
        assertThat(breached.status()).isEqualTo("critical");
        assertThat(published("system.disk_critical")).hasSize(1);
        assertThat(published("system.disk_critical").get(0).severity()).isEqualTo("critical");

        JsonNode system = body(call(get("/v1/system"), admin));
        List<String> codes = new java.util.ArrayList<>();
        system.get("warnings").forEach(w -> codes.add(w.get("code").asString()));
        assertThat(codes).containsOnlyOnce("DISK_CRITICAL").doesNotContain("DISK_LOW");
        assertThat(system.get("status").asString()).isEqualTo("degraded");

        evaluator.evaluate();
        evaluator.evaluate();
        assertThat(published("system.disk_critical")).as("still breached: no re-fire").hasSize(1);

        assertThat(thresholds("{'disk':null}").getStatus()).isEqualTo(200);
        evaluator.evaluate();
        assertThat(published("system.disk_recovered")).hasSize(1);
        evaluator.evaluate();
        assertThat(published("system.disk_recovered")).hasSize(1);
        assertThat(published("system.disk_critical")).hasSize(1);
        assertThat(body(call(get("/v1/system/resources"), admin)).get("warnings").size()).isZero();
    }

    @Test
    @DisplayName("an alert goes to exactly the alertChannels; with none, to system subscribers and audit.read users")
    void routing() throws Exception {
        String chosen = createChannel("Ops pager", admin, "");
        String subscriber = createChannel("System feed", admin, ",'events':['system']");
        assertThat(thresholds("{'disk':{'criticalFreePercent':100},'alertChannels':['" + chosen + "']}").getStatus())
                .isEqualTo(200);
        evaluator.evaluate();
        SystemEvent event = published("system.disk_critical").get(0);
        assertThat(event.targets().subscribers()).isFalse();
        assertThat(event.targets().channels()).containsExactly(UUID.fromString(chosen));
        List<String> delivered = jdbc.queryForList("SELECT d.channel_id::text FROM notification_delivery d"
                + " JOIN notification n ON n.id = d.notification_id WHERE n.subtype = 'system.disk_critical'",
                String.class);
        assertThat(delivered).containsExactly(chosen).doesNotContain(subscriber);

        // With no alert channels: the system subscribers and the personal channels of users holding audit.read.
        TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        String session = body(mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody("admin@example.com")))
                .andReturn().getResponse()).get("token").asString();
        String personal = createChannel("My phone", session, ",'global':false");
        alerts.reset();
        assertThat(thresholds("{'disk':{'criticalFreePercent':100}}").getStatus()).isEqualTo(200);
        evaluator.evaluate();
        SystemEvent second = published("system.disk_critical").get(1);
        assertThat(second.targets().subscribers()).isTrue();
        assertThat(second.targets().channels()).contains(UUID.fromString(personal));
    }
}
