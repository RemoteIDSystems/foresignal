package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Enforcement (prompt R2-024), with {@code foresignal.mfa.enforce-enrollment} on as in a deployment: an Admin or
 * Operator without MFA may read and enroll but write nothing else; once enrolled, signing in needs a step-up code.
 */
@IntegrationTest
class MfaEnforcementIT extends MfaItSupport {

    /** A write the gate refuses before the operation runs: creating an egress route. */
    private static final String WRITE = "/v1/egress";

    @BeforeEach
    void enforce() {
        mfa.enforceEnrollment(true);
    }

    @AfterEach
    void relax() {
        mfa.enforceEnrollment(false);
    }

    private MockHttpServletResponse write(String bearer) throws Exception {
        return postJson(WRITE, "{}", bearer);
    }

    @Test
    @DisplayName("an Operator without MFA reads and enrolls, but every other write is refused until enrolled")
    void operatorGatedToEnrollment() throws Exception {
        user("gate@example.com", BuiltInRole.OPERATOR);
        String bearer = login("gate@example.com");

        assertThat(call(get("/v1/me"), bearer).getStatus()).isEqualTo(200);
        MockHttpServletResponse refused = write(bearer);
        assertThat(refused.getStatus()).isEqualTo(403);
        assertThat(body(refused).get("code").asString()).isEqualTo("MFA_ENROLLMENT_REQUIRED");
        assertThat(call(put("/v1/me/preferences").contentType(MediaType.APPLICATION_JSON).content("{}"), bearer)
                .getContentAsString()).as("preferences are not gated").doesNotContain("MFA_ENROLLMENT_REQUIRED");

        Enrolled enrolled = enrollAndVerify(bearer);

        MockHttpServletResponse after = write(bearer);
        assertThat(after.getContentAsString()).doesNotContain("MFA_ENROLLMENT_REQUIRED");
        assertThat(enrolled.recoveryCodes()).hasSize(MfaService.RECOVERY_CODES);
    }

    @Test
    @DisplayName("an Admin with MFA needs the step-up code to get a session")
    void loginNeedsStepUp() throws Exception {
        user("admin@example.com", BuiltInRole.ADMIN);
        Enrolled enrolled = enrollAndVerify(login("admin@example.com"));

        MockHttpServletResponse passwordOnly = postJson("/v1/auth/login", TestUsers.loginBody("admin@example.com"),
                null);
        assertThat(passwordOnly.getStatus()).isEqualTo(200);
        assertThat(body(passwordOnly).get("mfaRequired").asBoolean()).isTrue();
        assertThat(body(passwordOnly).get("token").asString()).isEmpty();

        String wrong = code(enrolled.secret(), 1).equals("000000") ? "111111" : "000000";
        assertThat(login("admin@example.com", wrong).getStatus()).isEqualTo(401);

        MockHttpServletResponse stepped = login("admin@example.com", code(enrolled.secret(), 1));
        assertThat(stepped.getStatus()).as(stepped.getContentAsString()).isEqualTo(200);
        String bearer = body(stepped).get("token").asString();
        assertThat(bearer).startsWith("fss_");
        assertThat(write(bearer).getContentAsString()).doesNotContain("MFA_ENROLLMENT_REQUIRED");
    }

    @Test
    @DisplayName("a role that does not require MFA is not gated, and MfaStatus says so")
    void editorNotGated() throws Exception {
        user("free@example.com", BuiltInRole.EDITOR);
        String bearer = login("free@example.com");

        MockHttpServletResponse r = write(bearer);
        assertThat(r.getContentAsString()).doesNotContain("MFA_ENROLLMENT_REQUIRED");
        String secret = enroll(bearer);
        MockHttpServletResponse verified = postJson("/v1/me/mfa/verify", "{\"code\":\"" + code(secret, 0) + "\"}",
                bearer);
        assertThat(body(verified).get("required").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("the MFA operations need a signed-in session")
    void needsSession() throws Exception {
        assertThat(call(post("/v1/me/mfa/enroll"), null).getStatus()).isEqualTo(401);
    }
}
