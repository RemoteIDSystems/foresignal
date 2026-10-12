package ai.foresignal.inbound;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * ntfy- and Gotify-compatible publish (prompt 024): every ntfy shape (PUT, POST, GET/POST action
 * paths, JSON), {@code ?auth=}, Gotify with {@code X-Gotify-Key} and {@code ?token=}, response
 * shapes, external ids, the loop guard, and query secrets redacted in logs and {@code fetch_log}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@ExtendWith(OutputCaptureExtension.class)
class CompatPublishIT {

    static final Instant T = Instant.parse("2026-10-12T08:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstallIdService install;

    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        it = new InboundIt(mvc, "Bearer " + adminTokens.rotate());
    }

    InboundIt.Created source() throws Exception {
        return it.create(InboundIt.newId("notify"),
                "{'name':'Homelab','kinds':['notification'],'observedAtMode':'RECEIVED','recordTtl':'7d'}");
    }

    List<Map<String, Object>> heads(String sourceId) {
        return jdbc.queryForList("select external_id, payload::text as payload from record_head where source_id = ?"
                + " order by external_id", sourceId);
    }

    @Test
    @DisplayName("ntfy PUT and POST store the body as the message with title, priority and tags; tag id= sets the external id")
    void ntfyBody() throws Exception {
        InboundIt.Created source = source();
        MockHttpServletResponse posted = it.perform(post("/compat/ntfy/" + source.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + source.key())
                .header("X-Title", "Proxmox").header("X-Priority", "high").header("X-Tags", "backup,id=pve1")
                .contentType(MediaType.TEXT_PLAIN).content("Backup finished"));
        assertThat(posted.getStatus()).as(posted.getContentAsString()).isEqualTo(200);
        JsonNode message = body(posted);
        assertThat(message.get("event").asString()).isEqualTo("message");
        assertThat(message.get("topic").asString()).isEqualTo(source.id());
        assertThat(message.get("message").asString()).isEqualTo("Backup finished");
        assertThat(message.get("id").asString()).isEqualTo("pve1");
        assertThat(message.get("time").asLong()).isEqualTo(T.getEpochSecond());
        assertThat(message.get("priority").asInt()).isEqualTo(4);

        MockHttpServletResponse putted = it.perform(put("/compat/ntfy/" + source.id())
                .header("X-Foresignal-Ingest-Key", source.key()).header("Title", "NAS").header("p", "2")
                .contentType(MediaType.TEXT_PLAIN).content("Disk almost full"));
        assertThat(putted.getStatus()).isEqualTo(200);
        assertThat(body(putted).get("id").asString()).hasSize(26);

        List<Map<String, Object>> heads = heads(source.id());
        assertThat(heads).hasSize(2);
        assertThat(heads.get(0).get("external_id")).isNotNull();
        assertThat(heads.stream().map(h -> (String) h.get("payload")).toList())
                .anySatisfy(p -> assertThat(p).contains("\"title\": \"Proxmox\"", "\"priority\": 4", "backup"));
    }

