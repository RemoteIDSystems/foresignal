package ai.foresignal.source.adapters.ioda;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.Admin1Centroids;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.reference.ReferenceDataChangedEvent;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * {@link IodaGeometry} (prompt 068): country label points from the bundled table, regions matched by (country,
 * normalized name) in the admin-1 table, unmatched regions counted, ASNs without geometry, and
 * {@code geometryApproximate} on every placed record. No network is involved.
 */
class IodaGeometryTest {

    static final String ID = IodaOutagesAdapter.ID;

    /** Admin-1 rows standing in for {@code ref_admin1}. */
    static final Map<String, List<Admin1Centroids.Centroid>> ADMIN1 = Map.of(
            "BR", List.of(new Admin1Centroids.Centroid("BR-ES", "BR", "Espírito Santo", -40.6, -19.6)),
            "PK", List.of(new Admin1Centroids.Centroid("PK-BA", "PK", "Balochistan", 65.4, 28.3)),
            "CD", List.of(new Admin1Centroids.Centroid("CD-SK", "CD", "Sud-Kivu", 28.3, -3.0)));

    SimpleMeterRegistry meters;
    List<String> loads;
    CountryCentroids countries;
    IodaGeometry geometry;
    List<IodaAlertsParser.Alert> folded;

    @BeforeEach
    void setUp() {
        meters = new SimpleMeterRegistry();
        loads = new ArrayList<>();
        countries = new CountryCentroids();
        Admin1Centroids admin1 = new Admin1Centroids(iso2 -> {
            loads.add(iso2);
            return ADMIN1.getOrDefault(iso2, List.of());
        });
        geometry = new IodaGeometry(countries, admin1, meters);
        folded = IodaAlertsParser.fold(IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "alerts-12h.json")).alerts(),
                Set.copyOf(IodaAlertsParser.ENTITY_TYPES), Set.copyOf(IodaAlertsParser.DATASOURCES));
    }

    private IodaAlertsParser.Alert find(String key) {
        return folded.stream().filter(a -> a.key().equals(key)).findFirst().orElseThrow();
    }

    private double unmatched() {
        return meters.counter(IodaGeometry.REGION_UNMATCHED).count();
    }

    @Test
    @DisplayName("a country is placed at the bundled table's label point")
    void country() {
        CountryRef haiti = countries.byIso2("HT").orElseThrow();

        Point p = geometry.resolve(find("country:HT:bgp"));

        assertThat(p).isNotNull();
        assertThat(p.getX()).isEqualTo(haiti.labelLon());
        assertThat(p.getY()).isEqualTo(haiti.labelLat());
        assertThat(p.getSRID()).isEqualTo(4326);
        for (String code : List.of("SS", "CD", "BI", "DM", "LI", "NF")) {
            assertThat(geometry.resolve(folded.stream().filter(a -> a.entityType().equals("country")
                    && a.entityCode().equals(code)).findFirst().orElseThrow())).as(code).isNotNull();
        }
    }

    @Test
    @DisplayName("a region is matched by country and accent-insensitive name; the country's rows are read once")
    void regionByName() {
        Point santo = geometry.resolve(find("region:497:merit-nt"));
        Point kivu = geometry.resolve(find("region:694:bgp"));
        geometry.resolve(find("region:497:merit-nt"));

        assertThat(santo.getX()).isEqualTo(-40.6);
        assertThat(santo.getY()).isEqualTo(-19.6);
        assertThat(kivu.getX()).isEqualTo(28.3);
        assertThat(unmatched()).isZero();
        assertThat(loads).containsExactly("BR", "CD");
    }

    @Test
    @DisplayName("an unmatched region has null geometry and is counted")
    void unmatchedRegion() {
        // NetAcuity's "Baluchistan" is Natural Earth's "Balochistan": no crosswalk, so no match.
        assertThat(geometry.resolve(find("region:3082:ping-slash24"))).isNull();
        IodaAlertsParser.Alert atlantis = IodaAlertsParser.parse(AdapterFixtures.bytes(ID, "unknown-region.json"))
                .alerts().get(0);
        assertThat(geometry.resolve(atlantis)).isNull();

        assertThat(unmatched()).isEqualTo(2.0);
        NormalizedRecord r = IodaAlertsParser.toRecord(atlantis, null);
        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).doesNotContainKey("geometryApproximate").containsEntry("country", "GR");
    }

    @Test
    @DisplayName("an ASN has null geometry and is not counted")
    void asn() {
        assertThat(geometry.resolve(find("asn:8643:ping-slash24"))).isNull();
        assertThat(unmatched()).isZero();
    }

    @Test
    @DisplayName("every placed record carries geometryApproximate: true")
    void approximate() {
        int placed = 0;
        for (IodaAlertsParser.Alert a : folded) {
            Point p = geometry.resolve(a);
            NormalizedRecord r = IodaAlertsParser.toRecord(a, p);
            if (p != null) {
                placed++;
                assertThat(r.payload()).as(a.key()).containsEntry("geometryApproximate", true);
            } else {
                assertThat(r.payload()).as(a.key()).doesNotContainKey("geometryApproximate");
            }
        }
        assertThat(placed).as("7 countries and 2 matched regions").isEqualTo(9);
    }

    @Test
    @DisplayName("a database error is not cached; a reference change drops the cache")
    void cache() {
        List<String> calls = new ArrayList<>();
        boolean[] fail = {true};
        Admin1Centroids admin1 = new Admin1Centroids(iso2 -> {
            calls.add(iso2);
            if (fail[0]) {
                throw new DataAccessResourceFailureException("down");
            }
            return ADMIN1.getOrDefault(iso2, List.of());
        });

        assertThat(admin1.byName("BR", "Espirito Santo")).isEmpty();
        fail[0] = false;
        assertThat(admin1.byName("br", "ESPÍRITO SANTO")).isPresent();
        assertThat(admin1.byName("BR", "Espírito-Santo")).isPresent();
        assertThat(calls).containsExactly("BR", "BR");

        admin1.onReferenceChanged(new ReferenceDataChangedEvent(List.of("ne-admin1")));
        assertThat(admin1.byName("BR", "Espírito Santo")).isPresent();
        assertThat(calls).containsExactly("BR", "BR", "BR");

        assertThat(admin1.byName(null, "x")).isEmpty();
        assertThat(admin1.byName("BR", " ")).isEmpty();
    }

    @Test
    @DisplayName("without lookup tables nothing is placed")
    void noTables() {
        IodaGeometry bare = new IodaGeometry(null, null, null);
        assertThat(bare.resolve(find("country:HT:bgp"))).isNull();
        assertThat(bare.resolve(find("region:497:merit-nt"))).isNull();
    }
}
