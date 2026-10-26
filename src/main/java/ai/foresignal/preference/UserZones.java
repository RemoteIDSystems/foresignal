package ai.foresignal.preference;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.ServerSettingsService;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * The timezone a person's timestamps are rendered in (prompt R2-012): their own ({@code principal.timezone}, set
 * through {@code updateMyPreferences} or {@code patchPrincipal}), else the {@code server} setting's, else UTC.
 * Stored times stay UTC everywhere; only rendering consults this.
 */
@Component
public class UserZones {

    private final PrincipalRepository principals;
    private final ServerSettingsService server;

    /**
     * Creates the component.
     *
     * @param principals the principal store, which holds each person's timezone
     * @param server the server settings, whose timezone is the fallback
     */
    public UserZones(PrincipalRepository principals, ServerSettingsService server) {
        this.principals = principals;
        this.server = server;
    }

    /**
     * The zone {@code principalId} reads timestamps in.
     *
     * @param principalId the person's principal, or null for the machine (the server zone)
     * @return the zone, never null
     */
    public ZoneId zoneFor(String principalId) {
        String own = principalId == null ? null
                : principals.findById(principalId).map(PrincipalEntity::getTimezone).orElse(null);
        ZoneId zone = parse(own);
        return zone != null ? zone : serverZone();
    }

    /**
     * The server's zone: the {@code server} setting's timezone, else UTC.
     *
     * @return the zone, never null
     */
    public ZoneId serverZone() {
        ServerSettings settings;
        try {
            settings = server.get();
        } catch (RuntimeException e) {
            settings = null;
        }
        ZoneId zone = parse(settings == null || settings.timezone() == null ? ServerSettings.DEFAULT_TIMEZONE
                : settings.timezone());
        return zone != null ? zone : ZoneOffset.UTC;
    }

    private static ZoneId parse(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(id);
        } catch (DateTimeException e) {
            return null;
        }
    }
}
