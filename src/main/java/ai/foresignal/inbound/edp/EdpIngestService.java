package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.inbound.IngestBodyReader;
import ai.foresignal.inbound.IngestOutcomeView;
import ai.foresignal.inbound.IngestUrls;
import ai.foresignal.inbound.InboundRequests;
import ai.foresignal.inbound.PushIngestService;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.Rejection;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.source.spi.SourceDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * EDP pushes and dry-run validation (prompt 024, docs/External-Data-Protocol.md §3 and §4.1).
 *
 * <p>A push: a pending or suspended source answers {@code 409 SOURCE_PENDING_APPROVAL} /
 * {@code SOURCE_SUSPENDED} before anything is read (invariant EXT-1); the batch is read (JSON or
 * NDJSON, 5 MB, depth 32, 10,000 items) and its header checked ({@code PROTOCOL_UNSUPPORTED},
 * {@code SOURCE_MISMATCH}); the source is admitted through {@link PushIngestService} (request
 * budget); items are validated and mapped by {@link EdpBatchProcessor}; the valid ones go to the
 * writer as one {@code PUSH} batch. The result reports accepted items, every rejection with its
 * schema errors (validation and writer rejections), and the writer's {@code IngestOutcome}.
 */
@Service
public class EdpIngestService {

    private final EdpManifestService manifests;
    private final EdpBatchReader reader;
    private final EdpBatchProcessor processor;
    private final EdpItemValidator validator;
    private final PushIngestService pushes;
    private final Clock clock;

    /** Creates the service. */
    public EdpIngestService(EdpManifestService manifests, EdpBatchReader reader, EdpBatchProcessor processor,
            EdpItemValidator validator, PushIngestService pushes, Clock clock) {
        this.manifests = manifests;
        this.reader = reader;
        this.processor = processor;
        this.validator = validator;
        this.pushes = pushes;
        this.clock = clock;
    }

    /**
     * Pushes the request's batch into {@code sourceId}.
     *
     * @return the {@code EdpIngestResult}
     */
    public Map<String, Object> push(String sourceId, HttpServletRequest request) {
        IngestCaller caller = InboundRequests.caller(request, sourceId);
        ExternalSourceEntity ext = manifests.require(sourceId);
        requireActive(ext);
        Instant receivedAt = clock.instant();
        byte[] body = IngestBodyReader.read(request, ai.foresignal.inbound.edp.EdpProtocol.MAX_BATCH_BYTES);
        MediaType contentType = contentType(request);
        EdpBatchReader.EdpBatch batch = reader.read(body, contentType);
        EdpManifestModel manifest = manifests.model(ext);
        EdpBatchChecks.checkHeader(batch.header(), manifest, sourceId);

        String url = IngestUrls.redacted(request);
        SourceDescriptor descriptor = pushes.admit(sourceId, caller.principal(), receivedAt, url);
        EdpBatchProcessor.Processed processed =
                processor.process(sourceId, manifest, manifests.schemas(ext), batch, false, receivedAt);

        IngestOutcome outcome = IngestOutcome.empty();
        IngestOutcomeView view = IngestOutcomeView.of(outcome, 0, List.of());
        if (!processed.records().isEmpty()) {
            ParsedIngest parsed = new ParsedIngest(processed.records(), processed.indexes(), List.of(),
                    processed.records().size());
            PushIngestService.PushResult result = pushes.submit(descriptor, new PushIngestService.PushRequest(
                    sourceId, caller.principal(), parsed, receivedAt, body.length, InboundRequests.sha256(body), url));
            outcome = result.outcome();
            view = result.view();
        } else {
            pushes.recordAccepted(sourceId);
        }
        List<EdpItemRejection> writerRejections = writerRejections(processed, outcome);
        processor.recordActivity(sourceId, processed, writerRejections, receivedAt);
        if (!processed.valid().isEmpty()) {
            manifests.touchLastItem(sourceId, receivedAt);
        }

        List<EdpItemRejection> rejected = new ArrayList<>(processed.rejected());
        rejected.addAll(writerRejections);
        rejected.sort(java.util.Comparator.comparingInt(EdpItemRejection::index));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("accepted", processed.valid().size() - writerRejections.size());
        out.put("rejected", rejected);
        out.put("outcome", view);
        return out;
    }

