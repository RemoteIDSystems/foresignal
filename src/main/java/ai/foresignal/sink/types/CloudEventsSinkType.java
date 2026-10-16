package ai.foresignal.sink.types;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.notify.HmacSigner;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkDeliveryException;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkSupport;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * ({@code application/cloudevents+json}), or a batch of up to 100 ({@code application/cloudevents-batch+json})
 * when {@code batchSize > 1}, signed with {@code X-Foresignal-Signature} as the 018 webhook.
 *
 * <p>The URL is checked against the outbound URL policy ({@code PUSH_ENDPOINT}) at save <em>and on every
 * delivery</em>, and the request goes to the address that check resolved ({@link PinnedHttp}), so a DNS answer
 * that changes after the save (DNS rebinding) cannot turn the sink at a private address. A 2xx acknowledges; 429
 * and 5xx are retryable and carry the target's {@code Retry-After} to the supervisor's backoff; 408 is retryable;
 * any other 4xx is a permanent refusal. None of them advances the cursor.
 */
@Component
public class CloudEventsSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "cloudevents";

    /** The largest batch. */
    public static final int MAX_BATCH = 100;

    /** The structured-mode content type. */
    public static final String STRUCTURED = "application/cloudevents+json";

    /** The batched-mode content type. */
    public static final String BATCH = "application/cloudevents-batch+json";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final PinnedHttp.ClientSpec CLIENT =
            new PinnedHttp.ClientSpec(Duration.ofSeconds(10), HttpClient.Version.HTTP_1_1);

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "CloudEvents",
            "CloudEvents 1.0 over HTTP (structured or batched mode)",
            SinkKind.CHANGE_FEED, Set.of(SinkMode.PUSH),
            Map.of("type", "object", "properties", Map.of(
                    "url", Map.of("type", "string"),
                    "batchSize", Map.of("type", "integer", "minimum", 1, "maximum", MAX_BATCH, "default", 1))),
            List.of("hmacSecret"), 50);

    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;
    private final String instanceId;

    /** Creates the type. */
    public CloudEventsSinkType(OutboundUrlPolicy policy, PinnedHttp pinned,
            @Value("${foresignal.instance-id:foresignal}") String instanceId) {
        this.policy = policy;
        this.pinned = pinned;
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
            throw ai.foresignal.api.error.ValidationException.forField("config.url",
                    "is not allowed by the outbound URL policy");
        }
        Object batch = config.get("batchSize");
        if (batch != null) {
            int size = batch instanceof Number n ? n.intValue() : -1;
            if (size < 1 || size > MAX_BATCH) {
                throw ai.foresignal.api.error.ValidationException.forField("config.batchSize", "must be 1..100");
            }
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        int batch = Math.max(1, Math.min(MAX_BATCH, config.intConfig("batchSize", 1)));
        return new Instance(URI.create(config.config("url")), config.secrets().get("hmacSecret"), instanceId, policy,
                pinned, batch);
    }

    /** The CloudEvents attributes and {@code data} of one record. */
    static Map<String, Object> event(SinkRecord record, String instanceId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("specversion", "1.0");
        out.put("id", instanceId + ":" + record.seq());
        out.put("source", "urn:foresignal:" + instanceId + ":source:" + record.sourceId());
        out.put("type", "ai.foresignal.record." + eventType(record));
        out.put("subject", record.sourceId() + "/" + record.externalId());
        out.put("time", record.observedAt() == null ? null : record.observedAt().toString());
        out.put("datacontenttype", "application/json");
        out.put("foresignalseq", record.seq());
        out.put("foresignalkind", record.kind());
        out.put("foresignalreason", record.snapshot() ? "snapshot" : record.tombstone() ? "removed" : "observed");
        out.put("data", SinkSupport.statePayload(record));
        return out;
    }

    /** {@code snapshot}, {@code tombstoned}, {@code created} or {@code updated}. */
    static String eventType(SinkRecord record) {
        if (record.snapshot()) {
            return "snapshot";
        }
        if (record.tombstone()) {
            return "tombstoned";
        }
        return record.version() <= 1 ? "created" : "updated";
    }

    /**
     * The delay a {@code Retry-After} value asks for (delta-seconds or an HTTP date), or null when absent or
     * unreadable.
     */
    static Duration retryAfter(String value, Instant now) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        try {
            long seconds = Long.parseLong(text);
            return seconds < 0 ? null : Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            try {
                Instant at = ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                Duration delay = Duration.between(now, at);
                return delay.isNegative() ? Duration.ZERO : delay;
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    /** Classifies a non-2xx status: 408, 429 and 5xx are retryable; other 4xx are permanent. */
    static SinkDeliveryException failure(int status, Duration retryAfter) {
        boolean retryable = status == 408 || status == 429 || status >= 500;
        return new SinkDeliveryException("the receiver answered " + status, status, retryable ? retryAfter : null,
                !retryable);
    }

    /** The running instance. */
    private static final class Instance implements SinkInstance {
        private final URI url;
        private final String secret;
        private final String instanceId;
        private final OutboundUrlPolicy policy;
        private final PinnedHttp pinned;
        private final int batchSize;

        Instance(URI url, String secret, String instanceId, OutboundUrlPolicy policy, PinnedHttp pinned,
                int batchSize) {
            this.url = url;
            this.secret = secret;
            this.instanceId = instanceId;
            this.policy = policy;
            this.pinned = pinned;
            this.batchSize = batchSize;
        }

        @Override
        public int batchSize() {
            return batchSize;
        }

        @Override
        public boolean deliver(SinkRecord record) {
            send(STRUCTURED, JSON.writeValueAsBytes(event(record, instanceId)));
            return true;
        }

        @Override
        public int deliverAll(List<SinkRecord> records) {
            if (records.isEmpty()) {
                return 0;
            }
            if (batchSize <= 1 || records.size() == 1) {
                return SinkInstance.super.deliverAll(records);
            }
            List<Map<String, Object>> events = new ArrayList<>(records.size());
            for (SinkRecord record : records) {
                events.add(event(record, instanceId));
            }
            send(BATCH, JSON.writeValueAsBytes(events));
            return records.size();
        }

        /** Checks the URL now, pins the resolved address, posts and classifies the answer. */
        private void send(String contentType, byte[] body) {
            OutboundUrlPolicy.Result check = policy.check(url, OutboundUrlPolicy.Profile.PUSH_ENDPOINT, false);
            if (!(check instanceof OutboundUrlPolicy.Allowed allowed)) {
                throw new SinkDeliveryException("the outbound URL policy refuses " + url.getHost()
                        + " now (it may resolve to a different address than when the sink was saved)", null, null,
                        true);
            }
            String timestamp = Long.toString(System.currentTimeMillis() / 1000);
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(url, allowed.pinned()))
                        .header("Content-Type", contentType)
                        .header("X-Foresignal-Timestamp", timestamp)
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body));
                PinnedHttp.setHostHeader(builder, url);
                if (secret != null) {
                    builder.header(HmacSigner.HEADER, HmacSigner.sign(secret, timestamp, body));
                }
                HttpResponse<Void> response = pinned.client(url, CLIENT).send(builder.build(),
                        HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return;
                }
                throw failure(status, retryAfter(response.headers().firstValue("Retry-After").orElse(null),
                        Instant.now()));
            } catch (IOException e) {
                throw new SinkDeliveryException("the receiver could not be reached: " + e.getMessage(), null, null,
                        false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SinkDeliveryException("interrupted", null, null, false);
            }
        }

        @Override
        public SinkTestResult test(SinkRecord probe) {
            long start = System.nanoTime();
            try {
                deliver(probe);
                return new SinkTestResult(true, "send", "the receiver accepted the event",
                        (System.nanoTime() - start) / 1_000_000, Map.of());
            } catch (SinkDeliveryException e) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("status", e.status());
                details.put("permanent", e.permanent());
                return new SinkTestResult(false, "send", e.getMessage(), (System.nanoTime() - start) / 1_000_000,
                        details);
            }
        }

        @Override
        public void close() {
            // The pinned clients are shared; nothing to release.
        }
    }
}
