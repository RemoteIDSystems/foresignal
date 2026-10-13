package ai.foresignal.chatops;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mints and redeems the pending confirmations of {@code chatops_pending} (prompt 061).
 *
 * <ul>
 *   <li><b>Button tokens</b> (Telegram): {@code cc_} plus 16 base64url characters of 12 random bytes,
 *       19 bytes in all, well within the 64 bytes of {@code callback_data}. The {@code cc_} prefix tells
 *       the single Telegram poller's dispatcher a chat-command press from a 033 signed action ({@code ea:}).
 *   <li><b>Codes</b> (Matrix): 6 characters of {@value #CODE_ALPHABET} (no 0, O, 1 or I), for
 *       {@code /confirm <code>} and {@code /reject <code>}; matched case-insensitively.
 * </ul>
 *
 * Each lasts {@link #TTL}, is single-use and is bound to the chat and user it was shown to.
 */
@Component
public class PendingTokens {

    /** How long a token or code stays valid. */
    public static final Duration TTL = Duration.ofHours(1);

    /** The prefix of chat-command button tokens. */
    public static final String BUTTON_PREFIX = "cc_";

    /** The characters of a Matrix confirmation code. */
    public static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /** The length of a Matrix confirmation code. */
    public static final int CODE_LENGTH = 6;

    /** Acknowledge an alert ({@code ref} = alert id). */
    public static final String ALERT_ACK = "alert.ack";

    /** Resolve an alert ({@code ref} = alert id). */
    public static final String ALERT_RESOLVE = "alert.resolve";

    /** Confirm a proposed Ask action ({@code ref} = action id). */
    public static final String ASK_CONFIRM = "ask.confirm";

    /** Reject a proposed Ask action ({@code ref} = action id). */
    public static final String ASK_REJECT = "ask.reject";

    /** A Matrix code that confirms or rejects a proposed Ask action ({@code ref} = action id). */
    public static final String ASK_DECIDE = "ask.decide";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the component. */
    public PendingTokens(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Mints a Telegram button token. */
    public String button(ChatRef chat, String action, String ref) {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        String token = BUTTON_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        insert(token, chat, action, ref);
        return token;
    }

    /** Mints a Matrix confirmation code. */
    public String code(ChatRef chat, String action, String ref) {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            if (insert(code.toString(), chat, action, ref)) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Could not mint a unique confirmation code");
    }

    /**
     * Redeems {@code token} for {@code chat}'s sender: it must exist, be unused and unexpired, and belong to
     * the same channel, chat and user. A redeemed token is marked used in the same statement, so a second
     * press is refused.
     */
    @Transactional
    public Claim claim(String token, ChatRef chat) {
        if (token == null || token.isBlank() || token.length() > 64) {
            return Claim.refused("This button or code is not valid.");
        }
        String key = token.startsWith(BUTTON_PREFIX) ? token : token.strip().toUpperCase(Locale.ROOT);
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT channel_id, chat_id, user_id, action, ref, expires_at, used_at
                  FROM chatops_pending WHERE token = ? FOR UPDATE
                """, key);
        if (rows.isEmpty()) {
            return Claim.refused("This button or code is not valid.");
        }
        Map<String, Object> row = rows.getFirst();
        if (!chat.channelId().equals(row.get("channel_id")) || !chat.chatId().equals(row.get("chat_id"))
                || !Objects.equals(row.get("user_id"), chat.userId())) {
            return Claim.refused("This button belongs to someone else.");
        }
        if (row.get("used_at") != null) {
            return Claim.refused("This button or code was already used.");
        }
        Instant now = clock.instant();
        Instant expires = ((Timestamp) row.get("expires_at")).toInstant();
        if (!expires.isAfter(now)) {
            return Claim.refused("This button or code expired.");
        }
        jdbc.update("UPDATE chatops_pending SET used_at = ? WHERE token = ?", Timestamp.from(now), key);
        return new Claim(true, (String) row.get("action"), (String) row.get("ref"), null);
    }

    /** Deletes tokens that expired more than a day ago. */
    public int purge() {
        return jdbc.update("DELETE FROM chatops_pending WHERE expires_at < ?",
                Timestamp.from(clock.instant().minus(Duration.ofDays(1))));
    }

    private boolean insert(String token, ChatRef chat, String action, String ref) {
        return jdbc.update("""
                INSERT INTO chatops_pending (token, channel_id, chat_id, user_id, action, ref, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (token) DO NOTHING
                """, token, chat.channelId(), chat.chatId(), chat.userId(), action, ref,
                Timestamp.from(clock.instant().plus(TTL))) == 1;
    }

    /**
     * The outcome of {@link #claim}.
     *
     * @param ok true when the token was redeemed
     * @param action the action, when redeemed
     * @param ref what it acts on, when redeemed
     * @param reason why it was refused, else null
     */
    public record Claim(boolean ok, String action, String ref, String reason) {

        /** A refusal for {@code reason}. */
        static Claim refused(String reason) {
            return new Claim(false, null, null, reason);
        }
    }
}
