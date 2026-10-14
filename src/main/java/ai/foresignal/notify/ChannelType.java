package ai.foresignal.notify;

import ai.foresignal.api.error.FieldError;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One destination type (prompt 018). Each type is a Spring bean; {@link ChannelTypeRegistry}
 * collects them, so {@code webpush} (031) and {@code mqtt} (060) join by declaring a bean. The
 * {@code mqtt} type renders a {@link PreparedRequest.Mqtt} body that {@code ChannelSender} publishes on the
 * channel's supervised connection.
 *
 * <p>A type turns a {@link NotificationMessage} into a {@link PreparedRequest} in the destination's
 * native format, and never performs I/O itself: {@code ChannelSender} sends what it prepares. Types
 * that do not support attachments ignore {@link NotificationMessage#attachments()} without error; the
 * five image types of prompt 055 ({@code discord}, {@code telegram}, {@code ntfy}, {@code pushover},
 * {@code email}) send {@link NotificationMessage#image()} natively.
 */
public interface ChannelType {

    /** The type id ({@code webhook}, {@code slack}, …). */
    String id();

    /** The catalog entry: config and secret fields, served by {@code GET /v1/channels/types}. */
    ChannelTypeSpec spec();

    /** What the type can do. */
    ChannelCapabilities capabilities();

    /**
     * Type-specific checks beyond the generic field checks the channel service already applied
     * (required fields present, field types, enum values).
     *
     * @return the problems, empty when valid
     */
    default List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        return List.of();
    }

    /**
     * A more specific code for the 400 when {@link #validate} found problems (prompt 084: the mesh types answer
     * {@code MESH_PUBLIC_CHANNEL} when a problem is a public-channel rule), or null for {@code VALIDATION_FAILED}.
     *
     * @param config the channel's settings
     * @param secrets the channel's secrets
     */
    default String validationCode(Map<String, Object> config, Map<String, String> secrets) {
        return null;
    }

    /**
     * True when an alert lifecycle update of {@code subtype} ({@code alert.resolved}, {@code alert.acknowledged},
     * {@code alert.snoozed}) is delivered to channels of this type that received the original (prompt 084: the
     * mesh types take only {@code alert.resolved}, so acknowledgements and snoozes never cost airtime).
     */
    default boolean acceptsUpdate(String subtype) {
        return true;
    }

    /**
     * True when a successful response still means the recipient never confirmed the message (prompt 084: a
     * {@code meshcore} direct message with {@code requireAck} whose acknowledgement did not arrive after one
     * retry); the worker then settles the delivery {@code unacked} instead of {@code delivered}.
     */
    default boolean unacknowledged(ChannelResponse response) {
        return false;
    }

    /**
     * Non-fatal warnings about a saved channel, returned in its {@code warnings} (prompt 060:
     * {@code MQTT_INSECURE_PUBLIC_BROKER}).
     *
     * @param config the channel's settings
     * @param secretFields the names of the secrets that are set
     */
    default List<ChannelWarning> warnings(Map<String, Object> config, List<String> secretFields) {
        return List.of();
    }

    /** The request that sends {@code message} as a new message. */
    PreparedRequest render(NotificationMessage message, ChannelSettings settings);

    /**
     * The request that edits the original message into {@code message}, or empty when the type
     * cannot edit; the worker then sends {@code message} as a follow-up with {@link #render}.
     *
     * @param providerMessageRef what {@link #extractRef} returned for the original
     */
    default Optional<PreparedRequest> renderUpdate(
            NotificationMessage message, String providerMessageRef, ChannelSettings settings) {
        return Optional.empty();
    }

    /**
     * Requests sent before a lifecycle update or follow-up (Pushover cancels the emergency receipt
     * of the original). Failures are logged and do not stop the update.
     *
     * @param providerMessageRef the original's reference, or null
     */
    default List<PreparedRequest> beforeUpdate(
            NotificationMessage message, String providerMessageRef, ChannelSettings settings) {
        return List.of();
    }

    /** The destination's reference to the sent message (message id, event id, receipt), if any. */
    default Optional<String> extractRef(ChannelResponse response) {
        return Optional.empty();
    }

    /**
     * True when a non-2xx response means "delivered with a warning" and must never be retried
     * (Apprise answers 424 when some targets failed and others succeeded).
     */
    default boolean partialSuccess(ChannelResponse response) {
        return false;
    }

    /**
     * True when a failed response means the destination refused the image attachment rather than the
     * message (prompt 055: ntfy answers 413, or 400 code 40014 when attachments are disabled). The worker
     * then sends the message again without the image, as text with a link, and records {@code text-only}.
     */
    default boolean attachmentRefused(ChannelResponse response) {
        return false;
    }

    /**
     * Requests sent after a successful send of {@code message} (prompt 055: Telegram puts the text that does
     * not fit a photo caption in a follow-up {@code sendMessage}). Failures are logged and do not fail the
     * delivery.
     */
    default List<PreparedRequest> followUps(NotificationMessage message, ChannelSettings settings) {
        return List.of();
    }

    /** The per-channel rate limit. */
    ChannelRateLimit rateLimit();
}
