package ai.foresignal.export;

import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Archive-then-delete for holds (prompt 111): held rows survive a TRACK leaf drop and a STANDARD
 * chunk delete in {@code record_version_held}, retention still drops and deletes and {@code floorSeq}
 * is unchanged; a failed copy keeps the rows (the STANDARD source is skipped, the TRACK leaf kept);
 * the API's limits, listing, versions and deletion.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class HoldCopyStepIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static NormalizedRecord aircraft(String id, Instant at, double lon) {
        return NormalizedRecord.builder(id, TRACK_KIND).observedAt(at).point(lon, 50.0)
                .put("callsign", id.toUpperCase()).put("alt", 30000).put("gs", 400).put("track", 90).build();
    }

    private String hold(String body) throws Exception {
        String response = mvc.perform(post("/v1/holds").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JSON.readTree(response).get("id").asString();
    }

    private long held(String sourceId) {
        return jdbc.queryForObject("SELECT count(*) FROM record_version_held WHERE source_id = ?", Long.class, sourceId);
    }

    private List<String> leafNames() {
        return TestPartitions.leaves(jdbc, HistoryTier.TRACK).stream().map(LeafInfo::name).toList();
    }

    /** Old STANDARD versions at T0 and a marker minute after, so cutoffSeq(now − 7 d) lands between them. */
    private void seedStandard() {
        ingestAt(STD, T0, point("old1", STD_KIND, T0, 1, 1, 1), point("old2", STD_KIND, T0, 2, 2, 2));
        Instant marker = T0.plus(Duration.ofMinutes(1));
        ingestAt(STD, marker, point("marker", STD_KIND, marker, 3, 3, 3));
    }

    @Test
    @DisplayName("held STANDARD rows survive the chunked delete; retention deletes them and the floor still moves")
    void standardDeleteKeepsHeldCopies() throws Exception {
        seedStandard();
        String id = hold("{\"name\":\"keep\",\"reason\":\"inquiry\",\"sources\":[\"" + STD + "\"]}");
        RetentionRunResult result = runAt(T0.plus(Duration.ofDays(8)));
        assertThat(result.skippedSources()).doesNotContain(STD);
        assertThat(versions(STD, "old1")).isZero();
        assertThat(versions(STD, "marker")).isEqualTo(1);
        assertThat(held(STD)).isEqualTo(2);
        assertThat(state.floorSeq(STD)).isPositive().isEqualTo(result.perSource().get(STD).floorSeq());
        JsonNode view = JSON.readTree(mvc.perform(get("/v1/holds").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(view.get("items").get(0).get("heldRows").asLong()).isEqualTo(2);
        JsonNode page = JSON.readTree(mvc.perform(get("/v1/holds/" + id + "/versions?limit=1")
                .header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString());
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("items").get(0).get("source").asString()).isEqualTo(STD);
        assertThat(page.get("items").get(0).get("payload").get("v").asInt()).isIn(1, 2);
        String next = page.get("nextCursor").asString();
        JsonNode second = JSON.readTree(mvc.perform(get("/v1/holds/" + id + "/versions?limit=1&cursor=" + next)
                .header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse().getContentAsString());
        assertThat(second.get("items")).hasSize(1);
        assertThat(second.get("nextCursor").isNull()).isTrue();

        // A second run copies nothing new and fails nothing (ON CONFLICT DO NOTHING).
        runAt(T0.plus(Duration.ofDays(8)).plusSeconds(3600));
        assertThat(held(STD)).isEqualTo(2);

        mvc.perform(delete("/v1/holds/" + id).header(HttpHeaders.AUTHORIZATION, admin)).andExpect(status().isNoContent());
        assertThat(held(STD)).as("deleting a hold deletes its copies").isZero();
        mvc.perform(delete("/v1/holds/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_HOLD"));
    }

    @Test
    @DisplayName("held TRACK rows survive the leaf drop; the leaf is still dropped and the floor moves")
    void trackLeafDropKeepsHeldCopies() throws Exception {
        ingestAt(TRACK, T0, aircraft("a", T0, 1.0));
        jumpSeq(20_000_000L);
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(TRACK, later, aircraft("c", later, 2.0));
        hold("{\"name\":\"tracks\",\"reason\":\"incident\",\"sources\":[\"" + TRACK + "\"],\"kinds\":[\"" + TRACK_KIND
                + "\"]}");
        RetentionRunResult due = runAt(later.plus(Duration.ofDays(3)).plus(Duration.ofMinutes(5)));
        assertThat(due.partitionsDropped()).isGreaterThanOrEqualTo(1);
        assertThat(leafNames()).doesNotContain("rv_t_0");
        assertThat(versions(TRACK, "a")).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version_held WHERE external_id = 'a'", Long.class))
                .isEqualTo(1);
        assertThat(state.floorSeq(TRACK)).isEqualTo(20_000_000L);
    }

    @Test
    @DisplayName("a failed copy keeps the rows: the STANDARD source is skipped and the TRACK leaf is not dropped")
    void copyFailureSkipsTheDeletes() throws Exception {
        ingestAt(TRACK, T0, aircraft("a", T0, 1.0));
        seedStandard();
        jumpSeq(20_000_000L);
        Instant later = T0.plus(Duration.ofHours(1));
        ingestAt(TRACK, later, aircraft("c", later, 2.0));
        hold("{\"name\":\"all\",\"reason\":\"everything\"}");
        long floorBefore = state.floorSeq(STD);
        jdbc.execute("ALTER TABLE record_version_held RENAME TO record_version_held_off");
        RetentionRunResult result;
        try {
            result = runAt(later.plus(Duration.ofDays(8)));
        } finally {
            jdbc.execute("ALTER TABLE record_version_held_off RENAME TO record_version_held");
        }
        assertThat(result.skippedSources()).contains(STD);
        assertThat(versions(STD, "old1")).as("not deleted before it was copied").isEqualTo(1);
        assertThat(versions(TRACK, "a")).isEqualTo(1);
        assertThat(leafNames()).contains("rv_t_0");
        assertThat(state.floorSeq(STD)).isEqualTo(floorBefore);

        RetentionRunResult retry = runAt(later.plus(Duration.ofDays(8)).plusSeconds(3600));
        assertThat(retry.skippedSources()).doesNotContain(STD);
        assertThat(versions(STD, "old1")).isZero();
        assertThat(versions(TRACK, "a")).isZero();
        // The TRACK minute at T0 + 1 h moves the STANDARD cutoff past the marker too: all three are copied.
        assertThat(held(STD)).isEqualTo(3);
        assertThat(held(TRACK)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("an expired hold copies nothing; a hold's range bounds what it copies")
    void expiryAndRange() throws Exception {
        seedStandard();
        hold("{\"name\":\"lapsed\",\"reason\":\"old\",\"sources\":[\"" + STD + "\"],\"expiresAt\":\"2026-03-02T00:00:00Z\"}");
        long seqOld2 = jdbc.queryForObject("SELECT max(seq) FROM record_version WHERE source_id = ? AND external_id IN"
                + " ('old1','old2')", Long.class, STD);
        hold("{\"name\":\"one\",\"reason\":\"just the last\",\"sources\":[\"" + STD + "\"],\"fromSeq\":" + seqOld2
                + ",\"toSeq\":" + seqOld2 + "}");
        runAt(T0.plus(Duration.ofDays(8)));
        assertThat(held(STD)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT seq FROM record_version_held", Long.class)).isEqualTo(seqOld2);
    }

    @Test
    @DisplayName("holds.max is enforced with 409 HOLD_LIMIT; a bad body is 400; agents are refused")
    void limitsAndValidation() throws Exception {
        for (int i = 0; i < 20; i++) {
            hold("{\"name\":\"h" + i + "\",\"reason\":\"r\"}");
        }
        mvc.perform(post("/v1/holds").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"h21\",\"reason\":\"r\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HOLD_LIMIT"));
        JsonNode page = JSON.readTree(mvc.perform(get("/v1/holds?limit=15").header(HttpHeaders.AUTHORIZATION, admin))
                .andReturn().getResponse().getContentAsString());
        assertThat(page.get("items")).hasSize(15);
        JsonNode rest = JSON.readTree(mvc.perform(get("/v1/holds?limit=15&cursor=" + page.get("nextCursor").asString())
                .header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse().getContentAsString());
        assertThat(rest.get("items")).hasSize(5);
        mvc.perform(post("/v1/holds").header(HttpHeaders.AUTHORIZATION, admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"sources\":[\"nope\"],\"from\":\"x\"}")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/holds").header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isForbidden());
        mvc.perform(get("/v1/holds/01JNOSUCHHOLDNOSUCHHOLDNOS/versions").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }
}
