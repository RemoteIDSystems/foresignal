package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ai.foresignal.derive.CoverageService;
import ai.foresignal.derive.DefaultObservationFeed;
import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.derive.LayerStatus;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.adapters.aisstream.AisStreamAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 099 derived layers end to end, on the real DERIVED runtime, ingest writer, observation feed and
 * trigger engine. Input health and nearby density are stubbed through {@link CoverageService} (the coverage
 * gates themselves are unit-tested per generator); the stub's window status still reads the real feed's
 * gap/drop state, so a live drop degrades the window exactly as in production.
 *
 * <p>The five required scenarios: a jamming cell appears, survives a sparse window as
 * {@code insufficient-data} and is tombstoned after recovery; an unchanged condition writes no new versions;
 * restart reconciliation tombstones a stale loiter; a LiveBus drop marks {@code degraded-input} and
 * suppresses gap events; an {@code enter} rule on {@code level == high} fires once.
 */
@WebIntegrationTest
class DerivedLayersIT {

    private static final String JAMMING = GpsJammingGenerator.ID;
    private static final String LOITER = LoiterGenerator.ID;
    private static final String GAP = AisGapGenerator.ID;
    private static final String AIRCRAFT = AdsbLolMilAdapter.ID;
    private static final String VESSELS = AisStreamAdapter.ID;
    private static final Duration WAIT = Duration.ofSeconds(30);
    private static final double LON = 30.0;
    private static final double LAT = 50.0;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired DerivedRunner runner;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired IngestQueue queue;
    @Autowired Clock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired DefaultObservationFeed feed;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;

    @MockitoBean CoverageService coverage;

    private TestRecords records;

    @BeforeEach
    void coverage() {
        records = new TestRecords(queue, clock);
        when(coverage.health(anyString())).thenReturn(HealthStatus.HEALTHY);
        when(coverage.healthy(anyString())).thenReturn(true);
        when(coverage.coverage(anyString())).thenReturn(Coverage.none());
        when(coverage.densityNear(anyString(), anyDouble(), anyDouble(), anyDouble(), any())).thenReturn(5);
        when(coverage.statusFor(any(), any())).thenAnswer(invocation ->
                feed.degradedSince(invocation.getArgument(1)) ? LayerStatus.DEGRADED_INPUT : LayerStatus.OK);
    }

    @AfterEach
    void stop() {
        for (String id : List.of(JAMMING, LOITER, GAP)) {
            enable(id, false);
        }
        await().atMost(WAIT).until(() -> !runner.running().contains(JAMMING) && !runner.running().contains(LOITER)
                && !runner.running().contains(GAP));
    }

