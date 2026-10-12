package ai.foresignal.ask;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ask threads: listing, reading, deleting, and storing one question with its answer (prompt 028).
 *
 * <p>A thread belongs to the token that created it. {@code listAskThreads} and {@code getAskThread}
 * show a token its own threads (an anonymous caller only anonymous threads); an {@code admin} token
 * sees every thread. Anything else is {@code 404}, as if it did not exist.
 */
@Service
public class AskThreadService {

    /** The largest page. */
    public static final int MAX_LIMIT = 5000;

    /** The default page size. */
    public static final int DEFAULT_LIMIT = 500;

    private final AskThreadRepository threads;
    private final AskMessageRepository messages;
    private final AskActionRepository actions;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public AskThreadService(AskThreadRepository threads, AskMessageRepository messages, AskActionRepository actions,
            JdbcTemplate jdbc, Clock clock) {
        this.threads = threads;
        this.messages = messages;
        this.actions = actions;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * {@code listAskThreads}: the caller's threads, most recently active first.
     *
     * @param caller who asks
     * @param cursor the previous page's {@code nextCursor}, or null
     * @param limit the page size
     * @return {@code AskThreadPage}
     */
    public Map<String, Object> list(AskCaller caller, String cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, title, token_id, principal_id, message_count, created_at,"
                + " updated_at FROM ask_thread WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (!caller.admin()) {
            if (caller.tokenId() == null) {
                sql.append(" AND token_id IS NULL");
            } else {
                sql.append(" AND token_id = ?");
                args.add(caller.tokenId());
            }
        }
        if (cursor != null && !cursor.isBlank()) {
            Position after = decode(cursor);
            sql.append(" AND (updated_at < ? OR (updated_at = ? AND id < ?))");
            args.add(Timestamp.from(after.updatedAt()));
            args.add(Timestamp.from(after.updatedAt()));
            args.add(after.id());
        }
        sql.append(" ORDER BY updated_at DESC, id DESC LIMIT ?");
        args.add(limit + 1);
        List<AskThreadEntity> rows = jdbc.query(sql.toString(), (rs, n) -> {
            AskThreadEntity thread = new AskThreadEntity(rs.getObject("id", UUID.class), rs.getString("title"),
                    rs.getObject("token_id", UUID.class), rs.getString("principal_id"),
                    rs.getTimestamp("created_at").toInstant());
            thread.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            thread.setMessageCount(rs.getInt("message_count"));
            return thread;
        }, args.toArray());
        boolean more = rows.size() > limit;
        List<AskThreadEntity> page = more ? rows.subList(0, limit) : rows;
        List<Map<String, Object>> items = new ArrayList<>(page.size());
        for (AskThreadEntity thread : page) {
            items.add(AskViews.summary(thread));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? encode(page.getLast()) : null);
        return out;
    }

    /**
     * The thread {@code id} when {@code caller} may see it.
     *
     * @throws NotFoundException otherwise
     */
    public AskThreadEntity visible(UUID id, AskCaller caller) {
        AskThreadEntity thread = threads.findById(id).orElseThrow(() -> NotFoundException.of("Ask thread", id));
        if (!caller.owns(thread.getTokenId())) {
            throw NotFoundException.of("Ask thread", id);
        }
        return thread;
    }

    /** {@code getAskThread}: the thread with its messages (citations and warnings included) and actions. */
    public Map<String, Object> get(UUID id, AskCaller caller) {
        AskThreadEntity thread = visible(id, caller);
        return AskViews.thread(thread, messages.findByThreadIdOrderByPositionAsc(id),
                actions.findByThreadIdOrderByCreatedAtAscIdAsc(id));
    }

    /** The stored messages of a thread, in order. */
    public List<AskMessageEntity> messages(UUID threadId) {
        return messages.findByThreadIdOrderByPositionAsc(threadId);
    }

    /** {@code deleteAskThread}: the thread, its messages and its actions. */
    @Transactional
    public void delete(UUID id, AskCaller caller) {
        AskThreadEntity thread = visible(id, caller);
        actions.deleteByThread(thread.getId());
        messages.deleteByThread(thread.getId());
        threads.delete(thread);
    }

    /**
     * Stores one exchange in a single transaction: the thread (created on the first question, else
     * touched), the question, the answer and the proposed actions ({@code pending}).
     *
     * @param exchange what to store
     * @return the stored answer and actions
     */
    @Transactional
    public Stored store(Exchange exchange) {
        Instant now = clock.instant();
        AskThreadEntity thread = threads.findById(exchange.threadId()).orElse(null);
        if (thread == null) {
            thread = new AskThreadEntity(exchange.threadId(), AskThreadEntity.titleOf(exchange.question()),
                    exchange.caller().tokenId(),
                    exchange.caller().token() == null ? null : exchange.caller().token().principalId(), now);
        }
        int position = thread.getMessageCount();
        thread.setMessageCount(position + 2);
        thread.setUpdatedAt(now);
        threads.save(thread);
        messages.save(new AskMessageEntity(thread.getId(), position, AskMessageEntity.USER, exchange.userContent(), now));
        AskMessageEntity answer = new AskMessageEntity(thread.getId(), position + 1, AskMessageEntity.ASSISTANT,
                exchange.assistantContent(), now);
        answer.setCitations(exchange.citations());
        answer.setWarnings(exchange.warnings());
        answer.setUsage(exchange.usage());
        answer.setMode(exchange.mode());
        answer.setStopReason(exchange.stopReason());
        answer.setRefusalCategory(exchange.refusalCategory());
        AskMessageEntity savedAnswer = messages.save(answer);
        List<AskActionEntity> stored = new ArrayList<>();
        for (AskActionEntity action : exchange.actions()) {
            action.setMessageId(savedAnswer.getId());
            stored.add(actions.save(action));
        }
        return new Stored(thread, savedAnswer, stored);
    }

    /**
     * One question and its answer, ready to store.
     *
     * @param threadId the thread (new or existing)
     * @param caller who asked
     * @param question the question
     * @param userContent the question's {@code content}
     * @param assistantContent the answer's {@code content}
     * @param citations the verified citations
     * @param warnings the warnings
     * @param usage the usage
     * @param mode {@code tools} or {@code retrieval}
     * @param stopReason the stop reason
     * @param refusalCategory the refusal category, or null
     * @param actions the proposed actions, pending
     */
    public record Exchange(UUID threadId, AskCaller caller, String question, Map<String, Object> userContent,
            Map<String, Object> assistantContent, List<Map<String, Object>> citations, List<String> warnings,
            Map<String, Object> usage, String mode, String stopReason, String refusalCategory,
            List<AskActionEntity> actions) {}

    /**
     * What {@link #store} wrote.
     *
     * @param thread the thread
     * @param answer the answer message
     * @param actions the actions
     */
    public record Stored(AskThreadEntity thread, AskMessageEntity answer, List<AskActionEntity> actions) {}

    private record Position(Instant updatedAt, UUID id) {}

    private static String encode(AskThreadEntity last) {
        Instant at = last.getUpdatedAt();
        String raw = at.getEpochSecond() + "." + at.getNano() + ":" + last.getId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static Position decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int colon = raw.indexOf(':');
            String[] time = raw.substring(0, colon).split("\\.");
            Instant at = Instant.ofEpochSecond(Long.parseLong(time[0]), Long.parseLong(time[1]));
            return new Position(at, UUID.fromString(raw.substring(colon + 1)));
        } catch (RuntimeException e) {
            throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new FieldError("cursor", "is not a cursor from this list")));
        }
    }
}
