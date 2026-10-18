package ai.foresignal.llm;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * The {@code llm_provider} rows behind {@code /v1/llm/providers} (prompt 026).
 *
 * <p>The API key is encrypted with {@link CredentialCipher} and never returned. A replace that omits
 * {@code apiKey} keeps the stored key. A provider assigned to a role cannot be deleted
 * ({@code 409 LLM_PROVIDER_IN_USE}) and cannot be changed so that it no longer serves that role.
 * Changing the model or dimensions of the active embedding provider publishes
 * {@link EmbeddingModelChangedEvent} once the change commits.
 */
@Service
public class LlmProviderService {

    private final LlmProviderRepository repository;
    private final LlmProviderValidator validator;
    private final LlmSettingsService settings;
    private final CredentialCipher cipher;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /** Creates the service. */
    public LlmProviderService(LlmProviderRepository repository, LlmProviderValidator validator,
            LlmSettingsService settings, CredentialCipher cipher, ApplicationEventPublisher events, Clock clock) {
        this.repository = repository;
        this.validator = validator;
        this.settings = settings;
        this.cipher = cipher;
        this.events = events;
        this.clock = clock;
    }

    /** Every provider, by id. */
    @Transactional(readOnly = true)
    public List<LlmProviderEntity> list() {
        return repository.findAllByOrderByIdAsc();
    }

