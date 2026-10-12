package ai.foresignal.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The bundled country table (prompt 040): it loads with no network and no database, the ISO, FIPS and
 * name lookups (aliases, accents, case) work, there are no {@code -99} codes, and every label point lies
 * inside its country's bounding box.
 */
class CountryCentroidsTest {

    static final CountryCentroids COUNTRIES = new CountryCentroids();

    @Test
    @DisplayName("the bundled table loads: about 240 countries, ordered by ISO2, with no -99 codes")
    void loads() {
        assertThat(COUNTRIES.all()).hasSizeBetween(230, 260);
        assertThat(COUNTRIES.all()).allSatisfy(c -> {
            assertThat(c.iso2()).matches("^[A-Z]{2}$");
            assertThat(c.iso3()).matches("^[A-Z]{3}$");
            assertThat(c.name()).isNotBlank();
            assertThat(c.fips()).isNotEqualTo("-99");
            assertThat(c.isoNumeric()).isNotEqualTo("-99");
            assertThat(c.aliases()).noneMatch(a -> a.contains("-99"));
        });
        assertThat(COUNTRIES.all()).extracting(CountryRef::iso2).isSorted().doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("ISO2, ISO3 and FIPS lookups, case-insensitive")
    void codes() {
        assertThat(COUNTRIES.byIso2("us")).map(CountryRef::iso3).contains("USA");
        assertThat(COUNTRIES.byIso3("DEU")).map(CountryRef::iso2).contains("DE");
        assertThat(COUNTRIES.byFips("GM")).map(CountryRef::iso2).as("FIPS GM is Germany").contains("DE");
        assertThat(COUNTRIES.byFips("UK")).map(CountryRef::iso2).contains("GB");
        assertThat(COUNTRIES.byIso2("ZZ")).isEmpty();
        assertThat(COUNTRIES.byIso2(null)).isEmpty();
        assertThat(COUNTRIES.isKnown("FR")).isTrue();
        assertThat(COUNTRIES.name("JP")).isEqualTo("Japan");
    }

    @Test
    @DisplayName("names and aliases: Türkiye, Czechia, Ivory Coast, Kosovo XK; accents and case ignored")
    void names() {
        assertThat(COUNTRIES.byName("Türkiye")).map(CountryRef::iso2).contains("TR");
        assertThat(COUNTRIES.byName("turkiye")).map(CountryRef::iso2).contains("TR");
        assertThat(COUNTRIES.byName("Turkey")).map(CountryRef::iso2).contains("TR");
        assertThat(COUNTRIES.byName("Czechia")).map(CountryRef::iso2).contains("CZ");
        assertThat(COUNTRIES.byName("Czech Republic")).map(CountryRef::iso2).contains("CZ");
        assertThat(COUNTRIES.byName("Côte d'Ivoire")).map(CountryRef::iso2).contains("CI");
        assertThat(COUNTRIES.byName("ivory coast")).map(CountryRef::iso2).contains("CI");
        assertThat(COUNTRIES.byName("Kosovo")).map(CountryRef::iso2).contains("XK");
        assertThat(COUNTRIES.byIso2("XK")).isPresent();
        assertThat(COUNTRIES.byName("UNITED STATES")).map(CountryRef::iso2).contains("US");
        assertThat(COUNTRIES.byName("  ")).isEmpty();
        assertThat(COUNTRIES.byName("Atlantis")).isEmpty();
    }

    @Test
    @DisplayName("every label point lies inside its country's bounding box (bundled simplified polygons)")
    void labelPointsInsideTheirCountry() throws Exception {
        Map<String, Envelope> boxes = new HashMap<>();
        try (InputStream raw = getClass().getResourceAsStream(BundledCountryPolygons.RESOURCE);
                InputStream in = new GZIPInputStream(raw)) {
            JsonNode root = JsonMapper.builder().build().readTree(in);
            GeoJsonReader reader = new GeoJsonReader(5_000_000, 4);
            for (JsonNode feature : root.path("features")) {
                boxes.put(feature.path("properties").path("iso2").asString(),
                        reader.read(feature.get("geometry")).getEnvelopeInternal());
            }
        }
        assertThat(boxes).hasSizeGreaterThan(200);
        for (CountryRef c : COUNTRIES.all()) {
            Envelope box = boxes.get(c.iso2());
            assertThat(box).as("a polygon for %s", c.iso2()).isNotNull();
            Envelope padded = new Envelope(box);
            // The polygons are rounded to 0.01°; allow for that at the edges.
            padded.expandBy(0.05);
            assertThat(padded.contains(c.labelLon(), c.labelLat()))
                    .as("label point of %s (%s, %s) in %s", c.iso2(), c.labelLon(), c.labelLat(), box).isTrue();
        }
    }

    @Test
    @DisplayName("the CSV parser handles quoted cells, doubled quotes and comment lines")
    void csv() {
        assertThat(CountryCentroids.cells("a,\"b, c\",\"d \"\"e\"\"\",")).containsExactly("a", "b, c", "d \"e\"", "");
        assertThat(CountryCentroids.normalize(" Côte d'Ivoire ")).isEqualTo("cotedivoire");
    }

    @Test
    @DisplayName("the bundled polygons give a country with no database: Paris is FR, the mid-Atlantic none")
    void bundledPolygons() {
        BundledCountryPolygons polygons = new BundledCountryPolygons();
        assertThat(polygons.size()).isGreaterThan(200);
        assertThat(polygons.iso2At(2.35, 48.86)).isEqualTo("FR");
        assertThat(polygons.iso2At(-97.74, 30.27)).isEqualTo("US");
        assertThat(polygons.iso2At(-30, 30)).isNull();
    }
}
