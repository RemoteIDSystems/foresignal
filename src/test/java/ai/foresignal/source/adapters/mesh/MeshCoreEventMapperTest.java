package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.NormalizedRecord;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MeshCoreEventMapper} (prompt 083) over the synthetic companion stream: contact decoding (×1e-6 coordinates,
 * NUL-padded names), the companion's own node, the advert push and its re-query as a live advert, channel names without
 * secrets, text on the bus and as records only with {@code includeText}, and RECEIVED times.
 */
class MeshCoreEventMapperTest {

    private static final Instant T = Instant.ofEpochSecond(1_791_900_000L);

    private static List<byte[]> frames() {
        return new MeshCoreFrameDecoder().feed(MeshFixtures.hexStream("meshcore-local", "companion-tcp.hex"));
    }

    private static MeshCoreEventMapper.Mapped all(MeshCoreEventMapper mapper) {
        List<NormalizedRecord> records = new ArrayList<>();
        List<MeshTextMessage> messages = new ArrayList<>();
        for (byte[] frame : frames()) {
            MeshCoreEventMapper.Mapped mapped = mapper.onFrame(frame, T);
            records.addAll(mapped.records());
            messages.addAll(mapped.messages());
        }
        return new MeshCoreEventMapper.Mapped(records, messages);
    }

    @Test
    @DisplayName("a contact frame: key, type, NUL-padded name, ×1e-6 coordinates, timestamps")
    void contactDecode() {
        MeshCoreFrames.Contact hiker = MeshCoreFrames.contact(frames().get(11));
        assertThat(hiker.publicKey()).hasSize(64).startsWith("7b065359ae9c");
        assertThat(hiker.nodeId()).isEqualTo("mc:7b065359ae9c");
        assertThat(hiker.type()).isEqualTo(MeshCoreProtocol.ADV_TYPE_CHAT);
        assertThat(hiker.name()).isEqualTo("Synthetic Hiker");
        assertThat(hiker.latitude()).isEqualTo(37.7801);
        assertThat(hiker.longitude()).isEqualTo(-122.4302);
        assertThat(hiker.lastAdvert()).isEqualTo(T.minusSeconds(600));
        assertThat(hiker.lastModified()).isEqualTo(T.minusSeconds(590));
        assertThat(hiker.outPathLength()).isEqualTo(2);
        MeshCoreFrames.Contact repeater = MeshCoreFrames.contact(frames().get(12));
        assertThat(repeater.latitude()).as("0,0 is no position").isNull();
        assertThat(MeshCoreProtocol.nodeType(repeater.type())).isEqualTo("repeater");
        assertThat(MeshCoreFrames.contact(new byte[] {0x03, 1, 2})).isNull();
        assertThat(MeshCoreFrames.contact(frames().get(0))).isNull();
    }

