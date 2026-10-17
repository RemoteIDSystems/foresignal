package ai.foresignal.federation;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.provenance.JwsSigner;
import ai.foresignal.provenance.SigningKeyService;
import ai.foresignal.sink.SinkConfig;
import ai.foresignal.sink.SinkDeliveryException;
import ai.foresignal.sink.SinkInstance;
import ai.foresignal.sink.SinkKind;
import ai.foresignal.sink.SinkMode;
import ai.foresignal.sink.SinkRecord;
import ai.foresignal.sink.SinkTestResult;
import ai.foresignal.sink.SinkType;
import ai.foresignal.sink.SinkTypeSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code federation-push} (prompt 113; 110 Sink SPI, CHANGE_FEED, PUSH): an edge relay's durable push of its change
 * feed to its hub's {@code POST /v1/federation/ingest/{peerId}}.
 *
 * <p>The supervisor delivers versions in seq order from the sink's durable cursor, so an offline period holds the
 * cursor and the edge catches up without loss when the hub is reachable again; the 110 clamp release and resync
 * rules apply. Each batch (up to {@code batchSize}, default {@value #DEFAULT_BATCH}) is read back by seq through
 * {@link FederationFeedService#itemsBySeq} as full {@code FederationItem}s with provenance, egress-checked on
 * {@code FEDERATION} ({@code LOCAL_ONLY} records only when {@code sameOperator} is set: the operator's own hub, the
 * one documented exception), gzip-compressed, signed with this instance's key (detached JWS in
 * {@code Foresignal-Signature}, the public key in {@code Foresignal-Key} for the hub's first pin) and sent with the
 * peer ingest key ({@code fsp_}, secret {@code ingestKey}) as a Bearer token. The hub URL is checked by the outbound
 * URL policy ({@code LAN_ALLOWED}) at save and on every delivery, and the request goes to the resolved address.
 */
@Component
public class FederationPushSinkType implements SinkType {

    /** The type id. */
    public static final String ID = "federation-push";

    /** The default batch. */
    public static final int DEFAULT_BATCH = 500;

    private static final PinnedHttp.ClientSpec CLIENT =
            new PinnedHttp.ClientSpec(Duration.ofSeconds(10), HttpClient.Version.HTTP_1_1);

    private static final SinkTypeSpec SPEC = new SinkTypeSpec(ID, "Federation push",
            "An edge relay's change feed pushed to its hub (113): FederationItems with provenance, signed",
            SinkKind.CHANGE_FEED, Set.of(SinkMode.PUSH),
            Map.of("type", "object", "required", List.of("url", "peerId"), "properties", Map.of(
                    "url", Map.of("type", "string", "description", "The hub's base URL"),
                    "peerId", Map.of("type", "string", "description", "This edge's peer id on the hub"),
                    "sameOperator", Map.of("type", "boolean", "default", false,
                            "description", "The hub is the operator's own: LOCAL_ONLY records may be pushed"),
                    "batchSize", Map.of("type", "integer", "minimum", 1, "maximum", FederationIngestService.MAX_ITEMS,
                            "default", DEFAULT_BATCH))),
            List.of("ingestKey"), 50);

    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;
    private final FederationFeedService feed;
    private final JwsSigner signer;
    private final SigningKeyService keys;
    private final JsonMapper json;

    /** Creates the type. */
    public FederationPushSinkType(OutboundUrlPolicy policy, PinnedHttp pinned, FederationFeedService feed,
            JwsSigner signer, SigningKeyService keys, JsonMapper json) {
        this.policy = policy;
        this.pinned = pinned;
        this.feed = feed;
        this.signer = signer;
        this.keys = keys;
        this.json = json;
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
            throw ValidationException.forField("config.url", "is required");
        }
        if (!policy.check(String.valueOf(url), OutboundUrlPolicy.Profile.LAN_ALLOWED, true).allowed()) {
            throw ValidationException.forField("config.url", "is not allowed by the outbound URL policy");
        }
        Object peerId = config.get("peerId");
        if (!(peerId instanceof String id) || !PeerService.ID.matcher(id).matches()) {
            throw ValidationException.forField("config.peerId", "is required: this edge's peer id on the hub");
        }
        Object batch = config.get("batchSize");
        if (batch != null && (!(batch instanceof Number n) || n.intValue() < 1
                || n.intValue() > FederationIngestService.MAX_ITEMS)) {
            throw ValidationException.forField("config.batchSize", "must be 1.." + FederationIngestService.MAX_ITEMS);
        }
        String key = secrets == null ? null : secrets.get("ingestKey");
        if (key != null && !key.startsWith(PeerService.PREFIX)) {
            throw ValidationException.forField("secrets.ingestKey", "must be a peer ingest key (fsp_…)");
        }
    }

    @Override
    public SinkInstance open(SinkConfig config) {
        String base = config.config("url");
        base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        URI target = URI.create(base + "/v1/federation/ingest/" + config.config("peerId"));
        int batch = Math.max(1, Math.min(FederationIngestService.MAX_ITEMS, config.intConfig("batchSize", DEFAULT_BATCH)));
        return new Instance(target, config.secrets().get("ingestKey"), config.boolConfig("sameOperator", false), batch);
    }

    /** True when {@code config} marks the hub as the operator's own (the gate's same-operator exception). */
    public static boolean sameOperator(Map<String, Object> config) {
        return config != null && Boolean.TRUE.equals(config.get("sameOperator"));
    }

    /** The running push. */
    private final class Instance implements SinkInstance {

        private final URI target;
        private final String key;
        private final boolean sameOperator;
        private final int batchSize;

        Instance(URI target, String key, boolean sameOperator, int batchSize) {
            this.target = target;
            this.key = key;
            this.sameOperator = sameOperator;
            this.batchSize = batchSize;
        }

        @Override
        public int batchSize() {
            return batchSize;
        }

        @Override
        public boolean deliver(SinkRecord record) {
            return deliverAll(List.of(record)) == 1;
        }

        @Override
        public int deliverAll(List<SinkRecord> records) {
            if (records.isEmpty()) {
                return 0;
            }
            List<Long> seqs = new ArrayList<>(records.size());
            for (SinkRecord record : records) {
                if (record.seq() > 0) {
                    seqs.add(record.seq());
                }
            }
            List<Map<String, Object>> items = feed.itemsBySeq(seqs, FederationFeedService.Caller.push(sameOperator));
            if (!items.isEmpty()) {
                post(items);
            }
            return records.size();
        }

        private void post(List<Map<String, Object>> items) {
            if (key == null || key.isBlank()) {
                throw new SinkDeliveryException("no peer ingest key is set (secret ingestKey)", null, null, true);
            }
            OutboundUrlPolicy.Result check = policy.check(target, OutboundUrlPolicy.Profile.LAN_ALLOWED, true);
            if (!(check instanceof OutboundUrlPolicy.Allowed allowed)) {
                throw new SinkDeliveryException("the outbound URL policy refuses " + target.getHost() + " now", null,
                        null, true);
            }
            Map<String, Object> batch = new LinkedHashMap<>();
            batch.put("items", items);
            byte[] body = json.writeValueAsBytes(batch);
            String signature = signer.signDetached(body, "application/json");
            byte[] gzip = gzip(body);
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(target, allowed.pinned()))
                        .header("Content-Type", "application/json")
                        .header("Content-Encoding", "gzip")
                        .header("Authorization", "Bearer " + key)
                        .header(PeerRemote.SIGNATURE_HEADER, signature)
                        .header(FederationIngestService.KEY_HEADER,
                                Base64.getUrlEncoder().withoutPadding().encodeToString(keys.active().x()))
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofByteArray(gzip));
                PinnedHttp.setHostHeader(builder, target);
                HttpResponse<Void> response = pinned.client(target, CLIENT).send(builder.build(),
                        HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return;
                }
                boolean retryable = status == 408 || status == 429 || status >= 500;
                Duration retryAfter = response.headers().firstValue("Retry-After").map(v -> {
                    try {
                        return Duration.ofSeconds(Long.parseLong(v.strip()));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }).orElse(null);
                throw new SinkDeliveryException("the hub answered " + status, status, retryable ? retryAfter : null,
                        !retryable);
            } catch (IOException e) {
                throw new SinkDeliveryException("the hub could not be reached: " + e.getMessage(), null, null, false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SinkDeliveryException("interrupted", null, null, false);
            }
        }

        @Override
        public SinkTestResult test(SinkRecord probe) {
            long start = System.nanoTime();
            try {
                post(List.of());
                return new SinkTestResult(true, "send", "the hub accepted an empty batch",
                        (System.nanoTime() - start) / 1_000_000, Map.of());
            } catch (SinkDeliveryException e) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("status", e.status());
                details.put("permanent", e.permanent());
                return new SinkTestResult(false, "send", e.getMessage(), (System.nanoTime() - start) / 1_000_000,
                        details);
            }
        }
    }

    /** {@code body} gzip-compressed. */
    static byte[] gzip(byte[] body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, body.length / 4));
        try (GZIPOutputStream zip = new GZIPOutputStream(out)) {
            zip.write(body);
        } catch (IOException e) {
            throw new IllegalStateException("gzip failed", e);
        }
        return out.toByteArray();
    }
}
