package ai.foresignal.notify.mesh;

import ai.foresignal.source.spi.LanEndpoint;

/**
 * Reads one channel slot of a MeshCore companion (prompt 084) with {@code CMD_GET_CHANNEL} (0x1F), so the
 * {@code meshcore} channel type can refuse a public slot at create and update. Implemented by
 * {@link MeshCoreSender}, which shares 083's companion link.
 */
public interface MeshCoreSlotProbe {

    /** What a slot check found. */
    enum Status {
        /** A named slot with a non-zero secret that is neither {@code Public} nor a {@code #hashtag} channel. */
        PRIVATE,
        /** Slot 0, {@code Public}, a {@code #hashtag} name, an empty slot or an all-zero secret. */
        PUBLIC,
        /** The companion did not answer in time; the slot is checked again on every link connect. */
        UNREACHABLE
    }

    /**
     * The outcome of a check.
     *
     * @param status what was found
     * @param reason why, for the field error or the log
     */
    record SlotCheck(Status status, String reason) {}

    /** Checks slot {@code index} of the companion at {@code endpoint}. */
    SlotCheck check(LanEndpoint endpoint, int index);
}
