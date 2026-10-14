package ai.foresignal.collect.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HostPort;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Prompt 082's additions to the socket transport: {@code tcpPublic} accepts only a descriptor's {@code allowedHosts}
 * entry that resolves to a public address, refusing everything else before any budget is spent; and the write rules
 * of {@link SocketOptions}: a receive-only connection sends zero bytes, the write-after-connect mode sends exactly the
 * login, and no handle has a write method.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TcpPublicTest {

    private static final Instant NOW = Instant.parse("2026-10-14T00:30:00Z");

    private ServerSocket server;
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
    }

    @AfterEach
    void tearDown() throws IOException {
        server.close();
        for (Socket s : accepted) {
            s.close();
        }
    }

    private static SocketStreamContext context(String... allowed) {
        SourceDescriptor.Builder b = SourceDescriptor.builder("aprs-test").kind("thing").defaultBudget(60, 1000);
        for (String hostPort : allowed) {
            b.allowedHost(hostPort);
        }
        return new SocketStreamContext(b.build(), new MutableClock(NOW), ValidatedParams.empty());
    }

    private static DefaultSocketTransport transport(SocketStreamContext ctx) {
        return new DefaultSocketTransport(ctx, new SocketMetrics(new SimpleMeterRegistry(), ctx.sourceId()), null);
    }

    private LanEndpoint loopback() {
        return new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", server.getLocalPort(),
                InetAddress.getLoopbackAddress());
    }

    private Socket peer() throws InterruptedException {
        Socket socket = accepted.poll(5, TimeUnit.SECONDS);
        assertThat(socket).as("the transport connected").isNotNull();
        return socket;
    }

    @Test
    @DisplayName("a host:port that is not in allowedHosts is refused before any budget is spent")
    void notAllowed() {
        SocketStreamContext ctx = context("rotate.aprs2.net:14580");

        assertThatThrownBy(() -> transport(ctx).tcpPublic(HostPort.parse("evil.example.com:14580"),
                SocketOptions.defaults(), Framing.LINES, (f, at) -> { }))
                .isInstanceOf(AdapterException.class).hasMessageContaining("allowedHosts");
        assertThatThrownBy(() -> transport(ctx).tcpPublic(HostPort.parse("rotate.aprs2.net:10152"),
                SocketOptions.defaults(), Framing.LINES, (f, at) -> { }))
                .isInstanceOf(AdapterException.class).hasMessageContaining("allowedHosts");
        assertThat(ctx.charged.get()).isZero();
    }

    @Test
    @DisplayName("an allowed name that resolves to a loopback address is refused before any budget is spent")
    void allowedButNotPublic() {
        SocketStreamContext ctx = context("localhost:" + server.getLocalPort());

        assertThatThrownBy(() -> transport(ctx).tcpPublic(HostPort.parse("localhost:" + server.getLocalPort()),
                SocketOptions.defaults(), Framing.LINES, (f, at) -> { }))
                .isInstanceOf(AdapterException.class).hasMessageContaining("not a public address");
        assertThat(ctx.charged.get()).isZero();
        assertThat(accepted).isEmpty();
    }

    @Test
    @DisplayName("isPublic refuses loopback, private, link-local, CGNAT and wildcard addresses")
    void publicAddresses() throws Exception {
        assertThat(DefaultSocketTransport.isPublic(InetAddress.getByName("8.8.8.8"))).isTrue();
        for (String address : new String[] {"127.0.0.1", "10.1.2.3", "192.168.1.20", "172.16.0.1", "169.254.169.254",
                "100.64.0.1", "0.0.0.0", "::1", "fd00::1", "fe80::1"}) {
            assertThat(DefaultSocketTransport.isPublic(InetAddress.getByName(address))).as(address).isFalse();
        }
    }

    @Test
    @DisplayName("receive-only: the peer reads zero bytes, then EOF only when the handle closes")
    void receiveOnlySendsNothing() throws Exception {
        SocketStreamContext ctx = context();
        StreamHandle handle = transport(ctx).tcp(loopback(), SocketOptions.receiveOnly(), Framing.RAW, (f, at) -> { });
        try (Socket peer = peer()) {
            peer.setSoTimeout(500);
            InputStream in = peer.getInputStream();
            assertThatThrownBy(in::read).isInstanceOf(SocketTimeoutException.class);
            handle.close();
            peer.setSoTimeout(5000);
            assertThat(in.read()).as("EOF, no byte").isEqualTo(-1);
        }
        assertThat(ctx.charged.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("receive-only after connect: exactly the onConnect bytes, then nothing")
    void loginOnly() throws Exception {
        byte[] login = "user N0CALL pass -1 vers Foresignal dev filter r/0/0/10\r\n".getBytes(StandardCharsets.US_ASCII);
        StreamHandle handle = transport(context()).tcp(loopback(),
                SocketOptions.receiveOnlyAfterConnect().withOnConnect(login), Framing.LINES, (f, at) -> { });
        try (Socket peer = peer()) {
            peer.setSoTimeout(5000);
            InputStream in = peer.getInputStream();
            byte[] got = in.readNBytes(login.length);
            assertThat(got).containsExactly(login);
            peer.setSoTimeout(500);
            assertThatThrownBy(in::read).isInstanceOf(SocketTimeoutException.class);
            handle.close();
        }
    }

    @Test
    @DisplayName("a receive-only option set rejects an onConnect writer, and no handle has a write method")
    void noWritePath() throws Exception {
        assertThatThrownBy(() -> SocketOptions.receiveOnly().withOnConnect(new byte[] {1}))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SocketOptions(null, new byte[] {1}, SocketOptions.WriteMode.NONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(SocketOptions.receiveOnly().isReceiveOnly()).isTrue();
        assertThat(SocketOptions.receiveOnly().onConnect()).isNull();
        assertThat(SocketOptions.receiveOnlyAfterConnect().isReceiveOnly()).isFalse();

        StreamHandle handle = transport(context()).tcp(loopback(), SocketOptions.receiveOnly(), Framing.RAW,
                (f, at) -> { });
        try {
            for (Class<?> type = handle.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    assertThat(method.getName().toLowerCase()).as(type.getName() + "." + method.getName())
                            .doesNotContain("write").doesNotContain("send");
                }
            }
            assertThat(Arrays.stream(StreamHandle.class.getMethods()).map(Method::getName))
                    .noneMatch(n -> n.toLowerCase().contains("write") || n.toLowerCase().contains("send"));
        } finally {
            handle.close();
        }
    }
}
