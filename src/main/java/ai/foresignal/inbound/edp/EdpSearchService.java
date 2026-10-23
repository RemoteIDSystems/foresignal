package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.inbound.InboundRequests;
import ai.foresignal.inbound.PushIngestService;
import ai.foresignal.inbound.format.ParsedIngest;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Calls the providers' {@code transport.search} for a cue (OpenAPI webhook {@code edpProviderSearch},
 * docs/External-Data-Protocol.md §4.3, prompt 137).
 *
 * <p>Every active provider whose manifest declares {@code transport.search} is asked
 * {@code GET {url}?q=&since=&until=&bbox=&lang=&limit=} once per query, at most {@value #MAX_QUERIES} queries a
 * cue. Each call is admitted like a push ({@link PushIngestService#admit}: the source must be enabled, and the
 * call takes one request from its budget), authenticated as the manifest's {@code transport.search.auth} says with
 * the source's stored credential, read as an EDP batch and validated and mapped like a push. A search result is
 * never the end of a snapshot, so items it leaves out are not deleted. The records are written to the provider's
 * source marked as cue results ({@code payload.collection = cue}, {@code payload.cueId}), as cue-found social posts
 * are, so they never feed volume baselines.
 */
@Service
public class EdpSearchService {

    private static final Logger log = LoggerFactory.getLogger(EdpSearchService.class);

    /** The most queries sent to one provider for one cue. */
    public static final int MAX_QUERIES = 10;

    /** The {@code payload.collection} of a cue result, as on cue-found social posts. */
    public static final String CUE_COLLECTION = "cue";

    private final EdpManifestService manifests;
    private final EdpHttpClient http;
    private final EdpBatchReader reader;
    private final EdpBatchProcessor processor;
    private final PushIngestService pushes;
    private final CredentialsAccess credentials;
    private final Clock clock;

    /** Creates the service. */
    public EdpSearchService(EdpManifestService manifests, EdpHttpClient http, EdpBatchReader reader,
            EdpBatchProcessor processor, PushIngestService pushes, CredentialsAccess credentials, Clock clock) {
        this.manifests = manifests;
        this.http = http;
        this.reader = reader;
        this.processor = processor;
        this.pushes = pushes;
        this.credentials = credentials;
        this.clock = clock;
    }

    /**
     * One search: the cue's queries, window, area and result limit.
     *
     * @param cueId the cue
     * @param queries the query texts with their languages (null for any), in order
     * @param since the window start, or null
     * @param until the window end, or null
     * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
     * @param limit the items asked per query
     */
    public record Search(String cueId, List<Query> queries, Instant since, Instant until, List<Double> bbox,
            int limit) {}

    /**
     * One query.
     *
     * @param text the query text
     * @param language a BCP 47 language, or null
     */
    public record Query(String text, String language) {}

    /**
     * What one provider returned ({@code SocialCue.externalResults} items).
     *
     * @param sourceId the provider's source
     * @param queries the queries sent
     * @param items the records stored
     * @param error the first failure, or null
     */
    public record Result(String sourceId, int queries, int items, String error) {

        /** The {@code SocialCue.externalResults} item. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sourceId", sourceId);
            out.put("queries", queries);
            out.put("items", items);
            out.put("error", error);
            return out;
        }
    }

    /** Searches every active provider that declares {@code transport.search}; one result per provider. */
    public List<Result> search(Search search) {
        List<Result> out = new ArrayList<>();
        for (ExternalSourceEntity ext : manifests.list(ExternalSourceEntity.ACTIVE)) {
            EdpManifestModel manifest;
            try {
                manifest = manifests.model(ext);
            } catch (RuntimeException e) {
                continue;
            }
            if (manifest.search() != null) {
                out.add(searchOne(ext, manifest, search));
            }
        }
        return out;
    }

    private Result searchOne(ExternalSourceEntity ext, EdpManifestModel manifest, Search search) {
        String sourceId = ext.getSourceId();
        int sent = 0;
        int stored = 0;
        String error = null;
        Map<String, String> headers;
        try {
            headers = EdpPullAdapter.authHeaders(manifest.search().auth(), manifest.search().header(),
                    credentials.forSource(sourceId));
        } catch (AdapterException e) {
            return new Result(sourceId, 0, 0, e.getMessage());
        }
        for (Query query : search.queries().subList(0, Math.min(MAX_QUERIES, search.queries().size()))) {
            Instant receivedAt = clock.instant();
            SourceDescriptor descriptor;
            try {
                descriptor = pushes.admit(sourceId, principal(search), receivedAt, manifest.search().url());
            } catch (ForesignalException e) {
                error = error == null ? refusal(e) : error;
                break;
            }
            sent++;
            try {
                stored += searchQuery(ext, manifest, descriptor, search, query, headers, receivedAt);
            } catch (AdapterException | ForesignalException e) {
                error = error == null ? e.getMessage() : error;
                pushes.recordFailure(sourceId, e);
                log.debug("EDP search of {} for cue {} failed: {}", sourceId, search.cueId(), e.toString());
            }
        }
        return new Result(sourceId, sent, stored, error);
    }

    private int searchQuery(ExternalSourceEntity ext, EdpManifestModel manifest, SourceDescriptor descriptor,
            Search search, Query query, Map<String, String> headers, Instant receivedAt) throws AdapterException {
        String sourceId = ext.getSourceId();
        URI uri = searchUri(manifest.search().url(), query, search.since(), search.until(), search.bbox(),
                search.limit());
        EdpHttpClient.EdpHttpResponse response = http.get(uri, headers, ext.isAllowPrivateNetwork(),
                EdpProtocol.MAX_BATCH_BYTES);
        EdpBatchReader.EdpBatch batch = reader.read(response.body(), MediaType.APPLICATION_JSON);
        EdpBatchChecks.checkHeader(batch.header(), manifest, sourceId);
        EdpBatchProcessor.Processed processed =
                processor.process(sourceId, manifest, manifests.schemas(ext), batch, false, receivedAt);
        List<NormalizedRecord> records = processed.records().stream().map(r -> cueResult(r, search.cueId())).toList();
        if (records.isEmpty()) {
            pushes.recordAccepted(sourceId);
        } else {
            ParsedIngest parsed = new ParsedIngest(records, processed.indexes(), List.of(), records.size());
            pushes.submit(descriptor, new PushIngestService.PushRequest(sourceId, principal(search), parsed,
                    receivedAt, response.body().length, InboundRequests.sha256(response.body()),
                    manifest.search().url()));
        }
        processor.recordActivity(sourceId, processed, List.of(), receivedAt);
        if (!processed.valid().isEmpty()) {
            manifests.touchLastItem(sourceId, receivedAt);
        }
        return records.size();
    }

    /** {@code url?q=&since=&until=&bbox=&lang=&limit=}, leaving out what is null and keeping any query it has. */
    static URI searchUri(String url, Query query, Instant since, Instant until, List<Double> bbox, int limit) {
        StringBuilder out = new StringBuilder(url);
        out.append(url.contains("?") ? '&' : '?');
        out.append("q=").append(encode(query.text()));
        if (since != null) {
            out.append("&since=").append(encode(since.toString()));
        }
        if (until != null) {
            out.append("&until=").append(encode(until.toString()));
        }
        if (bbox != null && bbox.size() == 4) {
            out.append("&bbox=").append(encode(bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3)));
        }
        if (query.language() != null && !query.language().isBlank()) {
            out.append("&lang=").append(encode(query.language()));
        }
        out.append("&limit=").append(limit);
        return URI.create(out.toString());
    }

    /** {@code record} with {@code payload.collection = cue} and {@code payload.cueId}. */
    static NormalizedRecord cueResult(NormalizedRecord record, String cueId) {
        if (record.tombstone()) {
            return record;
        }
        Map<String, Object> payload = new LinkedHashMap<>(record.payload());
        payload.put("collection", CUE_COLLECTION);
        payload.put("cueId", cueId);
        return new NormalizedRecord(record.externalId(), record.kind(), record.observedAt(), record.eventTime(),
                record.geometry(), record.validUntil(), record.validFrom(), payload, false, record.quality(),
                record.observers());
    }

    private static String principal(Search search) {
        return "cue:" + search.cueId();
    }

    private static String refusal(ForesignalException e) {
        return e.getStatus() == HttpStatus.TOO_MANY_REQUESTS ? "BUDGET_EXCEEDED: " + e.getMessage() : e.getMessage();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
