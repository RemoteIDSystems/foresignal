package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.Actors;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceView;
import ai.foresignal.source.spi.SourceDescriptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The External Data Protocol endpoints of prompt 024 (tag {@code External}): the protocol and its
 * schemas (open), provider registration and lifecycle (admin), self-registration (registration
 * token), pushes and manifest updates (the provider's ingest key or admin), registration tokens
 * (admin) and dry-run validation (agent).
 */
@RestController
@Tag(name = "External")
public class ExternalSourcesController {

    private final EdpManifestService manifests;
    private final EdpIngestService ingest;
    private final RegistrationTokenService tokens;
    private final EdpSelfRegistration selfRegistration;
    private final EdpStatusStore status;
    private final EdpProperties properties;
    private final SourceRegistry registry;
    private final SourceService sources;
    private final EdpSnapshotService snapshots;
    private final ai.foresignal.inbound.PushIngestService pushes;
    private final java.time.Clock clock;
    private final JsonMapper json;

    /** Creates the controller. */
    public ExternalSourcesController(EdpManifestService manifests, EdpIngestService ingest,
            RegistrationTokenService tokens, EdpSelfRegistration selfRegistration, EdpStatusStore status,
            EdpProperties properties, SourceRegistry registry, SourceService sources, EdpSnapshotService snapshots,
            ai.foresignal.inbound.PushIngestService pushes, java.time.Clock clock, JsonMapper json) {
        this.manifests = manifests;
        this.ingest = ingest;
        this.tokens = tokens;
        this.selfRegistration = selfRegistration;
        this.status = status;
        this.properties = properties;
        this.registry = registry;
        this.sources = sources;
        this.snapshots = snapshots;
        this.pushes = pushes;
        this.clock = clock;
        this.json = json;
    }

    // ---------- protocol ----------

    /** {@code getEdpProtocol}. */
    @GetMapping("/v1/external/protocol")
    @Operation(operationId = "getEdpProtocol", tags = "External")
    public Map<String, Object> getEdpProtocol() {
        List<String> supported = new ArrayList<>();
        for (int minor = 0; minor <= properties.maxMinor(); minor++) {
            supported.add("foresignal-edp/1." + minor);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("protocol", EdpProtocol.VERSION);
        out.put("supported", supported);
        out.put("itemTypes", EdpProtocol.ITEM_TYPES);
        out.put("modes", EdpProtocol.MODES);
        out.put("schemas", EdpProtocol.SCHEMA_NAMES);
        out.put("documentation", "https://foresignal.ai/protocol/edp-1.0/");
        out.put("maxMinor", properties.maxMinor());
        return out;
    }

    /** {@code getEdpSchema}: an exported JSON Schema 2020-12 document. */
    @GetMapping("/v1/external/protocol/schemas/{name}")
    @Operation(operationId = "getEdpSchema", tags = "External")
    public ResponseEntity<byte[]> getEdpSchema(@PathVariable String name) {
        if (!EdpProtocol.SCHEMA_NAMES.contains(name)) {
            throw new NotFoundException("There is no EDP schema '" + name + "'.");
        }
        ClassPathResource resource = new ClassPathResource("edp/schemas/" + name + ".schema.json");
        try (InputStream in = resource.getInputStream()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/schema+json"))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(in.readAllBytes());
        } catch (IOException e) {
            throw new NotFoundException("The EDP schema '" + name + "' is not available.");
        }
    }

    // ---------- providers ----------

    /** {@code listExternalSources}. */
    @GetMapping("/v1/external/sources")
    @Operation(operationId = "listExternalSources", tags = "External")
    public Map<String, Object> listExternalSources(@RequestParam(required = false) String state,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "100") int limit) {
        if (state != null && !List.of("pending", "active", "suspended").contains(state)) {
            throw ValidationException.forField("state", "must be pending, active or suspended");
        }
        if (limit < 1 || limit > 500) {
            throw ValidationException.forField("limit", "must be from 1 to 500");
        }
        List<Map<String, Object>> items = new ArrayList<>();
        String next = null;
        for (ExternalSourceEntity ext : manifests.list(state)) {
            if (cursor != null && ext.getSourceId().compareTo(cursor) <= 0) {
                continue;
            }
            if (items.size() == limit) {
                next = items.get(items.size() - 1).get("sourceId").toString();
                break;
            }
            items.add(view(ext));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    /** {@code registerExternalSource}: by manifest or manifest URL; 201 with the ingest key once. */
    @PostMapping("/v1/external/sources")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "registerExternalSource", tags = "External")
    public Map<String, Object> registerExternalSource(@RequestBody JsonNode body, HttpServletRequest request) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        boolean allowPrivate = body.path("allowPrivateNetwork").asBoolean(false);
        JsonNode manifest = body.get("manifest");
        JsonNode url = body.get("manifestUrl");
        boolean hasManifest = manifest != null && !manifest.isNull();
        boolean hasUrl = url != null && !url.isNull();
        if (hasManifest == hasUrl) {
            throw ValidationException.forField("manifest", "give exactly one of manifest and manifestUrl");
        }
        if (hasUrl) {
            manifest = manifests.fetchManifest(url.asString(), allowPrivate);
        }
        EdpManifestService.Registered registered = manifests.register(manifest, new EdpManifestService.RegisterOptions(
                body.path("approve").asBoolean(false), ExternalSourceEntity.VIA_OPERATOR, allowPrivate,
                Actors.of(request), null));
        return created(registered);
    }

    /** {@code getExternalSource}. */
    @GetMapping("/v1/external/sources/{sourceId}")
    @Operation(operationId = "getExternalSource", tags = "External")
    public Map<String, Object> getExternalSource(@PathVariable String sourceId) {
        return view(manifests.require(sourceId));
    }

    /** {@code deleteExternalSource}: 204; records age out through retention. */
    @DeleteMapping("/v1/external/sources/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteExternalSource", tags = "External")
    public void deleteExternalSource(@PathVariable String sourceId) {
        manifests.delete(sourceId);
    }

    /** {@code updateExternalSourceManifest}: the provider's ingest key or an admin token. */
    @PutMapping("/v1/external/sources/{sourceId}/manifest")
    @Operation(operationId = "updateExternalSourceManifest", tags = "External")
    public Map<String, Object> updateExternalSourceManifest(@PathVariable String sourceId, @RequestBody JsonNode body,
            HttpServletRequest request) {
        IngestCaller caller = IngestCaller.of(request).orElseThrow(() -> new ForesignalException(
                HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "This endpoint needs the source's ingest key."));
        if (!caller.mayPushTo(sourceId)) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "The presented ingest key does not belong to this source.");
        }
        EdpManifestService.Updated updated = manifests.update(sourceId, body);
        Map<String, Object> out = view(updated.source());
        out.put("breakingChanges", updated.breakingChanges());
        return out;
    }

    /** {@code approveExternalSource}. */
    @PostMapping("/v1/external/sources/{sourceId}/approve")
    @Operation(operationId = "approveExternalSource", tags = "External")
    public Map<String, Object> approveExternalSource(@PathVariable String sourceId, HttpServletRequest request) {
        return view(manifests.approve(sourceId, Actors.of(request)));
    }

    /** {@code suspendExternalSource}. */
    @PostMapping("/v1/external/sources/{sourceId}/suspend")
    @Operation(operationId = "suspendExternalSource", tags = "External")
    public Map<String, Object> suspendExternalSource(@PathVariable String sourceId) {
        return view(manifests.suspend(sourceId));
    }

    /** {@code pushExternalItems}: a JSON batch or NDJSON items. */
    @PostMapping("/v1/external/sources/{sourceId}/items")
    @Operation(operationId = "pushExternalItems", tags = "External")
    public Map<String, Object> pushExternalItems(@PathVariable String sourceId, HttpServletRequest request) {
        return ingest.push(sourceId, request);
    }

    /**
     * {@code allowExternalSnapshot}: releases a snapshot deletion held above
     * {@code maxAbsentRatio}, once (EDP §3.2).
     */
    @PostMapping("/v1/external/sources/{sourceId}/snapshots/{snapshotId}/allow")
    @Operation(operationId = "allowExternalSnapshot", tags = "External")
    public Map<String, Object> allowExternalSnapshot(@PathVariable String sourceId, @PathVariable String snapshotId,
            HttpServletRequest request) {
        ExternalSourceEntity ext = manifests.require(sourceId);
        java.time.Instant now = clock.instant();
        List<ai.foresignal.source.spi.NormalizedRecord> tombstones =
                snapshots.releaseHeld(sourceId, snapshotId, manifests.model(ext), now);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("snapshotId", snapshotId);
        out.put("removed", tombstones.size());
        if (tombstones.isEmpty()) {
            out.put("outcome", ai.foresignal.inbound.IngestOutcomeView.of(null, 0, List.of()));
            return out;
        }
        List<Integer> indexes = new ArrayList<>();
        tombstones.forEach(t -> indexes.add(-1));
        ai.foresignal.inbound.format.ParsedIngest parsed =
                new ai.foresignal.inbound.format.ParsedIngest(tombstones, indexes, List.of(), tombstones.size());
        ai.foresignal.inbound.PushIngestService.PushResult result = pushes.submit(registry.descriptor(sourceId),
                new ai.foresignal.inbound.PushIngestService.PushRequest(sourceId, Actors.of(request), parsed, now, 0,
                        null, request.getRequestURI()));
        out.put("outcome", result.view());
        return out;
    }

    // ---------- registration tokens ----------

    /** {@code listRegistrationTokens}: unexpired tokens, never the secret. */
    @GetMapping("/v1/external/registration-tokens")
    @Operation(operationId = "listRegistrationTokens", tags = "External")
    public List<Map<String, Object>> listRegistrationTokens() {
        return tokens.list().stream().map(ExternalSourcesController::tokenView).toList();
    }

    /** {@code createRegistrationToken}: 201 with the token once. */
    @PostMapping("/v1/external/registration-tokens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRegistrationToken", tags = "External")
    public Map<String, Object> createRegistrationToken(@RequestBody JsonNode body) {
        RegistrationTokenService.Created created = tokens.create(body);
        Map<String, Object> out = tokenView(created.entity());
        out.put("token", created.token());
        return out;
    }

    /** {@code deleteRegistrationToken}: 204. */
    @DeleteMapping("/v1/external/registration-tokens/{tokenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRegistrationToken", tags = "External")
    public void deleteRegistrationToken(@PathVariable String tokenId) {
        tokens.delete(tokenId);
    }

    /** {@code selfRegisterExternalSource}: the registration token as bearer; created pending. */
    @PostMapping("/v1/external/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "selfRegisterExternalSource", tags = "External")
    public Map<String, Object> selfRegisterExternalSource(@RequestBody JsonNode manifest, HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)
                ? header.substring(7).strip()
                : null;
        return created(selfRegistration.register(token, manifest));
    }

    /** {@code validateEdp}: a dry run; nothing is stored. */
    @PostMapping("/v1/external/validate")
    @Operation(operationId = "validateEdp", tags = "External")
    public Map<String, Object> validateEdp(@RequestBody JsonNode body) {
        return ingest.validate(body);
    }

    // ---------- views ----------

    private Map<String, Object> created(EdpManifestService.Registered registered) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", view(registered.source()));
        out.put("ingestKey", registered.ingestKey());
        return out;
    }

    /** The {@code ExternalSource} view. */
    Map<String, Object> view(ExternalSourceEntity ext) {
        EdpManifestModel manifest = manifests.model(ext);
        EdpStats stats = status.read(ext);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sourceId", ext.getSourceId());
        out.put("state", ext.getState());
        out.put("registeredVia", ext.getRegisteredVia());
        out.put("manifest", json.readTree(ext.getManifest()));
        out.put("manifestSha256", ext.getManifestSha256());
        SourceDescriptor descriptor = registry.find(ext.getSourceId()).map(SourceRegistry.Registration::descriptor)
                .orElse(null);
        if (descriptor != null && descriptor.access() != null) {
            out.put("access", descriptor.access());
        }
        out.put("egress", descriptor == null ? null : descriptor.egress().wireValue());
        out.put("allowPrivateNetwork", ext.isAllowPrivateNetwork());
        out.put("approvedBy", ext.getApprovedBy());
        out.put("approvedAt", ext.getApprovedAt());
        out.put("createdAt", ext.getCreatedAt());
        out.put("lastItemAt", ext.getLastItemAt());
        if (manifest.pull() != null) {
            Map<String, Object> pull = new LinkedHashMap<>();
            pull.put("cursor", ext.getPullCursor());
            pull.put("lastPollAt", ext.getLastPollAt());
            pull.put("lastError", ext.getLastPullError());
            out.put("pull", pull);
        } else {
            out.put("pull", null);
        }
        List<Map<String, Object>> streams = new ArrayList<>();
        Map<String, Object> presentation = new LinkedHashMap<>();
        for (EdpManifestModel.Stream stream : manifest.streams()) {
            EdpStats.StreamStats s = stats.streams().get(stream.id());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("streamId", stream.id());
            entry.put("mode", stream.mode());
            entry.put("kind", stream.kind());
            entry.put("items", s == null ? 0 : s.items());
            entry.put("rejected", s == null ? 0 : s.rejected());
            entry.put("lastItemAt", s == null ? null : s.lastItemAt());
            entry.put("bindings", 0);
            streams.add(entry);
            if (!stream.atomic() && stream.presentation() != null) {
                presentation.put(stream.id(), stream.presentation());
            }
        }
        out.put("streams", streams);
        out.put("presentation", presentation);
        out.put("lastValidationErrors", stats.lastValidationErrors());
        String health = null;
        if (descriptor != null) {
            SourceView view = sources.view(ext.getSourceId());
            health = view.health() == null || view.health().status() == null ? null : view.health().status().value();
        }
        out.put("health", health);
        return out;
    }

    private static Map<String, Object> tokenView(RegistrationTokenEntity token) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", token.getId());
        out.put("name", token.getName());
        out.put("prefix", token.getPrefix());
        out.put("expiresAt", token.getExpiresAt());
        out.put("maxUses", token.getMaxUses());
        out.put("uses", token.getUses());
        out.put("sourceIdPrefix", token.getSourceIdPrefix());
        out.put("createdAt", token.getCreatedAt());
        return out;
    }
}
