package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/** {@code GET /v1/changes} through the real filter chain, checked against {@code ChangePage} and {@code Change}. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class ChangesControllerIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        retention.reset();
        records.ingestAt(STANDARD, T,
                NormalizedRecord.builder("a", STANDARD_KIND).observedAt(T).point(1, 2).put("k", "v").build(),
                NormalizedRecord.builder("b", STANDARD_KIND).observedAt(T).validUntil(T.plusSeconds(3600)).build());
        records.ingestAt(RecordsTestSources.RASTER, T,
                NormalizedRecord.builder("mrms", "raster-frame").observedAt(T).put("frameKey", "f1").build());
    }

    @AfterEach
    void tearDown() {
        retention.reset();
    }

    @Test
    @DisplayName("a page carries the spec fields")
    void page() throws Exception {
        mvc.perform(get("/v1/changes"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RecordsController.WATERMARK_HEADER))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[0].seq").isNumber())
                .andExpect(jsonPath("$.items[0].source").value(STANDARD))
                .andExpect(jsonPath("$.items[0].externalId").value("a"))
                .andExpect(jsonPath("$.items[0].kind").value(STANDARD_KIND))
                .andExpect(jsonPath("$.items[0].version").value(1))
                .andExpect(jsonPath("$.items[0].observedAt").value("2026-03-01T00:00:00Z"))
                .andExpect(jsonPath("$.items[0].tombstone").value(false))
                .andExpect(jsonPath("$.items[0].geometry.type").value("Point"))
                .andExpect(jsonPath("$.items[0].prevSeq").value(nullValue()))
                .andExpect(jsonPath("$.items[0].historyTier").value("STANDARD"))
                .andExpect(jsonPath("$.items[0].persistReason").value("FIRST"))
                .andExpect(jsonPath("$.items[0].qaFlags").value(0))
                .andExpect(jsonPath("$.items[0].bbox[0]").value(1.0))
                .andExpect(jsonPath("$.items[0].payload").doesNotExist())
                .andExpect(jsonPath("$.items[1].validUntil").value("2026-03-01T01:00:00Z"))
                .andExpect(jsonPath("$.items[2].payload.frameKey").value("f1"))
                .andExpect(jsonPath("$.lastSeq").isNumber())
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.watermarkSeq").isNumber())
                .andExpect(jsonPath("$.purgeHorizon").value(0));
    }

    @Test
    @DisplayName("payload=true, externalId, limit and fields")
    void parameters() throws Exception {
        mvc.perform(get("/v1/changes").param("payload", "true").param("externalId", "a"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].payload.k").value("v"));
        mvc.perform(get("/v1/changes").param("limit", "1"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.hasMore").value(true));
        mvc.perform(get("/v1/changes").param("fields", "seq,externalId"))
                .andExpect(jsonPath("$.items[0].externalId").value("a"))
                .andExpect(jsonPath("$.items[0].kind").doesNotExist());
        mvc.perform(get("/v1/changes").param("source", STANDARD).param("kind", STANDARD_KIND).param("bbox", "0,0,5,5"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("410 RESYNC_REQUIRED below the purge horizon, with extensions")
    void resync() throws Exception {
        retention.setPurgeSeq(STANDARD, 1_000_000);
        mvc.perform(get("/v1/changes").param("after", "5"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("RESYNC_REQUIRED"))
                .andExpect(jsonPath("$.purgeHorizon").value(1_000_000))
                .andExpect(jsonPath("$.watermarkSeq").isNumber());
        mvc.perform(get("/v1/changes").param("after", "5").param("format", "ndjson")).andExpect(status().isGone());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "after=-1", "after=x", "limit=0", "limit=10001", "payload=perhaps", "bbox=1,2", "fields=nope", "format=geojson"
    })
    @DisplayName("400 for malformed parameters")
    void badRequests(String query) throws Exception {
        mvc.perform(get("/v1/changes?" + query)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("404 for an unknown source filter")
    void unknownSource() throws Exception {
        mvc.perform(get("/v1/changes").param("source", "no-such-source")).andExpect(status().isNotFound());
    }
}
