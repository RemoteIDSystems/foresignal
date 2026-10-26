package ai.foresignal.source.view;

import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.ingest.IngestQueue;
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
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Per-user source views and the instance collection set through the real filter chain (prompt R2-010): the four
 * operations, their gates, view filtering of reads, and what deleting a user does to their view.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class SourceViewsIT {

    static final Instant T = Instant.parse("2026-10-26T12:00:00Z");

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private TokenService tokens;
    @Autowired private UserSourcePrefRepository prefs;
    @Autowired private UserAdminService userAdmin;
    @Autowired private IngestQueue queue;
    @Autowired private MutableClock clock;

    private String adminToken;
    private UserEntity alice;
    private String aliceSession;
    private String bobSession;
    private String operatorSession;

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(T);
        adminToken = TestTokens.create(tokens, "views-it-admin", Scope.ADMIN).plaintext();
        alice = TestUsers.active(principals, users, userRoles, hasher, "alice@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "bob@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "op@example.com", BuiltInRole.OPERATOR);
        aliceSession = login("alice@example.com");
        bobSession = login("bob@example.com");
        operatorSession = login("op@example.com");
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

    private static JsonNode entry(JsonNode set, String sourceId) {
        for (JsonNode item : set.get("sources")) {
            if (item.get("sourceId").asString().equals(sourceId)) {
                return item;
            }
        }
        throw new AssertionError(sourceId + " is not listed in " + set);
    }

    private JsonNode putView(String bearer, String sources) throws Exception {
        MockHttpServletResponse r = call(jsonBody(put("/v1/me/sources"), "{\"sources\":" + sources + "}"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r);
    }

    private void assertDenied(MockHttpServletResponse r, String permission) throws Exception {
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(403);
        assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.PERMISSION_DENIED);
        assertThat(body(r).get("requiredPermission").asString()).isEqualTo(permission);
    }

    // ------------------------------------------------------------------------- a user's view

    @Test
    @DisplayName("an untouched view lists every source, enabled, without overrides")
    void defaultView() throws Exception {
        MockHttpServletResponse r = call(get("/v1/me/sources"), aliceSession);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode a = entry(body(r), SCHEMA_A);
        assertThat(a.get("enabled").asBoolean()).isTrue();
        assertThat(a.get("displayName").isNull()).isTrue();
        assertThat(a.get("color").isNull()).isTrue();
    }

    @Test
    @DisplayName("updateMySources replaces the view and getMySources returns it; an empty list resets it")
    void roundTrip() throws Exception {
        JsonNode saved = putView(aliceSession,
                "[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":false,\"displayName\":\"Quakes\",\"color\":\"#112233\"}]");
        JsonNode a = entry(saved, SCHEMA_A);
        assertThat(a.get("enabled").asBoolean()).isFalse();
        assertThat(a.get("displayName").asString()).isEqualTo("Quakes");
        assertThat(a.get("color").asString()).isEqualTo("#112233");
        assertThat(entry(saved, SCHEMA_B).get("enabled").asBoolean()).isTrue();

        JsonNode read = body(call(get("/v1/me/sources"), aliceSession));
        assertThat(entry(read, SCHEMA_A).get("enabled").asBoolean()).isFalse();
        assertThat(entry(body(call(get("/v1/me/sources"), bobSession)), SCHEMA_A).get("enabled").asBoolean())
                .as("one user's view is not another's").isTrue();

        JsonNode reset = putView(aliceSession, "[]");
        assertThat(entry(reset, SCHEMA_A).get("enabled").asBoolean()).isTrue();
        assertThat(prefs.findByUserIdOrderBySourceId(alice.getId())).isEmpty();
    }

    @Test
    @DisplayName("an invalid view is 400 and stores nothing")
    void invalidView() throws Exception {
        String[] bodies = {
            "{}",
            "{\"sources\":[{\"sourceId\":\"no-such-source\",\"enabled\":true}]}",
            "{\"sources\":[{\"sourceId\":\"" + SCHEMA_A + "\"}]}",
            "{\"sources\":[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":true,\"color\":\"red\"}]}",
            "{\"sources\":[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":true},{\"sourceId\":\"" + SCHEMA_A
                    + "\",\"enabled\":false}]}",
        };
        for (String b : bodies) {
            MockHttpServletResponse r = call(jsonBody(put("/v1/me/sources"), b), aliceSession);
            assertThat(r.getStatus()).as(b + " -> " + r.getContentAsString()).isEqualTo(400);
            assertThat(body(r).get("code").asString()).isEqualTo(ErrorCodes.VALIDATION_FAILED);
        }
        assertThat(prefs.findByUserIdOrderBySourceId(alice.getId())).isEmpty();
    }

    @Test
    @DisplayName("a view belongs to a user: an API token gets 401 on the per-user operations")
    void tokenHasNoView() throws Exception {
        assertThat(call(get("/v1/me/sources"), adminToken).getStatus()).isEqualTo(401);
        assertThat(call(jsonBody(put("/v1/me/sources"), "{\"sources\":[]}"), adminToken).getStatus())
                .isEqualTo(401);
    }

    // ------------------------------------------------------------------------- gates

    @Test
    @DisplayName("self.sources gates the per-user operations and collection.manage the instance ones")
    void gates() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "viewer@example.com", BuiltInRole.VIEWER);
        String viewer = login("viewer@example.com");
        assertDenied(call(get("/v1/me/sources"), viewer), "self.sources");
        assertDenied(call(jsonBody(put("/v1/me/sources"), "{\"sources\":[]}"), viewer), "self.sources");
        assertDenied(call(get("/v1/sources/collection"), aliceSession), "collection.manage");
        assertDenied(call(jsonBody(put("/v1/sources/collection"), "{\"sources\":[]}"), aliceSession),
                "collection.manage");
        assertThat(call(get("/v1/sources/collection"), operatorSession).getStatus()).isEqualTo(200);
        assertThat(call(get("/v1/sources/collection"), adminToken).getStatus()).isEqualTo(200);
    }

    // ------------------------------------------------------------------------- the instance set

    @Test
    @DisplayName("updateCollectionSources sets inCollection; getCollectionSources counts the users who want each source")
    void collectionSet() throws Exception {
        MockHttpServletResponse on = call(jsonBody(put("/v1/sources/collection"),
                "{\"sources\":[{\"sourceId\":\"" + SCHEMA_A + "\",\"inCollection\":true},{\"sourceId\":\"" + SCHEMA_B
                        + "\",\"inCollection\":false,\"enabledByUsers\":99}]}"), operatorSession);
        assertThat(on.getStatus()).as(on.getContentAsString()).isEqualTo(200);
        assertThat(entry(body(on), SCHEMA_A).get("inCollection").asBoolean()).isTrue();
        assertThat(entry(body(on), SCHEMA_B).get("inCollection").asBoolean()).isFalse();
        assertThat(entry(body(on), SCHEMA_A).get("enabledByUsers").asLong()).as("three active users").isEqualTo(3);

        putView(aliceSession, "[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":false}]");
        JsonNode after = body(call(get("/v1/sources/collection"), adminToken));
        assertThat(entry(after, SCHEMA_A).get("enabledByUsers").asLong()).isEqualTo(2);
        assertThat(entry(after, SCHEMA_B).get("inCollection").asBoolean()).as("unchanged by a view").isFalse();
    }

    @Test
    @DisplayName("an invalid collection set is 400 and changes nothing")
    void invalidCollection() throws Exception {
        MockHttpServletResponse r = call(jsonBody(put("/v1/sources/collection"),
                "{\"sources\":[{\"sourceId\":\"" + SCHEMA_A + "\",\"inCollection\":true},"
                        + "{\"sourceId\":\"no-such-source\",\"inCollection\":true}]}"), adminToken);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(400);
        JsonNode set = body(call(get("/v1/sources/collection"), adminToken));
        assertThat(entry(set, SCHEMA_A).get("inCollection").asBoolean()).isFalse();
    }

    // ------------------------------------------------------------------------- view filtering

    @Test
    @DisplayName("a user's reads show only their view; another user and an admin token still see everything")
    void viewFiltering() throws Exception {
        TestRecords records = new TestRecords(queue, clock);
        records.ingestAt(SCHEMA_A, T, NormalizedRecord.builder("a1", SCHEMA_KIND).observedAt(T).point(1, 1).build());
        records.ingestAt(SCHEMA_B, T, NormalizedRecord.builder("b1", SCHEMA_KIND).observedAt(T).point(1, 1).build());
        putView(aliceSession, "[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":false}]");

        MockHttpServletResponse mine = call(get("/v1/records").param("order", "id"), aliceSession);
        assertThat(mine.getStatus()).as(mine.getContentAsString()).isEqualTo(200);
        assertThat(mine.getContentAsString()).contains("b1").doesNotContain("a1");
        assertThat(call(get("/v1/records").param("order", "id"), bobSession).getContentAsString())
                .contains("a1").contains("b1");
        assertThat(call(get("/v1/records").param("order", "id"), adminToken).getContentAsString())
                .contains("a1").contains("b1");
        assertThat(call(get("/v1/records").param("order", "id"), operatorSession).getContentAsString())
                .as("collection.manage reads the whole collection").contains("a1").contains("b1");
    }

    // ------------------------------------------------------------------------- deletion

    @Test
    @DisplayName("deleting a user removes their view, whether or not their configuration is reassigned")
    void deletion() throws Exception {
        UserEntity admin = TestUsers.active(principals, users, userRoles, hasher, "admin@example.com",
                BuiltInRole.ADMIN);
        putView(aliceSession, "[{\"sourceId\":\"" + SCHEMA_A + "\",\"enabled\":false}]");
        UserAdminService.Deletion deletion = userAdmin.delete(alice.getId(), true, admin.getId(), "127.0.0.1");
        assertThat(deletion.counts()).containsEntry("sourceViews", 1);
        assertThat(prefs.findByUserIdOrderBySourceId(alice.getId())).isEmpty();
        assertThat(prefs.findByUserIdOrderBySourceId(admin.getId())).as("not moved onto the admin").isEmpty();
    }
}
