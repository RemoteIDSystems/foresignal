package ai.foresignal.doctor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Extracts the {@link Structure} of a body by content type or by sniffing (prompt 140, task 2). Pure: no clock, no
 * network, no state.
 *
 * <ul>
 *   <li><b>JSON:</b> a path tree with {@code []} for arrays; per path the JSON types seen, the presence ratio across
 *       the parent's instances (array elements), up to 3 examples (numbers kept, strings truncated to 80 characters)
 *       and numeric ranges; per array path the length statistics.
 *   <li><b>CSV/TSV:</b> the delimiter, the header columns (or {@code colN}), per column the inferred type and the null
 *       ratio.
 *   <li><b>XML:</b> element and attribute paths with counts, parsed by a DOM factory with DTDs, external entities and
 *       XInclude disabled (a document with a DOCTYPE is rejected, which is how XXE attempts end).
 *   <li><b>Binary</b> (GRIB2, NetCDF, PNG, ZIP, gzip …): content type, size, magic bytes and ZIP entry names.
 *   <li><b>HTML:</b> recognized and its {@code <title>} captured, so HTML arriving where data was expected can be flagged
 *       ({@code HTML_INSTEAD_OF_DATA}).
 * </ul>
 */
public final class StructureExtractor {

    /** The most paths kept per body; deeper or wider documents are cut here. */
    public static final int MAX_PATHS = 2_000;

    /** The most examples kept per path. */
    public static final int MAX_EXAMPLES = 3;

    /** The longest example kept, in characters. */
    public static final int MAX_EXAMPLE_LENGTH = 80;

    /** The most CSV rows read. */
    public static final int MAX_CSV_ROWS = 5_000;

    /** The most ZIP entry names kept. */
    public static final int MAX_ZIP_ENTRIES = 50;

    private static final Pattern TITLE = Pattern.compile("<title[^>]*>(.*?)</title>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INTEGER = Pattern.compile("[-+]?\\d+");
    private static final Pattern NUMBER = Pattern.compile("[-+]?(\\d+\\.?\\d*|\\.\\d+)([eE][-+]?\\d+)?");
    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}([T ][0-9:.]+(Z|[-+]\\d{2}:?\\d{2})?)?");

    private final JsonMapper json;

    /** An extractor with its own JSON mapper. */
    public StructureExtractor() {
        this(JsonMapper.builder().build());
    }

    /** An extractor using {@code json}. */
    public StructureExtractor(JsonMapper json) {
        this.json = json;
    }

    /**
     * Extracts the structure of {@code body}.
     *
     * @param body the bytes, possibly empty
     * @param contentType the declared content type, or null
     */
    public Structure extract(byte[] body, String contentType) {
        byte[] bytes = body == null ? new byte[0] : body;
        StructureFormat format = sniff(bytes, contentType);
        return switch (format) {
            case JSON -> json(bytes, contentType);
            case CSV, TSV -> delimited(new String(bytes, StandardCharsets.UTF_8), contentType, bytes.length);
            case XML -> xml(bytes, contentType);
            case BINARY -> binary(bytes, contentType);
            case HTML -> html(new String(bytes, StandardCharsets.UTF_8), contentType, bytes.length);
            case TEXT -> new Structure(StructureFormat.TEXT, contentType, bytes.length, Map.of(), Map.of(), List.of(),
                    false, null, null, null, List.of(), null, null);
        };
    }

