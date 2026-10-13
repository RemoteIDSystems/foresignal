package ai.foresignal.chatops;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Routes what an interactive channel reads (prompt 061).
 *
 * <ol>
 *   <li><b>Who:</b> the chat must be in {@code allowedChats} and, when the entry lists {@code userIds}, the
 *       sender among them. Anyone else is ignored silently: no reply, the counter
 *       {@code foresignal.chatops.rejected{channel}} and a debug log line. With {@code discoveryMode} on,
 *       {@code /whoami} from any chat replies with only its chat id and user id.
 *   <li><b>What:</b> strict parsing. A message that does not start with {@code /} is not a command and is
 *       ignored; more than {@value #MAX_LENGTH} characters is refused; an unknown command gets a short help.
 *   <li><b>Limits:</b> {@code commandRateLimit} commands per chat per 10 minutes, else "rate limited, retry in
 *       N s".
 *   <li><b>Scope:</b> each command is allowed only when {@code AccessPolicy} allows the REST operation it
 *       mirrors to a token of the chat's scope ({@link ChatPrincipals#allows}); admin is never possible.
 *   <li><b>Audit:</b> every command from an allowlisted chat is written to {@code audit_event} (actor kind
 *       {@value #ACTOR_KIND}) with the principal, the command, its argument names (never their values), the
 *       result and the duration.
 * </ol>
 *
 * Message text is only ever a command argument or an Ask question; it is never executed.
 */
@Component
public class CommandRouter {

    private static final Logger log = LoggerFactory.getLogger(CommandRouter.class);

    /** The longest accepted message. */
    public static final int MAX_LENGTH = 2_000;

    /** The audit actor kind of chat commands. */
    public static final String ACTOR_KIND = "chat";

    /** The audit target type of chat commands. */
    public static final String AUDIT_TARGET = "chatops_command";

    /** The counter of messages from chats or users that are not allowlisted. */
    public static final String REJECTED_METRIC = "foresignal.chatops.rejected";

    private final ChatCommandService commands;
    private final ChatPrincipals principals;
    private final PendingTokens pending;
    private final ChatRateLimiter limiter;
    private final DiscoveryMode discovery;
    private final AuditService audit;
    private final MeterRegistry meters;
    private final Clock clock;

    /** Creates the router. */
    public CommandRouter(ChatCommandService commands, ChatPrincipals principals, PendingTokens pending,
            ChatRateLimiter limiter, DiscoveryMode discovery, AuditService audit, MeterRegistry meters, Clock clock) {
        this.commands = commands;
        this.principals = principals;
        this.pending = pending;
        this.limiter = limiter;
        this.discovery = discovery;
        this.audit = audit;
        this.meters = meters;
        this.clock = clock;
    }

    /**
     * A parsed command line.
     *
     * @param word the command word, lower case, without the slash
     * @param args the whitespace-separated arguments
     * @param rest everything after the command word, trimmed (the Ask question)
     */
    public record Parsed(String word, List<String> args, String rest) {}

    /**
     * Parses {@code /word arg …}; empty when the text is not a command. {@code /word@Bot} must already have been
     * normalized by the connector.
     */
    public static Optional<Parsed> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String line = text.strip();
        if (line.length() < 2 || line.charAt(0) != '/') {
            return Optional.empty();
        }
        int space = indexOfWhitespace(line);
        String word = (space < 0 ? line.substring(1) : line.substring(1, space)).toLowerCase(Locale.ROOT);
        if (!word.matches("[a-z][a-z0-9_]{0,31}")) {
            return Optional.empty();
        }
        String rest = space < 0 ? "" : line.substring(space).strip();
        List<String> args = rest.isEmpty() ? List.of() : Arrays.asList(rest.split("\\s+"));
        return Optional.of(new Parsed(word, List.copyOf(args), rest));
    }

    /**
     * Handles a text message from {@code ref} on {@code session}.
     *
     * @return the outcome, or empty when nothing was replied (ignored)
     */
    public Optional<ChatOutcome> onMessage(ChatSession session, ChatRef ref, String text) {
        ChannelConfig config = session.config();
        AllowedChat chat = config.interactive().chat(ref.chatId());
        boolean allowed = chat != null && chat.allows(ref.userId()) && session.connector().state().active(ref.chatId());
        Optional<Parsed> parsed = parse(text);
        if (parsed.isPresent() && "whoami".equals(parsed.get().word()) && discovery.activeUntil(config) != null) {
            ChatOutcome outcome = ChatOutcome.ok("chat id: `" + ref.chatId() + "`\nuser id: `"
                    + (ref.userId() == null ? "?" : ref.userId()) + "`", ref.messageId());
            session.connector().reply(ref, outcome.reply());
            return Optional.of(outcome);
        }
        if (!allowed) {
            reject(config, ref, "not allowlisted");
            return Optional.empty();
        }
        if (parsed.isEmpty()) {
            return Optional.empty();
        }
        ChatPrincipal principal = ChatPrincipal.of(config.channelId(), chat, ref.userId());
        Instant started = clock.instant();
        Parsed command = parsed.get();
        ChatOutcome outcome;
        List<String> argNames = List.of();
        if (text.length() > MAX_LENGTH) {
            outcome = ChatOutcome.of(ChatOutcome.INVALID, "The message is too long (at most " + MAX_LENGTH
                    + " characters).", ref.messageId());
        } else {
            Duration wait = limiter.command(ref, config.interactive().commandRateLimit());
            Optional<ChatCommand> known = ChatCommand.of(command.word());
            if (known.isPresent()) {
                argNames = argumentNames(known.get(), command);
            }
            if (!wait.isZero()) {
                outcome = ChatOutcome.of(ChatOutcome.RATE_LIMITED, "rate limited, retry in "
                        + Math.max(1, (wait.toMillis() + 999) / 1000) + " s", ref.messageId());
            } else if (known.isEmpty()) {
                outcome = ChatOutcome.of(ChatOutcome.INVALID, "Unknown command /" + ChatText.esc(command.word())
                        + ". " + shortHelp(principal), ref.messageId());
            } else {
                outcome = run(session, principal, ref, known.get(), command);
            }
        }
        if (outcome.reply() != null) {
            session.connector().reply(ref, outcome.reply());
        }
        audit(config, ref, principal, command.word(), argNames, outcome.result(), started);
        return Optional.of(outcome);
    }

    /**
     * Handles a button press: the token must belong to this chat and presser, be unused and unexpired, and its
     * action must be allowed at the chat's scope. The press is answered and the result also replied.
     *
     * @return the outcome, or empty when the presser is not allowlisted (ignored)
     */
    public Optional<ChatOutcome> onCallback(ChatSession session, ChatRef ref, String callbackRef, String token) {
        ChannelConfig config = session.config();
        AllowedChat chat = config.interactive().chat(ref.chatId());
        if (chat == null || !chat.allows(ref.userId()) || !session.connector().state().active(ref.chatId())) {
            reject(config, ref, "button press not allowlisted");
            session.connector().answerCallback(callbackRef, "Not allowed.");
            return Optional.empty();
        }
        ChatPrincipal principal = ChatPrincipal.of(config.channelId(), chat, ref.userId());
        Instant started = clock.instant();
        PendingTokens.Claim claim = pending.claim(token, ref);
        ChatOutcome outcome;
        String word = "button";
        if (!claim.ok()) {
            outcome = ChatOutcome.of(ChatOutcome.NOT_FOUND, claim.reason(), ref.messageId());
        } else {
            word = "button:" + claim.action();
            outcome = runClaim(principal, ref, claim);
        }
        String answer = outcome.reply() == null ? "Done." : ChatText.cut(plain(outcome.reply().markdown()), 190);
        session.connector().answerCallback(callbackRef, answer);
        if (outcome.reply() != null && ChatOutcome.OK.equals(outcome.result())) {
            session.connector().reply(ref, outcome.reply());
        }
        audit(config, ref, principal, word, List.of("token"), outcome.result(), started);
        return Optional.of(outcome);
    }

    private ChatOutcome run(ChatSession session, ChatPrincipal principal, ChatRef ref, ChatCommand command,
            Parsed parsed) {
        boolean briefRun = command == ChatCommand.BRIEF && !parsed.args().isEmpty()
                && "run".equalsIgnoreCase(parsed.args().getFirst());
        HttpMethod method = briefRun ? HttpMethod.POST : command.method();
        String path = briefRun ? ChatCommand.BRIEF_RUN_PATH : command.path();
        if (!principals.allows(principal, method, path)) {
            return ChatOutcome.of(ChatOutcome.DENIED, "This chat has " + principal.scope().wireValue()
                    + " scope; /" + command.word() + (briefRun ? " run" : "") + " needs agent scope.", ref.messageId());
        }
        try {
            return switch (command) {
                case HELP -> commands.help(principal, ref);
                case STATUS -> commands.status(principal, ref);
                case ALERTS -> commands.alerts(session, principal, ref, parsed.args(),
                        session.connector().supportsButtons()
                                && principals.allows(principal, ChatCommand.ACK.method(), ChatCommand.ACK.path()));
                case ALERT -> commands.alert(principal, ref, parsed.args());
                case ACK -> commands.ack(principal, ref, parsed.args());
                case RESOLVE -> commands.resolve(principal, ref, parsed.args());
                case SNOOZE -> commands.snooze(principal, ref, parsed.args());
                case ASK -> commands.ask(session, principal, ref, parsed.rest());
                case BRIEF -> briefRun ? commands.briefRun(session, principal, ref, parsed.args().subList(1, parsed.args().size()))
                        : commands.brief(principal, ref, parsed.args());
                case SOURCES -> commands.sources(principal, ref);
                case CONFIRM, REJECT -> decideByCode(principal, ref, parsed.args(), command == ChatCommand.CONFIRM);
            };
        } catch (ForesignalException e) {
            return ChatOutcome.of(e.getStatus().value() == 404 ? ChatOutcome.NOT_FOUND : ChatOutcome.UNAVAILABLE,
                    ChatText.esc(ChatText.cut(e.getMessage(), 300)), ref.messageId());
        } catch (RuntimeException e) {
            log.warn("Chat command /{} on channel {} failed", command.word(), session.config().channelId(), e);
            return ChatOutcome.of(ChatOutcome.ERROR, "Something went wrong; the operator can check the log.",
                    ref.messageId());
        }
    }

    private ChatOutcome decideByCode(ChatPrincipal principal, ChatRef ref, List<String> args, boolean confirm) {
        if (args.size() != 1) {
            return ChatCommandService.usage(confirm ? ChatCommand.CONFIRM : ChatCommand.REJECT, ref);
        }
        PendingTokens.Claim claim = pending.claim(args.getFirst(), ref);
        if (!claim.ok()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, claim.reason(), ref.messageId());
        }
        if (!PendingTokens.ASK_DECIDE.equals(claim.action())) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "That code is not a confirmation.", ref.messageId());
        }
        return commands.decide(principal, ref, claim.ref(), confirm);
    }

    private ChatOutcome runClaim(ChatPrincipal principal, ChatRef ref, PendingTokens.Claim claim) {
        try {
            return switch (claim.action()) {
                case PendingTokens.ALERT_ACK -> allowedOr(principal, ref, ChatCommand.ACK,
                        () -> commands.acknowledge(principal, ref, Long.parseLong(claim.ref())));
                case PendingTokens.ALERT_RESOLVE -> allowedOr(principal, ref, ChatCommand.RESOLVE,
                        () -> commands.resolveAlert(principal, ref, Long.parseLong(claim.ref())));
                case PendingTokens.ASK_CONFIRM -> allowedOr(principal, ref, ChatCommand.CONFIRM,
                        () -> commands.decide(principal, ref, claim.ref(), true));
                case PendingTokens.ASK_REJECT -> allowedOr(principal, ref, ChatCommand.REJECT,
                        () -> commands.decide(principal, ref, claim.ref(), false));
                default -> ChatOutcome.of(ChatOutcome.NOT_FOUND, "This button is not valid.", ref.messageId());
            };
        } catch (ForesignalException e) {
            return ChatOutcome.of(e.getStatus().value() == 404 ? ChatOutcome.NOT_FOUND : ChatOutcome.UNAVAILABLE,
                    ChatText.esc(ChatText.cut(e.getMessage(), 300)), ref.messageId());
        } catch (RuntimeException e) {
            log.warn("A chat button on channel {} failed", ref.channelId(), e);
            return ChatOutcome.of(ChatOutcome.ERROR, "Something went wrong.", ref.messageId());
        }
    }

    private ChatOutcome allowedOr(ChatPrincipal principal, ChatRef ref, ChatCommand command,
            java.util.function.Supplier<ChatOutcome> action) {
        if (!principals.allows(principal, command.method(), command.path())) {
            return ChatOutcome.of(ChatOutcome.DENIED, "This chat's scope does not allow that.", ref.messageId());
        }
        return action.get();
    }

    /** One short line naming the commands this scope allows. */
    String shortHelp(ChatPrincipal principal) {
        List<String> words = new ArrayList<>();
        for (ChatCommand command : ChatCommand.values()) {
            if (principals.allows(principal, command.method(), command.path())) {
                words.add("/" + command.word());
            }
        }
        return "Try " + String.join(", ", words) + ".";
    }

    private void reject(ChannelConfig config, ChatRef ref, String why) {
        meters.counter(REJECTED_METRIC, "channel", config.channelId().toString()).increment();
        log.debug("Ignored a message on interactive channel {} from chat {} user {}: {}", config.channelId(),
                ref.chatId(), ref.userId(), why);
    }

    private void audit(ChannelConfig config, ChatRef ref, ChatPrincipal principal, String command,
            List<String> argNames, String result, Instant started) {
        Instant now = clock.instant();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("channelId", config.channelId().toString());
        detail.put("chatId", ref.chatId());
        detail.put("command", command);
        detail.put("arguments", argNames);
        detail.put("result", result);
        detail.put("durationMs", Math.max(0, Duration.between(started, now).toMillis()));
        audit.record(new AuditRecord(now, ACTOR_KIND, principal.id(), principal.name(), null,
                principal.scope().wireValue(), null, null, "chatops." + command, null, AUDIT_TARGET,
                config.channelId().toString(), null, detail));
    }

    /** The argument names of {@code parsed} for the audit: as many of the command's names as were given. */
    static List<String> argumentNames(ChatCommand command, Parsed parsed) {
        if (parsed.args().isEmpty()) {
            return List.of();
        }
        if (command == ChatCommand.ASK) {
            return "new".equalsIgnoreCase(parsed.rest()) ? List.of("new") : List.of("question");
        }
        if (command == ChatCommand.BRIEF && "run".equalsIgnoreCase(parsed.args().getFirst())) {
            return parsed.args().size() > 1 ? List.of("run", "briefing") : List.of("run");
        }
        List<String> names = command.argumentNames();
        return names.subList(0, Math.min(names.size(), parsed.args().size()));
    }

    private static String plain(String markdown) {
        return ai.foresignal.notify.MarkdownRenderer.render(markdown, ai.foresignal.notify.MarkdownRenderer.Format.PLAIN);
    }

    private static int indexOfWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
