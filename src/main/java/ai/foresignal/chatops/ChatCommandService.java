package ai.foresignal.chatops;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.ask.AskActionEntity;
import ai.foresignal.ask.AskActionService;
import ai.foresignal.ask.AskCaller;
import ai.foresignal.ask.AskInput;
import ai.foresignal.ask.AskListener;
import ai.foresignal.ask.AskScope;
import ai.foresignal.ask.AskService;
import ai.foresignal.briefing.BriefingEntity;
import ai.foresignal.briefing.BriefingLinks;
import ai.foresignal.briefing.BriefingRepository;
import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.ingest.TitleResolver;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.stats.glance.GlanceService;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import ai.foresignal.trigger.Snoozes;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs the chat commands for an allowlisted chat's {@link ChatPrincipal} (prompt 061). The router has already
 * parsed the command, rate-limited it and checked the chat's scope against {@code AccessPolicy}; every service
 * call here takes the principal's own caller, so private sources, thread ownership and action ownership are
 * decided exactly as for a token. Replies carry summaries and links, never raw payloads.
 */
@Service
public class ChatCommandService {

    private static final Logger log = LoggerFactory.getLogger(ChatCommandService.class);

    /** The most alerts {@code /alerts} lists. */
    public static final int MAX_ALERTS = 10;

    /** The most characters of a briefing run shown by {@code /brief}. */
    public static final int BRIEF_CHARS = 1_500;

    /** The most characters of an Ask answer shown before the thread link. */
    public static final int ANSWER_CHARS = 3_000;

    /** The most citations listed under an Ask answer. */
    public static final int MAX_CITATIONS = 10;

    /** The most sources {@code /sources} lists. */
    public static final int MAX_SOURCES = 30;

