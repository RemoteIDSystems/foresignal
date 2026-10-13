package ai.foresignal.mapping.preset;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.MotionSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * The built-in device presets (prompt 059): every {@code classpath*:mapping/presets/*.json}, loaded once.
 *
 * <p>Static, like {@code SpriteCatalog}'s icon names, because {@code CustomSourceDescriptors} (a static builder)
 * reads a preset's descriptor defaults. {@link #apply} fills a {@code CustomSourceInput} from a preset;
 * {@link #setup} renders its device instructions.
 */
public final class PresetCatalog {

    /** The resource pattern of the preset files. */
    public static final String RESOURCES = "classpath*:mapping/presets/*.json";

    /** Members of {@code CustomSourceInput} a preset fills when the input leaves them out. */
    static final List<String> FILLED = List.of("kinds", "fieldSpecs", "observedAtMode", "significantFields",
            "volatileFields", "private", "allowPathKey", "tileProperties", "titleField");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final class Holder {
        static final Map<String, MappingPreset> PRESETS = load();
    }

    private PresetCatalog() {}

    /** Every preset, ordered by id. */
    public static List<MappingPreset> all() {
        return List.copyOf(Holder.PRESETS.values());
    }

    /** The preset {@code id}, or empty. */
    public static Optional<MappingPreset> find(String id) {
        return Optional.ofNullable(id == null ? null : Holder.PRESETS.get(id));
    }

    /** The preset ids. */
    public static Set<String> ids() {
        return Holder.PRESETS.keySet();
    }

    // ---------- applying a preset ----------

    /**
     * {@code body} (a {@code CustomSourceInput}) filled from {@code preset}: members the body leaves out take the
     * preset's values, the mapping and format options get the location parameter, and {@code presetParams} is
     * checked against the preset's parameters.
     *
     * @return the merged body, without {@code presetParams}
     * @throws ValidationException listing unknown or malformed parameters
     */
    public static ObjectNode apply(ObjectNode body, MappingPreset preset) {
        ObjectNode out = body.deepCopy();
        JsonNode document = preset.document();
        List<FieldError> errors = new ArrayList<>();
        Params params = params(body.get("presetParams"), preset, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The preset parameters are not valid.", errors);
        }
        out.remove("presetParams");
        if (absent(out, "name")) {
            out.put("name", preset.title());
        }
        if (absent(out, "description") && document.hasNonNull("description")) {
            out.put("description", document.get("description").asString());
        }
        for (String member : FILLED) {
            String from = member;
            if (absent(out, member) && document.hasNonNull(from)) {
                out.set(member, document.get(from).deepCopy());
            }
        }
        if (absent(out, "recordTtl") && document.hasNonNull("ttl")) {
            out.put("recordTtl", document.get("ttl").asString());
        }
        if (absent(out, "mapping") && preset.mapping() != null) {
            ObjectNode mapping = (ObjectNode) preset.mapping().deepCopy();
            if (params.location != null && "const".equals(preset.locationMode())) {
                mapping.set("geometry", pointObject("const", params.location));
            } else if (params.location != null && "fallback".equals(preset.locationMode())
                    && mapping.get("geometry") instanceof ObjectNode geometry) {
                geometry.set("fallback", point(params.location));
            }
            out.set("mapping", mapping);
        }
        ObjectNode options = formatOptions(out, preset);
        if (options != null) {
            if (params.location != null && "station".equals(preset.locationMode())) {
                options.set("station", point(params.location));
            }
            if (params.tombstoneOnResolve) {
                options.put("resolved", "tombstone");
            }
        }
        return out;
    }

    /** The {@code formats.{format}} object of {@code body}, created from the preset's options, or null. */
    private static ObjectNode formatOptions(ObjectNode body, MappingPreset preset) {
        JsonNode defaults = preset.document().get("formatOptions");
        if (defaults == null || !defaults.isObject()) {
            return null;
        }
        ObjectNode formats = body.get("formats") instanceof ObjectNode f ? f : body.putObject("formats");
        ObjectNode options = formats.get(preset.format()) instanceof ObjectNode o ? o : formats.putObject(preset.format());
        for (Map.Entry<String, JsonNode> entry : defaults.properties()) {
            if (!options.has(entry.getKey())) {
                options.set(entry.getKey(), entry.getValue().deepCopy());
            }
        }
        return options;
    }

    /** The checked parameters of a preset. */
    private record Params(double[] location, boolean tombstoneOnResolve) {}

    private static Params params(JsonNode node, MappingPreset preset, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return new Params(null, false);
        }
        if (!node.isObject()) {
            errors.add(new FieldError("presetParams", "must be an object"));
            return new Params(null, false);
        }
        List<String> known = preset.paramNames();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            if (!known.contains(entry.getKey())) {
                errors.add(new FieldError("presetParams." + entry.getKey(), "is not a parameter of preset "
                        + preset.id() + " " + known));
            }
        }
        Double lon = coordinate(node.get("lon"), "presetParams.lon", 180, errors);
        Double lat = coordinate(node.get("lat"), "presetParams.lat", 90, errors);
        if ((lon == null) != (lat == null)) {
            errors.add(new FieldError("presetParams", "lon and lat must be given together"));
        }
        boolean exact = bool(node.get("exactLocation"), "presetParams.exactLocation", errors);
        boolean tombstone = bool(node.get("tombstoneOnResolve"), "presetParams.tombstoneOnResolve", errors);
        double[] location = null;
        if (lon != null && lat != null) {
            location = exact ? new double[] {lon, lat} : new double[] {round(lon), round(lat)};
        }
        return new Params(location, tombstone);
    }

    /** {@code value} rounded to 0.01° (about 1 km), the default precision of a preset location. */
    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static Double coordinate(JsonNode node, String where, double limit, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber() || !(Math.abs(node.doubleValue()) <= limit)) {
            errors.add(new FieldError(where, "must be a number from -" + (int) limit + " to " + (int) limit));
            return null;
        }
        return node.doubleValue();
    }

    private static boolean bool(JsonNode node, String where, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return false;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(where, "must be a boolean"));
            return false;
        }
        return node.booleanValue();
    }

    private static ArrayNode point(double[] location) {
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        out.add(location[0]);
        out.add(location[1]);
        return out;
    }

    private static ObjectNode pointObject(String member, double[] location) {
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        out.set(member, point(location));
        return out;
    }

    private static boolean absent(JsonNode body, String member) {
        JsonNode value = body.get(member);
        return value == null || value.isNull();
    }

    // ---------- setup ----------

    /**
     * The {@code MappingPresetSetup} of {@code preset}.
     *
     * @param base the public base URL (no trailing slash)
     * @param sourceId the source id, or null to keep the {@code {sourceId}} placeholder (catalog listing)
     * @param key the plaintext ingest key, or null to keep the {@code {key}} placeholder
     * @return {@code {instructions, urlTemplate, url}}; {@code url} is null without a source id and key
     */
    public static Map<String, Object> setup(MappingPreset preset, String base, String sourceId, String key) {
        String root = base == null ? "" : (base.endsWith("/") ? base.substring(0, base.length() - 1) : base);
        String template = preset.urlTemplate().replace("{publicBaseUrl}", root);
        String url = template;
        if (sourceId != null) {
            url = url.replace("{sourceId}", sourceId);
        }
        if (key != null) {
            url = url.replace("{key}", key);
        }
        String path = url.startsWith(root) ? url.substring(root.length()) : url;
        String host = "";
        String port = "";
        String scheme = "";
        try {
            URI uri = URI.create(root.isEmpty() ? "http://localhost" : root);
            host = uri.getHost() == null ? "" : uri.getHost();
            scheme = uri.getScheme() == null ? "http" : uri.getScheme().toLowerCase(Locale.ROOT);
            port = Integer.toString(uri.getPort() > 0 ? uri.getPort() : "https".equals(scheme) ? 443 : 80);
        } catch (IllegalArgumentException e) {
            // A malformed publicBaseUrl leaves host and port blank; the URL itself is still shown.
        }
        String instructions = preset.instructions()
                .replace("{url}", url)
                .replace("{host}", host)
                .replace("{port}", port)
                .replace("{path}", path)
                .replace("{scheme}", scheme);
        if (sourceId != null) {
            instructions = instructions.replace("{sourceId}", sourceId);
        }
        if (key != null) {
            instructions = instructions.replace("{key}", key);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("instructions", instructions);
        out.put("urlTemplate", template);
        out.put("url", sourceId != null && key != null ? url : null);
        return out;
    }

    /**
     * The {@code MappingPreset} JSON of {@code preset} for {@code listMappingPresets}: the spec's members, with
     * the setup rendered for {@code base}.
     */
    public static Map<String, Object> view(MappingPreset preset, String base) {
        JsonNode d = preset.document();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", preset.id());
        out.put("title", preset.title());
        out.put("description", d.path("description").asString(""));
        out.put("format", preset.format());
        out.put("mapping", preset.mapping());
        out.put("kinds", d.get("kinds"));
        out.put("fieldSpecs", d.has("fieldSpecs") ? d.get("fieldSpecs") : JsonNodeFactory.instance.objectNode());
        out.put("observedAtMode", d.path("observedAtMode").asString("UPSTREAM"));
        out.put("ttl", d.hasNonNull("ttl") ? d.get("ttl").asString() : null);
        out.put("ingestPolicy", d.hasNonNull("ingestPolicy") ? d.get("ingestPolicy") : null);
        out.put("significantFields", d.path("significantFields"));
        out.put("volatileFields", d.path("volatileFields"));
        out.put("private", d.path("private").asBoolean(false));
        out.put("allowPathKey", d.path("allowPathKey").asBoolean(false));
        out.put("allowsGet", preset.allowsGet());
        out.put("params", d.path("params"));
        out.put("setup", setup(preset, base, null, null));
        return out;
    }

    // ---------- loading ----------

    private static Map<String, MappingPreset> load() {
        Map<String, MappingPreset> out = new java.util.TreeMap<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver(PresetCatalog.class.getClassLoader())
                    .getResources(RESOURCES);
            for (Resource resource : resources) {
                try (InputStream in = resource.getInputStream()) {
                    MappingPreset preset = parse(JSON.readTree(in));
                    if (out.putIfAbsent(preset.id(), preset) != null) {
                        throw new IllegalStateException("Duplicate mapping preset id '" + preset.id() + "'");
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the mapping presets", e);
        }
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(out));
    }

    /**
     * Parses one preset file.
     *
     * @throws IllegalStateException when the file is malformed
     */
    static MappingPreset parse(JsonNode d) {
        String id = d.path("id").asString(null);
        String format = d.path("format").asString(null);
        if (id == null || format == null || !d.path("kinds").isArray() || !d.path("setup").isObject()) {
            throw new IllegalStateException("A mapping preset needs id, format, kinds and setup: " + id);
        }
        JsonNode mapping = d.hasNonNull("mapping") ? d.get("mapping") : null;
        IngestPolicy policy = policy(d.get("ingestPolicy"), id);
        Duration retention = d.hasNonNull("retention") ? Durations.parse(d.get("retention").asString()) : null;
        List<FieldThreshold> thresholds = new ArrayList<>();
        d.path("thresholds").forEach(t -> thresholds.add(new FieldThreshold(t.path("field").asString(),
                t.path("delta").asDouble(), t.path("angular").asBoolean(false))));
        MotionSpec motion = null;
        JsonNode m = d.get("motion");
        if (m != null && m.isObject()) {
            motion = new MotionSpec(m.path("speedField").asString(), MotionSpec.SpeedUnit.valueOf(
                    m.path("speedUnit").asString("KNOTS")), m.path("courseField").asString());
        }
        String locationMode = d.hasNonNull("locationMode") ? d.get("locationMode").asString() : null;
        return new MappingPreset(id, d, format, mapping, policy, retention, thresholds, motion,
                d.path("allowsGet").asBoolean(false), d.path("mergeCurrent").asBoolean(false), locationMode);
    }

    /** The {@code IngestPolicy} of a preset file (durations in shorthand), or null. */
    static IngestPolicy policy(JsonNode node, String id) {
        if (node == null || node.isNull()) {
            return null;
        }
        String mode = node.path("mode").asString("");
        Duration keyframe = duration(node.get("keyframe"));
        Duration edge = duration(node.get("edgeMinInterval"));
        return switch (mode) {
            case "ALL" -> new IngestPolicy.All(keyframe);
            case "LATEST" -> new IngestPolicy.Latest(keyframe, edge);
            case "SAMPLED" -> new IngestPolicy.Sampled(duration(node.get("minInterval")), keyframe,
                    node.hasNonNull("minDeviationMeters") ? node.get("minDeviationMeters").asDouble() : null,
                    node.path("thresholds").asBoolean(false), edge);
            default -> throw new IllegalStateException("Mapping preset " + id + " has an unknown policy mode " + mode);
        };
    }

    private static Duration duration(JsonNode node) {
        return node == null || node.isNull() ? null : Durations.parse(node.asString());
    }
}
