package ai.foresignal.inbound;

import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.source.spi.FieldSpec;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.node.ObjectNode;

/**
 * A validated {@code CustomSourceInput} (OpenAPI), ready to be stored.
 *
 * @param id the source id ({@code custom-…})
 * @param name the display name
 * @param description the description, or null
 * @param kinds the kinds, in input order
 * @param attribution the attribution line, or null
 * @param recordTtl the TTL text as given, or null
 * @param recordTtlDuration the parsed TTL, or null
 * @param tileProperties payload fields copied into tiles
 * @param searchFields search fields, or null for the defaults
 * @param embedFields embedded fields
 * @param rankField the rank field, or null
 * @param rateLimitPerMinute requests per minute
 * @param observedAtMode {@code UPSTREAM} or {@code RECEIVED}
 * @param ttlAnchor {@code RECEIVED} or {@code OBSERVED}
 * @param volatileFields fields excluded from the hash
 * @param significantFields fields whose change is always a version
 * @param titleField the title field, or null
 * @param fieldSpecs declared payload fields
 * @param privateSource true for personal data
 * @param licence the data license, or null
 * @param recordsPerMinute the records-per-minute cap
 * @param formats per-format options (an object keyed by format id)
 * @param verifiers the non-secret verifier configuration per format
 * @param verifierSecrets the secrets given in this request per format (absent means keep)
 * @param profile the applied preset ({@code personal-location}, prompt 039), or null
 * @param allowPathKey true when the ingest key may be carried in the path (prompt 039)
 * @param ingestAllowCidrs the sender allowlist for path-key and GET pushes, or null for private ranges
 *     only (prompt 039)
 * @param mapping the {@code RecordMapping}, validated, or null (prompt 059)
 * @param preset the preset the source was created from, or null (prompt 059)
 * @param defaultFormat the format of {@code POST /v1/ingest/{sourceId}}, or null for native (prompt 059)
 * @param captureSamples true to capture raw requests for an hour (prompt 059)
 * @param licenceTerms the structured licence terms of pushed data (prompt 109), or null
 */
public record CustomSourceDefinition(
        String id,
        String name,
        String description,
        List<String> kinds,
        String attribution,
        String recordTtl,
        Duration recordTtlDuration,
        List<String> tileProperties,
        List<String> searchFields,
        List<String> embedFields,
        String rankField,
        int rateLimitPerMinute,
        String observedAtMode,
        String ttlAnchor,
        List<String> volatileFields,
        List<String> significantFields,
        String titleField,
        Map<String, FieldSpec> fieldSpecs,
        boolean privateSource,
        String licence,
        int recordsPerMinute,
        ObjectNode formats,
        Map<String, VerifierConfig> verifiers,
        Map<String, String> verifierSecrets,
        String profile,
        boolean allowPathKey,
        List<String> ingestAllowCidrs,
        tools.jackson.databind.JsonNode mapping,
        String preset,
        String defaultFormat,
        boolean captureSamples,
        ai.foresignal.provenance.LicenceTerms licenceTerms) {}
