package ai.foresignal.social.narrative;

import ai.foresignal.chronicle.network.MinHashSketch;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.evidence.EvidenceStore;
import ai.foresignal.evidence.ObservationFacts;
import ai.foresignal.evidence.StoryFamilyHandler;
import ai.foresignal.news.TitleEmbeddings;
import ai.foresignal.news.Vectors;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialOriginators;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialPosts;
import ai.foresignal.source.formats.news.ArticleUrls;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The 116 resolver of the {@code social-narrative} family (prompt 136, {@code NarrativeClusterer} 1.0.0): groups
 * {@code social-post} records into narratives on the evidence resolver's cursor ({@link StoryFamilyHandler}).
 *
 * <ol>
 *   <li>A post that reposts or quotes a member joins that member's narrative ({@code repost}, {@code quote}).
 *   <li>Otherwise, among unmerged narratives whose latest member is within {@code windowHours} (72) of the post: one
 *       with a member linking the same URL ({@code link}), then one with a member using the same image URL or media
 *       fingerprint ({@code media}), then, among those sharing a topic, a resolved place or region, or an entity, the
 *       most similar by text ({@code text}): the post's text embedding (027) against the centroid at
 *       {@code minCosine} (0.85) when both exist under one model, else the token MinHash Jaccard against the central
 *       member and the latest {@value #MEMBER_SIGNATURES} members at {@code minJaccard} (0.6). Ties go to the older
 *       narrative.
 *   <li>Otherwise the post starts a narrative: a canonical event of the family and a {@code social_narrative} row
 *       ({@code seed}).
 * </ol>
 * The member row keeps the post's text signature, links, image URLs, scopes and deterministic claims
 * ({@link ClaimExtractor}); text-derived regions are counted into 135's {@code social_coverage}, never into geometry.
 * Images are queued for {@link MediaFingerprinter}, which may later merge a one-post narrative into the narrative an
 * identical image belongs to. A post collected by two sources joins once. Observations are never modified (MEM-1).
 */
@Component
public class NarrativeClusterer implements StoryFamilyHandler {

    /** Candidate narratives compared by text per post. */
    static final int CANDIDATES = 200;

    /** Latest member signatures compared per candidate. */
    static final int MEMBER_SIGNATURES = 50;

    private final NarrativeStore store;
    private final NarrativeService service;
    private final EvidenceStore evidence;
    private final PlaceIndex places;
    private final EntityIndex entities;
    private final ObjectProvider<TitleEmbeddings> embeddings;
    private final ObjectProvider<SocialCollector> collector;
    private final Clock clock;

    /** Creates the clusterer. */
    public NarrativeClusterer(NarrativeStore store, NarrativeService service, EvidenceStore evidence,
            PlaceIndex places, EntityIndex entities, ObjectProvider<TitleEmbeddings> embeddings,
            ObjectProvider<SocialCollector> collector, Clock clock) {
        this.store = store;
        this.service = service;
        this.evidence = evidence;
        this.places = places;
        this.entities = entities;
        this.embeddings = embeddings;
        this.collector = collector;
        this.clock = clock;
    }

    @Override
    public boolean handles(String family) {
        return NarrativeVocabulary.FAMILY.equals(family);
    }

    /**
     * What a post contributes, before it is placed.
     *
     * @param postKey the post key
     * @param platform the network
     * @param url the public URL, or null
     * @param authorKey the author key
     * @param originatorId the 116 originator
     * @param sourceKey the reporting source
     * @param accountCreatedAt when the account was created, or null
     * @param signature the text signature
     * @param language the language, or null
     * @param createdAt the post time
     * @param repostOf the reposted post key, or null
     * @param quoteOf the quoted post key, or null
     * @param replyTo the post key replied to, or null
     * @param links normalized links
     * @param mediaUrls image and thumbnail URLs
     * @param fingerprints fingerprints already known for those URLs
     * @param topicIds attention topics
     * @param extraction the claim extraction
     */
    public record Facts(String postKey, String platform, String url, String authorKey, String originatorId,
            String sourceKey, Instant accountCreatedAt, MinHashSketch signature, String language, Instant createdAt,
            String repostOf, String quoteOf, String replyTo, List<String> links, List<String> mediaUrls,
            List<String> fingerprints, List<String> topicIds, ClaimExtractor.Extraction extraction) {}

    /**
     * Where a post goes.
     *
     * @param narrativeId the narrative, or null for a new one
     * @param reason the link reason
     * @param matchedKey the member matched, or null
     * @param similarity the similarity, or null
     */
    public record Placement(String narrativeId, String reason, String matchedKey, Double similarity) {}

    @Override
    public void process(CanonicalRules.FamilyRule rule, Observation obs) {
        Map<String, Object> p = obs.payload() == null ? Map.of() : obs.payload();
        if (p.get("platform") == null || p.get("postId") == null) {
            return;
        }
        CanonicalRules.Story params = rule.story() == null ? CanonicalRules.Story.DEFAULT : rule.story();
        Instant now = clock.instant();
        NarrativeStore.Member existing = store.member(obs.sourceId(), obs.externalId()).orElse(null);
        if (existing != null && existing.seq() >= obs.seq()) {
            return;
        }
        boolean deleted = Boolean.TRUE.equals(p.get("deleted"));
        if (existing != null) {
            if (!deleted) {
                Facts f = facts(obs, p);
                store.updateMember(member(existing.narrativeId(), obs, f, existing.linkReason(), existing.matchedKey(),
                        existing.similarity(), existing.linkId(), existing.joinedAt()));
                store.markDirty(existing.narrativeId(), now);
            }
            return;
        }
        if (deleted) {
            return;
        }
        Facts f = facts(obs, p);
        if (store.memberByPost(f.postKey()).isPresent()) {
            return;
        }
        Placement placement = place(f, params, obs.sourceId() + "/" + obs.externalId());
        ObservationFacts facts = new ObservationFacts(obs.sourceId(), obs.externalId(), obs.seq(), obs.version(),
                f.createdAt(), obs.observedAt() != null ? obs.observedAt() : now, null, null, null, null, null, null,
                null, null, List.of(), null);
        String narrativeId = placement.narrativeId();
        boolean created = false;
        Map<String, Object> reason = new LinkedHashMap<>();
        reason.put("rule", placement.reason());
        reason.put("similarity", placement.similarity());
        reason.put("matched", placement.matchedKey());
        if (narrativeId == null) {
            narrativeId = evidence.insertEvent(NarrativeVocabulary.FAMILY, facts, f.extraction().regionCodes(), now);
            String manifestId = service.manifest(NarrativeVocabulary.CLUSTERER, NarrativeVocabulary.CLUSTERER_VERSION,
                    NarrativeService.clustererRef(), narrativeId, null);
            String text = p.get("text") == null ? "" : p.get("text").toString();
            store.insertNarrative(narrativeId, text.isBlank() ? "(no text)" : NarrativeService.memberTitle(text),
                    NarrativeStore.memberId(obs.sourceId(), obs.externalId()), f.signature(), f.topicIds(),
                    f.extraction().placeRefs(), f.extraction().entityRefs(), f.extraction().regionCodes(),
                    f.language() == null ? List.of() : List.of(f.language()), f.createdAt(), "CLEAR", manifestId, now);
            store.log(narrativeId, "created", "system", List.of(), null, null, null, now);
            reason.put("rule", "first-observation");
            created = true;
        }
        evidence.insertLink(narrativeId, facts, SocialOriginators.of(f.originatorId()), placement.similarity() == null
                ? 1.0 : Math.max(0, Math.min(1, placement.similarity())), reason, now);
        String linkId = null;
        for (EvidenceStore.StoredLink link : evidence.linksOf(obs.sourceId(), obs.externalId())) {
            if (narrativeId.equals(link.canonicalEventId())) {
                linkId = link.id();
                break;
            }
        }
        NarrativeStore.Member m = member(narrativeId, obs, f, placement.reason(), placement.matchedKey(),
                placement.similarity(), linkId, now);
        store.insertMember(m);
        store.touch(narrativeId, f.createdAt(), now);
        coverage(f);
        enqueueMedia(obs, f, narrativeId, now);
        NarrativeStore.Narrative n = store.narrative(narrativeId).orElse(null);
        if (created || (n != null && n.postCount() <= NarrativeService.INLINE_REFRESH_POSTS)) {
            service.refresh(narrativeId, created);
        }
    }

    /**
     * Decides where a post goes.
     *
     * @param f the post's facts
     * @param params the family's clustering parameters
     * @param recordKey the post's {@code source/externalId} (its 027 vector key)
     */
    Placement place(Facts f, CanonicalRules.Story params, String recordKey) {
        for (String[] ref : new String[][] {{f.repostOf(), NarrativeVocabulary.REPOST},
            {f.quoteOf(), NarrativeVocabulary.QUOTE}}) {
            if (ref[0] != null) {
                NarrativeStore.Member target = store.memberByPost(ref[0]).orElse(null);
                if (target != null) {
                    return new Placement(target.narrativeId(), ref[1], target.id(), 1.0);
                }
            }
        }
        Instant from = f.createdAt().minus(Duration.ofMillis((long) (params.windowHours() * 3_600_000)));
        Map<String, String> byLink = store.narrativesSharingLinks(f.links(), from, 20);
        if (!byLink.isEmpty()) {
            Map.Entry<String, String> e = byLink.entrySet().iterator().next();
            return new Placement(e.getKey(), NarrativeVocabulary.LINK, e.getValue(), 1.0);
        }
        Map<String, String> byMedia = store.narrativesSharingMedia(f.mediaUrls(), f.fingerprints(), from, 20);
        if (!byMedia.isEmpty()) {
            Map.Entry<String, String> e = byMedia.entrySet().iterator().next();
            return new Placement(e.getKey(), NarrativeVocabulary.MEDIA, e.getValue(), 1.0);
        }
        if (f.signature() == null || f.signature().isEmpty()) {
            return new Placement(null, NarrativeVocabulary.SEED, null, null);
        }
        List<NarrativeStore.Narrative> candidates = store.candidates(from, f.topicIds(), f.extraction().placeRefs(),
                f.extraction().regionCodes(), f.extraction().entityRefs(), CANDIDATES);
        if (candidates.isEmpty()) {
            return new Placement(null, NarrativeVocabulary.SEED, null, null);
        }
        float[] vector = null;
        String model = null;
        TitleEmbeddings te = embeddings.getIfAvailable();
        if (te != null) {
            TitleEmbeddings.TitleVectors v = te.vectors(List.of(recordKey));
            vector = v.vectors().isEmpty() ? null : v.vectors().values().iterator().next();
            model = v.model();
        }
        Placement best = null;
        double bestScore = -1;
        Instant bestCreated = null;
        for (NarrativeStore.Narrative n : candidates) {
            double score;
            boolean ok;
            String matched = n.centralPost();
            if (vector != null && n.centroid() != null && model != null && model.equals(n.centroidModel())) {
                Double cos = Vectors.cosine(vector, n.centroid());
                score = cos == null ? 0 : cos;
                ok = score >= params.minCosine();
            } else {
                score = TextSignatures.jaccard(f.signature(), n.centralSignature());
                if (score < params.minJaccard() && score >= params.minJaccard() / 2) {
                    for (NarrativeStore.Member m : store.latestMembers(n.id(), MEMBER_SIGNATURES)) {
                        double s = TextSignatures.jaccard(f.signature(), m.signature());
                        if (s > score) {
                            score = s;
                            matched = m.id();
                        }
                    }
                }
                ok = score >= params.minJaccard();
            }
            if (!ok) {
                continue;
            }
            if (score > bestScore + 1e-12 || (Math.abs(score - bestScore) <= 1e-12 && bestCreated != null
                    && n.createdAt().isBefore(bestCreated))) {
                bestScore = score;
                bestCreated = n.createdAt();
                best = new Placement(n.id(), NarrativeVocabulary.TEXT, matched, Math.round(score * 10_000) / 10_000.0);
            }
        }
        return best != null ? best : new Placement(null, NarrativeVocabulary.SEED, null, null);
    }

    /** The facts of a post version. */
    Facts facts(Observation obs, Map<String, Object> p) {
        String platform = p.get("platform").toString();
        String text = p.get("text") == null ? null : p.get("text").toString();
        String authorKey = SocialPosts.authorKeyOf(p);
        Instant createdAt = instant(p.get("createdAt"), obs.eventTime() != null ? obs.eventTime()
                : obs.observedAt() != null ? obs.observedAt() : clock.instant());
        Instant accountCreatedAt = null;
        if (p.get("author") instanceof Map<?, ?> author) {
            accountCreatedAt = instant(author.get("accountCreatedAt"), null);
        }
        Set<String> links = new LinkedHashSet<>();
        if (p.get("links") instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null) {
                    String canonical = ArticleUrls.canonical(o.toString());
                    links.add(canonical != null ? canonical : o.toString());
                }
            }
        }
        Set<String> mediaUrls = new LinkedHashSet<>();
        if (p.get("media") instanceof Collection<?> c) {
            for (Object o : c) {
                if (o instanceof Map<?, ?> m) {
                    String type = m.get("type") == null ? "other" : m.get("type").toString();
                    if (("image".equals(type) || "gif".equals(type)) && m.get("url") != null) {
                        mediaUrls.add(m.get("url").toString());
                    } else if ("video".equals(type) && m.get("thumbUrl") != null) {
                        mediaUrls.add(m.get("thumbUrl").toString());
                    }
                }
            }
        }
        List<String> fingerprints = new ArrayList<>();
        for (String url : mediaUrls) {
            store.fingerprintOfUrl(url).ifPresent(fingerprints::add);
        }
        SocialPost.PostRef repost = SocialPosts.refOf(p, "repostOf");
        SocialPost.PostRef quote = SocialPosts.refOf(p, "quoteOf");
        SocialPost.PostRef reply = SocialPosts.refOf(p, "replyTo");
        String channel = p.get("channel") == null ? null : p.get("channel").toString();
        ClaimExtractor.Extraction extraction = ClaimExtractor.extract(text, createdAt, ClaimLexicon.bundled(), places,
                entities);
        String language = SocialPosts.primaryLanguage(p.get("language") == null ? null : p.get("language").toString());
        return new Facts(obs.externalId(), platform, p.get("url") == null ? null : p.get("url").toString(), authorKey,
                SocialOriginators.originatorId(p, null), SocialEvidenceGrades.sourceKey(platform, channel, authorKey),
                accountCreatedAt, TextSignatures.signature(text), language, createdAt,
                repost == null ? null : repost.externalId(), quote == null ? null : quote.externalId(),
                reply == null ? null : reply.externalId(), new ArrayList<>(links), new ArrayList<>(mediaUrls),
                fingerprints, SocialPosts.topicIdsOf(p), extraction);
    }

    private NarrativeStore.Member member(String narrativeId, Observation obs, Facts f, String reason, String matched,
            Double similarity, String linkId, Instant joinedAt) {
        List<Object> claims = new ArrayList<>();
        for (ClaimExtractor.PostClaim c : f.extraction().claims()) {
            claims.add(NarrativeService.postClaimMap(c));
        }
        return new NarrativeStore.Member(NarrativeStore.memberId(obs.sourceId(), obs.externalId()), narrativeId,
                obs.sourceId(), obs.externalId(), obs.seq(), linkId, f.platform(), f.postKey(), f.url(), f.authorKey(),
                f.originatorId(), f.sourceKey(), f.accountCreatedAt(), f.signature(), f.language(), f.createdAt(),
                reason, matched, similarity, f.repostOf(), f.quoteOf(), f.replyTo(), f.links(), f.mediaUrls(),
                f.fingerprints(), f.topicIds(), f.extraction().placeRefs(), f.extraction().entityRefs(),
                f.extraction().regionCodes(), claims, null, null, null, joinedAt);
    }

    /** Counts text-derived regions into 135's {@code social_coverage} (never into geometry). */
    private void coverage(Facts f) {
        SocialCollector c = collector.getIfAvailable();
        if (c == null) {
            return;
        }
        for (String region : f.extraction().regionCodes()) {
            c.accumulator().coverage(f.platform(), region, f.createdAt());
        }
    }

    private void enqueueMedia(Observation obs, Facts f, String narrativeId, Instant now) {
        for (String url : f.mediaUrls()) {
            if (store.fingerprintOfUrl(url).isPresent()) {
                continue;
            }
            store.enqueue(queueId(f.postKey(), url), url, obs.sourceId(), obs.externalId(), f.postKey(), f.platform(),
                    narrativeId, f.createdAt(), now);
        }
    }

    /** The queue id of a post image: SHA-256 of {@code {postKey}|{url}}. */
    static String queueId(String postKey, String url) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((postKey + "|" + url).getBytes(
                    StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Instant instant(Object value, Instant fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Instant.parse(value.toString());
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }
}
