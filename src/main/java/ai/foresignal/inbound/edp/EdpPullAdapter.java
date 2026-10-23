package ai.foresignal.inbound.edp;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

/**
 * Polls one EDP provider's items endpoint (docs/External-Data-Protocol.md §4.2, prompt 024).
 *
 * <p>Registered as the POLLING implementation of a provider whose manifest declares
 * {@code transport.pull}, so the collector scheduler runs it every {@code intervalSeconds} while the
 * provider is approved (enabled), and its failures become health and backoff like any adapter's.
 *
 * <p>Each run calls {@code itemsUrl?cursor=&limit=500} and follows {@code nextCursor} until it is
 * null (at most {@value #MAX_PAGES} pages per run), storing the cursor after each page. Every request
 * goes through {@link EdpHttpClient} (the URL guard, a pinned address, 5 MB, same-host redirects) and
 * takes one token from the source's budget; a spent budget ends the run early (or refuses it, on the
 * first page). The credential comes from {@code PUT /v1/sources/{id}/credentials}: {@code token}
 * (bearer), {@code value} (header) or {@code username}/{@code password} (basic). Items are validated,
 * mapped and snapshot-checked by {@link EdpBatchProcessor}; a pull snapshot ends with the page whose
 * {@code nextCursor} is null.
 */
public class EdpPullAdapter implements PollingAdapter {

    /** The page size asked of the provider. */
    public static final int PAGE_LIMIT = 500;

    /** The most pages one run follows; the next run continues from the stored cursor. */
    public static final int MAX_PAGES = 20;

    private final SourceDescriptor descriptor;
    private final EdpPullAdapters context;

    /** Creates the adapter. */
    EdpPullAdapter(SourceDescriptor descriptor, EdpPullAdapters context) {
        this.descriptor = descriptor;
        this.context = context;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String sourceId = descriptor.id();
        EdpManifestService manifests = context.manifests();
        ExternalSourceEntity ext = manifests.require(sourceId);
        if (!ExternalSourceEntity.ACTIVE.equals(ext.getState())) {
            return FetchResult.of(List.of());
        }
        EdpManifestModel manifest = manifests.model(ext);
        if (manifest.pull() == null) {
            return FetchResult.of(List.of());
        }
        Map<String, CompiledPayloadSchema> schemas = manifests.schemas(ext);
        Map<String, String> headers = authHeaders(manifest.pull(), ctx.credentials());
        String cursor = ext.getPullCursor();
        List<NormalizedRecord> records = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            if (!context.budgets().tryConsume(sourceId)) {
                if (page == 0) {
                    throw new BudgetExceededException(sourceId, "the provider's request budget is spent");
                }
                break;
            }
            URI uri = pageUri(manifest.pull().itemsUrl(), cursor);
            Instant receivedAt = context.clock().instant();
            EdpHttpClient.EdpHttpResponse response;
            try {
                response = context.http().get(uri, headers, ext.isAllowPrivateNetwork(), EdpProtocol.MAX_BATCH_BYTES);
            } catch (AdapterException e) {
                context.usage().record(sourceId, 0, false);
                manifests.recordPull(sourceId, null, receivedAt, e.getMessage());
                throw e;
            }
            context.usage().record(sourceId, response.body().length, true);
            EdpBatchReader.EdpBatch batch;
            try {
                batch = context.reader().read(response.body(), MediaType.APPLICATION_JSON);
                EdpBatchChecks.checkHeader(batch.header(), manifest, sourceId);
            } catch (ForesignalException e) {
                manifests.recordPull(sourceId, null, receivedAt, e.getMessage());
                throw new UpstreamFormatException("The provider returned an invalid EDP batch: " + e.getMessage(),
                        response.bodyText(), e);
            }
            String next = text(batch.header(), "nextCursor");
            EdpBatchProcessor.Processed processed =
                    context.processor().process(sourceId, manifest, schemas, batch, next == null, receivedAt);
            records.addAll(processed.records());
            context.processor().recordActivity(sourceId, processed, List.of(), receivedAt);
            if (!processed.valid().isEmpty()) {
                manifests.touchLastItem(sourceId, receivedAt);
            }
            if (next == null) {
                manifests.recordPull(sourceId, cursor, receivedAt, null);
                break;
            }
            cursor = next;
            manifests.recordPull(sourceId, cursor, receivedAt, null);
        }
        return FetchResult.of(records);
    }

    /** {@code itemsUrl?cursor=…&limit=500}, keeping any query the URL already has. */
    static URI pageUri(String itemsUrl, String cursor) {
        StringBuilder out = new StringBuilder(itemsUrl);
        out.append(itemsUrl.contains("?") ? '&' : '?');
        if (cursor != null) {
            out.append("cursor=").append(URLEncoder.encode(cursor, StandardCharsets.UTF_8)).append('&');
        }
        out.append("limit=").append(PAGE_LIMIT);
        return URI.create(out.toString());
    }

    /**
     * The request headers of the manifest's auth style.
     *
     * @throws AdapterException when the style needs a credential that is not stored
     */
    static Map<String, String> authHeaders(EdpManifestModel.Pull pull, Map<String, String> credentials)
            throws AdapterException {
        return authHeaders(pull.auth(), pull.header(), credentials);
    }

    /**
     * The request headers of an auth style ({@code none}, {@code bearer}, {@code header}, {@code basic}), shared
     * by {@code transport.pull} and {@code transport.search}.
     *
     * @throws AdapterException when the style needs a credential that is not stored
     */
    static Map<String, String> authHeaders(String auth, String header, Map<String, String> credentials)
            throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        Map<String, String> c = credentials == null ? Map.of() : credentials;
        switch (auth == null ? "none" : auth) {
            case "bearer" -> headers.put("Authorization", "Bearer " + required(c, "token"));
            case "header" -> {
                if (header == null || header.isBlank()) {
                    throw new AdapterException("The manifest's header auth names no header");
                }
                headers.put(header, required(c, "value"));
            }
            case "basic" -> headers.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                    (required(c, "username") + ":" + required(c, "password")).getBytes(StandardCharsets.UTF_8)));
            default -> {
                // none
            }
        }
        return headers;
    }

    private static String required(Map<String, String> credentials, String name) throws AdapterException {
        String value = credentials.get(name);
        if (value == null || value.isEmpty()) {
            throw new AdapterException("The provider needs the credential '" + name
                    + "'; set it with PUT /v1/sources/{sourceId}/credentials");
        }
        return value;
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
