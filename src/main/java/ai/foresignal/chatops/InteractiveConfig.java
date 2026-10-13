package ai.foresignal.chatops;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The interactive-command members of a {@code telegram} or {@code matrix} channel's config (OpenAPI
 * {@code InteractiveChatConfig}, prompt 061).
 *
 * @param interactive whether the channel accepts commands (default false)
 * @param allowedChats the allowlisted chats, at most {@value #MAX_CHATS}
 * @param askRateLimitPerHour {@code /ask} questions per chat per hour (default 10; 0 turns {@code /ask} off)
 * @param commandRateLimit commands per chat per 10 minutes (default 30)
 * @param takeOverWebhook Telegram: delete a webhook found on the bot at start (default false)
 * @param discoveryMode {@code /whoami} answers any chat with its ids, for 10 minutes (default false)
 */
public record InteractiveConfig(boolean interactive, List<AllowedChat> allowedChats, int askRateLimitPerHour,
        int commandRateLimit, boolean takeOverWebhook, boolean discoveryMode) {

    /** The most allowlisted chats. */
    public static final int MAX_CHATS = 50;

    /** The default {@code askRateLimitPerHour}. */
    public static final int DEFAULT_ASK_RATE = 10;

    /** The default {@code commandRateLimit}. */
    public static final int DEFAULT_COMMAND_RATE = 30;

    /** The config keys this record reads. */
    public static final Set<String> KEYS = Set.of("interactive", "allowedChats", "askRateLimitPerHour",
            "commandRateLimit", "takeOverWebhook", "discoveryMode");

    /**
     * The scalar interactive fields added to the {@code telegram} and {@code matrix} catalog entries
     * ({@code listChannelTypes}); {@code allowedChats} is a list of objects, described by the
     * {@code InteractiveChatConfig} schema instead.
     */
    public static List<ai.foresignal.notify.ChannelFieldSpec> fieldSpecs(boolean telegram) {
        List<ai.foresignal.notify.ChannelFieldSpec> out = new ArrayList<>(List.of(
                ai.foresignal.notify.ChannelFieldSpec.optional("interactive", ai.foresignal.notify.ChannelFieldSpec.BOOLEAN,
                        "Accept chat commands from allowedChats (prompt 061)", false),
                ai.foresignal.notify.ChannelFieldSpec.optional("askRateLimitPerHour",
                        ai.foresignal.notify.ChannelFieldSpec.INTEGER, "/ask questions per chat per hour", DEFAULT_ASK_RATE),
                ai.foresignal.notify.ChannelFieldSpec.optional("commandRateLimit",
                        ai.foresignal.notify.ChannelFieldSpec.INTEGER, "Commands per chat per 10 minutes",
                        DEFAULT_COMMAND_RATE),
                ai.foresignal.notify.ChannelFieldSpec.optional("discoveryMode", ai.foresignal.notify.ChannelFieldSpec.BOOLEAN,
                        "/whoami answers any chat with its ids; turns itself off after 10 minutes", false)));
        if (telegram) {
            out.add(ai.foresignal.notify.ChannelFieldSpec.optional("takeOverWebhook",
                    ai.foresignal.notify.ChannelFieldSpec.BOOLEAN, "Delete a webhook found on the bot at start", false));
        }
        return out;
    }

    private static final Pattern TELEGRAM_ID = Pattern.compile("-?[0-9]{1,20}");
    private static final Pattern MATRIX_ROOM = Pattern.compile("![^:\\s]+:\\S+");
    private static final Pattern MATRIX_USER = Pattern.compile("@[^:\\s]+:\\S+");

    /** Copies the chats. */
    public InteractiveConfig {
        allowedChats = allowedChats == null ? List.of() : List.copyOf(allowedChats);
    }

    /** A channel that is not interactive. */
    public static InteractiveConfig off() {
        return new InteractiveConfig(false, List.of(), DEFAULT_ASK_RATE, DEFAULT_COMMAND_RATE, false, false);
    }

    /** True when {@code config} turns interactive mode on. */
    public static boolean enabledIn(Map<String, Object> config) {
        return config != null && Boolean.TRUE.equals(config.get("interactive"));
    }

    /**
     * Reads a stored (already validated) config leniently: unknown or malformed members fall back to the
     * defaults, a chat with an unknown scope or an {@code admin} scope is left out.
     */
    public static InteractiveConfig of(Map<String, Object> config) {
        if (config == null) {
            return off();
        }
        List<AllowedChat> chats = new ArrayList<>();
        if (config.get("allowedChats") instanceof List<?> items) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> map) {
                    AllowedChat chat = chat(map);
                    if (chat != null) {
                        chats.add(chat);
                    }
                }
            }
        }
        return new InteractiveConfig(Boolean.TRUE.equals(config.get("interactive")), chats,
                integer(config.get("askRateLimitPerHour"), DEFAULT_ASK_RATE),
                Math.max(1, integer(config.get("commandRateLimit"), DEFAULT_COMMAND_RATE)),
                Boolean.TRUE.equals(config.get("takeOverWebhook")), Boolean.TRUE.equals(config.get("discoveryMode")));
    }

    /** The allowlisted chat {@code chatId}, or null. */
    public AllowedChat chat(String chatId) {
        if (chatId == null) {
            return null;
        }
        for (AllowedChat chat : allowedChats) {
            if (chat.chatId().equals(chatId)) {
                return chat;
            }
        }
        return null;
    }

    /**
     * Validates the interactive members of a channel config of {@code type}.
     *
     * <ul>
     *   <li>{@code interactive}, {@code takeOverWebhook}, {@code discoveryMode}: booleans;
     *       {@code askRateLimitPerHour} ≥ 0 and {@code commandRateLimit} ≥ 1: integers;
     *   <li>{@code allowedChats}: at most {@value #MAX_CHATS} distinct chats, each with a {@code chatId} (a
     *       numeric string for Telegram, {@code !room:server} for Matrix) and a {@code scope} of {@code read}
     *       or {@code agent} ({@code admin} is never possible);
     *   <li>a Telegram group or channel (negative chat id) must list {@code userIds}, otherwise any member
     *       could command. Matrix rooms are checked for their member count when the connector starts.
     * </ul>
     *
     * @return the problems, empty when valid
     */
    public static List<FieldError> validate(String type, Map<String, Object> config) {
        List<FieldError> errors = new ArrayList<>();
        if (config == null) {
            return errors;
        }
        boolean telegram = TelegramChannel.ID.equals(type);
        for (String key : List.of("interactive", "takeOverWebhook", "discoveryMode")) {
            Object value = config.get(key);
            if (value != null && !(value instanceof Boolean)) {
                errors.add(new FieldError("config." + key, "must be true or false"));
            }
        }
        checkInteger(config, "askRateLimitPerHour", 0, errors);
        checkInteger(config, "commandRateLimit", 1, errors);
        Object raw = config.get("allowedChats");
        if (raw == null) {
            return errors;
        }
        if (!(raw instanceof List<?> items)) {
            errors.add(new FieldError("config.allowedChats", "must be an array"));
            return errors;
        }
        if (items.size() > MAX_CHATS) {
            errors.add(new FieldError("config.allowedChats", "must have at most " + MAX_CHATS + " entries"));
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            String field = "config.allowedChats[" + i + "]";
            if (!(items.get(i) instanceof Map<?, ?> map)) {
                errors.add(new FieldError(field, "must be an object"));
                continue;
            }
            for (Object key : map.keySet()) {
                if (!Set.of("chatId", "name", "scope", "userIds", "privateAccess").contains(String.valueOf(key))) {
                    errors.add(new FieldError(field + "." + key, "is not an AllowedChat member"));
                }
            }
            String chatId = idText(map.get("chatId"));
            if (chatId == null || chatId.isBlank()) {
                errors.add(new FieldError(field + ".chatId", "is required"));
            } else if (telegram && !TELEGRAM_ID.matcher(chatId).matches()) {
                errors.add(new FieldError(field + ".chatId", "must be a numeric Telegram chat id, e.g. -1001234567890"));
            } else if (!telegram && !MATRIX_ROOM.matcher(chatId).matches()) {
                errors.add(new FieldError(field + ".chatId", "must be a Matrix room id, e.g. !abc:example.org"));
            } else if (!seen.add(chatId)) {
                errors.add(new FieldError(field + ".chatId", "is listed twice"));
            }
            Object scope = map.get("scope");
            if (scope == null) {
                errors.add(new FieldError(field + ".scope", "is required"));
            } else if ("admin".equals(scope)) {
                errors.add(new FieldError(field + ".scope", "admin is never possible for a chat; use read or agent"));
            } else if (!"read".equals(scope) && !"agent".equals(scope)) {
                errors.add(new FieldError(field + ".scope", "must be read or agent"));
            }
            Object name = map.get("name");
            if (name != null && (!(name instanceof String text) || text.length() > 200)) {
                errors.add(new FieldError(field + ".name", "must be a string of at most 200 characters"));
            }
            Object privateAccess = map.get("privateAccess");
            if (privateAccess != null && !(privateAccess instanceof Boolean)) {
                errors.add(new FieldError(field + ".privateAccess", "must be true or false"));
            }
            List<String> users = new ArrayList<>();
            Object userIds = map.get("userIds");
            if (userIds != null) {
                if (!(userIds instanceof List<?> list)) {
                    errors.add(new FieldError(field + ".userIds", "must be an array"));
                } else {
                    for (Object user : list) {
                        String text = idText(user);
                        if (text == null || (telegram ? !TELEGRAM_ID.matcher(text).matches()
                                : !MATRIX_USER.matcher(text).matches())) {
                            errors.add(new FieldError(field + ".userIds", telegram
                                    ? "must hold numeric Telegram user ids" : "must hold Matrix user ids, e.g. @ana:example.org"));
                            break;
                        }
                        users.add(text);
                    }
                }
            }
            if (telegram && chatId != null && chatId.startsWith("-") && users.isEmpty()) {
                errors.add(new FieldError(field + ".userIds",
                        "is required for a Telegram group or channel; otherwise any member could command"));
            }
        }
        return errors;
    }

    /** The text of a chat or user id given as a string, or as an integer (Telegram), else null. */
    static String idText(Object value) {
        if (value instanceof String text) {
            return text.strip();
        }
        if (value instanceof Integer || value instanceof Long || value instanceof java.math.BigInteger) {
            return value.toString();
        }
        return null;
    }

    private static AllowedChat chat(Map<?, ?> map) {
        String chatId = idText(map.get("chatId"));
        Object scope = map.get("scope");
        if (chatId == null || chatId.isBlank() || !("read".equals(scope) || "agent".equals(scope))) {
            return null;
        }
        List<String> users = new ArrayList<>();
        if (map.get("userIds") instanceof List<?> list) {
            for (Object user : list) {
                String text = idText(user);
                if (text != null && !text.isBlank()) {
                    users.add(text);
                }
            }
        }
        return new AllowedChat(chatId, map.get("name") instanceof String name ? name : null,
                Scope.fromWireValue(String.valueOf(scope)), users, Boolean.TRUE.equals(map.get("privateAccess")));
    }

    private static void checkInteger(Map<String, Object> config, String key, int min, List<FieldError> errors) {
        Object value = config.get(key);
        if (value == null) {
            return;
        }
        if (!(value instanceof Integer || value instanceof Long) || ((Number) value).longValue() < min
                || ((Number) value).longValue() > 100_000) {
            errors.add(new FieldError("config." + key, "must be an integer from " + min + " to 100000"));
        }
    }

    private static int integer(Object value, int def) {
        return value instanceof Number n ? (int) Math.max(0, Math.min(100_000, n.longValue())) : def;
    }

    /** True for the channel types that can be interactive. */
    public static boolean supports(String type) {
        return TelegramChannel.ID.equals(type) || MatrixChannel.ID.equals(type);
    }
}
