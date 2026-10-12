package ai.foresignal.raster.render;

import java.awt.image.IndexColorModel;

/**
 * The one shared transparent tile (prompt 045; Weather-Imagery-Design §6.3, §11): every empty or
 * out-of-bounds tile answers these same bytes with the same fixed ETag, so clients never see error
 * tiles and caches hold one copy. {@code ?empty=204} opts into a 204 instead.
 */
public final class TransparentTile {

    /** The fixed strong ETag of the transparent tile. */
    public static final String ETAG = "\"fs-transparent-1\"";

    private static final byte[] PNG = build();

    private TransparentTile() {}

    private static byte[] build() {
        IndexColorModel model = new IndexColorModel(8, 2, new byte[2], new byte[2], new byte[2], new byte[2]);
        int size = WebMercator.TILE;
        return ImageEncoding.indexedPng(size, size, new byte[size * size], model);
    }

    /** The PNG bytes (a copy). */
    public static byte[] png() {
        return PNG.clone();
    }

    /** The PNG's length. */
    public static int length() {
        return PNG.length;
    }
}
