package ai.foresignal.ask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Maps Ask rows to their API shapes ({@code AskAction}, {@code AskMessage}, {@code AskThread}). */
public final class AskViews {

    private AskViews() {}

    /** {@code AskAction}. */
    public static Map<String, Object> action(AskActionEntity action) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", action.getId().toString());
        out.put("type", action.getType());
        out.put("description", action.getDescription());
        out.put("payload", action.getPayload());
        out.put("status", action.getStatus());
        out.put("result", action.getResult());
        out.put("createdAt", action.getCreatedAt().toString());
        out.put("executedAt", AskActionEntity.PENDING.equals(action.getStatus()) || action.getExecutedAt() == null
                ? null : action.getExecutedAt().toString());
        return out;
    }

    /** {@code AskMessage}: the shown text, never the raw turns. */
    public static Map<String, Object> message(AskMessageEntity message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("role", message.getRole());
        Object text = message.getContent() == null ? null : message.getContent().get("text");
        out.put("content", text == null ? "" : text.toString());
        if (AskMessageEntity.ASSISTANT.equals(message.getRole())) {
            out.put("citations", message.getCitations() == null ? List.of() : message.getCitations());
            out.put("warnings", message.getWarnings() == null ? List.of() : message.getWarnings());
        }
        out.put("createdAt", message.getCreatedAt().toString());
        return out;
    }

    /** {@code AskThread} with its messages and actions. */
    public static Map<String, Object> thread(AskThreadEntity thread, List<AskMessageEntity> messages,
            List<AskActionEntity> actions) {
        Map<String, Object> out = summary(thread);
        out.remove("messageCount");
        List<Map<String, Object>> shownMessages = new ArrayList<>(messages.size());
        for (AskMessageEntity message : messages) {
            shownMessages.add(message(message));
        }
        out.put("messages", shownMessages);
        List<Map<String, Object>> shownActions = new ArrayList<>(actions.size());
        for (AskActionEntity action : actions) {
            shownActions.add(action(action));
        }
        out.put("actions", shownActions);
        return out;
    }

    /** One {@code AskThreadPage} item. */
    public static Map<String, Object> summary(AskThreadEntity thread) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", thread.getId().toString());
        out.put("title", thread.getTitle());
        out.put("createdAt", thread.getCreatedAt().toString());
        out.put("updatedAt", thread.getUpdatedAt().toString());
        out.put("messageCount", thread.getMessageCount());
        return out;
    }
}
