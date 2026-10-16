package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBWriter;

/** The WKB header reader (prompt 111): byte order, ISO and EWKB dimensions, GeoParquet names. */
class WkbTest {

    private static byte[] header(ByteOrder order, int type) {
        ByteBuffer buffer = ByteBuffer.allocate(5 + 16).order(order);
        buffer.put((byte) (order == ByteOrder.LITTLE_ENDIAN ? 1 : 0));
        buffer.putInt(type);
        return buffer.array();
    }

    @Test
    @DisplayName("the type is read as a uint32 in the declared byte order, not from the flag byte")
    void byteOrder() {
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 3))).isEqualTo("Polygon");
        assertThat(Wkb.geoParquetType(header(ByteOrder.BIG_ENDIAN, 6))).isEqualTo("MultiPolygon");
        byte[] xdr = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN).write(ExportRowsFixture.line(1, 2));
        assertThat(Wkb.geoParquetType(xdr)).isEqualTo("LineString");
    }

    @Test
    @DisplayName("ISO Z/M/ZM and EWKB flags map to the GeoParquet suffixes")
    void dimensions() {
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 1001))).isEqualTo("Point Z");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 2002))).isEqualTo("LineString M");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 3007))).isEqualTo("GeometryCollection ZM");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 0x80000001))).isEqualTo("Point Z");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 0x40000004))).isEqualTo("MultiPoint M");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 0xE0000005))).isEqualTo("MultiLineString ZM");
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 0x20000001))).as("an SRID flag only")
                .isEqualTo("Point");
        byte[] z = new WKBWriter(3).write(ExportRowsFixture.WGS84.createPoint(new Coordinate(1, 2, 3)));
        assertThat(Wkb.header(z).hasZ()).isTrue();
    }

    @Test
    @DisplayName("unknown types and malformed bytes yield null names")
    void malformed() {
        assertThat(Wkb.geoParquetType(null)).isNull();
        assertThat(Wkb.geoParquetType(new byte[] {1, 0})).isNull();
        assertThat(Wkb.geoParquetType(new byte[] {7, 1, 0, 0, 0})).isNull();
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 15))).isNull();
        assertThat(Wkb.geoParquetType(header(ByteOrder.LITTLE_ENDIAN, 4001))).isNull();
        assertThatThrownBy(() -> Wkb.parse(new byte[] {1, 9, 9, 9, 9})).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("parse returns an SRID 4326 geometry")
    void parse() {
        byte[] wkb = ExportRowsFixture.wkb(ExportRowsFixture.point(3, 4));
        assertThat(Wkb.parse(wkb).getSRID()).isEqualTo(4326);
        assertThat(Wkb.parse(wkb).getCoordinate()).isEqualTo(new Coordinate(3, 4));
    }
}
