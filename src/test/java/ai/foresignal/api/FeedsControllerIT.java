package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingRunRepository;
import ai.foresignal.feed.AtomFeedWriter;
import ai.foresignal.feed.GeoRss;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.w3c.dom.Document;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Atom and JSON Feed over HTTP (prompt 058): filters, conditional GET, feed keys and private exclusion. */
@IntegrationTest
@Import(TriggerTestSources.class)
class FeedsControllerIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired FeedKeyService feedKeys;
    @Autowired BriefingRunRepository runs;
    @Autowired JdbcTemplate jdbc;

    TriggerFixture fixture;
    UUID warning;
    UUID critical;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        warning = fixture.rule("{'name':'Warn','sources':['" + STANDARD + "'],'tags':['ops'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}");
        critical = fixture.rule("{'name':'Crit','sources':['" + STANDARD + "'],'severity':'critical',"
                + "'conditions':[{'path':'mag','op':'>=','value':7}]}");
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        settings.invalidateCache();
    }

    void quake(String id, double mag) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).point(-97.5, 30.25)
                .put("mag", mag).build());
    }

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    static int entries(String atom) throws Exception {
        return parse(atom).getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength();
    }

    String atom(String query) throws Exception {
        return mvc.perform(get("/v1/alerts/feed.atom" + query)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Atom: media type, tag ids, GeoRSS point, categories; filters by severity, rule, state and limit")
    void atomAndFilters() throws Exception {
        quake("a", 5.5);
        quake("b", 7.5);
        fixture.settle();
        MvcResult result = mvc.perform(get("/v1/alerts/feed.atom"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/atom+xml;charset=UTF-8"))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().exists(HttpHeaders.LAST_MODIFIED))
                .andReturn();
        String xml = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Document doc = parse(xml);
        assertThat(doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength()).isEqualTo(3);
        assertThat(doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "id").item(1).getTextContent())
                .startsWith("tag:foresignal,2026:").contains("/alert/");
        assertThat(doc.getElementsByTagNameNS(GeoRss.NAMESPACE, "point").item(0).getTextContent())
                .isEqualTo("30.25 -97.5");
        assertThat(xml).contains("term=\"critical\"").contains("term=\"ops\"");
        assertThat(entries(atom("?minSeverity=critical"))).isEqualTo(1);
        assertThat(entries(atom("?rule=" + warning))).isEqualTo(2);
        assertThat(entries(atom("?state=resolved"))).isZero();
        assertThat(entries(atom("?source=other"))).isZero();
        assertThat(entries(atom("?kind=" + STANDARD_KIND + "&limit=1"))).isEqualTo(1);
        mvc.perform(get("/v1/alerts/feed.atom?limit=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/alerts/feed.atom?limit=201")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/alerts/feed.atom?rule=nope")).andExpect(status().isBadRequest());
        mvc.perform(get("/v1/alerts/feed.atom?state=open")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON Feed: media type, version, items with the _foresignal extension")
    void jsonFeed() throws Exception {
        quake("j", 7.5);
        fixture.settle();
        MvcResult result = mvc.perform(get("/v1/alerts/feed.json?minSeverity=critical"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/feed+json;charset=UTF-8"))
                .andReturn();
        JsonNode feed = JSON.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(feed.get("version").asString()).isEqualTo("https://jsonfeed.org/version/1.1");
        JsonNode item = feed.get("items").get(0);
        assertThat(item.get("title").asString()).isEqualTo("Crit");
        assertThat(item.get("url").asString()).startsWith("http://localhost/v1/alerts/");
        assertThat(item.get("_foresignal").get("severity").asString()).isEqualTo("critical");
        assertThat(item.get("_foresignal").get("state").asString()).isEqualTo("firing");
        assertThat(item.get("_foresignal").get("sourceId").asString()).isEqualTo(STANDARD);
        assertThat(item.get("_foresignal").get("externalId").asString()).isEqualTo("j");
        assertThat(item.get("_foresignal").get("geometry").get("type").asString()).isEqualTo("Point");
        assertThat(item.get("date_published").asString()).isEqualTo(item.get("date_modified").asString());
    }

    @Test
    @DisplayName("conditional GET: If-None-Match and If-Modified-Since give 304 until a lifecycle change")
    void conditionalGet() throws Exception {
        quake("c", 5.5);
        fixture.settle();
        MvcResult first = mvc.perform(get("/v1/alerts/feed.atom")).andExpect(status().isOk()).andReturn();
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        String lastModified = first.getResponse().getHeader(HttpHeaders.LAST_MODIFIED);
        mvc.perform(get("/v1/alerts/feed.atom").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, etag));
        mvc.perform(get("/v1/alerts/feed.atom").header(HttpHeaders.IF_MODIFIED_SINCE, lastModified))
                .andExpect(status().isNotModified());
        // Another filter is another document.
        mvc.perform(get("/v1/alerts/feed.atom?limit=10").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isOk());
        // A lifecycle change (acknowledge) moves the newest change and the tag.
        long id = fixture.alerts(warning).getFirst().id();
        clock.setInstant(T.plusSeconds(300));
        jdbc.update("UPDATE alert SET acknowledged_at = ?, state = 'acknowledged' WHERE id = ?",
                java.sql.Timestamp.from(T.plusSeconds(300)), id);
        MvcResult changed = mvc.perform(get("/v1/alerts/feed.atom").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isOk()).andReturn();
        assertThat(changed.getResponse().getHeader(HttpHeaders.ETAG)).isNotEqualTo(etag);
        mvc.perform(get("/v1/alerts/feed.atom").header(HttpHeaders.IF_MODIFIED_SINCE, lastModified))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("with openReads off a feed key reads the feed through its path; no credential is 401")
    void feedKey() throws Exception {
        quake("k", 5.5);
        fixture.settle();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        FeedKeyService.CreatedKey key = feedKeys.create(JSON.readTree("{\"name\":\"reader\","
                + "\"prefixes\":[\"/v1/alerts/feed.atom\"],\"restrictions\":{\"sources\":[\"" + STANDARD + "\"]},"
                + "\"expiresAt\":\"" + T.plus(Duration.ofDays(30)) + "\"}"));
        mvc.perform(get("/v1/alerts/feed.atom")).andExpect(status().isUnauthorized());
        MvcResult keyed = mvc.perform(get("/v1/k/" + key.key() + "/alerts/feed.atom"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-cache"))
                .andReturn();
        String xml = keyed.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(entries(xml)).isEqualTo(1);
        // The self link keeps the key so the reader can refetch it; entry links never carry it.
        assertThat(xml).contains("/v1/k/" + key.key() + "/alerts/feed.atom");
        assertThat(parse(xml).getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").item(0).getTextContent())
                .doesNotContain(key.key());
        mvc.perform(get("/v1/k/" + key.key() + "/alerts/feed.json")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("alerts of private sources appear only for callers that pass the private gate")
    void privateExclusion() throws Exception {
        UUID rule = fixture.rule("{'name':'Mine','sources':['" + PRIVATE + "']}");
        records.ingest(PRIVATE, NormalizedRecord.builder("p", PRIVATE_KIND).observedAt(T).point(1, 1).build());
        fixture.settle();
        assertThat(entries(atom("?rule=" + rule))).isZero();
        String token = adminTokens.rotate();
        String admin = mvc.perform(get("/v1/alerts/feed.atom?rule=" + rule)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-cache"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(entries(admin)).isEqualTo(1);
    }

    @Test
    @DisplayName("briefing run feeds: finished runs newest first with the whole markdown; unknown briefings are 404")
    void briefingRuns() throws Exception {
        UUID briefing = UUID.randomUUID();
        BriefingRunEntity older = new BriefingRunEntity(UUID.randomUUID(), briefing, "Morning", "digest", "UTC",
                T.minusSeconds(7200), T.minusSeconds(86_400), T.minusSeconds(7200));
        older.setStatus(BriefingRunEntity.SUCCEEDED);
        older.setFinishedAt(T.minusSeconds(7100));
        older.setContentMarkdown("## Older\n\n<script>alert(1)</script> text");
        BriefingRunEntity newer = new BriefingRunEntity(UUID.randomUUID(), briefing, "Morning", "digest", "UTC",
                T.minusSeconds(60), T.minusSeconds(3600), T.minusSeconds(60));
        newer.setStatus(BriefingRunEntity.SUCCEEDED);
        newer.setFinishedAt(T);
        newer.setContentMarkdown("### Newer\n\n**bold**");
        runs.save(older);
        runs.save(newer);
        String xml = mvc.perform(get("/v1/briefings/" + briefing + "/runs/feed.atom"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/atom+xml;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Document doc = parse(xml);
        assertThat(doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength()).isEqualTo(2);
        String firstId = ((org.w3c.dom.Element) doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").item(0))
                .getElementsByTagNameNS(AtomFeedWriter.ATOM, "id").item(0).getTextContent();
        assertThat(firstId).endsWith("/briefing-run/" + newer.getId());
        assertThat(xml).doesNotContain("<script").contains("&lt;h3&gt;Newer&lt;/h3&gt;");
        JsonNode json = JSON.readTree(mvc.perform(get("/v1/briefings/" + briefing + "/runs/feed.json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/feed+json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(json.get("items")).hasSize(2);
        assertThat(json.get("title").asString()).isEqualTo("Foresignal briefing: Morning");
        mvc.perform(get("/v1/briefings/" + UUID.randomUUID() + "/runs/feed.atom")).andExpect(status().isNotFound());
        mvc.perform(get("/v1/briefings/nope/runs/feed.json")).andExpect(status().isNotFound());
    }
}
