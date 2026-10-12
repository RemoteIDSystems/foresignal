package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.Queries;
import ai.foresignal.testsupport.records.RecordsTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The streaming formats (ndjson, geojsonseq, csv) on records, history and changes, and the row cap. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class StreamingFormatsIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired RecordQueryService queries;

    @BeforeEach
    void seed() {
        clock.setInstant(T);
        records.ingestAt(STANDARD, T,
                NormalizedRecord.builder("a", STANDARD_KIND).observedAt(T).point(1.123456789, 2).put("mag", 4.5)
                        .put("name", "Alpha").build(),
                NormalizedRecord.builder("b", STANDARD_KIND).observedAt(T).point(3, 4).put("mag", 5.5)
                        .put("name", "Bravo, \"quoted\"").build(),
                NormalizedRecord.builder("c", STANDARD_KIND).observedAt(T).put("mag", 6.5).build());
    }

    private String body(String url) throws Exception {
        return new String(mvc.perform(get(url)).andExpect(status().isOk())
                .andExpect(header().exists(RecordsController.WATERMARK_HEADER))
                .andReturn().getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("ndjson: one Record per line, with the watermark header")
    void ndjson() throws Exception {
        mvc.perform(get("/v1/records?format=ndjson&order=id"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.startsWith("application/x-ndjson")));
        List<String> lines = body("/v1/records?format=ndjson&order=id").lines().toList();
        assertThat(lines).hasSize(3);
        JsonNode first = json.readTree(lines.getFirst());
        assertThat(first.get("id").asString()).isEqualTo(STANDARD + ":a");
        assertThat(first.get("payload").get("mag").doubleValue()).isEqualTo(4.5);

        List<String> viaAccept = new String(mvc.perform(get("/v1/records?order=id").header("Accept", "application/x-ndjson"))
                .andReturn().getResponse().getContentAsByteArray(), StandardCharsets.UTF_8).lines().toList();
        assertThat(viaAccept).hasSize(3);
    }

    @Test
    @DisplayName("geojsonseq: RFC 8142 record separators before each Feature")
    void geojsonSeq() throws Exception {
        String body = body("/v1/records?format=geojsonseq&order=id&precision=3");
        String[] records = body.split("\u001e", -1);
        assertThat(records[0]).isEmpty();
        assertThat(records).hasSize(4);
        JsonNode feature = json.readTree(records[1].strip());
        assertThat(feature.get("type").asString()).isEqualTo("Feature");
        assertThat(feature.get("geometry").get("coordinates").get(0).doubleValue()).isEqualTo(1.123);
        assertThat(feature.get("properties").get("externalId").asString()).isEqualTo("a");
        assertThat(json.readTree(records[3].strip()).get("geometry").isNull()).as("null geometry is allowed").isTrue();
    }

    @Test
    @DisplayName("csv: header, WKT geometry and payload columns from fields=")
    void csv() throws Exception {
        String body = body("/v1/records?format=csv&order=id&fields=externalId,title,geometry,payload.mag");
        List<String> lines = body.lines().toList();
        assertThat(lines.getFirst()).isEqualTo("externalId,title,geometry,payload.mag");
        assertThat(lines).hasSize(4);
        assertThat(lines.get(1)).startsWith("a,Alpha,").contains("POINT(1.1234568 2)").endsWith(",4.5");
        assertThat(lines.get(2)).contains("\"Bravo, \"\"quoted\"\"\"");
        assertThat(lines.get(3)).startsWith("c,,,");
    }

    @Test
    @DisplayName("csv without fields= has the default columns and no payload columns")
    void csvDefaults() throws Exception {
        String header = body("/v1/records?format=csv").lines().findFirst().orElseThrow();
        assertThat(header).isEqualTo("id,source,externalId,kind,title,version,seq,observedAt,validUntil,geometry");
    }

    @Test
    @DisplayName("history and changes stream too")
    void historyAndChanges() throws Exception {
        assertThat(body("/v1/history?since=2026-02-28T00:00:00Z&format=ndjson").lines()).hasSize(3);
        assertThat(body("/v1/changes?format=ndjson").lines()).hasSize(3);
        assertThat(body("/v1/changes?format=geojsonseq").split("\u001e")).hasSize(4);
        assertThat(body("/v1/changes?format=csv").lines().findFirst().orElseThrow())
                .isEqualTo("seq,source,externalId,kind,version,observedAt,tombstone,persistReason,validUntil,geometry");
        assertThat(body("/v1/history?since=2026-02-28T00:00:00Z&format=csv&fields=externalId,payload.mag").lines())
                .hasSize(4);
    }

    @Test
    @DisplayName("RESULT_TOO_LARGE over a lowered cap, before anything is written")
    void resultTooLarge() {
        List<RecordRow> written = new ArrayList<>();
        RecordQueryService.RowSink sink = new RecordQueryService.RowSink() {
            @Override
            public void begin(long watermarkSeq) {
                throw new AssertionError("nothing may be written over the cap");
            }

            @Override
            public void accept(RecordRow row) {
                written.add(row);
            }
        };
        assertThatThrownBy(() -> queries.streamCurrent(Queries.current().build(), 2, sink))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getCode()).isEqualTo(ErrorCodes.RESULT_TOO_LARGE));
        assertThatThrownBy(() -> queries.streamHistory(Queries.history(T.minusSeconds(60)).build(), 2, sink))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> queries.streamChanges(Queries.changes(0).build(), 2, sink))
                .isInstanceOf(ValidationException.class);
        assertThat(written).isEmpty();

        List<RecordRow> exact = new ArrayList<>();
        queries.streamCurrent(Queries.current().build(), 3, new RecordQueryService.RowSink() {
            @Override
            public void begin(long watermarkSeq) {}

            @Override
            public void accept(RecordRow row) {
                exact.add(row);
            }
        });
        assertThat(exact).hasSize(3);
    }
}
