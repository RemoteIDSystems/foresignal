package ai.foresignal.source.adapters.maritime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureKitFamily;
import ai.foresignal.source.kit.feature.FeatureMapper;
import ai.foresignal.source.kit.feature.FeatureSourceSpec;
import ai.foresignal.source.kit.feature.FeatureSpecValidator;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * The IMF PortWatch sources against stubbed ArcGIS layers through prompt 073's {@link ArcGisQueryClient} (prompt
 * 088): the layer-unchanged short-circuit (no table query), the incremental {@code date > DATE '…'} query ordered by
 * date, paging, the join of daily rows to the chokepoint and port points, the port filter, the port catalog's
 * publication time and tombstones, and the disruptions kit spec with its validity window.
 */
@IntegrationTest
class PortWatchAdapterTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-14T06:00:00Z");
    static final String SERVICES = "/services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services/";
    static final String CP = SERVICES + "PortWatch_chokepoints_database/FeatureServer/0";
    static final String CP_DAILY = SERVICES + "Daily_Chokepoints_Data/FeatureServer/0";
    static final String PORTS = SERVICES + "PortWatch_ports_database/FeatureServer/0";
    static final String PORTS_DAILY = SERVICES + "Daily_Ports_Data/FeatureServer/0";
    static final String FIXTURES = "imf-portwatch";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    PortWatchChokepointsAdapter chokepoints;
    PortWatchPortsAdapter ports;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(1000, 100_000));
        chokepoints = new PortWatchChokepointsAdapter(baseUrl());
        ports = new PortWatchPortsAdapter(baseUrl());
        List<String> scoped = new ArrayList<>();
        for (String scope : List.of("points", "daily", "ports")) {
            scoped.add(scope + "/" + ArcGisQueryClient.META_KEY);
            scoped.add(scope + "/" + ArcGisQueryClient.LAST_EDIT_KEY);
        }
        List<String> cpKeys = new ArrayList<>(scoped);
        cpKeys.addAll(List.of(PortWatchChokepointsAdapter.POINTS_KEY, PortWatchChokepointsAdapter.LAST_DATE_KEY));
        harness.reset(chokepoints.id(), cpKeys.toArray(String[]::new));
        List<String> portKeys = new ArrayList<>(scoped);
        portKeys.addAll(List.of(PortWatchPortsAdapter.POINTS_KEY, PortWatchPortsAdapter.LAST_DATE_KEY,
                PortWatchPortsAdapter.SCOPE_KEY, LiveIdSet.LIVE_PREFIX + PortWatchPortsAdapter.PORT_KIND,
                LiveIdSet.GONE_PREFIX + PortWatchPortsAdapter.PORT_KIND));
        harness.reset(ports.id(), portKeys.toArray(String[]::new));
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(chokepoints.id());
        harness.backoff.onSuccess(ports.id());
    }

    /** Serves a layer document with its edit date set to {@code lastEdit}. */
    private static void serveLayer(String path, String fixture, long lastEdit) {
        ObjectNode layer = (ObjectNode) JSON.readTree(AdapterFixtures.bytes(FIXTURES, fixture));
        ObjectNode editing = (ObjectNode) layer.get("editingInfo");
        editing.put("lastEditDate", lastEdit);
        editing.put("dataLastEditDate", lastEdit);
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).withQueryParam("f", equalTo("json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json").withBody(layer.toString())));
    }

    private static void serveQuery(String path, String where, int offset, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path + "/query")).withQueryParam("where", equalTo(where))
                .withQueryParam("resultOffset", equalTo(String.valueOf(offset))).willReturn(aResponse()
                        .withStatus(200).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private static List<JsonNode> features(String fixture) {
        List<JsonNode> out = new ArrayList<>();
        JSON.readTree(AdapterFixtures.bytes(FIXTURES, fixture)).get("features").forEach(out::add);
        return out;
    }

    private static String collection(List<JsonNode> features, boolean exceeded) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("type", "FeatureCollection");
        ArrayNode array = root.putArray("features");
        features.forEach(array::add);
        if (exceeded) {
            root.putObject("properties").put("exceededTransferLimit", true);
        }
        return root.toString();
    }

    /** The chokepoint points and the daily table edited at {@code dailyEdit}. */
    private static void serveChokepoints(long dailyEdit) {
        serveLayer(CP, "chokepoints-layer.json", 1777549336329L);
        serveQuery(CP, "1=1", 0, new String(AdapterFixtures.bytes(FIXTURES, "chokepoints-page.json")));
        serveLayer(CP_DAILY, "daily-chokepoints-layer.json", dailyEdit);
    }

    @Test
    @DisplayName("chokepoints: the first run reads the backfill window by date, paged, joined to the points")
    void chokepointJoinAndPaging() throws Exception {
        serveChokepoints(1791288541457L);
        List<JsonNode> rows = features("daily-chokepoints-page.json");
        serveQuery(CP_DAILY, "date > DATE '2026-09-14'", 0, collection(rows.subList(0, 28), true));
        serveQuery(CP_DAILY, "date > DATE '2026-09-14'", 28, collection(rows.subList(28, 56), false));

        FetchResult result = chokepoints.fetch(harness.context(chokepoints, Map.of()));
        assertThat(result.records()).hasSize(56).noneMatch(NormalizedRecord::tombstone);
        NormalizedRecord suez = result.records().stream().filter(r -> r.externalId().equals("chokepoint1:2026-10-03"))
                .findFirst().orElseThrow();
        assertThat(suez.kind()).isEqualTo(PortWatchChokepointsAdapter.KIND);
        assertThat(suez.eventTime()).isEqualTo(Instant.parse("2026-10-03T00:00:00Z"));
        assertThat(suez.observedAt()).isEqualTo(suez.eventTime());
        assertThat(((Point) suez.geometry()).getX()).isEqualTo(32.43688221);
        assertThat(suez.payload()).containsEntry("chokepoint", "Suez Canal").containsEntry("nTotal", 46L)
                .containsEntry("nTanker", 15L).containsEntry("capacityTotal", 2019755L)
                .containsEntry("capacityTanker", 937710L);
        assertThat(result.records()).allMatch(r -> r.geometry() != null);
        FieldSpecContract.assertPayloadKeysDeclared(chokepoints.descriptor(), result.records());
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(CP_DAILY + "/query"))
                .withQueryParam("orderByFields", equalTo("date,ObjectId"))
                .withQueryParam("resultRecordCount", equalTo("1000")));
        assertThat(harness.client.stateStore(chokepoints.id()).get(PortWatchChokepointsAdapter.LAST_DATE_KEY))
                .contains("2026-10-04");
    }

    @Test
    @DisplayName("chokepoints: an unchanged daily table sends no table query; a new edit date queries after lastDate")
    void unchangedThenIncremental() throws Exception {
        serveChokepoints(1791288541457L);
        serveQuery(CP_DAILY, "date > DATE '2026-09-14'", 0,
                new String(AdapterFixtures.bytes(FIXTURES, "daily-chokepoints-page.json")));
        chokepoints.fetch(harness.context(chokepoints, Map.of()));

        WIREMOCK.resetRequests();
        harness.clock.advance(Duration.ofHours(12));
        FetchResult unchanged = chokepoints.fetch(harness.context(chokepoints, Map.of()));
        assertThat(unchanged.notModified()).isTrue();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(CP_DAILY + "/query")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(CP + "/query")));

        serveLayer(CP_DAILY, "daily-chokepoints-layer.json", 1791893341457L);
        serveQuery(CP_DAILY, "date > DATE '2026-10-04'", 0, collection(List.of(), false));
        FetchResult incremental = chokepoints.fetch(harness.context(chokepoints, Map.of()));
        assertThat(incremental.notModified()).isFalse();
        assertThat(incremental.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(CP_DAILY + "/query"))
                .withQueryParam("where", equalTo("date > DATE '2026-10-04'")));
    }

    @Test
    @DisplayName("chokepoints: the backfill parameter is bounded; a failed points read fails the run")
    void chokepointParams() {
        assertThat(chokepoints.checkParams(ai.foresignal.source.spi.ValidatedParams.of(
                Map.of(PortWatchChokepointsAdapter.BACKFILL_PARAM, 0)))).containsKey("backfillDays");
        assertThat(chokepoints.checkParams(ai.foresignal.source.spi.ValidatedParams.of(
                Map.of(PortWatchChokepointsAdapter.BACKFILL_PARAM, 90)))).isEmpty();
        serveLayer(CP, "chokepoints-layer.json", 1777549336329L);
        WIREMOCK.stubFor(get(urlPathEqualTo(CP + "/query")).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> chokepoints.fetch(harness.context(chokepoints, Map.of())))
                .isInstanceOf(AdapterException.class);
    }

    @Test
    @DisplayName("window arithmetic: backfill bound, resume after a cut read, never backwards")
    void window() {
        LocalDate today = LocalDate.parse("2026-10-14");
        assertThat(PortWatchSupport.from(null, today, 30)).isEqualTo(LocalDate.parse("2026-09-14"));
        assertThat(PortWatchSupport.from(LocalDate.parse("2026-10-01"), today, 30)).isEqualTo("2026-10-01");
        LocalDate from = LocalDate.parse("2026-09-14");
        assertThat(PortWatchSupport.nextLastDate(from, LocalDate.parse("2026-10-04"), true)).isEqualTo("2026-10-04");
        assertThat(PortWatchSupport.nextLastDate(from, LocalDate.parse("2026-10-04"), false)).isEqualTo("2026-10-03");
        assertThat(PortWatchSupport.nextLastDate(from, LocalDate.parse("2026-09-15"), false)).isEqualTo(from);
        assertThat(PortWatchSupport.nextLastDate(from, null, true)).isEqualTo(from);
        assertThat(PortWatchSupport.dateWhere(from)).isEqualTo("date > DATE '2026-09-14'");
        assertThat(PortWatchSupport.date(JSON.readTree("1759536000000"))).isEqualTo("2025-10-04");
        assertThat(PortWatchSupport.date(JSON.readTree("\"2026-10-04\""))).isEqualTo("2026-10-04");
    }

    /** The port catalog edited at {@code catalogEdit} and the daily table edited at {@code dailyEdit}. */
    private static void servePorts(long catalogEdit, String catalogWhere, List<JsonNode> catalog, long dailyEdit) {
        serveLayer(PORTS, "ports-layer.json", catalogEdit);
        serveQuery(PORTS, catalogWhere, 0, collection(catalog, false));
        serveLayer(PORTS_DAILY, "daily-ports-layer.json", dailyEdit);
    }

    @Test
    @DisplayName("ports: catalog records at the layer's edit date, activity joined to the ports, missing ports"
            + " tombstoned on the next changed catalog")
    void portsCatalogAndActivity() throws Exception {
        List<JsonNode> catalog = features("ports-page.json");
        servePorts(1789742026415L, "1=1", catalog, 1791312087589L);
        serveQuery(PORTS_DAILY, "date > DATE '2026-09-30'", 0,
                new String(AdapterFixtures.bytes(FIXTURES, "daily-ports-page.json")));

        FetchResult first = ports.fetch(harness.context(ports, Map.of(PortWatchPortsAdapter.BACKFILL_PARAM, 14)));
        List<NormalizedRecord> portRecords = first.records().stream()
                .filter(r -> r.kind().equals(PortWatchPortsAdapter.PORT_KIND)).toList();
        List<NormalizedRecord> activity = first.records().stream()
                .filter(r -> r.kind().equals(PortWatchPortsAdapter.ACTIVITY_KIND)).toList();
        assertThat(portRecords).hasSize(20);
        assertThat(activity).hasSize(40).allMatch(r -> r.geometry() != null);
        NormalizedRecord tsuruga = portRecords.stream().filter(r -> r.externalId().equals("port1325")).findFirst()
                .orElseThrow();
        assertThat(tsuruga.observedAt()).isEqualTo(Instant.ofEpochMilli(1789742026415L));
        assertThat(tsuruga.payload()).containsEntry("name", "Tsuruga").containsEntry("iso3", "JPN")
                .containsEntry("locode", "JP TRG").containsEntry("vesselCountTotal", 529L)
                .containsEntry("industries", List.of("Mineral Products", "Wood & Wood Products", "Vegetable Products"));
        NormalizedRecord day = activity.stream().filter(r -> r.externalId().equals("port1325:2026-10-01")).findFirst()
                .orElseThrow();
        assertThat(day.payload()).containsEntry("portcalls", 1L).containsEntry("importTonnes", 3044L)
                .containsEntry("exportTonnes", 187L);
        assertThat(day.geometry().getCoordinate().x).isEqualTo(136.0706107);
        FieldSpecContract.assertPayloadKeysDeclared(ports.descriptor(), first.records());

        WIREMOCK.resetRequests();
        assertThat(ports.fetch(harness.context(ports, Map.of())).notModified()).as("both layers unchanged").isTrue();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PORTS_DAILY + "/query")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(PORTS + "/query")));

        servePorts(1789742099999L, "1=1", catalog.subList(1, 20), 1791312087589L);
        FetchResult changed = ports.fetch(harness.context(ports, Map.of()));
        assertThat(changed.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly(catalog.get(0).get("properties").get("portid").asString());
        assertThat(changed.records()).filteredOn(r -> r.kind().equals(PortWatchPortsAdapter.ACTIVITY_KIND)).isEmpty();
    }

    @Test
    @DisplayName("ports: the port filter narrows both queries, and changing it starts over")
    void portFilter() throws Exception {
        List<JsonNode> catalog = features("ports-page.json").subList(0, 1);
        String filter = "portid IN ('port1325')";
        servePorts(1789742026415L, filter, catalog, 1791312087589L);
        serveQuery(PORTS_DAILY, "date > DATE '2026-09-30' AND " + filter, 0, collection(List.of(), false));
        Map<String, Object> params = Map.of(PortWatchPortsAdapter.PORTS_PARAM, List.of("port1325"));
        FetchResult result = ports.fetch(harness.context(ports, params));
        assertThat(result.records()).hasSize(1);
        assertThat(harness.client.stateStore(ports.id()).get(PortWatchPortsAdapter.SCOPE_KEY)).contains("port1325");

        String two = "portid IN ('port1325','port1326')";
        serveQuery(PORTS, two, 0, collection(catalog, false));
        serveQuery(PORTS_DAILY, "date > DATE '2026-09-30' AND " + two, 0, collection(List.of(), false));
        FetchResult widened = ports.fetch(harness.context(ports, Map.of(PortWatchPortsAdapter.PORTS_PARAM,
                List.of("port1326", "port1325"))));
        assertThat(widened.notModified()).as("a new filter forces both reads").isFalse();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(PORTS + "/query")).withQueryParam("where", equalTo(two)));

        assertThat(ports.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of(
                PortWatchPortsAdapter.PORTS_PARAM, List.of("port1'; DROP"))))).containsKey("ports");
        assertThat(PortWatchPortsAdapter.portFilter(List.of())).isEqualTo("1=1");
    }

    @Test
    @DisplayName("the point table survives the state round trip")
    void pointTable() {
        Map<String, PortWatchSupport.Place> places = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 3000; i++) {
            places.put("port" + i, new PortWatchSupport.Place(i / 100.0, -i / 200.0, i % 2 == 0 ? "Port " + i : null));
        }
        String stored = PortWatchSupport.encode(places);
        assertThat(stored).startsWith("gz:");
        assertThat(PortWatchSupport.decode(stored)).isEqualTo(places);
        assertThat(PortWatchSupport.decode("gz:not-base64!")).isEmpty();
        assertThat(PortWatchSupport.decode(null)).isEmpty();
    }

    @Test
    @DisplayName("the descriptors: IMF PortWatch terms, 12 h, 20/min and 200/day, series and reference retention")
    void descriptors() {
        for (SourceAdapter a : List.of(chokepoints, ports)) {
            assertThat(DescriptorValidator.problems(a.descriptor(), a)).as(a.id()).isEmpty();
            assertThat(a.descriptor().licence()).isEqualTo("IMF PortWatch terms (attribution: IMF PortWatch)");
            assertThat(a.descriptor().termsUrl()).isEqualTo("https://www.imf.org/en/About/copyright-and-terms");
            assertThat(a.descriptor().defaultInterval()).isEqualTo(Duration.ofHours(12));
            assertThat(a.descriptor().defaultBudget()).isEqualTo(Budget.of(20, 200));
        }
        assertThat(chokepoints.descriptor().rankField()).isEqualTo("nTotal");
        assertThat(chokepoints.descriptor().defaultRetention()).isEqualTo(Duration.ofDays(3650));
        assertThat(ports.descriptor().kindRetention()).containsEntry("port", Duration.ofDays(90))
                .containsEntry("port-activity", Duration.ofDays(3650));
    }

    private static FeatureSourceSpec disruptionSpec() {
        List<String> problems = new ArrayList<>();
        List<KitSpecLoader.Loaded<FeatureSourceSpec>> loaded = KitSpecLoader.classpath("kits/features",
                FeatureSourceSpec.class, problems).stream().filter(l -> l.origin().endsWith("imf-portwatch.json"))
                .toList();
        assertThat(problems).isEmpty();
        assertThat(FeatureSpecValidator.problems(loaded, Set.of(), null)).isEmpty();
        assertThat(loaded).hasSize(1);
        return loaded.getFirst().spec();
    }

    @Test
    @DisplayName("disruptions: the kit spec validates; validFrom is fromdate, validUntil todate when set, no HTML")
    void disruptions() {
        FeatureSourceSpec spec = disruptionSpec();
        assertThat(spec.id()).isEqualTo("imf-portwatch-disruptions");
        assertThat(FeatureSpecValidator.ID_PATTERN.matcher(spec.id()).matches()).isTrue();
        assertThat(spec.tombstonesAbsent()).isTrue();
        assertThat(spec.kind()).isEqualTo("port-disruption");
        assertThat(spec.license()).isEqualTo("IMF PortWatch terms (attribution: IMF PortWatch)");
        assertThat(spec.intervalDuration()).isEqualTo(Duration.ofHours(12));
        List<SourceAdapter> adapters = new FeatureKitFamily(List.of(spec), "", null).adapters();
        assertThat(adapters).hasSize(1);
        assertThat(DescriptorValidator.problems(adapters.getFirst().descriptor(), adapters.getFirst())).isEmpty();
        assertThat(adapters.getFirst().descriptor().defaultBudget()).isEqualTo(Budget.of(20, 200));

        List<NormalizedRecord> records = new ArrayList<>();
        for (JsonNode feature : features("disruptions-page.json")) {
            FeatureMapper.Mapped mapped = FeatureMapper.map(spec, feature);
            assertThat(mapped.accepted()).as(mapped.reason()).isTrue();
            records.add(mapped.record());
        }
        NormalizedRecord hormuz = records.stream().filter(r -> r.externalId().equals("10000004")).findFirst()
                .orElseThrow();
        assertThat(hormuz.validFrom()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(hormuz.validUntil()).as("an open disruption").isNull();
        assertThat(hormuz.payload()).containsEntry("name", "HORMUZ-26").containsEntry("alertLevel", "RED")
                .containsEntry("affectedPorts", "chokepoint6").doesNotContainKey("htmldescription")
                .doesNotContainKey("htmlDescription");
        NormalizedRecord baltimore = records.stream().filter(r -> r.externalId().equals("10000002")).findFirst()
                .orElseThrow();
        assertThat(baltimore.validFrom()).isEqualTo(Instant.ofEpochMilli(1711411200000L));
        assertThat(baltimore.validUntil()).isEqualTo(Instant.ofEpochMilli(1718164800000L));
        assertThat(baltimore.geometry().getGeometryType()).isEqualTo("Polygon");
        assertThat(baltimore.payload()).containsEntry("country", "United States").containsEntry("nAffectedPorts", 1L);
    }
}
