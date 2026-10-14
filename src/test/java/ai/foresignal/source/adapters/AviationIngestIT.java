package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.awc.AwcCwasAdapter;
import ai.foresignal.source.adapters.awc.AwcGairmetsAdapter;
import ai.foresignal.source.adapters.awc.AwcPirepsAdapter;
import ai.foresignal.source.adapters.awc.AwcSigmetsAdapter;
import ai.foresignal.source.adapters.awc.AwcTafsAdapter;
import ai.foresignal.source.adapters.faa.FaaNasStatusAdapter;
import ai.foresignal.source.adapters.faa.FaaTfrAdapter;
import ai.foresignal.source.adapters.faa.FaaUasfmAdapter;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import ai.foresignal.trigger.CooldownScope;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSqlBuilder;
import ai.foresignal.trigger.Severity;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 087 aviation sources end to end through the real fetch path and ingest writer: every source registers
 * disabled; a TFR whose {@code validFrom} is in the future is stored with it and is not active until it starts (008
 * semantics: a {@code forecastHorizon} rule matches only once {@code validFrom ≤ evaluation time + horizon}); its
 * expiry drops it from current reads; a TFR that leaves the list is a tombstone; NAS delays land at their airports.
 */
class AviationIngestIT extends WeatherIngestSupport {

    static final String LIST = "/tfr.faa.gov/tfrapi/exportTfrList";
    static final String DETAIL = "/tfr.faa.gov/download/detail_6_9375.xml";
    static final String NAS = "/nasstatus.faa.gov/api/airport-status-information";
    static final List<String> IDS = List.of(FaaTfrAdapter.ID, FaaNasStatusAdapter.ID, FaaUasfmAdapter.ID,
            AwcSigmetsAdapter.ID, AwcGairmetsAdapter.ID, AwcCwasAdapter.ID, AwcPirepsAdapter.ID, AwcTafsAdapter.ID,
            "faa-uas-nsufr", "faa-uas-nsufr-parttime", "faa-uas-recreational-sites", "faa-uas-stadiums",
            "faa-uas-prohibited", "faa-uas-defense-tfr");

    @Autowired UpstreamClient client;
    @Autowired SourceRegistry registry;
    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired JsonMapper json;
    @Autowired NamedParameterJdbcTemplate named;

    @BeforeEach
    void setUp() {
        client.stateStore(FaaTfrAdapter.ID).remove(FaaTfrAdapter.ROWS_KEY);
        client.stateStore(FaaNasStatusAdapter.ID).remove(LiveIdSet.LIVE_PREFIX + FaaNasStatusAdapter.KIND);
        client.stateStore(FaaNasStatusAdapter.ID).remove(LiveIdSet.GONE_PREFIX + FaaNasStatusAdapter.KIND);
        configure(FaaTfrAdapter.ID, false, Map.of());
        configure(FaaNasStatusAdapter.ID, false, Map.of());
    }

    @AfterEach
    void disable() {
        configure(FaaTfrAdapter.ID, false, Map.of());
        configure(FaaNasStatusAdapter.ID, false, Map.of());
    }

