package ai.foresignal.codec.ais;

/**
 * Decodes an armored AIS payload into an {@link AisMessage} (prompt 085). Bit layouts after ITU-R M.1371-5 and the
 * gpsd AIVDM/AIVDO protocol notes; sentinels become null (see {@link AisMessage}).
 *
 * <p>Minimum lengths: 168 bits for types 1–4 and 18, 422 for type 5 (the trailing DTE bit may be missing), 312 for
 * 19, 272 for 21, 160 for 24 part A, 168 for part B and 96 for 27. A shorter payload is an
 * {@link AisDecodeException}; other types are {@link AisMessage.Unsupported}. Pure and thread-safe.
 */
public final class AisDecoder {

    /** Degrees per unit of a 1/10,000-minute position. */
    static final double POSITION_SCALE = 600_000.0;

    /** Degrees per unit of a type 27 1/10-minute position. */
    static final double LONG_RANGE_SCALE = 600.0;

    private AisDecoder() {}

    /** A payload that cannot be decoded (too short, or not armored). */
    public static final class AisDecodeException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        AisDecodeException(String message) {
            super(message);
        }
    }

    /**
     * Decodes one complete payload.
     *
     * @throws AisDecodeException when the payload is too short for its type or not armored
     */
    public static AisMessage decode(SixBit.Armored armored) {
        SixBit.Reader r;
        try {
            r = SixBit.decode(armored);
        } catch (IllegalArgumentException e) {
            throw new AisDecodeException(e.getMessage());
        }
        if (r.length() < 38) {
            throw new AisDecodeException("payload of " + r.length() + " bits is too short for a header");
        }
        int type = r.uint(6);
        int repeat = r.uint(2);
        int mmsi = r.uint(30);
        try {
            return switch (type) {
                case 1, 2, 3 -> {
                    require(r, 168, type);
                    yield classA(r, type, repeat, mmsi);
                }
                case 4 -> {
                    require(r, 168, type);
                    yield new AisMessage.BaseStationReport(repeat, mmsi);
                }
                case 5 -> {
                    require(r, 422, type);
                    yield staticVoyage(r, repeat, mmsi);
                }
                case 18 -> {
                    require(r, 168, type);
                    yield classB(r, repeat, mmsi);
                }
                case 19 -> {
                    require(r, 312, type);
                    yield classBExtended(r, repeat, mmsi);
                }
                case 21 -> {
                    require(r, 272, type);
                    yield new AisMessage.AidToNavigation(repeat, mmsi);
                }
                case 24 -> staticData(r, repeat, mmsi);
                case 27 -> {
                    require(r, 96, type);
                    yield longRange(r, repeat, mmsi);
                }
                default -> new AisMessage.Unsupported(type, repeat, mmsi);
            };
        } catch (AisDecodeException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AisDecodeException("type " + type + ": " + e.getMessage());
        }
    }

    /** Decodes {@code payload} with {@code fillBits}. */
    public static AisMessage decode(String payload, int fillBits) {
        try {
            return decode(new SixBit.Armored(payload, fillBits));
        } catch (AisDecodeException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AisDecodeException(e.getMessage());
        }
    }

    private static void require(SixBit.Reader r, int bits, int type) {
        if (r.length() < bits) {
            throw new AisDecodeException("type " + type + " needs " + bits + " bits, the payload has " + r.length());
        }
    }

    private static AisMessage.ClassAPosition classA(SixBit.Reader r, int type, int repeat, int mmsi) {
        Integer status = navStatus(r.uint(4));
        Integer rot = rot(r.sint(8));
        Double sog = sog(r.uint(10));
        boolean accuracy = r.bool();
        Double lon = longitude(r.sint(28), POSITION_SCALE);
        Double lat = latitude(r.sint(27), POSITION_SCALE);
        Double cog = cog(r.uint(12));
        Integer heading = heading(r.uint(9));
        int second = r.uint(6);
        int maneuver = r.uint(2);
        r.uint(3);
        boolean raim = r.bool();
        int radio = r.uint(19);
        return new AisMessage.ClassAPosition(type, repeat, mmsi, status, rot, sog, accuracy, lon, lat, cog, heading,
                second, maneuver, raim, radio);
    }

    private static AisMessage.StaticVoyage staticVoyage(SixBit.Reader r, int repeat, int mmsi) {
        int version = r.uint(2);
        Integer imo = positive(r.uint(30));
        String callsign = r.text(7);
        String name = r.text(20);
        Integer shipType = positive(r.uint(8));
        AisMessage.Dimension dims = AisMessage.Dimension.ofOrNull(r.uint(9), r.uint(9), r.uint(6), r.uint(6));
        int epfd = r.uint(4);
        AisMessage.Eta eta = AisMessage.Eta.ofOrNull(r.uint(4), r.uint(5), r.uint(5), r.uint(6));
        Integer draftRaw = positive(r.uint(8));
        String destination = r.text(20);
        boolean dte = r.peekOrZero(1) == 1;
        return new AisMessage.StaticVoyage(repeat, mmsi, version, imo, callsign, name, shipType, dims, epfd, eta,
                draftRaw == null ? null : draftRaw / 10.0, destination, dte);
    }

    private static AisMessage.ClassBPosition classB(SixBit.Reader r, int repeat, int mmsi) {
        r.uint(8);
        Double sog = sog(r.uint(10));
        boolean accuracy = r.bool();
        Double lon = longitude(r.sint(28), POSITION_SCALE);
        Double lat = latitude(r.sint(27), POSITION_SCALE);
        Double cog = cog(r.uint(12));
        Integer heading = heading(r.uint(9));
        int second = r.uint(6);
        int regional = r.uint(2);
        boolean cs = r.bool();
        boolean display = r.bool();
        boolean dsc = r.bool();
        boolean band = r.bool();
        boolean msg22 = r.bool();
        boolean assigned = r.bool();
        boolean raim = r.bool();
        int radio = r.uint(20);
        return new AisMessage.ClassBPosition(repeat, mmsi, sog, accuracy, lon, lat, cog, heading, second, regional, cs,
                display, dsc, band, msg22, assigned, raim, radio);
    }

    private static AisMessage.ClassBExtended classBExtended(SixBit.Reader r, int repeat, int mmsi) {
        r.uint(8);
        Double sog = sog(r.uint(10));
        boolean accuracy = r.bool();
        Double lon = longitude(r.sint(28), POSITION_SCALE);
        Double lat = latitude(r.sint(27), POSITION_SCALE);
        Double cog = cog(r.uint(12));
        Integer heading = heading(r.uint(9));
        int second = r.uint(6);
        r.uint(4);
        String name = r.text(20);
        Integer shipType = positive(r.uint(8));
        AisMessage.Dimension dims = AisMessage.Dimension.ofOrNull(r.uint(9), r.uint(9), r.uint(6), r.uint(6));
        int epfd = r.uint(4);
        boolean raim = r.bool();
        boolean dte = r.bool();
        boolean assigned = r.bool();
        return new AisMessage.ClassBExtended(repeat, mmsi, sog, accuracy, lon, lat, cog, heading, second, name, shipType,
                dims, epfd, raim, dte, assigned);
    }

    private static AisMessage staticData(SixBit.Reader r, int repeat, int mmsi) {
        require(r, 40, 24);
        int part = r.uint(2);
        if (part == 0) {
            require(r, 160, 24);
            return new AisMessage.StaticDataA(repeat, mmsi, r.text(20));
        }
        if (part == 1) {
            require(r, 168, 24);
            Integer shipType = positive(r.uint(8));
            String vendor = r.text(3);
            int model = r.uint(4);
            int serial = r.uint(20);
            String callsign = r.text(7);
            if (AisMessage.StaticDataB.auxiliary(mmsi)) {
                Integer mothership = positive(r.uint(30));
                return new AisMessage.StaticDataB(repeat, mmsi, shipType, vendor, model, serial, callsign, null,
                        mothership);
            }
            AisMessage.Dimension dims = AisMessage.Dimension.ofOrNull(r.uint(9), r.uint(9), r.uint(6), r.uint(6));
            return new AisMessage.StaticDataB(repeat, mmsi, shipType, vendor, model, serial, callsign, dims, null);
        }
        return new AisMessage.Unsupported(24, repeat, mmsi);
    }

    private static AisMessage.LongRangePosition longRange(SixBit.Reader r, int repeat, int mmsi) {
        boolean accuracy = r.bool();
        boolean raim = r.bool();
        Integer status = navStatus(r.uint(4));
        Double lon = longitude(r.sint(18), LONG_RANGE_SCALE);
        Double lat = latitude(r.sint(17), LONG_RANGE_SCALE);
        int speed = r.uint(6);
        int course = r.uint(9);
        boolean gnss = r.bool();
        return new AisMessage.LongRangePosition(repeat, mmsi, accuracy, raim, status, lon, lat,
                speed == 63 ? null : (double) speed, course > 359 ? null : (double) course, gnss);
    }

    // ---------- sentinels (adapter rule 11) ----------

    static Integer navStatus(int raw) {
        return raw == 15 ? null : raw;
    }

    static Integer rot(int raw) {
        return raw == -128 ? null : raw;
    }

    static Double sog(int raw) {
        return raw >= 1023 ? null : raw / 10.0;
    }

    static Double cog(int raw) {
        return raw >= 3600 ? null : raw / 10.0;
    }

    static Integer heading(int raw) {
        return raw > 359 ? null : raw;
    }

    static Double longitude(int raw, double scale) {
        double degrees = raw / scale;
        return degrees < -180 || degrees > 180 ? null : degrees;
    }

    static Double latitude(int raw, double scale) {
        double degrees = raw / scale;
        return degrees < -90 || degrees > 90 ? null : degrees;
    }

    static Integer positive(int raw) {
        return raw == 0 ? null : raw;
    }
}
