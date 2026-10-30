package ai.foresignal.news;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.LlmSpendWindow;
import ai.foresignal.notify.NotificationTranslator;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * Reads, validates and stores {@link TranslationSettings} (prompt NP-002; {@code getTranslationSettings} and
 * {@code updateTranslationSettings}, admin). A PUT replaces the whole value: a missing member takes its default, every
 * problem is reported in one 400, and the read-only {@code spend} of a GET may be sent back and is ignored. The GET
 * adds {@code spend}, the translation spend in the current budget window, summed from {@code news_translation}.
 */
@Service
public class TranslationSettingsService {

    static final Set<String> FIELDS = Set.of("enabled", "targetLanguages", "fields", "engine", "budget",
            "minSourceLanguageConfidence", "spend");

    static final Set<String> BUDGET_FIELDS = Set.of("window", "capUsd");

    private final AppSettingService settings;
    private final NewsTranslationStore store;
    private final Clock clock;

    /** Creates the service. */
    public TranslationSettingsService(AppSettingService settings, NewsTranslationStore store, Clock clock) {
        this.settings = settings;
        this.store = store;
        this.clock = clock;
    }

    /** The stored settings, or the shipped defaults (off). */
    public TranslationSettings get() {
        Optional<TranslationSettings> stored;
        try {
            stored = settings.get(SettingKeys.TRANSLATION, TranslationSettings.class);
        } catch (RuntimeException e) {
            stored = Optional.empty();
        }
        return stored.orElseGet(TranslationSettings::defaults);
    }

    /** Stores {@code value} (already validated by {@link #parse}). */
    public TranslationSettings put(TranslationSettings value) {
        settings.put(SettingKeys.TRANSLATION, value);
        return value;
    }

    /** The {@code TranslationSettings} response: the settings plus the read-only {@code spend}. */
    public Map<String, Object> view(TranslationSettings s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", s.enabled());
        out.put("targetLanguages", s.targetLanguages());
        out.put("fields", s.fields());
        out.put("engine", s.engine());
        Map<String, Object> budget = new LinkedHashMap<>();
        budget.put("window", s.budget().window());
        budget.put("capUsd", s.budget().capUsd());
        out.put("budget", budget);
        out.put("minSourceLanguageConfidence", s.minSourceLanguageConfidence());
        out.put("spend", spend(s));
        return out;
    }

    /** The {@code LlmSpendWindow} of the current budget window. */
    Map<String, Object> spend(TranslationSettings s) {
        LlmSpendWindow window = s.budget().spendWindow();
        Instant start = window.start(clock.instant());
        BigDecimal spent = store.spentSince(start);
        BigDecimal cap = s.budget().capUsd();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("window", window.wireValue());
        out.put("start", start.toString());
        out.put("resetsAt", window.end(start).toString());
        out.put("spentUsd", spent);
        out.put("reservedUsd", BigDecimal.ZERO);
        out.put("capUsd", cap);
        out.put("remainingUsd", cap == null ? null : cap.subtract(spent).max(BigDecimal.ZERO));
        out.put("exhausted", cap != null && spent.compareTo(cap) >= 0);
        return out;
    }

    /** True when the translation budget of {@code s} is spent for the current window. */
    public boolean exhausted(TranslationSettings s) {
        BigDecimal cap = s.budget().capUsd();
        if (cap == null) {
            return false;
        }
        Instant start = s.budget().spendWindow().start(clock.instant());
        return store.spentSince(start).compareTo(cap) >= 0;
    }

