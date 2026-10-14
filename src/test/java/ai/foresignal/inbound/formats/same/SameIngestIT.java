package ai.foresignal.inbound.formats.same;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.reference.CountyUnionService;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code POST /v1/ingest/{id}/same} end to end (prompt 086): a custom source with the {@code same-alert} kind receives
 * multimon-ng output; the three repetitions are one record drawn on prompt 040's county polygons (the synthetic
 * {@code ne-admin2-us} fixture: Travis 48453 and Harris 48201), the whole-state header on the state's counties, and
 * {@code validUntil} is the issue time plus the purge.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class SameIngestIT {

    static final Instant RECEIVED = Instant.parse("2026-10-13T15:04:20Z");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ApplicationContext context;
    @Autowired CountyUnionService counties;

    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(RECEIVED);
        settings.invalidateCache();
        it = new InboundIt(mvc, "Bearer " + adminTokens.rotate());
        ReferenceFixtures.load(context, List.of("ne-admin2-us"));
        counties.resetState();
    }

    @Test
    @DisplayName("multimon-ng output: one record per distinct header, county geometry, validUntil = issue + purge")
    void ingest() throws Exception {
        InboundIt.Created source = it.create(InboundIt.newId("nwr"),
                "{'name':'NOAA Weather Radio','kinds':['same-alert'],'observedAtMode':'UPSTREAM'}");
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id() + "/same")
                .header("X-Foresignal-Ingest-Key", source.key()).contentType(MediaType.TEXT_PLAIN)
                .content(AdapterFixtures.text("local-rf", "same-headers.txt")));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);

        List<Map<String, Object>> rows = jdbc.queryForList("select h.external_id, h.valid_until_explicit as valid_until,"
                + " ST_XMin(h.persisted_geom) as xmin, ST_XMax(h.persisted_geom) as xmax, h.payload::text as payload from record_head h"
                + " where h.source_id = ? order by h.external_id", source.id());
        assertThat(rows).extracting(r -> r.get("external_id")).containsExactly(
                "KEWX/NWS|2026-10-13T15:04:00Z|TOR", "KEWX/NWS|2026-12-31T23:30:00Z|WSW",
                "TXDPS/EM|2026-10-13T16:00:00Z|CEM");
        Map<String, Object> tor = rows.getFirst();
        assertThat(((Timestamp) tor.get("valid_until")).toInstant()).isEqualTo(Instant.parse("2026-10-13T15:34:00Z"));
        assertThat(((Number) tor.get("xmin")).doubleValue()).isLessThan(-97.9);
        assertThat(((Number) tor.get("xmax")).doubleValue()).as("Harris is in the union").isGreaterThan(-95.5);
        assertThat(String.valueOf(tor.get("payload"))).contains("Tornado Warning").contains("048201");
        assertThat(rows.get(2).get("xmin")).as("the whole state from its counties").isNotNull();
    }
}
