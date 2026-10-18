package ai.foresignal.doctor;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.usgs.UsgsEarthquakesAdapter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The packaged doctor resources (prompt 140, task 1): the fixtures and their index in the built resources, truncation
 * keeping JSON valid, the primary-fixture choice, and the adapter source index. Reads what {@code processResources}
 * packaged ({@code packageDoctorResources}); no Spring.
 */
class FixtureCatalogTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    final FixtureCatalog fixtures = new FixtureCatalog();

    @Test
    @DisplayName("the fixture index and the USGS fixtures are packaged, with sizes, SHA-256 and the capture date")
    void packaged() {
        assertThat(fixtures.available()).isTrue();
        assertThat(new ClassPathResource(FixtureCatalog.ROOT + FixtureCatalog.INDEX).exists()).isTrue();
        List<FixtureCatalog.FixtureFile> files = fixtures.files(UsgsEarthquakesAdapter.ID);
        assertThat(files).extracting(FixtureCatalog.FixtureFile::file).contains("all_day.geojson", "README.md");
        FixtureCatalog.FixtureFile allDay = files.stream().filter(f -> f.file().equals("all_day.geojson")).findFirst()
                .orElseThrow();
        assertThat(allDay.sha256()).hasSize(64);
        assertThat(allDay.bytes()).isPositive().isLessThanOrEqualTo(256 * 1024);
        assertThat(allDay.capturedAt()).isEqualTo("2026-10-11");
        assertThat(fixtures.read(UsgsEarthquakesAdapter.ID, "all_day.geojson")).get()
                .satisfies(bytes -> assertThat(bytes.length).isEqualTo((int) allDay.bytes()));
    }

    @Test
    @DisplayName("every packaged file is within the 256 KB cap, and every truncated JSON file still parses")
    void truncationKeepsJsonValid() {
        int truncatedJson = 0;
        for (String source : fixtures.sources()) {
            for (FixtureCatalog.FixtureFile f : fixtures.files(source)) {
                assertThat(f.bytes()).as(source + "/" + f.file()).isLessThanOrEqualTo(256 * 1024);
                if (f.truncated() && (f.file().endsWith(".json") || f.file().endsWith(".geojson"))) {
                    byte[] bytes = fixtures.read(source, f.file()).orElseThrow();
                    JsonNode tree = JSON.readTree(bytes);
                    assertThat(tree).as(source + "/" + f.file()).isNotNull();
                    assertThat(f.originalBytes()).isGreaterThan(f.bytes());
                    truncatedJson++;
                }
            }
        }
        assertThat(truncatedJson).as("at least one fixture over the cap was truncated (for example nws/national.json)")
                .isPositive();
    }

    @Test
    @DisplayName("the primary fixture is the normal capture, not an edge case, a README or a deliberate break")
    void primary() {
        assertThat(fixtures.primary(UsgsEarthquakesAdapter.ID, StructureFormat.JSON))
                .map(FixtureCatalog.FixtureFile::file).contains("all_day.geojson");
        assertThat(FixtureCatalog.isNormal("edge-cases.geojson")).isFalse();
        assertThat(FixtureCatalog.isNormal("not-a-feature-collection.json")).isFalse();
        assertThat(FixtureCatalog.isNormal("all_day.geojson")).isTrue();
        assertThat(fixtures.primary("no-such-source", null)).isEmpty();
    }

    @Test
    @DisplayName("absent-safe: a catalog over a missing folder has no sources and no files")
    void absentSafe() {
        FixtureCatalog missing = new FixtureCatalog("META-INF/foresignal/no-fixtures-here/", JSON);
        assertThat(missing.available()).isFalse();
        assertThat(missing.sources()).isEmpty();
        assertThat(missing.files(UsgsEarthquakesAdapter.ID)).isEmpty();
        assertThat(missing.read(UsgsEarthquakesAdapter.ID, "all_day.geojson")).isEmpty();
        assertThat(missing.read("x", "../secret")).isEmpty();
    }

    @Test
    @DisplayName("the adapter sources are packaged with an index; ported third-party files are left out")
    void adapterSources() {
        AdapterSourceCatalog catalog = new AdapterSourceCatalog(AdapterSourceCatalog.ROOT, JSON,
                id -> Optional.of("usgs"));
        List<AdapterSourceCatalog.SourceFile> files = catalog.files(UsgsEarthquakesAdapter.ID);
        assertThat(files).extracting(AdapterSourceCatalog.SourceFile::path)
                .contains("usgs/UsgsGeoJsonParser.java", "usgs/UsgsEarthquakesAdapter.java")
                .doesNotContain("usgs/package-info.java");
        assertThat(catalog.contains("usgs/UsgsGeoJsonParser.java")).isTrue();
        assertThat(catalog.contains("src/main/java/ai/foresignal/source/adapters/usgs/UsgsGeoJsonParser.java")).isTrue();
        assertThat(catalog.contains("transit/TransitFeeds.java")).as("ported from God's Eye View").isFalse();
        assertThat(AdapterSourceCatalog.packageOf(UsgsEarthquakesAdapter.class)).isEqualTo("usgs");
        assertThat(AdapterSourceCatalog.packageOf(String.class)).isNull();

        AdapterSourceCatalog none = new AdapterSourceCatalog(AdapterSourceCatalog.ROOT, JSON, id -> Optional.empty());
        assertThat(none.files(UsgsEarthquakesAdapter.ID)).isEmpty();
    }
}
