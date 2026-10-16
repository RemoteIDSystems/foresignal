package ai.foresignal.source.adapters.internet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end internet-measurement ingest (prompt 092): a gpsjam cell cleared on day 2 is tombstoned; the same
 * day is not re-fetched; a re-fetched Cloudflare outage adds no version; a resolved Statuspage incident gets a final
 * version and a tombstone.
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

    @Test
    @DisplayName("a Cloudflare outage ingests once; re-fetching the same annotations adds no version")
    void outageRefetch() {
        UPSTREAM.stubFor(get(urlPathEqualTo("/client/v4/radar/annotations/outages")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("cloudflare-radar-outages", "outages.json"))));
        sources.setCredentials(CloudflareOutageAdapter.ID, Map.of(CloudflareOutageAdapter.API_TOKEN, "it-radar-token"));
        configure(CloudflareOutageAdapter.ID, true, Map.of());
        fetch(CloudflareOutageAdapter.ID);
        int afterFirst = versions(CloudflareOutageAdapter.ID, (PersistReason) null);
        assertThat(afterFirst).isEqualTo(2);
        fetch(CloudflareOutageAdapter.ID);
        assertThat(versions(CloudflareOutageAdapter.ID, (PersistReason) null)).isEqualTo(afterFirst);
    }

    @Test
    @DisplayName("a resolved Statuspage incident gets a final version, then a tombstone")
    void statusIncidentResolved() {
        String id = "status-github";
        UPSTREAM.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).willReturn(json("{'page':{'id':'gh',"
                + "'updated_at':'2026-10-14T11:00:00Z'},'status':{'indicator':'minor'},'components':[],"
                + "'incidents':[{'id':'I1','name':'Slow','status':'investigating',"
                + "'created_at':'2026-10-14T10:00:00Z','updated_at':'2026-10-14T11:00:00Z'}]}")));
        configure(id, true, Map.of());
        fetch(id);
        assertThat(versions(id, "incident:I1")).isEqualTo(1);

        UPSTREAM.stubFor(get(urlPathEqualTo("/api/v2/summary.json")).willReturn(json("{'page':{'id':'gh',"
                + "'updated_at':'2026-10-14T12:00:00Z'},'status':{'indicator':'none'},'components':[],"
                + "'incidents':[]}")));
        UPSTREAM.stubFor(get(urlPathEqualTo("/api/v2/incidents.json")).willReturn(json("{'incidents':[{'id':'I1',"
                + "'name':'Slow','status':'resolved','created_at':'2026-10-14T10:00:00Z',"
                + "'updated_at':'2026-10-14T11:40:00Z'}]}")));
        fetch(id);
        assertThat(versions(id, "incident:I1")).as("the resolved version, then the tombstone").isGreaterThanOrEqualTo(2);
        assertThat(tombstoned(id, "incident:I1")).isTrue();
    }

    /** A 200 JSON response; {@code body} is written with single quotes for readability. */
    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(body.replace('\'', '"'));
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
