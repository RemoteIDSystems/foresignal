package ai.foresignal.testsupport.adapters;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PushAdapter;
import ai.foresignal.source.spi.SourceDescriptor;

/** A PUSH source, for testing that the scheduler leaves it alone and ingest accepts it. */
public class FakePushAdapter implements PushAdapter {

    private final SourceDescriptor descriptor;

    public FakePushAdapter(SourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public static FakePushAdapter withId(String id) {
        return new FakePushAdapter(defaultDescriptor(id).build());
    }

    public static SourceDescriptor.Builder defaultDescriptor(String id) {
        return SourceDescriptor.builder(id)
                .name("Fake push " + id)
                .kind("fake-record")
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }
}
