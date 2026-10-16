package ai.foresignal.raster.render;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** The registered tile matrix sets (prompt 108): {@code WebMercatorQuad} and {@code WorldCrs84Quad}. */
public final class TileMatrixSets {

    /** The registered sets, in order. */
    public static final List<TileMatrixSet> ALL = List.of(WebMercatorQuad.INSTANCE, WorldCrs84Quad.INSTANCE);

    /** The default set. */
    public static final TileMatrixSet DEFAULT = WebMercatorQuad.INSTANCE;

    private TileMatrixSets() {}

    /** The set with {@code id} (case-insensitive), or empty. */
    public static Optional<TileMatrixSet> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.of(DEFAULT);
        }
        String wanted = id.strip().toLowerCase(Locale.ROOT);
        return ALL.stream().filter(t -> t.id().toLowerCase(Locale.ROOT).equals(wanted)).findFirst();
    }

    /** The set with {@code id}, or the default when blank. */
    public static TileMatrixSet resolve(String id) {
        return find(id).orElseThrow(() -> ValidationException.forField("tms",
                "must be one of " + ALL.stream().map(TileMatrixSet::id).toList()));
    }
}
