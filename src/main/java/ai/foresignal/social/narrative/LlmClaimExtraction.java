package ai.foresignal.social.narrative;

import ai.foresignal.ask.AskPrompts;
import ai.foresignal.llm.LlmBudgetExceededException;
import ai.foresignal.llm.LlmService;
import ai.foresignal.llm.PromptTemplates;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.social.TextFolding;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Optional LLM claim extraction for large narratives (prompt 136, 026 role {@code claim-extraction}). Off without a
 * chat provider; budgeted per UTC day ({@code social.llmClaimsPerDay}, 20) on top of the provider's own token budget.
 *
 * <p>A narrative qualifies with at least {@code social.llmClaimMinPosts} (50) posts or
 * {@value NarrativeVocabulary#LLM_MIN_GROUPS} evidence groups, and again once it has doubled since the last run. The
 * versioned template {@code claim-extraction-system@1.0.0} receives the {@value NarrativeVocabulary#LLM_TEXTS} most
 * central member texts as a data block ({@code {"tool": …, "data": …}}, never instructions; sources the provider may not
 * receive are left out) and answers claim tuples in a JSON schema. Every tuple is validated: types outside the lexicon
 * are dropped, polarity defaults to asserted, places are resolved through the gazetteer (an unknown place is kept as a
 * name without geometry), entities through the entity index. The model never sets a status or a count: support and
 * evidence groups are the members whose deterministic claims share the type. Claims are stored with
 * {@code extraction = llm} and a manifest naming the template.
 */
@Component
public class LlmClaimExtraction {

    private static final Logger log = LoggerFactory.getLogger(LlmClaimExtraction.class);

    /** The component of LLM extraction manifests. */
    public static final String COMPONENT = "ClaimExtractorLlm";

    /** Its version. */
    public static final String VERSION = "1.0.0";

    /** The output cap of one call. */
    static final int MAX_TOKENS = 2_000;

    /** The longest post text sent. */
    static final int MAX_TEXT = 1_000;

    /** What a run gave. */
    public enum Outcome {
        /** The narrative does not qualify (size, groups, or no growth since the last run). */
        NOT_ELIGIBLE,
        /** No chat provider is configured. */
        DISABLED,
        /** The daily cap or the provider budget refused the call. */
        BUDGET_REFUSED,
        /** The model refused, failed or answered nothing usable. */
        FAILED,
        /** Claims were stored. */
        STORED
    }

    /**
     * One validated claim tuple.
     *
     * @param type the lexicon type
     * @param polarity the polarity
     * @param hedged hedged
     * @param place the place, or null
     * @param actor the actor, or null
     * @param entityRefs resolved entities
     */
    public record Tuple(String type, String polarity, boolean hedged, ClaimExtractor.Place place, String actor,
            List<String> entityRefs) {

        /** The claim key: type, polarity and place. */
        public String key() {
            return type + "|" + polarity + "|" + (place == null ? "" : place.key());
        }
    }

    private final NarrativeStore store;
    private final NarrativeService service;
    private final ObjectProvider<LlmService> llm;
    private final ObjectProvider<EgressPolicy> egress;
    private final PlaceIndex places;
    private final EntityIndex entities;
    private final AppSettingService settings;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the extraction. */
    public LlmClaimExtraction(NarrativeStore store, NarrativeService service, ObjectProvider<LlmService> llm,
            ObjectProvider<EgressPolicy> egress, PlaceIndex places, EntityIndex entities, AppSettingService settings,
            JsonMapper json, Clock clock) {
        this.store = store;
        this.service = service;
        this.llm = llm;
        this.egress = egress;
        this.places = places;
        this.entities = entities;
        this.settings = settings;
        this.json = json;
        this.clock = clock;
    }

    /** {@code social.llmClaimMinPosts}. */
    int minPosts() {
        return settings.get(SettingKeys.SOCIAL_LLM_CLAIM_MIN_POSTS, Number.class).map(Number::intValue)
                .orElse(NarrativeVocabulary.DEFAULT_LLM_MIN_POSTS);
    }

    /** {@code social.llmClaimsPerDay}. */
    int perDay() {
        return settings.get(SettingKeys.SOCIAL_LLM_CLAIMS_PER_DAY, Number.class).map(Number::intValue)
                .orElse(NarrativeVocabulary.DEFAULT_LLM_PER_DAY);
    }

    /** Whether a narrative qualifies now. */
    boolean eligible(NarrativeStore.Narrative n) {
        boolean big = n.postCount() >= minPosts() || n.evidenceGroupCount() >= NarrativeVocabulary.LLM_MIN_GROUPS;
        boolean grown = n.llmExtractedAt() == null || n.postCount() >= 2 * Math.max(1, n.llmPostCount());
        return big && grown && n.mergedInto() == null;
    }

    /** Runs over every qualifying narrative active in the last day, within the daily cap; the narratives stored. */
    public int runDue() {
        int n = 0;
        for (NarrativeStore.Narrative narrative : store.activeSince(clock.instant().minus(1, ChronoUnit.DAYS), 1_000)) {
            if (!eligible(narrative)) {
                continue;
            }
            Outcome o = run(narrative.id());
            if (o == Outcome.BUDGET_REFUSED || o == Outcome.DISABLED) {
                break;
            }
            if (o == Outcome.STORED) {
                n++;
            }
        }
        return n;
    }

    /** Runs the extraction over one narrative. */
    public Outcome run(String narrativeId) {
        NarrativeStore.Narrative n = store.narrative(narrativeId).orElse(null);
        if (n == null || !eligible(n)) {
            return Outcome.NOT_ELIGIBLE;
        }
        LlmService service = llm.getIfAvailable();
        if (service == null || !service.chatConfigured()) {
            return Outcome.DISABLED;
        }
        Instant now = clock.instant();
        if (store.llmRunsSince(now.truncatedTo(ChronoUnit.DAYS)) >= perDay()) {
            log.info("LLM claim extraction of {} skipped: the daily cap of {} is reached", narrativeId, perDay());
            return Outcome.BUDGET_REFUSED;
        }
        try {
            LlmClient client = service.chatClient().orElse(null);
            if (client == null) {
                return Outcome.DISABLED;
            }
            EgressPolicy policy = egress.getIfAvailable();
            Set<String> excluded = policy == null ? Set.of()
                    : policy.llmExcludedSources(service.config(client.providerId()).destination());
            List<NarrativeStore.Member> members = central(store.members(narrativeId), excluded);
            List<String> texts = new ArrayList<>();
            for (NarrativeStore.Member m : members) {
                String t = this.service.text(m);
                if (t != null && !t.isBlank()) {
                    texts.add(NarrativeStore.clip(t, MAX_TEXT));
                }
            }
            if (texts.isEmpty()) {
                return Outcome.NOT_ELIGIBLE;
            }
            ChatRequest request = ChatRequest.simple(Purpose.ANALYST,
                    PromptTemplates.text(PromptTemplates.CLAIM_EXTRACTION, PromptTemplates.V1),
                    AskPrompts.wrap("narrative_posts", packet(narrativeId, texts), json), MAX_TOKENS);
            ChatResponse response = client.chat(request);
            store.markLlm(narrativeId, n.postCount(), now);
            if (response.stopReason() == StopReason.REFUSAL) {
                return Outcome.FAILED;
            }
            List<Tuple> tuples = parse(response.content(), ClaimLexicon.bundled(), places, entities, json);
            String manifestId = this.service.manifest(COMPONENT, VERSION, Map.of("texts", texts.size(), "lexicon",
                    ClaimLexicon.bundled().version()), narrativeId, model(response));
            store(n, tuples, manifestId, now);
            return Outcome.STORED;
        } catch (LlmBudgetExceededException e) {
            log.info("LLM claim extraction of {} refused by the budget: {}", narrativeId, e.getMessage());
            return Outcome.BUDGET_REFUSED;
        } catch (RuntimeException e) {
            log.warn("LLM claim extraction of {} failed: {}", narrativeId, e.toString());
            return Outcome.FAILED;
        }
    }

    private Map<String, Object> model(ChatResponse response) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("model", response.model());
        out.put("role", NarrativeVocabulary.LLM_ROLE);
        out.put("templates", List.of(PromptTemplates.reference(PromptTemplates.CLAIM_EXTRACTION, PromptTemplates.V1)));
        return out;
    }

    /** The most central members whose sources the provider may receive. */
    static List<NarrativeStore.Member> central(List<NarrativeStore.Member> members, Set<String> excluded) {
        List<NarrativeStore.Member> out = new ArrayList<>();
        for (NarrativeStore.Member m : members) {
            if (!excluded.contains(m.sourceId()) && m.repostOf() == null) {
                out.add(m);
            }
        }
        out.sort(Comparator.comparing((NarrativeStore.Member m) -> m.centrality() == null ? -1.0 : m.centrality())
                .reversed().thenComparing(NarrativeStore.Member::createdAt));
        return out.size() > NarrativeVocabulary.LLM_TEXTS ? out.subList(0, NarrativeVocabulary.LLM_TEXTS) : out;
    }

    /** The data block: numbered post texts. */
    JsonNode packet(String narrativeId, List<String> texts) {
        ObjectNode data = json.createObjectNode();
        data.put("narrativeId", narrativeId);
        ArrayNode posts = data.putArray("posts");
        for (int i = 0; i < texts.size(); i++) {
            ObjectNode p = posts.addObject();
            p.put("n", i + 1);
            p.put("text", texts.get(i));
        }
        return data;
    }

    /**
     * Parses and validates the model's answer.
     *
     * @param content the answer text
     * @return the valid tuples (deduplicated by key)
     */
    public static List<Tuple> parse(String content, ClaimLexicon lexicon, PlaceIndex places, EntityIndex entities,
            JsonMapper json) {
        List<Tuple> out = new ArrayList<>();
        if (content == null) {
            return out;
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return out;
        }
        JsonNode root;
        try {
            root = json.readTree(content.substring(start, end + 1));
        } catch (RuntimeException e) {
            return out;
        }
        JsonNode claims = root.get("claims");
        if (claims == null || !claims.isArray()) {
            return out;
        }
        Set<String> keys = new HashSet<>();
        for (JsonNode c : claims) {
            if (out.size() >= 10) {
                break;
            }
            String type = text(c.get("type"));
            if (type == null || lexicon.type(type) == null) {
                continue;
            }
            String polarity = text(c.get("polarity"));
            if (polarity == null || !List.of("asserted", "denied", "questioned").contains(polarity)) {
                polarity = "asserted";
            }
            boolean hedged = c.get("hedged") != null && c.get("hedged").asBoolean(false);
            String placeName = text(c.get("place"));
            ClaimExtractor.Place place = placeName == null ? null : resolvePlace(placeName, places);
            String actor = text(c.get("actor"));
            Set<String> refs = new LinkedHashSet<>();
            JsonNode names = c.get("entities");
            if (names != null && names.isArray()) {
                for (JsonNode name : names) {
                    String t = text(name);
                    if (t != null) {
                        for (EntityIndex.Hit h : entities.find(TextFolding.fold(t))) {
                            refs.addAll(h.refs());
                        }
                    }
                }
            }
            Tuple tuple = new Tuple(type, polarity, hedged, place, actor == null ? null : NarrativeStore.clip(actor,
                    200), new ArrayList<>(refs));
            if (keys.add(tuple.key())) {
                out.add(tuple);
            }
        }
        return out;
    }

    /** A place name resolved through the index when the whole name matches; otherwise kept without geometry. */
    static ClaimExtractor.Place resolvePlace(String name, PlaceIndex places) {
        String folded = TextFolding.fold(name);
        for (PlaceIndex.NameHit h : places.find(folded)) {
            if (h.start() == 0 && h.end() == folded.length() && !h.candidates().isEmpty()) {
                return ClaimExtractor.choose(h.candidates());
            }
        }
        return new ClaimExtractor.Place(null, NarrativeStore.clip(name.strip(), 200), null, null, "locality", 0.3,
                null);
    }

    private void store(NarrativeStore.Narrative n, List<Tuple> tuples, String manifestId, Instant now) {
        List<NarrativeStore.Member> members = store.members(n.id());
        Map<?, ?> overrides = settings.get(SettingKeys.SOCIAL_SOURCE_RELIABILITY, Map.class).orElse(Map.of());
        List<String> keys = new ArrayList<>();
        for (Tuple t : tuples) {
            Function<NarrativeStore.Member, Boolean> supports = m -> {
                for (Object o : m.claims()) {
                    if (o instanceof Map<?, ?> c && t.type().equals(c.get("type"))) {
                        return true;
                    }
                }
                return false;
            };
            int support = 0;
            Set<String> groups = new HashSet<>();
            NarrativeStore.Member first = null;
            for (NarrativeStore.Member m : members) {
                if (supports.apply(m)) {
                    support++;
                    groups.add(m.evidenceGroupId() == null ? m.id() : m.evidenceGroupId());
                    if (first == null || m.createdAt().isBefore(first.createdAt())) {
                        first = m;
                    }
                }
            }
            String sourceKey = first == null ? null : first.sourceKey();
            int[] record = store.trackRecord(sourceKey);
            String reliability = SocialEvidenceGrades.reliability(record[0], record[1],
                    SocialEvidenceGrades.override(overrides, sourceKey));
            ClaimExtractor.Place place = t.place();
            NarrativeStore.Claim claim = new NarrativeStore.Claim(Ulids.next(now), n.id(), t.key(), t.type(),
                    ClaimLexicon.bundled().type(t.type()).signalFamily(), t.polarity(), t.hedged(),
                    place == null ? null : place.toMap(), place == null ? null : place.lon(),
                    place == null ? null : place.lat(), place == null ? null : place.regionCode(), t.entityRefs(), null,
                    t.actor(), support, groups.size(), first == null ? n.firstObservedAt() : first.createdAt(), "llm",
                    manifestId, 0.5, NarrativeVocabulary.UNASSESSED, null, sourceKey, reliability,
                    SocialEvidenceGrades.credibility(NarrativeVocabulary.UNASSESSED), n.tlp(), now, now);
            keys.add(t.key());
            Map.Entry<String, Boolean> result = store.upsertClaim(claim);
            if (result.getValue()) {
                store.claim(result.getKey()).ifPresent(c -> serviceEvents(c, now));
            }
        }
        store.deleteClaimsExcept(n.id(), "llm", keys);
    }

    private void serviceEvents(NarrativeStore.Claim c, Instant now) {
        service.emitClaim(c, now);
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String s = node.asString("").strip();
        return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
    }
}
