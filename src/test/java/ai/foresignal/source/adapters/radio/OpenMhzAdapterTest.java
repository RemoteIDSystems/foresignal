package ai.foresignal.source.adapters.radio;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code openmhz-systems} against WireMock (prompt 082): the challenge page OpenMHz really serves (403,
 * {@code cf-mitigated: challenge}, HTML) is {@code BLOCKED_CHALLENGE}, as is an HTML body without the header or with a
 * 200; exactly one request per run with Foresignal's User-Agent, never a retry with another one; a JSON catalog maps.
 */
@IntegrationTest
class OpenMhzAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T00:45:00Z");
    static final String SYSTEMS = "/api.openmhz.com/systems";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    OpenMhzAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = new OpenMhzAdapter(baseUrl());
        harness.reset(adapter.id());
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    @Test
    @DisplayName("the real challenge page (403 + cf-mitigated) is BLOCKED_CHALLENGE after one request, no other UA")
    void challenge() {
        WIREMOCK.stubFor(get(urlPathEqualTo(SYSTEMS)).willReturn(aResponse().withStatus(403)
                .withHeader("Content-Type", "text/html; charset=UTF-8").withHeader("cf-mitigated", "challenge")
                .withBody(AdapterFixtures.bytes("openmhz-systems", "challenge-403.html"))));

        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamHttpException.class)
                .satisfies(e -> {
                    UpstreamHttpException http = (UpstreamHttpException) e;
                    assertThat(http.getStatus()).isEqualTo(403);
                    assertThat(http.getReason()).isEqualTo(UpstreamHttpException.REASON_BLOCKED_CHALLENGE);
                    assertThat(http.isBlockedChallenge()).isTrue();
                    assertThat(http.getMessage()).contains("BLOCKED_CHALLENGE");
                });
        var requests = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(SYSTEMS)));
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getHeader("User-Agent")).startsWith("Foresignal/").doesNotContain("Mozilla");
    }

    @Test
    @DisplayName("an HTML 403 without the header, and an HTML 200, are BLOCKED_CHALLENGE too")
    void htmlWithoutHeader() {
        WIREMOCK.stubFor(get(urlPathEqualTo(SYSTEMS)).willReturn(aResponse().withStatus(403)
                .withBody("<!DOCTYPE html><html><title>Just a moment...</title></html>")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .satisfies(e -> assertThat(((UpstreamHttpException) e).isBlockedChallenge()).isTrue());

        harness.reset(adapter.id());
        WIREMOCK.stubFor(get(urlPathEqualTo(SYSTEMS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withBody("<html>challenge</html>")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .satisfies(e -> assertThat(((UpstreamHttpException) e).isBlockedChallenge()).isTrue());
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(SYSTEMS)).withHeader("User-Agent", matching("Foresignal/.*")));
    }

    @Test
    @DisplayName("a JSON 404 keeps its plain status, without a challenge reason")
    void plainError() {
        WIREMOCK.stubFor(get(urlPathEqualTo(SYSTEMS)).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/json").withBody("{\"error\":\"not found\"}")));

        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .satisfies(e -> assertThat(((UpstreamHttpException) e).getReason()).isNull());
    }

    @Test
    @DisplayName("if OpenMHz admits API clients: a systems catalog maps to scanner records; the descriptor validates")
    void catalog() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(SYSTEMS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(("{\"success\":true,\"systems\":["
                        + "{\"shortName\":\"sys1\",\"name\":\"County Trunk\",\"location\":\"Somewhere\",\"state\":\"VA\","
                        + "\"county\":\"Fairfax\",\"lastActive\":\"2026-10-14T00:40:00.000Z\",\"clientCount\":12,"
                        + "\"lat\":38.8,\"lng\":-77.3},{\"name\":\"no short name\"}]}").getBytes(StandardCharsets.UTF_8))));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(result.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("sys1");
            assertThat(r.payload()).containsEntry("name", "County Trunk").containsEntry("state", "VA")
                    .containsEntry("lastActive", "2026-10-14T00:40:00Z").containsEntry("clientCount", 12);
            assertThat(r.geometry()).isNotNull();
        });
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), result.records().stream()
                .map(NormalizedRecord.class::cast).toList());
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(OpenMhzParser.looksLikeHtml("  <html>".getBytes(), null)).isTrue();
        assertThat(OpenMhzParser.looksLikeHtml("[]".getBytes(), "application/json")).isFalse();
    }
}
