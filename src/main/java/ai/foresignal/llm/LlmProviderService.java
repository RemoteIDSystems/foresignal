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
import java.time.LocalDate;
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
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code llm_provider} rows behind {@code /v1/llm/providers} (prompt 026).
 *
 * <p>The API key is encrypted with {@link CredentialCipher} and never returned. A replace that omits
 * {@code apiKey} keeps the stored key. A provider assigned to a role cannot be deleted
 * ({@code 409 LLM_PROVIDER_IN_USE}) and cannot be changed so that it no longer serves that role.
 * Changing the model or dimensions of the active embedding provider publishes
 * {@link EmbeddingModelChangedEvent} once the change commits.
 *
 * <p>Prompt R2-011: a provider is shared (no owner, managed under {@code llm.shared.manage}) or personal (owned by a
 * user's principal, managed by that user under {@code self.llm}); {@link LlmProviderScope} decides who sees and
 * manages which. A replace keeps the kind. Only a shared provider may carry per-user caps, and only a shared provider
 * may hold an instance role ({@link LlmSettingsService}).
 */
@Service
public class LlmProviderService {

    /** The id a {@code probeLlmProvider} body without one is validated under. */
    static final String PROBE_ID = "probe";

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
     * The providers {@code scope} sees, by id (prompt R2-011).
     *
     * @param scope the caller's scope
     * @return the providers
     */
    @Transactional(readOnly = true)
    public List<LlmProviderEntity> list(LlmProviderScope scope) {
        return repository.findAllByOrderByIdAsc().stream().filter(p -> scope.sees(p.getOwnerPrincipalId())).toList();
    }

    /**
     * The shared providers, by id (prompt R2-011).
     *
     * @return the providers
     */
    @Transactional(readOnly = true)
    public List<LlmProviderEntity> shared() {
        return repository.findByOwnerPrincipalIdIsNullOrderByIdAsc();
    }

    /**
     * A user's personal providers, by id (prompt R2-011).
     *
     * @param principalId the user's principal
     * @return the providers
     */
    @Transactional(readOnly = true)
    public List<LlmProviderEntity> personal(String principalId) {
        return principalId == null ? List.of() : repository.findByOwnerPrincipalIdOrderByIdAsc(principalId);
    }

    /**
     * The personal provider a user's chat goes to (prompt R2-011): their first enabled personal provider with a chat
     * model, by id; empty when they have none.
     *
     * @param principalId the user's principal, or null for the machine
     * @return the provider id, or empty
     */
    @Transactional(readOnly = true)
    public Optional<String> personalChatProvider(String principalId) {
        return personal(principalId).stream()
                .filter(p -> p.isEnabled() && p.getChatModel() != null)
                .map(LlmProviderEntity::getId)
                .findFirst();
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
        return create(body, true, LlmProviderScope.MACHINE);
    }

    /**
     * Creates a provider for {@code scope} (prompt R2-011): personal for a session holding {@code self.llm} unless the
     * body asks for {@code shared: true}, shared for an API token.
     *
     * @param body an {@code LlmProviderInput}
     * @param scope the caller's scope
     * @return the provider
     * @throws ValidationException when the body is not valid
     * @throws ConflictException ({@code LLM_PROVIDER_EXISTS}) when the id is taken
     * @throws ai.foresignal.api.error.ForesignalException {@code 403 PERMISSION_DENIED} when the caller may not create
     *     that kind
     */
    @Transactional
    public LlmProviderEntity create(JsonNode body, LlmProviderScope scope) {
        return create(body, true, scope);
    }

    private LlmProviderEntity create(JsonNode body, boolean keyRequired, LlmProviderScope scope) {
        LlmProviderInput input = validator.parse(body, null);
        String owner = scope.ownerForNew(input.shared());
        requireSharedForPerUserCaps(input, owner);
        if (repository.existsById(input.id())) {
            throw new ConflictException(ErrorCodes.LLM_PROVIDER_EXISTS,
                    "An LLM provider with id '" + input.id() + "' already exists.");
        }
        if (keyRequired) {
            requireKey(input, null);
        }
        LlmProviderEntity entity = new LlmProviderEntity(input.id(), input.type().wireValue());
        entity.setOwnerPrincipalId(owner);
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
        requireSharedForPerUserCaps(input, entity.getOwnerPrincipalId());
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
        return create(body, false, LlmProviderScope.MACHINE);
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
        return config(entity.getId(), LlmProviderType.fromWire(entity.getType()), entity.getBaseUrl(),
                entity.getChatModel(), entity.getEmbeddingModel(), entity.getEmbeddingDimensions(), entity.getOptions(),
                key, entity.getRequestsPerMinute(), entity.getTokensPerDay(), entity.isEnabled(),
                entity.getKnowledgeCutoff(), entity.getPricing(), entity.getSpendCaps(), entity.getOwnerPrincipalId(),
                entity.getPerUserSpendCaps());
    }

    /**
     * The configuration snapshot of an unsaved {@code LlmProviderInput} body, for
     * {@code probeLlmProvider}; nothing is stored. The body is validated as a create, except that
     * {@code id} may be left out. Without {@code apiKey}, the stored key of the provider with the
     * body's {@code id} is used, when there is one, so a saved provider can be tested with edits that are
     * not saved yet.
     *
     * @throws ValidationException when the body is not valid, or an anthropic body has no key
     */
    @Transactional(readOnly = true)
    public LlmProviderConfig probeConfig(JsonNode body) {
        JsonNode checked = body;
        if (body != null && body.isObject() && (body.get("id") == null || body.get("id").isNull())) {
            ObjectNode copy = ((ObjectNode) body).deepCopy();
            copy.put("id", PROBE_ID);
            checked = copy;
        }
        LlmProviderInput input = validator.parse(checked, null);
        Optional<LlmProviderEntity> saved = repository.findById(input.id());
        requireKey(input, saved.orElse(null));
        String key = input.apiKey();
        if (key == null && saved.isPresent() && apiKeySet(saved.get())) {
            LlmProviderEntity entity = saved.get();
            key = cipher.decryptToString(new Encrypted(entity.getSecretCiphertext(), entity.getSecretNonce()));
        }
        return config(input.id(), input.type(), input.baseUrl(), input.chatModel(), input.embeddingModel(),
                input.embeddingDimensions(), input.options(), key, input.requestsPerMinute(), input.tokensPerDay(),
                input.enabled(), input.knowledgeCutoff(), input.pricing(), input.spendCaps(),
                saved.map(LlmProviderEntity::getOwnerPrincipalId).orElse(null), input.perUserSpendCaps());
    }

    private static LlmProviderConfig config(String id, LlmProviderType type, String baseUrl, String chatModel,
            String embeddingModel, Integer embeddingDimensions, Map<String, Object> storedOptions, String key,
            int requestsPerMinute, long tokensPerDay, boolean enabled, LocalDate knowledgeCutoff,
            LlmPricing pricing, LlmSpendCaps spendCaps, String ownerPrincipalId, LlmSpendCaps perUserSpendCaps) {
        Map<String, Object> options = storedOptions == null ? Map.of() : storedOptions;
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
        return new LlmProviderConfig(id, type, baseUrl, chatModel, embeddingModel, embeddingDimensions, effort,
                maxTokens, temperature, headers, local, key, requestsPerMinute, tokensPerDay, enabled, knowledgeCutoff,
                pricing, spendCaps, ownerPrincipalId, perUserSpendCaps);
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
        entity.setPricing(input.pricing());
        entity.setSpendCaps(input.spendCaps());
        entity.setPerUserSpendCaps(input.perUserSpendCaps());
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

    /** Per-user caps bound each user's share of a shared provider; a personal provider has one user already. */
    private static void requireSharedForPerUserCaps(LlmProviderInput input, String owner) {
        if (owner != null && input.perUserSpendCaps().isSet()) {
            String field = input.perUserSpendCaps().hourlyUsd() != null ? "perUserHourlyCapUsd"
                    : input.perUserSpendCaps().dailyUsd() != null ? "perUserDailyCapUsd" : "perUserMonthlyCapUsd";
            throw ValidationException.forField(field, "applies to shared providers only; a personal provider's"
                    + " hourlyCapUsd, dailyCapUsd and monthlyCapUsd are already its owner's own caps");
        }
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
