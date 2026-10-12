package ai.foresignal.raster.decode;

import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import ucar.nc2.grib.grib2.Grib2Gds;
import ucar.nc2.grib.grib2.Grib2Pds;
import ucar.nc2.grib.grib2.Grib2Record;
import ucar.nc2.grib.grib2.Grib2RecordScanner;
import ucar.unidata.io.RandomAccessFile;

/**
 * GRIB2 decoding on NetCDF-Java (prompt 046): MRMS (template 5.41, PNG), GFS (5.3, complex packing)
 * and ECMWF open data (5.42, CCSDS/AEC through libaec).
 *
 * <p>Messages are read one by one with {@link Grib2RecordScanner} and {@link Grib2Record#readData};
 * no {@code .gbx9} or {@code .ncx4} index is ever created, and the GRIB IOSP (which would build them)
 * is never used. The field is the first message whose discipline, parameter category and number
 * match the request and, when the request names one, whose first fixed surface (type and value)
 * matches; <strong>never a message index</strong>. A file with GRIB2 messages but no matching one is a
 * {@link SilentFormatChangeException} ("field MergedReflectivityQCComposite missing at level 500 m").
 *
 * <p>The grid comes from the grid definition section, never from constants. Template 3.0
 * (latitude/longitude) is read from the section's raw octets (47–71: La1, Lo1, La2, Lo2, Di, Dj, in
 * units of the basic angle over its subdivisions, or micro-degrees when the basic angle is 0), because NetCDF-Java exposes them as floats that would
 * turn MRMS's 230.005° into 230.00499°. Samples are reordered to rows north to south and columns
 * west to east whatever the scanning mode (flags 0x80 and 0x40); a column-major (0x20) message is
 * refused. Other grid templates are refused until a product needs them.
 *
 * <p>Missing samples (the bitmap) are {@code NaN}; MRMS's in-band sentinels (−999, −99, −3) are
 * ordinary values here and are mapped by the adapter.
 */
@Component
public class Grib2Decoder implements RasterDecoder {

    /** GRIB2 scanning mode flag: points of the first row run east to west. */
    static final int SCAN_I_NEGATIVE = 0x80;

    /** GRIB2 scanning mode flag: rows run south to north. */
    static final int SCAN_J_POSITIVE = 0x40;

    /** GRIB2 scanning mode flag: adjacent points are consecutive in j (column-major). */
    static final int SCAN_J_CONSECUTIVE = 0x20;

    private static final double LEVEL_TOLERANCE = 1e-3;

    private final SafeNetcdf safe;

    /** Creates the decoder. */
    public Grib2Decoder(SafeNetcdf safe) {
        this.safe = safe;
    }

    @Override
    public boolean supports(DecodeRequest r) {
        return r != null && r.format() == DecodeRequest.Format.GRIB2;
    }

    @Override
    public DecodedGrid decode(Path file, DecodeRequest r) {
        if (!supports(r)) {
            throw new IllegalArgumentException("Grib2Decoder reads GRIB2 requests, not " + (r == null ? null : r.format()));
        }
        Path inflated = null;
        Path input = file;
        if (GzipInputs.isGzip(safe.requireAllowed(file))) {
            inflated = GzipInputs.inflateSibling(file);
            input = inflated;
        }
        try (RandomAccessFile raf = safe.openRandomAccess(input)) {
            if (!Grib2RecordScanner.isValidFile(raf)) {
                throw new RasterFormatException(file.getFileName() + " is not a GRIB2 file");
            }
            Grib2RecordScanner scanner = new Grib2RecordScanner(raf);
            int messages = 0;
            while (scanner.hasNext()) {
                Grib2Record record = scanner.next();
                messages++;
                if (matches(record, r)) {
                    return toGrid(record, raf, r, messages);
                }
            }
            if (messages == 0) {
                throw new RasterFormatException(file.getFileName() + " holds no GRIB2 message");
            }
            throw new SilentFormatChangeException(r.productId(), missingField(r));
        } catch (IOException e) {
            throw new RasterFormatException("Cannot read GRIB2 " + file.getFileName() + ": " + e.getMessage(), e);
        } finally {
            GzipInputs.deleteQuietly(inflated);
        }
    }

    /** The health reason for a missing field, for example "field PrecipRate missing at level 0 m". */
    static String missingField(DecodeRequest r) {
        return "field " + r.label() + " missing" + (r.levelLabel() == null ? "" : " at level " + r.levelLabel());
    }

