package ai.foresignal.raster.sample;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.UnitConversion;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

/**
 * {@code POST /v1/products/{id}/stats} (prompt 052; Weather-Imagery-Design §6.5): min, max, mean, p95,
 * coverage and the area above each threshold of one frame over a geometry, place or bbox.
 *
 * <p>Limits: the geometry has at most {@value #MAX_VERTICES} vertices and covers at most
 * {@value #MAX_AREA_KM2} km² (both 400). Thresholds are given in the requested unit system and compared
 * in the product's own. A non-ok status (unavailable, no coverage, stale) carries no statistics: unknown
 * is never clear.
 */
@Service
public class AreaStatsService {

    /** The most vertices of a stats geometry. */
    public static final int MAX_VERTICES = 10_000;

    /** The largest stats area, km². */
    public static final double MAX_AREA_KM2 = 2_000_000;

    /** The most thresholds. */
    public static final int MAX_THRESHOLDS = 20;

    private final SampleService samples;

    /** Creates the service. */
    public AreaStatsService(SampleService samples) {
        this.samples = samples;
    }

    /**
     * Checks a stats geometry against the limits.
     *
     * @throws ValidationException when it has too many vertices, is not polygonal, or is too large
     */
    public static double checkGeometry(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            throw ValidationException.forField("geometry", "is required and must not be empty");
        }
        if (geometry.getNumPoints() > MAX_VERTICES) {
            throw ValidationException.forField("geometry", "has " + geometry.getNumPoints() + " vertices; at most "
                    + MAX_VERTICES);
        }
        if (geometry.getDimension() != 2) {
            throw ValidationException.forField("geometry", "must be a Polygon or MultiPolygon");
        }
        double area = CellAreas.geometryAreaKm2(geometry);
        if (area > MAX_AREA_KM2) {
            throw ValidationException.forField("geometry", "covers " + Math.round(area) + " km²; at most "
                    + (long) MAX_AREA_KM2 + " km²");
        }
        return area;
    }

    /**
     * The {@code StatsResponse}.
     *
     * @param thresholds thresholds in the requested unit system (at most {@value #MAX_THRESHOLDS})
     */
    public Map<String, Object> stats(ProductRegistry.Registered product, Geometry geometry, TimeSpec time, String run,
            List<Double> thresholds, boolean imperial) {
        checkGeometry(geometry);
        List<Double> ts = thresholds == null ? List.of() : thresholds;
        if (ts.size() > MAX_THRESHOLDS) {
            throw ValidationException.forField("thresholds", "at most " + MAX_THRESHOLDS + " thresholds");
        }
        Instant now = samples.now();
        ProductSpec spec = product.spec();
        UnitConversion conversion = UnitConversion.of(spec.units(), imperial);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("productId", spec.id());
        FrameChoice choice = samples.choose(product, time, run, now);
        RasterFrameRow frame = choice.frame();
        out.put("frameKey", frame == null ? null : frame.frameKey());
        out.put("validTime", frame == null ? null : frame.validTime().toString());
        out.put("run", frame == null || frame.referenceTime() == null ? null : FrameKeys.runIso(frame.referenceTime()));
        out.put("ageMinutes", frame == null ? null : SampleService.ageMinutes(frame.validTime(), now));
        out.put("units", conversion.units());
        Optional<FrameView> view = frame == null ? Optional.empty() : samples.view(frame);
        SampleStatus status;
        String text;
        AreaScan.Result result = null;
        if (frame == null || view.isEmpty()) {
            status = SampleStatus.UNAVAILABLE;
            text = frame == null ? choice.text() : "frame " + frame.frameKey() + " could not be read";
        } else {
            double[] nativeThresholds = new double[ts.size()];
            for (int i = 0; i < ts.size(); i++) {
                nativeThresholds[i] = (ts.get(i) - conversion.offset()) / conversion.scale();
            }
            result = AreaScan.scan(view.get(), geometry, nativeThresholds, false);
            if (result.cellsInArea() == 0 || result.coveredCells() == 0) {
                status = SampleStatus.NO_COVERAGE;
                text = SampleStatus.NO_COVERAGE.defaultText();
            } else if (choice.status() != null) {
                status = choice.status();
                text = choice.text();
            } else {
                status = SampleStatus.OK;
                text = null;
            }
        }
        out.put("status", status.wireValue());
        out.put("statusText", status.ok() ? null : (text == null ? status.defaultText() : text));
        boolean ok = status.ok();
        out.put("min", ok ? SampleService.round(conversion.apply(result.min()), 3) : null);
        out.put("max", ok ? SampleService.round(conversion.apply(result.max()), 3) : null);
        out.put("mean", ok ? SampleService.round(conversion.apply(result.mean()), 3) : null);
        out.put("p95", ok ? SampleService.round(conversion.apply(result.p95()), 3) : null);
        out.put("coveragePct", result == null || result.cellsInArea() == 0 ? null
                : SampleService.round(result.coveragePct(), 1));
        out.put("areaKm2", result == null ? SampleService.round(CellAreas.geometryAreaKm2(geometry), 1)
                : SampleService.round(result.areaKm2(), 1));
        List<Map<String, Object>> above = new ArrayList<>();
        if (ok) {
            for (int i = 0; i < ts.size(); i++) {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("threshold", ts.get(i));
                t.put("areaKm2", SampleService.round(result.areaAboveKm2()[i], 1));
                above.add(t);
            }
        }
        out.put("thresholds", above);
        out.put("attribution", SampleService.attribution(spec));
        return out;
    }
}
