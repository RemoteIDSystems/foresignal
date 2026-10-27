package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/** Disabling MFA (prompt R2-024): it needs a code or the password, and a role that requires MFA refuses it. */
@IntegrationTest
class MfaDisableIT extends MfaItSupport {

    @Test
    @DisplayName("disable needs a code or the correct password")
    void needsReauthentication() throws Exception {
        UserEntity u = user("dis@example.com", BuiltInRole.EDITOR);
        String bearer = login("dis@example.com");
        enrollAndVerify(bearer);

        assertThat(postJson("/v1/me/mfa/disable", "{}", bearer).getStatus()).isEqualTo(400);
        assertThat(postJson("/v1/me/mfa/disable", "{\"password\":\"not it\"}", bearer).getStatus()).isEqualTo(400);
        assertThat(postJson("/v1/me/mfa/disable", "{\"code\":\"000000x\"}", bearer).getStatus()).isEqualTo(400);
        assertThat(users.findById(u.getId()).orElseThrow().isMfaEnabled()).isTrue();

        MockHttpServletResponse r = postJson("/v1/me/mfa/disable", "{\"password\":\"" + TestUsers.PASSWORD + "\"}",
                bearer);

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(body(r).get("enabled").asBoolean()).isFalse();
        assertThat(jdbc.queryForObject("select mfa_secret_cipher is null from \"user\" where id = ?", Boolean.class,
                u.getId())).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from user_recovery_code where user_id = ?", Long.class,
                u.getId())).isZero();
        assertThat(postJson("/v1/auth/login", TestUsers.loginBody("dis@example.com"), null).getStatus())
                .isEqualTo(200);
    }

    @Test
    @DisplayName("a current TOTP code disables it too")
    void codeDisables() throws Exception {
        user("code@example.com", BuiltInRole.EDITOR);
        String bearer = login("code@example.com");
        Enrolled enrolled = enrollAndVerify(bearer);

        MockHttpServletResponse r = postJson("/v1/me/mfa/disable", "{\"code\":\"" + code(enrolled.secret(), 1)
                + "\"}", bearer);

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        assertThat(body(r).get("enabled").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("an Operator, whose role requires MFA, cannot disable it; an administrator reset can")
    void requiredRoleCannotSelfDisable() throws Exception {
        UserEntity u = user("op@example.com", BuiltInRole.OPERATOR);
        String bearer = login("op@example.com");
        enrollAndVerify(bearer);

        MockHttpServletResponse r = postJson("/v1/me/mfa/disable", "{\"password\":\"" + TestUsers.PASSWORD + "\"}",
                bearer);

        assertThat(r.getStatus()).isEqualTo(409);
        assertThat(body(r).get("code").asString()).isEqualTo("MFA_REQUIRED");
        assertThat(users.findById(u.getId()).orElseThrow().isMfaEnabled()).isTrue();

        assertThat(mfa.adminReset(u.getId())).isTrue();
        assertThat(users.findById(u.getId()).orElseThrow().isMfaEnabled()).isFalse();
    }
}
