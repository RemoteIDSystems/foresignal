package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystRunner;
import ai.foresignal.ask.AskPrompts;
import ai.foresignal.ask.CitableExtractor;
import ai.foresignal.ask.ToolOutputFilter;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.llm.client.TokenUsage;
import ai.foresignal.llm.client.ToolCall;
import ai.foresignal.llm.client.ToolLoop;
import ai.foresignal.llm.client.ToolSpec;
import ai.foresignal.manifest.CanonicalJson;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolCatalog;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPolicy;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;

/**
 * The LLM machinery the cycle roles share (prompt 128 §3, Architecture §19), built on the 028 {@link ToolLoop} and the
 * 102 runner's rules: the read catalog ({@link AnalystRunner#readToolNames()}) called as a read-only caller with the
 * provider destination's excluded sources, every tool result passed through the {@link ToolOutputFilter} and
 * delimited as untrusted data, a stable system prompt (the versioned template plus the profile's instructions) and a
 * deterministic tool order so the provider can cache both, the profile's limits capped by the run's remaining
 * {@link TokenBudget}, and {@code stop_reason} checks: a refusal skips the role (the deterministic assessment stands),
 * a truncated or limited loop leaves a warning.
 */
@Component
public class CycleLlm {

    private static final Logger log = LoggerFactory.getLogger(CycleLlm.class);

    /** The transport tag of cycle tool calls. */
    static final String TRANSPORT = "cycle";

    /** The output cap of a regeneration call. */
    static final int REGENERATE_MAX_TOKENS = 2_000;

    private final LlmService llm;
    private final LlmSettingsService llmSettings;
    private final ToolCatalog catalog;
    private final EgressPolicy egress;
    private final ToolOutputFilter outputFilter;
    private final List<CitableExtractor> extractors;
    private final IntelVisibility visibility;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the component. */
    public CycleLlm(LlmService llm, LlmSettingsService llmSettings, ToolCatalog catalog, EgressPolicy egress,
            ToolOutputFilter outputFilter, List<CitableExtractor> extractors, IntelVisibility visibility,
            JsonMapper json, Clock clock) {
        this.llm = llm;
        this.llmSettings = llmSettings;
        this.catalog = catalog;
        this.egress = egress;
        this.outputFilter = outputFilter;
        this.extractors = List.copyOf(extractors);
        this.visibility = visibility;
        this.json = json;
        this.clock = clock;
    }

