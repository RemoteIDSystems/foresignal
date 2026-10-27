package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/** Recovery codes (prompt R2-024): accepted once where a TOTP code is, then consumed; regenerating replaces the set. */
@IntegrationTest
class RecoveryCodeIT extends MfaItSupport {

    @Test
    @DisplayName("a recovery code signs in once, then is consumed")
    void recoveryCodeIsSingleUse() throws Exception {
        user("rc@example.com", BuiltInRole.EDITOR);
        Enrolled enrolled = enrollAndVerify(login("rc@example.com"));
        String recovery = enrolled.recoveryCodes()[0];

        MockHttpServletResponse first = login("rc@example.com", recovery.toUpperCase());
        assertThat(first.getStatus()).as(first.getContentAsString()).isEqualTo(200);
        assertThat(body(first).get("token").asString()).startsWith("fss_");

        MockHttpServletResponse second = login("rc@example.com", recovery);
        assertThat(second.getStatus()).isEqualTo(401);
        assertThat(body(second).get("code").asString()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(jdbc.queryForObject("select count(*) from user_recovery_code where used_at is not null",
                Long.class)).isEqualTo(1L);
    }

    @Test
    @DisplayName("regenerating invalidates the old set; the new codes work")
    void regenerateInvalidatesOldSet() throws Exception {
        user("regen@example.com", BuiltInRole.EDITOR);
        String bearer = login("regen@example.com");
        Enrolled enrolled = enrollAndVerify(bearer);

        MockHttpServletResponse r = call(get("/v1/me/mfa/recovery-codes"), bearer);

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode b = body(r);
        assertThat(b.get("codes").size()).isEqualTo(MfaService.RECOVERY_CODES);
        assertThat(b.get("generatedAt").isNull()).isFalse();
        assertThat(login("regen@example.com", enrolled.recoveryCodes()[1]).getStatus()).as("an old code").isEqualTo(401);
        assertThat(login("regen@example.com", b.get("codes").get(0).asString()).getStatus()).isEqualTo(200);
        assertThat(jdbc.queryForObject("select count(*) from user_recovery_code", Long.class))
                .isEqualTo((long) MfaService.RECOVERY_CODES);
    }

    @Test
    @DisplayName("a recovery code is accepted as a step-up too")
    void recoveryCodeStepsUp() throws Exception {
        user("stepup@example.com", BuiltInRole.EDITOR);
        String bearer = login("stepup@example.com");
        Enrolled enrolled = enrollAndVerify(bearer);

        MockHttpServletResponse r = postJson("/v1/me/mfa/verify", "{\"code\":\"" + enrolled.recoveryCodes()[2]
                + "\"}", bearer);

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(postJson("/v1/me/mfa/verify", "{\"code\":\"" + enrolled.recoveryCodes()[2] + "\"}", bearer)
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("recovery codes need MFA active; the password alone no longer signs in")
    void needsActiveMfa() throws Exception {
        user("none@example.com", BuiltInRole.VIEWER);
        String bearer = login("none@example.com");
        MockHttpServletResponse r = call(get("/v1/me/mfa/recovery-codes"), bearer);
        assertThat(r.getStatus()).isEqualTo(409);
        assertThat(body(r).get("code").asString()).isEqualTo("MFA_NOT_ENABLED");

        enrollAndVerify(bearer);
        MockHttpServletResponse passwordOnly = postJson("/v1/auth/login", TestUsers.loginBody("none@example.com"),
                null);
        assertThat(passwordOnly.getStatus()).isEqualTo(200);
        assertThat(body(passwordOnly).get("mfaRequired").asBoolean()).isTrue();
        assertThat(body(passwordOnly).get("token").asString()).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from user_session", Long.class)).as("only the first session")
                .isEqualTo(1L);
    }
}
