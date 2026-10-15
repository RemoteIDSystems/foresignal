package ai.foresignal.collect.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.net.Cidr;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.test.MutableClock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The UDP half of the socket transport (prompt 069) on loopback: a datagram from an allowed sender is
 * delivered; one from outside the allowlist is dropped and counted and never reaches the handler; an
 * oversize one is dropped and counted; a bind costs one budget unit.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class UdpTransportIT {

    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");

    private TestStreamContext ctx;
    private SimpleMeterRegistry meters;
    private DefaultSocketTransport transport;

    @BeforeEach
    void setUp() {
        ctx = new TestStreamContext("local-ais-1", new MutableClock(NOW));
        meters = new SimpleMeterRegistry();
        transport = new DefaultSocketTransport(ctx, new SocketMetrics(meters, "local-ais-1"), null, 32, 64);
    }

    private static int freePort() throws Exception {
        try (DatagramSocket probe = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            return probe.getLocalPort();
        }
    }

    private static void send(int port, String text) throws Exception {
        try (DatagramSocket sender = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
            sender.send(new DatagramPacket(bytes, bytes.length, new InetSocketAddress(InetAddress.getLoopbackAddress(), port)));
        }
    }

    private double counter(String name) {
        var c = meters.find(name).tag("source", "local-ais-1").counter();
        return c == null ? 0.0 : c.count();
    }

    @Test
    @DisplayName("an allowed sender's datagram is delivered with its sender and the context time")
    void allowedSender() throws Exception {
        int port = freePort();
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        StreamHandle handle = transport.udp(
                new LanEndpoint(LanEndpoint.Scheme.UDP, "127.0.0.1", port, InetAddress.getLoopbackAddress()),
                Set.of(Cidr.parse("127.0.0.0/8").orElseThrow()),
                (datagram, sender, at) -> received.add(new String(datagram, StandardCharsets.US_ASCII) + "@"
                        + sender.getAddress().getHostAddress() + "@" + at));
        try {
            // Shorter than maxDatagramBytes (32), or it would be dropped as oversize.
            send(port, "!AIVDM,1,1,,A,13u?etPv2;0n,0*23");
            String got = received.poll(5, TimeUnit.SECONDS);
            assertThat(got).startsWith("!AIVDM").endsWith("@127.0.0.1@" + NOW);
        } finally {
            handle.close();
        }
        assertThat(ctx.charged.get()).isEqualTo(1);
        assertThat(counter(SocketMetrics.FRAMES)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("a sender outside the allowlist is dropped, counted and never parsed")
    void disallowedSender() throws Exception {
        int port = freePort();
        BlockingQueue<byte[]> received = new LinkedBlockingQueue<>();
        StreamHandle handle = transport.udp(
                new LanEndpoint(LanEndpoint.Scheme.UDP, "127.0.0.1", port, InetAddress.getLoopbackAddress()),
                Set.of(Cidr.parse("192.168.77.0/24").orElseThrow()),
                (datagram, sender, at) -> received.add(datagram));
        try {
            send(port, "hello");
            send(port, "again");
            assertThat(received.poll(500, TimeUnit.MILLISECONDS)).isNull();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (counter(SocketMetrics.UDP_REJECTED) < 2 && System.nanoTime() < deadline) {
                Thread.sleep(20);
            }
            assertThat(counter(SocketMetrics.UDP_REJECTED)).isEqualTo(2.0);
            assertThat(counter(SocketMetrics.FRAMES)).isZero();
        } finally {
            handle.close();
        }
    }

    @Test
    @DisplayName("a datagram longer than the limit is dropped and counted; the next one still arrives")
    void oversize() throws Exception {
        int port = freePort();
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        StreamHandle handle = transport.udp(
                new LanEndpoint(LanEndpoint.Scheme.UDP, "127.0.0.1", port, InetAddress.getLoopbackAddress()),
                Set.of(Cidr.parse("127.0.0.1").orElseThrow()),
                (datagram, sender, at) -> received.add(new String(datagram, StandardCharsets.US_ASCII)));
        try {
            send(port, "Y".repeat(40));
            send(port, "small");
            assertThat(received.poll(5, TimeUnit.SECONDS)).isEqualTo("small");
            assertThat(meters.find(SocketMetrics.FRAMES_DROPPED).tag("reason", SocketMetrics.REASON_OVERSIZE).counter()
                    .count()).isEqualTo(1.0);
        } finally {
            handle.close();
        }
        assertThat(handle.isRunning()).isFalse();
    }

    @Test
    @DisplayName("an empty or public allowlist and a tcp endpoint are refused before a unit is spent")
    void refusals() throws Exception {
        LanEndpoint bind = new LanEndpoint(LanEndpoint.Scheme.UDP, "127.0.0.1", 1, InetAddress.getLoopbackAddress());
        assertThatThrownBy(() -> transport.udp(bind, Set.of(), (d, s, a) -> {})).isInstanceOf(AdapterException.class);
        assertThatThrownBy(() -> transport.udp(bind, Set.of(Cidr.parse("8.8.8.0/24").orElseThrow()), (d, s, a) -> {}))
                .isInstanceOf(AdapterException.class);
        LanEndpoint tcp = new LanEndpoint(LanEndpoint.Scheme.TCP, "127.0.0.1", 1, InetAddress.getLoopbackAddress());
        assertThatThrownBy(() -> transport.udp(tcp, Set.of(Cidr.parse("127.0.0.1").orElseThrow()), (d, s, a) -> {}))
                .isInstanceOf(AdapterException.class);
        assertThat(ctx.charged.get()).isZero();
    }
}
