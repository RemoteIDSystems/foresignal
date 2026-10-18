package ai.foresignal.plugin.feed;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.plugin.PluginManifest;
import ai.foresignal.source.IngestPolicyCodec;
import ai.foresignal.source.kit.KitSpecLoader;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loads and checks every {@code wasm-feed} spec once at startup (prompt 141): {@code ${FORESIGNAL_DATA_DIR}/wasm-feeds/*.json},
 * each file a JSON array of {@link WasmFeedSpec}s, read in file-name order with links refused and unknown properties
 * refused ({@link KitSpecLoader}). A missing directory holds no specs, so the broker runs unchanged without them.
 *
 * <p>Any problem stops startup with every problem listed ({@link IllegalStateException}), the rule of the kit
 * overlay (prompt 073): an operator who wrote a spec wants to hear at once that it is wrong, not find a source that
 * never runs. The source registry then applies the descriptor validator to every built descriptor.
 *
 * <p><strong>Checks:</strong> ids {@code wasm-…} and unique; a valid plugin name; an https URL without user info that
 * the outbound URL policy ({@code PUBLIC_ONLY}, prompt 035) allows (a host that does not resolve now is accepted and
 * checked again before every fetch); a name and at least one kind; known enumerations; durations that parse; a budget
 * of positive numbers; a response cap of 1 byte to 16 MB; an ingest policy in the API's shape.
 */
@Component
public class WasmFeedSpecs {

    private static final Logger log = LoggerFactory.getLogger(WasmFeedSpecs.class);

    /** The spec folder under the data directory. */
    public static final String FOLDER = "wasm-feeds";

    /** The source id prefix. */
    public static final String ID_PREFIX = "wasm-";

    /** A source id: {@code wasm-} then lower case, digits and hyphens, at most 64 characters in all. */
    public static final Pattern ID = Pattern.compile("^wasm-[a-z0-9][a-z0-9-]{0,58}$");

    /** A kind. */
    static final Pattern KIND = Pattern.compile("^[a-z0-9][a-z0-9-]{0,63}$");

    private final List<WasmFeedSource> sources;

    /**
     * Loads the specs from the data directory.
     *
     * @param properties supplies the data directory
     * @param policy the outbound URL policy
     * @param codec reads ingest policies
     * @throws IllegalStateException when any spec is invalid
     */
    @Autowired
    public WasmFeedSpecs(ForesignalProperties properties, ObjectProvider<OutboundUrlPolicy> policy,
            IngestPolicyCodec codec) {
        this(properties.dataDir().resolve(FOLDER), policy == null ? () -> null : policy::getIfAvailable, codec);
    }

    /**
     * Loads the specs from a directory (tests).
     *
     * @param dir the spec directory; it need not exist
     * @param policy supplies the outbound URL policy, or returns null when it is unavailable
     * @param codec reads ingest policies
     * @throws IllegalStateException when any spec is invalid
     */
    public WasmFeedSpecs(Path dir, Supplier<OutboundUrlPolicy> policy, IngestPolicyCodec codec) {
        List<String> problems = new ArrayList<>();
        List<KitSpecLoader.Loaded<WasmFeedSpec>> loaded = KitSpecLoader.directory(dir, WasmFeedSpec.class, problems);
        List<WasmFeedSource> built = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (KitSpecLoader.Loaded<WasmFeedSpec> l : loaded) {
            List<String> specProblems = problems(l.spec(), policy == null ? null : policy.get(), codec);
            if (l.spec().id() != null && !ids.add(l.spec().id())) {
                specProblems.add("id: '" + l.spec().id() + "' is used by more than one spec");
            }
            if (!specProblems.isEmpty()) {
                specProblems.forEach(p -> problems.add(l.where() + " (" + l.spec().id() + "): " + p));
                continue;
            }
            try {
                IngestPolicy ingestPolicy = codec.fromTree(l.spec().ingestPolicy());
                built.add(new WasmFeedSource(l.spec(), WasmFeedDescriptors.descriptor(l.spec(), ingestPolicy)));
            } catch (RuntimeException e) {
                problems.add(l.where() + " (" + l.spec().id() + "): the descriptor cannot be built: " + e.getMessage());
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Foresignal cannot start: " + problems.size()
                    + " wasm-feed spec problem(s).\n  - " + String.join("\n  - ", problems));
        }
        this.sources = List.copyOf(built);
        if (!sources.isEmpty()) {
            log.info("Loaded {} wasm-feed spec(s) from {}", sources.size(), dir);
        }
    }

    /** Every loaded source, in load order. */
    public List<WasmFeedSource> sources() {
        return sources;
    }

    /** The ids of the sources naming {@code plugin}, in load order. */
    public List<String> sourceIds(String plugin) {
        return sources.stream().filter(s -> s.spec().plugin().equals(plugin)).map(s -> s.spec().id()).toList();
    }

    /**
     * Every problem with one spec, in field order.
     *
     * @param spec the spec
     * @param policy the outbound URL policy, or null when it is unavailable
     * @param codec reads ingest policies
     * @return the problems; empty when the spec is valid
     */
    static List<String> problems(WasmFeedSpec spec, OutboundUrlPolicy policy, IngestPolicyCodec codec) {
        List<String> problems = new ArrayList<>();
        if (spec.id() == null || !ID.matcher(spec.id()).matches()) {
            problems.add("id: must match " + ID.pattern());
        }
        if (spec.name() == null || spec.name().isBlank()) {
            problems.add("name: is required");
        }
        if (spec.plugin() == null || !PluginManifest.NAME.matcher(spec.plugin()).matches()) {
            problems.add("plugin: must name a plugin directory (" + PluginManifest.NAME.pattern() + ")");
        }
        checkUrl(spec.url(), policy, problems);
        if (spec.kinds().isEmpty()) {
            problems.add("kinds: at least one kind is required");
        }
        for (String kind : spec.kinds()) {
            if (kind == null || !KIND.matcher(kind).matches()) {
                problems.add("kinds: '" + kind + "' must match " + KIND.pattern());
            }
        }
        checkEnum("historyTier", spec.historyTier(), ai.foresignal.source.spi.HistoryTier.class, problems);
        checkEnum("observedAtMode", spec.observedAtMode(), ai.foresignal.source.spi.ObservedAtMode.class, problems);
        checkEnum("ttlAnchor", spec.ttlAnchor(), ai.foresignal.source.spi.TtlAnchor.class, problems);
        checkEnum("egress", spec.egress(), ai.foresignal.source.spi.Egress.class, problems);
        checkDuration("recordTtl", spec.recordTtl(), problems);
        checkDuration("interval", spec.interval(), problems);
        checkDuration("minInterval", spec.minInterval(), problems);
        checkDuration("retention", spec.retention(), problems);
        WasmFeedSpec.Budget budget = spec.budget();
        if (budget != null) {
            if (budget.perMinute() != null && budget.perMinute() < 1) {
                problems.add("budget.perMinute: must be at least 1");
            }
            if (budget.perDay() != null && budget.perDay() < 1) {
                problems.add("budget.perDay: must be at least 1");
            }
            if (budget.mbPerDay() != null && budget.mbPerDay() < 1) {
                problems.add("budget.mbPerDay: must be at least 1");
            }
        }
        Integer max = spec.maxResponseBytes();
        if (max != null && (max < 1 || max > WasmFeedDescriptors.MAX_RESPONSE_BYTES)) {
            problems.add("maxResponseBytes: must be 1 to " + WasmFeedDescriptors.MAX_RESPONSE_BYTES
                    + " (the plugin input cap)");
        }
        if (spec.ingestPolicy() != null && !spec.ingestPolicy().isNull()) {
            try {
                codec.fromTree(spec.ingestPolicy());
            } catch (InvalidInputException e) {
                problems.add("ingestPolicy: " + e.getMessage());
            }
        }
        return problems;
    }

    private static void checkUrl(String url, OutboundUrlPolicy policy, List<String> problems) {
        if (url == null || url.isBlank()) {
            problems.add("url: is required");
            return;
        }
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            problems.add("url: is not a URL");
            return;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            problems.add("url: must be an absolute https URL");
            return;
        }
        if (uri.getRawUserInfo() != null) {
            problems.add("url: must not carry user info");
            return;
        }
        if (policy == null) {
            problems.add("url: the outbound URL policy is unavailable, so the URL cannot be checked");
            return;
        }
        OutboundUrlPolicy.Result result = policy.check(uri, OutboundUrlPolicy.Profile.PUBLIC_ONLY);
        if (result instanceof OutboundUrlPolicy.Denied denied && !denied.unresolved()) {
            problems.add("url: refused by the outbound URL policy: " + denied.reason());
        }
    }

    private static <E extends Enum<E>> void checkEnum(String field, String value, Class<E> type,
            List<String> problems) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            Enum.valueOf(type, value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            problems.add(field + ": '" + value + "' is not one of " + java.util.Arrays.toString(type.getEnumConstants()));
        }
    }

    private static void checkDuration(String field, String value, List<String> problems) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            ai.foresignal.settings.Durations.parsePositive(value);
        } catch (InvalidInputException e) {
            problems.add(field + ": " + e.getMessage());
        }
    }

    /**
     * One loaded source.
     *
     * @param spec the spec
     * @param descriptor its descriptor
     */
    public record WasmFeedSource(WasmFeedSpec spec, SourceDescriptor descriptor) {}
}
