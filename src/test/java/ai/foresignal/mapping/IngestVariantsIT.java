package ai.foresignal.mapping;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * The device ingest variants of prompt 059 end to end: form bodies (with the key in the path and on the default
 * endpoint), GET with the WU {@code PASSWORD}, gzip and its decompression cap, Basic and Bearer keys, the
 * {@code allowPathKey} switch, the sender allowlist, the native replies, and keys kept out of the fetch log, the
 * audit log and the log output. Sources are created from presets through the API.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
@ExtendWith(OutputCaptureExtension.class)
class IngestVariantsIT {

    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");
    static final String ECOWITT_FORM = "PASSKEY=0123456789ABCDEF0123456789ABCDEF&stationtype=GW2000A_V3.2.4"
            + "&dateutc=2026-10-13+15:04:05&tempf=68.4&humidity=55&baromrelin=29.921&winddir=212&windspeedmph=4.47";

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

    InboundIt.Created preset(String preset, String extra) throws Exception {
        return it.create(InboundIt.newId(preset.replace("-", "")), "{'preset':'" + preset + "'" + extra + "}");
    }

    static MockHttpServletRequestBuilder from(MockHttpServletRequestBuilder request, String address) {
        return request.with(r -> {
            r.setRemoteAddr(address);
            return r;
        });
    }

    List<String> externalIds(String sourceId) throws Exception {
        JsonNode listed = body(it.perform(get("/v1/records").param("source", sourceId).header(HttpHeaders.AUTHORIZATION, admin)));
        return listed.findValuesAsString("externalId");
    }

    @Test
    @DisplayName("an Ecowitt form POST with the key in the path lands, with Ecowitt's empty 200; PASSKEY is never stored")
    void ecowittForm() throws Exception {
        InboundIt.Created source = preset("ecowitt", ",'presetParams':{'lon':-97.7431,'lat':30.2672}");
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id() + "/k/" + source.key() + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEmpty();
        assertThat(externalIds(source.id())).containsExactly("station");
        String payload = jdbc.queryForObject("select payload::text from record_head where source_id = ?", String.class,
                source.id());
        assertThat(payload).contains("tempC").doesNotContain("PASSKEY").doesNotContain("0123456789ABCDEF");
    }

