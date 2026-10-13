package ai.foresignal.source.adapters.fire;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The fire adapters against stubbed upstreams through the real upstream client (prompt 075): NIFC paging while
 * {@code exceededTransferLimit} is true and the {@code lastEditDate} short-circuit, CWFIS {@code startIndex} paging
 * with {@code sortBy} and the swapped-axis sample, and InciWeb's conditional GET. No adapter fetches an HTML page.
 */
@IntegrationTest
class FireAdaptersTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T19:30:00Z");
    static final String NIFC = "/services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/"
            + "WFIGS_Interagency_Perimeters_Current/FeatureServer/0";
    static final String WFS = "/cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs";
    static final String INCIWEB = "/inciweb.wildfire.gov/incidents/rss.xml";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    List<SourceAdapter> adapters;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapters = new ArrayList<>();
        adapters.add(new NifcPerimetersAdapter(baseUrl(), () -> null));
        adapters.add(new InciwebIncidentsAdapter(baseUrl(), () -> null));
        adapters.addAll(new CwfisFamily(baseUrl(), () -> null).adapters());
        for (SourceAdapter a : adapters) {
            harness.reset(a.id(), ArcGisQueryClient.META_KEY, ArcGisQueryClient.LAST_EDIT_KEY,
                    AbsenceTombstoner.IDS_KEY, AbsenceTombstoner.MISSED_KEY,
                    SourceHttp.ETAG_STATE_PREFIX + InciwebIncidentsAdapter.STATE_KEY,
                    SourceHttp.LAST_MODIFIED_STATE_PREFIX + InciwebIncidentsAdapter.STATE_KEY);
        }
    }

    @AfterEach
    void tearDown() {
        for (SourceAdapter a : adapters) {
            harness.backoff.onSuccess(a.id());
        }
    }

    SourceAdapter adapter(String id) {
        return adapters.stream().filter(a -> a.id().equals(id)).findFirst().orElseThrow();
    }

    FetchResult fetch(String id) throws Exception {
        SourceAdapter a = adapter(id);
        return ((PollingAdapter) a).fetch(harness.context(a, Map.of()));
    }

    static void json(String path, Map<String, String> query, byte[] body) {
        var stub = get(urlPathEqualTo(path));
        for (Map.Entry<String, String> q : query.entrySet()) {
            stub = stub.withQueryParam(q.getKey(), equalTo(q.getValue()));
        }
        WIREMOCK.stubFor(stub.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(body)));
    }

    /** The NIFC fixture split into two pages: the first {@code split} features with exceededTransferLimit. */
    static byte[][] nifcPages(int split) {
        JsonNode all = JSON.readTree(AdapterFixtures.bytes("nifc-perimeters", "query-page-1.json"));
        ArrayNode features = (ArrayNode) all.get("features");
        ObjectNode first = JSON.createObjectNode().put("type", "FeatureCollection");
        ObjectNode second = JSON.createObjectNode().put("type", "FeatureCollection");
        ArrayNode a = first.putArray("features");
        ArrayNode b = second.putArray("features");
        for (int i = 0; i < features.size(); i++) {
            (i < split ? a : b).add(features.get(i));
        }
        first.put("exceededTransferLimit", true);
        return new byte[][] {JSON.writeValueAsBytes(first), JSON.writeValueAsBytes(second)};
    }

    @Test
    @DisplayName("every fire descriptor validates and declares a license; none is a kit spec")
    void descriptors() {
        assertThat(adapters).extracting(SourceAdapter::id).containsExactly("nifc-perimeters", "inciweb-incidents",
                "cwfis-perimeters", "cwfis-fwi-stations", "cwfis-hotspots");
        for (SourceAdapter a : adapters) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().licence()).as(a.id()).isNotBlank();
        }
        assertThat(adapter("nifc-perimeters").descriptor().volatileFields()).containsExactly("personnel");
        assertThat(adapter("nifc-perimeters").descriptor().rankField()).isEqualTo("gisAcres");
        assertThat(adapter("inciweb-incidents").descriptor().embedFields()).containsExactly("name", "overview");
        assertThat(adapter("inciweb-incidents").descriptor().recordTtl()).isEqualTo(Duration.ofDays(14));
    }

    @Test
    @DisplayName("NIFC: GEV's query, two pages while exceededTransferLimit, then the lastEditDate short-circuit")
    void nifcPagingAndShortCircuit() throws Exception {
        byte[][] pages = nifcPages(10);
        json(NIFC, Map.of("f", "json"), AdapterFixtures.bytes("nifc-perimeters", "layer.json"));
        json(NIFC + "/query", Map.of("resultOffset", "0"), pages[0]);
        json(NIFC + "/query", Map.of("resultOffset", "10"), pages[1]);

        FetchResult first = fetch("nifc-perimeters");
        assertThat(first.complete()).isTrue();
        assertThat(first.records()).hasSize(25).noneMatch(NormalizedRecord::tombstone);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(NIFC + "/query"))
                .withQueryParam("where", equalTo("1=1"))
                .withQueryParam("outFields", equalTo(String.join(",", NifcPerimeterMapper.OUT_FIELDS)))
                .withQueryParam("maxAllowableOffset", equalTo("0.001"))
                .withQueryParam("outSR", equalTo("4326"))
                .withQueryParam("f", equalTo("geojson"))
                .withQueryParam("resultRecordCount", equalTo("2000"))
                .withHeader("User-Agent", matching("Foresignal/.*")));
        assertThat(harness.client.stateStore("nifc-perimeters").get(AbsenceTombstoner.IDS_KEY)).isPresent();

        harness.clock.advance(Duration.ofMinutes(5));
        assertThat(fetch("nifc-perimeters").notModified()).as("same dataLastEditDate").isTrue();
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(NIFC + "/query")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(NIFC)));
    }

    @Test
    @DisplayName("NIFC: the 5-page cap makes the fetch partial and tombstones nothing; an error body is a format error")
    void nifcCapAndError() throws Exception {
        json(NIFC, Map.of("f", "json"), AdapterFixtures.bytes("nifc-perimeters", "layer.json"));
        byte[][] pages = nifcPages(5);
        for (int offset = 0; offset < 25; offset += 5) {
            json(NIFC + "/query", Map.of("resultOffset", String.valueOf(offset)), pages[0]);
        }
        FetchResult capped = fetch("nifc-perimeters");
        assertThat(capped.complete()).isFalse();
        WIREMOCK.verify(NifcPerimetersAdapter.MAX_PAGES, getRequestedFor(urlPathEqualTo(NIFC + "/query")));
        assertThat(harness.client.stateStore("nifc-perimeters").get(AbsenceTombstoner.IDS_KEY)).isEmpty();

        WIREMOCK.resetAll();
        json(NIFC, Map.of("f", "json"), "{\"error\": {\"code\": 500, \"message\": \"Service busy\"}}"
                .getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> fetch("nifc-perimeters")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("Service busy");
    }

    @Test
    @DisplayName("CWFIS stations: GetFeature with sortBy=aes, startIndex paging until a short page")
    void cwfisPaging() throws Exception {
        ObjectNode page = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("cwfis-fwi-stations", "page.json"));
        page.put("numberReturned", 1000);
        json(WFS, Map.of("startIndex", "0"), JSON.writeValueAsBytes(page));
        json(WFS, Map.of("startIndex", "1000"), ("{\"type\": \"FeatureCollection\", \"numberReturned\": 0,"
                + " \"features\": []}").getBytes(StandardCharsets.UTF_8));

        FetchResult result = fetch("cwfis-fwi-stations");
        assertThat(result.complete()).isTrue();
        assertThat(result.records()).hasSize(24);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(WFS))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeNames", equalTo("public:firewx_stns_current"))
                .withQueryParam("outputFormat", equalTo("application/json"))
                .withQueryParam("count", equalTo("1000"))
                .withQueryParam("sortBy", equalTo("aes")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(WFS)));
    }

    @Test
    @DisplayName("CWFIS hotspots: a latitude-first sample is swapped back by the WFS kit's North America check")
    void cwfisSwappedAxis() throws Exception {
        json(WFS, Map.of("typeNames", "public:hotspots_last24hrs"), AdapterFixtures.bytes("cwfis-hotspots",
                "swapped.json"));
        FetchResult result = fetch("cwfis-hotspots");
        assertThat(result.records()).hasSize(3);
        Point fresno = (Point) result.records().getFirst().geometry();
        assertThat(fresno.getX()).isEqualTo(-119.7802887);
        assertThat(fresno.getY()).isEqualTo(36.72795105);
        assertThat(result.records().getFirst().externalId()).isEqualTo(
                "2026-10-12T21:24:00Z:36.72795:-119.78029:NOAA-21");
    }

    @Test
    @DisplayName("CWFIS perimeters: absent from two complete fetches → one tombstone")
    void cwfisAbsence() throws Exception {
        byte[] full = AdapterFixtures.bytes("cwfis-perimeters", "page.json");
        ObjectNode fewer = (ObjectNode) JSON.readTree(full);
        ((ArrayNode) fewer.get("features")).remove(0);
        json(WFS, Map.of("typeNames", "public:m3_polygons_current"), full);
        assertThat(fetch("cwfis-perimeters").records()).hasSize(24);
        WIREMOCK.resetAll();
        json(WFS, Map.of("typeNames", "public:m3_polygons_current"), JSON.writeValueAsBytes(fewer));
        assertThat(fetch("cwfis-perimeters").records()).noneMatch(NormalizedRecord::tombstone);
        List<NormalizedRecord> third = fetch("cwfis-perimeters").records();
        assertThat(third).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("2026-05-13T18:09:00Z@51.73,-98.58");
    }

    @Test
    @DisplayName("InciWeb: a conditional GET; an ETag is sent back and a 304 is unchanged; HTML is a format error")
    void inciwebConditional() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo(INCIWEB)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/rss+xml; charset=utf-8").withHeader("ETag", "\"v1\"")
                .withBody(AdapterFixtures.bytes("inciweb-incidents", "rss.xml"))));
        FetchResult first = fetch("inciweb-incidents");
        assertThat(first.complete()).as("a feed is a window").isFalse();
        assertThat(first.records()).hasSize(25);

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(INCIWEB)).withHeader("If-None-Match", equalTo("\"v1\""))
                .willReturn(aResponse().withStatus(304)));
        assertThat(fetch("inciweb-incidents").notModified()).isTrue();

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo(INCIWEB)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withBody("<html><body>maintenance</body></html>")));
        assertThatThrownBy(() -> fetch("inciweb-incidents")).isInstanceOf(UpstreamFormatException.class);
        assertThat(harness.client.stateStore("inciweb-incidents")
                .get(SourceHttp.ETAG_STATE_PREFIX + InciwebIncidentsAdapter.STATE_KEY)).isEmpty();
    }
}
