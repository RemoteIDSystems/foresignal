package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.goes.GoesFdcAdapter;
import ai.foresignal.source.adapters.goes.GoesGlmAdapter;
import ai.foresignal.source.adapters.goes.GoesWestAbiAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpAggregatorAdapter;
import ai.foresignal.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Supersede, never remove (prompt R2-022): with direct satellite reception live for GOES-East and
 * {@code disableSupersededSources}, the NOAA ABI imagery pull {@code goes-east-abi} is turned off and held off by
 * {@code enable-core} and {@code enable-free}, yet stays registered; {@code goes-west-abi}, GLM and FDC are untouched;
 * turning the aggregator off turns {@code goes-east-abi} back on.
 */
@IntegrationTest
class SatelliteSupersededIT extends AggregatorItSupport {

    private static final String EAST = GoesAbiAdapter.ID;

    private static final List<String> UNTOUCHED = List.of(GoesWestAbiAdapter.ID, GoesGlmAdapter.ID, GoesFdcAdapter.ID);

    private void goLive(String goes, boolean disableSuperseded) {
        turnedOn.add(SatDumpAggregatorAdapter.ID);
        aggregators.update("satellite", json("{'enabled':true,'mode':'receiver','endpoint':'http://127.0.0.1:8081',"
                + "'config':{'location':'51.47,-0.45'" + (goes == null ? "" : ",'goes':'" + goes + "'") + "},"
                + "'disableSupersededSources':" + disableSuperseded + "}"));
    }

    @Test
    @DisplayName("going live for GOES-East turns goes-east-abi off; it stays registered; the rest are untouched")
    void liveTurnsTheImageryPullOff() {
        turnOn(EAST);
        UNTOUCHED.forEach(this::turnOn);

        goLive("east", true);

        assertThat(sources.view(EAST).enabled()).isFalse();
        assertThat(registry.ids()).as("it stays in the catalog").contains(EAST);
        assertThat(aggregators.isSuperseded(EAST)).isTrue();
        assertThat(aggregators.settings("satellite").disabledByAggregator()).containsExactly(EAST);
        for (String id : UNTOUCHED) {
            assertThat(sources.view(id).enabled()).as(id).isTrue();
            assertThat(aggregators.isSuperseded(id)).isFalse();
        }
        assertThat(aggregators.get("satellite").get("supersededSources")).isEqualTo(List.of(EAST));
    }

    @Test
    @DisplayName("a polar-only station supersedes nothing")
    void polarOnlySupersedesNothing() {
        turnOn(EAST);

        goLive(null, true);

        assertThat(sources.view(EAST).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(EAST)).isFalse();
    }

    @Test
    @DisplayName("while live, enable-core and enable-free leave goes-east-abi off, but an operator may turn it on")
    void bulkEnableSkipsTheImageryPull() {
        goLive("east", true);

        SourceService.EnableFreeResult core = sources.enableCore();
        turnedOn.addAll(core.enabled());
        assertThat(core.enabled()).doesNotContain(EAST);
        SourceService.EnableFreeResult free = sources.enableFree();
        turnedOn.addAll(free.enabled());
        assertThat(free.enabled()).doesNotContain(EAST);

        turnOn(EAST);
        assertThat(sources.view(EAST).enabled()).as("a hand enable is honored").isTrue();
    }

    @Test
    @DisplayName("turning the aggregator off turns goes-east-abi back on")
    void offRestoresTheImageryPull() {
        turnOn(EAST);

        goLive("east", true);
        aggregators.update("satellite", json("{'enabled':false,'endpoint':'http://127.0.0.1:8081',"
                + "'config':{'location':'51.47,-0.45','goes':'east'}}"));

        assertThat(sources.view(EAST).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(EAST)).isFalse();
        assertThat(aggregators.settings("satellite").disabledByAggregator()).isEmpty();
    }
}
