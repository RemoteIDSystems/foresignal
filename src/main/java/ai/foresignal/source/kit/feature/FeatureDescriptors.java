package ai.foresignal.source.kit.feature;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * The descriptor of a feature-kit source, built from its spec (prompt 073).
 *
 * <ul>
 *   <li><strong>Schedule:</strong> POLLING at the spec's interval (default 15 min, floor 5 min) and budget;
 *       redirects refused; a 60 s fetch timeout; the spec's response cap (default 16 MB).
 *   <li><strong>History:</strong> STANDARD, policy ALL (its keyframe derived from the TTL, Core-Data-Model §5.1),
 *       {@code observedAtMode} UPSTREAM when {@code time.observedAt} names a field, else RECEIVED;
 *       {@code ttlAnchor} as the spec says (RECEIVED by default); retention the spec's (default 1825 d, the
 *       {@code event} class).
 *   <li><strong>Fields:</strong> one {@code fieldSpec} per mapped field and constant, plus the simplification flags;
 *       the spec's title, search, tile, significant and volatile fields.
 * </ul>
 *
 * <p>Every kit source ships disabled.
 */
public final class FeatureDescriptors {

    /** The fetch timeout. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(60);

    private FeatureDescriptors() {}

    /** The descriptor of {@code spec}'s source. */
    public static SourceDescriptor descriptor(FeatureSourceSpec spec) {
        return builder(spec).build();
    }

    /**
     * The descriptor builder of {@code spec}'s source, for a hand-written adapter that adds what a spec cannot say,
     * such as operator parameters (prompt 079's {@code eccc-hydrometric}).
     */
    public static SourceDescriptor.Builder builder(FeatureSourceSpec spec) {
        FeatureSourceSpec.BudgetSpec budget = spec.budget();
        SourceDescriptor.Builder b = SourceDescriptor.builder(spec.id())
                .name(spec.name())
                .description(spec.description() == null || spec.description().isBlank()
                        ? spec.name() + " (" + spec.protocol() + " feature kit)" : spec.description())
                .kind(spec.kind())
                .homepage(spec.homepage())
                .attribution(spec.attribution())
                .licence(spec.license())
                .termsUrl(spec.termsUrl())
                .mode(Mode.POLLING)
                .interval(spec.minIntervalDuration(), spec.intervalDuration())
                .defaultBudget(budget.mbPerDay() == null ? Budget.of(budget.perMinute(), budget.perDay())
                        : Budget.of(budget.perMinute(), budget.perDay(), budget.mbPerDay()))
                .maxResponseBytes(spec.effectiveMaxResponseBytes())
                .fetchTimeout(FETCH_TIMEOUT)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor("OBSERVED".equals(spec.ttlAnchor()) ? TtlAnchor.OBSERVED : TtlAnchor.RECEIVED)
                .observedAtMode(spec.time().observedAtReceived() ? ObservedAtMode.RECEIVED : ObservedAtMode.UPSTREAM)
                .recordTtl(spec.recordTtlDuration())
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .defaultRetention(spec.retentionDuration())
                .kindRetention(spec.kind(), spec.retentionDuration())
                .zeroResultsNormal(Boolean.TRUE.equals(spec.zeroResultsNormal()))
                .searchFields(spec.searchFields().toArray(String[]::new))
                .tileProperties(spec.tileProperties().toArray(String[]::new))
                .significantFields(spec.significantFields().toArray(String[]::new))
                .volatileFields(spec.volatileFields().toArray(String[]::new));
        if (spec.titleField() != null) {
            b.titleField(spec.titleField());
        }
        if (spec.credential() != null) {
            FeatureSourceSpec.CredentialSpec c = spec.credential();
            String description = c.description() == null ? "The " + c.param() + " value for " + spec.name()
                    : c.description();
            b.credential(Boolean.TRUE.equals(c.required()) ? CredentialField.required(c.name(), description)
                    : CredentialField.optional(c.name(), description));
        }
        for (Map.Entry<String, FeatureSourceSpec.FieldMapping> e : spec.fields().entrySet()) {
            FeatureSourceSpec.FieldMapping m = e.getValue();
            String target = m.target() == null ? e.getKey() : m.target();
            FieldType type = type(m);
            String unit = m.unit();
            b.fieldSpec(target, new FieldSpec(type, unit, target, "Upstream attribute " + e.getKey(), null, Set.of()));
        }
        for (Map.Entry<String, Object> c : spec.constants().entrySet()) {
            Object v = c.getValue();
            FieldType type = v instanceof Boolean ? FieldType.BOOLEAN
                    : v instanceof Integer || v instanceof Long ? FieldType.INTEGER
                    : v instanceof Number ? FieldType.NUMBER : FieldType.STRING;
            b.fieldSpec(c.getKey(), new FieldSpec(type, null, c.getKey(), "Fixed by the spec", null, Set.of()));
        }
        b.fieldSpec(FeatureGeometry.SIMPLIFIED_KEY, new FieldSpec(FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry was simplified to fit the 10,000-vertex limit", null, Set.of()));
        b.fieldSpec(FeatureGeometry.TOLERANCE_KEY, new FieldSpec(FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance the geometry was simplified with", null, Set.of()));
        return b;
    }

    /**
     * The field type of a mapping. A value map turns a coded value into its label, so a mapped field whose map
     * values are all text is a string whatever its upstream type.
     */
    static FieldType type(FeatureSourceSpec.FieldMapping m) {
        if (!m.valueMap().isEmpty() && m.valueMap().values().stream().allMatch(v -> v instanceof String)) {
            return FieldType.STRING;
        }
        return switch (m.type() == null ? "string" : m.type()) {
            case "number" -> FieldType.NUMBER;
            case "integer" -> FieldType.INTEGER;
            case "boolean" -> FieldType.BOOLEAN;
            case "timestamp" -> FieldType.TIMESTAMP;
            default -> FieldType.STRING;
        };
    }
}
