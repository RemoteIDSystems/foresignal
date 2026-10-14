package ai.foresignal.notify.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.mesh.MeshCoreSender;
import ai.foresignal.notify.mesh.MeshCoreSlotProbe;
import ai.foresignal.notify.mesh.cbor.MeshCborProfile;
import ai.foresignal.source.adapters.mesh.MeshCoreLinks;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.testsupport.net.TestOutbound;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * The {@code meshcore} channel type against a fake companion on a loopback socket (prompt 084): the exact frame bytes
 * of 0x03 (channel text), 0x3E (channel datagram) and 0x02 (direct text), the public-slot refusal after
 * {@code CMD_GET_CHANNEL} (at save and at send), a direct message acknowledged by {@code PUSH_CODE_SEND_CONFIRMED},
 * and a missing acknowledgement retried once with {@code attempt=1} before the delivery ends {@code unacked}.
 */
class MeshCoreChannelTest {

    static final HexFormat HEX = HexFormat.of();
    static final String TEXT = "!! Quakes Austin 1200Z #0007";
    /** {@code NotifyFixtures.T} (2026-05-01T12:00:00Z, 1777636800) as uint32 little-endian. */
    static final String TS = "c095f469";

    private ServerSocket server;
    private final List<byte[]> received = new CopyOnWriteArrayList<>();
    private final AtomicBoolean confirm = new AtomicBoolean(true);
    private final AtomicInteger dms = new AtomicInteger();
    private MeshCoreLinks links;
    private MeshCoreSender sender;
    private final UUID channelId = UUID.randomUUID();

    @BeforeEach
    void companion() throws IOException {
        server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
        Thread.ofVirtual().start(this::serve);
        links = new MeshCoreLinks(Clock.systemUTC());
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("policy", TestOutbound.offlinePolicy());
        ObjectProvider<OutboundUrlPolicy> policy = beans.getBeanProvider(OutboundUrlPolicy.class);
        sender = new MeshCoreSender(links, policy, beans.getBeanProvider(MeterRegistry.class), Clock.systemUTC());
    }

    @AfterEach
    void close() throws IOException {
        sender.detach(channelId);
        server.close();
    }

    /** The fake companion: answers every command a real one would, with slots 1 private and 2-5 public. */
    private void serve() {
        while (!server.isClosed()) {
            try (Socket socket = server.accept()) {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                OutputStream out = socket.getOutputStream();
                while (true) {
                    int marker = in.read();
                    if (marker < 0) {
                        break;
                    }
                    int length = in.readUnsignedByte() | in.readUnsignedByte() << 8;
                    byte[] payload = new byte[length];
                    in.readFully(payload);
                    received.add(payload);
                    answer(payload, out);
                }
            } catch (IOException e) {
                // the test closed the server or the link went away
            }
        }
    }

    private void answer(byte[] p, OutputStream out) throws IOException {
        switch (p[0] & 0xFF) {
            case MeshCoreProtocol.CMD_APP_START -> frame(out, bytes(0x05, new byte[60]));
            case MeshCoreProtocol.CMD_DEVICE_QUERY -> frame(out, bytes(0x0D, new byte[20]));
            case MeshCoreProtocol.CMD_GET_CHANNEL -> frame(out, channelInfo(p[1] & 0xFF));
            case MeshCoreProtocol.CMD_GET_CONTACTS -> frame(out, new byte[] {MeshCoreProtocol.RESP_END_OF_CONTACTS});
            case MeshCoreProtocol.CMD_GET_BATT_AND_STORAGE -> frame(out, new byte[] {0x0C, 0x10, 0x0E});
            case MeshCoreProtocol.CMD_SYNC_NEXT_MESSAGE -> frame(out, new byte[] {MeshCoreProtocol.RESP_NO_MORE_MESSAGES});
            case MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG, MeshCoreProtocol.CMD_SEND_CHANNEL_DATA ->
                    frame(out, new byte[] {MeshCoreProtocol.RESP_OK});
            case MeshCoreProtocol.CMD_SEND_TXT_MSG -> {
                int n = dms.incrementAndGet();
                byte[] ack = {0x44, 0x33, 0x22, (byte) (0x10 + n)};
                frame(out, bytes(MeshCoreProtocol.RESP_SENT, new byte[] {1, ack[0], ack[1], ack[2], ack[3],
                        (byte) 0xE8, 0x03, 0, 0}));
                if (confirm.get()) {
                    frame(out, bytes(MeshCoreProtocol.PUSH_SEND_CONFIRMED, new byte[] {ack[0], ack[1], ack[2], ack[3],
                            0x20, 0, 0, 0}));
                }
            }
            default -> frame(out, new byte[] {MeshCoreProtocol.RESP_ERR, 1});
        }
    }

