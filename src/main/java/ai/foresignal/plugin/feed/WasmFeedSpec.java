package ai.foresignal.plugin.feed;

import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.source.spi.FieldSpec;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * One {@code wasm-feed} source as the operator writes it in {@code ${FORESIGNAL_DATA_DIR}/wasm-feeds/*.json} (prompt
 * 141). Bound with unknown properties refused and checked by {@link WasmFeedSpecs} at startup.
 *
 * <p>The spec carries the full descriptor, because the plugin, not Foresignal, knows what the records are: tier,
 * time base, license terms and ingest policy are all the operator's to state. Durations are shorthand text
 * ({@code 15m}, {@code 30d}).
 *
 * @param id the source id, {@code wasm-…}
 * @param name the display name
 * @param description what the source holds, or null for a generated line
 * @param plugin the plugin name under {@code ${FORESIGNAL_DATA_DIR}/plugins/}
 * @param url the upstream URL, https only
 * @param kinds the record kinds the plugin emits; a record without {@code kind} gets the single kind
 * @param homepage where a human reads about the upstream
 * @param attribution the credit line
 * @param license the data license text (the descriptor's {@code licence})
 * @param termsUrl the upstream's terms
 * @param licenseTerms the structured license terms (the descriptor's {@code licenceTerms}), or null
 * @param historyTier {@code STANDARD} (default) or {@code TRACK}
 * @param observedAtMode {@code RECEIVED} (default) or {@code UPSTREAM} (then every record needs {@code observedAt})
 * @param recordTtl how long a record stays current, or null for never
 * @param ttlAnchor {@code RECEIVED} (default) or {@code OBSERVED}
 * @param ingestPolicy the default ingest policy in the API's {@code IngestPolicy} shape, or null for {@code ALL}
 * @param interval the default poll interval (default 15 min)
 * @param minInterval the floor on the poll interval (default 5 min)
 * @param budget the default request budget (default 4 a minute, 400 a day)
 * @param retention the default raw retention (default 1825 d, the {@code event} class); ignored for {@code TRACK}
 * @param titleField the payload field holding the title, or null
 * @param searchFields payload fields in the search text, or null for the defaults
 * @param embedFields payload fields embedded for semantic search
 * @param tileProperties payload fields copied into tiles
 * @param volatileFields payload fields excluded from hashing
 * @param significantFields payload fields whose change is always a version
 * @param fieldSpecs declared payload fields
 * @param egress {@code OPEN} (default) or {@code LOCAL_ONLY}
 * @param zeroResultsNormal true when an empty answer is unremarkable (default false)
 * @param conditional false to send plain GETs instead of conditional ones (default true)
 * @param maxResponseBytes the cap on one response (default 8 MB, at most the plugin input cap of 16 MB)
 */
public record WasmFeedSpec(
        String id,
        String name,
        String description,
        String plugin,
        String url,
        List<String> kinds,
        String homepage,
        String attribution,
        String license,
        String termsUrl,
        LicenceTerms licenseTerms,
        String historyTier,
        String observedAtMode,
        String recordTtl,
        String ttlAnchor,
        JsonNode ingestPolicy,
        String interval,
        String minInterval,
        Budget budget,
        String retention,
        String titleField,
        List<String> searchFields,
        List<String> embedFields,
        List<String> tileProperties,
        List<String> volatileFields,
        List<String> significantFields,
        Map<String, FieldSpec> fieldSpecs,
        String egress,
        Boolean zeroResultsNormal,
        Boolean conditional,
        Integer maxResponseBytes) {

    /** Null lists and maps become empty. */
    public WasmFeedSpec {
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        embedFields = embedFields == null ? List.of() : List.copyOf(embedFields);
        tileProperties = tileProperties == null ? List.of() : List.copyOf(tileProperties);
        volatileFields = volatileFields == null ? List.of() : List.copyOf(volatileFields);
        significantFields = significantFields == null ? List.of() : List.copyOf(significantFields);
        fieldSpecs = fieldSpecs == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(fieldSpecs));
    }

    /** True unless the spec turns conditional GETs off. */
    public boolean wantsConditional() {
        return !Boolean.FALSE.equals(conditional);
    }

    /**
     * The default request budget.
     *
     * @param perMinute requests per minute
     * @param perDay requests per day
     * @param mbPerDay megabytes per day, or null
     */
    public record Budget(Integer perMinute, Integer perDay, Integer mbPerDay) {}
}
