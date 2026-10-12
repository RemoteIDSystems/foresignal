package ai.foresignal.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.openapi.OpenApiSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * How the authored spec and Swagger UI are served (prompt 020): {@code GET /v1/openapi.json} is open,
 * {@code application/json} and byte-identical to {@code docs/OpenAPI.json}; {@code /docs} reaches
 * Swagger UI, which points at the authored document only.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class OpenApiServingIT {

    @Autowired private MockMvc mvc;

    @Test
    @DisplayName("GET /v1/openapi.json needs no token and returns application/json")
    void openAndJson() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v1/openapi.json"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(MediaType.parseMediaType(response.getContentType()).isCompatibleWith(MediaType.APPLICATION_JSON))
                .as(response.getContentType()).isTrue();
    }

    @Test
    @DisplayName("the served bytes equal docs/OpenAPI.json, server URL included")
    void byteIdentical() throws Exception {
        byte[] served = mvc.perform(get("/v1/openapi.json")).andReturn().getResponse().getContentAsByteArray();

        assertThat(served).isEqualTo(OpenApiSpec.bytes());
        assertThat(OpenApiSpec.JSON.readTree(served).get("servers"))
                .isEqualTo(OpenApiSpec.document().get("servers"));
    }

    @Test
    @DisplayName("an invalid bearer token does not block the open document")
    void invalidTokenStillOpen() throws Exception {
        mvc.perform(get("/v1/openapi.json").header(HttpHeaders.AUTHORIZATION, "Bearer fsr_not-a-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/docs answers 200 or redirects to the Swagger UI index")
    void docsReachable() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/docs")).andReturn().getResponse();

        assertThat(response.getStatus()).isIn(200, 301, 302, 303, 307, 308);
        if (response.getStatus() != 200) {
            assertThat(response.getHeader(HttpHeaders.LOCATION)).contains("swagger-ui");
        }
    }

    @Test
    @DisplayName("Swagger UI's config points at /v1/openapi.json and is open")
    void swaggerConfigPointsAtTheAuthoredSpec() throws Exception {
        String config = mvc.perform(get("/v1/openapi-generated.json/swagger-config"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(config).contains("/v1/openapi.json");
    }
}
