package ai.foresignal.store.query;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL;
import static ai.foresignal.testsupport.ingest.TestIngestSources.TTL_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.Queries;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

/** The change feed: {@code seq > after} in commit order, its page fields, and the purge horizon. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class ChangesQueryIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired RecordQueryService queries;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        retention.reset();
    }

    @AfterEach
    void tearDown() {
        retention.reset();
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        records.ingestAt(source, at, batch);
    }

    private static NormalizedRecord.Builder rec(String id, String kind, Instant observedAt) {
        return NormalizedRecord.builder(id, kind).observedAt(observedAt);
    }

    private static List<String> ids(Slices.ChangeSlice slice) {
        return slice.rows().stream().map(RecordRow::externalId).toList();
    }

    @Test
    @DisplayName("after is exclusive, order is seq, hasMore and lastSeq page the feed")
    void afterSemantics() {
        ingest(T, STANDARD, rec("a", STANDARD_KIND, T).build(), rec("b", STANDARD_KIND, T).build(),
                rec("c", STANDARD_KIND, T).build());
        Slices.ChangeSlice all = queries.changes(Queries.changes(0).build());
        assertThat(ids(all)).containsExactly("a", "b", "c");
        assertThat(all.hasMore()).isFalse();
        long first = all.rows().getFirst().seq();

        Slices.ChangeSlice page = queries.changes(Queries.changes(0).limit(2).build());
        assertThat(ids(page)).containsExactly("a", "b");
        assertThat(page.hasMore()).isTrue();
        assertThat(page.lastSeq()).isEqualTo(page.rows().get(1).seq());
        Slices.ChangeSlice rest = queries.changes(Queries.changes(page.lastSeq()).limit(2).build());
        assertThat(ids(rest)).containsExactly("c");
        assertThat(rest.hasMore()).isFalse();

        assertThat(ids(queries.changes(Queries.changes(first).build()))).containsExactly("b", "c");
        assertThat(all.watermarkSeq()).isEqualTo(all.rows().getLast().seq());
        assertThat(all.purgeHorizon()).isZero();
    }

    @Test
    @DisplayName("an empty filtered page advances lastSeq to the watermark")
    void filteredClientsSkipAhead() {
        ingest(T, STANDARD, rec("a", STANDARD_KIND, T).build());
        Slices.ChangeSlice slice = queries.changes(Queries.changes(0).sources(TTL).build());
        assertThat(slice.rows()).isEmpty();
        assertThat(slice.lastSeq()).isEqualTo(slice.watermarkSeq()).isPositive();
    }

    @Test
    @DisplayName("changes carry validUntil, persistReason, prevSeq, historyTier and bbox")
    void changeFields() {
        ingest(T, TTL, rec("q", TTL_KIND, T).point(5, 6).put("mag", 1).build());
        ingest(T.plusSeconds(60), TTL, rec("q", TTL_KIND, T.plusSeconds(60)).point(5, 6).put("mag", 2).build());
        List<RecordRow> rows = queries.changes(Queries.changes(0).build()).rows();
        assertThat(rows).hasSize(2);
        RecordRow first = rows.get(0);
        RecordRow second = rows.get(1);
        assertThat(first.validUntil()).isEqualTo(T.plus(Duration.ofMinutes(10)));
        assertThat(PersistReason.fromCode(first.persistReason())).isEqualTo(PersistReason.FIRST);
        assertThat(first.prevSeq()).isNull();
        assertThat(second.prevSeq()).isEqualTo(first.seq());
        assertThat(PersistReason.fromCode(second.persistReason())).isNotEqualTo(PersistReason.FIRST);
        assertThat(first.historyTier()).isEqualTo((short) 0);
        assertThat(first.bbox()).containsExactly(5, 6, 5, 6);
        assertThat(first.payload()).as("payload=false").isNull();
        assertThat(first.geometry()).contains("Point");
    }

    @Test
    @DisplayName("source, kind, externalId and bbox filter the feed; payload=true loads payloads")
    void filtersAndPayload() {
        ingest(T, STANDARD, rec("a", STANDARD_KIND, T).point(1, 1).put("k", "v").build(),
                rec("b", STANDARD_KIND, T).point(9, 9).build());
        ingest(T, TTL, rec("q", TTL_KIND, T).build());
        assertThat(ids(queries.changes(Queries.changes(0).ids("b", "q").build()))).containsExactly("b", "q");
        assertThat(ids(queries.changes(Queries.changes(0).kinds(TTL_KIND).build()))).containsExactly("q");
        assertThat(ids(queries.changes(Queries.changes(0).sources(STANDARD).build()))).containsExactly("a", "b");
        assertThat(ids(queries.changes(Queries.changes(0).bbox(BboxParser.parse("0,0,2,2")).build()))).containsExactly("a");
        assertThat(ids(queries.changes(Queries.changes(0).excluded(STANDARD).build()))).containsExactly("q");

        RecordRow withPayload = queries.changes(Queries.changes(0).ids("a").payload(true).build()).rows().getFirst();
        assertThat(withPayload.payload()).contains("\"k\"");
        RecordRow projected = queries.changes(Queries.changes(0).ids("a").payload(true)
                .fields(FieldSelector.parse("seq,payload.k")).build()).rows().getFirst();
        assertThat(projected.payload()).isEqualTo("{\"k\": \"v\"}");
    }

    @Test
    @DisplayName("raster-frame changes always carry their payload, even with payload=false")
    void rasterFramePayloadInline() {
        ingest(T, RecordsTestSources.RASTER, rec("mrms-reflectivity", "raster-frame", T)
                .put("productId", "mrms").put("frameKey", "20260301T0000").build());
        ingest(T, STANDARD, rec("a", STANDARD_KIND, T).put("k", "v").build());
        List<RecordRow> rows = queries.changes(Queries.changes(0).build()).rows();
        assertThat(rows.get(0).payload()).contains("frameKey");
        assertThat(rows.get(1).payload()).isNull();
    }

    @Test
    @DisplayName("after below the purge horizon is 410 RESYNC_REQUIRED with purgeHorizon and watermarkSeq")
    void resyncRequired() {
        ingest(T, STANDARD, rec("a", STANDARD_KIND, T).build());
        retention.setPurgeSeq(STANDARD, 5);

        assertThatThrownBy(() -> queries.changes(Queries.changes(4).build()))
                .isInstanceOf(ForesignalException.class)
                .satisfies(e -> {
                    ForesignalException f = (ForesignalException) e;
                    assertThat(f.getStatus()).isEqualTo(HttpStatus.GONE);
                    assertThat(f.getCode()).isEqualTo(ErrorCodes.RESYNC_REQUIRED);
                    assertThat(f.getProperties()).containsEntry("purgeHorizon", 5L).containsKey("watermarkSeq");
                });
        assertThat(queries.changes(Queries.changes(5).build()).purgeHorizon()).isEqualTo(5);
        assertThat(queries.changes(Queries.changes(0).sources(TTL).build()).purgeHorizon())
                .as("the horizon is taken over the filtered sources only").isZero();
    }
}
