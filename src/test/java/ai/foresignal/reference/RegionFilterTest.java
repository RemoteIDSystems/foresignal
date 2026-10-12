package ai.foresignal.reference;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.trigger.GeofenceRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The SQL and in-memory forms of the 040 region filters and geofence presets: members ANDed, codes ORed,
 * every value bound, null tags never matching, and the stored form of a preset.
 */
class RegionFilterTest {

    @Test
    @DisplayName("of() is null without codes; the canonical form is order-independent")
    void construction() {
        assertThat(RegionFilter.of(List.of(), List.of(), List.of())).isNull();
        assertThat(RegionFilter.of(List.of("US", "MX"), List.of(), List.of()).canonical())
                .isEqualTo(RegionFilter.of(List.of("MX", "US"), null, null).canonical());
    }

    @Test
    @DisplayName("the record predicate joins record_region on the key and binds every list")
    void recordSql() {
        List<List<String>> bound = new ArrayList<>();
        String sql = RegionFilter.of(List.of("US"), List.of("US-TX"), List.of("48453")).sql("h.source_id",
                "h.external_id", codes -> {
                    bound.add(codes);
                    return ":p" + bound.size();
                });
        assertThat(sql).isEqualTo("EXISTS (SELECT 1 FROM record_region rr WHERE rr.source_id = h.source_id AND"
                + " rr.external_id = h.external_id AND rr.iso2 = ANY(:p1) AND rr.admin1 = ANY(:p2)"
                + " AND rr.admin2 = ANY(:p3))");
        assertThat(bound).containsExactly(List.of("US"), List.of("US-TX"), List.of("48453"));
    }

    @Test
    @DisplayName("the alert predicate reads the region snapshot; the in-memory test agrees")
    void alertSqlAndTest() {
        RegionFilter filter = RegionFilter.of(List.of("US"), List.of(), List.of("48453"));
        assertThat(filter.jsonSql("a.region", codes -> "?")).isEqualTo("(a.region IS NOT NULL AND"
                + " (a.region ->> 'iso2') = ANY(?) AND (a.region ->> 'admin2') = ANY(?))");
        RegionTags travis = new RegionTags("US", "US-TX", "48453", null, null, null, null, null);
        RegionTags harris = new RegionTags("US", "US-TX", "48201", null, null, null, null, null);
        assertThat(filter.test(travis)).isTrue();
        assertThat(filter.test(harris)).isFalse();
        assertThat(filter.test(null)).isFalse();
        assertThat(RegionFilter.of(List.of("MX"), List.of(), List.of()).test(travis)).isFalse();
    }

    @Test
    @DisplayName("a geofence preset renders one union subquery and round-trips its stored form")
    void geofenceRef() {
        GeofenceRef ref = new GeofenceRef(List.of("US"), List.of(), List.of("48453"));
        assertThat(ref.unionSql(codes -> "?")).isEqualTo("(SELECT ST_Multi(ST_CollectionExtract(ST_Union(u.g), 3))"
                + " FROM (SELECT geom AS g FROM ref_country WHERE iso2 = ANY(?) UNION ALL SELECT geom AS g FROM"
                + " ref_admin2 WHERE fips5 = ANY(?)) u)");
        assertThat(ref.toMap()).isEqualTo(Map.of("country", List.of("US"), "county", List.of("48453")));
        assertThat(GeofenceRef.fromMap(Map.of("country", List.of("US"), "county", List.of("48453")))).isEqualTo(ref);
        assertThat(GeofenceRef.fromMap(Map.of())).isNull();
        assertThat(GeofenceRef.fromMap(null)).isNull();
        assertThat(new GeofenceRef(null, null, null).unionSql(codes -> "?")).isEqualTo("NULL::geometry");
    }
}
