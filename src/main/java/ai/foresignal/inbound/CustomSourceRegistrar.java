package ai.foresignal.inbound;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers every persisted custom source and external provider with the {@link SourceRegistry} at
 * startup (prompt 024, {@code StartupOrder.CUSTOM_SOURCES}).
 *
 * <p>Runs in {@link #afterSingletonsInstantiated()}: after every bean exists (the schema is in
 * place), but before the web server accepts requests and before the writer, the trigger engine and
 * the scheduler start on {@code ApplicationReadyEvent}. A row that no longer validates is logged and
 * skipped rather than stopping startup, because the operator must still be able to fix or delete it.
 *
 * <p>As an {@link IngestStateReset} it also reconciles the registry with the table after the test
 * database cleaner truncates it: dynamic {@code custom-*} and {@code ext-*} sources without a row are
 * unregistered.
 */
@Component
public class CustomSourceRegistrar implements SmartInitializingSingleton, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(CustomSourceRegistrar.class);

    private final CustomSourceRepository repository;
    private final SourceRegistry registry;
    private final List<DynamicSourceFactory> factories;
    private final JsonMapper json;

    /** Creates the registrar. */
    public CustomSourceRegistrar(CustomSourceRepository repository, SourceRegistry registry,
            List<DynamicSourceFactory> factories, JsonMapper json) {
        this.repository = repository;
        this.registry = registry;
        this.factories = factories;
        this.json = json;
    }

    @Override
    public void afterSingletonsInstantiated() {
        reconcile();
    }

    @Override
    public void resetState() {
        reconcile();
    }

    /** Registers missing rows and unregisters dynamic custom or external sources without one. */
    public synchronized void reconcile() {
        Set<String> persisted = new HashSet<>();
        for (CustomSourceEntity row : repository.findAll()) {
            persisted.add(row.getSourceId());
            if (registry.contains(row.getSourceId())) {
                continue;
            }
            try {
                DynamicSourceFactory.Registration registration = registration(row);
                registry.registerDynamic(registration.descriptor(), registration.adapter());
            } catch (RuntimeException e) {
                log.error("Could not register {} source {}; fix or delete it", row.getOrigin(), row.getSourceId(), e);
            }
        }
        for (SourceRegistry.Registration registration : registry.all()) {
            String id = registration.id();
            if (registration.dynamic() && (id.startsWith("custom-") || id.startsWith("ext-"))
                    && !persisted.contains(id)) {
                registry.unregisterDynamic(id);
            }
        }
    }

    /** The registration of {@code row}: an EDP factory's for external rows, else the custom descriptor. */
    public DynamicSourceFactory.Registration registration(CustomSourceEntity row) {
        for (DynamicSourceFactory factory : factories) {
            if (factory.handles(row)) {
                return factory.build(row);
            }
        }
        SourceDescriptor descriptor = CustomSourceDescriptors.toDescriptor(row, json);
        return new DynamicSourceFactory.Registration(descriptor, null);
    }
}
