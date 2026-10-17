package ai.foresignal.impact.hazard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.union.UnaryUnionOp;

/**
 * Adapts stored hazard records to {@link HazardForecast}s (prompt 127 §1). Pure.
 *
 * <table>
 *   <caption>Hazard inputs</caption>
 *   <tr><th>Hazard type</th><th>Records</th><th>Probability model</th></tr>
 *   <tr><td>{@code tropical-wind-34/50/64}</td><td>{@code nhc-wind-probs} bands of one advisory</td>
 *       <td>published polygons; each band's lower bound</td></tr>
 *   <tr><td>{@code tropical-track}</td><td>an {@code nhc-cyclone-cones} cone</td><td>none (context and timing; the
 *       cone covers about two thirds of historical track errors)</td></tr>
 *   <tr><td>{@code tornado}, {@code wind}, {@code hail}</td><td>{@code spc-outlooks} day 1–2 probabilistic areas</td>
 *       <td>published point probability ({@code torn:0.10} → 10% within 25 miles of a point)</td></tr>
 *   <tr><td>{@code severe-categorical}</td><td>{@code spc-outlooks} categorical areas</td><td>none (context only)</td></tr>
 *   <tr><td>{@code excessive-rain}</td><td>{@code wpc-ero-d1..d3}</td><td>WPC's published range per category
 *       (marginal ≥ 5%, slight ≥ 15%, moderate ≥ 40%, high ≥ 70%), using the lower bound and storing the range</td></tr>
 *   <tr><td>{@code river-flood}</td><td>an {@code nwps-gauges} gauge</td><td>the forecast category (else the current
 *       category under persistence), calibrated by the gauge's history ({@link GaugeCalibration}); no calibration, no
 *       probability</td></tr>
 *   <tr><td>{@code storm-cell}</td><td>a {@code wx-storm-tracks} track</td><td>the extrapolated position; the tracker
 *       publishes no position error, so no probability</td></tr>
 * </table>
 */
public final class HazardAdapters {

    /** 25 statute miles in kilometers: the radius of SPC and WPC point probabilities. */
    public static final double POINT_RADIUS_KM = 40.2336;

    /** The NHC wind-probability source. */
    public static final String NHC_WIND_PROBS = "nhc-wind-probs";

    /** The NHC cone source. */
    public static final String NHC_CONES = "nhc-cyclone-cones";

    /** The SPC outlook source. */
    public static final String SPC_OUTLOOKS = "spc-outlooks";

    /** The NWPS gauge source. */
    public static final String NWPS_GAUGES = "nwps-gauges";

    /** The storm-track generator. */
    public static final String STORM_TRACKS = "wx-storm-tracks";

    /** The WPC ERO sources the projector reads (days 1 to 3). */
    public static final List<String> WPC_ERO = List.of("wpc-ero-d1", "wpc-ero-d2", "wpc-ero-d3");

    /** WPC ERO categories with their published ranges, lowest first. */
    public static final Map<String, HazardForecast.Range> ERO_RANGES;

    /** WPC ERO category names. */
    public static final Map<String, String> ERO_NAMES = Map.of("MRGL", "marginal", "SLGT", "slight", "MDT",
            "moderate", "HIGH", "high");

    static {
        Map<String, HazardForecast.Range> ranges = new LinkedHashMap<>();
        ranges.put("MRGL", new HazardForecast.Range(0.05, 0.15));
        ranges.put("SLGT", new HazardForecast.Range(0.15, 0.40));
        ranges.put("MDT", new HazardForecast.Range(0.40, 0.70));
        ranges.put("HIGH", new HazardForecast.Range(0.70, null));
        ERO_RANGES = java.util.Collections.unmodifiableMap(ranges);
    }

    private static final GeometryFactory GEOMETRY = new GeometryFactory(new PrecisionModel(), 4326);

    private HazardAdapters() {}

    // ------------------------------------------------------------------------- keys

