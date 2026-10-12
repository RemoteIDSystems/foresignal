package ai.foresignal.testsupport.openapi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The authored contract {@code docs/OpenAPI.json} and the pending list
 * {@code src/test/resources/openapi/pending-operations.txt}, as the OpenAPI conformance tests of
 * prompt 020 read them.
 *
 * <p>Files are read from the repository by path, relative to the project directory (Gradle's test
 * working directory), so the tests check the document that is committed, not a copy.
 */
public final class OpenApiSpec {

    /** The authored document, relative to the project directory. */
    public static final Path DOCUMENT = Path.of("docs", "OpenAPI.json");

    /** The pending list, relative to the project directory. */
    public static final Path PENDING = Path.of("src", "test", "resources", "openapi", "pending-operations.txt");

    /** The generated Postman collection, relative to the project directory. */
    public static final Path POSTMAN = Path.of("postman", "Foresignal.postman_collection.json");

    /** Completion Reports, whose highest number is the last completed prompt. */
    public static final Path COMPLETION_REPORTS = Path.of("completion-reports");

    /** The prompt that created the pending list; the current prompt is never lower. */
    public static final int CONFORMANCE_PROMPT = 20;

    /** HTTP methods an OpenAPI path item can hold, in document order. */
    public static final List<String> METHODS =
            List.of("get", "put", "post", "delete", "options", "head", "patch", "trace");

    /** A JSON mapper for reading documents. */
    public static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{[^/}]+}");
    private static final Pattern REPORT = Pattern.compile("^(\\d{3})-CR-.*\\.md$");
    private static final Pattern PENDING_LINE = Pattern.compile("^([A-Za-z][A-Za-z0-9_]*) (\\d{3})$");

    private static volatile JsonNode document;

    private OpenApiSpec() {}

    /**
     * One operation of the document.
     *
     * @param section {@code paths} or {@code webhooks}
     * @param path the path template, or the webhook name
     * @param method the lowercase HTTP method
     * @param operationId the operation id
     * @param node the operation object
     */
    public record SpecOperation(String section, String path, String method, String operationId, JsonNode node) {

        /** {@code x-foresignal-scope}, or null. */
        public String scope() {
            return text("x-foresignal-scope");
        }

        /** {@code x-foresignal-prompt}, or null. */
        public String prompt() {
            return text("x-foresignal-prompt");
        }

        /** {@code x-foresignal-prompt} as a number, or -1 when it is missing or not a number. */
        public int promptNumber() {
            String prompt = prompt();
            return prompt != null && prompt.matches("\\d{3}") ? Integer.parseInt(prompt) : -1;
        }

        /** {@code x-foresignal-status}, or null. */
        public String status() {
            return text("x-foresignal-status");
        }

        /** True when the status is {@code implemented}. */
        public boolean implemented() {
            return "implemented".equals(status());
        }

        /** True for an operation under {@code paths} (a route), false for a webhook. */
        public boolean isRoute() {
            return "paths".equals(section);
        }

        /** {@code METHOD path}, for messages. */
        public String label() {
            return method.toUpperCase() + " " + path + " (" + operationId + ")";
        }

        private String text(String field) {
            JsonNode value = node.get(field);
            return value == null || value.isNull() ? null : value.asString();
        }
    }

    /** The authored document, parsed once. */
    public static JsonNode document() {
        JsonNode result = document;
        if (result == null) {
            result = JSON.readTree(new String(bytes(), StandardCharsets.UTF_8));
            document = result;
        }
        return result;
    }

    /** The authored document's bytes, exactly as committed. */
    public static byte[] bytes() {
        try {
            return Files.readAllBytes(DOCUMENT);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + DOCUMENT.toAbsolutePath(), e);
        }
    }

    /** Every operation under {@code paths}, in document order. */
    public static List<SpecOperation> routes() {
        return operations(document(), "paths");
    }

    /** Every operation under {@code webhooks}, in document order. */
    public static List<SpecOperation> webhooks() {
        return operations(document(), "webhooks");
    }

    /** Routes followed by webhooks. */
    public static List<SpecOperation> allOperations() {
        List<SpecOperation> all = new ArrayList<>(routes());
        all.addAll(webhooks());
        return all;
    }

    /**
     * The operations under {@code section} ({@code paths} or {@code webhooks}) of {@code doc}.
     *
     * @param doc an OpenAPI document (authored or generated)
     * @param section the top-level member to read
     */
    public static List<SpecOperation> operations(JsonNode doc, String section) {
        List<SpecOperation> out = new ArrayList<>();
        JsonNode items = doc.get(section);
        if (items == null) {
            return out;
        }
        for (String path : items.propertyNames()) {
            JsonNode item = items.get(path);
            for (String method : METHODS) {
                JsonNode op = item.get(method);
                if (op != null && op.isObject()) {
                    JsonNode id = op.get("operationId");
                    out.add(new SpecOperation(section, path, method, id == null ? null : id.asString(), op));
                }
            }
        }
        return out;
    }

    /** {@code path} with every path variable replaced by {@code {}}, so variable names never matter. */
    public static String normalizePath(String path) {
        return PATH_VARIABLE.matcher(path).replaceAll("{}");
    }

    /** The {@code (normalized path, METHOD)} key of an operation. */
    public static String routeKey(String path, String method) {
        return method.toUpperCase() + " " + normalizePath(path);
    }

    /** The 2xx status codes an operation documents (for example {@code 200}, {@code 204}, {@code 2XX}). */
    public static Set<String> successCodes(JsonNode operation) {
        Set<String> codes = new java.util.TreeSet<>();
        JsonNode responses = operation.get("responses");
        if (responses != null) {
            for (String code : responses.propertyNames()) {
                if (code.startsWith("2")) {
                    codes.add(code);
                }
            }
        }
        return codes;
    }

    /**
     * The pending list: operationId to the prompt that implements it, in file order.
     *
     * @throws IllegalStateException on a malformed or duplicated line, naming it
     */
    public static Map<String, Integer> pending() {
        List<String> lines;
        try {
            lines = Files.readAllLines(PENDING, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + PENDING.toAbsolutePath(), e);
        }
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty()) {
                continue;
            }
            Matcher m = PENDING_LINE.matcher(line);
            if (!m.matches()) {
                throw new IllegalStateException(PENDING + ":" + (i + 1) + ": expected '<operationId> <NNN>', got '" + line + "'");
            }
            if (out.put(m.group(1), Integer.parseInt(m.group(2))) != null) {
                throw new IllegalStateException(PENDING + ":" + (i + 1) + ": duplicate operation " + m.group(1));
            }
        }
        return Collections.unmodifiableMap(out);
    }

    /**
     * The prompt the build has reached: the highest Completion Report number, and never less than
     * {@value #CONFORMANCE_PROMPT}. A {@code planned} operation owned by a prompt at or below it is a
     * defect: implement it or re-assign it.
     */
    public static int currentPrompt() {
        int highest = CONFORMANCE_PROMPT;
        if (Files.isDirectory(COMPLETION_REPORTS)) {
            try (Stream<Path> files = Files.list(COMPLETION_REPORTS)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    Matcher m = REPORT.matcher(file.getFileName().toString());
                    if (m.matches()) {
                        highest = Math.max(highest, Integer.parseInt(m.group(1)));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return highest;
    }

    /** Reads a JSON file of the repository. */
    public static JsonNode readJson(Path file) {
        try {
            return JSON.readTree(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file.toAbsolutePath(), e);
        }
    }
}
