package ai.foresignal.source.adapters.gdelt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link LastUpdateParser} (prompt 066): the three lines, the file-name check, foreign hosts and the size cap. */
class LastUpdateParserTest {

    private static final String ID = GdeltEventsAdapter.ID;

    @Test
    @DisplayName("the captured file parses into three lines: export, mentions and gkg")
    void threeLines() {
        List<LastUpdateParser.Line> lines = LastUpdateParser.parse(AdapterFixtures.text(ID, "lastupdate.txt"));
        assertThat(lines).extracting(LastUpdateParser.Line::fileName).containsExactly(
                "20261013143000.export.CSV.zip", "20261013143000.mentions.CSV.zip", "20261013143000.gkg.csv.zip");
        assertThat(lines.getFirst().size()).isEqualTo(90443);
        assertThat(lines.getFirst().md5()).isEqualTo("8768bad35cf2f5e245598013a24497d7");

        LastUpdateParser.ExportFile export = LastUpdateParser.export(AdapterFixtures.text(ID, "lastupdate.txt"));
        assertThat(export.fileName()).isEqualTo("20261013143000.export.CSV.zip");
        assertThat(export.entryName()).isEqualTo("20261013143000.export.CSV");
        assertThat(export.size()).isEqualTo(90443);
        assertThat(export.timestamp()).isEqualTo(Instant.parse("2026-10-13T14:30:00Z"));
    }

    @Test
    @DisplayName("only the file name is taken from a URL on a foreign host")
    void foreignHost() {
        LastUpdateParser.ExportFile export =
                LastUpdateParser.export(AdapterFixtures.text(ID, "lastupdate-foreign-host.txt"));
        assertThat(export.fileName()).isEqualTo("20261013143000.export.CSV.zip");
        assertThat(LastUpdateParser.fileName("https://evil.example/a/b/../20261013143000.export.CSV.zip"))
                .isEqualTo("20261013143000.export.CSV.zip");
    }

    @Test
    @DisplayName("the file name must be {14 digits}.export.CSV.zip with a real timestamp")
    void fileName() {
        assertThat(LastUpdateParser.EXPORT_FILE.matcher("20261013143000.export.CSV.zip").matches()).isTrue();
        assertThat(LastUpdateParser.EXPORT_FILE.matcher("2026101314300.export.CSV.zip").matches()).isFalse();
        assertThat(LastUpdateParser.EXPORT_FILE.matcher("20261013143000.export.csv.zip").matches()).isFalse();
        assertThat(LastUpdateParser.EXPORT_FILE.matcher("../20261013143000.export.CSV.zip").matches()).isFalse();
        assertThatThrownBy(() -> LastUpdateParser.export(line("x20261013143000.export.CSV.zip")))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("does not match");
        assertThatThrownBy(() -> LastUpdateParser.export(line("20261013143000.export.CSV.zip?x=.export.CSV.zip")))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.export(line("20261399143000.export.CSV.zip")))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("invalid timestamp");
    }

    @Test
    @DisplayName("a declared size above 50 MB, or of zero, is refused")
    void oversize() {
        String big = (LastUpdateParser.MAX_DECLARED_BYTES + 1) + " 8768bad35cf2f5e245598013a24497d7"
                + " http://data.gdeltproject.org/gdeltv2/20261013143000.export.CSV.zip\n";
        assertThatThrownBy(() -> LastUpdateParser.export(big)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("at most");
        String exact = LastUpdateParser.MAX_DECLARED_BYTES + " 8768bad35cf2f5e245598013a24497d7"
                + " http://data.gdeltproject.org/gdeltv2/20261013143000.export.CSV.zip\n";
        assertThat(LastUpdateParser.export(exact).size()).isEqualTo(LastUpdateParser.MAX_DECLARED_BYTES);
        String zero = "0 8768bad35cf2f5e245598013a24497d7"
                + " http://data.gdeltproject.org/gdeltv2/20261013143000.export.CSV.zip\n";
        assertThatThrownBy(() -> LastUpdateParser.export(zero)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("malformed lines, an empty file and a file without an export line are refused")
    void malformed() {
        assertThatThrownBy(() -> LastUpdateParser.parse("90443 nothex http://x/y.export.CSV.zip"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.parse("90443 8768bad35cf2f5e245598013a24497d7"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.parse("<html>error</html>"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.parse("\n \n")).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.parse(null)).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> LastUpdateParser.export("135393 7e40ab6dc4a8b33ad2e8db4de2ce9dd2"
                + " http://data.gdeltproject.org/gdeltv2/20261013143000.mentions.CSV.zip\n"))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("no export line");
        assertThat(LastUpdateParser.parse("90443 8768BAD35CF2F5E245598013A24497D7 http://h/20261013143000.export.CSV.zip"
                + "\r\n").getFirst().md5()).isEqualTo("8768bad35cf2f5e245598013a24497d7");
    }

    private static String line(String fileName) {
        return "90443 8768bad35cf2f5e245598013a24497d7 http://data.gdeltproject.org/gdeltv2/" + fileName + "\n";
    }
}
