package ai.foresignal.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The social term index (prompt 135): one Aho–Corasick automaton over every enabled topic's terms, plus the
 * hashtags, accounts and channels it follows and an exclude automaton. Immutable; {@link TermIndexService} rebuilds
 * it whenever a topic changes.
 *
 * <p>A post matches a topic when it matches one of its terms (folded, on word boundaries, {@link TextFolding}), one of
 * its hashtags, one of its accounts or one of its channels, and none of its exclude terms; when the topic names
 * {@code platforms}, only posts of those networks match; a {@code cue-only} topic matches only cue posts (137). Terms
 * of every language are matched in every post: a term list per language says which words to look for, not which
 * posts to look in, because a platform's language tag is often missing or wrong.
 *
 * <p>Scopes: every match counts for {@code topic:{id}}; a match of a place's names also counts for
 * {@code place:{id}}, an entity's names for {@code entity:{id}}, and a region's names attribute the post to that 040
 * region for {@code social_coverage}.
 */
public final class TermIndex {

    /**
     * One topic as indexed.
     *
     * @param topicId the topic id
     * @param block its social block
     * @param placeTerms place id to its names and aliases
     * @param regionTerms region code to its names
     * @param entityTerms entity id to its names and identifiers
     */
    public record TopicEntry(String topicId, SocialTopicBlock block, Map<String, List<String>> placeTerms,
            Map<String, List<String>> regionTerms, Map<String, List<String>> entityTerms) {

        /** Copies the maps. */
        public TopicEntry {
            placeTerms = placeTerms == null ? Map.of() : Map.copyOf(placeTerms);
            regionTerms = regionTerms == null ? Map.of() : Map.copyOf(regionTerms);
            entityTerms = entityTerms == null ? Map.of() : Map.copyOf(entityTerms);
        }
    }

    /**
     * What a post offers for matching.
     *
     * @param platform the network
     * @param text the text, or null
     * @param hashtags its hashtags
     * @param handle the author handle, or null
     * @param channel the channel (subreddit, instance, YouTube channel id), or null
     * @param collection the post collection ({@code stream}, {@code cue}, …)
     */
    public record Input(String platform, String text, List<String> hashtags, String handle, String channel,
            String collection) {

        /** The input of a post. */
        public static Input of(SocialPost post) {
            return new Input(post.platform(), post.text(), post.hashtags().isEmpty()
                    ? SocialPosts.hashtags(post.text()) : post.hashtags(),
                    post.author() == null ? null : post.author().handle(), post.channel(), post.collection());
        }
    }

    /**
     * The result of matching one post.
     *
     * @param topicIds the matched topics, sorted
     * @param scopes the matched scope keys ({@code topic:…}, {@code place:…}, {@code entity:…}), sorted
     * @param regionCodes the regions whose names matched, sorted
     */
    public record Match(List<String> topicIds, List<String> scopes, List<String> regionCodes) {

        /** No match. */
        public static final Match NONE = new Match(List.of(), List.of(), List.of());

        /** True when at least one topic matched. */
        public boolean matched() {
            return !topicIds.isEmpty();
        }
    }

    /** What one indexed term stands for: a topic plus an optional place, entity or region. */
    record Target(int topic, String scope, String region) {}

    private final List<TopicEntry> topics;
    private final AhoCorasick<List<Target>> terms;
    private final AhoCorasick<List<Integer>> excludes;
    private final Map<String, List<Integer>> hashtags = new HashMap<>();
    private final Map<String, List<Integer>> accounts = new HashMap<>();
    private final Map<String, List<Integer>> channels = new HashMap<>();
    private final Map<String, List<Integer>> hashtagChannels = new HashMap<>();
    private final int termCount;

    /** An index with no topics. */
    public static final TermIndex EMPTY = new TermIndex(List.of());

    /**
     * Builds the index.
     *
     * @param entries the enabled topics with a social block
     */
    public TermIndex(List<TopicEntry> entries) {
        this.topics = List.copyOf(entries);
        Map<String, List<Target>> termMap = new LinkedHashMap<>();
        Map<String, List<Integer>> excludeMap = new LinkedHashMap<>();
        for (int t = 0; t < topics.size(); t++) {
            TopicEntry e = topics.get(t);
            final int ti = t;
            for (String term : e.block().allTerms()) {
                add(termMap, term, new Target(t, null, null));
            }
            e.placeTerms().forEach((placeId, names) -> {
                for (String name : names) {
                    add(termMap, name, new Target(ti, SocialVocabulary.scopeKey(
                            SocialVocabulary.SCOPE_PLACE, placeId), null));
                }
            });
            e.entityTerms().forEach((entityId, names) -> {
                for (String name : names) {
                    add(termMap, name, new Target(ti, SocialVocabulary.scopeKey(
                            SocialVocabulary.SCOPE_ENTITY, entityId), null));
                }
            });
            e.regionTerms().forEach((code, names) -> {
                for (String name : names) {
                    add(termMap, name, new Target(ti, null, code));
                }
            });
            for (String ex : e.block().exclude()) {
                String folded = TextFolding.fold(ex);
                if (!folded.isEmpty()) {
                    excludeMap.computeIfAbsent(folded, k -> new ArrayList<>()).add(t);
                }
            }
            for (String tag : e.block().hashtags()) {
                put(hashtags, TextFolding.hashtag(tag), t);
            }
            for (SocialTopicBlock.Account a : e.block().accounts()) {
                put(accounts, a.platform() + "|" + TextFolding.handle(a.handle()), t);
            }
            for (SocialTopicBlock.Channel c : e.block().channels()) {
                if ("mastodon-hashtag".equals(c.type())) {
                    put(hashtagChannels, c.platform() + "|" + TextFolding.hashtag(c.id()), t);
                } else {
                    put(channels, c.platform() + "|" + channelKey(c.id()), t);
                }
            }
        }
        this.terms = new AhoCorasick<>(termMap);
        this.excludes = new AhoCorasick<>(excludeMap);
        this.termCount = termMap.size();
    }

