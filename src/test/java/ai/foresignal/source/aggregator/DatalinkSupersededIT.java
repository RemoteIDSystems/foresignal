package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.acars.LocalAcarsAggregatorAdapter;
import ai.foresignal.source.adapters.radio.AprsIsAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAggregatorAdapter;
import ai.foresignal.test.IntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Supersede, never remove (prompt R2-021): with the datalink aggregator live and {@code disableSupersededSources}, the
 * public APRS-IS network ({@code aprs-is}) is turned off and held off by {@code enable-core} and {@code enable-free},
 * yet stays registered and can be turned on by hand; turning the aggregator off turns it back on.
 */
@IntegrationTest
class DatalinkSupersededIT extends AggregatorItSupport {

    private static final String APRS_IS = AprsIsAdapter.ID;

    @Autowired JdbcTemplate jdbc;

    /**
     * {@code aprs-is} off with no stored filter again: other tests rely on it refusing to be enabled without one, and
     * a required parameter cannot be cleared through the service.
     */
    @AfterEach
    void resetAprsIs() {
        jdbc.update("update source_settings set enabled = false, params = '{}'::jsonb where source_id = ?", APRS_IS);
    }

    /** Turns {@code aprs-is} on with a narrow filter, as an operator would. */
    private void turnOnAprsIs() {
        sources.update(APRS_IS, new SourceSettingsUpdate(true, null, Map.of(AprsIsAdapter.FILTER,
                "r/37.77/-122.42/50"), null, null, false, null, false));
    }

    private void goLive(boolean disableSuperseded) {
        turnedOn.add(LocalAcarsAggregatorAdapter.ID);
        turnedOn.add(LocalAprsAggregatorAdapter.ID);
        aggregators.update("datalink", json("{'enabled':true,'mode':'receiver','endpoint':'tcp://127.0.0.1',"
                + "'config':{'acars':15550,'vdlm2':15555,'aprs':8001},'disableSupersededSources':" + disableSuperseded
                + "}"));
    }

    @Test
    @DisplayName("going live turns aprs-is off; it stays registered")
    void liveTurnsAprsIsOff() {
        turnOnAprsIs();

        goLive(true);

        assertThat(sources.view(APRS_IS).enabled()).isFalse();
        assertThat(registry.ids()).as("it stays in the catalog").contains(APRS_IS);
        assertThat(aggregators.isSuperseded(APRS_IS)).isTrue();
        assertThat(aggregators.settings("datalink").disabledByAggregator()).containsExactly(APRS_IS);
        assertThat(sources.view(LocalAcarsAggregatorAdapter.ID).enabled()).isTrue();
        assertThat(sources.view(LocalAprsAggregatorAdapter.ID).enabled()).isTrue();
    }

    @Test
    @DisplayName("while live, enable-core and enable-free leave aprs-is off, but an operator may turn it on")
    void bulkEnableSkipsAprsIs() {
        goLive(true);

        SourceService.EnableFreeResult core = sources.enableCore();
        turnedOn.addAll(core.enabled());
        assertThat(core.enabled()).doesNotContain(APRS_IS);
        SourceService.EnableFreeResult free = sources.enableFree();
        turnedOn.addAll(free.enabled());
        assertThat(free.enabled()).doesNotContain(APRS_IS);

        turnOnAprsIs();
        assertThat(sources.view(APRS_IS).enabled()).as("a hand enable is honored").isTrue();
    }

    @Test
    @DisplayName("turning the aggregator off turns aprs-is back on")
    void offRestoresAprsIs() {
        turnOnAprsIs();

        goLive(true);
        aggregators.update("datalink", json("{'enabled':false,'endpoint':'tcp://127.0.0.1','config':{'aprs':8001}}"));

        assertThat(sources.view(APRS_IS).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(APRS_IS)).isFalse();
        assertThat(aggregators.settings("datalink").disabledByAggregator()).isEmpty();
    }

    @Test
    @DisplayName("with disableSupersededSources false aprs-is stays on")
    void keepAprsIsOn() {
        turnOnAprsIs();

        goLive(false);

        assertThat(sources.view(APRS_IS).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(APRS_IS)).isFalse();
    }
}
