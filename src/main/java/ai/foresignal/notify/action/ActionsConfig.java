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

    /** Every action a channel may list (prompt 115 adds the two feedback variants). */
    public static final List<String> SUPPORTED = List.of(NotificationAction.ACK, NotificationAction.SNOOZE,
            NotificationAction.MUTE, NotificationAction.FEEDBACK_USEFUL, NotificationAction.FEEDBACK_NOISE);

    /** Types whose default list also carries the feedback variants (room for five buttons or links; prompt 115). */
    public static final Set<String> FEEDBACK_BY_DEFAULT = Set.of("telegram", "matrix");

    /**
     * The default list of a channel type: {@link #SUPPORTED} for Telegram and Matrix, {@link #DEFAULT_LIST}
     * elsewhere (ntfy and Web Push show two buttons; list the feedback variants there to get them).
     */
    public static List<String> defaultList(String type) {
        return FEEDBACK_BY_DEFAULT.contains(type) ? SUPPORTED : DEFAULT_LIST;
    }

    /** Copies the list. */
    public ActionsConfig {
        list = list == null ? DEFAULT_LIST : List.copyOf(list);
    }

    /** The settings of a channel of {@code type} with {@code config}. */
    public static ActionsConfig of(String type, Map<String, Object> config) {
        Object raw = config == null ? null : config.get("actions");
        Map<?, ?> actions = raw instanceof Map<?, ?> map ? map : Map.of();
        boolean enabled = bool(actions.get("enabled"), DEFAULT_ON.contains(type));
        List<String> list = defaultList(type);
        if (actions.get("list") instanceof List<?> items) {
            List<String> out = new ArrayList<>();
            for (Object item : items) {
                String name = String.valueOf(item);
                if (SUPPORTED.contains(name) && !out.contains(name)) {
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
