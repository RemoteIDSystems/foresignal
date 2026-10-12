package ai.foresignal.notify.inbox;

import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushBadgeProvider;
import ai.foresignal.notify.push.PushTarget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The badge of a native push device (prompt 033): the unread count of the device key of the token
 * that registered it ({@code push_device.token_id}, the 032 {@code ClientDeviceResolver} identity),
 * sent as {@code aps.badge}. Web Push targets have no badge.
 */
@Component
public class InboxBadgeProvider implements PushBadgeProvider {

    private final NamedParameterJdbcTemplate jdbc;
    private final InboxService inbox;

    /** Creates the provider. */
    public InboxBadgeProvider(NamedParameterJdbcTemplate jdbc, InboxService inbox) {
        this.jdbc = jdbc;
        this.inbox = inbox;
    }

    @Override
    public Optional<Integer> badgeFor(PushTarget target) {
        if (!(target instanceof NativeDeviceTarget device)) {
            return Optional.empty();
        }
        List<UUID> tokens = jdbc.query("select token_id from push_device where id = :id",
                new MapSqlParameterSource("id", device.deviceId()), (rs, n) -> rs.getObject(1, UUID.class));
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(inbox.badge(tokens.getFirst().toString()).unread());
    }
}
