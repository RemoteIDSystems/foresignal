package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

/** Enrollment → verify (prompt R2-024): the secret is sealed at rest, a wrong code activates nothing. */
@IntegrationTest
class MfaEnrollmentIT extends MfaItSupport {

    @Autowired TokenHasher tokenHasher;

    @Test
    @DisplayName("enroll returns a pending secret with its otpauth URI and QR; the secret is stored sealed")
    void enrollReturnsPendingSecret() throws Exception {
        UserEntity u = user("enroll@example.com", BuiltInRole.EDITOR);
        String bearer = login("enroll@example.com");

        MockHttpServletResponse r = call(post("/v1/me/mfa/enroll"), bearer);

        assertThat(r.getStatus()).isEqualTo(200);
        JsonNode b = body(r);
        String secret = b.get("secret").asString();
        assertThat(secret).matches("[A-Z2-7]{32}");
        assertThat(b.get("otpauthUri").asString()).isEqualTo("otpauth://totp/ForeSignal:enroll%40example.com?secret="
                + secret + "&issuer=ForeSignal");
        assertThat(b.get("qrSvg").asString()).contains("<svg");
        byte[] stored = jdbc.queryForObject("select mfa_secret_cipher from \"user\" where id = ?", byte[].class,
                u.getId());
        assertThat(stored).isNotNull().isNotEqualTo(TotpService.fromBase32(secret));
        assertThat(new String(stored, java.nio.charset.StandardCharsets.ISO_8859_1)).doesNotContain(secret);
        assertThat(jdbc.queryForObject("select mfa_enabled from \"user\" where id = ?", Boolean.class, u.getId()))
                .as("pending until verified").isFalse();
    }

    @Test
    @DisplayName("a wrong code does not activate MFA")
    void wrongCodeDoesNotActivate() throws Exception {
        UserEntity u = user("wrong@example.com", BuiltInRole.EDITOR);
        String bearer = login("wrong@example.com");
        String secret = enroll(bearer);
        String wrong = code(secret, 0).equals("000000") ? "111111" : "000000";

        MockHttpServletResponse r = postJson("/v1/me/mfa/verify", "{\"code\":\"" + wrong + "\"}", bearer);

        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(body(r).get("code").asString()).isEqualTo("MFA_CODE_INVALID");
        assertThat(users.findById(u.getId()).orElseThrow().isMfaEnabled()).isFalse();
        assertThat(postJson("/v1/me/mfa/verify", "{}", bearer).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("verify activates MFA and returns ten recovery codes once; they are stored hashed")
    void verifyActivates() throws Exception {
        UserEntity u = user("verify@example.com", BuiltInRole.EDITOR);
        String bearer = login("verify@example.com");
        String secret = enroll(bearer);

        MockHttpServletResponse r = postJson("/v1/me/mfa/verify", "{\"code\":\"" + code(secret, 0) + "\"}", bearer);

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode b = body(r);
        assertThat(b.get("enabled").asBoolean()).isTrue();
        assertThat(b.get("enrolledAt").isNull()).isFalse();
        assertThat(b.get("required").asBoolean()).isFalse();
        JsonNode codes = b.get("recoveryCodes").get("codes");
        assertThat(codes.size()).isEqualTo(MfaService.RECOVERY_CODES);
        assertThat(codes.get(0).asString()).matches("[a-z2-7]{5}-[a-z2-7]{5}");
        List<String> hashes = jdbc.queryForList("select code_hash from user_recovery_code where user_id = ?",
                String.class, u.getId());
        assertThat(hashes).hasSize(MfaService.RECOVERY_CODES)
                .contains(tokenHasher.hash(MfaService.normalizeRecoveryCode(codes.get(0).asString())))
                .doesNotContain(codes.get(0).asString());
        assertThat(users.findById(u.getId()).orElseThrow().isMfaEnabled()).isTrue();

        MockHttpServletResponse replay = postJson("/v1/me/mfa/verify", "{\"code\":\"" + code(secret, 0) + "\"}",
                bearer);
        assertThat(replay.getStatus()).as("the same code twice").isEqualTo(400);
        MockHttpServletResponse stepUp = postJson("/v1/me/mfa/verify", "{\"code\":\"" + code(secret, 1) + "\"}",
                bearer);
        assertThat(stepUp.getStatus()).as("a later step is a step-up").isEqualTo(200);
        assertThat(body(stepUp).has("recoveryCodes")).isFalse();
    }

    @Test
    @DisplayName("enrolling again while active is a conflict; verify with nothing pending is a conflict")
    void conflicts() throws Exception {
        user("again@example.com", BuiltInRole.VIEWER);
        String bearer = login("again@example.com");
        MockHttpServletResponse none = postJson("/v1/me/mfa/verify", "{\"code\":\"123456\"}", bearer);
        assertThat(none.getStatus()).isEqualTo(409);
        assertThat(body(none).get("code").asString()).isEqualTo("MFA_NOT_ENABLED");

        enrollAndVerify(bearer);

        MockHttpServletResponse again = call(post("/v1/me/mfa/enroll"), bearer);
        assertThat(again.getStatus()).isEqualTo(409);
        assertThat(body(again).get("code").asString()).isEqualTo("MFA_ALREADY_ENABLED");
    }
}
