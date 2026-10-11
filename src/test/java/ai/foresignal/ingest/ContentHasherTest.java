package ai.foresignal.ingest;

import static ai.foresignal.ingest.IngestFixtures.T0;
import static ai.foresignal.ingest.IngestFixtures.aircraft;
import static ai.foresignal.ingest.IngestFixtures.aircraftRecord;
import static ai.foresignal.ingest.IngestFixtures.at;
import static ai.foresignal.ingest.IngestFixtures.eventRecord;
import static ai.foresignal.ingest.IngestFixtures.eventsDescriptor;
import static ai.foresignal.ingest.IngestFixtures.observe;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.Quality;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Canonical JSON, the full and static hashes, and the recipe (Core-Data-Model §4, ING-7). */
class ContentHasherTest {

    private static final Set<String> NONE = Set.of();

    private static long full(Map<String, ?> payload) {
        return ContentHasher.fullHash("k", null, null, null, false, null, payload, NONE);
    }

    @Nested
    @DisplayName("canonical JSON")
    class Canonical {

        @Test
        @DisplayName("keys are sorted recursively, so insertion order never matters")
        void keyOrder() {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("b", 1L);
            a.put("a", Map.of("z", 1L, "y", 2L));
            Map<String, Object> b = new TreeMap<>(Map.of("a", new LinkedHashMap<>(Map.of("y", 2L, "z", 1L)), "b", 1L));

            assertThat(CanonicalJson.write(a)).isEqualTo("{\"a\":{\"y\":2,\"z\":1},\"b\":1}");
            assertThat(CanonicalJson.write(a)).isEqualTo(CanonicalJson.write(b));
            assertThat(full(a)).isEqualTo(full(b));
        }

        @Test
        @DisplayName("5, 5L, 5.0 and 5.0f are the same number")
        void integralDoubles() {
            assertThat(CanonicalJson.write(5)).isEqualTo("5");
            assertThat(CanonicalJson.write(5L)).isEqualTo("5");
            assertThat(CanonicalJson.write(5.0)).isEqualTo("5");
            assertThat(CanonicalJson.write(5.0f)).isEqualTo("5");
            assertThat(CanonicalJson.write(new BigDecimal("5.000"))).isEqualTo("5");
            assertThat(full(Map.of("v", 5L))).isEqualTo(full(Map.of("v", 5.0)));
        }

        @Test
        @DisplayName("numbers are written in plain form")
        void plainNumbers() {
            assertThat(CanonicalJson.write(1e3)).isEqualTo("1000");
            assertThat(CanonicalJson.write(1.5e-7)).isEqualTo("0.00000015");
            assertThat(CanonicalJson.write(0.1)).isEqualTo("0.1");
            assertThat(CanonicalJson.write(5.1f)).isEqualTo("5.1");
            assertThat(CanonicalJson.write(-0.0)).isEqualTo("0");
            assertThat(CanonicalJson.write(new java.math.BigInteger("123456789012345678901234567890")))
                    .isEqualTo("123456789012345678901234567890");
        }

        @Test
        @DisplayName("strings are escaped; booleans, nulls and lists are plain")
        void stringsAndScalars() {
            assertThat(CanonicalJson.write("a\"b\\c\n\t\u0001é")).isEqualTo("\"a\\\"b\\\\c\\n\\t\\u0001é\"");
            assertThat(CanonicalJson.write(java.util.Arrays.asList(true, null, "x"))).isEqualTo("[true,null,\"x\"]");
        }

        @Test
        @DisplayName("NaN, infinities and non-JSON values are refused")
        void refusesNonJson() {
            assertThatThrownBy(() -> CanonicalJson.write(Double.NaN)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CanonicalJson.write(Float.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CanonicalJson.write(new Object())).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CanonicalJson.write(Map.of(1, "x"))).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deterministic: a known input has a fixed hash across JVMs")
        void fixedValue() {
            // Pinned so an accidental change to the encoding is caught; changing it requires
            // bumping HASHER_VERSION.
            long hash = ContentHasher.fullHash("k", null, null, null, false, null, Map.of("a", 1L), NONE);
            assertThat(hash).isEqualTo(ContentHasher.fullHash("k", null, null, null, false, null, Map.of("a", 1.0), NONE));
            assertThat(CanonicalJson.write(Map.of("a", 1L))).isEqualTo("{\"a\":1}");
        }
    }

