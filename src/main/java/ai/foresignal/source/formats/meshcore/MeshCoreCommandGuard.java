package ai.foresignal.source.formats.meshcore;

import ai.foresignal.source.spi.CommandGuard;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The allowlist of every frame Foresignal writes to a MeshCore companion (prompt 083). One guard per companion link.
 *
 * <ul>
 *   <li><strong>Envelope:</strong> a frame must be {@code '<'}, a uint16 little-endian length equal to the rest of the
 *       frame, and a payload of 1 to {@link MeshCoreProtocol#MAX_FRAME_SIZE} bytes.
 *   <li><strong>Read commands, always allowed:</strong> 0x01 app start, 0x16 device query, 0x04 get contacts, 0x0A sync
 *       next message, 0x05 get time, 0x14 battery and storage, 0x1E contact by key, 0x1F get channel.
 *   <li><strong>Send commands, allowed only once registered</strong> ({@link #allowSend}, prompt 084): 0x02 direct
 *       text, 0x03 channel text, 0x3E channel datagram. Nothing else can ever be registered.
 *   <li><strong>Never:</strong> every {@code CMD_SET_*}, key import and export, contact import, export, add, update and
 *       removal, reboot, factory reset, and every request to another node (login, status, telemetry, binary,
 *       anonymous, path discovery, trace), so repeater health stays receive-only. Anything not listed is refused too.
 * </ul>
 */
public final class MeshCoreCommandGuard implements CommandGuard {

    /** The read commands prompt 083 sends. */
    public static final Set<Integer> READ_COMMANDS = Set.of(MeshCoreProtocol.CMD_APP_START,
            MeshCoreProtocol.CMD_DEVICE_QUERY, MeshCoreProtocol.CMD_GET_CONTACTS, MeshCoreProtocol.CMD_SYNC_NEXT_MESSAGE,
            MeshCoreProtocol.CMD_GET_DEVICE_TIME, MeshCoreProtocol.CMD_GET_BATT_AND_STORAGE,
            MeshCoreProtocol.CMD_GET_CONTACT_BY_KEY, MeshCoreProtocol.CMD_GET_CHANNEL);

    /** The send commands prompt 084 may register; nothing else is registrable. */
    public static final Set<Integer> REGISTRABLE_SENDS = Set.of(MeshCoreProtocol.CMD_SEND_TXT_MSG,
            MeshCoreProtocol.CMD_SEND_CHANNEL_TXT_MSG, MeshCoreProtocol.CMD_SEND_CHANNEL_DATA);

    /**
     * Commands that are never allowed, named so a reader can audit the list: set device time (6), self advert (7), set
     * advert name (8), add or update contact (9), set radio parameters (11), set TX power (12), reset path (13), set
     * advert position (14), remove contact (15), share contact (16), export contact (17), import contact (18), reboot
     * (19), set tuning parameters (21), export private key (23), import private key (24), send raw data (25), login
     * (26), status request (27), logout (29), set channel (32), signing (33–35), trace path (36), set device PIN (37),
     * set other parameters (38), telemetry request (39), set custom variable (41), binary request (50), factory reset
     * (51), path discovery (52), set flood scope key (54), control data (55), anonymous request (57), set auto-add
     * (58), set path hash mode (61), set default flood scope (63), send raw packet (65).
     */
    public static final Set<Integer> FORBIDDEN = Set.of(6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 23, 24, 25,
            26, 27, 29, 32, 33, 34, 35, 36, 37, 38, 39, 41, 50, 51, 52, 54, 55, 57, 58, 61, 63, 65);

    private final Set<Integer> sends = new ConcurrentSkipListSet<>();

    /**
     * Allows one send command on this link (prompt 084).
     *
     * @throws IllegalArgumentException when {@code code} is not one of {@link #REGISTRABLE_SENDS}
     */
    public void allowSend(int code) {
        if (!REGISTRABLE_SENDS.contains(code) || FORBIDDEN.contains(code)) {
            throw new IllegalArgumentException("MeshCore command 0x" + Integer.toHexString(code)
                    + " cannot be allowed; only 0x02, 0x03 and 0x3E may be registered");
        }
        sends.add(code);
    }

    /** Every command currently allowed, ascending. */
    public Set<Integer> allowed() {
        Set<Integer> all = new TreeSet<>(READ_COMMANDS);
        all.addAll(sends);
        return Collections.unmodifiableSet(all);
    }

    /** True when {@code code} may be sent now. */
    public boolean allows(int code) {
        return !FORBIDDEN.contains(code) && (READ_COMMANDS.contains(code) || sends.contains(code));
    }

    @Override
    public void check(byte[] frame) throws CommandRefusedException {
        if (frame == null || frame.length < 4) {
            throw new CommandRefusedException("a MeshCore command frame needs an envelope and a payload");
        }
        if (frame[0] != MeshCoreProtocol.APP_TO_RADIO) {
            throw new CommandRefusedException("a MeshCore command frame starts with '<'");
        }
        int length = (frame[1] & 0xFF) | (frame[2] & 0xFF) << 8;
        if (length != frame.length - 3 || length > MeshCoreProtocol.MAX_FRAME_SIZE) {
            throw new CommandRefusedException("the MeshCore frame length " + length + " does not match its "
                    + (frame.length - 3) + " payload bytes or exceeds " + MeshCoreProtocol.MAX_FRAME_SIZE);
        }
        int code = frame[3] & 0xFF;
        if (!allows(code)) {
            throw new CommandRefusedException("MeshCore command 0x" + Integer.toHexString(code)
                    + " is not on Foresignal's allowlist");
        }
    }
}
