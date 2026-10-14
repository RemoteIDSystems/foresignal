package ai.foresignal.source.adapters.submarinecables;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link SubmarineCableParser} contract (prompt 094). */
class SubmarineCableParserTest {

    private final SourceDescriptor descriptor = new SubmarineCablesAdapter("").descriptor();

    @Test
    @DisplayName("cables parse with a prefixed id, a MultiLineString and the isTbd flag")
    void cables() {
        List<NormalizedRecord> records =
                SubmarineCableParser.parseCables(AdapterFixtures.bytes(SubmarineCablesAdapter.ID, "cable-geo.json"));
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("cable:trans-atlantic", "cable:pacific-tbd");
        assertThat(records.get(0).payload()).containsEntry("name", "Trans Atlantic Cable")
                .containsEntry("color", "#ff0000")
                .containsEntry("isTbd", false);
        assertThat(records.get(1).payload()).containsEntry("isTbd", true);
        assertThat(records.get(0).geometry().getGeometryType()).isEqualTo("MultiLineString");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }

    @Test
    @DisplayName("landing points parse to points with a prefixed id")
    void landings() {
        List<NormalizedRecord> records = SubmarineCableParser.parseLandings(
                AdapterFixtures.bytes(SubmarineCablesAdapter.ID, "landing-point-geo.json"));
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("landing:lp-nyc", "landing:lp-london");
        assertThat(records.get(0).kind()).isEqualTo(SubmarineCableParser.LANDING_KIND);
        assertThat(records.get(0).geometry().getGeometryType()).isEqualTo("Point");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }

    @Test
    @DisplayName("the descriptor is non-commercial and local-only")
    void descriptor() {
        assertThat(descriptor.egress()).isEqualTo(ai.foresignal.source.spi.Egress.LOCAL_ONLY);
        assertThat(descriptor.kindPresentation().get(SubmarineCableParser.CABLE_KIND))
                .containsEntry("nonCommercial", true);
        assertThat(descriptor.licence()).contains("non-commercial");
    }
}
