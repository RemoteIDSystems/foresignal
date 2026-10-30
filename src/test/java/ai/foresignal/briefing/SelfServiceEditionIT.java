package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.Permissions;
import ai.foresignal.security.user.RoleEntity;
import ai.foresignal.security.user.RolePermissionEntity;
import ai.foresignal.security.user.RolePermissionRepository;
import ai.foresignal.security.user.RoleRepository;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleEntity;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Self-service editions over HTTP (prompt NP-008): a signed-in user holding {@code self.editions} but no agent scope
 * creates, updates, runs and deletes their own edition through {@code /v1/me/editions}; {@code ownerUserId} is forced
 * to the caller; another user's edition and an API token are turned away.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class SelfServiceEditionIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository rolePermissions;
    @Autowired PasswordHasher hasher;

    BriefingHttp http;
    UserEntity self;
    UserEntity other;
    String selfSession;
    String otherSession;

    @BeforeEach
    void setUpUsers() throws Exception {
        http = new BriefingHttp(mvc);
        self = selfEditionsUser("self-edition@example.com");
        other = selfEditionsUser("other-edition@example.com");
        selfSession = login("self-edition@example.com");
        otherSession = login("other-edition@example.com");
    }

    /** An active user whose only role holds {@code self.editions} and {@code data.read}: a read-scope session. */
    private UserEntity selfEditionsUser(String email) {
        UserEntity user = TestUsers.active(principals, users, userRoles, hasher, email);
        Instant now = Instant.now();
        UUID roleId = UUID.randomUUID();
        roles.saveAndFlush(new RoleEntity(roleId, "Editions " + email, "self.editions only", false, now));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.SELF_EDITIONS.key()));
        rolePermissions.saveAndFlush(new RolePermissionEntity(roleId, Permissions.DATA_READ.key()));
        userRoles.saveAndFlush(new UserRoleEntity(user.getId(), roleId));
        return user;
    }

    private String login(String email) throws Exception {
        return "Bearer " + http.call("POST", "/v1/auth/login", null, TestUsers.loginBody(email), 200).get("token")
                .asString();
    }

    private static List<String> ids(JsonNode items, String field) {
        List<String> out = new ArrayList<>();
        items.forEach(i -> out.add(i.get(field).asString()));
        return out;
    }

    @Test
    @DisplayName("a self.editions user without agent scope self-serves through /v1/me/editions but not /v1/briefings")
    void worksWithoutAgentScope() throws Exception {
        // The direct briefing writes need agent scope, which this read-scope session lacks.
        http.call("POST", "/v1/briefings", selfSession, digestBody("Blocked", "0 7 * * *", null), 403);
        // The self-service create needs only the self.editions permission.
        JsonNode created = http.call("POST", "/v1/me/editions", selfSession, digestBody("Mine", "0 7 * * *", null), 201);
        assertThat(created.get("ownerUserId").asString()).isEqualTo(self.getId().toString());
        assertThat(ids(http.call("GET", "/v1/me/editions", selfSession, null, 200).get("items"), "briefingId"))
                .containsExactly(created.get("id").asString());
    }

    @Test
    @DisplayName("create, update, run and delete an owned edition through /v1/me/editions")
    void fullLifecycle() throws Exception {
        String id = http.call("POST", "/v1/me/editions", selfSession, digestBody("Daily", "0 7 * * *", null), 201)
                .get("id").asString();

        JsonNode updated = http.call("PUT", "/v1/me/editions/" + id, selfSession, digestBody("Renamed", "0 8 * * *",
                null), 200);
        assertThat(updated.get("name").asString()).isEqualTo("Renamed");
        assertThat(updated.get("ownerUserId").asString()).isEqualTo(self.getId().toString());

        quake("self-ed", "Tokyo", 5.4);
        JsonNode run = http.call("POST", "/v1/me/editions/" + id + "/run", selfSession, null, 202);
        assertThat(run.get("status").asString()).isEqualTo("succeeded");

        http.call("DELETE", "/v1/me/editions/" + id, selfSession, null, 204);
        assertThat(http.call("GET", "/v1/me/editions", selfSession, null, 200).get("items").size()).isZero();
    }

    @Test
    @DisplayName("ownerUserId in the body is ignored on self-service create: the owner is the caller")
    void ownerForcedToCaller() throws Exception {
        JsonNode created = http.call("POST", "/v1/me/editions", selfSession,
                digestBody("Still mine", "0 7 * * *", "\"ownerUserId\":\"" + other.getId() + "\""), 201);
        assertThat(created.get("ownerUserId").asString()).isEqualTo(self.getId().toString());
    }

    @Test
    @DisplayName("another user's edition is 404 to update, run and delete; a session is required")
    void isolatedAndSessionOnly() throws Exception {
        String mine = http.call("POST", "/v1/me/editions", selfSession, digestBody("Private", "0 7 * * *", null), 201)
                .get("id").asString();

        http.call("PUT", "/v1/me/editions/" + mine, otherSession, digestBody("Taken", "0 8 * * *", null), 404);
        http.call("POST", "/v1/me/editions/" + mine + "/run", otherSession, null, 404);
        http.call("DELETE", "/v1/me/editions/" + mine, otherSession, null, 404);
        assertThat(http.call("GET", "/v1/me/editions", otherSession, null, 200).get("items").size()).isZero();

        // An API token has no session, so every self-service operation is 401.
        http.call("POST", "/v1/me/editions", agent, digestBody("Tokened", "0 7 * * *", null), 401);
        http.call("PUT", "/v1/me/editions/" + mine, agent, digestBody("Tokened", "0 8 * * *", null), 401);
        http.call("POST", "/v1/me/editions/" + mine + "/run", agent, null, 401);
        http.call("DELETE", "/v1/me/editions/" + mine, agent, null, 401);
    }
}
