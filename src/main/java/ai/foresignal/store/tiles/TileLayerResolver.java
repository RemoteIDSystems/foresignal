package ai.foresignal.store.tiles;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.system.SystemCanaryAdapter;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Resolves a tile layer name against the source registry (prompt 016).
 *
 * <ul>
 *   <li>A registered source id wins: the layer is that source.
 *   <li>Otherwise a kind declared by any registered source: the layer is every source declaring it.
 *   <li>Otherwise, or when every matching source is hidden from the caller, {@code 404 UNKNOWN_LAYER}.
 * </ul>
 *
 * <p>Sources in {@code excluded} (private sources for anonymous {@code openReads} callers) are
 * removed <em>before</em> anything else is computed, so they contribute no feature, no count, no
 * property and no attribution. Property names are checked against {@link #PROPERTY_PATTERN} because
 * they are interpolated into SQL; an invalid name is dropped with a warning.
 */
@Component
public class TileLayerResolver {

    /** A property name that may be interpolated into SQL as a JSON key. */
    public static final Pattern PROPERTY_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    private static final Logger log = LoggerFactory.getLogger(TileLayerResolver.class);

    /** Sources that never appear as tile layers: the watchdog's synthetic canary (prompt 019). */
    public static final Set<String> HIDDEN_FROM_TILES = Set.of(SystemCanaryAdapter.ID);

    private final SourceRegistry registry;

    /** Creates the resolver. */
    public TileLayerResolver(SourceRegistry registry) {
        this.registry = registry;
    }

    /**
     * The layer named {@code layer} as {@code excluded} sees it.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_LAYER} when the name is neither a source id nor
     *     a declared kind, or every matching source is excluded
     */
    public TileLayer resolve(String layer, Set<String> excluded) {
        return resolve(layer, visible(), excluded)
                .orElseThrow(() -> unknownLayer(layer));
    }

    /**
     * Every layer {@code excluded} can see, for the TileJSON index: each visible source id, then each
     * declared kind with at least one visible source, both sorted. A kind spelled like a source id is
     * left out, because the name resolves to the source.
     */
    public List<TileLayer> layers(Set<String> excluded) {
        return layers(visible(), excluded);
    }

    /** Every registered descriptor except those hidden from tiles ({@link #HIDDEN_FROM_TILES}). */
    private List<SourceDescriptor> visible() {
        return registry.descriptors().stream().filter(d -> !HIDDEN_FROM_TILES.contains(d.id())).toList();
    }

    /** {@link #resolve(String, Set)} over an explicit descriptor list; empty when unknown or hidden. */
    public static Optional<TileLayer> resolve(
            String layer, Collection<SourceDescriptor> descriptors, Set<String> excluded) {
        if (layer == null || layer.isBlank()) {
            return Optional.empty();
        }
        for (SourceDescriptor descriptor : descriptors) {
            if (descriptor.id().equals(layer)) {
                if (excluded.contains(layer)) {
                    return Optional.empty();
                }
                return Optional.of(build(layer, TileLayerType.SOURCE, List.of(descriptor)));
            }
        }
        List<SourceDescriptor> declaring = new ArrayList<>();
        for (SourceDescriptor descriptor : descriptors) {
            if (descriptor.kinds().contains(layer) && !excluded.contains(descriptor.id())) {
                declaring.add(descriptor);
            }
        }
        if (declaring.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(build(layer, TileLayerType.KIND, declaring));
    }

    /** {@link #layers(Set)} over an explicit descriptor list. */
    public static List<TileLayer> layers(Collection<SourceDescriptor> descriptors, Set<String> excluded) {
        Set<String> sourceIds = new TreeSet<>();
        Set<String> kinds = new TreeSet<>();
        for (SourceDescriptor descriptor : descriptors) {
            if (excluded.contains(descriptor.id())) {
                continue;
            }
            sourceIds.add(descriptor.id());
            kinds.addAll(descriptor.kinds());
        }
        kinds.removeAll(allIds(descriptors));
        // EDP atomic kinds (prompt 024) stay requestable by name but are left out of the index
        // unless some visible source shows them as an ordinary layer.
        kinds.removeIf(kind -> hiddenEverywhere(kind, descriptors, excluded));
        List<TileLayer> layers = new ArrayList<>();
        for (String id : sourceIds) {
            resolve(id, descriptors, excluded).ifPresent(layers::add);
        }
        for (String kind : kinds) {
            resolve(kind, descriptors, excluded).ifPresent(layers::add);
        }
        return List.copyOf(layers);
    }

    /** True when every visible source declaring {@code kind} hides it from the layer lists. */
    private static boolean hiddenEverywhere(
            String kind, Collection<SourceDescriptor> descriptors, Set<String> excluded) {
        boolean declared = false;
        for (SourceDescriptor descriptor : descriptors) {
            if (excluded.contains(descriptor.id()) || !descriptor.kinds().contains(kind)) {
                continue;
            }
            declared = true;
            if (!descriptor.hidesKind(kind)) {
                return false;
            }
        }
        return declared;
    }

    /** True when {@code name} may be interpolated into SQL as a property name. */
    public static boolean validProperty(String name) {
        return name != null && PROPERTY_PATTERN.matcher(name).matches();
    }

    /** The {@code 404 UNKNOWN_LAYER} problem for {@code layer}. */
    public static ForesignalException unknownLayer(String layer) {
        return new ForesignalException(
                HttpStatus.NOT_FOUND,
                ErrorCodes.UNKNOWN_LAYER,
                "No tile layer '" + layer + "': it is neither a registered source id nor a kind of a visible source.");
    }

    private static Set<String> allIds(Collection<SourceDescriptor> descriptors) {
        Set<String> ids = new TreeSet<>();
        for (SourceDescriptor descriptor : descriptors) {
            ids.add(descriptor.id());
        }
        return ids;
    }

    /**
     * The tile layer of a saved view (prompt 036): the visible sources among {@code viewSources}
     * (every visible source when empty), named after the view.
     *
     * @param id the layer id (the view slug)
     * @param name the view name
     * @param viewSources the view's sources; empty for all
     * @param excluded sources the caller may not see
     * @return the layer; its source list may be empty (the tile is then empty)
     */
    public TileLayer viewLayer(String id, String name, Collection<String> viewSources, Set<String> excluded) {
        List<SourceDescriptor> matched = new ArrayList<>();
        for (SourceDescriptor descriptor : visible()) {
            if (!excluded.contains(descriptor.id()) && (viewSources.isEmpty() || viewSources.contains(descriptor.id()))) {
                matched.add(descriptor);
            }
        }
        TileLayer built = build(id, TileLayerType.VIEW, matched);
        return new TileLayer(id, TileLayerType.VIEW, name, "Current records of the saved view " + name,
                built.sources(), built.tileProperties(), built.aggBelowZoom(), built.rankFields());
    }

    private static TileLayer build(String id, TileLayerType type, List<SourceDescriptor> matched) {
        List<SourceDescriptor> sources = new ArrayList<>(matched);
        sources.sort(Comparator.comparing(SourceDescriptor::id));

        Set<String> properties = new LinkedHashSet<>();
        Integer aggBelowZoom = null;
        Map<String, String> rankFields = new LinkedHashMap<>();
        boolean everySourceRanks = true;
        for (SourceDescriptor source : sources) {
            for (String property : source.tileProperties()) {
                if (!validProperty(property)) {
                    log.warn("Source {} declares tile property '{}', which is not a valid property name"
                            + " (^[A-Za-z0-9_]{1,64}$); it is left out of tiles", source.id(), property);
                } else if (!TileLayer.BASE_PROPERTIES.contains(property)) {
                    properties.add(property);
                }
            }
            if (source.aggBelowZoom() != null && (aggBelowZoom == null || source.aggBelowZoom() < aggBelowZoom)) {
                aggBelowZoom = source.aggBelowZoom();
            }
            String rank = numericRankField(source);
            if (rank == null) {
                everySourceRanks = false;
            } else {
                rankFields.put(source.id(), rank);
            }
        }
        if (!everySourceRanks) {
            rankFields.clear();
        }

        String name;
        String description;
        if (type == TileLayerType.SOURCE && !sources.isEmpty()) {
            SourceDescriptor source = sources.getFirst();
            name = source.name() == null || source.name().isBlank() ? source.id() : source.name();
            description = source.description() == null || source.description().isBlank()
                    ? "Current records of " + name
                    : source.description();
        } else {
            name = id;
            description = "Current records of kind " + id + " from " + String.join(", ",
                    sources.stream().map(SourceDescriptor::id).toList());
        }
        return new TileLayer(id, type, name, description, sources, List.copyOf(properties), aggBelowZoom, rankFields);
    }

    /**
     * The source's {@code rankField} when it is a valid property name and not declared as anything
     * but a number in {@code fieldSpecs}; otherwise null.
     */
    static String numericRankField(SourceDescriptor source) {
        String rank = source.rankField();
        if (!validProperty(rank)) {
            return null;
        }
        FieldSpec spec = source.fieldSpecs().get(rank);
        if (spec != null && spec.type() != FieldType.NUMBER && spec.type() != FieldType.INTEGER) {
            return null;
        }
        return rank;
    }
}