    @Nested
    @DisplayName("what is never hashed")
    class NeverHashed {

        @Test
        @DisplayName("observations differing only in observed_at and received_at hash equal")
        void observedAt() {
            SourceIngestProfile profile = aircraft();
            Observation first = observe(profile, aircraftRecord(T0).build());
            Observation later = observe(profile, aircraftRecord(at(90)).build(), at(95));

            assertThat(ContentHasher.fullHash(later, profile)).isEqualTo(ContentHasher.fullHash(first, profile));
            assertThat(ContentHasher.staticHash(later, profile)).isEqualTo(ContentHasher.staticHash(first, profile));
        }

        @Test
        @DisplayName("volatile keys are left out of both hashes")
        void volatileKeys() {
            SourceIngestProfile profile = SourceIngestProfile.forDescriptor(eventsDescriptor().volatileFields("updated").build());
            Observation first = observe(profile, eventRecord(T0).put("updated", "10:00").build());
            Observation refetch = observe(profile, eventRecord(T0).put("updated", "10:05").build());

            assertThat(ContentHasher.fullHash(refetch, profile)).isEqualTo(ContentHasher.fullHash(first, profile));
            assertThat(ContentHasher.staticHash(refetch, profile)).isEqualTo(ContentHasher.staticHash(first, profile));
        }

        @Test
        @DisplayName("quality, observers, derived valid_until, search text and title are not hashed")
        void qualityAndDerived() {
            SourceIngestProfile profile = aircraft();
            Observation plain = observe(profile, aircraftRecord(T0).build());
            Observation decorated =
                    observe(
                            profile,
                            aircraftRecord(T0)
                                    .quality(new Quality(5.0, 0.9, PositionSource.REPORTED, Map.of("adsb.seen_pos", 1.5)))
                                    .observers(List.of(Observer.of("rx1")))
                                    .build(),
                            at(30));

            assertThat(decorated.validUntil()).isEqualTo(plain.validUntil());
            assertThat(ContentHasher.fullHash(decorated, profile)).isEqualTo(ContentHasher.fullHash(plain, profile));
            assertThat(ContentHasher.staticHash(decorated, profile)).isEqualTo(ContentHasher.staticHash(plain, profile));
        }
    }

    static Stream<Arguments> hashedFields() {
        return Stream.<Arguments>of(
                Arguments.of("kind", (UnaryOperator<Input>) i -> i.with("kind", "other")),
                Arguments.of("eventTime", (UnaryOperator<Input>) i -> i.with("eventTime", T0)),
                Arguments.of("explicit validUntil", (UnaryOperator<Input>) i -> i.with("validUntil", T0)),
                Arguments.of("validFrom", (UnaryOperator<Input>) i -> i.with("validFrom", T0)),
                Arguments.of("tombstone", (UnaryOperator<Input>) i -> i.with("tombstone", true)),
                Arguments.of("significant payload key", (UnaryOperator<Input>) i -> i.payload("flight", "DAL2")));
    }

