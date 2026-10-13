package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.source.adapters.nhc.NhcCyclonesAdapter;
import ai.foresignal.source.adapters.tsunami.TsunamiWarningsAdapter;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The hazard sources end to end through the real fetch path and ingest writer (prompt 067): the tsunami cancellation
 * thread persists FIRST, SIGNIFICANT, SIGNIFICANT ({@code cancelled}) and expires at the cancellation's
 * {@code expires}; {@code validFrom} is stored on the head and returned by {@code /v1/records}; a cyclone that leaves
 * NHC's complete set is a TOMBSTONE version.
 */
class HazardIngestIT extends WeatherIngestSupport {

    static final String TSUNAMI = TsunamiWarningsAdapter.ID;
    static final String NHC = NhcCyclonesAdapter.ID;
    static final String PAAQ_CAP = "/events/xml/PAAQCAP.xml";
    static final String PHEB_CAP = "/events/xml/PHEBCAP.xml";
    static final String THREAD = "PAAQ:tsthr01";

    @Autowired private UpstreamClient client;
    @Autowired private MockMvc mvc;
    @Autowired private AdminTokenService adminTokens;
    @Autowired private JsonMapper json;

    @BeforeEach
    void setUp() {
        AdapterStateStore tsunami = client.stateStore(TSUNAMI);
        tsunami.remove(CapThreadResolver.STATE_KEY);
        for (String center : TsunamiWarningsAdapter.CENTERS) {
            SourceHttp.forgetValidators(tsunami, TsunamiWarningsAdapter.CAP_STATE_PREFIX + center);
            SourceHttp.forgetValidators(tsunami, TsunamiWarningsAdapter.ATOM_STATE_PREFIX + center);
        }
        AdapterStateStore nhc = client.stateStore(NHC);
        nhc.remove(LiveIdSet.LIVE_PREFIX + NhcCyclonesAdapter.ACTIVE_SCOPE);
        nhc.remove(LiveIdSet.GONE_PREFIX + NhcCyclonesAdapter.ACTIVE_SCOPE);
        SourceHttp.forgetValidators(nhc, NhcCyclonesAdapter.STATE_KEY);
        configure(TSUNAMI, false, Map.of());
        configure(NHC, false, Map.of());
    }

    private static void serveTsunami(String fixture) {
        UPSTREAM.stubFor(get(urlEqualTo(PAAQ_CAP)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes(TSUNAMI, fixture))));
        UPSTREAM.stubFor(get(urlEqualTo(PHEB_CAP)).willReturn(aResponse().withStatus(304)));
    }

    private List<Integer> reasons(String sourceId, String externalId) {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, sourceId, externalId);
    }

    @Test
    @DisplayName("tsunami thread: FIRST, SIGNIFICANT, SIGNIFICANT(cancelled), expiring at the cancellation's expires")
    void tsunamiThread() throws Exception {
        int before = versions(TSUNAMI, THREAD);
        clock.setInstant(Instant.parse("2026-10-13T20:05:00Z"));
        for (String fixture : List.of("thread-1-warning.xml", "thread-2-update.xml", "thread-3-cancellation.xml")) {
            serveTsunami(fixture);
            FetchOutcome outcome = fetch(TSUNAMI);
            assertThat(outcome.success()).as(fixture).isTrue();
            clock.advance(Duration.ofMinutes(30));
        }

        assertThat(versions(TSUNAMI, THREAD) - before).isEqualTo(3);
        List<Integer> reasons = reasons(TSUNAMI, THREAD);
        assertThat(reasons.subList(reasons.size() - 3, reasons.size())).containsExactly((int) PersistReason.FIRST.code(),
                (int) PersistReason.SIGNIFICANT.code(), (int) PersistReason.SIGNIFICANT.code());
        String state = jdbc.queryForObject("select payload->>'state' from record_head where source_id = ? and"
                + " external_id = ?", String.class, TSUNAMI, THREAD);
        assertThat(state).isEqualTo("cancelled");
        assertThat(tombstoned(TSUNAMI, THREAD)).isFalse();
        assertThat(live("valid_until", TSUNAMI, THREAD)).isEqualTo(Instant.parse("2026-10-13T22:00:00Z"));
        assertThat(head("valid_from", TSUNAMI, THREAD)).isEqualTo(Instant.parse("2026-10-13T21:00:00Z"));
    }

    @Test
    @DisplayName("validFrom is stored on the head and returned by /v1/records")
    void validFromReturned() throws Exception {
        String next = "PAAQ:tsnext1";
        clock.setInstant(Instant.parse("2026-10-13T20:20:00Z"));
        serveTsunami("unrelated-next-event.xml");
        assertThat(fetch(TSUNAMI).success()).isTrue();
        assertThat(head("valid_from", TSUNAMI, next)).isEqualTo(Instant.parse("2026-10-13T20:15:00Z"));

        String admin = adminTokens.rotate();
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/v1/records")
                .param("source", TSUNAMI).param("externalId", next)
                .header("Authorization", "Bearer " + admin)).andReturn().getResponse();

        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        JsonNode items = json.readTree(response.getContentAsString()).get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).get("validFrom").asString()).isEqualTo("2026-10-13T20:15:00Z");
        assertThat(items.get(0).get("validUntil").asString()).isEqualTo("2026-10-13T21:15:00Z");
    }

    @Test
    @DisplayName("a cyclone that leaves NHC's complete set is a TOMBSTONE version")
    void cycloneTombstone() {
        clock.setInstant(Instant.parse("2026-10-13T14:50:00Z"));
        UPSTREAM.stubFor(get(urlEqualTo(NhcCyclonesAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes(NHC, "CurrentStorms.json"))));
        assertThat(fetch(NHC).success()).isTrue();
        assertThat(tombstoned(NHC, "ep202026")).isFalse();

        clock.advance(Duration.ofMinutes(10));
        UPSTREAM.stubFor(get(urlEqualTo(NhcCyclonesAdapter.PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes(NHC, "empty.json"))));
        assertThat(fetch(NHC).success()).isTrue();

        assertThat(tombstoned(NHC, "ep202026")).isTrue();
        List<Integer> reasons = reasons(NHC, "ep202026");
        assertThat(reasons.get(reasons.size() - 1)).isEqualTo((int) PersistReason.TOMBSTONE.code());
    }
}
