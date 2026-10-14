package ai.foresignal.source.codec;

import ai.foresignal.source.spi.CommandGuard;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The allowlist of everything Foresignal may write to gpsd (prompt 086): only {@code ?WATCH}, {@code ?VERSION},
 * {@code ?DEVICES} and {@code ?POLL}.
 *
 * <p>gpsd's JSON protocol also has commands that change a receiver ({@code ?DEVICE=} sets its speed, mode and cycle,
 * and gpsd can forward binary control strings to the device). A GNSS integrity sentinel must never reconfigure the
 * receiver it is watching, so no such command can pass: UBX messages are decoded from gpsd's raw passthrough but never
 * sent, and no {@code CFG} write exists anywhere in Foresignal.
 *
 * <ul>
 *   <li>The bytes must be printable ASCII, one or more commands each ending in {@code ;}, optionally followed by one
 *       line terminator.
 *   <li>{@code ?WATCH} may carry a JSON object; the object must not contain {@code ;} or a line break, so it cannot
 *       smuggle a second command.
 *   <li>At most {@value #MAX_BYTES} bytes.
 * </ul>
 */
public final class GpsdCommandGuard implements CommandGuard {

    /** The shared guard; it holds no state. */
    public static final GpsdCommandGuard INSTANCE = new GpsdCommandGuard();

    /** The command names that may be written. */
    public static final List<String> ALLOWED = List.of("WATCH", "VERSION", "DEVICES", "POLL");

    /** The longest write accepted. */
    public static final int MAX_BYTES = 256;

    /** One allowed command. */
    static final String COMMAND = "\\?(?:WATCH(?:=\\{[\\x20-\\x3A\\x3C-\\x7E]*\\})?|VERSION|DEVICES|POLL);";

    /** A whole write: one or more allowed commands and an optional terminator. */
    static final Pattern WRITE = Pattern.compile("^(?:" + COMMAND + ")+(?:\\r?\\n)?$");

    private GpsdCommandGuard() {}

    /**
     * Refuses anything but allowlisted commands.
     *
     * @throws CommandRefusedException when the bytes are empty, too long, not ASCII, or contain any other command
     */
    @Override
    public void check(byte[] frame) throws CommandRefusedException {
        if (frame == null || frame.length == 0) {
            throw new CommandRefusedException("gpsd: an empty write is refused");
        }
        if (frame.length > MAX_BYTES) {
            throw new CommandRefusedException("gpsd: a write longer than " + MAX_BYTES + " bytes is refused");
        }
        for (byte b : frame) {
            if (b < 0) {
                throw new CommandRefusedException("gpsd: only ASCII commands may be written");
            }
        }
        String text = new String(frame, StandardCharsets.US_ASCII);
        if (!WRITE.matcher(text).matches()) {
            throw new CommandRefusedException("gpsd: only ?WATCH, ?VERSION, ?DEVICES and ?POLL may be written");
        }
    }

    /** True when {@link #check} accepts {@code frame}. */
    public boolean allows(byte[] frame) {
        try {
            check(frame);
            return true;
        } catch (CommandRefusedException e) {
            return false;
        }
    }
}
