package ai.foresignal.search;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.security.EgressPolicy;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Which sources' records are embedded (prompt 027): the descriptor declares {@code embedFields},
 * the source's {@code embed} setting is on (default on when {@code embedFields} is non-empty), and
 * {@link EgressPolicy#allowsEmbedding} allows it, which a private source never is (descriptor flag or
 * operator tightening, prompt 039; the {@code embed} setting is forced off for it).
 *
 * <p>The set is cached and rebuilt when a source is registered or removed, or its settings change.
 * Turning {@code embed} off deletes the source's record documents, so they stop being searchable.
 */
@Component
public class EmbeddingEligibility implements IngestStateReset {

    private final SourceRegistry registry;
    private final SourceSettingsRepository settings;
    private final EgressPolicy egress;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate newTransaction;
    private volatile Set<String> cached;

    /** Creates the component. */
    public EmbeddingEligibility(SourceRegistry registry, SourceSettingsRepository settings, EgressPolicy egress,
            JdbcTemplate jdbc, PlatformTransactionManager transactions) {
        this.registry = registry;
        this.settings = settings;
        this.egress = egress;
        this.jdbc = jdbc;
        this.newTransaction = new TransactionTemplate(transactions);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** The ids of every source whose records are embedded. */
    public Set<String> sources() {
        Set<String> current = cached;
        if (current == null) {
            current = compute();
            cached = current;
        }
        return current;
    }

    /** True when records of {@code sourceId} are embedded. */
    public boolean eligible(String sourceId) {
        return sources().contains(sourceId);
    }

    /** The descriptor of an eligible source, or empty. */
    public Optional<SourceDescriptor> descriptor(String sourceId) {
        if (!eligible(sourceId)) {
            return Optional.empty();
        }
        return registry.find(sourceId).map(SourceRegistry.Registration::descriptor);
    }

    /** Forgets the cache when a source is registered, redefined or removed. */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        cached = null;
    }

    /** Forgets the cache on a settings change; turning {@code embed} off deletes the source's documents. */
    @EventListener
    public void onSettingsChanged(SourceSettingsChangedEvent event) {
        cached = null;
        SourceView before = event.before();
        SourceView after = event.after();
        // before is null when this update created the settings row (SourceSettingsChangedEvent): the source was at
        // its defaults, embedded when eligible, so turning embed off still deletes its documents (checkpoint 041:
        // a source's first settings update to embed=false left them searchable).
        if (after != null && !after.embed() && (before == null || before.embed())) {
            // The event is published after the settings commit: write in a transaction of our own.
            newTransaction.executeWithoutResult(status -> jdbc.update(
                    "DELETE FROM embedding_document WHERE doc_type = 'record' AND source_id = ?", event.sourceId()));
        }
    }

    /**
     * A source became private (prompt 039): it is no longer eligible, and every embedding row it left
     * (records, and alerts or briefings attributed to it) is deleted, so nothing of it stays searchable
     * semantically. Published after the change commits: writes in a transaction of its own.
     */
    @EventListener
    public void onSourceBecamePrivate(ai.foresignal.security.visibility.SourceBecamePrivateEvent event) {
        cached = null;
        newTransaction.executeWithoutResult(status -> jdbc.update(
                "DELETE FROM embedding_document WHERE source_id = ?", event.sourceId()));
    }

    @Override
    public void resetState() {
        cached = null;
    }

    private Set<String> compute() {
        Map<String, Boolean> overrides = new HashMap<>();
        for (SourceSettingsEntity row : settings.findAll()) {
            if (row.getEmbed() != null) {
                overrides.put(row.getSourceId(), row.getEmbed());
            }
        }
        Set<String> out = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.embedFields().isEmpty() || !egress.allowsEmbedding(descriptor.id())) {
                continue;
            }
            if (overrides.getOrDefault(descriptor.id(), true)) {
                out.add(descriptor.id());
            }
        }
        return Set.copyOf(out);
    }
}
