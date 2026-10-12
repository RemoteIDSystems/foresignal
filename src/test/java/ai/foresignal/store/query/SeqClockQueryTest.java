package ai.foresignal.store.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/** {@code cutoffSeq} and {@code S(T)} over a mocked {@code seq_clock} (Core-Data-Model §8). */
class SeqClockQueryTest {

    private static final Instant M = Instant.parse("2026-03-01T12:00:00Z");

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final SeqClockQuery query = new SeqClockQuery();

    @SuppressWarnings("unchecked")
    private void latestMinute(SeqClockQuery.Minute minute) {
        when(jdbc.query(eq(SeqClockQuery.CUTOFF), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(minute == null ? List.of() : List.of(minute));
    }

    private void nextMinute(Long firstSeq) {
        when(jdbc.queryForList(eq(SeqClockQuery.NEXT_MINUTE), any(SqlParameterSource.class), eq(Long.class)))
                .thenReturn(firstSeq == null ? List.of() : List.of(firstSeq));
    }

    @Test
    @DisplayName("cutoffSeq is first_seq of the latest minute at or before t")
    void cutoff() {
        latestMinute(new SeqClockQuery.Minute(M, 100));
        assertThat(query.cutoffSeq(jdbc, M.plusSeconds(30))).isEqualTo(100);
    }

    @Test
    @DisplayName("cutoffSeq and S(T) are 0 before the first minute")
    void beforeEverything() {
        latestMinute(null);
        assertThat(query.cutoffSeq(jdbc, M)).isZero();
        assertThat(query.seqAt(jdbc, M)).isZero();
    }

    @Test
    @DisplayName("S(T) is the largest seq ingested at or before T within the minute")
    void seqAtWithinMinute() {
        latestMinute(new SeqClockQuery.Minute(M, 100));
        nextMinute(200L);
        when(jdbc.queryForObject(eq(SeqClockQuery.MAX_SEQ_BOUNDED), any(SqlParameterSource.class), eq(Long.class)))
                .thenReturn(150L);
        assertThat(query.seqAt(jdbc, M.plusSeconds(20))).isEqualTo(150);
        verify(jdbc, never()).queryForObject(eq(SeqClockQuery.MAX_SEQ_OPEN), any(SqlParameterSource.class), eq(Long.class));
    }

    @Test
    @DisplayName("S(T) is first_seq − 1 when nothing in the minute was ingested by T")
    void seqAtBeforeFirstBatchOfMinute() {
        latestMinute(new SeqClockQuery.Minute(M, 100));
        nextMinute(200L);
        when(jdbc.queryForObject(eq(SeqClockQuery.MAX_SEQ_BOUNDED), any(SqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);
        assertThat(query.seqAt(jdbc, M)).isEqualTo(99);
    }

    @Test
    @DisplayName("in the newest minute S(T) has no upper seq bound")
    void seqAtInNewestMinute() {
        latestMinute(new SeqClockQuery.Minute(M, 100));
        nextMinute(null);
        when(jdbc.queryForObject(eq(SeqClockQuery.MAX_SEQ_OPEN), any(SqlParameterSource.class), eq(Long.class)))
                .thenReturn(321L);
        assertThat(query.seqAt(jdbc, M.plusSeconds(59))).isEqualTo(321);
    }

    @Test
    void lastSeqBeforeLooksOneMicrosecondEarlier() {
        latestMinute(new SeqClockQuery.Minute(M, 100));
        nextMinute(null);
        when(jdbc.queryForObject(eq(SeqClockQuery.MAX_SEQ_OPEN), any(SqlParameterSource.class), eq(Long.class)))
                .thenReturn(null);
        assertThat(query.lastSeqBefore(jdbc, M)).isEqualTo(99);
    }

    @Test
    void resolveNeverGoesNegative() {
        assertThat(SeqClockQuery.resolve(1, null)).isZero();
        assertThat(SeqClockQuery.resolve(0, null)).isZero();
        assertThat(SeqClockQuery.resolve(5, 9L)).isEqualTo(9);
    }
}
