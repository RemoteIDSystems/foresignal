package ai.foresignal.source.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.adapters.ais.LocalAisAdapter;
import ai.foresignal.source.adapters.ais.LocalAisAggregatorAdapter;
import ai.foresignal.source.adapters.aisstream.AisStreamAdapter;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.test.IntegrationTest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@code ais} domain (prompt R2-020): enabling it points {@code local-ais-aggregator} at the local NMEA feed and
 * turns it on; {@code test()} listens to the feed and reports a sample count; disabling turns the read off again and
 * leaves every other source as it was. A loopback server stands in for AIS-catcher's {@code -S} TCP server.
 */
@IntegrationTest
class AisAggregatorTest extends AggregatorItSupport {

    /** One AIS position report (class A), the line {@code LocalAisAdapterTest} decodes. */
    static final String LINE = "!AIVDM,1,1,,A,13m=TNPtAsPh4;0RAV`8h6g22000,0*6B\r\n";

    @Autowired AisAggregator ais;

    private ServerSocket server;
    private final List<Socket> clients = new CopyOnWriteArrayList<>();

    /** A loopback NMEA server that writes {@link #LINE} to every client every 100 ms. */
    @BeforeEach
    void startServer() throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    Socket client = server.accept();
                    clients.add(client);
                    Thread.ofVirtual().start(() -> feed(client));
                } catch (IOException e) {
                    return;
                }
            }
        });
    }

    private static void feed(Socket client) {
        try (client) {
            OutputStream out = client.getOutputStream();
            while (!client.isClosed()) {
                out.write(LINE.getBytes(StandardCharsets.US_ASCII));
                out.flush();
                Thread.sleep(100);
            }
        } catch (IOException e) {
            // The probe closed its connection.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void stopServer() throws IOException {
        server.close();
        for (Socket client : clients) {
            client.close();
        }
    }

    private String tcp() {
        return "tcp://127.0.0.1:" + server.getLocalPort();
    }

    @Test
    @DisplayName("enabling points the local read at the feed and turns it on; disabling turns it off and keeps it")
    void enableRoutesReceptionToTheLocalFeed() {
        turnedOn.add(LocalAisAggregatorAdapter.ID);
        aggregators.update("ais", json("{'enabled':true,'mode':'receiver','endpoint':'" + tcp() + "',"
                + "'config':{'stationName':'harbor'},'disableSupersededSources':false}"));

        SourceView read = sources.view(LocalAisAggregatorAdapter.ID);
        assertThat(read.enabled()).isTrue();
        assertThat(String.valueOf(read.params().get(LocalAisAdapter.ENDPOINT))).isEqualTo(tcp());
        assertThat(read.params()).containsEntry(LocalAisAdapter.STATION_NAME, "harbor");

        aggregators.update("ais", json("{'enabled':false,'endpoint':'" + tcp() + "'}"));

        assertThat(sources.view(LocalAisAggregatorAdapter.ID).enabled()).isFalse();
        assertThat(String.valueOf(sources.view(LocalAisAggregatorAdapter.ID).params().get(LocalAisAdapter.ENDPOINT)))
                .as("the endpoint is kept for a later enable").isEqualTo(tcp());
    }

    @Test
    @DisplayName("test() listens to the feed and reports the records decoded")
    void testReportsASampleCount() {
        aggregators.update("ais", json("{'enabled':false,'endpoint':'" + tcp() + "'}"));

        Map<String, Object> result = ais.test(aggregators.settings("ais"));

        assertThat(result).containsEntry("ok", true).containsEntry("reachable", true);
        assertThat((Integer) result.get("sampleCount")).isPositive();
    }

    @Test
    @DisplayName("test() of a closed port is not reachable; with no endpoint it says so")
    void testUnreachable() throws IOException {
        int port;
        try (ServerSocket closed = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = closed.getLocalPort();
        }
        aggregators.update("ais", json("{'enabled':false,'endpoint':'tcp://127.0.0.1:" + port + "'}"));

        assertThat(ais.test(aggregators.settings("ais"))).containsEntry("ok", false).containsEntry("reachable", false);
        assertThat(ais.test(AggregatorSettings.defaults())).containsEntry("ok", false)
                .containsEntry("message", "No endpoint is configured.");
    }

    @Test
    @DisplayName("the endpoint must be tcp:// or udp:// with a port, and udp:// needs config.allowSenders")
    void endpointRules() {
        assertThat(AisAggregator.scheme("tcp://ais-catcher:5012")).isEqualTo("tcp");
        assertThat(AisAggregator.scheme("udp://0.0.0.0:10110")).isEqualTo("udp");
        assertThat(AisAggregator.scheme("tcp://ais-catcher")).isNull();
        assertThat(AisAggregator.scheme("http://ais-catcher:8100")).isNull();
        assertThat(AisAggregator.scheme("not a url")).isNull();

        assertThatThrownBy(() -> aggregators.update("ais", json("{'enabled':true,'endpoint':'udp://0.0.0.0:10110'}")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors())
                        .anyMatch(f -> f.field().equals("config.allowSenders")));
        assertThatThrownBy(() -> aggregators.update("ais", json("{'enabled':true,'endpoint':'" + tcp() + "',"
                + "'config':{'device':'rtlsdr'}}")))
                .isInstanceOf(ValidationException.class);
        assertThat(sources.view(LocalAisAggregatorAdapter.ID).enabled()).as("nothing was changed").isFalse();
    }

    @Test
    @DisplayName("a udp:// endpoint with allowSenders is stored on the read")
    void udpEndpoint() {
        aggregators.update("ais", json("{'enabled':false,'endpoint':'udp://127.0.0.1:10110',"
                + "'config':{'allowSenders':['127.0.0.1']}}"));

        SourceView read = sources.view(LocalAisAggregatorAdapter.ID);
        assertThat(String.valueOf(read.params().get(LocalAisAdapter.ENDPOINT))).isEqualTo("udp://127.0.0.1:10110");
        assertThat(read.params().get(LocalAisAggregatorAdapter.ALLOW_SENDERS)).isEqualTo(List.of("127.0.0.1"));
    }

    @Test
    @DisplayName("the read is LOCAL_ONLY and overridable, and carries the local-ais- prefix")
    void readIsLocalOnly() {
        var descriptor = registry.descriptor(LocalAisAggregatorAdapter.ID);
        assertThat(descriptor.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(descriptor.egressOverridable()).isTrue();
        assertThat(LocalAisAggregatorAdapter.ID).startsWith(LocalAisAdapter.ID_PREFIX);
    }

    @Test
    @DisplayName("it supersedes aisstream only")
    void supersededList() {
        assertThat(ais.supersededSourceIds()).containsExactly(AisStreamAdapter.ID);
        assertThat(ais.implemented()).isTrue();
    }
}
