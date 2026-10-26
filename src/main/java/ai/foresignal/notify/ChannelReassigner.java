package ai.foresignal.notify;

import ai.foresignal.security.user.UserEntity;
import ai.foresignal.security.user.UserOwnedConfigReassigner;
import ai.foresignal.security.user.UserRepository;
import ai.foresignal.store.NotificationChannelEntity;
import ai.foresignal.store.NotificationChannelRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Moves or deletes a deleted user's notification channels (prompt R2-009, the {@link UserOwnedConfigReassigner} of
 * R2-005), so the alerts R2-008 reassigned with the rules keep a channel to go to.
 *
 * <p>Reassigning moves each channel to the target admin's principal. With no target (no admin account exists) a
 * channel would become global, and a global channel without {@code ruleIds} takes every shared alert, so it is
 * <em>disabled</em> as it becomes global; an operator re-enables it deliberately. Deleting removes the channels the way
 * {@code deleteChannel} does (pending deliveries fail with "channel deleted").
 */
@Component
public class ChannelReassigner implements UserOwnedConfigReassigner {

    private final UserRepository users;
    private final NotificationChannelRepository channels;
    private final ChannelService service;
    private final Clock clock;

    /**
     * Creates the reassigner.
     *
     * @param users the user store, mapping a user to their principal
     * @param channels the channel store
     * @param service the channel service, for deletion
     * @param clock the clock
     */
    public ChannelReassigner(UserRepository users, NotificationChannelRepository channels, ChannelService service,
            Clock clock) {
        this.users = users;
        this.channels = channels;
        this.service = service;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "channels";
    }

    @Override
    public int reassign(UUID owner, UUID target) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        String to = target == null ? null : principal(target);
        List<NotificationChannelEntity> owned = channels.findByOwnerPrincipalId(from);
        for (NotificationChannelEntity channel : owned) {
            channel.setOwnerPrincipalId(to);
            if (to == null) {
                channel.setEnabled(false);
            }
            channel.setUpdatedAt(clock.instant());
        }
        channels.saveAllAndFlush(owned);
        return owned.size();
    }

    @Override
    public int delete(UUID owner) {
        String from = principal(owner);
        if (from == null) {
            return 0;
        }
        List<NotificationChannelEntity> owned = channels.findByOwnerPrincipalId(from);
        owned.forEach(channel -> service.delete(channel.getId()));
        return owned.size();
    }

    private String principal(UUID user) {
        return users.findById(user).map(UserEntity::getPrincipalId).orElse(null);
    }
}
