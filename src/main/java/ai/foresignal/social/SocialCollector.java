package ai.foresignal.social;

import ai.foresignal.reference.RegionTagger;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.source.spi.NormalizedRecord;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * What every social adapter hands its posts to (prompt 135).
 *
 * <p>For each post: the language (platform tag, else Lingua among {@code social.languages}), the {@code authorKey}
 * ({@link AuthorKeys}), the denominator (every post seen on a <em>complete</em> stream counts for its
 * platform-language hour, matching or not), the topic match ({@link TermIndex}), the volume counters of the matched
 * scopes and the region coverage. A matching post becomes a {@code social-post} record; a non-matching post on a
 * stream is only counted. Cue and import posts are always stored (a cue is a targeted sample, 137; an import is what
 * the operator bound, 138), but only complete and paged collection ever feeds baselines (SOC-3: the collection type
 * travels with every count).
 *
 * <p>A post is counted once: seeing it again (an edit, an overlapping search window, a statistics refresh) stores
 * the new version but adds nothing to the counts.
 *
 * <p>Amplification: a repost, quote or reply of a recently matched post counts into that post's scopes
 * ({@code reposts} and {@code engagement}); a plain repost is never stored as a post.
 */
@Component
public class SocialCollector {

    private static final Logger log = LoggerFactory.getLogger(SocialCollector.class);

    /** How many recently matched posts are remembered for amplification. */
    static final int RECENT_MAX = 500_000;

    /** How many stored post ids are remembered per source for deletion sync. */
    static final int STORED_MAX = 5_000_000;

    /** How many posts are remembered as counted, so a post seen again (an edit, an overlapping search) counts once. */
    static final int COUNTED_MAX = 2_000_000;

    /** The most ids loaded from the store per source when its deletion cache is first needed. */
    static final int WARM_LIMIT = 2_000_000;

    /**
     * What observing one post gave.
     *
     * @param record the record to store, or null when the post is only counted
     * @param match the topic match
     * @param language the language bucket
     * @param authorKey the author key
     */
    public record Observation(NormalizedRecord record, TermIndex.Match match, String language, String authorKey) {}

    /** A matched post remembered for amplification. */
    record Remembered(List<String> scopes, String language) {}

    private final TermIndexService terms;
    private final SocialLanguages languages;
    private final SocialSalt salt;
    private final StoredPosts stored;
    private final ObjectProvider<RegionTagger> regions;
    private final Clock clock;
    private final VolumeAccumulator accumulator = new VolumeAccumulator();
    private final Cache<String, Remembered> recent = Caffeine.newBuilder().maximumSize(RECENT_MAX)
            .expireAfterWrite(Duration.ofDays(7)).build();
    private final Cache<String, Boolean> counted = Caffeine.newBuilder().maximumSize(COUNTED_MAX)
            .expireAfterWrite(Duration.ofDays(3)).build();
    private final Map<String, Cache<String, Boolean>> storedIds = new ConcurrentHashMap<>();
    private final Set<String> warmed = ConcurrentHashMap.newKeySet();
    private AuthorKeys fixedKeys;

    /** Creates the collector. */
    @Autowired
    public SocialCollector(TermIndexService terms, SocialLanguages languages, SocialSalt salt, StoredPosts stored,
            ObjectProvider<RegionTagger> regions, Clock clock) {
        this.terms = terms;
        this.languages = languages;
        this.salt = salt;
        this.stored = stored;
        this.regions = regions;
        this.clock = clock;
    }

    /**
     * Creates a collector with fixed author keys and no store (tests).
     *
     * @param terms the term index holder
     * @param languages the languages
     * @param keys the author keyer
     * @param stored the post store, or null
     * @param clock the clock
     */
    public SocialCollector(TermIndexService terms, SocialLanguages languages, AuthorKeys keys, StoredPosts stored,
            Clock clock) {
        this(terms, languages, null, stored, null, clock);
        this.fixedKeys = keys;
    }

    /** The volume counters the flush job drains. */
    public VolumeAccumulator accumulator() {
        return accumulator;
    }

    /** The current term index. */
    public TermIndex index() {
        return terms.current();
    }

    private AuthorKeys keys() {
        return fixedKeys != null ? fixedKeys : salt.authorKeys();
    }

    /**
     * Observes one post.
     *
     * @param post the post
     * @param sourceId the collecting source
     * @param collectionType {@code complete}, {@code paged} or {@code sampled}
     * @return what to store and what matched
     */
    public Observation observe(SocialPost post, String sourceId, String collectionType) {
        return observe(post, sourceId, collectionType, SocialVocabulary.COMPLETE.equals(collectionType));
    }

