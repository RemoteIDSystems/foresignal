package ai.foresignal.stats.metrics;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.trigger.RuleValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/**
 * {@code getMetricSettings} / {@code updateMetricSettings} (prompt 038): validates and stores the metric
 * projections. A PUT replaces the whole list; every problem is reported in one 400.
 *
 * <ul>
 *   <li>{@code name}: {@code ^[a-z_][a-z0-9_]*$}, at most {@value #MAX_NAME} characters, unique;
 *   <li>{@code source}: a registered source; a private one needs {@code allowPrivate: true};
 *   <li>{@code kind}: one the source declares;
 *   <li>{@code path} and label paths: the 017 trigger path syntax, never a {@code volatileFields} key;
 *   <li>{@code labels}: at most {@value #MAX_LABELS}, names {@code ^[a-zA-Z_][a-zA-Z0-9_]*$}, unique;
 *   <li>{@code maxSeries}: 1 to {@value ProjectionService#MAX_TOTAL_SERIES}.
 * </ul>
 */
@Service
public class MetricSettingsService {

    /** The shared private-source visibility (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.visibility.SourceVisibility visibility;


    /** The projection name pattern. */
    public static final Pattern NAME = Pattern.compile("^[a-z_][a-z0-9_]*$");

    /** The label name pattern. */
    public static final Pattern LABEL = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /** The longest projection name. */
    public static final int MAX_NAME = 100;

    /** The most labels per projection. */
    public static final int MAX_LABELS = 4;

    /** The longest {@code unit} and {@code help}. */
    static final int MAX_TEXT = 500;

    /** The members of a projection. */
    static final Set<String> MEMBERS = Set.of("name", "source", "kind", "path", "labels", "unit", "help", "maxSeries",
            "allowPrivate");

    private final AppSettingService settings;
    private final SourceRegistry registry;

    /** Creates the service. */
    public MetricSettingsService(AppSettingService settings, SourceRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    /** The projections in force. */
    public MetricSettings get() {
        return settings.getOrDefault(SettingKeys.METRICS, MetricSettings.class, MetricSettings::empty);
    }

    /**
     * Validates and stores {@code body}.
     *
     * @throws ValidationException with every problem
     */
    @Transactional
    public MetricSettings update(JsonNode body) {
        MetricSettings value = parse(body);
        settings.put(SettingKeys.METRICS, value);
        return value;
    }

    /** Parses and validates a {@code MetricSettings} body without storing it. */
    public MetricSettings parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a MetricSettings object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : body.propertyNames()) {
            if (!"projections".equals(field)) {
                errors.add(new FieldError(field, "is not a MetricSettings member"));
            }
        }
        JsonNode list = body.get("projections");
        List<MetricProjection> out = new ArrayList<>();
        if (list == null || !list.isArray()) {
            errors.add(new FieldError("projections", "is required and must be an array"));
        } else {
            Set<String> names = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                MetricProjection p = projection(list.get(i), "projections[" + i + "]", names, errors);
                if (p != null) {
                    out.add(p);
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The metric settings are not valid.", errors);
        }
        return new MetricSettings(out);
    }

