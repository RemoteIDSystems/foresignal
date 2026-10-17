package ai.foresignal.benchmark;

import ai.foresignal.benchmark.BenchmarkResults.Check;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The benchmark thresholds (prompt 123): defaults in {@value #RESOURCE}, with the operator setting
 * {@value SettingKeys#BENCHMARK_THRESHOLDS} (same shape) merged over them: its {@code suite} values replace the
 * defaults', a component it lists replaces that component's checks, and its {@code gating} entries replace the
 * defaults'. A check passes inside its pass threshold, warns inside its warn threshold and fails outside it; a metric
 * that cannot be measured warns.
 */
@Component
public class BenchmarkThresholds {

    /** The bundled defaults. */
    public static final String RESOURCE = "intel/benchmark-thresholds.json";

    /** The comparators. */
    public static final Set<String> COMPARATORS = Set.of("le", "ge", "eq", "within");

    /** The default suite seed when neither the file nor the setting names one. */
    public static final long DEFAULT_SEED = 1_230_001L;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final Map<String, Object> bundled;
    private final ObjectProvider<AppSettingService> settings;

    /**
     * Creates the thresholds from the bundled file and the operator setting (the injection constructor: the class
     * also has a private one, so Spring must be told which to use).
     */
    @org.springframework.beans.factory.annotation.Autowired
    public BenchmarkThresholds(JsonMapper json, ObjectProvider<AppSettingService> settings) {
        this.bundled = read(json);
        this.settings = settings;
    }

    private BenchmarkThresholds(Map<String, Object> document) {
        this.bundled = document;
        this.settings = null;
    }

    /** Thresholds over a parsed document only (tests and pure runs): no setting. */
    public static BenchmarkThresholds of(Map<String, Object> document) {
        return new BenchmarkThresholds(document);
    }

    /** The bundled defaults only. */
    public static BenchmarkThresholds bundled(JsonMapper json) {
        return new BenchmarkThresholds(read(json));
    }

    private static Map<String, Object> read(JsonMapper json) {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(in, MAP);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    /** The document in force: the defaults with the operator setting merged over them. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> document() {
        Map<String, Object> out = new LinkedHashMap<>(bundled);
        Map<String, Object> override = null;
        AppSettingService service = settings == null ? null : settings.getIfAvailable();
        if (service != null) {
            try {
                override = service.get(SettingKeys.BENCHMARK_THRESHOLDS, Map.class).orElse(null);
            } catch (RuntimeException e) {
                override = null;
            }
        }
        if (override == null) {
            return out;
        }
        for (String section : List.of("suite", "components", "gating")) {
            if (override.get(section) instanceof Map<?, ?> o) {
                Map<String, Object> merged = bundled.get(section) instanceof Map<?, ?> b
                        ? new LinkedHashMap<>((Map<String, Object>) b) : new LinkedHashMap<>();
                o.forEach((k, v) -> merged.put(String.valueOf(k), v));
                out.put(section, merged);
            }
        }
        return out;
    }

    /** The version of the document in force. */
    public String version() {
        Object v = document().get("version");
        return v == null ? "1.0.0" : String.valueOf(v);
    }

    /**
     * The suite parameters in force, with {@code seed} replacing the default seed when given.
     *
     * @param seed the requested seed, or null for the suite's fixed seed
     */
    public SuiteParams suite(Long seed) {
        Map<?, ?> suite = document().get("suite") instanceof Map<?, ?> m ? m : Map.of();
        long fixed = suite.get("seed") instanceof Number n ? n.longValue() : DEFAULT_SEED;
        int replicates = suite.get("replicates") instanceof Number n ? Math.max(1, Math.min(200, n.intValue())) : 10;
        int minBin = suite.get("minBinCount") instanceof Number n ? Math.max(1, n.intValue()) : 100;
        return new SuiteParams(seed != null ? seed : fixed, replicates, minBin);
    }

    /**
     * A configured check.
     *
     * @param scenario the scenario wire value
     * @param metric the metric
     * @param comparator the comparator
     * @param pass the pass threshold
     * @param warn the warn threshold, or null
     * @param note the note, or null
     */
    public record Rule(String scenario, String metric, String comparator, double pass, Double warn, String note) {}

    /** The checks of a component. */
    public List<Rule> rules(String component) {
        List<Rule> out = new ArrayList<>();
        if (!(document().get("components") instanceof Map<?, ?> components)) {
            return out;
        }
        if (!(components.get(component) instanceof List<?> list)) {
            return out;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> m && m.get("metric") != null && m.get("pass") instanceof Number pass) {
                String comparator = m.get("comparator") == null ? "le" : String.valueOf(m.get("comparator"));
                if (!COMPARATORS.contains(comparator)) {
                    continue;
                }
                out.add(new Rule(m.get("scenario") == null ? null : String.valueOf(m.get("scenario")),
                        String.valueOf(m.get("metric")), comparator, pass.doubleValue(),
                        m.get("warn") instanceof Number w ? w.doubleValue() : null,
                        m.get("note") == null ? null : String.valueOf(m.get("note"))));
            }
        }
        return out;
    }

    /** The components an autonomous feature depends on ({@code proposerAutoExecution}, {@code llmEnsembleWeight}). */
    public List<String> dependencies(String feature) {
        if (document().get("gating") instanceof Map<?, ?> gating && gating.get(feature) instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /** The component behind a finding type, or null. */
    public String componentOfFindingType(String findingType) {
        if (document().get("gating") instanceof Map<?, ?> gating
                && gating.get("findingTypes") instanceof Map<?, ?> types && types.get(findingType) != null) {
            return String.valueOf(types.get(findingType));
        }
        return null;
    }

    /**
     * Applies the component's checks for the scenario of {@code metrics}.
     *
     * @param component the component
     * @param metrics what was measured
     * @return the checked scenario result
     */
    public ScenarioResult evaluate(String component, ScenarioMetrics metrics) {
        List<Check> checks = new ArrayList<>();
        for (Rule rule : rules(component)) {
            if (rule.scenario() != null && !rule.scenario().equals(metrics.scenario())) {
                continue;
            }
            Double observed = metrics.metrics().get(rule.metric());
            checks.add(check(rule, metrics.scenario(), observed));
        }
        return new ScenarioResult(metrics, checks, Verdict.worst(checks.stream().map(Check::status).toList()));
    }

    /** One check of an observed value (null or non-finite: not measurable, a warning). */
    public static Check check(Rule rule, String scenario, Double observed) {
        Double value = observed == null || !Double.isFinite(observed) ? null : observed;
        Verdict status = value == null ? Verdict.WARN : verdict(rule.comparator(), rule.pass(), rule.warn(), value);
        return new Check(scenario, rule.metric(), rule.comparator(), rule.pass(), rule.warn(), value, status,
                rule.note());
    }

    /** The verdict of a value against a pass and an optional warn threshold. */
    static Verdict verdict(String comparator, double pass, Double warn, double value) {
        double eps = 1e-9;
        return switch (comparator) {
            case "ge" -> value >= pass - eps ? Verdict.PASS
                    : warn != null && value >= warn - eps ? Verdict.WARN : Verdict.FAIL;
            case "eq" -> Math.abs(value - pass) <= eps ? Verdict.PASS
                    : warn != null && Math.abs(value - pass) <= Math.abs(warn - pass) + eps ? Verdict.WARN
                            : Verdict.FAIL;
            case "within" -> Math.abs(value) <= pass + eps ? Verdict.PASS
                    : warn != null && Math.abs(value) <= warn + eps ? Verdict.WARN : Verdict.FAIL;
            default -> value <= pass + eps ? Verdict.PASS
                    : warn != null && value <= warn + eps ? Verdict.WARN : Verdict.FAIL;
        };
    }
}
