package ai.foresignal.source.adapters.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.formats.meshcore.MeshCoreCommandGuard;
import ai.foresignal.source.formats.meshcore.MeshCoreFrames;
import ai.foresignal.source.spi.CommandGuard;
import ai.foresignal.source.spi.SocketOptions;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link MeshCoreCommandGuard} (prompt 083): every non-allowlisted command code throws, the never-list can never be
 * registered, only 0x02, 0x03 and 0x3E can be, and the envelope must be exact.
 */
class MeshCoreCommandGuardTest {

    private static final Set<Integer> READS = Set.of(0x01, 0x16, 0x04, 0x0A, 0x05, 0x14, 0x1E, 0x1F);

    @Test
    @DisplayName("of all 256 codes only the eight read commands pass a fresh guard")
    void everyOtherCodeThrows() {
        MeshCoreCommandGuard guard = new MeshCoreCommandGuard();
        for (int code = 0; code < 256; code++) {
            byte[] frame = MeshCoreFrames.wire(new byte[] {(byte) code, 0});
            if (READS.contains(code)) {
                assertThatCode(() -> guard.check(frame)).as("0x%02x", code).doesNotThrowAnyException();
            } else {
                int c = code;
                assertThatThrownBy(() -> guard.check(frame)).as("0x%02x", c)
                        .isInstanceOf(CommandGuard.CommandRefusedException.class);
            }
        }
        assertThat(guard.allowed()).containsExactlyInAnyOrderElementsOf(READS);
    }

    @Test
    @DisplayName("set, key, reboot, factory reset and remote requests are never registrable; 084's sends are")
    void registration() {
        MeshCoreCommandGuard guard = new MeshCoreCommandGuard();
        for (int code : new int[] {0x06, 0x08, 0x0B, 0x0C, 0x13, 0x17, 0x18, 0x1A, 0x1B, 0x20, 0x27, 0x33, 0x34, 0x01}) {
            assertThatThrownBy(() -> guard.allowSend(code)).as("0x%02x", code)
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(MeshCoreCommandGuard.FORBIDDEN).contains(0x13, 0x33, 0x17, 0x18, 0x1A, 0x1B, 0x27, 0x34);
        assertThat(MeshCoreCommandGuard.FORBIDDEN).doesNotContainAnyElementsOf(MeshCoreCommandGuard.READ_COMMANDS);
        assertThat(MeshCoreCommandGuard.FORBIDDEN).doesNotContainAnyElementsOf(MeshCoreCommandGuard.REGISTRABLE_SENDS);
        assertThatThrownBy(() -> guard.check(MeshCoreFrames.wire(new byte[] {0x03, 0, 1}))).isInstanceOf(
                CommandGuard.CommandRefusedException.class);
        guard.allowSend(0x03);
        assertThatCode(() -> guard.check(MeshCoreFrames.wire(new byte[] {0x03, 0, 1}))).doesNotThrowAnyException();
        assertThat(guard.allows(0x3E)).isFalse();
        guard.allowSend(0x3E);
        guard.allowSend(0x02);
        assertThat(guard.allowed()).contains(0x02, 0x03, 0x3E);
    }

    @Test
    @DisplayName("the envelope: '<', an exact length, at most 176 bytes")
    void envelope() {
        MeshCoreCommandGuard guard = new MeshCoreCommandGuard();
        assertThatThrownBy(() -> guard.check(null)).isInstanceOf(CommandGuard.CommandRefusedException.class);
        assertThatThrownBy(() -> guard.check(new byte[] {'<', 1, 0})).isInstanceOf(CommandGuard.CommandRefusedException.class);
        assertThatThrownBy(() -> guard.check(new byte[] {'>', 1, 0, 0x01})).isInstanceOf(
                CommandGuard.CommandRefusedException.class);
        assertThatThrownBy(() -> guard.check(new byte[] {'<', 2, 0, 0x01})).isInstanceOf(
                CommandGuard.CommandRefusedException.class);
        byte[] big = new byte[3 + 177];
        big[0] = '<';
        big[1] = (byte) 177;
        big[3] = 0x01;
        assertThatThrownBy(() -> guard.check(big)).isInstanceOf(CommandGuard.CommandRefusedException.class);
        assertThatCode(() -> guard.check(MeshCoreFrames.wire(MeshCoreFrames.appStart()))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SocketOptions.commandWriter needs a guard and refuses on-connect bytes; other modes refuse a guard")
    void socketOptions() {
        MeshCoreCommandGuard guard = new MeshCoreCommandGuard();
        SocketOptions options = SocketOptions.commandWriter(guard);
        assertThat(options.isCommandWriter()).isTrue();
        assertThat(options.isReceiveOnly()).isFalse();
        assertThat(options.commandGuard()).isSameAs(guard);
        assertThat(options.withSilenceTimeout(java.time.Duration.ofMinutes(15)).commandGuard()).isSameAs(guard);
        assertThatThrownBy(() -> options.withOnConnect(new byte[] {1})).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> SocketOptions.commandWriter(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SocketOptions(null, null, SocketOptions.WriteMode.GUARDED_COMMANDS, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SocketOptions(null, null, SocketOptions.WriteMode.NONE, guard))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(SocketOptions.receiveOnly().commandGuard()).isNull();
        assertThat(options.toString()).contains("MeshCoreCommandGuard");
    }
}
