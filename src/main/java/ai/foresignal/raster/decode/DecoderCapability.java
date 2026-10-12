package ai.foresignal.raster.decode;

/**
 * One decoding path the startup self-test proves (prompt 046, Weather-Imagery-Design §3).
 *
 * <p>Each value is a distinct code path inside NetCDF-Java, with its own way to fail on a given host:
 * complex packing is pure Java, PNG goes through the JDK's image reader, CCSDS/AEC needs libaec's
 * native library extracted through JNA under {@code jna.tmpdir}, and NetCDF4 goes through the pure-Java
 * HDF5 reader and its filters. A product that needs a path that failed is disabled; the others keep
 * working.
 */
public enum DecoderCapability {

    /** GRIB2 data representation template 5.3, complex packing with spatial differencing (GFS). */
    GRIB2_COMPLEX_PACKING("GRIB2 template 5.3 (complex packing)", 3),

    /** GRIB2 data representation template 5.41, PNG (MRMS). */
    GRIB2_PNG("GRIB2 template 5.41 (PNG)", 41),

    /** GRIB2 data representation template 5.42, CCSDS/AEC through libaec (ECMWF open data). */
    GRIB2_CCSDS("GRIB2 template 5.42 (CCSDS/AEC via libaec)", 42),

    /** NetCDF4 / HDF5 (GOES-R ABI Level 2). */
    NETCDF4("NetCDF4/HDF5", -1);

    private final String label;
    private final int gribTemplate;

    DecoderCapability(String label, int gribTemplate) {
        this.label = label;
        this.gribTemplate = gribTemplate;
    }

    /** A human-readable name, used in health reasons. */
    public String label() {
        return label;
    }

    /** The GRIB2 data representation template number, or -1 for NetCDF4. */
    public int gribTemplate() {
        return gribTemplate;
    }
}
