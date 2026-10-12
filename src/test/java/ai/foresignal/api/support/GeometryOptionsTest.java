package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.Test;

class GeometryOptionsTest {

    @Test
    void defaults() {
        GeometryOptions options = GeometryOptions.parse(null, null);
        assertThat(options).isEqualTo(GeometryOptions.DEFAULT);
        assertThat(options.simplifies()).isFalse();
        assertThat(options.simplified("l.geom", ":t")).isEqualTo("l.geom");
    }

    @Test
    void parsesRanges() {
        assertThat(GeometryOptions.parse("0", "100000").precision()).isZero();
        assertThat(GeometryOptions.parse("3", "0").simplifies()).isFalse();
        assertThatThrownBy(() -> GeometryOptions.parse("8", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> GeometryOptions.parse("-1", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> GeometryOptions.parse(null, "100001")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new GeometryOptions(9, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeometryOptions(1, -1.0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void simplificationLeavesPointsAlone() {
        String sql = GeometryOptions.parse("5", "250").simplified("l.geom", ":t");
        assertThat(sql)
                .contains("GeometryType(l.geom) IN ('POINT', 'MULTIPOINT') THEN l.geom")
                .contains("ST_SimplifyPreserveTopology(ST_Transform(l.geom, 3857), :t / ")
                .contains("cos(radians(ST_Y(ST_Centroid(l.geom))))")
                .contains(", 4326)");
        assertThat(GeometryOptions.parse("5", "250").canonical()).isEqualTo("5/250.0");
    }
}
