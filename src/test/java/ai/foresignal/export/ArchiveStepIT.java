package ai.foresignal.export;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.TestPartitions;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.apache.parquet.example.data.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The cold archive's retention step (prompt 111): the hive layout and manifests, deletion only after a
 * verified write, a failed write keeping the rows (STANDARD source skipped, TRACK leaf kept), no row
 * archived twice, the catalog and the settings API.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ArchiveStepIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MeterRegistry meters;

    Path root;

    @BeforeEach
    void archiveRoot() throws Exception {
        root = Files.createTempDirectory("foresignal-archive-it");
    }

    @AfterEach
    void cleanUp() {
        ExportStore.deleteTree(root);
    }

    private void enable(String rootPath) throws Exception {
        mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true,\"root\":\"" + rootPath + "\",\"target\":\"local\",\"sources\":[\""
                                + STD + "\",\"" + TRACK + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    private static NormalizedRecord aircraft(String id, Instant at, double lon) {
        return NormalizedRecord.builder(id, TRACK_KIND).observedAt(at).point(lon, 50.0)
                .put("callsign", id.toUpperCase()).put("alt", 30000).put("gs", 400).put("track", 90).build();
    }

    private List<Path> parts(String tier, String sourceId) throws Exception {
        Path dir = root.resolve("record_version/tier=" + tier + "/source_id=" + sourceId);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(p -> p.getFileName().toString().endsWith(".parquet")).sorted().toList();
        }
    }

    /** Versions on two UTC dates and a marker minute, so the cutoff of a run 8 days on lands after the first two. */
    private void seedStandard() {
        ingestAt(STD, T0, point("d1", STD_KIND, T0, 1, 1, 1));
        Instant nextDay = T0.plus(Duration.ofDays(1));
        ingestAt(STD, nextDay, point("d2", STD_KIND, nextDay, 2, 2, 2));
        Instant marker = nextDay.plus(Duration.ofMinutes(1));
        ingestAt(STD, marker, point("marker", STD_KIND, marker, 3, 3, 3));
    }

    @Test
    @DisplayName("STANDARD rows are written to the hive layout with a manifest, verified, then deleted; never twice")
    void standardHiveLayout() throws Exception {
        seedStandard();
        enable(root.toString());
        long d1 = jdbc.queryForObject("SELECT seq FROM record_version WHERE source_id = ? AND external_id = 'd1'",
                Long.class, STD);
        long d2 = jdbc.queryForObject("SELECT seq FROM record_version WHERE source_id = ? AND external_id = 'd2'",
                Long.class, STD);
        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(9)));
        assertThat(result.skippedSources()).doesNotContain(STD);
        assertThat(versions(STD, "d1")).isZero();
        assertThat(versions(STD, "d2")).isZero();
        assertThat(versions(STD, "marker")).isEqualTo(1);
        List<Path> parts = parts("std", STD);
        assertThat(parts).extracting(p -> root.relativize(p).toString()).containsExactly(
                "record_version/tier=std/source_id=" + STD + "/date=2026-03-01/part-" + d1 + "-" + d1 + ".parquet",
                "record_version/tier=std/source_id=" + STD + "/date=2026-03-02/part-" + d2 + "-" + d2 + ".parquet");
        List<Group> rows = GeoParquetTestReader.rows(parts.getFirst());
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getString("external_id", 0)).isEqualTo("d1");
        JsonNode manifest = JSON.readTree(Files.readAllBytes(parts.getFirst().getParent().resolve("_manifest.json")));
        assertThat(manifest.get("rows").asLong()).isEqualTo(1);
        JsonNode file = manifest.get("files").get(0);
        assertThat(file.get("name").asString()).isEqualTo("part-" + d1 + "-" + d1 + ".parquet");
        assertThat(file.get("sha256").asString()).isEqualTo(ExportManifests.sha256(parts.getFirst()));
        assertThat(file.get("local").asBoolean()).isTrue();
        assertThat(manifest.get("licence").get("licences").get(0).get("terms").get("id").asString())
                .isEqualTo("CC-BY-4.0");
        try (Stream<Path> walk = Files.walk(root)) {
            assertThat(walk.filter(p -> p.getFileName().toString().endsWith(".tmp"))).isEmpty();
        }

        runAt(T0.plus(Duration.ofDays(9)).plusSeconds(3600));
        assertThat(parts("std", STD)).as("nothing archived twice").hasSize(2);

        JsonNode catalog = JSON.readTree(mvc.perform(get("/v1/archive/catalog?source=" + STD + "&from=2026-03-02")
                        .header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString());
        assertThat(catalog.get("items")).hasSize(1);
        assertThat(catalog.get("items").get(0).get("date").asString()).isEqualTo("2026-03-02");
        assertThat(catalog.get("items").get(0).get("files").get(0).get("rows").asLong()).isEqualTo(1);
        JsonNode system = JSON.readTree(mvc.perform(get("/v1/system").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString());
        assertThat(system.get("archive").get("enabled").asBoolean()).isTrue();
        assertThat(system.get("archive").get("bytes").asLong()).isPositive();
    }

    @Test
    @DisplayName("a TRACK leaf is archived per source before it is dropped")
    void trackLeaf() throws Exception {
        ingestAt(TRACK, T0, aircraft("a", T0, 1.0));
        jumpSeq(20_000_000L);
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(TRACK, later, aircraft("c", later, 2.0));
        enable(root.toString());
        RetentionRunResult due = runAt(later.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(5)));
        assertThat(due.partitionsDropped()).isGreaterThanOrEqualTo(1);
        assertThat(TestPartitions.leaves(jdbc, HistoryTier.TRACK).stream().map(LeafInfo::name)).doesNotContain("rv_t_0");
        List<Path> parts = parts("trk", TRACK);
        assertThat(parts).hasSize(1);
        assertThat(parts.getFirst().toString()).contains("date=2026-03-01");
        assertThat(GeoParquetFiles.rowCount(parts.getFirst())).isEqualTo(1);
    }

    @Test
    @DisplayName("a failed write keeps the rows and the leaf, alarms, and a later run archives and deletes them")
    void failureKeepsRows() throws Exception {
        ingestAt(TRACK, T0, aircraft("a", T0, 1.0));
        ingestAt(STD, T0, point("d1", STD_KIND, T0, 1, 1, 1));
        jumpSeq(20_000_000L);
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(TRACK, later, aircraft("c", later, 2.0));
        Instant nextDay = T0.plus(Duration.ofDays(1));
        ingestAt(STD, nextDay, point("d2", STD_KIND, nextDay, 2, 2, 2));
        Instant marker = nextDay.plus(Duration.ofMinutes(1));
        ingestAt(STD, marker, point("marker", STD_KIND, marker, 3, 3, 3));
        Files.writeString(root.resolve("record_version"), "a file where the layout needs a directory");
        enable(root.toString());
        double failedBefore = meters.counter(ArchiveStep.FAILED_METRIC).count();
        RetentionRunResult failed = runAt(T0.plus(Duration.ofDays(9)));
        assertThat(failed.skippedSources()).contains(STD);
        assertThat(versions(STD, "d1")).isEqualTo(1);
        assertThat(versions(TRACK, "a")).isEqualTo(1);
        assertThat(TestPartitions.leaves(jdbc, HistoryTier.TRACK).stream().map(LeafInfo::name)).contains("rv_t_0");
        assertThat(meters.counter(ArchiveStep.FAILED_METRIC).count()).isGreaterThan(failedBefore);

        Files.delete(root.resolve("record_version"));
        RetentionRunResult retry = runAt(T0.plus(Duration.ofDays(9)).plusSeconds(3600));
        assertThat(retry.skippedSources()).doesNotContain(STD);
        assertThat(versions(STD, "d1")).isZero();
        assertThat(versions(TRACK, "a")).isZero();
        assertThat(parts("std", STD)).isNotEmpty();
        assertThat(parts("trk", TRACK)).isNotEmpty();
    }

    @Test
    @DisplayName("archive settings: secrets are write-only, a metadata endpoint is URL_NOT_ALLOWED, s3 needs a bucket")
    void settingsApi() throws Exception {
        JsonNode view = JSON.readTree(mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"s3\",\"s3\":{\"endpoint\":\"http://127.0.0.1:9000\",\"bucket\":\"b\","
                                + "\"pathStyle\":true},\"secrets\":{\"accessKey\":\"AK\",\"secretKey\":\"SK\"}}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(view.get("secretsSet").toString()).isEqualTo("[\"accessKey\",\"secretKey\"]");
        assertThat(view.toString()).doesNotContain("\"SK\"").doesNotContain("Cipher");
        String stored = mvc.perform(get("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString();
        assertThat(stored).doesNotContain("SK").contains("\"bucket\":\"b\"");
        mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"s3\":{\"endpoint\":\"http://169.254.169.254\",\"bucket\":\"b\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("URL_NOT_ALLOWED"));
        mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"s3\",\"s3\":null}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"target\":\"ftp\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/archive/catalog").header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isForbidden());
    }
}
