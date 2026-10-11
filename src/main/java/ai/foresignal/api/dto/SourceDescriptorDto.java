package ai.foresignal.api.dto;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.KindDisplay;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * The {@code SourceDescriptor} schema: everything fixed about a source, with every v1.2 field
 * (Core-Data-Model §3) and the display extras prompt 008 added.
 *
 * <p>Durations serialize in shorthand through the prompt 002 serializers. Fields the spec assigns
 * to later prompts ({@code egress}, {@code licenceTerms}, {@code reliability}, {@code access}, ...)
 * are added by those prompts.
 *
 * @param id source id
 * @param name display name
 * @param description what the source is
 * @param kinds record kinds it produces, sorted
 * @param homepage upstream homepage
 * @param attribution attribution text
 * @param mode {@code polling}, {@code streaming}, {@code push} or {@code derived}
 * @param custom true for a runtime-defined custom push source (none before prompt 024)
 * @param credentialFields credentials it accepts
 * @param params adapter parameters
 * @param minInterval the shortest allowed poll interval
 * @param defaultInterval the poll interval used when none is set
 * @param defaultBudget the budget used when none is set
 * @param recordTtl how long a record stays current without being seen, or null
 * @param zeroResultsNormal whether an empty fetch is normal
 * @param tileProperties payload keys copied into vector tiles
 * @param searchFields payload keys indexed for search
 * @param embedFields payload keys embedded for semantic search
 * @param rankField payload key used to rank, or null
 * @param licence license identifier snapshotted on fetch rows (the wire name is {@code licence})
 * @param termsUrl the upstream's terms
 * @param historyTier {@code STANDARD} or {@code TRACK}
 * @param observedAtMode {@code UPSTREAM} or {@code RECEIVED}
 * @param ttlAnchor {@code OBSERVED} or {@code RECEIVED}
 * @param defaultIngestPolicy the descriptor's policy, with its documented defaults filled in
 * @param volatileFields payload keys left out of hashes
 * @param significantFields payload keys whose change persists SIGNIFICANT
 * @param motion dead-reckoning fields, or null
 * @param thresholds SAMPLED thresholds
 * @param defaultRetention STANDARD default retention, or null
 * @param kindRetention per-kind default retention
 * @param skewTolerance future-clamp tolerance
 * @param allowNullIsland whether exact (0,0) points are kept
 * @param products raster product ids
 * @param titleField payload key used as record title, or null
 * @param entityKeys entity key type to payload field
 * @param fieldSpecs declared payload fields
 * @param kindDisplay display hints per kind
 * @param aggBelowZoom zoom below which tiles aggregate, or null
 * @param startField payload key of an interval start, or null
 * @param endField payload key of an interval end, or null
 * @param privateSource whether the source holds personal data (wire name {@code private})
 * @param privatePrecisionDecimals coordinate decimals kept for a private source
 */
public record SourceDescriptorDto(
        String id,
        String name,
        String description,
        List<String> kinds,
        String homepage,
        String attribution,
        String mode,
        boolean custom,
        List<CredentialFieldDto> credentialFields,
        List<ParamSpecDto> params,
        Duration minInterval,
        Duration defaultInterval,
        BudgetDto defaultBudget,
        Duration recordTtl,
        boolean zeroResultsNormal,
        List<String> tileProperties,
        List<String> searchFields,
        List<String> embedFields,
        String rankField,
        String licence,
        String termsUrl,
        String historyTier,
        String observedAtMode,
        String ttlAnchor,
        IngestPolicyDto defaultIngestPolicy,
        List<String> volatileFields,
        List<String> significantFields,
        MotionSpecDto motion,
        List<FieldThresholdDto> thresholds,
        Duration defaultRetention,
        Map<String, Duration> kindRetention,
        Duration skewTolerance,
        boolean allowNullIsland,
        List<String> products,
        String titleField,
        Map<String, String> entityKeys,
        Map<String, FieldSpec> fieldSpecs,
        Map<String, KindDisplay> kindDisplay,
        Integer aggBelowZoom,
        String startField,
        String endField,
        @JsonProperty("private") boolean privateSource,
        int privatePrecisionDecimals) {}
