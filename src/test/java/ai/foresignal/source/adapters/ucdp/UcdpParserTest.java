package ai.foresignal.source.adapters.ucdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Point;

/**
 * {@link UcdpParser} on the synthetic pages built from the UCDP API docs and the GED codebook 26.1 (prompt 081):
 * violence types, {@code where_prec} to accuracy, country-level (6) and international-waters (7) events, the paging
 * fields, {@code datasetVersion} as a volatile field, the fieldSpecs contract and a stable static hash.
 */
class UcdpParserTest {

    private static final CountryCentroids COUNTRIES = new CountryCentroids();
    private static final UcdpGedAdapter ADAPTER = new UcdpGedAdapter("", COUNTRIES);

    private static UcdpParser.Page page(String file) {
        return UcdpParser.parse(AdapterFixtures.bytes("ucdp-ged", file), "26.0.8", COUNTRIES);
    }

    @Test
    @DisplayName("page 1: two events, TotalCount 3, TotalPages 2, the next page number 2, date_end per id")
    void paging() {
        UcdpParser.Page p = page("page1.json");
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("780001", "780002");
        assertThat(p.totalCount()).isEqualTo(3);
        assertThat(p.totalPages()).isEqualTo(2);
        assertThat(p.nextPage()).isEqualTo(2);
        assertThat(p.dateEnds()).containsEntry("780001", LocalDate.of(2026, 8, 20))
                .containsEntry("780002", LocalDate.of(2026, 8, 31));
        assertThat(page("page2.json").nextPage()).isNull();
    }

    @Test
    @DisplayName("an exact state-based event: point, 1 km, date_start, no observedAt (received), the payload")
    void exact() {
        NormalizedRecord r = page("page1.json").records().getFirst();
        assertThat(r.kind()).isEqualTo("conflict-event");
        assertThat(((Point) r.geometry()).getX()).isEqualTo(36.2311);
        assertThat(r.quality().accuracyMeters()).isEqualTo(1_000.0);
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-08-20T00:00:00Z"));
        assertThat(r.observedAt()).isNull();
        assertThat(r.payload()).containsEntry("conflictName", "Russia - Ukraine")
                .containsEntry("violenceType", "state-based").containsEntry("sideA", "Government of Russia (Soviet Union)")
                .containsEntry("sideB", "Government of Ukraine").containsEntry("country", "Ukraine")
                .containsEntry("region", "Europe").containsEntry("adm1", "Kharkiv oblast")
                .containsEntry("adm2", "Kharkiv raion").containsEntry("deathsBest", 3L).containsEntry("deathsLow", 3L)
                .containsEntry("deathsHigh", 5L).containsEntry("deathsCivilians", 2L).containsEntry("sourceCount", 4L)
                .containsEntry("sourceHeadline", "Shelling hits Kharkiv").containsEntry("datePrecision", 1L)
                .containsEntry("wherePrecision", 1L).containsEntry("datasetVersion", "26.0.8")
                .doesNotContainKey("geometryApproximate");
    }

    @Test
    @DisplayName("where_prec 6: the country's label point, approximate, no accuracy; -1 sources omitted")
    void countryLevel() {
        NormalizedRecord r = page("page1.json").records().get(1);
        var yemen = COUNTRIES.byName("Yemen").orElseThrow();
        Point p = (Point) r.geometry();
        assertThat(p.getX()).isEqualTo(yemen.labelLon());
        assertThat(p.getY()).isEqualTo(yemen.labelLat());
        assertThat(r.quality().accuracyMeters()).isNull();
        assertThat(r.payload()).containsEntry("geometryApproximate", true).containsEntry("violenceType", "one-sided")
                .doesNotContainKeys("sourceCount", "sourceHeadline", "adm1");
    }

