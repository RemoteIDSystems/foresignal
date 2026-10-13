package ai.foresignal.source.adapters.spc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code spc-outlooks} end to end (prompt 047): 17 layers through the real fetch path; the time base,
 * the title, and an all-304 re-fetch.
 */
class SpcOutlookIngestIT extends WeatherIngestSupport {

    static final String ID = SpcOutlooksAdapter.ID;
    static final String ETAG = "\"e1\"";

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T00:05:00Z"));
        configure(ID, false, Map.of());
        for (SpcOutlooksAdapter.Layer layer : SpcOutlooksAdapter.LAYERS) {
            String path = "/products/outlook/" + layer.file();
            UPSTREAM.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", absent())
                    .willReturn(aResponse().withStatus(200).withHeader("ETag", ETAG)
                            .withBody(AdapterFixtures.bytes("spc", layer.file()))));
            UPSTREAM.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", equalTo(ETAG))
                    .willReturn(aResponse().withStatus(304)));
        }
    }

    @Test
    @DisplayName("12 risk areas with ISSUE/VALID/EXPIRE as observedAt/eventTime/validUntil; a re-fetch is NOT_MODIFIED")
    void ingest() {
        FetchOutcome first = fetch(ID);

        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(12);
        assertThat(live("observed_at", ID, "day2:cat:SLGT")).isEqualTo(Instant.parse("2026-10-12T16:58:00Z"));
        assertThat(head("event_time", ID, "day2:cat:SLGT")).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(live("valid_until", ID, "day2:cat:SLGT")).isEqualTo(Instant.parse("2026-10-14T12:00:00Z"));
        String title = jdbc.queryForObject("select title from record_head where source_id = ? and external_id = ?",
                String.class, ID, "day2:cat:SLGT");
        assertThat(title).isEqualTo("Day 2 Slight Risk");

        FetchOutcome second = fetch(ID);
        assertThat(second.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(12);
    }
}
