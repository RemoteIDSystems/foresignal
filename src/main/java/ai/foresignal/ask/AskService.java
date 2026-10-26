package ai.foresignal.ask;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.RawTurnCodec;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.llm.client.ToolCall;
import ai.foresignal.llm.client.ToolLoop;
import ai.foresignal.llm.client.ToolSpec;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.mcp.ToolCatalog;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.preference.UserZones;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertView;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Answers a question from Foresignal's own data (prompt 028, Architecture §21, FR-49).
 *
 * <p><strong>Preconditions.</strong> {@code llm.askEnabled} and an assigned chat provider, else
 * {@code 409 LLM_NOT_CONFIGURED}. Ask spends LLM budget, so its routes are always authenticated
 * ({@code AccessPolicy}, invariant SEC-5); a budget refusal is {@code 429 LLM_BUDGET_EXCEEDED}.
 *
 * <p><strong>Tools mode.</strong> The {@link ToolLoop} (at most {@value #MAX_ITERATIONS} iterations,
 * {@code 120 s}, token cap {@code llm.askMaxTokens}, effort {@code medium}) runs with the caller's read
 * tools from the {@link ToolCatalog} plus the {@code propose_*} tools. Every read call gets the
 * question's scope injected ({@link ScopeInjector}); every result is filtered for LLM egress and scope
 * ({@link ToolOutputFilter}), recorded as citable ({@link CitableExtractor}) and wrapped as
 * {@code {"tool", "data"}}. Sources the provider may not receive ({@link EgressPolicy}: private
 * sources for a non-local provider) are excluded from the tools' reads in the first place.
 *
 * <p><strong>Retrieval-only mode</strong> ({@code supportsTools=false}, or the provider rejects tools):
 * a hybrid search on the question within scope (top {@value #RETRIEVAL_LIMIT}) plus the alerts of the
 * last 24 h, filtered the same way, in one completion with the same citation rules.
 *
 * <p><strong>Answer.</strong> The citations are verified ({@link CitationVerifier}); dropped
 * citations, uncited claims, refusals and incomplete answers become warnings. A refusal is answered
 * (200) with an explanation and a warning. The question, the answer with its citations and warnings,
 * the raw conversation turns and the proposed actions are stored together; with {@code allowActions}
 * and agent scope the actions are then executed and still recorded.
 *
 * <p><strong>Threads.</strong> A follow-up replays every stored turn of the thread unchanged (the
 * provider's raw turns, byte for byte) and appends the new question: history is append-only.
 */
@Service
public class AskService {

    /** Tool-loop iterations at most. */
    public static final int MAX_ITERATIONS = 12;

    /** The tool-loop deadline. */
    public static final Duration DEADLINE = Duration.ofSeconds(120);

    /** Search hits in retrieval-only mode. */
    public static final int RETRIEVAL_LIMIT = 40;

    /** The window of alerts in retrieval-only mode. */
    public static final Duration RETRIEVAL_ALERTS = Duration.ofHours(24);

    /** Alerts in retrieval-only mode at most. */
    static final int RETRIEVAL_ALERT_LIMIT = 50;

    /** The longest search query taken from a question. */
    static final int MAX_SEARCH_QUERY = 1_000;

    /** The {@code ToolContext} transport of Ask tool calls. */
    public static final String TRANSPORT = "ask";

    /** {@code mode} of tools mode. */
    public static final String TOOLS = "tools";

    /** {@code mode} of retrieval-only mode. */
    public static final String RETRIEVAL = "retrieval";

    private final LlmService llm;
    private final LlmSettingsService llmSettings;
    private final LlmProviderService providers;
    private final ToolCatalog catalog;
    private final AskActionTools actionTools;
    private final ToolOutputFilter outputFilter;
    private final EgressPolicy egress;
    private final List<CitableExtractor> extractors;
    private final CitationVerifier verifier;
    private final AskThreadService threads;
    private final AskActionService actions;
    private final SearchService search;
    private final AlertService alerts;
    private final AlertMapper alertMapper;
    private final McpOutputShaper shaper;
    private final UserZones zones;
    private final ObjectProvider<AskScopeResolver> scopeResolvers;
    private final RawTurnCodec turns;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public AskService(LlmService llm, LlmSettingsService llmSettings, LlmProviderService providers, ToolCatalog catalog,
            AskActionTools actionTools, ToolOutputFilter outputFilter, EgressPolicy egress,
            List<CitableExtractor> extractors, CitationVerifier verifier, AskThreadService threads,
            AskActionService actions, SearchService search, AlertService alerts, AlertMapper alertMapper,
            McpOutputShaper shaper, UserZones zones, ObjectProvider<AskScopeResolver> scopeResolvers,
            JsonMapper json, Clock clock) {
        this.llm = llm;
        this.llmSettings = llmSettings;
        this.providers = providers;
        this.catalog = catalog;
        this.actionTools = actionTools;
        this.outputFilter = outputFilter;
        this.egress = egress;
        this.extractors = List.copyOf(extractors);
        this.verifier = verifier;
        this.threads = threads;
        this.actions = actions;
        this.search = search;
        this.alerts = alerts;
        this.alertMapper = alertMapper;
        this.shaper = shaper;
        this.zones = zones;
        this.scopeResolvers = scopeResolvers;
        this.turns = new RawTurnCodec(json);
        this.json = json;
        this.clock = clock;
    }

    /**
     * Checks that Ask can run at all: {@code 409 LLM_NOT_CONFIGURED} otherwise.
     *
     * @return the LLM settings
     */
    public LlmSettings requireConfigured() {
        return requireConfigured(null);
    }

    /**
     * {@link #requireConfigured()} for a question asked by {@code actor} (prompt R2-011): a user's enabled personal chat
     * provider serves Ask even when no shared chat provider is assigned.
     *
     * @param actor the asking user's principal, or null for the machine
     * @return the LLM settings
     */
    public LlmSettings requireConfigured(String actor) {
        LlmSettings settings = llmSettings.get();
        if (!settings.askEnabled()) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                    "Ask is disabled; set askEnabled in PUT /v1/llm/settings.");
        }
        if (llm.chatProviderFor(actor, settings) == null) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                    "No chat provider is configured; add one in /v1/llm/providers and assign it in PUT /v1/llm/settings.");
        }
        return settings;
    }

    /**
     * Checks everything that can be checked before the model runs (configuration, scope, thread), so
     * the streaming endpoint can answer a problem status instead of opening a stream.
     *
     * @return the prepared question
     */
    public Prepared prepare(AskInput input, AskCaller asker) {
        // R2-011: the asking user is captured here, on the request thread, because the answer may stream from another.
        String actor = ai.foresignal.llm.LlmActor.current();
        LlmSettings settings = requireConfigured(actor);
        String providerId = llm.chatProviderFor(actor, settings);
        AskScope scope = resolveScope(input.scope());
        AskCaller caller = narrowed(asker, input.scope());
        UUID threadId = input.threadId();
        List<ChatMessage> history = List.of();
        if (threadId != null) {
            threads.visible(threadId, caller);
            history = history(threadId, providerId);
        } else {
            threadId = UUID.randomUUID();
        }
        return new Prepared(input, caller, settings, scope, threadId, history, actor, providerId);
    }

    /**
     * {@code ask}: answers a question.
     *
     * @param input the validated request
     * @param caller who asks
     * @param listener receives events as they happen (streaming), or {@link AskListener#NONE}
     * @return {@code AskResponse}
     */
    public Map<String, Object> ask(AskInput input, AskCaller caller, AskListener listener) {
        return answer(prepare(input, caller), listener);
    }

    /**
     * Answers a prepared question.
     *
     * @param prepared from {@link #prepare}
     * @param listener receives events as they happen
     * @return {@code AskResponse}
     */
    public Map<String, Object> answer(Prepared prepared, AskListener listener) {
        Instant started = clock.instant();
        AskCaller caller = prepared.caller();
        String providerId = prepared.providerId();
        LlmProviderConfig config = llm.config(providerId);
        EgressPolicy.LlmDestination destination = config.destination();
        McpCaller toolCaller = caller.toolCaller(egress.llmExcludedSources(destination));
        AskScope scope = prepared.scope();
        Predicate<String> allowed = source -> egress.allowsLlmContext(source, destination)
                && !toolCaller.excludedSources().contains(source)
                && toolCaller.restriction().allowsSource(source)
                && (scope.sources().isEmpty() || scope.sources().contains(source));
        Run run = new Run(prepared, toolCaller, allowed, scope.bboxFilter(), listener);
        // Prompt 056: get_map_image images go to providers that take images in tool results; others get the caption.
        run.imagesAccepted = config.type() == ai.foresignal.llm.LlmProviderType.ANTHROPIC;
        LlmClient client = llm.client(providerId, prepared.actor());

        boolean supportsTools = !Boolean.FALSE.equals(
                providers.find(providerId).map(LlmProviderEntity::getSupportsTools).orElse(null));
        Outcome outcome;
        if (supportsTools) {
            try {
                outcome = toolsMode(run, client, prepared.settings());
            } catch (LlmProviderException e) {
                if (e.kind() != LlmProviderException.Kind.TOOLS_UNSUPPORTED) {
                    throw e;
                }
                run.reset();
                outcome = retrievalMode(run, client, config, prepared.settings());
            }
        } else {
            outcome = retrievalMode(run, client, config, prepared.settings());
        }
        return finish(run, outcome, providerId, started);
    }

    // ------------------------------------------------------------------------- modes

    private Outcome toolsMode(Run run, LlmClient client, LlmSettings settings) {
        List<ToolSpec> specs = new ArrayList<>();
        for (ForesignalMcpTool tool : catalog.readTools(run.toolCaller)) {
            specs.add(new ToolSpec(tool.name(), tool.description(), catalog.schema(tool)));
        }
        specs.addAll(actionTools.specs());
        ChatMessage.User user = new ChatMessage.User(AskPrompts.userTurn(run.prepared.input().question(), null,
                run.prepared.scope(), clock.instant(), zone(run.prepared.actor()), json));
        List<ChatMessage> messages = new ArrayList<>(run.prepared.history());
        messages.add(user);
        ChatRequest request = new ChatRequest(AskPrompts.SYSTEM, messages, specs, null, Effort.MEDIUM, Purpose.ASK,
                run.listener.streaming(), run.deltaSink());
        ToolLoop.RichToolExecutor executor = call -> execute(run, call);
        ToolLoop loop = new ToolLoop(client, executor,
                new ToolLoop.Limits(MAX_ITERATIONS, DEADLINE, settings.askMaxTokens()), clock);
        ToolLoop.Result result = loop.run(request);
        List<ChatMessage> appended = result.conversation().subList(run.prepared.history().size() + 1,
                result.conversation().size());
        ChatResponse last = result.lastResponse();
        return new Outcome(TOOLS, user, List.copyOf(appended), last, result.cause(), result.usage());
    }

    private Outcome retrievalMode(Run run, LlmClient client, LlmProviderConfig config, LlmSettings settings) {
        AskScope scope = run.prepared.scope();
        run.listener.progress("search", "calling", "hybrid search within the question's scope");
        String question = run.prepared.input().question();
        String q = question.length() > MAX_SEARCH_QUERY ? question.substring(0, MAX_SEARCH_QUERY) : question;
        SearchRequest request = new SearchRequest(q, SearchRequest.Mode.HYBRID, Set.of(), scope.sources(), scope.kinds(),
                scope.bboxFilter(), scope.since(), scope.until(), false, RETRIEVAL_LIMIT);
        Map<String, Object> results = search.search(request, run.toolCaller.excludedSources(),
                run.toolCaller.restriction());
        ObjectNode data = json.createObjectNode();
        ArrayNode records = data.putArray("records");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) results.getOrDefault("items", List.of());
        for (Map<String, Object> item : items) {
            records.add(searchItem(item));
        }
        Instant now = clock.instant();
        Instant since = now.minus(RETRIEVAL_ALERTS);
        if (scope.since() != null && scope.since().isAfter(since)) {
            since = scope.since();
        }
        ArrayNode alertItems = data.putArray("alerts");
        List<AlertView> recent = alerts.list(new AlertQuery(null, scope.sources(), scope.kinds(), since, null, null,
                null, null, null, false, run.toolCaller.excludedSources(), run.toolCaller.restriction()), null,
                RETRIEVAL_ALERT_LIMIT);
        for (AlertView alert : recent) {
            alertItems.add(shaper.objectTree(alertMapper.alert(alert, null)));
        }
        if (Boolean.TRUE.equals(results.get("degraded"))) {
            data.put("notice", "Semantic search is unavailable; records were found by text match only.");
        }
        JsonNode shown = run.accept("retrieval", data);
        run.listener.progress("search", "done", records.size() + " records, " + alertItems.size() + " alerts");

        ChatMessage.User user = new ChatMessage.User(AskPrompts.userTurn(question, shown, scope, now, zone(run.prepared.actor()),
                json));
        List<ChatMessage> messages = new ArrayList<>(run.prepared.history());
        messages.add(user);
        int maxTokens = (int) Math.max(1, Math.min(config.effectiveMaxTokens(), settings.askMaxTokens()));
        ChatResponse response = client.chat(new ChatRequest(AskPrompts.RETRIEVAL_SYSTEM, messages, List.of(), maxTokens,
                Effort.MEDIUM, Purpose.ASK, run.listener.streaming(), run.deltaSink()));
        return new Outcome(RETRIEVAL, user, List.of(response.assistantMessage()), response, null, response.usage());
    }

    /** A search hit as the model sees it: identity, seq, title, snippet and the short record. */
    private ObjectNode searchItem(Map<String, Object> item) {
        ObjectNode out = json.createObjectNode();
        for (String key : List.of("docType", "docId", "seq", "title", "snippet", "source", "kind", "observedAt",
                "effectiveTime")) {
            Object value = item.get(key);
            if (value != null) {
                out.set(key, shaper.tree(value));
            }
        }
        Object record = item.get("record");
        if (record != null) {
            out.set("record", shaper.shortRecord(shaper.objectTree(record)));
        }
        shaper.searchHitNotice(item, out);
        return out;
    }

    // ------------------------------------------------------------------------- tools

    /**
     * Runs one tool call for the loop; a failure is thrown so the loop returns it as an error result. An image result
     * (prompt 056, {@code get_map_image}) keeps its caption in the data and passes its images when the provider takes
     * them.
     */
    private ToolLoop.ToolOutput execute(Run run, ToolCall call) throws ToolFailure {
        String name = call.name();
        run.listener.progress(name, "calling", argumentSummary(call.input()));
        run.toolCalls.incrementAndGet();
        try {
            JsonNode data;
            List<ai.foresignal.llm.client.ToolResult.Image> images = List.of();
            if (actionTools.isActionTool(name)) {
                ProposedAction proposal = actionTools.propose(name, call.input(), run.prepared.caller());
                run.proposals.add(proposal);
                ObjectNode out = json.createObjectNode();
                out.put("actionId", proposal.id().toString());
                out.put("status", AskActionTools.PENDING_CONFIRMATION);
                out.put("description", proposal.description());
                data = out;
            } else {
                ForesignalMcpTool tool = catalog.readTool(name, run.toolCaller).orElseThrow(() -> new ToolFailure(
                        "Unknown tool " + name + ". Use only the tools you were given."));
                ObjectNode args = call.input() instanceof ObjectNode object ? object : json.createObjectNode();
                ObjectNode scoped = ScopeInjector.apply(run.prepared.scope(), catalog.schema(tool), args);
                ToolResult result = catalog.invoke(tool, scoped, new ToolContext(run.toolCaller, null, TRANSPORT));
                String text = text(result);
                if (result.error()) {
                    throw new ToolFailure(text);
                }
                JsonNode raw = result.structured() != null ? result.structured() : StringNode.valueOf(text);
                List<ai.foresignal.llm.client.ToolResult.Image> found = images(result);
                if (!found.isEmpty() && raw instanceof ObjectNode object) {
                    ObjectNode withCaption = object.deepCopy();
                    withCaption.put("caption", text);
                    raw = withCaption;
                }
                data = run.accept(name, raw);
                if (run.imagesAccepted) {
                    images = found;
                }
            }
            run.listener.progress(name, "done", resultSummary(data));
            return new ToolLoop.ToolOutput(AskPrompts.wrap(name, data, json), images);
        } catch (ToolFailure e) {
            run.listener.progress(name, "done", "error");
            throw e;
        } catch (ForesignalException e) {
            run.listener.progress(name, "done", "error");
            throw new ToolFailure(McpToolRegistry.describe(e));
        } catch (McpToolException e) {
            run.listener.progress(name, "done", "error");
            throw new ToolFailure(e.getMessage());
        }
    }

    /** The image blocks of a tool result (prompt 056). */
    private static List<ai.foresignal.llm.client.ToolResult.Image> images(ToolResult result) {
        List<ai.foresignal.llm.client.ToolResult.Image> out = new ArrayList<>();
        for (ToolResult.Content block : result.content()) {
            if (block instanceof ToolResult.Image image) {
                out.add(new ai.foresignal.llm.client.ToolResult.Image(image.data(), image.mimeType()));
            }
        }
        return out;
    }

    private static String text(ToolResult result) {
        StringBuilder out = new StringBuilder();
        for (ToolResult.Content block : result.content()) {
            if (block instanceof ToolResult.Text text) {
                if (!out.isEmpty()) {
                    out.append('\n');
                }
                out.append(text.text());
            }
        }
        return out.toString();
    }

    private static String argumentSummary(JsonNode input) {
        if (input == null || !input.isObject() || input.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>(input.propertyNames());
        return "with " + String.join(", ", names);
    }

    private static String resultSummary(JsonNode data) {
        if (data == null) {
            return null;
        }
        JsonNode items = data.get("items");
        if (items != null && items.isArray()) {
            return items.size() + (items.size() == 1 ? " item" : " items");
        }
        if (data.has("actionId")) {
            return "proposed, " + AskActionTools.PENDING_CONFIRMATION;
        }
        return data.path("withheld").asBoolean(false) ? "withheld" : "ok";
    }

    // ------------------------------------------------------------------------- answer

    private Map<String, Object> finish(Run run, Outcome outcome, String providerId, Instant started) {
        Prepared prepared = run.prepared;
        AskCaller caller = prepared.caller();
        List<String> warnings = new ArrayList<>();
        ChatResponse last = outcome.last();
        String answer = last == null ? "" : last.content();
        String stopReason = last == null ? StopReason.ERROR.wireValue() : responseStopReason(last.stopReason());
        String refusalCategory = null;
        if (last != null && last.stopReason() == StopReason.REFUSAL) {
            refusalCategory = last.refusalCategory();
            answer = "The model declined to answer this question"
                    + (refusalCategory == null ? "." : " (category: " + refusalCategory + ").");
            warnings.add("the model refused to answer" + (refusalCategory == null ? "" : " (category " + refusalCategory
                    + ")"));
        } else if (last != null && last.stopReason() == StopReason.MAX_TOKENS) {
            warnings.add("the answer was cut off at the model's output limit");
        } else if (last != null && last.stopReason() == StopReason.ERROR) {
            warnings.add("the provider reported an error before the answer was complete");
        }
        if (outcome.cause() != null) {
            switch (outcome.cause()) {
                case MAX_ITERATIONS, DEADLINE, TOKEN_CAP -> {
                    warnings.add("the answer may be incomplete: stopped at the " + limitName(outcome.cause()));
                    if (outcome.cause() == ToolLoop.StopCause.TOKEN_CAP) {
                        stopReason = StopReason.MAX_TOKENS.wireValue();
                    } else {
                        stopReason = StopReason.END.wireValue();
                    }
                }
                default -> {
                    // END, REFUSAL, MAX_TOKENS and ERROR are handled through the last response.
                }
            }
        }
        if (answer.isBlank()) {
            answer = "No answer was produced.";
        }
        CitationVerifier.Verification verification = verifier.verify(answer, run.citable);
        warnings.addAll(verification.warnings());
        // Prompt 056: a non-ok weather status the answer does not state gets the standard caveat and a warning.
        List<CitationSet.WeatherIssue> unaddressed = ai.foresignal.weather.WeatherAnswerGuard.unaddressed(answer,
                run.citable.weatherIssues());
        String caveat = null;
        if (refusalCategory == null && !unaddressed.isEmpty()) {
            caveat = ai.foresignal.weather.WeatherAnswerGuard.caveat(unaddressed);
            answer = answer + "\n\n" + caveat;
            warnings.add(ai.foresignal.weather.WeatherAnswerGuard.warning(unaddressed));
        }
        if (!run.deltaSeen.get()) {
            run.listener.delta(answer);
        } else if (caveat != null) {
            run.listener.delta("\n\n" + caveat);
        }

        List<String> allWarnings = List.copyOf(new LinkedHashSet<>(warnings));
        List<Map<String, Object>> citations = new ArrayList<>();
        for (AskCitation citation : verification.citations()) {
            citations.add(citation.toMap());
        }

        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("provider", providerId);
        usage.put("model", last == null ? null : last.model());
        TokenUsage tokens = outcome.usage() == null ? TokenUsage.ZERO : outcome.usage();
        usage.put("inputTokens", tokens.inputTokens());
        usage.put("outputTokens", tokens.outputTokens());
        usage.put("toolCalls", run.toolCalls.get());
        usage.put("durationMs", Duration.between(started, clock.instant()).toMillis());

        Instant now = clock.instant();
        List<AskActionEntity> pending = new ArrayList<>();
        for (ProposedAction proposal : run.proposals) {
            pending.add(new AskActionEntity(proposal.id(), prepared.threadId(), caller.tokenId(), proposal.type(),
                    proposal.description(), asMap(proposal.payload()), now));
        }
        Map<String, Object> userContent = new LinkedHashMap<>();
        userContent.put("text", prepared.input().question());
        userContent.put("scope", prepared.scope().describe());
        userContent.put("turns", json.convertValue(turns.encodeAll(List.of(outcome.user()), providerId), List.class));
        Map<String, Object> assistantContent = new LinkedHashMap<>();
        assistantContent.put("text", answer);
        assistantContent.put("turns", json.convertValue(turns.encodeAll(outcome.appended(), providerId), List.class));

        AskThreadService.Stored stored = threads.store(new AskThreadService.Exchange(prepared.threadId(), caller,
                prepared.input().question(), userContent, assistantContent, citations, allWarnings, usage,
                outcome.mode(), stopReason, refusalCategory, pending));

        List<AskActionEntity> finalActions = new ArrayList<>(stored.actions());
        if (prepared.input().allowActions() && caller.mayAct()) {
            for (int i = 0; i < finalActions.size(); i++) {
                finalActions.set(i, actions.execute(finalActions.get(i), caller, "ask", "/v1/ask"));
            }
        }
        List<Map<String, Object>> actionViews = new ArrayList<>();
        for (AskActionEntity action : finalActions) {
            actionViews.add(AskViews.action(action));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("threadId", prepared.threadId().toString());
        response.put("answer", answer);
        response.put("citations", citations);
        response.put("proposedActions", actionViews);
        response.put("warnings", allWarnings);
        response.put("mode", outcome.mode());
        response.put("usage", usage);
        response.put("stopReason", stopReason);
        response.put("refusalCategory", refusalCategory);

        AskListener listener = run.listener;
        for (AskCitation citation : verification.citations()) {
            listener.citation(citation);
        }
        for (String warning : allWarnings) {
            listener.warning(warning);
        }
        for (Map<String, Object> action : actionViews) {
            listener.action(action);
        }
        listener.done(response);
        return response;
    }

    private static String limitName(ToolLoop.StopCause cause) {
        return switch (cause) {
            case MAX_ITERATIONS -> "limit of " + MAX_ITERATIONS + " tool iterations";
            case DEADLINE -> "time limit of " + DEADLINE.toSeconds() + " s";
            case TOKEN_CAP -> "token cap (askMaxTokens)";
            default -> cause.name().toLowerCase(Locale.ROOT);
        };
    }

    private static String responseStopReason(StopReason reason) {
        return switch (reason) {
            case END, TOOL_USE -> StopReason.END.wireValue();
            case MAX_TOKENS -> StopReason.MAX_TOKENS.wireValue();
            case REFUSAL -> StopReason.REFUSAL.wireValue();
            case ERROR -> StopReason.ERROR.wireValue();
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(JsonNode node) {
        return node != null && node.isObject() ? json.convertValue(node, Map.class) : Map.of();
    }

    // ------------------------------------------------------------------------- helpers

    /**
     * {@code caller} with its restriction narrowed by the scope's places and view (prompt 036), so the
     * tool loop and retrieval read inside them.
     */
    private AskCaller narrowed(AskCaller caller, AskScope scope) {
        if (scope.placeIds().isEmpty() && scope.viewId() == null) {
            return caller;
        }
        AskScopeResolver resolver = scopeResolvers.getIfAvailable();
        if (resolver == null) {
            return caller;
        }
        ai.foresignal.security.restrict.ReadRestriction narrowed = resolver.restriction(scope, caller.restriction());
        if (narrowed == caller.restriction()) {
            return caller;
        }
        ai.foresignal.mcp.McpCaller mcp = caller.mcp();
        ai.foresignal.api.support.ReadCaller read = mcp.readCaller();
        return new AskCaller(new ai.foresignal.mcp.McpCaller(mcp.token(), new ai.foresignal.api.support.ReadCaller(
                read.scope(), read.exposeObserver(), read.openReads(), narrowed), mcp.excludedSources(), mcp.remoteIp()));
    }

    private AskScope resolveScope(AskScope scope) {
        if (scope.placeIds().isEmpty() && scope.viewId() == null) {
            return scope;
        }
        AskScopeResolver resolver = scopeResolvers.getIfAvailable();
        if (resolver == null) {
            List<FieldError> errors = new ArrayList<>();
            if (!scope.placeIds().isEmpty()) {
                errors.add(new FieldError("scope.placeIds", "places are not available yet (prompt 036)"));
            }
            if (scope.viewId() != null) {
                errors.add(new FieldError("scope.viewId", "saved views are not available yet (prompt 036)"));
            }
            throw new ValidationException("The question's scope is not valid.", errors);
        }
        return resolver.resolve(scope);
    }

    /** Every stored turn of a thread, in order, for continuing it with {@code providerId}. */
    List<ChatMessage> history(UUID threadId, String providerId) {
        List<ChatMessage> out = new ArrayList<>();
        for (AskMessageEntity message : threads.messages(threadId)) {
            Object stored = message.getContent() == null ? null : message.getContent().get("turns");
            if (stored != null) {
                out.addAll(turns.decodeAll(json.valueToTree(stored), providerId));
            }
        }
        return out;
    }

    /**
     * The zone the question's local times are written in: the asking user's own timezone (prompt R2-012), else the
     * server's, else UTC. Only the rendering changes; every stored and compared time stays UTC.
     */
    private ZoneId zone(String actor) {
        return zones.zoneFor(actor);
    }

    // ------------------------------------------------------------------------- types

    /**
     * A question checked and ready to answer.
     *
     * @param input the request
     * @param caller who asks
     * @param settings the LLM settings
     * @param scope the resolved scope
     * @param threadId the thread (new or existing)
     * @param history the stored turns of an existing thread
     * @param actor the asking user's principal, or null for the machine (R2-011)
     * @param providerId the chat provider that answers: the user's personal one, or the shared chat role (R2-011)
     */
    public record Prepared(AskInput input, AskCaller caller, LlmSettings settings, AskScope scope, UUID threadId,
            List<ChatMessage> history, String actor, String providerId) {

        /** Copies the history. */
        public Prepared {
            history = List.copyOf(history);
        }
    }

    /** What one mode produced. */
    private record Outcome(String mode, ChatMessage.User user, List<ChatMessage> appended, ChatResponse last,
            ToolLoop.StopCause cause, TokenUsage usage) {}

    /** A failed tool call; its message is returned to the model as an error result. */
    static final class ToolFailure extends Exception {

        private static final long serialVersionUID = 1L;

        ToolFailure(String message) {
            super(message);
        }
    }

    /** The state of one answer: what the tools returned, what was proposed, the listener. */
    private final class Run {

        final Prepared prepared;
        final McpCaller toolCaller;
        final Predicate<String> allowed;
        final BboxFilter area;
        final AskListener listener;
        CitationSet citable = new CitationSet();
        volatile boolean imagesAccepted;
        final List<ProposedAction> proposals = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger toolCalls = new AtomicInteger();
        final AtomicBoolean deltaSeen = new AtomicBoolean();

        Run(Prepared prepared, McpCaller toolCaller, Predicate<String> allowed, BboxFilter area, AskListener listener) {
            this.prepared = prepared;
            this.toolCaller = toolCaller;
            this.allowed = allowed;
            this.area = area;
            this.listener = listener;
        }

        /** Forgets a failed tools-mode attempt before falling back to retrieval. */
        void reset() {
            citable = new CitationSet();
            proposals.clear();
        }

        /** The delta consumer of a chat request: forwards provider text, remembering that some arrived. */
        Consumer<String> deltaSink() {
            if (!listener.streaming()) {
                return null;
            }
            return text -> {
                if (text != null && !text.isEmpty()) {
                    deltaSeen.set(true);
                    listener.delta(text);
                }
            };
        }

        /** Filters a result for the model and records what it makes citable; returns what the model sees. */
        JsonNode accept(String tool, JsonNode raw) {
            JsonNode shown = outputFilter.filter(raw, allowed, area).output();
            for (CitableExtractor extractor : extractors) {
                extractor.extract(tool, shown, citable);
            }
            citable.addText(shown == null ? null : (shown.isString() ? shown.asString() : shown.toString()));
            return shown;
        }
    }
}
