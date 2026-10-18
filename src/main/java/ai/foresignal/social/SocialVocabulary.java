package ai.foresignal.social;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The fixed words of the social layer (prompt 135): the record kind, networks, adapter ids, collection kinds and
 * types, volume metrics, retention and the defaults of the {@code social.*} settings.
 */
public final class SocialVocabulary {

    /** The record kind of a stored post. */
    public static final String KIND = "social-post";

    /** The analytic retention class of posts (Conventions "Analytic retention"). */
    public static final String RETENTION_CLASS = "social";

    /** The default retention of posts ({@code social.postRetention}), per platform. */
    public static final Duration DEFAULT_POST_RETENTION = Duration.ofDays(30);

    /** The longest post text kept. */
    public static final int MAX_TEXT = 5_000;

    /** A network name ({@code SocialPost.platform}). */
    public static final Pattern NETWORK = Pattern.compile("^[a-z0-9][a-z0-9-]{0,31}$");

    /** The Bluesky network. */
    public static final String BLUESKY = "bluesky";

    /** The Mastodon network. */
    public static final String MASTODON = "mastodon";

    /** The Reddit network. */
    public static final String REDDIT = "reddit";

    /** The YouTube network. */
    public static final String YOUTUBE = "youtube";

    /** The X network. */
    public static final String X = "x";

    /** The Threads network. */
    public static final String THREADS = "threads";

    /** The Instagram network. */
    public static final String INSTAGRAM = "instagram";

    /** The built-in networks. */
    public static final List<String> NETWORKS = List.of(BLUESKY, MASTODON, REDDIT, YOUTUBE, X, THREADS, INSTAGRAM);

    /** Adapter id: the Bluesky Jetstream live stream (on by default). */
    public static final String BLUESKY_JETSTREAM = "bluesky-jetstream";

    /** Adapter id: Bluesky AppView search. */
    public static final String BLUESKY_SEARCH = "bluesky-search";

    /** Adapter id: Mastodon instances. */
    public static final String MASTODON_ADAPTER = "mastodon";

    /** Adapter id: Reddit. */
    public static final String REDDIT_ADAPTER = "reddit";

    /** Adapter id: YouTube. */
    public static final String YOUTUBE_ADAPTER = "youtube";

    /** Adapter id: X. */
    public static final String X_ADAPTER = "x";

    /** Adapter id: Threads keyword search. */
    public static final String THREADS_ADAPTER = "threads";

    /** Adapter id: Instagram hashtag search. */
    public static final String INSTAGRAM_ADAPTER = "instagram-hashtags";

    /** Every adapter id, in the spec's {@code SocialPlatformId} order. */
    public static final List<String> PLATFORM_IDS = List.of(BLUESKY_JETSTREAM, BLUESKY_SEARCH, MASTODON_ADAPTER,
            REDDIT_ADAPTER, YOUTUBE_ADAPTER, X_ADAPTER, THREADS_ADAPTER, INSTAGRAM_ADAPTER);

    /** Post collection: a complete stream for its filter. */
    public static final String COLLECTION_STREAM = "stream";

    /** Post collection: a paged search. */
    public static final String COLLECTION_SEARCH = "search";

    /** Post collection: a ranked sample. */
    public static final String COLLECTION_SAMPLE = "sample";

    /** Post collection: a 137 cue (a targeted sample). */
    public static final String COLLECTION_CUE = "cue";

    /** Post collection: an EDP import (138). */
    public static final String COLLECTION_IMPORT = "import";

    /** The {@code SocialPostPayload.collection} values. */
    public static final List<String> COLLECTIONS = List.of(COLLECTION_STREAM, COLLECTION_SEARCH, COLLECTION_SAMPLE,
            COLLECTION_CUE, COLLECTION_IMPORT);

    /** Collection type: complete for its filter. */
    public static final String COMPLETE = "complete";

    /** Collection type: complete within the API's window and limits. */
    public static final String PAGED = "paged";

    /** Collection type: a ranked or targeted sample. */
    public static final String SAMPLED = "sampled";

    /** The {@code SocialCollectionType} values. */
    public static final List<String> COLLECTION_TYPES = List.of(COMPLETE, PAGED, SAMPLED);

    /** Metric: posts. */
    public static final String POSTS = "posts";