    /**
     * The hazard key of a record, or null when the record feeds no hazard: {@code nhc-wind-probs:{kt}},
     * {@code nhc-cyclone-cones:{stormId}}, {@code spc-outlooks:day{d}:{type}}, {@code wpc-ero-d{n}},
     * {@code nwps-gauges:{lid}} or {@code wx-storm-tracks:{id}}.
     *
     * @param sourceId the source
     * @param kind the kind
     * @param externalId the external id
     */
    public static String hazardKey(String sourceId, String kind, String externalId) {
        if (sourceId == null || externalId == null) {
            return null;
        }
        switch (sourceId) {
            case NHC_WIND_PROBS -> {
                int colon = externalId.indexOf("kt:");
                return colon > 0 ? NHC_WIND_PROBS + ":" + externalId.substring(0, colon) : null;
            }
            case NHC_CONES -> {
                return "cyclone-cone".equals(kind) ? NHC_CONES + ":" + externalId : null;
            }
            case SPC_OUTLOOKS -> {
                String[] parts = externalId.split(":");
                if (parts.length < 2 || !parts[0].matches("day[12]")) {
                    return null;
                }
                return List.of("torn", "wind", "hail", "cat").contains(parts[1])
                        ? SPC_OUTLOOKS + ":" + parts[0] + ":" + parts[1] : null;
            }
            case NWPS_GAUGES -> {
                return NWPS_GAUGES + ":" + externalId;
            }
            case STORM_TRACKS -> {
                return "storm-track".equals(kind) ? STORM_TRACKS + ":" + externalId : null;
            }
            default -> {
                return WPC_ERO.contains(sourceId) ? sourceId : null;
            }
        }
    }

    /** The source of a hazard key. */
    public static String sourceOf(String hazardKey) {
        int colon = hazardKey.indexOf(':');
        return colon < 0 ? hazardKey : hazardKey.substring(0, colon);
    }

    // ------------------------------------------------------------------------- NHC

    /**
     * The {@code tropical-wind-{kt}} forecast of one advisory: the bands of the newest product time (older bands still
     * waiting for their tombstone are ignored), highest first.
     *
     * @param thresholdKt 34, 50 or 64
     * @param bands the current {@code wind-probability} records of that threshold
     * @return the forecast, or empty without bands
     */
    public static Optional<HazardForecast> windProbabilities(int thresholdKt, List<HazardRecord> bands) {
        Instant issued = bands.stream().map(HazardRecord::observedAt).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        if (issued == null) {
            return Optional.empty();
        }
        List<HazardRecord> current = bands.stream().filter(b -> issued.equals(b.observedAt())
                && b.geometry() != null && b.number("pMin") != null).toList();
        if (current.isEmpty()) {
            return Optional.empty();
        }
        List<HazardForecast.Band> field = new ArrayList<>();
        List<String> refs = new ArrayList<>();
        for (HazardRecord r : current) {
            field.add(new HazardForecast.Band(r.text("band"), r.number("pMin"), r.number("pMax"), r.geometry()));
            refs.add(r.ref());
        }
        field.sort(Comparator.comparingDouble(HazardForecast.Band::pMin).reversed());
        Instant until = current.stream().map(r -> r.validUntil() != null ? r.validUntil() : r.instant("validUntil"))
                .filter(java.util.Objects::nonNull).findFirst().orElse(issued.plusSeconds(120 * 3600));
        String type = switch (thresholdKt) {
            case 34 -> HazardForecast.TROPICAL_WIND_34;
            case 50 -> HazardForecast.TROPICAL_WIND_50;
            case 64 -> HazardForecast.TROPICAL_WIND_64;
            default -> throw new IllegalArgumentException("no wind threshold " + thresholdKt);
        };
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("thresholdKt", thresholdKt);
        details.put("horizonHours", 120);
        return Optional.of(new HazardForecast(type, NHC_WIND_PROBS + ":" + thresholdKt, newest(current).ref(), refs,
                "official", issued, issued, until, union(field.stream().map(HazardForecast.Band::geometry).toList()),
                field, HazardForecast.MODEL_POLYGONS, null, null, null,
                "NHC cumulative probability that sustained winds of at least " + thresholdKt + " kt occur at a point"
                        + " within 120 hours of the advisory.", details));
    }

