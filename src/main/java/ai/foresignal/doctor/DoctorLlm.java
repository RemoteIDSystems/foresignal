package ai.foresignal.doctor;

import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.security.EgressPolicy;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The doctor's optional LLM step (prompt 140, task 5): the 026 {@code chat} role, purpose {@code doctor} (budgeted and
 * metered by the governed client, logged with purpose {@code doctor}), low effort. Works with local providers (Ollama,
 * OpenAI-compatible) as well as Anthropic.
 *
 * <p>Prompt-injection containment (NFR-14): upstream-derived content goes inside {@link DoctorPrompt}'s untrusted
 * block with its delimiters stripped from the content; the system prompt says the block may contain instructions to
 * ignore; no tools are offered; the answer is parsed as data by {@link DoctorLlmOutput}, keeping only the known members.
 *
 * <p>A proposed patch is validated in memory only: it must target a file of {@link AdapterSourceCatalog} and apply
 * cleanly ({@link UnifiedDiffApplier}); the patched text gets {@link PatchSanity} checks. It is never written to disk,
 * compiled or loaded, and is labeled {@value #PATCH_LABEL} everywhere.
 *
 * <p>Failure, timeout ({@value #TIMEOUT_SECONDS} s), budget refusal or no provider leave the deterministic report
 * intact, with {@code llm.status} saying why (G11).
 */
@Component
public class DoctorLlm {

    private static final Logger log = LoggerFactory.getLogger(DoctorLlm.class);

    /** The label of every proposed patch. */
    public static final String PATCH_LABEL = "proposed, not applied";

    /** The call's timeout. */
    public static final int TIMEOUT_SECONDS = 60;

    /** The output cap in compact mode. */
    static final int COMPACT_MAX_TOKENS = 1_500;

    /** The output cap in full mode. */
    static final int FULL_MAX_TOKENS = 4_000;

    /** {@code llm.status}: an answer was parsed. */
    public static final String OK = "ok";

    /** {@code llm.status}: the LLM step was off. */
    public static final String OFF = "off";

    /** {@code llm.status}: no chat provider is assigned. */
    public static final String NO_PROVIDER = "no-provider";

    /** {@code llm.status}: the provider's budget refused the call. */
    public static final String BUDGET_REFUSED = "budget-refused";

    /** {@code llm.status}: the call took longer than the timeout. */
    public static final String TIMEOUT = "timeout";

    /** {@code llm.status}: the call failed, the model refused, or the answer held no usable JSON. */
    public static final String FAILED = "failed";

    private final ObjectProvider<LlmService> llm;
    private final ObjectProvider<EgressPolicy> egress;
    private final AdapterSourceCatalog sources;
    private final JsonMapper json;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile Duration timeout = Duration.ofSeconds(TIMEOUT_SECONDS);

    /** Creates the step. */
    public DoctorLlm(ObjectProvider<LlmService> llm, ObjectProvider<EgressPolicy> egress, AdapterSourceCatalog sources,
            JsonMapper json) {
        this.llm = llm;
        this.egress = egress;
        this.sources = sources;
        this.json = json;
    }

    /** Overrides the timeout (tests). */
    void timeout(Duration value) {
        this.timeout = value;
    }

    /** Stops the call executor. */
    @PreDestroy
    void close() {
        executor.shutdownNow();
    }

    /** True when a chat provider is assigned. */
    public boolean available() {
        LlmService service = llm.getIfAvailable();
        return service != null && service.chatConfigured();
    }

    /** The {@code llm} block of a report whose LLM step did not run. */
    public static Map<String, Object> skipped(String status) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", status);
        return out;
    }

    /**
     * Asks the model and returns the report's {@code llm} block.
     *
     * @param sourceId the source (for the egress check)
     * @param descriptor the descriptor summary
     * @param findings the deterministic findings
     * @param diff the structure diff, or null
     * @param probeBody the probe body held in memory, or null
     * @param full {@code detail=full}
     */
    public Map<String, Object> explain(String sourceId, Map<String, Object> descriptor, List<DoctorFinding> findings,
            StructureDiff diff, String probeBody, boolean full) {
        LlmService service = llm.getIfAvailable();
        if (service == null || !service.chatConfigured()) {
            return skipped(NO_PROVIDER);
        }
        LlmClient client = service.chatClient().orElse(null);
        if (client == null) {
            return skipped(NO_PROVIDER);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", client.providerId());
        boolean upstreamAllowed = true;
        try {
            EgressPolicy policy = egress.getIfAvailable();
            if (policy != null) {
                Set<String> excluded = policy.llmExcludedSources(service.config(client.providerId()).destination());
                upstreamAllowed = !excluded.contains(sourceId);
            }
        } catch (RuntimeException e) {
            log.debug("The egress check for the doctor of {} failed; withholding upstream content", sourceId, e);
            upstreamAllowed = false;
        }
        String system = PromptTemplates.text(PromptTemplates.DOCTOR, PromptTemplates.V1);
        List<AdapterSourceCatalog.SourceFile> files = sources.files(sourceId);
        String user = DoctorPrompt.userMessage(new DoctorPrompt.Inputs(descriptor, findings, diff, probeBody, files,
                upstreamAllowed, full), system.length(), json);
        out.put("contextTokens", DoctorPrompt.estimateTokens(system) + DoctorPrompt.estimateTokens(user));
        out.put("detail", full ? DoctorOptions.FULL : DoctorOptions.COMPACT);
        ChatRequest request = new ChatRequest(system, List.of(new ChatMessage.User(user)), List.of(),
                full ? FULL_MAX_TOKENS : COMPACT_MAX_TOKENS, Effort.LOW, Purpose.DOCTOR, false, null);
        ChatResponse response;
        Future<ChatResponse> call = executor.submit(() -> client.chat(request));
        try {
            response = call.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            call.cancel(true);
            out.put("status", TIMEOUT);
            out.put("error", "no answer within " + timeout.toSeconds() + " s");
            return out;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            call.cancel(true);
            out.put("status", FAILED);
            out.put("error", "interrupted");
            return out;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof LlmBudgetExceededException) {
                out.put("status", BUDGET_REFUSED);
                out.put("error", StructureExtractor.cap(cause.getMessage(), 300));
            } else {
                out.put("status", FAILED);
                out.put("error", StructureExtractor.cap(cause.getClass().getSimpleName() + ": " + cause.getMessage(),
                        300));
            }
            log.info("The doctor's LLM call for {} ended with {}: {}", sourceId, out.get("status"), cause.toString());
            return out;
        }
        out.put("model", response.model());
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("input", response.usage().inputTokens());
        tokens.put("output", response.usage().outputTokens());
        out.put("tokens", tokens);
        if (response.stopReason() == StopReason.REFUSAL) {
            out.put("status", FAILED);
            out.put("error", "the model refused");
            return out;
        }
        DoctorLlmOutput parsed = DoctorLlmOutput.parse(response.content(), json);
        if (parsed == null) {
            out.put("status", FAILED);
            out.put("error", "the answer held no JSON object");
            return out;
        }
        out.put("status", OK);
        out.putAll(parsed.asMap());
        out.put("patch", parsed.patchFile() == null ? null : validatePatch(parsed.patchFile(), parsed.patchDiff()));
        return out;
    }

    /**
     * The report form of a proposed patch: the file, the diff, whether it applies cleanly in memory, the sanity checks
     * of the patched text and the label. Nothing is written, compiled or loaded.
     */
    Map<String, Object> validatePatch(String file, String diff) {
        Map<String, Object> patch = new LinkedHashMap<>();
        String normalized = AdapterSourceCatalog.normalize(file);
        patch.put("file", normalized == null ? file : normalized);
        patch.put("unifiedDiff", diff);
        patch.put("label", PATCH_LABEL);
        if (normalized == null || !sources.contains(normalized)) {
            patch.put("patchApplies", false);
            patch.put("error", "the file is not in the adapter source catalog");
            return patch;
        }
        String original = sources.read(normalized).orElse(null);
        if (original == null) {
            patch.put("patchApplies", false);
            patch.put("error", "the file could not be read from the adapter source catalog");
            return patch;
        }
        UnifiedDiffApplier.Result result = UnifiedDiffApplier.apply(original, diff);
        patch.put("patchApplies", result.applies());
        if (!result.applies()) {
            patch.put("error", result.error());
            return patch;
        }
        PatchSanity.Checks checks = PatchSanity.check(original, result.patched());
        Map<String, Object> sanity = new LinkedHashMap<>();
        sanity.put("bracesBalanced", checks.bracesBalanced());
        sanity.put("classNameUnchanged", checks.classNameUnchanged());
        patch.put("sanityChecks", sanity);
        return patch;
    }
}
