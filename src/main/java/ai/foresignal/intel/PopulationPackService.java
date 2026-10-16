package ai.foresignal.intel;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.InboxFiles;
import ai.foresignal.source.spi.Geo;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The optional population pack (prompt 101). Imports a GHSL GHS-POP GeoTIFF under {@code
 * ${FORESIGNAL_DATA_DIR}/packs}, aggregating each pixel's population to the H3 cells (res 5 and 7)
 * containing its centre, and estimates the population inside a geometry by polyfilling it at res 7
 * (res 5 for areas over 50,000 km²). The result is a modelled residential population, never a
 * casualty estimate.
 */
@Service
public class PopulationPackService {

    private static final Logger log = LoggerFactory.getLogger(PopulationPackService.class);

    /** The settings key holding the import record. */
    public static final String IMPORT_KEY = "population.import";

    /** The method string returned with an estimate. */
    public static final String METHOD = "GHSL→H3 r7 centroid-in-polygon";

    /** The note returned with an estimate. */
    public static final String NOTE = "modelled residential population; not a casualty or impact estimate";

    /** The attribution. */
    public static final String ATTRIBUTION = "© European Union, GHSL";

    /** The licence. */
    public static final String LICENCE = "CC-BY-4.0";

    /** The area over which a coarser resolution is used, km². */
    static final double COARSE_AREA_KM2 = 50_000;

    static final short RES_FINE = 7;
    static final short RES_COARSE = 5;

    private final ForesignalProperties properties;
    private final PopulationH3Repository population;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the service. */
    public PopulationPackService(ForesignalProperties properties, PopulationH3Repository population,
            AppSettingService settings, Clock clock) {
        this.properties = properties;
        this.population = population;
        this.settings = settings;
        this.clock = clock;
    }

    /** One estimate. */
    public record Estimate(String status, Double estimate, Short epoch, String method, String note) {

        static Estimate notInstalled() {
            return new Estimate("not-installed", null, null, null,
                    "Import the GHSL GHS-POP pack to enable population estimates (© European Union, GHSL).");
        }

        static Estimate ok(double estimate, short epoch) {
            return new Estimate("ok", estimate, epoch, METHOD, NOTE);
        }
    }

    /**
     * Imports a pack file at {@code path}, relative to (or under) {@code ${FORESIGNAL_DATA_DIR}/packs}.
     *
     * @return the import record
     */
    @Transactional
    public Map<String, Object> importPack(String path, short epoch) throws IOException {
        Path root = properties.dataDir().resolve("packs");
        InboxFiles packs = InboxFiles.rootedAt(root);
        Path file = packs.requireInside(properties.dataDir().resolve(path).normalize());
        // Pixels are streamed strip by strip straight into the per-cell sums; only the cell maps stay in memory.
        Map<Long, Double> fine = new LinkedHashMap<>();
        Map<Long, Double> coarse = new LinkedHashMap<>();
        try {
            PopulationTiffReader.read(file, (lon, lat, value) -> {
                fine.merge(H3Cells.latLngToCell(lat, lon, RES_FINE), value, Double::sum);
                coarse.merge(H3Cells.latLngToCell(lat, lon, RES_COARSE), value, Double::sum);
            });
        } catch (Exception e) {
            throw new IOException("Could not read population GeoTIFF: " + e.getMessage(), e);
        }
        population.deleteAllInBatch();
        List<PopulationH3Entity> rows = new ArrayList<>(fine.size() + coarse.size());
        fine.forEach((cell, pop) -> rows.add(new PopulationH3Entity(cell, RES_FINE, pop, epoch)));
        coarse.forEach((cell, pop) -> rows.add(new PopulationH3Entity(cell, RES_COARSE, pop, epoch)));
        population.saveAll(rows);

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("sha256", sha256(file));
        record.put("epoch", epoch);
        record.put("licence", LICENCE);
        record.put("attribution", ATTRIBUTION);
        record.put("cellsRes7", fine.size());
        record.put("cellsRes5", coarse.size());
        record.put("importedAt", clock.instant().toString());
        settings.put(IMPORT_KEY, record);
        log.info("Imported population pack {}: {} res-7 cells, {} res-5 cells", file, fine.size(), coarse.size());
        return record;
    }

    /** The stored import record, or empty. */
    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> importRecord() {
        return settings.get(IMPORT_KEY, Map.class).map(m -> (Map<String, Object>) m);
    }

    /** Estimates the population inside {@code area}. */
    public Estimate estimate(Geometry area) {
        if (population.countByRes(RES_FINE) == 0 && population.countByRes(RES_COARSE) == 0) {
            return Estimate.notInstalled();
        }
        short res = areaEnvelopeKm2(area) > COARSE_AREA_KM2 ? RES_COARSE : RES_FINE;
        Set<Long> cells = polyfill(area, res);
        Double sum = cells.isEmpty() ? 0.0 : population.sumOver(res, cells);
        short epoch = importRecord().map(r -> ((Number) r.getOrDefault("epoch", 2025)).shortValue()).orElse((short) 2025);
        return Estimate.ok(sum == null ? 0 : sum, epoch);
    }

    /** The H3 cells at {@code res} whose centre lies inside {@code area}. */
    Set<Long> polyfill(Geometry area, short res) {
        Set<Long> cells = new LinkedHashSet<>();
        if (area == null || area.isEmpty()) {
            return cells;
        }
        Envelope envelope = area.getEnvelopeInternal();
        double step = H3Cells.averageEdgeLengthKm(res) / 111.0;
        if (step <= 0) {
            return cells;
        }
        for (double lat = envelope.getMinY(); lat <= envelope.getMaxY(); lat += step) {
            for (double lon = envelope.getMinX(); lon <= envelope.getMaxX(); lon += step) {
                if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
                    continue;
                }
                long cell = H3Cells.latLngToCell(lat, lon, res);
                cells.add(cell);
            }
        }
        // Keep only cells whose centre is inside the polygon.
        Set<Long> inside = new LinkedHashSet<>();
        for (long cell : cells) {
            double[] centre = centre(cell);
            if (centre == null) {
                continue;
            }
            Point point = Geo.factory().createPoint(new org.locationtech.jts.geom.Coordinate(centre[0], centre[1]));
            if (area.covers(point) || area.intersects(point.buffer(0.001))) {
                inside.add(cell);
            }
        }
        return inside;
    }

    private static double[] centre(long cell) {
        try {
            var polygon = H3Cells.cellToPolygon(cell);
            Point centroid = polygon.getCentroid();
            return new double[] {centroid.getX(), centroid.getY()};
        } catch (RuntimeException e) {
            return null;
        }
    }

    static double areaEnvelopeKm2(Geometry area) {
        if (area == null || area.isEmpty()) {
            return 0;
        }
        Envelope envelope = area.getEnvelopeInternal();
        double midLat = Math.toRadians((envelope.getMinY() + envelope.getMaxY()) / 2);
        double widthKm = (envelope.getMaxX() - envelope.getMinX()) * 111.32 * Math.cos(midLat);
        double heightKm = (envelope.getMaxY() - envelope.getMinY()) * 110.57;
        return Math.abs(widthKm * heightKm);
    }

    private static String sha256(Path file) {
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(file)) {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                sha.update(buffer, 0, n);
            }
            byte[] digest = sha.digest();
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
