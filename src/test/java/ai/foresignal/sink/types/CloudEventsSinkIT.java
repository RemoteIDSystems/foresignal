package ai.foresignal.sink.types;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.settings.OutboundSettings;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.notify.HmacSigner;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkDeliveryException;
import ai.foresignal.sink.SinkFilter;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.testsupport.net.TestOutbound;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code cloudevents} against WireMock over TLS (prompt 110): structured and batched bodies, the CloudEvents
 * attributes, the HMAC header, a 503 with {@code Retry-After} reported as retryable, a 400 as permanent (no ack),
 * and the outbound URL policy checked on every delivery (DNS rebinding).
 */
class CloudEventsSinkIT {

    @RegisterExtension
    static final WireMockExtension TARGET = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort().gzipDisabled(true))
            .resetOnEachTest(true)
            .build();

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");

    /** Which address {@code receiver.test} resolves to; changing it simulates DNS rebinding. */
    final AtomicReference<String> receiverAddress = new AtomicReference<>("127.0.0.1");

    OutboundUrlPolicy policy() {
        return new OutboundUrlPolicy(host -> {
            if ("receiver.test".equals(host)) {
                return new java.net.InetAddress[] {java.net.InetAddress.getByName(receiverAddress.get())};
            }
            return TestOutbound.offlineResolver().resolve(host);
        }, OutboundSettings::defaults, () -> List.of("receiver.test"), Optional.empty());
    }

    static PinnedHttp trustingPinnedHttp() {
        return new PinnedHttp(host -> {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new TrustAll()}, new SecureRandom());
            return context;
        });
    }

    SinkInstance open(int batchSize) {
        CloudEventsSinkType type = new CloudEventsSinkType(policy(), trustingPinnedHttp(), "inst-1");
        Map<String, Object> config = Map.of("url", "https://receiver.test:" + TARGET.getRuntimeInfo().getHttpsPort()
                + "/events", "batchSize", batchSize);
        type.validate(config, Map.of("hmacSecret", "k"));
        return type.open(new SinkConfig("ce", CloudEventsSinkType.ID, "CE", SinkMode.PUSH, config,
                Map.of("hmacSecret", "k"), SinkFilter.ALL));
    }

    static SinkRecord record(long seq, int version) {
        return new SinkRecord("usgs", "q" + seq, "earthquake", seq, version, T, null, false, null, Map.of("mag", 5),
                null, "USGS");
    }

    @Test
    @DisplayName("structured mode: one event per request with the CloudEvents attributes and the HMAC signature")
    void structured() throws Exception {
        TARGET.stubFor(post(urlEqualTo("/events")).willReturn(aResponse().withStatus(202)));
        SinkInstance sink = open(1);
        assertThat(sink.deliverAll(List.of(record(7, 1), record(8, 2)))).isEqualTo(2);
        List<LoggedRequest> requests = TARGET.findAll(postRequestedFor(urlEqualTo("/events")));
        assertThat(requests).hasSize(2);
        LoggedRequest first = requests.getFirst();
        assertThat(first.getHeader("Content-Type")).isEqualTo(CloudEventsSinkType.STRUCTURED);
        JsonNode event = JSON.readTree(first.getBodyAsString());
        assertThat(event.get("specversion").asString()).isEqualTo("1.0");
        assertThat(event.get("id").asString()).isEqualTo("inst-1:7");
        assertThat(event.get("source").asString()).isEqualTo("urn:foresignal:inst-1:source:usgs");
        assertThat(event.get("type").asString()).isEqualTo("ai.foresignal.record.created");
        assertThat(event.get("subject").asString()).isEqualTo("usgs/q7");
        assertThat(event.get("foresignalseq").asLong()).isEqualTo(7);
        assertThat(event.get("data").get("attribution").asString()).isEqualTo("USGS");
        assertThat(JSON.readTree(requests.get(1).getBodyAsString()).get("type").asString())
                .isEqualTo("ai.foresignal.record.updated");
        String timestamp = first.getHeader("X-Foresignal-Timestamp");
        assertThat(first.getHeader(HmacSigner.HEADER)).isEqualTo(HmacSigner.sign("k", timestamp, first.getBody()));
        assertThat(first.getHeader("Host")).startsWith("receiver.test");
    }

    @Test
    @DisplayName("batched mode: one application/cloudevents-batch+json array; snapshot events are typed snapshot")
    void batched() throws Exception {
        TARGET.stubFor(post(urlEqualTo("/events")).willReturn(aResponse().withStatus(200)));
        SinkInstance sink = open(10);
        assertThat(sink.batchSize()).isEqualTo(10);
        assertThat(sink.deliverAll(List.of(record(1, 1), record(2, 1).asSnapshot(), record(3, 1)))).isEqualTo(3);
        TARGET.verify(1, postRequestedFor(urlEqualTo("/events"))
                .withHeader("Content-Type", equalTo(CloudEventsSinkType.BATCH))
                .withHeader(HmacSigner.HEADER, matching("sha256=[0-9a-f]{64}")));
        JsonNode batch = JSON.readTree(TARGET.findAll(postRequestedFor(urlEqualTo("/events"))).getFirst()
                .getBodyAsString());
        assertThat(batch.isArray()).isTrue();
        assertThat(batch.size()).isEqualTo(3);
        assertThat(batch.get(1).get("type").asString()).isEqualTo("ai.foresignal.record.snapshot");
        assertThat(batch.get(1).get("foresignalreason").asString()).isEqualTo("snapshot");
    }

    @Test
    @DisplayName("503 with Retry-After is retryable and carries the delay; 400 is permanent; neither acknowledges")
    void failures() {
        TARGET.stubFor(post(urlEqualTo("/events")).willReturn(aResponse().withStatus(503).withHeader("Retry-After",
                "7")));
        SinkInstance sink = open(1);
        assertThatThrownBy(() -> sink.deliver(record(1, 1))).isInstanceOfSatisfying(SinkDeliveryException.class,
                e -> {
                    assertThat(e.permanent()).isFalse();
                    assertThat(e.status()).isEqualTo(503);
                    assertThat(e.retryAfter()).isEqualTo(Duration.ofSeconds(7));
                });
        TARGET.resetAll();
        TARGET.stubFor(post(urlEqualTo("/events")).willReturn(aResponse().withStatus(400)));
        assertThatThrownBy(() -> sink.deliver(record(1, 1))).isInstanceOfSatisfying(SinkDeliveryException.class,
                e -> assertThat(e.permanent()).isTrue());
        assertThat(sink.test(record(1, 1)).ok()).isFalse();
    }

    @Test
    @DisplayName("the URL policy is checked on every delivery: a host that now resolves to a blocked address (cloud metadata) is refused")
    void rebinding() {
        TARGET.stubFor(post(urlEqualTo("/events")).willReturn(aResponse().withStatus(204)));
        SinkInstance sink = open(1);
        assertThat(sink.deliver(record(1, 1))).isTrue();
        receiverAddress.set("169.254.169.254");
        assertThatThrownBy(() -> sink.deliver(record(2, 1))).isInstanceOf(SinkDeliveryException.class)
                .hasMessageContaining("outbound URL policy");
        TARGET.verify(1, postRequestedFor(urlEqualTo("/events")));
    }

    @Test
    @DisplayName("Retry-After parses delta-seconds and HTTP dates; non-2xx are classified")
    void units() {
        assertThat(CloudEventsSinkType.retryAfter("120", T)).isEqualTo(Duration.ofSeconds(120));
        assertThat(CloudEventsSinkType.retryAfter("Fri, 16 Oct 2026 12:01:00 GMT", T)).isEqualTo(Duration.ofMinutes(1));
        assertThat(CloudEventsSinkType.retryAfter("soon", T)).isNull();
        assertThat(CloudEventsSinkType.retryAfter(null, T)).isNull();
        assertThat(CloudEventsSinkType.failure(429, Duration.ofSeconds(3)).permanent()).isFalse();
        assertThat(CloudEventsSinkType.failure(408, null).permanent()).isFalse();
        assertThat(CloudEventsSinkType.failure(404, Duration.ofSeconds(3)).permanent()).isTrue();
        assertThat(CloudEventsSinkType.failure(404, Duration.ofSeconds(3)).retryAfter()).isNull();
    }

    /** Trusts WireMock's self-signed certificate (test only). */
    static final class TrustAll extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
