package ai.foresignal.social.narrative;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.chronicle.network.MinHashSketch;
import ai.foresignal.evidence.EvidenceGrouper;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.evidence.QualityFlags;
import ai.foresignal.evidence.RecordRef;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import ai.foresignal.news.NewsStoryStore;
import ai.foresignal.news.TitleEmbeddings;
import ai.foresignal.news.TitleTokens;
import ai.foresignal.news.Vectors;
import ai.foresignal.security.Ulids;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.social.SocialEvents;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Narrative maintenance (prompt 136): the full refresh of a narrative (evidence groups by origin, title, scopes,
 * platform statistics, centroid, velocity and state, linked news stories and situations, deterministic claims with
 * their Admiralty grades, and the propagation edges), merges and splits with history (118 rules), the hourly state
 * pass, and the {@code social} SSE events.
 *
 * <p>Nothing here decides whether anything is true: status comes from 137, counts are deterministic, and the earliest
 * post per platform is the earliest observed, never the originator.
 */
@Component
public class NarrativeService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeService.class);

    /** Members whose centrality is computed (the latest). */
    static final int CENTRALITY_SAMPLE = 200;

    /** The narrative size up to which a new member refreshes it at once; larger ones refresh from the minute job. */
    public static final int INLINE_REFRESH_POSTS = 100;

    /** The token Jaccard that links a news story by text. */
    static final double STORY_JACCARD = 0.5;

    /** The most linked stories. */
    static final int MAX_STORIES = 20;

    /** The most cited agency records looked up. */
    static final int MAX_OFFICIALS = 50;

    /** The most articles of linked stories in a propagation graph. */
    static final int MAX_ARTICLES = 200;

    private final NarrativeStore store;
    private final EvidenceStore evidence;
    private final ObjectProvider<NewsStoryStore> stories;
    private final ObjectProvider<TitleEmbeddings> embeddings;
    private final ObjectProvider<ManifestService> manifests;
    private final SocialEvents events;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the service. */
    public NarrativeService(NarrativeStore store, EvidenceStore evidence, ObjectProvider<NewsStoryStore> stories,
            ObjectProvider<TitleEmbeddings> embeddings, ObjectProvider<ManifestService> manifests, SocialEvents events,
            AppSettingService settings, Clock clock) {
        this.store = store;
        this.evidence = evidence;
        this.stories = stories;
        this.embeddings = embeddings;
        this.manifests = manifests;
        this.events = events;
        this.settings = settings;
        this.clock = clock;
    }

    /** The {@code DetectorRef} of the clusterer. */
    public static Map<String, Object> clustererRef() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", NarrativeVocabulary.CLUSTERER);
        out.put("version", NarrativeVocabulary.CLUSTERER_VERSION);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("minCosine", 0.85);
        params.put("minJaccard", 0.6);
        params.put("windowHours", 72);
        params.put("linkReasons", NarrativeVocabulary.LINK_REASONS);
        out.put("params", params);
        return out;
    }

    /** The caveat every narrative carries. */
    public static Map<String, Object> reportingCaveat() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", "SOCIAL_IS_REPORTING");
        out.put("text", "A narrative is what people are saying, not what happened; its earliest posts are the earliest"
                + " Foresignal observed, never its originators, and its status comes only from comparison with"
                + " independent evidence.");
        return out;
    }

    /** The stored form of a post claim (member {@code claims}). */
    public static Map<String, Object> postClaimMap(ClaimExtractor.PostClaim c) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("key", c.key());
        out.put("type", c.type());
        out.put("signalFamily", c.signalFamily());
        out.put("polarity", c.polarity());
        out.put("hedged", c.hedged());
        out.put("place", c.place() == null ? null : c.place().toMap());
        out.put("entityRefs", c.entityRefs());
        out.put("eventTime", c.eventTime() == null ? null : c.eventTime().toMap());
        out.put("actor", c.actor());
        out.put("confidence", c.confidence());
        return out;
    }

    /** Records the manifest of a component run over a narrative, or null when manifests are unavailable. */
    String manifest(String component, String version, Map<String, Object> config, String narrativeId,
            Map<String, Object> model) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            ManifestRequest request = ManifestRequest.of(component, version).config(config)
                    .inputs(List.of("narrative:" + narrativeId));
            return service.record(model == null ? request : request.model(model));
        } catch (RuntimeException e) {
            log.debug("Could not record the {} manifest of {}: {}", component, narrativeId, e.toString());
            return null;
        }
    }

    // ------------------------------------------------------------------------- refresh

    /** Refreshes up to {@code limit} dirty narratives; the number refreshed. */
    public int refreshDirty(int limit) {
        int n = 0;
        for (String id : store.dirty(limit)) {
            try {
                refresh(id, false);
                n++;
            } catch (RuntimeException e) {
                log.warn("Narrative refresh of {} failed: {}", id, e.toString());
            }
        }
        return n;
    }

    /**
     * Recomputes everything derived about a narrative.
     *
     * @param id the narrative
     * @param created true right after it was created ({@code narrative.emerged} instead of {@code narrative.updated})
     */
    @Transactional
    public void refresh(String id, boolean created) {
        NarrativeStore.Narrative before = store.narrative(id).orElse(null);
        if (before == null || before.mergedInto() != null) {
            return;
        }
        List<NarrativeStore.Member> members = store.members(id);
        if (members.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        Set<String> allFingerprints = new LinkedHashSet<>();
        for (NarrativeStore.Member m : members) {
            allFingerprints.addAll(m.fingerprints());
        }
        Map<String, String> firstPosts = store.firstPosts(allFingerprints);
        List<NarrativeGrouper.Member> grouperMembers = new ArrayList<>();
        Map<String, NarrativeStore.Member> byKey = new HashMap<>();
        for (NarrativeStore.Member m : members) {
            String key = m.linkId() != null ? m.linkId() : m.id();
            byKey.put(key, m);
            grouperMembers.add(new NarrativeGrouper.Member(key, m.postKey(), m.url(), m.originatorId(), m.repostOf(),
                    m.quoteOf(), m.links(), m.fingerprints(), m.createdAt(), m.seq()));
        }
        List<NarrativeGrouper.Group> groups = NarrativeGrouper.group(grouperMembers, firstPosts);
        List<EvidenceGrouper.Group> stored = new ArrayList<>();
        for (NarrativeGrouper.Group g : groups) {
            List<String> links = new ArrayList<>();
            for (String k : g.members()) {
                if (byKey.get(k) != null && byKey.get(k).linkId() != null) {
                    links.add(k);
                }
            }
            if (!links.isEmpty()) {
                stored.add(new EvidenceGrouper.Group(g.root(), links, g.firstSeq()));
            }
        }
        Map<String, String> groupIds = evidence.writeGroups(id, stored, now);
        Map<String, Double> centrality = centrality(members);
        Map<String, String> groupOfMember = new HashMap<>();
        for (NarrativeGrouper.Group g : groups) {
            for (String k : g.members()) {
                NarrativeStore.Member m = byKey.get(k);
                String groupId = groupIds.get(g.root());
                groupOfMember.put(m.id(), groupId != null ? groupId : g.root());
                if (!java.util.Objects.equals(m.evidenceGroupKey(), g.root())
                        || !java.util.Objects.equals(m.evidenceGroupId(), groupId)
                        || !java.util.Objects.equals(m.centrality(), centrality.get(m.id()))) {
                    store.updateGroup(m.id(), g.root(), groupId, centrality.get(m.id()));
                }
            }
        }
        List<NarrativeStore.Member> current = store.members(id);
        Set<String> sources = new TreeSet<>();
        Set<String> topics = new LinkedHashSet<>();
        Set<String> places = new LinkedHashSet<>();
        Set<String> entities = new LinkedHashSet<>();
        Set<String> regions = new LinkedHashSet<>();
        Set<String> languages = new LinkedHashSet<>();
        Set<String> authors = new HashSet<>();
        Set<String> links = new LinkedHashSet<>();
        Instant first = null;
        Instant last = null;
        for (NarrativeStore.Member m : current) {
            sources.add(m.sourceId());
            topics.addAll(m.topicIds());
            places.addAll(m.placeRefs());
            entities.addAll(m.entityRefs());
            regions.addAll(m.regionCodes());
            if (m.language() != null) {
                languages.add(m.language());
            }
            if (m.authorKey() != null) {
                authors.add(m.platform() + ":" + m.authorKey());
            }
            links.addAll(m.links());
            first = first == null || m.createdAt().isBefore(first) ? m.createdAt() : first;
            last = last == null || m.createdAt().isAfter(last) ? m.createdAt() : last;
        }
        List<String> flags = new ArrayList<>();
        for (NarrativeGrouper.Group g : groups) {
            if (g.size() > 1) {
                flags.add(QualityFlags.ECHO);
                break;
            }
        }
        if (groups.size() == 1 && current.size() > 1) {
            flags.add(QualityFlags.SINGLE_ORIGINATOR);
        }
        evidence.updateSummary(id, current.size(), groups.size(), flags, new ArrayList<>(sources), now);
        NarrativeStore.Member central = central(current, centrality);
        List<NarrativeStore.Claim> claims = refreshClaims(id, current, groupOfMember, before.tlp(), now);
        List<NarrativeStore.Claim> deterministic = claims.stream()
                .filter(c -> "deterministic".equals(c.extraction())).toList();
        String title = before.title();
        String titleSource = before.titleSource();
        if (deterministic.size() >= 2) {
            title = claimTitle(deterministic);
            titleSource = "claims";
        } else if (central != null) {
            String text = text(central);
            if (text != null && !text.isBlank()) {
                title = memberTitle(text);
                titleSource = "member";
            }
        }
        float[] centroid = null;
        String model = null;
        int centroidN = 0;
        TitleEmbeddings te = embeddings.getIfAvailable();
        if (te != null) {
            List<String> keys = new ArrayList<>();
            for (NarrativeStore.Member m : current.subList(Math.max(0, current.size() - CENTRALITY_SAMPLE),
                    current.size())) {
                keys.add(m.sourceId() + "/" + m.externalId());
            }
            TitleEmbeddings.TitleVectors vectors = te.vectors(keys);
            List<float[]> list = new ArrayList<>(vectors.vectors().values());
            centroid = Vectors.mean(list);
            model = centroid == null ? null : vectors.model();
            centroidN = centroid == null ? 0 : list.size();
        }
        List<String> storyIds = new ArrayList<>();
        List<String> situationIds = new ArrayList<>();
        linkStories(new ArrayList<>(links), title, new ArrayList<>(topics), new ArrayList<>(regions), first, last,
                storyIds, situationIds);
        Double velocity = velocity(current, first, now);
        String state = state(first, last, velocity, now);
        Set<String> fingerprints = new LinkedHashSet<>();
        for (NarrativeStore.Member m : current) {
            fingerprints.addAll(m.fingerprints());
        }
        NarrativeStore.Narrative after = new NarrativeStore.Narrative(id, title, titleSource,
                central == null ? before.centralPost() : central.id(),
                central == null ? before.centralSignature() : central.signature(), centroid, model, centroidN,
                new ArrayList<>(topics), new ArrayList<>(places), new ArrayList<>(entities), new ArrayList<>(regions),
                new ArrayList<>(languages), platforms(current), first, last, current.size(), authors.size(),
                groups.size(), NarrativeGrouper.originConcentration(groups), velocity, new ArrayList<>(fingerprints),
                storyIds, situationIds, before.status(), state, before.coordination(), before.coordinationFindingId(),
                null, before.tlp(), before.manifestId(), false, before.llmExtractedAt(), before.llmPostCount(),
                before.createdAt(), now);
        store.updateNarrative(after);
        for (String fp : fingerprints) {
            store.addNarrative(fp, id, now);
        }
        try {
            List<PropagationTracer.Article> articles = articles(storyIds);
            List<PropagationTracer.Official> officials = officials(links);
            store.replaceEdges(id, PropagationTracer.edges(current, articles, officials), now);
        } catch (RuntimeException e) {
            log.debug("Propagation edges of {} not rebuilt: {}", id, e.toString());
        }
        if (!java.util.Objects.equals(before.title(), title) && !created) {
            store.log(id, "title-changed", "system", List.of(), before.title(), title, null, now);
        }
        if (!java.util.Objects.equals(before.state(), state) && !created) {
            store.log(id, "state-changed", "system", List.of(), before.state(), state, null, now);
        }
        if (created) {
            events.emit(NarrativeVocabulary.EVENT_EMERGED, narrativeBody(NarrativeVocabulary.EVENT_EMERGED,
                    NarrativeViews.summary(after), List.of(), now), false);
        } else {
            List<String> changed = changed(before, after);
            if (!changed.isEmpty()) {
                events.emit(NarrativeVocabulary.EVENT_UPDATED, narrativeBody(NarrativeVocabulary.EVENT_UPDATED,
                        NarrativeViews.summary(after), changed, now), false);
            }
        }
    }

    /** The {@code NarrativeEvent} body. */
    public static Map<String, Object> narrativeBody(String event, Map<String, Object> summary, List<String> changed,
            Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", event);
        body.put("narrative", summary);
        body.put("changed", changed);
        body.put("at", at.toString());
        return body;
    }

    /** The {@code ClaimExtractedEvent} body. */
    public static Map<String, Object> claimBody(Map<String, Object> claim, Instant at) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", NarrativeVocabulary.EVENT_CLAIM);
        body.put("claim", claim);
        body.put("at", at.toString());
        return body;
    }

    /** Emits {@code claim.extracted} for a new claim. */
    public void emitClaim(NarrativeStore.Claim claim, Instant at) {
        events.emit(NarrativeVocabulary.EVENT_CLAIM, claimBody(NarrativeViews.claim(claim), at), false);
    }

    /** What crossed a band between two versions of a narrative. */
    static List<String> changed(NarrativeStore.Narrative before, NarrativeStore.Narrative after) {
        List<String> out = new ArrayList<>();
        if (!java.util.Objects.equals(before.state(), after.state())) {
            out.add("state");
        }
        if (!java.util.Objects.equals(before.status(), after.status())) {
            out.add("status");
        }
        if (band(before.postCount()) != band(after.postCount())) {
            out.add("postCount");
        }
        if (band(before.evidenceGroupCount()) != band(after.evidenceGroupCount())) {
            out.add("evidenceGroupCount");
        }
        if (velocityBand(before.velocity()) != velocityBand(after.velocity())) {
            out.add("velocity");
        }
        if (!java.util.Objects.equals(before.title(), after.title()) && "claims".equals(after.titleSource())
                && !"claims".equals(before.titleSource())) {
            out.add("title");
        }
        return out;
    }

    /** The count band: 0, 1, 2–3, 4–7, … (powers of two). */
    static int band(int count) {
        return count <= 0 ? 0 : 32 - Integer.numberOfLeadingZeros(count);
    }

    private static int velocityBand(Double v) {
        if (v == null) {
            return -1;
        }
        return v >= NarrativeVocabulary.EMERGING_VELOCITY ? 2 : v < NarrativeVocabulary.FADING_VELOCITY ? 0 : 1;
    }

    /** Centrality: each sampled member's mean signature Jaccard to the other sampled members. */
    static Map<String, Double> centrality(List<NarrativeStore.Member> members) {
        List<NarrativeStore.Member> sample = members.subList(Math.max(0, members.size() - CENTRALITY_SAMPLE),
                members.size());
        Map<String, Double> out = new HashMap<>();
        if (sample.size() == 1) {
            out.put(sample.get(0).id(), 1.0);
            return out;
        }
        for (NarrativeStore.Member a : sample) {
            double sum = 0;
            int n = 0;
            for (NarrativeStore.Member b : sample) {
                if (a == b) {
                    continue;
                }
                sum += TextSignatures.jaccard(a.signature(), b.signature());
                n++;
            }
            out.put(a.id(), n == 0 ? null : Math.round(sum / n * 10_000) / 10_000.0);
        }
        return out;
    }

    /** The most central member (ties: the earliest); reposts without text count last. */
    static NarrativeStore.Member central(List<NarrativeStore.Member> members, Map<String, Double> centrality) {
        NarrativeStore.Member best = null;
        double bestScore = -1;
        for (NarrativeStore.Member m : members) {
            Double c = centrality.get(m.id());
            if (c == null || m.signature() == null || m.signature().isEmpty()) {
                continue;
            }
            if (c > bestScore + 1e-12) {
                bestScore = c;
                best = m;
            }
        }
        return best != null ? best : members.isEmpty() ? null : members.get(0);
    }

    /** The title from a member's text: whitespace collapsed, at most 140 characters. */
    static String memberTitle(String text) {
        String t = text.replaceAll("\\s+", " ").strip();
        if (t.codePointCount(0, t.length()) <= NarrativeVocabulary.TITLE_CHARS) {
            return t;
        }
        int end = t.offsetByCodePoints(0, NarrativeVocabulary.TITLE_CHARS - 1);
        return t.substring(0, end).strip() + "…";
    }

    /** The claim summary title: the three most supported claim types and the top claim's place. */
    static String claimTitle(List<NarrativeStore.Claim> claims) {
        List<NarrativeStore.Claim> sorted = new ArrayList<>(claims);
        sorted.sort(Comparator.comparingInt(NarrativeStore.Claim::supportCount).reversed()
                .thenComparing(NarrativeStore.Claim::firstObservedAt));
        Set<String> types = new LinkedHashSet<>();
        for (NarrativeStore.Claim c : sorted) {
            types.add(c.type().replace('-', ' '));
            if (types.size() == 3) {
                break;
            }
        }
        List<String> list = new ArrayList<>(types);
        String what = list.size() == 1 ? list.get(0) : String.join(", ", list.subList(0, list.size() - 1)) + " and "
                + list.get(list.size() - 1);
        NarrativeStore.Claim top = sorted.get(0);
        String place = top.place() == null || top.place().get("name") == null ? null : top.place().get("name")
                .toString();
        String prefix = "denied".equals(top.polarity()) ? "Denied reports of " : "questioned".equals(top.polarity())
                ? "Questioned reports of " : "Reported ";
        return memberTitle(prefix + what + (place == null ? "" : " in " + place));
    }

    /** The {@code NarrativePlatformStats} of the members. */
    static List<Object> platforms(List<NarrativeStore.Member> members) {
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        Map<String, Set<String>> authors = new HashMap<>();
        List<NarrativeStore.Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing(NarrativeStore.Member::createdAt).thenComparing(NarrativeStore.Member::id));
        for (NarrativeStore.Member m : sorted) {
            Map<String, Object> p = out.get(m.platform());
            if (p == null) {
                p = new LinkedHashMap<>();
                p.put("platform", m.platform());
                p.put("posts", 0);
                p.put("uniqueAuthors", 0);
                p.put("firstObservedAt", m.createdAt().toString());
                p.put("earliestPostRef", m.recordRef());
                out.put(m.platform(), p);
            }
            p.put("posts", (Integer) p.get("posts") + 1);
            if (m.authorKey() != null && authors.computeIfAbsent(m.platform(), k -> new HashSet<>()).add(
                    m.authorKey())) {
                p.put("uniqueAuthors", (Integer) p.get("uniqueAuthors") + 1);
            }
        }
        return new ArrayList<>(out.values());
    }

    /**
     * The narrative's velocity: posts in the last closed hour over the median hourly posts of the 24 hours before it
     * (zero hours included, floored at one); null for a narrative younger than two hours.
     */
    static Double velocity(List<NarrativeStore.Member> members, Instant first, Instant now) {
        Instant hour = now.truncatedTo(ChronoUnit.HOURS);
        Instant lastStart = hour.minus(Duration.ofHours(1));
        if (first == null || first.isAfter(hour.minus(Duration.ofHours(2)))) {
            return null;
        }
        long lastHour = 0;
        Map<Instant, Long> counts = new HashMap<>();
        for (NarrativeStore.Member m : members) {
            Instant h = m.createdAt().truncatedTo(ChronoUnit.HOURS);
            if (h.equals(lastStart)) {
                lastHour++;
            } else if (h.isBefore(lastStart) && !h.isBefore(lastStart.minus(Duration.ofHours(24)))) {
                counts.merge(h, 1L, Long::sum);
            }
        }
        Instant from = first.truncatedTo(ChronoUnit.HOURS);
        List<Double> hourly = new ArrayList<>();
        for (Instant h = lastStart.minus(Duration.ofHours(24)); h.isBefore(lastStart); h = h.plus(Duration.ofHours(1))) {
            if (!h.isBefore(from)) {
                hourly.add((double) counts.getOrDefault(h, 0L));
            }
        }
        if (hourly.isEmpty()) {
            return null;
        }
        double median = ai.foresignal.analytics.math.RobustStats.median(hourly.stream().mapToDouble(d -> d).toArray());
        return Math.round(lastHour / Math.max(1.0, median) * 100) / 100.0;
    }

    /** The state by silence, age and velocity. */
    static String state(Instant first, Instant last, Double velocity, Instant now) {
        if (last == null || first == null) {
            return NarrativeVocabulary.EMERGING;
        }
        Duration silence = Duration.between(last, now);
        if (silence.compareTo(NarrativeVocabulary.DORMANT_AFTER) >= 0) {
            return NarrativeVocabulary.DORMANT;
        }
        if (silence.compareTo(NarrativeVocabulary.FADING_AFTER) >= 0) {
            return NarrativeVocabulary.FADING;
        }
        if (Duration.between(first, now).compareTo(NarrativeVocabulary.EMERGING_AGE) < 0
                || (velocity != null && velocity >= NarrativeVocabulary.EMERGING_VELOCITY)) {
            return NarrativeVocabulary.EMERGING;
        }
        if (velocity != null && velocity < NarrativeVocabulary.FADING_VELOCITY) {
            return NarrativeVocabulary.FADING;
        }
        return NarrativeVocabulary.ACTIVE;
    }

    /** Recomputes the velocity and state of every live narrative (hourly); the number changed. */
    @Transactional
    public int refreshStates() {
        Instant now = clock.instant();
        int changed = 0;
        for (NarrativeStore.Narrative n : store.live(10_000)) {
            List<NarrativeStore.Member> recent = store.membersBetween(n.id(), now.minus(Duration.ofHours(26)), now);
            Double velocity = velocity(recent, n.firstObservedAt(), now);
            String state = state(n.firstObservedAt(), n.lastActivityAt(), velocity, now);
            if (!state.equals(n.state()) || !java.util.Objects.equals(velocity, n.velocity())) {
                store.updateState(n.id(), state, velocity, now);
                if (!state.equals(n.state())) {
                    store.log(n.id(), "state-changed", "system", List.of(), n.state(), state, null, now);
                    NarrativeStore.Narrative after = store.narrative(n.id()).orElse(n);
                    events.emit(NarrativeVocabulary.EVENT_UPDATED, narrativeBody(NarrativeVocabulary.EVENT_UPDATED,
                            NarrativeViews.summary(after), List.of("state"), now), false);
                    changed++;
                }
            }
        }
        return changed;
    }

    /** The stored text of a member's post, or null when it expired or was deleted. */
    String text(NarrativeStore.Member m) {
        List<String> rows = store.jdbc().queryForList("SELECT payload->>'text' FROM record_head WHERE source_id = ? AND"
                + " external_id = ? AND NOT tombstone", String.class, m.sourceId(), m.externalId());
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ------------------------------------------------------------------------- claims

    /** Aggregates the members' post claims into the narrative's deterministic claims; returns every claim. */
    List<NarrativeStore.Claim> refreshClaims(String narrativeId, List<NarrativeStore.Member> members,
            Map<String, String> groupOfMember, String tlp, Instant now) {
        Map<String, Agg> aggs = new LinkedHashMap<>();
        List<NarrativeStore.Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing(NarrativeStore.Member::createdAt).thenComparing(NarrativeStore.Member::id));
        for (NarrativeStore.Member m : sorted) {
            for (Object o : m.claims()) {
                if (!(o instanceof Map<?, ?> c) || c.get("key") == null || c.get("type") == null) {
                    continue;
                }
                Agg agg = aggs.computeIfAbsent(c.get("key").toString(), k -> new Agg(c, m));
                agg.add(c, m, groupOfMember.getOrDefault(m.id(), m.id()));
            }
        }
        Map<?, ?> overrides = settings.get(SettingKeys.SOCIAL_SOURCE_RELIABILITY, Map.class).orElse(Map.of());
        String manifestId = aggs.isEmpty() ? null : manifest(NarrativeVocabulary.EXTRACTOR,
                NarrativeVocabulary.EXTRACTOR_VERSION, Map.of("lexicon", ClaimLexicon.bundled().version()),
                narrativeId, null);
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Agg> e : aggs.entrySet()) {
            Agg a = e.getValue();
            keys.add(e.getKey());
            int[] record = store.trackRecord(a.sourceKey);
            String reliability = SocialEvidenceGrades.reliability(record[0], record[1],
                    SocialEvidenceGrades.override(overrides, a.sourceKey));
            NarrativeStore.Claim claim = new NarrativeStore.Claim(Ulids.next(now), narrativeId, e.getKey(), a.type,
                    a.signalFamily, a.polarity, a.hedged, a.place, a.lon(), a.lat(), a.regionCode(),
                    new ArrayList<>(a.entities), a.eventTime(), a.actor(), a.support, a.groups.size(), a.firstAt,
                    "deterministic", manifestId, a.confidence(), NarrativeVocabulary.UNASSESSED, null, a.sourceKey,
                    reliability, SocialEvidenceGrades.credibility(NarrativeVocabulary.UNASSESSED), tlp, now, now);
            Map.Entry<String, Boolean> result = store.upsertClaim(claim);
            if (result.getValue()) {
                store.claim(result.getKey()).ifPresent(c -> events.emit(NarrativeVocabulary.EVENT_CLAIM,
                        claimBody(NarrativeViews.claim(c), now), false));
            }
        }
        store.deleteClaimsExcept(narrativeId, "deterministic", keys);
        return store.claims(narrativeId);
    }

    /** One claim being aggregated. */
    private static final class Agg {
        final String type;
        final String signalFamily;
        final String polarity;
        final Map<String, Object> place;
        final Instant firstAt;
        final String sourceKey;
        boolean hedged;
        int support;
        double confidenceSum;
        final Set<String> groups = new HashSet<>();
        final Set<String> entities = new LinkedHashSet<>();
        final Map<String, Integer> actors = new LinkedHashMap<>();
        final List<TimeExpressions.EventTime> times = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Agg(Map<?, ?> c, NarrativeStore.Member first) {
            type = c.get("type").toString();
            signalFamily = c.get("signalFamily") == null ? null : c.get("signalFamily").toString();
            polarity = c.get("polarity") == null ? "asserted" : c.get("polarity").toString();
            place = c.get("place") instanceof Map<?, ?> p ? new LinkedHashMap<>((Map<String, Object>) p) : null;
            firstAt = first.createdAt();
            sourceKey = first.sourceKey();
        }

        void add(Map<?, ?> c, NarrativeStore.Member m, String group) {
            support++;
            groups.add(group);
            hedged |= Boolean.TRUE.equals(c.get("hedged"));
            if (c.get("confidence") instanceof Number n) {
                confidenceSum += n.doubleValue();
            }
            if (c.get("entityRefs") instanceof Collection<?> refs) {
                for (Object r : refs) {
                    if (r != null && entities.size() < 20) {
                        entities.add(r.toString());
                    }
                }
            }
            if (c.get("actor") != null) {
                actors.merge(c.get("actor").toString(), 1, Integer::sum);
            }
            if (c.get("eventTime") instanceof Map<?, ?> t && t.get("from") != null) {
                try {
                    times.add(new TimeExpressions.EventTime(Instant.parse(t.get("from").toString()),
                            t.get("to") == null ? null : Instant.parse(t.get("to").toString()),
                            t.get("precision") == null ? "day" : t.get("precision").toString()));
                } catch (RuntimeException e) {
                    // an unparseable stored time is ignored
                }
            }
        }

        double confidence() {
            return support == 0 ? 0 : Math.round(confidenceSum / support * 100) / 100.0;
        }

        String actor() {
            String best = null;
            int n = 0;
            for (Map.Entry<String, Integer> e : actors.entrySet()) {
                if (e.getValue() > n) {
                    best = e.getKey();
                    n = e.getValue();
                }
            }
            return best;
        }

        Map<String, Object> eventTime() {
            TimeExpressions.EventTime t = TimeExpressions.earliest(times);
            return t == null ? null : t.toMap();
        }

        Double lon() {
            return coordinate(0);
        }

        Double lat() {
            return coordinate(1);
        }

        String regionCode() {
            return place == null || place.get("regionCode") == null ? null : place.get("regionCode").toString();
        }

        private Double coordinate(int i) {
            if (place != null && place.get("geom") instanceof Map<?, ?> g && g.get("coordinates") instanceof List<?> c
                    && c.size() == 2 && c.get(i) instanceof Number n) {
                return n.doubleValue();
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------- news and agencies

    /** Links 133 stories by URL (an article a member links) or by text (title tokens), and their situations. */
    void linkStories(List<String> links, String title, List<String> topics, List<String> regions, Instant first,
            Instant last, List<String> storyIds, List<String> situationIds) {
        NewsStoryStore newsStore = stories.getIfAvailable();
        if (newsStore == null) {
            return;
        }
        Set<String> ids = new LinkedHashSet<>();
        try {
            if (!links.isEmpty()) {
                ids.addAll(store.jdbc().queryForList("SELECT DISTINCT story_id FROM news_article WHERE url = ANY(?) AND"
                        + " story_id IS NOT NULL LIMIT ?", String.class, NarrativeStore.array(links), MAX_STORIES));
            }
            if (first != null && (!topics.isEmpty() || !regions.isEmpty()) && title != null) {
                List<String> tokens = TitleTokens.tokens(title);
                Duration window = Duration.ofHours(72);
                for (NewsStoryStore.Story s : newsStore.candidates(first.minus(window), last.plus(window), topics,
                        regions, 200)) {
                    if (TitleTokens.jaccard(tokens, s.titleTokens()) >= STORY_JACCARD) {
                        ids.add(s.id());
                    }
                    if (ids.size() >= MAX_STORIES) {
                        break;
                    }
                }
            }
            Set<String> situations = new LinkedHashSet<>();
            for (NewsStoryStore.Story s : newsStore.stories(ids).values()) {
                situations.addAll(s.situationIds());
            }
            storyIds.addAll(ids);
            situationIds.addAll(situations);
        } catch (RuntimeException e) {
            log.debug("Could not link news stories: {}", e.toString());
        }
    }

    /** The articles of linked stories (at most {@value #MAX_ARTICLES}). */
    List<PropagationTracer.Article> articles(List<String> storyIds) {
        NewsStoryStore newsStore = stories.getIfAvailable();
        List<PropagationTracer.Article> out = new ArrayList<>();
        if (newsStore == null || storyIds.isEmpty()) {
            return out;
        }
        for (List<NewsStoryStore.Article> list : newsStore.articles(storyIds).values()) {
            for (NewsStoryStore.Article a : list) {
                if (out.size() >= MAX_ARTICLES) {
                    return out;
                }
                out.add(new PropagationTracer.Article(a.id(), RecordRef.format(a.sourceId(), a.externalId()),
                        a.storyId(), a.url(), a.seenAt(), a.title()));
            }
        }
        return out;
    }

    /** Agency records (not posts, not news) whose URL a member links. */
    List<PropagationTracer.Official> officials(Collection<String> links) {
        List<PropagationTracer.Official> out = new ArrayList<>();
        if (links.isEmpty()) {
            return out;
        }
        List<String> list = new ArrayList<>(links);
        try {
            store.jdbc().query("SELECT source_id, external_id, persisted_observed_at, payload->>'url' AS url, title"
                    + " FROM record_head WHERE payload->>'url' = ANY(?) AND kind NOT IN ('social-post', 'news-article')"
                    + " AND NOT tombstone LIMIT ?", rs -> {
                        out.add(new PropagationTracer.Official(RecordRef.format(rs.getString(1), rs.getString(2)),
                                rs.getString("url"), rs.getTimestamp(3) == null ? null : rs.getTimestamp(3).toInstant(),
                                rs.getString("title")));
                    }, NarrativeStore.array(list.subList(0, Math.min(list.size(), MAX_OFFICIALS))), MAX_OFFICIALS);
        } catch (RuntimeException e) {
            log.debug("Could not look up cited records: {}", e.toString());
        }
        return out;
    }

    /** The {@code PropagationGraph} of a narrative from its stored edges. */
    public Map<String, Object> propagation(NarrativeStore.Narrative n) {
        List<NarrativeStore.Member> members = store.members(n.id());
        Set<String> links = new LinkedHashSet<>();
        for (NarrativeStore.Member m : members) {
            links.addAll(m.links());
        }
        List<PropagationTracer.Article> articles = articles(n.newsStoryIds());
        List<PropagationTracer.Official> officials = officials(links);
        List<NarrativeStore.Edge> edges = store.edges(n.id());
        if (edges.isEmpty() && members.size() > 1) {
            edges = PropagationTracer.edges(members, articles, officials);
        }
        PropagationTracer.Graph graph = PropagationTracer.graph(n.id(), members, edges, articles, officials,
                NarrativeVocabulary.MAX_GRAPH_NODES);
        Map<String, Object> out = graph.toMap();
        Map<String, Object> origin = new LinkedHashMap<>();
        origin.put("instanceId", "local");
        origin.put("principal", "system");
        origin.put("productRef", "narrative:" + n.id());
        origin.put("manifestId", n.manifestId());
        origin.put("role", "origin");
        origin.put("at", n.createdAt().toString());
        out.put("provenanceChain", List.of(origin));
        Map<String, Object> tracer = new LinkedHashMap<>();
        tracer.put("name", NarrativeVocabulary.TRACER);
        tracer.put("version", NarrativeVocabulary.TRACER_VERSION);
        tracer.put("params", Map.of("maxNodes", NarrativeVocabulary.MAX_GRAPH_NODES, "crossPlatformJaccard",
                PropagationTracer.CROSS_PLATFORM_JACCARD, "crossPlatformPosts", PropagationTracer.CROSS_PLATFORM_POSTS));
        out.put("tracer", tracer);
        out.put("computedAt", clock.instant().toString());
        out.put("manifestId", n.manifestId());
        return out;
    }

    // ------------------------------------------------------------------------- merge and split

    /**
     * Merges narratives into a survivor (118 rules): members, evidence links, claims, edges and fingerprints move, the
     * others get {@code mergedInto}, and both histories record it.
     *
     * @param survivorId the surviving narrative
     * @param others the narratives merged into it
     * @param actor {@code system} or the principal
     * @param reason the reason, or null
     * @throws NotFoundException {@code UNKNOWN_NARRATIVE}
     * @throws ValidationException when a narrative is merged already or names the survivor
     */
    @Transactional
    public void merge(String survivorId, List<String> others, String actor, String reason) {
        NarrativeStore.Narrative survivor = store.narrative(survivorId).orElseThrow(() -> unknown(survivorId));
        if (survivor.mergedInto() != null) {
            throw ValidationException.forField("narrativeId", "is merged into " + survivor.mergedInto());
        }
        Instant now = clock.instant();
        List<String> merged = new ArrayList<>();
        for (String id : new LinkedHashSet<>(others)) {
            if (id.equals(survivorId)) {
                throw ValidationException.forField("narrativeIds", "names the surviving narrative");
            }
            NarrativeStore.Narrative loser = store.narrative(id).orElseThrow(() -> unknown(id));
            if (loser.mergedInto() != null) {
                throw ValidationException.forField("narrativeIds", id + " is already merged into "
                        + loser.mergedInto());
            }
            merged.add(id);
        }
        for (String id : merged) {
            evidence.merge(id, survivorId, now);
            store.moveAllMembers(id, survivorId);
            store.moveClaims(id, survivorId);
            store.moveEdges(id, survivorId);
            store.renameNarrative(id, survivorId);
            store.setMerged(id, survivorId, now);
            store.log(id, "merged-into", actor, List.of(survivorId), null, survivorId, reason, now);
            store.log(survivorId, "merged", actor, List.of(id), null, null, reason, now);
        }
        store.markDirty(survivorId, now);
        refresh(survivorId, false);
        NarrativeStore.Narrative after = store.narrative(survivorId).orElse(survivor);
        events.emit(NarrativeVocabulary.EVENT_UPDATED, narrativeBody(NarrativeVocabulary.EVENT_UPDATED,
                NarrativeViews.summary(after), List.of("merged"), now), false);
    }

    /**
     * Splits posts into a new narrative: the chosen members and the reposts and quotes of them in their evidence groups
     * move, with their evidence links, to a new canonical event; both narratives re-derive and record the split.
     *
     * @param narrativeId the narrative
     * @param postRefs record refs of member posts
     * @param actor the principal
     * @param reason the reason, or null
     * @return the new narrative's id
     * @throws NotFoundException {@code UNKNOWN_NARRATIVE}
     * @throws ValidationException when a post is not a member, or every member would move
     */
    @Transactional
    public String split(String narrativeId, List<String> postRefs, String actor, String reason) {
        NarrativeStore.Narrative n = store.narrative(narrativeId).orElseThrow(() -> unknown(narrativeId));
        if (n.mergedInto() != null) {
            throw ValidationException.forField("narrativeId", "is merged into " + n.mergedInto());
        }
        List<NarrativeStore.Member> members = store.members(narrativeId);
        Map<String, NarrativeStore.Member> byId = new LinkedHashMap<>();
        for (NarrativeStore.Member m : members) {
            byId.put(m.id(), m);
        }
        Set<String> chosen = new LinkedHashSet<>();
        List<FieldError> errors = new ArrayList<>();
        for (int i = 0; i < postRefs.size(); i++) {
            String ref = postRefs.get(i);
            try {
                RecordRef r = RecordRef.parse(ref);
                String id = NarrativeStore.memberId(r.sourceId(), r.externalId());
                if (!byId.containsKey(id)) {
                    errors.add(new FieldError("postRefs[" + i + "]", "is not a member of this narrative"));
                } else {
                    chosen.add(id);
                }
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("postRefs[" + i + "]", "is not a record ref"));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The split is not valid.", errors);
        }
        Set<String> chosenPosts = new HashSet<>();
        for (String id : chosen) {
            chosenPosts.add(byId.get(id).postKey());
        }
        boolean grew = true;
        while (grew) {
            grew = false;
            for (NarrativeStore.Member m : members) {
                if (chosen.contains(m.id())) {
                    continue;
                }
                if ((m.repostOf() != null && chosenPosts.contains(m.repostOf()))
                        || (m.quoteOf() != null && chosenPosts.contains(m.quoteOf()))) {
                    chosen.add(m.id());
                    chosenPosts.add(m.postKey());
                    grew = true;
                }
            }
        }
        if (chosen.size() >= members.size()) {
            throw ValidationException.forField("postRefs", "would move every member; nothing would remain");
        }
        Instant now = clock.instant();
        NarrativeStore.Member seed = null;
        for (NarrativeStore.Member m : members) {
            if (chosen.contains(m.id())) {
                seed = m;
                break;
            }
        }
        List<String> regions = new ArrayList<>(seed.regionCodes());
        ObservationFacts facts = new ObservationFacts(seed.sourceId(), seed.externalId(), seed.seq(), null,
                seed.createdAt(), seed.createdAt(), null, null, null, null, null, null, null, null, List.of(), null);
        String newId = evidence.insertEvent(NarrativeVocabulary.FAMILY, facts, regions, now);
        store.insertNarrative(newId, n.title(), seed.id(), seed.signature(), seed.topicIds(), seed.placeRefs(),
                seed.entityRefs(), regions, seed.language() == null ? List.of() : List.of(seed.language()),
                seed.createdAt(), n.tlp(), n.manifestId(), now);
        List<String> linkIds = new ArrayList<>();
        for (String id : chosen) {
            if (byId.get(id).linkId() != null) {
                linkIds.add(byId.get(id).linkId());
            }
        }
        store.moveMembers(chosen, newId);
        store.moveLinks(linkIds, newId);
        store.log(newId, "created", actor, List.of(narrativeId), null, null, reason, now);
        store.log(newId, "split-from", actor, List.of(narrativeId), narrativeId, null, reason, now);
        store.log(narrativeId, "split", actor, List.of(newId), null, newId, reason, now);
        refresh(newId, false);
        refresh(narrativeId, false);
        for (String id : List.of(narrativeId, newId)) {
            store.narrative(id).ifPresent(after -> events.emit(NarrativeVocabulary.EVENT_UPDATED, narrativeBody(
                    NarrativeVocabulary.EVENT_UPDATED, NarrativeViews.summary(after), List.of("split"), now), false));
        }
        return newId;
    }

    private static NotFoundException unknown(String id) {
        return new NotFoundException(ErrorCodes.UNKNOWN_NARRATIVE, "No narrative '" + id + "'.");
    }

    /** The lower-case form used to compare place names. */
    static String lower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    /** The signature of a text (convenience for callers). */
    static MinHashSketch signature(String text) {
        return TextSignatures.signature(text);
    }
}
