package ai.foresignal.trigger;

import static ai.foresignal.testsupport.reference.RegionTestSources.POINTS;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.reference.RegionGeometries;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Rule geofence presets (prompt 040): a {@code geofenceRef} on a country fires enter and exit exactly
 * like a polygon rule, the alert carries its region snapshot and renders {@code {{near.text}}}, the SQL
 * union agrees with the gate's geometry on every truth-table row, and saving a preset without reference
 * data is {@code 409 REFERENCE_DATA_MISSING}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RegionTestSources.class)
class GeofenceRefIT {

    static final Instant T = Instant.parse("2026-06-04T12:00:00Z");
    static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository repository;
    @Autowired TriggerEngine engine;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired ChangeBus changeBus;
    @Autowired JdbcTemplate jdbc;
    @Autowired RegionGeometries geometries;
    @Autowired RegionTagger tagger;
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired ApplicationContext context;

    TriggerFixture fixture;
    Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
    }

    @AfterEach
    void tearDown() {
        fixture.close();
    }

    void position(double lon, double lat) {
        now = now.plusSeconds(30);
        clock.setInstant(now);
        records.ingest(POINTS, NormalizedRecord.builder("truck", POINT_KIND).observedAt(now).point(lon, lat)
                .put("name", "Truck").build());
    }

    @Test
    @DisplayName("a country preset fires enter and exit like a polygon rule, with the region and near text")
    void enterAndExit() {
        ReferenceFixtures.loadAll(context);
        UUID enter = fixture.rule("{'name':'Into US','sources':['" + POINTS + "'],'geofenceRef':{'country':['us']},"
                + "'mode':'enter','messageTemplate':'{{name}}: {{near.text}} ({{country.name}}, {{county}})'}");
        UUID exit = fixture.rule("{'name':'Out of US','sources':['" + POINTS + "'],'geofenceRef':{'country':['US']},"
                + "'mode':'exit'}");
        UUID polygon = fixture.rule("{'name':'Polygon US','sources':['" + POINTS + "'],'geofence':{'type':'Polygon',"
                + "'coordinates':[[[-110,25],[-90,25],[-90,40],[-110,40],[-110,25]]]},'mode':'enter'}");
        assertThat(repository.findById(enter).orElseThrow().getGeofence()).as("the geometry is never stored").isNull();
        assertThat(repository.findById(enter).orElseThrow().getGeofenceRef()).containsEntry("country", List.of("US"));

        position(-100.3, 20.0);
        position(-97.74, 30.27);
        position(-97.70, 30.37);
        position(-100.3, 20.0);

        List<AlertView> entered = fixture.alerts(enter);
        assertThat(entered).hasSize(1);
        assertThat(entered.getFirst().message()).isEqualTo("Into US: in Austin, TX (United States of America, Travis)");
        RegionTags region = RegionTags.fromJson(entered.getFirst().region());
        assertThat(region.iso2()).isEqualTo("US");
        assertThat(region.admin2()).isEqualTo("48453");
        assertThat(fixture.alerts(exit)).hasSize(1);
        assertThat(RegionTags.fromJson(fixture.alerts(exit).getFirst().region()).iso2()).isEqualTo("MX");
        assertThat(fixture.alerts(polygon)).as("the same edges as the equivalent polygon rule")
                .extracting(AlertView::seq).containsExactlyElementsOf(entered.stream().map(AlertView::seq).toList());
    }

    @Test
    @DisplayName("040 truth table: the SQL union agrees with the gate's JTS geometry on every row")
    void truthTableAgreesWithSql() {
        ReferenceFixtures.loadAll(context);
        for (TruthTable.GeofenceRefRow row : TruthTable.geofenceRefRows()) {
            List<Object> args = new ArrayList<>(List.of(row.lon(), row.lat()));
            String union = row.ref().unionSql(codes -> {
                args.add(PgArrays.text(codes));
                return "CAST(? AS text[])";
            });
            Boolean sql = jdbc.queryForObject("SELECT ST_Intersects(ST_SetSRID(ST_MakePoint(CAST(? AS double precision),"
                    + " CAST(? AS double precision)), 4326), " + union + ")", Boolean.class, args.toArray());
            assertThat(sql).as("SQL %s", row).isEqualTo(row.expected());
            boolean java = org.locationtech.jts.geom.prep.PreparedGeometryFactory.prepare(geometries.union(row.ref()))
                    .intersects(WGS84.createPoint(new Coordinate(row.lon(), row.lat())));
            assertThat(java).as("gate %s", row).isEqualTo(row.expected());
        }
    }

    @Test
    @DisplayName("saving a preset: 409 without reference data; 400 for unknown codes and impossible combinations")
    void validation() throws Exception {
        String agent = TestTokens.bearer(TestTokens.create(tokens, "geofence-agent", Scope.AGENT));
        String body = "{\"name\":\"US\",\"sources\":[\"" + POINTS + "\"],\"geofenceRef\":{\"country\":[\"US\"]}}";
        tagger.resetState();
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REFERENCE_DATA_MISSING"));

        ReferenceFixtures.loadAll(context);
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.geofenceRef.country[0]").value("US"))
                .andExpect(jsonPath("$.geofence").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"US\"]", "\"ZZ\"]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_REGION"));
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"geofenceRef\":{\"country\":[\"US\"]},\"near\":{\"point\":[0,0],"
                                + "\"radiusKm\":1}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"geofenceRef\":{\"county\":[\"48453\"]},\"mode\":\"enter\","
                                + "\"hysteresisMeters\":100}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/v1/triggers").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"geofenceRef\":{}}"))
                .andExpect(status().isBadRequest());
    }
}
