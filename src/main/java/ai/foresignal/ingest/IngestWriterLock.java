package ai.foresignal.ingest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The single-writer advisory lock (Core-Data-Model §6.1, invariant I1).
 *
 * <p>Held on a dedicated connection opened with {@link DriverManager}, outside the Hikari pool, so
 * that pool eviction can never release it and no pooled connection is pinned for the life of the
 * process. A session-level {@code pg_try_advisory_lock(hashtext('foresignal.ingest.writer'))} is
 * released by PostgreSQL when the session ends, so a crashed instance never leaves it held.
 *
 * <p>Not thread-safe; only the writer (at start, then on its own thread) uses it.
 */
public class IngestWriterLock implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(IngestWriterLock.class);

    /** The lock key's text; hashed by PostgreSQL. */
    public static final String LOCK_NAME = "foresignal.ingest.writer";

    /** The startup failure when another instance holds the lock. */
    public static final String HELD_MESSAGE = "another Foresignal instance holds the ingest writer lock on this database";

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    /**
     * A lock that connects with these details.
     *
     * @param url the JDBC URL
     * @param username the user, or null
     * @param password the password, or null
     */
    public IngestWriterLock(String url, String username, String password) {
        this.url = Objects.requireNonNull(url, "url");
        this.username = username;
        this.password = password;
    }

    /**
     * Opens the dedicated connection and tries to take the lock.
     *
     * @return true when this lock now holds it; false when another session does
     * @throws SQLException when the database cannot be reached
     */
    public boolean tryAcquire() throws SQLException {
        closeQuietly();
        Properties properties = new Properties();
        if (username != null) {
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        properties.setProperty("ApplicationName", "foresignal-ingest-writer-lock");
        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(true);
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT pg_try_advisory_lock(hashtext('" + LOCK_NAME + "'))")) {
            boolean acquired = rs.next() && rs.getBoolean(1);
            if (!acquired) {
                closeQuietly();
            }
            return acquired;
        }
    }

    /**
     * Checks that the connection holding the lock is alive with {@code SELECT 1}.
     *
     * @return false when the connection is gone, which means the lock may be lost
     */
    public boolean keepalive() {
        if (connection == null) {
            return false;
        }
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(10);
            statement.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            log.warn("Ingest writer lock connection failed its keepalive: {}", e.getMessage());
            closeQuietly();
            return false;
        }
    }

    /** True while the dedicated connection is open. */
    public boolean isHeld() {
        return connection != null;
    }

    /** Releases the lock by closing the dedicated connection. */
    @Override
    public void close() {
        closeQuietly();
    }

    private void closeQuietly() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.debug("Closing the ingest writer lock connection failed", e);
            }
            connection = null;
        }
    }
}
