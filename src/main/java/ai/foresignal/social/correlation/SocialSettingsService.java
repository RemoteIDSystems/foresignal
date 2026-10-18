package ai.foresignal.social.correlation;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.VelocityDetector;
import ai.foresignal.social.narrative.NarrativeVocabulary;
import ai.foresignal.social.narrative.SocialEvidenceGrades;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The instance social settings (prompt 137, {@code GET/PUT /v1/settings/social}, OpenAPI {@code SocialSettings}): one
 * document over the {@code social.*} app settings of prompts 135–137 (languages, minimum regional baseline, velocity,
 * claim extraction, track-record grading and overrides, {@code keepMedia}, comparison windows, attention-gap
 * thresholds, cue triggers and caps). A PUT replaces the document; changes apply to the next evaluation, and
 * comparisons already final keep their status. Typed accessors serve the 137 components.
 */
@Component
public class SocialSettingsService {

    /** Default change-point families that cue (outage, vessel, flight and fire series). */
    public static final List<String> CHANGE_POINT_FAMILIES = List.of("outage", "internet-measurement",
            "ais-behaviour", "aviation-ops", "aviation-military", "fire");

    /** The default canonical families that cue. */
    public static final List<String> DEFAULT_CUE_FAMILIES = List.of("conflict", "seismic", "fire", "outage",
            "disaster");

    private static final Set<String> TOP = Set.of("languages", "minRegionBaseline", "velocity", "llmClaimMinPosts",
            "llmClaimMinEvidenceGroups", "keepMedia", "trackRecord", "compare", "attentionGap", "cues");

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    /**
     * The comparison settings.
     *
     * @param minGroups the evidence groups a narrative needs (without a velocity finding)
     * @param before the default window before the claim time
     * @param after the default window after the claim time
     * @param familyWindows the operator's family overrides
     */
    public record Compare(int minGroups, Duration before, Duration after, Map<String, Map<String, Object>> familyWindows) {}

    /**
     * The attention-gap settings.
     *
     * @param minSeverity {@code notable} or {@code significant}
     * @param percentile the percentile of the expected distribution below which attention is a gap
     * @param lookbackDays the trailing days of past events
     * @param windowHours the hours after the event that are counted
     */
    public record Gap(String minSeverity, double percentile, int lookbackDays, int windowHours) {}

    /**
     * The automatic cue triggers.
     *
     * @param situationActivated cue when a situation activates
     * @param situationEscalated cue when a situation escalates
     * @param findingTypes the {@code significant} finding types that cue
     * @param anomalies cue on 100 anomalies
     * @param criticalAlerts cue on critical alerts
     * @param canonicalFamilies the canonical families that cue
     * @param minEvidenceGroups the evidence groups a canonical event needs
     */
    public record Triggers(boolean situationActivated, boolean situationEscalated, List<String> findingTypes,
            boolean anomalies, boolean criticalAlerts, List<String> canonicalFamilies, int minEvidenceGroups) {}

    /**
     * The cue settings.
     *
     * @param enabled automatic cues on
     * @param triggers the triggers
     * @param maxPerDay cues per UTC day (automatic and manual)
     * @param maxCostUsdPerCue the default and ceiling cost cap of one cue
     * @param platforms the adapters cues may use; empty for every enabled search-capable one
     * @param xaiEnabled the optional {@code xai-x-search} backend
     * @param xaiMaxResults its results per cue
     */
    public record Cues(boolean enabled, Triggers triggers, int maxPerDay, double maxCostUsdPerCue,
            List<String> platforms, boolean xaiEnabled, int xaiMaxResults) {}

    /**
     * The track-record grading.
     *
     * @param minAssessed assessed claims before the record counts
     * @param eShare the corroborated share that earns {@code E}
     * @param dShare the corroborated share that earns {@code D}
     */
    public record TrackRecord(int minAssessed, double eShare, double dShare) {

        /** The defaults. */
        public static final TrackRecord DEFAULT = new TrackRecord(SocialEvidenceGrades.MIN_ASSESSED,
                SocialEvidenceGrades.E_SHARE, SocialEvidenceGrades.D_SHARE);
    }