    /**
     * Parses a {@code TranslationSettingsInput} body. Language tags are canonicalized ({@code pt-br} is
     * {@code pt-BR}) and deduplicated.
     *
     * @throws ValidationException listing every problem
     */
    public static TranslationSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a TranslationSettingsInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a TranslationSettingsInput member"));
            }
        }
        boolean enabled = false;
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode == null || !enabledNode.isBoolean()) {
            errors.add(new FieldError("enabled", "is required and must be true or false"));
        } else {
            enabled = enabledNode.asBoolean();
        }
        List<String> targets = null;
        JsonNode targetsNode = body.get("targetLanguages");
        if (targetsNode != null && !targetsNode.isNull()) {
            if (!targetsNode.isArray()) {
                errors.add(new FieldError("targetLanguages", "must be an array of BCP 47 language tags"));
            } else {
                Set<String> seen = new LinkedHashSet<>();
                int i = 0;
                for (JsonNode tag : targetsNode) {
                    String canonical = tag.isString() ? NotificationTranslator.canonicalTag(tag.asString()) : null;
                    if (canonical == null) {
                        errors.add(new FieldError("targetLanguages[" + i + "]", "is not a BCP 47 language tag"));
                    } else {
                        seen.add(canonical);
                    }
                    i++;
                }
                if (seen.size() > TranslationSettings.MAX_TARGETS) {
                    errors.add(new FieldError("targetLanguages", "may name at most "
                            + TranslationSettings.MAX_TARGETS + " languages"));
                }
                targets = new ArrayList<>(seen);
            }
        }
        List<String> fields = null;
        JsonNode fieldsNode = body.get("fields");
        if (fieldsNode != null && !fieldsNode.isNull()) {
            if (!fieldsNode.isArray()) {
                errors.add(new FieldError("fields", "must be an array"));
            } else {
                Set<String> seen = new LinkedHashSet<>();
                for (JsonNode f : fieldsNode) {
                    String v = f.isString() ? f.asString() : null;
                    if (!TranslationSettings.TITLE.equals(v) && !TranslationSettings.EXCERPT.equals(v)) {
                        errors.add(new FieldError("fields", "may hold only title and excerpt"));
                    } else {
                        seen.add(v);
                    }
                }
                fields = new ArrayList<>(seen);
            }
        }
        String engine = null;
        JsonNode engineNode = body.get("engine");
        if (engineNode != null && !engineNode.isNull()) {
            if (!engineNode.isString() || !TranslationSettings.ENGINE_LLM.equals(engineNode.asString())) {
                errors.add(new FieldError("engine", "must be llm"));
            } else {
                engine = engineNode.asString();
            }
        }
        TranslationSettings.Budget budget = null;
        JsonNode budgetNode = body.get("budget");
        if (budgetNode != null && !budgetNode.isNull()) {
            if (!budgetNode.isObject()) {
                errors.add(new FieldError("budget", "must be an object"));
            } else {
                for (String field : budgetNode.propertyNames()) {
                    if (!BUDGET_FIELDS.contains(field)) {
                        errors.add(new FieldError("budget." + field, "is not a budget member"));
                    }
                }
                String window = null;
                JsonNode w = budgetNode.get("window");
                if (w != null && !w.isNull()) {
                    if (!w.isString() || !List.of("hour", "day", "month").contains(w.asString())) {
                        errors.add(new FieldError("budget.window", "must be hour, day or month"));
                    } else {
                        window = w.asString();
                    }
                }
                BigDecimal cap = null;
                JsonNode c = budgetNode.get("capUsd");
                if (c != null && !c.isNull()) {
                    if (!c.isNumber() || c.decimalValue().signum() < 0) {
                        errors.add(new FieldError("budget.capUsd", "must be a number of at least 0, or null"));
                    } else {
                        cap = c.decimalValue();
                    }
                }
                budget = new TranslationSettings.Budget(window, cap);
            }
        }
        Double confidence = null;
        JsonNode confidenceNode = body.get("minSourceLanguageConfidence");
        if (confidenceNode != null && !confidenceNode.isNull()) {
            double v = confidenceNode.isNumber() ? confidenceNode.asDouble() : Double.NaN;
            if (Double.isNaN(v) || v < 0 || v > 1) {
                errors.add(new FieldError("minSourceLanguageConfidence", "must be a number from 0 to 1"));
            } else {
                confidence = v;
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The translation settings are not valid.", errors);
        }
        return new TranslationSettings(enabled, targets, fields, engine, budget, confidence);
    }
}
