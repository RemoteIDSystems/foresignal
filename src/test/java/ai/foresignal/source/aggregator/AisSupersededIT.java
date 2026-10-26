package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.ais.DigitrafficAisAdapter;
import ai.foresignal.source.adapters.ais.KystverketAdapter;
import ai.foresignal.source.adapters.ais.LocalAisAggregatorAdapter;
import ai.foresignal.source.adapters.aisstream.AisStreamAdapter;
import ai.foresignal.test.IntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Supersede, never remove (prompt R2-020): with the AIS aggregator live and {@code disableSupersededSources}, the
 * public mirror {@code aisstream} is turned off and held off by {@code enable-core} and {@code enable-free}, yet stays
 * registered; the official regional feeds {@code ais-digitraffic} and {@code ais-kystverket} are never touched; turning
 * the aggregator off turns {@code aisstream} back on.
 */
@IntegrationTest
class AisSupersededIT extends AggregatorItSupport {

    private static final String AISSTREAM = AisStreamAdapter.ID;

    private static final List<String> OFFICIAL = List.of(DigitrafficAisAdapter.ID, KystverketAdapter.ID);

    /** {@code aisstream} needs a key to be turned on. */
    @BeforeEach
    void key() {
        sources.setCredentials(AISSTREAM, Map.of(AisStreamAdapter.API_KEY, "superseded-test-key"));
    }

    /** The key is removed again; the fixture turns the sources off. */
    @AfterEach
    void removeKey() {
        if (sources.view(AISSTREAM).enabled()) {
            sources.update(AISSTREAM, ai.foresignal.source.SourceSettingsUpdate.enabled(false));
        }
        sources.deleteCredentials(AISSTREAM);
    }

    private void goLive(boolean disableSuperseded) {
        turnedOn.add(LocalAisAggregatorAdapter.ID);
        aggregators.update("ais", json("{'enabled':true,'mode':'hybrid','endpoint':'tcp://127.0.0.1:5012',"
                + "'disableSupersededSources':" + disableSuperseded + "}"));
    }

    @Test
    @DisplayName("going live turns aisstream off; it stays registered; the official feeds are untouched")
    void liveTurnsTheMirrorOff() {
        turnOn(AISSTREAM);
        OFFICIAL.forEach(this::turnOn);

        goLive(true);

        assertThat(sources.view(AISSTREAM).enabled()).isFalse();
        assertThat(registry.ids()).as("it stays in the catalog").contains(AISSTREAM);
        assertThat(aggregators.isSuperseded(AISSTREAM)).isTrue();
        assertThat(aggregators.settings("ais").disabledByAggregator()).containsExactly(AISSTREAM);
        for (String id : OFFICIAL) {
            assertThat(sources.view(id).enabled()).as(id + " is an authority, not a mirror").isTrue();
            assertThat(aggregators.isSuperseded(id)).isFalse();
        }
        assertThat(sources.view(LocalAisAggregatorAdapter.ID).enabled()).isTrue();
    }

    @Test
    @DisplayName("while live, enable-core and enable-free leave aisstream off, but an operator may turn it on")
    void bulkEnableSkipsTheMirror() {
        goLive(true);

        SourceService.EnableFreeResult core = sources.enableCore();
        turnedOn.addAll(core.enabled());
        assertThat(core.enabled()).doesNotContain(AISSTREAM);
        SourceService.EnableFreeResult free = sources.enableFree();
        turnedOn.addAll(free.enabled());
        assertThat(free.enabled()).doesNotContain(AISSTREAM);

        turnOn(AISSTREAM);
        assertThat(sources.view(AISSTREAM).enabled()).as("a hand enable is honored").isTrue();
    }

    @Test
    @DisplayName("turning the aggregator off turns aisstream back on, and leaves the official feeds as they were")
    void offRestoresTheMirror() {
        turnOn(AISSTREAM);
        turnOn(KystverketAdapter.ID);

        goLive(true);
        aggregators.update("ais", json("{'enabled':false,'endpoint':'tcp://127.0.0.1:5012'}"));

        assertThat(sources.view(AISSTREAM).enabled()).isTrue();
        assertThat(sources.view(KystverketAdapter.ID).enabled()).isTrue();
        assertThat(sources.view(DigitrafficAisAdapter.ID).enabled()).as("it was off before, so it stays off").isFalse();
        assertThat(aggregators.isSuperseded(AISSTREAM)).isFalse();
        assertThat(aggregators.settings("ais").disabledByAggregator()).isEmpty();
    }

    @Test
    @DisplayName("with disableSupersededSources false aisstream stays on")
    void keepTheMirrorOn() {
        turnOn(AISSTREAM);

        goLive(false);

        assertThat(sources.view(AISSTREAM).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(AISSTREAM)).isFalse();
    }
}
