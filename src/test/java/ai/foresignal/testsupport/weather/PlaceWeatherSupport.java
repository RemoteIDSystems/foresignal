package ai.foresignal.testsupport.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.RasterPipeline;
import ai.foresignal.raster.sample.PlaceGridIndex;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.adapters.wx.WxPlacesGenerator;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared plumbing of the prompt 052 place-weather ITs (the three required by Weather-Imagery-Design §7.4 and
 * the place-delete one). They join the 047/048 weather ingest context ({@link WeatherIngestSupport}; no new
 * configuration), commit synthetic MRMS reflectivity frames through the real raster pipeline, run the real
 * {@code wx-places} source on the real DERIVED runtime, and judge alerts with the real trigger engine.
 */
public abstract class PlaceWeatherSupport extends WeatherIngestSupport {

    /** How long to wait for the runtime. */
    protected static final Duration WAIT = Duration.ofSeconds(30);

    /** The place's position (inside {@link SampleFixtures#TEXAS}). */
    protected static final double LON = -98.0;

    /** The place's position. */
    protected static final double LAT = 30.5;

    protected static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired protected DerivedRunner runner;
    @Autowired protected PlaceService places;
    @Autowired protected RasterPipeline pipeline;
    @Autowired protected TriggerRuleService rules;
    @Autowired protected TriggerEngine engine;
    @Autowired protected SampleService samples;

    /** Clears the grid-index cache and source-health cache. */
    @BeforeEach
    void resetPlaceWeather() {
        PlaceGridIndex.clearCache();
        samples.invalidateHealth();
    }

    /** Stops {@code wx-places}. */
    @AfterEach
    void stopPlaceWeather() {
        configure(WxPlacesGenerator.ID, false, Map.of());
        await().atMost(WAIT).until(() -> !runner.running().contains(WxPlacesGenerator.ID));
    }

    /** Creates a point place named {@code name} with a 20 km radius. */
    protected void place(String id, String name) {
        places.create(JSON.readTree("{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + LON + "," + LAT + "]},\"radiusKm\":20,"
                + "\"timezone\":\"America/Chicago\"}"));
    }

    /** Enables {@code wx-places} and waits for its first reconcile. */
    protected void enableWxPlaces() {
        configure(WxPlacesGenerator.ID, true, Map.of());
        await().atMost(WAIT).until(() -> runner.running().contains(WxPlacesGenerator.ID));
        idle();
    }

    /** Waits until {@code wx-places} has nothing left to do. */
    protected void idle() {
        assertThat(runner.awaitIdle(WxPlacesGenerator.ID, WAIT)).isTrue();
    }

    /** Commits a reflectivity frame of {@code codes} on {@link SampleFixtures#TEXAS} valid at {@code t}, and waits. */
    protected void reflectivity(Instant t, int[] codes) throws Exception {
        pipeline.commit(MrmsAdapter.ID, FrameInput.builder(MrmsAdapter.REFLECTIVITY).validTime(t)
                .grid(SampleFixtures.TEXAS).bands(DecodedBands.codes(codes)).upstreamKey("synthetic/" + t).build());
        idle();
    }

    /** Echo codes: a 52 dBZ disc of radius 3 km whose center is {@code km} east of the place. */
    protected static int[] coreAt(double km) {
        return SampleFixtures.echoes(SampleFixtures.TEXAS, LON, LAT, km, 3, 52);
    }

    /** Runs a tick of {@code wx-places} now and waits. */
    protected void tick() {
        runner.tickNow(WxPlacesGenerator.ID);
        idle();
    }

    /** The current payload of a place's record, or null when it has no live head. */
    protected JsonNode payload(String placeId) {
        List<String> rows = jdbc.queryForList("select payload::text from record_head where source_id = ? and external_id = ?"
                + " and not tombstone", String.class, WxPlacesGenerator.ID, "place:" + placeId);
        return rows.isEmpty() ? null : JSON.readTree(rows.get(0));
    }

    /** Creates a rule on {@code wx-places} records of the named place. */
    protected UUID rule(String name, String placeName, String path, String op, Object value, String mode) {
        String v = value instanceof String s ? "\"" + s + "\"" : String.valueOf(value);
        return rules.create(JSON.readTree("{\"name\":\"" + name + "\",\"sources\":[\"" + WxPlacesGenerator.ID + "\"],"
                + "\"kinds\":[\"" + WxPlacesGenerator.KIND + "\"],\"conditions\":[{\"path\":\"place\",\"op\":\"==\","
                + "\"value\":\"" + placeName + "\"},{\"path\":\"" + path + "\",\"op\":\"" + op + "\",\"value\":" + v + "}],"
                + "\"mode\":\"" + mode + "\"}")).rule().getId();
    }

    /** Alerts of {@code rule} after the engine caught up. */
    protected int alerts(UUID rule) {
        engine.catchUp();
        Integer count = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, rule);
        return count == null ? 0 : count;
    }
}