    private void enable(String id, boolean enabled) {
        sources.update(id, new SourceSettingsUpdate(enabled, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    private void start(String id) {
        enable(id, true);
        await().atMost(WAIT).until(() -> runner.running().contains(id));
        assertThat(runner.awaitIdle(id, WAIT)).isTrue();
    }

    private void tick(String id) {
        runner.tickNow(id);
        assertThat(runner.awaitIdle(id, WAIT)).isTrue();
    }

    /** An airborne ADS-B aircraft at the test cell; bad when its NACp is 5, good at 10 (NIC 8 either way). */
    private void aircraft(String hex, boolean bad) {
        records.ingest(AIRCRAFT, NormalizedRecord.builder(hex, "aircraft").observedAt(clock.instant().minusSeconds(60))
                .point(LON, LAT).put("hex", hex).put("nac_p", bad ? 5 : 10).put("nic", 8).put("posSource", "adsb")
                .put("onGround", false).build());
    }

    private void gone(String hex) {
        records.ingest(AIRCRAFT, NormalizedRecord.tombstone(hex, "aircraft", clock.instant()));
    }

    private String cell() {
        return GpsJammingGenerator.key(LON, LAT, true);
    }

    /** The current payload of a derived record, or null when it has no live head. */
    private JsonNode head(String sourceId, String externalId) {
        List<String> rows = jdbc.queryForList("select payload::text from record_head where source_id = ?"
                + " and external_id = ? and not tombstone", String.class, sourceId, externalId);
        return rows.isEmpty() ? null : JSON.readTree(rows.get(0));
    }

    private boolean tombstoned(String sourceId, String externalId) {
        Integer n = jdbc.queryForObject("select count(*) from record_head where source_id = ? and external_id = ?"
                + " and tombstone", Integer.class, sourceId, externalId);
        return n != null && n > 0;
    }

    private int versions(String sourceId, String externalId) {
        Integer n = jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = ?",
                Integer.class, sourceId, externalId);
        return n == null ? 0 : n;
    }

    private int heads(String sourceId) {
        Integer n = jdbc.queryForObject("select count(*) from record_head where source_id = ? and not tombstone",
                Integer.class, sourceId);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("every derived layer is registered as a DERIVED source")
    void registered() {
        for (String id : new String[] {JAMMING, GAP, AircraftFusionGenerator.ID,
                CorroborationGenerator.ID, PortCallGenerator.ID, CpaGenerator.ID, LOITER,
                RendezvousGenerator.ID, DeceptionGenerator.ID, HoldingPatternGenerator.ID,
                EmergencySquawkGenerator.ID, ChokepointGenerator.ID, MilNavalGenerator.ID,
                FeltLocallyGenerator.ID, CableRiskGenerator.ID, DarkVesselMatchGenerator.ID,
                LightningHoldoverGenerator.ID, HfBlackoutGenerator.ID}) {
            assertThat(registry.find(id)).as(id).isPresent();
            assertThat(registry.get(id).descriptor().mode()).isEqualTo(Mode.DERIVED);
        }
    }

    @Test
    @DisplayName("a jamming cell appears, survives a sparse window as insufficient-data, and is tombstoned after"
            + " two recovered windows")
    void jammingCellLifecycle() {
        start(JAMMING);
        aircraft("a00001", true);
        aircraft("a00002", true);
        aircraft("a00003", true);
        aircraft("a00004", false);
        tick(JAMMING);
        JsonNode high = head(JAMMING, cell());
        assertThat(high).isNotNull();
        assertThat(high.get("level").asString()).isEqualTo("high");
        assertThat(high.get("status").asString()).isEqualTo("ok");

        // Sparse: one aircraft left in the cell. The level is kept, marked insufficient-data.
        gone("a00001");
        gone("a00002");
        gone("a00003");
        tick(JAMMING);
        JsonNode sparse = head(JAMMING, cell());
        assertThat(sparse).isNotNull();
        assertThat(sparse.get("level").asString()).isEqualTo("high");
        assertThat(sparse.get("status").asString()).isEqualTo("insufficient-data");

        // Recovery: four healthy aircraft. One window below medium keeps the cell; the second ends it.
        aircraft("a00005", false);
        aircraft("a00006", false);
        aircraft("a00007", false);
        tick(JAMMING);
        assertThat(tombstoned(JAMMING, cell())).isFalse();
        tick(JAMMING);
        assertThat(tombstoned(JAMMING, cell())).isTrue();
    }

    @Test
    @DisplayName("an unchanged high cell over six more windows writes no new versions, and an enter rule on"
            + " level == high fires once")
    void steadyStateAndEnterRule() {
        UUID rule = rules.create(JSON.readTree("{\"name\":\"Jamming high\",\"sources\":[\"" + JAMMING + "\"],"
                + "\"kinds\":[\"" + GpsJammingGenerator.KIND + "\"],"
                + "\"conditions\":[{\"path\":\"level\",\"op\":\"==\",\"value\":\"high\"}],\"mode\":\"enter\"}"))
                .rule().getId();
        start(JAMMING);
        aircraft("b00001", true);
        aircraft("b00002", true);
        aircraft("b00003", false);
        tick(JAMMING);
        assertThat(head(JAMMING, cell()).get("level").asString()).isEqualTo("high");
        int versions = versions(JAMMING, cell());
        assertThat(versions).isPositive();

        for (int i = 0; i < 6; i++) {
            tick(JAMMING);
        }

        assertThat(versions(JAMMING, cell())).as("versions after six unchanged windows").isEqualTo(versions);
        engine.catchUp();
        Integer alerts = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        assertThat(alerts).isEqualTo(1);
    }

    @Test
    @DisplayName("restart reconciliation ends a stale loiter whose vessel is no longer seen")
    void reconciliationTombstonesAStaleLoiter() {
        Instant then = clock.instant().minus(Duration.ofHours(3));
        records.ingest(LOITER, NormalizedRecord.builder("366999999", LoiterGenerator.KIND).observedAt(then)
                .point(LON, LAT).put("automated", true).put("status", "ok").put("fixes", 4).build());
        assertThat(head(LOITER, "366999999")).isNotNull();

        // Enabling reconciles: the record is adopted, and the first evaluation finds no such vessel.
        start(LOITER);
        tick(LOITER);
        await().atMost(WAIT).until(() -> tombstoned(LOITER, "366999999"));
    }

    @Test
    @DisplayName("without a degraded window a vessel silent for 40 minutes opens an AIS gap")
    void gapOpensWithCoverage() {
        Instant lastFix = clock.instant().minus(Duration.ofMinutes(40));
        records.ingest(VESSELS, NormalizedRecord.builder("366000777", "vessel").observedAt(lastFix).point(LON, LAT)
                .put("mmsi", "366000777").put("sog", 8.0).put("cog", 90.0).put("navStatus", 0).build());
        start(GAP);
        tick(GAP);
        assertThat(head(GAP, "366000777:" + lastFix.getEpochSecond())).isNotNull();
    }

    @Test
    @DisplayName("a LiveBus drop marks the window degraded-input and suppresses the AIS gap")
    void liveDropDegradesAndSuppressesGaps() {
        Instant lastFix = clock.instant().minus(Duration.ofMinutes(40));
        records.ingest(VESSELS, NormalizedRecord.builder("366000888", "vessel").observedAt(lastFix).point(LON, LAT)
                .put("mmsi", "366000888").put("sog", 8.0).put("cog", 90.0).put("navStatus", 0).build());
        aircraft("c00001", true);
        aircraft("c00002", true);
        aircraft("c00003", false);

        feed.onLiveDropped();

        start(GAP);
        tick(GAP);
        assertThat(heads(GAP)).as("no gap from a degraded window").isZero();

        start(JAMMING);
        tick(JAMMING);
        JsonNode cell = head(JAMMING, cell());
        assertThat(cell).isNotNull();
        assertThat(cell.get("status").asString()).isEqualTo("degraded-input");
    }
}
