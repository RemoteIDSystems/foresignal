package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.user.BuiltInRole;
import ai.foresignal.security.user.PasswordHasher;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.security.user.UserRoleRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.testsupport.security.TestUsers;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

/**
 * The hosted edition site (prompt NP-005): the latest successful run as a standalone HTML page with conditional GET,
 * the {@code EditionSite} JSON with its canonical, feed-key and share URLs, access through the feed key and the share
 * link and nowhere else, a private (user-owned) edition gated to its owner and its own keys, and keys revoked when the
 * site, sharing or the edition goes.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class EditionSiteIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired PrincipalService principals;
    @Autowired UserRepository users;
    @Autowired UserRoleRepository userRoles;
    @Autowired PasswordHasher hasher;

    BriefingHttp http;

    @BeforeEach
    void setUpHttp() {
        http = new BriefingHttp(mvc);
    }

    static final String SITE = "\"site\":{\"enabled\":true,\"shareable\":true}";

    private String create(String bearer, String extra) throws Exception {
        return http.call("POST", "/v1/briefings", bearer, digestBody("Daily paper", "0 7 * * *", extra), 201).get("id")
                .asString();
    }

    private MvcResult get(String path, String bearer, int status) throws Exception {
        return http.perform(BriefingHttp.request("GET", path, bearer, null), status);
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private JsonNode site(String id, String bearer) throws Exception {
        return http.call("GET", "/v1/editions/" + id + "/site?format=json", bearer, null, 200);
    }

    private static String path(String url) {
        return URI.create(url).getRawPath();
    }

    private String login(String email) throws Exception {
        return "Bearer " + http.call("POST", "/v1/auth/login", null, TestUsers.loginBody(email), 200).get("token")
                .asString();
    }

    @Test
    @DisplayName("the site is the latest successful run as a standalone HTML page, with ETag, Last-Modified and 304")
    void standalonePage() throws Exception {
        String id = create(agent, SITE);
        get("/v1/editions/" + id + "/site", reader, 404);

        quake("site-1", "Kobe", 5.1);
        http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        MvcResult page = get("/v1/editions/" + id + "/site", reader, 200);
        String html = body(page);
        assertThat(page.getResponse().getContentType()).startsWith("text/html");
        assertThat(html).startsWith("<!DOCTYPE html>").contains("<title>Daily paper</title>").contains("Kobe")
                .endsWith("</html>\n");
        assertThat(page.getResponse().getHeader("Content-Security-Policy"))
                .isEqualTo(BriefingHtmlRenderer.CONTENT_SECURITY_POLICY);
        String etag = page.getResponse().getHeader(HttpHeaders.ETAG);
        String modified = page.getResponse().getHeader(HttpHeaders.LAST_MODIFIED);
        assertThat(etag).isNotBlank();
        assertThat(modified).isNotBlank();

        http.perform(BriefingHttp.request("GET", "/v1/editions/" + id + "/site", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 304);
        http.perform(BriefingHttp.request("GET", "/v1/editions/" + id + "/site", reader, null)
                .header(HttpHeaders.IF_MODIFIED_SINCE, modified), 304);

        clock.advance(java.time.Duration.ofMinutes(5));
        quake("site-2", "Sendai", 6.0);
        http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        MvcResult newer = http.perform(BriefingHttp.request("GET", "/v1/editions/" + id + "/site", reader, null)
                .header(HttpHeaders.IF_NONE_MATCH, etag), 200);
        assertThat(body(newer)).contains("Sendai");
    }

    @Test
    @DisplayName("EditionSite: canonical, feed-key and share URLs; the feed key and share link reach the site only")
    void keys() throws Exception {
        String id = create(agent, SITE);
        quake("site-3", "Nagoya", 5.4);
        JsonNode run = http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);

        JsonNode site = site(id, agent);
        assertThat(path(site.get("url").asString())).isEqualTo("/v1/editions/" + id + "/site");
        assertThat(site.get("latestRunId").asString()).isEqualTo(run.get("id").asString());
        String feed = path(site.get("feedKeyUrl").asString());
        String share = path(site.get("shareUrl").asString());
        assertThat(feed).startsWith("/v1/k/fsf_").endsWith("/editions/" + id + "/site");
        assertThat(share).startsWith("/v1/k/fss_").endsWith("/site");

        assertThat(body(get(feed, null, 200))).contains("Nagoya");
        assertThat(body(get(share, null, 200))).contains("Nagoya");

        String feedKey = feed.split("/")[3];
        String shareKey = share.split("/")[3];
        get("/v1/k/" + feedKey + "/briefings/" + id, null, 403);
        get("/v1/k/" + feedKey + "/briefings", null, 403);
        get("/v1/k/" + shareKey + "/report", null, 404);
        http.perform(BriefingHttp.request("POST", share, null, null), 405);

        // A path-key caller never sees the key URLs in the JSON.
        JsonNode viaKey = http.call("GET", "/v1/k/" + feedKey + "/editions/" + id + "/site?format=json", null, null,
                200);
        assertThat(viaKey.get("feedKeyUrl").isNull()).isTrue();
        assertThat(viaKey.get("shareUrl").isNull()).isTrue();

        // The keys stay the same across saves (a stable address).
        http.call("PUT", "/v1/briefings/" + id, agent, digestBody("Daily paper", "0 8 * * *", SITE), 200);
        assertThat(path(site(id, agent).get("feedKeyUrl").asString())).isEqualTo(feed);
        assertThat(path(site(id, agent).get("shareUrl").asString())).isEqualTo(share);
    }

    @Test
    @DisplayName("a private edition's site is reachable only by its owner, its feed key and its share link")
    void privateEdition() throws Exception {
        TestUsers.active(principals, users, userRoles, hasher, "site-owner@example.com", BuiltInRole.EDITOR);
        TestUsers.active(principals, users, userRoles, hasher, "site-other@example.com", BuiltInRole.EDITOR);
        String owner = login("site-owner@example.com");
        String other = login("site-other@example.com");
        String id = create(owner, SITE);
        quake("site-4", "Fukuoka", 5.0);
        http.call("POST", "/v1/briefings/" + id + "/run", owner, null, 200);

        get("/v1/editions/" + id + "/site", owner, 200);
        get("/v1/editions/" + id + "/site", other, 404);
        // Anonymous: 401 while open reads are off, 404 (an owned edition is never open) while they are on.
        assertThat(mvc.perform(BriefingHttp.request("GET", "/v1/editions/" + id + "/site", null, null)).andReturn()
                .getResponse().getStatus()).isIn(401, 404);

        JsonNode site = site(id, owner);
        assertThat(body(get(path(site.get("feedKeyUrl").asString()), null, 200))).contains("Fukuoka");
        assertThat(body(get(path(site.get("shareUrl").asString()), null, 200))).contains("Fukuoka");
        http.call("GET", "/v1/editions/" + id + "/site?format=json", other, null, 404);
    }

    @Test
    @DisplayName("turning sharing off revokes the link (410); turning the site off is 404; deleting revokes the keys")
    void revocation() throws Exception {
        String id = create(agent, SITE);
        quake("site-5", "Sapporo", 5.2);
        http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        JsonNode site = site(id, agent);
        String feed = path(site.get("feedKeyUrl").asString());
        String share = path(site.get("shareUrl").asString());

        http.call("PUT", "/v1/briefings/" + id, agent, digestBody("Daily paper", "0 7 * * *",
                "\"site\":{\"enabled\":true,\"shareable\":false}"), 200);
        get(share, null, 410);
        assertThat(site(id, agent).get("shareUrl").isNull()).isTrue();
        get(feed, null, 200);

        http.call("PUT", "/v1/briefings/" + id, agent, digestBody("Daily paper", "0 7 * * *", null), 200);
        get("/v1/editions/" + id + "/site", reader, 404);
        get(feed, null, 410);

        http.call("PUT", "/v1/briefings/" + id, agent, digestBody("Daily paper", "0 7 * * *", SITE), 200);
        String renewed = path(site(id, agent).get("feedKeyUrl").asString());
        assertThat(renewed).isNotEqualTo(feed);
        get(renewed, null, 200);
        http.call("DELETE", "/v1/briefings/" + id, agent, null, 204);
        get(renewed, null, 410);
    }

    @Test
    @DisplayName("a share link revoked by an operator stays revoked and is not silently replaced")
    void operatorRevocation() throws Exception {
        String id = create(agent, SITE);
        quake("site-6", "Okinawa", 5.3);
        http.call("POST", "/v1/briefings/" + id + "/run", agent, null, 200);
        String share = path(site(id, agent).get("shareUrl").asString());
        String linkId = jdbc.queryForObject("SELECT key_id FROM edition_site_key WHERE briefing_id = ?::uuid"
                + " AND kind = 'share'", String.class, id);

        http.call("DELETE", "/v1/share-links/" + linkId, admin, null, 204);
        get(share, null, 410);
        http.call("PUT", "/v1/briefings/" + id, agent, digestBody("Daily paper", "0 9 * * *", SITE), 200);
        assertThat(site(id, agent).get("shareUrl").isNull()).isTrue();
    }
}
