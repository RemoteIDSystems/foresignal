package ai.foresignal.baserate.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.reference.CountryCentroids;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The ACLED history importer (prompt 121): without credentials the import is skipped with {@code credentials absent};
 * with them, event types map to the conflict families, rows are {@code LOCAL_ONLY} under the ACLED license, duplicate
 * events count once and strategic developments are not counted.
 */
class AcledHistoryImporterTest {

    /** A client that serves the fixture page for every request and records the requests. */
    static final class FixtureClient implements AcledHistoryClient {
        final boolean credentials;
        final List<String> requests = new ArrayList<>();

        FixtureClient(boolean credentials) {
            this.credentials = credentials;
        }

        @Override
        public boolean credentialsPresent() {
            return credentials;
        }

        @Override
        public byte[] page(String isoNumeric, LocalDate from, LocalDate to, int page) {
            requests.add(isoNumeric + ":" + from + ":" + to + ":" + page);
            return ReferenceTestSupport.fixture("acled-page.json");
        }
    }

    final ReferenceRegionResolver regions = new ReferenceRegionResolver(new CountryCentroids(), null, null);
    final JsonMapper json = JsonMapper.builder().build();

    @Test
    @DisplayName("without both credentials the import is skipped with the reason 'credentials absent'")
    void skippedWithoutCredentials() {
        AcledHistoryImporter importer = new AcledHistoryImporter(new FixtureClient(false), regions, json);
        Map<String, Object> params = importer.normalize(Map.of("countries", List.of("YE"), "yearFrom", 2023,
                "yearTo", 2023));
        ReferenceImporter.Availability availability = importer.availability(params);
        assertThat(availability.state()).isEqualTo(ReferenceImporter.Availability.State.SKIP);
        assertThat(availability.reason()).isEqualTo(AcledHistoryImporter.CREDENTIALS_ABSENT);
    }

    @Test
    @DisplayName("rows are LOCAL_ONLY under ACLED-EULA, by family and week, one per event")
    void localOnlyRows() throws Exception {
        FixtureClient client = new FixtureClient(true);
        AcledHistoryImporter importer = new AcledHistoryImporter(client, regions, json);
        assertThat(importer.availability(Map.of()).state()).isEqualTo(ReferenceImporter.Availability.State.AVAILABLE);
        Map<String, Object> params = importer.normalize(new LinkedHashMap<>(Map.of("countries", List.of("ye"),
                "yearFrom", 2023, "yearTo", 2023)));
        ReferenceTestSupport.MemoryWriter writer = new ReferenceTestSupport.MemoryWriter();
        ImportRun run = new ImportRun(ReferenceTestSupport.entity(AcledHistoryImporter.DATASET, params), writer, null,
                null);
        importer.run(run);
        assertThat(client.requests).containsExactly("887:2022-12-26:2023-12-31:1");
        LocalDate week = LocalDate.of(2023, 1, 2);
        Object[] battles = writer.row(ReferenceFamilies.BATTLES, "YE", week, ReferenceFamilies.ACLED);
        assertThat(battles[4]).isEqualTo(1L);
        assertThat(battles[5]).isEqualTo(4L);
        assertThat(battles[6]).isEqualTo("ACLED-EULA");
        assertThat(battles[7]).isEqualTo("LOCAL_ONLY");
        assertThat(writer.count(ReferenceFamilies.EXPLOSIONS, "YE", week, ReferenceFamilies.ACLED)).isEqualTo(1L);
        assertThat(writer.count(ReferenceFamilies.PROTESTS, "YE", LocalDate.of(2023, 1, 9), ReferenceFamilies.ACLED))
                .isEqualTo(1L);
        assertThat(writer.rows.values()).allMatch(r -> "LOCAL_ONLY".equals(r[7]));
        assertThat(writer.rows.keySet()).noneMatch(k -> k.contains("strategic"));
        assertThat(run.entity().getProgress().get("done")).isEqualTo(List.of("YE:2023"));
        assertThat(run.entity().getRowsRead()).isEqualTo(5);
    }

    @Test
    @DisplayName("countries are required ISO alpha-2 codes, at most 30")
    void validation() {
        AcledHistoryImporter importer = new AcledHistoryImporter(new FixtureClient(true), regions, json);
        assertThatThrownBy(() -> importer.normalize(Map.of())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> importer.normalize(Map.of("countries", List.of("XX1"))))
                .isInstanceOf(ValidationException.class);
        assertThat(importer.normalize(Map.of("countries", List.of("SD", "YE", "YE"))).get("countries"))
                .isEqualTo(List.of("SD", "YE"));
        assertThat(ReferenceFamilies.EGRESS.get(ReferenceFamilies.ACLED)).isEqualTo("LOCAL_ONLY");
    }
}
