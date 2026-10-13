package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.LiveImageHolder;
import ai.foresignal.source.spi.OrbitalElements;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.RemoteMediaReceivers;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceAdapterFamily;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Registers one {@code satdump-{station}} source per name in {@code foresignal.satdump.stations}
 * (environment {@code FORESIGNAL_SATDUMP_STATIONS}, a comma list, empty by default) (prompt 049).
 *
 * <p>The list is operator configuration read once at startup, never an API input (the family rule in
 * {@link SourceAdapterFamily}). A name must be kebab-case and short enough for a 64-character source
 * id; an invalid name fails startup rather than being silently dropped.
 */
@Component
public class SatDumpFamily implements SourceAdapterFamily {

    private static final Logger log = LoggerFactory.getLogger(SatDumpFamily.class);

    /** The property naming the stations. */
    public static final String STATIONS_PROPERTY = "foresignal.satdump.stations";

    private final List<SourceAdapter> adapters;

    /**
     * Creates the family.
     *
     * @param stations the comma list of station names
     * @param places resolves place-id locations (prompt 036), resolved lazily
     * @param liveImages holds the polar plots, resolved lazily
     * @param receivers where running stations register for pushed images (prompt 054), resolved lazily
     */
    public SatDumpFamily(@Value("${" + STATIONS_PROPERTY + ":}") String stations, ObjectProvider<PlacePoints> places,
            ObjectProvider<LiveImageHolder> liveImages, ObjectProvider<RemoteMediaReceivers> receivers) {
        this(stations, places, liveImages, receivers, null);
    }

    /**
     * Creates the family (prompt 064).
     *
     * @param stations the comma list of station names
     * @param places resolves place-id locations (prompt 036), resolved lazily
     * @param liveImages holds the polar plots, resolved lazily
     * @param receivers where running stations register for pushed images (prompt 054), resolved lazily
     * @param elements the current {@code celestrak-gp} element sets for predicted passes, resolved lazily
     */
    @org.springframework.beans.factory.annotation.Autowired
    public SatDumpFamily(@Value("${" + STATIONS_PROPERTY + ":}") String stations, ObjectProvider<PlacePoints> places,
            ObjectProvider<LiveImageHolder> liveImages, ObjectProvider<RemoteMediaReceivers> receivers,
            ObjectProvider<OrbitalElements> elements) {
        List<SourceAdapter> built = new ArrayList<>();
        java.util.function.Supplier<PlacePoints> placeSupplier = places == null ? () -> null : places::getIfAvailable;
        java.util.function.Supplier<LiveImageHolder> imageSupplier =
                liveImages == null ? () -> null : liveImages::getIfAvailable;
        java.util.function.Supplier<RemoteMediaReceivers> receiverSupplier =
                receivers == null ? () -> null : receivers::getIfAvailable;
        java.util.function.Supplier<OrbitalElements> elementSupplier =
                elements == null ? () -> null : elements::getIfAvailable;
        for (String station : names(stations)) {
            built.add(new SatDumpAdapter(station, placeSupplier, imageSupplier, receiverSupplier, elementSupplier));
        }
        this.adapters = List.copyOf(built);
        if (!adapters.isEmpty()) {
            log.info("SatDump stations configured: {}", names(stations));
        }
    }

    @Override
    public List<SourceAdapter> adapters() {
        return adapters;
    }

    /**
     * The station names of a comma list, trimmed, without repeats.
     *
     * @throws IllegalStateException when a name is not kebab-case or is too long
     */
    static Set<String> names(String stations) {
        Set<String> out = new LinkedHashSet<>();
        if (stations == null) {
            return out;
        }
        for (String part : stations.split(",")) {
            String name = part.trim();
            if (name.isEmpty()) {
                continue;
            }
            if (!SatDumpDescriptor.STATION.matcher(name).matches() || name.length() > SatDumpDescriptor.MAX_STATION_LENGTH) {
                throw new IllegalStateException("Foresignal cannot start: SatDump station name '" + name + "' in "
                        + STATIONS_PROPERTY + " must be kebab-case (a-z, 0-9 and single hyphens) and at most "
                        + SatDumpDescriptor.MAX_STATION_LENGTH + " characters.");
            }
            out.add(name);
        }
        return out;
    }
}