    private final AppSettingService settings;
    private final JsonMapper json;

    /** Creates the service. */
    public SocialSettingsService(AppSettingService settings, JsonMapper json) {
        this.settings = settings;
        this.json = json;
    }

    // ------------------------------------------------------------------------- typed accessors

    /** The comparison settings in force. */
    public Compare compare() {
        Map<?, ?> m = map(SettingKeys.SOCIAL_COMPARE);
        Map<?, ?> window = m.get("defaultWindow") instanceof Map<?, ?> w ? w : Map.of();
        Map<String, Map<String, Object>> families = new LinkedHashMap<>();
        if (m.get("familyWindows") instanceof Map<?, ?> f) {
            for (Map.Entry<?, ?> e : f.entrySet()) {
                if (e.getValue() instanceof Map<?, ?> v) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    v.forEach((k, x) -> copy.put(String.valueOf(k), x));
                    families.put(String.valueOf(e.getKey()), copy);
                }
            }
        }
        return new Compare((int) CompareRules.number(m.get("minGroups"), CorrelationVocabulary.DEFAULT_MIN_GROUPS),
                CompareRules.duration(window.get("before"), Duration.ofHours(6)),
                CompareRules.duration(window.get("after"), Duration.ofHours(24)), families);
    }

    /** The family rule in force (bundled rules, the operator's default window and family override). */
    public CompareRules.FamilyRule rule(String family) {
        Compare c = compare();
        return CompareRules.bundled().rule(family, c.before(), c.after(), c.familyWindows().get(family));
    }

    /** The attention-gap settings in force. */
    public Gap gap() {
        Map<?, ?> m = map(SettingKeys.SOCIAL_ATTENTION_GAP);
        String severity = m.get("minSeverity") == null ? "significant" : m.get("minSeverity").toString();
        return new Gap(severity, CompareRules.number(m.get("percentile"), CorrelationVocabulary.DEFAULT_GAP_PERCENTILE),
                (int) CompareRules.number(m.get("lookbackDays"), CorrelationVocabulary.DEFAULT_GAP_LOOKBACK_DAYS),
                (int) CompareRules.number(m.get("windowHours"), CorrelationVocabulary.DEFAULT_GAP_WINDOW_HOURS));
    }

    /** The cue settings in force. */
    public Cues cues() {
        Map<?, ?> m = map(SettingKeys.SOCIAL_CUES);
        Map<?, ?> t = m.get("triggers") instanceof Map<?, ?> x ? x : Map.of();
        Map<?, ?> xai = m.get("xaiXSearch") instanceof Map<?, ?> x ? x : Map.of();
        Triggers triggers = new Triggers(bool(t.get("situationActivated"), true), bool(t.get("situationEscalated"), true),
                t.containsKey("findingTypes") ? CompareRules.strings(t.get("findingTypes")) : List.of("change-point"),
                bool(t.get("anomalies"), true), bool(t.get("criticalAlerts"), true),
                t.containsKey("canonicalFamilies") ? CompareRules.strings(t.get("canonicalFamilies"))
                        : DEFAULT_CUE_FAMILIES,
                (int) CompareRules.number(t.get("minEvidenceGroups"), 2));
        return new Cues(bool(m.get("enabled"), true), triggers,
                (int) CompareRules.number(m.get("maxPerDay"), CorrelationVocabulary.DEFAULT_MAX_CUES_PER_DAY),
                CompareRules.number(m.get("maxCostUsdPerCue"), CorrelationVocabulary.DEFAULT_MAX_COST_PER_CUE),
                CompareRules.strings(m.get("platforms")), bool(xai.get("enabled"), false),
                (int) CompareRules.number(xai.get("maxResults"), CorrelationVocabulary.DEFAULT_XAI_MAX_RESULTS));
    }

    /** The track-record grading in force. */
    public TrackRecord trackRecord() {
        return trackRecord(map(SettingKeys.SOCIAL_TRACK_RECORD));
    }

    /** The track-record grading of a stored {@code social.trackRecord} object. */
    public static TrackRecord trackRecord(Map<?, ?> m) {
        if (m == null) {
            return TrackRecord.DEFAULT;
        }
        return new TrackRecord((int) CompareRules.number(m.get("minAssessed"), SocialEvidenceGrades.MIN_ASSESSED),
                CompareRules.number(m.get("gradeEShare"), SocialEvidenceGrades.E_SHARE),
                CompareRules.number(m.get("gradeDShare"), SocialEvidenceGrades.D_SHARE));
    }

    // ------------------------------------------------------------------------- document

    /** The {@code SocialSettings} document in force. */
    public Map<String, Object> get() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("languages", settings.get(SettingKeys.SOCIAL_LANGUAGES, List.class).orElse(
                SocialVocabulary.DEFAULT_LANGUAGES));
        out.put("minRegionBaseline", settings.get(SettingKeys.SOCIAL_MIN_REGION_BASELINE, Number.class)
                .map(Number::doubleValue).orElse(SocialVocabulary.DEFAULT_MIN_REGION_BASELINE));
        Map<?, ?> v = map(SettingKeys.SOCIAL_VELOCITY);
        Map<String, Object> velocity = new LinkedHashMap<>();
        velocity.put("minRatio", CompareRules.number(v.get("minRatio"), VelocityDetector.Thresholds.DEFAULT.minRatio()));
        velocity.put("minUniqueAuthors", (int) CompareRules.number(v.get("minUniqueAuthors"),
                VelocityDetector.Thresholds.DEFAULT.minUniqueAuthors()));
        velocity.put("dedupHours", (int) CompareRules.number(v.get("dedupHours"),
                VelocityDetector.Thresholds.DEFAULT.dedupHours()));
        out.put("velocity", velocity);
        out.put("llmClaimMinPosts", settings.get(SettingKeys.SOCIAL_LLM_CLAIM_MIN_POSTS, Number.class)
                .map(Number::intValue).orElse(NarrativeVocabulary.DEFAULT_LLM_MIN_POSTS));
        out.put("llmClaimMinEvidenceGroups", NarrativeVocabulary.LLM_MIN_GROUPS);
        out.put("keepMedia", settings.get(SettingKeys.SOCIAL_KEEP_MEDIA, Boolean.class).orElse(false));
        TrackRecord tr = trackRecord();
        Map<String, Object> track = new LinkedHashMap<>();
        track.put("minAssessed", tr.minAssessed());
        track.put("gradeEShare", tr.eShare());
        track.put("gradeDShare", tr.dShare());
        track.put("overrides", overridesView(map(SettingKeys.SOCIAL_SOURCE_RELIABILITY)));
        out.put("trackRecord", track);
        Compare c = compare();
        Map<String, Object> compare = new LinkedHashMap<>();
        compare.put("minGroups", c.minGroups());
        compare.put("defaultWindow", Map.of("before", Durations.format(c.before()), "after",
                Durations.format(c.after())));
        Map<String, Object> families = new LinkedHashMap<>();
        for (String family : CompareRules.bundled().families()) {
            families.put(family, rule(family).toMap());
        }
        for (String family : c.familyWindows().keySet()) {
            families.put(family, rule(family).toMap());
        }
        compare.put("familyWindows", families);
        out.put("compare", compare);
        Gap g = gap();
        Map<String, Object> gap = new LinkedHashMap<>();
        gap.put("minSeverity", g.minSeverity());
        gap.put("percentile", g.percentile());
        gap.put("lookbackDays", g.lookbackDays());
        gap.put("windowHours", g.windowHours());
        out.put("attentionGap", gap);
        Cues q = cues();
        Map<String, Object> cues = new LinkedHashMap<>();
        cues.put("enabled", q.enabled());
        Map<String, Object> triggers = new LinkedHashMap<>();
        triggers.put("situationActivated", q.triggers().situationActivated());
        triggers.put("situationEscalated", q.triggers().situationEscalated());
        triggers.put("findingTypes", q.triggers().findingTypes());
        triggers.put("anomalies", q.triggers().anomalies());
        triggers.put("criticalAlerts", q.triggers().criticalAlerts());
        triggers.put("canonicalFamilies", q.triggers().canonicalFamilies());
        triggers.put("minEvidenceGroups", q.triggers().minEvidenceGroups());
        cues.put("triggers", triggers);
        cues.put("maxPerDay", q.maxPerDay());
        cues.put("maxCostUsdPerCue", q.maxCostUsdPerCue());
        cues.put("platforms", q.platforms());
        cues.put("xaiXSearch", Map.of("enabled", q.xaiEnabled(), "maxResults", q.xaiMaxResults()));
        out.put("cues", cues);
        return out;
    }

    /**
     * Replaces the settings with a {@code SocialSettings} document; members left out return to their defaults.
     *
     * @throws ValidationException 400 {@code VALIDATION_FAILED} with every failing field
     */
    @Transactional
    public Map<String, Object> put(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The body must be a SocialSettings object.",
                    List.of(new FieldError("body", "must be an object")));
        }
        Map<String, Object> doc = json.convertValue(body, MAP);
        List<FieldError> errors = new ArrayList<>();
        for (String key : doc.keySet()) {
            if (!TOP.contains(key)) {
                errors.add(new FieldError(key, "is not a SocialSettings member"));
            }
        }
        List<String> languages = null;
        if (doc.containsKey("languages")) {
            languages = CompareRules.strings(doc.get("languages"));
            for (String l : languages) {
                if (!l.matches("^[a-z]{2,3}$")) {
                    errors.add(new FieldError("languages", "'" + l + "' is not a BCP 47 primary subtag"));
                }
            }
            if (languages.isEmpty()) {
                errors.add(new FieldError("languages", "needs at least one language"));
            }
        }
        Double minRegion = optionalNumber(doc, "minRegionBaseline", 0, Double.MAX_VALUE, errors);
        Map<String, Object> velocity = object(doc, "velocity", Set.of("minRatio", "minUniqueAuthors", "dedupHours"),
                errors);
        range(velocity, "velocity.minRatio", "minRatio", 1, Double.MAX_VALUE, errors);
        range(velocity, "velocity.minUniqueAuthors", "minUniqueAuthors", 1, Double.MAX_VALUE, errors);
        range(velocity, "velocity.dedupHours", "dedupHours", 1, Double.MAX_VALUE, errors);
        Double minPosts = optionalNumber(doc, "llmClaimMinPosts", 1, Double.MAX_VALUE, errors);
        if (doc.get("llmClaimMinEvidenceGroups") != null && CompareRules.number(doc.get("llmClaimMinEvidenceGroups"),
                -1) != NarrativeVocabulary.LLM_MIN_GROUPS) {
            errors.add(new FieldError("llmClaimMinEvidenceGroups", "is fixed at " + NarrativeVocabulary.LLM_MIN_GROUPS
                    + " in this release"));
        }
        if (doc.get("keepMedia") != null && !(doc.get("keepMedia") instanceof Boolean)) {
            errors.add(new FieldError("keepMedia", "must be a boolean"));
        }
        Map<String, Object> track = object(doc, "trackRecord", Set.of("minAssessed", "gradeEShare", "gradeDShare",
                "overrides"), errors);
        range(track, "trackRecord.minAssessed", "minAssessed", 1, Double.MAX_VALUE, errors);
        range(track, "trackRecord.gradeEShare", "gradeEShare", 0, 1, errors);
        range(track, "trackRecord.gradeDShare", "gradeDShare", 0, 1, errors);
        Map<String, String> overrides = overrides(track == null ? null : track.get("overrides"), errors);
        Map<String, Object> compare = object(doc, "compare", Set.of("minGroups", "defaultWindow", "familyWindows"),
                errors);
        range(compare, "compare.minGroups", "minGroups", 1, Double.MAX_VALUE, errors);
        if (compare != null) {
            if (compare.get("defaultWindow") instanceof Map<?, ?> w) {
                durations(w, "compare.defaultWindow", Set.of("before", "after"), errors);
            } else if (compare.get("defaultWindow") != null) {
                errors.add(new FieldError("compare.defaultWindow", "must be an object"));
            }
            if (compare.get("familyWindows") instanceof Map<?, ?> f) {
                for (Map.Entry<?, ?> e : f.entrySet()) {
                    String field = "compare.familyWindows." + e.getKey();
                    if (!(e.getValue() instanceof Map<?, ?> w)) {
                        errors.add(new FieldError(field, "must be an object"));
                        continue;
                    }
                    for (Object k : w.keySet()) {
                        if (!Set.of("before", "after", "toleranceKm", "publicationLag").contains(String.valueOf(k))) {
                            errors.add(new FieldError(field + "." + k, "is not a family window member"));
                        }
                    }
                    durations(w, field, Set.of("before", "after", "publicationLag"), errors);
                    if (w.get("toleranceKm") != null && CompareRules.number(w.get("toleranceKm"), -1) < 0) {
                        errors.add(new FieldError(field + ".toleranceKm", "must be a number ≥ 0"));
                    }
                }
            } else if (compare.get("familyWindows") != null) {
                errors.add(new FieldError("compare.familyWindows", "must be an object"));
            }
        }
        Map<String, Object> gap = object(doc, "attentionGap", Set.of("minSeverity", "percentile", "lookbackDays",
                "windowHours"), errors);
        if (gap != null && gap.get("minSeverity") != null
                && !List.of("notable", "significant").contains(gap.get("minSeverity").toString())) {
            errors.add(new FieldError("attentionGap.minSeverity", "must be notable or significant"));
        }
        range(gap, "attentionGap.percentile", "percentile", 0, 50, errors);
        range(gap, "attentionGap.lookbackDays", "lookbackDays", 30, Double.MAX_VALUE, errors);
        range(gap, "attentionGap.windowHours", "windowHours", 1, Double.MAX_VALUE, errors);
        Map<String, Object> cues = object(doc, "cues", Set.of("enabled", "triggers", "maxPerDay", "maxCostUsdPerCue",
                "platforms", "xaiXSearch"), errors);
        range(cues, "cues.maxPerDay", "maxPerDay", 0, Double.MAX_VALUE, errors);
        range(cues, "cues.maxCostUsdPerCue", "maxCostUsdPerCue", 0, Double.MAX_VALUE, errors);
        if (cues != null) {
            for (String p : CompareRules.strings(cues.get("platforms"))) {
                if (!CorrelationVocabulary.CUE_ADAPTERS.contains(p)) {
                    errors.add(new FieldError("cues.platforms", "'" + p + "' is not a search-capable adapter; one of "
                            + CorrelationVocabulary.CUE_ADAPTERS));
                }
            }
            if (cues.get("triggers") != null && !(cues.get("triggers") instanceof Map<?, ?>)) {
                errors.add(new FieldError("cues.triggers", "must be an object"));
            }
            if (cues.get("triggers") instanceof Map<?, ?> t) {
                for (Object k : t.keySet()) {
                    if (!Set.of("situationActivated", "situationEscalated", "findingTypes", "anomalies",
                            "criticalAlerts", "canonicalFamilies", "minEvidenceGroups").contains(String.valueOf(k))) {
                        errors.add(new FieldError("cues.triggers." + k, "is not a trigger member"));
                    }
                }
                if (t.get("minEvidenceGroups") != null && CompareRules.number(t.get("minEvidenceGroups"), 0) < 1) {
                    errors.add(new FieldError("cues.triggers.minEvidenceGroups", "must be ≥ 1"));
                }
            }
            if (cues.get("xaiXSearch") instanceof Map<?, ?> x && x.get("maxResults") != null
                    && CompareRules.number(x.get("maxResults"), 0) < 1) {
                errors.add(new FieldError("cues.xaiXSearch.maxResults", "must be ≥ 1"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "The social settings are not valid.", errors);
        }
        store(SettingKeys.SOCIAL_LANGUAGES, languages);
        store(SettingKeys.SOCIAL_MIN_REGION_BASELINE, minRegion);
        store(SettingKeys.SOCIAL_VELOCITY, velocity);
        store(SettingKeys.SOCIAL_LLM_CLAIM_MIN_POSTS, minPosts == null ? null : minPosts.intValue());
        store(SettingKeys.SOCIAL_KEEP_MEDIA, doc.get("keepMedia"));
        Map<String, Object> grading = null;
        if (track != null) {
            grading = new LinkedHashMap<>(track);
            grading.remove("overrides");
        }
        store(SettingKeys.SOCIAL_TRACK_RECORD, grading == null || grading.isEmpty() ? null : grading);
        store(SettingKeys.SOCIAL_SOURCE_RELIABILITY, overrides == null || overrides.isEmpty() ? null : overrides);
        store(SettingKeys.SOCIAL_COMPARE, compare);
        store(SettingKeys.SOCIAL_ATTENTION_GAP, gap);
        store(SettingKeys.SOCIAL_CUES, cues);
        return get();
    }

    /**
     * The {@code trackRecord.overrides} view of the stored {@code social.sourceReliability} map: keys
     * {@code account:{platform}:{authorKey}} and {@code channel:{platform}:{channel}}.
     */
    static List<Map<String, Object>> overridesView(Map<?, ?> stored) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<?, ?> e : stored.entrySet()) {
            String key = String.valueOf(e.getKey());
            String[] parts = key.split(":", 3);
            if (parts.length < 3) {
                continue;
            }
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("platform", parts[1]);
            o.put("account", "account".equals(parts[0]) ? parts[2] : null);
            o.put("channel", "channel".equals(parts[0]) ? parts[2] : null);
            o.put("reliability", String.valueOf(e.getValue()));
            o.put("note", null);
            out.add(o);
        }
        return out;
    }

    /** Parses {@code trackRecord.overrides} into the {@code social.sourceReliability} map. */
    static Map<String, String> overrides(Object value, List<FieldError> errors) {
        if (value == null) {
            return null;
        }
        Map<String, String> out = new LinkedHashMap<>();
        if (!(value instanceof List<?> list)) {
            errors.add(new FieldError("trackRecord.overrides", "must be an array"));
            return out;
        }
        int i = 0;
        for (Object o : list) {
            String field = "trackRecord.overrides[" + i++ + "]";
            if (!(o instanceof Map<?, ?> m)) {
                errors.add(new FieldError(field, "must be an object"));
                continue;
            }
            String platform = m.get("platform") == null ? null : m.get("platform").toString();
            String reliability = m.get("reliability") == null ? null
                    : m.get("reliability").toString().toUpperCase(Locale.ROOT);
            String account = m.get("account") == null ? null : m.get("account").toString().strip();
            String channel = m.get("channel") == null ? null : m.get("channel").toString().strip();
            if (platform == null || !platform.matches("^[a-z0-9][a-z0-9-]{0,31}$")) {
                errors.add(new FieldError(field + ".platform", "is required (a network id)"));
            }
            if (reliability == null || !reliability.matches("^[A-F]$")) {
                errors.add(new FieldError(field + ".reliability", "must be one of A to F"));
            }
            if ((account == null || account.isEmpty()) == (channel == null || channel.isEmpty())) {
                errors.add(new FieldError(field, "give exactly one of account (author key) or channel"));
                continue;
            }
            if (platform != null && reliability != null) {
                out.put(account != null && !account.isEmpty() ? "account:" + platform + ":" + account
                        : "channel:" + platform + ":" + channel, reliability);
            }
        }
        return out;
    }

    private void store(String key, Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
    }

    private Map<?, ?> map(String key) {
        try {
            return settings.get(key, Map.class).orElse(Map.of());
        } catch (RuntimeException e) {
            return Map.of();
        }
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

    private static Double optionalNumber(Map<String, Object> doc, String key, double min, double max,
            List<FieldError> errors) {
        Object v = doc.get(key);
        if (v == null) {
            return null;
        }
        if (!(v instanceof Number n) || n.doubleValue() < min || n.doubleValue() > max) {
            errors.add(new FieldError(key, "must be a number ≥ " + min));
            return null;
        }
        return n.doubleValue();
    }

    private static void durations(Map<?, ?> m, String field, Set<String> keys, List<FieldError> errors) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v == null) {
                continue;
            }
            try {
                Durations.parse(v.toString());
            } catch (RuntimeException e) {
                errors.add(new FieldError(field + "." + k, "must be an ISO-8601 or shorthand duration"));
            }
        }
    }

    private static boolean bool(Object v, boolean fallback) {
        return v instanceof Boolean b ? b : fallback;
    }
}
