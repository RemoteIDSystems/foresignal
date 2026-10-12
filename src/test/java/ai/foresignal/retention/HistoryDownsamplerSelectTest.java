package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.LeafInfo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link HistoryDownsampler#select} (eligibility, oldest first) and the shape of
 * {@link HistoryDownsampler#keepSetSql} (prompt 044).
 */
class HistoryDownsamplerSelectTest {

    static LeafInfo leaf(long from, long to) {
        return new LeafInfo("rv_t_" + from, HistoryTier.TRACK, from, to, false);
    }

    static final List<LeafInfo> LEAVES = List.of(leaf(200, 300), leaf(0, 100), leaf(100, 200), leaf(300, 400));

    @Test
    @DisplayName("the oldest leaf at or below the raw cutoff and the consumer bound, above the drop horizon")
    void oldestEligible() {
        assertThat(HistoryDownsampler.select(LEAVES, Set.of(), 300, Long.MAX_VALUE, 0)).map(LeafInfo::name)
                .contains("rv_t_0");
        assertThat(HistoryDownsampler.select(LEAVES, Set.of(), 300, Long.MAX_VALUE, 100)).as("rv_t_0 is about to drop")
                .map(LeafInfo::name).contains("rv_t_100");
        assertThat(HistoryDownsampler.select(LEAVES, Set.of("rv_t_0"), 300, Long.MAX_VALUE, 0)).as("DONE is skipped")
                .map(LeafInfo::name).contains("rv_t_100");
        assertThat(HistoryDownsampler.select(LEAVES, Set.of(), 300, 150, 0)).as("the consumer bound")
                .map(LeafInfo::name).contains("rv_t_0");
        assertThat(HistoryDownsampler.select(LEAVES, Set.of(), 300, 99, 0)).as("bound below every upper bound").isEmpty();
        assertThat(HistoryDownsampler.select(LEAVES, Set.of(), 99, Long.MAX_VALUE, 0)).as("inside the raw window").isEmpty();
    }

    @Test
    @DisplayName("pending detaches and copies or other names are never chosen")
    void skipsOddLeaves() {
        List<LeafInfo> odd = List.of(new LeafInfo("rv_t_0", HistoryTier.TRACK, 0, 100, true),
                new LeafInfo("rv_t_100_ds", HistoryTier.TRACK, 100, 200, false), leaf(200, 300));
        assertThat(HistoryDownsampler.select(odd, Set.of(), 1000, Long.MAX_VALUE, 0)).map(LeafInfo::name)
                .contains("rv_t_200");
    }

    @Test
    @DisplayName("the keep-set statement pins the reasons, the newest seq, heads and alerts, and ranks the rest per bucket")
    void keepSetShape() {
        String plain = HistoryDownsampler.keepSetSql("rv_t_0", "rv_t_0_ds", 0, 100, 300, Map.of());
        assertThat(plain)
                .contains("persist_reason IN (1, 2, 3, 4, 9)")
                .contains("max(v.seq) OVER (PARTITION BY v.source_id, v.external_id)")
                .contains("FROM record_head WHERE seq >= 0 AND seq < 100")
                .contains("FROM alert WHERE seq >= 0 AND seq < 100")
                .contains("300 AS ds_interval_s")
                .contains("floor(extract(epoch FROM f.observed_at) / f.ds_interval_s) ORDER BY f.seq")
                .contains("INSERT INTO rv_t_0_ds (seq, ingested_at")
                .contains("WHERE ds_pinned OR ds_rank = 1")
                .doesNotContain("VALUES");
        Map<String, Long> overrides = new LinkedHashMap<>();
        overrides.put("a", 60L);
        overrides.put("b", 600L);
        String withOverrides = HistoryDownsampler.keepSetSql("rv_t_0", "rv_t_0_ds", 0, 100, 300, overrides);
        assertThat(withOverrides)
                .contains("(VALUES (CAST(? AS varchar), 60), (CAST(? AS varchar), 600))")
                .contains("coalesce(iv.interval_s, 300) AS ds_interval_s");
    }
}
