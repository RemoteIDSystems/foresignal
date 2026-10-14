package ai.foresignal.source.adapters.radio;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
 * {@code kiwisdr} through the real upstream client against WireMock (prompt 082): plain HTTP to the list's path, the
 * conditional GET round trip (200 with an ETag, then a 304), tombstones for a receiver gone from a plausible list and
 * none from a list that shrank by more than half, a format error forgetting the validators, and the descriptor.
 */
@IntegrationTest
class KiwiSdrAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T00:45:00Z");
    static final String LIST = "/rx.linkfanel.net/kiwisdr_com.js";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    KiwiSdrAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = RadioTestAdapters.kiwi(baseUrl());
        harness.reset(adapter.id(), SourceHttp.ETAG_STATE_PREFIX + KiwiSdrAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + KiwiSdrAdapter.STATE_KEY,
                LiveIdSet.LIVE_PREFIX + KiwiSdrAdapter.STATE_KEY, LiveIdSet.GONE_PREFIX + KiwiSdrAdapter.STATE_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    static String list(int count) {
        StringBuilder body = new StringBuilder("// generated\nvar kiwisdr_com =\n[\n");
        for (int i = 1; i <= count; i++) {
            body.append("{\"id\":\"rx").append(i).append("\",\"name\":\"Receiver ").append(i)
                    .append("\",\"gps\":\"(50.0, 8.0)\",\"offline\":\"no\"},\n");
        }
        return body.append("]\n;\n").toString();
    }

    static void serve(String etag, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(LIST)).willReturn(aResponse().withStatus(200).withHeader("ETag", etag)
                .withHeader("Content-Type", "application/javascript").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    @DisplayName("the descriptor validates: plain HTTP, 24 h (min 6 h), 1/min and 4/day, RECEIVED, users and snr volatile")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(KiwiSdrAdapter.LIST_URL).startsWith("http://");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofHours(24));
        assertThat(d.minInterval()).isEqualTo(Duration.ofHours(6));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(1, 4));
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.volatileFields()).containsExactly("users", "snr");
    }

    @Test
    @DisplayName("200 with an ETag, then If-None-Match and a 304 is unchanged; the User-Agent is Foresignal's")
    void conditionalGet() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(LIST)).withHeader("If-None-Match", absent()).willReturn(aResponse()
                .withStatus(200).withHeader("ETag", "\"e1\"")
                .withBody(AdapterFixtures.bytes("kiwisdr", "kiwisdr_com.js"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(LIST)).withHeader("If-None-Match", equalTo("\"e1\""))
                .willReturn(aResponse().withStatus(304)));

        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(first.records()).hasSize(30);
        assertThat(first.complete()).isTrue();
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(LIST)).withHeader("User-Agent", matching("Foresignal/.*")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(LIST)).withHeader("If-None-Match", equalTo("\"e1\"")));
    }

    @Test
    @DisplayName("a receiver absent from a list at least half the previous size is tombstoned")
    void tombstone() throws Exception {
        serve("\"a\"", list(10));
        adapter.fetch(harness.context(adapter, Map.of()));
        serve("\"b\"", list(9));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(result.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("rx10");
    }

    @Test
    @DisplayName("a list under half the previous size tombstones nothing and keeps the previous live set")
    void shrunk() throws Exception {
        serve("\"a\"", list(10));
        adapter.fetch(harness.context(adapter, Map.of()));
        serve("\"b\"", list(4));

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(result.records()).noneMatch(NormalizedRecord::tombstone).hasSize(4);
        assertThat(result.complete()).isFalse();
        assertThat(LiveIdSet.read(harness.client.stateStore(adapter.id()), LiveIdSet.LIVE_PREFIX
                + KiwiSdrAdapter.STATE_KEY)).hasSize(10);
    }

    @Test
    @DisplayName("an unparseable body throws and forgets the validators, so the next run reads it again")
    void formatError() {
        serve("\"bad\"", "<html>maintenance</html>");

        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(UpstreamFormatException.class);
        assertThat(harness.client.stateStore(adapter.id()).get(SourceHttp.ETAG_STATE_PREFIX + KiwiSdrAdapter.STATE_KEY))
                .isEmpty();
    }
}
