package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.derive.DerivedContext;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Merging and splitting situations (prompt 118 §6): members move, the merged situation's {@code foresignal-intel}
 * record is tombstoned (driven through a {@link SituationRecordPublisher} of the test's own), both logs record the change with the actor, and a stale version is a 409.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class SituationMergeSplitIT {

    static final String STANDARD = TestIngestSources.STANDARD;
    static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired SituationService service;
    @Autowired SituationMemberRepository members;
    @Autowired SituationLogRepository logs;
    @Autowired SituationRepository situations;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    /** A publisher of its own, so the application's derived ticks cannot interleave with the assertions. */
    SituationRecordPublisher publisher;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        publisher = new SituationRecordPublisher(situations);
        publisher.reconcile(List.of());
    }

    long record(String id, double lon, double lat) {
        Instant now = clock.instant();
        records.ingestAt(STANDARD, now, NormalizedRecord.builder(id, "test-record").observedAt(now).point(lon, lat)
                .put("title", "Thing " + id).build());
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, STANDARD, id);
    }

    SituationEntity create(String title, String... ids) {
        StringBuilder body = new StringBuilder("{\"title\":\"" + title + "\",\"theme\":\"maritime\",\"members\":[");
        for (int i = 0; i < ids.length; i++) {
            long seq = record(ids[i], 45.0 + i * 0.1, 12.0);
            body.append(i == 0 ? "" : ",").append("{\"memberType\":\"record\",\"ref\":\"").append(STANDARD).append('/')
                    .append(ids[i]).append("\",\"seq\":").append(seq).append('}');
        }
        body.append("]}");
        return service.create(json.readTree(body.toString()), "token-1", "agent", false);
    }

    @Test
    @DisplayName("a merge keeps the older id, moves members, tombstones the merged record and logs both sides")
    void merge() {
        SituationEntity older = create("Older", "a1", "a2");
        clock.setInstant(T0.plusSeconds(60));
        SituationEntity newer = create("Newer", "b1");
        DerivedContext first = mock(DerivedContext.class);
        publisher.onTick(clock.instant(), first);
        verify(first).emit(org.mockito.ArgumentMatchers.argThat((NormalizedRecord r) ->
                r.externalId().equals(newer.getId())));

        // Merging from the newer situation: the older one survives anyway.
        SituationEntity survivor = service.merge(newer.getId(), older.getId(), null, "same story", "token-2", false);
        assertThat(survivor.getId()).isEqualTo(older.getId());
        assertThat(members.findBySituationIdOrderByAddedAtAscIdAsc(older.getId())).hasSize(3);
        assertThat(members.findBySituationIdOrderByAddedAtAscIdAsc(newer.getId())).isEmpty();
        SituationEntity merged = service.get(newer.getId());
        assertThat(merged.getMergedInto()).isEqualTo(older.getId());
        assertThat(merged.getStatus()).isEqualTo(SituationLifecycle.CLOSED);
        assertThat(logs.findBySituationIdOrderByIdAsc(newer.getId())).anySatisfy(row -> {
            assertThat(row.getAction()).isEqualTo("merged");
            assertThat(row.getActor()).isEqualTo("token-2");
            assertThat(row.getEvent()).isEqualTo(SituationEvent.MERGED);
        });
        assertThat(logs.findBySituationIdOrderByIdAsc(older.getId())).extracting(SituationLogEntity::getAction)
                .contains("merged");

        clock.setInstant(T0.plusSeconds(600));
        DerivedContext second = mock(DerivedContext.class);
        publisher.onTick(clock.instant(), second);
        verify(second).tombstone(SituationRecordPublisher.KIND, newer.getId());
        verify(second, never()).tombstone(SituationRecordPublisher.KIND, older.getId());

        // A closed situation is final.
        assertThatThrownBy(() -> service.merge(older.getId(), newer.getId(), null, null, "token-2", false))
                .isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode()).isEqualTo(ErrorCodes.SITUATION_CLOSED);
    }

    @Test
    @DisplayName("a split moves the chosen members into a new situation; both are re-derived and logged")
    void split() {
        SituationEntity source = create("Source", "a1", "a2", "a3");
        List<SituationMemberEntity> current = members.findBySituationIdOrderByAddedAtAscIdAsc(source.getId());
        String moved = current.get(2).getId();
        SituationEntity created = service.split(source.getId(), List.of(moved), "Split off", null, null, "token-3",
                "agent", false);
        assertThat(created.getTheme()).isEqualTo("maritime");
        assertThat(members.findBySituationIdOrderByAddedAtAscIdAsc(created.getId()))
                .extracting(SituationMemberEntity::getId).containsExactly(moved);
        assertThat(service.get(source.getId()).getMemberCount()).isEqualTo(2);
        assertThat(service.get(created.getId()).getMemberCount()).isEqualTo(1);
        assertThat(logs.findBySituationIdOrderByIdAsc(source.getId())).extracting(SituationLogEntity::getAction)
                .contains("split");
        assertThat(logs.findBySituationIdOrderByIdAsc(created.getId())).extracting(SituationLogEntity::getAction)
                .contains("split");
        // A member of another situation cannot be split off.
        assertThatThrownBy(() -> service.split(source.getId(), List.of(moved), "Again", null, null, "token-3",
                "agent", false)).isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode())
                .isEqualTo(ErrorCodes.SITUATION_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("a stale version is 409 SITUATION_VERSION_CONFLICT on patch, merge and split")
    void optimisticLock() {
        SituationEntity a = create("A", "a1");
        clock.setInstant(T0.plusSeconds(60));
        SituationEntity b = create("B", "b1");
        int stale = service.get(a.getId()).getVersion() + 7;
        assertThatThrownBy(() -> service.patch(a.getId(), json.readTree("{\"title\":\"x\",\"version\":" + stale + "}"),
                "t", false)).isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode())
                .isEqualTo(ErrorCodes.SITUATION_VERSION_CONFLICT);
        assertThatThrownBy(() -> service.merge(a.getId(), b.getId(), stale, null, "t", false))
                .isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode())
                .isEqualTo(ErrorCodes.SITUATION_VERSION_CONFLICT);
        String memberId = members.findBySituationIdOrderByAddedAtAscIdAsc(a.getId()).get(0).getId();
        assertThatThrownBy(() -> service.split(a.getId(), List.of(memberId), "x", null, stale, "t", "agent", false))
                .isInstanceOf(ForesignalException.class)
                .extracting(e -> ((ForesignalException) e).getCode())
                .isEqualTo(ErrorCodes.SITUATION_VERSION_CONFLICT);
        // The current version succeeds.
        int current = service.get(a.getId()).getVersion();
        assertThat(service.patch(a.getId(), json.readTree("{\"title\":\"Renamed\",\"version\":" + current + "}"),
                "t", false).getTitle()).isEqualTo("Renamed");
        assertThat(logs.findBySituationIdOrderByIdAsc(a.getId())).extracting(SituationLogEntity::getAction)
                .contains("renamed");
        verifyNoTombstones();
    }

    private void verifyNoTombstones() {
        DerivedContext ctx = mock(DerivedContext.class);
        publisher.onTick(clock.instant(), ctx);
        verify(ctx, never()).tombstone(any(), any());
    }
}
