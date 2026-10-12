package ai.foresignal.notify.push;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.notify.ChannelService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Push setup at startup (prompt 031, {@link StartupOrder#PUSH}): the VAPID key pair is generated on
 * first start, and the built-in channel "Push" (type {@code webpush}, events alert, briefing and
 * system, enabled) is created when it is missing. An existing Push channel is left as the operator
 * configured it (disabled, filtered, quiet hours). {@code foresignal.push.builtin-channel} turns the
 * channel off (the test profile does, so other notification tests see only their own channels).
 */
@Component
public class PushBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PushBootstrap.class);

    private final VapidKeyService vapid;
    private final ChannelService channels;
    private final PushProperties properties;

    /** Creates the bootstrap. */
    public PushBootstrap(VapidKeyService vapid, ChannelService channels, PushProperties properties) {
        this.vapid = vapid;
        this.channels = channels;
        this.properties = properties;
    }

    /** Generates the VAPID keys and ensures the Push channel. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.PUSH)
    public void start() {
        try {
            vapid.keys();
        } catch (RuntimeException e) {
            log.error("Could not load or create the VAPID key pair; Web Push will retry on first use", e);
        }
        if (properties.builtinChannel()) {
            ensureChannel();
        }
    }

    /**
     * Creates the built-in Push channel when it is missing.
     *
     * @return true when it was created
     */
    public boolean ensureChannel() {
        try {
            boolean created = channels.ensureBuiltIn(PushChannelType.BUILT_IN_ID, PushChannelType.BUILT_IN_NAME,
                    PushChannelType.ID, List.of("alert", "briefing", "system"));
            if (created) {
                log.info("Created the built-in Push channel {}", PushChannelType.BUILT_IN_ID);
            }
            return created;
        } catch (RuntimeException e) {
            log.error("Could not create the built-in Push channel", e);
            return false;
        }
    }
}
