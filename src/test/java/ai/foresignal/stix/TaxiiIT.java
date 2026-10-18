package ai.foresignal.stix;

import static ai.foresignal.testsupport.formats.FormatsTestSources.C2;
import static ai.foresignal.testsupport.formats.FormatsTestSources.KEV;
import static ai.foresignal.testsupport.formats.FormatsTestSources.URLS;
import static ai.foresignal.testsupport.formats.FormatsTestSources.URLS_LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.api.TaxiiController;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.formats.FormatsTestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * TAXII 2.1 and the STIX export over HTTP (prompt 141): discovery, the API root, collections and their 404, the
 * {@code application/taxii+json;version=2.1} media type, {@code added_after} and {@code next} paging with the
 * date-added headers, the agent scope (401 anonymous, 403 for read tokens, no write methods), and
 * {@code GET /v1/export/stix} with the egress filter.
 */
@IntegrationTest
@Import(FormatsTestSources.class)
class TaxiiIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final String ROOT = TaxiiController.API_ROOT;

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired TokenService tokens;

    String agent;
    String reader;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        agent = TestTokens.bearer(TestTokens.create(tokens, "taxii-agent", Scope.AGENT));
        reader = TestTokens.bearer(TestTokens.create(tokens, "taxii-reader", Scope.READ));
    }

    private void ingestAt(Instant at, String source, NormalizedRecord... batch) {
        clock.setInstant(at);
        assertThat(records.ingestAt(source, at, batch).rejections()).isEmpty();
    }

    private static NormalizedRecord cve(String id, Instant at) {
        return NormalizedRecord.builder(id, "vulnerability").observedAt(at).eventTime(at.minusSeconds(86_400))
                .put("cveId", id).put("shortDescription", "Description of " + id).put("kev", true).build();
    }

    private static NormalizedRecord url(String id, String url, Instant at) {
        return NormalizedRecord.builder(id, "malware-url").observedAt(at).eventTime(at).put("url", url)
                .put("urlDefanged", url.replace("http", "hxxp")).put("threat", "malware_download").build();
    }

    private static JsonNode json(MvcResult result) throws Exception {
        return JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private void seed() {
        ingestAt(T.plusSeconds(10), KEV, cve("CVE-2026-0001", T.plusSeconds(10)));
        ingestAt(T.plusSeconds(20), KEV, cve("CVE-2026-0002", T.plusSeconds(20)));
        ingestAt(T.plusSeconds(30), KEV, cve("CVE-2026-0003", T.plusSeconds(30)));
        ingestAt(T.plusSeconds(40), URLS, url("u1", "http://bad.example/a", T.plusSeconds(40)));
        ingestAt(T.plusSeconds(40), URLS_LOCAL, url("u2", "http://local.example/b", T.plusSeconds(40)));
        ingestAt(T.plusSeconds(50), C2, NormalizedRecord.builder("198.51.100.7:8443", "malware-c2")
                .observedAt(T.plusSeconds(50)).put("ip", "198.51.100.7").put("port", 8443).put("malware", "QakBot")
                .build());
        clock.setInstant(T.plusSeconds(3600));
    }

    @Test
    @DisplayName("discovery and API root: TAXII media type, the api_roots link, versions and max_content_length")
    void discoveryAndRoot() throws Exception {
        mvc.perform(get("/taxii2/").header(HttpHeaders.AUTHORIZATION, agent)
                        .header(HttpHeaders.ACCEPT, TaxiiController.MEDIA_TYPE))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(TaxiiController.MEDIA_TYPE)))
                .andExpect(jsonPath("$.title").value("Foresignal TAXII 2.1"))
                .andExpect(jsonPath("$.api_roots", hasSize(1)))
                .andExpect(jsonPath("$.api_roots[0]").value(endsWith("/taxii2/foresignal/")))
                .andExpect(jsonPath("$.default").value(endsWith("/taxii2/foresignal/")));
        mvc.perform(get(ROOT).header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(TaxiiController.MEDIA_TYPE)))
                .andExpect(jsonPath("$.versions[0]").value(TaxiiController.MEDIA_TYPE))
                .andExpect(jsonPath("$.max_content_length").value(0));
    }

    @Test
    @DisplayName("collections: two read-only collections with fixed ids; one by id; an unknown id is 404")
    void collections() throws Exception {
        mvc.perform(get(ROOT + "collections/").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(TaxiiController.MEDIA_TYPE)))
                .andExpect(jsonPath("$.collections", hasSize(2)))
                .andExpect(jsonPath("$.collections[0].id").value(TaxiiCollection.VULNERABILITIES.id()))
                .andExpect(jsonPath("$.collections[0].can_read").value(true))
                .andExpect(jsonPath("$.collections[0].can_write").value(false))
                .andExpect(jsonPath("$.collections[0].media_types[0]").value(StixMapper.STIX_MEDIA_TYPE));
        mvc.perform(get(ROOT + "collections/" + TaxiiCollection.INDICATORS.id() + "/")
                        .header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Indicators"));
        mvc.perform(get(ROOT + "collections/no-such-collection/").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNotFound());
        mvc.perform(get(ROOT + "collections/no-such-collection/objects/").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("objects: limit and next page through every version once; added_after is strict; date-added headers")
    void objectsPaging() throws Exception {
        seed();
        String objects = ROOT + "collections/" + TaxiiCollection.VULNERABILITIES.id() + "/objects/";
        MvcResult first = mvc.perform(get(objects).param("limit", "2").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(TaxiiController.MEDIA_TYPE)))
                .andExpect(header().string(TaxiiController.DATE_ADDED_FIRST, "2026-03-01T00:00:10.000Z"))
                .andExpect(header().string(TaxiiController.DATE_ADDED_LAST, "2026-03-01T00:00:20.000Z"))
                .andReturn();
        JsonNode page1 = json(first);
        assertThat(page1.get("more").asBoolean()).isTrue();
        assertThat(page1.get("objects")).hasSize(2);
        String next = page1.get("next").asString();
        JsonNode page2 = json(mvc.perform(get(objects).param("limit", "2").param("next", next)
                .header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isOk()).andReturn());
        assertThat(page2.get("more").asBoolean()).isFalse();
        assertThat(page2.has("next")).isFalse();
        List<String> names = new ArrayList<>();
        page1.get("objects").forEach(o -> names.add(o.get("name").asString()));
        page2.get("objects").forEach(o -> names.add(o.get("name").asString()));
        assertThat(names).containsExactly("CVE-2026-0001", "CVE-2026-0002", "CVE-2026-0003");
        assertThat(page1.get("objects").get(0).get("type").asString()).isEqualTo("vulnerability");
        assertThat(page1.get("objects").get(0).get("external_references").get(0).get("external_id").asString())
                .isEqualTo("CVE-2026-0001");

        JsonNode after = json(mvc.perform(get(objects).param("added_after", "2026-03-01T00:00:20Z")
                .header(HttpHeaders.AUTHORIZATION, agent)).andExpect(status().isOk()).andReturn());
        assertThat(after.get("objects")).hasSize(1);
        assertThat(after.get("objects").get(0).get("name").asString()).isEqualTo("CVE-2026-0003");

        mvc.perform(get(objects).param("limit", "1001").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isBadRequest());
        mvc.perform(get(objects).param("added_after", "yesterday").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("indicators: url and network-traffic patterns; the LOCAL_ONLY-licensed source is refused and counted")
    void indicatorsAndEgress() throws Exception {
        seed();
        MvcResult result = mvc.perform(get(ROOT + "collections/" + TaxiiCollection.INDICATORS.id() + "/objects/")
                        .header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string("Foresignal-Licence-Filtered", "1"))
                .andReturn();
        JsonNode objects = json(result).get("objects");
        assertThat(objects).hasSize(2);
        assertThat(objects.toString()).contains("[url:value = 'http://bad.example/a']")
                .contains("network-traffic:dst_port = 8443").doesNotContain("local.example");
    }

    @Test
    @DisplayName("scope: anonymous 401, read token 403, no write method; TAXII stays agent under openReads")
    void scope() throws Exception {
        mvc.perform(get("/taxii2/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/taxii2/").header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isForbidden());
        mvc.perform(get(ROOT + "collections/").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isForbidden());
        mvc.perform(post(ROOT + "collections/" + TaxiiCollection.INDICATORS.id() + "/objects/")
                        .header(HttpHeaders.AUTHORIZATION, agent).contentType(TaxiiController.MEDIA_TYPE).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/v1/export/stix")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v1/export/stix").header(HttpHeaders.AUTHORIZATION, reader)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("export: a STIX bundle with the identity, mapped objects, deterministic ids and the egress count")
    void export() throws Exception {
        seed();
        MvcResult result = mvc.perform(get("/v1/export/stix").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, startsWith(StixMapper.STIX_MEDIA_TYPE)))
                .andExpect(header().string("Foresignal-Licence-Filtered", "1"))
                .andReturn();
        JsonNode bundle = json(result);
        assertThat(bundle.get("type").asString()).isEqualTo("bundle");
        assertThat(bundle.get("id").asString()).startsWith("bundle--");
        JsonNode objects = bundle.get("objects");
        assertThat(objects.get(0).get("type").asString()).isEqualTo("identity");
        assertThat(objects).hasSize(1 + 3 + 1 + 1);
        String again = json(mvc.perform(get("/v1/export/stix").header(HttpHeaders.AUTHORIZATION, agent))
                .andReturn()).get("id").asString();
        assertThat(again).as("same data, same bundle id").isEqualTo(bundle.get("id").asString());

        JsonNode onlyVulns = json(mvc.perform(get("/v1/export/stix").param("kind", "vulnerability")
                .param("since", "2026-03-01T00:00:15Z").header(HttpHeaders.AUTHORIZATION, agent)
                .header(HttpHeaders.ACCEPT, "application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andReturn());
        assertThat(onlyVulns.get("objects")).hasSize(1 + 2);

        mvc.perform(get("/v1/export/stix").param("kind", "earthquake").header(HttpHeaders.AUTHORIZATION, agent))
                .andExpect(status().isBadRequest());
    }
}
