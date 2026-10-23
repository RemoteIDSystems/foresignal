package ai.foresignal.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The geography guard of migration V7: {@code fs_geog}, {@code fs_dwithin} and {@code fs_distance} accept
 * shapes {@code geom::geography} refuses, and leave the shapes it accepts as they were.
 */
@IntegrationTest
class GeographyGuardIT {

    /** The four-corner world box raster frames were stored with: its pole-to-pole edges are antipodal. */
    static final String WORLD = "ST_GeomFromText('POLYGON((-180 -90,180 -90,180 90,-180 90,-180 -90))', 4326)";

    /** A cable split at the antimeridian, wider than 180 degrees, whose long edges cast cleanly. */
    static final String CABLE = "ST_GeomFromText('MULTILINESTRING((-118 34,-160 21,-180 10),(180 10,150 -30))', 4326)";

    static final String HOUSTON = "ST_SetSRID(ST_MakePoint(-95.37, 29.76), 4326)";

    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("a world box casts through the guard and is within any distance of every point")
    void worldBox() {
        assertThatThrownBy(() -> jdbc.queryForObject("SELECT ST_AsText(" + WORLD + "::geography)", String.class))
                .hasMessageContaining("Antipodal");
        assertThat(jdbc.queryForObject("SELECT fs_geog(" + WORLD + ") IS NOT NULL", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT fs_dwithin(" + WORLD + ", " + HOUSTON + ", 1)", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("SELECT fs_distance(" + WORLD + ", " + HOUSTON + ")", Double.class))
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("a wide shape that casts cleanly keeps its great-circle distance")
    void wideShapeUnchanged() {
        Double plain = jdbc.queryForObject("SELECT ST_Distance(" + CABLE + "::geography, " + HOUSTON + "::geography)",
                Double.class);
        Double guarded = jdbc.queryForObject("SELECT fs_distance(" + CABLE + ", " + HOUSTON + ")", Double.class);
        assertThat(guarded).isEqualTo(plain);
        assertThat(jdbc.queryForObject("SELECT fs_dwithin(" + CABLE + ", " + HOUSTON + ", ?)", Boolean.class,
                plain + 1)).isTrue();
        assertThat(jdbc.queryForObject("SELECT fs_dwithin(" + CABLE + ", " + HOUSTON + ", ?)", Boolean.class,
                plain - 1)).isFalse();
    }

    @Test
    @DisplayName("a null geometry gives null, as the plain cast does")
    void nullGeometry() {
        assertThat(jdbc.queryForObject("SELECT fs_geog(NULL::geometry) IS NULL", Boolean.class)).isTrue();
    }
}
