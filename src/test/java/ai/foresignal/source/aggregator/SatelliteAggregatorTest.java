package ai.foresignal.source.aggregator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.goes.GoesWestAbiAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpAggregatorAdapter;
import ai.foresignal.source.adapters.satdump.SatDumpDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code satellite} domain (prompt R2-022): enabling it points the {@code satdump-aggregator} station at the local
 * SatDump and turns it on; {@code test()} runs the station and reports a sample count; disabling turns it off again.
 * The stub SatDump answers {@code /api} with the recorded autotrack state.
 */
@IntegrationTest
class SatelliteAggregatorTest extends AggregatorItSupport {

    @Autowired SatelliteAggregator satellite;

    /** The stub SatDump's web server root. */
    private static String satdump() {
        return url("/");
    }

    private static void stubApi() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api")).willReturn(aResponse().withHeader("Content-Type",
                "application/json").withBody(AdapterFixtures.text("satdump", "2.0.0-alpha/api/autotrack.json"))));
    }

    @Test
    @DisplayName("enabling routes imagery ingestion to the local SatDump station; disabling turns it off")
    void enableRoutesToTheLocalStation() {
        turnedOn.add(SatDumpAggregatorAdapter.ID);
        aggregators.update("satellite", json("{'enabled':true,'mode':'receiver','endpoint':'" + satdump() + "',"
                + "'config':{'location':'51.47,-0.45,30','predictNorad':[57166],'goes':'east'},"
                + "'disableSupersededSources':false}"));

        SourceView station = sources.view(SatDumpAggregatorAdapter.ID);
        assertThat(station.enabled()).isTrue();
        assertThat(String.valueOf(station.params().get(SatDumpDescriptor.API_URL))).isEqualTo(satdump());
        assertThat(station.params()).containsEntry(SatDumpDescriptor.LOCATION, "51.47,-0.45,30")
                .containsEntry(SatDumpDescriptor.PREDICT_NORAD, List.of("57166"));

        aggregators.update("satellite", json("{'enabled':false,'endpoint':'" + satdump() + "',"
                + "'config':{'location':'51.47,-0.45,30'}}"));

        assertThat(sources.view(SatDumpAggregatorAdapter.ID).enabled()).isFalse();
    }

    @Test
    @DisplayName("test() runs the station against SatDump and reports the records it produced")
    void testReportsASampleCount() {
        stubApi();
        aggregators.update("satellite", json("{'enabled':false,'endpoint':'" + satdump() + "',"
                + "'config':{'location':'51.47,-0.45'}}"));

        Map<String, Object> result = satellite.test(aggregators.settings("satellite"));

        assertThat(result).containsEntry("ok", true).containsEntry("reachable", true);
        assertThat((Integer) result.get("sampleCount")).isPositive();
    }

    @Test
    @DisplayName("test() with no endpoint says so")
    void testUnconfigured() {
        assertThat(satellite.test(AggregatorSettings.defaults())).containsEntry("ok", false)
                .containsEntry("message", "No endpoint is configured.");
    }

    @Test
    @DisplayName("an endpoint needs a location; the endpoint is an http(s) URL; members are checked")
    void validation() {
        assertThat(SatelliteAggregator.endpointProblem("http://satdump:8081")).isNull();
        assertThat(SatelliteAggregator.endpointProblem("https://10.0.0.9/satdump")).isNull();
        assertThat(SatelliteAggregator.endpointProblem("tcp://satdump:8081")).isNotNull();
        assertThat(SatelliteAggregator.endpointProblem("http://u:p@satdump:8081")).isNotNull();

        assertThatThrownBy(() -> aggregators.update("satellite", json("{'enabled':true,'endpoint':'" + satdump()
                + "'}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anyMatch(f -> f.field().equals("config.location")));
        assertThatThrownBy(() -> aggregators.update("satellite", json("{'enabled':true,'endpoint':'" + satdump()
                + "','config':{'location':'51.47,-0.45','goes':'central','dish':'1.2m'}}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anyMatch(f -> f.field().equals("config.goes"))
                        .anyMatch(f -> f.field().equals("config.dish")));
        assertThat(sources.view(SatDumpAggregatorAdapter.ID).enabled()).as("nothing was changed").isFalse();
    }

    @Test
    @DisplayName("it supersedes only the ABI imagery of the GOES the dish receives")
    void supersededFollowsTheDish() {
        assertThat(satellite.supersededSourceIds()).containsExactly(GoesAbiAdapter.ID, GoesWestAbiAdapter.ID);
        AggregatorSettings east = new AggregatorSettings(true, "receiver", satdump(), Map.of("goes", "east"), true,
                List.of());
        AggregatorSettings west = new AggregatorSettings(true, "receiver", satdump(), Map.of("goes", "west"), true,
                List.of());
        AggregatorSettings polar = new AggregatorSettings(true, "receiver", satdump(), Map.of(), true, List.of());
        assertThat(satellite.supersededSourceIds(east)).containsExactly(GoesAbiAdapter.ID);
        assertThat(satellite.supersededSourceIds(west)).containsExactly(GoesWestAbiAdapter.ID);
        assertThat(satellite.supersededSourceIds(polar)).as("a polar-only station replaces no NOAA pull").isEmpty();
        assertThat(satellite.supersededSourceIds()).doesNotContain("goes-glm", "goes-fdc");
    }

    @Test
    @DisplayName("the station registers without any configured SatDump station")
    void stationIsAlwaysRegistered() {
        assertThat(registry.ids()).contains(SatDumpAggregatorAdapter.ID);
    }
}
