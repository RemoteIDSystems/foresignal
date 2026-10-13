package ai.foresignal.source.adapters.cctv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CCTV pack adapters against stubbed upstreams through the real upstream client (prompt 072): the URL and
 * headers per pack ({@code Digitraffic-User}, TfL's {@code app_key} redacted from traces, Ontario's required
 * {@code key} and its {@code 400 Invalid Key}), conditional GET, Foresignal's own User-Agent, refused redirects,
 * Vegvesen paging, Estonia's two documents, bundled packs without requests, and absence tombstones.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class CctvPackAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T18:00:00Z");
    static final String TFL = "/api.tfl.gov.uk/Place/Type/JamCam";
    static final String CALTRANS = "/cwwp2.dot.ca.gov/data/d4/cctv/cctvStatusD04.json";
    static final String ONTARIO = "/511on.ca/api/v2/get/cameras";
    static final String DIGITRAFFIC = "/tie.digitraffic.fi/api/weathercam/v1/stations";
    static final String VEGVESEN = "/ogckart-sn1.atlas.vegvesen.no/ogc/features/v1/collections/datex_3_1:CctvSimple/items";
    static final String ESTONIA_LOC = "/tarktee.transpordiamet.ee/api/v1/datex/roadCameraLocations";
    static final String ESTONIA_IMG = "/tarktee.transpordiamet.ee/api/v1/datex/roadCameraImages";
    static final String TFL_KEY = "tfl0123456789abcdef";
    static final String ONTARIO_KEY = "on0123456789abcdef";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    CctvCatalogFamily family;
    SimpleMeterRegistry meters;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        meters = new SimpleMeterRegistry();
        family = new CctvCatalogFamily(baseUrl(), () -> "9.9.9", () -> meters);
        for (SourceAdapter adapter : family.adapters()) {
            harness.reset(adapter.id(), SourceHttp.ETAG_STATE_PREFIX + CctvPackAdapter.STATE_KEY,
                    SourceHttp.LAST_MODIFIED_STATE_PREFIX + CctvPackAdapter.STATE_KEY, AbsenceTombstoner.IDS_KEY,
                    AbsenceTombstoner.MISSED_KEY);
        }
    }

    @AfterEach
    void tearDown() {
        harness.credentials.clear();
        for (SourceAdapter adapter : family.adapters()) {
            harness.backoff.onSuccess(adapter.id());
        }
    }

    CctvPackAdapter adapter(String id) {
        return (CctvPackAdapter) family.adapters().stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    DefaultFetchContext context(CctvPackAdapter adapter) {
        return harness.context(adapter, Map.of());
    }

    static void serve(String path, byte[] body, String etag) {
        var response = aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
        if (etag != null) {
            response = response.withHeader("ETag", etag);
        }
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(path)).willReturn(response));
    }

    @Test
    @DisplayName("every descriptor validates; Caltrans D4's URL; conditional GET with the ETag; 304 is notModified")
    void conditionalGet() throws Exception {
        for (SourceAdapter a : family.adapters()) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
        }
        CctvPackAdapter caltrans = adapter("cctv-caltrans-d4");
        serve(CALTRANS, AdapterFixtures.bytes("cctv-caltrans-d4", "cctvStatusD04.json"), "\"e1\"");

        FetchResult first = caltrans.fetch(context(caltrans));
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(25);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(CALTRANS)).withHeader("If-None-Match", absent())
                .withHeader("User-Agent", equalTo(install.userAgent()))
                .withHeader("User-Agent", matching("Foresignal/.+")));

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(CALTRANS)).withHeader("If-None-Match", equalTo("\"e1\""))
                .willReturn(aResponse().withStatus(304)));
        FetchResult second = caltrans.fetch(context(caltrans));
        assertThat(second.notModified()).isTrue();
        assertThat(second.records()).isEmpty();
    }

    @Test
    @DisplayName("Digitraffic sends Digitraffic-User: Foresignal/<version> and Accept-Encoding gzip")
    void digitrafficUser() throws Exception {
        CctvPackAdapter dt = adapter("cctv-digitraffic");
        serve(DIGITRAFFIC, AdapterFixtures.bytes("cctv-digitraffic", "stations.json"), "W/\"d1\"");
        assertThat(dt.fetch(context(dt)).records()).hasSize(20);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(DIGITRAFFIC))
                .withHeader("Digitraffic-User", equalTo("Foresignal/9.9.9"))
                .withHeader("Accept-Encoding", matching(".*gzip.*"))
                .withHeader("User-Agent", equalTo(install.userAgent())));
    }

    @Test
    @DisplayName("TfL: keyless by default; with appKey it is sent as app_key and redacted from traces and exchanges")
    void tflAppKey(CapturedOutput output) throws Exception {
        CctvPackAdapter tfl = adapter("cctv-tfl");
        serve(TFL, AdapterFixtures.bytes("cctv-tfl", "jamcam.json"), null);
        assertThat(tfl.fetch(context(tfl)).records()).hasSize(24);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(TFL)));

        WIREMOCK.resetAll();
        serve(TFL, AdapterFixtures.bytes("cctv-tfl", "jamcam.json"), null);
        harness.credentials.put("appKey", TFL_KEY);
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(tfl, Map.of(), recorder);
        tfl.fetch(ctx);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(TFL + "?app_key=" + TFL_KEY)));
        assertThat(ctx.traces()).isNotEmpty();
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl)
                .allSatisfy(url -> assertThat(url).doesNotContain(TFL_KEY).contains("app_key=***"));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(TFL_KEY);
        }
        assertThat(output.getAll()).doesNotContain(TFL_KEY);
    }

    @Test
    @DisplayName("Ontario 511: the key is required and sent as key; 400 Invalid Key is an UpstreamHttpException")
    void ontarioKey() throws Exception {
        CctvPackAdapter on = adapter("cctv-ontario511");
        assertThat(on.descriptor().requiresCredentials()).isTrue();
        assertThatThrownBy(() -> on.fetch(context(on))).isInstanceOf(AdapterException.class)
                .hasMessageContaining("key");
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));

        harness.credentials.put("key", ONTARIO_KEY);
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(ONTARIO)).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/xml; charset=utf-8")
                .withBody("<Error><Message>Invalid Key</Message></Error>")));
        assertThatThrownBy(() -> on.fetch(context(on))).isInstanceOf(UpstreamHttpException.class)
                .hasMessageNotContaining(ONTARIO_KEY);
        WIREMOCK.verify(getRequestedFor(urlEqualTo(ONTARIO + "?format=json&lang=en&key=" + ONTARIO_KEY)));

        WIREMOCK.resetAll();
        serve(ONTARIO, AdapterFixtures.bytes("cctv-ontario511", "cameras.json"), null);
        assertThat(on.fetch(context(on)).records()).hasSize(5);
    }

    @Test
    @DisplayName("a redirect is refused and never followed; a truncated or non-JSON body forgets the validators")
    void failures() {
        CctvPackAdapter nsw = adapter("cctv-nsw");
        String nswPath = "/data.livetraffic.com/cameras/traffic-cam.json";
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(nswPath)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", baseUrl() + "/elsewhere.example/x.json")));
        assertThatThrownBy(() -> nsw.fetch(context(nsw))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("redirect refused");
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo("/elsewhere.example/x.json")));

        WIREMOCK.resetAll();
        serve(nswPath, "<html>maintenance</html>".getBytes(), "\"bad\"");
        assertThatThrownBy(() -> nsw.fetch(context(nsw))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("cctv-nsw");
        assertThat(harness.client.stateStore("cctv-nsw").get(SourceHttp.ETAG_STATE_PREFIX + CctvPackAdapter.STATE_KEY))
                .isEmpty();

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(nswPath)).willReturn(aResponse().withStatus(204)));
        assertThatThrownBy(() -> nsw.fetch(context(nsw))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("HTTP 204");
    }

    @Test
    @DisplayName("a body cut at the response cap is a format error and tombstones nothing")
    void truncated() throws Exception {
        CctvPackAdapter caltrans = adapter("cctv-caltrans-d4");
        serve(CALTRANS, AdapterFixtures.bytes("cctv-caltrans-d4", "cctvStatusD04.json"), "\"t1\"");
        caltrans.fetch(context(caltrans));
        var before = harness.client.stateStore("cctv-caltrans-d4").get(AbsenceTombstoner.IDS_KEY);

        var truncating = new WeatherAdapterHarness.Context(caltrans.descriptor(),
                WeatherAdapterHarness.truncating(harness.client.forSource(caltrans.id(), caltrans.descriptor())),
                ValidatedParams.empty(), harness.client.stateStore(caltrans.id()), harness.clock);
        assertThatThrownBy(() -> caltrans.fetch(truncating)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("exceeds");
        assertThat(harness.client.stateStore("cctv-caltrans-d4").get(AbsenceTombstoner.IDS_KEY)).isEqualTo(before);
    }

    @Test
    @DisplayName("Vegvesen follows rel=next on the same endpoint, at most 5 pages; an off-host next link is refused")
    void vegvesenPaging() throws Exception {
        CctvPackAdapter vv = adapter("cctv-vegvesen");
        String feature = """
                {"type": "Feature", "geometry": {"type": "Point", "coordinates": [10.75, 59.91]},
                 "properties": {"cameraId": "%s", "description": "Page %s", "stillImageUrl":
                 "https://kamera.atlas.vegvesen.no/api/images/%s", "status.stillImageAvailability": "videoOrImagesAvailable"}}""";
        String page1 = "{\"type\": \"FeatureCollection\", \"features\": [" + feature.formatted("p1", "1", "p1")
                + "], \"links\": [{\"rel\": \"next\", \"href\": \"" + baseUrl() + VEGVESEN + "?offset=1\"}]}";
        String page2 = "{\"type\": \"FeatureCollection\", \"features\": [" + feature.formatted("p2", "2", "p2")
                + "], \"links\": []}";
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(VEGVESEN)).withQueryParam("offset", absent())
                .willReturn(aResponse().withStatus(200).withBody(page1)));
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(VEGVESEN)).withQueryParam("offset", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withBody(page2)));
        FetchResult result = vv.fetch(context(vv));
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("p1", "p2");

        // A catalog that never ends: five pages, then a partial result.
        WIREMOCK.resetAll();
        String loop = "{\"type\": \"FeatureCollection\", \"features\": [" + feature.formatted("p1", "1", "p1")
                + "], \"links\": [{\"rel\": \"next\", \"href\": \"" + baseUrl() + VEGVESEN + "?offset=1\"}]}";
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(VEGVESEN)).willReturn(aResponse().withStatus(200).withBody(loop)));
        FetchResult partial = vv.fetch(context(vv));
        assertThat(partial.complete()).isFalse();
        WIREMOCK.verify(5, getRequestedFor(urlPathEqualTo(VEGVESEN)));

        WIREMOCK.resetAll();
        String offHost = "{\"type\": \"FeatureCollection\", \"features\": [], \"links\": [{\"rel\": \"next\","
                + " \"href\": \"https://evil.example" + VEGVESEN + "\"}]}";
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo(VEGVESEN)).willReturn(aResponse().withStatus(200)
                .withBody(offHost)));
        assertThatThrownBy(() -> vv.fetch(context(vv))).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("next link");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(VEGVESEN)));
    }

    @Test
    @DisplayName("Estonia requests both DATEX documents and joins them")
    void estonia() throws Exception {
        CctvPackAdapter ee = adapter("cctv-estonia");
        serve(ESTONIA_LOC, AdapterFixtures.bytes("cctv-estonia", "roadCameraLocations.xml"), null);
        serve(ESTONIA_IMG, AdapterFixtures.bytes("cctv-estonia", "roadCameraImages.xml"), null);
        assertThat(ee.fetch(context(ee)).records()).hasSize(25);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(ESTONIA_LOC)));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(ESTONIA_IMG)));
    }

    @Test
    @DisplayName("bundled packs read their resource and make no request")
    void bundled() throws Exception {
        CctvPackAdapter tallinn = adapter("cctv-tallinn");
        assertThat(tallinn.fetch(context(tallinn)).records()).hasSize(255);
        CctvPackAdapter warendorf = adapter("cctv-warendorf");
        assertThat(warendorf.fetch(context(warendorf)).records()).singleElement()
                .satisfies(r -> assertThat(r.payload()).containsEntry("frameInsecure", true));
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("a camera missing from two complete fetches is tombstoned once; the bbox parameter filters")
    void absenceAndBbox() throws Exception {
        CctvPackAdapter calgary = adapter("cctv-calgary");
        String path = "/data.calgary.ca/resource/k7p9-kppz.json";
        String all = AdapterFixtures.text("cctv-calgary", "k7p9-kppz.json");
        serve(path, all.getBytes(), null);
        assertThat(calgary.fetch(context(calgary)).records()).hasSize(25);

        String without = all.replace("http://trafficcam.calgary.ca/loc74.jpg", "http://trafficcam.calgary.ca/notacam");
        WIREMOCK.resetAll();
        serve(path, without.getBytes(), null);
        FetchResult firstMiss = calgary.fetch(context(calgary));
        assertThat(firstMiss.records()).noneMatch(NormalizedRecord::tombstone).hasSize(24);
        FetchResult secondMiss = calgary.fetch(context(calgary));
        assertThat(secondMiss.records()).filteredOn(NormalizedRecord::tombstone)
                .extracting(NormalizedRecord::externalId).containsExactly("loc74");
        assertThat(calgary.fetch(context(calgary)).records()).noneMatch(NormalizedRecord::tombstone);
        assertThat(meters.find(CctvPackAdapter.SKIPPED_METRIC).tag("source", "cctv-calgary")
                .tag("reason", PackContext.MALFORMED).counter().count()).isEqualTo(3);

        DefaultFetchContext box = harness.context(calgary,
                Map.of(CctvDescriptors.BBOX_PARAM, new Envelope(-114.12, -114.11, 51.01, 51.02)));
        FetchResult boxed = calgary.fetch(box);
        assertThat(boxed.records()).filteredOn(r -> !r.tombstone()).extracting(NormalizedRecord::externalId)
                .containsExactly("loc112");
    }

    @Test
    @DisplayName("an empty catalog tombstones nothing")
    void emptyCatalog() throws Exception {
        CctvPackAdapter calgary = adapter("cctv-calgary");
        String path = "/data.calgary.ca/resource/k7p9-kppz.json";
        serve(path, AdapterFixtures.bytes("cctv-calgary", "k7p9-kppz.json"), null);
        calgary.fetch(context(calgary));
        WIREMOCK.resetAll();
        serve(path, "[]".getBytes(), null);
        assertThat(calgary.fetch(context(calgary)).records()).isEmpty();
        assertThat(calgary.fetch(context(calgary)).records()).isEmpty();
        assertThat(AbsenceTombstoner.read(harness.client.stateStore("cctv-calgary"), AbsenceTombstoner.IDS_KEY))
                .hasSize(25);
    }

    @Test
    @DisplayName("no code in the package fetches frames or overrides the User-Agent")
    void noFrameFetchNoUserAgent() throws Exception {
        Path dir = Path.of("src", "main", "java", "ai", "foresignal", "source", "adapters", "cctv");
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.toList()) {
                String source = Files.readString(file);
                assertThat(source).as(file.toString()).doesNotContain("userAgentOverride")
                        .doesNotContain("Mozilla/").doesNotContain("download(").doesNotContain("media()")
                        .doesNotContain("HttpClient");
            }
        }
        for (SourceAdapter a : family.adapters()) {
            assertThat(a.descriptor().userAgentOverride()).as(a.id()).isNull();
            assertThat(((CctvPackAdapter) a).headers().keySet()).noneMatch(h -> h.equalsIgnoreCase("User-Agent"));
        }
        assertThat(List.of(CctvPackAdapter.sameEndpoint(java.net.URI.create("https://a.example/x?p=1"),
                java.net.URI.create("https://a.example/x?p=2")), CctvPackAdapter.sameEndpoint(
                java.net.URI.create("https://a.example/x"), java.net.URI.create("https://a.example/y"))))
                .containsExactly(true, false);
    }
}
