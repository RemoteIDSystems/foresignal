package ai.foresignal.doctor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The shape of one body, as {@link StructureExtractor} saw it: paths and their types, never the body itself.
 *
 * <p>Paths: JSON uses dots for members and {@code []} for array elements ({@code features[].properties.mag}); CSV and
 * TSV use the column names (or {@code col1}, {@code col2} … without a header); XML uses dots between element names and
 * {@code @} before an attribute ({@code feed.entry.link@href}).
 *
 * @param format the recognized format
 * @param contentType the declared content type, or null
 * @param bytes the body size in bytes
 * @param paths every path with its statistics, in document order
 * @param arrays JSON array paths (and {@code rows} for CSV/TSV) with their length statistics
 * @param columns CSV/TSV: the columns in order; empty otherwise
 * @param header CSV/TSV: true when the first row is a header
 * @param delimiter CSV/TSV: the delimiter; null otherwise
 * @param magic binary: the first bytes as hex; null otherwise
 * @param binaryKind binary: {@code zip}, {@code gzip}, {@code png}, {@code grib2}, {@code netcdf}, {@code hdf5},
 *     {@code pdf}, {@code jpeg}, {@code gif}, {@code protobuf-or-unknown}; null otherwise
 * @param zipEntries binary ZIP: up to 50 entry names
 * @param htmlTitle HTML: the {@code <title>}, capped; null otherwise
 * @param error why the body could not be read as its apparent format (a JSON or XML syntax error), or null
 */
public record Structure(
        StructureFormat format,
        String contentType,
        long bytes,
        Map<String, PathStats> paths,
        Map<String, ArrayStats> arrays,
        List<String> columns,
        boolean header,
        String delimiter,
        String magic,
        String binaryKind,
        List<String> zipEntries,
        String htmlTitle,
        String error) {

    /** Copies the collections. */
    public Structure {
        paths = paths == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(paths));
        arrays = arrays == null ? Map.of()
                : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(arrays));
        columns = columns == null ? List.of() : List.copyOf(columns);
        zipEntries = zipEntries == null ? List.of() : List.copyOf(zipEntries);
    }

    /** The media type of {@link #contentType()} without parameters, lowercase; null when there is none. */
    public String mediaType() {
        return StructureExtractor.mediaType(contentType);
    }

    /**
     * What one path looks like.
     *
     * @param types the JSON types seen ({@code object}, {@code array}, {@code string}, {@code number}, {@code boolean},
     *     {@code null}); for CSV the inferred column types ({@code integer}, {@code number}, {@code boolean},
     *     {@code date}, {@code string}); for XML {@code element} or {@code attribute} plus the text's type
     * @param presence the share of the parent's instances that carry the path, 0..1
     * @param examples up to 3 distinct example values, each at most 80 characters
     * @param min the smallest numeric value seen, or null
     * @param max the largest numeric value seen, or null
     * @param nullRatio CSV: the share of empty cells; JSON: the share of {@code null} values; 0 otherwise
     */
    public record PathStats(Set<String> types, double presence, List<String> examples, Double min, Double max,
            double nullRatio) {

        /** Copies the collections. */
        public PathStats {
            types = types == null ? Set.of() : java.util.Collections.unmodifiableSet(new java.util.TreeSet<>(types));
            examples = examples == null ? List.of() : List.copyOf(examples);
        }

        /** The types other than {@code null}. */
        public Set<String> nonNullTypes() {
            java.util.Set<String> out = new java.util.TreeSet<>(types);
            out.remove("null");
            return out;
        }
    }

    /**
     * Length statistics of one array path.
     *
     * @param instances how many arrays were seen at the path
     * @param minLength the shortest
     * @param maxLength the longest
     * @param meanLength the mean length
     */
    public record ArrayStats(int instances, int minLength, int maxLength, double meanLength) {}
}
