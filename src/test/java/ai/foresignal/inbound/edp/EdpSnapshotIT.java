package ai.foresignal.inbound.edp;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.EdpConformance;
import ai.foresignal.testsupport.inbound.InboundIt;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.node.ObjectNode;

/**
 * Snapshot deletions (docs/External-Data-Protocol.md §3.2, invariant EXT-5): absence tombstones only
 * after a final, complete, validated snapshot of the same scope within the window and the absent
 * ratio; a missing page, a rejected item, an unhealthy status, a timeout or a scope mismatch removes
 * nothing; a mass deletion is held; another scope is never touched; a pull snapshot ends at the page
 * whose {@code nextCursor} is null.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class EdpSnapshotIT extends WireMockSupport {

    static final Instant T = Instant.parse("2026-10-11T18:00:00Z");
    static final String ID = "ext-harbor-cams";

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired SourceService sources;
    @Autowired FetchExecutor executor;
    @Autowired JdbcTemplate jdbc;

    String admin;
    InboundIt it;
    String key;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
    }

    void registerPush() throws Exception {
        key = register(EdpConformance.manifest("push-layer.json"), false);
    }

    String register(String manifest, boolean allowPrivate) throws Exception {
        MockHttpServletResponse response = it.perform(post("/v1/external/sources").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"manifest\":" + manifest + ",\"approve\":true,\"allowPrivateNetwork\":" + allowPrivate + "}"));
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("ingestKey").asString();
    }

    static String camera(String id, String region, String name) {
        return "{\"stream\":\"cameras\",\"type\":\"feature\",\"id\":\"" + id + "\",\"observedAt\":\"2026-10-11T17:59:00Z\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-71.05,42.36]},"
                + "\"properties\":{\"name\":\"" + name + "\",\"region\":\"" + region + "\",\"online\":true}}";
    }

    static String batch(String snapshot, String next, String... items) {
        return "{\"protocol\":\"foresignal-edp/1.0\",\"sourceId\":\"harbor-cams\",\"snapshot\":" + snapshot
                + ",\"items\":[" + String.join(",", items) + "],\"nextCursor\":" + (next == null ? "null" : "\"" + next + "\"")
                + "}";
    }

    static String snapshot(String id, String scope, int page, boolean last) {
        return "{\"id\":\"" + id + "\",\"stream\":\"cameras\",\"scope\":" + (scope == null ? "null" : "\"" + scope + "\"")
                + ",\"page\":" + page + ",\"final\":" + last + "}";
    }

    void push(String batch) throws Exception {
        MockHttpServletResponse response = it.push("/v1/external/sources/" + ID + "/items", key, MediaType.APPLICATION_JSON,
                batch);
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(200);
    }

    /** Seeds cam-1..cam-4 in region north and cam-5 in south, without a snapshot. */
    void seed() throws Exception {
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            items.add(camera("cam-" + i, "north", "Pier " + i));
        }
        items.add(camera("cam-5", "south", "Dock 5"));
        push(batch("null", null, items.toArray(String[]::new)));
    }

    List<String> live() {
        return jdbc.queryForList("select external_id from record_head where source_id = ? and not tombstone"
                + " order by external_id", String.class, ID);
    }

    @Test
    @DisplayName("a complete two-page snapshot of a scope removes only that scope's absent entity")
    void complete() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s1", "north", 1, false), null, camera("cam-1", "north", "Pier 1")));
        assertThat(live()).hasSize(5);
        push(batch(snapshot("s1", "north", 2, true), null, camera("cam-2", "north", "Pier 2"),
                camera("cam-3", "north", "Pier 3")));
        assertThat(live()).containsExactly("cameras:cam-1", "cameras:cam-2", "cameras:cam-3", "cameras:cam-5");
        String payload = jdbc.queryForObject("select payload::text from record_head where source_id = ? and"
                + " external_id = 'cameras:cam-4'", String.class, ID);
        assertThat(payload).isNotNull();
        assertThat(jdbc.queryForObject("select count(*) from edp_snapshot_stage", Integer.class)).isZero();
    }

    @Test
    @DisplayName("a missing page removes nothing")
    void missingPage() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s2", "north", 2, true), null, camera("cam-1", "north", "Pier 1"),
                camera("cam-2", "north", "Pier 2"), camera("cam-3", "north", "Pier 3")));
        assertThat(live()).hasSize(5);
    }

    @Test
    @DisplayName("a rejected item in any page removes nothing")
    void rejectedItem() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s3", "north", 1, true), null, camera("cam-1", "north", "Pier 1"),
                camera("cam-2", "north", "Pier 2"), camera("cam-3", "north", "Pier #3")));
        assertThat(live()).hasSize(5);
    }

    @Test
    @DisplayName("an unhealthy status during the snapshot removes nothing")
    void unhealthyStatus() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s4", "north", 1, false), null, camera("cam-1", "north", "Pier 1"),
                "{\"stream\":\"cameras\",\"type\":\"status\",\"health\":\"degraded\",\"message\":\"half the feed\"}"));
        push(batch(snapshot("s4", "north", 2, true), null, camera("cam-2", "north", "Pier 2"),
                camera("cam-3", "north", "Pier 3")));
        assertThat(live()).hasSize(5);
    }

    @Test
    @DisplayName("a snapshot that outlives its window removes nothing")
    void timeout() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s5", "north", 1, false), null, camera("cam-1", "north", "Pier 1")));
        clock.setInstant(T.plusSeconds(31 * 60));
        push(batch(snapshot("s5", "north", 2, true), null, camera("cam-2", "north", "Pier 2"),
                camera("cam-3", "north", "Pier 3")));
        assertThat(live()).hasSize(5);
    }

    @Test
    @DisplayName("an item outside the snapshot's scope removes nothing")
    void scopeMismatch() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s6", "north", 1, true), null, camera("cam-1", "north", "Pier 1"),
                camera("cam-2", "north", "Pier 2"), camera("cam-3", "north", "Pier 3"), camera("cam-5", "south", "Dock 5")));
        assertThat(live()).hasSize(5);
    }

    @Test
    @DisplayName("removing more than maxAbsentRatio is held and the source turns degraded")
    void heldAboveRatio() throws Exception {
        registerPush();
        seed();
        push(batch(snapshot("s7", "north", 1, true), null, camera("cam-1", "north", "Pier 1")));
        assertThat(live()).hasSize(5);
        assertThat(sources.view(ID).health().status()).isEqualTo(HealthStatus.DEGRADED);
        assertThat(jdbc.queryForObject("select held from edp_snapshot where snapshot_id = 's7'", Boolean.class)).isTrue();

        // The operator releases the held deletion once.
        MockHttpServletResponse allowed = it.perform(post("/v1/external/sources/" + ID + "/snapshots/s7/allow")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(allowed.getStatus()).as(allowed.getContentAsString()).isEqualTo(200);
        assertThat(body(allowed).get("removed").asInt()).isEqualTo(3);
        assertThat(live()).containsExactly("cameras:cam-1", "cameras:cam-5");
        assertThat(sources.view(ID).health().status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(it.perform(post("/v1/external/sources/" + ID + "/snapshots/s7/allow")
                .header(HttpHeaders.AUTHORIZATION, admin)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("a pull snapshot ends at the page whose nextCursor is null")
    void pullSnapshot() throws Exception {
        ObjectNode manifest = (ObjectNode) InboundIt.JSON.readTree(EdpConformance.manifest("push-layer.json"));
        ObjectNode transport = (ObjectNode) manifest.get("transport");
        transport.putObject("pull").put("itemsUrl", url("/cams/items")).put("intervalSeconds", 300);
        key = register(manifest.toString(), true);
        seed();
        WIREMOCK.stubFor(get(urlPathEqualTo("/cams/items")).withQueryParam("cursor", absent())
                .willReturn(okJson(batch(snapshot("p1", "north", 1, false), "next", camera("cam-1", "north", "Pier 1")))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/cams/items")).withQueryParam("cursor", equalTo("next"))
                .willReturn(okJson(batch(snapshot("p1", "north", 2, false), null, camera("cam-2", "north", "Pier 2"),
                        camera("cam-3", "north", "Pier 3")))));

        assertThat(executor.execute(ID, FetchTrigger.MANUAL).success()).isTrue();

        assertThat(live()).containsExactly("cameras:cam-1", "cameras:cam-2", "cameras:cam-3", "cameras:cam-5");
    }
}