    /**
     * One provider.
     *
     * @throws NotFoundException when it does not exist
     */
    @Transactional(readOnly = true)
    public LlmProviderEntity get(String id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("LLM provider", id));
    }

    /** One provider, when it exists. */
    @Transactional(readOnly = true)
    public Optional<LlmProviderEntity> find(String id) {
        return repository.findById(id);
    }

    /**
     * Creates a provider from an {@code LlmProviderInput} body.
     *
     * @throws ValidationException when the body is not valid
     * @throws ConflictException ({@code LLM_PROVIDER_EXISTS}) when the id is taken
     */
    @Transactional
    public LlmProviderEntity create(JsonNode body) {
        return create(body, true);
    }

    private LlmProviderEntity create(JsonNode body, boolean keyRequired) {
        LlmProviderInput input = validator.parse(body, null);
        if (repository.existsById(input.id())) {
            throw new ConflictException(ErrorCodes.LLM_PROVIDER_EXISTS,
                    "An LLM provider with id '" + input.id() + "' already exists.");
        }
        if (keyRequired) {
            requireKey(input, null);
        }
        LlmProviderEntity entity = new LlmProviderEntity(input.id(), input.type().wireValue());
        entity.setCreatedAt(clock.instant());
        apply(entity, input);
        return repository.save(entity);
    }

    /**
     * Replaces a provider; an omitted {@code apiKey} keeps the stored one.
     *
     * @throws NotFoundException when it does not exist
     * @throws ValidationException when the body is not valid, or the change would leave a role it
     *     holds unserved
     */
    @Transactional
    public LlmProviderEntity replace(String id, JsonNode body) {
        return replace(id, body, true);
    }

    private LlmProviderEntity replace(String id, JsonNode body, boolean keyRequired) {
        LlmProviderEntity entity = get(id);
        LlmProviderInput input = validator.parse(body, id);
        if (keyRequired) {
            requireKey(input, entity);
        }
        Optional<EmbeddingModelSpec> before = isEmbeddingProvider(id) ? LlmSettingsService.specOf(entity)
                : Optional.empty();
        apply(entity, input);
        List<FieldError> roleErrors = new ArrayList<>();
        List<String> roles = settings.roles(id);
        if (roles.contains("chat")) {
            roleErrors.addAll(LlmSettingsService.chatProblems(entity, "chatProvider"));
        }
        if (roles.contains("embedding")) {
            roleErrors.addAll(LlmSettingsService.embeddingProblems(entity, "embeddingProvider"));
        }
        if (roles.contains("tts")) {
            roleErrors.addAll(LlmSettingsService.ttsProblems(entity, "ttsProvider"));
        }
        if (!roleErrors.isEmpty()) {
            throw new ValidationException("The change would leave a role this provider holds unserved;"
                    + " reassign the role first.", roleErrors);
        }
        LlmProviderEntity saved = repository.save(entity);
        if (roles.contains("embedding")) {
            Optional<EmbeddingModelSpec> after = LlmSettingsService.specOf(saved);
            if (!before.equals(after)) {
                EmbeddingModelSpec oldSpec = before.orElse(null);
                EmbeddingModelSpec newSpec = after.orElse(null);
                AfterCommit.run(() -> events.publishEvent(new EmbeddingModelChangedEvent(oldSpec, newSpec)));
            }
        }
        return saved;
    }

    /**
     * Creates or replaces a provider (settings import): a body without {@code apiKey} keeps a stored
     * key. An exported bundle carries no keys, so an imported {@code anthropic} provider may be left
     * without one; its calls then fail with an authentication error until the key is set.
     */
    @Transactional
    public LlmProviderEntity upsert(JsonNode body) {
        JsonNode id = body == null ? null : body.get("id");
        if (id != null && id.isString() && repository.existsById(id.asString())) {
            return replace(id.asString(), body, false);
        }
        return create(body, false);
    }

    /**
     * Deletes a provider.
     *
     * @throws NotFoundException when it does not exist
     * @throws ConflictException ({@code LLM_PROVIDER_IN_USE}) while it holds a role
     */
    @Transactional
    public void delete(String id) {
        LlmProviderEntity entity = get(id);
        List<String> roles = settings.roles(id);
        if (!roles.isEmpty()) {
            throw new ConflictException(ErrorCodes.LLM_PROVIDER_IN_USE, "LLM provider '" + id
                    + "' is assigned to the " + String.join(" and ", roles)
                    + " role; assign another provider in PUT /v1/llm/settings first.");
        }
        repository.delete(entity);
    }

    /** True when {@code entity} has an API key stored. */
    public static boolean apiKeySet(LlmProviderEntity entity) {
        return entity.getSecretCiphertext() != null && entity.getSecretNonce() != null;
    }

    /**
     * The configuration snapshot of {@code entity}, with its API key decrypted.
     */
    public LlmProviderConfig config(LlmProviderEntity entity) {
        String key = apiKeySet(entity)
                ? cipher.decryptToString(new Encrypted(entity.getSecretCiphertext(), entity.getSecretNonce()))
                : null;
        Map<String, Object> options = entity.getOptions() == null ? Map.of() : entity.getOptions();
        Effort effort = null;
        if (options.get("effort") instanceof String text) {
            try {
                effort = Effort.fromWire(text);
            } catch (IllegalArgumentException ignored) {
                // stored before validation tightened; fall back to the purpose default
            }
        }
        Integer maxTokens = options.get("maxTokens") instanceof Number n ? n.intValue() : null;
        Double temperature = options.get("temperature") instanceof Number n ? n.doubleValue() : null;
        Map<String, String> headers = new LinkedHashMap<>();
        if (options.get("extraHeaders") instanceof Map<?, ?> map) {
            map.forEach((k, v) -> headers.put(String.valueOf(k), String.valueOf(v)));
        }
        boolean local = Boolean.TRUE.equals(options.get("local"));
        return new LlmProviderConfig(entity.getId(), LlmProviderType.fromWire(entity.getType()), entity.getBaseUrl(),
                entity.getChatModel(), entity.getEmbeddingModel(), entity.getEmbeddingDimensions(), effort, maxTokens,
                temperature, headers, local, key, entity.getRequestsPerMinute(), entity.getTokensPerDay(),
                entity.isEnabled(), entity.getKnowledgeCutoff());
    }

    /** The configuration snapshot of provider {@code id}. */
    @Transactional(readOnly = true)
    public LlmProviderConfig config(String id) {
        return config(get(id));
    }

    private boolean isEmbeddingProvider(String id) {
        return id.equals(settings.get().embeddingProvider());
    }

    private void apply(LlmProviderEntity entity, LlmProviderInput input) {
        boolean typeChanged = !Objects.equals(entity.getType(), input.type().wireValue());
        entity.setType(input.type().wireValue());
        entity.setBaseUrl(input.baseUrl());
        entity.setChatModel(input.chatModel());
        entity.setEmbeddingModel(input.embeddingModel());
        entity.setEmbeddingDimensions(input.embeddingDimensions());
        entity.setOptions(input.options());
        entity.setRequestsPerMinute(input.requestsPerMinute());
        entity.setTokensPerDay(input.tokensPerDay());
        entity.setEnabled(input.enabled());
        entity.setKnowledgeCutoff(input.knowledgeCutoff());
        if (input.apiKey() != null) {
            Encrypted encrypted = cipher.encrypt(input.apiKey());
            entity.setSecret(encrypted.ciphertext(), encrypted.nonce());
        }
        if (typeChanged) {
            // A different implementation: what was learned about the old one no longer applies.
            entity.setSupportsTools(null);
        }
        Instant now = clock.instant();
        entity.setUpdatedAt(now);
    }

    /** Anthropic needs a key: the SDK would otherwise look for credentials elsewhere on the host. */
    private static void requireKey(LlmProviderInput input, LlmProviderEntity existing) {
        if (input.type() == LlmProviderType.ANTHROPIC && input.apiKey() == null
                && (existing == null || !apiKeySet(existing))) {
            throw ValidationException.forField("apiKey", "is required for anthropic providers"
                    + " (create one in the Anthropic Console)");
        }
    }
}
