package ai.foresignal.plugin.feed;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.inbound.format.GeoJsonIngestMapper;
import ai.foresignal.plugin.PluginApprovals;
import ai.foresignal.plugin.PluginCatalog;
import ai.foresignal.plugin.PluginException;
import ai.foresignal.plugin.PluginLimits;
import ai.foresignal.plugin.PluginOutputReader;
import ai.foresignal.plugin.PluginRuntime;
import ai.foresignal.plugin.PluginSettings;
import ai.foresignal.plugin.PluginTestFiles;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A {@code wasm-feed} source through the real upstream client against WireMock and the real example plugin
 * (prompt 141): fetch, plugin parse and records; the conditional GET and 304; {@code complete} only without
 * rejections; the rejected-item counter; refusals while plugins are off or the plugin is not enabled; the URL gate
 * before any request; a refused redirect.
 */
@IntegrationTest
class WasmFeedAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-18T06:00:00Z");
    static final String ID = "wasm-test-stations";
    static final String PATH = "/stations.example/stations.geojson";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    @TempDir
    Path plugins;

    AdapterHttpHarness harness;
    SimpleMeterRegistry meters;
    AtomicBoolean pluginsOn;
    PluginApprovals approvals;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        harness.reset(ID, SourceHttp.ETAG_STATE_PREFIX + WasmFeedAdapter.STATE_KEY,
                SourceHttp.LAST_MODIFIED_STATE_PREFIX + WasmFeedAdapter.STATE_KEY);
        meters = new SimpleMeterRegistry();
        PluginTestFiles.install(plugins, PluginTestFiles.PASSTHROUGH);
        pluginsOn = new AtomicBoolean(true);
        approvals = PluginApprovals.NONE.with(PluginTestFiles.PASSTHROUGH, PluginTestFiles.PASSTHROUGH_SHA256, NOW);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(ID);
    }

    PluginRuntime runtime() {
        return new PluginRuntime(new PluginCatalog(plugins), () -> new PluginSettings(pluginsOn.get()),
                () -> approvals, PluginLimits.DEFAULTS, new MutableClock(NOW),
                new PluginOutputReader(new GeoJsonIngestMapper(jsonMapper), jsonMapper), null);
    }

    static WasmFeedSpec spec(String url, Boolean conditional) {
        return new WasmFeedSpec(ID, "Test stations", null, PluginTestFiles.PASSTHROUGH, url, List.of("station"),
                null, null, "CC0-1.0", null, null, null, null, "2h", null, null, null, null, null, null, "name",
                null, List.of(), List.of("name"), List.of(), List.of(), Map.of(), null, null, conditional, null);
    }

    WasmFeedAdapter adapter(UrlGate gate, Boolean conditional) {
        WasmFeedSpec spec = spec(url(PATH), conditional);
        return new WasmFeedAdapter(spec, WasmFeedDescriptors.descriptor(spec, null), runtime(), gate, () -> meters);
    }

    DefaultFetchContext ctx(WasmFeedAdapter adapter) {
        return harness.context(adapter, Map.of());
    }

    static void serve(byte[] body, String etag) {
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).withHeader("If-None-Match", absent()).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/geo+json").withHeader("ETag", etag)
                .withBody(body)));
    }

    static byte[] cleanFixture() {
        return ("{\"type\":\"FeatureCollection\",\"features\":["
                + "{\"type\":\"Feature\",\"id\":\"a\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]},"
                + "\"properties\":{\"name\":\"A\"}},"
                + "{\"type\":\"Feature\",\"id\":\"b\",\"geometry\":null,\"properties\":{\"name\":\"B\"}}]}").getBytes();
    }

    @Test
    @DisplayName("fetch → plugin → records; rejections make the result partial and are counted; 304 is unchanged")
    void fetchParseAndConditional() throws Exception {
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, null);
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        serve(PluginTestFiles.fixture(), "\"v1\"");

        FetchResult first = adapter.fetch(ctx(adapter));

        assertThat(first.records()).extracting(NormalizedRecord::externalId).containsExactly("st-1", "42", "st-4");
        assertThat(first.records()).extracting(NormalizedRecord::kind).containsOnly("station");
        assertThat(first.complete()).as("an item was rejected, so absence must not mean deletion").isFalse();
        assertThat(meters.find(WasmFeedAdapter.REJECTED_METRIC).tag("reason", "EXTERNAL_ID_INVALID")
                .tag("plugin", PluginTestFiles.PASSTHROUGH).counter().count()).isEqualTo(1.0);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(PATH)).withHeader("User-Agent", equalTo(install.userAgent())));

        WIREMOCK.stubFor(get(urlEqualTo(PATH)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));
        assertThat(adapter.fetch(ctx(adapter)).notModified()).isTrue();
    }

    @Test
    @DisplayName("complete=true without rejections is a complete set")
    void completeSet() throws Exception {
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, false);
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody(cleanFixture())));

        FetchResult result = adapter.fetch(ctx(adapter));

        assertThat(result.complete()).isTrue();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("a", "b");
        assertThat(result.records().get(0).payload()).containsEntry("name", "A");
        WIREMOCK.verify(getRequestedFor(urlEqualTo(PATH)).withHeader("If-None-Match", absent()));
    }

    @Test
    @DisplayName("plugins off: the run fails with the plugin's reason before any request is spent")
    void pluginsOff() {
        pluginsOn.set(false);
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, null);
        serve(PluginTestFiles.fixture(), "\"v2\"");

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter)))
                .isInstanceOf(AdapterException.class)
                .hasMessageContaining(ID)
                .hasMessageContaining("plugins are disabled")
                .hasCauseInstanceOf(PluginException.class);
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    @DisplayName("a plugin failure after the request forgets the stored validators, so the next run refetches")
    void failureForgetsValidators() throws Exception {
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, null);
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withHeader("ETag", "\"v9\"")
                .withBody("{\"features\":[")));

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter))).isInstanceOf(AdapterException.class);
        assertThat(ctx(adapter).state().get(SourceHttp.ETAG_STATE_PREFIX + WasmFeedAdapter.STATE_KEY)).isEmpty();
    }

    @Test
    @DisplayName("plugin not enabled: the run fails and names the enabling call")
    void pluginNotEnabled() {
        approvals = PluginApprovals.NONE;
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, null);
        serve(PluginTestFiles.fixture(), "\"v3\"");

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter)))
                .isInstanceOf(AdapterException.class)
                .hasMessageContaining("PUT /v1/plugins/" + PluginTestFiles.PASSTHROUGH);
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    @DisplayName("the URL gate refuses before any request is sent")
    void urlGateRefuses() {
        WasmFeedAdapter adapter = adapter(uri -> "the address is private", null);

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter)))
                .isInstanceOf(AdapterException.class)
                .hasMessageContaining("refused by the outbound URL policy: the address is private");
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    @DisplayName("a redirect is refused, never followed")
    void redirectRefused() {
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, false);
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "http://169.254.169.254/latest/meta-data/")));

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter)))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("redirect refused");
    }

    @Test
    @DisplayName("malformed upstream bytes: the plugin answers nothing and the run fails")
    void malformedUpstream() {
        WasmFeedAdapter adapter = adapter(UrlGate.ALLOW_ALL, false);
        WIREMOCK.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json").withBody("{\"features\":[")));

        assertThatThrownBy(() -> adapter.fetch(ctx(adapter)))
                .isInstanceOf(AdapterException.class)
                .hasMessageContaining("returned no output");
        assertThat(adapter.spec().id()).isEqualTo(ID);
    }
}
