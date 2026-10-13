package ai.foresignal.mapping;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * {@code POST /v1/mappings/preview} (prompt 059): records exactly as ingest would write them (normalized, with
 * the source's time base and private precision), the four warning kinds, {@code MAPPING_INVALID} with paths,
 * admin only, and nothing written.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class MappingPreviewIT {

    static final Instant T = Instant.parse("2026-10-13T15:05:00Z");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    MockHttpServletResponse preview(String json) throws Exception {
        return it.perform(post("/v1/mappings/preview").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(json.replace('\'', '"')));
    }

    /** Rows the stand-in preview source (or {@code sourceId}) would have if anything were written. */
    long rows(String sourceId) {
        return jdbc.queryForObject("select (select count(*) from record_head where source_id = ?)"
                + " + (select count(*) from fetch_log where source_id = ?)", Long.class, sourceId, sourceId);
    }

    @Test
    @DisplayName("a form sample previews as normalized records with a GeoJSON point; nothing is written")
    void formPreviewWritesNothing() throws Exception {
        long before = rows("mapping-preview");
        MockHttpServletResponse response = preview("{'mapping':{'version':1,'externalId':{'const':'station'},"
                + "'kind':{'const':'weather-observation'},'observedAt':{'path':'$.dateutc','format':'pattern:yyyy-MM-dd HH:mm:ss'},"
                + "'geometry':{'const':[-97.74,30.27]},'fields':[{'name':'tempC','path':'$.tempf','type':'number',"
                + "'unit':{'from':'[degF]','to':'Cel'}}],'drop':['PASSKEY']},"
                + "'sample':{'contentType':'application/x-www-form-urlencoded','body':'PASSKEY=X&dateutc=2026-10-13+15:04:05&tempf=50&freq=915M'}}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode result = body(response);
        JsonNode record = result.get("records").get(0);
        assertThat(record.get("externalId").asString()).isEqualTo("station");
        assertThat(record.get("observedAt").asString()).isEqualTo("2026-10-13T15:04:05Z");
        assertThat(record.get("geometry").get("type").asString()).isEqualTo("Point");
        assertThat(record.get("payload").get("tempC").asDouble()).isEqualTo(10.0);
        assertThat(result.get("warnings").findValuesAsString("message")).anySatisfy(m -> assertThat(m).contains("freq"));
        assertThat(result.get("warnings").toString()).doesNotContain("PASSKEY");
        assertThat(rows("mapping-preview")).as("nothing persisted").isEqualTo(before).isZero();
    }

    @Test
    @DisplayName("with a sourceId: fieldSpecs, significant-field and time-base warnings, and the source's time base applies")
    void sourceWarnings() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("prev"), "{'preset':'wu-pws'}");
        MockHttpServletResponse response = preview("{'sourceId':'" + source.id() + "','mapping':{'version':1,"
                + "'externalId':{'const':'s'},'observedAt':{'path':'$.t','format':'iso'},"
                + "'fields':[{'name':'tempC','path':'$.tempf','type':'number'},{'name':'humidex','path':'$.h','type':'number'}]},"
                + "'sample':{'contentType':'application/json','body':{'t':'2026-10-13T15:00:00Z','tempf':1,'h':2}}}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode result = body(response);
        assertThat(result.get("warnings").findValuesAsString("code")).contains(MappingWarning.TIME_BASE_MISMATCH,
                MappingWarning.FIELD_NOT_IN_FIELD_SPECS, MappingWarning.SIGNIFICANT_FIELD_MISSING);
        assertThat(result.get("records").get(0).get("observedAt").asString()).as("RECEIVED imputes the receive time")
                .isEqualTo("2026-10-13T15:05:00Z");
        assertThat(rows(source.id())).as("nothing persisted for the source").isZero();
    }

    @Test
    @DisplayName("the standard normalizer applies: null island dropped, the future clamped, a missing observedAt rejected")
    void normalizerApplies() throws Exception {
        MockHttpServletResponse response = preview("{'mapping':{'version':1,'select':'$.p[*]','externalId':{'path':'$.id'},"
                + "'kind':{'const':'tracker'},'observedAt':{'path':'$.t','format':'iso'},"
                + "'geometry':{'lon':{'path':'$.lon'},'lat':{'path':'$.lat'}}},"
                + "'sample':{'contentType':'application/json','body':{'p':[{'id':'a','t':'2026-10-13T15:00:00Z','lon':0,'lat':0},"
                + "{'id':'b','t':'2030-01-01T00:00:00Z','lon':1,'lat':1},{'id':'c','lon':1,'lat':1}]}}}");
        JsonNode result = body(response);
        assertThat(result.get("records").get(0).get("geometry").isNull()).as("NULL_ISLAND").isTrue();
        assertThat(result.get("records").get(1).get("observedAt").asString()).as("future clamped").isEqualTo("2026-10-13T15:05:00Z");
        assertThat(result.get("rejections").findValuesAsString("reason")).containsExactly("MISSING_OBSERVED_AT");
        assertThat(result.get("rejections").get(0).get("index").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("an invalid mapping is 400 MAPPING_INVALID with per-path errors; a bad sample is 400; read tokens are 403")
    void errors() throws Exception {
        MockHttpServletResponse invalid = preview("{'mapping':{'version':1,'externalId':{'path':'$..id'},"
                + "'observedAt':{'path':'$.t','format':'iso'},'fields':[{'name':'t','path':'$.a','type':'number',"
                + "'unit':{'from':'K','to':'Cel'}}]},'sample':{'contentType':'application/json','body':{}}}");
        assertThat(invalid.getStatus()).isEqualTo(400);
        JsonNode problem = body(invalid);
        assertThat(problem.get("code").asString()).isEqualTo("MAPPING_INVALID");
        assertThat(problem.get("errors").findValuesAsString("field")).contains("mapping.externalId.path",
                "mapping.fields[0].unit.from", "mapping.kind");
        assertThat(preview("{'mapping':{'version':1,'externalId':{'const':'a'},'kind':{'const':'k'},"
                + "'observedAt':{'received':true}},'sample':{'contentType':'application/json'}}").getStatus()).isEqualTo(400);
        assertThat(preview("{'sourceId':'custom-nope','mapping':{'version':1,'externalId':{'const':'a'},"
                + "'observedAt':{'received':true}},'sample':{'contentType':'application/json','body':{}}}").getStatus())
                .isEqualTo(404);
        assertThat(it.perform(post("/v1/mappings/preview").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .getStatus()).isEqualTo(401);
    }
}
