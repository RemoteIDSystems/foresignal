package ai.foresignal.social;

import ai.foresignal.api.error.FieldError;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The {@code social} block of an attention topic (prompt 135, OpenAPI {@code AttentionTopicSocial}): terms per
 * language, hashtags, accounts, channels, exclude terms, platforms, places, regions and entities whose names are added
 * as terms, and the mode.
 *
 * @param terms language (BCP 47) to phrases; at most {@value #MAX_TERMS} in total
 * @param hashtags at most {@value #MAX_HASHTAGS} hashtags (without {@code #})
 * @param accounts at most {@value #MAX_ACCOUNTS} followed accounts
 * @param channels at most {@value #MAX_CHANNELS} followed channels
 * @param exclude exclude terms
 * @param platforms the networks the topic applies to; empty = all enabled
 * @param placeIds 036 places whose names and aliases are terms
 * @param regionCodes 040 regions whose names are terms
 * @param entityRefs 098 entities ({@code entity:{id}}) whose names and identifiers are terms
 * @param mode {@code standing} or {@code cue-only}
 */
public record SocialTopicBlock(Map<String, List<String>> terms, List<String> hashtags, List<Account> accounts,
        List<Channel> channels, List<String> exclude, List<String> platforms, List<String> placeIds,
        List<String> regionCodes, List<String> entityRefs, String mode) {

    /** The most terms in total. */
    public static final int MAX_TERMS = 50;

    /** The most hashtags. */
    public static final int MAX_HASHTAGS = 30;

    /** The most accounts. */
    public static final int MAX_ACCOUNTS = 100;

    /** The most channels. */
    public static final int MAX_CHANNELS = 100;

    /** The most exclude terms, places, regions and entities each. */
    public static final int MAX_OTHER = 100;

    /** The longest term. */
    public static final int MAX_TERM_LENGTH = 200;

    /** Standing collection. */
    public static final String STANDING = "standing";

    /** Collection on cues only (137). */
    public static final String CUE_ONLY = "cue-only";

    /** The channel types. */
    public static final List<String> CHANNEL_TYPES = List.of("subreddit", "mastodon-instance", "mastodon-hashtag",
            "youtube-channel");

    private static final Pattern LANGUAGE = Pattern.compile("^[a-z]{2,3}(-[A-Za-z0-9]{1,8})*$");
    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");
    private static final Pattern ENTITY_REF = Pattern.compile("^(entity:)?[0-9A-HJKMNP-TV-Z]{26}$");

    /**
     * A followed account.
     *
     * @param platform the network
     * @param handle the handle
     */
    public record Account(String platform, String handle) {}

    /**
     * A followed channel.
     *
     * @param platform the network
     * @param type {@code subreddit}, {@code mastodon-instance}, {@code mastodon-hashtag} or {@code youtube-channel}
     * @param id the subreddit, instance host, hashtag or channel id
     */
    public record Channel(String platform, String type, String id) {}

    /** Copies the collections. */
    public SocialTopicBlock {
        terms = terms == null ? Map.of() : copyTerms(terms);
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
        accounts = accounts == null ? List.of() : List.copyOf(accounts);
        channels = channels == null ? List.of() : List.copyOf(channels);
        exclude = exclude == null ? List.of() : List.copyOf(exclude);
        platforms = platforms == null ? List.of() : List.copyOf(platforms);
        placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        entityRefs = entityRefs == null ? List.of() : List.copyOf(entityRefs);
        mode = mode == null || mode.isBlank() ? STANDING : mode;
    }

    private static Map<String, List<String>> copyTerms(Map<String, List<String>> in) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        in.forEach((k, v) -> out.put(k, v == null ? List.of() : List.copyOf(v)));
        return java.util.Collections.unmodifiableMap(out);
    }

    /** Every term of every language, in order. */
    public List<String> allTerms() {
        List<String> out = new ArrayList<>();
        terms.values().forEach(out::addAll);
        return out;
    }

    /** True for a cue-only topic. */
    public boolean cueOnly() {
        return CUE_ONLY.equals(mode);
    }

    /** The entity ids of {@link #entityRefs} (without the {@code entity:} prefix). */
    public List<String> entityIds() {
        List<String> out = new ArrayList<>();
        for (String ref : entityRefs) {
            out.add(ref.startsWith("entity:") ? ref.substring(7) : ref);
        }
        return out;
    }

    /**
     * Parses a stored or submitted block.
     *
     * @param map the JSON object, or null
     * @param errors where problems are added (as {@code social.<field>}); may be null to ignore them
     * @return the block, or null for a null map
     */
    public static SocialTopicBlock parse(Map<String, Object> map, List<FieldError> errors) {
        if (map == null) {
            return null;
        }
        List<FieldError> problems = errors == null ? new ArrayList<>() : errors;
        Map<String, List<String>> terms = new LinkedHashMap<>();
        Object rawTerms = map.get("terms");
        int termCount = 0;
        if (rawTerms instanceof Map<?, ?> tm) {
            for (Map.Entry<?, ?> e : tm.entrySet()) {
                String lang = String.valueOf(e.getKey());
                if (!LANGUAGE.matcher(lang).matches()) {
                    problems.add(new FieldError("social.terms." + lang, "is not a BCP 47 language tag"));
                    continue;
                }
                List<String> list = strings(e.getValue(), "social.terms." + lang, problems);
                termCount += list.size();
                terms.put(lang, list);
            }
        } else if (rawTerms != null) {
            problems.add(new FieldError("social.terms", "must be an object of language to phrases"));
        }
        if (termCount > MAX_TERMS) {
            problems.add(new FieldError("social.terms", "must hold at most " + MAX_TERMS + " terms in total"));
        }
        List<String> hashtags = new ArrayList<>();
        for (String h : strings(map.get("hashtags"), "social.hashtags", problems)) {
            String bare = h.startsWith("#") ? h.substring(1) : h;
            if (!bare.isBlank()) {
                hashtags.add(bare);
            }
        }
        if (hashtags.size() > MAX_HASHTAGS) {
            problems.add(new FieldError("social.hashtags", "must hold at most " + MAX_HASHTAGS + " hashtags"));
        }
        List<Account> accounts = new ArrayList<>();
        Object rawAccounts = map.get("accounts");
        if (rawAccounts instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof Map<?, ?> m && m.get("platform") != null && m.get("handle") != null
                        && SocialVocabulary.NETWORK.matcher(m.get("platform").toString()).matches()
                        && !m.get("handle").toString().isBlank()) {
                    accounts.add(new Account(m.get("platform").toString(), m.get("handle").toString().strip()));
                } else {
                    problems.add(new FieldError("social.accounts[" + i + "]", "needs a platform and a handle"));
                }
            }
        } else if (rawAccounts != null) {
            problems.add(new FieldError("social.accounts", "must be an array"));
        }
        if (accounts.size() > MAX_ACCOUNTS) {
            problems.add(new FieldError("social.accounts", "must hold at most " + MAX_ACCOUNTS + " accounts"));
        }
        List<Channel> channels = new ArrayList<>();
        Object rawChannels = map.get("channels");
        if (rawChannels instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof Map<?, ?> m && m.get("platform") != null && m.get("type") != null
                        && m.get("id") != null && CHANNEL_TYPES.contains(m.get("type").toString())
                        && SocialVocabulary.NETWORK.matcher(m.get("platform").toString()).matches()
                        && !m.get("id").toString().isBlank()) {
                    channels.add(new Channel(m.get("platform").toString(), m.get("type").toString(),
                            m.get("id").toString().strip()));
                } else {
                    problems.add(new FieldError("social.channels[" + i + "]",
                            "needs a platform, a type of " + CHANNEL_TYPES + " and an id"));
                }
            }
        } else if (rawChannels != null) {
            problems.add(new FieldError("social.channels", "must be an array"));
        }
        if (channels.size() > MAX_CHANNELS) {
            problems.add(new FieldError("social.channels", "must hold at most " + MAX_CHANNELS + " channels"));
        }
        List<String> exclude = strings(map.get("exclude"), "social.exclude", problems);
        List<String> platforms = strings(map.get("platforms"), "social.platforms", problems);
        for (int i = 0; i < platforms.size(); i++) {
            if (!SocialVocabulary.NETWORK.matcher(platforms.get(i)).matches()) {
                problems.add(new FieldError("social.platforms[" + i + "]", "is not a network name"));
            }
        }
        List<String> placeIds = strings(map.get("placeIds"), "social.placeIds", problems);
        List<String> regionCodes = strings(map.get("regionCodes"), "social.regionCodes", problems);
        for (int i = 0; i < regionCodes.size(); i++) {
            if (!REGION.matcher(regionCodes.get(i)).matches()) {
                problems.add(new FieldError("social.regionCodes[" + i + "]", "is not a 040 region code"));
            }
        }
        List<String> entityRefs = strings(map.get("entityRefs"), "social.entityRefs", problems);
        for (int i = 0; i < entityRefs.size(); i++) {
            if (!ENTITY_REF.matcher(entityRefs.get(i)).matches()) {
                problems.add(new FieldError("social.entityRefs[" + i + "]", "is not an entity reference entity:{id}"));
            }
        }
        for (List<String> other : List.of(exclude, placeIds, regionCodes, entityRefs)) {
            if (other.size() > MAX_OTHER) {
                problems.add(new FieldError("social", "a list holds more than " + MAX_OTHER + " entries"));
                break;
            }
        }
        Object rawMode = map.get("mode");
        String mode = rawMode == null ? STANDING : rawMode.toString();
        if (!STANDING.equals(mode) && !CUE_ONLY.equals(mode)) {
            problems.add(new FieldError("social.mode", "must be standing or cue-only"));
            mode = STANDING;
        }
        return new SocialTopicBlock(terms, hashtags, accounts, channels, exclude, dedupe(platforms), placeIds,
                regionCodes, entityRefs, mode);
    }

    /** The JSON form (OpenAPI {@code AttentionTopicSocial}). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("terms", new LinkedHashMap<>(terms));
        out.put("hashtags", hashtags);
        List<Map<String, Object>> a = new ArrayList<>();
        for (Account account : accounts) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("platform", account.platform());
            m.put("handle", account.handle());
            a.add(m);
        }
        out.put("accounts", a);
        List<Map<String, Object>> c = new ArrayList<>();
        for (Channel channel : channels) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("platform", channel.platform());
            m.put("type", channel.type());
            m.put("id", channel.id());
            c.add(m);
        }
        out.put("channels", c);
        out.put("exclude", exclude);
        out.put("platforms", platforms);
        out.put("placeIds", placeIds);
        out.put("regionCodes", regionCodes);
        out.put("entityRefs", entityRefs);
        out.put("mode", mode);
        return out;
    }

    private static List<String> strings(Object raw, String field, List<FieldError> problems) {
        List<String> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        if (!(raw instanceof List<?> list)) {
            problems.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (v == null || v.toString().isBlank() || v.toString().length() > MAX_TERM_LENGTH) {
                problems.add(new FieldError(field + "[" + i + "]", "must be 1 to " + MAX_TERM_LENGTH + " characters"));
            } else {
                out.add(v.toString().strip());
            }
        }
        return out;
    }

    private static List<String> dedupe(List<String> values) {
        Set<String> out = new LinkedHashSet<>(values);
        return List.copyOf(out);
    }
}
