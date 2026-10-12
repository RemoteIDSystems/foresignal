/**
 * The raster product core (prompt 042, Weather-Imagery-Design §2): the product catalog
 * ({@link ai.foresignal.raster.ProductRegistry}), the native-grid frame store with its {@code .fsg}
 * codec ({@link ai.foresignal.raster.FrameStore}, {@link ai.foresignal.raster.ElgCodec}), the raster
 * pipeline behind {@code ctx.rasters()} ({@link ai.foresignal.raster.RasterPipeline}), frame
 * resolution ({@link ai.foresignal.raster.FrameResolver}), frame-age health and frame retention
 * ({@link ai.foresignal.raster.RasterRetentionStep}).
 *
 * <p>Frames live on disk and in {@code raster_frame}; each complete observation frame and each
 * completed forecast run also becomes one {@code raster-frame} record version, so SSE, resume, MCP
 * and triggers work with no raster-specific distribution code. No client request ever causes an
 * upstream fetch: frames arrive only from enabled, budgeted sources.
 *
 * <p>Prompt 045 adds {@code raster.render} (tiles, styles, legends) and 052 {@code raster.sample}.
 */
package ai.foresignal.raster;