    /** The profile's provider, else the assigned chat provider, else null. */
    public String providerFor(AnalystProfileEntity profile) {
        if (profile != null && profile.getProviderId() != null) {
            return profile.getProviderId();
        }
        try {
            return llmSettings.get().chatProvider();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** True when {@code providerId} names a usable provider. */
    public boolean usable(String providerId) {
        if (providerId == null) {
            return false;
        }
        try {
            return llm.config(providerId) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** A citation set over the packet's references. */
    public CycleCitations citations(Collection<String> packetRefs) {
        return new CycleCitations(extractors, packetRefs);
    }

    /**
     * A role context for {@code providerId}, or empty when the provider is not usable.
     *
     * @param providerId the provider
     * @param effort the effort wire value
     * @param profile the profile (limits, instructions)
     * @param budget the run's budget
     * @param citations the run's citations
     * @param privateSeen the run's private flag
     * @param runId the run
     */
    public Optional<RoleContext> context(String providerId, String effort, AnalystProfileEntity profile,
            TokenBudget budget, CycleCitations citations, AtomicBoolean privateSeen, String runId) {
        if (!usable(providerId)) {
            return Optional.empty();
        }
        EgressPolicy.LlmDestination destination = llm.config(providerId).destination();
        Set<String> excluded = egress.llmExcludedSources(destination);
        boolean privateAllowed = egress.privateAllowedFor(destination);
        McpCaller caller = AnalystRunner.toolCaller(excluded, privateAllowed);
        LlmClient client = llm.client(providerId);
        return Optional.of(new RoleContext(providerId, client, destination, excluded, privateAllowed, caller, effort,
                profile, budget, citations, privateSeen, runId));
    }

    /** A context like {@code base} for another provider, or empty when that provider is not usable. */
    public Optional<RoleContext> forProvider(RoleContext base, String providerId) {
        return context(providerId, base.effort(), base.profile(), base.budget(), base.citations(), base.privateSeen(),
                base.runId());
    }

    /** The system prompt of {@code templateId}: the template plus the profile's operator instructions. */
    public String systemPrompt(String templateId, AnalystProfileEntity profile) {
        String text = PromptTemplates.text(templateId, PromptTemplates.V1);
        return profile == null || profile.getInstructions() == null ? text
                : text + "\n\nOperator instructions: " + profile.getInstructions();
    }

    /** The read tools the roles get, in catalog order. */
    List<ToolSpec> readSpecs(McpCaller caller) {
        List<ToolSpec> specs = new ArrayList<>();
        for (ForesignalMcpTool tool : catalog.readTools(caller)) {
            if (AnalystRunner.readToolNames().contains(tool.name())) {
                specs.add(new ToolSpec(tool.name(), tool.description(), catalog.schema(tool)));
            }
        }
        return specs;
    }

    /** The SHA-256 of a role's system prompt and tool names (recorded with forecasts and in manifests). */
    public String promptHash(String templateId, AnalystProfileEntity profile, List<ToolSpec> specs) {
        StringBuilder text = new StringBuilder(systemPrompt(templateId, profile));
        for (ToolSpec spec : specs) {
            text.append('|').append(spec.name());
        }
        return CanonicalJson.sha256(text.toString());
    }

    /**
     * Runs one tool loop of a role.
     *
     * @param ctx the role context
     * @param outcome the role's outcome (tokens, model, warnings)
     * @param templateId the system prompt template
     * @param messages the conversation so far (the new user turn last)
     * @param outputSpecs the role's output tools
     * @param capture receives every output tool call
     * @return the loop result
     * @throws RoleSkippedException when the run's token budget is spent or the model refused
     */
    public ToolLoop.Result loop(RoleContext ctx, RoleOutcome outcome, String templateId, List<ChatMessage> messages,
            List<ToolSpec> outputSpecs, Consumer<ToolCall> capture) {
        if (ctx.budget().exhausted()) {
            throw new RoleSkippedException("max_tokens_per_cycle (" + ctx.budget().limit() + ") reached");
        }
        List<ToolSpec> specs = new ArrayList<>(readSpecs(ctx.caller()));
        specs.addAll(outputSpecs);
        Predicate<String> allowed = source -> {
            boolean ok = egress.allowsLlmContext(source, ctx.destination()) && !ctx.excluded().contains(source);
            if (ok && visibility.isPrivateSource(source)) {
                ctx.privateSeen().set(true);
            }
            return ok;
        };
        ToolLoop.ToolExecutor executor = call -> {
            if (CycleOutputTools.isOutputTool(call.name())) {
                boolean known = outputSpecs.stream().anyMatch(s -> s.name().equals(call.name()));
                if (!known) {
                    return AskPrompts.wrap(call.name(), StringNode.valueOf("not available in this step"), json);
                }
                synchronized (outcome) {
                    capture.accept(call);
                }
                return AskPrompts.wrap(call.name(), StringNode.valueOf("recorded"), json);
            }
            return read(call, ctx, allowed);
        };
        AnalystProfileEntity profile = ctx.profile();
        long cap = Math.max(1, Math.min(profile.getMaxTokensPerRun(), ctx.budget().remaining()));
        ToolLoop.Limits limits = new ToolLoop.Limits(Math.max(1, profile.getMaxToolCalls()),
                Duration.ofSeconds(Math.max(10, profile.getMaxWallSeconds())), cap);
        ChatRequest request = new ChatRequest(systemPrompt(templateId, profile), messages, specs, null,
                effort(ctx.effort()), Purpose.ANALYST, false, null);
        ToolLoop.Result result = new ToolLoop(ctx.client(), executor, limits, clock).run(request);
        TokenUsage usage = result.usage() == null ? TokenUsage.ZERO : result.usage();
        outcome.tokens(usage.inputTokens(), usage.outputTokens(), usage.cachedTokens());
        ctx.budget().spend(usage.total());
        ChatResponse last = result.lastResponse();
        if (last != null) {
            outcome.model(last.model());
        }
        if (last != null && last.stopReason() == StopReason.REFUSAL) {
            throw new RoleSkippedException("model refused" + (last.refusalCategory() == null ? ""
                    : ": " + last.refusalCategory()));
        }
        switch (result.cause()) {
            case MAX_TOKENS -> outcome.warn("the model hit its output cap; the output may be incomplete");
            case MAX_ITERATIONS, DEADLINE, TOKEN_CAP -> outcome.warn("the tool loop stopped at a limit ("
                    + result.cause().name().toLowerCase(java.util.Locale.ROOT) + ")");
            case ERROR -> outcome.warn("the provider reported an error stop reason");
            default -> { }
        }
        return result;
    }

    /**
     * One plain completion without tools (the regeneration of failing text), within the budget; null when the budget
     * is spent, the model refused or the call failed.
     */
    public String complete(RoleContext ctx, RoleOutcome outcome, String templateId, String userText) {
        if (ctx.budget().exhausted()) {
            return null;
        }
        try {
            ChatRequest request = new ChatRequest(systemPrompt(templateId, ctx.profile()),
                    List.of(new ChatMessage.User(userText)), List.of(), REGENERATE_MAX_TOKENS, effort(ctx.effort()),
                    Purpose.ANALYST, false, null);
            ChatResponse response = ctx.client().chat(request);
            TokenUsage usage = response.usage();
            outcome.tokens(usage.inputTokens(), usage.outputTokens(), usage.cachedTokens());
            ctx.budget().spend(usage.total());
            if (response.stopReason() == StopReason.REFUSAL) {
                return null;
            }
            return response.content();
        } catch (RuntimeException e) {
            log.debug("Regeneration failed: {}", e.toString());
            return null;
        }
    }

    /** Wraps untrusted input for a user turn. */
    public String wrap(String name, Object data) {
        JsonNode node = json.valueToTree(data);
        return AskPrompts.wrap(name, node, json);
    }

    private static Effort effort(String wire) {
        try {
            return wire == null ? null : Effort.fromWire(wire);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Runs one read tool for the model; the result is egress-filtered, recorded as citable and delimited. */
    private String read(ToolCall call, RoleContext ctx, Predicate<String> allowed) {
        Optional<ForesignalMcpTool> tool = catalog.readTool(call.name(), ctx.caller());
        if (tool.isEmpty() || !AnalystRunner.readToolNames().contains(call.name())) {
            return AskPrompts.wrap(call.name(), StringNode.valueOf("unknown tool " + call.name()
                    + "; use only the tools you were given"), json);
        }
        ToolResult result = catalog.invoke(tool.get(), call.input(), new ToolContext(ctx.caller(), null, TRANSPORT));
        JsonNode raw = result.structured() != null ? result.structured() : StringNode.valueOf(text(result));
        JsonNode shown = outputFilter.filter(raw, allowed, null).output();
        ctx.citations().accept(call.name(), shown);
        return AskPrompts.wrap(call.name(), shown, json);
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
}
