package ai.foresignal.raster;

/**
 * Published after a frame is stored and its row upserted (prompt 042). Prompt 045 warms tiles from
 * it; the record version, if any, follows through the ingest queue.
 *
 * @param productId the product
 * @param frameId the frame id
 * @param status {@code partial} or {@code complete}
 */
public record RasterFrameCommittedEvent(String productId, String frameId, String status) {}
