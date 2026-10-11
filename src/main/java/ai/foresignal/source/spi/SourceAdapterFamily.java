package ai.foresignal.source.spi;

import java.util.List;

/**
 * One parser serving many feeds: GTFS-RT agencies, GBFS systems, adsb.lol regions, CCTV packs.
 *
 * <p>Each returned adapter is a separate source with its own settings, budget and health, so an
 * operator can enable Boston without enabling Helsinki.
 *
 * <p><strong>Feed lists are static and code-reviewed, never supplied by an API caller.</strong>
 * A family that took its feed list from a request would turn Foresignal into an open HTTP proxy, which
 * is the SSRF hole this design exists to close.
 */
public interface SourceAdapterFamily {

    /** One adapter per feed. Called once at startup. */
    List<SourceAdapter> adapters();
}