    /**
     * A cone as a {@code tropical-track} forecast: no probability, context and timing only.
     *
     * @param cone a {@code cyclone-cone} record
     */
    public static HazardForecast cone(HazardRecord cone) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("stormId", cone.text("stormId"));
        details.put("stormName", cone.text("stormName"));
        details.put("advisoryNumber", cone.text("advisoryNumber"));
        Instant issued = cone.observedAt();
        return new HazardForecast(HazardForecast.TROPICAL_TRACK, NHC_CONES + ":" + cone.externalId(), cone.ref(),
                List.of(cone.ref()), "official", issued, issued, issued == null ? null : issued.plusSeconds(120 * 3600),
                cone.geometry(), List.of(), HazardForecast.MODEL_NONE, null, null, null, HazardForecast.CONE_NOTE,
                details);
    }

    // ------------------------------------------------------------------------- SPC

    /**
     * The SPC forecast of one day and layer: {@code torn}, {@code wind} and {@code hail} carry their published point
     * probabilities ({@code dn}, else the label: {@code 0.10} → 10%); {@code cat} is {@code severe-categorical},
     * context only. Significant-severe hatching ({@code SIGN}) is not a probability and is left out.
     *
     * @param day 1 or 2
     * @param type {@code torn}, {@code wind}, {@code hail} or {@code cat}
     * @param areas the current records of that day and type
     * @return the forecast, or empty without usable areas
     */
    public static Optional<HazardForecast> spc(int day, String type, List<HazardRecord> areas) {
        if (areas.isEmpty()) {
            return Optional.empty();
        }
        HazardRecord newest = newest(areas);
        List<String> refs = areas.stream().map(HazardRecord::ref).toList();
        Instant issued = newest.observedAt();
        Instant from = newest.eventTime() != null ? newest.eventTime() : newest.instant("valid");
        Instant to = newest.validUntil() != null ? newest.validUntil() : newest.instant("expire");
        String key = SPC_OUTLOOKS + ":day" + day + ":" + type;
        Map<String, Object> details = Map.of("day", day, "layer", type);
        if ("cat".equals(type)) {
            String highest = areas.stream().max(Comparator.comparingDouble(a -> a.number("dn") == null ? 0
                    : a.number("dn"))).map(a -> a.text("label")).orElse(null);
            return Optional.of(new HazardForecast(HazardForecast.SEVERE_CATEGORICAL, key, newest.ref(), refs,
                    "official", issued, from, to, union(areas.stream().map(HazardRecord::geometry).toList()), List.of(),
                    HazardForecast.MODEL_NONE, highest, null, null, "SPC categorical risk is context only; it is"
                            + " not used as a probability.", details));
        }
        String hazardType = switch (type) {
            case "torn" -> HazardForecast.TORNADO;
            case "wind" -> HazardForecast.WIND;
            case "hail" -> HazardForecast.HAIL;
            default -> throw new IllegalArgumentException("no SPC probabilistic layer " + type);
        };
        List<HazardForecast.Band> field = new ArrayList<>();
        for (HazardRecord a : areas) {
            Double p = spcProbability(a);
            if (p != null && a.geometry() != null) {
                field.add(new HazardForecast.Band(a.text("label"), p, null, a.geometry()));
            }
        }
        if (field.isEmpty()) {
            return Optional.empty();
        }
        field.sort(Comparator.comparingDouble(HazardForecast.Band::pMin).reversed());
        return Optional.of(new HazardForecast(hazardType, key, newest.ref(), refs, "official", issued, from, to,
                union(field.stream().map(HazardForecast.Band::geometry).toList()), field, HazardForecast.MODEL_POINT,
                null, null, POINT_RADIUS_KM, "SPC probability of the hazard within 25 miles of a point.", details));
    }

    /**
     * The published probability of an SPC probabilistic area: {@code dn} in percent, else a label such as
     * {@code 0.10}; null for hatching or anything unreadable.
     *
     * @param area the record
     */
    public static Double spcProbability(HazardRecord area) {
        String label = area.text("label");
        if (label != null && label.toUpperCase(Locale.ROOT).startsWith("SIGN")) {
            return null;
        }
        Double dn = area.number("dn");
        if (dn != null && dn > 0 && dn <= 100) {
            return dn / 100.0;
        }
        Double fromLabel = label == null ? null : area.number("label");
        if (fromLabel != null && fromLabel > 0 && fromLabel <= 1) {
            return fromLabel;
        }
        return null;
    }

    // ------------------------------------------------------------------------- WPC

    /**
     * The WPC excessive rainfall outlook of one day: each category's polygon is a band at the lower bound of WPC's
     * published range, with the range stored; the highest category is the forecast's category.
     *
     * @param sourceId {@code wpc-ero-d1}, {@code -d2} or {@code -d3}
     * @param categories the current {@code rainfall-outlook} records of that day
     * @return the forecast, or empty without known categories
     */
    public static Optional<HazardForecast> ero(String sourceId, List<HazardRecord> categories) {
        List<HazardForecast.Band> field = new ArrayList<>();
        String highest = null;
        for (HazardRecord r : categories) {
            String category = r.text("category");
            HazardForecast.Range range = category == null ? null : ERO_RANGES.get(category.toUpperCase(Locale.ROOT));
            if (range == null || r.geometry() == null) {
                continue;
            }
            String upper = category.toUpperCase(Locale.ROOT);
            field.add(new HazardForecast.Band(upper, range.low(), range.high(), r.geometry()));
            if (highest == null || ERO_RANGES.get(upper).low() > ERO_RANGES.get(highest).low()) {
                highest = upper;
            }
        }
        if (field.isEmpty()) {
            return Optional.empty();
        }
        field.sort(Comparator.comparingDouble(HazardForecast.Band::pMin).reversed());
        HazardRecord newest = newest(categories);
        Instant from = newest.validFrom() != null ? newest.validFrom() : newest.instant("startTime");
        Instant to = newest.validUntil() != null ? newest.validUntil() : newest.instant("endTime");
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("day", sourceId.substring(sourceId.length() - 1));
        details.put("categoryName", ERO_NAMES.get(highest));
        return Optional.of(new HazardForecast(HazardForecast.EXCESSIVE_RAIN, sourceId, newest.ref(),
                categories.stream().map(HazardRecord::ref).toList(), "official", newest.observedAt(), from, to,
                union(field.stream().map(HazardForecast.Band::geometry).toList()), field,
                HazardForecast.MODEL_CATEGORY_RANGE, highest, ERO_RANGES.get(highest), POINT_RADIUS_KM,
                "WPC probability of rainfall exceeding flash-flood guidance within 25 miles of a point: the lower"
                        + " bound of the category's published range.", details));
    }

    // ------------------------------------------------------------------------- NWPS

    /**
     * The basis of a gauge forecast: {@value GaugeCalibration#FORECAST} when the gauge carries a forecast category
     * whose valid time has not passed, else {@value GaugeCalibration#PERSISTENCE}.
     *
     * @param gauge the gauge record
     * @param now the decision time
     */
    public static String gaugeBasis(HazardRecord gauge, Instant now) {
        String forecast = gauge.text("forecastCategory");
        Instant valid = gauge.instant("forecastValidTime");
        return forecast != null && GaugeCalibration.rank(forecast) >= 0 && (valid == null || !valid.isBefore(now))
                ? GaugeCalibration.FORECAST : GaugeCalibration.PERSISTENCE;
    }

    /**
     * A gauge as a {@code river-flood} forecast. The probability is the gauge's calibrated rate for its basis when the
     * basis category reaches the flood-relevant category and the calibration is sufficient; otherwise the forecast
     * has no probability ({@code details.reason}).
     *
     * @param gauge the gauge record
     * @param calibration the calibration of the gauge's basis, or null
     * @param minCategory the flood-relevant category
     * @param areaKm the radius of the gauge's area
     * @param now the decision time
     */
    public static HazardForecast gauge(HazardRecord gauge, GaugeCalibration.Result calibration, String minCategory,
            double areaKm, Instant now) {
        String basis = gaugeBasis(gauge, now);
        String category = GaugeCalibration.FORECAST.equals(basis) ? gauge.text("forecastCategory")
                : gauge.text("floodCategory");
        Instant from = gauge.observedAt();
        Instant valid = gauge.instant("forecastValidTime");
        Instant to = GaugeCalibration.FORECAST.equals(basis) && valid != null && from != null && valid.isAfter(from)
                ? valid : from == null ? null : from.plus(GaugeCalibration.HORIZON);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("basis", basis);
        details.put("lid", gauge.externalId());
        details.put("observedCategory", gauge.text("floodCategory"));
        details.put("forecastCategory", gauge.text("forecastCategory"));
        details.put("minCategory", minCategory);
        details.put("calibration", calibration == null ? null : calibration.view());
        List<HazardForecast.Band> field = new ArrayList<>();
        Geometry area = gauge.geometry() == null ? null : circle(gauge.geometry(), areaKm);
        if (GaugeCalibration.rank(category) < GaugeCalibration.rank(minCategory)) {
            details.put("reason", "below-category");
        } else if (calibration == null || !calibration.sufficient()) {
            details.put("reason", "uncalibrated");
        } else if (area == null) {
            details.put("reason", "no-geometry");
        } else {
            field.add(new HazardForecast.Band(category, calibration.p(), calibration.upper(), area));
            details.put("pLower", calibration.lower());
        }
        return new HazardForecast(HazardForecast.RIVER_FLOOD, NWPS_GAUGES + ":" + gauge.externalId(), gauge.ref(),
                List.of(gauge.ref()), "official", from, from, to, area, field, HazardForecast.MODEL_GAUGE, category,
                null, null, "NWPS " + basis + " category at the gauge, calibrated by the gauge's own history; a"
                        + " category alone is not a probability.", details);
    }

    // ------------------------------------------------------------------------- storm cells

    /**
     * A storm track as a {@code storm-cell} forecast: the extrapolated nowcast area. The 105 tracker publishes no
     * position error, so the forecast carries no probability.
     *
     * @param track a {@code storm-track} record
     */
    public static HazardForecast stormCell(HazardRecord track) {
        Instant at = track.observedAt();
        return new HazardForecast(HazardForecast.STORM_CELL, STORM_TRACKS + ":" + track.externalId(), track.ref(),
                List.of(track.ref()), "foresignal", at, at, at == null ? null : at.plusSeconds(3600),
                track.geometry(), List.of(), HazardForecast.MODEL_TRACK_ERROR, null, null, null,
                "Linear-advection nowcast; the tracker publishes no position error, so no probability is derived.",
                Map.of("reason", "no-position-error"));
    }

    // ------------------------------------------------------------------------- helpers

    private static HazardRecord newest(List<HazardRecord> records) {
        return records.stream().max(Comparator.comparingLong(HazardRecord::seq)).orElseThrow();
    }

    /** The union of the geometries (null ones skipped), or null. */
    static Geometry union(List<Geometry> geometries) {
        List<Geometry> present = geometries.stream().filter(g -> g != null && !g.isEmpty()).toList();
        if (present.isEmpty()) {
            return null;
        }
        try {
            Geometry u = UnaryUnionOp.union(present);
            u.setSRID(4326);
            return u;
        } catch (RuntimeException e) {
            GeometryCollection c = GEOMETRY.createGeometryCollection(present.toArray(Geometry[]::new));
            return c.getEnvelope();
        }
    }

    /** A circle of {@code km} around the geometry's centroid (degrees, longitude scaled by latitude). */
    static Geometry circle(Geometry g, double km) {
        Point c = g.getCentroid();
        double dLat = km / 111.32;
        double cos = Math.max(0.05, Math.cos(Math.toRadians(c.getY())));
        Geometry circle = c.buffer(dLat, 16);
        org.locationtech.jts.geom.util.AffineTransformation t = new org.locationtech.jts.geom.util.AffineTransformation()
                .translate(-c.getX(), -c.getY()).scale(1 / cos, 1).translate(c.getX(), c.getY());
        Geometry out = t.transform(circle);
        out.setSRID(4326);
        return out;
    }
}