    /** Extracts the structure of a body that was already decoded to text (a probe body). */
    public Structure extract(String body, String contentType) {
        return extract(body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    // ---------- sniffing ----------

    /** The media type of a content type without parameters, lowercase; null for null or blank. */
    static String mediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        int semi = contentType.indexOf(';');
        return (semi < 0 ? contentType : contentType.substring(0, semi)).strip().toLowerCase(Locale.ROOT);
    }

    /** The format of {@code bytes}: magic bytes first, then the content type, then the first characters. */
    static StructureFormat sniff(byte[] bytes, String contentType) {
        if (binaryKind(bytes) != null) {
            return StructureFormat.BINARY;
        }
        String media = mediaType(contentType);
        String head = new String(bytes, 0, Math.min(bytes.length, 2048), StandardCharsets.UTF_8).stripLeading();
        if (head.startsWith("﻿")) {
            head = head.substring(1).stripLeading();
        }
        String lower = head.toLowerCase(Locale.ROOT);
        if (lower.startsWith("<!doctype html") || lower.startsWith("<html") || (media != null
                && media.equals("text/html") && lower.startsWith("<"))) {
            return StructureFormat.HTML;
        }
        if (head.startsWith("{") || head.startsWith("[")) {
            return StructureFormat.JSON;
        }
        if (head.startsWith("<")) {
            return lower.contains("<html") || lower.contains("<body") ? StructureFormat.HTML : StructureFormat.XML;
        }
        if (media != null && (media.contains("octet-stream") || media.startsWith("image/") || media.contains("zip")
                || media.contains("netcdf") || media.contains("grib"))) {
            return StructureFormat.BINARY;
        }
        if (containsNul(bytes)) {
            return StructureFormat.BINARY;
        }
        if (media != null && media.contains("tab-separated")) {
            return StructureFormat.TSV;
        }
        Character delimiter = delimiter(head);
        if (delimiter != null) {
            return delimiter == '\t' ? StructureFormat.TSV : StructureFormat.CSV;
        }
        return StructureFormat.TEXT;
    }

    private static boolean containsNul(byte[] bytes) {
        for (int i = 0; i < Math.min(bytes.length, 1024); i++) {
            if (bytes[i] == 0) {
                return true;
            }
        }
        return false;
    }

    /** The binary kind recognized by magic bytes, or null. */
    static String binaryKind(byte[] b) {
        if (startsWith(b, 'P', 'K', 3, 4) || startsWith(b, 'P', 'K', 5, 6)) {
            return "zip";
        }
        if (startsWith(b, 0x1f, 0x8b)) {
            return "gzip";
        }
        if (startsWith(b, 0x89, 'P', 'N', 'G')) {
            return "png";
        }
        if (startsWith(b, 'G', 'R', 'I', 'B')) {
            return "grib2";
        }
        if (startsWith(b, 'C', 'D', 'F', 1) || startsWith(b, 'C', 'D', 'F', 2)) {
            return "netcdf";
        }
        if (startsWith(b, 0x89, 'H', 'D', 'F')) {
            return "hdf5";
        }
        if (startsWith(b, '%', 'P', 'D', 'F')) {
            return "pdf";
        }
        if (startsWith(b, 0xff, 0xd8, 0xff)) {
            return "jpeg";
        }
        if (startsWith(b, 'G', 'I', 'F', '8')) {
            return "gif";
        }
        return null;
    }

    private static boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((bytes[i] & 0xff) != (prefix[i] & 0xff)) {
                return false;
            }
        }
        return true;
    }

    /** The delimiter of a delimited text (tab, comma, semicolon or pipe), when the first lines agree on one. */
    static Character delimiter(String text) {
        String[] lines = text.split("\r?\n", 12);
        List<String> sample = new ArrayList<>();
        for (int i = 0; i < lines.length && sample.size() < 10; i++) {
            if (i == lines.length - 1 && lines.length == 12) {
                break;
            }
            if (!lines[i].isBlank()) {
                sample.add(lines[i]);
            }
        }
        if (sample.isEmpty()) {
            return null;
        }
        for (char candidate : new char[] {'\t', ',', ';', '|'}) {
            int first = count(sample.get(0), candidate);
            if (first == 0) {
                continue;
            }
            boolean consistent = true;
            for (String line : sample) {
                if (count(line, candidate) != first) {
                    consistent = false;
                    break;
                }
            }
            if (consistent) {
                return candidate;
            }
        }
        return null;
    }

    private static int count(String line, char c) {
        int n = 0;
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                quoted = !quoted;
            } else if (ch == c && !quoted) {
                n++;
            }
        }
        return n;
    }

    // ---------- JSON ----------

    private Structure json(byte[] bytes, String contentType) {
        JsonNode root;
        try {
            root = json.readTree(bytes);
        } catch (JacksonException e) {
            return new Structure(StructureFormat.JSON, contentType, bytes.length, Map.of(), Map.of(), List.of(), false,
                    null, null, null, List.of(), null, "not valid JSON: " + cap(e.getOriginalMessage(), 200));
        }
        JsonAccumulator acc = new JsonAccumulator();
        acc.visit(root, "", "");
        return new Structure(StructureFormat.JSON, contentType, bytes.length, acc.paths(), acc.arrays(), List.of(),
                false, null, null, null, List.of(), null, null);
    }

    /** Joins a parent path and a member name. */
    static String child(String parent, String name) {
        return parent.isEmpty() ? name : parent + "." + name;
    }

    /** Walks a JSON tree, counting occurrences per path and per container. */
    private static final class JsonAccumulator {

        private final Map<String, Integer> occurrences = new LinkedHashMap<>();
        private final Map<String, String> parents = new HashMap<>();
        private final Map<String, Set<String>> types = new HashMap<>();
        private final Map<String, Set<String>> examples = new HashMap<>();
        private final Map<String, double[]> ranges = new HashMap<>();
        private final Map<String, Integer> nulls = new HashMap<>();
        private final Map<String, int[]> arrayLengths = new LinkedHashMap<>();

        void visit(JsonNode node, String path, String parent) {
            if (!occurrences.containsKey(path) && occurrences.size() >= MAX_PATHS) {
                return;
            }
            occurrences.merge(path, 1, Integer::sum);
            parents.putIfAbsent(path, parent);
            String type = typeOf(node);
            types.computeIfAbsent(path, k -> new TreeSet<>()).add(type);
            if (node.isNull()) {
                nulls.merge(path, 1, Integer::sum);
            }
            if (node.isObject()) {
                for (Map.Entry<String, JsonNode> e : node.properties()) {
                    visit(e.getValue(), child(path, e.getKey()), path);
                }
            } else if (node.isArray()) {
                int size = node.size();
                int[] stats = arrayLengths.computeIfAbsent(path, k -> new int[] {0, Integer.MAX_VALUE, 0, 0});
                stats[0]++;
                stats[1] = Math.min(stats[1], size);
                stats[2] = Math.max(stats[2], size);
                stats[3] += size;
                String element = path + "[]";
                for (JsonNode item : node) {
                    visit(item, element, path);
                }
            } else if (!node.isNull()) {
                example(path, node);
            }
        }

        private void example(String path, JsonNode node) {
            if (node.isNumber()) {
                double v = node.asDouble();
                double[] r = ranges.computeIfAbsent(path, k -> new double[] {v, v});
                r[0] = Math.min(r[0], v);
                r[1] = Math.max(r[1], v);
            }
            Set<String> ex = examples.computeIfAbsent(path, k -> new LinkedHashSet<>());
            if (ex.size() < MAX_EXAMPLES) {
                ex.add(cap(node.isString() ? node.asString() : node.toString(), MAX_EXAMPLE_LENGTH));
            }
        }

        Map<String, Structure.PathStats> paths() {
            Map<String, Structure.PathStats> out = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : occurrences.entrySet()) {
                String path = e.getKey();
                if (path.isEmpty()) {
                    continue;
                }
                String parent = parents.get(path);
                double presence;
                if (path.endsWith("[]")) {
                    presence = 1.0;
                } else {
                    // Members of array elements are counted against the elements, members of objects against the
                    // object's occurrences (the root occurs once).
                    int parentCount = occurrences.getOrDefault(parent, 1);
                    presence = Math.min(1.0, e.getValue() / (double) Math.max(1, parentCount));
                }
                double[] r = ranges.get(path);
                double nullRatio = nulls.getOrDefault(path, 0) / (double) e.getValue();
                out.put(path, new Structure.PathStats(types.get(path),
                        presence, List.copyOf(examples.getOrDefault(path, Set.of())),
                        r == null ? null : r[0], r == null ? null : r[1], nullRatio));
            }
            return out;
        }

        Map<String, Structure.ArrayStats> arrays() {
            Map<String, Structure.ArrayStats> out = new LinkedHashMap<>();
            arrayLengths.forEach((path, s) -> out.put(path.isEmpty() ? "[]" : path,
                    new Structure.ArrayStats(s[0], s[1] == Integer.MAX_VALUE ? 0 : s[1], s[2],
                            s[0] == 0 ? 0 : s[3] / (double) s[0])));
            return out;
        }

        private static String typeOf(JsonNode node) {
            if (node.isObject()) {
                return "object";
            }
            if (node.isArray()) {
                return "array";
            }
            if (node.isNumber()) {
                return "number";
            }
            if (node.isBoolean()) {
                return "boolean";
            }
            if (node.isNull()) {
                return "null";
            }
            return "string";
        }
    }

    // ---------- CSV / TSV ----------

    private static Structure delimited(String text, String contentType, long bytes) {
        Character d = delimiter(text);
        char delimiter = d == null ? (mediaType(contentType) != null
                && mediaType(contentType).contains("tab-separated") ? '\t' : ',') : d;
        List<List<String>> rows = new ArrayList<>();
        for (String line : text.split("\r?\n")) {
            if (line.isBlank()) {
                continue;
            }
            rows.add(split(line, delimiter));
            if (rows.size() > MAX_CSV_ROWS) {
                break;
            }
        }
        StructureFormat format = delimiter == '\t' ? StructureFormat.TSV : StructureFormat.CSV;
        if (rows.isEmpty()) {
            return new Structure(format, contentType, bytes, Map.of(), Map.of("rows", new Structure.ArrayStats(1, 0, 0,
                    0)), List.of(), false, String.valueOf(delimiter), null, null, List.of(), null, null);
        }
        boolean header = looksLikeHeader(rows);
        int width = 0;
        for (List<String> row : rows) {
            width = Math.max(width, row.size());
        }
        List<String> columns = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            String name = header && i < rows.get(0).size() ? rows.get(0).get(i).strip() : "";
            columns.add(name.isEmpty() ? "col" + (i + 1) : name);
        }
        List<List<String>> data = header ? rows.subList(1, rows.size()) : rows;
        Map<String, Structure.PathStats> paths = new LinkedHashMap<>();
        for (int c = 0; c < width; c++) {
            Set<String> types = new TreeSet<>();
            Set<String> examples = new LinkedHashSet<>();
            int empty = 0;
            double min = Double.NaN;
            double max = Double.NaN;
            for (List<String> row : data) {
                String cell = c < row.size() ? row.get(c).strip() : "";
                if (cell.isEmpty()) {
                    empty++;
                    continue;
                }
                String type = cellType(cell);
                types.add(type);
                if (type.equals("integer") || type.equals("number")) {
                    double v = Double.parseDouble(cell);
                    min = Double.isNaN(min) ? v : Math.min(min, v);
                    max = Double.isNaN(max) ? v : Math.max(max, v);
                }
                if (examples.size() < MAX_EXAMPLES) {
                    examples.add(cap(cell, MAX_EXAMPLE_LENGTH));
                }
            }
            if (types.contains("number")) {
                types.remove("integer");
            }
            double nullRatio = data.isEmpty() ? 0 : empty / (double) data.size();
            paths.put(columns.get(c), new Structure.PathStats(types, data.isEmpty() ? 1.0 : 1.0 - nullRatio,
                    List.copyOf(examples), Double.isNaN(min) ? null : min, Double.isNaN(max) ? null : max, nullRatio));
        }
        Map<String, Structure.ArrayStats> arrays = Map.of("rows",
                new Structure.ArrayStats(1, data.size(), data.size(), data.size()));
        return new Structure(format, contentType, bytes, paths, arrays, columns, header, String.valueOf(delimiter),
                null, null, List.of(), null, null);
    }

    /** True when the first row has no numeric cell while a later row does, or every first-row cell is a distinct word. */
    private static boolean looksLikeHeader(List<List<String>> rows) {
        List<String> first = rows.get(0);
        boolean firstNumeric = first.stream().map(String::strip).anyMatch(s -> NUMBER.matcher(s).matches());
        if (firstNumeric) {
            return false;
        }
        if (rows.size() > 1) {
            boolean laterNumeric = rows.get(1).stream().map(String::strip).anyMatch(s -> NUMBER.matcher(s).matches());
            if (laterNumeric) {
                return true;
            }
        }
        return new LinkedHashSet<>(first).size() == first.size() && first.stream().noneMatch(String::isBlank);
    }

    /** The inferred type of one non-empty cell. */
    static String cellType(String cell) {
        if (INTEGER.matcher(cell).matches()) {
            return "integer";
        }
        if (NUMBER.matcher(cell).matches()) {
            return "number";
        }
        String lower = cell.toLowerCase(Locale.ROOT);
        if (lower.equals("true") || lower.equals("false")) {
            return "boolean";
        }
        if (DATE.matcher(cell).matches()) {
            return "date";
        }
        return "string";
    }

    /** Splits one line on {@code delimiter}, honoring double quotes. */
    static List<String> split(String line, char delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == delimiter && !quoted) {
                out.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(ch);
            }
        }
        out.add(cell.toString());
        return out;
    }

    // ---------- XML ----------

    /** A DOM factory with DTDs, external entities and XInclude off and secure processing on. */
    static DocumentBuilderFactory secureFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);
        return factory;
    }

    private static Structure xml(byte[] bytes, String contentType) {
        Document document;
        try {
            DocumentBuilder builder = secureFactory().newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override
                public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
                    throw e;
                }
            });
            document = builder.parse(new ByteArrayInputStream(bytes));
        } catch (ParserConfigurationException | org.xml.sax.SAXException | IOException e) {
            return new Structure(StructureFormat.XML, contentType, bytes.length, Map.of(), Map.of(), List.of(), false,
                    null, null, null, List.of(), null, "not readable XML (DTDs and external entities are refused): "
                            + cap(e.getMessage(), 200));
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> parents = new HashMap<>();
        Map<String, Set<String>> types = new HashMap<>();
        Map<String, Set<String>> examples = new HashMap<>();
        walk(document.getDocumentElement(), "", counts, parents, types, examples);
        Map<String, Structure.PathStats> paths = new LinkedHashMap<>();
        counts.forEach((path, n) -> {
            String parent = parents.get(path);
            int parentCount = parent == null || parent.isEmpty() ? 1 : counts.getOrDefault(parent, 1);
            double presence = parent == null || parent.isEmpty() ? 1.0 : Math.min(1.0, n / (double) parentCount);
            paths.put(path, new Structure.PathStats(types.get(path), presence,
                    List.copyOf(examples.getOrDefault(path, Set.of())), null, null, 0));
        });
        return new Structure(StructureFormat.XML, contentType, bytes.length, paths, Map.of(), List.of(), false, null,
                null, null, List.of(), null, null);
    }

    private static void walk(Element element, String parent, Map<String, Integer> counts, Map<String, String> parents,
            Map<String, Set<String>> types, Map<String, Set<String>> examples) {
        String path = child(parent, element.getTagName());
        if (!counts.containsKey(path) && counts.size() >= MAX_PATHS) {
            return;
        }
        counts.merge(path, 1, Integer::sum);
        parents.putIfAbsent(path, parent);
        Set<String> t = types.computeIfAbsent(path, k -> new TreeSet<>());
        t.add("element");
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String attrPath = path + "@" + attr.getName();
            counts.merge(attrPath, 1, Integer::sum);
            parents.putIfAbsent(attrPath, path);
            Set<String> at = types.computeIfAbsent(attrPath, k -> new TreeSet<>());
            at.add("attribute");
            at.add(cellType(attr.getValue().strip().isEmpty() ? "x" : attr.getValue().strip()));
            addExample(examples, attrPath, attr.getValue());
        }
        NodeList children = element.getChildNodes();
        boolean hasElements = false;
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element e) {
                hasElements = true;
                walk(e, path, counts, parents, types, examples);
            } else if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(node.getNodeValue());
            }
        }
        String value = text.toString().strip();
        if (!hasElements && !value.isEmpty()) {
            t.add(cellType(value));
            addExample(examples, path, value);
        }
    }

    private static void addExample(Map<String, Set<String>> examples, String path, String value) {
        Set<String> ex = examples.computeIfAbsent(path, k -> new LinkedHashSet<>());
        if (ex.size() < MAX_EXAMPLES && value != null && !value.isBlank()) {
            ex.add(cap(value.strip(), MAX_EXAMPLE_LENGTH));
        }
    }

    // ---------- binary ----------

    private static Structure binary(byte[] bytes, String contentType) {
        StringBuilder magic = new StringBuilder();
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            magic.append(String.format(Locale.ROOT, "%02x", bytes[i] & 0xff));
        }
        String kind = binaryKind(bytes);
        List<String> entries = new ArrayList<>();
        if ("zip".equals(kind)) {
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null && entries.size() < MAX_ZIP_ENTRIES) {
                    entries.add(cap(entry.getName(), 200));
                }
            } catch (IOException | IllegalArgumentException e) {
                // A truncated archive still yields the entries read so far.
            }
        }
        Map<String, Structure.PathStats> paths = new LinkedHashMap<>();
        for (String entry : entries) {
            paths.put(entry, new Structure.PathStats(Set.of("zip-entry"), 1.0, List.of(), null, null, 0));
        }
        return new Structure(StructureFormat.BINARY, contentType, bytes.length, paths, Map.of(), List.of(), false, null,
                magic.toString(), kind == null ? "protobuf-or-unknown" : kind, entries, null, null);
    }

    // ---------- HTML ----------

    private static Structure html(String text, String contentType, long bytes) {
        return new Structure(StructureFormat.HTML, contentType, bytes, Map.of(), Map.of(), List.of(), false, null,
                null, null, List.of(), htmlTitle(text), null);
    }

    /** The {@code <title>} of an HTML page, whitespace collapsed and capped at 200 characters; null when absent. */
    static String htmlTitle(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = TITLE.matcher(text.length() > 65_536 ? text.substring(0, 65_536) : text);
        if (!m.find()) {
            return null;
        }
        String title = m.group(1).replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").strip();
        return title.isEmpty() ? null : cap(title, 200);
    }

    /** {@code value} cut to {@code max} characters, with an ellipsis when cut. */
    static String cap(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }
}
