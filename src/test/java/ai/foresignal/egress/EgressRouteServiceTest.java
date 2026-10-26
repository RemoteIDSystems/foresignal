package ai.foresignal.egress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.test.IntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The egress pool's CRUD (prompt R2-018) through the API: create, read, list, replace and delete; secrets are
 * write-only (never in a response, only named in {@code secretsSet}) and sealed at rest; the type, URL, interface and
 * secret validation; a deleted route takes its pins and the default route with it.
 */
@IntegrationTest
@AutoConfigureMockMvc
class EgressRouteServiceTest extends EgressItSupport {

    private static final String PASSWORD = "correct-horse-battery-staple";

    @Autowired MockMvc mvc;
    @Autowired CredentialCipher cipher;
    @Autowired SourceRegistry registry;

    private ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, admin);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
        }
        return mvc.perform(request);
    }

    private String create(String body) throws Exception {
        String answer = send(post("/v1/egress"), body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(answer).doesNotContain(PASSWORD);
        return JSON.readTree(answer).get("id").asString();
    }

    @Test
    @DisplayName("a proxy route is created, read and listed; its secrets are named but never returned")
    void createReadList() throws Exception {
        String id = create("{'name':'Frankfurt exit','type':'socks5','url':'socks5://10.0.0.20:1080',"
                + "'secrets':{'username':'collector','password':'" + PASSWORD + "'}}");

        String one = send(get("/v1/egress/" + id), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Frankfurt exit"))
                .andExpect(jsonPath("$.type").value("socks5"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.url").value("socks5://10.0.0.20:1080"))
                .andExpect(jsonPath("$.secretsSet[0]").value("username"))
                .andExpect(jsonPath("$.secretsSet[1]").value("password"))
                .andExpect(jsonPath("$.secrets").doesNotExist())
                .andExpect(jsonPath("$.available").value(true))
                .andReturn().getResponse().getContentAsString();
        assertThat(one).doesNotContain(PASSWORD).doesNotContain("collector");

        String list = send(get("/v1/egress"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id))
                .andExpect(jsonPath("$.nextCursor").isEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(list).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("the secrets are sealed at rest: the row holds no plaintext and opens with the instance key")
    void secretsAreEncrypted() throws Exception {
        UUID id = UUID.fromString(create("{'name':'sealed','type':'http','url':'http://10.0.0.21:3128',"
                + "'secrets':{'username':'collector','password':'" + PASSWORD + "'}}"));

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT secrets_cipher, secrets_nonce FROM egress_route WHERE id = ?", id);
        byte[] sealed = (byte[]) row.get("secrets_cipher");
        assertThat(new String(sealed, StandardCharsets.ISO_8859_1)).doesNotContain(PASSWORD);
        assertThat(cipher.decryptToMap(new Encrypted(sealed, (byte[]) row.get("secrets_nonce"))))
                .containsEntry("username", "collector")
                .containsEntry("password", PASSWORD);
    }

    @Test
    @DisplayName("a replace keeps an omitted secret, clears an empty one, and drops what the new type cannot use")
    void replaceKeepsAndClearsSecrets() throws Exception {
        String id = create("{'name':'rotating','type':'http','url':'http://10.0.0.22:3128',"
                + "'secrets':{'username':'collector','password':'" + PASSWORD + "'}}");

        send(put("/v1/egress/" + id), "{'name':'rotating','type':'http','url':'http://10.0.0.22:3129'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://10.0.0.22:3129"))
                .andExpect(jsonPath("$.secretsSet.length()").value(2));

        send(put("/v1/egress/" + id), "{'name':'rotating','type':'http','url':'http://10.0.0.22:3129',"
                + "'secrets':{'username':''}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("secrets.password"));

        send(put("/v1/egress/" + id), "{'name':'rotating','type':'http','url':'http://10.0.0.22:3129',"
                + "'secrets':{'username':'','password':''}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretsSet.length()").value(0));

        send(put("/v1/egress/" + id), "{'name':'rotating','type':'interface','boundInterface':'eth1'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("interface"))
                .andExpect(jsonPath("$.url").isEmpty());
    }

    @Test
    @DisplayName("type, url, interface and secrets are validated, every problem in one 400")
    void validation() throws Exception {
        send(post("/v1/egress"), "{'type':'tor','url':'tor://x'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'type')]").exists());
        send(post("/v1/egress"), "{'name':'n','type':'http','url':'socks5://10.0.0.1:1080'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("url"));
        send(post("/v1/egress"), "{'name':'n','type':'http','url':'http://user:pw@10.0.0.1:3128'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("url"));
        send(post("/v1/egress"), "{'name':'n','type':'http','url':'http://10.0.0.1'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("url"));
        send(post("/v1/egress"), "{'name':'n','type':'http','url':'http://10.0.0.1:3128/path'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("url"));
        send(post("/v1/egress"), "{'name':'n','type':'socks5','url':'socks5://169.254.169.254:1080'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("URL_NOT_ALLOWED"));
        send(post("/v1/egress"), "{'name':'n','type':'interface'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("boundInterface"));
        send(post("/v1/egress"), "{'name':'n','type':'wireguard'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("secrets.wireguardConfig"));
        send(post("/v1/egress"), "{'name':'n','type':'interface','boundInterface':'eth1',"
                + "'secrets':{'username':'u'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("secrets.username"));

        create("{'name':'Only once','type':'interface','boundInterface':'eth1'}");
        send(post("/v1/egress"), "{'name':'only ONCE','type':'interface','boundInterface':'eth2'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
        assertThat(store.all()).hasSize(1);
    }

    @Test
    @DisplayName("a WireGuard route is recorded with its profile sealed, and marked unavailable")
    void wireguardIsRecordedButUnavailable() throws Exception {
        String id = create("{'name':'wg','type':'wireguard','secrets':{'wireguardConfig':"
                + "'[Interface]\\nPrivateKey = " + PASSWORD + "\\n'}}");
        String body = send(get("/v1/egress/" + id), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.unavailableReason").isNotEmpty())
                .andExpect(jsonPath("$.secretsSet[0]").value("wireguardConfig"))
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("delete is 204, then 404 UNKNOWN_EGRESS; it clears the pins and the default route naming it")
    void deleteClearsPinsAndDefault() throws Exception {
        String id = create("{'name':'gone','type':'interface','boundInterface':'eth1'}");
        String source = registry.ids().stream().sorted().findFirst().orElseThrow();
        send(put("/v1/sources/" + source + "/egress"), "{'routeId':'" + id + "'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value(id));
        send(put("/v1/settings/egress"), "{'rotation':'round-robin','defaultRoute':'" + id + "'}")
                .andExpect(status().isOk());

        send(delete("/v1/egress/" + id), null).andExpect(status().isNoContent());

        send(get("/v1/egress/" + id), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_EGRESS"));
        send(get("/v1/egress/not-a-uuid"), null).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM source_settings WHERE egress_route_id IS NOT NULL",
                Integer.class)).isZero();
        assertThat(egressSettings.get().defaultRoute()).isNull();
        assertThat(egressSettings.get().rotation()).isEqualTo("round-robin");
    }

    @Test
    @DisplayName("settings reject an unknown rotation or a default route that names nothing; assignment checks both ids")
    void settingsAndAssignmentValidation() throws Exception {
        send(get("/v1/settings/egress"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rotation").value("direct"))
                .andExpect(jsonPath("$.defaultRoute").isEmpty());
        send(put("/v1/settings/egress"), "{'rotation':'fastest'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("rotation"));
        send(put("/v1/settings/egress"), "{'rotation':'random','defaultRoute':'" + UUID.randomUUID() + "'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("defaultRoute"));
        send(put("/v1/sources/no-such-source/egress"), "{'excludeFromRotation':true}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_SOURCE"));
        String source = registry.ids().stream().sorted().findFirst().orElseThrow();
        send(put("/v1/sources/" + source + "/egress"), "{'routeId':'" + UUID.randomUUID() + "'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("routeId"));
    }
}
