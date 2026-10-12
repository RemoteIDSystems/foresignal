package ai.foresignal.reference;

import ai.foresignal.source.spi.HistoryTier;
import java.util.List;
import java.util.Locale;

/**
 * How a source's records are region-tagged ({@code SourceSettings.regionTagging}, prompt 040).
 */
public enum RegionTagMode {
    /** Tagged by the {@link RegionTagWorker} as versions persist (default for STANDARD sources). */
    EAGER,
    /**
     * Tagged on demand, when a template renders, an alert fires, a briefing is built or a record is read
     * with {@code include=region}; the result is stored (default for TRACK sources).
     */
    LAZY,
    /** Never tagged. */
    OFF;

    /** The wire values. */
    public static final List<String> WIRE_VALUES = List.of("eager", "lazy", "off");

    /** The wire value. */
    public String wire() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The mode of a wire value. */
    public static RegionTagMode fromWire(String value) {
        return valueOf(value.strip().toUpperCase(Locale.ROOT));
    }

    /** The default of a source in {@code tier}: lazy for TRACK, eager otherwise. */
    public static RegionTagMode defaultFor(HistoryTier tier) {
        return tier == HistoryTier.TRACK ? LAZY : EAGER;
    }
}
