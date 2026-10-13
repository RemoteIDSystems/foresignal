package ai.foresignal.source.adapters.mrms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlacesAoiService;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code mrms-probsevere} end to end (prompt 047): the captured file through the real fetch path, the
 * default AOI (no places: the Texas box), then a place that shrinks the AOI, which re-filters the same
 * file and tombstones the objects now outside.
 */
class ProbSevereIngestIT extends WeatherIngestSupport {

    static final String ID = ProbSevereAdapter.ID;
    static final String BUCKET = ProbSevereAdapter.BUCKET;
    static final String PREFIX = "ProbSevere/20261012/";
    static final String KEY = PREFIX + ProbSevereParserTest.FILE;

    @Autowired PlaceService places;
    @Autowired PlacesAoiService aoi;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T23:59:50Z"));
        configure(ID, false, Map.of());
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(1)
                .withQueryParam("prefix", equalTo(PREFIX))
                .withQueryParam("start-after", com.github.tomakehurst.wiremock.client.WireMock.absent())
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, PREFIX, Map.of(KEY, 264_916L), false))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + KEY))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes("probsevere", ProbSevereParserTest.FILE))));
    }

    @Test
    @DisplayName("default AOI: the 10 Texas objects; a place re-filters the same file and tombstones the rest")
    void aoiChangeRefilters() {
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(10);
        assertThat(live("observed_at", ID, "981738")).isEqualTo(Instant.parse("2026-10-12T23:58:41Z"));

        Set<String> before = ids(ProbSevereParserTest.TEXAS);
        places.create(JsonMapper.builder().build().readTree("{\"id\":\"austin\",\"name\":\"Austin\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-97.74,30.27]},\"radiusKm\":20}"));
        Set<String> after = ids(aoi.envelope());
        assertThat(after).isNotEmpty().isSubsetOf(before);

        FetchOutcome second = fetch(ID);

        assertThat(second.success()).isTrue();
        Set<String> gone = new TreeSet<>(before);
        gone.removeAll(after);
        assertThat(second.byReason().get(PersistReason.TOMBSTONE)).isEqualTo(gone.size());
        for (String id : gone) {
            assertThat(tombstoned(ID, id)).as(id).isTrue();
        }
        for (String id : after) {
            assertThat(tombstoned(ID, id)).as(id).isFalse();
        }
    }

    private static Set<String> ids(double[] envelope) {
        return new TreeSet<>(ProbSevereParser.parse(AdapterFixtures.bytes("probsevere", ProbSevereParserTest.FILE),
                envelope).ids());
    }
}
