package ai.foresignal.source.adapters.nexrad;

import ai.foresignal.raster.decode.SafeNetcdf;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Reads a NEXRAD Level III radial product (prompt 106: N0B, product 153; N0G, product 154) with
 * NetCDF-Java's NIDS IOSP ({@code edu.ucar:cdm-radial}, {@code ucar.nc2.iosp.nids.Nidsiosp}), into a
 * {@link PolarScan}.
 *
 * <p><strong>Verified 2026-10-16</strong> by opening real files ({@code FWS_N0B_2026_10_16_19_15_25} and
 * {@code FWS_N0G_2026_10_16_19_01_16} from {@code unidata-nexrad-level3}) with cdm-radial 5.11.0: the IOSP
 * id is {@code NIDS}; the variables are {@code azimuth(azimuth=720)} (degrees, 0 = true north),
 * {@code gate(gate=N)} ("Radial distance to the start of gate", meters, 250 m apart from 0), and the data
 * variable {@code BaseReflectivityDR(azimuth, gate)} (dBz, N = 1840) or {@code BaseVelocityDV(azimuth, gate)}
 * (m/s, N = 1200) as floats with NaN for no data, beside a {@code *_RAW} ubyte copy; the global attributes
 * {@code RadarLatitude}, {@code RadarLongitude} and {@code time_coverage_start} (ISO-8601) locate the scan.
 *
 * <p>The file is opened through {@link SafeNetcdf}, the only place Foresignal opens a file with
 * NetCDF-Java (Weather-Imagery-Design §3), so it must be a download under {@code rasters/tmp}.
 */
public final class NidsRadialReader {

    private NidsRadialReader() {}

    /**
     * Reads the radial product in {@code file}.
     *
     * @throws SilentFormatChangeException when the file opens but lacks the radial variables or the
     *     radar position
     * @throws IOException when NetCDF-Java cannot read the file
     * @throws ai.foresignal.raster.RasterFormatException when {@link SafeNetcdf} refuses the path or
     *     NetCDF-Java cannot open it
     */
    public static PolarScan read(SafeNetcdf safe, Path file) throws IOException {
        try (NetcdfFile nc = safe.open(file)) {
            Variable azimuth = nc.findVariable("azimuth");
            Variable gate = nc.findVariable("gate");
            Variable data = dataVariable(nc);
            if (azimuth == null || gate == null || data == null) {
                throw new SilentFormatChangeException(null, "NIDS file has no azimuth/gate/radial data variable");
            }
            double lat = number(nc, "RadarLatitude");
            double lon = number(nc, "RadarLongitude");
            if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
                throw new SilentFormatChangeException(null, "NIDS file has no RadarLatitude/RadarLongitude");
            }
            float[] az = (float[]) azimuth.read().get1DJavaArray(DataType.FLOAT);
            float[] gates = (float[]) gate.read().get1DJavaArray(DataType.FLOAT);
            Array values = data.read();
            int[] shape = values.getShape();
            if (shape.length != 2 || shape[0] != az.length || shape[1] != gates.length) {
                throw new SilentFormatChangeException(null, "NIDS " + data.getShortName() + " shape does not match"
                        + " azimuth x gate");
            }
            float[] flat = (float[]) values.get1DJavaArray(DataType.FLOAT);
            Attribute units = data.findAttribute("units");
            return new PolarScan(data.getShortName(), units == null ? null : units.getStringValue(), lat, lon, az,
                    gates, flat, time(nc));
        }
    }

    /** The float radial variable of rank 2 (not its {@code _RAW} copy), or null. */
    static Variable dataVariable(NetcdfFile nc) {
        for (Variable v : nc.getVariables()) {
            if (v.getRank() == 2 && !v.getShortName().endsWith("_RAW")
                    && (v.getDataType() == DataType.FLOAT || v.getDataType() == DataType.DOUBLE)) {
                return v;
            }
        }
        return null;
    }

    private static double number(NetcdfFile nc, String name) {
        Attribute a = nc.findGlobalAttribute(name);
        return a == null || a.getNumericValue() == null ? Double.NaN : a.getNumericValue().doubleValue();
    }

    private static Instant time(NetcdfFile nc) {
        Attribute a = nc.findGlobalAttribute("time_coverage_start");
        if (a == null || a.getStringValue() == null) {
            return null;
        }
        try {
            return Instant.parse(a.getStringValue().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