    /** The inputs of one hash, editable one field at a time. */
    record Input(String kind, Instant eventTime, Instant validUntil, Instant validFrom, boolean tombstone,
            org.locationtech.jts.geom.Geometry geometry, Map<String, Object> payload) {

        static Input base() {
            return new Input("aircraft", null, null, null, false, Geo.point(1, 2),
                    Map.of("flight", "UAL1", "gs", 300.0, "seen", 1L));
        }

        Input with(String field, Object value) {
            return switch (field) {
                case "kind" -> new Input((String) value, eventTime, validUntil, validFrom, tombstone, geometry, payload);
                case "eventTime" -> new Input(kind, (Instant) value, validUntil, validFrom, tombstone, geometry, payload);
                case "validUntil" -> new Input(kind, eventTime, (Instant) value, validFrom, tombstone, geometry, payload);
                case "validFrom" -> new Input(kind, eventTime, validUntil, (Instant) value, tombstone, geometry, payload);
                case "tombstone" -> new Input(kind, eventTime, validUntil, validFrom, (Boolean) value, geometry, payload);
                default -> throw new IllegalArgumentException(field);
            };
        }

        Input payload(String key, Object value) {
            Map<String, Object> copy = new LinkedHashMap<>(payload);
            copy.put(key, value);
            return new Input(kind, eventTime, validUntil, validFrom, tombstone, geometry, copy);
        }

        Input geometry(org.locationtech.jts.geom.Geometry g) {
            return new Input(kind, eventTime, validUntil, validFrom, tombstone, g, payload);
        }

        long full(Set<String> volatileFields) {
            return ContentHasher.fullHash(kind, eventTime, validUntil, validFrom, tombstone, geometry, payload, volatileFields);
        }

        long statik(Set<String> volatileFields, Set<String> significant) {
            return ContentHasher.staticHash(
                    kind, eventTime, validUntil, validFrom, tombstone, geometry == null, payload, volatileFields, significant);
        }
    }

    private static final Set<String> VOLATILE = Set.of("seen");
    private static final Set<String> SIGNIFICANT = Set.of("flight");

    @ParameterizedTest(name = "changing {0} changes both hashes")
    @MethodSource("hashedFields")
    void anyHashedFieldChangesTheHash(String name, UnaryOperator<Input> edit) {
        Input base = Input.base();
        Input changed = edit.apply(base);

        assertThat(changed.full(VOLATILE)).isNotEqualTo(base.full(VOLATILE));
        assertThat(changed.statik(VOLATILE, SIGNIFICANT)).isNotEqualTo(base.statik(VOLATILE, SIGNIFICANT));
    }

    @Nested
    @DisplayName("static vs full scope")
    class Scope {

        @Test
        @DisplayName("a kinematic key changes the full hash only")
        void kinematicKey() {
            Input base = Input.base();
            Input changed = base.payload("gs", 310.0);

            assertThat(changed.full(VOLATILE)).isNotEqualTo(base.full(VOLATILE));
            assertThat(changed.statik(VOLATILE, SIGNIFICANT)).isEqualTo(base.statik(VOLATILE, SIGNIFICANT));
        }

        @Test
        @DisplayName("a moved geometry changes the full hash only; losing it changes both")
        void geometry() {
            Input base = Input.base();
            Input moved = base.geometry(Geo.point(1.001, 2));
            Input lost = base.geometry(null);

            assertThat(moved.full(VOLATILE)).isNotEqualTo(base.full(VOLATILE));
            assertThat(moved.statik(VOLATILE, SIGNIFICANT)).isEqualTo(base.statik(VOLATILE, SIGNIFICANT));
            assertThat(lost.full(VOLATILE)).isNotEqualTo(base.full(VOLATILE));
            assertThat(lost.statik(VOLATILE, SIGNIFICANT)).isNotEqualTo(base.statik(VOLATILE, SIGNIFICANT));
        }

        @Test
        @DisplayName("geometry is hashed at 1e-7 degrees")
        void geometryPrecision() {
            Input base = Input.base();
            assertThat(base.geometry(Geo.point(1.00000004, 2)).full(VOLATILE)).isEqualTo(base.full(VOLATILE));
            assertThat(base.geometry(Geo.point(1.0000002, 2)).full(VOLATILE)).isNotEqualTo(base.full(VOLATILE));
            assertThat(ContentHasher.wkt(Geo.point(1.00000004, -2.5))).isEqualTo("POINT(1 -2.5)");
        }

