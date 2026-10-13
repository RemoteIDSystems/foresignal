package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code GET /v1/transit/catalog} (prompt 070) over the test snapshot of {@link WeatherIngestSupport}'s context
 * ({@code mdb-9001} and {@code mdb-9003} enabled, {@code mdb-9005} refused by the URL policy): admin only, the
 * filters, pagination, the enabled flag and source ids, the 400s, and no POST.
 */
class TransitCatalogControllerIT extends WeatherIngestSupport {

    @Autowired private MockMvc mvc;
    @Autowired private AppSettingService settings;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private JsonMapper json;

    private String admin;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        admin = adminTokens.rotate();
    }

    @AfterEach
    void tearDown() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private MockHttpServletResponse call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode search(String query) throws Exception {
        MockHttpServletResponse response = call(get("/v1/transit/catalog" + query)
                .header("Authorization", "Bearer " + admin));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return json.readTree(response.getContentAsString());
    }

    private static List<String> ids(JsonNode page) {
        List<String> out = new ArrayList<>();
        page.get("items").forEach(item -> out.add(item.get("id").asString()));
        return out;
    }

    @Test
    @DisplayName("admin only: anonymous callers are refused even with open reads")
    void adminOnly() throws Exception {
        assertThat(call(get("/v1/transit/catalog")).getStatus()).isEqualTo(401);
        assertThat(call(get("/v1/transit/catalog").header("Authorization", "Bearer fs_not_a_token")).getStatus())
                .isEqualTo(401);
    }

    @Test
    @DisplayName("the filters: country, provider or id substring, entity type")
    void filters() throws Exception {
        assertThat(ids(search(""))).containsExactly("mdb-9001", "mdb-9002", "mdb-9003", "mdb-9004", "mdb-9005",
                "mdb-9006", "odpt-Some_City-AllLines-vp");
        assertThat(ids(search("?country=fr"))).containsExactly("mdb-9003");
        assertThat(ids(search("?q=example%20transit"))).containsExactly("mdb-9001", "mdb-9002");
        assertThat(ids(search("?entityType=sa"))).containsExactly("mdb-9002", "mdb-9003");
        assertThat(ids(search("?country=US&entityType=vp"))).containsExactly("mdb-9001");
    }

    @Test
    @DisplayName("the enabled flag and source ids; a URL refused by the policy is not enabled")
    void enabled() throws Exception {
        JsonNode page = search("");
        JsonNode first = page.get("items").get(0);
        assertThat(first.get("enabled").asBoolean()).isTrue();
        assertThat(first.get("sourceId").asString()).isEqualTo("transit-mdb-mdb-9001");
        assertThat(first.get("entityType").asString()).isEqualTo("vp");
        assertThat(first.get("directDownloadUrl").asString()).isEqualTo("https://feeds.example/vp.pb");
        assertThat(first.get("licenseUrl").asString()).isEqualTo("https://feeds.example/license");
        JsonNode combined = page.get("items").get(2);
        assertThat(combined.get("entityType").asString()).isEqualTo("sa|tu|vp");
        assertThat(combined.get("sourceIds").toString())
                .isEqualTo("[\"transit-mdb-mdb-9003\",\"transit-alerts-mdb-mdb-9003\"]");
        assertThat(page.get("items").get(1).get("enabled").asBoolean()).isFalse();
        assertThat(page.get("items").get(1).get("sourceId").isNull()).isTrue();
        assertThat(page.get("items").get(4).get("enabled").asBoolean()).as("mdb-9005 failed the URL policy").isFalse();
    }

    @Test
    @DisplayName("pagination by id with an opaque cursor")
    void pagination() throws Exception {
        JsonNode first = search("?limit=3");
        assertThat(ids(first)).containsExactly("mdb-9001", "mdb-9002", "mdb-9003");
        String cursor = first.get("nextCursor").asString();
        JsonNode second = search("?limit=3&cursor=" + cursor);
        assertThat(ids(second)).containsExactly("mdb-9004", "mdb-9005", "mdb-9006");
        JsonNode third = search("?limit=3&cursor=" + second.get("nextCursor").asString());
        assertThat(ids(third)).containsExactly("odpt-Some_City-AllLines-vp");
        assertThat(third.get("nextCursor").isNull()).isTrue();
    }

    @Test
    @DisplayName("bad parameters are 400s; there is no POST")
    void badRequests() throws Exception {
        for (String query : List.of("?country=USA", "?entityType=xx", "?limit=0", "?limit=501", "?cursor=nope")) {
            assertThat(call(get("/v1/transit/catalog" + query).header("Authorization", "Bearer " + admin)).getStatus())
                    .as(query).isEqualTo(400);
        }
        assertThat(call(post("/v1/transit/catalog").header("Authorization", "Bearer " + admin)
                .contentType("application/json").content("{\"url\":\"https://evil.example/feed.pb\"}")).getStatus())
                .isIn(404, 405);
    }
}
