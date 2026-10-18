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

    /** The variants this prompt implements. */
    List<String> TYPES = List.of(RECORDS, SAVED_VIEW, WATCHLIST, ENTITIES, SITUATIONS, FINDINGS, SERIES,
            RASTER_PRODUCTS, ALERTS);

    /** The variants later prompts add (137 social, 138 external streams). */
    List<String> LATER_TYPES = List.of("social-narratives", "social-volume", "external-stream");

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
