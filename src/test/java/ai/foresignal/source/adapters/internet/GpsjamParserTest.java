package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code gpsjam-daily} parsing (prompt 092): the pctBad formula and the thresholds. */
class GpsjamParserTest {

    private static final GpsjamAdapter ADAPTER = new GpsjamAdapter(null);

    @Test
    @DisplayName("cells above the threshold become jamming-cell records with an H3 polygon")
    void cells() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("gpsjam-daily", "day.csv"),
                new GpsjamAdapter.Manifest("2026-10-13", "adsbexchange"), 2.0);
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("jamming-cell");
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).startsWith("h3:");
        assertThat(first.geometry()).isNotNull();
        assertThat(first.payload()).containsKeys("date", "pctBad", "level", "goodAircraft", "badAircraft");
    }

    @Test
    @DisplayName("a high bad fraction is 'high', a low one below the threshold is dropped")
    void thresholds() {
        java.nio.charset.Charset utf8 = java.nio.charset.StandardCharsets.UTF_8;
        String csv = "hex,count_good_aircraft,count_bad_aircraft\n"
                + "8400cd7ffffffff,1,20\n"     // pctBad = 100*19/21 = 90.5 -> high
                + "8401227ffffffff,50,1\n";    // pctBad = 0 -> dropped
        List<NormalizedRecord> records = ADAPTER.parse(csv.getBytes(utf8),
                new GpsjamAdapter.Manifest("2026-10-13", null), 2.0);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).payload()).containsEntry("level", "high");
    }
}