    private static void add(Map<String, List<Target>> map, String term, Target target) {
        String folded = TextFolding.fold(term);
        if (!folded.isEmpty()) {
            map.computeIfAbsent(folded, k -> new ArrayList<>()).add(target);
        }
    }

    private static void put(Map<String, List<Integer>> map, String key, int topic) {
        if (key != null && !key.endsWith("|") && !key.isEmpty()) {
            List<Integer> list = map.computeIfAbsent(key, k -> new ArrayList<>());
            if (!list.contains(topic)) {
                list.add(topic);
            }
        }
    }

    /** A channel id folded for comparison: no {@code r/}, {@code @} or scheme, lower case. */
    static String channelKey(String id) {
        if (id == null) {
            return "";
        }
        String s = id.strip().toLowerCase(java.util.Locale.ROOT);
        if (s.startsWith("https://")) {
            s = s.substring(8);
        } else if (s.startsWith("http://")) {
            s = s.substring(7);
        }
        if (s.startsWith("/r/")) {
            s = s.substring(3);
        } else if (s.startsWith("r/")) {
            s = s.substring(2);
        }
        while (s.startsWith("@")) {
            s = s.substring(1);
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** The indexed topics. */
    public List<TopicEntry> topics() {
        return topics;
    }

    /** The number of distinct folded terms. */
    public int termCount() {
        return termCount;
    }

    /** True when no topic is indexed. */
    public boolean isEmpty() {
        return topics.isEmpty();
    }

    /**
     * Matches one post.
     *
     * @param input what the post offers
     * @return the topics, scopes and regions it matched
     */
    public Match match(Input input) {
        if (topics.isEmpty() || input == null) {
            return Match.NONE;
        }
        String text = TextFolding.fold(input.text());
        Set<Integer> matchedTopics = new LinkedHashSet<>();
        Map<Integer, Set<String>> scopes = new HashMap<>();
        Map<Integer, Set<String>> regions = new HashMap<>();
        for (AhoCorasick.Hit<List<Target>> hit : terms.search(text)) {
            if (!TextFolding.onBoundaries(text, hit.pattern(), hit.start(), hit.end())) {
                continue;
            }
            for (Target target : hit.value()) {
                matchedTopics.add(target.topic());
                if (target.scope() != null) {
                    scopes.computeIfAbsent(target.topic(), k -> new TreeSet<>()).add(target.scope());
                }
                if (target.region() != null) {
                    regions.computeIfAbsent(target.topic(), k -> new TreeSet<>()).add(target.region());
                }
            }
        }
        String platform = input.platform() == null ? "" : input.platform();
        if (input.hashtags() != null) {
            for (String tag : input.hashtags()) {
                String key = TextFolding.hashtag(tag);
                matchedTopics.addAll(hashtags.getOrDefault(key, List.of()));
                matchedTopics.addAll(hashtagChannels.getOrDefault(platform + "|" + key, List.of()));
            }
        }
        if (input.handle() != null) {
            matchedTopics.addAll(accounts.getOrDefault(platform + "|" + TextFolding.handle(input.handle()), List.of()));
        }
        if (input.channel() != null) {
            matchedTopics.addAll(channels.getOrDefault(platform + "|" + channelKey(input.channel()), List.of()));
        }
        if (matchedTopics.isEmpty()) {
            return Match.NONE;
        }
        Set<Integer> excluded = new LinkedHashSet<>();
        for (AhoCorasick.Hit<List<Integer>> hit : excludes.search(text)) {
            if (TextFolding.onBoundaries(text, hit.pattern(), hit.start(), hit.end())) {
                excluded.addAll(hit.value());
            }
        }
        boolean cue = SocialVocabulary.COLLECTION_CUE.equals(input.collection());
        Set<String> topicIds = new TreeSet<>();
        Set<String> scopeKeys = new TreeSet<>();
        Set<String> regionCodes = new TreeSet<>();
        for (int t : matchedTopics) {
            if (excluded.contains(t)) {
                continue;
            }
            TopicEntry e = topics.get(t);
            if (!e.block().platforms().isEmpty() && !e.block().platforms().contains(platform)) {
                continue;
            }
            if (e.block().cueOnly() && !cue) {
                continue;
            }
            topicIds.add(e.topicId());
            scopeKeys.add(SocialVocabulary.scopeKey(SocialVocabulary.SCOPE_TOPIC, e.topicId()));
            scopeKeys.addAll(scopes.getOrDefault(t, Set.of()));
            regionCodes.addAll(regions.getOrDefault(t, Set.of()));
        }
        if (topicIds.isEmpty()) {
            return Match.NONE;
        }
        return new Match(List.copyOf(topicIds), List.copyOf(scopeKeys), List.copyOf(regionCodes));
    }

    /** The search query terms of a topic for search-based adapters: its terms, place, region and entity names. */
    public static List<String> queryTerms(TopicEntry entry) {
        Set<String> out = new LinkedHashSet<>(entry.block().allTerms());
        entry.placeTerms().values().forEach(out::addAll);
        entry.regionTerms().values().forEach(out::addAll);
        entry.entityTerms().values().forEach(out::addAll);
        return List.copyOf(out);
    }
}
