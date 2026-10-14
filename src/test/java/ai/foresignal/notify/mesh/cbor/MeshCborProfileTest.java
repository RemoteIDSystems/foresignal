package ai.foresignal.notify.mesh.cbor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HexFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mesh CBOR profile (prompt 084): golden vectors (checked by hand against RFC 8949: definite-length map
 * {@code 0xa0 + n}, unsigned keys 0–9 as single bytes, minimal integers, definite text strings), the size reduction
 * order (title, then source id), the 160-byte cap, and a decoder that skips unknown integer keys.
 */
class MeshCborProfileTest {

    static final HexFormat HEX = HexFormat.of();

    /** A full alert: M5.1 near Austin, 2026-05-01T12:00:00Z. */
    static final MeshCborMessage FULL = new MeshCborMessage(1, 1, "7K2Q", 1, "earthquake", 3027000, -9774000,
            1777636800L, "Quakes", "usgs-earthquakes");

    /** Its encoding: a 10-member map, keys 0–9 in order. */
    static final String FULL_HEX = "aa" + "0001" + "0101" + "0264374b3251" + "0301" + "046a65617274687175616b65"
            + "051a002e3038" + "063a009523af" + "071a69f495c0" + "08665175616b6573"
            + "0970757367732d65617274687175616b6573";

    /** A minimal resolved message: version, type and code. */
    static final String MIN_HEX = "a3" + "0001" + "0102" + "026430303037";

    @Test
    @DisplayName("golden vectors: a full alert and a minimal resolved message")
    void golden() {
        assertThat(HEX.formatHex(MeshCborProfile.encode(FULL))).isEqualTo(FULL_HEX);
        assertThat(MeshCborProfile.encode(FULL)).hasSize(69);
        MeshCborMessage min = new MeshCborMessage(1, MeshCborProfile.TYPE_RESOLVED, "0007", null, null, null, null,
                null, null, null);
        assertThat(HEX.formatHex(MeshCborProfile.encode(min))).isEqualTo(MIN_HEX);
        assertThat(MeshCborProfile.decode(HEX.parseHex(FULL_HEX))).isEqualTo(FULL);
        assertThat(MeshCborProfile.decode(HEX.parseHex(MIN_HEX))).isEqualTo(min);
    }

    @Test
    @DisplayName("too long: key 8 (title) goes first, then key 9 (source id); never above the cap")
    void reduction() {
        MeshCborMessage big = new MeshCborMessage(1, 1, "ZZZZ", 2, "k".repeat(30), 9000000, -18000000, 4102444800L,
                "T".repeat(80), "s".repeat(40));
        byte[] whole = MeshCborProfile.encode(big);
        assertThat(whole.length).isLessThanOrEqualTo(MeshCborProfile.MAX_BYTES).isEqualTo(139);
        MeshCborMessage decoded = MeshCborProfile.decode(whole);
        assertThat(decoded.kind()).hasSize(MeshCborProfile.MAX_KIND);
        assertThat(decoded.title()).hasSize(MeshCborProfile.MAX_TITLE);
        assertThat(decoded.sourceId()).hasSize(MeshCborProfile.MAX_SOURCE_ID);

        MeshCborMessage noTitle = MeshCborProfile.decode(MeshCborProfile.encode(big, 100));
        assertThat(noTitle.title()).isNull();
        assertThat(noTitle.sourceId()).isNotNull();
        assertThat(MeshCborProfile.encode(big, 100)).hasSize(76);

        MeshCborMessage neither = MeshCborProfile.decode(MeshCborProfile.encode(big, 60));
        assertThat(neither.title()).isNull();
        assertThat(neither.sourceId()).isNull();
        assertThat(neither.shortCode()).isEqualTo("ZZZZ");
        assertThat(MeshCborProfile.encode(big, 60)).hasSize(49);

        assertThatThrownBy(() -> MeshCborProfile.encode(big, 20)).isInstanceOf(IllegalArgumentException.class);
        assertThat(MeshCborProfile.encode(big, 500).length).as("the cap is never above 160")
                .isLessThanOrEqualTo(MeshCborProfile.MAX_BYTES);
    }

    @Test
    @DisplayName("unknown integer keys are skipped (forward compatibility); a non-map or text keys are refused")
    void decoder() {
        // {0: 1, 1: 4, 10: "x", 11: [1, 2], 2: "ABCD"}
        MeshCborMessage m = MeshCborProfile.decode(HEX.parseHex("a5" + "0001" + "0104" + "0a6178" + "0b820102"
                + "026441424344"));
        assertThat(m.type()).isEqualTo(MeshCborProfile.TYPE_STATUS);
        assertThat(m.shortCode()).isEqualTo("ABCD");
        assertThatThrownBy(() -> MeshCborProfile.decode(HEX.parseHex("820102")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeshCborProfile.decode(HEX.parseHex("a1616101")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MeshCborProfile.decode(HEX.parseHex("a10001")))
                .as("missing type").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("severity and coordinate helpers")
    void helpers() {
        assertThat(MeshCborMessage.severity("critical")).isEqualTo(2);
        assertThat(MeshCborMessage.severity("warning")).isEqualTo(1);
        assertThat(MeshCborMessage.severity("info")).isZero();
        assertThat(MeshCborMessage.e5(30.27)).isEqualTo(3027000);
        assertThat(MeshCborMessage.e5(-97.740004)).isEqualTo(-9774000);
    }
}
