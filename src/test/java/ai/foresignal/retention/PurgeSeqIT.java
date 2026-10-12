package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Invariant RET-4: {@code purgeSeq} advances only when heads are purged, never on version deletes
 * alone; {@code lowestRetainedSeq} follows the floors; both survive a restart and drive prompt 014's
 * {@code 410 RESYNC_REQUIRED}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class PurgeSeqIT extends RetentionItSupport {

    @Autowired MockMvc mvc;
    @Autowired TransactionTemplate tx;
    @Autowired RetentionResolver resolver;

    @Test
    @DisplayName("version deletes leave purgeSeq alone; a head purge sets it to the watermark")
    void purgeSeqOnlyOnHeadPurge() throws Exception {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant m = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, m, record("m", STANDARD_KIND, m, 1));
        Instant changed = T0.plus(Duration.ofDays(2));
        ingestAt(STANDARD, changed, record("x", STANDARD_KIND, changed, 2));

        RetentionRunResult first = runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(2)));

        assertThat(first.versionsDeleted()).isEqualTo(1);
        assertThat(first.headsPurged()).isZero();
        assertThat(state.purgeSeq(STANDARD)).isZero();
        long floor = state.floorSeq(STANDARD);
        assertThat(floor).isPositive();
        assertThat(state.lowestRetainedSeq(Set.of(STANDARD))).isEqualTo(floor);
        mvc.perform(get("/v1/changes").param("source", STANDARD).param("after", "0"))
                .andExpect(status().isOk());

        RetentionRunResult second = runAt(changed.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(2)));

        assertThat(second.headsPurged()).isEqualTo(1);
        assertThat(head(STANDARD, "m")).isFalse();
        long watermark = changeBus.lastPublishedSeq();
        assertThat(state.purgeSeq(STANDARD)).isEqualTo(watermark);
        mvc.perform(get("/v1/changes").param("source", STANDARD).param("after", "0"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("RESYNC_REQUIRED"));
        mvc.perform(get("/v1/changes").param("source", STANDARD).param("after", Long.toString(watermark)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RetentionState reloads from retention_state after a restart")
    void reloadsAfterRestart() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant m = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, m, record("m", STANDARD_KIND, m, 1));
        runAt(T0.plus(Duration.ofDays(8)));
        long purge = state.purgeSeq(STANDARD);
        long floor = state.floorSeq(STANDARD);
        assertThat(purge).isPositive();

        RetentionStateStore restarted = new RetentionStateStore(jdbc, tx, resolver);
        restarted.onApplicationReady();

        assertThat(restarted.purgeSeq(STANDARD)).isEqualTo(purge);
        assertThat(restarted.floorSeq(STANDARD)).isEqualTo(floor);
        assertThat(restarted.lowestRetainedSeq(Set.of())).isGreaterThanOrEqualTo(floor);
        assertThat(restarted.window(STANDARD)).isEqualTo(Duration.ofDays(7));
    }
}
