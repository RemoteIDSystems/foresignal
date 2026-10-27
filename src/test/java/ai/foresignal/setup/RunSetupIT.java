package ai.foresignal.setup;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.security.user.UserStatus;
import ai.foresignal.settings.ResourceThresholdsService;
import ai.foresignal.settings.SmtpSettingsService;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code runSetup} (prompt R2-027): the bootstrap token creates the first admin, seeds SMTP, sources and thresholds
 * and gets a session back; a second call is {@code 409 ALREADY_SETUP}; any other caller is refused; a bad body changes
 * nothing.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class RunSetupIT {

    private static final String PASSWORD = "a long and unusual setup passphrase";

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private TokenService tokens;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private SmtpSettingsService smtp;
    @Autowired private SourceService sources;
    @Autowired private ResourceThresholdsService thresholds;

    private String bootstrap;

    @BeforeEach
    void setUp() {
        bootstrap = adminTokens.rotate();
        sources.update(STANDARD, SourceSettingsUpdate.enabled(false));
    }

    private MockHttpServletResponse setup(String bearer, String body) throws Exception {
        var request = post("/v1/setup").contentType(MediaType.APPLICATION_JSON).content(body);
        if (bearer != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private static String admin(String email) {
        return "\"admin\":{\"email\":\"" + email + "\",\"fullName\":\"First Admin\",\"password\":\"" + PASSWORD
                + "\"}";
    }

    private String code(MockHttpServletResponse r) throws Exception {
        return json.readTree(r.getContentAsString()).get("code").asString();
    }

    @Test
    @DisplayName("the bootstrap token creates the first admin with SMTP, sources and thresholds, and a session")
    void bootstrapCreatesFirstAdmin() throws Exception {
        MockHttpServletResponse r = setup(bootstrap, "{" + admin("Root@Example.com") + ","
                + "\"smtp\":{\"enabled\":true,\"host\":\"smtp.example.org\",\"port\":587,\"security\":\"starttls\"},"
                + "\"sources\":[\"" + STANDARD + "\"],"
                + "\"thresholds\":{\"disk\":{\"warnFreePercent\":25}}}");

        assertThat(r.getStatus()).as(r.getContentAsString()).isEqualTo(200);
        JsonNode body = json.readTree(r.getContentAsString());
        assertThat(body.get("user").get("email").asString()).isEqualTo("root@example.com");
        assertThat(body.get("user").get("status").asString()).isEqualTo("active");
        assertThat(body.toString()).doesNotContain(PASSWORD);
        String session = body.get("session").get("token").asString();
        assertThat(session).isNotBlank();

        UserEntity user = users.findByEmail("root@example.com").orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(userRoles.findByUserId(user.getId())).extracting(link -> link.getRoleId())
                .containsExactly(BuiltInRole.ADMIN.id());
        assertThat(smtp.get().host()).isEqualTo("smtp.example.org");
        assertThat(sources.view(STANDARD).enabled()).isTrue();
        assertThat(thresholds.get().disk().warnFreePercent()).isEqualTo(25.0);

        MockHttpServletResponse me = mvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + session))
                .andReturn().getResponse();
        assertThat(me.getStatus()).as("the returned session signs the admin in").isEqualTo(200);
    }

    @Test
    @DisplayName("a second setup is 409 ALREADY_SETUP")
    void secondCallRefused() throws Exception {
        assertThat(setup(bootstrap, "{" + admin("one@example.com") + "}").getStatus()).isEqualTo(200);

        MockHttpServletResponse again = setup(bootstrap, "{" + admin("two@example.com") + "}");

        assertThat(again.getStatus()).isEqualTo(409);
        assertThat(code(again)).isEqualTo("ALREADY_SETUP");
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("only the bootstrap token may run setup: another admin token is 403, no token 401")
    void onlyBootstrap() throws Exception {
        String other = TestTokens.create(tokens, "setup-other-admin", Scope.ADMIN).plaintext();

        assertThat(setup(other, "{" + admin("x@example.com") + "}").getStatus()).isEqualTo(403);
        assertThat(setup(null, "{" + admin("x@example.com") + "}").getStatus()).isEqualTo(401);
        assertThat(users.count()).isZero();
    }

    @Test
    @DisplayName("a body refused anywhere changes nothing: weak password, unknown source, bad thresholds")
    void atomic() throws Exception {
        MockHttpServletResponse weak = setup(bootstrap,
                "{\"admin\":{\"email\":\"w@example.com\",\"fullName\":\"W\",\"password\":\"short\"}}");
        assertThat(weak.getStatus()).isEqualTo(400);

        assertThat(setup(bootstrap, "{" + admin("s@example.com") + ",\"sources\":[\"no-such-source\"]}").getStatus())
                .isEqualTo(400);
        assertThat(setup(bootstrap, "{" + admin("t@example.com") + ",\"thresholds\":{\"disk\":\"lots\"}}")
                .getStatus()).isEqualTo(400);
        assertThat(setup(bootstrap, "{" + admin("u@example.com") + ",\"nonsense\":true}").getStatus())
                .isEqualTo(400);
        assertThat(setup(bootstrap, "{\"smtp\":null}").getStatus()).as("admin is required").isEqualTo(400);

        assertThat(users.count()).isZero();
        assertThat(sources.view(STANDARD).enabled()).isFalse();
    }
}
