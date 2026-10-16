package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.provenance.JwsVerifier;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.export.ExportItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The export license and privacy rules end to end (prompt 111): a FORBIDDEN source is excluded and
 * listed, {@code personalUse} includes it with the notice in the sidecars, a private source needs
 * admin and {@code includePrivate}, and the manifest's detached JWS verifies against the JWKS key.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ExportLicenceIT extends ExportItSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired ExportStore store;
    @Autowired SigningKeyService keys;

    @BeforeEach
    void seed() {
        ingestAt(STD, T0, point("open", STD_KIND, T0, 10, 50, 1));
        ingestAt(FORBIDDEN, T0, point("closed", STD_KIND, T0, 11, 51, 2));
        ingestAt(SECRET, T0, point("hidden", STD_KIND, T0, 12, 52, 3));
    }

    private ExportJobEntity export(String bearer, String body) throws Exception {
        MvcResult result = mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isAccepted()).andReturn();
        return awaitTerminal(JSON.readTree(result.getResponse().getContentAsString()).get("id").asString());
    }

    private JsonNode license(ExportJobEntity job) throws Exception {
        return JSON.readTree(Files.readAllBytes(store.dirOf(job).resolve("licence.json")));
    }

    @Test
    @DisplayName("a FORBIDDEN source is left out and listed as excluded")
    void forbiddenExcluded() throws Exception {
        ExportJobEntity job = export(agent, "{\"sources\":[\"" + STD + "\",\"" + FORBIDDEN + "\"]}");
        assertThat(job.getStatus()).isEqualTo(ExportJobEntity.DONE);
        assertThat(job.getRowCount()).isEqualTo(1L);
        assertThat(job.isPersonalUse()).isFalse();
        JsonNode license = license(job);
        assertThat(license.get("excluded").get(0).get("sourceId").asString()).isEqualTo(FORBIDDEN);
        assertThat(license.get("excluded").get(0).get("reason").asString()).isEqualTo("licence-forbidden");
        assertThat(license.get("personalUseOnly").asBoolean()).isFalse();
        mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sources\":[\"" + FORBIDDEN + "\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EXPORT_SPEC_INVALID"));
    }

    @Test
    @DisplayName("personalUse includes it and the sidecars say personal use only")
    void personalUse() throws Exception {
        ExportJobEntity job = export(agent, "{\"sources\":[\"" + STD + "\",\"" + FORBIDDEN + "\"],\"personalUse\":true}");
        assertThat(job.getRowCount()).isEqualTo(2L);
        assertThat(job.isPersonalUse()).isTrue();
        JsonNode license = license(job);
        assertThat(license.get("personalUseOnly").asBoolean()).isTrue();
        assertThat(license.get("personalUseSources").toString()).contains(FORBIDDEN);
        String attribution = Files.readString(store.dirOf(job).resolve("ATTRIBUTION.txt"));
        assertThat(attribution).contains("Personal use only — do not redistribute").contains(FORBIDDEN);
    }

    @Test
    @DisplayName("a private source needs admin and includePrivate; otherwise it is absent or refused")
    void privateSources() throws Exception {
        ExportJobEntity agentJob = export(agent, "{\"sources\":[\"" + STD + "\",\"" + SECRET + "\"]}");
        assertThat(agentJob.getRowCount()).isEqualTo(1L);
        assertThat(license(agentJob).get("excluded").toString()).as("not even named to an agent").doesNotContain(SECRET);
        mvc.perform(post("/v1/exports").header(HttpHeaders.AUTHORIZATION, agent).contentType(MediaType.APPLICATION_JSON)
                .content("{\"sources\":[\"" + SECRET + "\"],\"includePrivate\":true}")).andExpect(status().isForbidden());
        ExportJobEntity adminWithout = export(admin, "{\"sources\":[\"" + STD + "\",\"" + SECRET + "\"]}");
        assertThat(adminWithout.getRowCount()).isEqualTo(1L);
        assertThat(license(adminWithout).get("excluded").toString()).contains("private");
        ExportJobEntity adminWith = export(admin, "{\"sources\":[\"" + SECRET + "\"],\"includePrivate\":true}");
        assertThat(adminWith.getRowCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("the manifest's detached JWS verifies against the JWKS public key and fails on a tampered body")
    void manifestSignature() throws Exception {
        ExportJobEntity job = export(agent, "{\"sources\":[\"" + STD + "\"]}");
        Path dir = store.dirOf(job);
        byte[] manifest = Files.readAllBytes(dir.resolve("manifest.json"));
        String jws = Files.readString(dir.resolve("manifest.json.jws"));
        PublicKey key = keys.publicKeys().get(JwsVerifier.kid(jws));
        assertThat(key).isNotNull();
        assertThat(JwsVerifier.verify(manifest, jws, key)).isTrue();
        byte[] tampered = manifest.clone();
        tampered[tampered.length / 2] ^= 1;
        assertThat(JwsVerifier.verify(tampered, jws, key)).isFalse();
        JsonNode body = JSON.readTree(manifest);
        assertThat(body.get("signature").get("kid").asString()).isEqualTo(JwsVerifier.kid(jws));
        String jwks = mvc.perform(get("/.well-known/jwks.json")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        assertThat(jwks).contains(JwsVerifier.kid(jws));
        String sha = ExportManifests.sha256(manifest);
        assertThat(job.getManifestSha256()).isEqualTo(sha);
    }
}
