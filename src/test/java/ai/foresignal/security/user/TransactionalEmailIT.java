package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.notify.TransactionalMail;
import ai.foresignal.notify.TransactionalMailTransport;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.SmtpSettings;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The SMTP settings and the account emails through the real API (prompt R2-007), with an intercepting
 * {@link TransactionalMailTransport}: nothing is really sent.
 */
@IntegrationTest
@Import(TransactionalEmailIT.Fakes.class)
class TransactionalEmailIT {

    /** Records every email; fails when {@link #failWith} is set. */
    static final class InterceptingTransport implements TransactionalMailTransport {
        final List<TransactionalMail> sent = new ArrayList<>();
        final List<String> passwords = new ArrayList<>();
        String failWith;

        @Override
        public void send(SmtpSettings settings, String password, TransactionalMail mail) throws Exception {
            if (failWith != null) {
                throw new IllegalStateException(failWith);
            }
            passwords.add(password);
            sent.add(mail);
        }
    }

    /** The intercepting transport in place of SMTP. */
    @TestConfiguration
    static class Fakes {
        @Bean
        @Primary
        InterceptingTransport interceptingTransport() {
            return new InterceptingTransport();
        }
    }

    private static final String SECRET = "smtp-secret-do-not-leak";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private TokenService tokens;
    @Autowired private InterceptingTransport transport;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;

    @BeforeEach
    void setUp() {
        transport.sent.clear();
        transport.passwords.clear();
        transport.failWith = null;
        adminToken = TestTokens.create(tokens, "smtp-it-admin", Scope.ADMIN).plaintext();
    }

    private MockHttpServletResponse admin(MockHttpServletRequestBuilder builder) throws Exception {
        builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        return mvc.perform(builder).andReturn().getResponse();
    }

