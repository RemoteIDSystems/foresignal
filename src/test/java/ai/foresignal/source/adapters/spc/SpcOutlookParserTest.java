package ai.foresignal.source.adapters.spc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * The SPC outlook parser against the 17 layers captured 2026-10-12/13 (see the fixture README).
 */
class SpcOutlookParserTest {

    static final Instant NOW = Instant.parse("2026-10-13T00:05:00Z");

    static byte[] layer(String file) {
        return AdapterFixtures.bytes("spc", file);
    }

    @Test
    @DisplayName("ids are day{d}:{type}:{LABEL}; ISSUE, VALID and EXPIRE are observedAt, eventTime and validUntil")
    void idsAndTimes() {
        SpcOutlookParser.ParseResult result = SpcOutlookParser.parse(layer("day2otlk_cat.lyr.geojson"), 2, "cat", NOW);
        assertThat(result.ids()).containsExactly("day2:cat:TSTM", "day2:cat:MRGL", "day2:cat:SLGT");
        NormalizedRecord slight = result.records().get(2);
        assertThat(slight.observedAt()).isEqualTo(Instant.parse("2026-10-12T16:58:00Z"));
        assertThat(slight.eventTime()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(slight.validUntil()).isEqualTo(Instant.parse("2026-10-14T12:00:00Z"));
        assertThat(slight.payload()).containsEntry("day", 2L).containsEntry("type", "cat").containsEntry("label", "SLGT")
                .containsEntry("dn", 4L).containsEntry("issue", "2026-10-12T16:58:00Z")
                .containsEntry("valid", "2026-10-13T12:00:00Z").containsEntry("expire", "2026-10-14T12:00:00Z")
                .containsEntry("title", "Day 2 Slight Risk");
        assertThat(slight.geometry().getGeometryType()).isEqualTo("MultiPolygon");
    }

    @Test
    @DisplayName("probability layers keep their label as the id part and LABEL2 as the title")
    void probabilities() {
        SpcOutlookParser.ParseResult wind = SpcOutlookParser.parse(layer("day2otlk_wind.lyr.geojson"), 2, "wind", NOW);
        assertThat(wind.ids()).containsExactly("day2:wind:0.05", "day2:wind:0.15");
        assertThat(wind.records().get(1).payload()).containsEntry("dn", 15L).containsEntry("title", "Day 2 15% Wind Risk");
    }

    @Test
    @DisplayName("an empty placeholder layer has no records; an expired leftover is skipped")
    void placeholdersAndLeftovers() {
        SpcOutlookParser.ParseResult torn = SpcOutlookParser.parse(layer("day1otlk_torn.lyr.geojson"), 1, "torn", NOW);
        assertThat(torn.records()).isEmpty();
        assertThat(torn.diagnostics().empty()).isEqualTo(1);
        // day3 sigprob still carried a March 2026 polygon on 2026-10-12.
        SpcOutlookParser.ParseResult sigprob = SpcOutlookParser.parse(layer("day3otlk_sigprob.lyr.geojson"), 3, "sigprob", NOW);
        assertThat(sigprob.records()).isEmpty();
        assertThat(sigprob.diagnostics().expired()).isEqualTo(1);
        SpcOutlookParser.ParseResult then = SpcOutlookParser.parse(layer("day3otlk_sigprob.lyr.geojson"), 3, "sigprob",
                Instant.parse("2026-03-05T00:00:00Z"));
        assertThat(then.ids()).containsExactly("day3:sigprob:SIGN");
    }

    @Test
    @DisplayName("the day-3 layer set is cat, prob and sigprob; days 1 and 2 have seven layers each")
    void layerSet() {
        assertThat(SpcOutlooksAdapter.LAYERS).hasSize(17);
        assertThat(SpcOutlooksAdapter.LAYERS.stream().filter(l -> l.day() == 3).map(SpcOutlooksAdapter.Layer::type))
                .containsExactly("cat", "prob", "sigprob");
        assertThat(SpcOutlooksAdapter.LAYERS.stream().filter(l -> l.day() == 1).map(SpcOutlooksAdapter.Layer::type))
                .containsExactly("cat", "torn", "wind", "hail", "sigtorn", "sigwind", "sighail");
        assertThat(new SpcOutlooksAdapter.Layer(3, "prob").file()).isEqualTo("day3otlk_prob.lyr.geojson");
    }

    @Test
    @DisplayName("every captured layer parses and every payload key is declared")
    void everyLayer() {
        var descriptor = new SpcOutlooksAdapter("").descriptor();
        for (SpcOutlooksAdapter.Layer l : SpcOutlooksAdapter.LAYERS) {
            SpcOutlookParser.ParseResult result = SpcOutlookParser.parse(layer(l.file()), l.day(), l.type(), NOW);
            FieldSpecContract.assertPayloadKeysDeclared(descriptor, result.records());
        }
    }

    @Test
    @DisplayName("ISSUE/VALID/EXPIRE are yyyyMMddHHmm UTC")
    void stamps() {
        JsonNodeFactory f = JsonNodeFactory.instance;
        assertThat(SpcOutlookParser.stamp(f.stringNode("202610121948"))).isEqualTo(Instant.parse("2026-10-12T19:48:00Z"));
        assertThat(SpcOutlookParser.stamp(f.stringNode("2026-10-12"))).isNull();
        assertThat(SpcOutlookParser.stamp(null)).isNull();
    }

    @Test
    @DisplayName("two features with one label are merged into one record")
    void duplicateLabels() {
        String json = """
                {"type":"FeatureCollection","features":[
                 {"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-97,31],[-96,31],[-96,32],[-97,31]]]},
                  "properties":{"DN":4,"LABEL":"SLGT","LABEL2":"Slight Risk","ISSUE":"202610121948","VALID":"202610122000","EXPIRE":"202610131200"}},
                 {"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[-90,31],[-89,31],[-89,32],[-90,31]]]},
                  "properties":{"DN":4,"LABEL":"SLGT","LABEL2":"Slight Risk","ISSUE":"202610121948","VALID":"202610122000","EXPIRE":"202610131200"}}]}""";
        List<NormalizedRecord> records = SpcOutlookParser.parse(json.getBytes(StandardCharsets.UTF_8), 1, "cat", NOW).records();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).geometry().getNumGeometries()).isEqualTo(2);
    }

    @Test
    @DisplayName("a body that is not a FeatureCollection is a format error")
    void formatError() {
        assertThatThrownBy(() -> SpcOutlookParser.parse("<html>".getBytes(StandardCharsets.UTF_8), 1, "cat", NOW))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
