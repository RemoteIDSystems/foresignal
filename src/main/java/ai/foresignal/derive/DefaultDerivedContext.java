package ai.foresignal.derive;

import ai.foresignal.raster.sample.RasterReader;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The {@link DerivedContext} of one invocation (prompt 052): collects the emits, through the source's
 * {@link EmitLedger}, for the runner to submit as one batch.
 */
public final class DefaultDerivedContext implements DerivedContext {

    private final SourceDescriptor descriptor;
    private final ValidatedParams params;
    private final AdapterStateStore state;
    private final RasterReader rasters;
    private final DerivedRecords records;
    private final ObservationFeed observations;
    private final CoverageService coverage;
    private final DerivedPlaces places;
    private final Function<String, HealthStatus> health;
    private final Clock clock;
    private final EmitLedger ledger;
    private final List<NormalizedRecord> emitted = new ArrayList<>();
    private int skipped;

    /**
     * Creates the context.
     *
     * @param health the status of another source
     * @param ledger the source's emit ledger
     */
    public DefaultDerivedContext(SourceDescriptor descriptor, ValidatedParams params, AdapterStateStore state,
            RasterReader rasters, DerivedRecords records, ObservationFeed observations, CoverageService coverage,
            DerivedPlaces places, Function<String, HealthStatus> health, Clock clock, EmitLedger ledger) {
        this.descriptor = descriptor;
        this.params = params == null ? ValidatedParams.empty() : params;
        this.state = state;
        this.rasters = rasters;
        this.records = records;
        this.observations = observations;
        this.coverage = coverage;
        this.places = places;
        this.health = health;
        this.clock = clock;
        this.ledger = ledger;
    }

    @Override
    public String sourceId() {
        return descriptor.id();
    }

    @Override
    public ValidatedParams params() {
        return params;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException when the kind is not one of the source's kinds
     */
    @Override
    public void emit(NormalizedRecord record) {
        if (!descriptor.kinds().contains(record.kind())) {
            throw new IllegalArgumentException(descriptor.id() + " emitted kind " + record.kind()
                    + ", which it does not declare");
        }
        NormalizedRecord admitted = ledger.admit(record);
        if (admitted == null) {
            skipped++;
            return;
        }
        emitted.add(admitted);
    }

    @Override
    public void tombstone(String kind, String externalId) {
        emit(NormalizedRecord.tombstone(externalId, kind, clock.instant()));
    }

    @Override
    public AdapterStateStore state() {
        return state;
    }

    @Override
    public RasterReader rasters() {
        if (rasters == null) {
            throw new UnsupportedOperationException("No raster reader is available to " + sourceId());
        }
        return rasters;
    }

    @Override
    public DerivedRecords records() {
        return records;
    }

    @Override
    public ObservationFeed observations() {
        return observations;
    }

    @Override
    public CoverageService coverage() {
        return coverage;
    }

    @Override
    public DerivedPlaces places() {
        return places;
    }

    @Override
    public HealthStatus sourceStatus(String sourceId) {
        try {
            HealthStatus status = health == null ? null : health.apply(sourceId);
            return status == null ? HealthStatus.IDLE : status;
        } catch (RuntimeException e) {
            return HealthStatus.IDLE;
        }
    }

    @Override
    public Clock clock() {
        return clock;
    }

    /** The admitted emits, in order. */
    public List<NormalizedRecord> emitted() {
        return List.copyOf(emitted);
    }

    /** Emits skipped because they repeated the previous one. */
    public int skipped() {
        return skipped;
    }

    /** The source's ledger. */
    EmitLedger ledger() {
        return ledger;
    }
}
