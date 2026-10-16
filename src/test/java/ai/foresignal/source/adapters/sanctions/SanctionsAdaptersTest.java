package ai.foresignal.source.adapters.sanctions;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The UK and UN list fetches (prompt 091, WireMock): the UK list is one conditional GET (no second download) and
 * a 304 is not-modified; the UN blob, at a fixed path behind a changing SAS query, is change-checked by its ETag,
 * its SAS signature never reaches a trace, and a redirect off the reviewed blob account is refused.
 */
@IntegrationTest
class SanctionsAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");
    static final String UK_PATH = "/docs/UK-Sanctions-List.xml";
    static final String UN_EXPORT = "/resources/xml/en/consolidated.xml";
    static final String UN_BLOB = "/publiclegacyxmlfiles/EN/consolidatedLegacyByPRN.xml";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        harness.reset(UkSanctionsAdapter.ID, "etag:" + UkSanctionsAdapter.STATE_KEY,
                "lastmod:" + UkSanctionsAdapter.STATE_KEY, UkSanctionsAdapter.IDS_KEY);
        harness.reset(UnSanctionsAdapter.ID, "etag:" + UnSanctionsAdapter.BLOB_KEY,
                "lastmod:" + UnSanctionsAdapter.BLOB_KEY, UnSanctionsAdapter.IDS_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(UkSanctionsAdapter.ID);
        harness.backoff.onSuccess(UnSanctionsAdapter.ID);
    }

    @Test
    @DisplayName("UK: one conditional GET per run; a 304 is not-modified")
    void ukSingleConditionalRequest() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(UK_PATH)).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withHeader("ETag", "\"uk-1\"")
                .withBody(AdapterFixtures.bytes("uk-sanctions-list", "uk-sanctions-list.xml"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(UK_PATH)).atPriority(1).withHeader("If-None-Match", equalTo("\"uk-1\""))
                .willReturn(aResponse().withStatus(304)));
        UkSanctionsAdapter adapter = new UkSanctionsAdapter(baseUrl());

        FetchResult first = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(first.records()).isNotEmpty();
        assertThat(first.complete()).isTrue();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(UK_PATH)));

        FetchResult second = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(second.notModified()).isTrue();
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(UK_PATH)));
    }

    @Test
    @DisplayName("UN: a new SAS query on the same blob is a 304, and the signature is never traced")
    void unBlobEtag() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(UN_EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + UN_BLOB + "?sv=2024-05-04&sr=b&sp=r&sig=SECRETSIG1")));
        WIREMOCK.stubFor(get(urlPathEqualTo(UN_BLOB)).atPriority(5).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/xml").withHeader("ETag", "\"0x8DF\"")
                .withBody(AdapterFixtures.bytes("un-sanctions", "consolidated.xml"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(UN_BLOB)).atPriority(1).withHeader("If-None-Match", equalTo("\"0x8DF\""))
                .willReturn(aResponse().withStatus(304)));
        UnSanctionsAdapter adapter = new UnSanctionsAdapter(baseUrl());

        DefaultFetchContext first = harness.context(adapter, Map.of());
        assertThat(adapter.fetch(first).records()).isNotEmpty();

        WIREMOCK.stubFor(get(urlPathEqualTo(UN_EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + UN_BLOB + "?sv=2024-05-04&sr=b&sp=r&sig=SECRETSIG2")));
        DefaultFetchContext second = harness.context(adapter, Map.of());
        assertThat(adapter.fetch(second).notModified()).isTrue();

        for (DefaultFetchContext ctx : java.util.List.of(first, second)) {
            assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl)
                    .allSatisfy(url -> assertThat(url).doesNotContain("SECRETSIG"));
        }
    }

    @Test
    @DisplayName("UN: a redirect to another blob account is refused")
    void unUnapprovedHost() {
        WIREMOCK.stubFor(get(urlPathEqualTo(UN_EXPORT)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "https://someoneelse.blob.core.windows.net" + UN_BLOB + "?sig=x")));
        UnSanctionsAdapter adapter = new UnSanctionsAdapter(baseUrl());
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(AdapterException.class).hasMessageContaining("unapproved host");
    }
}
