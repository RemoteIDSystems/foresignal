package ai.foresignal.isolation;

import com.zaxxer.hikari.HikariDataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * The application's {@code DataSource}: a separate Hikari pool per {@link WorkRole}, chosen by the role of the
 * thread asking for a connection.
 *
 * <p>The {@link WorkRole#BACKGROUND} pool is the one Spring Boot built from {@code spring.datasource.*} (so its
 * URL, credentials, driver properties and test {@code @ServiceConnection} apply unchanged); every other pool is a
 * copy of its configuration with its own name and size ({@link WorkPoolProperties}). A pool opens its first
 * connection when a thread in its role first asks for one, so a role that never runs (collection in the test
 * profile) costs nothing.
 *
 * <p>Because every connection is taken through this one {@code DataSource}, JDBC templates, JPA and Spring's
 * transactions all route by role with no change of their own: a transaction holds a connection from the pool of
 * the thread that began it.
 */
public class RoleRoutingDataSource extends AbstractDataSource implements Closeable {

    private final HikariDataSource background;
    private final Map<WorkRole, HikariDataSource> pools = new EnumMap<>(WorkRole.class);

    /**
     * Builds the pools beside {@code background}.
     *
     * @param background the application's own pool, used for {@link WorkRole#BACKGROUND}; not yet started
     * @param sizes the sizes of the other pools
     */
    public RoleRoutingDataSource(HikariDataSource background, WorkPoolProperties sizes) {
        this.background = Objects.requireNonNull(background, "background");
        Objects.requireNonNull(sizes, "sizes");
        String base = background.getPoolName() == null ? "foresignal" : background.getPoolName();
        pools.put(WorkRole.BACKGROUND, background);
        for (WorkRole role : WorkRole.values()) {
            if (role == WorkRole.BACKGROUND) {
                continue;
            }
            HikariDataSource pool = new HikariDataSource();
            // Every non-final setting (URL, credentials, timeouts, driver properties); the copy is left unsealed.
            background.copyStateTo(pool);
            int size = sizes.sizeOf(role);
            pool.setPoolName(base + "-" + role.name().toLowerCase(Locale.ROOT));
            pool.setMaximumPoolSize(size);
            pool.setMinimumIdle(Math.min(sizes.minimumIdle(), size));
            pools.put(role, pool);
        }
    }

    /** The pool that serves {@code role}. */
    public HikariDataSource poolFor(WorkRole role) {
        return pools.get(Objects.requireNonNull(role, "role"));
    }

    /** A connection from the current thread's role's pool. */
    @Override
    public Connection getConnection() throws SQLException {
        return poolFor(WorkRole.current()).getConnection();
    }

    /** A connection from the current thread's role's pool, as another user. */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return poolFor(WorkRole.current()).getConnection(username, password);
    }

    /** Unwraps to this, or to the background pool (for pool metadata and metrics). */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        return background.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || background.isWrapperFor(iface);
    }

    /** Closes every pool. */
    @Override
    public void close() {
        for (HikariDataSource pool : pools.values()) {
            pool.close();
        }
    }
}
