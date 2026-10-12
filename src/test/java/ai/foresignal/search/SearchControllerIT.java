package ai.foresignal.search;

import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN;
import static ai.foresignal.testsupport.search.SearchTestSources.PLAIN_KIND;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.search.SearchItSupport;
import ai.foresignal.testsupport.search.SearchTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** The {@code Search} operations over HTTP (prompt 027): validation, status, scopes, reindex and cancel. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(SearchTestSources.class)
class SearchControllerIT extends SearchItSupport {

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;

    String admin;
    String reader;
    String agent;

    @BeforeEach
    void tokens() {
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.bearer(TestTokens.create(tokens, "reader", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "agent", Scope.AGENT));
    }

    ResultActions as(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(bearer == null ? request : request.header(HttpHeaders.AUTHORIZATION, bearer));
    }

    @Test
    @DisplayName("validation: q required and at most 1000 characters; mode, docType, timeField, limit and the window checked")
    void validation() throws Exception {
        as(reader, get("/v1/search")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.errors[0].field", is("q")));
        as(reader, get("/v1/search").param("q", "x".repeat(1001))).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("mode", "fuzzy")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("docType", "tweet")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("timeField", "ingested")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("limit", "0")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("limit", "101")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("bbox", "1,2")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("since", "2026-05-02T00:00:00Z")
                .param("until", "2026-05-01T00:00:00Z")).andExpect(status().isBadRequest());
        as(reader, get("/v1/search").param("q", "x").param("source", "no-such-source")).andExpect(status().isNotFound());
        as(reader, get("/v1/search").param("q", "x").param("source", RecordsTestSources.PRIVATE))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("search always needs a token, even under open reads; a read token gets text results")
    void searchNeedsToken() throws Exception {
        records.ingest(PLAIN, NormalizedRecord.builder("p1", PLAIN_KIND).observedAt(T).put("name", "Harbor crane").build());
        as(null, get("/v1/search").param("q", "crane")).andExpect(status().isUnauthorized());
        as(reader, get("/v1/search").param("q", "crane").param("mode", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode", is("text")))
                .andExpect(jsonPath("$.degraded", is(false)))
                .andExpect(jsonPath("$.items[0].docId", is(PLAIN + ":p1")))
                .andExpect(jsonPath("$.items[0].docType", is("record")))
                .andExpect(jsonPath("$.items[0].record.source", is(PLAIN)));
        as(reader, get("/v1/search").param("q", "crane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode", is("hybrid")))
                .andExpect(jsonPath("$.degraded", is(true)))
                .andExpect(jsonPath("$.reason", containsString("No embedding provider")));
        as(reader, get("/v1/search").param("q", "crane").param("mode", "semantic"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("SEMANTIC_UNAVAILABLE")));
    }

    @Test
    @DisplayName("status: open to anonymous reads, explains why semantic search is off, then reports the active model")
    void statusShape() throws Exception {
        as(null, get("/v1/search/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semanticEnabled", is(false)))
                .andExpect(jsonPath("$.reason", containsString("No embedding provider")))
                .andExpect(jsonPath("$.documents", is(0)))
                .andExpect(jsonPath("$.backlog", is(0)))
                .andExpect(jsonPath("$.reindexing", is(false)))
                .andExpect(jsonPath("$.activeModel", nullValue()))
                .andExpect(jsonPath("$.reindex.state", is("idle")))
                .andExpect(jsonPath("$.degraded", is(false)));

        semanticReady("p8", 8);
        as(reader, get("/v1/search/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semanticEnabled", is(true)))
                .andExpect(jsonPath("$.reason", nullValue()))
                .andExpect(jsonPath("$.activeModel", is(spec("p8").modelId())))
                .andExpect(jsonPath("$.model", is("fake-embed")))
                .andExpect(jsonPath("$.dimensions", is(8)));
    }

    @Test
    @DisplayName("reindex and cancel are admin-only; 202 with the status, 409 REINDEX_RUNNING, then cancel to idle")
    void reindexAndCancel() throws Exception {
        as(reader, post("/v1/search/reindex")).andExpect(status().isForbidden());
        as(agent, post("/v1/search/reindex")).andExpect(status().isForbidden());
        as(null, post("/v1/search/reindex")).andExpect(status().isUnauthorized());
        as(admin, post("/v1/search/reindex")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("SEMANTIC_UNAVAILABLE")));

        semanticReady("p8", 8);
        as(admin, post("/v1/search/reindex"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.reindexing", is(true)))
                .andExpect(jsonPath("$.reindex.state", is("backfilling")))
                .andExpect(jsonPath("$.reindex.toModel", is(spec("p8").modelId())));
        as(admin, post("/v1/search/reindex")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("REINDEX_RUNNING")));

        as(reader, delete("/v1/search/reindex")).andExpect(status().isForbidden());
        as(admin, delete("/v1/search/reindex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reindexing", is(false)))
                .andExpect(jsonPath("$.reindex.state", is("idle")));
        as(admin, delete("/v1/search/reindex")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("/v1/system carries the search block and the search features")
    void systemBlock() throws Exception {
        semanticReady("p8", 8);
        as(admin, get("/v1/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.search.semanticEnabled", is(true)))
                .andExpect(jsonPath("$.search.activeModel", is(spec("p8").modelId())))
                .andExpect(jsonPath("$.search.reindexState", is("idle")))
                .andExpect(jsonPath("$.search.documents", is(0)))
                .andExpect(jsonPath("$.features.search", is(true)))
                .andExpect(jsonPath("$.features.semanticSearch", is(true)));
    }

    @Test
    @DisplayName("the source settings expose and accept embed; true is refused for a source without embedFields")
    void embedSetting() throws Exception {
        as(admin, get("/v1/sources/" + SearchTestSources.NEWS)).andExpect(status().isOk())
                .andExpect(jsonPath("$.settings.embed", is(true)));
        as(admin, get("/v1/sources/" + PLAIN)).andExpect(status().isOk())
                .andExpect(jsonPath("$.settings.embed", is(false)));
        as(admin, org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/sources/" + PLAIN)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"embed\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field", is("embed")));
        as(admin, org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/v1/sources/"
                        + SearchTestSources.OPT_OUT)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"embed\":false}"))
                .andExpect(status().isOk());
        as(admin, get("/v1/sources/" + SearchTestSources.OPT_OUT)).andExpect(status().isOk())
                .andExpect(jsonPath("$.settings.embed", is(false)));
    }
}
