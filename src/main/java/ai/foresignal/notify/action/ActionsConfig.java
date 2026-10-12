package ai.foresignal.notify.action;

import ai.foresignal.notify.NotificationAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A channel's {@code config.actions} ({@code ChannelActionsConfig}, prompt 033).
 *
 * <p>{@code enabled} defaults to true for the types with buttons or reactions ({@code ntfy},
 * {@code webpush}, {@code telegram}, {@code matrix}, {@code pushover}) and to false for every other
 * type, whose confirm-page links in the message body are an opt-in. {@code list} defaults to
 * {@code [ack, snooze, mute]}, {@code links} to true, {@code reactions} (Matrix) to false.
 *
 * @param enabled whether firing alerts on this channel carry actions
 * @param list the actions, in button order
 * @param links whether types without buttons add confirm-page links to the body
 * @param reactions Matrix: whether a ✅ reaction from {@code allowedSenders} acknowledges
 */
public record ActionsConfig(boolean enabled, List<String> list, boolean links, boolean reactions) {

    /** Types whose actions are on unless the channel turns them off. */
    public static final Set<String> DEFAULT_ON = Set.of("ntfy", "webpush", "telegram", "matrix", "pushover");

    /** The default action list. */
    public static final List<String> DEFAULT_LIST =
            List.of(NotificationAction.ACK, NotificationAction.SNOOZE, NotificationAction.MUTE);

    /** Copies the list. */
    public ActionsConfig {
        list = list == null ? DEFAULT_LIST : List.copyOf(list);
    }

    /** The settings of a channel of {@code type} with {@code config}. */
    public static ActionsConfig of(String type, Map<String, Object> config) {
        Object raw = config == null ? null : config.get("actions");
        Map<?, ?> actions = raw instanceof Map<?, ?> map ? map : Map.of();
        boolean enabled = bool(actions.get("enabled"), DEFAULT_ON.contains(type));
        List<String> list = DEFAULT_LIST;
        if (actions.get("list") instanceof List<?> items) {
            List<String> out = new ArrayList<>();
            for (Object item : items) {
                String name = String.valueOf(item);
                if (DEFAULT_LIST.contains(name) && !out.contains(name)) {
                    out.add(name);
                }
            }
            list = out;
        }
        return new ActionsConfig(enabled, list, bool(actions.get("links"), true), bool(actions.get("reactions"), false));
    }

    private static boolean bool(Object value, boolean def) {
        if (value instanceof Boolean b) {
            return b;
        }
        return value == null ? def : Boolean.parseBoolean(String.valueOf(value));
    }
}