    @Test
    @DisplayName("ntfy GET /publish with ?auth= and POST /send take the fields from the query; ?auth= is redacted")
    void ntfyQuery(CapturedOutput output) throws Exception {
        InboundIt.Created source = source();
        String auth = Base64.getEncoder().encodeToString(("Bearer " + source.key()).getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse viaGet = it.perform(get("/compat/ntfy/" + source.id() + "/publish")
                .queryParam("message", "Door opened").queryParam("title", "Garage").queryParam("priority", "5")
                .queryParam("auth", auth));
        assertThat(viaGet.getStatus()).as(viaGet.getContentAsString()).isEqualTo(200);
        assertThat(body(viaGet).get("title").asString()).isEqualTo("Garage");

        MockHttpServletResponse viaPost = it.perform(post("/compat/ntfy/" + source.id() + "/send")
                .header("X-Foresignal-Ingest-Key", source.key()).queryParam("message", "Motion"));
        assertThat(viaPost.getStatus()).isEqualTo(200);
        assertThat(it.perform(get("/compat/ntfy/" + source.id() + "/delete").queryParam("auth", auth)).getStatus())
                .isEqualTo(404);

        List<String> urls = jdbc.queryForList("select url_redacted from fetch_log where source_id = ?", String.class,
                source.id());
        assertThat(urls).anySatisfy(u -> assertThat(u).contains("auth=***"));
        assertThat(urls).noneSatisfy(u -> assertThat(u).contains(auth));
        assertThat(output.getAll()).doesNotContain(auth).doesNotContain(source.key());
    }

    @Test
    @DisplayName("ntfy JSON publish (Uptime Kuma) names the topic in the body; another source's topic is refused")
    void ntfyJson() throws Exception {
        InboundIt.Created source = source();
        MockHttpServletResponse response = it.perform(post("/compat/ntfy").header("X-Foresignal-Ingest-Key", source.key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"topic\":\"" + source.id() + "\",\"message\":\"NAS down\","
                        + "\"title\":\"Uptime Kuma\",\"priority\":4,\"tags\":[\"warning\"],\"click\":\"https://kuma.local\"}"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(body(response).get("click").asString()).isEqualTo("https://kuma.local");

        InboundIt.Created other = source();
        assertThat(it.perform(post("/compat/ntfy").header("X-Foresignal-Ingest-Key", source.key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"topic\":\"" + other.id() + "\",\"message\":\"x\"}"))
                .getStatus()).isEqualTo(401);
        assertThat(it.perform(post("/compat/ntfy").contentType(MediaType.APPLICATION_JSON)
                .content("{\"topic\":\"" + source.id() + "\",\"message\":\"x\"}")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Gotify: X-Gotify-Key and ?token= identify the source; extras set the external id; the reply is a Gotify message")
    void gotify() throws Exception {
        InboundIt.Created source = source();
        MockHttpServletResponse header = it.perform(post("/compat/gotify/message").header("X-Gotify-Key", source.key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"vzdump\",\"message\":\"Backup done\","
                        + "\"priority\":5,\"extras\":{\"foresignal::record\":{\"externalId\":\"pve-101\"}}}"));
        assertThat(header.getStatus()).as(header.getContentAsString()).isEqualTo(200);
        JsonNode message = body(header);
        assertThat(message.get("appid").asInt()).isEqualTo(1);
        assertThat(message.get("id").asLong()).isPositive();
        assertThat(message.get("message").asString()).isEqualTo("Backup done");

        MockHttpServletResponse form = it.perform(post("/compat/gotify/message").queryParam("token", source.key())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("title", "Cron").param("message", "Done")
                .param("priority", "2"));
        assertThat(form.getStatus()).as(form.getContentAsString()).isEqualTo(200);

        assertThat(heads(source.id()).stream().map(h -> h.get("external_id")).toList()).contains("pve-101");
        List<String> urls = jdbc.queryForList("select url_redacted from fetch_log where source_id = ?", String.class,
                source.id());
        assertThat(urls).anySatisfy(u -> assertThat(u).contains("token=***")).noneSatisfy(u -> assertThat(u).contains(source.key()));
        assertThat(jdbc.queryForList("select principal from fetch_log where source_id = ?", String.class, source.id()))
                .allSatisfy(p -> assertThat(p).startsWith("compat-gotify:"));
        assertThat(it.perform(post("/compat/gotify/message").header("X-Gotify-Key", "eik_wrong")
                .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"x\"}")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a message carrying this instance's ntfy origin tag or Gotify origin extra is acknowledged and dropped")
    void loopGuard() throws Exception {
        InboundIt.Created source = source();
        assertThat(it.perform(post("/compat/ntfy/" + source.id()).header("X-Foresignal-Ingest-Key", source.key())
                .header("X-Tags", "foresignal-origin-" + install.installId()).contentType(MediaType.TEXT_PLAIN)
                .content("echo")).getStatus()).isEqualTo(200);
        assertThat(it.perform(post("/compat/gotify/message").header("X-Gotify-Key", source.key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"echo\",\"extras\":{\"foresignal::origin\":"
                        + "{\"installId\":\"" + install.installId() + "\"}}}")).getStatus()).isEqualTo(200);
        assertThat(heads(source.id())).isEmpty();
    }
}
