package ai.foresignal.inbound;

import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the dynamic {@link SourceDescriptor} of a custom push source from its row (prompt 024).
 *
 * <p>Fixed for every custom source: mode {@code PUSH}, {@code custom=true}, history tier
 * {@code STANDARD} (custom sources are never TRACK), and {@code zeroResultsNormal=true} (a quiet
 * webhook is not a broken upstream). The budget is {@code rateLimitPerMinute} per minute and
 * {@code rateLimitPerMinute × 1440} per day.
 */
public final class CustomSourceDescriptors {

    private static final TypeReference<LinkedHashMap<String, FieldSpec>> FIELD_SPECS = new TypeReference<>() {};

    private CustomSourceDescriptors() {}

    /** The descriptor of {@code row}. */
    public static SourceDescriptor toDescriptor(CustomSourceEntity row, JsonMapper json) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(row.getSourceId())
                .name(row.getName())
                .description(row.getDescription())
                .attribution(row.getAttribution())
                .licence(row.getLicence())
                .mode(Mode.PUSH)
                .custom(true)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.valueOf(row.getObservedAtMode()))
                .ttlAnchor(TtlAnchor.valueOf(row.getTtlAnchor()))
                .recordTtl(row.getRecordTtl() == null ? null : Durations.parse(row.getRecordTtl()))
                .titleField(row.getTitleField())
                .rankField(row.getRankField())
                .privateSource(row.isPrivateSource())
                .defaultBudget(row.getRateLimitPerMinute(), row.getRateLimitPerMinute() * 1440)
                .zeroResultsNormal(true);
        row.getKinds().forEach(builder::kind);
        builder.tileProperties(row.getTileProperties().toArray(String[]::new));
        if (row.getSearchFields() != null) {
            builder.searchFields(row.getSearchFields().toArray(String[]::new));
        }
        builder.embedFields(row.getEmbedFields().toArray(String[]::new));
        builder.volatileFields(row.getVolatileFields().toArray(String[]::new));
        builder.significantFields(row.getSignificantFields().toArray(String[]::new));
        fieldSpecs(row, json).forEach(builder::fieldSpec);
        return builder.build();
    }

    /** The row's field specs. */
    static Map<String, FieldSpec> fieldSpecs(CustomSourceEntity row, JsonMapper json) {
        if (row.getFieldSpecs() == null || row.getFieldSpecs().isBlank()) {
            return Map.of();
        }
        return json.readValue(row.getFieldSpecs(), FIELD_SPECS);
    }
}
