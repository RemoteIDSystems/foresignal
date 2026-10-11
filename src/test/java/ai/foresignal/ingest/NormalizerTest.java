package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.T0;
import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftDescriptor;
import static ai.foresignal.ingest.IngestFixtures.aircraftRecord;
import static ai.foresignal.ingest.IngestFixtures.at;
import static ai.foresignal.ingest.IngestFixtures.eventRecord;
import static ai.foresignal.ingest.IngestFixtures.events;
import static ai.foresignal.ingest.IngestFixtures.eventsDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.Quality;
import ai.foresignal.source.spi.RecordLimits;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/** The normalizer: time contract, limits, geometry, quality, validity and derived fields. */
class NormalizerTest {

    private final Normalizer normalizer = new Normalizer();

    private NormalizedBatch run(SourceIngestProfile profile, NormalizedRecord record, Instant receivedAt) {
        return normalizer.normalize(profile, List.of(new IngestRecord(record, receivedAt, 0)));
    }

    private Observation accepted(SourceIngestProfile profile, NormalizedRecord record, Instant receivedAt) {
        NormalizedBatch batch = run(profile, record, receivedAt);
        assertThat(batch.rejections()).as("rejections").isEmpty();
        return batch.observations().getFirst();
    }

    private RejectionReason rejected(SourceIngestProfile profile, NormalizedRecord record, Instant receivedAt) {
        NormalizedBatch batch = run(profile, record, receivedAt);
        assertThat(batch.observations()).as("observations").isEmpty();
        assertThat(batch.rejections()).hasSize(1);
        return batch.rejections().getFirst().reason();
    }

