package ai.foresignal.collect.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The TCP half of the socket transport (prompt 069) against a loopback server: LF and CRLF line framing, an
 * oversize line dropped and counted, the write on connect, the silence watchdog closing the connection so
 * the supervisor's ladder reconnects (a second connect costs a second unit), one budget unit per connect,
 * byte metering, and refusal of a non-private destination before any unit is spent.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TcpTransportIT {

    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");

    private ServerSocket server;
    private MutableClock clock;
    private TestStreamContext ctx;
    private SimpleMeterRegistry meters;
    private DefaultSocketTransport transport;
    private final BlockingQueue<Socket> accepted = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() throws IOException {
        server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    accepted.add(server.accept());
                } catch (IOException e) {
                    return;
                }
            }
        });
        clock = new MutableClock(NOW);
        ctx = new TestStreamContext("local-sbs-1", clock);
        meters = new SimpleMeterRegistry();
        transport = new DefaultSocketTransport(ctx, new SocketMetrics(meters, "local-sbs-1"), null, 1024, 64);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    private LanEndpoint endpoint() {
        return new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", server.getLocalPort(), InetAddress.getLoopbackAddress());
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the transport connected").isNotNull();
        return socket;
    }

    private double counter(String name, String... tags) {
        var search = meters.find(name).tag("source", "local-sbs-1");
        for (int i = 0; i + 1 < tags.length; i += 2) {
            search = search.tag(tags[i], tags[i + 1]);
        }
        var c = search.counter();
        return c == null ? 0.0 : c.count();
    }

    @Test
    @DisplayName("LF and CRLF lines arrive without terminators; an oversize line is dropped whole and counted")
    void lineFraming() throws Exception {
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        List<Instant> stamps = new CopyOnWriteArrayList<>();
        StreamHandle handle = transport.tcp(endpoint(), SocketOptions.defaults(), Framing.LINES, (frame, at) -> {
            lines.add(new String(frame, StandardCharsets.US_ASCII));
            stamps.add(at);
        });
        try (Socket peer = peer()) {
            OutputStream out = peer.getOutputStream();
            String oversize = "X".repeat(200);
            out.write(("first\nsecond\r\n" + oversize + "\nthi").getBytes(StandardCharsets.US_ASCII));
            out.flush();
            out.write("rd\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            assertThat(lines.poll(5, TimeUnit.SECONDS)).isEqualTo("first");
            assertThat(lines.poll(5, TimeUnit.SECONDS)).isEqualTo("second");
            assertThat(lines.poll(5, TimeUnit.SECONDS)).isEqualTo("third");
            assertThat(lines.poll(200, TimeUnit.MILLISECONDS)).isNull();
            assertThat(stamps).containsOnly(NOW);
            assertThat(counter(SocketMetrics.FRAMES_DROPPED, "reason", SocketMetrics.REASON_OVERSIZE)).isEqualTo(1.0);
            assertThat(counter(SocketMetrics.FRAMES)).isEqualTo(3.0);
            assertThat(ctx.bytes.get()).isEqualTo(6 + 8 + 201 + 3 + 4);
        } finally {
            handle.close();
        }
        assertThat(handle.isRunning()).isFalse();
    }

    @Test
    @DisplayName("RAW framing delivers chunks as read")
    void rawFraming() throws Exception {
        BlockingQueue<byte[]> chunks = new LinkedBlockingQueue<>();
        StreamHandle handle = transport.tcp(endpoint(), SocketOptions.defaults(), Framing.RAW, (frame, at) -> chunks.add(frame));
        try (Socket peer = peer()) {
            peer.getOutputStream().write(new byte[] {(byte) 0xC0, 0x00, 0x01, (byte) 0xC0});
            peer.getOutputStream().flush();
            byte[] chunk = chunks.poll(5, TimeUnit.SECONDS);
            assertThat(chunk).isNotNull();
            assertThat(chunk[0]).isEqualTo((byte) 0xC0);
        } finally {
            handle.close();
        }
    }

    @Test
    @DisplayName("the onConnect bytes are written once when the connection opens")
    void onConnectWrite() throws Exception {
        byte[] watch = "?WATCH={\"enable\":true,\"json\":true};\n".getBytes(StandardCharsets.US_ASCII);
        StreamHandle handle = transport.tcp(endpoint(), SocketOptions.defaults().withOnConnect(watch), Framing.LINES,
                (frame, at) -> {});
        try (Socket peer = peer()) {
            InputStream in = peer.getInputStream();
            byte[] read = in.readNBytes(watch.length);
            assertThat(read).isEqualTo(watch);
        } finally {
            handle.close();
        }
    }

    @Test
    @DisplayName("a silent connection is closed by the watchdog; the reconnect the ladder makes costs another unit")
    void silenceWatchdog() throws Exception {
        CompletableFuture<Throwable> ended = new CompletableFuture<>();
        SocketOptions quick = SocketOptions.defaults().withSilenceTimeout(Duration.ofMillis(300));
        StreamHandle first = transport.tcp(endpoint(), quick, Framing.LINES, (frame, at) -> {});
        first.onClose(ended::complete);
        Socket peer = peer();

        Throwable cause = ended.get(5, TimeUnit.SECONDS);
        assertThat(cause).isInstanceOf(DefaultSocketTransport.SilenceTimeoutException.class);
        assertThat(first.isRunning()).isFalse();
        assertThat(ctx.charged.get()).isEqualTo(1);

        // What the streaming supervisor does after its ladder delay: start again, paying one more unit.
        clock.advance(Duration.ofSeconds(5));
        StreamHandle second = transport.tcp(endpoint(), quick, Framing.LINES, (frame, at) -> {});
        try (Socket again = peer()) {
            assertThat(ctx.charged.get()).isEqualTo(2);
            assertThat(counter(SocketMetrics.CONNECTS, "result", "ok")).isEqualTo(2.0);
            assertThat(again.isConnected()).isTrue();
        } finally {
            second.close();
            peer.close();
        }
    }

    @Test
    @DisplayName("a peer that closes ends the stream with no error; a handler that throws ends it with that error")
    void endings() throws Exception {
        CompletableFuture<Throwable> clean = new CompletableFuture<>();
        StreamHandle handle = transport.tcp(endpoint(), SocketOptions.defaults(), Framing.LINES, (frame, at) -> {});
        handle.onClose(clean::complete);
        peer().close();
        assertThat(clean.get(5, TimeUnit.SECONDS)).isNull();

        CompletableFuture<Throwable> failed = new CompletableFuture<>();
        IllegalStateException boom = new IllegalStateException("bad frame");
        StreamHandle second = transport.tcp(endpoint(), SocketOptions.defaults(), Framing.LINES, (frame, at) -> {
            throw boom;
        });
        second.onClose(failed::complete);
        try (Socket peer = peer()) {
            peer.getOutputStream().write("line\n".getBytes(StandardCharsets.US_ASCII));
            peer.getOutputStream().flush();
            assertThat(failed.get(5, TimeUnit.SECONDS)).isSameAs(boom);
        }
    }

    @Test
    @DisplayName("one budget unit per connect attempt, failed ones included; a spent budget connects nothing")
    void budgetPerConnect() throws Exception {
        int closedPort;
        try (ServerSocket gone = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = gone.getLocalPort();
        }
        LanEndpoint refused = new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", closedPort,
                InetAddress.getLoopbackAddress());
        assertThatThrownBy(() -> transport.tcp(refused, SocketOptions.defaults(), Framing.LINES, (f, a) -> {}))
                .isInstanceOf(AdapterException.class);
        assertThat(ctx.charged.get()).isEqualTo(1);
        assertThat(counter(SocketMetrics.CONNECTS, "result", "failed")).isEqualTo(1.0);

        ctx.budgetLeft.set(0);
        assertThatThrownBy(() -> transport.tcp(endpoint(), SocketOptions.defaults(), Framing.LINES, (f, a) -> {}))
                .isInstanceOf(AdapterException.class).hasMessageContaining("budget");
        assertThat(accepted.poll(300, TimeUnit.MILLISECONDS)).as("nothing connected").isNull();
    }

    @Test
    @DisplayName("a public or wildcard destination is refused before any unit is spent")
    void lanOnly() throws Exception {
        LanEndpoint google = new LanEndpoint(LanEndpoint.Scheme.TCP, "8.8.8.8", 30003, InetAddress.getByName("8.8.8.8"));
        assertThatThrownBy(() -> transport.tcp(google, SocketOptions.defaults(), Framing.LINES, (f, a) -> {}))
                .isInstanceOf(AdapterException.class).hasMessageContaining("Refusing");
        LanEndpoint cgnat = new LanEndpoint(LanEndpoint.Scheme.TCP, "100.64.0.1", 1, InetAddress.getByName("100.64.0.1"));
        assertThatThrownBy(() -> transport.tcp(cgnat, SocketOptions.defaults(), Framing.LINES, (f, a) -> {}))
                .isInstanceOf(AdapterException.class);
        LanEndpoint udp = new LanEndpoint(LanEndpoint.Scheme.UDP, "0.0.0.0", 1, InetAddress.getByName("0.0.0.0"));
        assertThatThrownBy(() -> transport.tcp(udp, SocketOptions.defaults(), Framing.LINES, (f, a) -> {}))
                .isInstanceOf(AdapterException.class);
        assertThat(ctx.charged.get()).isZero();
    }
}
