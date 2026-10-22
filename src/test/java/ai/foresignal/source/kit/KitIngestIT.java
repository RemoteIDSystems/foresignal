package ai.foresignal.source.kit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.kit.feature.KitFeatureAdapter;
import ai.foresignal.source.kit.feed.KitFeedAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The kit sources end to end through the real fetch path and ingest writer (prompt 073): both families register their
 * specs disabled; {@code feature-wfigs-incidents} ingests the captured WFIGS pages and tombstones an incident missing
 * from two consecutive complete fetches; {@code feed-ecdc-threats} ingests its items and leaves absent items alone.
 */
class KitIngestIT extends WeatherIngestSupport {

    static final String WFIGS = "feature-wfigs-incidents";
    static final String LAYER = "/services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/"
            + "WFIGS_Incident_Locations_Current/FeatureServer/0";
    static final List<String> PAGE_TWO = List.of("{942CCFF6-7EF4-42BC-AC49-CB867C89D512}",
            "{A65C47FE-6BC0-4562-8D6E-FA1B403DE09D}", "{F6CFE8BA-B59D-4733-9E58-15034B31F208}");
    static final String ECDC = "feed-ecdc-threats";
    static final String FEED = "/www.ecdc.europa.eu/en/taxonomy/term/1307/feed";

    @Autowired SourceRegistry registry;

    @AfterEach
    void disable() {
        configure(WFIGS, false, Map.of());
        configure(ECDC, false, Map.of());
    }

    void layer(long lastEdit) {
        String body = AdapterFixtures.text(WFIGS, "layer.json").replace("1791913696835", Long.toString(lastEdit));
        UPSTREAM.stubFor(get(urlPathEqualTo(LAYER)).withQueryParam("f", equalTo("json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    void page(int offset, String body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(LAYER + "/query")).withQueryParam("resultOffset",
                equalTo(String.valueOf(offset))).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(body)));
    }

    int heads(String sourceId) {
        Integer n = jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class,
                sourceId);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("both families register every spec, and every kit source is disabled under the test profile")
    void registered() {
        // The test profile seeds every source disabled (foresignal.sources.default-enable=false, prompt 146); in
        // production the kit sources, which need no key, start enabled (prompt 145).
        List<String> ids = List.of(WFIGS, "feed-cisa-advisories", "feed-ncsc-uk", "feed-nhc-atlantic",
                "feed-nhc-eastpacific", ECDC, "feed-who-news");
        for (String id : ids) {
            assertThat(registry.contains(id)).as(id).isTrue();
            assertThat(sources.view(id).enabled()).as(id).isFalse();
        }
        assertThat(registry.find(WFIGS).orElseThrow().adapter()).isInstanceOf(KitFeatureAdapter.class);
        assertThat(registry.find(ECDC).orElseThrow().adapter()).isInstanceOf(KitFeedAdapter.class);
    }

    @Test
    @DisplayName("WFIGS: 8 incidents from two pages; an unchanged layer is not queried; absent twice is tombstoned")
    void wfigs() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        configure(WFIGS, true, Map.of());
        String page1 = AdapterFixtures.text(WFIGS, "query-page-1.json");
        String page2 = AdapterFixtures.text(WFIGS, "query-page-2.json");
        layer(1791913696835L);
        page(0, page1);
        page(5, page2);
        FetchOutcome first = fetch(WFIGS);
        assertThat(first.success()).isTrue();
        assertThat(heads(WFIGS)).isEqualTo(8);
        assertThat(jdbc.queryForObject("select payload->>'name' from record_head where source_id = ? and external_id"
                + " = ?", String.class, WFIGS, "{1B4FF51E-95DF-4E25-A871-BB406059EEA2}")).isEqualTo("D1 Pondo RX");

        clock.advance(Duration.ofMinutes(10));
        UPSTREAM.resetRequests();
        assertThat(fetch(WFIGS).success()).isTrue();
        assertThat(UPSTREAM.findAll(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                urlPathEqualTo(LAYER + "/query")))).as("unchanged layer: no query").isEmpty();

        // The layer changed and the second page's incidents are gone: one miss is not enough, two are.
        String lastPage = page1.replace("\"exceededTransferLimit\": true", "\"exceededTransferLimit\": false");
        clock.advance(Duration.ofMinutes(10));
        layer(1791914000000L);
        page(0, lastPage);
        assertThat(fetch(WFIGS).success()).isTrue();
        for (String id : PAGE_TWO) {
            assertThat(tombstoned(WFIGS, id)).as(id).isFalse();
        }
        clock.advance(Duration.ofMinutes(10));
        layer(1791914300000L);
        assertThat(fetch(WFIGS).success()).isTrue();
        for (String id : PAGE_TWO) {
            assertThat(tombstoned(WFIGS, id)).as(id).isTrue();
        }
        assertThat(tombstoned(WFIGS, "{1B4FF51E-95DF-4E25-A871-BB406059EEA2}")).isFalse();
    }

    @Test
    @DisplayName("feed: 10 items ingested; a later window without most of them tombstones nothing")
    void feed() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        configure(ECDC, true, Map.of());
        String full = AdapterFixtures.text("kit-feeds", "ecdc-1307.xml");
        UPSTREAM.stubFor(get(urlPathEqualTo(FEED)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/rss+xml; charset=utf-8").withBody(full)));
        assertThat(fetch(ECDC).success()).isTrue();
        assertThat(heads(ECDC)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select payload->>'feedTitle' from record_head where source_id = ? limit 1",
                String.class, ECDC)).isEqualTo("News");

        UPSTREAM.resetAll();
        UPSTREAM.stubFor(get(urlPathEqualTo(FEED)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/rss+xml; charset=utf-8").withBody(firstItems(full, 3))));
        clock.advance(Duration.ofMinutes(15));
        assertThat(fetch(ECDC).success()).isTrue();
        assertThat(heads(ECDC)).isEqualTo(10);
        Integer tombstones = jdbc.queryForObject("select count(*) from record_head where source_id = ? and tombstone",
                Integer.class, ECDC);
        assertThat(tombstones).as("feeds are windows: absence never tombstones").isZero();
    }

    /** {@code rss} with only its first {@code n} items. */
    static String firstItems(String rss, int n) {
        Matcher m = Pattern.compile("(?s)<item>.*?</item>").matcher(rss);
        StringBuilder kept = new StringBuilder();
        int count = 0;
        int end = 0;
        int start = -1;
        while (m.find()) {
            if (start < 0) {
                start = m.start();
            }
            if (count < n) {
                kept.append(m.group()).append('\n');
            }
            count++;
            end = m.end();
        }
        return rss.substring(0, start) + kept + rss.substring(end);
    }
}
