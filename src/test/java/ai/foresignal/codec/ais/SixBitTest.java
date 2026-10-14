package ai.foresignal.codec.ais;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The six-bit armoring, bit fields and text of {@link SixBit} (prompt 085). */
class SixBitTest {

    @Test
    @DisplayName("armoring maps ASCII 48-87 to 0-39 and 96-119 to 40-63, both ways, and refuses the gap")
    void armoring() {
        assertThat(SixBit.value('0')).isZero();
        assertThat(SixBit.value('W')).isEqualTo(39);
        assertThat(SixBit.value('`')).isEqualTo(40);
        assertThat(SixBit.value('w')).isEqualTo(63);
        for (int v = 0; v < 64; v++) {
            assertThat(SixBit.value(SixBit.character(v))).isEqualTo(v);
        }
        assertThatThrownBy(() -> SixBit.value('X')).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SixBit.value('_')).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SixBit.character(64)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("unsigned and signed fields read back what was written, across character boundaries")
    void fields() {
        SixBit.Armored armored = new SixBit.Writer().unsigned(6, 1).unsigned(2, 3).unsigned(30, 257_123_450)
                .signed(8, -15).signed(28, -74_100_000).bool(true).unsigned(9, 511).armor();

        SixBit.Reader r = SixBit.decode(armored);
        assertThat(r.uint(6)).isEqualTo(1);
        assertThat(r.uint(2)).isEqualTo(3);
        assertThat(r.uint(30)).isEqualTo(257_123_450);
        assertThat(r.sint(8)).isEqualTo(-15);
        assertThat(r.sint(28)).isEqualTo(-74_100_000);
        assertThat(r.bool()).isTrue();
        assertThat(r.uint(9)).isEqualTo(511);
        assertThat(r.remaining()).isZero();
    }

    @Test
    @DisplayName("fill bits pad the last character and are excluded from the length")
    void fillBits() {
        SixBit.Armored armored = new SixBit.Writer().unsigned(8, 0xFF).armor();
        assertThat(armored.payload()).hasSize(2);
        assertThat(armored.fillBits()).isEqualTo(4);
        assertThat(armored.bitLength()).isEqualTo(8);
        assertThat(SixBit.decode(armored).length()).isEqualTo(8);
        assertThatThrownBy(() -> new SixBit.Armored("0", 6)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("reading past the end fails; peekOrZero gives 0 for an optional trailing field")
    void bounds() {
        SixBit.Reader r = SixBit.decode("w", 0);
        assertThat(r.peekOrZero(6)).isEqualTo(63);
        assertThat(r.uint(6)).isEqualTo(63);
        assertThat(r.peekOrZero(1)).isZero();
        assertThatThrownBy(() -> r.uint(1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("text is upper-cased and @-padded when written, cut at the first @ and stripped when read")
    void text() {
        SixBit.Armored armored = new SixBit.Writer().text(20, "nordic star").text(7, null).text(3, " A~").armor();
        SixBit.Reader r = SixBit.decode(armored);
        assertThat(r.text(20)).isEqualTo("NORDIC STAR");
        assertThat(r.text(7)).as("all @ is absent").isNull();
        assertThat(r.text(3)).as("an unknown character becomes ?").isEqualTo("A?");
    }
}
