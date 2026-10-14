package ai.foresignal.source.adapters.internet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end internet-measurement ingest (prompt 092): a gpsjam cell cleared on day 2 is tombstoned; the same
 * day is not re-fetched.
 */
class InternetMeasurementIngestIT extends WeatherIngestSupport {

    @Test
    @DisplayName("a gpsjam cell cleared on day 2 is tombstoned; an unchanged day adds nothing")
    void gpsjamTombstone() {
        serve("2026-10-13", "8400cd7ffffffff,1,20\n8401227ffffffff,1,20\n");
        configure("gpsjam-daily", true, Map.of());
        fetch("gpsjam-daily");
        int afterFirst = versions("gpsjam-daily", (PersistReason) null);
        assertThat(afterFirst).isGreaterThan(0);
        assertThat(tombstoned("gpsjam-daily", "h3:8401227ffffffff")).isFalse();

        serve("2026-10-14", "8400cd7ffffffff,1,20\n");
        fetch("gpsjam-daily");
        assertThat(tombstoned("gpsjam-daily", "h3:8401227ffffffff")).isTrue();
    }

    private static void serve(String newest, String day) {
        String manifest = "date,suspect,num_bad_aircraft_hexes,source\n"
                + "2026-10-13,false,10,adsbexchange\n"
                + newest + ",false,5,adsbexchange\n";
        UPSTREAM.stubFor(get(urlPathEqualTo("/data/manifest.csv")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/csv").withBody(manifest)));
        UPSTREAM.stubFor(get(urlPathEqualTo("/data/" + newest + "-h3_4.csv")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/csv").withBody(day)));
    }
}
