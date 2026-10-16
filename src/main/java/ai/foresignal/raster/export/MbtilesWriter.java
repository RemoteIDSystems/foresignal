package ai.foresignal.raster.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * A minimal MBTiles 1.3 writer (prompt 108; Weather-Imagery-Design §11), schema 1.3 via SQLite JDBC:
 * a {@code metadata} table (attribution, bounds, format, type {@code overlay}, {@code json} with the
 * time) and a {@code tiles} table keyed by TMS rows. One file per frame.
 */
public final class MbtilesWriter {

    private MbtilesWriter() {}

    /** One tile. */
    public record Tile(int z, int x, int y, byte[] data) {}

    /**
     * Writes a single-file MBTiles archive.
     *
     * @param file the target file
     * @param tiles the tiles
     * @param metadata name/value metadata (name, bounds, minzoom, maxzoom, json, attribution, ...)
     * @param format {@code png} or {@code jpg}
     */
    public static void write(Path file, List<Tile> tiles, Map<String, String> metadata, String format) {
        String url = "jdbc:sqlite:" + file.toAbsolutePath();
        try (Connection connection = DriverManager.getConnection(url)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=MEMORY");
                statement.execute("CREATE TABLE metadata (name text, value text)");
                statement.execute("CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer,"
                        + " tile_data blob)");
                statement.execute("CREATE UNIQUE INDEX tile_index ON tiles (zoom_level, tile_column, tile_row)");
            }
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO metadata VALUES (?, ?)")) {
                put(insert, "format", format);
                put(insert, "type", "overlay");
                for (Map.Entry<String, String> e : metadata.entrySet()) {
                    if (e.getValue() != null) {
                        put(insert, e.getKey(), e.getValue());
                    }
                }
                insert.executeBatch();
            }
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT OR REPLACE INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)")) {
                int batch = 0;
                for (Tile tile : tiles) {
                    insert.setInt(1, tile.z());
                    insert.setInt(2, tile.x());
                    insert.setInt(3, (1 << tile.z()) - 1 - tile.y());
                    insert.setBytes(4, tile.data());
                    insert.addBatch();
                    if (++batch % 500 == 0) {
                        insert.executeBatch();
                    }
                }
                insert.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new UncheckedIOException(new IOException("Could not write MBTiles " + file + ": " + e.getMessage(), e));
        }
    }

    private static void put(PreparedStatement insert, String name, String value) throws SQLException {
        insert.setString(1, name);
        insert.setString(2, value);
        insert.addBatch();
    }
}
