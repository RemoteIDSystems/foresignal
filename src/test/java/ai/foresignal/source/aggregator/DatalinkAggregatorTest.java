package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.acars.LocalAcarsAdapter;
import ai.foresignal.source.adapters.acars.LocalAcarsAggregatorAdapter;
import ai.foresignal.source.adapters.radio.AprsIsAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAdapter;
import ai.foresignal.source.adapters.radio.LocalAprsAggregatorAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code datalink} domain (prompt R2-021): enabling it points {@code local-acars-aggregator} at acars_router and
 * {@code local-aprs-aggregator} at the igate's TNC, and turns on exactly the reads whose decoders are selected;
 * {@code test()} listens to them and reports a sample count; disabling turns both off. Loopback servers stand in for
 * acars_router (acarsdec JSON lines) and Direwolf (KISS frames).
 */
@IntegrationTest
class DatalinkAggregatorTest extends AggregatorItSupport {

    @Autowired DatalinkAggregator datalink;

    private LoopbackFeed acarsRouter;
    private LoopbackFeed direwolf;

    /** acars_router's ACARS server and Direwolf's KISS port, on loopback. */
    @BeforeEach
    void startDecoders() throws IOException {
        acarsRouter = new LoopbackFeed((AdapterFixtures.text("local-rf", "acarsdec.json").strip() + "\n")
                .getBytes(StandardCharsets.UTF_8));
        direwolf = new LoopbackFeed(kissFixture());
    }

    @AfterEach
    void stopDecoders() throws IOException {
        acarsRouter.close();
        direwolf.close();
    }

    /** The bytes of {@code fixtures/local-aprs/direwolf-kiss.hex} (as {@code LocalAprsAdapterTest} reads them). */
    static byte[] kissFixture() {
        StringBuilder hex = new StringBuilder();
        for (String line : AdapterFixtures.text("local-aprs", "direwolf-kiss.hex").split("\n")) {
            if (!line.startsWith("#")) {
                hex.append(line.replace(" ", "").strip());
            }
        }
        return HexFormat.of().parseHex(hex);
    }

    private String both(boolean enabled) {
        return "{'enabled':" + enabled + ",'mode':'receiver','endpoint':'tcp://127.0.0.1','config':{'acars':"
                + acarsRouter.port() + ",'aprs':" + direwolf.port() + ",'stationId':'FS-HOME',"
                + "'stationCallsign':'N0CALL-10'},'disableSupersededSources':false}";
    }

    @Test
    @DisplayName("enabling routes ACARS and APRS reception to the local decoders; disabling turns both reads off")
    void enableRoutesReceptionToTheLocalDecoders() {
        turnedOn.add(LocalAcarsAggregatorAdapter.ID);
        turnedOn.add(LocalAprsAggregatorAdapter.ID);
        aggregators.update("datalink", json(both(true)));

        SourceView acars = sources.view(LocalAcarsAggregatorAdapter.ID);
        assertThat(acars.enabled()).isTrue();
        assertThat(String.valueOf(acars.params().get(LocalAcarsAdapter.ACARS_ENDPOINT)))
                .isEqualTo("tcp://127.0.0.1:" + acarsRouter.port());
        assertThat(acars.params()).doesNotContainKey(LocalAcarsAdapter.VDLM2_ENDPOINT)
                .containsEntry(LocalAcarsAdapter.STATION_ID, "FS-HOME");
        SourceView aprs = sources.view(LocalAprsAggregatorAdapter.ID);
        assertThat(aprs.enabled()).isTrue();
        assertThat(String.valueOf(aprs.params().get(LocalAprsAdapter.ENDPOINT)))
                .isEqualTo("tcp://127.0.0.1:" + direwolf.port());
        assertThat(aprs.params()).containsEntry("stationCallsign", "N0CALL-10");

        aggregators.update("datalink", json(both(false)));

        assertThat(sources.view(LocalAcarsAggregatorAdapter.ID).enabled()).isFalse();
        assertThat(sources.view(LocalAprsAggregatorAdapter.ID).enabled()).isFalse();
    }

