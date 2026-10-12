package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.CustomSourceEntity;
import ai.foresignal.inbound.CustomSourceRepository;
import ai.foresignal.inbound.CustomSourceService;
import ai.foresignal.inbound.DynamicSourceFactory;
import ai.foresignal.inbound.IngestRecordLimiter;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers EDP providers from their manifests and manages their lifecycle (prompt 024,
 * docs/External-Data-Protocol.md §2 and §4.4).
 *
 * <p>{@link #register} validates the manifest ({@code 400 MANIFEST_INVALID} with every error,
 * {@code 400 PROTOCOL_UNSUPPORTED} for a minor above {@code foresignal.edp.max-minor}), stores it in
 * {@code external_source} and creates the custom source {@code ext-{source.id}} through
 * {@link CustomSourceService} with the descriptor {@link EdpDescriptorBuilder} derives, plus a pull
 * adapter when the manifest declares {@code transport.pull}. The source starts {@code pending}
 * (disabled) unless approved at once.
 *
 * <p>{@link #update} applies additive changes at once; a change to streams, kinds, item types,
 * modes, payload schemas or provider URLs returns an active source to {@code pending} (invariant
 * EXT-1); its records stay.
 *
 * <p>As a {@link DynamicSourceFactory} it rebuilds each provider's registration at startup from the
 * stored manifest.
 */
@Service
public class EdpManifestService implements DynamicSourceFactory {

    private static final Logger log = LoggerFactory.getLogger(EdpManifestService.class);

    /** The largest manifest fetched from a {@code manifestUrl}. */
    static final long MAX_MANIFEST_BYTES = 5L * 1024 * 1024;

    private final EdpManifestValidator validator;
    private final ExternalSourceRepository externals;
    private final CustomSourceService customSources;
    private final CustomSourceRepository customRows;
    private final EdpPullAdapters pullAdapters;
    private final EdpHttpClient http;
    private final SourceService sources;
    private final EdpStatusStore status;
    private final EdpSnapshotRepository snapshots;
    private final IngestRecordLimiter recordLimiter;
    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final Clock clock;
    private final Map<String, EdpManifestValidator.ManifestCheck> checks = new ConcurrentHashMap<>();

    /** Creates the service. */
    public EdpManifestService(EdpManifestValidator validator, ExternalSourceRepository externals,
            CustomSourceService customSources, CustomSourceRepository customRows, EdpPullAdapters pullAdapters,
            EdpHttpClient http, SourceService sources, EdpStatusStore status, EdpSnapshotRepository snapshots,
            IngestRecordLimiter recordLimiter, JdbcTemplate jdbc, JsonMapper json, Clock clock) {
        this.validator = validator;
        this.externals = externals;
        this.customSources = customSources;
        this.customRows = customRows;
        this.pullAdapters = pullAdapters;
        this.http = http;
        this.sources = sources;
        this.status = status;
        this.snapshots = snapshots;
        this.recordLimiter = recordLimiter;
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    /**
     * How a provider is being registered.
     *
     * @param approve true to create it active
     * @param via {@code operator} or {@code registration-token}
     * @param allowPrivateNetwork true when its URLs may be on a private network
     * @param actor who registered or approved it
     * @param providerIdPrefix the registration token's {@code sourceIdPrefix}, or null
     */
    public record RegisterOptions(boolean approve, String via, boolean allowPrivateNetwork, String actor,
            String providerIdPrefix) {}

    /**
     * A registered provider and its ingest key, shown once.
     *
     * @param source the stored provider
     * @param ingestKey the plaintext key
     */
    public record Registered(ExternalSourceEntity source, String ingestKey) {}

    // ---------- registration ----------

    /**
     * Fetches a manifest from {@code manifestUrl} once, through the URL guard.
     *
     * @throws ValidationException when it cannot be fetched or is not JSON; code
     *     {@code URL_NOT_ALLOWED} when the outbound URL policy refuses it (prompt 035)
     */
    public JsonNode fetchManifest(String manifestUrl, boolean allowPrivateNetwork) {
        URI uri;
        try {
            uri = URI.create(manifestUrl);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("manifestUrl", "is not a valid URL");
        }
        try {
            EdpHttpClient.EdpHttpResponse response = http.get(uri, Map.of("Accept", "application/json"),
                    allowPrivateNetwork, MAX_MANIFEST_BYTES);
            return json.readTree(response.body());
        } catch (ai.foresignal.source.spi.AdapterException e) {
            if (e.getCause() instanceof EdpUrlRefusedException refused) {
                // 035: the outbound URL policy refused the URL (or a redirect hop): 400 URL_NOT_ALLOWED.
                throw ai.foresignal.net.OutboundUrlPolicy.notAllowed(
                        java.util.List.of(new ai.foresignal.api.error.FieldError("manifestUrl", refused.getMessage())));
            }
            throw ValidationException.forField("manifestUrl", "could not be fetched: " + e.getMessage());
        } catch (RuntimeException e) {
            throw ValidationException.forField("manifestUrl", "did not return a JSON manifest");
        }
    }

    /**
     * Validates and registers a provider.
     *
     * @throws ValidationException {@code MANIFEST_INVALID} or {@code PROTOCOL_UNSUPPORTED}
     * @throws ConflictException {@code SOURCE_EXISTS} or {@code SOURCE_ID_NOT_ALLOWED}
     */
    @Transactional
    public Registered register(JsonNode manifest, RegisterOptions options) {
        EdpManifestModel model = requireValid(manifest).model();
        if (options.providerIdPrefix() != null && !model.providerId().startsWith(options.providerIdPrefix())) {
            throw new ConflictException(ErrorCodes.SOURCE_ID_NOT_ALLOWED, "This registration token only registers"
                    + " providers whose source.id starts with '" + options.providerIdPrefix() + "'.");
        }
        String sourceId = model.sourceId();
        if (externals.existsById(sourceId)) {
            throw new ConflictException(ErrorCodes.SOURCE_EXISTS, "A source with id '" + sourceId + "' already exists.");
        }
        ExternalSourceEntity ext = new ExternalSourceEntity(sourceId);
        String text = json.writeValueAsString(manifest);
        ext.setManifest(text, sha256(text));
        ext.setState(options.approve() ? ExternalSourceEntity.ACTIVE : ExternalSourceEntity.PENDING);
        ext.setRegisteredVia(options.via());
        ext.setAllowPrivateNetwork(options.allowPrivateNetwork());
        if (options.approve()) {
            ext.setApproval(options.actor(), clock.instant());
        }
        ext.setCreatedAt(clock.instant());
        ext.setUpdatedAt(ext.getCreatedAt());
        externals.save(ext);

        CustomSourceEntity row = new CustomSourceEntity(sourceId);
        describe(row, model);
        SourceDescriptor descriptor = EdpDescriptorBuilder.build(model, row.getRateLimitPerMinute());
        SourceAdapter adapter = model.pull() == null ? null : pullAdapters.create(descriptor);
        CustomSourceService.Created created = customSources.createExternal(row, descriptor, adapter, options.approve());
        log.info("Registered EDP provider {} via {} ({})", sourceId, options.via(), ext.getState());
        return new Registered(ext, created.ingestKey());
    }

    /**
     * Applies a new manifest to a registered provider.
     *
     * @return the provider and the breaking changes found (empty for an additive update)
     * @throws ValidationException {@code MANIFEST_INVALID}, {@code PROTOCOL_UNSUPPORTED} or
     *     {@code SOURCE_MISMATCH}
     */
    @Transactional
    public Updated update(String sourceId, JsonNode manifest) {
        ExternalSourceEntity ext = require(sourceId);
        EdpManifestModel model = requireValid(manifest).model();
        if (!model.sourceId().equals(sourceId)) {
            throw new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.SOURCE_MISMATCH,
                    "The manifest describes '" + model.sourceId() + "', not '" + sourceId + "'.");
        }
        List<String> breaking = breakingChanges(model(ext), model);
        String text = json.writeValueAsString(manifest);
        ext.setManifest(text, sha256(text));
        ext.setUpdatedAt(clock.instant());
        boolean wasActive = ExternalSourceEntity.ACTIVE.equals(ext.getState());
        if (!breaking.isEmpty() && wasActive) {
            ext.setState(ExternalSourceEntity.PENDING);
        }
        externals.save(ext);
        CustomSourceEntity row = customRows.findById(sourceId).orElseThrow(() -> unknown(sourceId));
        describe(row, model);
        SourceDescriptor descriptor = EdpDescriptorBuilder.build(model, row.getRateLimitPerMinute());
        SourceAdapter adapter = model.pull() == null ? null : pullAdapters.create(descriptor);
        customSources.redefineExternal(row, descriptor, adapter);
        if (!breaking.isEmpty() && wasActive) {
            sources.setDynamicEnabled(sourceId, false);
        }
        log.info("Updated the manifest of {} ({} breaking change(s))", sourceId, breaking.size());
        return new Updated(ext, breaking);
    }

    /**
     * The result of a manifest update.
     *
     * @param source the provider
     * @param breakingChanges what returned it to pending; empty for an additive update
     */
    public record Updated(ExternalSourceEntity source, List<String> breakingChanges) {}

    /** Activates a provider (enables it). */
    @Transactional
    public ExternalSourceEntity approve(String sourceId, String actor) {
        ExternalSourceEntity ext = require(sourceId);
        ext.setState(ExternalSourceEntity.ACTIVE);
        ext.setApproval(actor, clock.instant());
        ext.setUpdatedAt(clock.instant());
        externals.save(ext);
        sources.setDynamicEnabled(sourceId, true);
        return ext;
    }

    /** Suspends a provider: pushes are refused and pulls stop; records stay. */
    @Transactional
    public ExternalSourceEntity suspend(String sourceId) {
        ExternalSourceEntity ext = require(sourceId);
        ext.setState(ExternalSourceEntity.SUSPENDED);
        ext.setUpdatedAt(clock.instant());
        externals.save(ext);
        sources.setDynamicEnabled(sourceId, false);
        return ext;
    }

    /** Deletes a provider and its custom source; records age out through retention. */
    @Transactional
    public void delete(String sourceId) {
        ExternalSourceEntity ext = require(sourceId);
        for (EdpSnapshotEntity snapshot : snapshots.findBySourceId(sourceId)) {
            jdbc.update("delete from edp_snapshot_stage where snapshot_key = ?", snapshot.getKey());
            snapshots.delete(snapshot);
        }
        externals.delete(ext);
        if (customRows.existsById(sourceId)) {
            customSources.delete(sourceId);
        }
        status.forget(sourceId);
        recordLimiter.forget(sourceId);
        checks.remove(ext.getManifestSha256());
    }

    /** Records when an item was last accepted (outside the request transaction). */
    public void touchLastItem(String sourceId, java.time.Instant at) {
        jdbc.update("update external_source set last_item_at = ? where source_id = ?", java.sql.Timestamp.from(at),
                sourceId);
    }

    /**
     * Records a pull: the cursor to resume from, when it ran and its error (null on success).
     *
     * @param cursor the cursor to store; null keeps the stored one
     */
    public void recordPull(String sourceId, String cursor, java.time.Instant at, String error) {
        String trimmed = error == null || error.length() <= 2000 ? error : error.substring(0, 2000);
        if (cursor != null) {
            jdbc.update("update external_source set pull_cursor = ?, last_poll_at = ?, last_pull_error = ?"
                    + " where source_id = ?", cursor, java.sql.Timestamp.from(at), trimmed, sourceId);
        } else {
            jdbc.update("update external_source set last_poll_at = ?, last_pull_error = ? where source_id = ?",
                    java.sql.Timestamp.from(at), trimmed, sourceId);
        }
    }

    // ---------- reads ----------

    /**
     * The provider {@code sourceId}.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_EXTERNAL_SOURCE}
     */
    @Transactional(readOnly = true)
    public ExternalSourceEntity require(String sourceId) {
        return externals.findById(sourceId == null ? "" : sourceId).orElseThrow(() -> unknown(sourceId));
    }

    /** Every provider, optionally in one state, ordered by id. */
    @Transactional(readOnly = true)
    public List<ExternalSourceEntity> list(String state) {
        return state == null ? externals.findAllByOrderBySourceId() : externals.findByStateOrderBySourceId(state);
    }

    /** The stored manifest of {@code ext}. */
    public EdpManifestModel model(ExternalSourceEntity ext) {
        return check(ext).model();
    }

    /** The compiled payload schemas of {@code ext}, keyed by stream id. */
    public Map<String, CompiledPayloadSchema> schemas(ExternalSourceEntity ext) {
        return check(ext).payloadSchemas();
    }

    /** The cached validation of a stored manifest (it passed when stored). */
    EdpManifestValidator.ManifestCheck check(ExternalSourceEntity ext) {
        return checks.computeIfAbsent(ext.getManifestSha256(), sha -> {
            EdpManifestValidator.ManifestCheck check = validator.validate(json.readTree(ext.getManifest()));
            if (!check.valid()) {
                throw new IllegalStateException("The stored manifest of " + ext.getSourceId()
                        + " no longer validates: " + check.errors());
            }
            return check;
        });
    }

    /** Validates a manifest without storing anything. */
    public EdpManifestValidator.ManifestCheck validate(JsonNode manifest) {
        return validator.validate(manifest);
    }

    // ---------- DynamicSourceFactory ----------

    @Override
    public boolean handles(CustomSourceEntity row) {
        return row.isExternal();
    }

    @Override
    public Registration build(CustomSourceEntity row) {
        ExternalSourceEntity ext = require(row.getSourceId());
        EdpManifestModel model = model(ext);
        SourceDescriptor descriptor = EdpDescriptorBuilder.build(model, row.getRateLimitPerMinute());
        return new Registration(descriptor, model.pull() == null ? null : pullAdapters.create(descriptor));
    }

    // ---------- helpers ----------

    /**
     * The changes between two manifests that return a provider to pending: added or removed streams,
     * and per stream a changed kind, item type, mode, update style, geometry or payload schema; and a
     * changed pull or search URL (a new address the operator has not reviewed).
     */
    public static List<String> breakingChanges(EdpManifestModel before, EdpManifestModel after) {
        List<String> changes = new ArrayList<>();
        Set<String> beforeIds = new LinkedHashSet<>();
        before.streams().forEach(s -> beforeIds.add(s.id()));
        Set<String> afterIds = new LinkedHashSet<>();
        after.streams().forEach(s -> afterIds.add(s.id()));
        for (String id : afterIds) {
            if (!beforeIds.contains(id)) {
                changes.add("stream " + id + " added");
            }
        }
        for (String id : beforeIds) {
            if (!afterIds.contains(id)) {
                changes.add("stream " + id + " removed");
            }
        }
        for (EdpManifestModel.Stream b : before.streams()) {
            EdpManifestModel.Stream a = after.stream(b.id());
            if (a == null) {
                continue;
            }
            compare(changes, b.id(), "kind", b.kind(), a.kind());
            compare(changes, b.id(), "itemType", b.itemType(), a.itemType());
            compare(changes, b.id(), "mode", b.mode(), a.mode());
            compare(changes, b.id(), "update", b.update(), a.update());
            compare(changes, b.id(), "geometry", b.geometry(), a.geometry());
            compare(changes, b.id(), "payloadSchema", b.payloadSchema(), a.payloadSchema());
        }
        String pullBefore = before.pull() == null ? null : before.pull().itemsUrl();
        String pullAfter = after.pull() == null ? null : after.pull().itemsUrl();
        if (!Objects.equals(pullBefore, pullAfter)) {
            changes.add("transport.pull.itemsUrl changed");
        }
        String searchBefore = before.search() == null ? null : before.search().url();
        String searchAfter = after.search() == null ? null : after.search().url();
        if (!Objects.equals(searchBefore, searchAfter)) {
            changes.add("transport.search.url changed");
        }
        return changes;
    }

    private static void compare(List<String> changes, String stream, String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add("stream " + stream + " " + field + " changed");
        }
    }

    private EdpManifestValidator.ManifestCheck requireValid(JsonNode manifest) {
        if (manifest == null || !manifest.isObject()) {
            throw new InvalidInputException("The manifest must be a JSON object.");
        }
        EdpManifestValidator.ManifestCheck check = validator.validate(manifest);
        List<FieldError> errors = check.errors().stream().map(e -> new FieldError(e.path(), e.message())).toList();
        if (check.protocolUnsupported()) {
            throw new ValidationException(ErrorCodes.PROTOCOL_UNSUPPORTED,
                    "The manifest's protocol minor version is above what this instance supports.", errors);
        }
        if (!check.valid()) {
            throw new ValidationException(ErrorCodes.MANIFEST_INVALID, "The manifest is not valid.", errors);
        }
        return check;
    }

    /** Copies the manifest's identity into the custom-source row. */
    private static void describe(CustomSourceEntity row, EdpManifestModel model) {
        row.setName(model.name().length() <= 200 ? model.name() : model.name().substring(0, 200));
        row.setDescription(model.description());
        List<String> kinds = new ArrayList<>(new LinkedHashSet<>(model.streams().stream()
                .map(EdpManifestModel.Stream::kind).toList()));
        row.setKinds(kinds);
        row.setLicence(model.licenceId());
        row.setPrivateSource(model.privateSource());
        row.setAttribution(model.attribution());
    }

    static ForesignalException unknown(String sourceId) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_EXTERNAL_SOURCE,
                "There is no external source '" + sourceId + "'.");
    }

    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