    @Test
    @DisplayName("where_prec 7 (international waters or airspace): the coded point, approximate, no accuracy")
    void waters() {
        NormalizedRecord r = page("page2.json").records().getFirst();
        assertThat(((Point) r.geometry()).getY()).isEqualTo(1.5);
        assertThat(r.quality().accuracyMeters()).isNull();
        assertThat(r.payload()).containsEntry("geometryApproximate", true).containsEntry("violenceType", "non-state");
    }

    @ParameterizedTest(name = "type_of_violence {0} -> {1}")
    @CsvSource({"1, state-based", "2, non-state", "3, one-sided"})
    @DisplayName("violence types")
    void violence(long code, String text) {
        assertThat(UcdpParser.violenceType(code)).isEqualTo(text);
    }

    @ParameterizedTest(name = "where_prec {0} -> {1}")
    @CsvSource({"1, 1000.0", "2, 25000.0", "3, 50000.0", "4, 150000.0", "5, 250000.0"})
    @DisplayName("where_prec to accuracy")
    void accuracy(long code, double meters) {
        assertThat(UcdpParser.accuracyMeters(code)).isEqualTo(meters);
    }

    @Test
    @DisplayName("unknown codes, the probe counts, page parameters and format errors")
    void edges() {
        assertThat(UcdpParser.violenceType(9L)).isNull();
        assertThat(UcdpParser.violenceType(null)).isNull();
        assertThat(UcdpParser.accuracyMeters(6L)).isNull();
        assertThat(UcdpParser.accuracyMeters(7L)).isNull();
        assertThat(UcdpParser.totalCount(AdapterFixtures.bytes("ucdp-ged", "probe-hit.json"))).isEqualTo(2817);
        assertThat(UcdpParser.totalCount(AdapterFixtures.bytes("ucdp-ged", "probe-empty.json"))).isZero();
        assertThat(UcdpParser.pageOf("https://x/api/gedevents/26.1?pagesize=1&pagesize=100&page=2")).isEqualTo(2);
        assertThat(UcdpParser.pageOf("https://x/api/gedevents/26.1?pagesize=1")).isNull();
        assertThat(UcdpParser.pageOf("not a url ^")).isNull();
        assertThatThrownBy(() -> UcdpParser.parse("{}".getBytes(StandardCharsets.UTF_8), "26.0.8", COUNTRIES))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> UcdpParser.totalCount(AdapterFixtures.bytes("ucdp-ged", "unauthorized-401.txt")))
                .isInstanceOf(UpstreamFormatException.class);
        String noPosition = "{\"TotalCount\":1,\"TotalPages\":1,\"Result\":[{\"id\":5,\"where_prec\":1}]}";
        assertThat(UcdpParser.parse(noPosition.getBytes(StandardCharsets.UTF_8), "26.0.8", COUNTRIES).skipped())
                .containsEntry(UcdpParser.BAD_POSITION, 1);
    }

    @Test
    @DisplayName("every payload key is declared; the descriptor validates; datasetVersion is volatile; token required")
    void descriptor() {
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), page("page1.json").records());
        FieldSpecContract.assertPayloadKeysDeclared(ADAPTER.descriptor(), page("page2.json").records());
        assertThat(DescriptorValidator.problems(ADAPTER.descriptor(), ADAPTER)).isEmpty();
        assertThat(ADAPTER.descriptor().volatileFields()).containsExactly("datasetVersion");
        assertThat(ADAPTER.descriptor().credentialFields()).extracting(f -> f.name() + ":" + f.required())
                .containsExactly("token:true");
    }

    @Test
    @DisplayName("the same event in two candidate releases is one static content (datasetVersion is volatile)")
    void stableStaticHash() {
        NormalizedRecord a = page("page1.json").records().getFirst();
        NormalizedRecord b = UcdpParser.parse(AdapterFixtures.bytes("ucdp-ged", "page1.json"), "26.0.9", COUNTRIES)
                .records().getFirst();
        SnapshotContract.assertStableStaticHash(ADAPTER.descriptor(), List.of(a, b));
    }
}
