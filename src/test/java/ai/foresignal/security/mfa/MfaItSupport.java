package ai.foresignal.security.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.testsupport.security.TestUsers;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The shared fixture of the MFA integration tests (prompt R2-024): users, sign-in, enrollment and codes computed from
 * the secret the server handed out, through the real filter chain.
 */
abstract class MfaItSupport {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;
    @Autowired JdbcTemplate jdbc;
    @Autowired TotpService totp;
    @Autowired MfaService mfa;

    UserEntity user(String email, BuiltInRole... roles) {
        return TestUsers.active(principals, users, userRoles, hasher, email, roles);
    }

    MockHttpServletResponse call(MockHttpServletRequestBuilder builder, String bearer) throws Exception {
        if (bearer != null) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(builder).andReturn().getResponse();
    }

    MockHttpServletResponse postJson(String path, String body, String bearer) throws Exception {
        return call(post(path).contentType(MediaType.APPLICATION_JSON).content(body), bearer);
    }

    JsonNode body(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString());
    }

    /** Signs in with the password alone; the user has no MFA. */
    String login(String email) throws Exception {
        MockHttpServletResponse r = postJson("/v1/auth/login", TestUsers.loginBody(email), null);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("token").asString();
    }

    /** Signs in with the password and an MFA code. */
    MockHttpServletResponse login(String email, String code) throws Exception {
        return postJson("/v1/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + TestUsers.PASSWORD
                + "\",\"mfaCode\":\"" + code + "\"}", null);
    }

    /** Starts enrollment and returns the Base32 secret. */
    String enroll(String bearer) throws Exception {
        MockHttpServletResponse r = call(post("/v1/me/mfa/enroll"), bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        return body(r).get("secret").asString();
    }

    /** The code of the step {@code offset} steps from now. */
    String code(String secret, int offset) {
        return totp.code(TotpService.fromBase32(secret), TotpService.step(Instant.now()) + offset);
    }

    /** Enrolls and activates MFA; returns the secret and the first recovery codes. */
    Enrolled enrollAndVerify(String bearer) throws Exception {
        String secret = enroll(bearer);
        MockHttpServletResponse r = postJson("/v1/me/mfa/verify", "{\"code\":\"" + code(secret, 0) + "\"}", bearer);
        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode codes = body(r).get("recoveryCodes").get("codes");
        String[] out = new String[codes.size()];
        for (int i = 0; i < codes.size(); i++) {
            out[i] = codes.get(i).asString();
        }
        return new Enrolled(secret, out);
    }

    /** An activated enrollment: its secret and first recovery codes. */
    record Enrolled(String secret, String[] recoveryCodes) {
    }
}
