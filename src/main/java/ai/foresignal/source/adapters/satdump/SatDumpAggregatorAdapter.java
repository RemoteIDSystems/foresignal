package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.LiveImageHolder;
import ai.foresignal.source.spi.OrbitalElements;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.RemoteMediaReceivers;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code satdump-aggregator} (prompt R2-022): the satellite aggregator's ground station — the operator's own SatDump
 * receiving GOES and weather satellites off a dish, configured by {@code PUT /v1/aggregators/satellite} rather than by
 * {@value SatDumpFamily#STATIONS_PROPERTY}.
 *
 * <p>It is a {@link SatDumpAdapter} for the reserved station name {@value #STATION}, so it registers whether or not
 * any station is configured at startup and has exactly a station's parameters, loops and kinds. The aggregator writes
 * its {@code apiUrl} (the endpoint), {@code location}, {@code inboxPath}, {@code satdumpVersion} and
 * {@code predictNorad}, and turns it on while the aggregator is live.
 */
@Component
public class SatDumpAggregatorAdapter extends SatDumpAdapter {

    /** The reserved station name; {@value SatDumpFamily#STATIONS_PROPERTY} refuses it. */
    public static final String STATION = "aggregator";

    /** The source id. */
    public static final String ID = SatDumpDescriptor.PREFIX + STATION;

    /**
     * Creates the adapter.
     *
     * @param places resolves place-id locations, resolved lazily
     * @param liveImages holds the polar plots, resolved lazily
     * @param receivers where running stations register for pushed images, resolved lazily
     * @param elements the current {@code celestrak-gp} element sets for predicted passes, resolved lazily
     * @param extras the DCS and EMWIN processing, resolved lazily
     */
    public SatDumpAggregatorAdapter(ObjectProvider<PlacePoints> places, ObjectProvider<LiveImageHolder> liveImages,
            ObjectProvider<RemoteMediaReceivers> receivers, ObjectProvider<OrbitalElements> elements,
            ObjectProvider<SatDumpExtras> extras) {
        super(STATION, lazy(places), lazy(liveImages), lazy(receivers), lazy(elements), lazy(extras));
    }

    private static <T> Supplier<T> lazy(ObjectProvider<T> provider) {
        return provider == null ? () -> null : provider::getIfAvailable;
    }
}
