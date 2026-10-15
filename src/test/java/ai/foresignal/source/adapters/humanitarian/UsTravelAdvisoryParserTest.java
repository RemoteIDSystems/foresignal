package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.BundledCountryPolygons;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code travel-advisory-us} parsing (prompt 090): the level regex and the FIPS 10-4 to ISO2 mapping. */
class UsTravelAdvisoryParserTest {

    private static final UsTravelAdvisoryAdapter ADAPTER =
            new UsTravelAdvisoryAdapter("", new CountryCentroids(), new BundledCountryPolygons());

    @Test
    @DisplayName("a title level is read and the FIPS Category resolves to ISO2")
    void advisories() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("travel-advisory-us", "advisories.json"));
        assertThat(records).isNotEmpty();
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isEqualTo("us:SA");
        assertThat(first.payload()).containsEntry("issuer", "US").containsEntry("level", 3);
        assertThat(first.geometry()).isNotNull();
    }

    @Test
    @DisplayName("a title with no level leaves level null; YM resolves to Yemen (YE) and AS to Australia (AU)")
    void noLevel() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("travel-advisory-us", "no-level.json"));
        NormalizedRecord yemen = records.stream().filter(r -> r.externalId().equals("us:YE")).findFirst()
                .orElseThrow();
        assertThat(yemen.payload()).doesNotContainKey("level");
        // Category "AS" is FIPS 10-4 Australia (the API's code set), not ISO2 American Samoa.
        NormalizedRecord level2 = records.stream().filter(r -> r.externalId().equals("us:AU")).findFirst()
                .orElseThrow();
        assertThat(level2.payload()).containsEntry("level", 2);
    }
}
