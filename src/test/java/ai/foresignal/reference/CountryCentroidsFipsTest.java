package ai.foresignal.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.adapters.AdapterFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prompt 040's FIPS table against the State Department fixture (prompt 090): every FIPS 10-4 code the
 * travel-advisory API carries resolves to a country.
 */
class CountryCentroidsFipsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("every Category in the State advisories fixture resolves through byFips")
    void everyFipsResolves() {
        CountryCentroids centroids = new CountryCentroids();
        JsonNode advisories = JSON.readTree(AdapterFixtures.bytes("travel-advisory-us", "advisories.json"));
        assertThat(advisories.isArray()).isTrue();
        for (JsonNode advisory : advisories) {
            JsonNode category = advisory.get("Category");
            String fips = category.isArray() ? category.get(0).asString() : category.asString();
            assertThat(centroids.byFips(fips)).as("FIPS %s", fips).isPresent();
        }
    }

    @Test
    @DisplayName("Yemen's FIPS code YM resolves to ISO2 YE")
    void yemen() {
        assertThat(new CountryCentroids().byFips("YM")).get()
                .extracting(CountryRef::iso2).isEqualTo("YE");
    }
}
