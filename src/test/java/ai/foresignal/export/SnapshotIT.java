package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.apache.parquet.example.data.Group;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code atSeq} snapshots end to end (prompt 111): the state between two revisions is version 1,
 * tombstoned entities are absent, and a state before the retention horizon is {@code 400
 * SNAPSHOT_BEYOND_RETENTION} unless a hold kept the versions it needs.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class SnapshotIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ExportStore store;

    private long seqOf(String externalId, int version) {
        return jdbc.queryForObject("SELECT seq FROM record_version WHERE source_id = ? AND external_id = ? AND version = ?",
                Long.class, STD, externalId, version);
    }

    private MvcResult snapshot(String body) throws Exception {
        return mvc.perform(post("/v1/snapshots").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    private List<Group> rows(MvcResult result) throws Exception {
        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString()).isEqualTo(202);
        JsonNode view = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(view.get("kind").asString()).isEqualTo("SNAPSHOT");
        String id = view.get("id").asString();
        ExportJobEntity job = awaitTerminal(id);
        assertThat(job.getStatus()).as(job.getError()).isEqualTo(ExportJobEntity.DONE);
        assertThat(job.getExpiresAt()).isNull();
        Path file = store.dirOf(job).resolve(id + ".parquet");
        assertThat(store.dirOf(job).toString()).contains("snapshots");
        return GeoParquetTestReader.rows(file);
    }

    @Test
    @DisplayName("a snapshot between two revisions holds version 1; one at the watermark version 2")
    void betweenRevisions() throws Exception {
        ingestAt(STD, T0, point("x", STD_KIND, T0, 10, 50, 1));
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(STD, later, point("x", STD_KIND, later, 10.5, 50.5, 2));
        long s1 = seqOf("x", 1);
        List<Group> first = rows(snapshot("{\"atSeq\":" + s1 + ",\"sources\":[\"" + STD + "\"]}"));
        assertThat(first).hasSize(1);
        assertThat(first.getFirst().getInteger("version", 0)).isEqualTo(1);
        assertThat(first.getFirst().getLong("seq", 0)).isEqualTo(s1);
        List<Group> current = rows(snapshot("{\"sources\":[\"" + STD + "\"]}"));
        assertThat(current).hasSize(1);
        assertThat(current.getFirst().getInteger("version", 0)).isEqualTo(2);
        List<Group> byTime = rows(snapshot("{\"asOf\":\"" + T0.plusSeconds(60) + "\",\"sources\":[\"" + STD + "\"]}"));
        assertThat(byTime.getFirst().getInteger("version", 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("a tombstoned entity is absent from the snapshot")
    void tombstonesDropped() throws Exception {
        ingestAt(STD, T0, point("keep", STD_KIND, T0, 1, 1, 1), point("gone", STD_KIND, T0, 2, 2, 1));
        Instant later = T0.plus(Duration.ofMinutes(5));
        ingestAt(STD, later, NormalizedRecord.tombstone("gone", STD_KIND, later));
        List<Group> rows = rows(snapshot("{\"sources\":[\"" + STD + "\"]}"));
        assertThat(rows).extracting(g -> g.getString("external_id", 0)).containsExactly("keep");
    }

    @Test
    @DisplayName("before the retention horizon a snapshot is 400 unless a hold kept its versions")
    void beyondRetentionUnlessHeld() throws Exception {
        ingestAt(STD, T0, point("x", STD_KIND, T0, 10, 50, 1));
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(STD, later, point("x", STD_KIND, later, 10.5, 50.5, 2));
        long s1 = seqOf("x", 1);
        Instant marker = T0.plus(Duration.ofHours(2));
        ingestAt(STD, marker, point("marker", STD_KIND, marker, 0, 0, 0));
        clock.setInstant(T0.plus(Duration.ofDays(30)));
        mvc.perform(post("/v1/snapshots").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"atSeq\":" + s1 + ",\"sources\":[\"" + STD + "\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SNAPSHOT_BEYOND_RETENTION"))
                .andExpect(jsonPath("$.sourceId").value(STD));

        clock.setInstant(later.plusSeconds(10));
        mvc.perform(post("/v1/holds").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"keep x\",\"reason\":\"review\",\"sources\":[\"" + STD + "\"]}"))
                .andExpect(status().isCreated());
        runAt(T0.plus(Duration.ofDays(30)));
        assertThat(versions(STD, "x")).as("retention deleted the versions").isZero();
        List<Group> held = rows(snapshot("{\"atSeq\":" + s1 + ",\"sources\":[\"" + STD + "\"]}"));
        assertThat(held).hasSize(1);
        assertThat(held.getFirst().getInteger("version", 0)).isEqualTo(1);
        mvc.perform(post("/v1/snapshots").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an atSeq past the watermark and a malformed body are 400")
    void invalid() throws Exception {
        ingestAt(STD, T0, point("x", STD_KIND, T0, 10, 50, 1));
        assertThat(snapshot("{\"atSeq\":999999999,\"sources\":[\"" + STD + "\"]}").getResponse().getStatus()).isEqualTo(400);
        assertThat(snapshot("{\"atSeq\":\"x\"}").getResponse().getStatus()).isEqualTo(400);
        assertThat(snapshot("{\"atSeq\":1,\"asOf\":\"2026-03-01T00:00:00Z\"}").getResponse().getStatus()).isEqualTo(400);
    }
}