    /** Metric: distinct author keys. */
    public static final String UNIQUE_AUTHORS = "uniqueAuthors";

    /** Metric: reposts. */
    public static final String REPOSTS = "reposts";

    /** Metric: engagement. */
    public static final String ENGAGEMENT = "engagement";

    /** Metric: the platform-language denominator (every post seen on complete streams). */
    public static final String DENOMINATOR = "denominator";

    /** The volume metrics. */
    public static final List<String> METRICS = List.of(POSTS, UNIQUE_AUTHORS, REPOSTS, ENGAGEMENT, DENOMINATOR);

    /** Scope type: an attention topic. */
    public static final String SCOPE_TOPIC = "topic";

    /** Scope type: a 036 place. */
    public static final String SCOPE_PLACE = "place";

    /** Scope type: a 098 entity. */
    public static final String SCOPE_ENTITY = "entity";

    /** The scope types. */
    public static final List<String> SCOPE_TYPES = List.of(SCOPE_TOPIC, SCOPE_PLACE, SCOPE_ENTITY);

    /** The pseudo scope of denominator series ({@code social:all/{platform}/denominator}). */
    public static final String SCOPE_ALL = "all";

    /** The language bucket of posts in no configured language. */
    public static final String OTHER_LANGUAGE = "other";

    /** The default detection languages ({@code social.languages}). */
    public static final List<String> DEFAULT_LANGUAGES = List.of("en", "ar", "he", "fa", "ru", "uk", "fr", "es", "de",
            "tr", "zh");

    /** The default region baseline below which social observability is low ({@code social.minRegionBaseline}). */
    public static final double DEFAULT_MIN_REGION_BASELINE = 20;

    /** The default velocity ratio of a finding ({@code social.velocity.minRatio}). */
    public static final double DEFAULT_VELOCITY_RATIO = 5;

    /** The default distinct authors in the hour of a finding ({@code social.velocity.minUniqueAuthors}). */
    public static final int DEFAULT_VELOCITY_AUTHORS = 20;

    /** The default deduplication window of a finding per scope ({@code social.velocity.dedupHours}). */
    public static final int DEFAULT_VELOCITY_DEDUP_HOURS = 6;

    /** The observability family of social attention (prompt 135). */
    public static final String ATTENTION_FAMILY = "social-attention";

    /** The observability method of social attention. */
    public static final String PLATFORM_VOLUME_METHOD = "platform-volume";

    /** The reason of a social absence that coverage cannot support. */
    public static final String LOW_SOCIAL_COVERAGE = "LOW_SOCIAL_COVERAGE";

    /** The reason of a social absence over a window with an unreplayed stream gap. */
    public static final String SOCIAL_STREAM_GAP = "SOCIAL_STREAM_GAP";

    /** The finding type of a velocity spike. */
    public static final String VELOCITY_FINDING = "social-velocity";

    /** The originator class of social accounts (116). */
    public static final String ORIGINATOR_CLASS = "social";

    /** The prefix of per-account originators: {@code social:{platform}:{authorKey}}. */
    public static final String ORIGINATOR_PREFIX = "social:";

    /** Platforms whose deletion sync is on by default (X, Bluesky, Threads). */
    public static final Set<String> DELETION_SYNC_DEFAULT = Set.of(BLUESKY_JETSTREAM, BLUESKY_SEARCH, X_ADAPTER,
            THREADS_ADAPTER);

    private SocialVocabulary() {}

    /** The {@code externalId} of a post: {@code {platform}:{postId}}. */
    public static String externalId(String platform, String postId) {
        return platform + ":" + postId;
    }

    /** True for a collection that feeds baselines (complete and paged paths). */
    public static boolean feedsBaselines(String collectionType) {
        return COMPLETE.equals(collectionType) || PAGED.equals(collectionType);
    }

    /** The collection type of a post collection: stream complete, search paged, everything else sampled. */
    public static String collectionTypeOf(String collection) {
        if (COLLECTION_STREAM.equals(collection)) {
            return COMPLETE;
        }
        if (COLLECTION_SEARCH.equals(collection)) {
            return PAGED;
        }
        return SAMPLED;
    }

    /** The scope key {@code {type}:{id}}. */
    public static String scopeKey(String type, String id) {
        return type + ":" + id;
    }
}
