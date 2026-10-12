package ai.foresignal.inbound;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Creates, edits, rotates and deletes custom push sources, and registers external (EDP) providers
 * the same way (Architecture §16, prompt 024).
 *
 * <p><strong>create</strong> validates the input, builds the descriptor, runs the registry's
 * descriptor validation and {@link IngestPolicyValidator}, generates the ingest key (returned once),
 * persists the row, registers the source with {@link SourceRegistry#registerDynamic} and creates its
 * settings enabled.
 *
 * <p><strong>update</strong> keeps the id; removing a kind with current heads is
 * {@code 409 KIND_IN_USE}, changing {@code observedAtMode} while the source has heads is
 * {@code 409 OBSERVED_AT_MODE_LOCKED}, and a SAMPLED or LATEST effective policy left without
 * significant fields is {@code 400 INGEST_POLICY_INVALID}. The source is then re-registered
 * ({@code REDEFINED}) and its settings event published, so the writer rebuilds its profile and runs
 * the lazy hash-recipe migration (Core-Data-Model §4) before the next batch when the volatile or
 * significant fields changed.
 *
 * <p><strong>delete</strong> unregisters the source and deletes its row and settings; its records
 * age out through retention.
 */
@Service
public class CustomSourceService {

    private static final Logger log = LoggerFactory.getLogger(CustomSourceService.class);

    private final CustomSourceRepository repository;
    private final CustomSourceInputReader reader;
    private final CustomSourceVerifiers verifiers;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final IngestKeys keys;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public CustomSourceService(CustomSourceRepository repository, CustomSourceInputReader reader,
            CustomSourceVerifiers verifiers, SourceRegistry registry, SourceService sources, IngestKeys keys,
            JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.repository = repository;
        this.reader = reader;
        this.verifiers = verifiers;
        this.registry = registry;
        this.sources = sources;
        this.keys = keys;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * A created source and its ingest key, shown once.
     *
     * @param source the stored row
     * @param ingestKey the plaintext key
     */
    public record Created(CustomSourceEntity source, String ingestKey) {}

    // ---------- reads ----------

    /** Every custom source (not external providers), ordered by id. */
    @Transactional(readOnly = true)
    public List<CustomSourceEntity> list() {
        return repository.findByOriginOrderBySourceId(CustomSourceEntity.ORIGIN_CUSTOM);
    }

    /**
     * One custom source.
     *
     * @throws NotFoundException when there is no such custom source
     */
    @Transactional(readOnly = true)
    public CustomSourceEntity get(String id) {
        return repository.findById(id)
                .filter(row -> !row.isExternal())
                .orElseThrow(() -> NotFoundException.of("Custom source", id));
    }

    /** Any {@code custom_source} row, custom or external. */
    @Transactional(readOnly = true)
    public Optional<CustomSourceEntity> find(String id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    /** The verifier configuration of {@code row} per format. */
    public Map<String, VerifierConfig> verifierConfigs(CustomSourceEntity row) {
        return verifiers.configs(row);
    }

    /** The formats whose verifier secret is stored. */
    public Set<String> secretsSet(CustomSourceEntity row) {
        return verifiers.secrets(row).keySet();
    }

    // ---------- create ----------

    /**
     * Creates a custom source from a {@code CustomSourceInput} body.
     *
     * @throws ValidationException on invalid input or descriptor ({@code INGEST_POLICY_INVALID} for
     *     a policy problem)
     * @throws ConflictException {@code SOURCE_EXISTS} when the id is taken by any source
     */
    @Transactional
    public Created create(JsonNode body) {
        CustomSourceDefinition definition = reader.read(body, null, Set.of());
        if (registry.contains(definition.id()) || repository.existsById(definition.id())) {
            throw new ConflictException(ErrorCodes.SOURCE_EXISTS,
                    "A source with id '" + definition.id() + "' already exists.");
        }
        CustomSourceEntity row = new CustomSourceEntity(definition.id());
        apply(row, definition);
        verifiers.store(row, definition.verifiers(), definition.verifierSecrets());
        SourceDescriptor descriptor = CustomSourceDescriptors.toDescriptor(row, json);
        validate(descriptor, IngestPolicyValidator.applyDefaults(descriptor.defaultIngestPolicy(), descriptor));

        String key = keys.generate();
        row.setIngestKey(keys.hash(key), IngestKeys.prefixOf(key));
        row.setCreatedAt(clock.instant());
        row.setUpdatedAt(row.getCreatedAt());
        repository.save(row);
        registry.registerDynamic(descriptor);
        sources.createDynamicSettings(descriptor, true);
        log.info("Created custom source {} with kinds {}", row.getSourceId(), row.getKinds());
        return new Created(row, key);
    }

    /**
     * Registers an external (EDP) provider as a dynamic source with its own descriptor and, for a
     * pull provider, its polling adapter (prompt 024). The row carries the ingest key and caps.
     *
     * @param row the new row, with {@code origin = external} and its name and kinds set
     * @param descriptor the descriptor built from the manifest
     * @param adapter the pull adapter, or null for a push-only provider
     * @param enabled true when the provider starts approved
     * @return the row and its ingest key
     * @throws ConflictException {@code SOURCE_EXISTS} when the id is taken
     */
    @Transactional
    public Created createExternal(CustomSourceEntity row, SourceDescriptor descriptor, SourceAdapter adapter,
            boolean enabled) {
        if (registry.contains(row.getSourceId()) || repository.existsById(row.getSourceId())) {
            throw new ConflictException(ErrorCodes.SOURCE_EXISTS,
                    "A source with id '" + row.getSourceId() + "' already exists.");
        }
        validate(descriptor, IngestPolicyValidator.applyDefaults(descriptor.defaultIngestPolicy(), descriptor));
        row.setOrigin(CustomSourceEntity.ORIGIN_EXTERNAL);
        String key = keys.generate();
        row.setIngestKey(keys.hash(key), IngestKeys.prefixOf(key));
        row.setCreatedAt(clock.instant());
        row.setUpdatedAt(row.getCreatedAt());
        repository.save(row);
        registry.registerDynamic(descriptor, adapter);
        sources.createDynamicSettings(descriptor, enabled);
        log.info("Registered external source {} ({})", row.getSourceId(), enabled ? "active" : "pending");
        return new Created(row, key);
    }

    // ---------- update ----------

    /**
     * Replaces a custom source's definition.
     *
     * @throws NotFoundException when there is no such custom source
     * @throws ValidationException on invalid input, a changed id, or {@code INGEST_POLICY_INVALID}
     * @throws ConflictException {@code KIND_IN_USE} or {@code OBSERVED_AT_MODE_LOCKED}
     */
    @Transactional
    public CustomSourceEntity update(String id, JsonNode body) {
        CustomSourceEntity row = get(id);
        CustomSourceDefinition definition = reader.read(body, verifiers.configs(row), verifiers.secrets(row).keySet());
        if (!Objects.equals(definition.id(), id)) {
            throw ValidationException.forField("id", "cannot change (was " + id + ")");
        }
        Set<String> removed = new LinkedHashSet<>(row.getKinds());
        removed.removeAll(definition.kinds());
        for (String kind : removed) {
            if (hasCurrentHeads(id, kind)) {
                throw new ConflictException(ErrorCodes.KIND_IN_USE,
                        "Kind '" + kind + "' still has current records in " + id + "; it cannot be removed.");
            }
        }
        if (!row.getObservedAtMode().equals(definition.observedAtMode()) && hasHeads(id)) {
            throw new ConflictException(ErrorCodes.OBSERVED_AT_MODE_LOCKED,
                    "observedAtMode can change only while " + id + " has no records.");
        }
        apply(row, definition);
        verifiers.store(row, definition.verifiers(), definition.verifierSecrets());
        SourceDescriptor descriptor = CustomSourceDescriptors.toDescriptor(row, json);
        IngestPolicy effective = sources.ingestPolicyOverride(id)
                .orElse(descriptor.defaultIngestPolicy());
        validate(descriptor, IngestPolicyValidator.applyDefaults(effective, descriptor));
        row.setUpdatedAt(clock.instant());
        repository.save(row);
        registry.redefineDynamic(descriptor, null);
        sources.refreshDynamicSettings(descriptor);
        log.info("Updated custom source {}", id);
        return row;
    }

    /**
     * Redefines a registered external provider after a manifest change (prompt 024).
     *
     * @throws NotFoundException when the source is not registered
     */
    @Transactional
    public void redefineExternal(CustomSourceEntity row, SourceDescriptor descriptor, SourceAdapter adapter) {
        IngestPolicy effective = sources.ingestPolicyOverride(row.getSourceId())
                .orElse(descriptor.defaultIngestPolicy());
        validate(descriptor, IngestPolicyValidator.applyDefaults(effective, descriptor));
        row.setUpdatedAt(clock.instant());
        repository.save(row);
        registry.redefineDynamic(descriptor, adapter);
        sources.refreshDynamicSettings(descriptor);
    }

    // ---------- keys and delete ----------

    /**
     * Replaces a source's ingest key; the old key stops working at once.
     *
     * @return the new key, shown once
     * @throws NotFoundException when there is no such source
     */
    @Transactional
    public String rotateKey(String id) {
        CustomSourceEntity row = repository.findById(id).orElseThrow(() -> NotFoundException.of("Custom source", id));
        String key = keys.generate();
        row.setIngestKey(keys.hash(key), IngestKeys.prefixOf(key));
        row.setUpdatedAt(clock.instant());
        repository.save(row);
        return key;
    }

    /**
     * Deletes a custom source or external provider: unregisters it and deletes its row, settings
     * and credentials. Records age out through retention.
     *
     * @throws NotFoundException when there is no such source
     */
    @Transactional
    public void delete(String id) {
        CustomSourceEntity row = repository.findById(id).orElseThrow(() -> NotFoundException.of("Custom source", id));
        repository.delete(row);
        sources.deleteDynamicSettings(id);
        if (registry.contains(id)) {
            registry.unregisterDynamic(id);
        }
        log.info("Deleted {} source {}", row.getOrigin(), id);
    }

    /** Records a successful push's time on the row (best effort, outside the request transaction). */
    public void touchLastIngest(String id) {
        jdbc.update("update custom_source set last_ingest_at = ? where source_id = ?",
                java.sql.Timestamp.from(clock.instant()), id);
    }

    // ---------- helpers ----------

    private void apply(CustomSourceEntity row, CustomSourceDefinition d) {
        row.setName(d.name());
        row.setDescription(d.description());
        row.setKinds(d.kinds());
        row.setAttribution(d.attribution());
        row.setRecordTtl(d.recordTtl());
        row.setTileProperties(d.tileProperties());
        row.setSearchFields(d.searchFields());
        row.setEmbedFields(d.embedFields());
        row.setRankField(d.rankField());
        row.setRateLimitPerMinute(d.rateLimitPerMinute());
        row.setObservedAtMode(d.observedAtMode());
        row.setTtlAnchor(d.ttlAnchor());
        row.setVolatileFields(d.volatileFields());
        row.setSignificantFields(d.significantFields());
        row.setTitleField(d.titleField());
        row.setFieldSpecs(d.fieldSpecs().isEmpty() ? null : json.writeValueAsString(d.fieldSpecs()));
        row.setPrivateSource(d.privateSource());
        row.setLicence(d.licence());
        row.setRecordsPerMinute(d.recordsPerMinute());
        row.setFormats(d.formats() == null || d.formats().isEmpty() ? null : json.writeValueAsString(d.formats()));
    }

    /** Registry validation, then the ingest policy rules (Core-Data-Model §5.1). */
    private static void validate(SourceDescriptor descriptor, IngestPolicy policy) {
        List<String> problems = DescriptorValidator.problems(descriptor, null);
        if (descriptor.mode() == ai.foresignal.source.spi.Mode.POLLING) {
            // A pulled provider is checked with its adapter by the registry itself.
            problems = new ArrayList<>(problems);
            problems.removeIf(p -> p.contains("has no adapter implementation"));
        }
        if (!problems.isEmpty()) {
            throw new ValidationException("The source definition is not valid: " + String.join("; ", problems));
        }
        List<String> policyProblems = IngestPolicyValidator.problems(policy, descriptor, "ingestPolicy");
        if (!policyProblems.isEmpty()) {
            List<FieldError> errors = policyProblems.stream().map(p -> new FieldError("ingestPolicy", p)).toList();
            throw new ValidationException(ErrorCodes.INGEST_POLICY_INVALID,
                    "The effective ingest policy is not valid for this definition.", errors);
        }
    }

    private boolean hasCurrentHeads(String id, String kind) {
        return !jdbc.queryForList(
                "select 1 from record_head where source_id = ? and kind = ? and not tombstone limit 1",
                Integer.class, id, kind).isEmpty();
    }

    private boolean hasHeads(String id) {
        return !jdbc.queryForList("select 1 from record_head where source_id = ? limit 1", Integer.class, id)
                .isEmpty();
    }
}
