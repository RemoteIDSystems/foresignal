package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Supersede, never remove (prompt R2-019, EGR-7): with the ADS-B aggregator live and {@code disableSupersededSources},
 * the public mirrors and {@code gpsjam-daily} are turned off and kept off by seeding and {@code enable-core}, yet stay
 * registered and can be turned on by hand; turning the aggregator off turns back on exactly the ones it turned off.
 */
@IntegrationTest
class SupersededSourcesIT extends AggregatorItSupport {

    private static final List<String> MIRRORS = List.of("adsblol-world", "adsbfi-mil", "gpsjam-daily");

    private void goLive(boolean disableSuperseded) {
        turnedOn.add("local-adsb-aggregator");
        aggregators.update("adsb", json("{'enabled':true,'mode':'feed','endpoint':'" + endpoint() + "',"
                + "'disableSupersededSources':" + disableSuperseded + "}"));
    }

    @Test
    @DisplayName("going live turns the superseded sources off; they stay registered")
    void liveTurnsThemOff() {
        MIRRORS.forEach(this::turnOn);

        goLive(true);

        for (String id : MIRRORS) {
            assertThat(sources.view(id).enabled()).as(id).isFalse();
            assertThat(registry.ids()).as(id + " stays in the catalog").contains(id);
            assertThat(aggregators.isSuperseded(id)).isTrue();
        }
        assertThat(aggregators.settings("adsb").disabledByAggregator()).containsAll(MIRRORS);
        assertThat(sources.view("local-adsb-aggregator").enabled()).isTrue();
    }

    @Test
    @DisplayName("while live, enable-core and enable-free leave them off, but an operator may turn one on")
    void bulkEnableSkipsThem() {
        goLive(true);

        SourceService.EnableFreeResult core = sources.enableCore();
        turnedOn.addAll(core.enabled());
        assertThat(core.enabled()).doesNotContainAnyElementsOf(MIRRORS).doesNotContain("adsblol-mil");
        SourceService.EnableFreeResult free = sources.enableFree();
        turnedOn.addAll(free.enabled());
        assertThat(free.enabled()).doesNotContainAnyElementsOf(MIRRORS);

        turnOn("adsblol-world");
        assertThat(sources.view("adsblol-world").enabled()).as("a hand enable is honored").isTrue();
    }

    @Test
    @DisplayName("turning the aggregator off turns back on exactly the sources it turned off")
    void offRestoresThem() {
        turnOn("adsblol-world");
        turnOn("gpsjam-daily");

        goLive(true);
        aggregators.update("adsb", json("{'enabled':false,'endpoint':'" + endpoint() + "'}"));

        assertThat(sources.view("adsblol-world").enabled()).isTrue();
        assertThat(sources.view("gpsjam-daily").enabled()).isTrue();
        assertThat(sources.view("adsbfi-mil").enabled()).as("it was off before, so it stays off").isFalse();
        assertThat(aggregators.isSuperseded("adsblol-world")).isFalse();
        assertThat(aggregators.settings("adsb").disabledByAggregator()).isEmpty();
    }

    @Test
    @DisplayName("with disableSupersededSources false nothing is turned off or held off")
    void keepSupersededOn() {
        turnOn("adsblol-world");

        goLive(false);

        assertThat(sources.view("adsblol-world").enabled()).isTrue();
        assertThat(aggregators.isSuperseded("adsblol-world")).isFalse();
    }
}
