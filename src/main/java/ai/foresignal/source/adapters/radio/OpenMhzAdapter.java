package ai.foresignal.source.adapters.radio;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code openmhz-systems} (prompt 082): the trunked-radio scanner systems OpenMHz lists ({@link OpenMhzParser}).
 *
 * <p><strong>Blocked (verified 2026-10-14):</strong> {@value #SYSTEMS_URL} answers Foresignal's identifiable
 * User-Agent with a Cloudflare challenge: HTTP 403, {@code cf-mitigated: challenge} and an HTML page. A response with
 * that header, or any HTML body where JSON is expected, is reported as {@link UpstreamHttpException} with reason
 * {@value UpstreamHttpException#REASON_BLOCKED_CHALLENGE}, so health goes {@code failing} with a reason that says
 * why. Foresignal never solves the challenge, never sends a browser User-Agent and never retries with other headers:
 * one request per run. The source stays failing until OpenMHz admits API clients.
 */
@Component
public class OpenMhzAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "openmhz-systems";

    /** The systems catalog. */
    public static final String SYSTEMS_URL = "https://api.openmhz.com/systems";

    /** Raw retention: a catalog is reference data (Conventions), 90 d of history. */
    static final Duration RETENTION = Duration.ofDays(90);

    private final URI systems;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value RadioBrowserAdapter#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public OpenMhzAdapter(@Value("${" + RadioBrowserAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.systems = RadioSources.uri(SYSTEMS_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The catalog URL. */
    public URI systemsUri() {
        return systems;
    }

    /**
     * Reads the catalog once.
     *
     * @throws UpstreamHttpException with reason {@value UpstreamHttpException#REASON_BLOCKED_CHALLENGE} for a
     *     challenge page, or as the client raised it for any other error status
     * @throws AdapterException when the request fails
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(systems);
        } catch (UpstreamHttpException e) {
            if (!e.isBlockedChallenge() && !(e instanceof ai.foresignal.collect.UpstreamAccessDeniedException)
                    && OpenMhzParser.looksLikeHtml(e.getBodySnippet().getBytes(StandardCharsets.UTF_8), null)) {
                throw new UpstreamHttpException(e.getStatus(), e.getBodySnippet(),
                        UpstreamHttpException.REASON_BLOCKED_CHALLENGE);
            }
            throw e;
        }
        boolean challenge = response.header("cf-mitigated") != null
                && response.header("cf-mitigated").strip().equalsIgnoreCase("challenge");
        if (challenge || OpenMhzParser.looksLikeHtml(response.body(), response.header("Content-Type"))) {
            throw new UpstreamHttpException(response.status(),
                    UpstreamHttpException.snippet(response.bodyAsString()), UpstreamHttpException.REASON_BLOCKED_CHALLENGE);
        }
        return FetchResult.complete(OpenMhzParser.parse(RadioSources.body(ctx, response, null, systems)));
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = OpenMhzParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("OpenMHz scanner systems")
                .description("The trunked-radio systems OpenMHz records (name, location, last activity, listeners)."
                        + " As of 2026-10 the API answers Foresignal's identifiable client with a Cloudflare challenge"
                        + " (HTTP 403), so this source reports BLOCKED_CHALLENGE and stays failing until OpenMHz admits"
                        + " API clients; Foresignal never imitates a browser. Calls themselves are not fetched.")
                .kind(kind)
                .homepage("https://openmhz.com/")
                .attribution("OpenMHz (openmhz.com)")
                .licence("unspecified (OpenMHz)")
                .termsUrl("https://openmhz.com/")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(1), Duration.ofHours(1))
                .defaultBudget(Budget.of(1, 30))
                // Every official path answers a Cloudflare challenge (2026-10-22), so it is not switched on unasked.
                .autoEnable(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .volatileFields("clientCount")
                .titleField("name")
                .searchFields("name", "location", "county")
                .tileProperties("name", "state");
        RadioSources.field(b, "name", FieldType.STRING, null, "Name", "The system's name");
        RadioSources.field(b, "shortName", FieldType.STRING, null, "Short name", "OpenMHz's id for the system");
        RadioSources.field(b, "location", FieldType.STRING, null, "Location", "Where the system is");
        RadioSources.field(b, "state", FieldType.STRING, null, "State", "The state or region");
        RadioSources.field(b, "county", FieldType.STRING, null, "County", "The county");
        RadioSources.field(b, "lastActive", FieldType.TIMESTAMP, null, "Last active", "The last recorded call");
        RadioSources.field(b, "clientCount", FieldType.INTEGER, null, "Listeners", "Current listeners (volatile)");
        b.kindDisplay(kind, new KindDisplay("{name}", "radio", "#862E9C", null));
        return b.build();
    }
}
