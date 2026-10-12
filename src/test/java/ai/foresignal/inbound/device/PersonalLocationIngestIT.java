package ai.foresignal.inbound.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
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
 * The four tracker apps end to end with device keys (prompt 039): OwnTracks (Basic, reply
 * {@code []}), Traccar Client / OsmAnd (path key, GET query and JSON POST, empty 200), Overland (Bearer,
 * {@code {"result":"ok"}}) and GPSLogger (path key, GET, empty 200); the device binding, revocation,
 * the sender allowlist, {@code allowPathKey}, and that no key reaches the fetch log, the audit log or
 * the log output. Payloads follow each app's published format with synthetic positions.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@ExtendWith(OutputCaptureExtension.class)
class PersonalLocationIngestIT {

    static final Instant T = Instant.parse("2026-10-12T08:00:00Z");
    static final long TST = Instant.parse("2026-10-12T07:59:00Z").getEpochSecond();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired AuditService audit;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    InboundIt.Created source(String extra) throws Exception {
        return it.create(InboundIt.newId("loc"), "{'name':'Family','profile':'personal-location'" + extra + "}");
    }

    /** Creates a device and returns {@code [id, key]}. */
    String[] device(String sourceId, String externalId) throws Exception {
        MockHttpServletResponse response = it.perform(post("/v1/custom-sources/" + sourceId + "/devices")
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"externalId\":\"" + externalId + "\",\"name\":\"Phone\"}"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        JsonNode body = InboundIt.body(response);
        return new String[] {body.get("id").asString(), body.get("key").asString()};
    }

    List<String> entities(String sourceId) throws Exception {
        JsonNode listed = InboundIt.body(it.perform(get("/v1/records").param("source", sourceId)
                .header(HttpHeaders.AUTHORIZATION, admin)));
        return listed.findValuesAsString("externalId");
    }

    static String basic(String key) {
        return "Basic " + Base64.getEncoder().encodeToString(("device:" + key).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("OwnTracks HTTP mode with Basic auth gets [] and lands on the bound entity, whatever X-Limit-U/D say")
    void ownTracks() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "ana-phone");
        String body = "{\"_type\":\"location\",\"lat\":51.5007,\"lon\":-0.1246,\"tst\":" + TST
                + ",\"acc\":12,\"alt\":20,\"vel\":36,\"cog\":90,\"batt\":80,\"bs\":2,\"conn\":\"w\",\"tid\":\"AP\"}";
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id() + "/owntracks")
                .header(HttpHeaders.AUTHORIZATION, basic(dev[1])).header("X-Limit-U", "ana").header("X-Limit-D", "pixel")
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("[]");
        assertThat(entities(source.id())).containsExactly("ana-phone");
    }

