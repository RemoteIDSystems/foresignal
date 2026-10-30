package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.security.user.UserStatus;
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
 * Writable edition ownership over HTTP (prompt NP-008): a caller holding {@code users.manage} may set
 * {@code ownerUserId} on {@code createBriefing}/{@code updateBriefing} to stand an edition up for a user, or to
 * reassign one; a caller without it cannot, and an unknown or inactive target is {@code 400}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class EditionAssignmentIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    BriefingHttp http;
    UserEntity operator;
    UserEntity editor;
    UserEntity target;
    UserEntity target2;
    String operatorSession;
    String editorSession;
    String targetSession;
    String target2Session;

    @BeforeEach
    void setUpUsers() throws Exception {
        http = new BriefingHttp(mvc);
        operator = TestUsers.active(principals, users, userRoles, hasher, "operator-assign@example.com",
                BuiltInRole.ADMIN);
        editor = TestUsers.active(principals, users, userRoles, hasher, "editor-assign@example.com", BuiltInRole.EDITOR);
        target = TestUsers.active(principals, users, userRoles, hasher, "target-assign@example.com", BuiltInRole.EDITOR);
        target2 = TestUsers.active(principals, users, userRoles, hasher, "target2-assign@example.com",
                BuiltInRole.EDITOR);
        operatorSession = login("operator-assign@example.com");
        editorSession = login("editor-assign@example.com");
        targetSession = login("target-assign@example.com");
        target2Session = login("target2-assign@example.com");
    }

    private String login(String email) throws Exception {
        return "Bearer " + http.call("POST", "/v1/auth/login", null, TestUsers.loginBody(email), 200).get("token")
                .asString();
    }

    private static String owned(String name, UUID ownerUserId) {
        return digestBody(name, "0 7 * * *", "\"ownerUserId\":\"" + ownerUserId + "\"");
    }

    private static List<String> ids(JsonNode items, String field) {
        List<String> out = new ArrayList<>();
        items.forEach(i -> out.add(i.get(field).asString()));
        return out;
    }

    @Test
    @DisplayName("a users.manage operator assigns ownerUserId on create; the target owns the edition")
    void operatorAssignsOnCreate() throws Exception {
        JsonNode created = http.call("POST", "/v1/briefings", operatorSession, owned("Stood up", target.getId()), 201);
        String id = created.get("id").asString();
        assertThat(created.get("ownerUserId").asString()).isEqualTo(target.getId().toString());

        assertThat(http.call("GET", "/v1/briefings/" + id, targetSession, null, 200).get("ownerUserId").asString())
                .isEqualTo(target.getId().toString());
        assertThat(ids(http.call("GET", "/v1/me/editions", targetSession, null, 200).get("items"), "briefingId"))
                .containsExactly(id);
        // Another user's edition is 404; the operator's assignment did not make it the editor's.
        http.call("GET", "/v1/briefings/" + id, editorSession, null, 404);
    }

    @Test
    @DisplayName("a users.manage operator reassigns ownerUserId on update, moving the edition to another user")
    void operatorReassignsOnUpdate() throws Exception {
        String id = http.call("POST", "/v1/briefings", operatorSession, owned("Moving", target.getId()), 201).get("id")
                .asString();
        JsonNode updated = http.call("PUT", "/v1/briefings/" + id, operatorSession, owned("Moved", target2.getId()),
                200);
        assertThat(updated.get("ownerUserId").asString()).isEqualTo(target2.getId().toString());

        http.call("GET", "/v1/briefings/" + id, target2Session, null, 200);
        http.call("GET", "/v1/briefings/" + id, targetSession, null, 404);
    }

    @Test
    @DisplayName("an update that omits ownerUserId leaves the edition's owner unchanged")
    void updateWithoutOwnerKeepsIt() throws Exception {
        String id = http.call("POST", "/v1/briefings", operatorSession, owned("Kept", target.getId()), 201).get("id")
                .asString();
        JsonNode updated = http.call("PUT", "/v1/briefings/" + id, operatorSession, digestBody("Kept again",
                "0 8 * * *", null), 200);
        assertThat(updated.get("ownerUserId").asString()).isEqualTo(target.getId().toString());
    }

    @Test
    @DisplayName("a caller without users.manage cannot assign: ownerUserId is forced to the creating user")
    void nonOperatorCannotAssign() throws Exception {
        JsonNode created = http.call("POST", "/v1/briefings", editorSession, owned("Mine after all", target.getId()),
                201);
        assertThat(created.get("ownerUserId").asString()).isEqualTo(editor.getId().toString());
    }

    @Test
    @DisplayName("assigning an unknown user is 400")
    void unknownTargetIs400() throws Exception {
        http.call("POST", "/v1/briefings", operatorSession, owned("Nobody", UUID.randomUUID()), 400);
    }

    @Test
    @DisplayName("assigning a suspended (not active) user is 400")
    void suspendedTargetIs400() throws Exception {
        UserEntity suspended = TestUsers.active(principals, users, userRoles, hasher, "suspended-assign@example.com",
                BuiltInRole.EDITOR);
        suspended.setStatus(UserStatus.SUSPENDED, Instant.now());
        users.saveAndFlush(suspended);
        http.call("POST", "/v1/briefings", operatorSession, owned("Blocked", suspended.getId()), 400);
    }
}
