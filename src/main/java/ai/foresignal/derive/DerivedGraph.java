package ai.foresignal.derive;

import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * The derived-input graph and its cycle guard (prompt 052).
 *
 * <p>Nodes are the derived generators. There is an edge from {@code A} to {@code B} when {@code B}'s
 * change subscription matches a record {@code A} writes ({@code A}'s source id with any of {@code A}'s
 * kinds), so {@code A}'s output feeds {@code B}. A cycle ({@code wx-cells → wx-places → wx-cells}, or a
 * generator subscribed to its own output) would let derived sources feed each other forever, so it
 * fails startup: the graph is checked once every singleton exists, with each generator's default
 * parameters. Frame inputs never form edges, because derived sources do not store frames.
 */
@Component
public class DerivedGraph implements SmartInitializingSingleton {

    private final List<DerivedGenerator> generators;
    private final ParamValidator params;

    /** Creates the guard over every generator bean. */
    public DerivedGraph(List<DerivedGenerator> generators, ParamValidator params) {
        this.generators = List.copyOf(generators);
        this.params = params;
    }

    /**
     * Fails startup when the graph has a cycle.
     *
     * @throws IllegalStateException naming the cycle
     */
    @Override
    public void afterSingletonsInstantiated() {
        Map<SourceDescriptor, DerivedInputs> nodes = new LinkedHashMap<>();
        for (DerivedGenerator generator : generators) {
            SourceDescriptor descriptor = generator.descriptor();
            ValidatedParams defaults;
            try {
                defaults = params.validate(descriptor, Map.of());
            } catch (RuntimeException e) {
                defaults = ValidatedParams.empty();
            }
            nodes.put(descriptor, generator.inputs(defaults));
        }
        cycle(nodes).ifPresent(path -> {
            throw new IllegalStateException("The derived sources form a cycle: " + String.join(" → ", path)
                    + ". A derived source must not consume, directly or indirectly, the records it produces.");
        });
    }

    /**
     * The first cycle of the graph, as the source ids along it (first repeated at the end), or empty.
     *
     * @param nodes each generator's descriptor and inputs
     */
    public static Optional<List<String>> cycle(Map<SourceDescriptor, DerivedInputs> nodes) {
        List<SourceDescriptor> order = new ArrayList<>(nodes.keySet());
        Map<String, List<String>> edges = new LinkedHashMap<>();
        for (SourceDescriptor from : order) {
            List<String> targets = new ArrayList<>();
            for (SourceDescriptor to : order) {
                DerivedInputs in = nodes.get(to);
                for (String kind : from.kinds()) {
                    if (in.matchesChange(from.id(), kind)) {
                        targets.add(to.id());
                        break;
                    }
                }
            }
            edges.put(from.id(), targets);
        }
        Map<String, Integer> state = new LinkedHashMap<>();
        for (String start : edges.keySet()) {
            List<String> path = new ArrayList<>();
            Optional<List<String>> found = visit(start, edges, state, path);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    /** Depth-first search; state 1 = on the current path, 2 = finished. */
    private static Optional<List<String>> visit(String node, Map<String, List<String>> edges, Map<String, Integer> state,
            List<String> path) {
        Integer s = state.get(node);
        if (s != null && s == 2) {
            return Optional.empty();
        }
        if (s != null && s == 1) {
            List<String> cycle = new ArrayList<>(path.subList(path.indexOf(node), path.size()));
            cycle.add(node);
            return Optional.of(cycle);
        }
        state.put(node, 1);
        path.add(node);
        for (String next : edges.getOrDefault(node, List.of())) {
            Optional<List<String>> found = visit(next, edges, state, path);
            if (found.isPresent()) {
                return found;
            }
        }
        path.remove(path.size() - 1);
        state.put(node, 2);
        return Optional.empty();
    }
}
