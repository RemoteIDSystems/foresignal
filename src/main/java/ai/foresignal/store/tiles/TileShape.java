package ai.foresignal.store.tiles;

/** How one tile is rendered: raw features or aggregate cells. */
public enum TileShape {
    /** One feature per record. */
    RAW(null),
    /** Square cells aligned to tile edges ({@code agg: "grid"}). */
    GRID("grid"),
    /** Hexagonal cells ({@code agg: "hex"}). */
    HEX("hex");

    private final String aggValue;

    TileShape(String aggValue) {
        this.aggValue = aggValue;
    }

    /** The {@code agg} cell property, or null for raw tiles. */
    public String aggValue() {
        return aggValue;
    }

    /** True for grid and hex. */
    public boolean aggregated() {
        return this != RAW;
    }
}
