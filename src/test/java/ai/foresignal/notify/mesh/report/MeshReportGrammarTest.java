package ai.foresignal.notify.mesh.report;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.mesh.report.MeshReportGrammar.Ack;
import ai.foresignal.notify.mesh.report.MeshReportGrammar.Help;
import ai.foresignal.notify.mesh.report.MeshReportGrammar.Invalid;
import ai.foresignal.notify.mesh.report.MeshReportGrammar.Report;
import ai.foresignal.notify.mesh.report.MeshReportGrammar.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The field-report grammar (prompt 084): each command, case-insensitive keywords, coordinates and places, the
 * missing prefix ignored, and malformed input answered with a reason. Sender and channel filtering is
 * {@code MeshReportIngestIT}'s.
 */
class MeshReportGrammarTest {

    @Test
    @DisplayName("#E R with coordinates, with a place and with neither")
    void reports() {
        assertThat(MeshReportGrammar.parse("#E R FIRE 51.48,-0.12 smoke over the ridge"))
                .isEqualTo(new Report("FIRE", 51.48, -0.12, null, "smoke over the ridge"));
        assertThat(MeshReportGrammar.parse("#e r flood @Home-North water over the road"))
                .isEqualTo(new Report("FLOOD", null, null, "home-north", "water over the road"));
        assertThat(MeshReportGrammar.parse("#E R power lines down on Elm St"))
                .isEqualTo(new Report("POWER", null, null, null, "lines down on Elm St"));
        for (String type : MeshReportGrammar.TYPES) {
            assertThat(MeshReportGrammar.parse("#E R " + type.toLowerCase() + " x")).isInstanceOf(Report.class);
        }
        Report longText = (Report) MeshReportGrammar.parse("#E R OTHER " + "a".repeat(300));
        assertThat(longText.text()).hasSize(MeshReportGrammar.MAX_TEXT);
    }

    @Test
    @DisplayName("#E ACK, #E STATUS and #E HELP")
    void commands() {
        assertThat(MeshReportGrammar.parse("#E ACK 7k2q")).isEqualTo(new Ack("7K2Q"));
        assertThat(MeshReportGrammar.parse("#E ack #0007")).isEqualTo(new Ack("0007"));
        assertThat(MeshReportGrammar.parse("#E STATUS")).isEqualTo(new Status());
        assertThat(MeshReportGrammar.parse("  #e help  ")).isEqualTo(new Help());
        assertThat(MeshReportGrammar.HELP.length()).isLessThanOrEqualTo(133);
    }

    @Test
    @DisplayName("without the #E prefix a text is not a command")
    void missingPrefix() {
        assertThat(MeshReportGrammar.parse("R FIRE 51.48,-0.12 smoke")).isNull();
        assertThat(MeshReportGrammar.parse("#EACK 0007")).isNull();
        assertThat(MeshReportGrammar.parse("hello #E STATUS")).isNull();
        assertThat(MeshReportGrammar.parse(null)).isNull();
        assertThat(MeshReportGrammar.parse("#E")).isNull();
    }

    @Test
    @DisplayName("malformed coordinates, places, types and codes answer ERR with a reason")
    void malformed() {
        assertThat(MeshReportGrammar.parse("#E R FIRE 91.0,10 smoke")).isEqualTo(new Invalid("bad coordinates"));
        assertThat(MeshReportGrammar.parse("#E R FIRE 10,200 smoke")).isEqualTo(new Invalid("bad coordinates"));
        assertThat(MeshReportGrammar.parse("#E R FIRE 51.4x,-0.1 smoke")).isEqualTo(new Invalid("bad coordinates"));
        assertThat(MeshReportGrammar.parse("#E R FIRE 51.48,-0.12")).isEqualTo(new Invalid("report text missing"));
        assertThat(MeshReportGrammar.parse("#E R FIRE @bad_place smoke")).isEqualTo(new Invalid("bad place"));
        assertThat(MeshReportGrammar.parse("#E R LAVA hot")).isEqualTo(new Invalid("unknown type LAVA"));
        assertThat(MeshReportGrammar.parse("#E R")).isEqualTo(new Invalid("report type missing"));
        assertThat(MeshReportGrammar.parse("#E ACK 12345")).isEqualTo(new Invalid("bad code"));
        assertThat(MeshReportGrammar.parse("#E STATUS now")).isInstanceOf(Invalid.class);
        assertThat(MeshReportGrammar.parse("#E DROP TABLE")).isInstanceOf(Invalid.class);
    }
}