    @Test
    @DisplayName("the stream: the companion, three contacts, the repeater re-query and a new sensor; RECEIVED times")
    void stream() {
        MeshCoreEventMapper.Mapped mapped = all(new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults()));
        assertThat(mapped.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(MeshRecords.NODE_KIND);
            assertThat(r.observedAt()).isNull();
            assertThat(r.payload()).containsEntry("network", "meshcore");
            assertThat(r.observers()).extracting(o -> o.receiverId()).containsExactly("mc:f7ff142f1f28");
        });
        NormalizedRecord companion = last(mapped.records(), "mc:f7ff142f1f28");
        assertThat(companion.payload()).containsEntry("longName", "Synthetic Companion").containsEntry("nodeType", "chat")
                .containsEntry("hwModel", "Heltec V3").containsEntry("firmware", "v1.12.0").containsEntry("voltage", 4.012);
        assertThat(companion.geometry().getCoordinate().y).isEqualTo(37.7749);

        NormalizedRecord room = last(mapped.records(), "mc:d104384f64d9");
        assertThat(room.payload()).containsEntry("nodeType", "room").containsEntry("advertTimestamp",
                T.minusSeconds(3600).toString()).doesNotContainKey("lastAdvertAt").doesNotContainKey("advertStatus");

        NormalizedRecord repeater = last(mapped.records(), "mc:5f6a9ca3f1cc");
        assertThat(repeater.payload()).as("re-queried after PUSH_CODE_ADVERT: a live advert")
                .containsEntry("lastAdvertAt", T.toString()).containsEntry("advertStatus", RepeaterHealth.OK)
                .containsEntry("advertTimestamp", T.plusSeconds(60).toString());
        assertThat(repeater.geometry()).isNull();

        NormalizedRecord sensor = last(mapped.records(), "mc:74979e2bf73d");
        assertThat(sensor.payload()).containsEntry("nodeType", "sensor").containsEntry("lastAdvertAt", T.toString());
        assertThat(sensor.geometry().getCoordinate().x).isEqualTo(-122.41);
    }

    @Test
    @DisplayName("a contact-list repeater gets its status from the advert timestamp: 14 h is late")
    void listRepeaterLate() {
        MeshCoreEventMapper mapper = new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults());
        List<byte[]> frames = frames();
        mapper.onFrame(frames.get(0), T);
        NormalizedRecord repeater = mapper.onFrame(frames.get(12), T).records().get(0);
        assertThat(repeater.payload()).containsEntry("advertStatus", RepeaterHealth.LATE);
    }

    @Test
    @DisplayName("channel names are remembered; the channel secret never reaches a record or message")
    void channelNamesWithoutSecrets() {
        MeshCoreEventMapper mapper = new MeshCoreEventMapper("meshcore-local", true, RepeaterHealth.defaults());
        MeshCoreEventMapper.Mapped mapped = all(mapper);
        assertThat(mapper.channelName(0)).isEqualTo("Public");
        assertThat(mapper.channelName(1)).isEqualTo("Ops");
        assertThat(mapper.channelName(2)).isNull();
        String everything = mapped.toString().toLowerCase();
        assertThat(everything).doesNotContain("8b3387e9c5cdea6ac9e5edbaa115cd72");
        assertThat(MeshCoreFrames.channelName(frames().get(2))).isEqualTo(new MeshCoreFrames.ChannelName(0, "Public"));
    }

    @Test
    @DisplayName("messages: always on the bus; records only with includeText, with stable ids and channel names")
    void messages() {
        MeshCoreEventMapper.Mapped without = all(new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults()));
        assertThat(without.messages()).hasSize(2);
        assertThat(without.records()).noneMatch(r -> r.kind().equals(MeshRecords.MESSAGE_KIND));

        MeshCoreEventMapper.Mapped with = all(new MeshCoreEventMapper("meshcore-local", true, RepeaterHealth.defaults()));
        List<NormalizedRecord> texts = with.records().stream().filter(r -> r.kind().equals(MeshRecords.MESSAGE_KIND))
                .toList();
        assertThat(texts).hasSize(2);
        MeshTextMessage channel = with.messages().get(0);
        assertThat(channel.channelIndex()).isEqualTo(1);
        assertThat(channel.channelName()).isEqualTo("Ops");
        assertThat(channel.fromId()).isNull();
        assertThat(channel.text()).isEqualTo("Synthetic Hiker: #E synthetic report");
        assertThat(channel.snr()).isEqualTo(6.25);
        MeshTextMessage direct = with.messages().get(1);
        assertThat(direct.direct()).isTrue();
        assertThat(direct.fromId()).isEqualTo("mc:7b065359ae9c");
        assertThat(direct.toId()).isEqualTo("mc:f7ff142f1f28");
        assertThat(direct.snr()).isEqualTo(-2.5);
        assertThat(texts.get(0).externalId()).startsWith("mc:ch1:1791900120:");
        assertThat(texts.get(1).externalId()).startsWith("mc:7b065359ae9c:1791900130:");
        assertThat(texts.get(1).payload()).containsEntry("text", "synthetic direct").containsEntry("fromId",
                "mc:7b065359ae9c");
        assertThat(texts).allSatisfy(r -> assertThat(r.observedAt()).isNull());
    }

    @Test
    @DisplayName("text frames: standard and V3 layouts, signed text skips its signature; short frames are null")
    void textLayouts() {
        byte[] standard = concat(new byte[] {0x07}, HexFormat.of().parseHex("a1b2c3d4e5f6"), new byte[] {0, 2},
                le(1_791_900_000), new byte[] {9, 9, 9, 9}, "signed".getBytes(StandardCharsets.UTF_8));
        MeshCoreFrames.TextMessage signed = MeshCoreFrames.textMessage(standard);
        assertThat(signed.text()).isEqualTo("signed");
        assertThat(signed.fromKeyPrefix()).isEqualTo("a1b2c3d4e5f6");
        assertThat(signed.snr()).isNull();
        byte[] channel = concat(new byte[] {0x08, 3, 0, 0}, le(1_791_900_001), "hi".getBytes(StandardCharsets.UTF_8));
        assertThat(MeshCoreFrames.textMessage(channel).channelIndex()).isEqualTo(3);
        assertThat(MeshCoreFrames.textMessage(new byte[] {0x11, 1})).isNull();
        assertThat(MeshCoreFrames.textMessage(new byte[] {0x05})).isNull();
        assertThat(MeshCoreFrames.batteryMillivolts(new byte[] {0x0C, (byte) 0xAC, 0x0F})).isEqualTo(4012);
        assertThat(MeshCoreFrames.deviceInfo(new byte[] {0x0D, 2})).isEqualTo(new MeshCoreFrames.DeviceInfo(2, null, null));
    }

    @Test
    @DisplayName("device info and battery before self info produce nothing (the companion's id is unknown yet)")
    void ownFramesNeedSelf() {
        MeshCoreEventMapper mapper = new MeshCoreEventMapper("meshcore-local", false, RepeaterHealth.defaults());
        assertThat(mapper.onFrame(frames().get(1), T).records()).isEmpty();
        assertThat(mapper.onFrame(frames().get(15), T).records()).isEmpty();
        assertThat(mapper.selfId()).isNull();
        assertThat(mapper.onFrame(new byte[0], T)).isEqualTo(MeshCoreEventMapper.Mapped.NONE);
        assertThat(mapper.onFrame(new byte[] {(byte) 0x88, 1, 2}, T)).isEqualTo(MeshCoreEventMapper.Mapped.NONE);
        mapper.onFrame(frames().get(0), T.plus(Duration.ofSeconds(1)));
        assertThat(mapper.selfId()).isEqualTo("mc:f7ff142f1f28");
    }

    private static NormalizedRecord last(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).reduce((a, b) -> b).orElseThrow();
    }

    private static byte[] le(long v) {
        return new byte[] {(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)};
    }

    private static byte[] concat(byte[]... parts) {
        int size = 0;
        for (byte[] p : parts) {
            size += p.length;
        }
        byte[] out = new byte[size];
        int at = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, at, p.length);
            at += p.length;
        }
        return out;
    }
}