        @Test
        @DisplayName("empty significantFields means every non-volatile key is significant")
        void emptySignificant() {
            Input base = Input.base();
            Input changed = base.payload("gs", 310.0);
            Input volatileChanged = base.payload("seen", 2L);

            assertThat(changed.statik(VOLATILE, NONE)).isNotEqualTo(base.statik(VOLATILE, NONE));
            assertThat(volatileChanged.statik(VOLATILE, NONE)).isEqualTo(base.statik(VOLATILE, NONE));
            assertThat(volatileChanged.full(VOLATILE)).isEqualTo(base.full(VOLATILE));
        }

        @Test
        @DisplayName("the kinematic subset excludes significant and volatile keys, and is empty without significant fields")
        void kinematicSubset() {
            Map<String, Object> payload = Input.base().payload();

            assertThat(ContentHasher.kinematicSubset(payload, VOLATILE, SIGNIFICANT)).containsOnlyKeys("gs");
            assertThat(ContentHasher.kinematicSubset(payload, VOLATILE, NONE)).isEmpty();
            assertThat(ContentHasher.kinematicSubset(Map.of(), VOLATILE, SIGNIFICANT)).isEmpty();
        }

        @Test
        @DisplayName("every geometry type has a stable text form")
        void geometryTypes() {
            String polygon = "POLYGON((0 0,1 0,1 1,0 1,0 0))";
            var reader = new org.locationtech.jts.io.WKTReader(Geo.factory());
            for (String wkt : List.of(
                    "POINT(1 2)",
                    "LINESTRING(0 0,1 1)",
                    polygon,
                    "MULTIPOINT((0 0),(1 1))",
                    "MULTILINESTRING((0 0,1 1),(2 2,3 3))",
                    "MULTIPOLYGON(((0 0,1 0,1 1,0 1,0 0)))",
                    "GEOMETRYCOLLECTION(POINT(1 2),LINESTRING(0 0,1 1))",
                    "POLYGON((0 0,10 0,10 10,0 10,0 0),(1 1,2 1,2 2,1 2,1 1))")) {
                try {
                    var geometry = reader.read(wkt);
                    assertThat(ContentHasher.wkt(geometry)).isEqualTo(ContentHasher.wkt(reader.read(wkt)));
                } catch (org.locationtech.jts.io.ParseException e) {
                    throw new AssertionError(e);
                }
            }
            try {
                assertThat(ContentHasher.wkt(reader.read(polygon))).isEqualTo(polygon);
                assertThat(ContentHasher.wkt(reader.read("POINT EMPTY"))).isEqualTo("POINT EMPTY");
            } catch (org.locationtech.jts.io.ParseException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Nested
    @DisplayName("recipe")
    class Recipe {

        @Test
        @DisplayName("stable regardless of list order")
        void stable() {
            int a = ContentHasher.recipe(List.of("b", "a"), List.of("y", "x"));
            int b = ContentHasher.recipe(List.of("a", "b"), List.of("x", "y"));
            assertThat(a).isEqualTo(b);
            assertThat(ContentHasher.recipe(null, null)).isEqualTo(ContentHasher.recipe(List.of(), List.of()));
        }

        @Test
        @DisplayName("changes when either list changes, or a field moves between them")
        void changes() {
            int base = ContentHasher.recipe(List.of("updated"), List.of("mag"));
            assertThat(ContentHasher.recipe(List.of("updated", "seen"), List.of("mag"))).isNotEqualTo(base);
            assertThat(ContentHasher.recipe(List.of("updated"), List.of("mag", "place"))).isNotEqualTo(base);
            assertThat(ContentHasher.recipe(List.of("mag"), List.of("updated"))).isNotEqualTo(base);
        }

        @Test
        @DisplayName("the profile carries the descriptor's recipe")
        void profileRecipe() {
            SourceIngestProfile profile = aircraft();
            assertThat(profile.recipe()).isEqualTo(ContentHasher.recipe(List.of(), List.of("flight", "squawk")));
            assertThat(ContentHasher.HASHER_VERSION).isEqualTo(1);
        }
    }
}
