package ai.foresignal.test.schema;

import ai.foresignal.test.PostgisContainers;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * A throwaway database inside the shared test container, for schema tests that must start from an empty database
 * (prompt 144): the application's own database is already migrated, and other cached contexts are using it.
 *
 * <p>Created from {@code template1}, so it holds no extension until a migration creates one. The container's user is
 * a superuser, so {@code CREATE EXTENSION} works.
 */
public final class ScratchDatabase {

    private static final Pattern NAME = Pattern.compile("[a-z][a-z0-9_]{0,40}");

    private final String name;
    private final DataSource dataSource;

    private ScratchDatabase(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    /**
     * Drops {@code name} if it exists and creates it empty.
     *
     * @param admin a connection to any database of the container, used for {@code CREATE DATABASE}
     * @param name lower-case letters, digits and underscores
     * @return the new database
     */
    public static ScratchDatabase recreate(JdbcTemplate admin, String name) {
        if (!NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("not a scratch database name: " + name);
        }
        admin.execute("DROP DATABASE IF EXISTS " + name + " WITH (FORCE)");
        admin.execute("CREATE DATABASE " + name + " TEMPLATE template1");
        String url = "jdbc:postgresql://" + PostgisContainers.POSTGIS.getHost() + ":"
                + PostgisContainers.POSTGIS.getMappedPort(5432) + "/" + name;
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                url, PostgisContainers.POSTGIS.getUsername(), PostgisContainers.POSTGIS.getPassword());
        return new ScratchDatabase(name, dataSource);
    }

    /** The database name. */
    public String name() {
        return name;
    }

    /** A non-pooled data source for the database. */
    public DataSource dataSource() {
        return dataSource;
    }

    /** A template over {@link #dataSource()}. */
    public JdbcTemplate jdbc() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Drops the database.
     *
     * @param admin a connection to another database of the container
     */
    public void drop(JdbcTemplate admin) {
        admin.execute("DROP DATABASE IF EXISTS " + name + " WITH (FORCE)");
    }
}
