package ai.foresignal.mapping;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.mapping.preset.PresetCatalog;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
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
 * Custom sources created from presets (prompt 059): the key and the concrete setup URL once, the preset's
 * defaults accepted by the policy validator for every preset, the catalog listing, and the preset rules on
 * create and update.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class PresetCustomSourceIT {

    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    MockHttpServletResponse create(String json) throws Exception {
        return it.perform(post("/v1/custom-sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.replace('\'', '"')));
    }

    @Test
    @DisplayName("a preset create returns the key and the setup with the concrete URL once; reads show neither")
    void keyOnce() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("eco"), "{'preset':'ecowitt','presetParams':{'lon':-97.7431,"
                + "'lat':30.2672}}");
        JsonNode created = source.body();
        String url = created.get("setup").get("url").asString();
        assertThat(url).endsWith("/v1/ingest/" + source.id() + "/k/" + source.key() + "/ecowitt");
        assertThat(created.get("setup").get("instructions").asString()).contains("/v1/ingest/" + source.id() + "/k/"
                + source.key() + "/ecowitt").contains("Ecowitt").doesNotContain("{path}").doesNotContain("{host}");
        assertThat(created.get("preset").asString()).isEqualTo("ecowitt");
        assertThat(created.get("allowPathKey").asBoolean()).isTrue();
        assertThat(created.get("kinds").get(0).asString()).isEqualTo("weather-observation");
        assertThat(created.get("mapping").get("geometry").get("const").get(1).asDouble()).isEqualTo(30.27);
        assertThat(created.get("name").asString()).isEqualTo("Ecowitt weather station (Customized upload)");

        JsonNode read = body(it.perform(get("/v1/custom-sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(read.has("ingestKey")).isFalse();
        assertThat(read.has("setup")).isFalse();
        assertThat(read.toString()).doesNotContain(source.key());
    }

    @Test
    @DisplayName("every preset creates a source the policy validator accepts, with the preset's descriptor defaults")
    void everyPresetValidates() throws Exception {
        for (var preset : PresetCatalog.all()) {
            MockHttpServletResponse response = create("{'id':'" + InboundIt.newId("p" + preset.id().replace("-", ""))
                    + "','preset':'" + preset.id() + "'}");
            assertThat(response.getStatus()).as(preset.id() + ": " + response.getContentAsString()).isEqualTo(201);
            String id = body(response).get("id").asString();
            SourceDescriptor descriptor = registry.find(id).orElseThrow().descriptor();
            if (preset.ingestPolicy() != null) {
                assertThat(descriptor.defaultIngestPolicy().mode()).as(preset.id()).isEqualTo(preset.ingestPolicy().mode());
            }
        }
        SourceDescriptor ais = registry.find(body(create("{'id':'" + InboundIt.newId("aisx") + "','preset':'aiscatcher'}"))
                .get("id").asString()).orElseThrow().descriptor();
        assertThat(ais.defaultIngestPolicy()).isInstanceOf(IngestPolicy.Sampled.class);
        assertThat(ais.defaultRetention()).isEqualTo(Duration.ofDays(3));
        assertThat(ais.thresholds()).hasSize(2);
        assertThat(ais.motion()).isNotNull();
        assertThat(ais.significantFields()).contains("name", "callsign", "shipType", "destination", "navStatus");
        SourceDescriptor chirp = registry.find(body(create("{'id':'" + InboundIt.newId("chx") + "','preset':'chirpstack-v4'}"))
                .get("id").asString()).orElseThrow().descriptor();
        assertThat(chirp.privateSource()).isTrue();
    }

    @Test
    @DisplayName("the preset's significant fields cannot be emptied under its LATEST policy")
    void policyGuard() throws Exception {
        MockHttpServletResponse response = create("{'id':'" + InboundIt.newId("eco-empty") + "','preset':'ecowitt',"
                + "'significantFields':[]}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(400);
        assertThat(body(response).get("code").asString()).as(response.getContentAsString())
                .isEqualTo("INGEST_POLICY_INVALID");
    }

    @Test
    @DisplayName("unknown preset and unknown or malformed parameters are 400; an input member wins over the preset")
    void presetRules() throws Exception {
        assertThat(create("{'id':'" + InboundIt.newId("bad") + "','preset':'davis'}").getStatus()).isEqualTo(400);
        MockHttpServletResponse params = create("{'id':'" + InboundIt.newId("bad") + "','preset':'gmc',"
                + "'presetParams':{'altitude':3,'lon':500}}");
        assertThat(params.getStatus()).isEqualTo(400);
        assertThat(body(params).get("errors").findValuesAsString("field")).contains("presetParams.altitude",
                "presetParams.lon");
        assertThat(create("{'id':'" + InboundIt.newId("bad") + "','name':'x','kinds':['k'],'presetParams':{'lon':1}}")
                .getStatus()).as("presetParams without preset").isEqualTo(400);
        JsonNode named = body(create("{'id':'" + InboundIt.newId("named") + "','preset':'gmc','name':'Basement counter'}"));
        assertThat(named.get("name").asString()).isEqualTo("Basement counter");
    }

    @Test
    @DisplayName("the preset cannot change on update; repeating it is fine")
    void presetOnUpdate() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("upd"), "{'preset':'gmc'}");
        JsonNode view = source.body();
        String body = "{'id':'" + source.id() + "','name':'Counter','kinds':['radiation'],'observedAtMode':'RECEIVED',"
                + "'significantFields':['geigerId'],'recordTtl':'30m','mapping':" + view.get("mapping").toString().replace('"', '\'')
                + ",'preset':'%s'}";
        MockHttpServletResponse changed = it.perform(put("/v1/custom-sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(String.format(body, "airrohr").replace('\'', '"')));
        assertThat(changed.getStatus()).isEqualTo(400);
        MockHttpServletResponse same = it.perform(put("/v1/custom-sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(String.format(body, "gmc").replace('\'', '"')));
        assertThat(same.getStatus()).as(same.getContentAsString()).isEqualTo(200);
    }

    @Test
    @DisplayName("GET /v1/mappings/presets lists the eight presets with URL templates for this server; admin only")
    void listing() throws Exception {
        MockHttpServletResponse response = it.perform(get("/v1/mappings/presets").header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(response.getStatus()).isEqualTo(200);
        JsonNode presets = body(response);
        assertThat(presets.findValuesAsString("id")).contains("ecowitt", "wu-pws", "aiscatcher", "chirpstack-v4", "tts-v3",
                "alertmanager", "airrohr", "gmc");
        JsonNode ecowitt = null;
        for (JsonNode p : presets) {
            if ("ecowitt".equals(p.get("id").asString())) {
                ecowitt = p;
            }
        }
        assertThat(ecowitt.get("setup").get("urlTemplate").asString()).startsWith("http://")
                .endsWith("/v1/ingest/{sourceId}/k/{key}/ecowitt");
        assertThat(ecowitt.get("setup").get("url").isNull()).isTrue();
        assertThat(ecowitt.get("allowsGet").asBoolean()).isFalse();
        assertThat(ecowitt.get("params").findValuesAsString("name")).contains("lon", "lat", "exactLocation");
        assertThat(it.perform(get("/v1/mappings/presets")).getStatus()).isEqualTo(401);
    }
}
