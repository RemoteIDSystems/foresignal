package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.export.pmtiles.PmtilesWriter;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Offline packs end to end (prompt 111): an MVT pack for a small box with its PMTiles header and
 * metadata, downloaded with {@code Range}; {@code 413 PACK_TOO_LARGE} with the computed count; the
 * private-source rule.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class PackIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    private static byte[] gunzip(byte[] gz) throws Exception {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
            return in.readAllBytes();
        }
    }

    @Test
    @DisplayName("an MVT pack of a small box: a PMTiles v3 file with MVT tiles, metadata and the sidecars")
    void mvtPack() throws Exception {
        ingestAt(STD, T0, point("p1", STD_KIND, T0, 10.10, 50.10, 1), point("p2", STD_KIND, T0, 10.20, 50.20, 2));
        MvcResult created = mvc.perform(post("/v1/packs").header(HttpHeaders.AUTHORIZATION, agent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Field \\\"pack\\\"\",\"bbox\":[10.0,50.0,10.5,50.5],\"minZoom\":0,"
                                + "\"maxZoom\":4,\"vector\":{\"sources\":[\"" + STD + "\"]}}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.kind").value("PACK"))
                .andExpect(jsonPath("$.format").value("pmtiles"))
                .andReturn();
        String id = JSON.readTree(created.getResponse().getContentAsString()).get("id").asString();
        ExportJobEntity job = awaitTerminal(id);
        assertThat(job.getStatus()).as(job.getError()).isEqualTo(ExportJobEntity.DONE);
        assertThat(job.getRowCount()).isPositive();
        JsonNode view = JSON.readTree(mvc.perform(get("/v1/exports/" + id).header(HttpHeaders.AUTHORIZATION, agent))
                .andReturn().getResponse().getContentAsString());
        assertThat(view.get("files").get(0).get("name").asString()).isEqualTo("vector.pmtiles");

        byte[] archive = mvc.perform(get("/v1/exports/" + id + "/download?file=vector.pmtiles")
                        .header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        ByteBuffer header = ByteBuffer.wrap(archive).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(new String(archive, 0, 7, StandardCharsets.US_ASCII)).isEqualTo("PMTiles");
        assertThat(archive[7]).isEqualTo((byte) 3);
        assertThat(archive[96]).as("clustered").isEqualTo((byte) 1);
        assertThat(archive[98]).isEqualTo((byte) PmtilesWriter.COMPRESSION_GZIP);
        assertThat(archive[99]).isEqualTo((byte) PmtilesWriter.TYPE_MVT);
        assertThat(header.getLong(72)).as("addressed tiles").isEqualTo(job.getRowCount());
        assertThat(127 + header.getLong(16)).isLessThanOrEqualTo(16_384L);
        byte[] metadata = new byte[(int) header.getLong(32)];
        System.arraycopy(archive, (int) header.getLong(24), metadata, 0, metadata.length);
        JsonNode json = JSON.readTree(gunzip(metadata));
        assertThat(json.get("name").asString()).isEqualTo("Field \"pack\"");
        assertThat(json.get("vector_layers").get(0).get("id").asString()).isEqualTo(PackService.VECTOR_LAYER);
        assertThat(json.get("attribution").asString()).contains("Data: " + STD);
        assertThat(json.get("watermarkSeq").asLong()).isPositive();
        assertThat(json.has("generated")).isTrue();

        MvcResult range = mvc.perform(get("/v1/exports/" + id + "/download?file=vector.pmtiles")
                        .header(HttpHeaders.AUTHORIZATION, agent).header(HttpHeaders.RANGE, "bytes=0-126"))
                .andExpect(status().isPartialContent()).andReturn();
        assertThat(range.getResponse().getContentAsByteArray()).hasSize(127);
        mvc.perform(get("/v1/exports/" + id + "/download?file=manifest.json").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("over packs.maxTiles the pack is 413 PACK_TOO_LARGE with the computed tile count")
    void tooLarge() throws Exception {
        mvc.perform(post("/v1/packs").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"world\",\"bbox\":[-180,-85,180,85],\"minZoom\":0,\"maxZoom\":14,"
                                + "\"vector\":{\"sources\":[\"" + STD + "\"]}}"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("PACK_TOO_LARGE"))
                .andExpect(jsonPath("$.tileCount").isNumber());
        assertThat(PackService.countTiles(new double[] {-180, -85, 180, 85}, 0, 1)).isEqualTo(5L);
    }

    @Test
    @DisplayName("includePrivate needs admin; a pack of only forbidden sources is 400; anonymous callers are 401")
    void rules() throws Exception {
        mvc.perform(post("/v1/packs").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"p\",\"bbox\":[10,50,10.1,50.1],\"minZoom\":0,\"maxZoom\":1,\"includePrivate\":true,"
                        + "\"vector\":{\"sources\":[\"" + SECRET + "\"]}}")).andExpect(status().isForbidden());
        mvc.perform(post("/v1/packs").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"p\",\"bbox\":[10,50,10.1,50.1],\"minZoom\":0,\"maxZoom\":1,"
                                + "\"vector\":{\"sources\":[\"" + FORBIDDEN + "\"]}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EXPORT_SPEC_INVALID"));
        mvc.perform(post("/v1/packs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/v1/packs").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"p\",\"minZoom\":0,\"maxZoom\":20}")).andExpect(status().isBadRequest());
    }
}
