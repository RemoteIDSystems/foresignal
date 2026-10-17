package ai.foresignal.baserate.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * The UCDP GED importer on a small CSV fixture with the real GED 26.1 header (prompt 121): family mapping, ISO-week
 * bucketing, region mapping (including former names in parentheses and the {@code ZZ} fallback), fatalities, an
 * idempotent re-import, ZIP input, the recorded license and parameter validation.
 */
class UcdpImporterTest {

    @TempDir
    Path data;

    UcdpGedImporter importer;
    ReferenceTestSupport.MemoryWriter writer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        Files.createDirectories(data.resolve("imports"));
        Files.write(data.resolve("imports/ged.csv"), ReferenceTestSupport.fixture("ucdp-ged-sample.csv"));
        ReferenceRegionResolver regions = new ReferenceRegionResolver(new CountryCentroids(), null, null);
        importer = new UcdpGedImporter(regions, Mockito.mock(ObjectProvider.class), Mockito.mock(ObjectProvider.class),
                Mockito.mock(ObjectProvider.class), new ForesignalProperties(data, null, false, null),
                JsonMapper.builder().build());
        writer = new ReferenceTestSupport.MemoryWriter();
    }

    ImportRun run(String file) throws Exception {
        Map<String, Object> params = importer.normalize(new LinkedHashMap<>(Map.of("file", file)));
        ReferenceImportEntity entity = ReferenceTestSupport.entity(UcdpGedImporter.DATASET, params);
        ImportRun run = new ImportRun(entity, writer, null, null);
        importer.run(run);
        return run;
    }

    @Test
    @DisplayName("violence types map to families; events are counted per ISO week (Monday) with best fatalities")
    void familiesAndWeeks() throws Exception {
        run("ged.csv");
        LocalDate week = LocalDate.of(1992, 3, 16);
        assertThat(writer.count(ReferenceFamilies.BATTLES, "DZ", week, ReferenceFamilies.UCDP)).isEqualTo(2L);
        assertThat(writer.row(ReferenceFamilies.BATTLES, "DZ", week, ReferenceFamilies.UCDP)[5]).isEqualTo(5L);
        assertThat(writer.count(ReferenceFamilies.VIOLENCE_CIVILIANS, "DZ", LocalDate.of(1992, 3, 23),
                ReferenceFamilies.UCDP)).isEqualTo(1L);
        assertThat(writer.count(ReferenceFamilies.BATTLES, "DZ", LocalDate.of(1992, 3, 23), ReferenceFamilies.UCDP))
                .isNull();
        // type 2 (non-state) is battles; type 4 is not a GED violence type and is skipped
        assertThat(writer.count(ReferenceFamilies.BATTLES, "YE", LocalDate.of(2015, 3, 30), ReferenceFamilies.UCDP))
                .isEqualTo(1L);
        assertThat(writer.rows.keySet()).noneMatch(k -> k.startsWith("conflict:protests"));
    }

    @Test
    @DisplayName("regions: former names in parentheses resolve; an unknown country without a tagger is ZZ")
    void regions() throws Exception {
        run("ged.csv");
        assertThat(writer.count(ReferenceFamilies.BATTLES, "RU", LocalDate.of(2000, 1, 3), ReferenceFamilies.UCDP))
                .isEqualTo(1L);
        assertThat(writer.count(ReferenceFamilies.BATTLES, "ZZ", LocalDate.of(2015, 3, 30), ReferenceFamilies.UCDP))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("the license is CC-BY-4.0, egress OPEN, and the import records its file hash and period")
    void license() throws Exception {
        ImportRun run = run("ged.csv");
        Object[] row = writer.row(ReferenceFamilies.BATTLES, "DZ", LocalDate.of(1992, 3, 16), ReferenceFamilies.UCDP);
        assertThat(row[6]).isEqualTo("CC-BY-4.0");
        assertThat(row[7]).isEqualTo("OPEN");
        assertThat(run.entity().getFileSha256()).hasSize(64);
        assertThat(run.entity().getPeriodFrom()).isEqualTo(LocalDate.of(1992, 3, 16));
        assertThat(run.entity().getPeriodTo()).isEqualTo(LocalDate.of(2015, 4, 6));
        assertThat(run.entity().getRowsRead()).isEqualTo(7);
    }

    @Test
    @DisplayName("re-importing the same file leaves exactly the same rows")
    void idempotent() throws Exception {
        run("ged.csv");
        Map<String, Object[]> first = new LinkedHashMap<>();
        writer.rows.forEach((k, v) -> first.put(k, v.clone()));
        ImportRun again = run("ged.csv");
        assertThat(writer.rows.keySet()).isEqualTo(first.keySet());
        writer.rows.forEach((k, v) -> assertThat(v[4]).isEqualTo(first.get(k)[4]));
        assertThat((List<?>) again.entity().getProgress().get("done")).hasSize(1);
    }

    @Test
    @DisplayName("the downloaded ZIP gives the same rows as the CSV inside it")
    void zip() throws Exception {
        try (OutputStream out = Files.newOutputStream(data.resolve("imports/ged261-csv.zip"));
                ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("GEDEvent_v26_1.csv"));
            zip.write(ReferenceTestSupport.fixture("ucdp-ged-sample.csv"));
            zip.closeEntry();
        }
        run("ged261-csv.zip");
        assertThat(writer.count(ReferenceFamilies.BATTLES, "DZ", LocalDate.of(1992, 3, 16), ReferenceFamilies.UCDP))
                .isEqualTo(2L);
    }

    @Test
    @DisplayName("parameters: a file is required and stays inside the imports directory; the API needs a version")
    void validation() {
        assertThatThrownBy(() -> importer.normalize(Map.of())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> importer.normalize(Map.of("file", "../secret.key")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> importer.normalize(Map.of("file", "/etc/passwd")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> importer.normalize(Map.of("useApi", true))).isInstanceOf(ValidationException.class);
        assertThat(importer.normalize(Map.of("useApi", true, "version", "26.1", "yearFrom", 2020, "yearTo", 2021)))
                .containsEntry("version", "26.1").containsEntry("yearFrom", 2020);
        assertThat(importer.availability(importer.normalize(Map.of("file", "missing.csv"))).state())
                .isEqualTo(ReferenceImporter.Availability.State.UNAVAILABLE);
        assertThat(importer.availability(importer.normalize(Map.of("file", "ged.csv"))).state())
                .isEqualTo(ReferenceImporter.Availability.State.AVAILABLE);
    }
}
