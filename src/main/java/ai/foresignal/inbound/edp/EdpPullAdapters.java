package ai.foresignal.inbound.edp;

import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Creates the {@link EdpPullAdapter} of each pull provider (prompt 024) and holds what its fetches
 * need. The manifest service is looked up lazily, because it also creates adapters.
 */
@Component
public class EdpPullAdapters {

    private final ObjectProvider<EdpManifestService> manifests;
    private final EdpHttpClient http;
    private final EdpBatchReader reader;
    private final EdpBatchProcessor processor;
    private final BudgetGovernor budgets;
    private final UsageMeter usage;
    private final Clock clock;

    /** Creates the factory. */
    public EdpPullAdapters(ObjectProvider<EdpManifestService> manifests, EdpHttpClient http, EdpBatchReader reader,
            EdpBatchProcessor processor, BudgetGovernor budgets, UsageMeter usage, Clock clock) {
        this.manifests = manifests;
        this.http = http;
        this.reader = reader;
        this.processor = processor;
        this.budgets = budgets;
        this.usage = usage;
        this.clock = clock;
    }

    /** The adapter of the provider described by {@code descriptor}. */
    public EdpPullAdapter create(SourceDescriptor descriptor) {
        return new EdpPullAdapter(descriptor, this);
    }

    EdpManifestService manifests() {
        return manifests.getObject();
    }

    EdpHttpClient http() {
        return http;
    }

    EdpBatchReader reader() {
        return reader;
    }

    EdpBatchProcessor processor() {
        return processor;
    }

    BudgetGovernor budgets() {
        return budgets;
    }

    UsageMeter usage() {
        return usage;
    }

    Clock clock() {
        return clock;
    }
}
