package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code getLlmSettings} / {@code updateLlmSettings}: the roles and options in
 * {@code app_setting['llm']}.
 *
 * <p>An assigned provider must exist and be enabled; the chat provider needs a {@code chatModel};
 * the embedding provider needs {@code embeddingModel} and {@code embeddingDimensions} and cannot be
 * an {@code anthropic} provider (Anthropic has no embeddings endpoint). Reassigning the embedding
 * role publishes {@link EmbeddingModelChangedEvent} after the change commits. The {@code tts} provider
 * (prompt 141) must be an enabled {@code openai-compatible} provider: speech uses the OpenAI
 * {@code /audio/speech} endpoint, which Anthropic and Ollama do not offer.
 */
@Service
public class LlmSettingsService {

    private final AppSettingService settings;
    private final LlmProviderRepository providers;
    private final ApplicationEventPublisher events;

    /** Creates the service. */
    public LlmSettingsService(AppSettingService settings, LlmProviderRepository providers,
            ApplicationEventPublisher events) {
        this.settings = settings;
        this.providers = providers;
        this.events = events;
    }

    /** The current settings, or the defaults when never set. */
    public LlmSettings get() {
        return settings.getOrDefault(SettingKeys.LLM, LlmSettings.class, () -> LlmSettings.DEFAULTS);
    }

    /**
     * Validates and stores {@code next}.
     *
     * @throws ValidationException listing every role error
     */
    @Transactional
    public LlmSettings update(LlmSettings next) {
        List<FieldError> errors = new ArrayList<>();
        if (next.chatProvider() != null) {
            Optional<LlmProviderEntity> chat = providers.findById(next.chatProvider());
            if (chat.isEmpty()) {
                errors.add(new FieldError("chatProvider", "provider '" + next.chatProvider() + "' does not exist"));
            } else {
                errors.addAll(chatProblems(chat.get(), "chatProvider"));
            }
        }
        if (next.embeddingProvider() != null) {
            Optional<LlmProviderEntity> embedding = providers.findById(next.embeddingProvider());
            if (embedding.isEmpty()) {
                errors.add(new FieldError("embeddingProvider",
                        "provider '" + next.embeddingProvider() + "' does not exist"));
            } else {
                errors.addAll(embeddingProblems(embedding.get(), "embeddingProvider"));
            }
        }
        if (next.ttsProvider() != null) {
            Optional<LlmProviderEntity> tts = providers.findById(next.ttsProvider());
            if (tts.isEmpty()) {
                errors.add(new FieldError("ttsProvider", "provider '" + next.ttsProvider() + "' does not exist"));
            } else {
                errors.addAll(ttsProblems(tts.get(), "ttsProvider"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The LLM settings are not valid.", errors);
        }
        EmbeddingModelSpec before = embeddingSpec().orElse(null);
        settings.put(SettingKeys.LLM, next);
        EmbeddingModelSpec after = specOf(next.embeddingProvider()).orElse(null);
        if (!Objects.equals(before, after)) {
            AfterCommit.run(() -> events.publishEvent(new EmbeddingModelChangedEvent(before, after)));
        }
        return next;
    }

    /** The active embedding spec: the embedding provider's model and dimensions, when assigned. */
    public Optional<EmbeddingModelSpec> embeddingSpec() {
        return specOf(get().embeddingProvider());
    }

    /** The roles {@code providerId} holds: {@code chat}, {@code embedding}, {@code tts} (prompt 141). */
    public List<String> roles(String providerId) {
        LlmSettings current = get();
        List<String> roles = new ArrayList<>();
        if (providerId.equals(current.chatProvider())) {
            roles.add("chat");
        }
        if (providerId.equals(current.embeddingProvider())) {
            roles.add("embedding");
        }
        if (providerId.equals(current.ttsProvider())) {
            roles.add("tts");
        }
        return roles;
    }

    /** What stops {@code provider} from serving the chat role; empty when it can. */
    static List<FieldError> chatProblems(LlmProviderEntity provider, String field) {
        List<FieldError> errors = new ArrayList<>();
        if (!provider.isShared()) {
            // R2-011: an instance role serves everyone, so it never runs on one user's key.
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is a personal provider; only a shared"
                    + " provider can hold an instance role"));
            return errors;
        }
        if (!provider.isEnabled()) {
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is disabled"));
        }
        if (provider.getChatModel() == null) {
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' has no chatModel"));
        }
        return errors;
    }

    /** What stops {@code provider} from serving the embedding role; empty when it can. */
    static List<FieldError> embeddingProblems(LlmProviderEntity provider, String field) {
        List<FieldError> errors = new ArrayList<>();
        if (!provider.isShared()) {
            // R2-011: an instance role serves everyone, so it never runs on one user's key.
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is a personal provider; only a shared"
                    + " provider can hold an instance role"));
            return errors;
        }
        if (LlmProviderType.ANTHROPIC.wireValue().equals(provider.getType())) {
            errors.add(new FieldError(field, "Anthropic has no embeddings endpoint and cannot be the embedding provider;"
                    + " pair it with an openai-compatible or ollama provider for embeddings"));
            return errors;
        }
        if (!provider.isEnabled()) {
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is disabled"));
        }
        if (provider.getEmbeddingModel() == null || provider.getEmbeddingDimensions() == null) {
            errors.add(new FieldError(field,
                    "provider '" + provider.getId() + "' needs embeddingModel and embeddingDimensions"));
        }
        return errors;
    }

    /**
     * What stops {@code provider} from serving the {@code tts} role (prompt 141); empty when it can: it must be
     * enabled and {@code openai-compatible}.
     */
    static List<FieldError> ttsProblems(LlmProviderEntity provider, String field) {
        List<FieldError> errors = new ArrayList<>();
        if (!provider.isShared()) {
            // R2-011: an instance role serves everyone, so it never runs on one user's key.
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is a personal provider; only a shared"
                    + " provider can hold an instance role"));
            return errors;
        }
        if (!LlmProviderType.OPENAI_COMPATIBLE.wireValue().equals(provider.getType())) {
            errors.add(new FieldError(field, "the tts role needs an openai-compatible provider (an OpenAI-compatible"
                    + " /audio/speech endpoint such as OpenAI, Kokoro-FastAPI or openedai-speech)"));
            return errors;
        }
        if (!provider.isEnabled()) {
            errors.add(new FieldError(field, "provider '" + provider.getId() + "' is disabled"));
        }
        return errors;
    }

    private Optional<EmbeddingModelSpec> specOf(String providerId) {
        if (providerId == null) {
            return Optional.empty();
        }
        return providers.findById(providerId).flatMap(LlmSettingsService::specOf);
    }

    /** The embedding spec a provider row describes, when complete. */
    static Optional<EmbeddingModelSpec> specOf(LlmProviderEntity provider) {
        if (provider.getEmbeddingModel() == null || provider.getEmbeddingDimensions() == null) {
            return Optional.empty();
        }
        return Optional.of(new EmbeddingModelSpec(provider.getId(), provider.getEmbeddingModel(),
                provider.getEmbeddingDimensions()));
    }
}
