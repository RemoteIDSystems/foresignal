package ai.foresignal.source.adapters.radio;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 082 sources end to end in the shared ingest context ({@link WeatherIngestSupport}): all register, the
 * streaming ones disabled; {@code aprs-is} cannot be enabled without a valid narrow filter; PSK Reporter spots ingest
 * through the real fetch path and their receivers come back only as observers, only under {@code include=observer}
 * for an admin token; an APRS station re-beacon with the same content (another path and igate) only touches.
 */
class RadioIngestIT extends WeatherIngestSupport {

    static final String QUERY = "/retrieve.pskreporter.info/query";
    static final List<String> IDS = List.of(PskReporterAdapter.ID, KiwiSdrAdapter.ID, OpenMhzAdapter.ID,
            AprsIsAdapter.ID, "local-aprs-1", "local-aprs-2");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired JsonMapper json;
    @Autowired IngestQueue queue;
    @Autowired AprsIsAdapter aprsIs;

    @AfterEach
    void disable() {
        configure(PskReporterAdapter.ID, false, Map.of(PskReporterAdapter.APP_CONTACT, "operator@example.org"));
    }

    @Test
    @DisplayName("all six register; aprs-is and local-aprs-* are disabled, local-aprs-* LOCAL_ONLY")
    void registered() {
        assertThat(sources.list()).filteredOn(s -> IDS.contains(s.id())).hasSize(IDS.size());
        assertThat(sources.list()).filteredOn(s -> s.id().equals(AprsIsAdapter.ID) || s.id().startsWith("local-aprs-"))
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
        assertThat(sources.list()).filteredOn(s -> s.id().startsWith("local-aprs-"))
                .allSatisfy(s -> assertThat(s.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY));
    }

    @Test
    @DisplayName("aprs-is cannot be enabled without a filter, nor with a wide one; it stays disabled")
    void aprsIsNeedsNarrowFilter() {
        assertThatThrownBy(() -> sources.update(AprsIsAdapter.ID, SourceSettingsUpdate.enabled(true)))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anySatisfy(f -> assertThat(f.field()).isEqualTo("params.filter")));
        assertThatThrownBy(() -> configure(AprsIsAdapter.ID, true, Map.of(AprsIsAdapter.FILTER, "r/37.77/-122.42/900")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> configure(AprsIsAdapter.ID, true, Map.of(AprsIsAdapter.FILTER, "b/*")))
                .isInstanceOf(ValidationException.class);
        assertThat(sources.list()).filteredOn(s -> s.id().equals(AprsIsAdapter.ID)).singleElement()
                .satisfies(s -> assertThat(s.enabled()).isFalse());
    }

    @Test
    @DisplayName("PSK spots ingest; receivers are observers, returned only with include=observer to an admin token")
    void pskObservers() throws Exception {
        configure(PskReporterAdapter.ID, false, Map.of(PskReporterAdapter.APP_CONTACT, "operator@example.org"));
        UPSTREAM.stubFor(get(urlPathEqualTo(QUERY)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes("pskreporter-ft8",
                        "callsign-N6AD.xml"))));
        clock.setInstant(Instant.parse("2026-10-14T00:45:00Z"));

        assertThat(fetch(PskReporterAdapter.ID).success()).isTrue();
        String id = "K3UA|N6AD|14075|1791937846";
        assertThat(versions(PskReporterAdapter.ID, id)).isEqualTo(1);

        String token = adminTokens.rotate();
        JsonNode with = read(token, id, true);
        JsonNode without = read(token, id, false);
        assertThat(with.path("observer").get(0).path("receiverId").asString()).isEqualTo("N6AD");
        assertThat(without.has("observer")).isFalse();
        assertThat(with.path("payload").toString()).doesNotContain("\"N6AD\"");
        assertThat(with.path("payload").path("sender").asString()).isEqualTo("K3UA");
    }

    private JsonNode read(String token, String id, boolean observers) throws Exception {
        var request = MockMvcRequestBuilders.get("/v1/records/{source}/{id}", PskReporterAdapter.ID, id)
                .header("Authorization", "Bearer " + token);
        if (observers) {
            request = request.queryParam("include", "observer");
        }
        MockHttpServletResponse response = mvc.perform(request).andReturn().getResponse();
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("an APRS re-beacon with the same content through another igate and path only touches")
    void aprsRebeaconTouches() {
        AprsIsAdapter.Session session = aprsIs.newSession(AprsIsAdapter.ID, false, false);
        Instant t0 = Instant.parse("2026-10-14T01:00:00Z");
        clock.setInstant(t0);
        List<NormalizedRecord> first = session.onLine(("N0CALL-9>APRS,WIDE1-1,qAR,N0CALL-10:=3745.00N/12225.00W>"
                + "090/036/A=001234 it-rebeacon").getBytes(StandardCharsets.UTF_8), t0);
        List<NormalizedRecord> second = session.onLine(("N0CALL-9>APRS,WIDE2-1,qAR,N0CALL-11:=3745.00N/12225.00W>"
                + "090/036/A=001234 it-rebeacon").getBytes(StandardCharsets.UTF_8), t0.plusSeconds(600));
        TestRecords records = new TestRecords(queue, clock);

        IngestOutcome a = records.ingestAt(AprsIsAdapter.ID, t0, first.toArray(NormalizedRecord[]::new));
        clock.setInstant(t0.plusSeconds(600));
        IngestOutcome b = records.ingestAt(AprsIsAdapter.ID, t0.plusSeconds(600), second.toArray(NormalizedRecord[]::new));

        assertThat(a.persisted()).isEqualTo(1);
        assertThat(b.persisted()).isZero();
        assertThat(b.touched() + b.touchesThrottled()).isEqualTo(1);
        assertThat(versions(AprsIsAdapter.ID, "N0CALL-9")).isEqualTo(1);
    }
}