    @Test
    @DisplayName("POST /v1/ingest/{sourceId} with a form body uses the preset's default format")
    void defaultFormat() throws Exception {
        InboundIt.Created source = preset("ecowitt", "");
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id())
                .header("X-Foresignal-Ingest-Key", source.key())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(externalIds(source.id())).containsExactly("station");
    }

    @Test
    @DisplayName("WU GET with PASSWORD is authenticated by the query key and answers text/plain success; ID is ignored")
    void wuGet() throws Exception {
        InboundIt.Created source = preset("wu-pws", "");
        MockHttpServletResponse response = it.perform(get("/v1/ingest/" + source.id() + "/wu-pws")
                .queryParam("ID", "KTXAUSTI123").queryParam("PASSWORD", source.key()).queryParam("dateutc", "now")
                .queryParam("tempf", "70.2").queryParam("humidity", "60").queryParam("softwaretype", "EasyWeatherPro"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("success");
        assertThat(response.getContentType()).startsWith("text/plain");
        assertThat(externalIds(source.id())).containsExactly("station");

        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/wu-pws").queryParam("PASSWORD", "eik_wrong")
                .queryParam("tempf", "1")).getStatus()).isEqualTo(401);
        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/wu-pws").queryParam("tempf", "1")).getStatus())
                .isEqualTo(401);
        assertThat(it.perform(from(get("/v1/ingest/" + source.id() + "/wu-pws").queryParam("PASSWORD", source.key())
                .queryParam("tempf", "1"), "203.0.113.5")).getStatus()).as("query key from a public address").isEqualTo(403);
    }

    @Test
    @DisplayName("a format without GET answers 405 to a GET")
    void getNotAllowed() throws Exception {
        InboundIt.Created source = preset("airrohr", "");
        assertThat(it.perform(get("/v1/ingest/" + source.id() + "/airrohr")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + source.key())).getStatus()).isEqualTo(405);
    }

    @Test
    @DisplayName("AIS-catcher gzip with USERPWD (Basic) is decompressed and mapped; over 5 MB decompressed is 413")
    void gzip() throws Exception {
        InboundIt.Created source = preset("aiscatcher", "");
        String json = "{\"protocol\":\"jsonaiscatcher\",\"stationid\":\"home\",\"msgs\":[{\"rxtime\":\"20261013150401\","
                + "\"type\":1,\"mmsi\":366123456,\"status\":0,\"speed\":12.3,\"lon\":-94.8123,\"lat\":29.3101,\"course\":145.6,"
                + "\"heading\":146},{\"rxtime\":\"20261013150402\",\"type\":5,\"mmsi\":366123456,\"shipname\":\"GULF TRADER\","
                + "\"callsign\":\"WDA1234\",\"shiptype\":70,\"destination\":\"HOUSTON\"}]}";
        String basic = "Basic " + Base64.getEncoder().encodeToString(("x:" + source.key()).getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id() + "/aiscatcher")
                .header(HttpHeaders.AUTHORIZATION, basic).header("Content-Encoding", "gzip")
                .contentType(MediaType.APPLICATION_JSON).content(gzip(json.getBytes(StandardCharsets.UTF_8))));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        assertThat(body(response).get("seen").asInt()).isEqualTo(2);
        String payload = jdbc.queryForObject("select payload::text from record_head where source_id = ?", String.class,
                source.id());
        assertThat(payload).contains("GULF TRADER").contains("sog");

        byte[] huge = gzip(new byte[5 * 1024 * 1024 + 10]);
        assertThat(it.perform(post("/v1/ingest/" + source.id() + "/aiscatcher").header(HttpHeaders.AUTHORIZATION, basic)
                .header("Content-Encoding", "gzip").contentType(MediaType.APPLICATION_JSON).content(huge)).getStatus())
                .isEqualTo(413);
    }

    @Test
    @DisplayName("airrohr authenticates with Basic (key as password), ChirpStack and TTS with Bearer")
    void basicAndBearer() throws Exception {
        InboundIt.Created airrohr = preset("airrohr", "");
        String basic = "Basic " + Base64.getEncoder().encodeToString(("sensor:" + airrohr.key()).getBytes(StandardCharsets.UTF_8));
        assertThat(it.perform(post("/v1/ingest/" + airrohr.id() + "/airrohr").header(HttpHeaders.AUTHORIZATION, basic)
                .contentType(MediaType.APPLICATION_JSON).content("{\"esp8266id\":\"77\",\"software_version\":\"NRZ\","
                        + "\"sensordatavalues\":[{\"value_type\":\"SDS_P2\",\"value\":\"4.2\"}]}")).getStatus()).isEqualTo(200);
        assertThat(externalIds(airrohr.id())).containsExactly("airrohr-77");

        InboundIt.Created chirp = preset("chirpstack-v4", "");
        MockHttpServletResponse join = it.perform(post("/v1/ingest/" + chirp.id() + "/chirpstack-v4").queryParam("event", "join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + chirp.key()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceInfo\":{\"devEui\":\"a1\"}}"));
        assertThat(join.getStatus()).isEqualTo(200);
        assertThat(it.perform(post("/v1/ingest/" + chirp.id() + "/chirpstack-v4").queryParam("event", "up")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + chirp.key())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).content(new byte[] {1, 2, 3})).getStatus()).isEqualTo(415);
    }

    @Test
    @DisplayName("a path key needs allowPathKey: off is 404, on is 200; an unknown key is 401; GMC answers OK.")
    void pathKeySwitch() throws Exception {
        InboundIt.Created off = preset("wu-pws", "");
        assertThat(it.perform(get("/v1/ingest/" + off.id() + "/k/" + off.key() + "/gmc").queryParam("GID", "1")
                .queryParam("CPM", "1")).getStatus()).isEqualTo(404);
        InboundIt.Created on = preset("gmc", "");
        MockHttpServletResponse ok = it.perform(get("/v1/ingest/" + on.id() + "/k/" + on.key() + "/gmc")
                .queryParam("AID", "0123456").queryParam("GID", "0987654321").queryParam("CPM", "18").queryParam("ACPM", "17.5")
                .queryParam("uSV", "0.117"));
        assertThat(ok.getStatus()).as(ok.getContentAsString()).isEqualTo(200);
        assertThat(ok.getContentAsString()).isEqualTo("OK.");
        assertThat(externalIds(on.id())).containsExactly("gmc-0987654321");
        assertThat(it.perform(get("/v1/ingest/" + on.id() + "/k/eik_notakey/gmc").queryParam("GID", "1")).getStatus())
                .isEqualTo(401);
        assertThat(it.perform(get("/v1/ingest/" + on.id() + "/k/" + on.key() + "/airrohr")).getStatus())
                .as("a format not served on key-in-path URLs").isEqualTo(404);
    }

    @Test
    @DisplayName("the sender allowlist: a public address is 403 INGEST_SOURCE_DENIED unless ingestAllowCidrs lists it")
    void senderAllowlist() throws Exception {
        InboundIt.Created source = preset("ecowitt", "");
        MockHttpServletResponse denied = it.perform(from(post("/v1/ingest/" + source.id() + "/k/" + source.key() + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM), "203.0.113.5"));
        assertThat(denied.getStatus()).isEqualTo(403);
        assertThat(body(denied).get("code").asString()).isEqualTo("INGEST_SOURCE_DENIED");

        InboundIt.Created listed = preset("ecowitt", ",'ingestAllowCidrs':['203.0.113.0/24']");
        assertThat(it.perform(from(post("/v1/ingest/" + listed.id() + "/k/" + listed.key() + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM), "203.0.113.5")).getStatus())
                .isEqualTo(200);
        assertThat(it.perform(from(post("/v1/ingest/" + source.id() + "/k/" + source.key() + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM), "192.168.1.40")).getStatus())
                .as("RFC 1918 by default").isEqualTo(200);
    }

    @Test
    @DisplayName("path keys, PASSWORD and PASSKEY never reach the fetch log, the audit log or the log output")
    void redaction(CapturedOutput output) throws Exception {
        InboundIt.Created eco = preset("ecowitt", "");
        InboundIt.Created wu = preset("wu-pws", "");
        it.perform(post("/v1/ingest/" + eco.id() + "/k/" + eco.key() + "/ecowitt")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(ECOWITT_FORM));
        it.perform(get("/v1/ingest/" + wu.id() + "/wu-pws").queryParam("ID", "x").queryParam("PASSWORD", wu.key())
                .queryParam("PASSKEY", "PK0123").queryParam("tempf", "70.2"));
        List<String> urls = jdbc.queryForList("select url_redacted from fetch_log where source_id in (?, ?)", String.class,
                eco.id(), wu.id());
        assertThat(urls).isNotEmpty().allSatisfy(url -> assertThat(url).doesNotContain(eco.key())
                .doesNotContain(wu.key()).doesNotContain("PK0123"));
        assertThat(urls).anySatisfy(url -> assertThat(url).contains("/k/redacted/"));
        assertThat(urls).anySatisfy(url -> assertThat(url).contains("PASSWORD=***"));
        audit.flush();
        List<String> audited = jdbc.queryForList("select coalesce(path, '') || ' ' || coalesce(detail::text, '')"
                + " from audit_event where path like ? or path like ?", String.class, "/v1/ingest/" + eco.id() + "%",
                "/v1/ingest/" + wu.id() + "%");
        assertThat(audited).allSatisfy(a -> assertThat(a).doesNotContain(eco.key()).doesNotContain(wu.key()));
        assertThat(output.getAll()).doesNotContain(eco.key()).doesNotContain(wu.key()).doesNotContain("PK0123");
    }

    static byte[] gzip(byte[] data) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(data);
        }
        return out.toByteArray();
    }
}
