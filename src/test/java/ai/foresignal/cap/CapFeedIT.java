package ai.foresignal.cap;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.feed.AtomFeedWriter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.TriggerRuleRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.settings.AppSettingService;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CAP 1.2 output end to end (prompt 112): one stored message per alert state change, the Atom index tracking the
 * current message of each alert, the {@code references} chain, the {@code .cap} endpoints with ETags, XSD
 * validity, the Exercise status and the private-source exclusion.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class CapFeedIT {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired TestRecords records;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerRuleRepository ruleRepository;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alerts;
    @Autowired AlertBus bus;
    @Autowired AlertService alertService;
    @Autowired CapMessageRepository messages;
    @Autowired JdbcTemplate jdbc;

    TriggerFixture fixture;
    UUID rule;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        fixture = new TriggerFixture(rules, engine, changeBus, alerts, bus);
        rule = fixture.rule("{'name':'Strong quake','sources':['" + STANDARD + "'],'severity':'critical',"
                + "'tags':['geo'],'conditions':[{'path':'mag','op':'>=','value':5}]}");
    }

    @AfterEach
    void tearDown() {
        fixture.close();
        settings.invalidateCache();
    }

    AlertView fire(String id) {
        records.ingest(STANDARD, NormalizedRecord.builder(id, STANDARD_KIND).observedAt(T).point(-97.5, 30.25)
                .put("mag", 6.1).build());
        fixture.settle();
        AlertView alert = fixture.routed(rule).stream().filter(a -> a.externalId().equals(id)).findFirst().orElseThrow();
        awaitMessages(alert.id(), 1);
        return alert;
    }

    List<CapMessageEntity> awaitMessages(long alertId, int n) {
        await().atMost(Duration.ofSeconds(10)).until(() -> messages.findByAlertIdOrderByNAsc(alertId).size() >= n);
        return messages.findByAlertIdOrderByNAsc(alertId);
    }

    static Document xml(String text) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    static String text(Document doc, String name) {
        NodeList list = doc.getElementsByTagNameNS(CapMessageWriter.NAMESPACE, name);
        return list.getLength() == 0 ? null : list.item(0).getTextContent();
    }

    static void validate(String text) throws Exception {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(CapFeedIT.class.getResource("/cap/CAP-v1.2.xsd")).newValidator()
                .validate(new StreamSource(new StringReader(text)));
    }

    String body(String url) throws Exception {
        return mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("fire, acknowledge and resolve give Alert, Update and Update/AllClear with a references chain")
    void lifecycle() throws Exception {
        AlertView alert = fire("a");
        clock.setInstant(T.plusSeconds(60));
        alertService.acknowledge(alert.id(), AlertService.Transition.NONE, "token:test");
        awaitMessages(alert.id(), 2);
        clock.setInstant(T.plusSeconds(120));
        alertService.resolve(alert.id(), AlertService.Transition.NONE, "token:test");
        List<CapMessageEntity> all = awaitMessages(alert.id(), 3);
        assertThat(all).extracting(CapMessageEntity::getMsgType).containsExactly("Alert", "Update", "Update");
        assertThat(all).extracting(CapMessageEntity::getN).containsExactly(1, 2, 3);
        assertThat(all.get(0).getIdentifier()).endsWith("-" + alert.id() + "-1");
        for (CapMessageEntity m : all) {
            validate(m.getXml());
            assertThat(m.getXml()).contains("<scope>Public</scope>").contains(CapMessage.NOTE).doesNotContain("Cancel");
        }
        Document last = xml(all.get(2).getXml());
        assertThat(text(last, "responseType")).isEqualTo("AllClear");
        assertThat(text(last, "references")).contains(all.get(0).getIdentifier()).contains(all.get(1).getIdentifier());
        assertThat(text(last, "references").split(" ")).hasSize(2);
        assertThat(xml(all.get(0).getXml()).getElementsByTagNameNS(CapMessageWriter.NAMESPACE, "references").getLength())
                .isZero();
        Document first = xml(all.get(0).getXml());
        assertThat(text(first, "severity")).isEqualTo("Severe");
        assertThat(text(first, "event")).isEqualTo("Strong quake");
        assertThat(text(first, "category")).isEqualTo("Other");
        assertThat(text(first, "certainty")).isEqualTo("Observed");
        assertThat(text(first, "circle")).isEqualTo("30.25,-97.5 1");
        assertThat(text(first, "senderName")).startsWith("Foresignal (");
        assertThat(first.getElementsByTagNameNS(CapMessageWriter.NAMESPACE, "parameter").getLength()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("the Atom index lists each alert's current message, linked as application/cap+xml, and tracks changes")
    void atomIndex() throws Exception {
        AlertView a = fire("x");
        fire("y");
        String atom = body("/v1/cap/alerts.atom");
        Document doc = xml(atom);
        NodeList entries = doc.getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry");
        assertThat(entries.getLength()).isEqualTo(2);
        Element link = (Element) ((Element) entries.item(0)).getElementsByTagNameNS(AtomFeedWriter.ATOM, "link").item(0);
        assertThat(link.getAttribute("type")).isEqualTo("application/cap+xml");
        assertThat(link.getAttribute("href")).contains("/v1/cap/messages/").endsWith(".cap");
        clock.setInstant(T.plusSeconds(60));
        alertService.acknowledge(a.id(), AlertService.Transition.NONE, "token:test");
        CapMessageEntity update = awaitMessages(a.id(), 2).get(1);
        String after = body("/v1/cap/alerts.atom");
        assertThat(after).contains("/v1/cap/messages/" + update.getIdentifier() + ".cap")
                .doesNotContain("/v1/cap/messages/" + messages.findByAlertIdOrderByNAsc(a.id()).get(0).getIdentifier() + ".cap");
        assertThat(xml(body("/v1/cap/alerts.atom?minSeverity=critical&tags=geo"))
                .getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength()).isEqualTo(2);
        assertThat(xml(body("/v1/cap/alerts.atom?tags=other"))
                .getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength()).isZero();
        assertThat(xml(body("/v1/cap/alerts.atom?ruleId=" + rule + "&since=" + T.plusSeconds(30)))
                .getElementsByTagNameNS(AtomFeedWriter.ATOM, "entry").getLength()).isEqualTo(1);
        mvc.perform(get("/v1/cap/alerts.atom?ruleId=nope")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("the .cap endpoints serve the latest and a stored message with an ETag and 304")
    void capEndpoints() throws Exception {
        AlertView alert = fire("c");
        MvcResult latest = mvc.perform(get("/v1/cap/alerts/" + alert.id() + ".cap")).andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/cap+xml;charset=UTF-8"))
                .andExpect(header().exists(HttpHeaders.ETAG)).andReturn();
        String etag = latest.getResponse().getHeader(HttpHeaders.ETAG);
        mvc.perform(get("/v1/cap/alerts/" + alert.id() + ".cap").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());
        String identifier = messages.findByAlertIdOrderByNAsc(alert.id()).get(0).getIdentifier();
        String stored = body("/v1/cap/messages/" + identifier + ".cap");
        validate(stored);
        mvc.perform(get("/v1/cap/messages/nope-1-1.cap")).andExpect(status().isNotFound());
        mvc.perform(get("/v1/cap/alerts/999999.cap")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("an alert flagged as an exercise issues Exercise messages")
    void exercise() throws Exception {
        AlertView alert = fire("e");
        jdbc.update("UPDATE alert SET context = '{\"exercise\": true}'::jsonb WHERE id = ?", alert.id());
        clock.setInstant(T.plusSeconds(60));
        alertService.acknowledge(alert.id(), AlertService.Transition.NONE, "token:test");
        CapMessageEntity update = awaitMessages(alert.id(), 2).get(1);
        assertThat(update.getStatus()).isEqualTo("Exercise");
        assertThat(update.getXml()).contains("<status>Exercise</status>");
        validate(update.getXml());
    }

    @Test
    @DisplayName("alerts of a private source never reach the CAP feed or its documents")
    void privateExcluded() throws Exception {
        UUID secret = fixture.rule("{'name':'Secret','sources':['" + RecordsTestSources.PRIVATE + "'],"
                + "'conditions':[{'path':'mag','op':'>=','value':5}]}");
        records.ingest(RecordsTestSources.PRIVATE, NormalizedRecord.builder("s", RecordsTestSources.PRIVATE_KIND)
                .observedAt(T).point(-97.5, 30.25).put("mag", 6.0).build());
        fixture.settle();
        AlertView alert = fixture.routed(secret).getFirst();
        awaitMessages(alert.id(), 1);
        assertThat(body("/v1/cap/alerts.atom")).doesNotContain("-" + alert.id() + "-1");
        mvc.perform(get("/v1/cap/alerts/" + alert.id() + ".cap")).andExpect(status().isNotFound());
        assertThat(ruleRepository.findById(secret)).isPresent();
    }
}
