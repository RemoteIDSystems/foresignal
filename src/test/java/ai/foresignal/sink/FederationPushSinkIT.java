package ai.foresignal.sink;

import static ai.foresignal.testsupport.sink.SinkTestConfig.FORBIDDEN;
import static ai.foresignal.testsupport.sink.SinkTestConfig.KIND;
import static ai.foresignal.testsupport.sink.SinkTestConfig.OPEN;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.federation.FederationPushSinkType;
import ai.foresignal.federation.PeerKeys;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.sink.SinkTestConfig;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * An edge relay's {@code federation-push} sink (prompt 113) against a hub served from WireMock: while the hub is
 * unreachable (503) the durable cursor holds and nothing is lost; once it answers, the edge catches up in order with
 * signed, gzip-compressed {@code FederationItem} batches carrying provenance and the peer ingest key, and a record
 * whose license forbids redistribution never leaves.
 */
@IntegrationTest
@Import(SinkTestConfig.class)
class FederationPushSinkIT extends WireMockSupport {

    static final Duration WAIT = Duration.ofSeconds(30);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String PATH = "/v1/federation/ingest/edge";
    static final String KEY = "fsp_edge-test-key";

    @Autowired SinkService sinks;
    @Autowired SinkSupervisor supervisor;
    @Autowired SinkCursorRepository cursors;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;
    @Autowired SigningKeyService signing;
    @Autowired InstallIdService installId;

    @BeforeEach
    void setUp() {
        supervisor.timings(null, Duration.ofMillis(200));
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll();
        supervisor.timings(null, null);
    }

    void hubAnswers(int status) {
        WIREMOCK.resetMappings();
        WIREMOCK.stubFor(post(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"accepted\":0,\"duplicates\":0,\"dropped\":{}}")));
    }

    void point(String source, String id) {
        records.ingest(source, NormalizedRecord.builder(id, KIND).observedAt(Instant.now()).point(10, 20)
                .put("n", id).build());
    }

    long maxSeq() {
        Long seq = jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class);
        return seq == null ? 0 : seq;
    }

    SinkCursorEntity cursor() {
        return cursors.findById("push-hub").orElseThrow();
    }

    static byte[] body(ServeEvent event) {
        byte[] raw = event.getRequest().getBody();
        if (raw.length > 2 && (raw[0] & 0xff) == 0x1f && (raw[1] & 0xff) == 0x8b) {
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(raw))) {
                return in.readAllBytes();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return raw;
    }

    @Test
    @DisplayName("offline: the cursor holds; online again: signed batches with provenance catch up without loss")
    void buffersAndResumes() {
        hubAnswers(200);
        sinks.create(JSON.readTree("{\"id\":\"push-hub\",\"type\":\"" + FederationPushSinkType.ID + "\","
                + "\"name\":\"Hub\",\"enabled\":true,\"config\":{\"url\":\"" + baseUrl() + "\",\"peerId\":\"edge\"},"
                + "\"secrets\":{\"ingestKey\":\"" + KEY + "\"},"
                + "\"filter\":{\"sources\":[\"" + OPEN + "\",\"" + FORBIDDEN + "\"]}}"));
        await().atMost(WAIT).until(() -> cursors.findById("push-hub")
                .map(c -> SinkCursorEntity.RUNNING.equals(c.getState())).orElse(false));
        long before = cursor().getLastSeq();

        hubAnswers(503);
        point(OPEN, "a");
        point(FORBIDDEN, "f");
        point(OPEN, "b");
        await().atMost(WAIT).until(() -> SinkCursorEntity.FAILING.equals(cursor().getState()));
        assertThat(cursor().getLastSeq()).as("the cursor holds while the hub is down").isEqualTo(before);

        hubAnswers(200);
        await().atMost(WAIT).until(() -> cursor().getLastSeq() >= maxSeq());
        List<String> delivered = new ArrayList<>();
        ServeEvent last = null;
        for (ServeEvent event : WIREMOCK.getAllServeEvents()) {
            if (event.getResponse().getStatus() != 200) {
                continue;
            }
            JsonNode batch = JSON.readTree(body(event));
            for (JsonNode item : batch.get("items")) {
                delivered.add(item.get("externalId").asString());
                assertThat(item.get("provenance").get("chain").get(0).get("instanceId").asString())
                        .isEqualTo(installId.installId());
                assertThat(item.get("licence").asString()).isNotBlank();
            }
            if (batch.get("items").size() > 0) {
                last = event;
            }
        }
        assertThat(delivered).contains("a", "b").doesNotContain("f");
        assertThat(last).isNotNull();
        assertThat(last.getRequest().getHeader("Authorization")).isEqualTo("Bearer " + KEY);
        assertThat(last.getRequest().getHeader("Content-Encoding")).isEqualTo("gzip");
        PeerKeys.Verification verification = PeerKeys.verifyWithJwks(body(last),
                last.getRequest().getHeader("Foresignal-Signature"), signing.jwks());
        assertThat(verification.signatureValid()).as(String.valueOf(verification.error())).isTrue();
        assertThat(Base64.getUrlDecoder().decode(last.getRequest().getHeader("Foresignal-Key")))
                .isEqualTo(signing.active().x());
    }

    @Test
    @DisplayName("the gate uses the FEDERATION path: LOCAL_ONLY only with sameOperator, FORBIDDEN never")
    void gate(@Autowired SinkRecordGate gate) {
        SinkRecord forbidden = new SinkRecord(FORBIDDEN, "f", KIND, 1, 1, Instant.now(), null, false, null,
                java.util.Map.of(), null, null);
        assertThat(gate.allowFederation(true, forbidden).allowed()).isFalse();
        SinkRecord open = new SinkRecord(OPEN, "a", KIND, 2, 1, Instant.now(), null, false, null, java.util.Map.of(),
                null, null);
        assertThat(gate.allowFederation(false, open).allowed()).isTrue();
        SinkRecord mlat = new SinkRecord(OPEN, "m", KIND, 3, 1, Instant.now(), null, false, null,
                java.util.Map.of("posSource", "mlat"), null, null);
        assertThat(gate.allowFederation(true, mlat).allowed()).isFalse();
    }
}
