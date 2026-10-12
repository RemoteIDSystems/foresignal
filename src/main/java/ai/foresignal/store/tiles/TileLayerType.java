package ai.foresignal.store.tiles;

/** What a tile layer name resolved to. */
public enum TileLayerType {
    /** A registered source id: {@code source_id = layer}. */
    SOURCE("source"),
    /** A kind declared by at least one registered source: {@code kind = layer}. */
    KIND("kind"),
    /**
     * A saved view (prompt 036): the view's sources (or every visible source); its predicate is
     * applied through the caller's restriction filter. Never listed in the TileJSON index.
     */
    VIEW("view");

    private final String wireValue;

    TileLayerType(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The value of {@code TileLayerIndex.layers[].type}. */
    public String wireValue() {
        return wireValue;
    }
}
