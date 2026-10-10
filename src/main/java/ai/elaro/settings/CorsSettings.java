package ai.elaro.settings;

import java.util.List;

/**
 * Which browser origins may call Elaro.
 *
 * <p>Reads default to open because the broker is expected to sit on a trusted network and serve
 * any frontend on it; writes default to closed because they need the admin token anyway and
 * should be enabled deliberately.
 *
 * @param readOrigins origins allowed for safe methods
 * @param writeOrigins origins allowed for mutating methods
 */
public record CorsSettings(List<String> readOrigins, List<String> writeOrigins) {

    public static final List<String> DEFAULT_READ_ORIGINS = List.of("*");
    public static final List<String> DEFAULT_WRITE_ORIGINS = List.of();

    public CorsSettings {
        readOrigins = readOrigins == null ? DEFAULT_READ_ORIGINS : List.copyOf(readOrigins);
        writeOrigins = writeOrigins == null ? DEFAULT_WRITE_ORIGINS : List.copyOf(writeOrigins);
    }

    /** The shipped defaults: all origins may read, none may write. */
    public static CorsSettings defaults() {
        return new CorsSettings(DEFAULT_READ_ORIGINS, DEFAULT_WRITE_ORIGINS);
    }
}
