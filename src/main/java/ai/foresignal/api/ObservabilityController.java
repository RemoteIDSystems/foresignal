package ai.foresignal.api;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.observability.ObservabilityService;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.security.restrict.RestrictedRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The coverage layer (prompt 116, tag {@code Observability}, read): how well instrumented an area is for a
 * family at a time, per region or H3 cell, as JSON and as vector tiles (layer {@value #LAYER}, built by PostGIS
 * {@code ST_AsMVT} like the 016 record tiles). Estimates valid at {@code at} are used, never today's for a past
 * time. Detection probabilities are per source footprint, not record content, so no record leaves here.
 */
@RestController
@Tag(name = "Observability")
@RequestMapping("/v1/observability")
public class ObservabilityController {

    /** The MVT layer name. */
    public static final String LAYER = "observability";

    /** The MVT extent. */
    static final int EXTENT = 4096;

    static final String TILE_SQL = "SELECT ST_AsMVT(t.*, '" + LAYER + "', " + EXTENT + ", 'geom') FROM ("
            + "SELECT ST_AsMVTGeom(ST_Transform(ST_GeomFromText(g.wkt, 4326), 3857), ST_TileEnvelope(?, ?, ?), "
            + EXTENT + ", 64, true) AS geom, g.cell, g.p, g.lo, g.hi, g.methods"
            + " FROM unnest(?::text[], ?::text[], ?::float8[], ?::float8[], ?::float8[], ?::text[])"
            + " AS g(wkt, cell, p, lo, hi, methods)) t WHERE t.geom IS NOT NULL";

    private final ObservabilityService observability;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the controller. */
    public ObservabilityController(ObservabilityService observability, JdbcTemplate jdbc, Clock clock) {
        this.observability = observability;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** {@code getObservability}. */
    @GetMapping
    @RestrictedRead("source footprints only; no records are returned")
    @Operation(operationId = "getObservability", tags = "Observability")
    public Map<String, Object> getObservability(@RequestParam String family,
            @RequestParam(required = false) String region, @RequestParam(required = false) String bbox,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at,
            @RequestParam(required = false) String band, @RequestParam(required = false) List<String> include,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        if (region != null && bbox != null) {
            throw ValidationException.forField("bbox", "give region or bbox, not both");
        }
        int offset = 0;
        if (cursor != null) {
            try {
                offset = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                throw ValidationException.forField("cursor", "is not a cursor from a previous page");
            }
        }
        boolean estimates = EvidenceController.split(include).contains("estimates");
        return observability.coverage(family, region, bbox == null ? null : parseBbox(bbox),
                at == null ? clock.instant() : at, band, estimates, Math.max(0, offset), Math.max(1, Math.min(500, limit)));
    }

    /** {@code getObservabilityTile}. */
    @GetMapping("/tiles/{z}/{x}/{y}.mvt")
    @RestrictedRead("source footprints only; no records are returned")
    @Operation(operationId = "getObservabilityTile", tags = "Observability")
    public ResponseEntity<byte[]> getObservabilityTile(@PathVariable int z, @PathVariable int x, @PathVariable int y,
            @RequestParam String family,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at,
            @RequestParam(required = false) String band) {
        if (z < 0 || z > 16) {
            throw ValidationException.forField("z", "must be 0 to 16");
        }
        int n = 1 << z;
        if (x < 0 || y < 0 || x >= n || y >= n) {
            throw ValidationException.forField("x", "the tile is outside zoom " + z);
        }
        double[] bounds = WebMercator.tileBounds(z, x, y);
        Map<Long, ObservabilityService.Detectability> cells = observability.tileCells(family,
                at == null ? clock.instant() : at, band, bounds, z);
        byte[] tile = encode(z, x, y, cells);
        return ResponseEntity.ok().contentType(TilesController.MVT)
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(10))).body(tile);
    }

    /** The MVT of the cells (empty when there are none). */
    byte[] encode(int z, int x, int y, Map<Long, ObservabilityService.Detectability> cells) {
        if (cells.isEmpty()) {
            return new byte[0];
        }
        List<String> wkt = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<Double> p = new ArrayList<>();
        List<Double> lo = new ArrayList<>();
        List<Double> hi = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        cells.forEach((cell, d) -> {
            wkt.add(H3Cells.cellToPolygon(cell).toText());
            ids.add(H3Cells.cellToAddress(cell));
            p.add(d.p());
            lo.add(d.lo());
            hi.add(d.hi());
            methods.add(String.join(",", d.methods()));
        });
        byte[] tile = jdbc.queryForObject(TILE_SQL, byte[].class, z, x, y, wkt.toArray(String[]::new),
                ids.toArray(String[]::new), p.toArray(Double[]::new), lo.toArray(Double[]::new),
                hi.toArray(Double[]::new), methods.toArray(String[]::new));
        return tile == null ? new byte[0] : tile;
    }

    /** {@code minLon,minLat,maxLon,maxLat}. */
    static double[] parseBbox(String text) {
        String[] parts = text.split(",");
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat");
        }
        double[] out = new double[4];
        try {
            for (int i = 0; i < 4; i++) {
                out[i] = Double.parseDouble(parts[i].strip());
            }
        } catch (NumberFormatException e) {
            throw ValidationException.forField("bbox", "must be four numbers");
        }
        if (out[0] > out[2] || out[1] > out[3] || out[0] < -180 || out[2] > 180 || out[1] < -90 || out[3] > 90) {
            throw ValidationException.forField("bbox", "must be minLon,minLat,maxLon,maxLat within WGS84 bounds");
        }
        return out;
    }
}