    /** True when {@code record} is the field {@code r} asks for. */
    static boolean matches(Grib2Record record, DecodeRequest r) {
        Grib2Pds pds = record.getPDS();
        if (record.getDiscipline() != r.discipline() || pds.getParameterCategory() != r.category()
                || pds.getParameterNumber() != r.number()) {
            return false;
        }
        if (r.levelType() != null && pds.getLevelType1() != r.levelType()) {
            return false;
        }
        return r.levelValue() == null || Math.abs(pds.getLevelValue1() - r.levelValue()) <= LEVEL_TOLERANCE;
    }

    private DecodedGrid toGrid(Grib2Record record, RandomAccessFile raf, DecodeRequest r, int messageNumber)
            throws IOException {
        Grib2Gds gds = record.getGDS();
        int scanMode = gds.getScanMode();
        if ((scanMode & SCAN_J_CONSECUTIVE) != 0) {
            throw new RasterFormatException("GRIB2 scanning mode " + scanMode + " (column-major) is not supported");
        }
        LatLonBox box = latLonBox(gds);
        GridDefinition grid = box.definition();
        Instant reference = Instant.ofEpochMilli(record.getReferenceDate().getMillis());
        Instant valid = validTime(reference, record.getPDS());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("discipline", record.getDiscipline());
        metadata.put("category", record.getPDS().getParameterCategory());
        metadata.put("number", record.getPDS().getParameterNumber());
        metadata.put("levelType", record.getPDS().getLevelType1());
        metadata.put("levelValue", record.getPDS().getLevelValue1());
        metadata.put("dataTemplate", record.getDataRepresentationSection().getDataTemplate());
        metadata.put("gridTemplate", gds.template);
        metadata.put("scanMode", scanMode);
        metadata.put("center", record.getId().getCenter_id());
        metadata.put("message", messageNumber);
        if (r.headerOnly()) {
            return new DecodedGrid(grid, box.nx(), box.ny(), null, null, valid, reference, metadata);
        }
        float[] data = record.readData(raf);
        if (data.length != (long) box.nx() * box.ny()) {
            throw new RasterFormatException("GRIB2 message has " + data.length + " samples; its grid needs "
                    + ((long) box.nx() * box.ny()));
        }
        reorder(data, box.nx(), box.ny(), scanMode);
        return new DecodedGrid(grid, box.nx(), box.ny(), data, null, valid, reference, metadata);
    }

    /** Reference time plus the forecast time in its unit (GRIB2 code table 4.4). */
    static Instant validTime(Instant reference, Grib2Pds pds) {
        long amount = pds.getForecastTime();
        if (amount == 0) {
            return reference;
        }
        long seconds = switch (pds.getTimeUnit()) {
            case 0 -> 60L;
            case 1 -> 3600L;
            case 2 -> 86_400L;
            case 10 -> 3 * 3600L;
            case 11 -> 6 * 3600L;
            case 12 -> 12 * 3600L;
            case 13 -> 1L;
            default -> throw new RasterFormatException("GRIB2 time unit " + pds.getTimeUnit() + " is not supported");
        };
        return reference.plusSeconds(amount * seconds);
    }

    /** Flips rows and columns in place so rows run north to south and columns west to east. */
    static void reorder(float[] data, int nx, int ny, int scanMode) {
        if ((scanMode & SCAN_I_NEGATIVE) != 0) {
            for (int row = 0; row < ny; row++) {
                int base = row * nx;
                for (int i = 0, j = nx - 1; i < j; i++, j--) {
                    float t = data[base + i];
                    data[base + i] = data[base + j];
                    data[base + j] = t;
                }
            }
        }
        if ((scanMode & SCAN_J_POSITIVE) != 0) {
            float[] row = new float[nx];
            for (int top = 0, bottom = ny - 1; top < bottom; top++, bottom--) {
                System.arraycopy(data, top * nx, row, 0, nx);
                System.arraycopy(data, bottom * nx, data, top * nx, nx);
                System.arraycopy(row, 0, data, bottom * nx, nx);
            }
        }
    }

    /**
     * A latitude/longitude grid read from a template 3.0 section.
     *
     * @param nx columns
     * @param ny rows
     * @param westLon center longitude of the westernmost column, degrees as encoded (0..360 allowed)
     * @param northLat center latitude of the northernmost row
     * @param dx column step, degrees
     * @param dy row step, degrees (positive)
     */
    record LatLonBox(int nx, int ny, double westLon, double northLat, double dx, double dy) {

