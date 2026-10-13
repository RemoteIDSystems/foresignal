package ai.foresignal.source.adapters.ioda;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ioda-outages} against a stubbed IODA API through the real upstream client (prompt 068): {@code from} and
 * {@code until} from {@code window}, pagination that stops at a short page and at three pages, the single
 * {@code entityType} and {@code entityCode} query parameters, client-side filters, a 429 backoff (005), a 200 that is
 * not the envelope, the descriptor, and no request other than the alerts endpoint (no geocoding).
 */
@IntegrationTest
class IodaAdapterTest extends WireMockSupport {

    static final String ID = IodaOutagesAdapter.ID;
    static final String PATH = IodaOutagesAdapter.PATH;
    static final Instant NOW = Instant.parse("2026-10-13T15:18:25Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    AdapterHttpHarness harness;
    IodaOutagesAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 1000));
        harness.reset(ID);
        adapter = new IodaOutagesAdapter(baseUrl(), new CountryCentroids(), null, new SimpleMeterRegistry());
    }

    private FetchResult fetch(Map<String, Object> params) throws AdapterException {
        return adapter.fetch(harness.context(adapter, params));
    }

    private static void serve(byte[] body) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    private static void servePage(int page, byte[] body) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).withQueryParam("page", equalTo(Integer.toString(page)))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    /** {@code page-full-500.json}: a page of {@code rows} distinct region alerts in Brazil, numbered from {@code first}. */
    static byte[] regionPage(int first, int rows) {
        StringBuilder data = new StringBuilder("[");
        for (int i = 0; i < rows; i++) {
            if (i > 0) {
                data.append(',');
            }
            int code = first + i;
            data.append("{\"datasource\":\"bgp\",\"entity\":{\"code\":\"").append(code).append("\",\"name\":\"Region ")
                    .append(code).append("\",\"subnames\":[],\"type\":\"region\",\"attrs\":{\"country_code\":\"BR\"}},")
                    .append("\"time\":").append(1791861600L + i).append(",\"level\":\"critical\",")
                    .append("\"condition\":\"< 0.99\",\"value\":5,\"historyValue\":10,\"method\":\"median\"}");
        }
        data.append(']');
        return ("{\"type\":\"outages.alerts\",\"metadata\":{},\"requestParameters\":{},\"error\":null,\"perf\":null,"
                + "\"data\":" + data + ",\"copyright\":\"c\"}").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("from and until are epoch seconds computed from window and the clock")
    void window() throws Exception {
        serve(AdapterFixtures.bytes(ID, "alerts-12h.json"));

        fetch(Map.of(IodaOutagesAdapter.WINDOW_PARAM, Duration.ofHours(2)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withQueryParam("from", equalTo(Long.toString(NOW.getEpochSecond() - 7200)))
                .withQueryParam("until", equalTo(Long.toString(NOW.getEpochSecond())))
                .withQueryParam("limit", equalTo("500")).withQueryParam("page", equalTo("0")));

        clock.advance(Duration.ofMinutes(5));
        fetch(Map.of());
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH))
                .withQueryParam("from", equalTo(Long.toString(NOW.getEpochSecond() + 300 - 43_200)))
                .withQueryParam("until", equalTo(Long.toString(NOW.getEpochSecond() + 300))));
    }

    @Test
    @DisplayName("the captured window with the defaults: 14 records, never complete, nothing but the alerts endpoint")
    void captured() throws Exception {
        serve(AdapterFixtures.bytes(ID, "alerts-12h.json"));

        FetchResult result = fetch(Map.of());

        assertThat(result.complete()).isFalse();
        assertThat(result.coverage().isEmpty()).isTrue();
        assertThat(result.records()).hasSize(14);
        assertThat(result.records()).extracting(r -> r.payload().get("entityType")).containsOnly("country", "region");
        assertThat(result.records().stream().filter(r -> r.payload().get("entityType").equals("country")))
                .allSatisfy(r -> {
                    assertThat(r.geometry()).isNotNull();
                    assertThat(r.payload()).containsEntry("geometryApproximate", true);
                });
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("entityType", absent())
                .withQueryParam("entityCode", absent()));
    }

    @Test
    @DisplayName("pagination stops at the first short page")
    void shortPage() throws Exception {
        servePage(0, regionPage(0, 500));
        servePage(1, regionPage(500, 20));

        FetchResult result = fetch(Map.of());

        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(PATH)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("page", equalTo("1")));
        assertThat(result.records()).hasSize(520);
        assertThat(result.coverage().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("pagination stops after three full pages and reports Coverage(expected=null, actual=rows)")
    void pageCap() throws Exception {
        servePage(0, regionPage(0, 500));
        servePage(1, regionPage(500, 500));
        servePage(2, regionPage(1000, 500));
        servePage(3, regionPage(1500, 500));

        FetchResult result = fetch(Map.of());

        WIREMOCK.verify(3, getRequestedFor(urlPathEqualTo(PATH)));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("page", equalTo("3")));
        assertThat(result.records()).hasSize(1500);
        assertThat(result.coverage().expected()).isNull();
        assertThat(result.coverage().actual()).isEqualTo(1500);
    }

    @Test
    @DisplayName("an empty page is a quiet window: no records, no error")
    void quiet() throws Exception {
        serve(regionPage(0, 0));
        assertThat(fetch(Map.of()).records()).isEmpty();
    }

    @Test
    @DisplayName("entityType is sent only for a single type; entityCode only for one country with type country")
    void singleTypeParams() throws Exception {
        serve(AdapterFixtures.bytes(ID, "alerts-12h.json"));

        FetchResult regions = fetch(Map.of(IodaOutagesAdapter.ENTITY_TYPES_PARAM, List.of("region")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("entityType", equalTo("region"))
                .withQueryParam("entityCode", absent()));
        assertThat(regions.records()).extracting(r -> r.payload().get("entityType")).containsOnly("region");

        WIREMOCK.resetRequests();
        fetch(Map.of(IodaOutagesAdapter.ENTITY_TYPES_PARAM, List.of("country"), IodaOutagesAdapter.COUNTRIES_PARAM,
                List.of("ht")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("entityType", equalTo("country"))
                .withQueryParam("entityCode", equalTo("HT")));

        WIREMOCK.resetRequests();
        fetch(Map.of(IodaOutagesAdapter.COUNTRIES_PARAM, List.of("BR")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("entityType", absent())
                .withQueryParam("entityCode", absent()));
    }

    @Test
    @DisplayName("countries filter client-side: a country and its regions are kept, ASNs dropped")
    void countriesFilter() throws Exception {
        serve(AdapterFixtures.bytes(ID, "alerts-12h.json"));

        FetchResult result = fetch(Map.of(IodaOutagesAdapter.ENTITY_TYPES_PARAM, List.of("country", "region", "asn"),
                IodaOutagesAdapter.DATASOURCES_PARAM, List.of("bgp", "ping-slash24", "gtr", "merit-nt"),
                IodaOutagesAdapter.COUNTRIES_PARAM, List.of("CD", "BR")));

        assertThat(result.records()).extracting(r -> r.externalId()).containsExactlyInAnyOrder("region:497:merit-nt",
                "region:4680:bgp", "country:CD:bgp", "region:694:bgp");
    }

    @Test
    @DisplayName("a 429 backs off through 005, honoring Retry-After")
    void throttled() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)
                .withHeader("Retry-After", "120")));

        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.backoffUntil(ID)).isEqualTo(NOW.plus(Duration.ofSeconds(120)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PATH)));

        harness.reset(ID);
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(429)));
        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOf(UpstreamHttpException.class);
        assertThat(harness.backoff.backoffUntil(ID)).isAfter(NOW);
    }

    @Test
    @DisplayName("a 200 that is not the JSON envelope is UpstreamFormatException")
    void notEnvelope() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withBody(AdapterFixtures.bytes(ID, "not-envelope.html"))));

        assertThatThrownBy(() -> fetch(Map.of())).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("params: window 1-24 h, ISO2 countries, non-empty lists; unknown values refused at fetch")
    void params() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of()))).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(IodaOutagesAdapter.WINDOW_PARAM, Duration.ofHours(24)))))
                .isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(IodaOutagesAdapter.WINDOW_PARAM,
                Duration.ofMinutes(30))))).containsKey(IodaOutagesAdapter.WINDOW_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(IodaOutagesAdapter.WINDOW_PARAM, Duration.ofHours(25)))))
                .containsKey(IodaOutagesAdapter.WINDOW_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(IodaOutagesAdapter.COUNTRIES_PARAM, List.of("USA")))))
                .containsKey(IodaOutagesAdapter.COUNTRIES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(IodaOutagesAdapter.ENTITY_TYPES_PARAM, List.of(),
                IodaOutagesAdapter.DATASOURCES_PARAM, List.of())))).containsKeys(IodaOutagesAdapter.ENTITY_TYPES_PARAM,
                IodaOutagesAdapter.DATASOURCES_PARAM);
        assertThatThrownBy(() -> IodaOutagesAdapter.entityTypes(ValidatedParams.of(Map.of(
                IodaOutagesAdapter.ENTITY_TYPES_PARAM, List.of("geoasn"))))).isInstanceOf(AdapterException.class);
        assertThatThrownBy(() -> IodaOutagesAdapter.datasources(ValidatedParams.of(Map.of(
                IodaOutagesAdapter.DATASOURCES_PARAM, List.of("ucsd-nt"))))).isInstanceOf(AdapterException.class);
        assertThat(IodaOutagesAdapter.window(ValidatedParams.of(Map.of()))).isEqualTo(Duration.ofHours(12));
    }

    @Test
    @DisplayName("the descriptor is valid, LOCAL_ONLY, and carries the prompt's values")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();

        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("ioda-outages");
        assertThat(d.kinds()).containsExactly("outage");
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 1200));
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(6));
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
        assertThat(d.volatileFields()).containsExactly("value", "historyValue", "ratio");
        assertThat(d.titleField()).isEqualTo("summary");
        assertThat(d.searchFields()).containsExactly("entityName", "entityCode");
        assertThat(d.tileProperties()).containsExactly("level", "datasource", "entityType", "ratio");
        assertThat(d.licence()).isEqualTo(IodaOutagesAdapter.LICENSE);
        assertThat(d.termsUrl()).isEqualTo("https://ioda.inetintel.cc.gatech.edu/");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.params()).extracting(ParamSpec::name).containsExactly("entityTypes", "datasources", "window",
                "countries");
        assertThat(d.params().get(0).defaultValue()).isEqualTo(List.of("country", "region"));
        assertThat(d.params().get(1).defaultValue()).isEqualTo(List.of("bgp", "ping-slash24"));
    }
}
