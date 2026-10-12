package ai.foresignal.search;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

/** Geometry encoding for {@code embedding_document} writes. */
final class Geometries {

    private Geometries() {}

    /**
     * {@code geom} as hex EWKB with SRID 4326 (assumed when the geometry carries none), or null.
     * {@link EmbeddingStore} decodes it with {@code ST_GeomFromEWKB(decode(?, 'hex'))}.
     */
    static String hexEwkb(Geometry geom) {
        if (geom == null) {
            return null;
        }
        if (geom.getSRID() == 0) {
            geom = geom.copy();
            geom.setSRID(4326);
        }
        return WKBWriter.toHex(new WKBWriter(2, true).write(geom));
    }
}
