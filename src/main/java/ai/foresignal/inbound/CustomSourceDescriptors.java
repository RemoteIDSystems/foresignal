package ai.foresignal.inbound;

import ai.foresignal.mapping.preset.PresetCatalog;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import java.time.Duration;
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
 *
 * <p>Profile {@code personal-location} (prompt 039) adds descriptor defaults the operator can still
 * override per source ({@code PUT /v1/sources/{id}}): ingest policy SAMPLED (minInterval 30 s,
 * keyframe 10 min, minDeviation 50 m), default retention 7 days, and the private precision cap set to
 * its maximum of 7 decimals (about 1 cm), which is "off" for practical purposes: it is the owner's
 * own data. A lower {@code privatePrecisionDecimals} remains available through 008's cap.
 *
 * <p>A source created from a device preset (prompt 059, {@code preset}) gets the preset's descriptor defaults,
 * which the operator can still override per source: its ingest policy (LATEST for weather stations, air quality
 * and radiation; SAMPLED vessel defaults for {@code aiscatcher}), retention ({@code aiscatcher} 3 days), field
 * thresholds and motion fields.
 */
public final class CustomSourceDescriptors {

    private static final TypeReference<LinkedHashMap<String, FieldSpec>> FIELD_SPECS = new TypeReference<>() {};

    /** The personal-location profile's ingest policy (prompt 039). */
    public static final IngestPolicy PERSONAL_LOCATION_POLICY =
            new IngestPolicy.Sampled(Duration.ofSeconds(30), Duration.ofMinutes(10), 50.0, false, null);

    /** The personal-location profile's default retention (prompt 039). */
    public static final Duration PERSONAL_LOCATION_RETENTION = Duration.ofDays(7);

    /** The largest private precision (decimals) a descriptor accepts. */
    static final int MAX_PRECISION_DECIMALS = 7;

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
        if (row.getLicenceTerms() != null && !row.getLicenceTerms().isBlank()) {
            // Prompt 109: the custom source's own terms, snapshotted on each PUSH batch's fetch row.
            builder.licenceTerms(json.readValue(row.getLicenceTerms(), ai.foresignal.provenance.LicenceTerms.class));
        }
        row.getKinds().forEach(builder::kind);
        builder.tileProperties(row.getTileProperties().toArray(String[]::new));
        if (row.getSearchFields() != null) {
            builder.searchFields(row.getSearchFields().toArray(String[]::new));
        }
        builder.embedFields(row.getEmbedFields().toArray(String[]::new));
        builder.volatileFields(row.getVolatileFields().toArray(String[]::new));
        builder.significantFields(row.getSignificantFields().toArray(String[]::new));
        fieldSpecs(row, json).forEach(builder::fieldSpec);
        PresetCatalog.find(row.getPreset()).ifPresent(preset -> {
            if (preset.ingestPolicy() != null) {
                builder.defaultIngestPolicy(preset.ingestPolicy());
            }
            if (preset.retention() != null) {
                builder.defaultRetention(preset.retention());
            }
            preset.thresholds().forEach(builder::threshold);
            if (preset.motion() != null) {
                builder.motion(preset.motion());
            }
        });
        if (CustomSourceInputReader.PROFILE_PERSONAL_LOCATION.equals(row.getProfile())) {
            builder.defaultIngestPolicy(PERSONAL_LOCATION_POLICY)
                    .defaultRetention(PERSONAL_LOCATION_RETENTION)
                    .privatePrecisionDecimals(MAX_PRECISION_DECIMALS);
        }
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
