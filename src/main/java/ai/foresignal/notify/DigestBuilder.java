package ai.foresignal.notify;

import ai.foresignal.trigger.Severity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the one message that stands for several alerts (prompt 018): a quiet-hours or pause
 * release, a backlog fold ("+N more alerts") or a grouping window.
 *
 * <p>Items are listed newest first, at most {@code cap} of them, followed by {@code +N more alerts}
 * when there are more. The digest takes the highest severity of its items, the newest item's link,
 * and, when every item comes from one rule, that rule's name in its title.
 */
public final class DigestBuilder {

    /** The subtype of a digest message. */
    public static final String SUBTYPE = "alert.digest";

    private DigestBuilder() {}

    /**
     * The digest of {@code items}.
     *
     * @param items the notifications (any order; the digest's own notification first is not required)
     * @param cap the most items listed
     */
    public static NotificationMessage build(List<NotificationMessage> items, int cap) {
        List<NotificationMessage> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing((NotificationMessage m) -> m.id() == null ? 0L : m.id()).reversed());
        NotificationMessage newest = sorted.getFirst();
        Severity max = Severity.INFO;
        for (NotificationMessage m : sorted) {
            Severity s = Severity.fromWire(m.severity());
            if (s.compareTo(max) > 0) {
                max = s;
            }
        }
        String rule = commonRule(sorted);
        int count = sorted.size();
        String title = "[" + max.wire() + "] " + (rule == null ? "" : rule + ": ") + count + " alerts";
        StringBuilder body = new StringBuilder();
        int listed = Math.min(Math.max(1, cap), count);
        for (int i = 0; i < listed; i++) {
            NotificationMessage m = sorted.get(i);
            body.append("- **[").append(m.severity()).append("]** ").append(MarkdownRenderer.escapeMarkdown(m.title()))
                    .append('\n');
        }
        if (count > listed) {
            body.append("\n+").append(count - listed).append(" more alerts");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);
        data.put("notificationIds", sorted.stream().map(NotificationMessage::id).toList());
        if (rule != null) {
            data.put("ruleName", rule);
        }
        return new NotificationMessage(newest.id(), NotificationMessage.ALERT, SUBTYPE, title, count + " alerts",
                body.toString().strip(), max.wire(), newest.url(), newest.links(), data, List.of(), List.of(), null,
                newest.origin(), newest.createdAt(), null, rule == null ? null : newest.ruleId(), null);
    }

    private static String commonRule(List<NotificationMessage> items) {
        Object first = null;
        for (NotificationMessage m : items) {
            Object name = m.data() == null ? null : m.data().get("ruleName");
            if (name == null) {
                return null;
            }
            if (first == null) {
                first = name;
            } else if (!Objects.equals(first, name)) {
                return null;
            }
        }
        return first == null ? null : String.valueOf(first);
    }
}