    private static Geometry wkt(String text) {
        try {
            return new WKTReader(Geo.factory()).read(text);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    // ---------- identity ----------

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("an external id over 256 characters is rejected")
        void longExternalId() {
            NormalizedRecord record = NormalizedRecord.builder("x".repeat(257), "earthquake").observedAt(T0).build();
            assertThat(rejected(events(), record, T0)).isEqualTo(RejectionReason.EXTERNAL_ID_INVALID);
            NormalizedRecord ok = NormalizedRecord.builder("x".repeat(256), "earthquake").observedAt(T0).build();
            assertThat(accepted(events(), ok, T0).externalId()).hasSize(256);
        }

        @Test
        @DisplayName("an undeclared kind is rejected")
        void unknownKind() {
            NormalizedRecord record = NormalizedRecord.builder("e1", "volcano").observedAt(T0).build();
            assertThat(rejected(events(), record, T0)).isEqualTo(RejectionReason.UNKNOWN_KIND);
        }

        @Test
        @DisplayName("a rejection carries the index and external id and never fails the batch")
        void rejectionDetails() {
            List<IngestRecord> in =
                    List.of(
                            new IngestRecord(eventRecord(T0).build(), T0, 0),
                            new IngestRecord(NormalizedRecord.builder("bad", "volcano").observedAt(T0).build(), T0, 1),
                            new IngestRecord(eventRecord(T0).build(), T0, 2));

            NormalizedBatch batch = normalizer.normalize(events(), in);

            assertThat(batch.observations()).hasSize(2);
            assertThat(batch.rejections())
                    .singleElement()
                    .satisfies(r -> {
                        assertThat(r.index()).isEqualTo(1);
                        assertThat(r.externalId()).isEqualTo("bad");
                        assertThat(r.reason()).isEqualTo(RejectionReason.UNKNOWN_KIND);
                        assertThat(r.detail()).contains("volcano");
                    });
        }
    }

    // ---------- time ----------

    @Nested
    @DisplayName("time base and clamp")
    class Time {

        @Test
        @DisplayName("RECEIVED mode imputes observed_at from received_at, ignoring the adapter value")
        void receivedMode() {
            SourceIngestProfile profile =
                    SourceIngestProfile.forDescriptor(eventsDescriptor().observedAtMode(ObservedAtMode.RECEIVED).build());

            Observation obs = accepted(profile, eventRecord(at(-3600)).build(), T0);

            assertThat(obs.observedAt()).isEqualTo(T0);
            assertThat(obs.hasFlag(QaFlags.TIME_IMPUTED)).isTrue();
            assertThat(accepted(profile, eventRecord(null).build(), T0).observedAt()).isEqualTo(T0);
        }

        @Test
        @DisplayName("UPSTREAM mode rejects a missing observedAt")
        void upstreamMissing() {
            assertThat(rejected(events(), eventRecord(null).build(), T0)).isEqualTo(RejectionReason.MISSING_OBSERVED_AT);
        }

        @Test
        @DisplayName("a future observed_at within skewTolerance is clamped without a flag")
        void clampWithinTolerance() {
            NormalizedBatch batch = run(events(), eventRecord(T0.plus(Duration.ofMinutes(2))).build(), T0);
            Observation obs = batch.observations().getFirst();

            assertThat(obs.observedAt()).isEqualTo(T0);
            assertThat(obs.hasFlag(QaFlags.FUTURE_CLAMPED)).isFalse();
            assertThat(batch.clamped()).isZero();
        }

        @Test
        @DisplayName("a future observed_at beyond skewTolerance is clamped, flagged and counted")
        void clampBeyondTolerance() {
            NormalizedBatch batch = run(events(), eventRecord(T0.plus(Duration.ofMinutes(2)).plusMillis(1)).build(), T0);
            Observation obs = batch.observations().getFirst();

            assertThat(obs.observedAt()).isEqualTo(T0);
            assertThat(obs.hasFlag(QaFlags.FUTURE_CLAMPED)).isTrue();
            assertThat(batch.clamped()).isEqualTo(1);
        }

        @Test
        @DisplayName("observed_at before 1970-01-02 is rejected; the floor itself is accepted")
        void epochFloor() {
            Instant floor = Instant.parse("1970-01-02T00:00:00Z");
            assertThat(rejected(events(), eventRecord(floor.minusMillis(1)).build(), T0))
                    .isEqualTo(RejectionReason.OBSERVED_AT_TOO_OLD);
            assertThat(accepted(events(), eventRecord(floor).build(), T0).observedAt()).isEqualTo(floor);
        }
    }

    // ---------- limits ----------

    @Nested
    @DisplayName("limits")
    class Limits {

        private SourceIngestProfile tight() {
            return SourceIngestProfile.forDescriptor(
                    eventsDescriptor().limits(new RecordLimits(200, 3, 5, 10, 4, 2)).build());
        }

        @Test
        @DisplayName("payload size")
        void payloadSize() {
            NormalizedRecord big = eventRecord(T0).put("blob", "x".repeat(9)).put("b2", "y".repeat(9))
                    .put("b3", "z".repeat(9)).build();
            assertThat(rejected(SourceIngestProfile.forDescriptor(
                            eventsDescriptor().limits(new RecordLimits(60, 8, 512, 8192, 10_000, 8)).build()),
                    big, T0)).isEqualTo(RejectionReason.PAYLOAD_TOO_LARGE);
        }

        @Test
        @DisplayName("the default 64 KB limit")
        void defaultPayloadSize() {
            Map<String, Object> payload = new LinkedHashMap<>();
            for (int i = 0; i < 10; i++) {
                payload.put("k" + i, "x".repeat(7000));
            }
            NormalizedRecord record = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).putAll(payload).build();
            assertThat(rejected(events(), record, T0)).isEqualTo(RejectionReason.PAYLOAD_TOO_LARGE);
        }

        @Test
        @DisplayName("nesting depth")
        void depth() {
            Map<String, Object> deep = Map.of("a", Map.of("b", Map.of("c", 1L)));
            NormalizedRecord ok = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("d", Map.of("b", 1L)).build();
            NormalizedRecord tooDeep = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("d", deep).build();
            assertThat(accepted(tight(), ok, T0)).isNotNull();
            assertThat(rejected(tight(), tooDeep, T0)).isEqualTo(RejectionReason.PAYLOAD_TOO_DEEP);
            NormalizedRecord deepList =
                    NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("d", List.of(List.of(List.of(1L)))).build();
            assertThat(rejected(tight(), deepList, T0)).isEqualTo(RejectionReason.PAYLOAD_TOO_DEEP);
        }

        @Test
        @DisplayName("total keys, counted at every level")
        void keys() {
            NormalizedRecord record =
                    NormalizedRecord.builder("e1", "earthquake")
                            .observedAt(T0)
                            .put("a", 1L)
                            .put("b", 1L)
                            .put("c", Map.of("x", 1L, "y", 2L, "z", 3L))
                            .build();
            assertThat(rejected(tight(), record, T0)).isEqualTo(RejectionReason.PAYLOAD_TOO_MANY_KEYS);
        }

        @Test
        @DisplayName("string length in UTF-8 bytes")
        void stringLength() {
            NormalizedRecord ascii = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("s", "x".repeat(10)).build();
            NormalizedRecord tooLong = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("s", "x".repeat(11)).build();
            NormalizedRecord multibyte = NormalizedRecord.builder("e1", "earthquake").observedAt(T0).put("s", "é".repeat(6)).build();
            assertThat(accepted(tight(), ascii, T0)).isNotNull();
            assertThat(rejected(tight(), tooLong, T0)).isEqualTo(RejectionReason.PAYLOAD_STRING_TOO_LONG);
            assertThat(rejected(tight(), multibyte, T0)).isEqualTo(RejectionReason.PAYLOAD_STRING_TOO_LONG);
        }

        @ParameterizedTest
        @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
        @DisplayName("NaN and infinities are rejected, nested too")
        void nonFinite(double value) {
            NormalizedRecord top = eventRecord(T0).put("v", value).build();
            NormalizedRecord nested = eventRecord(T0).put("v", Map.of("w", List.of(value))).build();
            assertThat(rejected(events(), top, T0)).isEqualTo(RejectionReason.PAYLOAD_NON_FINITE_NUMBER);
            assertThat(rejected(events(), nested, T0)).isEqualTo(RejectionReason.PAYLOAD_NON_FINITE_NUMBER);
            assertThat(rejected(events(), eventRecord(T0).put("f", (float) value).build(), T0))
                    .isEqualTo(RejectionReason.PAYLOAD_NON_FINITE_NUMBER);
        }

        @Test
        @DisplayName("observers")
        void observers() {
            NormalizedRecord three =
                    eventRecord(T0).observers(List.of(Observer.of("a"), Observer.of("b"), Observer.of("c"))).build();
            assertThat(rejected(tight(), three, T0)).isEqualTo(RejectionReason.TOO_MANY_OBSERVERS);
            List<Observer> nine = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                nine.add(Observer.of("rx" + i));
            }
            assertThat(rejected(events(), eventRecord(T0).observers(nine).build(), T0))
                    .isEqualTo(RejectionReason.TOO_MANY_OBSERVERS);
        }

