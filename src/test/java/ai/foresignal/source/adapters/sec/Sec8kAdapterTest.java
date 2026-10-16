package ai.foresignal.source.adapters.sec;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
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
 * The SEC 8-K contact User-Agent and paging (prompt 091): the {@code contactEmail} credential (not a constructor
 * argument) reaches the User-Agent; without any contact the fetch is refused; a full page of new entries pages on,
 * at most three pages.
 */
@IntegrationTest
class Sec8kAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");
    static final String PATH = "/cgi-bin/browse-edgar";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        harness.reset(Sec8kAdapter.ID, Sec8kAdapter.NEWEST_KEY);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(Sec8kAdapter.ID);
    }

    @Test
    @DisplayName("the User-Agent carries the Foresignal identifier and the contactEmail credential")
    void userAgentFromCredential() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml")
                .withBody(AdapterFixtures.bytes("sec-8k", "current-8k.xml"))));
        harness.credentials.put(Sec8kAdapter.CONTACT_EMAIL, "ops@example.org");
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).isNotEmpty();
        assertThat(result.complete()).isFalse();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("User-Agent", containing("Foresignal/"))
                .withHeader("User-Agent", containing("; contact=ops@example.org")));
    }

    @Test
    @DisplayName("the credential takes precedence over the fallback property")
    void credentialWins() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml")
                .withBody(AdapterFixtures.bytes("sec-8k", "current-8k.xml"))));
        harness.credentials.put(Sec8kAdapter.CONTACT_EMAIL, "credential@example.org");
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "property@example.org");
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("User-Agent", containing("contact=credential@example.org")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH))
                .withHeader("User-Agent", containing("property@example.org")));
    }

    @Test
    @DisplayName("with neither the credential nor the fallback, nothing is sent")
    void noContact() {
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "");
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, Map.of())))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageContaining("contactEmail");
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH)));
    }

    @Test
    @DisplayName("full pages of new entries page on, and paging stops at three pages")
    void pagingStopsAtThree() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml").withBody(fullPage(NOW))));
        harness.credentials.put(Sec8kAdapter.CONTACT_EMAIL, "ops@example.org");
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "");

        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));

        assertThat(result.records()).hasSize(3 * Sec8kAdapter.PAGE_SIZE);
        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(PATH)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start", absent()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start", equalTo("100")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("start", equalTo("200")));
    }

    @Test
    @DisplayName("a full first page that is not all new does not page on")
    void seenEntriesStopPaging() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/atom+xml").withBody(fullPage(NOW))));
        harness.credentials.put(Sec8kAdapter.CONTACT_EMAIL, "ops@example.org");
        Sec8kAdapter adapter = new Sec8kAdapter(baseUrl(), "");
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.resetRequests();

        adapter.fetch(harness.context(adapter, Map.of()));

        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)));
    }

    /** A synthetic Atom page of 100 distinct 8-K entries, updated one minute apart before {@code newest}. */
    private static byte[] fullPage(Instant newest) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n"
                + "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n<title>Latest Filings</title>\n");
        for (int i = 0; i < Sec8kAdapter.PAGE_SIZE; i++) {
            String acc = String.format("0000000000-26-%06d", i);
            xml.append("<entry>\n<title>8-K - EXAMPLE CORP ").append(i).append(" (0000000001) (Filer)</title>\n")
                    .append("<link rel=\"alternate\" type=\"text/html\" href=\"https://www.sec.gov/x/").append(acc)
                    .append("-index.htm\"/>\n<summary type=\"html\"> &lt;b&gt;Filed:&lt;/b&gt; 2026-10-14 ")
                    .append("&lt;b&gt;AccNo:&lt;/b&gt; ").append(acc).append(" &lt;b&gt;Size:&lt;/b&gt; 1 MB\n")
                    .append("&lt;br&gt;Item 8.01: Other Events\n</summary>\n<updated>")
                    .append(newest.minusSeconds(60L * i)).append("</updated>\n")
                    .append("<category scheme=\"https://www.sec.gov/\" label=\"form type\" term=\"8-K\"/>\n")
                    .append("<id>urn:tag:sec.gov,2008:accession-number=").append(acc).append("</id>\n</entry>\n");
        }
        return xml.append("</feed>\n").toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
