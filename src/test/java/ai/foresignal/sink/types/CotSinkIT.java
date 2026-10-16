package ai.foresignal.sink.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkFilter;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@code cot} SERVE over TCP (prompt 110): a TAK client receives well-formed CoT events, the current picture on
 * connect, speeds in m/s, a removal with {@code stale = now}; records without a point are not sent; the PUSH
 * multicast group must be LAN-scoped.
 */
class CotSinkIT {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");
    static final GeometryFactory GEOMETRY = new GeometryFactory();

    static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    static SinkRecord aircraft(String hex, boolean point) {
        return new SinkRecord("adsblol-mil", hex, "aircraft", 1, 1, T, null, false,
                point ? GEOMETRY.createPoint(new Coordinate(-79.38, 43.65)) : null,
                Map.of("flight", "TEST01\u0007", "gs", 100.0, "track", 90.0), null, "adsb.lol (ODbL)");
    }

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    /** Reads one {@code <event>} document (the XML declaration line, then the event line). */
    static String readEvent(BufferedReader reader) throws Exception {
        String declaration = reader.readLine();
        String event = reader.readLine();
        return declaration + "\n" + event;
    }

    @Test
    @DisplayName("a TCP client gets the snapshot, then well-formed events in m/s; no-point records are skipped")
    void serveTcp() throws Exception {
        int port = freePort();
        CotSinkType type = new CotSinkType(new ServeGuard(), "instance-0001");
        Map<String, Object> config = Map.of("mode", "SERVE", "bind", "127.0.0.1", "port", port);
        type.validate(config, Map.of());
        SinkInstance sink = type.open(new SinkConfig("cot", CotSinkType.ID, "CoT", SinkMode.SERVE, config, Map.of(),
                SinkFilter.ALL));
        try (sink) {
            sink.upsert(aircraft("abc123", true));
            try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
                socket.setSoTimeout(5_000);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                Document snapshot = parse(readEvent(reader));
                Element event = snapshot.getDocumentElement();
                assertThat(event.getAttribute("uid")).isEqualTo("FORESIGNAL.instance.adsblol-mil.abc123");
                assertThat(event.getAttribute("type")).isEqualTo("a-u-A");
                Element track = (Element) event.getElementsByTagName("track").item(0);
                assertThat(Double.parseDouble(track.getAttribute("speed"))).isCloseTo(51.4444,
                        org.assertj.core.data.Offset.offset(0.001));
                Element contact = (Element) event.getElementsByTagName("contact").item(0);
                assertThat(contact.getAttribute("callsign")).as("control characters stripped").isEqualTo("TEST01");
                await().atMost(Duration.ofSeconds(5)).until(() -> sink.clients() == 1);

                sink.upsert(aircraft("nopos1", false));
                sink.upsert(aircraft("def456", true));
                Element next = parse(readEvent(reader)).getDocumentElement();
                assertThat(next.getAttribute("uid")).as("the no-point record was skipped").endsWith(".def456");

                sink.remove("adsblol-mil", "def456");
                Element removed = parse(readEvent(reader)).getDocumentElement();
                assertThat(removed.getAttribute("stale")).isEqualTo(removed.getAttribute("time"));
            }
        }
    }

    @Test
    @DisplayName("the PUSH multicast group must be LAN-scoped multicast")
    void multicastGroup() {
        assertThat(CotSinkType.multicastGroup(null).getHostAddress()).isEqualTo(CotSinkType.DEFAULT_GROUP);
        assertThat(CotSinkType.multicastGroup("239.255.0.1")).isNotNull();
        assertThatThrownBy(() -> CotSinkType.multicastGroup("230.1.2.3")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CotSinkType.multicastGroup("192.168.1.5")).isInstanceOf(ValidationException.class);
        CotSinkType type = new CotSinkType(new ServeGuard(), "x");
        assertThatThrownBy(() -> type.validate(Map.of("group", "8.8.8.8"), Map.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("speed and course come from gs/track (aircraft) or sog/cog (vessels), knots to m/s")
    void units() {
        SinkRecord vessel = new SinkRecord("ais", "123456789", "vessel", 1, 1, T, null, false, null,
                Map.of("sog", 10.0, "cog", 45.0, "name", "BALTIC"), null, null);
        assertThat(CotSinkType.speedMs(vessel)).isCloseTo(5.14444, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(CotSinkType.course(vessel)).isEqualTo(45.0);
        assertThat(CotSinkType.callsign(vessel)).isEqualTo("BALTIC");
    }
}
