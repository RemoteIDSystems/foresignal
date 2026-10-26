package ai.foresignal.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.security.PrincipalEntity;
import ai.foresignal.security.PrincipalKind;
import ai.foresignal.security.PrincipalRepository;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.ServerSettingsService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link UserZones} (prompt R2-012): the user's own zone, else the server's, else UTC. */
class UserZonesTest {

    private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

    private final PrincipalRepository principals = mock(PrincipalRepository.class);
    private final ServerSettingsService server = mock(ServerSettingsService.class);
    private final UserZones zones = new UserZones(principals, server);

    private void principal(String id, String timezone) {
        PrincipalEntity p = new PrincipalEntity(id, id, id, PrincipalKind.PERSON, NOW);
        p.setTimezone(timezone, NOW);
        when(principals.findById(id)).thenReturn(Optional.of(p));
    }

    private void serverZone(String timezone) {
        when(server.get()).thenReturn(new ServerSettings(null, timezone, null, null, null));
    }

    @Test
    @DisplayName("a user's own timezone wins over the server's")
    void ownZone() {
        serverZone("Europe/Berlin");
        principal("p-1", "America/Denver");
        assertThat(zones.zoneFor("p-1")).isEqualTo(ZoneId.of("America/Denver"));
    }

    @Test
    @DisplayName("no own zone, an unknown principal, a broken zone or the machine read the server's")
    void fallsBackToServer() {
        serverZone("Europe/Berlin");
        principal("p-2", null);
        principal("p-3", "Not/AZone");
        assertThat(zones.zoneFor("p-2")).isEqualTo(ZoneId.of("Europe/Berlin"));
        assertThat(zones.zoneFor("p-3")).isEqualTo(ZoneId.of("Europe/Berlin"));
        assertThat(zones.zoneFor("nobody")).isEqualTo(ZoneId.of("Europe/Berlin"));
        assertThat(zones.zoneFor(null)).isEqualTo(ZoneId.of("Europe/Berlin"));
    }

    @Test
    @DisplayName("with no usable server zone it is UTC")
    void utc() {
        serverZone(null);
        assertThat(zones.zoneFor(null).normalized()).isEqualTo(ZoneOffset.UTC);
        when(server.get()).thenThrow(new IllegalStateException("settings down"));
        assertThat(zones.serverZone().normalized()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("the zone changes the rendering of an instant, never the instant")
    void renderingOnly() {
        principal("p-4", "Asia/Kolkata");
        principal("p-5", "America/New_York");
        assertThat(NOW.atZone(zones.zoneFor("p-4")).toLocalTime().toString()).isEqualTo("17:30");
        assertThat(NOW.atZone(zones.zoneFor("p-5")).toLocalTime().toString()).isEqualTo("08:00");
        assertThat(NOW.atZone(zones.zoneFor("p-4")).toInstant()).isEqualTo(NOW.atZone(zones.zoneFor("p-5"))
                .toInstant());
    }
}