    private final AlertService alerts;
    private final ChatPrincipals principals;
    private final GlanceService glance;
    private final HealthService health;
    private final AskService ask;
    private final AskActionService askActions;
    private final BriefingService briefings;
    private final BriefingRepository briefingRepository;
    private final BriefingLinks links;
    private final PendingTokens pending;
    private final ChatRateLimiter limiter;
    private final AppSettingService settings;
    private final ChatopsProperties properties;
    private final JsonMapper json;
    private final Clock clock;
    private final ExecutorService runs = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the service. */
    public ChatCommandService(AlertService alerts, ChatPrincipals principals, GlanceService glance, HealthService health,
            AskService ask, AskActionService askActions, BriefingService briefings,
            BriefingRepository briefingRepository, BriefingLinks links,
            PendingTokens pending, ChatRateLimiter limiter, AppSettingService settings, ChatopsProperties properties,
            JsonMapper json, Clock clock) {
        this.alerts = alerts;
        this.principals = principals;
        this.glance = glance;
        this.health = health;
        this.ask = ask;
        this.askActions = askActions;
        this.briefings = briefings;
        this.briefingRepository = briefingRepository;
        this.links = links;
        this.pending = pending;
        this.limiter = limiter;
        this.settings = settings;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    /** Stops waiting for briefing runs started from chats. */
    @jakarta.annotation.PreDestroy
    public void shutdown() {
        runs.shutdownNow();
    }

    // ------------------------------------------------------------------------- help and status

    /** {@code /help}: the commands this chat's scope allows. */
    public ChatOutcome help(ChatPrincipal principal, ChatRef ref) {
        StringBuilder out = new StringBuilder("**Foresignal commands** (").append(principal.scope().wireValue())
                .append(" scope)\n");
        for (ChatCommand command : ChatCommand.values()) {
            if (principals.allows(principal, command.method(), command.path())) {
                out.append("- /").append(command.word()).append(" — ").append(ChatText.esc(command.help())).append('\n');
            }
        }
        if (principals.allows(principal, org.springframework.http.HttpMethod.POST, ChatCommand.BRIEF_RUN_PATH)) {
            out.append("- /brief run <id|name> — run a briefing now\n");
        }
        return ChatOutcome.ok(out.toString().strip(), ref.messageId());
    }

    /** {@code /status}: the 038 text glance with full counts (the principal is authenticated). */
    public ChatOutcome status(ChatPrincipal principal, ChatRef ref) {
        String text = glance.text(new GlanceService.Request(principals.readCaller(principal),
                principals.excludedSources(principal), null));
        return ChatOutcome.ok("```\n" + text.replace("```", "'''") + "\n```", ref.messageId());
    }

    /** {@code /sources}: degraded and failing sources the chat may see, with their reasons. */
    public ChatOutcome sources(ChatPrincipal principal, ChatRef ref) {
        Set<String> excluded = principals.excludedSources(principal);
        List<SourceHealthView> bad = new ArrayList<>();
        for (SourceHealthView view : health.all(Optional.of(EnumSet.of(HealthStatus.DEGRADED, HealthStatus.FAILING)))) {
            if (!excluded.contains(view.sourceId())) {
                bad.add(view);
            }
        }
        if (bad.isEmpty()) {
            return ChatOutcome.ok("No source is degraded or failing.", ref.messageId());
        }
        bad.sort((a, b) -> a.status() != b.status() ? b.status().compareTo(a.status()) : a.sourceId().compareTo(b.sourceId()));
        StringBuilder out = new StringBuilder("**Sources needing a look**\n");
        for (SourceHealthView view : bad.subList(0, Math.min(MAX_SOURCES, bad.size()))) {
            out.append("- `").append(view.sourceId()).append("` ").append(view.status().value());
            if (view.statusReason() != null && !view.statusReason().isBlank()) {
                out.append(" — ").append(ChatText.esc(ChatText.cut(view.statusReason(), 160)));
            }
            out.append('\n');
        }
        if (bad.size() > MAX_SOURCES) {
            out.append("…and ").append(bad.size() - MAX_SOURCES).append(" more\n");
        }
        return ChatOutcome.ok(out.toString().strip(), ref.messageId());
    }

    // ------------------------------------------------------------------------- alerts

    /**
     * {@code /alerts [n] [critical|warning|info]}: open (firing, unacknowledged, unsuppressed) alerts, newest
     * first, at most {@value #MAX_ALERTS}; a severity lists that severity and above. Agent chats with buttons
     * get Ack and Resolve buttons.
     */
    public ChatOutcome alerts(ChatSession session, ChatPrincipal principal, ChatRef ref, List<String> args,
            boolean agentButtons) {
        int n = MAX_ALERTS;
        Severity min = null;
        if (args.size() > 2) {
            return usage(ChatCommand.ALERTS, ref);
        }
        for (String arg : args) {
            if (arg.matches("[0-9]{1,3}")) {
                n = Integer.parseInt(arg);
                if (n < 1 || n > MAX_ALERTS) {
                    return ChatOutcome.of(ChatOutcome.INVALID, "n must be from 1 to " + MAX_ALERTS + ".", ref.messageId());
                }
            } else if (Severity.WIRE_VALUES.contains(arg.toLowerCase(Locale.ROOT))) {
                min = Severity.fromWire(arg.toLowerCase(Locale.ROOT));
            } else {
                return usage(ChatCommand.ALERTS, ref);
            }
        }
        AlertQuery query = new AlertQuery(null, null, null, null, false, List.of("firing"), null, min, null, false,
                principals.excludedSources(principal));
        List<AlertView> open = alerts.list(query, null, n);
        if (open.isEmpty()) {
            return ChatOutcome.ok("No open alerts" + (min == null ? "" : " at " + min.wire() + " or above") + ".",
                    ref.messageId());
        }
        Instant now = clock.instant();
        StringBuilder out = new StringBuilder("**Open alerts**\n");
        List<List<ChatButton>> buttons = new ArrayList<>();
        for (AlertView alert : open) {
            out.append("- `").append(alert.id()).append("` ").append(alert.severity()).append(" ")
                    .append(ChatText.esc(shortTitle(alert)));
            String near = near(alert);
            if (near != null) {
                out.append(" — ").append(ChatText.esc(near));
            }
            out.append(" · ").append(ChatText.age(alert.firedAt(), now)).append(" ago · ")
                    .append(ChatText.link("open", links.alert(alert.id()))).append('\n');
            if (agentButtons) {
                buttons.add(List.of(new ChatButton("Ack " + alert.id(), pending.button(ref, PendingTokens.ALERT_ACK,
                                Long.toString(alert.id()))),
                        new ChatButton("Resolve " + alert.id(), pending.button(ref, PendingTokens.ALERT_RESOLVE,
                                Long.toString(alert.id())))));
            }
        }
        return new ChatOutcome(ChatOutcome.OK, new ChatReply(out.toString().strip(), buttons, ref.messageId()));
    }

    /** {@code /alert <id>}: rule, record summary (title, kind, source, observed time), state and link. */
    public ChatOutcome alert(ChatPrincipal principal, ChatRef ref, List<String> args) {
        Long id = alertId(args, 1);
        if (id == null) {
            return usage(ChatCommand.ALERT, ref);
        }
        Optional<AlertView> found = visibleAlert(principal, id);
        if (found.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No alert " + id + ".", ref.messageId());
        }
        AlertView a = found.get();
        Instant now = clock.instant();
        StringBuilder out = new StringBuilder("**Alert ").append(a.id()).append("** — ").append(a.severity())
                .append(", ").append(a.state()).append('\n');
        out.append("- Rule: ").append(ChatText.esc(a.ruleName() == null ? "(deleted rule)" : a.ruleName())).append('\n');
        String title = recordTitle(a);
        if (title != null) {
            out.append("- Record: ").append(ChatText.esc(title)).append('\n');
        }
        out.append("- Kind: ").append(ChatText.esc(a.kind() == null ? "?" : a.kind())).append(" · source `")
                .append(a.sourceId()).append("`\n");
        out.append("- Observed: ").append(a.observedAt()).append(" (").append(ChatText.age(a.observedAt(), now))
                .append(" ago)\n");
        String near = near(a);
        if (near != null) {
            out.append("- Near: ").append(ChatText.esc(near)).append('\n');
        }
        if (a.acknowledgedBy() != null) {
            out.append("- Acknowledged by ").append(ChatText.esc(a.acknowledgedBy())).append('\n');
        }
        if (a.resolvedAt() != null) {
            out.append("- Resolved ").append(ChatText.age(a.resolvedAt(), now)).append(" ago").append('\n');
        }
        if (a.snoozedUntil() != null && a.snoozedUntil().isAfter(now)) {
            out.append("- Snoozed until ").append(a.snoozedUntil()).append('\n');
        }
        out.append(ChatText.link("Open the alert", links.alert(a.id())));
        return ChatOutcome.ok(out.toString(), ref.messageId());
    }

    /** {@code /ack <id>}: acknowledges as the principal ({@code acknowledged_by} = its name). */
    public ChatOutcome ack(ChatPrincipal principal, ChatRef ref, List<String> args) {
        Long id = alertId(args, 1);
        return id == null ? usage(ChatCommand.ACK, ref) : acknowledge(principal, ref, id);
    }

    /** Acknowledges alert {@code id} for the principal. */
    public ChatOutcome acknowledge(ChatPrincipal principal, ChatRef ref, long id) {
        Optional<AlertView> found = visibleAlert(principal, id);
        if (found.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No alert " + id + ".", ref.messageId());
        }
        AlertView after = alerts.acknowledge(id, AlertService.Transition.NONE, actor(principal));
        return ChatOutcome.ok("Alert " + id + " (" + ChatText.esc(shortTitle(after)) + ") is " + after.state() + ".",
                ref.messageId());
    }

    /** {@code /resolve <id>}: resolves as the principal. */
    public ChatOutcome resolve(ChatPrincipal principal, ChatRef ref, List<String> args) {
        Long id = alertId(args, 1);
        return id == null ? usage(ChatCommand.RESOLVE, ref) : resolveAlert(principal, ref, id);
    }

    /** Resolves alert {@code id} for the principal. */
    public ChatOutcome resolveAlert(ChatPrincipal principal, ChatRef ref, long id) {
        Optional<AlertView> found = visibleAlert(principal, id);
        if (found.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No alert " + id + ".", ref.messageId());
        }
        AlertView after = alerts.resolve(id, AlertService.Transition.NONE, actor(principal));
        return ChatOutcome.ok("Alert " + id + " (" + ChatText.esc(shortTitle(after)) + ") is " + after.state() + ".",
                ref.messageId());
    }

    /**
     * {@code /feedback <id> <value>} (prompt 115): records {@code useful}, {@code noise}, {@code late} or
     * {@code false_positive} on the alert, in any state.
     */
    public ChatOutcome feedback(ChatPrincipal principal, ChatRef ref, List<String> args) {
        if (args.size() != 2) {
            return usage(ChatCommand.FEEDBACK, ref);
        }
        Long id = alertId(args.subList(0, 1), 1);
        String value;
        try {
            value = ai.foresignal.trigger.Feedback.fromWire(args.get(1).toLowerCase(java.util.Locale.ROOT)).wire();
        } catch (IllegalArgumentException e) {
            value = null;
        }
        if (id == null || value == null) {
            return usage(ChatCommand.FEEDBACK, ref);
        }
        Optional<AlertView> found = visibleAlert(principal, id);
        if (found.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No alert " + id + ".", ref.messageId());
        }
        alerts.setFeedback(id, new AlertService.Transition(null, value));
        return ChatOutcome.ok("Alert " + id + " marked " + value + ".", ref.messageId());
    }

    /** {@code /snooze <id> <duration>}: a positive duration such as {@code 2h}, at most 7 days. */
    public ChatOutcome snooze(ChatPrincipal principal, ChatRef ref, List<String> args) {
        if (args.size() != 2) {
            return usage(ChatCommand.SNOOZE, ref);
        }
        Long id = alertId(args.subList(0, 1), 1);
        Duration duration;
        try {
            duration = Durations.parsePositive(args.get(1));
        } catch (RuntimeException e) {
            duration = null;
        }
        if (id == null || duration == null) {
            return usage(ChatCommand.SNOOZE, ref);
        }
        if (duration.compareTo(Snoozes.MAX_RULE_SNOOZE) > 0) {
            return ChatOutcome.of(ChatOutcome.INVALID, "A snooze lasts at most 7 days.", ref.messageId());
        }
        Optional<AlertView> found = visibleAlert(principal, id);
        if (found.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No alert " + id + ".", ref.messageId());
        }
        Instant until = clock.instant().plus(duration);
        alerts.snooze(id, until);
        return ChatOutcome.ok("Alert " + id + " is snoozed until " + until + ".", ref.messageId());
    }

    // ------------------------------------------------------------------------- ask

    /**
     * {@code /ask <question>} or {@code /ask new}: the 028 AskService with one thread per chat principal, the
     * per-chat Ask limit and the LLM budget. The answer is cut with a link to the thread; citations become
     * links; proposed actions get Confirm and Reject buttons (Telegram) or a code (Matrix) in agent chats, and
     * are listed without a way to execute them in read chats.
     */
    public ChatOutcome ask(ChatSession session, ChatPrincipal principal, ChatRef ref, String question) {
        String text = question == null ? "" : question.strip();
        if (text.isEmpty()) {
            return usage(ChatCommand.ASK, ref);
        }
        if (text.equalsIgnoreCase("new")) {
            settings.put(threadKey(principal), "");
            return ChatOutcome.ok("Started a new conversation; your next /ask begins a fresh thread.", ref.messageId());
        }
        if (text.length() > AskInput.MAX_QUESTION) {
            return ChatOutcome.of(ChatOutcome.INVALID, "The question is too long.", ref.messageId());
        }
        Duration wait = limiter.ask(ref, session.config().interactive().askRateLimitPerHour());
        if (!wait.isZero()) {
            return ChatOutcome.of(ChatOutcome.RATE_LIMITED, "Ask is rate limited, retry in " + seconds(wait) + " s.",
                    ref.messageId());
        }
        AskCaller caller = principals.askCaller(principal);
        UUID thread = currentThread(principal);
        Map<String, Object> response;
        try {
            response = askOnce(text, thread, caller);
        } catch (NotFoundException e) {
            if (thread == null) {
                throw e;
            }
            response = askOnce(text, null, caller);
        }
        String threadId = String.valueOf(response.get("threadId"));
        settings.put(threadKey(principal), threadId);
        return new ChatOutcome(ChatOutcome.OK, answerReply(session, principal, ref, response, threadId));
    }

    private Map<String, Object> askOnce(String question, UUID thread, AskCaller caller) {
        return ask.ask(new AskInput(question, thread, AskScope.NONE, false), caller, AskListener.NONE);
    }

    private ChatReply answerReply(ChatSession session, ChatPrincipal principal, ChatRef ref,
            Map<String, Object> response, String threadId) {
        String threadUrl = links.baseUrl() + "/v1/ask/threads/" + threadId;
        String answer = String.valueOf(response.getOrDefault("answer", ""));
        StringBuilder out = new StringBuilder();
        if (answer.length() > ANSWER_CHARS) {
            out.append(ChatText.esc(ChatText.cut(answer, ANSWER_CHARS))).append('\n')
                    .append(ChatText.link("Read the full answer", threadUrl));
        } else {
            out.append(ChatText.esc(answer));
        }
        if (response.get("citations") instanceof List<?> citations && !citations.isEmpty()) {
            out.append("\n\n**Sources**\n");
            int shown = 0;
            for (Object item : citations) {
                if (!(item instanceof Map<?, ?> citation) || shown >= MAX_CITATIONS) {
                    continue;
                }
                shown++;
                String title = String.valueOf(citation.get("title"));
                String url = citationUrl(String.valueOf(citation.get("id")), String.valueOf(citation.get("type")));
                out.append("- ").append(url == null ? ChatText.esc(title) : ChatText.link(title, url)).append('\n');
            }
            if (citations.size() > MAX_CITATIONS) {
                out.append("- …and ").append(citations.size() - MAX_CITATIONS).append(" more\n");
            }
        }
        List<List<ChatButton>> buttons = new ArrayList<>();
        if (response.get("proposedActions") instanceof List<?> actions && !actions.isEmpty()) {
            boolean agent = principals.allows(principal, ChatCommand.CONFIRM.method(), ChatCommand.CONFIRM.path());
            out.append("\n**Proposed actions**\n");
            for (Object item : actions) {
                if (!(item instanceof Map<?, ?> action)) {
                    continue;
                }
                String actionId = String.valueOf(action.get("id"));
                String description = ChatText.esc(ChatText.cut(String.valueOf(action.get("description")), 300));
                boolean pendingAction = AskActionEntity.PENDING.equals(action.get("status"));
                out.append("- ").append(description);
                if (!pendingAction) {
                    out.append(" (").append(ChatText.esc(String.valueOf(action.get("status")))).append(")");
                } else if (!agent) {
                    out.append(" (needs an agent chat to confirm)");
                } else if (session.connector().supportsButtons()) {
                    buttons.add(List.of(
                            new ChatButton("Confirm", pending.button(ref, PendingTokens.ASK_CONFIRM, actionId)),
                            new ChatButton("Reject", pending.button(ref, PendingTokens.ASK_REJECT, actionId))));
                } else {
                    String code = pending.code(ref, PendingTokens.ASK_DECIDE, actionId);
                    out.append(" — reply `/confirm ").append(code).append("` or `/reject ").append(code).append('`');
                }
                out.append('\n');
            }
        }
        return new ChatReply(out.toString().strip(), buttons, ref.messageId());
    }

    /** Confirms ({@code confirm}) or rejects a proposed Ask action for the principal. */
    public ChatOutcome decide(ChatPrincipal principal, ChatRef ref, String actionId, boolean confirm) {
        UUID id;
        try {
            id = UUID.fromString(actionId);
        } catch (IllegalArgumentException e) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "That proposed action no longer exists.", ref.messageId());
        }
        AskCaller caller = principals.askCaller(principal);
        AskActionEntity action = confirm ? askActions.confirm(id, caller) : askActions.reject(id, caller);
        return ChatOutcome.ok("The proposed action is " + action.getStatus() + ": "
                + ChatText.esc(ChatText.cut(action.getDescription(), 300)), ref.messageId());
    }

    // ------------------------------------------------------------------------- briefings

    /** {@code /brief [id|name]}: the latest finished run's title, its first 1,500 characters and a link. */
    public ChatOutcome brief(ChatPrincipal principal, ChatRef ref, List<String> args) {
        Optional<BriefingRunEntity> run;
        if (args.isEmpty()) {
            run = latestOfAll();
        } else {
            Optional<BriefingEntity> briefing = findBriefing(String.join(" ", args));
            if (briefing.isEmpty()) {
                return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No briefing " + ChatText.esc(String.join(" ", args)) + ".",
                        ref.messageId());
            }
            run = latest(briefing.get().getId());
        }
        if (run.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No briefing has run yet.", ref.messageId());
        }
        return runReply(principal, ref, run.get());
    }

    /**
     * {@code /brief run <id|name>}: 029 run-now; replies when the run finishes, or with "running" and a link
     * after {@code replyWait}, then again when it finishes.
     */
    public ChatOutcome briefRun(ChatSession session, ChatPrincipal principal, ChatRef ref, List<String> args) {
        if (args.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.INVALID, "Usage: /brief run <id|name>", ref.messageId());
        }
        Optional<BriefingEntity> briefing = findBriefing(String.join(" ", args));
        if (briefing.isEmpty()) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND, "No briefing " + ChatText.esc(String.join(" ", args)) + ".",
                    ref.messageId());
        }
        UUID id = briefing.get().getId();
        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> briefings.runNow(id), runs);
        try {
            future.get(properties.replyWait().toMillis(), TimeUnit.MILLISECONDS);
            return latest(id).map(run -> runReply(principal, ref, run))
                    .orElseGet(() -> ChatOutcome.ok("The briefing ran.", ref.messageId()));
        } catch (TimeoutException e) {
            future.whenComplete((result, error) -> {
                ChatOutcome done = error != null
                        ? ChatOutcome.of(ChatOutcome.ERROR, "The run of " + ChatText.esc(briefing.get().getName())
                                + " failed.", null)
                        : latest(id).map(run -> runReply(principal, ref.withoutMessage(), run)).orElse(null);
                if (done != null && done.reply() != null) {
                    session.connector().reply(ref.withoutMessage(), done.reply());
                }
            });
            return ChatOutcome.ok("Running " + ChatText.esc(briefing.get().getName()) + "; I will reply when it finishes. "
                    + ChatText.link("Runs", links.baseUrl() + "/v1/briefings/" + id + "/runs"), ref.messageId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ChatOutcome.of(ChatOutcome.ERROR, "Interrupted.", ref.messageId());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ForesignalException fe) {
                throw fe;
            }
            log.warn("A briefing run from chat failed: {}", String.valueOf(e.getCause()));
            return ChatOutcome.of(ChatOutcome.ERROR, "The run failed.", ref.messageId());
        }
    }

    private ChatOutcome runReply(ChatPrincipal principal, ChatRef ref, BriefingRunEntity run) {
        // Prompt 039: a run that names a source hidden from this chat is withheld, as on the REST run listing.
        if (briefings.citesAny(run, principals.excludedSources(principal))) {
            return ChatOutcome.of(ChatOutcome.NOT_FOUND,
                    "The latest run of " + ChatText.esc(run.getBriefingName()) + " cites sources this chat may not see.",
                    ref.messageId());
        }
        Instant now = clock.instant();
        StringBuilder out = new StringBuilder("**").append(ChatText.esc(run.getBriefingName())).append("** — ")
                .append(run.getStatus()).append(", ").append(ChatText.age(run.getStartedAt(), now)).append(" ago\n\n");
        String content = run.getContentMarkdown() == null ? "" : run.getContentMarkdown();
        out.append(ChatText.cut(content, BRIEF_CHARS)).append("\n\n")
                .append(ChatText.link("Read the run", links.runHtml(run.getBriefingId(), run.getId())));
        return ChatOutcome.ok(out.toString(), ref.messageId());
    }

    private Optional<BriefingEntity> findBriefing(String key) {
        String wanted = key.strip();
        try {
            UUID id = UUID.fromString(wanted);
            return briefingRepository.findById(id);
        } catch (IllegalArgumentException e) {
            for (BriefingEntity b : briefingRepository.findAllByOrderByCreatedAtAscIdAsc()) {
                if (b.getName() != null && b.getName().equalsIgnoreCase(wanted)) {
                    return Optional.of(b);
                }
            }
            return Optional.empty();
        }
    }

    private Optional<BriefingRunEntity> latest(UUID briefingId) {
        try {
            return Optional.of(briefings.latest(briefingId));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<BriefingRunEntity> latestOfAll() {
        BriefingRunEntity best = null;
        for (BriefingEntity b : briefingRepository.findAllByOrderByCreatedAtAscIdAsc()) {
            Optional<BriefingRunEntity> run = latest(b.getId());
            if (run.isPresent() && (best == null || run.get().getStartedAt().isAfter(best.getStartedAt()))) {
                best = run.get();
            }
        }
        return Optional.ofNullable(best);
    }

    // ------------------------------------------------------------------------- helpers

    /** The usage line of {@code command} as an invalid outcome. */
    static ChatOutcome usage(ChatCommand command, ChatRef ref) {
        return ChatOutcome.of(ChatOutcome.INVALID, "Usage: " + ChatText.esc(command.help()), ref.messageId());
    }

    /** The actor recorded on alert transitions: the principal's name (at most 200 characters). */
    static String actor(ChatPrincipal principal) {
        return principal.name().length() > 200 ? principal.name().substring(0, 200) : principal.name();
    }

    /** The alert, when it exists and the principal may see its source. */
    Optional<AlertView> visibleAlert(ChatPrincipal principal, long id) {
        try {
            AlertView alert = alerts.get(id);
            return principals.excludedSources(principal).contains(alert.sourceId()) ? Optional.empty() : Optional.of(alert);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private static Long alertId(List<String> args, int expected) {
        if (args.size() != expected || !args.getFirst().matches("[0-9]{1,18}")) {
            return null;
        }
        return Long.parseLong(args.getFirst());
    }

    /** The short title: the rule name, else the message, at most 80 characters. */
    static String shortTitle(AlertView a) {
        String title = a.ruleName() != null && !a.ruleName().isBlank() ? a.ruleName() : a.message();
        return ChatText.cut(title == null ? "Alert " + a.id() : title, 80);
    }

    private static String near(AlertView a) {
        if (a.region() == null || a.region().isBlank()) {
            return null;
        }
        try {
            return RegionTags.fromJson(a.region()).nearText();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String recordTitle(AlertView a) {
        if (a.recordSnapshot() == null) {
            return null;
        }
        try {
            JsonNode payload = json.readTree(a.recordSnapshot()).path("payload");
            if (!payload.isObject()) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = json.convertValue(payload, Map.class);
            String title = TitleResolver.resolve((String) null, map);
            return title == null ? null : ChatText.cut(title, 160);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String citationUrl(String id, String type) {
        if ("alert".equals(type) && id.startsWith("alert:")) {
            try {
                return links.alert(Long.parseLong(id.substring("alert:".length())));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if ("record".equals(type)) {
            int colon = id.indexOf(':');
            if (colon > 0) {
                return links.record(id.substring(0, colon), id.substring(colon + 1));
            }
        }
        return null;
    }

    private UUID currentThread(ChatPrincipal principal) {
        String stored = settings.get(threadKey(principal), String.class).orElse("");
        try {
            return stored.isBlank() ? null : UUID.fromString(stored);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The {@code app_setting} key holding the principal's current Ask thread. */
    static String threadKey(ChatPrincipal principal) {
        return "chatops.thread." + principal.id();
    }

    private static long seconds(Duration wait) {
        return Math.max(1, (wait.toMillis() + 999) / 1000);
    }
}