    @Test
    @DisplayName("Traccar Client: GET query with the key in the path, and the JSON POST, answer an empty 200")
    void traccar() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "ben-phone");
        MockHttpServletResponse getResponse = it.perform(get("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/osmand")
                .queryParam("id", "ben-phone").queryParam("lat", "48.8584").queryParam("lon", "2.2945")
                .queryParam("timestamp", Long.toString(TST)).queryParam("speed", "10").queryParam("accuracy", "8")
                .queryParam("batt", "64"));
        assertThat(getResponse.getStatus()).as(getResponse.getContentAsString()).isEqualTo(200);
        assertThat(getResponse.getContentAsString()).isEmpty();

        String json = "{\"device_id\":\"ben-phone\",\"location\":{\"timestamp\":\"2026-10-12T07:59:30Z\","
                + "\"coords\":{\"latitude\":48.8590,\"longitude\":2.2950,\"speed\":3.2,\"heading\":180,"
                + "\"accuracy\":6,\"altitude\":35},\"battery\":{\"level\":0.63,\"is_charging\":false},"
                + "\"is_moving\":true}}";
        MockHttpServletResponse postResponse = it.perform(post("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/osmand")
                .contentType(MediaType.APPLICATION_JSON).content(json));
        assertThat(postResponse.getStatus()).as(postResponse.getContentAsString()).isEqualTo(200);
        assertThat(entities(source.id())).containsExactly("ben-phone");
    }

    @Test
    @DisplayName("Overland with a Bearer device key gets {\"result\":\"ok\"}")
    void overland() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "cleo-phone");
        String body = "{\"locations\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.4194,37.7749]},"
                + "\"properties\":{\"timestamp\":\"2026-10-12T07:59:00Z\",\"horizontal_accuracy\":5,\"altitude\":12,"
                + "\"speed\":1.2,\"course\":45,\"motion\":[\"walking\"],\"battery_level\":0.9,\"battery_state\":\"unplugged\","
                + "\"device_id\":\"cleo-phone\"}}]}";
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id() + "/overland")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + dev[1]).contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(InboundIt.body(response).get("result").asString()).isEqualTo("ok");
        assertThat(entities(source.id())).containsExactly("cleo-phone");
    }

    @Test
    @DisplayName("GPSLogger's custom URL with the key in the path answers an empty 200")
    void gpsLogger() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "dan-phone");
        MockHttpServletResponse response = it.perform(get("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/gpslogger")
                .queryParam("lat", "35.6586").queryParam("lon", "139.7454").queryParam("time", "2026-10-12T07:59:00Z")
                .queryParam("acc", "9").queryParam("alt", "40").queryParam("spd", "0.5").queryParam("dir", "270")
                .queryParam("batt", "55").queryParam("dev", "dan-phone"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(entities(source.id())).containsExactly("dan-phone");
    }

    @Test
    @DisplayName("a payload naming another entity is 403 DEVICE_ENTITY_MISMATCH and writes nothing")
    void deviceMismatch() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "eve-phone");
        MockHttpServletResponse osmand = it.perform(get("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/osmand")
                .queryParam("id", "someone-else").queryParam("lat", "1.5").queryParam("lon", "1.5")
                .queryParam("timestamp", Long.toString(TST)));
        assertThat(osmand.getStatus()).isEqualTo(403);
        assertThat(InboundIt.body(osmand).get("code").asString()).isEqualTo("DEVICE_ENTITY_MISMATCH");

        String nativeBody = "{\"records\":[{\"externalId\":\"someone-else\",\"kind\":\"tracker\","
                + "\"observedAt\":\"2026-10-12T07:59:00Z\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1.5,1.5]},"
                + "\"payload\":{}}]}";
        MockHttpServletResponse viaNative = it.perform(post("/v1/ingest/" + source.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + dev[1]).contentType(MediaType.APPLICATION_JSON)
                .content(nativeBody));
        assertThat(viaNative.getStatus()).isEqualTo(403);
        assertThat(entities(source.id())).isEmpty();

        InboundIt.Created other = source("");
        MockHttpServletResponse foreign = it.perform(post("/v1/ingest/" + other.id() + "/owntracks")
                .header(HttpHeaders.AUTHORIZATION, basic(dev[1])).contentType(MediaType.APPLICATION_JSON)
                .content("{\"_type\":\"location\",\"lat\":1.5,\"lon\":1.5,\"tst\":" + TST + "}"));
        assertThat(foreign.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a revoked device key is 401 at once; the device's records stay")
    void revokedKey() throws Exception {
        InboundIt.Created source = source("");
        String[] dev = device(source.id(), "fay-phone");
        String fix = "{\"_type\":\"location\",\"lat\":40.7128,\"lon\":-74.006,\"tst\":" + TST + ",\"acc\":5}";
        assertThat(it.perform(post("/v1/ingest/" + source.id() + "/owntracks").header(HttpHeaders.AUTHORIZATION, basic(dev[1]))
                .contentType(MediaType.APPLICATION_JSON).content(fix)).getStatus()).isEqualTo(200);
        assertThat(it.perform(delete("/v1/custom-sources/" + source.id() + "/devices/" + dev[0])
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(204);
        assertThat(it.perform(post("/v1/ingest/" + source.id() + "/owntracks").header(HttpHeaders.AUTHORIZATION, basic(dev[1]))
                .contentType(MediaType.APPLICATION_JSON).content(fix)).getStatus()).isEqualTo(401);
        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/osmand").queryParam("lat", "1")
                .queryParam("lon", "1")).getStatus()).isEqualTo(401);
        assertThat(entities(source.id())).containsExactly("fay-phone");
    }

    @Test
    @DisplayName("GET and path-key pushes from a public sender are 403 INGEST_SOURCE_DENIED unless ingestAllowCidrs lists it")
    void senderAllowlist() throws Exception {
        InboundIt.Created closed = source("");
        String[] dev = device(closed.id(), "gus-phone");
        MockHttpServletResponse denied = it.perform(get("/v1/ingest/" + closed.id() + "/k/" + dev[1] + "/osmand")
                .queryParam("lat", "1.5").queryParam("lon", "1.5").queryParam("timestamp", Long.toString(TST))
                .with(r -> {
                    r.setRemoteAddr("203.0.113.5");
                    return r;
                }));
        assertThat(denied.getStatus()).isEqualTo(403);
        assertThat(InboundIt.body(denied).get("code").asString()).isEqualTo("INGEST_SOURCE_DENIED");

        // A Basic-auth POST is not a path-key push: the allowlist does not apply.
        assertThat(it.perform(post("/v1/ingest/" + closed.id() + "/owntracks").header(HttpHeaders.AUTHORIZATION, basic(dev[1]))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"_type\":\"location\",\"lat\":1.5,\"lon\":1.5,\"tst\":" + TST + ",\"acc\":5}")
                .with(r -> {
                    r.setRemoteAddr("203.0.113.5");
                    return r;
                })).getStatus()).isEqualTo(200);

        InboundIt.Created open = source(",'ingestAllowCidrs':['203.0.113.0/24']");
        String[] dev2 = device(open.id(), "hal-phone");
        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/k/" + dev2[1] + "/osmand")
                .queryParam("lat", "1.5").queryParam("lon", "1.5").queryParam("timestamp", Long.toString(TST))
                .with(r -> {
                    r.setRemoteAddr("203.0.113.5");
                    return r;
                })).getStatus()).isEqualTo(200);
        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/k/" + dev2[1] + "/osmand")
                .queryParam("lat", "1.5").queryParam("lon", "1.5").queryParam("timestamp", Long.toString(TST)))
                .getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("the source's eik_ key in the path is 404 unless allowPathKey; a format without GET is 405")
    void pathKeysAndMethods() throws Exception {
        InboundIt.Created closed = source("");
        assertThat(it.perform(get("/v1/ingest/" + closed.id() + "/k/" + closed.key() + "/osmand")
                .queryParam("id", "ivy-phone").queryParam("lat", "1.5").queryParam("lon", "1.5")
                .queryParam("timestamp", Long.toString(TST))).getStatus()).isEqualTo(404);

        InboundIt.Created open = source(",'allowPathKey':true");
        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/k/" + open.key() + "/osmand")
                .queryParam("id", "ivy-phone").queryParam("lat", "1.5").queryParam("lon", "1.5")
                .queryParam("timestamp", Long.toString(TST))).getStatus()).isEqualTo(200);
        assertThat(entities(open.id())).containsExactly("ivy-phone");

        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/overland").header("X-Foresignal-Ingest-Key", open.key()))
                .getStatus()).isEqualTo(405);
        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/k/" + open.key() + "/cap")).getStatus()).isEqualTo(404);
        assertThat(it.perform(get("/v1/ingest/" + open.id() + "/k/eid_nope/osmand")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("keys never reach the fetch log, the audit log or the log output")
    void keysRedacted(CapturedOutput output) throws Exception {
        InboundIt.Created source = source(",'allowPathKey':true");
        String[] dev = device(source.id(), "jo-phone");
        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/k/" + dev[1] + "/gpslogger")
                .queryParam("lat", "1.5").queryParam("lon", "1.5").queryParam("time", "2026-10-12T07:59:00Z")
                .queryParam("key", "eik_queryvalue")).getStatus()).isEqualTo(200);
        assertThat(it.perform(post("/v1/ingest/" + source.id() + "/k/" + source.key() + "/native")
                .contentType(MediaType.APPLICATION_JSON).content(InboundIt.feature("pt", "tracker", "2026-10-12T07:59:00Z", 1)))
                .getStatus()).isEqualTo(200);
        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/k/eid_wrongwrongwrong/osmand")).getStatus()).isEqualTo(401);

        List<String> urls = jdbc.queryForList("select url_redacted from fetch_log where source_id = ?", String.class,
                source.id());
        assertThat(urls).isNotEmpty().allSatisfy(url -> assertThat(url).doesNotContain(dev[1])
                .doesNotContain(source.key()).doesNotContain("eik_queryvalue"));
        assertThat(urls).anySatisfy(url -> assertThat(url).contains("/k/redacted/"));

        audit.flush();
        List<String> paths = jdbc.queryForList("select coalesce(path, '') || ' ' || coalesce(detail::text, '')"
                + " from audit_event where path like ?", String.class, "/v1/ingest/" + source.id() + "%");
        assertThat(paths).allSatisfy(p -> assertThat(p).doesNotContain(dev[1]).doesNotContain(source.key()));

        assertThat(output.getAll()).doesNotContain(dev[1]).doesNotContain(source.key()).doesNotContain("eik_queryvalue");
    }
}
