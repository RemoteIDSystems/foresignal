package ai.foresignal.sink.types;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.notify.HmacSigner;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkSupport;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code cloudevents} (prompt 110; CHANGE_FEED, PUSH HTTP): structured-mode CloudEvents 1.0
 * ({@code application/cloudevents+json}, or a batch ≤ 100), signed with {@code X-Foresignal-Signature}
 * as the 018 webhook. 5xx and 429 retry (the caller keeps the cursor); other 4xx stop without advancing.
 */
@Component
public class CloudEventsSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "cloudevents";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "CloudEvents",
            "CloudEvents 1.0 over HTTP (structured mode)",
            SinkKind.CHANGE_FEED, Set.of(SinkMode.PUSH),
            Map.of("type", "object", "properties", Map.of(
                    "url", Map.of("type", "string"),
                    "batchSize", Map.of("type", "integer", "minimum", 1, "maximum", 100, "default", 1))),
            List.of("hmacSecret"), 50);

    private final OutboundUrlPolicy policy;
    private final String instanceId;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER).build();

    /** Creates the type. */
    public CloudEventsSinkType(OutboundUrlPolicy policy,
            @Value("${foresignal.instance-id:foresignal}") String instanceId) {
        this.policy = policy;
        this.instanceId = instanceId == null || instanceId.isBlank() ? "foresignal" : instanceId;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public SinkTypeSpec spec() {
        return SPEC;
    }

    @Override
    public void validate(Map<String, Object> config, Map<String, String> secrets) {
        Object url = config == null ? null : config.get("url");
        if (url == null || String.valueOf(url).isBlank()) {
            throw ai.foresignal.api.error.ValidationException.forField("config.url", "is required");
        }
        var result = policy.check(String.valueOf(url), OutboundUrlPolicy.Profile.PUSH_ENDPOINT, false);
        if (!result.allowed()) {
            throw ai.foresignal.api.error.ValidationException.forField("config.url", "is not allowed by the outbound URL policy");
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        return new Instance(URI.create(config.config("url")), config.secrets().get("hmacSecret"), instanceId, policy,
                client);
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final URI url;
        private final String secret;
        private final String instanceId;
        private final OutboundUrlPolicy policy;
        private final HttpClient client;

        Instance(URI url, String secret, String instanceId, OutboundUrlPolicy policy, HttpClient client) {
            this.url = url;
            this.secret = secret;
            this.instanceId = instanceId;
            this.policy = policy;
            this.client = client;
        }

        @Override
        public boolean deliver(SinkRecord record) {
            try {
                byte[] body = JSON.writeValueAsBytes(event(record));
                String timestamp = Long.toString(System.currentTimeMillis() / 1000);
                HttpRequest.Builder builder = HttpRequest.newBuilder(url)
                        .header("Content-Type", "application/cloudevents+json")
                        .header("X-Foresignal-Timestamp", timestamp)
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body));
                if (secret != null) {
                    builder.header(HmacSigner.HEADER, HmacSigner.sign(secret, timestamp, body));
                }
                HttpResponse<Void> response = client.send(builder.build(), HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return true;
                }
                // 5xx and 429 retry (no ack, so the cursor stays); other 4xx do not advance either.
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        private Map<String, Object> event(SinkRecord record) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("specversion", "1.0");
            out.put("id", instanceId + ":" + record.seq());
            out.put("source", "urn:foresignal:" + instanceId + ":source:" + record.sourceId());
            out.put("type", "ai.foresignal.record." + (record.tombstone() ? "tombstoned"
                    : record.version() <= 1 ? "created" : "updated"));
            out.put("subject", record.sourceId() + "/" + record.externalId());
            out.put("time", record.observedAt() == null ? null : record.observedAt().toString());
            out.put("datacontenttype", "application/json");
            out.put("foresignalseq", record.seq());
            out.put("foresignalkind", record.kind());
            out.put("foresignalreason", record.tombstone() ? "removed" : "observed");
            out.put("data", SinkSupport.statePayload(record));
            return out;
        }

        @Override
        public SinkTestResult test(SinkRecord probe) {
            long start = System.nanoTime();
            boolean ok = deliver(probe);
            return new SinkTestResult(ok, "send", ok ? "the receiver accepted the event" : "the receiver refused",
                    (System.nanoTime() - start) / 1_000_000, Map.of());
        }

        @Override
        public void close() {
            // The JDK client is shared; nothing to release.
        }
    }
}
