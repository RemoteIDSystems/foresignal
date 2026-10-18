package ai.foresignal.chronicle;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import ai.foresignal.store.TriggerCondition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * What a chronicle layer selects (prompt 131, OpenAPI {@code ChronicleLayerSelector}): one variant by {@code type}.
 * {@link #parse} checks the shape only; {@link ChronicleSelectorValidator} checks that what it names exists.
 */
public sealed interface ChronicleSelector {

    /** {@code records}: sources and kinds with 017 conditions. */
    String RECORDS = "records";

    /** {@code saved-view}: a 036 saved view. */
    String SAVED_VIEW = "saved-view";

    /** {@code watchlist}: a 036 watchlist (entities whose identity is on it). */
    String WATCHLIST = "watchlist";

    /** {@code entities}: 098 entities (their linked records). */
    String ENTITIES = "entities";

    /** {@code situations}: 118 situations (the situation records and their members, membership-driven). */
    String SITUATIONS = "situations";

    /** {@code findings}: 119 findings by type and family. */
    String FINDINGS = "findings";

    /** {@code series}: series refs of the 119 registry. */
    String SERIES = "series";

    /** {@code raster-products}: 042 raster products (frames by reference). */
    String RASTER_PRODUCTS = "raster-products";

    /** {@code alerts}: alerts by rule ids or tags. */
    String ALERTS = "alerts";

    /** {@code social-narratives}: 137 narratives, claims and comparisons by topic, place, region or situation. */
    String SOCIAL_NARRATIVES = "social-narratives";

    /** {@code social-volume}: 135 social volume series by topic, place or entity. */
    String SOCIAL_VOLUME = "social-volume";

    /** {@code external-stream}: one stream of an EDP provider with optional 017 conditions (prompt 138). */
    String EXTERNAL_STREAM = "external-stream";

    /** The variants implemented (131, the two social layers of 137 and the external stream of 138). */
    List<String> TYPES = List.of(RECORDS, SAVED_VIEW, WATCHLIST, ENTITIES, SITUATIONS, FINDINGS, SERIES,
            RASTER_PRODUCTS, ALERTS, SOCIAL_NARRATIVES, SOCIAL_VOLUME, EXTERNAL_STREAM);

    /** The variants a later prompt adds: none since prompt 138. */
    List<String> LATER_TYPES = List.of();

    /** Most items in one list. */
    int MAX_ITEMS = 200;

    /** Most conditions. */
    int MAX_CONDITIONS = 20;

    /** The variant's {@code type}. */
    String type();

    /** The wire shape (also stored as jsonb). */
    Map<String, Object> toMap();

    /**
     * Sources and kinds with conditions.
     *
     * @param sources source ids; empty for any
     * @param kinds kinds; empty for any
     * @param conditions the 017 conditions, ANDed
     */
    record Records(List<String> sources, List<String> kinds, List<TriggerCondition> conditions)
            implements ChronicleSelector {

        /** Copies the lists. */
        public Records {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }

        @Override
        public String type() {
            return RECORDS;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("type", RECORDS);
            out.put("sources", sources);
            out.put("kinds", kinds);
            List<Map<String, Object>> c = new ArrayList<>();
            for (TriggerCondition t : conditions) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("path", t.path());
                m.put("op", t.op());
                m.put("value", t.value());
                c.add(m);
            }
            out.put("conditions", c);
            return out;
        }
    }

    /**
     * One stream of an external (EDP) provider (prompt 138): the provider's records whose external id starts with
     * {@code {streamId}:}, with optional 017 conditions on the item properties.
     *
     * @param sourceId the provider ({@code ext-…})
     * @param streamId the stream id
     * @param filter the 017 conditions, ANDed
     */
    record ExternalStream(String sourceId, String streamId, List<TriggerCondition> filter)
            implements ChronicleSelector {

        /** Copies the list. */
        public ExternalStream {
            filter = filter == null ? List.of() : List.copyOf(filter);
        }

        @Override
        public String type() {
            return EXTERNAL_STREAM;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("type", EXTERNAL_STREAM);
            out.put("sourceId", sourceId);
            out.put("streamId", streamId);
            List<Map<String, Object>> c = new ArrayList<>();
            for (TriggerCondition t : filter) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("path", t.path());
                m.put("op", t.op());
                m.put("value", t.value());
                c.add(m);
            }
            out.put("filter", c);
            return out;
        }

        /** True when {@code externalId} belongs to the stream. */
        public boolean holds(String externalId) {
            return externalId != null && externalId.startsWith(streamId + ":");
        }
    }

    /**
     * A saved view.
     *
     * @param viewId the view id or slug
     */
    record SavedView(String viewId) implements ChronicleSelector {

        @Override
        public String type() {
            return SAVED_VIEW;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", SAVED_VIEW, "viewId", viewId);
        }
    }

    /**
     * A watchlist.
     *
     * @param watchlistId the watchlist id (a UUID)
     */
    record Watchlist(String watchlistId) implements ChronicleSelector {

        @Override
        public String type() {
            return WATCHLIST;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", WATCHLIST, "watchlistId", watchlistId);
        }
    }

    /**
     * Entities.
     *
     * @param entityIds the entity ids
     */
    record Entities(List<String> entityIds) implements ChronicleSelector {

        /** Copies the list. */
        public Entities {
            entityIds = entityIds == null ? List.of() : List.copyOf(entityIds);
        }

        @Override
        public String type() {
            return ENTITIES;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", ENTITIES, "entityIds", entityIds);
        }
    }

    /**
     * Situations.
     *
     * @param situationIds the situation ids
     */
    record Situations(List<String> situationIds) implements ChronicleSelector {

        /** Copies the list. */
        public Situations {
            situationIds = situationIds == null ? List.of() : List.copyOf(situationIds);
        }

        @Override
        public String type() {
            return SITUATIONS;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", SITUATIONS, "situationIds", situationIds);
        }
    }

    /**
     * Findings by type and family.
     *
     * @param findingTypes finding types; empty for any
     * @param families signal families; empty for any
     */
    record Findings(List<String> findingTypes, List<String> families) implements ChronicleSelector {

        /** Copies the lists. */
        public Findings {
            findingTypes = findingTypes == null ? List.of() : List.copyOf(findingTypes);
            families = families == null ? List.of() : List.copyOf(families);
        }

        @Override
        public String type() {
            return FINDINGS;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", FINDINGS, "findingTypes", findingTypes, "families", families);
        }
    }

    /**
     * Series refs.
     *
     * @param refs the series refs
     */
    record Series(List<String> refs) implements ChronicleSelector {

        /** Copies the list. */
        public Series {
            refs = refs == null ? List.of() : List.copyOf(refs);
        }

        @Override
        public String type() {
            return SERIES;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", SERIES, "refs", refs);
        }
    }

    /**
     * Raster products.
     *
     * @param productIds the product ids
     */
    record RasterProducts(List<String> productIds) implements ChronicleSelector {

        /** Copies the list. */
        public RasterProducts {
            productIds = productIds == null ? List.of() : List.copyOf(productIds);
        }

        @Override
        public String type() {
            return RASTER_PRODUCTS;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", RASTER_PRODUCTS, "productIds", productIds);
        }
    }

    /**
     * Alerts by rule ids or tags (either matches; both empty matches every alert).
     *
     * @param ruleIds rule ids (UUIDs)
     * @param tags rule tags
     */
    record Alerts(List<String> ruleIds, List<String> tags) implements ChronicleSelector {

        /** Copies the lists. */
        public Alerts {
            ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
            tags = tags == null ? List.of() : List.copyOf(tags);
        }

        @Override
        public String type() {
            return ALERTS;
        }

        @Override
        public Map<String, Object> toMap() {
            return ordered("type", ALERTS, "ruleIds", ruleIds, "tags", tags);
        }
    }

    /**
     * Social narratives (prompt 137): narratives, claims and comparisons whose narrative matches the topics, places,
     * regions or situations (any list empty matches anything) and whose status is listed (empty: any). Thread items keep
     * their history after the posts expire; member post text only when {@code keepPostText}.
     *
     * @param topicIds attention topics
     * @param placeIds 036 places
     * @param regionCodes 040 regions
     * @param situationIds 118 situations
     * @param statuses narrative statuses
     * @param keepPostText keep the central post text in the snapshots
     */
    record SocialNarratives(List<String> topicIds, List<String> placeIds, List<String> regionCodes,
            List<String> situationIds, List<String> statuses, boolean keepPostText) implements ChronicleSelector {

        /** Copies the lists. */
        public SocialNarratives {
            topicIds = topicIds == null ? List.of() : List.copyOf(topicIds);
            placeIds = placeIds == null ? List.of() : List.copyOf(placeIds);
            regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
            situationIds = situationIds == null ? List.of() : List.copyOf(situationIds);
            statuses = statuses == null ? List.of() : List.copyOf(statuses);
        }

        @Override
        public String type() {
            return SOCIAL_NARRATIVES;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("type", SOCIAL_NARRATIVES);
            out.put("topicIds", topicIds);
            out.put("placeIds", placeIds);
            out.put("regionCodes", regionCodes);
            out.put("situationIds", situationIds);
            out.put("statuses", statuses);
            out.put("keepPostText", keepPostText);
            return out;
        }

        /**
         * True when a narrative matches.
         *
         * @param topics its topics
         * @param places its 036 place refs or ids
         * @param regions its regions
         * @param situations its situations
         * @param status its status
         */
        public boolean takes(List<String> topics, List<String> places, List<String> regions, List<String> situations,
                String status) {
            if (!statuses.isEmpty() && !statuses.contains(status)) {
                return false;
            }
            boolean scoped = !topicIds.isEmpty() || !placeIds.isEmpty() || !regionCodes.isEmpty()
                    || !situationIds.isEmpty();
            if (!scoped) {
                return true;
            }
            for (String p : places) {
                String id = p.startsWith("place:") ? p.substring(6) : p;
                if (placeIds.contains(id) || placeIds.contains(p)) {
                    return true;
                }
            }
            for (String r : regions) {
                if (regionCodes.contains(r) || (r.length() > 2 && regionCodes.contains(r.substring(0, 2)))) {
                    return true;
                }
            }
            return topics.stream().anyMatch(topicIds::contains) || situations.stream().anyMatch(situationIds::contains);
        }
    }

    /**
     * Social volume series (prompt 137).
     *
     * @param scopes {@code [type, id]} pairs: {@code topic}, {@code place} or {@code entity}
     * @param platforms networks; empty for every network
     * @param metrics {@code posts}, {@code uniqueAuthors}, {@code reposts}, {@code engagement}; empty for posts
     */
    record SocialVolume(List<List<String>> scopes, List<String> platforms, List<String> metrics)
            implements ChronicleSelector {

        /** The networks sampled when none is named. */
        public static final List<String> ALL_PLATFORMS = List.of("bluesky", "mastodon", "reddit", "youtube", "x",
                "threads", "instagram");

        /** The metrics. */
        public static final List<String> METRICS = List.of("posts", "uniqueAuthors", "reposts", "engagement");

        /** Copies the lists. */
        public SocialVolume {
            scopes = scopes == null ? List.of() : List.copyOf(scopes);
            platforms = platforms == null ? List.of() : List.copyOf(platforms);
            metrics = metrics == null ? List.of() : List.copyOf(metrics);
        }

        @Override
        public String type() {
            return SOCIAL_VOLUME;
        }

        @Override
        public Map<String, Object> toMap() {
            List<Map<String, Object>> s = new ArrayList<>();
            for (List<String> scope : scopes) {
                s.add(ordered("type", scope.get(0), "id", scope.get(1)));
            }
            return ordered("type", SOCIAL_VOLUME, "scopes", s, "platforms", platforms, "metrics", metrics);
        }

        /** The {@code social:} series refs sampled: every scope × platform × metric. */
        public List<String> refs() {
            List<String> out = new ArrayList<>();
            for (List<String> scope : scopes) {
                for (String platform : platforms.isEmpty() ? ALL_PLATFORMS : platforms) {
                    for (String metric : metrics.isEmpty() ? List.of("posts") : metrics) {
                        out.add("social:" + scope.get(0) + ":" + scope.get(1) + "/" + platform + "/" + metric);
                    }
                }
            }
            return out;
        }
    }

    /**
     * Parses a selector's shape.
     *
     * @param node the body
     * @param field the field name for errors
     * @return the selector
     * @throws ValidationException {@code 400 CHRONICLE_LAYER_INVALID}
     */
    static ChronicleSelector parse(JsonNode node, String field) {
        if (node == null || node.isNull() || !node.isObject()) {
            throw invalid(field, "must be an object with type");
        }
        JsonNode t = node.get("type");
        String type = t != null && t.isString() ? t.asString() : null;
        if (type != null && LATER_TYPES.contains(type)) {
            throw invalid(field + ".type", "'" + type + "' layers arrive with prompts 137 and 138");
        }
        if (type == null || !TYPES.contains(type)) {
            throw invalid(field + ".type", "must be one of " + TYPES);
        }
        switch (type) {
            case RECORDS -> members(node, field, Set.of("type", "sources", "kinds", "conditions"));
            case SAVED_VIEW -> members(node, field, Set.of("type", "viewId"));
            case WATCHLIST -> members(node, field, Set.of("type", "watchlistId"));
            case ENTITIES -> members(node, field, Set.of("type", "entityIds"));
            case SITUATIONS -> members(node, field, Set.of("type", "situationIds"));
            case FINDINGS -> members(node, field, Set.of("type", "findingTypes", "families"));
            case SERIES -> members(node, field, Set.of("type", "refs"));
            case RASTER_PRODUCTS -> members(node, field, Set.of("type", "productIds"));
            case SOCIAL_NARRATIVES -> members(node, field, Set.of("type", "topicIds", "placeIds", "regionCodes",
                    "situationIds", "statuses", "keepPostText"));
            case SOCIAL_VOLUME -> members(node, field, Set.of("type", "scopes", "platforms", "metrics"));
            case EXTERNAL_STREAM -> members(node, field, Set.of("type", "sourceId", "streamId", "filter"));
            default -> members(node, field, Set.of("type", "ruleIds", "tags"));
        }
        return switch (type) {
            case RECORDS -> {
                Records r = new Records(strings(node, "sources", field, false), strings(node, "kinds", field, false),
                        conditions(node.get("conditions"), field + ".conditions"));
                if (r.sources().isEmpty() && r.kinds().isEmpty()) {
                    throw invalid(field, "a records selector needs sources or kinds");
                }
                yield r;
            }
            case SAVED_VIEW -> new SavedView(required(node, "viewId", field));
            case WATCHLIST -> {
                String id = required(node, "watchlistId", field);
                try {
                    UUID.fromString(id);
                } catch (IllegalArgumentException e) {
                    throw invalid(field + ".watchlistId", "must be a watchlist id (UUID)");
                }
                yield new Watchlist(id);
            }
            case ENTITIES -> new Entities(ulids(strings(node, "entityIds", field, true), field + ".entityIds"));
            case SITUATIONS -> new Situations(ulids(strings(node, "situationIds", field, true),
                    field + ".situationIds"));
            case FINDINGS -> new Findings(strings(node, "findingTypes", field, false),
                    strings(node, "families", field, false));
            case SERIES -> new Series(strings(node, "refs", field, true));
            case RASTER_PRODUCTS -> new RasterProducts(strings(node, "productIds", field, true));
            case SOCIAL_NARRATIVES -> socialNarratives(node, field);
            case SOCIAL_VOLUME -> socialVolume(node, field);
            case EXTERNAL_STREAM -> new ExternalStream(required(node, "sourceId", field),
                    required(node, "streamId", field), conditions(node.get("filter"), field + ".filter"));
            default -> {
                List<String> rules = strings(node, "ruleIds", field, false);
                for (String id : rules) {
                    try {
                        UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        throw invalid(field + ".ruleIds", "'" + id + "' is not a rule id (UUID)");
                    }
                }
                yield new Alerts(rules, strings(node, "tags", field, false));
            }
        };
    }

    /** Parses a {@code social-narratives} selector. */
    private static SocialNarratives socialNarratives(JsonNode node, String field) {
        List<String> regions = strings(node, "regionCodes", field, false);
        for (String r : regions) {
            if (!r.matches("^[A-Z]{2}(-[A-Z0-9]{1,3})?$")) {
                throw invalid(field + ".regionCodes", "'" + r + "' is not a 040 region code");
            }
        }
        List<String> statuses = strings(node, "statuses", field, false);
        List<String> allowed = List.of("unassessed", "pending", "corroborated", "unobserved", "contradicted", "mixed",
                "insufficient-coverage");
        for (String s : statuses) {
            if (!allowed.contains(s)) {
                throw invalid(field + ".statuses", "'" + s + "' is not a narrative status; one of " + allowed);
            }
        }
        JsonNode keep = node.get("keepPostText");
        if (keep != null && !keep.isNull() && !keep.isBoolean()) {
            throw invalid(field + ".keepPostText", "must be a boolean");
        }
        return new SocialNarratives(strings(node, "topicIds", field, false), strings(node, "placeIds", field, false),
                regions, ulids(strings(node, "situationIds", field, false), field + ".situationIds"), statuses,
                keep != null && keep.isBoolean() && keep.asBoolean());
    }

    /** Parses a {@code social-volume} selector. */
    private static SocialVolume socialVolume(JsonNode node, String field) {
        JsonNode scopes = node.get("scopes");
        if (scopes == null || !scopes.isArray() || scopes.isEmpty()) {
            throw invalid(field + ".scopes", "is required (at least one {type, id})");
        }
        if (scopes.size() > MAX_ITEMS) {
            throw invalid(field + ".scopes", "holds more than " + MAX_ITEMS + " items");
        }
        List<List<String>> out = new ArrayList<>();
        for (JsonNode scope : scopes) {
            String type = scope != null && scope.isObject() && scope.hasNonNull("type") ? scope.get("type").asString()
                    : null;
            String id = scope != null && scope.isObject() && scope.hasNonNull("id") ? scope.get("id").asString() : null;
            if (type == null || !List.of("topic", "place", "entity").contains(type) || id == null || id.isBlank()
                    || id.length() > 200) {
                throw invalid(field + ".scopes", "each scope needs type (topic, place or entity) and an id");
            }
            out.add(List.of(type, id.strip()));
        }
        List<String> platforms = strings(node, "platforms", field, false);
        for (String p : platforms) {
            if (!p.matches("^[a-z0-9][a-z0-9-]{0,31}$")) {
                throw invalid(field + ".platforms", "'" + p + "' is not a network id");
            }
        }
        List<String> metrics = strings(node, "metrics", field, false);
        for (String m : metrics) {
            if (!SocialVolume.METRICS.contains(m)) {
                throw invalid(field + ".metrics", "'" + m + "' is not one of " + SocialVolume.METRICS);
            }
        }
        return new SocialVolume(out, platforms, metrics);
    }

    /** A selector from its stored map. */
    static ChronicleSelector fromMap(Map<String, Object> map, JsonMapper json) {
        return parse(json.valueToTree(map), "selector");
    }

    private static void members(JsonNode node, String field, Set<String> allowed) {
        for (String member : node.propertyNames()) {
            if (!allowed.contains(member)) {
                throw invalid(field + "." + member, "is not a member of this selector type");
            }
        }
    }

    private static String required(JsonNode node, String member, String field) {
        JsonNode v = node.get(member);
        if (v == null || !v.isString() || v.asString().isBlank() || v.asString().length() > 200) {
            throw invalid(field + "." + member, "is required (1 to 200 characters)");
        }
        return v.asString().strip();
    }

    private static List<String> strings(JsonNode node, String member, String field, boolean required) {
        JsonNode v = node.get(member);
        List<String> out = new ArrayList<>();
        if (v == null || v.isNull()) {
            if (required) {
                throw invalid(field + "." + member, "is required (at least one item)");
            }
            return out;
        }
        if (!v.isArray()) {
            throw invalid(field + "." + member, "must be an array of strings");
        }
        for (JsonNode item : v) {
            if (!item.isString() || item.asString().isBlank() || item.asString().length() > 600) {
                throw invalid(field + "." + member, "must hold non-empty strings of at most 600 characters");
            }
            if (!out.contains(item.asString().strip())) {
                out.add(item.asString().strip());
            }
        }
        if (required && out.isEmpty()) {
            throw invalid(field + "." + member, "needs at least one item");
        }
        if (out.size() > MAX_ITEMS) {
            throw invalid(field + "." + member, "holds more than " + MAX_ITEMS + " items");
        }
        return out;
    }

    private static List<String> ulids(List<String> ids, String field) {
        for (String id : ids) {
            if (!Ulids.isUlid(id)) {
                throw invalid(field, "'" + id + "' is not a ULID");
            }
        }
        return ids;
    }

    private static List<TriggerCondition> conditions(JsonNode node, String field) {
        List<TriggerCondition> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            throw invalid(field, "must be an array of {path, op, value}");
        }
        if (node.size() > MAX_CONDITIONS) {
            throw invalid(field, "holds more than " + MAX_CONDITIONS + " conditions");
        }
        int i = 0;
        for (JsonNode item : node) {
            String prefix = field + "[" + i++ + "]";
            if (!item.isObject()) {
                throw invalid(prefix, "must be an object with path, op and value");
            }
            for (String member : item.propertyNames()) {
                if (!Set.of("path", "op", "value").contains(member)) {
                    throw invalid(prefix + "." + member, "is not a condition member");
                }
            }
            JsonNode op = item.get("op");
            if (op == null || !op.isString() || op.asString().isBlank()) {
                throw invalid(prefix + ".op", "is required");
            }
            JsonNode path = item.get("path");
            String p = path == null || path.isNull() ? null : path.isString() ? path.asString() : null;
            if (path != null && !path.isNull() && (p == null || p.isBlank() || p.length() > 200)) {
                throw invalid(prefix + ".path", "must be a dot path of at most 200 characters");
            }
            out.add(new TriggerCondition(p, op.asString(), plain(item.get("value"))));
        }
        return out;
    }

    /** A JSON value as plain Java (null, boolean, number, string, list, map). */
    private static Object plain(JsonNode v) {
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isBoolean()) {
            return v.asBoolean();
        }
        if (v.isIntegralNumber()) {
            return v.asLong();
        }
        if (v.isNumber()) {
            return v.asDouble();
        }
        if (v.isString()) {
            return v.asString();
        }
        if (v.isArray()) {
            List<Object> out = new ArrayList<>();
            v.forEach(e -> out.add(plain(e)));
            return out;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (String name : v.propertyNames()) {
            out.put(name, plain(v.get(name)));
        }
        return out;
    }

    private static Map<String, Object> ordered(Object... pairs) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            out.put((String) pairs[i], pairs[i + 1]);
        }
        return out;
    }

    /** {@code 400 CHRONICLE_LAYER_INVALID} on {@code field}. */
    static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.CHRONICLE_LAYER_INVALID, "The layer selector is not valid.",
                List.of(new FieldError(field, message)));
    }
}
