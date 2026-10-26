package ai.foresignal.source.aggregator;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.acars.LocalAcarsAggregatorAdapter;
import ai.foresignal.source.adapters.env.airq.AirqLocalAggregatorAdapter;
import ai.foresignal.source.adapters.ais.LocalAisAggregatorAdapter;
import ai.foresignal.source.adapters.local.DavisWllAggregatorAdapter;
import ai.foresignal.source.adapters.local.EcowittAggregatorAdapter;
import ai.foresignal.source.adapters.local.LocalAdsbAggregatorAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAggregatorAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpAggregatorAdapter;
import ai.foresignal.test.WireMockSupport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The shared fixture of prompt R2-019's aggregator tests: an admin token, a stub readsb from {@link WireMockSupport},
 * and every aggregator off with no stored configuration before and after each test. Sources a test turns on are
 * listed in {@link #turnedOn} and turned off again, because the shared database outlives the test and a scheduler in
 * another context reconciles from it.
 */
abstract class AggregatorItSupport extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** A readsb aircraft.json with two aircraft. */
    static final String AIRCRAFT_JSON = """
            {"now": 1761480000.0, "messages": 100, "aircraft": [
              {"hex": "4ca7b4", "flight": "RYR1AB  ", "lat": 51.47, "lon": -0.45, "alt_baro": 3000, "seen": 0.5},
              {"hex": "a1b2c3", "flight": "UAL900  ", "lat": 40.64, "lon": -73.78, "alt_baro": 12000, "seen": 1.2}
            ]}""";

    @Autowired AdminTokenService adminTokens;
    @Autowired AggregatorService aggregators;
    @Autowired AppSettingService settings;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;

    /** {@code Bearer …} of an admin token. */
    String admin;

    /** Sources the test turned on, turned off afterwards. */
    final List<String> turnedOn = new ArrayList<>();

    /** An admin token and every aggregator off. */
    @BeforeEach
    void aggregatorFixture() {
        admin = "Bearer " + adminTokens.rotate();
        clearAggregators();
    }

    /** Every aggregator off, and the sources the test turned on off again. */
    @AfterEach
    void clearAfter() {
        clearAggregators();
        for (String id : turnedOn) {
            if (sources.view(id).enabled()) {
                sources.update(id, SourceSettingsUpdate.enabled(false));
            }
        }
        turnedOn.clear();
    }

    private void clearAggregators() {
        for (String domain : AggregatorService.DOMAINS.keySet()) {
            if (settings.get(AggregatorService.key(domain), AggregatorSettings.class).isPresent()) {
                aggregators.update(domain, json("{'enabled':false}"));
                settings.remove(AggregatorService.key(domain));
            }
        }
        for (String id : List.of(LocalAdsbAggregatorAdapter.ID, LocalAisAggregatorAdapter.ID,
                LocalAcarsAggregatorAdapter.ID, LocalAprsAggregatorAdapter.ID, SatDumpAggregatorAdapter.ID,
                AirqLocalAggregatorAdapter.ID, DavisWllAggregatorAdapter.ID, EcowittAggregatorAdapter.ID)) {
            if (sources.view(id).enabled()) {
                sources.update(id, SourceSettingsUpdate.enabled(false));
            }
        }
    }

    /** Turns {@code id} on and remembers to turn it off. */
    void turnOn(String id) {
        sources.update(id, SourceSettingsUpdate.enabled(true));
        turnedOn.add(id);
    }

    /** Parses {@code text}, single quotes standing for double. */
    static JsonNode json(String text) {
        return JSON.readTree(text.replace('\'', '"'));
    }

    /** The stub readsb's aircraft.json URL. */
    static String endpoint() {
        return url("/data/aircraft.json");
    }

    /** Stubs the readsb with {@link #AIRCRAFT_JSON}. */
    static void stubReadsb() {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get("/data/aircraft.json")
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json").withBody(AIRCRAFT_JSON)));
    }
}
