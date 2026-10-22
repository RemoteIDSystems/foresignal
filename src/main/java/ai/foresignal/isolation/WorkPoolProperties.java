package ai.foresignal.isolation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The sizes of the database pools kept apart by {@link WorkRole}, bound from {@code foresignal.db.pools.*}. The
 * {@link WorkRole#BACKGROUND} pool is the application's own ({@code spring.datasource.hikari.*}, ten connections by
 * default); these are the others, each a copy of its settings with its own size.
 *
 * <p>The defaults total 34 connections beside the background pool's ten, inside PostgreSQL's default
 * {@code max_connections} of 100.
 *
 * @param api connections for HTTP request threads
 * @param collect connections shared by every thread that gathers data, however many sources are enabled; a
 *     source waits for one rather than taking the API's
 * @param ingest connections for the ingest writer
 * @param minimumIdle the idle connections each of these pools keeps open (at most its size); it grows to its size
 *     under load
 */
@ConfigurationProperties("foresignal.db.pools")
public record WorkPoolProperties(
        @DefaultValue("10") int api,
        @DefaultValue("20") int collect,
        @DefaultValue("4") int ingest,
        @DefaultValue("2") int minimumIdle) {

    /** Validates the sizes. */
    public WorkPoolProperties {
        requirePositive("api", api);
        requirePositive("collect", collect);
        requirePositive("ingest", ingest);
        if (minimumIdle < 0) {
            throw new IllegalArgumentException("foresignal.db.pools.minimum-idle must not be negative, but was " + minimumIdle);
        }
    }

    /** The production defaults. */
    public static WorkPoolProperties defaults() {
        return new WorkPoolProperties(10, 20, 4, 2);
    }

    /**
     * The size of {@code role}'s pool.
     *
     * @param role a role other than {@link WorkRole#BACKGROUND}, whose pool is the application's own
     * @return its maximum number of connections
     */
    public int sizeOf(WorkRole role) {
        return switch (role) {
            case API -> api;
            case COLLECT -> collect;
            case INGEST -> ingest;
            case BACKGROUND -> throw new IllegalArgumentException("the background pool is spring.datasource.hikari's");
        };
    }

    private static void requirePositive(String name, int value) {
        if (value < 1) {
            throw new IllegalArgumentException("foresignal.db.pools." + name + " must be positive, but was " + value);
        }
    }
}
