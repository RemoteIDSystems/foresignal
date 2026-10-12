package ai.foresignal.notify.push;

import ai.foresignal.notify.ChannelCapabilities;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.QuietHoursMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * The {@code webpush} channel type (prompt 031): one built-in channel named "Push" that fans out to
 * every verified push target (Web Push and UnifiedPush subscriptions here, native devices in 032).
 *
 * <p>It renders nothing itself ({@link ChannelCapabilities#fanOut()}): the delivery worker hands its
 * deliveries to {@link PushFanout}. The channel's filters (events, rules, minimum severity), quiet
 * hours and pause apply before the fan-out; each target's own {@link PushFilters} apply in it.
 * {@code config.quietHoursMode} chooses what quiet hours do ({@link QuietHoursMode}). The per-channel
 * rate limit is generous because push services are rate-limited per host instead
 * ({@link PushHostLimiter}).
 */
@Component
public class PushChannelType implements ChannelType {

    /** The type id. */
    public static final String ID = "webpush";

    /** The fixed id of the built-in channel, so settings exports and imports line up across installs. */
    public static final UUID BUILT_IN_ID = UUID.nameUUIDFromBytes("foresignal:channel:webpush".getBytes(
            java.nio.charset.StandardCharsets.UTF_8));

    /** The built-in channel's name. */
    public static final String BUILT_IN_NAME = "Push";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Push",
            "Built-in channel: Web Push to browsers and UnifiedPush apps (subscriptions under /v1/push)",
            List.of(ChannelFieldSpec.choice(QuietHoursMode.KEY, false,
                    "What quiet hours do: defer until the window ends, downgrade (Urgency very-low, silent) or drop",
                    QuietHoursMode.DEFER, List.of(QuietHoursMode.DEFER, QuietHoursMode.DOWNGRADE, QuietHoursMode.DROP))),
            List.of(), null);

    private static final ChannelCapabilities CAPABILITIES = new ChannelCapabilities(false, false, true);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    @Override
    public ChannelCapabilities capabilities() {
        return CAPABILITIES;
    }

    /**
     * Never called by the delivery worker (it fans out instead).
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public PreparedRequest render(NotificationMessage message, ChannelSettings settings) {
        throw new UnsupportedOperationException("the webpush channel fans out to push targets");
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perSecond(1_000);
    }
}
