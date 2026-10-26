package ai.foresignal.security.user;

import java.sql.Timestamp;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puts back any part of the permission catalog or the five built-in roles that is missing (prompt R2-006), on every
 * start, so a role cannot be lost: a permission row, a built-in role row, or one of a built-in role's permission
 * links. It inserts with {@code on conflict do nothing}, exactly as {@code V14__identity_model.sql} seeded them, and
 * never deletes or changes a row that exists. After a repair it publishes {@link RolePermissionsChangedEvent} so no
 * cached permission set outlives it.
 */
@Component
public class BuiltInRoleSeeder {

    private static final Logger LOG = LoggerFactory.getLogger(BuiltInRoleSeeder.class);

    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    /**
     * Creates the seeder.
     *
     * @param jdbc JDBC
     * @param events the event publisher
     * @param clock the clock, for a re-created role's timestamps
     */
    public BuiltInRoleSeeder(JdbcTemplate jdbc, ApplicationEventPublisher events, Clock clock) {
        this.jdbc = jdbc;
        this.events = events;
        this.clock = clock;
    }

    /** Runs the repair once the application is up. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        seed();
    }

    /**
     * Inserts whatever is missing.
     *
     * @return how many rows were inserted
     */
    @Transactional
    public int seed() {
        int inserted = 0;
        for (Permissions permission : Permissions.values()) {
            inserted += jdbc.update("insert into permission (key, \"group\", description) values (?, ?, ?)"
                    + " on conflict do nothing", permission.key(), permission.group().wireValue(),
                    permission.description());
        }
        Timestamp now = Timestamp.from(clock.instant());
        for (BuiltInRole role : BuiltInRole.values()) {
            int roleRows = jdbc.update("insert into role (id, name, description, built_in, created_at, updated_at)"
                    + " values (?, ?, ?, true, ?, ?) on conflict do nothing", role.id(), role.roleName(),
                    role.description(), now, now);
            if (roleRows > 0) {
                LOG.warn("The built-in role {} was missing and has been re-created", role.roleName());
            }
            inserted += roleRows;
            for (Permissions permission : role.permissions()) {
                inserted += jdbc.update("insert into role_permission (role_id, permission_key) values (?, ?)"
                        + " on conflict do nothing", role.id(), permission.key());
            }
        }
        if (inserted > 0) {
            LOG.warn("Repaired the permission catalog and built-in roles: {} rows re-inserted", inserted);
            events.publishEvent(new RolePermissionsChangedEvent(null));
        }
        return inserted;
    }
}
