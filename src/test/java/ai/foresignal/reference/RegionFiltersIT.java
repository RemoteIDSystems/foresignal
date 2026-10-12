package ai.foresignal.reference;

import static ai.foresignal.testsupport.reference.RegionTestSources.POINTS;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINT_KIND;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The {@code country}, {@code admin1} and {@code county} filters (prompt 040) on records (JSON and
 * GeoJSON), search, alerts and stats, {@code include=region}, and {@code 400 UNKNOWN_REGION} for unknown
 * codes. One record is in Austin (Travis County, Texas), one in the fixture Mexico.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RegionTestSources.class)
class RegionFiltersIT {

    static final Instant T = Instant.parse("2026-06-03T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired RegionTagWorker worker;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired AlertRepository alerts;
    @Autowired ApplicationContext context;

    String reader;

    @BeforeEach
    void setUp() {
        reader = TestTokens.bearer(TestTokens.create(tokens, "region-reader", Scope.READ));
        clock.setInstant(T);
        ReferenceFixtures.loadAll(context);
        await().atMost(Duration.ofSeconds(15)).until(() -> !worker.retagging());
        records.ingest(POINTS,
                NormalizedRecord.builder("austin", POINT_KIND).observedAt(T).point(-97.74, 30.27).put("name", "Point A")
                        .build(),
                NormalizedRecord.builder("mexico", POINT_KIND).observedAt(T).point(-100.3, 20.0).put("name", "Point B")
                        .build());
        await().atMost(Duration.ofSeconds(15)).until(() -> jdbc.queryForObject(
                "SELECT count(*) FROM record_region WHERE source_id = ? AND iso2 IS NOT NULL", Long.class, POINTS) == 2);
    }

    @Test
    @DisplayName("records: country, admin1 and county narrow the list; codes are case-insensitive and ORed")
    void records() throws Exception {
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].externalId").value("austin"));
        mvc.perform(get("/v1/records").param("source", POINTS).param("admin1", "us-tx")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(1)));
        mvc.perform(get("/v1/records").param("source", POINTS).param("county", "48453")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(1)));
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US,MX")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(2)));
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "MX").param("admin1", "US-TX")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(0)));
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US").param("count", "true")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("include=region returns Record.region, and GeoJSON features gain properties.near")
    void includeRegion() throws Exception {
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US").param("include", "region")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items[0].region.iso2").value("US"))
                .andExpect(jsonPath("$.items[0].region.admin1Name").value("Texas"))
                .andExpect(jsonPath("$.items[0].region.countyName").value("Travis"))
                .andExpect(jsonPath("$.items[0].region.near.text").value("in Austin, TX"))
                .andExpect(jsonPath("$.items[0].region.attribution").value(RegionAttribution.GEONAMES));
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US").param("format", "geojson")
                        .param("include", "region").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.features[0].properties.near").value("in Austin, TX"))
                .andExpect(jsonPath("$.features[0].properties.nearAttribution").value(RegionAttribution.GEONAMES));
        mvc.perform(get("/v1/records/" + POINTS + "/mexico").param("include", "region")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.region.iso2").value("MX"))
                .andExpect(jsonPath("$.region.near.text").value(org.hamcrest.Matchers.endsWith("of Monterrey, Mexico")));
        mvc.perform(get("/v1/records/" + POINTS + "/mexico").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.region").doesNotExist());
    }

    @Test
    @DisplayName("unknown codes are 400 UNKNOWN_REGION; the region filters cannot be combined with asOf")
    void unknownRegion() throws Exception {
        for (String[] param : List.of(new String[] {"country", "ZZ"}, new String[] {"admin1", "US-QQQ"},
                new String[] {"county", "99999"}, new String[] {"county", "abc"})) {
            mvc.perform(get("/v1/records").param(param[0], param[1]).header(HttpHeaders.AUTHORIZATION, reader))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("UNKNOWN_REGION"));
        }
        mvc.perform(get("/v1/search").param("q", "Point").param("country", "ZZ")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_REGION"));
        mvc.perform(get("/v1/alerts").param("admin1", "XX-1").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_REGION"));
        mvc.perform(get("/v1/records").param("source", POINTS).param("country", "US").param("asOf", T.toString())
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("search: text search over records honors the region filters")
    void search() throws Exception {
        mvc.perform(get("/v1/search").param("q", "Point").param("mode", "text").param("docType", "record")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(2)));
        mvc.perform(get("/v1/search").param("q", "Point").param("mode", "text").param("country", "MX")
                        .header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].docId").value(POINTS + ":mexico"));
    }

    @Test
    @DisplayName("alerts: the region snapshot taken at fire time is filtered and returned")
    void alerts() throws Exception {
        alert("austin", RegionTagsFixture.AUSTIN);
        alert("mexico", Map.of("iso2", "MX", "countryName", "Mexico"));
        alert("unknown", null);
        mvc.perform(get("/v1/alerts").param("country", "US").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].externalId").value("austin"))
                .andExpect(jsonPath("$.items[0].region.admin2").value("48453"));
        mvc.perform(get("/v1/alerts").param("county", "48453").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(1)));
        mvc.perform(get("/v1/alerts").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(jsonPath("$.items", hasSize(3)));
    }

    @Test
    @DisplayName("stats: a region filter is answered from raw rows")
    void stats() throws Exception {
        mvc.perform(get("/v1/stats/timeseries").param("source", POINTS).param("country", "MX")
                        .param("from", T.minusSeconds(3600).toString()).param("to", T.plusSeconds(3600).toString())
                        .param("bucket", "1h").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backend").value("raw"));
    }

    void alert(String externalId, Map<String, Object> region) {
        AlertEntity a = new AlertEntity(UUID.randomUUID(), 1L, POINTS, externalId, T);
        a.setKind(POINT_KIND);
        a.setSeverity("warning");
        a.setMessage("Alert " + externalId);
        a.setRegion(region);
        alerts.saveAndFlush(a);
    }

    /** A {@code RegionTags} snapshot as stored on an alert. */
    static final class RegionTagsFixture {
        static final Map<String, Object> AUSTIN = new RegionTags("US", "US-TX", "48453", null, null,
                "United States of America", "Texas", "Travis").toMap();
    }
}
