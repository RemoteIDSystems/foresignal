package ai.foresignal.intel;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The corroboration policy (prompt 154, {@code GET/PUT /v1/intel/corroboration}, OpenAPI
 * {@code IntelCorroboration}): the tunable "promote to a trusted signal" gates, lifted out of the code so an
 * operator can adjust them without a release.
 *
 * <ul>
 *   <li><strong>eventQuality</strong> — the {@link EventScorer.Quality} thresholds for an intelligence event's
 *       {@code strong} / {@code moderate} evidence quality, including an optional cross-modality requirement;
 *   <li><strong>situationActivation</strong> — the signal families and independent evidence groups a situation
 *       needs to move from {@code emerging} to {@code active} (prompt 118's hardcoded 2 &amp; 2);
 *   <li><strong>autoCorroborate</strong> — whether a lone agent/analyst/operator event searches for independent
 *       corroborating records on creation, and the space/time window and cap of that search
 *       ({@link AutoCorroboration}).
 * </ul>
 *
 * <p>Every gate defaults to the historical constant, so an instance with no setting behaves exactly as before.
 */
@Component
public class IntelCorroborationService {

    private static final Set<String> TOP = Set.of("eventQuality", "situationActivation", "autoCorroborate");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * The situation activation rule.
     *
     * @param minFamilies distinct signal families an emerging situation needs to activate
     * @param minGroups independent evidence groups it needs to activate
     */
    public record Activation(int minFamilies, int minGroups) {

        /** The historical default (prompt 118): 2 families and 2 independent groups. */
        public static final Activation DEFAULT = new Activation(2, 2);
    }

    /**
     * The automatic corroboration search.
     *
     * @param enabled whether a lone event searches for corroboration on creation
     * @param withinKm the search radius around the event's members, in kilometers
     * @param window the time window before and after the event's members' observations
     * @param maxMembers the most corroborating members one search attaches
     */
    public record AutoCorroborate(boolean enabled, double withinKm, Duration window, int maxMembers) {

        /** The default: on, 50 km, ±24 h, at most 10 members. */
        public static final AutoCorroborate DEFAULT = new AutoCorroborate(true, 50, Duration.ofHours(24), 10);
    }

    private final AppSettingService settings;
    private final JsonMapper json;

    /** Creates the service. */
    public IntelCorroborationService(AppSettingService settings, JsonMapper json) {
        this.settings = settings;
        this.json = json;
    }

    // ------------------------------------------------------------------------- typed accessors

    /** The event evidence-quality thresholds in force. */
    public EventScorer.Quality quality() {
        Map<?, ?> m = map("eventQuality");
        EventScorer.Quality d = EventScorer.Quality.DEFAULT;
        return new EventScorer.Quality(intValue(m.get("strongFamilies"), d.strongFamilies()),
                intValue(m.get("strongGroups"), d.strongGroups()),
                intValue(m.get("moderateFamilies"), d.moderateFamilies()),
                intValue(m.get("requireModalities"), d.requireModalities()));
    }

    /** The situation activation rule in force. */
    public Activation situationActivation() {
        Map<?, ?> m = map("situationActivation");
        return new Activation(intValue(m.get("minFamilies"), Activation.DEFAULT.minFamilies()),
                intValue(m.get("minGroups"), Activation.DEFAULT.minGroups()));
    }

    /** The automatic corroboration search in force. */
    public AutoCorroborate autoCorroborate() {
        Map<?, ?> m = map("autoCorroborate");
        AutoCorroborate d = AutoCorroborate.DEFAULT;
        Duration window = d.window();
        if (m.get("window") != null) {
            try {
                window = Durations.parse(m.get("window").toString());
            } catch (RuntimeException e) {
                window = d.window();
            }
        }
        return new AutoCorroborate(boolValue(m.get("enabled"), d.enabled()),
                doubleValue(m.get("withinKm"), d.withinKm()), window, intValue(m.get("maxMembers"), d.maxMembers()));
    }

    // ------------------------------------------------------------------------- document

    /** The {@code IntelCorroboration} document in force. */
    public Map<String, Object> get() {
        EventScorer.Quality q = quality();
        Activation a = situationActivation();
        AutoCorroborate c = autoCorroborate();
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> eventQuality = new LinkedHashMap<>();
        eventQuality.put("strongFamilies", q.strongFamilies());
        eventQuality.put("strongGroups", q.strongGroups());
        eventQuality.put("moderateFamilies", q.moderateFamilies());
        eventQuality.put("requireModalities", q.requireModalities());
        out.put("eventQuality", eventQuality);
        Map<String, Object> activation = new LinkedHashMap<>();
        activation.put("minFamilies", a.minFamilies());
        activation.put("minGroups", a.minGroups());
        out.put("situationActivation", activation);
        Map<String, Object> auto = new LinkedHashMap<>();
        auto.put("enabled", c.enabled());
        auto.put("withinKm", c.withinKm());
        auto.put("window", Durations.format(c.window()));
        auto.put("maxMembers", c.maxMembers());
        out.put("autoCorroborate", auto);
        return out;
    }

    /**
     * Replaces the policy with an {@code IntelCorroboration} document; members left out return to their defaults.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED} with every failing field
     */
    @Transactional
    public Map<String, Object> put(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The body must be an IntelCorroboration object.",
                    List.of(new FieldError("body", "must be an object")));
        }
        Map<String, Object> doc = json.convertValue(body, MAP);
        List<FieldError> errors = new ArrayList<>();
        for (String key : doc.keySet()) {
            if (!TOP.contains(key)) {
                errors.add(new FieldError(key, "is not an IntelCorroboration member"));
            }
        }
        Map<String, Object> eventQuality = object(doc, "eventQuality",
                Set.of("strongFamilies", "strongGroups", "moderateFamilies", "requireModalities"), errors);
        range(eventQuality, "eventQuality.strongFamilies", "strongFamilies", 1, 100, errors);
        range(eventQuality, "eventQuality.strongGroups", "strongGroups", 1, 100, errors);
        range(eventQuality, "eventQuality.moderateFamilies", "moderateFamilies", 1, 100, errors);
        range(eventQuality, "eventQuality.requireModalities", "requireModalities", 0, 100, errors);
        Map<String, Object> activation = object(doc, "situationActivation", Set.of("minFamilies", "minGroups"), errors);
        range(activation, "situationActivation.minFamilies", "minFamilies", 1, 100, errors);
        range(activation, "situationActivation.minGroups", "minGroups", 1, 100, errors);
        Map<String, Object> auto = object(doc, "autoCorroborate",
                Set.of("enabled", "withinKm", "window", "maxMembers"), errors);
        if (auto != null && auto.get("enabled") != null && !(auto.get("enabled") instanceof Boolean)) {
            errors.add(new FieldError("autoCorroborate.enabled", "must be a boolean"));
        }
        range(auto, "autoCorroborate.withinKm", "withinKm", 0, 20_000, errors);
        range(auto, "autoCorroborate.maxMembers", "maxMembers", 1, 100, errors);
        if (auto != null && auto.get("window") != null) {
            try {
                Durations.parse(auto.get("window").toString());
            } catch (RuntimeException e) {
                errors.add(new FieldError("autoCorroborate.window", "must be an ISO-8601 or shorthand duration"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The corroboration policy is not valid.",
                    errors);
        }
        store(SettingKeys.INTEL_CORROBORATION, strip(doc));
        return get();
    }

    private Map<String, Object> strip(Map<String, Object> doc) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : TOP) {
            if (doc.get(key) instanceof Map<?, ?> m && !m.isEmpty()) {
                out.put(key, m);
            }
        }
        return out.isEmpty() ? null : out;
    }

    private void store(String key, Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
    }

    private Map<?, ?> map(String member) {
        try {
            Object doc = settings.get(SettingKeys.INTEL_CORROBORATION, Map.class).orElse(null);
            if (doc instanceof Map<?, ?> m && m.get(member) instanceof Map<?, ?> inner) {
                return inner;
            }
        } catch (RuntimeException e) {
            // fall through to the empty map and the defaults
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> doc, String key, Set<String> allowed,
            List<FieldError> errors) {
        Object v = doc.get(key);
        if (v == null) {
            return null;
        }
        if (!(v instanceof Map<?, ?> m)) {
            errors.add(new FieldError(key, "must be an object"));
            return null;
        }
        for (Object k : m.keySet()) {
            if (!allowed.contains(String.valueOf(k))) {
                errors.add(new FieldError(key + "." + k, "is not a member of " + key));
            }
        }
        return new LinkedHashMap<>((Map<String, Object>) m);
    }

    private static void range(Map<String, Object> m, String field, String key, double min, double max,
            List<FieldError> errors) {
        if (m == null || m.get(key) == null) {
            return;
        }
        if (!(m.get(key) instanceof Number n) || n.doubleValue() < min || n.doubleValue() > max) {
            errors.add(new FieldError(field, "must be a number from " + min + " to " + max));
        }
    }

    private static int intValue(Object v, int fallback) {
        return v instanceof Number n ? n.intValue() : fallback;
    }

    private static double doubleValue(Object v, double fallback) {
        return v instanceof Number n ? n.doubleValue() : fallback;
    }

    private static boolean boolValue(Object v, boolean fallback) {
        return v instanceof Boolean b ? b : fallback;
    }
}
