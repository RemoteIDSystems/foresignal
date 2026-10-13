package ai.foresignal.notify;

/**
 * What a channel type can do (OpenAPI {@code ChannelType.capabilities}).
 *
 * @param editInPlace lifecycle updates can edit the original message ({@link ChannelType#renderUpdate})
 * @param attachments image attachments are sent (prompt 055)
 * @param fanOut one delivery fans out to many endpoints ({@code webpush} 031; {@code mqtt} 060 is not a
 *     fan-out type: each delivery is one publish through the ordinary pipeline)
 */
public record ChannelCapabilities(boolean editInPlace, boolean attachments, boolean fanOut) {

    /** No edit-in-place, no attachments, no fan-out. */
    public static final ChannelCapabilities BASIC = new ChannelCapabilities(false, false, false);
}
