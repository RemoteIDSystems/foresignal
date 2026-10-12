package ai.foresignal.place;

/**
 * The places AOI changed (prompt 036): weather sources (047, 048, 049, 052) and render warming (045)
 * re-read {@link PlacesAoiService#aoi()} and {@link PlacesAoiService#envelope()}.
 *
 * @param version the AOI's new version
 */
public record PlacesAoiChangedEvent(long version) {}
