package ai.foresignal.notify;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The OpenAPI {@code NotificationMessage} JSON shape of a message, used by the {@code webhook} and
 * Home Assistant default bodies and the {@code notifications} and {@code system} SSE topics.
 */
public final class NotificationJson {

    private NotificationJson() {}

    /** The message as an ordered map, nulls included where the schema allows them. */
    public static Map<String, Object> toMap(NotificationMessage m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.id());
        out.put("event", m.event());
        out.put("subtype", m.subtype());
        out.put("title", m.title());
        out.put("shortTitle", m.shortTitle());
        out.put("bodyMarkdown", m.bodyMarkdown());
        out.put("severity", m.severity());
        out.put("url", m.url());
        List<Map<String, Object>> links = new ArrayList<>();
        for (NotificationMessage.Link link : m.links()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", link.label());
            item.put("url", link.url());
            links.add(item);
        }
        out.put("links", links);
        out.put("data", m.data());
        out.put("tags", m.tags());
        List<Map<String, Object>> attachments = new ArrayList<>();
        for (NotificationMessage.Attachment a : m.attachments()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("filename", a.filename());
            item.put("contentType", a.contentType());
            item.put("bytes", a.size());
            item.put("mediaId", a.mediaId());
            item.put("url", a.url());
            attachments.add(item);
        }
        out.put("attachments", attachments);
        out.put("updateOf", m.updateOf());
        out.put("origin", m.origin());
        out.put("createdAt", m.createdAt() == null ? null : m.createdAt().toString());
        // Exercise messages (prompt 115) say so in every serialized form.
        out.put("exercise", m.exercise());
        return out;
    }
}
