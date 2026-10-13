package ai.foresignal.source.adapters.spc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.retention.RetentionResolver;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code spc-storm-reports} end to end (prompt 053): today's and yesterday's files through the real fetch path and
 * ingest writer; point records with the report time as observedAt and eventTime, the title, the resolved
 * {@code storm-report} retention, a 304 re-fetch, and no duplicate versions when the same reports come again.
 */
class StormReportsIngestIT extends WeatherIngestSupport {

    static final String ID = SpcStormReportsAdapter.ID;

    @Autowired RetentionResolver retention;
    @Autowired ai.foresignal.collect.UpstreamClient client;

    private static void serve(String path, String fixture, String etag, String lastModified) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", absent())
                .willReturn(aResponse().withStatus(200).withHeader("ETag", etag).withHeader("Last-Modified", lastModified)
                        .withBody(AdapterFixtures.bytes("spc-storm-reports", fixture))));
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).withHeader("If-None-Match", equalTo(etag))
                .willReturn(aResponse().withStatus(304)));
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-13T04:45:00Z"));
        configure(ID, false, Map.of());
        serve("/climo/reports/today.csv", "today-20261012.csv", "\"t1\"", "Tue, 13 Oct 2026 04:38:06 GMT");
        serve("/climo/reports/yesterday.csv", "yesterday-20261011.csv", "\"y1\"", "Mon, 12 Oct 2026 23:51:04 GMT");
    }

    @Test
    @DisplayName("59 reports with point geometry, report times and titles; storm-report kept 30 d; a re-fetch is NOT_MODIFIED")
    void ingest() {
        FetchOutcome first = fetch(ID);

        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(59);
        String muleshoe = SpcStormReportsParser.externalId("wind", java.time.LocalDate.parse("2026-10-12"), "0353", "34.21",
                "-102.74", "2 SSW Muleshoe");
        assertThat(live("observed_at", ID, muleshoe)).isEqualTo(Instant.parse("2026-10-13T03:53:00Z"));
        assertThat(head("event_time", ID, muleshoe)).isEqualTo(Instant.parse("2026-10-13T03:53:00Z"));
        String title = jdbc.queryForObject("select title from record_head where source_id = ? and external_id = ?",
                String.class, ID, muleshoe);
        assertThat(title).isEqualTo("Wind 58 mph — 2 SSW Muleshoe, TX");
        Integer points = jdbc.queryForObject("select count(*) from record_live where source_id = ?"
                + " and GeometryType(geom) = 'POINT'", Integer.class, ID);
        assertThat(points).isEqualTo(59);
        assertThat(retention.window(ID, "storm-report")).isEqualTo(Duration.ofDays(30));

        FetchOutcome second = fetch(ID);
        assertThat(second.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(59);
    }

    @Test
    @DisplayName("the same reports served again under new validators add no versions and tombstone nothing")
    void noDuplicates() {
        fetch(ID);
        UPSTREAM.resetAll();
        serve("/climo/reports/today.csv", "today-20261012.csv", "\"t2\"", "Tue, 13 Oct 2026 04:38:06 GMT");
        serve("/climo/reports/yesterday.csv", "yesterday-20261011.csv", "\"y2\"", "Mon, 12 Oct 2026 23:51:04 GMT");
        ai.foresignal.source.spi.AdapterStateStore state = client.stateStore(ID);
        ai.foresignal.source.spi.SourceHttp.forgetValidators(state, "today");
        ai.foresignal.source.spi.SourceHttp.forgetValidators(state, "yesterday");
        state.remove(SpcStormReportsAdapter.YESTERDAY_AT);
        clock.setInstant(Instant.parse("2026-10-13T05:50:00Z"));

        FetchOutcome again = fetch(ID);

        assertThat(again.success()).isTrue();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(59);
        Integer tombstones = jdbc.queryForObject("select count(*) from record_head where source_id = ? and tombstone",
                Integer.class, ID);
        assertThat(tombstones).isZero();
    }
}