        /** The grid definition: edges half a cell outside the centers, longitudes in −180..180. */
        GridDefinition definition() {
            Map<String, Object> params = new LinkedHashMap<>();
            double lo1 = normalizeLon(westLon);
            params.put("lo1", round(lo1));
            params.put("la1", round(northLat));
            params.put("dx", round(dx));
            params.put("dy", round(dy));
            double north = Math.min(90.0, round(northLat + dy / 2));
            double south = Math.max(-90.0, round(northLat - (ny - 0.5) * dy));
            if (Math.abs(nx * dx - 360.0) < dx / 2) {
                return new GridDefinition(GridDefinition.LATLON, nx, ny, -180.0, south, 180.0, north, params);
            }
            double west = round(normalizeLon(westLon - dx / 2));
            double east = round(west + nx * dx);
            if (east > 180.0) {
                east = round(east - 360.0);
            }
            return new GridDefinition(GridDefinition.LATLON, nx, ny, west, south, east, north, params);
        }
    }

    /** The latitude/longitude box of {@code gds}; refuses other grid templates. */
    static LatLonBox latLonBox(Grib2Gds gds) {
        if (gds.template != 0 || !(gds instanceof Grib2Gds.LatLon ll)) {
            throw new RasterFormatException("GRIB2 grid template 3." + gds.template + " is not supported yet");
        }
        int nx = gds.getNx();
        int ny = gds.getNy();
        int scan = gds.getScanMode();
        double la1;
        double lo1;
        double la2;
        double lo2;
        double di;
        double dj;
        byte[] raw = gds.getRawBytes();
        double unit = angleUnit(raw);
        if (raw.length >= 71 && unit > 0) {
            la1 = signed32(raw, 47) * unit;
            lo1 = signed32(raw, 51) * unit;
            la2 = signed32(raw, 56) * unit;
            lo2 = signed32(raw, 60) * unit;
            di = signed32(raw, 64) * unit;
            dj = signed32(raw, 68) * unit;
        } else {
            la1 = ll.la1;
            lo1 = ll.lo1;
            la2 = ll.la2;
            lo2 = ll.lo2;
            di = ll.deltaLon;
            dj = ll.deltaLat;
        }
        di = Math.abs(di);
        dj = Math.abs(dj);
        if (!(di > 0) || !(dj > 0)) {
            throw new RasterFormatException("GRIB2 grid has no increments (Di " + di + ", Dj " + dj + ")");
        }
        double westLon = (scan & SCAN_I_NEGATIVE) == 0 ? lo1 : lo2;
        double northLat = Math.max(la1, la2);
        return new LatLonBox(nx, ny, westLon, northLat, di, dj);
    }

    /**
     * The angle unit of a template 3.0 section, degrees: 10⁻⁶ when the basic angle (octets 39–42) is 0
     * or missing, else basic angle / subdivisions (octets 43–46; MRMS writes 1 / 1,000,000); 0 when
     * it cannot be read.
     */
    static double angleUnit(byte[] raw) {
        if (raw.length < 46) {
            return 0;
        }
        long basic = unsigned32(raw, 39);
        long subdivisions = unsigned32(raw, 43);
        if (basic == 0 || basic == 0xFFFF_FFFFL) {
            return 1e-6;
        }
        if (subdivisions == 0 || subdivisions == 0xFFFF_FFFFL) {
            return 0;
        }
        return (double) basic / subdivisions;
    }

    /** Octets {@code octet..octet+3} (1-based, within the section) as a GRIB2 sign-magnitude integer. */
    static long signed32(byte[] raw, int octet) {
        long value = unsigned32(raw, octet);
        return (value & 0x8000_0000L) != 0 ? -(value & 0x7FFF_FFFFL) : value;
    }

    private static long unsigned32(byte[] raw, int octet) {
        int i = octet - 1;
        return ((raw[i] & 0xFFL) << 24) | ((raw[i + 1] & 0xFFL) << 16) | ((raw[i + 2] & 0xFFL) << 8) | (raw[i + 3] & 0xFFL);
    }

    /** {@code lon} in [−180, 180). */
    static double normalizeLon(double lon) {
        double n = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return n == 180.0 ? -180.0 : n;
    }

    private static double round(double v) {
        return Math.round(v * 1e9) / 1e9;
    }
}
