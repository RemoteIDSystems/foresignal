package ai.foresignal.inbound;

import static ai.foresignal.testsupport.inbound.InboundIt.body;
import static ai.foresignal.testsupport.inbound.InboundIt.feature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.InboundIt;
import ai.foresignal.testsupport.trigger.TriggerFixture;
import ai.foresignal.trigger.AlertBus;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * The inbound push endpoints end to end (prompt 024): {@code POST /v1/ingest/{sourceId}} and the
 * named formats, credentials, caps, the time base, provenance, touches, change events and triggers.
 * Records are seeded only through the endpoints.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class IngestControllerIT {

    static final Instant T = Instant.parse("2026-10-12T08:00:00Z");
    static final String OBSERVED = "2026-10-12T07:59:00Z";
    static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    @Autowired MockMvc mvc;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired InstallIdService install;
    @Autowired TriggerRuleService rules;
    @Autowired TriggerEngine engine;
    @Autowired ChangeBus changeBus;
    @Autowired AlertStore alertStore;
    @Autowired AlertBus alertBus;
    @Autowired AlertRepository alerts;

    String admin;
    InboundIt it;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        admin = "Bearer " + adminTokens.rotate();
        it = new InboundIt(mvc, admin);
        if (!engine.running()) {
            engine.start();
        }
    }

    InboundIt.Created source(String stem, String extra) throws Exception {
        return it.create(InboundIt.newId(stem), "{'name':'Sensors','kinds':['sensor-reading']" + extra + "}");
    }

    MockHttpServletResponse push(InboundIt.Created source, String json) throws Exception {
        return it.push("/v1/ingest/" + source.id(), source.key(), MediaType.APPLICATION_JSON, json);
    }

    @Test
    @DisplayName("a GeoJSON FeatureCollection and the records shape both land as records, as JSON and as geo+json")
    void nativeShapes() throws Exception {
        InboundIt.Created source = source("native", "");

        MockHttpServletResponse geo = it.push("/v1/ingest/" + source.id(), source.key(), GEO_JSON,
                feature("garage", null, OBSERVED, 31.5));
        assertThat(geo.getStatus()).as(geo.getContentAsString()).isEqualTo(200);
        JsonNode outcome = body(geo);
        assertThat(outcome.get("seen").asInt()).isEqualTo(1);
        assertThat(outcome.get("persisted").asInt()).isEqualTo(1);
        assertThat(outcome.get("changed").asInt()).isEqualTo(1);
        assertThat(outcome.get("byReason").get("FIRST").asInt()).isEqualTo(1);
        assertThat(outcome.get("fromSeq").asLong()).isPositive();

        MockHttpServletResponse records = push(source, "{\"records\":[{\"externalId\":\"attic\",\"kind\":\"sensor-reading\","
                + "\"observedAt\":\"" + OBSERVED + "\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]},"
                + "\"payload\":{\"name\":\"Attic\",\"value\":20}}]}");
        assertThat(records.getStatus()).as(records.getContentAsString()).isEqualTo(200);
        assertThat(body(records).get("persisted").asInt()).isEqualTo(1);

        JsonNode listed = body(it.perform(get("/v1/records").param("source", source.id())
                .header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(listed.findValuesAsString("externalId")).contains("garage", "attic");
    }

    @Test
    @DisplayName("the key is accepted as X-Foresignal-Ingest-Key, Bearer eik_ and Basic; an admin token too; a wrong key is 401")
    void credentials() throws Exception {
        InboundIt.Created source = source("creds", "");
        String path = "/v1/ingest/" + source.id();
        String body = feature("a", null, OBSERVED, 1);

        assertThat(push(source, body).getStatus()).isEqualTo(200);
        assertThat(it.perform(post(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + source.key())
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(200);
        String basic = Base64.getEncoder().encodeToString(("anyone:" + source.key()).getBytes(StandardCharsets.UTF_8));
        assertThat(it.perform(post(path).header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(200);
        assertThat(it.perform(post(path).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(200);

        assertThat(it.push(path, "eik_wrong", MediaType.APPLICATION_JSON, body).getStatus()).isEqualTo(401);
        assertThat(it.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(401);
        InboundIt.Created other = source("other", "");
        assertThat(it.push(path, other.key(), MediaType.APPLICATION_JSON, body).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a rotated key replaces the old one at once")
    void rotation() throws Exception {
        InboundIt.Created source = source("rotate", "");
        MockHttpServletResponse rotated = it.perform(post("/v1/custom-sources/" + source.id() + "/ingest-key/rotate")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(rotated.getStatus()).isEqualTo(200);
        String key = body(rotated).get("ingestKey").asString();

        assertThat(push(source, feature("a", null, OBSERVED, 1)).getStatus()).isEqualTo(401);
        assertThat(it.push("/v1/ingest/" + source.id(), key, MediaType.APPLICATION_JSON, feature("a", null, OBSERVED, 1))
                .getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a non-push source is 404 and a disabled source 409 SOURCE_DISABLED")
    void notPushAndDisabled() throws Exception {
        assertThat(it.perform(post("/v1/ingest/" + CollectorTestSources.POLL).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(feature("a", null, OBSERVED, 1))).getStatus()).isEqualTo(404);

        InboundIt.Created source = source("disabled", "");
        assertThat(it.perform(put("/v1/sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).getStatus()).isEqualTo(200);
        MockHttpServletResponse response = push(source, feature("a", null, OBSERVED, 1));
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(body(response).get("code").asString()).isEqualTo("SOURCE_DISABLED");
    }

    @Test
    @DisplayName("over 5 MB is 413, over 10,000 records 400; a gzip body is decompressed")
    void limits() throws Exception {
        InboundIt.Created source = source("limits", ",'recordsPerMinute':60000");
        String huge = "{\"records\":[],\"pad\":\"" + "x".repeat(5 * 1024 * 1024) + "\"}";
        assertThat(push(source, huge).getStatus()).isEqualTo(413);

        StringBuilder many = new StringBuilder("{\"records\":[");
        for (int i = 0; i < 10_001; i++) {
            many.append(i == 0 ? "" : ",").append("{\"externalId\":\"r").append(i)
                    .append("\",\"kind\":\"sensor-reading\",\"payload\":{}}");
        }
        many.append("]}");
        MockHttpServletResponse tooMany = push(source, many.toString());
        assertThat(tooMany.getStatus()).as(tooMany.getContentAsString()).isEqualTo(400);

        ByteArrayOutputStream zipped = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(zipped)) {
            gzip.write(feature("zipped", null, OBSERVED, 1).getBytes(StandardCharsets.UTF_8));
        }
        MockHttpServletResponse gz = it.perform(post("/v1/ingest/" + source.id())
                .header("X-Foresignal-Ingest-Key", source.key()).header("Content-Encoding", "gzip")
                .contentType(MediaType.APPLICATION_JSON).content(zipped.toByteArray()));
        assertThat(gz.getStatus()).as(gz.getContentAsString()).isEqualTo(200);
        assertThat(body(gz).get("persisted").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("the request budget and the records-per-minute cap answer 429 with Retry-After and a REFUSED fetch row")
    void caps() throws Exception {
        InboundIt.Created slow = source("rate", ",'rateLimitPerMinute':1");
        assertThat(push(slow, feature("a", null, OBSERVED, 1)).getStatus()).isEqualTo(200);
        MockHttpServletResponse refused = push(slow, feature("a", null, OBSERVED, 2));
        assertThat(refused.getStatus()).isEqualTo(429);
        assertThat(body(refused).get("code").asString()).isEqualTo("RATE_LIMITED");
        assertThat(refused.getHeader(HttpHeaders.RETRY_AFTER)).isNotBlank();

        InboundIt.Created capped = source("records", ",'recordsPerMinute':2");
        String three = "{\"records\":[" + String.join(",", List.of("a", "b", "c").stream()
                .map(id -> "{\"externalId\":\"" + id + "\",\"kind\":\"sensor-reading\",\"observedAt\":\"" + OBSERVED
                        + "\",\"payload\":{}}").toList()) + "]}";
        MockHttpServletResponse over = push(capped, three);
        assertThat(over.getStatus()).isEqualTo(429);
        assertThat(body(over).get("code").asString()).isEqualTo("INGEST_RECORD_RATE");
        assertThat(over.getHeader(HttpHeaders.RETRY_AFTER)).isNotBlank();

        assertThat(jdbc.queryForList("select status from fetch_log where source_id in (?, ?) and trigger = 'PUSH'",
                String.class, slow.id(), capped.id())).contains("REFUSED", "OK");
        assertThat(jdbc.queryForList("select count(*) from fetch_log where source_id = ? and status = 'REFUSED'",
                Integer.class, capped.id())).containsExactly(1);
    }

    @Test
    @DisplayName("UPSTREAM rejects a record without observedAt (MISSING_OBSERVED_AT); RECEIVED stamps the receipt time")
    void timeBase() throws Exception {
        InboundIt.Created upstream = source("upstream", "");
        JsonNode outcome = body(push(upstream, feature("no-time", null, null, 1)));
        assertThat(outcome.get("rejected").asInt()).isEqualTo(1);
        assertThat(outcome.get("rejections").get(0).get("reason").asString()).isEqualTo("MISSING_OBSERVED_AT");
        assertThat(outcome.get("rejections").get(0).get("index").asInt()).isZero();

        InboundIt.Created received = source("received", ",'observedAtMode':'RECEIVED'");
        assertThat(body(push(received, feature("stamped", null, null, 1))).get("persisted").asInt()).isEqualTo(1);
        // record_head keeps persisted_observed_at; the latest observed_at lives on record_live (Core-Data-Model §8).
        assertThat(jdbc.queryForObject("select observed_at from record_live where source_id = ? and external_id = 'stamped'",
                java.sql.Timestamp.class, received.id()).toInstant()).isEqualTo(T);
    }

    @Test
    @DisplayName("a push writes a PUSH fetch row with principal and body hash; an identical re-push is a touch; changes are published")
    void provenanceTouchAndChanges() throws Exception {
        InboundIt.Created source = source("touch", "");
        String body = feature("door", null, OBSERVED, 1);
        assertThat(body(push(source, body)).get("persisted").asInt()).isEqualTo(1);
        clock.setInstant(T.plusSeconds(3_700));
        JsonNode again = body(push(source, body));
        assertThat(again.get("persisted").asInt()).isZero();
        assertThat(again.get("touched").asInt()).isEqualTo(1);

        Map<String, Object> row = jdbc.queryForMap("select trigger, status, principal, body_sha256, url_redacted, bytes"
                + " from fetch_log where source_id = ? order by id limit 1", source.id());
        assertThat(row.get("trigger")).isEqualTo("PUSH");
        assertThat(row.get("status")).isEqualTo("OK");
        assertThat(row.get("principal")).isEqualTo("ingest-key:" + source.key().substring(0, 8));
        assertThat((String) row.get("body_sha256")).hasSize(64);
        assertThat(row.get("url_redacted")).isEqualTo("/v1/ingest/" + source.id());

        JsonNode changes = body(it.perform(get("/v1/changes").param("after", "0").param("source", source.id())
                .header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(changes.findValuesAsString("externalId")).contains("door");
    }

    @Test
    @DisplayName("a trigger on the custom kind fires on a pushed record")
    void triggerFires() throws Exception {
        InboundIt.Created source = source("trigger", "");
        try (TriggerFixture fixture = new TriggerFixture(rules, engine, changeBus, alertStore, alertBus)) {
            UUID rule = fixture.rule("{'name':'Hot','sources':['" + source.id() + "'],'kinds':['sensor-reading'],"
                    + "'conditions':[{'path':'value','op':'>=','value':40}]}");
            push(source, feature("garage", null, OBSERVED, 45));
            assertThat(fixture.alerts(rule)).singleElement().satisfies(a -> assertThat(a.externalId()).isEqualTo("garage"));
        }
    }

    @Test
    @DisplayName("a configured hmac-sha256 verifier must verify; keyOptional lets a valid signature alone authenticate")
    void signatures() throws Exception {
        String secret = "s3cret-value";
        InboundIt.Created source = source("signed",
                ",'verifiers':{'native':{'scheme':'hmac-sha256','secret':'" + secret + "','keyOptional':true}}");
        String body = feature("signed", null, OBSERVED, 1);
        String path = "/v1/ingest/" + source.id();

        MockHttpServletResponse unsigned = push(source, body);
        assertThat(unsigned.getStatus()).isEqualTo(401);
        assertThat(body(unsigned).get("code").asString()).isEqualTo("SIGNATURE_INVALID");

        String signature = "sha256=" + hmac(secret, body);
        assertThat(it.perform(post(path).header("X-Foresignal-Ingest-Key", source.key()).header("X-Signature-256", signature)
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(200);
        assertThat(it.perform(post(path).header("X-Signature-256", signature)
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(200);
        assertThat(it.perform(post(path).header("X-Signature-256", "sha256=" + hmac("wrong", body))
                .contentType(MediaType.APPLICATION_JSON).content(body)).getStatus()).isEqualTo(401);
        // The secret is write-only.
        JsonNode view = body(it.perform(get("/v1/custom-sources/" + source.id()).header(HttpHeaders.AUTHORIZATION, admin)));
        assertThat(view.get("verifiers").get("native").get("secretSet").asBoolean()).isTrue();
        assertThat(view.toString()).doesNotContain(secret);
    }

    @Test
    @DisplayName("named formats: Alertmanager firing then resolved (tombstone), CAP Alert, unknown and reserved formats 404")
    void formats() throws Exception {
        InboundIt.Created source = source("formats", "");
        String firing = alertmanager("firing", "0001-01-01T00:00:00Z");
        MockHttpServletResponse fired = it.push("/v1/ingest/" + source.id() + "/alertmanager", source.key(),
                MediaType.APPLICATION_JSON, firing);
        assertThat(fired.getStatus()).as(fired.getContentAsString()).isEqualTo(200);
        assertThat(body(fired).get("persisted").asInt()).isEqualTo(1);
        MockHttpServletResponse resolved = it.push("/v1/ingest/" + source.id() + "/alertmanager", source.key(),
                MediaType.APPLICATION_JSON, alertmanager("resolved", "2026-10-12T07:59:30Z"));
        assertThat(body(resolved).get("byReason").get("TOMBSTONE").asInt()).isEqualTo(1);

        MockHttpServletResponse cap = it.push("/v1/ingest/" + source.id() + "/cap", source.key(),
                MediaType.parseMediaType("application/cap+xml"), CAP);
        assertThat(cap.getStatus()).as(cap.getContentAsString()).isEqualTo(200);
        assertThat(body(cap).get("persisted").asInt()).isEqualTo(1);

        MockHttpServletResponse unknown = it.push("/v1/ingest/" + source.id() + "/nope", source.key(),
                MediaType.APPLICATION_JSON, "{}");
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("FORMAT_UNKNOWN");
        assertThat(it.push("/v1/ingest/" + source.id() + "/media", source.key(), MediaType.APPLICATION_JSON, "{}")
                .getStatus()).isEqualTo(404);
        assertThat(it.push("/v1/ingest/" + source.id(), source.key(), MediaType.TEXT_PLAIN, "hello").getStatus())
                .isEqualTo(415);
    }

    @Test
    @DisplayName("the CloudEvents handshake answers with the origin and the per-minute budget; other formats 404")
    void handshake() throws Exception {
        InboundIt.Created source = source("handshake", ",'rateLimitPerMinute':42");
        MockHttpServletResponse ok = it.perform(options("/v1/ingest/" + source.id() + "/cloudevents")
                .header("X-Foresignal-Ingest-Key", source.key()).header("WebHook-Request-Origin", "eventgrid.azure.net"));
        assertThat(ok.getStatus()).isEqualTo(200);
        assertThat(ok.getHeader("WebHook-Allowed-Origin")).isEqualTo("eventgrid.azure.net");
        assertThat(ok.getHeader("WebHook-Allowed-Rate")).isEqualTo("42");
        assertThat(it.perform(options("/v1/ingest/" + source.id() + "/native").header("X-Foresignal-Ingest-Key", source.key())
                .header("WebHook-Request-Origin", "x")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("GET /v1/ingest/formats lists the formats with options and verifiers, plus the reserved names (admin)")
    void listFormats() throws Exception {
        MockHttpServletResponse response = it.perform(get("/v1/ingest/formats").header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(response.getStatus()).isEqualTo(200);
        JsonNode formats = body(response);
        assertThat(formats.findValuesAsString("id")).contains("native", "alertmanager", "cloudevents", "cap", "mapping",
                "media", "rasters", "mapping-preview");
        assertThat(formats.get(0).get("verifiers").toString()).contains("hmac-sha256", "standard-webhooks", "foresignal");
        assertThat(it.perform(get("/v1/ingest/formats")).getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("a push carrying this instance's own origin marker is acknowledged but not stored (loop guard)")
    void loopGuard() throws Exception {
        InboundIt.Created source = source("loop", "");
        MockHttpServletResponse response = it.perform(post("/v1/ingest/" + source.id())
                .header("X-Foresignal-Ingest-Key", source.key()).header("X-Foresignal-Origin", install.installId())
                .contentType(MediaType.APPLICATION_JSON).content(feature("echo", null, OBSERVED, 1)));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body(response).get("seen").asInt()).isZero();
        assertThat(jdbc.queryForList("select 1 from record_head where source_id = ?", Integer.class, source.id())).isEmpty();
    }

    static String alertmanager(String status, String endsAt) {
        return "{\"version\":\"4\",\"groupKey\":\"g\",\"status\":\"" + status + "\",\"receiver\":\"foresignal\","
                + "\"alerts\":[{\"status\":\"" + status + "\",\"labels\":{\"alertname\":\"HighTemp\"},"
                + "\"annotations\":{\"summary\":\"hot\"},\"startsAt\":\"2026-10-12T07:00:00Z\",\"endsAt\":\"" + endsAt + "\","
                + "\"generatorURL\":\"http://prom\",\"fingerprint\":\"fp1\"}]}";
    }

    static final String CAP = """
            <?xml version="1.0" encoding="UTF-8"?>
            <alert xmlns="urn:oasis:names:tc:emergency:cap:1.2">
              <identifier>CAP-1</identifier>
              <sender>alerts@example.org</sender>
              <sent>2026-10-12T07:00:00-00:00</sent>
              <status>Actual</status>
              <msgType>Alert</msgType>
              <scope>Public</scope>
              <info>
                <event>Flood Warning</event>
                <urgency>Immediate</urgency>
                <severity>Severe</severity>
                <certainty>Likely</certainty>
                <expires>2026-10-12T18:00:00-00:00</expires>
                <area>
                  <areaDesc>Valley</areaDesc>
                  <polygon>30.20,-97.80 30.20,-97.60 30.35,-97.60 30.20,-97.80</polygon>
                </area>
              </info>
            </alert>
            """;

    static String hmac(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
