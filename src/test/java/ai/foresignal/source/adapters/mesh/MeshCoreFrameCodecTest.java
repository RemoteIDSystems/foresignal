package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.codec.CodecContext;
import ai.foresignal.source.codec.MeshCoreCompanionCodec;
import ai.foresignal.source.formats.meshcore.MeshCoreFrameDecoder;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.formats.meshcore.MeshCoreProtocol;
import ai.foresignal.source.spi.Framing;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The MeshCore TCP framing (prompt 083): {@link MeshCoreFrameDecoder} reassembles {@code '>'} frames across reads,
 * skips oversize frames and resynchronizes after junk; {@link MeshCoreFrames} builds exactly the commands of
 * {@code commands.hex}; the {@code meshcore-companion} codec maps a stream.
 */
class MeshCoreFrameCodecTest {

    private static final Instant T = Instant.ofEpochSecond(1_791_900_000L);

    private static byte[] stream() {
        return MeshFixtures.hexStream("meshcore-local", "companion-tcp.hex");
    }

    @Test
    @DisplayName("the fixture in one read: 25 frames, one oversize frame skipped, two junk bytes skipped")
    void wholeStream() {
        MeshCoreFrameDecoder decoder = new MeshCoreFrameDecoder();
        List<byte[]> frames = decoder.feed(stream());
        assertThat(frames).hasSize(25);
        assertThat(MeshCoreFrames.code(frames.get(0))).isEqualTo(MeshCoreProtocol.RESP_SELF_INFO);
        assertThat(MeshCoreFrames.code(frames.get(24))).isEqualTo(MeshCoreProtocol.RESP_NO_MORE_MESSAGES);
        assertThat(decoder.oversize()).isEqualTo(1);
        assertThat(decoder.junk()).isEqualTo(2);
        assertThat(decoder.buffered()).isZero();
    }

    @Test
    @DisplayName("reads split anywhere, even byte by byte, give the same frames")
    void byteByByte() {
        byte[] all = stream();
        MeshCoreFrameDecoder whole = new MeshCoreFrameDecoder();
        List<byte[]> expected = whole.feed(all);
        for (int chunk : new int[] {1, 2, 3, 7, 64, 150}) {
            MeshCoreFrameDecoder decoder = new MeshCoreFrameDecoder();
            List<byte[]> frames = new ArrayList<>();
            for (int i = 0; i < all.length; i += chunk) {
                frames.addAll(decoder.feed(Arrays.copyOfRange(all, i, Math.min(all.length, i + chunk))));
            }
            assertThat(frames).as("chunk %d", chunk).hasSameSizeAs(expected);
            for (int i = 0; i < frames.size(); i++) {
                assertThat(frames.get(i)).isEqualTo(expected.get(i));
            }
            assertThat(decoder.oversize()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("a frame over 176 bytes is skipped whole, even when its body arrives later; an empty frame is ignored")
    void oversizeAcrossReads() {
        MeshCoreFrameDecoder decoder = new MeshCoreFrameDecoder();
        byte[] header = {'>', (byte) 200, 0};
        assertThat(decoder.feed(header)).isEmpty();
        assertThat(decoder.feed(new byte[150])).isEmpty();
        assertThat(decoder.feed(new byte[50])).isEmpty();
        assertThat(decoder.feed(new byte[] {'>', 0, 0, '>', 1, 0, 0x0A})).singleElement()
                .satisfies(f -> assertThat(f).containsExactly(0x0A));
        assertThat(decoder.oversize()).isEqualTo(1);
        assertThat(decoder.feed(new byte[] {'>', (byte) 177, 0})).isEmpty();
        assertThat(decoder.oversize()).isEqualTo(2);
    }

    @Test
    @DisplayName("the startup commands are exactly those of commands.hex, each with its '<' envelope")
    void startupCommands() {
        List<byte[]> expected = MeshFixtures.hexBlocks("meshcore-local", "commands.hex");
        List<byte[]> built = new ArrayList<>();
        built.add(MeshCoreFrames.wire(MeshCoreFrames.appStart()));
        built.add(MeshCoreFrames.wire(MeshCoreFrames.deviceQuery()));
        for (int i = 0; i < 8; i++) {
            built.add(MeshCoreFrames.wire(MeshCoreFrames.getChannel(i)));
        }
        built.add(MeshCoreFrames.wire(MeshCoreFrames.getContacts()));
        built.add(MeshCoreFrames.wire(MeshCoreFrames.getBatteryAndStorage()));
        built.add(MeshCoreFrames.wire(MeshCoreFrames.syncNextMessage()));
        for (int i = 0; i < built.size(); i++) {
            assertThat(built.get(i)).as("command %d", i).isEqualTo(expected.get(i));
        }
        byte[] repeaterKey = MeshCoreFrames.advertKey(new MeshCoreFrameDecoder().feed(stream()).get(17));
        assertThat(MeshCoreFrames.wire(MeshCoreFrames.getContactByKey(repeaterKey))).isEqualTo(expected.get(13));
        assertThat(MeshCoreFrames.appStart()).hasSize(18);
        assertThatThrownBy(() -> MeshCoreFrames.getChannel(8)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeshCoreFrames.getContactByKey(new byte[31])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeshCoreFrames.wire(new byte[177])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeshCoreFrames.wire(new byte[0])).isInstanceOf(IllegalArgumentException.class);
        assertThat(MeshCoreFrames.getDeviceTime()).containsExactly(MeshCoreProtocol.CMD_GET_DEVICE_TIME);
    }

    @Test
    @DisplayName("the meshcore-companion codec: RAW framing, records from the stream fed in 13-byte reads")
    void codec() {
        MeshCoreCompanionCodec codec = new MeshCoreCompanionCodec();
        assertThat(codec.id()).isEqualTo("meshcore-companion");
        assertThat(codec.framing()).isEqualTo(Framing.RAW);
        assertThat(codec.onConnect()).isNull();
        MeshCoreCompanionCodec.Session session = (MeshCoreCompanionCodec.Session) codec.newSession(
                CodecContext.of("meshcore-local"));
        byte[] all = stream();
        List<NormalizedRecord> records = new ArrayList<>();
        for (int i = 0; i < all.length; i += 13) {
            records.addAll(session.onFrame(Arrays.copyOfRange(all, i, Math.min(all.length, i + 13)), T));
        }
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly(MeshRecords.NODE_KIND);
        assertThat(records).extracting(NormalizedRecord::externalId).contains("mc:f7ff142f1f28", "mc:7b065359ae9c",
                "mc:5f6a9ca3f1cc", "mc:d104384f64d9", "mc:74979e2bf73d");
        assertThat(session.skipped()).isEqualTo(3);
    }
}
