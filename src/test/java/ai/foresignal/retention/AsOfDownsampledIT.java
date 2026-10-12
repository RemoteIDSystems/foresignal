package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.DownsampleItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * asOf over downsampled history (prompt 044, Core-Data-Model §12): validity widens to
 * {@code max(ttl, effectiveDownsampleInterval)} and the answer is {@code approximate}; STANDARD is
 * unaffected.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class AsOfDownsampledIT extends DownsampleItSupport {

    @Autowired MockMvc mvc;
    @Autowired AsOfResolver resolver;

    @Test
    @DisplayName("a stationary keyframed aircraft queried between surviving versions is still present, approximate")
    void stationaryAircraftBridged() throws Exception {
        // A 15-minute interval over the 5-minute TTL: survivors must bridge gaps the TTL alone cannot.
        sources.update(TRACK, SourceSettingsUpdate.downsampleInterval(Duration.ofMinutes(15)));
        try {
            // Stationary: only keyframes persist (every 240 s), at minutes 0 (FIRST), 4, 8, ..., 28.
            for (int k = 0; k <= 29; k++) {
                ingestAt(TRACK, minute(k), aircraft("parked", minute(k), 5.0, "PARKED"));
            }
            List<Long> before = seqs(TRACK, "parked");
            assertThat(reasons(TRACK, "parked").getFirst()).isEqualTo((int) PersistReason.FIRST.code());
            assertThat(before).hasSize(8);
            Instant asOf = minute(22);
            // Raw history: the version of minute 20 is valid at minute 22, and nothing is downsampled.
            clock.setInstant(minute(40));
            mvc.perform(get("/v1/records").param("asOf", asOf.toString()).param("source", TRACK))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].seq").value(before.get(5)));
            AsOfPlan raw = resolver.plan(asOf, Set.of(TRACK));
            assertThat(raw.downsampled()).isFalse();
            assertThat(raw.validity(TRACK)).isEqualTo(Duration.ofMinutes(5));
            pushPastRawWindow();

            runDue();

            // 15-minute buckets: FIRST 0 and 4 survive in the first, 16 and the newest (28) in the second.
            assertThat(seqs(TRACK, "parked")).containsExactly(before.get(0), before.get(1), before.get(4), before.get(7));
            // At minute 22 the newest surviving version is minute 16: dead under the 5-minute TTL (16 + 5 < 22),
            // alive under max(ttl, 15 min).
            mvc.perform(get("/v1/records").param("asOf", asOf.toString()).param("source", TRACK))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].externalId").value("parked"))
                    .andExpect(jsonPath("$.items[0].seq").value(before.get(4)))
                    .andExpect(jsonPath("$.approximate").value(true));
            AsOfPlan thinned = resolver.plan(asOf, Set.of(TRACK));
            assertThat(thinned.downsampled()).isTrue();
            assertThat(thinned.approximate()).isTrue();
            assertThat(thinned.validity(TRACK)).as("max(5 min TTL, 15 min interval)").isEqualTo(Duration.ofMinutes(15));
        } finally {
            sources.update(TRACK, SourceSettingsUpdate.clearingDownsampleInterval());
        }
    }

    @Test
    @DisplayName("STANDARD asOf is unaffected by thinned TRACK leaves")
    void standardUnaffected() throws Exception {
        ingestAt(STANDARD, minute(0), record("quake", STANDARD_KIND, minute(0), 1));
        flyEveryMinute("a", 1.0, 10);
        pushPastRawWindow();

        runDue();

        assertThat(bookkeeping(LEAF)).containsEntry("status", "DONE");
        mvc.perform(get("/v1/records").param("asOf", minute(5).toString()).param("source", STANDARD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.approximate").value(false));
    }
}
