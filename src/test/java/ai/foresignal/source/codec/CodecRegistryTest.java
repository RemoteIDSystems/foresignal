package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.Framing;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link CodecRegistry} (prompt 069): lookup by id, and startup failure on a duplicate or malformed id. */
class CodecRegistryTest {

    private static SocketCodec named(String id) {
        return new SocketCodec() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public Framing framing() {
                return Framing.RAW;
            }

            @Override
            public CodecSession newSession(CodecContext context) {
                return (frame, at) -> List.of();
            }
        };
    }

    @Test
    @DisplayName("codecs are found by id and listed in id order")
    void lookup() {
        CodecRegistry registry = new CodecRegistry(List.of(new Sbs1Codec(), new GpsdCodec()));
        assertThat(registry.require("sbs1")).isInstanceOf(Sbs1Codec.class);
        assertThat(registry.find("aivdm")).isEmpty();
        assertThat(registry.all()).extracting(SocketCodec::id).containsExactly("gpsd", "sbs1");
        assertThatThrownBy(() -> registry.require("aivdm")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("a duplicate or malformed id fails startup")
    void invalid() {
        assertThatThrownBy(() -> new CodecRegistry(List.of(named("x"), named("x")))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CodecRegistry(List.of(named("Bad_Id")))).isInstanceOf(IllegalStateException.class);
    }
}
