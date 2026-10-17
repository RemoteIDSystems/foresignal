package ai.foresignal.baserate.reference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.InstanceOfAssertFactories;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The USGS ComCat importer against WireMock (prompt 121): one FDSN query per year chunk, a refused (too large) year
 * split in two by time with duplicates removed, the cumulative magnitude families, non-earthquake rows skipped and the
 * offshore rule (nearest country within 200 km, else {@code ZZ}).
 */
class UsgsComCatImporterTest {

    static final String PATH = "/fdsnws/event/1/query";

    WireMockServer server;
    UsgsComCatImporter importer;
    ReferenceTestSupport.MemoryWriter writer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        server = new WireMockServer(wireMockConfig().dynamicPort().gzipDisabled(true));
        server.start();
        RegionTagger tagger = Mockito.mock(RegionTagger.class);
        Mockito.when(tagger.tag(anyDouble(), anyDouble())).thenAnswer(inv -> {
            double lon = inv.getArgument(0);
            if (lon > 60 && lon < 75) {
                return new RegionTags("AF", null, null, null, null, "Afghanistan", null, null);
            }
            if (lon > 129 && lon < 142) {
                return new RegionTags("JP", null, null, null, null, "Japan", null, null);
            }
            return RegionTags.EMPTY;
        });
        ReferenceRegionResolver regions = new ReferenceRegionResolver(new CountryCentroids(), tagger, null) {
            @Override
            String nearestCountry(double lon, double lat) {
                // Off Honshu (145E, 35N) is within 200 km of Japan; the East Pacific Rise is not near any country.
                return lon > 142 && lon < 147 && lat > 33 && lat < 38 ? "JP" : ReferenceRegionResolver.UNKNOWN;
            }
        };
        importer = new UsgsComCatImporter(regions, Mockito.mock(ObjectProvider.class), server.baseUrl() + PATH);
        writer = new ReferenceTestSupport.MemoryWriter();
        server.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("starttime", equalTo("2022-12-26T00:00:00Z"))
                .withQueryParam("endtime", equalTo("2024-01-01T00:00:00Z"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/csv")
                        .withBody(ReferenceTestSupport.fixture("usgs-2023.csv"))));
        server.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("starttime", equalTo("2024-01-01T00:00:00Z"))
                .withQueryParam("endtime", equalTo("2024-12-30T00:00:00Z"))
                .willReturn(aResponse().withStatus(400).withBody("Error 400: Bad Request\n\n"
                        + "22000 matching events exceeds search limit of 20000.")));
        server.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("starttime", equalTo("2024-01-01T00:00:00Z"))
                .withQueryParam("endtime", equalTo("2024-07-01T00:00:00Z"))
                .willReturn(aResponse().withStatus(200).withBody(ReferenceTestSupport.fixture("usgs-2024-h1.csv"))));
        server.stubFor(get(urlPathEqualTo(PATH)).withQueryParam("starttime", equalTo("2024-07-01T00:00:00Z"))
                .withQueryParam("endtime", equalTo("2024-12-30T00:00:00Z"))
                .willReturn(aResponse().withStatus(200).withBody(ReferenceTestSupport.fixture("usgs-2024-h2.csv"))));
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    ImportRun run() throws Exception {
        Map<String, Object> params = importer.normalize(new LinkedHashMap<>(Map.of("yearFrom", 2023, "yearTo", 2024)));
        ImportRun run = new ImportRun(ReferenceTestSupport.entity(UsgsComCatImporter.DATASET, params), writer, null,
                null);
        importer.run(run, ReferenceTestSupport.fetcher());
        return run;
    }

    @Test
    @DisplayName("one query per year with format=csv, minmagnitude, time ordering and the 20,000 limit")
    void yearlyPaging() throws Exception {
        ImportRun run = run();
        server.verify(getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("format", equalTo("csv"))
                .withQueryParam("minmagnitude", equalTo("4.5")).withQueryParam("orderby", equalTo("time-asc"))
                .withQueryParam("limit", equalTo("20000")).withQueryParam("starttime",
                        equalTo("2022-12-26T00:00:00Z")));
        assertThat(run.entity().getProgress().get("done")).asInstanceOf(InstanceOfAssertFactories.LIST)
                .containsExactly("2023", "2024");
        assertThat(run.entity().getPeriodFrom()).isEqualTo(LocalDate.of(2022, 12, 26));
        assertThat(run.entity().getPeriodTo()).isEqualTo(LocalDate.of(2024, 12, 30));
    }

    @Test
    @DisplayName("a year refused as too large is split by time; an event in both halves counts once")
    void splitAndDeduplicate() throws Exception {
        run();
        server.verify(getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("endtime", equalTo("2024-07-01T00:00:00Z")));
        assertThat(writer.count("seismic:m6", "JP", LocalDate.of(2024, 1, 1), ReferenceFamilies.USGS)).isEqualTo(1L);
        assertThat(writer.count("seismic:m6", "JP", LocalDate.of(2024, 8, 5), ReferenceFamilies.USGS)).isEqualTo(1L);
    }

    @Test
    @DisplayName("magnitude families are cumulative; a quarry blast is not an earthquake")
    void magnitudeFamilies() throws Exception {
        run();
        LocalDate week = LocalDate.of(2023, 1, 2);
        assertThat(writer.count("seismic:m4.5", "AF", week, ReferenceFamilies.USGS)).isEqualTo(2L);
        assertThat(writer.count("seismic:m5", "AF", week, ReferenceFamilies.USGS)).isEqualTo(1L);
        assertThat(writer.count("seismic:m6", "AF", week, ReferenceFamilies.USGS)).isEqualTo(1L);
        assertThat(writer.row("seismic:m4.5", "AF", week, ReferenceFamilies.USGS)[6]).isEqualTo("public-domain");
    }

    @Test
    @DisplayName("offshore: the nearest country within 200 km, else ZZ")
    void offshore() throws Exception {
        run();
        assertThat(writer.count("seismic:m5", "JP", LocalDate.of(2023, 1, 30), ReferenceFamilies.USGS)).isEqualTo(1L);
        assertThat(writer.count("seismic:m5", "ZZ", LocalDate.of(2023, 2, 27), ReferenceFamilies.USGS)).isEqualTo(1L);
        assertThat(ReferenceRegionResolver.OFFSHORE_KM).isEqualTo(200);
    }

    @Test
    @DisplayName("a resumed import skips the years already done")
    void resume() throws Exception {
        Map<String, Object> params = importer.normalize(new LinkedHashMap<>(Map.of("yearFrom", 2023, "yearTo", 2024)));
        ReferenceImportEntity entity = ReferenceTestSupport.entity(UsgsComCatImporter.DATASET, params);
        entity.setProgress(Map.of("done", java.util.List.of("2023")));
        importer.run(new ImportRun(entity, writer, null, null), ReferenceTestSupport.fetcher());
        server.verify(0, getRequestedFor(urlPathEqualTo(PATH)).withQueryParam("starttime",
                equalTo("2022-12-26T00:00:00Z")));
        assertThat(writer.count("seismic:m4.5", "AF", LocalDate.of(2023, 1, 2), ReferenceFamilies.USGS)).isNull();
    }
}
