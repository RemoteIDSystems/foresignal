package ai.foresignal.source.adapters.sanctions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The OFAC cheap change check (prompt 091): an unchanged publication path is not downloaded and the run is
 * not-modified; a new path downloads; a new path with the same bytes is still not-modified; the pre-signed query is
 * never stored or logged; an unapproved redirect host is refused; an implausible shrink writes no tombstones.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class OfacChangeCheckTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String EXPORT = "/api/PublicationPreview/exports/SDN.XML";
    static final String OBJECT = "/Published/abc/2026-10-14/abc/SDN.XML";
    static final String OBJECT_2 = "/Published/def/2026-10-15/def/SDN.XML";
    static final String SIGNATURE = "c2VjcmV0LXNpZ25hdHVyZS12YWx1ZQ";
    static final String CREDENTIAL = "ASIAEXAMPLECREDENTIAL";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    OfacSdnAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        adapter = new OfacSdnAdapter(baseUrl());
        harness.reset(adapter.id(), "ofac:path:SDN", "ofac:ids:SDN", "ofac:sha:SDN",
                AdapterStateStore.DEGRADED_REASON_KEY);
        redirectTo(OBJECT);
        serve(OBJECT, "sdn.xml");
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(adapter.id());
    }

    private void redirectTo(String object) {
        WIREMOCK.stubFor(get(urlPathEqualTo(EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + object + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential="
                        + CREDENTIAL + "%2F20261014%2Fus-gov-west-1%2Fs3%2Faws4_request&X-Amz-Signature=" + SIGNATURE)));
    }

    private static void serve(String object, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo(object)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody(AdapterFixtures.bytes("ofac-sdn", fixture))));
    }

    @Test
    @DisplayName("a new publication downloads once; the same publication path is not-modified without a download")
    void changeCheck() throws Exception {
        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        assertThat(first.complete()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OBJECT)));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.notModified()).as("unchanged(), not an empty complete run").isTrue();
        assertThat(second.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OBJECT)));
    }

    @Test
    @DisplayName("a new publication path with identical bytes is not-modified (SHA-256 check)")
    void sameBytesNewPath() throws Exception {
        adapter.fetch(harness.context(adapter, Map.of()));
        redirectTo(OBJECT_2);
        serve(OBJECT_2, "sdn.xml");

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(OBJECT_2)));
    }

    @Test
    @DisplayName("the pre-signed query never reaches traces, the log output or the adapter state")
    void presignedQueryNeverStored(CapturedOutput output) throws Exception {
        DefaultFetchContext ctx = harness.context(adapter, Map.of());

        adapter.fetch(ctx);

        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(SIGNATURE).doesNotContain(CREDENTIAL));
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).anyMatch(url -> url.endsWith(OBJECT + "?***"));
        assertThat(output.getAll()).doesNotContain(SIGNATURE).doesNotContain(CREDENTIAL);
        harness.client.stateStore(adapter.id()).all().values().forEach(value ->
                assertThat(value).doesNotContain(SIGNATURE).doesNotContain(CREDENTIAL).doesNotContain("X-Amz"));
    }

    @Test
    @DisplayName("a redirect to a host other than the us-gov-west-1 S3 endpoint is refused")
    void unapprovedHost() {
        WIREMOCK.stubFor(get(urlPathEqualTo(EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "https://example-bucket.s3.amazonaws.com" + OBJECT + "?X-Amz-Signature=x")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("unapproved host");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(OBJECT)));
    }

    @Test
    @DisplayName("a 20%-shorter publication writes no tombstones and degrades the source; allowShrink accepts it")
    void shrinkGuard() throws Exception {
        int full = adapter.fetch(harness.context(adapter, Map.of())).records().size();
        redirectTo(OBJECT_2);
        serve(OBJECT_2, "sdn-short.xml");

        FetchResult shrunk = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(shrunk.records()).noneMatch(NormalizedRecord::tombstone);
        AdapterStateStore state = harness.client.stateStore(adapter.id());
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY).orElseThrow())
                .startsWith(DelistingGuard.SHRINK_REASON);
        assertThat(ListReconciler.stored(state, "ofac:ids:SDN")).hasSize(full);

        // the operator verified upstream: the override run tombstones and makes the short list the baseline
        harness.reset(adapter.id(), "ofac:path:SDN", "ofac:sha:SDN");
        DefaultFetchContext manual = harness.context(adapter, Map.of());
        manual.allowShrink(true);
        FetchResult accepted = adapter.fetch(manual);
        assertThat(accepted.records()).filteredOn(NormalizedRecord::tombstone).isNotEmpty();
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }
}
