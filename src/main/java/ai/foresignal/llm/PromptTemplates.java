package ai.foresignal.llm;

import ai.foresignal.manifest.CanonicalJson;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * The versioned LLM prompt templates (prompt 116): every LLM role's system prompt lives in
 * {@code src/main/resources/llm/templates/{id}@{version}.md}, exactly as sent, so manifests can record
 * {@code {templateId, version}} and clients can show what the model was asked
 * ({@code GET /v1/prompt-templates/{id}}). A changed prompt is a new file with a new version; old versions stay.
 */
public final class PromptTemplates {

    /** The resource folder. */
    public static final String FOLDER = "llm/templates/";

    /** Ask, tools mode (028). */
    public static final String ASK = "ask-system";

    /** Ask, retrieval-only mode (028). */
    public static final String ASK_RETRIEVAL = "ask-retrieval-system";

    /** Alert enrichment (030). */
    public static final String ENRICHMENT = "enrichment-system";

    /** LLM briefings (029). */
    public static final String BRIEFING = "briefing-system";

    /** Mapping drafts (059). */
    public static final String MAPPING_SUGGEST = "mapping-suggest-system";

    /** Tuning-suggestion explanations (115). */
    public static final String TUNING_EXPLAINER = "tuning-explainer-system";

    /** The autonomous analyst (102); the profile's operator instructions are appended at run time. */
    public static final String ANALYST = "analyst-system";

    /** The version every template shipped at in prompt 116. */
    public static final String V1 = "1.0.0";

    /** The situation brief (prompt 118). */
    public static final String SITUATION_BRIEF = "situation-brief-system";

    /** The analysis-cycle analyst role (prompt 128). */
    public static final String CYCLE_ANALYST = "cycle-analyst-system";

    /** The analysis-cycle red team (prompt 128). */
    public static final String CYCLE_RED_TEAM = "cycle-red-team-system";

    /** The analysis-cycle forecaster (prompt 128). */
    public static final String CYCLE_FORECASTER = "cycle-forecaster-system";

    /** The analysis-cycle proposer (prompt 128). */
    public static final String CYCLE_PROPOSER = "cycle-proposer-system";

    /** The LLM role of each template. */
    public static final Map<String, String> ROLES = Map.ofEntries(Map.entry(ASK, "ask"),
            Map.entry(ASK_RETRIEVAL, "ask"), Map.entry(ENRICHMENT, "enrichment"), Map.entry(BRIEFING, "briefing"),
            Map.entry(MAPPING_SUGGEST, "mapping"), Map.entry(TUNING_EXPLAINER, "tuning"),
            Map.entry(ANALYST, "analyst"), Map.entry(SITUATION_BRIEF, "situation-brief"),
            Map.entry(CYCLE_ANALYST, "cycle-analyst"), Map.entry(CYCLE_RED_TEAM, "cycle-red-team"),
            Map.entry(CYCLE_FORECASTER, "cycle-forecaster"), Map.entry(CYCLE_PROPOSER, "cycle-proposer"));

    private static final Pattern FILE = Pattern.compile("([a-z0-9-]+)@([0-9]+\\.[0-9]+\\.[0-9]+)\\.md");

    private static final Map<String, String> TEXTS = new ConcurrentHashMap<>();

    private static volatile Map<String, List<String>> index;

    private PromptTemplates() {}

    /**
     * The text of template {@code id} at {@code version}.
     *
     * @throws IllegalStateException when the template is not bundled (a build defect)
     */
    public static String text(String id, String version) {
        return find(id, version).orElseThrow(() -> new IllegalStateException(
                "Prompt template " + id + "@" + version + " is not bundled under " + FOLDER));
    }

    /** The text of template {@code id} at {@code version}, if bundled. */
    public static Optional<String> find(String id, String version) {
        if (id == null || version == null || !FILE.matcher(id + "@" + version + ".md").matches()) {
            return Optional.empty();
        }
        String key = id + "@" + version;
        String cached = TEXTS.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        ClassPathResource resource = new ClassPathResource(FOLDER + key + ".md");
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream in = resource.getInputStream()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            TEXTS.put(key, text);
            return Optional.of(text);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read prompt template " + key, e);
        }
    }

    /** The bundled versions of {@code id}, oldest first; empty when unknown. */
    public static List<String> versions(String id) {
        return index().getOrDefault(id, List.of());
    }

    /** The newest bundled version of {@code id}, if any. */
    public static Optional<String> latest(String id) {
        List<String> versions = versions(id);
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(versions.size() - 1));
    }

    /** Every bundled template id with its versions. */
    public static Map<String, List<String>> all() {
        return index();
    }

    /** The SHA-256 of a template's text. */
    public static String sha256(String id, String version) {
        return CanonicalJson.sha256(text(id, version));
    }

    /** {@code {templateId, version}} as manifests record it. */
    public static Map<String, Object> reference(String id, String version) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("templateId", id);
        out.put("version", version);
        return out;
    }

    private static Map<String, List<String>> index() {
        Map<String, List<String>> current = index;
        if (current != null) {
            return current;
        }
        Map<String, List<String>> out = new TreeMap<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:" + FOLDER + "*.md");
            for (Resource resource : resources) {
                String name = resource.getFilename();
                Matcher m = name == null ? null : FILE.matcher(name);
                if (m != null && m.matches()) {
                    out.computeIfAbsent(m.group(1), k -> new ArrayList<>()).add(m.group(2));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list the prompt templates", e);
        }
        for (List<String> versions : out.values()) {
            versions.sort(Comparator.comparing(PromptTemplates::semver));
        }
        index = out;
        return out;
    }

    private static String semver(String version) {
        StringBuilder key = new StringBuilder();
        for (String part : version.split("\\.")) {
            key.append(String.format("%08d", Integer.parseInt(part)));
        }
        return key.toString();
    }
}
