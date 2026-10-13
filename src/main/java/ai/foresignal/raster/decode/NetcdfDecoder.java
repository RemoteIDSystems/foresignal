package ai.foresignal.raster.decode;

import ai.foresignal.raster.GeosGrid;
import ai.foresignal.raster.RasterFormatException;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.SilentFormatChangeException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * NetCDF4/HDF5 decoding on NetCDF-Java (prompt 046): GOES-R ABI Level 2 products such as CMIPC
 * band 13.
 *
 * <p>Files are opened in enhanced mode through {@link SafeNetcdf}, so {@code scale_factor},
 * {@code add_offset}, {@code _Unsigned} and {@code _FillValue} are applied by NetCDF-Java: a fill
 * sample reads as {@code NaN}. Variables, coordinates and projection attributes are found by name;
 * a missing variable or projection attribute is a {@link SilentFormatChangeException} ("variable CMI
 * missing"). With a quality variable ({@code DQF}), samples whose flag exceeds the request's
 * {@code maxQuality}, or is fill, become {@code NaN}.
 *
 * <p>A gridded variable must be two-dimensional {@code (y, x)} with a {@code grid_mapping} naming a
 * {@code geostationary} projection variable ({@code goes_imager_projection}): its
 * {@code perspective_point_height}, {@code semi_major_axis}, {@code semi_minor_axis},
 * {@code longitude_of_projection_origin} and {@code sweep_angle_axis}, plus the {@code x}/{@code y}
 * scan-angle coordinates (radians), become a {@code geos} {@link GridDefinition} whose box is the
 * projected outline of the grid. Rows are reordered north to south and columns west to east when
 * the coordinates run the other way.
 *
 * <p>Times: {@code time_coverage_start} is the valid time; both coverage times are in the metadata
 * ({@code obsStart}, {@code obsEnd}).
 */
@Component
public class NetcdfDecoder implements RasterDecoder {

    /** The CF {@code grid_mapping_name} of the GOES-R fixed grid. */
    static final String GEOSTATIONARY = "geostationary";

    private final SafeNetcdf safe;

    /** Creates the decoder. */
    public NetcdfDecoder(SafeNetcdf safe) {
        this.safe = safe;
    }

    @Override
    public boolean supports(DecodeRequest r) {
        return r != null && r.format() == DecodeRequest.Format.NETCDF;
    }

    @Override
    public DecodedGrid decode(Path file, DecodeRequest r) {
        if (!supports(r)) {
            throw new IllegalArgumentException("NetcdfDecoder reads NetCDF requests, not " + (r == null ? null : r.format()));
        }
        Path inflated = null;
        Path input = file;
        if (GzipInputs.isGzip(safe.requireAllowed(file))) {
            inflated = GzipInputs.inflateSibling(file);
            input = inflated;
        }
        try (NetcdfDataset ds = safe.openDataset(input)) {
            Variable variable = requireVariable(ds, r.variable(), r);
            Map<String, Object> metadata = new LinkedHashMap<>();
            Instant start = time(ds, "time_coverage_start");
            Instant end = time(ds, "time_coverage_end");
            put(metadata, "obsStart", start == null ? null : start.toString());
            put(metadata, "obsEnd", end == null ? null : end.toString());
            put(metadata, "units", variable.findAttributeString("units", null));
            put(metadata, "platform", globalString(ds, "platform_ID"));
            put(metadata, "scene", globalString(ds, "scene_id"));
            put(metadata, "fileType", ds.getFileTypeId());
            if (!r.gridded()) {
                int size = Math.toIntExact(variable.getSize());
                float[] values = r.headerOnly() ? null : floats(variable.read());
                return new DecodedGrid(null, Math.max(1, size), 1, values, null, start, null, metadata);
            }
            int[] shape = variable.getShape();
            if (shape.length != 2) {
                throw new RasterFormatException("Variable " + r.variable() + " has " + shape.length
                        + " dimensions; a gridded decode needs (y, x)");
            }
            int ny = shape[0];
            int nx = shape[1];
            Geos geos = geos(ds, variable, nx, ny, r);
            metadata.put("projection", geos.projectionName());
            if (r.headerOnly()) {
                return new DecodedGrid(geos.definition(), nx, ny, null, null, start, null, metadata);
            }
            float[] data = floats(variable.read());
            if (r.qualityVariable() != null) {
                Variable quality = requireVariable(ds, r.qualityVariable(), r);
                Array flags = quality.read();
                if (flags.getSize() != data.length) {
                    throw new RasterFormatException("Quality variable " + r.qualityVariable() + " has " + flags.getSize()
                            + " samples; " + r.variable() + " has " + data.length);
                }
                for (int i = 0; i < data.length; i++) {
                    double flag = flags.getDouble(i);
                    if (Double.isNaN(flag) || flag > r.maxQuality()) {
                        data[i] = Float.NaN;
                    }
                }
            }
            reorder(data, nx, ny, geos.flipColumns(), geos.flipRows());
            return new DecodedGrid(geos.definition(), nx, ny, data, null, start, null, metadata);
        } catch (IOException e) {
            throw new RasterFormatException("Cannot read NetCDF " + file.getFileName() + ": " + e.getMessage(), e);
        } finally {
            GzipInputs.deleteQuietly(inflated);
        }
    }

    /**
     * Opens {@code file} in enhanced mode for reading several variables by name (prompt 048: GLM flash
     * vectors, ABI fire characterization), as opposed to {@link #decode}, which reads one gridded field.
     *
     * @param file the downloaded file, under {@code rasters/tmp} ({@link SafeNetcdf})
     * @param productId the product or source the read is for, carried into a
     *     {@link SilentFormatChangeException} for a missing variable
     * @return an open reader; close it
     * @throws RasterFormatException when the file is not allowed or not readable
     */
    public Reader open(Path file, String productId) {
        return new Reader(safe.openDataset(file), productId, file.getFileName().toString());
    }

    /**
     * Named-variable access to one open NetCDF file (prompt 048). Enhanced mode applies
     * {@code scale_factor}, {@code add_offset}, {@code _Unsigned} and {@code _FillValue}, so a fill sample
     * reads as {@code NaN}. A missing variable is a {@link SilentFormatChangeException} with the reason
     * {@code variable {name} missing}.
     */
    public static final class Reader implements AutoCloseable {

        private final NetcdfDataset ds;
        private final String productId;
        private final String name;

        Reader(NetcdfDataset ds, String productId, String name) {
            this.ds = ds;
            this.productId = productId;
            this.name = name;
        }

        /** True when the file has variable {@code variable}. */
        public boolean has(String variable) {
            return ds.findVariable(variable) != null;
        }

        /** The shape of {@code variable}. */
        public int[] shape(String variable) {
            return require(variable).getShape();
        }

        /**
         * Every value of {@code variable}, flattened row-major in file order, as floats ({@code NaN} for
         * fill).
         */
        public float[] floats(String variable) {
            try {
                return NetcdfDecoder.floats(require(variable).read());
            } catch (IOException e) {
                throw new RasterFormatException("Cannot read " + variable + " of " + name + ": " + e.getMessage(), e);
            }
        }

        /** Every value of {@code variable} as doubles ({@code NaN} for fill). */
        public double[] doubles(String variable) {
            try {
                return (double[]) require(variable).read().get1DJavaArray(DataType.DOUBLE);
            } catch (IOException e) {
                throw new RasterFormatException("Cannot read " + variable + " of " + name + ": " + e.getMessage(), e);
            }
        }

        /** The first value of {@code variable} (a scalar). */
        public double scalar(String variable) {
            try {
                Array a = require(variable).read();
                if (a.getSize() < 1) {
                    throw new RasterFormatException("Variable " + variable + " of " + name + " is empty");
                }
                return a.getDouble(0);
            } catch (IOException e) {
                throw new RasterFormatException("Cannot read " + variable + " of " + name + ": " + e.getMessage(), e);
            }
        }

        /**
         * The values of {@code variable} at the flat indices {@code indices}, in that order. The variable
         * is read once and dropped, so only one full variable is held at a time.
         */
        public float[] gather(String variable, int[] indices) {
            float[] all = floats(variable);
            float[] out = new float[indices.length];
            for (int k = 0; k < indices.length; k++) {
                int i = indices[k];
                if (i < 0 || i >= all.length) {
                    throw new RasterFormatException("Index " + i + " is outside " + variable + " (" + all.length + ")");
                }
                out[k] = all[i];
            }
            return out;
        }

        /** A string attribute of {@code variable}, or null. */
        public String attribute(String variable, String attribute) {
            return require(variable).findAttributeString(attribute, null);
        }

        /** A global string attribute, or null. */
        public String globalString(String attribute) {
            return NetcdfDecoder.globalString(ds, attribute);
        }

        /** A global ISO-8601 instant attribute ({@code time_coverage_start}), or null. */
        public Instant globalTime(String attribute) {
            return NetcdfDecoder.time(ds, attribute);
        }

        /**
         * The geostationary fixed grid of the two-dimensional {@code variable} in file order: column
         * {@code c} at {@code x[c]}, row {@code r} at {@code y[r]} (no reordering), from its
         * {@code grid_mapping} ({@code goes_imager_projection}) and its {@code x}/{@code y} coordinates.
         *
         * @throws SilentFormatChangeException when the projection or a coordinate is missing
         */
        public FixedGrid fixedGrid(String variable) {
            Variable v = require(variable);
            int[] shape = v.getShape();
            if (shape.length != 2) {
                throw new RasterFormatException("Variable " + variable + " has " + shape.length
                        + " dimensions; a fixed grid needs (y, x)");
            }
            DecodeRequest r = DecodeRequest.netcdf(productId, variable);
            try {
                String mappingName = v.findAttributeString("grid_mapping", "goes_imager_projection");
                Variable projection = requireVariable(ds, mappingName, r);
                double h = number(projection, "perspective_point_height", r);
                double a = number(projection, "semi_major_axis", r);
                double b = number(projection, "semi_minor_axis", r);
                double lon0 = number(projection, "longitude_of_projection_origin", r);
                String sweep = projection.findAttributeString("sweep_angle_axis", null);
                if (sweep == null) {
                    throw new SilentFormatChangeException(productId, "attribute sweep_angle_axis of " + mappingName
                            + " missing");
                }
                int ny = shape[0];
                int nx = shape[1];
                double[] x = coordinate(ds, v.getDimension(1).getShortName(), nx, r);
                double[] y = coordinate(ds, v.getDimension(0).getShortName(), ny, r);
                double dx = nx > 1 ? (x[nx - 1] - x[0]) / (nx - 1) : 1e-9;
                double dy = ny > 1 ? (y[ny - 1] - y[0]) / (ny - 1) : -1e-9;
                GeosGrid grid = new GeosGrid(nx, ny, lon0, h, a, b, !"y".equals(sweep.trim()), x[0], dx, y[0], dy,
                        new double[] {-180, -90, 180, 90});
                return new FixedGrid(grid);
            } catch (IOException e) {
                throw new RasterFormatException("Cannot read the grid of " + variable + " in " + name + ": "
                        + e.getMessage(), e);
            }
        }

        private Variable require(String variable) {
            Variable v = ds.findVariable(variable);
            if (v == null) {
                throw new SilentFormatChangeException(productId, missingVariable(variable));
            }
            return v;
        }

        /** Closes the file. */
        @Override
        public void close() {
            try {
                ds.close();
            } catch (IOException e) {
                // Nothing to recover: the file is read-only and deleted by its owner.
            }
        }
    }

    /**
     * A geostationary fixed grid in file order (prompt 048): pixel centers as longitude and latitude,
     * through {@link GeosGrid}.
     */
    public static final class FixedGrid {

        private final GeosGrid grid;

        FixedGrid(GeosGrid grid) {
            this.grid = grid;
        }

        /** Columns. */
        public int nx() {
            return grid.nx();
        }

        /** Rows. */
        public int ny() {
            return grid.ny();
        }

        /**
         * The center of pixel {@code (col, row)}.
         *
         * @return {@code {lon, lat}} in degrees, or {@code {NaN, NaN}} off the Earth
         */
        public double[] lonLat(int col, int row) {
            return grid.indexToLonLat(col, row);
        }

        /** The center of the pixel at flat index {@code index} (row-major, file order). */
        public double[] lonLat(int index) {
            return lonLat(index % grid.nx(), index / grid.nx());
        }
    }

    /** The health reason for a missing variable, for example "variable CMI missing". */
    static String missingVariable(String name) {
        return "variable " + name + " missing";
    }

    private static Variable requireVariable(NetcdfDataset ds, String name, DecodeRequest r) {
        Variable v = ds.findVariable(name);
        if (v == null) {
            throw new SilentFormatChangeException(r.productId(), missingVariable(name));
        }
        return v;
    }

    /**
     * The geostationary grid of {@code variable}.
     *
     * @param definition the grid definition, rows north to south
     * @param flipColumns true when the file's x runs east to west
     * @param flipRows true when the file's y runs south to north
     * @param projectionName the projection variable's name
     */
    record Geos(GridDefinition definition, boolean flipColumns, boolean flipRows, String projectionName) {}

    private static Geos geos(NetcdfDataset ds, Variable variable, int nx, int ny, DecodeRequest r) throws IOException {
        String mappingName = variable.findAttributeString("grid_mapping", "goes_imager_projection");
        Variable projection = requireVariable(ds, mappingName, r);
        String kind = projection.findAttributeString("grid_mapping_name", GEOSTATIONARY);
        if (!GEOSTATIONARY.equals(kind)) {
            throw new RasterFormatException("Projection " + mappingName + " is " + kind + "; only geostationary is supported");
        }
        double h = number(projection, "perspective_point_height", r);
        double a = number(projection, "semi_major_axis", r);
        double b = number(projection, "semi_minor_axis", r);
        double lon0 = number(projection, "longitude_of_projection_origin", r);
        String sweep = projection.findAttributeString("sweep_angle_axis", null);
        if (sweep == null) {
            throw new SilentFormatChangeException(r.productId(), "attribute sweep_angle_axis of " + mappingName + " missing");
        }
        String yName = variable.getDimension(0).getShortName();
        String xName = variable.getDimension(1).getShortName();
        double[] x = coordinate(ds, xName, nx, r);
        double[] y = coordinate(ds, yName, ny, r);
        boolean flipColumns = x[nx - 1] < x[0];
        boolean flipRows = y[ny - 1] > y[0];
        double westX = flipColumns ? x[nx - 1] : x[0];
        double northY = flipRows ? y[ny - 1] : y[0];
        double dx = nx > 1 ? Math.abs(x[nx - 1] - x[0]) / (nx - 1) : 0;
        double dy = ny > 1 ? -Math.abs(y[ny - 1] - y[0]) / (ny - 1) : 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("lon0", round(lon0));
        params.put("h", h);
        params.put("a", a);
        params.put("b", b);
        params.put("sweep", sweep.trim());
        params.put("x0", round(westX));
        params.put("dx", round(dx));
        params.put("y0", round(northY));
        params.put("dy", round(dy));
        GeosGrid grid = new GeosGrid(nx, ny, round(lon0), h, a, b, !"y".equals(sweep.trim()), round(westX), round(dx),
                round(northY), round(dy), new double[] {-180, -90, 180, 90});
        double[] box = outline(grid, nx, ny);
        GridDefinition definition = new GridDefinition(GridDefinition.GEOS, nx, ny, box[0], box[1], box[2], box[3], params);
        return new Geos(definition, flipColumns, flipRows, mappingName);
    }

    /**
     * The WGS84 box of the grid's outline: the cell edges along the four sides projected to
     * longitude and latitude, ignoring points beyond the visible disc.
     *
     * @throws RasterFormatException when no edge point is on the Earth
     */
    static double[] outline(GeosGrid grid, int nx, int ny) {
        double west = Double.POSITIVE_INFINITY;
        double east = Double.NEGATIVE_INFINITY;
        double south = Double.POSITIVE_INFINITY;
        double north = Double.NEGATIVE_INFINITY;
        int stepX = Math.max(1, nx / 256);
        int stepY = Math.max(1, ny / 256);
        java.util.List<double[]> edge = new java.util.ArrayList<>();
        for (int i = 0; i <= nx; i += stepX) {
            edge.add(new double[] {i - 0.5, -0.5});
            edge.add(new double[] {i - 0.5, ny - 0.5});
        }
        for (int j = 0; j <= ny; j += stepY) {
            edge.add(new double[] {-0.5, j - 0.5});
            edge.add(new double[] {nx - 0.5, j - 0.5});
        }
        edge.add(new double[] {nx - 0.5, ny - 0.5});
        for (double[] ij : edge) {
            double[] lonLat = grid.indexToLonLat(ij[0], ij[1]);
            if (Double.isNaN(lonLat[0]) || Double.isNaN(lonLat[1])) {
                continue;
            }
            west = Math.min(west, lonLat[0]);
            east = Math.max(east, lonLat[0]);
            south = Math.min(south, lonLat[1]);
            north = Math.max(north, lonLat[1]);
        }
        if (west > east) {
            throw new RasterFormatException("No edge of the geostationary grid lies on the Earth");
        }
        return new double[] {round6(Math.max(-180, west)), round6(Math.max(-90, south)), round6(Math.min(180, east)),
                round6(Math.min(90, north))};
    }

    private static double[] coordinate(NetcdfDataset ds, String name, int expected, DecodeRequest r) throws IOException {
        Variable v = requireVariable(ds, name, r);
        Array values = v.read();
        if (values.getSize() != expected) {
            throw new RasterFormatException("Coordinate " + name + " has " + values.getSize() + " values; expected " + expected);
        }
        return (double[]) values.get1DJavaArray(DataType.DOUBLE);
    }

    private static double number(Variable v, String attribute, DecodeRequest r) {
        Attribute a = v.findAttribute(attribute);
        if (a == null || a.getNumericValue() == null) {
            throw new SilentFormatChangeException(r.productId(), "attribute " + attribute + " of " + v.getShortName()
                    + " missing");
        }
        return a.getNumericValue().doubleValue();
    }

    private static float[] floats(Array array) {
        return (float[]) array.get1DJavaArray(DataType.FLOAT);
    }

    /** Flips columns and rows in place. */
    static void reorder(float[] data, int nx, int ny, boolean flipColumns, boolean flipRows) {
        int scanMode = (flipColumns ? Grib2Decoder.SCAN_I_NEGATIVE : 0) | (flipRows ? Grib2Decoder.SCAN_J_POSITIVE : 0);
        if (scanMode != 0) {
            Grib2Decoder.reorder(data, nx, ny, scanMode);
        }
    }

    private static Instant time(NetcdfDataset ds, String attribute) {
        String text = globalString(ds, attribute);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new RasterFormatException("Global attribute " + attribute + " is not an ISO instant: " + text, e);
        }
    }

    private static String globalString(NetcdfDataset ds, String name) {
        Attribute a = ds.findGlobalAttribute(name);
        return a == null || !a.isString() ? null : a.getStringValue();
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static double round(double v) {
        return Math.round(v * 1e9) / 1e9;
    }

    private static double round6(double v) {
        return Math.round(v * 1e6) / 1e6;
    }
}
