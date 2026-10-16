package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.maritime.GfwEventsAdapter;
import ai.foresignal.source.adapters.maritime.NgaMsiAdapter;
import ai.foresignal.source.adapters.maritime.PortWatchChokepointsAdapter;
import ai.foresignal.source.adapters.maritime.PortWatchPortsAdapter;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 088 maritime sources end to end through the real fetch path and ingest writer: every source registers
 * disabled; chokepoint-day records are idempotent when the same days are fetched again; an NGA warning that a later
 * warning cancels, and that leaves the active list, becomes a tombstone version.
 */
class MaritimeIngestIT extends WeatherIngestSupport {

    static final String SERVICES = "/services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services/";
    static final String CP = SERVICES + "PortWatch_chokepoints_database/FeatureServer/0";
    static final String CP_DAILY = SERVICES + "Daily_Chokepoints_Data/FeatureServer/0";
    static final String NGA = "/msi.nga.mil/api/publications/broadcast-warn";
    static final List<String> IDS = List.of(NgaMsiAdapter.ID, PortWatchChokepointsAdapter.ID, PortWatchPortsAdapter.ID,
            "imf-portwatch-disruptions", GfwEventsAdapter.ID);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired UpstreamClient client;
    @Autowired SourceRegistry registry;

    @BeforeEach
    void setUp() {
        AdapterStateStore cp = client.stateStore(PortWatchChokepointsAdapter.ID);
        for (String key : List.of(PortWatchChokepointsAdapter.POINTS_KEY, PortWatchChokepointsAdapter.LAST_DATE_KEY,
                PortWatchChokepointsAdapter.RESUME_KEY,
                "points/" + ArcGisQueryClient.META_KEY, "points/" + ArcGisQueryClient.LAST_EDIT_KEY,
                "daily/" + ArcGisQueryClient.META_KEY, "daily/" + ArcGisQueryClient.LAST_EDIT_KEY)) {
            cp.remove(key);
        }
        AdapterStateStore nga = client.stateStore(NgaMsiAdapter.ID);
        nga.remove(LiveIdSet.LIVE_PREFIX + NgaMsiAdapter.LIVE_SCOPE);
        nga.remove(LiveIdSet.GONE_PREFIX + NgaMsiAdapter.LIVE_SCOPE);
        configure(PortWatchChokepointsAdapter.ID, false, Map.of());
        configure(NgaMsiAdapter.ID, false, Map.of());
    }

    @AfterEach
    void disable() {
        configure(PortWatchChokepointsAdapter.ID, false, Map.of());
        configure(NgaMsiAdapter.ID, false, Map.of());
    }

