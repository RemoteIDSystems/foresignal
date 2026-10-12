package ai.foresignal.api;

import static ai.foresignal.testsupport.records.RecordsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.records.RecordsTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/** {@code GET /v1/kinds/{kind}/schema}: the merged JSON Schema, conflicts, 404 and ETag. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RecordsTestSources.class)
class KindSchemaIT {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("merges the fieldSpecs of every source emitting the kind; a type conflict becomes oneOf")
    void mergeAndConflicts() throws Exception {
        mvc.perform(get("/v1/kinds/" + SCHEMA_KIND + "/schema"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$['$schema']").value("https://json-schema.org/draft/2020-12/schema"))
                .andExpect(jsonPath("$.title").value(SCHEMA_KIND))
                .andExpect(jsonPath("$.type").value("object"))
                .andExpect(jsonPath("$.properties.depth.type").value("number"))
                .andExpect(jsonPath("$.properties.depth['x-unit']").value("km"))
                .andExpect(jsonPath("$.properties.depth['x-label']").value("Depth"))
                .andExpect(jsonPath("$.properties.time['x-format']").value("timestamp"))
                .andExpect(jsonPath("$.properties.mag.oneOf[0].type").value("number"))
                .andExpect(jsonPath("$.properties.mag.oneOf[0]['x-sources']", contains(SCHEMA_A)))
                .andExpect(jsonPath("$.properties.mag.oneOf[1].type").value("string"))
                .andExpect(jsonPath("$.properties.mag.oneOf[1]['x-sources']", contains(SCHEMA_B)))
                .andExpect(jsonPath("$['x-conflicts']", contains(SCHEMA_A, SCHEMA_B)))
                .andExpect(jsonPath("$['x-display'].icon").value("earthquake"))
                .andExpect(jsonPath("$.properties.other").doesNotExist());
    }

    @Test
    @DisplayName("a field limited to another kind belongs to that kind's schema only")
    void kindScopedField() throws Exception {
        mvc.perform(get("/v1/kinds/" + OTHER_KIND + "/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.properties.other.type").value("boolean"))
                .andExpect(jsonPath("$['x-conflicts']").isEmpty());
    }

    @Test
    @DisplayName("ETag and 304")
    void etag() throws Exception {
        String etag = mvc.perform(get("/v1/kinds/" + SCHEMA_KIND + "/schema")).andReturn().getResponse()
                .getHeader(HttpHeaders.ETAG);
        mvc.perform(get("/v1/kinds/" + SCHEMA_KIND + "/schema").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
    }

    @Test
    @DisplayName("404 for a kind no source emits")
    void unknownKind() throws Exception {
        mvc.perform(get("/v1/kinds/no-such-kind/schema"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
