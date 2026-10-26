package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.local.LocalAdsbAdapter;
import ai.foresignal.source.adapters.local.LocalAdsbAggregatorAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.test.IntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code adsb} domain (prompt R2-019): enabling it points {@code local-adsb-aggregator} at the local endpoint and
 * turns it on; {@code test()} reports the aircraft count; disabling turns the read off again and leaves the public
 * sources as they were; the read is {@code LOCAL_ONLY} like every local receiver (EGR-6) and is a {@code local-adsb-}
 * source, so the derived aircraft layers read it.
 */
@IntegrationTest
class AdsbAggregatorTest extends AggregatorItSupport {

    @Autowired AdsbAggregator adsb;

    @Test
    @DisplayName("enabling points the local read at the endpoint and turns it on; disabling turns it off")
    void enablePointsTheReadAtTheEndpoint() {
        turnedOn.add(LocalAdsbAggregatorAdapter.ID);
        aggregators.update("adsb", json("{'enabled':true,'mode':'receiver','endpoint':'" + endpoint() + "',"
                + "'config':{'band':'978'},'disableSupersededSources':false}"));

        SourceView read = sources.view(LocalAdsbAggregatorAdapter.ID);
        assertThat(read.enabled()).isTrue();
        assertThat(read.params()).containsEntry(LocalAdsbAdapter.URL_PARAM, endpoint())
                .containsEntry(LocalAdsbAdapter.BAND_PARAM, "978");

        aggregators.update("adsb", json("{'enabled':false,'endpoint':'" + endpoint() + "'}"));

        assertThat(sources.view(LocalAdsbAggregatorAdapter.ID).enabled()).isFalse();
        assertThat(sources.view(LocalAdsbAggregatorAdapter.ID).params())
                .as("the endpoint is kept for a later enable").containsEntry(LocalAdsbAdapter.URL_PARAM, endpoint());
    }

    @Test
    @DisplayName("test() reads the endpoint once and reports the aircraft in it")
    void testReportsASampleCount() {
        stubReadsb();
        aggregators.update("adsb", json("{'enabled':false,'endpoint':'" + endpoint() + "'}"));

        Map<String, Object> result = adsb.test(aggregators.settings("adsb"));

        assertThat(result).containsEntry("ok", true).containsEntry("reachable", true).containsEntry("sampleCount", 2);
    }

    @Test
    @DisplayName("an answer that is not readsb JSON is reachable but not ok")
    void testRejectsOtherAnswers() {
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get("/data/aircraft.json")
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200)
                        .withBody("<html>tar1090</html>")));
        aggregators.update("adsb", json("{'enabled':false,'endpoint':'" + endpoint() + "'}"));

        assertThat(adsb.test(aggregators.settings("adsb"))).containsEntry("ok", false).containsEntry("reachable", true);
    }

    @Test
    @DisplayName("the endpoint must be an http(s) aircraft.json URL with no credentials, query or fragment")
    void endpointRules() {
        assertThat(AdsbAggregator.endpointProblem("http://readsb/data/aircraft.json")).isNull();
        assertThat(AdsbAggregator.endpointProblem("https://10.0.0.5:8443/tar1090/data/aircraft.json")).isNull();
        assertThat(AdsbAggregator.endpointProblem("http://readsb/")).isNotNull();
        assertThat(AdsbAggregator.endpointProblem("ftp://readsb/data/aircraft.json")).isNotNull();
        assertThat(AdsbAggregator.endpointProblem("http://u:p@readsb/data/aircraft.json")).isNotNull();
        assertThat(AdsbAggregator.endpointProblem("http://readsb/data/aircraft.json?x=1")).isNotNull();
        assertThat(AdsbAggregator.endpointProblem("not a url")).isNotNull();
    }

    @Test
    @DisplayName("the read is LOCAL_ONLY and overridable, and carries the local-adsb- prefix")
    void readIsLocalOnly() {
        var descriptor = registry.descriptor(LocalAdsbAggregatorAdapter.ID);
        assertThat(descriptor.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(descriptor.egressOverridable()).isTrue();
        assertThat(LocalAdsbAggregatorAdapter.ID).startsWith(LocalAdsbAdapter.ID_PREFIX);
    }

    @Test
    @DisplayName("it supersedes every registered public mirror and gpsjam-daily, and nothing local")
    void supersededList() {
        assertThat(adsb.supersededSourceIds()).contains("adsblol-world", "adsblol-mil", "adsbfi-mil",
                "airplaneslive-mil", "gpsjam-daily");
        assertThat(adsb.supersededSourceIds()).noneMatch(id -> id.startsWith("local-"));
        assertThat(adsb.supersededSourceIds()).allMatch(registry.ids()::contains);
    }
}
