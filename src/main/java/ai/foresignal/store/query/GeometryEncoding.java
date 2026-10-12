package ai.foresignal.store.query;

/** How a query renders geometries: GeoJSON text ({@code ST_AsGeoJSON}) or WKT ({@code ST_AsText}, for CSV). */
public enum GeometryEncoding {
    GEOJSON,
    WKT
}