    @Test
    @DisplayName("only the selected decoders' reads are turned on, and the status follows the read in use")
    void onlySelectedReads() {
        turnedOn.add(LocalAprsAggregatorAdapter.ID);
        aggregators.update("datalink", json("{'enabled':true,'endpoint':'tcp://127.0.0.1','config':{'aprs':"
                + direwolf.port() + "},'disableSupersededSources':false}"));

        assertThat(sources.view(LocalAprsAggregatorAdapter.ID).enabled()).isTrue();
        assertThat(sources.view(LocalAcarsAggregatorAdapter.ID).enabled()).isFalse();
        assertThat(aggregators.statusSource(datalink)).isEqualTo(LocalAprsAggregatorAdapter.ID);
    }

    @Test
    @DisplayName("a decoder may be a whole tcp://host:port instead of a port on the endpoint host")
    void decoderUrl() {
        assertThat(DatalinkAggregator.decoderUrl("router", 15550)).isEqualTo("tcp://router:15550");
        assertThat(DatalinkAggregator.decoderUrl("router", "tcp://10.0.0.7:15555")).isEqualTo("tcp://10.0.0.7:15555");
        assertThat(DatalinkAggregator.decoderUrl("router", 0)).isNull();
        assertThat(DatalinkAggregator.decoderUrl("router", 70000)).isNull();
        assertThat(DatalinkAggregator.decoderUrl("router", "http://router:80")).isNull();
        assertThat(DatalinkAggregator.decoderUrl("router", "tcp://router")).isNull();
    }

    @Test
    @DisplayName("test() listens to every selected read and reports the messages decoded")
    void testReportsASampleCount() {
        aggregators.update("datalink", json(both(false)));

        Map<String, Object> result = datalink.test(aggregators.settings("datalink"));

        assertThat(result).containsEntry("ok", true).containsEntry("reachable", true);
        assertThat((Integer) result.get("sampleCount")).isPositive();
    }

    @Test
    @DisplayName("test() with no endpoint, or no decoder, says so")
    void testUnconfigured() {
        assertThat(datalink.test(AggregatorSettings.defaults())).containsEntry("ok", false)
                .containsEntry("message", "No endpoint is configured.");
        aggregators.update("datalink", json("{'enabled':false,'endpoint':'tcp://127.0.0.1'}"));
        assertThat(datalink.test(aggregators.settings("datalink"))).containsEntry("ok", false)
                .containsEntry("message", "No decoder is selected.");
    }

    @Test
    @DisplayName("the endpoint is tcp://host with no port; enabling needs a decoder; members are checked")
    void validation() {
        assertThat(DatalinkAggregator.host("tcp://acars-router")).isEqualTo("acars-router");
        assertThat(DatalinkAggregator.host("tcp://acars-router:15550")).isNull();
        assertThat(DatalinkAggregator.host("http://acars-router")).isNull();
        assertThat(DatalinkAggregator.host("tcp://u@acars-router")).isNull();

        assertThatThrownBy(() -> aggregators.update("datalink", json("{'enabled':true,'endpoint':'tcp://127.0.0.1'}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anyMatch(f -> f.field().equals("config")));
        assertThatThrownBy(() -> aggregators.update("datalink", json("{'enabled':true,'endpoint':'tcp://127.0.0.1',"
                + "'config':{'acars':'nope','sdr':'rtl0'}}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anyMatch(f -> f.field().equals("config.acars"))
                        .anyMatch(f -> f.field().equals("config.sdr")));
        assertThat(sources.view(LocalAcarsAggregatorAdapter.ID).enabled()).as("nothing was changed").isFalse();
    }

    @Test
    @DisplayName("the reads keep their slots' egress: ACARS private and fixed LOCAL_ONLY, APRS LOCAL_ONLY overridable")
    void readsKeepTheirEgress() {
        var acars = registry.descriptor(LocalAcarsAggregatorAdapter.ID);
        assertThat(acars.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(acars.egressOverridable()).isFalse();
        assertThat(acars.privateSource()).isTrue();
        assertThat(LocalAcarsAggregatorAdapter.ID).startsWith(LocalAcarsAdapter.ID_PREFIX);
        var aprs = registry.descriptor(LocalAprsAggregatorAdapter.ID);
        assertThat(aprs.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(aprs.egressOverridable()).isTrue();
        assertThat(LocalAprsAggregatorAdapter.ID).startsWith(LocalAprsAdapter.ID_PREFIX);
    }

    @Test
    @DisplayName("it supersedes aprs-is only")
    void supersededList() {
        assertThat(datalink.supersededSourceIds()).containsExactly(AprsIsAdapter.ID);
        assertThat(datalink.implemented()).isTrue();
    }
}
