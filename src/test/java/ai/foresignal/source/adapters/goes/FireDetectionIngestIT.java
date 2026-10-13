package ai.foresignal.source.adapters.goes;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code fire-detection} from {@code goes-fdc} end to end (prompt 048): the captured scan through the real
 * fetch path and ingest writer. An identical scan writes no version; pixels are tombstoned after three
 * processed scans without them; a scan that was not processed (the download failed) counts for nothing.
 */
class FireDetectionIngestIT extends WeatherIngestSupport {

    static final String ID = GoesFdcAdapter.ID;
    static final String BUCKET = GoesFdcAdapter.BUCKET;
    static final String HOUR_21 = "ABI-L2-FDCC/2026/285/21/";
    static final String HOUR_22 = "ABI-L2-FDCC/2026/285/22/";
    static final long SIZE = 524_009L;

    /** A G19 FDCC key with the {@code s} time {@code hhmmss}. */
    static String key(String hhmmss) {
        return GoesFdcAdapterTest.key(hhmmss);
    }

    /** Lists {@code keys} under {@code prefix} after {@code startAfter}, and serves each as the captured scan. */
    static void scans(String prefix, String startAfter, String... keys) {
        Map<String, Long> objects = new LinkedHashMap<>();
        for (String k : keys) {
            objects.put(k, SIZE);
            UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + k))
                    .willReturn(aResponse().withBody(AdapterFixtures.bytes("fdc", GlmParserTest.FDC))));
        }
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(1).withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    /** Versions of {@code kind} from {@code goes-fdc}, of one persist reason. */
    int kindVersions(String kind, PersistReason reason) {
        Integer n = jdbc.queryForObject("select count(*) from record_version where source_id = ? and kind = ?"
                + " and persist_reason = ?", Integer.class, ID, kind, reason.code());
        return n == null ? 0 : n;
    }

    /** An empty listing for any other prefix. */
    static void emptyListings() {
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T21:59:30Z"));
        configure(ID, false, Map.of(GoesFdcAdapter.SCOPE_PARAM, "conus"));
        emptyListings();
    }

    @Test
    @DisplayName("25 detections; an identical scan versions nothing; tombstones after 3 processed misses, none in an outage")
    void lifecycle() {
        scans(HOUR_21, null, key("215117"));
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(kindVersions("fire-detection", PersistReason.FIRST)).isEqualTo(25);
        assertThat(live("observed_at", ID, "g19:927:692")).isEqualTo(Instant.parse("2026-10-12T21:51:17Z"));
        assertThat(head("event_time", ID, "g19:927:692")).isEqualTo(Instant.parse("2026-10-12T21:51:17Z"));

        // The same detections in the next scan: no new version, eventTime still the first sighting.
        scans(HOUR_21, key("215117"), key("215617"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(versions(ID, "g19:927:692")).isEqualTo(1);
        assertThat(head("event_time", ID, "g19:927:692")).isEqualTo(Instant.parse("2026-10-12T21:51:17Z"));

        // From now on the AOI scope keeps only the two Texas pixels: the other 23 miss.
        configure(ID, false, Map.of());
        clock.setInstant(Instant.parse("2026-10-12T22:02:30Z"));
        scans(HOUR_22, null, key("220117"));
        assertThat(fetch(ID).success()).isTrue();
        clock.setInstant(Instant.parse("2026-10-12T22:07:30Z"));
        scans(HOUR_22, key("220117"), key("220617"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(kindVersions("fire-detection", PersistReason.TOMBSTONE)).isZero();

        // An outage: the download fails. Not processed, so it is not the third miss.
        clock.setInstant(Instant.parse("2026-10-12T22:12:30Z"));
        scans(HOUR_22, key("220617"), key("221117"));
        UPSTREAM.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key("221117"))).atPriority(1)
                .willReturn(aResponse().withStatus(500)));
        assertThat(fetch(ID).success()).isFalse();
        assertThat(kindVersions("fire-detection", PersistReason.TOMBSTONE)).isZero();

        // The next processed scan is the third miss: 23 tombstones, the two Texas pixels stay.
        clock.setInstant(Instant.parse("2026-10-12T22:17:30Z"));
        scans(HOUR_22, key("220617"), key("221117"), key("221617"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(kindVersions("fire-detection", PersistReason.TOMBSTONE)).isEqualTo(23);
        assertThat(tombstoned(ID, "g19:640:394")).isTrue();
        assertThat(tombstoned(ID, "g19:927:692")).isFalse();
        assertThat(tombstoned(ID, "g19:608:594")).isFalse();
        assertThat(live("observed_at", ID, "g19:640:394")).isEqualTo(Instant.parse("2026-10-12T22:16:17Z"));
    }
}
