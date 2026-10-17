package ai.foresignal.baserate.reference;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The upstream paths of reference imports (prompt 121), all through {@link UpstreamClient} so budgets, backoff, the
 * User-Agent, the byte cap and redaction apply: a budgeted system client ({@value #SYSTEM_ID}, {@value #PER_MINUTE}
 * requests a minute and {@value #PER_DAY} a day) for USGS ComCat and GDACS, and a registered source's own client (and
 * budget) for UCDP and ACLED. A request refused by a per-minute budget waits for it once (up to
 * {@value #MAX_WAIT_SECONDS} s); a spent daily budget fails the chunk, and the import resumes there when submitted
 * again.
 */
@Component
public class ReferenceHttp {

    /** The system client's id (usage and backoff are metered under it). */
    public static final String SYSTEM_ID = "reference-import";

    /** The system client's requests per minute. */
    public static final int PER_MINUTE = 30;

    /** The system client's requests per day. */
    public static final int PER_DAY = 2_000;

    /** The longest wait for a per-minute budget. */
    public static final long MAX_WAIT_SECONDS = 65;

    /** The system client's response cap: a year of M4.5+ events is about 2 MB of CSV. */
    public static final int MAX_RESPONSE_BYTES = 64 * 1024 * 1024;

    /**
     * Sends a GET.
     */
    @FunctionalInterface
    public interface Fetcher {

        /**
         * Sends a GET.
         *
         * @param uri the URL
         * @param headers extra request headers
         * @return the status and body (a 4xx or 5xx is returned, not thrown)
         * @throws Exception when the request could not be made (budget spent, I/O, backoff)
         */
        Response get(URI uri, Map<String, String> headers) throws Exception;
    }

    /**
     * A response.
     *
     * @param status the HTTP status
     * @param body the body, never null
     */
    public record Response(int status, byte[] body) {

        /** Copies the body. */
        public Response {
            body = body == null ? new byte[0] : body;
        }
    }

    private final UpstreamClient upstream;
    private final SourceRegistry registry;
    private final BudgetGovernor budgets;

    /** Creates the component. */
    public ReferenceHttp(UpstreamClient upstream, SourceRegistry registry, BudgetGovernor budgets) {
        this.upstream = upstream;
        this.registry = registry;
        this.budgets = budgets;
    }

    /** The system client (USGS ComCat, GDACS). */
    public Fetcher system() {
        SourceDescriptor descriptor = SourceDescriptor.builder(SYSTEM_ID)
                .name("Reference-history imports")
                .description("Reference-history imports of public datasets (prompt 121)")
                .mode(Mode.POLLING)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .build();
        return fetcher(SYSTEM_ID, upstream.forSystem(descriptor, Budget.of(PER_MINUTE, PER_DAY)));
    }

    /** A registered source's own client and budget (UCDP, ACLED). */
    public Fetcher source(String sourceId) {
        return fetcher(sourceId, http(sourceId));
    }

    /** A registered source's {@link SourceHttp} (the ACLED token requests need it). */
    public SourceHttp http(String sourceId) {
        return upstream.forSource(sourceId, registry.descriptor(sourceId));
    }

    private Fetcher fetcher(String meterId, SourceHttp http) {
        return (uri, headers) -> {
            for (int attempt = 0; ; attempt++) {
                try {
                    UpstreamResponse response = http.get(uri, headers == null ? Map.of() : headers);
                    if (response.truncated()) {
                        throw new IllegalStateException(uri.getHost() + " answered more than the response cap");
                    }
                    return new Response(response.status(), response.body());
                } catch (BudgetExceededException e) {
                    long wait = budgets.secondsUntilAvailable(meterId);
                    if (attempt > 0 || wait <= 0 || wait > MAX_WAIT_SECONDS) {
                        throw e;
                    }
                    Thread.sleep(wait * 1000);
                } catch (AdapterException e) {
                    if (e.httpStatus().isPresent()) {
                        return new Response(e.httpStatus().getAsInt(), new byte[0]);
                    }
                    throw e;
                }
            }
        };
    }
}
