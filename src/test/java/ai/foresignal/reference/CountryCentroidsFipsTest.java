package ai.foresignal.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.humanitarian.TravelAdvisoryCodes;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prompt 040's FIPS table and prompt 097's travel-advisory code set against the State Department
 * fixture (prompt 090): every {@code Category} the travel-advisory API carries resolves to a country,
 * and the FIPS 10-4 vs ISO 3166-1 alpha-2 collision cases resolve to the country the API means.
 */
class CountryCentroidsFipsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    @DisplayName("every Category in the State advisories fixture resolves through TravelAdvisoryCodes")
    void everyCategoryResolves() {
        CountryCentroids centroids = new CountryCentroids();
        JsonNode advisories = JSON.readTree(AdapterFixtures.bytes("travel-advisory-us", "advisories.json"));
        assertThat(advisories.isArray()).isTrue();
        for (JsonNode advisory : advisories) {
            JsonNode category = advisory.get("Category");
            String fips = category.isArray() ? category.get(0).asString() : category.asString();
            assertThat(TravelAdvisoryCodes.resolve(centroids, fips)).as("Category %s", fips).isPresent();
        }
    }

    @Test
    @DisplayName("the collision cases resolve to the country the API means, not the other code set")
    void collisions() {
        CountryCentroids centroids = new CountryCentroids();
        // AS is FIPS Australia; the ISO2 AS is American Samoa.
        assertThat(TravelAdvisoryCodes.resolve(centroids, "AS")).get()
                .extracting(CountryRef::iso2).isEqualTo("AU");
        // YM is FIPS Yemen; there is no ISO2 YM.
        assertThat(TravelAdvisoryCodes.resolve(centroids, "YM")).get()
                .extracting(CountryRef::iso2).isEqualTo("YE");
        // KH is the API's ISO2 Cambodia; its FIPS code is CB.
        assertThat(TravelAdvisoryCodes.resolve(centroids, "KH")).get()
                .extracting(CountryRef::iso2).isEqualTo("KH");
        assertThat(TravelAdvisoryCodes.resolve(centroids, "CB")).get()
                .extracting(CountryRef::iso2).isEqualTo("KH");
        // CH is FIPS China; the ISO2 CH is Switzerland.
        assertThat(TravelAdvisoryCodes.resolve(centroids, "CH")).get()
                .extracting(CountryRef::iso2).isEqualTo("CN");
        // The API sends SR for Switzerland; FIPS SR is Suriname.
        assertThat(TravelAdvisoryCodes.resolve(centroids, "SR")).get()
                .extracting(CountryRef::iso2).isEqualTo("CH");
    }

    @Test
    @DisplayName("Yemen's FIPS code YM resolves to ISO2 YE")
    void yemen() {
        assertThat(new CountryCentroids().byFips("YM")).get()
                .extracting(CountryRef::iso2).isEqualTo("YE");
    }
}