    /**
     * {@code validateEdp}: checks a manifest and/or items without storing anything.
     *
     * @return the {@code EdpValidationReport}
     */
    public Map<String, Object> validate(JsonNode request) {
        if (request == null || !request.isObject()) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        JsonNode manifestNode = request.get("manifest");
        String sourceId = request.path("sourceId").isString() ? request.get("sourceId").asString() : null;
        EdpManifestModel manifest = null;
        Map<String, CompiledPayloadSchema> schemas = Map.of();
        if (manifestNode != null && !manifestNode.isNull()) {
            EdpManifestValidator.ManifestCheck check = manifests.validate(manifestNode);
            out.put("manifestValid", check.valid());
            out.put("manifestErrors", check.errors());
            if (check.valid()) {
                manifest = check.model();
                schemas = check.payloadSchemas();
                if (sourceId != null) {
                    ExternalSourceEntity ext = manifests.require(sourceId);
                    out.put("breakingChanges", EdpManifestService.breakingChanges(manifests.model(ext), manifest));
                }
            }
        } else {
            out.put("manifestValid", null);
            out.put("manifestErrors", List.of());
        }
        JsonNode items = request.get("items");
        if (items != null && items.isArray() && !items.isEmpty()) {
            if (manifest == null && sourceId != null) {
                ExternalSourceEntity ext = manifests.require(sourceId);
                manifest = manifests.model(ext);
                schemas = manifests.schemas(ext);
            }
            if (manifest == null) {
                throw new InvalidInputException(
                        "Items need a valid manifest or the sourceId of a registered provider.");
            }
            List<JsonNode> list = new ArrayList<>();
            items.forEach(list::add);
            EdpItemValidator.ItemCheck check = validator.validate(list, manifest, schemas, clock.instant());
            out.put("itemsValid", check.valid().size());
            out.put("rejected", check.rejected());
        } else {
            out.put("itemsValid", 0);
            out.put("rejected", List.of());
        }
        return out;
    }

    /**
     * Refuses a pending or suspended provider.
     *
     * @throws ConflictException {@code SOURCE_PENDING_APPROVAL} or {@code SOURCE_SUSPENDED}
     */
    static void requireActive(ExternalSourceEntity ext) {
        if (ExternalSourceEntity.PENDING.equals(ext.getState())) {
            throw new ConflictException(ErrorCodes.SOURCE_PENDING_APPROVAL,
                    ext.getSourceId() + " is awaiting the operator's approval; nothing is stored until then.");
        }
        if (ExternalSourceEntity.SUSPENDED.equals(ext.getState())) {
            throw new ConflictException(ErrorCodes.SOURCE_SUSPENDED, ext.getSourceId() + " is suspended.");
        }
    }

    /** The writer's rejections as EDP rejections, with the item's stream and id. */
    static List<EdpItemRejection> writerRejections(EdpBatchProcessor.Processed processed, IngestOutcome outcome) {
        List<EdpItemRejection> out = new ArrayList<>();
        Map<Integer, EdpValidItem> byIndex = new LinkedHashMap<>();
        processed.valid().forEach(item -> byIndex.put(item.index(), item));
        for (Rejection rejection : outcome.rejections()) {
            int position = rejection.index();
            int index = position >= 0 && position < processed.indexes().size() ? processed.indexes().get(position) : -1;
            EdpValidItem item = byIndex.get(index);
            out.add(EdpItemRejection.of(index, item == null || item.stream() == null ? null : item.stream().id(),
                    item == null ? null : item.id(), rejection.reason().name(), rejection.detail()));
        }
        return out;
    }

    private static MediaType contentType(HttpServletRequest request) {
        try {
            return request.getContentType() == null ? MediaType.APPLICATION_JSON
                    : MediaType.parseMediaType(request.getContentType());
        } catch (org.springframework.util.InvalidMimeTypeException e) {
            throw new InvalidInputException("The Content-Type is not valid.");
        }
    }
}
