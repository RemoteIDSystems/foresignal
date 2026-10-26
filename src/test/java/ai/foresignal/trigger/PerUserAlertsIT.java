package ai.foresignal.trigger;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserAdminService;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-user alerts (prompt R2-008) through the real filter chain: rule ownership, the per-user trigger API, one shared
 * record stream evaluated for every owner, alert visibility by owner and reassignment on user deletion.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class PerUserAlertsIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final String RULE = "{\"name\":\"%s\",\"sources\":[\"" + STANDARD + "\"],"
            + "\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private TriggerRuleService rules;
    @Autowired private TriggerRuleRepository repository;
    @Autowired private TriggerEngine engine;
    @Autowired private AlertStore alertStore;
    @Autowired private AlertBus bus;
    @Autowired private ChangeBus changeBus;
    @Autowired private TestRecords records;
    @Autowired private MutableClock clock;
    @Autowired private UserAdminService userAdmin;
    @Autowired private JdbcTemplate jdbc;

    private TriggerFixture fixture;
    private String adminToken;
    private UserEntity alice;
    private UserEntity bob;
    private String aliceSession;
    private String bobSession;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alertStore, bus);
        if (!engine.running()) {
            engine.start();
        }
        adminToken = TestTokens.create(tokens, "alerts-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        bob = TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    // ------------------------------------------------------------------------- helpers

    private MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder jsonBody(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    private String login(String email) throws Exception {
        MockHttpServletResponse r = mvc.perform(jsonBody(post("/v1/auth/login"), TestUsers.loginBody(email)))
                .andReturn().getResponse();
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    private UUID createRule(String bearer, String name) throws Exception {
        MockHttpServletResponse r = call(jsonBody(post("/v1/triggers"), RULE.formatted(name)), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(201);
        return UUID.fromString(body(r).get("id").asString());
    }

    private List<String> ruleNames(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/triggers"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<String> names = new ArrayList<>();
        body(r).forEach(rule -> names.add(rule.get("name").asString()));
        return names;
    }

    private List<Long> alertIds(String bearer) throws Exception {
        MockHttpServletResponse r = call(get("/v1/alerts"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        List<Long> ids = new ArrayList<>();
        body(r).get("items").forEach(a -> ids.add(a.get("id").asLong()));
        return ids;
    }

    private void quake(String id, double mag) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).put("mag", mag).build());
    }

    // ------------------------------------------------------------------------- the trigger API

    @Test
    @DisplayName("a session's new rule is owned by its user's principal; an API token's is shared")
    void ownerStamped() throws Exception {
        UUID mine = createRule(aliceSession, "Alice quakes");
        UUID shared = createRule(adminToken, "Shared quakes");
        assertThat(repository.findById(mine).orElseThrow().getOwnerPrincipalId()).isEqualTo(alice.getPrincipalId());
        assertThat(repository.findById(shared).orElseThrow().getOwnerPrincipalId()).isNull();
        JsonNode read = body(call(get("/v1/triggers/" + mine), aliceSession));
        assertThat(read.get("ownerPrincipalId").asString()).isEqualTo(alice.getPrincipalId());
    }

    @Test
    @DisplayName("a user lists only their own rules; an admin session and an API token list every rule")
    void listScoped() throws Exception {
        createRule(aliceSession, "Alice quakes");
        createRule(bobSession, "Bob quakes");
        createRule(adminToken, "Shared quakes");
        TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        assertThat(ruleNames(aliceSession)).containsExactly("Alice quakes");
        assertThat(ruleNames(bobSession)).containsExactly("Bob quakes");
        assertThat(ruleNames(login("admin@example.com")))
                .containsExactlyInAnyOrder("Alice quakes", "Bob quakes", "Shared quakes");
        assertThat(ruleNames(adminToken)).containsExactlyInAnyOrder("Alice quakes", "Bob quakes", "Shared quakes");
    }

    @Test
    @DisplayName("another user's rule, and a shared one, are 404 to a user on every per-rule operation")
    void crossUserNotFound() throws Exception {
        UUID alices = createRule(aliceSession, "Alice quakes");
        UUID shared = createRule(adminToken, "Shared quakes");
        for (UUID id : List.of(alices, shared)) {
            assertThat(call(get("/v1/triggers/" + id), bobSession).getStatus()).isEqualTo(404);
            assertThat(call(jsonBody(put("/v1/triggers/" + id), RULE.formatted("Taken")), bobSession).getStatus())
                    .isEqualTo(404);
            assertThat(call(jsonBody(post("/v1/triggers/" + id + "/snooze"), "{\"for\":\"1h\"}"), bobSession)
                    .getStatus()).isEqualTo(404);
            assertThat(call(post("/v1/triggers/" + id + "/test"), bobSession).getStatus()).isEqualTo(404);
            assertThat(call(get("/v1/triggers/" + id + "/tuning-log"), bobSession).getStatus()).isEqualTo(404);
            assertThat(call(delete("/v1/triggers/" + id), bobSession).getStatus()).isEqualTo(404);
            assertThat(repository.existsById(id)).isTrue();
        }
        assertThat(repository.findById(alices).orElseThrow().getName()).isEqualTo("Alice quakes");
        assertThat(call(delete("/v1/triggers/" + alices), aliceSession).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("an update keeps the owner")
    void updateKeepsOwner() throws Exception {
        UUID id = createRule(aliceSession, "Alice quakes");
        MockHttpServletResponse r = call(jsonBody(put("/v1/triggers/" + id), RULE.formatted("Renamed")), aliceSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(repository.findById(id).orElseThrow().getOwnerPrincipalId()).isEqualTo(alice.getPrincipalId());
    }

    @Test
    @DisplayName("a Viewer cannot author alerts: createTrigger needs self.alerts")
    void viewerRefused() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        MockHttpServletResponse r = call(jsonBody(post("/v1/triggers"), RULE.formatted("Nope")),
                login("viewer@example.com"));
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo("self.alerts");
    }

    // ------------------------------------------------------------------------- one stream, every owner

    @Test
    @DisplayName("one ingested record fires each owner's matching rule, attributed to that owner")
    void sharedStreamPerOwner() throws Exception {
        UUID alices = createRule(aliceSession, "Alice quakes");
        UUID bobs = createRule(bobSession, "Bob quakes");
        UUID shared = createRule(adminToken, "Shared quakes");
        quake("q1", 6.5);
        assertThat(fixture.alerts(alices)).singleElement()
                .satisfies(a -> assertThat(a.ownerPrincipalId()).isEqualTo(alice.getPrincipalId()));
        assertThat(fixture.alerts(bobs)).singleElement()
                .satisfies(a -> assertThat(a.ownerPrincipalId()).isEqualTo(bob.getPrincipalId()));
        assertThat(fixture.alerts(shared)).singleElement().satisfies(a -> assertThat(a.ownerPrincipalId()).isNull());
        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = 'q1'",
                Long.class, STANDARD)).as("the record is stored once for every owner").isEqualTo(1L);
    }

    @Test
    @DisplayName("a user sees their own and the shared alerts; another user's is 404 and left out")
    void alertVisibility() throws Exception {
        UUID alices = createRule(aliceSession, "Alice quakes");
        UUID bobs = createRule(bobSession, "Bob quakes");
        UUID shared = createRule(adminToken, "Shared quakes");
        quake("q1", 7.0);
        long aliceAlert = fixture.alerts(alices).getFirst().id();
        long bobAlert = fixture.alerts(bobs).getFirst().id();
        long sharedAlert = fixture.alerts(shared).getFirst().id();
        assertThat(alertIds(aliceSession)).contains(aliceAlert, sharedAlert).doesNotContain(bobAlert);
        assertThat(alertIds(adminToken)).contains(aliceAlert, bobAlert, sharedAlert);
        assertThat(call(get("/v1/alerts/" + bobAlert), aliceSession).getStatus()).isEqualTo(404);
        assertThat(call(post("/v1/alerts/" + bobAlert + "/ack"), aliceSession).getStatus()).isEqualTo(404);
        assertThat(call(post("/v1/alerts/" + aliceAlert + "/ack"), aliceSession).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("cooldown and anchor state stay per rule: one owner's cooldown never silences another's rule")
    void perRuleState() throws Exception {
        String cooled = "{\"name\":\"%s\",\"sources\":[\"" + STANDARD + "\"],\"cooldown\":\"10m\","
                + "\"cooldownScope\":\"rule\",\"conditions\":[{\"path\":\"mag\",\"op\":\">=\",\"value\":6}]}";
        MockHttpServletResponse a = call(jsonBody(post("/v1/triggers"), cooled.formatted("Alice")), aliceSession);
        MockHttpServletResponse b = call(jsonBody(post("/v1/triggers"), cooled.formatted("Bob")), bobSession);
        UUID alices = UUID.fromString(body(a).get("id").asString());
        UUID bobs = UUID.fromString(body(b).get("id").asString());
        quake("q1", 6.5);
        quake("q2", 6.6);
        assertThat(fixture.routed(alices)).hasSize(1);
        assertThat(fixture.routed(bobs)).hasSize(1);
    }

    // ------------------------------------------------------------------------- reassignment

    @Test
    @DisplayName("deleting a user moves their rules and alerts to the acting admin, and the rules keep firing")
    void reassignedOnDelete() throws Exception {
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com",
                BuiltInRole.ADMIN);
        UUID alices = createRule(aliceSession, "Alice quakes");
        quake("q1", 6.5);
        long before = fixture.alerts(alices).getFirst().id();
        UserAdminService.Deletion deletion = userAdmin.delete(alice.getId(), true, admin.getId(), "127.0.0.1");
        assertThat(deletion.counts()).containsEntry("triggerRules", 1);
        assertThat(repository.findById(alices).orElseThrow().getOwnerPrincipalId()).isEqualTo(admin.getPrincipalId());
        assertThat(alertStore.find(before).orElseThrow().ownerPrincipalId()).isEqualTo(admin.getPrincipalId());
        quake("q2", 6.9);
        assertThat(fixture.alerts(alices)).hasSize(2).last()
                .satisfies(x -> assertThat(x.ownerPrincipalId()).isEqualTo(admin.getPrincipalId()));
    }

    @Test
    @DisplayName("deleting a user with reassign=false deletes their rules and keeps their alerts")
    void deletedWithoutReassign() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "admin@example.com", BuiltInRole.ADMIN);
        UUID alices = createRule(aliceSession, "Alice quakes");
        quake("q1", 6.5);
        long alert = fixture.alerts(alices).getFirst().id();
        userAdmin.delete(alice.getId(), false, null, "127.0.0.1");
        assertThat(repository.existsById(alices)).isFalse();
        assertThat(alertStore.find(alert)).isPresent();
    }
}
