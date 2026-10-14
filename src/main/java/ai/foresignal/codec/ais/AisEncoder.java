package ai.foresignal.codec.ais;

/**
 * Encodes AIS messages into armored payloads (prompt 085), for prompt 110's NMEA sink: types 1, 2 and 3
 * ({@link AisMessage.ClassAPosition}), 5 ({@link AisMessage.StaticVoyage}), 18 ({@link AisMessage.ClassBPosition}) and
 * 24 parts A and B ({@link AisMessage.StaticDataA}, {@link AisMessage.StaticDataB}).
 *
 * <p>The exact inverse of {@link AisDecoder}: absent values are written as their "not available" sentinels, scaled
 * values are rounded to the wire resolution, and {@code AisDecoder.decode(encode(m))} equals {@code m} for every
 * message whose values lie on that resolution (every decoded message does). Pure and thread-safe.
 */
public final class AisEncoder {

    private AisEncoder() {}

    /**
     * Encodes a supported message.
     *
     * @throws IllegalArgumentException for a type the encoder does not write
     */
    public static SixBit.Armored encode(AisMessage message) {
        return switch (message) {
            case AisMessage.ClassAPosition m -> encode(m);
            case AisMessage.StaticVoyage m -> encode(m);
            case AisMessage.ClassBPosition m -> encode(m);
            case AisMessage.StaticDataA m -> encode(m);
            case AisMessage.StaticDataB m -> encode(m);
            default -> throw new IllegalArgumentException("type " + message.type() + " is not encoded");
        };
    }

    /** Types 1, 2 and 3 (168 bits). */
    public static SixBit.Armored encode(AisMessage.ClassAPosition m) {
        if (m.type() < 1 || m.type() > 3) {
            throw new IllegalArgumentException("a Class A position report is type 1, 2 or 3, not " + m.type());
        }
        SixBit.Writer w = header(m.type(), m.repeat(), m.mmsi());
        w.unsigned(4, m.navStatus() == null ? 15 : m.navStatus());
        w.signed(8, m.rot() == null ? -128 : m.rot());
        w.unsigned(10, scaled(m.sog(), 10, 1023));
        w.bool(m.accuracy());
        w.signed(28, position(m.longitude(), AisDecoder.POSITION_SCALE, 181));
        w.signed(27, position(m.latitude(), AisDecoder.POSITION_SCALE, 91));
        w.unsigned(12, scaled(m.cog(), 10, 3600));
        w.unsigned(9, m.heading() == null ? 511 : m.heading());
        w.unsigned(6, m.second());
        w.unsigned(2, m.maneuver());
        w.unsigned(3, 0);
        w.bool(m.raim());
        w.unsigned(19, m.radio());
        return w.armor();
    }

    /** Type 5 (424 bits, two sentences). */
    public static SixBit.Armored encode(AisMessage.StaticVoyage m) {
        SixBit.Writer w = header(5, m.repeat(), m.mmsi());
        w.unsigned(2, m.aisVersion());
        w.unsigned(30, m.imo() == null ? 0 : m.imo());
        w.text(7, m.callsign());
        w.text(20, m.name());
        w.unsigned(8, m.shipType() == null ? 0 : m.shipType());
        dimension(w, m.dimension());
        w.unsigned(4, m.epfd());
        AisMessage.Eta eta = m.eta();
        w.unsigned(4, eta == null ? 0 : eta.month());
        w.unsigned(5, eta == null ? 0 : eta.day());
        w.unsigned(5, eta == null ? 24 : eta.hour());
        w.unsigned(6, eta == null ? 60 : eta.minute());
        w.unsigned(8, scaled(m.draft(), 10, 0));
        w.text(20, m.destination());
        w.bool(m.dte());
        w.unsigned(1, 0);
        return w.armor();
    }

    /** Type 18 (168 bits). */
    public static SixBit.Armored encode(AisMessage.ClassBPosition m) {
        SixBit.Writer w = header(18, m.repeat(), m.mmsi());
        w.unsigned(8, 0);
        w.unsigned(10, scaled(m.sog(), 10, 1023));
        w.bool(m.accuracy());
        w.signed(28, position(m.longitude(), AisDecoder.POSITION_SCALE, 181));
        w.signed(27, position(m.latitude(), AisDecoder.POSITION_SCALE, 91));
        w.unsigned(12, scaled(m.cog(), 10, 3600));
        w.unsigned(9, m.heading() == null ? 511 : m.heading());
        w.unsigned(6, m.second());
        w.unsigned(2, m.regional());
        w.bool(m.cs());
        w.bool(m.display());
        w.bool(m.dsc());
        w.bool(m.band());
        w.bool(m.msg22());
        w.bool(m.assigned());
        w.bool(m.raim());
        w.unsigned(20, m.radio());
        return w.armor();
    }

    /** Type 24 part A (168 bits: 160 and 8 spare). */
    public static SixBit.Armored encode(AisMessage.StaticDataA m) {
        SixBit.Writer w = header(24, m.repeat(), m.mmsi());
        w.unsigned(2, 0);
        w.text(20, m.name());
        w.unsigned(8, 0);
        return w.armor();
    }

    /** Type 24 part B (168 bits). */
    public static SixBit.Armored encode(AisMessage.StaticDataB m) {
        SixBit.Writer w = header(24, m.repeat(), m.mmsi());
        w.unsigned(2, 1);
        w.unsigned(8, m.shipType() == null ? 0 : m.shipType());
        w.text(3, m.vendorId());
        w.unsigned(4, m.model());
        w.unsigned(20, m.serial());
        w.text(7, m.callsign());
        if (AisMessage.StaticDataB.auxiliary(m.mmsi())) {
            w.unsigned(30, m.mothershipMmsi() == null ? 0 : m.mothershipMmsi());
        } else {
            dimension(w, m.dimension());
        }
        w.unsigned(6, 0);
        return w.armor();
    }

    private static SixBit.Writer header(int type, int repeat, int mmsi) {
        return new SixBit.Writer().unsigned(6, type).unsigned(2, repeat).unsigned(30, mmsi);
    }

    private static void dimension(SixBit.Writer w, AisMessage.Dimension d) {
        w.unsigned(9, d == null ? 0 : d.a());
        w.unsigned(9, d == null ? 0 : d.b());
        w.unsigned(6, d == null ? 0 : d.c());
        w.unsigned(6, d == null ? 0 : d.d());
    }

    private static long scaled(Double value, int factor, int sentinel) {
        return value == null ? sentinel : Math.round(value * factor);
    }

    private static long position(Double degrees, double scale, int sentinelDegrees) {
        return Math.round((degrees == null ? sentinelDegrees : degrees) * scale);
    }
}