    /** {@code RESP_CODE_CHANNEL_INFO}: 1 Family (private), 2 Public, 3 #ham, 4 Zero (all-zero secret), else empty. */
    static byte[] channelInfo(int index) {
        String name = switch (index) {
            case 0 -> "Public";
            case 1 -> "Family";
            case 2 -> "Public";
            case 3 -> "#ham";
            case 4 -> "Zero";
            default -> "";
        };
        byte[] out = new byte[2 + 32 + 16];
        out[0] = MeshCoreProtocol.RESP_CHANNEL_INFO;
        out[1] = (byte) index;
        byte[] n = name.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(n, 0, out, 2, n.length);
        if (index == 1 || index == 2 || index == 3) {
            for (int i = 34; i < 50; i++) {
                out[i] = (byte) (i * 7);
            }
        }
        return out;
    }

    private static byte[] bytes(int code, byte[] rest) {
        byte[] out = new byte[rest.length + 1];
        out[0] = (byte) code;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }

    private static synchronized void frame(OutputStream out, byte[] payload) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write('>');
        b.write(payload.length & 0xFF);
        b.write(payload.length >>> 8);
        b.write(payload);
        out.write(b.toByteArray());
        out.flush();
    }

    private Map<String, Object> config(int index) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("companionUrl", "tcp://127.0.0.1:" + server.getLocalPort());
        c.put("channelIndex", index);
        c.put("region", "EU_868");
        c.put("airtimeCustom", Map.of("spreadingFactor", 10, "bandwidthKhz", 250, "codingRate", 5,
                "preambleSymbols", 8));
        return c;
    }

    private ChannelSettings settings(Map<String, Object> config) {
        return new ChannelSettings(channelId, "mc", config, Map.of(), ZoneId.of("UTC"), 99, NotifyFixtures.T,
                NotifyFixtures.ORIGIN, "Foresignal/test");
    }

    private MeshCoreChannel type() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("probe", sender);
        return new MeshCoreChannel(TestOutbound.offlinePolicy(), beans.getBeanProvider(MeshCoreSlotProbe.class));
    }

    static NotificationMessage fired() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        data.put("firedAt", "2026-05-01T12:00:00Z");
        data.put("region", Map.of("near", Map.of("name", "Austin")));
        return NotificationMessage.builder("alert", "alert.fired", "[warning] Quakes", "warning").id(42L)
                .shortTitle("Quakes").data(data).createdAt(NotifyFixtures.T).alertId(7L).build();
    }

    private byte[] lastSent(int code) {
        for (int i = received.size() - 1; i >= 0; i--) {
            if ((received.get(i)[0] & 0xFF) == code) {
                return received.get(i);
            }
        }
        return null;
    }

    @Test
    @DisplayName("0x03 channel text: 03 00 {idx} {ts u32 LE} {utf8}, answered RESP_CODE_OK")
    void channelText() throws IOException {
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type().render(fired(), settings(config(1))).body();
        assertThat(HEX.formatHex(mesh.payload())).isEqualTo("030001" + TS
                + HEX.formatHex(TEXT.getBytes(StandardCharsets.UTF_8)));
        ChannelResponse response = sender.send(channelId, config(1), mesh, () -> true);
        assertThat(response.body()).isEqualTo("OK");
        assertThat(lastSent(0x03)).isEqualTo(mesh.payload());
        assertThat(links.link(((ai.foresignal.notify.mesh.MeshLanUrls.Pinned) ai.foresignal.notify.mesh.MeshLanUrls
                .pin(config(1).get("companionUrl").toString(), java.util.Set.of("tcp"), TestOutbound.offlinePolicy()))
                .endpoint()).guard().allowed()).contains(0x02, 0x03, 0x3E);
    }

    @Test
    @DisplayName("0x3E channel datagram: 3E {idx} FF {dataType u16 LE} {cbor}")
    void datagram() throws IOException {
        Map<String, Object> c = config(1);
        c.put("profile", "cbor");
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type().render(fired(), settings(c)).body();
        assertThat(HEX.formatHex(mesh.payload(), 0, 5)).isEqualTo("3e01ff45ff");
        byte[] cbor = java.util.Arrays.copyOfRange(mesh.payload(), 5, mesh.payload().length);
        assertThat(cbor.length).isLessThanOrEqualTo(MeshCborProfile.MAX_BYTES);
        assertThat(MeshCborProfile.decode(cbor).shortCode()).isEqualTo("0007");
        assertThat(sender.send(channelId, c, mesh, () -> true).body()).isEqualTo("OK");
        assertThat(lastSent(0x3E)).isEqualTo(mesh.payload());
    }

    @Test
    @DisplayName("0x02 direct text: 02 00 {attempt} {ts} {prefix 6 bytes} {utf8}; acknowledged by 0x82")
    void directAcked() throws IOException {
        Map<String, Object> c = config(1);
        c.put("dmContactPrefix", "A1B2C3D4E5F6");
        c.put("requireAck", true);
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type().render(fired(), settings(c)).body();
        assertThat(HEX.formatHex(mesh.payload())).isEqualTo("020000" + TS + "a1b2c3d4e5f6"
                + HEX.formatHex(TEXT.getBytes(StandardCharsets.UTF_8)));
        assertThat(mesh.requireAck()).isTrue();
        ChannelResponse response = sender.send(channelId, c, mesh, () -> true);
        assertThat(response.body()).isEqualTo("ACKED");
        assertThat(type().unacknowledged(response)).isFalse();
        assertThat(dms.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("no acknowledgement: one retry with attempt=1, then UNACKED (a terminal status, not failed)")
    void directUnacked() throws IOException {
        confirm.set(false);
        Map<String, Object> c = config(1);
        c.put("dmContactPrefix", "a1b2c3d4e5f6");
        c.put("requireAck", true);
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type().render(fired(), settings(c)).body();
        ChannelResponse response = sender.send(channelId, c, mesh, () -> true);
        assertThat(response.body()).isEqualTo(MeshCoreChannel.UNACKED);
        assertThat(type().unacknowledged(response)).isTrue();
        assertThat(dms.get()).isEqualTo(2);
        assertThat(lastSent(0x02)[2]).as("the retry's attempt byte").isEqualTo((byte) 1);

        int before = dms.get();
        ChannelResponse refused = sender.send(channelId, c, mesh, () -> false);
        assertThat(refused.body()).as("the governor refused the retry").isEqualTo(MeshCoreChannel.UNACKED);
        assertThat(dms.get()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("without requireAck a direct message is SENT on RESP_CODE_SENT")
    void directWithoutAck() throws IOException {
        Map<String, Object> c = config(1);
        c.put("dmContactPrefix", "a1b2c3d4e5f6");
        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type().render(fired(), settings(c)).body();
        assertThat(sender.send(channelId, c, mesh, () -> true).body()).isEqualTo("SENT");
    }

    @Test
    @DisplayName("public slots are refused after CMD_GET_CHANNEL: at save (MESH_PUBLIC_CHANNEL) and at send")
    void publicSlots() {
        MeshCoreChannel type = type();
        assertThat(type.validate(config(1), Map.of())).isEmpty();
        for (int slot : List.of(2, 3, 4, 5)) {
            List<FieldError> errors = type.validate(config(slot), Map.of());
            assertThat(errors).extracting(FieldError::field).as("slot " + slot).contains("config.channelIndex");
            assertThat(type.validationCode(config(slot), Map.of())).isEqualTo(ErrorCodes.MESH_PUBLIC_CHANNEL);
        }
        assertThat(type.validationCode(config(0), Map.of())).isEqualTo(ErrorCodes.MESH_PUBLIC_CHANNEL);
        assertThat(received.stream().anyMatch(p -> (p[0] & 0xFF) == MeshCoreProtocol.CMD_GET_CHANNEL)).isTrue();

        PreparedRequest.Mesh mesh = (PreparedRequest.Mesh) type.render(fired(), settings(config(3))).body();
        assertThatThrownBy(() -> sender.send(channelId, config(3), mesh, () -> true))
                .isInstanceOf(IOException.class).hasMessageContaining("not verified private");
        assertThat(lastSent(0x03)).as("nothing was sent on the public slot").isNull();
    }

    @Test
    @DisplayName("slot verdicts; an unreachable companion does not block a save")
    void verdicts() throws IOException {
        assertThat(MeshCoreSender.classify(1, channelInfo(1)).status()).isEqualTo(MeshCoreSlotProbe.Status.PRIVATE);
        assertThat(MeshCoreSender.classify(2, channelInfo(2)).status()).isEqualTo(MeshCoreSlotProbe.Status.PUBLIC);
        assertThat(MeshCoreSender.classify(3, channelInfo(3)).reason()).contains("hashtag");
        assertThat(MeshCoreSender.classify(4, channelInfo(4)).reason()).contains("all-zero");
        assertThat(MeshCoreSender.classify(5, channelInfo(5)).reason()).contains("empty");
        assertThat(MeshCoreSender.classify(1, new byte[] {1, 2}).status())
                .isEqualTo(MeshCoreSlotProbe.Status.UNREACHABLE);
        int port;
        try (ServerSocket closed = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = closed.getLocalPort();
        }
        Map<String, Object> c = config(1);
        c.put("companionUrl", "tcp://127.0.0.1:" + port);
        assertThat(type().validate(c, Map.of())).isEmpty();
    }

    @Test
    @DisplayName("config checks: companion on the LAN, dm-only ack, cbor not with dm, data type range, custom preset")
    void configChecks() {
        Map<String, Object> c = config(1);
        c.put("companionUrl", "tcp://8.8.8.8:5000");
        c.put("requireAck", true);
        c.put("dataType", 0x1234);
        c.remove("airtimeCustom");
        assertThat(type().validate(c, Map.of())).extracting(FieldError::field).contains("config.companionUrl",
                "config.requireAck", "config.dataType", "config.airtimeCustom");
        Map<String, Object> dm = config(1);
        dm.put("dmContactPrefix", "xyz");
        dm.put("profile", "cbor");
        assertThat(type().validate(dm, Map.of())).extracting(FieldError::field).contains("config.dmContactPrefix",
                "config.profile");
    }
}
