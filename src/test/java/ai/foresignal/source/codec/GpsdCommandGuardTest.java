package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.CommandGuard;
import ai.foresignal.source.spi.SocketOptions;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link GpsdCommandGuard} (prompt 086): only {@code ?WATCH}, {@code ?VERSION}, {@code ?DEVICES} and {@code ?POLL} may be
 * written to gpsd; the codec's own watch commands pass it, and the socket writes nothing after them.
 */
class GpsdCommandGuardTest {

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"?WATCH={\"enable\":true,\"json\":true};\n", "?WATCH={\"enable\":true,\"json\":true,\"raw\":2};\n",
        "?VERSION;", "?DEVICES;\n", "?POLL;\r\n", "?WATCH;", "?VERSION;?DEVICES;?POLL;\n"})
    @DisplayName("allowlisted commands pass")
    void allowed(String command) {
        assertThat(GpsdCommandGuard.INSTANCE.allows(ascii(command))).as(command).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"?DEVICE={\"path\":\"/dev/ttyACM0\",\"bps\":115200};", "?WATCH={\"a\":1};?DEVICE={};",
        "?WATCH={\"enable\":true;}", "?POLL", "POLL;", "?VERSION;\n?POLL;", "?WATCH={\"x\":\"\n\"};", "µb\u0006\u0001;",
        "?WATCH={\"enable\":true}; ", "?SEND=b562;"})
    @DisplayName("anything else is refused: device configuration, smuggled commands, binary, missing terminators")
    void refused(String command) {
        assertThatThrownBy(() -> GpsdCommandGuard.INSTANCE.check(ascii(command)))
                .isInstanceOf(CommandGuard.CommandRefusedException.class);
    }

    @Test
    @DisplayName("empty, null and over-long writes are refused")
    void bounds() {
        assertThat(GpsdCommandGuard.INSTANCE.allows(new byte[0])).isFalse();
        assertThat(GpsdCommandGuard.INSTANCE.allows(null)).isFalse();
        assertThat(GpsdCommandGuard.INSTANCE.allows(ascii("?WATCH={\"" + "x".repeat(300) + "\":1};"))).isFalse();
        assertThat(GpsdCommandGuard.ALLOWED).containsExactly("WATCH", "VERSION", "DEVICES", "POLL");
    }

    @Test
    @DisplayName("the codec's watch commands pass the guard; ubxMonitor adds raw 2; the connection writes only on connect")
    void codecWrites() {
        byte[] plain = GpsdCodec.watchCommand(false);
        byte[] raw = GpsdCodec.watchCommand(true);
        assertThat(new String(plain, StandardCharsets.US_ASCII)).isEqualTo(GpsdCodec.WATCH);
        assertThat(new String(raw, StandardCharsets.US_ASCII)).isEqualTo("?WATCH={\"enable\":true,\"json\":true,\"raw\":2};\n");
        assertThat(GpsdCommandGuard.INSTANCE.allows(plain)).isTrue();
        assertThat(GpsdCommandGuard.INSTANCE.allows(raw)).isTrue();
        assertThat(new GpsdCodec().onConnect()).isEqualTo(plain);
        SocketOptions options = SocketOptions.receiveOnlyAfterConnect().withOnConnect(raw);
        assertThat(options.writeMode()).isEqualTo(SocketOptions.WriteMode.ON_CONNECT_ONLY);
        assertThat(options.isCommandWriter()).isFalse();
    }
}