    private JsonNode json(MockHttpServletRequestBuilder builder, String body) throws Exception {
        MockHttpServletResponse r = admin(builder.contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(r.getStatus()).as(r.getContentAsString()).isBetween(200, 299);
        return json.readTree(r.getContentAsString());
    }

    private void enableSmtp() throws Exception {
        json(put("/v1/settings/smtp"), "{\"enabled\":true,\"host\":\"smtp.example.org\",\"port\":587,"
                + "\"security\":\"starttls\",\"username\":\"fs\",\"password\":\"" + SECRET + "\"}");
    }

    @Test
    @DisplayName("the defaults: off, port 587, STARTTLS, from info@remoteidsystems.com, no secret")
    void defaults() throws Exception {
        JsonNode b = json.readTree(admin(get("/v1/settings/smtp")).getContentAsString());
        assertThat(b.get("enabled").asBoolean()).isFalse();
        assertThat(b.get("port").asInt()).isEqualTo(587);
        assertThat(b.get("security").asString()).isEqualTo("starttls");
        assertThat(b.get("fromAddress").asString()).isEqualTo("info@remoteidsystems.com");
        assertThat(b.get("secretsSet")).isEmpty();
        assertThat(b.has("password")).isFalse();
    }

    @Test
    @DisplayName("the password is stored encrypted, never returned, kept when omitted and cleared when empty")
    void passwordIsWriteOnly() throws Exception {
        enableSmtp();
        JsonNode got = json.readTree(admin(get("/v1/settings/smtp")).getContentAsString());
        assertThat(got.get("secretsSet")).hasSize(1);
        assertThat(got.toString()).doesNotContain(SECRET);
        assertThat(jdbc.queryForObject("select value::text from app_setting where key = 'smtp'", String.class))
                .doesNotContain(SECRET);

        json(put("/v1/settings/smtp"), "{\"enabled\":true,\"host\":\"smtp2.example.org\"}");
        assertThat(json.readTree(admin(get("/v1/settings/smtp")).getContentAsString()).get("secretsSet")).hasSize(1);
        json(post("/v1/settings/smtp/test"), "{\"to\":\"me@example.org\"}");
        assertThat(transport.passwords).containsExactly(SECRET);

        json(put("/v1/settings/smtp"), "{\"enabled\":true,\"host\":\"smtp2.example.org\",\"password\":\"\"}");
        assertThat(json.readTree(admin(get("/v1/settings/smtp")).getContentAsString()).get("secretsSet")).isEmpty();
    }

    @Test
    @DisplayName("invalid settings are refused: enabled without a host, a bad port, security or from-address")
    void validation() throws Exception {
        for (String body : new String[] {"{\"enabled\":true}", "{\"enabled\":false,\"port\":0}",
            "{\"enabled\":false,\"security\":\"ssl3\"}", "{\"enabled\":false,\"fromAddress\":\"nope\"}", "{}"}) {
            assertThat(admin(put("/v1/settings/smtp").contentType(MediaType.APPLICATION_JSON).content(body))
                    .getStatus()).as(body).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("testSmtpSettings reports ok, or the failure without the password")
    void testSend() throws Exception {
        JsonNode off = json(post("/v1/settings/smtp/test"), "{\"to\":\"me@example.org\"}");
        assertThat(off.get("ok").asBoolean()).isFalse();
        enableSmtp();
        JsonNode ok = json(post("/v1/settings/smtp/test"), "{\"to\":\"me@example.org\"}");
        assertThat(ok.get("ok").asBoolean()).isTrue();
        assertThat(ok.get("sentAt").isNull()).isFalse();
        assertThat(transport.sent.get(0).to()).isEqualTo("me@example.org");
        transport.failWith = "535 authentication failed";
        JsonNode failed = json(post("/v1/settings/smtp/test"), "{\"to\":\"me@example.org\"}");
        assertThat(failed.get("ok").asBoolean()).isFalse();
        assertThat(failed.get("message").asString()).contains("535").doesNotContain(SECRET);
    }

    @Test
    @DisplayName("with SMTP on, createUser emails the welcome with name, roles and the activation link")
    void welcomeSent() throws Exception {
        enableSmtp();
        JsonNode created = json(post("/v1/users"), "{\"email\":\"w@example.org\",\"fullName\":\"Wendy Welcome\","
                + "\"roles\":[\"" + BuiltInRole.EDITOR.id() + "\"]}");
        assertThat(created.get("activation").get("emailed").asBoolean()).isTrue();
        assertThat(created.get("activation").get("token").isNull()).isTrue();
        TransactionalMail mail = transport.sent.get(0);
        assertThat(mail.to()).isEqualTo("w@example.org");
        assertThat(mail.text()).contains("Wendy Welcome", "Editor", "foresignal://activate?u=", "&t=fsv_",
                "expires on");
        assertThat(mail.html()).contains("Wendy Welcome", "Editor");
    }

    @Test
    @DisplayName("with SMTP off, createUser succeeds, returns the link and /v1/system warns SMTP_NOT_CONFIGURED")
    void degradesWithoutSmtp() throws Exception {
        JsonNode created = json(post("/v1/users"), "{\"email\":\"n@example.org\",\"fullName\":\"No Mail\","
                + "\"roles\":[]}");
        assertThat(created.get("activation").get("emailed").asBoolean()).isFalse();
        assertThat(created.get("activation").get("token").asString()).startsWith("fsv_");
        assertThat(transport.sent).isEmpty();
        JsonNode system = json.readTree(admin(get("/v1/system")).getContentAsString());
        assertThat(system.get("warnings").findValuesAsString("code")).contains(TransactionalEmailService.NOT_CONFIGURED);
    }

    @Test
    @DisplayName("a failing SMTP server never fails the caller: the link comes back instead")
    void failingSmtp() throws Exception {
        enableSmtp();
        transport.failWith = "connection refused";
        JsonNode created = json(post("/v1/users"), "{\"email\":\"f@example.org\",\"fullName\":\"F\",\"roles\":[]}");
        assertThat(created.get("activation").get("emailed").asBoolean()).isFalse();
        assertThat(created.get("activation").get("token").asString()).startsWith("fsv_");
    }

    @Test
    @DisplayName("suspend, role change and admin reset send their emails")
    void lifecycleEmails() throws Exception {
        enableSmtp();
        String id = json(post("/v1/users"), "{\"email\":\"l@example.org\",\"fullName\":\"Lee\",\"roles\":[]}")
                .get("id").asString();
        jdbc.update("update \"user\" set status = 'active' where id = ?::uuid", id);
        transport.sent.clear();
        json(post("/v1/users/" + id + "/roles"), "{\"roles\":[\"" + BuiltInRole.VIEWER.id() + "\"]}");
        json(post("/v1/users/" + id + "/suspend"), "{}");
        admin(post("/v1/users/" + id + "/unsuspend"));
        JsonNode reset = json(post("/v1/users/" + id + "/password-reset"), "{}");
        assertThat(reset.get("emailed").asBoolean()).isTrue();
        assertThat(transport.sent).extracting(TransactionalMail::subject).containsExactly(
                "Your Foresignal roles changed", "Your Foresignal account was suspended",
                "Reset your Foresignal password");
        assertThat(transport.sent.get(0).text()).contains("Viewer");
        assertThat(transport.sent.get(2).text()).contains("foresignal://reset-password?u=", "&t=fsw_",
                "If you did not request this");
    }
}
