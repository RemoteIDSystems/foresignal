package ai.foresignal.watch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Watchlists} operations over HTTP (prompt 036): CSV import in merge, replace and dry-run
 * modes with invalid rows, normalization and validation per identity, the export round trip, audit
 * rows for every edit, and the 100,000-item cap.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(PlaceTestSources.class)
class WatchlistsIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final MediaType CSV = MediaType.parseMediaType("text/csv");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired AppSettingService settings;
    @Autowired AuditService audit;
    @Autowired JdbcTemplate jdbc;

    String agent;
    String reader;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        adminTokens.rotate();
        agent = TestTokens.bearer(TestTokens.create(tokens, "agent", Scope.AGENT));
        reader = TestTokens.bearer(TestTokens.create(tokens, "reader", Scope.READ));
    }

    ResultActions send(MockHttpServletRequestBuilder request, String body) throws Exception {
        request = request.header(HttpHeaders.AUTHORIZATION, agent);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(request);
    }

    ResultActions csv(String id, String query, String body) throws Exception {
        return mvc.perform(post("/v1/watchlists/" + id + "/import" + query).header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(CSV).content(body));
    }

    JsonNode json(ResultActions result) throws Exception {
        return JSON.readTree(result.andReturn().getResponse().getContentAsString());
    }

    String list(String identity) throws Exception {
        return json(send(post("/v1/watchlists"), "{\"name\":\"" + identity + " list\",\"identity\":\"" + identity + "\"}")
                .andExpect(status().isCreated())).get("id").asString();
    }

    List<String> values(String id) throws Exception {
        List<String> out = new ArrayList<>();
        json(send(get("/v1/watchlists/" + id + "/items?limit=500"), null)).get("items")
                .forEach(i -> out.add(i.get("value").asString()));
        return out;
    }

    @Test
    @DisplayName("CSV import: dryRun reports adds and invalid rows and changes nothing; merge adds and updates; replace removes")
    void csvImport() throws Exception {
        String id = list("icao24");
        String body = "value,label,notes,tags\r\na1b2c3,N123AB,,survey;local\r\nZZZZZZ,bad,,\r\nadf7c8,N456CD,\"Medevac, county\",\r\n";

        JsonNode dry = json(csv(id, "?dryRun=true", body).andExpect(status().isOk()));
        assertThat(dry.get("dryRun").asBoolean()).isTrue();
        assertThat(dry.get("added").asInt()).isEqualTo(2);
        assertThat(dry.get("invalid")).hasSize(1);
        assertThat(dry.get("invalid").get(0).get("row").asInt()).isEqualTo(2);
        assertThat(dry.get("invalid").get(0).get("value").asString()).isEqualTo("ZZZZZZ");
        assertThat(dry.get("itemCount").asInt()).isEqualTo(2);
        assertThat(values(id)).isEmpty();

        JsonNode merged = json(csv(id, "?mode=merge", body).andExpect(status().isOk()));
        assertThat(merged.get("added").asInt()).isEqualTo(2);
        assertThat(values(id)).containsExactly("A1B2C3", "ADF7C8");
        send(get("/v1/watchlists/" + id), null).andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.normalize").value("upper"));
        JsonNode items = json(send(get("/v1/watchlists/" + id + "/items?q=medevac"), null));
        assertThat(items.get("items")).hasSize(1);
        assertThat(items.get("items").get(0).get("notes").asString()).isEqualTo("Medevac, county");
        assertThat(items.get("items").get(0).get("origin").asString()).isEqualTo("csv");

        JsonNode again = json(csv(id, "", "value,label\nA1B2C3,N999ZZ\nadf7c8,N456CD\n").andExpect(status().isOk()));
        assertThat(again.get("updated").asInt()).as("label changed on one; the other lost its notes").isEqualTo(2);

        JsonNode replaced = json(csv(id, "?mode=replace", "value\nA1B2C3\n").andExpect(status().isOk()));
        assertThat(replaced.get("removed").asInt()).isEqualTo(1);
        assertThat(values(id)).containsExactly("A1B2C3");

        mvc.perform(post("/v1/watchlists/" + id + "/import").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.TEXT_PLAIN).content("A1B2C3")).andExpect(status().isUnsupportedMediaType());
        csv(id, "", "label\nx\n").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON import and items: normalization and validation per identity (mmsi digits, imo check digit)")
    void normalization() throws Exception {
        String mmsi = list("mmsi");
        send(get("/v1/watchlists/" + mmsi), null).andExpect(jsonPath("$.normalize").value("digits"));
        JsonNode added = json(send(post("/v1/watchlists/" + mmsi + "/items"),
                "{\"items\":[{\"value\":\" 366 123 456 \"},{\"value\":\"12345\"}]}").andExpect(status().isOk()));
        assertThat(added.get("added").asInt()).isEqualTo(1);
        assertThat(added.get("invalid").get(0).get("reason").asString()).contains("nine digits");
        assertThat(values(mmsi)).containsExactly("366123456");

        String imo = list("imo");
        JsonNode imported = json(mvc.perform(post("/v1/watchlists/" + imo + "/import").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON).content("[{\"value\":\"IMO 9074729\",\"label\":\"Ever Given?\"},"
                        + "{\"value\":\"IMO 9074728\"}]")).andExpect(status().isOk()));
        assertThat(imported.get("added").asInt()).isEqualTo(1);
        assertThat(imported.get("invalid").get(0).get("reason").asString()).contains("check digit");
        assertThat(values(imo)).containsExactly("9074729");

        JsonNode removed = json(send(delete("/v1/watchlists/" + imo + "/items?value=IMO%209074729&value=1234567"), null)
                .andExpect(status().isOk()));
        assertThat(removed.get("removed").asInt()).isEqualTo(1);
        assertThat(removed.get("unchanged").asInt()).isEqualTo(1);

        send(put("/v1/watchlists/" + mmsi), "{\"name\":\"Vessels\",\"identity\":\"mmsi\",\"normalize\":\"none\"}")
                .andExpect(status().isBadRequest());
        send(put("/v1/watchlists/" + mmsi), "{\"name\":\"Vessels\",\"identity\":\"imo\"}").andExpect(status().isBadRequest());
        send(put("/v1/watchlists/" + mmsi), "{\"name\":\"Vessels\",\"identity\":\"mmsi\",\"description\":\"Ships\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Ships"));
        send(post("/v1/watchlists"), "{\"name\":\"x\",\"identity\":\"passport\"}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("export writes value,label,notes,tags and round-trips through import")
    void exportRoundTrip() throws Exception {
        String id = list("callsign");
        csv(id, "", "value,label,notes,tags\nual123,United,\"Says \"\"hi\"\", often\",a;b\ndal9,,,\n").andExpect(status().isOk());
        String exported = mvc.perform(get("/v1/watchlists/" + id + "/export").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk()).andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertThat(exported).startsWith("value,label,notes,tags\r\n").contains("\"Says \"\"hi\"\", often\"");

        String copy = list("callsign");
        JsonNode imported = json(csv(copy, "?mode=replace", exported).andExpect(status().isOk()));
        assertThat(imported.get("invalid")).isEmpty();
        assertThat(imported.get("added").asInt()).isEqualTo(2);
        String reexported = mvc.perform(get("/v1/watchlists/" + copy + "/export").header(HttpHeaders.AUTHORIZATION, agent))
                .andReturn().getResponse().getContentAsString();
        assertThat(reexported).isEqualTo(exported);
    }

    @Test
    @DisplayName("every edit is audited; reads need agent; unknown ids are UNKNOWN_WATCHLIST")
    void auditAndScopes() throws Exception {
        String id = list("icao24");
        send(post("/v1/watchlists/" + id + "/items"), "{\"items\":[{\"value\":\"abcdef\"}]}").andExpect(status().isOk());
        send(delete("/v1/watchlists/" + id + "/items?value=ABCDEF"), null).andExpect(status().isOk());
        audit.flush();
        Long edits = jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE target_type = 'watchlists'"
                + " AND method IN ('POST', 'DELETE', 'PUT')", Long.class);
        assertThat(edits).isGreaterThanOrEqualTo(3);

        mvc.perform(get("/v1/watchlists").header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/watchlists")).andExpect(status().isUnauthorized());
        send(get("/v1/watchlists/" + UUID.randomUUID()), null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_WATCHLIST"));
        send(get("/v1/watchlists/not-a-uuid"), null).andExpect(status().isNotFound());
        send(delete("/v1/watchlists/" + id), null).andExpect(status().isNoContent());
        send(get("/v1/watchlists/" + id), null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a list holds at most 100,000 items: a larger import is WATCHLIST_TOO_LARGE before anything is written")
    void sizeCap() throws Exception {
        String id = list("icao24");
        StringBuilder body = new StringBuilder("value\n");
        for (int i = 0; i <= WatchlistService.MAX_ITEMS; i++) {
            body.append(String.format("%06X", i)).append('\n');
        }
        csv(id, "?dryRun=true", body.toString()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WATCHLIST_TOO_LARGE"));
        assertThat(values(id)).isEmpty();
    }
}
