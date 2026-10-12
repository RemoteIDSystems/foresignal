package ai.foresignal.inbound;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ntfy- and Gotify-compatible publish (prompt 024): one message becomes one record of the source.
 *
 * <p>Kind: the source's single kind, else {@code notification}, which must then be one of its
 * kinds. External id: the {@code X-Foresignal-External-Id} header, an ntfy tag {@code id=<x>}, or
 * Gotify {@code extras["foresignal::record"].externalId}, else a new ULID per message. Payload:
 * {@code {title, message, priority, tags, click, topic}}. {@code observedAt} is the receipt time:
 * these protocols carry no observation time, which is why the operator guide recommends
 * {@code observedAtMode=RECEIVED} and a TTL for such sources.
 */
@Service
public class CompatPublishService {

    /** The kind used when a source declares several. */
    public static final String NOTIFICATION_KIND = "notification";

    private final PushIngestService pushes;

    /** Creates the service. */
    public CompatPublishService(PushIngestService pushes) {
        this.pushes = pushes;
    }

    /**
     * One compatibility message.
     *
     * @param topic the ntfy topic (the source id), or null for Gotify
     * @param title the title, or null
     * @param message the message text, or null
     * @param priority the priority, or null
     * @param tags the tags, without an {@code id=} tag
     * @param click the click URL, or null
     * @param externalId an explicit external id, or null
     * @param extras Gotify extras, or null
     */
    public record Message(String topic, String title, String message, Integer priority, List<String> tags, String click,
            String externalId, Map<String, Object> extras) {}

    /**
     * A stored message.
     *
     * @param externalId the record's external id
     * @param receivedAt when it arrived
     * @param result the push result
     */
    public record Published(String externalId, Instant receivedAt, PushIngestService.PushResult result) {}

    /**
     * Stores {@code message} in {@code sourceId}.
     *
     * @throws ValidationException when the source has several kinds but no {@code notification}
     */
    public Published publish(String sourceId, IngestCaller caller, Message message, Instant receivedAt, long bytes,
            String bodySha256, String url) {
        SourceDescriptor descriptor = pushes.admit(sourceId, caller.principal(), receivedAt, url);
        String kind = kind(descriptor);
        String externalId = message.externalId() == null || message.externalId().isBlank()
                ? Ulids.next(receivedAt)
                : message.externalId().strip();
        NormalizedRecord.Builder record = NormalizedRecord.builder(externalId, kind)
                .observedAt(receivedAt)
                .put("title", message.title())
                .put("message", message.message())
                .put("priority", message.priority())
                .put("tags", message.tags() == null || message.tags().isEmpty() ? null : message.tags())
                .put("click", message.click())
                .put("topic", message.topic());
        ParsedIngest parsed = ParsedIngest.builder().add(0, record.build()).build();
        PushIngestService.PushResult result = pushes.submit(descriptor,
                new PushIngestService.PushRequest(sourceId, caller.principal(), parsed, receivedAt, bytes, bodySha256, url));
        return new Published(externalId, receivedAt, result);
    }

    /**
     * A message the loop guard dropped: acknowledged with an id, never stored.
     *
     * @return a published shape with an empty outcome
     */
    public Published dropped(Message message, Instant receivedAt) {
        String id = message.externalId() == null || message.externalId().isBlank()
                ? Ulids.next(receivedAt)
                : message.externalId().strip();
        return new Published(id, receivedAt, new PushIngestService.PushResult(
                ai.foresignal.ingest.IngestOutcome.empty(), IngestOutcomeView.of(null, 0, List.of())));
    }

    /** The kind compat messages use in {@code descriptor}. */
    static String kind(SourceDescriptor descriptor) {
        if (descriptor.kinds().size() == 1) {
            return descriptor.kinds().iterator().next();
        }
        if (descriptor.kinds().contains(NOTIFICATION_KIND)) {
            return NOTIFICATION_KIND;
        }
        throw ValidationException.forField("kind",
                "the source declares several kinds and none is '" + NOTIFICATION_KIND + "'");
    }

    /** An ntfy priority: 1–5 or min, low, default, high, max, urgent; null when absent. */
    public static Integer ntfyPriority(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.strip().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "1", "min" -> 1;
            case "2", "low" -> 2;
            case "3", "default" -> 3;
            case "4", "high" -> 4;
            case "5", "max", "urgent" -> 5;
            default -> throw new InvalidInputException("Priority must be 1–5 or min, low, default, high, max or urgent.");
        };
    }

    /**
     * Splits ntfy tags; an {@code id=<x>} tag is returned as the external id instead of a tag.
     *
     * @return the tags and the id from the tags (null when none)
     */
    public static Tags ntfyTags(String value) {
        List<String> tags = new ArrayList<>();
        String id = null;
        if (value != null) {
            for (String part : value.split(",")) {
                String tag = part.strip();
                if (tag.isEmpty()) {
                    continue;
                }
                if (tag.startsWith("id=") && tag.length() > 3) {
                    id = tag.substring(3);
                } else {
                    tags.add(tag);
                }
            }
        }
        return new Tags(tags, id);
    }

    /**
     * ntfy tags.
     *
     * @param tags the tags
     * @param externalId the {@code id=} tag's value, or null
     */
    public record Tags(List<String> tags, String externalId) {}

    /** The ntfy message object returned to the publisher. */
    public static Map<String, Object> ntfyResponse(Published published, Message message, String topic) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", published.externalId());
        out.put("time", published.receivedAt().getEpochSecond());
        out.put("event", "message");
        out.put("topic", topic);
        out.put("message", message.message() == null ? "" : message.message());
        if (message.title() != null) {
            out.put("title", message.title());
        }
        if (message.priority() != null) {
            out.put("priority", message.priority());
        }
        if (message.tags() != null && !message.tags().isEmpty()) {
            out.put("tags", message.tags());
        }
        if (message.click() != null) {
            out.put("click", message.click());
        }
        return out;
    }

    /** The Gotify message object returned to the publisher. */
    public static Map<String, Object> gotifyResponse(Published published, Message message) {
        long seq = published.result().outcome().toSeq();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", seq > 0 ? seq : published.receivedAt().toEpochMilli());
        out.put("appid", 1);
        out.put("date", published.receivedAt().toString());
        if (message.title() != null) {
            out.put("title", message.title());
        }
        out.put("message", message.message() == null ? "" : message.message());
        out.put("priority", message.priority() == null ? 0 : message.priority());
        out.put("extras", message.extras() == null ? Map.of() : message.extras());
        return out;
    }
}
