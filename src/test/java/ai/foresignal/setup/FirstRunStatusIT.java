package ai.foresignal.setup;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.settings.SmtpSettingsService;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.system.ApiVersion;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestUsers;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code getFirstRunStatus} (prompt R2-027): open, {@code firstRun} until a user exists, SMTP and sources reflected. */
@IntegrationTest
@Import(TriggerTestSources.class)
class FirstRunStatusIT {

    @Autowired private MockMvc mvc;
    @Autowired private JsonMapper json;
    @Autowired private PrincipalService principals;
    @Autowired private UserRepository users;
    @Autowired private UserRoleRepository userRoles;
    @Autowired private PasswordHasher hasher;
    @Autowired private SmtpSettingsService smtp;
    @Autowired private SourceService sources;
    @Autowired private ApiVersion apiVersion;
    @Autowired private JdbcTemplate jdbc;

    private JsonNode status() throws Exception {
        MockHttpServletResponse r = mvc.perform(get("/v1/system/first-run")).andReturn().getResponse();
        assertThat(r.getStatus()).as("open, no credential").isEqualTo(200);
        return json.readTree(r.getContentAsString());
    }

    @Test
    @DisplayName("with no user the instance is in first run; after the first admin it is not")
    void firstRunUntilAUserExists() throws Exception {
        JsonNode before = status();
        assertThat(before.get("firstRun").asBoolean()).isTrue();
        assertThat(before.get("hasUsers").asBoolean()).isFalse();
        assertThat(before.get("apiVersion").asString()).isEqualTo(apiVersion.version());

        TestUsers.active(principals, users, userRoles, hasher, "first@example.com", BuiltInRole.ADMIN);

        JsonNode after = status();
        assertThat(after.get("firstRun").asBoolean()).isFalse();
        assertThat(after.get("hasUsers").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("smtpConfigured follows the account email settings")
    void smtpConfigured() throws Exception {
        assertThat(status().get("smtpConfigured").asBoolean()).isFalse();

        smtp.update(json.readTree("{\"enabled\":true,\"host\":\"smtp.example.org\",\"port\":587,"
                + "\"security\":\"starttls\"}"));

        assertThat(status().get("smtpConfigured").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("sourcesEnabled counts the collection set")
    void sourcesEnabled() throws Exception {
        sources.update(STANDARD, SourceSettingsUpdate.enabled(false));
        int before = status().get("sourcesEnabled").asInt();

        sources.update(STANDARD, SourceSettingsUpdate.enabled(true));

        JsonNode after = status();
        assertThat(after.get("sourcesEnabled").asInt()).isEqualTo(before + 1);
        assertThat(after.get("sourcesEnabled").asLong()).isEqualTo(
                jdbc.queryForObject("SELECT count(*) FROM source_settings WHERE enabled", Long.class));
    }
}
