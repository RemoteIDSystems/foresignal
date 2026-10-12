package ai.foresignal.store.query;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.FieldSelector;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.api.support.NearFilter;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;

/** The pure SQL-assembly helpers of the read side. */
class SqlHelpersTest {

    @Test
    void pgArraysQuoteAndEscape() {
        assertThat(PgArrays.text(List.of("a", "b\"c", "d\\e", "f,g"))).isEqualTo("{\"a\",\"b\\\"c\",\"d\\\\e\",\"f,g\"}");
        assertThat(PgArrays.text(List.of())).isEqualTo("{}");
        assertThat(PgArrays.bigint(List.of(1L, 2L))).isEqualTo("{1,2}");
    }

    @Test
    void sqlParamsBindValuesNotText() {
        SqlParams p = new SqlParams();
        assertThat(p.add("x'; DROP TABLE record_head; --")).isEqualTo(":p0");
        assertThat(p.instant(Instant.EPOCH)).isEqualTo("CAST(:p1 AS timestamptz)");
        assertThat(p.source().getValue("p1")).isEqualTo(OffsetDateTime.parse("1970-01-01T00:00:00Z"));
        assertThat(p.textArray(List.of("a"))).isEqualTo("CAST(:p2 AS text[])");
        assertThat(p.bigintArray(List.of(1L))).isEqualTo("CAST(:p3 AS bigint[])");
        assertThat(p.dbl(1.5)).isEqualTo("CAST(:p4 AS double precision)");
        assertThat(p.bigint(5)).isEqualTo("CAST(:p5 AS bigint)");
        assertThat(p.text(null)).isEqualTo("CAST(:p6 AS text)");
    }

    @Test
    @DisplayName("LIKE metacharacters are escaped")
    void escapesLike() {
        assertThat(RecordQueryService.escapeLike("50%_off\\")).isEqualTo("50\\%\\_off\\\\");
    }

    @Test
    void bboxClauseOrsEnvelopes() {
        String sql = RecordQueryService.bboxClause("l.geom", BboxParser.parse("170,0,-170,10"), new SqlParams());
        assertThat(sql).contains(" OR ").contains("l.geom && ST_MakeEnvelope(").contains("ST_Intersects(l.geom, ");
    }

    @Test
    void nearClausePrefiltersThenUsesGeography() {
        String sql = RecordQueryService.nearClause("l.geom", new NearFilter(0, 0, 10), new SqlParams());
        assertThat(sql).contains("l.geom && ST_MakeEnvelope(").contains("ST_DWithin(l.geom::geography, ");
    }

    @Test
    void payloadExpression() {
        SqlParams p = new SqlParams();
        assertThat(RecordQueryService.payloadExpression("v.payload", FieldSelector.parse("source"), p)).isNull();
        assertThat(RecordQueryService.payloadExpression("v.payload", FieldSelector.parse(null), p))
                .isEqualTo("(v.payload)::text");
        assertThat(RecordQueryService.payloadExpression("v.payload", FieldSelector.parse("payload.a.b"), p))
                .startsWith("jsonb_strip_nulls(jsonb_build_object(")
                .contains("(v.payload) #> CAST(");
    }

    @Test
    void geometryExpression() {
        assertThat(RecordQueryService.geometryExpression("l.geom", GeometryOptions.DEFAULT, GeometryEncoding.GEOJSON,
                new SqlParams())).startsWith("ST_AsGeoJSON(l.geom, ");
        assertThat(RecordQueryService.geometryExpression("l.geom", GeometryOptions.DEFAULT, GeometryEncoding.WKT,
                new SqlParams())).startsWith("ST_AsText(l.geom, ");
    }

    @Test
    void historyAxis() {
        assertThat(RecordQueryService.historyAxis(ai.foresignal.api.support.TimeField.INGESTED)).isNull();
        assertThat(RecordQueryService.historyAxis(ai.foresignal.api.support.TimeField.EFFECTIVE))
                .isEqualTo("COALESCE(v.event_time, v.observed_at)");
    }

    @Test
    void selectListFillsEveryLabel() {
        String sql = new SelectList().put("seq", "h.seq").sql();
        assertThat(sql).contains("h.seq AS seq").contains("NULL::text AS source_id").contains("NULL::double precision AS by2");
    }

    @Test
    @DisplayName("SQLState 57014 anywhere in the cause chain is a statement timeout")
    void detectsQueryCanceled() {
        SQLException canceled = new SQLException("canceling statement due to statement timeout", "57014");
        assertThat(ReadQueryRunner.isQueryCanceled(new QueryTimeoutException("x", canceled))).isTrue();
        assertThat(ReadQueryRunner.isQueryCanceled(new QueryTimeoutException("x", new SQLException("y", "40001")))).isFalse();
    }
}
