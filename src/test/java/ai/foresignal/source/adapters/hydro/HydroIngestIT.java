package ai.foresignal.source.adapters.hydro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.source.adapters.hydro.nwps.NwpsGaugesAdapter;
import ai.foresignal.source.adapters.hydro.usdm.UsdmAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 079 sources end to end through the real fetch path and ingest writer, against the shared stub server:
 * every source registers disabled with its required parameters enforced at PUT; an NWPS flood-category change creates
 * a version whose head carries the new {@code floodCategory}; an unchanged USDM week is a 304 that writes no version.
 *
 * <p>{@code nwps-gauges} and {@code usdm} are used by no other integration test; each test resets their adapter state
 * and counts versions relative to what was there before.
 */
class HydroIngestIT extends WeatherIngestSupport {

    static final String NWPS = "/api.water.noaa.gov/nwps/v1/gauges";
    static final String USDM = "/droughtmonitor.unl.edu/data/json/";
    static final String ETAG = "\"7f7f5dbe57dd1:0\"";

    @Autowired UpstreamClient client;

    @AfterEach
    void disable() {
        configure(NwpsGaugesAdapter.ID, false, Map.of("bbox", "-105.5,39.5,-104.5,40.2"));
        configure(UsdmAdapter.ID, false, Map.of());
    }

    private void resetState() {
        var nwps = client.stateStore(NwpsGaugesAdapter.ID);
        nwps.remove(NwpsGaugesAdapter.INDEX_KEY);
        for (String lid : List.of("BBCC2", "CGCC2", "DNVC2", "GSEC2")) {
            nwps.remove(NwpsGaugesAdapter.DETAIL_PREFIX + lid);
        }
        var usdm = client.stateStore(UsdmAdapter.ID);
        usdm.remove(SourceHttp.ETAG_STATE_PREFIX + UsdmAdapter.STATE_KEY);
        usdm.remove(SourceHttp.LAST_MODIFIED_STATE_PREFIX + UsdmAdapter.STATE_KEY);
    }

    /** The Denver list with DNVC2's observation replaced. */
    private static byte[] denver(String category, double stage, String validTime) {
        JsonNode root = HydroSources.JSON.readTree(AdapterFixtures.bytes("nwps-gauges", "gauges-denver.json"));
        for (JsonNode gauge : root.get("gauges")) {
            if ("DNVC2".equals(gauge.get("lid").asString())) {
                ObjectNode observed = (ObjectNode) gauge.get("status").get("observed");
                observed.put("floodCategory", category);
                observed.put("primary", stage);
                observed.put("validTime", validTime);
            }
        }
        return HydroSources.JSON.writeValueAsBytes(root);
    }

    private static void serveList(byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(NWPS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    private String headCategory() {
        return jdbc.queryForObject("select payload->>'floodCategory' from record_head where source_id = ? and"
                + " external_id = ?", String.class, NwpsGaugesAdapter.ID, "DNVC2");
    }

    @Test
    @DisplayName("every 079 source is registered and disabled")
    void registered() {
        List<String> ids = List.of("usgs-water", "nwps-gauges", "snotel", "usdm", "noaa-coops-tides",
                "ea-flood-warnings", "eccc-hydrometric");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
    }

    @Test
    @DisplayName("required parameters are enforced at PUT")
    void requiredParams() {
        for (String id : List.of("usgs-water", "nwps-gauges", "snotel", "noaa-coops-tides", "eccc-hydrometric")) {
            assertThatThrownBy(() -> configure(id, false, Map.of())).as(id).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> configure("usgs-water", false, Map.of("bbox", "-110,35,-100,45")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> configure("noaa-coops-tides", false, Map.of("stations", "TheBattery")))
                .isInstanceOf(ValidationException.class);
        configure("usgs-water", false, Map.of("bbox", "-105.5,39.5,-104.5,40.2"));
        configure("snotel", false, Map.of("states", List.of("CO")));
        configure("noaa-coops-tides", false, Map.of("stations", List.of("8518750")));
        configure("eccc-hydrometric", false, Map.of("bbox", "-76,45,-75,45.6"));
    }

    @Test
    @DisplayName("nwps-gauges: a flood-category change creates a version with floodCategory updated")
    void nwpsCategoryChange() {
        resetState();
        clock.setInstant(Instant.parse("2026-10-13T21:00:00Z"));
        configure(NwpsGaugesAdapter.ID, false, Map.of("bbox", "-105.5,39.5,-104.5,40.2"));
        UPSTREAM.stubFor(get(urlPathEqualTo(NWPS + "/DNVC2")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("nwps-gauges", "detail-DNVC2.json"))));
        UPSTREAM.stubFor(get(urlPathMatching(NWPS + "/(BBCC2|CGCC2|GSEC2)")).willReturn(aResponse().withStatus(404)));
        serveList(denver("no_flooding", 2.47, "2026-10-13T20:45:00Z"));
        assertThat(fetch(NwpsGaugesAdapter.ID).success()).isTrue();
        int before = versions(NwpsGaugesAdapter.ID, "DNVC2");
        assertThat(before).isPositive();
        assertThat(headCategory()).isEqualTo("no_flooding");

        clock.advance(Duration.ofMinutes(15));
        serveList(denver("minor", 11.4, "2026-10-13T21:00:00Z"));
        assertThat(fetch(NwpsGaugesAdapter.ID).success()).isTrue();
        assertThat(versions(NwpsGaugesAdapter.ID, "DNVC2")).isEqualTo(before + 1);
        assertThat(headCategory()).isEqualTo("minor");
        assertThat(jdbc.queryForObject("select payload->>'stageToMinor' from record_head where source_id = ? and"
                + " external_id = ?", String.class, NwpsGaugesAdapter.ID, "DNVC2")).isEqualTo("-0.4");
    }

    @Test
    @DisplayName("usdm: an unchanged week is a 304 with no versions")
    void usdmUnchangedWeek() {
        resetState();
        clock.setInstant(Instant.parse("2026-10-13T22:00:00Z"));
        configure(UsdmAdapter.ID, false, Map.of());
        UPSTREAM.stubFor(get(urlPathEqualTo(USDM + "usdm_current.json")).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("ETag", ETAG).withHeader("Last-Modified", "Thu, 08 Oct 2026 10:16:12 GMT")
                        .withBody(AdapterFixtures.bytes("usdm", "usdm_current-d3d4.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo(USDM + "usdm_current.json")).withHeader("If-None-Match", equalTo(ETAG))
                .willReturn(aResponse().withStatus(304)));
        UPSTREAM.stubFor(com.github.tomakehurst.wiremock.client.WireMock.head(urlPathEqualTo(USDM + "usdm_20261006.json")).willReturn(aResponse().withStatus(200)));
        assertThat(fetch(UsdmAdapter.ID).success()).isTrue();
        int before = versions(UsdmAdapter.ID, (ai.foresignal.ingest.PersistReason) null);
        assertThat(versions(UsdmAdapter.ID, "D3")).isPositive();

        clock.advance(Duration.ofHours(6));
        FetchOutcome second = fetch(UsdmAdapter.ID);
        assertThat(second.success()).isTrue();
        assertThat(second.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(second.persisted()).isZero();
        assertThat(versions(UsdmAdapter.ID, (ai.foresignal.ingest.PersistReason) null)).isEqualTo(before);
    }
}
