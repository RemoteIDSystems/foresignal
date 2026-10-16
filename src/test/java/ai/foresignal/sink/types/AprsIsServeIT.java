package ai.foresignal.sink.types;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkFilter;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * {@code aprs-is-serve} (prompt 110): banner and login handshake, the {@code r/} filter, client-sent packets
 * discarded (never re-broadcast), raw packets with embedded line breaks flattened, and the 20 s keepalive declared.
 */
class AprsIsServeIT {

    static final GeometryFactory GEOMETRY = new GeometryFactory();

    static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    static SinkRecord packet(String id, double lat, double lon, String raw) {
        return new SinkRecord("aprs-is", id, "aprs-position", 1, 1, Instant.now(), null, false,
                GEOMETRY.createPoint(new Coordinate(lon, lat)), Map.of("raw", raw), null, null);
    }

    @Test
    @DisplayName("handshake, r/ filter, uploads discarded")
    void session() throws Exception {
        int port = freePort();
        Map<String, Object> config = Map.of("bind", "127.0.0.1", "port", port);
        SinkInstance sink = new AprsIsServeSinkType(new ServeGuard()).open(new SinkConfig("aprs",
                AprsIsServeSinkType.ID, "APRS", SinkMode.SERVE, config, Map.of(), SinkFilter.ALL));
        try (sink; Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
            socket.setSoTimeout(5_000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
            OutputStream out = socket.getOutputStream();
            assertThat(in.readLine()).startsWith("# foresignal aprs-is-serve");
            out.write("user N0CALL pass -1 vers test 1.0 filter r/43.65/-79.38/50\r\n"
                    .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            assertThat(in.readLine()).isEqualTo("# logresp N0CALL unverified, server " + AprsIsServeSinkType.SERVER);

            out.write("N0CALL>APRS:!4339.00N/07923.00W-uploaded\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            sink.upsert(packet("far", 51.5, -0.12, "G0ABC>APRS:!5130.00N/00007.00W-far"));
            sink.upsert(packet("near", 43.7, -79.4, "VE3XYZ>APRS:!4342.00N/07924.00W-near\r\ninjected"));
            String line = in.readLine();
            while (line != null && line.startsWith("#")) {
                line = in.readLine();
            }
            assertThat(line).as("the far packet is filtered, the upload never echoed, line breaks flattened")
                    .isEqualTo("VE3XYZ>APRS:!4342.00N/07924.00W-near injected");
        }
    }

    @Test
    @DisplayName("the keepalive is a comment line every 20 s")
    void keepaliveDeclared() {
        assertThat(AprsIsServeSinkType.KEEPALIVE).startsWith("#");
        assertThat(AprsIsServeSinkType.KEEPALIVE_EVERY).isEqualTo(java.time.Duration.ofSeconds(20));
    }
}