    private static void serveLayer(String path, String fixture) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).withQueryParam("f", equalTo("json")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("imf-portwatch", fixture))));
    }

    private static void serveQuery(String path, String fixture) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path + "/query")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes("imf-portwatch",
                        fixture))));
    }

    private static void serveNga(String json) {
        UPSTREAM.stubFor(get(urlPathEqualTo(NGA)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(json.getBytes(StandardCharsets.UTF_8))));
    }

    /** The captured active list plus the hand-made warnings {@code navArea/msgYear/msgNumber} named. */
    private static String list(String... handmadeIds) {
        ObjectNode root = (ObjectNode) JSON.readTree(AdapterFixtures.bytes(NgaMsiAdapter.ID,
                "broadcast-warn-active.json"));
        ArrayNode warnings = (ArrayNode) root.get("broadcast-warn");
        JsonNode handmade = JSON.readTree(AdapterFixtures.bytes(NgaMsiAdapter.ID, "handmade.json"))
                .get("broadcast-warn");
        for (String id : handmadeIds) {
            for (JsonNode w : handmade) {
                String wid = w.get("navArea").asString() + "/" + w.get("msgYear").intValue() + "/"
                        + w.get("msgNumber").intValue();
                if (wid.equals(id)) {
                    warnings.add(w);
                }
            }
        }
        return root.toString();
    }

    /** A warning that {@code 12/2026/904} cancels ({@code CANCEL HYDROPAC 1234/26}). */
    private static String withCanceledWarning() {
        ObjectNode root = (ObjectNode) JSON.readTree(list());
        ObjectNode target = ((ArrayNode) root.get("broadcast-warn")).addObject();
        target.put("msgYear", 2026).put("msgNumber", 1234).put("navArea", "P").put("subregion", "97")
                .put("text", "CORAL SEA.\nDERELICT REPORTED IN 18-10.0S 152-20.0E.\n").put("status", "A")
                .put("issueDate", "130800Z OCT 2026").put("authority", "HANDMADE 0/26 130700Z OCT 26.");
        return root.toString();
    }

    @Test
    @DisplayName("every maritime source registers, disabled")
    void registered() {
        for (String id : IDS) {
            assertThat(registry.contains(id)).as(id).isTrue();
            assertThat(sources.view(id).enabled()).as(id).isFalse();
        }
    }

    @Test
    @DisplayName("imf-portwatch-chokepoints: re-fetching the same days stores no new versions")
    void chokepointsIdempotent() {
        clock.setInstant(Instant.parse("2026-10-14T06:00:00Z"));
        serveLayer(CP, "chokepoints-layer.json");
        serveQuery(CP, "chokepoints-page.json");
        serveLayer(CP_DAILY, "daily-chokepoints-layer.json");
        serveQuery(CP_DAILY, "daily-chokepoints-page.json");
        assertThat(fetch(PortWatchChokepointsAdapter.ID).success()).isTrue();
        int before = jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class,
                PortWatchChokepointsAdapter.ID);
        assertThat(before).isEqualTo(56);
        assertThat(jdbc.queryForObject("select payload->>'nTotal' from record_head where source_id = ? and"
                + " external_id = ?", String.class, PortWatchChokepointsAdapter.ID, "chokepoint1:2026-10-03"))
                .isEqualTo("46");
        assertThat(jdbc.queryForObject("select ST_GeometryType(geom) from record_live where source_id = ? and"
                + " external_id = ?", String.class, PortWatchChokepointsAdapter.ID, "chokepoint1:2026-10-03"))
                .isEqualTo("ST_Point");

        // Forget the window and the edit date, so the same days are queried and stored again.
        AdapterStateStore state = client.stateStore(PortWatchChokepointsAdapter.ID);
        state.remove(PortWatchChokepointsAdapter.LAST_DATE_KEY);
        state.remove("daily/" + ArcGisQueryClient.LAST_EDIT_KEY);
        clock.setInstant(Instant.parse("2026-10-14T18:00:00Z"));
        assertThat(fetch(PortWatchChokepointsAdapter.ID).success()).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class,
                PortWatchChokepointsAdapter.ID)).isEqualTo(before);
        assertThat(versions(PortWatchChokepointsAdapter.ID, "chokepoint1:2026-10-03")).isEqualTo(1);
    }

    @Test
    @DisplayName("nga-msi-warnings: a canceled warning that leaves the list is a tombstone version")
    void canceledWarningTombstoned() {
        clock.setInstant(Instant.parse("2026-10-14T05:30:00Z"));
        serveNga(withCanceledWarning());
        assertThat(fetch(NgaMsiAdapter.ID).success()).isTrue();
        assertThat(tombstoned(NgaMsiAdapter.ID, "P/2026/1234")).isFalse();
        assertThat(jdbc.queryForObject("select ST_GeometryType(geom) from record_live where source_id = ? and"
                + " external_id = ?", String.class, NgaMsiAdapter.ID, "P/2026/1234")).isEqualTo("ST_Point");
        int before = versions(NgaMsiAdapter.ID, "P/2026/1234");

        clock.setInstant(Instant.parse("2026-10-14T15:30:00Z"));
        serveNga(list("12/2026/904"));
        assertThat(fetch(NgaMsiAdapter.ID).success()).isTrue();
        assertThat(jdbc.queryForObject("select payload->'cancels'->>0 from record_head where source_id = ? and"
                + " external_id = ?", String.class, NgaMsiAdapter.ID, "12/2026/904")).isEqualTo("P/2026/1234");
        assertThat(tombstoned(NgaMsiAdapter.ID, "P/2026/1234")).isTrue();
        assertThat(versions(NgaMsiAdapter.ID, "P/2026/1234")).isEqualTo(before + 1);
        assertThat(tombstoned(NgaMsiAdapter.ID, "4/2024/517")).isFalse();
    }
}
