package ai.foresignal.source.aggregator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.env.airq.AirqLocalAdapter;
import ai.foresignal.source.adapters.env.airq.AirqLocalAggregatorAdapter;
import ai.foresignal.source.adapters.env.openaq.OpenAqAdapter;
import ai.foresignal.source.adapters.env.sensorcommunity.SensorCommunityAdapter;
import ai.foresignal.source.adapters.local.DavisWllAggregatorAdapter;
import ai.foresignal.source.adapters.local.EcowittAggregatorAdapter;
import ai.foresignal.source.adapters.mesh.MeshCoreBridgeAdapter;
import ai.foresignal.source.adapters.mesh.MeshtasticMapAdapter;
import ai.foresignal.source.adapters.mesh.MeshtasticMqttAdapter;
import ai.foresignal.source.adapters.space.sondehub.SondehubAdapter;
import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code ground-sensors} domain (prompt R2-023): each enabled sub-section configures and turns on its own local
 * read; a sub-section turned off turns off what it configured and nothing else; {@code test()} reads the sensors and
 * sums their records. The stub PurpleAir answers {@code /json} with the recorded local answer.
 */
@IntegrationTest
class GroundSensorAggregatorTest extends AggregatorItSupport {

    @Autowired GroundSensorAggregator ground;

    /** {@code meshcore-bridge} is on by default; its state before the test. */
    private boolean bridgeWasOn;

    /** Remembers {@code meshcore-bridge}'s state. */
    @BeforeEach
    void rememberBridge() {
        bridgeWasOn = sources.view(MeshCoreBridgeAdapter.ID).enabled();
    }

    /** Turns this domain off first (it may turn the bridge off), then puts the bridge back as it was. */
    @AfterEach
    void restoreBridge() {
        aggregators.update("ground-sensors", json("{'enabled':false}"));
        settings.remove(AggregatorService.key("ground-sensors"));
        if (sources.view(MeshCoreBridgeAdapter.ID).enabled() != bridgeWasOn) {
            sources.update(MeshCoreBridgeAdapter.ID, SourceSettingsUpdate.enabled(bridgeWasOn));
        }
    }

    /** The stub PurpleAir's address. */
    private static String purpleAir() {
        return url("/").replaceAll("/$", "");
    }

