/**
 * Serving raster products (prompt 045; Weather-Imagery-Design §6.3): WebMercator tiles, single images
 * and animation loops rendered on the fly from native-grid frames through cached index maps; the
 * versioned style registry and legends; the one raster cache policy; the bounded fair render queue;
 * and newest-frame warming. Nothing here ever causes an upstream fetch.
 */
package ai.foresignal.raster.render;
