package ai.foresignal.source.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.socket.DefaultSocketTransport;
import ai.foresignal.collect.socket.SocketMetrics;
import ai.foresignal.mqtt.MqttConnections;
import ai.foresignal.mqtt.MqttProperties;
import ai.foresignal.net.Cidr;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.acars.LocalAcarsAdapter;
import ai.foresignal.source.adapters.acars.LocalAcarsFamily;
import ai.foresignal.source.adapters.local.LocalGpsdAdapter;
import ai.foresignal.source.adapters.remoteid.LocalRemoteIdAdapter;
import ai.foresignal.source.adapters.remoteid.RemoteIdMapper;
import ai.foresignal.source.adapters.space.localsonde.LocalSondeAdapter;
import ai.foresignal.source.codec.AcarsJsonCodec;
import ai.foresignal.source.codec.AutoRxPayloadCodec;
import ai.foresignal.source.codec.GpsdCodec;
import ai.foresignal.source.codec.UbxMonParser;
import ai.foresignal.source.formats.acars.AcarsMetadataMapper;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DatagramHandler;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FrameHandler;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.LanEndpoint;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SocketOptions;
import ai.foresignal.source.spi.SocketTransport;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.mqtt.MosquittoSupport;
import ai.foresignal.testsupport.mqtt.MqttProbe;
import ai.foresignal.testsupport.stream.SocketStreamContext;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * The prompt 086 streaming sources against embedded servers: {@code local-acars-{n}} against three loopback TCP servers
 * standing in for acars_router (one receive-only connection each, nothing written, reconnect after a drop),
 * {@code local-gpsd-{n}} against a loopback gpsd (only the guarded {@code ?WATCH} written, UBX read from the raw
 * passthrough), {@code local-sonde} through a loopback UDP listener, and {@code local-remoteid} against prompt 060's
 * Mosquitto broker (one unit per CONNECT and SUBSCRIBE, a lost connection reported for the supervisor's reconnect).
 */
@Timeout(value = 90, unit = TimeUnit.SECONDS)
class LocalRfAdaptersTest {

    private static final Instant NOW = Instant.parse("2026-10-13T14:01:00Z");

    private final MutableClock clock = new MutableClock(NOW);
    private final List<ServerSocket> servers = new CopyOnWriteArrayList<>();
    private final List<StreamHandle> handles = new CopyOnWriteArrayList<>();

    /** The real transport, recording the options of every connection. */
    static final class RecordingTransport implements SocketTransport {

        final List<SocketOptions> options = new CopyOnWriteArrayList<>();
        final List<Framing> framings = new CopyOnWriteArrayList<>();
        private final DefaultSocketTransport delegate;

        RecordingTransport(SocketStreamContext ctx) {
            this.delegate = new DefaultSocketTransport(ctx, new SocketMetrics(null, ctx.sourceId()), null);
        }

        @Override
        public StreamHandle tcp(LanEndpoint endpoint, SocketOptions opts, Framing framing, FrameHandler handler)
                throws AdapterException {
            options.add(opts);
            framings.add(framing);
            return delegate.tcp(endpoint, opts, framing, handler);
        }

        @Override
        public StreamHandle udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler handler)
                throws AdapterException {
            return delegate.udp(bind, allowSenders, handler);
        }
    }

    /** A loopback server whose accepted sockets land in {@code accepted}. */
    private ServerSocket server(BlockingQueue<Socket> accepted) throws IOException {
        ServerSocket server = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
        servers.add(server);
        Thread.ofVirtual().start(() -> {
            while (!server.isClosed()) {
                try {
                    accepted.add(server.accept());
                } catch (IOException e) {
                    return;
                }
            }
        });
        return server;
    }

    @BeforeEach
    void reset() {
        clock.setInstant(NOW);
    }

    @AfterEach
    void tearDown() throws IOException {
        handles.forEach(StreamHandle::close);
        for (ServerSocket s : servers) {
            s.close();
        }
    }

    private static URI tcp(ServerSocket s) {
        return URI.create("tcp://127.0.0.1:" + s.getLocalPort());
    }

    private static Socket take(BlockingQueue<Socket> q) throws InterruptedException {
        Socket s = q.poll(5, TimeUnit.SECONDS);
        assertThat(s).as("the adapter connected").isNotNull();
        return s;
    }

    private static void assertNothingWritten(Socket peer) throws IOException {
        peer.setSoTimeout(300);
        InputStream in = peer.getInputStream();
        assertThatThrownBy(in::read).as("Foresignal writes nothing").isInstanceOf(SocketTimeoutException.class);
    }

    @Test
    @DisplayName("local-acars: one receive-only connection per endpoint, the three shapes, text never stored, reconnect")
    void acars() throws Exception {
        BlockingQueue<Socket> acarsQ = new LinkedBlockingQueue<>();
        BlockingQueue<Socket> vdlQ = new LinkedBlockingQueue<>();
        BlockingQueue<Socket> hfdlQ = new LinkedBlockingQueue<>();
        ServerSocket acars = server(acarsQ);
        ServerSocket vdl = server(vdlQ);
        ServerSocket hfdl = server(hfdlQ);
        LocalAcarsAdapter adapter = new LocalAcarsAdapter(1, new AcarsJsonCodec());
        InetAddress lo = InetAddress.getLoopbackAddress();
        ValidatedParams params = new ValidatedParams(Map.of(LocalAcarsAdapter.ACARS_ENDPOINT, tcp(acars),
                LocalAcarsAdapter.VDLM2_ENDPOINT, tcp(vdl), LocalAcarsAdapter.HFDL_ENDPOINT, tcp(hfdl),
                LocalAcarsAdapter.STATION_ID, "home"), Map.of(LocalAcarsAdapter.ACARS_ENDPOINT, lo,
                LocalAcarsAdapter.VDLM2_ENDPOINT, lo, LocalAcarsAdapter.HFDL_ENDPOINT, lo));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, params);
        RecordingTransport transport = new RecordingTransport(ctx);
        ctx.withSockets(transport);
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        StreamHandle handle = adapter.start(ctx, records::add);
        handles.add(handle);
        assertThat(ctx.charged).hasValue(3);
        assertThat(transport.options).hasSize(3).allSatisfy(o -> assertThat(o.isReceiveOnly()).isTrue());

        Socket a = take(acarsQ);
        Socket v = take(vdlQ);
        Socket h = take(hfdlQ);
        a.getOutputStream().write((AdapterFixtures.text("local-rf", "acarsdec.json").strip() + "\n")
                .getBytes(StandardCharsets.UTF_8));
        String vdlLine = AdapterFixtures.text("local-rf", "dumpvdl2.json").strip();
        // Back to back, without a newline, split across writes.
        OutputStream vo = v.getOutputStream();
        vo.write(vdlLine.substring(0, 40).getBytes(StandardCharsets.UTF_8));
        vo.flush();
        vo.write((vdlLine.substring(40) + vdlLine.replace("D01", "D02")).getBytes(StandardCharsets.UTF_8));
        h.getOutputStream().write((AdapterFixtures.text("local-rf", "dumphfdl.json").strip() + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
        await().atMost(Duration.ofSeconds(10)).until(() -> records.size() == 4);
        assertThat(records).extracting(r -> r.payload().get("protocol")).containsExactlyInAnyOrder("acars", "vdl2",
                "vdl2", "hfdl");
        assertThat(records).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(AcarsMetadataMapper.KIND);
            assertThat(r.payload().keySet()).isSubsetOf(AcarsMetadataMapper.PAYLOAD_FIELDS);
            assertThat(String.valueOf(r)).doesNotContain("SYNTHETIC");
            assertThat(r.observers().getFirst().receiverId()).isEqualTo("home");
        });
        FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);
        assertNothingWritten(a);
        assertNothingWritten(v);
        assertNothingWritten(h);

        List<Throwable> closed = new CopyOnWriteArrayList<>();
        handle.onClose(closed::add);
        v.close();
        await().atMost(Duration.ofSeconds(10)).until(() -> !closed.isEmpty());
        assertThat(handle.isRunning()).isFalse();
        // The supervisor reconnects by starting again: three new connections, three more units.
        StreamHandle again = adapter.start(ctx, records::add);
        handles.add(again);
        take(acarsQ);
        take(vdlQ);
        take(hfdlQ);
        assertThat(ctx.charged).hasValue(6);
    }

    @Test
    @DisplayName("local-acars: no endpoint is refused before any unit; a failed connect closes the ones opened")
    void acarsParams() throws Exception {
        LocalAcarsAdapter adapter = new LocalAcarsAdapter(1, new AcarsJsonCodec());
        assertThat(adapter.checkParams(ValidatedParams.empty())).containsKey(LocalAcarsAdapter.ACARS_ENDPOINT);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalAcarsAdapter.STATION_ID, "bad id!"))))
                .containsKey(LocalAcarsAdapter.STATION_ID);
        SocketStreamContext empty = new SocketStreamContext(adapter.descriptor(), clock, ValidatedParams.empty());
        assertThatThrownBy(() -> adapter.start(empty, r -> {})).isInstanceOf(AdapterException.class);
        assertThat(empty.charged).hasValue(0);

        BlockingQueue<Socket> q = new LinkedBlockingQueue<>();
        ServerSocket up = server(q);
        ServerSocket down = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        int deadPort = down.getLocalPort();
        down.close();
        InetAddress lo = InetAddress.getLoopbackAddress();
        ValidatedParams params = new ValidatedParams(Map.of(LocalAcarsAdapter.ACARS_ENDPOINT, tcp(up),
                LocalAcarsAdapter.HFDL_ENDPOINT, URI.create("tcp://127.0.0.1:" + deadPort)),
                Map.of(LocalAcarsAdapter.ACARS_ENDPOINT, lo, LocalAcarsAdapter.HFDL_ENDPOINT, lo));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, params);
        ctx.withSockets(new RecordingTransport(ctx));
        assertThatThrownBy(() -> adapter.start(ctx, r -> {})).isInstanceOf(AdapterException.class);
        Socket opened = take(q);
        opened.setSoTimeout(5_000);
        assertThat(opened.getInputStream().read()).as("the first connection was closed again").isEqualTo(-1);

        LocalAcarsFamily family = new LocalAcarsFamily(3, new AcarsJsonCodec());
        assertThat(family.adapters()).extracting(x -> x.descriptor().id())
                .containsExactly("local-acars-1", "local-acars-2", "local-acars-3");
        assertThatThrownBy(() -> new LocalAcarsFamily(0, new AcarsJsonCodec())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new LocalAcarsFamily(17, new AcarsJsonCodec())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("descriptors: acars private, LOCAL_ONLY fixed, 7 d, no TTL; drone TRACK; gpsd integrity kind 30 d")
    void descriptors() {
        LocalAcarsAdapter acarsAdapter = new LocalAcarsAdapter(1, new AcarsJsonCodec());
        var acars = acarsAdapter.descriptor();
        assertThat(acars.privateSource()).isTrue();
        assertThat(acars.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(acars.egressOverridable()).isFalse();
        assertThat(acars.defaultRetention()).isEqualTo(Duration.ofDays(7));
        assertThat(acars.recordTtl()).isNull();
        assertThat(acars.embedFields()).isEmpty();
        assertThat(acars.historyTier()).isEqualTo(HistoryTier.STANDARD);
        LocalRemoteIdAdapter droneAdapter = new LocalRemoteIdAdapter(null, () -> "x", null);
        var drone = droneAdapter.descriptor();
        assertThat(drone.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(drone.recordTtl()).isEqualTo(Duration.ofMinutes(3));
        assertThat(drone.significantFields()).containsExactly("idType", "uaType", "operatorId");
        assertThat(drone.egress()).isEqualTo(Egress.LOCAL_ONLY);
        LocalGpsdAdapter gpsdAdapter = new LocalGpsdAdapter(1, new GpsdCodec());
        var gpsd = gpsdAdapter.descriptor();
        assertThat(gpsd.kinds()).contains(GpsdCodec.KIND, GpsdCodec.INTEGRITY_KIND);
        assertThat(gpsd.kindRetention()).containsEntry(GpsdCodec.INTEGRITY_KIND, Duration.ofDays(30));
        assertThat(gpsd.significantFields()).contains("integrityState");
        LocalSondeAdapter sondeAdapter = new LocalSondeAdapter(new AutoRxPayloadCodec());
        var sonde = sondeAdapter.descriptor();
        assertThat(sonde.params()).anySatisfy(p -> assertThat(p.name()).isEqualTo(LocalSondeAdapter.STATION_CALLSIGN_PARAM));
        for (ai.foresignal.source.spi.SourceAdapter a : List.of(acarsAdapter, droneAdapter, gpsdAdapter, sondeAdapter)) {
            var d = a.descriptor();
            assertThat(DescriptorValidator.problems(d, a)).as(d.id()).isEmpty();
            assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "ingestPolicy")).as(d.id()).isEmpty();
        }
    }

    @Test
    @DisplayName("local-gpsd: only the guarded ?WATCH is written; ubxMonitor reads raw, adds raw 2 and decodes MON-HW")
    void gpsd() throws Exception {
        for (boolean ubx : new boolean[] {false, true}) {
            BlockingQueue<Socket> q = new LinkedBlockingQueue<>();
            ServerSocket s = server(q);
            LocalGpsdAdapter adapter = new LocalGpsdAdapter(1, new GpsdCodec());
            Map<String, Object> values = new HashMap<>();
            values.put(LocalGpsdAdapter.ENDPOINT_PARAM, tcp(s));
            values.put(LocalGpsdAdapter.UBX_MONITOR, ubx);
            values.put(LocalGpsdAdapter.STATION_LABEL, "roof");
            ValidatedParams params = new ValidatedParams(values,
                    Map.of(LocalGpsdAdapter.ENDPOINT_PARAM, InetAddress.getLoopbackAddress()));
            SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, params);
            RecordingTransport transport = new RecordingTransport(ctx);
            ctx.withSockets(transport);
            List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
            handles.add(adapter.start(ctx, records::add));
            Socket peer = take(q);
            byte[] expected = GpsdCodec.watchCommand(ubx);
            byte[] got = peer.getInputStream().readNBytes(expected.length);
            assertThat(got).isEqualTo(expected);
            assertThat(transport.framings.getFirst()).isEqualTo(ubx ? Framing.RAW : Framing.LINES);
            assertThat(transport.options.getFirst().writeMode()).isEqualTo(SocketOptions.WriteMode.ON_CONNECT_ONLY);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            String tpv = "{\"class\":\"TPV\",\"device\":\"d\",\"mode\":3,\"time\":\"2026-10-13T12:00:%02dZ\",\"lat\":1.0,"
                    + "\"lon\":2.0,\"eph\":3.0}\n";
            stream.writeBytes("{\"class\":\"VERSION\",\"proto_major\":3,\"proto_minor\":15}\n".getBytes(StandardCharsets.UTF_8));
            stream.writeBytes(String.format(tpv, 0).getBytes(StandardCharsets.UTF_8));
            stream.writeBytes("{\"class\":\"SKY\",\"device\":\"d\",\"uSat\":8,\"nSat\":9}\n".getBytes(StandardCharsets.UTF_8));
            if (ubx) {
                byte[] payload = new byte[60];
                payload[22] = (byte) (3 << 2);
                payload[45] = (byte) 240;
                stream.writeBytes(UbxMonParser.frame(UbxMonParser.CLASS_MON, UbxMonParser.ID_MON_HW, payload));
            }
            stream.writeBytes(String.format(tpv, 31).getBytes(StandardCharsets.UTF_8));
            peer.getOutputStream().write(stream.toByteArray());
            await().atMost(Duration.ofSeconds(10))
                    .until(() -> records.stream().anyMatch(r -> r.kind().equals(GpsdCodec.INTEGRITY_KIND)));
            NormalizedRecord integrity = records.stream().filter(r -> r.kind().equals(GpsdCodec.INTEGRITY_KIND))
                    .findFirst().orElseThrow();
            assertThat(integrity.payload().get("integrityState")).isEqualTo(ubx ? "JAMMED" : "OK");
            FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);
            assertThat(peer.getInputStream().available()).as("nothing after ?WATCH").isZero();
            assertNothingWritten(peer);
        }
    }

    @Test
    @DisplayName("local-sonde: a PAYLOAD_SUMMARY datagram from an allowed sender becomes a record with the station callsign")
    void sondeUdp() throws Exception {
        int port;
        try (DatagramSocket probe = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            port = probe.getLocalPort();
        }
        LocalSondeAdapter adapter = new LocalSondeAdapter(new AutoRxPayloadCodec());
        ValidatedParams params = new ValidatedParams(Map.of(LocalSondeAdapter.ENDPOINT_PARAM,
                URI.create("udp://127.0.0.1:" + port), LocalSondeAdapter.ALLOW_SENDERS_PARAM, List.of("127.0.0.1"),
                LocalSondeAdapter.STATION_CALLSIGN_PARAM, "FS0HOME"),
                Map.of(LocalSondeAdapter.ENDPOINT_PARAM, InetAddress.getLoopbackAddress()));
        SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), clock, params);
        ctx.withSockets(new RecordingTransport(ctx));
        List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
        handles.add(adapter.start(ctx, records::add));
        try (DatagramSocket sender = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            for (String line : AdapterFixtures.text("local-rf", "autorx-summaries.jsonl").lines().toList()) {
                byte[] b = line.getBytes(StandardCharsets.UTF_8);
                sender.send(new DatagramPacket(b, b.length, new InetSocketAddress(InetAddress.getLoopbackAddress(), port)));
            }
        }
        await().atMost(Duration.ofSeconds(10)).until(() -> records.size() == 10);
        assertThat(records).allSatisfy(r -> assertThat(r.observers().getFirst().receiverId()).isEqualTo("FS0HOME"));
        assertThat(ctx.charged).hasValue(1);
    }

    @Test
    @DisplayName("local-remoteid: CONNECT and SUBSCRIBE paid, Remote ID mapped, DJI and side topics dropped, drop reported")
    void remoteIdMqtt() throws Exception {
        MosquittoSupport broker = MosquittoSupport.get();
        MqttConnections connections = new MqttConnections(MqttProperties.defaults(),
                OutboundUrlPolicy.withoutOverrides(InetAddress::getAllByName),
                new StaticListableBeanFactory().getBeanProvider(MeterRegistry.class), Clock.systemUTC());
        try {
            String root = "wardragon" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            LocalRemoteIdAdapter adapter = new LocalRemoteIdAdapter(connections, () -> "itest086", null);
            URI url = URI.create(broker.passwordUrl());
            ValidatedParams params = new ValidatedParams(Map.of(LocalRemoteIdAdapter.BROKER_URL,
                    URI.create("mqtt://" + url.getHost() + ":" + url.getPort()), LocalRemoteIdAdapter.TOPICS,
                    List.of(root + "/#")), Map.of(LocalRemoteIdAdapter.BROKER_URL, InetAddress.getByName(url.getHost())));
            SocketStreamContext ctx = new SocketStreamContext(adapter.descriptor(), Clock.systemUTC(), params)
                    .withCredentials(Map.of(LocalRemoteIdAdapter.USERNAME, MosquittoSupport.USER,
                            LocalRemoteIdAdapter.PASSWORD, MosquittoSupport.PASSWORD));
            List<NormalizedRecord> records = new CopyOnWriteArrayList<>();
            StreamHandle handle = adapter.start(ctx, records::add);
            handles.add(handle);
            assertThat(ctx.charged).hasValue(2);
            try (MqttProbe publisher = MqttProbe.subscribe(broker, "unused/" + UUID.randomUUID())) {
                for (String line : AdapterFixtures.text("local-rf", "dragonsync-session.tsv").lines().toList()) {
                    int tab = line.indexOf('\t');
                    publisher.publish(line.substring(0, tab).replaceFirst("^wardragon", root), line.substring(tab + 1));
                }
                await().atMost(Duration.ofSeconds(15)).until(() -> records.size() == 4
                        && adapter.dropped(RemoteIdMapper.Drop.TOPIC) == 4);
            }
            assertThat(adapter.dropped(RemoteIdMapper.Drop.DJI)).isEqualTo(1);
            assertThat(records).allSatisfy(r -> assertThat(r.payload()).doesNotContainKey("operatorPoint"));
            FieldSpecContract.assertPayloadKeysDeclared(adapter.descriptor(), records);

            LocalRemoteIdAdapter proxied = new LocalRemoteIdAdapter(connections, () -> "itest086b", null);
            URI p = URI.create(broker.proxiedUrl());
            ValidatedParams pp = new ValidatedParams(Map.of(LocalRemoteIdAdapter.BROKER_URL,
                    URI.create("mqtt://" + p.getHost() + ":" + p.getPort())),
                    Map.of(LocalRemoteIdAdapter.BROKER_URL, InetAddress.getByName(p.getHost())));
            SocketStreamContext pctx = new SocketStreamContext(proxied.descriptor(), Clock.systemUTC(), pp);
            handle.close();
            StreamHandle dropping = proxied.start(pctx, r -> {});
            handles.add(dropping);
            List<Throwable> closed = new CopyOnWriteArrayList<>();
            dropping.onClose(closed::add);
            try {
                broker.cut();
                await().atMost(Duration.ofSeconds(30)).until(() -> !closed.isEmpty());
            } finally {
                broker.restore();
            }
            assertThat(dropping.isRunning()).isFalse();
            assertThat(pctx.charged).as("no reconnect on its own").hasValue(2);
        } finally {
            connections.closeAll();
        }
    }

    @Test
    @DisplayName("local-remoteid: topic filters are checked; the default subscribes to DragonSync's aggregate topic")
    void remoteIdParams() {
        LocalRemoteIdAdapter adapter = new LocalRemoteIdAdapter(null, () -> "x", null);
        assertThat(adapter.checkParams(ValidatedParams.empty())).isEmpty();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalRemoteIdAdapter.TOPICS, List.of("a/#/b")))))
                .containsKey(LocalRemoteIdAdapter.TOPICS);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(LocalRemoteIdAdapter.TOPICS,
                List.of("a", "b", "c", "d", "e"))))).containsKey(LocalRemoteIdAdapter.TOPICS);
        assertThat(adapter.descriptor().params()).anySatisfy(p -> {
            assertThat(p.name()).isEqualTo(LocalRemoteIdAdapter.TOPICS);
            assertThat(p.defaultValue()).isEqualTo(List.of(LocalRemoteIdAdapter.DEFAULT_TOPIC));
        });
    }
}
