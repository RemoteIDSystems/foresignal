package ai.foresignal.raster.render;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.source.spi.ProductSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The versioned style registry (prompt 045; Weather-Imagery-Design §6.3): every
 * {@code classpath:raster/styles/{name}@{version}.json}, compiled per product into a palette and a
 * code table ({@link CompiledStyle}).
 *
 * <p>Only registered names are accepted: {@code name@v} names one version and is content-addressed;
 * a bare {@code name} resolves to the current (highest) version and is not. Anything else is
 * {@code 400 UNKNOWN_STYLE}. TileJSON, frames, {@code /loop} and SSE payloads always carry the
 * versioned form ({@link #versioned}).
 */
@Component
public class StyleRegistry {

    /** Where the style files live. */
    public static final String LOCATION = "classpath*:raster/styles/*.json";

    static final Pattern REF = Pattern.compile("^([a-z0-9-]+)(?:@([0-9]{1,6}))?$");
    static final Pattern FILE = Pattern.compile("^([a-z0-9-]+)@([0-9]{1,6})\\.json$");

    private static volatile StyleRegistry shipped;

    private final Map<String, NavigableMap<Integer, StyleDefinition>> styles;
    private final Map<String, CompiledStyle> compiled = new ConcurrentHashMap<>();

    /**
     * A resolved style reference.
     *
     * @param style the style version
     * @param explicitVersion true when the request named the version ({@code name@v})
     */
    public record Resolved(StyleDefinition style, boolean explicitVersion) {

        /** {@code name@v}. */
        public String ref() {
            return style.ref();
        }
    }

    /**
     * Loads every shipped style.
     *
     * @throws IllegalStateException when a style file is invalid, so startup fails
     */
    public StyleRegistry() {
        this(load());
    }

    /** A registry over {@code definitions} (tests). */
    public StyleRegistry(List<StyleDefinition> definitions) {
        Map<String, NavigableMap<Integer, StyleDefinition>> byName = new TreeMap<>();
        for (StyleDefinition d : definitions) {
            StyleDefinition previous = byName.computeIfAbsent(d.id(), k -> new TreeMap<>()).put(d.version(), d);
            if (previous != null) {
                throw new IllegalStateException("Style " + d.ref() + " is defined twice");
            }
        }
        this.styles = Collections.unmodifiableMap(byName);
    }

    /** The shipped registry, loaded once (for static URL builders such as {@code ProductUrls}). */
    public static StyleRegistry shipped() {
        StyleRegistry r = shipped;
        if (r == null) {
            synchronized (StyleRegistry.class) {
                r = shipped;
                if (r == null) {
                    r = new StyleRegistry();
                    shipped = r;
                }
            }
        }
        return r;
    }

    /** Reads and validates every style file on the classpath. */
    static List<StyleDefinition> load() {
        JsonMapper json = JsonMapper.builder().build();
        List<StyleDefinition> out = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        try {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(LOCATION)) {
                String name = resource.getFilename();
                Matcher m = name == null ? null : FILE.matcher(name);
                if (m == null || !m.matches()) {
                    problems.add(name + ": style files are named {name}@{version}.json");
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    StyleDefinition d = parse(json.readValue(in, new TypeReference<Map<String, Object>>() {}));
                    if (!d.id().equals(m.group(1)) || d.version() != Integer.parseInt(m.group(2))) {
                        problems.add(name + ": id and version must match the file name");
                    }
                    CompiledStyle.compile(d, probeSpec(d));
                    out.add(d);
                } catch (RuntimeException e) {
                    problems.add(name + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list " + LOCATION, e);
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid raster style(s):\n  - " + String.join("\n  - ", problems));
        }
        return out;
    }

    /** A u8 spec used only to check that a style compiles within 256 colors. */
    private static ProductSpec probeSpec(StyleDefinition d) {
        return ProductSpec.builder("style-probe").dtype(d.isPassthrough()
                ? ai.foresignal.source.spi.RasterDtype.RGBA8 : ai.foresignal.source.spi.RasterDtype.U8).build();
    }

    /** Parses one style document. */
    @SuppressWarnings("unchecked")
    static StyleDefinition parse(Map<String, Object> m) {
        List<StyleDefinition.Stop> stops = new ArrayList<>();
        for (Map<String, Object> s : (List<Map<String, Object>>) m.getOrDefault("stops", List.of())) {
            stops.add(new StyleDefinition.Stop(((Number) s.get("value")).doubleValue(), (String) s.get("color"),
                    (String) s.get("label")));
        }
        List<StyleDefinition.Category> categories = new ArrayList<>();
        for (Map<String, Object> c : (List<Map<String, Object>>) m.getOrDefault("categories", List.of())) {
            categories.add(new StyleDefinition.Category(((Number) c.get("code")).intValue(), (String) c.get("color"),
                    (String) c.get("label")));
        }
        List<StyleDefinition.Special> specials = new ArrayList<>();
        for (Map<String, Object> s : (List<Map<String, Object>>) m.getOrDefault("specials", List.of())) {
            specials.add(new StyleDefinition.Special((String) s.get("kind"), (String) s.get("color"),
                    (String) s.get("label")));
        }
        List<StyleDefinition.Meaning> meaning = new ArrayList<>();
        for (Map<String, Object> s : (List<Map<String, Object>>) m.getOrDefault("meaning", List.of())) {
            meaning.add(new StyleDefinition.Meaning(number(s.get("min")), number(s.get("max")), (String) s.get("text")));
        }
        Object version = m.get("version");
        return new StyleDefinition((String) m.get("id"), version instanceof Number n ? n.intValue() : 0,
                (String) m.get("type"), (String) m.get("units"), stops, categories, specials, meaning,
                (String) m.get("notice"), (String) m.get("passthrough"));
    }

    private static Double number(Object v) {
        return v instanceof Number n ? n.doubleValue() : null;
    }

    /**
     * Resolves {@code ref}.
     *
     * @throws ForesignalException {@code 400 UNKNOWN_STYLE} for an unregistered or malformed name
     */
    public Resolved resolve(String ref) {
        return find(ref).orElseThrow(() -> unknown(ref));
    }

    /** Resolves {@code ref}, if registered. */
    public Optional<Resolved> find(String ref) {
        if (ref == null) {
            return Optional.empty();
        }
        Matcher m = REF.matcher(ref.strip());
        if (!m.matches()) {
            return Optional.empty();
        }
        NavigableMap<Integer, StyleDefinition> versions = styles.get(m.group(1));
        if (versions == null || versions.isEmpty()) {
            return Optional.empty();
        }
        if (m.group(2) == null) {
            return Optional.of(new Resolved(versions.lastEntry().getValue(), false));
        }
        StyleDefinition d = versions.get(Integer.parseInt(m.group(2)));
        return d == null ? Optional.empty() : Optional.of(new Resolved(d, true));
    }

    /**
     * The versioned form of {@code ref}: {@code name@current} for a bare registered name, the input
     * otherwise (an unregistered name is left for the request path to refuse).
     */
    public String versioned(String ref) {
        if (ref == null) {
            return null;
        }
        return find(ref).map(Resolved::ref).orElse(ref);
    }

    /** True when {@code style} is its name's current version. */
    public boolean isCurrent(StyleDefinition style) {
        NavigableMap<Integer, StyleDefinition> versions = styles.get(style.id());
        return versions != null && versions.lastKey() == style.version();
    }

    /** Every registered version, by name then version. */
    public List<StyleDefinition> all() {
        List<StyleDefinition> out = new ArrayList<>();
        styles.values().forEach(v -> out.addAll(v.values()));
        return out;
    }

    /** {@code style} compiled for {@code spec}, cached. */
    public CompiledStyle compiled(StyleDefinition style, ProductSpec spec) {
        String key = style.ref() + "|" + spec.id() + "|" + spec.hashCode();
        return compiled.computeIfAbsent(key, k -> CompiledStyle.compile(style, spec));
    }

    /** Forgets the compiled tables of {@code productId} (its spec changed). */
    public void forgetProduct(String productId) {
        compiled.keySet().removeIf(k -> k.contains("|" + productId + "|"));
    }

    /** {@code 400 UNKNOWN_STYLE}. */
    public static ForesignalException unknown(String ref) {
        return new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.UNKNOWN_STYLE,
                "Style '" + ref + "' is not registered; GET /v1/styles lists the registered styles.");
    }
}