    /**
     * Observes one post, saying whether it counts for the platform-language denominator: true only for a stream that
     * carries the whole platform (Jetstream); a stream complete for its own filter (a Mastodon hashtag stream) is not
     * a denominator.
     *
     * @param post the post
     * @param sourceId the collecting source
     * @param collectionType {@code complete}, {@code paged} or {@code sampled}
     * @param denominator whether the post counts for the denominator
     * @return what to store and what matched
     */
    public Observation observe(SocialPost post, String sourceId, String collectionType, boolean denominator) {
        Instant now = clock.instant();
        String language = languages.languageOf(post.language(), post.text());
        SocialPost p = language != null && !language.equals(post.language()) ? post.withLanguage(language) : post;
        String bucket = languages.bucket(language);
        String accountId = p.author() == null ? null
                : p.author().accountId() != null && !p.author().accountId().isBlank() ? p.author().accountId()
                : p.author().handle();
        String authorKey = keys().key(p.platform(), accountId);
        Instant at = p.createdAt() != null ? p.createdAt() : now;
        boolean first = counted.asMap().putIfAbsent(p.externalId(), Boolean.TRUE) == null;
        if (denominator && first) {
            accumulator.denominator(p.platform(), bucket, at);
        }
        TermIndex.Match match = terms.current().match(TermIndex.Input.of(p));
        Set<String> regionCodes = new LinkedHashSet<>(match.regionCodes());
        if (p.geo() != null && p.geo().located()) {
            String region = regionOf(p.geo().lon(), p.geo().lat());
            if (region != null) {
                regionCodes.add(region);
            }
        }
        if (first) {
            for (String region : regionCodes) {
                accumulator.coverage(p.platform(), region, at);
            }
            amplify(p, at, collectionType);
        }
        boolean keep = match.matched() || SocialVocabulary.COLLECTION_CUE.equals(p.collection())
                || SocialVocabulary.COLLECTION_IMPORT.equals(p.collection());
        if (match.matched() && first) {
            long engagement = p.engagement() == null ? 0 : p.engagement().total();
            accumulator.post(match.scopes(), p.platform(), bucket, at, collectionType, authorKey, engagement);
            recent.put(p.externalId(), new Remembered(match.scopes(), bucket));
        }
        if (!keep) {
            return new Observation(null, match, bucket, authorKey);
        }
        remember(sourceId, p.externalId());
        return new Observation(SocialPosts.record(p, authorKey, match.topicIds(), now), match, bucket, authorKey);
    }

    private void amplify(SocialPost p, Instant at, String collectionType) {
        for (SocialPost.PostRef ref : new SocialPost.PostRef[] {p.quoteOf(), p.replyTo()}) {
            if (ref == null) {
                continue;
            }
            Remembered r = recent.getIfPresent(ref.externalId());
            if (r != null) {
                accumulator.amplification(r.scopes(), p.platform(), r.language(), at, collectionType, false);
            }
        }
    }

    /**
     * Observes a plain repost (no text of its own): counted into the original's scopes when the original matched
     * recently; never stored.
     *
     * @param subject the reposted post
     * @param at the repost time
     * @param collectionType the collection type
     * @return true when it was counted
     */
    public boolean observeRepost(SocialPost.PostRef subject, Instant at, String collectionType) {
        if (subject == null) {
            return false;
        }
        Remembered r = recent.getIfPresent(subject.externalId());
        if (r == null) {
            return false;
        }
        accumulator.amplification(r.scopes(), subject.platform(), r.language(), at == null ? clock.instant() : at,
                collectionType, true);
        return true;
    }

    /**
     * Observes a deletion: the tombstoned version of a post this source stored, or null when it stored none.
     *
     * @param sourceId the source
     * @param platform the network
     * @param postId the deleted post's id
     * @return the tombstoned record, or null
     */
    public NormalizedRecord observeDelete(String sourceId, String platform, String postId) {
        String externalId = SocialVocabulary.externalId(platform, postId);
        if (!knownStored(sourceId, externalId)) {
            return null;
        }
        Map<String, Object> payload = stored == null ? null : stored.payload(sourceId, externalId);
        if (payload == null || Boolean.TRUE.equals(payload.get("deleted"))) {
            return null;
        }
        return SocialPosts.tombstoned(payload, clock.instant());
    }

    /** Remembers that a source stored a post. */
    void remember(String sourceId, String externalId) {
        storedIds.computeIfAbsent(sourceId, k -> Caffeine.newBuilder().maximumSize(STORED_MAX)
                .expireAfterWrite(Duration.ofDays(400)).build()).put(externalId, Boolean.TRUE);
    }

    private boolean knownStored(String sourceId, String externalId) {
        Cache<String, Boolean> ids = storedIds.get(sourceId);
        if (stored != null && warmed.add(sourceId)) {
            try {
                for (String id : stored.externalIds(sourceId, WARM_LIMIT)) {
                    remember(sourceId, id);
                }
                ids = storedIds.get(sourceId);
            } catch (RuntimeException e) {
                warmed.remove(sourceId);
                log.debug("Could not warm the stored-post ids of {}: {}", sourceId, e.toString());
            }
        }
        return ids != null && ids.getIfPresent(externalId) != null;
    }

    private String regionOf(double lon, double lat) {
        RegionTagger tagger = regions == null ? null : regions.getIfAvailable();
        if (tagger == null) {
            return null;
        }
        try {
            RegionTags tags = tagger.tag(lon, lat);
            if (tags == null || tags.isEmpty()) {
                return null;
            }
            return tags.iso2();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Counts an absolute hourly count a platform reports itself (X {@code counts/recent}) for one scope; the store
     * replaces the hour's posts rather than adding.
     *
     * @param store the store
     * @param scope the scope key
     * @param platform the network
     * @param hour the hour start
     * @param posts the count
     */
    public void reportCount(SocialVolumeStore store, String scope, String platform, Instant hour, long posts) {
        store.putCount(scope, platform, SocialVocabulary.OTHER_LANGUAGE, hour, SocialVocabulary.COMPLETE, posts,
                clock.instant());
    }
}