    private static void stubPurpleAir() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/json")).willReturn(aResponse().withHeader("Content-Type",
                "application/json").withBody(AdapterFixtures.text("airq-local", "purpleair.json"))));
    }

    private static String airq(boolean enabled) {
        return "'airq':{'enabled':" + enabled + ",'deviceType':'purpleair','endpoint':'" + purpleAir() + "',"
                + "'name':'Back yard'}";
    }

    private static Predicate<ValidationException> hasField(String field) {
        return e -> e.getErrors().stream().map(FieldError::field).anyMatch(field::equals);
    }

    @Test
    @DisplayName("enabling a sensor configures and turns on its local read; turning it off turns that read off")
    void sensorRoutesToItsRead() {
        turnedOn.add(AirqLocalAggregatorAdapter.ID);
        aggregators.update("ground-sensors", json("{'enabled':true,'mode':'receiver','config':{" + airq(true) + ","
                + "'mesh':{'enabled':true,'bridge':true}},'disableSupersededSources':false}"));

        SourceView monitor = sources.view(AirqLocalAggregatorAdapter.ID);
        assertThat(monitor.enabled()).isTrue();
        assertThat(monitor.params()).containsEntry(AirqLocalAdapter.DEVICE_TYPE_PARAM, "purpleair")
                .containsEntry(AirqLocalAdapter.NAME_PARAM, "Back yard");
        assertThat(String.valueOf(monitor.params().get(AirqLocalAdapter.URL_PARAM))).isEqualTo(purpleAir());
        assertThat(sources.view(MeshCoreBridgeAdapter.ID).enabled()).isTrue();
        assertThat(ground.sourceIds()).containsExactly(AirqLocalAggregatorAdapter.ID, MeshCoreBridgeAdapter.ID);

        aggregators.update("ground-sensors", json("{'enabled':true,'mode':'receiver','config':{" + airq(false) + ","
                + "'mesh':{'enabled':true,'bridge':true}},'disableSupersededSources':false}"));

        assertThat(sources.view(AirqLocalAggregatorAdapter.ID).enabled()).as("airq turned off").isFalse();
        assertThat(sources.view(MeshCoreBridgeAdapter.ID).enabled()).as("mesh untouched").isTrue();

        aggregators.update("ground-sensors", json("{'enabled':false}"));

        assertThat(sources.view(MeshCoreBridgeAdapter.ID).enabled()).as("the domain off").isFalse();
    }

    @Test
    @DisplayName("a source no sub-section configured is left as the operator set it")
    void unmanagedSourcesAreLeftAlone() {
        sources.update(MeshCoreBridgeAdapter.ID, SourceSettingsUpdate.enabled(true));
        turnedOn.add(AirqLocalAggregatorAdapter.ID);
        aggregators.update("ground-sensors", json("{'enabled':true,'config':{" + airq(true) + "},"
                + "'disableSupersededSources':false}"));
        aggregators.update("ground-sensors", json("{'enabled':false,'config':{" + airq(true) + "}}"));

        assertThat(sources.view(MeshCoreBridgeAdapter.ID).enabled()).isTrue();
        assertThat(sources.view(AirqLocalAggregatorAdapter.ID).enabled()).isFalse();
    }

    @Test
    @DisplayName("test() reads the enabled sensors and sums their records")
    void testReportsASampleCount() {
        stubPurpleAir();
        aggregators.update("ground-sensors", json("{'enabled':false,'config':{" + airq(true) + "}}"));

        Map<String, Object> result = ground.test(aggregators.settings("ground-sensors"));

        assertThat(result).containsEntry("ok", true).containsEntry("reachable", true);
        assertThat((Integer) result.get("sampleCount")).isPositive();
    }

    @Test
    @DisplayName("test() with no sensor enabled says so; a push source with no data is named")
    void testUnconfiguredAndPush() {
        assertThat(ground.test(AggregatorSettings.defaults())).containsEntry("ok", false)
                .containsEntry("message", "No sensor is enabled.");

        AggregatorSettings bridge = new AggregatorSettings(true, "receiver", null,
                Map.of("mesh", Map.of("enabled", true, "bridge", true)), false, List.of());
        Map<String, Object> result = ground.test(bridge);
        if (sources.view(MeshCoreBridgeAdapter.ID).health().lastDataAt() == null) {
            assertThat(result).containsEntry("ok", false);
            assertThat(String.valueOf(result.get("message"))).contains(MeshCoreBridgeAdapter.ID);
        }
    }

    @Test
    @DisplayName("the endpoint is per sensor; sensors and members are checked; nothing is changed on an error")
    void validation() {
        assertThatThrownBy(() -> aggregators.update("ground-sensors", json("{'enabled':true,'endpoint':'"
                + purpleAir() + "','config':{" + airq(true) + "}}")))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e).matches(hasField("endpoint")));
        assertThatThrownBy(() -> aggregators.update("ground-sensors", json("{'enabled':true,'config':{}}")))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e).matches(hasField("config")));
        assertThatThrownBy(() -> aggregators.update("ground-sensors", json("{'enabled':true,'config':{"
                + "'lightning':{'enabled':true},"
                + "'pws':{'enabled':true,'station':'netatmo','endpoint':'http://192.168.1.70','height':2},"
                + "'seismic':{'enabled':true,'source':'" + UsgsEarthquakesAdapter.ID + "'},"
                + "'same':{'enabled':true,'source':'no-such-source'},"
                + "'radiosonde':{'enabled':true}}}")))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e)
                        .matches(hasField("config.lightning"))
                        .matches(hasField("config.pws.station"))
                        .matches(hasField("config.pws.height"))
                        .matches(hasField("config.seismic.source"))
                        .matches(hasField("config.same.source"))
                        .matches(hasField("config.radiosonde.endpoint")));
        assertThat(sources.view(AirqLocalAggregatorAdapter.ID).enabled()).as("nothing was changed").isFalse();
    }

    @Test
    @DisplayName("a source's own parameter error is reported at the sensor's member")
    void sourceErrorsAreRemapped() {
        assertThatThrownBy(() -> aggregators.update("ground-sensors", json("{'enabled':true,'config':{"
                + "'pws':{'enabled':true,'station':'ecowitt','endpoint':'http://192.168.1.70','lat':95,'lon':0}}}")))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e).matches(hasField(
                        "config.pws.lat")));
        assertThat(sources.view(EcowittAggregatorAdapter.ID).enabled()).isFalse();
    }

    @Test
    @DisplayName("only the mirrors of the enabled sensors are superseded; authorities never are")
    void supersededFollowsTheSensors() {
        assertThat(ground.supersededSourceIds()).contains(SondehubAdapter.ID, OpenAqAdapter.ID,
                SensorCommunityAdapter.ID, MeshtasticMqttAdapter.ID, MeshtasticMapAdapter.ID)
                .doesNotContain(UsgsEarthquakesAdapter.ID, "usgs-shakemap", "emsc-seismic", "nws-alerts");
        AggregatorSettings airqOnly = new AggregatorSettings(true, "receiver", null,
                Map.of("airq", Map.of("enabled", true)), true, List.of());
        AggregatorSettings bridgeOnly = new AggregatorSettings(true, "receiver", null,
                Map.of("mesh", Map.of("enabled", true, "bridge", true)), true, List.of());
        AggregatorSettings meshtastic = new AggregatorSettings(true, "receiver", null,
                Map.of("mesh", Map.of("enabled", true, "meshtastic", "mqtt://192.168.1.9:1883")), true, List.of());
        AggregatorSettings seismic = new AggregatorSettings(true, "receiver", null,
                Map.of("seismic", Map.of("enabled", true, "source", "rsudp")), true, List.of());
        assertThat(ground.supersededSourceIds(airqOnly)).containsExactly(OpenAqAdapter.ID,
                SensorCommunityAdapter.ID);
        assertThat(ground.supersededSourceIds(bridgeOnly)).isEmpty();
        assertThat(ground.supersededSourceIds(meshtastic)).containsExactly(MeshtasticMapAdapter.ID,
                MeshtasticMqttAdapter.ID);
        assertThat(ground.supersededSourceIds(seismic)).isEmpty();
    }

    @Test
    @DisplayName("every ground-sensor read registers")
    void readsAreRegistered() {
        assertThat(registry.ids()).contains(AirqLocalAggregatorAdapter.ID, DavisWllAggregatorAdapter.ID,
                EcowittAggregatorAdapter.ID);
    }
}
