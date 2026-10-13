package ai.foresignal.mapping;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Sample capture (prompt 059): {@code captureSamples} keeps the last five raw requests, redacted and without
 * headers; it turns itself off after an hour; {@code POST /v1/custom-sources/{id}/mapping/test} runs the stored or
 * a proposed mapping over them.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SampleCaptureIT {

    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");
    static final String FORM = "PASSKEY=0123456789ABCDEF&stationtype=GW2000A&dateutc=2026-10-13+15:04:05&tempf=68.4";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    MockHttpServletResponse pushForm(InboundIt.Created source, String form) throws Exception {
        return it.perform(post("/v1/ingest/" + source.id() + "/k/" + source.key() + "/ecowitt")
                .header("User-Agent", "GW2000A").header("X-Secret-Header", "hdr-secret")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content(form));
    }

    JsonNode samples(String id) throws Exception {
        MockHttpServletResponse response = it.perform(get("/v1/custom-sources/" + id + "/samples")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return body(response);
    }

    MockHttpServletResponse test(String id, String body) throws Exception {
        var request = post("/v1/custom-sources/" + id + "/mapping/test").header(HttpHeaders.AUTHORIZATION, admin);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"'));
        }
        return it.perform(request);
    }

    @Test
    @DisplayName("samples are redacted (path key, PASSKEY, password members), carry no headers but Content-Type, newest first, at most 5")
    void redactedAndCapped() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("cap"), "{'preset':'ecowitt','captureSamples':true}");
        assertThat(source.body().get("captureSamples").asBoolean()).isTrue();
        for (int i = 0; i < 6; i++) {
            assertThat(pushForm(source, FORM + "&n=" + i).getStatus()).isEqualTo(200);
        }
        JsonNode samples = samples(source.id());
        assertThat(samples).hasSize(SampleCapture.PER_SOURCE);
        JsonNode newest = samples.get(0);
        assertThat(newest.get("body").asString()).contains("n=5").contains("PASSKEY=***").doesNotContain("0123456789ABCDEF");
        assertThat(samples.get(4).get("body").asString()).contains("n=1");
        assertThat(newest.get("method").asString()).isEqualTo("POST");
        assertThat(newest.get("format").asString()).isEqualTo("ecowitt");
        assertThat(newest.get("contentType").asString()).startsWith("application/x-www-form-urlencoded");
        String text = samples.toString();
        assertThat(text).doesNotContain(source.key()).doesNotContain("hdr-secret").doesNotContain("GW2000A\"")
                .doesNotContain("User-Agent");

        InboundIt.Created json = it.create(InboundIt.newId("capjson"), "{'name':'J','kinds':['k'],'captureSamples':true}");
        it.perform(post("/v1/ingest/" + json.id()).header("X-Foresignal-Ingest-Key", json.key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"hunter2\",\"records\":[]}"));
        JsonNode jsonSample = samples(json.id()).get(0);
        assertThat(jsonSample.get("body").asString()).doesNotContain("hunter2").contains("\"password\":\"***\"");
        assertThat(jsonSample.get("format").isNull()).as("the default endpoint").isTrue();
    }

    @Test
    @DisplayName("capture turns itself off after an hour; turning it off drops the samples")
    void autoOff() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("capoff"), "{'preset':'ecowitt','captureSamples':true}");
        pushForm(source, FORM);
        clock.setInstant(T.plus(Duration.ofMinutes(61)));
        pushForm(source, FORM.replace("15:04:05", "16:05:00"));
        assertThat(samples(source.id())).hasSize(1);
        JsonNode view = body(it.perform(get("/v1/custom-sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(view.get("captureSamples").asBoolean()).isFalse();

        clock.setInstant(T);
        InboundIt.Created other = it.create(InboundIt.newId("capdrop"), "{'preset':'ecowitt','captureSamples':true}");
        pushForm(other, FORM);
        JsonNode stored = body(it.perform(get("/v1/custom-sources/" + other.id()).header(HttpHeaders.AUTHORIZATION, admin)));
        ((tools.jackson.databind.node.ObjectNode) stored).put("captureSamples", false);
        for (String member : new String[] {"createdAt", "lastIngestAt", "ingestKeyPrefix", "verifiers"}) {
            ((tools.jackson.databind.node.ObjectNode) stored).remove(member);
        }
        MockHttpServletResponse updated = it.perform(put("/v1/custom-sources/" + other.id())
                .header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON).content(stored.toString()));
        assertThat(updated.getStatus()).as(updated.getContentAsString()).isEqualTo(200);
        assertThat(samples(other.id())).isEmpty();
    }

    @Test
    @DisplayName("the mapping test runs the stored mapping, or a proposed one, over every captured sample")
    void mappingTest() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("captest"), "{'preset':'ecowitt','captureSamples':true}");
        pushForm(source, FORM);
        pushForm(source, FORM.replace("tempf=68.4", "tempf=50"));

        JsonNode stored = body(test(source.id(), null));
        assertThat(stored.get("mappingSource").asString()).isEqualTo("stored");
        assertThat(stored.get("samples")).hasSize(2);
        assertThat(stored.get("samples").get(0).get("records").get(0).get("payload").get("tempC").asDouble()).isEqualTo(10.0);
        assertThat(stored.get("samples").get(0).get("receivedAt").asString()).isEqualTo("2026-10-13T15:05:00Z");

        JsonNode proposed = body(test(source.id(), "{'mapping':{'version':1,'externalId':{'const':'x'},"
                + "'observedAt':{'path':'$.dateutc','format':'pattern:yyyy-MM-dd HH:mm:ss'},"
                + "'fields':[{'name':'model','path':'$.stationtype','type':'string'}]}}"));
        assertThat(proposed.get("mappingSource").asString()).isEqualTo("proposed");
        assertThat(proposed.get("samples").get(0).get("records").get(0).get("payload").get("model").asString())
                .isEqualTo("GW2000A");

        MockHttpServletResponse invalid = test(source.id(), "{'mapping':{'version':1}}");
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("MAPPING_INVALID");
    }

    @Test
    @DisplayName("409 MAPPING_NOT_SET without a mapping, 409 NO_SAMPLES without samples, 404 for an unknown source")
    void testConflicts() throws Exception {
        InboundIt.Created plain = it.create(InboundIt.newId("capplain"), "{'name':'P','kinds':['k']}");
        MockHttpServletResponse noMapping = test(plain.id(), null);
        assertThat(noMapping.getStatus()).isEqualTo(409);
        assertThat(body(noMapping).get("code").asString()).isEqualTo("MAPPING_NOT_SET");
        InboundIt.Created preset = it.create(InboundIt.newId("capnone"), "{'preset':'ecowitt'}");
        MockHttpServletResponse noSamples = test(preset.id(), null);
        assertThat(noSamples.getStatus()).isEqualTo(409);
        assertThat(body(noSamples).get("code").asString()).isEqualTo("NO_SAMPLES");
        assertThat(test("custom-does-not-exist", null).getStatus()).isEqualTo(404);
        assertThat(it.perform(get("/v1/custom-sources/custom-does-not-exist/samples")
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
    }
}
