package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.LiveImageHolder;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.ValidatedParams;
import java.util.Map;
import java.util.function.Supplier;

/**
 * One {@code satdump-{station}} source (prompt 049, Weather-Imagery-Design §4.1): the owner's SatDump
 * ground station as a first-class source. {@link SatDumpFamily} creates one per configured station.
 *
 * <p>STREAMING: {@link #start} runs a {@link SatDumpStation} with an optional {@code /api} loop and an
 * inbox loop over SatDump's output directory, under the streaming supervisor. Foresignal talks to SatDump
 * only through HTTP and files; SatDump is a separate, operator-supplied GPL process, and no SatDump code
 * is in Foresignal (Architecture §26).
 */
public class SatDumpAdapter implements StreamingAdapter {

    private final String station;
    private final SourceDescriptor descriptor;
    private final Supplier<PlacePoints> places;
    private final Supplier<LiveImageHolder> liveImages;

    /**
     * Creates the adapter.
     *
     * @param station the station name
     * @param places resolves place-id locations; may supply null
     * @param liveImages holds the polar plot; may supply null
     */
    public SatDumpAdapter(String station, Supplier<PlacePoints> places, Supplier<LiveImageHolder> liveImages) {
        this.station = station;
        this.descriptor = SatDumpDescriptor.build(station);
        this.places = places == null ? () -> null : places;
        this.liveImages = liveImages == null ? () -> null : liveImages;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The station name. */
    public String station() {
        return station;
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        return SatDumpDescriptor.check(params);
    }

    /**
     * Starts the station's loops.
     *
     * @throws AdapterException when the parameters cannot be used
     */
    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        SatDumpStation running = newStation(ctx, sink);
        running.start();
        return running;
    }

    /**
     * A station that is not started yet: tests drive its loops one step at a time.
     *
     * @throws AdapterException when the parameters cannot be used
     */
    public SatDumpStation newStation(StreamContext ctx, RecordSink sink) throws AdapterException {
        try {
            return new SatDumpStation(station, ctx, sink, places.get(), liveImages.get());
        } catch (IllegalArgumentException e) {
            throw new AdapterException(descriptor.id() + ": " + e.getMessage(), e);
        }
    }
}
