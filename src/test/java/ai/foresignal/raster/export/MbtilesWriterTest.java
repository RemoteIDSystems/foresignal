package ai.foresignal.raster.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The MBTiles 1.3 writer: metadata and TMS-row tiles (prompt 108). */
class MbtilesWriterTest {

    @Test
    @DisplayName("writes metadata and flips y to the TMS row")
    void write(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("out.mbtiles");
        MbtilesWriter.write(file, List.of(
                new MbtilesWriter.Tile(2, 1, 0, new byte[] {1, 2, 3}),
                new MbtilesWriter.Tile(2, 1, 3, new byte[] {4, 5})),
                Map.of("name", "test", "bounds", "-180,-85,180,85", "minzoom", "2", "maxzoom", "2"), "png");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
                Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT value FROM metadata WHERE name='format'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).isEqualTo("png");
            }
            try (ResultSet rs = statement.executeQuery(
                    "SELECT zoom_level, tile_column, tile_row FROM tiles ORDER BY tile_row")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt("tile_row")).isEqualTo((1 << 2) - 1 - 3); // y=3 -> TMS row 0
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt("tile_row")).isEqualTo((1 << 2) - 1 - 0); // y=0 -> TMS row 3
            }
            try (ResultSet rs = statement.executeQuery("SELECT count(*) FROM tiles")) {
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(2);
            }
        }
    }
}
