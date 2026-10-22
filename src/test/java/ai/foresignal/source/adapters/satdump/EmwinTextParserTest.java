package ai.foresignal.source.adapters.satdump;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.satdump.SatDumpFiles.FormatException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The EMWIN text parser (prompt 107). */
class EmwinTextParserTest {

    private static final String TORNADO = "WUUS54 KFWD 152300\n"
            + "TORFWD\n"
            + "BULLETIN - EAS ACTIVATION REQUESTED\n"
            + "Tornado Warning\n"
            + "National Weather Service Fort Worth TX\n"
            + "...\n"
            + "/O.NEW.KFWD.TO.W.0042.261010T2130Z-261010T2215Z/\n"
            + "\n"
            + "LAT...LON 3250 09730 3240 09720 3260 09710 3250 09730\n"
            + "TIME...MOT...LOC 2130Z 250DEG 24KT 3250 09730\n";

    @Test
    @DisplayName("reads the WMO heading, AWIPS id, P-VTEC and polygon")
    void parse() throws FormatException {
        EmwinTextParser.Parsed parsed = EmwinTextParser.parse(TORNADO.getBytes(StandardCharsets.UTF_8));
        assertThat(parsed.wmoHeading()).isEqualTo("WUUS54 KFWD 152300");
        assertThat(parsed.awipsId()).isEqualTo("TORFWD");
        assertThat(parsed.category()).isEqualTo("TOR");
        assertThat(parsed.office()).isEqualTo("FWD");
        assertThat(parsed.warning()).isNotNull();
        EmwinTextParser.Warning w = parsed.warning();
        assertThat(w.action()).isEqualTo("NEW");
        assertThat(w.externalId()).isEqualTo("KFWD.TO.W.0042");
        assertThat(w.etnNumber()).isEqualTo(42);
        assertThat(w.validFrom()).isEqualTo(Instant.parse("2026-10-10T21:30:00Z"));
        assertThat(w.validUntil()).isEqualTo(Instant.parse("2026-10-10T22:15:00Z"));
        assertThat(w.canceled()).isFalse();
        assertThat(w.polygon()).isNotNull();
        assertThat(w.polygon().getCoordinates()[0].x).isEqualTo(-97.30);
        assertThat(w.polygon().getCoordinates()[0].y).isEqualTo(32.50);
    }

    @Test
    @DisplayName("LAT...LON pairs are latitude first; longitudes are west, and five digits mean 100 degrees or more")
    void latLonOrder() {
        var polygon = EmwinTextParser.polygon("LAT...LON 3250 09730 3260 10123\n      3240 10110 3250 09730\n$$");
        assertThat(polygon).isNotNull();
        var c = polygon.getCoordinates();
        assertThat(c[0].x).isEqualTo(-97.30);
        assertThat(c[0].y).isEqualTo(32.50);
        assertThat(c[1].x).isEqualTo(-101.23);
        assertThat(c[1].y).isEqualTo(32.60);
        assertThat(c[2].x).isEqualTo(-101.10);
        assertThat(c[2].y).isEqualTo(32.40);
        assertThat(polygon.getEnvelopeInternal().getMinY()).isGreaterThan(30);
        assertThat(EmwinTextParser.polygon("LAT...LON 9999 09730 3260 09720 3240 09710")).isNull();
    }

    @Test
    @DisplayName("TIME...MOT...LOC is found anywhere in the text")
    void timeMotLoc() {
        assertThat(EmwinTextParser.timeMotLoc(TORNADO)).containsExactly(2130, 250, 24);
        assertThat(EmwinTextParser.timeMotLoc("no motion here")).isNull();
    }

    @Test
    @DisplayName("the issue time comes from the WMO heading, resolved against now, BBB suffix allowed")
    void issuedAt() throws FormatException {
        Instant now = Instant.parse("2026-10-16T00:10:00Z");
        assertThat(EmwinTextParser.issuedAt("WUUS54 KFWD 152300", now)).isEqualTo(Instant.parse("2026-10-15T23:00:00Z"));
        assertThat(EmwinTextParser.issuedAt("WUUS54 KFWD 160005 CCA", now)).isEqualTo(Instant.parse("2026-10-16T00:05:00Z"));
        // Day 31 read on the 1st belongs to the previous month.
        assertThat(EmwinTextParser.issuedAt("WUUS54 KFWD 312350", Instant.parse("2026-11-01T00:10:00Z")))
                .isEqualTo(Instant.parse("2026-10-31T23:50:00Z"));
        assertThat(EmwinTextParser.issuedAt("not a heading", now)).isNull();
        assertThat(EmwinTextParser.issuedAt(null, now)).isNull();
        String corrected = TORNADO.replace("WUUS54 KFWD 152300", "WUUS54 KFWD 152300 CCA");
        assertThat(EmwinTextParser.parse(corrected.getBytes(StandardCharsets.UTF_8)).wmoHeading())
                .isEqualTo("WUUS54 KFWD 152300 CCA");
    }

    @Test
    @DisplayName("a CAN action is a cancellation")
    void canceled() throws FormatException {
        String can = TORNADO.replace("/O.NEW.", "/O.CAN.");
        EmwinTextParser.Warning w = EmwinTextParser.parse(can.getBytes(StandardCharsets.UTF_8)).warning();
        assertThat(w.canceled()).isTrue();
    }

    @Test
    @DisplayName("unwraps a ZIP, guarding traversal and the entry count")
    void zip() throws Exception {
        byte[] zip = zip("product.txt", TORNADO);
        EmwinTextParser.Parsed parsed = EmwinTextParser.parse(zip);
        assertThat(parsed.awipsId()).isEqualTo("TORFWD");

        assertThatThrownBy(() -> EmwinTextParser.unwrap(zip("../evil.txt", TORNADO)))
                .isInstanceOf(FormatException.class);
        ByteArrayOutputStream many = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(many)) {
            for (int i = 0; i < 20; i++) {
                out.putNextEntry(new ZipEntry("p" + i + ".txt"));
                out.write("x".getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        assertThatThrownBy(() -> EmwinTextParser.unwrap(many.toByteArray())).isInstanceOf(FormatException.class);
    }

    @Test
    @DisplayName("caps the text and strips control characters")
    void cap() throws FormatException {
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            big.append("A line here\n");
        }
        EmwinTextParser.Parsed parsed = EmwinTextParser.parse(big.toString().getBytes(StandardCharsets.UTF_8));
        assertThat(parsed.text().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(EmwinTextParser.MAX_TEXT_BYTES);
    }

    @Test
    @DisplayName("category filtering is case-insensitive")
    void filter() {
        assertThat(EmwinTextParser.categoryAllowed("TOR", List.of("TOR", "SVR"))).isTrue();
        assertThat(EmwinTextParser.categoryAllowed("tor", List.of("TOR"))).isTrue();
        assertThat(EmwinTextParser.categoryAllowed("XYZ", List.of("TOR"))).isFalse();
    }

    private static byte[] zip(String name, String content) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            out.putNextEntry(new ZipEntry(name));
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        return bytes.toByteArray();
    }
}