    private static void serveTfr(String listJson) {
        UPSTREAM.stubFor(get(urlPathEqualTo(LIST)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(listJson.getBytes(StandardCharsets.UTF_8))));
        UPSTREAM.stubFor(get(urlPathEqualTo(DETAIL)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes("faa-tfr",
                        "detail_6_9375.xml"))));
    }

    private static String row() {
        return "[{\"notam_id\":\"6/9375\",\"type\":\"HAZARDS\",\"facility\":\"ZLC\",\"state\":\"OR\",\"description\":"
                + "\"11NM SE SENECA, OR, Wednesday, October 14, 2026 through Monday, October 19, 2026 UTC\","
                + "\"creation_date\":\"10/14/2026\"}]";
    }

    /** The versions of {@code faa-tfr} a {@code forecastHorizon = 0} rule matches when evaluated at {@code at}. */
    private List<MatchRow> activeAt(Instant at) {
        RuleSpec rule = new RuleSpec(UUID.fromString("00000000-0000-0000-0000-000000000087"), "tfr in force", true,
                Set.of(FaaTfrAdapter.ID), Set.of(FaaTfrAdapter.KIND), List.of(), List.of(), null, null, null, 0,
                TriggerMode.MATCH, false, false, Duration.ZERO, CooldownScope.RECORD, Severity.WARNING, null, null,
                false, null, null, null, Duration.ZERO, null, null, false, null, null, false);
        Long to = jdbc.queryForObject("select max(seq) from record_version", Long.class);
        RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, 0, to == null ? 0 : to, at, false);
        return named.query(sql.sql(), sql.params(), MatchRow.MAPPER).stream()
                .filter(r -> r.externalId().equals("6/9375")).toList();
    }

    private JsonNode records(boolean includeExpired) throws Exception {
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/v1/records")
                .param("source", FaaTfrAdapter.ID).param("externalId", "6/9375")
                .param("includeExpired", String.valueOf(includeExpired))
                .header("Authorization", "Bearer " + adminTokens.rotate())).andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return json.readTree(response.getContentAsString()).get("items");
    }

    @Test
    @DisplayName("every aviation source registers, disabled")
    void registered() {
        for (String id : IDS) {
            assertThat(registry.contains(id)).as(id).isTrue();
            assertThat(sources.view(id).enabled()).as(id).isFalse();
        }
    }

    @Test
    @DisplayName("faa-tfr: validFrom in the future is stored and not active until it starts; expiry drops it from"
            + " current reads")
    void futureTfr() throws Exception {
        clock.setInstant(Instant.parse("2026-10-14T04:30:00Z"));
        serveTfr(row());
        assertThat(fetch(FaaTfrAdapter.ID).success()).isTrue();

        assertThat(head("valid_from", FaaTfrAdapter.ID, "6/9375")).isEqualTo(Instant.parse("2026-10-14T15:00:00Z"));
        assertThat(live("valid_until", FaaTfrAdapter.ID, "6/9375")).isEqualTo(Instant.parse("2026-10-19T03:00:00Z"));
        assertThat(jdbc.queryForObject("select payload->>'upperFt' from record_head where source_id = ? and"
                + " external_id = ?", String.class, FaaTfrAdapter.ID, "6/9375")).isEqualTo("6100");
        assertThat(jdbc.queryForObject("select ST_GeometryType(geom) from record_live where source_id = ? and"
                + " external_id = ?", String.class, FaaTfrAdapter.ID, "6/9375")).isEqualTo("ST_Polygon");

        assertThat(activeAt(Instant.parse("2026-10-14T04:30:00Z"))).as("before dateEffective").isEmpty();
        assertThat(activeAt(Instant.parse("2026-10-14T15:00:00Z"))).as("from dateEffective").isNotEmpty();
        assertThat(records(false)).as("current until it expires").hasSize(1);
        assertThat(records(false).get(0).get("validFrom").asString()).isEqualTo("2026-10-14T15:00:00Z");

        clock.setInstant(Instant.parse("2026-10-19T03:30:00Z"));
        assertThat(records(false)).as("expired: gone from current reads").isEmpty();
        assertThat(records(true)).as("still there with includeExpired").hasSize(1);
    }

    @Test
    @DisplayName("faa-tfr: a TFR that leaves the list is a tombstone version")
    void removedTfr() {
        clock.setInstant(Instant.parse("2026-10-14T04:30:00Z"));
        serveTfr(row());
        assertThat(fetch(FaaTfrAdapter.ID).success()).isTrue();
        int before = versions(FaaTfrAdapter.ID, "6/9375");

        clock.setInstant(Instant.parse("2026-10-14T04:45:00Z"));
        serveTfr("[]");
        assertThat(fetch(FaaTfrAdapter.ID).success()).isTrue();
        assertThat(tombstoned(FaaTfrAdapter.ID, "6/9375")).isTrue();
        assertThat(versions(FaaTfrAdapter.ID, "6/9375")).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("faa-nas-status: delays land at their airports; a cleared program is tombstoned")
    void nasStatus() {
        clock.setInstant(Instant.parse("2026-10-14T04:33:00Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(NAS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/xml").withBody(AdapterFixtures.bytes("faa-nas-status",
                        "airport-status-information.xml"))));
        assertThat(fetch(FaaNasStatusAdapter.ID).success()).isTrue();
        assertThat(jdbc.queryForObject("select ST_Y(geom) from record_live where source_id = ? and external_id = ?",
                Double.class, FaaNasStatusAdapter.ID, "ORD|departure-delay")).isBetween(41.9, 42.1);

        clock.setInstant(Instant.parse("2026-10-14T04:38:00Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(NAS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/xml").withBody("<AIRPORT_STATUS_INFORMATION><Update_Time>"
                        + "Wed Oct 14 04:37:43 2026 GMT</Update_Time></AIRPORT_STATUS_INFORMATION>")));
        assertThat(fetch(FaaNasStatusAdapter.ID).success()).isTrue();
        assertThat(tombstoned(FaaNasStatusAdapter.ID, "ORD|departure-delay")).isTrue();
    }
}
