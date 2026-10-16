package ai.foresignal.source.adapters.meteoalarm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link MeteoAlarmAdapter} against a stubbed JSON API (prompt 089, post-review fixes): one malformed warning (no
 * {@code sent}) is skipped and counted, and the rest of the country's warnings are still read.
 */
@IntegrationTest
class MeteoAlarmAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    private void serve(String slug, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/warnings/feeds-" + slug)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    @Test
    @DisplayName("a warning without sent is skipped; the country's other warnings are kept")
    void malformedWarningSkipped() throws Exception {
        AdapterHttpHarness harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(NOW), Budget.of(1000, 100_000));
        ObjectNode clean = (ObjectNode) jsonMapper.readTree(AdapterFixtures.bytes("meteoalarm-greece",
                "warnings.json"));
        ObjectNode broken = clean.deepCopy();
        ((ArrayNode) broken.get("warnings")).insertObject(0).putObject("alert").put("identifier", "no-sent");

        MeteoAlarmAdapter reference = new MeteoAlarmAdapter(new MeteoAlarmCountries.Country("test-clean", "GR",
                "Test clean"), baseUrl());
        MeteoAlarmAdapter adapter = new MeteoAlarmAdapter(new MeteoAlarmCountries.Country("test-broken", "GR",
                "Test broken"), baseUrl());
        for (String id : new String[] {reference.id(), adapter.id()}) {
            harness.reset(id, CapThreadResolver.STATE_KEY, SourceHttp.ETAG_STATE_PREFIX + "meteoalarm:index",
                    SourceHttp.LAST_MODIFIED_STATE_PREFIX + "meteoalarm:index");
        }
        serve("test-clean", clean.toString());
        serve("test-broken", broken.toString());

        FetchResult expected = reference.fetch(harness.context(reference, Map.of()));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(expected.records()).isNotEmpty();
        assertThat(result.records()).hasSameSizeAs(expected.records());
    }
}
