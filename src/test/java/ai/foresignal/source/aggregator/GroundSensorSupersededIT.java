package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.env.airq.AirqLocalAggregatorAdapter;
import ai.foresignal.source.adapters.env.openaq.OpenAqAdapter;
import ai.foresignal.source.adapters.nws.NwsAlertsAdapter;
import ai.foresignal.source.adapters.space.localsonde.LocalSondeAdapter;
import ai.foresignal.source.adapters.space.sondehub.SondehubAdapter;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.test.IntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Supersede, never remove (prompt R2-023): with the ground-sensor aggregator live, only the public mirrors of the
 * enabled sensors are turned off ({@code openaq} for air quality, {@code sondehub} for radiosondes); the authorities
 * (USGS, NWS) stay as the operator set them; a configuration that supersedes less gives back what it no longer
 * replaces; turning the aggregator off turns every mirror back on.
 */
@IntegrationTest
class GroundSensorSupersededIT extends AggregatorItSupport {

    private static final List<String> AUTHORITIES = List.of(UsgsEarthquakesAdapter.ID, NwsAlertsAdapter.ID);

    @Autowired JdbcTemplate jdbc;

    /** {@code openaq} needs a key and {@code sondehub} a center to be turned on. */
    @BeforeEach
    void mirrors() {
        sources.setCredentials(OpenAqAdapter.ID, Map.of(OpenAqAdapter.API_KEY, "superseded-test-key"));
        sources.update(SondehubAdapter.ID, new SourceSettingsUpdate(false, null, Map.of(SondehubAdapter.CENTER_PARAM,
                "37.77,-122.42"), null, null, false, null, false));
    }

    /** The mirrors off with no key or stored parameters again. */
    @AfterEach
    void resetMirrors() {
        for (String id : List.of(OpenAqAdapter.ID, SondehubAdapter.ID)) {
            jdbc.update("update source_settings set enabled = false, params = '{}'::jsonb where source_id = ?", id);
        }
        sources.deleteCredentials(OpenAqAdapter.ID);
    }

    private void goLive(boolean radiosonde, boolean disableSuperseded) {
        turnedOn.add(AirqLocalAggregatorAdapter.ID);
        turnedOn.add(LocalSondeAdapter.ID);
        aggregators.update("ground-sensors", json("{'enabled':true,'mode':'receiver','config':{"
                + "'airq':{'enabled':true,'deviceType':'purpleair','endpoint':'http://192.168.1.60'},"
                + "'radiosonde':{'enabled':" + radiosonde + ",'endpoint':'udp://0.0.0.0:55673',"
                + "'allowSenders':['192.168.1.20']}},'disableSupersededSources':" + disableSuperseded + "}"));
    }

    @Test
    @DisplayName("going live turns the mirrors of the enabled sensors off; the authorities are untouched")
    void liveTurnsMirrorsOff() {
        turnOn(OpenAqAdapter.ID);
        turnOn(SondehubAdapter.ID);
        AUTHORITIES.forEach(this::turnOn);

        goLive(true, true);

        assertThat(sources.view(OpenAqAdapter.ID).enabled()).isFalse();
        assertThat(sources.view(SondehubAdapter.ID).enabled()).isFalse();
        assertThat(registry.ids()).as("they stay in the catalog").contains(OpenAqAdapter.ID, SondehubAdapter.ID);
        assertThat(aggregators.isSuperseded(OpenAqAdapter.ID)).isTrue();
        AUTHORITIES.forEach(id -> assertThat(sources.view(id).enabled()).as(id).isTrue());
        AUTHORITIES.forEach(id -> assertThat(aggregators.isSuperseded(id)).as(id).isFalse());
    }

    @Test
    @DisplayName("turning a sensor off gives back its mirror; the aggregator off gives back all of them")
    void narrowingRestores() {
        turnOn(OpenAqAdapter.ID);
        turnOn(SondehubAdapter.ID);

        goLive(true, true);
        goLive(false, true);

        assertThat(sources.view(SondehubAdapter.ID).enabled()).as("radiosonde off").isTrue();
        assertThat(sources.view(OpenAqAdapter.ID).enabled()).as("airq still live").isFalse();
        assertThat(aggregators.settings("ground-sensors").disabledByAggregator()).containsExactly(OpenAqAdapter.ID);

        aggregators.update("ground-sensors", json("{'enabled':false}"));

        assertThat(sources.view(OpenAqAdapter.ID).enabled()).isTrue();
        assertThat(aggregators.settings("ground-sensors").disabledByAggregator()).isEmpty();
    }

    @Test
    @DisplayName("with disableSupersededSources false the mirrors stay on")
    void keepMirrorsOn() {
        turnOn(OpenAqAdapter.ID);

        goLive(false, false);

        assertThat(sources.view(OpenAqAdapter.ID).enabled()).isTrue();
        assertThat(aggregators.isSuperseded(OpenAqAdapter.ID)).isFalse();
    }
}
