package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/** {@link Antimeridian}: splitting fences at ±180° (prompt 017). */
class AntimeridianTest {

    static Polygon ring(double... xy) {
        Coordinate[] coordinates = new Coordinate[xy.length / 2];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = new Coordinate(xy[2 * i], xy[2 * i + 1]);
        }
        return Antimeridian.FACTORY.createPolygon(coordinates);
    }

    @Test
    @DisplayName("a polygon inside -180..180 is kept as one part")
    void plain() {
        MultiPolygon split = Antimeridian.split(ring(0, 0, 10, 0, 10, 10, 0, 10, 0, 0));
        assertThat(split.getNumGeometries()).isEqualTo(1);
        assertThat(split.getSRID()).isEqualTo(4326);
        assertThat(split.getArea()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("a ring that jumps across the antimeridian is cut into an east and a west part")
    void jump() {
        MultiPolygon split = Antimeridian.split(ring(170, -10, -170, -10, -170, 10, 170, 10, 170, -10));
        assertThat(split.getNumGeometries()).isEqualTo(2);
        Envelope env = split.getEnvelopeInternal();
        assertThat(env.getMinX()).isEqualTo(-180.0);
        assertThat(env.getMaxX()).isEqualTo(180.0);
        assertThat(split.getArea()).isEqualTo(400.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(split.covers(Antimeridian.FACTORY.createPoint(new Coordinate(179, 0)))).isTrue();
        assertThat(split.covers(Antimeridian.FACTORY.createPoint(new Coordinate(-179, 0)))).isTrue();
        assertThat(split.covers(Antimeridian.FACTORY.createPoint(new Coordinate(0, 0)))).isFalse();
    }

    @Test
    @DisplayName("a part beyond 180 is shifted back into range")
    void beyond() {
        MultiPolygon split = Antimeridian.split(ring(175, 0, 185, 0, 185, 5, 175, 5, 175, 0));
        assertThat(split.getNumGeometries()).isEqualTo(2);
        assertThat(split.getEnvelopeInternal().getMaxX()).isEqualTo(180.0);
        assertThat(split.covers(Antimeridian.FACTORY.createPoint(new Coordinate(-177, 2)))).isTrue();
    }

    @Test
    @DisplayName("an empty input gives an empty MultiPolygon")
    void empty() {
        assertThat(Antimeridian.split(Antimeridian.FACTORY.createPolygon()).isEmpty()).isTrue();
        assertThat(Antimeridian.toMulti(Antimeridian.FACTORY.createPoint(new Coordinate(0, 0))).isEmpty()).isTrue();
    }
}
