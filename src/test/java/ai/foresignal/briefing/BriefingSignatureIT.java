package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ai.foresignal.provenance.JwsVerifier;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prompt 109, task 6: a stored briefing run carries {@code signature}, a detached {@code b64:false} JWS over its
 * markdown bytes, which verifies against the key published at {@code /.well-known/jwks.json} (rebuilt from the
 * JWK's {@code x}, as a stock JOSE library would). The JSON rendition sends the stored signature as
 * {@code Foresignal-Signature}; the HTML rendition signs its own bytes.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class BriefingSignatureIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;

    BriefingHttp http;
    UUID briefingId;
    Map<String, Object> run;

    @BeforeEach
    void setUpRun() {
        http = new BriefingHttp(mvc);
        quake("q1", "M 5.1 near the coast", 5.1);
        briefingId = id(briefings.create(json(digestBody("Signed brief", "0 7 * * *", null)), agentCaller()));
        run = briefings.runNow(briefingId);
    }

    /** The JWKS key with {@code kid}, rebuilt from its raw {@code x}. */
    @SuppressWarnings("unchecked")
    PublicKey published(String kid) throws Exception {
        String body = mvc.perform(get("/.well-known/jwks.json")).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<Map<String, Object>> keys = (List<Map<String, Object>>) mapper.readValue(body, Map.class).get("keys");
        Map<String, Object> jwk = keys.stream().filter(k -> kid.equals(k.get("kid"))).findFirst().orElseThrow();
        byte[] x = Base64.getUrlDecoder().decode((String) jwk.get("x"));
        byte[] prefix = {0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00};
        byte[] spki = new byte[prefix.length + x.length];
        System.arraycopy(prefix, 0, spki, 0, prefix.length);
        System.arraycopy(x, 0, spki, prefix.length, x.length);
        return KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(spki));
    }

    @Test
    @DisplayName("the stored run's signature verifies over its markdown against the published JWKS")
    void storedSignatureVerifies() throws Exception {
        String signature = (String) run.get("signature");
        String markdown = (String) run.get("contentMarkdown");
        assertThat(signature).isNotBlank().contains("..");
        byte[] payload = markdown.getBytes(StandardCharsets.UTF_8);
        PublicKey key = published(JwsVerifier.kid(signature));
        assertThat(JwsVerifier.verify(payload, signature, key)).isTrue();
        assertThat(JwsVerifier.verify((markdown + " ").getBytes(StandardCharsets.UTF_8), signature, key)).isFalse();
    }

    @Test
    @DisplayName("renditions: JSON carries the stored signature; HTML signs its own bytes")
    void renditions() throws Exception {
        String path = "/v1/briefings/" + briefingId + "/runs/" + run.get("id");
        MvcResult json = http.perform(BriefingHttp.request("GET", path, reader, null), 200);
        assertThat(json.getResponse().getHeader("Foresignal-Signature")).isEqualTo(run.get("signature"));

        MvcResult html = http.perform(BriefingHttp.request("GET", path + "?format=html", reader, null)
                .header(HttpHeaders.ACCEPT, "text/html"), 200);
        String signature = html.getResponse().getHeader("Foresignal-Signature");
        byte[] page = html.getResponse().getContentAsString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8);
        assertThat(JwsVerifier.verify(page, signature, published(JwsVerifier.kid(signature)))).isTrue();
    }
}
