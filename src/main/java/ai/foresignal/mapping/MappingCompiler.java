package ai.foresignal.mapping;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.source.spi.ObservedAtMode;
import com.jayway.jsonpath.JsonPath;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates a {@code RecordMapping} (OpenAPI schema, prompt 059) and compiles it into an immutable
 * {@link CompiledMapping}.
 *
 * <p>Every problem is collected before failing, each with a path such as {@code mapping.fields[3].unit.from}
 * ({@link MappingInvalidException}, {@code 400 MAPPING_INVALID}). Rules:
 *
 * <ul>
 *   <li>{@code version} is 1; unknown members are refused.
 *   <li>Every value spec is exactly one of {@code {path}}, {@code {template}} or {@code {const}}. Paths are Jayway
 *       JSONPath without deep scan, function calls or regex filters ({@link MappingJsonPath#compile}); a path of the
 *       form {@code $pairs.<key>} reads a flattened pair and needs {@code pairs}.
 *   <li>{@code externalId} and {@code observedAt} are required; {@code kind} is required unless the source has
 *       exactly one kind, and a constant kind must be one of the source's kinds.
 *   <li>{@code observedAt} agrees with the source's {@code observedAtMode}: {@code {received: true}} exactly when
 *       the mode is RECEIVED (Core-Data-Model §2: the time base is never switched per record). A mismatch fails
 *       a strict compile and is a {@link MappingWarning#TIME_BASE_MISMATCH} otherwise.
 *   <li>At most 64 fields; names match {@code ^[A-Za-z][A-Za-z0-9_]{0,63}$} and are not in {@code drop}; a unit
 *       pair is in {@link Units}; units, {@code scale} and {@code offset} need a numeric type.
 * </ul>
 */
@Component
public class MappingCompiler {

    /** The mapping schema version this compiler reads. */
    public static final int VERSION = 1;

    /** The most fields a mapping may declare. */
    public static final int MAX_FIELDS = 64;

    /** The most members {@code spread} may copy. */
    public static final int MAX_SPREAD = 32;

    /** A payload field name. */
    public static final Pattern FIELD_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,63}$");

    /** A {@code quality.extra} key (Core-Data-Model §14). */
    public static final Pattern EXTRA_KEY = Pattern.compile("^[a-z0-9]+\\.[a-z0-9_]+$");

    /** The field types. */
    public static final Set<String> TYPES = Set.of("number", "integer", "string", "boolean");

    /** The filter operators. */
    public static final Set<String> OPS = Set.of("==", "!=", "exists", "in");

    static final String PAIRS_PREFIX = "$pairs.";

    private static final Set<String> MEMBERS = Set.of("version", "select", "externalId", "kind", "observedAt",
            "eventTime", "validUntil", "geometry", "tombstone", "quality", "fields", "pairs", "spread", "drop",
            "filter");

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*(.+?)\\s*}}");
    private static final Pattern FIRST_KEY = Pattern.compile("^\\$(?:\\.([^.\\[]+)|\\[\\s*'([^']+)'|\\[\\s*\"([^\"]+)\")");
    private static final Pattern MEMBER_NAME = Pattern.compile("^[A-Za-z0-9_\\-]{1,64}$");

    /**
     * Compiles {@code mapping} for {@code context}.
     *
     * @throws MappingInvalidException listing every problem
     */
    public CompiledMapping compile(JsonNode mapping, MappingContext context) {
        return new Run(context == null ? MappingContext.none() : context).compile(mapping);
    }

    /** The state of one compilation. */
    private static final class Run {

        private final MappingContext context;
        private final List<FieldError> errors = new ArrayList<>();
        private final List<MappingWarning> warnings = new ArrayList<>();
        private final Set<String> referenced = new LinkedHashSet<>();
        private boolean usesPairs;

        Run(MappingContext context) {
            this.context = context;
        }

        CompiledMapping compile(JsonNode node) {
            if (node == null || !node.isObject()) {
                throw new MappingInvalidException(List.of(new FieldError("mapping", "must be an object")));
            }
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (!MEMBERS.contains(entry.getKey())) {
                    errors.add(new FieldError("mapping." + entry.getKey(), "is not a RecordMapping member"));
                }
            }
            JsonNode version = node.get("version");
            if (version == null || !version.isIntegralNumber() || version.asInt() != VERSION) {
                errors.add(new FieldError("mapping.version", "must be 1"));
            }
            JsonPath select = null;
            JsonNode selectNode = node.get("select");
            if (selectNode != null && !selectNode.isNull()) {
                if (!selectNode.isString()) {
                    errors.add(new FieldError("mapping.select", "must be a JSONPath string"));
                } else {
                    select = path(selectNode.asString(), "mapping.select", false);
                }
            }
            CompiledMapping.PairsSpec pairs = pairs(node.get("pairs"));
            Set<String> drop = drop(node.get("drop"));

            ValueSpec externalId = required(node, "externalId");
            ValueSpec kind = optionalValue(node.get("kind"), "mapping.kind");
            String defaultKind = null;
            if (kind == null) {
                defaultKind = context.singleKind();
                if (defaultKind == null) {
                    errors.add(new FieldError("mapping.kind", context.kinds() == null
                            ? "is required when no source is given"
                            : "is required: the source has " + context.kinds().size() + " kinds"));
                }
            } else if (kind instanceof ValueSpec.Const constant && context.kinds() != null
                    && !context.kinds().contains(constant.value().asString())) {
                errors.add(new FieldError("mapping.kind.const", "is not one of the source's kinds " + context.kinds()));
            }

            TimeSpec observedAt = null;
            JsonNode observedNode = node.get("observedAt");
            if (observedNode == null || observedNode.isNull()) {
                errors.add(new FieldError("mapping.observedAt", "is required"));
            } else {
                observedAt = time(observedNode, "mapping.observedAt");
                checkTimeBase(observedAt);
            }
            TimeSpec eventTime = optionalTime(node.get("eventTime"), "mapping.eventTime");
            TimeSpec validUntil = optionalTime(node.get("validUntil"), "mapping.validUntil");
            GeometrySpec geometry = geometry(node.get("geometry"));
            CompiledMapping.Tombstone tombstone = tombstone(node.get("tombstone"));
            CompiledMapping.QualitySpec quality = quality(node.get("quality"));
            List<CompiledMapping.FieldMapping> fields = fields(node.get("fields"), drop);
            CompiledMapping.SpreadSpec spread = spread(node.get("spread"));
            List<CompiledMapping.Filter> filters = filters(node.get("filter"));

            if (usesPairs && pairs == null) {
                errors.add(new FieldError("mapping.pairs", "is required: a path reads $pairs.<key>"));
            }
            if (!errors.isEmpty()) {
                throw new MappingInvalidException(errors);
            }
            if (pairs != null) {
                referenced.add(firstKeyOf(pairs.path().getPath()));
            }
            referenced.remove(null);
            return new CompiledMapping(node.deepCopy(), select, externalId, kind, defaultKind, context.kinds(),
                    observedAt, eventTime, validUntil, geometry, tombstone, quality, fields, pairs, spread, drop,
                    filters, referenced, warnings);
        }

        private void checkTimeBase(TimeSpec observedAt) {
            ObservedAtMode mode = context.observedAtMode();
            if (mode == null || observedAt == null) {
                return;
            }
            boolean received = observedAt instanceof TimeSpec.Received;
            String problem = null;
            if (mode == ObservedAtMode.RECEIVED && !received) {
                problem = "must be {\"received\": true}: the source's observedAtMode is RECEIVED";
            } else if (mode == ObservedAtMode.UPSTREAM && received) {
                problem = "must read a time from the input: the source's observedAtMode is UPSTREAM";
            }
            if (problem == null) {
                return;
            }
            if (context.strictTimeBase()) {
                errors.add(new FieldError("mapping.observedAt", problem));
            } else {
                warnings.add(new MappingWarning(MappingWarning.TIME_BASE_MISMATCH, "observedAt " + problem,
                        "mapping.observedAt"));
            }
        }

        // ---------- value specs ----------

        private ValueSpec required(JsonNode node, String name) {
            JsonNode value = node.get(name);
            if (value == null || value.isNull()) {
                errors.add(new FieldError("mapping." + name, "is required"));
                return null;
            }
            return value(value, "mapping." + name);
        }

        private ValueSpec optionalValue(JsonNode value, String where) {
            return value == null || value.isNull() ? null : value(value, where);
        }

        /** A {@code MappingValueSpec}: exactly one of path, template or const. */
        private ValueSpec value(JsonNode node, String where) {
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {\"path\"}, {\"template\"} or {\"const\"}"));
                return null;
            }
            int members = 0;
            ValueSpec out = null;
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                members++;
                switch (entry.getKey()) {
                    case "path" -> out = pathValue(entry.getValue(), where + ".path");
                    case "template" -> out = template(entry.getValue(), where + ".template");
                    case "const" -> {
                        if (!entry.getValue().isValueNode() || entry.getValue().isNull()) {
                            errors.add(new FieldError(where + ".const", "must be a string, number or boolean"));
                        } else {
                            out = new ValueSpec.Const(entry.getValue());
                        }
                    }
                    default -> errors.add(new FieldError(where + "." + entry.getKey(),
                            "is not allowed; use exactly one of path, template or const"));
                }
            }
            if (members != 1) {
                errors.add(new FieldError(where, "must have exactly one of path, template or const"));
                return null;
            }
            return out;
        }

        private ValueSpec pathValue(JsonNode node, String where) {
            if (node == null || !node.isString()) {
                errors.add(new FieldError(where, "must be a JSONPath string"));
                return null;
            }
            return pathSpec(node.asString(), where);
        }

        /** A path string: {@code $pairs.<key>} or a JSONPath. */
        private ValueSpec pathSpec(String text, String where) {
            String trimmed = text.strip();
            if (trimmed.startsWith(PAIRS_PREFIX)) {
                String key = trimmed.substring(PAIRS_PREFIX.length());
                if (!MEMBER_NAME.matcher(key).matches()) {
                    errors.add(new FieldError(where, "must be $pairs.<key> with a plain key"));
                    return null;
                }
                usesPairs = true;
                return new ValueSpec.Pair(key);
            }
            JsonPath path = path(trimmed, where, true);
            return path == null ? null : new ValueSpec.Path(trimmed, path);
        }

        private JsonPath path(String text, String where, boolean reference) {
            try {
                JsonPath path = MappingJsonPath.compile(text);
                if (reference) {
                    referenced.add(firstKeyOf(text.strip()));
                }
                return path;
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError(where, e.getMessage()));
                return null;
            }
        }

        private ValueSpec template(JsonNode node, String where) {
            if (node == null || !node.isString() || node.asString().isEmpty()) {
                errors.add(new FieldError(where, "must be a non-empty string"));
                return null;
            }
            String text = node.asString();
            List<Object> parts = new ArrayList<>();
            Matcher matcher = PLACEHOLDER.matcher(text);
            int last = 0;
            int placeholders = 0;
            while (matcher.find()) {
                if (matcher.start() > last) {
                    parts.add(text.substring(last, matcher.start()));
                }
                ValueSpec inner = pathSpec(matcher.group(1), where + "{{" + matcher.group(1) + "}}");
                if (inner != null) {
                    parts.add(inner);
                }
                placeholders++;
                last = matcher.end();
            }
            if (last < text.length()) {
                parts.add(text.substring(last));
            }
            if (placeholders == 0) {
                errors.add(new FieldError(where, "has no {{path}} placeholder; use const for a fixed value"));
                return null;
            }
            return new ValueSpec.Template(text, parts);
        }

        // ---------- time ----------

        private TimeSpec optionalTime(JsonNode node, String where) {
            return node == null || node.isNull() ? null : time(node, where);
        }

        private TimeSpec time(JsonNode node, String where) {
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {\"path\", \"format\"} or {\"received\": true}"));
                return null;
            }
            if (node.has("received")) {
                if (!node.get("received").isBoolean() || !node.get("received").booleanValue() || node.size() != 1) {
                    errors.add(new FieldError(where + ".received", "must be true and the only member"));
                    return null;
                }
                return new TimeSpec.Received();
            }
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (!Set.of("path", "format", "zone").contains(entry.getKey())) {
                    errors.add(new FieldError(where + "." + entry.getKey(), "is not allowed"));
                }
            }
            ValueSpec value = pathValue(node.get("path"), where + ".path");
            JsonNode formatNode = node.get("format");
            String format = formatNode != null && formatNode.isString() ? formatNode.asString() : null;
            DateTimeFormatter pattern = null;
            if (format == null) {
                errors.add(new FieldError(where + ".format", "is required: iso, epochS, epochMs or pattern:<pattern>"));
            } else if (format.startsWith("pattern:")) {
                try {
                    pattern = DateTimeFormatter.ofPattern(format.substring("pattern:".length()));
                    format = "pattern";
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError(where + ".format", "is not a valid DateTimeFormatter pattern: "
                            + e.getMessage()));
                }
            } else if (!Set.of("iso", "epochS", "epochMs").contains(format)) {
                errors.add(new FieldError(where + ".format", "must be iso, epochS, epochMs or pattern:<pattern>"));
            }
            ZoneId zone = ZoneOffset.UTC;
            JsonNode zoneNode = node.get("zone");
            if (zoneNode != null && !zoneNode.isNull()) {
                try {
                    zone = ZoneId.of(zoneNode.asString());
                } catch (DateTimeException e) {
                    errors.add(new FieldError(where + ".zone", "is not a time zone id"));
                }
            }
            return new TimeSpec.FromValue(value, format, pattern, zone);
        }

        // ---------- geometry, tombstone, quality ----------

        private GeometrySpec geometry(JsonNode node) {
            String where = "mapping.geometry";
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {lon, lat}, {geojson}, {const} or null"));
                return null;
            }
            double[] fallback = null;
            if (node.has("fallback")) {
                fallback = point(node.get("fallback"), where + ".fallback");
            }
            if (node.has("const")) {
                if (node.size() != 1) {
                    errors.add(new FieldError(where, "const must be the only member"));
                }
                double[] point = point(node.get("const"), where + ".const");
                return point == null ? null : new GeometrySpec.ConstPoint(point[0], point[1]);
            }
            if (node.has("geojson")) {
                for (Map.Entry<String, JsonNode> entry : node.properties()) {
                    if (!Set.of("geojson", "fallback").contains(entry.getKey())) {
                        errors.add(new FieldError(where + "." + entry.getKey(), "is not allowed with geojson"));
                    }
                }
                ValueSpec value = pathValue(node.get("geojson"), where + ".geojson");
                return new GeometrySpec.GeoJson(value, fallback);
            }
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (!Set.of("lon", "lat", "fallback").contains(entry.getKey())) {
                    errors.add(new FieldError(where + "." + entry.getKey(), "is not allowed"));
                }
            }
            if (!node.has("lon") || !node.has("lat")) {
                errors.add(new FieldError(where, "needs lon and lat, geojson or const"));
                return null;
            }
            ValueSpec lon = value(node.get("lon"), where + ".lon");
            ValueSpec lat = value(node.get("lat"), where + ".lat");
            return new GeometrySpec.LonLat(lon, lat, fallback);
        }

        private double[] point(JsonNode node, String where) {
            if (node == null || !node.isArray() || node.size() != 2 || !node.get(0).isNumber()
                    || !node.get(1).isNumber()) {
                errors.add(new FieldError(where, "must be [lon, lat]"));
                return null;
            }
            double lon = node.get(0).doubleValue();
            double lat = node.get(1).doubleValue();
            if (!(lon >= -180 && lon <= 180) || !(lat >= -90 && lat <= 90)) {
                errors.add(new FieldError(where, "is out of range"));
                return null;
            }
            return new double[] {lon, lat};
        }

        private CompiledMapping.Tombstone tombstone(JsonNode node) {
            String where = "mapping.tombstone";
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {path, equals | in}"));
                return null;
            }
            ValueSpec value = pathValue(node.get("path"), where + ".path");
            JsonNode equals = node.get("equals");
            JsonNode in = node.get("in");
            if ((equals == null) == (in == null)) {
                errors.add(new FieldError(where, "needs exactly one of equals or in"));
            }
            if (in != null && !in.isArray()) {
                errors.add(new FieldError(where + ".in", "must be an array"));
            }
            List<JsonNode> values = new ArrayList<>();
            if (in != null && in.isArray()) {
                in.forEach(values::add);
            }
            return new CompiledMapping.Tombstone(value, equals, values);
        }

        private CompiledMapping.QualitySpec quality(JsonNode node) {
            String where = "mapping.quality";
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be an object"));
                return null;
            }
            ValueSpec accuracy = null;
            Map<String, ValueSpec> extra = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                switch (entry.getKey()) {
                    case "accuracyMeters" -> accuracy = value(entry.getValue(), where + ".accuracyMeters");
                    case "extra" -> {
                        if (!entry.getValue().isObject()) {
                            errors.add(new FieldError(where + ".extra", "must be an object of value specs"));
                            continue;
                        }
                        for (Map.Entry<String, JsonNode> e : entry.getValue().properties()) {
                            if (!EXTRA_KEY.matcher(e.getKey()).matches()) {
                                errors.add(new FieldError(where + ".extra." + e.getKey(),
                                        "keys must match ^[a-z0-9]+\\.[a-z0-9_]+$"));
                                continue;
                            }
                            extra.put(e.getKey(), value(e.getValue(), where + ".extra." + e.getKey()));
                        }
                    }
                    default -> errors.add(new FieldError(where + "." + entry.getKey(), "is not allowed"));
                }
            }
            return new CompiledMapping.QualitySpec(accuracy, extra);
        }

        // ---------- fields ----------

        private List<CompiledMapping.FieldMapping> fields(JsonNode node, Set<String> drop) {
            List<CompiledMapping.FieldMapping> out = new ArrayList<>();
            if (node == null || node.isNull()) {
                return out;
            }
            if (!node.isArray()) {
                errors.add(new FieldError("mapping.fields", "must be an array"));
                return out;
            }
            if (node.size() > MAX_FIELDS) {
                errors.add(new FieldError("mapping.fields", "must have at most " + MAX_FIELDS + " entries"));
            }
            for (int i = 0; i < node.size(); i++) {
                CompiledMapping.FieldMapping field = field(node.get(i), "mapping.fields[" + i + "]", drop);
                if (field != null) {
                    out.add(field);
                }
            }
            return out;
        }

        private CompiledMapping.FieldMapping field(JsonNode node, String where, Set<String> drop) {
            if (node == null || !node.isObject()) {
                errors.add(new FieldError(where, "must be an object"));
                return null;
            }
            int before = errors.size();
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (!Set.of("name", "path", "type", "unit", "scale", "offset", "default", "sentinels")
                        .contains(entry.getKey())) {
                    errors.add(new FieldError(where + "." + entry.getKey(), "is not allowed"));
                }
            }
            JsonNode nameNode = node.get("name");
            String name = nameNode != null && nameNode.isString() ? nameNode.asString() : null;
            if (name == null || !FIELD_NAME.matcher(name).matches()) {
                errors.add(new FieldError(where + ".name", "must match ^[A-Za-z][A-Za-z0-9_]{0,63}$"));
            } else if (drop.contains(name)) {
                errors.add(new FieldError(where + ".name", "is listed in drop"));
            }
            ValueSpec value = pathValue(node.get("path"), where + ".path");
            JsonNode typeNode = node.get("type");
            String type = typeNode != null && typeNode.isString() ? typeNode.asString() : null;
            if (type == null || !TYPES.contains(type)) {
                errors.add(new FieldError(where + ".type", "must be number, integer, string or boolean"));
            }
            boolean numeric = "number".equals(type) || "integer".equals(type);
            DoubleUnaryOperator conversion = null;
            JsonNode unit = node.get("unit");
            if (unit != null && !unit.isNull()) {
                conversion = unit(unit, where + ".unit");
                if (!numeric) {
                    errors.add(new FieldError(where + ".unit", "needs type number or integer"));
                }
            }
            Double scale = number(node.get("scale"), where + ".scale", numeric);
            Double offset = number(node.get("offset"), where + ".offset", numeric);
            JsonNode defaultValue = node.get("default");
            if (defaultValue != null && (defaultValue.isNull() || !defaultValue.isValueNode())) {
                errors.add(new FieldError(where + ".default", "must be a string, number or boolean"));
            }
            List<JsonNode> sentinels = new ArrayList<>();
            JsonNode sentinelNode = node.get("sentinels");
            if (sentinelNode != null && !sentinelNode.isNull()) {
                if (!sentinelNode.isArray()) {
                    errors.add(new FieldError(where + ".sentinels", "must be an array"));
                } else {
                    sentinelNode.forEach(sentinels::add);
                }
            }
            if (errors.size() > before) {
                return null;
            }
            return new CompiledMapping.FieldMapping(name, value, type, conversion, scale, offset, defaultValue,
                    sentinels);
        }

        private DoubleUnaryOperator unit(JsonNode node, String where) {
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {from, to}"));
                return null;
            }
            String from = node.path("from").isString() ? node.get("from").asString() : null;
            String to = node.path("to").isString() ? node.get("to").asString() : null;
            boolean ok = true;
            if (!Units.known(from)) {
                errors.add(new FieldError(where + ".from", "is not a unit of the built-in table " + Units.CODES));
                ok = false;
            }
            if (!Units.known(to)) {
                errors.add(new FieldError(where + ".to", "is not a unit of the built-in table " + Units.CODES));
                ok = false;
            }
            if (!ok) {
                return null;
            }
            return Units.conversion(from, to).orElseGet(() -> {
                errors.add(new FieldError(where, "converting " + from + " to " + to + " is not in the built-in table"));
                return null;
            });
        }

        private Double number(JsonNode node, String where, boolean numeric) {
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isNumber() || !Double.isFinite(node.doubleValue())) {
                errors.add(new FieldError(where, "must be a finite number"));
                return null;
            }
            if (!numeric) {
                errors.add(new FieldError(where, "needs type number or integer"));
            }
            return node.doubleValue();
        }

        // ---------- pairs, spread, drop, filter ----------

        private CompiledMapping.PairsSpec pairs(JsonNode node) {
            String where = "mapping.pairs";
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {path, key, value}"));
                return null;
            }
            JsonPath path = null;
            if (!node.path("path").isString()) {
                errors.add(new FieldError(where + ".path", "must be a JSONPath string"));
            } else {
                path = path(node.get("path").asString(), where + ".path", false);
            }
            String key = memberName(node.get("key"), where + ".key");
            String value = memberName(node.get("value"), where + ".value");
            return path == null || key == null || value == null ? null : new CompiledMapping.PairsSpec(path, key, value);
        }

        private String memberName(JsonNode node, String where) {
            if (node == null || !node.isString() || !MEMBER_NAME.matcher(node.asString()).matches()) {
                errors.add(new FieldError(where, "must be a member name such as value_type"));
                return null;
            }
            return node.asString();
        }

        private CompiledMapping.SpreadSpec spread(JsonNode node) {
            String where = "mapping.spread";
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isObject()) {
                errors.add(new FieldError(where, "must be {path, type, max}"));
                return null;
            }
            ValueSpec value = pathValue(node.get("path"), where + ".path");
            String type = node.path("type").isString() ? node.get("type").asString() : "number";
            if (!TYPES.contains(type)) {
                errors.add(new FieldError(where + ".type", "must be number, integer, string or boolean"));
            }
            int max = MAX_SPREAD;
            JsonNode maxNode = node.get("max");
            if (maxNode != null && !maxNode.isNull()) {
                if (!maxNode.isIntegralNumber() || maxNode.asInt() < 1 || maxNode.asInt() > MAX_SPREAD) {
                    errors.add(new FieldError(where + ".max", "must be an integer from 1 to " + MAX_SPREAD));
                } else {
                    max = maxNode.asInt();
                }
            }
            return new CompiledMapping.SpreadSpec(value, type, max);
        }

        private Set<String> drop(JsonNode node) {
            Set<String> out = new LinkedHashSet<>();
            if (node == null || node.isNull()) {
                return out;
            }
            if (!node.isArray()) {
                errors.add(new FieldError("mapping.drop", "must be an array of input keys"));
                return out;
            }
            for (int i = 0; i < node.size(); i++) {
                JsonNode item = node.get(i);
                if (item == null || !item.isString() || item.asString().isBlank()) {
                    errors.add(new FieldError("mapping.drop[" + i + "]", "must be a key"));
                } else {
                    out.add(item.asString());
                }
            }
            return out;
        }

        private List<CompiledMapping.Filter> filters(JsonNode node) {
            List<CompiledMapping.Filter> out = new ArrayList<>();
            if (node == null || node.isNull()) {
                return out;
            }
            if (!node.isArray()) {
                errors.add(new FieldError("mapping.filter", "must be an array of conditions"));
                return out;
            }
            for (int i = 0; i < node.size(); i++) {
                String where = "mapping.filter[" + i + "]";
                JsonNode item = node.get(i);
                if (item == null || !item.isObject()) {
                    errors.add(new FieldError(where, "must be {path, op, value}"));
                    continue;
                }
                ValueSpec value = pathValue(item.get("path"), where + ".path");
                String op = item.path("op").isString() ? item.get("op").asString() : null;
                JsonNode operand = item.get("value");
                if (op == null || !OPS.contains(op)) {
                    errors.add(new FieldError(where + ".op", "must be ==, !=, exists or in"));
                    continue;
                }
                if ("in".equals(op) && (operand == null || !operand.isArray())) {
                    errors.add(new FieldError(where + ".value", "must be an array for in"));
                    continue;
                }
                if (("==".equals(op) || "!=".equals(op)) && (operand == null || !operand.isValueNode())) {
                    errors.add(new FieldError(where + ".value", "must be a string, number, boolean or null"));
                    continue;
                }
                if ("exists".equals(op) && operand != null && !operand.isNull() && !operand.isBoolean()) {
                    errors.add(new FieldError(where + ".value", "must be true or false for exists"));
                    continue;
                }
                if (value != null) {
                    out.add(new CompiledMapping.Filter(value, op, operand));
                }
            }
            return out;
        }
    }

    /** The first member name a path reads ({@code $.a.b} → {@code a}, {@code $['a-b']} → {@code a-b}), or null. */
    static String firstKeyOf(String path) {
        if (path == null) {
            return null;
        }
        Matcher matcher = FIRST_KEY.matcher(path.strip());
        if (!matcher.find()) {
            return null;
        }
        for (int g = 1; g <= 3; g++) {
            if (matcher.group(g) != null) {
                String key = matcher.group(g).strip();
                return "*".equals(key) ? null : key;
            }
        }
        return null;
    }
}
