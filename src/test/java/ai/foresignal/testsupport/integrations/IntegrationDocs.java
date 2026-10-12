package ai.foresignal.testsupport.integrations;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.security.Scope;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Reads the agent and automation integration material of prompt 034 the way its drift tests check
 * it: the OpenClaw skill, the cookbooks under {@code docs/cookbooks}, the importable artifacts under
 * {@code integrations} and {@code docs/Agent-Guide.md}.
 *
 * <p>Files are read by path relative to the project directory (Gradle's test working directory), so
 * the tests check what is committed. The MCP tool catalog is read from the production classes, not
 * from a running server, so the unit tests need no Spring context; {@code AgentWorkflowIT} checks
 * the same names against the live registry.
 */
public final class IntegrationDocs {

    /** The OpenClaw skill. */
    public static final Path SKILL = Path.of("integrations", "openclaw", "foresignal", "SKILL.md");

    /** The agent guide. */
    public static final Path AGENT_GUIDE = Path.of("docs", "Agent-Guide.md");

    /** The cookbooks directory. */
    public static final Path COOKBOOKS = Path.of("docs", "cookbooks");

    /** The importable artifacts directory. */
    public static final Path INTEGRATIONS = Path.of("integrations");

    /** The Grafana Operations dashboard. */
    public static final Path OPERATIONS_DASHBOARD =
            Path.of("integrations", "grafana", "dashboards", "foresignal-operations.json");

    /** Extensions of the files scanned for API paths. */
    public static final Set<String> TEXT_EXTENSIONS =
            Set.of("md", "json", "yaml", "yml", "py", "txt", "sh", "js", "toml");

    /** A YAML reader (Jackson 3); JSON is a subset, so it also reads JSON. */
    public static final YAMLMapper YAML = YAMLMapper.builder().build();

    /**
     * A {@code /v1/...} path as written in prose, code or configuration: it stops at whitespace,
     * quotes, backticks, a query string, a fragment or closing brackets of the surrounding text.
     */
    private static final Pattern API_PATH = Pattern.compile("/v1/[A-Za-z0-9_\\-.{}<>$…~%:]+(?:/[A-Za-z0-9_\\-.{}<>$…~%:]*)*");

    private static final Pattern FRONTMATTER = Pattern.compile("\\A---\\R(.*?)\\R---\\R(.*)\\z", Pattern.DOTALL);
    private static final Pattern ENV_REFERENCE = Pattern.compile("\\$\\{?([A-Z][A-Z0-9_]*)}?");
    private static final Pattern TABLE_ROW_TOOL = Pattern.compile("^\\|\\s*`([a-z][a-z0-9_]*)`\\s*\\|");
    private static final Pattern MCP_CALL = Pattern.compile("^([a-z][a-z0-9_]*)\\s*(\\{.*)?$");

    private IntegrationDocs() {}

    /** The UTF-8 text of {@code file}. */
    public static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    /** Every regular file under {@code root} with one of {@code extensions}, sorted. */
    public static List<Path> files(Path root, Set<String> extensions) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> extensions.contains(extension(p)))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list " + root, e);
        }
    }

    /** The lowercase extension of {@code file}, or the empty string. */
    public static String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** The files whose API paths must exist: the cookbooks, the artifacts and the agent guide. */
    public static List<Path> apiReferencingFiles() {
        List<Path> out = new ArrayList<>(files(COOKBOOKS, TEXT_EXTENSIONS));
        out.addAll(files(INTEGRATIONS, TEXT_EXTENSIONS));
        out.add(AGENT_GUIDE);
        return out;
    }

    /**
     * The {@code /v1/...} paths in {@code text}, without query strings, fragments and trailing
     * punctuation, in order of first appearance. A path ending in {@code /} is a prefix that code
     * completes (for example {@code "/v1/ingest/" + sourceId}). A key-in-path URL
     * {@code /v1/k/{key}/<rest>} (prompt 035) is reported as the path it serves, {@code /v1/<rest>}.
     */
    public static List<String> apiPaths(String text) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = API_PATH.matcher(text);
        while (m.find()) {
            String path = m.group();
            while (!path.isEmpty() && ".,:;".indexOf(path.charAt(path.length() - 1)) >= 0) {
                path = path.substring(0, path.length() - 1);
            }
            String canonical = keyInPath(path);
            if (canonical != null) {
                out.add(canonical);
            }
        }
        return List.copyOf(out);
    }

    /**
     * {@code path} with a {@code /v1/k/{key}} carrier removed ({@code /v1/k/fsf_…/records} becomes
     * {@code /v1/records}); null for a bare {@code /v1/k/{key}}, which serves no operation by itself.
     */
    static String keyInPath(String path) {
        if (!path.startsWith("/v1/k/")) {
            return path;
        }
        int slash = path.indexOf('/', "/v1/k/".length());
        return slash < 0 ? null : "/v1" + path.substring(slash);
    }

    /**
     * True when a documented path segment is a placeholder: {@code {sourceId}}, {@code <id>},
     * {@code ${sourceId}}, {@code eac_…} or {@code %7Bid%7D}. A placeholder matches only a path
     * variable of the spec, never a literal segment.
     */
    public static boolean placeholder(String segment) {
        return segment.contains("{") || segment.contains("<") || segment.contains("$") || segment.contains("…")
                || segment.contains("%");
    }

    /**
     * True when the documented {@code path} matches the spec path template {@code template}: the same
     * number of segments, each literal equal or the template's segment a variable. A placeholder
     * segment matches only a variable. A prefix (ending in {@code /}) matches a template that has
     * more segments after it.
     */
    public static boolean matches(String path, String template) {
        boolean prefix = path.endsWith("/");
        String[] doc = (prefix ? path.substring(0, path.length() - 1) : path).split("/", -1);
        String[] spec = template.split("/", -1);
        if (prefix ? doc.length >= spec.length : doc.length != spec.length) {
            return false;
        }
        for (int i = 0; i < doc.length; i++) {
            boolean variable = spec[i].startsWith("{") && spec[i].endsWith("}");
            if (variable) {
                if (doc[i].isEmpty()) {
                    return false;
                }
            } else if (spec[i].contains("{")) {
                // A segment with an embedded variable, such as {y}.mvt (checkpoint 041): the variable part matches
                // any non-empty text, the rest literally.
                String regex = java.util.Arrays.stream(spec[i].split("\\{[^}]+}", -1))
                        .map(java.util.regex.Pattern::quote)
                        .collect(java.util.stream.Collectors.joining(".+"));
                if (!doc[i].matches(regex)) {
                    return false;
                }
            } else if (placeholder(doc[i]) || !doc[i].equals(spec[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * The skill's frontmatter and body.
     *
     * @param frontmatter the parsed YAML frontmatter
     * @param body the markdown after the closing {@code ---}
     */
    public record Skill(JsonNode frontmatter, String body) {}

    /**
     * Splits {@code markdown} into its YAML frontmatter and body.
     *
     * @throws AssertionError when the file does not start with a {@code ---} block
     */
    public static Skill skill(String markdown) {
        Matcher m = FRONTMATTER.matcher(markdown);
        if (!m.matches()) {
            throw new AssertionError("SKILL.md must start with a YAML frontmatter block between --- lines");
        }
        return new Skill(YAML.readTree(m.group(1)), m.group(2));
    }

    /** The environment variables {@code text} references as {@code $NAME} or {@code ${NAME}}. */
    public static Set<String> envReferences(String text) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = ENV_REFERENCE.matcher(text);
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    /**
     * The tool names of {@code markdown}: the backticked first cell of every row of a table whose
     * header's first cell is {@code Tool}, and every call line in a fenced {@code mcp} block
     * ({@code tool_name {json arguments}}).
     */
    public static Set<String> toolNames(String markdown) {
        Set<String> out = new LinkedHashSet<>();
        boolean inToolTable = false;
        boolean inMcpBlock = false;
        for (String raw : markdown.split("\\R")) {
            String line = raw.strip();
            if (line.startsWith("```")) {
                inMcpBlock = !inMcpBlock && line.equals("```mcp");
                inToolTable = false;
                continue;
            }
            if (inMcpBlock) {
                Matcher call = MCP_CALL.matcher(line);
                if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("//") && call.matches()) {
                    out.add(call.group(1));
                }
                continue;
            }
            if (!line.startsWith("|")) {
                inToolTable = false;
                continue;
            }
            String first = line.substring(1).split("\\|", 2)[0].strip();
            if (first.equals("Tool")) {
                inToolTable = true;
                continue;
            }
            if (inToolTable) {
                Matcher row = TABLE_ROW_TOOL.matcher(line);
                if (row.find()) {
                    out.add(row.group(1));
                }
            }
        }
        return out;
    }

    /**
     * The MCP tools of the production code, by name, with their least scope. Read from every concrete
     * {@link ForesignalMcpTool} class compiled from {@code src/main/java}, without constructing it
     * (the name and scope methods return constants).
     */
    public static Map<String, Scope> toolCatalog() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ForesignalMcpTool.class));
        Map<String, Scope> out = new TreeMap<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("ai.foresignal")) {
            Class<?> type;
            try {
                type = Class.forName(definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            if (!fromMainSources(type)) {
                continue;
            }
            ForesignalMcpTool tool = (ForesignalMcpTool) Mockito.mock(type, Mockito.CALLS_REAL_METHODS);
            out.put(tool.name(), tool.requiredScope());
        }
        return out;
    }

    private static boolean fromMainSources(Class<?> type) {
        URL location = type.getProtectionDomain().getCodeSource() == null ? null
                : type.getProtectionDomain().getCodeSource().getLocation();
        return location != null && location.getPath().replace('\\', '/').contains("/main");
    }

    /**
     * The second cell (the scope column) of every row of a {@code Tool} table in {@code markdown}, by
     * tool name, for example {@code run_briefing → "agent, cost-bearing"}.
     */
    public static Map<String, String> toolScopeCells(String markdown) {
        Map<String, String> out = new LinkedHashMap<>();
        boolean inToolTable = false;
        for (String raw : markdown.split("\\R")) {
            String line = raw.strip();
            if (!line.startsWith("|")) {
                inToolTable = false;
                continue;
            }
            String[] cells = line.substring(1).split("\\|");
            String first = cells.length == 0 ? "" : cells[0].strip();
            if (first.equals("Tool")) {
                inToolTable = true;
                continue;
            }
            Matcher row = TABLE_ROW_TOOL.matcher(line);
            if (inToolTable && row.find() && cells.length > 1) {
                out.put(row.group(1), cells[1].strip());
            }
        }
        return out;
    }

    /** Credential-looking strings: an API token, ingest key or action capability with a real body. */
    public static final Pattern REAL_CREDENTIAL = Pattern.compile("\\b(fsr|eik|eac|fsf|fsx)_[A-Za-z0-9]{16,}");
}
