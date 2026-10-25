package ai.foresignal.api;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.LlmMapper;
import ai.foresignal.collect.UsageResolution;
import ai.foresignal.llm.LlmBudgetGovernor;
import ai.foresignal.llm.LlmProviderBalances;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.LlmUsageMeter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code LLM} operations (prompt 026): {@code listLlmProviders}, {@code createLlmProvider},
 * {@code getLlmProvider}, {@code updateLlmProvider}, {@code deleteLlmProvider},
 * {@code testLlmProvider}, {@code listLlmProviderModels}, {@code getLlmSettings},
 * {@code updateLlmSettings}, {@code getLlmUsage}, and (API 1.5.0) {@code probeLlmProvider} and
 * {@code getLlmProviderBudget}. Every operation needs {@code admin} ({@code /v1/llm/**} in
 * {@code AccessPolicy}); the test and probe endpoints call the provider and are always authenticated.
 * API keys are never returned.
 */
@RestController
@Tag(name = "LLM")
@RequestMapping("/v1/llm")
public class LlmController {

    /** Range {@code getLlmUsage} covers when {@code from} is omitted. */
    static final Duration DEFAULT_USAGE_RANGE = Duration.ofHours(24);

    /** Longest range served at minute resolution. */
    static final Duration MAX_MINUTE_RANGE = Duration.ofDays(31);

    private final LlmProviderService providers;
    private final LlmSettingsService settings;
    private final LlmService llm;
    private final LlmUsageMeter usage;
    private final LlmMapper mapper;
    private final LlmBudgetGovernor governor;
    private final LlmProviderBalances balances;
    private final Clock clock;

    /** Creates the controller. */
    public LlmController(LlmProviderService providers, LlmSettingsService settings, LlmService llm,
            LlmUsageMeter usage, LlmMapper mapper, LlmBudgetGovernor governor, LlmProviderBalances balances,
            Clock clock) {
        this.providers = providers;
        this.settings = settings;
        this.llm = llm;
        this.usage = usage;
        this.mapper = mapper;
        this.governor = governor;
        this.balances = balances;
        this.clock = clock;
    }

    /** {@code listLlmProviders}: by id. */
    @GetMapping("/providers")
    @Operation(operationId = "listLlmProviders", tags = "LLM")
    public List<Map<String, Object>> listLlmProviders() {
        return providers.list().stream().map(mapper::provider).toList();
    }

    /** {@code createLlmProvider}: 201 with {@code Location}. */
    @PostMapping("/providers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createLlmProvider", tags = "LLM")
    public ResponseEntity<Map<String, Object>> createLlmProvider(@RequestBody JsonNode body) {
        LlmProviderEntity created = providers.create(body);
        llm.forget(created.getId());
        return ResponseEntity.created(URI.create("/v1/llm/providers/" + created.getId()))
                .body(mapper.provider(created));
    }

    /** {@code getLlmProvider}. */
    @GetMapping("/providers/{providerId}")
    @Operation(operationId = "getLlmProvider", tags = "LLM")
    public Map<String, Object> getLlmProvider(@PathVariable String providerId) {
        return mapper.provider(providers.get(providerId));
    }

    /** {@code updateLlmProvider}: a full replace; an omitted {@code apiKey} keeps the stored key. */
    @PutMapping("/providers/{providerId}")
    @Operation(operationId = "updateLlmProvider", tags = "LLM")
    public Map<String, Object> updateLlmProvider(@PathVariable String providerId, @RequestBody JsonNode body) {
        LlmProviderEntity replaced = providers.replace(providerId, body);
        balances.forget(providerId);
        return mapper.provider(replaced);
    }

    /** {@code deleteLlmProvider}: 204; 409 while it holds a role. */
    @DeleteMapping("/providers/{providerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteLlmProvider", tags = "LLM")
    public ResponseEntity<Void> deleteLlmProvider(@PathVariable String providerId) {
        providers.delete(providerId);
        llm.forget(providerId);
        balances.forget(providerId);
        return ResponseEntity.noContent().build();
    }

    /** {@code testLlmProvider}: a tiny chat and, when configured, an embedding; consumes budget. */
    @PostMapping("/providers/{providerId}/test")
    @Operation(operationId = "testLlmProvider", tags = "LLM")
    public Map<String, Object> testLlmProvider(@PathVariable String providerId) {
        providers.get(providerId);
        return llm.test(providerId);
    }

    /** {@code listLlmProviderModels}: 502 when the provider fails. */
    @GetMapping("/providers/{providerId}/models")
    @Operation(operationId = "listLlmProviderModels", tags = "LLM")
    public List<Map<String, Object>> listLlmProviderModels(@PathVariable String providerId) {
        providers.get(providerId);
        return llm.models(providerId).stream().map(LlmMapper::model).toList();
    }

    /**
     * {@code probeLlmProvider}: tests an unsaved {@code LlmProviderInput} and lists its models; nothing is
     * stored. Without {@code apiKey}, a saved provider's stored key is used when {@code id} names one.
     */
    @PostMapping("/probe")
    @Operation(operationId = "probeLlmProvider", tags = "LLM")
    public Map<String, Object> probeLlmProvider(@RequestBody JsonNode body) {
        return LlmMapper.probe(llm.probe(providers.probeConfig(body)));
    }

    /** {@code getLlmProviderBudget}: spend against the USD caps per window, and any provider-reported balance. */
    @GetMapping("/providers/{providerId}/budget")
    @Operation(operationId = "getLlmProviderBudget", tags = "LLM")
    public Map<String, Object> getLlmProviderBudget(@PathVariable String providerId) {
        LlmProviderEntity provider = providers.get(providerId);
        return mapper.budget(provider, governor.spend(providerId), balances.balance(providers.config(provider)));
    }

    /** {@code getLlmSettings}. */
    @GetMapping("/settings")
    @Operation(operationId = "getLlmSettings", tags = "LLM")
    public Map<String, Object> getLlmSettings() {
        return LlmMapper.settings(settings.get());
    }

    /** {@code updateLlmSettings}: replaces the settings; omitted members take their defaults. */
    @PutMapping("/settings")
    @Operation(operationId = "updateLlmSettings", tags = "LLM")
    public Map<String, Object> updateLlmSettings(@RequestBody JsonNode body) {
        return LlmMapper.settings(settings.update(LlmSettings.parse(body)));
    }

    /**
     * {@code getLlmUsage}: per-bucket counters over {@code [from, to)} for one provider or all, with
     * totals. Defaults: the 24 hours up to the end of the current minute, at hour resolution.
     */
    @GetMapping("/usage")
    @Operation(operationId = "getLlmUsage", tags = "LLM")
    public Map<String, Object> getLlmUsage(
            @RequestParam(name = "provider", required = false) String provider,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to,
            @RequestParam(name = "resolution", required = false) String resolution) {
        if (provider != null) {
            providers.get(provider);
        }
        // The default end includes the minute in progress, whose counters are not flushed yet.
        Instant end = to == null ? clock.instant().truncatedTo(ChronoUnit.MINUTES).plus(Duration.ofMinutes(1))
                : instant("to", to);
        Instant start = from == null ? end.minus(DEFAULT_USAGE_RANGE) : instant("from", from);
        if (!start.isBefore(end)) {
            throw ValidationException.forField("from", "must be before to");
        }
        UsageResolution res;
        try {
            res = resolution == null || resolution.isBlank() ? UsageResolution.HOUR
                    : UsageResolution.fromWireValue(resolution);
        } catch (InvalidInputException e) {
            throw ValidationException.forField("resolution", "must be minute, hour or day");
        }
        if (res == UsageResolution.MINUTE && Duration.between(start, end).compareTo(MAX_MINUTE_RANGE) > 0) {
            throw ValidationException.forField("resolution",
                    "minute resolution serves at most 31 days; use hour or day for longer ranges");
        }
        return LlmMapper.usage(provider, res.wireValue(), start, usage.series(provider, start, end, res));
    }

    private static Instant instant(String field, String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an ISO-8601 instant, for example 2026-10-11T00:00:00Z");
        }
    }
}
