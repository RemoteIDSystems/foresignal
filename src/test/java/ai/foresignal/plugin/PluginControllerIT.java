package ai.foresignal.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * The four plugin operations end to end (prompt 141): {@code listPlugins}, {@code updatePluginSettings},
 * {@code getPlugin} and {@code updatePlugin}, admin only, over plugins copied into
 * {@code ${FORESIGNAL_DATA_DIR}/plugins/}: disabled by default, the review-and-enable flow with every refusal
 * ({@code PLUGINS_DISABLED}, missing or wrong {@code sha256}, {@code PLUGIN_HASH_MISMATCH}, a module that does not
 * load), the {@code hash-mismatch} state after the file changes, disabling, and the runtime honoring all of it.
 */
@IntegrationTest
class PluginControllerIT {

    static final String NAME = PluginTestFiles.PASSTHROUGH;

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired ForesignalProperties properties;
    @Autowired PluginRuntime runtime;

    String admin;
    Path root;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        root = properties.dataDir().resolve(PluginCatalog.FOLDER);
        PluginTestFiles.deleteTree(root);
        PluginTestFiles.install(root, NAME);
        PluginTestFiles.install(root, PluginTestFiles.WASI_IMPORT);
    }

    @AfterEach
    void tearDown() {
        PluginTestFiles.deleteTree(root);
    }

    ResultActions putJson(String path, String body) throws Exception {
        return mvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    ResultActions getAs(String path) throws Exception {
        return mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, admin));
    }

    String enableBody(String sha) {
        return "{\"enabled\":true,\"sha256\":\"" + sha + "\"}";
    }

    @Test
    @DisplayName("admin only: no token is 401, an agent token is 403 on all four operations")
    void adminOnly() throws Exception {
        String agent = TestTokens.bearer(TestTokens.create(tokens, "plugin-agent", Scope.AGENT));

        mvc.perform(get("/v1/plugins")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/plugins").header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/plugins/" + NAME).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isForbidden());
        mvc.perform(put("/v1/plugins").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}")).andExpect(status().isForbidden());
        mvc.perform(put("/v1/plugins/" + NAME).header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content(enableBody(PluginTestFiles.PASSTHROUGH_SHA256)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("disabled by default: the list says enabled=false and every plugin is disabled or in error")
    void disabledByDefault() throws Exception {
        getAs("/v1/plugins")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.plugins", hasSize(2)))
                .andExpect(jsonPath("$.plugins[0].name", is(NAME)))
                .andExpect(jsonPath("$.plugins[0].version", is("1.0.0")))
                .andExpect(jsonPath("$.plugins[0].abi", is(1)))
                .andExpect(jsonPath("$.plugins[0].kind", is("parser")))
                .andExpect(jsonPath("$.plugins[0].sha256", is(PluginTestFiles.PASSTHROUGH_SHA256)))
                .andExpect(jsonPath("$.plugins[0].actualSha256", is(PluginTestFiles.PASSTHROUGH_SHA256)))
                .andExpect(jsonPath("$.plugins[0].hashMatches", is(true)))
                .andExpect(jsonPath("$.plugins[0].enabled", is(false)))
                .andExpect(jsonPath("$.plugins[0].state", is("disabled")))
                .andExpect(jsonPath("$.plugins[0].error", nullValue()))
                .andExpect(jsonPath("$.plugins[0].sourceIds", hasSize(0)))
                .andExpect(jsonPath("$.plugins[0].loadedAt", nullValue()))
                .andExpect(jsonPath("$.plugins[1].name", is(PluginTestFiles.WASI_IMPORT)))
                .andExpect(jsonPath("$.plugins[1].state", is("error")))
                .andExpect(jsonPath("$.plugins[1].error", containsString("wasi_snapshot_preview1.fd_write")));
        assertThat(runtime.enabled()).isFalse();
    }

    @Test
    @DisplayName("enabling needs plugins on (409 PLUGINS_DISABLED); disabling works while they are off")
    void enableNeedsPluginsOn() throws Exception {
        putJson("/v1/plugins/" + NAME, enableBody(PluginTestFiles.PASSTHROUGH_SHA256))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("PLUGINS_DISABLED")));
        putJson("/v1/plugins/" + NAME, "{\"enabled\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.state", is("disabled")));
    }

    @Test
    @DisplayName("updatePluginSettings: on and off, and enabled is required")
    void settingsOnOff() throws Exception {
        putJson("/v1/plugins", "{\"enabled\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.plugins", hasSize(2)));
        assertThat(settings.get(SettingKeys.PLUGINS, PluginSettings.class).orElseThrow().on()).isTrue();
        assertThat(runtime.enabled()).isTrue();
        putJson("/v1/plugins", "{\"enabled\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));
        putJson("/v1/plugins", "{}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("review and enable: sha256 required, must match the file, then the plugin runs; disabling stops it")
    void reviewAndEnable() throws Exception {
        putJson("/v1/plugins", "{\"enabled\":true}").andExpect(status().isOk());

        putJson("/v1/plugins/" + NAME, "{\"enabled\":true}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")));
        putJson("/v1/plugins/" + NAME, enableBody("NOT-A-HASH")).andExpect(status().isBadRequest());
        putJson("/v1/plugins/" + NAME, "{}").andExpect(status().isBadRequest());
        putJson("/v1/plugins/" + NAME, enableBody("b".repeat(64)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("PLUGIN_HASH_MISMATCH")));

        putJson("/v1/plugins/" + NAME, enableBody(PluginTestFiles.PASSTHROUGH_SHA256))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.state", is("enabled")));
        getAs("/v1/plugins/" + NAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("enabled")));

        SourceDescriptor stations = SourceDescriptor.builder("wasm-it-stations").name("IT").kind("station").build();
        ParserOutput output = runtime.parse(NAME, PluginTestFiles.fixture(), stations);
        assertThat(output.records()).hasSize(3);
        getAs("/v1/plugins/" + NAME).andExpect(jsonPath("$.loadedAt", org.hamcrest.Matchers.notNullValue()));

        putJson("/v1/plugins/" + NAME, "{\"enabled\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.state", is("disabled")));
        assertThat(runtime.approvals().find(NAME)).isEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runtime.parse(NAME, PluginTestFiles.fixture(), stations))
                .isInstanceOfSatisfying(PluginException.class,
                        e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.NOT_ENABLED));
    }

    @Test
    @DisplayName("a file changed after review: state hash-mismatch, and it no longer runs")
    void changedAfterReview() throws Exception {
        putJson("/v1/plugins", "{\"enabled\":true}").andExpect(status().isOk());
        putJson("/v1/plugins/" + NAME, enableBody(PluginTestFiles.PASSTHROUGH_SHA256)).andExpect(status().isOk());

        byte[] other = PluginTestFiles.wasm(PluginTestFiles.LOG_SPAMMER);
        Files.write(root.resolve(NAME).resolve(PluginCatalog.MODULE), other);
        Files.writeString(root.resolve(NAME).resolve(PluginCatalog.MANIFEST),
                PluginTestFiles.manifest(NAME, PluginCatalog.sha256(other)));

        getAs("/v1/plugins/" + NAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("hash-mismatch")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.hashMatches", is(true)))
                .andExpect(jsonPath("$.error", containsString("approved " + PluginTestFiles.PASSTHROUGH_SHA256)));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runtime.host(NAME))
                .isInstanceOfSatisfying(PluginException.class,
                        e -> assertThat(e.reason()).isEqualTo(PluginException.Reason.HASH_MISMATCH));
    }

    @Test
    @DisplayName("a declared hash that is not the file's: listed as hash-mismatch and cannot be enabled")
    void declaredMismatch() throws Exception {
        PluginTestFiles.install(root, NAME, NAME, "c".repeat(64));
        putJson("/v1/plugins", "{\"enabled\":true}").andExpect(status().isOk());

        getAs("/v1/plugins/" + NAME)
                .andExpect(jsonPath("$.state", is("hash-mismatch")))
                .andExpect(jsonPath("$.hashMatches", is(false)))
                .andExpect(jsonPath("$.sha256", is("c".repeat(64))))
                .andExpect(jsonPath("$.actualSha256", is(PluginTestFiles.PASSTHROUGH_SHA256)));
        putJson("/v1/plugins/" + NAME, enableBody(PluginTestFiles.PASSTHROUGH_SHA256))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("PLUGIN_HASH_MISMATCH")))
                .andExpect(jsonPath("$.detail", containsString("plugin.json declares")));
    }

    @Test
    @DisplayName("a module that does not load cannot be enabled (400), whatever the hash")
    void invalidModule() throws Exception {
        putJson("/v1/plugins", "{\"enabled\":true}").andExpect(status().isOk());
        String sha = PluginCatalog.sha256(PluginTestFiles.wasm(PluginTestFiles.WASI_IMPORT));

        putJson("/v1/plugins/" + PluginTestFiles.WASI_IMPORT, enableBody(sha))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("not granted")));
    }

    @Test
    @DisplayName("unknown and invalid names are 404 UNKNOWN_PLUGIN")
    void unknown() throws Exception {
        getAs("/v1/plugins/absent")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("UNKNOWN_PLUGIN")));
        getAs("/v1/plugins/Bad_Name")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("UNKNOWN_PLUGIN")));
        putJson("/v1/plugins/absent", "{\"enabled\":false}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("UNKNOWN_PLUGIN")));
    }

    @Test
    @DisplayName("no plugin directory at all: an empty list")
    void emptyRoot() throws Exception {
        PluginTestFiles.deleteTree(root);
        getAs("/v1/plugins")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plugins", hasSize(0)));
    }
}
