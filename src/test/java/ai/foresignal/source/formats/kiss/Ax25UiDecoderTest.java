package ai.foresignal.source.formats.kiss;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Ax25UiDecoder} (prompt 082): UI frames to TNC2 text; non-UI and non-AX.25 frames are told apart. */
class Ax25UiDecoderTest {

    @Test
    @DisplayName("a UI frame becomes SRC>DEST,PATH:info with SSIDs and the last repeated digipeater marked")
    void uiToTnc2() {
        byte[] frame = Ax25UiDecoder.encode("N8QH-9", "SWSPQT", List.of("WA6TOW-2", "WIDE1*", "WIDE2-2"),
                "`28ol 6Y/]=".getBytes(StandardCharsets.US_ASCII));

        Ax25UiDecoder.Result result = Ax25UiDecoder.decode(frame);

        assertThat(result).isInstanceOf(Ax25UiDecoder.Result.Ui.class);
        Ax25UiDecoder.Result.Ui ui = (Ax25UiDecoder.Result.Ui) result;
        assertThat(ui.source()).isEqualTo("N8QH-9");
        assertThat(ui.destination()).isEqualTo("SWSPQT");
        assertThat(ui.digipeaters()).containsExactly("WA6TOW-2", "WIDE1*", "WIDE2-2");
        assertThat(ui.tnc2()).isEqualTo("N8QH-9>SWSPQT,WA6TOW-2,WIDE1*,WIDE2-2:`28ol 6Y/]=");
    }

    @Test
    @DisplayName("a frame without digipeaters and SSID 0 renders without a path or -0")
    void noPath() {
        byte[] frame = Ax25UiDecoder.encode("N0CALL", "APRS", List.of(), ">hi".getBytes(StandardCharsets.US_ASCII));

        assertThat(((Ax25UiDecoder.Result.Ui) Ax25UiDecoder.decode(frame)).tnc2()).isEqualTo("N0CALL>APRS:>hi");
    }

    @Test
    @DisplayName("the hand-checked bytes of a UI frame decode (shifted ASCII, SSID bits, extension bit)")
    void knownBytes() {
        byte[] frame = HexFormat.ofDelimiter(" ").parseHex("82 a0 a4 a6 40 40 60 9c 60 86 82 98 98 72 ae 92 88 8a 62 40"
                + " 63 03 f0 3e 68 69");

        assertThat(((Ax25UiDecoder.Result.Ui) Ax25UiDecoder.decode(frame)).tnc2()).isEqualTo("N0CALL-9>APRS,WIDE1-1:>hi");
    }

    @Test
    @DisplayName("connected-mode and other non-UI frames are NotUi, never text")
    void notUi() {
        byte[] sabm = HexFormat.ofDelimiter(" ").parseHex("9c 60 86 82 98 98 6c 9c 60 86 82 98 98 69 3f");
        byte[] wrongPid = Ax25UiDecoder.encode("N0CALL", "APRS", List.of(), new byte[] {1});
        wrongPid[15] = (byte) 0xCF;

        assertThat(Ax25UiDecoder.decode(sabm)).isEqualTo(new Ax25UiDecoder.Result.NotUi(0x3F, -1));
        assertThat(Ax25UiDecoder.decode(wrongPid)).isEqualTo(new Ax25UiDecoder.Result.NotUi(0x03, 0xCF));
    }

    @Test
    @DisplayName("bytes that are not AX.25 (a MeshCore KISS modem's packet, garbage, a short frame) are NotAx25")
    void notAx25() {
        byte[] meshcore = HexFormat.of().parseHex("11c35a0b7f00e1d2c3b4a5968778695a4b3c2d1e0f");
        byte[] noExtension = new byte[80];
        for (int i = 0; i < noExtension.length; i++) {
            noExtension[i] = (byte) ('A' << 1);
        }

        assertThat(Ax25UiDecoder.decode(meshcore)).isInstanceOf(Ax25UiDecoder.Result.NotAx25.class);
        assertThat(Ax25UiDecoder.decode(noExtension)).isInstanceOf(Ax25UiDecoder.Result.NotAx25.class);
        assertThat(Ax25UiDecoder.decode(new byte[5])).isInstanceOf(Ax25UiDecoder.Result.NotAx25.class);
        assertThat(Ax25UiDecoder.decode(null)).isInstanceOf(Ax25UiDecoder.Result.NotAx25.class);
    }
}
