package ai.foresignal.source.adapters.spaceports;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link GcatParser} contract (prompt 094). */
class GcatParserTest {

    private final SourceDescriptor descriptor = new SpaceportsAdapter("").descriptor();

    @Test
    @DisplayName("sites parse with the # header, - blanks and a point where coordinates exist")
    void sites() {
        List<NormalizedRecord> records =
                GcatParser.parseSites(AdapterFixtures.bytes(SpaceportsAdapter.ID, "sites.tsv"));
        assertThat(records).hasSize(3);
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("site:SITE1", "site:SITE2", "site:SITE3");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord cape = records.get(0);
        assertThat(cape.payload()).containsEntry("name", "Cape Canaveral")
                .containsEntry("code", "CC")
                .containsEntry("type", "LAUNCH")
                .containsEntry("stateCode", "SITE")
                .containsEntry("tStart", "1950")
                .containsEntry("errorKm", 0.5);
        assertThat(cape.payload()).doesNotContainKey("tStop");
        assertThat(cape.geometry()).isNotNull();

        NormalizedRecord unknown = records.get(2);
        assertThat(unknown.geometry()).isNull();
        assertThat(unknown.payload()).doesNotContainKey("parent");
    }

    @Test
    @DisplayName("pads parse with the Pad id column and a pad: externalId")
    void pads() {
        List<NormalizedRecord> records = GcatParser.parsePads(AdapterFixtures.bytes(SpaceportsAdapter.ID, "lp.tsv"));
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("pad:PAD1", "pad:PAD2");
        assertThat(records.get(0).payload()).containsEntry("name", "Pad 39A").containsEntry("parent", "SITE1");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }

    @Test
    @DisplayName("a missing # header is a format error")
    void noHeader() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> GcatParser.parseSites("a\tb\n".getBytes()))
                .isInstanceOf(ai.foresignal.source.spi.UpstreamFormatException.class);
    }
}