    private MetricProjection projection(JsonNode node, String at, Set<String> names, List<FieldError> errors) {
        if (node == null || !node.isObject()) {
            errors.add(new FieldError(at, "must be an object"));
            return null;
        }
        int before = errors.size();
        for (String field : node.propertyNames()) {
            if (!MEMBERS.contains(field)) {
                errors.add(new FieldError(at + "." + field, "is not a MetricProjection member"));
            }
        }
        String name = text(node, "name", at, true, errors);
        if (name != null) {
            if (!NAME.matcher(name).matches() || name.length() > MAX_NAME) {
                errors.add(new FieldError(at + ".name", "must match ^[a-z_][a-z0-9_]*$ (at most " + MAX_NAME
                        + " characters)"));
            } else if (!names.add(name)) {
                errors.add(new FieldError(at + ".name", "duplicates another projection's name"));
            }
        }
        boolean allowPrivate = node.hasNonNull("allowPrivate") && node.get("allowPrivate").asBoolean();
        if (node.hasNonNull("allowPrivate") && !node.get("allowPrivate").isBoolean()) {
            errors.add(new FieldError(at + ".allowPrivate", "must be a boolean"));
        }
        String sourceId = text(node, "source", at, true, errors);
        SourceDescriptor descriptor = null;
        if (sourceId != null) {
            Optional<SourceDescriptor> found = registry.find(sourceId).map(r -> r.descriptor());
            if (found.isEmpty()) {
                errors.add(new FieldError(at + ".source", "unknown source " + sourceId));
            } else {
                descriptor = found.get();
                boolean isPrivate = visibility == null ? descriptor.privateSource() : visibility.isPrivate(sourceId);
                if (isPrivate && !allowPrivate) {
                    errors.add(new FieldError(at + ".source", sourceId
                            + " is private; set allowPrivate: true to project it"));
                }
            }
        }
        String kind = text(node, "kind", at, false, errors);
        if (kind != null && descriptor != null && !descriptor.kinds().contains(kind)) {
            errors.add(new FieldError(at + ".kind", sourceId + " does not emit kind " + kind));
        }
        String path = text(node, "path", at, true, errors);
        path(path, at + ".path", descriptor, errors);
        List<MetricProjection.Label> labels = new ArrayList<>();
        JsonNode labelNodes = node.get("labels");
        if (labelNodes != null && !labelNodes.isNull()) {
            if (!labelNodes.isArray()) {
                errors.add(new FieldError(at + ".labels", "must be an array"));
            } else if (labelNodes.size() > MAX_LABELS) {
                errors.add(new FieldError(at + ".labels", "at most " + MAX_LABELS + " labels"));
            } else {
                Set<String> labelNames = new HashSet<>();
                for (int j = 0; j < labelNodes.size(); j++) {
                    JsonNode l = labelNodes.get(j);
                    String la = at + ".labels[" + j + "]";
                    if (l == null || !l.isObject()) {
                        errors.add(new FieldError(la, "must be an object with name and path"));
                        continue;
                    }
                    String labelName = text(l, "name", la, true, errors);
                    String labelPath = text(l, "path", la, true, errors);
                    if (labelName != null && (!LABEL.matcher(labelName).matches() || labelName.startsWith("__"))) {
                        errors.add(new FieldError(la + ".name", "must match ^[a-zA-Z_][a-zA-Z0-9_]*$ and not start"
                                + " with __"));
                    } else if (labelName != null && !labelNames.add(labelName)) {
                        errors.add(new FieldError(la + ".name", "duplicates another label"));
                    }
                    path(labelPath, la + ".path", descriptor, errors);
                    labels.add(new MetricProjection.Label(labelName, labelPath));
                }
            }
        }
        String unit = text(node, "unit", at, false, errors);
        String help = text(node, "help", at, false, errors);
        for (String[] t : new String[][] {{"unit", unit}, {"help", help}}) {
            if (t[1] != null && t[1].length() > MAX_TEXT) {
                errors.add(new FieldError(at + "." + t[0], "at most " + MAX_TEXT + " characters"));
            }
        }
        Integer maxSeries = null;
        if (node.hasNonNull("maxSeries")) {
            JsonNode m = node.get("maxSeries");
            if (!m.isIntegralNumber() || m.asLong() < 1 || m.asLong() > ProjectionService.MAX_TOTAL_SERIES) {
                errors.add(new FieldError(at + ".maxSeries", "must be an integer from 1 to "
                        + ProjectionService.MAX_TOTAL_SERIES));
            } else {
                maxSeries = m.asInt();
            }
        }
        if (errors.size() > before) {
            return null;
        }
        return new MetricProjection(name, sourceId, kind, path, labels, unit, help, maxSeries, allowPrivate);
    }

    private static void path(String path, String at, SourceDescriptor descriptor, List<FieldError> errors) {
        if (path == null) {
            return;
        }
        if (!RuleValidator.PATH.matcher(path).matches()) {
            errors.add(new FieldError(at, "must be a dotted payload path such as mag or properties.depth"));
            return;
        }
        String root = path.contains(".") ? path.substring(0, path.indexOf('.')) : path;
        if (descriptor != null && descriptor.volatileFields().contains(root)) {
            errors.add(new FieldError(at, root + " is a volatile field of " + descriptor.id()
                    + "; volatile fields are not projected"));
        }
        // The same path splitting the trigger engine uses (017).
        RuleCondition.steps(path);
    }

    private static String text(JsonNode node, String field, String at, boolean required, List<FieldError> errors) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            if (required) {
                errors.add(new FieldError(at + "." + field, "is required"));
            }
            return null;
        }
        if (!value.isString() || value.asString().isBlank()) {
            errors.add(new FieldError(at + "." + field, "must be a non-empty string"));
            return null;
        }
        return value.asString().strip();
    }
}
