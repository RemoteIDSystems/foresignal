package ai.foresignal.source.adapters.maritime;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code nga-msi-warnings} against a stubbed msi.nga.mil through the real upstream client (prompt 088): Foresignal's
 * own User-Agent and no Referer on the request, the records of the captured list, a tombstone once a warning leaves
 * the list, no tombstones from an empty list, unparseable bodies, and the descriptor.
 */
@IntegrationTest
class NgaMsiAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T05:30:00Z");
    static final String PATH = "/msi.nga.mil/api/publications/broadcast-warn";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    NgaMsiAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new NgaMsiAdapter(baseUrl());
        harness.reset(adapter.id(), LiveIdSet.LIVE_PREFIX + NgaMsiAdapter.LIVE_SCOPE,
                LiveIdSet.GONE_PREFIX + NgaMsiAdapter.LIVE_SCOPE, SourceHttp.ETAG_STATE_PREFIX + NgaMsiAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + NgaMsiAdapter.STATE_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private static void serve(byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("status", equalTo("active"))
                .withQueryParam("output", equalTo("json")).willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withHeader("Cache-Control", "max-age=8186").withBody(body)));
    }

    private static byte[] capture() {
        return AdapterFixtures.bytes(NgaMsiAdapter.ID, "broadcast-warn-active.json");
    }

    /** The capture without the warning {@code navArea/msgYear/msgNumber}. */
    private static byte[] without(String area, int year, int number) {
        ObjectNode root = (ObjectNode) JSON.readTree(capture());
        ArrayNode list = (ArrayNode) root.get("broadcast-warn");
        for (int i = 0; i < list.size(); i++) {
            var w = list.get(i);
            if (w.get("navArea").asString().equals(area) && w.get("msgYear").intValue() == year
                    && w.get("msgNumber").intValue() == number) {
                list.remove(i);
                break;
            }
        }
        return root.toString().getBytes();
    }

    private FetchResult fetch() throws Exception {
        return adapter.fetch(harness.context(adapter, Map.of()));
    }

    @Test
    @DisplayName("our User-Agent, no Referer and no browser headers; the captured list maps to 30 complete records")
    void request() throws Exception {
        serve(capture());
        FetchResult result = fetch();
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(30).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("User-Agent", containing("Foresignal/"))
                .withHeader("User-Agent", com.github.tomakehurst.wiremock.client.WireMock.notContaining("Mozilla"))
                .withHeader("Referer", absent())
                .withHeader("Origin", absent()));
    }

    @Test
    @DisplayName("a warning that leaves the active list is tombstoned once, and only then")
    void tombstoneOnRemoval() throws Exception {
        serve(capture());
        assertThat(fetch().records()).noneMatch(NormalizedRecord::tombstone);

        serve(without("4", 2024, 517));
        FetchResult second = fetch();
        assertThat(second.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("4/2024/517");
        assertThat(second.records()).filteredOn(r -> !r.tombstone()).hasSize(29);
        NormalizedRecord tombstone = second.records().stream().filter(NormalizedRecord::tombstone).findFirst()
                .orElseThrow();
        assertThat(tombstone.kind()).isEqualTo(NgaMsiParser.KIND);
        assertThat(tombstone.observedAt()).isEqualTo(NOW);

        harness.clock.advance(Duration.ofMinutes(15));
        serve(without("4", 2024, 517));
        assertThat(fetch().records()).as("retried once, then no more").filteredOn(NormalizedRecord::tombstone)
                .hasSize(1);
        serve(without("4", 2024, 517));
        assertThat(fetch().records()).noneMatch(NormalizedRecord::tombstone);
    }

    @Test
    @DisplayName("an empty list tombstones nothing and is not a complete set")
    void emptyList() throws Exception {
        serve(capture());
        fetch();
        serve("{\"broadcast-warn\":[]}".getBytes());
        FetchResult result = fetch();
        assertThat(result.records()).isEmpty();
        assertThat(result.complete()).isFalse();
        serve(capture());
        assertThat(fetch().records()).noneMatch(NormalizedRecord::tombstone);
    }

    @Test
    @DisplayName("a body that is not the warning list fails the run and forgets the validators")
    void unparseable() {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("ETag", "\"v1\"").withBody("<html>blocked</html>")));
        assertThatThrownBy(this::fetch).isInstanceOf(UpstreamFormatException.class);
        assertThat(harness.client.stateStore(adapter.id()).get(SourceHttp.ETAG_STATE_PREFIX + NgaMsiAdapter.STATE_KEY))
                .isEmpty();
    }

    @Test
    @DisplayName("a 304 is unchanged")
    void notModified() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200).withHeader("ETag", "\"v1\"")
                .withBody(capture())));
        fetch();
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));
        assertThat(fetch().notModified()).isTrue();
    }

    @Test
    @DisplayName("the descriptor: US-PD, event retention, 15 min, 2/min and 200/day, embedded text")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 200));
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(1825));
        assertThat(d.embedFields()).containsExactly("text");
        assertThat(d.searchFields()).containsExactly("text", "authority");
        assertThat(d.titleField()).isEqualTo("title");
        assertThat(d.userAgentOverride()).isNull();
    }
}
