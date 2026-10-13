package ai.foresignal.source.spi;

import ai.foresignal.space.OmmElements;
import java.util.Optional;

/**
 * The current GP element sets Foresignal holds (prompt 064): the {@code celestrak-gp} heads, as an SPI
 * type so an adapter (a SatDump station predicting its passes) can read them without touching the
 * store (Architecture §3). {@code ai.foresignal.space.CelestrakElementsService} implements it.
 */
public interface OrbitalElements {

    /**
     * The current element set of one object, or empty when Foresignal holds none (the source is disabled,
     * the object is not in its groups, or its head expired).
     *
     * @param norad the NORAD catalog number (up to 9 digits)
     */
    Optional<OmmElements> byNorad(long norad);
}
