package ai.foresignal.source.adapters.derived;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/**
 * Drives the prompt 099 layer generators tick by tick over an in-memory {@link FakeDerived} world: a
 * registry that knows the input source ids, current records replaced per entity (as {@code record_head
 * ⋈ record_live} is), and the emits and tombstones of every tick. No Spring context, no database.
 */
final class LayerHarness {

    /** The in-memory world: current records, statuses, densities, frames and the clock. */
    final FakeDerived world;

    private final Set<String> sourceIds = new LinkedHashSet<>();
    private final SourceRegistry registry = mock(SourceRegistry.class);
    private long seq;

    /** The context of the last tick (its state store carries the health notes). */
    DefaultDerivedContext last;

    /** Tombstones of the last tick, as {@code kind/externalId}. */
    final List<String> tombstones = new ArrayList<>();

    /**
     * A world at {@code now} whose registry lists {@code sources}.
     *
     * @param now the initial clock
     * @param sources the registered source ids the generators resolve their prefixes against
     */
    LayerHarness(Instant now, String... sources) {
        world = new FakeDerived(now);
        sourceIds.addAll(List.of(sources));
        when(registry.ids()).thenAnswer(invocation -> Set.copyOf(sourceIds));
    }

    /** The registry provider every {@link LayerGenerator} constructor takes. */
    ObjectProvider<SourceRegistry> registry() {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        factory.addBean("sourceRegistry", registry);
        return factory.getBeanProvider(SourceRegistry.class);
    }

    /** A reference-data view over this world (ports, cables, airports; no chokepoint radii). */
    ReferenceData reference() {
        return new ReferenceData(world, null, world.clock);
    }

    /**
     * Sets the current record of {@code (sourceId, externalId)}, replacing the previous one.
     *
     * @param sourceId the input source
     * @param externalId the entity
     * @param kind the record kind
     * @param at the live {@code observedAt}
     * @param lon the longitude
     * @param lat the latitude
     * @param fields payload key/value pairs
     */
    void put(String sourceId, String externalId, String kind, Instant at, double lon, double lat, Object... fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            payload.put((String) fields[i], fields[i + 1]);
        }
        List<HeadSnapshot> heads = world.heads.computeIfAbsent(sourceId, k -> new ArrayList<>());
        heads.removeIf(h -> h.externalId().equals(externalId));
        heads.add(new HeadSnapshot(sourceId, externalId, kind, ++seq, 1, at, false, payload, Geo.point(lon, lat)));
    }

    /** Removes every current record of {@code sourceId}. */
    void clear(String sourceId) {
        world.heads.remove(sourceId);
    }

    /** One tick at {@code now} with default parameters; returns the records emitted. */
    List<NormalizedRecord> tick(LayerGenerator generator, Instant now) {
        return tick(generator, now, ValidatedParams.empty());
    }

    /** One tick at {@code now}; returns the records emitted and fills {@link #tombstones}. */
    List<NormalizedRecord> tick(LayerGenerator generator, Instant now, ValidatedParams params) {
        world.clock.setInstant(now);
        DefaultDerivedContext ctx = world.context(generator.descriptor(), params);
        generator.onTick(now, ctx);
        last = ctx;
        List<NormalizedRecord> all = world.collect(ctx);
        List<NormalizedRecord> emitted = new ArrayList<>();
        tombstones.clear();
        for (NormalizedRecord record : all) {
            if (record.tombstone()) {
                tombstones.add(record.kind() + "/" + record.externalId());
            } else {
                emitted.add(record);
            }
        }
        return emitted;
    }

    /** Runs the generator's startup reconcile over {@code ownHeads} at the world's clock; returns its emits. */
    List<NormalizedRecord> reconcile(LayerGenerator generator, List<HeadSnapshot> ownHeads) {
        DefaultDerivedContext ctx = world.context(generator.descriptor(), ValidatedParams.empty());
        world.seed(ownHeads);
        generator.reconcile(ownHeads, ctx);
        last = ctx;
        return world.collect(ctx);
    }

    /** The km per degree of latitude used by {@link Geo#haversineKm}, so offsets land on exact distances. */
    static double kmPerDegreeLat() {
        return Geo.haversineKm(0, 0, 1, 0);
    }

    /** The latitude {@code km} north of {@code lat} along a meridian. */
    static double north(double lat, double km) {
        return lat + km / kmPerDegreeLat();
    }
}
