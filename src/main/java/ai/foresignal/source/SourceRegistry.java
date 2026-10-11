package ai.foresignal.source;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Every source Foresignal knows about.
 *
 * <p>Built at startup from the {@link SourceAdapter} beans plus whatever the {@link
 * SourceAdapterFamily} beans return, and extended at runtime by custom push sources.
 *
 * <p><strong>Validation failures stop startup.</strong> A broken descriptor is a programming error,
 * and the alternatives are both worse: skipping the source silently means an operator enables
 * something that never runs, and accepting it means the ingest writer later meets a record it
 * cannot classify. Every problem across every adapter is reported in one message, because fixing
 * them one restart at a time is miserable.
 *
 * <p>Reads happen on hot paths — every scheduler tick and every ingest batch consults the registry
 * — so lookups go through a {@link ConcurrentHashMap} rather than a lock.
 */
@Component
public class SourceRegistry {

    private static final Logger log = LoggerFactory.getLogger(SourceRegistry.class);

    private final Map<String, Registration> registrations = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher events;

    public SourceRegistry(
            List<SourceAdapter> adapters,
            List<SourceAdapterFamily> families,
            ApplicationEventPublisher events) {
        this.events = events;

        List<SourceAdapter> discovered = new ArrayList<>(adapters);
        for (SourceAdapterFamily family : families) {
            List<SourceAdapter> fromFamily = family.adapters();
            if (fromFamily == null) {
                throw new IllegalStateException(
                        family.getClass().getName() + ".adapters() returned null");
            }
            discovered.addAll(fromFamily);
        }

        List<String> problems = new ArrayList<>();
        Map<String, SourceAdapter> byId = new LinkedHashMap<>();

        for (SourceAdapter adapter : discovered) {
            SourceDescriptor descriptor;
            try {
                descriptor = adapter.descriptor();
            } catch (RuntimeException e) {
                problems.add(
                        adapter.getClass().getName() + ".descriptor() threw " + e);
                continue;
            }
            problems.addAll(DescriptorValidator.problems(descriptor, adapter));
            if (descriptor == null || descriptor.id() == null) {
                continue;
            }
            SourceAdapter existing = byId.putIfAbsent(descriptor.id(), adapter);
            if (existing != null) {
                problems.add(
                        "Duplicate source id '"
                                + descriptor.id()
                                + "' is declared by both "
                                + existing.getClass().getName()
                                + " and "
                                + adapter.getClass().getName());
            }
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException(
                    "Foresignal cannot start: "
                            + problems.size()
                            + " source descriptor problem(s).\n  - "
                            + String.join("\n  - ", problems));
        }

        byId.forEach(
                (id, adapter) ->
                        registrations.put(id, new Registration(adapter.descriptor(), adapter, false)));

        for (SourceAdapter adapter : byId.values()) {
            DescriptorValidator.warnings(adapter.descriptor()).forEach(log::warn);
        }
        log.info(
                "Registered {} source(s) from {} adapter bean(s) and {} family bean(s)",
                registrations.size(),
                adapters.size(),
                families.size());
    }

    /** Every registration, ordered by id. */
    public List<Registration> all() {
        return registrations.values().stream()
                .sorted(Comparator.comparing(r -> r.descriptor().id()))
                .toList();
    }

    /** The registration for {@code id}, or empty. */
    public Optional<Registration> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(registrations.get(id));
    }

    /**
     * The registration for {@code id}.
     *
     * @throws NotFoundException when no such source exists
     */
    public Registration get(String id) {
        return find(id).orElseThrow(() -> NotFoundException.of("Source", id));
    }

    /** The descriptor for {@code id}. */
    public SourceDescriptor descriptor(String id) {
        return get(id).descriptor();
    }

    /** Every descriptor, ordered by id. */
    public List<SourceDescriptor> descriptors() {
        return all().stream().map(Registration::descriptor).toList();
    }

    /** Every id, ordered. */
    public Set<String> ids() {
        return new TreeSet<>(registrations.keySet());
    }

    /** Every kind any registered source emits, ordered. */
    public Set<String> kinds() {
        Set<String> kinds = new TreeSet<>();
        registrations.values().forEach(r -> kinds.addAll(r.descriptor().kinds()));
        return kinds;
    }

    /** True when {@code id} is registered. */
    public boolean contains(String id) {
        return id != null && registrations.containsKey(id);
    }

    /**
     * Registers a runtime-defined source (a custom push source, prompt 020).
     *
     * <p>Applies exactly the same validation as a compile-time adapter, so a custom source cannot
     * express something the rest of the system treats as impossible.
     *
     * @throws ConflictException if the id is already registered
     * @throws ValidationException if the descriptor is not sound
     */
    public void registerDynamic(SourceDescriptor descriptor) {
        if (descriptor == null) {
            throw new ValidationException("A descriptor is required.");
        }
        List<String> problems = DescriptorValidator.problems(descriptor, null);
        if (!problems.isEmpty()) {
            throw new ValidationException(
                    "The source definition is not valid: " + String.join("; ", problems));
        }
        if (descriptor.mode() != Mode.PUSH && descriptor.mode() != Mode.DERIVED) {
            throw new ValidationException(
                    "A dynamic source must be PUSH or DERIVED, but " + descriptor.id() + " is "
                            + descriptor.mode());
        }

        Registration previous =
                registrations.putIfAbsent(
                        descriptor.id(), new Registration(descriptor, null, true));
        if (previous != null) {
            throw new ConflictException(
                    "SOURCE_EXISTS", "A source with id '" + descriptor.id() + "' already exists.");
        }
        DescriptorValidator.warnings(descriptor).forEach(log::warn);
        log.info("Registered dynamic source {}", descriptor.id());
        events.publishEvent(SourceRegistryChangedEvent.registered(descriptor.id()));
    }

    /**
     * Removes a dynamic source.
     *
     * @throws NotFoundException when no such source exists
     * @throws ConflictException when the source is a compile-time adapter
     */
    public void unregisterDynamic(String id) {
        Registration registration = get(id);
        if (!registration.dynamic()) {
            throw new ConflictException(
                    "SOURCE_NOT_DYNAMIC",
                    "'" + id + "' is a built-in source and cannot be removed.");
        }
        registrations.remove(id);
        log.info("Unregistered dynamic source {}", id);
        events.publishEvent(SourceRegistryChangedEvent.unregistered(id));
    }

    /** Ids that are registered but not present in {@code known}, ordered. */
    public Set<String> idsNotIn(Set<String> known) {
        Set<String> result = new LinkedHashSet<>();
        ids().forEach(
                id -> {
                    if (!known.contains(id)) {
                        result.add(id);
                    }
                });
        return result;
    }

    /**
     * One registered source.
     *
     * @param descriptor its description
     * @param adapter the implementation, or null for a dynamic PUSH or DERIVED source
     * @param dynamic true when it was registered at runtime
     */
    public record Registration(SourceDescriptor descriptor, SourceAdapter adapter, boolean dynamic) {

        public String id() {
            return descriptor.id();
        }

        public Optional<SourceAdapter> adapterIfPresent() {
            return Optional.ofNullable(adapter);
        }
    }
}