        @Test
        @DisplayName("geometry vertices are counted before repair")
        void vertices() {
            NormalizedRecord five = eventRecord(T0).geometry(wkt("LINESTRING(0 1,1 1,2 1,3 1,4 1)")).build();
            assertThat(rejected(tight(), five, T0)).isEqualTo(RejectionReason.GEOMETRY_TOO_MANY_VERTICES);
            // A self-intersecting bow tie of 5 vertices that repair would turn into fewer is still refused.
            NormalizedRecord bowTie = eventRecord(T0).geometry(wkt("POLYGON((0 1,2 3,2 1,0 3,0 1))")).build();
            assertThat(rejected(tight(), bowTie, T0)).isEqualTo(RejectionReason.GEOMETRY_TOO_MANY_VERTICES);
        }

        @Test
        @DisplayName("unsupported payload values are rejected")
        void unsupportedValue() {
            NormalizedRecord record = eventRecord(T0).put("o", new Object()).build();
            assertThat(rejected(events(), record, T0)).isEqualTo(RejectionReason.PAYLOAD_UNSUPPORTED_TYPE);
        }

        @Test
        @DisplayName("null map values are dropped recursively; JSON-like values become JSON types")
        void dropsNulls() {
            Map<String, Object> nested = new HashMap<>();
            nested.put("keep", 1);
            nested.put("drop", null);
            NormalizedRecord record =
                    eventRecord(T0)
                            .put("n", nested)
                            .put("when", T0)
                            .put("mode", ObservedAtMode.RECEIVED)
                            .put("ints", new int[] {1, 2})
                            .build();

            Observation obs = accepted(events(), record, T0);

            assertThat(obs.payload().get("n")).isEqualTo(Map.of("keep", 1L));
            assertThat(obs.payload()).containsEntry("when", T0.toString()).containsEntry("mode", "RECEIVED");
            assertThat(obs.payload().get("ints")).isEqualTo(List.of(1L, 2L));
        }
    }

    // ---------- geometry ----------

    @Nested
    @DisplayName("geometry")
    class Geometries {

        @Test
        @DisplayName("Z and M are dropped; the result is 2D in SRID 4326")
        void forces2d() {
            GeometryFactory plain = new GeometryFactory(new PrecisionModel(), 0);
            Point withZ = plain.createPoint(new Coordinate(10, 20, 3000));

            Observation obs = accepted(events(), eventRecord(T0).geometry(withZ).build(), T0);

            assertThat(obs.geometry().getSRID()).isEqualTo(4326);
            assertThat(Double.isNaN(obs.geometry().getCoordinate().getZ())).isTrue();
            assertThat(obs.geometry().getCoordinate().x).isEqualTo(10.0);
        }

        @Test
        @DisplayName("coordinates outside ±180 / ±90 are rejected")
        void outOfRange() {
            GeometryFactory plain = new GeometryFactory();
            NormalizedRecord lon = eventRecord(T0).geometry(plain.createPoint(new Coordinate(181, 0))).build();
            NormalizedRecord lat = eventRecord(T0).geometry(wkt("LINESTRING(0 0,1 91)")).build();
            assertThat(rejected(events(), lon, T0)).isEqualTo(RejectionReason.COORDINATES_OUT_OF_RANGE);
            assertThat(rejected(events(), lat, T0)).isEqualTo(RejectionReason.COORDINATES_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("an SRID other than 4326 is rejected")
        void wrongSrid() {
            GeometryFactory mercator = new GeometryFactory(new PrecisionModel(), 3857);
            NormalizedRecord record = eventRecord(T0).geometry(mercator.createPoint(new Coordinate(1, 1))).build();
            assertThat(rejected(events(), record, T0)).isEqualTo(RejectionReason.GEOMETRY_WRONG_SRID);
        }

        @Test
        @DisplayName("invalid geometry is repaired and flagged GEOMETRY_FIXED")
        void geometryFixer() {
            Observation obs =
                    accepted(events(), eventRecord(T0).geometry(wkt("POLYGON((0 1,2 3,2 1,0 3,0 1))")).build(), T0);

            assertThat(obs.geometry().isValid()).isTrue();
            assertThat(obs.hasFlag(QaFlags.GEOMETRY_FIXED)).isTrue();
        }

        @Test
        @DisplayName("a repair that empties the geometry leaves none")
        void repairToEmpty() {
            Observation obs =
                    accepted(events(), eventRecord(T0).geometry(wkt("POLYGON((0 1,1 1,2 1,0 1))")).build(), T0);

            assertThat(obs.geometry()).isNull();
            assertThat(obs.hasFlag(QaFlags.GEOMETRY_FIXED)).isTrue();
        }

        @Test
        @DisplayName("an exact (0,0) point becomes null with NULL_ISLAND unless the source allows it")
        void nullIsland() {
            Observation obs = accepted(events(), eventRecord(T0).point(0, 0).build(), T0);
            assertThat(obs.geometry()).isNull();
            assertThat(obs.hasFlag(QaFlags.NULL_ISLAND)).isTrue();

            SourceIngestProfile allowed =
                    SourceIngestProfile.forDescriptor(eventsDescriptor().allowNullIsland(true).build());
            Observation kept = accepted(allowed, eventRecord(T0).point(0, 0).build(), T0);
            assertThat(kept.geometry()).isNotNull();
            assertThat(kept.hasFlag(QaFlags.NULL_ISLAND)).isFalse();
        }

        @Test
        @DisplayName("an empty geometry is treated as none")
        void emptyGeometry() {
            Observation obs = accepted(events(), eventRecord(T0).geometry(wkt("POINT EMPTY")).build(), T0);
            assertThat(obs.geometry()).isNull();
            assertThat(obs.qaFlags()).isZero();
        }

        @Test
        @DisplayName("collections are forced to 2D member by member")
        void collections() {
            Observation obs =
                    accepted(
                            events(),
                            eventRecord(T0).geometry(wkt("GEOMETRYCOLLECTION(POINT(1 2 3),MULTILINESTRING((0 1,1 2)))")).build(),
                            T0);
            assertThat(obs.geometry().getNumGeometries()).isEqualTo(2);
            assertThat(obs.geometry().getSRID()).isEqualTo(4326);
        }

        @Test
        @DisplayName("private sources keep only privatePrecisionDecimals, before hashing")
        void privatePrecision() {
            SourceIngestProfile profile =
                    SourceIngestProfile.forDescriptor(eventsDescriptor().privateSource(true).privatePrecisionDecimals(3).build());

            Observation obs = accepted(profile, eventRecord(T0).point(-122.123456, 37.987654).build(), T0);
            Observation near = accepted(profile, eventRecord(T0).point(-122.12349, 37.98771).build(), T0);

            assertThat(obs.geometry().getCoordinate().x).isEqualTo(-122.123);
            assertThat(obs.geometry().getCoordinate().y).isEqualTo(37.988);
            assertThat(obs.geometry().getSRID()).isEqualTo(4326);
            assertThat(ContentHasher.fullHash(near, profile)).isEqualTo(ContentHasher.fullHash(obs, profile));
            Observation open = accepted(events(), eventRecord(T0).point(-122.123456, 37.987654).build(), T0);
            assertThat(open.geometry().getCoordinate().x).isEqualTo(-122.123456);
        }
    }

    // ---------- quality ----------

    @Nested
    @DisplayName("quality")
    class QualityValidation {

        @Test
        @DisplayName("valid quality is accepted")
        void valid() {
            Quality quality = new Quality(0.0, 1.0, null, Map.of("adsb.seen_pos", 1.5, "ais.raim", true, "x1.y_2", "s"));
            assertThat(accepted(events(), eventRecord(T0).quality(quality).build(), T0).quality()).isEqualTo(quality);
        }

        @Test
        @DisplayName("negative or non-finite accuracy, confidence outside [0,1], bad extra keys or values are rejected")
        void invalid() {
            List<Quality> bad =
                    List.of(
                            new Quality(-1.0, null, null, Map.of()),
                            new Quality(Double.NaN, null, null, Map.of()),
                            new Quality(Double.POSITIVE_INFINITY, null, null, Map.of()),
                            new Quality(null, 1.01, null, Map.of()),
                            new Quality(null, -0.01, null, Map.of()),
                            new Quality(null, Double.NaN, null, Map.of()),
                            new Quality(null, null, null, Map.of("noNamespace", 1)),
                            new Quality(null, null, null, Map.of("Adsb.seen", 1)),
                            new Quality(null, null, null, Map.of("adsb.seen", Map.of("a", 1))),
                            new Quality(null, null, null, Map.of("adsb.seen", Double.NaN)));
            for (Quality quality : bad) {
                assertThat(rejected(events(), eventRecord(T0).quality(quality).build(), T0))
                        .as("%s", quality)
                        .isEqualTo(RejectionReason.QUALITY_INVALID);
            }
        }
    }

    // ---------- validity ----------

    @Nested
    @DisplayName("validity")
    class Validity {

        @Test
        @DisplayName("TRACK sources ignore an explicit validUntil, and count it")
        void trackIgnoresExplicit() {
            NormalizedBatch batch = run(aircraft(), aircraftRecord(T0).validUntil(at(30)).build(), T0);
            Observation obs = batch.observations().getFirst();

            assertThat(obs.validUntilExplicit()).isNull();
            assertThat(obs.validUntil()).isEqualTo(at(300));
            assertThat(batch.validUntilIgnored()).isEqualTo(1);
        }

        @Test
        @DisplayName("STANDARD keeps the explicit validUntil as the effective one")
        void standardKeepsExplicit() {
            Observation obs = accepted(events(), eventRecord(T0).validUntil(at(30)).build(), T0);
            assertThat(obs.validUntilExplicit()).isEqualTo(at(30));
            assertThat(obs.validUntil()).isEqualTo(at(30));
        }

        @Test
        @DisplayName("effective validUntil counts from the anchor: RECEIVED")
        void receivedAnchor() {
            Observation obs = accepted(events(), eventRecord(T0).build(), at(100));
            assertThat(obs.validUntil()).isEqualTo(at(100).plus(Duration.ofMinutes(10)));
        }

        @Test
        @DisplayName("effective validUntil counts from the anchor: OBSERVED")
        void observedAnchor() {
            SourceIngestProfile profile =
                    SourceIngestProfile.forDescriptor(eventsDescriptor().ttlAnchor(TtlAnchor.OBSERVED).build());
            Observation obs = accepted(profile, eventRecord(T0).build(), at(100));
            assertThat(obs.validUntil()).isEqualTo(T0.plus(Duration.ofMinutes(10)));
        }

        @Test
        @DisplayName("without a TTL and without an explicit value there is no expiry")
        void noTtl() {
            assertThat(accepted(IngestFixtures.catalog(), IngestFixtures.catalogRecord(T0).build(), T0).validUntil())
                    .isNull();
        }

        @Test
        @DisplayName("validFrom is kept; validFrom after validUntil is rejected")
        void validFrom() {
            Observation obs = accepted(events(), eventRecord(T0).validFrom(at(10)).validUntil(at(10)).build(), T0);
            assertThat(obs.validFrom()).isEqualTo(at(10));
            assertThat(rejected(events(), eventRecord(T0).validFrom(at(11)).validUntil(at(10)).build(), T0))
                    .isEqualTo(RejectionReason.VALID_RANGE_INVERTED);
            assertThat(accepted(events(), eventRecord(T0).validFrom(at(11)).build(), T0).validFrom()).isEqualTo(at(11));
        }
    }

    // ---------- tombstones, order, derived ----------

    @Test
    @DisplayName("tombstones are stored with no geometry and an empty payload, kind kept")
    void tombstoneNormalization() {
        NormalizedRecord record =
                NormalizedRecord.builder("ev1", "earthquake")
                        .observedAt(T0)
                        .tombstone(true)
                        .point(1, 1)
                        .put("mag", 5.0)
                        .put("bad", Double.NaN)
                        .build();

        Observation obs = accepted(events(), record, T0);

        assertThat(obs.tombstone()).isTrue();
        assertThat(obs.geometry()).isNull();
        assertThat(obs.payload()).isEmpty();
        assertThat(obs.kind()).isEqualTo("earthquake");
        assertThat(obs.title()).isNull();
        assertThat(obs.searchText()).isNull();
    }

    @Test
    @DisplayName("duplicates are kept and the order is stable by (externalId, observedAt, arrival)")
    void duplicatesAndOrder() {
        List<IngestRecord> in =
                List.of(
                        new IngestRecord(eventRecord(at(5)).build(), at(10), 0),
                        new IngestRecord(NormalizedRecord.builder("ev0", "earthquake").observedAt(at(9)).build(), at(10), 1),
                        new IngestRecord(eventRecord(at(5)).build(), at(10), 2),
                        new IngestRecord(eventRecord(at(1)).build(), at(10), 3),
                        new IngestRecord(eventRecord(at(5)).put("mag", 6.0).build(), at(10), 4));

        NormalizedBatch batch = normalizer.normalize(events(), in);

        assertThat(batch.observations()).extracting(Observation::arrival).containsExactly(1L, 3L, 0L, 2L, 4L);
        assertThat(batch.observations()).extracting(Observation::index).containsExactly(1, 3, 0, 2, 4);
    }

    @Test
    @DisplayName("search text, title and entity keys are derived")
    void derivedFields() {
        SourceDescriptor descriptor =
                aircraftDescriptor()
                        .searchFields("flight", "owner.name", "tags")
                        .titleField("flight")
                        .entityKey("icao24", "hex")
                        .build();
        SourceIngestProfile profile = SourceIngestProfile.forDescriptor(descriptor);
        NormalizedRecord record =
                aircraftRecord(T0)
                        .put("hex", "A1B2C3")
                        .put("owner", Map.of("name", "ACME Air"))
                        .put("tags", List.of("Cargo", 7L))
                        .build();

        Observation obs = accepted(profile, record, T0);

        assertThat(obs.searchText()).isEqualTo("ual1 acme air cargo 7");
        assertThat(obs.title()).isEqualTo("UAL1");
        assertThat(obs.entityKeys()).containsExactly(Map.entry("icao24", "a1b2c3"));
    }

    @Test
    @DisplayName("search text is cut at 1,000 characters and null when empty")
    void searchTextLimits() {
        assertThat(Normalizer.searchText(List.of("a"), Map.of("a", "X".repeat(1500)))).hasSize(1000).isEqualTo("x".repeat(1000));
        assertThat(Normalizer.searchText(List.of("a"), Map.of("b", "x"))).isNull();
        assertThat(Normalizer.searchText(List.of(), Map.of("a", "x"))).isNull();
    }

    @Test
    @DisplayName("a custom entity key extractor is used")
    void customExtractor() {
        Normalizer custom = new Normalizer((p, payload) -> Map.of("custom", "k"));
        Observation obs =
                custom.normalize(events(), List.of(new IngestRecord(eventRecord(T0).build(), T0, 0))).observations().getFirst();
        assertThat(obs.entityKeys()).containsEntry("custom", "k");
    }
}
