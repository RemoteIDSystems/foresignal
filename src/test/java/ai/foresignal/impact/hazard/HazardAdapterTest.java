package ai.foresignal.impact.hazard;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * The hazard adapters of prompt 127, one nested group per hazard type: NHC probability bands, the cone that yields no
 * probability, SPC point probabilities, WPC category ranges, NWPS gauge calibration with the persistence fallback, and
 * storm cells without a position error; plus {@code P(hazard at asset)}.
 */
class HazardAdapterTest {

    static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
    static final Instant T = Instant.parse("2026-09-10T15:00:00Z");

    /** A square of side {@code size} degrees with its lower-left corner at (lon, lat). */
    static Geometry square(double lon, double lat, double size) {
        return GF.createPolygon(new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon + size, lat),
            new Coordinate(lon + size, lat + size), new Coordinate(lon, lat + size), new Coordinate(lon, lat)});
    }

    static Point point(double lon, double lat) {
        return GF.createPoint(new Coordinate(lon, lat));
    }

    static HazardRecord record(String source, String id, long seq, String kind, Map<String, Object> payload,
            Geometry g, Instant observed) {
        return new HazardRecord(source, id, seq, kind, payload, g, observed, null, null, null);
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    @Nested
    @DisplayName("tropical-wind (NHC probability bands)")
    class Nhc {

        List<HazardRecord> bands(Instant issued) {
            List<HazardRecord> out = new ArrayList<>();
            out.add(record("nhc-wind-probs", "34kt:5-10", 10, "wind-probability", map("thresholdKt", 34, "band",
                    "5-10%", "pMin", 0.05, "pMax", 0.1), square(-96, 27, 6), issued));
            out.add(record("nhc-wind-probs", "34kt:40-50", 11, "wind-probability", map("thresholdKt", 34, "band",
                    "40-50%", "pMin", 0.4, "pMax", 0.5), square(-95, 28, 2), issued));
            return out;
        }

        @Test
        @DisplayName("published polygons, highest band first, 120-hour validity, the newest advisory only")
        void bands() {
            List<HazardRecord> records = new ArrayList<>(bands(T));
            records.add(record("nhc-wind-probs", "34kt:90-100", 5, "wind-probability", map("thresholdKt", 34,
                    "band", ">90%", "pMin", 0.9, "pMax", 1.0), square(-95, 28, 1), T.minus(Duration.ofHours(6))));
            HazardForecast f = HazardAdapters.windProbabilities(34, records).orElseThrow();
            assertThat(f.hazardType()).isEqualTo(HazardForecast.TROPICAL_WIND_34);
            assertThat(f.hazardKey()).isEqualTo("nhc-wind-probs:34");
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_POLYGONS);
            assertThat(f.probabilityField()).extracting(HazardForecast.Band::pMin).containsExactly(0.4, 0.05);
            assertThat(f.validFrom()).isEqualTo(T);
            assertThat(f.validTo()).isEqualTo(T.plus(Duration.ofHours(120)));
            assertThat(f.sourceRef()).isEqualTo("record:nhc-wind-probs/34kt:40-50@11");
            assertThat(f.probabilistic()).isTrue();
        }

        @Test
        @DisplayName("P(hazard at asset): the highest containing band, or within the buffer; the band's bounds")
        void atAsset() {
            HazardForecast f = HazardAdapters.windProbabilities(34, bands(T)).orElseThrow();
            HazardProbability.At inner = HazardProbability.at(f, point(-94, 29), 5).orElseThrow();
            assertThat(inner.pHazard()).isEqualTo(0.4);
            assertThat(inner.lower()).isEqualTo(0.4);
            assertThat(inner.upper()).isEqualTo(0.5);
            // 0.03° (about 3.3 km) outside the 40–50% band: within the 5 km buffer.
            assertThat(HazardProbability.at(f, point(-92.97, 29), 5).orElseThrow().pHazard()).isEqualTo(0.4);
            assertThat(HazardProbability.at(f, point(-92.97, 29), 0).orElseThrow().pHazard()).isEqualTo(0.05);
            assertThat(HazardProbability.at(f, point(-120, 29), 5)).isEmpty();
        }

        @Test
        @DisplayName("no bands, no forecast")
        void empty() {
            assertThat(HazardAdapters.windProbabilities(50, List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("tropical-track (NHC cone)")
    class Cone {

        @Test
        @DisplayName("the cone yields no probability: model none, the two-thirds note, context only")
        void noProbability() {
            HazardRecord cone = record("nhc-cyclone-cones", "al052026", 7, "cyclone-cone", map("stormId", "al052026",
                    "stormName", "Ernesto", "advisoryNumber", "12"), square(-90, 20, 8), T);
            HazardForecast f = HazardAdapters.cone(cone);
            assertThat(f.hazardType()).isEqualTo(HazardForecast.TROPICAL_TRACK);
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_NONE);
            assertThat(f.probabilityField()).isEmpty();
            assertThat(f.probabilistic()).isFalse();
            assertThat(f.note()).contains("two thirds");
            assertThat(HazardProbability.at(f, point(-86, 24), 5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("tornado, wind, hail (SPC point probabilities)")
    class Spc {

        @Test
        @DisplayName("torn:0.10 → 10% within 25 miles of a point; hatching is not a probability")
        void pointProbability() {
            List<HazardRecord> areas = List.of(
                    record("spc-outlooks", "day1:torn:0.02", 1, "spc-outlook", map("day", 1, "type", "torn", "label",
                            "0.02", "dn", 2), square(-100, 30, 10), T),
                    record("spc-outlooks", "day1:torn:0.10", 2, "spc-outlook", map("day", 1, "type", "torn", "label",
                            "0.10", "dn", 10), square(-97, 33, 2), T),
                    record("spc-outlooks", "day1:torn:SIGN", 3, "spc-outlook", map("day", 1, "type", "torn", "label",
                            "SIGN", "dn", 0), square(-97, 33, 1), T));
            HazardForecast f = HazardAdapters.spc(1, "torn", areas).orElseThrow();
            assertThat(f.hazardType()).isEqualTo(HazardForecast.TORNADO);
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_POINT);
            assertThat(f.pointRadiusKm()).isEqualTo(HazardAdapters.POINT_RADIUS_KM);
            assertThat(f.probabilityField()).extracting(HazardForecast.Band::pMin).containsExactly(0.10, 0.02);
            assertThat(HazardProbability.at(f, point(-96, 34), 5).orElseThrow().pHazard()).isEqualTo(0.10);
            // A point probability adds no buffer: just outside the 10% area is the 2% area.
            assertThat(HazardProbability.at(f, point(-94.98, 34), 5).orElseThrow().pHazard()).isEqualTo(0.02);
        }

        @Test
        @DisplayName("the probability comes from dn, else from the label")
        void labelFallback() {
            HazardRecord a = record("spc-outlooks", "day2:hail:0.15", 1, "spc-outlook", map("type", "hail",
                    "label", "0.15"), square(0, 0, 1), T);
            assertThat(HazardAdapters.spcProbability(a)).isEqualTo(0.15);
        }

        @Test
        @DisplayName("severe-categorical is context only")
        void categorical() {
            HazardForecast f = HazardAdapters.spc(1, "cat", List.of(record("spc-outlooks", "day1:cat:MDT", 1,
                    "spc-outlook", map("type", "cat", "label", "MDT", "dn", 6), square(-97, 33, 2), T))).orElseThrow();
            assertThat(f.hazardType()).isEqualTo(HazardForecast.SEVERE_CATEGORICAL);
            assertThat(f.probabilistic()).isFalse();
            assertThat(f.category()).isEqualTo("MDT");
        }

        @Test
        @DisplayName("hazard keys: day 1–2 layers only")
        void keys() {
            assertThat(HazardAdapters.hazardKey("spc-outlooks", "spc-outlook", "day1:wind:0.15"))
                    .isEqualTo("spc-outlooks:day1:wind");
            assertThat(HazardAdapters.hazardKey("spc-outlooks", "spc-outlook", "day3:prob:0.15")).isNull();
            assertThat(HazardAdapters.hazardKey("nhc-wind-probs", "wind-probability", "64kt:5-10"))
                    .isEqualTo("nhc-wind-probs:64");
            assertThat(HazardAdapters.hazardKey("wpc-ero-d2", "rainfall-outlook", "SLGT")).isEqualTo("wpc-ero-d2");
            assertThat(HazardAdapters.hazardKey("wpc-ero-d5", "rainfall-outlook", "SLGT")).isNull();
        }
    }

    @Nested
    @DisplayName("excessive-rain (WPC category ranges)")
    class Wpc {

        @Test
        @DisplayName("marginal ≥ 5%, slight ≥ 15%, moderate ≥ 40%, high ≥ 70%: the lower bound, the range stored")
        void ranges() {
            List<HazardRecord> categories = List.of(
                    record("wpc-ero-d1", "MRGL", 1, "rainfall-outlook", map("category", "MRGL"), square(-92, 30, 6), T),
                    record("wpc-ero-d1", "SLGT", 2, "rainfall-outlook", map("category", "SLGT"), square(-91, 31, 4), T),
                    record("wpc-ero-d1", "MDT", 3, "rainfall-outlook", map("category", "MDT"), square(-90, 32, 2), T));
            HazardForecast f = HazardAdapters.ero("wpc-ero-d1", categories).orElseThrow();
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_CATEGORY_RANGE);
            assertThat(f.category()).isEqualTo("MDT");
            assertThat(f.probabilityRange()).isEqualTo(new HazardForecast.Range(0.40, 0.70));
            assertThat(f.probabilityField()).extracting(HazardForecast.Band::pMin).containsExactly(0.40, 0.15, 0.05);
            assertThat(f.probabilityField()).extracting(HazardForecast.Band::pMax).containsExactly(0.70, 0.40, 0.15);
            assertThat(HazardAdapters.ERO_RANGES.get("HIGH")).isEqualTo(new HazardForecast.Range(0.70, null));
            HazardProbability.At at = HazardProbability.at(f, point(-89, 33), 5).orElseThrow();
            assertThat(at.pHazard()).isEqualTo(0.40);
            assertThat(at.upper()).isEqualTo(0.70);
        }

        @Test
        @DisplayName("unknown categories are ignored")
        void unknown() {
            assertThat(HazardAdapters.ero("wpc-ero-d1", List.of(record("wpc-ero-d1", "X", 1, "rainfall-outlook",
                    map("category", "EXTREME"), square(0, 0, 1), T)))).isEmpty();
        }
    }

    @Nested
    @DisplayName("river-flood (NWPS gauges)")
    class Nwps {

        HazardRecord gauge(String flood, String forecast, Instant valid) {
            Map<String, Object> p = map("floodCategory", flood);
            if (forecast != null) {
                p.put("forecastCategory", forecast);
                p.put("forecastValidTime", valid.toString());
            }
            return record("nwps-gauges", "LKCT2", 9, "river-gauge", p, point(-95, 30), T);
        }

        List<GaugeCalibration.Observation> history(String basis, int days, int hits) {
            List<GaugeCalibration.Observation> out = new ArrayList<>();
            // One sample every two days, so a sample never verifies through the next one.
            Instant start = T.minus(Duration.ofDays(2L * days + 5));
            for (int d = 0; d < days; d++) {
                Instant at = start.plus(Duration.ofDays(2L * d));
                boolean forecast = GaugeCalibration.FORECAST.equals(basis);
                out.add(new GaugeCalibration.Observation(at, forecast ? "minor" : "moderate", forecast ? "moderate"
                        : null, forecast ? at.plus(Duration.ofHours(12)) : null));
                out.add(new GaugeCalibration.Observation(at.plus(Duration.ofHours(6)), d < hits ? "moderate"
                        : "action", null, null));
            }
            return out;
        }

        @Test
        @DisplayName("a forecast category, calibrated by the gauge's history (hits / n with a Jeffreys interval)")
        void forecastCalibrated() {
            HazardRecord g = gauge("minor", "moderate", T.plus(Duration.ofHours(18)));
            assertThat(HazardAdapters.gaugeBasis(g, T)).isEqualTo(GaugeCalibration.FORECAST);
            GaugeCalibration.Result c = GaugeCalibration.calibrate(history(GaugeCalibration.FORECAST, 20, 15),
                    GaugeCalibration.FORECAST, "moderate", T);
            assertThat(c.n()).isEqualTo(20);
            assertThat(c.hits()).isEqualTo(15);
            assertThat(c.p()).isEqualTo(0.75);
            assertThat(c.lower()).isLessThan(0.75);
            assertThat(c.upper()).isGreaterThan(0.75);
            HazardForecast f = HazardAdapters.gauge(g, c, "moderate", 5, T);
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_GAUGE);
            assertThat(f.category()).isEqualTo("moderate");
            assertThat(f.validTo()).isEqualTo(T.plus(Duration.ofHours(18)));
            assertThat(f.probabilityField()).singleElement().satisfies(b -> assertThat(b.pMin()).isEqualTo(0.75));
            HazardProbability.At at = HazardProbability.at(f, point(-95.01, 30.01), 5).orElseThrow();
            assertThat(at.lower()).isEqualTo(c.lower());
        }

        @Test
        @DisplayName("persistence fallback: no forecast (or a stale one) uses the current category for 24 hours")
        void persistence() {
            HazardRecord noForecast = gauge("moderate", null, null);
            assertThat(HazardAdapters.gaugeBasis(noForecast, T)).isEqualTo(GaugeCalibration.PERSISTENCE);
            HazardRecord stale = gauge("moderate", "major", T.minus(Duration.ofHours(2)));
            assertThat(HazardAdapters.gaugeBasis(stale, T)).isEqualTo(GaugeCalibration.PERSISTENCE);
            GaugeCalibration.Result c = GaugeCalibration.calibrate(history(GaugeCalibration.PERSISTENCE, 12, 6),
                    GaugeCalibration.PERSISTENCE, "moderate", T);
            assertThat(c.n()).isEqualTo(12);
            assertThat(c.p()).isEqualTo(0.5);
            HazardForecast f = HazardAdapters.gauge(noForecast, c, "moderate", 5, T);
            assertThat(f.details()).containsEntry("basis", GaugeCalibration.PERSISTENCE);
            assertThat(f.validTo()).isEqualTo(T.plus(GaugeCalibration.HORIZON));
            assertThat(f.probabilistic()).isTrue();
        }

        @Test
        @DisplayName("no probability without a calibration, or below the flood-relevant category")
        void uncalibrated() {
            GaugeCalibration.Result few = GaugeCalibration.calibrate(history(GaugeCalibration.PERSISTENCE, 5, 5),
                    GaugeCalibration.PERSISTENCE, "moderate", T);
            assertThat(few.sufficient()).isFalse();
            HazardForecast f = HazardAdapters.gauge(gauge("moderate", null, null), few, "moderate", 5, T);
            assertThat(f.probabilistic()).isFalse();
            assertThat(f.details()).containsEntry("reason", "uncalibrated");
            HazardForecast low = HazardAdapters.gauge(gauge("action", null, null), few, "moderate", 5, T);
            assertThat(low.details()).containsEntry("reason", "below-category");
        }

        @Test
        @DisplayName("samples whose horizon has not passed are left out")
        void noLookAhead() {
            List<GaugeCalibration.Observation> recent = List.of(new GaugeCalibration.Observation(T.minus(
                    Duration.ofHours(3)), "moderate", null, null));
            assertThat(GaugeCalibration.calibrate(recent, GaugeCalibration.PERSISTENCE, "moderate", T).n()).isZero();
        }
    }

    @Nested
    @DisplayName("storm-cell (105 tracks)")
    class StormCell {

        @Test
        @DisplayName("no published position error: the extrapolated area without a probability")
        void noError() {
            HazardForecast f = HazardAdapters.stormCell(record("wx-storm-tracks", "t-01J", 3, "storm-track", map(),
                    square(-97, 35, 0.2), T));
            assertThat(f.probabilityModel()).isEqualTo(HazardForecast.MODEL_TRACK_ERROR);
            assertThat(f.probabilistic()).isFalse();
            assertThat(f.issuer()).isEqualTo("foresignal");
        }
    }
}
