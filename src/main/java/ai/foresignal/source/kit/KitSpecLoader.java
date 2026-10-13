package ai.foresignal.source.kit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads kit spec files (prompt 073): each file is a JSON array of specs, bound to a record type with unknown
 * properties refused, so a misspelled key fails startup instead of being silently ignored.
 *
 * <p>Problems (an unreadable file, a document that is not an array, an element that does not bind) are collected,
 * never thrown one at a time, so the family can stop startup with every problem listed.
 */
public final class KitSpecLoader {

    /** The mapper of spec files: unknown properties are refused. */
    public static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    /** The largest spec file read, in bytes. */
    public static final int MAX_FILE_BYTES = 4 * 1024 * 1024;

    /**
     * One spec and where it came from.
     *
     * @param origin the file name, for problem messages
     * @param index the position in the file's array
     * @param overlay true for an operator overlay file
     * @param spec the bound spec
     * @param <T> the spec type
     */
    public record Loaded<T>(String origin, int index, boolean overlay, T spec) {

        /** The prefix of a problem message about this spec: {@code file[index]}. */
        public String where() {
            return origin + "[" + index + "]";
        }
    }

    private KitSpecLoader() {}

    /**
     * Reads every {@code *.json} file of a classpath folder, in file-name order.
     *
     * @param folder the classpath folder, for example {@code kits/features}
     * @param type the spec record type
     * @param problems receives every problem found
     * @return the specs that bound
     */
    public static <T> List<Loaded<T>> classpath(String folder, Class<T> type, List<String> problems) {
        List<Loaded<T>> out = new ArrayList<>();
        Resource[] resources;
        try {
            resources = new PathMatchingResourcePatternResolver(KitSpecLoader.class.getClassLoader())
                    .getResources("classpath*:" + folder + "/*.json");
        } catch (IOException e) {
            problems.add(folder + ": the spec files could not be listed: " + e.getMessage());
            return out;
        }
        Arrays.sort(resources, Comparator.comparing(r -> String.valueOf(r.getFilename())));
        for (Resource resource : resources) {
            String name = folder + "/" + resource.getFilename();
            try (InputStream in = resource.getInputStream()) {
                read(name, in.readNBytes(MAX_FILE_BYTES + 1), type, false, out, problems);
            } catch (IOException e) {
                problems.add(name + ": could not be read: " + e.getMessage());
            }
        }
        return out;
    }

    /**
     * Reads every {@code *.json} file of an operator overlay directory, in file-name order. A missing directory
     * holds no specs.
     *
     * @param dir the directory
     * @param type the spec record type
     * @param problems receives every problem found
     * @return the specs that bound, marked as overlay specs
     */
    public static <T> List<Loaded<T>> directory(Path dir, Class<T> type, List<String> problems) {
        List<Loaded<T>> out = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) {
            return out;
        }
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            stream.forEach(files::add);
        } catch (IOException e) {
            problems.add(dir + ": the overlay spec files could not be listed: " + e.getMessage());
            return out;
        }
        files.sort(Comparator.comparing(p -> p.getFileName().toString()));
        for (Path file : files) {
            if (!Files.isRegularFile(file) || Files.isSymbolicLink(file)) {
                problems.add(file + ": an overlay spec must be a regular file, not a link or directory");
                continue;
            }
            try (InputStream in = Files.newInputStream(file)) {
                read(file.toString(), in.readNBytes(MAX_FILE_BYTES + 1), type, true, out, problems);
            } catch (IOException e) {
                problems.add(file + ": could not be read: " + e.getMessage());
            }
        }
        return out;
    }

    /**
     * Binds one file's array.
     *
     * @param origin the file name, for messages
     * @param bytes the file content
     * @param type the spec record type
     * @param overlay true for an operator overlay file
     * @param out receives the bound specs
     * @param problems receives every problem found
     */
    public static <T> void read(String origin, byte[] bytes, Class<T> type, boolean overlay, List<Loaded<T>> out,
            List<String> problems) {
        if (bytes.length > MAX_FILE_BYTES) {
            problems.add(origin + ": larger than " + MAX_FILE_BYTES + " bytes");
            return;
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(bytes);
        } catch (JacksonException e) {
            problems.add(origin + ": not valid JSON: " + e.getOriginalMessage());
            return;
        }
        if (root == null || !root.isArray()) {
            problems.add(origin + ": must be a JSON array of specs");
            return;
        }
        int index = 0;
        for (JsonNode element : root) {
            try {
                T spec = MAPPER.treeToValue(element, type);
                if (spec == null) {
                    problems.add(origin + "[" + index + "]: is null");
                } else {
                    out.add(new Loaded<>(origin, index, overlay, spec));
                }
            } catch (JacksonException e) {
                problems.add(origin + "[" + index + "]: does not bind: " + e.getOriginalMessage());
            }
            index++;
        }
    }
}
