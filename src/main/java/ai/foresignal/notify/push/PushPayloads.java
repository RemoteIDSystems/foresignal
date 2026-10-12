package ai.foresignal.notify.push;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The plaintext payloads of Web Push (prompt 031, OpenAPI {@code PushPayload} and
 * {@code DeclarativePushPayload}).
 *
 * <ul>
 *   <li><b>{@code foresignal}</b>: {@code {v:1, event, id, alertId?, ruleId?, severity, title,
 *       shortTitle, body, url (alias of links.api), links {api, app, web}, tags, lang, tag, silent,
 *       ts, actions?, confirmUrl?}}; absent links and null members are omitted. {@code actions}
 *       (at most two {@code {action, title, url}}) and {@code confirmUrl} are the capability
 *       buttons of a firing alert (prompt 033). The verification push is
 *       {@code {v:1, event:"verify", subscriptionId, code}}.
 *   <li><b>{@code declarative}</b> (WebKit Declarative Web Push, Safari 18.4+, no service worker
 *       needed): {@code {"web_push": 8030, "notification": {title, body, navigate, lang, tag,
 *       silent, data}}} where {@code navigate} is {@code links.web ?? links.api} and {@code data} is
 *       the Foresignal payload without title, body and actions; a firing alert with actions
 *       navigates to its confirm page instead (prompt 033). {@code app_badge} is never sent (a crash was
 *       reported in Safari 18.5).
 * </ul>
 */
public final class PushPayloads {

    /** The Foresignal JSON format. */
    public static final String FORESIGNAL = "foresignal";

    /** The WebKit declarative format. */
    public static final String DECLARATIVE = "declarative";

    /** The declarative opt-in value of {@code web_push}. */
    public static final int WEB_PUSH_MAGIC = 8030;

    private PushPayloads() {}

    /**
     * The editable fields of one payload, which {@link PushPayloadBudget} shrinks.
     *
     * @param message the content
     * @param lang the effective language
     * @param title the title (may be shortened)
     * @param body the body (may be shortened)
     * @param linkApi the API link (may be dropped)
     * @param tags the tags (may be capped)
     */
    public record Fields(PushMessage message, String lang, String title, String body, String linkApi, List<String> tags) {

        /** The fields of {@code message} in {@code lang}. */
        public static Fields of(PushMessage message, String lang) {
            return new Fields(message, lang, message.title(), message.body(), message.linkApi(), message.tags());
        }
    }

    /** The payload of {@code fields} in {@code format}. */
    public static Map<String, Object> render(Fields fields, String format) {
        Map<String, Object> foresignal = foresignal(fields);
        return DECLARATIVE.equals(format) ? declarative(fields, foresignal) : foresignal;
    }

    /** The minimal fallback {@code {v, event, id, severity, shortTitle}}. */
    public static Map<String, Object> minimal(PushMessage m, String format) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("v", 1);
        out.put("event", m.event());
        putIfPresent(out, "id", m.id());
        putIfPresent(out, "severity", m.severity());
        putIfPresent(out, "shortTitle", m.shortTitle());
        if (!DECLARATIVE.equals(format)) {
            return out;
        }
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("title", m.shortTitle() == null ? "Foresignal" : m.shortTitle());
        notification.put("navigate", navigate(m.linkWeb(), null));
        notification.put("data", out);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("web_push", WEB_PUSH_MAGIC);
        wrapper.put("notification", notification);
        return wrapper;
    }

    static Map<String, Object> foresignal(Fields f) {
        PushMessage m = f.message();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("v", 1);
        out.put("event", m.event());
        if (PushMessage.VERIFY.equals(m.event())) {
            out.put("subscriptionId", String.valueOf(m.subscriptionId()));
            out.put("code", m.code());
            return out;
        }
        putIfPresent(out, "id", m.id());
        putIfPresent(out, "alertId", m.alertId());
        putIfPresent(out, "ruleId", m.ruleId() == null ? null : m.ruleId().toString());
        putIfPresent(out, "severity", m.severity());
        putIfPresent(out, "title", f.title());
        putIfPresent(out, "shortTitle", m.shortTitle());
        putIfPresent(out, "body", f.body());
        putIfPresent(out, "url", f.linkApi());
        Map<String, Object> links = new LinkedHashMap<>();
        putIfPresent(links, "api", f.linkApi());
        putIfPresent(links, "app", m.linkApp());
        putIfPresent(links, "web", m.linkWeb());
        if (!links.isEmpty()) {
            out.put("links", links);
        }
        out.put("tags", new ArrayList<>(f.tags()));
        putIfPresent(out, "lang", f.lang());
        putIfPresent(out, "tag", m.tag());
        out.put("silent", m.silent());
        out.put("ts", m.ts());
        if (!m.actions().isEmpty()) {
            List<Map<String, Object>> actions = new ArrayList<>();
            for (PushMessage.Action action : m.actions()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("action", action.action());
                item.put("title", action.title());
                item.put("url", action.url());
                actions.add(item);
            }
            out.put("actions", actions);
        }
        putIfPresent(out, "confirmUrl", m.confirmUrl());
        return out;
    }

    static Map<String, Object> declarative(Fields f, Map<String, Object> foresignal) {
        PushMessage m = f.message();
        Map<String, Object> data = new LinkedHashMap<>(foresignal);
        data.remove("title");
        data.remove("body");
        // Declarative notifications have no service worker to POST: no buttons, the confirm page only (033).
        data.remove("actions");
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("title", f.title() == null || f.title().isBlank() ? "Foresignal" : f.title());
        putIfPresent(notification, "body", f.body());
        notification.put("navigate", m.confirmUrl() != null ? m.confirmUrl() : navigate(m.linkWeb(), f.linkApi()));
        putIfPresent(notification, "lang", f.lang());
        putIfPresent(notification, "tag", m.tag());
        notification.put("silent", m.silent());
        notification.put("data", data);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("web_push", WEB_PUSH_MAGIC);
        out.put("notification", notification);
        return out;
    }

    private static String navigate(String web, String api) {
        if (web != null) {
            return web;
        }
        return api != null ? api : "about:blank";
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
